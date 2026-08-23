package com.swyp.mangro.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.swyp.mangro.buildlogic.utils.debugImplementation
import com.swyp.mangro.buildlogic.utils.implementation
import com.swyp.mangro.buildlogic.utils.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class MangroComposePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            pluginManager.withPlugin("com.android.application") {
                extensions.configure<ApplicationExtension> {
                    buildFeatures {
                        compose = true
                    }
                }
            }

            pluginManager.withPlugin("com.android.library") {
                extensions.configure<LibraryExtension> {
                    buildFeatures {
                        compose = true
                    }
                }
            }

            dependencies {
                val composeBom = platform(library("androidx.compose.bom"))

                implementation(composeBom)
                implementation(library("androidx.compose.foundation"))
                implementation(library("androidx.compose.material3"))
                implementation(library("androidx.compose.ui"))
                implementation(library("androidx.compose.ui.tooling.preview"))
                debugImplementation(library("androidx.compose.ui.tooling"))
            }
        }
    }
}
