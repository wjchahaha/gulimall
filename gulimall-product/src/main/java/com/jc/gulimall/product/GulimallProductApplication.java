package com.jc.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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
 *
 *
 *   模板引擎：
 *   1.引入thymeleaf
 *   2.配置thymeleaf缓存关闭
 *   3.将静态文件放到static下
 *   4.html放到templates
 *   5.使用spring-boot-devtools 不重启服务器更新路修改页面
     *   5.1)引入dev-tools
 *      5.2)修改完页面ctrl+shirt+f9
 *
 *   6.整合redis
 *      6.1)引入场景启动器 starter
 *      6.2)配置redis的host，port等信息
 *      6.3)使用boot自动配置好的 StringRedisTemplate
 *   7.整合redisson
 *      7.1)引入redisson依赖
 *      7.2)配置
 *
 *   8.整合spring.cache
 *      8.1)引入spring.boot.starter.redis
 *      8.2)spring.cache.type= redis
 *      8.3)@EnableCaching
 *      8.4)方法上加注解
 *      @Cacheable: Triggers cache population.  把结果放入缓存  (出发缓存入口)
 *      @CacheEvict: Triggers cache eviction.  删除缓存
        @CachePut: Updates the cache without interfering with the method execution. 修改缓存在不影响方法执行的情况下
        @Caching: Regroups multiple cache operations to be applied on a method. 重新组合要应用于方法上的多个缓存操作
        @CacheConfig: Shares some common cache-related settings at class-level. 在类级别共享一些公共缓存相关设置
 *  9.spring.cache不足
 *      读模式:
 *          缓存击穿：大量并发一直请求一个不存的数据。解决:缓存一个null值：spring.cache.redis.cache-null-values=true
 *          缓存穿透：高并发请求那个数据，数据刚好过期，全部落到db。解决：加锁，springCache锁的粒度是当前服务，也没多大关系。
 *                    只有get(俩参数)哪个方法加锁了,锁的粒度是当前服务
         *            只有Cacheable能加锁
         *            public synchronized <T> T get(Object key, Callable<T> valueLoader)
 *          缓存雪崩：大量数据同时过期。解决：家随机时间。加过期时间。
         *          一般不会出现这个问题，因为缓存的时候时间节点就不同，所以这个被忽略了。
 *      写模式:
 *      1）读写加锁：
 *      2）引入Cancle,监控数据库，如果db有更新则缓存
 *      3）读多写多：直接去数据库
 *
 */

@EnableRedisHttpSession
@EnableCaching
@EnableTransactionManagement
@EnableFeignClients(basePackages = "com.jc.gulimall.product.feign")
@EnableDiscoveryClient
@MapperScan("com.jc.gulimall.product.dao")
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
