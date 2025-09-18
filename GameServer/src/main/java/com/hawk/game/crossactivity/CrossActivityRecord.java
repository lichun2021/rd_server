package com.hawk.game.crossactivity;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.player.hero.SerializJsonStrAble;

public class CrossActivityRecord implements SerializJsonStrAble {
	/**
	 * 服务器ID
	 */
	private String serverId;
	
	/**
	 * 期数
	 */
	private int termId;
	
	
	/**
	 * 服务器记分排名
	 */
	private int serverRank;
	
	
	
	
	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("serverId", this.serverId);
		obj.put("termId", this.termId);
		obj.put("serverRank", this.serverRank);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			this.serverId = "";
			this.termId = 0;
			this.serverRank = 0;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.serverId = obj.getString("serverId");
		this.termId = obj.getIntValue("termId");
		this.serverRank =  obj.getIntValue("serverRank");
	}
	

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public int getServerRank() {
		return serverRank;
	}

	public void setServerRank(int serverRank) {
		this.serverRank = serverRank;
	}

	

	
	
}
