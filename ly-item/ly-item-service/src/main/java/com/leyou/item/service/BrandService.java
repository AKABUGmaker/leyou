package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entily.Brand;
import com.leyou.item.mapper.BrandMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;


import java.util.Date;
import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<BrandDTO> pageQuery(Integer page,Integer rows,String sortBy,Boolean desc,String key){
        PageHelper.startPage(page,rows);

        //动态sql拼接工具的生成器
        Example example = new Example(Brand.class);


        //如果不为空,要排序
        if (!StringUtils.isBlank(sortBy)){
            //set方法说明sql语句中写好了OrderBy条件
            //select * from tb_xx where a = b order by id DESC
            //条件记得加空格
            example.setOrderByClause(sortBy+(desc?" DESC":" ASC"));
        }

        if (!StringUtils.isBlank(key)){
            //动态sql拼接工具
            Example.Criteria criteria = example.createCriteria();
            //拼接动态sql,
            //连接方式为and : select * from tb_xx where a = b order by id DESC and ...
            //比较方式Like : 模糊比较
            criteria.andLike("name","%"+key+"%");
        }


        //执行sql查询时加入example条件
        List<Brand> brands = this.brandMapper.selectByExample(example);

        if (CollectionUtils.isEmpty(brands)){
            throw new LyException((ExceptionEnum.BRAND_NOT_FOUND));
        }

        //自动统计查询到的元素的个数以及计算总的页面数量
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);

        /**
         * 封装分页查询品牌的结果
         * 参数1:总元素个数
         * 参数2:总页面数
         * 参数3:页面需要的数据,通过工具类转换
         */
        return new PageResult<>(pageInfo.getTotal(),pageInfo.getPages(), BeanHelper.copyWithCollection(brands,BrandDTO.class));

    }


    //不只操纵一次数据库,需要添加事务注解
    @Transactional
    public void addBrand(BrandDTO brandDTO,List<Long> cids){

        Brand brand = BeanHelper.copyProperties(brandDTO, Brand.class);


        //新增品牌
        int count = this.brandMapper.insertSelective(brand);

        if (1!=count){
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }

        //与分类和品牌中间表添加外键
        count = this.brandMapper.insertCategoryBrand(cids,brand.getId());

        if (count!=cids.size()){
            throw new LyException(ExceptionEnum.BRAND_CATEGORY_SAVE_ERROR);
        }
    }

    @Transactional
    public void updateBrand(BrandDTO brandDTO, List<Long> cids) {


        Brand brand = BeanHelper.copyProperties(brandDTO, Brand.class);

        //Selective修改和新增方法中如果加入了Selective，表示在修改操作时，不会对所有的字段操作，只操作有值得字段
        //int count = this.brandMapper.updateByPrimaryKeySelective(brand);

        //先查询数据库，把会被误伤的字段，先查询，然后赋值
        Brand oldBrand = this.brandMapper.selectByPrimaryKey(brandDTO.getId());

        brand.setCreateTime(oldBrand.getCreateTime());
        brand.setUpdateTime(new Date());

        //这个是对所有的字段进行修改，有值的改，没有值得的置null
        int count = this.brandMapper.updateByPrimaryKey(brand);

        if (1!=count){
            throw new LyException(ExceptionEnum.DATA_MODIFY_ERROR);
        }

        //根据品牌id清空对应的分类
        count = this.brandMapper.deleteCategoryBrand(brand.getId());
        //TODO 删除品牌时要先查询品牌对应的分类有多少个，删除后要进行校验


        //重建品牌和分类之间的关系

        count = this.brandMapper.insertCategoryBrand(cids,brand.getId());

        if (count!=cids.size()){
            throw new LyException(ExceptionEnum.BRAND_CATEGORY_SAVE_ERROR);
        }
    }

    public BrandDTO queryBrandByBrandIdInGoods(Long brandId) {

        Brand brand = this.brandMapper.selectByPrimaryKey(brandId);

        if (null == brand){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        return BeanHelper.copyProperties(brand,BrandDTO.class);
    }

    public List<BrandDTO> queryBrandByCategoryIdInGoods(Long cid) {

       List<Brand> brands = this.brandMapper.queryBrandByCategoryIdInGoods(cid);

       if (CollectionUtils.isEmpty(brands)){
           throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
       }

       return BeanHelper.copyWithCollection(brands,BrandDTO.class);
    }
}
