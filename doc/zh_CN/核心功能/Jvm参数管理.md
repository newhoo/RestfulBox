# Jvm参数管理

## 概述

方便管理Jvm运行参数，启动应用时，启用的参数将被自动设置到jvm启动参数中。

## 功能

- 项目级参数管理
- 全局(跨项目)参数管理

## 配置

在设置中添加 jvm 参数。

![img.png](images/1717509596336.png)

- 预览：预览启用的参数（鼠标Hover时可换行查看）
- 参数表格说明

| 列名     | 描述                       |
|--------|--------------------------|
| NAME   | 参数key，不可为空               |
| VALUE  | 参数value，为空时，生成的内容为NAME内容 |
| ENABLE | 是否启用                     |
| GLOBAL | 是否为全局参数                  |

## 其他

- 依赖Java插件，如IDEA/Android Studio
- 源自插件 <a href="https://plugins.jetbrains.com/plugin/13204-jvm-parameter">Jvm Parameter</a>，如果已经安装，需卸载，数据兼容。