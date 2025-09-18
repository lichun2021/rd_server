package com.hawk.game.lianmengjunyan.player.npc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.annotation.SerializeField;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.entity.PlayerData4Activity;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.LMJYNpcCfg;
import com.hawk.game.config.TalentLevelCfg;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.data.BubbleRewardInfo;
import com.hawk.game.entity.AccumulateOnlineEntity;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.DailyDataEntity;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.EquipEntity;
import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.entity.GuildHospiceEntity;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.MissionEntity;
import com.hawk.game.entity.MoneyReissueEntity;
import com.hawk.game.entity.PayStateEntity;
import com.hawk.game.entity.PlantFactoryEntity;
import com.hawk.game.entity.PlantTechEntity;
import com.hawk.game.entity.PlayerAchieveEntity;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.PlayerGachaEntity;
import com.hawk.game.entity.PlayerGiftEntity;
import com.hawk.game.entity.PlayerGuildGiftEntity;
import com.hawk.game.entity.PlayerMonsterEntity;
import com.hawk.game.entity.PlayerResourceGiftEntity;
import com.hawk.game.entity.PlayerWarCollegeEntity;
import com.hawk.game.entity.PlotBattleEntity;
import com.hawk.game.entity.PushGiftEntity;
import com.hawk.game.entity.QuestionnaireEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.RechargeDailyEntity;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.entity.StatisticsEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.StorehouseBaseEntity;
import com.hawk.game.entity.StorehouseEntity;
import com.hawk.game.entity.StorehouseHelpEntity;
import com.hawk.game.entity.StoryMissionEntity;
import com.hawk.game.entity.SuperSoldierEntity;
import com.hawk.game.entity.TalentEntity;
import com.hawk.game.entity.TavernEntity;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.entity.WharfEntity;
import com.hawk.game.entity.WishingWellEntity;
import com.hawk.game.entity.YuriStrikeEntity;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayerData;
import com.hawk.game.lianmengjunyan.player.npc.cache.LMJYNPCPlayerDataCache;
import com.hawk.game.lianmengjunyan.player.npc.cache.LMJYNPCPlayerDataKey;
import com.hawk.game.module.staffofficer.StaffOfficerSkillCollection;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.player.PowerElectric;
import com.hawk.game.player.cache.PlayerDataCache;
import com.hawk.game.player.equip.CommanderObject;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.GachaType;
import com.hawk.game.protocol.Const.LimitType;
import com.hawk.game.protocol.Player.PlayerFlagPosition;

public class LMJYNPCPlayerData extends ILMJYPlayerData {
	public LMJYNPCPlayerData(ILMJYPlayer parent) {
		super(parent);
	}

	private LMJYNpcCfg npcCfg;
	private LMJYNPCPlayerDataCache npcDataCache;
	private String playerId;

