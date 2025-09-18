package com.hawk.game.module.lianmengyqzz.march.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.*;
import com.hawk.game.util.GameUtil;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 月球之战常量表
 * 
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "xml/moon_war_const.xml")
public class YQZZWarConstCfg extends HawkConfigBase {

	private final String openServer;
	private final int serverDelay;
	
	private final int unoperatorTime;
	
	private final int playerJoinFreeCount;
	private final int playerJoinExtraCount;
	private final int playerJoinExtraCountMin;
	
	
	private final int playerScoreRankSize;
	/**
	 * 匹配参数
	 */
	private final int moonMatchNumLimit;
	private final int moonMatchTimesLimit;
	private final String moonMatchBattleResultCof;
	private final double moonMatchCofMaxValue;
	private final double moonMatchCofMinValue;
	
	private final int joinGuildCount;
	
	private final int joinBattleCdTime;
	
	/**
	 * 战场活跃状态检测周期
	 */
	private final int perioTime;
	
	/**
	 * 强制迁回时间.
	 */
	private final int forceMoveBackTime;
	
	/**
	 *回原服最小时间,
	 */
	private final int minBackServerWaitTime;
	/**
	 *回原服最大时间,
	 */
	private final int maxBackServerWaitTime;

	private final String newServerMatch;

	private final String groupRankAdd;
	private final float groupWinpointAdd;
	private RangeMap<Integer, Integer> newServerMatchRangeMap;
	
	private List<String> openServerList;
	private Map<Integer,Double> moonMatchBattleResultMap;

	private Map<Integer, Integer> groupRankAddMap;

	private final String serverMatchOpenDays;

	private final int isGM;

	private RangeMap<Integer, Integer> serverMatchOpenDayRangeMap;

	public YQZZWarConstCfg() {
		openServer = "";
		serverDelay = 0;
		unoperatorTime = 0;
		playerJoinFreeCount = 100;
		playerJoinExtraCount = 150;
		playerScoreRankSize = 1000;
		moonMatchNumLimit = 0;
		moonMatchTimesLimit = 0;
		moonMatchBattleResultCof = "";
		moonMatchCofMaxValue = 0;
		moonMatchCofMinValue = 0;
		joinGuildCount = 5;
		perioTime = 10000;
		joinBattleCdTime = 600000;
		forceMoveBackTime = 30000;
		minBackServerWaitTime = 1000;
		maxBackServerWaitTime = 5000;
		newServerMatch = "";
		groupRankAdd = "1,1000;2,800;3,600;4,400;5,200;6,100";
		groupWinpointAdd = 5;
		isGM = 0;
		playerJoinExtraCountMin = 5;
		serverMatchOpenDays = "1_90,91_365,366_999999";
	}
	
	public int getUnoperatorTime() {
		return unoperatorTime;
	}
	
	public int getPlayerJoinFreeCount() {
		return playerJoinFreeCount;
	}
	
	public int getPlayerJoinExtraCount() {
		return playerJoinExtraCount;
	}

	public int getPlayerScoreRankSize() {
		return playerScoreRankSize;
	}
	
	public int getMoonMatchNumLimit() {
		return moonMatchNumLimit;
	}
	
	public int getMoonMatchTimesLimit() {
		return moonMatchTimesLimit;
	}
	
	public double getMoonMatchCofMaxValue() {
		return moonMatchCofMaxValue / 10000;
	}
	
	public double getMoonMatchCofMinValue() {
		return moonMatchCofMinValue  / 10000;
	}
	
	public double getMoonMatchBattleResultValue(int rank) {
		double rlt = moonMatchBattleResultMap.getOrDefault(rank, 0d);
		return rlt / 10000;
	}
	
	public int getJoinGuildCount() {
		return joinGuildCount;
	}
	
	public int getPerioTime() {
		return perioTime;
	}
	
	public int getJoinBattleCdTime() {
		return joinBattleCdTime;
	}
	
	public int getForceMoveBackTime() {
		return forceMoveBackTime;
	}
	
	public int getMaxBackServerWaitTime() {
		return maxBackServerWaitTime;
	}
	
	public int getMinBackServerWaitTime() {
		return minBackServerWaitTime;
	}
	
	
	public List<String> getOpenServerList() {
		return openServerList;
	}
	
	public int getServerDelay() {
		return serverDelay;
	}

	public boolean isGm(){
		return isGM == 1;
	}
	
	@Override
	protected boolean assemble() {
		Map<Integer,Double> matchBattleResult = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(this.moonMatchBattleResultCof)) {
			String[] arr = moonMatchBattleResultCof.split("_");
			for(int i=0;i< arr.length;i++){
				String str = arr[i];
				matchBattleResult.put(i+1, Double.parseDouble(str));
			}
		}
		this.moonMatchBattleResultMap =ImmutableMap.copyOf(matchBattleResult);
		
		List<String> openServers = new ArrayList<>();
		if(!HawkOSOperator.isEmptyString(this.openServer)){
			String[] arr = openServer.split(",");
			for(int i=0;i< arr.length;i++){
				String str = arr[i];
				openServers.add(str);
			}
		}
		this.openServerList = ImmutableList.copyOf(openServers);
		//把字符串转换成RangeMap
		if(!HawkOSOperator.isEmptyString(this.newServerMatch)){
			newServerMatchRangeMap = ImmutableRangeMap.copyOf(SerializeHelper.str2RangeMap(newServerMatch));
		}
		if(!HawkOSOperator.isEmptyString(this.groupRankAdd)){
			groupRankAddMap = SerializeHelper.stringToMap(this.groupRankAdd, Integer.class, Integer.class, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.SEMICOLON_ITEMS);
		}
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
		return super.assemble();
	}

	public double getMatchPowerParam(){
		int day = GameUtil.getServerOpenDay();
		Integer param = newServerMatchRangeMap.get(day);
		if(param == null){
			return 1f;
		}else {
			return 1f;
			//return param / 100f;
		}
	}

	public int getGroupRankAdd(int rank){
		return groupRankAddMap.getOrDefault(rank, 0);
	}

	public float getGroupWinpointAdd() {
		return groupWinpointAdd;
	}

	public int getPlayerJoinExtraCountMin() {
		return playerJoinExtraCountMin;
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
