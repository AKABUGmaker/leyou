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
import java.util.Map;
import java.util.stream.Collectors;


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
    public List<SpecParamDTO> querySpecParams(Long gid,Long cid,Boolean searching) {

        SpecParam record = new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setSearching(searching);

        List<SpecParam> specParams = this.specParamMapper.select(record);

        if (CollectionUtils.isEmpty(specParams)){
            throw new LyException(ExceptionEnum.DATA_NOT_FOUND);
        }

        return BeanHelper.copyWithCollection(specParams,SpecParamDTO.class);
    }

    public List<SpecGroupDTO> querySpecByCid(Long cid){
        //查询规格组
        List<SpecGroupDTO> groupList = querySpecGroupByCategoryId(cid);

        //查询分类下所有规格参数
        //通过groupingBy对规格参数进行分组
        List<SpecParamDTO> params = querySpecParams(null, cid, null);

        //将规格参数按照groupId进行分组，得到每个group下的param的集合
        Map<Long,List<SpecParamDTO>> paramMap = params.stream()
                .collect(Collectors.groupingBy(SpecParamDTO::getGroupId));

        //将其填写到group中
        for (SpecGroupDTO groupDTO : groupList){
            groupDTO.setParams(paramMap.get(groupDTO.getId()));
        }
        return groupList;
    }
}
