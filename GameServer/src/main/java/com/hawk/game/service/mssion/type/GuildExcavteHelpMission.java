package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventGuildExcavteHelp;

/**
 * 联盟宝藏帮助挖掘
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_GUILD_EXCAVTE_HELP)
public class GuildExcavteHelpMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventGuildExcavteHelp event = (EventGuildExcavteHelp) missionEvent;
		entityItem.addValue(event.getTimes());
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		// TODO Auto-generated method stub
	}
}
