package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.util.GameUtil;

/**
 * 研究{1}类型科技{2}次数(大类型 军事 发展 资源 城防)
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_TECHNOLOGY_TYPE_STUDY_TIMES)
public class TechTypeStudyTimes implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int times = GameUtil.getTechRechearTimes(playerData, cfg.getIds().get(0), false);
		entityItem.setValue(times);
		checkMissionFinish(entityItem, cfg);
	}
	
	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int times = GameUtil.getTechRechearTimes(playerData, cfg.getIds().get(0), false);
		entityItem.setValue(times);
		checkMissionFinish(entityItem, cfg);
	}
}
