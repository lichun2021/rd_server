package com.hawk.game.service.mssion.type;

import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.GhostTowerCfg;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventGhostTowerPass;

@Mission(missionType = MissionType.GHOST_TOWER_PASS)
public class GhostTowerPassMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}
	
	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int stageId = playerData.getPlayerGhostTowerEntity().getStageId();
		GhostTowerCfg ghostCfg = HawkConfigManager.getInstance().
				getConfigByKey(GhostTowerCfg.class, stageId);
		if(ghostCfg == null){
			return;
		}
		List<Integer> conditions = cfg.getIds();
		int levelLimt = conditions.get(0);
		int floorLimit = cfg.getValue();
		
		int level = ghostCfg.getLevel();
		int floor = ghostCfg.getFloor();
		//层级不足
		if(level < levelLimt ){
			return;
		}
		//如果层级已经大要求，直接赋值
		if(level > levelLimt){
			entityItem.setValue(floorLimit);
			checkMissionFinish(entityItem, cfg);
			return;
		}
		//如果是同级，则比较层是否符合要求
		long curVal = entityItem.getValue();
		if(floor > curVal){
			entityItem.setValue(floor);
			checkMissionFinish(entityItem, cfg);
		}
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventGhostTowerPass event = (EventGhostTowerPass)missionEvent;
		List<Integer> conditions = cfg.getIds();
	
		int levelLimt = conditions.get(0);
		int floorLimit = cfg.getValue();
		
		int level = event.getLevel();
		int floor = event.getFloor();
		//层级不足
		if(level < levelLimt ){
			return;
		}
		//如果层级已经大要求，直接赋值
		if(level > levelLimt){
			entityItem.setValue(floorLimit);
			checkMissionFinish(entityItem, cfg);
			return;
		}
		//如果是同级，则比较层是否符合要求
		long curVal = entityItem.getValue();
		if(floor > curVal){
			entityItem.setValue(floor);
			checkMissionFinish(entityItem, cfg);
		}
		
	}
}