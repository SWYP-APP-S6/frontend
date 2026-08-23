package com.swyp.mangro.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class MangroLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jlleitschuh.gradle.ktlint")

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
}
