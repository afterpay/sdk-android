plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
}

subprojects {
    val VERSION_NAME: String by project
    val GROUP: String by project
    group = GROUP
    version = VERSION_NAME

    // TODO @jatwood re-add ktlint via a plugin (e.g. spotless)
}
