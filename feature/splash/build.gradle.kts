plugins {
    id("mangro.android.library")
    id("mangro.android.compose")
}

android {
    namespace = "com.swyp.mangro.feature.splash"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.core)

    testImplementation(libs.junit)
}
