package com.leyou.service;

import com.leyou.entily.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> queryCategoryByParentId(Long pid){
        Category record = new Category();
        record.setParentId(pid);
       List<Category> categories =  this.categoryMapper.select(record);
    }
}
