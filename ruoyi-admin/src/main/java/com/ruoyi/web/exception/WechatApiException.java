package com.ruoyi.web.exception;

/**
 * 微信API调用异常
 * 当调用微信API失败时抛出此异常
 * 
 * @author ruoyi
 */
public class WechatApiException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 微信API接口地址
     */
    private String apiUrl;
    
    /**
     * 微信错误码
     */
    private Integer errcode;
    
    /**
     * 微信错误信息
     */
    private String errmsg;
    
    /**
     * 请求参数
     */
    private String requestParams;
    
    /**
     * 响应内容
     */
    private String responseBody;
    
    public WechatApiException(String message) {
        super(message);
    }
    
    public WechatApiException(String message, String apiUrl, Integer errcode, String errmsg) {
        super(message);
        this.apiUrl = apiUrl;
        this.errcode = errcode;
        this.errmsg = errmsg;
    }
    
    public WechatApiException(String message, String apiUrl, Integer errcode, String errmsg, String requestParams, String responseBody) {
        super(message);
        this.apiUrl = apiUrl;
        this.errcode = errcode;
        this.errmsg = errmsg;
        this.requestParams = requestParams;
        this.responseBody = responseBody;
    }
    
    public WechatApiException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public WechatApiException(String message, String apiUrl, Integer errcode, String errmsg, Throwable cause) {
        super(message, cause);
        this.apiUrl = apiUrl;
        this.errcode = errcode;
        this.errmsg = errmsg;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
    
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    public Integer getErrcode() {
        return errcode;
    }
    
    public void setErrcode(Integer errcode) {
        this.errcode = errcode;
    }
    
    public String getErrmsg() {
        return errmsg;
    }
    
    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
    
    public String getRequestParams() {
        return requestParams;
    }
    
    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
    
    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
    
    @Override
    public String toString() {
        return "WechatApiException{" +
                "message=" + getMessage() +
                ", apiUrl='" + apiUrl + '\'' +
                ", errcode=" + errcode +
                ", errmsg='" + errmsg + '\'' +
                ", requestParams='" + requestParams + '\'' +
                ", responseBody='" + responseBody + '\'' +
                '}';
    }
}
