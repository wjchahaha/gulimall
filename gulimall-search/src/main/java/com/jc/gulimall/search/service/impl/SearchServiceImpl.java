package com.jc.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.jc.common.to.SkuEsModel;
import com.jc.gulimall.search.config.ElasticSearch;
import com.jc.gulimall.search.constant.ElasticSearchConstant;
import com.jc.gulimall.search.service.SearchService;
import com.jc.gulimall.search.vo.SearchParam;
import com.jc.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sun.util.locale.ParseStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * ???????????????es??????,
     * @param vo
     * @return
     */
    @Override
    public SearchResult search(SearchParam vo)  {
        //1.??????searchRequest???????????????
        SearchRequest searchRequest = buildSearchRequest(vo);


        //1.1??????????????????
        //1.1.1 ????????????????????????query??????
        SearchResult result = null;
        try {
            //2.??????????????????
            SearchResponse response = restHighLevelClient.search(searchRequest, ElasticSearch.COMMON_OPTIONS);
            result = buildSearchResult(response,vo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //???????????????resp????????????
        return result;
    }

    /**
     * #1.must match ????????????
     * #2.filte ?????? ??????,??????,??????,??????,??????...??????,??????..
     * #3.????????????..
     * ??????????????????
     * @param vo
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam vo) {
        //1??????dsl??????
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //1.1?????? ??????
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1.1must????????????
        if(!StringUtils.isEmpty(vo.getKeyword()))
        boolQuery.must(QueryBuilders.matchQuery("skuTitle",vo.getKeyword()));

        //1.1.2.1filter??????-??????????????????
        if(vo.getCatalog3Id() != null)
        boolQuery.filter(QueryBuilders.termQuery("catalogId",vo.getCatalog3Id()));

        //1.1.2.2filter??????-??????[??????id]
        if(vo.getBrandId() != null && vo.getBrandId().size() > 0)
        boolQuery.filter(QueryBuilders.termsQuery("brandId",vo.getBrandId()));
        //1.1.2.3filter??????-????????????
        if (vo.getAttrs() != null && vo.getAttrs().size() > 0){
            List<String> attrs = vo.getAttrs();
            for (String attr : attrs) {
                //attrs attrs=1_5???:8???&attrs=2_18G:8G

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
        //1.1.2.3filter??????-??????????????????
        if (vo.getHashStock() != null)
        boolQuery.filter(QueryBuilders.termQuery("hasStock",vo.getHashStock() == 1));

        //1.1.2.3filter??????-????????????????????????
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
        //????????? ??????boolQuery???builder????????????
        builder.query(boolQuery);

        //1.2?????? ?????? ??????
        /**
         * sort=saleCount_asc/desc
         * sort=skuPrice_asc/desc
         * sort=hotScore_asc/desc
         */

        //1.2.1??????
        if(!StringUtils.isEmpty(vo.getSort())){
            String sort = vo.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc")? SortOrder.ASC: SortOrder.DESC;

            builder.sort(s[0],order);
        }
        //1.2.2??????
        builder.from((vo.getPageNum() - 1) * ElasticSearchConstant.PRODUCT_PAGESIZE);
        builder.size(ElasticSearchConstant.PRODUCT_PAGESIZE);
        //1.2.3??????
        if(!StringUtils.isEmpty(vo.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            builder.highlighter(highlightBuilder);
        }


        //1.3????????????
        //1.3.1 ????????????
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        builder.aggregation(brand_agg);
        //1.3.2 ????????????
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        builder.aggregation(catalog_agg);
        //1.3.3 ????????????
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        builder.aggregation(attr_agg);
        SearchRequest searchRequest = new SearchRequest(ElasticSearchConstant.PRODUCT_INDEX);
        //????????????

        //?????????????????????
        String dsl = builder.toString();
        System.out.println("?????????dsl====>"+dsl);
        searchRequest.source(builder);
        return searchRequest;
    }
    /**
     * ??????????????????
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response,SearchParam searchParam) {
        SearchResult searchResult = new SearchResult();
        //1.???????????????????????????
        List<SkuEsModel> skuEsModelList = new ArrayList<>();
        SearchHits hits = response.getHits();
        //????????????????????????hit??????hits???
        SearchHit[] products = hits.getHits();

        if(products != null && products.length > 0) {
            for (SearchHit product : products) {
                String source = product.getSourceAsString();
                //????????????
                SkuEsModel skuEsModel = JSON.parseObject(source, SkuEsModel.class);
//                if (!StringUtils.isEmpty(searchParam.getKeyword())){
//                    String skuTitle = product.getHighlightFields().get("skuTitle").getFragments()[0].string();
//                    skuEsModel.setSkuTitle(skuTitle);
//                }
                skuEsModelList.add(skuEsModel);
            }
            searchResult.setProducts(skuEsModelList);
        }
        Aggregations aggregations = response.getAggregations();
        //==========????????????????????????======
        //2.??????????????????????????????????????????
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = aggregations.get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");

        for(Terms.Bucket attr:attr_id_agg.getBuckets()){
            SearchResult.AttrVo vo = new SearchResult.AttrVo();
            //??????id
            long attrId = attr.getKeyAsNumber().longValue();
            //????????????
            ParsedStringTerms attr_name_agg = attr.getAggregations().get("attr_name_agg");
            String attrName = attr_name_agg.getBuckets().get(0).getKeyAsString();
            //?????????

            List<String> attrValue = new ArrayList<>();
            ParsedStringTerms attr_value_agg = attr.getAggregations().get("attr_value_agg");
            //????????????
//            List<? extends Terms.Bucket> attr_value_aggBuckets = attr_value_agg.getBuckets();
//            List<String> collect = attr_value_agg.getBuckets().stream().map((bucket) -> {
//                String keyAsString = bucket.getKeyAsString();
//                return keyAsString;
//            }).collect(Collectors.toList());
            //???????????? ??????????????????..
            for(Terms.Bucket attr_value : attr_value_agg.getBuckets()){
                String keyAsString = attr_value.getKeyAsString();
                attrValue.add(keyAsString);
            }
            vo.setAttrId(attrId);
            vo.setAttrName(attrName);
            vo.setAttrValue(attrValue);
            attrVos.add(vo);
        }
        searchResult.setAttrs(attrVos);
//        searchResult.setAttrs();
        //3.??????????????????????????????????????????
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = aggregations.get("brand_agg");
        List<? extends Terms.Bucket> brand_aggBuckets = brand_agg.getBuckets();
        for(Terms.Bucket brand : brand_aggBuckets){
            SearchResult.BrandVo vo = new SearchResult.BrandVo();
            //??????id
            long brandId = brand.getKeyAsNumber().longValue();
            //??????????????????????????????????????????
            //????????????
            ParsedStringTerms brand_name_agg = brand.getAggregations().get("brand_name_agg");
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();
            //????????????
            ParsedStringTerms brand_img_agg = brand.getAggregations().get("brand_img_agg");
            String brandImg = brand_img_agg.getBuckets().get(0).getKeyAsString();
            //??????
            vo.setBrandId(brandId);
            vo.setBrandImg(brandImg);
            vo.setBrandName(brandName);
            brandVos.add(vo);
        }
        searchResult.setBrands(brandVos);
        //4.??????????????????????????????????????????
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = aggregations.get("catalog_agg");
        List<? extends Terms.Bucket> catalog_buckets = catalog_agg.getBuckets();
        for(Terms.Bucket bucket: catalog_buckets){
            SearchResult.CatalogVo vo = new SearchResult.CatalogVo();
            String key = bucket.getKeyAsString();
            //???????????????????????????
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            vo.setCatalogName(catalog_name_agg.getBuckets().get(0).getKeyAsString());
            vo.setCatalogId(Long.valueOf(key));
            catalogVos.add(vo);
        }
        searchResult.setCatalogs(catalogVos);
        //5.???????????? ????????????,?????????
        long total = hits.getTotalHits().value;
        int totalPages = (int) (total % ElasticSearchConstant.PRODUCT_PAGESIZE == 0? total / ElasticSearchConstant.PRODUCT_PAGESIZE : total / ElasticSearchConstant.PRODUCT_PAGESIZE + 1);

        searchResult.setTotal(total);
        searchResult.setTotalPages(totalPages);
        searchParam.setPageNum(searchParam.getPageNum());
        return searchResult;
    }



}
