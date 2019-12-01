package com.leyou.item.mapper;

import com.leyou.item.entily.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {

    int insertCategoryBrand(@Param("cids")List<Long> cids,@Param("bid") Long bid);

    @Delete("delete from tb_category_brand where brand_id = #{bid}")
    int deleteCategoryBrand(@Param("bid") Long bid);
}