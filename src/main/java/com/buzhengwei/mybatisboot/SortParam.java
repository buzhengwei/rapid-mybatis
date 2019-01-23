package com.buzhengwei.mybatisboot;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @ClassName: SortParam   
 * @Description: 排序的相关参数  
 * @author: zhengwei.bu  
 * @date:2017年2月16日 下午5:37:46     
 */
@ApiModel(description= "排序的相关参数")
public class SortParam {
	
	/**
	 * Domain对象的属性名
	 */
	@ApiModelProperty(value = "Domain对象的属性名")
	private String propertyName;
	
	/**
	 * 排序选项：ASC 或 DESC
	 */
	@ApiModelProperty(value = "排序选项：ASC 或 DESC")
	private String sortOption;
	
	/**  
	 * 构造方法   
	 */
	public SortParam(){
		super();
	}
			
	/**  
	 * @param propertyName Domain对象的属性名
	 * @param sortOption 排序选项：ASC 或 DESC  
	 */
	public SortParam(String propertyName, String sortOption) {
		super();
		this.propertyName = propertyName;
		this.sortOption = sortOption;
	}

	/* (non-Javadoc)   
	 * @return   
	 * @see java.lang.Object#toString()   
	 */  
	@Override
	public String toString() {
		return "SortParam [propertyName=" + propertyName + ", sortOption=" + sortOption + "]";
	}

	/**   
	 * @return propertyName   
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**     
	 * @param propertyName the propertyName to set     
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**   
	 * @return sortOption   
	 */
	public String getSortOption() {
		return sortOption;
	}

	/**     
	 * @param sortOption the sortOption to set     
	 */
	public void setSortOption(String sortOption) {
		this.sortOption = sortOption;
	}

}
