package com.hawk.game.script;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildScienceEntity;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.game.GsConfig;
import com.hawk.game.config.BuildAreaCfg;
import com.hawk.game.config.BuildLimitCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.GuildScienceMainCfg;
import com.hawk.game.config.SuperSoldierCfg;
import com.hawk.game.config.TalentLevelCfg;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.PlantTechEntity;
import com.hawk.game.entity.SuperSoldierEntity;
import com.hawk.game.entity.TalentEntity;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.plantfactory.tech.PlantTech;
import com.hawk.game.player.Player;
import com.hawk.game.player.cache.PlayerDataCache;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Building.BuildingUpdateOperation;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierState;
import com.hawk.game.service.BuildingService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventUnlockGround;
import com.hawk.game.util.GameUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 复制账号
 * @author golden
 *
 */
public class CopyPlayerHandler extends HawkScript {
	static final String csplayerId = "234j234klj234jlk123";

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {

		if (!GsConfig.getInstance().isDebug()) {
			return "not debug mode !";
		}
		//	PlayerDataCache from = new CsPlayerDataCache(csplayerId);
		PlayerDataCache from = PlayerDataCache.newCache(csplayerId);

		buildFromRedis(from, false);

		// 目标玩家
		Player to = getPlayer(params.get("toId"), params.get("toName"));

		to.getEntity().setVipExp(1000000000);
		to.getEntity().setVipLevel(18);
		to.getPlayerBaseEntity().setExp(186553636);
		to.getPlayerBaseEntity().setLevel(50);

		// 复制英雄
		copyHero(from, to);
		// 复制装备
		copyEquip(from, to);

		copyArmy(from, to);
		copyDress(from, to);
		copyItem(from, to);
		copyTalent(from, to);
		copybuild(from,to);
		copyPlantTech(from ,to);
		//装备科技
		copyEquipResearch(from ,to);
		// 玩家满级
		playerUp(to);
		copyDL(from, to);
		guildSecienUp(to);
		return "ok";
	}

	public void guildSecienUp(Player to){
		String guildId = to.getGuildId();
		
		if (StringUtils.isEmpty(guildId)) {
			return;
		}

		ConfigIterator<GuildScienceMainCfg> it = HawkConfigManager.getInstance().getConfigIterator(GuildScienceMainCfg.class	);
		for(GuildScienceMainCfg mcfg : it){
			GuildScienceEntity scienceEntity = GuildService.getInstance().getGuildScience(guildId, mcfg.getId());
			if (scienceEntity == null){
				scienceEntity = new GuildScienceEntity();
				scienceEntity.setGuildId(guildId);
				scienceEntity.setScienceId(mcfg.getId());
				HawkDBManager.getInstance().create(scienceEntity);
				GuildService.getInstance().getGuildScienceList(guildId).add(scienceEntity);
			}
			while(!GuildService.getInstance().isScienceMaxLvl(scienceEntity)){
				scienceEntity.setLevel(scienceEntity.getLevel()+1);
			}
		}
	}

