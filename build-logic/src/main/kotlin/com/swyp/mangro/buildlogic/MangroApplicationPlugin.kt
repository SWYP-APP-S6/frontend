package com.swyp.mangro.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class MangroApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jlleitschuh.gradle.ktlint")

            extensions.configure<ApplicationExtension> {
                compileSdk {
                    version = release(Constants.COMPILE_SDK)
                }
                defaultConfig {
                    minSdk = Constants.MIN_SDK
                    targetSdk = Constants.TARGET_SDK

                    versionCode = Constants.VERSION_CODE
                    versionName = Constants.VERSION_NAME

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                compileOptions {
                    sourceCompatibility = Constants.JAVA_VERSION
                    targetCompatibility = Constants.JAVA_VERSION
                }

                flavorDimensions += "role"
                productFlavors {
                    create("owner") {
                        dimension = "role"
                        applicationIdSuffix = ".owner"
                    }
                    create("consumer") {
                        dimension = "role"
                        applicationIdSuffix = ".consumer"
                    }
                }
            }
        }
    }
}
