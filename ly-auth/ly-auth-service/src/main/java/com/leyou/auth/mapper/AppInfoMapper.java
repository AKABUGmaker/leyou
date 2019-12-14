package com.leyou.auth.mapper;

import com.leyou.auth.entity.ApplicationInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface AppInfoMapper extends Mapper<ApplicationInfo> {

    @Select("select target_id from tb_application_privilege where service_id=#{sid}")
    List<Long> selectTargetList(@Param("sid")Long id);

    @Select("select * from tb_application where id = #{id}")
    ApplicationInfo selectById(Long id);
}
