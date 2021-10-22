# gulimall
谷粒商城
# 项目后端接口api文档地址：https://easydoc.xyz/s/78237135/ZUqEdvA4/hKJTcbfd

# 项目介绍
gulimall（谷粒商城） 项目是一套电商项目，包括前台商城系统以及后台管理系统，基于springboot + SpringCloud + SpringCloudAlibaba + MyBatis-Plus实现，采用 Docker 容器化部署。前台商城系统包括：用户登录、注册、商品搜索、商品详情、购物车、下订单流程、秒杀活动等模块。后台管理系统包括：系统管理、商品系统、优惠营销、库存系统、订单系统、用户系统、内容管理等七大模块。

# 项目演示
## 前台部分
![image](https://user-images.githubusercontent.com/76938084/137830559-f1fd13ba-e6e9-477e-ac81-47f515af8487.png)

![image](https://user-images.githubusercontent.com/76938084/137830536-c34cc634-ca7b-4fa7-a4a5-874777badd34.png)


## 后台管理系统


# 项目结构
````
gulimall
├── gulimall-common -- 工具类及通用代码
├── renren-generator -- 人人开源项目的代码生成器
├── gulimall-auth-server -- 认证中心（社交登录、OAuth2.0、单点登录）
├── gulimall-cart -- 购物车服务
├── gulimall-coupon -- 优惠卷服务
├── gulimall-gateway -- 统一配置网关
├── gulimall-order -- 订单服务
├── gulimall-product -- 商品服务
├── gulimall-search -- 检索服务
├── gulimall-seckill -- 秒杀服务
├── gulimall-third-party -- 第三方服务
├── gulimall-ware -- 仓储服务
└── gulimall-member -- 会员服务
````

# 技术选型
SpringBoot+SpringCloud+SpringCloudAlibaba+MyBatis-Plus+Elasticsearch+Redisson+Docker+OSS;


# 业务架构图
![image](https://user-images.githubusercontent.com/76938084/137830407-26c8f655-d0dc-459a-8d23-86c71405b9f3.png)

