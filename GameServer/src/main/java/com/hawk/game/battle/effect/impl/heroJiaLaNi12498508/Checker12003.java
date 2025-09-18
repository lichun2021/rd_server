package com.hawk.game.battle.effect.impl.heroJiaLaNi12498508;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.hawk.os.HawkException;

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

/**
 * #12498508 【0608版本】【新英雄】【SSS】【战车双将】杰拉尼制作 https://meego.feishu.cn/ccredalert/story/detail/12498508
 * 
 - 【万分比】【12001】近战支援：集结战斗开始前，若自身出征携带不低于集结部队总数 5% 且 超过自身部队总数 50% 的采矿车，全体近战部队攻击 +XX.XX%；（多个杰拉尼同时存在时，上述效果可叠加，至多 2 层）
- 【万分比】【12002】近战支援：集结战斗开始前，若自身出征携带不低于集结部队总数 5% 且 超过自身部队总数 50% 的采矿车，全体近战部队防御 +XX.XX%；（多个杰拉尼同时存在时，上述效果可叠加，至多 2 层）
- 【万分比】【12003】近战支援：集结战斗开始前，若自身出征携带不低于集结部队总数 5% 且 超过自身部队总数 50% 的采矿车，全体近战部队生命 +XX.XX%；（多个杰拉尼同时存在时，上述效果可叠加，至多 2 层）
  - 上述3个作用号机制完全一致，加的属性类别不一样，分3个作用号开发；采矿车兵种类型 = 8
  - 战报相关
    - 于战报中展示
    - 合并至精简战报中
  - 该作用号仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 集结战斗开始前，若自身出征携带不低于集结部队总数 5% 的采矿车
    - 数量1 = 某玩家出征携带的采矿车（兵种类型 = 8）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12001AllNumLimit、effect12002AllNumLimit、effect12003AllNumLimit
        - 配置格式：万分比
  - 集结战斗开始前，若自身携带超过自身部队总数 50% 的采矿车
    - 数量1 = 某玩家出征携带的采矿车（兵种类型 = 8）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12001SelfNumLimit、effect12002SelfNumLimit、effect12003SelfNumLimit
        - 配置格式：万分比
  - 全体近战部队攻击/防御/生命 +XX.XX%；（多个杰拉尼同时存在时，上述效果可叠加，至多 2 层）
    - 单玩家拥有此作用号时，只能提供 1 层效果；集结中存在多个此作用号时，可以叠加
    - 全体近战部队
      - 此为【光环效果】，集结战斗中所有己方的近战部队都有此效果加成
      - 近战部队类型包含有：主战坦克（兵种类型 = 2）、防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、轰炸机（兵种类型 = 3）
    - 效果记录在己方部队身上，数值直接叠加；限制该作用号生效层数上限
      - 注：这里限制的是生效层数上限，若集结中层数超出此上限，取作用号数值高的
      - 层数上限读取const表，字段effect12001Maxinum、effect12002Maxinum、effect12003Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.HP)
@EffectChecker(effType = EffType.HERO_12003)
public class Checker12003 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (isYuanCheng(parames.type) || !BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}

		Integer cacheVal = (Integer) parames.getLeaderExtryParam(getSimpleName());
		if (Objects.nonNull(cacheVal)) {
			return new CheckerKVResult(cacheVal.intValue(), 0);
		}

		Map<String, Integer> valMap = new HashMap<>();
		for (BattleUnity unity : parames.unityList) {
			if (valMap.containsKey(unity.getPlayer())) {
				continue;
			}
			valMap.put(unity.getPlayerId(), effvalue(unity, parames));
		}

		int effPer = valMap.values().stream()
				.sorted(Comparator.comparingInt(Integer::intValue).reversed())
				.limit(ConstProperty.getInstance().getEffect12003Maxinum())
				.mapToInt(Integer::intValue)
				.sum();

		parames.putLeaderExtryParam(getSimpleName(), effPer);

		return new CheckerKVResult(effPer, 0);
	}

	private int effvalue(BattleUnity unity, CheckerParames parames) {
		try {
			if (unity.getEffVal(effType()) == 0) {
				return 0;
			}
			String playerId = unity.getPlayer().getId();
			int effPer = 0;
			// 采矿车数量
			int march8cnt = parames.unitStatic.getPlayerSoldierCountMarch().get(playerId, SoldierType.CANNON_SOLDIER_8);
			// 若自身出征携带不低于集结部队总数 5% 的采矿车
			if (march8cnt / parames.unitStatic.getTotalCountMarch() >= ConstProperty.getInstance().getEffect12003AllNumLimit() * GsConst.EFF_PER) {
				// 若自身携带超过自身部队总数 50% 的采矿车
				if (march8cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > ConstProperty.getInstance().getEffect12003SelfNumLimit() * GsConst.EFF_PER) {
					effPer = unity.getEffVal(effType());
				}
			}
			return effPer;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}
