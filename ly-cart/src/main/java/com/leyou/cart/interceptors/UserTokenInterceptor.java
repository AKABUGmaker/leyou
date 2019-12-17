package com.leyou.cart.interceptors;

import com.leyou.cart.config.JwtProperties;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
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


    private ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    @Autowired
    private JwtProperties props;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        try {
            //从请求中获取cookie,然后从cookie中获取token
            String token = CookieUtils.getCookieValue(request, props.getUser().getCookieName());

            //解析token
            Payload<UserInfo> infoFromToken = JwtUtils.getInfoFromToken(token, props.getPublicKey(), UserInfo.class);

            UserInfo userInfo = infoFromToken.getInfo();

            //存储userInfo
            tl.set(userInfo);

            log.info("【购物车】获取用户信息成功");
            return true;
        } catch (Exception e) {

            log.error("【购物车】获取用户信息失败");
            response.setStatus(401);//未授权
        }
        return false;

    }

    //拦截器最终执行方法
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        tl.remove();//请求结束时，删除localStorage中的内容
    }

    public UserInfo getUserInfo() {
        return tl.get();
    }

}
