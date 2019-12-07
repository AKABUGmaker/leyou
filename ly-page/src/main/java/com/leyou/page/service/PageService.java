package com.leyou.page.service;


import com.leyou.item.clients.ItemClient;
import com.leyou.item.dto.SpuDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class PageService {

    @Autowired
    private ItemClient itemClient;

    public Map<String, Object> LoadData(Long spuId) {

        SpuDTO spuDTO = this.itemClient.querySpuById(spuId);

        HashMap<String, Object> result = new HashMap<>();

        result.put("spu",spuDTO);

        return result;

    }
}
