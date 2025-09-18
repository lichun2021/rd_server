package com.hawk.game.module.dayazhizhan.playerteam.service;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.DYZZWar.PBDYZZTeamMatchData;
import com.hawk.game.protocol.DYZZWar.PBDYZZTeamMember;

public class DYZZMatchData implements SerializJsonStrAble{
		
	private String serverId;
	
	private String teamId;
	
	private String leaderId;
	
	private List<DYZZMember> members = new ArrayList<DYZZMember>();
	
	private int matchScore;
	private int matchScoreAdd;
	
	private long matchPoolTime;
	
	public DYZZMatchData() {
		
	}
	
	public DYZZMatchData(DYZZTeamRoom room){
		this.serverId = room.getServerId();
		this.teamId = room.getTeamId();
		this.leaderId = room.getLeader();
		this.members.addAll(room.getMembers());
	}
	
	private void calMatchScore(){
		int totalScore = 0;
		for(DYZZMember member : this.members){
			totalScore += member.getSeasonScore();
		}
		this.matchScore = totalScore;
	}
	

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("1", serverId);
		obj.put("2", teamId);
		obj.put("3", leaderId);
		JSONArray memberArr = new JSONArray();
		for(DYZZMember member : this.members){
			memberArr.add(member.serializ());
		}
		obj.put("4", memberArr.toJSONString());
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.serverId = obj.getString("1");
		this.teamId = obj.getString("2");
		this.leaderId =  obj.getString("3");
		String memberStr = obj.getString("4");
		JSONArray memberArr = JSONArray.parseArray(memberStr);
		for(int i=0;i<memberArr.size();i++){
			String serStr = memberArr.getString(i);
			DYZZMember member = new DYZZMember();
			member.mergeFrom(serStr);
			this.members.add(member);
		}
		//设置一下数据
		this.calMatchScore();
	}
	
	
	public PBDYZZTeamMatchData.Builder genDYZZTeamMatchData(){
		PBDYZZTeamMatchData.Builder builder = PBDYZZTeamMatchData.newBuilder();
		builder.setServerId(this.serverId);
		builder.setTeamId(this.teamId);
		builder.setLeader(this.leaderId);
		for(DYZZMember member : this.members){
			builder.addMembers(member.genDYZZTeamMemberBuilder());
		}
		return builder;
	}
	
	public void mergeFromDYZZTeamMatchData(PBDYZZTeamMatchData data){
		this.serverId = data.getServerId();
		this.teamId = data.getTeamId();
		this.leaderId = data.getLeader();
		for(PBDYZZTeamMember mb : data.getMembersList()){
			DYZZMember member = new DYZZMember();
			member.mergeFromDYZZTeamMemberBuilder(mb);
			this.members.add(member);
		}
		//设置一下数据
		this.calMatchScore();
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	

	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}


	
	public String getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(String leaderId) {
		this.leaderId = leaderId;
	}

	public List<DYZZMember> getMembers() {
		return members;
	}

	public void setMembers(List<DYZZMember> members) {
		this.members = members;
	}
	
	public int getMatchScore() {
		return matchScore;
	}

	public int getTotalMatchScore() {
		return matchScore + this.matchScoreAdd;
	}
	
	public void addMatchScore(int add){
		this.matchScoreAdd += add;
	}
	
	
	public long getMatchPoolTime() {
		return matchPoolTime;
	}
	
	public void setMatchPoolTime(long matchPoolTime) {
		this.matchPoolTime = matchPoolTime;
	}
	
	public int getMemberCount(){
		return this.members.size();
	}
}
