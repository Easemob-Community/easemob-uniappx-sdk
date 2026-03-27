#!/bin/bash
# 配置 UniApp Android 离线 SDK 脚本 (SDK 5.05)

set -e

echo "=== 配置 UniApp Android 离线 SDK (5.05) ==="
echo ""

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SDK_DIR="SDK"
SDK_SOURCE=""

# 查找 SDK 源目录
find_sdk_source() {
    # 检查常见路径
    local paths=(
        "/Users/neohuang/Desktop/WorkCommonUse/SDK/easemob-uniappx-sdk/Android-SDK@5.05.82597_20260319"
        "../Android-SDK@5.05.82597_20260319"
        "../../Android-SDK@5.05.82597_20260319"
    )
    
    for path in "${paths[@]}"; do
        if [ -d "$path/SDK" ]; then
            SDK_SOURCE="$path/SDK"
            return 0
        fi
    done
    
    return 1
}

# 步骤 1: 查找 SDK
echo "[1/5] 查找离线 SDK..."
if [ -n "$1" ]; then
    # 用户指定了路径
    if [ -d "$1/SDK" ]; then
        SDK_SOURCE="$1/SDK"
    elif [ -d "$1" ]; then
        SDK_SOURCE="$1"
    else
        echo -e "${RED}错误: 指定的路径不存在: $1${NC}"
        exit 1
    fi
    echo -e "${GREEN}使用指定路径: $SDK_SOURCE${NC}"
elif find_sdk_source; then
    echo -e "${GREEN}找到 SDK: $SDK_SOURCE${NC}"
else
    echo -e "${RED}未找到离线 SDK${NC}"
    echo ""
    echo "请确保已解压 SDK 到项目目录，或手动指定路径:"
    echo "  ./setup-offline-sdk.sh /path/to/Android-SDK@5.05.82597_20260319"
    echo ""
    echo "SDK 下载地址:"
    echo "  https://nativesupport.dcloud.net.cn/AppDocs/download/android.html"
    exit 1
fi

# 步骤 2: 复制 SDK 文件
echo ""
echo "[2/5] 复制 SDK 文件..."

# 清理旧的 SDK
if [ -d "$SDK_DIR" ]; then
    echo -e "${YELLOW}SDK 目录已存在，正在备份...${NC}"
    mv "$SDK_DIR" "${SDK_DIR}.backup.$(date +%Y%m%d%H%M%S)"
fi

# 创建目录
mkdir -p "$SDK_DIR/libs"
mkdir -p "$SDK_DIR/assets"

# 复制关键 AAR 文件（与官方示例一致）
echo "复制基础库 AAR..."
REQUIRED_AARS=(
    "lib.5plus.base-release.aar"
    "uniapp-v8-release.aar"
    "android-gif-drawable-1.2.29.aar"
    "breakpad-build-release.aar"
    "oaid_sdk_1.0.25.aar"
)

for aar in "${REQUIRED_AARS[@]}"; do
    if [ -f "$SDK_SOURCE/libs/$aar" ]; then
        cp "$SDK_SOURCE/libs/$aar" "$SDK_DIR/libs/"
        echo "  ✓ $aar"
    else
        echo -e "  ${YELLOW}✗ $aar (未找到)${NC}"
    fi
done

# 可选：复制 utsplugin-release.aar（UTS 插件支持）
if [ -f "$SDK_SOURCE/libs/utsplugin-release.aar" ]; then
    cp "$SDK_SOURCE/libs/utsplugin-release.aar" "$SDK_DIR/libs/"
    echo "  ✓ utsplugin-release.aar (UTS 插件支持)"
fi

# 可选：复制 install-apk-release.aar（3.8.7+）
if [ -f "$SDK_SOURCE/libs/install-apk-release.aar" ]; then
    cp "$SDK_SOURCE/libs/install-apk-release.aar" "$SDK_DIR/libs/"
    echo "  ✓ install-apk-release.aar"
fi

echo -e "${GREEN}AAR 文件复制完成${NC}"

# 步骤 3: 复制 assets
echo ""
echo "[3/5] 复制 assets..."
if [ -d "$SDK_SOURCE/assets" ]; then
    cp -r "$SDK_SOURCE/assets/"* "$SDK_DIR/assets/"
    echo -e "${GREEN}assets 复制完成${NC}"
else
    echo -e "${YELLOW}警告: SDK 中没有 assets 目录${NC}"
fi

# 步骤 4: 复制 data 到 app
echo ""
echo "[4/5] 复制 data 资源到 app..."
if [ -d "$SDK_SOURCE/assets/data" ]; then
    mkdir -p app/src/main/assets
    cp -r "$SDK_SOURCE/assets/data" app/src/main/assets/
    echo -e "${GREEN}data 资源复制完成${NC}"
else
    echo -e "${YELLOW}警告: 未找到 data 目录${NC}"
fi

# 步骤 5: 创建 debug.keystore
echo ""
echo "[5/5] 创建调试签名..."
if [ ! -f "app/debug.keystore" ]; then
    keytool -genkey -v -keystore app/debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -validity 10000 -dname "CN=Android Debug,O=Android,C=US" 2>/dev/null || echo "keytool 不可用，请手动创建签名"
else
    echo "调试签名已存在"
fi

# 总结
echo ""
echo "=== 配置完成 ==="
echo ""
echo "SDK 内容:"
ls -lh "$SDK_DIR/libs/"

echo ""
echo -e "${GREEN}✓ SDK 配置成功!${NC}"
echo ""
echo "下一步:"
echo "1. 申请 DCloud AppKey: https://dev.dcloud.net.cn/"
echo "2. 修改 app/src/main/AndroidManifest.xml 中的 dcloud_appkey"
echo "3. 在 HBuilderX 中生成本地打包资源"
echo "4. 运行 ./copy-resources.sh 复制前端资源"
echo "5. 运行 ./build.sh debug 构建 APK"
