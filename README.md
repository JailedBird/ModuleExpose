# SmartAppSearch

SmartAppSearch is a simple and smart app launcher



Bug & Confuse

1、 elevation阴影失效

解决方案：为View添加非透明的背景色

方案来源：[Android "elevation" not showing a shadow](https://stackoverflow.com/questions/27477371/android-elevation-not-showing-a-shadow)



2、 Hilt注入运行时参数

https://www.yuque.com/jailedbird/exkxrk/mxgn49ts511b1622?singleDoc# 《Hilt运行时注入参数》
https://www.yuque.com/jailedbird/exkxrk/gkyuffo5v76r4r0s?singleDoc# 《在非Android内中注入Hilt对象》
## TODO

- 添加匹配规则，使用首字符匹配or非首字母按顺序匹配
- 启动App时检查 or try catch异常
- ~~卸载功能需要完善和优化 or 禁用~~
- ~~启动APK时候需要可能需要优化启动方法~~
- 搜索无果时，添加无内容交互、点击可刷新界面
- 设置页面、开关及其配置加载
- 最近搜索历史记录
- ~~添加APK新增和卸载的监听 https://stackoverflow.com/questions/7470314/receiving-package-install-and-uninstall-events~~
- ~~重构Room的获取规则 通one-shot迁移为Flow, 以及时响应最新的数据~~