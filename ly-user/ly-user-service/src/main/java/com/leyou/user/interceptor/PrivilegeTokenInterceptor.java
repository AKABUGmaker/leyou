package com.leyou.user.interceptor;

import com.leyou.common.auth.entity.AppInfo;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.user.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
@EnableConfigurationProperties(JwtProperties.class)
public class PrivilegeTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties props;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {


        try {
            //从请求头中获取token
            String token = request.getHeader(props.getApp().getHeaderName());
            //解析token
            Payload<AppInfo> infoFromToken = JwtUtils.getInfoFromToken(token, props.getPublicKey(), AppInfo.class);
            //从解析的token中获取真正保存的appInfo
            AppInfo info = infoFromToken.getInfo();
            //判断当前服务是不是请求来源者可以请求的服务
            if (info.getTargetList().contains(props.getApp().getId())) {
                log.info("【用户服务】允许{}的访问", info.getServiceName());
                return true;
            }
        } catch (Exception e) {
            log.error("【用户服务】拒绝非法请求");
            response.setStatus(401);//未授权
        }
        return false;
    }
}
