package com.hawk.game.crossactivity.season;

import java.util.ArrayList;
import java.util.List;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.protocol.CrossActivity.CrossActivitySeasonScoreRecord;

public class CrossSeasonScoreRecord {

	private int season;
	
	private String serverId;
	
	private String opponentServerId;
	
	private int score;
	
	private int scoreBef;
	
	private int scoreAft;
	
	private int win;
	
	private long time;
	
	private int crossTerm;

	private int crossId;
	
	private int camp;  //1进攻  2防守
	
	public CrossSeasonScoreRecord(){}
	
	public CrossSeasonScoreRecord(int season, String serverId, String opponentServerId, int score, int scoreBef, int scoreAft, int win,
			long time,int crossTerm,int crossId,boolean defender) {
		super();
		this.season = season;
		this.serverId = serverId;
		this.opponentServerId = opponentServerId;
		this.score = score;
		this.scoreBef = scoreBef;
		this.scoreAft = scoreAft;
		this.win = win;
		this.time = time;
		this.crossTerm = crossTerm;
		this.crossId = crossId;
		this.camp =  1;
		if(defender){
			this.camp = 2;
		}
	}
	
	

	
	public int getSeason() {
		return season;
	}
	
	
	public String getServerId() {
		return serverId;
	}
	
	public String getOpponentServerId() {
		return opponentServerId;
	}
	
	public int getScore() {
		return score;
	}
	
	public int getScoreAft() {
		return scoreAft;
	}
	
	public int getScoreBef() {
		return scoreBef;
	}
	
	public long getTime() {
		return time;
	}
	
	
	public int getWin() {
		return win;
	}
	
	
	public int getCrossId() {
		return crossId;
	}
	
	public int getCamp() {
		return camp;
	}
	
	
	
	public String serialize(){
		JSONObject obj = new JSONObject();
        obj.put("season", this.season);
        obj.put("serverId", this.serverId);
        obj.put("opponentServerId", this.opponentServerId);
        obj.put("score", this.score);
        obj.put("scoreBef", this.scoreBef);
        obj.put("scoreAft", this.scoreAft);
        obj.put("win", this.win);
        obj.put("time", this.time);
        obj.put("crossTerm", this.crossTerm);
        obj.put("crossId", this.crossId);
        obj.put("camp", this.camp);
        return obj.toJSONString();
	}
	
	public void unSerialize(String data){
		if(HawkOSOperator.isEmptyString(data)){
			return;
		}
		JSONObject obj = JSONObject.parseObject(data);
		this.season = obj.getIntValue("season");
		this.serverId = obj.getString("serverId");
		this.opponentServerId = obj.getString("opponentServerId");
		this.score = obj.getIntValue("score");
		this.scoreBef = obj.getIntValue("scoreBef");
		this.scoreAft = obj.getIntValue("scoreAft");
		this.win = obj.getIntValue("win");
		this.time = obj.getLongValue("time");
		this.crossTerm = obj.getIntValue("crossTerm");
		this.crossId = obj.getIntValue("crossId");
		this.camp = obj.getIntValue("camp");
	}
	
	
	public void saveData(){
		String key = String.format(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_SCPRE_RECORD ,this.season, this.serverId);
        RedisProxy.getInstance().getRedisSession().lPush(key, 0, this.serialize());
	}
	

	
	public CrossActivitySeasonScoreRecord.Builder build(){
		CrossActivitySeasonScoreRecord.Builder rbuilder = CrossActivitySeasonScoreRecord.newBuilder();
		rbuilder.setTargetServerId(this.opponentServerId);
		rbuilder.setScore(this.score);
		rbuilder.setTime(this.time);
		rbuilder.setWin(this.win);
		rbuilder.setCamp(this.camp);
		return rbuilder;
	}
	
	public static List<CrossSeasonScoreRecord> loadAllData(int season,String serverId){
		List<CrossSeasonScoreRecord> list = new ArrayList<>();
		String key = String.format(CrossActivitySeasonConst.ResidKey.CROSS_SEASON_SCPRE_RECORD ,season,serverId);
		List<String> datas = ActivityGlobalRedis.getInstance().getRedisSession()
    			.lRange(key, 0, -1, 0);
    	for(String data : datas){
    		CrossSeasonScoreRecord record = new CrossSeasonScoreRecord();
    		record.unSerialize(data);
    		list.add(record);
    	}
        return list;
	}
	
	
	
	
}
