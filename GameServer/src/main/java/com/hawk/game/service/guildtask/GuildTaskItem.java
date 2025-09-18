package com.hawk.game.service.guildtask;

import com.hawk.game.util.GsConst.MissionState;

public class GuildTaskItem {
	/** 配置id*/
	private int cfgId;

	/** 进度值*/
	private int value;

	/** 状态 0 未完成 1已完成 2已领奖*/
	private int state;
	
	
	public static GuildTaskItem valueOf(String info){
		String[] arr = info.split("_");
		if(arr.length!=3){
			return null;
		}
		GuildTaskItem item = new GuildTaskItem(Integer.valueOf(arr[0]), Integer.valueOf(arr[1]), Integer.valueOf(arr[2]));
		return item;
		
	}
	
	public GuildTaskItem(int cfgId, int value, int state) {
		this.cfgId = cfgId;
		this.value = value;
		this.state = state;
	}

	public int getCfgId() {
		return cfgId;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public void addValue(int value) {
		this.value += value;
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String toString() {
		return String.format("%d_%d_%d", cfgId, value, state);
	}
	
	

	/**
	 * 任务是否完成
	 */
	public void isMissionComplete() {
		this.state = MissionState.STATE_FINISH;
	}
	
}
