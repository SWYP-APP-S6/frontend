plugins {
    id("mangro.android.library")
}

android {
    namespace = "com.swyp.mangro.core.utils"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
