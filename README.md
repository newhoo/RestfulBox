<a href="https://plugins.jetbrains.com/plugin/14723-restkit">![bg](doc/en/images/bg2.png)</a>

## Powerful toolkit for restful services development.

[简体中文](./README.zh_CN.md) | [Github](https://github.com/newhoo/RESTKit) | [Gitee](https://gitee.com/newhoo/RESTKit) | [Jetbrains](https://plugins.jetbrains.com/plugin/14723-restkit/reviews)

This plugin is committed to enhancing development efficiency with useful features. From the initial RestfulToolkit and joined common functions of Postman, rich and complete features can greatly improve the efficiency of IDEA development. Former name: RESTKit.

## Features ([3.0 Upgrade Guide](doc/zh_CN/快速入门/3.0升级指南.md))
- [x] More supported jetbrains ides.
- [x] Restful service automatic scanning and display.
  - Support searching service in native Search Everywhere. ( use: <kbd>Ctrl \\</kbd> or <kbd>Ctrl Alt N</kbd> )
  - Show restful service structure in tool window.
  - View services in different IDE and project.
  - Service and Method jump to each other.
- [x] Powerful request client：
  - Multi-protocol: support http by default.
  - Custom parameter format, support placeholder variable, formatted JSON.
  - Environment variable：define/manage/use/export/import, support preset function and script function.
  - Global header：can use with Environment, also support preset function and script function.
  - Parameter library：support display/save/delete in Headers/Params/Body tab.
  - Request script：support pre-request and post-script script.
  - Display request info like http packet, and save to log file.
- [x] Language & Framework：
  - Support services using local store by default.
  - Support Spring MVC / SpringBoot with java and kotlin in idea by default.
- [x] Datasource: Support multiple data sources. Local/Cloud/Custom.
- [x] Plugin extension: through this, you can try your great ideas.


## Ecology

- Scanning Type
  - [x] Spring MVC: supported by default. Support Java and Kotlin implement.
  - [x] Jax-Rs: supported by plugin. See [RESTKit-JAX-RS](https://github.com/newhoo/RESTKit-JAX-RS)
  - [x] Dubbo: supported by plugin. See [RESTKit-Dubbo](https://github.com/newhoo/RESTKit-Dubbo). Support scanning and sending dubbo request.
  - [x] Solon: supported by plugin. See [RestfulBox-Solon](https://github.com/newhoo/RestfulBox-Solon). Support scanning and sending http request.
- Storage Type (Suggest using datasource instead)
  - [x] Redis: supported by plugin. See [RESTKit-Redis](https://github.com/newhoo/RESTKit-Redis). Support store services to redis and sending simple redis command.
  - [x] Local Store: supported by default. Support store services to local file.
- Protocol
  - [x] HTTP/HTTPS: supported by default.
  - [x] DUBBO: see above.
  - [x] Redis: see above.
- Datasource (Support store all data to selected datasource)
  - [x] Sqlite Datasource: supported by default from 3.0.0.
  - [x] MySQL Datasource: supported by default from 3.0.1.

## Document

**_Due to limited time, Chinese documents are more comprehensive. Please visit as needed ~_**

- [Document-Github](doc/en/README.md)
- [中文文档-Github](doc/zh_CN/目录.md)  [Gitee](https://gitee.com/newhoo/RESTKit#%E4%BD%BF%E7%94%A8%E6%96%87%E6%A1%A3)
- [CHANGELOG](doc/CHANGELOG.md)

## Contact & Feedback
If you find any problems with this plugin, please give me feedback. If it helps, star for it! :star2:

:fire: **_If you want to experience the subscription feature more fully, i will provide 3 months of free use at a time. If you need, provide your [jetbrains account](https://account.jetbrains.com/profile-details) to me by email!_**

[Issues](https://github.com/newhoo/RESTKit/issues) | [Email](mailto:huzunrong@foxmail.com) | [Ratings & Previews](https://plugins.jetbrains.com/plugin/14723-restkit/reviews)

> Note  
> Please provide necessary information when you feedback: IDEA version, plugin version, exception content, recreation way(if can), desire, and etc.


## Sponsor
You can take me a cup of coffee as you want. Thanks!

![pay](doc/en/images/pay.png)