#!/bin/bash

# Git 项目初始化脚本

echo "========================================"
echo "   FieldSurvey Git 项目初始化"
echo "========================================"
echo ""

# 检查git是否安装
if ! command -v git &> /dev/null; then
    echo "错误: Git未安装。请从 https://git-scm.com 下载并安装Git。"
    exit 1
fi

echo "✓ Git已安装"

# 初始化Git仓库
if [ ! -d .git ]; then
    echo ""
    echo "初始化Git仓库..."
    git init
    echo "✓ Git仓库已初始化"
else
    echo "✓ Git仓库已存在"
fi

# 添加所有文件到暂存区（除了.gitignore中的文件）
echo ""
echo "添加文件到Git..."
git add .
echo "✓ 文件已添加"

# 创建初始提交
echo ""
echo "创建初始提交..."
if git commit -m "Initial commit: FieldSurvey project setup"; then
    echo "✓ 初始提交已创建"
else
    echo "ℹ 没有新文件需要提交或仓库已有提交"
fi

# 显示状态
echo ""
echo "Git仓库状态:"
git status

echo ""
echo "========================================"
echo "   初始化完成！"
echo "========================================"
echo ""
echo "后续步骤:"
echo "1. 添加远程仓库: git remote add origin <repository-url>"
echo "2. 推送到远程: git push -u origin master"
echo ""

