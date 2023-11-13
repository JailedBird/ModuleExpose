package com.yeahka.android.sgpos

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

// 可以获取VersionCatalog 对应Extension, 避免gradle.properties获取
val Project.libs: VersionCatalog
    get() = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

val Project.minSdk: Int
    get() = libs.findVersion("minSdk").get().toString().toInt()

val Project.targetSdk: Int
    get() = libs.findVersion("targetSdk").get().toString().toInt()

val Project.compileSdk: Int
    get() = libs.findVersion("compileSdk").get().toString().toInt()

val Project.groupId: String
    get() = libs.findVersion("applicationId").get().toString()


val Project.versionName: String
    get() = libs.findVersion("versionName").get().toString()

val Project.versionCode: Int
    get() {
        val code = versionName.filter { it.isDigit() }
        if (code.length != 6) {
            error("Please input valid VERSION_NAME, such as 1.0.0(001), 6 numbers")
        }
        return code.toInt()
    }