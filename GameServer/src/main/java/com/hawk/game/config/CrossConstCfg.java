package com.hawk.game.config;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple4;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.item.ItemInfo;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 跨服基础配置
 * 
 * @author Codej
 *
 */
@HawkConfigManager.KVResource(file = "xml/cross_const.xml")
public class CrossConstCfg extends HawkConfigBase {

	/**
	 * 实例
	 */
	private static CrossConstCfg instance = null;

	/**
	 * 获取实例
	 * @return
	 */
	public static CrossConstCfg getInstance() {
		return instance;
	}

	/**
	 * 总开关
	 */
	private final boolean isSystemOpen;

	/**
	 * 截止时间, 一般是以活动的end的状态前多长时间(s)
	 */
	private final int deadTime;

	/**
	 * 数据在redis中生命周期(s) = 432000
	 */
	private final int cActivityRedisExpire;

	/**
	 * 开启阶段全服buff
	 */
	private final String serverBuff;

	/**
	 * 排行tick周期
	 */
	private final long rankTickPeriod;

	/**
	 * 排行buff检测周期
	 */
	private final long rankBuffTickPeriod;

	/**
	 * 活动开始后奖励
	 */
	private final String cross_statReward;

	/**
	 * 服务器排名上限
	 */
	private final int cross_serverLimit;

	/**
	 * 个人排名上限
	 */
	private final int cross_personalLimit;

	/**
	 * 联盟排名奖励上限
	 */
	private final int cross_unionLimit;

	/**
	 * 跨服cd时间(s)
	 */
	private final int crossCd;

	/**
	 * 跨服保护罩时间(s)
	 */
	private final int crossProtect;
	/**
	 * 不可操作时间.
	 */
	private final int unoperatorTime;

	/**
	 * 战略点排行上限
	 */
	private final int talentRankLimit;

	/**
	 * 战略点联盟排行上限
	 */
	private final int talentGuildRankLimit;

	/**
	 * 充能联盟排行数量限制
	 */
	private final int chargeGuildRankLimit;

	/**
	 * 投票服务器排行数量限制
	 */
	private final int voteServerRankLimit;

	

	/**
	 * 单次充能消耗
	 */
	private final String chargeCost;

	/**
	 * 单次充能值
	 */
	private final int chargeValue;

	/**
	 * 充能上限(有区分先到到这个值，就开始投票) 
	 */
	private final int chargeMax;

	/**
	 * 胜利区服总统票数
	 */
	private final int presidentVoteCount;

	/**
	 * 交税时间间隔(s)
	 */
	private final int taxPeroid;

	/**
	 * 税率(万分比)
	 */
	private final int taxRate;
	
	/**
	 * 本服税率(万分比)
	 */
	private final int taxRateOwnServer;
	
	/**
	 * 税收记录条数限制
	 */
	private final int taxRecordLimit;

	/**
	 * 税收奖励接收次数限制
	 */
	private final int taxReceiveTimesLimit;

	/**
	 * 税收奖励接收数量限制
	 */
	private final int taxReceiveCountLimit;

	/**
	 * 医院恢复一次伤兵所需道具
	 */
	private final String hospitalRecoverCost;

	/**
	 * 跨服检测移除道具
	 */
	private final String crossCheckRemoveItem;
	
	/**
	 * 王战战斗时间(s)
	 */
	private final int presidentWarFareTime;
	
	/**
	 * 王战占领时间(s)
	 */
	private final int presidentOccupyTime;
	
	/**
	 * 刷新特殊矿的等级
	 */
	private final int specialResourceLevel;
	
	/**
	 * 刷新特殊矿的数量
	 */
	private final int specialResourceCount;

	/**
	 * 刷新特殊矿tick周期(21600s/6h)
	 */
	private final int specialResourceTickPeriod;
	
	/**
	 * 刷新特殊矿周期(s) 2天
	 */
	private final int specialResourcePeriod;
	
