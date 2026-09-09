package com.swyp.mangro.buildlogic.conf

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke
import java.util.Properties

fun Project.configureBuildConfig(commonExtension: CommonExtension) {
    commonExtension.apply {
        buildTypes {
            val localProperties = Properties().apply {
                rootProject.file("local.properties")
                    .takeIf { file -> file.exists() }
                    ?.inputStream()
                    ?.use(::load)
            }

            commonExtension.buildTypes {
                getByName("debug") {
                    manifestPlaceholders["NCP_MAP_CLIENT_ID"] = localProperties.getProperty("MANGRO_NAVER_MAP_NCP_CLIENT_ID").orEmpty()
                }

                getByName("release") {
                    manifestPlaceholders["NCP_MAP_CLIENT_ID"] = localProperties.getProperty("MANGRO_NAVER_MAP_NCP_CLIENT_ID").orEmpty()
                }
            }
        }
    }
}
