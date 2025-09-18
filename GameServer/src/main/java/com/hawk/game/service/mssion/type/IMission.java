package com.hawk.game.service.mssion.type;

import com.hawk.game.item.PlayerAchieveItem;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.util.GsConst.MissionState;

public interface IMission {

	/**
	 * 普通任务刷新
	 * @param missionEvent
	 */
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent);
	
	/**
	 * 刷新任务
	 * 
	 * @param eneity
	 *            实体
	 * @param cfg
	 *            配置
	 */
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg);
	
	/**
	 * 累计任务. 即使没有接到也要记录
	 * 
	 * @param cfg
	 *            配置
	 */
	default <T extends MissionEvent> void cumulativeMission(String playerId, T missionEvent){
		
	}

	/**
	 * 初始化任务
	 * 
	 * @param entityItem
	 * @param cfg
	 */
	default void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {

	}

	/**
	 * 检测任务完成
	 * 
	 * @param entityItem
	 * @param cfg
	 */
	default void checkMissionFinish(MissionEntityItem entityItem, MissionCfgItem cfg) {
		if (entityItem instanceof PlayerAchieveItem) {
			return;
		}
		if (entityItem.getValue() >= cfg.getValue() && entityItem.getState() == MissionState.STATE_NOT_FINISH) {
			entityItem.setState(MissionState.STATE_FINISH);
		}
	}
}