	/**
	 * 仓库单种资源最大值
	 */
	private final long taxMax;
	
	/**
	 * 能量塔占领一次给的积分
	 */
	private final int pylonOccupyScore;

	/**
	 * 首次占领奖励
	 */
	private final String firstOccupyAward;
	
	/**
	 * 占领奖励
	 */
	private final String occupyAward;
	
	/**
	 * 控制奖励
	 */
	private final String controlAward;
	
	/**
	 * 控制联盟奖励
	 */
	private final String controlGuildAward;
	
	/**
	 * 取联盟的前多少名
	 */
	private final int guildBattleNumber;
	
	/**
	 * 相对show time的偏移刷新工会战力. 
	 */
	private final long flushGuildBattleTime;
	
	/**
	 * 匹配时间,相对show time的偏移
	 */
	private final long matchTime;
	
	/**
	 * 开服时间到这一期的时间.
	 */
	private final long serverDelayTime;
	/**
	 * 一组跨服列表需要几个区服.
	 */
	private final int groupServerNumber;
	/**
	 * 限制区服.
	 */
	private final String limitServers;
	/**
	 * 杀死敌军，部队星级积分加成
	 */
	private final String killSoldierStarScoreAdd;
	/**
	 * 杀伤敌军，部队星级积分加成
	 */
	private final String damageSoldierStarScoreAdd;
	/**
	 * 最小的公会数参加匹配.
	 */
	private final int minGuildNumberJoinMatch;
	/**
	 * 单次有多少个区服.
	 */
	private final int matchSize;
	
	/**
	 * 是否是debug模式
	 */
	private final boolean debugMode;
	
	/**
	 * debug模式活动阶段 1SHOW 2OPEN 3END 4HIDDEN
	 */
	private final int debugActivityState;
	
	/**
	 * debug模式战斗阶段
		= 1; // 战斗阶段未开启
		= 2; // 展示阶段
		= 3; // 积分比拼阶段
		= 4; // 备战阶段
		= 5; // 王战阶段
		= 6; // 王战阶段
		= 7; // 等待结束阶段
		= 8; // 活动结束了
	 */
	private final int debugFightState;
	
	/**
	 * debug期数
	 */
	private final int debugTermId;
	
	/**
	 * 展示阶段开始时间(秒)
	 */
	private final int fightShowTime;
	
	/**
	 * 积分比拼阶段开始时间(秒)
	 */
	private final int fightScoreTime;
	
	/**
	 * 远征要塞开始时间(秒)
	 */
	private final int fightFortressStartTime;

	/**
	 * 远征要塞结束时间(秒)
	 */
	private final int fightFortressEndTime;
	
	/**
	 * 备战阶段开始时间(秒)
	 */
	private final int fightPrepareTime;
	
	/**
	 * 王战开始时间(秒)
	 */
	private final int fightPresidentTime;
	
	/**
	 * 结果展示阶段开始时间(秒)
	 */
	private final int fightShowEndTime ;
	
	/**
	 * 在远征要塞战斗，额外获得的积分比例，万分比 
	 */
	private final int fortressPointBuff;

	/**
	 * 在远征要塞战斗，额外获得的军功比例，万分比
	 */
	private final int fortressMeritBuff;

	/**
	 * 爆仓配置1207_10_1001,1208_10_1002,1209_10_1003,1210_10_1004
	 */
	private final String resBoxInfo;
	
	/**
	 * 资源狂欢，每种资源的最少刷新数量
	 */
	private final int resNumMin;

	/**
	 * 资源狂欢，每种资源的最大刷新数量
	 */
	private final int resNumMax;
	
	/**
	 * 资源狂欢，宝箱生成范围x1_y1_x2_y2
	 */
	private final String resBoxGenScope;
	
	/**
	 * 每期可领取资源宝箱个数
	 */
	private final int resBoxSoloMax;
	
	/**
	 * 出战联盟数量
	 */
	private final int fightGuildCount;
	
