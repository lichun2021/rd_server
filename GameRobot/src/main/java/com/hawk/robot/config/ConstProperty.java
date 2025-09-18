package com.hawk.robot.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;

import com.google.common.collect.ImmutableSortedMap;
import com.hawk.robot.config.element.MaterialSlotLimit;

/**
 * 常量使用配置
 *
 * @author julia
 *
 */
@HawkConfigManager.KVResource(file = "xml/const.xml")
public class ConstProperty extends HawkConfigBase {
	/**
	 * 设置一个单例对象, 以便访问
	 */
	private static ConstProperty instance = null;

	public static ConstProperty getInstance() {
		return instance;
	}

	protected final int giftRefreshAM;
	protected final int giftRefreshPM;
	// 新手保护时间
	protected final int newProtectTime;

	// 新手默认体力
	protected final int newVitPoint;

	// 新手引导建兵时间 (s)
	protected final int guideFirstTrainSoldierTime;

	// 新手造兵的最大人口上限
	protected final int newTrainSafeNum;

	// 体力点数恢复时间间隔（秒）
	protected final int vitPointAddTime;

	// 登录获得vip积分（未激活vip时获得积分_激活vip时倍数）
	protected final String loginVipPointAdd;

	// 聊天屏蔽玩家上限
	protected final int blockListLimit;

	// VIP等级提升奖励VIP激活时间（秒）
	protected final int vipTimeReward;

	// 玩家名称最短最长（字符数
	protected final String playerNameMinMax;

	// 首次加入联盟奖励
	private final String firstJoinGuildReward;

	// 主城最大坐标
	private final String maxCoordinate;
	private int[] cityMaxCoordinate = new int[] { 100, 100 };

	private final int freeTime;
	// 使用道具加速时，决定推荐数量取整方式的时间阈值，单位：秒
	private final int itemSpeedUpTimeThresholdValue;
	
	// 战斗最大回合数
	private final int battleRoundMax;

	// 掠夺系数（用前除1000）
	private final int grabWeightKey;

	// 基地沦陷之后，损毁建筑物的修理所需时间（秒）
	private final int repairTime;

	// 搜索输入最短限制
	private final int searchWordMinimum;
	// 玩家初始士兵
	protected final String newSoldier;
	// 造兵上限初始值
	protected final int newTrainQuantity;
	// 新手初始装备
	protected final String initialEquipments;
	// # 新手赠送免费VIP时间
	private final int freshPlayerFreeVipTime;

	// 邮件上限
	private final int mailLimit;
	// 邮件生存时间(s)
	private final int mailEffectTime;
	// 聊天邮件保存条数
	private final int chatMessage;

	// 训练取消返还资源比率。万分比
	private final int trainCancelReclaimRate;
	// 治疗取消返还资源比率。万分比
	private final int recoverCancelReclaimRate;
	// 升级取消返还资源比率。万分比
	private final int buildCancelReclaimRate;
	// 改建取消返还资源比率。万分比
	private final int reBuildCancelReclaimRate;
	// 科技研究取消返还资源比率。万分比
	private final int researchCancelReclaimRate;
	// 制造陷阱取消返还资源比例。万分比
	private final int cancelTrapResourceReturn;

	// # lower1_k1_b1,lower2_k2_b2……（分段线性公式）
	protected final String speedUpCost;

	// # 城建时间价值钻石的权重
	protected final double buildingTimeWeight;

	// # 造兵时间价值钻石的权重
	protected final double trainSoldierTimeWeight;

	// # 部队治疗时间价值钻石的权重
	protected final double cureSoldierTimeWeight;

	// # 科技研究时间价值水晶的权重
	protected final double techResearchTimeWeight;

	// # 英雄治疗时间价值钻石的权重
	protected final double cureHeroTimeWeight;

	// # 英雄训练时间价值钻石的权重
	protected final double trainHeroTimeWeight;
	
	// # 宝藏加速时间系数
	protected final double speedUpCoefficient;
	
	// # 装备材料加速时间系数
	protected final double equipQueueTimeWeight;

	// # lower1_k1_b1,lower2_k2_b2……（分段线性公式）
	protected final String buyResCost;

	// # 黄金资源价值钻石的权重
	protected final double goldResWeight;

	// # 石油资源价值钻石的权重
	protected final double oilResWeight;

	// # 合金资源价值钻石的权重
	protected final double soilResWeight;

	// # 铀矿资源价值钻石的权重
	protected final double uraniumResWeight;

	// # 是否开启世界资源刷新
	protected final boolean openWorldResourceUpdate;

	// 开启战斗异常行军消耗返还
	protected final boolean isOpenfightUnusualReturnDia;

	// 实际体力上限
	protected final int actualVitLimit;
	// 购买体力每次获得值
	protected final int buyEnergyAdd;
	// 购买体力消耗
	protected final String buyEnergyCost;

	// 充值钻石转换vip经验系数
	protected final int diaExchangeVipExpCof;

	// # 王城驻扎人口上限
	protected final int presidentOfficeDefencePopulation;

	// # 每日重置整点时间
	protected final int everyDayResetTime;

	// # 好友每日收礼上限（个数）
	protected final int friendGift;

	// # 亲密度区间与好友礼包映射(亲密度a_亲密度b,物品ID2;亲密度c_亲密度d,物品ID2)
	protected final String friendGiftIntimacy;
	
	// QQ关系涟礼物
	protected final String giveFriendGift;

	// # 好友上限（个数）
	protected final int friendUpperLimit;

	// # 推荐好友出现条件（当好友数量低于这个值时出现推荐好友）
	protected final int friendMinimumValue;

	//# 每次推荐好友个数
	protected final int friendRecommendCount;

	// # 申请列表容纳上限（个数）
	protected final int friendApplyLimit;

