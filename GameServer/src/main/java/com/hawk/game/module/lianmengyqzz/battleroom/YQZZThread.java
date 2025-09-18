package com.hawk.game.module.lianmengyqzz.battleroom;

import java.util.concurrent.atomic.AtomicLong;

import org.hawk.thread.HawkThread;

public class YQZZThread extends HawkThread {
	private static final AtomicLong ThreadIdx = new AtomicLong();
	private YQZZBattleRoom battleRoom;

	private YQZZThread() {
	}

	public static YQZZThread create(YQZZBattleRoom battleRoom) {
		YQZZThread thread = new YQZZThread();
		thread.battleRoom = battleRoom;
		thread.setDaemon(true);
		thread.setName("YQZZ-" + battleRoom.getId() + "-" + ThreadIdx.incrementAndGet());
		thread.getTickableContainer().setTickPeriod(250);
		thread.setPriority(10); // :)
		thread.start();

		battleRoom.setThread(thread);
		thread.addTickable(YQZZPeriodTickable.create(battleRoom));

		return thread;
	}

	public YQZZBattleRoom getBattleRoom() {
		return battleRoom;
	}

}
