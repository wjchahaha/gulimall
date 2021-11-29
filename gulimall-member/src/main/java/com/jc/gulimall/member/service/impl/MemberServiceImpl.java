package com.jc.gulimall.member.service.impl;

import com.jc.common.vo.MemberEntity;
import com.jc.gulimall.member.entity.MemberReceiveAddressEntity;
import com.jc.gulimall.member.exception.PhoneNoUniqueException;
import com.jc.gulimall.member.exception.UserNameNoUniqueException;
import com.jc.gulimall.member.service.MemberLevelService;
import com.jc.gulimall.member.vo.GiteeUserVo;
import com.jc.gulimall.member.vo.MemberLoginVo;
import com.jc.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.Query;

import com.jc.gulimall.member.dao.MemberDao;
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

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String username = vo.getLoginacct();
        String password = vo.getPassword();
        //去数据库中查询
        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>()
                .eq("username", username)
                .or()
                .eq("mobile",username));
        if (entity == null){
            return null;
        }else{
            String password1 = entity.getPassword();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean matches = encoder.matches(password, password1);
            if (matches){
                return entity;
            }
            return null;
        }

    }

    @Override
    public MemberEntity oauthLogin(GiteeUserVo vo) {
        //获取用户id看用户是否在数据库中有

        long id = vo.getId();

        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("gitee_id", id));
        if (entity != null){//如果注册过
            System.out.println("此Gitee不是新用户直接登录！");
            return entity;
        }else {//如果没注册过 则进行注册
            MemberEntity regist = new MemberEntity();
            regist.setLevelId(1L);
            regist.setGiteeId(vo.getId());
            regist.setUsername(vo.getName());
            regist.setNickname(vo.getName());
            System.out.println("此Gitee是个新用户,要进行注册！");
            this.baseMapper.insert(regist);
            return regist;
        }
    }



}