	// # 亲密度上限
	protected final int friendIntimacyLimit;

	// # 赠送一次礼包互相增加多少亲密度
	protected final int friendIntimacy;

	// # 查找陌生人好友每次抓取上限
	protected final int friendFindLimit;
	
	// 藏兵洞藏兵时间
	protected final String shelterRecall;
	
	// 攻击新版野怪返还体力系数（万分比）
	protected final int physicalPowerReturnCoe;
	
	// 钻石兑换水晶的值
	private final int exchangeValue;

	// # 每日活跃任务的刷新时间
	private final int dailyMissionTime;

	// 切换天赋2需要大本等级
	private final int unlockTalentLine2NeedCityLevel;
	
	// 切换天赋3需要大本等级	
	private final int unlockTalentLine3NeedCityLevel;
	/**
	 * 迁移城数据保存最大条数
	 */
	private final int cityMoveRecordLimit;

	/**
	 * 玩家加入联盟时vip一次性升到的等级，加入联盟后vip处于激活状态
	 */
	private final int vipActiveLevel;
	/**
	 * 初始可购买体力次数
	 */
	private final int initBuyEnergyTimes;
	/**
	 * 许愿池获取资源数值变化表达式，x1:基础值 x2:今日许愿次数
	 */
	private final String wishingAddValueExpr;
	/**
	 * 许愿池消耗资源数值变化表达式，x1:基础值 x2:今日许愿次数
	 */
	private final String wishingCostValueExpr;
	/**
	 * 许愿池消耗资源类型id
	 */
	private final int wishingCostResType;
	/**
	 * # 许愿池暴击概率(千分比)，格式：基础概率;2倍概率;5倍概率;10倍概率
	 */
	private final String wishingCritRate;
	/**
	 * 城外区域
	 */
	private final String outOfTownArea;
	
	/**
	 * 酒馆每日刷新时间，整点时间
	 */
	private final int tavernRefreshTime;

	/**
	 * 单次修复城墙增加城防值
	 */
	private final int onceWallRepair;

	/**
	 * 修复城墙间隔时间（秒）
	 */
	private final int wallRepairCd;

	/**
	 * 着火状态下，普通土地上的燃烧速度（点/毫秒）
	 */
	private final int wallFireSpeed;

	/**
	 * 着火状态下，黑土地上的燃烧速度（点/毫秒）
	 */
	private final int wallFireSpeedOnBlackLand;

	/**
	 * 城墙着火时间上限值（秒）
	 */
	private final int wallFireMaxTime;

	/**
	 * 单次攻击城墙造成的燃烧时间（秒）
	 */
	private final int onceAttackWallFireTime;

	/**
	 * 灭火消耗资源（type_id_count）
	 */
	private final String outFireCost;
	/**
	 * 付费队列购买
	 */
	private final String buyBuildQueue;
	/**
	 * 天赋洗点
	 */
	private final int talentResetItemSaleId;
	/**
	 * 天赋切换
	 */
	private final int talentExchangeItemSaleId;
	/**
	 * 造陷阱限定时间
	 */
	private final int trapTrainMaxTime;
	/**
	 * 主城等级低于此值，保护罩不会消失
	 */
	private final int protectNotDisLevel;

	/**
	 * 主城等级低于rebuildLevel的玩家主城，持续rebuildTime时间不上下，则起城堡在世界场景内被清除
	 */
	private final int rebuildLevel;

	private final int rebuildTime;

	/**
	 * 玩家离线时间推送点
	 */
	private final String recallPushTimeInterval;

	/**
	 * 兵种头顶可领取奖励的气泡(气泡类型1|资源类型_资源ID_资源数量|每日最大领取次数上限）
	 */
	private final String rondaAwardBubble;
	/**
	 * 建筑头顶气泡(气泡类型2|资源类型_资源ID_资源数量|每日最大领取次数上限）
	 */
	private final String buildAwardBubble;

	/**
	 * 研究基础免费时间，单位：秒
	 */
	private final int scienceFreeTime;
	/**
	 * 玩家离线后消息推送截止时间
	 */
	private final int pushEffectiveTime;
	/**
	 * 作用号366，采集时额外掉落资源宝箱，取的award表中的id
	 */
	private final int effect366LinkToAwardId;
	
	/**
	 * 建筑减少的时间
	 */
	private final int buildReduceValue;
	/**
	 * 训练士兵减少时间
	 */
	private final int trainReduceValue;
	/**
	 * 攻击尤里
	 */
	private final int attackReduceValue;
	/**
	 * 联盟传送离盟主的距离
	 */
	private final int allianceTransfer;
	
	// 判断是否大胜的条件，战斗力损失的阈值
	private final double battleReportPower;
	
	// 判断是否险胜的条件，双方交战回合数的阈值
	private final int battleReportBout;
	
	// 判断兵种是否单一条件，敌方兵种种类的阈值
	private final double battleReportKind;
	
	// 判断是否缺盾兵，盾兵的占比的阈值
	private final double battleReportTank;
	
	// 判断是否险胜的条件，失败方和胜利方战力的比值
	private final double battleReportspecific;
	
	/**
	 * 全服buff 临时使用
	 */
	private final String allSeverBuff;
	
	
	// 伤兵补偿每日次数
	private final int injuredSoldierTimes ;

	// 死兵补偿每日次数
	private final int deadSoldierTimes ;

	// 伤兵补偿最低战力
	private final int injuredSoldierPower;

	// 死兵补偿最低战力
	private final int deadSoldierPower ;

	// 伤兵补偿加速道具系数
	private final double injuredSoldierSpeedUpCoefficient ;

	// 伤兵补偿资源系数
	private final double injuredSoldierResourceCoefficient ;

	// 死兵补偿加速道具系数
	private final double deadSoldierSpeedUpCoefficient ;

