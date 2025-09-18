package com.hawk.game.script;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.SoldierNumChangeEvent;
import com.hawk.game.GsConfig;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventSoldierAdd;
import com.hawk.game.strengthenguide.StrengthenGuideManager;
import com.hawk.game.strengthenguide.msg.SGPlayerSoldierNumChangeMsg;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 清除所有兵
 * 
 * http://localhost:8080/script/deleteSoldier?playerId=1aau-2ayfd6-1
 * 
 * @author lating
 *
 */
public class DeleteSoldierHandler extends HawkScript {
	
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		if (!GsConfig.getInstance().isDebug()) {
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "is not debug");
		}
		
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
			}
			
			if (player.isActiveOnline()) {
				int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
				HawkTaskManager.getInstance().postTask(new HawkTask() {
					@Override
					public Object run() {
						deleteSoldier(player);
						return null;
					}
				}, threadIdx);
			} else {
				deleteSoldier(player);
			}
			
			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkException.formatStackMsg(e);
		}
	}
	
	private void deleteSoldier(Player player) {
		Map<Integer, Integer> fireArmy = new HashMap<>();
		int woundedCount = 0;
		Set<Integer> buildingSet = new HashSet<Integer>();
		for (ArmyEntity armyEntity : player.getData().getArmyEntities()) {
			if (armyEntity.getWoundedCount() > 0) {
				fireSoldier(player, armyEntity.getArmyId(), armyEntity.getWoundedCount());
				fireArmy.put(armyEntity.getArmyId(), armyEntity.getWoundedCount());
				armyEntity.addWoundedCount(0 - armyEntity.getWoundedCount());
				LogUtil.logArmyChange(player, armyEntity, armyEntity.getWoundedCount(), ArmySection.WOUNDED, ArmyChangeReason.FIRE);
				woundedCount++;
			}
			
			if (armyEntity.getCureFinishCount() > 0) {
				fireArmy.put(armyEntity.getArmyId(), armyEntity.getCureFinishCount());
				armyEntity.setCureFinishCount(0);
				woundedCount++;
			}

			if (armyEntity.getFree() > 0) {
				fireSoldier(player, armyEntity.getArmyId(), armyEntity.getFree());
				fireArmy.put(armyEntity.getArmyId(), armyEntity.getFree());
				armyEntity.addFree(0 - armyEntity.getFree());
				LogUtil.logArmyChange(player, armyEntity, armyEntity.getFree(), ArmySection.FREE, ArmyChangeReason.FIRE);
			}
			
			if (armyEntity.getTrainFinishCount() > 0) {
				fireArmy.put(armyEntity.getArmyId(), armyEntity.getTrainFinishCount());
				armyEntity.setTrainFinishCount(0);
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyEntity.getArmyId());
				buildingSet.add(cfg.getBuilding());
			}
		}
		
		if (woundedCount > 0) {
			GameUtil.changeBuildingStatus(player, BuildingType.HOSPITAL_STATION_VALUE, Const.BuildingStatus.COMMON);
		}
		
		for (int type : buildingSet) {
			GameUtil.changeBuildingStatus(player, type, Const.BuildingStatus.COMMON);
		}

		List<Integer> armyIds = fireArmy.keySet().stream().collect(Collectors.toList());
        // 异步推送消息
        player.getPush().syncArmyInfo(ArmyChangeCause.FIRE, armyIds.toArray(new Integer[armyIds.size()]));
        player.refreshPowerElectric(PowerChangeReason.FIRE_SOLDIER);
        player.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT);
	}
	
	private void fireSoldier(Player player, int armyId, int fireCnt) {
		// 刷新任务
		ActivityManager.getInstance().postEvent(new SoldierNumChangeEvent(player.getId(), armyId, 0 - fireCnt));
		MissionManager.getInstance().postMsg(player, new EventSoldierAdd(armyId, fireCnt, 0));
        // 我要变强士兵数量变更
        StrengthenGuideManager.getInstance().postMsg( new SGPlayerSoldierNumChangeMsg(player));
	}
	
}
