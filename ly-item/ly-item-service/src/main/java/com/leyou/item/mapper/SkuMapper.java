package com.leyou.item.mapper;

import com.leyou.item.dto.SkuDTO;
import com.leyou.item.entily.Sku;
import tk.mybatis.mapper.additional.idlist.DeleteByIdListMapper;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

public interface SkuMapper extends Mapper<Sku> , InsertListMapper<Sku>, DeleteByIdListMapper<Sku,Long>, SelectByIdListMapper<Sku,Long> {
}