	/**
	 * 远征战略点发放周期
	 */
	private final int fortressPointMailPeroid;
	
	/**
	 * 能量塔出征最低数量
	 */
	private final int crossPylonArmyCountLimit;

	/**
	 * 远征要塞最大积分部队数量
	 */
	private final int crossFortressArmyNum;
	
	/**
	 * 国家远征战略点初始数量
	 */
	private final int talentInit;
	
	/**
	 * 物资宝箱爆出比例，万分比
	 */
	private final int resBoxProportion;
	
	
	/**
	 * 匹配参数
	 */
	private final int crossMatchNumLimit;
	private final int crossMatchTimesLimit;
	private final String crossMatchBattleResultCof;
	private final double crossMatchCofMaxValue;
	private final double crossMatchCofMinValue;
	
	/**
	 * 电塔占领时长任务检测周期(5s)
	 */
	private final int fortressOccupyMissionPeriod;
	
	/**
	 * 国家集结、盟主申请CD，单位：秒
	 */
	private final int crossRallyApplicationCd;
	
	/**
	 * 国家集结、司令邀请CD，单位：秒
	 */
	private final int crossRallyInviteCd;

	/**
	 * 自由军修改电塔归属CD，单位：秒
	 */
	private final int changeBelongCd;

	/**
	 * 在远征要塞战斗额外积分加成
	 */
	private final int fortressBattlePointBuff;

	/**
	 * 跨服盟总胜利后，国家军成员奖励
	 */
	private final String crossRallyWinReward;
	
	/**
	 * 跨服盟总争夺进度条总长度
	 */
	private final int crossProgressTotal;
	
	/**
	 * 跨服盟总争夺征服方初始长度
	 */
	private final int crossAttackInit;
	
	/**
	 * 跨服盟总争夺征服方推条速度
	 */
	private final int crossAttackSpeed;
	
	/**
	 * 跨服盟总争夺防守方推条速度
	 */
	private final int crossDefenseSpeed;
	

	/**
	 * 跨服盟总胜利累积战令时间，秒
	 */
	private final int crossWinAccumulateOccupyTime;
	
	
	private final String serverMatchOpenDays;
	
	
	/** 赛季-匹配偏移*/
	private final int  seasonMatchOffset;
	/** 赛季-初始积分*/
	private final int seasonInitScore;
	/** 赛季初始化积分递减量*/
	private final int seasonInitScoreDecay;
	/** 赛季-征服之星道具ID*/
	private final int seasonStarItem;
	/** 赛季-航海战败损失积分百分比*/
	private final int seasonScoreLosePer;
	/** 能量塔排行*/
	private final int fortressRankMax;
	
	// -----------------------------自己组装的---------------------------------

	/**
	 * 跨服活动全服作用号属性
	 */
	private List<EffectObject> effectList;

	/**
	 * 跨服活动开启时全服邮件奖励
	 */
	private List<ItemInfo> rewardList;
	/**
	 *不开跨服的区服.
	 */
	private List<String> limitServerList = new ArrayList<>();
	
	/**
	 * 跨服杀敌星级加成
	 */
	private Map<Integer, Integer> starKillMap;
	
	/**
	 * 跨服伤敌星级加成
	 */
	private Map<Integer, Integer> starHurtMap;

	/**
	 * 资源宝箱爆仓比率
	 */
	private Map<Integer, Integer> resBoxRateMap;
	
	/**
	 * 爆仓资源宝箱数量
	 */
	private Map<Integer, Integer> resBoxIdMap;
	
	/**
	 * 匹配参数
	 */
	private Map<Integer,Double> crossMatchBattleResultMap;
	
	
	private List<HawkTuple2<Integer, Integer>> serverMatchOpenDayList;
	
	// -----------------------------------------------------------------------

