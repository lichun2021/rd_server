package com.hawk.game.config;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableList;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 国家建设处常量表
 * @author zhenyu.shang
 * @since 2022年3月24日
 */
@HawkConfigManager.KVResource(file = "xml/nation_construction_const.xml")
public class NationConstCfg extends HawkConfigBase {
	
	
	/** 国家整体开放状态 */
	protected final int nationalOpen;
	
	
	protected final int initialTimes;
	
	/**
	 * 任务刷新数量
	 */
	protected final int refreshTimes;
	
	/**
	 * 每日任务上限
	 */
	protected final int timesLimit;

	/**
	 * 任务刷新消耗
	 */
	protected final String refreshCost;

	/**
	 * 任务刷新次数上限
	 */
	protected final int refreshTimesLimit;

	/**
	 * 任务需求兵数
	 */
	protected final int taskSoldiers;
	
	/**
	 * 开服多久国家系统开启( 单位是天 )
	 */
	protected final long nationOpenDelay;
	
	/**
	 * 重建值上限
	 */
	protected final int rebuildingLimit;
	
	/**
	 * 重建奖励
	 */
	protected final String rebuildingAward;
	
	/**
	 * 捐献上限
	 */
	protected final int rebuildingCountLimit;
	
	/**
	 * 捐献恢复时间
	 */
	protected final int rebuildingRecoveryTime;
	
	/**
	 * 个人奖励建筑道具
	 */
	protected final String buildItemContact;
	
	/**
	 * 消耗四种资源的量
	 */
	protected final String consumeRes;
	
	/**
	 * 单次捐献的贡献值
	 */
	protected final int rebuildVal;
	
	/**
	 * 建筑半径
	 */
	protected final int radius;
	
	/**
	 * 行军时间
	 */
	protected final int marchTime;
	
	/**
	 * 资助获得建设值
	 */
	protected final int supportBuilding;
	
	/**
	 * 每日资助次数上限
	 */
	protected final int supportLimit;
	
	/**
	 * 单次资助转换国家金条
	 */
	protected final int supportGold;
	
	/**
	 * 单次资助消耗
	 */
	protected final String supportCost;
	
	/**
	 * 资助奖励
	 */
	protected final String supportAward;
	
	/** 单次捐献消耗资源量 */
	protected final String warehouseConsumeRes;
	
	/** 单次捐献个人奖励  */
	protected final String warehouseAward;
	
	/** 捐献记录的存储条数上限值  */
	protected final int warehouseListLimit;
	
	/** 单次捐献最高上限 */
	protected final int warehouseConsumeLimit;
	
	/**
	 * 任务刷新时间（秒）
	 */
	protected final int missionRefreshTime;

	/**
	 * 0到1级初始刷新任务
	 */
	protected final int missionInitCount;

	/**
	 * 任务列表上限
	 */
	protected final int missionTaskLimit;

	/**
	 * 每日可购买任务数
	 */
	protected final int missionDayBuyTimes;

	/**
	 * 购买任务消耗
	 */
	protected final String missionPrice;

	/**
	 * 放弃任务时间间隔（秒）
	 */
	protected final int missionGiveupCd;

	/**
	 * 每周科技值上限
	 */
	protected final int missionWeekLimit;

	/**
	 * 玩家初始任务数量
	 */
	protected final int playerMissionInitCount;
	/**
	 * 盟军司令对应的国家军衔等级
	 */
	protected final int commanderMilitary;

	/**
	 * 助力时间（秒）
	 */
	protected final int assistTime;
	
	/**
	 * 助力奖励
	 */
	protected final String assistAward;
	
	/**
	 * 每日助力上限（秒）
	 */
	protected final int assistLimit;
	
	/**
	 * 取消升级返回资源比例-万分比
	 */
	protected final int cancelReturn;
	
	/**
	 * 单人助力时间(秒)
	 */
	protected final int assistTechTime;
	
	/**
	 * 助力奖励
	 */
	protected final String assistTechAward;
	
	/**
	 * 助力每日上限时间(秒)
	 */
	protected final int assistTechLimit;
	
	/**
	 * 取消升级返回资源比例(万分比)
	 */
	protected final int cancelTechReturn;
	/**
	 * 泰能兵死兵恢复特殊处理
	 */
	protected final String specialSoldierType;
	/**
	 *  泰能兵死兵恢复特殊处理时间
	 */
	protected final int specialSoldierTime;
	
	/**
	 * 飞船取消CD（单位秒）
	 */
	protected final int modelGiveUpCD;
	
	/**
	 * 飞船取消CD（单位秒）
	 */
	protected final int techGiveUpCD;
	
