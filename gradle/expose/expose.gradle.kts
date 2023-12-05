@file:Suppress("PrivatePropertyName")

import java.io.File
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.absolutePathString


/* [Tutorial documentation]
Project: https://github.com/JailedBird/ModuleExpose
1) import this gradle script in setting.gradle.kts, like this:
apply(from = "$rootDir/gradle/expose/expose.gradle.kts")
2) import function includeWithExpose & includeWithJavaExpose, like this:
val includeWithExpose: (projectPaths: String) -> Unit by extra
val includeWithJavaExpose: (projectPaths: String) -> Unit by extra
3) use includeWithExpose or includeWithJavaExpose include your project, like this:
includeWithExpose(":feature:settings")
includeWithExpose(":feature:search")
4) in your module, create expose directory and place your file that need to be exposed

Note: ensure your project enable kts! TODO: support traditional gradle project
*/
extra["includeWithExpose"] = { module: String ->
    includeWithExpose(module, isJava = false, DEFAULT_EXPOSE_DIR_NAME, DEFAULT_CONDITION)
}

extra["includeWithJavaExpose"] = { module: String ->
    includeWithExpose(module, isJava = true, DEFAULT_EXPOSE_DIR_NAME, DEFAULT_CONDITION)
}

private val MODULE_EXPOSE_TAG = "expose"
private val DEFAULT_EXPOSE_DIR_NAME = "expose"
private val SCRIPT_DIR = "$rootDir/gradle/expose/"
private val BUILD_TEMPLATE_PATH_JAVA = "${SCRIPT_DIR}build_gradle_template_java"
private val BUILD_TEMPLATE_PATH_ANDROID = "${SCRIPT_DIR}build_gradle_template_android"
private val BUILD_TEMPLATE_PATH_CUSTOM = "build_gradle_template_expose"
private val ENABLE_FILE_CONDITION = false
private val MODULE_NAMESPACE_TEMPLATE = "cn.jailedbird.module.%s_expose"
private val DEBUG_ENABLE = false


private val DEFAULT_CONDITION: (String) -> Boolean = if (ENABLE_FILE_CONDITION) {
    ::ownCondition
} else {
    ::noFilter
}

fun includeWithExpose(
    module: String,
    isJava: Boolean,
    expose: String,
    condition: (String) -> Boolean
) {
    include(module)
    debug("[Module $module]")
    val namespace = module.replace(":", ".").trim('.')
    measure("ModuleExpose $module", true) {
        val moduleProject = project(module)
        val src = moduleProject.projectDir.absolutePath
        val des = "${src}_${MODULE_EXPOSE_TAG}"
        // generate build.gradle.kts
        measure("[generateBuildGradle]") {
            generateBuildGradle(
                src,
                BUILD_TEMPLATE_PATH_CUSTOM,
                des,
                "build.gradle.kts",
                namespace,
                isJava
            )
        }

        doSync(src, expose, condition)
        // Add module_expose to Project!
        include("${module}_${MODULE_EXPOSE_TAG}")
    }
}

fun doSync(src0: String, expose: String, condition: (String) -> Boolean) {
    val src = "${src0}${File.separator}src${File.separator}main"
    val des = "${src0}_${MODULE_EXPOSE_TAG}${File.separator}src${File.separator}main"
    // Do not delete
    val root = File(src)
    val pathList = mutableListOf<String>()
    if (root.exists() && root.isDirectory) {
        measure("[findDirectoryByNio]") {
            findDirectoryByNIO(src, expose, pathList)
            if (DEBUG_ENABLE) {
                pathList.forEach { exposeDir ->
                    println("[findDirectoryByNio] find $exposeDir")
                }
            }
        }
    }

    pathList.forEach { copyFrom ->
        val suffix = copyFrom.removePrefix(src)
        val copyTo = des + suffix
        /*syncDirectory(copyFrom, copyTo) { fileName ->
            fileName.endsWith(".api.kt") // Note: you can define your own filter statement
        }*/
        syncDirectory(copyFrom, copyTo, condition)
    }
}

