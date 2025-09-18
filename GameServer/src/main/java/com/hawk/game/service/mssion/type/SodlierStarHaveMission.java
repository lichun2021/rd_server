package com.hawk.game.service.mssion.type;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 拥有x星级士兵x个
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_HAVE_STAR_SOLIDER)
public class SodlierStarHaveMission implements IMission {

	/**
	 * 刷新任务
	 */
	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		checkMission(playerData, entityItem, cfg);
	}

	/**
	 * 初始化任务
	 */
	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		checkMission(playerData, entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	/**
	 * 检测任务
	 */
	public void checkMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		// 目标星级
		int targetStar = cfg.getIds().get(0);

		// 数量
		int number = 0;
		
		List<ArmyEntity> armys = playerData.getArmyEntities();
		for (ArmyEntity army : armys) {
			if (army == null) {
				continue;
			}
			if (getSoldierStar(army.getArmyId(), playerData) < targetStar) {
				continue;
			}
			number += army.getCureCount() + army.getWoundedCount() + army.getFree() + army.getMarch();
		}
		
		entityItem.setValue(number);
		checkMissionFinish(entityItem, cfg);
	}
	
	/**
	 * 计算兵种荣耀等级
	 */
	public int getSoldierStar(int armyId, PlayerData playerData) {
		try {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			BuildingType buildType = BuildingType.valueOf(cfg.getBuilding());
			// 如果是尖塔 会有两个
			if (cfg.getBuilding() == BuildingType.PRISM_TOWER_VALUE) {
				List<BuildingBaseEntity> towerList = playerData.getBuildingListByType(buildType);
				for (BuildingBaseEntity tower : towerList) {
					BuildingCfg towerCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, tower.getBuildingCfgId());
					if (towerCfg.getBattleSoldierId() == armyId) {
						return towerCfg.getHonor();
					}
				}
				return 0;
			}
			BuildingCfg bcfg = playerData.getBuildingCfgByType(buildType);
			if (bcfg == null) {
				return 0;
			}
			return bcfg.getHonor();

		} catch (Exception e) {
			HawkException.catchException(e, "armyId = " + armyId);
		}
		return 0;
	}
}
