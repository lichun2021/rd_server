package com.hawk.game.battle.sssSolomon;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.BattleSoldier_6;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.util.GsConst;

public class SolomonPet_6 extends BattleSoldier_6 implements ISSSSolomonPet {

	private final BattleSoldier parent;

	public SolomonPet_6(BattleSoldier parent) {
		this.parent = parent;
	}

	@Override
	public void addKillCnt(BattleSoldier soldier, int kill) {
		super.addKillCnt(soldier, kill);
		parent.addKillCnt(soldier, kill);
	}

	@Override
	public void addDeadCnt(int roundDeadCnt) {
		parent.addDeadCnt(roundDeadCnt);
	}

	@Override
	public int skillReduceHurtPer(BattleSoldier atkSoldier) {
		return GsConst.RANDOM_MYRIABIT_BASE;
	}

	@Override
	public boolean canBeAttack() {
		return false;
	}

	@Override
	public ArmyInfo calcArmyInfo(double selfCoverRete) {
		ArmyInfo result = super.calcArmyInfo(selfCoverRete);
		result.setSssSLM1667Kill(true);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends BattleSoldier> T getParent() {
		return (T) parent;
	}
}
