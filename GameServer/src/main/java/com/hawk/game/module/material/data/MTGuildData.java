package com.hawk.game.module.material.data;

import com.alibaba.fastjson.JSONObject;

public class MTGuildData {
	private String guildId;
	private int yearDay;
	// # 联盟每日最多普通列车发车次数
	private int allianceCommonTrainNumber;// = 1

	// # 联盟每日最多豪华列车发车次数
	private int allianceSpecialTrainNumber;// = 8

	public String serializStr() {
		JSONObject obj = new JSONObject();
		obj.put("1", guildId);
		obj.put("2", yearDay);
		obj.put("3", allianceCommonTrainNumber);
		obj.put("4", allianceSpecialTrainNumber);
		return obj.toJSONString();
	}

	public void mergeFrom(String str) {
		JSONObject obj = JSONObject.parseObject(str);
		this.guildId = obj.getString("1");
		this.yearDay = obj.getIntValue("2");
		this.allianceCommonTrainNumber = obj.getIntValue("3");
		this.allianceSpecialTrainNumber = obj.getIntValue("4");

	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public int getYearDay() {
		return yearDay;
	}

	public void inrCommonTrainNumber() {
		allianceCommonTrainNumber++;
	}

	public void incSpecialTrainNumber() {
		allianceSpecialTrainNumber++;
	}

	public void setYearDay(int yearDay) {
		this.yearDay = yearDay;
	}

	public int getAllianceCommonTrainNumber() {
		return allianceCommonTrainNumber;
	}

	public void setAllianceCommonTrainNumber(int allianceCommonTrainNumber) {
		this.allianceCommonTrainNumber = allianceCommonTrainNumber;
	}

	public int getAllianceSpecialTrainNumber() {
		return allianceSpecialTrainNumber;
	}

	public void setAllianceSpecialTrainNumber(int allianceSpecialTrainNumber) {
		this.allianceSpecialTrainNumber = allianceSpecialTrainNumber;
	}

}
