package com.hawk.game.service;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 全服邮件
 *
 * @author david
 */

public class GlobalMail {
	private String uuid;
	private int mailId;
	private String title;
	private String subTitle;
	private JSONArray msg = new JSONArray();
	private String reward;
	private long createTime = HawkTime.getMillisecond();
	private long startTime = 0;
	private long endTime = 0;
	private String channel;
	private String platform;
	private String rewardStatus;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public String getReward() {
		return reward;
	}

	public void setReward(String reward) {
		this.reward = reward;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public int getMailId() {
		return mailId;
	}

	public void setMailId(int mailId) {
		this.mailId = mailId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("uuid", uuid);
		json.put("mailId", mailId);
		json.put("title", title);
		json.put("subTitle", subTitle);
		json.put("msg", msg);
		json.put("reward", reward);
		json.put("createTime", createTime);
		json.put("startTime", startTime);
		json.put("endTime", endTime);
		json.put("channel", channel);
		json.put("platform", platform);
		json.put("rewardStatus", rewardStatus);
		return json;
	}

	public static GlobalMail create(JSONObject json) {
		try {
			GlobalMail mail = new GlobalMail();
			mail.uuid = json.getString("uuid");
			mail.mailId = json.getIntValue("mailId");
			mail.title = json.getString("title");
			mail.subTitle = json.getString("subTitle");
			mail.msg = json.getJSONArray("msg");
			mail.reward = json.getString("reward");
			mail.createTime = json.getLong("createTime");
			mail.startTime = json.getLong("startTime");
			mail.endTime = json.getLong("endTime");
			mail.platform = json.getString("platform");
			mail.channel = json.getString("channel");
			mail.rewardStatus = json.getString("rewardStatus");
			return mail;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public JSONArray getMsg() {
		return msg;
	}

	public void setMsg(JSONArray msg) {
		this.msg = msg;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getRewardStatus() {
		return rewardStatus;
	}

	public void setRewardStatus(String rewardStatus) {
		this.rewardStatus = rewardStatus;
	}

}
