package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.entity.QueueEntity;
import com.hawk.game.guild.GuildHelpInfo;
import com.hawk.game.service.GuildService;

public class HospiceQueueFinishMsg extends HawkMsg {
	String items;
	String queueId;
	long battleTime;
	GuildHelpInfo helpInfo;

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static HospiceQueueFinishMsg valueOf(QueueEntity queueEntity, String guildId) {
		HospiceQueueFinishMsg msg = new HospiceQueueFinishMsg();
		msg.items = queueEntity.getCancelBackRes();
		msg.queueId = queueEntity.getId();
		msg.battleTime = queueEntity.getStartTime();
		msg.helpInfo = GuildService.getInstance().getGuildHelpInfo(guildId, msg.getQueueId());
		return msg;
	}

	public String getItems() {
		return items;
	}

	public void setItems(String items) {
		this.items = items;
	}

	public String getQueueId() {
		return queueId;
	}

	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	public long getBattleTime() {
		return battleTime;
	}

	public void setBattleTime(long battleTime) {
		this.battleTime = battleTime;
	}

	public GuildHelpInfo getHelpInfo() {
		return helpInfo;
	}

	public void setHelpInfo(GuildHelpInfo helpInfo) {
		this.helpInfo = helpInfo;
	}

}
