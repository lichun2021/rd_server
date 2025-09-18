package com.hawk.activity.type.impl.aftercompetition;

import java.util.List;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import com.hawk.activity.type.impl.aftercompetition.cfg.AfterCompetitionConstCfg;
import com.hawk.activity.type.impl.aftercompetition.cfg.AfterCompetitionShopCfg;
import com.hawk.activity.type.impl.aftercompetition.data.GiftBigAwardInfo;

/**
 * 自动注水逻辑
 * 
 * @author lating
 *
 */
public class AfterCompetitionAutoLogic {
	/**
	 * 上一次注水的时间
	 */
	private long addHomageAutoTime;
	/**
	 * 起服时间
	 */
	private long serverStartTime;
	/**
	 * 活动类
	 */
	private AfterCompetitionActivity activity;

	public AfterCompetitionAutoLogic(AfterCompetitionActivity activity) {
		this.activity = activity;
	}
	
	/**
	 * 自动注水
	 */
	protected void autoAddHomage() {
		long now = HawkTime.getMillisecond();
		if (addHomageAutoTime <= 0) {
			addHomageAutoTime = now;
			return;
		}
		if (now - addHomageAutoTime < 60000L) {
			return;
		}

		addHomageAutoTime = now;
		String lockKey = AfterCompetitionConst.REDIS_KEY_ZHU_LOCK;
		boolean succ = getRedis().setNx(lockKey, String.valueOf(now));
		if (!succ) {
			HawkLog.logPrintln("AfterCompetitionActivity autoAddHomage lock failed, serverId: {}", activity.getDataGeter().getServerId());
			return;
		}
		
		HawkLog.logPrintln("AfterCompetitionActivity autoAddHomage into, serverId: {}", activity.getDataGeter().getServerId());
		try {
			List<int[]> homageAddParams = AfterCompetitionConstCfg.getInstance().getHomageAddValList();
			int addVal = getAutoAddHomageVal("homage", now, 0, homageAddParams);
			if (addVal > 0) {
				getRedis().increaseBy(activity.getGlobalHomageKey(), addVal, activity.getRedisExpire());
				HawkLog.logPrintln("AfterCompetitionActivity autoAddHomage, addVal: {}, serverId: {}", addVal, activity.getDataGeter().getServerId());
			}
			
			ConfigIterator<AfterCompetitionShopCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(AfterCompetitionShopCfg.class);
			while (iterator.hasNext()) {
				try {
					AfterCompetitionShopCfg giftCfg = iterator.next();
					GiftBigAwardInfo giftInfo = activity.getGiftInfo(giftCfg.getId());
					if (giftInfo != null) {
						addVal = getAutoAddHomageVal("gift-" + giftCfg.getId(), now, giftInfo.getUnlockTime(), giftCfg.getShopAddValList());
						if (addVal > 0) {
							getRedis().hIncrBy(activity.getGiftBuyCountKey(), String.valueOf(giftCfg.getId()), addVal, activity.getRedisExpire());
							HawkLog.logPrintln("AfterCompetitionActivity autoAddHomage gift: {}, addVal: {}, serverId: {}", giftCfg.getId(), addVal, activity.getDataGeter().getServerId());
						}
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			getRedis().del(lockKey);
		}
	}
	
	/**
	 * 获取注水数值
	 * @param homageType
	 * @param timeNow
	 * @param startTime
	 * @param paramList
	 * @return
	 */
	private int getAutoAddHomageVal(String homageType, long timeNow, long startTime, List<int[]> paramList) {
		try {
			String addTimeKey = AfterCompetitionConst.REDIS_KEY_ZHU_TIME + activity.getActivityTermId();
			String addTime = getRedis().hGet(addTimeKey, homageType);
			if (HawkOSOperator.isEmptyString(addTime)) {
				getRedis().hSetNx(addTimeKey, homageType, String.valueOf(timeNow));
				getRedis().expire(addTimeKey, activity.getRedisExpire());
				return 0;
			}
			
			int[] params = getAddHomageParams(timeNow, paramList, startTime);
			if (params == null) {
				return 0;
			}
			
			long lastTime = Math.max(serverStartTime, Long.valueOf(addTime)); //addTime要考虑停服因素
			if (timeNow - lastTime >= params[2] * 1000L) {
				getRedis().hSet(addTimeKey, homageType, String.valueOf(timeNow));
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
	private int[] getAddHomageParams(long now, List<int[]> paramList, long startTime) {
		int termId = activity.getActivityTermId();
		long actEndTime = activity.getTimeControl().getEndTimeByTermId(termId);
		if (actEndTime - now <= HawkTime.HOUR_MILLI_SECONDS) {
			return null;
		}
		
		if (startTime <= 0) {
			startTime = activity.getTimeControl().getStartTimeByTermId(termId);
		}
		for (int[] params : paramList) {
			long beginTime = startTime + params[0] * 1000L, endTime = startTime + params[1] * 1000L;
			HawkLog.debugPrintln("AfterCompetitionActivity getAddHomageParams, param1: {}, params2: {}, startTime: {}, startTime: {}, endTime: {}", params[0], params[1], HawkTime.formatTime(startTime), HawkTime.formatTime(beginTime), HawkTime.formatTime(endTime));
			if (now >= beginTime && now <= endTime) {
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
