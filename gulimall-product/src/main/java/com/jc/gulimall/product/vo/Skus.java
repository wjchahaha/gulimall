/**
  * Copyright 2021 json.cn 
  */
package com.jc.gulimall.product.vo;
import java.util.List;

/**
 * Auto-generated: 2021-09-08 3:8:28
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class Skus {

    private List<Attr> attr;
    private String skuName;
    private String price;
    private String skuTitle;
    private String skuSubtitle;
    private List<Images> images;
    private List<String> descar;
    private int fullCount;
    private int discount;
    private int countStatus;
    private int fullPrice;
    private int reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
    public void setAttr(List<Attr> attr) {
         this.attr = attr;
     }
     public List<Attr> getAttr() {
         return attr;
     }

    public void setSkuName(String skuName) {
         this.skuName = skuName;
     }
     public String getSkuName() {
         return skuName;
     }

    public void setPrice(String price) {
         this.price = price;
     }
     public String getPrice() {
         return price;
     }

    public void setSkuTitle(String skuTitle) {
         this.skuTitle = skuTitle;
     }
     public String getSkuTitle() {
         return skuTitle;
     }

    public void setSkuSubtitle(String skuSubtitle) {
         this.skuSubtitle = skuSubtitle;
     }
     public String getSkuSubtitle() {
         return skuSubtitle;
     }

    public void setImages(List<Images> images) {
         this.images = images;
     }
     public List<Images> getImages() {
         return images;
     }

    public void setDescar(List<String> descar) {
         this.descar = descar;
     }
     public List<String> getDescar() {
         return descar;
     }

    public void setFullCount(int fullCount) {
         this.fullCount = fullCount;
     }
     public int getFullCount() {
         return fullCount;
     }

    public void setDiscount(int discount) {
         this.discount = discount;
     }
     public int getDiscount() {
         return discount;
     }

    public void setCountStatus(int countStatus) {
         this.countStatus = countStatus;
     }
     public int getCountStatus() {
         return countStatus;
     }

    public void setFullPrice(int fullPrice) {
         this.fullPrice = fullPrice;
     }
     public int getFullPrice() {
         return fullPrice;
     }

    public void setReducePrice(int reducePrice) {
         this.reducePrice = reducePrice;
     }
     public int getReducePrice() {
         return reducePrice;
     }

    public void setPriceStatus(int priceStatus) {
         this.priceStatus = priceStatus;
     }
     public int getPriceStatus() {
         return priceStatus;
     }

    public void setMemberPrice(List<MemberPrice> memberPrice) {
         this.memberPrice = memberPrice;
     }
     public List<MemberPrice> getMemberPrice() {
         return memberPrice;
     }

}