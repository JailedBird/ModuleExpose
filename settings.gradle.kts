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
val includeWithExpose: (projectPaths: String) -> Unit by extra
val includeWithJavaExpose: (projectPaths: String) -> Unit by extra

rootProject.name = "ModuleExpose"
include(":app")
include(":core:settings")
include(":core:resource")
include(":core:common")
includeWithExpose(":feature:settings")
includeWithExpose(":feature:search")
includeWithExpose(":feature:about")
includeWithExpose(":feature:benchmark")