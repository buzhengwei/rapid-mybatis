package com.buzhengwei.mybatisboot.dialect;

import com.buzhengwei.mybatisboot.Dialect;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @ClassName: MySQLDialect
 * @Package com.sdepc.fim.common.jdbc.dialect
 * @Description: MySQL的相关特例处理，例：分页
 * @author zhengwei.bu
 * @date 2014�?2�?12�? 下午2:12:13
 */
public class MySQLDialect extends Dialect {
	
	@Override
	public String getLimitString(String sql, long beginRowNumber, long endRowNumber) {
		return sql + (beginRowNumber>1 ? " limit "+(beginRowNumber-1)+", "+(endRowNumber-beginRowNumber+1) : " limit "+endRowNumber);
	}

	@Override
	public String getCountString(String sql) {
		sql = sql.trim();
		if ( sql.toLowerCase().endsWith( " for update" ) ) {
			sql = sql.substring( 0, sql.length()-11 );
		}
		return "select count(0) from (" + sql.trim()+ ") as temp_count";
	}

	@Override
	public String getJdbcTypeName(Class<?> javaType) {
		JavaJdbcTypeEnum javaJdbcTypeEnum=JavaJdbcTypeEnum.forCode(javaType);
		return javaJdbcTypeEnum!=null?javaJdbcTypeEnum.toString():null;
	}
	
	private enum JavaJdbcTypeEnum {

		VARCHAR(String.class),
		DECIMAL(BigDecimal.class),
		BOOLEAN(Boolean.class),
		TINYINT(Byte.class),
		SMALLINT(Short.class),
		INTEGER(Integer.class),	
		BIGINT(Long.class),
		REAL(Float.class),	
		DOUBLE(Double.class),
		BINARY(Byte[].class),
		TIMESTAMP(Date.class),
		TIME(Time.class),
//		TIMESTAMP(Timestamp.class),
		NULL(null);
		
		public final Class<?> TYPE_CODE;
		private static Map<Class<?>,JavaJdbcTypeEnum> codeLookup = new HashMap<Class<?>,JavaJdbcTypeEnum>();

		static {
			for (JavaJdbcTypeEnum type : JavaJdbcTypeEnum.values()) {
				codeLookup.put(type.TYPE_CODE, type);
			}
		}

		JavaJdbcTypeEnum(Class<?> code) {
			this.TYPE_CODE = code;
		}

		public static JavaJdbcTypeEnum forCode(Class<?> code)  {
			return codeLookup.get(code);
		}
		
	}

}
