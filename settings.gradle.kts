pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://jitpack.io") }
        google()
        mavenCentral()
    }
}

//apply(from = "$rootDir/supportApi.gradle.kts")

val includeWithApi: (projectPaths: String) -> Unit by extra
//enableFeaturePreview("VERSION_CATALOGS")
rootProject.name = "SmartAppSearch"
//include(":app")
includeWithApi(":app")
include(":lib")


fun includeWithApi(module: String) {
    include(module)
    val moduleProject = project(module)
    doCopyAll(moduleProject.projectDir.absolutePath)
    include("${module}_api")
}


/**
 * @param src0 : such as module1 abs path
 * */
fun doCopyAll(src0: String) {
    val t1 = System.currentTimeMillis()
    val src = "${src0}${File.separator}src${File.separator}main"
    val des = "${src0}_api${File.separator}src${File.separator}main"
    println("debug $src")
    println("debug $des")
    delete(des)
    copy {
        from(src)
        into(des)
        exclude("assets")
        exclude("res")
        exclude("jniLibs")
        exclude("AndroidManifest.xml")
        include("**/*.api.kt")
    }
    // remove empty dirs
    deleteEmptyDir(des)
    println("Module $src do copy spend ${(System.currentTimeMillis() - t1)} ms")
}


fun deleteEmptyDir(path: String) {
    deleteEmptyDir(File(path))
}

// A包含A1 A2 A3 AN AN全为空 A不会被删除
fun deleteEmptyDir(file: File) {
    if (file.isDirectory) {
        file.listFiles().forEach {
            if (it.isDirectory) {
                if (it.listFiles().isEmpty()) {
                    it.delete() // 删除后 file对象依然存在
                    if (it.parentFile?.listFiles()?.isEmpty() == true) {
                        it.parentFile.delete() // 使用file对象尝试删除父目录
                    }
                } else {
                    deleteEmptyDir(it)
                }
            }
        }
    }
}

