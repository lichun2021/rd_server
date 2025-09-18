package com.hawk.game.battle.sssSolomon;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.BattleSoldier_4;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;

public class SolomonPet_4 extends BattleSoldier_4 implements ISSSSolomonPet {

	private final BattleSoldier parent;

	public SolomonPet_4(BattleSoldier parent) {
		this.parent = parent;
	}

	@Override
	public void addKillCnt(BattleSoldier soldier, int kill) {
		super.addKillCnt(soldier, kill);
		parent.addKillCnt(soldier, kill);
	}

	@Override
	public int getEffVal(EffType eff) {
		if (eff == EffType.HERO_12465) {
			return 0;
		}

		return parent.getEffVal(eff);
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
