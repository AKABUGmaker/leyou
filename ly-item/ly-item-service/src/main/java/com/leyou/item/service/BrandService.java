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
}
