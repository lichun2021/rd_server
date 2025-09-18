package com.hawk.game.module.lianmengtaiboliya.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.entifytype.EntityType;
import org.hawk.os.HawkOSOperator;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.entity.PlayerData4Activity;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.TalentLevelCfg;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.data.BubbleRewardInfo;
import com.hawk.game.entity.AccumulateOnlineEntity;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.DailyDataEntity;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.EquipEntity;
import com.hawk.game.entity.GuildHospiceEntity;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.MissionEntity;
import com.hawk.game.entity.MoneyReissueEntity;
import com.hawk.game.entity.PayStateEntity;
import com.hawk.game.entity.PlayerAchieveEntity;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.PlayerGachaEntity;
import com.hawk.game.entity.PlayerGiftEntity;
import com.hawk.game.entity.PlayerGuildGiftEntity;
import com.hawk.game.entity.PlayerMonsterEntity;
import com.hawk.game.entity.PlayerResourceGiftEntity;
import com.hawk.game.entity.PlotBattleEntity;
import com.hawk.game.entity.PushGiftEntity;
import com.hawk.game.entity.QuestionnaireEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.RechargeDailyEntity;
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
import com.hawk.game.module.lianmengtaiboliya.entity.TBLYArmyEntity;
import com.hawk.game.module.lianmengtaiboliya.entity.TBLYDbEntityCopyUtil;
import com.hawk.game.module.staffofficer.StaffOfficerSkillCollection;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PowerElectric;
import com.hawk.game.player.cache.PlayerDataCache;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.player.equip.CommanderObject;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.GachaType;
import com.hawk.game.protocol.Const.LimitType;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Player.PlayerFlagPosition;

public class TBLYPlayerData extends ITBLYPlayerData {
	private ITBLYPlayerEffect playerEffect;
	/** 兵数据副本 */
	private List<ArmyEntity> armyEntities;
	/** 队列 */
	private List<QueueEntity> queueEntities;
	// private List<StatusDataEntity> statusDataEntities;

	private TBLYPlayerData(ITBLYPlayer parent) {
		super(parent);
	}

	@Override
	public TBLYPlayer getParent() {
		return (TBLYPlayer) super.getParent();
	}

	public static TBLYPlayerData valueOf(TBLYPlayer player) {
		PlayerData source = player.getSource().getData();
		TBLYPlayerData result = new TBLYPlayerData(player);
		result.playerId = source.getPlayerId();
		result.dataCache = source.getDataCache();
		// army副本
		Map<Integer, TBLYArmyEntity> armyMap = new HashMap<>();
		ConfigIterator<BattleSoldierCfg> armyit = HawkConfigManager.getInstance().getConfigIterator(BattleSoldierCfg.class);
		for (BattleSoldierCfg arCfg : armyit) {
			TBLYArmyEntity army = new TBLYArmyEntity();
			army.setId(HawkUUIDGenerator.genUUID());
			army.setPlayerId(source.getPlayerId());
			army.setArmyId(arCfg.getId());
			army.setPersistable(false);
			army.setEntityType(EntityType.TEMPORARY);
			armyMap.put(army.getArmyId(), army);
		}
		for (ArmyEntity army : source.getArmyEntities()) {
			armyMap.put(army.getArmyId(), TBLYDbEntityCopyUtil.copyOf(army));
		}
		result.armyEntities = new ArrayList<>(armyMap.values());

		// 队列
		result.queueEntities = new ArrayList<>();

		// buff
		// result.statusDataEntities = new ArrayList<>();

		// effect 对象
		result.playerEffect = new TBLYPlayerEffect(result);
		result.playerEffect.setParent(player);
		result.lockOriginalData();
		return result;
	}
	
	@Override
	public StaffOfficerSkillCollection getStaffOffic(){
		return getSource().getStaffOffic();
	}

	public void lockOriginalData() {
		// 锁定原始数据
		getSource().getDataCache().lockKey(PlayerDataKey.ArmyEntities,getArmyEntities());
		getSource().getDataCache().lockKey(PlayerDataKey.QueueEntities,getQueueEntities());
		// getSource().getDataCache().lockKey(PlayerDataKey.StatusDataEntities);
	}

	public void unLockOriginalData() {
		// 锁定原始数据
		getSource().getDataCache().unLockKey(PlayerDataKey.ArmyEntities);
		getSource().getDataCache().unLockKey(PlayerDataKey.QueueEntities);
		// getSource().getDataCache().unLockKey(PlayerDataKey.StatusDataEntities);
	}
	
	public int getAchievePoint() {
		return 0;
	}

	@Override
	public ITBLYPlayerEffect getPlayerEffect() {
		return playerEffect;
	}

