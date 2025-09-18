package com.hawk.activity.type.impl.snowball.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

/**
 * 雪球大战
 * @author golden
 *
 */
@HawkConfigManager.KVResource(file = "activity/snowball/snowball_cfg.xml")
public class SnowballCfg extends HawkConfigBase {

	/**
	 * 开服延迟开放时间
	 */
	private final long serverDelay;

	/**
	 * 游戏开始时间 12:00,20:00
	 */
	private final String beginTime;
	
	/**
	 * 游戏持续时间(s)
	 */
	private final int continueTime;
	
	/**
	 * 击中积分
	 */
	private final int atkScore;
	
	/**
	 * 连续击中积分增长(万分比)
	 */
	private final int continueAtkIncrease;
	
	/**
	 * 连续击中积分增长上限(万分比)
	 */
	private final int continueAtkIncreaseMax;
	
	/**
	 * 助攻积分
	 */
	private final int assistScore;
	
	/**
	 * 进球助攻积分
	 */
	private final int goalAssistScore;
	
	/**
	 * 雪球刷新数量
	 */
	private final int refreshBallCount;
	
	/**
	 * 踢一个球的次数限制
	 */
	private final int kickOnceLimit;
	
	/**
	 * 行军加速buff值(万分比)
	 */
	private final int marchSpeedValue;

	/**
	 * 行军加速buff值上限(万分比)
	 */
	private final int marchSpeedValueMax;
	
	/**
	 * 刷新区域外层
	 */
	private final String refreshRangeOuter;
	
	/**
	 * 刷新区域内层
	 */
	private final String refreshRangeInter;

	/**
	 * 检测周期
	 */
	private final int tickPeriod;
	
	/**
	 * 行军距离
	 */
	private final String marchDistance;
	
	/**
	 * 个人排行榜行数限制
	 */
	private final int selfRankLimit;
	
	/**
	 * 联盟排行榜行数限制
	 */
	private final int guildRankLimit;
	
	/**
	 * 球门范围 x1,y1,10;x2,y2,10
	 */
	private final String torRange;
	
	/**
	 * 轮次剩余多久发公告(s)
	 */
	private final int turnRemainNoticeTime;

	/**
	 * 进球奖励获得限制
	 */
	private final int goalRewardLimit;
	
	/**
	 * 排行榜检测时间(s)
	 */
	private final int rankPeriod;
	
	/**-------------------------------------------------------------------------------*/
	
	/**
	 * 游戏开始时间毫秒数(距离当日零点)
	 */
	protected List<Long> beginTimeMillSeconds;
	
	/**
	 * 刷新区域顶点1
	 */
	protected int[] refreshRangePoint1;
	
	/**
	 * 刷新区域顶点2
	 */
	protected int[] refreshRangePoint2;
	
	/**
	 * 刷新区域顶点3
	 */
	protected int[] refreshRangePoint3;
	
	/**
	 * 刷新区域顶点4
	 */
	protected int[] refreshRangePoint4;
	
	/**
	 * 行军距离
	 */
	private Map<Integer, Integer> marchDistanceMap;
	
	/**
	 * 球门范围
	 */
	private Map<Integer, Integer> torRangeMap;
	
	/**-------------------------------------------------------------------------------*/
	
	
	/**
	 * 单例
	 */
	private static SnowballCfg instance = null;

	
	public static SnowballCfg getInstance() {
		return instance;
	}

