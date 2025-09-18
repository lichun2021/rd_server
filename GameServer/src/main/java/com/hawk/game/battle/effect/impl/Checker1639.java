package com.hawk.game.battle.effect.impl;

import org.hawk.tuple.HawkTuple3;

import com.hawk.game.battle.BattleSoldier_8;
import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_1639)
public class Checker1639 implements IChecker {
	// •雷达干扰：出征或集结时，如果采矿车超过部队总数的 5 % ，每4回合（战斗前敌方目标每满10个目标，干扰冷却回合-1，最低2回合）
	// • 会选择战场近战装甲最薄弱的目标（防御值越低的近战兵种概率越高），进行雷达干扰，被干扰的目标，下回合无法攻击
	// • （卡洛琳分摊伤害，巴克援护等被动触发的效果不受影响）且减少 42.48% 的部队防御加成，来自不同指挥官的帕斯卡，能够干扰不同目标（来自同一指挥官的多排采矿车只有数量最多的一排才能释放干扰），
	// • 为保证干扰指令波段正常，同一回合敌方最多有 3 个目标会被干扰打乱进攻。
	// •视线干扰：当指挥官派遣帕斯卡与帕克一同出征时，帕克削弱敌军生命和防御加成所需要采矿车数量减少 5 % 。
	// 【1639】
	// 触发回合间隔 ：
	// 1.基础4个回合一次，
	// 2.根据战前敌方排数，每满10个 -1回合，即敌方10排，为3回合，敌方20排，为2回合，
	// 3.最小值减至2个回合
	// 4.由effect1639Parametric 4_10_2 来表示上边三个条件
	// 触发逻辑：
	// 自身最多的单排采矿车（兵种类型 =8）士兵数量>= 部队总量 的X% X由const.xml effect1639Per控制 填500 = 5%
	// 效果：本排按照 原狙击技能：60501 的p3 选取目标逻辑一样，根据配置各个兵种的被选中的权重，
	// 被选中的概率 = 自身所属兵种权重/ 总权重池
	//
	// 效果逻辑：
	// 选取被眩晕的目标 effect1639SoldierPer控制
	// 例：1_5,2_15,3_75,8_5 即只有1排轰炸，主战，防御，采矿车，轰炸被选中概率75%，主战15%，防坦，采矿车为5%
	//
	// 其他情况
	// 多人携带【1639】第二个，会把第一个被眩晕的目标排除后，进行权重选取计算，依次论推
	// 最多同一个回合内可最多可标记眩晕X个目标 X由const.xml effect1639Maxinum控制 填3 即最多3个
	// 被眩晕的目标，下回合无法进行任何出手操作（巴克的援护，卡洛琳的分摊可被触发），同时降低防御力加成
	//
	// 目标的防御加成 = 1+其他防御加成 - 【1639】

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (parames.type != SoldierType.CANNON_SOLDIER_8) {
			return CheckerKVResult.DefaultVal;
		}
		effPer = parames.unity.getEffVal(effType());
		if (effPer == 0) {
			return CheckerKVResult.DefaultVal;
		}

		String playerId = parames.unity.getArmyInfo().getPlayerId();

		double footCount = parames.getPlayerArmyCount(playerId, SoldierType.CANNON_SOLDIER_8);
		double per = ConstProperty.getInstance().getEffect1639Per() * GsConst.EFF_PER;
		if (footCount / parames.totalCount < per) {
			return CheckerKVResult.DefaultVal;
		}

		BattleUnity maxUnity = parames.getPlayerMaxFreeArmy(playerId, SoldierType.CANNON_SOLDIER_8);
		if (maxUnity == parames.unity) {
			HawkTuple3<Integer, Integer, Integer> effect1639ParametricNum = ConstProperty.getInstance().getEffect1639ParametricNum();
			int round1639 = effect1639ParametricNum.first;
			round1639 = round1639 - parames.tarStatic.getUnityList().size() / effect1639ParametricNum.second;
			round1639 = Math.max(round1639, effect1639ParametricNum.third);

			((BattleSoldier_8) parames.solider).setRound1639(round1639);
		}

		return new CheckerKVResult(effPer, effNum);
	}
}
