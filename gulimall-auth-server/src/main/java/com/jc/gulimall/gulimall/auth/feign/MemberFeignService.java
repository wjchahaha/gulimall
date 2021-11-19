package com.jc.gulimall.gulimall.auth.feign;

import com.jc.common.utils.R;
import com.jc.gulimall.gulimall.auth.vo.GiteeUserVo;
import com.jc.gulimall.gulimall.auth.vo.MemberEntity;
import com.jc.gulimall.gulimall.auth.vo.UserLoginVo;
import com.jc.gulimall.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("member/member/regist")
    public R regist(@RequestBody UserRegistVo vo);

    @PostMapping("member/member/login")
    public R login(@RequestBody UserLoginVo vo);

    @PostMapping("member/member/oauth/login")
    public MemberEntity oauthLogin(@RequestBody GiteeUserVo vo);


}
