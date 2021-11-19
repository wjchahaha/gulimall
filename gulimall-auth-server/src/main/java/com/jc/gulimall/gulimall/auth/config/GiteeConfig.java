package com.jc.gulimall.gulimall.auth.config;

import com.jc.gulimall.gulimall.auth.vo.Gitee;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-19 13:38
**/
@Configuration
public class GiteeConfig {

    @Bean
    public Gitee gitePro(GiteeConfigProperties e){
        Gitee gitee = new Gitee();
        gitee.setCLIENTID(e.getClientid());
        gitee.setCLIENTSECRET(e.getClientsecret());
        gitee.setURL(e.getCallback());
       return gitee;
    }
}
