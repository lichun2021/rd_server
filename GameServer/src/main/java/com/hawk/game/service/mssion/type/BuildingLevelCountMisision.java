package com.hawk.game.service.mssion.type;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;	

@Mission(missionType = MissionType.MISSION_BUILD_COUNT_LEVEL)
public class BuildingLevelCountMisision implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		refresh(playerData, entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		refresh(playerData, entityItem, cfg);
	}
	
	/**
	 * MissionFunType.FUN_BUILD_LEVEL
	 */
	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}
	
	private <T extends MissionEvent> void refresh(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		
		// 目标等级
		int targetLevel = cfg.getIds().get(cfg.getIds().size() - 1);
		
		// 所有的建筑类型
		List<Integer> buildIds = new ArrayList<>();
		for (int i = 0; i < cfg.getIds().size() - 1;  i++) {
			buildIds.add(cfg.getIds().get(i));
		}
		
		int currentCount = 0;
		
		for (int cfgId : buildIds) {
			
			List<BuildingBaseEntity> builds = playerData.getBuildingListByType(BuildingType.valueOf(cfgId));
			for (BuildingBaseEntity build : builds) {
				int buildLevel = playerData.getBuildingLevel(build.getId());
				if (buildLevel < targetLevel) {
					continue;
				}
				currentCount++;
			}
		}
		
		// 设置完成进度
		entityItem.setValue(currentCount);
		
		// 刷新完成
		checkMissionFinish(entityItem, cfg);
	}
}