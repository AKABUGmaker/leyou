package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.entily.Sku;
import com.leyou.item.entily.Spu;
import com.leyou.item.entily.SpuDetail;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    public PageResult<SpuDTO> pageQuery(Integer page, Integer rows, String key, Boolean saleable) {

        PageHelper.startPage(page,rows);

        Example example = new Example(Spu.class);

        Example.Criteria criteria = example.createCriteria();

        if (!StringUtils.isBlank(key)){
            criteria.andLike("name","%"+key+"%");
        }

        if (null != saleable){
            criteria.andEqualTo("saleable",saleable);
        }

        List<Spu> spus = this.spuMapper.selectByExample(example);


        if (CollectionUtils.isEmpty(spus)){
            throw new LyException(ExceptionEnum.DATA_NOT_FOUND);
        }

        PageInfo<Spu> spuPageInfo = new PageInfo<>(spus);

        List<SpuDTO> spuDTOS = BeanHelper.copyWithCollection(spus, SpuDTO.class);

        spuDTOS.forEach(spuDTO -> {
            List<CategoryDTO> categoryDTOS = this.categoryService.queryCategoryByCategoryIdsInGoods(spuDTO.getCategoryIds());

            String names = categoryDTOS.stream() //使得categorDtos可以开启迭代模式Stream<CategoryDTO>
                    .map(CategoryDTO::getName) //从每个对象中取出name，此时Stream<String>
                    .collect(Collectors.joining("/")); //Stream<String>  str1/str2/str3

            //StringUtils.join表示拼接，把数组拼接成字符串，
            spuDTO.setCategoryName(names);

            BrandDTO brandDTO = this.brandService.queryBrandByBrandIdInGoods(spuDTO.getBrandId());

            spuDTO.setBrandName(brandDTO.getName());
        });

        return new PageResult<>(spuPageInfo.getTotal(),spuPageInfo.getPages(),spuDTOS);
    }

    /**
     * 新增商品，
     * 三张表，spu，spuDetail，sku
     * @param spuDTO
     */
    @Transactional
    public void addGoods(SpuDTO spuDTO) {

        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);

        //保存spu,并回显主键
        int count = this.spuMapper.insertSelective(spu);

        if (1!=count){
            throw new LyException(ExceptionEnum.DATA_SAVE_ERROR);
        }

        //保存spuDetail
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), SpuDetail.class);
        //保存spuDto并不会把主键给spuDTO,从spu将主键给spuDetail
        spuDetail.setSpuId(spu.getId());

        count = this.spuDetailMapper.insertSelective(spuDetail);

        if (1!=count){
            throw new LyException(ExceptionEnum.DATA_SAVE_ERROR);
        }


        List<SkuDTO> skuDTOs = spuDTO.getSkus();

        List<Sku> skus = new ArrayList<>();

        for (SkuDTO skuDTO : skuDTOs) {
            skuDTO.setSpuId(spu.getId());
            skus.add(BeanHelper.copyProperties(skuDTO,Sku.class));
        }

        count = this.skuMapper.insertList(skus);

        if (count!=skus.size()){
            throw new LyException(ExceptionEnum.DATA_SAVE_ERROR);
        }
    }
}
