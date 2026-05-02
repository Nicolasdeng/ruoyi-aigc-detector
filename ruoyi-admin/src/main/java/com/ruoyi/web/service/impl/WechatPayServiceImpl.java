package com.ruoyi.web.service.impl;

import com.google.gson.Gson;
import com.ruoyi.web.domain.MembershipOrder;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付服务实现类
 * 
 * @author ruoyi
 */
@Service
public class WechatPayServiceImpl implements IWechatPayService {

    private static final Logger log = LoggerFactory.getLogger(WechatPayServiceImpl.class);

    @Autowired
    private CloseableHttpClient wechatPayHttpClient;

    @Value("${wechat.pay.mchId}")
    private String mchId;

    @Value("${wechat.pay.apiV3Key}")
    private String apiV3Key;

    @Value("${wechat.pay.notifyUrl}")
    private String notifyUrl;

    @Value("${wechat.pay.refundNotifyUrl}")
    private String refundNotifyUrl;

    @Value("${wechat.miniapp.appid}")
    private String appId;

    @Autowired
    private Verifier verifier;

    @Autowired
    private PrivateKey merchantPrivateKey;

    private static final String JSAPI_URL = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";
    private static final String QUERY_URL = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/";
    private static final String REFUND_URL = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";

    private final Gson gson = new Gson();

    /**
     * 创建微信支付订单（JSAPI统一下单）
     *
     * @param order 会员订单对象
     * @param openid 用户openid
     * @return 支付参数Map，包含小程序调起支付所需的参数
     * @throws Exception 创建支付订单异常
     */
    @Override
    public Map<String, String> createPayment(MembershipOrder order, String openid) throws Exception {
        // 从订单对象中提取信息
        String orderNo = order.getOrderNo();
        Long amount = order.getAmount(); // 订单金额（分）
        String description = "AI检测会员-" + order.getPackageType() + "套餐";
        
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("mchid", mchId);
            params.put("out_trade_no", orderNo);
            params.put("appid", getAppId());
            params.put("description", description);
            params.put("notify_url", notifyUrl);

            // 金额信息
            Map<String, Object> amountMap = new HashMap<>();
            amountMap.put("total", amount.intValue()); // 转换为int（微信要求）
            amountMap.put("currency", "CNY");
            params.put("amount", amountMap);

            // 支付者信息
            Map<String, String> payerMap = new HashMap<>();
            payerMap.put("openid", openid);
            params.put("payer", payerMap);

            // 发送请求
            HttpPost httpPost = new HttpPost(JSAPI_URL);
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Accept", "application/json");
            
            String requestBody = gson.toJson(params);
            httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            CloseableHttpResponse response = wechatPayHttpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            int statusCode = response.getStatusLine().getStatusCode();

            log.info("微信支付统一下单响应 - 状态码: {}, 响应体: {}", statusCode, responseBody);

            if (statusCode == 200) {
                Map<String, Object> result = gson.fromJson(responseBody, Map.class);
                String prepayId = (String) result.get("prepay_id");

                // 构建小程序调起支付所需的参数
                return buildMiniProgramPayParams(prepayId);
            } else {
                log.error("微信支付统一下单失败 - 订单号: {}, 状态码: {}, 响应: {}", orderNo, statusCode, responseBody);
                throw new Exception("微信支付统一下单失败: " + responseBody);
            }

        } catch (Exception e) {
            log.error("创建微信支付订单异常 - 订单号: {}", orderNo, e);
            throw new Exception("创建微信支付订单失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询支付订单状态
     *
     * @param orderNo 订单号
     * @return 订单状态信息
     */
    @Override
    public Map<String, Object> queryPayment(String orderNo) {
        try {
            String url = QUERY_URL + orderNo + "?mchid=" + mchId;
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("Accept", "application/json");

            CloseableHttpResponse response = wechatPayHttpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            int statusCode = response.getStatusLine().getStatusCode();

            log.info("查询支付订单响应 - 订单号: {}, 状态码: {}, 响应体: {}", orderNo, statusCode, responseBody);

            if (statusCode == 200) {
                return gson.fromJson(responseBody, Map.class);
            } else {
                log.error("查询支付订单失败 - 订单号: {}, 状态码: {}, 响应: {}", orderNo, statusCode, responseBody);
                throw new RuntimeException("查询支付订单失败: " + responseBody);
            }

        } catch (Exception e) {
            log.error("查询支付订单异常 - 订单号: {}", orderNo, e);
            throw new RuntimeException("查询支付订单失败", e);
        }
    }

    /**
     * 验证微信支付回调签名
     *
     * @param timestamp 时间戳
     * @param nonce 随机串
     * @param body 请求体
     * @param signature 签名
     * @return 验证是否通过
     */
    @Override
    public boolean verifySignature(String timestamp, String nonce, String body, String signature) {
        try {
            // 验证时间戳，防止重放攻击（5分钟有效期）
            long currentTime = System.currentTimeMillis() / 1000;
            long requestTime = Long.parseLong(timestamp);
            if (Math.abs(currentTime - requestTime) > 300) {
                log.warn("微信支付回调签名验证失败 - 时间戳过期，当前时间: {}, 请求时间: {}", currentTime, requestTime);
                return false;
            }

            // 构建待验证的签名消息
            // 格式：时间戳\n随机串\n请求体\n
            String message = timestamp + "\n" + nonce + "\n" + body + "\n";
            
            // 使用微信支付SDK的Verifier进行签名验证
            // Verifier内部会使用微信平台公钥证书进行RSA-SHA256验签
            boolean isValid = verifier.verify(
                null,  // serialNumber参数在某些场景下可为null
                message.getBytes(StandardCharsets.UTF_8),
                signature
            );

            if (isValid) {
                log.info("微信支付回调签名验证成功");
            } else {
                log.warn("微信支付回调签名验证失败 - 签名不匹配");
            }

            return isValid;

        } catch (NumberFormatException e) {
            log.error("微信支付回调签名验证失败 - 时间戳格式错误: {}", timestamp, e);
            return false;
        } catch (Exception e) {
            log.error("验证微信支付回调签名异常", e);
            return false;
        }
    }

    /**
     * 解密微信支付回调数据
     *
     * @param associatedData 附加数据
     * @param nonce 随机串
     * @param ciphertext 密文（Base64编码）
     * @return 解密后的明文
     */
    @Override
    public String decryptNotifyData(String associatedData, String nonce, String ciphertext) {
        try {
            // 使用AES-256-GCM解密
            byte[] keyBytes = apiV3Key.getBytes(StandardCharsets.UTF_8);
            byte[] nonceBytes = nonce.getBytes(StandardCharsets.UTF_8);
            byte[] associatedDataBytes = associatedData.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertext);

            // 创建密钥规范
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            // 创建GCM参数规范（tag长度为128位）
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, nonceBytes);

            // 创建并初始化解密器
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            
            // 设置附加认证数据
            cipher.updateAAD(associatedDataBytes);

            // 解密
            byte[] decryptedBytes = cipher.doFinal(ciphertextBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("解密微信支付回调数据失败", e);
            throw new RuntimeException("解密微信支付回调数据失败", e);
        }
    }

    /**
     * 申请退款
     *
     * @param orderNo 订单号
     * @param transactionId 微信支付订单号
     * @param totalAmount 订单总金额（分）
     * @param refundAmount 退款金额（分）
     * @param reason 退款原因
     * @return 退款结果
     * @throws Exception 申请退款异常
     */
    @Override
    public Map<String, Object> refund(String orderNo, String transactionId, Long totalAmount, Long refundAmount, String reason) throws Exception {
        // 生成退款单号
        String refundNo = "R" + orderNo.substring(1);
        
        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("out_trade_no", orderNo);
            params.put("out_refund_no", refundNo);
            params.put("reason", reason);
            params.put("notify_url", refundNotifyUrl);

            // 金额信息
            Map<String, Object> amountMap = new HashMap<>();
            amountMap.put("total", totalAmount.intValue()); // 转换为int
            amountMap.put("refund", refundAmount.intValue()); // 转换为int
            amountMap.put("currency", "CNY");
            params.put("amount", amountMap);

            // 发送请求
            HttpPost httpPost = new HttpPost(REFUND_URL);
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Accept", "application/json");
            
            String requestBody = gson.toJson(params);
            httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));

