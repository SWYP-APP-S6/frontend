plugins {
    id("mangro.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android { namespace = "com.swyp.mangro.data.user" }

dependencies {
    implementation(project(":remote:user"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.retrofit.core)
    testImplementation(project(":core:network"))
    testImplementation(libs.retrofit.kotlinx.serialization)
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(platform(libs.okhttp.bom))
    testImplementation(libs.okhttp.core)
    testImplementation(libs.okhttp.mockwebserver)
}
