@echo off
chcp 65001 >nul
echo ====================================
echo   AI图片检测模型下载脚本 V2
echo ====================================
echo.
echo 使用替代模型方案
echo.
echo 请确保：
echo 1. 已开启Clash代理
echo 2. 浏览器能访问Google/Hugging Face
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
echo 可用的AI图片检测模型选项：
echo ====================================
echo.
echo 方案1：使用 Salesforce CLIP 模型（推荐）
echo   - 模型：openai/clip-vit-base-patch32
echo   - 优点：成熟稳定，准确率高
echo   - 大小：约150MB
echo.
echo 方案2：使用 Google AI 图片分类模型
echo   - 模型：google/vit-base-patch16-224
echo   - 优点：速度快，准确度好
echo   - 大小：约350MB
echo.
echo 方案3：手动下载其他模型
echo.

set /p choice="请选择方案 (1/2/3): "

if "%choice%"=="1" goto clip_model
if "%choice%"=="2" goto vit_model
if "%choice%"=="3" goto manual
goto end

:clip_model
echo.
echo ====================================
echo 下载 CLIP 模型
echo ====================================
echo.
echo 正在下载模型文件...
echo.

REM 下载 CLIP 模型的主要文件
curl -L -o "models\ai-image-detector\pytorch_model.bin" --proxy %HTTP_PROXY% "https://huggingface.co/openai/clip-vit-base-patch32/resolve/main/pytorch_model.bin"

if %errorlevel% equ 0 (
    echo.
    echo 正在下载配置文件...
    curl -L -o "models\ai-image-detector\config.json" --proxy %HTTP_PROXY% "https://huggingface.co/openai/clip-vit-base-patch32/resolve/main/config.json"
    
    echo.
    echo 正在下载预处理配置...
    curl -L -o "models\ai-image-detector\preprocessor_config.json" --proxy %HTTP_PROXY% "https://huggingface.co/openai/clip-vit-base-patch32/resolve/main/preprocessor_config.json"
    
    echo.
    echo ✓ CLIP 模型下载成功！
    echo.
    goto convert_note
) else (
    echo × 下载失败
    goto manual
)

:vit_model
echo.
echo ====================================
echo 下载 ViT 模型
echo ====================================
echo.
echo 正在下载模型文件...
echo.

curl -L -o "models\ai-image-detector\pytorch_model.bin" --proxy %HTTP_PROXY% "https://huggingface.co/google/vit-base-patch16-224/resolve/main/pytorch_model.bin"

if %errorlevel% equ 0 (
    echo.
    echo 正在下载配置文件...
    curl -L -o "models\ai-image-detector\config.json" --proxy %HTTP_PROXY% "https://huggingface.co/google/vit-base-patch16-224/resolve/main/config.json"
    
    echo.
    echo 正在下载预处理配置...
    curl -L -o "models\ai-image-detector\preprocessor_config.json" --proxy %HTTP_PROXY% "https://huggingface.co/google/vit-base-patch16-224/resolve/main/preprocessor_config.json"
    
    echo.
    echo ✓ ViT 模型下载成功！
    echo.
    goto convert_note
) else (
    echo × 下载失败
    goto manual
)

:convert_note
echo ====================================
echo 重要提示：需要转换为ONNX格式
echo ====================================
echo.
echo 下载的是 PyTorch 格式，需要转换为 ONNX 格式才能使用。
echo.
echo 转换步骤：
echo 1. 安装 Python 和相关依赖
echo 2. 运行转换脚本：
echo    python convert_pytorch_to_onnx.py
echo.
echo 或者，您可以直接下载已转换好的 ONNX 模型。
echo.
goto success

:manual
echo.
echo ====================================
echo 推荐手动下载方案
echo ====================================
echo.
echo 由于原模型不存在，推荐以下替代方案：
echo.
echo 方案A：使用现成的 ONNX 模型（推荐）
echo ----------------------------------------
echo 1. 访问：https://github.com/onnx/models
echo 2. 选择图片分类模型，如：
echo    - ResNet50
echo    - MobileNet
echo    - EfficientNet
echo.
echo 下载后放到：models\ai-image-detector\model.onnx
echo.
echo 方案B：使用其他 AI 检测服务
echo ----------------------------------------
echo 1. 配置 Hugging Face API（需要 token）
echo 2. 使用本地特征检测器（已内置）
echo.
echo 方案C：自己训练模型
echo ----------------------------------------
echo 1. 准备数据集（AI图片 + 真实图片）
echo 2. 训练分类模型
echo 3. 转换为 ONNX 格式
echo.
goto end

:success
echo ====================================
echo 下一步操作
echo ====================================
echo.
echo 1. 如果下载的是 PyTorch 模型，需要转换：
echo    python convert_pytorch_to_onnx.py
echo.
echo 2. 或者直接下载 ONNX 模型：
echo    访问 https://github.com/onnx/models
echo.
echo 3. 配置 application.yml：
echo.
echo    ai:
echo      detection:
echo        onnx:
echo          enabled: true
echo          model-path: models/ai-image-detector/model.onnx
echo.
echo 4. 重启服务
echo.

:end
pause