	// 死兵补偿资源系数
	private final double deadSoldierResourceCoefficient ;

	// 5分钟伤兵加速道具id
	private final int injuredSoldierUpSpeedItem ;

	// 5分钟训练加速道具id
	private final int trainSoldierUpSpeedItem ;

	// 1000黄金
	private final int compensationGold ;

	// 1000石油
	private final int compensationOil ;

	// 150合金
	private final int compensationMetal ;

	// 40铀矿
	private final int compensationUranium;
	// 联盟关怀触发的联盟帮助的可帮助次数
	protected final int allianceCareHelpNumber;

	// 联盟关怀触发的联盟帮助的可持续时间，单位是秒
	protected final int allianceCareHelpTime;// = 600

	// 联盟关怀每次帮助的补偿系数
	protected final double allianceCareHelpCoefficient;// = 0.01

	/** 同一封邮件分享到联盟聊天有间隔时间，单位是秒*/
	private final int shareTime;
	
	/** 联盟邀请函过期时间*/
	private final int allianceInvitationOverTime;
	
	/** 装备最高品质*/
	private final int equipMaxQuality;
	
	/** 指挥官装备位限制*/
	private final String equipSlotLimit;
	
	/**重置英雄属性点数的消耗*/
	private final String heroAttrResetCost;

	/** 兵种晋升资源系数（出售兵种打的折扣z）晋升消耗公式=xb-(Xa×z) */
	private final double promotionVariate;

	/** 兵种晋升时间系数 */
	private final double promotionTimeVariate;
	/**
	 *  小于计算周期时好友活跃度参考系数百分比（对比值=实际登录天数/已开服天数）
	 */
	private final float friendActivePer;
	/**
	 * 好友活跃度计算周期
	 */
	private final int friendCycle;
	/**
	 * 好友活跃度计算周期对比天数(高于这个天数则为活跃）
	 */
	private final int friendCycleCom;
	/**
	 * 资源增产作用号序列
	 */
	private final String effectQueueForResUp;
	
	/**
	 * vip0级时的免费编队个数
	 */
	protected final int iniTroopTeamNum;
	
	private final String vipShopItem;
	
	/** 一键材料分解上限（最多一次获得材料数）*/
	private final int quickBreakUpperLimit;
	/**
	 * 黑市商人免费刷新次数
	 */
	private final int travelShopFreeRefreshTimes;
	/**
	 * 水晶刷新消耗
	 */
	private final String travelShopCrystalRefreshCost;
	/**
	 * 黑市商人的刷新时间
	 */
	private final String travelShopRefreshTime;
	/**
	 * 黑市礼包次数上限
	 */
	private final int travelGiftBuyTimesLimit;
	/**
	 * vip黑市礼包的次数上限
	 */
	private final int	specialTravelGiftBuyTimesLimit;
	/**
	 * 初始概率
	 */
	private final int travelGiftActivateInitProb;
	/**
	 * vip初始概率
	 */
	private final int specialTravelGiftActivateInitProb;
	/**
	 * 黑市礼包递增概率
	 */
	private final int travelGiftActivateAddProb;
	/**
	 * vip递增概率
	 */
	private final int specialTravelGiftActivateAddProb;
	
	// 迷雾宝箱最大空位
	private final int foggyBoxMaxSpace;
	
	// 联盟悬赏可持续时间（单位秒）
	private final int allianceCareHelpSustainTime;// = 604800

	// 联盟悬赏的最多条数
	private final int allianceCareHelpQuantityUpLimit;// = 50
	
	// 同一玩家的举报CD, 单位秒
	private final int reportingCD;
	/**
	 * 超值礼包刷新时间间隔.
	 */
	private final long giftResetTimeInterval;
	/**
	 * 破冰礼包
	 */
	private final int breakIceHeroPackageId;
	
	private final String LoginandRename;
	
	/**
	 *  屠城作用号触发概率
	 */
	private final int effect1060;
	/**
	 * 签名vip限制
	 */
	private final int signatureVipLimit;
	/**
	 * 签名长度限制
	 */
	private final int signatureLengthLimit;
	/**
	 * 每日迷雾要塞奖励次数 
	 */
	private final int foggyAttackMaxTimes;
	
	//# 聊天功能屏蔽（大本小于或等于XX等级时，无法发送聊天信息）
	private final int chatBlockByMainCityLevel;
	
	//# 世界聊天同样内容连续发送次数（大于3次不能发送，并弹出提示条）
	private final int chatSameContentTimes ;

	//# 连续发送同样内容时间限制
	private final int chatSameContentCD;

	//# 世界聊天连续发送间隔时间
	private final int chatSendTimeCD;
	
	// 通过道具获取黄金数量每日上限
	private final int gainCrystalLimitByUseItem;
	// 玩家每日通过金币购买体力有次数上限
	private final int dailyBuyEnergyTimesLimit;
	
	// 超时空急救冷却时间
	private final String superTimeRescueCd;
	
	// QQ启动加成
	private final String qqStartUp;
	
	// 微信游戏启动加成
	private final String wxStartUp;
	
	// 超级会员
	private final String svipStartUp;
	
	private final String resOutputBuffAll;
	
	private final String resOutputBuff1007;
	
	private final String resOutputBuff1008;
	
	private final String resOutputBuff1009;

	private final String resOutputBuff1010;
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 上面是配置数据, 底下是组装数据
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public String getSuperTimeRescueCd() {
		return superTimeRescueCd;
	}

	public String getQqStartUp() {
		return qqStartUp;
	}

	public String getWxStartUp() {
		return wxStartUp;
	}

	public String getSvipStartUp() {
		return svipStartUp;
	}

	public String getResOutputBuffAll() {
		return resOutputBuffAll;
	}

	public String getResOutputBuff1007() {
		return resOutputBuff1007;
	}

