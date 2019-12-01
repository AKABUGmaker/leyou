package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entily.SpecGroup;
import com.leyou.item.entily.SpecParam;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Service
public class SpecService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroupDTO> querySpecGroupByCategoryId(Long cid) {


        SpecGroup record = new SpecGroup();

        record.setCid(cid);

        List<SpecGroup> specGroups = this.specGroupMapper.select(record);

        if (CollectionUtils.isEmpty(specGroups)){
            throw new LyException(ExceptionEnum.DATA_NOT_FOUND);
        }

        return BeanHelper.copyWithCollection(specGroups,SpecGroupDTO.class);
    }


    /**
     * 根据规格参数组的id查询组内参数
     * @param gid
     * @return
     */
    public List<SpecParamDTO> querySpecParams(Long gid) {

        SpecParam record = new SpecParam();
        record.setGroupId(gid);

        List<SpecParam> specParams = this.specParamMapper.select(record);

        if (CollectionUtils.isEmpty(specParams)){
            throw new LyException(ExceptionEnum.DATA_NOT_FOUND);
        }

        return BeanHelper.copyWithCollection(specParams,SpecParamDTO.class);
    }
}