	/**
	 * 构造
	 */
	public CrossConstCfg() {
		instance = this;

		isSystemOpen = true;
		deadTime = 0;
		cActivityRedisExpire = 432000;
		serverBuff = "";
		cross_statReward = "";
		cross_serverLimit = 3;
		cross_personalLimit = 100;
		cross_unionLimit = 100;
		rankTickPeriod = 60000L;
		rankBuffTickPeriod = 60000L;
		crossCd = 0;
		crossProtect = 0;
		unoperatorTime = 0;

		talentRankLimit = 100;
		talentGuildRankLimit = 10;
		chargeGuildRankLimit = 10;
		voteServerRankLimit = 3;
		chargeCost = "";
		chargeValue = 1;
		chargeMax = 100;
		presidentVoteCount = 100;
		taxRecordLimit = 20;
		taxPeroid = 86400;
		taxReceiveTimesLimit = 10;
		taxReceiveCountLimit = 10000;
		hospitalRecoverCost = "";
		crossCheckRemoveItem = "";
		presidentWarFareTime = 600;
		presidentOccupyTime = 300;
		taxRate = 500;
		taxRateOwnServer = 500;
		
		specialResourceCount = 50;
		specialResourceLevel = 9;
		
		specialResourceTickPeriod = 21600;
		specialResourcePeriod = 172800;
		taxMax = 500000000;
		
		pylonOccupyScore = 1;
		
		firstOccupyAward = "";
		occupyAward = "";
		guildBattleNumber = 3;
		flushGuildBattleTime = 1500 * 1000l;
		matchTime = 600 * 1000l;
		serverDelayTime = 56 * HawkTime.DAY_MILLI_SECONDS;
		controlAward = "";
		controlGuildAward = "";
		groupServerNumber = 3;
		limitServers = "";
		killSoldierStarScoreAdd = "";
		damageSoldierStarScoreAdd = "";
		this.minGuildNumberJoinMatch = 10;
		this.matchSize = 15;
		debugMode = false;
		debugActivityState = 0;
		debugFightState = 0;
		debugTermId = 0;
		fightShowTime = 0;
		fightScoreTime = 0;
		fightFortressStartTime = 0;
		fightFortressEndTime = 0;
		fightPrepareTime = 0;
		fightPresidentTime = 0;
		fightShowEndTime = 0;
		fortressPointBuff = 0;
		fortressMeritBuff = 0;
		resNumMin = 0;
		resNumMax = 0;
		resBoxGenScope = "";
		fightGuildCount = 20;
		resBoxInfo = "";
		resBoxSoloMax = 0;
		fortressPointMailPeroid = 3600;
		crossPylonArmyCountLimit = 30000;
		crossFortressArmyNum = 100000;
		talentInit = 5000;
		resBoxProportion = 5000;
		
		crossMatchNumLimit = 0;
		crossMatchTimesLimit = 0;
		crossMatchBattleResultCof = "";
		crossMatchCofMaxValue = 0;
		crossMatchCofMinValue = 0;
		fortressOccupyMissionPeriod = 5;
		
		crossRallyApplicationCd = 0;
		crossRallyInviteCd = 0;
		changeBelongCd = 0;
		fortressBattlePointBuff = 0;
		crossRallyWinReward = "";
		crossProgressTotal = 0;
		crossAttackInit = 0;
		crossAttackSpeed = 0;
		crossDefenseSpeed = 0;
		
		crossWinAccumulateOccupyTime = 0;
		serverMatchOpenDays = "";
		seasonMatchOffset = 1;
		seasonInitScore = 0;
		seasonInitScoreDecay = 0;
		seasonStarItem = 0;
		seasonScoreLosePer = 0;
		fortressRankMax = 100;
	}

	public boolean isSystemOpen() {
		return isSystemOpen;
	}

	public long getDeadTime() {
		return deadTime * 1000l;
	}

	public int getcActivityRedisExpire() {
		return cActivityRedisExpire;
	}

	public List<EffectObject> getEffectList() {
		return effectList;
	}

	public long getRankTickPeriod() {
		return rankTickPeriod;
	}

