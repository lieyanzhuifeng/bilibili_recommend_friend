<!-- .github/copilot-instructions.md -->
# 快速上手 — 给 AI 代码助手的项目说明

下面是针对本仓库（基于 Vue 3 + Vite + Pinia）的简明、可操作的指导，帮助 AI 代码助手快速理解代码结构、常见约定、关键集成点以及可直接使用的命令和示例。

## 一行概述
- 前端单页应用：Vue 3 + Vite，使用 `pinia` 做状态管理，`vue-router` 做路由，项目以客户端模拟/本地存储为主（无后端服务目录）。

## 快速命令（在项目根目录运行）
- 安装依赖：`npm install`
- 开发热重载：`npm run dev`（启动 Vite 开发服务器）
- 生产打包：`npm run build`
- 预览构建：`npm run preview`
- 修复/检查 lint：`npm run lint`

项目依赖 Node >= 20.19 或 >= 22.12（详见 `package.json` 的 `engines`）。

## 大局架构（必读文件与流向）
- 入口：`src/main.js`（注册 Pinia、Router，挂载 App）
- 路由：`src/router/index.js`
  - 路由使用按需加载（`component: () => import('../views/..')`）实现 code-splitting。
  - 全局路由守卫在 `beforeEach` 中使用 `useUserStore()` 判断认证状态并重定向到登录页。
- 状态管理：`src/stores/`（Pinia 定义的 store）
  - `src/stores/user.js`：负责读取/写入 localStorage 的 `user`，提供 `isLoggedIn` getter 与 `loadUserInfo()`、`setUserInfo()`、`logout()` 等 action。
  - `src/stores/chat.js`：包含好友、会话、未读计数的模拟实现（`initMockData()`），发送/接收消息为模拟逻辑（用于开发/demo）。
- 视图与布局：`src/layouts/MainLayout.vue` 与 `src/views/*.vue`（例如 `Login.vue`, `Profile.vue`, `Friends.vue`, `Chat.vue`）

## 项目特定约定（重要）
- 本项目侧重前端演示/本地模拟：
  - 聊天与好友数据在 `chat` store 中为模拟数据（`initMockData()`），没有现成的 API 层。若要接入后端，优先在 `src/stores/*` 的 action 中替换模拟逻辑或新增 `src/services/`。
- localStorage key：用户信息保存在 `localStorage` 的 `user`（见 `user.js` 的 `loadUserInfo()` / `setUserInfo()`）。
- 当前用户 id 表示与消息字段约定：发送者为当前客户端时，`senderId` 值为 `'current'`（见 `chat.js`）。
- Getter 命名风格：多数 getter 以 `getXxx` 或直观名称（`isLoggedIn`、`username`）命名；Actions 使用驼峰（`setUserInfo`, `sendMessage`）。遵循同样风格新增 store 成员。
- 路由守卫行为：如果路由 `meta.requiresAuth` 为 true，会检查 `userStore.isLoggedIn`；未登录会重定向到 `/`（登录页）。如果已登录访问 `/`，会重定向到 `/profile`。修改权限逻辑请更新 `src/router/index.js`。

## 典型修改位置 — 示例引用
- 把登录流程与后端对接：
  - 在 `src/stores/user.js` 的 `setUserInfo` / `loadUserInfo` 中添加从后端验证/刷新 token 的逻辑，或新增 `actions.login(credentials)` 调用 API，再调用 `setUserInfo()`。
- 把聊天改成真实后端：
  - 在 `src/stores/chat.js` 中替换 `sendMessage()` 与 `receiveMessage()` 的实现，或添加 `src/services/chat.js` 提供 socket/HTTP 封装，store action 调用该服务。

## 本仓库常见开发注意点
- 懒加载路由：路由组件大多使用 `() => import('...')`，在修改路由路径或文件名时注意同步更新 `router/index.js`。
- 默认头像：`user.js` 的 `avatar` getter 返回默认 DiceBear URL；在 UI 变更时请保持默认 URL 的降级兼容。
- 模拟回复：`chat.js` 使用随机延迟和回复数组模拟对端消息，删除或替换该逻辑前请确保测试页面不会因缺少 mock 而空白。

## 代码风格与工具链
- 使用 ESLint（`npm run lint`），配置见项目根（`eslint.config.js`）。推荐在 VS Code 中启用 Volar（Vue 官方扩展）。
- Vite 插件：项目引入了 `vite-plugin-vue-devtools`，开发时启用可观察组件树。

## 当你（AI）需要做的事 — 优先级指引
1. 若与认证/路由相关，先读 `src/router/index.js` 与 `src/stores/user.js`。
2. 若与聊天/数据展示相关，先读 `src/stores/chat.js` 与相关 `views/Chat.vue`（查找 UI 如何消费 store）。
3. 新增后端 API 集成时，优先新增 `src/services/*` 并将 store 的模拟 action 转为调用服务。

## 变更提交流程（建议）
- 小改动（UI/样式、单个组件）：直接提交 feature 分支并发 PR。
- 大变动（引入后端、改路由守卫、重大状态模型变更）：在 issue 中先讨论设计，然后拆分成小 PR。

---

如果你想让我把某一部分展开成更详细的“如何接入后端”示例（例如基于 REST 或 WebSocket 的聊天实现），告诉我目标后端类型和认证方式，我可以为你写出具体 patch 与示例代码。现在我把这个文件添加到仓库，想要我调整或补充哪部分？
