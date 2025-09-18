package com.hawk.game.player;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.hawk.game.entity.*;
import com.hawk.game.module.homeland.entity.PlayerHomeLandEntity;
import org.hawk.annotation.SerializeField;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.entity.PlayerData4Activity;
import com.hawk.activity.event.impl.EquipChangeEvent;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.cfgElement.ArmourStarExplores;
import com.hawk.game.config.ArmourCfg;
import com.hawk.game.config.ArmourConstCfg;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuffCfg;
import com.hawk.game.config.BuildLimitCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.CrossFortressCfg;
import com.hawk.game.config.CrossTechCfg;
import com.hawk.game.config.CustomKeyCfg;
import com.hawk.game.config.DressCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.ManhattanSWSkillCfg;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.config.PlayerAchieveCfg;
import com.hawk.game.config.PlotChapterCfg;
import com.hawk.game.config.PlotLevelCfg;
import com.hawk.game.config.PrivateSettingOptionCfg;
import com.hawk.game.config.SkillCfg;
import com.hawk.game.config.TalentLevelCfg;
import com.hawk.game.config.TavernScoreCfg;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.config.YuristrikeCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossfortress.CrossFortressService;
import com.hawk.game.crossfortress.IFortress;
import com.hawk.game.data.BubbleRewardInfo;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.PlayerAchieveItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.manager.IconManager;
import com.hawk.game.module.college.entity.CollegeMemberEntity;
import com.hawk.game.module.hospice.HospiceObj;
import com.hawk.game.module.lianmengyqzz.march.entitiy.PlayerYQZZEntity;
import com.hawk.game.module.mechacore.entity.MechaCoreEntity;
import com.hawk.game.module.mechacore.entity.MechaCoreModuleEntity;
import com.hawk.game.module.nationMilitary.entity.NationMilitaryEntity;
import com.hawk.game.module.plantsoldier.advance.PlantSoldierAdvanceEntity;
import com.hawk.game.module.plantsoldier.science.PlantScienceEntity;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchoolEntity;
import com.hawk.game.module.staffofficer.StaffOfficerSkillCollection;
import com.hawk.game.module.toucai.entity.MedalEntity;
import com.hawk.game.player.cache.PlayerDataCache;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.player.equip.CommanderObject;
import com.hawk.game.player.manhattan.PlayerManhattan;
import com.hawk.game.president.PresidentOfficier;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.GachaType;
import com.hawk.game.protocol.Const.LimitType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.StateType;
import com.hawk.game.protocol.Const.ToolType;
import com.hawk.game.protocol.Dress.PlayerDressPlayerInfo;
import com.hawk.game.protocol.Manhattan.PBDeployedSwInfo;
import com.hawk.game.protocol.Obelisk;
import com.hawk.game.protocol.Player.PlayerFlagPosition;
import com.hawk.game.protocol.Player.PlayerSnapshotPB;
import com.hawk.game.protocol.PlotBattle;
import com.hawk.game.protocol.President.OfficerType;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPeriod;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.recharge.RechargeType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.PlayerImageService;
import com.hawk.game.service.QueueService;
import com.hawk.game.util.BitIntUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.MissionState;
import com.hawk.game.util.GsConst.QueueReusage;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MapUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.yuriStrikes.YuriStrike;
import com.hawk.log.LogConst.CityWallChangeType;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.log.LogConst.Platform;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 管理所有玩家数据集合
 *
 * @author hawk
 *
 */
public class PlayerData {
	/**
	 * 玩家id
	 */
	protected String playerId;
	/**
	 * 对应数据加载缓存
	 */
	protected PlayerDataCache dataCache;
	
	/**
	 * 保存一些玩家不方便携带的作用号, 
	 */
	private Map<Integer, Integer> csEffect;
	/** 参谋技能 */
	private StaffOfficerSkillCollection staffOffic;
	/**
	 * 获取玩家数据实体
	 */
	@SerializeField
	public PlayerEntity getPlayerEntity() {
		return getDataCache().makesureDate(PlayerDataKey.PlayerEntity);
	}

	/**
	 * 获取玩家基础数据
	 * 
	 * @return
	 */
	@SerializeField
	public PlayerBaseEntity getPlayerBaseEntity() {
		return getDataCache().makesureDate(PlayerDataKey.PlayerBaseEntity);
	}

	/**
	 * 获取玩家统计数据实体
	 */
	@SerializeField
	public StatisticsEntity getStatisticsEntity() {
		return getDataCache().makesureDate(PlayerDataKey.StatisticsEntity);
	}

	/**
	 * 获取支付状态信息
	 */
	@SerializeField
	public PayStateEntity getPayStateEntity() {
		return getDataCache().makesureDate(PlayerDataKey.PayStateEntity);
	}

	/**
	 * 获取天赋列表
	 */
	@SerializeField
	public List<TalentEntity> getTalentEntities() {
		return getDataCache().makesureDate(PlayerDataKey.TalentEntities);
	}

	/**
	 * 获取物品列表
	 */
	@SerializeField
	public List<ItemEntity> getItemEntities() {
		return getDataCache().makesureDate(PlayerDataKey.ItemEntities);
	}
	
	@SerializeField
	public List<BuildingBaseEntity> getBuildingEntitiesIgnoreStatus() {
		return getDataCache().makesureDate(PlayerDataKey.BuildingEntities);
	}

	/**
	 * 获取装扮赠送请求记录
	 */
	@SerializeField
	public List<PlayerDressPlayerInfo> getDressAskEntities() {
		return getDataCache().makesureDate(PlayerDataKey.PlayerDressAskEntities);
	}
	
	/**
	 * 获取装扮赠送记录
	 */
	@SerializeField
	public List<PlayerDressPlayerInfo> getDressSendEntities() {
		return getDataCache().makesureDate(PlayerDataKey.PlayerDressSendEntities);
	}

	/**
	 * 获得所有科技实体
	 */
	@SerializeField
	public List<TechnologyEntity> getTechnologyEntities() {
		return getDataCache().makesureDate(PlayerDataKey.TechnologyEntities);
	}
	
	/**
	 * 获得所有科技实体
	 */
	@SerializeField
	public List<CrossTechEntity> getCrossTechEntities() {
		return getDataCache().makesureDate(PlayerDataKey.CrossTechEntities);
	}
	
	@SerializeField
	public List<PlayerGachaEntity> getPlayerGachaEntities() {
		return getDataCache().makesureDate(PlayerDataKey.PlayerGachaEntities);
	}

	/**
	 * 获取队列实体列表
	 */
	@SerializeField
	public List<QueueEntity> getQueueEntities() {
		return getDataCache().makesureDate(PlayerDataKey.QueueEntities);
	}

	/**
	 * 获得所有军队实体
	 */
	@SerializeField
	public List<ArmyEntity> getArmyEntities() {
		return getDataCache().makesureDate(PlayerDataKey.ArmyEntities);
	}

	/**
	 * 获取装备列表
	 * 
	 * @return
	 */
	@SerializeField
	public List<EquipEntity> getEquipEntities() {
		return getDataCache().makesureDate(PlayerDataKey.EquipEntities);
	}

	/**
	 * 获取指挥官实体
	 */
	@SerializeField
	public CommanderEntity getCommanderEntity() {
		return getDataCache().makesureDate(PlayerDataKey.CommanderEntity);
	}

	/**
	 * 获取玩家状态实体
	 */
	@SerializeField
	public List<StatusDataEntity> getStatusDataEntities() {
		return getDataCache().makesureDate(PlayerDataKey.StatusDataEntities);
	}

	/**
	 * 获取玩家任务实体
	 */
	@SerializeField
	public List<MissionEntity> getMissionEntities() {
		return getDataCache().makesureDate(PlayerDataKey.MissionEntities);
	}

	/**
	 * 获取剧情任务实体
	 */
	@SerializeField
	public StoryMissionEntity getStoryMissionEntity() {
		return getDataCache().makesureDate(PlayerDataKey.StoryMissionEntity);
	}

	/**
	 * 获取国家任务实体
	 */
	@SerializeField
	public NationMissionEntity getNationMissionEntity() {
		return getDataCache().makesureDate(PlayerDataKey.NationMissionEntity);
	}
	
	/**
	 * 获取玩家成就实体
	 */
	@SerializeField
	public PlayerAchieveEntity getPlayerAchieveEntity() {
		return getDataCache().makesureDate(PlayerDataKey.PlayerAchieveEntity);
	}
	
	/**
	 * 获取玩家历史充值记录:返回的结果中已包含所有的RechargeDailyEntity数据
	 */
	@SerializeField
	public List<RechargeEntity> getPlayerRechargeEntities() {
		return getDataCache().makesureDate(PlayerDataKey.PlayerRechargeEntities);
	}
	
	/**
	 * 获取玩家当日充值数据
	 * @return
	 */
	@SerializeField
	public List<RechargeDailyEntity> getPlayerRechargeDailyEntities() {
		return getDataCache().makesureDate(PlayerDataKey.PlayerRechargeDailyEntities);
	}
	
	/**
	 * 获取玩家历史充值备份数据
	 */
	@SerializeField
	public List<RechargeBackEntity> getPlayerRechargeBackEntities() {
		return getDataCache().makesureDate(PlayerDataKey.PlayerRechargeBackEntities);
	}

	/**
	 * 玩家调查问卷实体
	 */
	@SerializeField
	public QuestionnaireEntity getQuestionnaireEntity() {
		return getDataCache().makesureDate(PlayerDataKey.QuestionnaireEntity);
	}

	/**
	 * 玩家野怪实体
	 */
	@SerializeField
	public PlayerMonsterEntity getMonsterEntity() {
		return getDataCache().makesureDate(PlayerDataKey.MonsterEntity);
	}

	/**
	 * 许愿池信息
	 */
	@SerializeField
	public WishingWellEntity getWishingEntity() {
		return getDataCache().makesureDate(PlayerDataKey.WishingEntity);
	}

	/**
	 * 酒馆信息
	 */
	@SerializeField
	public TavernEntity getTavernEntity() {
		return getDataCache().makesureDate(PlayerDataKey.TavernEntity);
	}

	/**
	 * 码头信息
	 */
	@SerializeField
	public WharfEntity getWharfEntity() {
		return getDataCache().makesureDate(PlayerDataKey.WharfEntity);
	}

	/**
	 * 玩家英雄DB实体
	 */
	@SerializeField
	public List<HeroEntity> getHeroEntityList() {
		return getDataCache().makesureDate(PlayerDataKey.HeroEntityList);
	}

	/**
	 * 玩家商店实体
	 */
	@SerializeField
	public List<PlayerShopEntity> getShopEntityList() {
		return getDataCache().makesureDate(PlayerDataKey.PlayerShopEntityList);
	}

	@SerializeField
	public List<PlayerXQHXTalentEntity> getXQHXTalentEntityList() {
		return getDataCache().makesureDate(PlayerDataKey.PlayerXQHXTalentEntityList);
	}

	@SerializeField
	public PlayerXQHXEntity getPlayerXQHXEntity() {
		return dataCache.makesureDate(PlayerDataKey.PlayerXQHXEntity);
	}
	
	@SerializeField
	public PlantSoldierSchoolEntity getPlantSoldierSchoolEntity() {
		return getDataCache().makesureDate(PlayerDataKey.PlantSoldierSchoolEntity);
	}
	
	@SerializeField
	public PlantSoldierAdvanceEntity getPlantSoldierAdvanceEntity() {
		return getDataCache().makesureDate(PlayerDataKey.PlantSoldierAdvanceEntity);
	}
	
	@SerializeField
	public List<LaboratoryEntity> getLaboratoryEntityList() {
		return getDataCache().makesureDate(PlayerDataKey.LaboratoryEntityList);
	}
	
	/**
	 * 玩家铠甲DB实体
	 */
	@SerializeField
	public List<ArmourEntity> getArmourEntityList() {
		return getDataCache().makesureDate(PlayerDataKey.ArmourEntities);
	}
	
	/**
	 * 装备科技db实体
	 * @return
	 */
	@SerializeField
	public List<EquipResearchEntity> getEquipResearchEntityList() {
		return getDataCache().makesureDate(PlayerDataKey.EquipResearchEntities);
	}
	
	/**
	 * 玩家超级兵DB实体
	 */
	@SerializeField
	public List<SuperSoldierEntity> getSuperSoldierEntityList() {
		return getDataCache().makesureDate(PlayerDataKey.SuperSoldierEntityList);
	}
	
	/**
	 * 玩家超武entity
	 */
	@SerializeField
	public List<ManhattanEntity> getManhattanEntityList() {
		return getDataCache().makesureDate(PlayerDataKey.ManhattanEntityList);
	}
	
	/**
	 * 机甲核心数据
	 */
	@SerializeField
	public MechaCoreEntity getMechaCoreEntity() {
		if (getDataCache() == null) {
			HawkLog.errPrintln("playerData getMechaCoreEntity dataCache null");
		}
		return getDataCache().makesureDate(PlayerDataKey.MechaCoreEntity);
	}
	
	/**
	 * 机甲核心的模块数据
	 */
	@SerializeField
	public List<MechaCoreModuleEntity> getMechaCoreModuleEntityList() {
		return getDataCache().makesureDate(PlayerDataKey.MechaCoreModuleEntities);
	}

	/** 联盟礼物 */
	@SerializeField
	public List<PlayerGuildGiftEntity> getGuildGiftEntity() {
		return getDataCache().makesureDate(PlayerDataKey.GuildGiftEntity);
	}

	/**
	 * 获取用户自定义数据对象
	 */
	@SerializeField
	public List<CustomDataEntity> getCustomDataEntities() {
		return getDataCache().makesureDate(PlayerDataKey.CustomDataEntities);
	}

	/**
	 * 每日数据实体
	 */
	@SerializeField
	public DailyDataEntity getDailyDataEntity() {
		return getDataCache().makesureDate(PlayerDataKey.DailyDataEntity);
	}
	
	@SerializeField
	public PlotBattleEntity getPlotBattleEntity() {
		return getDataCache().makesureDate(PlayerDataKey.PlotBattleEntity);
	}
	
	/**
	 * 获取装扮信息
	 * 
	 * @return
	 */
	@SerializeField
	public DressEntity getDressEntity() {
		return getDataCache().makesureDate(PlayerDataKey.DressEntity);
	}

	/**
	 * 累积在线
	 */
	@SerializeField
	public AccumulateOnlineEntity getAccumulateOnlineEntity() {
		return getDataCache().makesureDate(PlayerDataKey.AccumulateOnlineEntity);
	}

	/**
	 * 超值礼包
	 */
	@SerializeField
	public PlayerGiftEntity getPlayerGiftEntity() {
		return getDataCache().makesureDate(PlayerDataKey.PlayerGiftEntity);
	}

	// 货币待补发记录
	@SerializeField
	public List<MoneyReissueEntity> getMoneyReissueEntityList() {
		return getDataCache().makesureDate(PlayerDataKey.MoneyReissueEntityList);
	}

	/**
	 * 资源礼包
	 */
	@SerializeField
	public PlayerResourceGiftEntity getPlayerResourceGiftEntity() {
		return getDataCache().makesureDate(PlayerDataKey.PlayerResourceGiftEntity);
	}
	
