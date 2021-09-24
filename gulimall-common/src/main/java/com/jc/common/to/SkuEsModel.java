package com.jc.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>Title: SkuEsModel</p>
 * Description：
 * "mappings": {
 *     "properties": {
 *       "skuId":{
 *         "type": "long"
 *       },
 *       "spuId":{
 *         "type": "keyword"
 *       },
 *       "skuTitle":{
 *         "type": "text",
 *         "analyzer": "ik_smart"
 *       },
 *       "skuPrice":{
 *         "type": "keyword"
 *       },
 *       "skuImg":{
 *         "type": "keyword",
 *         "index": false,
 *         "doc_values": false
 *       },
 *       "saleCount":{
 *         "type": "long"
 *       },
 *       "hasStock":{
 *         "type": "boolean"
 *       },
 *       "hotScore":{
 *         "type": "long"
 *       },
 *       "brandId":{
 *         "type": "long"
 *       },
 *       "catalogId":{
 *         "type": "long"
 *       },
 *       "brandName":{
 *         "type":"keyword",
 *         "index": false,
 *         "doc_values": false
 *       },
 *       "brandImg":{
 *         "type": "keyword",
 *         "index": false,
 *         "doc_values": false
 *       },
 *       "catalogName":{
 *         "type": "keyword",
 *         "index": false,
 *         "doc_values": false
 *       },
 *       "attrs":{
 *         "type": "nested",
 *         "properties": {
 *           "attrId":{
 *             "type":"long"
 *           },
 *           "attrName":{
 *             "type":"keyword",
 *             "index":false,
 *             "doc_values": false
 *           },
 *           "attrValue":{
 *             "type":"keyword"
 *           }
 *         }
 *       }
 *     }
 *   }
 * date：2020/6/8 18:52
 */
@Data
public class SkuEsModel implements Serializable {

	private Long skuId;

	private Long spuId;

	private String skuTitle;
	//对应的是sku中的price
	private BigDecimal skuPrice;
	//对应的是skuDefaultImg
	private String skuImg;

	private Long saleCount;

	private Boolean hasStock;

	private Long hotScore;

	private Long brandId;

	private Long catalogId;

	private String brandName;
	//对应brand中的logo
	private String brandImg;
	//对应Catelgory中的name
	private String catalogName;

	private List<Attrs> attrs;

	/**
	 *  检索属性
	 */
	@Data
	public static class Attrs implements Serializable{
		private Long attrId;

		private String attrName;

		private String attrValue;
	}
}
