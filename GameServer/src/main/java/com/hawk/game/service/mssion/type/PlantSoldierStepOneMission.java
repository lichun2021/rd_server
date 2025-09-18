package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.SoldierStrengthen;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

@Mission(missionType = MissionType.PLANT_SOLDIER_STEP_ONE)
public class PlantSoldierStepOneMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {

	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		PlantSoldierSchool school = playerData.getPlantSoldierSchoolEntity().getPlantSchoolObj();
		for (SoldierStrengthen crack : school.getStrengthens()) {
			if (crack.getPlantStrengthLevel() >= 1) {
				entityItem.addValue(1);
				checkMissionFinish(entityItem, cfg);
				break;
			}
		}
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}
}