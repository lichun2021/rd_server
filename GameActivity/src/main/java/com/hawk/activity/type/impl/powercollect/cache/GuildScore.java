package com.hawk.activity.type.impl.powercollect.cache;

import java.util.concurrent.atomic.AtomicLong;
import com.hawk.activity.type.impl.powercollect.rank.PowerCollectRankData;

public class GuildScore extends PowerCollectRankData{
	
	private AtomicLong al;
	
	public GuildScore(){}
	
	public GuildScore(String guildId){
		super(guildId);
	}

	public long getScore() {
		return al.get();
	}

	public void setAl(AtomicLong al) {
		this.al = al;
	}
	
	public void addScore(double score){
		al.addAndGet((long)score);
	}
}
