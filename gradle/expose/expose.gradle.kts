@file:Suppress("PrivatePropertyName")

import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.absolutePathString
import java.io.File
/* [Tutorial documentation]
1) import this gradle script in setting.gradle.kts, like this:
apply(from = "$rootDir/gradle/expose/expose.gradle.kts")
2) import function includeWithApi & includeWithJavaApi, like this:
val includeWithApi: (projectPaths: String) -> Unit by extra
val includeWithJavaApi: (projectPaths: String) -> Unit by extra

Note: ensure your project enable kts! TODO: support traditional gradle project
*/
extra["includeWithApi"] = { module: String ->
    includeWithApi(module, isJava = false, DEFAULT_EXPOSE_DIR_NAME, DEFAULT_CONDITION)
}

extra["includeWithJavaApi"] = { module: String ->
    includeWithApi(module, isJava = true, DEFAULT_EXPOSE_DIR_NAME, DEFAULT_CONDITION)
}

private val MODULE_EXPOSE_TAG = "expose"
private val DEFAULT_EXPOSE_DIR_NAME = "expose"
private val SCRIPT_DIR = "$rootDir/gradle/expose/"
private val BUILD_TEMPLATE_PATH_JAVA = "${SCRIPT_DIR}build_gradle_template_java"
private val BUILD_TEMPLATE_PATH_ANDROID = "${SCRIPT_DIR}build_gradle_template_android"
private val ENABLE_FILE_CONDITION = false
private val MODULE_NAMESPACE_TEMPLATE = "cn.jailedbird.module.%s_expose"
private val DEBUG_ENABLE = false


private val DEFAULT_CONDITION: (String) -> Boolean = if (ENABLE_FILE_CONDITION) {
    ::ownCondition
} else {
    ::noFilter
}

fun includeWithApi(
    module: String,
    isJava: Boolean,
    expose: String,
    condition: (String) -> Boolean
) {
    include(module)
    measure("Expose ${module}", true) {
        val moduleProject = project(module)
        val src = moduleProject.projectDir.absolutePath
        val des = "${src}_${MODULE_EXPOSE_TAG}"
        // generate build.gradle.kts
        generateBuildGradle(des, "build.gradle.kts", moduleProject.name, isJava)
        doSync(src, expose, condition)
        // Add module_expose to Project!
        include("${module}_${MODULE_EXPOSE_TAG}")
    }
    println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
}

fun doSync(src0: String, expose: String, condition: (String) -> Boolean) {
    val start = System.currentTimeMillis()
    val src = "${src0}${File.separator}src${File.separator}main"
    val des = "${src0}_${MODULE_EXPOSE_TAG}${File.separator}src${File.separator}main"
    // Do not delete
    val root = File(src)
    val pathList = mutableListOf<String>()
    if (root.exists() && root.isDirectory) {
        measure("findDirectoryByNio") {
            findDirectoryByNIO(src, expose, pathList)
        }
    }

    pathList.forEach { copyFrom ->
        val suffix = copyFrom.removePrefix(src)
        val copyTo = des + suffix
        measure("syncDirectory $copyFrom") {
            /*syncDirectory(copyFrom, copyTo) { fileName ->
                fileName.endsWith(".api.kt") // Note: you can define your own filter statement
            }*/
            syncDirectory(copyFrom, copyTo, condition)
        }
        measure("Delete empty dir") {
            // remove empty dirs
            deleteEmptyDir(copyTo)
        }
    }
    debug("Module $src all spend ${(System.currentTimeMillis() - start)} ms")
}

fun measure(tag: String, force: Boolean = false, block: () -> Unit) {
    val t1 = System.currentTimeMillis()
    block.invoke()
    debug("Measure: $tag spend ${System.currentTimeMillis() - t1}ms", force)
}

