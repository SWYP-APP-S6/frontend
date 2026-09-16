plugins {
    id("mangro.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.swyp.mangro.data.auth"
    buildFeatures { buildConfig = true }
    flavorDimensions += "role"
    productFlavors {
        create("owner") {
            dimension = "role"
            buildConfigField("boolean", "IS_OWNER", "true")
        }
        create("consumer") {
            dimension = "role"
            buildConfigField("boolean", "IS_OWNER", "false")
        }
    }
}

dependencies {
    implementation(project(":core:network"))
    implementation(project(":remote:auth"))
    implementation(project(":core:local"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)
    testImplementation(libs.retrofit.kotlinx.serialization)
    testImplementation(platform(libs.okhttp.bom))
    testImplementation(libs.okhttp.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}
