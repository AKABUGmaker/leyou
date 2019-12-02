package com.leyou.item.controller;


import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//管理的是商品的整体模块
//有spu和sku两个请求,所以不写@RequestMapping
@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuDTO>> pageQuery(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "saleable",required = false)Boolean saleable
    ){

        return ResponseEntity.ok(this.goodsService.pageQuery(page,rows,key,saleable));
    }

    @PostMapping("goods")
    public ResponseEntity<Void> addGoods(@RequestBody SpuDTO spuDTO){

        this.goodsService.addGoods(spuDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
