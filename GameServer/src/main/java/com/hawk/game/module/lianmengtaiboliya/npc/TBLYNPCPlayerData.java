package com.hawk.game.module.lianmengtaiboliya.npc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.annotation.SerializeField;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.entity.PlayerData4Activity;
import com.hawk.game.battle.BattleService;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.CrossTechCfg;
import com.hawk.game.config.TalentLevelCfg;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.data.BubbleRewardInfo;
import com.hawk.game.entity.AccumulateOnlineEntity;
import com.hawk.game.entity.AgencyEntity;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.entity.CrossTechEntity;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.DailyDataEntity;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.EquipEntity;
import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.entity.GuildHospiceEntity;
import com.hawk.game.entity.HeroArchivesEntity;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.LaboratoryEntity;
import com.hawk.game.entity.LifetimeCardEntity;
import com.hawk.game.entity.ManhattanEntity;
import com.hawk.game.entity.MissionEntity;
import com.hawk.game.entity.MoneyReissueEntity;
import com.hawk.game.entity.NationBuildQuestEntity;
import com.hawk.game.entity.NationMissionEntity;
import com.hawk.game.entity.ObeliskEntity;
import com.hawk.game.entity.PayStateEntity;
import com.hawk.game.entity.PlantFactoryEntity;
import com.hawk.game.entity.PlantTechEntity;
import com.hawk.game.entity.PlayerAchieveEntity;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.entity.PlayerDailyGiftBuyEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.PlayerGachaEntity;
import com.hawk.game.entity.PlayerGhostTowerEntity;
import com.hawk.game.entity.PlayerGiftEntity;
import com.hawk.game.entity.PlayerGuildGiftEntity;
import com.hawk.game.entity.PlayerMonsterEntity;
import com.hawk.game.entity.PlayerOtherEntity;
import com.hawk.game.entity.PlayerResourceGiftEntity;
import com.hawk.game.entity.PlayerShopEntity;
import com.hawk.game.entity.PlayerWarCollegeEntity;
import com.hawk.game.entity.PlayerXQHXEntity;
import com.hawk.game.entity.PlayerXQHXTalentEntity;
import com.hawk.game.entity.PlotBattleEntity;
import com.hawk.game.entity.PushGiftEntity;
import com.hawk.game.entity.QuestionnaireEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.RechargeBackEntity;
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
import com.hawk.game.module.college.entity.CollegeMemberEntity;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayerData;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayerEffect;
import com.hawk.game.module.lianmengyqzz.march.entitiy.PlayerYQZZEntity;
import com.hawk.game.module.mechacore.PlayerMechaCore;
import com.hawk.game.module.mechacore.entity.MechaCoreEntity;
import com.hawk.game.module.mechacore.entity.MechaCoreModuleEntity;
import com.hawk.game.module.nationMilitary.entity.NationMilitaryEntity;
import com.hawk.game.module.plantsoldier.advance.PlantSoldierAdvanceEntity;
import com.hawk.game.module.plantsoldier.science.PlantScienceEntity;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchoolEntity;
import com.hawk.game.module.staffofficer.StaffOfficerSkillCollection;
import com.hawk.game.module.toucai.entity.MedalEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PowerElectric;
import com.hawk.game.player.cache.PlayerDataCache;
import com.hawk.game.player.equip.CommanderObject;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.GachaType;
import com.hawk.game.protocol.Const.LimitType;
import com.hawk.game.protocol.Dress.PlayerDressPlayerInfo;
import com.hawk.game.protocol.Manhattan.PBDeployedSwInfo.Builder;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.Player.PlayerFlagPosition;

public class TBLYNPCPlayerData extends ITBLYPlayerData {
	private TBLYNPCPlayerEffect playerEffect;
	private MechaCoreEntity mechaCoreEntity;

	private TBLYNPCPlayerData(ITBLYPlayer parent) {
		super(parent);
	}

	@Override
	public TBLYNpcPlayer getParent() {
		return (TBLYNpcPlayer) super.getParent();
	}

	public static TBLYNPCPlayerData valueOf(ITBLYPlayer player) {
		TBLYNPCPlayerData result = new TBLYNPCPlayerData(player);
		result.playerId = player.getId();

		// buff
		// result.statusDataEntities = new ArrayList<>();

		// effect 对象
		result.playerEffect = new TBLYNPCPlayerEffect(result);
		result.playerEffect.setParent(player);

		MechaCoreEntity mechaCoreEntity = new MechaCoreEntity();
		mechaCoreEntity.setPlayerId(player.getId());
		mechaCoreEntity.setWorkSuit(MechaCoreSuitType.MECHA_ONE_VALUE);
		PlayerMechaCore.create(mechaCoreEntity);
		result.mechaCoreEntity = mechaCoreEntity;
		return result;
	}