	public String getResOutputBuff1008() {
		return resOutputBuff1008;
	}

	public String getResOutputBuff1009() {
		return resOutputBuff1009;
	}

	public String getResOutputBuff1010() {
		return resOutputBuff1010;
	}

	public void setResUpEffectList(List<Integer> resUpEffectList) {
		this.resUpEffectList = resUpEffectList;
	}

	public void setMaterialSoltLimits(List<MaterialSlotLimit> materialSoltLimits) {
		this.materialSoltLimits = materialSoltLimits;
	}

	/**
	 * 建筑、士兵等一天中冒泡的最大次数
	 */
	private Map<Integer, Integer> bubbleMaxTimes;
	private ImmutableSortedMap<Long, HawkTuple2<Long, Long>> speedUpTimeLKB;
	private ImmutableSortedMap<Long, HawkTuple2<Long, Long>> buyResCostLKB;
	
	private List<Integer> resUpEffectList;

	// 登录获得vip积分（未激活vip时获得积分_激活vip时倍数）
	private int loginVipPoint = 0;
	private int loginVipBundle = 0;

	// 玩家名字最长最短值
	private int playerNameMin = 0;
	private int playerNameMax = 0;

	private int[] initSoldier;

	private Long[] recallPushIntervals;
	
	/** 生产装备材料的位置的解锁条件列表*/
	private List<MaterialSlotLimit>  materialSoltLimits;
	

	public ConstProperty() {
		instance = this;
		speedUpCoefficient = 0;
		chatMessage = 0;
		mailLimit = 0;
		mailEffectTime = 0;
		searchWordMinimum = 0;
		firstJoinGuildReward = null;
		newProtectTime = 0;
		newVitPoint = 0;
		vitPointAddTime = 0;
		loginVipPointAdd = "";
		blockListLimit = 0;
		vipTimeReward = 0;
		playerNameMinMax = "";
		freeTime = 0;
		itemSpeedUpTimeThresholdValue = 0;
		battleRoundMax = 0;
		maxCoordinate = "";
		grabWeightKey = 0;
		repairTime = 0;
		initialEquipments = "";
		newSoldier = "";
		newTrainQuantity = 0;
		freshPlayerFreeVipTime = 0;
		trainCancelReclaimRate = 0;
		recoverCancelReclaimRate = 0;
		buildCancelReclaimRate = 0;
		reBuildCancelReclaimRate = 0;
		researchCancelReclaimRate = 0;
		cancelTrapResourceReturn = 0;
		speedUpCost = "";
		buildingTimeWeight = 0;
		trainSoldierTimeWeight = 0;
		cureSoldierTimeWeight = 0;
		techResearchTimeWeight = 0;
		cureHeroTimeWeight = 0;
		equipQueueTimeWeight = 0;
		buyResCost = "";
		goldResWeight = 0;
		oilResWeight = 0;
		soilResWeight = 0;
		uraniumResWeight = 0;
		trainHeroTimeWeight = 0;
		guideFirstTrainSoldierTime = 0;
		openWorldResourceUpdate = true;
		newTrainSafeNum = 0;
		isOpenfightUnusualReturnDia = false;
		actualVitLimit = 0;
		buyEnergyAdd = 0;
		diaExchangeVipExpCof = 0;
		presidentOfficeDefencePopulation = 0;
		everyDayResetTime = 0;
		exchangeValue = 0;
		dailyMissionTime = 0;
		giftRefreshAM = 5;
		giftRefreshPM = 17;
		cityMoveRecordLimit = 30;
		vipActiveLevel = 5;
		initBuyEnergyTimes = 0;
		wishingAddValueExpr = "";
		wishingCostValueExpr = "";
		outOfTownArea = "";
		wishingCritRate = "";
		wishingCostResType = 0;
		tavernRefreshTime = 0;
		onceWallRepair = 0;
		wallRepairCd = 0;
		wallFireSpeed = 0;
		wallFireSpeedOnBlackLand = 0;
		wallFireMaxTime = 0;
		onceAttackWallFireTime = 0;
		outFireCost = "";
		buyBuildQueue = "";
		talentResetItemSaleId = 0;
		talentExchangeItemSaleId = 0;
		trapTrainMaxTime = 0;
		protectNotDisLevel = 0;
		rebuildLevel = 0;
		rebuildTime = 0;
		trainReduceValue = 0;
		attackReduceValue = 0;
		buildReduceValue = 0;
		buyEnergyCost = "";
		recallPushTimeInterval = "";
		rondaAwardBubble = "";
		buildAwardBubble = "";
		scienceFreeTime = 0;
		pushEffectiveTime = 0;
		friendGift = 0;
		friendGiftIntimacy = "";
		friendUpperLimit = 0;
		friendApplyLimit = 0;
		friendMinimumValue = 0;
		friendRecommendCount = 0;
		friendIntimacyLimit = 0;
		friendIntimacy = 0;
		friendFindLimit = 0;
		allianceTransfer = 0;
		unlockTalentLine2NeedCityLevel = 0;
		unlockTalentLine3NeedCityLevel = 0;
		effect366LinkToAwardId = 0;
		battleReportBout = 0;
		battleReportKind = 0;
		battleReportPower = 0;
		battleReportTank = 0;
		battleReportspecific = 0;
		allSeverBuff = "";
		injuredSoldierTimes= 0;
		deadSoldierTimes= 0;
		injuredSoldierPower= 0;
		deadSoldierPower= 0;
		injuredSoldierSpeedUpCoefficient= 0;
		injuredSoldierResourceCoefficient= 0;
		deadSoldierSpeedUpCoefficient= 0;
		deadSoldierResourceCoefficient= 0;
		injuredSoldierUpSpeedItem= 0;
		trainSoldierUpSpeedItem= 0;
		compensationGold= 0;
		compensationOil= 0;
		compensationMetal= 0;
		compensationUranium= 0;
		shareTime = 0;
		allianceInvitationOverTime =0;
		equipMaxQuality = 0;
		equipSlotLimit = "";
		heroAttrResetCost = "";
		promotionVariate = 0;
		promotionTimeVariate = 0;
		friendActivePer = 0.0f;
		friendCycle = 0;
		friendCycleCom = 0;
		effectQueueForResUp = "";
		iniTroopTeamNum = 0;
		vipShopItem = "";
		quickBreakUpperLimit = 0;
		travelShopCrystalRefreshCost = "";
		travelShopFreeRefreshTimes = 0;
		travelShopRefreshTime = "";
		travelGiftActivateInitProb = 0;
		travelGiftBuyTimesLimit = 0;
		travelGiftActivateAddProb = 0;
		allianceCareHelpNumber = 10;
		allianceCareHelpTime = 600;
		allianceCareHelpCoefficient = 0.01;
		foggyBoxMaxSpace = 0;
		allianceCareHelpSustainTime = 604800;
		allianceCareHelpQuantityUpLimit = 50;
		shelterRecall = "";
		physicalPowerReturnCoe = 0;
		reportingCD = 1800;
		this.giftResetTimeInterval = 0;
		this.breakIceHeroPackageId = 0;
		this.specialTravelGiftActivateAddProb = 0;
		this.specialTravelGiftActivateInitProb = 0;
		this.specialTravelGiftBuyTimesLimit = 0;
		LoginandRename = "";
		effect1060 = 0;
		signatureVipLimit = 1;
		signatureLengthLimit = 14;
		foggyAttackMaxTimes = 0;
		chatBlockByMainCityLevel = 0;
		chatSameContentTimes = 0 ;
		chatSameContentCD = 0;
		chatSendTimeCD = 0;
		gainCrystalLimitByUseItem = 1000;
		dailyBuyEnergyTimesLimit = 10;
		giveFriendGift = "";
		superTimeRescueCd = "";
		qqStartUp = "";
		wxStartUp = "";
		svipStartUp = "";
		resOutputBuffAll = "";
		resOutputBuff1007 = "";
		resOutputBuff1008 = "";
		resOutputBuff1009 = "";
		resOutputBuff1010 = "";
	}

