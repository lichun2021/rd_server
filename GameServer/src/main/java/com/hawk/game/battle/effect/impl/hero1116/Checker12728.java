package com.hawk.game.battle.effect.impl.hero1116;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12728)
public class Checker12728 implements IChecker {
	/**
	 * 【12727~12728】
- 【万分比】【12727~12728】镜像残影：通过战场中布置的棱镜矩阵，在行动时出现大量镜像残影误导敌人，敌方对自身狙击兵发起进攻时会被残影迷惑，自身狙击兵受到伤害减少 25%【12727】(->针对敌方兵种留个内置系数effect12727SoldierAdjust)，如本回合进行形态切换，则使产生更多残影，自身狙击兵受到伤害再减少 20%【12728】。(->针对敌方兵种留个内置系数effect12728SoldierAdjust)
  - 战报相关
  - 于战报中隐藏
  - 不合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 注：技能优先级需要注意
  - 在【12723】形态转换触发后触发
  - 在本回合受到伤害前触发
  - 如本回合没有进行形态切换，自身狙击兵受到伤害减少 25%【12727】
    - 【12723】作用号触发为没有进行形态切换则触发
      - 注：形态和上回合一致则为不触发
    - 该作用号为伤害减少效果，作用号自身叠加为作用值累加计算，与其他作用号累乘计算，即
      - 最终伤害 = 基础伤害 *（1 + 各类加成）*（1 - 各类减免）*（1 - 【12727作用值】* 敌方兵种修正系数/10000）
      - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect12727SoldierAdjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
  - 如本回合进行形态切换，则使产生更多残影，自身狙击兵受到伤害再减少 20%【12728】
    - 【12723】作用号触发形态切换则触发
      - 注：从狙击切换为紧凑，或者紧凑切换为狙击都触发
    - 该作用号为伤害减少效果，作用号自身叠加为作用值累加计算，与其他作用号累乘计算，即
      - 最终伤害 = 基础伤害 *（1 + 各类加成）*（1 - 各类减免）*（1 - 【12728作用值】* 敌方兵种修正系数/10000）
      - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect12728SoldierAdjust
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