	public long getRankBuffTickPeriod() {
		return rankBuffTickPeriod;
	}

	public int getCross_serverLimit() {
		return cross_serverLimit;
	}

	public int getCross_personalLimit() {
		return cross_personalLimit;
	}

	public int getCross_unionLimit() {
		return cross_unionLimit;
	}

	public int getTalentRankLimit() {
		return talentRankLimit;
	}

	public int getTalentGuildRankLimit() {
		return talentGuildRankLimit;
	}

	public int getChargeGuildRankLimit() {
		return chargeGuildRankLimit;
	}

	public int getVoteServerRankLimit() {
		return voteServerRankLimit;
	}

	public List<ItemInfo> getRewardList() {
		return Collections.unmodifiableList(rewardList);
	}

	/**
	 * 获取跨服cd时间(ms)
	 * @return
	 */
	public long getCrossCd() {
		return crossCd * 1000L;
	}

	/**
	 * 获取跨服保护时间(ms)
	 * @return
	 */
	public long getCrossProtect() {
		return crossProtect * 1000L;
	}

	public String getChargeCost() {
		return chargeCost;
	}

	public int getChargeValue() {
		return chargeValue;
	}

	public int getChargeMax() {
		return chargeMax;
	}

	public int getPresidentVoteCount() {
		return presidentVoteCount;
	}

	public List<ItemInfo> getChargeCostList() {
		return ItemInfo.valueListOf(chargeCost);
	}

	public int getTaxRecordLimit() {
		return taxRecordLimit;
	}

	public long getTaxPeroid() {
		return taxPeroid * 1000L;
	}

	public int getTaxReceiveTimesLimit() {
		return taxReceiveTimesLimit;
	}

	public int getTaxReceiveCountLimit() {
		return taxReceiveCountLimit;
	}

	public List<ItemInfo> getHospitalRecoverCostList() {
		return ItemInfo.valueListOf(hospitalRecoverCost);
	}

	public List<ItemInfo> getCrossCheckRemoveItemList() {
		return ItemInfo.valueListOf(crossCheckRemoveItem);
	}

	public int getPresidentWarFareTime() {
		return presidentWarFareTime;
	}

	public int getPresidentOccupyTime() {
		return presidentOccupyTime;
	}

	public int getTaxRate() {
		return taxRate;
	}

	public int getTaxRateOwnServer() {
		return taxRateOwnServer;
	}

	public int getSpecialResourceCount() {
		return specialResourceCount;
	}

	public int getPylonOccupyScore() {
		return pylonOccupyScore;
	}
	
	public String getKillSoldierStarScoreAdd() {
		return killSoldierStarScoreAdd;
	}

	public String getDamageSoldierStarScoreAdd() {
		return damageSoldierStarScoreAdd;
	}

	public Map<Integer, Integer> getStarKillMap() {
		return starKillMap;
	}

	public Map<Integer, Integer> getStarHurtMap() {
		return starHurtMap;
	}

	public int getFortressPointBuff() {
		return fortressPointBuff;
	}

	public int getFortressMeritBuff() {
		return fortressMeritBuff;
	}

	public int getResNumMin() {
		return resNumMin;
	}

	public int getResNumMax() {
		return resNumMax;
	}

