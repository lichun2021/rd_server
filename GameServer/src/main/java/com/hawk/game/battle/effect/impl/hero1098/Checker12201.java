package com.hawk.game.battle.effect.impl.hero1098;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * - 【万分比】【12201】蓄能：出征或驻防时，自身出征数量最多的突击步兵每攻击命中 1 个敌方非防御坦克单位后，增加自身 10 点蓄能；若目标处于【燃烧状态】，额外增加 3 点蓄能
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 自身出征数量最多的突击步兵
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的突击步兵（兵种类型 = 5）生效（若存在多个突击步兵数量一样且最高，取等级高的）
    - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队也生效
  - 每攻击命中 1 个敌方非防御坦克单位后
    - 非防御坦克单位包含有：1~8兵种类型（防御坦克 = 1 除外）
    - 攻击每命中1个敌方单位，则增加 10 点蓄能
    - 增加蓄能值读取const表，字段effect12201BasePoint
      - 配置格式：绝对值
    - 若目标处于【燃烧状态】且为非防御坦克单位，则额外增加蓄能值；读取const表，字段effect12201ExtraPoint
      - 配置格式：绝对值
    - 另外该蓄能值有数量上限，读取const表，字段effect12201MaxPoint
      - 配置格式：绝对值
    - 另外在触发马克西恩的AOE技能时（即作用号【1562】），不计算蓄能值
[图片]
  - 【燃烧状态】
    - 【燃烧状态】是一种特殊标识，目前来源
      - 杰拉尼的【12005】作用号
      - 杰西卡的【12161】作用号
      - 杰西卡的【12162】作用号
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12201)
public class Checker12201 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.type != SoldierType.FOOT_SOLDIER_5
				|| parames.unity.getEffVal(effType()) == 0) {
			return CheckerKVResult.DefaultVal;
		}

		String playerId = parames.unity.getPlayer().getId();
		BattleUnity max8 = parames.unitStatic.getPlayerSoldierTable().get(playerId, SoldierType.FOOT_SOLDIER_5).get(0);
		if (max8.getArmyId() != parames.unity.getArmyId()) {
			return CheckerKVResult.DefaultVal;
		}
		int effPer = parames.unity.getEffVal(effType());
		int effNum = 0;

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
	
	
}