// @Deprecated(message = "Deprecated because of low performance")
// fun findDirectoryByJavaIO(
//     root: File,
//     specPath: String,
//     pathList: MutableList<String>
// ): String? {
//     if (!root.exists() || !root.isDirectory) {
//         return null
//     }
//     if (root.name == specPath) {
//         return root.absolutePath
//     }
//
//     for (file in root.listFiles()) {
//         if (file.isDirectory) {
//             val find = findDirectoryByJavaIO(file, specPath, pathList)
//             if (find != null) {
//                 pathList.add(find)
//             }
//         }
//     }
//     return null
// }

/**
 * Better than findDirectoryByJavaIO with lowest time spend
 * */
fun findDirectoryByNIO(dir: String, specPath: String, pathList: MutableList<String>) {
    Files.walkFileTree(Paths.get(dir), object :
        SimpleFileVisitor<Path>() {
        override fun preVisitDirectory(
            dir: Path,
            attrs: BasicFileAttributes?
        ): FileVisitResult {
            return if (dir.fileName.toString() == specPath) {
                pathList.add(dir.toAbsolutePath().toString())
                FileVisitResult.SKIP_SUBTREE
            } else {
                FileVisitResult.CONTINUE
            }
        }
    })
}

/**
 * delete by nio, delete app module, 43->22ms
 * */
fun deleteDirectoryByNio(dir: String) {
    try {
        val path = FileSystems.getDefault().getPath(dir)
        if (!Files.exists(path)) { // empty dir to check
            debug("empty path ${path.toAbsolutePath()} to delete")
            return
        }
        debug("to delete path " + path.absolutePathString().toString())
        // Files.delete(path); // can not delete not empty dir
        Files.walkFileTree(
            path,
            object : SimpleFileVisitor<Path>() {
                override fun visitFile(
                    file: Path,
                    attrs: BasicFileAttributes?
                ): FileVisitResult {
                    // debug("${file.absolutePathString()} to delete")
                    Files.delete(file)
                    return super.visitFile(file, attrs)
                }

                override fun postVisitDirectory(
                    dir: Path?,
                    exc: java.io.IOException?
                ): FileVisitResult {
                    if (dir != null) {
                        Files.delete(dir)
                    }
                    return super.postVisitDirectory(dir, exc)
                }
            })
    } catch (e: Exception) {
        e.printStackTrace()
    }

}


fun deleteExtraFiles(
    destinationDirectory: Path,
    sourceDirectory: Path,
    condition: ((fileName: String) -> Boolean) = ::noFilter
) {
    // delete file not in
    Files.walk(destinationDirectory)
        .filter { path -> path != destinationDirectory }
        .filter { path ->
            !Files.exists(sourceDirectory.resolve(destinationDirectory.relativize(path)))
                    || !condition.invoke(path.fileName.toString())
        }
        .forEach { path ->
            try {
                if (!Files.isDirectory(path)) { // Skip dir, avoid directory not empty exception
                    Files.delete(path)
                    debug("Deleted extra file: ${destinationDirectory.relativize(path)}")
                }
            } catch (e: java.io.IOException) {
                e.printStackTrace()
            }
        }
}

fun noFilter(@Suppress("UNUSED_PARAMETER") fileName: String) = true
fun ownCondition(fileName: String): Boolean {
    /*TODO custom your own condition filter*/
    /*return fileName.endsWith(".api.kt")*/
    return true
}

/**
 *  @param condition, that need to be saved
 * */