	@Override
	protected boolean assemble() {

		this.rewardList = ItemInfo.valueListOf(cross_statReward);

		List<EffectObject> effectList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(serverBuff)) {
			String[] array = serverBuff.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				EffectObject effect = new EffectObject(Integer.parseInt(info[0]), Integer.parseInt(info[1]));
				effectList.add(effect);
			}
		}
		this.effectList = effectList;
		
		this.limitServerList = SerializeHelper.stringToList(String.class, limitServers, SerializeHelper.ATTRIBUTE_SPLIT);
		
		Map<Integer, Integer> starKillMap = new HashMap<>();
		if(!HawkOSOperator.isEmptyString(killSoldierStarScoreAdd)){
			String[] array = killSoldierStarScoreAdd.split(",");
			for(String val : array){
				String[]info = val.split("_");
				starKillMap.put(Integer.parseInt(info[0]), Integer.parseInt(info[1]));
			}
		}
		this.starKillMap = starKillMap;
		
		Map<Integer, Integer> starHurtMap = new HashMap<>();
		if(!HawkOSOperator.isEmptyString(damageSoldierStarScoreAdd)){
			String[] array = damageSoldierStarScoreAdd.split(",");
			for(String val : array){
				String[]info = val.split("_");
				starHurtMap.put(Integer.parseInt(info[0]), Integer.parseInt(info[1]));
			}
		}
		this.starHurtMap = starKillMap;
		if (this.groupServerNumber > matchSize) {
			throw new InvalidParameterException("匹配池比匹配组大,报错.");
		}
		
		Map<Integer, Integer> resBoxRateMap = new HashMap<>();
		Map<Integer, Integer> resBoxCountMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(resBoxInfo)) {
			String[] splitStr = resBoxInfo.split(",");
			for (int i = 0; i < splitStr.length; i++) {
				String split = splitStr[i];
				resBoxRateMap.put(Integer.valueOf(split.split("_")[0]), Integer.valueOf(split.split("_")[1]));
				resBoxCountMap.put(Integer.valueOf(split.split("_")[0]), Integer.valueOf(split.split("_")[2]));
			}
		}
		this.resBoxRateMap = resBoxRateMap;
		this.resBoxIdMap = resBoxCountMap;
		
		
		Map<Integer,Double> matchBattleResult = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(this.crossMatchBattleResultCof)) {
			String[] arr = crossMatchBattleResultCof.split("_");
			for(int i=0;i< arr.length;i++){
				String str = arr[i];
				matchBattleResult.put(i+1, Double.parseDouble(str));
			}
		}
		this.crossMatchBattleResultMap =ImmutableMap.copyOf(matchBattleResult);
		
		
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
		return super.assemble();
	}

	public int getUnoperatorTime() {
		return unoperatorTime;
	}

	public int getSpecialResourceLevel() {
		return specialResourceLevel;
	}

	public long getSpecialResourceTickPeriod() {
		return specialResourceTickPeriod * 1000L;
	}

	public long getSpecialResourcePeriod() {
		return specialResourcePeriod * 1000L;
	}

	public long getTaxMax() {
		return taxMax;
	}

	public String getFirstOccupyAward() {
		return firstOccupyAward;
	}

	public String getOccupyAward() {
		return occupyAward;
	}

	public int getGuildBattleNumber() {
		return guildBattleNumber;
	}

	public long getFlushServerBattleTime() {
		return flushGuildBattleTime;
	}

	public long getMatchTime() {
		return matchTime;
	}

	public long getServerDelayTime() {
		return serverDelayTime;
	}

	public String getControlAward() {
		return controlAward;
	}

	public String getControlGuildAward() {
		return controlGuildAward;
	}

	public int getGroupServerNumber() {
		return groupServerNumber;
	}

	public List<String> getLimitServerList() {
		return limitServerList;
	}

	public void setLimitServerList(List<String> limitServerList) {
		this.limitServerList = limitServerList;
	}

	public int getMinGuildNumberJoinMatch() {
		return minGuildNumberJoinMatch;
	}

	public int getMatchSize() {
		return matchSize;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public int getDebugActivityState() {
		return debugActivityState;
	}

	public int getDebugFightState() {
		return debugFightState;
	}

	public int getDebugTermId() {
		return debugTermId;
	}

	public long getFightShowTime() {
		return fightShowTime * 1000L;
	}

	public long getFightScoreTime() {
		return fightScoreTime * 1000L;
	}

	public long getFightPrepareTime() {
		return fightPrepareTime * 1000L;
	}

	public long getFightPresidentTime() {
		return fightPresidentTime * 1000L;
	}

	public long getFightShowEndTime() {
		return fightShowEndTime * 1000L;
	}

	public long getFightFortressStartTime() {
		return fightFortressStartTime * 1000L;
	}

	public long getFightFortressEndTime() {
		return fightFortressEndTime * 1000L;
	}
	
	public int getResBoxRate(int resType) {
		return resBoxRateMap.getOrDefault(resType, 0);
	}

	public int getResBoxId(int resType) {
		return resBoxIdMap.getOrDefault(resType, 0);
	}

	public HawkTuple4<Integer, Integer, Integer, Integer> getResBoxGenScope(){
		if(HawkOSOperator.isEmptyString(this.resBoxGenScope)){
			return null;
		}
		String[] arr = this.resBoxGenScope.split("_");
		HawkTuple4<Integer, Integer, Integer, Integer> tupe= new HawkTuple4<Integer, Integer, Integer, Integer>(
				Integer.parseInt(arr[0]),
				Integer.parseInt(arr[1]),
				Integer.parseInt(arr[2]),
				Integer.parseInt(arr[3]));
		return tupe;
	}
	
	public int getResBoxSoloMax() {
		return resBoxSoloMax;
	}

	public int getFightGuildCount() {
		return fightGuildCount;
	}

	public long getFortressPointMailPeroid() {
		return fortressPointMailPeroid * 1000L;
	}

	public int getCrossPylonArmyCountLimit() {
		return crossPylonArmyCountLimit;
	}

	public int getCrossFortressArmyNum() {
		return crossFortressArmyNum;
	}

	public Map<Integer, Integer> getResBoxIdMap() {
		return resBoxIdMap;
	}

	public int getTalentInit() {
		return talentInit;
	}

	public int getResBoxProportion() {
		return resBoxProportion;
	}
	
	public int getCrossMatchNumLimit() {
		return crossMatchNumLimit;
	}
	
	public int getCrossMatchTimesLimit() {
		return crossMatchTimesLimit;
	}
	
	public double getCrossMatchCofMaxValue() {
		return crossMatchCofMaxValue / 10000;
	}
	
	public double getCrossMatchCofMinValue() {
		return crossMatchCofMinValue  / 10000;
	}
	
	public double getCrossMatchBattleResultValue(int rank) {
		double rlt = crossMatchBattleResultMap.getOrDefault(rank, 0d);
		return rlt / 10000;
	}

	public long getFortressOccupyMissionPeriod() {
		return fortressOccupyMissionPeriod * 1000L;
	}

	public long getCrossRallyApplicationCd() {
		return crossRallyApplicationCd * 1000L;
	}

	public long getCrossRallyInviteCd() {
		return crossRallyInviteCd * 1000L;
	}

	public String getCrossRallyWinReward() {
		return crossRallyWinReward;
	}

	public long getChangeBelongCd() {
		return changeBelongCd * 1000L;
	}

	public int getFortressBattlePointBuff() {
		return fortressBattlePointBuff;
	}

	public int getCrossProgressTotal() {
		return crossProgressTotal;
	}

	public int getCrossAttackInit() {
		return crossAttackInit;
	}

	public int getCrossAttackSpeed() {
		return crossAttackSpeed;
	}

	public int getCrossDefenseSpeed() {
		return crossDefenseSpeed;
	}
	
	public int getCrossWinAccumulateOccupyTime() {
		return crossWinAccumulateOccupyTime;
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
	
	public int getSeasonMatchOffset() {
		return seasonMatchOffset;
	}
	
	public int getSeasonInitScore() {
		return seasonInitScore;
	}
	
	public int getSeasonInitScoreDecay() {
		return seasonInitScoreDecay;
	}
	
	public int getSeasonStarItem() {
		return seasonStarItem;
	}
	
	public int getSeasonScoreLosePer() {
		return seasonScoreLosePer;
	}
	
	public int getFortressRankMax() {
		return fortressRankMax;
	}
}
