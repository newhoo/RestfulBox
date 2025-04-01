# API Javadoc Rule

## api group name

1. 位于类文档注释中的自定义tag`@name`
2. 类的文档注释第一行，要求不为空
3. 类全限定名

```java
/**
 * 用户接口
 *
 * @name 接口分组名称，优先级最高
 */
public class UserController {
}
```

## api name

1. 位于方法文档注释中的自定义tag`@name`
2. 方法文档注释第一行，要求不为空
3. 方法名

```java
/**
 * 测试方法
 *
 * @name 接口名称，优先级最高
 */
public R<Map<String, User>> test();
```

## api description

1. 位于方法文档注释中的tag`@apiNote`
2. 方法文档注释描述部分，若第一行作为接口名称，则跳过第一行

```java
/**
 * 测试方法
 *
 * @apiNote 接口描述，优先级最高
 */
```

## api return type

_优先级从高到低_

1. 在方法文档注释中存在tag`@return`，且方法返回`java.lang.Object`类型或者约定使用`use`关键词，则会尝试解析 `@return` 的值作为类型，如下示例
2. 方法本身的返回类型

`@return`示例，如`R<PageRespVo<User>>`支持以下两种格式（Kotlin请使用**格式2**）：

- 格式1:

```java
/**
 * 方法返回`java.lang.Object`类型，从@return中解析返回类型
 *
 * @return {@link R<PageRespVo<User>>}
 */
public Object get(@PathVariable BigInteger docId);

/**
 * 方法不返回`java.lang.Object`类型，约定使用use，从@return中解析返回类型
 *
 * @return use {@link R<PageRespVo<User>>}
 */
public Map get(@PathVariable BigInteger docId);
```

- 格式2:

```java
/**
 * 方法返回`java.lang.Object`类型，从@return中解析返回类型
 *
 * @return com.example.bean.R#com.example.bean.PageRespVo#com.example.bean.User
 */
public Object get(@PathVariable BigInteger docId);

/**
 * 方法不返回`java.lang.Object`类型，约定使用use，从@return中解析返回类型
 *
 * @return use com.example.bean.R#com.example.bean.PageRespVo#com.example.bean.User
 */
public Map get(@PathVariable BigInteger docId);
```

## api tag

1. 位于类文档注释中的自定义tag`@tag`、`@version`、`@since`
2. 位于方法文档注释中的自定义tag`@tag`、`@version`、`@since`

```java
/**
 * 测试方法
 *
 * @tag v1, 自定义标记
 * @since 1.0.3
 */
```

## api、field ignore

如果类、方法、字段的文档注释存在`@ignore`时，则忽略对应的接口、字段。如：

```java
/**
 * 忽略类、方法、字段、递归字段等
 *
 * @ignore
 */
```