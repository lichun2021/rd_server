package com.hawk.game.battle.effect.impl.hero1116;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12722)
public class Checker12722 implements IChecker {
	/**
	 * 【12722】
	- 【万分比】【12722】光轨锁定：每X（effect12722AtkRound）回合开始时，向敌方后排射出光量子追踪信标，在本回合内附着于敌方X（effect12722AtkNums）个单位周围(->针对敌方兵种留个内置随机权重effect12722RoundWeight)，自身狙击兵攻击后，溢出的光量子会被信标吸引而射向目标，造成+XX.XX%【12722】伤害。(->针对敌方兵种留个内置系数)
	- 战报相关
	- 于战报中隐藏
	- 不合并至精简战报中
	- 在战斗开始前判定，满足条件后本次战斗全程生效
	- 每X（effect12722AtkRound）回合开始时，向敌方后排射出光量子追踪信标，在本回合内附着于敌方X（effect12722AtkNums）个单位周围(->针对敌方兵种留个内置随机权重)
	- 指定回合数读取const表，字段effect12722AtkRound
	  - 配置格式：绝对值
	- 注：回合开始时触发 光量子追踪信标（早于作用号【12723~12726】），常规攻击后触发伤害
	- 攻击目标数读取const表，字段effect12722AtkNums
	  - 配置格式：绝对值
	- (->针对敌方兵种留个内置随机权重)
	  - 实际针对敌方各兵种类型，配置随机权重系数；读取const表，字段effect12722RoundWeight
	    - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
	      - 修正系数具体配置为万分比
	  - 配置格式：万分比
	- 自身狙击兵攻击后，溢出的光量子会被信标吸引而射向目标，造成+XX.XX%【12722】伤害。(->针对敌方兵种留个内置系数effect12722SoldierAdjust)
	- 即 实际伤害 =  基础伤害 *（1 + 各类加成）* 【本作用值】 * 敌方兵种修正系数/10000
	  - 实际针对敌方各兵种类型，单独配置系数；自身兵种修正系数 读取const表，字段effect12722SoldierAdjust
	    - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
	      - 修正系数具体配置为万分比
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12721) == 0) {
			return CheckerKVResult.DefaultVal;
		}
		int effPer = 0;
		int effNum = 0;
		effPer = parames.unity.getEffVal(effType());
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
