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
- 【万分比】【12516~12517】自身出征数量最多的主战坦克在触发坦克对决时，本次攻击额外附加如下效果
  - 超能湮灭：自身主战坦克施加的重力缠绕解除时，会使所有原本处于该重力缠绕状态中单位受到一次超能攻击伤害，伤害率为 XX.XX%【12516】，且受伤害的单位的伤害效率降低 +XX.XX%【12517】（可叠加，最多提升 X （effect12517Maxinum）层）->针对敌方兵种留个内置修正系数（effect12517SoldierAdjust）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 自身出征数量最多的主战坦克在触发坦克对决时
    - 可无视，实际触发条件为重力缠绕解除，【12516~12517】可直接关联【12515】中重力缠绕解除
- 【万分比】【12516】
  - 自身主战坦克施加的重力缠绕解除时
    - 触发条件本质为重力缠绕解除，关联【12515】
  - 会使所有原本处于该重力缠绕状态中的单位受到一次超能攻击伤害
    - 超能攻击伤害，为普通攻击时，将基础攻击伤害记为0，仅保留超能伤害
      - 普通攻击：攻击伤害 = （基础攻击伤害 + 超能攻击伤害）*（1 + 各类加成）*（1 - 各类减免）
  - 伤害率为 XX.XX%【12516】
    - 实际伤害 = 超能攻击伤害*（1 + 各类加成）*（1 - 各类减免） *【本作用值】
      - 配置格式：万分比
- 【万分比】【12517】
  - 且受伤害的单位的
    - 触发条件本质为重力缠绕解除，关联【12515】
  - 伤害效率降低 +XX.XX%（可叠加，最多提升 X 层）
    - 该作用号为敌方伤害效率降低效果，与其他作用号累乘计算，叠层时在总作用值处累加计算，即敌方在造成伤害时
      - 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【总作用值】* 敌方兵种修正系数/10000）
        - 总作用值=【玩家A作用值】*玩家A叠加次数+【玩家B作用值】*玩家B叠加次数+【玩家C作用值】*玩家C叠加次数+……
        - 实际针对敌方各兵种类型，单独配置系数；敌方兵种修正系数 读取const表，字段effect12517SoldierAdjust
          - 配置格式：兵种类型id1_修正系数1，......兵种类型id8_修正系数8
            - 修正系数具体配置为万分比
    - 层数上限为多个玩家叠加次数总上限，如当前三个玩家主战坦克触发该作用号，即
      - 玩家A叠加次数+玩家B叠加次数+玩家C叠加次数=总叠加次数，总叠加次数不能超过叠加上限
      - 层数上限读取const表，字段effect12517Maxinum
      - 配置格式：绝对值
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL3)
@EffectChecker(effType = EffType.HERO_12516)
public class Checker12516 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12515) <= 0) {
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