package com.jc.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.jc.gulimall.search.config.ElasticSearch;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;


import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {
    /**
     * 把商品数据从数据库中导入到es中叫做上架
     * （1）.方便检索{
     *     skuid:1
     *     spuId:11
     *     skutitle:Apple 13
     *     price:998
     *     saleCount:99
     *     attrs:[
     *      {屏幕尺寸：5寸},
     *      {CPU:高通886},
     *      {分辨率：全高清}
     *     ]
     * }
     */
    @Autowired
    private RestHighLevelClient restHighLevelClient;


    @Autowired
    private ElasticSearch elasticSearch;

        /**
         * 测试复杂检索
         * @throws IOException
         */
        @ToString
        @Data
        static class Account{
            private int account_number;

            private int balance;

            private String firstname;

            private String lastname;

            private int age;

            private String gender;

            private String address;

            private String employer;

        private String email;

        private String city;

        private String state;
    }



    @Autowired
    public static ExecutorService executor = Executors.newFixedThreadPool(10);



    @Test
    public void searchData() throws IOException {
        //1.通过searchRequest来进行检索
        SearchRequest searchRequest = new SearchRequest("bank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //1.1构建检索条件
        //1.1.1 构建检索条件中的query条件
        searchSourceBuilder.query(QueryBuilders.matchQuery("address","Court"));

        //1.1.2构建检索条件中的聚合条件
        searchSourceBuilder.aggregation(AggregationBuilders.terms("ageAgg").field("age").size(10));
        searchSourceBuilder.aggregation(AggregationBuilders.avg("ageAvg").field("age"));
        searchSourceBuilder.aggregation(AggregationBuilders.avg("balanceAvg").field("balance"));
        //2.进行检索.
        //2.1添加检索条件到requst中
        searchRequest.source(searchSourceBuilder);

        //2.2进行检索
        System.out.println(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient
                .search(searchRequest,ElasticSearch.COMMON_OPTIONS);
        //3.分析检索的结果
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hits1 = hits.getHits();
        //3.1获取检索的信息
        for (SearchHit hit : hits1) {
            //先过去hit中的source字符串
            String sourceAsString = hit.getSourceAsString();

            //在将json字符串转成实体类
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println(account);
        }
        //3.2获取聚合的信息
        Aggregations aggregations = searchResponse.getAggregations();
        //获取年龄的聚合
        Terms ageAgg = aggregations.get("ageAgg");
        //
        for (Terms.Bucket bucket : ageAgg.getBuckets()) {
            System.out.println("年龄信息"+bucket.getKey());
        }
        //获取年龄平均值的聚合
        Avg ageAvg = aggregations.get("ageAvg");
        System.out.println("年龄平均值"+ageAvg.getValue());
        //获取余额平均值的聚合
        Avg balanceAvg = aggregations.get("balanceAvg");
        System.out.println("获取余额平均值"+balanceAvg.getValue());

    }

    /**
     * 测试存储数据到el
     * @throws IOException
     */
    @Test
    public void contextLoads() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");

        indexRequest.id("1");
        User user = new User();
        user.setUserName("wjc");
        user.setGender("男");
        user.setAge(22);
        String s = JSON.toJSONString(user);

        indexRequest.source(s, XContentType.JSON);
        //执行操作
        IndexResponse index = restHighLevelClient.index(indexRequest, ElasticSearch.COMMON_OPTIONS);
        System.out.println(index);
    }
    @Data
    class User{
        private String userName;
        private String gender;
        private Integer age;
    }
//    @Test
//    public void contextLoads() {
//        System.out.println(restHighLevelClient);
//    }

}
