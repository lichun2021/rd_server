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
import com.hawk.game.protocol.Const.BuildingType;

/**
 * 建筑区域配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/build_area.xml")
public class BuildAreaCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 区域解锁对应的大本等级
	protected final int unlockrankLevel;
	// 章节任务解锁条件
	protected final int unlockDramaLevel;
	
	// 区域对应的地块
	protected final String blocks;
	// 区域对应可放置的类型建筑
	protected final String buildType;
	// 是否需要前端点击解锁
	protected final int click;
	// 是否允许解锁
	protected final int difference;
	//解锁战力玩家总战力
	protected final int unlockPower;
	//解锁兵数量
	protected final int unlockSoldierNum;
	//解锁区域
	protected final int unlockPreArea;
	//解锁防线
	protected final int defenseLine;
	// 解锁地块奖励
	protected final String ItemDrop;
	
	private List<Integer> blockList;
	
	private List<Integer> buildTypeList;
	
	private List<ItemInfo> unlockAwardItems;
	
	private static Map<Integer, List<Integer>> rankUnlockAreaMap = new HashMap<>();
	private static Map<Integer, Integer> blockAreaMap = new HashMap<>();
	private static Set<Integer> shareBlockBuildTypes = new HashSet<>();
	
	public BuildAreaCfg() {
		id = 0;
		unlockrankLevel = 1;
		unlockDramaLevel = 0;
		blocks = "";
		buildType = "";
		click = 0;
		difference = 0;
		unlockPower = 0;
		unlockSoldierNum = 0;
		unlockPreArea = 0;
		defenseLine = 0;
		ItemDrop = "";
	}
	
	public int getId() {
		return id;
	}

	public int getUnlockCityLevel() {
		return unlockrankLevel;
	}
	
	public int getUnlockDramaLevel() {
		return unlockDramaLevel;
	}

	public String getBlocks() {
		return blocks;
	}

	public String getBuildType() {
		return buildType;
	}
	
	// 是否需要点击解锁
	public boolean isNeedClick() {
		return click > 0;
	}
	
	// 是否允许解锁
	public boolean isAllowedUnlock() {
		return difference <= 0;
	}

	public List<Integer> getBlockList() {
		if (blockList != null) {
			return Collections.unmodifiableList(blockList);
		}
		
		return Collections.emptyList();
	}

	public List<Integer> getBuildTypeList() {
		if (buildTypeList != null) {
			return Collections.unmodifiableList(buildTypeList);
		}
		
		return Collections.emptyList();
	}
	
	public List<ItemInfo> getUnlockAwardItems() {
		return unlockAwardItems.stream().map(e -> e.clone()).collect(Collectors.toList());
	}

	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(blocks)) {
			blockList = new ArrayList<>();
			for (String block : blocks.split(",")) {
				int blockId = Integer.valueOf(block);
				blockList.add(blockId);
				blockAreaMap.put(blockId, id);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(buildType)) {
			buildTypeList = new ArrayList<>();
			for (String type : buildType.split(",")) {
				int buildingType = Integer.valueOf(type);
				if (BuildingType.valueOf(buildingType) == null) {
					throw new RuntimeException("building type not exist");
				}
				buildTypeList.add(buildingType);
				shareBlockBuildTypes.add(buildingType);
			}
		}
		
		List<Integer> areaIdList = rankUnlockAreaMap.get(unlockrankLevel);
		if (areaIdList == null) {
			areaIdList = new ArrayList<>();
			rankUnlockAreaMap.put(unlockrankLevel, areaIdList);
		}
		
		areaIdList.add(id);
		
		unlockAwardItems = ItemInfo.valueListOf(ItemDrop);
		
		return true;
	}
	
	/**
	 * 获取已解锁的区块列表
	 * @param cityLevel
	 * @return
	 */
	public static List<Integer> getUnlockedArea(int rank) {
		List<Integer> areaList = new ArrayList<>();
		for (int level = 1; level <= rank; level++) {
			if (rankUnlockAreaMap.containsKey(level)) {
				areaList.addAll(rankUnlockAreaMap.get(level));
			}
		}
		
		return Collections.unmodifiableList(areaList);
	}
	
	/**
	 * 通过建筑地块Id获取区块
	 * @param blockId
	 * @return
	 */
	public static int getAreaByBlock(int blockId) {
		if (!blockAreaMap.containsKey(blockId)) {
			return 0;
		}
		
		return blockAreaMap.get(blockId);
	}
	
	/**
	 * 判断一种建筑类型是否是共享地块建筑类型
	 * @param buildType
	 * @return
	 */
	public static boolean isShareBlockBuildType(int buildType) {
		return shareBlockBuildTypes.contains(buildType);
	}

	public int getUnlockPower() {
		return unlockPower;
	}

	public int getUnlockSoldierNum() {
		return unlockSoldierNum;
	}

	public int getUnlockPreArea() {
		return unlockPreArea;
	}

	public int getDefenseLine() {
		return defenseLine;
	}
}