	@Override
	public List<ArmyEntity> getArmyEntities() {
		return armyEntities;
	}

	@Override
	public List<QueueEntity> getQueueEntities() {
		return queueEntities;
	}

	@Override
	public List<StatusDataEntity> getStatusDataEntities() {
		// return statusDataEntities;
		return getSource().getStatusDataEntities();
	}

	@Override
	public PlayerEntity getPlayerEntity() {
		// TODO Auto-generated method stub
		return getSource().getPlayerEntity();
	}

	@Override
	public PlayerBaseEntity getPlayerBaseEntity() {
		// TODO Auto-generated method stub
		return getSource().getPlayerBaseEntity();
	}

	@Override
	public StatisticsEntity getStatisticsEntity() {
		// TODO Auto-generated method stub
		return getSource().getStatisticsEntity();
	}

	@Override
	public PayStateEntity getPayStateEntity() {
		// TODO Auto-generated method stub
		return getSource().getPayStateEntity();
	}

	@Override
	public List<TalentEntity> getTalentEntities() {
		// TODO Auto-generated method stub
		return getSource().getTalentEntities();
	}

	@Override
	public List<ItemEntity> getItemEntities() {
		// TODO Auto-generated method stub
		return getSource().getItemEntities();
	}

	@Override
	public List<BuildingBaseEntity> getBuildingEntitiesIgnoreStatus() {
		// TODO Auto-generated method stub
		return getSource().getBuildingEntitiesIgnoreStatus();
	}

	@Override
	public List<TechnologyEntity> getTechnologyEntities() {
		// TODO Auto-generated method stub
		return getSource().getTechnologyEntities();
	}

	@Override
	public List<PlayerGachaEntity> getPlayerGachaEntities() {
		// TODO Auto-generated method stub
		return getSource().getPlayerGachaEntities();
	}

	@Override
	public List<EquipEntity> getEquipEntities() {
		// TODO Auto-generated method stub
		return getSource().getEquipEntities();
	}

	@Override
	public CommanderEntity getCommanderEntity() {
		// TODO Auto-generated method stub
		return getSource().getCommanderEntity();
	}

	@Override
	public List<MissionEntity> getMissionEntities() {
		// TODO Auto-generated method stub
		return getSource().getMissionEntities();
	}

	@Override
	public StoryMissionEntity getStoryMissionEntity() {
		// TODO Auto-generated method stub
		return getSource().getStoryMissionEntity();
	}

	@Override
	public PlayerAchieveEntity getPlayerAchieveEntity() {
		// TODO Auto-generated method stub
		return getSource().getPlayerAchieveEntity();
	}

	@Override
	public List<RechargeDailyEntity> getPlayerRechargeDailyEntities() {
		return getSource().getPlayerRechargeDailyEntities();
	}

	@Override
	public QuestionnaireEntity getQuestionnaireEntity() {
		// TODO Auto-generated method stub
		return getSource().getQuestionnaireEntity();
	}

	@Override
	public PlayerMonsterEntity getMonsterEntity() {
		// TODO Auto-generated method stub
		return getSource().getMonsterEntity();
	}

	@Override
	public WishingWellEntity getWishingEntity() {
		// TODO Auto-generated method stub
		return getSource().getWishingEntity();
	}

	@Override
	public TavernEntity getTavernEntity() {
		// TODO Auto-generated method stub
		return getSource().getTavernEntity();
	}

	@Override
	public WharfEntity getWharfEntity() {
		// TODO Auto-generated method stub
		return getSource().getWharfEntity();
	}

	@Override
	public List<HeroEntity> getHeroEntityList() {
		// TODO Auto-generated method stub
		return getSource().getHeroEntityList();
	}

	@Override
	public List<SuperSoldierEntity> getSuperSoldierEntityList() {
		// TODO Auto-generated method stub
		return getSource().getSuperSoldierEntityList();
	}

	@Override
	public List<PlayerGuildGiftEntity> getGuildGiftEntity() {
		// TODO Auto-generated method stub
		return getSource().getGuildGiftEntity();
	}

	@Override
	public List<CustomDataEntity> getCustomDataEntities() {
		// TODO Auto-generated method stub
		return getSource().getCustomDataEntities();
	}

	@Override
	public DailyDataEntity getDailyDataEntity() {
		// TODO Auto-generated method stub
		return getSource().getDailyDataEntity();
	}

	@Override
	public PlotBattleEntity getPlotBattleEntity() {
		// TODO Auto-generated method stub
		return getSource().getPlotBattleEntity();
	}

	@Override
	public DressEntity getDressEntity() {
		// TODO Auto-generated method stub
		return getSource().getDressEntity();
	}

