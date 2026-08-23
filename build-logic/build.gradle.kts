plugins {
    `kotlin-dsl`
}

group = "com.swyp.mangro.buildlogic"

dependencies {
    compileOnly(libs.android.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "mangro.android.application"
            implementationClass = "com.swyp.mangro.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "mangro.android.library"
            implementationClass = "com.swyp.mangro.buildlogic.AndroidLibraryConventionPlugin"
        }
    }
}
