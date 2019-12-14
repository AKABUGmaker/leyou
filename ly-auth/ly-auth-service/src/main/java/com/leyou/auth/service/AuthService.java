package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.ApplicationInfo;
import com.leyou.auth.mapper.AppInfoMapper;
import com.leyou.common.auth.entity.AppInfo;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.auth.utils.RsaUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.clients.UserClient;
import com.leyou.user.dto.UserDTO;
import com.sun.deploy.net.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.core.ApplicationMapping;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {
    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties props;

    public void login(String username, String password, HttpServletResponse response) {

        UserDTO userDTO = this.userClient.queryUserByNameAndPass(username, password);

        UserInfo userInfo = BeanHelper.copyProperties(userDTO, UserInfo.class);

        //实际应该查询处理用户的角色
        userInfo.setRole("测试role");



        //使用私钥加密生成token
        String token = JwtUtils.generateTokenExpireInMinutes(userInfo, props.getPrivateKey(), 30);

        //将token存入cookie

        CookieUtils.newBuilder()
                .name(props.getUser().getCookieName())
                .value(token)
                .domain(props.getUser().getCookieDomain())
                .maxAge(props.getUser().getCookieMaxAge())
                .httpOnly(true)
                .response(response)
                .build();
}

    public UserInfo verifyUser(HttpServletRequest request,HttpServletResponse response) {

        String token = CookieUtils.getCookieValue(request, props.getUser().getCookieName());

        UserInfo userInfo = null;
        try {


            Payload<UserInfo> info = null;
            try {
                info = JwtUtils.getInfoFromToken(token, props.getPublicKey(), UserInfo.class);
                log.info("用户token解析成功");
            } catch (Exception e) {
                log.error("用户token解析失败");
                throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
            }


            //首先要查验这个tokenId是否在redis中

            if (redisTemplate.hasKey(info.getId())) {
                log.error("所请求的token是非法token");
                deleteCookie(response);
                throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
            }

            userInfo = info.getInfo();

            //当请求到达服务后台时，要重新生效token和cookie，使其达到刷新的目的
            //如果过期时间-29min，比当前时间早，说明生成已经超过了1min
            if (new DateTime(info.getExpiration()).minusMinutes(29).isBeforeNow()) {

                log.info("刷新token");
                //当请求到达服务后台时，要重新生成token和cookie，使得其达到刷新的目的

                String newToken = JwtUtils.generateTokenExpireInMinutes(userInfo, props.getPrivateKey(), props.getUser().getExpire());

                //把token存入cookie中
                CookieUtils.newBuilder()
                        .name(props.getUser().getCookieName())
                        .value(newToken)
                        .domain(props.getUser().getCookieDomain())
                        .maxAge(props.getUser().getCookieMaxAge())
                        .httpOnly(true)
                        .response(response)
                        .build();
            }

            return userInfo;
        } catch (Exception e) {

            log.error("用户信息校验失败");
        }


        return null;
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void logout(HttpServletResponse response,HttpServletRequest request) {


        log.info("执行推出业务");

        //token黑名单
        //从cookie中获取token

        String token = CookieUtils.getCookieValue(request, props.getUser().getCookieName());

        //解析token
        Payload<UserInfo> info = JwtUtils.getInfoFromToken(token, props.getPublicKey(), UserInfo.class);

        //先判断token还有多久过期，如果，过期时间在3秒以内不管
        //失效时间超过3秒，把tokenId加入到redis，中，加入的时间就是你的剩余过期时间
        //获取过期时间
        Date expiration = info.getExpiration();
        if (!new DateTime(expiration).minusSeconds(3).isBeforeNow()){
            log.info("把退出的token的id加入redis");

            String id = info.getId();

            //把tokenId加入到redis中，并设置过期时间为token的过期时间
            redisTemplate.opsForValue().set(id,"",expiration.getTime()-System.currentTimeMillis(), TimeUnit.MILLISECONDS);

        }

        deleteCookie(response);


    }

    private void deleteCookie(HttpServletResponse response) {
        log.info("执行cookie删除任务");
        Cookie cookie = new Cookie(props.getUser().getCookieName(), "");
        cookie.setDomain(props.getUser().getCookieDomain());
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    @Autowired
    private AppInfoMapper appInfoMapper;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public String authenticate(Long id, String secret) {


        //根据服务id查询服务对象
        ApplicationInfo applicationInfo = this.appInfoMapper.selectById(id);

        //判断查询结果以及验密
        if (null==applicationInfo||!bCryptPasswordEncoder.matches(secret,applicationInfo.getSecret())){
            log.error("【授权服务】对服务的id和密码认证失败");
            throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
        }

        AppInfo appInfo = BeanHelper.copyProperties(applicationInfo, AppInfo.class);

        //根据服务的id查询目标服务的id集合
        List<Long> targetList = this.appInfoMapper.selectTargetList(id);
        appInfo.setTargetList(targetList);

        try {
            String token = JwtUtils.generateTokenExpireInMinutes(appInfo, props.getPrivateKey(), props.getApp().getExpire());
            log.info("【授权中心】为 {} 服务生成token成功",appInfo.getServiceName());
            //基于appInfo生成服务的token
            return token;
        } catch (Exception e) {
            log.error("【授权中心】为 {} 服务生成token失败",appInfo.getServiceName());
            throw new LyException(ExceptionEnum.TOKEN_GENERATE_ERROR);
        }
    }
}