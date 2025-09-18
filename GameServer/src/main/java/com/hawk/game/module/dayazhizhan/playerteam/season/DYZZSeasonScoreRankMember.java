package com.hawk.game.module.dayazhizhan.playerteam.season;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.module.dayazhizhan.battleroom.extry.DYZZBattleRoomFameHallMember;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.DYZZWar.PBDYZZSeasonScoreRankMember;

public class DYZZSeasonScoreRankMember  implements SerializJsonStrAble{

	private String playerId;
	
	private String serverId;
	
	private String playerName;

	private int icon;
	
	private String pficon;
	
	private int rank;
	
	private int score;
	
	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("playerId", playerId);
		obj.put("serverId", serverId);
		obj.put("playerName", playerName);
		obj.put("icon", icon);
		if(!HawkOSOperator.isEmptyString(pficon)){
			obj.put("pficon", pficon);
		}
		obj.put("rank", rank);
		obj.put("score", score);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSON.parseObject(serialiedStr);
		this.playerId = obj.getString("playerId");
		this.serverId = obj.getString("serverId");
		this.playerName = obj.getString("playerName");
		this.icon = obj.getIntValue("icon");
		this.pficon = obj.getString("pficon");
		this.rank = obj.getIntValue("rank");
		this.score = obj.getIntValue("score");
		
	}
	
	public DYZZBattleRoomFameHallMember toDYZZBattleRoomFameHallMember(){
		DYZZBattleRoomFameHallMember member = new DYZZBattleRoomFameHallMember();
		member.setPlayerId(this.playerId);
		member.setServerId(this.serverId);
		member.setPlayerName(this.playerName);
		member.setIcon(this.icon);
		member.setPficon(this.pficon);
		member.setRank(this.rank);
		member.setScore(this.score);
		return member;
	}
	
	
	public PBDYZZSeasonScoreRankMember createPBDYZZSeasonScoreRankMember(){
		PBDYZZSeasonScoreRankMember.Builder builder = PBDYZZSeasonScoreRankMember.newBuilder();
		builder.setPlayerId(this.playerId);
		builder.setServerId(this.serverId);
		builder.setPlayerName(this.playerName);
		builder.setIcon(this.icon);
		if(!HawkOSOperator.isEmptyString(this.pficon)){
			builder.setPfIcon(this.pficon);
		}
		builder.setRank(this.rank);
		builder.setScore(this.score);
		return builder.build();
	}
	

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getPficon() {
		return pficon;
	}

	public void setPficon(String pficon) {
		this.pficon = pficon;
	}

	public int getRank() {
		return rank;
	}
	
	public void setRank(int rank) {
		this.rank = rank;
	}
	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}



	
	
	
	

}
