package com.hawk.game.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.MapUtils;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.common.AccountRoleInfo;
import com.hawk.game.cfgElement.ArmourEffObject;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.config.ArmourChargeLabCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.MarchEmoticonProperty;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.config.VipShopCfg;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.EquipEntity;
import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.entity.GlobalBuffEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.entity.PlayerGachaEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.StatisticsEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.TalentEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.manager.IconManager;
import com.hawk.game.module.nationMilitary.entity.NationMilitaryEntity;
import com.hawk.game.module.nationMilitary.rank.NationMilitaryRankObj;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.equip.EquipSlot;
import com.hawk.game.protocol.Armour.ArmourAllInfoResp;
import com.hawk.game.protocol.Armour.ArmourAttr;
import com.hawk.game.protocol.Armour.ArmourAttrType;
import com.hawk.game.protocol.Armour.ArmourBriefInfo;
import com.hawk.game.protocol.Armour.ArmourInfo;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Armour.ArmourTechInfo;
import com.hawk.game.protocol.Armour.ArmourTechInfoPush;
import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Building.UnlockedAreaPB;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.Common.KeyValuePairStrStr;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.GachaType;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.StateType;
import com.hawk.game.protocol.CrossActivity.CrossGiftMiniPlayerMsg;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.protocol.Equip.EquipInfo;
import com.hawk.game.protocol.Equip.EquipState;
import com.hawk.game.protocol.Friend.GuardGiftMsg;
import com.hawk.game.protocol.Item.HPGachaInfo;
import com.hawk.game.protocol.Item.HPSyncGachaInfoResp;
import com.hawk.game.protocol.Item.ItemInfo;
import com.hawk.game.protocol.Item.VipShopItem;
import com.hawk.game.protocol.Login.MergeServerInfo;
import com.hawk.game.protocol.Mail.ChatRoomMember;
import com.hawk.game.protocol.Mail.MailLiteInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.protocol.Player.ImageSource;
import com.hawk.game.protocol.Player.LoginWay;
import com.hawk.game.protocol.Player.MiniPlayerMsg;
import com.hawk.game.protocol.Player.PlayerCommon;
import com.hawk.game.protocol.Player.PlayerInfo;
import com.hawk.game.protocol.Player.PlayerSnapshotPB;
import com.hawk.game.protocol.Player.StateInfo;
import com.hawk.game.protocol.Player.StatisticPB;
import com.hawk.game.protocol.Player.VipFlag;
import com.hawk.game.protocol.PushGift.PushGiftMsg;
import com.hawk.game.protocol.Queue.QueuePB;
import com.hawk.game.protocol.Recharge.GiftInfoSync;
import com.hawk.game.protocol.Recharge.MonthCardPuchaseInfoSync;
import com.hawk.game.protocol.RedisMail.ChatData;
import com.hawk.game.protocol.RedisMail.ChatMessage;
import com.hawk.game.protocol.RedisMail.ChatRoomData;
import com.hawk.game.protocol.RedisMail.MemberData;
import com.hawk.game.protocol.RedisMail.PlayerChatRoom;
import com.hawk.game.protocol.SimulateWar.SimulateWarBasePlayerStruct;
import com.hawk.game.protocol.Talent.TalentInfo;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.starwars.StarWarsOfficerService;
import com.hawk.game.recharge.RechargeType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 协议同步对象生成帮助类, 大部分接口的Player参数没有必要
 *
 * @author hawk
 */
