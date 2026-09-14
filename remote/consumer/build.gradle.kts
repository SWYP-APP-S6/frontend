import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.openapitools.generator.gradle.plugin.tasks.ValidateTask

plugins {
    id("mangro.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.openapi.generator)
}

val generatedDirectory = layout.buildDirectory.dir("generated/openapi")
val specification = rootProject.layout.buildDirectory.file("openapi/consumer.json")

extensions.configure<LibraryExtension> {
    namespace = "com.swyp.mangro.remote.consumer"
}

ktlint {
    filter {
        exclude { it.file.path.contains("/build/generated/") }
    }
}

tasks.named<ValidateTask>("openApiValidate") {
    dependsOn(rootProject.tasks.named("prepareOpenApiSpecs"))
    inputSpec.set(specification)
}

tasks.named<GenerateTask>("openApiGenerate") {
    dependsOn(tasks.named("openApiValidate"))
    generatorName.set("kotlin")
    library.set("jvm-retrofit2")
    inputSpec.set(specification)
    outputDir.set(generatedDirectory)
    cleanupOutput.set(true)
    templateDir.set(rootProject.layout.projectDirectory.dir("openapi/templates/kotlin").asFile.absolutePath)
    packageName.set("com.swyp.mangro.remote.consumer")
    apiPackage.set("com.swyp.mangro.remote.consumer.service")
    modelPackage.set("com.swyp.mangro.remote.consumer.model")
    apiNameSuffix.set("Service")
    configOptions.set(
        mapOf(
            "serializationLibrary" to "kotlinx_serialization",
            "useCoroutines" to "true",
            "useResponseAsReturnType" to "true",
            "dateLibrary" to "string",
            "enumPropertyNaming" to "UPPERCASE",
            "modelMutable" to "false",
        ),
    )
    globalProperties.set(mapOf("apis" to "", "models" to "", "supportingFiles" to "CollectionFormats.kt", "apiTests" to "false", "modelTests" to "false", "apiDocs" to "false", "modelDocs" to "false"))
}

extensions.configure<LibraryAndroidComponentsExtension> {
    onVariants(selector().all()) { variant ->
        variant.sources.kotlin?.addGeneratedSourceDirectory(tasks.named<GenerateTask>("openApiGenerate")) { it.outputDir }
    }
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    api(project(":core:network"))
    api(libs.retrofit.core)
    api(libs.kotlinx.serialization.json)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp.core)
    testImplementation(project(":remote:auth"))
    testImplementation(project(":remote:owner"))
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
}

// Live traffic is opt-in; normal builds never contact production.
tasks.withType<Test>().configureEach {
    systemProperty("mangro.liveApi", providers.gradleProperty("mangroLiveApi").getOrElse("false"))
}
