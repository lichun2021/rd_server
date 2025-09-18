package com.hawk.activity.type.impl.growUpBoost.entity;

import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class GrowUpBoostScore  implements SplitEntity{

	
	private long dayZero;
	
	private int itemScore;
	
	private int achieveScore;
	
	
	

	

	public long getDayZero() {
		return dayZero;
	}

	public void setDayZero(long dayZero) {
		this.dayZero = dayZero;
	}

	public int getItemScore() {
		return itemScore;
	}

	public void setItemScore(int itemScore) {
		this.itemScore = itemScore;
	}

	public int getAchieveScore() {
		return achieveScore;
	}

	public void setAchieveScore(int achieveScore) {
		this.achieveScore = achieveScore;
	}
	
	
	@Override
	public SplitEntity newInstance() {
		return new GrowUpBoostScore();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(this.dayZero);
		dataList.add(this.achieveScore);
		dataList.add(this.itemScore);
		
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		this.dayZero = dataArray.getLong();
		this.achieveScore = dataArray.getInt();
		this.itemScore =  dataArray.getInt();
		
	}
	
}
