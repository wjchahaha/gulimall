package com.jc.gulimall.cart.interceptor;

import com.jc.common.constant.AuthServerConstant;
import com.jc.common.constant.CartConstant;
import com.jc.common.vo.MemberEntity;
import com.jc.gulimall.cart.vo.UserInfoTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-22 16:30
**/
public class CartInteceptor implements HandlerInterceptor {
    /**
     * 1.如果是第一次访问购物车 给浏览器发一个cookie key：user-key  value:uuid  75行代码
     *
     * 2.如果不是第一次访问购物车   user.setUserKey(cookie.getValue());  cookie是我们之前的uuid 前提是没过期
     */
    public static final ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();
    /**
     *
     * ===========判断是否是临时用户===========
     * 判断用户是否登录
     * 根据一个user-key的cookie
     * 如果有的话则登录了
     * 否则没登录
     * 根据 value
     *
     * 来看是否是临时用户
     * 登录的话 为user设置一个
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

            UserInfoTo user = new UserInfoTo();
            HttpSession session = request.getSession();
            MemberEntity member = (MemberEntity) session.getAttribute(AuthServerConstant.LOGIN_USER);
            if(member != null){
                //如果登录了的话
                user.setUserId(member.getId());
            }
            //无论如何,都为cookie设置一个value
            Cookie[] cookies = request.getCookies();
            if(cookies != null && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    //如果不是第一次访问 这词带着key为user-key的cookie就为他设置一个值
                    if (cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                        //给用户的key设置上user-key的value 为他设置一个value
                        user.setUserKey(cookie.getValue());
                        user.setTempUser(true);
                    }
                }
            }


        /**
         * 这种情况是：第一次访问
         */
            if(StringUtils.isEmpty(user.getUserKey())){
                //如果是临时的用户 为他设置一个uuid
                String uuid = UUID.randomUUID().toString();
                user.setUserKey(uuid);
            }

            //到这两种结果
            // 1.如果不是临时的用户 userId为用户id,
            // 2.是临时用户         userID
            threadLocal.set(user);
            return true;
        }

    /**
     * 用户处理完之后  让浏览器保存一个cookie
     * 只有第一次访问就发给浏览器保存cookie一个月
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();

        if(!userInfoTo.isTempUser()){  //不判断就是持续延长cookie时间
            //如果不是临时用户的话  就一直增大user-key的临时时间
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME,userInfoTo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.COOKIE_MAX_AGE);
            response.addCookie(cookie);
        }

    }
}
