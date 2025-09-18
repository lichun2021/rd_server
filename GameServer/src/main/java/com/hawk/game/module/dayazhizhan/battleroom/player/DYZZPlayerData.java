package com.hawk.game.module.dayazhizhan.battleroom.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.entifytype.EntityType;
import org.hawk.os.HawkException;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.FoggyHeroCfg;
import com.hawk.game.config.SuperSoldierCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.SuperSoldierEntity;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.dayazhizhan.battleroom.entity.DYZZArmyEntity;
import com.hawk.game.module.dayazhizhan.battleroom.extry.DYZZGamer;
import com.hawk.game.module.staffofficer.StaffOfficerSkillCollection;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PowerElectric;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;

public class DYZZPlayerData extends IDYZZPlayerData {
	private IDYZZPlayerEffect playerEffect;
	/** 兵数据副本 */
	private List<ArmyEntity> armyEntities;
	/** 队列 */
	private List<QueueEntity> queueEntities;

	private List<SuperSoldierEntity> superSlldiers;
	private List<HeroEntity> heros;

	private List<BuildingBaseEntity> buildings;
	// private List<StatusDataEntity> statusDataEntities;

	private DYZZPlayerData(IDYZZPlayer parent) {
		super(parent);
	}

	@Override
	public DYZZPlayer getParent() {
		return (DYZZPlayer) super.getParent();
	}

