plugins {
    id("mangro.android.library")
    id("mangro.android.compose")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.swyp.mangro.feature.consumer.home"
    defaultConfig { missingDimensionStrategy("role", "consumer") }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:utils"))
    implementation(project(":data:auth"))
    implementation(project(":feature:consumer:store"))
    implementation(project(":data:consumer:home"))
    implementation(project(":feature:consumer:hold"))

    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.naver.maps)
    implementation(libs.naver.maps.compose)

    testImplementation(libs.junit)
}
