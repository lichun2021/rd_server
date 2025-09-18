package com.hawk.game.battle.effect.impl;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
/**
 * 
【1665】
作用号效果 ：所有部队（包含集结中友方士兵）防御增加
【1665】是万分比数值
部队防御加成 = 1+其他防御百分比加成+【1665】
当集结部队中，有多个【1665】存在时，多个【1665】效果叠加，效果上限由const表 effect1665Maxinum控制
填6000 即上限为 60%
 * @author lwt
 * @date 2023年1月10日
 */
@BattleTupleType(tuple = Type.DEF)
@EffectChecker(effType = EffType.HERO_1665)
public class Checker1665 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (isSoldier(parames.type)) {

			Object object = parames.getLeaderExtryParam(getSimpleName());
			if (Objects.nonNull(object)) {
				effPer = (int) object;
			} else {
				Set<String> set = new HashSet<>();
				for (BattleUnity unit : parames.unityList) {
					String pid = unit.getPlayer().getId();
					if (set.contains(pid)) {
						continue;
					}
					set.add(pid);
					int val = unit.getEffVal(effType());
					effPer += val;
				}
				effPer = Math.min(ConstProperty.getInstance().getEffect1665Maxinum(), effPer);

				parames.putLeaderExtryParam(getSimpleName(), effPer);
			}

		}

		return new CheckerKVResult(effPer, effNum);
	}
}