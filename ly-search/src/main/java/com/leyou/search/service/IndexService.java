package com.leyou.search.service;

import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.search.clients.ItemClient;
import com.leyou.search.pojo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class IndexService {

    @Autowired
    private ItemClient itemClient;
    /**
     * 基于spu原型,把spu转为Goods的过程
     * @param spuDTO
     * @return
     */
    public Goods buildGoods(SpuDTO spuDTO){

        Goods goods = BeanHelper.copyProperties(spuDTO, Goods.class);

        //createTime 赋值createTime
        goods.setCreateTime(spuDTO.getCreateTime().getTime());

        //categoryId
        goods.setCategoryId(spuDTO.getCid3());

        //all // 所有需要被搜索的信息，包含标题，分类，甚至品牌

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(spuDTO.getName());


        String names =  this.itemClient.queryCategoryByIds(spuDTO.getCategoryIds())
                .stream()
                .map(CategoryDTO::getName)
                .collect(Collectors.joining(" "));

        stringBuilder.append(names);
        goods.setAll(stringBuilder.toString());

        //skus 	id,
        //	price,
        //	title,
        //	image

        List<SkuDTO> skuDTOS = this.itemClient.querySkuBySpuId(spuDTO.getId());


        List< Map<String,Object>> skuMaps = new ArrayList<>();

        Set<Long> prices = new HashSet<>();
        skuDTOS.forEach(skuDTO -> {
            Map<String,Object> skuMap = new HashMap<>();
            skuMap.put("id",skuDTO.getId());
            skuMap.put("title",skuDTO.getTitle());
            skuMap.put("price",skuDTO.getPrice());
            //jvm优化，重点的优化在代码中，开发规范，
            skuMap.put("image", StringUtils.substringBefore(skuDTO.getImages(),","));

            skuMaps.add(skuMap);
            prices.add(skuDTO.getPrice());
        });

        goods.setSkus(JsonUtils.toString(skuMaps));

        //price

        goods.setPrice(prices);

        //specs

        return goods;

    }
}