	/**
	 * 全局静态对象
	 */
	private static NationConstCfg instance = null;
	
	/**
	 * 消耗资源列表
	 */
	private ItemInfo[] consumeResItem = new ItemInfo[4];
	
	/**
	 * 任务刷新消耗
	 */
	private ItemInfo refreshCostItem;
	
	private Map<Integer, ItemInfo> warehouseConsumeMap = new HashMap<Integer, ItemInfo>();
	private List<ItemInfo> warehouseAwardItemList;
	
	private List<ItemInfo> assistAwardList;
	
	private Set<Integer> specialSoldierTypeSet = new HashSet<Integer>();
	
	
	private Map<Integer, Integer> buildItemContactMap = new HashMap<Integer, Integer>();
	
	public static NationConstCfg getInstance(){
		return instance;
	}
	
	public NationConstCfg() {
		this.nationalOpen = 0;
		this.initialTimes = 0;
		this.refreshTimes = 0;
		this.refreshCost = "";
		this.refreshTimesLimit = 0;
		this.taskSoldiers = 0;
		this.nationOpenDelay = 120;
		this.rebuildingLimit = 0;
		this.rebuildingAward = "";
		this.rebuildingCountLimit = 0;
		this.rebuildingRecoveryTime = 0;
		this.consumeRes = "";
		this.rebuildVal = 0;
		this.radius = 2;
		this.timesLimit = 0;
		this.marchTime = 0;
		this.supportBuilding = 0;
		this.supportLimit = 0;
		this.supportCost = "";
		this.supportGold = 0;
		this.supportAward = "";
		this.missionRefreshTime = 0;
		this.missionInitCount = 0;
		this.missionTaskLimit = 0;
		this.missionDayBuyTimes = 0;
		this.missionPrice = "";
		this.missionGiveupCd = 0;
		this.missionWeekLimit = 0;
		this.warehouseConsumeRes = "";
		this.warehouseAward = "";
		this.warehouseListLimit = 0;
		this.playerMissionInitCount = 0;
		this.commanderMilitary = 15;
		this.assistTime = 0;
		this.assistAward = "";
		this.assistLimit = 0;
		this.cancelReturn = 0;
		this.assistTechTime = 0;
		this.assistTechAward = "";
		this.assistTechLimit = 0;
		this.cancelTechReturn = 0;
		this.warehouseConsumeLimit = 0;
		this.buildItemContact = "";
		this.specialSoldierType = "";
		this.specialSoldierTime = 0;
		this.modelGiveUpCD = 0;
		this.techGiveUpCD = 0;
	}

	public int getNationalOpen() {
		return nationalOpen;
	}

	public int warehouseListLimit() {
		return warehouseListLimit;
	}

	public int getInitialTimes() {
		return initialTimes;
	}

	public int getRefreshTimes() {
		return refreshTimes;
	}

	public String getRefreshCost() {
		return refreshCost;
	}
	
	public int getRefreshTimesLimit() {
		return refreshTimesLimit;
	}

	public int getTaskSoldiers() {
		return taskSoldiers;
	}

	public long getNationOpenDelay() {
		return nationOpenDelay;
	}

	public int getRebuildingLimit() {
		return rebuildingLimit;
	}

	public String getRebuildingAward() {
		return rebuildingAward;
	}

	public int getRebuildingCountLimit() {
		return rebuildingCountLimit;
	}

	public int getRebuildingRecoveryTime() {
		return rebuildingRecoveryTime;
	}

	public String getConsumeRes() {
		return consumeRes;
	}

	public int getRebuildVal() {
		return rebuildVal;
	}
	
	public int getRadius() {
		return radius;
	}

	public int getTimesLimit() {
		return timesLimit;
	}

	public int getMarchTime() {
		return marchTime;
	}

	public int getSupportBuilding() {
		return supportBuilding;
	}

	public int getSupportLimit() {
		return supportLimit;
	}

	public int getSupportGold() {
		return supportGold;
	}

	public String getSupportCost() {
		return supportCost;
	}

	public String getSupportAward() {
		return supportAward;
	}
	
	public long getMissionRefreshTime() {
		return missionRefreshTime * 1000;
	}

	public int getMissionInitCount() {
		return missionInitCount;
	}

	public int getMissionTaskLimit() {
		return missionTaskLimit;
	}

	public int getMissionDayBuyTimes() {
		return missionDayBuyTimes;
	}

	public String getMissionPrice() {
		return missionPrice;
	}

	public long getMissionGiveupCd() {
		return missionGiveupCd * 1000L;
	}

