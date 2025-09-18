package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.util.GsConst.MissionFunType;

@HawkConfigManager.XmlResource(file = "xml/mission.xml")
public class MissionCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 任务类型
	 */
	private final int taskType;
	/**
	 * 玩家等级
	 */
	private final int level;
	/**
	 * 建筑ID
	 */
	private final int buildingClass;
	/**
	 * 前置任务id
	 */
	private final String beforeIds;
	/**
	 * 任务刷出截止大本等级
	 */
	private final int castleClass;
	/**
	 * 功能类型
	 */
	private final int funType;
	/**
	 * 功能id
	 */
	private final int funId;
	/**
	 * 功能值
	 */
	private final int funVal;
	/**
	 * 1为默认开启任务
	 */
	private final int defaultVal;
	/**
	 * 奖励列表
	 */
	private final String reward;
	/**
	 * 推荐任务
	 */
	private final int order1;
	/**
	 * 普通任务
	 */
	private final int order2;
	/**
	 * 任务刷出时是否取历史累加数据
	 */
	private final int add;
	/**
	 * 任务奖励列表
	 */
	private List<ItemInfo> rewardItemInfo;
	
	private List<Integer> preMissions;
	/**
	 * 前置任务条件，任务
	 */
	private static Map<Integer, List<Integer>> preMissionConditions = new HashMap<>();
	/**
	 * 前置玩家等级条件，任务
	 */
	private static Map<Integer, List<Integer>> playerLevelConditions = new HashMap<>();
	/**
	 * 前置建筑等级条件，任务
	 */
	private static Map<Integer, List<Integer>> buildIdConditions = new HashMap<>();
	
	/**
	 * 初始化任务
	 */
	private static List<Integer> initMissions = new ArrayList<>();
	
	private static Set<Integer> missionTypeSet = new HashSet<>();
	private static Set<Integer> accumulatedTypeSet = new HashSet<>();
	
	public MissionCfg() {
		id = 0;
		taskType = 0;
		level = 0;
		buildingClass = 0;
		beforeIds = "";
		castleClass = Integer.MAX_VALUE;
		funType = 0;
		funId = 0;
		funVal = 0;
		defaultVal = 0;
		reward = "";
		order1 = 0;
		order2 = 0;
		add = 0;
	}

	public int getId() {
		return id;
	}

	public int getTaskType() {
		return taskType;
	}

	public int getFunType() {
		return funType;
	}

	public int getFunId() {
		int missionFunId = funId;
		if(funType == MissionFunType.FUN_BUILD_LEVEL.intValue()) {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, missionFunId);
			if(buildingCfg != null) {
				missionFunId = buildingCfg.getBuildType();
			}
		}
		return missionFunId;
	}

	public int getFunVal() {
		return funVal;
	}
	
	public int getLevel() {
		return level;
	}

	public int getBuildingClass() {
		return buildingClass;
	}

	public String getBeforeIds() {
		return beforeIds;
	}

	public int getCastleClass() {
		return castleClass;
	}

	public int getDefaultVal() {
		return defaultVal;
	}

	public String getReward() {
		return reward;
	}
	
	public int getAdd() {
		return add;
	}
	
	public boolean isOverlayMission() {
		return add == 1;
	}

	public List<ItemInfo> getRewardItemInfo() {
		return rewardItemInfo.stream().map(e -> e.clone()).collect(Collectors.toList());
	}

	public int getOrder1() {
		return order1;
	}

	public int getOrder2() {
		return order2;
	}
	
	public int getOrder() {
		return order1 > 0 ? order1 : order2;
	}

	@Override
	protected boolean assemble() {
		missionTypeSet.add(funType);
		if (add == 1) {
			accumulatedTypeSet.add(funType);
		}
		
		if (HawkOSOperator.isEmptyString(reward)) {
			return false;
		}
		rewardItemInfo = ItemInfo.valueListOf(reward);
		
		if(defaultVal == 1) {
			initMissions.add(id);
		} else if (!HawkOSOperator.isEmptyString(beforeIds)) {
			String[] preIds = beforeIds.split(",");
			if (preIds.length > 1) {
				preMissions = new ArrayList<Integer>();
			}
			
 			for(String preIdStr : preIds) {
				int preId = Integer.valueOf(preIdStr);
				List<Integer> afterIds = preMissionConditions.get(preId);
				if(afterIds == null) {
					afterIds = new ArrayList<>();
					preMissionConditions.put(preId, afterIds);
				}
				afterIds.add(id);
				
				if (preMissions != null) {
					preMissions.add(preId);
				}
			}
 			
		} else {
			if(buildingClass > 0) {
				List<Integer> afterIds = buildIdConditions.get(buildingClass);
				if(afterIds == null) {
					afterIds = new ArrayList<>();
					buildIdConditions.put(buildingClass,afterIds);
				}
				afterIds.add(id);
			} else if (level > 0) {
				List<Integer> afterIds = playerLevelConditions.get(level);
				if(afterIds == null) {
					afterIds = new ArrayList<>();
					playerLevelConditions.put(level,afterIds);
				}
				afterIds.add(id);
			}
		}

		return true;
	}

	@Override
	protected boolean checkValid() {
		if (rewardItemInfo != null) {
			for (ItemInfo itemInfo : rewardItemInfo) {
				if (!itemInfo.checkItemInfo()) {
					return false;
				}
			}
		}

		if (MissionFunType.valueOf(funType) == null) {
			return false;
		}

		return true;
	}
	
	public int getMissionTypeId() {
		int missionFunId = getFunId();
		return getTypeId(MissionFunType.valueOf(this.funType), missionFunId);
	}
	
	public static int getTypeId(MissionFunType funType, int funId) {
		return (funType.intValue() << 20) | funId;
	}

	public static List<Integer> getAfterMissionsByPreMission(int preMissionId) {
		if(preMissionConditions.containsKey(preMissionId)) {
			return Collections.unmodifiableList(preMissionConditions.get(preMissionId));
		}
		
		return Collections.emptyList();
	}
	
	public static List<Integer> getAfterMissionsByPlayerLv(int playerLevel) {
		if(playerLevelConditions.containsKey(playerLevel)) {
			return Collections.unmodifiableList(playerLevelConditions.get(playerLevel));
		}
		
		return Collections.emptyList();
	}
	
	public static List<Integer> getAfterMissionsByBuildId(int buildId) {
		if(buildIdConditions.containsKey(buildId)) {
			return Collections.unmodifiableList(buildIdConditions.get(buildId));
		}
		
		return Collections.emptyList();
	}

	public static List<Integer> getInitMissions() {
		return Collections.unmodifiableList(initMissions);
	}
	
	public static Set<Integer> getMissionTypes() {
		return Collections.unmodifiableSet(missionTypeSet);
	}
	
	public static Set<Integer> getAccumulatedTypes() {
		return Collections.unmodifiableSet(accumulatedTypeSet);
	}

	public List<Integer> getPreMissions() {
		if (preMissions != null) {
			return Collections.unmodifiableList(preMissions);
		}
		
		return Collections.emptyList();
	}

}
