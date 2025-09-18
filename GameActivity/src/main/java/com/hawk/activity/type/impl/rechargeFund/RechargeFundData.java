package com.hawk.activity.type.impl.rechargeFund;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 玩家待补发奖励信息
 * @author Jesse
 *
 */
public class RechargeFundData {
	public String playerId;

	private int termId;

	public Map<Integer, List<String>> rewardMap = new HashMap<>();

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public Map<Integer, List<String>> getRewardMap() {
		return rewardMap;
	}

	public void setRewardMap(Map<Integer, List<String>> rewardMap) {
		this.rewardMap = rewardMap;
	}

}
