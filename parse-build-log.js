#!/usr/bin/env node
/**
 * HBuilderX iOS 打包日志解析器
 * 用法: node parse-build-log.js <html日志文件路径>
 * 示例: node parse-build-log.js uts-sdk-demo-2026-04-13-19-30-53.html
 */

const fs = require('fs');
const path = require('path');

// ==================== 颜色输出 ====================
const C = {
  reset: '\x1b[0m',
  red: '\x1b[31m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  magenta: '\x1b[35m',
  cyan: '\x1b[36m',
  gray: '\x1b[90m',
  bold: '\x1b[1m',
  bgRed: '\x1b[41m',
  bgGreen: '\x1b[42m',
};

function colored(text, ...styles) {
  return styles.join('') + text + C.reset;
}

// ==================== HTML 解析 ====================
function stripHtml(html) {
  // 提取 <a> 标签中的 data-path（保留真实路径）
  let text = html.replace(/<a[^>]*data-path="([^"]*)"[^>]*>[^<]*<\/a>/g, '$1');
  // 去掉剩余 HTML 标签
  text = text.replace(/<[^>]+>/g, '');
  // 解码 HTML 实体
  text = text
    .replace(/&#x27;/g, "'")
    .replace(/&#39;/g, "'")
    .replace(/&quot;/g, '"')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&')
    .replace(/&nbsp;/g, ' ');
  return text;
}

function parseLines(text) {
  // 按时间戳分行: "2026-04-13 19:30:24.823: ..."
  const lines = [];
  const regex = /(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}): /g;
  let match;
  let lastIndex = 0;
  let lastTimestamp = null;

  while ((match = regex.exec(text)) !== null) {
    if (lastTimestamp !== null) {
      lines.push({
        timestamp: lastTimestamp,
        content: text.substring(lastIndex, match.index).trim(),
      });
    }
    lastTimestamp = match[1];
    lastIndex = match.index + match[0].length;
  }
  // 最后一行
  if (lastTimestamp !== null) {
    lines.push({
      timestamp: lastTimestamp,
      content: text.substring(lastIndex).trim(),
    });
  }
  return lines;
}

// ==================== 分类逻辑 ====================

function classifyLine(content) {
  const lower = content.toLowerCase();

  // Swift 编译错误（最重要）— 严格匹配 "文件:行:列: error: 消息" 格式
  if (/\.swift:\d+:\d+:\s*error:/.test(content)) {
    return 'swift_error';
  }
  // ObjC header 里的 error:(EMError... 是参数名不是编译错误，跳过
  // 其他带 error: 的行，检查是否是真正的编译器 error 输出
  if (/\.(?:m|h|c|cpp):\d+:\d+:\s*error:/.test(content)) {
    return 'error';
  }

  // 构建失败
  if (content.includes('ARCHIVE FAILED') || content.includes('BUILD FAILED')) return 'fatal';
  if (content.includes('打包失败') || content.includes('编译失败')) return 'fatal';
  if (content.includes('pod install 失败') || content.includes('Pod install failed')) return 'fatal';

  // 失败的构建命令
  if (lower.includes('the following build commands failed')) return 'failed_commands';
  if (/^\s*(SwiftCompile|CompileC|Ld|Archiving)/.test(content) && lower.includes('failed')) return 'failed_commands';

  // failures 计数 (提前判断)
  if (/^\(\d+ failures?\)$/.test(content.trim())) return 'failure_count';

  // Swift 编译警告（只保留项目相关的，过滤 SDK header 警告）
  if (/\.swift:\d+:\d+:.*warning:/.test(content)) return 'swift_warning';

  // note（通常是错误的补充说明）
  if (/\.swift:\d+:\d+:.*note:/.test(content)) return 'note';

  // 构建成功
  if (content.includes('BUILD SUCCEEDED') || content.includes('ARCHIVE SUCCEEDED')) return 'success';
  if (content.includes('打包成功')) return 'success';

  // Pod 安装
  if (content.includes('pod install') || content.includes('Pod installation')) return 'pod';
  if (/^(Analyzing|Downloading|Installing|Generating|Integrating)/.test(content)) return 'pod';

  // 状态信息（✔ / ✖）
  if (content.includes('✔')) return 'status_ok';
  if (content.includes('✖')) return 'status_fail';

  // 签名相关
  if (lower.includes('signing') || lower.includes('签名') || lower.includes('证书') || lower.includes('profile')) return 'signing';

  // failures 计数
  if (/^\(\d+ failures?\)$/.test(content.trim())) return 'failure_count';

  // SDK header 警告（大量的，可以忽略）
  if (/Headers\/.*\.h:\d+:\d+:.*warning:/.test(content)) return 'sdk_header_warning';

  return 'other';
}

// ==================== 主逻辑 ====================

function main() {
  const args = process.argv.slice(2);
  if (args.length === 0) {
    console.log(colored('用法: node parse-build-log.js <html日志文件路径>', C.cyan));
    console.log(colored('示例: node parse-build-log.js uts-sdk-demo-2026-04-13-19-30-53.html', C.gray));
    process.exit(1);
  }

  const filePath = path.resolve(args[0]);
  if (!fs.existsSync(filePath)) {
    console.error(colored(`文件不存在: ${filePath}`, C.red));
    process.exit(1);
  }

  console.log(colored(`\n📋 解析构建日志: ${path.basename(filePath)}`, C.bold, C.cyan));
  console.log(colored('─'.repeat(60), C.gray));

  const html = fs.readFileSync(filePath, 'utf-8');
  const text = stripHtml(html);
  const lines = parseLines(text);

  // 收集各类信息
  const errors = [];       // Swift 编译 error
  const warnings = [];     // Swift 编译 warning（项目代码）
  const notes = [];        // 编译 note
  const fatals = [];       // 构建失败
  const failedCmds = [];   // 失败的命令
  const pods = [];         // Pod 信息
  const statuses = [];     // 状态行
  const signing = [];      // 签名相关
  let sdkWarningCount = 0; // SDK header 警告计数
  let buildResult = null;  // BUILD SUCCEEDED / FAILED
  let inFailedSection = false; // 标记是否在 "build commands failed" 区段内

  for (const line of lines) {
    const type = classifyLine(line.content);
    switch (type) {
      case 'swift_error':
        errors.push(line);
        break;
      case 'error':
        errors.push(line);
        break;
      case 'swift_warning':
        warnings.push(line);
        break;
      case 'note':
        notes.push(line);
        break;
      case 'fatal':
        fatals.push(line);
        buildResult = 'FAILED';
        break;
      case 'failed_commands':
        failedCmds.push(line);
        inFailedSection = true;
        break;
      case 'failure_count':
        failedCmds.push(line);
        inFailedSection = false;
        break;
      case 'success':
        buildResult = 'SUCCESS';
        statuses.push(line);
        break;
      case 'pod':
        pods.push(line);
        break;
      case 'status_ok':
      case 'status_fail':
        statuses.push(line);
        if (type === 'status_fail') fatals.push(line);
        break;
      case 'signing':
        signing.push(line);
        break;
      case 'sdk_header_warning':
        sdkWarningCount++;
        break;
      default:
        // 在 "build commands failed" 区段内的行归入失败命令
        if (inFailedSection && /^\s*(SwiftCompile|CompileC|Ld)\s/.test(line.content)) {
          failedCmds.push(line);
        }
        break;
    }
  }

  // ========== 输出结果 ==========

  // 1. 构建结果
  console.log(colored('\n🏗️  构建结果', C.bold));
  if (buildResult === 'SUCCESS') {
    console.log(colored('  ✅ 构建成功!', C.green, C.bold));
  } else if (buildResult === 'FAILED') {
    console.log(colored('  ❌ 构建失败!', C.red, C.bold));
  } else {
    console.log(colored('  ⚠️  无法判断构建结果', C.yellow));
  }

  // 2. 构建流程状态
  console.log(colored('\n📌 构建流程', C.bold));
  for (const s of statuses) {
    const icon = s.content.includes('✔') ? colored('  ✔', C.green) :
                 s.content.includes('✖') ? colored('  ✖', C.red) :
                 colored('  ●', C.blue);
    // 清理内容，去掉原始的 ✔ ✖ 符号
    const cleanContent = s.content.replace(/[✔✖]/g, '').trim();
    console.log(`${icon} ${cleanContent}`);
  }

  // 3. Pod 安装信息
  if (pods.length > 0) {
    console.log(colored('\n📦 CocoaPods', C.bold));
    for (const p of pods) {
      if (p.content.startsWith('Installing')) {
        console.log(colored(`  + ${p.content.replace('Installing ', '')}`, C.cyan));
      } else if (p.content.includes('Pod installation complete')) {
        console.log(colored(`  ✔ ${p.content}`, C.green));
      } else if (p.content.toLowerCase().includes('失败') || p.content.toLowerCase().includes('failed')) {
        console.log(colored(`  ✖ ${p.content}`, C.red));
      }
    }
  }

  // 4. 签名信息
  if (signing.length > 0) {
    console.log(colored('\n🔐 签名配置', C.bold));
    for (const s of signing) {
      console.log(colored(`  ${s.content}`, C.gray));
    }
  }

  // 5. Swift 编译错误（最重要！）
  if (errors.length > 0) {
    console.log(colored(`\n🔴 编译错误 (${errors.length} 个)`, C.bold, C.red));
    console.log(colored('─'.repeat(60), C.red));

    for (const e of errors) {
      // 尝试提取文件名:行号:列号: error: 具体信息
      const match = e.content.match(/([^\s/]*\.swift:\d+:\d+):\s*error:\s*(.*)/);
      if (match) {
        const location = match[1];
        const message = match[2].substring(0, 200); // 截断过长信息
        console.log(colored(`\n  📍 ${location}`, C.yellow));
        console.log(colored(`     ${message}`, C.red));

        // 找到对应的 note 行（补充说明）
        const relatedNotes = notes.filter(n => {
          const noteMatch = n.content.match(/([^\s/]*\.swift:\d+:\d+):\s*note:/);
          return noteMatch && noteMatch[1] === location;
        });
        for (const n of relatedNotes) {
          const noteMsg = n.content.match(/note:\s*(.*)/);
          if (noteMsg) {
            console.log(colored(`     💡 ${noteMsg[1].substring(0, 200)}`, C.cyan));
          }
        }
      } else {
        console.log(colored(`\n  ${e.content.substring(0, 300)}`, C.red));
      }
    }
  }

  // 6. 项目 Swift 警告
  if (warnings.length > 0) {
    console.log(colored(`\n🟡 Swift 编译警告 (${warnings.length} 个)`, C.bold, C.yellow));
    for (const w of warnings) {
      const match = w.content.match(/([^\s/]*\.swift:\d+:\d+):\s*warning:\s*(.*)/);
      if (match) {
        console.log(colored(`  ⚠ ${match[1]}: ${match[2].substring(0, 150)}`, C.yellow));
      }
    }
  }

  // 7. SDK Header 警告汇总
  if (sdkWarningCount > 0) {
    console.log(colored(`\n🔶 SDK Header 警告: ${sdkWarningCount} 个 (来自 HyphenateChat 等 framework，可忽略)`, C.gray));
  }

  // 8. 失败的构建命令
  if (failedCmds.length > 0) {
    console.log(colored('\n💥 失败的构建命令', C.bold, C.red));
    for (const cmd of failedCmds) {
      // 提取关键信息：SwiftCompile ... /path/to/file.swift (in target 'xxx')
      const swiftMatch = cmd.content.match(/SwiftCompile\s+\w+\s+\w+\s+(?:Compiling\\\s+\S+\s+)?(\S+\.swift)\s*\(in target '([^']+)'/);
      if (swiftMatch) {
        const file = path.basename(swiftMatch[1]);
        const target = swiftMatch[2];
        console.log(colored(`  → SwiftCompile ${file} (target: ${target})`, C.red));
      } else if (/^\(\d+ failures?\)$/.test(cmd.content.trim())) {
        console.log(colored(`  ${cmd.content.trim()}`, C.red));
      } else {
        const short = cmd.content.substring(0, 200);
        console.log(colored(`  → ${short}`, C.red));
      }
    }
  }

  // 9. 总结
  console.log(colored('\n' + '─'.repeat(60), C.gray));
  console.log(colored('📊 汇总', C.bold));
  console.log(`  编译错误:     ${errors.length > 0 ? colored(String(errors.length), C.red, C.bold) : colored('0', C.green)}`);
  console.log(`  Swift 警告:   ${warnings.length > 0 ? colored(String(warnings.length), C.yellow) : colored('0', C.green)}`);
  console.log(`  SDK 警告:     ${colored(String(sdkWarningCount), C.gray)} (可忽略)`);
  console.log(`  总行数:       ${colored(String(lines.length), C.cyan)}`);
  console.log('');
}

main();
