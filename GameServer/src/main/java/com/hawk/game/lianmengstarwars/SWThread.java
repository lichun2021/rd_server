package com.hawk.game.lianmengstarwars;

import java.util.concurrent.atomic.AtomicLong;

import org.hawk.thread.HawkThread;

public class SWThread extends HawkThread {
	private static final AtomicLong ThreadIdx = new AtomicLong();
	private SWBattleRoom battleRoom;

	private SWThread() {
	}

	public static SWThread create(SWBattleRoom battleRoom) {
		SWThread thread = new SWThread();
		thread.battleRoom = battleRoom;
		thread.setDaemon(true);
		thread.setName("SW-" + battleRoom.getId() + "-" + ThreadIdx.incrementAndGet());
		thread.getTickableContainer().setTickPeriod(50);
		thread.setPriority(10); // :)
		thread.start();

		battleRoom.setThread(thread);
		thread.addTickable(SWPeriodTickable.create(battleRoom));

		return thread;
	}

	public SWBattleRoom getBattleRoom() {
		return battleRoom;
	}

}
