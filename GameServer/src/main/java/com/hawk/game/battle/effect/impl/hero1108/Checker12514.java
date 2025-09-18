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
【12514】
- 【万分比】【12514】自身出征数量最多的主战坦克在触发坦克对决时，本次攻击额外附加如下效果
  - 猎袭追击：当追加攻击没有可选取目标时，将会选取敌方随机单位进行攻击，优先选取近战单位作为目标，造成伤害为原本伤害的 XX.XX% 【12514】->针对敌方兵种留个内置修正系数(effect12514RoundWeight)
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 自身出征数量最多的主战坦克在触发坦克对决时
    - 触发条件同【12513】
  - 当追加攻击没有可选取目标时，将会选取敌方随机单位进行攻击，优先选取近战单位作为目标
    - 追加攻击的作用号只包含坦克对决【12131】、歼灭突袭【12513】和猎袭追击【12514】（自身）
      - 坦克对决【12131】或歼灭突袭【12513】未成功选择目标而导致攻击失效时，会重新选择敌方随机单位作为本次攻击的目标
      - 猎袭追击【12514】理论上不会无目标，但为保险此处写死为不能自身触发自身
    - 随机规则
      - 作用号生效后，优先在敌方近战单位中随机选择1个目标，若敌方无可选单位，则在敌方所有单位中随机选择1个目标
        - 近战部队类型包含有：防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、主战坦克（兵种类型 = 2）、轰炸机（兵种类型 = 3）
        - 近战单位中选择各兵种类型随机权重读取const表，字段effect12514RoundWeight
          - 配置格式：类型1_权重1,类型2_权重2,类型3_权重3,类型4_权重4
  - 造成伤害为原本伤害的 XX.XX% 
    - 即 实际伤害 = 原追加攻击对新目标的基础伤害*（1 + 各类加成）*（1 - 各类减免） *【本作用值】
      - 配置格式：万分比
 */

@BattleTupleType(tuple = Type.SOLDIER_SKILL2)
@EffectChecker(effType = EffType.HERO_12514)
public class Checker12514 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (parames.solider.getEffVal(EffType.HERO_12131) <= 0|| parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.TANK_SOLDIER_2)) {
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