	@Override
	public AccumulateOnlineEntity getAccumulateOnlineEntity() {
		// TODO Auto-generated method stub
		return getSource().getAccumulateOnlineEntity();
	}

	@Override
	public PlayerGiftEntity getPlayerGiftEntity() {
		// TODO Auto-generated method stub
		return getSource().getPlayerGiftEntity();
	}

	@Override
	public List<MoneyReissueEntity> getMoneyReissueEntityList() {
		// TODO Auto-generated method stub
		return getSource().getMoneyReissueEntityList();
	}

	@Override
	public PlayerResourceGiftEntity getPlayerResourceGiftEntity() {
		// TODO Auto-generated method stub
		return getSource().getPlayerResourceGiftEntity();
	}

	@Override
	public StorehouseBaseEntity getStorehouseBase() {
		// TODO Auto-generated method stub
		return getSource().getStorehouseBase();
	}

	@Override
	public List<StorehouseEntity> getStorehouseEntities() {
		// TODO Auto-generated method stub
		return getSource().getStorehouseEntities();
	}

	@Override
	public List<StorehouseHelpEntity> getStorehouseHelpEntities() {
		// TODO Auto-generated method stub
		return getSource().getStorehouseHelpEntities();
	}

	@Override
	public YuriStrikeEntity getYuriStrikeEntity() {
		// TODO Auto-generated method stub
		return getSource().getYuriStrikeEntity();
	}

	@Override
	public GuildHospiceEntity getGuildHospiceEntity() {
		// TODO Auto-generated method stub
		return getSource().getGuildHospiceEntity();
	}

	@Override
	public Map<Integer, Integer> getSettingDatas() {
		// TODO Auto-generated method stub
		return getSource().getSettingDatas();
	}

	@Override
	public Set<String> getShieldPlayers() {
		// TODO Auto-generated method stub
		return getSource().getShieldPlayers();
	}

	@Override
	public Map<String, String> getBanRankInfos() {
		// TODO Auto-generated method stub
		return getSource().getBanRankInfos();
	}

	@Override
	public boolean loadPlayerData(String playerId) {
		// TODO Auto-generated method stub
		return getSource().loadPlayerData(playerId);
	}

	@Override
	public void loadAll(boolean isNewly) {
		// TODO Auto-generated method stub
		getSource().loadAll(isNewly);
	}

	@Override
	public boolean updateCacheExpire(int expireTime) {
		// TODO Auto-generated method stub
		return getSource().updateCacheExpire(expireTime);
	}

	@Override
	public void addBanRankInfo(String rankType, String banInfo) {
		// TODO Auto-generated method stub
		getSource().addBanRankInfo(rankType, banInfo);
	}

	@Override
	public String getBanRankInfo(String rankType) {
		// TODO Auto-generated method stub
		return getSource().getBanRankInfo(rankType);
	}

	@Override
	public void removeBanRankInfo(String... rankTypes) {
		// TODO Auto-generated method stub
		getSource().removeBanRankInfo(rankTypes);
	}

	@Override
	public boolean assembleData(Player player) {
		// TODO Auto-generated method stub
		return getSource().assembleData(player);
	}

	@Override
	public void setFromCache(boolean isFromCache) {
		// TODO Auto-generated method stub
		getSource().setFromCache(isFromCache);
	}

	@Override
	public boolean isFromCache() {
		// TODO Auto-generated method stub
		return getSource().isFromCache();
	}

	@Override
	public void setSnapshotNextUpdateTime(long snapshotNextUpdateTime) {
		// TODO Auto-generated method stub
		getSource().setSnapshotNextUpdateTime(snapshotNextUpdateTime);
	}

	@Override
	public boolean isSnapshotNeedUpdateTime() {
		// TODO Auto-generated method stub
		return getSource().isSnapshotNeedUpdateTime();
	}

	@Override
	public PowerElectric getPowerElectric() {
		// TODO Auto-generated method stub
		return getSource().getPowerElectric();
	}

	@Override
	public int getEffVal(EffType effType) {
		return getPlayerEffect().getEffVal(effType);
	}

	@Override
	public int getEffVal(EffType effType, String targetId) {
		return getPlayerEffect().getEffVal(effType, targetId);
	}

	@Override
	public void addItemEntity(ItemEntity itemEntity) {
		// TODO Auto-generated method stub
		getSource().addItemEntity(itemEntity);
	}

	@Override
	public ItemEntity getItemById(String id) {
		// TODO Auto-generated method stub
		return getSource().getItemById(id);
	}

	@Override
	public List<ItemEntity> getItemsByItemId(int itemId) {
		// TODO Auto-generated method stub
		return getSource().getItemsByItemId(itemId);
	}

