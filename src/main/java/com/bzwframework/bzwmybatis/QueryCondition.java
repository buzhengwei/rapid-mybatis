package com.bzwframework.bzwmybatis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: QueryCondition
 * @Package com.sdepc.fim.web.pojo
 * @Description: 查询条件抽象类，所有查询条件类均继承此类
 * @author zhengwei.bu
 * @date 2014年2月12日 下午2:10:44
 */
@ApiModel(description= "查询条件抽象类")
public abstract class QueryCondition {
	
	/**
	 * 排序参数列表
	 */
	@ApiModelProperty(value = "排序参数列表")
	protected List<SortParam> sortParamList;
		
	/**
	 * @Title: isSort
	 * @Description: 判断是否需要对结果进行排序操作
	 * @return 是否需要对结果进行排序操作
	 */
	@ApiModelProperty(value = "是否排序",readOnly = true)
	public boolean isSort(){
		if(this.sortParamList!=null && this.sortParamList.size()>0){			
			return true;
		}	
		return false;
	}
	
	/**   
	 * @Title: addSortParam   
	 * @Description: 添加排序参数至本条件，重复添加可用于多重排序  
	 * @param propertyName Domain对象的属性名
	 * @param orderOptions 排序选项：ASC 或 DESC
	 */
	public void addSortParam(String propertyName,String orderOptions){
		if(sortParamList==null) sortParamList=new ArrayList<SortParam>();
		sortParamList.add(new SortParam(propertyName,orderOptions));
	}
	
	/* (non-Javadoc)   
	 * @return   
	 * @see java.lang.Object#toString()   
	 */  
	@Override
	public String toString() {
		return "QueryCondition [sortParamList=" + sortParamList + "]";
	}

	/**   
	 * @return sortParamList   
	 */
	public List<SortParam> getSortParamList() {
		return sortParamList;
	}

	/**     
	 * @param sortParamList the sortParamList to set     
	 */
	public void setSortParamList(List<SortParam> sortParamList) {
		this.sortParamList = sortParamList;
	}

}

