package com.buzhengwei.mybatisboot;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @ClassName: PageQueryCondition
 * @Package com.sdepc.fim.web.pojo
 * @Description: 分页条件类，所有分页查询的条件均继承此类
 * @author zhengwei.bu
 * @date 2014年2月12日 下午2:06:51
 */
@ApiModel(description= "分页查询条件类")
public abstract class PageQueryCondition extends QueryCondition{

	/**
	 * 总记录数，默�?0�?
	 */
	@ApiModelProperty(value = "总记录数",readOnly = true)
	protected long totalRecord=0;
	
	/**
	 * 每页记录数，默认显示�?有，即pageSize=0
	 */
	@ApiModelProperty(value = "每页记录数，默认不分页")
	protected int pageSize=0;
	
	/**
	 * 当前页码，默认每1�?
	 */
	@ApiModelProperty(value = "当前页码，默认第1页")
	protected int page=1;

	/**
	 * @Title: getTotalPage
	 * @Description: 获取总页数
	 * @return 总页数
	 */
	@ApiModelProperty(value = "总页数",readOnly = true)
	public long getTotalPage() {
		return pageSize==0?1:totalRecord/pageSize+1;
	}

	/**
	 * @Title: getBeginRowNumber
	 * @Description: 获取当前页的开始行号，行号最小为1
	 * @return 当前页的开始行号
	 */
	@ApiModelProperty(value = "当前页的开始行号",readOnly = true)
	public long getBeginRowNumber(){
		return (page-1)*pageSize+1;
	}
	
	/**
	 * @Title: getEndRowNumber
	 * @Description: 获取当前页的结束行号
	 * @return 当前页的结束行号
	 */
	@ApiModelProperty(value = "当前页的结束行号",readOnly = true)
	public long getEndRowNumber(){
		return pageSize==0?totalRecord:page*pageSize;		
	}

	/* (non-Javadoc)   
	 * @return   
	 * @see java.lang.Object#toString()   
	 */  
	@Override
	public String toString() {
		return "PageQueryCondition [totalRecord=" + totalRecord + ", pageSize=" + pageSize + ", page=" + page + "]";
	}

	/**   
	 * @return totalRecord   
	 */
	public long getTotalRecord() {
		return totalRecord;
	}

	/**     
	 * @param totalRecord the totalRecord to set     
	 */
	public void setTotalRecord(long totalRecord) {
		this.totalRecord = totalRecord;
	}

	/**   
	 * @return pageSize   
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**     
	 * @param pageSize the pageSize to set     
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**   
	 * @return page   
	 */
	public int getPage() {
		return page;
	}

	/**     
	 * @param page the page to set     
	 */
	public void setPage(int page) {
		this.page = page;
	}

}
