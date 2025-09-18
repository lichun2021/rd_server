package com.hawk.game.config;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuple4;
import org.hawk.tuple.HawkTuples;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.cfgElement.RefreshAreaObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.protocol.President.PresidentResourceInfoSyn;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.object.Point;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 世界地图常量配置
 *
 * @author julia
 *
 */
@HawkConfigManager.KVResource(file = "xml/world_map_const.xml")
public class WorldMapConstProperty extends HawkConfigBase {
	/**
	 * 设置一个单例对象, 以便访问
	 */
	private static WorldMapConstProperty instance = null;

	public static WorldMapConstProperty getInstance() {
		return instance;
	}

	// 新手阶段等级（城市等级）（出生、清除道具、跨服，无盟被集结）
	protected final int stepCityLevel1;

	// 玩家城市等级≥多少，可以生产和采集高级资源，可采集钢铁等级_可采集合金等级
	protected final String stepCityLevel2;

	// 影响服务器进程的玩家数量（世界资源点等级提升，世界开刷钢铁矿）
	protected final int affectPlayersNum;

	// 世界地图最大坐标(x_y)
	protected final String worldMaxXy;

	// 世界地图中心点坐标(x_y)
	protected final String worldCentreXy;

	// 王座区域，服务器初始化加入阻挡点列表
	protected final String worldBanXy;

	// 黑土地区域，世界核心区域
	protected final String worldCoreXy;

	// 城点最小间隔坐标(x,y) 5_5
	protected final String worldPlayerMinRange;

	// 玩家初始坐标点，随机迁城坐标点，被打飞的坐标点这三种情况生成的点不能在此危险区域
	protected final String worldCityBanRange;

	// 清除城点条件（city＜6|city≥6）（天）
	protected final String worldCityCleanTime;

	// 资源带宽度（从里到外，6-1，每个资源带配左上和右下坐标）
	// 510_1020,694_1382|420_840,784_1562|330_660,874_1742|240_480,964_1922|150_300,1054_2102|0_0,1203_2402
	protected final String worldResLevelRange;

	// 资源刷新区块边长，坐标
	protected final String worldResRefreshRange;

	protected final String worldMonsterRefreshRange;
	
	// 世界资源刷新比例权重[资源参数_权重,资源参数_权重,资源参数_权重]= 1007_2,1008_2,1009_1,1010_1
	protected final String worldResRefreshRatio;

	// 刷新资源点总数[金矿、石油、钢铁、合金](资源数/区域内格子总数)（用前除以1000）
	protected final int worldResRefreshMax;

	// 开服新手期(秒)
	protected final int newPlayerTime;

	// 刷新野怪总数(野怪数/区域内格子总数)（用前除以1000）
	protected final int worldEnemyRefreshMax;
	// 刷新精英野怪总数(野怪数/区域内格子总数)（用前除以1000）
	protected final int worldSuperEnemyRefreshMax;
	// 精英怪物刷新时间段
	protected final String worldSuperEnemyRefreshTime;

	protected final int worldYuriRefreshMax;
	
	protected final int worldfoggyFortressRefreshMax;

	// 收藏夹最大数量
	protected final int favoriteMax;

	//
	protected final int mcvBrokenTime;

	// 世界地图搜索半径
	protected final String searchRadius;

	// 是否初始化联盟领地
	protected final boolean manorSwitch;

	// # 玩家城点刷新区域
	protected final String birthArea;

	// # 区域按规则出生人口数量上限
	protected final int areaPeopleLimit;

	// # 新手城点站位距离
	protected final int peopleDistance;

	// # 新手城店距离资源点距离
	protected final int peopleResourceDistance;

	// # 同时刷新新手城点的区域数量
	protected final int birthAreaNumber;

	// # 新版野怪猎杀加成持续时间s
	protected final int worldNewMonsterAtkBuffContiTime;
	
	// # 新版野怪多次攻击的次数
	protected final int newMonsterAttackNumber;
	
	// # 哪些基地外显特效需要播放（如飘加号，飘护盾）
	private final String baseShow;

	// # 玩家首次攻击前三只野怪的时间（单位秒）
	protected final String newMonsterSpecialTime;

	// 特殊区域id
	protected final String specialAreaIds;
	
	// # 伤兵转换率(万分比)
	private final String woundedSoldRate;

	// # 初始装扮
	private final String initWorldDressShow;

	// 合服初始年K
	private final int mergeNianInitK;
	
	// 世界地图最大坐标
	private int worldMaxX = 0;
	private int worldMaxY = 0;

	// 世界地图中心点坐标
	private int WorldCenterX = 0;
	private int WorldCenterY = 0;

	// 世界地图资源块长和宽
	private int worldResRefreshWidth = 0;
	private int worldResRefreshHeight = 0;

	private List<String[]> superEnemyRefreshTimes = new ArrayList<String[]>();

	// 世界地图上新手玩家以及随机迁城不能去的坐标区域,王座区域
	private int[] kingPalaceRange = new int[2];

	// 世界地图上新手玩家以及随机迁城不能去的坐标区域
	private int[] capitalCoreRange = new int[2];

	// 世界地图上随机，出生，打飞不能出现的区域
	private int[] cityRandomRange = new int[2];

	// 城点清除时间数据
	private int[] cityCleanTime = new int[2];

	// 特殊区域id
	private List<Integer> specialAreaIdList;
	
	// 世界刷资源类型概率
	private List<RefreshAreaObject> worldResRefresh = null;

	// 收集野外资源等级
	private int canCollectSteelLevel = 0;
	private int canCollectTombarthiteLevel = 0;

	// 可掠夺的资源大本等级限制
	private int[] RES_LV = null;

	// 世界地图上的搜索功能半径
	private int[] worldSearchRadiusArray = null;

	// 资源带坐标组
	private int[][] worldResAreas = new int[6][4];

	// 野怪刷新
	private final int monsterRefreshTime;
	
	// 资源刷新	
	private final int resourceRefreshTime;
	
	// 据点刷新
	private final int strongpointRefreshTime;

	private final int nianBoxReceiveLimit;
	
	// 随机刷新权重列表
	static List<RefreshAreaObject> randomRefreshRes = new ArrayList<RefreshAreaObject>();

	// 城点刷新区域
	private static List<Long> birthAreas = new ArrayList<>();

	// 首都黑土地区域四个顶点
	private Point[] capitalPoints = new Point[4];

	// 打野怪实际伤兵转化率随机区间(万分比)
	private int[] woundedConvertRate = new int[2];

	// 尤里生命周期
	protected final String disappearTime;

