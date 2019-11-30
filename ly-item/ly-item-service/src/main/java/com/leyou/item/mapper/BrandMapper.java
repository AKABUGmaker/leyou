package com.leyou.item.mapper;

import com.leyou.item.entily.Brand;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {

    int insertCategoryBrand(@Param("cids")List<Long> cids,@Param("bid") Long bid);
}
