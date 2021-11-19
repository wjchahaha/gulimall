package com.jc.gulimall.gulimall.auth.app;

import com.alibaba.fastjson.TypeReference;
import com.jc.common.utils.R;
import com.jc.gulimall.gulimall.auth.feign.MemberFeignService;
import com.jc.gulimall.gulimall.auth.feign.ThirdPartyService;
import com.jc.gulimall.gulimall.auth.vo.UserLoginVo;
import com.jc.gulimall.gulimall.auth.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-11-14 19:41
**/
@Controller
public class LoginController {

    @Autowired
    private ThirdPartyService thirdPartyService;

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/reg.html")
    public String toReg(){
        return "reg";
    }

    @GetMapping("/login.html")
    public String toLogin(){
        return "login";
    }

    @ResponseBody
    @PostMapping("/sms/code")
    public R sendCode(@RequestParam("phone") String phone){
        String code = UUID.randomUUID().toString();
        code = code.substring(0,4);
        thirdPartyService.sms(phone,code);
        return R.ok();
    }


    /**
     * 要做重定向携带数据 历用Session原理 将数据放在session中
     * RedirectAttributes model:重定向携带数据
     *只要跳到下一个页面，session数据就会被删掉
     * 就会产生分布式下session的问提
     * @param vo
     * @param result
     * @param model
     * @return
     */
    @PostMapping("/regist")
    public String  regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes model){
        if(result.hasErrors()){//提交数据有误
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(fieldError -> {
                return fieldError.getField();
            }, message -> {
                return message.getDefaultMessage();
            }));
            model.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        //如果数据没有格式问题，还是会失败因为已经手机号还有用户名已经存在
        //注册,调用远程服务进行注册
        R regist = memberFeignService.regist(vo);

        if (regist.getCode() == 0){//成功

            return "redirect:http://auth.gulimall.com/login.html";
        }else{//失败

            Map<String,String> errors = new HashMap<>();
            errors.put("msg",regist.getData("msg",new TypeReference<String>(){}));

            model.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

    }


    @PostMapping("/login")
    public String  login(@Valid UserLoginVo vo,RedirectAttributes redirectAttributes,Model model){
        R r = memberFeignService.login(vo);
        //远程登录
        R login = memberFeignService.login(vo);
        if (login.getCode() == 0){
            //TODO 商城主页显示信息
            redirectAttributes.addFlashAttribute("username",login.get("username"));
            return "redirect:http://gulimall.com";
        }else{
            Map<String,String> map = new HashMap<>();
            map.put("msg",login.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",map);
            return "redirect:http://auth.gulimall.com/login.html";
        }



    }



}
