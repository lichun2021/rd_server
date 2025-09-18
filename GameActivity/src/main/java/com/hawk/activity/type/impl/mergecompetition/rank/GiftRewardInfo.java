package com.hawk.activity.type.impl.mergecompetition.rank;

import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

/**
 * 嘉奖礼包信息
 */
public class GiftRewardInfo implements SplitEntity {
	
	private int rewardId;
	
	private int count;
	
	private long time;
	
	private String playerId = "";
	
	@Override
	public SplitEntity newInstance() {
		return new GiftRewardInfo();
	}
	
	public static GiftRewardInfo valueOf(int rewardId, int count, long time, String playerId) {
		GiftRewardInfo info = new GiftRewardInfo();
		info.rewardId = rewardId;
		info.count = count;
		info.time = time;
		info.playerId = playerId;
		return info;
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(rewardId);
		dataList.add(count);
		dataList.add(time);
		dataList.add(playerId);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(4);
		rewardId = dataArray.getInt();
		count = dataArray.getInt();
		time = dataArray.getLong();
		playerId = dataArray.getString();
	}

	public int getRewardId() {
		return rewardId;
	}

	public void setGiftId(int rewardId) {
		this.rewardId = rewardId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String toString() {
		return SerializeHelper.toSerializeString(this, SerializeHelper.COLON_ITEMS);
	}
	
	public static GiftRewardInfo parseObj(String str) {
		return SerializeHelper.getValue(GiftRewardInfo.class, str, SerializeHelper.COLON_ITEMS);
	}
}