	public String getGiveFriendGift() {
		return giveFriendGift;
	}

	/**
	 * 按资源区段,向下取K,B值
	 * @param res 权重*资源量
	 * @return
	 */
	public HawkTuple2<Long, Long> speedupKB(Long res) {
		return speedUpTimeLKB.floorEntry(res).getValue();
	}

	/**
	 * 按时间区段,向下取K,B值
	 * @param t 权重*时间(秒)
	 * @return
	 */
	public HawkTuple2<Long, Long> buyResKB(Long t) {
		return buyResCostLKB.floorEntry(t).getValue();
	}

	public int getPlayerNameMin() {
		return playerNameMin;
	}

	public int getPlayerNameMax() {
		return playerNameMax;
	}

	public int getLoginVipPoint() {
		return loginVipPoint;
	}

	public int getLoginVipBundle() {
		return loginVipBundle;
	}

	public int getNewProtectTime() {
		return newProtectTime;
	}

	public int getNewVitPoint() {
		return newVitPoint;
	}

	public int getVitPointAddTime() {
		return vitPointAddTime;
	}

	public String getLoginVipPointAdd() {
		return loginVipPointAdd;
	}

	public int getBlockListLimit() {
		return blockListLimit;
	}

	public int getVipTimeReward() {
		return vipTimeReward;
	}

	public String getPlayerNameMinMax() {
		return playerNameMinMax;
	}

	public int getFreeTime() {
		return freeTime;
	}

	public int getNewTrainQuantity() {
		return newTrainQuantity;
	}

	public String getHeroAttrResetCost() {
		return heroAttrResetCost;
	}

	public int getBattleRoundMax() {
		return battleRoundMax;
	}

	public int getGrabWeightKey() {
		return grabWeightKey;
	}

	public int getActualVitLimit() {
		return actualVitLimit;
	}

	public int getBuyEnergyAdd() {
		return buyEnergyAdd;
	}

	public int getDiaExchangeVipExpCof() {
		return diaExchangeVipExpCof;
	}

	public int getProtectNotDisLevel() {
		return protectNotDisLevel;
	}
	
	public int getRebuildLevel() {
		return rebuildLevel;
	}
	
	public int getRebuildTime() {
		return rebuildTime;
	}
	
	public ImmutableSortedMap<Long, HawkTuple2<Long, Long>> getSpeedUpTimeLKB() {
		return speedUpTimeLKB;
	}

	public void setSpeedUpTimeLKB(ImmutableSortedMap<Long, HawkTuple2<Long, Long>> speedUpTimeLKB) {
		this.speedUpTimeLKB = speedUpTimeLKB;
	}

	public ImmutableSortedMap<Long, HawkTuple2<Long, Long>> getBuyResCostLKB() {
		return buyResCostLKB;
	}

	public void setBuyResCostLKB(ImmutableSortedMap<Long, HawkTuple2<Long, Long>> buyResCostLKB) {
		this.buyResCostLKB = buyResCostLKB;
	}

	public int[] getInitSoldier() {
		return initSoldier;
	}

	public void setInitSoldier(int[] initSoldier) {
		this.initSoldier = initSoldier;
	}

	public String getMaxCoordinate() {
		return maxCoordinate;
	}

	public String getNewSoldier() {
		return newSoldier;
	}

	public int getCancelTrapResourceReturn() {
		return cancelTrapResourceReturn;
	}

	public String getSpeedUpCost() {
		return speedUpCost;
	}

