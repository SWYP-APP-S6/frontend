plugins {
    id("mangro.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android { namespace = "com.swyp.mangro.data.consumer.notification" }

dependencies {
    implementation(project(":remote:user"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.retrofit.core)
}