	@Override
	public ITBLYPlayerEffect getPlayerEffect() {
		return playerEffect;
	}

	@Override
	public MechaCoreEntity getMechaCoreEntity() {
		return mechaCoreEntity;
	}

	@Override
	public StaffOfficerSkillCollection getStaffOffic() {
		return StaffOfficerSkillCollection.defaultInst;
	}

	/**
	 * 获得所有科技实体
	 */
	@Override
	public List<TechnologyEntity> getTechnologyEntities() {
		return Collections.emptyList();
	}

	/**
	 * 获取天赋列表
	 */
	@Override
	public List<TalentEntity> getTalentEntities() {
		return Collections.emptyList();
	}

	@Override
	public PlayerEntity getPlayerEntity() {
		PlayerEntity result = new PlayerEntity();
		result.setPersistable(false);
		result.setId(BattleService.NPC_ID);
		return result;
	}

	@Override
	public List<StatusDataEntity> getStatusDataEntities() {
		return Collections.emptyList();
	}

	@SerializeField
	public List<ArmourEntity> getArmourEntityList() {
		return Collections.emptyList();
	}

	@SerializeField
	public List<EquipResearchEntity> getEquipResearchEntityList() {
		return Collections.emptyList();
	}

	@Override
	public PlayerBaseEntity getPlayerBaseEntity() {
		// TODO Auto-generated method stub
		return super.getPlayerBaseEntity();
	}

	@Override
	public StatisticsEntity getStatisticsEntity() {
		// TODO Auto-generated method stub
		return super.getStatisticsEntity();
	}

	@Override
	public PayStateEntity getPayStateEntity() {
		// TODO Auto-generated method stub
		return super.getPayStateEntity();
	}

	@Override
	public List<ItemEntity> getItemEntities() {
		// TODO Auto-generated method stub
		return super.getItemEntities();
	}

	@Override
	public List<BuildingBaseEntity> getBuildingEntitiesIgnoreStatus() {
		// TODO Auto-generated method stub
		return super.getBuildingEntitiesIgnoreStatus();
	}

	@Override
	public List<PlayerDressPlayerInfo> getDressAskEntities() {
		// TODO Auto-generated method stub
		return super.getDressAskEntities();
	}

	@Override
	public List<PlayerDressPlayerInfo> getDressSendEntities() {
		// TODO Auto-generated method stub
		return super.getDressSendEntities();
	}

	@Override
	public List<CrossTechEntity> getCrossTechEntities() {
		// TODO Auto-generated method stub
		return super.getCrossTechEntities();
	}

	@Override
	public List<PlayerGachaEntity> getPlayerGachaEntities() {
		// TODO Auto-generated method stub
		return super.getPlayerGachaEntities();
	}

	@Override
	public List<QueueEntity> getQueueEntities() {
		// TODO Auto-generated method stub
		return super.getQueueEntities();
	}

	@Override
	public List<ArmyEntity> getArmyEntities() {
		// TODO Auto-generated method stub
		return super.getArmyEntities();
	}

	@Override
	public List<EquipEntity> getEquipEntities() {
		// TODO Auto-generated method stub
		return super.getEquipEntities();
	}

	@Override
	public CommanderEntity getCommanderEntity() {
		// TODO Auto-generated method stub
		return super.getCommanderEntity();
	}

	@Override
	public List<MissionEntity> getMissionEntities() {
		// TODO Auto-generated method stub
		return super.getMissionEntities();
	}

	@Override
	public StoryMissionEntity getStoryMissionEntity() {
		// TODO Auto-generated method stub
		return super.getStoryMissionEntity();
	}

	@Override
	public NationMissionEntity getNationMissionEntity() {
		// TODO Auto-generated method stub
		return super.getNationMissionEntity();
	}

	@Override
	public PlayerAchieveEntity getPlayerAchieveEntity() {
		// TODO Auto-generated method stub
		return super.getPlayerAchieveEntity();
	}

	@Override
	public List<RechargeEntity> getPlayerRechargeEntities() {
		// TODO Auto-generated method stub
		return super.getPlayerRechargeEntities();
	}

