plugins {
    id("mangro.android.library")
    id("mangro.android.compose")
}

android {
    namespace = "com.swyp.mangro.feature.auth"
}

dependencies {
    implementation(project(":core:designsystem"))

    testImplementation(libs.junit)
}
