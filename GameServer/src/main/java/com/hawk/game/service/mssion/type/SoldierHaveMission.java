package com.hawk.game.service.mssion.type;

import java.util.List;

import com.hawk.game.config.MissionCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.MissionEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventSoldierAdd;
import com.hawk.game.service.mssion.event.EventSoldierTrain;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 拥有士兵数量任务
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_HAVE_SOLIDER)
public class SoldierHaveMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		// 士兵训练触发任务刷新
		if (missionEvent instanceof EventSoldierTrain) {
			trainRefresh(playerData, missionEvent, entityItem, cfg);
		}
		// 士兵数量增加(非士兵训练)触发任务刷新
		else if (missionEvent instanceof EventSoldierAdd) {
			addRefresh(playerData, missionEvent, entityItem, cfg);
		}
	}

	/**
	 * 士兵训练触发任务刷新
	 * @param missionEvent
	 * @param entityItem
	 * @param cfg
	 */
	private <T extends MissionEvent> void trainRefresh(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<Integer> armyIds = cfg.getIds();
		int haveNum = 0;
		for (int armyId : armyIds) {
			// armyId为0代表任意的
			if (armyId != 0) {
				ArmyEntity armyEntity = playerData.getArmyEntity(armyId);
				if (armyEntity != null) {
					haveNum += armyEntity.getCureCount() + armyEntity.getWoundedCount() + armyEntity.getFree() + armyEntity.getMarch();
				}
			} else {
				List<ArmyEntity> armyEntities = playerData.getArmyEntities();
				for (ArmyEntity armyEntity : armyEntities) {
					if (armyEntity != null) {
						haveNum += armyEntity.getCureCount() + armyEntity.getWoundedCount() + armyEntity.getFree() + armyEntity.getMarch();
					}
				}
			}
		}
		entityItem.setValue(haveNum);
		checkMissionFinish(entityItem, cfg);
	}

	/**
	 * 士兵数量增加(非士兵训练)触发任务刷新
	 * @param missionEvent
	 * @param entityItem
	 * @param cfg
	 */
	private <T extends MissionEvent> void addRefresh(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<Integer> armyIds = cfg.getIds();
		int haveNum = 0;
		for (int armyId : armyIds) {
			// armyId为0代表任意的
			if (armyId != 0) {
				ArmyEntity armyEntity = playerData.getArmyEntity(armyId);
				if (armyEntity != null) {
					haveNum += armyEntity.getCureCount() + armyEntity.getWoundedCount() + armyEntity.getFree() + armyEntity.getMarch();
				}
			} else {
				List<ArmyEntity> armyEntities = playerData.getArmyEntities();
				for (ArmyEntity armyEntity : armyEntities) {
					if (armyEntity != null) {
						haveNum += armyEntity.getCureCount() + armyEntity.getWoundedCount() + armyEntity.getFree() + armyEntity.getMarch();
					}
				}
			}
		}
		entityItem.setValue(haveNum);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<Integer> armyIds = cfg.getIds();
		int haveNum = 0;
		for (int armyId : armyIds) {
			// armyId为0代表任意的
			if (armyId != 0) {
				ArmyEntity armyEntity = playerData.getArmyEntity(armyId);
				if (armyEntity != null) {
					haveNum += armyEntity.getCureCount() + armyEntity.getWoundedCount() + armyEntity.getFree() + armyEntity.getMarch();
				}
			} else {
				List<ArmyEntity> armyEntities = playerData.getArmyEntities();
				for (ArmyEntity armyEntity : armyEntities) {
					if (armyEntity != null) {
						haveNum += armyEntity.getCureCount() + armyEntity.getWoundedCount() + armyEntity.getFree() + armyEntity.getMarch();
					}
				}
			}
		}
		entityItem.setValue(haveNum);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		int soldierId = 0;
		
		// 士兵训练触发任务刷新
		if (missionEvent instanceof EventSoldierTrain) {
			EventSoldierTrain event = (EventSoldierTrain) missionEvent;
			soldierId = event.getSoldierId();
			
			// 士兵数量增加(非士兵训练)触发任务刷新
		} else if (missionEvent instanceof EventSoldierAdd) {
			EventSoldierAdd event = (EventSoldierAdd) missionEvent;
			soldierId = event.getSoldierId();
		}
		
		// 特殊任务，触发任务的地方可能是兵减少的情况，所以算增量的方式跟其它一般任务有所不同
		int typeId = MissionCfg.getTypeId(MissionFunType.FUN_TRAIN_SOLDIER_HAVE_NUMBER, soldierId);
		MissionEntity mission = player.getData().getMissionByTypeId(typeId);
		int beforeCount = mission == null ? 0 : mission.getNum();
		int afterCount = GameUtil.getSoldierHaveNum(player.getData(), soldierId);
		int count = afterCount - beforeCount;
		if (count > 0) {
			MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_TRAIN_SOLDIER_HAVE_NUMBER, soldierId, count);
			MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_TRAIN_SOLDIER_HAVE_NUMBER, 0, count);
		}
	}
}
