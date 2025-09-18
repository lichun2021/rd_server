package com.hawk.game.entity.item;

import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 装扮构造器
 * @author golden
 *
 */
public class DressItem implements SplitEntity {

	/**
	 * 装扮类型 -> 1:名牌 2:皮肤 3:聊天框
	 */
	private int dressType;
	
	/**
	 * 装扮具体类型
	 */
	private int modelType;
	
	/**
	 * 开始时间
	 */
	private long startTime;
	
	/**
	 * 持续时间
	 */
	private long continueTime;
	
	/**
	 * 外观展示类型
	 */
	private int showType;
	/**
	 * 外观展示结束时间（结束后显示为当前装扮的默认外观）
	 */
	private long showEndTime;
	
	
	public int getDressType() {
		return dressType;
	}

	public void setDressType(int dressType) {
		this.dressType = dressType;
	}

	public int getModelType() {
		return modelType;
	}

	public void setModelType(int modelType) {
		this.modelType = modelType;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getContinueTime() {
		return continueTime;
	}

	public void setContinueTime(long continueTime) {
		this.continueTime = continueTime;
	}
	
	public int getShowType() {
		return showType;
	}

	public void setShowType(int showType) {
		this.showType = showType;
	}
	
	public long getShowEndTime() {
		return showEndTime;
	}

	public void setShowEndTime(long showEndTime) {
		this.showEndTime = showEndTime;
	}

	@Override
	public SplitEntity newInstance() {
		return new DressItem();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(dressType);
		dataList.add(modelType);
		dataList.add(startTime);
		dataList.add(continueTime);
		dataList.add(showType);
		dataList.add(showEndTime);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(6);
		dressType = dataArray.getInt();
		modelType = dataArray.getInt();
		startTime = dataArray.getLong();
		continueTime = dataArray.getLong();
		showType = dataArray.getInt();
		showEndTime = dataArray.getLong();
	}

}
