package com.hawk.game.entity.item;

import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class WishingCountItem implements SplitEntity {

	private int resourceType;
	
	private int freeCount;
	
	private int costCount;
	
	private int extraCount;
	
	public WishingCountItem() {
	}
	
	public WishingCountItem(int resourceType) {
		this.resourceType = resourceType;
	}

	public int getResourceType() {
		return resourceType;
	}

	public int getFreeCount() {
		return freeCount;
	}

	public void setFreeCount(int freeCount) {
		this.freeCount = freeCount;
	}

	public int getCostCount() {
		return costCount;
	}

	public void setCostCount(int costCount) {
		this.costCount = costCount;
	}

	public void addFreeCount(int count) {
		this.freeCount += count;
	}
	
	public void addCostCount(int count) {
		this.costCount += count;
	}

	public int getExtraCount() {
		return extraCount;
	}

	public void addExtraCount(int count) {
		this.extraCount += count;
	}

	@Override
	public SplitEntity newInstance() {
		return new WishingCountItem();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(resourceType);
		dataList.add(freeCount);
		dataList.add(costCount);
		dataList.add(extraCount);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(4);
		resourceType = dataArray.getInt();
		freeCount = dataArray.getInt();
		costCount = dataArray.getInt();
		extraCount = dataArray.getInt();
	}
}
