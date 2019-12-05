package com.leyou.search.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.pojo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    public PageResult<GoodsDTO> pageQuery(SearchRequest searchRequest) {

        String key = searchRequest.getKey();

        if (StringUtils.isBlank(key)) {
            throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
        }

        //查询条件搭建工具
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //添加查询条件
        queryBuilder.withQuery(QueryBuilders.matchQuery("all",key));

        //添加分页条件
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage()-1,searchRequest.getSize()));

        //添加字段过滤条件
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));

        //执行查询
        AggregatedPage<Goods> goodsAggregatedPage = this.esTemplate.queryForPage(queryBuilder.build(), Goods.class);

        //把查询返回的分页结果封装
        return new PageResult<>(goodsAggregatedPage.getTotalElements(), goodsAggregatedPage.getTotalPages(), BeanHelper.copyWithCollection(goodsAggregatedPage.getContent(), GoodsDTO.class));

    }
}
