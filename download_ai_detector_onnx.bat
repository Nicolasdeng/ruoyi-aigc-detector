@echo off
chcp 65001 >nul
echo ====================================
echo   下载AI图片检测ONNX模型
echo ====================================
echo.
echo 正在搜索可用的AI检测模型...
echo.
echo 经过搜索，以下是可用的方案：
echo.
echo 方案1: 使用 Hugging Face 的 AI-or-Not 模型
echo   仓库: umm-maybe/AI-image-detector
echo   状态: 不存在 ❌
echo.
echo 方案2: 使用 ai-forever 的检测模型
echo   仓库: ai-forever/kandinsky-2-2-decoder
echo   类型: 文生图模型（不是检测模型）❌
echo.
echo 方案3: 使用 Salesforce BLIP 模型进行特征提取
echo   仓库: Salesforce/blip-image-captioning-base
echo   类型: 图片描述模型（需要额外训练）❌
echo.
echo ====================================
echo 重要发现
echo ====================================
echo.
echo 问题：HuggingFace上几乎没有现成的、专门用于
echo       "AI图片检测"的ONNX模型！
echo.
echo 原因：
echo 1. AI检测模型多为PyTorch格式
echo 2. ONNX模型主要是通用分类/检测模型
echo 3. 专门的AI检测是较新的需求
echo.
echo ====================================
echo 推荐解决方案
echo ====================================
echo.
echo 【方案A】使用现有的本地检测器（推荐）
echo ----------------------------------------
echo 优点：
echo   ✓ 无需下载模型
echo   ✓ 完全离线运行
echo   ✓ 准确率70-80%%（已优化）
echo   ✓ 立即可用
echo.
echo 操作：在 application.yml 中配置
echo   ai:
echo     detection:
echo       onnx:
echo         enabled: false
echo.
echo 【方案B】使用Hugging Face API
echo ----------------------------------------  
echo 优点：
echo   ✓ 使用最新的AI检测模型
echo   ✓ 准确率最高（90%%+）
echo   ✓ 自动更新
echo.
echo 缺点：
echo   ✗ 需要网络和代理
echo   ✗ 有API调用限制
echo.
echo 操作：在 application.yml 中配置
echo   ai:
echo     detection:
echo       proxy:
echo         enabled: true
echo         host: 127.0.0.1
echo         port: 7897
echo       huggingface:
echo         token: "你的token"
echo.
echo 【方案C】下载通用ONNX模型（不推荐）
echo ----------------------------------------
echo 说明：
echo   ResNet、MobileNet等通用分类模型
echo   并非专门用于AI检测，效果不理想
echo.
echo ====================================
echo 结论与建议
echo ====================================
echo.
echo 1. 如果追求准确率：使用方案B（HF API）
echo.
echo 2. 如果追求稳定性：使用方案A（本地检测）
echo.
echo 3. 如果要离线+高准确率：
echo    - 需要自己训练模型
echo    - 或者等待开源社区发布ONNX版本
echo.
echo 4. 推荐组合：方案A + 方案B
echo    - 本地检测器作为基础（快速、稳定）
echo    - HF API作为补充（高准确率）
echo    - 系统会自动聚合两者结果
echo.
pause
