# PowerShell脚本 - 使用代理下载AI图片检测模型

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AI图片检测模型下载工具 (PowerShell版)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 配置
$proxyUrl = "http://127.0.0.1:7897"
$modelUrl = "https://huggingface.co/umm-maybe/AI-image-detector/resolve/main/model.onnx"
$outputPath = "models\ai-image-detector\model.onnx"

Write-Host "[信息] 下载配置:" -ForegroundColor Yellow
Write-Host "  代理地址: $proxyUrl" -ForegroundColor White
Write-Host "  模型地址: $modelUrl" -ForegroundColor White
Write-Host "  保存路径: $outputPath" -ForegroundColor White
Write-Host ""

# 创建目录
$modelDir = "models\ai-image-detector"
if (-not (Test-Path $modelDir)) {
    Write-Host "[1/3] 创建目录: $modelDir" -ForegroundColor Green
    New-Item -ItemType Directory -Force -Path $modelDir | Out-Null
} else {
    Write-Host "[1/3] 目录已存在: $modelDir" -ForegroundColor Green
}

# 设置代理
Write-Host "[2/3] 配置代理并开始下载..." -ForegroundColor Green
Write-Host "      模型文件约500MB，请耐心等待..." -ForegroundColor Yellow
Write-Host ""

try {
    # 使用Invoke-WebRequest并配置代理
    $ProgressPreference = 'Continue'  # 显示进度条
    
    Invoke-WebRequest -Uri $modelUrl `
        -OutFile $outputPath `
        -Proxy $proxyUrl `
        -UseBasicParsing `
        -TimeoutSec 1800
    
    Write-Host ""
    Write-Host "[3/3] 下载完成！" -ForegroundColor Green
    Write-Host ""
    
    # 验证文件
    if (Test-Path $outputPath) {
        $fileSize = (Get-Item $outputPath).Length
        $fileSizeMB = [math]::Round($fileSize / 1MB, 2)
        
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "文件验证" -ForegroundColor Cyan
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "[✓] 文件存在" -ForegroundColor Green
        Write-Host "[✓] 文件大小: $fileSizeMB MB" -ForegroundColor Green
        Write-Host "[✓] 文件路径: $((Get-Item $outputPath).FullName)" -ForegroundColor Green
        Write-Host ""
        
        if ($fileSizeMB -lt 50) {
            Write-Host "[警告] 文件大小异常，可能下载不完整" -ForegroundColor Red
            Write-Host "       正常模型文件应该约为500MB" -ForegroundColor Red
        } else {
            Write-Host "[成功] 模型文件下载完成并验证通过！" -ForegroundColor Green
            Write-Host ""
            Write-Host "接下来的步骤:" -ForegroundColor Yellow
            Write-Host "1. 在 application.yml 中启用 ONNX 检测器" -ForegroundColor White
            Write-Host "2. 添加 DJL 相关的 Maven 依赖" -ForegroundColor White
            Write-Host "3. 创建 OnnxModelDetector.java 类" -ForegroundColor White
            Write-Host "4. 重启应用程序" -ForegroundColor White
            Write-Host ""
            Write-Host "详细说明请查看: QUICK_START_LOCAL_AI_DETECTION.md" -ForegroundColor Cyan
        }
    }
    
} catch {
    Write-Host ""
    Write-Host "[错误] 下载失败！" -ForegroundColor Red
    Write-Host "错误信息: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "可能的原因:" -ForegroundColor Yellow
    Write-Host "1. 代理地址不正确或代理未启动" -ForegroundColor White
    Write-Host "2. 网络连接问题" -ForegroundColor White
    Write-Host "3. Hugging Face 服务器繁忙" -ForegroundColor White
    Write-Host ""
    Write-Host "建议:" -ForegroundColor Yellow
    Write-Host "1. 检查 Clash 是否正在运行 (端口7897)" -ForegroundColor White
    Write-Host "2. 尝试使用国内镜像: https://hf-mirror.com" -ForegroundColor White
    Write-Host "3. 或使用浏览器手动下载" -ForegroundColor White
}

Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
