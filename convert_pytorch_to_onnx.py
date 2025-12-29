#!/usr/bin/env python3
"""
将已下载的PyTorch模型转换为ONNX格式
适用于 umm-maybe/AI-image-detector 模型
"""
import torch
import os
import sys

def check_dependencies():
    """检查必要的依赖"""
    try:
        import torch
        import transformers
        import onnx
        print("✓ 所有依赖已安装")
        return True
    except ImportError as e:
        print(f"× 缺少依赖: {e}")
        print("\n请安装必要的库:")
        print("  pip install torch transformers onnx")
        return False

def convert_model():
    """转换模型"""
    model_dir = "models/ai-image-detector"
    pytorch_model_path = os.path.join(model_dir, "pytorch_model.bin")
    output_path = os.path.join(model_dir, "model.onnx")
    
    # 检查PyTorch模型是否存在
    if not os.path.exists(pytorch_model_path):
        print(f"× 未找到PyTorch模型: {pytorch_model_path}")
        print("\n请先下载模型文件")
        return False
    
    print(f"✓ 找到PyTorch模型: {pytorch_model_path}")
    model_size_mb = os.path.getsize(pytorch_model_path) / (1024 * 1024)
    print(f"  文件大小: {model_size_mb:.2f} MB")
    
    try:
        print("\n正在加载模型...")
        from transformers import AutoModelForImageClassification
        
        # 加载模型
        model = AutoModelForImageClassification.from_pretrained(model_dir)
        model.eval()
        
        print("✓ 模型加载成功")
        print(f"  模型类型: {type(model).__name__}")
        
        # 创建示例输入
        print("\n正在创建示例输入...")
        dummy_input = torch.randn(1, 3, 224, 224)
        
        print("\n开始转换为ONNX格式...")
        print("  这可能需要1-2分钟...")
        
        # 导出为ONNX
        torch.onnx.export(
            model,
            dummy_input,
            output_path,
            export_params=True,
            opset_version=12,
            do_constant_folding=True,
            input_names=['input'],
            output_names=['output'],
            dynamic_axes={
                'input': {0: 'batch_size'},
                'output': {0: 'batch_size'}
            },
            verbose=False
        )
        
        # 验证输出文件
        if os.path.exists(output_path):
            output_size_mb = os.path.getsize(output_path) / (1024 * 1024)
            print(f"\n✓ 转换成功！")
            print(f"  ONNX模型: {output_path}")
            print(f"  文件大小: {output_size_mb:.2f} MB")
            
            # 验证ONNX模型
            print("\n正在验证ONNX模型...")
            import onnx
            onnx_model = onnx.load(output_path)
            onnx.checker.check_model(onnx_model)
            print("✓ ONNX模型验证通过")
            
            return True
        else:
            print("\n× 转换失败：输出文件未生成")
            return False
            
    except Exception as e:
        print(f"\n× 转换失败: {e}")
        import traceback
        traceback.print_exc()
        return False

def main():
    print("=" * 60)
    print("PyTorch模型转ONNX格式 - 转换工具")
    print("=" * 60)
    print()
    
    # 检查依赖
    if not check_dependencies():
        return 1
    
    print()
    
    # 转换模型
    success = convert_model()
    
    if success:
        print("\n" + "=" * 60)
        print("转换完成！")
        print("=" * 60)
        print("\n下一步:")
        print("1. 重启后端服务")
        print("2. 检查日志确认ONNX模型加载成功")
        print("3. 测试AI图片检测功能")
        return 0
    else:
        print("\n" + "=" * 60)
        print("转换失败")
        print("=" * 60)
        return 1

if __name__ == "__main__":
    sys.exit(main())
