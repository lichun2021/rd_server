package com.hawk.activity.type.impl.backFlow.backGift;

import java.util.List;

import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class BackGift implements SplitEntity {

	
	private int poolType;
	
	private int itemType;
	
	private int itemId;
	
	private long itemCount;
	
	public BackGift() {
		
	}
	
	

	public BackGift(int poolType, int itemType, int itemId, long itemCount) {
		super();
		this.poolType = poolType;
		this.itemType = itemType;
		this.itemId = itemId;
		this.itemCount = itemCount;
	}

	

	

	

	public int getPoolType() {
		return poolType;
	}



	public void setPoolType(int poolType) {
		this.poolType = poolType;
	}



	public int getItemType() {
		return itemType;
	}

	public void setItemType(int itemType) {
		this.itemType = itemType;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public long getItemCount() {
		return itemCount;
	}

	public void setItemCount(long itemCount) {
		this.itemCount = itemCount;
	}



	@Override
	public SplitEntity newInstance() {
		return new BackGift();
	}



	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(this.poolType);
		dataList.add(this.itemType);
		dataList.add(this.itemId);
		dataList.add(this.itemCount);
	}



	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(4);
		this.poolType = dataArray.getInt();
		this.itemType = dataArray.getInt();
		this.itemId = dataArray.getInt();
		this.itemCount = dataArray.getInt();
	}
	
	
	@Override
	public String toString() {
		return "[poolType=" + poolType + "itemType=" + itemType+ ", itemId=" + itemId + ", itemCount=" + itemCount + "]";
	}
	
	
	public BackGift copy(){
		BackGift copy = new BackGift(this.itemType, this.itemType, this.itemId, this.itemCount);
		return copy;
	}
	
	
	public RewardItem.Builder getRewardBuilder(){
		RewardItem.Builder item = RewardItem.newBuilder();
		item.setItemType(this.itemType);
		item.setItemId(this.itemId);
		item.setItemCount(this.itemCount);
		return item;
	}
	
	
	
}
