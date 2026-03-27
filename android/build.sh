#!/bin/bash
# Android 本地离线打包脚本

set -e

echo "=== 环信 UniAppX 本地离线打包脚本 ==="
echo ""

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查参数
BUILD_TYPE=${1:-debug}
if [ "$BUILD_TYPE" != "debug" ] && [ "$BUILD_TYPE" != "release" ]; then
    echo -e "${RED}错误: 构建类型必须是 debug 或 release${NC}"
    echo "用法: ./build.sh [debug|release]"
    exit 1
fi

# 检查前置条件
echo "[1/5] 检查环境..."

if [ ! -d "app/libs" ]; then
    echo -e "${RED}错误: 缺少 app/libs 目录${NC}"
    echo "请从 UniApp 离线 SDK 复制 AAR 文件到 app/libs/"
    exit 1
fi

AAR_COUNT=$(find app/libs -name "*.aar" 2>/dev/null | wc -l)
if [ "$AAR_COUNT" -eq 0 ]; then
    echo -e "${YELLOW}警告: app/libs 目录下没有 AAR 文件${NC}"
    echo "请从 UniApp 离线 SDK 复制 AAR 文件"
fi

if [ ! -f "plugins/uts-kotlin-gradle-plugin-0.0.1.jar" ]; then
    echo -e "${YELLOW}警告: 缺少 UTS 编译插件${NC}"
    echo "将使用 Kotlin 混编方案（备选）"
fi

# 检查前端资源
echo ""
echo "[2/5] 检查前端资源..."
if [ ! -d "app/src/main/assets/apps" ]; then
    echo -e "${YELLOW}警告: 缺少前端资源${NC}"
    echo "请在 HBuilderX 中执行：发行 → 原生 App-本地打包 → 生成本地打包 App 资源"
    echo "然后将生成的资源复制到 app/src/main/assets/apps/"
fi

# 赋予 Gradle 执行权限
echo ""
echo "[3/5] 配置 Gradle..."
if [ ! -f "./gradlew" ]; then
    echo "创建 Gradle Wrapper..."
    gradle wrapper --gradle-version 8.4
fi
chmod +x ./gradlew

# 清理旧构建
echo ""
echo "[4/5] 清理旧构建..."
./gradlew clean

# 开始构建
echo ""
echo "[5/5] 开始构建 $BUILD_TYPE 版本..."
if [ "$BUILD_TYPE" == "debug" ]; then
    ./gradlew :app:assembleDebug
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
else
    ./gradlew :app:assembleRelease
    APK_PATH="app/build/outputs/apk/release/app-release.apk"
fi

# 检查构建结果
if [ -f "$APK_PATH" ]; then
    echo ""
    echo -e "${GREEN}=== 构建成功! ===${NC}"
    echo "APK 路径: $APK_PATH"
    echo "APK 大小: $(du -h "$APK_PATH" | cut -f1)"
    echo ""
    echo "安装到设备:"
    echo "  adb install -r $APK_PATH"
else
    echo ""
    echo -e "${RED}=== 构建失败 ===${NC}"
    echo "请检查上面的错误日志"
    exit 1
fi
