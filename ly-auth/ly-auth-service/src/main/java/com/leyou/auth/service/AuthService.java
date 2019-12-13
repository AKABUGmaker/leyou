package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.auth.utils.RsaUtils;
import com.leyou.common.utils.BeanHelper;
import com.leyou.user.clients.UserClient;
import com.leyou.user.dto.UserDTO;
import com.sun.deploy.net.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.security.PrivateKey;
import java.security.PublicKey;

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
        Cookie cookie = new Cookie("heima86", token);

        cookie.setDomain("leyou.com");
        cookie.setPath("/");//域名对应的适用路径
        cookie.setMaxAge(1800);//cookie有效时间
        cookie.setHttpOnly(true);//所有的script都无法访问到这个cookie
        response.addCookie(cookie);
    }

    public UserInfo verifyUser(String token) {

        UserInfo userInfo = null;
        try {



            Payload<UserInfo> info = JwtUtils.getInfoFromToken(token, props.getPublicKey(), UserInfo.class);

            userInfo = info.getInfo();

            return userInfo;
        } catch (Exception e) {

            log.error("用户信息校验失败");
        }


        return null;
    }
}