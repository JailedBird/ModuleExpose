# ModuleExpose

## 项目资料

项目地址：[JailedBird/ModuleExpose](https://github.com/JailedBird/ModuleExpose)

维基文档：[JailedBird/ModuleExpose/wiki](https://github.com/JailedBird/ModuleExpose/wiki)

- [1、模块暴露](https://github.com/JailedBird/ModuleExpose/wiki/1、模块暴露)
- [2、接入方式](https://github.com/JailedBird/ModuleExpose/wiki/2、接入方式)
- [3、依赖注入](https://github.com/JailedBird/ModuleExpose/wiki/3、依赖注入)
- [4、性能测试](https://github.com/JailedBird/ModuleExpose/wiki/4、性能测试)

## 项目简介

安卓模块化最重要点就是：如何优雅的实现模块间通信；而模块之间通信往往需要获取相同的实体类或接口，导致部分涉及模块通信的实体类和接口被迫下沉到基础模块，造成 *基础模块膨胀、模块代码分散、不便维护* 等问题；

ModuleExpose方案（简称模块暴露），是将module内部的通信代码暴露出来，并自动生成新的module_expose模块；不同于手动形式的代码下沉，ModuleExpose是直接将module中需要暴露的代码完整拷贝到module_expose模块，而module_expose模块的生成、拷贝和配置是由ModuleExpose脚本自动完成，并保证编译时两者代码的完全同步；



最终，工程中包含如下几类核心模块：

- 基础模块：基础代码封装，可供任何业务模块使用；

- 业务模块：包含业务功能，业务模块可以依赖基础模块，但无法依赖其他业务模块；
- 暴露模块：由脚本基于业务模块或基础模块自动拷贝生成，业务模块可(compileOnly)依赖其他暴露模块；

效果如图所示：

![image-20231206141629690](https://zhaojunchen-1259455842.cos.ap-nanjing.myqcloud.com//imgimage-20231206141629690.png)

> 注意这种方案并非原创，原创出处如下：
>
> 思路原创：[微信Android模块化架构重构实践](https://mp.weixin.qq.com/s/6Q818XA5FaHd7jJMFBG60w)
>
> 项目原创： [github/tyhjh/module_api](https://github.com/tyhjh/module_api)
>
> [部分博客](https://juejin.cn/post/6945413567285821453)中称这种方式为***模块api化***；本文中 ***expose和api***、***模块api化和模块暴露*** 含义等价



## 工程简介

[ModuleExpose](https://github.com/JailedBird/ModuleExpose)工程内容：

- 基于nio重写脚本，并同时支持kts脚本和groovy脚本，详见维基文档；
- 基于性能的考量，对暴露规则和生成方式进行改进，详见维基文档；
- 综合优秀技术栈，优雅实现 模块化示例工程：
  - 结合 [now in android](https://github.com/android/nowinandroid) 项目编译脚本系统，实现快速生成和配置统一的模块
  - 结合最新ksp版本Hilt依赖注入框架，实现基于暴露接口的模块化解耦方案
  - 完整实现支持拼音的安卓App搜索启动器，包含Room等 Jetpack主流组件



更多细节，请参阅[维基文档](https://github.com/JailedBird/ModuleExpose/wiki) 和 [项目代码](https://github.com/JailedBird/ModuleExpose)；如果对大家有帮助，欢迎点亮项目star😘
