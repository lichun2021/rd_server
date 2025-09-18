package com.hawk.game.config;

import java.util.*;

import com.google.common.collect.*;
import com.hawk.game.util.GameUtil;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;

/**
 * 泰伯利亚之战基础配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "xml/tiberium_const.xml")
public class TiberiumConstCfg extends HawkConfigBase {
	
	/**
	 * 实例
	 */
	private static TiberiumConstCfg instance = null;
	
	/**
	 * 获取实例
	 * @return
	 */
	public static TiberiumConstCfg getInstance() {
		return instance;
	}
	
	/**
	 * 总开关
	 */
	private final int isSystemClose;

	/**
	 * 开服时长不足的服务器不参与泰伯利亚之战(单位:秒)
	 */
	private final int serverDelay;

	/**
	 * 报名联盟排行条件
	 */
	private final int signRankLimit;
	
	/**
	 * 报名联盟创建时间限制
	 */
	private final int guildCreateTimeLimit;

	/**
	 * 报名选择时间
	 */
	private final String warTimeHour;

	/**
	 * 排行tick周期
	 */
	private final int warMemberLimit;

	/**
	 * 参与匹配最低人数限制
	 */
	private final int warMemberMinCnt;
	
	/**
	 *  战场开启时间(单位:秒)
	 */
	private final int warOpenTime;
	/**
	 * 在结束多长时间之前不能进入
	 */
	private final int limitTimeBoforeEnd;
	
	/**
	 * 强制迁回时间.
	 */
	private final int forceMoveBackTime;
	
	/**
	 * 周期时间.
	 */
	private final int perioTime;
	/**
	 * 回原服最小等待时间.
	 */
	private final int minBackServerWaitTime;
	/**
	 *回原服最大时间,
	 */
	private final int maxBackServerWaitTime;
	
	/**
	 * 匹配竞争锁有效期(单位:秒)
	 */
	private final int matchLockExpire;
	
	/**
	 * 匹配准备时间(单位:秒)
	 */
	private final int matchPrepareTime;
	
	/**
	 * 延迟发奖时间(单位:秒)
	 */
	private final int awardDelayTime;
	
	/**
	 * 房间服选取是否开启随机模式(默认id小的服为房间服)
	 */
	private final int roomServerRandomOpen;
	
	private final int isTestMatch;
	
	/**
	 * 作战小队人数上限
	 */
	private final int teamMemberLimit;
	/**
	 * 作战小队任务数量上限
	 */
	private final int teamTargetLimit;
	/**
	 * 个人策略目标上限
	 */
	private final int teamMemberStrategyLimit;

	/**
	 * 个人主力兵种数量上限
	 */
	private final int mySoldierTypeLimit;
	
	/**
	 * 小队数量上限
	 */
	private final int teamNumLimit;
	
	/**
	 * 每个时间段最多报名联盟数量
	 */
	private final int maxSignNum;
	
	/**
	 * 联赛排行数量
	 */
	private final int powerRankSize;
	
	/**
	 * 刷新本服战力前X名联盟的数据到全服排行
	 */
	private final int updateRankLimit;
	
	/**
	 * 联赛联盟单场积分上限
	 */
	private final long seasonGuildScoreLimit;
	/**
	 * 联赛个人单场积分上限
	 */
	private final long seasonPersonScoreLimit;
	
	/**
	 * 是否开启表演赛OB模式
	 */
	private final int isPerform;
	
	/**
	 * 开启elo规则的期数,只在此期泰伯利亚匹配时计算初始化积分(附加战力额外积分),此期前采用战力匹配,此期及以后采取elo匹配
	 */
	private final int eloBeginTermId;
	
	/**
	 * 初始化elo积分
	 */
	private final int initEloScore;
	
	/**
	 * 每亿出战战力额外积分(仅在首次开启elo算法时在匹配阶段初始化添加此额外战力)
	 */
	private final int extPowerScore;
	
	/**
	 * ELO算法K值
	 */
	private final int eloParamK;
	
	/**
	 * ELO算法L值(概率计算参数)
	 */
	private final double eloParamL;
	
	/**
	 * ELO积分赛季结算保留系数(万分比)
	 */
	private final int eloSeasonCalcPer;
	
	/**
	 * 连续多期不参与比赛,积分衰减
	 */
	private final int eloAbsentDissScore;
	
	/**
	 * 积分衰减触发轮次
	 */
	private final int eloAbsentTerms;
	
	
	private final int tiberiumMatchTimesLimit;
	private final String tiberiumMatchBattleResultCof;
	private final double tiberiumMatchCofMaxValue;
	private final double tiberiumMatchCofMinValue;
	private final int tiberiumMatchSwitch;

	private final String newServerMatch;

	private final int guildPickCnt;

	private final int teamCnt;

	private final int eliminationStartTermId;

	private final int eliminationFinalTermId;

	private final String teamNameNumLimit;
	private final String createTeamCost;
	private final int preparationTime;
	//private final int teamMemberLimit;
	private final int teamPreparationLimit;
	//private final int teamNumLimit;
	private final int isNewOpen;
	private final String onlyOldServers;
	private final String serverMatchOpenDays;

	private RangeMap<Integer, Integer> newServerMatchRangeMap;
	
	private List<HawkTuple2<Integer, Integer>> timeList;
	
	
	private double tiberiumMatchBattleResultWin;
	private double tiberiumMatchBattleResultLoss;
	private Set<String> onlyOldServerSet;
	private RangeMap<Integer, Integer> serverMatchOpenDayRangeMap;
	/**
	 * 构造
	 */
	public TiberiumConstCfg() {
		instance = this;

		isSystemClose = 0;
		serverDelay = 0;
		signRankLimit = 0;
		guildCreateTimeLimit = 0;
		warTimeHour = "";
		warMemberLimit = 0;
		warMemberMinCnt = 0;
		warOpenTime = 0;
		limitTimeBoforeEnd = 300;
		forceMoveBackTime = 10;
		perioTime = 10_000;
		minBackServerWaitTime = 1000;
		maxBackServerWaitTime = 5000;
		matchLockExpire = 120;
		matchPrepareTime = 300;
		awardDelayTime = 300;
		roomServerRandomOpen = 0;
		isTestMatch = 0;
		maxSignNum = 400;
		teamMemberLimit = 12;
		teamTargetLimit = 2;
		teamMemberStrategyLimit = 2;
		mySoldierTypeLimit = 2;
		teamNumLimit = 12;
		powerRankSize = 500;
		updateRankLimit = 30;
		seasonGuildScoreLimit = 100000;
		seasonPersonScoreLimit = 100000;
		isPerform = 0;
		eloBeginTermId = 0;
		initEloScore = 1200;
		extPowerScore = 30;
		eloParamK = 200;
		eloParamL = 400;
		eloSeasonCalcPer = 0;
		eloAbsentDissScore = 50;
		eloAbsentTerms = 2;
		
		tiberiumMatchTimesLimit = 0;
		tiberiumMatchBattleResultCof = "";
		tiberiumMatchCofMaxValue = 0;
		tiberiumMatchCofMinValue = 0;
		tiberiumMatchSwitch = 0;
		newServerMatch = "";
		guildPickCnt = 256;
		teamCnt = 32;
		eliminationStartTermId = 8;
		eliminationFinalTermId = 21;
		teamNameNumLimit = "";
		createTeamCost = "";
		preparationTime = 0;
		//teamMemberLimit = 0;
		teamPreparationLimit = 0;
		//teamNumLimit = 0;
		isNewOpen = 1;
		onlyOldServers = "";
		serverMatchOpenDays = "1_90,91_365,366_999999";
	}

	public boolean isSystemClose() {
		return isSystemClose == 1;
	}

	public final long getServerDelay() {
		return serverDelay * 1000l;
	}

	public final int getSignRankLimit() {
		return signRankLimit;
	}

	public final String getWarTimeHour() {
		return warTimeHour;
	}

	public final int getWarMemberLimit() {
		return warMemberLimit;
	}

	public final int getWarMemberMinCnt() {
		return warMemberMinCnt;
	}
	
	public final long getWarOpenTime() {
		return warOpenTime * 1000l;
	}

	public int getPowerRankSize() {
		return powerRankSize;
	}
	
	public int getUpdateRankLimit() {
		return updateRankLimit;
	}

	public static final void setInstance(TiberiumConstCfg instance) {
		TiberiumConstCfg.instance = instance;
	}
	

	public final List<HawkTuple2<Integer, Integer>> getTimeList() {
		List<HawkTuple2<Integer, Integer>> list = new ArrayList<>();
		for (HawkTuple2<Integer, Integer> tuple : timeList) {
			list.add(new HawkTuple2<Integer, Integer>(tuple.first, tuple.second));
		}
		return list;
	}

	public HawkTuple2<Integer, Integer> getTimeList(int index) {
		if (index >= timeList.size()) {
			return null;
		}
		return timeList.get(index);
	}

	@Override
	protected boolean assemble() {
		try {
			List<HawkTuple2<Integer, Integer>> timeList = new ArrayList<>();
			if (!HawkOSOperator.isEmptyString(warTimeHour)) {
				String[] timeStrs = warTimeHour.split("_");
				for (String timeStr : timeStrs) {
					String[] strs = timeStr.split(":");
					int hour = Integer.valueOf(strs[0]);
					int minute = Integer.valueOf(strs[1]);
					if (hour < 0 || hour > 24 || minute < 0 || minute > 60) {
						return false;
					}
					HawkTuple2<Integer, Integer> tuple = new HawkTuple2<Integer, Integer>(hour, minute);
					timeList.add(tuple);
				}
			}
			timeList.sort(new Comparator<HawkTuple2<Integer, Integer>>() {
				@Override
				public int compare(HawkTuple2<Integer, Integer> arg0, HawkTuple2<Integer, Integer> arg1) {
					if (arg0.first != arg1.first) {
						return arg0.first - arg1.first;
					} else {
						return arg0.second - arg1.second;
					}
				}
			});
			this.timeList = timeList;
			
			if (!HawkOSOperator.isEmptyString(this.tiberiumMatchBattleResultCof)) {
				String[] arr = tiberiumMatchBattleResultCof.split("_");
				if(arr.length != 2){
					return false;
				}
				this.tiberiumMatchBattleResultWin = Double.parseDouble(arr[0]);
				this.tiberiumMatchBattleResultLoss = Double.parseDouble(arr[1]);
			}
			//把字符串转换成RangeMap
			if(!HawkOSOperator.isEmptyString(this.newServerMatch)){
				newServerMatchRangeMap = ImmutableRangeMap.copyOf(SerializeHelper.str2RangeMap(newServerMatch));
			}

			Set<String> onlyOldServerSetTmp = new HashSet<>();
			if(!HawkOSOperator.isEmptyString(this.onlyOldServers)){
				for(String rangeStr : this.onlyOldServers.split(",")){
					// 如果包含'-'，说明是一个范围
					if (rangeStr.contains("-")) {
						String[] range = rangeStr.split("-");
						int start = Integer.parseInt(range[0]);
						int end = Integer.parseInt(range[1]);

						// 将范围内的所有数字转换为字符串并加入列表
						for (int i = start; i <= end; i++) {
							onlyOldServerSetTmp.add(String.valueOf(i));
						}
					} else {
						// 单个数字，直接加入列表
						onlyOldServerSetTmp.add(rangeStr);
					}
				}
			}
			onlyOldServerSet = onlyOldServerSetTmp;
			RangeMap<Integer, Integer> serverMatchOpenDayRangeMapTmp = TreeRangeMap.create();
			if(!HawkOSOperator.isEmptyString(this.serverMatchOpenDays)){
				String[] timeStrs = serverMatchOpenDays.split(",");
				int i = 0;
				for (String timeStr : timeStrs) {
					String[] strs = timeStr.split("_");
					int min = Integer.valueOf(strs[0]);
					int max = Integer.valueOf(strs[1]);
					serverMatchOpenDayRangeMapTmp.put(Range.closed(min, max), i);
					i++;
				}
			}
			this.serverMatchOpenDayRangeMap = serverMatchOpenDayRangeMapTmp;
		} catch (Exception e) {
			return false;
		}
		return super.assemble();
	}

	public int getLimitTimeBoforeEnd() {
		return limitTimeBoforeEnd;
	}

	public int getForceMoveBackTime() {
		return forceMoveBackTime * 1000;
	}

	public int getPerioTime() {
		return perioTime;
	}

	public int getMinBackServerWaitTime() {
		return minBackServerWaitTime;
	}

	public int getMaxBackServerWaitTime() {
		return maxBackServerWaitTime;
	}

	public int getMatchLockExpire() {
		return matchLockExpire;
	}

	public long getMatchPrepareTime() {
		return matchPrepareTime * 1000l;
	}

	public long getAwardDelayTime() {
		return awardDelayTime * 1000l;
	}

	public boolean isRoomServerRandomOpen() {
		return roomServerRandomOpen == 1;
	}

	public long getGuildCreateTimeLimit() {
		return guildCreateTimeLimit * 1000l;
	}
	
	/**
	 * 是否采用灰度匹配模式
	 * @return
	 */
	public boolean isTestMatch() {
		return isTestMatch == 1;
	}

	public int getMaxSignNum() {
		return maxSignNum;
	}

	public int getTeamMemberLimit() {
		return teamMemberLimit;
	}

	public int getTeamTargetLimit() {
		return teamTargetLimit;
	}

	public int getTeamMemberStrategyLimit() {
		return teamMemberStrategyLimit;
	}

	public int getMySoldierTypeLimit() {
		return mySoldierTypeLimit;
	}

	public int getTeamNumLimit() {
		return teamNumLimit;
	}

	public long getSeasonGuildScoreLimit() {
		return seasonGuildScoreLimit;
	}

	public long getSeasonPersonScoreLimit() {
		return seasonPersonScoreLimit;
	}
	
	public boolean isPerform() {
		return isPerform == 1;
	}

	public int getEloBeginTermId() {
		return eloBeginTermId;
	}

	public int getInitEloScore() {
		return initEloScore;
	}

	public int getExtPowerScore() {
		return extPowerScore;
	}

	public int getEloParamK() {
		return eloParamK;
	}

	public double getEloParamL() {
		return eloParamL;
	}

	public int getEloSeasonCalcPer() {
		return eloSeasonCalcPer;
	}

	public int getEloAbsentDissScore() {
		return eloAbsentDissScore;
	}

	public int getEloAbsentTerms() {
		return eloAbsentTerms;
	}
	
	
	public int getTiberiumMatchTimesLimit() {
		return tiberiumMatchTimesLimit;
	}
	
	public double getTiberiumMatchBattleResultWin() {
		return tiberiumMatchBattleResultWin/10000;
	}
	
	public double getTiberiumMatchBattleResultLoss() {
		return tiberiumMatchBattleResultLoss /10000;
	}
	
	public double getTiberiumMatchCofMaxValue() {
		return tiberiumMatchCofMaxValue /10000;
	}
	
	public double getTiberiumMatchCofMinValue() {
		return tiberiumMatchCofMinValue /10000;
	}
	
	public int getTiberiumMatchSwitch() {
		return tiberiumMatchSwitch;
	}

	public double getMatchPowerParam(){
		int day = GameUtil.getServerOpenDay();
		Integer param = newServerMatchRangeMap.get(day);
		if(param == null){
			return 1f;
		}else {
			return param / 100f;
		}
	}

	public int getGuildPickCnt() {
		return guildPickCnt;
	}

	public int getTeamCnt() {
		return teamCnt;
	}

	public int getEliminationStartTermId() {
		return eliminationStartTermId;
	}

	public int getEliminationFinalTermId() {
		return eliminationFinalTermId;
	}

	public String getTeamNameNumLimit() {
		return teamNameNumLimit;
	}

	public int getPreparationTime() {
		return preparationTime;
	}

	public int getTeamPreparationLimit() {
		return teamPreparationLimit;
	}

	public boolean IsNewOpen() {
		return isNewOpen == 1;
	}

	public Set<String> getOnlyOldServerSet() {
		return onlyOldServerSet;
	}

	public int serverMatchOpenDayWeight(){
		int day = GameUtil.getServerOpenDay();
		Integer weight = serverMatchOpenDayRangeMap.get(day);
		if(weight == null){
			return 0;
		}else {
			return weight;
		}
	}
}
