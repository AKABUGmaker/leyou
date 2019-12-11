package com.leyou.user.entity;

import com.leyou.common.utils.constants.RegexPatterns;
import lombok.Data;

import org.hibernate.validator.constraints.Length;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Table(name = "tb_user")
@Data
public class User {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;

    @NotNull(message = "用户名不能为空")
    @Length(min = 4,max = 30,message = "用户名长度不能越界，4-30")
    private String username;
    private String password;
    @Pattern(regexp = RegexPatterns.PHONE_REGEX,message = "手机号不匹配")
    private String phone;
    private Date createTime;
    private Date updateTime;
}