	public String getBuyResCost() {
		return buyResCost;
	}

	public String getBuyEnergyCost() {
		return buyEnergyCost;
	}

	public int getFriendGift() {
		return friendGift;
	}

	public String getFriendGiftIntimacy() {
		return friendGiftIntimacy;
	}

	public int getFriendUpperLimit() {
		return friendUpperLimit;
	}

	public int getFriendMinimumValue() {
		return friendMinimumValue;
	}

	public int getFriendRecommendCount() {
		return friendRecommendCount;
	}

	public int getFriendApplyLimit() {
		return friendApplyLimit;
	}

	public int getFriendIntimacyLimit() {
		return friendIntimacyLimit;
	}

	public int getFriendIntimacy() {
		return friendIntimacy;
	}

	public int getFriendFindLimit() {
		return friendFindLimit;
	}

	public String getRecallPushTimeInterval() {
		return recallPushTimeInterval;
	}

	public String getRondaAwardBubble() {
		return rondaAwardBubble;
	}

	public String getAllSeverBuff() {
		return allSeverBuff;
	}

	public int getInjuredSoldierTimes() {
		return injuredSoldierTimes;
	}

	public int getDeadSoldierTimes() {
		return deadSoldierTimes;
	}

	public int getInjuredSoldierPower() {
		return injuredSoldierPower;
	}

	public int getDeadSoldierPower() {
		return deadSoldierPower;
	}

	public double getInjuredSoldierSpeedUpCoefficient() {
		return injuredSoldierSpeedUpCoefficient;
	}

	public double getInjuredSoldierResourceCoefficient() {
		return injuredSoldierResourceCoefficient;
	}

	public double getDeadSoldierSpeedUpCoefficient() {
		return deadSoldierSpeedUpCoefficient;
	}

	public double getDeadSoldierResourceCoefficient() {
		return deadSoldierResourceCoefficient;
	}

	public int getInjuredSoldierUpSpeedItem() {
		return injuredSoldierUpSpeedItem;
	}

	public int getTrainSoldierUpSpeedItem() {
		return trainSoldierUpSpeedItem;
	}

	public int getCompensationGold() {
		return compensationGold;
	}

	public int getCompensationOil() {
		return compensationOil;
	}

	public int getCompensationMetal() {
		return compensationMetal;
	}

	public int getCompensationUranium() {
		return compensationUranium;
	}

	public void setCityMaxCoordinate(int[] cityMaxCoordinate) {
		this.cityMaxCoordinate = cityMaxCoordinate;
	}

	public void setLoginVipPoint(int loginVipPoint) {
		this.loginVipPoint = loginVipPoint;
	}

	public void setLoginVipBundle(int loginVipBundle) {
		this.loginVipBundle = loginVipBundle;
	}

	public void setPlayerNameMin(int playerNameMin) {
		this.playerNameMin = playerNameMin;
	}

	public void setPlayerNameMax(int playerNameMax) {
		this.playerNameMax = playerNameMax;
	}

	public void setRecallPushIntervals(Long[] recallPushIntervals) {
		this.recallPushIntervals = recallPushIntervals;
	}

	@Override
	protected boolean assemble() {
		bubbleMaxTimes = new HashMap<Integer, Integer>();
		if (!HawkOSOperator.isEmptyString(rondaAwardBubble)) {
			String[] strs = rondaAwardBubble.split(";");
			int type = Integer.valueOf(strs[0]);
			bubbleMaxTimes.put(type, Integer.valueOf(strs[2]));
		}
		
		if (!HawkOSOperator.isEmptyString(buildAwardBubble)) {
			String[] strs = buildAwardBubble.split(";");
			int type = Integer.valueOf(strs[0]);
			bubbleMaxTimes.put(type, Integer.valueOf(strs[2]));
		}
		
		if (!HawkOSOperator.isEmptyString(recallPushTimeInterval)) {
			String[] intervalStrs = recallPushTimeInterval.split("_");
			recallPushIntervals = new Long[intervalStrs.length];
			for (int i = 0; i < intervalStrs.length; i++) {
				recallPushIntervals[i] = Integer.valueOf(intervalStrs[i].trim()) * 1000L;
			}
		} else {
			recallPushIntervals = new Long[0];
		}
		
		if (!HawkOSOperator.isEmptyString(maxCoordinate)) {
			cityMaxCoordinate = new int[2];
			String[] strs = maxCoordinate.split("_");
			cityMaxCoordinate[0] = Integer.parseInt(strs[0]);
			cityMaxCoordinate[1] = Integer.parseInt(strs[1]);
		}

		if (!HawkOSOperator.isEmptyString(speedUpCost)) {
			String[] lkbarray = speedUpCost.split(",");
			Map<Long, HawkTuple2<Long, Long>> map = new HashMap<>();
			for (String lkb : lkbarray) {
				String[] lkbstr = lkb.split("_");
				Long L = Long.valueOf(lkbstr[0]);
				Long K = Long.valueOf(lkbstr[1]);
				Long B = Long.valueOf(lkbstr[2]);
				map.put(L, new HawkTuple2<Long, Long>(K, B));
			}
			speedUpTimeLKB = ImmutableSortedMap.copyOf(map);
		}

		if (!HawkOSOperator.isEmptyString(buyResCost)) {
			String[] lkbarray = buyResCost.split(",");
			Map<Long, HawkTuple2<Long, Long>> map = new HashMap<>();
			for (String lkb : lkbarray) {
				String[] lkbstr = lkb.split("_");
				Long L = Long.valueOf(lkbstr[0]);
				Long K = Long.valueOf(lkbstr[1]);
				Long B = Long.valueOf(lkbstr[2]);
				map.put(L, new HawkTuple2<Long, Long>(K, B));
			}
			buyResCostLKB = ImmutableSortedMap.copyOf(map);
		}
		
		if (!HawkOSOperator.isEmptyString(effectQueueForResUp)) {
			resUpEffectList = new ArrayList<>();
			String[] effects = effectQueueForResUp.split("_");
			for (String effect : effects) {
				resUpEffectList.add(Integer.valueOf(effect));
			}
		}

		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		if (speedUpTimeLKB == null) {
			return false;
		}
		if (buyResCostLKB == null) {
			return false;
		}
		return super.checkValid();
	}

