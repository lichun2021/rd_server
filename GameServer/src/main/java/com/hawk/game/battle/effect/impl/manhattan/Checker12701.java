package com.hawk.game.battle.effect.impl.manhattan;

import java.util.Comparator;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
 * 【12701】
- 【万分比】【12701】进攻战斗时，每X（effect12701AtkRound）回合随机选中敌方某指挥官全部近战部队卷入（优先选择未被选中指挥官），
使该其受到额外XX.XX%【12701】的伤害，持续X（effect12701ContinueRound）回合（效果不叠加）->针对敌方兵种留个内置修正系数（effect12701SoldierAdjust）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 此效果对战斗开始时（算作0回合？）造成的伤害也生效（数值算第1回合的）；如【11041】
[图片]
  - 进攻战斗时，每X（effect12701AtkRound）回合随机选中敌方某指挥官全部近战部队卷入（优先选择未被选中指挥官）
    - 指定数值读取const表，字段effect12701AtkRound
      - 配置格式：绝对值
    - 近战部队包含：主战坦克（兵种类型 = 2）、防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、轰炸机（兵种类型 = 3）
    - 随机逻辑同作用号【11041】，仅选取单位逻辑不同
      - 若存在多个部队数量一样且最高，取等级高的；若存在多个部队数量一样且最高且等级最高，取兵种类型小的部队（最后有且仅有1个战斗单位有此效果）
  - 使该其受到额外XX.XX%【12701】的伤害
    - 即实际伤害 = 基础伤害 *（1 + 各类加成）*（1+【本作用值】 * 敌方兵种修正系数 ）
      - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队无效！！（不会被随机选择）
  - 持续X（effect12701ContinueRound）回合（效果不叠加）
    - 回合数值读取const表，字段effect12701ContinueRound
      - 配置格式：万分比
    - 作用号效果不叠加
  - ->针对敌方兵种留个内置修正系数
    - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect12701SoldierAdjust
      - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
        - 修正系数具体配置为万分比
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_12701)
public class Checker12701 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {

		if (!BattleConst.WarEff.ATK.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}
		String pid = parames.unity.getPlayerId();
		BattleUnity max = parames.getPlayeMaxMarchArmy(pid);

		if (parames.unity != max) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type)) {
			effPer = parames.unity.getEffVal(effType());
		}

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