fun syncDirectory(
    src: String,
    des: String,
    condition: ((fileName: String) -> Boolean) = ::noFilter
) {
    val sourceDirectory: Path = Paths.get(src)

    if (!Files.exists(sourceDirectory) || !Files.isDirectory(
            sourceDirectory
        )
    ) {
        debug("Source directory does not exist or is not a directory.")
        return
    }

    val destinationDirectory: Path = Paths.get(des)

    if (!Files.exists(destinationDirectory)) {
        try {
            Files.createDirectories(destinationDirectory)
            debug("Created destination directory: $destinationDirectory")
        } catch (e: java.io.IOException) {
            e.printStackTrace()
            return
        }
    } else { // delete file in destinationDirectory that not exists in sourceDirectory
        measure("deleteExtraFiles") {
            deleteExtraFiles(destinationDirectory, sourceDirectory, condition)
        }
    }

    try {
        Files.walkFileTree(
            sourceDirectory,
            object : SimpleFileVisitor<Path>() {
                override fun visitFile(
                    file: Path,
                    attrs: BasicFileAttributes
                ): FileVisitResult {
                    // Skip filter
                    if (!condition.invoke(file.fileName.toString())) {
                        return FileVisitResult.CONTINUE
                    }

                    val relativePath: Path = sourceDirectory.relativize(file)
                    val destinationFile: Path =
                        destinationDirectory.resolve(relativePath)

                    Files.copy(
                        file,
                        destinationFile,
                        StandardCopyOption.REPLACE_EXISTING
                    )
                    // debug("Copied file: $relativePath")
                    return FileVisitResult.CONTINUE
                }

                override fun preVisitDirectory(
                    dir: Path,
                    attrs: BasicFileAttributes
                ): FileVisitResult {
                    val relativePath: Path = sourceDirectory.relativize(dir)
                    val destinationDir: Path =
                        destinationDirectory.resolve(relativePath)

                    if (!Files.exists(destinationDir)) {
                        try {
                            Files.createDirectories(destinationDir)
                            // debug("Created directory: $relativePath")
                        } catch (e: java.io.IOException) {
                            e.printStackTrace()
                            return FileVisitResult.TERMINATE
                        }
                    }

                    return FileVisitResult.CONTINUE
                }
            })

        debug("Directory copy completed!")
    } catch (e: java.io.IOException) {
        e.printStackTrace()
    }
}

/**
 * Create build.gradle.kts
 * @param isJava Android module or Java module
 * */
fun generateBuildGradle(
    scriptDir: String,
    scriptName: String,
    selfName: String,
    isJava: Boolean = false
) {
    // Ensure _expose directory is created!
    File(scriptDir).let {
        if (!it.exists()) {
            it.mkdir()
        }
    }
    val scriptPath = "${scriptDir}${File.separator}${scriptName}"
    val buildScript = File(scriptPath)
    val path = if (isJava) {
        BUILD_TEMPLATE_PATH_JAVA
    } else {
        BUILD_TEMPLATE_PATH_ANDROID
    }
    val templateFile = File(path)

    if (!templateFile.exists()) {
        throw Exception("Template file ${templateFile.absolutePath} not found!")
    }
    val readText = templateFile.readText()
    val copyText = String.format(readText, String.format(MODULE_NAMESPACE_TEMPLATE, selfName))
    buildScript.writeText(copyText)
}


fun deleteEmptyDir(path: String) {
    deleteEmptyDir(File(path))
}

// A包含A1 A2 A3 AN AN全为空 A不会被删除
fun deleteEmptyDir(file: File) {
    if (file.isDirectory) {
        file.listFiles()?.forEach {
            if (it.isDirectory) {
                if (it.listFiles()?.isEmpty() == true) {
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

/**
 * @param src0 : such as module1 abs path
 * */
@Deprecated("Deprecated because of low performance")
fun doSyncByGradleApi(src0: String) {
    val t1 = System.currentTimeMillis()
    val src = "${src0}${File.separator}src${File.separator}main"
    val des = "${src0}_${MODULE_EXPOSE_TAG}${File.separator}src${File.separator}main"
    debug("debug $src")
    debug("debug $des")
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
    debug("Module $src do copy spend ${(System.currentTimeMillis() - t1)} ms")
}

fun debug(message: String, force: Boolean = false) {
    if (force or DEBUG_ENABLE) {
        println(message)
    }
}