import com.swyp.mangro.buildlogic.conf.configureBuildConfig

plugins {
    id("mangro.android.application")
    id("mangro.android.compose")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
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
    add("ownerImplementation", project(":feature:owner:onboarding"))

    implementation(project(":remote:auth"))
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(project(":core:local"))
    add("consumerImplementation", project(":remote:consumer"))
    add("ownerImplementation", project(":remote:owner"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:utils"))
    add("ownerImplementation", project(":feature:owner:product"))
    add("ownerImplementation", project(":feature:owner:home"))
    add("ownerImplementation", project(":feature:owner:setting"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    add("consumerImplementation", libs.naver.maps)
    add("consumerImplementation", libs.naver.maps.compose)

    add("consumerImplementation", project(":feature:splash"))
    add("consumerImplementation", project(":feature:auth"))
    add("consumerImplementation", project(":feature:consumer:store"))
    add("consumerImplementation", project(":feature:consumer:home"))
    add("consumerImplementation", project(":feature:consumer:hold"))

    add("ownerImplementation", project(":feature:splash"))
    add("ownerImplementation", project(":feature:auth"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.material)

    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.kotlinx.collections.immutable)
}
