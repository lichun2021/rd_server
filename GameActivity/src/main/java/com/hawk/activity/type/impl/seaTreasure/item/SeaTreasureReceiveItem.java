package com.hawk.activity.type.impl.seaTreasure.item;

import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 秘海寻珍接受宝箱信息
 * @author Golden
 *
 */
public class SeaTreasureReceiveItem implements SplitEntity {
	
	/**
	 * 奖励id
	 */
	private int advanceRewardId;
	
	/**
	 * 奖励获取时间
	 */
	private long receiveTime;
	
	/**
	 * 剩余可获取次数
	 */
	private int remainTimes;

	public SeaTreasureReceiveItem() {
		
	}
	
	@Override
	public SplitEntity newInstance() {
		return new SeaTreasureReceiveItem();
	}

	public int getAdvanceRewardId() {
		return advanceRewardId;
	}

	public void setAdvanceRewardId(int advanceRewardId) {
		this.advanceRewardId = advanceRewardId;
	}

	public long getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(long receiveTime) {
		this.receiveTime = receiveTime;
	}

	public int getRemainTimes() {
		return remainTimes;
	}

	public void setRemainTimes(int remainTimes) {
		this.remainTimes = remainTimes;
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(advanceRewardId);
		dataList.add(receiveTime);
		dataList.add(remainTimes);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		advanceRewardId = dataArray.getInt();
		receiveTime = dataArray.getLong();
		remainTimes = dataArray.getInt();
	}
	
	@Override
	public String toString() {
		return advanceRewardId + "_" + receiveTime + "_" + remainTimes;
	}
}
