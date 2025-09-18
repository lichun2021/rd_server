package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.RangeMap;
import com.hawk.game.util.GameUtil;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.item.ItemInfo;
import com.hawk.gamelib.GameConst;

/**
 * 赛博之战基础配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "xml/cyborg_const.xml")
public class CyborgConstCfg extends HawkConfigBase {

	/**
	 * 实例
	 */
	private static CyborgConstCfg instance = null;

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static CyborgConstCfg getInstance() {
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
	 * 报名战队排行条件
	 */
	private final int signRankLimit;

	/**
	 * 报名战队创建时间限制
	 */
	private final int teamCreateTimeLimit;
	
	/**
	 * x期之前的报名,不受战队创建时间限制
	 */
	private final int teamTimeLimitTermId;

	/**
	 * 报名选择时间
	 */
	private final String warTimeHour;

	/**
	 * 战队人数限制
	 */
	private final int teamMemberLimit;
	
	private final String teamNameNumLimit;
	
	/**
	 * 战队创建花费
	 */
	private final String createTeamCost;

	/**
	 * 联盟战队数量限制
	 */
	private final int teamNumLimit;

	/**
	 * 参与匹配最低人数限制
	 */
	private final int warMemberMinCnt;
	
	/**
	 * 报名时新增出战人员数量限制
	 */
	private final int warNewMemberLimit;
	
	/**
	 * 每个时间段最多报名战队数量
	 */
	private final int maxSignNum;

	/**
	 * 战场开启时间(单位:秒)
	 */
	private final int warOpenTime;

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
	 * 匹配战力公式参数1 (万分比)
	 */
	private final int matchParam1;

	/**
	 * 匹配战力公式参数2 (万分比)
	 */
	private final int matchParam2;

	/**
	 * 匹配战力公式参数3 (万分比)
	 */
	private final int matchParam3;

	/**
	 * 匹配战力公式参数4 (万分比)
	 */
	private final int matchParam4;
	/**
	 * 回原服最小等待时间.
	 */
	private final int minBackServerWaitTime;
	/**
	 *回原服最大时间,
	 */
	private final int maxBackServerWaitTime;
	/**
	 * 强制迁回时间.
	 */
	private final int forceMoveBackTime;
	
	/**
	 * 在结束多长时间之前不能进入
	 */
	private final int limitTimeBoforeEnd;
	
	/**
	 * 战场活跃状态检测周期
	 */
	private final int perioTime;
	
	/**
	 * 匹配范围参数(万分比)
	 */
	private final int matchTopRate;

	
	private final String killMaxRewards;
	private final String damagedMaxRewards;
	private final String monsterMaxRewards;
	

	/**# 赛博能量药剂分配的总数*/
	private final int cyborgItemTotal;

	
	private final int cyborgMatchTimesLimit;
	private final String cyborgMatchBattleResultCof;
	private final double cyborgMatchCofMaxValue;
	private final double cyborgMatchCofMinValue;
	private final int cyborgMatchSwitch;
	private final String newServerMatch;
	private final String serverMatchOpenDays;

	private RangeMap<Integer, Integer> newServerMatchRangeMap;
	
	
	private List<HawkTuple2<Integer, Integer>> timeList;
	
	private List<ItemInfo> teamCreateCostItem;
	
	private HawkTuple2<Integer, Integer> nameSize;
	
	private Map<Integer,Double> cyborgMatchBattleResultMap;
	
	private List<HawkTuple2<Integer, Integer>> serverMatchOpenDayList;

	/**
	 * 构造
	 */
	public CyborgConstCfg() {
		instance = this;
		isSystemClose = 0;
		serverDelay = 0;
		signRankLimit = 0;
		teamCreateTimeLimit = 0;
		teamTimeLimitTermId = 1;
		warTimeHour = "";
		teamMemberLimit = 0;
		teamNameNumLimit = "";
		createTeamCost = "";
		teamNumLimit = 0;
		warMemberMinCnt = 0;
		warOpenTime = 0;
		matchLockExpire = 300;
		matchPrepareTime = 300;
		awardDelayTime = 300;
		matchParam1 = 0;
		matchParam2 = 0;
		matchParam3 = 0;
		matchParam4 = 0;
		minBackServerWaitTime = 1000;
		maxBackServerWaitTime = 5000;
		forceMoveBackTime = 10;
		limitTimeBoforeEnd = 300;
		perioTime = 10_000;
		warNewMemberLimit = 5;
		maxSignNum = 100000;
		matchTopRate = 150;

		killMaxRewards = "";
		damagedMaxRewards = "";
		monsterMaxRewards = "";

		cyborgItemTotal = 3000;
		
		cyborgMatchTimesLimit = 1;
		cyborgMatchBattleResultCof ="";
		cyborgMatchCofMaxValue = 0;
		cyborgMatchCofMinValue = 0;
		cyborgMatchSwitch = 0;
		newServerMatch = "";
		serverMatchOpenDays = "";

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

	public long getTeamCreateTimeLimit() {
		return teamCreateTimeLimit * 1000l;
	}

	public int getTeamTimeLimitTermId() {
		return teamTimeLimitTermId;
	}

	public void setTimeList(List<HawkTuple2<Integer, Integer>> timeList) {
		this.timeList = timeList;
	}

	public final int getWarMemberMinCnt() {
		return warMemberMinCnt;
	}

	public final long getWarOpenTime() {
		return warOpenTime * 1000l;
	}
	
	
	public static final void setInstance(CyborgConstCfg instance) {
		CyborgConstCfg.instance = instance;
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
	
	public String getTeamNameNumLimit() {
		return teamNameNumLimit;
	}

	public String getCreateTeamCost() {
		return createTeamCost;
	}

	public List<ItemInfo> getTeamCreateCostItem() {
		return teamCreateCostItem;
	}
	
	public HawkTuple2<Integer, Integer> getNameSize() {
		return nameSize;
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
			this.teamCreateCostItem = ItemInfo.valueListOf(createTeamCost);
			this.nameSize = new HawkTuple2<Integer, Integer>(1, 8);
			if (!HawkOSOperator.isEmptyString(teamNameNumLimit)) {
				String[] nameLimit = teamNameNumLimit.split("_");
				this.nameSize = new HawkTuple2<Integer, Integer>(Integer.valueOf(nameLimit[0]), Integer.valueOf(nameLimit[1]));
			}
			
			Map<Integer,Double> matchBattleResult = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(this.cyborgMatchBattleResultCof)) {
				String[] arr = cyborgMatchBattleResultCof.split("_");
				for(int i=0;i< arr.length;i++){
					String str = arr[i];
					matchBattleResult.put(i+1, Double.parseDouble(str));
				}
			}
			this.cyborgMatchBattleResultMap =ImmutableMap.copyOf(matchBattleResult);

			//把字符串转换成RangeMap
			if(!HawkOSOperator.isEmptyString(this.newServerMatch)){
				newServerMatchRangeMap = ImmutableRangeMap.copyOf(SerializeHelper.str2RangeMap(newServerMatch));
			}
			
			List<HawkTuple2<Integer, Integer>> serverMatchOpenDayListTemp = new ArrayList<>();
			if(!HawkOSOperator.isEmptyString(this.serverMatchOpenDays)){
				String[] timeStrs = serverMatchOpenDays.split(",");
				for (String timeStr : timeStrs) {
					String[] strs = timeStr.split("_");
					int min = Integer.valueOf(strs[0]);
					int max = Integer.valueOf(strs[1]);
					HawkTuple2<Integer, Integer> tuple = new HawkTuple2<Integer, Integer>(min, max);
					serverMatchOpenDayListTemp.add(tuple);
				}
			}
			Collections.reverse(serverMatchOpenDayListTemp);
			this.serverMatchOpenDayList = ImmutableList.copyOf(serverMatchOpenDayListTemp);
		} catch (Exception e) {
			return false;
		}
		return super.assemble();
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

	public int getTeamMemberLimit() {
		return teamMemberLimit;
	}

	public int getTeamNumLimit() {
		return teamNumLimit;
	}

	public int getIsSystemClose() {
		return isSystemClose;
	}

	public int getMatchParam1() {
		return matchParam1;
	}

	public int getMatchParam2() {
		return matchParam2;
	}

	public int getMatchParam3() {
		return matchParam3;
	}

	public int getMatchParam4() {
		return matchParam4;
	}

	public int getMinBackServerWaitTime() {
		return minBackServerWaitTime;
	}

	public int getMaxBackServerWaitTime() {
		return maxBackServerWaitTime;
	}

	public long getForceMoveBackTime() {
		return forceMoveBackTime * 1000l;
	}

	public int getLimitTimeBoforeEnd() {
		return limitTimeBoforeEnd;
	}

	public int getPerioTime() {
		return perioTime;
	}

	public int getWarNewMemberLimit() {
		return warNewMemberLimit;
	}

	public int getMaxSignNum() {
		return maxSignNum;
	}

	public int getMatchTopRate() {
		return matchTopRate;
	}


	public List<ItemInfo> getKillMaxRewards() {
		return ItemInfo.valueListOf(this.killMaxRewards);
	}

	public List<ItemInfo> getDamagedMaxRewards() {	
		return ItemInfo.valueListOf(this.damagedMaxRewards);
	}

	public List<ItemInfo> getMonsterMaxRewards() {
		return ItemInfo.valueListOf(this.monsterMaxRewards);
	}


	public int getCyborgItemTotal() {
		return cyborgItemTotal;
	}

	
	
	public int getCyborgMatchTimesLimit() {
		return cyborgMatchTimesLimit;
	}
	
	public double getCyborgMatchCofMaxValue() {
		return cyborgMatchCofMaxValue / 10000;
	}
	
	public double getCyborgMatchCofMinValue() {
		return cyborgMatchCofMinValue / 10000;
	}
	
	public int getCyborgMatchSwitch() {
		return cyborgMatchSwitch;
	}
	
	public double getCyborgMatchBattleResultValue(int rank) {
		double rlt = cyborgMatchBattleResultMap.getOrDefault(rank, 0d);
		return rlt / 10000;
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
	
	public int getMatchOpenDaysPoolIndex(int days){
		for(int i=0;i<this.serverMatchOpenDayList.size();i++){
			HawkTuple2<Integer, Integer> tuple = this.serverMatchOpenDayList.get(i);
			if(tuple.first <= days && days <= tuple.second){
				return i;
			}
		}
		return -1;
	}
	
	public int getMatchOpenDaysPoolSize(){
		return this.serverMatchOpenDayList.size();
	}
}
