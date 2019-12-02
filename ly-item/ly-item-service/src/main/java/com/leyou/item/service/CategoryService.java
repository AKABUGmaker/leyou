package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.entily.Category;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public List<CategoryDTO> queryCategoryByParentId(Long pid){
        Category record = new Category();
        record.setParentId(pid);
        //根据pid查询对应的category分类
       List<Category> categories =  this.categoryMapper.select(record);

       //判断分类查询的结果是否为空
       if(CollectionUtils.isEmpty(categories)){
           //如果结果为空,抛出对应的异常
           throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
       }

       //通过该方法将查询到的数据集合转换为另一个相似的类型
        //该类型中属性名一样的才能转换
        List<CategoryDTO> categoryDTOS = BeanHelper.copyWithCollection(categories, CategoryDTO.class);

        return categoryDTOS;
    }

    public List<CategoryDTO> queryCategoryByBrandId(Long bid) {

        List<Category> categories = this.categoryMapper.queryCategoryByBrandId(bid);

        if (CollectionUtils.isEmpty(categories)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }

        return BeanHelper.copyWithCollection(categories,CategoryDTO.class);
    }

    public List<CategoryDTO> queryCategoryByCategoryIdsInGoods(List<Long> categoryIds) {

        //通过sql中的in关键字将集合中的id一次性传进去
     List<Category> categories =  this.categoryMapper.selectByIdList(categoryIds);

     if (CollectionUtils.isEmpty(categories)){
         throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
     }

     return BeanHelper.copyWithCollection(categories,CategoryDTO.class);
    }
}
