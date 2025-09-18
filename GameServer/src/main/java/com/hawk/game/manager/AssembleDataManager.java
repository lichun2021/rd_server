package com.hawk.game.manager;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.hawk.activity.type.impl.achieve.AchieveManager;
import com.hawk.activity.type.impl.bestprize.cfg.BestPrizePoolCfg;
import com.hawk.activity.type.impl.cnyExam.cfg.CnyExamLevelCfg;
import com.hawk.activity.type.impl.plantSoldierFactory.cfg.PlantSoldierFactoryPoolCfg;
import com.hawk.activity.type.impl.supplyCrate.cfg.SupplyCrateDrawCfg;
import com.hawk.game.config.*;
import org.apache.commons.collections4.CollectionUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.hawk.activity.config.MergeServerTimeCfg;
import com.hawk.activity.type.impl.commandAcademy.cfg.CommandAcademyRankScoreCfg;
import com.hawk.activity.type.impl.commandAcademySimplify.cfg.CommandAcademySimplifyRankScoreCfg;
import com.hawk.activity.type.impl.groupBuy.cfg.GroupBuyActivityTimerCfg;
import com.hawk.activity.type.impl.hellfire.cfg.ActivityHellFireRankCfg;
import com.hawk.activity.type.impl.hellfire.cfg.ActivityHellFireTargetCfg;
import com.hawk.activity.type.impl.hellfirethree.cfg.ActivityHellFireThreeRankCfg;
import com.hawk.activity.type.impl.hellfirethree.cfg.ActivityHellFireThreeTargetCfg;
import com.hawk.activity.type.impl.hellfiretwo.cfg.ActivityHellFireTwoRankCfg;
import com.hawk.activity.type.impl.hellfiretwo.cfg.ActivityHellFireTwoTargetCfg;
import com.hawk.activity.type.impl.monthcard.cfg.MonthCardActivityCfg;
import com.hawk.activity.type.impl.spaceguard.cfg.SpaceGuardTimeCfg;
import com.hawk.activity.type.impl.strongestGuild.cfg.StrongestGuildCfg;
import com.hawk.game.GsConfig;
import com.hawk.game.data.PlayerImageData;
import com.hawk.game.item.ArmourAttrTemplate;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZSeasonTimeCfg;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZTimeCfg;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZTimeCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaConstCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaEnemyCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaLevelCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaStageCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaStrongholdCfg;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ToolType;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaStage;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.SuperWeaponAwardType;
import com.hawk.game.util.GsConst.XZQAwardType;

public class AssembleDataManager {

	/**
	 * 日志对象
	 */
	static Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 全局实例对象
	 */
	private static AssembleDataManager instance = null;

	/**
	 * 获取实例对象
	 *
	 * @return
	 */
	public static AssembleDataManager getInstance() {
		if (instance == null) {
			instance = new AssembleDataManager();
		}
		return instance;
	}

	/**
	 * 构造
	 *
	 */
	private AssembleDataManager() {
	}

	/**
	 * 初始化管理器
	 * 
	 * @return
	 */
	public boolean init() {
		return true;
	}

	// 战斗士兵最大等级
	private int maxSoldierLv = 0;

	// 类型id列表表 type-ids
	private Map<Integer, List<Integer>> mTypeIds = new HashMap<Integer, List<Integer>>();

	// 防御建筑 soldierId-buildingCfgId
	private Map<Integer, Integer> mDefBuildCfgId = new HashMap<Integer, Integer>();

	// 秒资源对应数量价格档位，同时受大本等级限制 resType-[cnt][price][cityLevel]
	// private Map<Integer, Integer[][]> mResTypeParams = new HashMap<Integer,
	// Integer[][]>();
	// 剧情任务配置
	private Table<Integer, Integer, StoryMissionCfg> storyMissionCfg = HashBasedTable.create();

	// 根据开服时间，在起服时就确定好每个serverAward的条目
	private Map<Integer, ServerAwardCfg> serverAwards = new HashMap<Integer, ServerAwardCfg>();

	private Table<Integer, Integer, TalentLevelCfg> talentLevelCfg = HashBasedTable.create();

	// 领地加成信息 <领地等级, buff作用号列表>
	private Map<Integer, List<int[]>> manorBuffs = new HashMap<>();

	// 领地加成信息 <领地等级, deBuff作用号列表>
	private Map<Integer, List<int[]>> manorDeBuffs = new HashMap<>();

	// 领地相关作用号ID
	private Set<Integer> manorEffectIds = new HashSet<>();

	// 科技技能id-科技id映射信息
	private Map<Integer, Integer> skill2TechMap = new HashMap<>();
	//
	private Map<Integer, List<TravelShopCfg>> travelShopCfgListMap = null;

	private Map<Integer, List<TravelShopCfg>> travelShopAssistCfgListMap = null;
	
	private Map<Integer, List<TravelShopCfg>> dragonBoatBenefitShopCfgListMap = null;
	
	/**
	 * 新版野怪 185活动专用<modelType, level, monsterId>
	 */
	private Table<Integer, Integer, WorldEnemyCfg> newMonsterIdsAct185 = HashBasedTable.create();

	/**
	 * 新版野怪 <modelType, level, monsterId>
	 */
	private Table<Integer, Integer, WorldEnemyCfg> newMonsterIds = HashBasedTable.create();

	/**
	 * 老版野怪 <modelType, level, monsterId>
	 */
	private Table<Integer, Integer, Integer> oldMonsterIds = HashBasedTable.create();

	/**
	 * 老版野怪 <modelType, level, cfg>
	 */
	private Table<Integer, Integer, WorldEnemyCfg> oldMonsterCfgs = HashBasedTable.create();

	/**
	 * 剧情战役 {chapterId, [PlotLevelCfg]}
	 */
	private Map<Integer, List<PlotLevelCfg>> plotLevelCfgListMap = null;
	/**
	 * 推送礼物{gropuId List<PushGiftLevelCfg}
	 */
	private Map<Integer, List<PushGiftLevelCfg>> pushGiftGroupPushGiftLevelListMap;
	/**
	 * 推送礼包 {condition, List<PushGiftGroupCfg>}
	 */
	private Map<Integer, List<PushGiftGroupCfg>> pushGiftConditionTypeGroupMap;
	/**
	 * 黑市礼包权重列表
	 */
	private List<Integer> travelShopGiftCfgWeigthList;
	/**
	 * vip 黑市礼包权重列表
	 */
	private List<Integer> vipTravelShopGiftCfgWeigthList;
	/**
	 * 黑市礼包列表
	 */
	private List<TravelShopGiftCfg> travelShopGiftCfgList;
	/**
	 * vip黑市礼包列表
	 */
	private List<TravelShopGiftCfg> vipTravelShopGfitCfgList;
	/**
	 * 新手引导黑市礼包
	 */
	private List<TravelShopGiftCfg> guideTravelGiftList;
	/**
	 * {targetId,[ActivityHellFireTargetCfg]>> 地狱火目标
	 */
	private Table<Integer, Integer, List<ActivityHellFireTargetCfg>> hellFireTargetTable;
	/**
	 * 地狱火 2 目标
	 */
	private Table<Integer, Integer, List<ActivityHellFireTwoTargetCfg>> hellFireTwoTargetTable;
	/**
	 * 地狱火 3目标
	 */
	private Table<Integer, Integer, List<ActivityHellFireThreeTargetCfg>> hellFireThreeTargetTable;
	/**
	 * {rankId, List<ActivityHellFireRankCfg>}
	 */
	private Map<Integer, List<ActivityHellFireRankCfg>> hellFireRankMap;
	/**
	 * 地狱火 2排行
	 */
	private Map<Integer, List<ActivityHellFireTwoRankCfg>> hellFireTwoRankMap;
	/**
	 * 地狱火 3排行
	 */
	private Map<Integer, List<ActivityHellFireThreeRankCfg>> hellFireThreeRankMap;
	/**
	 * {groupId, List<GiftCfg>>} 组ID对应的id
	 */
	private Map<Integer, List<GiftCfg>> giftListMap;
	/**
	 * {groupId, maxGiftId} 组Id对应的最大的礼包ID
	 */
	private Map<Integer, GiftCfg> groupMaxGiftCfgMap;
	/**
	 * {giftId, giftPoolId}
	 */
	private Map<Integer, Integer> groupIdPoolIdMap;

	/**
	 * 最大军衔经验
	 */
	private int maxMilitaryRankExp;

	/**
	 * 装扮配置 <dressType, modelType, dressCfg>
	 */
	private Table<Integer, Integer, DressCfg> dressCfgs = HashBasedTable.create();

	/**
	 * 累积在线
	 */
	private Table<Integer, Integer, AccumulateOnlineCfg> auucumulateOnlineCfg = HashBasedTable.create();

	/**
	 * 超级武器奖励
	 */
	private Table<Integer, SuperWeaponAwardType, List<SuperWeaponAwardCfg>> superWeaponAward = HashBasedTable.create();

	/**
	 * 玩家成就
	 */
	private Table<Integer, Integer, PlayerAchieveCfg> playerAchieve = HashBasedTable.create();

	private Map<Integer, SuperWeaponCfg> superWeaponCfgs;
	/**
	 * 资源礼包列表
	 */
	private Map<Integer, List<ResGiftLevelCfg>> resGiftLevelListMap;
	/**
	 * {vipLevel, List<VipShopCfg>} vip等级和vip商城配置列表
	 */
	private Map<Integer, List<VipShopCfg>> vipLevelCfgMap;

	/**
	 * 超级武器点
	 */
	private List<Integer> superWeaponPoints = new ArrayList<>();
	/**
	 * 排序后的联盟收藏
	 */
	private List<AllianceFileCfg> allianceFileList = new ArrayList<>();	
	/**
	 * 跨服状态 原服处理的的协议
	 */
	private Set<Integer> crossLocalProtocolSet = new HashSet<>();
	/**
	 * 跨服状态下 屏蔽的协议.
	 */
	private Set<Integer> crossShieldProtocolSet = new HashSet<>();
	/**
	 * 所有的区服对应的MergeServerGroupCfg edit 2019-04-20 配置提前更出本表的意义发生了扩展,
	 * 主服不管有没有合服都会出现在这里所以需要特殊处理 注意注意.
	 */
	private Map<String, MergeServerGroupCfg> mergeServerCfgMap = new HashMap<>();
	/**
	 * 在将要合服的列表里面. 主服务的信息会被写在这里.
	 */
	private Map<String, MergeServerGroupCfg> futureMergeServerCfgMap = new HashMap<>();
	/**
	 * 合服时间表.
	 */
	private Map<String, Long> mergeServerTimeMap = new HashMap<>();

	/**
	 * 铠甲属性配置
	 */
	private Table<Integer, Integer, List<ArmourAdditionalCfg>> armourAdditionTable = HashBasedTable.create();

	/**
	 * 超值礼包里面的直购礼包和payGift表的映射.
	 */
	private Map<String, List<Integer>> payGiftIdSuperGiftIdMap = new HashMap<>();

	/**
	 * 泰伯利亚联赛赛区
	 */
	private Map<String, Integer> tiberiumZoneMap = new HashMap<>();

	/**
	 * 泰伯利亚联赛时间信息
	 */
	private Map<Integer, HawkTuple2<Long, Long>> tlwTimeInfo = new HashMap<>();

	/**
	 * 星球大战分区.
	 */
	private Set<Integer> starWarsPartSet = new HashSet<>();
	/**
	 * 区服对应的partcfg;
	 */
	private Map<String, StarWarsPartCfg> serverPartCfgMap = new HashMap<>();

	/**
	 * 星球大战官职.
	 * {type, officerCfg}
	 */
	private Map<Integer, StarWarsOfficerCfg> starOfficerMap = new HashMap<>();
	/**
	 * 星球大战的分区
	 */
	private Table<Integer, Integer, StarWarsPartCfg> starWarsPartTable = HashBasedTable.create();
	
	/**
	 * 指挥官学院排行分数
	 */
	private Map<Integer,List<CommandAcademyRankScoreCfg> > academyRankScoreCfgMap = new HashMap<>();
	private Map<Integer,List<CommandAcademySimplifyRankScoreCfg> > academyRankSimplifyScoreCfgMap = new HashMap<>();

	/**
	 * {dressId}
	 */
	private Set<Integer> guardDressIdSet = new HashSet<>();
	/**
	 * }
	 */
	private Map<Integer, List<Integer>> guardDressListMap = new HashMap<>();
	/**
	 * 支付ID和
	 */
	private Map<String, List<Integer>> payGiftIdPushIdListMap = new HashMap<>();
	/**
	 * {serverId, CrossServerListCfg}
	 */
	private Map<String, CrossServerListCfg> serverCrossListMap = new HashMap<>();

	/**
	 * 装备科技 <researchId, level, EquipResearchLevelCfg>
	 */
	private Table<Integer, Integer, EquipResearchLevelCfg> equipResearchTable = HashBasedTable.create();
	
	private Table<Integer, Integer, EquipResearchRewardCfg> equipResearchRewardTable = HashBasedTable.create();
	
	/**
	 * 铠甲完美属性
	 */
	private Table<Integer, Integer, Integer> armourPerfectAttr = HashBasedTable.create();
	
	/**
	 * 铠甲神器完美属性
	 */
	private Table<Integer, Integer, Integer> armourSuperPerfectAttr = HashBasedTable.create();
	
	/**
	 * 推送礼包触发类型  @PushGiftConditionEnum 关联参数 <conditionType, paramSet>
	 */
	private Map<Integer, Set<Integer>> pushGiftTriggerParamMap = new HashMap<Integer, Set<Integer>>();
	
	private Table<Integer, Integer, ArmourStarCfg> armourStarTable = HashBasedTable.create();

	private Table<Integer, Integer, ArmourQuantumCfg> armourQuantumTable = HashBasedTable.create();

	
	
	/** 情报中心权重*/
	private Map<Integer,List<AgencyWeightCfg>> agencyWeightMap = new HashMap<>();


	/**
	 * 小战区奖励 pointId,类型, cfg 
	 */
	private static Table<Integer, XZQAwardType, List<XZQAwardCfg>> xzqAward = HashBasedTable.create();
	private static Table<Integer, XZQAwardType, List<XZQAwardCfg>> xzqFirstAward = HashBasedTable.create();
	private static Map<Integer, Set<Integer>> xzqBuidMap = new HashMap<Integer, Set<Integer>>();
	private static Map<Integer,Integer> xzqSpecialAwardMaxRound = new HashMap<>();
	
	/**
	 * 国家建筑对应的点
	 */
	private Map<Integer, Integer> nationalBuildingPoint = new HashMap<>();
	
	/**
	 * 国家科技
	 */
	private Table<Integer, Integer, NationTechCfg> nationTechTable = HashBasedTable.create();
	
	
	/**
	 * 排名权重
	 */
	private Map<Integer,List<TeamStrengthWeightCfg>> teamStrengthWeightMap = new HashMap<>();
	
	
	/**
	 * 根据travelShop里面的shopPool找对应的组
	 * 
	 * @param groupId
	 * @return
	 */
	public List<TravelShopCfg> getTravelShopCfgList(Integer shopPool) {
		return travelShopCfgListMap.get(shopPool);
	}

	public Map<Integer, List<TravelShopCfg>> getTravelShopCfgListMap() {
		return travelShopCfgListMap;
	}

	public List<TravelShopCfg> getTravelShopAssistCfgList(Integer shopPool) {
		return travelShopAssistCfgListMap.get(shopPool);
	}

