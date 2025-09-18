package com.hawk.game.service.mssion.type;

import java.util.List;

import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventTechnologyUpgrade;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 科技升级任务
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_TECHNOLOGY_UPGRADE)
public class TechnologyUpgradeMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventTechnologyUpgrade event = (EventTechnologyUpgrade) missionEvent;

		// 科技id
		int talentId = event.getTechId();
		List<Integer> conditions = cfg.getIds();

		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(talentId)) {
			return;
		}

		entityItem.setValue(event.getAfterLevel());
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<Integer> conditions = cfg.getIds();

		for (int condition : conditions) {
			TechnologyEntity entity = playerData.getTechEntityByTechId(condition);
			if (entity == null) {
				continue;
			}

			int level = entity.getLevel();
			if (level > entityItem.getValue()) {
				entityItem.setValue(entity.getLevel());
			}
		}
		
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		EventTechnologyUpgrade event = (EventTechnologyUpgrade) missionEvent;
		MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_TECH_LEVEL, event.getTechId(), event.getAfterLevel());
		MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_TECH_RESEARCH, 0, 1);
	}
}
