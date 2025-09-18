package com.hawk.activity.type.impl.equipCraftsman.item;

import java.util.List;

import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 装备工匠,词条item
 * 
 * @author Golden
 *
 */
public class EquipCarftsmanItem implements SplitEntity {

	/**
	 * uuid
	 */
	private String uuid;
	
	/**
	 * 抽取的id
	 */
	private int gachaId;
	
	/**
	 * 抽取时间
	 */
	private long gachaTime;

	public static EquipCarftsmanItem valueOf(int gachaId) {
		EquipCarftsmanItem item = new EquipCarftsmanItem();
		item.setUuid(HawkUUIDGenerator.genUUID());
		item.setGachaId(gachaId);
		item.setGachaTime(HawkTime.getMillisecond());
		return item;
	}
	
	@Override
	public SplitEntity newInstance() {
		return new EquipCarftsmanItem();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(uuid);
		dataList.add(gachaId);
		dataList.add(gachaTime);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(4);
		uuid = dataArray.getString();
		gachaId = dataArray.getInt();
		gachaTime = dataArray.getLong();
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getGachaId() {
		return gachaId;
	}

	public void setGachaId(int gachaId) {
		this.gachaId = gachaId;
	}

	public long getGachaTime() {
		return gachaTime;
	}

	public void setGachaTime(long gachaTime) {
		this.gachaTime = gachaTime;
	}
}
