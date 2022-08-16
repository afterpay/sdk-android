---
layout: default
title: Security
nav_order: 5
---

# Security

To limit the possibility of a man-in-the-middle attack during the checkout process, certificate pinning can be configured for the Afterpay portal. Please refer to the Android [Network Security Configuration][network-config]{:target="_blank"} documentation for more information.

Add the following configuration to your `res/xml/network_security_configuration.xml` to enforce certificate pinning for the Afterpay portal.

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config xmlns:tools="http://schemas.android.com/tools">
    <domain-config cleartextTrafficPermitted="false">
        <domain>portal.afterpay.com</domain>
        <pin-set expiration="2022-05-25">
            <pin digest="SHA-256">nQ1Tu17lpJ/Hsr3545eCkig+X9ZPcxRQoe5WMSyyqJI=</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

{: .note }
> It is necessary to keep the certificate PINs updated to ensure pinning will not be bypassed beyond the expiry date of the certificate.

[network-config]: https://developer.android.com/training/articles/security-config#CertificatePinning
