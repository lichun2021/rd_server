package com.hawk.game.module.lianmengyqzz.battleroom.player;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.player.PlayerData;

public abstract class IYQZZPlayerData extends PlayerData {
	
	private final IYQZZPlayer parent;

	public IYQZZPlayerData(IYQZZPlayer parent){
		this.parent = parent;
	}
	
	public IYQZZPlayer getParent() {
		return parent;
	}

	/** 锁定真实数据 */
	public void lockOriginalData() {
	}

	public void unLockOriginalData() {
	}

	/** 可出征部队 */
	public List<ArmyEntity> getMarchArmy() {
		List<ArmyEntity> armyEntities = new ArrayList<>();
		for (ArmyEntity armyEntity : getArmyEntities()) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyEntity.getArmyId());
			if (cfg != null && !cfg.isDefWeapon() && armyEntity.getFree() > 0) {
				armyEntities.add(armyEntity);
			}
		}
		return armyEntities;
	}
}
