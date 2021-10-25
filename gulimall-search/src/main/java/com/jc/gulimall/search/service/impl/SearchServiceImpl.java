package com.jc.gulimall.search.service.impl;

import com.jc.gulimall.search.config.ElasticSearch;
import com.jc.gulimall.search.constant.ElasticSearchConstant;
import com.jc.gulimall.search.service.SearchService;
import com.jc.gulimall.search.vo.SearchParam;
import com.jc.gulimall.search.vo.SearchRespVo;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.List;

/**
* @program: gulimall
*
* @description: 
*
* @author: Mr.Wang
*
* @create: 2021-10-16 10:15
**/
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 带着参数去es中查,
     * @param vo
     * @return
     */
    @Override
    public SearchRespVo search(SearchParam vo)  {
        //1.通过searchRequest来进行检索
        SearchRequest searchRequest = buildSearchRequest(vo);

        SearchResult searchResult;
        //1.1构建检索条件
        //1.1.1 构建检索条件中的query条件

        try {
            //2.执行检索请求
            SearchResponse response = restHighLevelClient.search(searchRequest, ElasticSearch.COMMON_OPTIONS);
            searchResult = buildSearchResult(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //通过返回的resp进行聚合
        return null;
    }

    /**
     * #1.must match 模糊匹配
     * #2.filte 过滤 价格,属性,分类,品牌,库存...排序,分页..
     * #3.聚合分析..
     * 构建检索请求
     * @param vo
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam vo) {
        //1构建dsl语句
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //1.1查询 过滤
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1.1must模糊匹配
        if(!StringUtils.isEmpty(vo.getKeyword()))
        boolQuery.must(QueryBuilders.matchQuery("skuTitle",vo.getKeyword()));

        //1.1.2.1filter过滤-按照三级分类
        if(vo.getCatalog3Id() != null)
        boolQuery.filter(QueryBuilders.termQuery("catalogId",vo.getCatalog3Id()));

        //1.1.2.2filter过滤-按照[品牌id]
        if(vo.getBrandId() != null && vo.getBrandId().size() > 0)
        boolQuery.filter(QueryBuilders.termsQuery("brandId",vo.getBrandId()));
        //1.1.2.3filter过滤-按照属性
        if (vo.getAttrs() != null && vo.getAttrs().size() > 0){
            List<String> attrs = vo.getAttrs();
            for (String attr : attrs) {
                //attrs attrs=1_5寸:8存&attrs=2_18G:8G

                BoolQueryBuilder boolQuery1 = QueryBuilders.boolQuery();

                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");

                boolQuery1.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                boolQuery1.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));

                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", boolQuery1, ScoreMode.None);
                boolQuery.filter(nestedQuery);

            }


        }
        //1.1.2.3filter过滤-按照库存过滤
        if (vo.getHashStock() != null)
        boolQuery.filter(QueryBuilders.termQuery("hasStock",vo.getHashStock() == 1));

        //1.1.2.3filter过滤-按照价格区间去查
        /**
         * 1_1000 _1000 1000_
         */
        if (vo.getSkuPrice() != null){
            final String skuPrice = vo.getSkuPrice();
            String[] s = skuPrice.split("_");
            RangeQueryBuilder range = new RangeQueryBuilder("skuPrice");
            if(s.length == 2){
                range.gte(s[0]).lte(s[1]);
            }else if (s.length == 1){
                if(s[0] == "_"){
                    range.lte(s[1]);
                }else{
                    range.gte(s[0]);
                }
            }
            boolQuery.filter(range);
        }
        //将查询 过滤boolQuery和builder进行拼装
        builder.query(boolQuery);

        //1.2排序 分页 高亮
        /**
         * sort=saleCount_asc/desc
         * sort=skuPrice_asc/desc
         * sort=hotScore_asc/desc
         */

        //1.2.1排序
        if(!StringUtils.isEmpty(vo.getSort())){
            String sort = vo.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc")? SortOrder.ASC: SortOrder.DESC;

            builder.sort(s[0],order);
        }
        //1.2.2分页
        builder.from((vo.getPageNum() - 1) * ElasticSearchConstant.PRODUCT_PAGESIZE);
        builder.size(ElasticSearchConstant.PRODUCT_PAGESIZE);
        //1.2.3高亮
        if(!StringUtils.isEmpty(vo.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            builder.highlighter(highlightBuilder);
        }

        String dsl = builder.toString();
        System.out.println("构建的dsl====>"+dsl);
        //1.3聚合分析
        SearchRequest searchRequest = new SearchRequest(ElasticSearchConstant.PRODUCT_INDEX);

        //都拿来进行拼装
        searchRequest.source(builder);
        return searchRequest;
    }
    /**
     * 构建结果数据
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response) {

        return null;
    }



}
