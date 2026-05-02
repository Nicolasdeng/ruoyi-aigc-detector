# AI图片检测模型下载脚本 - 简化版
# 编码: UTF-8

Write-Host "========================================"
Write-Host "AI图片检测模型下载工具"
Write-Host "========================================"
Write-Host ""

# 创建模型目录
$ModelDir = "models\ai-image-detector"
if (-not (Test-Path $ModelDir)) {
    New-Item -ItemType Directory -Path $ModelDir -Force | Out-Null
    Write-Host "[信息] 已创建目录: $ModelDir"
}

Write-Host "[提示] 正在下载AI图片检测模型..."
Write-Host "[提示] 文件大小约500MB，请耐心等待..."
Write-Host ""

# 定义下载源列表
$sources = @(
    "https://hf-mirror.com/umm-maybe/AI-image-detector/resolve/main/model.onnx",
    "https://www.modelscope.cn/models/umm-maybe/AI-image-detector/resolve/main/model.onnx",
    "https://huggingface.co/umm-maybe/AI-image-detector/resolve/main/model.onnx"
)

$OutputFile = Join-Path $ModelDir "model.onnx"
$success = $false

foreach ($url in $sources) {
    Write-Host "[尝试] 从以下地址下载:"
    Write-Host "       $url"
    Write-Host ""
    
    try {
        $ProgressPreference = 'Continue'
        Invoke-WebRequest -Uri $url -OutFile $OutputFile -UseBasicParsing -TimeoutSec 600
        
        if (Test-Path $OutputFile) {
            $fileSize = (Get-Item $OutputFile).Length / 1MB
            $fileSizeMB = [math]::Round($fileSize, 2)
            
            Write-Host ""
            Write-Host "[成功] 下载完成!"
            Write-Host "[验证] 文件大小: $fileSizeMB MB"
            
            if ($fileSizeMB -ge 50) {
                Write-Host "[验证] 文件验证通过!"
                $success = $true
                break
            } else {
                Write-Host "[警告] 文件大小异常，删除并尝试下一个源..."
                Remove-Item $OutputFile -Force
            }
        }
    }
    catch {
        Write-Host "[失败] $($_.Exception.Message)"
        if (Test-Path $OutputFile) {
            Remove-Item $OutputFile -Force
        }
    }
    Write-Host ""
}

Write-Host "========================================"
if ($success) {
    Write-Host ""
    Write-Host "[完成] 模型下载成功！"
    Write-Host ""
    Write-Host "后续步骤:"
    Write-Host "1. 在 application.yml 中启用 ONNX 检测器"
    Write-Host "2. 添加 DJL 的 Maven 依赖"
    Write-Host "3. 重启应用"
    Write-Host ""
    Write-Host "详细说明: LOCAL_AI_IMAGE_DETECTION_GUIDE.md"
} else {
    Write-Host ""
    Write-Host "[失败] 所有下载源均失败"
    Write-Host ""
    Write-Host "建议:"
    Write-Host "1. 检查网络连接"
    Write-Host "2. 手动下载: https://hf-mirror.com/umm-maybe/AI-image-detector"
    Write-Host "3. 将 model.onnx 放到: $ModelDir\"
}
Write-Host ""
Write-Host "========================================"
Write-Host ""
Write-Host "按任意键退出..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
