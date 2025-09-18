package com.hawk.game.battle.effect.impl.hero1108;

import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;

/**
【12518】
- 【万分比】【12518】自身出征数量最多的主战坦克在触发坦克对决时，本次攻击额外附加如下效果
  - 个人战时，每对敌方施加 1 次重力缠绕状态，自身主战坦克受到伤害减少 XX.XX%【12518】（可叠加，最多提升 X （effect12518Maxinum）层）->针对敌方兵种留个内置修正系数（effect12518SoldierAdjust）
  - 战报相关
  - 于战报中隐藏
  - 不合并至精简战报中
  - 自身出征数量最多的主战坦克在触发坦克对决时
    - 可无视，实际触发条件为重力缠绕，可直接关联【12515】中重力缠绕状态
  - 个人战时，每对敌方施加 1 次重力缠绕状态
    - 仅个人战生效，集结无效
    - 对多个敌方单位施加同一重力缠绕状态，也视为施加1次
  - 自身主战坦克受到伤害减少 XX.XX%【12518】
    - 该作用号为伤害减少效果，作用号自身叠加为作用值累加计算，与其他作用号累乘计算，即 
      - 最终伤害 = 基础伤害 *（1 + 各类加成）*（1 - 各类减免）*（1 - 【本作用值】* 敌方兵种修正系数/10000）
      - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect12518SoldierAdjust
        - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
          - 修正系数具体配置为万分比
  - （可叠加，最多提升 X 层）
    - 层数上限读取const表，字段effect12518Maxinum
      - 配置格式：绝对值
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL3)
@EffectChecker(effType = EffType.HERO_12518)
public class Checker12518 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (BattleConst.WarEff.MASS.check(parames.troopEffType) || parames.solider.getEffVal(EffType.HERO_12515) <= 0 || parames.tarStatic.getPlayerArmyCountMap().size() > 1) {
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