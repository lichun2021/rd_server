package com.hawk.game.service.mssion.type;

import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.HeroOfficeCfg;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 英雄委任任务
 * 
 * @author lating
 *
 */
@Mission(missionType = MissionType.HERO_APPOINT_COUNT)
public class HeroAppointMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<Integer> buildingTypes = cfg.getIds();
		long count = 0, oldCount = entityItem.getValue();
		for (HeroEntity heroEntity : playerData.getHeroEntityList()) {
			HeroOfficeCfg officeCfg = HawkConfigManager.getInstance().getConfigByKey(HeroOfficeCfg.class, heroEntity.getOffice());
			if (officeCfg == null) {
				continue;
			}
			
			if (buildingTypes.get(0) == 0 || buildingTypes.contains(officeCfg.getUnlockBuildingType())) {
				count ++;
			}
		}
		
		if (oldCount != count) {
			entityItem.setValue(count);
			checkMissionFinish(entityItem, cfg);
		}
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<Integer> buildingTypes = cfg.getIds();
		int count = 0;
		for (HeroEntity heroEntity : playerData.getHeroEntityList()) {
			HeroOfficeCfg officeCfg = HawkConfigManager.getInstance().getConfigByKey(HeroOfficeCfg.class, heroEntity.getOffice());
			if (officeCfg == null) {
				continue;
			}
			
			if (buildingTypes.get(0) == 0 || buildingTypes.contains(officeCfg.getUnlockBuildingType())) {
				count ++;
			}
		}
		
		entityItem.setValue(count);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}
	
}
