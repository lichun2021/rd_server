package com.hawk.game.battle.effect.impl.skill44;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.battle.effect.BattleTupleType;
import com.hawk.game.battle.effect.BattleTupleType.Type;
import com.hawk.game.battle.effect.CheckerKVResult;
import com.hawk.game.battle.effect.CheckerParames;
import com.hawk.game.battle.effect.EffectChecker;
import com.hawk.game.battle.effect.IChecker;
import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 【采矿车】【844】
- 【采矿车】【泰能近战护卫】：集结战斗开始前，自身出征数量最多的采矿车为己方全体近战部队提供防御加成 +XX.XX%（至多有 2 个采矿车单位生效）
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 自身出征数量最多的采矿车
    - 真实出征部队数量，在战斗开始前判定，即参谋技军威、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
  - 该技能为常规防御外围加成效果，即 实际防御 = 基础防御*（1 + 其他加成 +【本技能值】）
  - 该技能为【光环】效果，对己方全体近战部队生效
    - 近战部队类型：主战坦克（兵种类型 = 2）、防御坦克（兵种类型 = 1）、采矿车（兵种类型 = 8）、轰炸机（兵种类型 = 3）
  - （至多有 2 个采矿车单位生效）
    - 集结战斗时，若己方存在多个此技能，该加成数值可叠加，限制其生效个数上限（若超出此上限，优先取数值高的）；即这里只能有 2 个玩家携带的采矿车技能生效
  - 对应技能参数如下
    - trigger：无意义
    - p1：加成防御
      - 配置格式：万分比
    - p2：生效单位上限
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.DEF)
@EffectChecker(effType = EffType.PLANT_SOLDIER_SKILL_844)
public class CheckerSkill844 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (!BattleConst.WarEff.MASS.check(parames.troopEffType) || !isJinzhan(parames.type)) {
			return CheckerKVResult.DefaultVal;
		}
		Integer object = (Integer) parames.getLeaderExtryParam(getSimpleName());
		if (Objects.nonNull(object)) {
			return new CheckerKVResult(object, effNum);
		}

		BattleSoldierSkillCfg scfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierSkillCfg.class, 84401);
		if (scfg == null) {
			return CheckerKVResult.DefaultVal;
		}

		int maxUnit = NumberUtils.toInt(scfg.getP2());
		List<BattleSoldier> tanktopList = parames.unitStatic.getPlantUnityStatistics().getPlayerSoldierTable().column(SoldierType.CANNON_SOLDIER_8).values().stream()
				.map(list -> list.get(0).getSolider())
				.filter(s -> s.hasSkill(PBSoldierSkill.CANNON_SOLDIER_8_SKILL_44))
				.sorted(Comparator.comparingInt((BattleSoldier s) -> s.getSkill(PBSoldierSkill.CANNON_SOLDIER_8_SKILL_44).getP1IntVal())
						.thenComparingInt(BattleSoldier::getFreeCnt)
						.reversed())
				.limit(maxUnit)
				.collect(Collectors.toList());

		for (BattleSoldier tank1 : tanktopList) {
			scfg = tank1.getSkill(PBSoldierSkill.CANNON_SOLDIER_8_SKILL_44);
			parames.solider.addDebugLog("{} 为己方全体近战部队提供防御加成  + {}", tank1.getUUID(), scfg.getP1IntVal());
			effPer += scfg.getP1IntVal();
		}

		parames.putLeaderExtryParam(getSimpleName(), effPer);
		return new CheckerKVResult(effPer, effNum);
	}
}