package com.hawk.activity.type.impl.newbietrain.entity;

import java.util.List;

import com.hawk.game.protocol.Activity.NoviceTrainRecord;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 *  作训记录数据
 */
public class TrainRecordInfo implements SplitEntity {
	/**
	 * 获得的物品ID
	 */
	private int itemId;
	/**
	 * 获得的物品数量
	 */
	private int count;
	/**
	 * 获得物品的时间
	 */
	private int time;
	
	public static TrainRecordInfo valueOf(int itemId, int count, int time) {
		TrainRecordInfo obj = new TrainRecordInfo();
		obj.setItemId(itemId);
		obj.setCount(count);
		obj.setTime(time);
		return obj;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public int getTime() {
		return time;
	}
	
	public void setTime(int time) {
		this.time = time;
	}
	
	@Override
	public TrainRecordInfo newInstance() {
		return new TrainRecordInfo();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(itemId);
		dataList.add(count);
		dataList.add(time);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		itemId = dataArray.getInt();
		count = dataArray.getInt();
		time = dataArray.getInt();
	}
	
	@Override
	public String toString() {
		return String.format("%d_%d_%d", itemId, count, time);
	}
	
	public NoviceTrainRecord.Builder toBuilder() {
		NoviceTrainRecord.Builder builder = NoviceTrainRecord.newBuilder();
		builder.setItemId(itemId);
		builder.setCount(count);
		builder.setTime(time * 1000L);
		return builder;
	}
}
