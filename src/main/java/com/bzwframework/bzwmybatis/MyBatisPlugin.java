package com.bzwframework.bzwmybatis;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.PropertyException;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bzwframework.bzwmybatis.annotation.Column;
import com.bzwframework.bzwmybatis.annotation.Table;
import com.bzwframework.bzwmybatis.annotation.Transient;

/**
 * @ClassName: MyBatisPlugin
 * @Package com.sdepc.fim.common.jdbc
 * @Description: 自定义的MyBatis插件，用于系统自动生成增、删、改、单实体查询的SQL，还有自动分页、自动排序。
 * @author zhengwei.bu
 * @date 2014年2月12日 下午4:28:06
 */
@Intercepts({@Signature(type=StatementHandler.class,method="prepare",args={Connection.class,Integer.class})})
public class MyBatisPlugin implements Interceptor {
	private final static Logger log = LoggerFactory.getLogger(MyBatisPlugin.class);
	
	private final static String BASE_MAPPER_SAVE=".save";
	private final static String BASE_MAPPER_REMOVE=".remove";
	private final static String BASE_MAPPER_UPDATE=".update";
	private final static String BASE_MAPPER_UPDATE_BY_NOT_NULL=".updateByNotNull";
	private final static String BASE_MAPPER_GET_DOMAIN_OBJECT_BY_ID=".getDomainObjectById";
	
	/**
	 * 数据库标识，例：Oracle、MySQL
	 */
	private static String dialectName = "";	
	
	/**
	 * 数据库特例
	 */
	private static Dialect dialect;
	
	/**
	 * 实体类对应的各类SQL， 主要作用是提高性能，每种实体类对应的SQL只构建一次。
	 */
	private static Hashtable<String,String> entityClassMappingSqlMemoryTable=new Hashtable<String,String>();

