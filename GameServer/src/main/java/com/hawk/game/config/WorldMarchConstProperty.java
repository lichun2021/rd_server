package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 世界行军常量配置
 *
 * @author julia
 *
 */
@HawkConfigManager.KVResource(file = "xml/world_march_const.xml")
public class WorldMarchConstProperty extends HawkConfigBase {
	/**
	 * 设置一个单例对象, 以便访问
	 */
	private static WorldMarchConstProperty instance = null;

	public static WorldMarchConstProperty getInstance() {
		return instance;
	}

	// 默认队列数目
	protected final int worldMarchBaseNum;

	// 默认行军速度（行进坐标/秒）（用前除1000）
	private final int worldMarchBaseVelocity;
	
	// 行军距离减去系数
	private final float distanceSubtractionParam;

	// 最大行军距离
	private final int worldMarchMaxDistance;

	// 世界行军穿过核心区域行军时间倍数
	private final float worldMarchCoreRangeTime;

	// 采集资源1001速度（x点/秒）（用前除以1000）
	protected final int collectRes1001speed;
		
	// 采集资源1007速度（x点/秒）（用前除以1000）
	protected final int collectRes1007speed;

	// 采集资源1008速度（x点/秒）（用前除以1000）
	protected final int collectRes1008speed;

	// 采集资源1009速度（x点/秒）（用前除以1000）
	protected final int collectRes1009speed;

	// 采集资源1010速度（x点/秒）（用前除以1000）
	protected final int collectRes1010speed;

	protected final int collectRes1107speed;

	protected final int collectRes1108speed;

	protected final int collectRes1109speed;

	protected final int collectRes1110speed;
	
	// 资源1001对应负重（1点=x负重）
	protected final int res1001Weight;
	
	// 资源1007对应负重（1点=x负重）
	protected final int res1007Weight;

	// 资源1008对应负重（1点=x负重）
	protected final int res1008Weight;

	// 资源1009对应负重（1点=x负重）
	protected final int res1009Weight;

	// 资源1010对应负重（1点=x负重）
	protected final int res1010Weight;

	protected final int res1107Weight;

	protected final int res1108Weight;

	protected final int res1109Weight;

	protected final int res1110Weight;

	// 攻打野怪扣除体力（每次）
	protected final int atkEnemyCostVitPoint;
	
	//尤里复仇怪物速度参数
	protected final float yuriMarchCoefficient;

	// 攻打野怪不伤兵比例（死兵按比例转化为伤兵，其余自动复活）（用前除以1000）
	protected final int atkEnemyHurtRatio;

	// # 玩家首次攻击某等级的怪物特殊处理
	protected final String newMonsterLevel;
	
	// # 玩家首次攻击某等级的怪物行军时间上限
	protected final String newMonsterMarchTime;
	
	// 连续攻打野怪可选择次数
	private final String atkEnemyContinuityNums;

	// 侦查兵力浮动范围（用前除1000）
	private final double scoutSoldierRandomRange;

	// 侦查防御武器浮动范围
	private final int scoutDefWeaponRandomRange;
	// 侦查相关作用号
	private final String scoutEffectID;

	// 行军报告相关作用号
	private final String attackReportEffectID;

	// 侦查行军速度
	private final int investigationMarchSpeed;

	// 侦查花费基础值(消耗黄金资源)
	private final String investigationMarchCost;

	// 资源援助行军速度
	private final int resourceAssistMarchSpeed;
	
	// 集结等待时间
	private final String worldGatherTime;

	// 机甲集结等待时间
	private final String gundamMassTime;
	/**
	 * 机甲召唤集结等待时间
	 */
	private final String spmechaMassTime;
	
	// 野怪集结等待时间
	private final String monsterMass;

	// 机甲攻击次数限制
	private final int gundamAtkLimit;

	// 年兽集结等待时间
	private final String nianMassTime;

	private final double nianTypeAdjustParam;
	
	// 年兽攻击次数限制
	private final int nianAtkLimit;
	
	// 集结参加者上限基础值
	private final int assemblyQueueNum;

	// 临时队列开启上限
	private final int tempAssemblyQueueUpper;

