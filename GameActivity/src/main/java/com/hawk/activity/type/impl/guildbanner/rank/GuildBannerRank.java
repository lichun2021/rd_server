package com.hawk.activity.type.impl.guildbanner.rank;

import java.util.List;

import com.hawk.activity.type.impl.rank.ActivityRank;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class GuildBannerRank extends ActivityRank implements SplitEntity {
	public GuildBannerRank() {
		
	}
	
	public  GuildBannerRank(String guildId, long score, int rank) {
		this.setId(guildId);
		this.setScore(score);
		this.setRank(rank);
	}
	
	public GuildBannerRank(String guildId, long score) {
		this.setId(guildId);
		this.setScore(score);
	}
	
	@Override
	public SplitEntity newInstance() {
		return new GuildBannerRank();
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
