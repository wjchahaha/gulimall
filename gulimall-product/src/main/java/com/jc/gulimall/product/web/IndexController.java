package com.jc.gulimall.product.web;

import com.jc.gulimall.product.entity.CategoryEntity;
import com.jc.gulimall.product.service.CategoryService;
import com.jc.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @program: gulimall
 * @description:
 * @author: Mr.Wang
 * @create: 2021-09-25 17:17
 **/
@Controller
public class IndexController {



    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/111")
    @ResponseBody
    public String fun1(){
        System.out.println(1/0);
        System.out.println("123");
        return "成功返回123";
    }

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {

        List<CategoryEntity> categorys = categoryService.getOneLevelCategory();

        model.addAttribute("categorys", categorys);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson() {

        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJson();

        return map;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello(Model model) throws InterruptedException {
        //拿锁
        RLock lock = redisson.getLock("my-lock");

        //加锁
        //阻塞式等待 默认加锁时间为30s
        //1）锁的自动续费，如果业务时间超长，运行期间启动给锁上新的30s(TTL剩余20）。不需要担心业务时间长，锁自动过期被删掉
        //2）加锁的业务只要运行完成，或者中间出现错误和异常，就不会给当前锁进行续费，就算不手动解锁  也会30s后自动删除。
        //  加锁
        // 执行业务代码   业务代码运行期间,会不断给锁上心的30s
        // 手动解锁

        //lock.lock(leaseTime,TimeUnit.SECONDS);  锁时间到了以后不会自动续期
        //如果我们传递了锁的超时时间,就会给redis执行脚本,进行占锁,默认超时就是我们指定的时间。
        //如果没有指定超时时间，就会使用30 * 1000[看门狗的默认时间]
        //  只要占锁成功,就会启动一个定时任务,每隔看门狗的默认时间的/3的时间就会对锁进行自动续期。


        //最佳的还是使用lock.lock(30）省掉了整个续期操作,手动解锁。
        lock.lock();
        //执行业务代码
        try {
            System.out.println("执行业务代码,线程号：" + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception e) {

        } finally {
            //手动解锁
            lock.unlock();
            System.out.println("释放锁,线程号:" + Thread.currentThread().getId());
        }


        return "hello";
    }

    //加读写锁的目的就是一定能读到最新数据,修改期间，写锁时一个互斥锁。读锁时一个共享锁。
    //写锁没释放 读就会一直等待
    //读 + 读： 相当于无锁  会在redis中记录好所有的读锁 他们会同时加锁成功
    //写 + 读：读必须等待写锁释放
    //写 + 写：阻塞方式
    //读 + 写：有读锁 写也需要等待读锁释放
    //只要有写的存在，都必须等待
    @ResponseBody
    @GetMapping("/write")
    public String writeValue() {
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.writeLock();//拿写锁
        String s = null;
        rLock.lock();//上锁成功了
        try {
            System.out.println("写锁加锁成功.....");
            //1.改数据加写锁  读数据加读锁
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue",s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("写锁释放成功......");
        }
        return s;
    }

    @ResponseBody
    @GetMapping("/read")
    public String readValue() {
        String s = "";
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        //为了拿最新数据
        RLock rLock = lock.readLock();//拿锁

        //加锁成功了   执行业务代码
        rLock.lock();
        try {
            System.out.println("读锁加锁成gong....");
            s = redisTemplate.opsForValue().get("writeValue");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            rLock.unlock();
            System.out.println("读锁释放成.....");
        }
        return s;
    }


    @ResponseBody
    @GetMapping("/read1")
    public String readValue1() {
        String s = "";
//        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        //为了拿最新数据
//        RLock rLock = lock.readLock();//拿锁
        //加锁成功了   执行业务代码
//        rLock.lock();
        try {
            s = redisTemplate.opsForValue().get("writeValue");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
//            rLock.unlock();
        }
        return s;
    }


}


