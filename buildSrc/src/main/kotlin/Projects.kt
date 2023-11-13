package com.yeahka.android.sgpos

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.Lint
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

fun Project.setupLibraryModule(
    namespaceSuffix: String,
    resourcePrefix: String? = null,
    block: LibraryExtension.() -> Unit = {},
) = setupBaseModule<LibraryExtension>(
    namespaceSuffix = namespaceSuffix,
    _resourcePrefix = resourcePrefix
) {
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    block()
}

fun LibraryExtension.enableViewBinding() {
    buildFeatures {
        viewBinding = true
    }
}

//fun LibraryExtension.enableDataBinding() {
//    buildFeatures {
//        dataBinding {
//            enable = true
//        }
//    }
//}


fun Project.setupAppModule(
    resourcePrefix: String? = null,
    block: BaseAppModuleExtension.() -> Unit = {}
) = setupBaseModule<BaseAppModuleExtension>(
    namespaceSuffix = null,
    _resourcePrefix = resourcePrefix
) {
    defaultConfig {
        applicationId = project.groupId
        versionCode = project.versionCode
        versionName = project.versionName
    }
    block()
}

private inline fun <reified T : BaseExtension> Project.setupBaseModule(
    namespaceSuffix: String? = null,
    _resourcePrefix: String? = null,
    crossinline block: T.() -> Unit = {},
) = extensions.configure<T>("android") {
    namespace = if (namespaceSuffix.isNullOrEmpty()) {
        project.groupId
    } else {
        project.groupId + "." + namespaceSuffix
    }
    compileSdkVersion(project.compileSdk)
    defaultConfig {
        minSdk = project.minSdk
        targetSdk = project.targetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (!_resourcePrefix.isNullOrEmpty()) {
            resourcePrefix = _resourcePrefix
        }
    }

//    tasks.withType(JavaCompile::class.java) {
//        kotlinOptions {
//            jvmTarget = JavaVersion.VERSION_17.toString()
//        }
//    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"

        // val arguments = mutableListOf(
        //     // https://kotlinlang.org/docs/compiler-reference.html#progressive
        //     "-progressive",
        //     // Enable Java default method generation.
        //     "-Xjvm-default=all",
        //     // Generate smaller bytecode by not generating runtime not-null assertions.
        //     "-Xno-call-assertions",
        //     "-Xno-param-assertions",
        //     "-Xno-receiver-assertions",
        // )
        // https://youtrack.jetbrains.com/issue/KT-41985
        // freeCompilerArgs += arguments
    }
    // packagingOptions {
    //     resources.pickFirsts += arrayOf(
    //         "META-INF/AL2.0",
    //         "META-INF/LGPL2.1",
    //         "META-INF/*kotlin_module",
    //     )
    // }
    // testOptions {
    //     unitTests.isIncludeAndroidResources = true
    // }
//    lint {
//        disable += arrayOf("MissingClass")
//    }
    block()
}

private fun BaseExtension.kotlinOptions(block: KotlinJvmOptions.() -> Unit) {
    (this as ExtensionAware).extensions.configure("kotlinOptions", block)
}

//private fun BaseExtension.lint(block: Lint.() -> Unit) {
//    (this as CommonExtension<*, *, *, *>).lint(block)
//}
