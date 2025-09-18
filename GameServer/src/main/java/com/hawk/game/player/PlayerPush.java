package com.hawk.game.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.hawk.activity.type.impl.newFirstRecharge.cfg.NewFirstRechargeKVCfg;
import com.hawk.common.CommonConst.ServerType;
import com.hawk.common.ServerInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.activity.ActivityConfigLoader;
import com.hawk.game.activity.impl.backflow.BackFlowService;
import com.hawk.game.battle.BattleService;
import com.hawk.game.cfgElement.ArmourStarExplores;
import com.hawk.game.city.CityManager;
import com.hawk.game.config.AllianceFileCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.ControlProperty;
import com.hawk.game.config.CustomKeyCfg;
import com.hawk.game.config.EffectCfg;
import com.hawk.game.config.HeroCfg;
import com.hawk.game.config.HeroSkinCfg;
import com.hawk.game.config.ManhattanSWCfg;
import com.hawk.game.config.VipShopCfg;
import com.hawk.game.config.WarCollegeTimeControlCfg;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.data.BubbleRewardInfo;
import com.hawk.game.data.ProtectSoldierInfo;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.DailyDataEntity;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.EquipEntity;
import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.entity.GlobalBuffEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.MissionEntity;
import com.hawk.game.entity.PlayerMonsterEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.StoryMissionEntity;
import com.hawk.game.entity.TalentEntity;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.manor.GuildBuildingStat;
import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.guild.manor.ManorBastionStat;
import com.hawk.game.guild.manor.building.IGuildBuilding;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.college.cfg.CollegeConstCfg;
import com.hawk.game.module.mechacore.obj.MechaAddProductInfo;
import com.hawk.game.msg.PlayerEffectChangeMsg;
import com.hawk.game.player.equip.EquipSlot;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourAllInfoResp;
import com.hawk.game.protocol.Armour.ArmourDeleteResp;
import com.hawk.game.protocol.Armour.ArmourInfo;
import com.hawk.game.protocol.Armour.ArmourSingleUpdate;
import com.hawk.game.protocol.Armour.ArmourStarShowInfo;
import com.hawk.game.protocol.Armour.ArmourSuit;
import com.hawk.game.protocol.Armour.ArmourSuitInfoResp;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Armour.ArmourTechAwardInfo;
import com.hawk.game.protocol.Armour.ArmourTechInfo;
import com.hawk.game.protocol.Armour.ArmourTechInfoPush;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Army.ArmyInfoPB;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Army.HPArmyInfoSync;
import com.hawk.game.protocol.Army.ProtectSoldierPushPB;
import com.hawk.game.protocol.Building.CityDefPB;
import com.hawk.game.protocol.Building.HPBuildingInfoSync;
import com.hawk.game.protocol.Building.PushBuildingStatus;
import com.hawk.game.protocol.Common.HpRedPointSync;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.Common.RedType;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.StateType;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.Dress.DressEditData;
import com.hawk.game.protocol.Dress.DressEditDataSync;
import com.hawk.game.protocol.Dress.DressEditType;
import com.hawk.game.protocol.Dress.DressSendGiftTimesInfo;
import com.hawk.game.protocol.Dress.DressSendGiftTimesInfoPush;
import com.hawk.game.protocol.Dress.DressSendProtectInfo;
import com.hawk.game.protocol.Dress.DressSendProtectInfoSync;
import com.hawk.game.protocol.Dress.DressSingleInfo;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.protocol.Dress.HPDressInfoSync;
import com.hawk.game.protocol.Equip.CommanderEquipSync;
import com.hawk.game.protocol.Equip.HPEquipInfo;
import com.hawk.game.protocol.Friend.GiftReceiveTimesPush;
import com.hawk.game.protocol.Friend.LoveAddPush;
import com.hawk.game.protocol.Friend.RecommendFriendsResp;
import com.hawk.game.protocol.Friend.StrangerMsg;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildManager.GuildScoreSync;
import com.hawk.game.protocol.GuildManager.GuildSign;
import com.hawk.game.protocol.GuildManager.HPGuildInfoSync;
import com.hawk.game.protocol.GuildWar.HPGuildWarCountPush;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.PBHeroListPush;
import com.hawk.game.protocol.Item.HPGachaModuleInfo;
import com.hawk.game.protocol.Item.HPItemInfoSync;
import com.hawk.game.protocol.Item.HPSyncGachaInfoResp;
import com.hawk.game.protocol.Item.VipBoxInfoPB;
import com.hawk.game.protocol.Item.VipExclusiveBox;
import com.hawk.game.protocol.Item.VipShopItem;
import com.hawk.game.protocol.Item.VipShopItemInfo;
import com.hawk.game.protocol.Mail.HPDelMailByIdResp;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Mission.MissionListRes;
import com.hawk.game.protocol.Newly.HPGenNewlyDataRes;
import com.hawk.game.protocol.Player.EffectPB;
import com.hawk.game.protocol.Player.HPPlayerEffectSync;
import com.hawk.game.protocol.Player.HPPlayerInfoSync;
import com.hawk.game.protocol.Player.HPShieldPlayerInfoSync;
import com.hawk.game.protocol.Player.PlayerDailyDataMsg;
import com.hawk.game.protocol.Player.PlayerFlagSyn;
import com.hawk.game.protocol.Player.PlayerInfo;
import com.hawk.game.protocol.Player.PlayerMilitaryRankAward;
import com.hawk.game.protocol.Player.ShieldPlayerInfo;
import com.hawk.game.protocol.Player.StateInfoSync;
import com.hawk.game.protocol.Player.SynGlobalBuff;
import com.hawk.game.protocol.Player.SynPlayerDailyData;
import com.hawk.game.protocol.Player.UpdatePlayerDiamond;
import com.hawk.game.protocol.Queue.HPQueueInfoSync;
import com.hawk.game.protocol.Queue.PaidQueuePB;
import com.hawk.game.protocol.Recharge.HasFirstRechargeSync;
import com.hawk.game.protocol.Reward.BubbleRewardDetailPB;
import com.hawk.game.protocol.Reward.BubbleRewardInfoPB;
import com.hawk.game.protocol.StoryMission.StoryMissionPage;
import com.hawk.game.protocol.StoryMission.StoryMissionRewRes;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierListPush;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierSkinInfoResp;
import com.hawk.game.protocol.SysProtocol.ActivityCfgVersionInfo;
import com.hawk.game.protocol.SysProtocol.ActivityCfgVersionInfoPush;
import com.hawk.game.protocol.SysProtocol.HPClientCfgResp;
import com.hawk.game.protocol.SysProtocol.HPCustomDataSync;
import com.hawk.game.protocol.SysProtocol.KVData;
import com.hawk.game.protocol.SysProtocol.PersonalProtectSwitchPB;
import com.hawk.game.protocol.Talent.HPTalentInfoSync;
import com.hawk.game.protocol.Talent.HPTalentSkillSync;
import com.hawk.game.protocol.Talent.TalentSkillInfo;
import com.hawk.game.protocol.Technology.HPTechSkillSync;
import com.hawk.game.protocol.Technology.HPTechnologySync;
import com.hawk.game.protocol.Technology.TechSkillInfo;
import com.hawk.game.protocol.World.HPWorldFavoriteSync;
import com.hawk.game.protocol.World.KillMonsterPush;
import com.hawk.game.protocol.World.MaxMonsterLvlResp;
import com.hawk.game.protocol.World.MonsterDropLimitData;
import com.hawk.game.protocol.World.MonsterKillData;
import com.hawk.game.protocol.World.MonsterKillDataSync;
import com.hawk.game.protocol.World.PushTreasureHuntEvent;
import com.hawk.game.protocol.World.TreasureHuntType;
import com.hawk.game.protocol.World.UnlockedMarchEmoticonSync;
import com.hawk.game.protocol.World.WorldFavoritePB;
import com.hawk.game.protocol.World.WorldInfoPush;
import com.hawk.game.protocol.World.WorldMarchDeletePush;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMoveCityResp;
import com.hawk.game.service.BuffService;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.mail.PersonalMailService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.MissionState;
import com.hawk.game.util.GsConst.QueueReusage;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldFoggyFortressService;
import com.hawk.game.world.service.WorldMonsterService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.sdk.SDKConst.UserType;
import com.hawk.sdk.config.TencentCfg;
import com.hawk.serialize.string.SerializeHelper;

public class PlayerPush {
	/**
	 * 玩家对象
	 */
	protected Player player;
	/**
	 * 保存上一次推送的playerInfoBuilder 做增量更新
	 */
	private PlayerInfo.Builder playerInfoBuilder;
	/**
	 * Player$PlayerInfo的fieldDescriptor;
	 */
	private static List<FieldDescriptor> fieldDescriptorList = null; 

	/**
	 * 构造
	 *
	 * @param player
	 */
	public PlayerPush(Player player) {
		this.player = player;
	}

	/**
	 * 获取玩家数据
	 *
	 * @return
	 */
	public PlayerData getData() {
		return player.getData();
	}

