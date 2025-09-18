package com.hawk.game.battle.effect.impl.heroJiaLaNi12498508;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
 - 【万分比】【12015】集结战斗开始前，若自身出征携带不低于集结部队总数 5% 且 超过自身部队总数 50% 的攻城车，自身出征数量最多的攻城车在本场战斗中获得如下效果：每第 5 回合，额外向敌方随机 6 个单位进行 2 轮攻击（伤害率XX.XX%，每轮攻击独立选择目标），命中处于【点燃状态】的目标时伤害额外 +XX.XX%，发动此效果后 3 个回合内，无法释放以敌方为目标的攻击、技能等各类效果；（多个杰拉尼同时存在时，至多有 2 个攻城车单位获得上述效果）
  - 战报相关
    - 于战报中隐藏
    - 不合并至精简战报中
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 在战斗开始前判定，满足条件后本次战斗全程生效
  - 集结战斗开始前，若自身出征携带不低于集结部队总数 5% 的攻城车
    - 数量1 = 某玩家出征携带的攻城车（兵种类型 = 7）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 集结部队的出征数量总和
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 >= 指定数值时生效
      - 指定数值读取const表，字段effect12015AllNumLimit
        - 配置格式：万分比
  - 集结战斗开始前，若自身携带超过自身部队总数 50% 的攻城车
    - 数量1 = 某玩家出征携带的攻城车（兵种类型 = 7）数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量2 = 某玩家出征数量
      - 真实出征部队数量，在战斗开始前判定，即所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
    - 数量1/数量2 > 指定数量时生效
      - 指定数值读取const表，字段effect12015SelfNumLimit
        - 配置格式：万分比
  - 自身出征数量最多的攻城车在本场战斗中获得如下效果
    - 满足条件后，该作用号效果仅对玩家出征时数量最多的攻城车（兵种类型 = 7）生效（若存在多个攻城车数量一样且最高，取等级高的）
    - 注：若存在荣耀所罗门的幻影部队，此作用号对幻影部队也生效
  - 每第 5 回合，额外向敌方随机 6 个单位进行 2 轮攻击（伤害率XX.XX%，每轮攻击独立选择目标），命中处于【点燃状态】的目标时伤害额外 +XX.XX%，发动此效果后 3 个回合内，无法释放以敌方为目标的攻击、技能等各类效果
    - 每第 5 回合
      - 战斗中每第 5 的倍数的回合开始后，自身开始普攻前，额外进行2次攻击（可以理解为释放2次技能效果）
        - 注：此攻击效果为额外攻击效果，无法触发英雄茉碧乌丝的专属芯片-燃烧火箭弹（作用号【1540】）的效果
        - 注：此效果于自身开始普攻前即可触发（即在攻城车无法行动的回合时，也能触发此效果）
      - 指定回合数读取const表，字段effect12015AtkRound
        - 配置格式：绝对值
      - 攻击次数读取const表，字段effect12015AtkTimes
        - 配置格式：绝对值
    - 敌方随机 6 个单位
      - 纯随机；不足 6 个则有多少取多少
      - 指定敌方数量读取const表，字段effect12015AtkNum
        - 配置格式：绝对值
    - 伤害率（XX.XX%）
      - 即 实际伤害 = 本次伤害率 * 基础伤害
    - 发动此效果后 3 个回合内，无法释放以敌方为目标的攻击、技能等各类效果
      - 下回合开始到下回合结束算 1 回合，不影响本回合的各类攻击、技能效果
      - 持续回合数读取const表，字段effect12015ContinueRound
        - 配置格式：绝对值
  - （多个杰拉尼同时存在时，至多有 2 个攻城车单位获得上述效果）
    - 效果记录在己方部队身上，限制该作用号生效层数上限
      - 注：这里限制的是生效层数上限，若集结中层数超出此上限，取作用号数值高的；即这里只能有 2 个玩家携带的作用号生效
      - 层数上限读取const表，字段effect12015Maxinum
        - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.HERO_12015)
public class Checker12015 implements IChecker {
	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!BattleConst.WarEff.MASS.check(parames.troopEffType) || parames.type != SoldierType.CANNON_SOLDIER_7) {
			return CheckerKVResult.DefaultVal;
		}

		if (parames.unity != parames.getPlayerMaxFreeArmy(parames.unity.getPlayerId(), SoldierType.CANNON_SOLDIER_7)) {
			return CheckerKVResult.DefaultVal;
		}

		Map<String, Integer> effPlayerVal = selectPlayer(parames);
		if (!effPlayerVal.containsKey(parames.unity.getPlayerId())) {
			return CheckerKVResult.DefaultVal;
		}

		int effPer = effPlayerVal.get(parames.unity.getPlayerId());
		parames.unity.getSolider().addDebugLog("-Jelani 自身出征数量最多的攻城车 {}  获得 12015 {}", parames.unity.getSolider().getUUID(), effPer);
		return new CheckerKVResult(effPer, 0);
	}

	/**数值最高的玩家*/
	private Map<String, Integer> selectPlayer(CheckerParames parames) {
		Map<String, Integer> valMap = new HashMap<>();
		for (BattleUnity unity : parames.unityList) {
			if (valMap.containsKey(unity.getPlayer())) {
				continue;
			}
			int effvalue = effvalue(unity, parames);
			valMap.put(unity.getPlayerId(), effvalue);
		}

		valMap = valMap.entrySet().stream()
				.sorted(((item1, item2) -> {
					int compare = item2.getValue().compareTo(item1.getValue());
					return compare * -1;
				}))
				.filter(ent -> ent.getValue() > 0)
				.limit(ConstProperty.getInstance().getEffect12015Maxinum())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return valMap;
	}

	private int effvalue(BattleUnity unity, CheckerParames parames) {
		try {
			if (unity.getEffVal(effType()) == 0) {
				return 0;
			}
			String playerId = unity.getPlayerId();
			int effPer = 0;
			// 采矿车数量
			int march8cnt = parames.unitStatic.getPlayerSoldierCountMarch().get(playerId, SoldierType.CANNON_SOLDIER_7);
			// 若自身出征携带不低于集结部队总数 5% 的采矿车
			if (march8cnt / parames.unitStatic.getTotalCountMarch() >= ConstProperty.getInstance().getEffect12015AllNumLimit() * GsConst.EFF_PER) {
				// 若自身携带超过自身部队总数 50% 的采矿车
				if (march8cnt * 1D / parames.unitStatic.getPlayerArmyCountMapMarch().get(playerId) > ConstProperty.getInstance().getEffect12015SelfNumLimit() * GsConst.EFF_PER) {
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
