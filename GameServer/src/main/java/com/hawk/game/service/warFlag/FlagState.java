package com.hawk.game.service.warFlag;

import com.hawk.game.service.WarFlagService;

import org.slf4j.Logger;

public interface FlagState {
	
	/**
	 * 状态tick
	 */
	public void stateTick();
	
	/**
	 * 资源产出tick
	 */
	public void resTick();
	
	/**
	 * 是否有领地(是否是建造完成以后的状态)
	 */
	public boolean hasManor();
	
	/**
	 * 获取当前建筑值
	 */
	public int getCurrBuildLife();

	/**
	 * 获取当前占领值
	 */
	public int getCurrOccupyLife();
	
	/**
	 * 行军返回
	 */
	public void marchReturn();
	
	/**
	 * 行军到达
	 */
	public void marchReach(String guildId);

	/**
	 * 是否已经建造完成
	 */
	public boolean isBuildComplete();
	
	/**
	 * 是否已经放置
	 */
	public boolean isPlaced();
	
	/**
	 * 是否可以收回
	 */
	public boolean canTakeBack();

	/**
	 * 是否可以放置
	 */
	public boolean canPlace();
	
	/**
	 * 母旗是否可以tick
	 */
	public boolean canCenterTick();
	
	/**
	 * 日志
	 */
	default Logger getLogger() {
		return WarFlagService.logger;
	}
}
