package com.hawk.game.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.game.aoi.HawkAOIObj;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple3;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.config.WorldResourceCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.lianmengxzq.worldpoint.XZQWorldPoint;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.march.MarchPart;
import com.hawk.game.player.Player;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.ResourceZone;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildWar.PushQuarteredMarchDelItem;
import com.hawk.game.protocol.GuildWar.PushQuarteredMarchType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.CityMoveType;
import com.hawk.game.protocol.World.MarchTargetPointType;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.flag.FlagCollection;
import com.hawk.game.service.warFlag.IFlag;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.superweapon.weapon.IWeapon;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.WorldScene;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPointService;

public class WorldUtil {
	/**
	 * 获得一个中心点，按照x半径，y半径辐射到的所有点
	 * 
	 * @param x
	 * @param y
	 * @param radiusX
	 * @param radiusY
	 * @return
	 */
	public static Collection<Integer> getRangePointIds(int x, int y, int radiusX, int radiusY) {
		// 城点距离限制范围内的所有点
		Set<Integer> pointIds = new HashSet<Integer>();
		for (int i = 0; i <= radiusX; i++) {
			int x1 = x + i;
			int x2 = x - i;
			for (int j = 0; j <= radiusY - i; j++) {
				int y1 = y + j;
				int y2 = y - j;
				pointIds.add(GameUtil.combineXAndY(x1, y1));
				pointIds.add(GameUtil.combineXAndY(x1, y2));
				pointIds.add(GameUtil.combineXAndY(x2, y1));
				pointIds.add(GameUtil.combineXAndY(x2, y2));
			}
		}
		return pointIds;
	}

	/**
	 * 根据点坐标获取所在资源带
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static int getPointResourceZone(int x, int y) {
		//先判断黑土地, 如果在黑土地, 直接返回黑土地
		if(WorldPointService.getInstance().isInCapitalArea(GameUtil.combineXAndY(x, y))){
			return ResourceZone.ZONE_BLACK_VALUE;
		}
		int[][] areaConfig = WorldMapConstProperty.getInstance().getWorldResAreas();
		for (int i = 0; i < areaConfig.length; i++) {
			int[] area = areaConfig[i];
			if (x >= area[0] && y >= area[1] && x < area[2] && y < area[3]) {
				return areaConfig.length - i;
			}
		}
		return 0;
	}

	/**
	 * 根据点计算区域id
	 */
	public static int getAreaId(int x, int y) {
		int worldMaxX = WorldMapConstProperty.getInstance().getWorldMaxX();
		int worldResRefreshWidth = WorldMapConstProperty.getInstance().getWorldResRefreshWidth();
		int areaCols = worldMaxX / worldResRefreshWidth;
		areaCols = worldMaxX % worldResRefreshWidth == 0 ? areaCols : areaCols + 1;

		int worldMaxY = WorldMapConstProperty.getInstance().getWorldMaxY();
		int worldResRefreshHeight = WorldMapConstProperty.getInstance().getWorldResRefreshHeight();
		int areaRows = worldMaxY / worldResRefreshHeight;
		areaRows = worldMaxY % worldResRefreshHeight == 0 ? areaRows : areaRows + 1;

		// 计算行列
		int col = x / WorldMapConstProperty.getInstance().getWorldResRefreshWidth();
		int row = y / WorldMapConstProperty.getInstance().getWorldResRefreshHeight();

		// 构建地图区块
		int areaId = row * areaCols + col + 1;
		return areaId;
	}

	/**
	 * 根据类型和等级查找对应的资源
	 * 
	 * @param resType
	 * @param resLevel
	 * @return
	 */
	public static WorldResourceCfg getResourceCfg(int resType, int resLevel) {
		ConfigIterator<WorldResourceCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(WorldResourceCfg.class);
		while (iterator.hasNext()) {
			WorldResourceCfg cfg = iterator.next();
			if (!cfg.isNewly() && cfg.getResType() == resType && cfg.getLevel() == resLevel) {
				return cfg;
			}
		}
		return null;
	}

	/**
	 * 将一个行军士兵列表一分为二
	 * 
	 * @param oriList
	 *            原列表
	 * @param stayPopulationCnt
	 *            剩下来的士兵人口
	 * @param backList
	 *            返回的士兵列表
	 * @param stayList
	 *            留下来的士兵列表
	 */
	public static void splitArmyList(List<ArmyInfo> oriList, int stayPopulationCnt, List<ArmyInfo> backList, List<ArmyInfo> stayList) {
		if (stayPopulationCnt < 0 || backList == null || stayList == null) {
			return;
		}
		backList.clear();
		stayList.clear();

		// TODO 伤兵转死病规则
		for (ArmyInfo armyInfo : oriList) {
			final int maxArmNum = stayPopulationCnt;
			if (maxArmNum <= 0) {
				backList.add(new ArmyInfo(armyInfo.getArmyId(), armyInfo.getTotalCount()));
			} else if (maxArmNum >= armyInfo.getTotalCount()) {
				stayList.add(new ArmyInfo(armyInfo.getArmyId(), armyInfo.getTotalCount()));
				stayPopulationCnt -= armyInfo.getTotalCount();
			} else {
				stayList.add(new ArmyInfo(armyInfo.getArmyId(), maxArmNum));
				backList.add(new ArmyInfo(armyInfo.getArmyId(), armyInfo.getTotalCount() - maxArmNum));
				stayPopulationCnt -= maxArmNum;
			}
		}
	}

	/**
	 * 获得部队列表对象的拷贝
	 * 
	 * @param armyList
	 * @param copyList
	 * @return
	 */
	public static List<ArmyInfo> copyArmyList(List<ArmyInfo> armyList, List<ArmyInfo> copyList) {
		if (copyList == null) {
			copyList = new ArrayList<ArmyInfo>();
		}
		if (armyList == null || armyList.size() == 0) {
			return copyList;
		}
		for (ArmyInfo armyInfo : armyList) {
			copyList.add(armyInfo.getCopy());
		}
		return copyList;
	}

	/**
	 * 计算行军列表人口
	 * 
	 * @param armyList
	 * @return
	 */
	public static int calcMarchsSoldierCnt(List<WorldMarch> marchs) {
		int totalCnt = 0;
		for (WorldMarch march : marchs) {
			if (march == null || march.isInvalid()) {
				continue;
			}
			totalCnt += calcSoldierCnt(march.getArmys());
		}
		return totalCnt;
	}

	/**
	 * 计算部队活兵人口
	 * 
	 * @param armyList
	 * @return
	 */
	public static int calcSoldierCnt(List<ArmyInfo> armyList) {
		int totalCnt = 0;
		if (armyList == null || armyList.size() == 0) {
			return totalCnt;
		}
		for (ArmyInfo armyInfo : armyList) {
			int freeCnt = armyInfo.getFreeCnt();
			if (freeCnt <= 0) {
				continue;
			}
			totalCnt += freeCnt;
		}
		return totalCnt;
	}

	/**
	 * 计算援助去/留的士兵
	 * 
	 * @param march
	 * @param remainSpace
	 *            最大人口
	 * @param stayList
	 * @param backList
	 * @return 留下来的人口
	 */
	public static int calcStayArmy(WorldMarch march, int remainSpace, List<ArmyInfo> stayList, List<ArmyInfo> backList) {
		int carryCnt = WorldUtil.calcSoldierCnt(march.getArmys());
		int stayCnt = 0;
		int backCnt = 0;

		// 超出空位
		if (carryCnt > remainSpace) {
			stayCnt = remainSpace;// 留下
			backCnt = carryCnt - remainSpace;// 回家
		} else {
			stayCnt = carryCnt;
		}

		// 计算留下来的和回家的表
		if (backCnt > 0) {
			WorldUtil.splitArmyList(march.getArmys(), stayCnt, backList, stayList);
		} else {
			WorldUtil.copyArmyList(march.getArmys(), stayList);
		}
		return stayCnt;
	}

	/**
	 * 计算军队负重（作用号加成前）
	 * 
	 * @param armyList
	 * @return
	 */
	public static int calcTotalWeight(Player atker, List<ArmyInfo> armyList, EffectParams effParams) {
		int totalWeight = 0;
		if (armyList == null || armyList.size() == 0) {
			return totalWeight;
		}
		for (ArmyInfo armyInfo : armyList) {
			int freeCnt = armyInfo.getFreeCnt();
			if (freeCnt <= 0) {
				continue;
			}
			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
			if (armyCfg == null || armyCfg.getLoad() <= 0) {
				continue;
			}
			totalWeight += armyCfg.getLoad() * freeCnt;
		}

		double effVal = 1 + GsConst.EFF_PER * atker.getEffect().getEffVal(EffType.RES_TROOP_WEIGHT, effParams);
		double PREY_RES_PER = atker.getEffect().getEffVal(EffType.PREY_RES_PER, effParams);// 掠夺资源比率增加
		final double grabKey = 0.001 * ConstProperty.getInstance().getGrabWeightKey() * (1 + GsConst.EFF_PER * PREY_RES_PER);// 掠夺系数
		int grabWeight = (int) Math.floor(grabKey * effVal * totalWeight);
		return grabWeight;
	}

