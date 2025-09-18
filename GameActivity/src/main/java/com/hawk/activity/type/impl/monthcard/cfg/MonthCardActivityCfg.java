package com.hawk.activity.type.impl.monthcard.cfg;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple3;

import com.google.common.collect.ImmutableList;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 月卡周卡配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/monthCardData.xml")
public class MonthCardActivityCfg extends HawkConfigBase {
	@Id
	protected final int cardId;
	// 类型：同一类型的月卡，原始月卡用于前端展示，后续月卡分别作为续费月卡内容
	protected final int type;
	// 原始月卡标识
	protected final int initialCard;
	// 价格
	protected final String price;
	
	protected final int sustainType;
	
	// 持续有效的天数
	protected final int sustain;
	// 购买时一次性奖励
	protected final String oneOffAward;
	// 有效期间每日奖励
	protected final String dailyAward;
	// 有效期buff加成
	protected final String buff;
	// 续费后的月卡ID
	protected final int nextCard;
	// 续费有效期天数
	protected final int renewTime;
	// 有效期间的战力值
	protected final int power;
	//控制时间
	protected final String startTime;
	protected final String stopTime;
	protected final String hiddenTime;
	// 1：正常售卖, 2：已激活玩家，正常领奖；未激活玩家，可见但不可购买, 3：不可见，不可购买
	protected final int sellType;
	// 前置建筑条件：建筑ID
	protected final int frontBuild;
	
	private long startTimeValue;
	private long stopTimeValue;
	private long hiddenTimeValue;
	
	protected final String atkAttr;
	protected final String hpAttr;
	
	protected final String cardItem;
	/**
	 * 0是正常周卡 1是免费卡 2是定制自选卡
	 */
	protected final int cardUseType;
	/**
	 * 定制自选卡选择奖励个数
	 */
	protected final int madeRewardValue;
	/**
	 * 1是双倍  0是不双倍
	 */
	protected final int isDouble;
	
	//免费特权卡
	private static int freeType;
	private static Set<Integer> customTypeSet = new HashSet<>();
	
	private List<Integer> buffList;
	// <type, initialCard>
	private static Map<Integer, Integer> initialCardMap = new HashMap<Integer, Integer>();
	
	private static Map<Integer, Integer> cardTypeMap = new HashMap<Integer, Integer>();
	
	private static Map<Integer,HawkTuple3<Long, Long, Long>> timeLimitMap= new HashMap<>();
	
	private static Set<Integer> notInSellCardSet = new HashSet<Integer>();
	private static Map<Integer, Integer> frontBuildCardMap = new HashMap<Integer, Integer>();
	private static Map<Integer, Integer> itemCardMap = new HashMap<Integer, Integer>();

	public MonthCardActivityCfg() {
		cardId = 0;
		type = 0;
		initialCard = 1;
		price = "";
		sustainType = 0;
		sustain = 0;
		oneOffAward = "";
		dailyAward = "";
		buff = "";
		nextCard = 0;
		renewTime = 0;
		power = 0;
		startTime = "";
		stopTime = "";
		hiddenTime = "";
		sellType = 1;
		frontBuild = 0;
		atkAttr = "";
		hpAttr = "";
		cardItem = "";
		cardUseType = 0;
		madeRewardValue = 0;
		isDouble = 0;
	}
	
	public int getCardUseType() {
		return cardUseType;
	}
	
	public int getMadeRewardValue() {
		return madeRewardValue;
	}
	
	public int getSellType() {
		return sellType;
	}

	public int getCardId() {
		return cardId;
	}
	
	public String getPrice() {
		return price;
	}
	
	public int getSustainType() {
		return sustainType;
	}

	public int getSustain() {
		return sustain;
	}
	
	public long getValidEndTime(long purchaseTime) {
		return purchaseTime + ((long)sustain) * 24 * 3600 * 1000;
	}

	public String getOneOffAward() {
		return oneOffAward;
	}

	public String getDailyAward() {
		return dailyAward;
	}

	public String getBuff() {
		return buff;
	}

	public int getRenewLimitDay() {
		return renewTime;
	}
	
	public long getRenewEndTime(long purchaseTime) {
		return getValidEndTime(purchaseTime) + ((long)renewTime) * 24 * 3600 * 1000;
	}
	
	public boolean isRenewable() {
		return renewTime > 0;
	}

	public List<Integer> getBuffList() {
		return buffList;
	}
	
	public int getType() {
		return type;
	}

	public int getInitialCard() {
		return initialCard;
	}

	public int getNextCard() {
		return nextCard;
	}
	
	public int getPower() {
		return power;
	}
	
