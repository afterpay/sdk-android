plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.secrets.gradle.plugin)
}

android {
    namespace = "com.example"

    compileSdk = libs.versions.exampleCompileSdk.get().toInt()

    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    defaultConfig {
        applicationId = "com.afterpay.android.sample"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.exampleCompileSdk.get().toInt()

        versionCode = 1
        versionName = "1.0"
    }
    kotlinOptions {
        jvmTarget = libs.versions.java.get()
    }
}

dependencies {
    // toggle between using Maven artifact and local module
    implementation(projects.afterpay)
    // implementation(libs.afterpay.android)

    implementation(libs.app.cash.paykit)

    implementation(libs.androidxAppcompat)
    implementation(libs.androidxCoreKtx)
    implementation(libs.androidxLifecycleRuntimeKtx)
    implementation(libs.material)
}
