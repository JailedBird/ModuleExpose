# ModuleExpose

[![GitHub stars](https://img.shields.io/github/stars/JailedBird/ModuleExpose.svg)](https://github.com/JailedBird/ModuleExpose/stargazers) [![GitHub forks](https://img.shields.io/github/forks/JailedBird/ModuleExpose.svg)](https://github.com/JailedBird/ModuleExpose/network/members) [![GitHub issues](https://img.shields.io/github/issues/JailedBird/ModuleExpose.svg)](https://github.com/JailedBird/ModuleExpose/issues) [![GitHub license](https://img.shields.io/github/license/JailedBird/ModuleExpose.svg)](https://github.com/JailedBird/ModuleExpose/blob/master/LICENSE)

安卓模块化最重要点就是：如何优雅的实现模块间通信；而模块之间通信往往需要获取相同的实体类或接口，导致部分涉及模块通信的实体类和接口被迫下沉到基础模块，造成 *基础模块膨胀、模块代码分散、不便维护* 等问题；



## 快速接入

以kts版本为例展示，gradle版本、详细用法请参考对应维基文档；

方式1：克隆本项目，将本项目 gradle/expose目录文件拷贝到工程的gradle/expose即可；

方式2：考虑到克隆项目比较麻烦，提供命令行操作方式如下：

- 在项目gradle目录下创建expose目录，将命令行工作目录切换到gradle/expose 

- 在gradle/expose下执行curl命令，分别下载 核心脚本expose.gradle.kts、Android Library配置模板、Java Library配置模板

  ```
  curl -O https://raw.githubusercontent.com/JailedBird/ModuleExpose/main/gradle/expose/expose.gradle.kts
  curl -O https://raw.githubusercontent.com/JailedBird/ModuleExpose/main/gradle/expose/build_gradle_template_android
  curl -O https://raw.githubusercontent.com/JailedBird/ModuleExpose/main/gradle/expose/build_gradle_template_java
  ```

  PS：两个模板文件需要根据项目实际情况进行定制；

  

## 简单介绍

**ModuleExpose方案（简称模块暴露），是将模块（module）内部的这部分代码暴露出来并自动生成新的暴露模块（module_expose）；**

不同于手动形式的代码下沉，本方案是直接将module中需要暴露的代码完整拷贝到module_expose模块，而module_expose模块的生成和配置是由脚本自动完成，并保证编译时两者代码的完全同步；



最终，工程中包含如下几类核心模块：

- 基础模块：基础代码封装，可供任何业务模块使用；

- 业务模块：包含业务功能，业务模块可以依赖基础模块，但无法依赖其他业务模块；
- 暴露模块：由脚本基于业务模块或基础模块自动拷贝生成，业务模块可(compileOnly)依赖其他暴露模块；

示例如图：

![image-20231206141629690](https://zhaojunchen-1259455842.cos.ap-nanjing.myqcloud.com//imgimage-20231206141629690.png)

注意这种方案并非原创，原创出处如下：

> 思路原创：[微信Android模块化架构重构实践](https://mp.weixin.qq.com/s/6Q818XA5FaHd7jJMFBG60w)
>
> 项目原创： [github/tyhjh/module_api](https://github.com/tyhjh/module_api)



## 工程架构

示例工程简介：

- 基于nio重写脚本，并同时支持kts脚本和groovy脚本，详见维基文档；
- 基于性能的考量，对暴露规则和生成方式进行改进，详见维基文档；
- 综合优秀技术栈，优雅实现 模块化示例工程：
  - 结合 [now in android](https://github.com/android/nowinandroid) 项目编译脚本系统，实现快速生成和配置统一的模块
  - 结合最新ksp版本Hilt依赖注入框架，实现基于暴露接口的模块化解耦方案
  - 完整实现支持拼音的安卓App搜索启动器，包含Room等 Jetpack主流组件



## 工程文档

详细维基文档：[JailedBird/ModuleExpose/wiki](https://github.com/JailedBird/ModuleExpose/wiki)

- [1、模块暴露](https://github.com/JailedBird/ModuleExpose/wiki/1、模块暴露)
- [2、接入方式](https://github.com/JailedBird/ModuleExpose/wiki/2、接入方式)
- [3、依赖注入](https://github.com/JailedBird/ModuleExpose/wiki/3、依赖注入)
- [4、性能测试](https://github.com/JailedBird/ModuleExpose/wiki/4、性能测试)



如果本方案对大家有帮助，欢迎点亮项目star支持作者😘



