#!/bin/bash

echo "🔐 配置 GitHub 认证"
echo "================================"
echo ""

cd /Users/admin/Projects/voiceMemory

# 提示用户输入
echo "请输入你的 GitHub Personal Access Token:"
echo "(访问 https://github.com/settings/tokens 创建)"
echo ""
read -s -p "Token: " TOKEN
echo ""
echo ""

# 配置 Git 凭证
git config credential.helper store

# 使用 token 推送
echo "https://layjoy:${TOKEN}@github.com" > ~/.git-credentials

echo "✅ 凭证已保存"
echo ""
echo "🚀 开始推送..."
echo ""

git push -u origin main

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 推送成功！"
    echo ""
    echo "📱 查看编译进度:"
    echo "   https://github.com/layjoy/layjoy/actions"
    echo ""
    echo "⏱️  预计 5-10 分钟后可以下载 APK"
else
    echo ""
    echo "❌ 推送失败，请检查 token 是否正确"
fi
