#!/bin/bash

echo "🚀 推送代码到 GitHub"
echo "================================"
echo ""

cd /Users/admin/Projects/voiceMemory

echo "📝 当前分支:"
git branch

echo ""
echo "📦 准备推送到: https://github.com/layjoy/layjoy.git"
echo ""
echo "⚠️  需要 GitHub 认证"
echo ""
echo "请选择认证方式:"
echo ""
echo "1️⃣  使用 Personal Access Token (推荐)"
echo "   - 访问: https://github.com/settings/tokens"
echo "   - 点击 'Generate new token (classic)'"
echo "   - 勾选 'repo' 权限"
echo "   - 生成后复制 token"
echo "   - 用户名输入: layjoy"
echo "   - 密码输入: 你的 token"
echo ""
echo "2️⃣  使用 SSH 密钥"
echo "   - 访问: https://github.com/settings/keys"
echo "   - 添加这个公钥:"
echo "   ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIBQmnaBHfva6KwnCslh5G6ZcWr8ZXjFMeU8cmZoekG4u"
echo ""
echo "================================"
echo ""
echo "准备好后，运行:"
echo "  git push -u origin main"
echo ""
