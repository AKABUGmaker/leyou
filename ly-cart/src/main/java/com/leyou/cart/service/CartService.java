package com.leyou.cart.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.cart.entity.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void addCart(Cart cart) {
        BoundHashOperations<String, String, String> ops = null;

        try {
            //根据用户的id过去当前用户的redis,map的操作对象
            ops = redisTemplate.boundHashOps("userId");
        } catch (Exception e) {
            log.error("redis访问出错");
        }

        //获取第二层的map的key（sku的ID）
        String hKey = String.valueOf(cart.getSkuId());

        //判断此sku是否加入过
        if (ops.hasKey(hKey)){
            //先取出之前的值，修改数据后保存
            String cartJson = ops.get(hKey);

            Cart storeCart = JsonUtils.nativeRead(cartJson, new TypeReference<Cart>() {
            });

            //将原来有的数量加上新添加的数量
            storeCart.setNum(storeCart.getNum()+cart.getNum());

            //保存进去
            ops.put(hKey,JsonUtils.toString(storeCart));
        }else {
            //没有加入过，直接添加
            ops.put(hKey,JsonUtils.toString(cart));
        }


    }

    public List<Cart> listCarts() {

        if (redisTemplate.hasKey("userId")){
            //如果用户有购物车数据信息，则查询展示

            BoundHashOperations<String, String, String> ops = null;

            try {
                //根据用户的id过去当前用户的redis,map的操作对象
                ops = redisTemplate.boundHashOps("userId");
            } catch (Exception e) {
                log.error("redis访问出错");
            }

            //Map<key,Map<hKey,hValue>>
            //一次性取出所有的hValue,并把取出的list<String>集合转为，List<Cart>

            List<String> values = ops.values();

            List<Cart> carts = values.stream().map(value -> JsonUtils.nativeRead(value, new TypeReference<Cart>() {
            })).collect(Collectors.toList());

            return carts;
        }else {
            //购物车数据为空
            throw new LyException(ExceptionEnum.CART_IS_NULL);
        }


    }
}
