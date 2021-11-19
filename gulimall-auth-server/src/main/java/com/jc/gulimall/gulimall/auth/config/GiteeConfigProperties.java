package com.jc.gulimall.gulimall.auth.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-19 13:33
**/
@Component
@ConfigurationProperties(prefix = "gitee.oauth")
@Data
public class GiteeConfigProperties {

    public String clientid;

    public String clientsecret;

    public String callback;


}
