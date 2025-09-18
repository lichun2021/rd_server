package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 使用资源增产道具n次
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_UPGRADE_RESOURCE_PRODUCTOR)
public class UpgradeResourceProductorMission implements IMission {
	

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}
}