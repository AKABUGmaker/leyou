package com.leyou.order.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.clients.ItemClient;
import com.leyou.item.dto.SkuDTO;
import com.leyou.order.dto.CartDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.entity.Order;
import com.leyou.order.entity.OrderDetail;
import com.leyou.order.entity.OrderLogistics;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.interceptors.UserTokenInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderLogisticsMapper;
import com.leyou.order.mapper.OrderMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderLogisticsMapper orderLogisticsMapper;

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private UserTokenInterceptor userTokenInterceptor;

    public void createOrder(OrderDTO orderDTO) {

        //雪花算法生成订单id
        long orderId = idWorker.nextId();

        //list集合转换为map集合
        //map的key为skuId,value为对应的num
        Map<Long, Integer> cartsMap = orderDTO.getCarts().stream()
                .collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));


        //根据传入的skuId查询对应的sku
        List<SkuDTO> skuDTOS = this.itemClient.querySkuByIds(new ArrayList<>(cartsMap.keySet()));


        //定义总价
        long totalFee = 0;

        //创建订单详情的集合
        List<OrderDetail> orderDetails = new ArrayList<>();

        for (SkuDTO skuDTO : skuDTOS) {

            //获取到指定商品的购买数量
            Integer skuNum = cartsMap.get(skuDTO.getId());
            //总价+=单价*数量
            totalFee += skuDTO.getPrice() * skuNum;

            //把SkuDTO转换为orderDetail
            OrderDetail orderDetail = BeanHelper.copyProperties(skuDTO, OrderDetail.class);
            orderDetail.setId(null);//让主键自增
            orderDetail.setSkuId(skuDTO.getId()); //给订单详情设置skuId
            orderDetail.setNum(skuNum); //设置购买数量
            orderDetail.setImage(StringUtils.substringBefore(skuDTO.getImages(), ",")); //设置sku的图片，第一张

            orderDetail.setOrderId(orderId);//给订单详情添加orderId

            //设置订单详情创建时间
            orderDetail.setCreateTime(new Date());
            //第一次创建时间和修改时间一致
            orderDetail.setUpdateTime(orderDetail.getUpdateTime());


            orderDetails.add(orderDetail);
        }


        Order order = new Order();
        order.setOrderId(orderId);
        order.setTotalFee(totalFee);//每个商品的单价*num并且要累加
        order.setActualFee(totalFee);//实际支付的价格
        order.setPaymentType(orderDTO.getPaymentType());//支付方式
        order.setPostFee(0L);
        order.setUserId(userTokenInterceptor.getUserInfo().getId()); //从用户信息拦截器中获取用户的id
        order.setStatus(OrderStatusEnum.INIT.value()); //初始化订单状态
        order.setCreateTime(new Date());

        //订单的保存
        int count = this.orderMapper.insertSelective(order);

        if (1 != count) {
            throw new LyException(ExceptionEnum.DATA_SAVE_ERROR);
        }

        //订单详情的保存
        count = this.orderDetailMapper.insertList(orderDetails);

        if (orderDetails.size() != count) {
            throw new LyException(ExceptionEnum.DATA_SAVE_ERROR);
        }


        OrderLogistics orderLogistics = new OrderLogistics();

    }
}
