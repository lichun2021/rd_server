package com.hawk.game.battle.effect.impl.eff5001;

import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
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
@EffectChecker(effType = EffType.EFF_5004)
public class Checker5004 extends Checker5001 {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		SoldierType needtype = SoldierType.PLANE_SOLDIER_4;
		return jiSuan(parames, needtype);
	}

}
