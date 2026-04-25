# Agent 协作指南

本文档面向 AI Agent 和自动化工具，描述本项目的结构、核心逻辑和开发规范，以便 Agent 能够高效地协助开发和维护。

---

## 项目概述

- **名称**：Environment
- **类型**：Burp Suite 扩展插件（Extension）
- **功能**：在 HTTP 请求发送前，自动替换请求中 `{{variableName}}` 标记的变量为动态生成的值。
- **语言**：Java 17
- **构建**：Maven
- **入口类**：[`oxff.org.Environment`](src/main/java/oxff/org/Environment.java)

---

## 目录结构

```
src/main/java/oxff/org/
├── Environment.java                 # 插件入口，实现 BurpExtension 接口
├── GlobalConst.java                 # 全局常量
├── controler/
│   └── EnviHttpHandler.java         # HTTP 请求处理器，注册到 Burp
├── model/
│   ├── Arg.java                     # 参数实体类
│   ├── ArgDialogOpType.java         # 对话框操作类型枚举（ADD/EDIT/VIEW）
│   ├── ArgTableModel.java           # 参数表格数据模型
│   ├── ArgType.java                 # 参数类型枚举（TEXT/NUMBER/ALL）
│   ├── AutoUpdateType.java          # 自动更新类型枚举
│   ├── HeaderLineVariableInfo.java  # 请求头变量信息
│   └── VariableInfo.java            # 通用变量信息
├── persistence/
│   └── PersistenceManager.java      # SQLite 数据库操作
├── ui/
│   ├── ArgDialog.java               # 参数添加/编辑对话框
│   ├── EnvironmentTab.java          # 插件主界面（Swing Tab）
│   └── PopUpMenu.java               # 右键上下文菜单
└── utils/
    ├── requestProcessor/            # 请求处理链
    │   ├── RequestProcessor.java    # 请求处理器总控
    │   ├── bodyProcessor/           # 请求体处理
    │   ├── headerProcessor/         # 请求头处理
    │   ├── urlPathProcessor/        # URL 路径处理
    │   └── urlQueryProcessor/       # URL 查询参数处理
    ├── sec/sha/                     # SHA 工具类
    ├── ArgTool.java                 # 参数工具类（变量提取、标记检测）
    ├── GroovyUtils.java             # Groovy 脚本加载与执行
    ├── StringTool.java              # 字符串工具
    ├── Tools.java                   # 自动更新值生成工具
    ├── YamlExporter.java            # YAML 导出
    └── YamlImporter.java            # YAML 导入
```

---

## 核心流程

### 1. 插件初始化流程

[`Environment.initialize()`](src/main/java/oxff/org/Environment.java) 执行以下步骤：

1. 设置扩展名称、日志记录器。
2. 初始化参数列表 `args` 和表格模型 `argTableModel`。
3. 建立 `AutoUpdateType` 到 `Method` 的映射 `autoUpdateMethods`，用于后续反射调用生成值。
4. 初始化 SQLite 持久化层，从数据库加载已保存的参数。
5. 注册 Suite Tab（[`EnvironmentTab`](src/main/java/oxff/org/ui/EnvironmentTab.java)）。
6. 注册右键菜单（[`PopUpMenu`](src/main/java/oxff/org/ui/PopUpMenu.java)）。
7. 注册 HTTP 处理器（[`EnviHttpHandler`](src/main/java/oxff/org/controler/EnviHttpHandler.java)）。

### 2. HTTP 请求处理流程

[`EnviHttpHandler.handleHttpRequestToBeSent()`](src/main/java/oxff/org/controler/EnviHttpHandler.java) 在请求发出前被 Burp 调用：

1. 调用 [`RequestProcessor.processPath()`](src/main/java/oxff/org/utils/requestProcessor/RequestProcessor.java) 处理 URL 路径中的变量标记。
2. 调用 [`RequestProcessor.processQuery()`](src/main/java/oxff/org/utils/requestProcessor/RequestProcessor.java) 处理查询参数中的变量标记。
3. 调用 [`RequestProcessor.processBody()`](src/main/java/oxff/org/utils/requestProcessor/RequestProcessor.java) 处理请求体中的变量标记。
4. 返回处理后的请求继续发送。

### 3. 变量替换机制

