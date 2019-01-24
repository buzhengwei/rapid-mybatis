package com.buzhengwei.rapidmybatis.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @ClassName: Table
 * @Package com.sdepc.fim.common.jdbc.annotation
 * @Description: 指定该类为持久化的数据库表，即此类在数据库中存在对应表�??
 * @注意: 当该类已标记为Table后，默认该类的所有成员属性全为持久化列，即默认所有属性均注解上了"@Column"�?
 * @author zhengwei.bu
 * @date 2014�?3�?3�? 下午1:56:57
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Table {
	
	/**
	 * @Title: name
	 * @Description: 数据库中相对应的表名
	 * @return 表名
	 */
	String name() default "";
	
}
