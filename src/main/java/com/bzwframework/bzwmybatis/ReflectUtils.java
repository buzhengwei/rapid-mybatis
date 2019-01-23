package com.bzwframework.bzwmybatis;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * @ClassName: ReflectUtils
 * @Package com.sdepc.fim.common.util
 * @Description: 反射工具�?
 * @author zhengwei.bu
 * @date 2014�?3�?3�? 上午9:26:29
 */
public class ReflectUtils {
		
	/**
	 * @Title: getFieldByFieldName
	 * @Description: 根据属�?�名（对象的成员变量）获取其属�?�对�?
	 * @param target 目标源对�?
	 * @param fieldName 属�?�名（对象成员变量的名称�?
	 * @return 属�?�对�?
	 */
	public static Field getFieldByFieldName(Object target, String fieldName) {
		Class<?> superClass = target.getClass();
		while( superClass != Object.class) {
			try {
				return superClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
			}
			superClass = superClass.getSuperclass();
		}
		return null;
	}

	/**
	 * @Title: getValueByFieldName
	 * @Description: 根据属�?�名（对象的成员变量）获取其属�?�的�?
	 * @param target 目标源对�?
	 * @param fieldName 属�?�名（对象成员变量的名称�?
	 * @return 属�?�的�?
	 * @throws IllegalArgumentException 获取属�?��?�时出现异常
	 * @throws IllegalAccessException 获取属�?��?�时出现异常
	 */
	public static Object getValueByFieldName(Object target, String fieldName) 
			throws IllegalArgumentException, IllegalAccessException{
		Field field = getFieldByFieldName(target, fieldName);
		Object value = null;
		if(field!=null){
			if (field.isAccessible()) {
				value = field.get(target);
			} else {
				field.setAccessible(true);
				value = field.get(target);
				field.setAccessible(false);
			}
		}
		return value;
	}

	/**
	 * @Title: setValueByFieldName
	 * @Description: 根据属�?�名（对象的成员变量）为该属性设置属性�??
	 * @param target 目标源对�?
	 * @param fieldName 属�?�名（对象成员变量的名称�?
	 * @param value 属�?�的�?
	 * @throws IllegalArgumentException 设置属�?��?�时出现异常
	 * @throws IllegalAccessException 设置属�?��?�时出现异常
	 * @throws NoSuchFieldException 获取属�?�对象时出现异常
	 */
	public static void setValueByFieldName(Object target, String fieldName, Object value) 
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException{
		Field field = getFieldByFieldName(target, fieldName);
		if(field==null){
			throw new NoSuchFieldException("在["+target.getClass()+"]中找不到["+fieldName+"]属�?�！");
		}
		if (field.isAccessible()) {
			field.set(target, value);
		} else {
			field.setAccessible(true);
			field.set(target, value);
			field.setAccessible(false);
		}
	}
	
	/**
	 * @Title: getMethodByMethodName
	 * @Description: 根据方法名与方法参数类型获取其方法对�?
	 * @param targetClass 目标源对象的类型
	 * @param methodName 方法�?
	 * @param parameterTypes 参数类型数组
	 * @return 方法对象
	 */
	public static Method getMethodByMethodName(Class<?> targetClass, String methodName, Class<?>[] parameterTypes) {
		Class<?> superClass = targetClass;
		while( superClass != Object.class) {
			try {
				return superClass.getDeclaredMethod(methodName,parameterTypes);
			} catch (NoSuchMethodException e) {
			}
			superClass = superClass.getSuperclass();
		}
		return null;
	}
	
	/**
	 * @Title: executeMethodByMethodName
	 * @Description: 根据方法名，执行指定对象的方�?
	 * @param target 目标源对�?
	 * @param methodName 方法�?
	 * @param parameters �?要传的参数，数组按顺传参
	 * @return 方法的执行返回结�?
	 * @throws IllegalAccessException 执行方法时出现异�?
	 * @throws IllegalArgumentException 执行方法时出现异�?
	 * @throws InvocationTargetException 执行方法时出现异�?
	 * @throws NoSuchMethodException 获取方法对象时出现异�?
	 */
	public static Object executeMethodByMethodName(Object target, String methodName, Object[] parameters) 
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		Object result=null;
		Class<?>[] parameterTypes=null;
		if(parameters!=null){
			List<Class<?>> parameterTypeList=new ArrayList<Class<?>>();
			for(Object parameter:parameters){
				parameterTypeList.add(parameter.getClass());
			}
			parameterTypes=(Class<?>[]) parameterTypeList.toArray();
		}		
		Method method = getMethodByMethodName(target.getClass(),methodName,parameterTypes);
		if(method==null){
			throw new NoSuchMethodException("在["+target.getClass()+"]中找不到["+methodName+"]方法�?");
		}
		if (method.isAccessible()) {
			result=method.invoke(target, parameters);
		} else {
			method.setAccessible(true);
			result=method.invoke(target, parameters);
			method.setAccessible(false);
		}
		return result;
	}

}
