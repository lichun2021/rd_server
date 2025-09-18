package com.hawk.game.module.lianmengyqzz.battleroom;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;

import com.hawk.game.log.DungeonRedisLog;

public class YQZZPeriodTickable extends HawkPeriodTickable{

	private YQZZBattleRoom battleRoom;
	
	private YQZZPeriodTickable(long tickPeriod) {
		super(tickPeriod);
	}
	
	public static YQZZPeriodTickable create(YQZZBattleRoom battleRoom){
		YQZZPeriodTickable result = new YQZZPeriodTickable(567);
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
				DungeonRedisLog.log(battleRoom.getId(), "process tick too much time, costtime: {}", costTimeMs);
			}
		}
	}

}
