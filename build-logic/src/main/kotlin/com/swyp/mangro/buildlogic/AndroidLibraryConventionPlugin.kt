package com.swyp.mangro.buildlogic

import com.android.build.api.dsl.LibraryExtension
import com.swyp.mangro.buildlogic.conf.Constants
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            compileSdk {
                version = release(Constants.COMPILE_SDK)
            }

            defaultConfig {
                minSdk = Constants.MIN_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            compileOptions {
                sourceCompatibility = Constants.JAVA_VERSION
                targetCompatibility = Constants.JAVA_VERSION
            }
        }
    }
}
