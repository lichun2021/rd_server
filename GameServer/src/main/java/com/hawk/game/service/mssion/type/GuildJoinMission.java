package com.hawk.game.service.mssion.type;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 加入联盟任务
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_GUILD_JOIN)
public class GuildJoinMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		if (entityItem.getValue() >= 1) {
			return;
		}
		entityItem.setValue(1);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		String playerId = playerData.getPlayerBaseEntity().getPlayerId();
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		if (!HawkOSOperator.isEmptyString(guildId)) {
			entityItem.setValue(1);
		}
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		// TODO Auto-generated method stub
		
	}
}
