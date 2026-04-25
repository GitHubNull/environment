# Environment

[![Java Version](https://img.shields.io/badge/Java-17-blue)](https://openjdk.org/)
[![Maven Central](https://img.shields.io/maven-central/v/net.portswigger.burp.extensions/montoya-api)](https://mvnrepository.com/artifact/net.portswigger.burp.extensions/montoya-api)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**Environment** 是一款 Burp Suite 插件，用于自动修改 HTTP 请求报文中的用户定义变量（使用 `{{variableName}}` 标记）。它支持多种自动更新类型（UUID、时间戳、随机数、自增数、Groovy 脚本等），适用于渗透测试、API 自动化测试等场景。

[English Version](README_EN.md)

---

## 功能特性

- **参数管理**：支持添加、编辑、删除、查看参数；提供表格视图，支持多字段排序和关键字搜索。
- **自动更新机制**：支持 UUID、时间戳、SHA1 时间戳、随机数、随机文本、自增数、Groovy 脚本等多种自动更新类型。
- **灵活的参数配置**：支持文本、数字、通用三种参数类型；可设置默认值、长度、描述；支持启用/禁用和持久化开关。
- **请求全链路处理**：自动处理 HTTP 请求的 URL 路径、查询参数、请求体中的变量标记。
- **数据持久化**：使用 SQLite 本地存储参数配置，重启后自动恢复。
- **YAML 导入/导出**：支持将参数配置导出为 YAML 文件，或从 YAML 文件导入。
- **Groovy 脚本扩展**：支持通过外部 Groovy 脚本实现自定义参数生成逻辑。
- **用户友好的界面**：基于 Swing 的图形化界面，支持双击查看、上下移动排序。

---

## 技术栈

- **编程语言**：Java 17
- **构建工具**：Maven
- **核心依赖**：
  - [Montoya API](https://portswigger.net/burp/extender/api)：Burp Suite 扩展开发接口
  - [Groovy](https://groovy-lang.org/)：动态脚本执行
  - [SQLite JDBC](https://github.com/xerial/sqlite-jdbc)：本地数据持久化
  - [SnakeYAML](https://bitbucket.org/snakeyaml/snakeyaml)：YAML 导入/导出
  - [Apache Commons IO](https://commons.apache.org/proper/commons-io/) / [Codec](https://commons.apache.org/proper/commons-codec/)：文件与编码工具

---

## 安装与运行

### 环境要求

- JDK 17 或更高版本
- Maven 3.x
- Burp Suite Professional / Community Edition

### 构建项目

1. 克隆项目代码：
   ```bash
   git clone https://github.com/oxff/burp-environment.git
   cd environment
   ```

2. 使用 Maven 构建：
   ```bash
   mvn clean package
   ```

3. 构建完成后，带依赖的 JAR 文件位于 `target` 目录下，文件名格式为 `environment-0.1.13_yyyyMMdd_HHmm.jar`（同时会生成不带时间戳的 `environment-0.1.13.jar`）。

### 安装到 Burp Suite

1. 打开 Burp Suite，进入 **Extensions → Installed → Add**。
2. 选择 **Extension type** 为 **Java**。
3. 点击 **Select file**，选择 `target` 目录下生成的 JAR 文件。
4. 点击 **Next** 完成安装，插件将自动加载并显示 **environment** 标签页。

---

## 使用说明

### 添加参数

1. 点击 **Add** 按钮。
2. 在弹出的对话框中填写：
   - **arg name**：参数名称（仅允许字母、数字、下划线，且必须以字母或下划线开头）。
   - **arg type**：参数类型（TEXT / NUMBER / ALL）。
   - **auto update type**：自动更新类型（UUID、TIMESTAMP、RANDOM_NUMBER 等）。
   - **arg length**：长度（随机数/随机文本时有效）。
   - **defaultValue**：默认值（自增数时作为起始值）。
   - **arg value**：当前值。
   - **code path**：Groovy 脚本路径（仅 Groovy_CODE 类型时有效）。
   - **description**：描述信息。
   - **enabled**：是否启用。
   - **persistent**：是否持久化到数据库。
3. 点击 **OK** 完成添加。

### 编辑参数

1. 在表格中选中需要编辑的参数（单击行）。
2. 点击 **Edit** 按钮，或**双击**该行。
3. 修改信息后点击 **OK**。

### 删除参数

1. 在表格中选中需要删除的参数（支持多选）。
2. 点击 **Remove** 按钮，确认删除。

### 搜索参数

在搜索框中输入关键字，点击 **Query** 按钮即可按参数名正则筛选。

### 移动参数

选中参数后，点击 **Move Up** 或 **Move Down** 调整顺序。

### 导入/导出

- **Import**：点击 **Import**，选择 YAML 文件，自动导入参数（重复名称将跳过）。
- **Export**：点击 **Export**，选择保存路径，将所有参数导出为 YAML 文件。

---

## 变量标记语法

在 HTTP 请求中，使用 `{{variableName}}` 标记需要自动替换的变量：

```http
GET /api/user/{{user_id}}?token={{session_token}} HTTP/1.1
Host: example.com
X-Timestamp: {{timestamp}}

{"request_id": "{{request_uuid}}"}
```

插件会在请求发送前自动将标记替换为对应参数的最新值。

---

## 自动更新类型说明

| 类型 | 说明 | 是否需要长度 | 示例 |
|------|------|------------|------|
| NONE | 不自动更新 | 否 | - |
| UUID | 随机 UUID | 否 | `550e8400-e29b-41d4-a716-446655440000` |
| TIMESTAMP | 当前时间戳（毫秒） | 否 | `1714032000000` |
| SHA1_OF_TIMESTAMP | 当前时间戳的 SHA1 值 | 否 | `a9993e36...` |
| RANDOM_NUMBER | 指定长度的随机数字 | 是 | `83749201` |
| RANDOM_TEXT | 指定长度的随机小写字母 | 是 | `abcxyz` |
| INCREMENT_NUMBER | 自增数（从默认值开始） | 否 | `1, 2, 3...` |
| Groovy_CODE | 通过 Groovy 脚本自定义生成 | 否 | 自定义 |

---

## Groovy 脚本示例

创建 `modify.groovy` 文件，内容如下：

```groovy
def modifyArg(Map<String, String> params) {
    def name = params.keySet().iterator().next()
    def value = params.get(name)
    return "custom_" + value + "_" + System.currentTimeMillis()
}
```

在添加参数时选择 **auto update type** 为 `Groovy_CODE`，并指定脚本路径即可。Groovy 脚本中需定义 `modifyArg(Map<String, String>)` 方法。

---

## 贡献指南

欢迎贡献代码或提出改进建议！请遵循以下步骤：

1. Fork 本仓库。
2. 创建功能分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -m 'Add some feature'`
4. 推送到分支：`git push origin feature/your-feature`
5. 提交 Pull Request。

---

## 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 安全免责声明

本项目仅供安全研究和授权测试使用。使用本插件时，请确保您拥有对目标系统的合法测试授权。开发者不对因滥用本工具造成的任何直接或间接损害负责。详见 [SECURITY.md](SECURITY.md)。

---

## 联系方式

- GitHub Issues：[https://github.com/oxff/burp-environment/issues](https://github.com/oxff/burp-environment/issues)

---

## 相关文档

- [使用教程](doc/tutorial.md)
- [变更日志](doc/CHANGELOG.md)
- [English README](README_EN.md)
- [AGENT.md](AGENT.md)