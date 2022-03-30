# CHANGELOG

## 2.0.4

- Optimize details.


- 优化细节

## 2.0.3

- support api group by file name.
- support rest client ep.
- support param filter for springmvc


- 扫描的API支持按文件名显示分组
- 发送请求提供扩展点，即将提供Dubbo服务支持.
- springmvc接口扫描支持param参数过滤

## 2.0.2

- more powerful api local store, support synchronization in different IDE.
- fix bugs.


- API本地存储升级，支持跨IDE同步


## 2.0.1

- support more jetbrains ides.
- support api local storage, export and import by default.
- most particular optimization.


- 支持更多的jetbrains产品
- 默认支持API本地存储/导出/导入
- 大量细节优化


## 2.0.0

- add extension for scanning restful services from other web framework.
- support some common config.
- support pre-request and post-request script.


- 提供扩展点，支持自定义web框架中的接口扫描与展示
- 增加一些通用配置
- 支持请求前置/后置脚本


## 1.0.8

- Integrate URL search into native Search Everywhere for idea 211.*.
- support scan library services.
- support environment migration.
- support restfull client parameter operation(Headers/Params/Body).


- 在Search Everywhere中集成URL搜索
- 支持扫描依赖包的接口
- 环境支持导出导入，一键迁移
- 支持restfull client参数操作(Headers/Params/Body)


## 1.0.7

- Integrate URL search into native Search Everywhere for idea 203.*.


- 在Search Everywhere中集成URL搜索 (idea 203.*)


## 1.0.6

- Integrate URL search into native Search Everywhere.


- 在Search Everywhere中集成URL搜索


## 1.0.5

- fix bugs.


## 1.0.4

- fix bugs.


## 1.0.3

- show http request info.
- update custom logging format.
- fix known issues and support 2021.2.


- 显示HTTP请求信息
- 更新日志打印格式
- 修复已知问题，兼容2021.2


## 1.0.2

- fix compatibility problems with 2020.


- 修复2020版本兼容性问题。


## 1.0.1

- print request logs to file (path: $PROJECT_PATH$/.idea/restkit/*.log);
- parse header from springmvc annotation.
- support copying current Environment(can use to rename if you want).


- 请求日志输出到文件（path: $PROJECT_PATH$/.idea/restkit/*.log）;
- 解析header (已验证java语言);
- 支持复制当前环境变量（可间接重命名）。


## 1.0.0

- support jump to api in tree window from Controller method ( use: ⌥ + ↵) ;
- more useful http client: supporting global request header/environment and request script, delete requests support body, param/body mocking more powerful, etc.
- fix bugs, remove useless function.


- 支持从方法跳转到tree窗口中对应的接口（在方法上按⌥ + ↵，如不存在需先刷新接口）;
- http工具更好用，支持全局请求头、环境变量和请求脚本，delete支持body参数，mock参数识别增强等等;
- 修复兼容性问题，去除了用处不大的功能（右键copy full url等）。