	@Override
	public Object intercept(Invocation invocation) throws Exception {
		Object target=invocation.getTarget();
		if(target instanceof RoutingStatementHandler){
			if(dialect==null){
				dialect=Dialect.getDialect(MyBatisPlugin.dialectName);
			}
			Connection connection = (Connection) invocation.getArgs()[0];
			RoutingStatementHandler statementHandler = (RoutingStatementHandler)target;
			BaseStatementHandler delegate = (BaseStatementHandler) ReflectUtils.getValueByFieldName(statementHandler, "delegate");
			Configuration configuration=(Configuration)ReflectUtils.getValueByFieldName(delegate, "configuration");			
			BoundSql boundSql = delegate.getBoundSql();
			Object parameterObject = boundSql.getParameterObject();
			String sql = boundSql.getSql();
			MappedStatement mappedStatement=(MappedStatement)ReflectUtils.getValueByFieldName(delegate, "mappedStatement");
			String mappedStatementId=mappedStatement.getId();
			if(mappedStatementId.endsWith(BASE_MAPPER_SAVE) || mappedStatementId.endsWith(BASE_MAPPER_REMOVE)
					|| mappedStatementId.endsWith(BASE_MAPPER_UPDATE) || mappedStatementId.endsWith(BASE_MAPPER_UPDATE_BY_NOT_NULL)
					|| mappedStatementId.endsWith(BASE_MAPPER_GET_DOMAIN_OBJECT_BY_ID)){
				SqlCommandType sqlCommandType=mappedStatement.getSqlCommandType();
				Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
				if(sqlCommandType==SqlCommandType.INSERT || sqlCommandType==SqlCommandType.UPDATE){
					sql=builderSqlByEntityClass(parameterObject,sqlCommandType,connection,mappedStatementId);
				}else if(sqlCommandType==SqlCommandType.SELECT){
					List<ResultMap> resultMaps=mappedStatement.getResultMaps();
					if(resultMaps==null || resultMaps.size()!=1){
						throw new RuntimeException("系统自动为实体类生成SQL的Mapper方法["+mappedStatement.getId()+"]的resultType属性不能空，且不支持resultMap返回结果！");
					}
					Class<?> resultType =resultMaps.get(0).getType();
					if(resultType.getSimpleName().equals("Object")){
						//针对返回结果为泛型，无法实时获取返结果类型的情况，采用参数类型为结果类型
						resultType=parameterType;
						resultMaps = new ArrayList<ResultMap>();
						ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(
								configuration,
								mappedStatement.getId() + "-Inline",
								resultType,
								new ArrayList<ResultMapping>(),
								null);
						resultMaps.add(inlineResultMapBuilder.build());
						boolean hasNestedResultMaps=false;
						for (ResultMap resultMap : resultMaps) {
							hasNestedResultMaps = hasNestedResultMaps || resultMap.hasNestedResultMaps();
						}
						ReflectUtils.setValueByFieldName(mappedStatement, "hasNestedResultMaps", hasNestedResultMaps);
						ReflectUtils.setValueByFieldName(mappedStatement, "resultMaps", Collections.unmodifiableList(resultMaps));
					}
					sql=builderSqlByEntityClass(resultType,sqlCommandType,mappedStatementId);					
				}else{
					sql=builderSqlByEntityClass(parameterType,sqlCommandType,mappedStatementId);
				}
			    DynamicContext context = new DynamicContext(configuration, parameterObject);
			    SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(configuration);
			    SqlSource sqlSource = sqlSourceBuilder.parse(sql, parameterType, context.getBindings());
			    BoundSql boundSqlTemp = sqlSource.getBoundSql(parameterObject);
			    for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
			    	boundSqlTemp.setAdditionalParameter(entry.getKey(), entry.getValue());
			    }
			    sql=boundSqlTemp.getSql();
				ReflectUtils.setValueByFieldName(boundSql, "sql", sql);
			    ReflectUtils.setValueByFieldName(boundSql, "parameterMappings", boundSqlTemp.getParameterMappings());
			    ReflectUtils.setValueByFieldName(boundSql, "parameterObject", boundSqlTemp.getParameterObject());
			    ReflectUtils.setValueByFieldName(boundSql, "additionalParameters", ReflectUtils.getValueByFieldName(boundSqlTemp, "additionalParameters"));
			    ReflectUtils.setValueByFieldName(boundSql, "metaParameters", ReflectUtils.getValueByFieldName(boundSqlTemp, "metaParameters"));
			}
			if(parameterObject instanceof QueryCondition){
				QueryCondition queryCondition = (QueryCondition) parameterObject;
				if(queryCondition.isSort()){
					StringBuilder sortString=new StringBuilder();
					List<SortParam> sortParamList=queryCondition.getSortParamList();
					for(SortParam sortParam:sortParamList){
						if(sortParam.getSortOption()==null || sortParam.getSortOption().equals("")){
							sortParam.setSortOption("asc");
						}else if(!sortParam.getSortOption().equalsIgnoreCase("asc")
								&& !sortParam.getSortOption().equalsIgnoreCase("desc")){
							String errorInfo="排序关键字不是asc或desc，SQL："+sql+"\n出错的排序参数："+sortParam;
							log.error(errorInfo);
							throw new RuntimeException(errorInfo);					
						}	
						sortString.append(this.getOrderString(sql, sortParam.getPropertyName(), sortParam.getSortOption())+",");
					}
					sql+=" ORDER BY "+sortString.substring(0, sortString.length()-1).toString();
					ReflectUtils.setValueByFieldName(boundSql, "sql", sql); 
				}
				if(parameterObject instanceof PageQueryCondition){
					PageQueryCondition pageQueryCondition = (PageQueryCondition) parameterObject;
					String pageSql="";
					if(pageQueryCondition.getPageSize()==0){
						pageSql = sql;
					}else{
						int count = 0;
						PreparedStatement countStmt=null;
						ResultSet rs=null;
						try{
							countStmt = connection.prepareStatement(dialect.getCountString(sql));
							delegate.parameterize(countStmt);
							rs = countStmt.executeQuery();
							if (rs.next()) {
								count = rs.getInt(1);
							}
						}finally{
							if(rs!=null) rs.close();
							if(countStmt!=null) countStmt.close();
						}
						pageQueryCondition.setTotalRecord(count);
						pageSql = dialect.getLimitString(sql, pageQueryCondition.getBeginRowNumber(), pageQueryCondition.getEndRowNumber());
					}
					ReflectUtils.setValueByFieldName(boundSql, "sql", pageSql);
				}
			}
		}
		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		dialectName = properties.getProperty("dialect");
		if (dialectName==null || dialectName.equals("")) {
			try {
				throw new PropertyException("The Dialect was not set. Set the property dialect.");
			} catch (PropertyException e) {
				log.error(e.getMessage(),e);
			}
		}
	}

	/**
	 * @Title: getOrderString
	 * @Description: 根据传入的属性名获取排序字符串，属性名必须与SQL语句中的别名或字段名一至（排除下划线全小写对比一致）
	 * @param sql SQL语句
	 * @param propertyName 顺序的需要排序的属性名，此属性名必须与SQL语句中的别名或字段名一至（排除下划线全小写对比一致）
	 * @param order 对应上方属性名的排序方式
	 * @return 排序语句，即ORDER BY 后的语句
	 */
	public String getOrderString(String sql,String propertyName,String order){
		StringBuilder resultOrderString=new StringBuilder("");
		String fields=this.getFields(sql, "");
		propertyName=propertyName.replace("_", "");
		if(fields.matches("(?si).*"+propertyName+".*")){
			String countString=fields.replaceFirst("(?si)"+propertyName+".*", "");
			Matcher matcher=Pattern.compile("\\,").matcher(countString);
			int count=1;
			while(matcher.find()){
				count++;
			}
			resultOrderString.append(count+" "+order);
		}else{
			String errorInfo="用于排序的属性\""+propertyName+"\"在此SQL中无对应字段，SQL："+sql;
			log.error(errorInfo);
			throw new RuntimeException(errorInfo);
		}
		return resultOrderString.toString();
	}
	
	/**
	 * @Title: getFields 
	 * @Description: 从SQL语名中获取字段顺序列表,用于判断字段在第几列，支持解析*号所指定的子SQL
	 * @param sql SQL语句
	 * @param fields 递归参数，传空字符串即可
	 * @return 结果中各字段的顺序列表，各字段间用逗号分隔
	 */
	public String getFields(String sql,String fields){
		String sqlLower=sql.toLowerCase().replaceAll("count\\(\\*\\)", "count(0)").replaceAll("_", "").trim();
		if(sqlLower.matches("(?s).*union.*")){
			String result="";
			List<String> childSqlLowerList=new ArrayList<String>();
			while(sqlLower.matches("(?s).*union\\s+all.*")||sqlLower.matches("(?s).*union.*")){
				childSqlLowerList.add(sqlLower.substring(0, sqlLower.indexOf("union")).trim());
				sqlLower=sqlLower.substring(sqlLower.indexOf("union")+5).trim();
				if(sqlLower.startsWith("all")){
					sqlLower=sqlLower.substring(3).trim();
				}
				if(!sqlLower.matches("(?s).*union.*")){
					childSqlLowerList.add(sqlLower);
				}
			}
			for(int i=0;i<childSqlLowerList.size();i++){
				String childSqlLower=childSqlLowerList.get(i);
				if(i==childSqlLowerList.size()-1){
					result+=getFields(childSqlLower,"");
				}else{
					result+=getFields(childSqlLower,"")+",";
				}
			}
			return result;
		}
		char[] sqlLowerCharArray=sqlLower.toCharArray();
		int pairFlag=1;
		StringBuilder s=new StringBuilder();
		int beginIndex=sqlLower.indexOf("select")+6;
		for(int i=beginIndex;i<sqlLowerCharArray.length;i++){
			String tempStr=s.append(sqlLowerCharArray[i]).toString();
			if(tempStr.endsWith("select")){
				pairFlag++;
			}
			if(tempStr.endsWith("from")){
				pairFlag--;
			}
			if(pairFlag==0){
				fields=sqlLower.substring(beginIndex,i-4).trim();
				break;
			}
		}
		fields=fields.replaceAll("\\(\\s*select.*from.*\\)\\s+", "");
		if(fields.indexOf("*")>-1){
			sqlLower=sqlLower.substring(sqlLower.indexOf("from")+4).trim();
			sqlLower=sqlLower.startsWith("(")?sqlLower.substring(1,sqlLower.length()-1):sqlLower;
			fields=fields.replaceAll("\\*", getFields(sqlLower,fields));				
		}
		return fields.replaceAll("\\s+", " ").replaceAll("\\s*\\,\\s*", ",").replaceAll("[^\\,]+\\s", "").replaceAll("[^\\,]+\\.", "");
	}
	
	/**
	 * @Title: builderSqlByEntityClass
	 * @Description: 构建SQL
	 * @param entityClass 实体类，即注解为@Table的类
	 * @param sqlCommandType 生成的SQL类型
	 * @return SQL
	 * @throws SQLException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private String builderSqlByEntityClass(Class<?> entityClass,SqlCommandType sqlCommandType,String mappedStatementId) throws SQLException, IllegalArgumentException, IllegalAccessException{
		return this.builderSqlByEntityClass(entityClass, null, sqlCommandType, null, mappedStatementId);		
	}
	
	/**
	 * @Title: builderSqlByEntityClass
	 * @Description: 构建SQL
	 * @param entityObject 实体对象，即注解为@Table的类的对象
	 * @param sqlCommandType 生成的SQL类型
	 * @return SQL
	 * @throws SQLException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private String builderSqlByEntityClass(Object entityObject,SqlCommandType sqlCommandType,Connection connection,String mappedStatementId) throws SQLException, IllegalArgumentException, IllegalAccessException{
		return this.builderSqlByEntityClass(entityObject.getClass(), entityObject, sqlCommandType, connection, mappedStatementId);
	}
	
	/**
	 * @Title: builderSqlByEntityClass
	 * @Description: 构建SQL
	 * @param entityClass 实体类，即注解为@Table的类
	 * @param sqlCommandType 生成的SQL类型
	 * @return SQL
	 * @throws SQLException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private String builderSqlByEntityClass(Class<?> entityClass,Object entityObject,SqlCommandType sqlCommandType,Connection connection,String mappedStatementId) throws SQLException, IllegalArgumentException, IllegalAccessException{
		boolean isOnlyUpdateNotNullField=mappedStatementId.endsWith(BASE_MAPPER_UPDATE_BY_NOT_NULL);
		String result=null;
		if(!isOnlyUpdateNotNullField){
			result=entityClassMappingSqlMemoryTable.get(entityClass.getName()+"-"+sqlCommandType.name());
		}
		if(result==null){
			Table table=entityClass.getAnnotation(Table.class);
			if(table!=null){
				String tableName=table.name();
				if(tableName.equals("")){
					tableName=generatedUnderscoreToCamelCase(entityClass.getSimpleName());
				}
				switch (sqlCommandType) {
				case INSERT:
					{
						StringBuilder nameString=new StringBuilder();
						StringBuilder valueString=new StringBuilder();
						List<Field> fieldList= new ArrayList<Field>(Arrays.asList(entityClass.getDeclaredFields()));
						Class<?> superclass=entityClass.getSuperclass();
						while(!superclass.isAssignableFrom(Object.class)){
							fieldList.addAll(new ArrayList<Field>(Arrays.asList(superclass.getDeclaredFields())));
							superclass=superclass.getSuperclass();
						}
						for(Field field:fieldList){
							if(!field.isAnnotationPresent(Transient.class) && !field.getName().equals("serialVersionUID")){
								Column column=field.getAnnotation(Column.class);
								if(column==null || column.name().equals("")){
									nameString.append(generatedUnderscoreToCamelCase(field.getName())+",");
								}else{
									nameString.append(column.name()+",");
								}
								if(column!=null && column.isPrimaryKey()
										&& (
											column.generatedPrimaryKey32UUID()
											|| (column.generatedPrimaryKeySQLExpression()!=null && !column.generatedPrimaryKeySQLExpression().equals(""))
								   		)
								){
									if (!field.isAccessible()) {
										field.setAccessible(true);
									}
									Object filedValue=field.get(entityObject);
									if(filedValue==null || filedValue.toString().equals("")) {
										String primaryKey = "";
										if (column.generatedPrimaryKey32UUID()) {
											primaryKey = UUID.randomUUID().toString();
										} else if (column.generatedPrimaryKeySQLExpression() != null && !column.generatedPrimaryKeySQLExpression().equals("")) {
											primaryKey = this.generatedPrimaryKey(column.generatedPrimaryKeySQLExpression(), connection);
										}
										field.set(entityObject, primaryKey);
									}
								}
								valueString.append("#{"+field.getName()+dialect.getJdbcTypeExpression(field.getType())+"},");									
							}
						}				
						result="INSERT INTO "+tableName+"("+
									(nameString.toString().endsWith(",")?nameString.substring(0, nameString.length()-1).toString():nameString.toString())+
								")VALUES("+
									(valueString.toString().endsWith(",")?valueString.substring(0, valueString.length()-1).toString():valueString.toString())+
								")";
					}
					break; 
				case DELETE:
					{
						StringBuilder whereString=new StringBuilder();
						List<Field> fieldList= new ArrayList<Field>(Arrays.asList(entityClass.getDeclaredFields()));
						Class<?> superclass=entityClass.getSuperclass();
						while(!superclass.isAssignableFrom(Object.class)){
							fieldList.addAll(new ArrayList<Field>(Arrays.asList(superclass.getDeclaredFields())));
							superclass=superclass.getSuperclass();
						}
						for(Field field:fieldList){
							if(!field.isAnnotationPresent(Transient.class) && !field.getName().equals("serialVersionUID")){
								Column column=field.getAnnotation(Column.class);
								if(column!=null && column.isPrimaryKey()){
									whereString.append(" AND ");
									if(!column.name().equals("")){
										whereString.append(column.name());
									}else{
										whereString.append(generatedUnderscoreToCamelCase(field.getName()));
									}
									whereString.append("=#{"+field.getName()+"}");
								}
							}
						}
						if(whereString.length()==0){
							throw new RuntimeException("实体类["+entityClass+"("+tableName+")]必须至少有一个成员属性注解为主键！");
						}
						result="DELETE FROM "+tableName+" WHERE 1=1"+whereString.toString();
					}
					break; 
				case UPDATE:
					{
						StringBuilder setString=new StringBuilder();
						StringBuilder whereString=new StringBuilder();
						List<Field> fieldList= new ArrayList<Field>(Arrays.asList(entityClass.getDeclaredFields()));
						Class<?> superclass=entityClass.getSuperclass();
						while(!superclass.isAssignableFrom(Object.class)){
							fieldList.addAll(new ArrayList<Field>(Arrays.asList(superclass.getDeclaredFields())));
							superclass=superclass.getSuperclass();
						}
						for(Field field:fieldList){
							if(!field.isAnnotationPresent(Transient.class) && !field.getName().equals("serialVersionUID")){
								Column column=field.getAnnotation(Column.class);
								String columnName="";
								if(column==null || column.name().equals("")){
									columnName=generatedUnderscoreToCamelCase(field.getName());
								}else{
									columnName=column.name();
								}
								if(column!=null && column.isPrimaryKey()){
									whereString.append(" AND "+columnName+"=#{"+field.getName()+"}");
								}else{
									if(isOnlyUpdateNotNullField){
										Object fieldValue = null;
										if(field!=null){
											if (field.isAccessible()) {
												fieldValue = field.get(entityObject);
											} else {
												field.setAccessible(true);
												fieldValue = field.get(entityObject);
												field.setAccessible(false);
											}
										}
										if(fieldValue!=null){
											setString.append(columnName+"=#{"+field.getName()+dialect.getJdbcTypeExpression(field.getType())+"},");
										}
									}else{
										setString.append(columnName+"=#{"+field.getName()+dialect.getJdbcTypeExpression(field.getType())+"},");
									}
								}						
							}
						}
						if(whereString.length()==0){
							throw new RuntimeException("实体类["+entityClass+"("+tableName+")]必须至少有一个成员属性注解为主键！");
						}
						if(setString.length()==0){
							throw new RuntimeException("实体类["+entityClass+"("+tableName+")]所有非主键属性的值全为null值时，不能使用updateByNotNull方法！");
						}
						result="UPDATE "+tableName+" SET "+
									(setString.toString().endsWith(",")?setString.substring(0, setString.length()-1).toString():setString.toString())+
								" WHERE 1=1 "+whereString.toString();
					}
					break; 
				case SELECT:
					{
						StringBuilder nameString=new StringBuilder();
						StringBuilder whereString=new StringBuilder();
						List<Field> fieldList= new ArrayList<Field>(Arrays.asList(entityClass.getDeclaredFields()));
						Class<?> superclass=entityClass.getSuperclass();
						while(!superclass.isAssignableFrom(Object.class)){
							fieldList.addAll(new ArrayList<Field>(Arrays.asList(superclass.getDeclaredFields())));
							superclass=superclass.getSuperclass();
						}
						for(Field field:fieldList){
							if(!field.isAnnotationPresent(Transient.class) && !field.getName().equals("serialVersionUID")){
								Column column=field.getAnnotation(Column.class);
								String columnName="";
								if(column==null || column.name().equals("")){
									columnName=generatedUnderscoreToCamelCase(field.getName());
								}else{
									columnName=column.name();
								}
								if(column!=null && column.isPrimaryKey()){
									whereString.append(" AND "+columnName+"=#{"+field.getName()+"}");
								}
								nameString.append(columnName+",");													
							}
						}
						if(whereString.length()==0){
							throw new RuntimeException("实体类["+entityClass+"("+tableName+")]必须至少有一个成员属性注解为主键！");
						}				
						result=" SELECT "+
								(nameString.toString().endsWith(",")?nameString.substring(0, nameString.length()-1).toString():nameString.toString())+
								"  FROM "+tableName+
								" WHERE 1=1 "+whereString.toString();
					}
					break;
				default:
					break;
				}
				if(!isOnlyUpdateNotNullField){
					entityClassMappingSqlMemoryTable.put(entityClass.getName()+"-"+sqlCommandType.name(),result);
				}
			}
		}else if(sqlCommandType==SqlCommandType.INSERT){
			List<Field> fieldList= new ArrayList<Field>(Arrays.asList(entityClass.getDeclaredFields()));
			Class<?> superclass=entityClass.getSuperclass();
			while(!superclass.isAssignableFrom(Object.class)){
				fieldList.addAll(new ArrayList<Field>(Arrays.asList(superclass.getDeclaredFields())));
				superclass=superclass.getSuperclass();
			}
			for(Field field:fieldList){
				if(!field.isAnnotationPresent(Transient.class) && !field.getName().equals("serialVersionUID")){
					Column column=field.getAnnotation(Column.class);					
					if(column!=null && column.isPrimaryKey() && (
							column.generatedPrimaryKey32UUID() 
							|| (column.generatedPrimaryKeySQLExpression()!=null && !column.generatedPrimaryKeySQLExpression().equals(""))
					   )
					){
						if (!field.isAccessible()) {
							field.setAccessible(true);
						}
						Object filedValue=field.get(entityObject);
						if(filedValue==null || filedValue.toString().equals("")) {
							String primaryKey = "";
							if (column.generatedPrimaryKey32UUID()) {
								primaryKey = UUID.randomUUID().toString();
							} else if (column.generatedPrimaryKeySQLExpression() != null && !column.generatedPrimaryKeySQLExpression().equals("")) {
								primaryKey = this.generatedPrimaryKey(column.generatedPrimaryKeySQLExpression(), connection);
							}
							field.set(entityObject, primaryKey);
						}
					}						
				}
			}
		}
		return result;		
	}
	
	/**
	 * @Title: generatedUnderscoreToCamelCase
	 * @Description: 在驼峰式大小写间增加下划线
	 * @param sourceString 源字符串
	 * @return 带下划线的字符串
	 */
	private String generatedUnderscoreToCamelCase(String sourceString){
		StringBuilder resultString=new StringBuilder();
		Boolean oldCharIsUpperCase=null;
		for(char c:sourceString.toCharArray()){
			boolean currCharIsUpperCase=Pattern.matches("\\p{javaUpperCase}",String.valueOf(c));
			if(oldCharIsUpperCase!=null && !oldCharIsUpperCase && currCharIsUpperCase){
				resultString.append("_");
			}
			resultString.append(c);
			oldCharIsUpperCase=currCharIsUpperCase;
		}
		return resultString.toString();
	}
	
	private String generatedPrimaryKey(String sqlExpression,Connection connection) throws SQLException{
		String primaryKey="";
		String sql="SELECT "+sqlExpression+" FROM dual";
		PreparedStatement countStmt=null;
		ResultSet rs=null;
		try{
			countStmt = connection.prepareStatement(sql);
			rs = countStmt.executeQuery();
			if (rs.next()) {
				primaryKey = rs.getString(1);
			}
		}finally{
			if(rs!=null) rs.close();
			if(countStmt!=null) countStmt.close();
		}
		return primaryKey;
	}
	
	
	@SuppressWarnings("javadoc")
	public static void main(String[] args){
		MyBatisPlugin myBatisPlugin=new MyBatisPlugin();
//		String sqlLower="SELECT DISTINCT            a.notice_id            ,a.title            ,a.release_time            ,a.content            ,a.publisher_id            ,CASE WHEN f.operat_log_id IS NULL THEN 0 ELSE 1 END is_see            ,b.name publisherName            ,(	            SELECT REPLACE(WMSYS.WM_CONCAT(m.name),',','/') 	              FROM ts_work_organ m	            START WITH m.work_organ_id=g.work_organ_id	            CONNECT BY PRIOR m.parent_work_organ_id=m.work_organ_id            ) publisher_work_organ_path       FROM tn_notice a            JOIN ts_operat_obj b ON b.operat_obj_id=a.publisher_id             JOIN tn_recipient c ON c.notice_id=a.notice_id            JOIN ts_operat_obj d ON d.is_valid='1' AND d.operat_obj_id=c.operat_obj_id            LEFT JOIN te_team_member e ON e.team_id=d.operat_obj_id AND d.type='2'            LEFT JOIN ts_operat_log f ON f.func_id='1025' AND f.param LIKE '%id:'||a.notice_id||'%' AND (f.user_id=c.operat_obj_id OR f.user_id=e.user_id)            JOIN ts_user g ON g.user_id=b.operat_obj_id      WHERE a.is_valid='1'		             AND (d.operat_obj_id=20140516111928001527            OR e.user_id=20140516111928001527)";
//		System.out.println(myBatisPlugin.getFields(sqlLower, ""));
		String s="ab12HC";
		System.out.println(myBatisPlugin.generatedUnderscoreToCamelCase(s));
	}
}
