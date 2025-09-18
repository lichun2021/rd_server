package com.hawk.game.battle.effect.impl.skill44;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.BattleSoldier_7;
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
 * 【攻城车】【744】
- 【攻城车】【泰能火力掩护】：集结战斗时，自身出征数量最多的攻城车攻击命中敌方部队时，降低其攻击、防御、生命 +XX.XX%（持续 2 回合，至多有 2 个攻城车单位生效）
  - 仅对集结战斗生效（包含集结进攻和集结防守）
  - 自身出征数量最多的攻城车
    - 真实出征部队数量，在战斗开始前判定，即参谋技军威、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
  - 该效果不可叠加，先到先得，持续回合结束后消失
  - 该技能为常规属性外围削减效果，即： 实际属性 = 基础属性*（1 + 其他加成 -【本技能值】）
  - （至多有 2 个攻城车单位生效）
    - 集结战斗时，若己方存在多个此技能，限制其生效个数上限（若超出此上限，优先取数值高的）；即这里只能有 2 个玩家携带的攻城车技能生效
  - 对应技能参数如下
    - trigger：无意义
    - p1：攻击、防御、生命削减数值
      - 配置格式：万分比
    - p2：持续回合数
      - 配置格式：绝对值
      - 注：由被附加开始到当前回合结算，算作 1 回合
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.PLANT_SOLDIER_SKILL_744)
public class CheckerSkill744 implements IChecker {

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
		List<BattleSoldier> tanktopList = parames.unitStatic.getPlantUnityStatistics().getPlayerSoldierTable().column(SoldierType.CANNON_SOLDIER_7).values().stream()
				.map(list -> list.get(0).getSolider())
				.filter(s -> s.hasSkill(PBSoldierSkill.CANNON_SOLDIER_7_SKILL_44))
				.sorted(Comparator.comparingInt((BattleSoldier s) -> s.getSkill(PBSoldierSkill.CANNON_SOLDIER_7_SKILL_44).getP1IntVal())
						.thenComparingInt(BattleSoldier::getFreeCnt)
						.reversed())
				.limit(maxUnit)
				.collect(Collectors.toList());

		for (BattleSoldier tank1 : tanktopList) {
			scfg = tank1.getSkill(PBSoldierSkill.CANNON_SOLDIER_7_SKILL_44);
			parames.solider.addDebugLog("{} 攻城车攻击命中敌方部队时，降低其攻击、防御、生命 + {}", tank1.getUUID(), scfg.getP1IntVal());
			((BattleSoldier_7) tank1).setTriggerSkill744(true);
		}

		return CheckerKVResult.DefaultVal;
	}
}