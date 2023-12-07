# ModuleExpose

## 项目资料

最新项目地址：[https://github.com/JailedBird/ModuleExpose](https://github.com/JailedBird/ModuleExpose)

详细维基文档：[https://github.com/JailedBird/ModuleExpose/wiki](https://github.com/JailedBird/ModuleExpose/wiki)

- [1、模块暴露](https://github.com/JailedBird/ModuleExpose/wiki/1、模块暴露)
- [2、接入方式](https://github.com/JailedBird/ModuleExpose/wiki/2、接入方式)
- [3、依赖注入](https://github.com/JailedBird/ModuleExpose/wiki/3、依赖注入)
- [4、性能测试](https://github.com/JailedBird/ModuleExpose/wiki/4、性能测试)

## 项目简介

安卓模块化最重要点就是：如何优雅的实现模块间通信；而模块之间通信往往需要获取相同的实体类或接口，导致部分涉及模块通信的实体类和接口被迫下沉到基础模块，造成 *基础模块膨胀、模块代码分散、不便维护* 等问题；

ModuleExpose，是将module内部需要暴露的代码通过脚本自动暴露出来；不同于手动形式的代码下沉，ModuleExpose是直接将module中需要暴露的代码完整拷贝到module_expose模块，而module_expose模块的生成、拷贝和配置是由ModuleExpose脚本自动完成，并保证编译时两者代码的完全同步；



最终，工程中包含如下几类核心模块：

- 基础模块：基础代码封装，可供任何业务模块使用；

- 业务模块：包含业务功能，业务模块可以依赖基础模块，但无法依赖其他业务模块（避免循环依赖）；
- 暴露模块：由脚本基于业务模块或基础模块自动拷贝生成，业务模块可(compileOnly)依赖其他暴露模块，打破模块壁垒的同时避免循环引用；

效果如图所示：

![image-20231206141629690](https://zhaojunchen-1259455842.cos.ap-nanjing.myqcloud.com//imgimage-20231206141629690.png)
