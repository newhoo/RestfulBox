# Rust 扫描接口配置

## 一、概述
目前支持 Actix-Web 接口扫描

## 二、接口信息

用于定义如何识别接口分组、名称、描述、参数中涉及成员字段的名称及描述

### 接口分组

1. 接口所在文件名，不包括扩展名

### 接口名称

_优先级从高到低_

1. 方法文档注释第一行，要求不为空
2. 方法名

- 示例

```rust
/// 删除用户
///
/// # Arguments
///
/// *  `user_id`  - 用户ID
///
/// # Returns
/// [`Option<Resp>`]
#[delete("/{user_id}")]
pub async fn delete_user()
```

### 接口描述

_优先级从高到低_

1. 方法文档注释描述部分
2. 留空

- 示例

```rust
/// 删除用户
/// 建议调用批量删除接口
///
/// # Arguments
///
/// *  `user_id`  - 用户ID
///
/// # Returns
/// [`Option<Resp>`]
#[delete("/{user_id}")]
pub async fn delete_user()
```

### 成员字段名称

_优先级从高到低_

1. 字段本身名称

### 成员字段描述

_优先级从高到低_

1. 方法文档注释第一行，要求不为空
2. 留空

### 接口标签

暂不支持

## 三、过滤规则

用于定义如何过滤接口、参数中的成员字段

### 过滤接口

_优先级从高到低_

1. 方法的文档注释存在`@ignore`时，忽略对应的接口

- 示例1

```rust
/// 过滤接口
///
/// @ignore
/// 
#[delete("/{user_id}")]
pub async fn delete_user()
```

### 过滤参数中的成员字段

_优先级从高到低_

1. 成员字段的文档注释存在`@ignore`时, 忽略解析

- 示例

```rust
pub struct UserData {
    /// 用户ID
    /// @ignore
    pub user_id: String,
    pub username: String,
}
```

## 四、类型规则

### 接口入参

当前仅支持参数本身的类型

### 接口返回值

1. 在方法文档注释中存在 `# Returns` 段落时，则会尝试解析其中的链接作为类型，实际类型需使用`Option`包裹一层，如下示例
2. 方法本身的返回类型

- 示例 格式:

```rust
/// 获取用户
///
/// # Returns
/// [`Option<Resp<UserListVo>>`]
#[get("/")]
pub async fn query_user()
```

### 自定义类型及字段信息

暂不支持

### 定义文件参数

一般扫描框架内部会自动识别常用的文件类型，暂不支持自定义配置。

## 五、替换规则

暂不支持