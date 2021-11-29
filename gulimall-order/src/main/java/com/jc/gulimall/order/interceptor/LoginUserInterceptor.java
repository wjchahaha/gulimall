package com.jc.gulimall.order.interceptor;

import com.jc.common.constant.AuthServerConstant;
import com.jc.common.vo.MemberEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-28 17:12
**/
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberEntity> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断用户是否登录 true是拦截
        MemberEntity user = (MemberEntity) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (user != null){
            loginUser.set(user);
            //过去了

            return true;
        }else{
            //为空的话就拦截
            System.out.println("要提示了");
            request.getSession().setAttribute("msg","请先登录");
            //重定向到哪个页面
            response.sendRedirect("http://auth.gulimall.com/login.html");
            //过不去
            return false;
        }
    }
}