	public Map<Integer, List<TravelShopCfg>> getTravelShopAssistCfgListMap() {
		return travelShopAssistCfgListMap;
	}
	
	public List<TravelShopCfg> getDragonBoatBenefitShopCfgList(Integer shopPool) {
		return dragonBoatBenefitShopCfgListMap.get(shopPool);
	}

	public Map<Integer, List<TravelShopCfg>> getDragonBoatBenefitShopCfgListMap() {
		return dragonBoatBenefitShopCfgListMap;
	}

	public int getMaxSoldierLv() {
		return maxSoldierLv;
	}

	/**
	 * 兵种类型id列表
	 */
	public Map<Integer, List<Integer>> getSoldierTypeMap() {
		return mTypeIds;
	}

	/**
	 * 根据建筑soldierId获取对应cfgId
	 * 
	 * @param soldierId
	 * @return
	 */
	public int getBuildingCfgId(int soldierId) {
		return mDefBuildCfgId.get(soldierId);
	}

	/**
	 * 获取建筑配置
	 * 
	 * @param buildingType
	 * @param level
	 * @return
	 */
	public BuildingCfg getBuildingCfg(BuildingType buildingType, int level) {
		ConfigIterator<BuildingCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(BuildingCfg.class);
		for (BuildingCfg cfg : cfgs) {
			if (cfg.getBuildType() == buildingType.getNumber() && cfg.getLevel() == level) {
				return cfg;
			}
		}
		return null;
	}

	public boolean isValidBuildingLevel(BuildingType buildingType, int level) {
		return Objects.nonNull(this.getBuildingCfg(buildingType, level));
	}

	/**
	 * 剧情任务配置
	 * 
	 * @param chapterId
	 * @param missionId
	 * @return
	 */
	public StoryMissionCfg getStoryMissionCfg(int chapterId, int missionId) {
		return storyMissionCfg.get(chapterId, missionId);
	}

	/**
	 * 剧情任务配置
	 * 
	 * @param chapterId
	 * @param missionId
	 * @return
	 */
	public Map<Integer, StoryMissionCfg> getStoryMissionCfg(int chapterId) {
		return storyMissionCfg.row(chapterId);
	}

	/**
	 * 获取一条配置奖励
	 * 
	 * @param serverAwardId
	 * @return
	 */
	public ServerAwardCfg getServerAwardByAwardId(int serverAwardId) {
		return serverAwards.get(serverAwardId);
	}

	/**
	 * 获取天赋等级配置
	 * 
	 * @return
	 */
	public TalentLevelCfg getTalentLevelCfg(int talentId, int level) {
		return talentLevelCfg.get(talentId, level);
	}

	/**
	 * 获取联盟领地增益buff
	 * 
	 * @param level
	 * @return
	 */
	public List<int[]> getManorBuff(int level) {
		if (!manorBuffs.containsKey(level)) {
			return new ArrayList<>();
		}
		return manorBuffs.get(level);
	}

	/**
	 * 获取联盟领地减益buff
	 * 
	 * @param level
	 * @return
	 */
	public List<int[]> getManorDeBuff(int level) {
		if (!manorDeBuffs.containsKey(level)) {
			return new ArrayList<>();
		}
		return manorDeBuffs.get(level);
	}

	/**
	 * 获取领地增益/减益作用号Id列表
	 * 
	 * @return
	 */
	public EffType[] getManorEffectTypes() {
		EffType[] effTypes = new EffType[manorEffectIds.size()];
		int index = 0;
		for (int effId : manorEffectIds) {
			effTypes[index] = EffType.valueOf(effId);
			index++;
		}
		return effTypes;
	}

	/**
	 * 根据科技技能ID获取对应的科技ID
	 * 
	 * @param skillId
	 * @return
	 */
	public int getTechIdBySkill(int skillId) {
		if (skill2TechMap.containsKey(skillId)) {
			return skill2TechMap.get(skillId);
		}
		return 0;
	}

	/**
	 * 获取科技技能ID-科技ID映射
	 * 
	 * @return
	 */
	public Map<Integer, Integer> getSkill2TechMap() {
		return skill2TechMap;
	}

	/**
	 * 清理已组装的数据
	 */
	public void clearData() {

		maxSoldierLv = 0;

		// 战斗士兵相关
		mTypeIds.clear();
		mDefBuildCfgId.clear();
		storyMissionCfg.clear();
		serverAwards.clear();
		talentLevelCfg.clear();
		manorBuffs.clear();
		manorDeBuffs.clear();
		manorEffectIds.clear();
		skill2TechMap.clear();
		newMonsterIds.clear();
	}

	/**
	 * 重新组装数据 assemble的 clear再组装的做法是有问题的. 参考 {@link #assembleNewMonster}
	 */
	public void doAssemble() {
		checkEnv();
		// 建筑配置信息
		ConfigIterator<BuildingCfg> buildingCfgs = HawkConfigManager.getInstance().getConfigIterator(BuildingCfg.class);
		while (buildingCfgs.hasNext()) {
			BuildingCfg cfg = buildingCfgs.next();
			if (cfg.getBattleSoldierId() != 0) {
				mDefBuildCfgId.put(cfg.getBattleSoldierId(), cfg.getId());
			}

		}
		// 剧情任务格式重组
		ConfigIterator<StoryMissionCfg> storyMissionCfgs = HawkConfigManager.getInstance()
				.getConfigIterator(StoryMissionCfg.class);
		while (storyMissionCfgs.hasNext()) {
			StoryMissionCfg cfg = storyMissionCfgs.next();
			storyMissionCfg.put(cfg.getChapter(), cfg.getId(), cfg);
		}

		// 加载按照开服时间取得的奖励配置
		ConfigIterator<ServerAwardCfg> awards = HawkConfigManager.getInstance().getConfigIterator(ServerAwardCfg.class);
		while (awards.hasNext()) {
			ServerAwardCfg cfg = awards.next();
			serverAwards.put(cfg.getServerAwardId(), cfg);
		}

		// 玩家天赋
		ConfigIterator<TalentLevelCfg> talentIter = HawkConfigManager.getInstance()
				.getConfigIterator(TalentLevelCfg.class);
		while (talentIter.hasNext()) {
			TalentLevelCfg cfg = talentIter.next();
			talentLevelCfg.put(cfg.getTalentId(), cfg.getLevel(), cfg);
		}

		// 科技解锁技能信息
		ConfigIterator<TechnologyCfg> techIt = HawkConfigManager.getInstance().getConfigIterator(TechnologyCfg.class);
		while (talentIter.hasNext()) {
			TechnologyCfg cfg = techIt.next();
			if (cfg.getTechSkill() != 0) {
				skill2TechMap.put(cfg.getTechSkill(), cfg.getTechId());
			}
		}

		// 领地增益/减益数据组装
		ConfigIterator<AllianceFunctionCfg> fuctions = HawkConfigManager.getInstance()
				.getConfigIterator(AllianceFunctionCfg.class);
		while (fuctions.hasNext()) {
			AllianceFunctionCfg cfg = fuctions.next();
			List<int[]> effs = cfg.getEffList();
			for (int i = 0; i < effs.size(); i++) {
				manorEffectIds.add(effs.get(i)[0]);
				if (cfg.getType() == GsConst.MANOR_BUFF_TYPE) {
					if (!manorBuffs.containsKey(i + 1)) {
						manorBuffs.put(i + 1, new ArrayList<>());
					}
					manorBuffs.get(i + 1).add(effs.get(i));
				} else {
					if (!manorDeBuffs.containsKey(i + 1)) {
						manorDeBuffs.put(i + 1, new ArrayList<>());
					}
					manorDeBuffs.get(i + 1).add(effs.get(i));
				}
			}
		}

		int militaryConfigSize = HawkConfigManager.getInstance().getConfigSize(MilitaryRankCfg.class);
		if (militaryConfigSize > 0) {
			MilitaryRankCfg militaryRankCfg = HawkConfigManager.getInstance().getConfigByIndex(MilitaryRankCfg.class,
					militaryConfigSize - 1);
			maxMilitaryRankExp = militaryRankCfg.getRankExp();
		}

		Table<Integer, Integer, DressCfg> dressCfgs = HashBasedTable.create();
		ConfigIterator<DressCfg> dressCfgIterator = HawkConfigManager.getInstance().getConfigIterator(DressCfg.class);
		while (dressCfgIterator.hasNext()) {
			DressCfg dressCfg = dressCfgIterator.next();
			dressCfgs.put(dressCfg.getDressType(), dressCfg.getModelType(), dressCfg);
		}
		this.dressCfgs = dressCfgs;

		Table<Integer, Integer, AccumulateOnlineCfg> auucumulateOnlineCfg = HashBasedTable.create();
		ConfigIterator<AccumulateOnlineCfg> auucumulateOnlineIterator = HawkConfigManager.getInstance()
				.getConfigIterator(AccumulateOnlineCfg.class);
		while (auucumulateOnlineIterator.hasNext()) {
			AccumulateOnlineCfg cfg = auucumulateOnlineIterator.next();
			auucumulateOnlineCfg.put(cfg.getDayCount(), cfg.getPosId(), cfg);
		}
		this.auucumulateOnlineCfg = auucumulateOnlineCfg;

		Table<Integer, Integer, PlayerAchieveCfg> playerAchieve = HashBasedTable.create();
		ConfigIterator<PlayerAchieveCfg> playerAchieveIterator = HawkConfigManager.getInstance()
				.getConfigIterator(PlayerAchieveCfg.class);
		while (playerAchieveIterator.hasNext()) {
			PlayerAchieveCfg cfg = playerAchieveIterator.next();
			playerAchieve.put(cfg.getGroupId(), cfg.getLevel(), cfg);
		}
		this.playerAchieve = playerAchieve;

		// 铠甲属性配置
		Table<Integer, Integer, List<ArmourAdditionalCfg>> armourAdditionTable = HashBasedTable.create();
		ConfigIterator<ArmourAdditionalCfg> armourAddIterator = HawkConfigManager.getInstance()
				.getConfigIterator(ArmourAdditionalCfg.class);
		while (armourAddIterator.hasNext()) {
			ArmourAdditionalCfg cfg = armourAddIterator.next();
			List<ArmourAdditionalCfg> list = armourAdditionTable.get(cfg.getType(), cfg.getQuality());
			if (list == null) {
				list = new ArrayList<>();
			}
			list.add(cfg);
			armourAdditionTable.put(cfg.getType(), cfg.getQuality(), list);
		}
		this.armourAdditionTable = armourAdditionTable;

		
		Table<Integer, Integer, Integer> armourPerfectAttr = HashBasedTable.create();
		Table<Integer, Integer, Integer> armourSuperPerfectAttr = HashBasedTable.create();
		ConfigIterator<ArmourAdditionalCfg> armourAddIterator1 = HawkConfigManager.getInstance().getConfigIterator(ArmourAdditionalCfg.class);
		while (armourAddIterator1.hasNext()) {
			ArmourAdditionalCfg cfg = armourAddIterator1.next();
			int type = cfg.getType();
			int quality = cfg.getQuality();
			for (ArmourAttrTemplate attr : cfg.getAttrList()) {
				int effect = attr.getEffect();
				int randMax = attr.getRandMax();
				if (type == 1) {
					Integer before = armourPerfectAttr.get(quality, effect);
					if (before == null || before < randMax) {
						armourPerfectAttr.put(quality, effect, randMax);
					}
				} else {
					Integer before = armourSuperPerfectAttr.get(quality, effect);
					if (before == null || before < randMax) {
						armourSuperPerfectAttr.put(quality, effect, randMax);
					}
				}
			}
		}
		this.armourPerfectAttr = armourPerfectAttr;
		this.armourSuperPerfectAttr = armourSuperPerfectAttr;
		
		Table<Integer, Integer, ArmourStarCfg> armourStarTable = HashBasedTable.create();
		ConfigIterator<ArmourStarCfg> armourStarCfgIter = HawkConfigManager.getInstance().getConfigIterator(ArmourStarCfg.class);
		while (armourStarCfgIter.hasNext()) {
			ArmourStarCfg cfg = armourStarCfgIter.next();
			armourStarTable.put(cfg.getArmourId(), cfg.getStarLevel(), cfg);
		}
		this.armourStarTable = armourStarTable;


		Table<Integer, Integer, ArmourQuantumCfg> armourQuantumTable = HashBasedTable.create();
		ConfigIterator<ArmourQuantumCfg> armourQuantumCfgIter = HawkConfigManager.getInstance().getConfigIterator(ArmourQuantumCfg.class);
		while (armourQuantumCfgIter.hasNext()) {
			ArmourQuantumCfg cfg = armourQuantumCfgIter.next();
			armourQuantumTable.put(cfg.getArmourId(), cfg.getLevel(), cfg);
		}
		this.armourQuantumTable = armourQuantumTable;
		
		// 黑市商人
		assembleTravelShop();
		// 黑市商人活动数据
		assembleTravelShopAssist();
		//黑市商人端午特惠活动数据
		assembleDragonBoatBenefitShop();
		// rts战斗
		assemblePlotBattle();
		// 推送礼包
		assemblePushGift();
		// 黑市商人推送礼包
		assembleTravelShopGift();
		// 新版野怪
		assembleNewMonster();
		// 地狱火
		assemblHellFire();
		// 超值礼包
		assemblGift();
		// 资源礼包
		assemblResGift();
		// 超级武器
		assembleSuperWeapon();
		// vip商城礼包
		assembleVipShop();
		// 联盟收藏
		assembleAllianceFile();		
		// 跨服相关的特殊协议
		assembleCrossSpecialProtocol();
		// 合并区服
		assembleMergeServer();
		// 合并区服的时间.
		assembleMergeServerTime();
		// 联合检测合服和合服时间
		assembleCheckMergeServer();
		// 泰伯利亚联赛赛区
		assembleTiberiumZone();
		// 泰伯利亚联赛时间
		assembleTiberiumSeasonTime();
		// 星球大战
		assembleStarWars();

		// 跨服,九鼎,合服联合校验. 还是放在最后面吧.
		veryImportantCheck();
		
		//指挥官学院
		assembleRankScoreCfg();
		assembleGuardDress();
		//推送礼包ID和直购ID
		assemblePushGiftPay();
		
		// 装备科技
		assembleEquipResearchCfg();
		assembleEquipResearchRewardCfg();
		
		assemblePushGiftOfUseItemType();

		assAgencyWeightCfg();
		
		assembleXZQAwardCfg();
		assembleXZQPointCfg();
		assembleXZQSpecialAwardCfg();
		
		// 初始化国家建筑坐标
		assembleNationBuilding();
		// 国家科技
		assembleNationTechCfg();
		
		//排名权重
		assembleTeamStrengthWeightCfg();
		// 检查月卡配置中配置的道具
		monthCardItemCheck();
		// 星甲召唤活动时间检测
		spaceMechaConfigCheck();
		BestPrizePoolCfg.doCheck();
		CnyExamLevelCfg.doAssemble();
		TechnologyCfg.doAssemble();
		ArmourStarExploreUpgradeCfg.doAssemble();
		SupplyCrateDrawCfg.doAssemble();
		PlantSoldierFactoryPoolCfg.doAssemble();
		XHJZSeasonRankRewrdCfg.doAssemble();
		XHJZSeasonTargetRewardCfg.doAssemble();
		XQHXTalentLevelCfg.doAssemble();
		AchieveManager.getInstance().getAchieveConfigAndProviderMap().clear();
	}
	