- 变量标记格式：`{{variableName}}`
- 检测工具：[`ArgTool.isMarker()`](src/main/java/oxff/org/utils/ArgTool.java) 系列方法
- 提取工具：[`ArgTool.extractBodyVariableInfos()`](src/main/java/oxff/org/utils/ArgTool.java) 等
- 值生成：通过 [`ArgTool.getNewValue()`](src/main/java/oxff/org/utils/ArgTool.java) 反射调用 [`Tools`](src/main/java/oxff/org/utils/Tools.java) 中的方法或执行 Groovy 脚本

### 4. 数据持久化

- 数据库：SQLite，文件位于运行目录下的 `environment/environment.db`
- 管理类：[`PersistenceManager`](src/main/java/oxff/org/persistence/PersistenceManager.java)
- 每个参数可通过 `persistent` 字段控制是否保存到数据库

---

## 关键类说明

| 类 | 职责 |
|---|---|
| `Environment` | 插件入口，全局状态管理 |
| `Arg` | 参数实体，包含名称、类型、自动更新类型、值、脚本等属性 |
| `ArgTableModel` | Swing 表格模型，管理参数的增删改查和排序 |
| `EnvironmentTab` | 主界面，包含按钮、搜索框、表格 |
| `ArgDialog` | 参数编辑对话框，包含完整的表单校验逻辑 |
| `Tools` | 值生成器：UUID、时间戳、随机数、自增数等 |
| `ArgTool` | 变量标记检测与提取、值生成调用 |
| `GroovyUtils` | Groovy 脚本解析、缓存、执行 |
| `RequestProcessor` | 请求处理总控，协调各子处理器 |

---

## 开发规范

### 命名规范

- 类名：PascalCase
- 方法/变量：camelCase
- 常量：UPPER_SNAKE_CASE
- 包名：全小写

### 代码风格

- 使用 4 空格缩进
- 大括号使用 K&R 风格
- 日志输出使用 Burp 的 `Logging` 接口（`logger.logToOutput` / `logger.logToError`）

### 添加新的自动更新类型

1. 在 [`AutoUpdateType`](src/main/java/oxff/org/model/AutoUpdateType.java) 枚举中新增类型。
2. 在 [`Tools`](src/main/java/oxff/org/utils/Tools.java) 中新增对应的静态生成方法。
3. 在 [`Environment.initialize()`](src/main/java/oxff/org/Environment.java) 中将新类型映射到对应方法。
4. 在 [`ArgDialog`](src/main/java/oxff/org/ui/ArgDialog.java) 的 UI 初始化中添加到下拉框选项。
5. 在 [`ArgTool.needParams()`](src/main/java/oxff/org/utils/ArgTool.java) 中判断是否需要额外参数。
6. 在 [`ArgTool.getNewValue()`](src/main/java/oxff/org/utils/ArgTool.java) 中添加分支处理。
7. 在 [`ArgDialog.updateExampleLabelAndLog()`](src/main/java/oxff/org/ui/ArgDialog.java) 中添加示例显示。

### 数据库变更

如需修改参数结构：

1. 更新 [`Arg`](src/main/java/oxff/org/model/Arg.java) 实体类。
2. 更新 [`PersistenceManager`](src/main/java/oxff/org/persistence/PersistenceManager.java) 的建表和 CRUD 语句。
3. 更新 [`YamlExporter`](src/main/java/oxff/org/utils/YamlExporter.java) 和 [`YamlImporter`](src/main/java/oxff/org/utils/YamlImporter.java)。
4. 更新 [`ArgDialog`](src/main/java/oxff/org/ui/ArgDialog.java) 的表单和校验逻辑。

---

## 构建与测试

```bash
# 构建
mvn clean package

# 构建产物
target/environment-1.0_yyyyMMdd_HHmm.jar
```

---

## 常见问题

**Q: 插件加载后没有显示 Tab？**
A: 检查 Burp 的 Extensions 输出日志，确认是否有异常。常见问题包括依赖缺失或 Java 版本不匹配。

**Q: 参数值没有自动更新？**
A: 确认参数已启用（enabled=true），且 HTTP 请求中使用了正确的 `{{variableName}}` 标记语法。

**Q: Groovy 脚本执行失败？**
A: 确认脚本文件中包含 `modify(Map<String, String>)` 方法，且文件路径正确。

---

## 相关文件

- [README.md](README.md)
- [README_EN.md](README_EN.md)
- [LICENSE](LICENSE)
- [SECURITY.md](SECURITY.md)
