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
 * 	//- 战场内，自身携带XX机甲时，自身XX兵种攻击增加。
 EFF_5011 = 5011;// - 5011：携带天霸（机甲ID：1011），防御坦克（兵种ID：1）。
 EFF_5012 = 5012;// - 5012：携带黑武士（机甲ID：1007），主站坦克（兵种ID：2）。
 EFF_5013 = 5013;// - 5013：携带深渊梦魇（机甲ID：1015），轰炸机（兵种ID：3）。
 EFF_5014 = 5014;// - 5014：携带大天使（机甲ID：1009），直升机（兵种ID：4）。
 EFF_5015 = 5015;// - 5015：携带阿努比斯（机甲ID：1012），突击步兵（兵种ID：5）。
 EFF_5016 = 5016;// - 5016：携带极寒毁灭（机甲ID：1008），狙击兵（兵种ID：6）。
 EFF_5017 = 5017;// - 5017：携带煞星（机甲ID：1013），攻城车（兵种ID：7）。
 EFF_5018 = 5018;// - 5018：携带捍卫者（机甲ID：1014），采矿车（兵种ID：8）。
//- 战场内，自身携带XX机甲时，自身XX兵种防御增加。
 EFF_5021 = 5021;// - 5021：携带天霸（机甲ID：1011），防御坦克（兵种ID：1）。
 EFF_5022 = 5022;// - 5022：携带黑武士（机甲ID：1007），主站坦克（兵种ID：2）。
 EFF_5023 = 5023;// - 5023：携带深渊梦魇（机甲ID：1015），轰炸机（兵种ID：3）。
 EFF_5024 = 5024;// - 5024：携带大天使（机甲ID：1009），直升机（兵种ID：4）。
 EFF_5025 = 5025;// - 5025：携带阿努比斯（机甲ID：1012），突击步兵（兵种ID：5）。
 EFF_5026 = 5026;// - 5026：携带极寒毁灭（机甲ID：1008），狙击兵（兵种ID：6）。
 EFF_5027 = 5027;// - 5027：携带煞星（机甲ID：1013），攻城车（兵种ID：7）。
 EFF_5028 = 5028;// - 5028：携带捍卫者（机甲ID：1014），采矿车（兵种ID：8）。
//- 战场内，自身携带XX机甲时，自身XX兵种生命增加。
 EFF_5031 = 5031;// - 5031：携带天霸（机甲ID：1011），防御坦克（兵种ID：1）。
 EFF_5032 = 5032;// - 5032：携带黑武士（机甲ID：1007），主站坦克（兵种ID：2）。
 EFF_5033 = 5033;// - 5033：携带深渊梦魇（机甲ID：1015），轰炸机（兵种ID：3）。
 EFF_5034 = 5034;// - 5034：携带大天使（机甲ID：1009），直升机（兵种ID：4）。
 EFF_5035 = 5035;// - 5035：携带阿努比斯（机甲ID：1012），突击步兵（兵种ID：5）。
 EFF_5036 = 5036;// - 5036：携带极寒毁灭（机甲ID：1008），狙击兵（兵种ID：6）。
 EFF_5037 = 5037;// - 5037：携带煞星（机甲ID：1013），攻城车（兵种ID：7）。
 EFF_5038 = 5038;// - 5038：携带捍卫者（机甲ID：1014），采矿车（兵种ID：8）。
//- 战场内，自身携带XX机甲时，自身XX兵种攻击/防御/生命增加。
 EFF_5041 = 5041;// - 5041：携带天霸（机甲ID：1011），防御坦克（兵种ID：1）。
 EFF_5042 = 5042;// - 5042：携带黑武士（机甲ID：1007），主站坦克（兵种ID：2）。
 EFF_5043 = 5043;// - 5043：携带深渊梦魇（机甲ID：1015），轰炸机（兵种ID：3）。
 EFF_5044 = 5044;// - 5044：携带大天使（机甲ID：1009），直升机（兵种ID：4）。
 EFF_5045 = 5045;// - 5045：携带阿努比斯（机甲ID：1012），突击步兵（兵种ID：5）。
 EFF_5046 = 5046;// - 5046：携带极寒毁灭（机甲ID：1008），狙击兵（兵种ID：6）。
 EFF_5047 = 5047;// - 5047：携带煞星（机甲ID：1013），攻城车（兵种ID：7）。
 EFF_5048 = 5048;// - 5048：携带捍卫者（机甲ID：1014），采矿车（兵种ID：8）。
 * @author lwt
 * @date 2023年9月21日
 */
@BattleTupleType(tuple = { Type.ATK, Type.DEF, Type.HP })
@EffectChecker(effType = EffType.EFF_5041)
public class Checker5041 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type == SoldierType.TANK_SOLDIER_1 && parames.unity.getPlayer().isInDungeonMap()) {
			effPer = parames.unity.getEffVal(effType());
		}
		return new CheckerKVResult(effPer, effNum);
	}
}
