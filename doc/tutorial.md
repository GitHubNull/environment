# Environment 插件使用教程

本教程由简入繁，逐步介绍 Environment 插件的使用方法，适合从初学者到高级用户的各个阶段。

---

## 目录

1. [第一章：快速上手](#第一章快速上手)
2. [第二章：参数管理基础](#第二章参数管理基础)
3. [第三章：自动更新类型详解](#第三章自动更新类型详解)
4. [第四章：在 HTTP 请求中使用变量](#第四章在-http-请求中使用变量)
5. [第五章：YAML 导入与导出](#第五章yaml-导入与导出)
6. [第六章：Groovy 脚本高级扩展](#第六章groovy-脚本高级扩展)
7. [第七章：常见问题与排错](#第七章常见问题与排错)

---

## 第一章：快速上手

### 1.1 什么是 Environment？

Environment 是一个 Burp Suite 插件，它可以在你发送 HTTP 请求之前，自动将请求中的特殊标记（如 `{{user_id}}`）替换为动态生成的值（如随机数、UUID、时间戳等）。

### 1.2 安装插件

1. 下载或编译得到 `environment-1.0_yyyyMMdd_HHmm.jar` 文件。
2. 打开 Burp Suite，点击菜单栏 **Extensions → Installed → Add**。
3. Extension type 选择 **Java**。
4. 点击 **Select file**，选择下载的 JAR 文件。
5. 点击 **Next**，插件安装成功后会自动显示 **environment** 标签页。

### 1.3 第一个示例：使用时间戳

让我们创建一个自动更新的时间戳参数：

1. 在 **environment** 标签页中，点击 **Add** 按钮。
2. 在弹出的对话框中填写：
   - **arg name**：`timestamp`
   - **arg type**：`ALL`
   - **auto update type**：`TIMESTAMP`
   - **description**：`当前时间戳`
3. 点击 **OK**。

现在，在任何 HTTP 请求中写入 `{{timestamp}}`，发送时它会被自动替换为当前毫秒时间戳。

---

## 第二章：参数管理基础

### 2.1 参数的核心属性

每个参数包含以下属性：

| 属性 | 说明 |
|------|------|
| arg name | 参数名称，用于在请求中标记，如 `{{user_id}}` |
| arg type | 参数类型：TEXT（文本）、NUMBER（数字）、ALL（通用） |
| auto update type | 自动更新方式，决定值如何生成 |
| arg length | 长度限制（部分类型需要） |
| defaultValue | 默认值（自增数时作为起始值） |
| arg value | 当前值 |
| code path | Groovy 脚本路径（仅 Groovy_CODE 类型） |
| description | 描述信息 |
| enabled | 是否启用该参数 |
| persistent | 是否保存到数据库（重启后保留） |

### 2.2 添加参数

点击 **Add** 按钮，按提示填写信息。参数名称只能包含字母、数字和下划线，且必须以字母或下划线开头。

### 2.3 编辑参数

- 单击选中表格中的某一行，点击 **Edit** 按钮。
- 或者直接**双击**某一行，即可打开查看/编辑对话框。

### 2.4 删除参数

- 选中一行或多行（按住 Ctrl 可多选），点击 **Remove** 按钮，确认后删除。
- 点击 **Clear** 按钮可清空所有参数（谨慎操作）。

### 2.5 搜索参数

在搜索框中输入关键字，点击 **Query** 按钮，表格会按参数名进行正则匹配筛选。

### 2.6 调整顺序

选中一行，点击 **Move Up** 或 **Move Down** 调整参数在列表中的顺序。

---

## 第三章：自动更新类型详解

### 3.1 类型总览

| 类型 | 说明 | 需要长度 | 需要默认值 |
|------|------|---------|-----------|
| NONE | 不自动更新，保持当前值 | 否 | 否 |
| UUID | 生成随机 UUID | 否 | 否 |
| TIMESTAMP | 当前时间戳（毫秒） | 否 | 否 |
| SHA1_OF_TIMESTAMP | 当前时间戳的 SHA1 哈希 | 否 | 否 |
| RANDOM_NUMBER | 指定长度的随机数字 | 是 | 否 |
| RANDOM_TEXT | 指定长度的随机小写字母 | 是 | 否 |
| INCREMENT_NUMBER | 自增数 | 否 | 是 |
| Groovy_CODE | 通过 Groovy 脚本自定义 | 否 | 否 |

### 3.2 UUID 示例

适合生成会话标识、请求 ID 等：

- **arg name**：`session_id`
- **auto update type**：`UUID`
- 每次请求都会生成一个新的 UUID，如 `550e8400-e29b-41d4-a716-446655440000`

### 3.3 随机数示例

适合生成验证码、随机 ID 等：

- **arg name**：`verify_code`
- **auto update type**：`RANDOM_NUMBER`
- **arg length**：`6`
- 每次生成 6 位随机数字，如 `837492`

### 3.4 随机文本示例

适合生成随机字符串：

- **arg name**：`random_str`
- **auto update type**：`RANDOM_TEXT`
- **arg length**：`10`
- 每次生成 10 位随机小写字母，如 `abcxyzdefg`

### 3.5 自增数示例

适合需要顺序编号的场景：

- **arg name**：`order_id`
- **auto update type**：`INCREMENT_NUMBER`
- **defaultValue**：`1000`
- 第一次请求值为 `1001`，第二次为 `1002`，以此类推

### 3.6 SHA1 时间戳示例

适合需要签名或防重放攻击的场景：

- **arg name**：`signature`
- **auto update type**：`SHA1_OF_TIMESTAMP`
- 生成当前时间戳的 SHA1 哈希值

---

## 第四章：在 HTTP 请求中使用变量

### 4.1 基本语法

在 HTTP 请求的任何位置使用 `{{参数名}}` 标记变量：

```http
GET /api/users/{{user_id}}/orders/{{order_id}}?token={{session_token}} HTTP/1.1
Host: api.example.com
Authorization: Bearer {{auth_token}}
X-Request-ID: {{request_uuid}}
Content-Type: application/json

{
    "timestamp": {{timestamp}},
    "nonce": "{{random_str}}"
}
```

### 4.2 URL 路径中的变量

```http
GET /api/v1/users/{{user_id}}/profile HTTP/1.1
```

插件会自动将 `{{user_id}}` 替换为对应参数的当前值。

### 4.3 URL 查询参数中的变量

```http
GET /api/search?q={{keyword}}&page={{page_num}}&token={{session_token}} HTTP/1.1
```

### 4.4 请求头中的变量

```http
GET /api/data HTTP/1.1
Host: example.com
X-API-Key: {{api_key}}
X-Timestamp: {{timestamp}}
X-Nonce: {{nonce}}
```

### 4.5 请求体中的变量

**JSON 格式：**

```http
POST /api/create HTTP/1.1
Content-Type: application/json

{
    "user_id": "{{user_id}}",
    "request_id": "{{request_uuid}}",
    "created_at": {{timestamp}}
}
```

**Form 格式：**

```http
POST /api/login HTTP/1.1
Content-Type: application/x-www-form-urlencoded

username={{username}}&password={{password}}&nonce={{nonce}}
```

### 4.6 注意事项

- 参数名必须严格匹配，区分大小写。
- 如果参数被禁用（enabled=false），标记不会被替换。
- 如果参数不存在，标记将保持原样。

---

## 第五章：YAML 导入与导出

### 5.1 导出参数配置

1. 点击 **Export** 按钮。
2. 选择保存位置，默认文件名为 `environment_export.yaml`。
3. 点击保存，所有参数将被导出。

导出的 YAML 示例：

```yaml
version: "1.0"
exported_at: "2024-01-15T10:30:00+08:00"
args:
  - id: 1
    name: "timestamp"
    type: "ALL"
    auto_update_type: "TIMESTAMP"
    length: 0
    default_value: ""
    value: ""
    code_path: ""
    enabled: true
    description: "当前时间戳"
    persistent: true
  - id: 2
    name: "user_id"
    type: "NUMBER"
    auto_update_type: "INCREMENT_NUMBER"
    length: 0
    default_value: "1000"
    value: "1000"
    code_path: ""
    enabled: true
    description: "用户ID"
    persistent: true
```

### 5.2 导入参数配置

1. 点击 **Import** 按钮。
2. 选择 YAML 文件（支持 `.yaml` 和 `.yml`）。
3. 插件会自动导入参数，重复名称的参数将被跳过。
4. 导入完成后会显示成功导入和跳过的数量。

### 5.3 使用场景

- **团队协作**：将配置导出分享给团队成员。
- **环境迁移**：在不同机器或 Burp 实例间迁移配置。
- **备份恢复**：定期导出作为备份。

---

## 第六章：Groovy 脚本高级扩展

### 6.1 什么是 Groovy 扩展？

当内置的自动更新类型无法满足需求时，你可以编写 Groovy 脚本来自定义参数值的生成逻辑。

### 6.2 脚本规范

脚本必须包含一个名为 `modify` 的方法，接收一个 `Map<String, String>` 参数：

```groovy
def modify(Map<String, String> params) {
    // params 的 key 是参数名，value 是当前值
    def name = params.keySet().iterator().next()
    def value = params.get(name)
    // 返回新的值
    return "your_custom_value"
}
```

### 6.3 示例一：带前缀的时间戳

```groovy
def modify(Map<String, String> params) {
    def name = params.keySet().iterator().next()
    def value = params.get(name)
    return "REQ_" + System.currentTimeMillis()
}
```

### 6.4 示例二：基于当前值的变换

```groovy
def modify(Map<String, String> params) {
    def name = params.keySet().iterator().next()
    def value = params.get(name)
    // 将值反转并加上后缀
    return value.reverse() + "_modified"
}
```

### 6.5 示例三：生成特定格式的订单号

```groovy
def modify(Map<String, String> params) {
    def date = new Date().format("yyyyMMdd")
    def random = (new Random().nextInt(9000) + 1000).toString()
    return "ORD-${date}-${random}"
}
```

### 6.6 使用 Groovy 参数

1. 创建一个 `.groovy` 文件，写入上述脚本内容。
2. 在插件中点击 **Add**。
3. **auto update type** 选择 `Groovy_CODE`。
4. 点击 **code path** 旁边的 `...` 按钮，选择你的 `.groovy` 文件。
5. 点击 **OK** 完成添加。

### 6.7 调试技巧

- 脚本执行错误会输出到 Burp 的 Extensions 日志中。
- 可以先在 Groovy 控制台测试脚本逻辑。
- 确保脚本文件编码为 UTF-8。

---

## 第七章：常见问题与排错

### Q1：插件安装后没有显示 environment 标签页？

**排查步骤：**

1. 打开 Burp 的 **Extensions → Installed**，确认插件已加载。
2. 查看 **Output** 子标签，检查是否有错误日志。
3. 确认使用的 JAR 文件是带依赖的 fat JAR（通过 `mvn clean package` 生成）。
4. 确认 Burp Suite 的 Java 版本不低于 17。

### Q2：变量没有被替换？

**排查步骤：**

1. 确认参数已启用（表格中 enabled 为 true）。
2. 确认请求中使用了正确的标记语法 `{{参数名}}`。
3. 确认参数名大小写完全匹配。
4. 查看 Burp 的 Extensions 输出日志，确认 HTTP 处理器已触发。

### Q3：自增数没有递增？

**排查步骤：**

1. 确认 **auto update type** 为 `INCREMENT_NUMBER`。
2. 确认 **defaultValue** 已设置有效的数字。
3. 每次请求后参数值会自动更新，可在表格中查看当前值。

### Q4：Groovy 脚本执行报错？

**排查步骤：**

1. 确认脚本中包含 `modify(Map<String, String>)` 方法。
2. 确认方法返回值为 `String` 类型。
3. 确认脚本文件路径正确且文件存在。
4. 查看 Burp 的 Extensions 错误日志获取详细信息。

### Q5：参数重启后丢失？

**排查步骤：**

1. 确认添加参数时 **persistent** 复选框已勾选。
2. 确认 Burp 运行目录有写入权限（需要创建 `environment/environment.db`）。
3. 检查 Extensions 日志中是否有数据库初始化失败的错误。

### Q6：导入 YAML 失败？

**排查步骤：**

1. 确认 YAML 文件格式正确。
2. 确认文件编码为 UTF-8。
3. 检查 Extensions 日志中的具体错误信息。

---

## 附录：快速参考卡

### 标记语法

```
{{parameterName}}
```

### 常用自动更新类型速查

| 场景 | 推荐类型 |
|------|---------|
| 会话 ID | UUID |
| 时间戳 | TIMESTAMP |
| 签名/防重放 | SHA1_OF_TIMESTAMP |
| 验证码 | RANDOM_NUMBER |
| 随机字符串 | RANDOM_TEXT |
| 顺序编号 | INCREMENT_NUMBER |
| 自定义逻辑 | Groovy_CODE |

### 快捷键与操作

| 操作 | 方式 |
|------|------|
| 添加参数 | 点击 Add |
| 编辑参数 | 选中行 + Edit，或双击行 |
| 删除参数 | 选中行 + Remove |
| 搜索参数 | 输入关键字 + Query |
| 调整顺序 | 选中行 + Move Up / Move Down |

---

**文档版本**：1.0  
**最后更新**：2024年
