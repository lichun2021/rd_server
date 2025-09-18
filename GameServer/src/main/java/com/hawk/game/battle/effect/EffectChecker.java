package com.hawk.game.battle.effect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hawk.game.protocol.Const.EffType;

/**
 * 使用做用号
 * @author lwt
 * @date 2017年11月6日
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EffectChecker {
	EffType effType();
}
