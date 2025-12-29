@echo off
chcp 65001 >nul
echo ====================================
echo   AI图片检测模型下载脚本
echo ====================================
echo.
echo 请确保：
echo 1. 已开启Clash代理
echo 2. 浏览器能访问Google
echo.
pause

echo.
echo 正在创建模型目录...
if not exist "models\ai-image-detector" mkdir "models\ai-image-detector"

echo.
echo 正在设置代理...
set HTTP_PROXY=http://127.0.0.1:7897
set HTTPS_PROXY=http://127.0.0.1:7897

echo 代理已设置为: %HTTP_PROXY%
echo.

echo ====================================
echo 开始下载模型文件...
echo ====================================
echo.
echo 方法1：使用git下载（推荐）
echo 如果您安装了git，将自动下载模型
echo.

where git >nul 2>nul
if %errorlevel% equ 0 (
    echo 检测到git，开始克隆模型仓库...
    cd models
    git clone https://huggingface.co/umm-maybe/AI-image-detector ai-image-detector
    if %errorlevel% equ 0 (
        echo.
        echo ✓ 模型下载成功！
        echo 模型位置: models\ai-image-detector\
        echo.
        goto :success
    ) else (
        echo.
        echo × git下载失败，尝试方法2...
        cd ..
    )
) else (
    echo git未安装，跳过方法1
)

echo.
echo ====================================
echo 方法2：使用curl下载（备选）
echo ====================================
echo.
echo 正在下载model.onnx文件（约500MB）...
echo 下载地址: https://huggingface.co/umm-maybe/AI-image-detector/resolve/main/model.onnx
echo.

curl -L -o "models\ai-image-detector\model.onnx" --proxy %HTTP_PROXY% "https://huggingface.co/umm-maybe/AI-image-detector/resolve/main/model.onnx"

if %errorlevel% equ 0 (
    echo.
    echo 正在下载config.json...
    curl -L -o "models\ai-image-detector\config.json" --proxy %HTTP_PROXY% "https://huggingface.co/umm-maybe/AI-image-detector/resolve/main/config.json"
    
    echo.
    echo ✓ 模型下载成功！
    echo 模型位置: models\ai-image-detector\
    echo.
    goto :success
) else (
    echo.
    echo × curl下载失败
    goto :manual
)

:success
echo ====================================
echo 下一步操作：
echo ====================================
echo.
echo 1. 在 application.yml 中添加配置：
echo.
echo ai:
echo   detection:
echo     onnx:
echo       enabled: true
echo       model-path: models/ai-image-detector/model.onnx
echo       input-size: 224
echo.
echo 2. 重启后端服务
echo.
echo 3. 测试检测功能
echo.
pause
exit /b 0

:manual
echo ====================================
echo 自动下载失败，请手动下载
echo ====================================
echo.
echo 请按以下步骤手动下载模型：
echo.
echo 1. 确保Clash代理已开启
echo.
echo 2. 浏览器访问:
echo    https://huggingface.co/umm-maybe/AI-image-detector/tree/main
echo.
echo 3. 下载以下文件到 models\ai-image-detector\ 目录:
echo    - model.onnx （必需，约500MB）
echo    - config.json （可选）
echo.
echo 4. 确认文件路径:
echo    models\ai-image-detector\model.onnx
echo.
echo 5. 配置 application.yml 并重启服务
echo.
pause
exit /b 1