	/**
	 * 构造
	 */
	public SnowballCfg() {
		instance = this;
		serverDelay = 0L;
		beginTime = "12:00,13:01";
		continueTime = 0;
		atkScore = 0;
		continueAtkIncrease = 0;
		continueAtkIncreaseMax = 0;
		assistScore = 0;
		goalAssistScore = 0;
		refreshBallCount = 0;
		kickOnceLimit = 0;
		marchSpeedValue = 0;
		marchSpeedValueMax = 0;
		refreshRangeOuter = "126_252,634_1268";
		refreshRangeInter = "189_387,571_1142";
		tickPeriod = 10;
		marchDistance = "50000_5,100000_10,200000_20";
		selfRankLimit = 100;
		guildRankLimit = 10;
		torRange = "";
		turnRemainNoticeTime = 600;
		goalRewardLimit = 0;
		rankPeriod = 60;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public String getBeginTime() {
		return beginTime;
	}

	public long getContinueTime() {
		return continueTime * 1000L;
	}

	public int getAtkScore() {
		return atkScore;
	}

	public int getContinueAtkIncrease() {
		return continueAtkIncrease;
	}

	public int getContinueAtkIncreaseMax() {
		return continueAtkIncreaseMax;
	}

	public int getAssistScore() {
		return assistScore;
	}

	public int getGoalAssistScore() {
		return goalAssistScore;
	}

	public int getRefreshBallCount() {
		return refreshBallCount;
	}

	public int getKickOnceLimit() {
		return kickOnceLimit;
	}

	public int getMarchSpeedValue() {
		return marchSpeedValue;
	}
	
	public int getMarchSpeedValueMax() {
		return marchSpeedValueMax;
	}

	public long getTickPeriod() {
		return tickPeriod * 1000L;
	}

	public int getSelfRankLimit() {
		return selfRankLimit;
	}

	public int getGuildRankLimit() {
		return guildRankLimit;
	}

	public List<Long> getBeginTimeMillSeconds() {
		return beginTimeMillSeconds;
	}

	public int[] getRefreshRangePoint1() {
		return refreshRangePoint1;
	}

	public int[] getRefreshRangePoint2() {
		return refreshRangePoint2;
	}

	public int[] getRefreshRangePoint3() {
		return refreshRangePoint3;
	}

	public int[] getRefreshRangePoint4() {
		return refreshRangePoint4;
	}

	public Map<Integer, Integer> getMarchDistanceMap() {
		return marchDistanceMap;
	}

	public Map<Integer, Integer> getTorRangeMap() {
		return torRangeMap;
	}
	
	public long getTurnRemainNoticeTime() {
		return turnRemainNoticeTime * 1000L;
	}

	public int getGoalRewardLimit() {
		return goalRewardLimit;
	}

	public long getRankPeriod() {
		return rankPeriod * 1000L;
	}

	@Override
	protected boolean assemble() {
		
		List<Long> beginTimeMillSeconds = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(beginTime)) {
			String[] beginTimeSplit = beginTime.split(",");
			for (int i = 0; i < beginTimeSplit.length; i++) {
				String[] time = beginTimeSplit[i].split(":");
				int hour = Integer.parseInt(time[0]);
				int minute = Integer.parseInt(time[1]);
				
				long millSeconds = hour * HawkTime.HOUR_MILLI_SECONDS + minute * HawkTime.MINUTE_MILLI_SECONDS;
				beginTimeMillSeconds.add(millSeconds);
			}
		}
		this.beginTimeMillSeconds = beginTimeMillSeconds;

		
		if (!HawkOSOperator.isEmptyString(refreshRangeOuter)) {
			
			String[] rangeOuter = refreshRangeOuter.split(",");
			
			String[] point1Str = rangeOuter[0].split("_");
			int[] point1 = new int[2];
			point1[0] = Integer.parseInt(point1Str[0]);
			point1[1] = Integer.parseInt(point1Str[1]);
			refreshRangePoint1 = point1;
			
			String[] point2Str = rangeOuter[1].split("_");
			int[] point2 = new int[2];
			point2[0] = Integer.parseInt(point2Str[0]);
			point2[1] = Integer.parseInt(point2Str[1]);
			refreshRangePoint2 = point2;
		}
		
		if (!HawkOSOperator.isEmptyString(refreshRangeInter)) {
			String[] rangeInter = refreshRangeInter.split(",");
			
			String[] point3Str = rangeInter[0].split("_");
			int[] point3 = new int[2];
			point3[0] = Integer.parseInt(point3Str[0]);
			point3[1] = Integer.parseInt(point3Str[1]);
			refreshRangePoint3 = point3;
			
			String[] point4Str = rangeInter[1].split("_");
			int[] point4 = new int[2];
			point4[0] = Integer.parseInt(point4Str[0]);
			point4[1] = Integer.parseInt(point4Str[1]);
			refreshRangePoint4 = point4;
		}

		Map<Integer, Integer> marchDistanceMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(marchDistance)) {
			String[] distanceSplit = marchDistance.split(",");
			for (int i = 0; i < distanceSplit.length; i++) {
				String[] info = distanceSplit[i].split("_");
				marchDistanceMap.put(Integer.valueOf(info[0]), Integer.valueOf(info[1]));
			}
		}
		this.marchDistanceMap = marchDistanceMap;
		
		Map<Integer, Integer> torRangeMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(torRange)) {
			String[] tor = torRange.split(";");
			for (int i = 0; i < tor.length; i++) {
				String[] split = tor[i].split(",");
				int x = Integer.parseInt(split[0]);
				int y = Integer.parseInt(split[1]);
				int pointId = (y << 16) | x;
				Integer range = Integer.valueOf(split[2]);
				torRangeMap.put(pointId, range);
			}
		}
		this.torRangeMap = torRangeMap; 
		
		return true;
	}
}
