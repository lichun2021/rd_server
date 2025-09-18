package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;

/**
 * 战地之王配置
 * @author golden
 *
 */
@HawkConfigManager.KVResource(file = "xml/war_flag_const.xml")
public class WarFlagConstProperty extends HawkConfigBase {

	/**
	 * 单例
	 */
	private static WarFlagConstProperty instance = null;
	
	/**
	 * 
	 * @return
	 */
	public static WarFlagConstProperty getInstance() {
		return instance;
	}
	
	/**
	 * 半径
	 */
	private final int flagRadius;

	/**
	 * tick周期
	 */
	private final long tickPeriod;
	
	/**
	 * 最大建筑值
	 */
	private final int maxBuildLife;
	
	/**
	 * 建造速度
	 */
	private final int buildSpeed;
	
	/**
	 * 产出资源周期(s)
	 */
	private final int productResourcePeriod;
	
	/**
	 * 战地旗帜产出
	 */
	private final String flagResource;
	
	/**
	 * 玩家旗帜资源上限
	 */
	private final String flagResourceLimit;
	
	/**
	 * 联盟旗帜数量
	 */
	private final String flagCount;
	
	/**
	 * 点资源产出转化率
	 */
	private final int pointProRate;
	
	/**
	 * 旗子地图比例尺
	 */
	private final int flagMapRatio;
	
	/**
	 * 红点检测周期(s)
	 */
	private final int redPointCheckPeriod;
	
	/**
	 * 活动关闭tick周期(ms)
	 */
	private final long closeTickPeriod;
	
	/**
	 * 活动关闭一次移除数量
	 */
	private final int closeOnceRemove;
	
	/**
	 * 最大占领值
	 */
	private final int flagOccupy;
	
	/**
	 * 占领时加速倍数
	 */
	private final int flagOccupyDouble;
	
	/**
	 * 母旗建造值
	 */
	private final int bigFlagOccupy;
	
	/**
	 * 小旗达到指定数量可解锁母旗
	 */
	private final String unlockBigFlag;
	
	/**
	 * 母旗拥有产出宝箱的格子数
	 */
	private final int bigFlagCells;
	
	/**
	 * 母旗宝箱格子tick奖励时间（秒）
	 */
	private final int bigFlagCellsTickTime;
	
	/**
	 * 母旗在黑土地特殊奖励内容
	 */
	private final String bigFlagSpecialReward;
	
	/**
	 * 母旗结算时间节点（每日整点）
	 */
	private final String bigFlagAccountTimeList;

	/**
	 * 母旗结算时间延迟(秒)
	 */
	private final int bigFlagAccountTimeDelay;
	
	/**
	 * 母旗辐射半径
	 */
	private final int bigFlagRadius;
	
	/**
	 * 母旗可以激活数量(连接到总部)
	 */
	private final int bigFlagActiveCount;
	
	/**
	 * 拆除旗子时间(秒)
	 */
	private final int removeFlagTime;

	/**
	 * 拆除母旗时间(秒)
	 */
	private final int centerRemoveFlagTime;
	
	/**
	 * 自动建筑值增加周期(秒)
	 */
	private final int autoLifeAddPeroid;
	
	/**
	 * 自动建筑值增加
	 */
	private final int autoLifeAdd;
	
	/**
	 * 母旗奖励排序
	 */
	private final String bigFlagRewardSort;
	
	/**
	 * 战地旗帜产出
	 */
	private List<ItemInfo> resource;
	
	/**
	 * 玩家旗帜资源上限
	 */
	private Map<Integer, Integer> resourceLimit;
	
	/**
	 * 旗帜数量控制
	 */
	private List<List<Integer>> flagCountLimit;
	
	/**
	 * 母旗数量限制
	 */
	private List<Integer> centerFlagCountLimit;
	
	/**
	 * 母旗结算时间点
	 */
	private List<Integer> bigFlagAccountTime;
	
	/**
	 * 母旗奖励排序列表
	 */
	private List<Integer> bigFlagRewardSortList;
	
	/**
	 * 构造
	 */
	public WarFlagConstProperty() {
		instance = this;
		flagRadius = 5;
		tickPeriod = 1000;
		maxBuildLife = 0;
		buildSpeed = 10;
		flagResource = "";
		productResourcePeriod = 30;
		flagResourceLimit = "";
		flagCount = "";
		pointProRate = 10000;
		flagMapRatio = 5;
		redPointCheckPeriod = 1800;
		closeTickPeriod = 3000L;
		closeOnceRemove = 20;
		flagOccupy = 1000;
		flagOccupyDouble = 3;
		bigFlagOccupy = 50000;
		unlockBigFlag = "10_30_50_70";
		bigFlagCells = 30;
		bigFlagCellsTickTime = 7200;
		bigFlagSpecialReward = "30000_800000_1";
		bigFlagAccountTimeList = "1_9_17";
		bigFlagRadius = 13;
		bigFlagAccountTimeDelay = 300;
		bigFlagActiveCount = 4;
		removeFlagTime = 300;
		bigFlagRewardSort = "";
		autoLifeAdd = 100;
		autoLifeAddPeroid = 20;
		centerRemoveFlagTime =300;
	}

	public int getFlagRadius(boolean isCenter) {
		return isCenter ? bigFlagRadius : flagRadius;
	}

	public long getTickPeriod() {
		return tickPeriod;
	}

