plugins {
    alias(libs.plugins.openapi.generator) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint)
}

subprojects {
    val subproject = this

    pluginManager.withPlugin("org.jlleitschuh.gradle.ktlint") {
        rootProject.tasks.named("ktlintCheck") {
            dependsOn(subproject.tasks.named("ktlintCheck"))
        }
        rootProject.tasks.named("ktlintFormat") {
            dependsOn(subproject.tasks.named("ktlintFormat"))
        }
    }
}

val prepareOpenApiSpecs by tasks.registering(Exec::class) {
    inputs.dir(layout.projectDirectory.dir("openapi"))
    inputs.files(fileTree("scripts/openapi") { include("*.py") })
    outputs.dir(layout.buildDirectory.dir("openapi"))
    commandLine("python3", layout.projectDirectory.file("scripts/openapi/prepare_specs.py").asFile.absolutePath)
}
