package com.hawk.game.lianmengjunyan.player.npc;

import com.hawk.game.entity.SuperSoldierEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.supersoldier.SuperSoldier;

public class LMJYNPCSuperSoldier extends SuperSoldier{
	private LMJYNPCPlayer parent;
	protected LMJYNPCSuperSoldier(SuperSoldierEntity dbEntity) {
		super(dbEntity);
	}
	
	public static LMJYNPCSuperSoldier create(SuperSoldierEntity dbEntity) {
		LMJYNPCSuperSoldier soldier = new LMJYNPCSuperSoldier(dbEntity);
		soldier.init();
		dbEntity.recordSObj(soldier);
		return soldier;
	}

	@Override
	public void notifyChange() {
	}

	@Override
	public Player getParent() {
		return parent;
	}

	public void setParent(LMJYNPCPlayer parent) {
		this.parent = parent;
	}

	
}
