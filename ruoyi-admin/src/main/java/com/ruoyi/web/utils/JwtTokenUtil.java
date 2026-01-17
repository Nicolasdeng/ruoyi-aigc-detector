package com.ruoyi.web.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 
 * @author ruoyi
 */
@Component
public class JwtTokenUtil {

    /**
     * JWT签名密钥
     */
    @Value("${jwt.secret:abcdefghijklmnopqrstuvwxyz}")
    private String secret;

    /**
     * JWT过期时间（默认7天）
     */
    @Value("${jwt.expiration:604800}")
    private Long expiration;

    /**
     * 从Token中获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null ? Long.parseLong(claims.getSubject()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中获取openid
     * 
     * @param token JWT Token
     * @return openid
     */
    public String getOpenidFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null ? (String) claims.get("openid") : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中获取过期时间
     * 
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null ? claims.getExpiration() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中获取Claims
     * 
     * @param token JWT Token
     * @return Claims
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断Token是否过期
     * 
     * @param token JWT Token
     * @return true-过期 false-未过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 生成Token
     * 
     * @param userId 用户ID
     * @param openid 微信openid
     * @return JWT Token
     */
    public String generateToken(Long userId, String openid) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("openid", openid);
        claims.put("created", new Date());
        return generateToken(userId.toString(), claims);
    }

    /**
     * 生成Token（内部方法）
     * 
     * @param subject 主题（用户ID）
     * @param claims 声明信息
     * @return JWT Token
     */
    private String generateToken(String subject, Map<String, Object> claims) {
        Date createdDate = new Date();
        Date expirationDate = new Date(createdDate.getTime() + expiration * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 刷新Token
     * 
     * @param token 原Token
     * @return 新Token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return null;
            }
            claims.put("created", new Date());
            return generateToken(claims.getSubject(), claims);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证Token
     * 
     * @param token JWT Token
     * @param userId 用户ID
     * @return true-有效 false-无效
     */
    public Boolean validateToken(String token, Long userId) {
        try {
            Long tokenUserId = getUserIdFromToken(token);
            return tokenUserId != null 
                    && tokenUserId.equals(userId) 
                    && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
