# ModuleExpose

项目地址：[https://github.com/JailedBird/ModuleExpose](https://github.com/JailedBird/ModuleExpose)

维基文档：[https://github.com/JailedBird/ModuleExpose/wiki](https://github.com/JailedBird/ModuleExpose/wiki)

## 序言

Android模块化必须要解决的问题是 *如何实现模块间通信* ？而模块之间通信往往需要获取相同的实体类和接口，造成部分涉及模块通信的接口和实体类被迫下沉到基础模块，导致 基础模块代码膨胀、模块代码分散和不便维护等问题；



ModuleExpose方案使用模块暴露&依赖注入框架Hilt的方式，实现模块间通信：

- 使用模块暴露（模块api化）解决基础模块下沉问题
- 使用依赖注入框架Hilt实现基于接口的模块解耦方案

## 简介

ModuleExpose，是将module内部需要暴露的代码通过脚本自动暴露出来；不同于手动形式的接口下沉，ModuleExpose是直接将module中需要暴露的代码完整拷贝到module_expose模块，而module_expose模块的生成、拷贝和配置是由ModuleExpose脚本自动完成，并保证编译时两者代码的完全同步；



最终，工程中包含如下几类核心模块：

- 基础模块：基础代码封装，可供任何业务模块使用；

- 业务模块：包含业务功能，业务模块可以依赖基础模块，但无法依赖其他业务模块（避免循环依赖）；
- 暴露模块：由脚本基于业务模块或基础模块自动拷贝生成，业务模块可依赖其他暴露模块（通过compileOnly方式，只参与编译不参与打包），避免模块通信所需的接口、数据实体类下沉到基础模块，造成基础模块膨胀、业务模块核心类分散到基础模块等问题；

如图所示：

![image-20231206141629690](https://zhaojunchen-1259455842.cos.ap-nanjing.myqcloud.com//imgimage-20231206141629690.png)

注意这种方案并非原创，原创出处如下：

思路原创：[微信Android模块化架构重构实践](https://mp.weixin.qq.com/s/6Q818XA5FaHd7jJMFBG60w)

> 先寻找代码膨胀的原因。
>
> 翻开基础工程的代码，我们看到除了符合设计初衷的存储、网络等支持组件外，还有相当多的业务相关代码。这些代码是膨胀的来源。但代码怎么来的，非要放这？一切不合理皆有背后的逻辑。在之前的架构中，我们大量使用Event事件总线作为模块间通信的方式，也基本是唯一的方式。使用Event作为通信的媒介，自然要有定义它的地方，好让模块之间都能知道Event结构是怎样的。这时候基础工程好像就成了存放Event的唯一选择——Event定义被放在基础工程中；接着，遇到某个模块A想使用模块B的数据结构类，怎么办？把类下沉到基础工程；遇到模块A想用模块B的某个接口返回个数据，Event好像不太适合？那就把代码下沉到基础工程吧……
>
> 就这样越来越多的代码很“自然的”被下沉到基础工程中。
>
> 
>
> implementation工程提供逻辑的实现。api工程提供对外的接口和数据结构。library工程，则提供该模块的一些工具类。



项目原创:  [github/tyhjh/module_api](https://github.com/tyhjh/module_api)

> 如果每次有一个模块要使用另一个模块的接口都把接口和相关文件放到公共模块里面，那么公共模块会越来越大，而且每个模块都依赖了公共模块，都依赖了一大堆可能不需要的东西；
>
> 所以我们可以提取出每个模块提供api的文件放到各种单独的模块里面；比如user模块，我们把公共模块里面的User和UserInfoService放到新的user-api模块里面，这样其他模块使用的时候可以单独依赖于这个专门提供接口的模块，以此解决公共模块膨胀的问题



本人工作：

- 使用kts和nio重写脚本，基于性能的考量，对暴露规则和生成方式进行改进；详见 [关于性能](#关于性能)
- 将[nowinandroid](https://github.com/android/nowinandroid)项目编译脚本系统、Ksp版本的Hilt依赖注入框架、示例工程三者结合起来，完善基于 *模块暴露&依赖注入框架* 的模块解耦示例工程；
- 将api改名expose（PS：因内部项目使用过之前的api方案，为避免冲突所以改名，也避免和大佬项目名字冲突😘 脚本中亦可自定义关键词）



术语说明：

- [部分博客](https://juejin.cn/post/6945413567285821453)中称这种方式为***模块api化***，我觉得这是合理的；本文的语境中的expose和api是等价的意思；




## 模块暴露

**1、项目启用kts配置**

ModuleExpose同时适配了Groovy和Kts，这里以kts脚本演示；

Groovy接入方式：

- 参考文档：https://github.com/JailedBird/ModuleExpose/wiki
- 参考项目：https://github.com/JailedBird/ModuleExpose/tree/main/subproj/GradleSample



**2、导入脚本到gradle目录&修改模板**

请拷贝示例工程`gradle/expose`目录到个人项目gradle目录，拷贝后目录如下：

```
Path
ModuleExpose\gradle

gradle
│  libs.versions.toml
├─expose
│      build_gradle_template_android
│      build_gradle_template_java
│      build_gradle_template_expose
│      expose.gradle.kts
└─wrapper
        gradle-wrapper.jar
        gradle-wrapper.properties
```

其中：expose.gradle.kts是模块暴露的核心脚本，包含若干函数和配置参数；

其中：build_gradle_template_android和build_gradle_template_java脚本模板因项目不同而有所不同，需要自行根据项目修改，否则无法编译；

- build_gradle_template_android，生成Android模块的脚本模板，注意高版本gradle必须配置namespace，因此最好保留%s的配置（细节见脚本）：

  ```
  android {
      namespace = "%s"
  }
  ```

- build_gradle_template_java， 生成Java模块的脚本模板，配置较为简单；

- includeWithExpose函数使用build_gradle_template_android模板生成Android Library模块

- includeWithJavaExpose函数使用build_gradle_template_java模板生成Java Library模块

- build_gradle_template_expose，不同于build_gradle_template_android、build_gradle_template_java的模板形式的配置，使用includeWithExpose、includeWithJavaExpose时，**会优先检查模块根目录是否存在build_gradle_template_expose**，如果存在则**优先、直接**将build_gradle_template_expose内容拷贝到module_expose, 作为build.gradle.kts ！ **保留这个配置的原因在于：如果需要暴露的类，引用三方类如gson、但不便将三方库implementation到build_gradle_template_android，这会导致module_expose编译报错，因此为解决这样的问题，最好使用自定义module_expose脚本（拷贝module的配置、稍加修改即可）**

  PS：注意这几个模板都是无后缀的，kts后缀文件会被IDE提示一大堆东西；

**注意：** Java模块编译更快，但是缺少Activity、Context等Android环境，请灵活使用；当然最灵活的方式是为每个module_expose单独配置build_gradle_template_expose （稍微麻烦一点）；另外，如果不用includeWithJavaExpose，其实build_gradle_template_java也是不需要的；



**3、settings.gradle.kts导入脚本函数**

根目录settings.gradle.kts配置如下：

```
apply(from = "$rootDir/gradle/expose/expose.gradle.kts")
val includeWithExpose: (projectPaths: String) -> Unit by extra
val includeWithJavaExpose: (projectPaths: String) -> Unit by extra
```



**4、模块配置**

将需要暴露的模块，在settings.gradle.kts 使用includeWithExpose（或includeWithJavaExpose）导入；

```
includeWithExpose(":feature:settings")
includeWithExpose(":feature:search")
```

即可自动生成新模块 `${module_expose}`；然后在模块源码目录下创建名为expose的目录，将需要暴露的文件放在expose目录下， expose目录下的文件即可在新模块中自动拷贝生成；

生成细则：

1、 模块支持多个expose目录（递归、含子目录）同时暴露，这可以避免将实体类，接口等全部放在单个expose，看着很乱

2、 expose内部的文件，默认全部复制，但脚本提供了开关，可以自行更改并配置基于文件名的拷贝过滤；



**5、使用module_expose模块**

请使用 `compileOnly` 导入项目，如下：

```
compileOnly(project(mapOf("path" to ":feature:search_expose")))
```

错误用法：会导致资源冲突

```
implementation(project(mapOf("path" to ":feature:search_expose")))
```

原理解释：compileOnly只参与编译，不会被打包；implementation参与编译和打包；

因此search_expose只能使用compileOnly导入，确保解耦的模块之间可以访问到类引用，但不会造成打包时2个类相同的冲突问题；



**6、 添加clean module_expose**

当clean项目，最好是能删除所有module_expose目录所有代码，请在项目根build.gradle.kts中添加如下task任务；

```
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
```



## 依赖注入

**基于模块暴露的相关接口，可以使用依赖注入框架Hilt实现基于接口的解耦；** 当然如果大家不使用Hilt技术栈的话，这节可以跳过；

本节内容会以业务模块search和settings为例，通过代码展示：

- search模块跳转到settings模块，打开SettingsActivity
- settings模块跳转到search模块，打开SearchActivity



PS：关于Hilt的配置和导入，本项目直接沿用nowinandroid工程中build-logic的配置，具体配置和使用请参考本项目和nowinandroid项目；



**1、 基本配置&工程结构：**

![image-20231204171057867](https://zhaojunchen-1259455842.cos.ap-nanjing.myqcloud.com//imgimage-20231204171057867.png)

导入脚本之后，使用includeWithExpose导入三个业务模块，各自生成对应的module_expose；

**注意，请将`*_expose/`添加到gitignore，避免expose模块提交到git**



**2、 业务模块接口暴露&实现**

settings模块expose目录下暴露`SettingExpose`接口， 脚本会自动将其同步拷贝到settings_expose中对应expose目录

![image-20231204171204479](https://zhaojunchen-1259455842.cos.ap-nanjing.myqcloud.com//imgimage-20231204171204479.png)



exposeimpl/SettingExposeImpl实现SettingExpose接口的具体功能，完善跳转功能

```
class SettingExposeImpl @Inject constructor() : SettingExpose {
    override fun startSettingActivity(context: Context) {
        SettingsActivity.start(context)
    }
}
```



**3、 Hilt添加注入接口绑定**

使用Hilt绑定全局单例SettingExpose接口实现，其对应实现为SettingExposeImpl

![image.png](https://zhaojunchen-1259455842.cos.ap-nanjing.myqcloud.com//img1701008419851-2d972419-d0bf-4d95-8b24-9c3db6ee5cdb.png)



**4、 search模块compileOnly导入settings_expose**

```
compileOnly(projects.feature.settingsExpose)
```

**注意，模块暴露依赖只能使用compileOnly**，保证编译时候能找到对应文件即可；另外projects.feature.settingsExpose这种项目导入方式，需要在settings.gradle.kts启用project类型安全配置；

```
 enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
```



**5、 search注入并使用SettingExpose**

```
@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {
    @Inject
    lateinit var settingExpose: SettingExpose
    
    private val listener = object : AppSettingsPopWindow.Listener {

        override fun settings() {
            settingExpose.startSettingActivity(this@SearchActivity)
        }
    }
}
```



**6、 实现解耦**

最终实现【search模块跳转到settings模块，打开SettingsActivity】， 至于【settings模块跳转到search模块，打开SearchActivity】的操作完全一致，不重复叙述了；



## 性能问题

***注：测试设备SSD为顶配PCIE4 zhitai 7100，磁盘性能会影响观测结果（PS：长江存储牛逼😘）***

开篇提到，ModuleExpose通过脚本实现自动暴露，并保证编译时module和moudle_expose的代码完全同步；那深究下ModuleExpose includeWithExpose等函数的执行时机是什么？又为什么能保证代码是完全同步的呢？

这个和gradle生命周期有关，我不是很懂，但已知有：

- 项目sync时候，会执行setting.gradle.kts文件，同步工程模块
- 项目运行时候，会执行setting.gradle.kts文件，同步工程模块

setting.gradle.kts中使用自定义的includeWithExpose函数，实现原有include module功能，并生成和include module_expose，这个操作在每次run运行都是会执行的，因此expose脚本的处理方式和性能，会对项目编译产生极大的影响！

**1、分析下脚本includeWithExpose处理细节和性能问题**

```
fun includeWithExpose(module: String, isJava: Boolean, expose: String, condition: (String) -> Boolean) {
    include(module)
    measure("Expose ${module}", true) {
        val moduleProject = project(module)
        val src = moduleProject.projectDir.absolutePath
        val des = "${src}_${MODULE_EXPOSE_TAG}"
        // generate build.gradle.kts
        generateBuildGradle(
            src, BUILD_TEMPLATE_PATH_CUSTOM, des,
            "build.gradle.kts", moduleProject.name, isJava
        )
        doSync(src, expose, condition)
        // Add module_expose to Project!
        include("${module}_${MODULE_EXPOSE_TAG}")
    }
}
```

- include module，这个本身就需要做，不计入额外损耗；
- generateBuildGradle，创建module_expose的build.gradle.kts，涉及单个文件的拷贝；
- doSync，源码文件的同步，处理稍微复杂，后续单独说；
- include module_expose, 这个操作是将module_expose include到工程，开销和include module相当，实测无影响；

总体看， 除第三步文件同步doSync，其他的性能损耗都无关紧要；



**2、分析下doSync文件同步的细节：**

```
fun doSync(src0: String, expose: String, condition: (String) -> Boolean) {
    val start = System.currentTimeMillis()
    val src = "${src0}${File.separator}src${File.separator}main"
    val des = "${src0}_${MODULE_EXPOSE_TAG}${File.separator}src${File.separator}main"
    // Do not delete
    val root = File(src)
    val pathList = mutableListOf<String>()
    if (root.exists() && root.isDirectory) {
        measure("findDirectoryByNio") {
        	// 1): 使用NIO 搜索名称为expose目录
            findDirectoryByNIO(src, expose, pathList)
        }
    }
    pathList.forEach { copyFrom ->
        val suffix = copyFrom.removePrefix(src)
        val copyTo = des + suffix
        measure("syncDirectory $copyFrom") {
            // 2): 实现文件同步 
            //	a) 先遍历module_expose删除不存在于module中的文件 
            //	b) 将module中的文件，通过NIO StandardCopyOption.REPLACE_EXISTING模式直接拷贝
            syncDirectory(copyFrom, copyTo, condition)
        }
        // 3):删除空目录
        measure("Delete empty dir") {
            // remove empty dirs
            deleteEmptyDir(copyTo)
        }
    }
    debug("Module $src all spend ${(System.currentTimeMillis() - start)} ms")
}
```



1)、 目录搜索

基于NIO实现目录遍历，搜索所有名称为expose的目录；使用NIO的直接原因在于NIO的性能远高于Java IO；开发时使用Java IO基于递归搜索，耗时12+ms，而NIO耗时1~2ms；从时间复杂度来看，此算法时间复杂度为N，N为目录数量，实际项目N一般很小，耗时可以忽略不计；

2）、 文件同步

基于1的目录搜索结果，在expose目录下定点文件同步，可以减少很多开销和遍历；

a）删除module_expose expose目录下，不存在于module expose中的文件， 文件删除比较耗时，貌似单个文件0.5ms的样子

b）以替换拷贝形式（**REPLACE_EXISTING**），复制module expose目录下的文件，到module_expose expose目录下，文件拷贝比较耗时，貌似也差不多单个文件0.5~1ms；**每次同步都需要拷贝，很不完美，后续有优化方案**

3）、 删除空目录

主要是精简目录结构，耗时不多；不在意空目录对视觉干扰的甚至可以去掉；另外这是基于Java IO的操作，写代码的是这块暂时没优化到；



**3、 优化理论及方案**

1、 绝大部分情况，我们是不会修改expose中任何代码的，因此可以认为 **95%+** 情况下的文件同步，都只是 2-b）中描述的【文件同步-替换拷贝】情况；因此直接读取拷贝源文件，拷贝目的文件，对比他们是否存在差异，性能是否会更好？经过尝试，如下方案有更好的性能，单个小文件处理耗时不超过1ms：

```
fun areFilesContentEqual(path1: Path, path2: Path,tag:String=""): Boolean {
    try {
        val size1 = Files.size(path1)
        val size2 = Files.size(path2)
        if (size1 != size2) {
            return false // Different sizes, files can't be equal
        }
        // 小细节 4MB大文件直接判定不相同，通过文件系统直接拷贝同步 项目中是不可能出现这种情况的！
        if(size1 > 4_000_000){ // 4MB return false 
            return false
        }
        val start = System.currentTimeMillis()
        val content1 = Files.readAllBytes(path1) // Huge file will cause performance problem
        val content2 = Files.readAllBytes(path2)
        val isSame = content1.contentEquals(content2)
        debug("$tag Read ${path1.fileName}*2 & FilesContentEqual spend ${System.currentTimeMillis()-start} ms, isSame $isSame")
        return isSame
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

```

先判定内容是否一致，不一致才进行拷贝【**算是增量编译了吧🤣**】；实测针对普通文件读文件和比较耗时小于**1ms**！

```
if(areFilesContentEqual(file,destinationFile, "[directorySync]")){
    // Do nothing
}else{
    measure("[directorySync] ${file.fileName} sync with copy REPLACE_EXISTING",true) {
        Files.copy(
            file,
            destinationFile,
            StandardCopyOption.REPLACE_EXISTING
        )
    }

}
```



2、 改进点，看下老方案吧， a) 针对范围过大，未细化目录；b) 针对api后缀的目录，这个格式不是很灵活；

```
https://github.com/tyhjh/module_api#%E5%85%B7%E4%BD%93%E5%AE%9E%E7%8E%B0
def includeWithApi(String moduleName) {
    //先正常加载这个模块
    include(moduleName)
    // 每次编译删除之前的文件
    deleteDir(targetDir)
    //复制.api文件到新的路径
    copy() {
        from originDir
        into targetDir
        exclude '**/build/'
        exclude '**/res/'
        include '**/*.api'
    }
}
```

对比看下我的输出结果，二次编译情况下耗时，只能说相对完美 哈哈😘（测试tag：release-1.0.0-beta）

```
ModuleExpose :core:common spend 2ms
ModuleExpose :feature:settings spend 2ms
ModuleExpose :feature:search spend 2ms
ModuleExpose :feature:about spend 2ms
```

极端情况下测试：在settings模块expose目录下添加100个内容为expose.gradle.kts的文件，单文件480行、16.2 KB；

分别测试首次编译、源码不变情况下的二次编译、修改单个文件的二次编译，耗时如下：

```
ModuleExpose :feature:settings spend 80ms
ModuleExpose :feature:settings spend 32ms
ModuleExpose :feature:settings spend 34ms 
// 可以看出源码修改的耗时 介于首次编译 ~ 源码不变情况下的二次编译之间 且耗时和修改内容成正比！
```

**综上：性能还是非常优秀的，请大家放心食用😘**





3、另外，如果项目中模块暴露的文件已经多到影响编译，那么建议直接将暴露的模块，单独抽出来为真正的模块：

- module_expose本就来自module，甚至包名都没变，直接将其作为独立模块是完全没问题的，但是要注意将其添加到git中去、当然最好改个名字吧
- 删除module中expose的内容，然后直接implement module_expose，其他compileOnly module_expose 直接搜索替换为implement module_expose即可；
- 使用include而非includeWithExpose导入module，避免自动生成module_expose；

Ok，几乎零成本拆除，这也是为什么相比原创项目要将暴露内容集中收敛到expose目录——**便于集中管理和后期迁移**



**综上，绝绝绝大部分情况下，即已生成module_expose并且expose目录内容无任何变化，ModuleExpose额外性能开销约为：2N个文件读耗时+这2N个文件内容的ByteArray对象的contentEquals耗时！其中N为module中expose目录下的文件数量**



**除MoudleExpose本身的开销，还需要考虑的一个问题是：源码不变情况下每次运行，module同步到module_expose，是否会破坏Transform、注解处理器、模块编译等增量编译机制，进而导致其他的编译耗时问题呢？**

- 在之前不做文件内容校验，直接拷贝复制的情况下，虽然拷贝前后文件内容完全一致，但是从文件管理器可以看出其文件创建时间是变化了的；因为对gradle的编译机制不熟悉，所以我不确定他是否触发了module_expose的重新编译、以及其他可能的副作用；【按理来说可以不重新编译，但是文件时间戳确实又变了🤣】
- 启用文件内容校验的情况下，module_expose中的文件也不会被重新生成，从任务管理器看文件本身、build文件夹等的创建时间也未变化，因此姑且可以断定 **最终的ModuleExpose不会破坏相关的增量机制，造成其他性能问题**； （有了解这个的大佬也欢迎指正！）

编译日志如下，看起来确实是不会造成重新编译等问题；

```
> Task :feature:search:kspProDebugKotlin UP-TO-DATE
> Task :feature:search:compileProDebugKotlin UP-TO-DATE
> Task :feature:search:javaPreCompileProDebug UP-TO-DATE
> Task :feature:search:compileProDebugJavaWithJavac UP-TO-DATE
> Task :feature:search_expose:preBuild UP-TO-DATE
> Task :feature:search_expose:preProDebugBuild UP-TO-DATE
> Task :feature:search_expose:generateProDebugBuildConfig UP-TO-DATE
> Task :feature:search_expose:generateProDebugResValues UP-TO-DATE
> Task :feature:search_expose:generateProDebugResources UP-TO-DATE
```

**另外：虽然但是，还是尽量减少需要expose的内容吧，非必要不暴露！如果项目根本不需要暴露，请不要使用includeWithExpose，直接include！**



**groovy or kts？**

老项目，几乎不可能只存在kts；如果渐进式引入kts仍然不行，ModuleExpose是同样支持groovy的；

文档请参考：https://github.com/JailedBird/ModuleExpose/wiki

项目请参考：https://github.com/JailedBird/ModuleExpose/tree/main/subproj/GradleSample



**自定义配置**

*expose.gradle.kts* 中定义了很多自定义配置，比如需要暴露的目录名称、暴露模块名称、日志开关等，方便大家自定义；

```
private val MODULE_EXPOSE_TAG = "expose"
private val DEFAULT_EXPOSE_DIR_NAME = "expose"
private val SCRIPT_DIR = "$rootDir/gradle/expose/"
private val BUILD_TEMPLATE_PATH_JAVA = "${SCRIPT_DIR}build_gradle_template_java"
private val BUILD_TEMPLATE_PATH_ANDROID = "${SCRIPT_DIR}build_gradle_template_android"
private val BUILD_TEMPLATE_PATH_CUSTOM = "build_gradle_template_expose"
private val ENABLE_FILE_CONDITION = false
private val MODULE_NAMESPACE_TEMPLATE = "cn.jailedbird.module.%s_expose"
private val DEBUG_ENABLE = false
```



## 参考资料

1、思路原创：[微信Android模块化架构重构实践](https://mp.weixin.qq.com/s/6Q818XA5FaHd7jJMFBG60w)

2、项目原创：[github/tyhjh/module_api](https://github.com/tyhjh/module_api)

3、脚本迁移：[将 build 配置从 Groovy 迁移到 KTS](https://developer.android.com/studio/build/migrate-to-kts?hl=zh-cn)

4、参考文章：[Android模块化设计方案之接口API化](https://juejin.cn/post/6945413567285821453)

5、Nowinandroid：[https://github.com/android/nowinandroid](https://github.com/android/nowinandroid)

6、Dagger项目：[https://github.com/google/dagger](https://github.com/google/dagger)

7、Hilt官方教程：[https://developer.android.com/training/dependency-injection/hilt-android](https://developer.android.com/training/dependency-injection/hilt-android?hl=zh-cn)