	/**
	 * 同步玩家信息
	 */
	public void syncPlayerInfo() {
		HPPlayerInfoSync.Builder builder = HPPlayerInfoSync.newBuilder();
		PlayerInfo.Builder newBuilder = BuilderUtil.genPlayerBuilder(getData());
		newBuilder.setIpBelongsAddr(player.getIpBelongsAddr());
		//newBuilder.setCredit(player.getCredit()); //平台侧将信用分系统停掉了，客户端需要检查下，如果服务器不发这个或发一个0，是否有影响
		newBuilder.setLmjyState(player.getLmjyState() == null ? 0 : player.getLmjyState().intValue());
		newBuilder.setLmjyEnterCd(player.lmjyCD);
		if (player.getTBLYState() != null) {
			newBuilder.setLmjyState(player.getTBLYState().intValue());
		}
		if(player.getSwState()!=null){
			newBuilder.setLmjyState(player.getSwState().intValue());
		}
		if(player.getCYBORGState()!=null){
			newBuilder.setLmjyState(player.getCYBORGState().intValue());
		}
		if(player.getDYZZState()!=null){
			newBuilder.setLmjyState(player.getDYZZState().intValue());
		}
		if(player.getYQZZState()!=null){
			newBuilder.setLmjyState(player.getYQZZState().intValue());
		}
		if(player.getXhjzState()!=null){
			newBuilder.setLmjyState(player.getXhjzState().intValue());
		}
		if(player.getFgylState()!=null){
			newBuilder.setLmjyState(player.getFgylState().intValue());
		}
		if(player.getXQHXState()!=null){
			newBuilder.setLmjyState(player.getXQHXState().intValue());
		}
		newBuilder.setIsRegisterPuidCtrlPlayer(GameUtil.isOBPuidCtrlPlayer(player.getOpenId()));
		PlayerInfo.Builder oldBuilder = playerInfoBuilder;
		PlayerInfo.Builder updateBuilder = null;

		if (oldBuilder != null) {
			updateBuilder = PlayerInfo.newBuilder();
			//之前的写法会导致频繁的拷贝
			if (fieldDescriptorList == null) {
				fieldDescriptorList = PlayerInfo.Builder.getDescriptor().getFields();				
			}
			Object oldObject = null;
			Object newObject = null;
			for (FieldDescriptor fd : fieldDescriptorList) {
				oldObject = oldBuilder.getField(fd);
				newObject = newBuilder.getField(fd);
				if (newObject == null)  {
					continue;
				}
				if (oldObject != null) {
					if (oldObject.equals(newObject)) {
						continue;
					}
				}
				updateBuilder.setField(fd, newObject);
			}					
		} else {
			updateBuilder = newBuilder;
		}

		this.playerInfoBuilder = newBuilder;

		builder.setPlayerInfo(updateBuilder);

		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_INFO_SYNC_S, builder));
	}

	/**
	 * 同步世界信息
	 */
	public void syncPlayerWorldInfo() {
		int[] posInfo = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		WorldInfoPush.Builder worldBuilder = WorldInfoPush.newBuilder();
		worldBuilder.setTargetX(posInfo[0]);
		worldBuilder.setTargetY(posInfo[1]);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_PLAYER_WORLD_INFO_PUSH, worldBuilder));
	}

	/**
	 * 同步玩家state同步
	 */
	public void syncPlayerStatusInfo(boolean isLogin, StatusDataEntity... entities) {
		StateInfoSync.Builder builder = StateInfoSync.newBuilder();
		if (entities.length == 0) {
			List<StatusDataEntity> entityList = player.getData().getStatusDataEntities();
			entities = entityList.toArray(entities);
		}

		long now = HawkTime.getMillisecond();
		for (StatusDataEntity entity : entities) {
			if (isLogin && entity.getType() == StateType.BUFF_STATE_VALUE && entity.getEndTime() < now) {
				continue;
			}

			builder.addStateInfos(BuilderUtil.genStateInfoBuilder(entity));
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.STATE_INFO_SYNC_S_VALUE, builder));
	}

	/**
	 * 同步玩家作用号数据
	 * 
	 * @param types
	 *            作用号列表，为空推送所有作用号数据
	 */
	public void syncPlayerEffect(EffType... types) {
		// 需要同步的资源田增产专项作用号，当此方法同步的是所有作用号时，就不同步资源田增产专项作用号了
		List<Integer> effTypes = new ArrayList<>();

		if (types == null || types.length == 0) {
			effTypes = null;
			types = EffType.values();
		} else { // 游戏中变更
			HawkTaskManager.getInstance().postMsg(player.getXid(), PlayerEffectChangeMsg.valueOf(types));
		}

		List<Integer> allResOutputBuffList = ConstProperty.getInstance().getAllResOutputBuffList();
		HPPlayerEffectSync.Builder builder = HPPlayerEffectSync.newBuilder();
		for (EffType effType : types) {
			if (effType == null) {
				continue;
			}
			if (EffectCfg.effectNotVisible(effType)) {
				continue;
			}

			if (effTypes != null && allResOutputBuffList.contains(effType.getNumber())) {
				effTypes.add(effType.getNumber());
			}

			List<StatusDataEntity> entityList = player.getData().getStatusListById(effType.getNumber());
			if (entityList.isEmpty()) {
				int effVal = player.getData().getEffVal(effType);
				EffectPB.Builder effPB = EffectPB.newBuilder();
				effPB.setEffId(effType.getNumber());
				effPB.setEffVal(effVal);
				builder.addEffList(effPB);
				continue;
			}

			for (StatusDataEntity entity : entityList) {
				int effVal = player.getData().getEffVal(effType, entity.getTargetId());
				EffectPB.Builder effPB = EffectPB.newBuilder();
				effPB.setEffId(effType.getNumber());
				effPB.setEffVal(effVal);
				effPB.setTargetId(entity.getTargetId());
				builder.addEffList(effPB);
			}
		}

		if (builder.getEffListCount() <= 0) {
			return;
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_EFFECT_INFO_SYNC_S_VALUE, builder));
		notifyWorldPointEffectChange(types);

		// 同步资源田专项作用号
		if (effTypes != null && !effTypes.isEmpty()) {
			player.getData().getPlayerEffect().syncResOutputBuff(player, effTypes);
		}
	}

	/**
	 * 通知城点作用号变更
	 * 
	 * @param types
	 */
	private void notifyWorldPointEffectChange(EffType... types) {
//		boolean needPointUpdate = false;
//		Map<Integer, BaseShowItem> updateMap = new HashMap<>();
//
//		List<Integer> baseShow = WorldMapConstProperty.getInstance().getBaseShow();
//		for (EffType effType : types) {
//			if (baseShow.contains(effType.getNumber())) {
//				needPointUpdate = true;
//				StatusDataEntity state = player.getData().getStateById(effType.getNumber(), StateType.BUFF_STATE_VALUE);
//				if (state != null) {
//					updateMap.put(effType.getNumber(),
//							new BaseShowItem(state.getStatusId(), state.getStartTime(), state.getEndTime()));
//				}
//			}
//		}
//
//		if (needPointUpdate) {
//			WorldPointService.getInstance().updateBaseShow(player.getId(), updateMap);
//			// 同步城点状态
//			WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
//			if (worldPoint != null) {
//				WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
//			}
//		}
	}

	public void synGlobalBuffInfo() {
		Map<Integer, GlobalBuffEntity> globalBuffEntitys = BuffService.getInstance().getBuffMap();
		SynGlobalBuff.Builder sbuilder = SynGlobalBuff.newBuilder();
		for (GlobalBuffEntity globalBuffEntity : globalBuffEntitys.values()) {
			sbuilder.addStateInfos(BuilderUtil.genStateInfoBuilder(globalBuffEntity));
		}

//		player.sendProtocol(HawkProtocol.valueOf(HP.code.SYN_GLOBAL_BUFF_VALUE, sbuilder));
	}

	/**
	 * 同步物品信息
	 */
	public void syncItemInfo() {
		HPItemInfoSync.Builder builder = HPItemInfoSync.newBuilder();
		for (ItemEntity itemEntity : getData().getItemEntities()) {
			if (itemEntity.getItemCount() > 0 && !itemEntity.isInvalid()) {
				builder.addItemInfos(BuilderUtil.genItemBuilder(itemEntity));
			}
		}
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.ITEM_INFO_SYNC_S, builder);
		player.sendProtocol(protocol);
	}

	public void syncItemInfo(String... ids) {
		HPItemInfoSync.Builder builder = HPItemInfoSync.newBuilder();
		for (String id : ids) {
			for (ItemEntity itemEntity : getData().getItemEntities()) {
				if ((id.length() <= 0 || id.equals(itemEntity.getId())) && itemEntity.getItemCount() > 0
						&& !itemEntity.isInvalid()) {
					builder.addItemInfos(BuilderUtil.genItemBuilder(itemEntity));
				}
			}
		}
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.ITEM_INFO_SYNC_S, builder);
		player.sendProtocol(protocol);
	}

	/**
	 * 同步天赋信息
	 */
	public void syncTalentInfo() {
		HPTalentInfoSync.Builder builder = HPTalentInfoSync.newBuilder();
		List<TalentEntity> talentEntities = getData().getTalentEntities();
		for (TalentEntity talentEntity : talentEntities) {
			if (talentEntity.getLevel() > 0 && !talentEntity.isInvalid()) {
				builder.addTalentInfos(BuilderUtil.genTalentBuilder(talentEntity));
			}
		}
		builder.setType(player.getEntity().getTalentType());
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PLAYER_TALENT_SYNC_S_VALUE, builder);
		player.sendProtocol(protocol);
	}

	/**
	 * 同步天赋技能信息
	 */
	public void syncTalentSkillInfo() {
		HPTalentSkillSync.Builder builder = HPTalentSkillSync.newBuilder();
		List<TalentEntity> skills = player.getData().getAllTalentSkills();
		for (TalentEntity skill : skills) {
			TalentSkillInfo.Builder skillInfo = TalentSkillInfo.newBuilder();
			skillInfo.setSkillId(skill.getSkillId());
			skillInfo.setCdEndTime(skill.getSkillRefTime());
			skillInfo.setCastSkillTime(skill.getCastSkillTime());
			builder.addSkillInfo(skillInfo);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TALENT_SKILLS_SYNC_VALUE, builder));
	}

	/**
	 * 同步全量建筑信息
	 */
	public void syncBuildingEntityInfo() {
		syncBuildingEntityInfo(getData().getBuildingEntitiesIgnoreStatus());
	}

	/**
	 * 同步建筑信息
	 * 
	 * @param buildingList
	 */

	public void syncBuildingEntityInfo(List<BuildingBaseEntity> buildingList) {
		HPBuildingInfoSync.Builder builder = HPBuildingInfoSync.newBuilder();
		for (BuildingBaseEntity buildingEntity : buildingList) {
			if (!buildingEntity.isInvalid()) {
				builder.addBuildings(BuilderUtil.genBuildingBuilder(player, buildingEntity));
			}
		}
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PLAYER_BUILDING_SYNC_S, builder);
		player.sendProtocol(protocol);
	}

	/**
	 * 同步已解锁区块数据
	 */
	public void synUnlockedArea() {
		player.sendProtocol(
				HawkProtocol.valueOf(HP.code.UNLOCKED_AREA_PUSH_VALUE, BuilderUtil.getUnlockedAreaBuilder(player)));
	}

	/**
	 * 同步队列信息
	 */
	public void syncQueueEntityInfo() {
		HPQueueInfoSync.Builder builder = HPQueueInfoSync.newBuilder();
		for (QueueEntity queueEntity : getData().getQueueEntities()) {
			if (!queueEntity.isInvalid() && queueEntity.getReusage() != QueueReusage.FREE.intValue()) {
				builder.addQueues(BuilderUtil.genQueueBuilder(queueEntity));
			}
		}
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PLAYER_QUEUE_SYNC_S, builder);
		player.sendProtocol(protocol);
	}

	/**
	 * 同步科技信息
	 */
	public void syncTechnologyInfo() {
		HPTechnologySync.Builder builder = HPTechnologySync.newBuilder();
		for (TechnologyEntity entity : getData().getTechnologyEntities()) {
			if (entity.getLevel() != 0) {
				builder.addTechId(entity.getCfgId());
			}
		}
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PLAYER_TECHNOLOGY_S, builder);
		player.sendProtocol(protocol);
	}

	/**
	 * 同步科技技能信息
	 */
	public void syncTechSkillInfo() {
		HPTechSkillSync.Builder builder = HPTechSkillSync.newBuilder();
		Map<Integer, Integer> skill2Tech = AssembleDataManager.getInstance().getSkill2TechMap();
		for (Entry<Integer, Integer> entry : skill2Tech.entrySet()) {
			TechnologyEntity entity = player.getData().getTechEntityByTechId(entry.getValue());
			if (entity == null || entity.getLevel() < 1) {
				continue;
			}
			TechSkillInfo.Builder skillInfo = TechSkillInfo.newBuilder();
			skillInfo.setCdEndTime(entity.getSkillCd());
			skillInfo.setSkillId(entry.getKey());
			builder.addSkillInfo(skillInfo);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TECH_SKILL_SYNC_S, builder));
	}

	/**
	 * 同步科技等级
	 * 
	 * @param techId
	 */
	public void syncTechnologyLevelUpFinish(int techId) {
		HPTechnologySync.Builder builder = HPTechnologySync.newBuilder();
		builder.addTechId(techId);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PLAYER_TECHNOLOGY_S, builder);
		player.sendProtocol(protocol);
	}
	
	/**
	 * 同步远征科技等级
	 * 
	 * @param techId
	 */
	public void syncCrossTechLevelUpFinish(int techId) {
		HPTechnologySync.Builder builder = HPTechnologySync.newBuilder();
		builder.addTechId(techId);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PLAYER_CROSS_TECH_SYNC_S, builder);
		player.sendProtocol(protocol);
	}

	/**
	 * 同步军队信息
	 * 
	 * @param cause
	 * @param armyIds
	 */
	public void syncArmyInfo(ArmyChangeCause cause, Integer... armyIds) {
		HPArmyInfoSync.Builder builder = HPArmyInfoSync.newBuilder();
		if (armyIds != null && armyIds.length > 0) {
			for (int armyId : armyIds) {
				for (ArmyEntity army : getData().getArmyEntities()) {
					if (armyId == army.getArmyId() && !army.isInvalid()) {
						builder.addArmyInfos(army.toProtoBuilder());
						break;
					}
				}
			}
		} else {
			for (ArmyEntity army : getData().getArmyEntities()) {
				builder.addArmyInfos(army.toProtoBuilder());
			}
		}
		builder.setCause(cause);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PLAYER_ARMY_S, builder);
		player.sendProtocol(protocol);
	}

	/**
	 * 同步军队信息
	 * 
	 * @param cause
	 * @param soldierAdds
	 */
	public void syncArmyInfo(ArmyChangeCause cause, Map<Integer, Integer> soldierAdds) {
		if (soldierAdds == null || soldierAdds.size() == 0) {
			return;
		}

		HPArmyInfoSync.Builder builder = HPArmyInfoSync.newBuilder();
		for (int armyId : soldierAdds.keySet()) {
			for (ArmyEntity army : getData().getArmyEntities()) {
				if (armyId == army.getArmyId() && !army.isInvalid()) {
					ArmyInfoPB.Builder armyInfo = army.toProtoBuilder();
					armyInfo.setAddCount(soldierAdds.get(armyId));
					builder.addArmyInfos(armyInfo);
					break;
				}
			}
		}
		builder.setCause(cause);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PLAYER_ARMY_S, builder);
		player.sendProtocol(protocol);
	}

	/**
	 * 推送建筑状态的变化
	 * 
	 * @param buildingEntity
	 *            建筑实体
	 * @param status
	 *            建筑的状态
	 */
	public void pushBuildingStatus(BuildingBaseEntity buildingEntity, BuildingStatus status) {
		buildingEntity.setStatus(status.getNumber());
		PushBuildingStatus.Builder push = PushBuildingStatus.newBuilder();
		push.setBuildId(buildingEntity.getId());
		push.setStatus(status);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.BUILDING_STATUS_CHANGE_PUSH, push);
		player.sendProtocol(protocol);
	}

	/**
	 * 同步世界收藏夹
	 * 
	 * @return
	 */
	public void syncWorldFavorite() {
		HPWorldFavoriteSync.Builder favoriteBuilder = HPWorldFavoriteSync.newBuilder();
		List<WorldFavoritePB.Builder> favoriteList = LocalRedis.getInstance().getWorldFavorite(player.getId());
		if (favoriteList != null) {
			for (WorldFavoritePB.Builder favorite : favoriteList) {
				favoriteBuilder.addFavorites(favorite);
			}
		}
		List<WorldFavoritePB> guildFavouriteList = buildGuildFavourite();
		favoriteBuilder.addAllFavorites(guildFavouriteList);
		favoriteBuilder.setSynType(0);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_FAVORITE_SYNC_S, favoriteBuilder);
		player.sendProtocol(protocol);
	}

	@SuppressWarnings("unchecked")
	private List<WorldFavoritePB> buildGuildFavourite() {
		String guildId = player.getGuildId();
		//没有工会或者是跨服玩家.
		if (HawkOSOperator.isEmptyString(guildId) || player.isCsPlayer()) {
			return Collections.EMPTY_LIST;
		}
		List<WorldFavoritePB> buildList = new ArrayList<>();
		List<AllianceFileCfg> cfgList = AssembleDataManager.getInstance().getAllianceFileList();
		WorldFavoritePB.Builder builder = null;
		for (AllianceFileCfg allianceFileCfg : cfgList) {			
			switch (allianceFileCfg.getType()) {
			case GsConst.GuildFavourite.TYPE_GUILD_MANOR:
				builder = builderGuildManorFavouriteBuilder(allianceFileCfg, guildId);
				if (builder != null) {
					buildList.add(builder.build());
				}
				break;
			case GsConst.GuildFavourite.TYPE_GUILD_MEMBER:
				boolean rlt = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.MEMBER_POSITION);				
				buildList.addAll(buildGuildMemberFavourite(allianceFileCfg, guildId, rlt));				
				break;
			case GsConst.GuildFavourite.TYPE_GUILD_SIGN:
				buildList.addAll(buildGuildSignFavourite(allianceFileCfg, guildId));				
				break;
			case GsConst.GuildFavourite.TYPE_GUILD_BUILDING:
				buildList.addAll(buildGuildBuildingFavourite(allianceFileCfg, guildId));
				break;
			}
		}
		
		return buildList;
	}
	
	private List<WorldFavoritePB> buildGuildBuildingFavourite(AllianceFileCfg allianceFileCfg, String guildId) {
		List<IGuildBuilding> buildingList = GuildManorService.getInstance().getGuildBuildByType(guildId, TerritoryType.valueOf(allianceFileCfg.getTypeCoefficient()));
		List<WorldFavoritePB> builderList = new ArrayList<>();
		for (IGuildBuilding guildBuilding : buildingList) {
			if (guildBuilding.getBuildStat().index < GuildBuildingStat.BUILDING.index) {
				continue;
			}
			WorldFavoritePB.Builder builder = WorldFavoritePB.newBuilder();
			builder.setTag(GsConst.GuildFavourite.TAG_GUILD);
			builder.setType("");
			builder.setCfgId(allianceFileCfg.getId());
			builder.setPosX(guildBuilding.getEntity().getPosX());
			builder.setPosY(guildBuilding.getEntity().getPosY());
			builder.setName("");
			builder.setServerId(Integer.parseInt(GsConfig.getInstance().getServerId()));
			builder.setExtCfgId(guildBuilding.getEntity().getBuildingId());
			builderList.add(builder.build());
		}
		
		return builderList;
	}

	private List<WorldFavoritePB> buildGuildSignFavourite(AllianceFileCfg allianceFileCfg, String guildId) {
		Map<Integer,GuildSign> map = GuildService.getInstance().getGuildSignMap(guildId);
		List<WorldFavoritePB> builderList = new ArrayList<>();
		for (Entry<Integer, GuildSign> entry : map.entrySet()) {
			WorldFavoritePB.Builder builder = WorldFavoritePB.newBuilder();
			builder.setTag(GsConst.GuildFavourite.TAG_GUILD);
			builder.setType("");
			builder.setCfgId(allianceFileCfg.getId());
			builder.setPosX(entry.getValue().getPosX());
			builder.setPosY(entry.getValue().getPosY());
			builder.setName(entry.getValue().getInfo());
			builder.setServerId(Integer.parseInt(GsConfig.getInstance().getServerId()));
			builder.setExtCfgId(entry.getKey());
			builderList.add(builder.build());
			
		}
		
		return builderList;
	}
	private List<WorldFavoritePB> buildGuildMemberFavourite(AllianceFileCfg allianceFileCfg, String guildId, boolean authority) {
		//需求来回改.
		if (!authority) {
			return new ArrayList<>();
		}
		List<String> playerIds = GuildService.getInstance().getGuildMemberIdsByAuthority(guildId, allianceFileCfg.getTypeCoefficient());
		List<WorldFavoritePB> builderList = new ArrayList<>();
		for (String playerId : playerIds) {
			int[] pos = WorldPlayerService.getInstance().getPlayerPosXY(playerId);
			if (pos[0] == 0 & pos[1] == 0) {
				continue;
			}
			
			WorldFavoritePB.Builder builder = WorldFavoritePB.newBuilder();
			builder.setTag(GsConst.GuildFavourite.TAG_GUILD);
			builder.setType("");
			builder.setName(GlobalData.getInstance().getPlayerNameById(playerId));			
			builder.setPosX(pos[0]);
			builder.setPosY(pos[1]);			 
			builder.setCfgId(allianceFileCfg.getId());
			builder.setServerId(Integer.parseInt(GsConfig.getInstance().getServerId()));
			builderList.add(builder.build());
		}
		
		return builderList;
	}
	private  WorldFavoritePB.Builder builderGuildManorFavouriteBuilder(AllianceFileCfg allianceFileCfg, String guildId ) {
		WorldFavoritePB.Builder builder = WorldFavoritePB.newBuilder();
		builder.setTag(GsConst.GuildFavourite.TAG_GUILD);
		builder.setType("");
		builder.setServerId(Integer.parseInt(GsConfig.getInstance().getServerId()));
		List<GuildManorObj> objList = GuildManorService.getInstance().getGuildManors(guildId);
		Optional<GuildManorObj> op = objList.stream()
				.filter(obj -> obj.getBastionStat().index >= ManorBastionStat.BUILDING.index
						&& obj.getEntity().getManorIndex() == allianceFileCfg.getTypeCoefficient())
				.findAny();
		if (!op.isPresent()) {
			return null;
		}
		builder.setName(op.get().getEntity().getManorName());
		builder.setPosX(op.get().getEntity().getPosX());
		builder.setPosY(op.get().getEntity().getPosY());
		builder.setCfgId(allianceFileCfg.getId());		
		
		return builder;
	}

	/**
	 * 同步任务列表
	 */
	public void syncMissionList() {
		MissionListRes.Builder builder = MissionListRes.newBuilder();
		synchronized (player) {
			List<MissionEntity> missionList = getData().getOpenedMissions();
			for (MissionEntity entity : missionList) {
				if (entity.getState() != MissionState.STATE_BONUS) {
					builder.addList(MissionService.getInstance().entityToPB(entity));
				}
			}
		}
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.MISSION_LIST_SYNC_S, builder);
		player.sendProtocol(protocol);
	}

	/**
	 * 同步用户自定义数据
	 */
	public void syncCustomData() {
		HPCustomDataSync.Builder builder = HPCustomDataSync.newBuilder();
		List<CustomDataEntity> customDataList = player.getData().getCustomDataEntities();
		if (customDataList != null) {
			for (CustomDataEntity customData : customDataList) {
				KVData.Builder kvData = KVData.newBuilder();
				kvData.setKey(customData.getType());
				kvData.setVal(customData.getValue());
				if (!HawkOSOperator.isEmptyString(customData.getArg())) {
					kvData.setArg(customData.getArg());
				}
				builder.addData(kvData);
			}
		}

		HawkProtocol protocol = HawkProtocol.valueOf(HP.sys.CUSTOM_DATA_SYNC, builder);
		player.sendProtocol(protocol);
	}

	/**
	 * 同步联盟信息
	 */
	public void syncGuildInfo() {
		if (player.hasGuild()) {
			// 联盟积分信息
			GuildScoreSync.Builder scoreInfo = GuildScoreSync.newBuilder();
			scoreInfo.setAllianScore(GuildService.getInstance().getGuildScore(player.getGuildId()));
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_SCORE_SYNC_S_VALUE, scoreInfo));
			// 联盟签到信息
			GuildService.getInstance().syncGuildSignatureInfo(player);
		}
		HPGuildInfoSync.Builder builder = GuildService.getInstance().buildGuildSyncInfo(player);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.GUILD_BASIC_INFO_SYNC_S, builder);
		player.sendProtocol(protocol);
	}

	/**
	 * 通知玩家离开联盟
	 * 
	 * @param player
	 */
	public void pushLeaveGuild() {
		if (player != null && player.isActiveOnline()) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_LEAVE_GUILD_VALUE));
		}
	}

	/**
	 * 同步聊天屏蔽玩家信息
	 */
	public void syncShieldPlayerInfo() {
		Set<String> shieldPlayerIds = player.getShieldPlayers();
		if (shieldPlayerIds == null || shieldPlayerIds.size() <= 0) {
			return;
		}
		String[] playerIds = shieldPlayerIds.toArray(new String[shieldPlayerIds.size()]);
		Map<String, Player> map = GlobalData.getInstance().getPlayerMap(playerIds);
		HPShieldPlayerInfoSync.Builder shieldPlayerInfos = HPShieldPlayerInfoSync.newBuilder();
		for (Player playerInfo : map.values()) {
			ShieldPlayerInfo.Builder builder = ShieldPlayerInfo.newBuilder();
			builder.setPlayerId(playerInfo.getId());
			builder.setName(playerInfo.getName());
			builder.setIcon(playerInfo.getIcon());
			builder.setBattlePoint(playerInfo.getPower());
			builder.setGuildName(playerInfo.hasGuild() ? playerInfo.getGuildName() : "");
			shieldPlayerInfos.addShieldPlayer(builder);
		}
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.SHIELD_PLAYER_INFO_SYNC_S, shieldPlayerInfos);
		player.sendProtocol(protocol);
	}

	/**
	 * 推送红点信息
	 * 
	 * @param type
	 * @param value
	 */
	public void syncRedPoint(RedType type, String value) {
		HpRedPointSync.Builder builder = HpRedPointSync.newBuilder();
		builder.setType(type);
		if (!HawkOSOperator.isEmptyString(value)) {
			builder.setValue(value);
		}
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.NOTICE_RED_POINT_SYNC_S, builder);
		player.sendProtocol(protocol);
	}

	/**
	 * 同步剧情任务
	 */
	public void syncStoryMissionInfo() {
		StoryMissionEntity entity = getData().getStoryMissionEntity();
		StoryMissionPage.Builder builder = entity.toBuilder();
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_STORY_MISSION_INFO_S, builder));
	}

	/**
	 * 同步剧情任务奖励
	 */
	public void syncStoryMissionAward(boolean isChapterAward, int missionId, int chapterId) {
		StoryMissionRewRes.Builder builder = StoryMissionRewRes.newBuilder();
		builder.setGetChapterAward(isChapterAward);
		builder.setMissionId(missionId);
		builder.setChapterId(chapterId);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.STORY_MISSION_REWARD_S_VALUE, builder));
	}

	/**
	 * 同步新手世界点创建
	 * 
	 * @param success
	 */
	public void syncNewlyPointSucc(boolean success, int pointId) {
		HPGenNewlyDataRes.Builder builder = HPGenNewlyDataRes.newBuilder();
		builder.setSuccess(success);
		if (success) {
			int[] pos = GameUtil.splitXAndY(pointId);
			builder.setX(pos[0]);
			builder.setY(pos[1]);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GEN_NEWLY_DATA_S_VALUE, builder));
	}

	/**
	 * 同步扭蛋信息
	 */
	public void syncGachaInfo() {
		HPSyncGachaInfoResp.Builder resp = BuilderUtil.gachaInfoPB(player.getData());
		MechaAddProductInfo info = player.getPlayerMechaCore().getAddProductInfo();
		HPGachaModuleInfo.Builder moduleInfo = HPGachaModuleInfo.newBuilder();
		moduleInfo.setUseCountDaily(info.getUseCountDaily());
		moduleInfo.setAddProductCount(info.getProductAddCount() - info.getUseCountDaily());
		resp.setGachaModuleInfo(moduleInfo);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_GACHA_SYNC_S, resp));

	}

	/**
	 * 刷新玩家钻石数目
	 */
	public void syncPlayerDiamonds() {
		UpdatePlayerDiamond.Builder builder = UpdatePlayerDiamond.newBuilder();
		builder.setDiamonds(player.getPlayerBaseEntity().getDiamonds());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_PLAYER_DIAMONDS_VALUE, builder));
	}

	/**
	 * 通知邮件被删除
	 */
	public void notifyMailDeleted(int mailType, List<String> mailIds) {
		HPDelMailByIdResp.Builder builder = HPDelMailByIdResp.newBuilder().setType(mailType).addAllId(mailIds);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_DEL_MAIL_BY_ID_S, builder));
	}

	/**
	 * 同步礼包列表
	 */
	public void syncGiftList() {
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GIFT_INFO_SYNC, BuilderUtil.getGiftListBuilder(player)));
	}

	/**
	 * 同步月卡充值信息
	 */
	public void syncMonthCardRechargeInfo() {
		player.sendProtocol(
				HawkProtocol.valueOf(HP.code.MONTH_CARD_RECHARGE_SYNC, BuilderUtil.getMonthCardRechargeBuilder()));
	}

	/**
	 * 同步野怪击杀等级
	 * 
	 * @return
	 */
	public void syncMonsterKilled(int monsterCfgId, boolean isWin) {
		PlayerMonsterEntity entity = player.getData().getMonsterEntity();
		KillMonsterPush.Builder builder = KillMonsterPush.newBuilder();
		builder.setKillMaxLvl(entity.getMaxLevel());
		builder.setMonsterId(monsterCfgId);
		builder.setNewMonsterMaxLvl(entity.getNewMonsterKileLvl());
		builder.setIsWin(isWin);
		builder.setAtkNewMonsterTimes(entity.getAttackNewMonsterTimes());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_KILL_MONSTER_LEVEL, builder));
	}

	/**
	 * 行军删除推送
	 * 
	 * @param marchId
	 */
	public void pushMarchRemove(String marchId) {
		if (!player.isActiveOnline()) {
			HawkLog.logPrintln("push march remove break, player not online, playerId: {}", player.getId());
			return;
		}

		WorldMarchDeletePush.Builder builder = WorldMarchDeletePush.newBuilder();
		builder.setMarchId(marchId);
		builder.setRelation(WorldMarchRelation.SELF);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_DELETE_PUSH_VALUE, builder);
		player.sendProtocol(protocol);
	}

	/**
	 * MassJoin行军删除推送
	 * 
	 * @param marchId
	 */
	public void pushMassJoinMarchRemove(String marchId) {
		if (!player.isActiveOnline()) {
			return;
		}
		WorldMarchDeletePush.Builder massBuilder = WorldMarchDeletePush.newBuilder();
		massBuilder.setMarchId(marchId);
		massBuilder.setRelation(WorldMarchRelation.TEAM_LEADER);
		HawkProtocol massProtocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_DELETE_PUSH_VALUE, massBuilder);
		player.sendProtocol(massProtocol);
	}

	/**
	 * 同步付费队列数据
	 */
	public void syncSecondaryBuildQueue() {
		PaidQueuePB.Builder builder = PaidQueuePB.newBuilder();
		if (player.getData().isSecondBuildUnlock()) {
			builder.setUnlockedPermanently(true);
		} else {
			builder.setUnlockedPermanently(false);
			QueueEntity queue = player.getData().getPaidQueue();
			if (queue != null) {
				builder.setEnableEndTime(queue.getEnableEndTime());
			} else {
				builder.setEnableEndTime(HawkTime.getMillisecond());
			}
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.PAID_QUEUE_PUSH, builder));
	}

	/**
	 * 同步城防信息
	 */
	public void syncCityDef(boolean cityOnFireStateChange) {
		BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(BuildingType.CITY_WALL);
		// 还没有城墙建筑
		if (buildingEntity == null) {
			return;
		}

		CityDefPB.Builder builder = CityDefPB.newBuilder();
		// 实际城防值
		int cityDefVal = player.getPlayerBaseEntity().getCityDefVal();
		// 城防值上限
		int maxCityDef = player.getData().getRealMaxCityDef();
		// 城防修复时间
		long repairTime = player.getPlayerBaseEntity().getCityDefNextRepairTime();
		builder.setCityDefVal(cityDefVal);

		long now = HawkTime.getMillisecond();
		if (repairTime == 0 && cityDefVal < maxCityDef) {
			CityManager.getInstance().increaseCityDef(player, maxCityDef - cityDefVal);
			builder.setCityDefVal(maxCityDef);
			cityDefVal = maxCityDef;
		} else if (repairTime >= now) {
			builder.setNextRepairTime(repairTime);
		}

		long onFireEnd = player.getPlayerBaseEntity().getOnFireEndTime();
		if (onFireEnd > now) {
			builder.setOnFireEndTime(onFireEnd);
			pushBuildingStatus(buildingEntity, BuildingStatus.CITYWALL_ONFIRE_STATUS);
		} else if (cityDefVal < maxCityDef) {
			pushBuildingStatus(buildingEntity, BuildingStatus.CITYWALL_DAMAGED_STATUS);
		} else {
			pushBuildingStatus(buildingEntity, BuildingStatus.COMMON);
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.CITY_DEF_PUSH, builder));

		if (cityOnFireStateChange) {
			WorldPlayerService.getInstance().resetCityFireStatus(player, onFireEnd > now ? onFireEnd : 0);
		}
	}

	/**
	 * 同步冒泡奖励信息
	 */
	public void syncBubbleRewardInfo(int bubbleType) {
		Set<Integer> bubbleTypes = ConstProperty.getInstance().getBubbleTypes();
		BubbleRewardInfoPB.Builder bubbleInfoPB = BubbleRewardInfoPB.newBuilder();
		if (bubbleType > 0) {
			bubbleInfoPB.setType(bubbleType);
		}

		for (Integer type : bubbleTypes) {
			BubbleRewardDetailPB.Builder detail = BubbleRewardDetailPB.newBuilder();
			BubbleRewardInfo bubbleInfo = player.getData().getBubbleRewardInfo(type);
			if (bubbleInfo == null) {
				continue;
			}

			detail.setType(type);
			detail.setGotTimes(bubbleInfo.getGotTimes());
			detail.setLastTime(bubbleInfo.getLastTime());
			detail.setNextRewardItem(bubbleInfo.getNextRewardItem().toRewardItem());
			bubbleInfoPB.addBubbleReward(detail);
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.BUBBLE_REWARD_INFO_PUSH, bubbleInfoPB));
	}

	/**
	 * 同步接收好友礼包次数
	 */
	public void syncGiftReceiveTimes() {
		DailyDataEntity dailyDataEntity = player.getData().getDailyDataEntity();
		int friendBoxTimes = dailyDataEntity.getFriendBoxTimes(MailId.PRESTENT_GIFT_VALUE);
		GiftReceiveTimesPush.Builder builder = GiftReceiveTimesPush.newBuilder();
		builder.setCount(friendBoxTimes);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.FRIEND_PRESENT_RECEIVE_TIMES_PUSH, builder));
	}

	/**
	 * 同步推荐好友
	 */
	public void syncRecommFriends() {
		int friendCount = RelationService.getInstance().getFriendNum(player.getId());
		if (friendCount > ConstProperty.getInstance().getFriendMinimumValue()) {
			return;
		}

		// 推荐列表
		List<String> recommFriends = new ArrayList<String>();

		// 每次推荐好友个数
		int friendRecommendCount = ConstProperty.getInstance().getFriendRecommendCount();
		Set<Player> onlinePlayerSet = GlobalData.getInstance().getOnlinePlayers();
		List<Player> onlinePlayerList = new ArrayList<>(onlinePlayerSet.size());		
		//玩家如果是跨服状态,就不推荐了
		CrossService crossService = CrossService.getInstance();
		for (Player player : onlinePlayerSet) {
			if (crossService.isImmigrationPlayer(player.getId())) {
				continue;
			} 
			onlinePlayerList.add(player);
		}
		
		Collections.shuffle(onlinePlayerList);
		Set<String> lossRecommends = BackFlowService.getInstance().getLossRecommendRoles(player.getId());
		recommFriends.addAll(lossRecommends);
		for (Player friend : onlinePlayerList) {
			if (recommFriends.size() >= friendRecommendCount) {
				break;
			}
			if (RelationService.getInstance().isFriend(this.player.getId(), friend.getId())) {
				continue;
			}
			if (this.player.getId().equals(friend.getId())) {
				continue;
			}
			if (RelationService.getInstance().isAlreadyApply(this.player.getId(), friend.getId())) {
				continue;
			}
			if (RelationService.getInstance().isAlreadyApply(friend.getId(), this.player.getId())) {
				continue;
			}
			if (RelationService.getInstance().isBlacklist(player.getId(), friend.getId())) {
				continue;
			}
			
			recommFriends.add(friend.getId());
		}

		
		RecommendFriendsResp.Builder resp = RecommendFriendsResp.newBuilder();
		for (String playerId : recommFriends) {
			StrangerMsg.Builder strangerMsg = RelationService.getInstance().buildStrangerMsg(player.getId(), playerId);
			resp.addStrangers(strangerMsg);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.RECOMMEND_FRIENDS_RESP, resp));
	}

	/**
	 * 同步世界上最大等级野怪
	 */
	public void syncMaxMonsterLevel() {
		int playerKillLevel = player.getData().getMonsterEntity().getMaxLevel();
		int worldMaxLevel = WorldMonsterService.getInstance().getMaxCommonMonsterLvl();
		worldMaxLevel = Math.max(playerKillLevel, worldMaxLevel);

		MaxMonsterLvlResp.Builder resp = MaxMonsterLvlResp.newBuilder();
		resp.setMaxLevel(worldMaxLevel);
		resp.setMaxNewMonsterLevel(0);
		resp.setMaxFoggyLevel(WorldFoggyFortressService.getInstance().getCurrentMaxForggyLevel());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MAX_MONSTER_LVL_RESP, resp));
	}

	/**
	 * 同步好友建筑状态
	 */
	public void syncFriendBuildStatus() {
		//如果发现是跨到其它服的玩家则不处理
		if (CrossService.getInstance().isEmigrationPlayer(player.getId())) {
			return;
		}
		BuildingBaseEntity entity = player.getData().getBuildingEntityByType(BuildingType.FRIENDLY_HALL);
		if (entity == null) {
			return;
		}
		// 建筑状态
		BuildingStatus status = BuildingStatus.COMMON;

		int applySize = RelationService.getInstance().getPlayerRelationApplySize(player.getId());
		if (applySize > 0) {
			status = BuildingStatus.HAS_FRIEND_APPLY;
		}

		if (entity.getStatus() != status.getNumber()) {
			entity.setStatus(status.getNumber());
			player.getPush().pushBuildingStatus(entity, status);
		}

	}

	/**
	 * 亲密度增加推送
	 * 
	 * @param sendGiftPlayerId
	 * @param loveAdd
	 */
	public void loveAddPush(String sendGiftPlayerId, int loveAdd) {
		LoveAddPush.Builder builder = LoveAddPush.newBuilder();
		builder.setPlayerId(sendGiftPlayerId);
		builder.setLoveAdd(loveAdd);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.LOVE_ADD_PUSH, builder));
	}

	public void synPlayerDailyData() {
		SynPlayerDailyData.Builder sbuilder = SynPlayerDailyData.newBuilder();
		DailyDataEntity entity = player.getData().getDailyDataEntity();
		sbuilder.setDataMsg(this.buildPlayerDailyDataMsg(entity));

		player.sendProtocol(HawkProtocol.valueOf(HP.code.SYN_PLAYER_DAILY_DATA_VALUE, sbuilder));
	}

	private PlayerDailyDataMsg.Builder buildPlayerDailyDataMsg(DailyDataEntity dailyDataEntity) {
		PlayerDailyDataMsg.Builder builder = PlayerDailyDataMsg.newBuilder();
		builder.setTravelShopRefreshTimes(dailyDataEntity.getTravelShopRefreshTimes());
		builder.addAllRecieveMailTimes(this.buildKeyValuePairIntList(dailyDataEntity.getFriendBoxMap()));
		builder.setArmourStarAttrRandTimes(dailyDataEntity.getArmourStarAttrTimes());
		return builder;
	}

	private List<KeyValuePairInt> buildKeyValuePairIntList(Map<Integer, Integer> map) {
		List<KeyValuePairInt> builderList = new ArrayList<>();
		KeyValuePairInt.Builder builder = null;
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			builder = KeyValuePairInt.newBuilder();
			builder.setKey(entry.getKey());
			builder.setVal(entry.getValue());

			builderList.add(builder.build());
		}

		return builderList;
	}

	public void syncGuildWarCount() {
		if (!player.hasGuild() || StringUtils.isNotEmpty(player.getDungeonMap())) {
			return;
		}
		HPGuildWarCountPush.Builder builder = HPGuildWarCountPush.newBuilder();
		Collection<IWorldMarch> mCloection = WorldMarchService.getInstance().getGuildMarchs(player.getGuildId());
		for(IWorldMarch march : mCloection){
			if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
				builder.addMassEndTime(march.getStartTime());
			}
		}
		int count = mCloection.size();
		builder.setCount(count);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_GUILD_WAR_COUNT_VALUE, builder));
	}

	/**
	 * 同步客户端配置信息
	 */
	public void syncClientCfg() {
		HPClientCfgResp.Builder resp = HPClientCfgResp.newBuilder();
		JSONObject json = ControlProperty.getInstance().getControlCfg();
		if (!GameUtil.isWin32Platform(player) && UserType.getByChannel(player.getChannel()) != UserType.WX) {
			json.put("couponSwitch", 0);
		}
		
		json.put("midasPayEnv", TencentCfg.getInstance().getPayModel());
		if (GameUtil.isWin32Platform(player)) {
			// win32登录，一律支持充值
			json.put("openRecharge", true);
		} else if (!"ios".equals(player.getPlatform()) || !"apple".equals(player.getChannel())) {
			// 非apple登录，以实际开关配置为准
			json.put("openRecharge", TencentCfg.getInstance().getPayModel() >= 0);
		} else {
			// apple登录时，只有ios审核服需要支持apple充值（当然还是以实际开关配置为准），其它类型的区服都不支持充值
			ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(GsConfig.getInstance().getServerId());
			if (serverInfo != null && serverInfo.getServerType() == ServerType.REVIEW) {
				json.put("openRecharge", TencentCfg.getInstance().getPayModel() >= 0);
			} else {
				json.put("openRecharge", false);
			}
		}
		
		json.put("openGm", GsConfig.getInstance().isOpenGm());
		json.put("gmPort", GsConfig.getInstance().getGmPort());
		json.put("openOneKeyCreateGroup", ConstProperty.getInstance().isOpenOneKeyCreateGroup());
		json.put("openFriendsInvite", ConstProperty.getInstance().isOpenFriendsInvite());
		json.put("openPlatformFriends", ConstProperty.getInstance().isOpenPlatformFriends());
		json.put("openShare", ConstProperty.getInstance().isOpenShare());
		json.put("openLoginWay", ConstProperty.getInstance().isOpenLoginWay());
		json.put("openPlatformFriendsGift", ConstProperty.getInstance().isOpenPlatformFriendsGift());
		json.put("openStrategy", ConstProperty.getInstance().isOpenStrategy());

		json.put("openPlat", ConstProperty.getInstance().getOpenPlat());
		json.put("openPlatformPrivilegeIds", ConstProperty.getInstance().getOpenPlatformPrivilegeIds());
		if (!HawkOSOperator.isEmptyString(player.getPlatform()) && player.getPlatform().equals("ios")) {
			json.put("openPlat", ConstProperty.getInstance().getIos_openPlat());
			json.put("openPlatformPrivilegeIds", ConstProperty.getInstance().getIos_openPlatformPrivilegeIds());
		}
		json.put("openQuestShare", ConstProperty.getInstance().isOpenQuestShare());
		WarCollegeTimeControlCfg wcfig = HawkConfigManager.getInstance().getKVInstance(WarCollegeTimeControlCfg.class);
		json.put("warCollegeSwitch", wcfig.isWarCollegeSwitch());
		json.put("warCollegeWhiteList", wcfig.getWarCollegeWhiteList());
		json.put("warCollegeDays", wcfig.getDays());
		json.put("warCollegeStartTime", wcfig.getStartTime());
		json.put("warCollegeEndTime", wcfig.getEndTime());
		json.put("openALVoice", ConstProperty.getInstance().isOpenGuildVoice());
		
		json.put("appUrl", GameUtil.getAppUrl(player));
		json.put("openCollege", CollegeConstCfg.getInstance().getIsSysterOpen());
		
		json.put("heroHide", getHeroHideInfo()); //heroId:time, heroId:time, heroId:time
		json.put("heroSkinHide", getHeroSkinHideInfo());
		json.put("manhattanHide", getManhattanHideInfo());
		json.put("duelPower", BattleService.getInstance().getDuelPower());
		json.put("scienceOpenServers", ConstProperty.getInstance().getScienceOpenServers());
		json.put("honorSevenOpen", ConstProperty.getInstance().isHonorSevenOpen());
		json.put("noticeHeader", PersonalMailService.NOTICE_HEADER);
		String version = player.getAppVersion();
		boolean special = false;
		if (version != null) {
			String[] versionStrs = version.split("\\.");
			if (versionStrs.length >= 5) {
				special = ConstProperty.getInstance().getBuildNoList().contains(Integer.valueOf(versionStrs[4]));
			}
		}
		json.put("QAshareQRCode", special ? 0 : ConstProperty.getInstance().getQAshareQRCode());
		json.put("privacyDisplay", special ? 0 : ConstProperty.getInstance().getPrivacyDisplay());
		json.put("searchPrecise", ConstProperty.getInstance().getSearchPrecise());
		json.put("showIpOnline", ConstProperty.getInstance().getShowIpOnline());
		
		boolean superVIPEntranceOpen = false;
		Optional<CustomKeyCfg> optional = CustomKeyCfg.getIdipRedDotSwitchKeys().stream().filter(e -> e.getIdipRedDotType() == 2).findFirst();
		if (optional.isPresent()) {
			CustomDataEntity customData = player.getData().getCustomDataEntity(optional.get().getKey());
			superVIPEntranceOpen = customData != null && customData.getValue() == 0;
		}
		json.put("superVIPEntrance", superVIPEntranceOpen ? 1 : 0); //主界面增加超核icon入口并带链接（1开0关）
		resp.setData(json.toString());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CLIENT_CONFIG_S, resp));
	}

	/**
	 * 获取屏蔽的超武数据
	 * @return
	 */
	public String getManhattanHideInfo(){
		List<ManhattanSWCfg> swLimitLists = HawkConfigManager.getInstance().getConfigIterator(ManhattanSWCfg.class)
				.stream()
				.filter(cfg -> cfg.getShowTimeValue() > HawkTime.getMillisecond())
				.collect(Collectors.toList());
		List<String> list = new ArrayList<>();
		for (ManhattanSWCfg swCfg : swLimitLists) {
			String str = swCfg.getSwId() +"_"+ swCfg.getShowTimeValue();
			list.add(str);
		}
		String swStr = SerializeHelper.collectionToString(list, SerializeHelper.ELEMENT_DELIMITER);
		return swStr;
	}
	
	/**获取屏蔽的英雄数据
	 * @return
	 */
	public String getHeroHideInfo(){
		List<HeroCfg> heroLimitLists = HawkConfigManager.getInstance().getConfigIterator(HeroCfg.class)
				.stream()
				.filter(cfg -> cfg.getShowTimeValue() > HawkTime.getMillisecond())
				.collect(Collectors.toList());
		List<String> list = new ArrayList<>();
		for (HeroCfg heroCfg : heroLimitLists) {
			String str = heroCfg.getHeroId() +"_"+ heroCfg.getShowTimeValue();
			list.add(str);
		}
		String heroStr = SerializeHelper.collectionToString(list, SerializeHelper.ELEMENT_DELIMITER);
		return heroStr;
	}
	
	/**获取屏蔽的英雄皮肤数据
	 * @return
	 */
	public String getHeroSkinHideInfo(){
		List<HeroSkinCfg> heroSkins = HawkConfigManager.getInstance().getConfigIterator(HeroSkinCfg.class)
				.stream()
				.filter(cfg -> cfg.getShowTimeValue() > HawkTime.getMillisecond())
				.collect(Collectors.toList());
		List<String> list = new ArrayList<>();
		for (HeroSkinCfg heroSkinCfg : heroSkins) {
			String str = heroSkinCfg.getSkinId() +"_"+ heroSkinCfg.getShowTimeValue();
			list.add(str);
		}
		String heroSkinStr = SerializeHelper.collectionToString(list, SerializeHelper.ELEMENT_DELIMITER);
		return heroSkinStr;
	}
	/**
	 * 同步装备信息
	 */
	public void syncEquipInfo() {
		syncEquipInfo(getData().getEquipEntities());
	}

	/**
	 * 同步装备信息
	 */
	public void syncEquipInfo(List<EquipEntity> entityList) {
		HPEquipInfo.Builder builder = HPEquipInfo.newBuilder();
		for (EquipEntity equipEntity : entityList) {
			builder.addEquipInfo(BuilderUtil.genEquipInfoBuilder(equipEntity));
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.EQUIP_INFO_SYNC_S, builder));
	}

	/**
	 * 同步装备信息
	 */
	public void syncEquipInfo(EquipEntity entity) {
		HPEquipInfo.Builder builder = HPEquipInfo.newBuilder();
		builder.addEquipInfo(BuilderUtil.genEquipInfoBuilder(entity));
		player.sendProtocol(HawkProtocol.valueOf(HP.code.EQUIP_INFO_SYNC_S, builder));
	}

	/**
	 * 同步装备信息
	 */
	public void syncAddEquipInfo(EquipEntity entity) {
		HPEquipInfo.Builder builder = HPEquipInfo.newBuilder();
		builder.addEquipInfo(BuilderUtil.genEquipInfoBuilder(entity));
		player.sendProtocol(HawkProtocol.valueOf(HP.code.EQUIP_ADD_SYNC_S, builder));
	}

	/**
	 * 同步指挥官装备信息
	 */
	public void syncCommanderEquipSoltInfo() {
		CommanderEquipSync.Builder builder = CommanderEquipSync.newBuilder();
		for (EquipSlot equipSolt : getData().getCommanderObject().getEquipSlots()) {
			builder.addEquipSlot(equipSolt.toPBObject());
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.COMMANDER_EQUIP_SYNC_S, builder));
	}

	/**
	 * 同步指挥官装备信息
	 */
	public void syncCommanderEquipSoltInfo(EquipSlot equipSolt) {
		CommanderEquipSync.Builder builder = CommanderEquipSync.newBuilder();
		builder.addEquipSlot(equipSolt.toPBObject());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.COMMANDER_EQUIP_SYNC_S, builder));
	}

	/**
	 * 推送英雄信息
	 */
	public void pushHeroList() {
		List<PlayerHero> heros = player.getAllHero();
		PBHeroListPush.Builder resp = PBHeroListPush.newBuilder();
		heros.forEach(hero -> resp.addHeros(hero.toPBobj()));
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_ALL_HERO, resp));
	}
	
	public void pushSuperSoldier(){
		List<SuperSoldier> soldiers = player.getAllSuperSoldier();
		PBSuperSoldierListPush.Builder resp = PBSuperSoldierListPush.newBuilder();
		soldiers.forEach(sd -> resp.addSuperSoldiers(sd.toPBobj()));
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_ALL_SUPER_SOLDIER, resp));
	}

	public void pushSuperSoldierSkin(){
		try {
			CommanderEntity entity = player.getData().getCommanderEntity();
			PBSuperSoldierSkinInfoResp.Builder resp = PBSuperSoldierSkinInfoResp.newBuilder();
			resp.addAllSkinIds(entity.getSuperSoldierSkins());
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.SUPER_SOLDIER_SKIN_INFO_RESP, resp));
		}catch (Exception e){
			HawkException.catchException(e);
		}
	}

	/**
	 * 同步vip礼包信息
	 * @param pushAll
	 * @param isNewly
	 */
	public void syncAllVipBoxStatus(boolean pushAll, boolean isNewly) {
		VipBoxInfoPB.Builder builder = VipBoxInfoPB.newBuilder();
		if (isNewly) {
			builder.setBenefitBoxTaken(false);
		} else {
			List<String> unreceivedBenefitBox = RedisProxy.getInstance().getUnreceivedBenefitBox(player.getId());
			for (String id : unreceivedBenefitBox) {
				builder.addUnreceivedBenefitBox(Integer.valueOf(id));
			}
			
			if (pushAll) {
				Map<Integer, Boolean> statusMap = RedisProxy.getInstance().getAllVipBoxStatus(player.getId());
				// key为0表示是vip福利礼包的状态数据
				builder.setBenefitBoxTaken(statusMap.containsKey(0) ? statusMap.get(0) : false);
				statusMap.remove(0);
				for (Entry<Integer, Boolean> status : statusMap.entrySet()) {
					VipExclusiveBox.Builder exclusiveBuilder = VipExclusiveBox.newBuilder();
					exclusiveBuilder.setVipLevel(status.getKey());
					exclusiveBuilder.setBought(status.getValue());
					builder.addExclusiveBox(exclusiveBuilder);
				}
			}
		}

		// 同步vip礼包信息信息
		player.sendProtocol(HawkProtocol.valueOf(HP.code.VIP_BOX_SYNC_S, builder));
	}

	/**
	 * 同步vip礼包信息
	 * 
	 * @param vipBoxInfo
	 */
	public void syncVipBoxStatus(int vipLevel, boolean status) {
		VipBoxInfoPB.Builder builder = VipBoxInfoPB.newBuilder();
		// vipLevel为0表示是vip福利礼包的状态数据
		if (vipLevel == 0) {
			builder.setBenefitBoxTaken(status);
		} else {
			VipExclusiveBox.Builder exclusiveBuilder = VipExclusiveBox.newBuilder();
			exclusiveBuilder.setVipLevel(vipLevel);
			exclusiveBuilder.setBought(status);
			builder.addExclusiveBox(exclusiveBuilder);
		}

		List<String> unreceivedBenefitBox = RedisProxy.getInstance().getUnreceivedBenefitBox(player.getId());
		for (String id : unreceivedBenefitBox) {
			builder.addUnreceivedBenefitBox(Integer.valueOf(id));
		}

		// 同步vip礼包信息信息
		player.sendProtocol(HawkProtocol.valueOf(HP.code.VIP_BOX_SYNC_S, builder));
	}

	/**
	 * 同步vip商城商品信息
	 * 
	 * @param params
	 */
	public void syncVipShopItemInfo(int... params) {
		VipShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(VipShopCfg.class, params[0]);
		VipShopItemInfo.Builder vipShopItemBuilder = VipShopItemInfo.newBuilder();
		VipShopItem.Builder buider = BuilderUtil.getVipShopItemBuilder(cfg);
		if (params.length > 1) {
			buider.setRemainBuyTimes(cfg.getNum() - params[1]);
		}

		vipShopItemBuilder.addVipShopItems(buider);
		vipShopItemBuilder.setRefreshAll(false);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.VIP_SHOP_ITEM_SYNC_S, vipShopItemBuilder));
	}

	/**
	 * 同步装扮信息
	 */
	public void syncDressInfo() {
		HPDressInfoSync.Builder sync = HPDressInfoSync.newBuilder();
		DressEntity dressEntity = player.getData().getDressEntity();
		BlockingDeque<DressItem> dressInfos = dressEntity.getDressInfo();

		for (DressItem dressInfo : dressInfos) {
			DressSingleInfo.Builder single = DressSingleInfo.newBuilder();
			single.setDressType(DressType.valueOf(dressInfo.getDressType()));
			single.setModelType(dressInfo.getModelType());
			single.setReaminTime(GameUtil.getDressRemainTime(dressInfo));
			DressItem currentDress = WorldPointService.getInstance().getShowDress(player.getId(), dressInfo.getDressType());
			single.setIsDress(currentDress != null && dressInfo.getModelType() == currentDress.getModelType());
			long now = HawkTime.getMillisecond();
			if (dressInfo.getShowType() != 0 && dressInfo.getShowEndTime() > now) {
				single.setShowType(dressInfo.getShowType());
				single.setShowRemainTime(dressInfo.getShowEndTime() - now);
			}
			
			sync.addDressSingleInfo(single);
		}

		// 装扮称号显示类型
		sync.setDressTitleType(WorldPointService.getInstance().getDressTitleType(player.getId()));
		if(ConstProperty.getInstance().isDressGodOpen()){
			CustomDataEntity customData = player.getData().getCustomDataEntity(GsConst.DRESS_GOD_ACTIVE);
			sync.setIsGodActive(customData == null ? false:true);
		}else {
			sync.setIsGodActive(false);
		}

		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SYNC_DRESS_INFO, sync));
	}

	/**
	 * 同步装扮赠送保护期信息
	 */
	public void syncDressSendProtectInfo() {
		DressSendProtectInfoSync.Builder sync = DressSendProtectInfoSync.newBuilder();
		for (Entry<Integer, Long> info : player.getSendTimeLimitTool().entrySet()) {
			DressSendProtectInfo.Builder builder = DressSendProtectInfo.newBuilder();
			builder.setItemId(info.getKey());
			builder.setReceiveTime(info.getValue());
			sync.addInfo(builder);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DRESS_SEND_PROTECT_INFO_SYNC, sync));
	}
	
	/**
	 * 推送装扮信使礼包每周赠送次数
	 */
	public void syncSendDressGiftInfo() {
		Map<Integer, Integer> sendDressGiftInfo = RedisProxy.getInstance().getSendDressGiftInfo(player.getId());
		
		DressSendGiftTimesInfoPush.Builder push = DressSendGiftTimesInfoPush.newBuilder();
		for (Entry<Integer, Integer> giftInfo : sendDressGiftInfo.entrySet()) {
			DressSendGiftTimesInfo.Builder builder = DressSendGiftTimesInfo.newBuilder();
			builder.setGfitId(giftInfo.getKey());
			builder.setBuyTimes(giftInfo.getValue());
			push.addInfos(builder);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.SEND_DRESS_GIFT_INFO_PUSH, push));
	}
	
	/**
	 * 是否首充过
	 */
	public void syncHasFirstRecharge() {
		HasFirstRechargeSync.Builder sync = HasFirstRechargeSync.newBuilder();
		boolean hasAlreadyFirstRecharge = player.getPlayerBaseEntity().getSaveAmtTotal() > 0;
		if(!hasAlreadyFirstRecharge){
			NewFirstRechargeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(NewFirstRechargeKVCfg.class);
			if(cfg != null){
				long serverOpenDate = GlobalData.getInstance().getServerOpenTime(player.getServerId());
				if(serverOpenDate >= cfg.getServerOpenValue()){
					hasAlreadyFirstRecharge = player.getRechargeTotal() > 0;
				}
			}
		}
		sync.setHasFirstRecharge(hasAlreadyFirstRecharge);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.HAS_FIRST_RECHARGE_SYNC, sync));
	}

	/**
	 * 下线的时候清空一下。
	 */
	public void clearLastPlayerInfoBuilder() {
		this.playerInfoBuilder = null;
	}

	public void synPlayerFlag() {
		PlayerFlagSyn.Builder sbuilder = PlayerFlagSyn.newBuilder();
		sbuilder.setFlag(player.getData().getPlayerBaseEntity().getFlag());

		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_FLAG_SYN_VALUE, sbuilder));
	}

	/**
	 * 同步军衔奖励领取状态
	 */
	public void syncMilitaryRankAwardState() {
		PlayerMilitaryRankAward.Builder builder = PlayerMilitaryRankAward.newBuilder();
		builder.setIsReceived(player.getData().getDailyDataEntity().isMilitaryRankRecieve());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.MILITARY_RANK_REWAR_STATUS_SYNC, builder));
	}

	public void notifyGuildFavouriteRedPoint(int type, int manorIndex) {
		List<AllianceFileCfg> fileList = AssembleDataManager.getInstance().getAllianceFileList();
		Optional<AllianceFileCfg> op = fileList.stream().filter(cfg->{
			if (cfg.getType() == GsConst.GuildFavourite.TYPE_GUILD_MEMBER) {
				if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.MEMBER_POSITION)) {
					return false;
				}
			}
			return (cfg.getType() == type &&(cfg.getTypeCoefficient() == 0 || cfg.getTypeCoefficient() == manorIndex)); 
		}).findAny();
		if (op.isPresent()) {
			this.syncRedPoint(RedType.GUILD_FAVOURITE, "");
		}
	}
	
	/**
	 * 同步玩家城点
	 * @param posInfo
	 */
	public void pushPlayerPos(int[] pos) {
		// 回复协议
		WorldMoveCityResp.Builder builder = WorldMoveCityResp.newBuilder();
		builder.setResult(true);
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MOVE_CITY_S_VALUE, builder));
	}
	
	/**
	 * 推送寻宝事件
	 * @param type
	 */
	public void pushTreasureHuntEvent(TreasureHuntType type) {
		PushTreasureHuntEvent.Builder builder = PushTreasureHuntEvent.newBuilder();
		builder.setType(type);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_TREASURE_HUNT_EVENT, builder));
	}
	
	/**
     * 同步新兵救援信息
     * 
     * @param protectSoldierInfo
     */
    public void pushProtectSoldierInfo() {
    	long timeLong = ConstProperty.getInstance().getRescueDuration() * 1000L;
    	if (HawkApp.getInstance().getCurrentTime() - player.getCreateTime() >= timeLong) {
     		return;
     	}
    	
    	ProtectSoldierInfo protectSoldierInfo = player.getProtectSoldierInfo(true);
    	ProtectSoldierPushPB.Builder builder = ProtectSoldierPushPB.newBuilder();
    	builder.setLastReceiveTime(protectSoldierInfo.getLastReceiveTime());
    	builder.setReceiveCountDay(protectSoldierInfo.getReceiveCountDay());
    	builder.setReceiveTotalCount(protectSoldierInfo.getReceiveTotalCount());
    	for (Entry<Integer, Integer> entry : protectSoldierInfo.getDeadSoldier().entrySet()) {
    		if (entry.getValue() <= 0) {
    			continue;
    		}
    		
    		ArmySoldierPB.Builder inner = ArmySoldierPB.newBuilder();
    		inner.setArmyId(entry.getKey());
    		inner.setCount(entry.getValue());
    		builder.addDeadSoldier(inner);
    	}
    	
    	player.sendProtocol(HawkProtocol.valueOf(HP.code.PROTECT_SOLDIER_PUSH, builder));
    }
    
	/**
	 * 同步所有铠甲信息
	 */
	public void syncAllArmourInfo() {
		ArmourAllInfoResp.Builder builder = BuilderUtil.genAllArmourInfoBuilder(player.getData());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ARMOUR_ALL_INFO_RESP, builder));
	}
	
	/**
	 * 同步所有装备星能探索信息
	 */
	public void syncArmourStarExploreInfo() {
		try {
			CommanderEntity entity = player.getData().getCommanderEntity();
			ArmourStarExplores starExpores = entity.getStarExplores();
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.ARMOUR_STAR_EXPLORE_INFO_SYNC, starExpores.toPB(player.getId())));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 同步铠甲信息
	 */
	public void syncArmourInfo(ArmourEntity armour) {
		ArmourSingleUpdate.Builder builder = ArmourSingleUpdate.newBuilder();
		ArmourInfo.Builder armourInfo = BuilderUtil.genArmourInfoBuilder(armour);
		builder.addArmourInfo(armourInfo);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ARMOUR_SINGLE_PUSH, builder));
	}
	
	/**
	 * 推送装备外显
	 */
	public void syncEquipStarShow() {
		ArmourStarShowInfo.Builder builder = ArmourStarShowInfo.newBuilder();
		int[] equipStarShowInfo = WorldPointService.getInstance().getEquipStarShowInfo(player.getId());
		builder.setShow(equipStarShowInfo[0]);
		builder.setOpenShow(equipStarShowInfo[1] == 0);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.EUQUIP_STAR_SHOW_CHANGE_RESP, builder));
	}
	
	/**
	 * 同步铠甲信息
	 */
	public void syncArmourInfo(List<ArmourEntity> armours) {
		ArmourSingleUpdate.Builder builder = ArmourSingleUpdate.newBuilder();
		for (ArmourEntity armour : armours) {
			ArmourInfo.Builder armourInfo = BuilderUtil.genArmourInfoBuilder(armour);
			builder.addArmourInfo(armourInfo);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ARMOUR_SINGLE_PUSH, builder));
	}
	
	/**
	 * 同步铠甲套装信息
	 */
	public void syncArmourSuitInfo() {
		ArmourSuitInfoResp.Builder builder = ArmourSuitInfoResp.newBuilder();
		Map<Integer, String> suitNames = RedisProxy.getInstance().getArmourSuitName(player.getId());
		for (Entry<Integer, String> suit : suitNames.entrySet()) {
			ArmourSuit.Builder suitBuilder = ArmourSuit.newBuilder();
			suitBuilder.setType(ArmourSuitType.valueOf(suit.getKey()));
			String name = suit.getValue();
			if (!HawkOSOperator.isEmptyString(name)) {
				suitBuilder.setName(name);
				builder.addSuit(suitBuilder);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ARMOUR_SUIT_INF_RESP, builder));
	}
	
	/**
	 * 同步装扮删除
	 */
	public void syncArmourDelete(String armourId) {
		ArmourDeleteResp.Builder builder = ArmourDeleteResp.newBuilder();
		builder.addArmourId(armourId);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ARMOUR_DELETE_RESP, builder));
	}
	
	/**
	 * 同步已解锁的行军表情包
	 */
	public void syncMarchEmoticon() {
		CustomDataEntity customData = player.getData().getCustomDataEntity(GsConst.MARCH_EMOTICON_BAG);
		if (customData == null || HawkOSOperator.isEmptyString(customData.getArg())) {
			return;
		}
		
		UnlockedMarchEmoticonSync.Builder builder = UnlockedMarchEmoticonSync.newBuilder();
		String[] emoticonBags = customData.getArg().split(",");
		for (String bag : emoticonBags) {
			builder.addEmoticon(Integer.parseInt(bag));
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.MARCH_EMOTICON_SYNC_S, builder));
	}
	
	
	/**
	 * 同步玩家装备科技信息
	 */
	public void syncEquipResearchInfo() {
		ArmourTechInfoPush.Builder builder = ArmourTechInfoPush.newBuilder();
		
		List<EquipResearchEntity> equipResearchEntityList = player.getData().getEquipResearchEntityList();
		for (EquipResearchEntity researchEntity : equipResearchEntityList) {
			
			// 装备研究信息
			ArmourTechInfo.Builder researchInfo = ArmourTechInfo.newBuilder();
			researchInfo.setArmourTechId(researchEntity.getResearchId());
			researchInfo.setArmourTechLevel(researchEntity.getResearchLevel());
			builder.addTechInfo(researchInfo);
			
			// 装备研究奖励信息
			Set<Integer> receiveBoxSet = researchEntity.getReceiveBoxSet();
			for (Integer receiveBox : receiveBoxSet) {
				ArmourTechAwardInfo.Builder awardInfo = ArmourTechAwardInfo.newBuilder();
				awardInfo.setArmourTechId(researchEntity.getResearchId());
				awardInfo.setRewardLevel(receiveBox);
				builder.addAwardInfo(awardInfo);
			}
		}
		builder.setUnlock(player.getEntity().getUnlockEquipResearch() > 0);
		builder.setShow(WorldPointService.getInstance().getShowEquipTech(player.getId()));
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ARMOUR_TECH_INFO_PUSH_VALUE, builder));
	}
	
	/**
	 * 同步个保法系列开关设定值
	 */
	public void syncPersonalProtectVals() {
	    List<Integer> switchVals = player.getData().getPersonalProtectListVals();
		PersonalProtectSwitchPB.Builder builder = PersonalProtectSwitchPB.newBuilder();
		builder.addAllSwitchVals(switchVals);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PERSONAL_PROTECT_SWITCH_SYNC_VALUE, builder));
	}

	/**
	 * 推送服务器活动配置版本信息
	 */
	public void syncActivityCfgVersionInfo() {
		try {
			ActivityCfgVersionInfoPush.Builder builder = ActivityCfgVersionInfoPush.newBuilder();
			Map<String, String> activityCfgVersionMap = ActivityConfigLoader.getInstance().getActivityCfgVersionMap();
			for (Entry<String, String> cfgVersion : activityCfgVersionMap.entrySet()) {
				ActivityCfgVersionInfo.Builder infoBuilder = ActivityCfgVersionInfo.newBuilder();
				infoBuilder.setActivityId(Integer.parseInt(cfgVersion.getKey()));
				infoBuilder.setVersion(cfgVersion.getValue());
				builder.addInfo(infoBuilder);
			}
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.ACTIVITY_CFG_VERION_INFO_PUSH_VALUE, builder));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	public void syncPlayerDressEditData(){
		DressEditDataSync.Builder builder = DressEditDataSync.newBuilder();
		String collegeName = WorldPointService.getInstance().getCollegeNameShow(this.player.getId());
		DressEditData.Builder collegeData = DressEditData.newBuilder();
		collegeData.setType(DressEditType.COLLEGE_SHOW);
		if(!HawkOSOperator.isEmptyString(collegeName)){
			collegeData.setData(collegeName);
		}
		builder.addDatas(collegeData);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DRESS_EDITDATA_SYNC_S_VALUE, builder));
	}
	
	public void updatePlayerDressEditData(){
		DressEditDataSync.Builder builder = DressEditDataSync.newBuilder();
		String collegeName = WorldPointService.getInstance().getCollegeNameShow(player.getId());
		DressEditData.Builder collegeData = DressEditData.newBuilder();
		collegeData.setType(DressEditType.COLLEGE_SHOW);
		if(!HawkOSOperator.isEmptyString(collegeName)){
			collegeData.setData(collegeName);
		}
		builder.addDatas(collegeData);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DRESS_EDITDATA_UPDATE_S_VALUE, builder));
	}
	
	
	public void syncMonsterKillData(){
		MonsterKillDataSync.Builder builder = MonsterKillDataSync.newBuilder();
		Map<Integer,Integer> killMap = player.getData().getMonsterEntity().getBosskillMap();
		Map<Integer,Integer> dropMap = player.getData().getMonsterEntity().getDropLimitMap();
		for(Map.Entry<Integer,Integer> entry : killMap.entrySet()){
			MonsterKillData.Builder kb = MonsterKillData.newBuilder();
			kb.setMonsterId(entry.getKey());
			kb.setKillCnt(entry.getValue());
			builder.addKillData(kb);
		}
		for(Map.Entry<Integer,Integer> entry : dropMap.entrySet()){
			MonsterDropLimitData.Builder kb = MonsterDropLimitData.newBuilder();
			kb.setItemId(entry.getKey());
			kb.setDropCnt(entry.getValue());
			builder.addDropData(kb);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.MONSTER_KILL_DATA_RESP_S_VALUE, builder));
	}
	
}
