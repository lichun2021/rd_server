package com.hawk.game.gmscript;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.config.BaseInitCfg;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.MissionCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.MissionEntity;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.StoryMissionEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.PlayerStoryMissionModule;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.cache.PlayerDataCache;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.MissionState;

/**
 * 玩家数据修复脚本
 * 
 * localhost:8080/script/repairData?opType=2&missionId=2103101&playerId=7ps-m3ut5-1
 * 
 * @param playerId
 * @param opType     // 修复类型
 * @param missionId  // 任务数据配置ID
 *
 * @author lating
 * 
 */
public class PlayerDataRepairHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
		}
		
		int opType = Integer.parseInt(params.get("opType"));
		if (opType == 1) {
			incorrectPlayerInitRepair(player, Long.parseLong(params.get("time")));
		} else if (opType == 2) {
			// 2103101 解锁地块
			missionDataRepair(player, Integer.parseInt(params.get("missionId")));
		} else if(opType == 3){
			//数据脏了，库里出现2条玩家的剧情数据,取数据添加排序，取最早创建的一条
			storyMissionDataRepair(player);
		}
		
		return HawkScript.successResponse("");
	}
	
	
	/**
	 * 重新加载一下数据给玩家
	 * @param player
	 */
	protected void storyMissionDataRepair(Player player){
		//查库
		StoryMissionEntity entity = (StoryMissionEntity) PlayerDataKey.StoryMissionEntity.load(player.getId(), false);
		player.getData().getDataCache().update(PlayerDataKey.StoryMissionEntity, entity);
		PlayerStoryMissionModule module = player.getModule(GsConst.ModuleType.STORY_MISSSION);
		Method method = HawkOSOperator.getClassMethod(module, "onPlayerLogin");
		try {
			method.invoke(module);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 修复任务数据
	 * 
	 * @param player
	 */
	protected void missionDataRepair(Player player, int missionId) {
		
		HawkLog.logPrintln("missionDataRepair start, playerId: {}", player.getId());
		
		List<MissionEntity> missionEntities = player.getData().getMissionEntities();
		for (MissionEntity mission : missionEntities) {
			if (mission.getCfgId() != missionId) {
				continue;
			}
			
			MissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MissionCfg.class, missionId);
			mission.setNum(cfg.getFunVal());
			mission.setState(MissionState.STATE_FINISH);
			HawkLog.logPrintln("missionDataRepair success, playerId: {}, data: {}", player.getId(), mission);
		}
	}
	
	/**
	 * 玩家初始化还原处理
	 */
	protected void incorrectPlayerInitRepair(Player player, long time) {
		if (time > HawkApp.getInstance().getCurrentTime()) {
			HawkLog.logPrintln("incorrectPlayerInitRepair param invalid, playerId: {}, time: {}", player.getId(), HawkTime.formatTime(time));
			return;
		}
		
		PlayerData playerData = player.getData();
		List<BuildingBaseEntity> buildings = playerData.getBuildingEntitiesIgnoreStatus();
		int mainBuildingCount = 0;
		for (BuildingBaseEntity building : buildings) {
			if (building.getType() == BuildingType.CONSTRUCTION_FACTORY_VALUE) {
				mainBuildingCount ++;
			}
		}
		
		String playerId = player.getId();
		if (mainBuildingCount <= 1) {
			HawkLog.logPrintln("incorrectPlayerInitRepair handler failed, playerId: {}, mainBuildingCount: {}", playerId, mainBuildingCount);
			return;
		}
		
		if (player.isActiveOnline()) {
			player.kickout(Status.IdipMsgCode.IDIP_CHANGE_BUILD_LEVEL_VALUE, true, null);
		}
		
		// 1、 建筑
		ConfigIterator<BaseInitCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(BaseInitCfg.class);
		List<BuildingBaseEntity> removeBuildings = new ArrayList<BuildingBaseEntity>();
		while (iterator.hasNext()) {
			BaseInitCfg cfg = iterator.next();
			int buildCfgId = cfg.getBuildId();
			Iterator<BuildingBaseEntity> it = buildings.iterator();
			BuildingBaseEntity repairBuilding = null;
			int buildingCfgId = 0;
			while (it.hasNext()) {
				BuildingBaseEntity building = it.next();
				if (building.getType() == buildCfgId / 100) {
					if (building.getCreateTime() > time && buildingCfgId == 0) {
						buildingCfgId = building.getBuildingCfgId();
						removeBuildings.add(building);
						HawkDBManager.getInstance().executeUpdate("delete from building where id = ?", building.getId());
						HawkLog.logPrintln("incorrectPlayerInitRepair handler delete building, playerId: {}, buildingCfgId: {}", playerId, building.getBuildingCfgId());
					} else {
						repairBuilding = building;
					}
				}
			}
			
			if (repairBuilding != null && repairBuilding.getBuildingCfgId() < buildingCfgId) {
				HawkLog.logPrintln("incorrectPlayerInitRepair handler repair building, playerId: {}, old cfgId: {}, new CfgId: {}", playerId, repairBuilding.getBuildingCfgId(), buildingCfgId);
				repairBuilding.setBuildingCfgId(buildingCfgId);
			}
		}
		
		buildings.removeAll(removeBuildings);
		
		// 2、部队
		int[] soldiers = ConstProperty.getInstance().getInitSoldiers();
		if (soldiers != null && soldiers.length >= 2) {
			List<ArmyEntity> playerArmyData = playerData.getArmyEntities();
			for (int i = 0; i < soldiers.length / 2; i++) {
				int armyId = soldiers[i * 2];
				BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
				if (soldierCfg == null) {
					continue;
				}
				
				int count = 0;
				Iterator<ArmyEntity> it = playerArmyData.iterator();
				while (it.hasNext()) {
					ArmyEntity army = it.next();
					if (army.getArmyId() == armyId && army.getCreateTime() > time && count == 0) {
						it.remove();
						count++;
						HawkDBManager.getInstance().executeUpdate("delete from army where id = ?", army.getId());
						HawkLog.logPrintln("incorrectPlayerInitRepair handler delete army, playerId: {}, armyId: {}", playerId, army.getArmyId());
					}
				}
			}
		}
		
		// 3、 任务
		List<MissionEntity> missionList = player.getData().getMissionEntities();
		List<MissionEntity> removeMissions = new ArrayList<MissionEntity>();
		for(Integer cfgId : MissionCfg.getInitMissions()) {
			Iterator<MissionEntity> it = missionList.iterator();
			int count = 0;
			while (it.hasNext()) {
				MissionEntity mission = it.next();
				if (mission.getCfgId() == cfgId && mission.getCreateTime() > time && count == 0) {
					removeMissions.add(mission);
					count++;
					HawkDBManager.getInstance().executeUpdate("delete from mission where id = ?", mission.getId());
					HawkLog.logPrintln("incorrectPlayerInitRepair handler delete mission, playerId: {}, cfgId: {}", playerId, mission.getCfgId());
				}
			}
		}
		
		missionList.removeAll(removeMissions);
		
		// 4、 队列
		List<QueueEntity> queueEntities = playerData.getQueueEntities();
		Iterator<QueueEntity> it = queueEntities.iterator();
		int count = GsConst.REUSABLE_QUEUE_COUNT;
		while (it.hasNext()) {
			QueueEntity queue = it.next();
			if (queue.getCreateTime() > time && count > 0) {
				if (queue.getReusage() != GsConst.QueueReusage.FREE.intValue()) {
					QueueEntity freeQueue = playerData.getFreeQueue(0);
					freeQueue.setQueueType(queue.getQueueType());
					freeQueue.setStatus(queue.getStatus());
					freeQueue.setStartTime(queue.getStartTime());
					freeQueue.setEndTime(queue.getEndTime());
					freeQueue.setTotalQueueTime(queue.getTotalQueueTime());
					freeQueue.setItemId(queue.getItemId());
					freeQueue.setBuildingType(queue.getBuildingType());
					freeQueue.setReusage(queue.getReusage()); // 可重用队列
					freeQueue.setHelpTimes(queue.getHelpTimes());
					freeQueue.setTotalReduceTime(queue.getTotalReduceTime());
					freeQueue.setCancelBackRes(queue.getCancelBackRes());
				}
				
				it.remove();
				count --;
				HawkDBManager.getInstance().executeUpdate("delete from queue where id = ?", queue.getId());
				HawkLog.logPrintln("incorrectPlayerInitRepair handler delete queue, playerId: {}, queue: {}", playerId, queue.toString());
			}
		}
		
		PlayerBaseEntity newBaseEntity = player.getPlayerBaseEntity();
		// 5、指挥官等级
		List<PlayerBaseEntity> playerBaseEntities = HawkDBManager.getInstance().query(
				"from PlayerBaseEntity where playerId = ? and invalid = 0", playerId);
		if (playerBaseEntities == null || playerBaseEntities.size() == 0) {
			HawkLog.logPrintln("incorrectPlayerInitRepair handler update playerBaseEntity failed, playerId: {}", playerId);
			return;
		}
		
		PlayerBaseEntity playerBaseEntity = playerBaseEntities.get(0);
		repairPlayerBaseEntity(playerBaseEntity, newBaseEntity);
		
		PlayerDataCache dataCache = HawkOSOperator.getFieldValue(playerData, "dataCache");
		dataCache.update(PlayerDataKey.PlayerBaseEntity, playerBaseEntity);
		
		HawkLog.logPrintln("incorrectPlayerInitRepair handler success, playerId: {}", playerId);
		
	}
	
	private void repairPlayerBaseEntity(PlayerBaseEntity playerBaseEntity, PlayerBaseEntity newBaseEntity) {
		if (newBaseEntity.getLevel() > playerBaseEntity.getLevel()) {
			playerBaseEntity.setLevel(newBaseEntity.getLevel());
		}
		
		if (newBaseEntity._getChargeAmt()> playerBaseEntity._getChargeAmt()) {
			playerBaseEntity._setChargeAmt(newBaseEntity._getChargeAmt());
		}
		
		if (newBaseEntity.getDiamonds() > playerBaseEntity.getDiamonds()) {
			playerBaseEntity.setDiamonds(newBaseEntity.getDiamonds());
		}
		
		if (newBaseEntity.getRecharge() > playerBaseEntity.getRecharge()) {
			playerBaseEntity.setRecharge(newBaseEntity.getRecharge());
		}
		
		if (newBaseEntity.getSaveAmt() > playerBaseEntity.getSaveAmt()) {
			playerBaseEntity.setSaveAmt(newBaseEntity.getSaveAmt());
		}

		if (newBaseEntity.getGold() > playerBaseEntity.getGold()) {
			playerBaseEntity.setGold(newBaseEntity.getGold());
		}
		
		if (newBaseEntity.getExp() > playerBaseEntity.getExp()) {
			playerBaseEntity.setExp(newBaseEntity.getExp());
		}
		
		if (newBaseEntity.getExpDec() > playerBaseEntity.getExpDec()) {
			playerBaseEntity.setExpDec(newBaseEntity.getExpDec());
		}
		
		if (newBaseEntity.getGoldoreUnsafe() > playerBaseEntity.getGoldoreUnsafe()) {
			playerBaseEntity.setGoldoreUnsafe(newBaseEntity.getGoldoreUnsafe());
		}
		
		if (newBaseEntity.getOilUnsafe() > playerBaseEntity.getOilUnsafe()) {
			playerBaseEntity.setOilUnsafe(newBaseEntity.getOilUnsafe());
		}
		
		if (newBaseEntity.getSteelUnsafe() > playerBaseEntity.getSteelUnsafe()) {
			playerBaseEntity.setSteelUnsafe(newBaseEntity.getSteelUnsafe());
		}
		
		if (newBaseEntity.getTombarthiteUnsafe() > playerBaseEntity.getTombarthiteUnsafe()) {
			playerBaseEntity.setTombarthiteUnsafe(newBaseEntity.getTombarthiteUnsafe());
		}
		
		if (newBaseEntity.getGoldore() > playerBaseEntity.getGoldore()) {
			playerBaseEntity.setGoldore(newBaseEntity.getGoldore());
		}
		
		if (newBaseEntity.getOil() > playerBaseEntity.getOil()) {
			playerBaseEntity.setOil(newBaseEntity.getOil());
		}
		
		if (newBaseEntity.getSteel() > playerBaseEntity.getSteel()) {
			playerBaseEntity.setSteel(newBaseEntity.getSteel());
		}
		
		if (newBaseEntity.getTombarthite() > playerBaseEntity.getTombarthite()) {
			playerBaseEntity.setTombarthite(newBaseEntity.getTombarthite());
		}
		
		if (newBaseEntity.getCityDefVal() > playerBaseEntity.getCityDefVal()) {
			playerBaseEntity.setCityDefVal(newBaseEntity.getCityDefVal());
			playerBaseEntity.setCityDefConsumeTime(newBaseEntity.getCityDefConsumeTime());
			playerBaseEntity.setCityDefNextRepairTime(newBaseEntity.getCityDefNextRepairTime());
		}
		
		if (newBaseEntity.getGuildContribution() > playerBaseEntity.getGuildContribution()) {
			playerBaseEntity.setGuildContribution(newBaseEntity.getGuildContribution());
		}
		
		playerBaseEntity.assemble();
	}
}
