package com.hawk.activity.type.impl.seaTreasure.item;

import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 秘海寻珍宝箱
 * @author Golden
 *
 */
public class SeaTreasureBoxItem implements SplitEntity {

	/**
	 * 宝箱格子id
	 */
	private int grid;
	
	/**
	 * 宝箱奖励id
	 */
	private int advanceRewardId;
	
	/**
	 * 宝箱获取时间
	 */
	private long createTime;
	
	/**
	 * 宝箱开始挖掘时间
	 */
	private long startTime;

	public SeaTreasureBoxItem() {
		
	}
	
	@Override
	public SplitEntity newInstance() {
		return new SeaTreasureBoxItem();
	}

	public int getGrid() {
		return grid;
	}

	public void setGrid(int grid) {
		this.grid = grid;
	}

	public int getAdvanceRewardId() {
		return advanceRewardId;
	}

	public void setAdvanceRewardId(int advanceRewardId) {
		this.advanceRewardId = advanceRewardId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(grid);
		dataList.add(advanceRewardId);
		dataList.add(createTime);
		dataList.add(startTime);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(4);
		grid = dataArray.getInt();
		advanceRewardId = dataArray.getInt();
		createTime = dataArray.getLong();
		startTime = dataArray.getLong();
	}
	
	@Override
	public String toString() {
		return grid + "_" + advanceRewardId + "_" + createTime + "_" + startTime;
	}
}