	/**宝藏基础信息*/
	@SerializeField
	public StorehouseBaseEntity getStorehouseBase() {
		return getDataCache().makesureDate(PlayerDataKey.StorehouseBase);
	}

	/**已挖宝*/
	@SerializeField
	public List<StorehouseEntity> getStorehouseEntities() {
		return getDataCache().makesureDate(PlayerDataKey.StorehouseEntities);
	}

	/**已帮助*/
	@SerializeField
	public List<StorehouseHelpEntity> getStorehouseHelpEntities() {
		return getDataCache().makesureDate(PlayerDataKey.StorehouseHelpEntities);
	}
	
	/**取得尤里来饭*/
	public YuriStrikeEntity getYuriStrikeEntity(){
		return getDataCache().makesureDate(PlayerDataKey.YuriStrike);
	}
	
	/**联盟关怀*/
	public GuildHospiceEntity getGuildHospiceEntity(){
		return getDataCache().makesureDate(PlayerDataKey.GuildHospice);
	}
	
	@SerializeField
	public PlayerWarCollegeEntity getPlayerWarCollegeEntity() {
		return getDataCache().makesureDate(PlayerDataKey.PlayerWarCollegeEntity);
	}
	
	/**
	 * 获取战争学院成员实例
	 */
	@SerializeField
	public CollegeMemberEntity getCollegeMemberEntity() {
		return getDataCache().makesureDate(PlayerDataKey.CollegeMemberEntity);
	}

	
	/**
	 * 获取送装扮的记录
	 */
	public List<PlayerDressPlayerInfo> getPlayerDressSendEntities(){
		return getDataCache().makesureDate(PlayerDataKey.PlayerDressSendEntities);
	}
		
	/**
	 * 获取请求送装扮的记录
	 */
	public List<PlayerDressPlayerInfo> getPlayerDressAskEntities(){
		return getDataCache().makesureDate(PlayerDataKey.PlayerDressAskEntities);
	}

	/**
	 * 玩家的散乱属性.
	 * @return
	 */
	@SerializeField
	public PlayerOtherEntity getPlayerOtherEntity() {
		return getDataCache().makesureDate(PlayerDataKey.PlayerOtherEntity);
	}

	/**
	 * 获取玩加幽灵工厂数据
	 * @return
	 */
	@SerializeField
	public PlayerGhostTowerEntity getPlayerGhostTowerEntity(){
		return dataCache.makesureDate(PlayerDataKey.PlayerGhostTowerEntity);
	}


	/**
	 * 获取方尖碑列表
	 */
	@SerializeField
	public List<ObeliskEntity> getObeliskEntities() {
		return getDataCache().makesureDate(PlayerDataKey.ObeliskEntities);
	}
	
	/**
	 * 获得所泰能生产线
	 */
	@SerializeField
	public List<PlantFactoryEntity> getPlantFactoryEntities() {
		return getDataCache().makesureDate(PlayerDataKey.PlantFactoryEntities);
	}
	
	@SerializeField
	public List<PlantTechEntity> getPlantTechEntities() {
		return getDataCache().makesureDate(PlayerDataKey.PlantTechEntities);
	}
	
	/**情报中心信息*/
	@SerializeField
	public AgencyEntity getAgencyEntity() {
		return dataCache.makesureDate(PlayerDataKey.AgencyEntity);
	}
	
	/**泰能科技树*/
	@SerializeField
	public PlantScienceEntity getPlantScienceEntity() {
		return dataCache.makesureDate(PlayerDataKey.PlantScienceEntity);
	}
	
	/**月球之战信息*/
	@SerializeField
	public PlayerYQZZEntity getPlayerYqzzEntity() {
		return dataCache.makesureDate(PlayerDataKey.PlayerYqzzData);
	}
	
	/** 国家建设任务 */
	@SerializeField
	public NationBuildQuestEntity getNationalBuildQuestEntity() {
		return dataCache.makesureDate(PlayerDataKey.NationalBuildQuestEntity);
	}
	/**国家军功*/
	@SerializeField
	public NationMilitaryEntity getNationMilitaryEntity() {
		return dataCache.makesureDate(PlayerDataKey.NationMilitaryEntity);
	}
	
	/**
	 * 英雄档案
	 * @return
	 */
	@SerializeField
	public HeroArchivesEntity getHeroArchivesEntity() {
		return getDataCache().makesureDate(PlayerDataKey.HeroArchivesEntity);
	}
	
	/**
	 * 终身卡
	 * @return
	 */
	@SerializeField
	public LifetimeCardEntity getLifetimeCardEntity() {
		return getDataCache().makesureDate(PlayerDataKey.LifetimeCardEntity);
	}
	
	@SerializeField
	public MedalEntity getMedalEntity() {
		return getDataCache().makesureDate(PlayerDataKey.MedalEntity);
	}
	
	
	/**每日必买宝箱*/
	@SerializeField
	public PlayerDailyGiftBuyEntity getPlayerDailyGiftBuyEntity() {
		return dataCache.makesureDate(PlayerDataKey.PlayerDailyGiftBuy);
	}

	@SerializeField
	public PlayerHomeLandEntity getHomeLandEntity() {
		return getDataCache().makesureDate(PlayerDataKey.PlayerHomeLand);
	}
	/////////////////////////////////////////////////////////////////////////////////////
	// 华丽丽的分割线, 别越界, 上面是DB实体, 需要序列化, 下面是非序列号内存对象
	/////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 获取设置列表
	 */
	public Map<Integer, Integer> getSettingDatas() {
		return getDataCache().makesureDate(PlayerDataKey.SettingDatas);
	}

	public Set<String> getShieldPlayers() {
		return getDataCache().makesureDate(PlayerDataKey.ShieldPlayers);
	}

	public Map<String, String> getBanRankInfos() {
		return getDataCache().makesureDate(PlayerDataKey.BanRankInfos);
	}

	/**
	 * 是否为缓存数据
	 */
	@JSONField(serialize = false)
	protected boolean isFromCache = false;
	/**
	 * 快照数据
	 */
	@JSONField(serialize = false)
	protected PlayerSnapshotPB snapshot = null;
	/**
	 * 玩家作用号
	 */
	@JSONField(serialize = false)
	protected PlayerEffect playerEffect = null;
	/**
	 * 玩家资源限制
	 */
	@JSONField(serialize = false)
	protected ResourceLimit resourceLimit = null;
	/**
	 * 玩家战力电力
	 */
	@JSONField(serialize = false)
	protected PowerElectric powerElectric = null;
	/**
	 * 快照下次更新时间
	 */
	@JSONField(serialize = false)
	protected long snapshotNextUpdateTime = 0;
	/**
	 * 玩家操作次数，用于记录操作流水安全日志上报
	 */
	@JSONField(serialize = false)
	private int operationCount = 0;
	/**
	 * 客户端服务器时间差 = clineSeconds - serverSeconds
	 */
	@JSONField(serialize = false)
	private int clientServerTimeSub = 0;
	/**
	 * 热卖商品redis存储信息
	 */
	@JSONField(serialize = false)
	private int hotSaleValue = -1;
	/**
	 * 玩家头像信息 此pfIcon已经为pfIcon的CRC.
	 * 切记此头像初始化必须为null
	 * 为null的时候做一次redis查询操作当查询之后不管是否查找到了数据都会赋值成一个""保证不会在没有头像的情况频繁访问Redis
	 */
	@JSONField(serialize = false)
	private String pfIcon = null;
	/**
	 * 最大城防值，在tick时检测到maxCityDef发生了变化，及时更新和通知前端
	 */
	@JSONField(serialize = false)
	private int maxCityDef = 0;

	/**
	 * 被国王禁言状态
	 */
	@JSONField(serialize = false)
	private JSONObject presidentSilentInfo = null;

	/**
	 * 玩家冒泡奖励相关数据
	 */
	@JSONField(serialize = false)
	protected Map<Integer, BubbleRewardInfo> bubbleRewardInfos = null;

	/**
	 * 已拆除且经验值还没扣完的建筑
	 */
	@JSONField(serialize = false)
	protected Map<String, Integer> buildRemoveExps = null;

	@JSONField(serialize = false)
	private transient PlayerData4Activity playerData4Activity;
	
	/**
	 * 物品兑换次数信息
	 */
	@JSONField(serialize = false)
	protected Map<Integer, Integer> itemExchangeInfo = null;

	/**
	 * 最后一次购买体力的时间
	 */
	@JSONField(serialize = false)
	private long lastBuyVitTime = 0;

	/**
	 * 当天已购买体力的次数
	 */
	@JSONField(serialize = false)
	private int vitBuyTimes = 0;
	
	/**
	 * 原始pfIcon串，用于上报安全日志数据
	 */
	@JSONField(serialize = false)
	private String primitivePfIcon = null;

	/**
	 * 上次邀请集结时间
	 */
	@JSONField(serialize = false)
	private long lastInviteMassTime;
	
	/**
	 * 构造
	 */
	public PlayerData() {
		isFromCache = false;
		resourceLimit = new ResourceLimit();
		bubbleRewardInfos = new ConcurrentHashMap<>();
		buildRemoveExps = new ConcurrentHashMap<String, Integer>();
		itemExchangeInfo = new HashMap<Integer, Integer>();
	}
	