	private int[] disappearTimeVal = new int[2];

	// 探索时间，单位s
	protected final String exploreTime;

	private int[] exploreTimeArr = new int[4];

	// 单次tick奖励权重（经验_资源_道具）
	protected final String yuriRevengeWeight;
	
	private List<Integer> yuriRevengeWeightList;

	protected final int yuriSearchPeriod;

	/** 一个地图块上尤里的最大数量*/
	protected final int yuriRefreshMax;
	/** 一个地图块上暴怒的尤里最大数量*/
	protected final int angryYuriRefreshMax;
	/** 尤里刷新时间间隔（单位：秒） */
	protected final int yuriRefreshCd;
	/** 暴怒的尤里刷新时间间隔（单位：秒） */
	protected final int angryYuriRefreshCd;
	//# 击杀尤里积分
	protected final int yuriKillPoints;
	//# 击杀暴怒的尤里积分
	protected final int angryYuriKillPoints;
	//# 领取随机宝箱时间间隔（单位：秒）
	protected final int randomBoxGetCd;
	//# 领取随机宝箱的行军速度（行进坐标/秒）（标准为5秒走1坐标）（用前除1000）
	protected final int randomBoxMarchSpeed;
	//# 刷新尤里叛军总数(野怪数/区域内格子总数)（用前除以1000）
	protected final int worldYuriSoldierRefreshMax;
	//# 刷新尤里总数(野怪数/区域内格子总数)（用前除以1000）
	protected final int worldNormalYuriRefreshMax;
	//# 刷新暴怒的尤里总数(野怪数/区域内格子总数)（用前除以1000）
	protected final int worldAngryYuriRefreshMax;
	//# 刷新随机宝箱总数(野怪数/区域内格子总数)（用前除以1000）
	protected final int worldBoxRefreshMax;
	protected final int actWildMonsterLossRate;
	//# 玩家战胜尤里比例
	protected final int yuriSucceedRate;
	//# 尤里失败n次结束活动
	protected final int yuriFailTimes;
	//黑土地开服x天后开始刷高级田
	protected final long worldBlackResRefreshTime;
	//刷新据点的数量系数
	protected final int worldStrongpointRefreshMax;
	//保底怪物的刷新系数
	protected final int worldEnemyLevelMinLimitCof;
	//保底迷雾要塞的刷新系数
	protected final int worldEnemyFoggyLevelMinLimitCof;
	//# 影响资源点刷新人数
	protected final int affectRefreshPlayersNum;
	// # 新版野怪刷新时间
	protected final String newMonsterRefreshTime;
	// # 新版野怪跨天时间
	protected final int newMonsterCrossDayTime;
	// # 老版野怪刷新时间	
	protected final String oldMonsterRefreshTime;
	// # 刷新新版野怪总数(野怪数/区域内格子总数)（用前除以1000）
	protected final int worldNewMonsterRefreshMax;
	// # x秒刷新完地图新版野怪
	protected final int worldNewMonsterRefreshContinueTime;

	// # 黑土地区域资源点刷新修正概率，万分比
	protected final int capitalResFixRate;

	// # 黑土地区域野怪点刷新修正概率，万分比
	protected final int capitalMonsterFixRate;

	// # 黑土地区域新野怪点刷新修正概率，万分比
	protected final int capitalNewMonsterFixRate;

	// # 黑土地区域迷雾要塞点刷新修正概率，万分比
	protected final int capitalFoggyFortressFixRate;

	// 联盟迁城偏移量
	protected final String allianceMoveOffset;
	
	private Map<Integer, Integer> initWorldDressShowMap;
	
	// # 玩家首次攻击前三只野怪的时间（单位秒）
	private ArrayList<Integer> newMonsterSpecialTimeArr;

	// 区域更新时间点
	private final int areaUpdateClock;
	
	// 区域更新刷新时间
	private final int areaUpdateRefreshTime;
	
	//# 机甲刷新时间
	private final String gundamRefreshTime;

	//# 机甲生成坐标
	private final String gundamRefreshPos;

	//# 机甲刷新区域半径(以坐标为中心，半径内区域随机生成点)
	private final int gundamRefreshAreaRadius;

	//# 年兽刷新时间
	private final String nianRefreshTime;

	//# 年兽生成坐标
	private final String nianRefreshPos;

	//# 年兽刷新区域半径(以坐标为中心，半径内区域随机生成点)
	private final int nianRefreshAreaRadius;
	
	// 年兽血量自适应参数
	private final String nianParamA;
	private final int nianParamB;
	private final String nianParamC;
	private final String nianParamX;
	private final int nianParamY;
	private final String nianParamZ;
	private final int nianParamH;
	private final int nianParamG;
	private final int nianParamK;
	private final String nianParamEffectList;
	private final int nianRemoveTime;
	private final String nianRemoveNoticeTime;
	
	// 保护罩同步时间(min)
	private final int pointProtectNotify;
	
	// 年兽宝箱刷新区域半径
	private final int nianBoxRefRadius;
	
	// 能量塔刷新区域
	private final String pylonRefreshArea;
	
	// 能量塔刷新数量
	private final int pylonRefreshCount;
	
	// 能量塔刷新周期(s)
	private final String pylonRefreshPeroid;
	
	/**
	 * 新版野怪刷新时间数组
	 */
	private int[] newMonsterRefreshTimeArr;
	/**
	 * 老版野怪刷新时间数组
	 */
	private int[] oldMonsterRefreshTimeArr;
	
	private int[] allianceMoveOffsetArr;
	/**
	 * 最小搜索半径
	 */
	private final int minStoryMonsterSerachRadius;
	/**
	 * 最大搜索半径
	 */
	private final int maxStoryMonsterSearchRadius;
	/**
	 * 停服后续罩时间
	 */
	private final int protectCoverAddTime;
	/**
	 * 叛军BOSS伤害奖励修正系数
	 */
	private final int yuriBossDamageRewardParam;
	/**
	 * 圣诞boss 刷新次数限制.
	 */
	private final int christmasRefreshLimit;
	/**
	 * 圣诞boss 刷新时间。
	 */
	private final String  christmasRefreshTime;
	/**
	 * 圣诞boss 刷新坐标.
	 */
	private final String  christmasRefreshPos;
	/**
	 * 圣诞boss刷新半径.
	 */
	private final int christmasRefreshAreaRadius;
	/**
	 * 圣诞boss删除时间
	 */
	private final int christmasRemoveTime;
	/**
	 * 圣诞boss删除提示时间.
	 */
	private final String christmasRemoveNoticeTime;
	
