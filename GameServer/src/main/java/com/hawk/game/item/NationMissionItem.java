package com.hawk.game.item;

import java.util.List;

import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 国家任务
 * 
 * @author Golden
 *
 */
public class NationMissionItem extends MissionEntityItem implements SplitEntity {

	private long receiveTime;

	private String uuid;
	
	public NationMissionItem() {
		
	}
	
	public NationMissionItem(int cfgId, int value, int state) {
		super(cfgId, value, state);
		this.receiveTime = 0;
		this.uuid = "";
	}

	public NationMissionItem(int cfgId, int value, int state, long receiveTime, String uuid) {
		super(cfgId, value, state);
		this.receiveTime = receiveTime;
		this.uuid = uuid;
	}

	public long getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(long receiveTime) {
		this.receiveTime = receiveTime;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public SplitEntity newInstance() {
		return new NationMissionItem(0, 0, 0);
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(cfgId);
		dataList.add(value);
		dataList.add(state);
		dataList.add(receiveTime);
		dataList.add(uuid);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(5);
		cfgId = dataArray.getInt();
		value = dataArray.getInt();
		state = dataArray.getInt();
		receiveTime = dataArray.getLong();
		uuid = dataArray.getString();
	}

	@Override
	public String toString() {
		return cfgId + "_" + value + "_" + state + "_" + receiveTime + "_" + uuid;
	}
}