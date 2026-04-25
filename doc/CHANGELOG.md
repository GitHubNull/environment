# 变更日志

本文档记录 Environment 项目的所有重要变更。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

---

## [0.1.13] - 2026-04-25

### 变更

- 版本号升级至 0.1.13

### 文档

- 更新 README.md、README_EN.md、AGENT.md、doc/tutorial.md、SECURITY.md 中的版本号和 JAR 文件名
- 修复文档中 Groovy 方法名为 `modifyArg`（与代码实际调用一致）
- 修正 GitHub 仓库 URL 为 `https://github.com/oxff/burp-environment`
- 补全 AGENT.md 目录结构（新增 `AboutPanel`、`TutorialPanel`、`MarkdownRenderer`）
- 新增 `doc/CHANGELOG.md` 变更日志文件
- 更新版权年份至 2024-2026
- 同步更新 `TutorialPanel.java` 和 `AboutPanel.java` 中内嵌的文档内容

---

## [0.1.12] - 2026-04-25

### 修复

- 修复 CI 工作流中时间戳 JAR 文件名 glob 匹配模式

### 变更

- 版本号升级至 0.1.12

---

## [0.1.11] - 2025-03-10

### CI/CD

- 优化 Maven 发布流程和发布说明生成

---

## [0.1.10] - 2025-03-07

### CI/CD

- 优化 release 创建流程中的版本发布说明
- 修改版本发布说明标题格式
- 更新 Maven 发布工作流
- 修正 JAR 文件名引用并添加动态生成发布说明
- 优化 GitHub Actions 发布流程

---

## [0.1.9] - 2025-03-07

### CI/CD

- 优化 GitHub Actions 发布流程
- 更新 Maven 发布工作流

---

## [0.1.8] - 2025-03-07

### CI/CD

- 优化 GitHub Actions 发布流程

---

## [0.1.7] - 2025-03-06

### 文档

- 添加 README.md 文件

### CI/CD

- 优化 GitHub Actions 发布流程

---

## [0.1.6] - 2025-03-06

### CI/CD

- 优化 Maven 发布流程

---

## [0.1.5] - 2025-03-06

### CI/CD

- 更新版本匹配规则

---

## [0.1.4] - 2025-03-06

### CI/CD

- 添加 Maven 发布 GitHub Release 的工作流

---

## [0.1.3] - 2025-03-06

### CI/CD

- 添加 Maven 发布 GitHub Release 的工作流

---

## [0.1.2] - 2025-03-06

### CI/CD

- 添加 Maven 发布 GitHub Release 的工作流

---

## [0.1.1] - 2025-03-06

### 构建

- 更新项目版本并添加时间戳构建支持
- 构建产物 JAR 文件名格式：`environment-{version}_{yyyyMMdd_HHmm}.jar`
- 新增 `maven-antrun-plugin` 插件，自动复制生成不带时间戳的 JAR

### 重构

- 优化 ArgDialog 中的代码结构

---

## [0.1.0] - 2024-10-20 ~ 2025-03-06

### 新增

- **核心功能**：实现环境变量替换功能，支持 `{{variableName}}` 标记语法
- **参数管理**：支持添加、编辑、删除、查看参数；提供表格视图
- **自动更新类型**：
  - `UUID`：生成随机 UUID
  - `TIMESTAMP`：当前时间戳（毫秒）
  - `SHA1_OF_TIMESTAMP`：当前时间戳的 SHA1 哈希
  - `RANDOM_NUMBER`：指定长度的随机数字
  - `RANDOM_TEXT`：指定长度的随机小写字母
  - `INCREMENT_NUMBER`：自增数
  - `Groovy_CODE`：通过 Groovy 脚本自定义生成
- **Groovy 脚本扩展**：支持外部 Groovy 脚本自定义参数生成逻辑（`GroovyUtils`）
- **请求全链路处理**：自动处理 URL 路径、查询参数、请求头、请求体中的变量标记
- **请求体处理**：支持 Form、Multipart、Text/JSON 格式
- **数据持久化**：使用 SQLite 本地存储参数配置（`PersistenceManager`）
- **YAML 导入/导出**：支持参数配置的 YAML 格式导入导出
- **SHA 工具类**：支持 ByteArray、File、String 的 SHA 哈希计算
- **右键菜单**：注册 Burp 上下文菜单（`PopUpMenu`）
- **图形化界面**：基于 Swing 的参数管理界面（`EnvironmentTab`）
  - 参数管理标签页：Add / Edit / Remove / Clear / Move Up / Move Down / Import / Export / Query
  - 关于标签页：内嵌 MIT License 和安全声明
  - 教程标签页：内嵌完整使用教程

### 修复

- 修复参数表中上下移动行时的索引错误
- 修复修改参数时显示不正确的问题

### 重构

- 重构参数处理逻辑
- 重构参数表格模型（`ArgTableModel`）
- 重构参数对话框逻辑（`ArgDialog`）
- 重构请求处理器架构

### 构建

- 更新 `.gitignore` 文件排除 `out` 目录

---

## 版本说明

- **0.1.x** 系列为初始开发版本，功能逐步完善中
- 版本号格式遵循 `主版本.次版本.修订号`
- 带 `v` 前缀的 Git 标签用于触发 GitHub Actions 自动发布

---

> 由 Git 提交历史自动整理生成，部分早期版本的变更范围可能不够精确。
