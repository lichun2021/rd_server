package com.hawk.game.battle.effect.impl.hero1118;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.util.concurrent.AtomicLongMap;
import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.BattleSoldier_1;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

public class Hero1118 {
	private static final int CYZY = 1;
	private static final int JYBL = 2;

	private int xingTai = JYBL;
	private final AtomicLongMap<SoldierType> effect12784Nums = AtomicLongMap.create();
	private int effect12784AddFirePoint;
	private boolean triger12782;
	private final BattleSoldier_1 soldier;

	public Hero1118(BattleSoldier_1 soldier) {
		this.soldier = soldier;
	}

	public void roundStart() {
	}

	public void roundEnd() {
		int effVal = soldier.getEffVal(EffType.HERO_12781);
		if (!Hero1118Rules.shouldSwitchStance(
				effVal, soldier.getBattleRound(), ConstProperty.getInstance().effect12781AtkRound)) {
			return;
		}
		xingTai = Hero1118Rules.nextStance(xingTai);
		soldier.addDebugLog("[12781] {} stance switched to {}", soldier.getUUID(), xingTai);

		int round = soldier.getBattleRound();
		Map<SoldierType, Integer> killMap = new HashMap<>();
		for (BattleSoldier enemy : soldier.getTroop().getEnemyTroop().getSoldierList()) {
			if (!enemy.canBeAttack()) {
				continue;
			}
			int start = round - ConstProperty.getInstance().effect12784Round + 1;
			for (int i = start; i < round; i++) {
				killMap.merge(enemy.getType(), enemy.getRoundKill().getOrDefault(i, 0), (v1, v2) -> v1 + v2);
			}
		}
		List<Map.Entry<SoldierType, Integer>> entryList = new ArrayList<>(killMap.entrySet());
		Collections.sort(entryList, Comparator.comparing(Map.Entry::getValue));
		Collections.reverse(entryList);
		entryList.stream().limit(ConstProperty.getInstance().effect12784Nums)
				.forEach(entry -> effect12784Nums.incrementAndGet(entry.getKey()));
		effect12784AddFirePoint += ConstProperty.getInstance().effect12784AddFirePoint;
		soldier.addDebugLog("[12784] patterns {} layers {}", effect12784Nums, effect12784AddFirePoint);
	}

	public int buff12784reduceHurtValPct(BattleSoldier def) {
		if (!soldier.isAlive() || soldier.getEffVal(EffType.HERO_12784) <= 0) {
			return 0;
		}
		int baseValue = (int) (ConstProperty.getInstance().effect12784BaseVaule
				+ soldier.getEffVal(EffType.HERO_12784) * effect12784Nums.get(def.getType()));
		int result = Hero1118Rules.combinedEffectValue(baseValue, soldier.getEffVal(EffType.HERO_12803));
		soldier.addDebugLog("[12784] {} damage reduction {}", soldier.getUUID(), result);
		return result;
	}

	public int buff12782(BattleSoldier tar) {
		int effVal = Hero1118Rules.combinedEffectValue(
				soldier.getEffVal(EffType.HERO_12782), soldier.getEffVal(EffType.HERO_12801));
		effVal = (int) (effVal * GsConst.EFF_PER
				* ConstProperty.getInstance().effect12782SoldierAdjustMap.getOrDefault(tar.getType(), 10000));
		boolean activeStance = xingTai == CYZY
				|| effect12784AddFirePoint >= ConstProperty.getInstance().effect12785AtkThresholdValue4;
		if (soldier.isAlive() && effVal > 0 && activeStance
				&& soldier.getBattleRound() % ConstProperty.getInstance().effect12782AtkRound == 0) {
			triger12782 = true;
			soldier.addDebugLog("[12782] {} damage increase {}", soldier.getUUID(), effVal);
			return effVal;
		}
		triger12782 = false;
		return 0;
	}

	public double soulLink(BattleSoldier atkSoldier, double hurtVal) {
		boolean activeStance = xingTai == JYBL
				|| effect12784AddFirePoint >= ConstProperty.getInstance().effect12785AtkThresholdValue4;
		if (!activeStance) {
			return hurtVal;
		}
		int val = (int) (soldier.getEffVal(EffType.HERO_12783) * GsConst.EFF_PER
				* ConstProperty.getInstance().effect12783SoldierAdjustMap.getOrDefault(atkSoldier.getType(), 10000));
		if (val <= 0 || !soldier.isAlive()) {
			return hurtVal;
		}
		if (soldier.getEffVal(EffType.EFF_12081) > 0) {
			val += ConstProperty.getInstance().effect12783ExtraVaule;
		}

		int freeTank = soldier.getFreeCnt();
		double absorbedHurt = hurtVal * GsConst.EFF_PER * val;
		double soulHurt = absorbedHurt * (1 - GsConst.EFF_PER * ConstProperty.getInstance().effect12783BaseVaule);
		soulHurt = soldier.forceField(atkSoldier, soulHurt);
		int maxKillCnt = (int) Math.ceil(4.0f * soulHurt / soldier.getHpVal());
		maxKillCnt = Math.max(1, maxKillCnt);
		int killCnt = Math.min(maxKillCnt, soldier.getFreeCnt());
		soldier.addDeadCnt(killCnt);
		atkSoldier.addKillCnt(soldier, killCnt);
		soldier.addDebugLog("[12783] {} tank {} shared {} -> {}, dead {}",
				soldier.getUUID(), freeTank, absorbedHurt, soulHurt, killCnt);
		return hurtVal - absorbedHurt;
	}

	public void attackOver(BattleSoldier defSoldier) {
		if (soldier.getEffVal(EffType.HERO_12781) > 0 && triger12782
				&& effect12784AddFirePoint >= ConstProperty.getInstance().effect12785AtkThresholdValue1) {
			Debuff12785 debuff = new Debuff12785();
			debuff.round = soldier.getBattleRound() + 1;
			debuff.eff12785 = soldier.getEffVal(EffType.HERO_12785);
			defSoldier.debuff12785.put(debuff.round, debuff);
			soldier.addDebugLog("[12785] {} next-round attack reduction {}", defSoldier.getUUID(), debuff.eff12785);
		}
	}

	public int debuff12786(BattleSoldier defSoldier) {
		if (xingTai == JYBL && effect12784AddFirePoint >= ConstProperty.getInstance().effect12785AtkThresholdValue2) {
			int effectValue = Hero1118Rules.combinedEffectValue(
					soldier.getEffVal(EffType.HERO_12786), soldier.getEffVal(EffType.HERO_12802));
			double result = effectValue * GsConst.EFF_PER
					* ConstProperty.getInstance().effect12786SoldierAdjustMap.getOrDefault(defSoldier.getType(), 10000);
			soldier.addDebugLog("[12786] ignore target attack increase {}", result);
			return (int) result;
		}
		return 0;
	}

	public int buff12787(BattleSoldier tar) {
		if (soldier.getEffVal(EffType.HERO_12781) <= 0 || !soldier.isAlive()
				|| effect12784AddFirePoint < ConstProperty.getInstance().effect12785AtkThresholdValue3) {
			return 0;
		}
		double result = (ConstProperty.getInstance().effect12787BaseVaule
				+ soldier.getEffVal(EffType.HERO_12787) * effect12784Nums.get(tar.getType()))
				* GsConst.EFF_PER
				* ConstProperty.getInstance().effect12787SoldierAdjustMap.getOrDefault(tar.getType(), 10000);
		soldier.addDebugLog("[12787] allied attributes increase {}", result);
		return (int) result;
	}
}
