package com.hawk.game.module.toucai.entity;

import com.alibaba.fastjson.JSONObject;

/**
 * 偷取中
 * @author lwt
 * @date 2024年3月13日
 */
public class MedalSteal {

	private String targetId;
	private int rewardCfgId;// = 2;
	private String tarName="";
	private long start;// = 3;
	private long end;// = 4; // end之后就成功了,不能驱赶
	private String stealed;
	public MedalSteal() {
	}

	public void mergeFrom(String string) {
		JSONObject obj = JSONObject.parseObject(string);
		this.targetId = obj.getString("targetId");
		this.rewardCfgId = obj.getIntValue("rf");
		this.start = obj.getLongValue("start");
		this.end = obj.getLongValue("end");
		this.tarName = obj.getString("tarName");
		
	}

	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("targetId", targetId);
		obj.put("rf", rewardCfgId);
		obj.put("start", start);
		obj.put("end", end);
		obj.put("tarName", tarName);
		return obj.toJSONString();
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public int getRewardCfgId() {
		return rewardCfgId;
	}

	public void setRewardCfgId(int rewardCfgId) {
		this.rewardCfgId = rewardCfgId;
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

	public String getTarName() {
		return tarName;
	}

	public void setTarName(String tarName) {
		this.tarName = tarName;
	}

	public String getStealed() {
		return stealed;
	}

	public void setStealed(String stealed) {
		this.stealed = stealed;
	}

}
