plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
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