	/**
	 * 检查cfg/gameConst.cfg中的testEnv配置
	 */
	private void checkEnv() {
		String serverId = GsConfig.getInstance().getServerId();
		if (GsConfig.getInstance().isDebug() && Integer.parseInt(serverId)/10000 == 1 && !GameConstCfg.getInstance().isTestEnv()) {
			throw new InvalidParameterException("testEnv in cfg/gameConst.cfg error");
		}
	}
	
	/**
	 * 星甲召唤活动时间检测
	 */
	private void spaceMechaConfigCheck() {
		long now = HawkTime.getMillisecond();
		ConfigIterator<SpaceGuardTimeCfg> timeCfgIterator = HawkConfigManager.getInstance().getConfigIterator(SpaceGuardTimeCfg.class);
		while (timeCfgIterator.hasNext()) {
			SpaceGuardTimeCfg timeCfg = timeCfgIterator.next();
			if (timeCfg.getHiddenTimeValue() <= now) {
				continue;
			}
			
			// 时间不要卡得太紧，这里取一分钟的阈值
			if (timeCfg.getHiddenTimeValue() - timeCfg.getStopTimeValue() < SpaceMechaStageCfg.getTotalTime() + 60000L) {
				throw new InvalidParameterException(String.format("space_machine_guard_time.xml termId: %d, invalid config", timeCfg.getTermId()));
			}
		}
		
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		long monsterMarchTime = cfg.getEnemyMarchTime();
		SpaceMechaStageCfg stageCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaStageCfg.class, SpaceMechaStage.SPACE_GUARD_1_VALUE);
		ConfigIterator<SpaceMechaLevelCfg> levelCfgIterator = HawkConfigManager.getInstance().getConfigIterator(SpaceMechaLevelCfg.class);
		while (levelCfgIterator.hasNext()) {
			SpaceMechaLevelCfg levelCfg = levelCfgIterator.next();
			long wavePushMarchCd = levelCfg.getStage1WaveCd();
			long createMonsterCd = levelCfg.getStage1RefreshCd();
			long roundWholeTime = wavePushMarchCd + monsterMarchTime + createMonsterCd;
			if (levelCfg.getSpEnemyWaveMin() > 0) {
				long firstSubSpaceMarchPushTime = (levelCfg.getSpEnemyWaveMin() - 1) * roundWholeTime + wavePushMarchCd;
				if (cfg.getSubcabinFirstWaveTime() < firstSubSpaceMarchPushTime) {
					throw new InvalidParameterException(String.format("space_machine_const.xml -> subcabinFirstWaveTime & space_mechine_level.xml -> %d, invalid config", levelCfg.getId()));
				}
			}
			
			int totalWave = levelCfg.getStage1Wave();
			if (roundWholeTime * totalWave > stageCfg.getTimeLong()) {
				throw new InvalidParameterException(String.format("space_mechine_level.xml -> %d stage1 time error", levelCfg.getId()));
			}
			
			for (int enemyId :levelCfg.getStage1EnemyIdList()) {
				SpaceMechaEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaEnemyCfg.class, enemyId);
				if (enemyCfg == null) {
					throw new InvalidParameterException(String.format("space_mechine_level.xml -> %d, enemy config error -> enemyId: %d", levelCfg.getId(), enemyId));
				}
			}
			
