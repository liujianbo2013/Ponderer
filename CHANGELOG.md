# Changelog

## 1.2.0

### 新增功能 / New Features

- **JEI 集成**：所有 ID 输入框旁新增 [J] 按钮（需安装 JEI），点击后可从 JEI 物品列表中点击或拖放物品自动填入 ID。支持方块、物品、实体三种模式，自动过滤不兼容类型。
  JEI integration for all ID input fields. Click [J] to browse JEI, then click or drag-drop items to fill in IDs. Supports block, item, and entity modes with automatic type filtering.

- **方块状态属性**：放置/替换方块时可指定 BlockState 属性（如 facing、half、powered 等）。编辑器中以动态列表形式输入，支持添加/删除属性行。
  Block state properties support when placing/replacing blocks (e.g. facing, half, powered). Editor uses a dynamic key=value list with add/remove buttons.

- **扩展实体解析**：JEI 中除刷怪蛋外，船、矿车、盔甲架等物品类实体也可直接拖入实体 ID 字段。
  Extended entity resolution: boats, minecarts, armor stands, and other item-based entities can be dragged into entity ID fields from JEI (not just spawn eggs).

---

## 1.1.0

### 新增功能 / New Features

- **步骤复制/粘贴**：在场景编辑器中复制步骤并粘贴到任意位置。
  Copy/paste steps in the scene editor and insert at any position.

- **撤销/重做**：Ctrl+Z 撤销、Ctrl+Y 重做步骤操作。
  Undo with Ctrl+Z, redo with Ctrl+Y.

- **场景包导入导出**：将所有脚本和结构导出为 ZIP 文件，方便在社区分享或备份。
  Export all scripts and structures as a ZIP file for easy sharing or backup.
  - `/ponderer export [filename]`
  - `/ponderer import <filename>`

- **场景选点**：所有坐标字段旁新增选点按钮（+），点击后跳转到 Ponder 场景中直接选取方块坐标。左键选取方块坐标，右键选取相邻方块坐标。实体/文本等非方块字段自动应用面感知偏移（+0.5）。
  All coordinate fields now have a pick button (+). Click it to jump into the Ponder scene and pick block coordinates directly. Left-click picks the block position, right-click picks the adjacent block position. Non-block fields (entity/text) automatically apply face-aware +0.5 offset.

- **编辑器表单 tooltip**：所有标签和选点按钮支持鼠标悬停显示 tooltip 说明。
  All labels and pick buttons now show tooltip descriptions on hover.

### 改进 / Improvements

- 步骤编辑器支持在指定位置插入新步骤（而非仅追加到末尾）。
  Step editor supports inserting new steps at a specific position (not just appending).

- 步骤类型选择器按类别分组排列，更易查找。
  Step type selector is grouped by category for easier navigation.

---

## 1.0.0

### 核心功能 / Core Features

- **JSON DSL 场景定义**：在 `config/ponderer/scripts/` 中使用 JSON DSL 定义 Ponder 场景。
  JSON DSL scene definition in `config/ponderer/scripts/`.

- **游戏内场景编辑器**：通过图形界面新增、编辑、删除、排序步骤。
  In-game scene editor with GUI for adding, editing, deleting, and reordering steps.

- **自定义结构加载**：从 `config/ponderer/structures/` 加载自定义结构文件。
  Custom structure loading from `config/ponderer/structures/`.

- **蓝图选区与结构保存**：使用蓝图工具（默认为纸）选择区域并保存结构。
  Blueprint selection and structure saving using the blueprint tool (default: paper).

- **开箱即用**：内置引导思索；手持书与笔可直接查看示例思索。
  Works out of the box with a built-in guide scene; hold a writable_book to view the demo scene.

- **NBT 场景过滤**：通过 `nbtFilter` 实现基于 NBT 的场景匹配。
  NBT-based scene filtering via `nbtFilter`.

- **本地热重载**：编辑后直接重载，快速查看效果。
  Local hot reload for fast iteration without restarts.

- **多人协作同步**：客户端与服务端拉取/推送场景内容（含冲突处理）。
  Client-server pull/push with conflict handling.

- **PonderJS 双向转换**：支持与 PonderJS 格式互相导入/导出。
  Bidirectional PonderJS conversion (import/export).

- **物品列表界面**：展示全部已注册思索物品。
  Item list UI for all registered ponder items.

### 命令 / Commands

- `/ponderer reload`、`pull`、`push`、`download`、`new`、`copy`、`delete`、`list`、`convert`
