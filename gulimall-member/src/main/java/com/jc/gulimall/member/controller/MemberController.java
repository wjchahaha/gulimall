package com.jc.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.jc.common.exception.BizCodeEnume;
import com.jc.common.vo.MemberEntity;
import com.jc.gulimall.member.feign.CouponFeignService;
import com.jc.gulimall.member.exception.PhoneNoUniqueException;
import com.jc.gulimall.member.exception.UserNameNoUniqueException;
import com.jc.gulimall.member.vo.GiteeUserVo;
import com.jc.gulimall.member.vo.MemberLoginVo;
import com.jc.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.jc.gulimall.member.service.MemberService;
import com.jc.common.utils.PageUtils;
import com.jc.common.utils.R;



/**
 * 会员
 *
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-17 11:27:10
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @PostMapping("oauth/login")
    public MemberEntity oauthLogin(@RequestBody GiteeUserVo vo){
        MemberEntity giteeUserVo =  memberService.oauthLogin(vo);

        return giteeUserVo;

    }


    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo){
        try {
            memberService.regist(vo);
        }catch (PhoneNoUniqueException e){
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        }catch (UserNameNoUniqueException e){
            return R.error(BizCodeEnume.USERNAME_EXIST_EXCEPTION.getCode(),BizCodeEnume.USERNAME_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }


    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){

        MemberEntity entity=memberService.login(vo);
        if (entity == null){
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getCode(),BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getMsg());
        }
        return R.ok().setData(entity);
    }

    @RequestMapping("/coupons")
    public R test(){
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("王金城");

        R memeberCoupons = couponFeignService.memberCoupons();

        R r = new R();

        return R.ok().put("member",memberEntity).put("coupons",memeberCoupons.get("coupons"));
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
