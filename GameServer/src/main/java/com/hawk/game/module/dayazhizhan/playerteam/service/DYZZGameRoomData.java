package com.hawk.game.module.dayazhizhan.playerteam.service;

import java.util.ArrayList;
import java.util.List;

import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.DYZZWar.PBDYZZGameRoomState;

public class DYZZGameRoomData  implements SerializJsonStrAble{

	private String gameId;
	private int termId;
	private String serverId;
	private List<DYZZMatchData> campATeams =  new ArrayList<>();
	private List<DYZZMatchData> campBTeams  =  new ArrayList<>();
	private PBDYZZGameRoomState state;
	private long startTime;
	private long lastActiveTime;
	
	
	
	public DYZZGameRoomData(){}
	
	
	
	
	public boolean isActive(long periodTime) {
		long curTime = HawkTime.getMillisecond();
		if (curTime - lastActiveTime < 10 * 1000 + periodTime) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("1", gameId);
		obj.put("2", termId);
		obj.put("3", serverId);
		JSONArray arrCampA = new JSONArray();
		for(DYZZMatchData data : this.campATeams){
			arrCampA.add(data.serializ());
		}
		obj.put("4", arrCampA.toJSONString());
		JSONArray arrCampB = new JSONArray();
		for(DYZZMatchData data : this.campBTeams){
			arrCampB.add(data.serializ());
		}
		obj.put("5", arrCampB.toJSONString());
		obj.put("6", state.getNumber());
		obj.put("7", startTime);
		obj.put("8", lastActiveTime);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.gameId = obj.getString("1");
		this.termId = obj.getIntValue("2");
		this.serverId = obj.getString("3");
		
		JSONArray arrCampA = JSONArray.parseArray(obj.getString("4"));
		for(int i =0;i<arrCampA.size();i++){
			String str = arrCampA.getString(i);
			DYZZMatchData data = new DYZZMatchData();
			data.mergeFrom(str);
			this.campATeams.add(data);
		}
		
		JSONArray arrCampB = JSONArray.parseArray(obj.getString("5"));
		for(int i =0;i<arrCampB.size();i++){
			String str = arrCampB.getString(i);
			DYZZMatchData data = new DYZZMatchData();
			data.mergeFrom(str);
			this.campBTeams.add(data);
		}
		this.state =PBDYZZGameRoomState.valueOf(obj.getIntValue("6")) ;
		this.startTime = obj.getLongValue("7");
		this.lastActiveTime = obj.getLongValue("8");
	}
	
	
	
	public String getGameId() {
		return gameId;
	}
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
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
	public List<DYZZMatchData> getCampATeams() {
		return campATeams;
	}
	public void setCampATeams(List<DYZZMatchData> campATeams) {
		this.campATeams = campATeams;
	}
	public List<DYZZMatchData> getCampBTeams() {
		return campBTeams;
	}
	public void setCampBTeams(List<DYZZMatchData> campBTeams) {
		this.campBTeams = campBTeams;
	}
	public PBDYZZGameRoomState getState() {
		return state;
	}
	public void setState(PBDYZZGameRoomState state) {
		this.state = state;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getLastActiveTime() {
		return lastActiveTime;
	}
	public void setLastActiveTime(long lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}
	
	
	
	
	
}
