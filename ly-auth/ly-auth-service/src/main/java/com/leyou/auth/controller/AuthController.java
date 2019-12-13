package com.leyou.auth.controller;

import com.leyou.auth.service.AuthService;
import com.leyou.common.auth.entity.UserInfo;
import com.netflix.client.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("login")
    public ResponseEntity<Void> login(
            @RequestParam("username")String username,
            @RequestParam("password")String password,
            HttpServletResponse response){

        this.authService.login(username,password,response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyUser(@CookieValue("heima86")String token){
        return ResponseEntity.ok(this.authService.verifyUser(token));
    }

}

