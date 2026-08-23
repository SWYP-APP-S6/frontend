package com.swyp.mangro.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")

        extensions.configure<ApplicationExtension> {
            compileSdk {
                version = release(AndroidConfig.COMPILE_SDK) {
                    minorApiLevel = AndroidConfig.COMPILE_SDK_MINOR
                }
            }

            defaultConfig {
                minSdk = AndroidConfig.MIN_SDK
                targetSdk = AndroidConfig.TARGET_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            compileOptions {
                sourceCompatibility = AndroidConfig.javaVersion
                targetCompatibility = AndroidConfig.javaVersion
            }
        }
    }
}
