package com.leyou.search.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SpuDTO;
import com.leyou.search.clients.ItemClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.reponsitory.GoodsRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestIndexService {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Autowired
    private IndexService indexService;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ItemClient itemClient;

    @Test
    public void testCreateIndexesAndPutMapping() {
        this.esTemplate.createIndex(Goods.class);
        this.esTemplate.putMapping(Goods.class);
    }

    @Test
    public void testLoadData() {
        //查询的是上架的商品,saleable应为true
        int page = 1;
        while (true) {
            //分页查询spu
            PageResult<SpuDTO> spuDTOResponseEntity = itemClient.pageQuery(page, 50, null, null);

            page++;

            //没有查询到就结束
            if (null == spuDTOResponseEntity | CollectionUtils.isEmpty(spuDTOResponseEntity.getItems())) {
                break;
            }

            List<SpuDTO> spuDTOS = spuDTOResponseEntity.getItems();

            List<Goods> goods = new ArrayList<>();
            spuDTOS.forEach(spuDTO -> {
                //把spu转为Goods
                goods.add(this.indexService.buildGoods(spuDTO));
            });

            this.goodsRepository.saveAll(goods);
        }

    }
}
