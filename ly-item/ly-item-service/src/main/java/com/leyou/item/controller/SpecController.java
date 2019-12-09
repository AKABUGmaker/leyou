package com.leyou.item.controller;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecController {

    @Autowired
    SpecService specService;

    /**
     * 根据商品所属的分类的id查询对应商品的规格参数
     * @param cid
     * @return
     */
    @GetMapping("groups/of/category")
    public ResponseEntity<List<SpecGroupDTO>> querySpecGroupByCategoryId(@RequestParam("id") Long cid){

        return ResponseEntity.ok(this.specService.querySpecGroupByCategoryId(cid));
    }


    /**
     * 当前是为根据规格参数组id查询规格参数集合
     * @param gid
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParamDTO>> querySpecParams(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "searching",required = false)Boolean searching){

        return ResponseEntity.ok(this.specService.querySpecParams(gid,cid,searching));
    }


    /**
     * 根据分类同时查询规格参数组以及组内参数
     * @param cid
     * @return
     */
    @GetMapping("of/category")
    public ResponseEntity<List<SpecGroupDTO>> querySpecByCid(@RequestParam("cid")Long cid){
        return ResponseEntity.ok(this.specService.querySpecByCid(cid));
    }


}
