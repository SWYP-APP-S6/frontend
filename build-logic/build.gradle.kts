plugins {
    `kotlin-dsl`
}

group = "com.swyp.mangro.buildlogic"

dependencies {
    compileOnly(libs.android.gradle.plugin)
    implementation(libs.compose.compiler.gradle.plugin)
    implementation(libs.ktlint.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "mangro.android.application"
            implementationClass = "com.swyp.mangro.buildlogic.MangroApplicationPlugin"
        }
        register("androidLibrary") {
            id = "mangro.android.library"
            implementationClass = "com.swyp.mangro.buildlogic.MangroLibraryPlugin"
        }
        register("androidCompose") {
            id = "mangro.android.compose"
            implementationClass = "com.swyp.mangro.buildlogic.MangroComposePlugin"
        }
    }
}
