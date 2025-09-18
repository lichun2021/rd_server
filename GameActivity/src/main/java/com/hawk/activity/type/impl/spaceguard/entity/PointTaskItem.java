package com.hawk.activity.type.impl.spaceguard.entity;

import java.util.List;

import com.hawk.game.protocol.Activity.GuildCoinTaskItemPB;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class PointTaskItem implements SplitEntity {
	
	/** 任务id*/
	private int taskId;
	
	/** 已获得代币数 */
	private int points;
	
	/** 任务值*/
	private long value;
	
	public PointTaskItem() {
	}
	
	public static PointTaskItem valueOf(int taskId) {
		PointTaskItem data = new PointTaskItem();
		data.taskId = taskId;
		return data;
	}
	
	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}
	
	@Override
	public SplitEntity newInstance() {
		return new PointTaskItem();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(taskId);
		dataList.add(points);
		dataList.add(value);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		taskId = dataArray.getInt();
		points = dataArray.getInt();
		value = dataArray.getInt();
	}

	@Override
	public String toString() {
		return "PointTaskItem [taskId=" + taskId + ", points=" + points + ", value=" + value + "]";
	}
	
	public GuildCoinTaskItemPB build(){
		GuildCoinTaskItemPB.Builder builder = GuildCoinTaskItemPB.newBuilder();
		builder.setId(this.taskId);
		builder.setPoints(points);
		builder.setValue(this.value);
		return builder.build();
	}

}
