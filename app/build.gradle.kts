import com.swyp.mangro.buildlogic.conf.configureBuildConfig

plugins {
    id("mangro.android.application")
    id("mangro.android.compose")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.swyp.mangro"

    configureBuildConfig(this)

    defaultConfig {
        applicationId = "com.swyp.mangro"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:utils"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    add("consumerImplementation", libs.naver.maps)
    add("consumerImplementation", libs.naver.maps.compose)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.kotlinx.collections.immutable)
}
