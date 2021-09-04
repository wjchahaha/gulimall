package com.jc.gulimall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1.整合mybatis-plus
 *   1)导入依赖
 *   <!--        mybatis-plus-->
 *         <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.3.2</version>
 *         </dependency>
 *   2)配置
 *      1.配置数据源
 *          1)导入数据库驱动
 *          2）配置数据源
 *              配置连接数据库信息
 *      2.配置mybatis-plus
 *          1）使用@MapperScan
 *          2) 告诉Mybatis-Plus,sql映射文件位置
 *  3）逻辑删除
 *      1）配置逻辑删除字段
 *       logic-delete-field: showStatus
 *       logic-delete-value: 0
 *       logic-not-delete-value: 1
 *   4）JSR303
 *      1) 给Bean字段添加校验注解，并定义自己的message提示
 *      2) 在请求参数中添加@Valid  例如@Valid @RequestBody BrandEntity brand
 *       效果：校验错误会有响应
 *
 *       添加另一个字段BindingResult
 *
 *       分组校验：
 *       在Entity字段注解的后边加上 groups={xx.class}
 *         //@NotNull(message = "logo不能为空！",groups = {AddGroup.class,UpdateGroup.class})
 *       在对应的Controller方法上，要检验的Bean@
 *
 *       自定义校验：
 *       1）编写一个自定义的校验注解
 *       2）编写一个自定义的校验器
 *       3)将他们关联起来
 *   5 )统一的异常处理
 */
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
