package com.hawk.game.service.tiberium;

import java.util.HashSet;
import java.util.Set;

/**
 * 联赛积分数据
 * 
 * @author admin
 *
 */
public class TLWScoreData {

	public String id;

	public long score;

	// 个人
	public Set<Integer> rewardedList;
	
	// 已领取的联盟积分奖励
	public Set<Integer> guildRewardeds;

	public TLWScoreData() {
		rewardedList = new HashSet<>();
		guildRewardeds = new HashSet<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}

	public Set<Integer> getRewardedList() {
		return rewardedList;
	}

	public void setRewardedList(Set<Integer> rewardedList) {
		this.rewardedList = rewardedList;
	}

	public Set<Integer> getGuildRewardeds() {
		return guildRewardeds;
	}

	public void setGuildRewardeds(Set<Integer> guildRewardeds) {
		this.guildRewardeds = guildRewardeds;
	}

}
