package com.hawk.game.msg;

import com.hawk.game.player.Player;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.gamelib.GameConst;
import org.hawk.msg.HawkMsg;

/**
 * 方尖碑任务分发
 * @author hf
 */
public class ObeliskMissionRefreshMsg extends HawkMsg {
	private Player player;
	private MissionEvent event;
	public ObeliskMissionRefreshMsg() {
		super(GameConst.MsgId.OBELISK_MISSION_REFRESH);
	}
	public Player getPlayer() {
		return player;
	}

	public void setPlayerId(Player player) {
		this.player = player;
	}

	public MissionEvent getEvent() {
		return event;
	}

	public void setEvent(MissionEvent event) {
		this.event = event;
	}

	public static ObeliskMissionRefreshMsg valueOf(Player player, MissionEvent event) {
		ObeliskMissionRefreshMsg migratePlayerMsg = new ObeliskMissionRefreshMsg();
		migratePlayerMsg.player = player;
		migratePlayerMsg.event = event;
		return migratePlayerMsg;
	}
}
