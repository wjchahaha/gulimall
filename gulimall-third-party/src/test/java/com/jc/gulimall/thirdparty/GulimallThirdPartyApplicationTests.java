package com.jc.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.jc.gulimall.thirdparty.component.SmsComponent;
import com.jc.gulimall.thirdparty.util.HttpUtils;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class GulimallThirdPartyApplicationTests {


    @Autowired
    OSSClient ossClient;
    @Test
    void contextLoads() {
    }

    @Autowired
    private SmsComponent smsComponent;

    @Test
    public void saveFile() throws IOException {

//        InputStream inputStream = new FileInputStream(new File("F:\\桌面\\Snipaste_2021-08-22_10-36-26.png"));
        ossClient.putObject("gulimall-wwjjcc", "26号",new File("F:\\桌面\\Snipaste_2021-08-22_10-36-26.png"));


        System.out.println("wanshi!");
//        System.out.println("上传完成");

    }
    @Test
    void testAuth(){
        smsComponent.sms("18974927407","【创信】你的验证码是：5873，3分钟内有效！");
    }

}