	public static DYZZPlayerData valueOf(DYZZPlayer player) {
		PlayerData source = player.getSource().getData();
		DYZZPlayerData result = new DYZZPlayerData(player);
		result.playerId = source.getPlayerId();
		result.dataCache = source.getDataCache();

		result.armyEntities = new CopyOnWriteArrayList<>();
		// 队列
		result.queueEntities = new CopyOnWriteArrayList<>();
		result.superSlldiers = new CopyOnWriteArrayList<>();
		result.heros = new CopyOnWriteArrayList<>();
		result.buildings = new CopyOnWriteArrayList<>();

		DYZZGamer gamer = player.getParent().getExtParm().getGamer(player.getId());
		// army副本
		Map<Integer, DYZZArmyEntity> armyMap = new HashMap<>();
		ConfigIterator<BattleSoldierCfg> armyit = HawkConfigManager.getInstance().getConfigIterator(BattleSoldierCfg.class);
		for (BattleSoldierCfg arCfg : armyit) {
			DYZZArmyEntity army = new DYZZArmyEntity();
			army.setId(HawkUUIDGenerator.genUUID());
			army.setPlayerId(source.getPlayerId());
			army.setArmyId(arCfg.getId());
			army.setPersistable(false);
			army.setEntityType(EntityType.TEMPORARY);
			armyMap.put(army.getArmyId(), army);
		}
		for (ArmyEntity army : source.getArmyEntities()) {
			DYZZArmyEntity armyentity = armyMap.get(army.getArmyId());
			armyentity.setId(army.getId());
		}
		for (ArmyInfo army : gamer.getArmys()) {
			DYZZArmyEntity armyentity = armyMap.get(army.getArmyId());
			armyentity.addFree(army.getFreeCnt());
			armyentity.setMaxFree(army.getFreeCnt());
		}
		result.armyEntities = new ArrayList<>(armyMap.values());

		// 机甲
		ConfigIterator<SuperSoldierCfg> sit = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierCfg.class);
		for (SuperSoldierCfg scfg : sit) {
			if (scfg.getPreSupersoldierId() <= 0) {
				continue;
			}
			try {
				DYZZSuperSoldier hero = DYZZSuperSoldier.create(player, scfg);
				result.getSuperSoldierEntityList().add(hero.getDBEntity());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 英雄
		for (int heroId : gamer.getFoggyHeros()) {
			try {
				FoggyHeroCfg cfg = HawkConfigManager.getInstance().getConfigByKey(FoggyHeroCfg.class, heroId);
				DYZZPlayerHero hero = DYZZPlayerHero.create(player, cfg);
				result.getHeroEntityList().add(hero.getHeroEntity());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		// 建筑
		for (BuildingBaseEntity build : source.getBuildingEntities()) {
			BuildingBaseEntity entity = new BuildingBaseEntity();
			entity.setPersistable(false);
			entity.setEntityType(EntityType.TEMPORARY);
			entity.setId(build.getId());
			entity.setPlayerId(player.getId());
			entity.setBuildingCfgId(build.getBuildingCfgId());
			entity.setType(build.getType());
			entity.setBuildIndex(build.getBuildIndex());
			entity.setResUpdateTime(build.getResUpdateTime());
			entity.setHp(build.getHp());
			entity.setStatus(BuildingStatus.COMMON_VALUE);
			entity.setLastResCollectTime(Long.MAX_VALUE);
//			if(build.getType() == Const.BuildingType.CONSTRUCTION_FACTORY_VALUE){
//				entity.setBuildingCfgId(20103800);
//			}
			result.buildings.add(entity);
		}

		BuildingBaseEntity plantHospital = result.getBuildingEntityByType(BuildingType.PLANT_HOSPITAL);
		if (Objects.isNull(plantHospital)) {
			BuildingBaseEntity entity = new BuildingBaseEntity();
			entity.setPersistable(false);
			entity.setEntityType(EntityType.TEMPORARY);
			entity.setId(HawkUUIDGenerator.genUUID());
			entity.setPlayerId(player.getId());
			entity.setBuildingCfgId(223501);
			entity.setType(BuildingType.PLANT_HOSPITAL_VALUE);
			entity.setBuildIndex("1");
			entity.setStatus(BuildingStatus.COMMON_VALUE);
			result.buildings.add(entity);
		}

		// buff
		// result.statusDataEntities = new ArrayList<>();

		// effect 对象
		result.playerEffect = new DYZZPlayerEffect(result);
		result.playerEffect.setParent(player);
		result.lockOriginalData();
		return result;
	}

	@Override
	public StaffOfficerSkillCollection getStaffOffic(){
		return StaffOfficerSkillCollection.defaultInst;
	}
	
	public void lockOriginalData() {
		// 锁定原始数据
		getSource().getDataCache().lockKey(PlayerDataKey.ArmyEntities, getArmyEntities());
		getSource().getDataCache().lockKey(PlayerDataKey.QueueEntities, getQueueEntities());
		getSource().getDataCache().lockKey(PlayerDataKey.SuperSoldierEntityList, getSuperSoldierEntityList());
		getSource().getDataCache().lockKey(PlayerDataKey.HeroEntityList, getHeroEntityList());
		getSource().getDataCache().lockKey(PlayerDataKey.BuildingEntities, getBuildingEntitiesIgnoreStatus());
		// getSource().getDataCache().lockKey(PlayerDataKey.StatusDataEntities);
	}

	public void unLockOriginalData() {
		// 锁定原始数据
		getSource().getDataCache().unLockKey(PlayerDataKey.ArmyEntities);
		getSource().getDataCache().unLockKey(PlayerDataKey.QueueEntities);
		getSource().getDataCache().unLockKey(PlayerDataKey.SuperSoldierEntityList);
		getSource().getDataCache().unLockKey(PlayerDataKey.HeroEntityList);
		getSource().getDataCache().unLockKey(PlayerDataKey.BuildingEntities);
	}
	
	/**
	 * 获取玩家战力电力
	 */
	public PowerElectric getPowerElectric() {
		if (powerElectric == null) {
			powerElectric = new DYZZPowerElectric(this);
		}
		return powerElectric;
	}

	// @Override
	// public List<StatusDataEntity> getStatusDataEntities() {
	// return statusDataEntities;
	// }

	public int getAchievePoint() {
		return 0;
	}

	@Override
	public List<SuperSoldierEntity> getSuperSoldierEntityList() {
		return superSlldiers;
	}

	@Override
	public List<HeroEntity> getHeroEntityList() {
		return heros;
	}

	@Override
	public IDYZZPlayerEffect getPlayerEffect() {
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
	
	
	public List<BuildingBaseEntity> getBuildingListByType(BuildingType type) {
		if (type == BuildingType.PRISM_TOWER) {
			return Collections.emptyList();
		}
		return super.getBuildingListByType(type);
	}

	@Override
	public List<BuildingBaseEntity> getBuildingEntitiesIgnoreStatus() {
		return buildings;
	}

	@Override
	public ArmyEntity getArmyEntity(int armyId) {
		for (ArmyEntity armyEntity : getArmyEntities()) {
			if (armyEntity.getArmyId() == armyId) {
				return armyEntity;
			}
		}
		DYZZArmyEntity armyEntity = new DYZZArmyEntity();
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
	public void setPrimitivePfIcon(String primitivePfIcon) {
		// TODO Auto-generated method stub
		getSource().setPrimitivePfIcon(primitivePfIcon);
	}

	public void setPlayerEffect(IDYZZPlayerEffect playerEffect) {
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
