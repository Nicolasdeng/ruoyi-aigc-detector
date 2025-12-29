@echo off
chcp 65001 >nul
echo ====================================
echo   下载 ResNet50 ONNX 模型
echo ====================================
echo.
echo 模型信息：
echo - 名称：ResNet50-v2
echo - 大小：约97MB
echo - 来源：ONNX Model Zoo
echo - 用途：AI图片检测
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
echo 提示：如果不需要代理，请按 Ctrl+C 取消，然后编辑此脚本删除代理设置
echo.
pause

echo.
echo ====================================
echo 开始下载模型文件...
echo ====================================
echo.
echo 正在下载 ResNet50 模型（约97MB）...
echo 下载地址：https://github.com/onnx/models/raw/main/vision/classification/resnet/model/resnet50-v2-7.onnx
echo.

curl -L --progress-bar ^
     --proxy %HTTP_PROXY% ^
     --connect-timeout 30 ^
     --max-time 600 ^
     -o "models\ai-image-detector\model.onnx" ^
     "https://github.com/onnx/models/raw/main/vision/classification/resnet/model/resnet50-v2-7.onnx"

if %errorlevel% equ 0 (
    echo.
    echo ====================================
    echo ✓ 模型下载成功！
    echo ====================================
    echo.
    
    REM 检查文件大小
    for %%A in ("models\ai-image-detector\model.onnx") do (
        set size=%%~zA
        set /a sizeMB=%%~zA/1024/1024
    )
    echo 文件大小: %sizeMB% MB
    echo 文件路径: models\ai-image-detector\model.onnx
    echo.
    
    goto success
) else (
    echo.
    echo ====================================
    echo × 下载失败！
    echo ====================================
    echo.
    echo 可能的原因：
    echo 1. 网络连接问题
    echo 2. 代理配置错误（如果使用Clash，确保端口是7897）
    echo 3. GitHub访问受限
    echo.
    echo 解决方案：
    echo 1. 检查网络连接
    echo 2. 检查代理设置
    echo 3. 使用浏览器手动下载：
    echo    https://github.com/onnx/models/tree/main/vision/classification/resnet
    echo    下载 resnet50-v2-7.onnx 并重命名为 model.onnx
    echo    放到：models\ai-image-detector\model.onnx
    echo.
    goto end
)

:success
echo ====================================
echo 配置步骤
echo ====================================
echo.
echo 1. 修改 application.yml 添加以下配置：
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
echo 3. 测试图片检测功能
echo.
echo ====================================
echo 启动检查
echo ====================================
echo.
echo 服务启动后，查看日志应该看到：
echo   [INFO] ONNX模型加载成功！输入尺寸: 224x224
echo.
echo 如果看到错误，请查看：
echo   - MODEL_DOWNLOAD_SOLUTIONS.md
echo   - AI_DETECTION_TROUBLESHOOTING.md
echo.

:end
echo.
echo 按任意键退出...
pause >nul
