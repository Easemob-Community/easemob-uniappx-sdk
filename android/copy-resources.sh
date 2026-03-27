#!/bin/bash
# 复制 HBuilderX 生成的本地打包资源到 Android 工程

set -e

echo "=== 复制前端资源到 Android 工程 ==="
echo ""

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 检查 demo 项目资源路径
DEMO_RESOURCES="../demo/unpackage/resources/app-android"

if [ ! -d "$DEMO_RESOURCES" ]; then
    echo -e "${RED}错误: 找不到本地打包资源${NC}"
    echo "路径: $DEMO_RESOURCES"
    echo ""
    echo "请在 HBuilderX 中先执行："
    echo "  发行 → 原生 App-本地打包 → 生成本地打包 App 资源"
    exit 1
fi

echo "[1/3] 找到资源目录: $DEMO_RESOURCES"

# 检查 apps 目录
if [ ! -d "$DEMO_RESOURCES/apps" ]; then
    echo -e "${RED}错误: 资源目录结构不正确${NC}"
    exit 1
fi

# 清理旧资源
echo "[2/3] 清理旧资源..."
rm -rf app/src/main/assets/apps/*
mkdir -p app/src/main/assets/apps

# 复制新资源
echo "[3/3] 复制新资源..."
cp -r "$DEMO_RESOURCES/apps/"* app/src/main/assets/apps/

# 统计
APP_COUNT=$(ls -1 app/src/main/assets/apps/ | wc -l)
echo ""
echo -e "${GREEN}复制完成!${NC}"
echo "应用数量: $APP_COUNT"
echo ""
echo "资源列表:"
ls -la app/src/main/assets/apps/