	@Override
	public int getItemNumByItemType(int itemType) {
		// TODO Auto-generated method stub
		return getSource().getItemNumByItemType(itemType);
	}

	@Override
	public int getItemNumByItemId(int itemId) {
		// TODO Auto-generated method stub
		return getSource().getItemNumByItemId(itemId);
	}

	@Override
	public void addTalentEntity(TalentEntity talentEntity) {
		// TODO Auto-generated method stub
		getSource().addTalentEntity(talentEntity);
	}

	@Override
	public TalentEntity createTalentEntity(int talentId, int type, int level, TalentLevelCfg cfg) {
		// TODO Auto-generated method stub
		return getSource().createTalentEntity(talentId, type, level, cfg);
	}

	@Override
	public int getTalentCurrentMaxLvl(int talentId) {
		// TODO Auto-generated method stub
		return getSource().getTalentCurrentMaxLvl(talentId);
	}

	@Override
	public TalentEntity getTalentByTalentId(int talentId, int talentType) {
		// TODO Auto-generated method stub
		return getSource().getTalentByTalentId(talentId, talentType);
	}

	@Override
	public TalentEntity getTalentSkill(int skillId) {
		// TODO Auto-generated method stub
		return getSource().getTalentSkill(skillId);
	}

	@Override
	public List<TalentEntity> getTalentSkills(int skillId) {
		// TODO Auto-generated method stub
		return getSource().getTalentSkills(skillId);
	}

	@Override
	public long getTalentSkillMaxRefTime(int skillId) {
		// TODO Auto-generated method stub
		return getSource().getTalentSkillMaxRefTime(skillId);
	}

	@Override
	public List<TalentEntity> getAllTalentSkills() {
		// TODO Auto-generated method stub
		return getSource().getAllTalentSkills();
	}

	@Override
	public List<TalentEntity> getTalentSkills() {
		// TODO Auto-generated method stub
		return getSource().getTalentSkills();
	}

	@Override
	public List<Integer> castedTalentSkill() {
		// TODO Auto-generated method stub
		return getSource().castedTalentSkill();
	}

	@Override
	public List<BuildingBaseEntity> getBuildingEntities() {
		// TODO Auto-generated method stub
		return getSource().getBuildingEntities();
	}

	@Override
	public void addBuildingEntity(BuildingBaseEntity entity) {
		// TODO Auto-generated method stub
		getSource().addBuildingEntity(entity);
	}

	@Override
	public void deleteBuildingEntity(BuildingBaseEntity entity) {
		// TODO Auto-generated method stub
		getSource().deleteBuildingEntity(entity);
	}

	@Override
	public BuildingBaseEntity getBuildingBaseEntity(String id) {
		// TODO Auto-generated method stub
		return getSource().getBuildingBaseEntity(id);
	}

	@Override
	public BuildingBaseEntity getBuildingEntityIgnoreStatus(String id) {
		// TODO Auto-generated method stub
		return getSource().getBuildingEntityIgnoreStatus(id);
	}

	@Override
	public int getBuildingLevel(String id) {
		// TODO Auto-generated method stub
		return getSource().getBuildingLevel(id);
	}

	@Override
	public List<BuildingBaseEntity> getBuildingListByType(BuildingType type) {
		// TODO Auto-generated method stub
		return getSource().getBuildingListByType(type);
	}

	@Override
	public int getBuildCount(BuildingType type) {
		// TODO Auto-generated method stub
		return getSource().getBuildCount(type);
	}

	@Override
	public int getResBuildCount() {
		// TODO Auto-generated method stub
		return getSource().getResBuildCount();
	}

	@Override
	public BuildingBaseEntity getBuildingEntityByType(BuildingType type) {
		// TODO Auto-generated method stub
		return getSource().getBuildingEntityByType(type);
	}

	@Override
	public int getBuildingResOutputByType(int buildingType) {
		// TODO Auto-generated method stub
		return getSource().getBuildingResOutputByType(buildingType);
	}

	@Override
	public List<BuildingBaseEntity> getBuildingListByLimitTypeIgnoreStatus(LimitType... limitTypes) {
		// TODO Auto-generated method stub
		return getSource().getBuildingListByLimitTypeIgnoreStatus(limitTypes);
	}

	@Override
	public List<BuildingBaseEntity> getBuildingListByLimitType(LimitType... limitTypes) {
		// TODO Auto-generated method stub
		return getSource().getBuildingListByLimitType(limitTypes);
	}

	@Override
	public List<BuildingBaseEntity> getBuildingListByCfgId(int buildingCfgId) {
		// TODO Auto-generated method stub
		return getSource().getBuildingListByCfgId(buildingCfgId);
	}

