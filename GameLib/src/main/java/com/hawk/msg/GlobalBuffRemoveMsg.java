package com.hawk.msg;

import java.util.List;

import org.hawk.msg.HawkMsg;

public class GlobalBuffRemoveMsg extends HawkMsg {
	
	
	private List<Integer> buffIdList;
	
	public GlobalBuffRemoveMsg(List<Integer> buffIdList) {
		this.buffIdList = buffIdList;
	}

	public List<Integer> getBuffIdList() {
		return buffIdList;
	}

	public void setBuffIdList(List<Integer> buffIdList) {
		this.buffIdList = buffIdList;
	}
}
