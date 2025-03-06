# Environment

[![Java Version](https://img.shields.io/badge/Java-17-blue)](https://openjdk.org/)
[![Maven Central](https://img.shields.io/maven-central/v/net.portswigger.burp.extensions/montoya-api)](https://mvnrepository.com/artifact/net.portswigger.burp.extensions/montoya-api)

**Environment** 是一款 Burp Suite 插件，用于自动修改 HTTP 请求报文中的用户定义变量。它支持多种类型的参数（如文本、数字等），并提供了丰富的功能来满足不同场景的需求。

---

## 功能特性

- **参数管理**：
    - 支持添加、编辑、删除和查看参数。
    - 提供表格视图，方便用户快速浏览和操作参数列表。
    - 支持多字段排序和关键字搜索功能。

- **自动更新机制**：
    - 支持多种自动更新类型，包括 UUID、时间戳、随机数、自增数等。
    - 可通过 Groovy 脚本实现自定义逻辑的参数更新。

- **灵活的参数配置**：
    - 参数类型支持文本、数字和通用类型。
    - 可设置默认值、长度限制以及描述信息。
    - 提供启用/禁用开关，便于控制参数的使用状态。

- **用户友好的界面**：
    - 基于 Swing 的图形化界面，操作直观便捷。
    - 提供双击查看、上下移动等功能，提升用户体验。

- **扩展性**：
    - 支持外部 Groovy 脚本加载，满足复杂业务逻辑需求。
    - 易于集成到现有系统中，适合作为 Burp Suite 扩展或其他 Java 应用的一部分。

---

## 技术栈

- **编程语言**：Java 17
- **构建工具**：Maven
- **依赖库**：
    - [Montoya API](https://portswigger.net/burp/extender/api)：Burp Suite 扩展开发的核心接口。
    - [Groovy](https://groovy-lang.org/)：用于动态脚本执行。
    - [Apache Commons IO](https://commons.apache.org/proper/commons-io/) 和 [Apache Commons Codec](https://commons.apache.org/proper/commons-codec/)：提供文件操作和编码工具。

---

## 安装与运行

### 环境要求

- JDK 17 或更高版本
- Maven 3.x

### 构建项目

1. 克隆项目代码：
   ```bash
   git clone https://github.com/your-repo/environment-manager.git
   cd environment-manager
   ```


2. 使用 Maven 构建项目：
   ```bash
   mvn clean package
   ```


3. 构建完成后，生成的可执行 JAR 文件位于 `target` 目录下。

### 运行项目

运行生成的 JAR 文件：
```bash
java -jar target/environment-1.0_yyyyMMdd_HHmm.jar
```


---

## 使用说明

### 添加参数

1. 点击界面上的 **Add** 按钮。
2. 在弹出的对话框中填写参数名称、类型、自动更新方式等信息。
3. 点击 **OK** 完成添加。

### 编辑参数

1. 在表格中选中需要编辑的参数。
2. 点击 **Edit** 按钮。
3. 修改参数信息后点击 **OK**。

### 删除参数

1. 在表格中选中需要删除的参数。
2. 点击 **Remove** 按钮，确认删除操作。

### 搜索参数

在搜索框中输入关键字，点击 **Query** 按钮即可筛选出匹配的参数。

---

## 示例

以下是一个简单的参数配置示例：

| 参数名称   | 类型   | 自动更新类型       | 长度 | 默认值 | 描述           |
|------------|--------|--------------------|------|--------|----------------|
| user_id    | 数字   | 自增数            | 8    | 1      | 用户 ID        |
| session_id | 文本   | UUID              | -    | -      | 会话标识符     |
| timestamp  | 通用   | 时间戳            | -    | -      | 当前时间戳     |

---

## 贡献指南

欢迎贡献代码或提出改进建议！请遵循以下步骤：

1. Fork 本仓库。
2. 创建您的功能分支 (`git checkout -b feature/your-feature`)。
3. 提交更改 (`git commit -m 'Add some feature'`)。
4. 推送到分支 (`git push origin feature/your-feature`)。
5. 提交 Pull Request。

---

## 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 联系方式

如有任何问题或建议，请通过以下方式联系我们：
- GitHub Issues：[Issues Page](https://github.com/GitHubNull/environment/issues)

---

希望这份 `README.md` 能够帮助您更好地宣传和推广项目！如果有其他需求或需要进一步调整，请随时告知.