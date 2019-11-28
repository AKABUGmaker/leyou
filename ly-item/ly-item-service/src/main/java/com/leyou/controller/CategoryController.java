package com.leyou.controller;

import com.leyou.entily.Category;
import com.leyou.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("Category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父类的id,查询对应的子
     * @param pid
     * @return
     */
    @GetMapping("of/parent")
    public ResponseEntity<List<Category>> queryCategoryByParentId(@RequestParam("pid")Long pid){

        return ResponseEntity.ok(this.categoryService.queryCategoryByParentId(pid));
    }
}