	// 临时队列开启消耗钻石
	private final String tempAssemblyQueueCost;

	// 被击飞天数计数
	private final int daysOfDefeated;

	// 被击飞次数计数
	private final int numsOfDefeated;

	// 通用抓将行军速度
	private final int generalBackMarchSpeed;

	// 新手击败尤里残部行军时间
	private final int newGuidePvpMarchTime;

	// 英雄行军速度
	private final int heroMarchSpeed;

	// 攻打精英野怪首领体力消耗
	protected final int atkEliteEnemyCostVitPoint;

	// # 行军距离修正参数
	private final double distanceAdjustParam;
	// # 部队行军类型行军时间调整参数
	private final double armyTypeAdjustParam;
	// # 新版野怪行军时间调整参数
	private final double newMonsterAdjustParam;
	// # 侦察行军时间调整参数
	private final double reconnoitreTypeAdjustParam;
	
	// 机甲宝箱行军时间参数调整
	private final double nianBoxAdjustParam;
	
	// # 资源援助行军时间调整参数
	private final double resAidTypeAdjustParam;
	// # 领取世界宝箱行军时间调整参数
	private final double boxTypeAdjustParam;
	// # 联盟仓库存取资源行军时间调整参数
	private final double allianceStoreAdjustParam;
	// # 攻打野怪类型行军时间调整参数
	private final double monsterTypeAdjustParam;
	// # 攻打高达类型行军时间调整参数
	private final double bossTypeAdjustParam;
	// 行军上需要显示的作用号
	private final String marchShowEff;

	private final int spyMarkDisappearTime;

	private final int assistanceResTimes;
	
	private final int beAssistanceResTimes;
	
	// # 单次攻击机甲伤害上限
	private final int gundamOnceKillLimit;
	
	// # 集结攻击机甲伤害上限
	private final int massGundamOnceKillLimit;
	
	// # 单次攻击年兽伤害上限
	private final int nianOnceKillLimit;
	
	// # 集结攻击年兽伤害上限
	private final int massNianOnceKillLimit;
	
	
	// # 能量道具(打野掉落)
	private final String energyDetector;
	
	// # 能量道具翻倍系数(打野掉落)
	private final int energyDetectorMultipleEffect;
	
	// # 勋章道具(打野掉落)
	private final String honnerDetector;
	
	// # 勋章道具翻倍系数(打野掉落)
	private final int honnerDetectorMultipleEffect;
		
	private final String specialResLevel;
	
	private final int specialResBuffVal;

	// 邀请集结cd时间
	private final int inviteMassCD;
	
	// 能量塔体力消耗
	private final int pylonVitCost;
	/**
	 * 圣诞boss行军速度参数调整.
	 */
	private final double christmasTypeAdjustParam;
	/**
	 * 圣诞boss 攻击次数限制
	 */
	private final int christmasAtkLimit;
	/**
	 * 单次攻击boss伤害上限（万分比）
	 */
	private final int christmasDeadlinessAtkLimit;
	/**
	 *  集结攻击boss伤害上限（万分比）
	 */
	private final int christmasMassDeadlinessAtkLimit;
	/**
	 * 圣诞宝箱的拾取限制.
	 */
	private final int christmasBoxReceiveLimit;
	/**
	 * 圣诞集结时间.
	 */
	private final String christmasMassTime;
	/**
	 * 担任伤害保底.
	 */
	private final int christmasDeadlinessAtkMin;
	/**
	 * 集结伤害保底
	 */
	private final int christmasMassDeadlinessAtkMin;
	/**
	 * 间谍行军速度调整参数
	 */
	private final double espionageAdjustParam;
	/**
	 * 打雪球消耗
	 */
	private final String snowballAtkCost;
	
	private final int snowballAdjustParam;
	
	/**
	 * 间谍行军消耗
	 */
	private final String espionageCost;
	
	/**
	 * 战旗集结时间
	 */
	private final String warFlagGatherTime;
	
	
	
	private final int foggyAdjustParam;
	private final int monsterBossAdjustParam;
	
