package com.swyp.mangro.buildlogic

import org.gradle.api.JavaVersion

internal object Constants {
    const val MIN_SDK = 28
    const val TARGET_SDK = 36
    const val COMPILE_SDK = 36

    private const val MAJOR_VERSION = 1
    private const val MINOR_VERSION = 0
    private const val PATCH_VERSION = 0
    private const val BUILD_VERSION = 0

    const val VERSION_CODE = MAJOR_VERSION * 1000000 + MINOR_VERSION * 10000 + PATCH_VERSION * 100 + BUILD_VERSION
    const val VERSION_NAME = "${MAJOR_VERSION}.${MINOR_VERSION}.${PATCH_VERSION}"

    val JAVA_VERSION = JavaVersion.VERSION_17
}
