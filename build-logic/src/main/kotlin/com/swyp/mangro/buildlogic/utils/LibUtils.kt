package com.swyp.mangro.buildlogic.utils

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType


/**
 * Version Catalog에 추가된 라이브러리 의존성 정보를 가져오는 프로퍼티
 *
 * 사용 예시
 * `libs.findLibrary("androidx.activity.compose").get()`
 * 위 코드를 이용하여 activity compose 라이브러리와 관련된 정보를 가져올 수 있다.
 */
internal val Project.libs
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")


internal fun Project.library(alias: String): Provider<MinimalExternalModuleDependency> {
    return libs.findLibrary(alias).get()
}

internal fun Project.bundle(alias: String): Provider<ExternalModuleDependencyBundle> {
    return libs.findBundle(alias).get()
}

internal fun Project.version(alias: String): String {
    return libs.findVersion(alias).get().requiredVersion
}


internal fun DependencyHandler.implementation(dependencyNotation: Any) {
    add("implementation", dependencyNotation)
}

internal fun DependencyHandler.implementation(
    dependencyNotation: Any,
    configure: ExternalModuleDependency.() -> Unit,
) {
    val dependency = add("implementation", dependencyNotation)

    (dependency as? ExternalModuleDependency)?.apply(configure)
}

internal fun DependencyHandler.debugImplementation(dependencyNotation: Any) {
    add("debugImplementation", dependencyNotation)
}

internal fun DependencyHandler.releaseImplementation(dependencyNotation: Any) {
    add("releaseImplementation", dependencyNotation)
}

internal fun DependencyHandler.testImplementation(dependencyNotation: Any) {
    add("testImplementation", dependencyNotation)
}

internal fun DependencyHandler.androidTestImplementation(dependencyNotation: Any) {
    add("androidTestImplementation", dependencyNotation)
}

internal fun DependencyHandler.ksp(dependencyNotation: Any) {
    add("ksp", dependencyNotation)
}
