package com.leyou.auth.service;

import com.leyou.user.clients.UserClient;
import com.leyou.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserClient userClient;

    public void login(String username, String password) {

        UserDTO userDTO = this.userClient.queryUserByNameAndPass(username, password);

        //TODO 根据 userDTO生成对应的token，并且存入cookie中
    }
}