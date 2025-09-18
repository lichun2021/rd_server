package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.pool.HawkObjectPool;
import org.hawk.pool.ObjectPool;

import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 联盟任务消息
 * 
 * @author Jesse
 *
 */
@ObjectPool.Declare(minIdle = 128, maxIdle = 2048, timeBetweenEvictionRunsMillis = 300000, minEvictableIdleTimeMillis = 600000)
public class GuildTaskMsg extends HawkMsg {

	private GuildTaskEvent event;

	public GuildTaskMsg() {
		super(MsgId.GUILD_TASK_MSG);
	}

	public GuildTaskEvent getEvent() {
		return event;
	}

	public void setEvent(GuildTaskEvent event) {
		this.event = event;
	}

	public static GuildTaskMsg valueOf(GuildTaskEvent event) {
		GuildTaskMsg msg = HawkObjectPool.getInstance().borrowObject(GuildTaskMsg.class);
		msg.event = event;
		return msg;
	}
}