	public PlayerData(String playerId) {
		this();
		this.playerId = playerId;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////
	// 数据加载区
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 初始化加载玩家全量数据
	 * 
	 */
	public boolean loadPlayerData(String playerId) {
		this.playerId = playerId;
		this.dataCache = PlayerDataCache.newCache(playerId);
		long loadStartTime = HawkTime.getMillisecond();
		try {
			// 首先判断玩家数据加载
			if (getPlayerEntity() == null) {
				HawkLog.errPrintln("load player entity data failed, playerId: {}", playerId);
				return false;
			}

			HawkLog.logPrintln("player load data success, playerId: {}, costtime: {}",
					playerId, HawkTime.getMillisecond() - loadStartTime);

			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return false;
	}
	
	/**
	 * 玩家登录加载全量数据. 必须先执行 loadPlayerData
	 */
	public void loadAll(boolean isNewly) {
		if (dataCache == null) {
			throw new RuntimeException("loadPlayerData didnot call");
		}
		
		long startTime = HawkTime.getMillisecond();
		
		// 新手玩家预创建数据
		if (isNewly && GsConfig.getInstance().isPreparePlayerData()) {
			preparePlayerEntities();
		}
		
		// 加载所有数据
		for (PlayerDataKey key : EnumSet.allOf(PlayerDataKey.class)) {
			dataCache.makesureDate(key);
		}
		
		// 加载玩家活动数据
		ActivityManager.getInstance().loadPlayerActivityData(playerId);
		
		// 日志记录
		HawkLog.logPrintln("player login load data success, playerId: {}, costtime: {}", 
				playerId, HawkTime.getMillisecond() - startTime);
	}
	
	/**
	 * 加载必要数据
	 */
	public void loadStart() {
		if (dataCache == null) {
			throw new RuntimeException("loadPlayerData didnot call");
		}
		
		long startTime = HawkTime.getMillisecond();
		
		// 加载所有数据
		for (PlayerDataKey key : PlayerDataKey.values()) {
			switch (key) {
			case ArmyEntities:
			case PlantScienceEntity:
			case LaboratoryEntityList:
			case BuildingEntities:
			case CommanderEntity:
			case PlayerEntity:
			case DressEntity:
			case StatisticsEntity:
			case PlantTechEntities:
			case TechnologyEntities:
			case DailyDataEntity:
			case EquipResearchEntities:
			case TalentEntities:
			case HeroEntityList:
			case PlayerAchieveEntity:
			case PlayerBaseEntity:
			case CrossTechEntities:
			case StorehouseEntities:
			case CustomDataEntities:
			case SuperSoldierEntityList:
			case HeroArchivesEntity:
			case StatusDataEntities:
			case TavernEntity:
			case PlantSoldierSchoolEntity:
			case ArmourEntities:
			case CollegeMemberEntity:
			case LifetimeCardEntity:
				dataCache.makesureDate(key);
				break;

			default:
				break;
			}
		}
		
		// 日志记录
		HawkLog.logPrintln("serverStart load data success, playerId: {}, costtime: {}", 
				playerId, HawkTime.getMillisecond() - startTime);
	}
	
	/**
	 * 预创建玩家各个数据表
	 */
	private boolean preparePlayerEntities() {
		long startTime = HawkTime.getMillisecond();
		
		try {
			List<HawkDBEntity> entityList = new LinkedList<HawkDBEntity>();
			
			// 玩家基础数据
			PlayerBaseEntity playerBaseEntity = dataCache.getData(PlayerDataKey.PlayerBaseEntity);
			if (playerBaseEntity == null) {
				playerBaseEntity = new PlayerBaseEntity();
				playerBaseEntity.setPlayerId(playerId);
				playerBaseEntity.assemble();
				entityList.add(playerBaseEntity);
			}
			
			// 分析数据
			StatisticsEntity statisticsEntity = dataCache.getData(PlayerDataKey.StatisticsEntity);
			if (statisticsEntity == null) {
				statisticsEntity = new StatisticsEntity();
				statisticsEntity.setPlayerId(playerId);
				entityList.add(statisticsEntity);
			}
			
			// 支付状态
			PayStateEntity payStateEntity = dataCache.getData(PlayerDataKey.PayStateEntity);
			if (payStateEntity == null) {
				payStateEntity = new PayStateEntity();
				payStateEntity.setPlayerId(playerId);
				entityList.add(payStateEntity);
			}
			
			// 指挥官
			CommanderEntity commanderEntity = dataCache.getData(PlayerDataKey.CommanderEntity);
			if (commanderEntity == null) {
				commanderEntity = new CommanderEntity();
				commanderEntity.setPlayerId(playerId);
				commanderEntity.setStarExplores(ArmourStarExplores.unSerialize(commanderEntity, "", ""));
				CommanderObject.create(commanderEntity);
				entityList.add(commanderEntity);
			}
			
			// 剧情任务
			StoryMissionEntity storyMissionEntity = dataCache.getData(PlayerDataKey.StoryMissionEntity);
			if (storyMissionEntity == null) {
				storyMissionEntity = new StoryMissionEntity();
				storyMissionEntity.setPlayerId(playerId);
				storyMissionEntity.setMissionItems(new ArrayList<MissionEntityItem>());
				entityList.add(storyMissionEntity);
			}
			
			// 联盟
			GuildHospiceEntity guildHospiceEntity = dataCache.getData(PlayerDataKey.GuildHospice);
			if (guildHospiceEntity == null) {
				guildHospiceEntity = new GuildHospiceEntity();
				guildHospiceEntity.setPlayerId(playerId);
				HospiceObj bean = new HospiceObj();
				bean.setDbEntity(guildHospiceEntity);
				entityList.add(guildHospiceEntity);
			}
			
			// 尤里复仇
			YuriStrikeEntity yuriStrikeEntity = dataCache.getData(PlayerDataKey.YuriStrike);
			if (yuriStrikeEntity == null) {
				Integer nextCfg = YuristrikeCfg.higherCfgId(0);
				yuriStrikeEntity = new YuriStrikeEntity();
				yuriStrikeEntity.setCfgId(nextCfg.intValue());
				yuriStrikeEntity.setPlayerId(playerId);
				YuriStrike bean = new YuriStrike();
				bean.setDbEntity(yuriStrikeEntity);
				entityList.add(yuriStrikeEntity);
			}
			
			// 调查问卷
			QuestionnaireEntity questionnaireEntity = dataCache.getData(PlayerDataKey.QuestionnaireEntity);
			if (questionnaireEntity == null) {
				questionnaireEntity = new QuestionnaireEntity();
				questionnaireEntity.setPlayerId(playerId);
				questionnaireEntity.setLastCheckTime(HawkTime.getMillisecond());
				entityList.add(questionnaireEntity);
			}
			
			// 玩家野怪
			PlayerMonsterEntity monsterEntity = dataCache.getData(PlayerDataKey.MonsterEntity);
			if (monsterEntity == null) {
				monsterEntity = new PlayerMonsterEntity();
				monsterEntity.setPlayerId(playerId);
				monsterEntity.setMaxLevel(0);
				monsterEntity.setCurrentLevelCount(0);
				entityList.add(monsterEntity);
			}
			
			// 许愿池
			WishingWellEntity wishingEntity = dataCache.getData(PlayerDataKey.WishingEntity);
			if (wishingEntity == null) {
				wishingEntity = new WishingWellEntity(playerId);
				entityList.add(wishingEntity);
			}
			
			// 酒馆, 计算积分成就项
			TavernEntity tavernEntity = dataCache.getData(PlayerDataKey.TavernEntity);
			if (tavernEntity == null) {
				tavernEntity = new TavernEntity(playerId, HawkTime.getMillisecond());
				ConfigIterator<TavernScoreCfg> scoreIterator = HawkConfigManager.getInstance().getConfigIterator(TavernScoreCfg.class);
				while (scoreIterator.hasNext()) {
					TavernScoreCfg cfg = scoreIterator.next();
					AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
					tavernEntity.addScoreItem(item);
				}
				entityList.add(tavernEntity);
			}
			
			// 码头
			WharfEntity wharfEntity = dataCache.getData(PlayerDataKey.WharfEntity);
			if (wharfEntity == null) {
				wharfEntity = new WharfEntity(playerId);
				entityList.add(wharfEntity);
			}
			
			// 每日统计
			DailyDataEntity dailyDataEntity = dataCache.getData(PlayerDataKey.DailyDataEntity);
			if (dailyDataEntity == null) {
				dailyDataEntity = new DailyDataEntity(playerId);
				entityList.add(dailyDataEntity);
			}
			
			PlotBattleEntity plotBattleEntity = dataCache.getData(PlayerDataKey.PlotBattleEntity);
			if (plotBattleEntity == null) {
				plotBattleEntity = new PlotBattleEntity();
				plotBattleEntity.setPlayerId(playerId);
				entityList.add(plotBattleEntity);
			}
			
			// 装扮
			DressEntity dressEntity = dataCache.getData(PlayerDataKey.DressEntity);
			if (dressEntity == null) {
				dressEntity = new DressEntity(playerId);
				entityList.add(dressEntity);
			}
			
			// 累计在线
			AccumulateOnlineEntity accumulateOnlineEnitiy = dataCache.getData(PlayerDataKey.AccumulateOnlineEntity);
			if (accumulateOnlineEnitiy == null) {
				accumulateOnlineEnitiy = new AccumulateOnlineEntity();
				accumulateOnlineEnitiy.setPlayerId(playerId);
				entityList.add(accumulateOnlineEnitiy);
			}
			
			// 超值礼包
			PlayerGiftEntity playerGiftEntity = dataCache.getData(PlayerDataKey.PlayerGiftEntity);
			if (playerGiftEntity == null) {
				playerGiftEntity = new PlayerGiftEntity();
				playerGiftEntity.setPlayerId(playerId);
				playerGiftEntity.afterRead();
				entityList.add(playerGiftEntity);
			}
			
			// 资源礼包
			PlayerResourceGiftEntity playerResourceGiftEntity = dataCache.getData(PlayerDataKey.PlayerResourceGiftEntity);
			if (playerResourceGiftEntity == null) {
				playerResourceGiftEntity = new PlayerResourceGiftEntity();
				playerResourceGiftEntity.setPlayerId(playerId);
				playerResourceGiftEntity.afterRead();
				entityList.add(playerResourceGiftEntity);
			}
			
			// 玩家宝藏基础
			StorehouseBaseEntity storehouseBase = dataCache.getData(PlayerDataKey.StorehouseBase);
			if (storehouseBase == null) {
				storehouseBase = new StorehouseBaseEntity();
				storehouseBase.setPlayerId(playerId);
				entityList.add(storehouseBase);
			}
			
			List<PlayerGachaEntity> playerGachaEntities = dataCache.getData(PlayerDataKey.PlayerGachaEntities);
			if (playerGachaEntities == null) {
				playerGachaEntities = new ArrayList<PlayerGachaEntity>();
				for (GachaType type : GachaType.values()) {
					PlayerGachaEntity gachaEntity = new PlayerGachaEntity();
					gachaEntity.setPlayerId(playerId);
					gachaEntity.setGachaType(type.getNumber());
					playerGachaEntities.add(gachaEntity);
				}
				entityList.addAll(playerGachaEntities);
			}
			
			// 推送礼包
			PushGiftEntity pushGiftEntity = dataCache.getData(PlayerDataKey.PushGiftEntity);
			if (pushGiftEntity == null) {
				pushGiftEntity = new PushGiftEntity();
				pushGiftEntity.setPlayerId(playerId);
				pushGiftEntity.afterRead();
				entityList.add(pushGiftEntity);
			}
			
			// 玩家成就
			PlayerAchieveEntity playerAchieveEntity = dataCache.getData(PlayerDataKey.PlayerAchieveEntity);
			if (playerAchieveEntity == null) {
				playerAchieveEntity = new PlayerAchieveEntity();
				playerAchieveEntity.setPlayerId(playerId);
				playerAchieveEntity.updateMissionItems(new ArrayList<PlayerAchieveItem>());
				entityList.add(playerAchieveEntity);
			}
			
			PlayerOtherEntity playerOtherEntity = dataCache.getData(PlayerDataKey.PlayerOtherEntity);
			if (playerOtherEntity == null) {
				playerOtherEntity = new PlayerOtherEntity();
				playerOtherEntity.setPlayerId(playerId);
				playerOtherEntity.afterRead();
				entityList.add(playerOtherEntity);
			}
			
			HawkDBEntity.batchCreate(entityList);
			
			dataCache.update(PlayerDataKey.PlayerBaseEntity, playerBaseEntity);
			dataCache.update(PlayerDataKey.StatisticsEntity, statisticsEntity);
			dataCache.update(PlayerDataKey.PayStateEntity, payStateEntity);
			dataCache.update(PlayerDataKey.CommanderEntity, commanderEntity);
			dataCache.update(PlayerDataKey.StoryMissionEntity, storyMissionEntity);
			dataCache.update(PlayerDataKey.GuildHospice, guildHospiceEntity);
			dataCache.update(PlayerDataKey.YuriStrike, yuriStrikeEntity);
			dataCache.update(PlayerDataKey.QuestionnaireEntity, questionnaireEntity);
			dataCache.update(PlayerDataKey.MonsterEntity, monsterEntity);
			dataCache.update(PlayerDataKey.WishingEntity, wishingEntity);
			dataCache.update(PlayerDataKey.TavernEntity, tavernEntity);
			dataCache.update(PlayerDataKey.WharfEntity, wharfEntity);
			dataCache.update(PlayerDataKey.DailyDataEntity, dailyDataEntity);
			dataCache.update(PlayerDataKey.PlotBattleEntity, plotBattleEntity);
			dataCache.update(PlayerDataKey.DressEntity, dressEntity);
			dataCache.update(PlayerDataKey.AccumulateOnlineEntity, accumulateOnlineEnitiy);
			dataCache.update(PlayerDataKey.PlayerGiftEntity, playerGiftEntity);
			dataCache.update(PlayerDataKey.PlayerResourceGiftEntity, playerResourceGiftEntity);
			dataCache.update(PlayerDataKey.StorehouseBase, storehouseBase);
			dataCache.update(PlayerDataKey.PlayerGachaEntities, playerGachaEntities);
			dataCache.update(PlayerDataKey.PushGiftEntity, pushGiftEntity);
			dataCache.update(PlayerDataKey.PlayerAchieveEntity, playerAchieveEntity);
			dataCache.update(PlayerDataKey.PlayerOtherEntity, playerOtherEntity);
			
			// 活动数据创建
			List<HawkDBEntity> activityEntities = ActivityManager.getInstance().prepareNewPlayerActivityEntity(playerId);
			HawkDBEntity.batchCreate(activityEntities);
			
			// 日志记录
			HawkLog.logPrintln("prepare newly player dbentities success, playerId: {}, costtime: {}", 
					playerId, HawkTime.getMillisecond() - startTime);
			
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		HawkLog.logPrintln("prepare newly player dbentities failed, playerId: {}", playerId);
		return false;
	}

	/**
	 * 更新缓存失效周期
	 * 
	 * @param expireTime
	 * @return
	 */
	public boolean updateCacheExpire(int expireTime) {
		try {
			if (dataCache != null) {
				return getDataCache().updateCacheExpire(expireTime);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 添加榜单封禁信息
	 * 
	 * @param rankType
	 * @param banInfo
	 */
	public void addBanRankInfo(String rankType, String banInfo) {
		getBanRankInfos().put(rankType, banInfo);
	}

	/**
	 * 获取榜单封禁信息
	 * 
	 * @param rankType
	 * @return
	 */
	public String getBanRankInfo(String rankType) {
		return getBanRankInfos().get(rankType);
	}

	/**
	 * 移除榜单封禁信息
	 * 
	 * @param rankTypes
	 */
	public void removeBanRankInfo(String... rankTypes) {
		for (String rankType : rankTypes) {
			getBanRankInfos().remove(rankType);
		}
	}

	/**
	 * 数据加载完成之后的组装
	 */
	public boolean assembleData(Player player) {
		if (playerEffect != null) {
			playerEffect.init();
		}

		if (getPowerElectric() != null) {
			powerElectric.refreshPowerElectric(player, false, PowerChangeReason.OTHER);
		}

		if (resourceLimit != null) {
			resourceLimit.init();
		}

		return true;
	}

	/**
	 * 设置从缓存中来
	 * 
	 * @param isFromCache
	 */
	public void setFromCache(boolean isFromCache) {
		this.isFromCache = isFromCache;
	}

	/**
	 * 是否从缓存中加载的数据
	 */
	public boolean isFromCache() {
		return isFromCache;
	}

	/**
	 * 设置快照下次更新时间
	 * 
	 * @param snapshotNextUpdateTime
	 */
	public void setSnapshotNextUpdateTime(long snapshotNextUpdateTime) {
		this.snapshotNextUpdateTime = snapshotNextUpdateTime;
	}

	/**
	 * 快照是否需要更新
	 */
	public boolean isSnapshotNeedUpdateTime() {
		return snapshotNextUpdateTime <= HawkTime.getMillisecond();
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////////////
	// 数据操作区
	// ///////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 获取玩家作用号
	 */
	public PlayerEffect getPlayerEffect() {
		if (playerEffect == null) {
			playerEffect = new PlayerEffect(this);
			playerEffect.init();
		}
		return playerEffect;
	}

	/**
	 * 获取玩家战力电力
	 */
	public PowerElectric getPowerElectric() {
		if (powerElectric == null) {
			powerElectric = new PowerElectric(this);
		}
		if (GsApp.getInstance().isInitOK() && !powerElectric.getPowerData().isInited()) {
			// 流失玩家不计算战力
			if (GlobalData.getInstance().getPlayerLossDays(getPlayerId()) < GsConfig.getInstance().getLossCacheDays()) {
				powerElectric.calcElectricBeforeChange();
			}
		}
		return powerElectric;
	}

	/**
	 * 根据作用号取作用值
	 * 
	 * @param effType
	 * @return
	 */
	public int getEffVal(Const.EffType effType) {
		return getEffVal(effType, null);
	}

	/**
	 * 根据作用号取作用值
	 */
	public int getEffVal(Const.EffType effType, String targetId) {
		return getPlayerEffect().getEffVal(effType, targetId);
	}

	/**
	 * 增加物品实体
	 */
	public void addItemEntity(ItemEntity itemEntity) {
		getItemEntities().add(itemEntity);
	}

	/**
	 * 获取物品
	 */
	public ItemEntity getItemById(String id) {
		for (ItemEntity itemEntity : getItemEntities()) {
			if (id.equals(itemEntity.getId())) {
				return itemEntity;
			}
		}
		return null;
	}

	/**
	 * 获取物品
	 */
	public List<ItemEntity> getItemsByItemId(int itemId) {
		return getItemEntities().stream()
				.filter(e -> itemId == e.getItemId() && e.getItemCount() > 0)
				.sorted(new Comparator<ItemEntity>() {
					@Override
					public int compare(ItemEntity o1, ItemEntity o2) {
						if (o1.getItemCount() < o2.getItemCount()) {
							return -1;
						} else if (o1.getItemCount() > o2.getItemCount()) {
							return 1;
						}
						return 0;
					}
				})
				.collect(Collectors.toList());
	}
	
	public int getItemNumByItemType(int itemType) {
		int count = 0;
		HawkConfigManager configManager = HawkConfigManager.getInstance(); 
		ItemCfg itemCfg = null;
		for (ItemEntity itemEntity : getItemEntities()) {
			itemCfg = configManager.getConfigByKey(ItemCfg.class, itemEntity.getItemId());
			if (itemCfg.getItemType() == itemType) {
				count +=  itemEntity.getItemCount();
			}
		}
		return count;
	}
	/**
	 * 获取物品数目
	 */
	public int getItemNumByItemId(int itemId) {
		int cnt = 0;
		for (ItemEntity itemEntity : getItemEntities()) {
			if (itemId == itemEntity.getItemId()) {
				cnt += itemEntity.getItemCount();
			}
		}
		return cnt;
	}
	
	/**
	 * 计算背包中现存的指定加速道具还剩多少时间，仅针对指定加速道具，通用加速不算
	 * 
	 * @param speedType
	 * @return
	 */
	public int getItemSpeedTimeByItemType(int speedType) {
		int speedTime = 0;
		String speedUpType = String.valueOf(speedType);
		for (ItemEntity itemEntity : getItemEntities()) {
			if (itemEntity.getItemCount() <= 0) {
				continue;
			}
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemEntity.getItemId());
			if (itemCfg != null && itemCfg.getItemType() == ToolType.SPEED_UP_VALUE && itemCfg.getSpeedUpType().trim().equals(speedUpType)) {
				speedTime += itemCfg.getSpeedUpTime();
			}
		}
		
		return speedTime;
	}

	/**
	 * 增加天赋实体
	 */
	public void addTalentEntity(TalentEntity talentEntity) {
		getTalentEntities().add(talentEntity);
	}

	/**
	 * 创建新的天赋实体
	 */
	public TalentEntity createTalentEntity(int talentId, int type, int level, TalentLevelCfg cfg) {
		TalentEntity talentEntity = new TalentEntity();
		talentEntity.setPlayerId(playerId);
		talentEntity.setTalentId(talentId);
		talentEntity.setLevel(level);
		talentEntity.setType(type);
		if (cfg.getSkill() != 0) {
			talentEntity.setSkillId(cfg.getSkill());
			talentEntity.setSkillRefTime(getTalentSkillMaxRefTime(cfg.getSkill()));
			if (talentEntity.getSkillRefTime() > 0) {
				talentEntity.setSkillState(GsConst.TalentSkill.IN_CD);
			}
			talentEntity.setCastSkillTime(0);
		}
		if (HawkDBManager.getInstance().create(talentEntity)) {
			getTalentEntities().add(talentEntity);
		}
		return talentEntity;
	}

	/**
	 * 获取天赋等级(几套技能中等级最大的)
	 */
	public int getTalentCurrentMaxLvl(int talentId) {
		int lvl = 0;
		for (TalentEntity talentEntity : getTalentEntities()) {
			if (talentId == talentEntity.getTalentId()) {
				lvl = (talentEntity.getLevel() > lvl) ? talentEntity.getLevel() : lvl;
			}
		}
		return lvl;
	}

	/**
	 * 获取天赋
	 */
	public TalentEntity getTalentByTalentId(int talentId, int talentType) {
		for (TalentEntity talentEntity : getTalentEntities()) {
			if (talentId == talentEntity.getTalentId() && talentType == talentEntity.getType()) {
				return talentEntity;
			}
		}
		return null;
	}

	/**
	 * 获取当前方案天赋技能
	 * 
	 * @param skillId
	 * @return
	 */
	public TalentEntity getTalentSkill(int skillId) {
		for (TalentEntity talentEntity : getTalentEntities()) {
			if (skillId == talentEntity.getSkillId() && talentEntity.getLevel() > 0) {
				return talentEntity;
			}
		}
		return null;
	}

	/**
	 * 获取天赋技能
	 * 
	 * @param skillId
	 * @return
	 */
	public List<TalentEntity> getTalentSkills(int skillId) {
		List<TalentEntity> entities = new ArrayList<>();
		for (TalentEntity talentEntity : getTalentEntities()) {
			if (skillId == talentEntity.getSkillId()) {
				entities.add(talentEntity);
			}
		}
		return entities;
	}

	/**
	 * 获取最大的下次刷新时间
	 * 
	 * @param skillId
	 * @return
	 */
	public long getTalentSkillMaxRefTime(int skillId) {
		long maxRefTime = 0;
		List<TalentEntity> entities = getTalentSkills(skillId);
		for (TalentEntity entity : entities) {
			maxRefTime = (entity.getSkillRefTime() > maxRefTime) ? entity.getSkillRefTime() : maxRefTime;
		}
		return maxRefTime;
	}

	/**
	 * 获取天赋技能
	 * 
	 * @param skillId
	 * @return
	 */
	public List<TalentEntity> getAllTalentSkills() {
		List<TalentEntity> retList = new ArrayList<>();
		for (TalentEntity talentEntity : getTalentEntities()) {
			if (talentEntity.getLevel() > 0 && talentEntity.getSkillId() != 0) {
				retList.add(talentEntity);
			}
		}
		return retList;
	}
	
	/**
	 * 获取天赋技能
	 * 
	 * @param skillId
	 * @return
	 */
	public List<TalentEntity> getTalentSkills() {
		List<TalentEntity> retList = new ArrayList<>();
		int talentType = getPlayerEntity().getTalentType();
		for (TalentEntity talentEntity : getTalentEntities()) {
			if (talentEntity.getType() == talentType && talentEntity.getLevel() > 0 && talentEntity.getSkillId() != 0) {
				retList.add(talentEntity);
			}
		}
		return retList;
	}

	/**
	 * 已使用的天赋技能
	 * 
	 * @return
	 */
	public List<Integer> castedTalentSkill() {
		List<Integer> retList = new ArrayList<>();
		List<TalentEntity> entities = getTalentEntities();
		for (TalentEntity entity : entities) {
			int skillId = entity.getSkillId();
			SkillCfg skillCfg = HawkConfigManager.getInstance().getConfigByKey(SkillCfg.class, skillId);
			if (skillCfg == null) {
				continue;
			}

			if (entity.getSkillRefTime() - skillCfg.getCd() * 1000 < HawkTime.getMillisecond()) {
				continue;
			}
			retList.add(entity.getSkillId());
		}
		return retList;
	}

	/**
	 * 获取建筑列表
	 */
	public List<BuildingBaseEntity> getBuildingEntities() {
		return getBuildingEntitiesIgnoreStatus().stream()
				.filter(e -> e.getStatus() != BuildingStatus.BUILDING_CREATING_VALUE)
				.collect(Collectors.toList());
	}

	/**
	 * 增加一个建筑
	 */
	public void addBuildingEntity(BuildingBaseEntity entity) {
		getBuildingEntitiesIgnoreStatus().add(entity);
	}

	/**
	 * 删除建筑实体
	 * 
	 * @param entity
	 */
	public void deleteBuildingEntity(BuildingBaseEntity entity) {
		getBuildingEntitiesIgnoreStatus().remove(entity);
	}

	/**
	 * 根据建筑唯一id获取建筑对象
	 * 
	 * @param id
	 */
	public BuildingBaseEntity getBuildingBaseEntity(String id) {
		Optional<BuildingBaseEntity> op = getBuildingEntitiesIgnoreStatus().stream()
				.filter(e -> e.getStatus() != BuildingStatus.BUILDING_CREATING_VALUE)
				.filter(e -> e.getId().equals(id))
				.findAny();
		
		if (op.isPresent()) {
			return op.get();
		}
		
		return null;
	}

	/**
	 * 根据建筑唯一id获取建筑对象, 包括在建中的建筑
	 * 
	 * @param id
	 */
	public BuildingBaseEntity getBuildingEntityIgnoreStatus(String id) {
		Optional<BuildingBaseEntity> op = getBuildingEntitiesIgnoreStatus().stream()
				.filter(e -> e.getId().equals(id)).findAny();
		
		if (op.isPresent()) {
			return op.get();
		}
		
		return null;
	}

	/**
	 * 获取建筑等级
	 * 
	 * @param id
	 */
	public int getBuildingLevel(String id) {
		BuildingBaseEntity entity = getBuildingBaseEntity(id);
		if (entity == null) {
			return 0;
		}

		BuildingCfg conf = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, entity.getBuildingCfgId());
		return conf.getLevel();
	}

	/**
	 * 根据类型获取建筑列表
	 * 
	 * @param type
	 */
	public List<BuildingBaseEntity> getBuildingListByType(BuildingType type) {
		List<BuildingBaseEntity> list = new ArrayList<BuildingBaseEntity>();
		for (BuildingBaseEntity buildingEntity : getBuildingEntitiesIgnoreStatus()) {
			if (buildingEntity.getType() == type.getNumber() && buildingEntity.getStatus() != BuildingStatus.BUILDING_CREATING_VALUE) {
				list.add(buildingEntity);
			}
		}
		return list;
	}

	/**
	 * 根据类型获取建筑数量
	 * 
	 * @param type
	 */
	public int getBuildCount(BuildingType type) {
		return getBuildingListByType(type).size();
	}

	/**
	 * 根据资源田类型建筑数量
	 * 
	 * @param resourceType
	 */
	public int getResBuildCount() {
		int count = 0;
		count += getBuildCount(BuildingType.ORE_REFINING_PLANT);
		count += getBuildCount(BuildingType.OIL_WELL);
		count += getBuildCount(BuildingType.STEEL_PLANT);
		count += getBuildCount(BuildingType.RARE_EARTH_SMELTER);
		return count;
	}

	/**
	 * 根据建筑类型获取建筑实体
	 * 
	 * @param type
	 * @return
	 */
	public BuildingBaseEntity getBuildingEntityByType(BuildingType type) {
		for (BuildingBaseEntity building : getBuildingEntitiesIgnoreStatus()) {
			if (building.getStatus() == BuildingStatus.BUILDING_CREATING_VALUE || building.getType() != type.getNumber()) {
				continue;
			}
			return building;
		}
		return null;
	}

	/**
	 * 根据资源建筑类型获取同类资源建筑的总产出
	 * 
	 * @param buildingType
	 * @return
	 */
	public int getBuildingResOutputByType(int buildingType) {
		int output = 0;
		for (BuildingBaseEntity buildingEntity : getBuildingEntitiesIgnoreStatus()) {
			if (buildingEntity.getType() == buildingType && buildingEntity.getStatus() != BuildingStatus.BUILDING_CREATING_VALUE) {
				BuildingCfg buildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
				if (buildCfg != null && buildCfg.isResBuilding()) {
					output += buildCfg.getResPerHour();
				}
			}
		}

		return output;
	}

	/**
	 * 根据限制类型获取建筑列表
	 * 
	 * @param limitType
	 */
	public List<BuildingBaseEntity> getBuildingListByLimitTypeIgnoreStatus(LimitType... limitTypes) {
		EnumSet<LimitType> set = EnumSet.noneOf(LimitType.class);
		for (LimitType e : limitTypes) {
			set.add(e);
		}

		List<BuildingBaseEntity> list = new ArrayList<BuildingBaseEntity>();
		for (BuildingBaseEntity buildingEntity : getBuildingEntitiesIgnoreStatus()) {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
			if (buildingCfg == null) {
				continue;
			}
			LimitType cfgLimitType = LimitType.valueOf(buildingCfg.getLimitType());
			if (set.contains(cfgLimitType)) {
				list.add(buildingEntity);
			}
		}
		return list;
	}

	/**
	 * 根据限制类型获取建筑列表
	 * 
	 * @param limitType
	 */
	public List<BuildingBaseEntity> getBuildingListByLimitType(LimitType... limitTypes) {
		return getBuildingListByLimitTypeIgnoreStatus(limitTypes).stream()
				.filter(e -> e.getStatus() != BuildingStatus.BUILDING_CREATING_VALUE)
				.collect(Collectors.toList());
	}

	/**
	 * 获取特定种类的防御建筑
	 */
	public List<BuildingBaseEntity> getBuildingListByCfgId(int buildingCfgId) {
		List<BuildingBaseEntity> list = new ArrayList<BuildingBaseEntity>();
		for (BuildingBaseEntity buildingEntity : getBuildingEntitiesIgnoreStatus()) {
			if (buildingEntity.getBuildingCfgId() != buildingCfgId || buildingEntity.getStatus() == BuildingStatus.BUILDING_CREATING_VALUE) {
				continue;
			}
			list.add(buildingEntity);
		}
		return list;
	}

	/**
	 * 获取某个类型等级最大的建筑
	 * 
	 * @param type
	 */
	public int getBuildingMaxLevel(int type) {
		int level = 0;
		for (BuildingBaseEntity buildingEntity : getBuildingEntitiesIgnoreStatus()) {
			if (buildingEntity.getType() != type || buildingEntity.getStatus() == BuildingStatus.BUILDING_CREATING_VALUE) {
				continue;
			}
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
			if (buildingCfg == null) {
				continue;
			}
			if (buildingCfg.getLevel() > level) {
				level = buildingCfg.getLevel();
			}
		}
		return level;
	}
	
	/**
	 * 获取同类建筑里面最大的建筑ID
	 * 
	 * @param type
	 * @return
	 */
	public int getMaxLevelBuildingCfg(int type) {
		int cfgId = 0;
		for (BuildingBaseEntity buildingEntity : getBuildingEntitiesIgnoreStatus()) {
			if (buildingEntity.getType() != type || buildingEntity.getStatus() == BuildingStatus.BUILDING_CREATING_VALUE) {
				continue;
			}
			
			if (buildingEntity.getBuildingCfgId() > cfgId) {
				cfgId = buildingEntity.getBuildingCfgId();
			}
		}
		return cfgId;
	}

	/**
	 * 获取建筑配置
	 * 
	 * @param buildingType
	 */
	public BuildingCfg getBuildingCfgByType(BuildingType buildingType) {
		BuildingBaseEntity building = getBuildingEntityByType(buildingType);
		if (building == null) {
			return null;
		}
		return HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, building.getBuildingCfgId());
	}

	/**
	 * 获取交易税率
	 */
	public int getTradeTaxRate() {
		BuildingCfg cfg = getBuildingCfgByType(Const.BuildingType.TRADE_CENTRE);
		int rate = cfg == null ? 5000 : cfg.getMarketTax();
		rate -= getEffVal(EffType.GUILD_TRADE_TAXRATE);
		return Math.max(0, rate);
	}

	/**
	 * 获取大本等级
	 */
	public int getConstructionFactoryLevel() {
		BuildingCfg buildingCfg = getBuildingCfgByType(Const.BuildingType.CONSTRUCTION_FACTORY);
		if (buildingCfg == null) {
			return 0;
		}
		return buildingCfg.getLevel();
	}

	/**
	 * 获取玩家大本营
	 * 
	 * @return
	 */
	public BuildingBaseEntity getConstructionFactory() {
		return getBuildingEntityByType(Const.BuildingType.CONSTRUCTION_FACTORY);
	}

	/**
	 * 增加一个队列
	 * 
	 * @param queueEntity
	 */
	public void addQueueEntity(QueueEntity queueEntity) {
		getQueueEntities().add(queueEntity);
	}

	/**
	 * 通过队列的唯一id查找正在进行的队列
	 * 
	 * @param id
	 */
	public QueueEntity getQueueEntity(String id) {
		for (QueueEntity queueEntity : getQueueEntities()) {
			if (queueEntity.getId().equals(id)) {
				return queueEntity;
			}
		}
		return null;
	}

	/**
	 * 通过队列类型获取正在进行中的队列
	 * 
	 * @param queueType
	 */
	public List<QueueEntity> getBusyCommonQueue(int queueType) {
		List<QueueEntity> queueList = new ArrayList<QueueEntity>();
		for (QueueEntity queueEntity : getQueueEntities()) {
			// enableEndTime判断是否是第二城建队列
			if (queueEntity.getEnableEndTime() == 0 && queueEntity.getQueueType() == queueType && queueEntity.getReusage() != QueueReusage.FREE.intValue()) {
				queueList.add(queueEntity);
			}
		}
		return queueList;
	}
	
	/**
	 * 根据建筑类型获取建筑身上的队列数据
	 * 
	 * @param buildingType
	 * @return
	 */
	public QueueEntity getQueueByBuildingType(int buildingType) {
		for (QueueEntity queueEntity : getQueueEntities()) {
			if (queueEntity.getBuildingType() == buildingType && queueEntity.getReusage() != QueueReusage.FREE.intValue()) {
				return queueEntity;
			}
		}
		
		return null;
	}

	/**
	 * 有正在进行中的队列
	 */
	public boolean hasBusyCommonQueue(List<Integer> typeList) {
		for (QueueEntity queueEntity : getQueueEntities()) {
			if (queueEntity.getEnableEndTime() == 0 && typeList.contains(queueEntity.getQueueType()) && queueEntity.getReusage() != QueueReusage.FREE.intValue()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取付费队列实体
	 * 
	 * @return
	 */
	public QueueEntity getPaidQueue() {
		Optional<QueueEntity> op = getQueueEntities().stream().filter(e -> e.getEnableEndTime() > 0).findAny();
		// 付费队列只有一条
		if (op.isPresent()) {
			return op.get();
		}
		
		try {
			if (isSecondBuildUnlock()) {
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				QueueEntity queue = QueueService.getInstance().addQueue(player, 0, 0, GsConst.NULL_STRING, 0, 0, 0d, null, GsConst.QueueReusage.FREE,0);
				if(queue != null) {
					queue.setEnableEndTime(HawkApp.getInstance().getCurrentTime());
					addQueueEntity(queue);
					HawkDBManager.getInstance().create(queue);
				}
				
				return queue;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return null;
	}
	
	/**
	 * 判断第二建造队列是否已开启
	 * 
	 * @return
	 */
	public boolean isSecondBuildUnlock() {
		long serverOpenTime = GlobalData.getInstance().getServerOpenTime(getPlayerEntity().getServerId());
		if (serverOpenTime < ConstProperty.getInstance().getSecondBuildDefaultOpenTimeLong()) {
			return true;
		}
		
		CustomDataEntity customData = getCustomDataEntity(GsConst.SECOND_BUILD_OPEN_KEY);
		return customData != null && customData.getValue() > 0;
	}

	/**
	 * 获取可重用的空闲队列
	 * 
	 * @param queueType
	 * @return
	 */
	public QueueEntity getFreeQueue(int queueType) {
		if (queueType == QueueType.BUILDING_QUEUE_VALUE) {
			return getFreeBuildingQueue();
		}

		for (QueueEntity queueEntity : getQueueEntities()) {
			if (queueEntity.getEnableEndTime() == 0 && queueEntity.getReusage() == QueueReusage.FREE.intValue()) {
				return queueEntity;
			}
		}

		return null;
	}

	/**
	 * 获取可重用的空闲建筑队列
	 * 
	 * @return
	 */
	private QueueEntity getFreeBuildingQueue() {
		Optional<QueueEntity> op = getQueueEntities().stream().filter(e -> e.getReusage() != QueueReusage.FREE.intValue())
				.filter(e -> e.getQueueType() == QueueType.BUILDING_QUEUE_VALUE)
				.filter(e -> e.getEnableEndTime() == 0)
				.findAny();
		if (op.isPresent()) {
			Optional<QueueEntity> op1 = getQueueEntities().stream().filter(e -> e.getEnableEndTime() > 0 && e.getReusage() == QueueReusage.FREE.intValue()).findAny();
			if (op1.isPresent()) {
				return op1.get();
			}
		} else {
			op = getQueueEntities().stream().filter(e -> e.getEnableEndTime() == 0 && e.getReusage() == QueueReusage.FREE.intValue()).findAny();
			if (op.isPresent()) {
				return op.get();
			}
		}

		return null;
	}

	/**
	 * 通过队列的
	 * 
	 * @param id
	 */
	public QueueEntity getQueueEntityByItemId(String id) {
		for (QueueEntity queueEntity : getQueueEntities()) {
			if (queueEntity.getItemId().equals(id) && queueEntity.getReusage() != QueueReusage.FREE.intValue()) {
				return queueEntity;
			}
		}
		return null;
	}

	/**
	 * 通过队列的唯一id和附属itemId查找正在进行的队列
	 * 
	 * @param id
	 */
	public QueueEntity getQueueEntity(String id, String itemId) {
		for (QueueEntity queueEntity : getQueueEntities()) {
			if (queueEntity.getId().equals(id) && queueEntity.getItemId().equals(itemId)) {
				return queueEntity;
			}
		}
		return null;
	}

	/**
	 * 获取某个队列类型里的所有队列
	 * 
	 * @param type
	 */
	public Map<String, QueueEntity> getQueueEntitiesByType(int type) {
		Map<String, QueueEntity> map = new HashMap<String, QueueEntity>();
		for (QueueEntity queueEntity : getQueueEntities()) {
			if (queueEntity.getQueueType() == type) {
				map.put(queueEntity.getItemId(), queueEntity);
			}
		}
		return map;
	}

	/**
	 * 创建科技实体
	 * 
	 * @param scienceId
	 */
	public TechnologyEntity createTechnologyEntity(TechnologyCfg cfg) {
		TechnologyEntity entity = new TechnologyEntity();
		entity.setTechId(cfg.getTechId());
		entity.setPlayerId(playerId);
		entity.setLevel(0);
		if (!HawkDBManager.getInstance().create(entity)) {
			return null;
		}

		addTechnologyEntity(entity);
		return entity;
	}

	/**
	 * 获得科技实体
	 * 
	 * @param id
	 *            科技实体Id
	 */
	public TechnologyEntity getTechnologyEntity(String id) {
		for (TechnologyEntity technologyEntity : getTechnologyEntities()) {
			if (technologyEntity.getId().equals(id)) {
				return technologyEntity;
			}
		}
		return null;
	}

	/**
	 * 获得科技实体
	 * 
	 * @param techId
	 *            科技Id
	 */
	public TechnologyEntity getTechEntityByTechId(int techId) {
		return getTechnologyEntities().stream()
				.filter(e -> e.getTechId() == techId)
				.findFirst()
				.orElse(null);
	}

	/**
	 * 增加科技实体
	 * 
	 * @param entity
	 */
	public void addTechnologyEntity(TechnologyEntity entity) {
		getTechnologyEntities().add(entity);
	}

	/**
	 * 移除科技实体
	 * 
	 * @param id
	 */
	public void removeTechnologyEntity(TechnologyEntity entity) {
		getTechnologyEntities().remove(entity);
		entity.delete();
	}

	/**
	 * 获取军队实体
	 * 
	 * @param armyId
	 */
	public ArmyEntity getArmyEntity(int armyId) {
		for (ArmyEntity armyEntity : getArmyEntities()) {
			if (armyEntity.getArmyId() == armyId) {
				return armyEntity;
			}
		}
		return null;
	}

	/**
	 * 添加军队实体
	 * 
	 * @param armyEntity
	 */
	public void addArmyEntity(ArmyEntity armyEntity) {
		this.getArmyEntities().add(armyEntity);
	}

	/**
	 * 获取总兵力数
	 */
	public int getArmyCount() {
		int armyCount = 0;
		for (ArmyEntity entity : getArmyEntities()) {
			armyCount += entity.getFree();
		}
		return armyCount;
	}

	/**
	 * 批量添加装备实体
	 * 
	 * @param equipEntity
	 */
	public void addEquipEntities(Collection<? extends EquipEntity> equipEntities) {
		this.getEquipEntities().addAll(equipEntities);
		// 抛出活动事件
		ActivityManager.getInstance().postEvent(new EquipChangeEvent(getPlayerEntity().getId()));
	}

	/**
	 * 删除装备实体
	 * 
	 * @param entity
	 */
	public void removeEquipEntity(EquipEntity equipEntity) {
		getEquipEntities().remove(equipEntity);
		equipEntity.delete();
	}

	/**
	 * 根据装备id获取装备实体
	 * 
	 * @param equipId
	 * @return
	 */
	public EquipEntity getEquipEntity(String equipId) {
		Optional<EquipEntity> op = getEquipEntities().stream()
				.filter(e -> e.getId().equals(equipId))
				.findAny();
		if (op.isPresent()) {
			return op.get();
		}
		return null;
	}

	/**
	 * 获取指挥官包装类
	 * 
	 * @return
	 */
	public CommanderObject getCommanderObject() {
		return getCommanderEntity().getCommanderObject();
	}

	/**
	 * 根据任务的uuid获取任务实体
	 * 
	 * @param id
	 */
	public MissionEntity getMissionById(String uuid) {
		for (MissionEntity missionEntity : getMissionEntities()) {
			if (missionEntity.getId().equals(uuid)) {
				return missionEntity;
			}
		}
		return null;
	}

	public List<MissionEntity> getOpenedMissions() {
		return getMissionEntities().stream().filter(e -> e.getState() != MissionState.STATE_NOT_OPEN).collect(Collectors.toList());
	}

	/**
	 * 根据任务的typeId获取任务实体
	 * 
	 * @param typeId
	 */
	public MissionEntity getMissionByTypeId(int typeId) {
		for (MissionEntity entity : getMissionEntities()) {
			if (entity.getTypeId() == typeId) {
				return entity;
			}
		}
		return null;
	}

	/**
	 * 获取玩家Buff
	 */
	public StatusDataEntity getStatusById(int statusId) {
		return getStatusById(statusId, null);
	}

	/**
	 * 获取玩家Buff
	 */
	public StatusDataEntity getStatusById(int statusId, String targetId) {
		for (StatusDataEntity entity : getStatusDataEntities()) {
			if (entity.getStatusId() != statusId || entity.getType() != StateType.BUFF_STATE_VALUE) {
				continue;
			}

			// 目标不一致
			if (!HawkOSOperator.isEmptyString(targetId)) {
				if (HawkOSOperator.isEmptyString(entity.getTargetId()) || !entity.getTargetId().equals(targetId)) {
					continue;
				}
			} else if (!HawkOSOperator.isEmptyString(entity.getTargetId())) {
				continue;
			}

			return entity;
		}
		
		if (statusId == Const.EffType.CITY_SHIELD_VALUE) {
			return createCityShieldEntity();
		}

		return null;
	}
	
	/**
	 * #27658 大本升8级流程处理
	 * 
	 * @return
	 */
	private StatusDataEntity createCityShieldEntity() {
		try {
			AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(this.getPlayerEntity().getId());
			// 一个新玩家进来，会在外部统一创建
			if (accountInfo != null && accountInfo.isNewly()) {
				return null;
			}
			
			HawkLog.logPrintln("playerdata fix, add city_shield entity, playerId: {}", this.getPlayerEntity().getId());
			long time = HawkTime.getMillisecond() - HawkTime.DAY_MILLI_SECONDS;
			StatusDataEntity entity = new StatusDataEntity();
			entity.setPlayerId(this.getPlayerEntity().getId());
			entity.setStartTime(time);
			entity.setEndTime(time);
			entity.setType(StateType.BUFF_STATE_VALUE);
			entity.setVal(1);
			entity.setStatusId(Const.EffType.CITY_SHIELD_VALUE);
			entity.setTargetId("");
			HawkDBManager.getInstance().create(entity);
			getStatusDataEntities().add(entity);
			return entity;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return null;
	}

	/**
	 * 获取作用在不同对象上的相同buff的列表
	 * 
	 * @param statusId
	 * @return
	 */
	public List<StatusDataEntity> getStatusListById(int statusId) {
		List<StatusDataEntity> list = new ArrayList<>();
		for (StatusDataEntity entity : getStatusDataEntities()) {
			if (entity.getStatusId() == statusId && entity.getType() == StateType.BUFF_STATE_VALUE && entity.getTargetId() != null) {
				list.add(entity);
			}
		}

		return list;
	}

	/**
	 * 获取玩家状态
	 */
	public StatusDataEntity getStateById(int stateId, int stateType) {
		for (StatusDataEntity entity : getStatusDataEntities()) {
			if (entity.getStatusId() == stateId && entity.getType() == stateType) {
				return entity;
			}
		}
		return null;
	}

	/**
	 * 获得正在创建或者升级建筑的uuid
	 */
	public List<String> getBusyBuildingUuid() {
		List<String> uuidList = new ArrayList<String>();
		for (QueueEntity entity : getQueueEntities()) {
			if (entity.getQueueType() == QueueType.BUILDING_QUEUE_VALUE || entity.getQueueType() == QueueType.BUILDING_DEFENER_VALUE) {
				uuidList.add(entity.getItemId());
			}
		}
		return uuidList;
	}

	/**
	 * 获取建筑数目上限
	 * 
	 * @param limitType
	 */
	public int getBuildingNumLimit(int limitType) {
		// 获取大本数据等级
		int level = this.getBuildingMaxLevel(BuildingType.CONSTRUCTION_FACTORY_VALUE);
		BuildLimitCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildLimitCfg.class, limitType);
		if (cfg == null) {
			return 0;
		}

		return cfg.getLimit(level);
	}

	/**
	 * 玩家石油最大存储量
	 */
	public long getMaxStoreOil() {
		return resourceLimit.getMaxStoreOil();
	}

	/**
	 * 增加玩家石油最大存储量
	 */
	public void addOilMaxStore(long addStoreOil) {
		resourceLimit.addOilMaxStore(addStoreOil);
	}

	/**
	 * 玩家钢铁最大存储量
	 */
	public long getMaxStoreSteel() {
		return resourceLimit.getMaxStoreSteel();
	}

	/**
	 * 增加玩家钢铁最大存储量
	 */
	public void addSteelMaxStore(long addStoreSteel) {
		resourceLimit.addSteelMaxStore(addStoreSteel);
	}

	/**
	 * 玩家合金最大存储量
	 */
	public long getMaxStoreRare() {
		return resourceLimit.getMaxStoreRare();
	}

	/**
	 * 增加addStoreRare 玩家合金最大存储量
	 */
	public void addRareMaxStore(long addStoreRare) {
		resourceLimit.addRareMaxStore(addStoreRare);
	}

	/**
	 * 玩家矿石最大存储量
	 */
	public long getMaxStoreOre() {
		return resourceLimit.getMaxStoreOre();
	}

	/**
	 * 增加玩家矿石最大存储量
	 */
	public void addOreMaxStore(long addStoreOre) {
		resourceLimit.addOreMaxStore(addStoreOre);
	}

	/**
	 * 玩家每小时石油产出量
	 */
	public long getOilOutputPerHour() {
		return resourceLimit.getOilOutputPerHour();
	}

	/**
	 * 增加addOilOutputPerHour 玩家每小时石油产出量
	 */
	public void addOilOutputPerHour(long addOilOutputPerHour) {
		resourceLimit.addOilOutputPerHour(addOilOutputPerHour);
	}

	/**
	 * 玩家每小时钢铁产出量
	 */
	public long getSteelOutputPerHour() {
		return resourceLimit.getSteelOutputPerHour();
	}

	/**
	 * 增加玩家每小时钢铁产出量
	 */
	public void addSteelOutputPerHour(long addSteelOutputPerHour) {
		resourceLimit.addSteelOutputPerHour(addSteelOutputPerHour);
	}

	/**
	 * 玩家每小时合金产出量
	 */
	public long getRareOutputPerHour() {
		return resourceLimit.getRareOutputPerHour();
	}

	/**
	 * 增加玩家每小时合金产出量
	 */
	public void addRareOutputPerHour(long addRareOutputPerHour) {
		resourceLimit.addRareOutputPerHour(addRareOutputPerHour);
	}

	/**
	 * 玩家每小时矿石产出量
	 */
	public long getOreOutputPerHour() {
		return resourceLimit.getOreOutputPerHour();
	}

	/**
	 * 增加玩家每小时矿石产出量
	 */
	public void addOreOutputPerHour(long addOreOutputPerHour) {
		resourceLimit.addOreOutputPerHour(addOreOutputPerHour);
	}

	/**
	 * 玩家vip是否激活
	 * 
	 * @return 激活true
	 */
	public boolean getVipActivated() {
		return getPlayerEntity().getVipLevel() > 0;
	}

	/**
	 * buff效果结束时间
	 * 
	 * @param buffId
	 *            作用号
	 */
	public long getBuffEndTime(int buffId) {
		StatusDataEntity entity = getStatusById(buffId);
		if (entity == null) {
			return 0;
		}
		return entity.getEndTime();
	}

	/**
	 * 删除城市保护buff
	 */
	protected StatusDataEntity removeCityShield() {
		StatusDataEntity entity = getStatusById(Const.EffType.CITY_SHIELD_VALUE);
		if (entity != null) {
			entity.setEndTime(0);
			entity.setVal(GsConst.ProtectState.NO_BUFF);
			
			Player player = GlobalData.getInstance().makesurePlayer(getPlayerEntity().getId());
			if (player != null) {
				LogUtil.logCityShieldChange(player, false, 0, false);
			} else {
				HawkLog.errPrintln("add remove CityShieldChangeLog failed, playerId: {}", getPlayerEntity().getId());
			}
		}
		
		return entity;
	}

	/**
	 * 获取城市保护buff时间（无buff 返回0）
	 */
	public long getCityShieldTime() {
		return getBuffEndTime(EffType.CITY_SHIELD_VALUE);
	}

	/**
	 * 使用道具添加buff
	 * 
	 * @param buffId
	 */
	protected StatusDataEntity addStatusBuff(int buffId) {
		return addStatusBuff(buffId, null);
	}

	/**
	 * 使用道具添加buff
	 * 
	 * @param buffId
	 */
	protected StatusDataEntity addStatusBuff(int buffId, String targetId) {
		return addStatusBuff(buffId, targetId, 0);
	}

	/**
	 * 添加自带补偿时间的buff
	 * 
	 * @param buffId
	 * @param targetId
	 * @param endTime
	 * @return
	 */
	protected StatusDataEntity addStatusBuff(int buffId, String targetId, long endTime) {
		BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, buffId);
		if (buffCfg == null) {
			return null;
		}

		long now = HawkTime.getMillisecond();
		if (endTime <= 0) {
			endTime = now + buffCfg.getTime() * 1000;
			// 使用增产道具时，计算作用号加成
			if (!HawkOSOperator.isEmptyString(targetId)) {
				endTime = now + (int) Math.ceil(buffCfg.getTime() * (1 + getEffVal(Const.EffType.PRODUCT_INC_TIME_ADD_PER) * GsConst.EFF_PER)) * 1000;
			}
		}

		StatusDataEntity entity = addStatusBuff(buffCfg.getEffect(), targetId, now, endTime, buffCfg.getValue());
		return entity;
	}
	
	/**
	 * 添加buff
	 * @param statusId
	 * @param endTime
	 * @return
	 */
	public StatusDataEntity addStatusBuff(int statusId, long endTime) {
		if (Const.EffType.valueOf(statusId) == null) {
			return null;
		}
		
		return addStatusBuff(statusId, "", HawkTime.getMillisecond(), endTime, 1);
	}
			
	/**
	 * 添加buff
	 * @param statusId
	 * @param targetId
	 * @param startTime
	 * @param endTime
	 * @param val
	 * @return
	 */
	private StatusDataEntity addStatusBuff(int statusId, String targetId, long startTime, long endTime, int val) {
		StatusDataEntity entity = getStatusById(statusId, targetId);

		if (entity != null) {
			// 若使用免战道具，先去掉新手保护,且解除现有警报
			if (entity.getStatusId() == Const.EffType.CITY_SHIELD_VALUE) {
				getStatisticsEntity().setAtkInProtectCnt(1);
				entity.setEndTime(0);
				entity.resetShieldNoticed(false);
				entity.setInitiative(false);
			}

			entity.setVal(val);
			entity.setStartTime(startTime);
			entity.setEndTime(endTime);
		} else {
			entity = new StatusDataEntity();
			entity.setPlayerId(playerId);
			entity.setStartTime(startTime);
			entity.setEndTime(endTime);
			entity.setType(StateType.BUFF_STATE_VALUE);
			entity.setVal(val);
			entity.setStatusId(statusId);
			entity.setTargetId(targetId);
			if (HawkDBManager.getInstance().create(entity)) {
				getStatusDataEntities().add(entity);
			}
		}
		
		logStatusBuff(entity);
		
		return entity;
	}
	
	/**
	 * buff生效变更打点记录
	 * @param entity
	 */
	private void logStatusBuff(StatusDataEntity entity) {
		if (entity.getStatusId() == Const.EffType.CITY_SHIELD_VALUE) {
			Player player = GlobalData.getInstance().makesurePlayer(getPlayerEntity().getId());
			if (player != null) {
				LogUtil.logCityShieldChange(player, true, entity.getEndTime(), false);
			} else {
				HawkLog.errPrintln("add CityShieldChangeLog failed, playerId: {}", getPlayerEntity().getId());
			}
			return;
		}

		try {
			String targetId = HawkOSOperator.isEmptyString(entity.getTargetId()) ? "" : entity.getTargetId();
			Player player = GlobalData.getInstance().makesurePlayer(entity.getPlayerId());
			Map<String, Object> param = new HashMap<>();
	        param.put("effectId", entity.getStatusId()); //作用号ID
	        param.put("effectVal", entity.getVal());     //作用号值
	        param.put("targetId", targetId); //作用对象ID
	        param.put("endTime", HawkTime.formatTime(entity.getEndTime())); //作用号生效结束时间
	        LogUtil.logActivityCommon(player, LogInfoType.buff_change, param);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 删除队列
	 * 
	 * @param queueEntity
	 */
	public void removeQueue(QueueEntity queueEntity) {
		// 判断是否是可重用队列，可重用队列不删除
		if (queueEntity.getReusage() < GsConst.QueueReusage.FREE.intValue()) {
			queueEntity.delete();
			getQueueEntities().remove(queueEntity);
		} else {
			queueEntity.remove();
		}

		// 删除队列的联盟帮助数据
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		GuildService.getInstance().removeGuildHelp(guildId, queueEntity.getId());
	}

	/**
	 * 删除建筑
	 * 
	 * @param building
	 */
	public void removeBuilding(BuildingBaseEntity building) {
		building.delete();
		getBuildingEntitiesIgnoreStatus().remove(building);
	}

	/**
	 * 获取玩家城内的资源产出率
	 * 
	 * @param resType
	 *            资源类型
	 */
	public long getResourceOutputRate(int resType) {
		long output = 0;
		switch (resType) {
		case PlayerAttr.GOLDORE_UNSAFE_VALUE:
			output = getOreOutputPerHour();
			break;
		case PlayerAttr.OIL_UNSAFE_VALUE:
			output = getOilOutputPerHour();
			break;
		case PlayerAttr.STEEL_UNSAFE_VALUE:
			output = getSteelOutputPerHour();
			break;
		case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
			output = getRareOutputPerHour();
			break;
		default:
			break;
		}

		return output;
	}

	/**
	 * 获取自定义数据对象
	 * 
	 * @param key
	 */
	public CustomDataEntity getCustomDataEntity(String key) {
		for (CustomDataEntity entity : getCustomDataEntities()) {
			if (entity.getType().equals(key)) {
				return entity;
			}
		}
		return null;
	}

	/**
	 * 创建新的用户数据项
	 * 
	 * @param key
	 * @param value
	 */
	public CustomDataEntity createCustomDataEntity(String type, int value, String arg) {
		if (!HawkOSOperator.isEmptyString(type)) {
			CustomDataEntity entity = new CustomDataEntity();
			entity.setPlayerId(playerId);
			entity.setType(type);
			entity.setValue(value);
			if (arg != null) {
				entity.setArg(arg);
			}

			entity.setId(HawkOSOperator.randomUUID());
			if (entity.create(true)) {
				getCustomDataEntities().add(entity);
				return entity;
			}
		}
		return null;
	}

	/**
	 * 创建建筑
	 * 
	 * @param buildingCfg
	 * @param index
	 * @param immediate
	 * @return
	 */
	public BuildingBaseEntity createBuildingEntity(BuildingCfg buildingCfg, String index, boolean immediate) {
		// 入库
		BuildingBaseEntity buildingEntity = new BuildingBaseEntity();
		buildingEntity.setId(HawkOSOperator.randomUUID());
		buildingEntity.setBuildingCfgId(buildingCfg.getId());
		buildingEntity.setPlayerId(playerId);
		buildingEntity.setType(buildingCfg.getBuildType());
		buildingEntity.setBuildIndex(index);
		buildingEntity.setStatus(BuildingStatus.BUILDING_CREATING_VALUE);
		buildingEntity.setResUpdateTime(HawkApp.getInstance().getCurrentTime());
		if (!immediate) {
			boolean success = buildingEntity.create(GsConfig.getInstance().isEntityAsyncCreate());
			if (!success) {
				HawkLog.errPrintln("create building entity failed, playerId: {}, cfgId: {}", playerId, buildingCfg.getId());
			}
			
			addBuildingEntity(buildingEntity);
		}

		return buildingEntity;
	}

	/**
	 * 创建完建筑后刷新建筑，修改建筑属性
	 * 
	 * @param buildingEntity
	 */
	public void refreshNewBuilding(BuildingBaseEntity buildingEntity) {
		buildingEntity.setStatus(BuildingStatus.COMMON_VALUE);
		if (buildingEntity.getType() == BuildingType.CITY_WALL_VALUE) {
			int maxCityDef = getRealMaxCityDef();
			HawkLog.logPrintln("cityDef update by create building, playerId: {}, value: {}", playerId, maxCityDef);
			getPlayerBaseEntity().setCityDefVal(maxCityDef);
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player != null) {
				LogUtil.logCityWallDataChange(player, CityWallChangeType.NEW_BUILDING);
			}
		}

		// 如果是资源建筑
		if (!BuildingCfg.isResBuildingType(buildingEntity.getType())) {
			return;
		}

		// 只有第一个油井建造完成之时就储存了一天的石油产出量（宇航说这是新手功能）
		if (buildingEntity.getType() != BuildingType.OIL_WELL_VALUE) {
			buildingEntity.setLastResCollectTime(HawkApp.getInstance().getCurrentTime());
			return;
		}

		CustomDataEntity customDataEntity = getCustomDataEntity(CustomKeyCfg.getTutorialKey());
		if (customDataEntity == null || String.valueOf(GsConst.NEWBIE_COMPLETE_VALUE).equals(customDataEntity.getArg())) {
			buildingEntity.setLastResCollectTime(HawkApp.getInstance().getCurrentTime());
		} else {
			CustomDataEntity oilTutorialState = getCustomDataEntity(GsConst.TUTORIAL_OIL_KEY);
			if (oilTutorialState == null || HawkOSOperator.isEmptyString(oilTutorialState.getArg())) {
				buildingEntity.setLastResCollectTime(HawkApp.getInstance().getCurrentTime() - GsConst.DAY_MILLI_SECONDS);
				
				if (oilTutorialState == null) {
					oilTutorialState = createCustomDataEntity(GsConst.TUTORIAL_OIL_KEY, 0, "1");
				} else {
					oilTutorialState.setArg("1");
				}
			} else {
				buildingEntity.setLastResCollectTime(HawkApp.getInstance().getCurrentTime());
			}
		}
	}

	/**
	 * 获取城防值上限 除了建筑表本身的配置外，可能还受科技和其他作用号的影响
	 * 
	 * @return
	 */
	public int getRealMaxCityDef() {
		BuildingBaseEntity buildingEntity = getBuildingEntityByType(BuildingType.CITY_WALL);
		if (buildingEntity == null) {
			return 0;
		}

		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		if (buildingCfg == null) {
			return 0;
		}

		int cityDefMax = buildingCfg.getCityDefence() + getEffVal(EffType.WAR_CITYDEF_VAL);
		return cityDefMax;
	}

	/**
	 * 获取陷阱容量
	 * 
	 * @return
	 */
	public int getTrapCapacity() {
		BuildingBaseEntity buildingEntity = getBuildingEntityByType(BuildingType.CITY_WALL);
		if (buildingEntity == null) {
			return 0;
		}
		BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		if (buildingCfg == null) {
			return 0;
		}

		int trapCapacity = buildingCfg.getTrapCapacity() + playerEffect.getEffVal(EffType.WAR_TRAP_LIMIT_VAL);
		return trapCapacity;
	}

	/**
	 * 获取当前已有的陷阱数量
	 * 
	 * @return
	 */
	public int getTrapCount() {
		int count = 0;
		for (ArmyEntity armyEntity : getArmyEntities()) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyEntity.getArmyId());
			if (cfg != null && cfg.isDefWeapon()) {
				count += armyEntity.getFree();
			}
		}

		return count;
	}

	/**
	 * 添加历史充值备份数据
	 * 
	 * @param entity
	 */
	public void addPlayerRechargeBackEntity(RechargeBackEntity entity) {
		getPlayerRechargeBackEntities().add(entity);
	}
	
	/**
	 * 添加历史充值记录
	 * @param entity
	 */
	public void addPlayerRechargeEntity(RechargeEntity entity) {
		getPlayerRechargeEntities().add(entity);
	}
	
	/**
	 * 添加当日充值记录
	 * @param entity
	 */
	public void addPlayerRechargeDailyEntity(RechargeDailyEntity entity) {
		getPlayerRechargeDailyEntities().add(entity);
	}

	/**
	 * 获取充值记录
	 * 
	 * @param billno
	 * @return
	 */
	public RechargeDailyEntity getPlayerRechargeDailyEntity(String billno) {
		//在2024-11-08日0点整之前，读取老数据；之后读取新数据 TODO
		if (HawkTime.getMillisecond() < 1730995200000L) {
			for (RechargeEntity entity : getPlayerRechargeEntities()) {
				if (entity.getBillno().equals(billno)) {
					return entity.toRechargeDailyEntity();
				}
			}
		} else {
			for (RechargeDailyEntity entity : getPlayerRechargeDailyEntities()) {
				if (entity.getBillno().equals(billno)) {
					return entity;
				}
			}
		}

		return null;
	}

	/**
	 * 获取玩家总充值钻石数（充值钻石数=充值金额*10），直充和直购模式的充值都包含在内
	 * @return
	 */
	protected int getRechargeTotal() {
		int count = getPlayerBaseEntity().getSaveAmtTotal();
		// 这里不能取diamonds字段，月卡的diamonds没有记在此处。payMoney的单位是人民币：角，正好对应钻石数
		int payGiftTotal = getPlayerRechargeEntities().parallelStream().filter(e -> e.getType() == RechargeType.GIFT).mapToInt(e -> e.getPayMoney()).sum();
		return count + payGiftTotal;
	}

	/**
	 * 获取指定商品当日购买次数
	 * 
	 * @param rechargeType
	 * @param goodsId
	 * @return
	 */
	public int getRechargeTimesToday(int rechargeType, String goodsId) {
		String platform = this.getPlayerEntity().getPlatform();
		String otherPlatorm = Platform.IOS.strLowerCase().equals(platform) ? Platform.ANDROID.strLowerCase() : Platform.IOS.strLowerCase();
		int count = 0;
		long now = HawkTime.getMillisecond();
		//在2024-11-08日0点整之前，读取老数据；之后读取新数据 TODO
		if (now < 1730995200000L) {
			for (RechargeEntity entity : getPlayerRechargeEntities()) {
				if (entity.getType() != rechargeType || !HawkTime.isSameDay(now, entity.getCreateTime())) {
					continue;
				}
				String goodsCfgId = entity.getGoodsId();
				PayGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, goodsCfgId);
				if (cfg != null && !cfg.getChannelType().equals(platform)) {
					goodsCfgId = PayGiftCfg.getId(cfg.getSaleId(), otherPlatorm);
				}
				
				if (goodsId.equals(goodsCfgId)) {
					count++;
				}
			}
		} else {
			for (RechargeDailyEntity entity : getPlayerRechargeDailyEntities()) {
				if (entity.getType() != rechargeType || !HawkTime.isSameDay(now, entity.getCreateTime())) {
					continue;
				}
				String goodsCfgId = entity.getGoodsId();
				PayGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, goodsCfgId);
				if (cfg != null && !cfg.getChannelType().equals(platform)) {
					goodsCfgId = PayGiftCfg.getId(cfg.getSaleId(), otherPlatorm);
				}
				
				if (goodsId.equals(goodsCfgId)) {
					count++;
				}
			}
		}

		return count;
	}

	/**
	 * 取得扭蛋信息
	 * 
	 * @param gachaType
	 * @return
	 */
	public PlayerGachaEntity getGachaEntityByType(GachaType gachaType) {
		Optional<PlayerGachaEntity> op = getPlayerGachaEntities().stream()
				.filter(o -> o.getGachaType() == gachaType.getNumber())
				.findFirst();

		if (op.isPresent()) {
			return op.get();
		}

		PlayerGachaEntity en = new PlayerGachaEntity();
		en.setPlayerId(playerId);
		en.setGachaType(gachaType.getNumber());

		HawkDBManager.getInstance().create(en);
		getPlayerGachaEntities().add(en);

		return en;

	}

	public List<HeroEntity> getHeroEntityByCfgId(List<Integer> heroIds) {
		List<HeroEntity> result = new ArrayList<>(heroIds.size());
		for (HeroEntity heroEntity : getHeroEntityList()) {
			if (heroIds.contains(heroEntity.getHeroId())) {
				result.add(heroEntity);
			}
		}
		return result;
	}

	public int getOperationCount() {
		return operationCount;
	}

	public void setOperationCount(int operationCount) {
		this.operationCount = operationCount;
	}

	public int getClientServerTimeSub() {
		return clientServerTimeSub;
	}

	public void setClientServerTimeSub(int clientServerTimeSub) {
		this.clientServerTimeSub = clientServerTimeSub;
	}

	public int getHotSaleValue() {
		return hotSaleValue;
	}

	public void setHotSaleValue(int hotSaleValue) {
		this.hotSaleValue = hotSaleValue;
	}

	/**
	 * 保留之前获取im头像的接口，名字不能修改
	 * 其它接口调用平台头像，如果修改过即调用修改过的头像
	 * 如果未修改则返回平台头像
	 */
	public String getPfIcon() {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if(Objects.isNull(player)){
			return "";
		}
		return PlayerImageService.getInstance().getPfIcon(player);
	}
	
	/***
	 * 获取平台头像的接口
	 * 之前的业务逻辑，保留
	 * 如果玩家想将头像改为IM头像，可以获取此接口数据
	 * 
	 * 切记此pfIcon初始化必须为null
	 * 为null的时候做一次redis查询操作当查询之后不管是否查找到了数据都会赋值成一个""保证不会在没有头像的情况频繁访问Redis
	 * @return
	 */
	public String getIMPfIcon(){
		PlayerEntity entity = this.getPlayerEntity();
		if (GlobalData.getInstance().isBanPortraitAccount(entity.getOpenid())) {
			HawkLog.errPrintln("playerData getIMPfIcon failed -> account banPortrait, playerId: {}, openid: {}", entity.getId(), entity.getOpenid());
			return "";
		}
		
		// 平台授权个人信息解除
		if (GlobalData.getInstance().isPfPersonInfoCancel(entity.getOpenid())) {
			HawkLog.errPrintln("playerData getIMPfIcon failed -> PfPersonInfoCancel, playerId: {}, openid: {}", entity.getId(), entity.getOpenid());
			return "";
		}
		
		if (Objects.isNull(pfIcon)) {
			String redisPficon = RedisProxy.getInstance().getPfIcon(entity.getPuid());
			if (!HawkOSOperator.isEmptyString(redisPficon)) {
				pfIcon = IconManager.getInstance().getPficonCrc(redisPficon);
			} else {
				pfIcon = "";
			}
		}

		return pfIcon;
	}

	public int getMaxCityDef() {
		return maxCityDef;
	}

	public void setMaxCityDef(int cityDef) {
		maxCityDef = cityDef;
	}

	/**
	 * 获取被国王禁言状态
	 * 
	 * @return
	 */
	public JSONObject getPresidentSilentInfo() {
		return presidentSilentInfo;
	}

	public void setPresidentSilentInfo(JSONObject presidentSilentInfo) {
		this.presidentSilentInfo = presidentSilentInfo;
	}

	/**
	 * 更新冒泡奖励信息
	 * 
	 * @param type
	 * @param bubbleInfo
	 */
	public void updateBubbleRewardInfo(int type, BubbleRewardInfo bubbleInfo) {
		if (bubbleInfo != null) {
			bubbleRewardInfos.put(type, bubbleInfo);
		}
	}

	/**
	 * 获取冒泡奖励信息
	 * 
	 * @param type
	 * @return
	 */
	public BubbleRewardInfo getBubbleRewardInfo(int type) {
		return bubbleRewardInfos.get(type);
	}

	/**
	 * 获取已拆除的建筑数据
	 * 
	 * @return
	 */
	public Map<String, Integer> getRemoveBuildingExps() {
		if (buildRemoveExps.isEmpty()) {
			Map<String, String> removeBuilds = RedisProxy.getInstance().getAllBuildingRemoveExp(playerId);
			if (removeBuilds == null) {
				return buildRemoveExps;
			}

			for (Entry<String, String> entry : removeBuilds.entrySet()) {
				buildRemoveExps.put(entry.getKey(), Integer.valueOf(entry.getValue()));
			}
		}

		return buildRemoveExps;
	}

	public boolean isLively() {
		Calendar openServer = HawkTime.getCalendar(true);
		openServer.setTimeInMillis(GsApp.getInstance().getServerOpenTime());
		int day = HawkTime.calendarDiff(HawkTime.getCalendar(false), openServer);

		if (day <= 0) {
			return true;
		} else {
			ConstProperty constProperty = ConstProperty.getInstance();
			if (day >= (constProperty.getFriendCycle() - 1)) {
				return Long.bitCount(this.getPlayerEntity().getLivelyMask()) >= constProperty.getFriendCycleCom();
			} else {
				return Long.bitCount(this.getPlayerEntity().getLivelyMask()) * 1.0f / (day + 1) >= constProperty.getFriendActivePer();
			}
		}
	}

	public boolean isRtsComplete(int missionId) {
		PlotBattleEntity entity = this.getPlotBattleEntity();
		if (entity.getLevelId() == 0) {
			return false;
		}

		PlotLevelCfg checkLevelCfg = HawkConfigManager.getInstance().getConfigByKey(PlotLevelCfg.class, missionId);
		PlotLevelCfg curLevelCfg = HawkConfigManager.getInstance().getConfigByKey(PlotLevelCfg.class, entity.getLevelId());
		PlotChapterCfg checkChapter = HawkConfigManager.getInstance().getConfigByKey(PlotChapterCfg.class, checkLevelCfg.getChapterId());
		PlotChapterCfg curChapter = HawkConfigManager.getInstance().getConfigByKey(PlotChapterCfg.class, curLevelCfg.getChapterId());
		if (curChapter.getSequenceNo() < checkChapter.getSequenceNo()) {
			return false;
		} else if (curChapter.getSequenceNo() == checkChapter.getSequenceNo()) {
			if (curLevelCfg.getSequeceNo() < checkLevelCfg.getSequeceNo()) {
				return false;
			} else if (curLevelCfg.getSequeceNo() == checkLevelCfg.getSequeceNo() && entity.getStatus() != PlotBattle.LevelState.CROSSED_VALUE) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 根据giftId获取数据库实体
	 * 
	 * @param giftId
	 * @return
	 */
	public PushGiftEntity getPushGiftEntity() {
		return getDataCache().makesureDate(PlayerDataKey.PushGiftEntity);
	}

	public void addMoneyReissueEntity(MoneyReissueEntity entity) {
		getMoneyReissueEntityList().add(entity);
	}

	public void removeMoneyReissueEntity(MoneyReissueEntity entity) {
		getMoneyReissueEntityList().remove(entity);
	}

	public PlayerData4Activity getPlayerData4Activity() {
		return playerData4Activity;
	}

	public void setPlayerData4Activity(PlayerData4Activity playerData4Activity) {
		this.playerData4Activity = playerData4Activity;
	}

	public boolean checkFlagSet(PlayerFlagPosition position) {
		return this.getFlag(position) == 1;
	}

	public int getFlag(PlayerFlagPosition position) {
		return BitIntUtil.getBitValue(getPlayerBaseEntity().getFlag(), position.getNumber());
	}

	public boolean setFlag(PlayerFlagPosition position, int value) {
		if (value > 0) {
			value = 1;
		}
		int oldValue = getFlag(position);
		if (oldValue == value) {
			return false;
		} else {
			int entityFlag = BitIntUtil.setBitValue(getPlayerBaseEntity().getFlag(), position.getNumber(), value);
			getPlayerBaseEntity().setFlag(entityFlag);
			return true;
		}
	}
	
	/**
	 * 谨慎调用这个接口。 
	 *  
	 * @param pfIcon
	 */
	public void setPfIcon(String pfIcon) {
		this.pfIcon = pfIcon;
	}

	/**
	 * 更新体力购买信息
	 * 
	 * @param time
	 * @param times
	 */
	public synchronized void updateVitBuyTimesInfo(int times) {
		lastBuyVitTime = HawkApp.getInstance().getCurrentTime();
		vitBuyTimes = times;
	}

	/**
	 * 获取当日体力购买次数
	 * 
	 * @return
	 */
	public synchronized int getVitBuyTimesToday() {
		if (!HawkTime.isSameDay(lastBuyVitTime, HawkTime.getMillisecond())) {
			String timeCountInfo = LocalRedis.getInstance().getBuyVitTimes(playerId);
			HawkTuple2<Long, Integer> tuple = GameUtil.spliteTimeAndCount(playerId, timeCountInfo);
			if (tuple.second == -1) {
				LocalRedis.getInstance().updateBuyVitTimes(playerId, 0);
			}
			lastBuyVitTime = tuple.first;
			vitBuyTimes = tuple.second;
			if (lastBuyVitTime <= 0) {
				lastBuyVitTime = HawkApp.getInstance().getCurrentTime();
			}
		}

		return vitBuyTimes;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * vip flag
	 * @return
	 */
	public int getVipFlag() {
		return getPlayerEntity().getVipFlag();
	}
	
	public String getPrimitivePfIcon() {
		return primitivePfIcon;
	}

	public void setPrimitivePfIcon(String primitivePfIcon) {
		this.primitivePfIcon = primitivePfIcon;
	}

	public PlayerDataCache getDataCache() {
		return dataCache;
	}

	/**
	 * 更新玩家坐标
	 * @param x
	 * @param y
	 */
	public void updatePlayerPos(int x, int y) {
		getPlayerEntity().updatePos(x, y);
	}
	
	/**
	 * 获取物品兑换信息
	 * @return
	 */
	public Map<Integer, Integer> getItemExchangeInfo() {
		return itemExchangeInfo;
	}

	public void setDataCache(PlayerDataCache dataCache) {
		this.dataCache = dataCache;
	}
	
	
	public void serialData4Cross() {
		serialEffectData4Cross();
	}
	
	public void serialEffectData4Cross() {
		String key = RedisProxy.PLAYER_EFFECT_CROSS + ":" + playerId;
		Map<Integer, Integer> effectMap = new HashMap<>();
		GuildService guildService = GuildService.getInstance();
		String guildId = guildService.getPlayerGuildId(playerId);
		for (EffType effectType : EffType.values()) {
			
			//公会官职.
			if (!HawkOSOperator.isEmptyString(guildId)) {
				MapUtil.appendIntValue(effectMap, effectType.getNumber(), guildService.getEffectGuildOfficer(playerId, effectType.getNumber()));
			}
			
			//联盟官职
			MapUtil.appendIntValue(effectMap, effectType.getNumber(), PresidentOfficier.getInstance().getEffectOfficer(playerId, effectType.getNumber()));
		}
		
		Map<String, String> redisMap = MapUtil.toStringString(effectMap);
		RedisProxy.getInstance().getRedisSession().hmSet(key, redisMap, CrossActivityService.getInstance().getCrossKeyExpireTime());
		
		// 跨服把官职带过去
		int officerId = GameUtil.getOfficerId(playerId);
		if (officerId != OfficerType.OFFICER_00_VALUE) {
			RedisProxy.getInstance().updatePlayerOfficerId(playerId, officerId);
		}
		//跨服把所有官职都带过去
		Set<Integer> oset = GameUtil.getAllOfficerIdSet(playerId);
		RedisProxy.getInstance().updatePlayerOfficerIdSet(playerId, oset);
	}
	
	public void loadData4Cross() {
		loadEffectData4Cross();
	}
	
	public void loadEffectData4Cross() {
		String key = RedisProxy.PLAYER_EFFECT_CROSS + ":" + playerId;
		Map<String, String> redisMap = RedisProxy.getInstance().getRedisSession().hGetAll(key);
		csEffect = MapUtil.toIntegerInteger(redisMap);
	}
	
	
	
	public int getEffectValueInCross(int effectType) {
		return MapUtil.getIntValue(csEffect, effectType);
	}	
	
	public void checkEffectValueInCross() {
		if (csEffect == null) {
			loadData4Cross();
		}
	}
	
	/**
	 * 获取已解锁的最大兵种等级
	 * 
	 * @return
	 */
	public int getUnlockedSoldierMaxLevel() {
		int level = 0;
		for (ArmyEntity entity : getArmyEntities()) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, entity.getArmyId());
			if (cfg != null && cfg.getLevel() > level) {
				level = cfg.getLevel();
			}
		}
		
		return level;
	}

	/**
	 * 获取要塞作用号
	 */
	@SuppressWarnings("deprecation")
	public int getFortressEffect(int effectType) {
		int effectVal = 0;
		
		if (!GsApp.getInstance().isInitOK()) {
			return effectVal;
		}
		
		if (CrossFortressService.getInstance().getCurrentState() != SuperWeaponPeriod.WARFARE_VALUE) {
			return effectVal;
		}
		
		String ownServerId = GlobalData.getInstance().getMainServerId(getPlayerEntity().getServerId());
		for (IFortress fortress : CrossFortressService.getInstance().getAllFortress()) {
			
			Player fortressLeader = WorldMarchService.getInstance().getFortressLeader(fortress.getPointId());
			if (fortressLeader == null) {
				continue;
			}
			
			String occupyServerId = GlobalData.getInstance().getMainServerId(fortressLeader.getServerId());
			if (!ownServerId.equals(occupyServerId)) {
				continue;
			}
			
			CrossFortressCfg crossFortressCfg = CrossFortressService.getInstance().getCrossFortressCfg(fortress.getPosX(), fortress.getPosY());
			if (crossFortressCfg == null) {
				continue;
			}
			
			effectVal += crossFortressCfg.getEffectVal(effectType);
		}
		
		return effectVal;
	}
	
	/**
	 * 获取玩家成就点
	 */
	public int getAchievePoint() {
		int achievePoints = 0;
		
		for (PlayerAchieveItem item : getPlayerAchieveEntity().getMissionItems()) {
			
			for (int i = 1; i <= item.getState(); i++) {
				// 这里 achieveitem state 表示的意思是已经领取的等级
				PlayerAchieveCfg achieveCfg = AssembleDataManager.getInstance().getPlayerAchieve(item.getCfgId(), i);
				if (achieveCfg == null) {
					continue;
				}
				achievePoints += achieveCfg.getStar();
			}
		}
		
		return achievePoints;
	}

	public long getLastInviteMassTime() {
		return lastInviteMassTime;
	}

	public void setLastInviteMassTime(long lastInviteMassTime) {
		this.lastInviteMassTime = lastInviteMassTime;
	}
	
	/**
	 * 获取铠甲
	 */
	public ArmourEntity getArmourEntity(String id) {
		Optional<ArmourEntity> op = getArmourEntityList().stream()
				.filter(e -> e.getId().equals(id))
				.findAny();
		if (op.isPresent()) {
			return op.get();
		}
		return null;
	}	
	
	/**
	 * 删除铠甲
	 */
	public void removeArmourEntity(ArmourEntity armour) {
		getArmourEntityList().remove(armour);
	}

	/**
	 * 获取套装上的铠甲
	 */
	public List<ArmourEntity> getSuitArmours(int suit) {
		List<ArmourEntity> armours = new ArrayList<>();
		
		List<ArmourEntity> armourEntityList = getArmourEntityList();
		for (ArmourEntity armour : armourEntityList) {
			if (armour.getSuitSet().contains(suit)) {
				armours.add(armour);
			}
		}
		return armours;
	}
	
	/**
	 * 获取铠甲作用号
	 * 
	 * @param suit 套装Id
	 */
	public Map<EffType, Integer> getArmourEffect(int suit) {
		Map<EffType, Integer> effect = new HashMap<>();
		
		try {
			List<ArmourEntity> armours = getSuitArmours(suit);
			int redCount = 0;
			int redlevel = ArmourConstCfg.getInstance().getQuantumRedLevel();
			for(ArmourEntity ar : armours){
				if(ar.getQuantum() >= redlevel){
					redCount++;
				}
			}
			// 每个铠甲的属性
			for (ArmourEntity armour : armours) {
				GameUtil.mergeEffval(effect, GameUtil.getArmourEffect(this, armour,redCount));
			}
			
			// 套装属性
			Map<Integer, List<ArmourEntity>> armourSuitMap = getArmourAttrSuitMap(armours);
			for (Entry<Integer, List<ArmourEntity>> armourSuit : armourSuitMap.entrySet()) {
				GameUtil.mergeEffval(effect, GameUtil.getArmourSuitEffect(this, armourSuit.getKey(), armourSuit.getValue()));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return effect;
	}
	
	/**
	 * 铠甲属性套装
	 */
	public Map<Integer, List<ArmourEntity>> getArmourAttrSuitMap(List<ArmourEntity> armours) {
		Map<Integer, List<ArmourEntity>> map = new HashMap<>();
		for (ArmourEntity armour : armours) {
			ArmourCfg armourCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, armour.getArmourId());
			int armourSuitId = armourCfg.getArmourSuitId();
			
			List<ArmourEntity> list = map.get(armourSuitId);
			if (list == null) {
				list = new ArrayList<>();
				map.put(armourSuitId, list);
			}
			list.add(armour);
		}
		return map;
	}
	
	/**
	 * 获取铠甲战力
	 */
	public int getArmourSuitPower() {
		int power = 0;
		
		try {
			// 当前套装
			int suitId = getPlayerEntity().getArmourSuit();
			
			List<ArmourEntity> armours = getSuitArmours(suitId);
			
			// 铠甲战力
			for (ArmourEntity armour : armours) {
				power += GameUtil.getArmourPower(armour);
			}
			
			// 套装战力
			Map<Integer, List<ArmourEntity>> armourSuitMap = getArmourAttrSuitMap(armours);
			for (Entry<Integer, List<ArmourEntity>> armourSuit : armourSuitMap.entrySet()) {
				power += GameUtil.getArmourSuitPower(this, armourSuit.getValue());
			}	
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return power;
	}
	
	/**
	 * 创建远征科技实体
	 * 
	 * @param scienceId
	 */
	public CrossTechEntity createCrossTechEntity(CrossTechCfg cfg) {
		CrossTechEntity entity = new CrossTechEntity();
		entity.setTechId(cfg.getTechId());
		entity.setPlayerId(playerId);
		entity.setLevel(0);
		if (!HawkDBManager.getInstance().create(entity)) {
			return null;
		}

		addCrossTechEntity(entity);
		return entity;
	}

	/**
	 * 获得远征科技实体
	 * 
	 * @param id
	 *            科技实体Id
	 */
	public CrossTechEntity getCrossTechEntity(String id) {
		for (CrossTechEntity technologyEntity : getCrossTechEntities()) {
			if (technologyEntity.getId().equals(id)) {
				return technologyEntity;
			}
		}
		return null;
	}

	/**
	 * 获得远征科技实体
	 * 
	 * @param techId
	 *            科技Id
	 */
	public CrossTechEntity getCrossTechEntityByTechId(int techId) {
		return getCrossTechEntities().stream()
				.filter(e -> e.getTechId() == techId)
				.findFirst()
				.orElse(null);
	}

	/**
	 * 增加远征科技实体
	 * 
	 * @param entity
	 */
	public void addCrossTechEntity(CrossTechEntity entity) {
		getCrossTechEntities().add(entity);
	}

	/**
	 * 移除远征科技实体
	 * 
	 * @param id
	 */
	public void removeCrossTechEntity(CrossTechEntity entity) {
		getCrossTechEntities().remove(entity);
		entity.delete();
	}

	/**
	 * 获取装备科技实体
	 */
	public EquipResearchEntity getEquipResearchEntity(int researchId) {
		List<EquipResearchEntity> entityList = getEquipResearchEntityList();
		for (EquipResearchEntity entity : entityList) {
			if (entity.getResearchId() == researchId) {
				return entity;
			}
		}
		
		// 校验researchId是否正确
		Set<Integer> rechargeCfgIds = AssembleDataManager.getInstance().getEquipResearchIds();
		if (!rechargeCfgIds.contains(researchId)) {
			return null;
		}
		
		EquipResearchEntity entity = new EquipResearchEntity();
		entity.setPlayerId(getPlayerId());
		entity.setResearchId(researchId);
		entity.setResearchLevel(0);
		if (HawkDBManager.getInstance().create(entity)) {
			getEquipResearchEntityList().add(entity);
		}
		return entity;
	}

	/**
	 * 获取方尖碑任务
	 */
	public ObeliskEntity getObeliskById(String id) {
		for (ObeliskEntity obeliskEntity : getObeliskEntities()) {
			if (id.equals(obeliskEntity.getId())) {
				return obeliskEntity;
			}
		}
		return null;
	}

	/**
	 * 获取方尖碑任务
	 */
	public ObeliskEntity getObeliskByCfgId(int cfgId) {
		for (ObeliskEntity obeliskEntity : getObeliskEntities()) {
			if (cfgId == obeliskEntity.getCfgId()) {
				return obeliskEntity;
			}
		}
		return null;
	}

	/**
	 * 获取方尖碑任务,没有则创建
	 * @param cfgId
	 * @return
	 */
	public synchronized ObeliskEntity getObeliskByCfgIdOrCreate(int cfgId) {
		ObeliskEntity obeliskEntity = getObeliskByCfgId(cfgId);
		if (obeliskEntity == null){
			obeliskEntity = new ObeliskEntity(playerId, cfgId, Obelisk.PBObeliskPlayerState.NOT_OPEN);
			if (!HawkDBManager.getInstance().create(obeliskEntity)) {
				return null;
			}
			addObeliskEntity(obeliskEntity);
		}
		return obeliskEntity;
	}

	/**
	 * 添加方尖碑
	 */
	public void addObeliskEntity(ObeliskEntity obeliskEntity) {
		getObeliskEntities().add(obeliskEntity);
	}
	
	/**
	 * 获取个保法系列开关设定值
	 */
	public List<Integer> getPersonalProtectListVals() {
		String val = getPersonalProtectVals();
		String[] args = val.split(",");
		List<Integer> vals = new ArrayList<Integer>(args.length);
		for (int i = 0; i < args.length; i++) {
			vals.add(Integer.parseInt(args[i]));
		}
		
		return vals;
	}
	
	/**
	 * 获取个保法系列开关设定值
	 */
	public String getPersonalProtectVals() {
		CustomDataEntity customDataEntity = getCustomDataEntity(GsConst.PERSONAL_PROTECT_KEY);
		if (customDataEntity == null) {
			customDataEntity = createCustomDataEntity(GsConst.PERSONAL_PROTECT_KEY, 0, "0");
		}
		
		String[] args = customDataEntity.getArg().split(",");
		
		int switchCount = HawkConfigManager.getInstance().getConfigSize(PrivateSettingOptionCfg.class);
		
		// int型数值32位用低31位来表示开关，最高为代表符号，不用来表示开关
		int switchIntCount = switchCount / 31;
		if (switchCount % 31 > 0) {
			switchIntCount += 1;
		}
		
		// 如果已记录的开关数小于配置的开关数，需要把新添加的开关补上（默认0为开启，表示不受限制）
		if (args.length < switchIntCount) {
			for (int i = args.length + 1; i <= switchIntCount; i++) {
				customDataEntity.setArg(customDataEntity.getArg() + ",0");
			}
		}
		
		return customDataEntity.getArg();
	}
	
	/**
	 * 更新 个保法系列开关设定值
	 * @param switchVals
	 */
	public void updatePersonalProtectVals(List<Integer> switchVals) {
		CustomDataEntity customDataEntity = getCustomDataEntity(GsConst.PERSONAL_PROTECT_KEY);
		StringJoiner sj = new StringJoiner(",");
		for (int i = 0; i < switchVals.size(); i++) {
			sj.add(String.valueOf(switchVals.get(i)));
		}
		
		customDataEntity.setArg(sj.toString());
	}
	
	/**
	 *  获取特定位次的开关
	 * @param switchIndex
	 * @return
	 */
	public int getIndexedProtectSwitchVal(int switchIndex) {
		try {
			List<Integer> switchVals = getPersonalProtectListVals();
			int switchIntCount = switchIndex / 31; // 只有1到31位表示开关， 第32位不用来表示开关
			// 保证switchIntCount是从0开始
			if (switchIndex % 31 == 0) {
				switchIntCount = switchIntCount - 1;
			} 
			
			switchIndex = switchIndex - switchIntCount * 31;  // 31个开关按位凑成一个int型整数
			
			int switchVal = switchVals.get(switchIntCount);
			int locationPositive = 1 << (switchIndex - 1);
			int val = (switchVal & locationPositive) >> (switchIndex - 1);
			return val;
		} catch (Exception e) {
			HawkLog.errPrintln("getIndexedProtectSwitchVal error, playerId: {}, param: {}, msg: {}", 
					getPlayerEntity().getId(), switchIndex, e.getMessage());
		}
		
		return 0;
	}
	
	/**
	 * 计算兵种荣耀等级
	 */
	public int getSoldierStar(int armyId) {
		try {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			BuildingType buildType = BuildingType.valueOf(cfg.getBuilding());
			if (cfg.getBuilding() == BuildingType.PRISM_TOWER_VALUE) {// 如果是尖塔 会有两个
				List<BuildingBaseEntity> towerList = getBuildingListByType(buildType);
				for (BuildingBaseEntity tower : towerList) {
					BuildingCfg towerCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, tower.getBuildingCfgId());
					if (towerCfg.getBattleSoldierId() == armyId) {
						return towerCfg.getHonor();
					}
				}
				return 0;
			}
			BuildingCfg bcfg = getBuildingCfgByType(buildType);
			if (bcfg == null) {
				return 0;
			}
			return bcfg.getHonor();

		} catch (Exception e) {
			HawkException.catchException(e, "armyId = " + armyId);
		}
		return 0;
	}	
	
	/**装扮点总数*/
	public int getSkinPoint(){
		try {
			DressEntity dressEntity = getDressEntity();
			BlockingDeque<DressItem> dressInfos = dressEntity.getDressInfo();
			int skinPoint = 0;
			for (DressItem dressInfo : dressInfos) {
				DressCfg dressCfg = AssembleDataManager.getInstance().getDressCfg(dressInfo.getDressType(), dressInfo.getModelType());
				if(Objects.nonNull(dressCfg)){
					skinPoint += dressCfg.getSkinPoint();
				}
			}
			return skinPoint;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	public StaffOfficerSkillCollection getStaffOffic(){
		if(staffOffic == null){
			staffOffic = StaffOfficerSkillCollection.create(this);
		}
		return staffOffic;
	}
	
	/**
	 * 获取部署的超武信息
	 * @return
	 */
	public PBDeployedSwInfo.Builder getDeployedSwInfo() {
		PBDeployedSwInfo.Builder info = PBDeployedSwInfo.newBuilder();
		try {
			List<PlayerManhattan> list = getManhattanEntityList().stream().map(e -> e.getManhattanObj()).filter(e -> e.isDeployed()).collect(Collectors.toList());
			for (PlayerManhattan manhattan : list) {
				ManhattanSWSkillCfg skillCfg = manhattan.getUnlockedSkillCfg();
				if (manhattan.getSwCfg().getType() == 1) {
					info.setAtkSwId(manhattan.getSWCfgId());
					info.setAtkSwSkillId(skillCfg == null ? 0 : skillCfg.getId());
				} else {
					info.setDefSwId(manhattan.getSWCfgId());
					info.setDefSwSkillId(skillCfg == null ? 0 : skillCfg.getId());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return info;
	}
	
}
