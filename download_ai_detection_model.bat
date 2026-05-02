@echo off
chcp 65001 >nul
echo ========================================
echo AI图片检测模型下载工具 v1.0
echo ========================================
echo.

REM 设置模型目录
set MODEL_DIR=models\ai-image-detector
if not exist "%MODEL_DIR%" mkdir "%MODEL_DIR%"

echo [提示] 本脚本将帮助您下载AI图片检测模型
echo.
echo 模型信息:
echo - 名称: AI Image Detector (umm-maybe)
echo - 大小: 约 500MB
echo - 准确率: 85%%+
echo - 格式: ONNX
echo.

echo ========================================
echo 下载方式选择
echo ========================================
echo.
echo 1. 使用国内镜像站 (推荐，无需代理)
echo 2. 使用Hugging Face官方 (需要代理)
echo 3. 手动下载指引
echo 4. 退出
echo.

set /p choice="请选择下载方式 (1-4): "

if "%choice%"=="1" goto mirror_download
if "%choice%"=="2" goto hf_download
if "%choice%"=="3" goto manual_guide
if "%choice%"=="4" goto end

echo 无效选择，请重新运行脚本
pause
goto end

:mirror_download
echo.
echo [方式1] 使用国内镜像站下载
echo ========================================
echo.
echo 正在准备下载...
echo.

REM 检查curl是否可用
where curl >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] 未找到curl命令
    echo 请安装curl或使用手动下载方式
    pause
    goto end
)

echo 开始下载模型文件...
echo 下载地址: https://hf-mirror.com/umm-maybe/AI-image-detector/resolve/main/model.onnx
echo.

curl -L -o "%MODEL_DIR%\model.onnx" "https://hf-mirror.com/umm-maybe/AI-image-detector/resolve/main/model.onnx"

if %errorlevel% equ 0 (
    echo.
    echo [成功] 模型下载完成！
    goto verify_model
) else (
    echo.
    echo [失败] 下载失败，请检查网络连接或尝试其他下载方式
    pause
    goto end
)

:hf_download
echo.
echo [方式2] 使用Hugging Face官方下载
echo ========================================
echo.
echo [注意] 此方式需要配置代理才能访问
echo.

REM 检查curl是否可用
where curl >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] 未找到curl命令
    echo 请安装curl或使用手动下载方式
    pause
    goto end
)

set /p use_proxy="是否使用代理? (Y/N): "
if /i "%use_proxy%"=="Y" (
    set /p proxy_addr="请输入代理地址 (例如: http://127.0.0.1:7890): "
    echo 使用代理: !proxy_addr!
    
    curl -x !proxy_addr! -L -o "%MODEL_DIR%\model.onnx" "https://huggingface.co/umm-maybe/AI-image-detector/resolve/main/model.onnx"
) else (
    curl -L -o "%MODEL_DIR%\model.onnx" "https://huggingface.co/umm-maybe/AI-image-detector/resolve/main/model.onnx"
)

if %errorlevel% equ 0 (
    echo.
    echo [成功] 模型下载完成！
    goto verify_model
) else (
    echo.
    echo [失败] 下载失败，请检查代理配置或尝试其他下载方式
    pause
    goto end
)

:manual_guide
echo.
echo [方式3] 手动下载指引
echo ========================================
echo.
echo 请按照以下步骤手动下载模型：
echo.
echo 步骤1: 选择一个下载地址
echo.
echo   国内镜像 (推荐):
echo   https://hf-mirror.com/umm-maybe/AI-image-detector/tree/main
echo.
echo   官方地址 (需代理):
echo   https://huggingface.co/umm-maybe/AI-image-detector/tree/main
echo.
echo 步骤2: 在页面中找到并下载 model.onnx 文件
echo.
echo 步骤3: 将下载的文件放到以下目录：
echo   %cd%\%MODEL_DIR%\
echo.
echo 步骤4: 确保文件名为 model.onnx
echo.
echo.
pause
echo.
goto verify_model

:verify_model
echo.
echo [验证] 正在检查模型文件...
echo ========================================
echo.

if exist "%MODEL_DIR%\model.onnx" (
    echo [✓] 模型文件存在
    
    REM 获取文件大小
    for %%A in ("%MODEL_DIR%\model.onnx") do set size=%%~zA
    
    REM 转换为MB
    set /a sizeMB=!size! / 1048576
    
    echo [✓] 文件大小: !sizeMB! MB
    echo [✓] 文件路径: %cd%\%MODEL_DIR%\model.onnx
    echo.
    
    if !sizeMB! LSS 50 (
        echo [警告] 文件大小异常，可能下载不完整
        echo        正常模型文件应该约为 500MB
        echo.
    ) else (
        echo [成功] 模型文件验证通过！
        echo.
        echo 接下来的步骤:
        echo 1. 在 application.yml 中配置启用 ONNX 检测器
        echo 2. 添加 DJL 相关的 Maven 依赖
        echo 3. 重启应用程序
        echo.
        echo 详细说明请查看: LOCAL_AI_IMAGE_DETECTION_GUIDE.md
    )
) else (
    echo [✗] 未找到模型文件
    echo.
    echo 请确保已将 model.onnx 放置在以下目录:
    echo %cd%\%MODEL_DIR%\
    echo.
)

echo.
echo ========================================
pause
goto end

:end
echo.
echo 感谢使用AI图片检测模型下载工具
exit /b