	/**
	 * 雪球攻击距离限制
	 */
	private final int snowballAtkDistance;
	
	/**
	 * 雪球攻击CD限制
	 */
	private final int snowballAtkCd;
	
	/**
	 * 圣诞boss 删除提示时间。
	 */
	private List<Integer> christmasRemoveNoticeTimeList;
	
	/**
	 * 圣诞boss刷新区域坐标
	 * {@link #christmasRefreshPos}
	 */
	private List<int[]> christmasRefreshPosList;
	/**
	 * 圣诞刷新时间
	 * {@link #christmasRefreshTime}
	 */
	private  List<Integer> christmasRefreshTimeList;	
	/**
	 * 机甲刷新时间
	 */
	private List<Integer> goundamRefreshTimeArr;
	
	/**
	 * 机甲生成坐标
	 */
	private List<int []> gundamRefreshPosArr;
	
	/**
	 * 年兽刷新时间
	 */
	private List<Integer> nianRefreshTimeArr;
	
	/**
	 * 年兽生成坐标
	 */
	private List<int []> nianRefreshPosArr;

	private List<Integer> nianParamAArray;
	private List<Integer> nianParamCArray;
	private List<Integer> nianParamXArray;
	private List<Integer> nianParamZArray;
	
	private List<Integer> nianRmNoticeTimeArray;
	
	private int[] pylonAreaBegin = new int[2];
	
	private int[] pylonAreaEnd = new int[2];

	//泰伯利亚名人堂刷新点
	private final String tblyFameHallPoint;

	private final float difficultyRatio;
	private final int difficultyCorrection;
	
	private final String guildAutoMoveArea;
	private final int guildAutoMoveCount;
	private final int guildAutoMoveLeaderRadius;
	private final int guildAutoMoveOpenTimeLimt;
	private final int guildAutoMoveLevelLimt;
	
	public WorldMapConstProperty() {
		instance = this;

		snowballAtkDistance = 100;
		snowballAtkCd = 0;
		newPlayerTime = 0;
		stepCityLevel1 = 0;
		stepCityLevel2 = "";
		affectPlayersNum = 0;
		worldMaxXy = "";
		worldCentreXy = "";
		worldResLevelRange = "";
		worldResRefreshRange = "";
		worldResRefreshRatio = "";
		worldResRefreshMax = 0;
		worldPlayerMinRange = "";
		worldCityBanRange = "";
		worldCityCleanTime = "";
		favoriteMax = 0;
		worldBanXy = "";
		worldCoreXy = "";
		worldEnemyRefreshMax = 0;
		worldSuperEnemyRefreshMax = 0;
		mcvBrokenTime = 0;
		searchRadius = "";
		worldSuperEnemyRefreshTime = "";
		manorSwitch = false;
		birthArea = "";
		areaPeopleLimit = 0;
		peopleDistance = 0;
		peopleResourceDistance = 0;
		birthAreaNumber = 0;
		baseShow = "";
		woundedSoldRate = "";
		worldYuriRefreshMax = 0;
		disappearTime = "";
		exploreTime = "";
		yuriRevengeWeight = "";
		yuriSearchPeriod = 0;
		yuriRefreshMax = 0;
		angryYuriRefreshMax = 0;
		yuriRefreshCd = 0;
		angryYuriRefreshCd = 0;
		yuriKillPoints = 0;
		angryYuriKillPoints = 0;
		randomBoxGetCd = 0;
		randomBoxMarchSpeed = 0;
		worldYuriSoldierRefreshMax = 0;
		worldNormalYuriRefreshMax = 0;
		worldAngryYuriRefreshMax = 0;
		worldBoxRefreshMax = 0;
		actWildMonsterLossRate = 0;
		yuriSucceedRate = 0;
		yuriFailTimes = 0;
		worldBlackResRefreshTime = 0;
		worldMonsterRefreshRange = "";
		worldStrongpointRefreshMax = 0;
		worldEnemyLevelMinLimitCof = 0;
		initWorldDressShow = "";
		affectRefreshPlayersNum = 0;
		newMonsterRefreshTime = "0";
		newMonsterCrossDayTime = 0;
		oldMonsterRefreshTime = "0";
		worldNewMonsterRefreshMax = 0;
		worldNewMonsterRefreshContinueTime = 900;
		worldfoggyFortressRefreshMax = 0;
		worldNewMonsterAtkBuffContiTime = 0;
		newMonsterSpecialTime = "";
		newMonsterAttackNumber = 1;
		worldEnemyFoggyLevelMinLimitCof = 0;
		capitalResFixRate = 3000;
		capitalMonsterFixRate = 3000;
		capitalNewMonsterFixRate = 3000;
		capitalFoggyFortressFixRate = 3000;
		areaUpdateClock = 4;
		areaUpdateRefreshTime = 60 * 60;
		specialAreaIds = "";
		monsterRefreshTime = 25 * 60;
		allianceMoveOffset = "0,2";
		this.minStoryMonsterSerachRadius = 50;
		this.maxStoryMonsterSearchRadius = 100;
		resourceRefreshTime = 3 * 60 * 60;
		strongpointRefreshTime = 1800;
		protectCoverAddTime = 0;
		gundamRefreshTime = "";
		gundamRefreshPos = "";
		gundamRefreshAreaRadius = 1;
		nianRefreshTime = "";
		nianRefreshPos = "";
		nianRefreshAreaRadius = 1;
		nianParamA = "";
		nianParamB = 0;
		nianParamC = "";
		nianParamX = "";
		nianParamY = 0;
		nianParamZ = "";
		nianParamH = 0;
		nianParamG = 0;
		yuriBossDamageRewardParam = 10000;
		mergeNianInitK = 50;
		pointProtectNotify = 11;
		nianRemoveTime = 100000;
		nianRemoveNoticeTime = "";
		
		nianBoxRefRadius = 50;
		nianBoxReceiveLimit = 10;
		
		pylonRefreshArea = "315_630,445_890";
		pylonRefreshCount = 200;
		
		
		this.christmasRefreshAreaRadius = 30;
		this.christmasRefreshLimit = 3;
		this.christmasRefreshTime = "";
		this.christmasRefreshPos = "";
		this.christmasRemoveTime = 7200;
		this.christmasRemoveNoticeTime = "";
		
		nianParamK = 100;
		nianParamEffectList = "";
		
		pylonRefreshPeroid = "";
		tblyFameHallPoint = "386_1403";
		difficultyRatio = 1;
		difficultyCorrection = 0;
		guildAutoMoveArea = "";
		guildAutoMoveCount = 0;
		guildAutoMoveLeaderRadius = 50;
		guildAutoMoveOpenTimeLimt = 0;
		guildAutoMoveLevelLimt = 0;
	}

