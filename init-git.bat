@echo off
REM Git 项目初始化脚本

echo ========================================
echo   FieldSurvey Git 项目初始化
echo ========================================
echo.

REM 检查git是否安装
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Git未安装。请从 https://git-scm.com 下载并安装Git。
    pause
    exit /b 1
)

echo ✓ Git已安装

REM 初始化Git仓库
if not exist .git (
    echo.
    echo 初始化Git仓库...
    git init
    echo ✓ Git仓库已初始化
) else (
    echo ✓ Git仓库已存在
)

REM 添加所有文件到暂存区（除了.gitignore中的文件）
echo.
echo 添加文件到Git...
git add .
echo ✓ 文件已添加

REM 创建初始提交
echo.
echo 创建初始提交...
git commit -m "Initial commit: FieldSurvey project setup"
if %errorlevel% equ 0 (
    echo ✓ 初始提交已创建
) else (
    echo ℹ 没有新文件需要提交或仓库已有提交
)

REM 显示状态
echo.
echo Git仓库状态:
git status

echo.
echo ========================================
echo   初始化完成！
echo ========================================
echo.
echo 后续步骤:
echo 1. 添加远程仓库: git remote add origin <repository-url>
echo 2. 推送到远程: git push -u origin master
echo.
pause

