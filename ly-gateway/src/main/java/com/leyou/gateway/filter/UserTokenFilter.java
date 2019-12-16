package com.leyou.gateway.filter;

import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
@Slf4j
public class UserTokenFilter extends ZuulFilter {

    @Autowired
    private FilterProperties filterProps;

    @Autowired
    private JwtProperties props;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.SERVLET_DETECTION_FILTER_ORDER - 1;
    }

    //决定过滤器是否生效，true，生效，false不生效（run方法是否执行）
    @Override
    public boolean shouldFilter() {

        RequestContext currentContext = RequestContext.getCurrentContext();

        //获取请求对象
        HttpServletRequest request = currentContext.getRequest();

        //获取请求地址
        String requestURI = request.getRequestURI();

        //获取到白名单地址
        List<String> allowPaths = filterProps.getAllowPaths();

        //遍历白名单地址，如果当前请求的资源地址以某个白名单地址开头，则直接放行
        for (String allowPath : allowPaths) {
            if (requestURI.startsWith(allowPath)){
                return false;
            }
        }
        return true;
    }

    //网关过滤器，放行不用额外声明，但是拦截，要声明，拦截就是不响应
    @Override
    public Object run() throws ZuulException {

        RequestContext currentContext = RequestContext.getCurrentContext();
        //获取请求request对象
        HttpServletRequest request = currentContext.getRequest();
        //源请求的地址
        String remoteHost = request.getRemoteHost();

        //目标资源
        String requestURI = request.getRequestURI();

        //获取请求的资源管理器
        try {
            //从请求request中获取token
            String token = CookieUtils.getCookieValue(request, props.getUser().getCookieName());


            JwtUtils.getInfoFromToken(token, props.getPublicKey(), UserInfo.class);
            log.info("【网关】允许来自{},的请求，请求目标为:{}", remoteHost, requestURI);
        } catch (Exception e) {
            log.error("【网关】拒绝来自{},的请求，请求目标为:{}", remoteHost, requestURI);
            currentContext.setSendZuulResponse(false);//声明不响应
            currentContext.setResponseStatusCode(401);//设置响应的状态码为401

        }
        return null;
    }
}

