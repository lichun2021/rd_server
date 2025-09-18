package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Table;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.HawkTuple2WeightObj;
import com.hawk.serialize.string.SerializeHelper;

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

	private final String effect12031EffectiveRound,effect12036EffectiveRound,effect12062RoundWeight,effect12253SoldierAdjust,effect12253HoldTimes,effect12468RoundWeight;
	private int[] effect12031EffectiveRoundArr = { 0, 0 } , effect12036EffectiveRoundArr={ 0, 0 } ;
	private final int effect12001AllNumLimit, effect12002AllNumLimit, effect12003AllNumLimit, effect12001SelfNumLimit, effect12002SelfNumLimit, effect12003SelfNumLimit,
			effect12001Maxinum, effect12002Maxinum, effect12003Maxinum, effect12004AllNumLimit, effect12004SelfNumLimit, effect12004Maxinum, effect12005AllNumLimit,
			effect12005SelfNumLimit, effect12005AtkRound, effect12005AtkNum, effect12005ContinueRound, effect12005Maxinum, effect12006MaxValue, effect12007MaxValue,
			effect12008MaxValue, effect12011AllNumLimit, effect12012AllNumLimit, effect12013AllNumLimit,effect12011SelfNumLimit,effect12012SelfNumLimit,effect12013SelfNumLimit,
			effect12011Maxinum,effect12012Maxinum,effect12013Maxinum,effect12014AllNumLimit,effect12014SelfNumLimit,effect12014Maxinum,effect12015AllNumLimit,effect12015SelfNumLimit,effect12015AtkRound,effect12015AtkTimes,effect12015AtkNum,effect12015ContinueRound,effect12015Maxinum
			,effect12033Maxinum,effect12034Maxinum,effect12035Maxinum,effect12039Maxinum,effect12040Maxinum,effect12040Nums,effect12041ContinueRound,effect12061AllNumLimit,effect12061SelfNumLimit,effect12061MaxValue,effect12061Maxinum
			, effect12062AllNumLimit, effect12062SelfNumLimit, effect12062AtkNum, effect12062AtkTimes, effect12062Maxinum,effect12063MaxValue,effect12064MaxValue,effect12065MaxValue,effect12254Maxinum;
	public final int effect1681AllNumLimit, effect1681SelfNumLimit, effect1681RateValue, effect1681MaxValue, effect1682SelfNumLimit, effect1683SelfNumLimit,
			effect1684SelfNumLimit, effect1682RateValue, effect1683RateValue, effect1684RateValue, effect12051SelfNumLimit, effect12051AtkRound,effect12051AtkTimesForMass,effect12051AtkTimesForPerson
			,effect12066AllNumLimit,effect12066SelfNumLimit,effect12066AtkTimes,effect12066Maxinum,effect12053SelfNumLimit,effect12054SelfNumLimit,effect12052SelfNumLimit,effect12081SelfNumLimit,effect12082SelfNumLimit,effect12083SelfNumLimit,effect12081AllNumLimit,effect12082AllNumLimit,effect12083AllNumLimit
			,effect12081Maxinum,effect12082Maxinum,effect12083Maxinum,effect12084SelfNumLimit,effect12084AllNumLimit,effect12084Maxinum,effect12085SelfNumLimit,effect12085AllNumLimit,effect12085EffectNum,effect12085NumLimit,effect12085Maxinum
			,effect12086RateMin,effect12086RateMax,effect12086TransferRate,effect12085AtkRound,effect12085ContinueRound,effect12085GetLimit,effect12104MaxNum,effect5001Cnt,effect12113ContinueRound,effect12114LossThresholdValue,staffOffice4Max
			,effect12122AllNumLimit,effect12122BaseVaule,effect12122Maxinum,effect12123AllNumLimit,effect12123BaseVaule,effect12123Maxinum,effect12134ContinueRound,effect12132ContinueRound,effect12151AllNumLimit
			,effect12151BaseVaule,effect12151Maxinum,effect12152Maxinum,effect12153ContinueRound,effect12154MaxNum,effect12154ContinueRound,effect1685Maxinum,effect12161AllNumLimit,effect12161SelfNumLimit,effect12161ContinueRound,effect12161Maxinum
			,effect12162AllNumLimit,effect12162SelfNumLimit,effect12162NextRound,effect12162EffectiveUnit,effect12162ContinueRound,effect12162Maxinum,effect12163AllNumLimit,effect12163SelfNumLimit,effect12163AtkNum,effect12163Maxinum
			,effect12164ContinueRound,effect12165ContinueRound,effect12166ContinueRound,effect12167ContinueRound,effect12168ContinueRound,effect12169ContinueRound,effect12170ContinueRound,effect12171ContinueRound
			,effect12172AllNumLimit,effect12173AllNumLimit,effect12172SelfNumLimit,effect12173SelfNumLimit,effect12173MaxNum,effect12172Maxinum,effect12173Maxinum,effect12191AllNumLimit,effect12191BaseVaule,effect12191Maxinum,effect12192AllNumLimit,effect12192BaseVaule,effect12192Maxinum,effect12193Maxinum,effect12194Maxinum
			,effect12201BasePoint,effect12201ExtraPoint,effect12201MaxPoint,effect12202AtkRound,effect12202AtkThresholdValue,effect12202AtkTimes,effect12206Maxinum,effect12208MaxNum,effect12221NumLimit,effect12221Maxinum
			,effect12222NumLimit,effect12222Maxinum,effect12223Maxinum,effect12224Maxinum,effect12231ArmyNum,effect12251AtkTimes,effect12261AllNumLimit,effect12261SelfNumLimit,effect12261Maxinum,effect12262AllNumLimit,effect12262SelfNumLimit,effect12262Maxinum,effect12264HoldTimes
			,effect12271AtkTimesForMass,effect12271AtkTimesForPerson,effect12272MaxValue,effect12281SelfNumLimit,effect12281AllNumLimit,effect12281Maxinum,effect12282SelfNumLimit,effect12282AllNumLimit,effect12282Maxinum,effect12292AtkNum,effect12293Maxinum
			,effect12301AdjustForMass,effect12302AllNumLimit,effect12302SelfNumLimit,effect12302AffectNum,effect12302MaxTimes,effect12302Maxinum,effect12312AtkNum,heroSoulTroopImageUnlockStage,heroSoulMarchImageUnlockStage,heroSoulSkinUnlockStage,
			effect12331AllNumLimit,effect12331SelfNumLimit,effect12331AddEscortPoint,effect12331MaxEscortPoint,effect12331Maxinum,effect12333AllNumLimit,effect12333SelfNumLimit,effect12333AffectNum,effect12333Maxinum,effect12335AllNumLimit,effect12335SelfNumLimit,effect12335AddFirePoint,effect12335MaxFirePoint,effect12335Maxinum,
			effect12337AllNumLimit,effect12337SelfNumLimit,effect12337AffectNum,effect12337Maxinum,effect12339AllNumLimit,effect12339SelfNumLimit,effect12339CountRound,effect12339Maxinum,effect12355AddNum,effect12356AddNum,effect12361BasePro,effect12362BasePro,effect12362ContinueRound,effect12363TargetNum,effect12365MaxNum,effect12383AddNum,
			effect12393Maxinum, effect12394Maxinum, effect12401Maxinum, effect12402Maxinum, effect12403Maxinum, effect12404Maxinum, effect11042ContinueRound, effect11043ContinueRound, effect11044MaxNum, superDamageCof, effect12412AtkNums, effect12412AtkTimes,effect12413ContinueRound,
			effect12414AtkTimesForPerson,effect12414Maxinum,effect12433AddAtkNum,effect12441AllNumLimit,effect12441BaseVaule,effect12441Maxinum,effect12442NumLimit,effect12442Maxinum,effect12443ContinueRound,effect12451ConditionalRatio,effect12451Maxinum,batchHeroMax,batchChipMax,batchEquipmentMax,
			effect12461Maxinum, effect12461AtkRound,effect12461InitChooseNum,effect12461ContinueRound,effect12461AllNumLimit,effect12461SelfNumLimit,effect12464Maxinum,effect12464BaseVaule,effect12465Maxinum,effect12465AddFirePoint,effect12465AtkRound,effect12465AtkThresholdValue,effect12465InitChooseNum,effect12465BaseVaule,effect12465ContinueRound,effect12465CountMaxinum
			,effect12466Maxinum,effect12468AtkNum,effect12469Maxinum,effect12481BaseVaule,effect12482BaseVaule,effect12482CountMaxinum,effect12461AdjustCof,effect12461ShareBaseVaule,effect12461ShareCountMaxinum,effect12461GrowCof,effect12461AdjustCountMaxinum,effect12465GrowCof,effect12465AirForceCountMaxinum,effect12116IntervalRound,effect12116ContinueRound,effect12116Maxinum
			,effect12501AllNumLimit,effect12501BaseVaule,effect12501Maxinum,effect12502AllNumLimit,effect12502Maxinum,effect12503NumLimit,effect12503Maxinum,effect12504NumLimit,effect12504Maxinum
			,effect12511AddFirePoint, effect12511AddFirePointExtra,effect12511AtkRound,effect12511AtkThresholdValue,effect12511ContinueRound,effect12511CountMaxinum,effect12511BaseVaule,effect12541AtkRound,effect12541AtkNum,effect12541LoseAdjust,effect12551AllNumLimit,effect12551BaseVaule,effect12551Maxinum,effect12552AllNumLimit,effect12552BaseVaule,effect12552Maxinum,
			effect12553AllNumLimit,effect12553BaseVaule,effect12553Maxinum,effect12553MaxValue,effect12553CountMaxinum,effect12554AllNumLimit,effect12554BaseVaule,effect12554Maxinum,effect12554MaxValue,effect12554CountMaxinum,effect12555NumLimit,effect12555Maxinum,effect12556NumLimit,effect12556Maxinum
			,effect12512CritTimes,effect12512Maxinum,effect12513AtkNum,effect12513AtkTimes,effect12513BaseVaule,effect12513CountMaxinum,effect12515Maxinum,effect12517Maxinum,effect12518Maxinum,effect12532BaseVaule,effect12532CountMaxinum,effect1325RankRatio,effect1325Maxinum
			,effect12571SelfNumLimit,effect12571AllNumLimit,effect12571Maxinum,effect12571BaseVaule,effect12571AddFirePoint,effect12571AtkThresholdValue,effect12571ContinueRound,effect12573BaseVaule,effect12573ContinueRound,effect12574AtkRound,effect12574InitChooseNum,effect12574ContinueRound,effect12574BaseVaule,effect12575BaseVaule,effect12576ReduceFirePoint,effect12577BaseVaule,effect12578BaseVaule
			,effect12577Adjust,effect12578Adjust,effect12561AtkRound,effect12561BaseVaule,effect12561ContinueRound,effect12601AllNumLimit,effect12601BaseVaule,effect12601Maxinum,effect12602AllNumLimit,effect12602BaseVaule,effect12602Maxinum,effect12602CountMaxinum,effect12603NumLimit,effect12603Maxinum,effect12604NumLimit,effect12604Maxinum
			,effect12611BaseVaule,effect12611AtkNums,effect12611AtkTimes,effect12611ContinueRound,effect12611Maxinum,effect12612BaseVaule,effect12613Maxinum,effect12614BaseVaule,effect12615AtkRound,effect12615BaseVaule,effect12615Maxinum,effect12616AtkRound,effect12616ContinueRound,effect12616BaseVaule,effect12616MaxTimes,effect12616Maxinum,effect12617Maxinum,effect12641ContinueRound
			,effect12651AtkRound,effect12651ContinueRound,effect12661AllNumLimit,effect12661BaseVaule,effect12661Maxinum, effect12662BaseVaule,effect12662Maxinum,effect12662CountMaxinum,effect12663NumLimit,effect12663Maxinum,effect12664NumLimit,effect12664Maxinum,effect12701AtkRound,effect12701ContinueRound
			,effect12671SelfNumLimit,effect12671AllNumLimit,effect12671Maxinum,effect12671PlusFirePoint,effect12672PlusEscortPoint,effect12671AddFirePoint,effect12671AtkThresholdValue,effect12672AddEscortPoint,effect12672AtkThresholdValue,effect12671BaseVaule,effect12672BaseVaule,effect12673BaseVaule,effect12673BaseVaule1543,effect12673BaseVaule1544,effect12674AtkRound,effect12674AtkTimes,effect12674BaseVaule1,effect12674BaseVaule2,effect12674ContinueRound,effect12674Maxinum,effect12674AtkThresholdValue
			,effect12675BaseVaule,effect12675ContinueRound,effect12675Maxinum,effect12676BaseVaule,effect12676ContinueRound,effect12676Maxinum,effect12711AllNumLimit,effect12711BaseVaule,effect12711Maxinum,effect12712BaseVaule,effect12713NumLimit,effect12713Maxinum,effect12714NumLimit,effect12714Maxinum;
	private final String effect12202DamageAdjust,effect12311DamageAdjust,effect12361TargetWeight,effect12361DamageAdjust,effect12362Adjust,effect12363TargetWeight,effect11041DamageAdjust,effect11042Adjust,effect11043Adjust, superDamageLimit,effect12414Adjust,effect12451DamageAdjust,effect12491RoundAdjust,effect12491SoldierAdjust,effect12465Adjust,effect12469Adjust,effect12512SoldierAdjust
		,effect12514RoundWeight,effect12515SoldierAdjust,effect12517SoldierAdjust,effect12518SoldierAdjust,effect1325SoldierAdjust,effect10052SoldierAdjust,effect10053SoldierAdjust,effect10054SoldierAdjust,effect10055SoldierAdjust,effect10056SoldierAdjust,effect10057SoldierAdjust,effect10058SoldierAdjust,effect10059SoldierAdjust,effect10060SoldierAdjust,effect12573SoldierAdjust
		,effect12574RoundWeight,effect12576SoldierAdjust,effect12561SoldierAdjust,effect10061SoldierAdjust,effect10062SoldierAdjust,effect10063SoldierAdjust,effect10064SoldierAdjust,effect10065SoldierAdjust,effect10066SoldierAdjust,effect10067SoldierAdjust,effect10068SoldierAdjust,effect10069SoldierAdjust,effect10070SoldierAdjust,
		effect12601SoldierAdjust,effect12602SoldierAdjust,effect12611RoundWeight,effect12612SoldierAdjust,effect12616SoldierAdjust,effect12617SoldierAdjust,effect12641SoldierAdjust,effect12651SoldierAdjust,effect12661SoldierAdjust,effect12662SoldierAdjust,effect10076SoldierAdjust,effect10077SoldierAdjust,effect12701SoldierAdjust,effect12671SoldierAdjust,effect12672SoldierAdjust,effect12674SoldierAdjust,effect12675SoldierAdjust
		,effect12676SoldierAdjust,effect12711SoldierAdjust,effect12712SoldierAdjust;
	public ImmutableMap<SoldierType, Integer> effect12363TargetWeightMap,effect12512SoldierAdjustMap,effect12514RoundWeightMap,effect12515SoldierAdjustMap,effect12517SoldierAdjustMap,effect12518SoldierAdjustMap,effect1325SoldierAdjustMap,effect10052SoldierAdjustMap,effect10053SoldierAdjustMap,effect10054SoldierAdjustMap,effect10055SoldierAdjustMap,effect10056SoldierAdjustMap
		,effect10057SoldierAdjustMap,effect10058SoldierAdjustMap,effect10059SoldierAdjustMap,effect10060SoldierAdjustMap,effect12573SoldierAdjustMap,effect12574RoundWeightMap,effect12576SoldierAdjustMap,effect12561SoldierAdjustMap,effect10061SoldierAdjustMap,effect10062SoldierAdjustMap,effect10063SoldierAdjustMap,effect10064SoldierAdjustMap,effect10065SoldierAdjustMap ,effect10066SoldierAdjustMap,effect10067SoldierAdjustMap,effect10068SoldierAdjustMap,effect10069SoldierAdjustMap,effect10070SoldierAdjustMap,
		effect12601SoldierAdjustMap,effect12602SoldierAdjustMap,effect12611RoundWeightMap,effect12612SoldierAdjustMap,effect12616SoldierAdjustMap,effect12617SoldierAdjustMap,effect12641SoldierAdjustMap,effect12651SoldierAdjustMap,effect12661SoldierAdjustMap,effect12662SoldierAdjustMap,effect10076SoldierAdjustMap,effect10077SoldierAdjustMap,effect12701SoldierAdjustMap,effect12671SoldierAdjustMap,effect12672SoldierAdjustMap,effect12674SoldierAdjustMap,effect12675SoldierAdjustMap
		,effect12676SoldierAdjustMap,effect12711SoldierAdjustMap,effect12712SoldierAdjustMap= ImmutableMap.of();
	private ImmutableMap<SoldierType, Integer> effect12362AdjustMap = ImmutableMap.of();
	private ImmutableMap<SoldierType, Integer> effect12361TargetWeightMap = ImmutableMap.of();
	private ImmutableMap<SoldierType, Integer> effect12361DamageAdjustMap = ImmutableMap.of();
	private ImmutableMap<SoldierType, Integer> effect12202DamageAdjustMap = ImmutableMap.of();
	private ImmutableMap<SoldierType, Integer> effect12253SoldierAdjustMap = ImmutableMap.of();
	private ImmutableMap<SoldierType, Integer> effect12253HoldTimesMap = ImmutableMap.of();
	private ImmutableMap<SoldierType, Integer> effect12311DamageAdjustMap = ImmutableMap.of();
	private ImmutableMap<SoldierType, Integer> effect11041DamageAdjustMap = ImmutableMap.of();
	private ImmutableMap<SoldierType, Integer> effect11042AdjustMap = ImmutableMap.of(); 
	private ImmutableMap<SoldierType, Integer> effect11043AdjustMap = ImmutableMap.of();
	private HawkTuple2<Integer, Integer> superDamageLimitMinMax = HawkTuples.tuple(0, 0);
	private ImmutableMap<SoldierType, Integer> effect12414AdjustMap = ImmutableMap.of();
	private ImmutableMap<SoldierType, Integer> effect12451DamageAdjustMap = ImmutableMap.of();
	private ImmutableMap<Integer, Integer> effect12491RoundAdjustMap = ImmutableMap.of();
	private ImmutableMap<SoldierType, Integer> effect12491SoldierAdjustMap = ImmutableMap.of();
	private ImmutableMap<SoldierType, Integer> effect12465AdjustMap = ImmutableMap.of();
	private ImmutableMap<Integer, Integer> 	effect12469AdjustMap = ImmutableMap.of();
	private final int heroSoulResetOpen,heroSoulResetTimeLimit;
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
	
	// # 英雄试炼加速时间系数
	protected final double heroTrialQueueTimeWeight;

	// # 装备科技加速时间系数
	protected final double equipResearchQueueTimeWeight;
	
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
	
	private int[] shelterRecallArr = new int[4];
	
	// 钻石兑换水晶的值
	private final int exchangeValue;

	// # 每日活跃任务的刷新时间
	private final int dailyMissionTime;

	// 切换天赋2需要大本等级
	private final int unlockTalentLine2NeedCityLevel;
	// 切换天赋3需要大本等级	
	private final int unlockTalentLine3NeedCityLevel;
	// 切换天赋4需要大本等级
	private final int unlockTalentLine4NeedCityLevel;
	// 切换天赋5需要大本等级
	private final int unlockTalentLine5NeedCityLevel;
	// 切换天赋6需要大本等级
	private final int unlockTalentLine6NeedCityLevel;
	// 切换天赋7需要大本等级
	private final int unlockTalentLine7NeedCityLevel;
	// 切换天赋8需要大本等级
	private final int unlockTalentLine8NeedCityLevel;
	
	private final int snowballAtkFireTime;
	
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
	// 判断是否大胜的阀值，失败战损/胜利战损 >= X
	private final double battleReportPower;
	
	// 判断普通胜利的条件，双方交战回合数的阈值 X > round为普通胜利
	private final int battleReportBout;
	
	// 判断兵种是否单一条件，敌方兵种种类的阈值
	private final double battleReportKind;
	
	// 判断是否缺盾兵，盾兵的占比的阈值
	private final double battleReportTank;
	
	// 判断是否险胜的条件，X >= 失败战损/胜利战损> 0
	private final double battleReportspecific;
	/**
	 * 全服buff 临时使用
	 */
	private final String allSeverBuff;
	
	
	// 伤兵补偿每日次数
	private final int injuredSoldierTimes;

	// 死兵补偿每日次数
	private final int deadSoldierTimes;

	// 伤兵补偿最低战力
	private final int injuredSoldierPower;

	// 死兵补偿最低战力
	private final int deadSoldierPower;

	// 伤兵补偿加速道具系数
	private final double injuredSoldierSpeedUpCoefficient;

	// 伤兵补偿资源系数
	private final double injuredSoldierResourceCoefficient;

	// 死兵补偿加速道具系数
	private final double deadSoldierSpeedUpCoefficient;

	// 死兵补偿资源系数
	private final double deadSoldierResourceCoefficient;

	// 5分钟伤兵加速道具id
	private final int injuredSoldierUpSpeedItem;

	// 5分钟训练加速道具id
	private final int trainSoldierUpSpeedItem;

	// 1000黄金
	private final int compensationGold;

	// 1000石油
	private final int compensationOil;

	// 150合金
	private final int compensationMetal;

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
	private final int chatBlockByMainCityLevel ;

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
	
	private final int effect377LinkToAwardId;
	
	// 单次拉取未注册好友的数量
	private final int qqFriend;
	
	// qq好友邀请关系过期时间, 单位秒
	private final int qqFriendTime;
	
	// 热销商城期数--该数据用于同步服务器热销商城是否有新商品上架
	private final int shopTerm;
	
	// 平台特权入口开关
	protected final boolean openPlatformPrivilege;
	// 一键建群开关
	protected final boolean openOneKeyCreateGroup;
	// 密友邀请开关
	protected final boolean openFriendsInvite;
	// 好友界面 平台好友页签开关
	protected final boolean openPlatformFriends;
	// 分享开关
	protected final boolean openShare;
	// 攻略,社区入口开关
	protected final boolean openPlatStrategy;
	// 启动特权加成开关
	protected final boolean openLoginWay;
	// 社区入口开关1_2_3
	protected final String openPlat;
	// 手Q启动#格式1_2_3_4_5
	protected final String openPlatformPrivilegeIds;
	
	// ios 社区入口开关1_2_3
	protected final String ios_openPlat;
	// ios 手Q启动#格式1_2_3_4_5
	protected final String ios_openPlatformPrivilegeIds;

	// 同玩好友QQ
	protected final boolean openWithQq;
	// 同玩好友微信
	protected final boolean openWithWechat;
	// 友界面赠送好礼
	protected final boolean openPlatformFriendsGift;
	// 攻略入口
	protected final boolean openStrategy;
	
	// CR英雄试练每日领奖次数限制
	protected final int crRewardLimit;
	
	// 机甲生产暴击概率
	protected final int gouda_crit;
	// 机甲比例
	protected final int gouda_percent;
	
	// 机甲解锁时发放道具
	protected final String grantItemForUnlockMecha;
	
	//是否开启每日福袋分享
	protected final boolean openQuestShare;
	
	/**跨服需要的物品消耗*/
	private final String crossCostItems;
	/**跨服需要的大本等级*/
	private final int crossCityLevel;
	
	/**每日分享战报奖励*/
	private final String shareReward;
	
	/**大版本更新奖励*/
	private final int versionUpRewardMailId;
	
	
	/**
	 * 开启额外侦查队列雷达建筑需要达到的等级
	 */
	private final int extraSpyRadarLv;
	
	/**
	 * 联盟语音功能开启关闭 
	 * 
	 */
	private final boolean openGuildVoice;
	
	/**
	 * 精英对决死转伤比例，万分比
	 */
	private final int dieBecomeInjury;
	/**莉塔系数*/
	private final int litaRatio;
	//# 英雄天赋更换每日花费金币递增
	private final String heroTalentChangeCost;// = 10000_1001_10,10000_1001_20,10000_1001_30,10000_1001_40,10000_1001_50,10000_1001_60,10000_1001_70,10000_1001_80,10000_1001_90,10000_1001_100

	//# 英雄天赋强化分段增加百分比及概率
	private final String heroTalentLevelupRateProbability;// = 100#1_30|2_50|3_30|4_10|5_5,1000#1_30|2_50|3_30|4_10|5_5,1000000#1_80|2_50|3_30|4_10|5_5

	// 新兵救援等级上限
	private final int recruitslevelLimit;
	// 新兵救援单日领取上限
	private final int dayclaimLimt;
	// 新兵救援总领取上限
	private final int totalclaimLimt;
	// 新兵救援功能开启时间限制
	private final int rescueDuration;
	// 复仇商店开关
	private final int revengeShopOpen;
	// 复仇商店刷新周期
	private final int revengeShopRefresh;
	// 复仇商店存在时长
	private final int revengeShopDuration;
	// 复仇商店功能触发时长
	private final int revengeShopTriggerTime;
	// 复仇商店功能触发死兵数量
	private final int revengeShopTriggerNum;
	// 复仇商店的有效兵种最低等级
	private final int revengeShopTroopsLevel;
	// 限时商店的每日触发次数
	private final int timeLimitShopTriggerTimes;
	
	private final String heroHide;
	private final String heroSkinHide;
	private final int effect1508Prob;
	private final int duelInitPower;//注：战力初始值读取const表，字段duelInitPower。该数值可调整，调整后仅对调整后的新服生效
	private final int duelPowerCheckTimeInterval; //注：检测间隔时间读取const表，字段duelPowerCheckTimeInterval（单位：天）。该数值可调整，调整后影响之后的检测时间间隔
	private final String duelPowerRankInterval; //注：排名区间读取const表，字段duelPowerRankInterval（排名靠前_排名靠后）。该数值可调整，调整后下次检测时的参考区间变化
	private final int duelPowerOnceAddLimit; //注：X%读取const表，字段duelPowerOnceAddLimit（万分比数值）。该数值可调整，调整后下次战力变化时以最新数值为参考
	
	private final int effect1512Prob;
	private final int effect1512DisDecayCof;
	private final int effect1512DisDecayMin;
	private final int effect1121Prob;
