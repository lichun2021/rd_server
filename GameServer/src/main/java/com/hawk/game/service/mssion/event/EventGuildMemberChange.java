package com.hawk.game.service.mssion.event;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

import java.util.ArrayList;
import java.util.List;

/**
 * 联盟人数变化
 * @author hf
 */
public class EventGuildMemberChange extends MissionEvent {

	/** 联盟id */
	private String guildId;
	/** 变化数量 */
	private int memberNum;

	public EventGuildMemberChange(String guildId, int memberNum) {
		this.guildId = guildId;
		this.memberNum = memberNum;
	}

	public String getGuildId() {
		return guildId;
	}

	public int getMemberNum() {
		return memberNum;
	}
}
