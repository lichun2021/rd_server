package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.PlayerAchieveService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventPlayerUpLevel;

/**
 * 全服玩家第一个达到{1}等级{2}个
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_SOLE_PLAYER_LEVEL)
public class SolePlayerLevelMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventPlayerUpLevel event = (EventPlayerUpLevel) missionEvent;
		if (event.getAfter() < cfg.getIds().get(0)) {
			return;
		}
		
		boolean soleAchieveConclude = PlayerAchieveService.getInstance().soleAchieveConclude(playerData.getPlayerId(), MissionType.MISSION_SOLE_PLAYER_LEVEL.intValue());
		if (!soleAchieveConclude) {
			return;
		}
		
		entityItem.setValue(1);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int level = playerData.getPlayerBaseEntity().getLevel();
		if (level < cfg.getIds().get(0)) {
			return;
		}
		
		entityItem.setValue(1);
		checkMissionFinish(entityItem, cfg);
		
		PlayerAchieveService.getInstance().soleAchieveConclude(playerData.getPlayerId(), MissionType.MISSION_SOLE_PLAYER_LEVEL.intValue());
	}
	
	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}
}
