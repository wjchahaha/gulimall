package com.jc.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.jc.common.to.SkuEsModel;
import com.jc.gulimall.search.config.ElasticSearch;
import com.jc.gulimall.search.constant.ElasticSearchConstant;
import com.jc.gulimall.search.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-09-23 22:20
**/
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productUp(List<SkuEsModel> skuEsModels) throws IOException {

        BulkRequest bulkRequest = new BulkRequest();

        for (SkuEsModel skuEsModel : skuEsModels) {
            //构造保存请求
            IndexRequest indexRequest = new IndexRequest(ElasticSearchConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String jsonString = JSON.toJSONString(skuEsModel);
            indexRequest.source(jsonString, XContentType.JSON);
            //添加批量请求中
            bulkRequest.add(indexRequest);
        }

        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ElasticSearch.COMMON_OPTIONS);

        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());

        if(b){
         log.error("商品上架错误:{}",collect);
        }

        return b;
    }
}
