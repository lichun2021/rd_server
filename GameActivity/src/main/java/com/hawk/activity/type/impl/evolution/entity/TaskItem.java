package com.hawk.activity.type.impl.evolution.entity;

import java.util.List;
import com.hawk.game.protocol.Activity.EvolutionTaskPB;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 任务数据
 * 
 * @author lating
 *
 */
public class TaskItem implements SplitEntity {
	
	/** 任务id*/
	private int taskId;
	
	/** 完成次数*/
	private int finishTimes;
	
	/** 任务值*/
	private long value;
	
	public TaskItem() {
	}
	
	public static TaskItem valueOf(int taskId) {
		TaskItem data = new TaskItem();
		data.taskId = taskId;
		return data;
	}
	
	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getFinishTimes() {
		return finishTimes;
	}

	public void setFinishTimes(int finishTimes) {
		this.finishTimes = finishTimes;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}
	
	@Override
	public SplitEntity newInstance() {
		return new TaskItem();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(taskId);
		dataList.add(finishTimes);
		dataList.add(value);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(4);
		taskId = dataArray.getInt();
		finishTimes = dataArray.getInt();
		value = dataArray.getLong();
	}

	/**
	 * 重置数据
	 */
	public void reset() {
		this.finishTimes = 0;
		this.value = 0;
	}

	@Override
	public String toString() {
		return "TaskItem [taskId=" + taskId + ", finishTimes=" + finishTimes + ", value=" + value + "]";
	}
	
	public TaskItem getCopy(){
		TaskItem copy = new TaskItem();
		copy.setTaskId(this.taskId);
		copy.setFinishTimes(this.finishTimes);
		copy.setValue(this.value);
		return copy;
	}
	
	/**
	 * 构建任务PB数据
	 * @return
	 */
	public EvolutionTaskPB build(){
		EvolutionTaskPB.Builder builder = EvolutionTaskPB.newBuilder();
		builder.setId(this.taskId);
		builder.setTimes(this.finishTimes);
		builder.setValue(this.value);
		return builder.build();
	}

}
