package com.hawk.game.module.toucai.entity;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.protocol.MedalFactory.HPMedalFactorySteal;

/**
 * 被偷取中
 * @author lwt
 * @date 2024年3月13日
 */
public class MedalStealed {
	private int rewardCfgId;// = 2;
	private long start;// = 3;
	private long end;// = 4; // end之后就成功了,不能驱赶
	private String playerId;// = 5; // 小偷id
	private String name;// = 6;
	private String pficon;// = 7;
	private int icon;// = 8;
	private int qcnt; // 驱赶记数
	private int log; // 已进入log
	private boolean stealAll;
	private String stealed;
	public void mergeFrom(String string) {
		JSONObject obj = JSONObject.parseObject(string);
		this.playerId = obj.getString("playerId");
		this.rewardCfgId = obj.getIntValue("rf");
		this.start = obj.getLongValue("start");
		this.end = obj.getLongValue("end");
		this.name = obj.getString("name");
		this.pficon = obj.getString("pficon");
		this.icon = obj.getIntValue("icon");
		this.qcnt = obj.getIntValue("qcnt");
		this.log = obj.getIntValue("log");
	}

	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("playerId", playerId);
		obj.put("rf", rewardCfgId);
		obj.put("start", start);
		obj.put("end", end);
		obj.put("name", name);
		obj.put("pficon", pficon);
		obj.put("icon", icon);
		obj.put("qcnt", qcnt);
		obj.put("log", log);
		return obj.toJSONString();
	}

	public HPMedalFactorySteal toHP() {
		HPMedalFactorySteal.Builder builder = HPMedalFactorySteal.newBuilder();
		builder.setPlayerId(playerId);
		builder.setRewardCfgId(rewardCfgId);
		builder.setStartTime(start);
		builder.setEndTime(end);
		builder.setName(name);
		builder.setPficon(pficon);
		builder.setIcon(icon);
		builder.setQcnt(qcnt);
		return builder.build();
	}

	public int getRewardCfgId() {
		return rewardCfgId;
	}

	public void setRewardCfgId(int rewardCfgId) {
		this.rewardCfgId = rewardCfgId;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPficon() {
		return pficon;
	}

	public void setPficon(String pficon) {
		this.pficon = pficon;
	}

	public int getQcnt() {
		return qcnt;
	}

	public void setQcnt(int qcnt) {
		this.qcnt = qcnt;
	}

	public int getLog() {
		return log;
	}

	public void setLog(int log) {
		this.log = log;
	}

	public boolean isStealAll() {
		return stealAll;
	}

	public void setStealAll(boolean stealAll) {
		this.stealAll = stealAll;
	}

	public String getStealed() {
		return stealed;
	}

	public void setStealed(String stealed) {
		this.stealed = stealed;
	}

}
