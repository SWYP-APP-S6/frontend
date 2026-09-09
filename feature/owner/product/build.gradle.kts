plugins {
    id("mangro.android.library")
    id("mangro.android.compose")
}

android {
    namespace = "com.swyp.mangro.feature.owner.product"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
}
