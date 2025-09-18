package com.hawk.game.service.mssion.type;

import java.util.List;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventStartTechUpgrade;

/**
 * 开始研究{1}id的科技{2}次
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_START_TECH__TIMES)
public class TechStartUpTimes implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		// 科技id
		EventStartTechUpgrade event = (EventStartTechUpgrade)missionEvent;
		int talentId = event.getTechId();
		
		List<Integer> conditions = cfg.getIds();
		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(talentId)) {
			return;
		}
		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}
}
