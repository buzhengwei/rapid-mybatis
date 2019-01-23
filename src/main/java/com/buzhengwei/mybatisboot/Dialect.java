package com.buzhengwei.mybatisboot;

import com.buzhengwei.mybatisboot.dialect.MySQLDialect;
import com.buzhengwei.mybatisboot.dialect.OracleDialect;

/**
 * @ClassName: Dialect
 * @Package com.sdepc.fim.common.jdbc.dialect
 * @Description: 数据库特例抽像类
 * @author zhengwei.bu
 * @date 2014�?2�?12�? 下午2:13:15
 */
public abstract class Dialect{

	/**
	 * @Fields DIALECT_ORACLE : oracle
	 */
	public static final String DIALECT_ORACLE="oracle";
	/**
	 * @Fields DIALECT_MYSQL : mysql
	 */
	public static final String DIALECT_MYSQL="mysql";
	
	/**
	 * @Title: getDialect
	 * @Description: 获取指定数据库的特例�?
	 * @param dialectName
	 * @return 指定数据库的特例�?
	 * @throws Exception TODO(请描述参数和返回值的含义)
	 */
	public static Dialect getDialect(String dialectName) throws Exception{
		if ( dialectName != null && !dialectName.equals("")) {
			if(dialectName.equalsIgnoreCase(Dialect.DIALECT_ORACLE)){
				return new OracleDialect();
			}else if(dialectName.equalsIgnoreCase(Dialect.DIALECT_MYSQL)){
				return new MySQLDialect();
			}else{
				throw new Exception("Dialect class not found: " + dialectName );				
			}
		}else{
			throw new Exception("dialect parameter is null!");
		}
	}
	
	/**
	 * @Title: getLimitString
	 * @Description: 获取从指定开始行到结束行的SQL语句，基于原SQL生成
	 * @param sql 原SQL语句
	 * @param beginRowNumber �?始行�?(包含�?始行)，首行为�?1�?
	 * @param endRowNumber 结束行号(包含结束�?)
	 * @return 带限定开始与结束行号的SQL语句
	 */
	public abstract String getLimitString(String sql, long beginRowNumber, long endRowNumber);
	
	/**
	 * @Title: getCountString
	 * @Description: 获取统计记录条数的SQL语句
	 * @param sql 原SQL语句
	 * @return 统计记录条数的SQL语句
	 */
	public abstract String getCountString(String sql);
	
	/**
	 * @Title: getJdbcTypeName
	 * @Description: 获取java类型在MyBatis中所对应的JdbcType类型名称
	 * @param javaType java类型
	 * @return JdbcType类型名称
	 */
	protected abstract String getJdbcTypeName(Class<?> javaType);
	
	/**
	 * @Title: getJdbcTypeExpression
	 * @Description: 获取java类型在MyBatis中所对应的JdbcType类型的表达式，例�?",jdbcType=VARCHAR"
	 * @param javaType java类型
	 * @return JdbcType类型的表达式
	 */
	public String getJdbcTypeExpression(Class<?> javaType){
		String result=getJdbcTypeName(javaType);
		return result!=null&&!result.equals("")?",jdbcType="+result:"";
	}
	
}
