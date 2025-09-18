package com.hawk.game.battle.effect.impl.plant45;

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
 * 【84501】采矿车
- 采矿车（兵种类型 = 8），新增技能id = 84501
- 技能效果：集结战斗开始前，自身出征数量多的采矿车为己方全体近战部队提供生命加成+XX%（至多有2个采矿车单位生效）
  - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
  - 满足条件后，该作用号效果仅玩家出征时数量最多的采矿车（兵种类型 = 8）生效（若存在多个采矿车数量一样且最高，取等级高的）
  - 近战单位类型包含有：防御坦克（兵种类型 = 1）、主战坦克（兵种类型 = 2）、轰炸机（兵种类型 = 3）、采矿车（兵种类型 = 8）
  - 此为常规外围属性加成效果，与其他作用号累加计算
    - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】）
  - 相关技能参数读取battle_soldier_skill表，对应参数效果如下
    - trigger：触发概率，这里无意义（填上10000）
      - 配置格式：万分比
    - p1：友方兵种类型
      - 配置格式：兵种类型1_兵种类型2_......
    - p2：单次增加生命加成数值
      - 配置格式：万分比
    - p3：生效采矿车数量
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.HP)
@EffectChecker(effType = EffType.EFF_SOLDIER_SKILL_84501)
public class CheckerSkill84501 implements IChecker {

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

		BattleSoldierSkillCfg scfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierSkillCfg.class, 84501);
		if (scfg == null) {
			return CheckerKVResult.DefaultVal;
		}

		int maxUnit = NumberUtils.toInt(scfg.getP3());
		List<BattleSoldier> tanktopList = parames.unitStatic.getPlantUnityStatistics().getPlayerSoldierTable().column(SoldierType.CANNON_SOLDIER_8).values().stream()
				.map(list -> list.get(0).getSolider())
				.filter(s -> s.hasSkill(PBSoldierSkill.SOLDIER_SKILL_84501))
				.sorted(Comparator.comparingInt((BattleSoldier s) -> s.getSkill(PBSoldierSkill.SOLDIER_SKILL_84501).getP2IntVal())
						.thenComparingInt(BattleSoldier::getFreeCnt)
						.reversed())
				.limit(maxUnit)
				.collect(Collectors.toList());

		for (BattleSoldier tank1 : tanktopList) {
			scfg = tank1.getSkill(PBSoldierSkill.SOLDIER_SKILL_84501);
			parames.solider.addDebugLog("{} 全体近战部队提供生命加成  + {}", tank1.getUUID(), scfg.getP2IntVal());
			effPer += scfg.getP2IntVal();
		}

		parames.putLeaderExtryParam(getSimpleName(), effPer);
		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}