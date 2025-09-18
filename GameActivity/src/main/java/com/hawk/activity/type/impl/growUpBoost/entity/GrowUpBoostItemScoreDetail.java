package com.hawk.activity.type.impl.growUpBoost.entity;

import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class GrowUpBoostItemScoreDetail  implements SplitEntity{
	
	
	private int itemId;
	
	private int itemNum;
	
	private int score;

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getItemNum() {
		return itemNum;
	}

	public void setItemNum(int itemNum) {
		this.itemNum = itemNum;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	@Override
	public SplitEntity newInstance() {
		return new GrowUpBoostItemScoreDetail();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(this.itemId);
		dataList.add(this.itemNum);
		dataList.add(this.score);
		
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		this.itemId = dataArray.getInt();
		this.itemNum = dataArray.getInt();
		this.score =  dataArray.getInt();
		
	}
	
	

}
