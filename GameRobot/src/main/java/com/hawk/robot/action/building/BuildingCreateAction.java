package com.hawk.robot.action.building;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.hawk.annotation.RobotAction;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;
import com.hawk.game.protocol.Building.BuildingCreateReq;
import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.LimitType;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.config.BuildAreaCfg;
import com.hawk.robot.config.BuildingCfg;
import com.hawk.robot.util.ClientUtil;

/**
 * 
 * 新建建筑
 * 
 * @author lating
 *
 */
@RobotAction(valid = true)
public class BuildingCreateAction extends HawkRobotAction {
	/**
	 * 需要优先建造的建筑
	 */
	private static final List<Integer> DETAULT_BUILDING_LIMIT = Arrays.asList(LimitType.LIMIT_TYPE_EMBASSY_VALUE, LimitType.LIMIT_TYPE_SOILDER_VALUE,
			LimitType.LIMIT_TYPE_WAR_FACTORY_VALUE, LimitType.LIMIT_TYPE_AIR_FORCE_COMMAND_VALUE, LimitType.LIMIT_TYPE_REMOTE_FIRE_FACTORY_VALUE,
			LimitType.LIMIT_TYPE_FIGHTING_LABORATORY_VALUE, LimitType.LIMIT_TYPE_SATELLITE_COMMUNICATIONS_VALUE, LimitType.LIMIT_TYPE_WISHING_WELL_VALUE);
	
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
		doBuildingCreateAction(gameRobotEntity);
	}
	
	public static synchronized boolean doBuildingCreateAction(GameRobotEntity robot) {
		if (!ClientUtil.isExecuteAllowed(robot, BuildingCreateAction.class.getSimpleName(), 60000)) {
			return false;
		}
		//插入一个建造黑市商人的代码 我也不知道怎么读取开启条件，先写死吧.
		List<BuildingPB> buildingList = robot.getCityData().getBuildingByType(BuildingType.CONSTRUCTION_FACTORY_VALUE);
		boolean needCreateTravelShop = false;
		BuildingCfg cfg = null;
		for (BuildingPB building : buildingList) {
			cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildCfgId());
			if (cfg.getLevel() >= 6) {
				needCreateTravelShop = true;
			}
		}
		
		if (needCreateTravelShop) {
			buildingList = robot.getCityData().getBuildingByType(BuildingType.ARMS_DEALER_VALUE);
			cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, 202101);
			if (buildingList.isEmpty()) {
				sendProtocol(robot, cfg, true);
			}
		}
		
		Map<Integer, Integer> unlockBuildingMap = robot.getCityData().getUnLockBuildingMap();
		if(unlockBuildingMap.size() <= 0) {
			RobotLog.cityErrPrintln("building create failed, has no unlock building, playerId: {}", robot.getPlayerId());
			return false;
		}
		
		List<Integer> needCreatedBuilds = robot.getCityData().getUnlockedNeedCreateBuild();
		BuildingCfg buildingCfg = selectBuilding(robot, unlockBuildingMap, needCreatedBuilds);
		if(buildingCfg == null) {
			RobotLog.cityErrPrintln("building create failed, select building null, playerId: {}", robot.getPlayerId());
			return false;
		}
		
		boolean immediate = DETAULT_BUILDING_LIMIT.contains(buildingCfg.getLimitType()) || needCreatedBuilds.contains(buildingCfg.getId()) ? true : HawkRand.randPercentRate(50);
		if(!immediate && !robot.getBasicData().hasFreeBuildingQueue(buildingCfg)) {
			RobotLog.cityErrPrintln("building create failed, has no free building queue, playerId: {}", robot.getPlayerId());
			return false;
		}
		
		 return sendProtocol(robot, buildingCfg, immediate);
	}
	
	/**
	 * 发送建筑建造协议
	 * @param robot
	 * @param buildingCfg
	 * @param immediate
	 */
	private static boolean sendProtocol(GameRobotEntity robot, BuildingCfg buildingCfg, boolean immediate) {
		int index = getBuildingIndex(robot, buildingCfg);
		if (index < 0) {
			RobotLog.cityErrPrintln("building create failed, building index error, playerId: {}", robot.getPlayerId());
			return false;
		}
		
		BuildingCreateReq.Builder builder = BuildingCreateReq.newBuilder();
		builder.setBuildCfgId(buildingCfg.getId());
		builder.setIndex(String.valueOf(index));
		builder.setImmediately(immediate);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.BUILDING_CREATE_C_VALUE, builder));
		robot.getCityData().getLastExecuteTime().put(BuildingCreateAction.class.getSimpleName(), HawkTime.getMillisecond());
		RobotLog.cityDebugPrintln("building create action, playerId: {}, buildingId: {}, index: {}, immediate: {}", 
				robot.getPlayerId(), buildingCfg.getId(), index, immediate);
		
		return true;
	}
	
	/**
	 * 筛选建筑
	 * @param robot
	 * @param unlockBuildingMap
	 * @return
	 */
	private static BuildingCfg selectBuilding(GameRobotEntity robot, Map<Integer, Integer> unlockBuildingMap, List<Integer> needCreatedBuilds) {
		BuildingCfg buildingCfg = null;
		for (int limitType : DETAULT_BUILDING_LIMIT) {
			if (!unlockBuildingMap.containsKey(limitType)) {
				continue;
			}
			
			buildingCfg = getBuildingCfg(robot, limitType, 1);
			if (buildingCfg != null) {
				break;
			}
		}
		
		if (buildingCfg != null) {
			return buildingCfg;
		}
		
		List<Integer> limitTypes = new ArrayList<>();
		for (int buildCfgId : needCreatedBuilds) {
			BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildCfgId);
			if (!unlockBuildingMap.containsKey(cfg.getLimitType())) {
				continue;
			}
			
			limitTypes.add(cfg.getLimitType());
			buildingCfg = getBuildingCfg(robot, cfg.getLimitType(), unlockBuildingMap.get(cfg.getLimitType()));
			if (buildingCfg != null) {
				break;
			}
		}
		
		if (buildingCfg != null) {
			return buildingCfg;
		}
		
		for(Entry<Integer, Integer> entry : unlockBuildingMap.entrySet()) {
			if (DETAULT_BUILDING_LIMIT.contains(entry.getKey()) || limitTypes.contains(entry.getKey())) {
				continue;
			}
			
			buildingCfg = getBuildingCfg(robot, entry.getKey(), entry.getValue());
			if (buildingCfg != null) {
				break;
			}
		}
		
		return buildingCfg;
	}
	
	/**
	 * 获取建筑配置
	 * @param robot
	 * @param type
	 * @param limitCount
	 * @return
	 */
	private static BuildingCfg getBuildingCfg(GameRobotEntity robot, int type, int limitCount) {
		LimitType limitType = LimitType.valueOf(type);
		if(limitType == null) {
			return null;
		}
		
		List<BuildingPB> buildings = robot.getBuildingListByLimitType(limitType);
		if (buildings.size() >= limitCount) {
			return null;
		}
		
		int buildType = BuildingCfg.getBuildTypeByLimitType(type);
		int buildCfgId = BuildingCfg.getBuildMinIdByType(buildType);
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildCfgId);
		if(buildingCfg == null || !BuildingUpgradeAction.checkFrontBuilding(robot, buildingCfg.getFrontBuildIds(), null)) {
			return null;
		}
		
		return buildingCfg;
	}
	
	/**
	 * 生成建筑地标
	 * @param robot
	 * @param buildingCfg
	 * @return
	 */
	private static int getBuildingIndex(GameRobotEntity robot, BuildingCfg buildingCfg) {
		if (!BuildAreaCfg.isSharaBlockBuildType(buildingCfg.getBuildType())) {
			int count = robot.getBuildingByType(buildingCfg.getBuildType()).size();
			return count + 1;
		}
		
		int index = -1;

		// 已使用的地块
		List<String> usedBlockIds = robot.getCityData().getUsedSharedBlocks();
		// 遍历已解锁的区域
		for (int areaId : robot.getCityData().getUnlockedAreas()) {
			BuildAreaCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildAreaCfg.class, areaId);
			Optional<Integer> op = cfg.getBlockList().stream().filter(e -> !usedBlockIds.contains(String.valueOf(e))).findAny();
			if (!op.isPresent()) {
				continue;
			}
			
			index = op.get();
			break;
		}
		
		return index;
	}
}
