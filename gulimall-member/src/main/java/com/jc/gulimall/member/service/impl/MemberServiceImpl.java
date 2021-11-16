package com.jc.gulimall.member.service.impl;

import com.jc.gulimall.member.exception.PhoneNoUniqueException;
import com.jc.gulimall.member.exception.UserNameNoUniqueException;
import com.jc.gulimall.member.service.MemberLevelService;
import com.jc.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.Query;

import com.jc.gulimall.member.dao.MemberDao;
import com.jc.gulimall.member.entity.MemberEntity;
import com.jc.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberDao memberDao;
    @Autowired
    private MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        Long  defaultLevel = memberLevelService.getDefaultLevel();
        MemberEntity memberEntity = new MemberEntity();

        //检查用户名和手机号是否唯一  为了让controller感知到异常 异常机制
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());
        //如果异常发生了则不会到这里
        memberEntity.setUsername(vo.getUserName());
        memberEntity.setMobile(vo.getPhone());

        memberEntity.setLevelId(defaultLevel);
        //密码加密
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        memberDao.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneNoUniqueException {
       if(memberDao.checkPhoneUnique(phone) > 0){
           throw new PhoneNoUniqueException();
       }
    }

    @Override
    public void checkUserNameUnique(String userName) throws UserNameNoUniqueException {
        if(memberDao.checkUserNameUnique(userName) > 0){
            throw new UserNameNoUniqueException();
        }
    }

}