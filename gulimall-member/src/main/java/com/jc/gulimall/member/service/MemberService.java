package com.jc.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jc.common.utils.PageUtils;
import com.jc.common.vo.MemberEntity;
import com.jc.gulimall.member.entity.MemberReceiveAddressEntity;
import com.jc.gulimall.member.exception.PhoneNoUniqueException;
import com.jc.gulimall.member.exception.UserNameNoUniqueException;
import com.jc.gulimall.member.vo.GiteeUserVo;
import com.jc.gulimall.member.vo.MemberLoginVo;
import com.jc.gulimall.member.vo.MemberRegistVo;

import java.util.List;
import java.util.Map;

/**
 * 会员
 *
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-17 11:27:10
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo) throws PhoneNoUniqueException, UserNameNoUniqueException;

    void checkPhoneUnique(String phone) throws PhoneNoUniqueException;

    void checkUserNameUnique(String userName) throws UserNameNoUniqueException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity oauthLogin(GiteeUserVo vo);


}

