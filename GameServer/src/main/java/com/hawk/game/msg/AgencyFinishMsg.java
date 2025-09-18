package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

public class AgencyFinishMsg extends HawkMsg {
	/**
	 * 情报uuid
	 */
	private String agencyUUid;
	
	public String getAgencyUUid() {
		return agencyUUid;
	}

	public void setAgencyUUid(String agencyUUid) {
		this.agencyUUid = agencyUUid;
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static AgencyFinishMsg valueOf(String agencyUUid) {
		AgencyFinishMsg msg = new AgencyFinishMsg();
		msg.setAgencyUUid(agencyUUid);
		return msg;
	}
}
