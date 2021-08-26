package com.jc.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author wjc
 * @email 1678912421@gmail.com
 * @date 2021-07-16 21:22:32
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名不能为空！")
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotNull(message = "logo不能为空！")
	@URL(message = "logo地址必须是一个合法的URL！")
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotNull(message = "首字母不能为空！")
	@Pattern(regexp = "/^[a-zA-Z]$/",message = "检索首字符必须是一个字母")
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(message = "排序字段不能为空！")
	@Min(value = 0,message = "排序必须大于等于0")
	private Integer sort;

}
