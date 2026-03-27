#!/bin/bash
# 检查 UniApp 离线 SDK 配置

echo "=== 检查 UniApp 离线 SDK 配置 ==="
echo ""

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

SDK_DIR="SDK"
LIBS_DIR="$SDK_DIR/libs"

check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $1"
        return 0
    else
        echo -e "${RED}✗${NC} $1 (不存在)"
        return 1
    fi
}

check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $1"
        return 0
    else
        echo -e "${RED}✗${NC} $1 (不存在)"
        return 1
    fi
}

# 检查 SDK 目录
echo "[1/3] 检查 SDK 目录..."
check_dir "$SDK_DIR"
check_dir "$LIBS_DIR"

# 检查关键 AAR 文件
echo ""
echo "[2/3] 检查 UniApp 基础库..."
if [ -d "$LIBS_DIR" ]; then
    AAR_COUNT=$(find "$LIBS_DIR" -name "*.aar" 2>/dev/null | wc -l)
    echo "发现 AAR 文件数量: $AAR_COUNT"
    
    # 列出前 5 个 AAR 文件
    echo ""
    echo "AAR 文件列表:"
    ls -lh "$LIBS_DIR/"*.aar 2>/dev/null | head -5 || echo "(无)"
else
    echo -e "${YELLOW}跳过: SDK 目录不存在${NC}"
fi

# 检查前端资源
echo ""
echo "[3/3] 检查前端资源..."
ASSETS_DIR="app/src/main/assets/apps"
check_dir "$ASSETS_DIR"

if [ -d "$ASSETS_DIR" ]; then
    APP_COUNT=$(ls -1 "$ASSETS_DIR" 2>/dev/null | wc -l)
    echo "应用数量: $APP_COUNT"
    if [ $APP_COUNT -eq 0 ]; then
        echo -e "${YELLOW}警告: 没有前端资源，请运行 ./copy-resources.sh${NC}"
    fi
fi

# 总结
echo ""
echo "=== 检查结果 ==="
if [ -d "$LIBS_DIR" ] && [ -d "$ASSETS_DIR" ] && [ "$(ls -1 $ASSETS_DIR 2>/dev/null | wc -l)" -gt 0 ]; then
    echo -e "${GREEN}✓ 配置完成，可以开始构建!${NC}"
    echo ""
    echo "运行: ./build.sh debug"
else
    echo -e "${YELLOW}⚠ 配置不完整，请按以下步骤操作:${NC}"
    echo ""
    if [ ! -d "$LIBS_DIR" ]; then
        echo "1. 下载 UniApp Android 离线 SDK"
        echo "   https://nativesupport.dcloud.net.cn/AppDocs/download/android.html"
        echo "2. 解压到 android/SDK/ 目录"
    fi
    if [ ! -d "$ASSETS_DIR" ] || [ "$(ls -1 $ASSETS_DIR 2>/dev/null | wc -l)" -eq 0 ]; then
        echo "3. 在 HBuilderX 中生成本地打包资源"
        echo "4. 运行 ./copy-resources.sh 复制资源"
    fi
fi
