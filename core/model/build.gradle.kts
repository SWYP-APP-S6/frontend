plugins {
    id("mangro.android.library")
}

android {
    namespace = "com.swyp.mangro.core.model"
}

dependencies {
    implementation(libs.kotlinx.collections.immutable)
}
