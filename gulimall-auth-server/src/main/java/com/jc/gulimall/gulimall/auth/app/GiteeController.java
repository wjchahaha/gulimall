package com.jc.gulimall.gulimall.auth.app;

import com.alibaba.fastjson.JSONObject;
import com.jc.gulimall.gulimall.auth.feign.MemberFeignService;
import com.jc.gulimall.gulimall.auth.vo.Gitee;
import com.jc.gulimall.gulimall.auth.vo.GiteeUserVo;
import com.jc.gulimall.gulimall.auth.vo.MemberEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.PrivateKey;
import java.util.UUID;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-19 13:30
**/
@Controller
public class GiteeController {


    @Autowired
    Gitee gitee;

    @Autowired
    private MemberFeignService memberFeignService;

    /**
     * 将用户导向到认证页面
     */
    @GetMapping("/gitee/auth")
    public String qqAuth(HttpSession session) {
        // 用于第三方应用防止CSRF攻击
//        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
//        session.setAttribute("state", uuid);

        // Step1：获取Authorization Code  //scope=user_info 就是用户授权信息
        String url = "https://gitee.com/oauth/authorize?response_type=code" +
                "&client_id=" + gitee.getCLIENTID() +
                "&redirect_uri=" + URLEncoder.encode(gitee.getURL())+
                "&scope=user_info";

//        System.out.println(url);
        return "redirect:"+url;
    }


    /**
     * 授权回调  request是码云服务器传回给浏览器一个请求   用户授权吗 code
     */
    @GetMapping(value = "/success")
    public String qqCallback(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession();
        // 得到Authorization Code
        String code = request.getParameter("code");
        System.out.println("用户授权码-->"+code);
        String state = request.getParameter("state");

        String uuid = (String) session.getAttribute("state");

//        // 验证信息我们发送的状态码
//        if (null != uuid) {
//            // 状态码不正确，直接返回登录页面 防止CSRF攻击
//            if (!uuid.equals(state)) {
//                return "redirect:http://auth.gulimall.com/login.html";
//            }
//        }

        // Step2：通过Authorization Code获取Access Token
        String url = "https://gitee.com/oauth/token?grant_type=authorization_code" +
                "&client_id=" + gitee.getCLIENTID() +
                "&client_secret=" + gitee.getCLIENTSECRET() +
                "&code=" + code +
                "&redirect_uri=" + gitee.getURL();
        JSONObject accessTokenJson = getAccessToken(url);

        // Step3: 获取用户信息
        url = "https://gitee.com/api/v5/user?access_token=" + accessTokenJson.get("access_token");
        JSONObject jsonObject = getUserInfo(url);

        //将json转为实体类

        /**
         * 获取到用户信息之后，就该写你自己的业务逻辑了 携带的数据
         * 看用户是不是第一次登录
         */
        GiteeUserVo giteeUser =(GiteeUserVo) JSONObject.parseObject(jsonObject.toString(),GiteeUserVo.class);
        MemberEntity memberEntity = memberFeignService.oauthLogin(giteeUser);
        System.out.println("用户已保护信息已获取完成！即将要跳转,用户信息"+jsonObject);

        return "redirect:http://gulimall.com";
    }

    /**
     * 获取Access Token
     * post
     */
    public static JSONObject getAccessToken(String url) throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        HttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();
        if (null != entity) {
            String result = EntityUtils.toString(entity, "UTF-8");
            return JSONObject.parseObject(result);
        }
        httpPost.releaseConnection();
        return null;
    }

    /**
     * 获取用户信息
     * get
     */
    public static JSONObject getUserInfo(String url) throws  IOException {
        JSONObject jsonObject = null;
        CloseableHttpClient client = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        HttpResponse response = client.execute(httpGet);

        HttpEntity entity = response.getEntity();

        if (entity != null) {
            String result = EntityUtils.toString(entity, "UTF-8");
            jsonObject = JSONObject.parseObject(result);
        }

        httpGet.releaseConnection();
        System.out.println("用户信息："+jsonObject);
        return jsonObject;
    }

}
