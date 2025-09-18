package com.hawk.activity.type.impl.achieve.entity;

import java.util.ArrayList;
import java.util.List;

import com.hawk.activity.type.impl.achieve.AchieveManager;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.datatype.AchieveData;
import com.hawk.game.protocol.Activity.AchieveItemPB;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

/**
 * 成就数据
 * @author PhilChen
 *
 */
public class AchieveItem implements SplitEntity {
	
	/** 成就id*/
	private int achieveId;
	
	/** 成就状态*/
	private int state;

	/** 当前成就数据列表，根据不同的成就类型进行不同的数据格式存储*/
	private List<Integer> dataList;
	
	public AchieveItem() {
		dataList = new ArrayList<>();
	}
	
	public static AchieveItem valueOf(int achieveId) {
		AchieveItem data = new AchieveItem();
		data.achieveId = achieveId;
		data.state = AchieveState.NOT_ACHIEVE_VALUE;
		return data;
	}
	
	public List<Integer> getDataList() {
		return dataList;
	}

	public void setValue(int index, int value) {
		if (dataList.size() <= index) {
			for (int i = dataList.size(); i <= index; i++) {
				dataList.add(0);
			}
		}
		dataList.set(index, value);
	}

	public int getValue(int index) {
		if (dataList.size() <= index) {
			return 0;
		}
		return dataList.get(index);
	}
	
	public int getAchieveId() {
		return achieveId;
	}

	public void setAchieveId(int achieveId) {
		this.achieveId = achieveId;
	}

	public void setDataList(List<Integer> dataList) {
		this.dataList = dataList;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public int getState() {
		return state;
	}
	
	@Override
	public AchieveItem newInstance() {
		return new AchieveItem();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(achieveId);
		dataList.add(state);
		dataList.add(SerializeHelper.collectionToString(this.dataList, SerializeHelper.BETWEEN_ITEMS));
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		achieveId = dataArray.getInt();
		state = dataArray.getInt();
		String achieveDataStr = dataArray.getString();
		this.dataList = SerializeHelper.stringToList(Integer.class, achieveDataStr, SerializeHelper.BETWEEN_ITEMS);
	}

	/**
	 * 重置数据
	 */
	public void reset() {
		this.state = AchieveState.NOT_ACHIEVE_VALUE;
		if (this.dataList != null) {
			this.dataList.clear();
		}
	}

	@Override
	public String toString() {
		return "AchieveItem [achieveId=" + achieveId + ", state=" + state + ", dataList=" + dataList + "]";
	}
	
	public AchieveItem getCopy(){
		AchieveItem copy = new AchieveItem();
		copy.setAchieveId(this.achieveId);
		copy.setState(this.state);
		List<Integer> dataCopy = new ArrayList<>();
		dataCopy.addAll(dataList);
		copy.setDataList(dataCopy);
		return copy;
	}
	
	public AchieveItemPB.Builder createAchieveItemPB() {
		AchieveItemPB.Builder builder = AchieveItemPB.newBuilder();
		builder.setAchieveId(this.getAchieveId());
		builder.setState(this.getState());
		AchieveConfig achieveConfig = AchieveManager.getInstance().getAchieveConfig(this.getAchieveId());
		if (achieveConfig != null) {
			AchieveData achieveData = achieveConfig.getAchieveType().getAchieveData();
			builder.setValue(achieveData.getShowValue(this));
		} else {
			builder.setValue(0);
		}
		return builder;
	}
}
