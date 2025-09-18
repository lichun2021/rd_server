package com.hawk.game.item;

import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 世界点增益状态显示
 * @author golden
 *
 */
public class BaseShowItem implements SplitEntity {

	private int buffId;
	
	private int startTime;
	
	private int endTime;
	
	public BaseShowItem() {
		
	}
	
	public BaseShowItem(int buffId, long startTime, long endTime) {
		this.buffId = buffId;
		this.startTime = (int)(startTime / 1000);
		this.endTime = (int)(endTime / 1000);
	}

	public int getBuffId() {
		return buffId;
	}
	
	public long getStartTime() {
		return startTime * 1000L;
	}

	public long getEndTime() {
		return endTime * 1000L;
	}
	
	public String toString() {
		return String.format("%d_%d_%d", buffId, startTime, endTime);
	}
	
	public BaseShowItem valueOf(String info) {
		String[] item = info.split("_");
		return new BaseShowItem(Integer.parseInt(item[0]), Integer.parseInt(item[1]), Integer.parseInt(item[2]));
	}

	@Override
	public SplitEntity newInstance() {
		return new BaseShowItem();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(buffId);
		dataList.add(startTime);
		dataList.add(endTime);
		
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		buffId = dataArray.getInt();
		startTime = dataArray.getInt();
		endTime = dataArray.getInt();
	}
}
