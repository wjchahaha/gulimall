package com.jc.gulimall.member.dao;

import com.jc.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-17 11:27:10
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
