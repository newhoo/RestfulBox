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
     * 协议排序
     */
    default int getProtocolOrder() {
        return 999;
    }

    /**
     * tree node icon
     */
    @NotNull
    Icon getIcon(@NotNull RestItem restItem);

    /**
     * tree node name
     *
     * @param restItem
     * @param useApiName see in the config
     * @return not null or keep empty to use default logic
     */
    @NotNull
    default String getName(@NotNull RestItem restItem, boolean useApiName) {
        return "";
    }

    /**
     * DoubleClickOrEnter
     *
     * @param restItem
     * @return true - 不再执行原有方法  false - 继续执行原有逻辑
     */
    default boolean handleDoubleClickOrEnter(@NotNull RestItem restItem, @NotNull Project project) {
        return false;
    }

    /**
     * Jump to source
     */
    default void navigateToSource(@NotNull RestItem restItem, @NotNull Project project) {
    }
}
```
## 使用示例

