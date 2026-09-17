plugins {
    id("mangro.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.swyp.mangro.data.owner.product"
}

dependencies {
    api(libs.androidx.paging.common)
    implementation(project(":remote:owner"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.retrofit.core)
    testImplementation(project(":core:network"))
    testImplementation(libs.retrofit.kotlinx.serialization)
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.androidx.paging.testing)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp.core)
    testImplementation(libs.okhttp.mockwebserver)
}
