package com.hawk.game.lianmengstarwars;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;

public class SWPeriodTickable extends HawkPeriodTickable{

	private SWBattleRoom battleRoom;
	
	private SWPeriodTickable(long tickPeriod) {
		super(tickPeriod);
	}
	
	public static SWPeriodTickable create(SWBattleRoom battleRoom){
		SWPeriodTickable result = new SWPeriodTickable(168);
		result.battleRoom = battleRoom;
		return result;
	}

	@Override
	public void onPeriodTick() {
		long beginTimeMs = HawkTime.getMillisecond();
		try {
			battleRoom.onBattleTick();
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			// 时间消耗的统计信息
			long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
			if (costTimeMs > 200) {
				HawkLog.logPrintln("process SWThread tick too much time, costtime: {}", costTimeMs);
			}
		}
	}

}