	// # 连续攻打野怪可选择次数
	private int[] atkEnemyContinuityNumsArray = null;
	// 侦查消耗
	private List<ItemInfo> investigationCostItems;
	
	private int[] waitTimeAry = null;
	
	private int[] monsterWaitTimeAry = null;
	
	private int[] gundamWaitTimeAry = null;
	
	private int[] spaceMechaWaitTimeAry = null;

	private int[] nianWaitTimeAry = null;
	
	private int[] scoutEffectIDMin_Max = new int[] { 0, 0 };

	private int[] attackReportEffectIDMin_Max = new int[] { 0, 0 };

	private int[] tempAssemblyQueueCostArray;

	private int[] marchShowEffArray;
	
	private Map<Integer, Integer> monsterLevelSpeed;
	
	private List<Integer> energyDetectorList;
	private List<Integer> honnerDetectorList;

	/**
	 * {@link #christmasMassTime}
	 */
	private Set<Integer> christmasMassTimeSet;
	
	private Set<Integer> warFlagMassSet = new HashSet<>();
	
	public WorldMarchConstProperty() {
		instance = this;
		snowballAdjustParam = 0;
		snowballAtkCost = "";
		espionageCost = "";
		investigationMarchSpeed = 150;
		investigationMarchCost = null;
		resourceAssistMarchSpeed = 117;
		atkEnemyContinuityNums = null;

		daysOfDefeated = 0;
		numsOfDefeated = 0;

		worldMarchBaseVelocity = 0;
		worldMarchMaxDistance = 0;
		worldMarchBaseNum = 0;
		collectRes1001speed = 0;
		collectRes1007speed = 0;
		collectRes1008speed = 0;
		collectRes1009speed = 0;
		collectRes1010speed = 0;
		collectRes1107speed = 0;
		collectRes1108speed = 0;
		collectRes1109speed = 0;
		collectRes1110speed = 0;
		res1001Weight = 0;
		res1007Weight = 0;
		res1008Weight = 0;
		res1009Weight = 0;
		res1010Weight = 0;
		res1107Weight = 0;
		res1108Weight = 0;
		res1109Weight = 0;
		res1110Weight = 0;
		atkEnemyCostVitPoint = 0;
		atkEnemyHurtRatio = 0;
		worldGatherTime = "";
		worldMarchCoreRangeTime = 0;

		scoutSoldierRandomRange = 0;
		scoutDefWeaponRandomRange = 0;
		assemblyQueueNum = 0;
		scoutEffectID = "";
		attackReportEffectID = "";

		tempAssemblyQueueUpper = 0;
		tempAssemblyQueueCost = "";
		generalBackMarchSpeed = 0;
		newGuidePvpMarchTime = 0;
		heroMarchSpeed = 0;
		atkEliteEnemyCostVitPoint = 0;

		distanceAdjustParam = 0.0;
		armyTypeAdjustParam = 0.0;
		reconnoitreTypeAdjustParam = 0.0;
		resAidTypeAdjustParam = 0.0;
		boxTypeAdjustParam = 0.0;
		yuriMarchCoefficient = 0.0f;
		allianceStoreAdjustParam = 0.0;
		distanceSubtractionParam = 0.0f;
		monsterTypeAdjustParam = 0.0;
		bossTypeAdjustParam = 120f;
		
		newMonsterLevel = "";
		newMonsterMarchTime = "";
		
		newMonsterAdjustParam = 0.0;
		marchShowEff = "";
		spyMarkDisappearTime = 1800;
		
		assistanceResTimes = 10;
		beAssistanceResTimes = 10;
		
		gundamMassTime = "";
		gundamAtkLimit = 0;
		
		gundamOnceKillLimit = 0;
		massGundamOnceKillLimit = 0;
		
		energyDetector = "";
		energyDetectorMultipleEffect = 0;
		
		nianMassTime = "";
		spmechaMassTime = "";
		nianAtkLimit = 0;
		nianOnceKillLimit = 0;
		massNianOnceKillLimit = 0;
		nianTypeAdjustParam = 120f;
		
		specialResLevel = "";
		specialResBuffVal = 10000;
		
		inviteMassCD = 60;
		monsterMass = "60_300_600_1800";
		
		nianBoxAdjustParam = 0.0;
		
		pylonVitCost = 0;
		this.christmasTypeAdjustParam = 120d;
		this.christmasAtkLimit = 30;
		this.christmasBoxReceiveLimit = 3;
		this.christmasDeadlinessAtkLimit = 100;
		this.christmasMassTime = "";
		this.christmasMassDeadlinessAtkLimit = 300;	
		this.christmasDeadlinessAtkMin = 100;
		this.christmasMassDeadlinessAtkMin = 200;
		
		espionageAdjustParam = 0.0;
		warFlagGatherTime = "";
		
		honnerDetector = "";
		honnerDetectorMultipleEffect = 0;
		
		foggyAdjustParam = 0;
		monsterBossAdjustParam = 0;
	}

