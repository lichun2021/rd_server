package com.hawk.activity.type.impl.stronestleader.rank;

import java.util.List;

import com.hawk.activity.type.impl.rank.ActivityRank;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class StrongestRank extends ActivityRank implements SplitEntity {

	public static StrongestRank valueOf(String playerId, long score, int rank) {
		StrongestRank strongestRank = new StrongestRank();
		strongestRank.setId(playerId);
		strongestRank.setScore(score);
		strongestRank.setRank(rank);
		return strongestRank;
	}
	
	public static StrongestRank valueOf(String playerId, long score) {
		StrongestRank rank = new StrongestRank();
		rank.setId(playerId);
		rank.setScore(score);
		return rank;
	}

	@Override
	public SplitEntity newInstance() {
		return new StrongestRank();
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
