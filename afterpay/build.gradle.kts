/*
 * Copyright (C) 2024 Afterpay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.vanniktech.maven.publish)
    alias(libs.plugins.kotlin.serialization)
}

android {
    compileSdk = libs.versions.compileSdkVersion.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdkVersion.get().toInt()

        val VERSION_NAME: String by project
        buildConfigField("String", "AfterpayLibraryVersion", "\"$VERSION_NAME\"")

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        create("staging") {
            initWith(getByName("debug"))
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = libs.versions.java.get()
    }

    namespace = "com.afterpay.android"
}

dependencies {
    implementation(libs.kotlinCoroutinesAndroid)
    implementation(libs.kotlinSerializationJson)
    implementation(libs.kotlinCoroutinesJdk8)
    implementation(libs.androidxLifecycleRuntimeKtx)

    implementation(libs.androidxCoreKtx)
    implementation(libs.androidxAppcompat)

    testImplementation(libs.junit)
    coreLibraryDesugaring(libs.androidToolsDesugarJdk)
    testImplementation(libs.kotlinCoroutinesTest)
    testImplementation(libs.mockK)
}

tasks.withType<Sign> {
    val version = project.version.toString()
    onlyIf { !version.endsWith("SNAPSHOT") }
}

signing {
    useInMemoryPgpKeys(
        findProperty("signingKeyId").toString(),
        findProperty("signingKey").toString(),
        findProperty("signingPassword").toString(),
    )
}