	public int[] getCityMaxCoordinate() {
		return cityMaxCoordinate;
	}

	public int getRepairTime() {
		return repairTime;
	}

	public int getSearchWordMinimum() {
		return searchWordMinimum;
	}

	public String getFirstJoinGuildReward() {
		return firstJoinGuildReward;
	}

	public String getInitialEquipments() {
		return initialEquipments;
	}

	public int[] getInitSoldiers() {
		return initSoldier;
	}

	public int getFreshPlayerFreeVipTime() {
		return freshPlayerFreeVipTime;
	}

	public int getMailLimit() {
		return mailLimit;
	}

	public int getMailEffectTime() {
		return mailEffectTime;
	}

	public int getChatMessage() {
		return chatMessage;
	}

	public int getTrainCancelReclaimRate() {
		return trainCancelReclaimRate;
	}

	public int getRecoverCancelReclaimRate() {
		return recoverCancelReclaimRate;
	}

	public int getBuildCancelReclaimRate() {
		return buildCancelReclaimRate;
	}

	public int getReBuildCancelReclaimRate() {
		return reBuildCancelReclaimRate;
	}

	public int getResearchCancelReclaimRate() {
		return researchCancelReclaimRate;
	}

	public double getBuildingTimeWeight() {
		return buildingTimeWeight;
	}

	public double getTrainSoldierTimeWeight() {
		return trainSoldierTimeWeight;
	}

	public double getCureSoldierTimeWeight() {
		return cureSoldierTimeWeight;
	}
	
	public double getTechResearchTimeWeight() {
		return techResearchTimeWeight;
	}

	public double getCureHeroTimeWeight() {
		return cureHeroTimeWeight;
	}

	public double getGoldResWeight() {
		return goldResWeight;
	}

	public double getSoilResWeight() {
		return soilResWeight;
	}

	public double getUraniumResWeight() {
		return uraniumResWeight;
	}

	public double getTrainHeroTimeWeight() {
		return trainHeroTimeWeight;
	}

	public double getOilResWeight() {
		return oilResWeight;
	}

	public boolean isOpenWorldResourceUpdate() {
		return openWorldResourceUpdate;
	}

	public int getNewTrainSafeNum() {
		return newTrainSafeNum;
	}
	
	public int getTrapTrainMaxTime() {
		return trapTrainMaxTime;
	}

	public int getGuideFirstTrainSoldierTime() {
		return guideFirstTrainSoldierTime;
	}

	/**
	 * 开启战斗异常行军消耗返还
	 * @return
	 */
	public boolean isOpenfightUnusualReturnDia() {
		return isOpenfightUnusualReturnDia;
	}

	/**
	 * 获取王城驻扎人口上限
	 * @return
	 */
	public int getPresidentOfficeDefencePopulation() {
		return presidentOfficeDefencePopulation;
	}

	public int getEveryDayResetTime() {
		return everyDayResetTime;
	}

	public int getExchangeValue() {
		return exchangeValue;
	}

	/**
	 * 获取日常任务每日刷新时间
	 * @return
	 */
	public int getDailyMissionTime() {
		return dailyMissionTime;
	}

	public int getGiftRefreshAM() {
		return giftRefreshAM;
	}

	public int getGiftRefreshPM() {
		return giftRefreshPM;
	}

	public int getCityMoveRecordLimit() {
		return cityMoveRecordLimit;
	}

	public int getVipActiveLevel() {
		return vipActiveLevel;
	}

	public int getInitBuyEnergyTimes() {
		return initBuyEnergyTimes;
	}

	public String getWishingAddValueExpr() {
		return wishingAddValueExpr;
	}

	public String getWishingCostValueExpr() {
		return wishingCostValueExpr;
	}

	public int getWishingCostResType() {
		return wishingCostResType;
	}

	public String getWishingCritRate() {
		return wishingCritRate;
	}
	
	public int getTavernRefreshTime() {
		return tavernRefreshTime;
	}

	public int getOnceWallRepair() {
		return onceWallRepair;
	}

	public int getWallRepairCd() {
		return wallRepairCd;
	}

	public int getWallFireSpeed() {
		return wallFireSpeed;
	}

	public int getWallFireSpeedOnBlackLand() {
		return wallFireSpeedOnBlackLand;
	}

	public int getWallFireMaxTime() {
		return wallFireMaxTime;
	}

	public int getOnceAttackWallFireTime() {
		return onceAttackWallFireTime;
	}

	public int getMakeTrapCancelReclaimRate() {
		return cancelTrapResourceReturn;
	}

	public double getSpeedUpCoefficient() {
		return speedUpCoefficient;
	}

	public int getTalentResetItemSaleId() {
		return talentResetItemSaleId;
	}

	public int getTalentExchangeItemSaleId() {
		return talentExchangeItemSaleId;
	}

	public int getBuildReduceValue() {
		return buildReduceValue;
	}

	public int getTrainReduceValue() {
		return trainReduceValue;
	}

	public int getAttackReduceValue() {
		return attackReduceValue;
	}
	
	public int getBubbleMaxTimes(int type) {
		if (bubbleMaxTimes == null || !bubbleMaxTimes.containsKey(type)) {
			return 0;
		}
		
		return bubbleMaxTimes.get(type);
	}
	
	public Long[] getRecallPushIntervals() {
		return recallPushIntervals;
	}
	
