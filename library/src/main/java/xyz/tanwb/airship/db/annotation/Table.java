package xyz.tanwb.airship.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import xyz.tanwb.airship.BaseConstants;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    /**
     * 表名
     */
    String name();

    /**
     * 是否创建
     */
    String onCreated() default BaseConstants.NULL;
}

