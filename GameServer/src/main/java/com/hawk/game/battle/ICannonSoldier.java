package com.hawk.game.battle;

import org.hawk.os.HawkRand;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;

public abstract class ICannonSoldier extends BattleSoldier {
	/** 看skill1086*/
	private int effect12023p2 = 1000;
	private int effect12023p3;
	private int effect12023p4;

	private int eff12023Round;
	private int eff12023Cnt;

	@Override
	public void roundStart() {
		super.roundStart();
		if (getEffVal(EffType.HERO_12023) > 0 && getTroop().getBattle().getBattleRound() % effect12023p2 == 0) {
			eff12023Round = getBattleRound() + effect12023p4 - 1;
			eff12023Cnt = effect12023p3;
			if (HawkRand.randInt(10000) < getEffVal(EffType.EFF_12264)) {
				eff12023Cnt = ConstProperty.getInstance().getEffect12264HoldTimes();
			}
			addDebugLog("-Jelani 12023 准备 {}", eff12023Cnt);
		}
	}

	@Override
	public double reduceHurtValPct(BattleSoldier atkSoldier, double hurtVal) {
		double result = super.reduceHurtValPct(atkSoldier, hurtVal);
		if (eff12023Round >= getBattleRound() && eff12023Cnt > 0) {
			int effVal1672 = getEffVal(EffType.EFF_12263) > 0 ? getEffVal(EffType.EFF_12263) : getEffVal(EffType.HERO_12023);
			result = result * (1 - effVal1672 * GsConst.EFF_PER);
			eff12023Cnt--;
			getTroop().getBattle().addDebugLog("-Jelani {} 12023 减伤  减伤 {}", getUUID(), effVal1672);
		}

		return result;
	}

	public int getEffect12023p2() {
		return effect12023p2;
	}

	public void setEffect12023p2(int effect12023p2) {
		this.effect12023p2 = effect12023p2;
	}

	public int getEffect12023p3() {
		return effect12023p3;
	}

	public void setEffect12023p3(int effect12023p3) {
		this.effect12023p3 = effect12023p3;
	}

	public int getEffect12023p4() {
		return effect12023p4;
	}

	public void setEffect12023p4(int effect12023p4) {
		this.effect12023p4 = effect12023p4;
	}

}
