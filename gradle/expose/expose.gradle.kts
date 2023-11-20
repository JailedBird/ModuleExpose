import kotlin.io.path.absolutePathString

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

private val DEFAULT_EXPOSE_DIR_NAME = "test"
private val SCRIPE_DIR = "$rootDir/gradle/expose/"
private val BUILD_TEMPLATE_PATH_JAVA = "${SCRIPE_DIR}build_gradle_template_java"
private val BUILD_TEMPLATE_PATH_ANDROID = "${SCRIPE_DIR}build_gradle_template_android"
private val ENABLE_FILE_CONDITION = false

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
    val moduleProject = project(module)
    val src = moduleProject.projectDir.absolutePath
    val des = "${src}_api"
    doSync(src, expose, condition)
    createBuildGradle(
        "${des}${java.io.File.separator}build.gradle.kts",
        moduleProject.name, isJava
    )
    include("${module}_api")
}

fun doSync(src0: String, expose: String, condition: (String) -> Boolean) {
    val t1 = System.currentTimeMillis()
    val src = "${src0}${java.io.File.separator}src${java.io.File.separator}main"
    val des = "${src0}_api${java.io.File.separator}src${java.io.File.separator}main"
    // Do not delete
    val root = java.io.File(src)
    val pathList = mutableListOf<String>()
    if (root.exists() && root.isDirectory) {
        measure("findDirecoryByNio") {
            findDirecoryByNIO(src, expose, pathList)
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
    println("Module $src all spend ${(System.currentTimeMillis() - t1)} ms\n\n")
}

fun measure(tag: String, block: () -> Unit) {
    val t1 = System.currentTimeMillis()
    block.invoke()
    println("Measure: $tag spend ${System.currentTimeMillis() - t1}ms")
}

@Deprecated(message = "Deprecated because of low performance")
fun findDirectoryByJavaIO(
    root: java.io.File,
    specPath: String,
    pathList: MutableList<String>
): String? {
    if (!root.exists() || !root.isDirectory) {
        return null
    }
    if (root.name == specPath) {
        return root.absolutePath
    }

    for (file in root.listFiles()) {
        if (file.isDirectory) {
            val find = findDirectoryByJavaIO(file, specPath, pathList)
            if (find != null) {
                pathList.add(find)
            }
        }
    }
    return null
}

/**
 * Better than [findDirectoryByJavaIO] with lowest time spend
 * */
fun findDirecoryByNIO(dir: String, specPath: String, pathList: MutableList<String>) {
    java.nio.file.Files.walkFileTree(java.nio.file.Paths.get(dir), object :
        java.nio.file.SimpleFileVisitor<java.nio.file.Path>() {
        override fun preVisitDirectory(
            dir: java.nio.file.Path,
            attrs: java.nio.file.attribute.BasicFileAttributes?
        ): java.nio.file.FileVisitResult {
            return if (dir.fileName.toString() == specPath) {
                pathList.add(dir.toAbsolutePath().toString())
                java.nio.file.FileVisitResult.SKIP_SUBTREE
            } else {
                java.nio.file.FileVisitResult.CONTINUE
            }
        }
    })
}

/**
 * delete by nio, delete app module, 43->22ms
 * */
fun deleteDirectoryByNio(dir: String) {
    try {
        val path = java.nio.file.FileSystems.getDefault().getPath(dir)
        if (!java.nio.file.Files.exists(path)) { // empty dir to check
            println("empty path ${path.toAbsolutePath()} to delete")
            return
        }
        println("to delete path " + path.absolutePathString().toString())
        // java.nio.file.Files.delete(path); // can not delete not empty dir
        java.nio.file.Files.walkFileTree(
            path,
            object : java.nio.file.SimpleFileVisitor<java.nio.file.Path>() {
                override fun visitFile(
                    file: java.nio.file.Path,
                    attrs: java.nio.file.attribute.BasicFileAttributes?
                ): java.nio.file.FileVisitResult {
                    // println("${file.absolutePathString()} to delete")
                    java.nio.file.Files.delete(file)
                    return super.visitFile(file, attrs)
                }

                override fun postVisitDirectory(
                    dir: java.nio.file.Path?,
                    exc: java.io.IOException?
                ): java.nio.file.FileVisitResult {
                    if (dir != null) {
                        java.nio.file.Files.delete(dir)
                    }
                    return super.postVisitDirectory(dir, exc)
                }
            })
    } catch (e: Exception) {
        e.printStackTrace()
    }

}


fun deleteExtraFiles(
    destinationDirectory: java.nio.file.Path,
    sourceDirectory: java.nio.file.Path,
    condition: ((fileName: String) -> Boolean) = ::noFilter
) {
    // delete file not in
    java.nio.file.Files.walk(destinationDirectory)
        .filter { path -> path != destinationDirectory }
        .filter { path ->
            !java.nio.file.Files.exists(sourceDirectory.resolve(destinationDirectory.relativize(path)))
                    || !condition.invoke(path.fileName.toString())
        }
        .forEach { path ->
            try {
                if (!java.nio.file.Files.isDirectory(path)) { // Skip dir, avoid directory not empty exception
                    java.nio.file.Files.delete(path)
                    println("Deleted extra file: ${destinationDirectory.relativize(path)}")
                }
            } catch (e: java.io.IOException) {
                e.printStackTrace()
            }
        }
}

fun noFilter(fileName: String) = true
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
    val sourceDirectory: java.nio.file.Path = java.nio.file.Paths.get(src)

    if (!java.nio.file.Files.exists(sourceDirectory) || !java.nio.file.Files.isDirectory(
            sourceDirectory
        )
    ) {
        println("Source directory does not exist or is not a directory.")
        return
    }

    val destinationDirectory: java.nio.file.Path = java.nio.file.Paths.get(des)

    if (!java.nio.file.Files.exists(destinationDirectory)) {
        try {
            java.nio.file.Files.createDirectories(destinationDirectory)
            println("Created destination directory: $destinationDirectory")
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
        java.nio.file.Files.walkFileTree(
            sourceDirectory,
            object : java.nio.file.SimpleFileVisitor<java.nio.file.Path>() {
                override fun visitFile(
                    file: java.nio.file.Path,
                    attrs: java.nio.file.attribute.BasicFileAttributes
                ): java.nio.file.FileVisitResult {
                    // Skip filter
                    if (!condition.invoke(file.fileName.toString())) {
                        return java.nio.file.FileVisitResult.CONTINUE
                    }

                    val relativePath: java.nio.file.Path = sourceDirectory.relativize(file)
                    val destinationFile: java.nio.file.Path =
                        destinationDirectory.resolve(relativePath)

                    java.nio.file.Files.copy(
                        file,
                        destinationFile,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                    )
                    // println("Copied file: $relativePath")
                    return java.nio.file.FileVisitResult.CONTINUE
                }

                override fun preVisitDirectory(
                    dir: java.nio.file.Path,
                    attrs: java.nio.file.attribute.BasicFileAttributes
                ): java.nio.file.FileVisitResult {
                    val relativePath: java.nio.file.Path = sourceDirectory.relativize(dir)
                    val destinationDir: java.nio.file.Path =
                        destinationDirectory.resolve(relativePath)

                    if (!java.nio.file.Files.exists(destinationDir)) {
                        try {
                            java.nio.file.Files.createDirectories(destinationDir)
                            // println("Created directory: $relativePath")
                        } catch (e: java.io.IOException) {
                            e.printStackTrace()
                            return java.nio.file.FileVisitResult.TERMINATE
                        }
                    }

                    return java.nio.file.FileVisitResult.CONTINUE
                }
            })

        println("Directory copy completed!")
    } catch (e: java.io.IOException) {
        e.printStackTrace()
    }
}

/**
 * Create build.gradle.kts
 * @param isJava Android module or Java module
 * */
fun createBuildGradle(scriptPath: String, selfName: String, isJava: Boolean = false) {
    val buildScript = java.io.File(scriptPath)
    val path = if (isJava) {
        BUILD_TEMPLATE_PATH_JAVA
    } else {
        BUILD_TEMPLATE_PATH_ANDROID
    }
    val templateFile = java.io.File(path)

    if (!templateFile.exists()) {
        throw Exception("Template file ${templateFile.absolutePath} not found!")
    }
    val readText = templateFile.readText()
    val copyText = String.format(readText, "cn.jailedbird.module.${selfName}_api")
    buildScript.writeText(copyText)
}


fun deleteEmptyDir(path: String) {
    deleteEmptyDir(java.io.File(path))
}

// A包含A1 A2 A3 AN AN全为空 A不会被删除
fun deleteEmptyDir(file: java.io.File) {
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

/**
 * @param src0 : such as module1 abs path
 * */
@Deprecated("Deprecated because of low performance")
fun doSyncByGraldeApi(src0: String) {
    val t1 = System.currentTimeMillis()
    val src = "${src0}${java.io.File.separator}src${java.io.File.separator}main"
    val des = "${src0}_api${java.io.File.separator}src${java.io.File.separator}main"
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