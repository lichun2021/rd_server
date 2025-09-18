package com.hawk.game.battle;

import org.hawk.os.HawkRand;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;

public abstract class IFootSoldier extends BattleSoldier {
	private int skill1083p3;
	private boolean eff1672Round;
	private int eff1672Cnt;

	@Override
	public void roundStart() {
		super.roundStart();
		eff1672Cnt = 0;
		eff1672Round = getEffVal(EffType.HERO_1672) > 0 && getTroop().getBattle().getBattleRound() % 5 == 0;

	}

	@Override
	protected double getHurtVal(BattleSoldier defSoldier, double reducePer) {
		if (skill1083p3 > 0 && HawkRand.randInt(GsConst.RANDOM_MYRIABIT_BASE) < getEffVal(EffType.HERO_1671)) {
			defSoldier.setSss1671BiZhong(true);
			getTroop().getBattle().addDebugLog("### 阿尔托莉雅 {} 必中要害模式 ", getUUID());
		}
		double result = super.getHurtVal(defSoldier, reducePer);
		if (defSoldier.isSss1671BiZhong()) {
			result = result * (1 + skill1083p3 * GsConst.EFF_PER);
		}
		defSoldier.setSss1671BiZhong(false);
		return result;
	}

	@Override
	public double reduceHurtValPct(BattleSoldier atkSoldier, double hurtVal) {
		double result = super.reduceHurtValPct(atkSoldier, hurtVal);
		int eff12253Val = getEffVal(EffType.EFF_12253);
		int max = eff12253Val == 0 ? 3 : ConstProperty.getInstance().getEffect12253HoldTimesMap().getOrDefault(getType(), 0);
		if (!isSss1671BiZhong() && eff1672Round && eff1672Cnt < max) {
			int effVal1672 = getEffVal(EffType.HERO_1672);
			if (eff1672Cnt == 0 && eff12253Val > 0) { // 最终数值【1672】 = 【12253】*兵种修正系数/10000 // - 兵种修正系数按狙击兵（兵种类型 = 6）和突击步兵（兵种类型 = 5）进行配置修正，读取const表，字段effect12253SoldierAdjust
				effVal1672 = (int) (eff12253Val * GsConst.EFF_PER * ConstProperty.getInstance().getEffect12253SoldierAdjustMap().getOrDefault(getType(), 0));
				addDebugLog("【1672】的首次伤害抵抗效果进行数值变更 最终数值【1672】 = 【12253】*兵种修正系数/10000 = {}", effVal1672);
			}
			int reducePct = effVal1672 - effVal1672 * eff1672Cnt / max;
			result = result * (1 - reducePct * GsConst.EFF_PER);
			eff1672Cnt++;
			getTroop().getBattle().addDebugLog("### {} 阿尔托莉雅 {} 必防模式  减伤 {}", eff1672Cnt, getUUID(), reducePct);
		}

		return result;
	}

	public int kunNaXiuZhen(int kill) {
		int result = kill;
		if (eff1672Round) {
			result = (int) (kill * (1 - getEffVal(EffType.HERO_1672) * GsConst.EFF_PER));
			getTroop().getBattle().addDebugLog("### 阿尔托莉雅 {} 必防模式 昆娜 {} 输出 {}", getUUID(), kill, result);
		}
		return result;
	}

	public int getSkill1083p3() {
		return skill1083p3;
	}

	public void setSkill1083p3(int skill1083p3) {
		this.skill1083p3 = skill1083p3;
	}

	public boolean isEff1672Round() {
		return eff1672Round;
	}

	public void setEff1672Round(boolean eff1672Round) {
		this.eff1672Round = eff1672Round;
	}

	public int getEff1672Cnt() {
		return eff1672Cnt;
	}

	public void setEff1672Cnt(int eff1672Cnt) {
		this.eff1672Cnt = eff1672Cnt;
	}

}
