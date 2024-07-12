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
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.spotless)
}

subprojects {
    val VERSION_NAME: String by project
    val GROUP: String by project
    group = GROUP
    version = VERSION_NAME

    apply(plugin = "com.diffplug.spotless")
    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("$buildDir/**/*.kt")
            ktlint()
                .editorConfigOverride(
                    mapOf(
                        "ktlint_standard_multiline-expression-wrapping" to "disabled",
                    ),
                )
            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
            licenseHeaderFile(rootProject.file("gradle/license-header.txt"))
        }

        kotlinGradle {
            target("*.gradle.kts")
            ktlint()
                .editorConfigOverride(
                    mapOf(
                        // Sometimes we need ALL_CAPS properties,
                        //  esp. those pulled from gradle.properties

                        "ktlint_standard_property-naming" to "disabled",
                    ),
                )
        }
    }
}