inline fun measure(tag: String = "Measure", force: Boolean = false, block: () -> Unit) {
    val t1 = System.currentTimeMillis()
    block.invoke()
    debug("$tag spend ${System.currentTimeMillis() - t1}ms", force)
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

fun deleteExtraFiles(
    destinationDirectory: Path,
    sourceDirectory: Path,
    condition: ((fileName: String) -> Boolean) = ::noFilter
) {
    // delete file not in
    Files.walk(destinationDirectory)
        .filter { path -> path != destinationDirectory }
        .filter { path ->
            !condition.invoke(path.fileName.toString())
                    || !Files.exists(sourceDirectory.resolve(destinationDirectory.relativize(path)))
        }
        .forEach { path ->
            try {
                if (!Files.isDirectory(path)) { // Skip dir, avoid directory not empty exception
                    debug("[deleteExtraFiles] deleted file: ${destinationDirectory.relativize(path)}")
                    Files.delete(path)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
}

fun noFilter(@Suppress("UNUSED_PARAMETER") fileName: String) = true

fun ownCondition(@Suppress("UNUSED_PARAMETER") fileName: String): Boolean {
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

    if (!Files.exists(sourceDirectory) || !Files.isDirectory(sourceDirectory)) {
        debug("Source directory does not exist or is not a directory.")
        return
    }

    val destinationDirectory: Path = Paths.get(des)

    if (!Files.exists(destinationDirectory)) {
        try {
            Files.createDirectories(destinationDirectory)
            debug("Created destination directory: $destinationDirectory")
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
    } else { // delete file in destinationDirectory that not exists in sourceDirectory
        measure("[deleteExtraFiles]") {
            deleteExtraFiles(destinationDirectory, sourceDirectory, condition)
        }
    }

    val start = System.currentTimeMillis()
    try {
        Files.walkFileTree(sourceDirectory, object : SimpleFileVisitor<Path>() {
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
                if (areFilesContentEqual(file, destinationFile, "[directorySync]")) {
                    // Do nothing
                } else {
                    measure("[directorySync] ${file.fileName} sync with copy REPLACE_EXISTING") {
                        Files.copy(
                            file,
                            destinationFile,
                            StandardCopyOption.REPLACE_EXISTING
                        )
                    }

                }
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
                    } catch (e: IOException) {
                        e.printStackTrace()
                        return FileVisitResult.TERMINATE
                    }
                }

                return FileVisitResult.CONTINUE
            }
        })

        debug("[directorySync] all copy and sync spend ${System.currentTimeMillis() - start}")
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun areFilesContentEqual(path1: Path, path2: Path, tag: String = ""): Boolean {
    try {
        if (!Files.exists(path1) || !Files.exists(path2)) {
            return false
        }
        val size1 = Files.size(path1)
        val size2 = Files.size(path2)
        if (size1 != size2) {
            return false // Different sizes, files can't be equal
        }
        if (size1 > 4_000_000) { // 4MB return false
            return false
        }
        val start = System.currentTimeMillis()
        val content1 = Files.readAllBytes(path1) // Huge file will cause performance problem
        val content2 = Files.readAllBytes(path2)
        val isSame = content1.contentEquals(content2)
        debug("$tag Read ${path1.fileName}*2 & FilesContentEqual spend ${System.currentTimeMillis() - start} ms, isSame $isSame")
        return isSame
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

fun areFilesContentEqual(path: Path, content2: ByteArray): Boolean {
    try {
        if (!Files.exists(path)) {
            return false
        }
        val size1 = Files.size(path)
        val size2 = content2.size.toLong()
        if (size1 != size2) {
            return false // Different sizes, files can't be equal
        }
        if (size1 > 4_000_000) { // 4MB return false
            return false
        }
        val content1 = Files.readAllBytes(path) // Huge file will cause performance problem
        return java.util.Arrays.equals(content1, content2)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

/**
 * Create build.gradle.kts
 * @param isJava Android module or Java module
 * */
fun generateBuildGradle(
    srcScriptDir: String,
    srcScriptName: String,
    desScriptDir: String,
    desScriptName: String,
    selfName: String,
    isJava: Boolean = false
) {
    // Ensure _expose directory is created!
    File(desScriptDir).let {
        if (!it.exists()) {
            it.mkdir()
        }
    }
    val ownTemplate = File(srcScriptDir, srcScriptName)
    if (ownTemplate.exists()) {
        val sourcePath: Path = Paths.get("${srcScriptDir}${File.separator}${srcScriptName}")
        val destinationPath: Path = Paths.get("${desScriptDir}${File.separator}${desScriptName}")
        if (areFilesContentEqual(sourcePath, destinationPath)) {
            debug("[generateBuildGradle] from [$srcScriptName] to build.gradle.kts has no change!")
        } else {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING)
            debug("[generateBuildGradle] from [$srcScriptName] to build.gradle.kts has change with Files.copy")
        }
    } else {
        val scriptPath = "${desScriptDir}${File.separator}${desScriptName}"
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
        if (buildScript.exists() && areFilesContentEqual(
                Path.of(scriptPath),
                copyText.toByteArray(Charsets.UTF_8)
            )
        ) {
            debug("[generateBuildGradle] from [${templateFile.name}] to build.gradle.kts has no change!")
        } else {
            buildScript.writeText(copyText)
            debug("[generateBuildGradle] from [${templateFile.name}] to build.gradle.kts has change with writeText")
        }
    }
}

fun debug(message: String, force: Boolean = false) {
    if (force or DEBUG_ENABLE) {
        println(message)
    }
}
