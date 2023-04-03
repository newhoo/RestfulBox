<a href="https://plugins.jetbrains.com/plugin/14723-restkit">![bg](doc/en/images/bg2.png)</a>

## a powerful toolkit for restful services development.

[简体中文](./README.zh_CN.md) | [Github](https://github.com/newhoo/RESTKit) | [Gitee](https://gitee.com/newhoo/RESTKit) | [Jetbrains](https://plugins.jetbrains.com/plugin/14723-restkit/reviews)

This plugin is committed to enhancing development efficiency with useful features. From the initial RestfulToolkit and joined common functions of Postman, rich and complete features can greatly improve the efficiency of IDEA development. Free to share with everyone, not for commercial purposes. If you find any problems, please give me feedback. Former name: RESTKit.

## Features ([3.0 Upgrade Guide](doc/zh_CN/快速入门/3.0升级指南.md))
- More supported jetbrains ides.
- Restful service automatic scanning and display.
  - Support searching service in native Search Everywhere. ( use: <kbd>Ctrl \\</kbd> or <kbd>Ctrl Alt N</kbd> )
  - Show restful service structure in tool window.
  - Service and Method jump to each other.
- Powerful request client：
  - Custom parameter format, support placeholder variable, formatted JSON.
  - Environment variable：define/manage/use/export/import, support preset function and script function.
  - Global header：can use with Environment, also support preset function and script function.
  - Parameter library：support display/save/delete in Headers/Params/Body tab.
  - Multi-protocol: supported multiple protocol.
  - Request script：support pre-request and post-script script.
  - Request info display: such as HTTP packet.
- Request log：save multi-protocol request log such as HTTP packet format.
- Plugin extension：through this, you can try your great ideas.
- Language & Framework：
  - Support api local store and Sqlite datasource by default.
  - Support Spring MVC / SpringBoot with java and kotlin in idea by default.


## Ecology

- [x] Spring MVC: supported by default. Support Java and Kotlin implement.
- [x] Jax-Rs: supported by plugin. See [RESTKit-JAX-RS](https://github.com/newhoo/RESTKit-JAX-RS)
- [x] Dubbo: supported by plugin. See [RESTKit-Dubbo](https://github.com/newhoo/RESTKit-Dubbo). Support scanning and sending request.
- [x] Redis: supported by plugin. See [RESTKit-Redis](https://github.com/newhoo/RESTKit-Redis). Support store api to redis and sending simple redis command.
- [x] Local Store: supported by default. Support store api to local file.
- [x] Sqlite Datasource: supported by default. Support store all data to sqlite.

## Document
- [Document-Github](doc/en/README.md)
- [中文文档-Github](doc/zh_CN/目录.md)
- [中文文档-语雀](https://www.yuque.com/newhoo/restkit)

## Contact & Feedback
[Issues](https://github.com/newhoo/RESTKit/issues) | [Email](mailto:huzunrong@foxmail.com) | [Jetbrains Previews](https://plugins.jetbrains.com/plugin/14723-restkit/reviews)

> Note  
> Please provide necessary information when you feedback: IDEA version, plugin version, exception content, recreation way(if can), desire, and etc.


## Sponsor
If this plugin helps, you can take me a cup of coffee as you want. Thanks!

![pay](doc/en/images/pay.png)