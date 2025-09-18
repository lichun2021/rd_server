package com.hawk.game.battle.sssSolomon;

import com.hawk.game.battle.BattleSoldier;
import com.hawk.game.battle.IBattleSoldier;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.SoldierType;

public interface ISSSSolomonPet extends IBattleSoldier {

	public <T extends BattleSoldier> T getParent();
	
	public static void createPet(BattleSoldier parent, Player player, BattleSoldierCfg soldierCfg, int count, int shadowCnt) {
		SoldierType type = SoldierType.valueOf(soldierCfg.getType());
		BattleSoldier result = null;
		switch (type) {
		case TANK_SOLDIER_1:
			result = new SolomonPet_1(parent);
			break;
		case TANK_SOLDIER_2:
			result = new SolomonPet_2(parent);
			break;
		case PLANE_SOLDIER_3:
			result = new SolomonPet_3(parent);
			break;
		case PLANE_SOLDIER_4:
			result = new SolomonPet_4(parent);
			break;
		case FOOT_SOLDIER_5:
			result = new SolomonPet_5(parent);
			break;
		case FOOT_SOLDIER_6:
			result = new SolomonPet_6(parent);
			break;
		case CANNON_SOLDIER_7:
			result = new SolomonPet_7(parent);
			break;
		case CANNON_SOLDIER_8:
			result = new SolomonPet_8(parent);
			break;
		default:
			break;
		}
		if (type != result.getType()) {
			throw new RuntimeException("Create ISSSSolomonPet error!" + result.getType() + "  need = " + type);
		}
		result.init(player, soldierCfg, count, shadowCnt);
		result.setInvincible(true);
		result.setEffMap(parent.getEffMap());
		result.setEffPerNum(parent.getEffPerNum());
		result.setHeros(parent.getHeros());
		parent.setSolomonPet(result);
	}
}
