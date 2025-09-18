package com.hawk.game.battle.effect.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import com.hawk.game.protocol.Const.SoldierType;

@BattleTupleType(tuple = Type.ATK)
@EffectChecker(effType = EffType.ARMOUR_11005)
public class Checker11005 implements IChecker {
	/**
	 * 11005	万分比	直升机（兵种类型 = 4）战斗开始时，增加己方所有空军（兵种类型 = 4或3）部队XX%的攻击加成（至多叠加X层）	实际攻击 = 基础攻击 *（1 + 其他作用号 + 本作用值/10000）	"每个玩家拥有此作用号只会生效1层
	个人和集结战斗都生效，对战斗中所有玩家生效
	至多叠加层数读取const表，字段effect11005TimesLimit"
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.PLANE_SOLDIER_3 || parames.type == SoldierType.PLANE_SOLDIER_4) {
			Object object = parames.getLeaderExtryParam(getSimpleName());
			if (Objects.nonNull(object)) {
				effPer = (int) object;
			} else {
				Set<String> set = new HashSet<>();
				List<Integer> effList = new LinkedList<>();
				for (BattleUnity unit : parames.unityList) {
					String playerId = unit.getPlayer().getId();
					if (set.contains(playerId)) {
						continue;
					}
					set.add(playerId);
					if (parames.getPlayerArmyCount(playerId, SoldierType.PLANE_SOLDIER_4) > 0) {
						int val = unit.getEffVal(effType());
						if (val > 0) {
							effList.add(val);
						}
					}
				}

				effPer = effList.stream()
						.sorted(Comparator.comparingInt(Integer::intValue).reversed())
						.limit(ConstProperty.getInstance().getEffect11005TimesLimit())
						.mapToInt(Integer::intValue)
						.sum();

				parames.putLeaderExtryParam(getSimpleName(), effPer);

			}
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
