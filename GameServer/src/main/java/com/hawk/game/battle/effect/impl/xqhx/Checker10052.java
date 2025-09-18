package com.hawk.game.battle.effect.impl.xqhx;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.module.lianmengXianquhx.XQHXConst.XQHXState;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;
/**
 * 【10052】
【万分比】【10052】于先驱回响的核心建筑内战斗时，部队基础攻击增加XX.XX%
- 于先驱回响的核心建筑内战斗时
  - 核心建筑指此副本内，建筑类型为核心建筑的（下方五角星标识建筑），后续会有配置表标记核心建筑的类型id，以id作为识别	
 * @author lwt
 * @date 2025年3月21日
 */

@BattleTupleType(tuple = Type.ATK_BASE)
@EffectChecker(effType = EffType.XQHX_10052)
public class Checker10052 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.unity.getPlayer().getXQHXState() == XQHXState.GAMEING && isSoldier(parames.type)) {
			effPer = (int) (parames.unity.getEffVal(effType()) * GsConst.EFF_PER* ConstProperty.getInstance().effect10052SoldierAdjustMap.getOrDefault(parames.type, 10000));
		}

		return new CheckerKVResult(effPer, effNum);
	}
	
	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
