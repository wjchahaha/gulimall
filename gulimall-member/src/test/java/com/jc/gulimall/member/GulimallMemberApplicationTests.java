package com.jc.gulimall.member;

import com.jc.gulimall.member.dao.MemberDao;
import com.jc.gulimall.member.service.MemberService;
import org.apache.commons.codec.cli.Digest;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
class GulimallMemberApplicationTests {

    @Autowired
    private MemberService memberService;

    @Test
    void contextLoads() {
        String s = DigestUtils.md5Hex("空间的拉数据库里的几啊杀戮空间阿瓦发顺丰干什么，你123");
        System.out.println(s);
    }


}
