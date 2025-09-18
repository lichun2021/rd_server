package com.hawk.game.crossactivity;

import java.util.HashMap;
import java.util.Map;

public class CRankBuff {
	/** buff数据*/
	private String buff;
	
	/** 截止时间*/
	private long endTime;

	public String getBuff() {
		return buff;
	}

	public void setBuff(String buff) {
		this.buff = buff;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	public Map<Integer, Integer> calcBuffMap() {
		Map<Integer, Integer> map = new HashMap<>();
		String[] array = buff.split(",");
		for (String val : array) {
			String[] info = val.split("_");
			map.put(Integer.parseInt(info[0]), Integer.parseInt(info[1]));
		}
		return map;
	}

}
