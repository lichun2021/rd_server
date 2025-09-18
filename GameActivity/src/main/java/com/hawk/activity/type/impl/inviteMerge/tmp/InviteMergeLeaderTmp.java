package com.hawk.activity.type.impl.inviteMerge.tmp;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;

/**
 * 邀请合服对账信息
 * @author Golden
 *
 */
public class InviteMergeLeaderTmp {
	
	String playerId;
	String playerName;
	String guildTag;
	
	/**
	 * 序列化
	 * @return
	 */
	public String serializ() {
		JSONObject json = new JSONObject();
		json.put("playerId", playerId);
		json.put("playerName", playerName);
		json.put("guildTag", guildTag);
		return json.toJSONString();
	}
	
	/**
	 * 反序列化
	 * @param str
	 * @return
	 */
	public static InviteMergeLeaderTmp deSerializ(String str) {
		if (HawkOSOperator.isEmptyString(str)) {
			return null;
		}
		InviteMergeLeaderTmp leader = new InviteMergeLeaderTmp();
		JSONObject parse = JSONObject.parseObject(str);
		leader.setPlayerId(parse.getString("playerId"));
		leader.setPlayerName(parse.getString("playerName"));
		leader.setGuildTag(parse.getString("guildTag"));
		return leader;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getGuildTag() {
		return guildTag;
	}

	public void setGuildTag(String guildTag) {
		this.guildTag = guildTag;
	}
}