	public int[] getScoutEffectIDMin_Max() {
		return scoutEffectIDMin_Max;
	}

	public int[] getAttackReportEffectIDMin_Max() {
		return attackReportEffectIDMin_Max;
	}

	public String getScoutEffectID() {
		return scoutEffectID;
	}

	public String getAttackReportEffectID() {
		return attackReportEffectID;
	}

	public int getAssemblyQueueNum() {
		return assemblyQueueNum;
	}

	public double getScoutSoldierRandomRange() {
		return scoutSoldierRandomRange;
	}

	public int getScoutDefWeaponRandomRange() {
		return scoutDefWeaponRandomRange;
	}

	public float getWorldMarchCoreRangeTime() {
		return worldMarchCoreRangeTime;
	}

	public List<ItemInfo> getInvestigationMarchCost() {
		return new ArrayList<>(investigationCostItems);
	}

	public int getAtkEnemyHurtRatio() {
		return atkEnemyHurtRatio * 10;// 转成万分比
	}

	public int getNumsOfDefeated() {
		return numsOfDefeated;
	}

	public int getDaysOfDefeated() {
		return daysOfDefeated;
	}

	public int getInvestigationMarchSpeed() {
		return investigationMarchSpeed;
	}

	public int getResourceAssistMarchSpeed() {
		return resourceAssistMarchSpeed;
	}

	public int getWorldMarchBaseNum() {
		return worldMarchBaseNum;
	}

	public float getYuriMarchCoefficient() {
		return yuriMarchCoefficient;
	}

	public int getWorldMarchMaxDistance() {
		return worldMarchMaxDistance;
	}

	public int getCollectRes1001speed() {
		return collectRes1001speed;
	}

	public int getCollectRes1007speed() {
		return collectRes1007speed;
	}

	public int getCollectRes1008speed() {
		return collectRes1008speed;
	}

	public int getCollectRes1009speed() {
		return collectRes1009speed;
	}

	public int getCollectRes1010speed() {
		return collectRes1010speed;
	}

	public int getRes1007Weight() {
		return res1007Weight;
	}

	public int getRes1008Weight() {
		return res1008Weight;
	}

	public int getRes1009Weight() {
		return res1009Weight;
	}

	public int getRes1010Weight() {
		return res1010Weight;
	}

	public int getCollectRes1107speed() {
		return collectRes1107speed;
	}

	public int getCollectRes1108speed() {
		return collectRes1108speed;
	}

	public int getCollectRes1109speed() {
		return collectRes1109speed;
	}

	public int getCollectRes1110speed() {
		return collectRes1110speed;
	}

	public String getWorldGatherTime() {
		return worldGatherTime;
	}

	public int[] getTempAssemblyQueueCostArray() {
		return tempAssemblyQueueCostArray;
	}

	public int getTempAssemblyQueueUpper() {
		return tempAssemblyQueueUpper;
	}

	public int getNewGuidePvpMarchTime() {
		return newGuidePvpMarchTime;
	}

	public int getHeroMarchSpeed() {
		return heroMarchSpeed;
	}

	public int getRes1107Weight() {
		return res1107Weight;
	}

	public int getRes1108Weight() {
		return res1108Weight;
	}

