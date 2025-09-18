package com.hawk.robot.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;

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

	// 可以建造的最大数量
	protected final int maxNum;

	// 前置条件,必须创建前面的建筑
	protected final String frontBuild;
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
	// 作用号值
	private final String effectID;
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
	
	protected int[] frontBuildIds = new int[0];
	
	private static Map<Integer, Integer> buildTypeMinIdMap = new HashMap<>();
	private static Map<Integer, Integer> buildTypeMaxLevelMap = new HashMap<>();
	private static Map<Integer, Integer> limitBuildTypeMap = new HashMap<>();
	private static List<Integer> rebuildIds = new ArrayList<>();
	protected List<Integer> unlockedSoldierIds;

	public BuildingCfg() {
		id = 0;
		level = 0;
		maxNum = 0;
		frontBuild = "";
		buildCost = "";
		buildTime = 0;
		buildAward = "";
		width = 0;
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
		resProtectA = 0;
		resProtectB = 0;
		resProtectC = 0;
		resProtectD = 0;
		cancelReclaim = null;
		trainSoldier = null;
		effectID = "";
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
	}
	
    /**
     * 是否为资源建筑
     * @return
     */
    public boolean isResourceBuilding() {
        return buildType == BuildingType.OIL_WELL_VALUE
                || buildType == BuildingType.ORE_REFINING_PLANT_VALUE
                || buildType == BuildingType.RARE_EARTH_SMELTER_VALUE
                || buildType == BuildingType.STEEL_PLANT_VALUE;
    }
    
    /**
     * 是否为造兵建筑
     * @return
     */
    public boolean isArmyProduceBuilding() {
    	return buildType == BuildingType.BARRACKS_VALUE 
    			|| buildType == BuildingType.WAR_FACTORY_VALUE 
    			|| buildType == BuildingType.REMOTE_FIRE_FACTORY_VALUE
    			|| buildType == BuildingType.AIR_FORCE_COMMAND_VALUE;
    }

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public int getMaxNum() {
		return maxNum;
	}

	public String getFrontBuild() {
		return frontBuild;
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

	public String getEffectID() {
		return effectID;
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
	
	public int getExpPerMinute() {
		return expPerMinute;
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
	
	public List<Integer> getUnlockedSoldierIds() {
		return unlockedSoldierIds;
	}

	/**
	 * 根据资源类型取保护值
	 * 
	 * @return
	 */
	public int getResProtectByType(int type) {
		int protect = 0;
		switch (type) {
		case Const.PlayerAttr.GOLDORE_VALUE:
			protect = resProtectA;
			break;
		case Const.PlayerAttr.OIL_VALUE:
			protect = resProtectB;
			break;
		case Const.PlayerAttr.STEEL_VALUE:
			protect = resProtectC;
			break;
		case Const.PlayerAttr.TOMBARTHITE_VALUE:
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
	
	public int[] getFrontBuildIds() {
		return frontBuildIds;
	}

	@Override
	protected boolean assemble() {
		
		limitBuildTypeMap.put(limitType, buildType);
		
		if(level == 1) {
			buildTypeMinIdMap.put(buildType, id);
		}
		
		if(!buildTypeMaxLevelMap.containsKey(buildType) || level > buildTypeMaxLevelMap.get(buildType)) {
			buildTypeMaxLevelMap.put(buildType, level);
		}
		
		if (frontBuild != null && !"".equals(frontBuild) && !"0".equals(frontBuild)) {
			String[] ids = frontBuild.split(",");
			frontBuildIds = new int[ids.length];
			int index = 0;
			for (String frontId : ids) {
				frontBuildIds[index] = Integer.parseInt(frontId);
				index++;
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
		
		return true;
	}

	@Override
	protected boolean checkValid() {
		return true;
	}
	
	/**
	 * 根据限制类型获取建筑类型
	 * @param limitType
	 * @return
	 */
	public static int getBuildTypeByLimitType(int limitType) {
		if(!limitBuildTypeMap.containsKey(limitType)) {
			return 0;
		}
		return limitBuildTypeMap.get(limitType);
	}

	public static int getBuildMinIdByType(int type) {
		if(!buildTypeMinIdMap.containsKey(type)) {
			return 0;
		}
		return buildTypeMinIdMap.get(type);
	}
	
	public static int getBuildMaxLevelByType(int type) {
		if(!buildTypeMaxLevelMap.containsKey(type)) {
			return 0;
		}
		return buildTypeMaxLevelMap.get(type);
	}

	public static List<Integer> getRebuildIds() {
		return rebuildIds;
	}

	public static boolean isResBuildingType(int buildingType) {
		return buildingType == BuildingType.ORE_REFINING_PLANT_VALUE
               || buildingType == BuildingType.OIL_WELL_VALUE
               || buildingType == BuildingType.STEEL_PLANT_VALUE
               || buildingType == BuildingType.RARE_EARTH_SMELTER_VALUE;
	}

}
