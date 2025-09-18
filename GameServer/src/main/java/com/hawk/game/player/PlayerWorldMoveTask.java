package com.hawk.game.player;

import org.hawk.net.protocol.HawkProtocol;

public class PlayerWorldMoveTask {
	private HawkProtocol protocol;
	private boolean needSync;
	private long lastSend;
	private int tickPeriod = 500;

	public HawkProtocol getProtocol() {
		return protocol;
	}

	public void setProtocol(HawkProtocol protocol) {
		this.protocol = protocol;
	}

	public boolean isNeedSync() {
		return needSync;
	}

	public void setNeedSync(boolean needSync) {
		this.needSync = needSync;
	}

	public long getLastSend() {
		return lastSend;
	}

	public void setLastSend(long lastSend) {
		this.lastSend = lastSend;
	}

	public int getTickPeriod() {
		return tickPeriod;
	}

	public void setTickPeriod(int tickPeriod) {
		this.tickPeriod = tickPeriod;
	}

}
