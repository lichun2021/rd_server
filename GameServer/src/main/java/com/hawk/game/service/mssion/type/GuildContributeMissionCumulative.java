package com.hawk.game.service.mssion.type;

import java.util.Map;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventGuildContribute;

/**
 * 联盟捐献任务
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_GUILD_CONTRIBUTE_CUMULATIVE)
public class GuildContributeMissionCumulative implements IMission {
	final String KEY = "guildjuanxian";

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		Map<String, Integer> allMap = RedisProxy.getInstance().getCumulativeMissionCount(playerData.getPlayerId(), MissionType.MISSION_GUILD_CONTRIBUTE_CUMULATIVE);
		int value = allMap.getOrDefault(KEY, 0);
		entityItem.addValue(value);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void cumulativeMission(String playerId, T missionEvent) {
		EventGuildContribute event = (EventGuildContribute) missionEvent;
		RedisProxy.getInstance().incCumulativeMissionCount(playerId, MissionType.MISSION_GUILD_CONTRIBUTE_CUMULATIVE, KEY, event.getTimes());
	}
	
	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventGuildContribute event = (EventGuildContribute) missionEvent;
		entityItem.addValue(event.getTimes());
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		// TODO Auto-generated method stub
		
	}
}
