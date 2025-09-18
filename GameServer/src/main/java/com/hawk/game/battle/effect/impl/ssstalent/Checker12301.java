package com.hawk.game.battle.effect.impl.ssstalent;

import com.hawk.game.battle.effect.BattleConst;
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
 * 
【12301】
- 【万分比】【12301】出征或驻防时，采矿车生命 +XX.XX%（在集结战时，此数值翻倍）
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 此加成为常规外围属性加成效果；即
    - 实际生命 = 基础生命*（1 + 各类加成 +【本作用值】）
  - 个人战时（己方无队友玩家参战）实际数值即为作用号数值
  - 集结战时（己方存在队友玩家参战）
    - 实际数值 = 【本作用值】*修正系数/10000
  - 修正系数读取const表，字段effect12301AdjustForMass
    - 配置格式：万分比
 */
@BattleTupleType(tuple = Type.HP)
@EffectChecker(effType = EffType.EFF_12301)
public class Checker12301 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.CANNON_SOLDIER_8) {
			effPer = parames.unity.getEffVal(effType());
			if (BattleConst.WarEff.MASS.check(parames.troopEffType)) {
				effPer = (int) (effPer * GsConst.EFF_PER * ConstProperty.getInstance().getEffect12301AdjustForMass());
			}
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