	@Override
	public List<RechargeDailyEntity> getPlayerRechargeDailyEntities() {
		// TODO Auto-generated method stub
		return super.getPlayerRechargeDailyEntities();
	}

	@Override
	public List<RechargeBackEntity> getPlayerRechargeBackEntities() {
		// TODO Auto-generated method stub
		return super.getPlayerRechargeBackEntities();
	}

	@Override
	public QuestionnaireEntity getQuestionnaireEntity() {
		// TODO Auto-generated method stub
		return super.getQuestionnaireEntity();
	}

	@Override
	public PlayerMonsterEntity getMonsterEntity() {
		// TODO Auto-generated method stub
		return super.getMonsterEntity();
	}

	@Override
	public WishingWellEntity getWishingEntity() {
		// TODO Auto-generated method stub
		return super.getWishingEntity();
	}

	@Override
	public TavernEntity getTavernEntity() {
		// TODO Auto-generated method stub
		return super.getTavernEntity();
	}

	@Override
	public WharfEntity getWharfEntity() {
		// TODO Auto-generated method stub
		return super.getWharfEntity();
	}

	@Override
	public List<HeroEntity> getHeroEntityList() {
		// TODO Auto-generated method stub
		return super.getHeroEntityList();
	}

	@Override
	public List<PlayerShopEntity> getShopEntityList() {
		// TODO Auto-generated method stub
		return super.getShopEntityList();
	}

	@Override
	public List<PlayerXQHXTalentEntity> getXQHXTalentEntityList() {
		// TODO Auto-generated method stub
		return super.getXQHXTalentEntityList();
	}

	@Override
	public PlayerXQHXEntity getPlayerXQHXEntity() {
		// TODO Auto-generated method stub
		return super.getPlayerXQHXEntity();
	}

	@Override
	public PlantSoldierSchoolEntity getPlantSoldierSchoolEntity() {
		// TODO Auto-generated method stub
		return super.getPlantSoldierSchoolEntity();
	}

	@Override
	public PlantSoldierAdvanceEntity getPlantSoldierAdvanceEntity() {
		// TODO Auto-generated method stub
		return super.getPlantSoldierAdvanceEntity();
	}

	@Override
	public List<LaboratoryEntity> getLaboratoryEntityList() {
		// TODO Auto-generated method stub
		return super.getLaboratoryEntityList();
	}

	@Override
	public List<SuperSoldierEntity> getSuperSoldierEntityList() {
		// TODO Auto-generated method stub
		return super.getSuperSoldierEntityList();
	}

	@Override
	public List<ManhattanEntity> getManhattanEntityList() {
		// TODO Auto-generated method stub
		return super.getManhattanEntityList();
	}

	@Override
	public List<MechaCoreModuleEntity> getMechaCoreModuleEntityList() {
		// TODO Auto-generated method stub
		return super.getMechaCoreModuleEntityList();
	}

	@Override
	public List<PlayerGuildGiftEntity> getGuildGiftEntity() {
		// TODO Auto-generated method stub
		return super.getGuildGiftEntity();
	}

	@Override
	public List<CustomDataEntity> getCustomDataEntities() {
		// TODO Auto-generated method stub
		return super.getCustomDataEntities();
	}

	@Override
	public DailyDataEntity getDailyDataEntity() {
		// TODO Auto-generated method stub
		return super.getDailyDataEntity();
	}

	@Override
	public PlotBattleEntity getPlotBattleEntity() {
		// TODO Auto-generated method stub
		return super.getPlotBattleEntity();
	}

	@Override
	public DressEntity getDressEntity() {
		// TODO Auto-generated method stub
		return super.getDressEntity();
	}

	@Override
	public AccumulateOnlineEntity getAccumulateOnlineEntity() {
		// TODO Auto-generated method stub
		return super.getAccumulateOnlineEntity();
	}

	@Override
	public PlayerGiftEntity getPlayerGiftEntity() {
		// TODO Auto-generated method stub
		return super.getPlayerGiftEntity();
	}

	@Override
	public List<MoneyReissueEntity> getMoneyReissueEntityList() {
		// TODO Auto-generated method stub
		return super.getMoneyReissueEntityList();
	}

	@Override
	public PlayerResourceGiftEntity getPlayerResourceGiftEntity() {
		// TODO Auto-generated method stub
		return super.getPlayerResourceGiftEntity();
	}

