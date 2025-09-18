package com.hawk.game.battle.effect.impl.plant45;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
 * 【74501】攻城车
- 攻城车（兵种类型 = 7），新增技能id = 74501
- 技能效果：集结战斗时，自身出征数最多的攻城车攻击每命中敌方部队1次后，己方全体远程部队生命+XX%（该效果可加，至多X层，至多有2个攻城车单位生效）
  - 真实出征部队数量，在战斗开始前判定，即参谋军威技能、所罗门、吉迪恩 这种战斗中改变部队数量的机制对此无影响
  - 满足条件后，该作用号效果仅玩家出征时数量最多的攻城车（兵种类型 = 7）生效（若存在多个攻城车数量一样且最高，取等级高的）
  - 远程单位类型包含有：直升机（兵种类型 = 4）、突击步兵（兵种类型 = 5）、狙击兵（兵种类型 = 6）、攻城车（兵种类型 = 7）
  - 此为常规外围属性加成效果，与其他作用号累加计算
    - 即 实际属性 = 基础属性*（1 + 各类加成 +【本作用值】）
  - 相关技能参数读取battle_soldier_skill表，对应参数效果如下
    - trigger：触发概率，这里无意义（填上10000）
      - 配置格式：万分比
    - p1：攻城车攻击次数 生效指定数值
      - 配置格式：绝对值
    - p2：友方兵种类型
      - 配置格式：兵种类型1_兵种类型2_......
    - p2：单次增加生命加成数值
      - 配置格式：万分比
    - p4：该效果最多叠加层数
      - 配置格式：绝对值
    - p5：生效攻城车数量
      - 配置格式：绝对值
 */
@BattleTupleType(tuple = Type.SOLDIER_SKILL)
@EffectChecker(effType = EffType.EFF_SOLDIER_SKILL_74501)
public class CheckerSkill74501 implements IChecker {

	@Override
	public CheckerKVResult value(CheckerParames parames) {
		int effPer = 0;
		int effNum = 0;
		if (!BattleConst.WarEff.MASS.check(parames.troopEffType) || parames.type != SoldierType.CANNON_SOLDIER_7) {
			return CheckerKVResult.DefaultVal;
		}

		BattleSoldierSkillCfg scfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierSkillCfg.class, 74501);
		if (scfg == null) {
			return CheckerKVResult.DefaultVal;
		}

		int maxUnit = scfg.getP5IntVal();
		List<BattleSoldier> tanktopList = parames.unitStatic.getPlantUnityStatistics().getPlayerSoldierTable().column(SoldierType.CANNON_SOLDIER_7).values().stream()
				.map(list -> list.get(0).getSolider())
				.filter(s -> s.hasSkill(PBSoldierSkill.SOLDIER_SKILL_74501))
				.sorted(Comparator.comparingInt(BattleSoldier::getFreeCnt)
						.reversed())
				.limit(maxUnit)
				.collect(Collectors.toList());

		if (tanktopList.contains(parames.solider)) {
			scfg = parames.solider.getSkill(PBSoldierSkill.SOLDIER_SKILL_74501);
			effPer = scfg.getP3IntVal();
			parames.solider.addDebugLog("{} 自身出征数最多的攻城车攻击每命中敌方部队1次后，己方全体远程部队生命+XX% {}", parames.solider.getUUID(), scfg.getP3IntVal());
		}

		return new CheckerKVResult(effPer, effNum);
	}

	@Override
	public boolean tarTypeSensitive() {
		return false;
	}
}