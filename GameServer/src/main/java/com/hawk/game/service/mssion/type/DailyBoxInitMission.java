package com.hawk.game.service.mssion.type;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

@Mission(missionType = MissionType.DAILY_MISSION_BOX_INIT)
public class DailyBoxInitMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int count = RedisProxy.getInstance().getDailyMissionBox(playerData.getPlayerId());
		entityItem.setValue(count);
		checkMissionFinish(entityItem, cfg);
	}
	
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int count = RedisProxy.getInstance().getDailyMissionBox(playerData.getPlayerId());
		entityItem.setValue(count);
		checkMissionFinish(entityItem, cfg);
	}
}