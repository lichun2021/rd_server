package com.hawk.game.service.mssion.type;

import java.util.List;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventTalentUpgrade;

/**
 * 天赋升级任务
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_TALENT_UPGRADE)
public class TalentUpgradeMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventTalentUpgrade event = (EventTalentUpgrade) missionEvent;

		// 天赋id
		int talentId = event.getTalentId();
		List<Integer> conditions = cfg.getIds();

		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(talentId)) {
			return;
		}

		int trainCount = event.getAfterLevel() - event.getBeforeLevel();
		entityItem.addValue(trainCount);

		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		// TODO Auto-generated method stub
		
	}
}
