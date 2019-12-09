package com.leyou.item.clients;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("item-service")
public interface ItemClient {

    @GetMapping("spu/page")
    PageResult<SpuDTO> pageQuery(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "saleable",required = false)Boolean saleable
    );

    @GetMapping("category/list")
    List<CategoryDTO> queryCategoryByIds(@RequestParam("ids") List<Long> idList);

    /**
     * 根据spuId查询对应的sku
     * @param spuId
     * @return
     */
    @GetMapping("sku/of/spu")
    List<SkuDTO> querySkuBySpuId(@RequestParam("id")Long spuId);

    /**
     * 目前是根据规格参数组id，查询规格参数集合
     *
     * @param gid
     * @return
     */
    @GetMapping("spec/params")
    List<SpecParamDTO> querySpecParams(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching);

    /**
     * 根据spuId查询对应的spuDetail
     *
     * @param spuId
     * @return
     */
    @GetMapping("spu/detail")
    SpuDetailDTO querySpuDetailBySpuId(@RequestParam("id") Long spuId);

    /**
     * 根据id集合查询品牌对象
     *
     * @param ids
     * @return
     */
    @GetMapping("brand/list")
    List<BrandDTO> queryBrandByIds(@RequestParam("ids") List<Long> ids);


    @GetMapping("spu/{id}")
    SpuDTO querySpuById(@PathVariable("id")Long spuId);

    /**
     * 根据brandId查询对应的品牌信息
     *
     * @param brandId
     * @return
     */
    @GetMapping("brand/{id}")
    BrandDTO queryBrandById(@PathVariable("id") Long brandId);

    /**
     * 根据分类同时查询规格参数组以及组内参数
     * @param cid
     * @return
     */
    @GetMapping("spec/of/category")
    List<SpecGroupDTO> querySpecsByCid(@RequestParam("cid")Long cid);
}
