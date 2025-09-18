package com.hawk.game.module.lianmengyqzz.march.data.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.march.data.IYQZZData;

public class YQZZRecordData  implements IYQZZData{

	private static final String redisKey = "YQZZ_ACTIVITY_AWARD_RECORD_DATA";
	
	private int termId;
	
	private String serverId;
	
	private String roomId;
	
	private int sendAward;
	
	private int rank;
	
	private long score;
	private long seasonScore;
	private boolean isAdvance;
	
	private long time;
	

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public int getSendAward() {
		return sendAward;
	}

	public void setSendAward(int sendAward) {
		this.sendAward = sendAward;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}
	
	public long getScore() {
		return score;
	}
	
	public void setScore(long score) {
		this.score = score;
	}

	public long getSeasonScore() {
		return seasonScore;
	}

	public void setSeasonScore(long seasonScore) {
		this.seasonScore = seasonScore;
	}

	public boolean isAdvance() {
		return isAdvance;
	}

	public void setAdvance(boolean advance) {
		isAdvance = advance;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("termId", this.termId);
		obj.put("serverId", this.serverId);
		obj.put("roomId", this.roomId);
		obj.put("sendAward", this.sendAward);
		obj.put("rank", this.rank);
		obj.put("score", this.score);
		obj.put("seasonScore", this.seasonScore);
		obj.put("isAdvance", this.isAdvance);
		obj.put("time", this.time);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			this.termId = 0;
			this.serverId = null;
			this.roomId = null;
			this.sendAward = 0;
			this.rank = 0;
			this.score = 0;
			this.seasonScore = 0;
			this.isAdvance = false;
			this.time = 0;
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.termId = obj.getIntValue("termId");
		this.serverId = obj.getString("serverId");
		this.roomId = obj.getString("roomId");
		this.sendAward = obj.getIntValue("sendAward");
		this.rank = obj.getIntValue("rank");
		this.score = obj.getLongValue("score");
		this.seasonScore = obj.getLongValue("seasonScore");
		this.isAdvance = obj.getBooleanValue("isAdvance");
		this.time = obj.getLongValue("time");
	}

	
	
	
	@Override
	public void saveRedis() {
		String key = redisKey  + ":" + this.serverId;
		RedisProxy.getInstance().getRedisSession().hSet(key, String.valueOf(this.termId), this.serializ());	
		StatisManager.getInstance().incRedisKey(redisKey);
	}
	
	public static YQZZRecordData loadData(String serverid,int termId){
		String key = redisKey  + ":" + serverid;
		String str = RedisProxy.getInstance().getRedisSession()
				.hGet(key, String.valueOf(termId));
		if(!HawkOSOperator.isEmptyString(str)){
			YQZZRecordData join = new YQZZRecordData();
			join.mergeFrom(str);
			return join;
		}
		return null;
	}
	
	public static Map<Integer,YQZZRecordData> loadAll(String serverid,List<Integer> terms){
		String key = redisKey  + ":" + serverid;
		List<String> termArr = new ArrayList<>();
		terms.forEach(term->termArr.add(String.valueOf(term)));
		Map<Integer,YQZZRecordData> rlt = new HashMap<>();
		if(termArr.size() <= 0){
			return rlt;
		}
		List<String> list = RedisProxy.getInstance().getRedisSession()
				.hmGet(key, termArr.toArray(new String[termArr.size()]));
		for(String str : list){
			YQZZRecordData join = new YQZZRecordData();
			join.mergeFrom(str);
			rlt.put(join.getTermId(), join);
		}
		return rlt;
	}
	
}
