package com.hawk.game.service.tiberium.logunit;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.task.HawkDelayTask;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkThreadPool;

import com.hawk.game.util.LogUtil;
/**
 * 泰伯相关日志延时记录工具类
 * @author z
 *
 */
public class TWLogUtil {
	
	private static long randDelayTime() {
		long baseTime = 5000l;
		long randTime = HawkRand.randInt(30000);
		return baseTime + randTime;
	}

	private static void addDelayTask(HawkDelayTask task) {
		HawkThreadPool taskPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (null != taskPool) {
			task.setTypeName("TWDelayTlog");
			taskPool.addTask(task, 0, false);
		}
	}
	
	/**
	 * 记录泰伯联盟战场积分
	 * @param logUnitList
	 */
	public static void logTimberiumGuildScoreInfo(TWGuildScoreLogUnit logUnit) {
		try {
			long delayTime = randDelayTime();
			addDelayTask(new HawkDelayTask(delayTime, delayTime, 1) {
				@Override
				public Object run() {
					LogUtil.logTimberiumGuildScoreInfo(logUnit);
					return null;
				}
			});
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯联赛战场结果信息
	 * @param logUnitList
	 */
	public static void logTimberiumLeaguaWarResult(TLWWarResultLogUnit logUnit) {
		try {
			long delayTime = randDelayTime();
			addDelayTask(new HawkDelayTask(delayTime, delayTime, 1) {
				@Override
				public Object run() {
					LogUtil.logTimberiumLeaguaWarResult(logUnit);
					return null;
				}
			});
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯联赛参与联盟信息
	 * @param logUnitList
	 */
	public static void logTimberiumLeaguaGuildInfo(TLWLeaguaGuildInfoUnit logUnit) {
		try {
			long delayTime = randDelayTime();
			addDelayTask(new HawkDelayTask(delayTime, delayTime, 1) {
				@Override
				public Object run() {
					LogUtil.logTimberiumLeaguaGuildInfo(logUnit);
					return null;
				}
			});
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯ELO积分流水
	 * @param logUnitList
	 */
	public static void logTimberiumEloScoreInfo(TWEloScoreLogUnit logUnit) {
		try {
			long delayTime = randDelayTime();
			addDelayTask(new HawkDelayTask(delayTime, delayTime, 1) {
				@Override
				public Object run() {
					LogUtil.logTimberiumEloScoreFlow(logUnit);
					return null;
				}
			});
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}

	/**
	 * 记录泰伯联赛参与玩家积分战力数据
	 * @param logUnitList
	 */
	public static void logTimberiumLeaguaPlayerScore(List<TWPlayerSeasonScoreLogUnit> logUnitList) {
		try {
			if (CollectionUtils.isEmpty(logUnitList)) {
				return;
			}
			long delayTime = randDelayTime();
			addDelayTask(new HawkDelayTask(delayTime, delayTime, 1) {
				@Override
				public Object run() {
					for (TWPlayerSeasonScoreLogUnit logUnit : logUnitList) {
						LogUtil.logTimberiumLeaguaPlayerScore(logUnit);
					}
					return null;
				}
			});
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}
	
	/**
	 * 记录泰伯玩家个人战场积分
	 * @param logUnitList
	 */
	public static void logTimberiumPlayerScoreInfo(List<TWPlayerScoreLogUnit> logUnitList) {
		try {
			if (CollectionUtils.isEmpty(logUnitList)) {
				return;
			}
			long delayTime = randDelayTime();
			addDelayTask(new HawkDelayTask(delayTime, delayTime, 1) {
				@Override
				public Object run() {
					for (TWPlayerScoreLogUnit logUnit : logUnitList) {
						LogUtil.logTimberiumPlayerScoreInfo(logUnit);
					}
					return null;
				}
			});
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}
	
	/**
	 * 记录泰伯玩家个人积分奖励发放日志
	 * @param logUnitList
	 */
	public static void logTimberiumLeaguaSelfReward(List<TWSelfRewardLogUnit> logUnitList) {
		try {
			if (CollectionUtils.isEmpty(logUnitList)) {
				return;
			}
			long delayTime = randDelayTime();
			addDelayTask(new HawkDelayTask(delayTime, delayTime, 1) {
				@Override
				public Object run() {
					for (TWSelfRewardLogUnit logUnit : logUnitList) {
						LogUtil.logTimberiumLeaguaSelfReward(logUnit);
					}
					return null;
				}
			});
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}
	
}
