#!/bin/bash
# 初始化设置脚本

set -e

echo "=== 环信 UniAppX Android 离线打包环境初始化 ==="
echo ""

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 检查 Android Studio
if ! command -v studio &> /dev/null; then
    if ! command -v /Applications/Android\ Studio.app/Contents/MacOS/studio &> /dev/null; then
        echo -e "${YELLOW}警告: 未检测到 Android Studio${NC}"
        echo "请确保 Android Studio 已安装并添加到 PATH"
    fi
fi

# 检查 Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}错误: 未检测到 Java${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d '"' -f 2)
echo "Java 版本: $JAVA_VERSION"

# 创建必要目录
echo ""
echo "创建目录结构..."
mkdir -p app/libs
mkdir -p app/src/main/assets/apps
mkdir -p uni_modules/easemob-im/src/main/java
mkdir -p plugins

echo ""
echo -e "${GREEN}目录结构创建完成!${NC}"
echo ""

# 检查关键文件
echo "检查关键文件..."
FILES_OK=true

if [ ! -f "build.gradle" ]; then
    echo -e "${RED}缺少 build.gradle${NC}"
    FILES_OK=false
fi

if [ ! -f "settings.gradle" ]; then
    echo -e "${RED}缺少 settings.gradle${NC}"
    FILES_OK=false
fi

if [ ! -f "app/build.gradle" ]; then
    echo -e "${RED}缺少 app/build.gradle${NC}"
    FILES_OK=false
fi

if [ "$FILES_OK" = false ]; then
    echo ""
    echo -e "${RED}关键文件缺失，请重新克隆项目${NC}"
    exit 1
fi

# 创建空占位文件
touch app/src/main/assets/apps/.gitkeep
touch app/libs/.gitkeep

echo ""
echo "=== 初始化完成 ==="
echo ""
echo "下一步操作:"
echo ""
echo "1. 下载 UniApp Android 离线 SDK:"
echo "   https://nativesupport.dcloud.net.cn/AppDocs/download/android.html"
echo ""
echo "2. 复制 UniApp 基础库 AAR 到 app/libs/:"
echo "   - uniappx-release.aar"
echo "   - lib.5plus.base-release.aar"
echo "   - 其他依赖 AAR"
echo ""
echo "3. 复制 UTS 编译插件到 plugins/ (可选，用于 UTS 编译):"
echo "   - uts-kotlin-compiler-plugin-0.0.1.jar"
echo "   - uts-kotlin-gradle-plugin-0.0.1.jar"
echo ""
echo "4. 在 HBuilderX 中生成本地打包资源，复制到 app/src/main/assets/apps/"
echo ""
echo "5. 运行构建:"
echo "   ./build.sh debug"
echo ""