	@Override
	public StorehouseBaseEntity getStorehouseBase() {
		// TODO Auto-generated method stub
		return super.getStorehouseBase();
	}

	@Override
	public List<StorehouseEntity> getStorehouseEntities() {
		// TODO Auto-generated method stub
		return super.getStorehouseEntities();
	}

	@Override
	public List<StorehouseHelpEntity> getStorehouseHelpEntities() {
		// TODO Auto-generated method stub
		return super.getStorehouseHelpEntities();
	}

	@Override
	public YuriStrikeEntity getYuriStrikeEntity() {
		// TODO Auto-generated method stub
		return super.getYuriStrikeEntity();
	}

	@Override
	public GuildHospiceEntity getGuildHospiceEntity() {
		// TODO Auto-generated method stub
		return super.getGuildHospiceEntity();
	}

	@Override
	public PlayerWarCollegeEntity getPlayerWarCollegeEntity() {
		// TODO Auto-generated method stub
		return super.getPlayerWarCollegeEntity();
	}

	@Override
	public CollegeMemberEntity getCollegeMemberEntity() {
		// TODO Auto-generated method stub
		return super.getCollegeMemberEntity();
	}

	@Override
	public List<PlayerDressPlayerInfo> getPlayerDressSendEntities() {
		// TODO Auto-generated method stub
		return super.getPlayerDressSendEntities();
	}

	@Override
	public List<PlayerDressPlayerInfo> getPlayerDressAskEntities() {
		// TODO Auto-generated method stub
		return super.getPlayerDressAskEntities();
	}

	@Override
	public PlayerOtherEntity getPlayerOtherEntity() {
		// TODO Auto-generated method stub
		return super.getPlayerOtherEntity();
	}

	@Override
	public PlayerGhostTowerEntity getPlayerGhostTowerEntity() {
		// TODO Auto-generated method stub
		return super.getPlayerGhostTowerEntity();
	}

	@Override
	public List<ObeliskEntity> getObeliskEntities() {
		// TODO Auto-generated method stub
		return super.getObeliskEntities();
	}

	@Override
	public List<PlantFactoryEntity> getPlantFactoryEntities() {
		// TODO Auto-generated method stub
		return super.getPlantFactoryEntities();
	}

	@Override
	public List<PlantTechEntity> getPlantTechEntities() {
		// TODO Auto-generated method stub
		return super.getPlantTechEntities();
	}

	@Override
	public AgencyEntity getAgencyEntity() {
		// TODO Auto-generated method stub
		return super.getAgencyEntity();
	}

	@Override
	public PlantScienceEntity getPlantScienceEntity() {
		// TODO Auto-generated method stub
		return super.getPlantScienceEntity();
	}

	@Override
	public PlayerYQZZEntity getPlayerYqzzEntity() {
		// TODO Auto-generated method stub
		return super.getPlayerYqzzEntity();
	}

	@Override
	public NationBuildQuestEntity getNationalBuildQuestEntity() {
		// TODO Auto-generated method stub
		return super.getNationalBuildQuestEntity();
	}

	@Override
	public NationMilitaryEntity getNationMilitaryEntity() {
		// TODO Auto-generated method stub
		return super.getNationMilitaryEntity();
	}

	@Override
	public HeroArchivesEntity getHeroArchivesEntity() {
		// TODO Auto-generated method stub
		return super.getHeroArchivesEntity();
	}

	@Override
	public LifetimeCardEntity getLifetimeCardEntity() {
		// TODO Auto-generated method stub
		return super.getLifetimeCardEntity();
	}

	@Override
	public MedalEntity getMedalEntity() {
		// TODO Auto-generated method stub
		return super.getMedalEntity();
	}

	@Override
	public PlayerDailyGiftBuyEntity getPlayerDailyGiftBuyEntity() {
		// TODO Auto-generated method stub
		return super.getPlayerDailyGiftBuyEntity();
	}

	@Override
	public Map<Integer, Integer> getSettingDatas() {
		// TODO Auto-generated method stub
		return super.getSettingDatas();
	}

	@Override
	public Set<String> getShieldPlayers() {
		// TODO Auto-generated method stub
		return super.getShieldPlayers();
	}

	@Override
	public Map<String, String> getBanRankInfos() {
		// TODO Auto-generated method stub
		return super.getBanRankInfos();
	}

	@Override
	public boolean loadPlayerData(String playerId) {
		// TODO Auto-generated method stub
		return super.loadPlayerData(playerId);
	}