	@Override
	public int getBuildingMaxLevel(int type) {
		// TODO Auto-generated method stub
		return getSource().getBuildingMaxLevel(type);
	}

	@Override
	public BuildingCfg getBuildingCfgByType(BuildingType buildingType) {
		// TODO Auto-generated method stub
		return getSource().getBuildingCfgByType(buildingType);
	}

	@Override
	public int getTradeTaxRate() {
		// TODO Auto-generated method stub
		return getSource().getTradeTaxRate();
	}

	@Override
	public int getConstructionFactoryLevel() {
		// TODO Auto-generated method stub
		return getSource().getConstructionFactoryLevel();
	}

	@Override
	public BuildingBaseEntity getConstructionFactory() {
		// TODO Auto-generated method stub
		return getSource().getConstructionFactory();
	}

	@Override
	public void addQueueEntity(QueueEntity queueEntity) {
		getQueueEntities().add(queueEntity);
	}

	@Override
	public QueueEntity getQueueEntity(String id) {
		for (QueueEntity queueEntity : getQueueEntities()) {
			if (queueEntity.getId().equals(id)) {
				return queueEntity;
			}
		}
		return null;
	}

	@Override
	public List<QueueEntity> getBusyCommonQueue(int queueType) {
		// TODO Auto-generated method stub
		return getSource().getBusyCommonQueue(queueType);
	}

	@Override
	public QueueEntity getQueueByBuildingType(int buildingType) {
		// TODO Auto-generated method stub
		return getSource().getQueueByBuildingType(buildingType);
	}

	@Override
	public boolean hasBusyCommonQueue(List<Integer> typeList) {
		// TODO Auto-generated method stub
		return getSource().hasBusyCommonQueue(typeList);
	}

	@Override
	public QueueEntity getPaidQueue() {
		// TODO Auto-generated method stub
		return getSource().getPaidQueue();
	}

	@Override
	public QueueEntity getFreeQueue(int queueType) {
		QueueEntity queueEntity = new QueueEntity();
		///////////////////
		queueEntity.setPersistable(false);
		queueEntity.setEntityType(EntityType.TEMPORARY);
		/////////////////////
		queueEntity.setId(HawkOSOperator.randomUUID());
		return queueEntity;
	}

	@Override
	public QueueEntity getQueueEntityByItemId(String id) {
		// TODO Auto-generated method stub
		return getSource().getQueueEntityByItemId(id);
	}

	@Override
	public QueueEntity getQueueEntity(String id, String itemId) {
		// TODO Auto-generated method stub
		return getSource().getQueueEntity(id, itemId);
	}

	@Override
	public Map<String, QueueEntity> getQueueEntitiesByType(int type) {
		Map<String, QueueEntity> map = new HashMap<String, QueueEntity>();
		for (QueueEntity queueEntity : getQueueEntities()) {
			if (queueEntity.getQueueType() == type) {
				map.put(queueEntity.getItemId(), queueEntity);
			}
		}
		return map;
	}

	@Override
	public TechnologyEntity createTechnologyEntity(TechnologyCfg cfg) {
		// TODO Auto-generated method stub
		return getSource().createTechnologyEntity(cfg);
	}

	@Override
	public TechnologyEntity getTechnologyEntity(String id) {
		// TODO Auto-generated method stub
		return getSource().getTechnologyEntity(id);
	}

	@Override
	public TechnologyEntity getTechEntityByTechId(int techId) {
		// TODO Auto-generated method stub
		return getSource().getTechEntityByTechId(techId);
	}

	@Override
	public void addTechnologyEntity(TechnologyEntity entity) {
		// TODO Auto-generated method stub
		getSource().addTechnologyEntity(entity);
	}

	@Override
	public void removeTechnologyEntity(TechnologyEntity entity) {
		// TODO Auto-generated method stub
		getSource().removeTechnologyEntity(entity);
	}

	@Override
	public ArmyEntity getArmyEntity(int armyId) {
		for (ArmyEntity armyEntity : getArmyEntities()) {
			if (armyEntity.getArmyId() == armyId) {
				return armyEntity;
			}
		}
		TBLYArmyEntity armyEntity = new TBLYArmyEntity();
		armyEntity.setPlayerId(getPlayerId());
		armyEntity.setArmyId(armyId);
		armyEntity.setPersistable(false);
		armyEntity.setEntityType(EntityType.TEMPORARY);
		return null;
	}

	@Override
	public void addArmyEntity(ArmyEntity armyEntity) {
		this.getArmyEntities().add(armyEntity);
	}

	@Override
	public int getArmyCount() {
		// TODO Auto-generated method stub
		return getSource().getArmyCount();
	}

	@Override
	public void addEquipEntities(Collection<? extends EquipEntity> equipEntities) {
		// TODO Auto-generated method stub
		getSource().addEquipEntities(equipEntities);
	}