	public int getMaxBuildLife(boolean isCenter) {
		return isCenter ? bigFlagOccupy : maxBuildLife;
	}

	public int getBuildSpeed() {
		return buildSpeed;
	}

	public int getPointProRate() {
		return pointProRate;
	}

	public int getFlagMapRatio() {
		return flagMapRatio;
	}

	public long getProductResourcePeriod() {
		return productResourcePeriod * 1000L;
	}

	public int getRedPointCheckPeriod() {
		return redPointCheckPeriod;
	}

	public long getCloseTickPeriod() {
		return closeTickPeriod;
	}

	public int getCloseOnceRemove() {
		return closeOnceRemove;
	}

	public int getFlagOccupy(boolean isCenter) {
		return isCenter ? bigFlagOccupy : flagOccupy;
	}

	public List<ItemInfo> getResource() {
		return resource;
	}

	public int getFlagOccupyDouble() {
		return flagOccupyDouble;
	}

	public int getResourceLimit(int resId) {
		Integer limit = resourceLimit.get(resId);
		return (limit == null) ? 0 : limit;
	}

	public List<List<Integer>> getFlagCountLimit() {
		return flagCountLimit;
	}

	/**
	 * 获取母旗数量限制
	 * @param currentCount 当前数量
	 */
	public int getCenterFlagCountLimit(int currentCount) {
		int count = 0;
		for (Integer countLimit : centerFlagCountLimit) {
			if (currentCount >= countLimit) {
				count++;
			}
		}
		return count;
	}
	
	public int getBigFlagOccupy() {
		return bigFlagOccupy;
	}

	public String getUnlockBigFlag() {
		return unlockBigFlag;
	}

	public int getBigFlagCells() {
		return bigFlagCells;
	}

	public long getBigFlagCellsTickTime() {
		return bigFlagCellsTickTime * 1000L;
	}

	public String getBigFlagSpecialReward() {
		return bigFlagSpecialReward;
	}

	public String getBigFlagAccountTimeList() {
		return bigFlagAccountTimeList;
	}

	public int getBigFlagRadius() {
		return bigFlagRadius;
	}

	public int getBigFlagAccountTimeDelay() {
		return bigFlagAccountTimeDelay;
	}

	public List<Integer> getBigFlagAccountTime() {
		return bigFlagAccountTime;
	}

	public int getBigFalgActiveCount() {
		return bigFlagActiveCount;
	}

	
	public long getRemoveFlagTime() {
		return removeFlagTime * 1000L;
	}

	public List<Integer> getBigFlagRewardSortList() {
		return bigFlagRewardSortList;
	}

	public long getAutoLifeAddPeroid() {
		return autoLifeAddPeroid * 1000L;
	}

	public int getAutoLifeAdd() {
		return autoLifeAdd;
	}

	public int getCenterRemoveFlagTime() {
		return centerRemoveFlagTime;
	}

	@Override
	protected boolean assemble() {
		
		
		List<ItemInfo> resource = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(flagResource)) {
			resource = ItemInfo.valueListOf(flagResource);
		}
		this.resource = resource;
		
		
		Map<Integer, Integer> resourceLimit = new HashedMap<>();
		if (!HawkOSOperator.isEmptyString(flagResourceLimit)) {
			List<ItemInfo> limit = new ArrayList<>();
			limit = ItemInfo.valueListOf(flagResourceLimit);
			for (ItemInfo item : limit) {
				resourceLimit.put(item.getItemId(), (int)item.getCount());
			}
		}
		this.resourceLimit = resourceLimit;

		
		List<List<Integer>> flagCountLimit = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(flagCount)) {
			String[] single = flagCount.split(",");
			for (String s : single) {
				List<Integer> flagLimit = new ArrayList<>();
				String[] c = s.split("_");
				flagLimit.add(Integer.valueOf(c[0]));
				flagLimit.add(Integer.valueOf(c[1]));
				flagLimit.add(Integer.valueOf(c[2]));
				flagCountLimit.add(flagLimit);
			}
		}
		this.flagCountLimit = flagCountLimit;
		
		
		List<Integer> centerFlagCountLimit = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(unlockBigFlag)) {
			String[] single = unlockBigFlag.split("_");
			for (String s : single) {
				centerFlagCountLimit.add(Integer.valueOf(s));
			}
		}
		this.centerFlagCountLimit = centerFlagCountLimit;

		List<Integer> bigFlagAccountTime = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(bigFlagAccountTimeList)) {
			String[] split = bigFlagAccountTimeList.split("_");
			for (int i = 0; i < split.length; i++) {
				bigFlagAccountTime.add(Integer.valueOf(split[i]));
			}
		}
		this.bigFlagAccountTime = bigFlagAccountTime;
		
		List<Integer> bigFlagRewardSortList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(bigFlagRewardSort)) {
			String[] split = bigFlagRewardSort.split("_");
			for (int i = 0; i < split.length; i++) {
				bigFlagRewardSortList.add(Integer.parseInt(split[i]));
			}
		}
		this.bigFlagRewardSortList = bigFlagRewardSortList;
		
		return super.assemble();
	}
	
	@Override
	protected boolean checkValid() {
		if (HawkOSOperator.isEmptyString(bigFlagAccountTimeList)) {
			throw new RuntimeException("war_flg_const.xml bigFlagAccountTimeList error !");
		}
		return true;
	}
}
