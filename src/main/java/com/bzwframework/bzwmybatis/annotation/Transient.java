package com.bzwframework.bzwmybatis.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @ClassName: Transient
 * @Package com.sdepc.fim.common.jdbc.annotation
 * @Description: 指定该属性不是持久化的列，即此属性在数据库表中无对应列�??
 * @author zhengwei.bu
 * @date 2014�?3�?3�? 下午3:48:42
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Transient {

}
