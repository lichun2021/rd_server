package com.hawk.activity.type.impl.starInvest;


import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class StarInvestExploreCell  implements SplitEntity{
	
	private int cellId;
	
	private long startTime;
	
	private int boxId;

	private int speed;
	
	private int boxAdvance;
	
	public int getCellId() {
		return cellId;
	}
	
	public void setCellId(int cellId) {
		this.cellId = cellId;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	
	public int getBoxId() {
		return boxId;
	}
	
	public void setBoxId(int boxId) {
		this.boxId = boxId;
	}
		
	public int getSpeed() {
		return speed;
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	public int getBoxAdvance() {
		return boxAdvance;
	}
	
	public void setBoxAdvance(int boxAdvance) {
		this.boxAdvance = boxAdvance;
	}
	
	
	public void addSpeed(int use){
		this.speed += use;
	}
	

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(this.cellId);
		dataList.add(this.startTime);
		dataList.add(this.boxId);
		dataList.add(this.speed);
		dataList.add(this.boxAdvance);
		
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		this.cellId = dataArray.getInt();
		this.startTime = dataArray.getLong();
		this.boxId =  dataArray.getInt();
		this.speed =  dataArray.getInt();
		this.boxAdvance = dataArray.getInt();
	}

	@Override
	public SplitEntity newInstance() {
		return new StarInvestExploreCell();
	}

}