			for (int enemyId :levelCfg.getStage1SpEnemyIdList()) {
				SpaceMechaEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaEnemyCfg.class, enemyId);
				if (enemyCfg == null) {
					throw new InvalidParameterException(String.format("space_mechine_level.xml -> %d, enemy config error -> enemyId: %d", levelCfg.getId(), enemyId));
				}
			}
			
			SpaceMechaEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaEnemyCfg.class, levelCfg.getStage3Boss());
			if (enemyCfg == null) {
				throw new InvalidParameterException(String.format("space_mechine_level.xml -> %d, stage3Boss enemy config error -> enemyId: %d", levelCfg.getId(), levelCfg.getStage3Boss()));
			}
		}
		
		stageCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaStageCfg.class, SpaceMechaStage.SPACE_GUARD_2_VALUE);
		ConfigIterator<SpaceMechaStrongholdCfg> strongHoldIterator = HawkConfigManager.getInstance().getConfigIterator(SpaceMechaStrongholdCfg.class);
		while (strongHoldIterator.hasNext()) {
			SpaceMechaStrongholdCfg strongholdCfg = strongHoldIterator.next();
			if (strongholdCfg.getAtkWaveCd() <= monsterMarchTime) {
				throw new InvalidParameterException(String.format("space_machine_stronghold.xml -> %d atkWaveCd time error", strongholdCfg.getId()));
			}
			
			long totalTime = strongholdCfg.getAtkWaveCd() * strongholdCfg.getAtkWave() + cfg.getStrongholdFirstWaveTime();
			if (totalTime > stageCfg.getTimeLong()) {
				throw new InvalidParameterException(String.format("space_machine_stronghold.xml -> %d stage2 time error", strongholdCfg.getId()));
			}
			
			for (int enemyId : strongholdCfg.getAtkEnemyIdList()) {
				SpaceMechaEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaEnemyCfg.class, enemyId);
				if (enemyCfg == null) {
					throw new InvalidParameterException(String.format("space_machine_stronghold.xml -> %d, enemy config error -> enemyId: %d", strongholdCfg.getId(), enemyId));
				}
			}
		}
		
		stageCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaStageCfg.class, SpaceMechaStage.SPACE_GUARD_3_VALUE);
		long stage3Time = cfg.getBossMarchTime() + monsterMarchTime;
		if (stage3Time >= stageCfg.getTimeLong()) {
			throw new InvalidParameterException(String.format("space_machine_stage.xml -> %d stage3 time error", stageCfg.getId()));
		}
		
		
	}
	
	/**
	 * 检查月卡配置中配置的道具
	 */
	private void monthCardItemCheck() {
		ConfigIterator<MonthCardActivityCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(MonthCardActivityCfg.class);
		while (iterator.hasNext()) {
			MonthCardActivityCfg cardCfg = iterator.next();
			if (HawkOSOperator.isEmptyString(cardCfg.getCardItem())) {
				continue;
			}
			
			List<ItemInfo> itemList = ItemInfo.valueListOf(cardCfg.getCardItem());
			if (itemList.isEmpty()) {
				throw new InvalidParameterException(String.format("monthCardData.xml id: %d -> cardItem: %s, invalid config", cardCfg.getCardId(), cardCfg.getCardItem()));
			}
			
			for (ItemInfo item : itemList) {
				ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, item.getItemId());
				if (itemCfg == null || itemCfg.getItemType() != ToolType.MONTHCARD_ACTIVE_ITEM_VALUE) {
					throw new InvalidParameterException(String.format("monthCardData.xml id: %d -> cardItem: %s, item not exist or itemType not match", cardCfg.getCardId(), cardCfg.getCardItem()));
				}
			}
		}
	}
	
	private void assAgencyWeightCfg(){
		Map<Integer,List<AgencyWeightCfg>> map = new HashMap<>();
		List<AgencyWeightCfg> cfgs = HawkConfigManager.getInstance().
				getConfigIterator(AgencyWeightCfg.class).toList();
		for(AgencyWeightCfg cfg : cfgs){
			List<AgencyWeightCfg> list = map.get(cfg.getGroup());
			if(list == null){
				list = new ArrayList<>();
				map.put(cfg.getGroup(), list);
			}
			list.add(cfg);
		}
		this.agencyWeightMap = ImmutableMap.copyOf(map);
	}
	
	
	private void assembleTeamStrengthWeightCfg(){
		Map<Integer,List<TeamStrengthWeightCfg>> map = new HashMap<>();
		List<TeamStrengthWeightCfg> cfgs = HawkConfigManager.getInstance().
				getConfigIterator(TeamStrengthWeightCfg.class).toList();
		for(TeamStrengthWeightCfg cfg : cfgs){
			List<TeamStrengthWeightCfg> list = map.get(cfg.getType());
			if(list == null){
				list = new ArrayList<>();
				map.put(cfg.getType(), list);
			}
			list.add(cfg);
		}
		this.teamStrengthWeightMap = ImmutableMap.copyOf(map);
	}


	

	private static void assembleXZQSpecialAwardCfg() {
		Map<Integer,Integer> specialAwardMaxRound = new HashMap<>();
		ConfigIterator<XZQSpecialAwardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(XZQSpecialAwardCfg.class);
		while (configIterator.hasNext()) {
			XZQSpecialAwardCfg config = configIterator.next();
			int pointId = GameUtil.combineXAndY(config.getX(), config.getY());
			specialAwardMaxRound.merge(pointId, config.getRound(), (v1, v2) -> Math.max(v1, v2));
		}
		
		xzqSpecialAwardMaxRound = ImmutableMap.copyOf(specialAwardMaxRound);
	}
	
	
	
	private static void assembleXZQPointCfg() {
		Map<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
		ConfigIterator<XZQPointCfg> pointCfgIterator = HawkConfigManager.getInstance().getConfigIterator(XZQPointCfg.class);
		while (pointCfgIterator.hasNext()) {
			XZQPointCfg cfg = pointCfgIterator.next();
			Set<Integer> set = map.get(cfg.getLevel());
			if (set == null) {
				set = new HashSet<>();
				map.put(cfg.getLevel(), set);
			}
			set.add(cfg.getId());
		}
		xzqBuidMap = map;
	}

	
	private void assembleXZQAwardCfg() {
		Table<Integer, XZQAwardType, List<XZQAwardCfg>> superWeaponAward = HashBasedTable.create();
		Table<Integer, XZQAwardType, List<XZQAwardCfg>> firstAward = HashBasedTable.create();
		ConfigIterator<XZQPointCfg> pointIterator = HawkConfigManager.getInstance().getConfigIterator(XZQPointCfg.class);
		while (pointIterator.hasNext()) {
			XZQPointCfg cfg = pointIterator.next();
			List<Integer> awards = cfg.getAwards();
			int pointId = GameUtil.combineXAndY(cfg.getX(), cfg.getY());
			for(int aid : awards){
				XZQAwardCfg acfg = HawkConfigManager.getInstance().getConfigByKey(XZQAwardCfg.class, aid);
				List<XZQAwardCfg> awardList = superWeaponAward.get(pointId,
						XZQAwardType.valueOf(acfg.getType()));
				if (awardList == null) {
					awardList = new ArrayList<>();
					superWeaponAward.put(pointId, XZQAwardType.valueOf(acfg.getType()), awardList);
				}
				awardList.add(acfg);
			}
			
			List<Integer> firstAwards = cfg.getFirstAwards();
			for(int aid : firstAwards){
				XZQAwardCfg acfg = HawkConfigManager.getInstance().getConfigByKey(XZQAwardCfg.class, aid);
				List<XZQAwardCfg> awardList = firstAward.get(pointId,
						XZQAwardType.valueOf(acfg.getType()));
				if (awardList == null) {
					awardList = new ArrayList<>();
					firstAward.put(pointId, XZQAwardType.valueOf(acfg.getType()), awardList);
				}
				awardList.add(acfg);
			}
		}
		xzqAward = superWeaponAward;
		xzqFirstAward = firstAward;
		
		
	}

	/**
	 * 筛选 推送礼包类型中的 使用指定道具触发类型相关的道具ID
	 */
	private void assemblePushGiftOfUseItemType() {
		Set<Integer> itemConsumeParamSet = new HashSet<Integer>();
		pushGiftTriggerParamMap.put(PushGiftConditionEnum.ITEM_CONSUME.getType(), itemConsumeParamSet);
		for (Integer groupId : PushGiftGroupCfg.getGroupIdsByConditionType(PushGiftConditionEnum.ITEM_CONSUME)) {
			ConfigIterator<PushGiftLevelCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(PushGiftLevelCfg.class);
			while (cfgIterator.hasNext()) {
				PushGiftLevelCfg levelCfg = cfgIterator.next();
				if (levelCfg.getGroupId() == groupId) {
					itemConsumeParamSet.add(levelCfg.getParamList().get(0));
				}
			}
		}
		
		Set<Integer> CommanderExpParamSet = new HashSet<Integer>();
		pushGiftTriggerParamMap.put(PushGiftConditionEnum.COMMANDER_EXP.getType(), CommanderExpParamSet);
		for (Integer groupId : PushGiftGroupCfg.getGroupIdsByConditionType(PushGiftConditionEnum.COMMANDER_EXP)) {
			ConfigIterator<PushGiftLevelCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(PushGiftLevelCfg.class);
			while (cfgIterator.hasNext()) {
				PushGiftLevelCfg levelCfg = cfgIterator.next();
				if (levelCfg.getGroupId() == groupId) {
					CommanderExpParamSet.add(levelCfg.getParamList().get(0));
				}
			}
		}
	}

	public Set<Integer> getPushGiftTriggerParamSet(PushGiftConditionEnum conditionType) {
		return pushGiftTriggerParamMap.getOrDefault(conditionType.getType(), Collections.emptySet());
	}

	private void assemblePushGiftPay() {
		Map<String, List<Integer>> map = new HashMap<>();
		ConfigIterator<PushGiftLevelCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(PushGiftLevelCfg.class);
		while (cfgIterator.hasNext()) {
			PushGiftLevelCfg levelCfg = cfgIterator.next();
			List<Integer> idList = null;
			if (!HawkOSOperator.isEmptyString(levelCfg.getIosPayId())) {
				PayGiftCfg payGiftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, levelCfg.getIosPayId());
				if (payGiftCfg == null) {
					throw new InvalidParameterException(String.format("push_gift_level.xml id:{} iosPayId can not found in payGift.xml", levelCfg.getIosPayId()));
				}
				
				idList = map.get(levelCfg.getIosPayId());
				if (idList == null) {
					idList = new ArrayList<>();
					map.put(levelCfg.getIosPayId(), idList);
				}
				idList.add(levelCfg.getId());
			}	
			
			if (!HawkOSOperator.isEmptyString(levelCfg.getAndroidPayId())) {
				PayGiftCfg payGiftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, levelCfg.getAndroidPayId());
				if (payGiftCfg == null) {
					throw new InvalidParameterException(String.format("push_gift_level.xml id:{} androidPayId can not found in payGift.xml", levelCfg.getAndroidPayId()));
				}
				
				idList = map.get(levelCfg.getAndroidPayId());
				if (idList == null) {
					idList = new ArrayList<>();
					map.put(levelCfg.getAndroidPayId(), idList);
				}
				idList.add(levelCfg.getId());
			}	
		}
		this.payGiftIdPushIdListMap = map;
	}

	private void assembleGuardDress() {
		ConfigIterator<GuardianItemDressCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(GuardianItemDressCfg.class);
		Set<Integer> guardDressIdSet = new HashSet<>();
		Map<Integer, List<Integer>> guardDressListMap = new HashMap<>();
		while (cfgIterator.hasNext()) {
			GuardianItemDressCfg itemDressCfg = cfgIterator.next();
			guardDressIdSet.addAll(itemDressCfg.getDressIdList());
			List<Integer> copyList = new ArrayList<>(itemDressCfg.getDressIdList());
			for (Integer dressid : itemDressCfg.getDressIdList()) {				
				guardDressListMap.put(dressid, copyList);
			}
		}
		
		this.guardDressIdSet = guardDressIdSet;
		this.guardDressListMap = guardDressListMap;
	}

	private void assembleRankScoreCfg() {
		List<CommandAcademyRankScoreCfg> cfgs = HawkConfigManager.getInstance().
				getConfigIterator(CommandAcademyRankScoreCfg.class).toList();
		for(CommandAcademyRankScoreCfg cfg : cfgs){
			List<CommandAcademyRankScoreCfg> list = this.academyRankScoreCfgMap.get(cfg.getRankId());
			if(list == null){
				list = new ArrayList<>();
				this.academyRankScoreCfgMap.put(cfg.getRankId(), list);
			}
			list.add(cfg);
		}
		
		
		List<CommandAcademySimplifyRankScoreCfg> cfgList = HawkConfigManager.getInstance().
				getConfigIterator(CommandAcademySimplifyRankScoreCfg.class).toList();
		for(CommandAcademySimplifyRankScoreCfg cfg : cfgList){
			List<CommandAcademySimplifyRankScoreCfg> list = this.academyRankSimplifyScoreCfgMap.get(cfg.getRankId());
			if(list == null){
				list = new ArrayList<>();
				this.academyRankSimplifyScoreCfgMap.put(cfg.getRankId(), list);
			}
			list.add(cfg);
		}
		
	}

	private void assembleStarWars() {
		Set<Integer> partSet = new HashSet<>();
		ConfigIterator<StarWarsPartCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(StarWarsPartCfg.class);
		Map<String, StarWarsPartCfg> serverPartMap = new HashMap<>();
		Map<Integer, StarWarsOfficerCfg> officerMap = new HashMap<>();
		Table<Integer, Integer, StarWarsPartCfg> localPartTable = HashBasedTable.create();
		cfgIterator.stream().forEach(cfg -> {					
			if (!GsConfig.getInstance().getAreaId().equals(cfg.getAreaId())) {
				return;
			}
			localPartTable.put(cfg.getZone(), cfg.getTeam(), cfg);
			partSet.add(cfg.getZone());
			//非0的做一个映射.
			if (cfg.getTeam() != GsConst.StarWarsConst.TEAM_NONE) {
				cfg.getServerList().forEach(serverId->{
					serverPartMap.put(serverId, cfg);
				});
			}
		});

		ConfigIterator<StarWarsOfficerCfg> officerIterator = HawkConfigManager.getInstance().getConfigIterator(StarWarsOfficerCfg.class);
		officerIterator.stream().forEach(officerCfg -> {
			officerMap.put(officerCfg.getLevel(), officerCfg);
		});

		this.starOfficerMap = officerMap;
		this.starWarsPartSet = partSet;
		this.serverPartCfgMap = serverPartMap;
		this.starWarsPartTable = localPartTable;
	}

	private void assembleTiberiumZone() {
		String areaId = GsConfig.getInstance().getAreaId();
		Map<String, Integer> zoneMap = new HashMap<>();
		ConfigIterator<TiberiumZoneCfg> crossTimeIterator = HawkConfigManager.getInstance()
				.getConfigIterator(TiberiumZoneCfg.class);
		for (TiberiumZoneCfg cfg : crossTimeIterator) {
			if (areaId.equals(cfg.getAreaId())) {
				for (String server : cfg.getServerList()) {
					zoneMap.put(server, cfg.getZone());
				}
			}
		}
		this.tiberiumZoneMap = zoneMap;
	}

	/**
	 * 组装泰伯利亚联赛时间
	 */
	private void assembleTiberiumSeasonTime() {
		Map<Integer, HawkTuple2<Long, Long>> _tlwTimeInfo = new HashMap<>();
		Map<Integer, Long> startMap = new HashMap<>();
		Map<Integer, Long> endMap = new HashMap<>();
		ConfigIterator<TiberiumSeasonTimeCfg> its = HawkConfigManager.getInstance()
				.getConfigIterator(TiberiumSeasonTimeCfg.class);
		for (TiberiumSeasonTimeCfg cfg : its) {
			int season = cfg.getSeason();
			long startTime = cfg.getSeasonStartTimeValue();
			long endTime = cfg.getSeasonEndTimeValue();
			if (startTime > 0) {
				startMap.put(season, startTime);
			}
			if (endTime > 0) {
				endMap.put(season, endTime);
			}
		}
		for (Entry<Integer, Long> entry : startMap.entrySet()) {
			int season = entry.getKey();
			HawkTuple2<Long, Long> tuple = new HawkTuple2<Long, Long>(startMap.get(season), endMap.get(season));
			_tlwTimeInfo.put(season, tuple);
		}
		this.tlwTimeInfo = _tlwTimeInfo;
	}
	
	/**
	 * 联合检测.
	 */
	private void veryImportantCheck() {
		// 先找出在当前，或者将来的时间集合.
		List<CrossTimeCfg> crossTimeList = this.getFutureCrossTimeList();
		List<MergeServerTimeCfg> mergeTimeList = this.getFutureMergeServerTimeList();
		List<TiberiumTimeCfg> tiberiumTimeList = this.getFutureTiberiumnList();
		List<StarWarsTimeCfg> starWarsTimeList = this.getFutureStarWarsList();			
		// 校验跨服和泰伯利亚和合服.
		for (CrossTimeCfg crossTimeCfg : crossTimeList) {							
			String msg = "跨服 cross_time.xml id:" + crossTimeCfg.getTermId();
			this.isCrossOverTiberiumCfg(crossTimeCfg.getStartTimeValue(), crossTimeCfg.getAwardTimeValue(),
					"", msg, false);
			this.isCrossOverMergeServerCfg(crossTimeCfg.getStartTimeValue(), crossTimeCfg.getAwardTimeValue(),
					"", msg);
			this.isCrossOverStarWarsCfg(crossTimeCfg.getStartTimeValue(), crossTimeCfg.getAwardTimeValue(),
					"", msg);
			this.isCrossOverCyborgCfg(crossTimeCfg.getStartTimeValue(), crossTimeCfg.getAwardTimeValue(), "", msg, false);
			// 万人团购和跨服时间交叉检测
			this.isCrossOverGroupBuyActivityCfg(crossTimeCfg.getStartTimeValue(), crossTimeCfg.getHiddenTimeValue(), msg);
			//月球副本和跨服时间交叉检测
			this.isCrossOverYQZZCfg(crossTimeCfg.getStartTimeValue(), crossTimeCfg.getHiddenTimeValue(), msg);
		}
		
		//泰伯利亚无法迭代,所以只能用其它的配置去做迭代.		
		Set<String> allStarWarsServer = new HashSet<>();
		HawkConfigManager.getInstance().getConfigIterator(StarWarsPartCfg.class).stream()
				.forEach(x -> allStarWarsServer.addAll(x.getServerList()));
		for (StarWarsTimeCfg starWarsTimeCfg : starWarsTimeList) {
			for (String serverId : allStarWarsServer) {
				String msg = "星球大战 star_wars_time.xml id:" + starWarsTimeCfg.getTermId();
				this.isCrossOverTiberiumCfg(starWarsTimeCfg.getWarStartTimeOneValue(), starWarsTimeCfg.getWarEndTimeOneValue(), 
						serverId, msg, false);
				this.isCrossOverTiberiumCfg(starWarsTimeCfg.getWarStartTimeTwoValue(), starWarsTimeCfg.getWarEndTimeTwoValue(), 
						serverId, msg, false);
				this.isCrossOverTiberiumCfg(starWarsTimeCfg.getWarStartTimeThreeValue(), starWarsTimeCfg.getWarEndTimeThreeValue(), 
						serverId, msg, false);
				this.isCrossOverMergeServerCfg(starWarsTimeCfg.getWarStartTimeOneValue(), starWarsTimeCfg.getWarEndTimeOneValue(), 
						serverId, msg);
				this.isCrossOverMergeServerCfg(starWarsTimeCfg.getWarStartTimeTwoValue(), starWarsTimeCfg.getWarEndTimeTwoValue(), 
						serverId, msg);
				this.isCrossOverMergeServerCfg(starWarsTimeCfg.getWarStartTimeThreeValue(), starWarsTimeCfg.getWarEndTimeThreeValue(), 
						serverId, msg);
				this.isCrossOverCyborgCfg(starWarsTimeCfg.getWarStartTimeOneValue(), starWarsTimeCfg.getWarEndTimeOneValue(), 
						serverId, msg, false);
				this.isCrossOverCyborgCfg(starWarsTimeCfg.getWarStartTimeTwoValue(), starWarsTimeCfg.getWarEndTimeTwoValue(), 
						serverId, msg, false);
				this.isCrossOverCyborgCfg(starWarsTimeCfg.getWarStartTimeThreeValue(), starWarsTimeCfg.getWarEndTimeThreeValue(),
						serverId, msg, false);
				
				this.isCrossOverYQZZCfg(starWarsTimeCfg.getWarStartTimeOneValue(), starWarsTimeCfg.getWarEndTimeOneValue(), msg);
				this.isCrossOverYQZZCfg(starWarsTimeCfg.getWarStartTimeTwoValue(), starWarsTimeCfg.getWarEndTimeTwoValue(),msg);
				this.isCrossOverYQZZCfg(starWarsTimeCfg.getWarStartTimeThreeValue(), starWarsTimeCfg.getWarEndTimeThreeValue(), msg);
			}
		}
					
		for (TiberiumTimeCfg tiberiumTimeCfg : tiberiumTimeList) {
			String msg = "泰伯配置 tiberium_war_time.xml id:" + tiberiumTimeCfg.getTermId();
			this.isCrossOverCyborgCfg(tiberiumTimeCfg.getWarStartTimeValue(), tiberiumTimeCfg.getWarEndTimeValue(), "", msg, false);
		}
		
		for (MergeServerTimeCfg mergeTimeCfg : mergeTimeList) {
			for (String serverId : mergeTimeCfg.getMergeServerList()) {
				String msg = "合服配置 merge_server_time.xml id:" + mergeTimeCfg.getId();
				this.isCrossOverTiberiumCfg(mergeTimeCfg.getMergeTimeValue(), mergeTimeCfg.getMergeTimeValue(), serverId, 
						msg, true);
				/*this.isCrossOverCyborgCfg(mergeTimeCfg.getMergeTimeValue(), mergeTimeCfg.getMergeTimeValue(), serverId, 
						msg, true);*/
				//this.isCrossOverYQZZCfg(mergeTimeCfg.getMergeTimeValue(), mergeTimeCfg.getMergeTimeValue(), msg);
			}
		}
	}
	
	
	
	private boolean isCrossOverYQZZCfg(long startTime, long endTime, String msg) {
		List<YQZZTimeCfg> groupBuyTimeList = this.getFutureYQZZList();
		for (YQZZTimeCfg timeCfg : groupBuyTimeList) {
			long yqzzStartTime = timeCfg.getShowTimeValue();
			long yqzzEndTime = timeCfg.getHiddenTimeValue();
			if (this.isTimeCross(startTime, endTime, yqzzStartTime, yqzzEndTime)) {
				throw new InvalidParameterException(msg 
				+ " 和月球副本配置时间有交叉 moon_war_time.xml id:" 
				+ timeCfg.getTermId());
			}
		}

		return false;
	}
	/**
	 * 检查万人团购和跨服是否有交叉
	 * 
	 * @param startTime
	 * @param endTime
	 * @param msg
	 * @return
	 */
	private boolean isCrossOverGroupBuyActivityCfg(long startTime, long endTime, String msg) {
		List<GroupBuyActivityTimerCfg> groupBuyTimeList = this.getGroupBuyList();
		for (GroupBuyActivityTimerCfg groupBuyTimeCfg : groupBuyTimeList) {
			long groupBuyStartTime = groupBuyTimeCfg.getEndTimeValue();
			long groupBuyEndTime = groupBuyTimeCfg.getHiddenTimeValue();
			if (this.isTimeCross(startTime, endTime, groupBuyStartTime, groupBuyEndTime)) {
				throw new InvalidParameterException(msg 
				+ " 和万人团购配置时间有交叉 activity/group_buy/group_buying_time.xml id:" 
				+ groupBuyTimeCfg.getTermId());
			}
		}

		return false;
	}

	private boolean isCrossOverTiberiumCfg(long startTime, long endTime, String serverId, String msg, boolean isSign) {
		List<TiberiumTimeCfg> tiberiumTimeList = this.getFutureTiberiumnList();
		for (TiberiumTimeCfg tiberiumTimeCfg : tiberiumTimeList) {
			if (!HawkOSOperator.isEmptyString(serverId)) {
				boolean isMatchServer = false;
				if (tiberiumTimeCfg.getLimitServerList().isEmpty()
						|| tiberiumTimeCfg.getLimitServerList().contains(serverId)) {
					isMatchServer = true;
				}
				if (tiberiumTimeCfg.getForbidServerList().contains(serverId)) {
					isMatchServer = false;
				}

				if (!isMatchServer) {
					continue;
				}
			}			
			
			long tiberiumStartTime = tiberiumTimeCfg.getSignStartTimeValue();
			if (!isSign) {
				tiberiumStartTime = tiberiumTimeCfg.getWarStartTimeValue();
			}
			if (this.isTimeCross(startTime, endTime, tiberiumStartTime,
					tiberiumTimeCfg.getWarEndTimeValue())) {
				throw new InvalidParameterException(
						msg + " 和泰伯利亚配置时间有交叉 tiberium_time.xml id:" + tiberiumTimeCfg.getTermId());
			}
		}

		return false;
	}
	
	private boolean isCrossOverCyborgCfg(long startTime, long endTime, String serverId, String msg, boolean isSign) {
		List<CyborgWarTimeCfg> cyborgTimeList = this.getFutureCyborgList();
		for (CyborgWarTimeCfg cyborgCfg : cyborgTimeList) {
			
			if (!HawkOSOperator.isEmptyString(serverId)) {
				boolean isMatchServer = false;
				if (cyborgCfg.getLimitServerList().isEmpty()
						|| cyborgCfg.getLimitServerList().contains(serverId)) {
					isMatchServer = true;
				}
				if (cyborgCfg.getForbidServerList().contains(serverId)) {
					isMatchServer = false;
				}

				if (!isMatchServer) {
					continue;
				}
			}
			
			long cyborgStartTime = 0l;			
			if (isSign) {
				cyborgStartTime = cyborgCfg.getOpenTimeValue();
			} else {
				cyborgStartTime = cyborgCfg.getWarStartTimeValue();
			}
			if (this.isTimeCross(startTime, endTime, cyborgStartTime,
					cyborgCfg.getWarEndTimeValue())) {
				throw new InvalidParameterException(
						msg + " 和赛博配置时间有交叉 cyborg_war_time.xml id:" + cyborgCfg.getTermId());
			}
		}

		return false;
	}

	/**
	 * 是否和合服配置
	 * 
	 * @param startTime
	 * @param endTime
	 * @param serverId
	 * @param msg
	 * @return
	 */
	private boolean isCrossOverMergeServerCfg(long startTime, long endTime, String serverId, String msg) {
		List<MergeServerTimeCfg> mergeServerTimeList = this.getFutureMergeServerTimeList();
		for (MergeServerTimeCfg mergeServerTimeCfg : mergeServerTimeList) {
			if (mergeServerTimeCfg.getMergeServerList().contains(serverId)) {
				if (isTimeCross(startTime, endTime, mergeServerTimeCfg.getMergeTimeValue(),
						mergeServerTimeCfg.getMergeTimeValue())) {
					throw new InvalidParameterException(
							msg + "和合服时间配置有冲突 merge_server_time.xml id " + mergeServerTimeCfg.getId());
				}
			}
		}

		return false;
	}
	
	/**
	 * 判定合服时间是否在指定时间段内
	 * @param startTime
	 * @param endTime
	 * @param serverId
	 * @return
	 */
	public boolean isCrossOverMergeServerCfg(long startTime, long endTime, String serverId) {
		ConfigIterator<MergeServerTimeCfg> mergeTimeIterator = HawkConfigManager.getInstance().getConfigIterator(MergeServerTimeCfg.class);
		for (MergeServerTimeCfg timeCfg : mergeTimeIterator) {
			if (timeCfg.getMergeServerList().contains(serverId)) {
				if (timeCfg.getMergeTimeValue() >= startTime && timeCfg.getMergeTimeValue() <= endTime) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @param startTime
	 * @param endTime
	 * @param serverId
	 * @param msg
	 * @return
	 */
	private boolean isCrossOverStarWarsCfg(long startTime, long endTime, String serverId, String msg) {
		List<StarWarsTimeCfg> starWarsTimeList = this.getFutureStarWarsList();
		Set<String> allServer = new HashSet<>();
		HawkConfigManager.getInstance().getConfigIterator(StarWarsPartCfg.class).stream()
				.forEach(x -> allServer.addAll(x.getServerList()));
		for (StarWarsTimeCfg cfg : starWarsTimeList) {
			if (!HawkOSOperator.isEmptyString(serverId) && !allServer.contains(serverId)) {
				continue;
			}

			if (this.isTimeCross(startTime, endTime, cfg.getWarStartTimeOneValue(), cfg.getWarEndTimeOneValue())) {
				throw new InvalidParameterException(
						msg + "和星球大战配置有交叉 星球大战配置 star_wars_time.xml termId:" + cfg.getTermId());
			}
			
			if (this.isTimeCross(startTime, endTime, cfg.getWarStartTimeTwoValue(), cfg.getWarEndTimeTwoValue())) {
				throw new InvalidParameterException(
						msg + "和星球大战配置有交叉 星球大战配置 star_wars_time.xml termId:" + cfg.getTermId());
			}
		}

		return false;
	}

	/**
	 * 判断两段时间是否有交叉 //A,A1 B,B1 两个时间点, 不交叉的情况是 A1 < B 或者B1 < A
	 * 
	 * @return
	 */
	public boolean isTimeCross(long startTime1, long endTime1, long startTime2, long endTime2) {
		return !(endTime1 < startTime2 || endTime2 < startTime1);
	}

	/**
	 * 获得将来或者正在开启的跨服配置
	 * 
	 * @return
	 */
	private List<CrossTimeCfg> getFutureCrossTimeList() {
		ConfigIterator<CrossTimeCfg> crossTimeIterator = HawkConfigManager.getInstance()
				.getConfigIterator(CrossTimeCfg.class);
		long curTime = HawkTime.getMillisecond();
		List<CrossTimeCfg> crossTimeList = crossTimeIterator.stream().filter(crossTime -> {
			return crossTime.getHiddenTimeValue() > curTime;
		}).collect(Collectors.toList());

		return crossTimeList;
	}

	/**
	 * 获得将来或者正在开启的合服时间配置
	 * 
	 * @return
	 */
	private List<MergeServerTimeCfg> getFutureMergeServerTimeList() {
		ConfigIterator<MergeServerTimeCfg> mergeTimeIterator = HawkConfigManager.getInstance()
				.getConfigIterator(MergeServerTimeCfg.class);
		long curTime = HawkTime.getMillisecond();
		List<MergeServerTimeCfg> mergeTimeList = mergeTimeIterator.stream().filter(mergeTime -> {
			return mergeTime.getMergeTimeValue() > curTime;
		}).collect(Collectors.toList());

		return mergeTimeList;
	}

	/**
	 * 获得将来或者正在开启的
	 * 
	 * @return
	 */
	private List<TiberiumTimeCfg> getFutureTiberiumnList() {
		long curTime = HawkTime.getMillisecond();
		ConfigIterator<TiberiumTimeCfg> tiberiumTimeIterator = HawkConfigManager.getInstance()
				.getConfigIterator(TiberiumTimeCfg.class);
		List<TiberiumTimeCfg> tiberiumTimeList = tiberiumTimeIterator.stream().filter(tiberiumTime -> {
			return tiberiumTime.getWarEndTimeValue() > curTime;
		}).collect(Collectors.toList());

		return tiberiumTimeList;
	}
	
	/**
	 * 获取正在开启或将来要开启的万人团购活动时间配置
	 * @return
	 */
	private List<GroupBuyActivityTimerCfg> getGroupBuyList() {
		long curTime = HawkTime.getMillisecond();
		ConfigIterator<GroupBuyActivityTimerCfg> groupBuyTimeIterator = HawkConfigManager.getInstance()
				.getConfigIterator(GroupBuyActivityTimerCfg.class);
		List<GroupBuyActivityTimerCfg> groupBuyTimeList = groupBuyTimeIterator.stream().filter(
				groupBuyTime -> { 
					return groupBuyTime.getHiddenTimeValue() > curTime;
		}).collect(Collectors.toList());

		return groupBuyTimeList;
	}

	/**
	 * 将来或者正在开的星球大战.
	 * 
	 * @return
	 */
	private List<StarWarsTimeCfg> getFutureStarWarsList() {
		long curTime = HawkTime.getMillisecond();
		ConfigIterator<StarWarsTimeCfg> starWarsIteratro = HawkConfigManager.getInstance()
				.getConfigIterator(StarWarsTimeCfg.class);
		List<StarWarsTimeCfg> starwarsTimeList = starWarsIteratro.stream().filter(starWars -> {
			return starWars.getWarEndTimeTwoValue() > curTime;
		}).collect(Collectors.toList());

		return starwarsTimeList;
	}
	
	private List<CyborgWarTimeCfg> getFutureCyborgList() {
		long curTime = HawkTime.getMillisecond();
		ConfigIterator<CyborgWarTimeCfg> cyborgIterator = HawkConfigManager.getInstance()
				.getConfigIterator(CyborgWarTimeCfg.class);
		List<CyborgWarTimeCfg> starwarsTimeList = cyborgIterator.stream().filter(cyborg -> {
			return cyborg.getWarEndTimeValue() > curTime;
		}).collect(Collectors.toList());

		return starwarsTimeList;
	}
	
	private List<YQZZTimeCfg> getFutureYQZZList() {
		long curTime = HawkTime.getMillisecond();
		ConfigIterator<YQZZTimeCfg> cyborgIterator = HawkConfigManager.getInstance()
				.getConfigIterator(YQZZTimeCfg.class);
		List<YQZZTimeCfg> starwarsTimeList = cyborgIterator.stream().filter(cyborg -> {
			return cyborg.getHiddenTimeValue() > curTime;
		}).collect(Collectors.toList());

		return starwarsTimeList;
	}

	private void assembleCheckMergeServer() {
		Map<String, MergeServerGroupCfg> map = futureMergeServerCfgMap;		
		for (String serverId : map.keySet()) {
			Long mergeServerTime = this.mergeServerTimeMap.get(serverId);
			if (mergeServerTime == null) {
				throw new InvalidParameterException(
						String.format("serverId:%s 配置在将要合服列表里面,在merge_server_time里面却找不到相应的合服时间", serverId));
			}			
		}
		
		Set<MergeServerGroupCfg> futureSetCfg = new HashSet<>();
		futureSetCfg.addAll(map.values());
		
		//TODO 这里随着合服需求的变化（要支持多合一），MergeServerGroupCfg 不添加字段的话没法校验，策划【余焘】说先去掉这个校验，在配表时自己去保证正确性 -- 20241009
		
//		long now = HawkTime.getMillisecond(); 
//		for (MergeServerGroupCfg groupCfg : futureSetCfg) {
//			if (groupCfg.getFutureMergeServerIdList().size() <= 2) {
//				continue;
//			}
//			
//			String mainServerId = groupCfg.getFutureMergeServerIdList().get(0);
//			long futureTime = this.mergeServerTimeMap.get(mainServerId);
//			if (futureTime <= now) {
//				continue;
//			}
//			for (String serverId : groupCfg.getFutureMergeServerIdList()) {
//				if (!mergeServerCfgMap.containsKey(serverId)) {
//					throw new InvalidParameterException(String.format("merge_server_group.xml 中futureMergeServerIds在mergeServerIds"
//							+ "找不到合服记录  id:%s", groupCfg.getId()));
//				}
//			}
//		}
	}

	private void assembleMergeServerTime() {
		Map<String, Long> mergeTime = new HashMap<>();
		ConfigIterator<MergeServerTimeCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(MergeServerTimeCfg.class);
		MergeServerTimeCfg timeCfg = null;
		while (configIterator.hasNext()) {
			timeCfg = configIterator.next();
			List<String> serverIdList = timeCfg.getMergeServerList();
			for (String serverId : serverIdList) {
				Long mergeServerTime = mergeTime.get(serverId);
				if (mergeServerTime != null) {
					throw new InvalidParameterException(
							"mergeServerTimeCfg merge_server_time.xml 一个serverId不能存在多条记录.serverId:" + serverId);
				}
				mergeTime.put(serverId, timeCfg.getMergeTimeValue());
			}
		}

		this.mergeServerTimeMap = mergeTime;
	}

	/**
	 * 合服相关信息
	 */
	private void assembleMergeServer() {
		ConfigIterator<MergeServerGroupCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(MergeServerGroupCfg.class);
		Map<String, MergeServerGroupCfg> groupCfgMap = new HashMap<>();
		Map<String, MergeServerGroupCfg> futureCfgMap = new HashMap<>();
		while (cfgIterator.hasNext()) {
			MergeServerGroupCfg cfg = cfgIterator.next();
			for (String mergeServerId : cfg.getMergeServerIdList()) {
				MergeServerGroupCfg mapCfg = groupCfgMap.get(mergeServerId);
				if (mapCfg != null) {
					throw new InvalidParameterException("mergeServerGroupCfg mergeServerId duplicate:" + mergeServerId);
				}
				groupCfgMap.put(mergeServerId, cfg);
			}

			List<String> futureList = cfg.getFutureMergeServerIdList();
			for (String futureMergeServerId : futureList) {
				MergeServerGroupCfg futureCfg = futureCfgMap.get(futureMergeServerId);
				if (futureCfg != null) {
					throw new InvalidParameterException(
							"mergeServerGroupCfg 将来要合的区服ID重复了 重复的区服ID:" + futureMergeServerId);
				}

				futureCfgMap.put(futureMergeServerId, cfg);
			}
		}

		this.mergeServerCfgMap = groupCfgMap;
		this.futureMergeServerCfgMap = futureCfgMap;
	}

	private void assembleCrossSpecialProtocol() {
		HawkConfigManager configManager = HawkConfigManager.getInstance();
		ConfigIterator<CrossProtocolCfg> configIterator = configManager.getConfigIterator(CrossProtocolCfg.class);
		CrossProtocolCfg cfg = null;
		HashSet<Integer> localProtocol = new HashSet<>();
		HashSet<Integer> shieldProtocol = new HashSet<>();
		while (configIterator.hasNext()) {
			cfg = configIterator.next();
			localProtocol.addAll(cfg.getLocalProtocolSet());
			shieldProtocol.addAll(cfg.getShieldProtocolSet());
		}

		this.crossLocalProtocolSet = localProtocol;
		this.crossShieldProtocolSet = shieldProtocol;
	}

	private void assembleAllianceFile() {
		ConfigIterator<AllianceFileCfg> allianceFileIterator = HawkConfigManager.getInstance()
				.getConfigIterator(AllianceFileCfg.class);
		List<AllianceFileCfg> list = new ArrayList<>(allianceFileIterator.toList());
		this.allianceFileList = list;
	}

	private void assembleVipShop() {
		ConfigIterator<VipShopCfg> shopIterator = HawkConfigManager.getInstance().getConfigIterator(VipShopCfg.class);
		Map<Integer, List<VipShopCfg>> localVipLevelCfgMap = new TreeMap<>();
		VipShopCfg cfg = null;
		while (shopIterator.hasNext()) {
			cfg = shopIterator.next();
			List<VipShopCfg> cfgList = localVipLevelCfgMap.get(cfg.getVipLevel());
			if (cfgList == null) {
				cfgList = new ArrayList<>();
				localVipLevelCfgMap.put(cfg.getVipLevel(), cfgList);
			}

			cfgList.add(cfg);
		}

		ConstProperty constProperty = ConstProperty.getInstance();
		for (Entry<Integer, List<VipShopCfg>> entry : localVipLevelCfgMap.entrySet()) {
			int shopCount = constProperty.getVipShopItemCount(entry.getKey());
			int num = entry.getValue().stream().filter(cfg0 -> cfg0.getWeight() > 0).collect(Collectors.toList())
					.size();
			if (shopCount > num) {
				throw new InvalidParameterException("vip刷新池池比vip个数少");
			}
		}

		this.vipLevelCfgMap = localVipLevelCfgMap;

	}

	private void assemblResGift() {
		ConfigIterator<ResGiftLevelCfg> resGiftIterator = HawkConfigManager.getInstance()
				.getConfigIterator(ResGiftLevelCfg.class);
		Map<Integer, List<ResGiftLevelCfg>> resGiftListMap = new HashMap<>();
		List<ResGiftLevelCfg> resGiftList = null;
		for (ResGiftLevelCfg resCfg : resGiftIterator) {
			resGiftList = resGiftListMap.get(resCfg.getResType());
			if (resGiftList == null) {
				resGiftList = new ArrayList<>();
				resGiftListMap.put(resCfg.getResType(), resGiftList);
			}
			resGiftList.add(resCfg);
		}

		this.resGiftLevelListMap = resGiftListMap;
	}

	private void assemblGift() {
		ConfigIterator<GiftCfg> giftIterator = HawkConfigManager.getInstance().getConfigIterator(GiftCfg.class);
		Map<Integer, List<GiftCfg>> giftCfgMap = new HashMap<>();
		Map<Integer, GiftCfg> groupMaxGift = new HashMap<>();
		List<GiftCfg> giftCfgList = null;
		Map<String, List<Integer>> superGiftIdPayGiftIdMap = new HashMap<>();
		for (GiftCfg giftCfg : giftIterator) {
			giftCfgList = giftCfgMap.get(giftCfg.getGroupId());
			if (giftCfgList == null) {
				giftCfgList = new ArrayList<>();
				giftCfgMap.put(giftCfg.getGroupId(), giftCfgList);
			}
			giftCfgList.add(giftCfg);

			if (giftCfg.getIsMaxLevel() == 1) {
				groupMaxGift.put(giftCfg.getGroupId(), giftCfg);
			}

			if (!HawkOSOperator.isEmptyString(giftCfg.getAndroidPayId())) {
				PayGiftCfg payGiftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class,
						giftCfg.getAndroidPayId());
				if (payGiftCfg == null) {
					throw new InvalidParameterException(
							String.format("gift.xml 中的giftId:%s 这条记录配置的payId:%s 在payGift.xml中找不到", giftCfg.getId(),
									giftCfg.getAndroidPayId()));
				}
				List<Integer> superGiftIdList = superGiftIdPayGiftIdMap.get(giftCfg.getAndroidPayId());
				if (superGiftIdList == null) {
					superGiftIdList = new ArrayList<>();
					superGiftIdPayGiftIdMap.put(giftCfg.getAndroidPayId(), superGiftIdList);
				}
				superGiftIdList.add(giftCfg.getId());
			}

			if (!HawkOSOperator.isEmptyString(giftCfg.getIosPayId())) {
				PayGiftCfg payGiftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class,
						giftCfg.getIosPayId());
				if (payGiftCfg == null) {
					throw new InvalidParameterException(
							String.format("gift.xml 中的giftId:%s 这条记录配置的payId:%s 在payGift.xml中找不到", giftCfg.getId(),
									giftCfg.getIosPayId()));
				}
				List<Integer> superGiftIdList = superGiftIdPayGiftIdMap.get(giftCfg.getIosPayId());
				if (superGiftIdList == null) {
					superGiftIdList = new ArrayList<>();
					superGiftIdPayGiftIdMap.put(giftCfg.getIosPayId(), superGiftIdList);
				}
				superGiftIdList.add(giftCfg.getId());
			}

		}

		this.payGiftIdSuperGiftIdMap = superGiftIdPayGiftIdMap;
		for (Entry<Integer, List<GiftCfg>> entry : giftCfgMap.entrySet()) {
			Collections.sort(entry.getValue(), new Comparator<GiftCfg>() {

				@Override
				public int compare(GiftCfg o1, GiftCfg o2) {
					return o1.getLevel() - o2.getLevel();
				}

			});
		}

		ConfigIterator<GiftSysPoolCfg> poolIterator = HawkConfigManager.getInstance()
				.getConfigIterator(GiftSysPoolCfg.class);
		Map<Integer, Integer> groupIdPoolIdMap = new HashMap<>();
		while (poolIterator.hasNext()) {
			GiftSysPoolCfg poolCfg = poolIterator.next();
			poolCfg.getValueMap().entrySet().stream().forEach(entry -> {
				groupIdPoolIdMap.put(entry.getKey(), poolCfg.getId());
			});
		}

		ConfigIterator<GiftGroupCfg> groupIterator = HawkConfigManager.getInstance()
				.getConfigIterator(GiftGroupCfg.class);
		List<String> strList = new ArrayList<>();
		groupIterator.stream().forEach(group -> {
			List<GiftCfg> cfgList = giftCfgMap.get(group.getId());
			if (cfgList == null) {
				strList.add("" + group.getId());
			}
		});
		if (!strList.isEmpty()) {
			throw new InvalidParameterException(strList.toString() + " 这些groupId在gift.xml找不到对应的数据");
		}
		this.giftListMap = giftCfgMap;
		this.groupMaxGiftCfgMap = groupMaxGift;
		this.groupIdPoolIdMap = groupIdPoolIdMap;
	}

	private void assemblePlotBattle() {
		ConfigIterator<PlotLevelCfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(PlotLevelCfg.class);
		Map<Integer, List<PlotLevelCfg>> plotMap = new HashMap<>();
		PlotLevelCfg plotLevelCfg = null;
		List<PlotLevelCfg> plotLevelList = null;
		while (configIterator.hasNext()) {
			plotLevelCfg = configIterator.next();
			plotLevelList = plotMap.get(plotLevelCfg.getChapterId());
			if (plotLevelList == null) {
				plotLevelList = new ArrayList<>();
				plotMap.put(plotLevelCfg.getChapterId(), plotLevelList);
			}

			plotLevelList.add(plotLevelCfg);
		}

		this.plotLevelCfgListMap = plotMap;
	}

	private void assembleNewMonster() {
		ConfigIterator<WorldEnemyCfg> worldEnemyCfgIter = HawkConfigManager.getInstance()
				.getConfigIterator(WorldEnemyCfg.class);
		Table<Integer, Integer, WorldEnemyCfg> newMonsterIds185 = HashBasedTable.create();
		Table<Integer, Integer, WorldEnemyCfg> newMonsterIds = HashBasedTable.create();
		Table<Integer, Integer, Integer> oldMonsterIds = HashBasedTable.create();
		Table<Integer, Integer, WorldEnemyCfg> oldMonsterCfgs = HashBasedTable.create();
		while (worldEnemyCfgIter.hasNext()) {
			WorldEnemyCfg worldEnemy = worldEnemyCfgIter.next();
			if (worldEnemy.getType() == MonsterType.TYPE_2_VALUE) {
				if(worldEnemy.getRelateActivity() == 184){
					newMonsterIds.put(worldEnemy.getModelType(), worldEnemy.getLevel(), worldEnemy);
				}
				if(worldEnemy.getRelateActivity() == 185){
					newMonsterIds185.put(worldEnemy.getModelType(), worldEnemy.getLevel(), worldEnemy);
				}
			}
			if (worldEnemy.getType() == MonsterType.TYPE_1_VALUE) {
				oldMonsterIds.put(worldEnemy.getModelType(), worldEnemy.getLevel(), worldEnemy.getId());
				oldMonsterCfgs.put(worldEnemy.getModelType(), worldEnemy.getLevel(), worldEnemy);
			}
		}
		this.newMonsterIdsAct185 = newMonsterIds185;
		this.newMonsterIds = newMonsterIds;
		this.oldMonsterIds = oldMonsterIds;
		this.oldMonsterCfgs = oldMonsterCfgs;
	}
	
	/**
	 * 组装 装备科技 配置
	 */
	private void assembleEquipResearchCfg() {
		Table<Integer, Integer, EquipResearchLevelCfg> equipResearchTable = HashBasedTable.create();
		ConfigIterator<EquipResearchLevelCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(EquipResearchLevelCfg.class);
		while (cfgIterator.hasNext()) {
			EquipResearchLevelCfg cfg = cfgIterator.next();
			equipResearchTable.put(cfg.getResearchId(), cfg.getLevel(), cfg);
		}
		this.equipResearchTable = equipResearchTable;
	}
	
	
	private void assembleEquipResearchRewardCfg() {
		Table<Integer, Integer, EquipResearchRewardCfg> equipResearchRewardTable = HashBasedTable.create();
		ConfigIterator<EquipResearchRewardCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(EquipResearchRewardCfg.class);
		while (cfgIterator.hasNext()) {
			EquipResearchRewardCfg cfg = cfgIterator.next();
			if (!cfg.getEffTouchList().isEmpty()) {
				equipResearchRewardTable.put(cfg.getUnlockResearchId(), cfg.getUnlockResearchLevel(), cfg);
			}
		}
		this.equipResearchRewardTable = equipResearchRewardTable;
	}

	private void assembleTravelShop() {
		TravelShopCfg travelShopCfg = null;
		List<TravelShopCfg> travelShopCfgList = null;
		Map<Integer, List<TravelShopCfg>> travelShopCfgMap = new HashMap<>();
		ConfigIterator<TravelShopCfg> travelSohpCfgsItr = HawkConfigManager.getInstance().getConfigIterator(TravelShopCfg.class);
		while (travelSohpCfgsItr.hasNext()) {
			travelShopCfg = travelSohpCfgsItr.next();
			travelShopCfgList = travelShopCfgMap.get(travelShopCfg.getShopPool());
			if (travelShopCfgList == null) {
				travelShopCfgList = new ArrayList<>();
				travelShopCfgMap.put(travelShopCfg.getShopPool(), travelShopCfgList);
			}
			travelShopCfgList.add(travelShopCfg);
		}
		travelShopCfgListMap = travelShopCfgMap;
	}

	private void assembleTravelShopAssist() {
		TravelShopCfg travelShopCfg = null;
		List<TravelShopCfg> travelShopCfgList = null;
		Map<Integer, List<TravelShopCfg>> travelShopCfgMap = new HashMap<>();
		ConfigIterator<TravelShopAssistCfg> travelSohpCfgsItr = HawkConfigManager.getInstance().getConfigIterator(TravelShopAssistCfg.class);
		while (travelSohpCfgsItr.hasNext()) {
			travelShopCfg = travelSohpCfgsItr.next();
			travelShopCfgList = travelShopCfgMap.get(travelShopCfg.getShopPool());
			if (travelShopCfgList == null) {
				travelShopCfgList = new ArrayList<>();
				travelShopCfgMap.put(travelShopCfg.getShopPool(), travelShopCfgList);
			}
			travelShopCfgList.add(travelShopCfg);
		}

		travelShopAssistCfgListMap = travelShopCfgMap;
	}
	
	
	private void assembleDragonBoatBenefitShop() {
		TravelShopCfg travelShopCfg = null;
		List<TravelShopCfg> travelShopCfgList = null;
		Map<Integer, List<TravelShopCfg>> travelShopCfgMap = new HashMap<>();
		ConfigIterator<DragonBoatBenefitShopCfg> travelSohpCfgsItr = HawkConfigManager.getInstance().getConfigIterator(DragonBoatBenefitShopCfg.class);
		while (travelSohpCfgsItr.hasNext()) {
			travelShopCfg = travelSohpCfgsItr.next();
			travelShopCfgList = travelShopCfgMap.get(travelShopCfg.getShopPool());
			if (travelShopCfgList == null) {
				travelShopCfgList = new ArrayList<>();
				travelShopCfgMap.put(travelShopCfg.getShopPool(), travelShopCfgList);
			}
			travelShopCfgList.add(travelShopCfg);
		}
		dragonBoatBenefitShopCfgListMap = travelShopCfgMap;
	}

	private void assembleTravelShopGift() {
		ConfigIterator<TravelShopGiftCfg> travelShopGiftCfg = HawkConfigManager.getInstance().getConfigIterator(TravelShopGiftCfg.class);
		List<Integer> travelGiftWeigthList = new ArrayList<>();
		List<TravelShopGiftCfg> travelGiftCfgList = new ArrayList<>();
		List<Integer> vipTravelGiftWeightList = new ArrayList<>();
		List<TravelShopGiftCfg> vipTravelGiftCfgList = new ArrayList<>();
		List<TravelShopGiftCfg> guideGiftList = new ArrayList<>();
		while (travelShopGiftCfg.hasNext()) {
			TravelShopGiftCfg giftCfg = travelShopGiftCfg.next();
			if (giftCfg.getType() == GsConst.TravelShopConstant.POOL_TYPE_NORMAL) {
				travelGiftWeigthList.add(giftCfg.getWeight());
				travelGiftCfgList.add(giftCfg);
			} else {
				vipTravelGiftCfgList.add(giftCfg);
				vipTravelGiftWeightList.add(giftCfg.getWeight());
			}

			if (giftCfg.isGuide()) {
				guideGiftList.add(giftCfg);
			}
		}

		travelShopGiftCfgList = travelGiftCfgList;
		travelShopGiftCfgWeigthList = travelGiftWeigthList;
		vipTravelShopGfitCfgList = vipTravelGiftCfgList;
		vipTravelShopGiftCfgWeigthList = vipTravelGiftWeightList;
		this.guideTravelGiftList = guideGiftList;
	}

	private void assemblePushGift() {
		ConfigIterator<PushGiftLevelCfg> pushGiftLevelCfgIterator = HawkConfigManager.getInstance()
				.getConfigIterator(PushGiftLevelCfg.class);
		Map<Integer, List<PushGiftLevelCfg>> pushGfitLeveListMap = new HashMap<>();
		PushGiftLevelCfg pushGiftLevelCfg = null;
		List<PushGiftLevelCfg> pushGiftLevelCfgList = null;
		while (pushGiftLevelCfgIterator.hasNext()) {
			pushGiftLevelCfg = pushGiftLevelCfgIterator.next();
			pushGiftLevelCfgList = pushGfitLeveListMap.get(pushGiftLevelCfg.getGroupId());
			if (pushGiftLevelCfgList == null) {
				pushGiftLevelCfgList = new ArrayList<>();
				pushGfitLeveListMap.put(pushGiftLevelCfg.getGroupId(), pushGiftLevelCfgList);
			}

			pushGiftLevelCfgList.add(pushGiftLevelCfg);
		}

		for (List<PushGiftLevelCfg> cfgList : pushGfitLeveListMap.values()) {
			Collections.sort(cfgList);
		}

		this.pushGiftGroupPushGiftLevelListMap = pushGfitLeveListMap;

		ConfigIterator<PushGiftGroupCfg> pushGiftGroupCfgIterator = HawkConfigManager.getInstance()
				.getConfigIterator(PushGiftGroupCfg.class);
		Map<Integer, List<PushGiftGroupCfg>> pushGiftGroupListMap = new HashMap<>();
		PushGiftGroupCfg pushGiftGroupCfg = null;
		List<PushGiftGroupCfg> pushGiftGroupCfgList = null;
		while (pushGiftGroupCfgIterator.hasNext()) {
			pushGiftGroupCfg = pushGiftGroupCfgIterator.next();
			pushGiftGroupCfgList = pushGiftGroupListMap.get(pushGiftGroupCfg.getConditionType());
			if (pushGiftGroupCfgList == null) {
				pushGiftGroupCfgList = new ArrayList<>();
				pushGiftGroupListMap.put(pushGiftGroupCfg.getConditionType(), pushGiftGroupCfgList);
			}

			pushGiftGroupCfgList.add(pushGiftGroupCfg);
		}

		this.pushGiftConditionTypeGroupMap = pushGiftGroupListMap;

	}

	private void assembleSuperWeapon() {
		Table<Integer, SuperWeaponAwardType, List<SuperWeaponAwardCfg>> superWeaponAward = HashBasedTable.create();
		ConfigIterator<SuperWeaponAwardCfg> superWeaponAwardIterator = HawkConfigManager.getInstance()
				.getConfigIterator(SuperWeaponAwardCfg.class);
		while (superWeaponAwardIterator.hasNext()) {
			SuperWeaponAwardCfg cfg = superWeaponAwardIterator.next();
			int pointId = GameUtil.combineXAndY(cfg.getX(), cfg.getY());
			List<SuperWeaponAwardCfg> awardList = superWeaponAward.get(pointId,
					SuperWeaponAwardType.valueOf(cfg.getType()));
			if (awardList == null) {
				awardList = new ArrayList<>();
			}
			awardList.add(cfg);
			superWeaponAward.put(pointId, SuperWeaponAwardType.valueOf(cfg.getType()), awardList);

		}
		this.superWeaponAward = superWeaponAward;

		Map<Integer, SuperWeaponCfg> superWeaponCfgs = new HashMap<>();
		List<Integer> superWeaponPoints = new ArrayList<>();
		ConfigIterator<SuperWeaponCfg> superWeaponIterator = HawkConfigManager.getInstance()
				.getConfigIterator(SuperWeaponCfg.class);
		while (superWeaponIterator.hasNext()) {
			SuperWeaponCfg cfg = superWeaponIterator.next();
			int pointId = GameUtil.combineXAndY(cfg.getX(), cfg.getY());
			superWeaponCfgs.put(pointId, cfg);
			superWeaponPoints.add(GameUtil.combineXAndY(cfg.getX(), cfg.getY()));
		}
		this.superWeaponCfgs = superWeaponCfgs;
		this.superWeaponPoints = superWeaponPoints;
	}
	
	private void assembleNationBuilding(){
		for (NationConstructionBaseCfg cfg : HawkConfigManager.getInstance().getConfigIterator(NationConstructionBaseCfg.class)) {
			nationalBuildingPoint.put(GameUtil.combineXAndY(cfg.getX(), cfg.getY()), cfg.getBuildType());
		}
		
		if(nationalBuildingPoint.size() != 8){
			throw new InvalidParameterException("国家建筑坐标错误");
		}
	}

	private void assembleNationTechCfg() {
		Table<Integer, Integer, NationTechCfg> nationTechTable = HashBasedTable.create();
		ConfigIterator<NationTechCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(NationTechCfg.class);
		while (cfgIter.hasNext()) {
			NationTechCfg cfg = cfgIter.next();
			nationTechTable.put(cfg.getTechId(), cfg.getTechLevel(), cfg);
		}
		this.nationTechTable = nationTechTable;
	}
	
	private void assemblHellFire() {
		ConfigIterator<ActivityHellFireTargetCfg> targetCfgIterator = HawkConfigManager.getInstance()
				.getConfigIterator(ActivityHellFireTargetCfg.class);
		Table<Integer, Integer, List<ActivityHellFireTargetCfg>> hellFireTargetTable = HashBasedTable.create();
		ActivityHellFireTargetCfg targetCfg = null;
		while (targetCfgIterator.hasNext()) {
			targetCfg = targetCfgIterator.next();
			List<ActivityHellFireTargetCfg> targetCfgList = hellFireTargetTable.get(targetCfg.getTargetId(),
					targetCfg.getDifficultLevel());
			if (targetCfgList == null) {
				targetCfgList = new ArrayList<>();
				hellFireTargetTable.put(targetCfg.getTargetId(), targetCfg.getDifficultLevel(), targetCfgList);
			}

			targetCfgList.add(targetCfg);
		}

		Map<Integer, List<ActivityHellFireRankCfg>> rankMap = new HashMap<>();
		ConfigIterator<ActivityHellFireRankCfg> rankCfgIterator = HawkConfigManager.getInstance()
				.getConfigIterator(ActivityHellFireRankCfg.class);
		ActivityHellFireRankCfg rankCfg = null;
		while (rankCfgIterator.hasNext()) {
			rankCfg = rankCfgIterator.next();
			List<ActivityHellFireRankCfg> rankList = rankMap.get(rankCfg.getRankId());
			if (rankList == null) {
				rankList = new ArrayList<>();
				rankMap.put(rankCfg.getRankId(), rankList);
			}

			rankList.add(rankCfg);
		}

		this.hellFireRankMap = rankMap;
		this.hellFireTargetTable = hellFireTargetTable;

		ConfigIterator<ActivityHellFireTwoTargetCfg> targetTwoCfgIterator = HawkConfigManager.getInstance()
				.getConfigIterator(ActivityHellFireTwoTargetCfg.class);
		Table<Integer, Integer, List<ActivityHellFireTwoTargetCfg>> hellFireTwoTargetTable = HashBasedTable.create();
		ActivityHellFireTwoTargetCfg targetTwoCfg = null;
		while (targetTwoCfgIterator.hasNext()) {
			targetTwoCfg = targetTwoCfgIterator.next();
			List<ActivityHellFireTwoTargetCfg> targetCfgTwoList = hellFireTwoTargetTable.get(targetTwoCfg.getTargetId(),
					targetTwoCfg.getDifficultLevel());
			if (targetCfgTwoList == null) {
				targetCfgTwoList = new ArrayList<>();
				hellFireTwoTargetTable.put(targetTwoCfg.getTargetId(), targetTwoCfg.getDifficultLevel(),
						targetCfgTwoList);
			}

			targetCfgTwoList.add(targetTwoCfg);
		}

		Map<Integer, List<ActivityHellFireTwoRankCfg>> rankTwoMap = new HashMap<>();
		ConfigIterator<ActivityHellFireTwoRankCfg> rankTwoCfgIterator = HawkConfigManager.getInstance()
				.getConfigIterator(ActivityHellFireTwoRankCfg.class);
		ActivityHellFireTwoRankCfg rankTwoCfg = null;
		while (rankTwoCfgIterator.hasNext()) {
			rankTwoCfg = rankTwoCfgIterator.next();
			List<ActivityHellFireTwoRankCfg> rankList = rankTwoMap.get(rankTwoCfg.getRankId());
			if (rankList == null) {
				rankList = new ArrayList<>();
				rankTwoMap.put(rankTwoCfg.getRankId(), rankList);
			}

			rankList.add(rankTwoCfg);
		}

		this.hellFireTwoRankMap = rankTwoMap;
		this.hellFireTwoTargetTable = hellFireTwoTargetTable;

		ConfigIterator<ActivityHellFireThreeTargetCfg> targetThreeCfgIterator = HawkConfigManager.getInstance()
				.getConfigIterator(ActivityHellFireThreeTargetCfg.class);
		Table<Integer, Integer, List<ActivityHellFireThreeTargetCfg>> hellFireThreeTargetTable = HashBasedTable
				.create();
		ActivityHellFireThreeTargetCfg targetThreeCfg = null;
		while (targetThreeCfgIterator.hasNext()) {
			targetThreeCfg = targetThreeCfgIterator.next();
			List<ActivityHellFireThreeTargetCfg> targetCfgThreeList = hellFireThreeTargetTable
					.get(targetThreeCfg.getTargetId(), targetThreeCfg.getDifficultLevel());
			if (targetCfgThreeList == null) {
				targetCfgThreeList = new ArrayList<>();
				hellFireThreeTargetTable.put(targetThreeCfg.getTargetId(), targetThreeCfg.getDifficultLevel(),
						targetCfgThreeList);
			}

			targetCfgThreeList.add(targetThreeCfg);
		}

		Map<Integer, List<ActivityHellFireThreeRankCfg>> rankThreeMap = new HashMap<>();
		ConfigIterator<ActivityHellFireThreeRankCfg> rankThreeCfgIterator = HawkConfigManager.getInstance()
				.getConfigIterator(ActivityHellFireThreeRankCfg.class);
		ActivityHellFireThreeRankCfg rankThreeCfg = null;
		while (rankThreeCfgIterator.hasNext()) {
			rankThreeCfg = rankThreeCfgIterator.next();
			List<ActivityHellFireThreeRankCfg> rankThreeList = rankThreeMap.get(rankThreeCfg.getRankId());
			if (rankThreeList == null) {
				rankThreeList = new ArrayList<>();
				rankThreeMap.put(rankThreeCfg.getRankId(), rankThreeList);
			}

			rankThreeList.add(rankThreeCfg);
		}

		this.hellFireThreeRankMap = rankThreeMap;
		this.hellFireThreeTargetTable = hellFireThreeTargetTable;
	}

	public Map<Integer, List<PlotLevelCfg>> getPlotLevelCfgListMap() {
		return plotLevelCfgListMap;
	}

	public Map<Integer, List<PushGiftLevelCfg>> getPushGiftGroupPushGiftLevelListMap() {
		return pushGiftGroupPushGiftLevelListMap;
	}

	public Map<Integer, List<PushGiftGroupCfg>> getPushGiftConditionTypeGroupMap() {
		return pushGiftConditionTypeGroupMap;
	}

	public List<PushGiftLevelCfg> getPushGiftLevelCfgList(int groupId) {
		return pushGiftGroupPushGiftLevelListMap.get(groupId);
	}

	public List<Integer> getPushGiftLevelCfgIdList(int groupId) {
		List<PushGiftLevelCfg> levelCfgList = this.getPushGiftLevelCfgList(groupId);
		List<Integer> pushGiftLevelIdList = new ArrayList<>(levelCfgList.size());

		for (PushGiftLevelCfg levelCfg : levelCfgList) {
			pushGiftLevelIdList.add(levelCfg.getId());
		}

		return pushGiftLevelIdList;
	}

	public List<PushGiftGroupCfg> getPushGiftGroupCfgList(int conditionType) {
		return pushGiftConditionTypeGroupMap.get(conditionType);
	}

	public List<Integer> getTravelShopGiftCfgWeigthList() {
		return travelShopGiftCfgWeigthList;
	}

	public List<TravelShopGiftCfg> getTravelShopGiftCfgList() {
		return travelShopGiftCfgList;
	}

	public WorldEnemyCfg getNewMonsterCfg(int modelType, int level) {
		return newMonsterIds.get(modelType, level);
	}
	
	public WorldEnemyCfg getNewMonsterCfgAct185(int modelType, int level) {
		return newMonsterIdsAct185.get(modelType, level);
	}
	

	public Table<Integer, Integer, List<ActivityHellFireTargetCfg>> getHellFireTargetTable() {
		return hellFireTargetTable;
	}

	public Map<Integer, List<ActivityHellFireRankCfg>> getHellFireRankMap() {
		return hellFireRankMap;
	}

	public Table<Integer, Integer, List<ActivityHellFireTwoTargetCfg>> getHellFireTwoTargetTable() {
		return hellFireTwoTargetTable;
	}

	public Map<Integer, List<ActivityHellFireTwoRankCfg>> getHellFireTwoRankMap() {
		return hellFireTwoRankMap;
	}

	public Table<Integer, Integer, List<ActivityHellFireThreeTargetCfg>> getHellFireThreeTargetTable() {
		return hellFireThreeTargetTable;
	}

	public Map<Integer, List<ActivityHellFireThreeRankCfg>> getHellFireThreeRankMap() {
		return hellFireThreeRankMap;
	}

	public Map<Integer, List<GiftCfg>> getGiftListMap() {
		return giftListMap;
	}

	public GiftCfg getNextGiftCfg(int groupId) {
		return getNextGiftCfg(groupId, -1);
	}

	public GiftCfg getLastGiftCfg(int groupId, int level) {
		List<GiftCfg> giftList = this.getGiftCfgList(groupId);
		if (giftList == null || giftList.isEmpty()) {
			return null;
		}

		for (int i = giftList.size() - 1; i >= 0; i--) {
			if (giftList.get(i).getLevel() < level) {
				return giftList.get(i);
			}
		}

		return null;
	}

	public GiftCfg getNextGiftCfg(int groupId, int level) {
		List<GiftCfg> giftList = this.getGiftCfgList(groupId);
		if (giftList == null || giftList.isEmpty()) {
			return null;
		}

		for (GiftCfg giftCfg : giftList) {
			if (giftCfg.getLevel() > level) {
				return giftCfg;
			}
		}

		return null;
	}

	public List<GiftCfg> getGiftCfgList(int groupId) {
		return giftListMap.get(groupId);
	}

	public int getMaxMilitaryRankExp() {
		return maxMilitaryRankExp;
	}

	public int getOldMonsterId(int type, int level) {
		return oldMonsterIds.get(type, level);
	}

	public WorldEnemyCfg getOldMonsterCfg(int type, int level) {
		return oldMonsterCfgs.get(type, level);
	}

	public Map<Integer, GiftCfg> getGroupMaxGiftCfgMap() {
		return groupMaxGiftCfgMap;
	}

	public List<Integer> getVipTravelShopGiftCfgWeigthList() {
		return vipTravelShopGiftCfgWeigthList;
	}

	public List<TravelShopGiftCfg> getVipTravelShopGfitCfgList() {
		return vipTravelShopGfitCfgList;
	}

	public DressCfg getDressCfg(int dressType, int modelType) {
		return dressCfgs.get(dressType, modelType);
	}

	public Map<Integer, Integer> getGroupIdPoolIdMap() {
		return groupIdPoolIdMap;
	}

	public Integer getGiftPoolIdByGroupId(Integer groupId) {
		return groupIdPoolIdMap.get(groupId);
	}

	public List<TravelShopGiftCfg> getGuideTravelGiftList() {
		return guideTravelGiftList;
	}

	public int getAccumulateOnlineCfgSize(int dayCount) {
		Map<Integer, AccumulateOnlineCfg> cfgs = getAccumulateOnlineCfgs(dayCount);
		if (cfgs == null || cfgs.isEmpty()) {
			return 0;
		}
		return cfgs.size();
	}

	public Map<Integer, AccumulateOnlineCfg> getAccumulateOnlineCfgs(int dayCount) {
		return auucumulateOnlineCfg.row(dayCount);
	}

	public AccumulateOnlineCfg getAccumulateOnlineCfg(int dayCount, int posId) {
		return auucumulateOnlineCfg.get(dayCount, posId);
	}

	public int getMaxAccmulateOnlineDayCount() {
		return auucumulateOnlineCfg.rowKeySet().size();
	}

	public List<SuperWeaponAwardCfg> getSuperWeaponAwards(int pointId, SuperWeaponAwardType type) {
		List<SuperWeaponAwardCfg> cfgs = superWeaponAward.get(pointId, type);
		if (cfgs == null) {
			return new ArrayList<>();
		}
		return cfgs;
	}

	public SuperWeaponCfg getSuperWeaponCfg(int pointId) {
		return superWeaponCfgs.get(pointId);
	}

	public Map<Integer, List<ResGiftLevelCfg>> getResGiftLevelListMap() {
		return resGiftLevelListMap;
	}

	public ResGiftLevelCfg getNextgResGiftLevelCfg(Integer resType, int level) {
		List<ResGiftLevelCfg> levelCfgList = resGiftLevelListMap.get(resType);
		if (levelCfgList == null) {
			return null;
		}

		for (ResGiftLevelCfg resGift : levelCfgList) {
			if (resGift.getLevel() == level + 1) {
				return resGift;
			}
		}

		return null;
	}

	public List<Integer> getSuperWeaponPoints() {
		return superWeaponPoints;
	}

	/***
	 * 校验一个HawkConfigStorage
	 * 
	 * @return
	 */
	public boolean checkConfigData() {
		boolean strongestGuild = checkStrongestGuildConfig();
		boolean checkImage = checkPlayerImageConfig();
		boolean checkDYZZTime = this.checkDYZZTime();
		return strongestGuild && checkImage && checkDYZZTime;
	}
	
	private boolean checkDYZZTime(){
		List<DYZZSeasonTimeCfg> seasonit = HawkConfigManager.getInstance().getConfigIterator(DYZZSeasonTimeCfg.class).toList();
		List<DYZZTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(DYZZTimeCfg.class).toList();
		for (DYZZTimeCfg timeCfg : it) {
			long showTime = timeCfg.getShowTimeValue();
			long hiddenTime = timeCfg.getHiddenTimeValue();
			long curTime = HawkTime.getMillisecond();
			if(curTime < hiddenTime){
				boolean inseason = false;
				for (DYZZSeasonTimeCfg seasontimeCfg : seasonit) {
					if(seasontimeCfg.getShowTimeValue() < showTime
							&&seasontimeCfg.getHiddenTimeValue() > hiddenTime){
						inseason = true;
						break;
					}
				}
				if(!inseason){
					throw new RuntimeException(" DYZZTimeCfg check valid failed,not int season, termId:"+timeCfg.getTermId());
				}
			}
		}
		return true;
	}

	private boolean checkPlayerImageConfig() {
		boolean imageResult = false;
		ConfigIterator<PlayerImageCfg> imageIte = HawkConfigManager.getInstance()
				.getConfigIterator(PlayerImageCfg.class);
		while (imageIte.hasNext()) {
			PlayerImageCfg config = imageIte.next();
			if (config.getDefine() == PlayerImageCfg.define_Image && config.isSpecialType()) {
				imageResult = true;
			}
		}
		if (!imageResult) {
			throw new RuntimeException("config PlayerImageCfg has't define image");
		}
		boolean circleResult = false;
		ConfigIterator<PlayerFrameCfg> circleIte = HawkConfigManager.getInstance()
				.getConfigIterator(PlayerFrameCfg.class);
		while (circleIte.hasNext()) {
			PlayerFrameCfg config = circleIte.next();
			if (config.getDefine() == PlayerFrameCfg.define_frame) {
				circleResult = true;
			}
		}
		if (!circleResult) {
			throw new RuntimeException("config PlayerFrameCfg has't define frame");
		}

		CustomKeyCfg config = HawkConfigManager.getInstance().getConfigByKey(CustomKeyCfg.class, PlayerImageData.id);
		if (config == null) {
			throw new RuntimeException("config CustomKeyCfg has't des :" + PlayerImageData.id);
		}

		return true;
	}

	public List<VipShopCfg> getCfgListByVipLevel(int vipLevel) {
		List<VipShopCfg> cfgList = vipLevelCfgMap.get(vipLevel);
		if (cfgList == null) {
			cfgList = new ArrayList<>();
		}

		return Collections.unmodifiableList(cfgList);
	}

	public Map<Integer, List<VipShopCfg>> getVipShopCfgListMap() {
		return Collections.unmodifiableMap(vipLevelCfgMap);
	}

	/**
	 * 玩家成就类型
	 * 
	 * @return
	 */
	public Set<Integer> getPlayerAchieveGroups() {
		return playerAchieve.rowKeySet();
	}

	/**
	 * 玩家成就
	 * 
	 * @return
	 */
	public PlayerAchieveCfg getPlayerAchieve(int groupId) {
		return playerAchieve.get(groupId, 1);
	}

	/**
	 * 玩家成就
	 * 
	 * @return
	 */
	public PlayerAchieveCfg getPlayerAchieve(int groupId, int level) {
		return playerAchieve.get(groupId, level);
	}

	/**
	 * 玩家成就最大等级
	 * 
	 * @return
	 */
	public int getPlayerAchieveMaxLvl(int groupId) {
		return playerAchieve.row(groupId).size();
	}

	public List<AllianceFileCfg> getAllianceFileList() {
		return allianceFileList;
	}

	/****
	 * 校验王者联盟的配置数据
	 * 
	 * @return
	 */
	private boolean checkStrongestGuildConfig() {
		StrongestGuildCfg cfg = null;
		ConfigIterator<StrongestGuildCfg> ite = HawkConfigManager.getInstance()
				.getConfigIterator(StrongestGuildCfg.class);
		while (ite.hasNext()) {
			StrongestGuildCfg config = ite.next();
			if (config.getBeforeStageId() == 0) {
				cfg = config;
			}
		}
		if (cfg == null) {
			logger.error("王者联盟activity_strongest_allian.xml配置表错误，没有配置第一阶段数值,第一阶段数值nextStageId必须为0");
			return false;
		}
		while (cfg.getNextStageId() != 0) {
			StrongestGuildCfg nextCfg = HawkConfigManager.getInstance().getConfigByKey(StrongestGuildCfg.class,
					cfg.getNextStageId());
			if (nextCfg.getBeforeStageId() != cfg.getStageId()) {
				logger.error("王者联盟activity_strongest_allian.xml配置表错误，阶段:{}的下一个阶段的上一个阶段不等于本阶段", cfg.getStageId());
				return false;
			}
			cfg = nextCfg;
		}
		return true;
	}

	/**
	 * 根据区服id,获得所在组ID
	 * 
	 * @author jm 2019 下午4:22:26
	 * @param serverId
	 * @return
	 */
	public Integer getCrossServerCfgId(String serverId) {
		CrossServerListCfg crossServerList = this.getCrossServerListCfg(serverId);
		if (crossServerList == null) {
			return null; 
		} else {
			return crossServerList.getId();
		}
	}

	/**
	 * 获取本地处理协议
	 * 
	 * @author jm 2019 下午4:22:52
	 * @return
	 */
	public Set<Integer> getCrossLocalProtocolSet() {
		return crossLocalProtocolSet;
	}

	/**
	 * 跨服屏蔽协议
	 * 
	 * @author jm 2019 下午4:23:02
	 * @return
	 */
	public Set<Integer> getCrossShieldProtocolSet() {
		return crossShieldProtocolSet;
	}

	/**
	 * 判断跨服本地处理协议
	 * 
	 * @author jm 2019 下午4:23:16
	 * @param protocolNo
	 * @return
	 */
	public boolean isCrossLocalProtocol(Integer protocolNo) {
		return crossLocalProtocolSet.contains(protocolNo);
	}

	/**
	 * 跨服屏蔽协议.
	 * 
	 * @author jm 2019 下午4:23:37
	 * @param protocolNo
	 * @return
	 */
	public boolean isCrossShieldProtocl(Integer protocolNo) {
		return crossShieldProtocolSet.contains(protocolNo);
	}

	/**
	 * 当前服是否已经合服了.
	 * 
	 * @return
	 */
	public boolean isMergedServer() {
		return isMergedServer(GsConfig.getInstance().getServerId());
	}

	/**
	 * 判断下区服ID是否合服了.
	 * 
	 * @param serverId
	 * @return
	 */
	public boolean isMergedServer(String serverId) {
		// 要么是主区,要么是从区.
		return !HawkOSOperator.isEmptyString(this.getMainServerId(serverId));
	}

	/**
	 * @param serverId
	 * @return
	 */
	public Long getServerMergeTime(String serverId) {
		return mergeServerTimeMap.get(serverId);
	}

	/**
	 * 如果传进来的是主服Id那么返回的就是null
	 * 
	 * @param serverId
	 * @return
	 */
	public String getMainServerId(String serverId) {
		// 调用这个接口虽然效率差那么一丢丢，但是好在接口统一
		List<String> mergedServerList = this.getMergedServerList(serverId);
		if (!CollectionUtils.isEmpty(mergedServerList)) {
			return mergedServerList.get(0);
		} else {
			return null;
		}
	}

	/**
	 * 获取合服的信息, 主服在第一个,如果没有合服，则返回null.记得判空.
	 * 
	 * @param serverId
	 * @return
	 */
	public List<String> getMergedServerList(String serverId) {
		MergeServerGroupCfg serverCfg = this.futureMergeServerCfgMap.get(serverId);
		boolean isMerged = false;
		boolean isFuture = false;
		long curTime = HawkTime.getMillisecond();
		if (serverCfg != null) {
			long mergeTime = this.getServerMergeTime(serverId);
			if (curTime >= mergeTime) {
				isMerged = true;
				isFuture = true;
			}
		}

		if (!isMerged) {
			serverCfg = this.mergeServerCfgMap.get(serverId);
			if (serverCfg != null && !serverCfg.getMergeServerIdList().isEmpty()) {
				isMerged = true;
			}
		}

		if (isMerged) {
			List<String> mergedServerList = new ArrayList<>();
			mergedServerList.addAll(serverCfg.getMergeServerIdList());
			if (isFuture) {
				mergedServerList.addAll(serverCfg.getFutureMergeServerIdList());
			}

			return mergedServerList;
		} else {
			return null;
		}
	}

	/**
	 * 获取合服组的服务器数量
	 * @param serverId
	 * @return
	 */
	public int getMergedGroupServerNum(String serverId) {
		List<String> mergedServerList = getMergedServerList(serverId);
		if (mergedServerList == null) {
			return 0;
		}
		return mergedServerList.size();
	}
	
	/**
	 * 获取铠甲属性配置
	 */
	public List<ArmourAdditionalCfg> getArmourAdditionCfgs(Integer type, Integer quality) {
		if (type == null || quality == null) {
			return new ArrayList<>();
		}

		List<ArmourAdditionalCfg> cfgs = armourAdditionTable.get(type, quality);
		if (cfgs == null) {
			return new ArrayList<>();
		}
		return cfgs;
	}

	/**
	 * 获取铠甲属性配置
	 */
	public List<ArmourAdditionalCfg> getArmourAdditionUnderCfgs(Integer type, Integer quality) {
		List<ArmourAdditionalCfg> retList = new ArrayList<>();
		if (type == null || quality == null) {
			return retList;
		}

		for (int i = quality - 1; i > 0; i--) {
			retList.addAll(getArmourAdditionCfgs(type, i));
		}

		return retList;
	}

	public List<Integer> getSuperGiftIdListByPayGiftId(String payGiftId) {
		return payGiftIdSuperGiftIdMap.get(payGiftId);
	}

	/**
	 * 做判空处理
	 * 
	 * @param guardValue
	 * @return
	 */
	public GuardianAttributeCfg getGuardianAttribute(int guardValue) {
		ConfigIterator<GuardianAttributeCfg> attributeCfgIterator = HawkConfigManager.getInstance()
				.getConfigIterator(GuardianAttributeCfg.class);
		GuardianAttributeCfg guardianAttributeCfg = null;
		GuardianAttributeCfg tmpAttributeCfg = null;
		while (attributeCfgIterator.hasNext()) {
			tmpAttributeCfg = attributeCfgIterator.next();
			guardianAttributeCfg = guardValue >= tmpAttributeCfg.getNeedValue() ? tmpAttributeCfg
					: guardianAttributeCfg;
		}

		return guardianAttributeCfg;
	}


	/**
	 * 获取泰伯利亚联赛指定赛季的开启及关闭时间
	 * 
	 * @param season
	 * @return
	 */
	public HawkTuple2<Long, Long> getTiberiumSeasonTime(int season) {
		return tlwTimeInfo.get(season);
	}

	/**
	 * 星球大战的分区信息.
	 * 
	 * @return
	 */
	public Set<Integer> getStarWarsPartSet() {
		return this.starWarsPartSet;
	}

	/**
	 * 获取区服ID 没有就给你一个-1因为0代表的是世界.
	 * 
	 * @param serverId
	 * @return
	 */
	public Integer getServerPart(String serverId) {
		StarWarsPartCfg cfg = this.getServerPartCfg(serverId);
		if (cfg == null) {
			return -1;
		} else {
			return cfg.getZone();
		}
	}
	
	public Integer getTeamId(String serverId) {
		StarWarsPartCfg cfg = this.getServerPartCfg(serverId);
		if (cfg == null) {
			return -1;
		} else {
			return cfg.getTeam();
		}
	} 
	
	public StarWarsPartCfg getServerPartCfg(String serverId) {
		return serverPartCfgMap.get(serverId);
	}

	/**
	 * 获取星球大战的官职.
	 * 
	 * @return
	 */
	public  Map<Integer, StarWarsOfficerCfg> getStarOfficerMap() {
		return starOfficerMap;
	}

	/**
	 * 获取星球大战的官职.
	 * 
	 * @param isWorld
	 * @param officerId
	 * @return
	 */
	public StarWarsOfficerCfg getStarWarsOfficerCfg(int type) {
		return starOfficerMap.get(type);
	}

	/**
	 * 获取星球大战区服.
	 * 
	 * @return
	 */
	public Table<Integer, Integer, StarWarsPartCfg> getStarWarsPartTable() {
		return starWarsPartTable;
	}

	/**
	 * 分区ID
	 * 
	 * @param areaId
	 * @param zoneId
	 * @return
	 */
	public StarWarsPartCfg getStarWarsPartCfg(int zoneId, int teamId) {
		return starWarsPartTable.get(zoneId, teamId);
	}
	
	
	/**
	 * 获取指挥官学院分数配置
	 * @param rankId
	 * @return
	 */
	public List<CommandAcademyRankScoreCfg> getAcademyRankScoreCfg(int rankId){
		return this.academyRankScoreCfgMap.get(rankId);
	}
	public List<CommandAcademySimplifyRankScoreCfg> getAcademyRankSimplifyScoreCfg(int rankId){
		return this.academyRankSimplifyScoreCfgMap.get(rankId);
	}	

	public Set<Integer> getGuardDressIdSet() {
		return guardDressIdSet;
	}
	
	public boolean isGuardDressId(int dressId) {
		return guardDressIdSet.contains(dressId);
	}

	public Map<Integer, List<Integer>> getGuardDressListMap() {
		return guardDressListMap;
	}
	
	public List<Integer> getGuardDressList(int dressId) {
		return this.guardDressListMap.get(dressId);
	}
	
	/**
	 * 是否是单人守护特效.
	 * @param dressId
	 * @return
	 */
	public boolean isSingleGuardDressId(int dressId) {
		List<Integer> idList = this.getGuardDressList(dressId);
		if (CollectionUtils.isEmpty(idList)) {
			return false;
		}
		
		return idList.indexOf(dressId) == 0;
	}

	public Map<String, List<Integer>> getPayGiftIdPushIdListMap() {
		return payGiftIdPushIdListMap;
	}
	
	/**
	 * 
	 * @param payId
	 * @return
	 */
	public List<Integer> getPushIdListByPayId(String payId) {
		return payGiftIdPushIdListMap.get(payId);
	}
	
	public void parseCrossServerList(Map<Integer, String> serverMap) {
		//
		List<CrossServerListCfg> serverList = new ArrayList<>();
		for (Entry<Integer, String> entry : serverMap.entrySet()) {
			CrossServerListCfg serverCfg = new CrossServerListCfg(entry.getKey(), entry.getValue());
			serverCfg.assemble();
			serverList.add(serverCfg);
		}
		
		Map<String, CrossServerListCfg> serverCfgMap = new HashMap<>(serverList.size());
		for (CrossServerListCfg listCfg : serverList) {
			for (String serverId : listCfg.getServerList()) {
				serverCfgMap.put(serverId, listCfg);
			}
		}
		
		this.serverCrossListMap = serverCfgMap;
	}
		
	public CrossServerListCfg getCrossServerListCfg(String serverId) {
		return serverCrossListMap.get(serverId);
	}
	
	public Map<String, CrossServerListCfg> getCrossServerListCfgMap() {
		return serverCrossListMap;
	}
	
	/**
	 * 获取装备科技等级配置
	 */
	public EquipResearchLevelCfg getEquipResearchLevelCfg(int researchId, int level) {
		return equipResearchTable.get(researchId, level);
	}
	
	/**
	 * 获取装备科技id Set
	 */
	public Set<Integer> getEquipResearchIds() {
		return equipResearchTable.rowKeySet();
	}

	/**
	 * 获取装备科技奖励宝箱配置
	 */
	public EquipResearchRewardCfg getEquipResearchRewardCfg(int researchId, int level) {
		return equipResearchRewardTable.get(researchId, level);
	}

	public int getArmourPerfectAttr(int quality, int effect) {
		Integer value = armourPerfectAttr.get(quality, effect);
		return value == null ? 0 : value;
	}

	public int getArmourSuperPerfectAttr(int quality, int effect) {
		Integer value = armourSuperPerfectAttr.get(quality, effect);
		return value == null ? 0 : value;
	}
	

	
	public List<AgencyWeightCfg> getAgencyWeightCfgs(int group){
		return this.agencyWeightMap.get(group);
	}
	
	
	
	public ArmourStarCfg getArmourStarCfg(int armourId, int star) {
		return armourStarTable.get(armourId, star);
	}

	public ArmourQuantumCfg getArmourQuantumCfg(int armourId, int level) {
		return armourQuantumTable.get(armourId, level);
	}

	
	public List<XZQAwardCfg> getXZQAwards(int level, XZQAwardType type) {
		List<XZQAwardCfg> cfgs = xzqAward.get(level, type);
		if (cfgs == null) {
			return new ArrayList<>();
		}
		return cfgs;
	}
	
	public List<XZQAwardCfg> getXZQFirstAwards(int level, XZQAwardType type) {
		List<XZQAwardCfg> cfgs = xzqFirstAward.get(level, type);
		if (cfgs == null) {
			return new ArrayList<>();
		}
		return cfgs;
	}
	
	public Set<Integer> getXZQBuilds(int level) {
		return xzqBuidMap.get(level);
	}
	
	public int getXZQSpecialAwardMaxRound(int pointId){
		return xzqSpecialAwardMaxRound.getOrDefault(pointId, 1);
	}

	public Map<Integer, Integer> getNationalBuildingPoint() {
		return nationalBuildingPoint;
	}
	
	public NationTechCfg getNationTech(int techCfgId, int techLevel) {
		return nationTechTable.get(techCfgId, techLevel);
	}
	
	public List<TeamStrengthWeightCfg> getTeamStrengthWeightCfgList(int type){
		return this.teamStrengthWeightMap.get(type);
	}
}
