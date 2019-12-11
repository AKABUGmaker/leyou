package com.leyou.user.controller;

import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.user.service.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * 根据输入参数校验参数的唯一性，data表示手机号或用户名，type用来切换展示到底是用户名还是手机号
     *
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    @ApiOperation(value = "校验用户名数据是否可用，如果不存在则可用")//描述接口的功能
    @ApiResponses({
            @ApiResponse(code = 200, message = "校验结果有效，true或false代表可用或不可用"),
            @ApiResponse(code = 400, message = "请求参数有误，比如type不是指定值"),
            @ApiResponse(code = 500, message = "服务器内部错误例如数据库异常请求")
    })
    public ResponseEntity<Boolean> checkData(
            @ApiParam(value = "要校验的数据", example = "lisi") @PathVariable("data") String data,
            @ApiParam(value = "数据类型，1：用户名，2：手机号", example = "1") @PathVariable(value = "type") Integer type
    ){
        return ResponseEntity.ok(this.userService.checkData(data,type));
    }

    @PostMapping("/code")
    public ResponseEntity<Void> sendVerifyCode(@RequestParam("phone")String phone){
        this.userService.sendVerifyCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/register")
    //服务端数据校验在controller之前
    public ResponseEntity<Void> register(
            @Valid User user,
            @RequestParam("code")String code
    ){
        this.userService.register(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("query")
    public ResponseEntity<UserDTO> queryUserByNameAndPass(
            @RequestParam("username")String username,
            @RequestParam("password")String password
    ){
        return ResponseEntity.ok(this.userService.queryUserByNameAndPass(username,password));
    }

}
