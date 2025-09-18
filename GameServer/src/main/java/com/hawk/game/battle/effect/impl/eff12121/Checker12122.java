package com.hawk.game.battle.effect.impl.eff12121;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

@BattleTupleType(tuple = Type.REDUCE_HURT_PCT)
@EffectChecker(effType = EffType.EFF_12122)
public class Checker12122 implements IChecker {
	/**
	 【12122】
	- 【万分比】【12122】任命在战备参谋部时，集结战斗开始前，若集结全队出征采矿车数量不低于集结部队总数 10%，己方全体近战部队受到攻击时，伤害减少 【固定数值+XX.XX%】（多个蒂娜同时存在时，上述效果可叠加，至多 10 层）
	- 战报相关
	- 于战报中展示
	- 不合并至精简战报中
	- 仅对集结战斗生效（包含集结进攻和集结防守）
	- 在战斗开始前判定，满足条件后本次战斗全程生效
	- 集结战斗开始前，若集结全队出征采矿车数量不低于集结部队总数 10%
	- 数量1 = 集结全队出征携带的采矿车（兵种类型 = 8）数量
	  - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
	- 数量2 = 集结部队的出征数量总和
	  - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
	- 数量1/数量2 >= 指定数值时生效
	  - 指定数值读取const表，字段effect12122AllNumLimit
	    - 配置格式：万分比
	- 己方全体近战部队受到攻击时，伤害减少 【固定数值+XX.XX%】
	- 该作用号数值效果为【光环】效果，生效到己方全体近战部队上
	  - 近战部队类型包含有：主战坦克（兵种类型 = 2）、防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、轰炸机（兵种类型 = 3）
	- 单个玩家提供数值 = 该作用号固定数值 + 英雄参谋值*技能系数/10000
	  - 该作用号固定数值读取const表，字段effect12122BaseVaule
	    - 配置格式：万分比
	- 该作用号为伤害减少效果，与其他作用号累乘计算
	  - 即 实际伤害 = 基础伤害*（1 - 其他作用号）*（1 - 【本作用值】）
	- （多个蒂娜同时存在时，上述效果可叠加，至多 10 层）
	- 单玩家拥有此作用号时，只能提供 1 层效果；集结中存在多个此作用号时，可以叠加
	- 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
	  - 注：这里限制的是生效层数上限，若集结中层数超出此上限，取作用号数值高的；即这里只能有 10 个玩家携带的作用号生效
	  - 层数上限读取const表，字段effect12122Maxinum
	    - 配置格式：绝对值
	 */
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;

		if (!isJinzhan(parames.type) || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}
		
		Integer cacheV = (Integer) parames.getLeaderExtryParam(getSimpleName());
		if (cacheV != null) {
			effPer = cacheV.intValue();
			return new CheckerKVResult(effPer, effNum);
		}
		
		double count8 = parames.unitStatic.getArmyCountMapMarch().get(SoldierType.CANNON_SOLDIER_8);
		double total = parames.unitStatic.getTotalCountMarch();
		if (count8 / total >= ConstProperty.getInstance().getEffect12122AllNumLimit() * GsConst.EFF_PER) {
			Set<String> pset = new HashSet<>();
			List<Integer> vlist = new ArrayList<>();
			for (BattleUnity fu : parames.unityList) {
				if (pset.contains(fu.getPlayerId())) {
					continue;
				}
				pset.add(fu.getPlayerId());
				vlist.add(fu.getEffVal(effType()));
			}

			vlist.sort(Comparator.comparingInt(Integer::intValue).reversed());
			effPer = vlist.stream().mapToInt(Integer::intValue).limit(ConstProperty.getInstance().getEffect12122Maxinum()).sum();
		}
		parames.putLeaderExtryParam(getSimpleName(), effPer);
		return new CheckerKVResult(effPer, effNum);
	}
}
