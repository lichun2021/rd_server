package com.hawk.activity.type.impl.recallFriend.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.type.impl.recallFriend.cfg.RecallFriendCfg;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.os.HawkTime;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 被找回玩家
 * @author lwt
 * @date 2021年12月21日
 */
public class RecalPlayer {
	private String playerId;
	/** key yyyymmdd  val cnt*/
	private Map<Integer, Integer> dayCalCnt = new HashMap<Integer, Integer>();

	/** 回流时的联盟id 不可更改*/
	private String initGuildId;
	/**回流后首次加入的联盟*/
	private String joinGuildId;

	/**加入的联盟时玩家主堡等级*/
	private int joinGuildIdFacLv;

	/** 加入joinGuildId 又退出*/
	private boolean quitJoinguild;

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	/**
	 *  今天被招回数++
	 */
	public void incDayCalCnt() {
		int today = HawkTime.getYyyyMMddIntVal();
		if (dayCalCnt.containsKey(today)) {
			int cnt = dayCalCnt.get(today);
			dayCalCnt.put(today, cnt + 1);
		} else {
			dayCalCnt.put(today, 1);
		}
	}

	/**
	 *  今天被招回数
	 */
	public int getTodayCalCnt() {
		int today = HawkTime.getYyyyMMddIntVal();
		return dayCalCnt.getOrDefault(today, 0);
	}

	public boolean isValidDoEvent(String guildId){
		if(quitJoinguild){
			return false;
		}
		if(!Objects.equals(guildId, joinGuildId)){
			return false;
		}
		if(joinGuildIdFacLv < RecallFriendCfg.getInstance().getBaseLimit()){
			return false;
		}
		return true;

	}
	public Map<Integer, Integer> getDayCalCnt() {
		return dayCalCnt;
	}

	public String getInitGuildId() {
		return initGuildId;
	}

	public void setInitGuildId(String initGuildId) {
		this.initGuildId = initGuildId;
	}

	public String getJoinGuildId() {
		return joinGuildId;
	}

	public void setJoinGuildId(String joinGuildId) {
		this.joinGuildId = joinGuildId;
	}

	public boolean isQuitJoinguild() {
		return quitJoinguild;
	}

	public void setQuitJoinguild(boolean quitJoinguild) {
		this.quitJoinguild = quitJoinguild;
	}

	public void setDayCalCnt(Map<Integer, Integer> dayCalCnt) {
		this.dayCalCnt = dayCalCnt;
	}

	public int getJoinGuildIdFacLv() {
		return joinGuildIdFacLv;
	}

	public void setJoinGuildIdFacLv(int joinGuildIdFacLv) {
		this.joinGuildIdFacLv = joinGuildIdFacLv;
	}

	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("playerId", playerId);
		obj.put("initGuildId", initGuildId);
		obj.put("joinGuildId", joinGuildId);
		obj.put("quitJoinguild", quitJoinguild);
		String dayCalCntStr = SerializeHelper.mapToString(dayCalCnt);
		obj.put("dayCalCntStr", dayCalCntStr);
		obj.put("joinGuildIdFacLv", joinGuildIdFacLv);
		return obj.toJSONString();
	}

	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSON.parseObject(serialiedStr);
		this.playerId = obj.getString("playerId");
		this.initGuildId = obj.getString("initGuildId");
		this.joinGuildId = obj.getString("joinGuildId");
		this.quitJoinguild = obj.getBoolean("quitJoinguild");
		String dayCalCntStr = obj.getString("dayCalCntStr");
		this.dayCalCnt = SerializeHelper.stringToMap(dayCalCntStr);
		this.joinGuildIdFacLv = obj.getIntValue("joinGuildIdFacLv");
	}
}
