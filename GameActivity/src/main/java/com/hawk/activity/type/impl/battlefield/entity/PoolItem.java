package com.hawk.activity.type.impl.battlefield.entity;

import java.util.ArrayList;
import java.util.List;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

/**
 * 奖池数据
 * @author lating
 *
 */
public class PoolItem implements SplitEntity {
	
	/** 奖池id */
	private int poolId;
	
	/** 奖励id列表 */
	private List<Integer> awardList;
	
	public PoolItem() {
		awardList = new ArrayList<Integer>();
	}
	
	public static PoolItem valueOf(int poolId) {
		PoolItem data = new PoolItem();
		data.poolId = poolId;
		return data;
	}
	
	public int getPoolId() {
		return poolId;
	}
	
	public List<Integer> getAwardList() {
		return awardList;
	}
	
	@Override
	public SplitEntity newInstance() {
		return new PoolItem();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(poolId);
		dataList.add(SerializeHelper.collectionToString(this.awardList, SerializeHelper.BETWEEN_ITEMS));
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(2);
		poolId = dataArray.getInt();
		String awardIdStr = dataArray.getString();
		this.awardList = SerializeHelper.stringToList(Integer.class, awardIdStr, SerializeHelper.BETWEEN_ITEMS);
	}

	@Override
	public String toString() {
		return "PoolItem [poolId=" + poolId + ", awardList=" + awardList + "]";
	}
	
}