	/**
	 * 计算军队损失战力
	 * 
	 * @param armyList
	 * @return
	 */
	public static int calcLostPower(List<ArmyInfo> armyList) {
		int lostPower = 0;
		if (armyList == null || armyList.size() == 0) {
			return lostPower;
		}
		for (ArmyInfo armyInfo : armyList) {
			int lostCnt = armyInfo.getWoundedCount() + armyInfo.getDeadCount();
			if (lostCnt <= 0) {
				continue;
			}
			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
			if (armyCfg == null || armyCfg.getPower() <= 0) {
				continue;
			}
			lostPower += armyCfg.getPower() * lostCnt;
		}
		return lostPower;
	}

	/**
	 * 合并多个玩家的兵
	 * 
	 * @param armyMap
	 * @return
	 */
	public static List<ArmyInfo> mergAllPlayerArmy(Map<String, List<ArmyInfo>> armyMap) {
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (armyMap == null) {
			return armyList;
		}

		List<List<ArmyInfo>> lists = new ArrayList<List<ArmyInfo>>();
		lists.addAll(armyMap.values());
		armyList = mergMultArmyList(lists);

		return armyList;
	}

	/**
	 * 合并多个List<ArmyInfo>
	 * 
	 * @return
	 */
	public static List<ArmyInfo> mergMultArmyList(List<List<ArmyInfo>> lists) {
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (lists == null || lists.size() == 0) {
			return armyList;
		}
		Map<Integer, ArmyInfo> tmpMap = new HashMap<Integer, ArmyInfo>();
		for (List<ArmyInfo> tmpList : lists) {
			for (ArmyInfo tmpInfo : tmpList) {
				ArmyInfo info = null;
				if (tmpMap.containsKey(tmpInfo.getArmyId())) {
					info = tmpMap.get(tmpInfo.getArmyId());
				} else {
					info = new ArmyInfo(tmpInfo.getArmyId(), 0);
					tmpMap.put(tmpInfo.getArmyId(), info);
				}

				info.setTotalCount(info.getTotalCount() + tmpInfo.getTotalCount());
				info.setDeadCount(info.getDeadCount() + tmpInfo.getDeadCount());
				info.setWoundedCount(info.getWoundedCount() + tmpInfo.getWoundedCount());
				info.setKillCount(info.getKillCount() + tmpInfo.getKillCount());
			}
		}
		for (ArmyInfo tmpInfo : tmpMap.values()) {
			armyList.add(tmpInfo);
		}
		return armyList;
	}

	/**
	 * 获取行军目标点通知类型
	 * 
	 * @param march
	 * @return
	 */
	@Deprecated
	public static int getMarchTargetPointType(WorldPoint point) {
		if (point == null) {
			return 0;
		}

		switch (point.getPointType()) {
		case WorldPointType.PLAYER_VALUE:
			return MarchTargetPointType.PLAYER_CITY_POINT_VALUE;

		case WorldPointType.RESOURCE_VALUE:
			return MarchTargetPointType.RESOURCE_POINT_VALUE;

		case WorldPointType.QUARTERED_VALUE:
			return MarchTargetPointType.QUARTERED_POINT_VALUE;

		case WorldPointType.KING_PALACE_VALUE:
			return MarchTargetPointType.CAPITAL_POINT_VALUE;

		default:
			return 0;
		}

	}

	/**
	 * 整理相同兵种到一个对象里面
	 * 
	 * @param mailArmys
	 */
	public static Map<Integer, ArmyInfo> combineArmyInfo(List<ArmyInfo> mailArmys) {
		Map<Integer, ArmyInfo> map = new HashMap<Integer, ArmyInfo>();
		for (ArmyInfo info : mailArmys) {
			ArmyInfo temp = map.get(info.getArmyId());
			if (temp == null) {
				temp = new ArmyInfo();
				temp.setArmyId(info.getArmyId());
				map.put(info.getArmyId(), temp);
			}
			temp.setDeadCount(temp.getDeadCount() + info.getDeadCount());
			temp.setWoundedCount(temp.getWoundedCount() + info.getWoundedCount());
		}
		return map;
	}

	/**
	 * 获取战斗结束时死亡兵(包括伤兵)的数量
	 * 
	 * @param armyInfoList
	 * @return
	 */
	public static int getArmySoldierInfo(List<ArmyInfo> armyInfoList, Map<Integer, Integer> deadArmyMap) {
		int killCount = 0;
		if (armyInfoList != null) {
			for (ArmyInfo armyInfo : armyInfoList) {
				killCount += armyInfo.getKillCount();
				int count = armyInfo.getDeadCount() + armyInfo.getWoundedCount();
				if (count > 0) {
					BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
					if (armyCfg != null) {
						int level = armyCfg.getLevel();
						if (deadArmyMap.containsKey(level)) {
							count += deadArmyMap.get(level);
						}
						deadArmyMap.put(level, count);
					}
				}
			}
		}

		return killCount;
	}

	/**
	 * 获取行军队列的士兵人口总数
	 * 
	 * @param marchs
	 * @return
	 */
	public static int getMarchArmyTotal(WorldMarch... marchs) {
		int total = 0;
		if (marchs != null && marchs.length > 0) {
			for (WorldMarch march : marchs) {
				for (ArmyInfo army : march.getArmys()) {
					total += army.getFreeCnt();
				}
			}
		}
		return total;
	}

	/**
	 * 获取队伍里行军速度最慢的兵种的速度
	 * 
	 * @param armys
	 * @return
	 */
	public static double minSpeedInArmy(Player player, List<ArmyInfo> armys) {
		double minSpeed = 0.0d;
		for (ArmyInfo armyInfo : armys) {
			if (armyInfo.getFreeCnt() <= 0) {
				continue;
			}
			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
			if (armyCfg == null) {
				continue;
			}
			double speed = getBaseArmyEffectSpeed(player, armyCfg.getSpeed(), armyCfg.getType());
			if (minSpeed == 0) {
				minSpeed = speed;
				continue;
			}
			if (minSpeed > speed) {
				armyCfg.getType();
				minSpeed = speed;
			}
		}
		return minSpeed;
	}

	private static double getBaseArmyEffectSpeed(Player player, int speed, int armyType) {
		if (player == null) {
			return speed;
		}
		
		double effectSpeed = 0.0;
		switch (armyType) {
		case SoldierType.TANK_SOLDIER_1_VALUE:
			effectSpeed = speed * (1 + player.getEffect().getEffVal(EffType.MARCH_SPD_TANK_SOLDIER_1) * GsConst.EFF_PER);
			break;
		case SoldierType.TANK_SOLDIER_2_VALUE:
			effectSpeed = speed * (1 + player.getEffect().getEffVal(EffType.MARCH_SPD_TANK_SOLDIER_2) * GsConst.EFF_PER);
			break;
		case SoldierType.PLANE_SOLDIER_3_VALUE:
			effectSpeed = speed * (1 + player.getEffect().getEffVal(EffType.MARCH_SPD_PLANE_SOLDIER_3) * GsConst.EFF_PER);
			break;
		case SoldierType.PLANE_SOLDIER_4_VALUE:
			effectSpeed = speed * (1 + player.getEffect().getEffVal(EffType.MARCH_SPD_PLANE_SOLDIER_4) * GsConst.EFF_PER);
			break;
		case SoldierType.FOOT_SOLDIER_5_VALUE:
			effectSpeed = speed * (1 + player.getEffect().getEffVal(EffType.MARCH_SPD_FOOT_SOLDIER_5) * GsConst.EFF_PER);
			break;
		case SoldierType.FOOT_SOLDIER_6_VALUE:
			effectSpeed = speed * (1 + player.getEffect().getEffVal(EffType.MARCH_SPD_FOOT_SOLDIER_6) * GsConst.EFF_PER);
			break;
		case SoldierType.CANNON_SOLDIER_7_VALUE:
			effectSpeed = speed * (1 + player.getEffect().getEffVal(EffType.MARCH_SPD_CANNON_SOLDIER_7) * GsConst.EFF_PER);
			break;
		case SoldierType.CANNON_SOLDIER_8_VALUE:
			effectSpeed = speed * (1 + player.getEffect().getEffVal(EffType.MARCH_SPD_CANNON_SOLDIER_8) * GsConst.EFF_PER);
			break;
		default:
			break;
		}
		return effectSpeed;
	}
	
