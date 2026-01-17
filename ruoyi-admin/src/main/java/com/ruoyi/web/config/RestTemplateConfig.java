package com.ruoyi.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类
 * 用于微信API调用等HTTP请求
 * 
 * @author ruoyi
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 创建RestTemplate Bean
     * 
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 设置连接超时时间（毫秒）
        factory.setConnectTimeout(5000);
        // 设置读取超时时间（毫秒）
        factory.setReadTimeout(5000);
        
        return new RestTemplate(factory);
    }
}
