package com.hawk.game.battle.effect.impl;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.ARMOUR_11009)
public class Checker11009 implements IChecker {
	/**
	 * 9	11009	万分比	突击步兵（兵种类型 = 5）在战斗中每损失XX%的数量时，增加自身YY%伤害加成（至多叠加X层）	"实际伤害 = 基础伤害 *（1 + 其他伤害加成 + 本作用值/10000）
	损失比率 = 战斗中突击步兵损失数量/战斗初始化时突击步兵部队数量
	战斗初始化时突击步兵部队数量 = 实际数量 * （1 + 所罗门效果）"	"这里计算战斗中的部队损失，若携带所罗门上阵，则战斗初始化时部队数量会高于实际携带部队数量
	至多叠加层数读取const表，字段effect11009TimesLimit
	【扭曲力场】为作用号1121的效果
	【扭曲力场】基础触发概率读取const表，字段effect1121Prob
	至多叠加层数读取const表，字段effect11010TimesLimit"	不展示	不展示													
	10	11010	万分比	突击步兵（兵种类型 = 5）在战斗中每损失XX%的数量时，芯片【扭曲力场】触发概率额外增加YY%（至多叠加X层）	"实际概率 = 基础概率 + 额外增加概率/10000
	实际概率合法取值区间【0,1】"		不展示	不展示													
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.FOOT_SOLDIER_5) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
