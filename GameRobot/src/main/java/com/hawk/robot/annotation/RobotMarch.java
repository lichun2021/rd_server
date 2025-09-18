package com.hawk.robot.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 机器人行军
 * 
 * @author lating
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE, ElementType.METHOD })
public @interface RobotMarch {
	/**
	 * 行军类型
	 * 
	 * @return
	 */
	String marchType();
}
