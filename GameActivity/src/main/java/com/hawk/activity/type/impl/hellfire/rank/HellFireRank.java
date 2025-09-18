package com.hawk.activity.type.impl.hellfire.rank;

import java.util.List;

import com.hawk.activity.type.impl.rank.ActivityRank;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class HellFireRank extends ActivityRank implements SplitEntity{
	public HellFireRank() {
		
	}
	
	public  HellFireRank(String playerId, long score, int rank) {
		this.setId(playerId);
		this.setScore(score);
		this.setRank(rank);
	}
	
	public HellFireRank(String playerId, long score) {
		this.setId(playerId);
		this.setScore(score);
	}
	
	@Override
	public SplitEntity newInstance() {
		return new HellFireRank();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(getId());
		dataList.add(getScore());
		dataList.add(getRank());
		
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		setId(dataArray.getString());
		setScore(dataArray.getLong());
		setRank(dataArray.getInt());
		
	}
}