	public int getMissionWeekLimit() {
		return missionWeekLimit;
	}

	public int getPlayerMissionInitCount() {
		return playerMissionInitCount;
	}
	
	public int getCommanderMilitary() {
		return commanderMilitary;
	}

	public long getAssistTechTime() {
		return assistTechTime * 1000L;
	}

	public String getAssistTechAward() {
		return assistTechAward;
	}

	public long getAssistTechLimit() {
		return assistTechLimit * 1000L;
	}

	public int getCancelTechReturn() {
		return cancelTechReturn;
	}

	public int getAssistTime() {
		return assistTime;
	}

	public String getAssistAward() {
		return assistAward;
	}

	public int getAssistLimit() {
		return assistLimit;
	}

	public int getCancelReturn() {
		return cancelReturn;
	}
	
	public int getWarehouseConsumeLimit() {
		return warehouseConsumeLimit;
	}

	public int getModelGiveUpCD() {
		return modelGiveUpCD;
	}

	public int getTechGiveUpCD() {
		return techGiveUpCD;
	}

	@Override
	protected boolean assemble() {
		String[] resArr = this.consumeRes.split(SerializeHelper.SEMICOLON_ITEMS);
		if(resArr.length != 4){
			HawkLog.errPrintln(" consumeRes error , olny need 4 res , current:{}", consumeRes);
			return false;
		}
		// 赋值
		for (int i = 0; i < resArr.length; i++) {
			consumeResItem[i] = new ItemInfo(resArr[i]);
		}
		
		refreshCostItem = new ItemInfo(this.refreshCost);
		
		warehouseAwardItemList = ItemInfo.valueListOf(warehouseAward);
		List<ItemInfo> itemList = ItemInfo.valueListOf(warehouseConsumeRes, ";");
		for (ItemInfo item : itemList) {
			warehouseConsumeMap.put(item.getItemId(), item);
		}
		
		List<ItemInfo> aalist = new ArrayList<ItemInfo>();
		if (!HawkOSOperator.isEmptyString(assistAward)) {
			String[] split1 = assistAward.split(SerializeHelper.BETWEEN_ITEMS);
			for (int i = 0; i < split1.length; i++) {
				ItemInfo itemInfo = new ItemInfo(split1[i]);
				aalist.add(itemInfo);
			}
		}
		this.assistAwardList = ImmutableList.copyOf(aalist);
		
		// 加载个人建筑值奖励
		if(!HawkOSOperator.isEmptyString(buildItemContact)){
			String[] split1 = buildItemContact.split(SerializeHelper.BETWEEN_ITEMS);
			for (int i = 0; i < split1.length; i++) {
				String[] split2 = split1[i].split(SerializeHelper.ATTRIBUTE_SPLIT);
				
				int buildId = Integer.parseInt(split2[0]);
				NationbuildingType nationbuildingType = NationbuildingType.valueOf(buildId);
				if(nationbuildingType == null){
					throw new InvalidParameterException(String.format("NationConstCfg person reward buildId error, buildId: %s ", split2[0]));
				}
				buildItemContactMap.put(buildId, Integer.parseInt(split2[1]));
			}
		}
		
		if (!HawkOSOperator.isEmptyString(specialSoldierType)) {
			String[] vals = specialSoldierType.split("_");
			for (String val : vals) {
				specialSoldierTypeSet.add(Integer.parseInt(val));
			}
		}
		
		
		// 一定要放在assemble方法的最后一行 ！！！
		instance = this;
		
		return super.assemble();
	}
	
	public int getSpecialSoldierTime(int soldierLv) {
		if (specialSoldierTypeSet.contains(soldierLv)) {
			return specialSoldierTime;
		}
		
		return 0;
	}
	
	public List<ItemInfo> getAssistAwardList() {
		return assistAwardList;
	}

	public ItemInfo getConsumeInfo(int itemId){
		for (ItemInfo itemInfo : consumeResItem) {
			if(itemInfo.getItemId() == itemId){
				return itemInfo;
			}
		}
		return null;
	}
	
	public ItemInfo getRefreshCostItem(){
		return refreshCostItem.clone();
	}
	
	public ItemInfo getWarehouseConsume(int itemId) {
		ItemInfo item = warehouseConsumeMap.get(itemId);
		if (item != null) {
			return item.clone();
		}
		
		return item;
	}
	
	public List<ItemInfo> getWarehouseAwardItems() {
		return warehouseAwardItemList.stream().map(e -> e.clone()).collect(Collectors.toList());
	}
	
	
	public Integer getBuildItemContact(Integer buildId) {
		return this.buildItemContactMap.get(buildId);
	}
	
}