	@Override
	public void loadAll(boolean isNewly) {
		// TODO Auto-generated method stub
		super.loadAll(isNewly);
	}

	@Override
	public void loadStart() {
		// TODO Auto-generated method stub
		super.loadStart();
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
		// TODO Auto-generated method stub
		return super.getPowerElectric();
	}

	@Override
	public int getEffVal(EffType effType) {
		// TODO Auto-generated method stub
		return super.getEffVal(effType);
	}

	@Override
	public int getEffVal(EffType effType, String targetId) {
		// TODO Auto-generated method stub
		return super.getEffVal(effType, targetId);
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
	public int getItemSpeedTimeByItemType(int speedType) {
		// TODO Auto-generated method stub
		return super.getItemSpeedTimeByItemType(speedType);
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
	public int getMaxLevelBuildingCfg(int type) {
		// TODO Auto-generated method stub
		return super.getMaxLevelBuildingCfg(type);
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
	public int getConstructionFactoryLevel() {
		// TODO Auto-generated method stub
		return super.getConstructionFactoryLevel();
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
	public boolean isSecondBuildUnlock() {
		// TODO Auto-generated method stub
		return super.isSecondBuildUnlock();
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
	public StatusDataEntity addStatusBuff(int statusId, long endTime) {
		// TODO Auto-generated method stub
		return super.addStatusBuff(statusId, endTime);
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
		// TODO Auto-generated method stub
		return super.createBuildingEntity(buildingCfg, index, immediate);
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
	public void addPlayerRechargeBackEntity(RechargeBackEntity entity) {
		// TODO Auto-generated method stub
		super.addPlayerRechargeBackEntity(entity);
	}

	@Override
	public void addPlayerRechargeDailyEntity(RechargeDailyEntity entity) {
		// TODO Auto-generated method stub
		super.addPlayerRechargeDailyEntity(entity);
	}

	@Override
	public RechargeDailyEntity getPlayerRechargeDailyEntity(String billno) {
		// TODO Auto-generated method stub
		return super.getPlayerRechargeDailyEntity(billno);
	}

	@Override
	protected int getRechargeTotal() {
		// TODO Auto-generated method stub
		return super.getRechargeTotal();
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
	public String getPfIcon() {
		// TODO Auto-generated method stub
		return super.getPfIcon();
	}

	@Override
	public String getIMPfIcon() {
		// TODO Auto-generated method stub
		return super.getIMPfIcon();
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
	public String getPlayerId() {
		// TODO Auto-generated method stub
		return super.getPlayerId();
	}

	@Override
	public void setPlayerId(String playerId) {
		// TODO Auto-generated method stub
		super.setPlayerId(playerId);
	}

	@Override
	public int getVipFlag() {
		// TODO Auto-generated method stub
		return super.getVipFlag();
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
	public PlayerDataCache getDataCache() {
		// TODO Auto-generated method stub
		return super.getDataCache();
	}

	@Override
	public void updatePlayerPos(int x, int y) {
		// TODO Auto-generated method stub
		super.updatePlayerPos(x, y);
	}

	@Override
	public Map<Integer, Integer> getItemExchangeInfo() {
		// TODO Auto-generated method stub
		return super.getItemExchangeInfo();
	}

	@Override
	public void setDataCache(PlayerDataCache dataCache) {
		// TODO Auto-generated method stub
		super.setDataCache(dataCache);
	}

	@Override
	public void serialData4Cross() {
		// TODO Auto-generated method stub
		super.serialData4Cross();
	}

	@Override
	public void serialEffectData4Cross() {
		// TODO Auto-generated method stub
		super.serialEffectData4Cross();
	}

	@Override
	public void loadData4Cross() {
		// TODO Auto-generated method stub
		super.loadData4Cross();
	}

	@Override
	public void loadEffectData4Cross() {
		// TODO Auto-generated method stub
		super.loadEffectData4Cross();
	}

	@Override
	public int getEffectValueInCross(int effectType) {
		// TODO Auto-generated method stub
		return super.getEffectValueInCross(effectType);
	}

	@Override
	public void checkEffectValueInCross() {
		// TODO Auto-generated method stub
		super.checkEffectValueInCross();
	}

	@Override
	public int getUnlockedSoldierMaxLevel() {
		// TODO Auto-generated method stub
		return super.getUnlockedSoldierMaxLevel();
	}

	@Override
	public int getFortressEffect(int effectType) {
		// TODO Auto-generated method stub
		return super.getFortressEffect(effectType);
	}

	@Override
	public int getAchievePoint() {
		// TODO Auto-generated method stub
		return super.getAchievePoint();
	}

	@Override
	public long getLastInviteMassTime() {
		// TODO Auto-generated method stub
		return super.getLastInviteMassTime();
	}

	@Override
	public void setLastInviteMassTime(long lastInviteMassTime) {
		// TODO Auto-generated method stub
		super.setLastInviteMassTime(lastInviteMassTime);
	}

	@Override
	public ArmourEntity getArmourEntity(String id) {
		// TODO Auto-generated method stub
		return super.getArmourEntity(id);
	}

	@Override
	public void removeArmourEntity(ArmourEntity armour) {
		// TODO Auto-generated method stub
		super.removeArmourEntity(armour);
	}

	@Override
	public List<ArmourEntity> getSuitArmours(int suit) {
		// TODO Auto-generated method stub
		return super.getSuitArmours(suit);
	}

	@Override
	public Map<EffType, Integer> getArmourEffect(int suit) {
		// TODO Auto-generated method stub
		return super.getArmourEffect(suit);
	}

	@Override
	public Map<Integer, List<ArmourEntity>> getArmourAttrSuitMap(List<ArmourEntity> armours) {
		// TODO Auto-generated method stub
		return super.getArmourAttrSuitMap(armours);
	}

	@Override
	public int getArmourSuitPower() {
		// TODO Auto-generated method stub
		return super.getArmourSuitPower();
	}

	@Override
	public CrossTechEntity createCrossTechEntity(CrossTechCfg cfg) {
		// TODO Auto-generated method stub
		return super.createCrossTechEntity(cfg);
	}

	@Override
	public CrossTechEntity getCrossTechEntity(String id) {
		// TODO Auto-generated method stub
		return super.getCrossTechEntity(id);
	}

	@Override
	public CrossTechEntity getCrossTechEntityByTechId(int techId) {
		// TODO Auto-generated method stub
		return super.getCrossTechEntityByTechId(techId);
	}

	@Override
	public void addCrossTechEntity(CrossTechEntity entity) {
		// TODO Auto-generated method stub
		super.addCrossTechEntity(entity);
	}

	@Override
	public void removeCrossTechEntity(CrossTechEntity entity) {
		// TODO Auto-generated method stub
		super.removeCrossTechEntity(entity);
	}

	@Override
	public EquipResearchEntity getEquipResearchEntity(int researchId) {
		// TODO Auto-generated method stub
		return super.getEquipResearchEntity(researchId);
	}

	@Override
	public ObeliskEntity getObeliskById(String id) {
		// TODO Auto-generated method stub
		return super.getObeliskById(id);
	}

	@Override
	public ObeliskEntity getObeliskByCfgId(int cfgId) {
		// TODO Auto-generated method stub
		return super.getObeliskByCfgId(cfgId);
	}

	@Override
	public synchronized ObeliskEntity getObeliskByCfgIdOrCreate(int cfgId) {
		// TODO Auto-generated method stub
		return super.getObeliskByCfgIdOrCreate(cfgId);
	}

	@Override
	public void addObeliskEntity(ObeliskEntity obeliskEntity) {
		// TODO Auto-generated method stub
		super.addObeliskEntity(obeliskEntity);
	}

	@Override
	public List<Integer> getPersonalProtectListVals() {
		// TODO Auto-generated method stub
		return super.getPersonalProtectListVals();
	}

	@Override
	public String getPersonalProtectVals() {
		// TODO Auto-generated method stub
		return super.getPersonalProtectVals();
	}

	@Override
	public void updatePersonalProtectVals(List<Integer> switchVals) {
		// TODO Auto-generated method stub
		super.updatePersonalProtectVals(switchVals);
	}

	@Override
	public int getIndexedProtectSwitchVal(int switchIndex) {
		// TODO Auto-generated method stub
		return super.getIndexedProtectSwitchVal(switchIndex);
	}

	@Override
	public int getSoldierStar(int armyId) {
		// TODO Auto-generated method stub
		return super.getSoldierStar(armyId);
	}

	@Override
	public int getSkinPoint() {
		// TODO Auto-generated method stub
		return super.getSkinPoint();
	}

	@Override
	public Builder getDeployedSwInfo() {
		// TODO Auto-generated method stub
		return super.getDeployedSwInfo();
	}

}
