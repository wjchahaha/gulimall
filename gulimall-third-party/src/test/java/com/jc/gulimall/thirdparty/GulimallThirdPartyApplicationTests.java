package com.jc.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

@SpringBootTest
class GulimallThirdPartyApplicationTests {


    @Autowired
    OSSClient ossClient;
    @Test
    void contextLoads() {
    }


    @Test
    public void saveFile() throws IOException {

//        InputStream inputStream = new FileInputStream(new File("F:\\桌面\\Snipaste_2021-08-22_10-36-26.png"));
        ossClient.putObject("gulimall-wwjjcc", "26号",new File("F:\\桌面\\Snipaste_2021-08-22_10-36-26.png"));


        System.out.println("wanshi!");
//        System.out.println("上传完成");

    }

}
