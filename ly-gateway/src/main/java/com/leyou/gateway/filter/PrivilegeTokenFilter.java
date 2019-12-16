package com.leyou.gateway.filter;

import com.leyou.gateway.config.JwtProperties;
import com.leyou.gateway.task.PrivilegeTokenHolder;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class PrivilegeTokenFilter extends ZuulFilter {

    @Autowired
    private JwtProperties props;

    @Autowired
    private PrivilegeTokenHolder tokenHolder;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;//前置执行
    }

    @Override
    public int filterOrder() {//执行次序
        return FilterConstants.PRE_DECORATION_FILTER_ORDER+1;//在完成前置，配置之后执行
    }

    @Override
    public boolean shouldFilter() { //不论请求谁，都在请求头中添加token
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        //获取当前请求的上下文
        RequestContext currentContext = RequestContext.getCurrentContext();

        //给请求添加请求头
        currentContext.addZuulRequestHeader(props.getApp().getHeaderName(),tokenHolder.getToken());
        return null;
    }
}
