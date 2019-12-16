package com.leyou.auth.interceptor;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.task.PrivilegeTokenHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(JwtProperties.class)
//使用feign提供的拦截器
public class PrivilegeTokenInterceptor implements RequestInterceptor {
    @Autowired
    private JwtProperties props;

    @Autowired
    private PrivilegeTokenHolder tokenHolder;

    //把token加入到feign的请求头中
    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header(props.getApp().getHeaderName(),tokenHolder.getToken());
    }
}
