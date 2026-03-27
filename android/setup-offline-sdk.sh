#!/bin/bash
# 配置 UniApp 离线 SDK 脚本

set -e

echo "=== 配置 UniApp Android 离线 SDK ==="
echo ""

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SDK_ZIP=""
SDK_DIR="SDK"

# 查找 SDK zip 文件
find_sdk_zip() {
    # 用户指定的路径
    if [ -f "/Users/neohuang/Downloads/5.05/Android-SDK@5.05.82597_20260319.zip" ]; then
        SDK_ZIP="/Users/neohuang/Downloads/5.05/Android-SDK@5.05.82597_20260319.zip"
        return 0
    fi
    
    # 在 Downloads 目录中搜索
    local found=$(find /Users/neohuang/Downloads -name "Android-SDK*.zip" -type f 2>/dev/null | head -1)
    if [ -n "$found" ]; then
        SDK_ZIP="$found"
        return 0
    fi
    
    return 1
}

# 步骤 1: 查找 SDK
echo "[1/5] 查找离线 SDK..."
if find_sdk_zip; then
    echo -e "${GREEN}找到 SDK: $SDK_ZIP${NC}"
else
    echo -e "${RED}未找到离线 SDK zip 文件${NC}"
    echo ""
    echo "请确保已下载 SDK 到以下位置之一:"
    echo "  - /Users/neohuang/Downloads/5.05/Android-SDK@5.05.82597_20260319.zip"
    echo "  - /Users/neohuang/Downloads/ 目录下的 Android-SDK*.zip"
    echo ""
    echo "或者手动指定路径:"
    echo "  ./setup-offline-sdk.sh /path/to/Android-SDK@xxx.zip"
    exit 1
fi

# 步骤 2: 解压 SDK
echo ""
echo "[2/5] 解压 SDK..."
if [ -d "$SDK_DIR" ]; then
    echo -e "${YELLOW}SDK 目录已存在，正在备份...${NC}"
    mv "$SDK_DIR" "${SDK_DIR}.backup.$(date +%Y%m%d%H%M%S)"
fi

mkdir -p "$SDK_DIR"
echo "解压 $SDK_ZIP 到 $SDK_DIR/"
unzip -q "$SDK_ZIP" -d "$SDK_DIR/"

# 处理 SDK 目录结构（有些版本会多套一层目录）
if [ -d "$SDK_DIR/Android-SDK@"* ]; then
    mv "$SDK_DIR"/Android-SDK@*/* "$SDK_DIR/"
    rm -rf "$SDK_DIR"/Android-SDK@*
fi

echo -e "${GREEN}解压完成${NC}"

# 步骤 3: 检查 SDK 结构
echo ""
echo "[3/5] 检查 SDK 结构..."
if [ ! -d "$SDK_DIR/libs" ]; then
    echo -e "${RED}错误: SDK 结构不正确，缺少 libs 目录${NC}"
    exit 1
fi

echo "SDK 内容:"
ls -la "$SDK_DIR/"

# 步骤 4: 复制 data 资源
echo ""
echo "[4/5] 复制 data 资源..."
if [ -d "$SDK_DIR/assets/data" ]; then
    mkdir -p app/src/main/assets
    cp -r "$SDK_DIR/assets/data" app/src/main/assets/
    echo -e "${GREEN}已复制 data 资源${NC}"
else
    echo -e "${YELLOW}警告: SDK 中没有 data 目录${NC}"
fi

# 步骤 5: 检查关键 AAR
echo ""
echo "[5/5] 检查关键 AAR 文件..."
REQUIRED_AARS=(
    "lib.5plus.base-release.aar"
    "uniapp-v8-release.aar"
    "utsplugin-release.aar"
)

MISSING=0
for aar in "${REQUIRED_AARS[@]}"; do
    if [ -f "$SDK_DIR/libs/$aar" ]; then
        echo -e "${GREEN}✓${NC} $aar"
    else
        echo -e "${YELLOW}✗${NC} $aar (不存在)"
        MISSING=$((MISSING + 1))
    fi
done

# 显示所有 AAR
echo ""
echo "所有 AAR 文件:"
ls -lh "$SDK_DIR/libs/"*.aar 2>/dev/null || echo "(无)"

# 总结
echo ""
echo "=== 配置完成 ==="
if [ $MISSING -eq 0 ]; then
    echo -e "${GREEN}✓ SDK 配置成功!${NC}"
else
    echo -e "${YELLOW}⚠ SDK 配置完成，但缺少部分 AAR 文件${NC}"
fi
echo ""
echo "下一步:"
echo "1. 申请 DCloud AppKey: https://nativesupport.dcloud.net.cn/AppDocs/usesdk/appkey"
echo "2. 修改 app/src/main/AndroidManifest.xml 中的 dcloud_appkey"
echo "3. 在 HBuilderX 中生成本地打包资源"
echo "4. 运行 ./copy-resources.sh 复制前端资源"
echo "5. 运行 ./build.sh debug 构建 APK"