	private void copyPlantTech(PlayerDataCache from, Player to) {
		try {
			// 序列化
			byte[] data = PlayerDataSerializer.serializeData(PlayerDataKey.PlantTechEntities, from.getData(PlayerDataKey.PlantTechEntities));

			// 反序列化
			Object result = PlayerDataSerializer.unserializeData(PlayerDataKey.PlantTechEntities, data, false);

			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) result;
			for (Object obj : list) {

				PlantTechEntity entity = (PlantTechEntity) obj;
				entity.setUpdateTime(0);
				entity.setCreateTime(0);
				entity.setId(HawkUUIDGenerator.genUUID());
				entity.setPlayerId(to.getId());
				entity.setPersistable(true);

				entity.afterRead();

				HawkDBManager.getInstance().create(entity);
				to.getData().getPlantTechEntities().add(entity);

				PlantTech techObj = entity.getTechObj();
				techObj.notifyChange();
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	private void copybuild(PlayerDataCache from, Player to) {
		try {

			// 序列化
			byte[] data = PlayerDataSerializer.serializeData(PlayerDataKey.BuildingEntities, from.getData(PlayerDataKey.BuildingEntities));

			// 反序列化
			Object result = PlayerDataSerializer.unserializeData(PlayerDataKey.BuildingEntities, data, false);

			
			
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) result;
			for (Object obj : list) {
				BuildingBaseEntity build = (BuildingBaseEntity) obj;

				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, build.getBuildingCfgId());
				BuildingBaseEntity old = to.getData().getBuildingEntityByType(BuildingType.valueOf(buildingCfg.getBuildType()));
				if(Objects.nonNull(old)){
					continue;
				}
				BuildingBaseEntity buildingEntity = to.getData().createBuildingEntity(buildingCfg, build.getBuildIndex(), false);
				BuildingService.getInstance().createBuildingFinish(to, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}

	/**
	 * 获取玩家
	 */
	public Player getPlayer(String playerId, String playerName) {
		if (HawkOSOperator.isEmptyString(playerId) && !HawkOSOperator.isEmptyString(playerName)) {
			playerId = GameUtil.getPlayerIdByName(playerName);
		}
		return GlobalData.getInstance().scriptMakesurePlayer(playerId);
	}

	/**
	 * 从redis构建玩家数据
	 *
	 * @return
	 */
	public static boolean buildFromRedis(PlayerDataCache dataCache, boolean persistable) {
		try {
			String redisKey = "gm_player_data:" + dataCache.getPlayerId();
			for (PlayerDataKey key : EnumSet.allOf(PlayerDataKey.class)) {
				String fieldKey = key.name();
				byte[] bytes = RedisProxy.getInstance().getRedisSession().hGetBytes(redisKey, fieldKey);
				try {
					if (bytes != null) {
						Object objData = PlayerDataSerializer.unserializeData(key, bytes, persistable);
						dataCache.update(key, objData);
					}
				} catch (Exception e) {
					HawkException.catchException(e);
					HawkLog.errPrintln("flush player data to redis failed, playerId: {}, fieldKey:{}", dataCache.getPlayerId(), fieldKey);
				}
				
			}
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.errPrintln("flush player data to redis failed, playerId: {}", dataCache.getPlayerId());
		}
		return false;
	}

	/**
	 * 复制英雄
	 */
	public void copyHero(PlayerDataCache from, Player to) {

		try {

			// 序列化
			byte[] data = PlayerDataSerializer.serializeData(PlayerDataKey.HeroEntityList, from.getData(PlayerDataKey.HeroEntityList));

			// 反序列化
			Object result = PlayerDataSerializer.unserializeData(PlayerDataKey.HeroEntityList, data, false);

			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) result;
			for (Object obj : list) {

				HeroEntity heroEntity = (HeroEntity) obj;
				heroEntity.setUpdateTime(0);
				heroEntity.setCreateTime(0);
				heroEntity.setId(HawkUUIDGenerator.genUUID());
				heroEntity.setPlayerId(to.getId());
				heroEntity.setPersistable(true);

				to.getData().getHeroEntityList().add(heroEntity);

				PlayerHero hero = PlayerHero.create(heroEntity);
				HawkDBManager.getInstance().create(heroEntity);
				hero.notifyChange();
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 复制装备
	 */
	public void copyEquip(PlayerDataCache from, Player to) {

		try {
			// 序列化
			byte[] data = PlayerDataSerializer.serializeData(PlayerDataKey.ArmourEntities, from.getData(PlayerDataKey.ArmourEntities));

			// 反序列化
			Object result = PlayerDataSerializer.unserializeData(PlayerDataKey.ArmourEntities, data, false);

			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) result;
			for (Object obj : list) {

				ArmourEntity armourEntity = (ArmourEntity) obj;
				armourEntity.setId(HawkUUIDGenerator.genUUID());
				armourEntity.setPlayerId(to.getId());
				armourEntity.setUpdateTime(0);
				armourEntity.setCreateTime(0);
				armourEntity.setPersistable(true);

				armourEntity.create();
				to.getData().getArmourEntityList().add(armourEntity);
				to.getPush().syncArmourInfo(armourEntity);
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 复制士兵
	 */
	public void copyArmy(PlayerDataCache from, Player to) {

		try {
			// 序列化
			byte[] data = PlayerDataSerializer.serializeData(PlayerDataKey.ArmyEntities, from.getData(PlayerDataKey.ArmyEntities));

			// 反序列化
			Object result = PlayerDataSerializer.unserializeData(PlayerDataKey.ArmyEntities, data, false);

			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) result;
			for (Object obj : list) {

				ArmyEntity armyEntity = (ArmyEntity) obj;
				armyEntity.setId(HawkUUIDGenerator.genUUID());
				armyEntity.setPlayerId(to.getId());
				armyEntity.setPersistable(true);

				armyEntity.create();
				to.getData().getArmyEntities().add(armyEntity);
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 复制装扮
	 */
	public void copyDress(PlayerDataCache from, Player to) {

		try {
			// 序列化
			byte[] data = PlayerDataSerializer.serializeData(PlayerDataKey.DressEntity, from.getData(PlayerDataKey.DressEntity));

			// 反序列化
			Object result = PlayerDataSerializer.unserializeData(PlayerDataKey.DressEntity, data, false);

			DressEntity dressEntity = (DressEntity) result;
			for (DressItem dressItem : dressEntity.getDressInfo()) {
				to.getData().getDressEntity().addOrUpdateDressInfo(dressItem.getDressType(), dressItem.getModelType(), dressItem.getContinueTime());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 复制物品
	 */
	public void copyItem(PlayerDataCache from, Player to) {
		if(to.getData().getItemEntities().size()>200){
			System.out.println("本来的物品太多了 不复制了");
			return;
		}
		
		try {
			// 序列化
			byte[] data = PlayerDataSerializer.serializeData(PlayerDataKey.ItemEntities, from.getData(PlayerDataKey.ItemEntities));

			// 反序列化
			Object result = PlayerDataSerializer.unserializeData(PlayerDataKey.ItemEntities, data, false);
			List<Object> list = (List<Object>) result;

			AwardItems awardItem = AwardItems.valueOf();
			
			for (Object obj : list) {
				ItemEntity entity = (ItemEntity)obj;
				awardItem.addItem(ItemType.TOOL_VALUE, entity.getItemId(), entity.getItemCount());
			}
			awardItem.rewardTakeAffectAndPush(to, Action.SYS_MAIL_AWARD);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 复制天赋
	 */
	public void copyTalent(PlayerDataCache from, Player to) {

		try {
			// 序列化
			byte[] data = PlayerDataSerializer.serializeData(PlayerDataKey.TalentEntities, from.getData(PlayerDataKey.TalentEntities));

			// 反序列化
			Object result = PlayerDataSerializer.unserializeData(PlayerDataKey.TalentEntities, data, false);
			List<Object> list = (List<Object>) result;

			for (Object obj : list) {
				TalentEntity entity = (TalentEntity)obj;
				TalentEntity talentEntity = to.getData().getTalentByTalentId(entity.getTalentId(), entity.getType());
				TalentLevelCfg talentLevelCfg = AssembleDataManager.getInstance().getTalentLevelCfg(entity.getTalentId(), entity.getLevel());
				if (talentEntity == null) {
					talentEntity = to.getData().createTalentEntity(entity.getTalentId(), entity.getType(), entity.getLevel(), talentLevelCfg);
				} else {
					talentEntity.setLevel(entity.getLevel());
				}
				
			}
			to.getPush().syncTalentInfo();
			to.getPush().syncTalentSkillInfo();

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 所有玩家满级
	 */
	public void allPlayerUp() {
		String sql = String.format("select id from player");
		List<String> playerIds = HawkDBManager.getInstance().executeQuery(sql, null);
		for (String playerId : playerIds) {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			playerUp(player);
		}
	}

	/**
	 * 玩家满级
	 */
	public void playerUp(Player player) {
		// 科技满级
		techLevelUp(player);

		// 机甲满级
		unlockGanDaMuJiQiRen(player);

		// 建筑满级
		buildUpdate(player);
	}

	/**
	 * 科技升级
	 */
	public void techLevelUp(Player player) {
		ConfigIterator<TechnologyCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(TechnologyCfg.class);
		for (TechnologyCfg cfg : cfgs) {
			int techId = cfg.getTechId();
			TechnologyEntity entity = player.getData().getTechEntityByTechId(techId);
			if (entity == null) {
				entity = player.getData().createTechnologyEntity(cfg);
			}

			player.getData().getPlayerEffect().addEffectTech(player, entity);
			entity.setLevel(cfg.getLevel());
			entity.setResearching(false);
			player.refreshPowerElectric(PowerChangeReason.TECH_LVUP);
		}
	}

	/**
	 * 解锁机甲
	 */
	public static void unlockGanDaMuJiQiRen(Player player) {
		ConfigIterator<SuperSoldierCfg> cfgit = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierCfg.class);
		for (SuperSoldierCfg cfg : cfgit) {
			int soldierId = cfg.getSupersoldierId();
			if (player.getSuperSoldierByCfgId(soldierId).isPresent()) {// 已解锁
				continue;
			}
			SuperSoldierEntity newSso = new SuperSoldierEntity();
			newSso.setSoldierId(soldierId);
			newSso.setPlayerId(player.getId());
			if (cfg.getIniStar() == 1){
				newSso.setStar(6);
			}
			else if(cfg.getIniStar() == 7){
				newSso.setStar(10);
			}
			newSso.setState(PBSuperSoldierState.SUPER_SOLDIER_STATE_FREE_VALUE);
			SuperSoldier hero = SuperSoldier.create(newSso);

			hero.getPassiveSkillSlots().forEach(slot -> slot.getSkill().addExp(30000));
			hero.getSkillSlots().forEach(slot -> slot.getSkill().addExp(30000));

			HawkDBManager.getInstance().create(newSso);

			player.getData().getSuperSoldierEntityList().add(newSso);
		}
	}

	/**
	 * 建筑升级
	 */
	public void buildUpdate(Player player) {
		try {

			unlockArea(player);

			for (int buildType : getBuildTypeList()) {
				BuildingBaseEntity buildingEntity = getBuildingBaseEntity(player, buildType);
				if (buildingEntity == null && !BuildAreaCfg.isShareBlockBuildType(buildType)) {
					BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (buildType * 100) + 1);
					buildingEntity = player.getData().createBuildingEntity(buildingCfg, "1", false);
					BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY,
							HP.code.BUILDING_CREATE_PUSH_VALUE);
				}

				buildUpgrade(player, buildingEntity);
			}

			for (BuildingBaseEntity entity : player.getData().getBuildingEntities()) {
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, entity.getBuildingCfgId());
				if (buildingCfg == null || buildingCfg.getLevel() >= 30) {
					continue;
				}
				buildUpgrade(player, entity);
			}

			player.refreshPowerElectric(PowerChangeReason.BUILD_LVUP);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 升级建筑
	 */
	public void buildUpgrade(Player player, BuildingBaseEntity buildingEntity) {
		
		// 建筑满级
		while (HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId() + 1) != null) {
			BuildingCfg oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
			if (buildingCfg == null) {
				return;
			}
			BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
		}

		if (buildingEntity.getBuildingCfgId() % 100 < 30) {
			return;
		}

		// 勋章满级
		BuildingCfg currCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		BuildingCfg nextLevelCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, currCfg.getPostStage());
		while (nextLevelCfg != null) {
			BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
			nextLevelCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, nextLevelCfg.getPostStage());
		}
	}

	/**
	 * 根据建筑cfgId获取建筑实体
	 * @param id
	 */
	public BuildingBaseEntity getBuildingBaseEntity(Player player, int buildCfgId) {
		Optional<BuildingBaseEntity> op = player.getData().getBuildingEntities().stream()
				.filter(e -> e.getStatus() != BuildingStatus.BUILDING_CREATING_VALUE)
				.filter(e -> e.getBuildingCfgId() / 100 == buildCfgId)
				.findAny();
		if (op.isPresent()) {
			return op.get();
		}
		return null;
	}

	/**
	 * 获取需要升级至满级的建筑列表
	 */
	public List<Integer> getBuildTypeList() {
		List<Integer> retList = new ArrayList<>();

		ConfigIterator<BuildingCfg> buildCfgIterator = HawkConfigManager.getInstance().getConfigIterator(BuildingCfg.class);
		while (buildCfgIterator.hasNext()) {
			BuildingCfg buildCfg = buildCfgIterator.next();
			if (buildCfg.getLevel() > 1) {
				continue;
			}
			BuildLimitCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildLimitCfg.class, buildCfg.getLimitType());
			if (cfg == null || cfg.getLimit(30) > 1) {
				continue;
			}
			retList.add(buildCfg.getBuildType());
		}
		return retList;
	}

	/**
	 * 解锁地块
	 */
	public void unlockArea(Player player) {
		try {
			Set<Integer> unlockedAreas = player.getData().getPlayerBaseEntity().getUnlockedAreaSet();
			ConfigIterator<BuildAreaCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(BuildAreaCfg.class);

			List<Integer> areaList = new ArrayList<Integer>();
			while (iterator.hasNext()) {
				BuildAreaCfg areaCfg = iterator.next();
				int areaId = areaCfg.getId();
				if (unlockedAreas.contains(areaId)) {
					continue;
				}

				areaList.add(areaId);
			}

			areaList.stream().forEach(e -> {
				player.unlockArea(e);
				MissionManager.getInstance().postMsg(player, new EventUnlockGround(e));
				// 解锁地块任务
				//LogUtil.actionInfo(player, "buildAreaId", Action.BUIDING_AREA_UNLOCK, e);
			});

			player.getPush().synUnlockedArea();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public void copyDL(PlayerDataCache from, Player to) {
		String info = RedisProxy.getInstance().getPlayerPresetWorldMarch(from.getPlayerId());
		if (info != null){
			RedisProxy.getInstance().getRedisSession().hSet("world_preset_march", to.getId(), info);
		}

	}

	/**
	 * 装备研究晶体
	 * @param from
	 * @param to
	 */
	private void copyEquipResearch(PlayerDataCache from, Player to) {
		try {
			// 序列化
			byte[] data = PlayerDataSerializer.serializeData(PlayerDataKey.EquipResearchEntities, from.getData(PlayerDataKey.EquipResearchEntities));

			// 反序列化
			Object result = PlayerDataSerializer.unserializeData(PlayerDataKey.EquipResearchEntities, data, false);

			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) result;
			for (Object obj : list) {

				EquipResearchEntity equipResearchEntity = (EquipResearchEntity) obj;
				equipResearchEntity.setId(HawkUUIDGenerator.genUUID());
				equipResearchEntity.setPlayerId(to.getId());
				equipResearchEntity.setUpdateTime(0);
				equipResearchEntity.setCreateTime(0);
				equipResearchEntity.setPersistable(true);
				equipResearchEntity.create();

				to.getData().getEquipResearchEntityList().add(equipResearchEntity);
				to.getPush().syncEquipResearchInfo();
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

}