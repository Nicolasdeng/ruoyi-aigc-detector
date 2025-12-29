@echo off
chcp 65001 >nul
echo ====================================
echo   下载ONNX模型文件
echo ====================================
echo.
echo 正在使用代理: 127.0.0.1:7897
echo.

REM 创建目录
if not exist "models\ai-image-detector" mkdir "models\ai-image-detector"

echo 开始下载 model.onnx (约500MB)...
echo 这可能需要5-15分钟，请耐心等待...
echo.

REM 使用curl下载，设置代理和超时时间
curl -L --proxy http://127.0.0.1:7897 ^
     --connect-timeout 30 ^
     --max-time 1800 ^
     --progress-bar ^
     -o "models\ai-image-detector\model.onnx" ^
     "https://huggingface.co/umm-maybe/AI-image-detector/resolve/main/model.onnx"

if %errorlevel% equ 0 (
    echo.
    echo ✓ model.onnx 下载成功！
    echo.
    
    REM 检查文件大小
    for %%A in ("models\ai-image-detector\model.onnx") do (
        echo 文件大小: %%~zA 字节
    )
    
    echo.
    echo ====================================
    echo 下载完成！
    echo ====================================
    echo.
    echo 模型文件位置: models\ai-image-detector\model.onnx
    echo.
    echo 下一步：重启后端服务即可使用ONNX检测器
    echo.
    
) else (
    echo.
    echo × 下载失败！
    echo.
    echo 可能的原因：
    echo 1. Clash代理未开启或端口不是7897
    echo 2. 网络连接问题
    echo 3. Hugging Face访问受限
    echo.
    echo 建议：
    echo 1. 检查Clash是否正常运行
    echo 2. 确认代理端口是否为7897
    echo 3. 浏览器测试能否访问 https://huggingface.co
    echo.
)

pause
