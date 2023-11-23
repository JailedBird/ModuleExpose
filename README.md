# ModuleExpose

## 简介

ModuleExpose，顾名思义是将module内部代码通过脚本暴露出来；不同于接口下沉，ModuleExpose是直接将module中需要暴露的代码完整拷贝到module_expose项目，而这个过程是由ModuleExpose脚本自动生成，并完全保证每次编译module和module_expose代码完全一致；



## 接入方式

### 1、项目启用kts配置

因为脚本使用kts编写，因此需要在项目中启用kts配置；如因为gradle版本过低等原因导致无法接入kts，那应该是无法使用的；后续默认都开启kts，并使用kts语法脚本；



### 2、导入脚本到gradle目录

请参考工程，拷贝后目录如下：

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

`expose.gradle.kts`是我们核心脚本，包含配置函数；



### 3、 settings.gradle.kts导入脚本函数

启用kts后，建议将settings.gradle改为kts语法，然后添加如下代码：

```
apply(from = "$rootDir/gradle/expose/expose.gradle.kts")
val includeWithApi: (projectPaths: String) -> Unit by extra
val includeWithJavaApi: (projectPaths: String) -> Unit by extra
```

注意：只要正确启用kts，settings.gradle应该也是可以导入includeWithApi的，但是我没尝试；其次老项目针对本项目改造kts时，可以渐进式改造-只改settings.gradle.kts即可，其他文件是不需要kts改造的；



### 4、 修改脚本模板

脚本模板因项目不同而有所不同，需要自行根据项目修改，否则无法编译；

- build_gradle_template_android，针对Android模块自动生成的脚本模板，注意高版本gradle必须配置namespace，因此保留如下的配置：

  ```
  android {
      namespace = "%s"
  }
  ```

- build_gradle_template_java， 针对Java模块自动生成的脚本模板，配置较为简单；

- includeWithApi函数使用build_gradle_template_android模板生成Android Library模块

- includeWithJavaApi函数使用build_gradle_template_java模板生成Java Library模块

注意：Java模块编译更快，但是缺少Activity、Context等Android环境，请灵活使用；



### 5、 模块配置

将需要暴露的模块，在settings.gradle.kts 使用includeWithApi（或includeWithJavaApi）导入；

```
includeWithApi(":feature:settings")
includeWithApi(":feature:search")
```

然后在模块中的源码目录下创建名为expose的目录，然后将需要暴露的内容放在expose目录下，即可自动生成新模块 `${module_expose}` expose目录下的内容即可包含在新模块中；

生成细则：

1、 模块支持多个expose目录（递归、含子目录）同时暴露，这可以避免将实体类，接口等全部放在单个expose，看着很乱

2、 expose内部的文件，默认全部复制，但脚本提供了开关，可以自行更改并配置基于文件名的拷贝过滤；

###### 

### 6、 如何使用module_expose模块？

请使用 `compileOnly` 导入项目，如下：

```
compileOnly(project(mapOf("path" to ":feature:search_expose")))
```

错误：会导致资源冲突

```
implementation(project(mapOf("path" to ":feature:search_expose")))
```

原理解释：compileOnly只参与编译，不会被打包；implementation参与编译和打包；

因此search_expose只能使用compileOnly导入，确保解耦的模块之间可以访问到类引用，但不会造成打包时报错2个类相同的冲突问题；



## 参考资料：

[官方文档：将 build 配置从 Groovy 迁移到 KTS](https://developer.android.com/studio/build/migrate-to-kts?hl=zh-cn)