	public List<Integer> getBubbleTypes() {
		if (bubbleMaxTimes == null) {
			return new ArrayList<>();
		}
		return bubbleMaxTimes.keySet().stream().collect(Collectors.toList());
	}

	public int getScienceFreeTime() {
		return scienceFreeTime;
	}

	public int getPushEffectiveTime() {
		return pushEffectiveTime;
	}

	public int getAllianceTransfer() {
		return allianceTransfer;
	}

	public String getBuyBuildQueue() {
		return buyBuildQueue;
	}

	public String getOutFireCost() {
		return outFireCost;
	}

	public int getEffect366LinkToAwardId() {
		return effect366LinkToAwardId;
	}

	public int getUnlockTalentLine2NeedCityLevel() {
		return unlockTalentLine2NeedCityLevel;
	}

	public int getUnlockTalentLine3NeedCityLevel() {
		return unlockTalentLine3NeedCityLevel;
	}
	
	public double getBattleReportPower() {
		return battleReportPower;
	}

	public int getBattleReportBout() {
		return battleReportBout;
	}

	public double getBattleReportKind() {
		return battleReportKind;
	}

	public double getBattleReportTank() {
		return battleReportTank;
	}

	public double getBattleReportspecific() {
		return battleReportspecific;
	}

	public int getShareTime() {
		return shareTime;
	}

	public String getOutOfTownArea() {
		return outOfTownArea;
	}
	
	public String getBuildAwardBubble() {
		return buildAwardBubble;
	}

	public int getAllianceInvitationOverTime() {
		return allianceInvitationOverTime;
	}

	public double getPromotionVariate() {
		return promotionVariate;
	}

	public double getPromotionTimeVariate() {
		return promotionTimeVariate;
	}

	public float getFriendActivePer() {
		return friendActivePer;
	}

	public int getFriendCycle() {
		return friendCycle;
	}

	public int getFriendCycleCom() {
		return friendCycleCom;
	}

	public String getEffectQueueForResUp() {
		return effectQueueForResUp;
	}

	public String getVipShopItem() {
		return vipShopItem;
	}

	public int getIniTroopTeamNum() {
		return iniTroopTeamNum;
	}

	public List<MaterialSlotLimit> getMaterialSoltLimits() {
		return materialSoltLimits;
	}

	public int getEquipMaxQuality() {
		return equipMaxQuality;
	}

	public List<Integer> getResUpEffectList() {
		return resUpEffectList;
	}

	public int getQuickBreakUpperLimit() {
		return quickBreakUpperLimit;
	}

	public int getTravelShopFreeRefreshTimes() {
		return travelShopFreeRefreshTimes;
	}

	public String getTravelShopCrystalRefreshCost() {
		return travelShopCrystalRefreshCost;
	}

	public String getTravelShopRefreshTime() {
		return travelShopRefreshTime;
	}

	public int getItemSpeedUpTimeThresholdValue() {
		return itemSpeedUpTimeThresholdValue;
	}

	public String getEquipSlotLimit() {
		return equipSlotLimit;
	}

	public int getAllianceCareHelpSustainTime() {
		return allianceCareHelpSustainTime;
	}

	public int getAllianceCareHelpQuantityUpLimit() {
		return allianceCareHelpQuantityUpLimit;
	}

	public int getReportingCD() {
		return reportingCD;
	}

	public long getGiftResetTimeInterval() {
		return giftResetTimeInterval;
	}

	public int getBreakIceHeroPackageId() {
		return breakIceHeroPackageId;
	}

	public double getEquipQueueTimeWeight() {
		return equipQueueTimeWeight;
	}

	public String getShelterRecall() {
		return shelterRecall;
	}

	public int getPhysicalPowerReturnCoe() {
		return physicalPowerReturnCoe;
	}

	public int getAllianceCareHelpNumber() {
		return allianceCareHelpNumber;
	}

	public int getAllianceCareHelpTime() {
		return allianceCareHelpTime;
	}

	public double getAllianceCareHelpCoefficient() {
		return allianceCareHelpCoefficient;
	}

	public int getTravelGiftBuyTimesLimit() {
		return travelGiftBuyTimesLimit;
	}

	public int getSpecialTravelGiftBuyTimesLimit() {
		return specialTravelGiftBuyTimesLimit;
	}

	public int getTravelGiftActivateInitProb() {
		return travelGiftActivateInitProb;
	}

	public int getSpecialTravelGiftActivateInitProb() {
		return specialTravelGiftActivateInitProb;
	}

	public int getTravelGiftActivateAddProb() {
		return travelGiftActivateAddProb;
	}

	public int getSpecialTravelGiftActivateAddProb() {
		return specialTravelGiftActivateAddProb;
	}

	public String getLoginandRename() {
		return LoginandRename;
	}

	public int getEffect1060() {
		return effect1060;
	}

	public int getFoggyBoxMaxSpace() {
		return foggyBoxMaxSpace;
	}

	public int getSignatureVipLimit() {
		return signatureVipLimit;
	}

	public int getSignatureLengthLimit() {
		return signatureLengthLimit;
	}

	public int getFoggyAttackMaxTimes() {
		return foggyAttackMaxTimes;
	}

	public int getChatSameContentTimes() {
		return chatSameContentTimes;
	}

	public int getChatSameContentCD() {
		return chatSameContentCD;
	}

	public int getChatSendTimeCD() {
		return chatSendTimeCD;
	}

	public int getChatBlockByMainCityLevel() {
		return chatBlockByMainCityLevel;
	}

	public int getGainCrystalLimitByUseItem() {
		return gainCrystalLimitByUseItem;
	}

	public int getDailyBuyEnergyTimesLimit() {
		return dailyBuyEnergyTimesLimit;
	}
	
}
