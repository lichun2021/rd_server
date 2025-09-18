package com.hawk.game.battle.sssSolomon;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.BattleSoldier_1;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.util.GsConst;

public class SolomonPet_1 extends BattleSoldier_1 implements ISSSSolomonPet {

	private final BattleSoldier parent;

	public SolomonPet_1(BattleSoldier parent) {
		this.parent = parent;
	}
	
	@Override
	protected int get12085Cnt() {
		return parent.getFreeCnt();
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