	public int getRes1109Weight() {
		return res1109Weight;
	}

	public int getRes1110Weight() {
		return res1110Weight;
	}

	public double getDistanceAdjustParam() {
		return distanceAdjustParam;
	}

	public double getArmyTypeAdjustParam() {
		return armyTypeAdjustParam;
	}

	public double getReconnoitreTypeAdjustParam() {
		return reconnoitreTypeAdjustParam;
	}

	public double getResAidTypeAdjustParam() {
		return resAidTypeAdjustParam;
	}

	public double getBoxTypeAdjustParam() {
		return boxTypeAdjustParam;
	}

	public double getAllianceStoreAdjustParam() {
		return allianceStoreAdjustParam;
	}

	public double getMonsterTypeAdjustParam() {
		return monsterTypeAdjustParam;
	}

	public double getNewMonsterAdjustParam() {
		return newMonsterAdjustParam;
	}

	/**
	 * 首次攻击2/3级野怪，行军时间特殊处理。
	 * @param targetId
	 * @return
	 */
	public boolean isMonsterLevelId(int level) {
		return monsterLevelSpeed.keySet().contains(level);
	}

	public int getFirstMonsterMarchTime(int level) {
		return monsterLevelSpeed.get(level);
	}

	public float getDistanceSubtractionParam() {
		return distanceSubtractionParam;
	}

	/**
	 * 根据资源类型获取资源的负重
	 * 
	 * @param resType
	 *            资源类型
	 * @return 负重值
	 */
	public int getResWeightByType(int resType) {
		int weight = 0;
		switch (resType) {
		case PlayerAttr.GOLD_VALUE:
			weight = res1001Weight;
			break;
		case PlayerAttr.GOLDORE_UNSAFE_VALUE:
			weight = res1007Weight;
			break;
		case PlayerAttr.GOLDORE_VALUE:
			weight = res1107Weight;
			break;
		case PlayerAttr.OIL_UNSAFE_VALUE:
			weight = res1008Weight;
			break;
		case PlayerAttr.OIL_VALUE:
			weight = res1108Weight;
			break;
		case PlayerAttr.STEEL_UNSAFE_VALUE:
			weight = res1009Weight;
			break;
		case PlayerAttr.STEEL_VALUE:
			weight = res1109Weight;
			break;
		case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			weight = res1010Weight;
			break;
		case PlayerAttr.TOMBARTHITE_VALUE:
			weight = res1110Weight;
			break;
		default:
			break;
		}
		return Math.max(1, weight);
	}

	// 默认行军速度（行进坐标/秒）（用前除1000）
	public int getWorldMarchBaseVelocity() {
		return worldMarchBaseVelocity;
	}

