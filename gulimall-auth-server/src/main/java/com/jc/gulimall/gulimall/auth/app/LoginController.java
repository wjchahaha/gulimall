package com.jc.gulimall.gulimall.auth.app;

import com.jc.common.utils.R;
import com.jc.gulimall.gulimall.auth.feign.ThirdPartyService;
import com.jc.gulimall.gulimall.auth.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
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
        if(result.hasErrors()){

            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(fieldError -> {
                return fieldError.getField();
            }, message -> {
                return message.getDefaultMessage();
            }));
            model.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        //注册,调用远程服务进行注册


        return "redirect:/login.html";
    }
}