	public int getStepCityLevel1() {
		return stepCityLevel1;
	}

	public int getCanCollectSteelLevel() {
		return canCollectSteelLevel;
	}

	public int getCanCollectTombarthiteLevel() {
		return canCollectTombarthiteLevel;
	}

	public int[] getResLv() {
		return RES_LV;
	}

	public int getWorldResRefreshWidth() {
		return worldResRefreshWidth;
	}

	public int getWorldResRefreshHeight() {
		return worldResRefreshHeight;
	}

	public int[] getKingPalaceRange() {
		return kingPalaceRange;
	}

	public int[] getCapitalCoreRange() {
		return capitalCoreRange;
	}

	public int getWorldMaxX() {
		return worldMaxX;
	}

	public int getWorldMaxY() {
		return worldMaxY;
	}

	public int getWorldCenterX() {
		return WorldCenterX;
	}

	public int getWorldCenterY() {
		return WorldCenterY;
	}

	public int getAffectPlayersNum() {
		return affectPlayersNum;
	}

	public int getWorldResRefreshMax() {
		return worldResRefreshMax;
	}

	public int getFavoriteMax() {
		return favoriteMax;
	}

	public int[][] getWorldResAreas() {
		return worldResAreas;
	}

	public int[] getCityRandomRange() {
		return cityRandomRange;
	}

	public int[] getCityCleanTime() {
		return cityCleanTime;
	}

	public int getWorldEnemyRefreshMax() {
		return worldEnemyRefreshMax;
	}

	public int getWorldSuperEnemyRefreshMax() {
		return worldSuperEnemyRefreshMax;
	}

	public int getMcvBrokenTime() {
		return mcvBrokenTime;
	}

	public int[] getWorldSearchRadius() {
		return worldSearchRadiusArray;
	}

	public String getWorldSuperEnemyRefreshTime() {
		return worldSuperEnemyRefreshTime;
	}

	public List<String[]> getSuperEnemyRefreshTimes() {
		return superEnemyRefreshTimes;
	}

	public boolean getManorSwitch() {
		return manorSwitch;
	}

	public String getBirthArea() {
		return birthArea;
	}

	public List<Long> getBirthAreas() {
		return birthAreas;
	}

	public int getPeopleDistance() {
		return peopleDistance;
	}

	public int getPeopleResourceDistance() {
		return peopleResourceDistance;
	}

	public int getBirthAreaNumber() {
		return birthAreaNumber;
	}

	public Point[] getCapitalPoints() {
		return capitalPoints;
	}

	public void setCapitalPoints(Point[] capitalPoints) {
		this.capitalPoints = capitalPoints;
	}

	public int getAreaPeopleLimit() {
		return areaPeopleLimit;
	}

	public int getWorldYuriRefreshMax() {
		return worldYuriRefreshMax;
	}

	public String getYuriRevengeWeight() {
		return yuriRevengeWeight;
	}

	public int getYuriSearchPeriod() {
		return yuriSearchPeriod;
	}

	public int getWorldBoxRefreshMax() {
		return worldBoxRefreshMax;
	}

	public long getWorldBlackResRefreshTime() {
		return worldBlackResRefreshTime;
	}
	
	public int getWorldStrongpointRefreshMax() {
		return worldStrongpointRefreshMax;
	}

	public int getWorldEnemyLevelMinLimitCof() {
		return worldEnemyLevelMinLimitCof;
	}

	public int getAffectRefreshPlayersNum() {
		return affectRefreshPlayersNum;
	}

	public int[] getNewMonsterRefreshTimeArr() {
		return newMonsterRefreshTimeArr;
	}

	public int getNewMonsterCrossDayTime() {
		return newMonsterCrossDayTime;
	}

	public int getWorldNewMonsterRefreshMax() {
		return worldNewMonsterRefreshMax;
	}

	public int getWorldNewMonsterAtkBuffContiTime() {
		return worldNewMonsterAtkBuffContiTime;
	}

	// # 玩家首次攻击前三只野怪的时间（单位秒）
	public ArrayList<Integer> getNewMonsterSpecialTimeArr() {
		return newMonsterSpecialTimeArr;
	}

	public int getNewMonsterAttackNumber() {
		return newMonsterAttackNumber;
	}

	public int getCapitalResFixRate() {
		return capitalResFixRate;
	}

	public int getCapitalMonsterFixRate() {
		return capitalMonsterFixRate;
	}

	public int getCapitalNewMonsterFixRate() {
		return capitalNewMonsterFixRate;
	}

	public int getCapitalFoggyFortressFixRate() {
		return capitalFoggyFortressFixRate;
	}

	/**
	 * 获取哪些基地外显特效需要播放（如飘加号，飘护盾）
	 * 
	 * @return
	 */
	public List<Integer> getBaseShow() {
		List<Integer> ret = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(baseShow)) {
			return ret;
		}
		
