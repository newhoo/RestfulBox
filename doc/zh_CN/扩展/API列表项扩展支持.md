# API列表项扩展支持

## 概述

支持扩展API列表项，实现一些个性化展示及操作。
根据扩展点：`io.github.newhoo.restkit.open.ep.RestItemDetailProvider`

## 源码说明

- RestItemDetailProvider

```java
/**
 * RestItemDetailProvider
 */
public interface RestItemDetailProvider {

    ExtensionPointName<RestItemDetailProvider> EP_NAME = ExtensionPointName.create("io.github.newhoo.restkit.restItemDetail");

    RestItemDetail createRestItemDetail();
}
```

- RestItemDetail

```java
/**
 * rest item detail extension point
 */
public interface RestItemDetail {

    /**
     * supported protocol
     */
    @NotNull
    String getProtocol();

    /**
     * tree node icon
     */
    @NotNull
    Icon getIcon(@NotNull RestItem restItem);

    /**
     * get default file type for client body tab when create new client
     *
     * @return null to use inner default: json
     */
    @NotNull
    default Optional<FileType> getClientDefaultBodyFileType() {
        return Optional.empty();
    }

    @NotNull
    default String getClientToolTip() {
        return "";
    }

    /**
     * DoubleClickOrEnter
     *
     * @param restItem
     * @param jumpToSourceFirstly 优先跳转到源码
     * @return true - 不再执行原有方法  false - 继续执行原有逻辑
     */
    default boolean handleDoubleClickOrEnter(@NotNull RestItem restItem, boolean jumpToSourceFirstly, @NotNull Project project) {
        return false;
    }

    /**
     * 跳转到源码
     */
    default void navigateToSource(@NotNull RestItem restItem, @NotNull Project project) {
    }
}
```

## 使用示例

以支持Dubbo请求示例，完整代码查阅：[RestfulBox-Dubbo](https://github.com/newhoo/RESTKit-Dubbo)

**1、实现 RestItemDetailProvider**

```java
public static class DubboRestItemDetailProvider implements RestItemDetailProvider {
    @Override
    public @NotNull DubboRestItemDetail createRestItemDetail() {
        return new DubboRestItemDetail();
    }
}
```

**2、实现 RestItemDetail，核心逻辑**

```java
public class DubboRestItemDetail implements RestItemDetail {
    @NotNull
    @Override
    public String getProtocol() {
        return "dubbo";
    }

    @Override
    public @NotNull Icon getIcon(@NotNull RestItem restItem) {
        return DUBBO_API_ICON;
    }
}
```

**3、注册扩展实现，plugin.xml**

```xml

<idea-plugin>
    ...
    <depends>io.github.newhoo.restkit</depends>

    <extensions defaultExtensionNs="io.github.newhoo.restkit">
        <!-- your restful resolver implementation -->
        <restClient implementation="io.github.newhoo.restkit.ext.dubbo.DubboClient$DubboClientProvider"/>
        <restItemDetail implementation="io.github.newhoo.restkit.ext.dubbo.DubboRestItemDetail$DubboRestItemDetailProvider"/>
    </extensions>
</idea-plugin>
```
