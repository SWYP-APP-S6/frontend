package com.swyp.mangro.buildlogic

import org.gradle.api.JavaVersion

internal object AndroidConfig {
    const val COMPILE_SDK = 36
    const val COMPILE_SDK_MINOR = 1
    const val MIN_SDK = 28
    const val TARGET_SDK = 36

    val javaVersion = JavaVersion.VERSION_11
}
