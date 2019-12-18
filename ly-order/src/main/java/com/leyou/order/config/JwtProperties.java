package com.leyou.order.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {

    /**
     * 公钥地址
     */
    private String pubKeyPath;

    private PublicKey publicKey;


    //非普通所以一定要手动初始化地址
    private UserTokenInfo user = new UserTokenInfo();

    @Data
    public class UserTokenInfo{
        private String cookieName;
    }



    //等待其他属性注入完成后，再进行方法执行，
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
            log.info("【购物车】初始化公钥成功！");
        } catch (Exception e) {
            log.error("【购物车】初始化公钥失败！", e);
            throw new RuntimeException(e);
        }
    }
}
