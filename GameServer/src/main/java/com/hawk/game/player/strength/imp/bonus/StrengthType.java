package com.hawk.game.player.strength.imp.bonus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 战力类型
 * @author Administrator
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StrengthType {
	
	/**
	 * 类型
	 * @return
	 */
	int strengthType();
}
