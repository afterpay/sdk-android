---
layout: default
title: Installation
nav_order: 2
---

# Installation

## Requirements

- Android 7.0 (API level 24) and above

{: .info }
> While the native code in the SDK is Android API level 24+ compatible, the Afterpay / Clearpay checkout is a WebView wrapper for the web checkout which requires specific browser features. This means that while the SDK code will run, the checkout will depend on the version of WebView that the user has installed. As this is independent of the OS version, this will be up to the implementation to handle. If the WebView version is known to the app, then all Afterpay / Clearpay assets can be displayed on condition of the WebView version.

## Configuration

Add `afterpay-android` to your `build.gradle` dependencies.

### Release

``` gradle
dependencies {
    implementation 'com.afterpay:afterpay-android:4.8.3'
}
```

### Snapshot

{: .alert }
> Snapshot builds are available for testing and development purposes. These builds are not recommended for production use.

Follow the steps below to add the latest snapshot build to your project.

**Step 1**: Add the maven repository where to download the snapshot to your `build.gradle` file (or any other file where you define your repositories e.g. `settings.gradle`), in the `repositories` block.

``` gradle
repositories {
    ...
    maven {
        url  = 'https://oss.sonatype.org/content/repositories/snapshots/'
    }
    ...
}
```

**Step 2**: Add the dependency to your `build.gradle` file in the `dependencies` block.

``` gradle
dependencies {
    implementation 'com.afterpay:afterpay-android:4.8.3-SNAPSHOT' # or specific "vX.X.X-SNAPSHOT" version of this library.
}
```

## ProGuard

If you are using R8 the shrinking and obfuscation rules are included automatically.

Proguard users will need to manually apply the rules defined in [`consumer-rules.pro`][proguard-rules]{:target="_blank"}.

[proguard-rules]: https://github.com/afterpay/sdk-android/blob/master/afterpay/consumer-rules.pro
