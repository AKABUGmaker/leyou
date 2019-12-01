package com.leyou.item.controller;

import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父类的id,查询对应的子
     * @param pid
     * @return
     */
    @GetMapping("of/parent")
    public ResponseEntity<List<CategoryDTO>> queryCategoryByParentId(@RequestParam("pid")Long pid){

        return ResponseEntity.ok(this.categoryService.queryCategoryByParentId(pid));
    }


    /**
     * 根据品牌id查询对应的分类
     * 用于数据回显
     * @return
     */
    @GetMapping("of/brand")
    public ResponseEntity<List<CategoryDTO>> queryCategoryByBrandId(@RequestParam("id")Long bid){

        return ResponseEntity.ok(this.categoryService.queryCategoryByBrandId(bid));
    }

}
