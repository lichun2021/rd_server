package com.hawk.game.world.thread.tasks;

import java.util.Set;

import org.hawk.log.HawkLog;

import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.thread.WorldTask;

public class CrossEndRemoveCityTask extends WorldTask {
	
	/**
	 * 玩家集合列表
	 */
	private Set<String> playerIdSet;
	public CrossEndRemoveCityTask(Set<String> playerIdSet, int taskType) {
		super(taskType);
		this.playerIdSet = playerIdSet;
	}
	@Override
	public boolean onInvoke() {
		HawkLog.logPrintln("cross activity end remove city size:{} ", playerIdSet.size());
		for (String playerId : playerIdSet) {
			WorldPlayerService.getInstance().removeCity(playerId, true);
		}
		
		return true;
	}

}