//	# 克瑞斯专属芯片作用号【1524】最大单位
	private final int effect1524Maximum;// = 20
//	# 克瑞斯专属芯片作用号【1525】输出常量
	private final String effect1525Power;// = 0,10000,14000,20000,28000,38000
	private final int effect1528Prob;
	private final int effect1528Power1;
	private final int effect1528Power2;
	private final int effect1529Maximum;
	private final int effect1530Maximum;
	private final String effect1535mum;
	private final int effect1535power;
	private final int hateRankShowNum;		//仇恨排行榜展示个数
	private final int hateRankNumLimit;		//仇恨存储限制个数
	private final int hatePowerLoseLimit;	//仇恨数据存储条件,双方之和达到多少
	private final int effect1312Maxinum; // 最大1312 叠加层数
	private final int effect1315Maxinum; 
	private final int effect1541Power; 
	private final int effect1541Maxinum;
	private final int effect1540Prob1;
	private final int effect1540Prob2;
	private final int effect1543Power;
	private final int effect1546Power;
	private final int effect1547Maxinum;
	private final int effect1413SuperiorLimit;
	private final int effect1548Maxinum;
	private final int effect1549Maxinum;
	private final int effect1554limit;
	private final int effect1610NumParam;
	private final int effect1611NumParam;
	private final int effect1612NumParam;
	private final String effect1562DamageParam; //注：伤害随目标数量变化数值读取const表，字段effect1562DamageParam；配置格式：数量1_伤害1,数量2_伤害2_......（目标数量在配置1~2之间时取伤害1，在数量2之上时取伤害2，以此类推；伤害数值为万分比）
	private final int effect1562TimesLimit; //注：攒豆数量达到XX时，触发范围攻击；该数值读取const表，字段effect1562TimesLimit，配置格式：上限数值（int）
	private final int effect1562DamageUpParam; //注：对坦克部队造成X倍伤害数值读取const表，字段effect1562DamageUpParam；配置格式：额外伤害倍数_兵种类型1_兵种类型2_......（额外伤害倍数为万分比数值）
	private final int effect7004Maximum;
	private final int effect7010Maximum;
	private final int effect7011RoundBegin;
	private final int effect7012TroopNums;
	private final int effect7014Round;
	private final int effect7015TroopNums;
	private final int effect7016TroopNums;
	private final int effect7018Maximum;
	private final int effect7019Maximum;
	private final int effect7020Maximum;
	private final int effect7021Maximum;
	private final int effect1617TimesLimit;
	private final int effect1628Maxinum; // 最大1628 叠加层数
	private final int effect1631Maxinum;
	private final int effect1631Per;
	private final int effect1633TimesLimit;
	private final int effect1634Maxinum;
	private final int effect1635Maxinum;
	private final int effect1635BaseVal;
	private final int effect1637Per;
	private final int effect1637Maxinum;
	private final int effect1638Per;
	private final int effect1638Maxinum;
	private final int effect1640Parametric;
	private final String effect1639Parametric;//  4_10_2 来表示上边三个条件
	private final int effect1639Per;//控制 填500 = 5% 
	private final String effect1639SoldierPer,effect1656Per;//控制 例：1_5,2_15,3_75,8_5  即只有1排轰炸，主战，防御，采矿车，轰炸被选中概率75%，主战15%，防坦，采矿车为5%
	private final int effect1639Maxinum;// 填3 即最多3个
	private final int effect1641Per; //填500 = 5% 
	private final int effect1641Maxinum; //控制 填4000 40%
	private final int effect1642Per; //控制 填500 = 5%
	private final int effect1642SoldierPer; // 填3000= 30% 若有，【1642】效果逻辑翻倍
	private final int  effect1642Maxinum;//控制 填4000 40%
	private final int effect1643SoldierPer;
	private final int effect1643Maxinum;
	private final int effect1644SoldierPer;
	private final int effect1644Maxinum,effect1658Per,effect1658Per2,effect1659Num,effect1661Num,effect1664Maxinum,effect1665Maxinum;
	// 账号注销申请cd时间
	private final int accountCancReqCd;
	
	// 账号注销持续时间
	private final int accountCancContinue;
	
	// 装备科技解锁品质
	private final int equipResearchUnlockQuality;

	// 装备科技外显解锁
	private final String equipResearchShowUnlock;
	
	// 获取道具任务的道具ID
	private final String recordItems;
	
	/**
	 * 新服推送礼包开启时间.
	 */
	private final String newPushGiftStartTime;
	private final int effect1321Prob;
	/**
	 * 跨服是否开启列表.
	 * 为空则所有的区服开启,
	 * 所以配置了一个不存在的区服.
	 */
	private final String crossEntitiesOptimizeServers;	
	// 建筑等级控制开关
	private final int buildControlLevel;
	private final String mechaRepairItem;
	
	/**
	 * 黑市商店友好度特权卡持续时间(s)
	 */
	private final int travelShopFriendlyCardTime;
	/**
	 * 黑市商店友好度奖励领取消耗
	 */
	private final int travelShopFriendlyAwardCost;
	/**
	 * 黑市商店友好度奖励(三段式)
	 */
	private final String travelShoFriendlyCommonAward;
	
	/**
	 * 黑市商店普通奖励随机池Type
	 */
	private final int travelShopFriendlyAwardCommType;
	/**
	 * 黑市商店特权奖励随机池Type
	 */
	private final int travelShopFriendlyAwardPrivilegeType;
	/**
	 * 黑市商店特权奖励随机组group
	 */
	private final int travelShopFriendlyAwardPrivilegeGroup;
	/**
	 * 琳琅特惠购买商品时，全局权重提升%最大值（万分比）
	 */
	private final int travelShopAssistRateRise;
	
	/**
	 * 新手优化版本的时间节点
	 */
	private final String newbieVersionTime;
	private long newbieVersionTimeValue;
	/**
	 * 特惠商人购买获得好感度数量（1消耗资源，2消耗金币）
	 */
	private final String travelShopFriendly;
	/**
	 * 特惠商人好感度特权卡购买后，每次购物获得好感度提升%（配置千分比）
	 */
	private final int travelShopFriendlyUpRate;
	/**
	 * 至尊vip相关字段
	 */
	private final int integral;
	private final int rewardExperience;
	private final String dailyLogin;
	private final int loginMaximum;
	private final int searchPrecise;
	
	/**
	 * 英雄档案馆开放等级
	 */
	private final int heroArchivesOpenLv;
	
	/**
	 * 英雄档案馆开放奖励
	 */
	private final String heroArchivesOpenAward;
	
	/**
	 * 联盟编队名字长度限制
	 */
	private final String rallySetTeamName;
	/**
	 * 每日发送改名道具次数上限
	 */
	private final int LoginandRenameDailyTime;

	private final String isDressGodOpenTime;
	/**
	 * 单次增加道具时的最大增加数量（单个）
	 */
	private final int maxAddItemNum;
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 上面是配置数据, 底下是组装数据
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private int superVipDailyLoginScore;
	private int superVipDailyLoginScoreStep;
	
    private List<ItemInfo> mechaRepairItemList = new ArrayList<ItemInfo>();
	private List<ItemInfo> heroTalentChangeCostList = new ArrayList<>();
	private TreeMap<Integer, List<HawkTuple2WeightObj<Integer>>> heroTalentLevelupRateProbabilityMap = new TreeMap<>();
	private Set<Integer> resOutputBuffToAllSet = new HashSet<Integer>();
	private Set<Integer> resOutputBuffGoldoreSet = new HashSet<Integer>();
	private Set<Integer> resOutputBuffOilSet = new HashSet<Integer>();
	private Set<Integer> resOutputBuffSteelSet = new HashSet<Integer>();
	private Set<Integer> resOutputBuffTombarthiteSet = new HashSet<Integer>();
	
	private List<Integer> allResOutputBuffList = new ArrayList<Integer>();
	
	private Table<String, Integer, Integer> platEffTable = HashBasedTable.create();
	
	private List<Integer> superTimeRescueCdList = new ArrayList<Integer>();
	
	private Map<Integer, Integer> vipShopItems = new HashMap<>();
	
	private List<Integer> resUpEffectList;

	private List<Integer> cityOutsideAreas;
	/**
	 * 建筑、士兵等一天中冒泡的最大次数
	 */
	private Map<Integer, Integer> bubbleMaxTimes;
	/**
	 * 建筑、士兵等冒泡随机奖励
	 */
	private Map<Integer, List<ItemInfo>> bubbleAwardItems;

	private List<Integer> recordItemList;
	
	private List<ItemInfo> outFireCostItems;
	private ImmutableSortedMap<Long, HawkTuple2<Long, Long>> speedUpTimeLKB;
	private ImmutableSortedMap<Long, HawkTuple2<Long, Long>> buyResCostLKB;

	// 登录获得vip积分（未激活vip时获得积分_激活vip时倍数）
	private int loginVipPoint = 0;
	private int loginVipBundle = 0;

	// 玩家名字最长最短值
	private int playerNameMin = 0;
	private int playerNameMax = 0;

	private int[] initSoldier;

	private Map<Integer, Integer> wishCritRateMap;
	/**
	 * 开启一次付费队列可用时长（秒）
	 */
	private int buyBuildQueueTimeLong;
	
	private ItemInfo buyBuildQueueCostItem;

	private List<ItemInfo> buyEnergyItemCost;

	private Long[] recallPushIntervals;
	
	/**
	 * 好友赠送礼物
	 */
	private Map<Integer, Integer> loveGift = new HashMap<Integer, Integer>();
	
	/** 指挥官装备位解锁等级*/
	private Map<Integer, Integer> commanderEquipUnlockLvls;
	
	/** 装备孔位对应装备位置类型*/
	private Map<Integer, Integer> equipSlotPos;
	
	/** 指挥官装备位数量*/
	private int commanderEquipSlotSize;
	/**
	 * 黑市商人水晶刷新消耗
	 */
	private int[] travelShopCrystalRefreshCostArray;
	/**
	 * 时间點
	 */
	private int[] travelShopRefreshTimeArray;
	/**
	 * 机甲解锁
	 */
	private List<ItemInfo> grantItemForUnlockMechaList;
	
	//分享cd
	private final int cdTimeSecond;
	// 自动打野有效期（对于玩家下线来讲）
	private final int startAutoTime;
	// 每日分享活跃福袋金币奖励数值
	private final String activeShareReward;
	
	// 密友6周年活动开启时间
	private final String wechatFriendsStartTime;
	// 密友6周年活动开启时间
	private final String wechatFriendsEndTime;
	// 二级密码审核时间
	private final int secPasswordExamineTime;
	
	/**
	 * 科技灰度开启的服务器
	 */
	private final String scienceOpenServers;
	
	/**
	 * 荣耀7是否开启
	 */
	private final int honorSevenOpen;
	
	/**
	 * 跨服需要的物品
	 */
	private List<ItemInfo> crossCostItemInfos;	
	
	private List<ItemInfo> activeShareAwardItems;	
	
	private long wechatFriendsStartTimeLong;
	private long wechatFriendsEndTimeLong;
	
	//玩家回流天数限制
	private final int playerLossDays;  
	//7_15_10003;8_21_100004;22_45_1000    根据玩家流失天数，确定推送消息ID (全局push)
	private final String playerLossPushNotice;   
	private List<HawkTuple3<Integer, Integer, List<Integer>>> playerLossPushNoticeList;
	//玩家5天内一流失条推送 (全局push)
	private final int PlayerLossPushCD;
	// 推荐流失玩家数量(好友push)
	private final int playerLossRecommendNum; 
	// 玩家每日接收好友流失推送上限 (好友push)
	private final int playerFriendLossPushReceiveNum;  
	//好友7天未登陆发送push(好友push)
	private final int playerFriendLossDaysPush;  
	//推送生效标志
	private final int  pushWorkFlag;
	//推送生效服务器
	private final String pushServers;
	private List<String> pushServerList;
	//流失push时间点
	private final int lossPushTime;
	

	/**
	 * 采集额外掉落：指定大本等级
	 */
	private final String resCollectExtraDropLevelLimit;
	
	/**
	 * 采集额外掉落：采集自然时间
	 */
	private final int resCollectExtraDropTime;
	
	/**
	 * 采集额外掉落：额外掉落
	 */
	private final int resCollectExtraDropAward;
	
	/**
	 * 采集额外掉落：每日次数
	 */
	private final int resCollectExtraDropTimesLimit;
	
	/**
	 * 被邀请人注册后通知X个邀请人（剩下的舍弃）
	 */
	private final int closeFriendNumLimit;
	/**
	 * 密友邀请点击冷却CD
	 */
	private final int goodFriendInviteCD;
	
	// 拉斐尔专属芯片作用号【4036】坦克兵种额外获取比例；万分比
	private final int effect4036RatioParam;

	// 拉斐尔专属芯片作用号【4037】空军兵种额外获取比例；万分比
	private final int effect4037RatioParam;

	// 拉斐尔专属芯片作用号【4038】步兵兵种额外获取比例；万分比
	private final int effect4038RatioParam;

	// 拉斐尔专属芯片作用号【4039】战车兵种额外获取比例；万分比
	private final int effect4039RatioParam;
	private final int effect1620Maxinum,effect1621Maxinum,effect1622Maxinum,effect1623Maxinum,effect1624Maxinum,effect1625Maxinum;
	private final int effect1645Maxinum, effect1645Power;
	private final int effect1647Maxinum, effect1648Maxinum;
	private final int effect1649Per,effect1649Per2,effect1650Per,effect1651Per,effect1652Num;
	private final int effect11003TimesLimit,effect11004TimesLimit,effect11005TimesLimit,effect11009TimesLimit,effect11010TimesLimit,effect11012TimesLimit,effect11013TimesLimit;
	// （每日必买）卡维利珍藏伪随机掉落： 宝箱itemId_开启次数_获得的item_awardId
	private final String cavilliPseudorandom;
	// 卡维利珍藏伪随机掉落相关参数
	private int dailyGiftBoxItemId, dailyGiftBoxOpenTimes, dailyGiftMustRewardItemId, dailyGiftRandomAwardId;
	// 金币特权主界面按钮出现时长
	private final int goldPrivilegeButtonTime;
	// 金币特权半价券道具ID
	private final int goldPrivilegeDiscountItem;
	// 金币特权半价payGiftId-ios
	private final String goldPrivilegePayGiftIdIos;
	// 金币特权半价payGiftId-Android
	private final String goldPrivilegePayGiftIdAndroid;
	// 金币特权半价券生效的特权类型
	private final int goldPrivilegeType;

	private final int getDressNumLimit;
	
	/**
	 * 采集额外掉落：指定大本等级
	 */
	private int resCollectExtraDropLevelLimitMin;
	private int resCollectExtraDropLevelLimitMax;
	/**
	 * 推送礼包开启时间.
	 */
	private long newPushGiftStartTimeValue;
	
	private List<Integer> equipResearchShowUnlockList;
	/**
	 * 是否开启优化区服.
	 */
	private List<String> crossEntitiesOptimizeServerList = new ArrayList<>();	

	private HawkTuple3<Integer, Integer, Integer> effect1639ParametricNum = HawkTuples.tuple(50, 50, 50);
	private Map<SoldierType, Integer> effect1639SoldierPerMap;
	//祝福语活动图片列表
	private final String blessingImg;
	
	private final String  airdropUnlock;	//解锁码头建筑条件
	private int airdropUnlockBuildType;
	
	// 自动生成野怪的距离位置
	private final String worldEnemyDistance;
	// 自动生成野怪的最高等级
	private final int worldEnemyLevel;
	// 虚拟野怪最大击杀次数（剧情任务里面的野怪是前端请求告诉后端生成的，这个值是为了防止刷）
	private final int fakeMonsterMaxKillTime;
	// 在QA服，分享后的图片是否带有二维码，填写0则不带有，1则带有二维码-用于平台上架
	private final int QAshareQRCode;
	// 客户端是否全部显示隐私设置相关内容，填写0表示不受客户端开关控制全部显示，填写1表示收客户端开关控制
	private final int privacyDisplay;
	private final String verifyBuildNo;
	private List<Integer> buildNoList;
	//超时空特权（自动拉锅道具消耗三段式）
	private final String spaceProp;
	private Map<Integer, Integer> spacePropLKB;
	//超时空特权放锅点空置允许时间
	private final long spacePropVacant;
	// 行军加速返送累计多少个会返送
	private final int effect628Num;
	// 线上服是否显示玩家的IP属地, 0不显示，1显示
	private final int showIpOnline;
	// 第二建造队列默认自动开启时间
	private final String secondBuildDefaultOpenTime;
	private final int secondBuildGiftGroupId;
	private final int firstFreeBuildQueue;
	
	/**
	 * 超武基座-功能建筑所需大本等级
	 */
	private final int manhattanUnlockLevel;

	// 使用道具返还物品（在相关道具的作用效果已存在的情况下，使用该类道具直接返还对应的物品。逗号分隔的三段：第一段1表示称号，第二段表示使用的称号道具，第三段表示使用时称号已存在的情况下该返还的物品）
	private final String PropExchange; // 1,30000_9716000_1,10000_1000_99999;1,30000_9702000_1,10000_1000_99999
	// 使用道具返还物品的参数
	private Map<Integer, Object[]> propExchangeParamMap;

	/**
	 * 黑市商店友好度
	 */
	private Map<Integer, Integer> travelShopFriendlyMap;
	
	/**
	 * 联盟中成员通过成员发放的联盟礼的个数上限
	 */
	private final int massFoggyAllianceGiftLimit;
	/**
	 * 参与集结胜利获得的奖励次数
	 */
	private final int assembleRewardGetLimit;
	/**
	 * 发动集结胜利获得的奖励次数
	 */
	private final int startAssembleRewardGetLimit;
	
	
	// 霸主赐福中霸主坐标点
	private final String statueCoordinates;
	private int[] statueCoordinatesArr = new int[2];

	private long isDressGodOpenTimeValue;
	
	private final int moveCityFixAramCD;
	
	
	private final String bossDailyLootTimeLimit185;
	private final String bossEnemyIdList185;
	private final String bossEnemyIdList184;
	private ImmutableMap<Integer,List<Integer>> bossEnemyId185 = ImmutableMap.of();
	private ImmutableMap<Integer,Integer> bossDailyLimit185 = ImmutableMap.of();

	private final String givenItemCostEvent;
	private Set<Integer> givenItemCostEventSet = new HashSet<>();
	
	
	
	//限制awardId = 1_2_3|243_343
	private final String blazeMedalAward;
	//打野每日限定数量 = 1231_10,1231_10|234_10,232_100
	private final String blazeMedalLimit;
	private ImmutableMap<Integer,List<Integer>> blazemedalAwardMap = ImmutableMap.of();
	private ImmutableMap<Integer,List<HawkTuple2<Integer, Integer>>> blazeMedalLimitMap = ImmutableMap.of();

	//一键升级最多可以升X级
	private final int  buildContinuousUpgradeLimit;
	private final int buildContinuousUpgradeCondition;
	public ConstProperty() {
		effect12714NumLimit = Integer.MAX_VALUE;
		effect12714Maxinum = 0;
		effect12713NumLimit = Integer.MAX_VALUE;
		effect12713Maxinum = 0;
		effect12712BaseVaule = 0;
		effect12712SoldierAdjust = "";
		effect12711AllNumLimit = Integer.MAX_VALUE;
		effect12711BaseVaule = 0;
		effect12711Maxinum = 0;
		effect12711SoldierAdjust = "";
		effect12676SoldierAdjust = "";
		effect12676BaseVaule = 0;
		effect12676ContinueRound = 0;
		effect12676Maxinum = 0;
		effect12675SoldierAdjust = "";
		effect12675BaseVaule = 0;
		effect12675ContinueRound = 0;
		effect12675Maxinum = 0;
		effect12674SoldierAdjust = "";
		effect12674AtkRound = Integer.MAX_VALUE;
		effect12674AtkTimes = 0; 
		effect12674BaseVaule1 = 0;
		effect12674BaseVaule2 = 0;
		effect12674ContinueRound = 0;
		effect12674Maxinum = 0;
		effect12674AtkThresholdValue = 0;
		effect12673BaseVaule = 0;
		effect12673BaseVaule1543 = 0;
		effect12673BaseVaule1544 = 0;
		effect12671SoldierAdjust = "";
		effect12672SoldierAdjust = "";
		effect12671SelfNumLimit= Integer.MAX_VALUE;
		effect12671AllNumLimit= Integer.MAX_VALUE;
		effect12671Maxinum = 0;
		effect12671PlusFirePoint = 0;
		effect12672PlusEscortPoint = 0;
		effect12671AddFirePoint = 0;
		effect12671AtkThresholdValue = 0;
		effect12672AddEscortPoint = 0;
		effect12672AtkThresholdValue = 0;
		effect12671BaseVaule = 0;
		effect12672BaseVaule = 0;
		effect12701SoldierAdjust = "";
		effect12701AtkRound = Integer.MAX_VALUE;
		effect12701ContinueRound = 0;
		effect10077SoldierAdjust = "";
		effect10076SoldierAdjust = "";
		effect12664NumLimit = Integer.MAX_VALUE;
		effect12664Maxinum = Integer.MAX_VALUE;
		effect12663NumLimit = Integer.MAX_VALUE;
		effect12663Maxinum = Integer.MAX_VALUE;
		effect12662BaseVaule = 0;
		effect12662Maxinum = 0;
		effect12662CountMaxinum = 0;
		effect12662SoldierAdjust = "";
		effect12661SoldierAdjust = "";
		effect12661AllNumLimit = Integer.MAX_VALUE;
		effect12661BaseVaule = 0;
		effect12661Maxinum = 0;
		effect12651SoldierAdjust = "";
		effect12651AtkRound = Integer.MAX_VALUE;
		effect12651ContinueRound = 0;
		effect12641SoldierAdjust = "";
		effect12641ContinueRound = 0;
		effect12617Maxinum = 0;
		effect12617SoldierAdjust = "";
		effect12616SoldierAdjust = "";
		effect12616AtkRound = Integer.MAX_VALUE;
		effect12616ContinueRound = Integer.MAX_VALUE;
		effect12616BaseVaule = Integer.MAX_VALUE;
		effect12616MaxTimes = Integer.MAX_VALUE;
		effect12616Maxinum = Integer.MAX_VALUE;
		effect12615AtkRound = Integer.MAX_VALUE;
		effect12615BaseVaule = 0;
		effect12615Maxinum = 0;
		effect12614BaseVaule = 0;
		effect12613Maxinum = 0;
		effect12612SoldierAdjust = "";
		effect12612BaseVaule = 0;
		effect12611RoundWeight = "";
		effect12611BaseVaule = 0;
		effect12611AtkNums = 0;
		effect12611AtkTimes = 0;
		effect12611ContinueRound = 0;
		effect12611Maxinum = 0;
		effect12604NumLimit= Integer.MAX_VALUE;
		effect12604Maxinum = Integer.MAX_VALUE;
		effect12603NumLimit= Integer.MAX_VALUE;
		effect12603Maxinum= Integer.MAX_VALUE;
		effect12602SoldierAdjust = "";
		effect12602AllNumLimit = Integer.MAX_VALUE;
		effect12602BaseVaule = Integer.MAX_VALUE;
		effect12602Maxinum = Integer.MAX_VALUE;
		effect12602CountMaxinum = Integer.MAX_VALUE;
		effect12601SoldierAdjust = "";
		effect12601AllNumLimit = Integer.MAX_VALUE;
		effect12601BaseVaule = Integer.MAX_VALUE;
		effect12601Maxinum = Integer.MAX_VALUE;
		effect10066SoldierAdjust = "";
		effect10067SoldierAdjust = "";
		effect10068SoldierAdjust = "";
		effect10069SoldierAdjust = "";
		effect10070SoldierAdjust = "";
		effect10061SoldierAdjust = "";
		effect10062SoldierAdjust = "";
		effect10063SoldierAdjust = "";
		effect10064SoldierAdjust = "";
		effect10065SoldierAdjust = "";
		effect12561AtkRound = Integer.MAX_VALUE;
		effect12561BaseVaule = 0;
		effect12561ContinueRound = 0;
		effect12561SoldierAdjust = "";
		effect12578BaseVaule = 0;
		effect12577BaseVaule = 0;
		effect12577Adjust = 0;
		effect12578Adjust = 0;
		effect12576SoldierAdjust = "";
		effect12576ReduceFirePoint = 0;
		effect12574RoundWeight = "";
		effect12574AtkRound = 0;
		effect12574InitChooseNum = 0;
		effect12574ContinueRound = 0;
		effect12574BaseVaule = 0;
		effect12575BaseVaule = 0;
		effect12573SoldierAdjust = "";
		effect12573ContinueRound = 0;
		effect12571SelfNumLimit = 0;
		effect12571AllNumLimit = 0;
		effect12571Maxinum = 0;
		effect12571BaseVaule = 0;
		effect12571AddFirePoint = 0;
		effect12571AtkThresholdValue = 0;
		effect12571ContinueRound = 0;
		effect12573BaseVaule = 0;
		effect10058SoldierAdjust = "";
		effect10059SoldierAdjust = "";
		effect10060SoldierAdjust = "";
		effect10057SoldierAdjust = "";
		effect10056SoldierAdjust = "";
		effect10055SoldierAdjust = "";
		effect10054SoldierAdjust = "";
		effect10053SoldierAdjust = "";
		effect10052SoldierAdjust = "";
		effect1325RankRatio = 0;
		effect1325Maxinum = 0;
		effect1325SoldierAdjust = "";
		effect12532BaseVaule = 0;
		effect12532CountMaxinum = 0;
		effect12518Maxinum = 0;
		effect12518SoldierAdjust = "";
		effect12517SoldierAdjust = "";
		effect12517Maxinum = 0;
		effect12515SoldierAdjust = "";
		effect12515Maxinum = 0;
		effect12514RoundWeight = "";
		effect12513AtkNum = 0;
		effect12513AtkTimes = 0;
		effect12513BaseVaule = 0;
		effect12513CountMaxinum = 0;
		effect12512CritTimes = Integer.MAX_VALUE;
		effect12512Maxinum = 0;
		effect12512SoldierAdjust = "";
		effect12556NumLimit = Integer.MAX_VALUE;
		effect12556Maxinum = Integer.MAX_VALUE;
		effect12555NumLimit= Integer.MAX_VALUE;
		effect12555Maxinum= Integer.MAX_VALUE;
		effect12554AllNumLimit= Integer.MAX_VALUE;
		effect12554BaseVaule = 0;
		effect12554Maxinum = 0;
		effect12554MaxValue = 0;
		effect12554CountMaxinum = 0;
		effect12553AllNumLimit= Integer.MAX_VALUE;
		effect12553BaseVaule = 0;
		effect12553Maxinum = 0;
		effect12553MaxValue = 0;
		effect12553CountMaxinum = 0;
		effect12552AllNumLimit = Integer.MAX_VALUE;
		effect12552BaseVaule = Integer.MAX_VALUE;
		effect12552Maxinum = Integer.MAX_VALUE;
		effect12551AllNumLimit = Integer.MAX_VALUE;
		effect12551BaseVaule = 0;
		effect12551Maxinum = 0;
		effect12541AtkRound = Integer.MAX_VALUE;
		effect12511BaseVaule = 0;
		effect12541AtkNum = 0;
		effect12541LoseAdjust = 0;
		effect12511AddFirePoint = 0;
		effect12511AddFirePointExtra = 0;
		effect12511AtkRound = Integer.MAX_VALUE;
		effect12511AtkThresholdValue = 0;
		effect12511ContinueRound = 0;
		effect12511CountMaxinum = 0;
		effect12504NumLimit = Integer.MAX_VALUE;
		effect12504Maxinum = 0;
		effect12503NumLimit = Integer.MAX_VALUE;
		effect12503Maxinum = 0;
		effect12502AllNumLimit = Integer.MAX_VALUE;
		effect12502Maxinum = 0;
		effect12501AllNumLimit = 0;
		effect12501BaseVaule = 0;
		effect12501Maxinum = 0;
		effect12116IntervalRound = 0;
		effect12116ContinueRound = 0;
		effect12116Maxinum = 0;
		effect12461ShareBaseVaule = 0;
		effect12461ShareCountMaxinum = 0;
		effect12461GrowCof = 0;
		effect12461AdjustCountMaxinum = 0;
		effect12465GrowCof = 0;
		effect12465AirForceCountMaxinum = 0;
		effect12461AdjustCof = 0;
		effect12482CountMaxinum=0;
		effect12482BaseVaule= 0;
		effect12481BaseVaule = 0;
		effect12469Maxinum = 0;
		effect12469Adjust = "";
		effect12468RoundWeight = "";
		effect12466Maxinum = 0;
		effect12468AtkNum = 0;
		effect12465Adjust = "";
		effect12465Maxinum = 0;
		effect12465AddFirePoint = 0;
		effect12465AtkRound = 0;
		effect12465AtkThresholdValue = 0;
		effect12465InitChooseNum = 0;
		effect12465BaseVaule = 0;
		effect12465ContinueRound = 0;
		effect12465CountMaxinum = 0;
		effect12461Maxinum = 0;
		effect12461AtkRound = 0;
		effect12461InitChooseNum = 0;
		effect12461ContinueRound = 0;
		effect12461AllNumLimit = 0;
		effect12461SelfNumLimit = 0;
		effect12464Maxinum = 0;
		effect12464BaseVaule = 0;
		effect12491RoundAdjust = "";
		effect12491SoldierAdjust = "";
		batchHeroMax = 100;
		batchChipMax = 100;
		batchEquipmentMax = 100;
		effect12451DamageAdjust = "";
		effect12451ConditionalRatio = 10000;
		effect12451Maxinum = 0;
		effect12443ContinueRound = 0;
		effect12442NumLimit = 0;
		effect12442Maxinum = 0;
		effect12441AllNumLimit = 0;
		effect12441BaseVaule = 0;
		effect12441Maxinum = 0;
		effect12433AddAtkNum = 0;
		effect12414Adjust = "";
		effect12414AtkTimesForPerson = 0;
		effect12414Maxinum = 0;
		effect12413ContinueRound = 0;
		effect12412AtkNums = 0;
		effect12412AtkTimes = 0;
		superDamageLimit = "";
		superDamageCof = 0;
		effect11044MaxNum = 0;
		effect11042Adjust = "";
		effect11043Adjust = "";
		effect11042ContinueRound = 0;
		effect11043ContinueRound = 0;
		effect11041DamageAdjust = "";
		effect12404Maxinum = 0;
		effect12403Maxinum = 0;
		effect12402Maxinum = 0;
		effect12401Maxinum = 0;
		effect12393Maxinum = 0;
		effect12394Maxinum = 0;
		heroSoulResetOpen=0;
		heroSoulResetTimeLimit=0;
		effect12383AddNum=0;
		effect12365MaxNum=0;
		effect12363TargetNum=0;
		effect12363TargetWeight="";
		effect12362Adjust = "";
		effect12362BasePro=0;
		effect12362ContinueRound=0;
		effect12361BasePro=0;
		effect12361TargetWeight="";
		effect12361DamageAdjust="";
		effect12356AddNum = 0;
		effect12355AddNum = 0;
		effect12339AllNumLimit = 0;
		effect12339SelfNumLimit = 0;
		effect12339CountRound = 0;
		effect12339Maxinum = 0;
		effect12337AllNumLimit = 0;
		effect12337SelfNumLimit = 0;
		effect12337AffectNum = 0;
		effect12337Maxinum = 0;
		effect12335AllNumLimit = 0;
		effect12335SelfNumLimit = 0;
		effect12335AddFirePoint = 0;
		effect12335MaxFirePoint = 0;
		effect12335Maxinum = 0;
		effect12333AllNumLimit = 0;
		effect12333SelfNumLimit = 0;
		effect12333AffectNum = 0;
		effect12333Maxinum = 0;
		effect12331AllNumLimit = 0;
		effect12331SelfNumLimit = 0;
		effect12331AddEscortPoint = 0;
		effect12331MaxEscortPoint = 0;
		effect12331Maxinum = 0;
		effect12254Maxinum = 0;
		heroSoulTroopImageUnlockStage = 0;
		heroSoulMarchImageUnlockStage = 0;
		heroSoulSkinUnlockStage =0;
		effect12312AtkNum = 0;
		effect12302Maxinum = 0;
		effect12302AllNumLimit = 0;
		effect12302SelfNumLimit = 0;
		effect12302AffectNum = 0;
		effect12302MaxTimes = 0;
		effect12301AdjustForMass = 0;
		effect12293Maxinum = 0;
		effect12292AtkNum = 0;
		effect12282SelfNumLimit = 0;
		effect12282AllNumLimit = 0;
		effect12282Maxinum = 0;
		effect12281SelfNumLimit = 0;
		effect12281AllNumLimit = 0;
		effect12281Maxinum = 0;
		effect12272MaxValue = 0;
		effect12271AtkTimesForMass = 0;
		effect12271AtkTimesForPerson = 0;
		effect12264HoldTimes = 0;
		effect12262AllNumLimit = 0;
		effect12262SelfNumLimit = 0;
		effect12262Maxinum = 0;
		effect12261AllNumLimit = 0;
		effect12261SelfNumLimit = 0;
		effect12261Maxinum = 0;
		effect12253HoldTimes = "";
		effect12253SoldierAdjust = "";
		effect12251AtkTimes = 0;
		effect12231ArmyNum = 0;
		effect12224Maxinum = 0;
		effect12223Maxinum = 0;
		effect12222NumLimit = 0;
		effect12222Maxinum = 0;
		effect12221Maxinum = 0;
		effect12221NumLimit = 0;
		effect12208MaxNum = 0;
		effect12206Maxinum = 0;
		effect12202AtkRound = 0;
		effect12202AtkThresholdValue = 0;
		effect12202AtkTimes = 0;
		effect12202DamageAdjust = "";
		effect12311DamageAdjust = "";
		effect12201BasePoint = 0;
		effect12201ExtraPoint = 0;
		effect12201MaxPoint = 0;
		effect12191AllNumLimit = 0;
		effect12191BaseVaule = 0;
		effect12191Maxinum = 0;
		effect12192AllNumLimit = 0;
		effect12192BaseVaule = 0;
		effect12192Maxinum = 0;
		effect12193Maxinum = 0;
		effect12194Maxinum = 0;
		effect12172AllNumLimit = 0;
		effect12173AllNumLimit = 0;
		effect12172SelfNumLimit = 0;
		effect12173SelfNumLimit = 0;
		effect12173MaxNum = 0;
		effect12172Maxinum = 0;
		effect12173Maxinum = 0;
		effect12164ContinueRound = 0;
		effect12165ContinueRound = 0;
		effect12166ContinueRound = 0;
		effect12167ContinueRound = 0;
		effect12168ContinueRound = 0;
		effect12169ContinueRound = 0;
		effect12170ContinueRound = 0;
		effect12171ContinueRound = 0;
		effect12163AllNumLimit = 0;
		effect12163SelfNumLimit = 0;
		effect12163AtkNum = 0;
		effect12163Maxinum = 0;
		effect12162AllNumLimit = 0;
		effect12162SelfNumLimit = 0;
		effect12162NextRound = 0;
		effect12162EffectiveUnit = 0;
		effect12162ContinueRound = 0;
		effect12162Maxinum = 0;
		effect12161AllNumLimit = 0;
		effect12161SelfNumLimit = 0;
		effect12161ContinueRound = 0;
		effect12161Maxinum = 0;
		effect1685Maxinum = 0;
		effect12154MaxNum = 0;
		effect12154ContinueRound = 0;
		effect12153ContinueRound = 0;
		effect12152Maxinum = 0;
		effect12151BaseVaule = 0;
		effect12151Maxinum = 0;
		effect12151AllNumLimit = 0;
		effect12132ContinueRound = 0;
		effect12134ContinueRound = 0;
		effect12123AllNumLimit = 0;
		effect12123BaseVaule = 0;
		effect12123Maxinum = 0;
		effect12122AllNumLimit = 0;
		effect12122BaseVaule = 0;
		effect12122Maxinum = 0;
		staffOffice4Max = 15;
		effect12114LossThresholdValue = 100;
		effect12113ContinueRound = 0;
		effect5001Cnt = 0;
		effect12104MaxNum = 0;
		effect12085AtkRound = 1000;
		effect12085ContinueRound = 0;
		effect12085GetLimit = 0;
		effect12086TransferRate = 0;
		effect12086RateMin = 0;
		effect12086RateMax = 0;
		effect12085SelfNumLimit = 0;
		effect12085AllNumLimit = 0;
		effect12085EffectNum = 0;
		effect12085NumLimit = 0;
		effect12085Maxinum = 0;
		effect12084SelfNumLimit = 0;
		effect12084AllNumLimit = 0;
		effect12084Maxinum = 0;
		effect12081SelfNumLimit = 0;
		effect12082SelfNumLimit = 0;
		effect12083SelfNumLimit = 0;
		effect12081AllNumLimit = 0;
		effect12082AllNumLimit = 0;
		effect12083AllNumLimit = 0;
		effect12081Maxinum = 0;
		effect12082Maxinum = 0;
		effect12083Maxinum = 0;
		effect12066AllNumLimit = 0;
		effect12066SelfNumLimit = 0;
		effect12066AtkTimes = 0;
		effect12066Maxinum = 0;
		effect12053SelfNumLimit = 0;
		effect12054SelfNumLimit = 0;
		effect12052SelfNumLimit = 0;
		effect12063MaxValue = 0;
		effect12064MaxValue = 0;
		effect12065MaxValue = 0;
		effect12062RoundWeight = "";
		effect12062AllNumLimit = 0;
		effect12062SelfNumLimit = 0;
		effect12062AtkNum = 0;
		effect12062AtkTimes = 0;
		effect12062Maxinum = 0;
		effect12061AllNumLimit = 0;
		effect12061SelfNumLimit = 0;
		effect12061MaxValue = 0;
		effect12061Maxinum = 0;
		effect12051AtkTimesForMass = 0;
		effect12051AtkTimesForPerson = 0;
		effect12051AtkRound = 0;
		effect12051SelfNumLimit = 0;
		effect12041ContinueRound = 0;
		effect12040Nums = 0;
		effect12040Maxinum = 0;
		effect12039Maxinum = 0;
		effect12035Maxinum = 0;
		effect12034Maxinum = 0;
		effect12033Maxinum = 0;
		effect12031EffectiveRound = "";
		effect12036EffectiveRound = "";
		effect12015AllNumLimit = 0;
		effect12015SelfNumLimit = 0;
		effect12015AtkRound = 0;
		effect12015AtkTimes = 0;
		effect12015AtkNum = 0;
		effect12015ContinueRound = 0;
		effect12015Maxinum = 0;
		effect12014AllNumLimit = 0;
		effect12014SelfNumLimit = 0;
		effect12014Maxinum = 0;
		effect12011AllNumLimit = 0;
		effect12012AllNumLimit = 0;
		effect12013AllNumLimit = 0;
		effect12011SelfNumLimit = 0;
		effect12012SelfNumLimit = 0;
		effect12013SelfNumLimit = 0;
		effect12011Maxinum = 0;
		effect12012Maxinum = 0;
		effect12013Maxinum = 0;
		effect12006MaxValue = 0;
		effect12007MaxValue = 0;
		effect12008MaxValue = 0;
		effect12005AllNumLimit = 0;
		effect12005SelfNumLimit = 0;
		effect12005AtkRound = 0;
		effect12005AtkNum = 0;
		effect12005ContinueRound= 0;
		effect12005Maxinum = 0;
		effect12004AllNumLimit = 0;
		effect12004SelfNumLimit = 0;
		effect12004Maxinum = 0;
		effect12001AllNumLimit = 0;
		effect12002AllNumLimit = 0;
		effect12003AllNumLimit = 0;
		effect12001SelfNumLimit = 0;
		effect12002SelfNumLimit = 0;
		effect12003SelfNumLimit = 0;
		effect12001Maxinum = 0;
		effect12002Maxinum = 0;
		effect12003Maxinum = 0;
		effect1682SelfNumLimit = 0;
		effect1683SelfNumLimit = 0;
		effect1684SelfNumLimit = 0;
		effect1682RateValue = 0;
		effect1683RateValue = 0;
		effect1684RateValue = 0;
		effect1681AllNumLimit = 0;
		effect1681SelfNumLimit = 0;
		effect1681RateValue = 0;
		effect1681MaxValue = 0;
		cdTimeSecond = 15;
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
		heroTrialQueueTimeWeight = 0;
		equipResearchQueueTimeWeight = 0;
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
		chatBlockByMainCityLevel = 0 ;
		chatSameContentTimes =0 ;
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
		qqFriend = 0;
		qqFriendTime = 36000;
		shopTerm = 0;
		effect377LinkToAwardId = 0;
		
		openOneKeyCreateGroup = true;
		openFriendsInvite = true;
		openPlatformFriends = true;
		openShare = true;
		openLoginWay = true;
		openPlat = "";
		openPlatformPrivilegeIds = "";
		
		ios_openPlat = "";
		ios_openPlatformPrivilegeIds = "";
		
		openWithQq = true;
		openWithWechat = true;
		openPlatformFriendsGift = true;
		openStrategy = true;
		openPlatformPrivilege = true;
		openPlatStrategy = true;
		crRewardLimit = 0;
		gouda_crit = 0;
		gouda_percent = 0;
		grantItemForUnlockMecha = "";
		openQuestShare = false;
		crossCostItems = "";
		crossCityLevel = 25;
		shareReward = "10000_1001_10";
		versionUpRewardMailId = 20190230;
		extraSpyRadarLv = 34;
		openGuildVoice = true;
		dieBecomeInjury = 0;
		startAutoTime = 300;
		litaRatio = 10000;
		heroTalentChangeCost= "";
		heroTalentLevelupRateProbability="";
		recruitslevelLimit = 6;
		dayclaimLimt = 30000;
		totalclaimLimt = 300000;
		rescueDuration = 2592000;
		revengeShopOpen = 0;
		revengeShopRefresh = 0;
		revengeShopDuration = 0;
		revengeShopTriggerTime = 0;
		revengeShopTriggerNum = 0;
		revengeShopTroopsLevel = 0;
		timeLimitShopTriggerTimes = 0;
		heroHide = "";
		heroSkinHide = "";
		unlockTalentLine4NeedCityLevel = 0;
		unlockTalentLine5NeedCityLevel = 0;
		unlockTalentLine6NeedCityLevel = 0;
		unlockTalentLine7NeedCityLevel = 0;
		unlockTalentLine8NeedCityLevel = 0;
		activeShareReward = "";
		effect1508Prob = 5000;
		duelInitPower = 10000000;
		duelPowerCheckTimeInterval = 2;
		duelPowerRankInterval = "16_24";
		duelPowerOnceAddLimit = 1000;
		wechatFriendsStartTime = "";
		wechatFriendsEndTime = "";
		effect1512Prob = 5000;
		effect1512DisDecayCof = 1000;
		effect1512DisDecayMin = 3000;
		effect1121Prob = 5000;
		secPasswordExamineTime = 259200;
		scienceOpenServers = "";
		effect1524Maximum = 20;
		effect1525Power = "0,10000,14000,20000,28000,38000";
		effect1535mum = "10000,10000,14000,20000,28000,38000";
		effect1312Maxinum = 3;
		effect1315Maxinum = 3;
		effect1541Power = 100;
		effect1541Maxinum = 30;
		effect1540Prob1 = 1000;
		effect1540Prob2 = 1000;
		effect1543Power = 10000;
		hateRankShowNum = 0;		
		hateRankNumLimit = 0;		
		hatePowerLoseLimit = 0;	
		
		effect1528Prob = 0;
		effect1528Power1 = 0;
		effect1528Power2 = 0;
		effect1529Maximum = 0;
		effect1530Maximum = 0;
		honorSevenOpen = 0;
		effect1535power = 0;
		snowballAtkFireTime = 0;
		effect1546Power = 0;
		effect1547Maxinum = 0;
		effect1413SuperiorLimit = 0;
		effect1548Maxinum = 0;
		effect1549Maxinum = 0;
		effect1554limit = 2500;
		effect1610NumParam = 1000000;
		effect1611NumParam = 1000000;
		effect1612NumParam = 1000000;
		accountCancReqCd = 3600;
		accountCancContinue = 2592000;
		
		playerLossDays = 100;  
		playerLossPushNotice = "";   
		playerLossRecommendNum = 0; 
		playerFriendLossPushReceiveNum = 0;  
		PlayerLossPushCD = 100;
		playerFriendLossDaysPush = 100;
		pushWorkFlag = 1;
		pushServers = "";
		lossPushTime = 4;
		equipResearchUnlockQuality = 3;
		
		resCollectExtraDropLevelLimit = "";
		resCollectExtraDropTime = 0;
		resCollectExtraDropAward = 0;
		resCollectExtraDropTimesLimit = 0;
		equipResearchShowUnlock = "2_4_6";
		closeFriendNumLimit = 5;
		goodFriendInviteCD = 0;
		
		effect1562DamageParam = "100000_2000,200000_1500"; 
		effect1562TimesLimit = 100;
		effect1562DamageUpParam = 5000;// 额外伤害倍数_兵种类型1_兵种类型2
		effect7004Maximum = 5;
		effect7010Maximum = 20;
		effect7011RoundBegin = 3;
		effect7012TroopNums = 3;
		effect7014Round = 3;
		effect7015TroopNums = 6;
		effect7016TroopNums = 6;
		effect7018Maximum = 30;
		effect7019Maximum = 30;
		effect7020Maximum = 10;
		effect7021Maximum = 10;
		effect1617TimesLimit = 100;
		effect1628Maxinum = 10;
		effect1631Maxinum = 5;
		effect1631Per = 500;
		effect1633TimesLimit = 3;
		effect1634Maxinum = 75;
		effect1635Maxinum = 5000;
		effect1635BaseVal = 10;
		effect1637Per = 0;
		effect1637Maxinum = 0;
		effect1638Per = 0;
		effect1638Maxinum = 0;
		effect1640Parametric = 400;
		effect1639Parametric = "";
		effect1639Per = 0; 
		effect1639SoldierPer = "";
		effect1656Per = "";
		effect1639Maxinum = 0;
		newPushGiftStartTime = "2020-08-03 00:00:00";
		effect1321Prob = 0;
		crossEntitiesOptimizeServers = "101";
		blessingImg = "";
		effect4036RatioParam = 0;
		effect4037RatioParam = 0;
		effect4038RatioParam = 0;
		effect4039RatioParam = 0;
		effect1620Maxinum = 0;
		effect1621Maxinum = 0;
		effect1622Maxinum = 0;
		effect1623Maxinum = 0;
		effect1624Maxinum = 0;
		effect1625Maxinum = 0;
        buildControlLevel = 0;
        mechaRepairItem = "";
		cavilliPseudorandom = "";
		goldPrivilegeButtonTime = 0;
		goldPrivilegeDiscountItem = 0;
        goldPrivilegePayGiftIdIos = "";
		goldPrivilegePayGiftIdAndroid = "";
		goldPrivilegeType = 0;
		airdropUnlock = "";
		recordItems = "";
		worldEnemyDistance = "";
		worldEnemyLevel = 0;
		fakeMonsterMaxKillTime = 1;
		travelShopFriendlyCardTime = 86400 * 7;
		travelShopFriendlyAwardCost = 10;
		travelShoFriendlyCommonAward = "";
		travelShopFriendlyAwardCommType = 0;
		travelShopFriendlyAwardPrivilegeType= 0;
		travelShopFriendlyAwardPrivilegeGroup = 0;
		travelShopAssistRateRise = 0;
		newbieVersionTime = "2022-3-4 06:00:00";
		travelShopFriendly = "1_10,2_50";
		travelShopFriendlyUpRate = 0;
		effect1641Per = 0; //填500 = 5% 
		effect1641Maxinum = 0; //控制 填4000 40%
		effect1642Per = 0; //控制 填500 = 5%
		effect1642SoldierPer = 0; // 填3000= 30% 若有，【1642】效果逻辑翻倍
		effect1642Maxinum = 0;//控制 填4000 40%
		effect1643SoldierPer = 0;
		effect1643Maxinum = 0;
		effect1644SoldierPer = 0;
		effect1644Maxinum = 0;
		effect1658Per = 0;
		effect1658Per2 = 0;
		effect1659Num = 0;
		effect1661Num = 0;
		effect1664Maxinum = 0;
		effect1665Maxinum = 0;
		QAshareQRCode = 0;
		privacyDisplay = 0;
		verifyBuildNo = "";
		spaceProp = "";
		spacePropVacant = 0;
		effect1645Maxinum = 0;
		effect1645Power = 0;
		effect1647Maxinum = 0;
		effect1648Maxinum =0 ;
		effect1649Per = 0;
		effect1649Per2 = 0;
		effect1650Per = 0;
		effect1651Per = 0;
		effect1652Num = 0;
		effect11003TimesLimit = 0;
		effect11004TimesLimit = 0;
		effect11005TimesLimit = 0;
		effect11009TimesLimit = 0;
		effect11010TimesLimit = 0;
		effect11012TimesLimit = 0;
		effect11013TimesLimit = 0;
		integral = 0;
		rewardExperience = 0;
		dailyLogin = "";
		loginMaximum = 0;
		effect628Num = 100;
		searchPrecise = 0;
		showIpOnline = 0;
		secondBuildDefaultOpenTime = "2022-11-10 00:00:00";
		secondBuildGiftGroupId = 0;
		firstFreeBuildQueue = 1;
		heroArchivesOpenLv = 0;
		heroArchivesOpenAward = "";
		PropExchange = "";
		rallySetTeamName = "1_4";
		LoginandRenameDailyTime = 1;
		statueCoordinates = "";
		isDressGodOpenTime = "";
		manhattanUnlockLevel = 0;
		getDressNumLimit = 5;
		moveCityFixAramCD = 0;
		
		bossDailyLootTimeLimit185 = "";
		bossEnemyIdList185 = "";
		bossEnemyIdList184 = "";
		maxAddItemNum = 250000;
		givenItemCostEvent = "";
		massFoggyAllianceGiftLimit = 100;
		assembleRewardGetLimit = 10;
		startAssembleRewardGetLimit = 10;
		
		blazeMedalAward ="";
		blazeMedalLimit = "";
		buildContinuousUpgradeLimit = 1;
		buildContinuousUpgradeCondition = 5;
	}
	
	public int getMassFoggyAllianceGiftLimit() {
		return massFoggyAllianceGiftLimit;
	}
	
	public int getAssembleRewardGetLimit() {
		return assembleRewardGetLimit;
	}
	
	public int getStartAssembleRewardGetLimit() {
		return startAssembleRewardGetLimit;
	}
	
	public int getMaxAddItemNum() {
		return maxAddItemNum;
	}

	public int getManhattanUnlockLevel() {
		return manhattanUnlockLevel;
	}

	public boolean isDressGodOpen() {
		long now = HawkTime.getMillisecond();
		return now >= isDressGodOpenTimeValue;
	}

	public int getLoginandRenameDailyTime() {
		return LoginandRenameDailyTime;
	}
	
	public int getEffect628Num() {
		return effect628Num;
	}
	
	public int getFakeMonsterMaxKillTime() {
		return fakeMonsterMaxKillTime;
	}

	public int getEffect4036Param() {
		return effect4036RatioParam;
	}

	public int getEffect4037Param() {
		return effect4037RatioParam;
	}

	public int getEffect4038Param() {
		return effect4038RatioParam;
	}

	public int getEffect4039Param() {
		return effect4039RatioParam;
	}

	public int getGoodFriendInviteCD() {
		return goodFriendInviteCD;
	}

	public int getCloseFriendNumLimit() {
		return closeFriendNumLimit;
	}

	/**
	 * 联盟副本邀请冷却时间
	 * @return
	 */
	public int getCdTime(){
		return cdTimeSecond;
	}
	
	public double getGouda_percent() {
		return gouda_percent * GsConst.EFF_PER;
	}

	public int getGouda_crit() {
		return gouda_crit;
	}

	public int getShopTerm() {
		return shopTerm;
	}

	public String getGiveFriendGift() {
		return giveFriendGift;
	}

	public int getChatBlockByMainCityLevel() {
		return chatBlockByMainCityLevel;
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

	public ItemInfo getPaidQueueOpenCost() {
		if (buyBuildQueueCostItem == null) {
			return null;
		}
		
		return buyBuildQueueCostItem.clone();
	}

	public int getPaidQueueTimeLong() {
		return buyBuildQueueTimeLong;
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
	
	public int getCommanderEquipUnlockLvl(int slotId) {
		if(commanderEquipUnlockLvls .containsKey(slotId)){
			return commanderEquipUnlockLvls.get(slotId);
		}
		return Integer.MAX_VALUE;
	}
	
	public int getEquipSlotPosType(int slotId) {
		if (equipSlotPos.containsKey(slotId)) {
			return equipSlotPos.get(slotId);
		}
		return 0;
	}

	public int getCommanderEquipSlotSize() {
		return commanderEquipSlotSize;
	}

	public String getShelterRecall() {
		return shelterRecall;
	}
	
	public String getGrantItemForUnlockMecha() {
		return grantItemForUnlockMecha;
	}
	
	/**
	 * 检查探索时间是否合法
	 * 
	 * @param waitTime
	 * @return
	 */
	public boolean checkExploreTime(int waitTime) {
		if (shelterRecallArr == null) {
			return false;
		}
		for (int i = 0; i < shelterRecallArr.length; i++) {
			if (waitTime == shelterRecallArr[i]) {
				return true;
			}
		}
		return false;
	}
	
	public long getNewbieVersionTimeValue() {
		return newbieVersionTimeValue;
	}

	@Override
	protected boolean assemble() {
		effect12712SoldierAdjustMap = str2SoldierTypeIntMap(effect12712SoldierAdjust);
		effect12711SoldierAdjustMap = str2SoldierTypeIntMap(effect12711SoldierAdjust);
		effect12676SoldierAdjustMap = str2SoldierTypeIntMap(effect12676SoldierAdjust);
		effect12675SoldierAdjustMap = str2SoldierTypeIntMap(effect12675SoldierAdjust);
		effect12674SoldierAdjustMap = str2SoldierTypeIntMap(effect12674SoldierAdjust);
		effect12671SoldierAdjustMap = str2SoldierTypeIntMap(effect12671SoldierAdjust);
		effect12672SoldierAdjustMap = str2SoldierTypeIntMap(effect12672SoldierAdjust);
		
		effect12701SoldierAdjustMap = str2SoldierTypeIntMap(effect12701SoldierAdjust);
		effect10077SoldierAdjustMap = str2SoldierTypeIntMap(effect10077SoldierAdjust);
		effect10076SoldierAdjustMap = str2SoldierTypeIntMap(effect10076SoldierAdjust);
		effect12662SoldierAdjustMap = str2SoldierTypeIntMap(effect12662SoldierAdjust);
		effect12661SoldierAdjustMap = str2SoldierTypeIntMap(effect12661SoldierAdjust);
		effect12651SoldierAdjustMap = str2SoldierTypeIntMap(effect12651SoldierAdjust);
		effect12641SoldierAdjustMap = str2SoldierTypeIntMap(effect12641SoldierAdjust);
		effect12617SoldierAdjustMap = str2SoldierTypeIntMap(effect12617SoldierAdjust);
		effect12616SoldierAdjustMap = str2SoldierTypeIntMap(effect12616SoldierAdjust);
		effect12612SoldierAdjustMap = str2SoldierTypeIntMap(effect12612SoldierAdjust);
		effect12611RoundWeightMap = str2SoldierTypeIntMap(effect12611RoundWeight);
		effect12602SoldierAdjustMap = str2SoldierTypeIntMap(effect12602SoldierAdjust);
		effect12601SoldierAdjustMap = str2SoldierTypeIntMap(effect12601SoldierAdjust);
		effect10066SoldierAdjustMap = str2SoldierTypeIntMap(effect10066SoldierAdjust);
		effect10067SoldierAdjustMap = str2SoldierTypeIntMap(effect10067SoldierAdjust);
		effect10068SoldierAdjustMap = str2SoldierTypeIntMap(effect10068SoldierAdjust);
		effect10069SoldierAdjustMap = str2SoldierTypeIntMap(effect10069SoldierAdjust);
		effect10070SoldierAdjustMap = str2SoldierTypeIntMap(effect10070SoldierAdjust);
		
		effect10061SoldierAdjustMap = str2SoldierTypeIntMap(effect10061SoldierAdjust);
		effect10062SoldierAdjustMap = str2SoldierTypeIntMap(effect10062SoldierAdjust);
		effect10063SoldierAdjustMap = str2SoldierTypeIntMap(effect10063SoldierAdjust);
		effect10064SoldierAdjustMap = str2SoldierTypeIntMap(effect10064SoldierAdjust);
		effect10065SoldierAdjustMap = str2SoldierTypeIntMap(effect10065SoldierAdjust);
		effect12561SoldierAdjustMap = str2SoldierTypeIntMap(effect12561SoldierAdjust);
		effect12576SoldierAdjustMap = str2SoldierTypeIntMap(effect12576SoldierAdjust);
		effect12574RoundWeightMap = str2SoldierTypeIntMap(effect12574RoundWeight);
		effect12573SoldierAdjustMap = str2SoldierTypeIntMap(effect12573SoldierAdjust);
		effect10060SoldierAdjustMap = str2SoldierTypeIntMap(effect10060SoldierAdjust);
		effect10059SoldierAdjustMap = str2SoldierTypeIntMap(effect10059SoldierAdjust);
		effect10058SoldierAdjustMap = str2SoldierTypeIntMap(effect10058SoldierAdjust);
		effect10057SoldierAdjustMap = str2SoldierTypeIntMap(effect10057SoldierAdjust);
		effect10056SoldierAdjustMap = str2SoldierTypeIntMap(effect10056SoldierAdjust);
		effect10055SoldierAdjustMap = str2SoldierTypeIntMap(effect10055SoldierAdjust);
		effect10054SoldierAdjustMap = str2SoldierTypeIntMap(effect10054SoldierAdjust);
		effect10053SoldierAdjustMap = str2SoldierTypeIntMap(effect10053SoldierAdjust);
		effect10052SoldierAdjustMap = str2SoldierTypeIntMap(effect10052SoldierAdjust);
		effect1325SoldierAdjustMap = str2SoldierTypeIntMap(effect1325SoldierAdjust);
		effect12518SoldierAdjustMap = str2SoldierTypeIntMap(effect12518SoldierAdjust);
		effect12517SoldierAdjustMap = str2SoldierTypeIntMap(effect12517SoldierAdjust);
		effect12515SoldierAdjustMap = str2SoldierTypeIntMap(effect12515SoldierAdjust);
		effect12514RoundWeightMap = str2SoldierTypeIntMap(effect12514RoundWeight);
		effect12512SoldierAdjustMap = str2SoldierTypeIntMap(effect12512SoldierAdjust);
		if(StringUtils.isNotEmpty(effect12469Adjust)){
			effect12469AdjustMap = ImmutableMap.copyOf(SerializeHelper.stringToMap(effect12469Adjust, Integer.class, Integer.class, "_", ","));
		}
		if (!StringUtils.isEmpty(effect12465Adjust)) {
			String[] strs = effect12465Adjust.trim().split(",");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (String str : strs) {
				String[] arr = str.split("_");
				map.put(SoldierType.valueOf(Integer.parseInt(arr[0])), Integer.valueOf(arr[1]));
			}
			effect12465AdjustMap = ImmutableMap.copyOf(map);
		}
		if (!StringUtils.isEmpty(effect12491RoundAdjust)) {
			String[] strs = effect12491RoundAdjust.trim().split(",");
			Map<Integer, Integer> map = new HashMap<>();
			for (String str : strs) {
				String[] arr = str.split("_");
				int min = Integer.valueOf(arr[0]);
				int max = Integer.valueOf(arr[1]);
				int val = Integer.valueOf(arr[2]);
				for(int i = min ;i<=max;i++){
					map.put(i, val);
				}
			}
			effect12491RoundAdjustMap = ImmutableMap.copyOf(map);
		}
		if (!StringUtils.isEmpty(effect12491SoldierAdjust)) {
			String[] strs = effect12491SoldierAdjust.trim().split(",");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (String str : strs) {
				String[] arr = str.split("_");
				map.put(SoldierType.valueOf(Integer.parseInt(arr[0])), Integer.valueOf(arr[1]));
			}
			effect12491SoldierAdjustMap = ImmutableMap.copyOf(map);
		}
		if (!StringUtils.isEmpty(effect12451DamageAdjust)) {
			String[] strs = effect12451DamageAdjust.trim().split(",");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (String str : strs) {
				String[] arr = str.split("_");
				map.put(SoldierType.valueOf(Integer.parseInt(arr[0])), Integer.valueOf(arr[1]));
			}
			effect12451DamageAdjustMap = ImmutableMap.copyOf(map);
		}
		if (!StringUtils.isEmpty(effect12414Adjust)) {
			String[] strs = effect12414Adjust.trim().split(",");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (String str : strs) {
				String[] arr = str.split("_");
				map.put(SoldierType.valueOf(Integer.parseInt(arr[0])), Integer.valueOf(arr[1]));
			}
			effect12414AdjustMap = ImmutableMap.copyOf(map);
		}
		if (!StringUtils.isEmpty(superDamageLimit)) {
			String[] arr = superDamageLimit.split("_");
			superDamageLimitMinMax = HawkTuples.tuple(Integer.parseInt(arr[0]), Integer.valueOf(arr[1]));
		}
		if (!StringUtils.isEmpty(effect11042Adjust)) {
			String[] strs = effect11042Adjust.trim().split(",");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (String str : strs) {
				String[] arr = str.split("_");
				map.put(SoldierType.valueOf(Integer.parseInt(arr[0])), Integer.valueOf(arr[1]));
			}
			effect11042AdjustMap = ImmutableMap.copyOf(map);
		}
		if (!StringUtils.isEmpty(effect11043Adjust)) {
			String[] strs = effect11043Adjust.trim().split(",");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (String str : strs) {
				String[] arr = str.split("_");
				map.put(SoldierType.valueOf(Integer.parseInt(arr[0])), Integer.valueOf(arr[1]));
			}
			effect11043AdjustMap = ImmutableMap.copyOf(map);
		}
		if (!StringUtils.isEmpty(effect11041DamageAdjust)) {
			String[] strs = effect11041DamageAdjust.trim().split(",");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (String str : strs) {
				String[] arr = str.split("_");
				map.put(SoldierType.valueOf(Integer.parseInt(arr[0])), Integer.valueOf(arr[1]));
			}
			effect11041DamageAdjustMap = ImmutableMap.copyOf(map);
		}
		if (!StringUtils.isEmpty(effect12363TargetWeight)) {
			String[] strs = effect12363TargetWeight.trim().split(",");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (String str : strs) {
				String[] arr = str.split("_");
				map.put(SoldierType.valueOf(Integer.parseInt(arr[0])), Integer.valueOf(arr[1]));
			}
			effect12363TargetWeightMap = ImmutableMap.copyOf(map);
		}
		
		if (!StringUtils.isEmpty(effect12362Adjust)) {
			String[] strs = effect12362Adjust.trim().split(",");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (String str : strs) {
				String[] arr = str.split("_");
				map.put(SoldierType.valueOf(Integer.parseInt(arr[0])), Integer.valueOf(arr[1]));
			}
			effect12362AdjustMap = ImmutableMap.copyOf(map);
		}
		if (!StringUtils.isEmpty(effect12361TargetWeight)) {
			String[] strs = effect12361TargetWeight.trim().split(",");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (String str : strs) {
				String[] arr = str.split("_");
				map.put(SoldierType.valueOf(Integer.parseInt(arr[0])), Integer.valueOf(arr[1]));
			}
			effect12361TargetWeightMap = ImmutableMap.copyOf(map);
		}
		if (!StringUtils.isEmpty(effect12361DamageAdjust)) {
			String[] strs = effect12361DamageAdjust.trim().split(",");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (String str : strs) {
				String[] arr = str.split("_");
				map.put(SoldierType.valueOf(Integer.parseInt(arr[0])), Integer.valueOf(arr[1]));
			}
			effect12361DamageAdjustMap = ImmutableMap.copyOf(map);
		}
		
		if (!StringUtils.isEmpty(effect12311DamageAdjust)) {
			String[] strs = effect12311DamageAdjust.trim().split("_");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (int i = 0; i < strs.length; i++) {
				map.put(SoldierType.valueOf(i + 1), Integer.valueOf(strs[i]));
			}
			effect12311DamageAdjustMap = ImmutableMap.copyOf(map);
		}
		if (!StringUtils.isEmpty(effect12202DamageAdjust)) {
			String[] strs = effect12202DamageAdjust.trim().split("_");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (int i = 0; i < strs.length; i++) {
				map.put(SoldierType.valueOf(i + 1), Integer.valueOf(strs[i]));
			}
			effect12202DamageAdjustMap = ImmutableMap.copyOf(map);
		}
		if (!StringUtils.isEmpty(effect12253SoldierAdjust)) {
			String[] strs = effect12253SoldierAdjust.trim().split(",");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (String str : strs) {
				String[] arr = str.split("_");
				map.put(SoldierType.valueOf(Integer.parseInt(arr[0])), Integer.valueOf(arr[1]));
			}
			effect12253SoldierAdjustMap = ImmutableMap.copyOf(map);
		}
		if (!StringUtils.isEmpty(effect12253HoldTimes)) {
			String[] strs = effect12253HoldTimes.trim().split(",");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (String str : strs) {
				String[] arr = str.split("_");
				map.put(SoldierType.valueOf(Integer.parseInt(arr[0])), Integer.valueOf(arr[1]));
			}
			effect12253HoldTimesMap = ImmutableMap.copyOf(map);
		}
		
		if (!HawkOSOperator.isEmptyString(effect12031EffectiveRound)) {
			String[] strs = effect12031EffectiveRound.split("_");
			effect12031EffectiveRoundArr = new int[] { Integer.valueOf(strs[0]), Integer.valueOf(strs[1]) };
		}
		if (!HawkOSOperator.isEmptyString(effect12036EffectiveRound)) {
			String[] strs = effect12036EffectiveRound.split("_");
			effect12036EffectiveRoundArr = new int[] { Integer.valueOf(strs[0]), Integer.valueOf(strs[1]) };
		}
		
		newbieVersionTimeValue = HawkTime.parseTime(newbieVersionTime);
		bubbleMaxTimes = new HashMap<Integer, Integer>();
		bubbleAwardItems = new HashMap<Integer, List<ItemInfo>>();
		if (!HawkOSOperator.isEmptyString(rondaAwardBubble)) {
			String[] strs = rondaAwardBubble.split(";");
			int type = Integer.valueOf(strs[0]);
			bubbleMaxTimes.put(type, Integer.valueOf(strs[2]));
			bubbleAwardItems.put(type, ItemInfo.valueListOf(strs[1]));
		}

		if (!HawkOSOperator.isEmptyString(buildAwardBubble)) {
			String[] strs = buildAwardBubble.split(";");
			int type = Integer.valueOf(strs[0]);
			bubbleMaxTimes.put(type, Integer.valueOf(strs[2]));
			bubbleAwardItems.put(type, ItemInfo.valueListOf(strs[1]));
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

		if (!HawkOSOperator.isEmptyString(buyEnergyCost)) {
			buyEnergyItemCost = new ArrayList<ItemInfo>();
			String[] itemStr = buyEnergyCost.split(",");
			for (String str : itemStr) {
				buyEnergyItemCost.add(ItemInfo.valueOf(str));
			}
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

		// 登录获得vip积分（未激活vip时获得积分_激活vip时倍数）
		if (!HawkOSOperator.isEmptyString(loginVipPointAdd)) {
			String[] strs = loginVipPointAdd.split("_");
			loginVipPoint = Integer.parseInt(strs[0]);
			loginVipBundle = Integer.parseInt(strs[1]);
		}

		// 玩家名字最大最小值
		if (!HawkOSOperator.isEmptyString(playerNameMinMax)) {
			String[] strs = playerNameMinMax.split("_");
			playerNameMin = Integer.parseInt(strs[0]);
			playerNameMax = Integer.parseInt(strs[1]);
		}

		// 玩家初始士兵
		if (!HawkOSOperator.isEmptyString(newSoldier)) {
			String[] strs = newSoldier.split(",");
			initSoldier = new int[strs.length * 2];
			for (int i = 0; i < strs.length; i++) {
				String[] one = strs[i].split("_");
				if (one.length != 2) {
					return false;
				}
				initSoldier[i * 2] = Integer.parseInt(one[0]);
				initSoldier[i * 2 + 1] = Integer.parseInt(one[1]);
			}
		}

		if (!HawkOSOperator.isEmptyString(outOfTownArea)) {
			cityOutsideAreas = new ArrayList<>();
			String[] areas = outOfTownArea.split(",");
			for (String area : areas) {
				cityOutsideAreas.add(Integer.valueOf(area.trim()));
			}
		}

		if (!HawkOSOperator.isEmptyString(outFireCost)) {
			outFireCostItems = new ArrayList<ItemInfo>();
			String[] items = outFireCost.split(",");
			for (String item : items) {
				outFireCostItems.add(ItemInfo.valueOf(item));
			}
		}
		
		// 藏兵时间数组
		if (!HawkOSOperator.isEmptyString(shelterRecall)) {
			String[] tmp = shelterRecall.split("_");
			for (int i = 0; i < tmp.length; i++) {
				shelterRecallArr[i] = Integer.parseInt(tmp[i]);
			}
		}

		// 10000_1001_500_172800
		if (!HawkOSOperator.isEmptyString(buyBuildQueue)) {
			String[] buyQueueStr = buyBuildQueue.split("_");
			if (buyQueueStr.length >= 4) {
				buyBuildQueueTimeLong = Integer.valueOf(buyQueueStr[3]);
				buyBuildQueueCostItem = new ItemInfo(Integer.valueOf(buyQueueStr[0]), Integer.valueOf(buyQueueStr[1]), Integer.valueOf(buyQueueStr[2]));
			}
		}

		wishCritRateMap = SerializeHelper.stringToMap(wishingCritRate, Integer.class, Integer.class, SerializeHelper.COLON_ITEMS, SerializeHelper.SEMICOLON_ITEMS);

		loveGift = SerializeHelper.stringToMap(friendGiftIntimacy, Integer.class, Integer.class, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.SEMICOLON_ITEMS);

		commanderEquipUnlockLvls = new HashMap<>();
		equipSlotPos = new HashMap<>();
		commanderEquipSlotSize = 0;
		if (!HawkOSOperator.isEmptyString(equipSlotLimit)) {
			String[] strArr = equipSlotLimit.split(",");
			for (String str : strArr) {
				String[] limit = str.split("_");
				int id = Integer.valueOf(limit[0]);
				int posType = Integer.valueOf(limit[1]);
				int lvl = Integer.valueOf(limit[2]);
				commanderEquipUnlockLvls.put(id, lvl);
				equipSlotPos.put(id, posType);
				commanderEquipSlotSize = Math.max(commanderEquipSlotSize, id);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(effectQueueForResUp)) {
			resUpEffectList = new ArrayList<>();
			String[] effects = effectQueueForResUp.split("_");
			for (String effect : effects) {
				resUpEffectList.add(Integer.valueOf(effect));
			}
		}
		
		if (!HawkOSOperator.isEmptyString(vipShopItem)) {
			String[] shopItemCountArr = vipShopItem.split(",");
			for (String shopItemCount : shopItemCountArr) {
				String[] levelCount = shopItemCount.split("_");
				vipShopItems.put(Integer.valueOf(levelCount[0]), Integer.valueOf(levelCount[1]));
			}
		}
		
		if (!HawkOSOperator.isEmptyString(travelShopCrystalRefreshCost)) {
			String[] costArray = travelShopCrystalRefreshCost.split("_");
			int[] intCostArray = new int[costArray.length];			
			for (int i = 0; i < costArray.length; i++) {
				intCostArray[i] =  Integer.parseInt(costArray[i]);
			}
			
			travelShopCrystalRefreshCostArray = intCostArray;
		} else {
			throw new RuntimeException("travelShopCrystalRefreshCost must not null or empty");
		}
		
		if (!HawkOSOperator.isEmptyString(travelShopRefreshTime)) {
			String[] timeArray = travelShopRefreshTime.split("_");
			int[] intTimeArray = new int[timeArray.length];			
			for (int i = 0; i < timeArray.length; i++) {
				intTimeArray[i] =  Integer.parseInt(timeArray[i]);
			}
			
			travelShopRefreshTimeArray = intTimeArray;
		} else {
			throw new RuntimeException("travelShopRefreshTime must not null or empty");
		}
		
		if (!HawkOSOperator.isEmptyString(superTimeRescueCd)) {
			String[] timeCds = superTimeRescueCd.split(",");
			for (String timeCd : timeCds) {
				superTimeRescueCdList.add(Integer.valueOf(timeCd.trim()));
			}
		}
		
		if (!HawkOSOperator.isEmptyString(qqStartUp)) {
			String[] array = qqStartUp.split(",");
			for (String info : array) {
				String[] items = info.split("_");
				if (items.length != 2) {
					return false;
				}
				
				platEffTable.put("qq", Integer.parseInt(items[0]), Integer.parseInt(items[1]));
			}
		}
		
		if (!HawkOSOperator.isEmptyString(wxStartUp)) {
			String[] array = wxStartUp.split(",");
			for (String info : array) {
				String[] items = info.split("_");
				if (items.length != 2) {
					return false;
				}
				
				platEffTable.put("wx", Integer.parseInt(items[0]), Integer.parseInt(items[1]));
			}
		}
		
		if (!HawkOSOperator.isEmptyString(svipStartUp)) {
			String[] array = svipStartUp.split(",");
			for (String info : array) {
				String[] items = info.split("_");
				if (items.length != 2) {
					return false;
				}
				
				platEffTable.put("svip", Integer.parseInt(items[0]), Integer.parseInt(items[1]));
			}
		}
		
		if (!HawkOSOperator.isEmptyString(resOutputBuffAll)) {
			String[] array = resOutputBuffAll.split(",");
			for (String info : array) {
				int effType = Integer.parseInt(info);
				allResOutputBuffList.add(effType);
				resOutputBuffToAllSet.add(effType);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(resOutputBuff1007)) {
			String[] array = resOutputBuff1007.split(",");
			for (String info : array) {
				int effType = Integer.parseInt(info);
				allResOutputBuffList.add(effType);
				resOutputBuffGoldoreSet.add(effType);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(resOutputBuff1008)) {
			String[] array = resOutputBuff1008.split(",");
			for (String info : array) {
				int effType = Integer.parseInt(info);
				allResOutputBuffList.add(effType);
				resOutputBuffOilSet.add(effType);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(resOutputBuff1009)) {
			String[] array = resOutputBuff1009.split(",");
			for (String info : array) {
				int effType = Integer.parseInt(info);
				allResOutputBuffList.add(effType);
				resOutputBuffSteelSet.add(effType);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(resOutputBuff1010)) {
			String[] array = resOutputBuff1010.split(",");
			for (String info : array) {
				int effType = Integer.parseInt(info);
				allResOutputBuffList.add(effType);
				resOutputBuffTombarthiteSet.add(effType);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(grantItemForUnlockMecha)) {
			grantItemForUnlockMechaList = ItemInfo.valueListOf(grantItemForUnlockMecha, ";");
		} else {
			grantItemForUnlockMechaList = Collections.emptyList();
		}
		
		if (!HawkOSOperator.isEmptyString(crossCostItems)) {
			crossCostItemInfos = ItemInfo.valueListOf(crossCostItems);
		} else {
			crossCostItemInfos = Collections.emptyList();
		}
		
		{
			heroTalentChangeCostList = ItemInfo.valueListOf(heroTalentChangeCost);
			// 100#1_30|2_50|3_30|4_10|5_5,1000#1_30|2_50|3_30|4_10|5_5,1000000#1_80|2_50|3_30|4_10|5_5
			Iterable<String> levelat = Splitter.on(",").omitEmptyStrings().trimResults().split(heroTalentLevelupRateProbability);
			TreeMap<Integer, List<HawkTuple2WeightObj<Integer>>> map2 = new TreeMap<>();
			for (String levelstr : levelat) {
				String[] arr = levelstr.split("#");
				int level = NumberUtils.toInt(arr[0]);
				String wstr = arr[1];
				map2.put(level, new ArrayList<>());
				Splitter.on("|").omitEmptyStrings().trimResults().split(wstr).forEach(str -> {
					String[] wsarr = str.split("_");
					HawkTuple2WeightObj<Integer> slot = new HawkTuple2WeightObj<>(NumberUtils.toInt(wsarr[0]), NumberUtils.toInt(wsarr[1]));
					map2.get(level).add(slot);
				});
			}
			heroTalentLevelupRateProbabilityMap.putAll(map2);
		}
		
		if (!HawkOSOperator.isEmptyString(activeShareReward)) {
			activeShareAwardItems = ItemInfo.valueListOf(activeShareReward);
		} else {
			activeShareAwardItems = Collections.emptyList();
		}
		
		if (HawkOSOperator.isEmptyString(wechatFriendsStartTime) || HawkOSOperator.isEmptyString(wechatFriendsEndTime)) {
			return false;
		}
		
		wechatFriendsStartTimeLong = HawkTime.parseTime(wechatFriendsStartTime);
		wechatFriendsEndTimeLong = HawkTime.parseTime(wechatFriendsEndTime);
		
		
		if (!HawkOSOperator.isEmptyString(playerLossPushNotice)) {
			playerLossPushNoticeList = new ArrayList<HawkTuple3<Integer, Integer, List<Integer>>>();
			String[] array = playerLossPushNotice.split(";");
			for (String info : array) {
				List<Integer> list = new ArrayList<Integer>();
				String[] params = info.split("_");
				if (params.length < 2) {
					return false;
				}
				for(int i=0;i<params.length;i++){
					if(i>=2){
						list.add(Integer.parseInt(params[i]));
					}
				}
				HawkTuple3<Integer, Integer, List<Integer>> tupe3 = new 
						 HawkTuple3<Integer, Integer, List<Integer>>
							(Integer.parseInt(params[0]), Integer.parseInt(params[1]), list);
				playerLossPushNoticeList.add(tupe3);
			}
		}else{
			playerLossPushNoticeList = Collections.emptyList();
		}
		
		if (!HawkOSOperator.isEmptyString(pushServers)) {
			pushServerList = new ArrayList<>();
			String[] array = pushServers.split("_");
			for(String str : array){
				pushServerList.add(str);
			}
		}else{
			pushServerList = Collections.emptyList();
		}
		
		if (!HawkOSOperator.isEmptyString(resCollectExtraDropLevelLimit)) {
			String[] split = resCollectExtraDropLevelLimit.split("_");
			this.resCollectExtraDropLevelLimitMin = Integer.parseInt(split[0]);
			this.resCollectExtraDropLevelLimitMax = Integer.parseInt(split[1]);
		}
		
		if (!HawkOSOperator.isEmptyString(newPushGiftStartTime)) {
			newPushGiftStartTimeValue = HawkTime.parseTime(newPushGiftStartTime);
		}
		
		List<Integer> equipResearchShowUnlockList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(equipResearchShowUnlock)) {
			String[] split = equipResearchShowUnlock.split("_");
			for (int i = 0; i < split.length; i++) {
				equipResearchShowUnlockList.add(Integer.valueOf(split[i]));
			}
		}
		this.equipResearchShowUnlockList = equipResearchShowUnlockList;
		if (!HawkOSOperator.isEmptyString(crossEntitiesOptimizeServers)) {
			this.crossEntitiesOptimizeServerList = SerializeHelper.stringToList(String.class, crossEntitiesOptimizeServers, SerializeHelper.ATTRIBUTE_SPLIT);
		}
		
		if (!HawkOSOperator.isEmptyString(cavilliPseudorandom)) {
			String[] params = cavilliPseudorandom.split("_");
			dailyGiftBoxItemId = Integer.parseInt(params[0]);
			dailyGiftBoxOpenTimes = Integer.parseInt(params[1]);
			dailyGiftMustRewardItemId = Integer.parseInt(params[2]);
			dailyGiftRandomAwardId = Integer.parseInt(params[3]);
		}
		
        mechaRepairItemList = ItemInfo.valueListOf(mechaRepairItem);

        if(StringUtils.isNotEmpty(effect1639Parametric)){
        	String[] params = effect1639Parametric.split("_");
        	effect1639ParametricNum = HawkTuples.tuple(Integer.parseInt(params[0]), Integer.parseInt(params[1]), Integer.parseInt(params[2]));
        }
        if(StringUtils.isNotEmpty(effect1639SoldierPer)){
        	Map<SoldierType, Integer> xmap = new HashMap<>();
        	String[] arr = effect1639SoldierPer.trim().split(",");
    		for (String str : arr) {
    			String[] tv = str.split("_");
    			xmap.put(SoldierType.valueOf(Integer.valueOf(tv[0])), Integer.valueOf(tv[1]));
    		}
        	effect1639SoldierPerMap = ImmutableMap.copyOf(xmap);
        }
        recordItemList = SerializeHelper.stringToList(Integer.class, recordItems, "_");
        
        travelShopFriendlyMap = SerializeHelper.stringToMap(travelShopFriendly, Integer.class, Integer.class, SerializeHelper.ATTRIBUTE_SPLIT, SerializeHelper.BETWEEN_ITEMS);
		String[] condition = airdropUnlock.split("_");
		if (condition.length >= 2) {
			airdropUnlockBuildType = Integer.valueOf(condition[0]);
		}

		if (!HawkOSOperator.isEmptyString(spaceProp)) {
			String[] lkbarray = spaceProp.split(",");
			spacePropLKB = new HashMap<>();
			for (String lkb : lkbarray) {
				String[] lkbstr = lkb.split("_");
				int L = Integer.valueOf(lkbstr[0]);
				int K = Integer.valueOf(lkbstr[1]);
				int B = Integer.valueOf(lkbstr[2]);
				spacePropLKB.put(K, B);
			}
		}

		buildNoList = SerializeHelper.stringToList(Integer.class, verifyBuildNo, ",");
		
		if (!HawkOSOperator.isEmptyString(dailyLogin)) {
			String[] infos = dailyLogin.split(",");
			superVipDailyLoginScore = Integer.parseInt(infos[0]);
			superVipDailyLoginScoreStep = Integer.parseInt(infos[1]);
		}
		
		if (HawkOSOperator.isEmptyString(PropExchange)) {
			propExchangeParamMap = Collections.emptyMap();
		} else {
			propExchangeParamMap = new HashMap<>();
			String[] paramsArr = PropExchange.split(";");
			for (String params : paramsArr) {
				String[] propParam = params.split(",");
				if (propParam.length < 3) {
					return false;
				}
				ItemInfo itemInfo = ItemInfo.valueOf(propParam[1]);
				if (itemInfo.getCount() <= 0) {
					return false;
				}
				ItemInfo backItemInfo = ItemInfo.valueOf(propParam[2]);
				if (backItemInfo.getCount() <= 0) {
					return false;
				}
				Object[] propParamArr = new Object[3];
				propParamArr[0] = Integer.valueOf(propParam[0]);   // 类型
				propParamArr[1] = (int)itemInfo.getCount();        // 使用道具的个数
				propParamArr[2] = propParam[2];                    // 返还的物品
				propExchangeParamMap.put(itemInfo.getItemId(), propParamArr);
			}
		}
		
		if (HawkOSOperator.isEmptyString(statueCoordinates)) {
			statueCoordinatesArr[0] = 0;
			statueCoordinatesArr[1] = 0;
		} else {
			String[] coordArr = statueCoordinates.split("_");
			statueCoordinatesArr[0] = Integer.parseInt(coordArr[0].trim());
			statueCoordinatesArr[1] = Integer.parseInt(coordArr[1].trim());
		}
		if(HawkOSOperator.isEmptyString(isDressGodOpenTime)){
			isDressGodOpenTimeValue = Long.MAX_VALUE;
		}else {
			isDressGodOpenTimeValue = HawkTime.parseTime(isDressGodOpenTime);
		}
        
		
		if (!StringUtils.isEmpty(this.bossEnemyIdList185)) {
			Map<Integer,List<Integer>> bossEnemyId185Temp = new HashMap<>();
			Map<Integer,Integer> bossDailyLimit185Temp = new HashMap<>();
			String[] groupStrs = bossEnemyIdList185.trim().split(SerializeHelper.SEMICOLON_ITEMS);
			String[] limitStrs = this.bossDailyLootTimeLimit185.trim().split(SerializeHelper.SEMICOLON_ITEMS);
			if(groupStrs.length != limitStrs.length){
				return false;
			}
			
			for(int i=0;i<groupStrs.length;i++){
				String gstr = groupStrs[i];
				String lstr = limitStrs[i];
				String[] garr = gstr.split(SerializeHelper.ATTRIBUTE_SPLIT);
				List<Integer> boosList = new ArrayList<>();
				for(String gparam : garr){
					boosList.add(Integer.parseInt(gparam));
				}
				bossEnemyId185Temp.put(i, ImmutableList.copyOf(boosList));
				bossDailyLimit185Temp.put(i, Integer.parseInt(lstr));
			}
			this.bossEnemyId185 = ImmutableMap.copyOf(bossEnemyId185Temp);
			this.bossDailyLimit185 = ImmutableMap.copyOf(bossDailyLimit185Temp);
		}

		if (!HawkOSOperator.isEmptyString(givenItemCostEvent)) {
			Set<Integer> tmp = new HashSet<>();
			String[] array = givenItemCostEvent.split(",");
			for (String str : array) {
				int itemId = Integer.parseInt(str);
				tmp.add(itemId);
			}
			givenItemCostEventSet = tmp;
		}
		
		
		//限制awardId = 1,2,3|243_343
		//打野每日限定数量 = 1231_10,1231_10|234_10,232_100
		if(!HawkOSOperator.isEmptyString(this.blazeMedalAward) &&
				!HawkOSOperator.isEmptyString(this.blazeMedalLimit)){
			Map<Integer,List<Integer>> blazeMedalAwardMapTemp = new HashMap<>();
			Map<Integer,List<HawkTuple2<Integer, Integer>>> blazeMedalLimitMapTemp = new HashMap<>();
			String[] arr1 = this.blazeMedalAward.split(SerializeHelper.ELEMENT_SPLIT);
			String[] arr2 = this.blazeMedalLimit.split(SerializeHelper.ELEMENT_SPLIT);
			if(arr1.length != arr2.length){
				return false;
			}
			for(int i=0; i< arr2.length;i++){
				String[] param1= arr1[i].split(SerializeHelper.BETWEEN_ITEMS);
				String[] param2= arr2[i].split(SerializeHelper.BETWEEN_ITEMS);
				
				List<Integer> list1 = new ArrayList<>();
				List<HawkTuple2<Integer, Integer>> list2 = new ArrayList<>();
				
				for(String p1 : param1){
					list1.add(Integer.parseInt(p1));
					
				}
				for(String p2 : param2){
					String[] p2arr = p2.split(SerializeHelper.ATTRIBUTE_SPLIT);
					list2.add(HawkTuples.tuple(Integer.parseInt(p2arr[0]), Integer.parseInt(p2arr[1])));
				}
				blazeMedalAwardMapTemp.put(i, list1);
				blazeMedalLimitMapTemp.put(i, list2);
			}
			
			this.blazemedalAwardMap = ImmutableMap.copyOf(blazeMedalAwardMapTemp);
			this.blazeMedalLimitMap = ImmutableMap.copyOf(blazeMedalLimitMapTemp);
		}
		
        instance = this;
        return super.assemble();
	}
	
	private ImmutableMap<SoldierType, Integer> str2SoldierTypeIntMap(String adjustStr){
		if (!StringUtils.isEmpty(adjustStr)) {
			String[] strs = adjustStr.trim().split(",");
			Map<SoldierType, Integer> map = new HashMap<>();
			for (String str : strs) {
				String[] arr = str.split("_");
				map.put(SoldierType.valueOf(Integer.parseInt(arr[0])), Integer.valueOf(arr[1]));
			}
			return ImmutableMap.copyOf(map);
		}
		return ImmutableMap.of();
	}
	
	public int[] getStatueCoordinates() {
		return statueCoordinatesArr;
	}
	
	public Object[] getPropExchangeParam(int itemId) {
		return propExchangeParamMap.get(itemId);
	}
	
	public int getIntegral() {
		return integral;
	}

	public int getRewardExperience() {
		return rewardExperience;
	}

	public int getLoginMaximum() {
		return loginMaximum;
	}

	public int getSuperVipDailyLoginScore() {
		return superVipDailyLoginScore;
	}

	public int getSuperVipDailyLoginScoreStep() {
		return superVipDailyLoginScoreStep;
	}

	public List<Integer> getBuildNoList() {
		return buildNoList;
	}

	public List<ItemInfo> getMechaRepairItemList() {
		return mechaRepairItemList.stream().map(e -> e.clone()).collect(Collectors.toList());
	}
	public long getWechatFriendsStartTimeLong() {
		return wechatFriendsStartTimeLong;
	}

	public long getWechatFriendsEndTimeLong() {
		return wechatFriendsEndTimeLong;
	}
	
	public String getWechatFriendsStartTime() {
		return wechatFriendsStartTime;
	}

	public String getWechatFriendsEndTime() {
		return wechatFriendsEndTime;
	}

	public int randomHeroTalentLevelupRate(int seed){
		List<HawkTuple2WeightObj<Integer>> all = heroTalentLevelupRateProbabilityMap.higherEntry(seed).getValue();
		HawkTuple2WeightObj<Integer> randomWeightObject = HawkRand.randomWeightObject(all);
		return randomWeightObject.first;
	}
	
	public String getJeroTalentChangeCost(int count) {
		count = Math.min(count, heroTalentChangeCostList.size() - 1);
		return heroTalentChangeCostList.get(count).toString();
	}
	
	public List<ItemInfo> getGrantItemForUnlockMechaList() {
		return grantItemForUnlockMechaList.stream().map(e -> e.clone()).collect(Collectors.toList());
	}

	public Set<Integer> getResOutputBuffAllSet() {
		return resOutputBuffToAllSet;
	}

	public Set<Integer> getResOutputBuffGoldoreSet() {
		return resOutputBuffGoldoreSet;
	}

	public Set<Integer> getResOutputBuffOilSet() {
		return resOutputBuffOilSet;
	}

	public Set<Integer> getResOutputBuffSteelSet() {
		return resOutputBuffSteelSet;
	}

	public Set<Integer> getResOutputBuffTombarthiteSet() {
		return resOutputBuffTombarthiteSet;
	}
	
	public List<Integer> getCityOutsideAreas() {
		if (cityOutsideAreas != null) {
			return Collections.unmodifiableList(cityOutsideAreas);
		}
		return Collections.emptyList();
	}

	@Override
	protected boolean checkValid() {
		if (speedUpTimeLKB == null) {
			return false;
		}
		
		if (buyResCostLKB == null) {
			return false;
		}
		
		if (revengeShopRefresh <= 0 || revengeShopDuration <= 0 || revengeShopRefresh < revengeShopDuration) {
			return false;
		}
		
		if (revengeShopTriggerTime <= 0 || revengeShopTriggerNum <= 0 || revengeShopTroopsLevel <= 0) {
			return false;
		}

		if(spacePropLKB == null){
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
	
	public double getEquipQueueTimeWeight() {
		return equipQueueTimeWeight;
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

	public Map<Integer, Integer> getWishingCritRate() {
		return Collections.unmodifiableMap(wishCritRateMap);
	}

	public int getTavernRefreshTime() {
		return tavernRefreshTime;
	}

	public List<ItemInfo> getOutFireCostItems() {
		if (outFireCostItems == null) {
			return Collections.emptyList();
		}
		
		return outFireCostItems.stream().map(e -> e.clone()).collect(Collectors.toList());
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

	public List<ItemInfo> getBuyEnergyItemCost() {
		if (buyEnergyItemCost != null) {
			return buyEnergyItemCost.stream().map(e -> e.clone()).collect(Collectors.toList());
		}
		
		return Collections.emptyList();
	}

	public List<ItemInfo> getBubbleAwardItems(int type) {
		if (!bubbleAwardItems.containsKey(type)) {
			return Collections.emptyList();
		}

		return bubbleAwardItems.get(type).stream().map(e -> e.clone()).collect(Collectors.toList());
	}

	public ItemInfo randomBubbleAwardItem(int type) {
		List<ItemInfo> items = getBubbleAwardItems(type);
		if (!items.isEmpty()) {
			return items.get(HawkRand.randInt(items.size() - 1));
		}

		return new ItemInfo();
	}

	public Set<Integer> getBubbleTypes() {
		return Collections.unmodifiableSet(bubbleAwardItems.keySet());
	}

	public int getScienceFreeTime() {
		return scienceFreeTime;
	}

	public int getPushEffectiveTime() {
		return pushEffectiveTime;
	}

	public int getFriendGift() {
		return friendGift;
	}

	public int getFriendUpperLimit() {
		return friendUpperLimit;
	}

	public int getFriendApplyLimit() {
		return friendApplyLimit;
	}

	/**
	 * 获取亲密度好友礼包
	 * @param love 亲密度
	 * @return
	 */
	public int getLoveGiftId(int love) {
		int key = 0;
		for (int findKey : loveGift.keySet()) {
			if (love >= findKey) {
				key = findKey;
			}
		}
		return loveGift.get(key);
	}

	public int getFriendMinimumValue() {
		return friendMinimumValue;
	}

	public int getFriendRecommendCount() {
		return friendRecommendCount;
	}

	public String getFriendGiftIntimacy() {
		return friendGiftIntimacy;
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

	public int getAllianceTransfer() {
		return allianceTransfer;
	}

	public int getUnlockTalentLine2NeedCityLevel() {
		return unlockTalentLine2NeedCityLevel;
	}

	public int getUnlockTalentLine3NeedCityLevel() {
		return unlockTalentLine3NeedCityLevel;
	}

	public int getEffect366LinkToAwardId() {
		return effect366LinkToAwardId;
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

	public String getAllSeverBuff() {
		return allSeverBuff;
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

	public String getOutOfTownArea() {
		return outOfTownArea;
	}

	public String getOutFireCost() {
		return outFireCost;
	}

	public String getBuyBuildQueue() {
		return buyBuildQueue;
	}

	public String getRecallPushTimeInterval() {
		return recallPushTimeInterval;
	}

	public String getRondaAwardBubble() {
		return rondaAwardBubble;
	}

	public String getBuildAwardBubble() {
		return buildAwardBubble;
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

	public Map<Integer, Integer> getBubbleMaxTimes() {
		return Collections.unmodifiableMap(bubbleMaxTimes);
	}

	public ImmutableSortedMap<Long, HawkTuple2<Long, Long>> getSpeedUpTimeLKB() {
		return speedUpTimeLKB;
	}

	public ImmutableSortedMap<Long, HawkTuple2<Long, Long>> getBuyResCostLKB() {
		return buyResCostLKB;
	}

	public int[] getInitSoldier() {
		return initSoldier;
	}

	public Map<Integer, Integer> getLoveGift() {
		return loveGift;
	}

	public int getShareTime() {
		return shareTime;
	}

	public long getAllianceInvitationOverTime() {
		return allianceInvitationOverTime * 1000l;
	}
	
	public int getEquipMaxQuality() {
		return equipMaxQuality;
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

	public List<Integer> getResUpEffectList() {
		if (resUpEffectList != null) {
			return Collections.unmodifiableList(resUpEffectList);
		} 
		
		return Collections.emptyList();
	}

	public String getEffectQueueForResUp() {
		return effectQueueForResUp;
	}
	
	public int getVipShopItemCount(int vipLevel) {
		if (!vipShopItems.containsKey(vipLevel)) {
			return 0;
		}
		
		return vipShopItems.get(vipLevel);
	}

	public int getIniTroopTeamNum() {
		return iniTroopTeamNum;
	}

	public int getQuickBreakUpperLimit() {
		return quickBreakUpperLimit;
	}

	public int getTravelShopFreeRefreshTimes() {
		return travelShopFreeRefreshTimes;
	}

	public int[] getTravelShopCrystalRefreshCostArray() {
		return travelShopCrystalRefreshCostArray;
	}
	
	public int getMaxTravelShopRefreshTimes() {
		return travelShopFreeRefreshTimes + travelShopCrystalRefreshCostArray.length;
	}

	public int[] getTravelShopRefreshTimeArray() {
		return travelShopRefreshTimeArray;
	}

	public int getItemSpeedUpTimeThresholdValue() {
		return itemSpeedUpTimeThresholdValue;
	}

	public int getTravelGiftBuyTimesLimit() {
		return travelGiftBuyTimesLimit;
	}

	public int getTravelGiftActivateInitProb() {
		return travelGiftActivateInitProb;
	}

	public int getTravelGiftActivateAddProb() {
		return travelGiftActivateAddProb;
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

	public int getFoggyBoxMaxSpace() {
		return foggyBoxMaxSpace;
	}

	public int getAllianceCareHelpSustainTime() {
		return allianceCareHelpSustainTime;
	}

	public int getAllianceCareHelpQuantityUpLimit() {
		return allianceCareHelpQuantityUpLimit;
	}

	public int getPhysicalPowerReturnCoe() {
		return physicalPowerReturnCoe;
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

	public int getSpecialTravelGiftBuyTimesLimit() {
		return specialTravelGiftBuyTimesLimit;
	}

	public int getSpecialTravelGiftActivateInitProb() {
		return specialTravelGiftActivateInitProb;
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

	public int getSignatureVipLimit() {
		return signatureVipLimit;
	}

	public int getSignatureLengthLimit() {
		return signatureLengthLimit;
	}

	public int getFoggyAttackMaxTimes() {
		return foggyAttackMaxTimes;
	}

	public int getGainCrystalLimitByUseItem() {
		return gainCrystalLimitByUseItem;
	}

	public int getDailyBuyEnergyTimesLimit() {
		return dailyBuyEnergyTimesLimit;
	}

	public String getSuperTimeRescueCd() {
		return superTimeRescueCd;
	}

	public List<Integer> getSuperTimeRescueCdList() {
		return superTimeRescueCdList;
	}

	public Map<Integer, Integer> getSpacePropLKB() {
		return spacePropLKB;
	}

	/**
	 * 获取启动特权作用号
	 * 
	 * @param effectMap
	 * @param startUpType
	 */
	public void assemblePlatEffectMap(Map<Integer, Integer> effectMap, String startUpType) {
		effectMap.clear();
		Map<Integer, Integer> effMap = platEffTable.row(startUpType);
		if (effMap == null) {
			return;
		}
		
		for (int effId : effMap.keySet()) {
			effectMap.put(effId, effMap.get(effId));
		}
	}

	public List<Integer> getAllResOutputBuffList() {
		return allResOutputBuffList;
	}

	public void setAllResOutputBuffList(List<Integer> allResOutputBuffList) {
		this.allResOutputBuffList = allResOutputBuffList;
	}

	public int getUnregFriendShowCount() {
		return qqFriend;
	}

	public int getFriendInviteExpireTime() {
		return qqFriendTime;
	}

	public int getEffect377LinkToAwardId() {
		return effect377LinkToAwardId;
	}

	public boolean isOpenPlatformPrivilege() {
		return openPlatformPrivilege;
	}

	public boolean isOpenOneKeyCreateGroup() {
		return openOneKeyCreateGroup;
	}

	public boolean isOpenFriendsInvite() {
		return openFriendsInvite;
	}

	public boolean isOpenPlatformFriends() {
		return openPlatformFriends;
	}

	public boolean isOpenShare() {
		return openShare;
	}

	public boolean isOpenPlatStrategy() {
		return openPlatStrategy;
	}

	public boolean isOpenLoginWay() {
		return openLoginWay;
	}

	public String getOpenPlat() {
		return openPlat;
	}

	public String getOpenPlatformPrivilegeIds() {
		return openPlatformPrivilegeIds;
	}

	public String getIos_openPlat() {
		return ios_openPlat;
	}

	public String getIos_openPlatformPrivilegeIds() {
		return ios_openPlatformPrivilegeIds;
	}

	public boolean isOpenWithQq() {
		return openWithQq;
	}

	public boolean isOpenWithWechat() {
		return openWithWechat;
	}

	public boolean isOpenPlatformFriendsGift() {
		return openPlatformFriendsGift;
	}

	public boolean isOpenStrategy() {
		return openStrategy;
	}

	public int getCrRewardLimit() {
		return crRewardLimit;
	}

	public List<ItemInfo> getCrossCostItemInfos() {
		return crossCostItemInfos;
	}

	public int getCrossCityLevel() {
		return crossCityLevel;
	}
	
	public boolean isOpenQuestShare() {
		return openQuestShare;
	}

	public String getShareReward(){
		return shareReward;
	}

	public int getVersionUpRewardMailId() {
		return versionUpRewardMailId;
	}

	public int getExtraSpyRadarLv() {
		return extraSpyRadarLv;
	}

	public boolean isOpenGuildVoice() {
		return openGuildVoice;
	}

	public int getDieBecomeInjury() {
		return dieBecomeInjury;
	}

	public long getAutoAtkMonsterCD() {
		return startAutoTime * 1000L;
	}
	
	public int getLitaRatio(){
		return litaRatio;
	}

	public String getHeroTalentChangeCost() {
		return heroTalentChangeCost;
	}

	public String getHeroTalentLevelupRateProbability() {
		return heroTalentLevelupRateProbability;
	}

	public int getRecruitslevelLimit() {
		return recruitslevelLimit;
	}

	public int getDayclaimLimt() {
		return dayclaimLimt;
	}

	public int getTotalclaimLimt() {
		return totalclaimLimt;
	}

	public int getRescueDuration() {
		return rescueDuration;
	}

	public long getRevengeShopRefresh() {
		return revengeShopRefresh * 1000L;
	}

	public long getRevengeShopDuration() {
		return revengeShopDuration * 1000L;
	}

	public int getRevengeShopTriggerTime() {
		return revengeShopTriggerTime;
	}

	public int getRevengeShopTriggerNum() {
		return revengeShopTriggerNum;
	}

	public int getRevengeShopTroopsLevel() {
		return revengeShopTroopsLevel;
	}

	public int getTimeLimitShopTriggerTimes() {
		return timeLimitShopTriggerTimes;
	}

	public String getHeroHide() {
		return heroHide;
	}

	public String getHeroSkinHide() {
		return heroSkinHide;
	}

	public int getRevengeShopOpen() {
		return revengeShopOpen;
	}

	public int getUnlockTalentLine4NeedCityLevel() {
		return unlockTalentLine4NeedCityLevel;
	}

	public int getUnlockTalentLine5NeedCityLevel() {
		return unlockTalentLine5NeedCityLevel;
	}
	
	public int getUnlockTalentLine6NeedCityLevel() {
		return unlockTalentLine6NeedCityLevel;
	}
	
	public int getUnlockTalentLine7NeedCityLevel() {
		return unlockTalentLine7NeedCityLevel;
	}
	
	public int getUnlockTalentLine8NeedCityLevel() {
		return unlockTalentLine8NeedCityLevel;
	}

	public double getHeroTrialQueueTimeWeight() {
		return heroTrialQueueTimeWeight;
	}

	public String getActiveShareReward() {
		return activeShareReward;
	}

	public List<ItemInfo> getActiveShareAwardItems() {
		return activeShareAwardItems.stream().map(e -> e.clone()).collect(Collectors.toList());
	}

	public int getEffect1508Prob() {
		return effect1508Prob;
	}

	public int getDuelInitPower() {
		return duelInitPower;
	}

	public int getDuelPowerCheckTimeInterval() {
		return duelPowerCheckTimeInterval;
	}

	public String getDuelPowerRankInterval() {
		return duelPowerRankInterval;
	}

	public int getDuelPowerOnceAddLimit() {
		return duelPowerOnceAddLimit;
	}

	public int getEffect1512Prob() {
		return effect1512Prob;
	}

	public int getEffect1512DisDecayCof() {
		return effect1512DisDecayCof;
	}

	public int getEffect1512DisDecayMin() {
		return effect1512DisDecayMin;
	}

	public int getEffect1121Prob() {
		return effect1121Prob;
	}

	public int getSecPasswordExamineTime() {
		return secPasswordExamineTime;
	}

	public String getScienceOpenServers() {
		return scienceOpenServers;
	}

	public int getEffect1524Maximum() {
		return effect1524Maximum;
	}

	public String getEffect1525Power() {
		return effect1525Power;
	}

	public int getHateRankShowNum() {
		return hateRankShowNum;
	}

	public int getHateRankNumLimit() {
		return hateRankNumLimit;
	}

	public int getHatePowerLoseLimit() {
		return hatePowerLoseLimit;
	}

	public int getEffect1312Maxinum() {
		return effect1312Maxinum;
	}

	public int getEffect1315Maxinum() {
		return effect1315Maxinum;
	}

	public int getEffect1528Prob() {
		return effect1528Prob;
	}

	public int getEffect1528Power1() {
		return effect1528Power1;
	}

	public int getEffect1528Power2() {
		return effect1528Power2;
	}

	public int getEffect1529Maximum() {
		return effect1529Maximum;
	}

	public int getEffect1530Maximum() {
		return effect1530Maximum;
	}

	public boolean isHonorSevenOpen() {
		return honorSevenOpen == 1;
	}

	public String getEffect1535mum() {
		return effect1535mum;
	}

	public int getEffect1535power() {
		return effect1535power;
	}

	public long getSnwballAtkFireTime() {
		return snowballAtkFireTime * 1000L;
	}

	public int getEffect1541Power() {
		return effect1541Power;
	}

	public int getEffect1541Maxinum() {
		return effect1541Maxinum;
	}

	public int getEffect1540Prob1() {
		return effect1540Prob1;
	}

	public int getEffect1540Prob2() {
		return effect1540Prob2;
	}

	public int getEffect1543Power() {
		return effect1543Power;
	}

	public int getEffect1546Power() {
		return effect1546Power;
	}
	
	public int getEffect1547Maxinum(){
		return effect1547Maxinum;
	}

	public int getEffect1413SuperiorLimit() {
		return effect1413SuperiorLimit;
	}

	public long getAccountCancReqCd() {
		return accountCancReqCd * 1000L;
	}

	public long getAccountCancContinue() {
		return accountCancContinue * 1000L;
	}

	
	public int getEffect1548Maxinum() {
		return effect1548Maxinum;
	}


	public int getEffect1549Maxinum() {
		return effect1549Maxinum;
	}

	public int getPlayerLossDays() {
		return playerLossDays;
	}

	public int getPlayerLossRecommendNum() {
		return playerLossRecommendNum;
	}


	public List<HawkTuple3<Integer, Integer, List<Integer>>> getPlayerLossPushNoticeList() {
		return playerLossPushNoticeList;
	}


	public int getPlayerLossPushCD() {
		return PlayerLossPushCD;
	}

	public int getPlayerFriendLossPushReceiveNum() {
		return playerFriendLossPushReceiveNum;
	}

	public int getPlayerFriendLossDaysPush() {
		return playerFriendLossDaysPush;
	}
	
	public int getPushWorkFlag() {
		return pushWorkFlag;
	}

	public List<String> getPushServerList() {
		return pushServerList;
	}

	public int getLossPushTime() {
		return lossPushTime;
	}

	public int getEffect1554limit() {
		return effect1554limit;
	}


	public double getEquipResearchQueueTimeWeight() {
		return equipResearchQueueTimeWeight;
	}

	public int getEquipResearchUnlockQuality() {
		return equipResearchUnlockQuality;
	}

	public int getResCollectExtraDropTime() {
		return resCollectExtraDropTime;
	}

	public int getResCollectExtraDropAward() {
		return resCollectExtraDropAward;
	}

	public int getResCollectExtraDropTimesLimit() {
		return resCollectExtraDropTimesLimit;
	}

	public int getResCollectExtraDropLevelLimitMin() {
		return resCollectExtraDropLevelLimitMin;
	}

	public void setResCollectExtraDropLevelLimitMin(int resCollectExtraDropLevelLimitMin) {
		this.resCollectExtraDropLevelLimitMin = resCollectExtraDropLevelLimitMin;
	}

	public int getResCollectExtraDropLevelLimitMax() {
		return resCollectExtraDropLevelLimitMax;
	}

	public void setResCollectExtraDropLevelLimitMax(int resCollectExtraDropLevelLimitMax) {
		this.resCollectExtraDropLevelLimitMax = resCollectExtraDropLevelLimitMax;
	}

	public String getEffect1562DamageParam() {
		return effect1562DamageParam;
	}

	public int getEffect1562TimesLimit() {
		return effect1562TimesLimit;
	}

	public int getEffect1562DamageUpParam() {
		return effect1562DamageUpParam;
	}
	
	public long getNewPushGiftStartTimeValue() {
		return newPushGiftStartTimeValue;
	}

	public List<Integer> getEquipResearchShowUnlockList() {
		return equipResearchShowUnlockList;
	}

	public int getEffect1321Prob() {
		return effect1321Prob;
	}

	public String getBlessingImg() {
		return blessingImg;
	}
	
	public int getEffect1610NumParam() {
		return effect1610NumParam;
	}

	public int getEffect1611NumParam() {
		return effect1611NumParam;
	}

	public int getEffect1612NumParam() {
		return effect1612NumParam;
	}

	public int getEffect7004Maximum() {
		return effect7004Maximum;
	}

	public int getEffect7010Maximum() {
		return effect7010Maximum;
	}

	public int getEffect7011RoundBegin() {
		return effect7011RoundBegin;
	}

	public int getEffect7012TroopNums() {
		return effect7012TroopNums;
	}

	public int getEffect7015TroopNums() {
		return effect7015TroopNums;
	}

	public int getEffect7016TroopNums() {
		return effect7016TroopNums;
	}
	
	public List<String> getCrossEntitiesOptimizeServerList() {
		return crossEntitiesOptimizeServerList;
	}

	public int getEffect7018Maximum() {
		return effect7018Maximum;
	}

	public int getEffect7019Maximum() {
		return effect7019Maximum;
	}

	public int getEffect7020Maximum() {
		return effect7020Maximum;
	}

	public int getEffect7021Maximum() {
		return effect7021Maximum;
	}

	public int getEffect7014Round() {
		return effect7014Round;
	}

	public int getEffect1617TimesLimit() {
		return effect1617TimesLimit;
	}

	public int getEffect1620Maxinum() {
		return effect1620Maxinum;
	}

	public int getEffect1621Maxinum() {
		return effect1621Maxinum;
	}

	public int getEffect1622Maxinum() {
		return effect1622Maxinum;
	}

	public int getEffect1623Maxinum() {
		return effect1623Maxinum;
	}

	public int getEffect1624Maxinum() {
		return effect1624Maxinum;
	}

	public int getEffect1625Maxinum() {
		return effect1625Maxinum;
	}

	public int getBuildControlLevel() {
		return buildControlLevel;
	}
	public int getDailyGiftRandomAwardId() {
		return dailyGiftRandomAwardId;
	}

	public int getDailyGiftMustRewardItemId() {
		return dailyGiftMustRewardItemId;
	}

	public int getDailyGiftBoxOpenTimes() {
		return dailyGiftBoxOpenTimes;
	}

	public int getDailyGiftBoxItemId() {
		return dailyGiftBoxItemId;
	}

	public int getGoldPrivilegeButtonTime() {
		return goldPrivilegeButtonTime;
	}

	public int getGoldPrivilegeDiscountItem() {
		return goldPrivilegeDiscountItem;
	}

	public String getGoldPrivilegePayGiftIdIos() {
		return goldPrivilegePayGiftIdIos;
	}

	public String getGoldPrivilegePayGiftIdAndroid() {
		return goldPrivilegePayGiftIdAndroid;
	}

	public int getGoldPrivilegeType() {
		return goldPrivilegeType;
	}

	public int getEffect1628Maxinum() {
		return effect1628Maxinum;
	}

	public int getEffect1631Maxinum() {
		return effect1631Maxinum;
	}

	public int getEffect1631Per() {
		return effect1631Per;
	}

	public int getEffect1633TimesLimit() {
		return effect1633TimesLimit;
	}

	public int getEffect1634Maxinum() {
		return effect1634Maxinum;
	}

	public int getEffect1635BaseVal() {
		return effect1635BaseVal;
	}

	public int getEffect1635Maxinum() {
		return effect1635Maxinum;
	}

	public double getEffect1637Per() {
		return effect1637Per * GsConst.EFF_PER;
	}

	public int getEffect1637Maxinum() {
		return effect1637Maxinum;
	}

	public double getEffect1638Per() {
		return effect1638Per * GsConst.EFF_PER;
	}

	public int getEffect1638Maxinum() {
		return effect1638Maxinum;
	}

	public int getEffect1640Parametric() {
		return effect1640Parametric;
	}

	public String getEffect1639Parametric() {
		return effect1639Parametric;
	}

	public int getEffect1639Per() {
		return effect1639Per;
	}

	public String getEffect1639SoldierPer() {
		return effect1639SoldierPer;
	}

	public int getEffect1639Maxinum() {
		return effect1639Maxinum;
	}

	public HawkTuple3<Integer, Integer, Integer> getEffect1639ParametricNum() {
		return effect1639ParametricNum;
	}

	public Map<SoldierType, Integer> getEffect1639SoldierPerMap() {
		return effect1639SoldierPerMap;
	}
	
	public List<Integer> getRecordItemList() {
		return recordItemList;
	}
	
	public String getAirdropUnlock() {
		return airdropUnlock;
	}

	public String getWorldEnemyDistance() {
		return worldEnemyDistance;
	}

	public int getWorldEnemyLevel() {
		return worldEnemyLevel;
	}

	public long getTravelShopFriendlyCardTime() {
		return travelShopFriendlyCardTime * 1000L;
	}

	public int getTravelShopFriendlyAwardCost() {
		return travelShopFriendlyAwardCost;
	}

	public List<ItemInfo> getTravelShoFriendlyCommonAward() {
		return ItemInfo.valueListOf(travelShoFriendlyCommonAward);
	}

	public int getTravelShopFriendlyAwardCommType() {
		return travelShopFriendlyAwardCommType;
	}
	
	
	public int getTravelShopFriendlyAwardPrivilegeType() {
		return travelShopFriendlyAwardPrivilegeType;
	}
	
	public int getTravelShopFriendlyAwardPrivilegeGroup() {
		return travelShopFriendlyAwardPrivilegeGroup;
	}
	
	public int getTravelShopAssistRateRise() {
		return travelShopAssistRateRise;
	}
	
	/**
	 * 获取友好度
	 * @param costType
	 * @return
	 */
	public int getTravelShopFriendly(int costType) {
		if (!travelShopFriendlyMap.containsKey(costType)) {
			return 0;
		}
		return travelShopFriendlyMap.get(costType);
	}

	public int getTravelShopFriendlyUpRate() {
		return travelShopFriendlyUpRate;
	}

	public int getAirdropUnlockBuildType() {
		return airdropUnlockBuildType;
	}

	public int getEffect1641Per() {
		return effect1641Per;
	}

	public int getEffect1641Maxinum() {
		return effect1641Maxinum;
	}

	public int getEffect1642Per() {
		return effect1642Per;
	}

	public int getEffect1642SoldierPer() {
		return effect1642SoldierPer;
	}

	public int getEffect1642Maxinum() {
		return effect1642Maxinum;
	}

	public int getQAshareQRCode() {
		return QAshareQRCode;
	}

	public int getPrivacyDisplay() {
		return privacyDisplay;
	}

	public int getEffect1643SoldierPer() {
		return effect1643SoldierPer;
	}

	public int getEffect1643Maxinum() {
		return effect1643Maxinum;
	}

	public int getEffect1644SoldierPer() {
		return effect1644SoldierPer;
	}

	public int getEffect1644Maxinum() {
		return effect1644Maxinum;
	}

	public long getSpacePropVacant() {
		return spacePropVacant;
	}
	
	

	public int getEffect1645Maxinum() {
		return effect1645Maxinum;
	}

	public int getEffect1645Power() {
		return effect1645Power;
	}

	public int getEffect1647Maxinum() {
		return effect1647Maxinum;
	}

	public int getEffect1648Maxinum() {
		return effect1648Maxinum;
	}

	public int getEffect1649Per() {
		return effect1649Per;
	}

	public int getEffect1649Per2() {
		return effect1649Per2;
	}

	public int getEffect11003TimesLimit() {
		return effect11003TimesLimit;
	}

	public int getEffect11004TimesLimit() {
		return effect11004TimesLimit;
	}

	public int getEffect11005TimesLimit() {
		return effect11005TimesLimit;
	}

	public int getEffect11009TimesLimit() {
		return effect11009TimesLimit;
	}

	public int getEffect11010TimesLimit() {
		return effect11010TimesLimit;
	}

	public int getEffect11012TimesLimit() {
		return effect11012TimesLimit;
	}

	public int getEffect11013TimesLimit() {
		return effect11013TimesLimit;
	}

	public int getEffect1650Per() {
		return effect1650Per;
	}

	public int getEffect1651Per() {
		return effect1651Per;
	}

	public int getEffect1652Num() {
		return effect1652Num;
	}

	public String getEffect1656Per() {
		return effect1656Per;
	}

	public int getSearchPrecise() {
		return searchPrecise;
	}

	public int getEffect1658Per() {
		return effect1658Per;
	}

	public int getEffect1658Per2() {
		return effect1658Per2;
	}
	
	public int getShowIpOnline() {
		return showIpOnline;
	}
	
	public String getSecondBuildDefaultOpenTime() {
		return secondBuildDefaultOpenTime;
	}
	
	public long getSecondBuildDefaultOpenTimeLong() {
		return HawkTime.parseTime(secondBuildDefaultOpenTime);
	}
	
	public int getSecondBuildGiftGroupId() {
		return secondBuildGiftGroupId;
	}

	public int getEffect1659Num() {
		return effect1659Num;
	}

	public int getEffect1661Num() {
		return effect1661Num;
	}

	public int getHeroArchivesOpenLv() {
		return heroArchivesOpenLv;
	}

	public String getHeroArchivesOpenAward() {
		return heroArchivesOpenAward;
	}

	public int getEffect1664Maxinum() {
		return effect1664Maxinum;
	}

	public int getEffect1665Maxinum() {
		return effect1665Maxinum;
	}

	public String getRallySetTeamName() {
		return rallySetTeamName;
	}

	public int getEffect1681AllNumLimit() {
		return effect1681AllNumLimit;
	}

	public int getEffect1681SelfNumLimit() {
		return effect1681SelfNumLimit;
	}

	public int getEffect1681RateValue() {
		return effect1681RateValue;
	}

	public int getEffect1681MaxValue() {
		return effect1681MaxValue;
	}

	public int getEffect1682SelfNumLimit() {
		return effect1682SelfNumLimit;
	}

	public int getEffect1683SelfNumLimit() {
		return effect1683SelfNumLimit;
	}

	public int getEffect1684SelfNumLimit() {
		return effect1684SelfNumLimit;
	}

	public int getEffect1682RateValue() {
		return effect1682RateValue;
	}

	public int getEffect1683RateValue() {
		return effect1683RateValue;
	}

	public int getEffect1684RateValue() {
		return effect1684RateValue;
	}

	public int getEffect12001AllNumLimit() {
		return effect12001AllNumLimit;
	}

	public int getEffect12002AllNumLimit() {
		return effect12002AllNumLimit;
	}

	public int getEffect12003AllNumLimit() {
		return effect12003AllNumLimit;
	}

	public int getEffect12001SelfNumLimit() {
		return effect12001SelfNumLimit;
	}

	public int getEffect12002SelfNumLimit() {
		return effect12002SelfNumLimit;
	}

	public int getEffect12003SelfNumLimit() {
		return effect12003SelfNumLimit;
	}

	public int getEffect12001Maxinum() {
		return effect12001Maxinum;
	}

	public int getEffect12002Maxinum() {
		return effect12002Maxinum;
	}

	public int getEffect12003Maxinum() {
		return effect12003Maxinum;
	}

	public int getEffect12004AllNumLimit() {
		return effect12004AllNumLimit;
	}

	public int getEffect12004SelfNumLimit() {
		return effect12004SelfNumLimit;
	}

	public int getEffect12004Maxinum() {
		return effect12004Maxinum;
	}

	public int getEffect12005AllNumLimit() {
		return effect12005AllNumLimit;
	}

	public int getEffect12005SelfNumLimit() {
		return effect12005SelfNumLimit;
	}

	public int getEffect12005AtkRound() {
		return effect12005AtkRound;
	}

	public int getEffect12005AtkNum() {
		return effect12005AtkNum;
	}

	public int getEffect12005ContinueRound() {
		return effect12005ContinueRound;
	}

	public int getEffect12005Maxinum() {
		return effect12005Maxinum;
	}

	public int getEffect12006MaxValue() {
		return effect12006MaxValue;
	}

	public int getEffect12007MaxValue() {
		return effect12007MaxValue;
	}

	public int getEffect12008MaxValue() {
		return effect12008MaxValue;
	}

	public int getEffect12011AllNumLimit() {
		return effect12011AllNumLimit;
	}

	public int getEffect12012AllNumLimit() {
		return effect12012AllNumLimit;
	}

	public int getEffect12013AllNumLimit() {
		return effect12013AllNumLimit;
	}

	public int getEffect12011SelfNumLimit() {
		return effect12011SelfNumLimit;
	}

	public int getEffect12012SelfNumLimit() {
		return effect12012SelfNumLimit;
	}

	public int getEffect12013SelfNumLimit() {
		return effect12013SelfNumLimit;
	}

	public int getEffect12011Maxinum() {
		return effect12011Maxinum;
	}

	public int getEffect12012Maxinum() {
		return effect12012Maxinum;
	}

	public int getEffect12013Maxinum() {
		return effect12013Maxinum;
	}

	public int getEffect12014AllNumLimit() {
		return effect12014AllNumLimit;
	}

	public int getEffect12014SelfNumLimit() {
		return effect12014SelfNumLimit;
	}

	public int getEffect12014Maxinum() {
		return effect12014Maxinum;
	}

	public int getEffect12015AllNumLimit() {
		return effect12015AllNumLimit;
	}

	public int getEffect12015SelfNumLimit() {
		return effect12015SelfNumLimit;
	}

	public int getEffect12015AtkRound() {
		return effect12015AtkRound;
	}

	public int getEffect12015AtkTimes() {
		return effect12015AtkTimes;
	}

	public int getEffect12015AtkNum() {
		return effect12015AtkNum;
	}

	public int getEffect12015ContinueRound() {
		return effect12015ContinueRound;
	}

	public int getEffect12015Maxinum() {
		return effect12015Maxinum;
	}
	
	public int[] getEffect12031EffectiveRoundArr(){
		return effect12031EffectiveRoundArr;
	}
	
	public int getEffect12033Maxinum(){
		return effect12033Maxinum;
	}
	
	public int getEffect12034Maxinum(){
		return effect12034Maxinum;
	}
	
	public int getEffect12035Maxinum(){
		return effect12035Maxinum;
	}

	public int[] getEffect12036EffectiveRoundArr() {
		return effect12036EffectiveRoundArr;
	}
	
	public int getEffect12039Maxinum(){
		return effect12039Maxinum;
	}
	
	public int getEffect12040Maxinum(){
		return effect12040Maxinum;
	}
	
	public int getEffect12040Nums(){
		return effect12040Nums;
	}
	
	public int getEffect12041ContinueRound(){
		return effect12041ContinueRound;
	}
	
	public int getEffect12051SelfNumLimit(){
		return effect12051SelfNumLimit;
	}
	
	public int getEffect12051AtkRound(){
		return effect12051AtkRound;
	}
	
	public int getEffect12051AtkTimesForMass(){
		return effect12051AtkTimesForMass;
	}
	
	public int getEffect12051AtkTimesForPerson(){
		return effect12051AtkTimesForPerson;
	}

	public int getEffect12061AllNumLimit() {
		return effect12061AllNumLimit;
	}

	public int getEffect12061SelfNumLimit() {
		return effect12061SelfNumLimit;
	}

	public int getEffect12061MaxValue() {
		return effect12061MaxValue;
	}

	public int getEffect12061Maxinum() {
		return effect12061Maxinum;
	}

	public int[] getShelterRecallArr() {
		return shelterRecallArr;
	}

	public void setShelterRecallArr(int[] shelterRecallArr) {
		this.shelterRecallArr = shelterRecallArr;
	}

	public List<ItemInfo> getHeroTalentChangeCostList() {
		return heroTalentChangeCostList;
	}

	public void setHeroTalentChangeCostList(List<ItemInfo> heroTalentChangeCostList) {
		this.heroTalentChangeCostList = heroTalentChangeCostList;
	}

	public TreeMap<Integer, List<HawkTuple2WeightObj<Integer>>> getHeroTalentLevelupRateProbabilityMap() {
		return heroTalentLevelupRateProbabilityMap;
	}

	public void setHeroTalentLevelupRateProbabilityMap(TreeMap<Integer, List<HawkTuple2WeightObj<Integer>>> heroTalentLevelupRateProbabilityMap) {
		this.heroTalentLevelupRateProbabilityMap = heroTalentLevelupRateProbabilityMap;
	}

	public Set<Integer> getResOutputBuffToAllSet() {
		return resOutputBuffToAllSet;
	}

	public void setResOutputBuffToAllSet(Set<Integer> resOutputBuffToAllSet) {
		this.resOutputBuffToAllSet = resOutputBuffToAllSet;
	}

	public Table<String, Integer, Integer> getPlatEffTable() {
		return platEffTable;
	}

	public void setPlatEffTable(Table<String, Integer, Integer> platEffTable) {
		this.platEffTable = platEffTable;
	}

	public Map<Integer, Integer> getVipShopItems() {
		return vipShopItems;
	}

	public void setVipShopItems(Map<Integer, Integer> vipShopItems) {
		this.vipShopItems = vipShopItems;
	}

	public Map<Integer, List<ItemInfo>> getBubbleAwardItems() {
		return bubbleAwardItems;
	}

	public void setBubbleAwardItems(Map<Integer, List<ItemInfo>> bubbleAwardItems) {
		this.bubbleAwardItems = bubbleAwardItems;
	}

	public Map<Integer, Integer> getWishCritRateMap() {
		return wishCritRateMap;
	}

	public void setWishCritRateMap(Map<Integer, Integer> wishCritRateMap) {
		this.wishCritRateMap = wishCritRateMap;
	}

	public int getBuyBuildQueueTimeLong() {
		return buyBuildQueueTimeLong;
	}

	public void setBuyBuildQueueTimeLong(int buyBuildQueueTimeLong) {
		this.buyBuildQueueTimeLong = buyBuildQueueTimeLong;
	}

	public ItemInfo getBuyBuildQueueCostItem() {
		return buyBuildQueueCostItem;
	}

	public void setBuyBuildQueueCostItem(ItemInfo buyBuildQueueCostItem) {
		this.buyBuildQueueCostItem = buyBuildQueueCostItem;
	}

	public Map<Integer, Integer> getCommanderEquipUnlockLvls() {
		return commanderEquipUnlockLvls;
	}

	public void setCommanderEquipUnlockLvls(Map<Integer, Integer> commanderEquipUnlockLvls) {
		this.commanderEquipUnlockLvls = commanderEquipUnlockLvls;
	}

	public Map<Integer, Integer> getEquipSlotPos() {
		return equipSlotPos;
	}

	public void setEquipSlotPos(Map<Integer, Integer> equipSlotPos) {
		this.equipSlotPos = equipSlotPos;
	}

	public Map<Integer, Object[]> getPropExchangeParamMap() {
		return propExchangeParamMap;
	}

	public void setPropExchangeParamMap(Map<Integer, Object[]> propExchangeParamMap) {
		this.propExchangeParamMap = propExchangeParamMap;
	}

	public Map<Integer, Integer> getTravelShopFriendlyMap() {
		return travelShopFriendlyMap;
	}

	public void setTravelShopFriendlyMap(Map<Integer, Integer> travelShopFriendlyMap) {
		this.travelShopFriendlyMap = travelShopFriendlyMap;
	}

	public String getEffect12031EffectiveRound() {
		return effect12031EffectiveRound;
	}

	public String getEffect12036EffectiveRound() {
		return effect12036EffectiveRound;
	}

	public String getEffect12062RoundWeight() {
		return effect12062RoundWeight;
	}

	public int getEffect12062AllNumLimit() {
		return effect12062AllNumLimit;
	}

	public int getEffect12062SelfNumLimit() {
		return effect12062SelfNumLimit;
	}

	public int getEffect12062AtkNum() {
		return effect12062AtkNum;
	}

	public int getEffect12062AtkTimes() {
		return effect12062AtkTimes;
	}

	public int getSnowballAtkFireTime() {
		return snowballAtkFireTime;
	}

	public String getEquipSlotLimit() {
		return equipSlotLimit;
	}

	public String getVipShopItem() {
		return vipShopItem;
	}

	public String getTravelShopCrystalRefreshCost() {
		return travelShopCrystalRefreshCost;
	}

	public String getTravelShopRefreshTime() {
		return travelShopRefreshTime;
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

	public int getQqFriend() {
		return qqFriend;
	}

	public int getQqFriendTime() {
		return qqFriendTime;
	}

	public String getCrossCostItems() {
		return crossCostItems;
	}

	public String getEquipResearchShowUnlock() {
		return equipResearchShowUnlock;
	}

	public String getRecordItems() {
		return recordItems;
	}

	public String getNewPushGiftStartTime() {
		return newPushGiftStartTime;
	}

	public String getCrossEntitiesOptimizeServers() {
		return crossEntitiesOptimizeServers;
	}

	public String getMechaRepairItem() {
		return mechaRepairItem;
	}

	public String getNewbieVersionTime() {
		return newbieVersionTime;
	}

	public String getTravelShopFriendly() {
		return travelShopFriendly;
	}

	public String getDailyLogin() {
		return dailyLogin;
	}

	public int getCdTimeSecond() {
		return cdTimeSecond;
	}

	public int getStartAutoTime() {
		return startAutoTime;
	}

	public int getHonorSevenOpen() {
		return honorSevenOpen;
	}

	public String getPlayerLossPushNotice() {
		return playerLossPushNotice;
	}

	public String getPushServers() {
		return pushServers;
	}

	public String getResCollectExtraDropLevelLimit() {
		return resCollectExtraDropLevelLimit;
	}

	public int getEffect4036RatioParam() {
		return effect4036RatioParam;
	}

	public int getEffect4037RatioParam() {
		return effect4037RatioParam;
	}

	public int getEffect4038RatioParam() {
		return effect4038RatioParam;
	}

	public int getEffect4039RatioParam() {
		return effect4039RatioParam;
	}

	public String getCavilliPseudorandom() {
		return cavilliPseudorandom;
	}

	public String getVerifyBuildNo() {
		return verifyBuildNo;
	}

	public String getSpaceProp() {
		return spaceProp;
	}

	public String getPropExchange() {
		return PropExchange;
	}

	public static void setInstance(ConstProperty instance) {
		ConstProperty.instance = instance;
	}

	public void setEffect12031EffectiveRoundArr(int[] effect12031EffectiveRoundArr) {
		this.effect12031EffectiveRoundArr = effect12031EffectiveRoundArr;
	}

	public void setEffect12036EffectiveRoundArr(int[] effect12036EffectiveRoundArr) {
		this.effect12036EffectiveRoundArr = effect12036EffectiveRoundArr;
	}

	public void setCityMaxCoordinate(int[] cityMaxCoordinate) {
		this.cityMaxCoordinate = cityMaxCoordinate;
	}

	public void setNewbieVersionTimeValue(long newbieVersionTimeValue) {
		this.newbieVersionTimeValue = newbieVersionTimeValue;
	}

	public void setSuperVipDailyLoginScore(int superVipDailyLoginScore) {
		this.superVipDailyLoginScore = superVipDailyLoginScore;
	}

	public void setSuperVipDailyLoginScoreStep(int superVipDailyLoginScoreStep) {
		this.superVipDailyLoginScoreStep = superVipDailyLoginScoreStep;
	}

	public void setMechaRepairItemList(List<ItemInfo> mechaRepairItemList) {
		this.mechaRepairItemList = mechaRepairItemList;
	}

	public void setResOutputBuffGoldoreSet(Set<Integer> resOutputBuffGoldoreSet) {
		this.resOutputBuffGoldoreSet = resOutputBuffGoldoreSet;
	}

	public void setResOutputBuffOilSet(Set<Integer> resOutputBuffOilSet) {
		this.resOutputBuffOilSet = resOutputBuffOilSet;
	}

	public void setResOutputBuffSteelSet(Set<Integer> resOutputBuffSteelSet) {
		this.resOutputBuffSteelSet = resOutputBuffSteelSet;
	}

	public void setResOutputBuffTombarthiteSet(Set<Integer> resOutputBuffTombarthiteSet) {
		this.resOutputBuffTombarthiteSet = resOutputBuffTombarthiteSet;
	}

	public void setSuperTimeRescueCdList(List<Integer> superTimeRescueCdList) {
		this.superTimeRescueCdList = superTimeRescueCdList;
	}

	public void setResUpEffectList(List<Integer> resUpEffectList) {
		this.resUpEffectList = resUpEffectList;
	}

	public void setCityOutsideAreas(List<Integer> cityOutsideAreas) {
		this.cityOutsideAreas = cityOutsideAreas;
	}

	public void setBubbleMaxTimes(Map<Integer, Integer> bubbleMaxTimes) {
		this.bubbleMaxTimes = bubbleMaxTimes;
	}

	public void setRecordItemList(List<Integer> recordItemList) {
		this.recordItemList = recordItemList;
	}

	public void setOutFireCostItems(List<ItemInfo> outFireCostItems) {
		this.outFireCostItems = outFireCostItems;
	}

	public void setSpeedUpTimeLKB(ImmutableSortedMap<Long, HawkTuple2<Long, Long>> speedUpTimeLKB) {
		this.speedUpTimeLKB = speedUpTimeLKB;
	}

	public void setBuyResCostLKB(ImmutableSortedMap<Long, HawkTuple2<Long, Long>> buyResCostLKB) {
		this.buyResCostLKB = buyResCostLKB;
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

	public void setInitSoldier(int[] initSoldier) {
		this.initSoldier = initSoldier;
	}

	public void setBuyEnergyItemCost(List<ItemInfo> buyEnergyItemCost) {
		this.buyEnergyItemCost = buyEnergyItemCost;
	}

	public void setRecallPushIntervals(Long[] recallPushIntervals) {
		this.recallPushIntervals = recallPushIntervals;
	}

	public void setLoveGift(Map<Integer, Integer> loveGift) {
		this.loveGift = loveGift;
	}

	public void setCommanderEquipSlotSize(int commanderEquipSlotSize) {
		this.commanderEquipSlotSize = commanderEquipSlotSize;
	}

	public void setTravelShopCrystalRefreshCostArray(int[] travelShopCrystalRefreshCostArray) {
		this.travelShopCrystalRefreshCostArray = travelShopCrystalRefreshCostArray;
	}

	public void setTravelShopRefreshTimeArray(int[] travelShopRefreshTimeArray) {
		this.travelShopRefreshTimeArray = travelShopRefreshTimeArray;
	}

	public void setGrantItemForUnlockMechaList(List<ItemInfo> grantItemForUnlockMechaList) {
		this.grantItemForUnlockMechaList = grantItemForUnlockMechaList;
	}

	public void setCrossCostItemInfos(List<ItemInfo> crossCostItemInfos) {
		this.crossCostItemInfos = crossCostItemInfos;
	}

	public void setActiveShareAwardItems(List<ItemInfo> activeShareAwardItems) {
		this.activeShareAwardItems = activeShareAwardItems;
	}

	public void setWechatFriendsStartTimeLong(long wechatFriendsStartTimeLong) {
		this.wechatFriendsStartTimeLong = wechatFriendsStartTimeLong;
	}

	public void setWechatFriendsEndTimeLong(long wechatFriendsEndTimeLong) {
		this.wechatFriendsEndTimeLong = wechatFriendsEndTimeLong;
	}

	public void setPlayerLossPushNoticeList(List<HawkTuple3<Integer, Integer, List<Integer>>> playerLossPushNoticeList) {
		this.playerLossPushNoticeList = playerLossPushNoticeList;
	}

	public void setPushServerList(List<String> pushServerList) {
		this.pushServerList = pushServerList;
	}

	public void setDailyGiftBoxItemId(int dailyGiftBoxItemId) {
		this.dailyGiftBoxItemId = dailyGiftBoxItemId;
	}

	public void setDailyGiftBoxOpenTimes(int dailyGiftBoxOpenTimes) {
		this.dailyGiftBoxOpenTimes = dailyGiftBoxOpenTimes;
	}

	public void setDailyGiftMustRewardItemId(int dailyGiftMustRewardItemId) {
		this.dailyGiftMustRewardItemId = dailyGiftMustRewardItemId;
	}

	public void setDailyGiftRandomAwardId(int dailyGiftRandomAwardId) {
		this.dailyGiftRandomAwardId = dailyGiftRandomAwardId;
	}

	public void setNewPushGiftStartTimeValue(long newPushGiftStartTimeValue) {
		this.newPushGiftStartTimeValue = newPushGiftStartTimeValue;
	}

	public void setEquipResearchShowUnlockList(List<Integer> equipResearchShowUnlockList) {
		this.equipResearchShowUnlockList = equipResearchShowUnlockList;
	}

	public void setCrossEntitiesOptimizeServerList(List<String> crossEntitiesOptimizeServerList) {
		this.crossEntitiesOptimizeServerList = crossEntitiesOptimizeServerList;
	}

	public void setEffect1639ParametricNum(HawkTuple3<Integer, Integer, Integer> effect1639ParametricNum) {
		this.effect1639ParametricNum = effect1639ParametricNum;
	}

	public void setEffect1639SoldierPerMap(Map<SoldierType, Integer> effect1639SoldierPerMap) {
		this.effect1639SoldierPerMap = effect1639SoldierPerMap;
	}

	public void setAirdropUnlockBuildType(int airdropUnlockBuildType) {
		this.airdropUnlockBuildType = airdropUnlockBuildType;
	}

	public void setBuildNoList(List<Integer> buildNoList) {
		this.buildNoList = buildNoList;
	}

	public void setSpacePropLKB(Map<Integer, Integer> spacePropLKB) {
		this.spacePropLKB = spacePropLKB;
	}

	public int getEffect12062Maxinum() {
		return effect12062Maxinum;
	}

	public int getEffect12063MaxValue() {
		return effect12063MaxValue;
	}

	public int getEffect12064MaxValue() {
		return effect12064MaxValue;
	}

	public int getEffect12065MaxValue() {
		return effect12065MaxValue;
	}

	public int getEffect12066AllNumLimit() {
		return effect12066AllNumLimit;
	}

	public int getEffect12066SelfNumLimit() {
		return effect12066SelfNumLimit;
	}

	public int getEffect12066AtkTimes() {
		return effect12066AtkTimes;
	}

	public int getEffect12066Maxinum() {
		return effect12066Maxinum;
	}

	public int getEffect12053SelfNumLimit() {
		return effect12053SelfNumLimit;
	}

	public int getEffect12054SelfNumLimit() {
		return effect12054SelfNumLimit;
	}

	public int getEffect12052SelfNumLimit() {
		return effect12052SelfNumLimit;
	}

	public int[] getStatueCoordinatesArr() {
		return statueCoordinatesArr;
	}

	public void setStatueCoordinatesArr(int[] statueCoordinatesArr) {
		this.statueCoordinatesArr = statueCoordinatesArr;
	}

	public int getEffect12081SelfNumLimit() {
		return effect12081SelfNumLimit;
	}

	public int getEffect12082SelfNumLimit() {
		return effect12082SelfNumLimit;
	}

	public int getEffect12083SelfNumLimit() {
		return effect12083SelfNumLimit;
	}

	public int getEffect12081AllNumLimit() {
		return effect12081AllNumLimit;
	}

	public int getEffect12082AllNumLimit() {
		return effect12082AllNumLimit;
	}

	public int getEffect12083AllNumLimit() {
		return effect12083AllNumLimit;
	}

	public int getEffect12081Maxinum() {
		return effect12081Maxinum;
	}

	public int getEffect12082Maxinum() {
		return effect12082Maxinum;
	}

	public int getEffect12083Maxinum() {
		return effect12083Maxinum;
	}

	public int getEffect12084SelfNumLimit() {
		return effect12084SelfNumLimit;
	}

	public int getEffect12084AllNumLimit() {
		return effect12084AllNumLimit;
	}

	public int getEffect12084Maxinum() {
		return effect12084Maxinum;
	}

	public int getEffect12085SelfNumLimit() {
		return effect12085SelfNumLimit;
	}

	public int getEffect12085AllNumLimit() {
		return effect12085AllNumLimit;
	}

	public int getEffect12085EffectNum() {
		return effect12085EffectNum;
	}

	public int getEffect12085NumLimit() {
		return effect12085NumLimit;
	}

	public int getEffect12085Maxinum() {
		return effect12085Maxinum;
	}

	public int getEffect12086RateMin() {
		return effect12086RateMin;
	}

	public int getEffect12086RateMax() {
		return effect12086RateMax;
	}

	public int getEffect12086TransferRate() {
		return effect12086TransferRate;
	}

	public int getEffect12085AtkRound() {
		return effect12085AtkRound;
	}

	public int getEffect12085ContinueRound() {
		return effect12085ContinueRound;
	}

	public int getEffect12085GetLimit() {
		return effect12085GetLimit;
	}

	public int getEffect12104MaxNum() {
		return effect12104MaxNum;
	}

	public int getEffect5001Cnt() {
		return effect5001Cnt;
	}

	public int getEffect12113ContinueRound() {
		return effect12113ContinueRound;
	}

	public int getEffect12114LossThresholdValue() {
		return effect12114LossThresholdValue;
	}

	public int getStaffOffice4Max() {
		return staffOffice4Max;
	}

	public int getEffect12122AllNumLimit() {
		return effect12122AllNumLimit;
	}

	public int getEffect12122BaseVaule() {
		return effect12122BaseVaule;
	}

	public int getEffect12122Maxinum() {
		return effect12122Maxinum;
	}

	public int getEffect12123AllNumLimit() {
		return effect12123AllNumLimit;
	}

	public int getEffect12123BaseVaule() {
		return effect12123BaseVaule;
	}

	public int getEffect12123Maxinum() {
		return effect12123Maxinum;
	}

	public int getEffect12134ContinueRound() {
		return effect12134ContinueRound;
	}

	public int getEffect12132ContinueRound() {
		return effect12132ContinueRound;
	}

	public int getEffect12151AllNumLimit() {
		return effect12151AllNumLimit;
	}

	public int getEffect12151BaseVaule() {
		return effect12151BaseVaule;
	}

	public int getEffect12151Maxinum() {
		return effect12151Maxinum;
	}

	public int getEffect12152Maxinum() {
		return effect12152Maxinum;
	}

	public int getEffect12153ContinueRound() {
		return effect12153ContinueRound;
	}

	public int getEffect12154MaxNum() {
		return effect12154MaxNum;
	}

	public int getEffect12154ContinueRound() {
		return effect12154ContinueRound;
	}

	public int getEffect1685Maxinum() {
		return effect1685Maxinum;
	}

	public int getEffect12161AllNumLimit() {
		return effect12161AllNumLimit;
	}

	public int getEffect12161SelfNumLimit() {
		return effect12161SelfNumLimit;
	}

	public int getEffect12161ContinueRound() {
		return effect12161ContinueRound;
	}

	public int getEffect12161Maxinum() {
		return effect12161Maxinum;
	}

	public int getEffect12162AllNumLimit() {
		return effect12162AllNumLimit;
	}

	public int getEffect12162SelfNumLimit() {
		return effect12162SelfNumLimit;
	}

	public int getEffect12162NextRound() {
		return effect12162NextRound;
	}

	public int getEffect12162EffectiveUnit() {
		return effect12162EffectiveUnit;
	}

	public int getEffect12162ContinueRound() {
		return effect12162ContinueRound;
	}

	public int getEffect12162Maxinum() {
		return effect12162Maxinum;
	}

	public int getEffect12163AllNumLimit() {
		return effect12163AllNumLimit;
	}

	public int getEffect12163SelfNumLimit() {
		return effect12163SelfNumLimit;
	}

	public int getEffect12163AtkNum() {
		return effect12163AtkNum;
	}

	public int getEffect12163Maxinum() {
		return effect12163Maxinum;
	}

	public int getEffect12164ContinueRound() {
		return effect12164ContinueRound;
	}

	public int getEffect12165ContinueRound() {
		return effect12165ContinueRound;
	}

	public int getEffect12166ContinueRound() {
		return effect12166ContinueRound;
	}

	public int getEffect12167ContinueRound() {
		return effect12167ContinueRound;
	}

	public int getEffect12168ContinueRound() {
		return effect12168ContinueRound;
	}

	public int getEffect12169ContinueRound() {
		return effect12169ContinueRound;
	}

	public int getEffect12170ContinueRound() {
		return effect12170ContinueRound;
	}

	public int getEffect12171ContinueRound() {
		return effect12171ContinueRound;
	}

	public int getEffect12172AllNumLimit() {
		return effect12172AllNumLimit;
	}

	public int getEffect12173AllNumLimit() {
		return effect12173AllNumLimit;
	}

	public int getEffect12172SelfNumLimit() {
		return effect12172SelfNumLimit;
	}

	public int getEffect12173SelfNumLimit() {
		return effect12173SelfNumLimit;
	}

	public int getEffect12173MaxNum() {
		return effect12173MaxNum;
	}

	public int getEffect12172Maxinum() {
		return effect12172Maxinum;
	}

	public int getEffect12173Maxinum() {
		return effect12173Maxinum;
	}

	public int getEffect12191AllNumLimit() {
		return effect12191AllNumLimit;
	}

	public int getEffect12191BaseVaule() {
		return effect12191BaseVaule;
	}

	public int getEffect12191Maxinum() {
		return effect12191Maxinum;
	}

	public int getEffect12192AllNumLimit() {
		return effect12192AllNumLimit;
	}

	public int getEffect12192BaseVaule() {
		return effect12192BaseVaule;
	}

	public int getEffect12192Maxinum() {
		return effect12192Maxinum;
	}

	public int getEffect12193Maxinum() {
		return effect12193Maxinum;
	}

	public int getEffect12194Maxinum() {
		return effect12194Maxinum;
	}

	public int getEffect12201BasePoint() {
		return effect12201BasePoint;
	}

	public int getEffect12201ExtraPoint() {
		return effect12201ExtraPoint;
	}

	public int getEffect12201MaxPoint() {
		return effect12201MaxPoint;
	}

	public int getEffect12202AtkRound() {
		return effect12202AtkRound;
	}

	public int getEffect12202AtkThresholdValue() {
		return effect12202AtkThresholdValue;
	}

	public int getEffect12202AtkTimes() {
		return effect12202AtkTimes;
	}

	public String getEffect12202DamageAdjust() {
		return effect12202DamageAdjust;
	}

	public ImmutableMap<SoldierType, Integer> getEffect12202DamageAdjustMap() {
		return effect12202DamageAdjustMap;
	}

	public int getEffect12206Maxinum() {
		return effect12206Maxinum;
	}

	public int getEffect12208MaxNum() {
		return effect12208MaxNum;
	}

	public int getEffect12221NumLimit() {
		return effect12221NumLimit;
	}

	public int getEffect12221Maxinum() {
		return effect12221Maxinum;
	}

	public int getEffect12222NumLimit() {
		return effect12222NumLimit;
	}

	public int getEffect12222Maxinum() {
		return effect12222Maxinum;
	}

	public int getEffect12223Maxinum() {
		return effect12223Maxinum;
	}

	public int getEffect12224Maxinum() {
		return effect12224Maxinum;
	}

	public int getEffect12231ArmyNum() {
		return effect12231ArmyNum;
	}

	public int getEffect12251AtkTimes() {
		return effect12251AtkTimes;
	}

	public String getEffect12253SoldierAdjust() {
		return effect12253SoldierAdjust;
	}


	public ImmutableMap<SoldierType, Integer> getEffect12253HoldTimesMap() {
		return effect12253HoldTimesMap;
	}

	public void setEffect12253HoldTimesMap(ImmutableMap<SoldierType, Integer> effect12253HoldTimesMap) {
		this.effect12253HoldTimesMap = effect12253HoldTimesMap;
	}

	public String getEffect12253HoldTimes() {
		return effect12253HoldTimes;
	}

	public ImmutableMap<SoldierType, Integer> getEffect12253SoldierAdjustMap() {
		return effect12253SoldierAdjustMap;
	}

	public void setEffect12253SoldierAdjustMap(ImmutableMap<SoldierType, Integer> effect12253SoldierAdjustMap) {
		this.effect12253SoldierAdjustMap = effect12253SoldierAdjustMap;
	}

	public int getEffect12261AllNumLimit() {
		return effect12261AllNumLimit;
	}

	public int getEffect12261SelfNumLimit() {
		return effect12261SelfNumLimit;
	}

	public int getEffect12261Maxinum() {
		return effect12261Maxinum;
	}

	public int getEffect12262AllNumLimit() {
		return effect12262AllNumLimit;
	}

	public int getEffect12262SelfNumLimit() {
		return effect12262SelfNumLimit;
	}

	public int getEffect12262Maxinum() {
		return effect12262Maxinum;
	}

	public int getEffect12264HoldTimes() {
		return effect12264HoldTimes;
	}

	public int getEffect12271AtkTimesForMass() {
		return effect12271AtkTimesForMass;
	}

	public int getEffect12271AtkTimesForPerson() {
		return effect12271AtkTimesForPerson;
	}

	public int getEffect12272MaxValue() {
		return effect12272MaxValue;
	}

	public int getEffect12281SelfNumLimit() {
		return effect12281SelfNumLimit;
	}

	public int getEffect12281AllNumLimit() {
		return effect12281AllNumLimit;
	}

	public int getEffect12281Maxinum() {
		return effect12281Maxinum;
	}

	public int getEffect12282SelfNumLimit() {
		return effect12282SelfNumLimit;
	}

	public int getEffect12282AllNumLimit() {
		return effect12282AllNumLimit;
	}

	public int getEffect12282Maxinum() {
		return effect12282Maxinum;
	}

	public int getEffect12292AtkNum() {
		return effect12292AtkNum;
	}

	public int getEffect12293Maxinum() {
		return effect12293Maxinum;
	}

	public int getEffect12301AdjustForMass() {
		return effect12301AdjustForMass;
	}

	public int getEffect12302AllNumLimit() {
		return effect12302AllNumLimit;
	}

	public int getEffect12302SelfNumLimit() {
		return effect12302SelfNumLimit;
	}

	public int getEffect12302AffectNum() {
		return effect12302AffectNum;
	}

	public int getEffect12302MaxTimes() {
		return effect12302MaxTimes;
	}

	public void setEffect12202DamageAdjustMap(ImmutableMap<SoldierType, Integer> effect12202DamageAdjustMap) {
		this.effect12202DamageAdjustMap = effect12202DamageAdjustMap;
	}

	public int getEffect12302Maxinum() {
		return effect12302Maxinum;
	}

	public ImmutableMap<SoldierType, Integer> getEffect12311DamageAdjustMap() {
		return effect12311DamageAdjustMap;
	}

	public void setEffect12311DamageAdjustMap(ImmutableMap<SoldierType, Integer> effect12311DamageAdjustMap) {
		this.effect12311DamageAdjustMap = effect12311DamageAdjustMap;
	}

	public String getEffect12311DamageAdjust() {
		return effect12311DamageAdjust;
	}

	public int getEffect12312AtkNum() {
		return effect12312AtkNum;
	}

	public int getEffect12254Maxinum() {
		return effect12254Maxinum;
	}

	public int getEffect12331AllNumLimit() {
		return effect12331AllNumLimit;
	}

	public int getEffect12331SelfNumLimit() {
		return effect12331SelfNumLimit;
	}

	public int getEffect12331AddEscortPoint() {
		return effect12331AddEscortPoint;
	}

	public int getEffect12331MaxEscortPoint() {
		return effect12331MaxEscortPoint;
	}

	public int getEffect12331Maxinum() {
		return effect12331Maxinum;
	}

	public int getEffect12333AllNumLimit() {
		return effect12333AllNumLimit;
	}

	public int getEffect12333SelfNumLimit() {
		return effect12333SelfNumLimit;
	}

	public int getEffect12333AffectNum() {
		return effect12333AffectNum;
	}

	public int getEffect12333Maxinum() {
		return effect12333Maxinum;
	}

	public int getEffect12335AllNumLimit() {
		return effect12335AllNumLimit;
	}

	public int getEffect12335SelfNumLimit() {
		return effect12335SelfNumLimit;
	}

	public int getEffect12335AddFirePoint() {
		return effect12335AddFirePoint;
	}

	public int getEffect12335MaxFirePoint() {
		return effect12335MaxFirePoint;
	}

	public int getEffect12335Maxinum() {
		return effect12335Maxinum;
	}

	public int getEffect12337AllNumLimit() {
		return effect12337AllNumLimit;
	}

	public int getEffect12337SelfNumLimit() {
		return effect12337SelfNumLimit;
	}

	public int getEffect12337AffectNum() {
		return effect12337AffectNum;
	}

	public int getEffect12337Maxinum() {
		return effect12337Maxinum;
	}

	public int getEffect12339AllNumLimit() {
		return effect12339AllNumLimit;
	}

	public int getEffect12339SelfNumLimit() {
		return effect12339SelfNumLimit;
	}

	public int getEffect12339CountRound() {
		return effect12339CountRound;
	}

	public int getEffect12339Maxinum() {
		return effect12339Maxinum;
	}

	public int getEffect12355AddNum() {
		return effect12355AddNum;
	}

	public int getEffect12356AddNum() {
		return effect12356AddNum;
	}

	public ImmutableMap<SoldierType, Integer> getEffect12361TargetWeightMap() {
		return effect12361TargetWeightMap;
	}

	public void setEffect12361TargetWeightMap(ImmutableMap<SoldierType, Integer> effect12361TargetWeightMap) {
		this.effect12361TargetWeightMap = effect12361TargetWeightMap;
	}

	public ImmutableMap<SoldierType, Integer> getEffect12361DamageAdjustMap() {
		return effect12361DamageAdjustMap;
	}

	public void setEffect12361DamageAdjustMap(ImmutableMap<SoldierType, Integer> effect12361DamageAdjustMap) {
		this.effect12361DamageAdjustMap = effect12361DamageAdjustMap;
	}

	public int getEffect12361BasePro() {
		return effect12361BasePro;
	}

	public String getEffect12361TargetWeight() {
		return effect12361TargetWeight;
	}

	public String getEffect12361DamageAdjust() {
		return effect12361DamageAdjust;
	}

	public ImmutableMap<SoldierType, Integer> getEffect12362AdjustMap() {
		return effect12362AdjustMap;
	}

	public void setEffect12362AdjustMap(ImmutableMap<SoldierType, Integer> effect12362AdjustMap) {
		this.effect12362AdjustMap = effect12362AdjustMap;
	}

	public int getEffect12362BasePro() {
		return effect12362BasePro;
	}

	public int getEffect12362ContinueRound() {
		return effect12362ContinueRound;
	}

	public String getEffect12362Adjust() {
		return effect12362Adjust;
	}

	public ImmutableMap<SoldierType, Integer> getEffect12363TargetWeightMap() {
		return effect12363TargetWeightMap;
	}

	public void setEffect12363TargetWeightMap(ImmutableMap<SoldierType, Integer> effect12363TargetWeightMap) {
		this.effect12363TargetWeightMap = effect12363TargetWeightMap;
	}

	public int getEffect12363TargetNum() {
		return effect12363TargetNum;
	}

	public String getEffect12363TargetWeight() {
		return effect12363TargetWeight;
	}

	public int getEffect12365MaxNum() {
		return effect12365MaxNum;
	}

	public int getEffect12383AddNum() {
		return effect12383AddNum;
	}

	public int getHeroSoulResetOpen() {
		return heroSoulResetOpen;
	}

	public int getHeroSoulResetTimeLimit() {
		return heroSoulResetTimeLimit;
	}

	public int getEffect12393Maxinum() {
		return effect12393Maxinum;
	}

	public int getEffect12394Maxinum() {
		return effect12394Maxinum;
	}

	public int getEffect12401Maxinum() {
		return effect12401Maxinum;
	}

	public int getEffect12402Maxinum() {
		return effect12402Maxinum;
	}

	public int getEffect12403Maxinum() {
		return effect12403Maxinum;
	}

	public int getEffect12404Maxinum() {
		return effect12404Maxinum;
	}

	public int getGetDressNumLimit() {
		return getDressNumLimit;
	}

	public ImmutableMap<SoldierType, Integer> getEffect11041DamageAdjustMap() {
		return effect11041DamageAdjustMap;
	}

	public int getEffect11042ContinueRound() {
		return effect11042ContinueRound;
	}

	public int getEffect11043ContinueRound() {
		return effect11043ContinueRound;
	}

	public ImmutableMap<SoldierType, Integer> getEffect11042AdjustMap() {
		return effect11042AdjustMap;
	}

	public ImmutableMap<SoldierType, Integer> getEffect11043AdjustMap() {
		return effect11043AdjustMap;
	}

	public int getEffect11044MaxNum() {
		return effect11044MaxNum;
	}

	public HawkTuple2<Integer, Integer> getSuperDamageLimitMinMax() {
		return superDamageLimitMinMax;
	}

	public void setSuperDamageLimitMinMax(HawkTuple2<Integer, Integer> superDamageLimitMinMax) {
		this.superDamageLimitMinMax = superDamageLimitMinMax;
	}

	public int getSuperDamageCof() {
		return superDamageCof;
	}

	public String getSuperDamageLimit() {
		return superDamageLimit;
	}

	public int getEffect12412AtkNums() {
		return effect12412AtkNums;
	}

	public int getEffect12412AtkTimes() {
		return effect12412AtkTimes;
	}

	public int getEffect12413ContinueRound() {
		return effect12413ContinueRound;
	}

	public ImmutableMap<SoldierType, Integer> getEffect12414AdjustMap() {
		return effect12414AdjustMap;
	}

	public void setEffect12414AdjustMap(ImmutableMap<SoldierType, Integer> effect12414AdjustMap) {
		this.effect12414AdjustMap = effect12414AdjustMap;
	}

	public long getIsDressGodOpenTimeValue() {
		return isDressGodOpenTimeValue;
	}

	public void setIsDressGodOpenTimeValue(long isDressGodOpenTimeValue) {
		this.isDressGodOpenTimeValue = isDressGodOpenTimeValue;
	}

	public int getHeroSoulTroopImageUnlockStage() {
		return heroSoulTroopImageUnlockStage;
	}

	public int getHeroSoulMarchImageUnlockStage() {
		return heroSoulMarchImageUnlockStage;
	}

	public int getHeroSoulSkinUnlockStage() {
		return heroSoulSkinUnlockStage;
	}

	public int getEffect12414AtkTimesForPerson() {
		return effect12414AtkTimesForPerson;
	}

	public int getEffect12414Maxinum() {
		return effect12414Maxinum;
	}

	public String getEffect11041DamageAdjust() {
		return effect11041DamageAdjust;
	}

	public String getEffect11042Adjust() {
		return effect11042Adjust;
	}

	public String getEffect11043Adjust() {
		return effect11043Adjust;
	}

	public String getEffect12414Adjust() {
		return effect12414Adjust;
	}

	public String getIsDressGodOpenTime() {
		return isDressGodOpenTime;
	}

	public void setEffect11041DamageAdjustMap(ImmutableMap<SoldierType, Integer> effect11041DamageAdjustMap) {
		this.effect11041DamageAdjustMap = effect11041DamageAdjustMap;
	}

	public void setEffect11042AdjustMap(ImmutableMap<SoldierType, Integer> effect11042AdjustMap) {
		this.effect11042AdjustMap = effect11042AdjustMap;
	}

	public void setEffect11043AdjustMap(ImmutableMap<SoldierType, Integer> effect11043AdjustMap) {
		this.effect11043AdjustMap = effect11043AdjustMap;
	}

	public int getEffect12433AddAtkNum() {
		return effect12433AddAtkNum;
	}

	public int getMoveCityFixAramCD() {
		return moveCityFixAramCD;
	}

	public int getEffect12441AllNumLimit() {
		return effect12441AllNumLimit;
	}

	public int getEffect12441BaseVaule() {
		return effect12441BaseVaule;
	}

	public int getEffect12441Maxinum() {
		return effect12441Maxinum;
	}

	public int getEffect12442NumLimit() {
		return effect12442NumLimit;
	}

	public int getEffect12442Maxinum() {
		return effect12442Maxinum;
	}

	public int getEffect12443ContinueRound() {
		return effect12443ContinueRound;
	}

	public ImmutableMap<SoldierType, Integer> getEffect12451DamageAdjustMap() {
		return effect12451DamageAdjustMap;
	}

	public void setEffect12451DamageAdjustMap(ImmutableMap<SoldierType, Integer> effect12451DamageAdjustMap) {
		this.effect12451DamageAdjustMap = effect12451DamageAdjustMap;
	}

	public int getEffect12451ConditionalRatio() {
		return effect12451ConditionalRatio;
	}

	public int getEffect12451Maxinum() {
		return effect12451Maxinum;
	}

	public String getEffect12451DamageAdjust() {
		return effect12451DamageAdjust;
	}

	public int getBatchHeroMax() {
		return batchHeroMax;
	}

	public int getBatchChipMax() {
		return batchChipMax;
	}

	public int getBatchEquipmentMax() {
		return batchEquipmentMax;
	}
	
	public int getBossDailyLootTimeLimit185(int index) {
		return bossDailyLimit185.getOrDefault(index, 0);
	}
	
	public ImmutableMap<Integer,List<Integer>> getBossEnemyId185() {
		return this.bossEnemyId185;
	}

	public boolean inBossLimit185(int monsterId){
		for(Map.Entry<Integer,List<Integer>> entry : bossEnemyId185.entrySet()){
			List<Integer> blist = entry.getValue();
			if(blist.contains(monsterId)){
				return true;
			}
		}
		return false;
	}
	
	public ImmutableMap<Integer, Integer> getEffect12491RoundAdjustMap() {
		return effect12491RoundAdjustMap;
	}

	public void setEffect12491RoundAdjustMap(ImmutableMap<Integer, Integer> effect12491RoundAdjustMap) {
		this.effect12491RoundAdjustMap = effect12491RoundAdjustMap;
	}

	public ImmutableMap<SoldierType, Integer> getEffect12491SoldierAdjustMap() {
		return effect12491SoldierAdjustMap;
	}

	public void setEffect12491SoldierAdjustMap(ImmutableMap<SoldierType, Integer> effect12491SoldierAdjustMap) {
		this.effect12491SoldierAdjustMap = effect12491SoldierAdjustMap;
	}

	public String getEffect12491RoundAdjust() {
		return effect12491RoundAdjust;
	}

	public String getEffect12491SoldierAdjust() {
		return effect12491SoldierAdjust;
	}

	public int getEffect12461Maxinum() {
		return effect12461Maxinum;
	}

	public int getEffect12461AtkRound() {
		return effect12461AtkRound;
	}

	public int getEffect12461InitChooseNum() {
		return effect12461InitChooseNum;
	}

	public int getEffect12461ContinueRound() {
		return effect12461ContinueRound;
	}

	public int getEffect12461AllNumLimit() {
		return effect12461AllNumLimit;
	}

	public int getEffect12461SelfNumLimit() {
		return effect12461SelfNumLimit;
	}

	public int getEffect12464Maxinum() {
		return effect12464Maxinum;
	}

	public int getEffect12464BaseVaule() {
		return effect12464BaseVaule;
	}

	public int getEffect12465Maxinum() {
		return effect12465Maxinum;
	}

	public int getEffect12465AddFirePoint() {
		return effect12465AddFirePoint;
	}

	public int getEffect12465AtkRound() {
		return effect12465AtkRound;
	}

	public int getEffect12465AtkThresholdValue() {
		return effect12465AtkThresholdValue;
	}

	public int getEffect12465InitChooseNum() {
		return effect12465InitChooseNum;
	}

	public int getEffect12465BaseVaule() {
		return effect12465BaseVaule;
	}

	public int getEffect12465ContinueRound() {
		return effect12465ContinueRound;
	}

	public int getEffect12465CountMaxinum() {
		return effect12465CountMaxinum;
	}

	public String getEffect12465Adjust() {
		return effect12465Adjust;
	}

	public ImmutableMap<SoldierType, Integer> getEffect12465AdjustMap() {
		return effect12465AdjustMap;
	}

	public int getEffect12466Maxinum() {
		return effect12466Maxinum;
	}

	public int getEffect12468AtkNum() {
		return effect12468AtkNum;
	}

	public String getEffect12468RoundWeight() {
		return effect12468RoundWeight;
	}

	public ImmutableMap<Integer, Integer> getEffect12469AdjustMap() {
		return effect12469AdjustMap;
	}

	public int getEffect12469Maxinum() {
		return effect12469Maxinum;
	}

	public int getEffect12481BaseVaule() {
		return effect12481BaseVaule;
	}

	public int getEffect12482BaseVaule() {
		return effect12482BaseVaule;
	}

	public int getEffect12482CountMaxinum() {
		return effect12482CountMaxinum;
	}

	public int getEffect12461AdjustCof() {
		return effect12461AdjustCof;
	}

	public int getEffect12461ShareBaseVaule() {
		return effect12461ShareBaseVaule;
	}

	public int getEffect12461ShareCountMaxinum() {
		return effect12461ShareCountMaxinum;
	}

	public int getEffect12461GrowCof() {
		return effect12461GrowCof;
	}

	public int getEffect12461AdjustCountMaxinum() {
		return effect12461AdjustCountMaxinum;
	}

	public int getEffect12465GrowCof() {
		return effect12465GrowCof;
	}

	public int getEffect12465AirForceCountMaxinum() {
		return effect12465AirForceCountMaxinum;
	}

	public String getEffect12469Adjust() {
		return effect12469Adjust;
	}

	public void setEffect12465AdjustMap(ImmutableMap<SoldierType, Integer> effect12465AdjustMap) {
		this.effect12465AdjustMap = effect12465AdjustMap;
	}

	public void setEffect12469AdjustMap(ImmutableMap<Integer, Integer> effect12469AdjustMap) {
		this.effect12469AdjustMap = effect12469AdjustMap;
	}

	public int getEffect12116IntervalRound() {
		return effect12116IntervalRound;
	}

	public int getEffect12116ContinueRound() {
		return effect12116ContinueRound;
	}

	public int getEffect12116Maxinum() {
		return effect12116Maxinum;
	}

	public int getEffect12501AllNumLimit() {
		return effect12501AllNumLimit;
	}

	public int getEffect12501BaseVaule() {
		return effect12501BaseVaule;
	}

	public int getEffect12501Maxinum() {
		return effect12501Maxinum;
	}

	public int getEffect12502AllNumLimit() {
		return effect12502AllNumLimit;
	}

	public int getEffect12502Maxinum() {
		return effect12502Maxinum;
	}

	public int getEffect12503NumLimit() {
		return effect12503NumLimit;
	}

	public int getEffect12503Maxinum() {
		return effect12503Maxinum;
	}

	public int getEffect12504NumLimit() {
		return effect12504NumLimit;
	}

	public int getEffect12504Maxinum() {
		return effect12504Maxinum;
	}

	public boolean isItemPostEvnt(int itemId){
		try {
			return givenItemCostEventSet.contains(itemId);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
	}
	
	
	public List<Integer> checkBlazeMedalLimitAward(Map<Integer,Integer> itemRecord,List<Integer> awards){
		List<Integer> rlt = new ArrayList<>();
		rlt.addAll(awards);
		for(Entry<Integer, List<HawkTuple2<Integer, Integer>>> entry : this.blazeMedalLimitMap.entrySet()){
			List<HawkTuple2<Integer, Integer>> list = entry.getValue();
			for(HawkTuple2<Integer, Integer> tuple : list){
				int cnt = itemRecord.getOrDefault(tuple.first, 0);
				if(cnt < tuple.second){
					continue;
				}
				List<Integer> alist = this.blazemedalAwardMap.get(entry.getKey());
				if(Objects.isNull(alist)){
					continue;
				}
				rlt.removeAll(alist);
				break;
			}
		}
		return rlt;
	}
	
	
	
	public boolean checkBlazeMedalLimitItem(int itemId){
		for(Entry<Integer, List<HawkTuple2<Integer, Integer>>> entry : this.blazeMedalLimitMap.entrySet()){
			List<HawkTuple2<Integer, Integer>> list = entry.getValue();
			for(HawkTuple2<Integer, Integer> tuple : list){
				if(tuple.first == itemId){
					return true;
				}
			}
		}
		return false;
	}

	public String getBossEnemyIdList184() {
		return bossEnemyIdList184;
	}
	
	public int getFirstFreeBuildQueue() {
		return firstFreeBuildQueue;
	}

	
	public int getBuildContinuousUpgradeLimit() {
		return buildContinuousUpgradeLimit;
	}
	
	public int getBuildContinuousUpgradeCondition() {
		return buildContinuousUpgradeCondition;
	}
}
