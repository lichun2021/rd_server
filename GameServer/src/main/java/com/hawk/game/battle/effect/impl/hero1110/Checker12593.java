package com.hawk.game.battle.effect.impl.hero1110;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

/**
 *【12576】
- 关联【12574】：增幅效果状态攻击时额外附加如下效果：
- 【万分比】【12576】获得伤害吸收护盾，护盾可以吸收该单位XX.XX%（12576）的所受伤害，每次护盾生效期间可以抵消 5（effect12576ReduceFirePoint） 点震慑值叠加->针对近战兵种留个内置护盾吸收伤害系数（effect12576SoldierAdjust）
- 获得伤害吸收护盾，护盾可以吸收该单位XX.XX%（12576）的所受伤害
  - 该作用号本质为伤害减少效果，作用号自身叠加为作用值累加计算，与其他作用号累乘计算，即 
    - 最终伤害 = 基础伤害 *（1 + 各类加成）*（1 - 各类减免）*（1 - 【本作用值】* 兵种减伤修正系数/10000）
    - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect12576SoldierAdjust
      - 配置格式：兵种类型id1_修正系数1，兵种类型id2_修正系数2
        - 修正系数具体配置为万分比
- 每次护盾生效期间可以抵消 5 点震慑值叠加
  - 震慑值 为 作用号【12571】效果，叠加在敌方单位上，此处为抵消敌方采矿单位作用号【12571】施加的震慑值叠加效果
    - 效果为100%抵挡震慑值的护盾，每抵挡 1 点震慑值，护盾剩余抵挡减少 1
      - 如剩余抵挡只有1，此时受到2点震慑值，只会抵挡1点
    - 可抵挡的震慑值读取const表，字段effect12576ReduceFirePoint
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.EFF_12593)
public class Checker12593 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.EFF_12571) <= 0) {
			return CheckerKVResult.DefaultVal;
		}
		
		int effPer = parames.unity.getEffVal(effType());

		return new CheckerKVResult(effPer, 0);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
