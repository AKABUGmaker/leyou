package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.NumberUtils;
import com.leyou.common.utils.constants.RegexPatterns;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.user.mapper.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.leyou.common.constants.MQConstants.Exchange.SMS_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.RoutingKey.VERIFY_CODE_KEY;
import static javafx.scene.input.KeyCode.V;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public Boolean checkData(String data, Integer type) {

        if (StringUtils.isBlank(data)) {
            throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
        }

        User user = new User();

        switch (type) {
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
        }

        Boolean result = null;

        try {
            result = this.userMapper.selectCount(user)!=1;
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.DATA_SERVER_NO_SEE);
        }

        if (null == result){
            throw new LyException(ExceptionEnum.QUERY_FAIL);
        }

        return result;

    }

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String KEY_PREFIX = "ly:user:code:phone:";

    public void sendVerifyCode(String phone) {

        //校验参数,手机号为空，或者格式不符合要求则，抛异常
        if(StringUtils.isBlank(phone)||!phone.matches(RegexPatterns.PHONE_REGEX)){
            throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
        }

        Map<String,String> msg = new HashMap<>();
        msg.put("phone",phone);
        String code = NumberUtils.generateCode(6);
        msg.put("code",code);

        this.amqpTemplate.convertAndSend(SMS_EXCHANGE_NAME,VERIFY_CODE_KEY, msg);


        //把验证码存入redis
        redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 5, TimeUnit.MINUTES);


    }

    public void register(User user, String code) {

        //首先完成验证码的校验
        String key = KEY_PREFIX + user.getPhone();


        //首先要判断是否存在，如果不存在，或存在不匹配，则直接返回
        if (!redisTemplate.hasKey(key)||!redisTemplate.opsForValue().get(key).equals(code)){
            throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
        }

        //把原始密码的明文进行加密处理，然后保存
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        int count = userMapper.insertSelective(user);

        if (1!=count){
            throw new LyException(ExceptionEnum.DATA_SAVE_ERROR);
        }
        //演示一下如果离过期还远 3s以上，则需要手动删除
        if (redisTemplate.hasKey(key)&&redisTemplate.getExpire(key,TimeUnit.SECONDS)>3){
        //此时要手动删除
            redisTemplate.delete(key);
        }

    }

    public UserDTO queryUserByNameAndPass(String username, String password) {

        User record = new User();
        record.setUsername(username);
        //根据用户名查询user
        User user = this.userMapper.selectOne(record);

        //如果用户为空，或者密码不匹配
        if (null==user||!passwordEncoder.matches(password,user.getPassword())){
            throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
        }

        return BeanHelper.copyProperties(user,UserDTO.class);
    }
}
