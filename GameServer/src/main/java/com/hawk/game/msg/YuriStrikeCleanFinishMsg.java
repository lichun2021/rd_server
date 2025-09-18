package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.entity.QueueEntity;

public class YuriStrikeCleanFinishMsg extends HawkMsg {
	private YuriStrikeCleanFinishMsg() {
	}

	public String queueId;

	public static YuriStrikeCleanFinishMsg valueOf(QueueEntity queueEntity) {
		YuriStrikeCleanFinishMsg result = new YuriStrikeCleanFinishMsg();
		result.queueId = queueEntity.getId();
		return result;
	}

	public String getQueueId() {
		return queueId;
	}

	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

}
