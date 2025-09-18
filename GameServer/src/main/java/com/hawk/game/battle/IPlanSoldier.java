package com.hawk.game.battle;

import java.util.Objects;

import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.hawk.game.battle.sssSolomon.ISSSSolomonPet;
import com.hawk.game.config.BattleSoldierSkillCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PBSoldierSkill;
import com.hawk.game.util.GsConst;

public abstract class IPlanSoldier extends BattleSoldier {
	int debuff12041round;
	int debuff12041Val;

	private HawkTuple2<Integer, Integer> eff12134Debuff = HawkTuples.tuple(0, 0);

	private HawkTuple2<Integer, Integer> eff12143Debuff = HawkTuples.tuple(0, 0);

	private HawkTuple2<Integer, Integer> eff12153Debuff = HawkTuples.tuple(0, 0);
	private HawkTuple2<Integer, Integer> eff12154Debuff = HawkTuples.tuple(0, 0);

	@Override
	public int skillHurtExactly(BattleSoldier defSoldier) {
		int result = super.skillHurtExactly(defSoldier);
		if (debuff12041round >= getBattleRound()) {
			result -= debuff12041Val;
		}

		return result;
	}

	@Override
	public double addHurtValPct(BattleSoldier defSoldier, double hurtVal) {
		hurtVal = super.addHurtValPct(defSoldier, hurtVal);
		hurtVal *= (1 - eff12153DebuffVal() * GsConst.EFF_PER - eff12154DebuffVal() * GsConst.EFF_PER);
		if (eff12134DebuffVal() > 0) {
			final double oldhurtval = hurtVal;
			hurtVal *= (1 - eff12134DebuffVal() * GsConst.EFF_PER);
			hurtVal = Math.max(hurtVal, 0);
			addDebugLog("###Soldier={} 12134 debuf 伤害{}, 操作后伤害值 {}", getUUID(), oldhurtval, hurtVal);
		}
		return hurtVal;
	}

	@Override
	public boolean isPlan() {
		return true;
	}

	@Override
	public boolean trigerSkill(BattleSoldierSkillCfg skill, int weight) {
		if (skill.getIndex() == PBSoldierSkill.PLANE_SOLDIER_3_SKILL_3 || skill.getIndex() == PBSoldierSkill.PLANE_SOLDIER_4_SKILL_3) {
			weight += getTroop().getBuff12061Val();
		}

		boolean result = super.trigerSkill(skill, weight);
		if (result) {
			getTroop().addBuff12061Val(getEffVal(EffType.EFF_12061));
		}
		return result;
	}

	public void addDebuff12041(int val) {
		if (getBattleRound() <= debuff12041round) {
			return;
		}
		debuff12041round = getBattleRound() + ConstProperty.getInstance().getEffect12041ContinueRound() - 1;
		debuff12041Val = val;
		addDebugLog("{} debuff  12041 {}", getUUID(), val);
	}

	public HawkTuple2<Integer, Integer> getEff12134Debuff() {
		return eff12134Debuff;
	}

	public void setEff12134Debuff(HawkTuple2<Integer, Integer> debuff) {
		this.eff12134Debuff = debuff;
	}

	int eff12134DebuffVal() {
		if (Objects.isNull(eff12134Debuff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > eff12134Debuff.first) {
			return 0;
		}
		addDebugLog("### {} 12134 debuf 降低其hurt {} ", getUUID(), eff12134Debuff.second);
		return eff12134Debuff.second;
	}

	public HawkTuple2<Integer, Integer> getEff12143Debuff() {
		return eff12143Debuff;
	}

	public void setEff12143Debuff(HawkTuple2<Integer, Integer> debuff) {
		this.eff12143Debuff = debuff;
	}

	int eff12143DebuffVal() {
		if (Objects.isNull(eff12143Debuff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > eff12143Debuff.first) {
			return 0;
		}
		addDebugLog("### {} 12143 debuf 降低目标 +XX.XX% 的轰炸概率 {} ", getUUID(), eff12143Debuff.second);
		return eff12143Debuff.second;
	}

	public HawkTuple2<Integer, Integer> getEff12153Debuff() {
		return eff12153Debuff;
	}

	public void setEff12153Debuff(HawkTuple2<Integer, Integer> debuff) {
		this.eff12153Debuff = debuff;
	}

	public int eff12153DebuffVal() {
		if (this instanceof ISSSSolomonPet) {
			IPlanSoldier parent = ((ISSSSolomonPet) this).getParent();
			return parent.eff12153DebuffVal();
		}

		if (Objects.isNull(eff12153Debuff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > eff12153Debuff.first) {
			return 0;
		}
		addDebugLog("### {} 12153 debuf 降低目标 +XX.XX% 的伤害效果 {} ", getUUID(), eff12153Debuff.second);
		return eff12153Debuff.second;
	}

	public HawkTuple2<Integer, Integer> getEff12154Debuff() {
		return eff12154Debuff;
	}

	public void setEff12154Debuff(HawkTuple2<Integer, Integer> debuff) {
		this.eff12154Debuff = debuff;
	}

	int eff12154DebuffVal() {
		if (this instanceof ISSSSolomonPet) {
			IPlanSoldier parent = ((ISSSSolomonPet) this).getParent();
			return parent.eff12154DebuffVal();
		}

		if (Objects.isNull(eff12154Debuff)) {
			return 0;
		}
		int round = getTroop().getBattle().getBattleRound();
		if (round > eff12154Debuff.first) {
			return 0;
		}
		addDebugLog("### {} 12154 debuf 降低目标 +XX.XX% 的伤害效果 {} ", getUUID(), eff12154Debuff.second);
		return eff12154Debuff.second;
	}
}
