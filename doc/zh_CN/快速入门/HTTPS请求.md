# HTTPS请求

## 一、概述

https请求分为单向认证和双向认证。从`2.0.8`开始支持。
本插件对于单向认证，会信任所有来源的服务端证书，即不做校验，相当于 **curl** 中的 `-k`；对于双向认证，同样会信任所有来源的服务端证书，同时需要配置本地客户端证书。

## 二、单向认证
`baseUrl`中以 `https://` 开头，无需其他配置。

#### 【Copy as cURL】示例
```bash
curl -X GET https://www.yourdomain.com/server-api/health -k
```

## 二、双向认证
`baseUrl`中以`https://`开头，其他配置如下。

#### 证书全局管理
如图。双击单元格编辑内容；选中【PFX file】单元格，右键双击可选择文件。

![](images/553845514232195.png)

#### 【Copy as cURL】示例
```bash
curl -X GET https://www.yourdomain.com/server-api/health -k --cert-type P12 --cert /data/cert/client.p12:123456
```