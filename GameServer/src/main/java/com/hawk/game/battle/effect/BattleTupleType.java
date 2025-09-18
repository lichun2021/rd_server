package com.hawk.game.battle.effect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 战斗作用号类型相关
 * @author lwt
 * @date 2017年11月6日
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BattleTupleType {
	enum Type{
		ATK,
		DEF,
		/**破甲*/
		CUT_DEF,
		/** 闪避 */
		DODGE, 
		HURT,
		HURT_PCT,
		REDUCE_HURT,
		/** 伤害减少的作用号在计算时为累乘算式。即实际伤害 = 基础伤害 *（1 - 其他作用值/10000）*（1 - 本作用值/10000）*/
		REDUCE_HURT_PCT,
		HP,
		DEAD_TO_WOUND,
		
		/** 泰博副本 1701-1716做用号专用. 作用号计算方式均为最外层加成。即实际属性 = 基础属性 * （1 + 常规各类作用号加成）*（1 + 本次新增作用号加成）*/
		TBLY17XX_ATK_DEF_HP,
		
		ATK_BASE,
		DEF_BASE,
		HP_BASE,
		/**远征战略*/
		NATION_ATK,
		NATION_DEF,
		NATION_HP,

		ATKFIRE,
		DEFFIRE,
		/**超能强化*/
		ATKFIRE_MHT,
		/**超能抵御*/
		DEFFIRE_MHT,
		SOLDIER_SKILL,
		SOLDIER_SKILL2,
		SOLDIER_SKILL3,
		SOLDIER_SKILL4,
		/**星能护盾*/
		FORCE_FIELD,
		;
	}
	Type[] tuple();
}

