package com.hawk.game.task;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkTime;
import org.hawk.thread.HawkTask;

import com.hawk.game.GsApp;

public class PlayerLoginTask extends HawkTask {
	/**
	 * 任务构建时间
	 */
	private long timestamp;
	/**
	 * 会话
	 */
	private HawkSession session;
	/**
	 * 任务协议
	 */
	private HawkProtocol protocol;
	
	public PlayerLoginTask(HawkSession session, HawkProtocol protocol) {
		this.session = session;
		this.protocol = protocol;
		this.timestamp = HawkTime.getMillisecond();
	}

	@Override
	public Object run() {
		if (!GsApp.getInstance().doLoginProcess(session, protocol, timestamp)) {
			return false;
		}
		return true;
	}
}