	public int getDouble() {
		return isDouble;
	}

	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(buff)) {
			String[] buffArr = buff.split(",");
			buffList = new ArrayList<>();
			for (int i = 0; i < buffArr.length; i++) {
				buffList.add(Integer.valueOf(buffArr[i]));
 			}
		} else {
			buffList = Collections.emptyList();
		}
		
		if (initialCard == 1) {
			initialCardMap.put(type, cardId);
		}
		
		cardTypeMap.put(cardId, type);
		if (sellType != 1) {
			notInSellCardSet.add(type);
		}
		
		if(!HawkOSOperator.isEmptyString(startTime)){
			startTimeValue = HawkTime.parseTime(startTime);
		}
		if(!HawkOSOperator.isEmptyString(stopTime)){
			stopTimeValue = HawkTime.parseTime(stopTime);
		}
		if(!HawkOSOperator.isEmptyString(hiddenTime)){
			hiddenTimeValue = HawkTime.parseTime(hiddenTime);
		}
		//时间限制
		HawkTuple3<Long, Long,Long> timeLimt = new HawkTuple3<Long, Long,Long>
			(startTimeValue, stopTimeValue, hiddenTimeValue);
		timeLimitMap.put(type, timeLimt);
		
		if (frontBuild > 0) {
			frontBuildCardMap.put(type, frontBuild);
		}
		
		if (!HawkOSOperator.isEmptyString(cardItem)) {
			ImmutableList<RewardItem.Builder> builderList = RewardHelper.toRewardItemImmutableList(cardItem);
			for (RewardItem.Builder builder : builderList) {
				itemCardMap.put(builder.getItemId(), cardId);
			}
		}
		
		//免费月卡
		if (cardUseType == 1) {
			freeType = type;
		} else if (cardUseType == 2) { //定制类特权卡
			customTypeSet.add(type);
		}
		
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(oneOffAward);
		if (!valid) {
			throw new InvalidParameterException(String.format("MonthCardActivityCfg reward error, id: %s , oneOffAward: %s", cardId, oneOffAward));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(dailyAward);
		if (!valid) {
			throw new InvalidParameterException(String.format("MonthCardActivityCfg reward error, id: %s , dailyAward: %s", cardId, dailyAward));
		}
		if(startTimeValue > stopTimeValue){
			throw new InvalidParameterException(String.format("MonthCardActivityCfg time error, id: %s , dailyAward: %s", cardId, dailyAward));
		}
		if(stopTimeValue > hiddenTimeValue){
			throw new InvalidParameterException(String.format("MonthCardActivityCfg time error, id: %s , dailyAward: %s", cardId, dailyAward));
		}
		return super.checkValid();
	}
	
	public String getCardItem() {
		return cardItem;
	}
	
	/**
	 * 根据类型获取初始月卡
	 * 
	 * @param type
	 * @return
	 */
	public static int getInitialCardByType(int type) {
		if (!initialCardMap.containsKey(type)) {
			return 0;
		}
		
		return initialCardMap.get(type);
	}
	
	public static int getInitialCard(int cardId) {
		if (!cardTypeMap.containsKey(cardId)) {
			return 0;
		}
		
		if (!initialCardMap.containsKey(cardTypeMap.get(cardId))) {
			return 0;
		}
		
		return initialCardMap.get(cardTypeMap.get(cardId));
	}
	
	public static int getMonthCardType(int cardId) {
		if (!cardTypeMap.containsKey(cardId)) {
			return 0;
		}
		
		return cardTypeMap.get(cardId);
	}
	
	public static Collection<Integer> getInitialCards() {
		return Collections.unmodifiableCollection(initialCardMap.values());
	}
	
	
	public static HawkTuple3<Long,Long,Long> getTimeLimit(int type){
		return timeLimitMap.get(type);
	}
	
	public static boolean inShow(int type,long time){
		HawkTuple3<Long,Long,Long> limit = getTimeLimit(type);
		if(limit == null){
			return false;
		}
		long startTimeStamp = limit.first;
		long hiddenTimeStamp = limit.third;
		if(time < startTimeStamp){
			return false;
		}
		if(time > hiddenTimeStamp){
			return false;
		}
		return true;
	}
	
	
	public static boolean inSell(int type,long time){
		HawkTuple3<Long,Long,Long> limit = getTimeLimit(type);
		if(limit == null){
			return false;
		}
		long startTimeStamp = limit.first;
		long stopTimeStamp = limit.second;
		if(time < startTimeStamp){
			return false;
		}
		if(time > stopTimeStamp){
			return false;
		}
		
		if (notInSellCardSet.contains(type)) {
			return false;
		}
		
		return true;
	}
	
	public static int getFrontBuildId(int type) {
		return frontBuildCardMap.getOrDefault(type, 0);
	}
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}	
	
	public static int getCardIdByItem(int itemId) {
		return itemCardMap.getOrDefault(itemId, 0);
	}
	
	public boolean isFreeCard() {
		return type == freeType;
	}
	
	public boolean isCustomType() {
		return customTypeSet.contains(type);
	}
	
	public static int getFreeType() {
		return freeType;
	}
	
	public static boolean isCustomTypeCard(int type) {
		return customTypeSet.contains(type);
	}
}
