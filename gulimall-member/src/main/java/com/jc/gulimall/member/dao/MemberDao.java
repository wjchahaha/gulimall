package com.jc.gulimall.member.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jc.common.vo.MemberEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 会员
 * 
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-17 11:27:10
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {

    int checkPhoneUnique(@Param("phone") String phone);

    int checkUserNameUnique(@Param("userName") String userName);


}
