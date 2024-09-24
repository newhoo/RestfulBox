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

若方法返回`java.lang.Object`类型，则会尝试从文档注释中的`@return`判断返回值的实际类型。如`R<PageRespVo<User>>`支持以下两种格式：

- 格式1:

```java
/**
 * 测试返回参数
 *
 * @return {@link R<PageRespVo<User>>}
 */
```

- 格式2:

```java
/**
 * 测试返回参数
 *
 * @return com.example.bean.R#com.example.bean.PageRespVo#com.example.bean.User
 */
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