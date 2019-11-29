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
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;


import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<BrandDTO> pageQuery(Integer page,Integer rows,String sortBy,Boolean desc){
        PageHelper.startPage(page,rows);

        //动态sql拼接工具
        Example example = new Example(Brand.class);


        //如果不为空,要排序
        if (!StringUtils.isBlank(sortBy)){
            //set方法说明sql语句中写好了OrderBy条件
            //select * from tb_xx where a = b order by id DESC
            //条件记得加空格
            example.setOrderByClause(sortBy+(desc?" DESC":" ASC"));
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
}
