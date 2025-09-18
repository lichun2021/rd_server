package com.hawk.game.battle.effect.impl.hero1108;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
- 【万分比】【12511】自身出征数量最多的主战坦克在触发暴击时，增加自身 X(effect12511AddFirePoint)  点涌动值（若命中敌方主战坦克单位，额外增加 X(effect12511AddFirePointExtra)  点），
	每第 X (effect12511AtkRound) 回合开始时，若当前涌动值不低于 X(effect12511AtkThresholdValue 点，则进入能量激荡状态持续 X（effect12511ContinueRound）回合，并清空自身涌动值；能量激荡状态攻击时额外附加如下效果：
- 激能迸发：自身主战坦克暴击伤害增加 effect12511BaseVaule + XX.XX%【12511】*敌方空军单位数 （敌方空军单位计数时至多取 effect12511CountMaxinum 个）
  - 战报相关
    - 战报中隐藏
    - 不合并至精简战报中
  - 自身出征数量最多的主战坦克
    - 该作用号效果仅对玩家出征时数量最多的主战坦克（兵种类型 = 2）生效（若存在多个主战坦克数量一样且最高，取等级高的）
      - 注：若存在荣耀所罗门的幻影部队，此作用号对自身幻影部队不生效
  - 在触发暴击时
    - 触发暴击时指对敌方造成伤害并暴击时，如敌方两个单位受到一次AOE攻击，则针对两个单位的伤害单独计算暴击次数，如果都暴击则算2次
  - 增加自身 X(effect12511AddFirePoint)  点涌动值（若命中敌方主战坦克单位，额外增加 X(effect12511AddFirePointExtra)  点）
    - 增加涌动值读取const表，字段effect12511AddFirePoint
      - 配置格式：绝对值
    - 额外增加涌动值读取const表，字段effect12511AddFirePointExtra
      - 若本次暴击命中单位为敌方主战坦克单位（兵种类型 = 2），额外增加本次获取涌动值
        - 增加涌动值=基础值(effect12511AddFirePoint)+额外值(effect12511AddFirePointExtra)  
      - 配置格式：绝对值
  - 每第 X 回合开始时
    - 战斗中每第 X 的倍数的回合开始后，自身开始普攻前，进行判定
    - 指定回合数读取const表，字段effect12511AtkRound
      - 配置格式：绝对值
  - 若当前涌动值不低于 X 点
    - 数量1 = 自身主战坦克涌动值
    - 数量2 = 触发涌动值
      - 数量1 >= 数量2
        - 数量2读取const表，字段effect12511AtkThresholdValue
  - 进入能量激荡状态持续 X 回合
    - 回合开始：涌动值判定成功后，会立刻进入能量激荡状态，当前回合算作第1回合
    - 回合结束：当前回合结束时进行判定，如果 当前持续回合=理应持续回合，则清除能量激荡状态
    - 持续回合数读取const表，字段effect12511ContinueRound
      - 配置格式：绝对值
  - 激能迸发：自身主战坦克暴击伤害增加 effect12511BaseVaule + XX.XX%【12511】*敌方空军单位数 （敌方空军单位计数时至多取 effect12511CountMaxinum 个）
    - 该作用号固定数值读取const表，字段effect12511BaseVaule
      - 配置格式：万分比
    - 另外该计数有最高值限制，读取const表，字段effect12511CountMaxinum
      - 配置格式：绝对值
    - 暴击伤害
      - 对主战坦克兵种，是指其暴击时的额外伤害倍率
      - 即实际暴击伤害 = 基础伤害*（1 + 各类加成）*（1 - 各类减免）* （1+其他暴击伤害加成值+【本作用值】）
      - 配置格式：万分比
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12511)
public class Checker12511 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.type != SoldierType.TANK_SOLDIER_2 || parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.TANK_SOLDIER_2)) {
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