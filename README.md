# EBU6304-TA-System-Group11-国际学院助教招聘系统 

## 📌 项目概览
本项目为 BUPT 国际学院开发的助教（TA）招聘管理系统。
- **开发模式**：敏捷开发 (Agile Methods)
- **技术栈**：Java 17 (无数据库，纯文本存储)
- **验收日期**：第一阶段 (Phase 1) - 2026年3月22日


## 🌿 Git 分支管理规范 
为保证 GitHub Network Graph 的美观和专业性，请严格遵守：

1. **分支命名**：
   - `main`: 仅存放验收版本的稳定代码（严禁直接在此分支提交）。
   - `develop`: 公共开发汇聚分支。
   - `feature/功能名-姓名缩写`: 个人开发分支（例如：`feature/login-zs`）。

2. **协作流程**：
   - 每次开发前：在 VS Code 点击左下角分支名，切换到 `develop` 并执行 `Pull`（拉取最新代码）。
   - 开始开发：从 `develop` 创建自己的 `feature` 分支。
   - 完成开发：将分支 `Push` 到 GitHub，并在网页端发起 **Pull Request (PR)** 合并回 `develop`。


## ⚠️ 技术红线 (Requirement)
- **严禁使用数据库**：如 MySQL, SQLite 等。
- **持久化方案**：所有数据必须存储在 `data/` 目录下的 `.json` 或 `.txt` 文件中。
- **代码归属**：所有逻辑代码必须位于 `src/` 目录下。


## 👥 小组成员
- **Leader**: 
- **Member**: 
- **Member**: 
- **Member**:
- **Member**: 
- **Member**: 
