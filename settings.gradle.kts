@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        // maven { url = uri("https://maven.aliyun.com/repository/public") }
        // maven { url = uri("https://maven.aliyun.com/repository/google") }
        // maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // maven { url = uri("https://maven.aliyun.com/repository/public") }
        // maven { url = uri("https://maven.aliyun.com/repository/google") }
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
// TYPESAFE_PROJECT_ACCESSORS, we can implement project likes:
// implementation(projects.core.resource)
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

apply(from = "$rootDir/gradle/expose/expose.gradle.kts")
val includeWithApi: (projectPaths: String) -> Unit by extra
val includeWithJavaApi: (projectPaths: String) -> Unit by extra

rootProject.name = "ModuleExpose"
include(":app")
include(":core:settings")
include(":core:resource")
includeWithJavaApi(":core:common")
includeWithApi(":feature:settings")
includeWithApi(":feature:search")
includeWithApi(":feature:about")
includeWithApi(":feature:benchmark")