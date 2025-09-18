package com.hawk.robot.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;

/**
 * 战斗单元信息配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/battle_soldier.xml")
public class BattleSoldierCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 兵种类型
	protected final int type;
	// 士兵等级
	protected final int level;
	// 占位优先级
	protected final int posPrior;
	// 几回合攻击一次 放大100倍 50/100/200/300
	protected final int attackRound;
	// 攻击半径
	protected final int attackDis;
	// 攻击
	protected final int attack;
	// 生命
	protected final int hp;
	// 防御
	protected final int defence;
	// 一回合可移动步数，大于等于0的整数，0表示不可移动
	protected final int moveStep;
	// 训练时间
	protected final int time;
	// 训练消耗资源
	protected final String res;
	// 治疗消耗资源
	protected final int recoverRes;
	// 超时空急救站急救消耗资源
	protected final int superTimeRes;
	// 优先攻击类型
	protected final String firstTypes;
	// 负重
	protected final int load;
	// 兵种行军速度
	private final int speed;
	// 治疗时间
	protected final double recoverTime;
	// 战力
	protected final float power;
	// 兵营的建筑类型
	protected final int building;
	// 技能id
	protected final String skillIds;
	// 士兵石油消耗
	protected final int consume;
   	protected final String promotion;
	
	private static List<Integer> soldierIdList = new ArrayList<>();
	private static Map<Integer, List<Integer>> buildingSoldierIds = new HashMap<Integer, List<Integer>>();
	
	public BattleSoldierCfg() {
		id = 0;
		type = 0;
		level = 0;
		posPrior = 0;
		attackRound = 0;
		attackDis = 0;
		moveStep = 0;
		firstTypes = "";
		attack = 0;
		defence = 0;
		hp = 0;
		building = 0;
		load = 0;
		speed = 0;
		power = 0;
		time = 0;
		res = "";
		recoverTime = 0d;
		recoverRes = 100;
		superTimeRes = 100;
		skillIds = "";
		consume = 0;
		promotion = "";
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getLevel() {
		return level;
	}

	public int getBuilding() {
		return building;
	}

	public int getPosPrior() {
		return posPrior;
	}

	public int getAttackRound() {
		return attackRound;
	}

	public int getAttackDis() {
		return attackDis;
	}

	public int getAttack() {
		return attack;
	}

	public int getHp() {
		return hp;
	}

	public int getDefence() {
		return defence;
	}

	public int getMoveStep() {
		return moveStep;
	}

	public int getTime() {
		return time;
	}

	public String getRes() {
		return res;
	}

	public int getRecoverRes() {
		return recoverRes;
	}

	public String getFirstTypes() {
		return firstTypes;
	}

	public int getLoad() {
		return load;
	}

	public int getSpeed() {
		return speed;
	}

	public double getRecoverTime() {
		return recoverTime;
	}

	public float getPower() {
		return power;
	}

	public String getSkillIds() {
		return skillIds;
	}

	@Override
	protected boolean assemble() {
		soldierIdList.add(id);
		
		List<Integer> soldierIds = buildingSoldierIds.get(building);
		if(soldierIds == null) {
			soldierIds = new LinkedList<Integer>();
			buildingSoldierIds.put(building, soldierIds);
		}
		soldierIds.add(id);
		
		return true;
	}
	
	/**
	 * 获取所有的兵种id
	 * @return
	 */
	public static List<Integer> getSoldierIdList() {
		return soldierIdList;
	}
	
	/**
	 * 从所有的兵种id中随机获取一个
	 * @return
	 */
	public static int randSoldierId() {
		if(soldierIdList.size() <= 0) {
			return 0;
		}
		
		int index = HawkRand.randInt(soldierIdList.size() - 1);
		return soldierIdList.get(index);
	}
	
	/**
	 * 根据建筑类型获取对应的兵种id列表
	 * @param buildType
	 * @return
	 */
	public static List<Integer> getSoldierIdList(int buildType) {
		return buildingSoldierIds.get(buildType);
	}
	
	/**
	 * 根据建筑类型随机获取一个对应的兵种id
	 * @param buildType
	 * @return
	 */
	public static int randSoldierIdByBuildType(BuildingCfg buildCfg) {
		List<Integer> soldierIdList = getSoldierIdList(buildCfg.getBuildType());
		if(soldierIdList == null || soldierIdList.size() <= 0) {
			return 0;
		}
		
		Collections.shuffle(soldierIdList);
		Optional<Integer> op = soldierIdList.stream().filter(e -> buildCfg.getUnlockedSoldierIds().contains(e)).findAny();
		if (op.isPresent()) {
			return op.get();
		}
		
		return 0;
	}
	
	/**
	 * 获取所有造兵建筑类型
	 * @return
	 */
	public static List<Integer> getBuildingTypeList() {
		return buildingSoldierIds.keySet().stream().collect(Collectors.toList());
	}
	
}
