package com.hawk.activity.type.impl.mergecompetition.rank;

import java.util.List;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;
import com.hawk.activity.type.impl.rank.ActivityRank;

public class MergeCompetitionRank extends ActivityRank implements SplitEntity {
	
	public MergeCompetitionRank() {
	}
	
	/**
	 * @param id 可能是playerId或guildId
	 */
	public MergeCompetitionRank(String id, long score, int rank) {
		this.setId(id);
		this.setScore(score);
		this.setRank(rank);
	}
	
	public MergeCompetitionRank(String playerId, long score) {
		this.setId(playerId);
		this.setScore(score);
	}
	
	@Override
	public SplitEntity newInstance() {
		return new MergeCompetitionRank();
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
