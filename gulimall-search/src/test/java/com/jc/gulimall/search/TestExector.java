package com.jc.gulimall.search;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-10-21 17:37
**/
public class TestExector {

    @Autowired
    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //1.==========创建异步队形============
//        CompletableFuture<Void> 执行异步任务没有返回值的 = CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程："+Thread.currentThread().getId());
//            int i = 10 /2;
//            System.out.println("运行结果"+i);
//        },executor);
//
//        CompletableFuture<Integer> res = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("运行结果" + i);
//            return i;
////            whenComplete是方法完成后的感知
//        }, executor).whenComplete((result,u)->{
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            System.out.println(result);
////            String a = null;
////            System.out.println(a.charAt(1));
//            System.out.println("whenComplete可以继续执行任务用用一个线程");
//        }).exceptionally(throwable -> {
//            //捕获的异常是supplyAsync中的异常
//            //whenComplete中的异常不管
//            System.out.println("出异常了...."+throwable);
//            return 10;
//        });
        //2.========方法回调方法=========
//        CompletableFuture<Integer> res = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
////            System.out.println("运行结果" + i);
//            return i;
////            handle是方法完成后的处理
//        }, executor).handle((result,exe)->{
//            if (result != null){
//                System.out.println("方法没出异常");
//                return result * 2;
//            }
//            if (exe != null){
//                System.out.println("方法出异常后的处理");
//                return 0;
//            }
//            return 0;
//        });
        //3.===========线程串行化测试===============
//        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1启动....");
//            return "我是任务一";
//        }, executor).thenRunAsync(()->{
//            System.out.println("任务2启动");
//        },executor);

//        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1启动....");
//
//            System.out.println("任务1结束....");
//            return "我是任务一的返回结果";
//        }, executor).thenAcceptAsync((tastk1Res) -> {
//            System.out.println("任务2启动....");
//            System.out.println("任务1的返回值是"+tastk1Res);
//            System.out.println("任务2结束.....");
//        }, executor);

//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1启动....");
//            System.out.println("任务1结束....");
//            return "我是任务一的返回结果";
//        }, executor).thenApplyAsync((task1Res)->{
//            System.out.println("任务2启动....");
//            System.out.println("任务1的返回值是"+task1Res);
//            System.out.println("任务2结束....");
//            return "我是任务二的返回结果";
//        },executor);

//         //组合两个任务 等两个任务都执行完了 在处理当前任务
//        //任务1
//        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1启动....");
//            System.out.println("任务1结束....");
//            return "我是任务1的返回结果";
//        }, executor);
//        //任务2
//        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务2启动....");
//            System.out.println("任务2结束....");
//            return "我是任务2的返回结果";
//        }, executor);
//        //4.========组合任务测试===========
////        CompletableFuture<Void> future4 = future1.runAfterBothAsync(future2, () -> {
////            System.out.println("任务3启动....");
////            System.out.println("任务3结束....");
////        }, executor);
//
//        CompletableFuture<Void> future3 = future1.thenAcceptBothAsync(future2, (task1Res, tast2Res) -> {
//            System.out.println("任务3启动....");
//            System.out.println("任务1的返回结果" + task1Res);
//            System.out.println("任务2的返回结果" + tast2Res);
//            System.out.println("任务3结束....");
//        }, executor);
//
//        CompletableFuture<String> future = future1.thenCombineAsync(future2, (task1Res, tast2Res) -> {
//            System.out.println("任务3启动....");
//            System.out.println("任务1的返回结果" + task1Res);
//            System.out.println("任务2的返回结果" + tast2Res);
//            System.out.println("任务3结束....");
//            return "我是任务3的返回结果";
//        }, executor);
//
////      System.out.println(future4.get());
///       System.out.println(future3.get());
//        System.out.println(future.get());


        //组合两个任务 有一个任务处理完了 就处理当前任务
        //任务1
//        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1启动....");
//            try {
//                System.out.println("我正在睡觉.........");
//                Thread.sleep(20000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("任务1结束....");
//            return "我是任务1的返回结果";
//        }, executor);
//        //任务2
//        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务2启动....");
//            System.out.println("任务2结束....");
//            return "我是任务2的返回结果";
//        }, executor);
//
//        //4.========组合任务测试===========
////        CompletableFuture<Void> future = future1.runAfterEitherAsync(future2, () -> {
////            System.out.println("任务3启动....");
////            System.out.println("任务3结束....");
////        }, executor);
//
////        CompletableFuture<Void> future = future1.acceptEitherAsync(future2, (tast2Res) -> {
////            System.out.println("任务3启动....");
////            System.out.println("任务2的返回结果" + tast2Res);
////            System.out.println("任务3结束....");
////        }, executor);
//
//        CompletableFuture<String> future = future1.applyToEitherAsync(future2, ( tast2Res) -> {
//            System.out.println("任务3启动....");
//            System.out.println("任务2的返回结果" + tast2Res);
//            System.out.println("任务3结束....");
//            return "我是任务3的返回结果";
//        }, executor);
//
////        System.out.println(future4.get());
////        System.out.println(future3.get());
//        System.out.println(future.get());

    //=================多任务组合===================
        System.out.println("main......start........");
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1启动....");
            try {
                System.out.println("我正在睡觉.........");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务1结束....");
            return "我是任务1的返回结果";
        }, executor);
        //任务2
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2启动....");
            try {
                System.out.println("我正在睡觉.........");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务2结束....");
            return "我是任务2的返回结果";
        }, executor);
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务3启动....");
            try {
                System.out.println("我正在睡觉.........");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务3结束....");
            return "我是任务3的返回结果";
        }, executor);

        //等待所有任务完成 阻塞时等待
//        CompletableFuture<Void> future = CompletableFuture.allOf(future1, future2, future3);

        //有一个完成就ok
        CompletableFuture<Object> future4 = CompletableFuture.anyOf(future1, future2, future3);

        System.out.println(future4.get());
        System.out.println(future1.get());
        System.out.println(future2.get());
        System.out.println(future3.get());

        System.out.println("main......end........");

    }
}
