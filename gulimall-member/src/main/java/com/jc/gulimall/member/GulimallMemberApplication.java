package com.jc.gulimall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * 想要远程调用
 *  1.引入open-feign远程调用
 *  2.编写接口，告诉springcloud这个接口需要调用远程服务
 *      1.声明接口的每一个方法 都是调用哪个远程服务的哪个请求
 *  3.开启远程调用功能
 */
@EnableFeignClients("com.jc.gulimall.member.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallMemberApplication.class, args);
    }

}
