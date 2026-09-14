import java.util.Properties

plugins {
    id("mangro.android.library")
    id("mangro.android.compose")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    buildFeatures { resValues = true }
    namespace = "com.swyp.mangro.feature.auth"

    flavorDimensions += "role"
    productFlavors {
        create("owner") {
            dimension = "role"
            val properties = Properties().apply {
                rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
            }
            val kakaoKey = providers.environmentVariable("MANGRO_OWNER_KAKAO_NATIVE_APP_KEY").orNull
                ?: properties.getProperty("MANGRO_OWNER_KAKAO_NATIVE_APP_KEY").orEmpty()
            require(kakaoKey.isEmpty() || kakaoKey.matches(Regex("[a-zA-Z0-9]+"))) { "Invalid Owner Kakao native key format" }
            resValue("string", "owner_kakao_native_key", kakaoKey)
            manifestPlaceholders["ownerKakaoScheme"] = "kakao$kakaoKey"
        }
        create("consumer") { dimension = "role" }
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:utils"))
    add("ownerImplementation", project(":data:owner:terms"))
    add("ownerImplementation", project(":data:owner:auth"))
    add("ownerImplementation", libs.kakao.user)
    add("ownerImplementation", libs.commonmark.core)
    add("ownerImplementation", libs.commonmark.tables)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
