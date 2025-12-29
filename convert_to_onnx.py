#!/usr/bin/env python3
"""
将PyTorch模型转换为ONNX格式
"""
import torch
from transformers import AutoModel
import os

def convert_model_to_onnx():
    """转换模型到ONNX格式"""
    
    model_dir = "models/ai-image-detector"
    output_path = os.path.join(model_dir, "model.onnx")
    
    print(f"正在加载PyTorch模型: {model_dir}")
    
    try:
        # 加载模型
        model = AutoModel.from_pretrained(model_dir)
        model.eval()
        
        print("模型加载成功！")
        print(f"模型类型: {type(model)}")
        
        # 创建示例输入 (batch_size=1, channels=3, height=224, width=224)
        dummy_input = torch.randn(1, 3, 224, 224)
        
        print("\n开始转换为ONNX格式...")
        
        # 导出为ONNX
        torch.onnx.export(
            model,
            dummy_input,
            output_path,
            export_params=True,
            opset_version=11,
            do_constant_folding=True,
            input_names=['input'],
            output_names=['output'],
            dynamic_axes={
                'input': {0: 'batch_size'},
                'output': {0: 'batch_size'}
            }
        )
        
        print(f"\n✅ 转换成功！")
        print(f"ONNX模型已保存到: {output_path}")
        
        # 验证文件
        if os.path.exists(output_path):
            size_mb = os.path.getsize(output_path) / (1024 * 1024)
            print(f"文件大小: {size_mb:.2f} MB")
        
    except Exception as e:
        print(f"\n❌ 转换失败: {e}")
        print("\n建议：")
        print("1. 确保已安装必要的依赖: pip install torch transformers onnx")
        print("2. 或者直接下载已转换好的ONNX模型")
        return False
    
    return True

if __name__ == "__main__":
    print("=" * 60)
    print("PyTorch模型转ONNX格式转换工具")
    print("=" * 60)
    
    success = convert_model_to_onnx()
    
    if success:
        print("\n" + "=" * 60)
        print("转换完成！现在可以重启服务使用ONNX模型")
        print("=" * 60)
    else:
        print("\n如果转换失败，可以手动下载ONNX模型：")
        print("https://huggingface.co/umm-maybe/AI-image-detector")
