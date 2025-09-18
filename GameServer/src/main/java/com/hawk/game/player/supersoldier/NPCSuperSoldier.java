package com.hawk.game.player.supersoldier;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.config.SuperSoldierCfg;
import com.hawk.game.entity.SuperSoldierEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierState;

public class NPCSuperSoldier extends SuperSoldier {
	private NpcPlayer parent = NpcPlayer.DEFAULT_INSTANCE;

	protected NPCSuperSoldier(SuperSoldierEntity dbEntity) {
		super(dbEntity);
	}

	public static NPCSuperSoldier create(int soldierId, int star) {
		SuperSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierCfg.class, soldierId);
		if (cfg == null) {
			return null;
		}
		SuperSoldierEntity newSso = new SuperSoldierEntity();
		newSso.setSoldierId(soldierId);
		newSso.setPlayerId(NpcPlayer.DEFAULT_INSTANCE.getId());
		newSso.setStar(star);
		newSso.setState(PBSuperSoldierState.SUPER_SOLDIER_STATE_FREE_VALUE);

		NPCSuperSoldier soldier = new NPCSuperSoldier(newSso);
		soldier.init();
		newSso.recordSObj(soldier);

		soldier.getPassiveSkillSlots().forEach(slot -> slot.getSkill().addExp(3000));
		soldier.getSkillSlots().forEach(slot -> slot.getSkill().addExp(3000));
		soldier.loadEffVal();
		return soldier;
	}

	@Override
	public void notifyChange() {
	}

	@Override
	public Player getParent() {
		return parent;
	}

}
