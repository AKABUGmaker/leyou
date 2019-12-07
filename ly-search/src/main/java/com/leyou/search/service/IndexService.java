package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.dto.*;
import com.leyou.item.clients.ItemClient;
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

        //specs  可搜索规格参数，eg:CPU核数（查询规格参数表）：8核（特有：spuDetail.specailSpec,通用，spuDetailGenericSpec）
        Map<String, Object> specs = new HashMap<>();
        //跨服务查询，所有的可搜索规格参数
        List<SpecParamDTO> specParamDTOS = this.itemClient.querySpecParams(null, spuDTO.getCid3(), true);


        //查询spuDetail

        SpuDetailDTO spuDetailDTO = this.itemClient.querySpuDetailBySpuId(spuDTO.getId());


        //把json转为java对象,今后只要知道目标类型是什么，只要把目标类型，写到TypeReference的范型中就可以
        Map<Long,Object> genericMap = JsonUtils.nativeRead(spuDetailDTO.getGenericSpec(), new TypeReference<Map<Long, Object>>() {
        });


        //获取特有规格参数的map
        Map<Long,List<String>> specialMap = JsonUtils.nativeRead(spuDetailDTO.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });

        //循环把可搜索规格参数加入到specs的map集合中
        specParamDTOS.forEach(specParamDTO -> {
            Long id = specParamDTO.getId();//规格参数id

            Object value = null;
            if (specParamDTO.getGeneric()){//通用规格参数总，通用规格中取值，
                value = genericMap.get(id);
            }else {//特有规格参数，从特有规格中取值
                value = specialMap.get(id);
            }
            // 判断是否是数值类型
            if(specParamDTO.getNumeric()){
                // 是数字类型，分段
                value = chooseSegment(value, specParamDTO);
            }
            // 添加到specs
            specs.put(specParamDTO.getName(),value);
        });


        goods.setSpecs(specs);



        return goods;

    }
    private String chooseSegment(Object value, SpecParamDTO p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }
    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }
}
