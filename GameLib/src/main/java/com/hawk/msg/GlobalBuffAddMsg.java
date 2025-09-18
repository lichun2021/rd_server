package com.hawk.msg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.msg.HawkMsg;

public class GlobalBuffAddMsg extends HawkMsg {
	
	private List<Integer> buffIdList;
	/**
	 * buff的开始时间
	 */
	private long buffStartTime;
	/**
	 * buff的结束时间
	 */
	private long buffEndTime;
	
	public GlobalBuffAddMsg(List<Integer> buffIdList, long buffStartTime, long buffEndTime) {
		this.buffIdList = buffIdList;
		this.buffStartTime = buffStartTime;
		this.buffEndTime = buffEndTime;
	}
	
	public GlobalBuffAddMsg(int buffId, long buffStartTime, long buffEndTime) {
		this.buffIdList = new ArrayList<>();
		this.buffIdList.add(buffId);
		this.buffStartTime = buffStartTime;
		this.buffEndTime = buffEndTime;
	}
	
	public List<Integer> getBuffIdList() {
		return buffIdList;
	}
	
	public void setBuffIdList(List<Integer> buffIdList) {
		this.buffIdList = buffIdList;
	}

	public long getBuffStartTime() {
		return buffStartTime;
	}

	public void setBuffStartTime(long buffStartTime) {
		this.buffStartTime = buffStartTime;
	}

	public long getBuffEndTime() {
		return buffEndTime;
	}

	public void setBuffEndTime(long buffEndTime) {
		this.buffEndTime = buffEndTime;
	}
	
}