	/**
	 * 生成存储字符串
	 *
	 * @return
	 */
	public static String convertArmyListToDbString(List<ArmyInfo> armys) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < armys.size(); i++) {
			if (i > 0) {
				result.append(",");
			}
			result.append(armys.get(i).toString());
		}
		return result.toString();
	}

	public static List<ArmyInfo> convertStringToArmyList(String armyStr) {
		// 军队
		List<ArmyInfo> armys = new ArrayList<ArmyInfo>();
		if (armyStr != null && !armyStr.equals("")) {
			String[] strs = armyStr.split(",");
			for (int i = 0; i < strs.length; i++) {
				ArmyInfo info = new ArmyInfo(strs[i]);
				armys.add(info);
			}
		}
		return armys;
	}

	/**
	 * 是否是可援助点
	 * 
	 * @param pointType
	 * @return
	 */
	public static boolean isAssistantPoint(int pointType) {
		if (pointType == WorldPointType.PLAYER_VALUE) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是王座点
	 * 
	 * @param wp
	 * @return
	 */
	public static boolean isPresidentPoint(WorldPoint wp) {
		return wp.getPointType() == WorldPointType.KING_PALACE_VALUE;
	}

	/**
	 * 是否是王座箭塔点
	 * 
	 * @param wp
	 * @return
	 */
	public static boolean isPresidentTowerPoint(WorldPoint wp) {
		return wp.getPointType() == WorldPointType.CAPITAL_TOWER_VALUE;
	}
	
	/**
	 * 是否是超级武器点
	 * @param wp
	 * @return
	 */
	public static boolean isSuperWeaponPoint(WorldPoint wp) {
		return wp.getPointType() == WorldPointType.SUPER_WEAPON_VALUE;
	}
	
	/**
	 * 是否是联盟机甲舱体占位点
	 * @param wp
	 * @return
	 */
	public static boolean isGuildSpacePoint(WorldPoint wp) {
		return wp.getPointType() == WorldPointType.SPACE_MECHA_MAIN_VALUE || wp.getPointType() == WorldPointType.SPACE_MECHA_SLAVE_VALUE;
	}
	
	/**
	 * 是否是超级武器点
	 * @param wp
	 * @return
	 */
	public static boolean isXZQPoint(WorldPoint wp) {
		return wp.getPointType() == WorldPointType.XIAO_ZHAN_QU_VALUE;
	}
	
	/**
	 * 是否是航海要塞点
	 */
	public static boolean isFortressPoint(WorldPoint wp) {
		return wp.getPointType() == WorldPointType.CROSS_FORTRESS_VALUE;
	}
	
	/**
	 * 是否是集结类行军
	 * 
	 * @param marchType
	 * @return
	 */
	public static boolean isMassMarch(int marchType) {
		return marchType == WorldMarchType.MASS_VALUE
				|| marchType == WorldMarchType.MONSTER_MASS_VALUE
				|| marchType == WorldMarchType.PRESIDENT_MASS_VALUE
				|| marchType == WorldMarchType.PRESIDENT_TOWER_MASS_VALUE
				|| marchType == WorldMarchType.MANOR_MASS_VALUE
				|| marchType == WorldMarchType.MANOR_ASSISTANCE_MASS_VALUE
				|| marchType == WorldMarchType.FOGGY_FORTRESS_MASS_VALUE
				|| marchType == WorldMarchType.SUPER_WEAPON_MASS_VALUE
				|| marchType == WorldMarchType.XZQ_MASS_VALUE
				|| marchType == WorldMarchType.GUNDAM_MASS_VALUE
				|| marchType == WorldMarchType.NIAN_MASS_VALUE
				|| marchType == WorldMarchType.TREASURE_HUNT_MONSTER_MASS_VALUE
				|| marchType == WorldMarchType.FORTRESS_MASS_VALUE
				|| marchType == WorldMarchType.CHRISTMAS_MASS_VALUE
				|| marchType == WorldMarchType.WAR_FLAG_MASS_VALUE
				|| marchType == WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_VALUE
				|| marchType == WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_VALUE
				|| marchType == WorldMarchType.DRAGON_ATTACT_MASS_VALUE
				;
	}

	/**
	 * 是否是集结参与类型的行军
	 * 
	 * @param marchType
	 * @return
	 */
	public static boolean isMassJoinMarch(int marchType) {
		return marchType == WorldMarchType.MASS_JOIN_VALUE 
				|| marchType == WorldMarchType.MONSTER_MASS_JOIN_VALUE 
				|| marchType == WorldMarchType.PRESIDENT_MASS_JOIN_VALUE
				|| marchType == WorldMarchType.PRESIDENT_TOWER_MASS_JOIN_VALUE
				|| marchType == WorldMarchType.MANOR_MASS_JOIN_VALUE 
				|| marchType == WorldMarchType.MANOR_ASSISTANCE_MASS_JOIN_VALUE
				|| marchType == WorldMarchType.FOGGY_FORTRESS_MASS_JOIN_VALUE
				|| marchType == WorldMarchType.TREASURE_HUNT_MONSTER_MASS_JOIN_VALUE
				|| marchType == WorldMarchType.FOGGY_FORTRESS_MASS_JOIN_VALUE
				|| marchType == WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_JOIN_VALUE
				|| marchType == WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_JOIN_VALUE
				;
	}

	/**
	 * 是否是集结攻击玩家类型的行军
	 * 
	 * @param marchType
	 * @return
	 */
	public static boolean isMassAttackMarch(WorldMarch march) {
		if (march == null) {
			return false;
		}

		int marchType = march.getMarchType();
		return marchType == WorldMarchType.MASS_VALUE || marchType == WorldMarchType.MASS_JOIN_VALUE
				|| marchType == WorldMarchType.MONSTER_MASS_VALUE || marchType == WorldMarchType.MONSTER_MASS_JOIN_VALUE
				|| marchType == WorldMarchType.PRESIDENT_MASS_VALUE || marchType == WorldMarchType.PRESIDENT_MASS_JOIN_VALUE
				|| marchType == WorldMarchType.PRESIDENT_TOWER_MASS_VALUE || marchType == WorldMarchType.PRESIDENT_TOWER_MASS_JOIN_VALUE
				|| marchType == WorldMarchType.MANOR_MASS_VALUE || marchType == WorldMarchType.MANOR_MASS_JOIN_VALUE;
	}

	/**
	 * 是否是集结援助玩家类型的行军
	 * 
	 * @param marchType
	 * @return
	 */
	public static boolean isMassAssistanceMarch(WorldMarch march) {
		if (march == null) {
			return false;
		}

		int marchType = march.getMarchType();
		return marchType == WorldMarchType.PRESIDENT_ASSISTANCE_MASS_VALUE || marchType == WorldMarchType.PRESIDENT_ASSISTANCE_MASS_JOIN_VALUE

				|| marchType == WorldMarchType.MANOR_ASSISTANCE_MASS_VALUE || marchType == WorldMarchType.MANOR_ASSISTANCE_MASS_JOIN_VALUE;
	}

	/**
	 * 已到达或者已驻扎的行军
	 * 
	 * @param march
	 * @return
	 */
	public static boolean isReachAndStopMarch(WorldMarch march) {
		return march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE || march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_REACH_VALUE
				|| isManorMarchReachStatus(march.getMarchStatus());
	}

	/**
	 * 根据集结队长行军类型，找队员行军类型
	 * 
	 * @param massMarchType
	 * @return
	 */
	public static int getJoinMarchType(int massMarchType) {
		int marchType = 0;
		if (massMarchType == WorldMarchType.MASS_VALUE) {
			marchType = WorldMarchType.MASS_JOIN_VALUE;
		} else if (massMarchType == WorldMarchType.PRESIDENT_MASS_VALUE) {
			marchType = WorldMarchType.PRESIDENT_MASS_JOIN_VALUE;
		} else if (massMarchType == WorldMarchType.PRESIDENT_TOWER_MASS_VALUE) {
			marchType = WorldMarchType.PRESIDENT_TOWER_MASS_JOIN_VALUE;
		} else if (massMarchType == WorldMarchType.PRESIDENT_ASSISTANCE_MASS_VALUE) {
			marchType = WorldMarchType.PRESIDENT_ASSISTANCE_MASS_JOIN_VALUE;
		} else if (massMarchType == WorldMarchType.MONSTER_MASS_VALUE) {
			marchType = WorldMarchType.MONSTER_MASS_JOIN_VALUE;
		} else if (massMarchType == WorldMarchType.MANOR_MASS_VALUE) {
			marchType = WorldMarchType.MANOR_MASS_JOIN_VALUE;
		} else if (massMarchType == WorldMarchType.MANOR_ASSISTANCE_MASS_VALUE) {
			marchType = WorldMarchType.MANOR_ASSISTANCE_MASS_JOIN_VALUE;
		} else if (massMarchType == WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_VALUE) {
			marchType = WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_JOIN_VALUE;
		} else if (massMarchType == WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_VALUE) {
			marchType = WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_JOIN_VALUE;
		}else if (massMarchType == WorldMarchType.DRAGON_ATTACT_MASS_VALUE) {
			marchType = WorldMarchType.DRAGON_ATTACT_MASS_JOIN_VALUE;
		}
		return marchType;
	}

	/**
	 * 获取列表剩余兵力
	 * 
	 * @param remainArmys
	 * @return
	 */
	public static int getFreeArmyCnt(List<ArmyInfo> remainArmys) {
		int cnt = 0;
		if (remainArmys != null && remainArmys.size() > 0) {
			for (ArmyInfo info : remainArmys) {
				cnt += info.getFreeCnt();
			}
		}
		return cnt;
	}

	/**
	 * 获取采集速度
	 * 
	 * @param player
	 * @param resType
	 * @param heroId
	 * @return
	 */
	public static double getCollectSpeed(Player player, int resType, int resLevel, WorldPoint point, EffectParams effParams) {
		// 基础采集速度
		double baseSpeed = getCollectBaseSpeed(player, resType, effParams);
		
		int addBoost = 0;
		switch (resType) {
		case PlayerAttr.GOLDORE_VALUE:
		case PlayerAttr.GOLDORE_UNSAFE_VALUE:
			addBoost = player.getEffect().getEffVal(EffType.RES_GOLD_COLLECT_BOOST, effParams); // effect:21
			break;
		case PlayerAttr.OIL_VALUE:
		case PlayerAttr.OIL_UNSAFE_VALUE:
			addBoost = player.getEffect().getEffVal(EffType.RES_OIL_COLLECT_BOOST, effParams); // effect:322
			break;
		case PlayerAttr.TOMBARTHITE_VALUE:
		case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			addBoost = player.getEffect().getEffVal(EffType.RES_ALLOY_COLLECT_BOOST, effParams); // effect:323
			break;
		case PlayerAttr.STEEL_VALUE:
		case PlayerAttr.STEEL_UNSAFE_VALUE:
			addBoost = player.getEffect().getEffVal(EffType.RES_STEEL_COLLECT_BOOST, effParams); // effect:324
			break;
		case PlayerAttr.GOLD_VALUE:
			addBoost = player.getEffect().getEffVal(EffType.RES_CRYSTAL_COLLECT, effParams); // effect:326
			break;
		default:
			break;
		}

		int allAddBoost = 0;
		int allAddToolBoost = 0;
		int allAddSkillBoost = 0;
		int allAddMonthCardBoost = 0;
		int allAddSuperLab = 0;
		int backPrivilegeResCollect = 0;
		int allAddNewServerBoost = 0;
		
		// 不是金币矿才计算下面这些
		if (resType != PlayerAttr.GOLD_VALUE) {
			allAddBoost = player.getEffect().getEffVal(EffType.RES_COLLECT_BOOST, effParams); // effect:320
			if (WorldMarchConstProperty.getInstance().isSpecialResLevel(resLevel)) {
				
				if (resLevel == 9) {
					allAddBoost += WorldMarchConstProperty.getInstance().getSpecialResBuffVal();
				}
				
				String effect = point.getShowEffect();
				if (!HawkOSOperator.isEmptyString(effect)) {
					String[] effect379 = effect.split("_");
					allAddSuperLab = Integer.parseInt(effect379[1]); // effect:379
				}
			}
			
			allAddToolBoost = player.getEffect().getEffVal(EffType.RES_COLLECT_BUF, effParams); // effect:325
			allAddSkillBoost = player.getEffect().getEffVal(EffType.RES_COLLECT_SKILL, effParams); // effect:332
			allAddMonthCardBoost = player.getEffect().getEffVal(EffType.RES_COLLECT_MONTH_CARD, effParams); // effect:333
			backPrivilegeResCollect = player.getEffect().getEffVal(EffType.BACK_PRIVILEGE_RES_COLLECT, effParams); // effect:
			allAddNewServerBoost = player.getEffect().getEffVal(EffType.RES_COLLECT_NEW_SERVER, effParams) + player.getEffect().getEffVal(EffType.EFF_520, effParams); // effect:
		}

		// 实际采集速度
		double speed = baseSpeed * (1 + (addBoost + allAddBoost + allAddSkillBoost + allAddToolBoost + allAddMonthCardBoost + allAddSuperLab + backPrivilegeResCollect + allAddNewServerBoost) * GsConst.EFF_PER);
		
		return speed;
	}

	/**
	 * 获取采集基础速度
	 * 
	 * @param resType
	 * @return
	 */
	public static double getCollectBaseSpeed(Player player, int resType, EffectParams effParams) {
		double typeSpeed = 0.0d;
		switch (resType) {
		case PlayerAttr.GOLD_VALUE:
			typeSpeed = WorldMarchConstProperty.getInstance().getCollectRes1001speed() / 1000000d;
			break;
		case PlayerAttr.GOLDORE_UNSAFE_VALUE:
			typeSpeed = WorldMarchConstProperty.getInstance().getCollectRes1007speed() / 1000000d;
			break;
		case PlayerAttr.OIL_UNSAFE_VALUE:
			typeSpeed = WorldMarchConstProperty.getInstance().getCollectRes1008speed() / 1000000d;
			break;
		case PlayerAttr.STEEL_UNSAFE_VALUE:
			typeSpeed = WorldMarchConstProperty.getInstance().getCollectRes1009speed() / 1000000d;
			break;
		case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			typeSpeed = WorldMarchConstProperty.getInstance().getCollectRes1010speed() / 1000000d;
			break;
		case PlayerAttr.GOLDORE_VALUE:
			typeSpeed = WorldMarchConstProperty.getInstance().getCollectRes1107speed() / 1000000d;
			break;
		case PlayerAttr.OIL_VALUE:
			typeSpeed = WorldMarchConstProperty.getInstance().getCollectRes1108speed() / 1000000d;
			break;
		case PlayerAttr.STEEL_VALUE:
			typeSpeed = WorldMarchConstProperty.getInstance().getCollectRes1109speed() / 1000000d;
			break;
		case PlayerAttr.TOMBARTHITE_VALUE:
			typeSpeed = WorldMarchConstProperty.getInstance().getCollectRes1110speed() / 1000000d;
			break;
		default:
			break;
		}
		
		int addPercent = 0;
		switch (resType) {
		case PlayerAttr.GOLDORE_VALUE:
		case PlayerAttr.GOLDORE_UNSAFE_VALUE:
			addPercent = player.getEffect().getEffVal(EffType.RES_GOLD_COLLECT, effParams); // effect:328
			break;
		case PlayerAttr.OIL_VALUE:
		case PlayerAttr.OIL_UNSAFE_VALUE:
			addPercent = player.getEffect().getEffVal(EffType.RES_OIL_COLLECT, effParams); // effect:329
			break;
		case PlayerAttr.TOMBARTHITE_VALUE:
		case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			addPercent = player.getEffect().getEffVal(EffType.RES_ALLOY_COLLECT, effParams); // effect:323
			break;
		case PlayerAttr.STEEL_VALUE:
		case PlayerAttr.STEEL_UNSAFE_VALUE:
			addPercent = player.getEffect().getEffVal(EffType.RES_STEEL_COLLECT, effParams); // effect:324
			break;
		case PlayerAttr.GOLD_VALUE:
			break;
		default:
			break;
		}
		
		int allAddPercent = 0;
		
		// 金币矿不计算327
		if (resType != PlayerAttr.GOLD_VALUE) {
			allAddPercent = player.getEffect().getEffVal(EffType.RES_COLLECT, effParams); // effect:327
		}
		
		double baseSpeed = typeSpeed * (1 + (addPercent + allAddPercent) * GsConst.EFF_PER);
		return baseSpeed;
	}

	/**
	 * 获得行军和玩家的关系
	 * 
	 * @param march
	 * @param player
	 * @return
	 */
	public static WorldMarchRelation getRelation(WorldMarch march, Player player) {
		
		if (march.getMarchType() == WorldMarchType.YURI_STRIKE_MARCH_VALUE) {
			return WorldMarchRelation.YURI_STRIKE_RELATION;
		}
		
		if (march.getMarchType() == WorldMarchType.YURI_MONSTER_VALUE) {
			// 自己和同公会的显示红线
			if (GuildService.getInstance().isInTheSameGuild(player.getId(), march.getPlayerId())) {
				return WorldMarchRelation.ENEMY;
			} else {
				return WorldMarchRelation.NONE;
			}
		}
		
		if (march.getMarchType() == WorldMarchType.SPACE_MECHA_MONSTER_ATTACK_MARCH_VALUE || march.getMarchType() == WorldMarchType.SPACE_MECHA_EMPTY_MARCH_VALUE) {
			if (march.getTargetId().equals(player.getGuildId())) {
				return WorldMarchRelation.ENEMY;
			} else {
				return WorldMarchRelation.NONE;
			}
		}

		// 自己的行军
		if (march.getPlayerId().equals(player.getId())) {
			return WorldMarchRelation.SELF;
		}
		
		// 跨服国家集结行军处理
//		try {
//			if (CrossActivityService.getInstance().isOpen()) {
//				IWorldMarch iworldMarch = WorldMarchService.getInstance().getMarch(march.getMarchId());
//				if (iworldMarch != null && iworldMarch.isNationMassMarch()) {
//					String myServer = player.getMainServerId();
//					String marchServer = iworldMarch.getPlayer().getMainServerId();
//					if (myServer.equals(marchServer)) {
//						return WorldMarchRelation.GUILD_FRIEND;
//					}
//					
//					int camp1 = CrossActivityService.getInstance().getCamp(myServer);
//					int camp2 = CrossActivityService.getInstance().getCamp(marchServer);
//					if (camp1 == camp2) {
//						return WorldMarchRelation.NONE;
//					} else {
//						return WorldMarchRelation.ENEMY;
//					}
//				}
//			}
//		} catch (Exception e) {
//			HawkException.catchException(e);
//		}
		
		if (march.getMarchType() == WorldMarchType.CENTER_FLAG_REWARD_MARCH_VALUE) {
			if (GuildService.getInstance().isInTheSameGuild(player.getId(), march.getPlayerId())) {
				return WorldMarchRelation.GUILD_FRIEND;
			} else {
				return WorldMarchRelation.NONE;
			}
		}

		// 同盟玩家行军
		if (GuildService.getInstance().isInTheSameGuild(player.getId(), march.getPlayerId())) {
			return WorldMarchRelation.GUILD_FRIEND;
		}
		
		// 目标点
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
		if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			targetPoint = WorldPointService.getInstance().getWorldPoint(march.getOrigionId());
		}

		// 目标点为null
		if (targetPoint == null) {
			return WorldMarchRelation.NONE;
		}
		
		// 判断 自己联盟id 目标点联盟id 和 行军发起者联盟id关系
		String marchPlayerId = march.getPlayerId();
		switch (targetPoint.getPointType()) {
		
		case WorldPointType.PLAYER_VALUE:
		case WorldPointType.RESOURCE_VALUE:
		case WorldPointType.QUARTERED_VALUE:
		case WorldPointType.STRONG_POINT_VALUE:
		case WorldPointType.TH_RESOURCE_VALUE:
		case WorldPointType.PYLON_VALUE:
		case WorldPointType.CHRISTMAS_BOX_VALUE:
			String targetPlayerId = targetPoint.getPlayerId();
			if (HawkOSOperator.isEmptyString(targetPlayerId)) {
				break;
			}
			
			if ((player.getId().equals(targetPlayerId) 
					|| GuildService.getInstance().isInTheSameGuild(player.getId(), targetPlayerId))
					&& !GuildService.getInstance().isInTheSameGuild(player.getId(), marchPlayerId)) {
				return WorldMarchRelation.ENEMY;
			}
			
			break;								
		case WorldPointType.GUILD_TERRITORY_VALUE:
			String targetGuildId = targetPoint.getGuildId();
			if (GuildService.getInstance().isPlayerInGuild(targetGuildId, player.getId())
					&& !GuildService.getInstance().isPlayerInGuild(targetGuildId, marchPlayerId)) {
				return WorldMarchRelation.ENEMY;
			}
			
			Player manorLeader = GuildManorService.getInstance().getManorLeader(targetPoint.getId());
			if (manorLeader != null
					&& GuildService.getInstance().isInTheSameGuild(player.getId(), manorLeader.getId())
					&& !GuildService.getInstance().isInTheSameGuild(player.getId(), marchPlayerId)) {
				return WorldMarchRelation.ENEMY;
			}
			break;
			
		case WorldPointType.KING_PALACE_VALUE:
			String currentGuildId = PresidentFightService.getInstance().getCurrentGuildId();
			if (player.hasGuild() && player.getGuildId().equals(currentGuildId)
					&& !GuildService.getInstance().isInTheSameGuild(player.getId(), march.getPlayerId())) {
				return WorldMarchRelation.ENEMY;
			}
			break;
			
		case WorldPointType.CAPITAL_TOWER_VALUE:
			String towerGuildId = PresidentFightService.getInstance().getPresidentTowerGuild(targetPoint.getId());
			if (player.hasGuild() && player.getGuildId().equals(towerGuildId)
					&& !GuildService.getInstance().isInTheSameGuild(player.getId(), march.getPlayerId())) {
				return WorldMarchRelation.ENEMY;
			}
			break;
			
		case WorldPointType.SUPER_WEAPON_VALUE:
			IWeapon weapon = SuperWeaponService.getInstance().getWeapon(targetPoint.getId());
			String weaponGuildId = weapon.getGuildId();
			if (player.hasGuild() && player.getGuildId().equals(weaponGuildId)
					&& !GuildService.getInstance().isInTheSameGuild(player.getId(), marchPlayerId)) {
				return WorldMarchRelation.ENEMY;
			}
			break;
		case WorldPointType.XIAO_ZHAN_QU_VALUE:
			XZQWorldPoint xzqPoint = XZQService.getInstance().getXZQPoint(targetPoint.getId());
			String controlGuildId = xzqPoint.getGuildControl();
			String occupyGuildId = xzqPoint.getOccupyGuild();
			if (player.hasGuild() && player.getGuildId().equals(controlGuildId)
					&& !GuildService.getInstance().isInTheSameGuild(player.getId(), marchPlayerId)) {
				return WorldMarchRelation.ENEMY;
			}
			if (player.hasGuild() && player.getGuildId().equals(occupyGuildId)
					&& !GuildService.getInstance().isInTheSameGuild(player.getId(), marchPlayerId)) {
				return WorldMarchRelation.ENEMY;
			}
			break;
		
		case WorldPointType.CROSS_FORTRESS_VALUE:
			Player fortressLeader = WorldMarchService.getInstance().getFortressLeader(targetPoint.getId());
			if (fortressLeader == null || !fortressLeader.hasGuild()) {
				return WorldMarchRelation.NONE;
			}
			if (player.hasGuild() && player.getGuildId().equals(fortressLeader.getGuildId())
					&& !GuildService.getInstance().isInTheSameGuild(player.getId(), marchPlayerId)) {
				return WorldMarchRelation.ENEMY;
			}
			break;
			
		case WorldPointType.WAR_FLAG_POINT_VALUE:
			if (!player.hasGuild()) {
				return WorldMarchRelation.NONE;
			}
			
			IFlag flag = FlagCollection.getInstance().getFlag(targetPoint.getGuildBuildId());
			if (flag == null) {
				return WorldMarchRelation.NONE;
			}
			
			// 看别人摧毁自己的战旗
			if (player.getGuildId().equals(flag.getCurrentId())
					&& !GuildService.getInstance().isInTheSameGuild(player.getId(), marchPlayerId)) {
				return WorldMarchRelation.ENEMY;
			}
			
			// 自己盟摧毁别人战旗，看别人的收复(攻击)行军
			Player flagLeader = WorldMarchService.getInstance().getFlagLeader(flag.getFlagId());
			if (flagLeader != null
					&& flagLeader.hasGuild()
					&& GuildService.getInstance().isInTheSameGuild(player.getId(), flagLeader.getGuildId())
					&& !GuildService.getInstance().isInTheSameGuild(player.getId(), marchPlayerId)) {
				return WorldMarchRelation.ENEMY;
			}
			break;		
		}
		
		return WorldMarchRelation.NONE;
	}

	/**
	 * 获得玩家当前位置
	 * 
	 * @param march
	 *            参与集结的玩家的行军
	 * @return
	 */
	public static AlgorithmPoint getMarchCurrentPosition(WorldMarch march) {
		long startTime = march.getStartTime();
		double startX = march.getOrigionX();
		double startY = march.getOrigionY();

		if (march.getItemUseTime() > 0) {
			startTime = march.getItemUseTime();
			startX = march.getItemUseX();
			startY = march.getItemUseY();
		}

		return getMarchCurrentPosition(new AlgorithmPoint(startX, startY), new AlgorithmPoint(march.getTerminalX(), march.getTerminalY()), startTime, march.getEndTime());
	}

	/**
	 * 根据分段数据、开始和结束时间，计算当前走到了哪一段路程，同时计算走到的点 params: marchParts = [onePart1,
	 * onePart2, ...] local onePart1 = {} onePart1.startPos =
	 * Utilitys.ccpCopy(crossPt) onePart1.endPos = Utilitys.ccpCopy(endPos)
	 * onePart1.isSlowDown = true startTime、endTime为毫秒 partIndex
	 * 分段索引（marchParts的索引） currPos 当前点，线段marchParts[partIndex]上的一个点
	 **/
	private static AlgorithmPoint getMarchCurrentPosition(AlgorithmPoint startPoint, AlgorithmPoint endPoint, long startTime, long endTime) {
		List<MarchPart> marchParts = getMarchParts(startPoint, endPoint);

		// 计算每段距离
		// 计算总距离百分比
		double totalDisPer = 0;
		for (int i = 0; i < marchParts.size(); i++) {
			MarchPart partData = marchParts.get(i);
			totalDisPer = totalDisPer + partData.getDistancePer();
		}

		// 根据剩余的距离，计算走到了哪个分段，然后从那个分段开始构建数据
		double totalTime = endTime - startTime;
		double curTime = HawkTime.getMillisecond();

		// 走过的时间
		double pastTime = curTime - startTime;

		// 已经走过的时间占总时间的百分比
		double pastTimePer = pastTime / totalTime;

		double calcTimePer = 0;
		for (int i = 0; i < marchParts.size(); i++) {

			MarchPart partData = marchParts.get(i);
			calcTimePer += partData.getDistancePer();

			// 走完当前分段所耗时间，占总消耗时间的百分比
			double calcCurrTimePer = calcTimePer / totalDisPer;

			// 因为是从起点开始遍历，那么如果经历过的时间百分比小于 calcCurrTimePer，或者已经是最后一段（后两个条件同一目的）
			if (calcCurrTimePer > pastTimePer || calcCurrTimePer >= 1) {

				// 计算这一分段中，走了多少距离 (当前消耗时间百分比 - 上一分段累加的百分比) * 当前分段的真实距离
				double partMovedDis = (pastTimePer - (calcTimePer - partData.getDistancePer()) / totalDisPer) * partData.getDistance();

				// 计算走到了哪个点,partMovedDis为已经走的距离，调用这个接口时要从终点往起点算
				return getGapPointOnSegment(partMovedDis, partData.getEndPoint(), partData.getStartPoint());
			}
		}
		return null;
	}

	/**
	 * 获得距离断点一段距离的点
	 * 
	 * @param distance
	 * @param startPoint
	 * @param endPoint
	 * @return
	 */
	private static AlgorithmPoint getGapPointOnSegment(double distance, AlgorithmPoint startPoint, AlgorithmPoint endPoint) {
		double totalLength = startPoint.distanceTo(endPoint);
		if (totalLength > 1 ) {
			totalLength = (totalLength - WorldMarchConstProperty.getInstance().getDistanceSubtractionParam()) / Math.sqrt(2);
		}
		
		double percent = 1.0f - distance / totalLength;
		percent = Math.min(1, percent);
		percent = Math.max(0, percent);
		double x = (endPoint.getX() - startPoint.getX()) * percent + startPoint.getX();
		double y = (endPoint.getY() - startPoint.getY()) * percent + startPoint.getY();
		AlgorithmPoint temp = new AlgorithmPoint(x, y);
		return temp;
	}

	/**
	 * 行军分隔为段
	 * 
	 * @param startPoint
	 * @param endPoint
	 * @return
	 */
	public static List<MarchPart> getMarchParts(AlgorithmPoint startPoint, AlgorithmPoint endPoint) {
		// 计算和黑土地的交点信息
		List<MarchPart> list = new ArrayList<MarchPart>();
		Point[] points = WorldMapConstProperty.getInstance().getCapitalPoints();
		HawkTuple3<Integer, AlgorithmPoint, AlgorithmPoint> crossPoints = getMarchCross(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY(), points);
		if (crossPoints.first == 0) {
			MarchPart part = new MarchPart(startPoint, endPoint, false, true);
			list.add(part);
		} else if (crossPoints.first == 2) {
			AlgorithmPoint cross1 = crossPoints.second;
			AlgorithmPoint cross2 = crossPoints.third;
			//如果开始和结束都在黑土地, 则加速中间段
			if(cross1.equals(startPoint) && cross2.equals(endPoint)){
				MarchPart part = new MarchPart(startPoint, endPoint, true, true);
				list.add(part);
			} else {
				MarchPart part1 = new MarchPart(startPoint, cross1, false, true);
				list.add(part1);
				
				MarchPart part2 = new MarchPart(cross1, cross2, true, true);
				list.add(part2);

				MarchPart part3 = new MarchPart(cross2, endPoint, false, true);
				list.add(part3);
			}
		} else if (crossPoints.first == 1) {
			AlgorithmPoint cross = crossPoints.second;
			if (cross != null) {
				MarchPart part1 = new MarchPart(startPoint, cross, true, true);
				list.add(part1);

				MarchPart part2 = new MarchPart(cross, endPoint, false, true);
				list.add(part2);
			} else {
				cross = crossPoints.third;

				MarchPart part1 = new MarchPart(startPoint, cross, false, true);
				list.add(part1);

				MarchPart part2 = new MarchPart(cross, endPoint, true, true);
				list.add(part2);
			}
		}
		return list;
	}

	/**
	 * 计算行军和区域的交点
	 * 
	 * @param origionX
	 * @param origionY
	 * @param terminalX
	 * @param terminalY
	 * @return 第一个参数返回交点个数,第二个参数返回距离起点的交点,第二个返回距离终点的交点,若没有都是null
	 */
	public static boolean isMarchCrossArea(int fromId, int toId, Point[] points) {
		int[] posFrom = GameUtil.splitXAndY(fromId);
		int[] posTo = GameUtil.splitXAndY(toId);
		return getMarchCross(posFrom[0], posFrom[1], posTo[0], posTo[1], points).first != 0;
	}

	/**
	 * 计算行军和区域的交点
	 * 
	 * @param origionX
	 * @param origionY
	 * @param terminalX
	 * @param terminalY
	 * @return 第一个参数返回交点个数,第二个参数返回距离起点的交点,第二个返回距离终点的交点,若没有都是null
	 */
	private static HawkTuple3<Integer, AlgorithmPoint, AlgorithmPoint> getMarchCross(double origionX, double origionY, double terminalX, double terminalY, Point[] points) {
		// 起点和终点
		AlgorithmPoint origion = new AlgorithmPoint(origionX, origionY);
		AlgorithmPoint terminal = new AlgorithmPoint(terminalX, terminalY);

		// 顶点
		AlgorithmPoint top = new AlgorithmPoint(points[0].getX(), points[0].getY());
		AlgorithmPoint bottom = new AlgorithmPoint(points[1].getX(), points[1].getY());
		AlgorithmPoint left = new AlgorithmPoint(points[2].getX(), points[2].getY());
		AlgorithmPoint right = new AlgorithmPoint(points[3].getX(), points[3].getY());

		// 判断起点和终点是否在菱形内
		boolean isOriIn = isInDiamondArea(left, right, top, bottom, origion);
		boolean isTerIn = isInDiamondArea(left, right, top, bottom, terminal);

		// 两个都在,两个可能在同一条边上 ，原来的起始和终点就算作减速的起始点终点
		if (isOriIn && isTerIn) {
			return new HawkTuple3<Integer, AlgorithmPoint, AlgorithmPoint>(2, origion, terminal);
		}

		// 线段和线段是否有交点.3
		AlgorithmPoint point1 = AlgorithmUtil.getIntersection(origion, terminal, left, top);
		AlgorithmPoint point2 = AlgorithmUtil.getIntersection(origion, terminal, top, right);
		AlgorithmPoint point3 = AlgorithmUtil.getIntersection(origion, terminal, right, bottom);
		AlgorithmPoint point4 = AlgorithmUtil.getIntersection(origion, terminal, bottom, left);

		// 没有交点
		if (point1 == null && point2 == null && point3 == null && point4 == null) {
			return new HawkTuple3<Integer, AlgorithmPoint, AlgorithmPoint>(0, null, null);
		}

		// 记录最后交点
		List<AlgorithmPoint> end = new ArrayList<AlgorithmPoint>();
		int num = 0;
		if (point1 != null) {
			num++;
			end.add(point1);
		}

		if (point2 != null) {
			num++;
			end.add(point2);
		}

		if (point3 != null) {
			num++;
			end.add(point3);
		}

		if (point4 != null) {
			num++;
			end.add(point4);
		}

		// 一个交点时
		if (num == 1) {
			if (isOriIn) {// 起点在菱形内,计算起点和交点的距离
				return new HawkTuple3<Integer, AlgorithmPoint, AlgorithmPoint>(num, end.get(0), null);
			} else if (isTerIn) {// 终点在菱形内,计算交点和终点的距离
				return new HawkTuple3<Integer, AlgorithmPoint, AlgorithmPoint>(num, null, end.get(0));
			}
		}

		// 两个交点时要确认哪个是黑土地的起点，哪个是黑土地的终点
		if (num == 2) {
			AlgorithmPoint pointTemp1 = end.get(0);
			AlgorithmPoint pointTemp2 = end.get(1);
			if (Math.abs(pointTemp1.getX() - origionX) - Math.abs(pointTemp2.getX() - origionX) < 0) {
				return new HawkTuple3<Integer, AlgorithmPoint, AlgorithmPoint>(num, pointTemp1, pointTemp2);
			} else {
				return new HawkTuple3<Integer, AlgorithmPoint, AlgorithmPoint>(num, pointTemp2, pointTemp1);
			}
		}

		return new HawkTuple3<Integer, AlgorithmPoint, AlgorithmPoint>(0, null, null);
	}

	/**
	 * 是否在菱形区域内
	 * 
	 * @param 顶点
	 * @param x是左角到中心点的横向距离
	 *            ，y是上角到中心的纵向距离
	 **/
	private static Boolean isInDiamondArea(AlgorithmPoint left, AlgorithmPoint right, AlgorithmPoint top, AlgorithmPoint bottom, AlgorithmPoint crossPoint) {
		int capitalRadiusX = WorldMapConstProperty.getInstance().getCapitalCoreRange()[0];
		int capitalRadiusY = WorldMapConstProperty.getInstance().getCapitalCoreRange()[1];
		if (capitalRadiusX < 1 || capitalRadiusY < 1) {
			return false;
		}

		// 边界判断
		if (crossPoint.getX() < left.getX() || crossPoint.getX() > right.getX()) {
			return false;
		}

		if (crossPoint.getY() < top.getY() || crossPoint.getY() > bottom.getY()) {
			return false;
		}

		// 距离左顶点的偏移量
		double offsetX = crossPoint.getX() - left.getX();
		double k = capitalRadiusY / capitalRadiusX;

		// 菱形中心点左侧
		if (offsetX < capitalRadiusX) {
			return crossPoint.getY() >= (left.getY() - offsetX * k) && crossPoint.getY() <= (left.getY() + offsetX * k);
		} else if (offsetX > capitalRadiusX) {// 菱形中心点右侧
			offsetX = capitalRadiusX * 2 - offsetX;
			return crossPoint.getY() >= (left.getY() - offsetX * k) && crossPoint.getY() <= (left.getY() + offsetX * k);
		}

		return true;
	}

	/**
	 * 删除一条首都驻军
	 * 
	 * @param pushType
	 * @param guildId
	 * @param manorId
	 * @param marchId
	 * @param playerId
	 */
	public static HawkProtocol getQuarteredMarchDelItemProtocol(PushQuarteredMarchType pushType, String guildId, int manorId, String marchId, String playerId) {
		PushQuarteredMarchDelItem.Builder delBuilder = PushQuarteredMarchDelItem.newBuilder();
		delBuilder.setPushType(pushType);
		delBuilder.setGuildId(guildId);
		delBuilder.setManorId(manorId);
		delBuilder.setMarchId(marchId);
		delBuilder.setPlayerId(playerId);
		return HawkProtocol.valueOf(HP.code.PUSH_DEL_QUARTERED_MARCHS, delBuilder);
	}

	/**
	 * 计算指定坐标点的世界观察者列表
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static Set<String> calcPointViewers(int x, int y, int viewRadiusX, int viewRadiusY) {
		try {
			if (viewRadiusY <= 0) {
				viewRadiusX = GameConstCfg.getInstance().getViewXRadius();
			}

			if (viewRadiusY <= 0) {
				viewRadiusY = GameConstCfg.getInstance().getViewYRadius();
			}

			Set<HawkAOIObj> inviewObjs = WorldScene.getInstance().getRangeObjs(x, y, viewRadiusX, viewRadiusY);
			if (inviewObjs == null || inviewObjs.size() <= 0) {
				return null;
			}

			Set<String> viewerIds = new HashSet<String>();
			for (HawkAOIObj aoiObj : inviewObjs) {
				if (aoiObj.getType() == GsConst.WorldObjType.PLAYER) {
					Player player = (Player) aoiObj.getUserData();
					if (player == null) {
						continue;
					}
					viewerIds.add(player.getId());
				}
			}
			return viewerIds;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 点是否在区域内
	 * 
	 * @param point
	 * @param fromPoint
	 *            区域起始点
	 * @param toPoint
	 *            区域终止点
	 * @return
	 */
	public static boolean isPointInArea(int point, int fromPoint, int toPoint) {
		if (GameUtil.splitXAndY(point)[0] > GameUtil.splitXAndY(fromPoint)[0] && GameUtil.splitXAndY(point)[1] > GameUtil.splitXAndY(fromPoint)[1]
				&& GameUtil.splitXAndY(point)[0] < GameUtil.splitXAndY(toPoint)[0] && GameUtil.splitXAndY(point)[1] < GameUtil.splitXAndY(toPoint)[1]) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是玩家城点
	 * 
	 * @param worldPoint
	 * @return
	 */
	public static boolean isPlayerPoint(WorldPoint worldPoint) {
		if (worldPoint == null) {
			return false;
		}
		return worldPoint.getPointType() == WorldPointType.PLAYER_VALUE;
	}

	/**
	 * 是否是机器人行军
	 * 
	 * @param worldPoint
	 * @return
	 */
	public static boolean isRobotMarch(WorldMarch march) {
		if (march == null) {
			return false;
		}
		return march.getTargetPointType() == WorldPointType.ROBOT_VALUE;
	}

	/**
	 * 是否是联盟建筑
	 * 
	 * @param worldPoint
	 * @return
	 */
	public static boolean isGuildPoint(WorldPoint worldPoint) {
		return worldPoint.getPointType() == WorldPointType.GUILD_TERRITORY_VALUE;
	}

	/**
	 * 是否是需要tick计算的行军
	 * 
	 * @param march
	 * @return
	 */
	public static boolean isNeedCalcTickMarch(WorldMarch march) {
		if (march == null) {
			return false;
		}
		// 出征，收集，回程，集结等待需要tick
		return march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE || march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE
				|| march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE || march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
	}

	/**
	 * 是否是行军过程中的行军
	 * 
	 * @param march
	 * @return
	 */
	public static boolean isMarchState(WorldMarch march) {
		return march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE;
	}

	/**
	 * 可被动行军类型
	 * 
	 * @param march
	 * @return
	 */
	public static boolean isBeMarchedType(WorldMarch march) {
		if (march == null) {
			return false;
		}
		int marchType = march.getMarchType();
		return march.getTargetId() != null
				&& (marchType == WorldMarchType.ATTACK_PLAYER_VALUE || marchType == WorldMarchType.SPY_VALUE || marchType == WorldMarchType.ASSISTANCE_VALUE
						|| marchType == WorldMarchType.ASSISTANCE_RES_VALUE || marchType == WorldMarchType.MASS_VALUE || WorldUtil.isMassJoinMarch(marchType));
	}

	/**
	 * 是否是返程行军
	 * 
	 * @param march
	 * @return
	 */
	public static boolean isReturnBackMarch(IWorldMarch march) {
		if (march == null) {
			return false;
		}
		return march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE;
	}

	/**
	 * 是否是援助类型行军(资源援助，士兵援助)
	 * 
	 * @param march
	 * @return
	 */
	public static boolean isAssistanceMarch(WorldMarch march) {
		return march.getMarchType() == WorldMarchType.ASSISTANCE_RES_VALUE || march.getMarchType() == WorldMarchType.ASSISTANCE_VALUE;
	}

	/**
	 * 是否是联盟大本
	 * 
	 * @return
	 */
	public static boolean isGuildBastion(WorldPoint wp) {
		if (wp == null || wp.getBuildingId() == 0) {
			return false;
		}
		return wp.getBuildingId() == TerritoryType.GUILD_BASTION_VALUE;
	}

	/**
	 * 是否是联盟大本
	 * 
	 * @return
	 */
	public static boolean isGuildBastion(TerritoryType territoryType) {
		if (territoryType == null) {
			return false;
		}
		return territoryType.getNumber() == TerritoryType.GUILD_BASTION_VALUE;
	}

	/**
	 * 是否是联盟箭塔
	 * 
	 * @return
	 */
	public static boolean isGuildBartizan(TerritoryType territoryType) {
		return territoryType.getNumber() == TerritoryType.GUILD_BARTIZAN_VALUE;
	}

	/**
	 * 是否是联盟建筑点类型
	 * 
	 * @param wp
	 * @return
	 */
	public static boolean isGuildBuildPoint(WorldPoint wp) {
		return wp.getPointType() == WorldPointType.GUILD_TERRITORY_VALUE;
	}

	/**
	 * 是否是联盟建筑点类型
	 * 
	 * @param wp
	 * @return
	 */
	public static boolean isGuildBuildPoint(int pointId) {
		WorldPoint wp = WorldPointService.getInstance().getWorldPoint(pointId);
		if (wp == null) {
			return false;
		}
		return wp.getPointType() == WorldPointType.GUILD_TERRITORY_VALUE;
	}

	/**
	 * 是否是空的世界点
	 * 
	 * @param wp
	 * @return
	 */
	public static boolean isEmptyPoint(WorldPoint wp) {
		return wp == null || wp.getPointType() == WorldPointType.EMPTY_VALUE;
	}

	/**
	 * 是否是联盟领地行军
	 * 
	 * @param worldMarch
	 * @return
	 */
	public static boolean isManorMarch(WorldMarch worldMarch) {
		int marchType = worldMarch.getMarchType();
		return marchType == WorldMarchType.MANOR_BUILD_VALUE || marchType == WorldMarchType.MANOR_REPAIR_VALUE || marchType == WorldMarchType.MANOR_COLLECT_VALUE
				|| marchType == WorldMarchType.MANOR_SINGLE_VALUE || marchType == WorldMarchType.MANOR_ASSISTANCE_VALUE

				|| marchType == WorldMarchType.MANOR_ASSISTANCE_MASS_VALUE || marchType == WorldMarchType.MANOR_ASSISTANCE_MASS_JOIN_VALUE

				|| marchType == WorldMarchType.MANOR_MASS_VALUE || marchType == WorldMarchType.MANOR_MASS_JOIN_VALUE || marchType == WorldMarchType.DRAGON_ATTACT_MASS_VALUE 
				|| marchType == WorldMarchType.DRAGON_ATTACT_MASS_JOIN_VALUE;
	}

	/**
	 * 是否是联盟领地行军
	 * 
	 * @param worldMarch
	 * @return
	 */
	public static boolean isManorMarchReachStatus(int status) {
		return status == WorldMarchStatus.MARCH_STATUS_MANOR_BREAK_VALUE || status == WorldMarchStatus.MARCH_STATUS_MANOR_REPAIR_VALUE
				|| status == WorldMarchStatus.MARCH_STATUS_MANOR_BUILD_VALUE || status == WorldMarchStatus.MARCH_STATUS_MANOR_GARRASION_VALUE;
	}

	/**
	 * 是否是可以被联盟箭塔攻击的行军类型:
	 * 1、攻击玩家 
	 * 2、集结攻击玩家 
	 * 3、参与集结攻击玩家 
	 * 4、采集资源点 
	 * 5、驻扎点 
	 * 6、联盟领地单人行军 
	 * 7、联盟领地集结行军
	 * 8、联盟领地加入集结行军
	 * 
	 * @return
	 */
	public static boolean canBeAttackedByBartizan(WorldMarch march) {
		return march.getMarchType() == WorldMarchType.ATTACK_PLAYER_VALUE || march.getMarchType() == WorldMarchType.MASS_VALUE
				|| march.getMarchType() == WorldMarchType.MASS_JOIN_VALUE || march.getMarchType() == WorldMarchType.COLLECT_RESOURCE_VALUE
				|| march.getMarchType() == WorldMarchType.ARMY_QUARTERED_VALUE || march.getMarchType() == WorldMarchType.MANOR_SINGLE_VALUE
				|| march.getMarchType() == WorldMarchType.MANOR_MASS_VALUE || march.getMarchType() == WorldMarchType.MANOR_MASS_JOIN_VALUE;
	}

	/**
	 * 获取区域顶点
	 * 
	 * @param centerId
	 *            中心点id
	 * @param radius
	 *            半径
	 */
	public static Point[] getAreaVertex(int centerId, int radius) {
		int centerX = GameUtil.splitXAndY(centerId)[0];
		int centerY = GameUtil.splitXAndY(centerId)[1];

		int maxX = WorldMapConstProperty.getInstance().getWorldMaxX();
		int maxY = WorldMapConstProperty.getInstance().getWorldMaxY();

		Point[] vertex = new Point[4];
		vertex[0] = (new Point(centerX, centerY - radius < 0 ? 0 : centerY - radius));// 上
		vertex[1] = (new Point(centerX, centerY + radius > maxY ? maxY : centerY + radius));// 下
		vertex[2] = (new Point(centerX - radius < 0 ? 0 : centerX - radius, centerY));// 左
		vertex[3] = (new Point(centerX + radius > maxX ? maxX : centerX + radius, centerY));// 右

		return vertex;
	}

	/**
	 * 是否是合金资源类型
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isTombarthiteRes(int type) {
		return type == PlayerAttr.TOMBARTHITE_VALUE || type == PlayerAttr.TOMBARTHITE_UNSAFE_VALUE;
	}

	/**
	 * 是否是铀矿资源类型
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isSteelRes(int type) {
		return type == PlayerAttr.STEEL_VALUE || type == PlayerAttr.STEEL_UNSAFE_VALUE;
	}

	/**
	 * 是否是世界采集资源行军类型
	 * 
	 * @param march
	 * @return
	 */
	public static boolean isCollectResMarch(WorldMarch march) {
		if (march == null) {
			return false;
		}
		return march.getMarchType() == WorldMarchType.COLLECT_RESOURCE_VALUE;
	}

	/**
	 * 是否是采集超级矿行军类型
	 * 
	 * @param march
	 * @return
	 */
	public static boolean isCollectSuperResMarch(WorldMarch march) {
		if (march == null) {
			return false;
		}
		return march.getMarchType() == WorldMarchType.MANOR_COLLECT_VALUE;
	}

	/**
	 * 是否是自己的城点、资源点、驻扎点
	 * 
	 * @param worldPoint
	 * @return
	 */
	public static boolean isOwnPoint(String playerId, WorldPoint worldPoint) {
		return worldPoint != null && worldPoint.getPlayerId() != null && worldPoint.getPlayerId().equals(playerId);
	}

	/**
	 * 是否需要显示到联盟战争中
	 * 
	 * @param march
	 * @return
	 */
	public static boolean needShowMarchInGuild(WorldMarch march, WorldPoint point) {
		if (march == null) {
			return false;
		}
		int marchType = march.getMarchType();
		// 这几种不显示
		if (marchType == WorldMarchType.MANOR_COLLECT_VALUE || marchType == WorldMarchType.SPY_VALUE || marchType == WorldMarchType.CAPTIVE_RELEASE_VALUE) {
			return false;
		}
		if (!isPresidentPoint(point) && !isGuildBuildPoint(point)) {
			// 目标点又是玩家 && 不是自己
			return !HawkOSOperator.isEmptyString(point.getPlayerId()) && !march.getPlayerId().equals(point.getPlayerId());
		}
		return true;
	}

	/**
	 * 是否是联盟攻击行军类型(联盟战争界面显示 -> 联盟攻击)
	 * 
	 * @param march
	 * @return
	 */
	public static boolean isGuildWarAttackMarch(WorldMarch march) {
		if (march == null) {
			return false;
		}

		int marchType = march.getMarchType();
		if (marchType == WorldMarchType.ATTACK_PLAYER_VALUE || marchType == WorldMarchType.ARMY_QUARTERED_VALUE || marchType == WorldMarchType.COLLECT_RESOURCE_VALUE
				|| marchType == WorldMarchType.PRESIDENT_SINGLE_VALUE || marchType == WorldMarchType.MANOR_SINGLE_VALUE) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是侦查类型的行军
	 * 
	 * @param marchType
	 * @return
	 */
	public static boolean isSpyMarch(int marchType) {
		return marchType == WorldMarchType.SPY_VALUE;
	}

	/**
	 * 是否是随机迁城类型
	 * @param type
	 * @return
	 */
	public static boolean isRandomMoveCity(int type) {
		return type == CityMoveType.RANDOM_MOVE_VALUE;
	}

	/**
	 * 是否是定点迁城类型
	 * @param type
	 * @return
	 */
	public static boolean isSelectMoveCity(int type) {
		return type == CityMoveType.SELECT_MOVE_VALUE;
	}
	
	/**
	 * 是否是公会迁城
	 * @param type
	 * @return
	 */
	public static boolean isGuildMoveCity(int type){
		return type == CityMoveType.GUILD_MOVE_VALUE;
	}

	/**
	 * 是否是联盟自动迁城
	 * @param type
	 * @return
	 */
	public static boolean isGuildAutoMoveCity(int type){
		return type == CityMoveType.GUILD_CREATE_AUTO_MOVE_VALUE ||
				type == CityMoveType.GUILD_JOIN_AUTO_MOVE_VALUE;
	}
	/**
	 * 是否需要加入行军警报
	 * @param march
	 * @return
	 */
	public static boolean isNeedAddToAlarm(IWorldMarch march) {
		if (march.getMarchEntity() == null) {
			return false;
		}

		int marchType = march.getMarchEntity().getMarchType();
		return marchType == WorldMarchType.COLLECT_RESOURCE_VALUE || marchType == WorldMarchType.ATTACK_PLAYER_VALUE || marchType == WorldMarchType.ASSISTANCE_VALUE
				|| marchType == WorldMarchType.ARMY_QUARTERED_VALUE || marchType == WorldMarchType.SPY_VALUE || marchType == WorldMarchType.MASS_VALUE
				|| marchType == WorldMarchType.PRESIDENT_SINGLE_VALUE || marchType == WorldMarchType.PRESIDENT_MASS_VALUE
				|| marchType == WorldMarchType.PRESIDENT_ASSISTANCE_MASS_VALUE || marchType == WorldMarchType.PRESIDENT_ASSISTANCE_VALUE
				|| marchType == WorldMarchType.YURI_MONSTER_VALUE;
	}

	/**
	 * 算两个点的距离
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double distance(double x1, double y1, double x2, double y2) {
		double a = Math.pow(Math.abs(x1 - x2), 2);
		double b = Math.pow(Math.abs(y1 - y2), 2);
		double distance = Math.sqrt(a + b);
		return distance;
	}

	/**
	 * 是否是攻打野怪的行军
	 * @return
	 */
	public static boolean isAtkMonsterMarch(IWorldMarch iWorldMarch) {
		WorldMarchType marchType = iWorldMarch.getMarchType();
		return marchType.equals(WorldMarchType.ATTACK_MONSTER)
				|| marchType.equals(WorldMarchType.RANDOM_BOX)
				|| marchType.equals(WorldMarchType.MONSTER_MASS)
				|| marchType.equals(WorldMarchType.MONSTER_MASS_JOIN);
	}
	
	/**
	 * 是否是攻打高达行军
	 * @return
	 */
	public static boolean isAtkBossMarch(IWorldMarch iWorldMarch) {
		WorldMarchType marchType = iWorldMarch.getMarchType();
		return marchType.equals(WorldMarchType.GUNDAM_SINGLE)
				|| marchType.equals(WorldMarchType.GUNDAM_MASS)
				|| marchType.equals(WorldMarchType.GUNDAM_MASS_JOIN)
				
				|| marchType.equals(WorldMarchType.NIAN_SINGLE)
				|| marchType.equals(WorldMarchType.NIAN_MASS)
				|| marchType.equals(WorldMarchType.NIAN_MASS_JOIN)
						
				|| marchType.equals(WorldMarchType.CHRISTMAS_SINGLE)
				|| marchType.equals(WorldMarchType.CHRISTMAS_MASS)
				|| marchType.equals(WorldMarchType.CHRISTMAS_MASS_JOIN);
	}
	
	/**
	 * 是否是驻扎状态 (采集，据点，驻扎)
	 * @param iMarch
	 * @return
	 */
	public static boolean isQuarterStatus(IWorldMarch iMarch) {
		int marchStatus = iMarch.getMarchEntity().getMarchStatus();
		return marchStatus == WorldMarchStatus.MARCH_STATUS_MARCH_REACH.getNumber()
				|| marchStatus == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT.getNumber()
				|| marchStatus == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED.getNumber();
	}
	
	/**
	 * 是否是跨服屏蔽行军类型
	 * @param marchType
	 * @return
	 */
	public static boolean isCrossLimitMarch(WorldMarchType marchType) {		
		return marchType.equals(WorldMarchType.GUNDAM_SINGLE)
				|| marchType.equals(WorldMarchType.GUNDAM_MASS)
				|| marchType.equals(WorldMarchType.GUNDAM_MASS_JOIN)
				
				|| marchType.equals(WorldMarchType.SUPER_WEAPON_SINGLE)
				|| marchType.equals(WorldMarchType.SUPER_WEAPON_MASS)
				|| marchType.equals(WorldMarchType.SUPER_WEAPON_MASS_JOIN)
				
				|| marchType.equals(WorldMarchType.NIAN_SINGLE)
				|| marchType.equals(WorldMarchType.NIAN_MASS)
				|| marchType.equals(WorldMarchType.NIAN_MASS_JOIN)
						
				|| marchType.equals(WorldMarchType.CHRISTMAS_MASS)
				|| marchType.equals(WorldMarchType.CHRISTMAS_MASS_JOIN)
				|| marchType.equals(WorldMarchType.CHRISTMAS_SINGLE)
				|| marchType.equals(WorldMarchType.CHRISTMAS_BOX_MARCH)
				
				|| marchType.equals(WorldMarchType.WAR_FLAG_MARCH)
				|| marchType.equals(WorldMarchType.WAR_FLAG_MASS)
				|| marchType.equals(WorldMarchType.WAR_FLAG_MASS_JOIN)
				
				|| marchType.equals(WorldMarchType.SPACE_MECHA_MAIN_MARCH_SINGLE)
				|| marchType.equals(WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS)
				|| marchType.equals(WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_JOIN)
				|| marchType.equals(WorldMarchType.SPACE_MECHA_SLAVE_MARCH_SINGLE)
				|| marchType.equals(WorldMarchType.SPACE_MECHA_BOX_COLLECT)
				|| marchType.equals(WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_SINGLE)
				|| marchType.equals(WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS)
				|| marchType.equals(WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_JOIN)
				;
	}
}