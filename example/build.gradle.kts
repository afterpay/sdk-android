import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    compileSdk = libs.versions.exampleCompileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    defaultConfig {
        applicationId = "com.example.afterpay"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.exampleCompileSdk.get().toInt()

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        create("staging") {
            initWith(getByName("debug"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = libs.versions.java.get()
    }

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    namespace = "com.example.afterpay"
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs += listOf("-Xallow-result-return-type")
}

dependencies {
    implementation(projects.afterpay)

    implementation(libs.kotlinReflect)
    implementation(libs.kotlinxCoroutinesAndroid)
    implementation(libs.androidxCoreKtx)
    implementation(libs.androidxAppcompat)
    implementation(libs.androidXActivityKtx)
    implementation(libs.androidXConstraintLayout)
    implementation(libs.androidXFragmentKtx)
    implementation(libs.androidxLifecycleRuntimeKtx)

    implementation(libs.androidXLifecycleLivedataKtx)
    implementation(libs.androidXLifecycleViewmodelKtx)
    implementation(libs.androidxNavigationFragmentKtx)
    implementation(libs.androidxNavigationUiKtx)
    implementation(libs.androidXRecyclerview)
    implementation(libs.material)
    implementation(libs.moshi)
    implementation(libs.moshiKotlin)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.converterMoshi)
    implementation(libs.threetenabp)
    implementation(libs.paykit)

    androidTestUtil(libs.androidxTestOrchestrator)

    androidTestImplementation(libs.androidXTestCore)
    androidTestImplementation(libs.extJunit)
    androidTestImplementation(libs.androidXTestRunner)
    androidTestImplementation(libs.espressoCore)
    androidTestImplementation(libs.espressoContrib)
    androidTestImplementation(libs.espressoIdlingResource)
    androidTestImplementation(libs.espressoWeb)
    androidTestImplementation(libs.uiautomator)
}