	@Override
	public void removeEquipEntity(EquipEntity equipEntity) {
		// TODO Auto-generated method stub
		getSource().removeEquipEntity(equipEntity);
	}

	@Override
	public EquipEntity getEquipEntity(String equipId) {
		// TODO Auto-generated method stub
		return getSource().getEquipEntity(equipId);
	}

	@Override
	public CommanderObject getCommanderObject() {
		// TODO Auto-generated method stub
		return getSource().getCommanderObject();
	}

	@Override
	public MissionEntity getMissionById(String uuid) {
		// TODO Auto-generated method stub
		return getSource().getMissionById(uuid);
	}

	@Override
	public List<MissionEntity> getOpenedMissions() {
		// TODO Auto-generated method stub
		return getSource().getOpenedMissions();
	}

	@Override
	public MissionEntity getMissionByTypeId(int typeId) {
		// TODO Auto-generated method stub
		return getSource().getMissionByTypeId(typeId);
	}

	@Override
	public StatusDataEntity getStatusById(int statusId) {
		// TODO Auto-generated method stub
		return getSource().getStatusById(statusId);
	}

	@Override
	public StatusDataEntity getStatusById(int statusId, String targetId) {
		// TODO Auto-generated method stub
		return getSource().getStatusById(statusId, targetId);
	}

	@Override
	public List<StatusDataEntity> getStatusListById(int statusId) {
		// TODO Auto-generated method stub
		return getSource().getStatusListById(statusId);
	}

	@Override
	public StatusDataEntity getStateById(int stateId, int stateType) {
		// TODO Auto-generated method stub
		return getSource().getStateById(stateId, stateType);
	}

	@Override
	public PlayerDataCache getDataCache() {
		return getSource().getDataCache();
	}

	@Override
	public List<String> getBusyBuildingUuid() {
		List<String> uuidList = new ArrayList<String>();
		for (QueueEntity entity : getQueueEntities()) {
			if (entity.getQueueType() == QueueType.BUILDING_QUEUE_VALUE || entity.getQueueType() == QueueType.BUILDING_DEFENER_VALUE) {
				uuidList.add(entity.getItemId());
			}
		}
		return uuidList;
	}

	@Override
	public int getBuildingNumLimit(int limitType) {
		// TODO Auto-generated method stub
		return getSource().getBuildingNumLimit(limitType);
	}

	@Override
	public long getMaxStoreOil() {
		// TODO Auto-generated method stub
		return getSource().getMaxStoreOil();
	}

	@Override
	public void addOilMaxStore(long addStoreOil) {
		// TODO Auto-generated method stub
		getSource().addOilMaxStore(addStoreOil);
	}

	@Override
	public long getMaxStoreSteel() {
		// TODO Auto-generated method stub
		return getSource().getMaxStoreSteel();
	}

	@Override
	public void addSteelMaxStore(long addStoreSteel) {
		// TODO Auto-generated method stub
		getSource().addSteelMaxStore(addStoreSteel);
	}

	@Override
	public long getMaxStoreRare() {
		// TODO Auto-generated method stub
		return getSource().getMaxStoreRare();
	}

	@Override
	public void addRareMaxStore(long addStoreRare) {
		// TODO Auto-generated method stub
		getSource().addRareMaxStore(addStoreRare);
	}

	@Override
	public long getMaxStoreOre() {
		// TODO Auto-generated method stub
		return getSource().getMaxStoreOre();
	}

	@Override
	public void addOreMaxStore(long addStoreOre) {
		// TODO Auto-generated method stub
		getSource().addOreMaxStore(addStoreOre);
	}

	@Override
	public long getOilOutputPerHour() {
		// TODO Auto-generated method stub
		return getSource().getOilOutputPerHour();
	}

	@Override
	public void addOilOutputPerHour(long addOilOutputPerHour) {
		// TODO Auto-generated method stub
		getSource().addOilOutputPerHour(addOilOutputPerHour);
	}

	@Override
	public long getSteelOutputPerHour() {
		// TODO Auto-generated method stub
		return getSource().getSteelOutputPerHour();
	}

	@Override
	public void addSteelOutputPerHour(long addSteelOutputPerHour) {
		// TODO Auto-generated method stub
		getSource().addSteelOutputPerHour(addSteelOutputPerHour);
	}

	@Override
	public long getRareOutputPerHour() {
		// TODO Auto-generated method stub
		return getSource().getRareOutputPerHour();
	}

	@Override
	public void addRareOutputPerHour(long addRareOutputPerHour) {
		// TODO Auto-generated method stub
		getSource().addRareOutputPerHour(addRareOutputPerHour);
	}

