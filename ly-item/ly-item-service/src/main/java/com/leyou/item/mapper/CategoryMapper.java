package com.leyou.item.mapper;

import com.leyou.item.entily.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


public interface CategoryMapper extends Mapper<Category> {

    @Select("select tc.id,tc.name from tb_category tc inner join tb_category_brand cb on tc.id = cb.category_id where cb.brand_id = #{bid}")
    List<Category> queryCategoryByBrandId(@Param("bid") Long bid);
}
