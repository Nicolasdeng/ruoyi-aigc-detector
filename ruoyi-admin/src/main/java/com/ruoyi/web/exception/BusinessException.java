package com.ruoyi.web.exception;

/**
 * 业务异常
 * 当业务逻辑处理失败时抛出此异常
 * 
 * @author ruoyi
 */
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 业务错误码
     */
    private String code;
    
    /**
     * 业务模块
     */
    private String module;
    
    /**
     * 业务操作
     */
    private String operation;
    
    /**
     * 错误数据
     */
    private Object errorData;
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(String code, String message, String module) {
        super(message);
        this.code = code;
        this.module = module;
    }
    
    public BusinessException(String code, String message, String module, String operation) {
        super(message);
        this.code = code;
        this.module = module;
        this.operation = operation;
    }
    
    public BusinessException(String code, String message, String module, String operation, Object errorData) {
        super(message);
        this.code = code;
        this.module = module;
        this.operation = operation;
        this.errorData = errorData;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public BusinessException(String code, String message, String module, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.module = module;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getModule() {
        return module;
    }
    
    public void setModule(String module) {
        this.module = module;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public void setOperation(String operation) {
        this.operation = operation;
    }
    
    public Object getErrorData() {
        return errorData;
    }
    
    public void setErrorData(Object errorData) {
        this.errorData = errorData;
    }
    
    @Override
    public String toString() {
        return "BusinessException{" +
                "message=" + getMessage() +
                ", code='" + code + '\'' +
                ", module='" + module + '\'' +
                ", operation='" + operation + '\'' +
                ", errorData=" + errorData +
                '}';
    }
}
