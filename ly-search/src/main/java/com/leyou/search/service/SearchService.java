package com.leyou.search.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.clients.ItemClient;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.pojo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    ItemClient itemClient;

    @Autowired
    private ElasticsearchTemplate esTemplate;

    public PageResult<GoodsDTO> pageQuery(SearchRequest searchRequest) {

        //查询条件构建工具
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //添加查询条件
        queryBuilder.withQuery(buildBasicQuery(searchRequest));

        //添加分页条件
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage() - 1, searchRequest.getSize()));

        //添加字段过滤条件
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));

        //执行查询

        AggregatedPage<Goods> goodsAggregatedPage = this.esTemplate.queryForPage(queryBuilder.build(), Goods.class);




        //把查询返回的分页结果，进行封装，三个参数
        return new PageResult<>(goodsAggregatedPage.getTotalElements(), goodsAggregatedPage.getTotalPages(), BeanHelper.copyWithCollection(goodsAggregatedPage.getContent(), GoodsDTO.class));

    }

    //查询聚合可搜索规格参数
    public Map<String, List<?>> filterQuery(SearchRequest searchRequest) {

        //查询条件构建工具
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //获取到查询条件
        QueryBuilder query = buildBasicQuery(searchRequest);

        //添加查询条件
        queryBuilder.withQuery(query);


        //查询不可避免，但是不要查询结果，
        queryBuilder.withPageable(PageRequest.of(0, 1));

        //聚合，添加聚合条件
        String brand_aggName = "brands";
        String category_aggName = "categories";

        queryBuilder.addAggregation(AggregationBuilders.terms(brand_aggName).field("brandId"));
        queryBuilder.addAggregation(AggregationBuilders.terms(category_aggName).field("categoryId"));


        //执行查询聚合
        AggregatedPage<Goods> goodsAggregatedPage = this.esTemplate.queryForPage(queryBuilder.build(), Goods.class);

        //获取到所有的聚合
        Aggregations aggregations = goodsAggregatedPage.getAggregations();

        //根据聚合名称，获取聚合结果
        LongTerms brandTerms = aggregations.get(brand_aggName);

        List<Long> brandIds = brandTerms.getBuckets()
                .stream()
                .map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());

        LongTerms categoryTerms = aggregations.get(category_aggName);

        List<Long> categoryIds = categoryTerms.getBuckets()
                .stream()
                .map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());


        Map<String, List<?>> result = new LinkedHashMap<>();

        //跨服务查询品牌对象，以及分类对象

        result.put("分类",this.itemClient.queryCategoryByIds(categoryIds));

        result.put("品牌",this.itemClient.queryBrandByIds(brandIds));


        //展示其他可搜索规格参数，前提条件就是，分类唯一（确定了分类）
        if (null!=categoryIds&&1==categoryIds.size()){
            //获取其他可搜索规格参数，以及其的聚合值
            getSpecs(categoryIds.get(0),query,result);
        }

        return result;
    }

    //动态查询封装可搜索规格参数以及对应的值
    private void getSpecs(Long cid, QueryBuilder query, Map<String, List<?>> result) {
        //根据分类id查询可搜索规格参数
        List<SpecParamDTO> specParamDTOS = this.itemClient.querySpecParams(null, cid, true);

        //对可搜索规格参数进行聚合操作

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(query);

        //限定输出查询结果数量
        queryBuilder.withPageable(PageRequest.of(0,1));

        //添加source过滤条件
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""},null));

        //添加聚合条件,聚合名称，可搜索规格参数名称，聚合的字段，可搜索规格参数名称前后拼接了特殊值
        specParamDTOS.forEach(specParamDTO -> {
            String name = specParamDTO.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        });

        //执行查询聚合
        AggregatedPage<Goods> goodsAggregatedPage = this.esTemplate.queryForPage(queryBuilder.build(), Goods.class);

        //解析聚合,获取所有的聚合
        Aggregations aggregations = goodsAggregatedPage.getAggregations();

        //循环解析聚合,聚合名称是可搜索参数名称
        specParamDTOS.forEach(specParamDTO -> {
            String name = specParamDTO.getName();

            StringTerms stringTerms = aggregations.get(name);

            List<String> values = stringTerms.getBuckets()
                    .stream()
                    .map(StringTerms.Bucket::getKeyAsString)
                    .collect(Collectors.toList());

            result.put(name,values);
        });

    }

    //构建查询条件
    private QueryBuilder buildBasicQuery(SearchRequest searchRequest) {

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        String key = searchRequest.getKey();

        if (StringUtils.isBlank(key)) {
            throw new LyException(ExceptionEnum.INVALID_REQUEST_PARAM);
        }

        //添加查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all",key).operator(Operator.AND));

        //添加过滤条件
        searchRequest.getFilter().entrySet().forEach(entry -> {
            String key1 = entry.getKey();
            String value = entry.getValue();

            if ("品牌".equals(key1)){
                key1 = "brandId";
            }else if("分类".equals(key1)){
                key1 = "categoryId";
            }else {
                key1 = "specs."+key1+".keyword";
            }

            queryBuilder.filter(QueryBuilders.termQuery(key1,value));
        });


        return queryBuilder;
    }
}
