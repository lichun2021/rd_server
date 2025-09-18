package com.hawk.game.heroTrial.mission;

import java.util.List;

public interface IHeroTrialChecker {
	
	/**
	 * 是否触发任务
	 */
	public boolean touchMission(String playerId, List<Integer> heroIds, List<Integer> condition);
}
