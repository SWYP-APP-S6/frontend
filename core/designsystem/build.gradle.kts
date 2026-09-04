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
}
