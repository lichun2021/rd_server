package com.hawk.activity.type.impl.strongestGuild.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.activity.type.impl.strongestGuild.cfg.StrongestGuildCfg;

public class GuildData {
	
	private int termId;
	
	private String guildId;
	
	private ConcurrentHashMap<Integer, AtomicLong> map;
	
	public GuildData(){};
	
	public GuildData(int termId, String guildId){
		this.termId = termId;
		this.guildId = guildId;
		this.map = new ConcurrentHashMap<Integer, AtomicLong>();
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public Map<Integer, AtomicLong> getMap() {
		return map;
	}
	
	public void setMap(ConcurrentHashMap<Integer, AtomicLong> map) {
		this.map = map;
	}

	public void addScore(int stageId, long score){
		AtomicLong al = map.get(stageId);
		if(al == null){
			al = createAtomicScore(stageId);
		}
		al.addAndGet(score);
	}
	
	public long calTotalScore(){
		long totalScore = 0;
		for(Integer key : map.keySet()){
			StrongestGuildCfg circularCfg = HawkConfigManager.getInstance().getConfigByKey(StrongestGuildCfg.class, key);
			if(circularCfg == null){
				HawkLog.errPrintln("StrongestGuildActivity cal guild total score error:{}", key);
				continue;
			}
			AtomicLong al = map.get(key);
			totalScore += (long)(circularCfg.getScoreWeightCof() * al.get() / 10000.0f);
		}
		return totalScore;
	}
	
	public synchronized AtomicLong createAtomicScore(int stageId){
		AtomicLong al = map.get(stageId);
		if(al != null){
			return al;
		}
		synchronized (this) {
			al = new AtomicLong(0);
			map.put(stageId, al);
			return al;
		}
	}
}
