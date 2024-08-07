# API列表项扩展支持

## 概述
支持扩展API列表项，实现一些个性化展示及操作。
根据扩展点：`io.github.newhoo.restkit.open.ep.RestItemDetailProvider`
## 源码说明

- RestItemDetailProvider
```java
/**
 * RestItemDetailProvider
 *
 * @author huzunrong
 * @since 3.0.0
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
 *
 * @author huzunrong
 * @since 3.0.0
 */
public interface RestItemDetail {

    /**
     * supported protocol
     */
    @NotNull
    String getProtocol();

    /**
     * tree list item icon
     */
    @NotNull
    Icon getIcon(@NotNull RestItem restItem);

    @NotNull
    default String getName(@NotNull RestItem restItem, boolean useApiDesc) {
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
}
```
## 使用示例

