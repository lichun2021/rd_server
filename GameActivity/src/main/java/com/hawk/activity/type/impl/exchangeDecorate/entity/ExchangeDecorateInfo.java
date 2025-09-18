package com.hawk.activity.type.impl.exchangeDecorate.entity;

import java.util.List;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class ExchangeDecorateInfo  implements SplitEntity {
	//id
	private int levelId;
	
	//state 1=未开启,2=可领取,3=已领取
	//2个结构 0开始代表记数
	private int state;
	
	public int getLevelId() {
		return levelId;
	}

	public void setLevelId(int levelId) {
		this.levelId = levelId;
	}

	public boolean isStateReward(){
		return state == 3 || state==1;
	}
	public boolean isYestReward(){
		return state == 2;
	}
	public boolean isInitReward(){
		return state == 1;
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	public void setStateInit(){
		this.state = 1;
	}
	public void setStateReward(){
		this.state = 2;
	}
	public void setStateSuccess(){
		this.state = 3;
	}

	public ExchangeDecorateInfo(){
	}
	
	@Override
	public SplitEntity newInstance() {
		return new ExchangeDecorateInfo();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(levelId);
		dataList.add(state);
	}

	@Override
	public void fullData(DataArray dataArray) {
		this.levelId = dataArray.getInt();
		this.state = dataArray.getInt();
	}

	@Override
	public boolean equals(Object arg) {
		ExchangeDecorateInfo info = (ExchangeDecorateInfo)arg;
		if(info.getLevelId() == getLevelId())
			return true;
		return false;
	}
	
	

}
