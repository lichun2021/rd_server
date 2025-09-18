package com.hawk.game.service.mssion;

import com.hawk.game.module.obelisk.service.ObeliskService;
import com.hawk.game.msg.ObeliskMissionRefreshMsg;
import org.hawk.app.HawkApp;

import com.hawk.game.global.GlobalData;
import com.hawk.game.msg.MissionMsg;
import com.hawk.game.msg.SuperSoldierTriggeTaskMsg;
import com.hawk.game.player.Player;
import org.hawk.task.HawkTaskManager;

/**
 * 任务管理器
 * 
 * @author golden
 *
 */
public class MissionManager {

	private static MissionManager instance;

	public static MissionManager getInstance() {
		if (instance == null) {
			instance = new MissionManager();
		}
		return instance;
	}

	/**
	 * 初始化
	 * @return
	 */
	public boolean init() {
		return true;
	}
	
	/**
	 * 任务事件处理
	 * 
	 * @param event
	 */
	public void postMsg(String playerId, MissionEvent event) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		postMsg(player, event);
	}
	
	/**
	 * 任务事件处理
	 * 
	 * @param event
	 */
	public void postMsg(Player player, MissionEvent event) {
		HawkApp.getInstance().postMsg(player, MissionMsg.valueOf(event));

		//方尖碑刷新任务
		if(ObeliskService.getInstance().inOpen()){
			ObeliskMissionRefreshMsg missMsg = ObeliskMissionRefreshMsg.valueOf(player,event);
			HawkTaskManager.getInstance().postMsg(ObeliskService.getInstance().getXid(), missMsg);
		}
	}
	
	/**
	 * 转发机甲解锁任务消息
	 * @param player
	 * @param msg
	 */
	public void postSuperSoldierTaskMsg(Player player, SuperSoldierTriggeTaskMsg msg) {
		if (!player.getAllSuperSoldier().isEmpty()) {
			return;
		}
		
		HawkApp.getInstance().postMsg(player, msg);
	}
}
