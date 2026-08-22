package com.hawk.game.battle.effect.impl.hero1116;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12730)
public class Checker12730 implements IChecker {
	/**
	 * 【12729~12730】
- 【万分比】【12729~12730】个人战时，使用动态光学隐身与环境融合，使自身在前X（effect12729AtkRound）回合难以被察觉，受到伤害降低XX.XX%【12729】(->针对敌方兵种留个内置系数effect12729SoldierAdjust)，X（effect12730AtkRound）回合后解除迷彩，移动至最优射击位置，使得自身狙击兵造成伤害增加XX.XX%【12730】(->针对敌方兵种留个内置系数effect12730SoldierAdjust)
  - 战报相关
  - 于战报中隐藏
  - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 使自身在前X（effect12729AtkRound）回合难以被察觉
    - 指定回合数读取const表，字段effect12729AtkRound
      - 配置格式：绝对值
  - 受到伤害降低XX.XX%【12729】(->针对敌方兵种留个内置系数effect12729SoldierAdjust)
    - 该作用号为伤害减少效果，作用号自身叠加为作用值累加计算，与其他作用号累乘计算，即
      - 最终伤害 = 基础伤害 *（1 + 各类加成）*（1 - 各类减免）*（1 - 【12729作用值】* 敌方兵种修正系数/10000）
      - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect12729SoldierAdjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
  - X（effect12730AtkRound）回合后解除迷彩
    - 指定回合数读取const表，字段effect12730AtkRound
      - 配置格式：绝对值
  - 使得自身狙击兵造成伤害增加XX.XX%【12730】(->针对敌方兵种留个内置系数effect12730SoldierAdjust)
    - 该作用号为外围伤害加成效果，与其他伤害增加作用号累加计算（与12101/10082等伤害增加作用号加法运算）
      - 即 实际伤害 = 基础伤害*（1 + 各类伤害加成+【12730作用值】* 敌方兵种修正系数/10000）
        - 配置格式：万分比
      - 实际单独配置系数；敌方兵种修正系数 读取const表，字段effect12730SoldierAdjust
        - 配置格式：修正系数
          - 配置格式：万分比
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12721) == 0 || !Hero1116Rules.isBothSelfFight(BattleConst.WarEff.SELF_FIGHT.check(parames.troopEffType), BattleConst.WarEff.SELF_FIGHT.check(parames.tarTroopEffType))) {
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
