package com.hawk.game.battle.effect.impl.eff5001;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.hawk.os.HawkException;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * - 战场内，自身携带XX机甲时，队伍中每存在一个解锁精神赋能的机甲，自身XX兵种超能攻击增加。
  - 5001：携带天霸（机甲ID：1011），防御坦克（兵种ID：1）。
  - 5002：携带黑武士（机甲ID：1007），主站坦克（兵种ID：2）。
  - 5003：携带深渊梦魇（机甲ID：1015），轰炸机（兵种ID：3）。
  - 5004：携带大天使（机甲ID：1009），直升机（兵种ID：4）。
  - 5005：携带阿努比斯（机甲ID：1012），突击步兵（兵种ID：5）。
  - 5006：携带极寒毁灭（机甲ID：1008），狙击兵（兵种ID：6）。
  - 5007：携带煞星（机甲ID：1013），攻城车（兵种ID：7）。
  - 5008：携带捍卫者（机甲ID：1014），采矿车（兵种ID：8）。
 * @author lwt
 * @date 2023年9月21日
 */
@BattleTupleType(tuple = Type.ATKFIRE)
@EffectChecker(effType = EffType.EFF_5001)
public class Checker5001 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		SoldierType needtype = SoldierType.TANK_SOLDIER_1;
		return jiSuan(parames, needtype);
	}

	protected CheckerKVResult jiSuan(CheckerParames parames, SoldierType needtype) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == needtype && parames.unity.getPlayer().isInDungeonMap()) {
			Integer cnt = (Integer) parames.getLeaderExtryParam(getSimpleName());
			if (cnt == null) {
				cnt = count(parames);
				parames.putLeaderExtryParam(getSimpleName(), cnt);
			}

			effPer = parames.unity.getEffVal(effType()) * Math.min(ConstProperty.getInstance().getEffect5001Cnt(), cnt);
		}
		return new CheckerKVResult(effPer, effNum);
	}

	protected Integer count(CheckerParames parames) {
		Set<String> all = new HashSet<>();
		Set<String> allOK = new HashSet<>();
		for (BattleUnity unit : parames.unityList) {
			try {
				if (all.contains(unit.getPlayerId())) {
					continue;
				}
				all.add(unit.getPlayerId());

				Optional<SuperSoldier> superSoldier = unit.getPlayer().getSuperSoldierByCfgId(unit.getEffectParams().getSuperSoliderId());
				if (superSoldier.isPresent() && superSoldier.get().getSoldierEnergy().getLevel() >= 150) {
					allOK.add(unit.getPlayerId());
				}

			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		return allOK.size();
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}

}