public class BuilderUtil {
	/**
	 * 生成玩家协议同步信息
	 *
	 * @return
	 */
	public static PlayerInfo.Builder genPlayerBuilder(PlayerData playerData) {
        
		PlayerEntity playerEntity = playerData.getPlayerEntity();
		PlayerBaseEntity baseEntity = playerData.getPlayerBaseEntity();
		PlayerInfo.Builder builder = PlayerInfo.newBuilder();
		builder.setIsTBLYAnchor(false);
		builder.setPlayerId(playerEntity.getId());
		builder.setGold(baseEntity.getGold());	// 水晶
		builder.setCoin(baseEntity.getCoin());	// 暂未使用
		builder.setRecharge(baseEntity.getRecharge()); // 暂未使用
		builder.setDiamonds(baseEntity.getDiamonds()); // 钻石
		
		builder.setVipPoint(playerEntity.getVipExp());
		builder.setVipLevel(playerEntity.getVipLevel());
		builder.setCommon(BuilderUtil.genPlayerCommonBuilder(playerData));
		builder.setFreeVipPoint(playerEntity.getVipFreePoint());
		builder.setExp(baseEntity.getExp());
		builder.setIcon(playerEntity.getIcon());
		if (!HawkOSOperator.isEmptyString(playerEntity.getIconBuy())) {
			builder.setIconBuy(playerEntity.getIconBuy());
		} else {
			builder.setIconBuy("");
		}
		if (!HawkOSOperator.isEmptyString(playerData.getPfIcon())) {
			builder.setPfIcon(playerData.getPfIcon());
		}
		String imPfIcon = playerData.getIMPfIcon();
		if (!HawkOSOperator.isEmptyString(imPfIcon)) {
			String realUrl = IconManager.getInstance().getPficonByCrc(imPfIcon);
			if (!HawkOSOperator.isEmptyString(realUrl)) {
				builder.setOriginalPfIcon(realUrl);
			}			
		}
		
		builder.setGoldore(baseEntity.getGoldore());
		builder.setOil(baseEntity.getOil());
		builder.setSteel(baseEntity.getSteel());
		builder.setTombarthite(baseEntity.getTombarthite());
		
		builder.setGoldoreUnsafe(baseEntity.getGoldoreUnsafe());
		builder.setOilUnsafe(baseEntity.getOilUnsafe());
		builder.setSteelUnsafe(baseEntity.getSteelUnsafe());
		builder.setTombarthiteUnsafe(baseEntity.getTombarthiteUnsafe());
		
		builder.setName(playerEntity.getName());
		builder.setLevel(baseEntity.getLevel());
		builder.setBattlePoint(playerEntity.getBattlePoint());
		builder.setVit(playerEntity.getVit());
		builder.setVitTime(playerEntity.getVitTime());
		builder.setLastLoginTime(playerEntity.getLoginTime());
		builder.setBuyVitTimes(Math.max(0, playerData.getVitBuyTimesToday()));
		
		NationMilitaryEntity nationMilitaryEntity = playerData.getNationMilitaryEntity();
		// 国家军衔等级
		builder.setNationalMilitaryLv(nationMilitaryEntity.getNationMilitarLlevel());
		builder.setNationalMilitaryExp(nationMilitaryEntity.getNationMilitaryExp());
		builder.setNationalMilitaryReward(nationMilitaryEntity.getNationMilitaryReward() > 0);
		builder.setNationalMilitaryReset(NationMilitaryRankObj.getInstance().getNextResetTime());
		builder.setNationalMilitaryRankReset(NationMilitaryRankObj.getInstance().getNextDingBangTime());
		
		// 军衔等级、经验
		builder.setMilitaryRankLvl(GameUtil.getMilitaryRankByExp(playerEntity.getMilitaryExp()));
		builder.setMilitaryRankExp(playerEntity.getMilitaryExp());
		// 战争狂热结束时间
		builder.setWarFeverEndTime(baseEntity.getWarFeverEndTime());
		// 攻击迷雾要塞胜利次数
		builder.setAttackFoggyWinTimes(playerData.getDailyDataEntity().getMassAtkFoggyWinTimes());
		builder.setJoinAtkFoggyWinTimes(playerData.getDailyDataEntity().getJoinAtkFoggyWinTimes());
		
		boolean globalProtectBroken = GlobalData.getInstance().isBrokenProtect(playerData.getPlayerId());
		builder.setGlobalProtectEndTime(globalProtectBroken ? 0 : GlobalData.getInstance().getGlobalProtectEndTime());
		
		builder.setCityLevel(playerData.getConstructionFactoryLevel());
		builder.setCrRewardTimes(playerData.getDailyDataEntity().getCrRewardTimes());
		builder.setCrHighestScore(playerData.getDailyDataEntity().getCrHighestScore());
		builder.setSuperLab(playerEntity.getSuperLab());
		builder.setLaboratory(playerEntity.getLaboratory());
		
		builder.setLastInviteMass(playerData.getLastInviteMassTime());
		
		ArmourSuitType suitType = ArmourSuitType.valueOf(playerData.getPlayerEntity().getArmourSuit());
		if (suitType != null) {
			builder.setCurrArmourSuit(suitType);
		}
		
		builder.setUnlockASuitCount(playerData.getPlayerEntity().getArmourSuitCount());
		builder.setOldFlag(playerData.getPlayerEntity().getCreateTime() < ConstProperty.getInstance().getNewbieVersionTimeValue());
		builder.setSoulResetCd(playerData.getCommanderEntity().getSoulResetCd());
		builder.setMilitaryScore(baseEntity.getGuildMilitaryScore());
		builder.setCyborgScore(baseEntity.getCyborgScore());
		builder.setDyzzScore(baseEntity.getDyzzScore());
		return builder;
	}

	/**
	 * 生成物品同步协议信息builder
	 *
	 * @return
	 */
	public static ItemInfo.Builder genItemBuilder(ItemEntity itemEntity) {
		ItemInfo.Builder builder = ItemInfo.newBuilder();
		builder.setUuid(itemEntity.getId());
		builder.setItemId(itemEntity.getItemId());
		builder.setCount(itemEntity.getItemCount());
		builder.setIsNew(itemEntity.isNew());
		return builder;
	}

	/**
	 * 生成天赋同步协议信息builder
	 *
	 * @return
	 */
	public static TalentInfo.Builder genTalentBuilder(TalentEntity talentEntity) {
		TalentInfo.Builder builder = TalentInfo.newBuilder();
		builder.setId(talentEntity.getId());
		builder.setTalentId(talentEntity.getTalentId());
		builder.setLevel(talentEntity.getLevel());
		builder.setType(talentEntity.getType());
		return builder;
	}

	/**
	 * 创建建筑builder
	 * @param buildingEntity
	 * @return
	 */
	public static BuildingPB.Builder genBuildingBuilder(Player player, BuildingBaseEntity buildingEntity) {
		BuildingPB.Builder builder = BuildingPB.newBuilder();
		builder.setId(buildingEntity.getId());
		builder.setBuildCfgId(buildingEntity.getBuildingCfgId());
		builder.setIndex(buildingEntity.getBuildIndex());
		BuildingStatus status = BuildingStatus.valueOf(buildingEntity.getStatus());
		if (status != BuildingStatus.BUILDING_CREATING) {
			status = GameUtil.getBuildingStatus(player, buildingEntity);
		}
		
		builder.setStatus(status);
		if (buildingEntity.getLastResCollectTime() > 0) {
			builder.setLastRecruitsCollectTime(buildingEntity.getLastResCollectTime());
		}
		builder.setX(0);
		builder.setY(0);
		return builder;
	}
	
	/**
	 * 获取已解锁区块的builder
	 * @return
	 */
	public static UnlockedAreaPB.Builder getUnlockedAreaBuilder(Player player) {
		UnlockedAreaPB.Builder builder = UnlockedAreaPB.newBuilder();
	    Set<Integer> unlockedAreaSet = player.getData().getPlayerBaseEntity().getUnlockedAreaSet();
	    if (unlockedAreaSet.isEmpty()) {
	    	HawkLog.logPrintln("buildUnlockedAreaPB unnormal, unlockedAreaSet empty, playerId: {}", player.getId());
	    }
	    
	    builder.addAllUnlockedArea(unlockedAreaSet);
		return builder;
	}