            CloseableHttpResponse response = wechatPayHttpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            int statusCode = response.getStatusLine().getStatusCode();

            log.info("微信退款响应 - 订单号: {}, 退款单号: {}, 状态码: {}, 响应体: {}", 
                    orderNo, refundNo, statusCode, responseBody);

            if (statusCode == 200) {
                return gson.fromJson(responseBody, Map.class);
            } else {
                log.error("微信退款失败 - 订单号: {}, 退款单号: {}, 状态码: {}, 响应: {}", 
                        orderNo, refundNo, statusCode, responseBody);
                throw new Exception("微信退款失败: " + responseBody);
            }

        } catch (Exception e) {
            log.error("申请退款异常 - 订单号: {}, 退款单号: {}", orderNo, refundNo, e);
            throw new Exception("申请退款失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建小程序调起支付所需的参数
     *
     * @param prepayId 预支付交易会话ID
     * @return 支付参数Map
     */
    private Map<String, String> buildMiniProgramPayParams(String prepayId) {
        try {
            String appid = getAppId();
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            String nonceStr = generateNonceStr();
            String packageValue = "prepay_id=" + prepayId;
            
            // 构建签名串
            String signStr = appid + "\n" + timeStamp + "\n" + nonceStr + "\n" + packageValue + "\n";
            
            // 使用商户私钥进行RSA签名
            String paySign = sign(signStr.getBytes(StandardCharsets.UTF_8));
            
            Map<String, String> payParams = new HashMap<>();
            payParams.put("timeStamp", timeStamp);
            payParams.put("nonceStr", nonceStr);
            payParams.put("package", packageValue);
            payParams.put("signType", "RSA");
            payParams.put("paySign", paySign);
            
            return payParams;
        } catch (Exception e) {
            log.error("构建小程序支付参数失败", e);
            throw new RuntimeException("构建小程序支付参数失败", e);
        }
    }

    /**
     * 使用商户私钥进行RSA签名
     *
     * @param message 待签名数据
     * @return Base64编码的签名值
     */
    private String sign(byte[] message) {
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(merchantPrivateKey);
            sign.update(message);
            return Base64.getEncoder().encodeToString(sign.sign());
        } catch (Exception e) {
            log.error("RSA签名失败", e);
            throw new RuntimeException("RSA签名失败", e);
        }
    }

    /**
     * 生成随机字符串
     *
     * @return 随机字符串
     */
    private String generateNonceStr() {
        return java.util.UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 获取小程序AppId（从配置中读取）
     *
     * @return AppId
     */
    private String getAppId() {
        return appId;
    }
}
