package com.buzhengwei.rapidmybatis.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @ClassName: Column
 * @Package com.sdepc.fim.common.jdbc.annotation
 * @Description: 指定该属性为持久化的列（字段），即此属�?�在数据库表中存在对应的列（字段）�??
 * @author zhengwei.bu
 * @date 2014�?3�?3�? 下午2:16:38
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Column {
	
	/**
	 * @Title: name
	 * @Description: 数据库表中相应列（字段）的名称，当�?�为空字符串时表示列名与属�?�名相同（自动忽略下划线），默认空字符串�?
	 * @return 列名（字段名�?
	 */
	String name() default "";
	
	/**
	 * @Title: isPrimaryKey
	 * @Description: 本列是否为主键，默认为false-非主�?
	 * @return true-主键;false-非主�?
	 */	
	boolean isPrimaryKey() default false;

	/**
	 * @Title: generatedPrimaryKey32UUID
	 * @Description: 通过32位的UUID生成主键
	 * @return true-采用UUID生成主键；false-不采UUID生成主键
	 */
	boolean generatedPrimaryKey32UUID() default false;
	
	/**
	 * @Title: generatedPrimaryKeySQLExpression
	 * @Description: 在Insert中用于生成主键的SQL表达式（只是表达式，不含SELECT等语句），当值为空字符串时表示系统不自动生成主键，默认空字符串�??
	 * @return 生成主键的SQL表达式（只是表达式，不含SELECT等语句）
	 */
	String generatedPrimaryKeySQLExpression() default "";
	
}
