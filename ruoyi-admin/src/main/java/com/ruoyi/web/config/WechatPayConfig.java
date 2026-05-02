package com.ruoyi.web.config;

import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.AutoUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

/**
 * 微信支付配置类
 * 用于读取配置参数并初始化微信支付SDK客户端
 * 
 * @author ruoyi
 */
@Configuration
public class WechatPayConfig {

    /**
     * 微信支付商户号
     */
    @Value("${wechat.pay.mchId}")
    private String mchId;

    /**
     * API V3密钥
     */
    @Value("${wechat.pay.apiV3Key}")
    private String apiV3Key;

    /**
     * 商户证书序列号
     */
    @Value("${wechat.pay.mchSerialNo}")
    private String mchSerialNo;

    /**
     * 商户私钥文件路径
     */
    @Value("${wechat.pay.privateKeyPath}")
    private String privateKeyPath;

    /**
     * 支付回调通知URL
     */
    @Value("${wechat.pay.notifyUrl}")
    private String notifyUrl;

    /**
     * 退款回调通知URL
     */
    @Value("${wechat.pay.refundNotifyUrl}")
    private String refundNotifyUrl;

    /**
     * 获取商户号
     */
    public String getMchId() {
        return mchId;
    }

    /**
     * 获取API V3密钥
     */
    public String getApiV3Key() {
        return apiV3Key;
    }

    /**
     * 获取商户证书序列号
     */
    public String getMchSerialNo() {
        return mchSerialNo;
    }

    /**
     * 获取支付回调URL
     */
    public String getNotifyUrl() {
        return notifyUrl;
    }

    /**
     * 获取退款回调URL
     */
    public String getRefundNotifyUrl() {
        return refundNotifyUrl;
    }

    /**
     * 加载商户私钥
     * 
     * @return 商户私钥
     * @throws FileNotFoundException 文件不存在异常
     */
    private PrivateKey getPrivateKey() throws FileNotFoundException {
        return PemUtil.loadPrivateKey(new FileInputStream(privateKeyPath));
    }

    /**
     * 创建微信支付HTTP客户端
     * 用于发送微信支付API请求
     * 
     * @return CloseableHttpClient实例
     * @throws Exception 初始化异常
     */
    @Bean
    public CloseableHttpClient wechatPayHttpClient() throws Exception {
        // 加载商户私钥
        PrivateKey merchantPrivateKey = getPrivateKey();

        // 创建签名器
        PrivateKeySigner privateKeySigner = new PrivateKeySigner(mchSerialNo, merchantPrivateKey);

        // 创建身份认证对象
        WechatPay2Credentials wechatPay2Credentials = new WechatPay2Credentials(mchId, privateKeySigner);

        // 创建证书验证器（自动更新平台证书）
        AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
                wechatPay2Credentials,
                apiV3Key.getBytes(StandardCharsets.UTF_8));

        // 构建HTTP客户端
        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                .withMerchant(mchId, mchSerialNo, merchantPrivateKey)
                .withValidator(new WechatPay2Validator(verifier));

        // 返回可关闭的HTTP客户端
        return builder.build();
    }
}