	/**
	 * 创建队列builder
	 * @param queueEntity
	 * @return
	 */
	public static QueuePB.Builder genQueueBuilder(QueueEntity queueEntity) {
		QueuePB.Builder builder = QueuePB.newBuilder();
		builder.setEndTime(queueEntity.getEndTime());
		builder.setId(queueEntity.getId());
		builder.setItemId(queueEntity.getItemId());
		builder.setQueueType(QueueType.valueOf(queueEntity.getQueueType()));
		builder.setStartTime(queueEntity.getStartTime());
		builder.setInfo(String.valueOf(queueEntity.getBuildingType()));
		builder.setStatus(QueueStatus.valueOf(queueEntity.getStatus()));
		builder.setHelpTimes(queueEntity.getHelpTimes());
		builder.setTotalQueueTime(queueEntity.getTotalQueueTime());
		builder.setTotalReduceTime(queueEntity.getTotalReduceTime());
		builder.setPaidQueue(queueEntity.getEnableEndTime() > 0);
		builder.setMultiply(queueEntity.getMultiply());
		return builder;
	}

	/**
	 * @param entity
	 * @return
	 */
	public static StateInfo.Builder genStateInfoBuilder(StatusDataEntity entity) {
		StateInfo.Builder builder = StateInfo.newBuilder();
		builder.setType(entity.getType());
		builder.setKey(entity.getStatusId());
		builder.setValue(entity.getVal());
		builder.setStartTime(entity.getStartTime());
		builder.setEndTime(entity.getEndTime());
		if (!HawkOSOperator.isEmptyString(entity.getTargetId())) {
			builder.setTargetId(entity.getTargetId());
		}
		
		return builder;
	}
	
	public static StateInfo.Builder genStateInfoBuilder(GlobalBuffEntity entity) {
		StateInfo.Builder builder = StateInfo.newBuilder();
		builder.setType(StateType.BUFF_STATE_VALUE);
		builder.setKey(entity.getStatusId());
		builder.setValue(entity.getValue());
		builder.setStartTime(entity.getStartTime());
		builder.setEndTime(entity.getEndTime());
		
		return builder;
	}


//	/**
//	 * 生成邮件的builder
//	 * 
//	 * @param entity
//	 * @return
//	 */
//	public static MailLiteInfo.Builder genMailLiteBuilder(MailEntity entity) {
//		return genMailLiteBuilder(entity, null);
//	}
	
//	/**
//	 * 生成邮件的builder
//	 * 
//	 * @param entity
//	 * @return
//	 */
//	public static MailLiteInfo.Builder genMailLiteBuilder(MailEntity entity, List<String> tips) {
//		MailLiteInfo.Builder builder = MailLiteInfo.newBuilder();
//		builder.setId(entity.getUuid());
//		builder.setType(entity.getType());
//		builder.setMailId(entity.getMailId());
//		builder.setCtime(entity.getCreateTime());
//		builder.setLock(entity.getLocked());
//		builder.setStatus(entity.getStatus());
//		if (HawkOSOperator.isEmptyString(entity.getReward()) || entity.getRewardStatus() > 0) {
//			builder.setHasReward(false);
//		} else {
//			builder.setHasReward(true);
//		}
//		builder.addIcon(entity.getIcon());
//		if (!HawkOSOperator.isEmptyString(entity.getOppPlayerId())) {
//			PlayerSnapshotPB.Builder snapshot = SnapshotManager.getInstance().getPlayerSnapshot(entity.getOppPlayerId());
//			if (snapshot != null && !HawkOSOperator.isEmptyString(snapshot.getPfIcon())) {
//				builder.setPfIcon(snapshot.getPfIcon());
//			}
//		}
//		if (!HawkOSOperator.isEmptyString(entity.getTitle())) {
//			builder.setTitle(entity.getTitle());
//		}
//		if (!HawkOSOperator.isEmptyString(entity.getSubTitle())) {
//			builder.setSubTitle(entity.getSubTitle());
//		}
//		
//		if (tips != null && !tips.isEmpty()) {
//			builder.addAllTips(tips);
//		}
//
//		return builder;
//	}

