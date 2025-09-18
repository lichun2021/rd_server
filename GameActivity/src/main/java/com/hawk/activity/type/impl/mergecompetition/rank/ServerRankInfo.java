package com.hawk.activity.type.impl.mergecompetition.rank;

import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

/**
 * 区服积分排名信息
 */
public class ServerRankInfo implements SplitEntity {
	
	private int rank;
	
	private String rewards;
	
	private long rewardTime;
	
	private long score;
	
	@Override
	public SplitEntity newInstance() {
		return new ServerRankInfo();
	}
	
	public static ServerRankInfo valueOf(int rank, String rewards, long rewardTime, long score) {
		ServerRankInfo info = new ServerRankInfo();
		info.rank = rank;
		info.rewards = rewards;
		info.rewardTime = rewardTime;
		info.score = score;
		return info;
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(rank);
		dataList.add(rewards);
		dataList.add(rewardTime);
		dataList.add(score);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(4);
		rank = dataArray.getInt();
		rewards = dataArray.getString();
		rewardTime = dataArray.getLong();
		score = dataArray.getLong();
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getRewards() {
		return rewards;
	}

	public void setRewards(String rewards) {
		this.rewards = rewards;
	}

	public long getRewardTime() {
		return rewardTime;
	}

	public void setRewardTime(long rewardTime) {
		this.rewardTime = rewardTime;
	}
	
	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}

	public String toString() {
		return SerializeHelper.toSerializeString(this, SerializeHelper.COLON_ITEMS);
	}
	
	public static ServerRankInfo parseObj(String str) {
		return SerializeHelper.getValue(ServerRankInfo.class, str, SerializeHelper.COLON_ITEMS);
	}
}
