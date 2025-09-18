package com.hawk.game.service.mssion.type;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.game.config.BuildingCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.Const.StateType;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * n块资源田同时增产
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_UPGRADE_RESOURCE_FIELD_COUNT)
public class UpgradeResProdFiledCountMission implements IMission {
	

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		
		long now = HawkTime.getMillisecond();
		int statusBuildingCount = 0;
		
		// 判断哪些资源田使用过，哪些资源田没使用过增产道具
		List<BuildingBaseEntity> buildingEntities = playerData.getBuildingEntities();
		List<StatusDataEntity> statusDataList = playerData.getStatusDataEntities();
		
		for (BuildingBaseEntity buildingEntity : buildingEntities) {
			
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
			if (buildingCfg == null) {
				continue;
			}
			
			if (!buildingCfg.isResBuilding()) {
				continue;
			}
			
			for (StatusDataEntity statusEntity : statusDataList) {
				if (statusEntity.getType() == StateType.BUFF_STATE_VALUE 
						&& buildingEntity.getId().equals(statusEntity.getTargetId())
						&& statusEntity.getEndTime() > now) {
					statusBuildingCount++;
					break;
				}
			}
		}
		
		if (statusBuildingCount >= entityItem.getValue()) {
			entityItem.setValue(statusBuildingCount);
			checkMissionFinish(entityItem, cfg);
		}
	}
	
	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		long now = HawkTime.getMillisecond();
		int statusBuildingCount = 0;
		
		// 判断哪些资源田使用过，哪些资源田没使用过增产道具
		List<BuildingBaseEntity> buildingEntities = playerData.getBuildingEntities();
		List<StatusDataEntity> statusDataList = playerData.getStatusDataEntities();
		
		for (BuildingBaseEntity buildingEntity : buildingEntities) {
			
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
			if (buildingCfg == null) {
				continue;
			}
			
			if (!buildingCfg.isResBuilding()) {
				continue;
			}
			
			for (StatusDataEntity statusEntity : statusDataList) {
				if (statusEntity.getType() == StateType.BUFF_STATE_VALUE 
						&& buildingEntity.getId().equals(statusEntity.getTargetId())
						&& statusEntity.getEndTime() > now) {
					statusBuildingCount++;
					break;
				}
			}
		}
		
		entityItem.setValue(statusBuildingCount);
		checkMissionFinish(entityItem, cfg);
	}
}