		String[] baseShowArr = baseShow.split(",");
		for (String bs : baseShowArr) {
			ret.add(Integer.valueOf(bs));
		}
		return ret;
	}

	/**
	 * 获取野怪实际伤兵转化区间
	 * @return
	 */
	public int[] getWoundedConvertRate() {
		return woundedConvertRate;
	}

	/**
	 * 获取随机到的资源类型
	 * 
	 * @return
	 */
	public RefreshAreaObject getRefreshRes() {
		// 计算对应合金和钢铁的刷资源等级对应的人数
		int affectPlayersNum = WorldMapConstProperty.getInstance().getAffectRefreshPlayersNum();
		long tombarthitePlayerCount = GlobalData.getInstance().getMainBuildingCountByLevel(canCollectTombarthiteLevel);
		long steelPlayerCount = GlobalData.getInstance().getMainBuildingCountByLevel(canCollectSteelLevel);

		synchronized (randomRefreshRes) {
			int genTimes = 0;
			while (genTimes++ <= 1) {
				// 拉取合理的配置数据
				Iterator<RefreshAreaObject> it = randomRefreshRes.iterator();
				while (it.hasNext()) {
					RefreshAreaObject areaObj = it.next();
					if (WorldUtil.isTombarthiteRes(areaObj.getKey()) && tombarthitePlayerCount < affectPlayersNum) {
						continue;
					}

					if (WorldUtil.isSteelRes(areaObj.getKey()) && steelPlayerCount < affectPlayersNum) {
						continue;
					}

					it.remove();
					return areaObj;
				}

				// 生成失败, 重新生成
				randomRefreshRes.clear();
				
				List<RefreshAreaObject> worldResRefresh = getWorldResRefresh();
				for (int i = 0; i < worldResRefresh.size(); i++) {
					RefreshAreaObject areaObj = worldResRefresh.get(i);

					// 添加制定数量的可选值
					for (int cnt = 0; cnt < 100 * areaObj.getWeight(); cnt++) {
						randomRefreshRes.add(areaObj);
					}
				}
				HawkRand.randomOrder(randomRefreshRes);
			}
		}

		return null;
	}

	@Override
	protected boolean assemble() {

		// 刷超级怪的时间范围
		if (!HawkOSOperator.isEmptyString(worldSuperEnemyRefreshTime)) {
			String[] refreshTimes = worldSuperEnemyRefreshTime.split(",");
			for (String timeStr : refreshTimes) {
				String[] startEnd = timeStr.split("_");
				superEnemyRefreshTimes.add(startEnd);
			}
		}

		// 世界地图最大坐标
		if (worldMaxXy != null && !"".equals(worldMaxXy)) {
			String[] strs = worldMaxXy.split("_");
			worldMaxX = Integer.parseInt(strs[0]);
			worldMaxY = Integer.parseInt(strs[1]);
		}

		// 中心点坐标
		if (worldCentreXy != null && !"".equals(worldCentreXy)) {
			String[] strs = worldCentreXy.split("_");
			WorldCenterX = Integer.parseInt(strs[0]);
			WorldCenterY = Integer.parseInt(strs[1]);
		}

		// 分区长宽
		if (worldResRefreshRange != null && !"".equals(worldResRefreshRange)) {
			String[] strs = worldResRefreshRange.split("_");
			worldResRefreshWidth = Integer.parseInt(strs[0]);
			worldResRefreshHeight = Integer.parseInt(strs[1]);
		}

		// 玩家初始化城点不能去的区域
		if (worldCityBanRange != null && !"".equals(worldCityBanRange)) {
			String[] strs = worldCityBanRange.split("_");
			cityRandomRange[0] = Integer.parseInt(strs[0]);
			cityRandomRange[1] = Integer.parseInt(strs[1]);
		}

		if (worldCityCleanTime != null && !"".equals(worldCityCleanTime)) {
			String[] strs = worldCityCleanTime.split("_");
			cityCleanTime[0] = Integer.parseInt(strs[0]);
			cityCleanTime[1] = Integer.parseInt(strs[1]);
		}

		// 世界地图黑土地区域
		if (worldCoreXy != null && !"".equals(worldCoreXy)) {
			String[] strs = worldCoreXy.split("_");
			capitalCoreRange[0] = Integer.parseInt(strs[0]);
			capitalCoreRange[1] = Integer.parseInt(strs[1]);
		}

		// 世界禁区，即王座区域
		if (worldBanXy != null && !"".equals(worldBanXy)) {
			String[] strs = worldBanXy.split("_");
			kingPalaceRange[0] = Integer.parseInt(strs[0]);
			kingPalaceRange[1] = Integer.parseInt(strs[1]);
		}

		// 从里到外 6-1
		// 510_1020,694_1382;420_840,784_1562;330_660,874_1742;240_480,964_1922;150_300,1054_2102;0_0,1203_2402
		if (worldResLevelRange != null && !"".equals(worldResLevelRange)) {
			String[] areas = worldResLevelRange.split(";");
			for (int i = 0; i < areas.length; i++) {
				int[] temp = new int[4];
				String[] area = areas[i].split(",");
				int index = 0;
				for (int j = 0; j < area.length; j++) {
					String[] xy = area[j].split("_");
					temp[index] = Integer.parseInt(xy[0]);
					index++;
					temp[index] = Integer.parseInt(xy[1]);
					index++;
				}
				worldResAreas[i] = temp;
			}
		}

		// 资源刷新权重
		if (worldResRefreshRatio != null && !"".equals(worldResRefreshRatio)) {
			worldResRefresh = new ArrayList<RefreshAreaObject>();
			String[] strs = worldResRefreshRatio.split(",");
			for (String str : strs) {
				String[] temp = str.split("_");
				RefreshAreaObject obj = new RefreshAreaObject(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]));
				worldResRefresh.add(obj);
			}
		}

		// 收集野外资源
		if (stepCityLevel2 != null && !"".equals(stepCityLevel2)) {
			String[] strs = stepCityLevel2.split("_");
			canCollectSteelLevel = Integer.parseInt(strs[0]);
			canCollectTombarthiteLevel = Integer.parseInt(strs[1]);
		}

		// 加载世界地图搜索半径
		if (!HawkOSOperator.isEmptyString(searchRadius)) {
			String[] strs = searchRadius.split(",");
			worldSearchRadiusArray = new int[strs.length];
			for (int i = 0; i < strs.length; i++) {
				worldSearchRadiusArray[i] = Integer.parseInt(strs[i]);
			}
		}

		RES_LV = new int[] { 0, 0, canCollectTombarthiteLevel, canCollectSteelLevel };

		// 玩家城点区域
		birthAreas.clear();
		String[] birthAreaSplit = birthArea.split(";");
		for (int i = 0; i < birthAreaSplit.length; i++) {
			String[] birthArea = birthAreaSplit[i].split("_");
			int formPointId = GameUtil.combineXAndY(Integer.valueOf(birthArea[0]), Integer.valueOf(birthArea[1]));
			int toPointId = GameUtil.combineXAndY(Integer.valueOf(birthArea[2]), Integer.valueOf(birthArea[3]));
			birthAreas.add(GameUtil.combineFromAndTo(formPointId, toPointId));
		}

		// 黑土地四个顶点
		capitalPoints = new Point[4];
		capitalPoints[0] = (new Point(WorldCenterX, WorldCenterY - capitalCoreRange[1] < 0 ? 0 : WorldCenterY - capitalCoreRange[1]));// 上
		capitalPoints[1] = (new Point(WorldCenterX, WorldCenterY + capitalCoreRange[1] > worldMaxY ? worldMaxY : WorldCenterY + capitalCoreRange[1]));// 下
		capitalPoints[2] = (new Point(WorldCenterX - capitalCoreRange[0] < 0 ? 0 : WorldCenterX - capitalCoreRange[0], WorldCenterY));// 左
		capitalPoints[3] = (new Point(WorldCenterX + capitalCoreRange[0] > worldMaxX ? worldMaxX : WorldCenterX + capitalCoreRange[0], WorldCenterY));// 右

		woundedConvertRate = new int[2];
		// 玩家初始化城点不能去的区域
		if (!HawkOSOperator.isEmptyString(woundedSoldRate)) {
			String[] strs = woundedSoldRate.split("_");
			woundedConvertRate[0] = Integer.parseInt(strs[0]);
			woundedConvertRate[1] = Integer.parseInt(strs[1]);
		}

		// 尤里自刷新时间
		String[] appStr = disappearTime.split("_");
		for (int i = 0; i < appStr.length; i++) {
			disappearTimeVal[i] = Integer.parseInt(appStr[i]);
		}

		// 探索时间数组
		if (!HawkOSOperator.isEmptyString(exploreTime)) {
			String[] tmp = exploreTime.split("_");
			for (int i = 0; i < tmp.length; i++) {
				exploreTimeArr[i] = Integer.parseInt(tmp[i]);
			}
		}
		// 尤里探索奖励权重
		if (!HawkOSOperator.isEmptyString(yuriRevengeWeight)) {
			yuriRevengeWeightList = new ArrayList<Integer>(); 
			String[] tmp = yuriRevengeWeight.split("_");
			for (int i = 0; i < tmp.length; i++) {
				yuriRevengeWeightList.add(Integer.parseInt(tmp[i]));
			}
		}
		
		initWorldDressShowMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(initWorldDressShow)) {
			String[] dressShowSingle = initWorldDressShow.split(";");
			for (int i = 0; i < dressShowSingle.length; i++) {
				String[] dressShowSingleSplit = dressShowSingle[i].split("_");
				initWorldDressShowMap.put(Integer.valueOf(dressShowSingleSplit[0]), Integer.valueOf(dressShowSingleSplit[1]));
			}
		}

		// 新版野怪刷新时间
		if (!HawkOSOperator.isEmptyString(newMonsterRefreshTime)) {
			String[] newMonsterRefrestSplit = newMonsterRefreshTime.split("_");
			newMonsterRefreshTimeArr = new int[newMonsterRefrestSplit.length];
			for (int i = 0; i < newMonsterRefrestSplit.length; i++) {
				newMonsterRefreshTimeArr[i] = Integer.parseInt(newMonsterRefrestSplit[i]);
			}
		}
		
		// 新版野怪刷新时间
		if (!HawkOSOperator.isEmptyString(oldMonsterRefreshTime)) {
			String[] oldMonsterRefrestSplit = oldMonsterRefreshTime.split("_");
			oldMonsterRefreshTimeArr = new int[oldMonsterRefrestSplit.length];
			for (int i = 0; i < oldMonsterRefrestSplit.length; i++) {
				oldMonsterRefreshTimeArr[i] = Integer.parseInt(oldMonsterRefrestSplit[i]);
			}
		}
		
		newMonsterSpecialTimeArr = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(newMonsterSpecialTime)) {
			String[] newMonsterSpecialTimesplit = newMonsterSpecialTime.split("_");
			for (int i = 0; i < newMonsterSpecialTimesplit.length; i++) {
				newMonsterSpecialTimeArr.add(Integer.valueOf(newMonsterSpecialTimesplit[i]));
			}
		}
		
		List<Integer> specialAreaIdList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(specialAreaIds)) {
			String[] specialAreaIdArray = specialAreaIds.split(",");
			for (int i = 0; i < specialAreaIdArray.length; i++) {
				specialAreaIdList.add(Integer.valueOf(specialAreaIdArray[i]));
			}
		}
		this.specialAreaIdList = specialAreaIdList;
		
		if (!HawkOSOperator.isEmptyString(allianceMoveOffset)) {
			String[] allianceMoveOffsetSplit = allianceMoveOffset.split(",");
			allianceMoveOffsetArr = new int[allianceMoveOffsetSplit.length];
			for (int i = 0; i < allianceMoveOffsetSplit.length; i++) {
				allianceMoveOffsetArr[i] = Integer.parseInt(allianceMoveOffsetSplit[i]);
			}
		}
		
		List<Integer> goundamRefreshTimeArr = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(this.gundamRefreshTime)) {
			String[] goudaRefreshTimeSplit = this.gundamRefreshTime.split(",");
			for (int i = 0; i < goudaRefreshTimeSplit.length; i++) {
				goundamRefreshTimeArr.add(Integer.valueOf(goudaRefreshTimeSplit[i]));
			}
		}
		this.goundamRefreshTimeArr = goundamRefreshTimeArr;
		
		List<int []> gundamRefreshPosArr = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(this.gundamRefreshPos)) {
			String[] goudaRefreshPosSplit = this.gundamRefreshPos.split(";");
			for (int i = 0; i < goudaRefreshPosSplit.length; i++) {
				String[] posSplit = goudaRefreshPosSplit[i].split(",");
				int[] pos = new int[2];
				pos[0] = Integer.parseInt(posSplit[0]);
				pos[1] = Integer.parseInt(posSplit[1]);
				gundamRefreshPosArr.add(pos);
			}
		}
		this.gundamRefreshPosArr = gundamRefreshPosArr;
		
		List<Integer>  nianRefreshTimeArr = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(this.nianRefreshTime)) {
			String[] nianRefreshTimeSplit = this.nianRefreshTime.split(",");
			for (int i = 0; i < nianRefreshTimeSplit.length; i++) {
				nianRefreshTimeArr.add(Integer.valueOf(nianRefreshTimeSplit[i]));
			}
		}
		this.nianRefreshTimeArr = nianRefreshTimeArr;
		
		List<int []> nianRefreshPosArr = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(this.nianRefreshPos)) {
			String[] nianRefreshPosSplit = this.nianRefreshPos.split(";");
			for (int i = 0; i < nianRefreshPosSplit.length; i++) {
				String[] posSplit = nianRefreshPosSplit[i].split(",");
				int[] pos = new int[2];
				pos[0] = Integer.parseInt(posSplit[0]);
				pos[1] = Integer.parseInt(posSplit[1]);
				nianRefreshPosArr.add(pos);
			}
		}
		this.nianRefreshPosArr = nianRefreshPosArr;
		
		List<Integer> nianParamAArray = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(nianParamA)) {
			String[] paramArray = nianParamA.split("_");
			for (int i = 0; i < paramArray.length; i++) {
				nianParamAArray.add(Integer.valueOf(paramArray[i]));
			}
		}
		this.nianParamAArray = nianParamAArray;

		List<Integer> nianParamCArray = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(nianParamC)) {
			String[] paramArray = nianParamC.split("_");
			for (int i = 0; i < paramArray.length; i++) {
				nianParamCArray.add(Integer.valueOf(paramArray[i]));
			}
		}
		this.nianParamCArray = nianParamCArray;

		if (nianParamAArray.size() != nianParamCArray.size()) {
			throw new InvalidParameterException("nianParamAArray or nianParamCArray size error");
		}
		
		List<Integer> nianParamXArray = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(nianParamX)) {
			String[] paramArray = nianParamX.split("_");
			for (int i = 0; i < paramArray.length; i++) {
				nianParamXArray.add(Integer.valueOf(paramArray[i]));
			}
		}
		this.nianParamXArray = nianParamXArray;

		List<Integer> nianParamZArray = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(nianParamZ)) {
			String[] paramArray = nianParamZ.split("_");
			for (int i = 0; i < paramArray.length; i++) {
				nianParamZArray.add(Integer.valueOf(paramArray[i]));
			}
		}
		this.nianParamZArray = nianParamZArray;
		
		if (nianParamXArray.size() != nianParamZArray.size()) {
			throw new InvalidParameterException("nianParamXArray or nianParamZArray size error");
		}
		
		List<Integer> nianRmNoticeTimeArray = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(nianRemoveNoticeTime)) {
			String[] split = nianRemoveNoticeTime.split("_");
			for (int i = 0; i < split.length; i++) {
				nianRmNoticeTimeArray.add(Integer.valueOf(split[i]));
			}
		}
		this.nianRmNoticeTimeArray = nianRmNoticeTimeArray;
		
		// 能量塔刷新区域
		if (!HawkOSOperator.isEmptyString(pylonRefreshArea)) {
			
			String[] pos = pylonRefreshArea.split(",");
			
			String[] beginPos = pos[0].split("_");
			pylonAreaBegin[0] = Integer.parseInt(beginPos[0]);
			pylonAreaBegin[1] = Integer.parseInt(beginPos[1]);
					
			String[] endPos = pos[1].split("_");
			pylonAreaEnd[0] = Integer.parseInt(endPos[0]);
			pylonAreaEnd[1] = Integer.parseInt(endPos[1]);
			
		}
		
		List<int []> christmasRefreshPosArr = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(this.christmasRefreshPos)) {
			String[] christmasRefreshPosSplit = this.christmasRefreshPos.split(";");
			for (int i = 0; i < christmasRefreshPosSplit.length; i++) {
				String[] posSplit = christmasRefreshPosSplit[i].split(",");
				int[] pos = new int[2];
				pos[0] = Integer.parseInt(posSplit[0]);
				pos[1] = Integer.parseInt(posSplit[1]);
				christmasRefreshPosArr.add(pos);
			}
			
			christmasRefreshPosList = Collections.synchronizedList(christmasRefreshPosArr);
		} else {
			christmasRefreshPosList = Collections.synchronizedList(new ArrayList<>());
		}
		
		if (!HawkOSOperator.isEmptyString(christmasRefreshTime)) {
			christmasRefreshTimeList = Collections.synchronizedList(SerializeHelper.stringToList(Integer.class, christmasRefreshTime,
					SerializeHelper.BETWEEN_ITEMS));
		} else {
			christmasRefreshTimeList = Collections.synchronizedList(new ArrayList<>());
		}
		
		if (!HawkOSOperator.isEmptyString(christmasRemoveNoticeTime)) {
			christmasRemoveNoticeTimeList = Collections.synchronizedList(SerializeHelper.cfgStr2List(christmasRemoveNoticeTime));
		} else {
			christmasRemoveNoticeTimeList = Collections.synchronizedList(new ArrayList<>());
		}

		return super.assemble();
	}

	/**
	 * 检查探索时间是否合法
	 * 
	 * @param waitTime
	 * @return
	 */
	public boolean checkExploreTime(int waitTime) {
		if (exploreTimeArr == null) {
			return false;
		}
		for (int i = 0; i < exploreTimeArr.length; i++) {
			if (waitTime == exploreTimeArr[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取尤里工厂生命周期
	 * @return
	 */
	public long getYuriLifeTime() {
		return HawkRand.randInt(disappearTimeVal[0], disappearTimeVal[1]) * 1000L;
	}

	public int getYuriRefreshMax() {
		return yuriRefreshMax;
	}

	public int getAngryYuriRefreshMax() {
		return angryYuriRefreshMax;
	}

	public int getYuriRefreshCd() {
		return yuriRefreshCd;
	}

	public int getAngryYuriRefreshCd() {
		return angryYuriRefreshCd;
	}
	
	public int getYuriAwardType(){
		return HawkRand.randomWeightObject(yuriRevengeWeightList);
	}

	public int getRandomBoxGetCd() {
		return randomBoxGetCd;
	}

	public int getRandomBoxMarchSpeed() {
		return randomBoxMarchSpeed;
	}

	public int getYuriSucceedRate() {
		return yuriSucceedRate;
	}

	public int getYuriFailTimes() {
		return yuriFailTimes;
	}
	
	public String getInitWorldDressShow() {
		return initWorldDressShow;
	}

	public int getWorldNewMonsterRefreshContinueTime() {
		return worldNewMonsterRefreshContinueTime;
	}

	public int getWorldfoggyFortressRefreshMax() {
		return worldfoggyFortressRefreshMax;
	}

	public int getWorldEnemyFoggyLevelMinLimitCof() {
		return worldEnemyFoggyLevelMinLimitCof;
	}

	/**
	 * 获取世界中心点(王城点)id
	 * @return
	 */
	public int getCenterPointId() {
		return GameUtil.combineXAndY(WorldCenterX, WorldCenterY);
	}
	
	/**
	 * 获取资源刷新比例
	 * @return
	 */
	private List<RefreshAreaObject> getWorldResRefresh() {
		List<RefreshAreaObject> ret = new ArrayList<RefreshAreaObject>();
		
		// 国王调整资源
		PresidentResourceInfoSyn.Builder sbuilder = LocalRedis.getInstance().getPresidentResource();
		if (sbuilder == null) {
			return worldResRefresh;
		}
		
		int attrType = sbuilder.getAttrType();
		if (attrType == 0) {
			return worldResRefresh;
		}
		
		for (RefreshAreaObject resRefresh : worldResRefresh) {
			int key = resRefresh.getKey();
			int val = resRefresh.getWeight() * 10;
			if (attrType == key) {
				val = (int)(val * PresidentConstCfg.getInstance().getChangeResCoe());
			}
			ret.add(new RefreshAreaObject(key, val));
		}
		
		return ret;
	}

	/**
	 * 初始世界装扮
	 * @return
	 */
	public Map<Integer, Integer> getInitWorldDressShowMap() {
		return initWorldDressShowMap;
	}

	public int[] getOldMonsterRefreshTimeArr() {
		return oldMonsterRefreshTimeArr;
	}

	public int getAreaUpdateClock() {
		return areaUpdateClock;
	}

	public long getAreaUpdateRefreshTime() {
		return areaUpdateRefreshTime * 1000L;
	}
	
	/**
	 * 是否是特殊区域
	 * @param areaId
	 * @return
	 */
	public boolean isSpecialAreaId(int areaId) {
		return specialAreaIdList.contains(areaId);
	}
	
	/**
	 * 获取特殊区域id
	 * @return
	 */
	public List<Integer> getSpecialAreaIds() {
		return specialAreaIdList;
	}

	public int getMonsterRefreshTime() {
		return monsterRefreshTime;
	}

	public int getResourceRefreshTime() {
		return resourceRefreshTime;
	}

	public int[] getAllianceMoveOffsetArr() {
		return allianceMoveOffsetArr;
	}

	public int getMinStoryMonsterSerachRadius() {
		return minStoryMonsterSerachRadius;
	}

	public int getMaxStoryMonsterSearchRadius() {
		return maxStoryMonsterSearchRadius;
	}

	public int getStrongpointRefreshTime() {
		return strongpointRefreshTime;
	}

	public long getProtectCoverAddTime() {
		return protectCoverAddTime * 1000L;
	}

	public List<Integer> getGoundamRefreshTimeArr() {
		return goundamRefreshTimeArr;
	}

	public void setGoundamRefreshTimeArr(List<Integer> goundamRefreshTimeArr) {
		this.goundamRefreshTimeArr = goundamRefreshTimeArr;
	}

	public List<int[]> getGundamRefreshPosArr() {
		return gundamRefreshPosArr;
	}

	public void setGundamRefreshPosArr(List<int[]> gundamRefreshPosArr) {
		this.gundamRefreshPosArr = gundamRefreshPosArr;
	}

	public int getGundamRefreshAreaRadius() {
		return gundamRefreshAreaRadius;
	}

	public List<Integer> getNianRefreshTimeArr() {
		return nianRefreshTimeArr;
	}

	public List<int[]> getNianRefreshPosArr() {
		return nianRefreshPosArr;
	}

	public int getNianRefreshAreaRadius() {
		return nianRefreshAreaRadius;
	}

	public int getNianParamB() {
		return nianParamB;
	}

	public int getNianParamY() {
		return nianParamY;
	}

	public int getNianParamH() {
		return nianParamH;
	}

	public int getNianParamG() {
		return nianParamG;
	}

	public List<Integer> getNianParamAArray() {
		return nianParamAArray;
	}

	public List<Integer> getNianParamCArray() {
		return nianParamCArray;
	}

	public List<Integer> getNianParamXArray() {
		return nianParamXArray;
	}

	public List<Integer> getNianParamZArray() {
		return nianParamZArray;
	}

	public int getYuriBossDamageRewardParam() {
		return yuriBossDamageRewardParam;
	}

	public int getMergerNianInitK() {
		return mergeNianInitK;
	}

	public int getPointProtectNotify() {
		return pointProtectNotify;
	}

	public long getNianRemoveTime() {
		return nianRemoveTime * 1000L;
	}

	public List<Integer> getNianRmNoticeTimeArray() {
		return nianRmNoticeTimeArray;
	}

	public int getNianBoxRefRadius() {
		return nianBoxRefRadius;
	}

	public int getNianBoxReceiveLimit() {
		return nianBoxReceiveLimit;
	}

	public int[] getPylonAreaBegin() {
		return pylonAreaBegin;
	}

	public int[] getPylonAreaEnd() {
		return pylonAreaEnd;
	}

	public int getPylonRefreshCount() {
		return pylonRefreshCount;
	}

	public int getChristmasRefreshLimit() {
		return christmasRefreshLimit;
	}

	public String getChristmasRefreshTime() {
		return christmasRefreshTime;
	}

	public String getChristmasRefreshPos() {
		return christmasRefreshPos;
	}

	public int getChristmasRefreshAreaRadius() {
		return christmasRefreshAreaRadius;
	}

	public List<int[]> getChristmasRefreshPosList() {
		return christmasRefreshPosList;
	}

	public List<Integer> getChristmasRefreshTimeList() {
		return christmasRefreshTimeList;
	}

	public long getChristmasRemoveTime() {
		return christmasRemoveTime * 1000L;
	}

	public List<Integer> getChristmasRemoveNoticeTimeList() {
		return christmasRemoveNoticeTimeList;
	}

	public int getSnowballAtkDistance() {
		return snowballAtkDistance;
	}

	public long getSnowballAtkCd() {
		return snowballAtkCd * 1000L;
	}

	public int getNianParamK() {
		return nianParamK;
	}

	public List<EffectObject> getNianParamEffectList() {
		return GameUtil.assambleEffectObject(nianParamEffectList);
	}

	public List<Integer> getPylonRefreshTimePoints(){
		String[] arr = pylonRefreshPeroid.trim().split(",");
		List<Integer> list =new ArrayList<>();
		for(String str :arr){
			list.add(Integer.parseInt(str));
		}
		return list;
	}

	public float getDifficultyRatio() {
		return difficultyRatio;
	}

	public int getDifficultyCorrection() {
		return difficultyCorrection;
	}
	
	
	public List<HawkTuple3<Integer, Integer, Integer>> getGuildAutoMoveArea() {
		List<HawkTuple3<Integer, Integer, Integer>> list =new ArrayList<>();
		if(HawkOSOperator.isEmptyString(this.guildAutoMoveArea)){
			return list;
		}
		String[] arr = this.guildAutoMoveArea.trim().split(",");
		for(String str :arr){
			String[] param = str.split("_");
			HawkTuple3<Integer, Integer, Integer> tuple = HawkTuples.tuple(
					Integer.parseInt(param[0]), 
					Integer.parseInt(param[1]), 
					Integer.parseInt(param[2]));
			list.add(tuple);
		}
		return list;
	}
	
	
	public int getGuildAutoMoveCount() {
		return guildAutoMoveCount;
	}
	
	public int getGuildAutoMoveLeaderRadius() {
		return guildAutoMoveLeaderRadius;
	}
	
	public int getGuildAutoMoveLevelLimt() {
		return guildAutoMoveLevelLimt;
	}
	
	public int getGuildAutoMoveOpenTimeLimt() {
		return guildAutoMoveOpenTimeLimt;
	}
}
