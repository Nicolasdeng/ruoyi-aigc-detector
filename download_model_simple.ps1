# Download AI Image Detection Model with Proxy
# Simple version without Chinese characters

$proxyUrl = "http://127.0.0.1:7897"
$modelUrl = "https://huggingface.co/umm-maybe/AI-image-detector/resolve/main/model.onnx"
$outputPath = "models\ai-image-detector\model.onnx"

Write-Host "========================================"
Write-Host "AI Image Detection Model Downloader"
Write-Host "========================================"
Write-Host ""
Write-Host "Proxy: $proxyUrl"
Write-Host "URL: $modelUrl"
Write-Host "Output: $outputPath"
Write-Host ""

# Create directory
$modelDir = "models\ai-image-detector"
if (-not (Test-Path $modelDir)) {
    New-Item -ItemType Directory -Force -Path $modelDir | Out-Null
    Write-Host "[1/3] Directory created: $modelDir"
} else {
    Write-Host "[1/3] Directory exists: $modelDir"
}

# Download with proxy
Write-Host "[2/3] Downloading model file (approx 500MB)..."
Write-Host "      Please wait, this may take several minutes..."
Write-Host ""

try {
    $ProgressPreference = 'Continue'
    
    Invoke-WebRequest -Uri $modelUrl `
        -OutFile $outputPath `
        -Proxy $proxyUrl `
        -UseBasicParsing `
        -TimeoutSec 1800
    
    Write-Host ""
    Write-Host "[3/3] Download completed!"
    Write-Host ""
    
    # Verify file
    if (Test-Path $outputPath) {
        $fileSize = (Get-Item $outputPath).Length
        $fileSizeMB = [math]::Round($fileSize / 1MB, 2)
        
        Write-Host "========================================"
        Write-Host "File Verification"
        Write-Host "========================================"
        Write-Host "[OK] File exists"
        Write-Host "[OK] File size: $fileSizeMB MB"
        Write-Host "[OK] File path: $((Get-Item $outputPath).FullName)"
        Write-Host ""
        
        if ($fileSizeMB -lt 50) {
            Write-Host "[WARNING] File size is abnormal, may be incomplete"
            Write-Host "          Normal model file should be about 500MB"
        } else {
            Write-Host "[SUCCESS] Model file downloaded and verified!"
            Write-Host ""
            Write-Host "Next steps:"
            Write-Host "1. Enable ONNX detector in application.yml"
            Write-Host "2. Add DJL Maven dependencies"
            Write-Host "3. Create OnnxModelDetector.java class"
            Write-Host "4. Restart the application"
            Write-Host ""
            Write-Host "See: QUICK_START_LOCAL_AI_DETECTION.md for details"
        }
    }
    
}
catch {
    Write-Host ""
    Write-Host "[ERROR] Download failed!"
    Write-Host "Error: $($_.Exception.Message)"
    Write-Host ""
    Write-Host "Possible causes:"
    Write-Host "1. Proxy address incorrect or proxy not running"
    Write-Host "2. Network connection issue"
    Write-Host "3. Hugging Face server busy"
    Write-Host ""
    Write-Host "Suggestions:"
    Write-Host "1. Check if Clash is running (port 7897)"
    Write-Host "2. Try mirror site: https://hf-mirror.com"
    Write-Host "3. Or download manually with browser"
}

Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
