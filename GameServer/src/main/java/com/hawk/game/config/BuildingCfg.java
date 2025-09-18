package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.LimitType;

/**
 * 建筑配置
 *
 * @author julia
 *
 */
@HawkConfigManager.XmlResource(file = "xml/build.xml")
public class BuildingCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 类型
	protected final int level;
	
	// 荣耀等级
	private final int honor;
	
	// 荣耀升级阶段
	private final int progress;

	// 可以建造的最大数量
	protected final int maxNum;

	// 前置条件,必须创建前面的建筑
	protected final String frontBuild;
	
	/** 上一等级(阶段)建筑*/
	private final int frontStage;
	
	/** 下一等级(阶段)建筑*/
	private final int postStage;
	
	// 资源消耗 type_id_count,type_id_count
	protected final String buildCost;

	// 取消操作返还的东西
	private final String cancelReclaim;

	// 类型:单位(s)
	protected final int buildTime;

	// 奖励数据 type_id_count,type_id_count
	protected final String buildAward;

	// 建筑类型
	protected final int buildType;

	// 给队列使用，暂时未用，请不要修改
	private final int queueBuildType;

	// 地图长
	protected final int length;

	// 地图宽
	protected final int width;

	// 某一类建筑最大可建数目
	protected final int limitType;

	// 战力奖励
	protected final int battlePoint;
	// 产电量
	protected final long electricGenerate;

	// 占用电量
	protected final long electricConsume;
	// 产资源每min
	protected final int resPerHour;
	// 产资源上限
	protected final long resLimit;
	// 仓库资源保护
	protected final String resProtect;
	// 仓库
	protected final String resProtectPlus;
	// 士兵数量
	protected final int trainQuantity;
	// 士兵训练加速
	protected final int trainSpeed;

	// 援助减少时间
	protected final int assistTime;
	// 可受援助次数
	protected final int assistLimit;
	// 援助单位上限
	protected final int assistUnitLimit;
	// 市场负重
	protected final int marketBurden;
	// 市场税率
	protected final int marketTax;
	// 集结上限
	protected final int buildupLimit;
	// 行军单位数量
	protected final int attackUnitLimit;
	// 伤兵上限
	protected final int woundedLimit;
	// 资源保护：黄金
	protected final int resProtectA;
	// 资源保护：石油
	protected final int resProtectB;
	// 资源保护：钢材
	protected final int resProtectC;
	// 资源保护：合金
	protected final int resProtectD;
	protected final String trainSoldier;
	// 英雄大厦每分钟涨经验值
	protected final int expPerMinute;
	// 大本不同等级开放不同个的行军上限
	protected final String buildEffect;
	// 改建时间
	protected final int rebuildTime;
	// 改建所需资源
	protected final String rebuildRes;
	// 取消改建返还资源
	protected final String rebuildCancelReclaim;

	private List<ItemInfo> rebuildResList;

	private List<ItemInfo> rebuildCancelReclaimList;

	// 建造时爱因斯坦时光机器
	protected final int frontRechargeDiamond;
	// 城防值
	protected final int cityDefence;
	// 陷阱容量
	protected final int trapCapacity;
	
	protected final int dismantleTime;
	
	protected final int exp;
	
	protected final int heroSpace;
	
	// 资源田增产道具ID
	protected final int increaseProItem;
	
	// 超时空急救站容纳士兵的数量
	protected final int deadLimit;
	
	// 城墙着火，普通燃烧速度
	protected final int wallFireSpeed;
	// 城墙着火，黑土地燃烧速度
	protected final int wallFireSpeedOnBlackLand;

	// 地图高
	protected int height;
	// 建筑升级消耗
	protected List<ItemInfo> costItems;
	// 建筑升级取消返还
	protected List<ItemInfo> cancelReclaimItems;
	// 前置建筑
	protected int[] frontBuildIds = new int[0];
	// 解锁的兵种
	protected final String unlockSoldier;
	// PVE出征上限
	protected final int battlePopulation;
	// 解锁的PVE关卡
	protected final String unlockChapter;
	// 英雄训练速度
	protected final int heroTrainSpeed;
	// 英雄医疗速度
	protected final int heroTreatmentSpeed;
	// 最大兵力上限
	protected final long maxPopulation;
	// 防御建筑对应兵种id
	protected final int battleSoldierId;
	
	protected final String frontCondition;
	
	protected List<Integer> unlockedSoldierIds;
	
	private Map<Integer, Integer> frontConditionParamMap;

	// 建筑作用号
	protected Map<Integer, Integer> buildEffectMap = new HashMap<Integer, Integer>();
	
	private static Map<Integer, Integer> typeMinIds = new HashMap<Integer, Integer>();
	
	// 资源田增产道具ID
	private static Map<Integer, Integer> increaseProItemMap = new HashMap<Integer, Integer>();

	public BuildingCfg() {
		id = 0;
		level = 0;
		progress = 0;
		honor = 0;
		maxNum = 0;
		frontBuild = "";
		frontStage = 0;
		postStage = 0;
		buildCost = "";
		buildTime = 0;
		buildAward = "";
		width = 0;
		height = 0;
		length = 0;
		buildType = 0;
		battlePoint = 0;
		electricGenerate = 0;
		electricConsume = 0;
		resPerHour = 0;
		resLimit = 0;
		resProtect = "";
		resProtectPlus = "";
		trainQuantity = 0;
		trainSpeed = 0;
		assistTime = 0;
		assistLimit = 0;
		assistUnitLimit = 0;
		marketBurden = 0;
		marketTax = 0;
		buildupLimit = 0;
		attackUnitLimit = 0;
		woundedLimit = 0;
		queueBuildType = 0;
		limitType = 0;
		costItems = null;
		resProtectA = 0;
		resProtectB = 0;
		resProtectC = 0;
		resProtectD = 0;
		cancelReclaim = null;
		trainSoldier = null;
		rebuildTime = 0;
		rebuildRes = "";
		rebuildCancelReclaim = "";
		frontRechargeDiamond = 0;
		unlockSoldier = "";
		battlePopulation = 0;
		unlockChapter = "";
		heroTrainSpeed = 0;
		heroTreatmentSpeed = 0;
		expPerMinute = 0;
		buildEffect = "";
		maxPopulation = 0;
		cityDefence = 0;
		trapCapacity = 0;
		dismantleTime = 0;
		exp = 0;
		heroSpace = 0;
		battleSoldierId = 0;
		deadLimit = 0;
		increaseProItem = 0;
		wallFireSpeed = 0;
		wallFireSpeedOnBlackLand = 0;
		frontCondition = "";
	}
	
	/**
     * 该建筑是否为资源建筑
     * @return
     */
    public boolean isResBuilding() {
        return isResBuildingType(buildType);
    }
    
    /**
     * 判断是否是造兵建筑
     * @return
     */
    public boolean isSoldierProductBuilding() {
    	 return buildType == BuildingType.BARRACKS_VALUE
    			  || buildType == BuildingType.WAR_FACTORY_VALUE
                  || buildType == BuildingType.REMOTE_FIRE_FACTORY_VALUE
                  || buildType == BuildingType.AIR_FORCE_COMMAND_VALUE;
    }

    /**
     * 取得所有的资源建筑限制类型
     * @return
     */
    public static LimitType[] resBuildingLimitTypes() {
        return new LimitType[] { LimitType.LIMIT_TYPE_BUIDING_ORE_REFINING, LimitType.LIMIT_TYPE_BUIDING_OIL_WELL, LimitType.LIMIT_TYPE_BUIDING_RARE_EARTH, LimitType.LIMIT_TYPE_BUIDING_STEEL };
    }

	public int getExpPerMinute() {
		return expPerMinute;
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}
	
	public int getProgress() {
		return progress;
	}
	
	public int getHonor() {
		return honor;
	}

	public int getMaxNum() {
		return maxNum;
	}

	public String getFrontBuild() {
		return frontBuild;
	}

	public int getFrontStage() {
		return frontStage;
	}

	public int getPostStage() {
		return postStage;
	}

	public String getBuildCost() {
		return buildCost;
	}

	public int getBuildTime() {
		return buildTime;
	}

	public String getBuildAward() {
		return buildAward;
	}

	public int getWidth() {
		return width;
	}

	public int getBuildType() {
		return buildType;
	}

	public int getHeight() {
		return height;
	}

	public int getLength() {
		return length;
	}

	public int getBattlePoint() {
		return battlePoint;
	}

	public long getPowerGenerate() {
		return electricGenerate;
	}

	public long getPowerConsume() {
		return electricConsume;
	}

	public int getResPerHour() {
		return resPerHour;
	}

	public long getResLimit() {
		return resLimit;
	}

	public String getResProtect() {
		return resProtect;
	}

	public int getTrainQuantity() {
		return trainQuantity;
	}

	public int getTrainSpeed() {
		return trainSpeed;
	}

	public int getAssistTime() {
		return assistTime;
	}

	public int getAssistLimit() {
		return assistLimit;
	}

	public int getAssistUnitLimit() {
		return assistUnitLimit;
	}

	public int getMarketBurden() {
		return marketBurden;
	}

	public int getMarketTax() {
		return marketTax;
	}

	public int getBuildupLimit() {
		return buildupLimit;
	}

	public int getAttackUnitLimit() {
		return attackUnitLimit;
	}

	public int getWoundedLimit() {
		return woundedLimit;
	}
	
	public int getDismantleTime() {
		return dismantleTime;
	}
	
	public int getExp() {
		return exp;
	}
	
	public int getDeadLimit() {
		return deadLimit;
	}
	
	public int getIncreaseProItem() {
		return increaseProItem;
	}
	
    public int getWallFireSpeed() {
		return wallFireSpeed;
	}

	public int getWallFireSpeedOnBlackLand() {
		return wallFireSpeedOnBlackLand;
	}
	
	/**
	 * 获取增产道具ID
	 * @param buildingType
	 * @return
	 */
	public static int getIncreaseProItem(int buildingType) {
		if (increaseProItemMap.containsKey(buildingType)) {
			return increaseProItemMap.get(buildingType);
		}
		
		if (buildingType == BuildingType.ORE_REFINING_PLANT_VALUE) {
			return 830031;
		}
		
		if (buildingType == BuildingType.OIL_WELL_VALUE) {
			return 830032;
		}
		
		if (buildingType == BuildingType.STEEL_PLANT_VALUE) {
			return 830034;
		}
		
		if (buildingType == BuildingType.RARE_EARTH_SMELTER_VALUE) {
			return 830033;
		}
		
		return 0;
	}

	public List<ItemInfo> getCostItems() {
		//不暴露原始数据的引用.有可能被外部修改
		if(costItems != null) {
			return costItems.stream().map(ItemInfo::clone).collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}

	public List<ItemInfo> getCancelReclaimItems() {
		if(cancelReclaimItems != null) {
			return cancelReclaimItems.stream().map(ItemInfo::clone).collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}

	public int[] getFrontBuildIds() {
		return frontBuildIds;
	}

	public int getLimitType() {
		return limitType;
	}

	public int getRebuildTime() {
		return rebuildTime;
	}

	public String getRebuildRes() {
		return rebuildRes;
	}

	public String getRebuildCancelReclaim() {
		return rebuildCancelReclaim;
	}

	public int getFrontRechargeDiamond() {
		return frontRechargeDiamond;
	}

	public List<ItemInfo> getRebuildResList() {
		if (rebuildResList != null) {
			return rebuildResList.stream().map(e -> e.clone()).collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}

	public List<ItemInfo> getRebuildCancelReclaimList() {
		if (rebuildCancelReclaimList != null) {
			return rebuildCancelReclaimList.stream().map(e -> e.clone()).collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}

	/**
	 * 资源保护：黄金
	 * 
	 * @return
	 */
	public int getResProtectA() {
		return resProtectA;
	}

	/**
	 * 资源保护:石油
	 * 
	 * @return
	 */
	public int getResProtectB() {
		return resProtectB;
	}

	/**
	 * 资源保护：钢材
	 * 
	 * @return
	 */
	public int getResProtectC() {
		return resProtectC;
	}

	/**
	 * 资源保护：合金
	 * 
	 * @return
	 */
	public int getResProtectD() {
		return resProtectD;
	}

	/**
	 * 根据资源类型取保护值
	 * 
	 * @return
	 */
	public int getResProtectByType(int type) {
		int protect = 0;
		switch (type) {
		case Const.PlayerAttr.GOLDORE_UNSAFE_VALUE:
			protect = resProtectA;
			break;
		case Const.PlayerAttr.OIL_UNSAFE_VALUE:
			protect = resProtectB;
			break;
		case Const.PlayerAttr.STEEL_UNSAFE_VALUE:
			protect = resProtectC;
			break;
		case Const.PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			protect = resProtectD;
			break;
		default:
			break;
		}
		return protect;
	}

	public long getElectricGenerate() {
		return electricGenerate;
	}

	public long getElectricConsume() {
		return electricConsume;
	}

	public int getQueueBuildType() {
		return queueBuildType;
	}

	public String getCancelReclaim() {
		return cancelReclaim;
	}

	public List<Integer> getUnlockedSoldierIds() {
		return Collections.unmodifiableList(unlockedSoldierIds);
	}

	public int getBattlePopulation() {
		return battlePopulation;
	}

	public int getHeroTrainSpeed() {
		return heroTrainSpeed;
	}

	public int getHeroTreatmentSpeed() {
		return heroTreatmentSpeed;
	}

	public String getBuildEffect() {
		return buildEffect;
	}
	
	public long getMaxPopulation() {
		return maxPopulation;
	}
	
	public int getCityDefence() {
		return cityDefence;
	}

	public int getTrapCapacity() {
		return trapCapacity;
	}

	@Override
	protected boolean assemble() {
		// 地图高
		if (length != 0) {
			height = length;
		}
		
		if(level == 1) {
			BuildingType buildingType = BuildingType.valueOf(buildType);
			if (buildingType == null) {
				return false;
			}
			LimitType typeLimit = LimitType.valueOf(limitType);
			if (typeLimit == null) {
				return false;
			}
			typeMinIds.put(buildType, id);
		}

		// 升级消耗
		if (buildCost != null && !"".equals(buildCost)) {
			costItems = new ArrayList<ItemInfo>();
			String[] cost = buildCost.split(",");
			for (String info : cost) {
				ItemInfo item = new ItemInfo();
				if (!item.init(info)) {
					throw new RuntimeException("Building cfg cost error " + id);
				}
				costItems.add(item);
			}
		}

		// 升级消耗
		if (cancelReclaim != null && !"".equals(cancelReclaim)) {
			cancelReclaimItems = new ArrayList<ItemInfo>();
			String[] cost = cancelReclaim.split(",");
			for (String info : cost) {
				ItemInfo item = new ItemInfo();
				if (!item.init(info)) {
					throw new RuntimeException("Building cfg cancel error " + id);
				}
				cancelReclaimItems.add(item);
			}
		}

		// 前置建筑id
		if (frontBuild != null && !"".equals(frontBuild) && !"0".equals(frontBuild)) {
			String[] ids = frontBuild.split(",");
			frontBuildIds = new int[ids.length];
			int index = 0;
			for (String frontId : ids) {
				frontBuildIds[index] = Integer.parseInt(frontId);
				index++;
			}
		}

		if (!HawkOSOperator.isEmptyString(rebuildRes)) {
			rebuildResList = new ArrayList<ItemInfo>();
			String[] resItems = rebuildRes.split(",");
			for (String item : resItems) {
				String[] itemStr = item.split("_");
				if (itemStr.length >= 3) {
					rebuildResList.add(new ItemInfo(Integer.parseInt(itemStr[0]), Integer.parseInt(itemStr[1]), Integer.parseInt(itemStr[2])));
				}
			}
		}

		if (!HawkOSOperator.isEmptyString(rebuildCancelReclaim)) {
			rebuildCancelReclaimList = new ArrayList<ItemInfo>();
			String[] resItems = rebuildCancelReclaim.split(",");
			for (String item : resItems) {
				String[] itemStr = item.split("_");
				if (itemStr.length >= 3) {
					rebuildCancelReclaimList.add(new ItemInfo(Integer.parseInt(itemStr[0]), Integer.parseInt(itemStr[1]), Integer.parseInt(itemStr[2])));
				}
			}
		}

		if (!HawkOSOperator.isEmptyString(unlockSoldier)) {
			unlockedSoldierIds = new ArrayList<Integer>();
			String[] soldierIds = unlockSoldier.split("_");
			for (String soldierId : soldierIds) {
				unlockedSoldierIds.add(Integer.parseInt(soldierId));
			}
		} else {
			unlockedSoldierIds = Collections.emptyList();//没有数据返回空的集合.而不是null
		}

		// 初始化建筑升级带来的作用号的影响
		if (!HawkOSOperator.isEmptyString(buildEffect)) {
			String[] strs = buildEffect.split(",");
			for (int i = 0; i < strs.length; i++) {
				String[] idVal = strs[i].split("_");
				if (idVal.length < 2) {
					return false;
				}
				
				buildEffectMap.put(Integer.parseInt(idVal[0]), Integer.parseInt(idVal[1]));
			}
		}
		
		if (increaseProItem > 0) {
			increaseProItemMap.put(buildType, increaseProItem);
		}
		
		if (!HawkOSOperator.isEmptyString(frontCondition)) {
			String[] params = frontCondition.split(",");
			frontConditionParamMap = new HashMap<Integer, Integer>(params.length);
			for (String param : params) {
				String[] typeParam = param.split("_");
				int frontConditionType = Integer.parseInt(typeParam[0]);
				int frontConditionParam = Integer.parseInt(typeParam[1]);
				frontConditionParamMap.put(frontConditionType, frontConditionParam);
			}
		}

		return true;
	}

	public Map<Integer, Integer> getBuildEffectMap() {
		return Collections.unmodifiableMap(buildEffectMap);
	}
	
	public int getBattleSoldierId() {
		return battleSoldierId;
	}
	
	public static int getBuildTypeMinId(int type) {
		if(!typeMinIds.containsKey(type)) {
			return 0;
		}
		return typeMinIds.get(type);
	}

	@Override
	protected boolean checkValid() {
		// 前置建筑id
		if (frontBuildIds != null) {
			for (int frontId : frontBuildIds) {
				BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, frontId);
				if (cfg == null) {
					HawkLog.errPrintln("buildingCfg check valid failed, front buildCfgId: {}, buildingCfg: {}", frontId, cfg);
					return false;
				}
			}
		}
		if(frontStage != 0){
			BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, frontStage);
			if (cfg == null) {
				HawkLog.errPrintln("buildingCfg check valid failed, id: {}, frontStage: {}", id, frontStage);
				return false;
			}
		}
		if(postStage != 0){
			BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, postStage);
			if (cfg == null) {
				HawkLog.errPrintln("buildingCfg check valid failed, id: {}, postStage: {}", id, postStage);
				return false;
			}
		}

		return true;
	}
	
	public static boolean isResBuildingType(int buildingType) {
		return buildingType == BuildingType.ORE_REFINING_PLANT_VALUE
                || buildingType == BuildingType.OIL_WELL_VALUE
                || buildingType == BuildingType.STEEL_PLANT_VALUE
                || buildingType == BuildingType.RARE_EARTH_SMELTER_VALUE;
	}
	
	/**
	 * 获取建筑移除的大类型：资源建筑-resBuilding，复制中心和医院-soldierBuilding，其它普通建筑-commonBuilding
	 * @param buildingType
	 * @return
	 */
	public static String getBuildingRemoveType(int buildingType) {
		if (isResBuildingType(buildingType)) {
			return "resBuilding";
		}
		
		if (BuildAreaCfg.isShareBlockBuildType(buildingType)) {
			return "soldierBuilding";
		} 
		
		return "commonBuilding";
	}

	public Map<Integer, Integer> getFrontConditionParamMap() {
		return frontConditionParamMap;
	}
	
	
	
	
	public int[] getFrontBuildIdsWithout(List<Integer> withoutList) {
		List<Integer> rList = new ArrayList<>();
		for(int fb : frontBuildIds){
			if(withoutList.contains(fb)){
				continue;
			}
			rList.add(fb);
		}
		int[] rarr = rList.stream().mapToInt(Integer::intValue).toArray();
		return rarr;
	}
	
}
