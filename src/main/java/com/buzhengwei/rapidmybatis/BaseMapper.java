package com.buzhengwei.rapidmybatis;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Options.FlushCachePolicy;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @ClassName: BaseMapper
 * @Package com.sdepc.fim.persistence
 * @Description: 基础Mapper接口，所有需要自动完成实体对象操作（增�?�删、改、单体查询）的Mapper均需从此接口继承
 * @author zhengwei.bu
 * @date 2014�?3�?6�? 下午7:29:20
 * @param <T> 实体�?
 */
public interface BaseMapper<T> {

	/**
	 * @Title: save
	 * @Description: 根据领域对象生成记录插入到相应数据库表中
	 * @param domainObject 领域对象
	 */
	@Insert("")
	void save(T domainObject);
	
	/**
	 * @Title: remove
	 * @Description: 根据指定的领域对象，删除数据库表中相应记录
	 * @param domainObject 领域对象，只需要给注解为@Column(isPrimaryKey=true,...)的属性赋值即可
	 * @return 删除记录条数
	 */
	@Delete("")
	int remove(T domainObject);
	
	/**
	 * @Title: update
	 * @Description: 根据指定的领域对象，更新数据库表中相应记录
	 * @param domainObject 领域对象（如果领域对象的某个属性为null，系统也会同样将数据库表中相应字段更新为null）
	 * @return 更新记录条数
	 */
	@Update("")
	int update(T domainObject);
	
	/**
	 * @Title: updateByNotNull
	 * @Description: 根据指定的领域对象，更新数据库表中相应记录
	 * @param domainObject 领域对象（如果领域对象的某个属性为null，系统不会更新数据库表中相应字段）
	 * @return 更新记录条数
	 */
	@Update("")
	int updateByNotNull(T domainObject);
	
	/**
	 * @Title: getDomainObjectById
	 * @Description: 根据领域对象的主键（实体类中注解为@Column(isPrimaryKey=true,...)的属性）查找单个领域对象
	 * @param domainObject 领域对象，只需要给注解为@Column(isPrimaryKey=true,...)的属性赋值即可
	 * @return 领域对象
	 */
	@Select("")
	@Options(flushCache=FlushCachePolicy.TRUE)
	T getDomainObjectById(T domainObject);
	
}