	public static LMJYNPCPlayerData valueOf(LMJYNPCPlayer parent) {
		LMJYNPCPlayerData result = new LMJYNPCPlayerData(parent);
		result.npcCfg = parent.getNpcCfg();
		result.playerId = parent.getId();
		result.npcDataCache = LMJYNPCPlayerDataCache.newCache(parent);

		{// 雷达一个
			BuildingCfg leida = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, 202202);
			BuildingBaseEntity leidaEntity = result.createBuildingEntity(leida, "1", true);
			result.addBuildingEntity(leidaEntity);
			BuildingCfg dashi = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, 201630);
			BuildingBaseEntity dashiEntity = result.createBuildingEntity(dashi, "1", true);
			result.addBuildingEntity(dashiEntity);
		}
		
		result.playerEffect = new LMJYNpcPlayerEffect(result);
		
		return result;

	}
	
	@Override
	public StaffOfficerSkillCollection getStaffOffic(){
		return StaffOfficerSkillCollection.defaultInst;
	}

	@Override
	public List<PlantFactoryEntity> getPlantFactoryEntities() {
		return Collections.emptyList();
	}

	@Override
	public List<PlantTechEntity> getPlantTechEntities() {
		return Collections.emptyList();
	}

	@Override
	public int getAchievePoint() {
		return 0;
	}

	@Override
	public String getPlayerId() {
		return playerId;
	}

	@Override
	public int getConstructionFactoryLevel() {
		return npcCfg.getCityLevel();
	}

	@Override
	public void lockOriginalData() {
	}

	@Override
	public void unLockOriginalData() {
	}

	@Override
	public int getVipFlag() {
		return 0;
	}

	@Override
	public String getPfIcon() {
		return "";
	}

	@Override
	public String getIMPfIcon() {
		return "";
	}

	/**
	 * 获取玩家数据实体
	 */
	@SerializeField
	public PlayerEntity getPlayerEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.PlayerEntity);
	}
	
	@SerializeField
	public List<ArmourEntity> getArmourEntityList() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.ArmourEntities);
	}

	/**
	 * 获取玩家基础数据
	 * 
	 * @return
	 */
	@SerializeField
	public PlayerBaseEntity getPlayerBaseEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.PlayerBaseEntity);
	}

	/**
	 * 获取玩家统计数据实体
	 */
	@SerializeField
	public StatisticsEntity getStatisticsEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.StatisticsEntity);
	}

	/**
	 * 获取支付状态信息
	 */
	@SerializeField
	public PayStateEntity getPayStateEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.PayStateEntity);
	}

	/**
	 * 获取天赋列表
	 */
	@SerializeField
	public List<TalentEntity> getTalentEntities() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.TalentEntities);
	}

	/**
	 * 获取物品列表
	 */
	@SerializeField
	public List<ItemEntity> getItemEntities() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.ItemEntities);
	}

	@SerializeField
	public List<BuildingBaseEntity> getBuildingEntitiesIgnoreStatus() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.BuildingEntities);
	}

	/**
	 * 获得所有科技实体
	 */
	@SerializeField
	public List<TechnologyEntity> getTechnologyEntities() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.TechnologyEntities);
	}

	@SerializeField
	public List<PlayerGachaEntity> getPlayerGachaEntities() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.PlayerGachaEntities);
	}

	/**
	 * 获取队列实体列表
	 */
	@SerializeField
	public List<QueueEntity> getQueueEntities() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.QueueEntities);
	}

	/**
	 * 获得所有军队实体
	 */
	@SerializeField
	public List<ArmyEntity> getArmyEntities() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.ArmyEntities);
	}

	/**
	 * 获取装备列表
	 * 
	 * @return
	 */
	@SerializeField
	public List<EquipEntity> getEquipEntities() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.EquipEntities);
	}

	/**
	 * 获取指挥官实体
	 */
	@SerializeField
	public CommanderEntity getCommanderEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.CommanderEntity);
	}

	/**
	 * 获取玩家状态实体
	 */
	@SerializeField
	public List<StatusDataEntity> getStatusDataEntities() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.StatusDataEntities);
	}

	/**
	 * 获取玩家任务实体
	 */
	@SerializeField
	public List<MissionEntity> getMissionEntities() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.MissionEntities);
	}

	/**
	 * 获取剧情任务实体
	 */
	@SerializeField
	public StoryMissionEntity getStoryMissionEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.StoryMissionEntity);
	}

	/**
	 * 获取玩家成就实体
	 */
	@SerializeField
	public PlayerAchieveEntity getPlayerAchieveEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.PlayerAchieveEntity);
	}

	/**
	 * 获取玩家历史充值记录
	 */
	@SerializeField
	public List<RechargeEntity> getPlayerRechargeEntities() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.PlayerRechargeEntities);
	}

	/**
	 * 玩家调查问卷实体
	 */
	@SerializeField
	public QuestionnaireEntity getQuestionnaireEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.QuestionnaireEntity);
	}

	/**
	 * 玩家野怪实体
	 */
	@SerializeField
	public PlayerMonsterEntity getMonsterEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.MonsterEntity);
	}

	/**
	 * 许愿池信息
	 */
	@SerializeField
	public WishingWellEntity getWishingEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.WishingEntity);
	}

	/**
	 * 酒馆信息
	 */
	@SerializeField
	public TavernEntity getTavernEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.TavernEntity);
	}

	/**
	 * 码头信息
	 */
	@SerializeField
	public WharfEntity getWharfEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.WharfEntity);
	}

	/**
	 * 玩家英雄DB实体
	 */
	@SerializeField
	public List<HeroEntity> getHeroEntityList() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.HeroEntityList);
	}

	/**
	 * 玩家超级兵DB实体
	 */
	@SerializeField
	public List<SuperSoldierEntity> getSuperSoldierEntityList() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.SuperSoldierEntityList);
	}

	/** 联盟礼物 */
	@SerializeField
	public List<PlayerGuildGiftEntity> getGuildGiftEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.GuildGiftEntity);
	}

	/**
	 * 获取用户自定义数据对象
	 */
	@SerializeField
	public List<CustomDataEntity> getCustomDataEntities() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.CustomDataEntities);
	}

	/**
	 * 每日数据实体
	 */
	@SerializeField
	public DailyDataEntity getDailyDataEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.DailyDataEntity);
	}

	@SerializeField
	public PlotBattleEntity getPlotBattleEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.PlotBattleEntity);
	}

	/**
	 * 获取装扮信息
	 * 
	 * @return
	 */
	@SerializeField
	public DressEntity getDressEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.DressEntity);
	}

	/**
	 * 累积在线
	 */
	@SerializeField
	public AccumulateOnlineEntity getAccumulateOnlineEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.AccumulateOnlineEntity);
	}

	/**
	 * 超值礼包
	 */
	@SerializeField
	public PlayerGiftEntity getPlayerGiftEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.PlayerGiftEntity);
	}

	// 货币待补发记录
	@SerializeField
	public List<MoneyReissueEntity> getMoneyReissueEntityList() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.MoneyReissueEntityList);
	}

	/**
	 * 资源礼包
	 */
	@SerializeField
	public PlayerResourceGiftEntity getPlayerResourceGiftEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.PlayerResourceGiftEntity);
	}

	/** 宝藏基础信息 */
	@SerializeField
	public StorehouseBaseEntity getStorehouseBase() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.StorehouseBase);
	}

	/** 已挖宝 */
	@SerializeField
	public List<StorehouseEntity> getStorehouseEntities() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.StorehouseEntities);
	}

	/** 已帮助 */
	@SerializeField
	public List<StorehouseHelpEntity> getStorehouseHelpEntities() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.StorehouseHelpEntities);
	}

	/** 取得尤里来饭 */
	public YuriStrikeEntity getYuriStrikeEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.YuriStrike);
	}

	/** 联盟关怀 */
	public GuildHospiceEntity getGuildHospiceEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.GuildHospice);
	}

	@SerializeField
	public PlayerWarCollegeEntity getPlayerWarCollegeEntity() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.PlayerWarCollegeEntity);
	}
	
	/**
	 * 装备科技db实体
	 * @return
	 */
	@SerializeField
	public List<EquipResearchEntity> getEquipResearchEntityList() {
		return Collections.emptyList();
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// 华丽丽的分割线, 别越界, 上面是DB实体, 需要序列化, 下面是非序列号内存对象
	/////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 获取设置列表
	 */
	public Map<Integer, Integer> getSettingDatas() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.SettingDatas);
	}

	public Set<String> getShieldPlayers() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.ShieldPlayers);
	}

	public Map<String, String> getBanRankInfos() {
		return npcDataCache.makesureDate(LMJYNPCPlayerDataKey.BanRankInfos);
	}

	@Override
	public boolean loadPlayerData(String playerId) {
		// TODO Auto-generated method stub
		return super.loadPlayerData(playerId);
	}

	@Override
	public void loadAll(boolean isNewly) {
	}

	@Override
	public boolean updateCacheExpire(int expireTime) {
		// TODO Auto-generated method stub
		return super.updateCacheExpire(expireTime);
	}

	@Override
	public void addBanRankInfo(String rankType, String banInfo) {
		// TODO Auto-generated method stub
		super.addBanRankInfo(rankType, banInfo);
	}

	@Override
	public String getBanRankInfo(String rankType) {
		// TODO Auto-generated method stub
		return super.getBanRankInfo(rankType);
	}

	@Override
	public void removeBanRankInfo(String... rankTypes) {
		// TODO Auto-generated method stub
		super.removeBanRankInfo(rankTypes);
	}

	@Override
	public boolean assembleData(Player player) {
		// TODO Auto-generated method stub
		return super.assembleData(player);
	}

	@Override
	public void setFromCache(boolean isFromCache) {
		// TODO Auto-generated method stub
		super.setFromCache(isFromCache);
	}

	@Override
	public boolean isFromCache() {
		// TODO Auto-generated method stub
		return super.isFromCache();
	}

	@Override
	public void setSnapshotNextUpdateTime(long snapshotNextUpdateTime) {
		// TODO Auto-generated method stub
		super.setSnapshotNextUpdateTime(snapshotNextUpdateTime);
	}

	@Override
	public boolean isSnapshotNeedUpdateTime() {
		// TODO Auto-generated method stub
		return super.isSnapshotNeedUpdateTime();
	}

	@Override
	public PowerElectric getPowerElectric() {
		return super.getPowerElectric();
	}

	@Override
	public int getEffVal(EffType effType) {
		return getEffVal(effType, null);
	}

	@Override
	public int getEffVal(EffType effType, String targetId) {
		return getPlayerEffect().getEffVal(effType, targetId);
	}

	@Override
	public void addItemEntity(ItemEntity itemEntity) {
		// TODO Auto-generated method stub
		super.addItemEntity(itemEntity);
	}

	@Override
	public ItemEntity getItemById(String id) {
		// TODO Auto-generated method stub
		return super.getItemById(id);
	}

	@Override
	public List<ItemEntity> getItemsByItemId(int itemId) {
		// TODO Auto-generated method stub
		return super.getItemsByItemId(itemId);
	}

	@Override
	public int getItemNumByItemType(int itemType) {
		// TODO Auto-generated method stub
		return super.getItemNumByItemType(itemType);
	}

	@Override
	public int getItemNumByItemId(int itemId) {
		// TODO Auto-generated method stub
		return super.getItemNumByItemId(itemId);
	}

	@Override
	public void addTalentEntity(TalentEntity talentEntity) {
		// TODO Auto-generated method stub
		super.addTalentEntity(talentEntity);
	}

	@Override
	public TalentEntity createTalentEntity(int talentId, int type, int level, TalentLevelCfg cfg) {
		// TODO Auto-generated method stub
		return super.createTalentEntity(talentId, type, level, cfg);
	}

	@Override
	public int getTalentCurrentMaxLvl(int talentId) {
		// TODO Auto-generated method stub
		return super.getTalentCurrentMaxLvl(talentId);
	}

	@Override
	public TalentEntity getTalentByTalentId(int talentId, int talentType) {
		// TODO Auto-generated method stub
		return super.getTalentByTalentId(talentId, talentType);
	}

	@Override
	public TalentEntity getTalentSkill(int skillId) {
		// TODO Auto-generated method stub
		return super.getTalentSkill(skillId);
	}

	@Override
	public List<TalentEntity> getTalentSkills(int skillId) {
		// TODO Auto-generated method stub
		return super.getTalentSkills(skillId);
	}

	@Override
	public long getTalentSkillMaxRefTime(int skillId) {
		// TODO Auto-generated method stub
		return super.getTalentSkillMaxRefTime(skillId);
	}

	@Override
	public List<TalentEntity> getAllTalentSkills() {
		// TODO Auto-generated method stub
		return super.getAllTalentSkills();
	}

	@Override
	public List<TalentEntity> getTalentSkills() {
		// TODO Auto-generated method stub
		return super.getTalentSkills();
	}

	@Override
	public List<Integer> castedTalentSkill() {
		// TODO Auto-generated method stub
		return super.castedTalentSkill();
	}

	@Override
	public List<BuildingBaseEntity> getBuildingEntities() {
		// TODO Auto-generated method stub
		return super.getBuildingEntities();
	}

	@Override
	public void addBuildingEntity(BuildingBaseEntity entity) {
		// TODO Auto-generated method stub
		super.addBuildingEntity(entity);
	}

	@Override
	public void deleteBuildingEntity(BuildingBaseEntity entity) {
		// TODO Auto-generated method stub
		super.deleteBuildingEntity(entity);
	}

	@Override
	public BuildingBaseEntity getBuildingBaseEntity(String id) {
		// TODO Auto-generated method stub
		return super.getBuildingBaseEntity(id);
	}

	@Override
	public BuildingBaseEntity getBuildingEntityIgnoreStatus(String id) {
		// TODO Auto-generated method stub
		return super.getBuildingEntityIgnoreStatus(id);
	}

	@Override
	public int getBuildingLevel(String id) {
		// TODO Auto-generated method stub
		return super.getBuildingLevel(id);
	}

	@Override
	public List<BuildingBaseEntity> getBuildingListByType(BuildingType type) {
		// TODO Auto-generated method stub
		return super.getBuildingListByType(type);
	}

	@Override
	public int getBuildCount(BuildingType type) {
		// TODO Auto-generated method stub
		return super.getBuildCount(type);
	}

	@Override
	public int getResBuildCount() {
		// TODO Auto-generated method stub
		return super.getResBuildCount();
	}

	@Override
	public BuildingBaseEntity getBuildingEntityByType(BuildingType type) {
		// TODO Auto-generated method stub
		return super.getBuildingEntityByType(type);
	}

	@Override
	public int getBuildingResOutputByType(int buildingType) {
		// TODO Auto-generated method stub
		return super.getBuildingResOutputByType(buildingType);
	}

	@Override
	public List<BuildingBaseEntity> getBuildingListByLimitTypeIgnoreStatus(LimitType... limitTypes) {
		// TODO Auto-generated method stub
		return super.getBuildingListByLimitTypeIgnoreStatus(limitTypes);
	}

	@Override
	public List<BuildingBaseEntity> getBuildingListByLimitType(LimitType... limitTypes) {
		// TODO Auto-generated method stub
		return super.getBuildingListByLimitType(limitTypes);
	}

	@Override
	public List<BuildingBaseEntity> getBuildingListByCfgId(int buildingCfgId) {
		// TODO Auto-generated method stub
		return super.getBuildingListByCfgId(buildingCfgId);
	}

	@Override
	public int getBuildingMaxLevel(int type) {
		// TODO Auto-generated method stub
		return super.getBuildingMaxLevel(type);
	}

	@Override
	public BuildingCfg getBuildingCfgByType(BuildingType buildingType) {
		// TODO Auto-generated method stub
		return super.getBuildingCfgByType(buildingType);
	}

	@Override
	public int getTradeTaxRate() {
		// TODO Auto-generated method stub
		return super.getTradeTaxRate();
	}

	@Override
	public BuildingBaseEntity getConstructionFactory() {
		// TODO Auto-generated method stub
		return super.getConstructionFactory();
	}

	@Override
	public void addQueueEntity(QueueEntity queueEntity) {
		// TODO Auto-generated method stub
		super.addQueueEntity(queueEntity);
	}

	@Override
	public QueueEntity getQueueEntity(String id) {
		// TODO Auto-generated method stub
		return super.getQueueEntity(id);
	}

	@Override
	public List<QueueEntity> getBusyCommonQueue(int queueType) {
		// TODO Auto-generated method stub
		return super.getBusyCommonQueue(queueType);
	}

	@Override
	public QueueEntity getQueueByBuildingType(int buildingType) {
		// TODO Auto-generated method stub
		return super.getQueueByBuildingType(buildingType);
	}

	@Override
	public boolean hasBusyCommonQueue(List<Integer> typeList) {
		// TODO Auto-generated method stub
		return super.hasBusyCommonQueue(typeList);
	}

	@Override
	public QueueEntity getPaidQueue() {
		// TODO Auto-generated method stub
		return super.getPaidQueue();
	}

	@Override
	public QueueEntity getFreeQueue(int queueType) {
		// TODO Auto-generated method stub
		return super.getFreeQueue(queueType);
	}

	@Override
	public QueueEntity getQueueEntityByItemId(String id) {
		// TODO Auto-generated method stub
		return super.getQueueEntityByItemId(id);
	}

	@Override
	public QueueEntity getQueueEntity(String id, String itemId) {
		// TODO Auto-generated method stub
		return super.getQueueEntity(id, itemId);
	}

	@Override
	public Map<String, QueueEntity> getQueueEntitiesByType(int type) {
		// TODO Auto-generated method stub
		return super.getQueueEntitiesByType(type);
	}

	@Override
	public TechnologyEntity createTechnologyEntity(TechnologyCfg cfg) {
		// TODO Auto-generated method stub
		return super.createTechnologyEntity(cfg);
	}

	@Override
	public TechnologyEntity getTechnologyEntity(String id) {
		// TODO Auto-generated method stub
		return super.getTechnologyEntity(id);
	}

	@Override
	public TechnologyEntity getTechEntityByTechId(int techId) {
		// TODO Auto-generated method stub
		return super.getTechEntityByTechId(techId);
	}

	@Override
	public void addTechnologyEntity(TechnologyEntity entity) {
		// TODO Auto-generated method stub
		super.addTechnologyEntity(entity);
	}

	@Override
	public void removeTechnologyEntity(TechnologyEntity entity) {
		// TODO Auto-generated method stub
		super.removeTechnologyEntity(entity);
	}

	@Override
	public ArmyEntity getArmyEntity(int armyId) {
		// TODO Auto-generated method stub
		return super.getArmyEntity(armyId);
	}

	@Override
	public void addArmyEntity(ArmyEntity armyEntity) {
		// TODO Auto-generated method stub
		super.addArmyEntity(armyEntity);
	}

	@Override
	public int getArmyCount() {
		// TODO Auto-generated method stub
		return super.getArmyCount();
	}

	@Override
	public void addEquipEntities(Collection<? extends EquipEntity> equipEntities) {
		// TODO Auto-generated method stub
		super.addEquipEntities(equipEntities);
	}

	@Override
	public void removeEquipEntity(EquipEntity equipEntity) {
		// TODO Auto-generated method stub
		super.removeEquipEntity(equipEntity);
	}

	@Override
	public EquipEntity getEquipEntity(String equipId) {
		// TODO Auto-generated method stub
		return super.getEquipEntity(equipId);
	}

	@Override
	public CommanderObject getCommanderObject() {
		// TODO Auto-generated method stub
		return super.getCommanderObject();
	}

	@Override
	public MissionEntity getMissionById(String uuid) {
		// TODO Auto-generated method stub
		return super.getMissionById(uuid);
	}

	@Override
	public List<MissionEntity> getOpenedMissions() {
		// TODO Auto-generated method stub
		return super.getOpenedMissions();
	}

	@Override
	public MissionEntity getMissionByTypeId(int typeId) {
		// TODO Auto-generated method stub
		return super.getMissionByTypeId(typeId);
	}

	@Override
	public StatusDataEntity getStatusById(int statusId) {
		// TODO Auto-generated method stub
		return super.getStatusById(statusId);
	}

	@Override
	public StatusDataEntity getStatusById(int statusId, String targetId) {
		// TODO Auto-generated method stub
		return super.getStatusById(statusId, targetId);
	}

	@Override
	public List<StatusDataEntity> getStatusListById(int statusId) {
		// TODO Auto-generated method stub
		return super.getStatusListById(statusId);
	}

	@Override
	public StatusDataEntity getStateById(int stateId, int stateType) {
		// TODO Auto-generated method stub
		return super.getStateById(stateId, stateType);
	}

	@Override
	public PlayerDataCache getDataCache() {
		// TODO Auto-generated method stub
		return super.getDataCache();
	}

	@Override
	public List<String> getBusyBuildingUuid() {
		// TODO Auto-generated method stub
		return super.getBusyBuildingUuid();
	}

	@Override
	public int getBuildingNumLimit(int limitType) {
		// TODO Auto-generated method stub
		return super.getBuildingNumLimit(limitType);
	}

	@Override
	public long getMaxStoreOil() {
		// TODO Auto-generated method stub
		return super.getMaxStoreOil();
	}

	@Override
	public void addOilMaxStore(long addStoreOil) {
		// TODO Auto-generated method stub
		super.addOilMaxStore(addStoreOil);
	}

	@Override
	public long getMaxStoreSteel() {
		// TODO Auto-generated method stub
		return super.getMaxStoreSteel();
	}

	@Override
	public void addSteelMaxStore(long addStoreSteel) {
		// TODO Auto-generated method stub
		super.addSteelMaxStore(addStoreSteel);
	}

	@Override
	public long getMaxStoreRare() {
		// TODO Auto-generated method stub
		return super.getMaxStoreRare();
	}

	@Override
	public void addRareMaxStore(long addStoreRare) {
		// TODO Auto-generated method stub
		super.addRareMaxStore(addStoreRare);
	}

	@Override
	public long getMaxStoreOre() {
		// TODO Auto-generated method stub
		return super.getMaxStoreOre();
	}

	@Override
	public void addOreMaxStore(long addStoreOre) {
		// TODO Auto-generated method stub
		super.addOreMaxStore(addStoreOre);
	}

	@Override
	public long getOilOutputPerHour() {
		// TODO Auto-generated method stub
		return super.getOilOutputPerHour();
	}

	@Override
	public void addOilOutputPerHour(long addOilOutputPerHour) {
		// TODO Auto-generated method stub
		super.addOilOutputPerHour(addOilOutputPerHour);
	}

	@Override
	public long getSteelOutputPerHour() {
		// TODO Auto-generated method stub
		return super.getSteelOutputPerHour();
	}

	@Override
	public void addSteelOutputPerHour(long addSteelOutputPerHour) {
		// TODO Auto-generated method stub
		super.addSteelOutputPerHour(addSteelOutputPerHour);
	}

	@Override
	public long getRareOutputPerHour() {
		// TODO Auto-generated method stub
		return super.getRareOutputPerHour();
	}

	@Override
	public void addRareOutputPerHour(long addRareOutputPerHour) {
		// TODO Auto-generated method stub
		super.addRareOutputPerHour(addRareOutputPerHour);
	}

	@Override
	public long getOreOutputPerHour() {
		// TODO Auto-generated method stub
		return super.getOreOutputPerHour();
	}

	@Override
	public void addOreOutputPerHour(long addOreOutputPerHour) {
		// TODO Auto-generated method stub
		super.addOreOutputPerHour(addOreOutputPerHour);
	}

	@Override
	public boolean getVipActivated() {
		// TODO Auto-generated method stub
		return super.getVipActivated();
	}

	@Override
	public long getBuffEndTime(int buffId) {
		// TODO Auto-generated method stub
		return super.getBuffEndTime(buffId);
	}

	@Override
	protected StatusDataEntity removeCityShield() {
		// TODO Auto-generated method stub
		return super.removeCityShield();
	}

	@Override
	public long getCityShieldTime() {
		// TODO Auto-generated method stub
		return super.getCityShieldTime();
	}

	@Override
	protected StatusDataEntity addStatusBuff(int buffId) {
		// TODO Auto-generated method stub
		return super.addStatusBuff(buffId);
	}

	@Override
	protected StatusDataEntity addStatusBuff(int buffId, String targetId) {
		// TODO Auto-generated method stub
		return super.addStatusBuff(buffId, targetId);
	}

	@Override
	protected StatusDataEntity addStatusBuff(int buffId, String targetId, long endTime) {
		// TODO Auto-generated method stub
		return super.addStatusBuff(buffId, targetId, endTime);
	}

	@Override
	public void removeQueue(QueueEntity queueEntity) {
		// TODO Auto-generated method stub
		super.removeQueue(queueEntity);
	}

	@Override
	public void removeBuilding(BuildingBaseEntity building) {
		// TODO Auto-generated method stub
		super.removeBuilding(building);
	}

	@Override
	public long getResourceOutputRate(int resType) {
		// TODO Auto-generated method stub
		return super.getResourceOutputRate(resType);
	}

	@Override
	public CustomDataEntity getCustomDataEntity(String key) {
		// TODO Auto-generated method stub
		return super.getCustomDataEntity(key);
	}

	@Override
	public CustomDataEntity createCustomDataEntity(String type, int value, String arg) {
		// TODO Auto-generated method stub
		return super.createCustomDataEntity(type, value, arg);
	}

	@Override
	public BuildingBaseEntity createBuildingEntity(BuildingCfg buildingCfg, String index, boolean immediate) {
		// 入库
		BuildingBaseEntity buildingEntity = new BuildingBaseEntity();
		buildingEntity.setId(HawkOSOperator.randomUUID());
		buildingEntity.setBuildingCfgId(buildingCfg.getId());
		buildingEntity.setPlayerId(playerId);
		buildingEntity.setType(buildingCfg.getBuildType());
		buildingEntity.setBuildIndex(index);
		buildingEntity.setStatus(BuildingStatus.COMMON_VALUE);
		buildingEntity.setResUpdateTime(HawkApp.getInstance().getCurrentTime());
		buildingEntity.setPersistable(false);

		return buildingEntity;
	}

	@Override
	public void refreshNewBuilding(BuildingBaseEntity buildingEntity) {
		// TODO Auto-generated method stub
		super.refreshNewBuilding(buildingEntity);
	}

	@Override
	public int getRealMaxCityDef() {
		// TODO Auto-generated method stub
		return super.getRealMaxCityDef();
	}

	@Override
	public int getTrapCapacity() {
		// TODO Auto-generated method stub
		return super.getTrapCapacity();
	}

	@Override
	public int getTrapCount() {
		// TODO Auto-generated method stub
		return super.getTrapCount();
	}

	@Override
	public void addPlayerRechargeEntity(RechargeEntity entity) {
		// TODO Auto-generated method stub
		super.addPlayerRechargeEntity(entity);
	}
	
	@Override
	public void addPlayerRechargeDailyEntity(RechargeDailyEntity entity) {
		super.addPlayerRechargeDailyEntity(entity);
	}

	@Override
	public RechargeDailyEntity getPlayerRechargeDailyEntity(String billno) {
		return super.getPlayerRechargeDailyEntity(billno);
	}

	@Override
	public int getRechargeTimesToday(int rechargeType, String goodsId) {
		// TODO Auto-generated method stub
		return super.getRechargeTimesToday(rechargeType, goodsId);
	}

	@Override
	public PlayerGachaEntity getGachaEntityByType(GachaType gachaType) {
		// TODO Auto-generated method stub
		return super.getGachaEntityByType(gachaType);
	}

	@Override
	public List<HeroEntity> getHeroEntityByCfgId(List<Integer> heroIds) {
		// TODO Auto-generated method stub
		return super.getHeroEntityByCfgId(heroIds);
	}

	@Override
	public int getOperationCount() {
		// TODO Auto-generated method stub
		return super.getOperationCount();
	}

	@Override
	public void setOperationCount(int operationCount) {
		// TODO Auto-generated method stub
		super.setOperationCount(operationCount);
	}

	@Override
	public int getClientServerTimeSub() {
		// TODO Auto-generated method stub
		return super.getClientServerTimeSub();
	}

	@Override
	public void setClientServerTimeSub(int clientServerTimeSub) {
		// TODO Auto-generated method stub
		super.setClientServerTimeSub(clientServerTimeSub);
	}

	@Override
	public int getHotSaleValue() {
		// TODO Auto-generated method stub
		return super.getHotSaleValue();
	}

	@Override
	public void setHotSaleValue(int hotSaleValue) {
		// TODO Auto-generated method stub
		super.setHotSaleValue(hotSaleValue);
	}

	@Override
	public int getMaxCityDef() {
		// TODO Auto-generated method stub
		return super.getMaxCityDef();
	}

	@Override
	public void setMaxCityDef(int cityDef) {
		// TODO Auto-generated method stub
		super.setMaxCityDef(cityDef);
	}

	@Override
	public JSONObject getPresidentSilentInfo() {
		// TODO Auto-generated method stub
		return super.getPresidentSilentInfo();
	}

	@Override
	public void setPresidentSilentInfo(JSONObject presidentSilentInfo) {
		// TODO Auto-generated method stub
		super.setPresidentSilentInfo(presidentSilentInfo);
	}

	@Override
	public void updateBubbleRewardInfo(int type, BubbleRewardInfo bubbleInfo) {
		// TODO Auto-generated method stub
		super.updateBubbleRewardInfo(type, bubbleInfo);
	}

	@Override
	public BubbleRewardInfo getBubbleRewardInfo(int type) {
		// TODO Auto-generated method stub
		return super.getBubbleRewardInfo(type);
	}

	@Override
	public Map<String, Integer> getRemoveBuildingExps() {
		// TODO Auto-generated method stub
		return super.getRemoveBuildingExps();
	}

	@Override
	public boolean isLively() {
		// TODO Auto-generated method stub
		return super.isLively();
	}

	@Override
	public boolean isRtsComplete(int missionId) {
		// TODO Auto-generated method stub
		return super.isRtsComplete(missionId);
	}

	@Override
	public PushGiftEntity getPushGiftEntity() {
		// TODO Auto-generated method stub
		return super.getPushGiftEntity();
	}

	@Override
	public void addMoneyReissueEntity(MoneyReissueEntity entity) {
		// TODO Auto-generated method stub
		super.addMoneyReissueEntity(entity);
	}

	@Override
	public void removeMoneyReissueEntity(MoneyReissueEntity entity) {
		// TODO Auto-generated method stub
		super.removeMoneyReissueEntity(entity);
	}

	@Override
	public PlayerData4Activity getPlayerData4Activity() {
		// TODO Auto-generated method stub
		return super.getPlayerData4Activity();
	}

	@Override
	public void setPlayerData4Activity(PlayerData4Activity playerData4Activity) {
		// TODO Auto-generated method stub
		super.setPlayerData4Activity(playerData4Activity);
	}

	@Override
	public boolean checkFlagSet(PlayerFlagPosition position) {
		// TODO Auto-generated method stub
		return super.checkFlagSet(position);
	}

	@Override
	public int getFlag(PlayerFlagPosition position) {
		// TODO Auto-generated method stub
		return super.getFlag(position);
	}

	@Override
	public boolean setFlag(PlayerFlagPosition position, int value) {
		// TODO Auto-generated method stub
		return super.setFlag(position, value);
	}

	@Override
	public void setPfIcon(String pfIcon) {
		// TODO Auto-generated method stub
		super.setPfIcon(pfIcon);
	}

	@Override
	public synchronized void updateVitBuyTimesInfo(int times) {
		// TODO Auto-generated method stub
		super.updateVitBuyTimesInfo(times);
	}

	@Override
	public synchronized int getVitBuyTimesToday() {
		// TODO Auto-generated method stub
		return super.getVitBuyTimesToday();
	}

	@Override
	public void setPlayerId(String playerId) {
		// TODO Auto-generated method stub
		super.setPlayerId(playerId);
	}

	@Override
	public String getPrimitivePfIcon() {
		// TODO Auto-generated method stub
		return super.getPrimitivePfIcon();
	}

	@Override
	public void setPrimitivePfIcon(String primitivePfIcon) {
		// TODO Auto-generated method stub
		super.setPrimitivePfIcon(primitivePfIcon);
	}

	@Override
	public PlayerEffect getPlayerEffect() {
		return playerEffect;
	}

	public LMJYNpcCfg getNpcCfg() {
		return npcCfg;
	}
	
	/**
	 * 获取个保法系列开关设定值
	 */
	@Override
	public List<Integer> getPersonalProtectListVals() {
		return Collections.emptyList();
	}
}
