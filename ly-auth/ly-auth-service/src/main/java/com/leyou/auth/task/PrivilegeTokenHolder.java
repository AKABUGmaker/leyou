package com.leyou.auth.task;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
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
    private JwtProperties props;

    @Autowired
    private AuthService authService;


    final long refreshTime = 86400000L;  //24刷新，第一次执行在启动时执行

    private String token;

    //时间需要写常量，并且一定不能写对象类型
    @Scheduled(fixedDelay = refreshTime)
    public void getTokenInTime() {

        while (true) {
            try {
                this.token =  this.authService.authenticate(props.getApp().getId(), props.getApp().getSecret());
                log.info("【授权中心】获取token成功");
                break;
            } catch (Exception e) {
                log.error("【授权中心】获取token失败");
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