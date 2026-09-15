plugins {
    id("mangro.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.swyp.core.local"

    flavorDimensions += "role"
    productFlavors {
        create("owner") { dimension = "role" }
        create("consumer") { dimension = "role" }
    }
}

dependencies {
    implementation(project(":core:crypto"))
    implementation(libs.androidx.datastore.preferences)
    api(libs.kotlinx.coroutines.core)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
