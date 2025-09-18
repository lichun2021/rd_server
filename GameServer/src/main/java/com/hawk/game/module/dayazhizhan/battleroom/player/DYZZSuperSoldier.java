package com.hawk.game.module.dayazhizhan.battleroom.player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.entifytype.EntityType;

import com.hawk.game.config.SuperSoldierCfg;
import com.hawk.game.config.SuperSoldierEnergyCfg;
import com.hawk.game.entity.SuperSoldierEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.player.supersoldier.energy.ISuperSoldierEnergy;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierState;

public class DYZZSuperSoldier extends SuperSoldier {
	private DYZZPlayer parent;

	protected DYZZSuperSoldier(SuperSoldierEntity dbEntity) {
		super(dbEntity);
	}

	public static DYZZSuperSoldier create(DYZZPlayer player, SuperSoldierCfg scfg) {
		int soldierId = scfg.getSupersoldierId();
		int star = 10;
		SuperSoldierEntity newSso = new SuperSoldierEntity();
		newSso.setPersistable(false);
		newSso.setEntityType(EntityType.TEMPORARY);
		newSso.setSoldierId(soldierId);
		newSso.setPlayerId(player.getId());
		newSso.setStar(star);
		newSso.setState(PBSuperSoldierState.SUPER_SOLDIER_STATE_FREE_VALUE);
		if (scfg.getUnlockAnyWhereGetSkin() > 0) { // 没得解锁
			newSso.setAnyWhereUnlock(1);
			newSso.setSkin(scfg.getUnlockAnyWhereGetSkin());
		}
		DYZZSuperSoldier soldier = new DYZZSuperSoldier(newSso);
		soldier.init();
		newSso.recordSObj(soldier);
		soldier.parent = player;
		soldier.getPassiveSkillSlots().forEach(slot -> slot.getSkill().addExp(3000));
		soldier.getSkillSlots().forEach(slot -> slot.getSkill().addExp(3000));
		soldier.getSoldierEnergy().unlockEnergy();
		ConfigIterator<SuperSoldierEnergyCfg> ecfgit = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierEnergyCfg.class);
		Map<Integer, SuperSoldierEnergyCfg> configMap = new HashMap<>();
		for (SuperSoldierEnergyCfg ecfg : ecfgit) {
			if (ecfg.getSupersoldierId() == soldierId) {
				configMap.merge(ecfg.getEnablingPosition(), ecfg, (v1, v2) -> v1.getEnablingLevel() > v2.getEnablingLevel() ? v1 : v2);
			}
		}
		for (ISuperSoldierEnergy energy : soldier.getSoldierEnergy().getEnergys()) {
			energy.gmSetLevel(configMap.get(energy.getPos()).getId());
		}
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
