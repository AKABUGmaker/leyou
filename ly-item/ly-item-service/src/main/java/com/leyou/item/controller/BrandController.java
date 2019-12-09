package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     *品牌分页查询
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @param key
     * @return
     */
    @GetMapping("page")
    public ResponseEntity<PageResult<BrandDTO>> pageQuery(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            //非必要参数,请求时可以没有
            @RequestParam(value = "sortBy",required = false)String sortBy,//根据哪个属性排序
            @RequestParam(value = "desc",required = false)Boolean desc, //排序规则
            @RequestParam(value = "key",required = false)String key //搜索输入

    ){

        return ResponseEntity.ok(this.brandService.pageQuery(page,rows,sortBy,desc,key));
    }

    /**
     * 品牌新增
     * @param brandDTO
     * @param cids
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addBrand(
            BrandDTO brandDTO, //在编译过程中会根据内部getter和setter方法生成对应 @RequestParam,不过都是required为false
            @RequestParam("cids")List<Long> cids
    ){

        this.brandService.addBrand(brandDTO,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<Void> updateBrand(
            BrandDTO brandDTO,
            @RequestParam(value = "cids") List<Long> cids
    ){
        this.brandService.updateBrand(brandDTO,cids);

        return ResponseEntity.ok().build();
    }

    @GetMapping("of/category")
    public ResponseEntity<List<BrandDTO>> queryBrandByCategoryIdInGoods(@RequestParam("id")Long cid){

        return ResponseEntity.ok(this.brandService.queryBrandByCategoryIdInGoods(cid));

    }

    /**
     * 根据id集合查询品牌对象
     * @param ids
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<BrandDTO>> queryBrandByIds(@RequestParam("ids")List<Long> ids){

        return ResponseEntity.ok(this.brandService.queryBrandByIds(ids));
    }

    /**
     * 根据brandId查询对应的品牌信息
     * @param brandId
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<BrandDTO> queryBrandById(@PathVariable("id")Long brandId){
        return ResponseEntity.ok(this.brandService.queryBrandById(brandId));
    }

}
