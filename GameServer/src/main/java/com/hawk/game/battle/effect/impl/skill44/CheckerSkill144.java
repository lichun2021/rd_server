package com.hawk.game.battle.effect.impl.skill44;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.BattleSoldier_1;
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
 * 防御坦克】【144】
- 【防御坦克】【泰能坦克庇护】：集结战斗时，自身出征数量最多的防御坦克每受到1次攻击后，己方全体坦克部队防御、生命 +XX.XX%（至多叠加 X 层，至多有 2 个防御坦克单位生效）
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 自身出征数量最多的防御坦克
    - 真实出征部队数量，在战斗开始前判定，即参谋技军威、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
  - 该技能为【光环】效果，对己方全体坦克部队生效
    - 坦克部队类型：主战坦克（兵种类型 = 2）和防御坦克（兵种类型 = 1）
  - 该技能为常规属性外围加成效果，即： 实际属性 = 基础属性*（1 + 其他加成 +【本技能值】）
  - （至多叠加 X 层，至多有 2 个防御坦克单位生效）
    - 层数记录在己方坦克部队身上，这里限制层数上限
    - 另外集结战斗时，若己方存在多个此技能，限制其生效个数上限（若超出此上限，优先取数值高的）；即这里只能有 2 个玩家携带的防御坦克技能生效
  - 对应技能参数如下
    - trigger：无意义
    - p1：单次加成防御和生命值
      - 配置格式：万分比
    - p2：叠加层数上限
      - 配置格式：绝对值
    - p3：生效单位上限
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.PLANT_SOLDIER_SKILL_144)
public class CheckerSkill144 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		if (!BattleConst.WarEff.MASS.check(parames.troopEffType)) {
			return CheckerKVResult.DefaultVal;
		}
		Object object = parames.getLeaderExtryParam(getSimpleName());
		if (Objects.nonNull(object)) {
			return CheckerKVResult.DefaultVal;
		}

		parames.putLeaderExtryParam(getSimpleName(), true);

		BattleSoldierSkillCfg scfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierSkillCfg.class, 14401);
		if (scfg == null) {
			return CheckerKVResult.DefaultVal;
		}

		int maxUnit = NumberUtils.toInt(scfg.getP3());
		List<BattleSoldier> tanktopList = parames.unitStatic.getPlantUnityStatistics().getPlayerSoldierTable().column(SoldierType.TANK_SOLDIER_1).values().stream()
				.map(list -> list.get(0).getSolider())
				.filter(s -> s.hasSkill(PBSoldierSkill.TANK_SOLDIER_1_SKILL_44))
				.sorted(Comparator.comparingInt((BattleSoldier s) -> s.getSkill(PBSoldierSkill.TANK_SOLDIER_1_SKILL_44).getP1IntVal())
						.thenComparingInt(BattleSoldier::getFreeCnt)
						.reversed())
				.limit(maxUnit)
				.collect(Collectors.toList());

		for (BattleSoldier tank1 : tanktopList) {
			scfg = tank1.getSkill(PBSoldierSkill.TANK_SOLDIER_1_SKILL_44);
			parames.solider.addDebugLog("{} 每受到1次攻击后，己方全体坦克部队防御、生命 + {}", tank1.getUUID(), scfg.getP1IntVal());
			((BattleSoldier_1) tank1).setTriggerSkill144(true);
		}

		return CheckerKVResult.DefaultVal;
	}
}