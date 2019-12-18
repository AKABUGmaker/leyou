package com.leyou.order.interceptors;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.order.config.JwtProperties;
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
public class UserTokenInterceptor implements HandlerInterceptor {


    //ThreadLocal线程的独立作用域，每个线程独立，并且只有当前线程可以访问自己的threadLocal
    private ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    @Autowired
    private JwtProperties props;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        try {
            //从请求中获取cookie，然后从cookie中获取token
            String token = CookieUtils.getCookieValue(request, props.getUser().getCookieName());
            //解析token
            Payload<UserInfo> infoFromToken = JwtUtils.getInfoFromToken(token, props.getPublicKey(), UserInfo.class);

            //把userInfo存储
            tl.set(infoFromToken.getInfo());

            log.info("【订单】获取用户token信息成功");
            return true;
        } catch (Exception e) {
            log.error("【订单】获取用户token信息失败");
            response.setStatus(401);//同时返回状态码401
        }
        return false;
    }

    //拦截器最终执行方法
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        tl.remove();//当请求结束时，把localStorage中的内容直接清空删除，
    }

    public UserInfo getUserInfo() {
        return tl.get();
    }
}