	@Override
	public long getOreOutputPerHour() {
		// TODO Auto-generated method stub
		return getSource().getOreOutputPerHour();
	}

	@Override
	public void addOreOutputPerHour(long addOreOutputPerHour) {
		// TODO Auto-generated method stub
		getSource().addOreOutputPerHour(addOreOutputPerHour);
	}

	@Override
	public boolean getVipActivated() {
		// TODO Auto-generated method stub
		return getSource().getVipActivated();
	}

	@Override
	public long getBuffEndTime(int buffId) {
		// TODO Auto-generated method stub
		return getSource().getBuffEndTime(buffId);
	}

	@Override
	protected StatusDataEntity removeCityShield() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getCityShieldTime() {
		// TODO Auto-generated method stub
		return getSource().getCityShieldTime();
	}

	@Override
	protected StatusDataEntity addStatusBuff(int buffId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected StatusDataEntity addStatusBuff(int buffId, String targetId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected StatusDataEntity addStatusBuff(int buffId, String targetId, long endTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeQueue(QueueEntity queueEntity) {
		// TODO Auto-generated method stub
		getSource().removeQueue(queueEntity);
	}

	@Override
	public void removeBuilding(BuildingBaseEntity building) {
		// TODO Auto-generated method stub
		getSource().removeBuilding(building);
	}

	@Override
	public long getResourceOutputRate(int resType) {
		// TODO Auto-generated method stub
		return getSource().getResourceOutputRate(resType);
	}

	@Override
	public CustomDataEntity getCustomDataEntity(String key) {
		// TODO Auto-generated method stub
		return getSource().getCustomDataEntity(key);
	}

	@Override
	public CustomDataEntity createCustomDataEntity(String type, int value, String arg) {
		// TODO Auto-generated method stub
		return getSource().createCustomDataEntity(type, value, arg);
	}

	@Override
	public BuildingBaseEntity createBuildingEntity(BuildingCfg buildingCfg, String index, boolean immediate) {
		// TODO Auto-generated method stub
		return getSource().createBuildingEntity(buildingCfg, index, immediate);
	}

	@Override
	public void refreshNewBuilding(BuildingBaseEntity buildingEntity) {
		// TODO Auto-generated method stub
		getSource().refreshNewBuilding(buildingEntity);
	}

	@Override
	public int getRealMaxCityDef() {
		// TODO Auto-generated method stub
		return getSource().getRealMaxCityDef();
	}

	@Override
	public int getTrapCapacity() {
		// TODO Auto-generated method stub
		return getSource().getTrapCapacity();
	}

	@Override
	public int getTrapCount() {
		// TODO Auto-generated method stub
		return getSource().getTrapCount();
	}

	@Override
	public void addPlayerRechargeDailyEntity(RechargeDailyEntity entity) {
		getSource().addPlayerRechargeDailyEntity(entity);
	}

	@Override
	public RechargeDailyEntity getPlayerRechargeDailyEntity(String billno) {
		return getSource().getPlayerRechargeDailyEntity(billno);
	}

	@Override
	public int getRechargeTimesToday(int rechargeType, String goodsId) {
		// TODO Auto-generated method stub
		return getSource().getRechargeTimesToday(rechargeType, goodsId);
	}

	@Override
	public PlayerGachaEntity getGachaEntityByType(GachaType gachaType) {
		// TODO Auto-generated method stub
		return getSource().getGachaEntityByType(gachaType);
	}

	@Override
	public List<HeroEntity> getHeroEntityByCfgId(List<Integer> heroIds) {
		// TODO Auto-generated method stub
		return getSource().getHeroEntityByCfgId(heroIds);
	}

	@Override
	public int getOperationCount() {
		// TODO Auto-generated method stub
		return getSource().getOperationCount();
	}

	@Override
	public void setOperationCount(int operationCount) {
		// TODO Auto-generated method stub
		getSource().setOperationCount(operationCount);
	}

	@Override
	public int getClientServerTimeSub() {
		// TODO Auto-generated method stub
		return getSource().getClientServerTimeSub();
	}

	@Override
	public void setClientServerTimeSub(int clientServerTimeSub) {
		// TODO Auto-generated method stub
		getSource().setClientServerTimeSub(clientServerTimeSub);
	}

	@Override
	public int getHotSaleValue() {
		// TODO Auto-generated method stub
		return getSource().getHotSaleValue();
	}

	@Override
	public void setHotSaleValue(int hotSaleValue) {
		// TODO Auto-generated method stub
		getSource().setHotSaleValue(hotSaleValue);
	}

	@Override
	public String getPfIcon() {
		// TODO Auto-generated method stub
		return getSource().getPfIcon();
	}

	@Override
	public String getIMPfIcon() {
		// TODO Auto-generated method stub
		return getSource().getIMPfIcon();
	}

	@Override
	public int getMaxCityDef() {
		// TODO Auto-generated method stub
		return getSource().getMaxCityDef();
	}

	@Override
	public void setMaxCityDef(int cityDef) {
		// TODO Auto-generated method stub
		getSource().setMaxCityDef(cityDef);
	}

	@Override
	public JSONObject getPresidentSilentInfo() {
		// TODO Auto-generated method stub
		return getSource().getPresidentSilentInfo();
	}

	@Override
	public void setPresidentSilentInfo(JSONObject presidentSilentInfo) {
		// TODO Auto-generated method stub
		getSource().setPresidentSilentInfo(presidentSilentInfo);
	}

	@Override
	public void updateBubbleRewardInfo(int type, BubbleRewardInfo bubbleInfo) {
		// TODO Auto-generated method stub
		getSource().updateBubbleRewardInfo(type, bubbleInfo);
	}

	@Override
	public BubbleRewardInfo getBubbleRewardInfo(int type) {
		// TODO Auto-generated method stub
		return getSource().getBubbleRewardInfo(type);
	}

	@Override
	public Map<String, Integer> getRemoveBuildingExps() {
		// TODO Auto-generated method stub
		return getSource().getRemoveBuildingExps();
	}

	@Override
	public boolean isLively() {
		// TODO Auto-generated method stub
		return getSource().isLively();
	}

	@Override
	public boolean isRtsComplete(int missionId) {
		// TODO Auto-generated method stub
		return getSource().isRtsComplete(missionId);
	}

	@Override
	public PushGiftEntity getPushGiftEntity() {
		// TODO Auto-generated method stub
		return getSource().getPushGiftEntity();
	}

	@Override
	public void addMoneyReissueEntity(MoneyReissueEntity entity) {
		// TODO Auto-generated method stub
		getSource().addMoneyReissueEntity(entity);
	}

	@Override
	public void removeMoneyReissueEntity(MoneyReissueEntity entity) {
		// TODO Auto-generated method stub
		getSource().removeMoneyReissueEntity(entity);
	}

	@Override
	public PlayerData4Activity getPlayerData4Activity() {
		// TODO Auto-generated method stub
		return getSource().getPlayerData4Activity();
	}

	@Override
	public void setPlayerData4Activity(PlayerData4Activity playerData4Activity) {
		// TODO Auto-generated method stub
		getSource().setPlayerData4Activity(playerData4Activity);
	}

	@Override
	public boolean checkFlagSet(PlayerFlagPosition position) {
		// TODO Auto-generated method stub
		return getSource().checkFlagSet(position);
	}

	@Override
	public int getFlag(PlayerFlagPosition position) {
		// TODO Auto-generated method stub
		return getSource().getFlag(position);
	}

	@Override
	public boolean setFlag(PlayerFlagPosition position, int value) {
		// TODO Auto-generated method stub
		return getSource().setFlag(position, value);
	}

	@Override
	public void setPfIcon(String pfIcon) {
		// TODO Auto-generated method stub
		getSource().setPfIcon(pfIcon);
	}

	@Override
	public synchronized void updateVitBuyTimesInfo(int times) {
		// TODO Auto-generated method stub
		getSource().updateVitBuyTimesInfo(times);
	}

	@Override
	public synchronized int getVitBuyTimesToday() {
		// TODO Auto-generated method stub
		return getSource().getVitBuyTimesToday();
	}

	@Override
	public String getPlayerId() {
		// TODO Auto-generated method stub
		return getSource().getPlayerId();
	}

	@Override
	public void setPlayerId(String playerId) {
		// TODO Auto-generated method stub
		getSource().setPlayerId(playerId);
	}

	@Override
	public int getVipFlag() {
		// TODO Auto-generated method stub
		return getSource().getVipFlag();
	}

	@Override
	public String getPrimitivePfIcon() {
		// TODO Auto-generated method stub
		return getSource().getPrimitivePfIcon();
	}

	@Override
	public void setPrimitivePfIcon(String primitivePfIcon) {
		// TODO Auto-generated method stub
		getSource().setPrimitivePfIcon(primitivePfIcon);
	}

	public void setPlayerEffect(ITBLYPlayerEffect playerEffect) {
		this.playerEffect = playerEffect;
	}

	public void setArmyEntities(List<ArmyEntity> armyEntities) {
		this.armyEntities = armyEntities;
	}

	public void setQueueEntities(List<QueueEntity> queueEntities) {
		this.queueEntities = queueEntities;
	}

	public PlayerData getSource() {
		return getParent().getSource().getData();
	}

}
