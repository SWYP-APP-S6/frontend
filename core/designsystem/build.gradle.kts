plugins {
    id("mangro.android.library")
    id("mangro.android.compose")
}

android {
    namespace = "com.swyp.mangro.core.designsystem"
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.balloon.compose)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    testImplementation(libs.junit)
}
