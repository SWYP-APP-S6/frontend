import com.swyp.mangro.buildlogic.conf.configureBuildConfig
import java.util.Properties

plugins {
    id("mangro.android.application")
    id("mangro.android.compose")
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

// Firebase configuration is supplied privately for the Owner application.
if (file("src/owner/google-services.json").isFile) {
    apply(plugin = "com.google.gms.google-services")
    tasks.configureEach {
        if (name.startsWith("processConsumer") && name.endsWith("GoogleServices")) enabled = false
    }
}

android {
    namespace = "com.swyp.mangro"

    configureBuildConfig(this)
    buildFeatures { buildConfig = true }

    val localProperties = Properties().apply {
        rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
    }
    productFlavors.configureEach {
        val propertyName = "MANGRO_${name.uppercase()}_KAKAO_NATIVE_APP_KEY"
        val nativeAppKey = providers.environmentVariable(propertyName)
            .orElse(localProperties.getProperty(propertyName).orEmpty())
            .get().trim()
        require(nativeAppKey.matches(Regex("[a-zA-Z0-9]*"))) {
            "$propertyName must contain only letters and digits."
        }
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$nativeAppKey\"")
        manifestPlaceholders["kakaoRedirectScheme"] = "kakao${nativeAppKey.ifBlank { "unconfigured.$name" }}"
    }

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
    add("ownerImplementation", platform(libs.firebase.bom))
    add("ownerImplementation", libs.firebase.messaging)
    add("ownerImplementation", libs.androidx.work.runtime)
    add("ownerImplementation", libs.kotlinx.coroutines.play.services)
    add("ownerImplementation", project(":data:owner:notification"))
    implementation(libs.kakao.user)
    add("ownerImplementation", project(":feature:owner:onboarding"))
    add("ownerImplementation", project(":data:auth"))

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
    add("consumerImplementation", project(":feature:consumer:recipe"))
    add("consumerImplementation", project(":feature:consumer:myinfo"))

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
    androidTestImplementation(project(":data:auth"))
    androidTestImplementation(project(":data:owner:product"))
    androidTestImplementation(libs.androidx.paging.compose)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.kotlinx.collections.immutable)
}
