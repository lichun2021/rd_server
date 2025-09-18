package com.hawk.activity.type.impl.seasonpuzzle;

import java.util.List;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import com.hawk.activity.type.impl.seasonpuzzle.cfg.SeasonPuzzleConstCfg;

/**
 * 自动注水逻辑
 * 
 * @author lating
 *
 */
public class SeasonPuzzleAutoLogic {
	/**
	 * 上一次注水的时间
	 */
	private long addValAutoTime;
	/**
	 * 起服时间
	 */
	private long serverStartTime;
	/**
	 * 活动类
	 */
	private SeasonPuzzleActivity activity;

	public SeasonPuzzleAutoLogic(SeasonPuzzleActivity activity) {
		this.activity = activity;
	}
	
	/**
	 * 自动注水
	 */
	protected void autoAddPuzzle() {
		long now = HawkTime.getMillisecond();
		if (addValAutoTime <= 0) {
			addValAutoTime = now;
			return;
		}
		if (now - addValAutoTime < 60000L) {
			return;
		}

		addValAutoTime = now;
		String lockKey = SeasonPuzzleConst.REDIS_KEY_ZHU_LOCK;
		boolean succ = getRedis().setNx(lockKey, String.valueOf(now));
		if (!succ) {
			return;
		}
		
		try {
			List<int[]> addParams = SeasonPuzzleConstCfg.getInstance().getPuzzleAddValList();
			int addVal = getAutoAddVal("completePuzzle", now, 0, addParams);
			if (addVal > 0) {
				getRedis().increaseBy(activity.getGlobalPuzzleKey(), addVal, activity.getRedisExpire());
				HawkLog.logPrintln("SeasonPuzzleActivity autoAddPuzzle, addVal: {}, serverId: {}", addVal, activity.getDataGeter().getServerId());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			getRedis().del(lockKey);
		}
	}
	
	/**
	 * 获取注水数值
	 * @param type
	 * @param timeNow
	 * @param startTime
	 * @param paramList
	 * @return
	 */
	private int getAutoAddVal(String type, long timeNow, long startTime, List<int[]> paramList) {
		try {
			String addTimeKey = SeasonPuzzleConst.REDIS_KEY_ZHU_TIME + activity.getActivityTermId();
			String addTime = getRedis().hGet(addTimeKey, type);
			if (HawkOSOperator.isEmptyString(addTime)) {
				getRedis().hSetNx(addTimeKey, type, String.valueOf(timeNow));
				getRedis().expire(addTimeKey, activity.getRedisExpire());
				return 0;
			}
			
			int[] params = getAddParams(timeNow, paramList, startTime);
			if (params == null) {
				return 0;
			}
			
			long lastTime = Math.max(serverStartTime, Long.valueOf(addTime)); //addTime要考虑停服因素
			if (timeNow - lastTime >= params[2] * 1000L) {
				getRedis().hSet(addTimeKey, type, String.valueOf(timeNow));
				getRedis().expire(addTimeKey, activity.getRedisExpire());
				int addVal = HawkRand.randInt(params[3], params[4]);
				return addVal;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return 0;
	}
	
	/**
	 * 获取注水参数
	 * 
	 * @param now
	 * @return
	 */
	private int[] getAddParams(long now, List<int[]> paramList, long startTime) {
		if (startTime <= 0) {
			int termId = activity.getActivityTermId();
			startTime = activity.getTimeControl().getStartTimeByTermId(termId);
		}
		
		for (int[] params : paramList) {
			if (now >= startTime + params[0] * 1000L && now <= startTime + params[1] * 1000L) {
				return params;
			}
		}
		return null;
	}
	
	public long getServerStartTime() {
		return serverStartTime;
	}

	public void setServerStartTime(long serverStartTime) {
		this.serverStartTime = serverStartTime;
	}
	
	public HawkRedisSession getRedis() {
		return activity.getRedis();
	}
	
}
