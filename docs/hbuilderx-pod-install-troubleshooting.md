# HBuilderX "pod install 失败" 完整排查指南

> 本文档记录了 HBuilderX 编译 UTS iOS 插件时报 "pod install 失败" 的完整排查过程、根因分析和修复方案。

## 问题现象

在 HBuilderX 中运行 UTS iOS 插件（配置了 `dependencies-pods`）时，控制台输出：

```
uts插件[easemob-uts-sdk-beta]编译失败
pod install 失败! [详情点击](https://doc.dcloud.net.cn/uni-app-x/plugin/uts-ios-cocoapods.html#questions)
```

但在终端中直接执行 `pod --version` 完全正常。

## 排查过程

### 第一步：确认 config.json 格式无误

对比即构（Zego）的 config.json，确认格式完全一致：

```json
// easemob config.json
{
  "deploymentTarget": "12",
  "dependencies-pods": [{
    "name": "HyphenateChat",
    "version": "4.20.0"
  }]
}
```

```json
// zego config.json（参考）
{
  "deploymentTarget": "13",
  "dependencies-pods": [{
    "name": "ZegoZIM",
    "version": "2.26.0"
  }]
}
```

**结论：配置没有问题。**

### 第二步：检查 HBuilderX 如何调用 pod

通过阅读 HBuilderX 源码（路径：`HBuilderX.app/Contents/HBuilderX/plugins/uts-development-ios/dependences/buildFramework/cocoapods.js`），发现关键调用方式：

```javascript
// 检测 pod 是否安装（第 102 行）
shell.exec(`bash --login -c 'pod --version'`, {
  encoding: 'utf-8',
  silent: true,
  env: Object.assign({}, process.env, { LANG: 'en_US.UTF-8' })
});

// 执行 pod install（第 235 行）
shell.exec(`bash --login -c '${cdCode} && ${podCode}'`, {
  encoding: 'utf-8',
  silent: true,
  env: Object.assign({}, process.env, { LANG: 'en_US.UTF-8' })
});
```

**关键发现：HBuilderX 通过 `bash --login -c '...'` 调用 pod，走的是 bash 登录 shell 环境。**

### 第三步：模拟 HBuilderX 的调用方式

```bash
bash --login -c 'which ruby; ruby --version; pod --version'
```

输出：

```
/Users/neohuang/.local/ruby/bin/ruby     # ← 不是 RVM 的 Ruby！
ruby 3.3.0 (2023-12-25 revision 5124f9ac75)
# 然后是一长串 LoadError 崩溃...
```

### 第四步：定位 Ruby 冲突

检查 `~/.bash_profile`：

```bash
[[ -s "$HOME/.rvm/scripts/rvm" ]] && source "$HOME/.rvm/scripts/rvm"  # 加载 RVM (Ruby 3.2.2)
export PATH="$HOME/.local/ruby/bin:$PATH"    # ← 这行把 Ruby 3.3.0 放到了最前面！
```

**问题链条：**
1. RVM 加载 → 设置 gem 环境指向 Ruby 3.2.2 的 gems（cocoapods 在这里）
2. `.local/ruby/bin` 被 prepend 到 PATH → `ruby` 命令解析为 Ruby 3.3.0
3. `pod` 命令通过 RVM 的 `ruby_executable_hooks` 启动 → 但实际 ruby 是 3.3.0
4. Ruby 3.3.0 缺少 `psych` 库 → `cannot load such file -- psych (LoadError)` → 崩溃

## 根本原因

**PATH 中存在多个 Ruby 安装，且优先级顺序错误。**

`~/.local/ruby/bin` 中的 Ruby 3.3.0（缺少 psych 库）在 PATH 中排在 RVM 管理的 Ruby 3.2.2 前面，导致 CocoaPods 被错误版本的 Ruby 加载而崩溃。

## 修复方案

从 `~/.bash_profile` 中**移除** `.local/ruby/bin` 的 PATH 设置：

```bash
# 修改前
[[ -s "$HOME/.rvm/scripts/rvm" ]] && source "$HOME/.rvm/scripts/rvm"
export PATH="$HOME/.local/ruby/bin:$PATH"    # 删除这行
export PATH="$HOME/.gem/ruby/2.6.0/bin:$PATH"

# 修改后
[[ -s "$HOME/.rvm/scripts/rvm" ]] && source "$HOME/.rvm/scripts/rvm"
# 已移除 $HOME/.local/ruby/bin (Ruby 3.3.0 缺少 psych, 与 RVM 的 Ruby 3.2.2 冲突)
export PATH="$HOME/.gem/ruby/2.6.0/bin:$PATH"
```

修改后验证：

```bash
bash --login -c 'which ruby; ruby --version; pod --version'
# 应输出：
# /Users/neohuang/.rvm/rubies/ruby-3.2.2/bin/ruby
# ruby 3.2.2 (2023-03-30 revision e51014f9c0)
# 1.16.2
```

## 错误方案记录

### ❌ 方案一：创建 /usr/local/bin/pod wrapper 脚本

曾尝试在 `/usr/local/bin/pod` 创建 wrapper 脚本显式设置 RVM 环境变量。**此方案无效**，因为 HBuilderX 不通过 PATH 查找 `/usr/local/bin/pod`，而是通过 `bash --login` 加载完整 shell 环境后直接调用 `pod`。

### ❌ 方案二：调整 .bash_profile 中 RVM 加载顺序

曾尝试将 RVM 的 `source` 移到 `.bash_profile` 最后一行。**此方案无效**，因为 RVM 检测到已在 `.profile` 中被加载过，会跳过重复初始化，不会重新调整 PATH 顺序。

## 快速诊断清单

遇到 "pod install 失败" 时，按以下步骤排查：

1. **确认 config.json 格式正确**（`dependencies-pods` 数组，每项有 `name` 和 `version`）
2. **模拟 HBuilderX 调用**：`bash --login -c 'pod --version' 2>&1`
3. **检查 Ruby 来源**：`bash --login -c 'which ruby'`
   - 如果不是 RVM 的路径 → Ruby 冲突，检查 `.bash_profile` / `.profile` 中的 PATH
4. **检查 PATH 顺序**：`bash --login -c 'echo $PATH' | tr ':' '\n' | head -15`
5. **确认无 stderr 输出**：任何 warning 或 error 都可能导致 HBuilderX 判定失败

## 相关文件

| 文件 | 说明 |
|------|------|
| `~/.bash_profile` | bash 登录配置，HBuilderX 执行 pod 时会加载 |
| `~/.profile` | 被 `.bash_profile` source 的通用配置 |
| `uni_modules/插件名/utssdk/app-ios/config.json` | UTS 插件的 iOS CocoaPods 依赖配置 |
| `HBuilderX.app/.../uts-development-ios/dependences/buildFramework/cocoapods.js` | HBuilderX 调用 pod 的源码 |

## 环境信息

- macOS (darwin 26.3, arm64)
- HBuilderX 5.06
- CocoaPods 1.16.2
- RVM Ruby 3.2.2（正常）
- ~/.local/ruby Ruby 3.3.0（缺少 psych，已从 PATH 移除）
- 用户默认 shell: fish（但 HBuilderX 走 bash）
