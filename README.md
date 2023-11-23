# ModuleExpose

## 简介

ModuleExpose，顾名思义是将module内部代码通过脚本自动暴露出来；不同于手动形式的接口下沉，ModuleExpose是直接将module中需要暴露的代码完整拷贝到module_expose模块，而module_expose模块的生成、拷贝和配置是由ModuleExpose脚本自动完成，并保证编译时两者代码的完全同步；



注意这种方案并非原创，原创出处如下：

思路原创：[微信Android模块化架构重构实践](https://mp.weixin.qq.com/s/6Q818XA5FaHd7jJMFBG60w)

> 先寻找代码膨胀的原因。
>
> 翻开基础工程的代码，我们看到除了符合设计初衷的存储、网络等支持组件外，还有相当多的业务相关代码。这些代码是膨胀的来源。但代码怎么来的，非要放这？一切不合理皆有背后的逻辑。在之前的架构中，我们大量适用Event事件总线作为模块间通信的方式，也基本是唯一的方式。使用Event作为通信的媒介，自然要有定义它的地方，好让模块之间都能知道Event结构是怎样的。这时候基础工程好像就成了存放Event的唯一选择——Event定义被放在基础工程中；接着，遇到某个模块A想使用模块B的数据结构类，怎么办？把类下沉到基础工程；遇到模块A想用模块B的某个接口返回个数据，Event好像不太适合？那就把代码下沉到基础工程吧……
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

- 将api改名expose，哈哈 免得和大佬项目名字冲突😘

  


## 接入方式

0、show you my code！

工程代码：https://github.com/JailedBird/ModuleExpose 



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

**注意：**Java模块编译更快，但是缺少Activity、Context等Android环境，请灵活使用；另外，如果不用includeWithJavaApi，其实build_gradle_template_java也是不需要的；



**3、settings.gradle.kts导入脚本函数**

根目录settings.gradle.kts配置如下：

```
apply(from = "$rootDir/gradle/expose/expose.gradle.kts")
val includeWithApi: (projectPaths: String) -> Unit by extra
val includeWithJavaApi: (projectPaths: String) -> Unit by extra
```

注意：只要正确启用kts，settings.gradle应该也是可以导入includeWithApi的，但是我没尝试；其次老项目针对ModuleExpose改造kts时，可以渐进式改造，即只改settings.gradle.kts即可，其他文件是不需要kts改造的；



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



## 参考资料：

思路原创：[微信Android模块化架构重构实践](https://mp.weixin.qq.com/s/6Q818XA5FaHd7jJMFBG60w)

项目原创:  [github/tyhjh/module_api](https://github.com/tyhjh/module_api)

官方文档：[将 build 配置从 Groovy 迁移到 KTS](https://developer.android.com/studio/build/migrate-to-kts?hl=zh-cn)