	/**
	 * 检查连续打怪次数是否合法
	 * 
	 * @param attackTimes
	 * @return
	 */
	public boolean checkFightMonsterAttackTime(int attackTimes) {
		if (atkEnemyContinuityNumsArray == null) {
			return false;
		}
		for (int i = 0; i < atkEnemyContinuityNumsArray.length; i++) {
			if (attackTimes == atkEnemyContinuityNumsArray[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 检查集结时间是否合法
	 * 
	 * @param waitTime
	 * @return
	 */
	public boolean checkMassWaitTime(int waitTime) {
		if (waitTimeAry == null) {
			return false;
		}
		for (int i = 0; i < waitTimeAry.length; i++) {
			if (waitTime == waitTimeAry[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 检查高达集结时间是否合法
	 * 
	 * @param waitTime
	 * @return
	 */
	public boolean checkGundamMassWaitTime(int waitTime) {
		if (gundamWaitTimeAry == null) {
			return false;
		}
		for (int i = 0; i < gundamWaitTimeAry.length; i++) {
			if (waitTime == gundamWaitTimeAry[i]) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 检查年兽集结时间是否合法
	 * 
	 * @param waitTime
	 * @return
	 */
	public boolean checkNianMassWaitTime(int waitTime) {
		if (nianWaitTimeAry == null) {
			return false;
		}
		for (int i = 0; i < nianWaitTimeAry.length; i++) {
			if (waitTime == nianWaitTimeAry[i]) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 检查野怪集结时间是否合法
	 */
	public boolean checkMonsterMassWaitTime(int waitTime) {
		if (monsterWaitTimeAry == null) {
			return false;
		}
		for (int i = 0; i < monsterWaitTimeAry.length; i++) {
			if (waitTime == monsterWaitTimeAry[i]) {
				return true;
			}
		}
		return false;
	}
	
	public boolean checkSpaceMechaMassWaitTime(int waitTime) {
		if (spaceMechaWaitTimeAry == null) {
			return false;
		}
		for (int i = 0; i < spaceMechaWaitTimeAry.length; i++) {
			if (waitTime == spaceMechaWaitTimeAry[i]) {
				return true;
			}
		}
		return false;
	}
	
	
	public int[] getMonsterWaitTimeAry() {
		return monsterWaitTimeAry;
	}

	@Override
	protected boolean checkValid() {
		if (atkEnemyContinuityNumsArray == null || atkEnemyContinuityNumsArray.length == 0) {
			return false;
		}
		if (waitTimeAry == null || waitTimeAry.length == 0) {
			return false;
		}
		if (tempAssemblyQueueUpper != tempAssemblyQueueCostArray.length) {
			return false;
		}
		
		if (!HawkOSOperator.isEmptyString(newMonsterLevel) && !HawkOSOperator.isEmptyString(newMonsterMarchTime)) {
			String[] newMonsterLevels = newMonsterLevel.split("_");
			String[] newMonsterMarchTimes = newMonsterMarchTime.split("_");
			if (newMonsterLevels.length != newMonsterMarchTimes.length) {
				return false;
			}
		}
		return super.checkValid();
	}

	@Override
	protected boolean assemble() {
		// 组装杀怪扫荡次数表
		if (atkEnemyContinuityNums != null && !"".equals(atkEnemyContinuityNums)) {
			String[] strs = atkEnemyContinuityNums.split(",");
			atkEnemyContinuityNumsArray = new int[strs.length];
			for (int i = 0; i < strs.length; i++) {
				atkEnemyContinuityNumsArray[i] = Integer.parseInt(strs[i]);
			}
		}

		// 升级消耗
		if (investigationMarchCost != null && !"".equals(investigationMarchCost)) {
			investigationCostItems = new ArrayList<ItemInfo>();
			String[] cost = investigationMarchCost.split(",");
			for (String info : cost) {
				ItemInfo item = new ItemInfo();
				item.init(info);
				investigationCostItems.add(item);
			}
		}

		// 集结时间数组
		if (!HawkOSOperator.isEmptyString(worldGatherTime)) {
			String[] tmp = worldGatherTime.split("_");
			waitTimeAry = new int[tmp.length];
			for (int i = 0; i < tmp.length; i++) {
				waitTimeAry[i] = Integer.parseInt(tmp[i]);
			}
		}
		
		// 集结时间数组
		if (!HawkOSOperator.isEmptyString(monsterMass)) {
			String[] tmp = monsterMass.split("_");
			monsterWaitTimeAry = new int[tmp.length];
			for (int i = 0; i < tmp.length; i++) {
				monsterWaitTimeAry[i] = Integer.parseInt(tmp[i]);
			}
		}

		// 集结时间数组
		if (!HawkOSOperator.isEmptyString(gundamMassTime)) {
			String[] tmp = gundamMassTime.split("_");
			int[] gundamWaitTimeAry = new int[tmp.length];
			for (int i = 0; i < tmp.length; i++) {
				gundamWaitTimeAry[i] = Integer.parseInt(tmp[i]);
			}
			this.gundamWaitTimeAry = gundamWaitTimeAry;
		}
		
		// 集结时间数组
		if (!HawkOSOperator.isEmptyString(nianMassTime)) {
			String[] tmp = nianMassTime.split("_");
			int[] gundamWaitTimeAry = new int[tmp.length];
			for (int i = 0; i < tmp.length; i++) {
				gundamWaitTimeAry[i] = Integer.parseInt(tmp[i]);
			}
			this.nianWaitTimeAry = gundamWaitTimeAry;
		}
		
		if (!HawkOSOperator.isEmptyString(spmechaMassTime)) {
			String[] tmp = spmechaMassTime.split("_");
			int[] spaceMechaWaitTimeAry = new int[tmp.length];
			for (int i = 0; i < tmp.length; i++) {
				spaceMechaWaitTimeAry[i] = Integer.parseInt(tmp[i]);
			}
			this.spaceMechaWaitTimeAry = spaceMechaWaitTimeAry;
		}
		
		if (scoutEffectID != null && !"".equals(scoutEffectID)) {
			String[] strs = scoutEffectID.split("_");
			scoutEffectIDMin_Max[0] = Integer.parseInt(strs[0]);
			scoutEffectIDMin_Max[1] = Integer.parseInt(strs[1]);
		}

		if (attackReportEffectID != null && !"".equals(attackReportEffectID)) {
			String[] strs = attackReportEffectID.split("_");
			attackReportEffectIDMin_Max[0] = Integer.parseInt(strs[0]);
			attackReportEffectIDMin_Max[1] = Integer.parseInt(strs[1]);
		}

		// 加载购买集结者格子的消耗钻石数组
		if (!HawkOSOperator.isEmptyString(tempAssemblyQueueCost)) {
			String[] strs = tempAssemblyQueueCost.split("_");
			tempAssemblyQueueCostArray = new int[strs.length];
			for (int i = 0; i < strs.length; i++) {
				tempAssemblyQueueCostArray[i] = Integer.parseInt(strs[i]);
			}
		}
		
		// 行军上需要显示的作用号
		if (!HawkOSOperator.isEmptyString(marchShowEff)) {
			String[] strs = marchShowEff.split("_");
			marchShowEffArray = new int[strs.length];
			for (int i = 0; i < strs.length; i++) {
				marchShowEffArray[i] = Integer.parseInt(strs[i]);
			}
		}
		
		monsterLevelSpeed = new HashMap<Integer, Integer>();
		if (!HawkOSOperator.isEmptyString(newMonsterLevel) && !HawkOSOperator.isEmptyString(newMonsterMarchTime)) {
			String[] newMonsterLevels = newMonsterLevel.split("_");
			String[] newMonsterMarchTimes = newMonsterMarchTime.split("_");
			for (int i = 0; i < newMonsterLevels.length; i++) {
				monsterLevelSpeed.put(Integer.valueOf(newMonsterLevels[i]), Integer.valueOf(newMonsterMarchTimes[i]));
			}
		}
		
		List<Integer> energyDetectorList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(energyDetector)) {
			String[] split = energyDetector.split(",");
			for (String energy : split) {
				energyDetectorList.add(Integer.valueOf(energy));
			}
		}
		this.energyDetectorList = energyDetectorList;
		
		List<Integer> honnerDetectorList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(honnerDetector)) {
			String[] split = honnerDetector.split(",");
			for (String honner : split) {
				honnerDetectorList.add(Integer.valueOf(honner));
			}
		}
		this.honnerDetectorList = honnerDetectorList;
		
		if (!HawkOSOperator.isEmptyString(christmasMassTime)) {
			christmasMassTimeSet = Collections.synchronizedSet(SerializeHelper.stringToSet(Integer.class, 
					christmasMassTime, SerializeHelper.ATTRIBUTE_SPLIT));
		} else {
			christmasMassTimeSet = Collections.synchronizedSet(new HashSet<>());
		}
		
		Set<Integer> warFlagMassSet = new HashSet<>();
		if (!HawkOSOperator.isEmptyString(warFlagGatherTime)) {
			String[] split = warFlagGatherTime.split("_");
			for (int i = 0; i < split.length; i++) {
				warFlagMassSet.add(Integer.valueOf(split[i]));
			}
		}
		this.warFlagMassSet = warFlagMassSet;
		
		return super.assemble();
	}

	public int getGeneralBackMarchSpeed() {
		return generalBackMarchSpeed;
	}

	/**
	 * 获取行军上需要显示的作用号数组
	 * @return
	 */
	public int[] getMarchShowEffArray() {
		return marchShowEffArray;
	}

	public int getSpyMarkDisappearTime() {
		return spyMarkDisappearTime;
	}

	/**
	 * 资源援助次数
	 * @return
	 */
	public int getAssistanceResTimes() {
		return assistanceResTimes;
	}

	/**
	 * 被资源援助次数
	 * @return
	 */
	public int getBeAssistanceResTimes() {
		return beAssistanceResTimes;
	}

	public int[] getGundamWaitTimeAry() {
		return gundamWaitTimeAry;
	}

	public int getGundamAtkLimit() {
		return gundamAtkLimit;
	}

	public double getBossTypeAdjustParam() {
		return bossTypeAdjustParam;
	}

	public int getGundamOnceKillLimit() {
		return gundamOnceKillLimit;
	}

	public int getMassGundamOnceKillLimit() {
		return massGundamOnceKillLimit;
	}

	/**
	 * 是否能量道具(打野掉落)
	 * @return
	 */
	public boolean isEnergyDetectorTool(int toolId) {
		return energyDetectorList.contains(toolId);
	}

	public boolean isHonnerDetectorTool(int toolId) {
		return honnerDetectorList.contains(toolId);
	}

	public int getHonnerDetectorMultipleEffect() {
		return honnerDetectorMultipleEffect;
	}

	/**
	 * 打野能量道具翻倍系数
	 * @return
	 */
	public int getEnergyDetectorMultipleEffect() {
		return energyDetectorMultipleEffect;
	}

	public int[] getNianWaitTimeAry() {
		return nianWaitTimeAry;
	}
	
	public int[] getSpaceMechaWaitTimeAry() {
		return spaceMechaWaitTimeAry;
	}

	public int getNianAtkLimit() {
		return nianAtkLimit;
	}

	public int getNianOnceKillLimit() {
		return nianOnceKillLimit;
	}

	public int getMassNianOnceKillLimit() {
		return massNianOnceKillLimit;
	}

	public double getNianTypeAdjustParam() {
		return nianTypeAdjustParam;
	}

	public boolean isSpecialResLevel(int level) {
		List<Integer> array = new ArrayList<>();
		String[] split = specialResLevel.split(",");
		for (int i = 0; i < split.length; i++) {
			array.add(Integer.valueOf(split[i]));
		}
		return array.contains(level);
	}

	public int getSpecialResBuffVal() {
		return specialResBuffVal;
	}

	public long getInviteMassCD() {
		return inviteMassCD * 1000L;
	}

	public double getNianBoxAdjustParam() {
		return nianBoxAdjustParam;
	}

	public int getPylonVitCost() {
		return pylonVitCost;
	}

	public double getChristmasTypeAdjustParam() {
		return christmasTypeAdjustParam;
	}

	public int getChristmasAtkLimit() {
		return christmasAtkLimit;
	}

	public int getChristmasDeadlinessAtkLimit() {
		return christmasDeadlinessAtkLimit;
	}

	public int getChristmasMassDeadlinessAtkLimit() {
		return christmasMassDeadlinessAtkLimit;
	}

	public int getChristmasBoxReceiveLimit() {
		return christmasBoxReceiveLimit;
	}

	public int getChristmasDeadlinessAtkMin() {
		return christmasDeadlinessAtkMin;
	}

	public int getChristmasMassDeadlinessAtkMin() {
		return christmasMassDeadlinessAtkMin;
	}

	public Set<Integer> getChristmasMassTimeSet() {
		return christmasMassTimeSet;
	}

	public double getEspionageAdjustParam() {
		return espionageAdjustParam;
	}

	public String getSnowballAtkCost() {
		return snowballAtkCost;
	}

	public String getEspionageCost() {
		return espionageCost;
	}

	public int getSnowballAdjustParam() {
		return snowballAdjustParam;
	}

	public Set<Integer> getWarFlagMassSet() {
		return warFlagMassSet;
	}
	
	public int getMonsterBossAdjustParam() {
		return monsterBossAdjustParam;
	}
	
	public int getFoggyAdjustParam() {
		return foggyAdjustParam;
	}
}
