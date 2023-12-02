// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }

}

// Lists all plugins used throughout the project without applying them.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.gms) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.secrets) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
}

task("clean").dependsOn("module_expose_clean")
// This task is used for delete xx_expose module
tasks.register("module_expose_clean"){
    doLast {
        println("execute clean expose")
        subprojects.forEach{ project->
            // Please exclude these project that you don't want to delete
            if(project.name.endsWith("_expose")){
                println("ModuleExpose: delete ${project.path}")
                project.projectDir.deleteRecursively()
            }
        }
    }
}