	/**
	 * 生成私人邮件的builder
	 * 
	 * @param dataBuilder
	 * @return
	 */
	public static MailLiteInfo.Builder genMailLiteBuilder(PlayerChatRoom.Builder roomBuilder, ChatRoomData.Builder dataBuilder) {
		MailLiteInfo.Builder builder = MailLiteInfo.newBuilder();
		builder.setId(roomBuilder.getRoomId());
		builder.setType(MailService.getInstance().getMailType(MailId.CHAT_MAIL));
		builder.setMailId(MailId.CHAT_MAIL_VALUE);
		builder.setCtime(roomBuilder.getLastMsg());
		builder.setLock(roomBuilder.getLock());
		builder.setStatus(roomBuilder.getStatus());
		builder.setHasReward(false);
		builder.addIcon(0);
		try {
			// 聊天室成员数据
			if (dataBuilder != null) {
				builder.setChatType(dataBuilder.getChatType().getNumber());
				builder.setCreaterId(dataBuilder.getCreaterId());
				builder.setTitle(dataBuilder.getName());// 聊天室名

				List<MemberData> membersList = dataBuilder.getMembersList();
				String[] playerIds = new String[membersList.size()];
				int index = 0;
				for (MemberData memberData : membersList) {
					playerIds[index] = memberData.getPlayerId();
					index++;
				}

				Map<String, Player> snapshotMap = GlobalData.getInstance().getPlayerMap(playerIds);
				for (Player snapshot : snapshotMap.values()) {
					if(Objects.isNull(snapshot)){
						continue;
					}
					ChatRoomMember.Builder memBuilder = ChatRoomMember.newBuilder();
					memBuilder.setPlayerId(snapshot.getId());
					memBuilder.setName(snapshot.getName());
					memBuilder.setIcon(snapshot.getIcon());
					memBuilder.setVipLevel(snapshot.getVipLevel());
					memBuilder.setGuildTag(snapshot.getGuildTag());
					memBuilder.setCommon(BuilderUtil.genPlayerCommonBuilder(snapshot));
					if (!HawkOSOperator.isEmptyString(snapshot.getPfIcon())) {
						memBuilder.setPfIcon(snapshot.getPfIcon());
					}
					DressItem titleDress = WorldPointService.getInstance().getShowDress(snapshot.getId(), DressType.TITLE_VALUE);
					if (titleDress != null) {
						memBuilder.setDressTitle(titleDress.getModelType());
					} else {
						memBuilder.setDressTitle(0);
					}
					memBuilder.setDressTitleType(WorldPointService.getInstance().getDressTitleType(snapshot.getId()));
					builder.addMem(memBuilder);
				}
			}

			ChatMessage.Builder messageBuilder = LocalRedis.getInstance().getChatMessage(builder.getId());
			if (messageBuilder == null) {
				return builder;
			}

			List<ChatData.Builder> chatDatas = messageBuilder.getChatBuilderList();
			if (chatDatas == null || chatDatas.size() == 0) {
				return builder;
			}

			ChatData.Builder chatData = chatDatas.get(chatDatas.size() - 1);
			if (!HawkOSOperator.isEmptyString(chatData.getMessage()) && chatData.getMsgTime() - roomBuilder.getJoinTime() > -1000) {
				if (!HawkOSOperator.isEmptyString(chatData.getPlayerId())) {
					builder.setSubTitle(GlobalData.getInstance().getPlayerNameById(chatData.getPlayerId()) + ":" + chatData.getMessage());
				} else {
					builder.setSubTitle(chatData.getMessage());
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return builder;
	}

	public static HPSyncGachaInfoResp.Builder gachaInfoPB(PlayerData data) {
		HPSyncGachaInfoResp.Builder resp = HPSyncGachaInfoResp.newBuilder();
		
		for (GachaType gachaType : GachaType.values()) {
			
			PlayerGachaEntity entity = data.getGachaEntityByType(gachaType);
			
			// 获取数量
			int count = 0;
			for (PlayerGachaEntity thisEntity : data.getPlayerGachaEntities()) {
				if (entity.getGachaType() != thisEntity.getGachaType()) {
					continue;
				}
				count += thisEntity.getCount();
			}
			
			HPGachaInfo.Builder value = HPGachaInfo.newBuilder();
			value.setGachaType(entity.getGachaType())
					.setFreeTimesUsed(entity.getFreeTimesUsed())
					.setNextFree(entity.getNextFree())
					.setTotalCount(count)
					.setDayCount(entity.getDayCount());
			resp.addGacha(value);
		}
		return resp;
	}

	/**
	 * 获取礼包刷新的builder
	 * @param player
	 * @return
	 */
	public static GiftInfoSync.Builder getGiftListBuilder(Player player) {
		int priceType = GameUtil.getPriceType(player.getChannel()).intVal();		
		GiftInfoSync.Builder builder = GiftInfoSync.newBuilder();
		builder.setPriceType(priceType);
		long freeBoxTakenTime = LocalRedis.getInstance().getFreeBoxTakenTime(player.getId());
		ConfigIterator<PayGiftCfg> map = HawkConfigManager.getInstance().getConfigIterator(PayGiftCfg.class);
		for (PayGiftCfg payGiftCfg : map) {
			if (!payGiftCfg.getChannelType().equals(player.getPlatform())) {
				continue;
			}
			
			if (payGiftCfg.getMonthCardType() > 0) {
				continue;
			}
			
			int alreadyCount = 0;
			if (!payGiftCfg.isFree()) {
				alreadyCount = player.getData().getRechargeTimesToday(RechargeType.GIFT, payGiftCfg.getId());
			} else {
				// 月卡免费礼包
				long now = HawkApp.getInstance().getCurrentTime();
				boolean otherDay = !HawkTime.isSameDay(freeBoxTakenTime, now) && now - freeBoxTakenTime >= HawkTime.DAY_MILLI_SECONDS - 300000;
				alreadyCount = otherDay ? 0 : 1;
			}
			
			builder.addItems(payGiftCfg.toGoodsItem(alreadyCount));
		}

		return builder;
	}
	
	/**
	 * 获取月卡充值信息
	 * 
	 * @return
	 */
	public static MonthCardPuchaseInfoSync.Builder getMonthCardRechargeBuilder() {
		MonthCardPuchaseInfoSync.Builder builder = MonthCardPuchaseInfoSync.newBuilder();
		ConfigIterator<PayGiftCfg> map = HawkConfigManager.getInstance().getConfigIterator(PayGiftCfg.class);
		for (PayGiftCfg payGiftCfg : map) {
			if (payGiftCfg.getMonthCardType() <= 0) {
				continue;
			}
			
			builder.addMonthCardItems(payGiftCfg.toMonthCardItem());
		}
		
		return builder;
	}
	
	/**
	 * 构建玩家战斗相关统计信息
	 * @param entity
	 * @return
	 */
	public static StatisticPB.Builder genStatisticBuilder(StatisticsEntity entity) {
		int winCnt = entity.getWarWinCnt();
		int loseCnt = entity.getWarLoseCnt();
		StatisticPB.Builder builder = StatisticPB.newBuilder();
		builder.setWarWinCnt(entity.getWarWinCnt());
		builder.setWarLoseCnt(entity.getWarLoseCnt());
		builder.setAtkWinCnt(entity.getAtkWinCnt());
		builder.setAtkLoseCnt(entity.getAtkLoseCnt());
		builder.setDefWinCnt(entity.getDefWinCnt());
		if (winCnt + loseCnt == 0) {
			builder.setWinRate(0);
		} else {
			builder.setWinRate(entity.getWarWinCnt() * 100 / (entity.getWarWinCnt() + entity.getWarLoseCnt()));
		}
		builder.setSpyCnt(entity.getSpyCnt());
		builder.setArmyKillCnt(entity.getArmyKillCnt());
		builder.setArmyLoseCnt(entity.getArmyLoseCnt());
		builder.setArmyCureCnt(entity.getArmyCureCnt());
		return builder;
	}
	
	public static MiniPlayerMsg.Builder genMiniPlayer(String playerId) {
		return genMiniPlayer(GlobalData.getInstance().makesurePlayer(playerId));
	}
	
	public static MiniPlayerMsg.Builder genMiniPlayer(CrossPlayerStruct playerStruct) {
		MiniPlayerMsg.Builder sbuilder = MiniPlayerMsg.newBuilder();
		sbuilder.setIcon(playerStruct.getIcon());
		sbuilder.setPfIcon(playerStruct.getPfIcon() == null ? "" : playerStruct.getPfIcon());
		sbuilder.setOfflineTime(0);
		
		sbuilder.setPlayerId(playerStruct.getPlayerId());
		sbuilder.setPower(playerStruct.getBattlePoint());
		sbuilder.setPlayerName(playerStruct.getName());
		
		return sbuilder;
	}
	
	public static MiniPlayerMsg.Builder genMiniPlayer(Player player) {
		MiniPlayerMsg.Builder sbuilder = MiniPlayerMsg.newBuilder();
		sbuilder.setIcon(player.getIcon());
		sbuilder.setPfIcon(player.getPfIcon() == null ? "" : player.getPfIcon());
		if (GlobalData.getInstance().isOnline(player.getId())) {
			sbuilder.setOfflineTime(0);
		} else {
			sbuilder.setOfflineTime((int)player.getLogoutTime() / 1000);
		}
		
		sbuilder.setPlayerId(player.getId());
		sbuilder.setPower(player.getPower());
		sbuilder.setPlayerName(player.getName());
		
		return sbuilder;
	}
	
	public static CrossGiftMiniPlayerMsg.Builder genCrossGiftMiniPlayer(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		CrossGiftMiniPlayerMsg.Builder sbuilder = CrossGiftMiniPlayerMsg.newBuilder();
		sbuilder.setIcon(player.getIcon());
		sbuilder.setPfIcon(player.getPfIcon() == null ? "" : player.getPfIcon());
		if (GlobalData.getInstance().isOnline(player.getId())) {
			sbuilder.setOfflineTime(0);
		} else {
			sbuilder.setOfflineTime((int)player.getLogoutTime() / 1000);
		}
		sbuilder.setPlayerId(player.getId());
		sbuilder.setPower(player.getPower());
		sbuilder.setPlayerName(player.getName());
		return sbuilder;
	}
	
	/**
	 * 构建装备信息
	 * @param entity
	 * @return
	 */
	public static EquipInfo.Builder genEquipInfoBuilder(EquipEntity entity) {
		EquipInfo.Builder builder = EquipInfo.newBuilder()
				.setId(entity.getId())
				.setCfgId(entity.getCfgId())
				.setState(EquipState.valueOf(entity.getState()))
				.setIsNew(entity.isNew());
		return builder;
	}
	
	/**
	 * 获取部队类型建筑
	 */
	public static List<BuildingType> getArmyBuildingType() {
		List<BuildingType> types = new ArrayList<>();
		types.add(BuildingType.BARRACKS);
		types.add(BuildingType.WAR_FACTORY);
		types.add(BuildingType.REMOTE_FIRE_FACTORY);
		types.add(BuildingType.AIR_FORCE_COMMAND);
		return types;
	}
	
	/**
	 * 通过vip商城购买配置信息获取builder
	 * @param cfg
	 * @return
	 */
	public static VipShopItem.Builder getVipShopItemBuilder(VipShopCfg cfg) {
		VipShopItem.Builder builder = VipShopItem.newBuilder();
		builder.setShopId(cfg.getId());
		builder.setItemId(cfg.getItemId());
		builder.setVipLevel(cfg.getVipLevel());
		builder.setPrice(cfg.getPrice());
		builder.setDiscount(cfg.getDiscount());
		builder.setRemainBuyTimes(cfg.getNum());
		return builder;
	}
	
	/**
	 * builder keyvalue pair
	 * @param key
	 * @param value
	 * @return
	 */
	public static KeyValuePairStrStr.Builder buildKeyValuePairStrStr(String key, String value) {
		KeyValuePairStrStr.Builder keyValueBuilder = KeyValuePairStrStr.newBuilder();
		keyValueBuilder.setKey(key);
		keyValueBuilder.setValue(value);
		
		return keyValueBuilder;
		
	}
	
	public static List<KeyValuePairInt> buildKeyValuePairIntInt(Map<Integer, Integer> map) {
		if (MapUtils.isEmpty(map)) {
			return new ArrayList<>();
		} 
		
		List<KeyValuePairInt> builderList = new ArrayList<>();
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			builderList.add(buildKeyValuePairIntInt(entry.getKey(), entry.getValue()).build());
		}
		
		return builderList;
	}
	
	public static KeyValuePairInt.Builder buildKeyValuePairIntInt(Integer key, Integer value) {
		KeyValuePairInt.Builder builder = KeyValuePairInt.newBuilder();
		builder.setKey(key);
		builder.setVal(value);
		
		return builder;
	}
	
	/**
	 * 推送礼包
	 * @param entity
	 * @return
	 */
	public static PushGiftMsg.Builder buildPushGiftMsg(int giftId, long endTime) {
		PushGiftMsg.Builder builder = PushGiftMsg.newBuilder();
		builder.setGiftId(giftId);
		builder.setEndTime(endTime);
		
		return builder;
	}
	
	/**
	 * 构建玩家快照信息
	 * 
	 * @param playerData
	 * @return
	 */
	public static PlayerSnapshotPB buildSnapshotData(Player player) {
		if (player == null) {
			return null;
		}
		PlayerData playerData = player.getData();
		
		PlayerSnapshotPB.Builder builder = PlayerSnapshotPB.newBuilder();
		builder.setIpBelongsAddr(player.getIpBelongsAddr());
		builder.setCityPlantLevel(player.getCityPlantLv());
		builder.setAchievement(playerData.getAchievePoint());
		builder.setServerName(player.getServerId());
		builder.setCityLevel(playerData.getConstructionFactoryLevel());
		builder.setPlayerId(playerData.getPlayerEntity().getId());
		builder.setPuid(playerData.getPlayerEntity().getPuid());
		builder.setName(playerData.getPlayerEntity().getName());
		builder.setLevel(playerData.getPlayerBaseEntity().getLevel());
		builder.setIcon(playerData.getPlayerEntity().getIcon());
		if(!HawkOSOperator.isEmptyString(playerData.getPfIcon())) {
			builder.setPfIcon(playerData.getPfIcon());
		}
		builder.setPower(playerData.getPlayerEntity().getBattlePoint());
		builder.setLanguage(playerData.getPlayerEntity().getLang());
		builder.setVip(playerData.getPlayerEntity().getVipLevel());
		builder.setCommon(BuilderUtil.genPlayerCommonBuilder(playerData));
		builder.setLively(playerData.isLively());
		builder.setLivelyMask(playerData.getPlayerEntity().getLivelyMask());
		builder.setNationalMilitaryLv(playerData.getNationMilitaryEntity().getNationMilitarLlevel());
		//联盟信息
		String guildId = GuildService.getInstance().getPlayerGuildId(playerData.getPlayerEntity().getId());
		if (GuildService.getInstance().isGuildExist(guildId)) {
			builder.setGuildId(guildId);
			builder.setGuildName(GuildService.getInstance().getGuildName(guildId));
			builder.setGuildTag(GuildService.getInstance().getGuildTag(guildId));
		} else {
			builder.setGuildId("");
			builder.setGuildName("");
			builder.setGuildTag("");
		}
		
		// 设置登出时间
		builder.setLogoutTime(playerData.getPlayerEntity().getLogoutTime());
		
		// 军衔
		builder.setMilitaryRankLvl(GameUtil.getMilitaryRankByExp(playerData.getPlayerEntity().getMilitaryExp()));
		
		// 统计次数
		StatisticsEntity entity = playerData.getStatisticsEntity();
		builder.setStatInfo(BuilderUtil.genStatisticBuilder(entity));
		for (EquipSlot slot : playerData.getCommanderObject().getEquipSlots()) {
			builder.addEquipSlot(slot.toPBObject());
		}
		PlayerSnapshotPB build = builder.build();
		return build;
	}
	
	/**
	 * 玩家通用数据
	 * @param player
	 * @return
	 */
	public static PlayerCommon.Builder genPlayerCommonBuilder(Player player) {
		return genPlayerCommonBuilder(player.getData());
	}
	
	/**
	 * 玩家通用数据
	 * @param player
	 * @return
	 */
	public static PlayerCommon.Builder genPlayerCommonBuilder(PlayerData playerData) {
		PlayerCommon.Builder builder = PlayerCommon.newBuilder();
		VipFlag vipFlag = VipFlag.valueOf(playerData.getVipFlag());
		builder.setVipFlag(vipFlag == null ? VipFlag.VIP_FLAG_COMMON : vipFlag);
		AccountRoleInfo ari = GlobalData.getInstance().getAccountRoleInfo(playerData.getPlayerId());
		builder.setLoginWay(LoginWay.valueOf(playerData.getPlayerEntity().getLoginWay()));
		if (ari != null && ari.getQqSVIPLevel() > 0) {
			builder.setSvipLevel(ari.getQqSVIPLevel());
		}
		builder.setOfficeId(GameUtil.getPresidentOfficerId(playerData.getPlayerId()));
		builder.setStarWarOfficeId(StarWarsOfficerService.getInstance().getPlayerOfficerId(playerData.getPlayerId()));
		builder.setServerId(GlobalData.getInstance().getMainServerId(playerData.getPlayerEntity().getServerId()));
		builder.addAllPersonalProtectSwitch(playerData.getPersonalProtectListVals());
		return builder;
	}
	
	public static MergeServerInfo.Builder buildMergeServerInfo(String mainServerId, List<String> mergeServerIds) {
		MergeServerInfo.Builder builder = MergeServerInfo.newBuilder();
		builder.setMainServerId(mainServerId);
		builder.addAllMergeServerIds(mergeServerIds);
		
		return builder;
	}

	/**
	 * 构建所有铠甲信息
	 */
	public static ArmourAllInfoResp.Builder genAllArmourInfoBuilder(PlayerData playerData) {
		ArmourAllInfoResp.Builder builder = ArmourAllInfoResp.newBuilder();
		List<ArmourEntity> armours = playerData.getArmourEntityList();
		for (ArmourEntity armour : armours) {
			try {
				builder.addArmourInfo(genArmourInfoBuilder(armour));
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return builder;
	}
	
	/**
	 * 构建单个铠甲信息
	 */
	public static ArmourInfo.Builder genArmourInfoBuilder(ArmourEntity armour) {
		ArmourInfo.Builder armourInfo = ArmourInfo.newBuilder();
		armourInfo.setArmourId(armour.getId());
		armourInfo.setCfgId(armour.getArmourId());
		armourInfo.setLevel(armour.getLevel());
		armourInfo.setQuality(armour.getQuality());
		
		if (armour.isLock()) {
			armourInfo.setIsLock(armour.isLock());
		}
		
		// 额外属性
		for (ArmourEffObject extrEff : armour.getExtraAttrEff()) {
			ArmourAttr.Builder extrAttrInfo = ArmourAttr.newBuilder();
			extrAttrInfo.setAttrId(extrEff.getAttrId());
			extrAttrInfo.setAttrType(extrEff.getEffectType());
			extrAttrInfo.setAttrValue(extrEff.getEffectValue());
			ArmourAttrType attrType = armour.isSuper() ? ArmourAttrType.SUPER_EXTR : ArmourAttrType.EXTR; 
			extrAttrInfo.setType(attrType);
			armourInfo.addAttr(extrAttrInfo);
		}
		
		// 特技属性
		for (ArmourEffObject skillEff : armour.getSkillEff()) {
			ArmourAttr.Builder skillAttrInfo = ArmourAttr.newBuilder();
			skillAttrInfo.setAttrId(skillEff.getAttrId());
			skillAttrInfo.setAttrType(skillEff.getEffectType());
			skillAttrInfo.setAttrValue(skillEff.getEffectValue());
			skillAttrInfo.setType(ArmourAttrType.SPECIAL);
			armourInfo.addAttr(skillAttrInfo);
		}
		
		// 穿戴套装
		for (Integer suit : armour.getSuitSet()) {
			armourInfo.addSuit(ArmourSuitType.valueOf(suit));
		}
		
		if (armour.isSuper()) {
			armourInfo.setEndTime(armour.getEndTime());
		}
		
		if (armour.getStar() > 0) {
			armourInfo.setStarLevel(armour.getStar());
			armourInfo.setStarAttrNum(armour.getStarEff().size());
			for (ArmourEffObject starEff : armour.getStarEff()) {
				ArmourAttr.Builder starAttrInfo = ArmourAttr.newBuilder();
				starAttrInfo.setAttrId(starEff.getAttrId());
				
				ArmourChargeLabCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ArmourChargeLabCfg.class, starEff.getAttrId());
				EffectObject attributeValue = cfg.getAttributeValue();
				starAttrInfo.setAttrType(attributeValue.getEffectType());
				starAttrInfo.setAttrValue(attributeValue.getEffectValue());
				
				starAttrInfo.setRate(starEff.getRate());
				starAttrInfo.setReplaceAttrId(starEff.getReplaceAttrId());
				starAttrInfo.setType(ArmourAttrType.STAR_EXTR);
				starAttrInfo.setBreakthrough(starEff.getBreakthrough());
				armourInfo.addStarAttr(starAttrInfo);
			}
		}
		if(armour.getQuantum() > 0){
			armourInfo.setQuantumLevel(armour.getQuantum());
		}
		return armourInfo;
	}
	
	/**
	 * 构建单个铠甲简要信息
	 */
	public static ArmourInfo.Builder genArmourBriefInfoBuilder(ArmourEntity armour) {
		ArmourInfo.Builder armourInfo = ArmourInfo.newBuilder();
		armourInfo.setArmourId(armour.getId());
		armourInfo.setCfgId(armour.getArmourId());
		armourInfo.setLevel(armour.getLevel());
		armourInfo.setQuality(armour.getQuality());
		
		if (armour.getStar() > 0) {
			armourInfo.setStarLevel(armour.getStar());
			armourInfo.setStarAttrNum(armour.getStarEff().size());
			for (ArmourEffObject starEff : armour.getStarEff()) {
				ArmourAttr.Builder starAttrInfo = ArmourAttr.newBuilder();
				starAttrInfo.setAttrId(starEff.getAttrId());
				
				ArmourChargeLabCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ArmourChargeLabCfg.class, starEff.getAttrId());
				EffectObject attributeValue = cfg.getAttributeValue();
				starAttrInfo.setAttrType(attributeValue.getEffectType());
				starAttrInfo.setAttrValue(attributeValue.getEffectValue());
				
				starAttrInfo.setRate(starEff.getRate());
				starAttrInfo.setReplaceAttrId(starEff.getReplaceAttrId());
				starAttrInfo.setType(ArmourAttrType.STAR_EXTR);
				starAttrInfo.setBreakthrough(starEff.getBreakthrough());
				armourInfo.addStarAttr(starAttrInfo);
			}
		}
		
		if (armour.isSuper()) {
			// 额外属性
			for (ArmourEffObject extrEff : armour.getExtraAttrEff()) {
				ArmourAttr.Builder extrAttrInfo = ArmourAttr.newBuilder();
				extrAttrInfo.setAttrId(extrEff.getAttrId());
				extrAttrInfo.setAttrType(extrEff.getEffectType());
				extrAttrInfo.setAttrValue(extrEff.getEffectValue());
				ArmourAttrType attrType = armour.isSuper() ? ArmourAttrType.SUPER_EXTR : ArmourAttrType.EXTR; 
				extrAttrInfo.setType(attrType);
				armourInfo.addAttr(extrAttrInfo);
			}
			
			// 特技属性
			for (ArmourEffObject skillEff : armour.getSkillEff()) {
				ArmourAttr.Builder skillAttrInfo = ArmourAttr.newBuilder();
				skillAttrInfo.setAttrId(skillEff.getAttrId());
				skillAttrInfo.setAttrType(skillEff.getEffectType());
				skillAttrInfo.setAttrValue(skillEff.getEffectValue());
				skillAttrInfo.setType(ArmourAttrType.SPECIAL);
				armourInfo.addAttr(skillAttrInfo);
			}
			
			armourInfo.setEndTime(armour.getEndTime());
			
			armourInfo.setIsLock(armour.isLock());
		}
		if(armour.getQuantum() > 0){
			armourInfo.setQuantumLevel(armour.getQuantum());
		}
		return armourInfo;
	}
	
	/**
	 * 铠甲简要信息
	 */
	public static ArmourBriefInfo.Builder genArmourBriefInfo(PlayerData playerData) {
		ArmourBriefInfo.Builder builder = ArmourBriefInfo.newBuilder();
		int suit = playerData.getPlayerEntity().getArmourSuit();
		List<ArmourEntity> suitArmours = playerData.getSuitArmours(suit);
		for (ArmourEntity armour : suitArmours) {
			builder.addArmourInfo(genArmourBriefInfoBuilder(armour));
		}
		// 装备个保法开关,22写死就行
		builder.setArmourStarAttrProtectValue(playerData.getIndexedProtectSwitchVal(22));
		return builder;
	}
	
	/**
	 * 装备科技简要信息
	 */
	public static ArmourTechInfoPush.Builder genArmourEquipBriefInfo(PlayerData playerData) {
		ArmourTechInfoPush.Builder builder = ArmourTechInfoPush.newBuilder();
		List<EquipResearchEntity> equipResearchEntityList = playerData.getEquipResearchEntityList();
		for (EquipResearchEntity researchEntity : equipResearchEntityList) {
			// 装备研究信息
			ArmourTechInfo.Builder researchInfo = ArmourTechInfo.newBuilder();
			researchInfo.setArmourTechId(researchEntity.getResearchId());
			researchInfo.setArmourTechLevel(researchEntity.getResearchLevel());
			builder.addTechInfo(researchInfo);
		}
		builder.setUnlock(playerData.getPlayerEntity().getUnlockEquipResearch() > 0);
		return builder;
	}
	/**
	 * 铠甲战斗信息(战报用)
	 */
	public static ArmourBriefInfo.Builder genArmourBattleInfo(PlayerData playerData, int suit) {
		ArmourBriefInfo.Builder builder = ArmourBriefInfo.newBuilder();
		ArmourSuitType suitType = ArmourSuitType.valueOf(suit);
		if (suitType != ArmourSuitType.ARMOUR_NONE) {
			List<ArmourEntity> suitArmours = playerData.getSuitArmours(suit);
			for (ArmourEntity armour : suitArmours) {
				builder.addArmourInfo(genArmourInfoBuilder(armour));
			}
		}
		// 装备个保法开关,22写死就行
		builder.setArmourStarAttrProtectValue(playerData.getIndexedProtectSwitchVal(22));
		// 装备研究信息
		List<EquipResearchEntity> equipResearchEntityList = playerData.getEquipResearchEntityList();
		for (EquipResearchEntity researchEntity : equipResearchEntityList) {
			ArmourTechInfo.Builder researchInfo = ArmourTechInfo.newBuilder();
			researchInfo.setArmourTechId(researchEntity.getResearchId());
			researchInfo.setArmourTechLevel(researchEntity.getResearchLevel());
			builder.addArmourTechInfo(researchInfo);
		}
		builder.setSuitType(suitType);
		return builder;
	}
	
	/**
	 * 守护赠送礼包
	 * @param giftId
	 * @param num
	 * @return
	 */
	public static GuardGiftMsg.Builder buildGuardGift(int giftId, int num) {
		GuardGiftMsg.Builder builder = GuardGiftMsg.newBuilder();
		builder.setGiftId(giftId);
		builder.setSentNum(num);
		
		return builder;
	}
	
	/**
	 * 
	 * @param player
	 * @return
	 */
	public static CrossPlayerStruct.Builder buildCrossPlayer(Player player) {
		CrossPlayerStruct.Builder builder = CrossPlayerStruct.newBuilder();
		builder.setPlayerId(player.getId());
		builder.setName(player.getName());
		builder.setBattlePoint(player.getPower());
		builder.setGuildID(player.getGuildId());
		String guildName = player.getGuildName();
		//工会名字.
		if (HawkOSOperator.isEmptyString(guildName)) {
			guildName="";
		}
		builder.setGuildName(guildName);
		builder.setGuildTag(player.getGuildTag());
		builder.setServerId(player.getServerId());
		builder.setMainServerId(player.getMainServerId());
		builder.setIcon(player.getIcon());
		if (HawkOSOperator.isEmptyString(player.getPfIcon())) {
			builder.setPfIcon("");
		} else {
			String[] icons = player.getPfIcon().split("_");						 
			 if (icons[0].equals(ImageSource.FROMIM_VALUE+"")) {
				 builder.setPfIcon(icons[0] + "_" + 
						 player.getData().getIMPfIcon() + "_" + icons[2]); 
			 } else {
				 builder.setPfIcon(player.getPfIcon());
			 }	 
		}
		builder.setGuildFlag(GuildService.getInstance().getGuildFlag(player.getGuildId()));
		
		return builder;
	}
	
	/**
	 * 构建攻防模拟战的结构体.
	 * @param player
	 * @return
	 */
	public static SimulateWarBasePlayerStruct.Builder buildSimulateWarBasePlayerStruct(Player player) {
		SimulateWarBasePlayerStruct.Builder builder = SimulateWarBasePlayerStruct.newBuilder();
		builder.setBattlePoint(player.getPower());
		builder.setPlayerId(player.getId());
		builder.setName(player.getName());
		//builder.setCityLevel(player.getCityLevel());
		builder.setGuildFlag(player.getGuildFlag());
		builder.setGuildID(player.getGuildId());
		builder.setGuildTag(player.getGuildTag());		
		builder.setServerId(player.getServerId());
		//builder.setLevel(player.getLevel());
		builder.setCityPlantLevel(player.getCityPlantLv());
		builder.setIcon(player.getIcon());
		builder.setPfIcon(player.getPfIcon());
		
		return builder;
	}
	
	public static void buildMarchEmotion(WorldPointPB.Builder builder, WorldMarch march) {
		if (march != null && march.getEmoticonUseTime() > 0
				&& march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_VALUE
				&& march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			// 停留类行军
			long period = MarchEmoticonProperty.getInstance().getEmoticonPeriod();
			if (march.getEmoticonUseTime() > march.getReachTime() && march.getEmoticonUseTime() + period > HawkTime.getMillisecond()) {
				builder.setEmoticonId(march.getEmoticon());
				builder.setEmoticonEndTime(march.getEmoticonUseTime() + period);
			}
		}
	}

	public static void buildPlayerEmotion(WorldPointPB.Builder builder, long emoticonUseTime, int emoticon) {
		if (emoticonUseTime <= 0) {
			return;
		}
		long period = MarchEmoticonProperty.getInstance().getEmoticonPeriod();
		if (emoticonUseTime + period > HawkTime.getMillisecond()) {
			builder.setEmoticonId(emoticon);
			builder.setEmoticonEndTime(emoticonUseTime + period);
		}
	}

	public static void buildMarchEmotion(WorldPointDetailPB.Builder builder, WorldMarch march) {
		if (march != null && march.getEmoticonUseTime() > 0
				&& march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_VALUE
				&& march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			// 停留类行军
			long period = MarchEmoticonProperty.getInstance().getEmoticonPeriod();
			if (march.getEmoticonUseTime() > march.getReachTime() && march.getEmoticonUseTime() + period > HawkTime.getMillisecond()) {
				builder.setEmoticonId(march.getEmoticon());
				builder.setEmoticonEndTime(march.getEmoticonUseTime() + period);
			}
		}
	}

	public static void buildPlayerEmotion(WorldPointDetailPB.Builder builder, long emoticonUseTime, int emoticon) {
		if (emoticonUseTime <= 0) {
			return;
		}
		long period = MarchEmoticonProperty.getInstance().getEmoticonPeriod();
		if (emoticonUseTime + period > HawkTime.getMillisecond()) {
			builder.setEmoticonId(emoticon);
			builder.setEmoticonEndTime(emoticonUseTime + period);
		}
	}
}
