package com.hawk.game.service.mssion.type;

import java.util.List;
import org.hawk.config.HawkConfigManager;
import com.hawk.game.config.PlantFactoryCfg;
import com.hawk.game.entity.PlantFactoryEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventPlantFactoryLvup;

/**
 * 泰能生产线等级任务
 * 
 * @author lating
 *
 */
@Mission(missionType = MissionType.PLANT_FACTOFY_LEVEL)
public class PlantFactoryLevelMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventPlantFactoryLvup event = (EventPlantFactoryLvup) missionEvent;
		int factoryType = event.getFactoryType();
		int newLevel = event.getAfterLevel();
		long maxLevel = entityItem.getValue();
		long oldMissionVal = maxLevel;
		List<Integer> factoryTypes = cfg.getIds();
		for (int type : factoryTypes) {
			if (type == 0) {
				maxLevel = Math.max(maxLevel, newLevel);
			} else if (type == factoryType) {
				maxLevel = Math.max(maxLevel, newLevel);
			}
		}
		
		if (oldMissionVal != maxLevel) {
			entityItem.setValue(maxLevel);
			checkMissionFinish(entityItem, cfg);
		}
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<Integer> factoryTypes = cfg.getIds();
		int maxLevel = 0;
		for (PlantFactoryEntity factory : playerData.getPlantFactoryEntities()) {
			PlantFactoryCfg factoryCfg = HawkConfigManager.getInstance().getConfigByKey(PlantFactoryCfg.class, factory.getPlantCfgId());
			// type为0代表任意的
			if ((factoryTypes.get(0) == 0 || factoryTypes.contains(factoryCfg.getFactoryType())) && factoryCfg.getLevel() > maxLevel) {
				maxLevel = factoryCfg.getLevel();
			}
		}
		
		entityItem.setValue(maxLevel);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}
	
}
