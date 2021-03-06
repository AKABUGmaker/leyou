package com.leyou.gateway.task;

import com.leyou.auth.clients.AuthClient;
import com.leyou.gateway.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@EnableConfigurationProperties(JwtProperties.class)
public class PrivilegeTokenHolder {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private AuthClient authClient;

    private String token;

    final long refreshTime = 86400000L;//24小时,启动时执行第一次
    //时间需要写常量
    @Scheduled(fixedDelay = refreshTime)
    public void getTokenInTime() {

        while (true) {
            try {
                this.token =  authClient.authorize(jwtProperties.getApp().getId(), jwtProperties.getApp().getSecret());
                log.info("【网关】获取token成功");
                break;
            } catch (Exception e) {
                log.error("【网关】获取token失败");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    //获取token的方法
    public String getToken() {
        return token;
    }
}
