# HTTP上传/下载文件

## 一、概述
从`2.0.8`开始支持，不建议在idea中下载大文件。

## 二、上传文件
默认支持`org.springframework.web.multipart.MultipartFile`参数识别，可在设置中配置更多。

在 **Params编辑窗口** 中，文件参数表现为：`paramName: @filepath`。(`5.4.0`以前版本为`paramName: file@[filepath]`)

- 通过自动补全上传

![img.png](images/1727200568948.png)

- 选择参数上传，步骤如下图：

![img.png](images/1724578236844.png)

同其他http请求一样，点击发送即可。在接口断点查看参数，如图：

![](images/179465814254137.png)

## 三、下载文件
设置下载目录：RESTKit -> HTTP -> Download directory
根据响应头中的`Content-Disposition`或`Content-type: application/octet-stream`识别为下载文件，如：
```
Content-Disposition: attachment; filename="2d8e6de174899729ccd12f41230a5510.webp"; filename*=utf-8''2d8e6de174899729ccd12f41230a5510.webp
```
下载完成后在**Body编辑器**中会显示下载文件的绝对路径，可点击上方下载按钮在文件管理器中打开文件。
