package com.hawk.game.player;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 玩家序列化到对象中的对象
 * @author jm
 *
 */
public class PlayerSerializeData {
	
	/**
	 * 好友列表 序列化好友， 申请这种数据丢了就丢了
	 */
	JSONArray relationList;
	
	/**
	 * 活动数据
	 */
	JSONObject activityData;
	/**
	 * PlayerData里面的entity 列表
	 */
	JSONObject playerData;

	public JSONObject getPlayerData() {
		return playerData;
	}

	public void setPlayerData(JSONObject playerData) {
		this.playerData = playerData;
	}

	public JSONArray getRelationList() {
		return relationList;
	}

	public void setRelationList(JSONArray relationList) {
		this.relationList = relationList;
	}

	public JSONObject getActivityData() {
		return activityData;
	}

	public void setActivityData(JSONObject activityData) {
		this.activityData = activityData;
	}		
}
