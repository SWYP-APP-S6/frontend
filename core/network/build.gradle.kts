plugins {
    id("mangro.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.swyp.mangro.core.network"
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    api(libs.retrofit.core)
    api(libs.kotlinx.serialization.json)
    api(platform(libs.okhttp.bom))
    api(libs.okhttp.core)
    implementation(libs.retrofit.kotlinx.serialization)
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
}
