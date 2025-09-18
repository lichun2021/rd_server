package com.hawk.activity.type.impl.resourceDefense.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 资源保卫战
 * @author golden
 *
 */
@HawkConfigManager.KVResource(file = "activity/resource_defense/resource_defense_activity_cfg.xml")
public class ResourceDefenseCfg extends HawkConfigBase {
	
	/**
	 * 开服延迟开放时间
	 */
	private final long serverDelay;
	
	/**
	 * 可被偷取次数
	 */
	private final int pickupCoverLimit;

	/**
	 * 每日可偷取次数
	 */
	private final int pickupLimit;
	
	/**
	 * 偷取列表数量
	 */
	private final int pickupListNum;
	
	/**
	 * 解锁高级宝箱价格
	 */
	private final int rewardPriceNum;
	
	/**
	 * 事件触发时间
	 */
	private final int eventTime;
	
	/**
	 * 事件处理获得经验
	 */
	private final int eventExp;
	
	/**
	 * 收取资源获得经验
	 */
	private final int collectExp;
	
	/**
	 * 偷取资源获得经验
	 */
	private final int pickupExp;
	
	/**
	 *  上油增长量
	 */
	private final int noEnsureIds;
	
	/**
	 * 满仓时间
	 */
	private final int resourceTime;
	
	/**
	 * 购买经验次数，整个活动期间
	 */
	private final int purchaseExp;
	
	/**
	 * 资源站数量限制 1~4
	 */
	private final int stationCountLimit;
	
	/**
	 * 资源站资源类型
	 */
	private final String stationResourceType;

	/**
	 * 触发上油事件概率 万分比   不触发上油的话就触发清理
	 */
	private final int evenOilRate;
	
	/**
	 * 资源增长周期(s)
	 */
	private final int resIncreasePeroid;
	
	/**
	 * 解锁高级奖励经验
	 */
	private final int unlockGiftExp;
	
	/**
	 * 可以被偷取的资源数量限制
	 */
	private final int canBeStealResLimit;
	
	/**
	 * 机器人资源数量随机  10000_50000
	 */
	private final String robotResourceRandom;
	
	private List<Integer> stationResourceTypeList;
	
	/**
	 * 机器人资源数量随机最小值
	 */
	private int robotResourceMin;
	
	/**
	 * 机器人资源数量随机最大值
	 */
	private int robotResourceMax;
	
	/**
	 * 被帮助次数限制
	 */
	private final int beHelpTimesLimit;
	
	/**
	 * 帮助时间减少(s)
	 */
	private final int helpTimeReduce;
	
	/**
	 * 记录数量
	 */
	private final int recordLimit;
	
	/**
	 * 零点增加次数
	 */
	private final int stealZeroTimeAdd;
	
	/**
	 * 偷取次数增加周期(秒)
	 */
	private final int stealAddPeriod;
	
	/**
	 * 偷取次数最大值
	 */
	private final int stealTimesMax;
	
	/**
	 * 偷取一个玩家的cd时间(秒)
	 */
	private final int stealOneCd;

	/**
	 * 各类机器人每日可偷晶体个数
	 */
	private final String robotStealNum;
	/**
	 * 各类型机器人刷出改率
	 */
	private final String robotRefreshWeight;
	/**
	 * 各类机器人最多可同时出现个数
	 */
	private final String robotShowNum;
	/**
	 * 特工技能每日免费次数
	 */
	private final int abilityfreeFreshNum;

	/**
	 * 活动开启30分钟以后有多久不给刷玩家可以偷取列表
	 */
	private final int stealPageBanTime;
	
	private Map<Integer, Integer> robotStealNumMap = new HashMap<>();
	private Map<Integer, Integer> robotRefreshWeightMap = new HashMap<>();
	private Map<Integer, Integer> robotShowNumMap = new HashMap<>();
	/**
	 * 单例
	 */
	private static ResourceDefenseCfg instance = null;

	
	public static ResourceDefenseCfg getInstance() {
		return instance;
	}

