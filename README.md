# ModuleExpose

项目地址：[https://github.com/JailedBird/ModuleExpose](https://github.com/JailedBird/ModuleExpose)

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

- 使用kts和nio重写脚本，基于性能的考量，对暴露规则和生成方式进行改进；
- 将[nowinandroid](https://github.com/android/nowinandroid)项目编译脚本系统、Ksp版本的Hilt依赖注入框架、示例工程三者结合起来，完善基于 *模块暴露&依赖注入框架* 的模块解耦示例工程；
- 将api改名expose（PS：因内部项目使用过之前的api方案，为避免冲突所以改名，也避免和大佬项目名字冲突😘 脚本中亦可自定义关键词）



术语说明：

- [部分博客](https://juejin.cn/post/6945413567285821453)中称这种方式为***模块api化***，我觉得这是合理的；本文的语境中的expose和api是等价的意思；




## 模块暴露

**1、项目启用kts配置**

因为脚本使用kts编写，因此需要在项目中启用kts配置；如因为gradle版本过低等原因导致无法接入kts，那应该是无法使用的；后续默认都开启kts，并使用kts语法脚本；



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

- build_gradle_template_android，生成Android模块的脚本模板，注意高版本gradle必须配置namespace，因此最好保留如下的配置（细则见脚本如何处理的）：

  ```
  android {
      namespace = "%s"
  }
  ```

- build_gradle_template_java， 生成Java模块的脚本模板，配置较为简单；

- includeWithApi函数使用build_gradle_template_android模板生成Android Library模块

- includeWithJavaApi函数使用build_gradle_template_java模板生成Java Library模块

- build_gradle_template_expose，不同于build_gradle_template_android、build_gradle_template_java的模板形式的配置，使用includeWithApi、includeWithJavaApi时，**会优先检查模块根目录是否存在build_gradle_template_expose**，如果存在则**优先、直接**将build_gradle_template_expose内容拷贝到module_expose, 作为build.gradle.kts ！ **保留这个配置的原因在于：如果需要暴露的类，引用三方类如gson、但不便将三方库implementation到build_gradle_template_android，这会导致module_expose编译报错，因此为解决这样的问题，最好使用自定义module_expose脚本（拷贝module的配置、稍加修改即可）**

  PS：注意这几个模板都是无后缀的，kts后缀文件会被IDE提示一大堆东西；

**注意：** Java模块编译更快，但是缺少Activity、Context等Android环境，请灵活使用；当然最灵活的方式是为每个module_expose单独配置build_gradle_template_expose （稍微麻烦一点）；另外，如果不用includeWithJavaApi，其实build_gradle_template_java也是不需要的；



**3、settings.gradle.kts导入脚本函数**

根目录settings.gradle.kts配置如下：

```
apply(from = "$rootDir/gradle/expose/expose.gradle.kts")
val includeWithApi: (projectPaths: String) -> Unit by extra
val includeWithJavaApi: (projectPaths: String) -> Unit by extra
```

（PS：只要正确启用kts，settings.gradle应该也是可以导入includeWithApi的，但是我没尝试；其次老项目针对ModuleExpose改造kts时，可以渐进式改造，即只改settings.gradle.kts即可）



**4、模块配置**

将需要暴露的模块，在settings.gradle.kts 使用includeWithApi（或includeWithJavaApi）导入；

```
includeWithApi(":feature:settings")
includeWithApi(":feature:search")
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

错误：会导致资源冲突

```
implementation(project(mapOf("path" to ":feature:search_expose")))
```

原理解释：compileOnly只参与编译，不会被打包；implementation参与编译和打包；

因此search_expose只能使用compileOnly导入，确保解耦的模块之间可以访问到类引用，但不会造成打包时2个类相同的冲突问题；



## 依赖注入

**基于模块暴露的相关接口，可以使用依赖注入框架Hilt实现基于接口的解耦；** 当然如果大家不使用Hilt技术栈的话，这节可以跳过；

本节内容会以业务模块search和settings为例，通过代码展示：

- search模块跳转到settings模块，打开SettingsActivity
- settings模块跳转到search模块，打开SearchActivity



PS：关于Hilt的配置和导入，本项目直接沿用nowinandroid工程中build-logic的配置，具体配置和使用请参考本项目和nowinandroid项目；



**1、 基本配置&工程结构：**

![image.png](https://zhaojunchen-1259455842.cos.ap-nanjing.myqcloud.com//imgimg1701008044066-f5a578b3-0e3a-4f50-a891-f71333294e65.png)

导入脚本之后，使用includeWithApi导入三个业务模块，各自生成对应的module_expose；

**注意，请将`*_expose/`添加到gitignore，避免expose模块提交到git**



**2、 业务模块接口暴露&实现**

settings模块expose目录下暴露`SettingExpose`接口， 脚本会自动将其同步拷贝到settings_expose中对应expose目录

![image.png](https://zhaojunchen-1259455842.cos.ap-nanjing.myqcloud.com//img1701008271448-b0b32e1f-0988-479e-bc16-ba9845414ea7.png)



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







## 参考资料

1、思路原创：[微信Android模块化架构重构实践](https://mp.weixin.qq.com/s/6Q818XA5FaHd7jJMFBG60w)

2、项目原创：[github/tyhjh/module_api](https://github.com/tyhjh/module_api)

3、脚本迁移：[将 build 配置从 Groovy 迁移到 KTS](https://developer.android.com/studio/build/migrate-to-kts?hl=zh-cn)

4、参考文章：[Android模块化设计方案之接口API化](https://juejin.cn/post/6945413567285821453)

5、Nowinandroid：[https://github.com/android/nowinandroid](https://github.com/android/nowinandroid)

6、Dagger项目：[https://github.com/google/dagger](https://github.com/google/dagger)

7、Hilt官方教程：[https://developer.android.com/training/dependency-injection/hilt-android](https://developer.android.com/training/dependency-injection/hilt-android?hl=zh-cn)











