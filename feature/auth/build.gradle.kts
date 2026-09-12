plugins {
    id("mangro.android.library")
    id("mangro.android.compose")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.swyp.mangro.feature.auth"

    flavorDimensions += "role"
    productFlavors {
        create("owner") { dimension = "role" }
        create("consumer") { dimension = "role" }
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:utils"))

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
}