	/**
	 * 构造
	 */
	public ResourceDefenseCfg() {
		serverDelay = 0L;
		pickupCoverLimit = 5;
		pickupLimit = 10;
		pickupListNum = 10;
		rewardPriceNum = 18;
		eventTime = 3000;
		eventExp = 1000;
		collectExp = 500;
		pickupExp = 800;
		noEnsureIds = 100;
		resourceTime = 3000;
		purchaseExp = 1;
		stationCountLimit = 4;
		stationResourceType = "";
		resIncreasePeroid = 300;
		unlockGiftExp = 0;
		canBeStealResLimit = 100000;
		robotResourceRandom = "10000_50000";
		evenOilRate = 5000;
		beHelpTimesLimit = 10;
		helpTimeReduce = 100;
		recordLimit = 15;
		stealZeroTimeAdd = 5;
		stealAddPeriod = 3600;
		stealTimesMax = 10;
		stealOneCd = 3600;
		
		robotStealNum = "";
		robotRefreshWeight = "";
		robotShowNum = "";
		abilityfreeFreshNum = 0;
		stealPageBanTime = 4 * 3600;
		instance = this;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getPickupCoverLimit() {
		return pickupCoverLimit;
	}

	public int getPickupListNum() {
		return pickupListNum;
	}

	public int getRewardPriceNum() {
		return rewardPriceNum;
	}

	public long getEventTime() {
		return eventTime * 1000L;
	}

	public int getEventExp() {
		return eventExp;
	}

	public int getCollectExp() {
		return collectExp;
	}

	public int getPickupExp() {
		return pickupExp;
	}

	public int getNoEnsureIds() {
		return noEnsureIds;
	}

	public long getResourceTime() {
		return resourceTime * 1000L;
	}

	public int getPurchaseExp() {
		return purchaseExp;
	}

	public int getStationCountLimit() {
		return stationCountLimit;
	}
	
	public int getResIncreasePeroid() {
		return resIncreasePeroid;
	}

	public int getUnlockGiftExp() {
		return unlockGiftExp;
	}

	public int getCanBeStealResLimit() {
		return canBeStealResLimit;
	}

	public int getEvenOilRate() {
		return evenOilRate;
	}
	
	public int getBeHelpTimesLimit() {
		return beHelpTimesLimit;
	}

	public int getHelpTimeReduce() {
		return helpTimeReduce;
	}

	public int getRobotResourceMin() {
		return robotResourceMin;
	}

	public int getRobotResourceMax() {
		return robotResourceMax;
	}

	public int getRecordLimit() {
		return recordLimit;
	}

	@Override
	protected boolean assemble() {
		
		List<Integer> stationResourceTypeList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(stationResourceType)) {
			String[] array = stationResourceType.split("_");
			for (int i = 0; i < array.length; i++) {
				stationResourceTypeList.add(Integer.valueOf(array[i]));
			}
		}
		this.stationResourceTypeList = stationResourceTypeList;
		
		if (!HawkOSOperator.isEmptyString(robotResourceRandom)) {
			String[] resourceInfo = robotResourceRandom.split("_");
			robotResourceMin = Integer.parseInt(resourceInfo[0]);
			robotResourceMax = Integer.parseInt(resourceInfo[1]);
		}

		this.robotStealNumMap = SerializeHelper.stringToMap(robotStealNum, Integer.class, Integer.class, "_", ",");
		this.robotRefreshWeightMap = SerializeHelper.stringToMap(robotRefreshWeight, Integer.class, Integer.class, "_", ",");
		this.robotShowNumMap = SerializeHelper.stringToMap(robotShowNum, Integer.class, Integer.class, "_", ",");

		return true;
	}

	public List<Integer> getStationResourceTypeList() {
		return stationResourceTypeList;
	}

	public int getStealZeroTimeAdd() {
		return stealZeroTimeAdd;
	}

	public long getStealAddPeriod() {
		return stealAddPeriod * 1000L;
	}

	public int getStealTimesMax() {
		return stealTimesMax;
	}

	public long getStealOneCd() {
		return stealOneCd * 1000L;
	}

	public int getPickupLimit() {
		return pickupLimit;
	}


	public Map<Integer, Integer> getRobotStealNumMap() {
		return robotStealNumMap;
	}

	public Map<Integer, Integer> getRobotRefreshWeightMap() {
		return robotRefreshWeightMap;
	}

	public Map<Integer, Integer> getRobotShowNumMap() {
		return robotShowNumMap;
	}

	public int getAbilityfreeFreshNum() {
		return abilityfreeFreshNum;
	}

	public long getStealPageBanTime() {
		return stealPageBanTime * 1000;
	}
}
