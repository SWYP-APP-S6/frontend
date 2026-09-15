plugins {
    id("mangro.android.library")
    id("mangro.android.compose")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.swyp.mangro.feature.consumer.home"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:utils"))

    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.naver.maps)
    implementation(libs.naver.maps.compose)

    testImplementation(libs.junit)
}
