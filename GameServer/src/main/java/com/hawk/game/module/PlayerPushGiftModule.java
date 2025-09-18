package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.CostDiamondBuyGiftEvent;
import com.hawk.activity.helper.PlayerAcrossDayLoginMsg;
import com.hawk.game.GsApp;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.PushGiftGroupCfg;
import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.entity.PushGiftEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.msg.ArmourIntensifyTimesMsg;
import com.hawk.game.msg.ArmourStarUpTimesMsg;
import com.hawk.game.msg.AtkAfterPveMsg;
import com.hawk.game.msg.AtkPlayerAfterWarMsg;
import com.hawk.game.msg.BuildingLevelUpMsg;
import com.hawk.game.msg.CommanderExpAddMsg;
import com.hawk.game.msg.CommanderLevlUpMsg;
import com.hawk.game.msg.DefPlayerAfterWarMsg;
import com.hawk.game.msg.EquipEnhanceMsg;
import com.hawk.game.msg.EquipLevelUpMsg;
import com.hawk.game.msg.EquipQueueSpeedMsg;
import com.hawk.game.msg.EquipResearchLevelUpMsg;
import com.hawk.game.msg.EquipResearchUnlockMsg;
import com.hawk.game.msg.HeroLevelUpMsg;
import com.hawk.game.msg.HeroStarUpMsg;
import com.hawk.game.msg.OneKeyHeroSkillUpMsg;
import com.hawk.game.msg.OneKeyHeroUpMsg;
import com.hawk.game.msg.PlantCrystalAnalysisChipMsg;
import com.hawk.game.msg.PlantInstrumentUpChipMsg;
import com.hawk.game.msg.PlantSoldierCrackChipMsg;
import com.hawk.game.msg.PlantTechnologyChipMsg;
import com.hawk.game.msg.PushGiftDeliverMsg;
import com.hawk.game.msg.QueueSpeedMsg;
import com.hawk.game.msg.SoldierStrengthenTechLevelUpMsg;
import com.hawk.game.msg.SuperLabLevelUpMsg;
import com.hawk.game.msg.SuperLabLvUpMsg;
import com.hawk.game.msg.UnlockHeroMsg;
import com.hawk.game.msg.UseItemMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Activity.PushGiftBuyReq;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Player.PlayerFlagPosition;
import com.hawk.game.protocol.PushGift.PushGiftNotifyDue;
import com.hawk.game.protocol.PushGift.PushGiftOper;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;
import com.hawk.game.service.pushgift.PushGiftConditionFromEnum;
import com.hawk.game.service.pushgift.PushGiftManager;
import com.hawk.game.util.BattleUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.GiftType;
import com.hawk.sdk.msdk.entity.PayItemInfo;

public class PlayerPushGiftModule extends PlayerModule {
	
	static Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 上一次tick推送礼包的时刻
	 */
	private long lastTickTime;
	
	
	public PlayerPushGiftModule(Player player) {
		super(player);
	}
	
	@Override
	public boolean onPlayerLogin() {
		PushGiftEntity pushGiftEntity = player.getData().getPushGiftEntity();
		PushGiftManager.getInstance().synPushGiftList(player, pushGiftEntity);
		
		//检测刷新时间是否已过
		long curTime = HawkApp.getInstance().getCurrentTime();
		lastTickTime = curTime;
		boolean update = false;
		Map<Integer, Integer> map = PushGiftGroupCfg.getGroupDateTimeIntervalMap();
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			int oldNextTime = pushGiftEntity.getGroupRefreshTimeMap().getOrDefault(entry.getKey(), 0);
			// 历史记录的刷新时间还没过，不需要更新刷新时间
			if (oldNextTime * 1000L > curTime) {
				continue;
			}
			
			update = true;
			long period = entry.getValue() * HawkTime.HOUR_MILLI_SECONDS;
			long nextTime = HawkTime.getNextAM0Date() - period;
			// 若当前时间处在一天内的最后一个周期，则不再更新刷新时间; 否则按周期依次递减，知道找到符合的区间再更新刷新时间
			if (nextTime > curTime && period > 0) {
				while (true) {
					if (nextTime >= curTime && nextTime - period <= curTime) {
						pushGiftEntity.getGroupRefreshTimeMap().put(entry.getKey(), (int) (nextTime / 1000));
						break;
					}
					
					nextTime -= period;
				}
			}
			
			pushGiftEntity.getGroupStatisticsMap().put(entry.getKey(), 0);
		}
		
		if (update) {
			pushGiftEntity.notifyUpdate();
		}
		
		return true;
	}
	
	public boolean onTick() {
		long curTime = HawkApp.getInstance().getCurrentTime();
		if (curTime - lastTickTime < 5000L) {
			return true;
		}
		
		// tick检测礼包刷新周期
		lastTickTime = curTime;
		boolean update = false;
		PushGiftEntity pushGiftEntity = player.getData().getPushGiftEntity();
		Map<Integer, Integer> map = PushGiftGroupCfg.getGroupDateTimeIntervalMap();
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			int oldNextTime = pushGiftEntity.getGroupRefreshTimeMap().getOrDefault(entry.getKey(), 0);
			// 历史记录的刷新时间还没过，不需要更新刷新时间
			if (oldNextTime * 1000L > curTime) {
				continue;
			}
			
			update = true;
			long nextTime = curTime + entry.getValue() * HawkTime.HOUR_MILLI_SECONDS;
			// 如果新算出的刷新时间跨天了，则不更新这个刷新时间，防止玩家跨天登录的时候时间判断上的麻烦。只要清一下统计数据就行
			if (nextTime < HawkTime.getNextAM0Date()) {
				pushGiftEntity.getGroupRefreshTimeMap().put(entry.getKey(), (int) (nextTime / 1000));
			}
			
			pushGiftEntity.getGroupStatisticsMap().put(entry.getKey(), 0);
		}
		
		if (update) {
			pushGiftEntity.notifyUpdate();
		}
		
		return true;
	}
	
	@Override
	public boolean onPlayerAssemble(){		
		PushGiftEntity entity = player.getData().getPushGiftEntity();
		int curSeconds = HawkTime.getSeconds();
		PushGiftGroupCfg groupCfg = null;
		PushGiftManager pushGfitManager = PushGiftManager.getInstance();
		List<Integer> removeIds = new ArrayList<Integer>();
		for (Entry<Integer, Integer> entry : entity.getGiftIdTimeMap().entrySet()) {
			groupCfg = pushGfitManager.getPushGiftGroupCfg(entry.getKey());
			//上线就开始计时
			if (entry.getValue() == 0) {				
				entry.setValue(curSeconds + groupCfg.getLimitTime());
				continue;
			}
			
			//服务器没有做倒计时，上线的时候触发一次.
			if (groupCfg.getLimitTime() > 0 && entry.getValue()  < curSeconds) {				
				removeIds.add(entry.getKey());
				logger.info("pushGiftTimeOut playerId:{}, gropuId:{}, endTime:{}", player.getId(), entry.getKey(), entry.getValue());
			}
		}
		
		removeIds.stream().forEach(id->entity.removeGiftIdTime(id));
		this.reset();
		return true;
	}

	/**
	 * pve战败触发
	 * @param msg
	 */
	@MessageHandler
	private void onPveFailMsg(AtkAfterPveMsg msg) {
		if (!msg.getOut().isAtkWin()) {
			List<ArmyInfo> armList = msg.getOut().getAftArmyMapAtk().get(player.getId());
			int woundedCount = BattleUtil.getWoundedCount(armList);
			AbstractPushGiftCondition condition = PushGiftManager.getInstance()
					.getCondition(PushGiftConditionEnum.ALL_ATTACK_FAIL.getType(), PushGiftConditionFromEnum.PVE_FAIL);
			condition.handle(player.getData(), Arrays.asList(woundedCount), player.isActiveOnline());
			
			//新增多条件.
			AbstractPushGiftCondition extraCondition = PushGiftManager.getInstance()
					.getCondition(PushGiftConditionEnum.ALL_ATTACK_FAIL_EXTRA.getType(), PushGiftConditionFromEnum.PVE_FAIL);
			extraCondition.handle(player.getData(), Arrays.asList(woundedCount, player.getCityLv()), player.isActiveOnline());
			
			checkSpecial(new ArrayList<>());
		}
	}
	
	/**
	 * 防守方战败触发
	 * @param msg
	 */
	@MessageHandler
	private void onPvpDefMsg(DefPlayerAfterWarMsg msg) {
		if (msg.isAtkWin()) {
			AbstractPushGiftCondition condition = PushGiftManager.getInstance()
					.getCondition(PushGiftConditionEnum.ALL_ATTACK_FAIL.getType(), PushGiftConditionFromEnum.PVP_DEF_FAIL);
			int deadCount = BattleUtil.getDeadCount(msg.getLeftArmyList());
			int woundedCount = BattleUtil.getWoundedCount(msg.getLeftArmyList());
			int allCount = deadCount + woundedCount;
			condition.handle(player.getData(), Arrays.asList(allCount), player.isActiveOnline());
			
			//新增多条件.
			AbstractPushGiftCondition extraCondition = PushGiftManager.getInstance()
					.getCondition(PushGiftConditionEnum.ALL_ATTACK_FAIL_EXTRA.getType(), PushGiftConditionFromEnum.PVP_DEF_FAIL);
			extraCondition.handle(player.getData(), Arrays.asList(allCount, player.getCityLv()), player.isActiveOnline());
			checkSpecial(new ArrayList<>());
		}
	}

	/**
	 * 进攻方战败触发
	 * @param msg
	 */
	@MessageHandler
	private void onPvpAtkMsg(AtkPlayerAfterWarMsg msg) {
		if (!msg.isAtkWin()) {
			AbstractPushGiftCondition condition = PushGiftManager.getInstance()
					.getCondition(PushGiftConditionEnum.ALL_ATTACK_FAIL.getType(), PushGiftConditionFromEnum.PVP_ATK_FAIL);
			int deadCount = BattleUtil.getDeadCount(msg.getBattleOutcome().getBattleArmyMapAtk().get(player.getId()));
			int woundedCount = BattleUtil.getWoundedCount(msg.getBattleOutcome().getBattleArmyMapAtk().get(player.getId()));
			int allCount = deadCount + woundedCount;
			condition.handle(player.getData(), Arrays.asList(allCount), player.isActiveOnline());
			
			//新增多条件.
			AbstractPushGiftCondition extraCondition = PushGiftManager.getInstance()
					.getCondition(PushGiftConditionEnum.ALL_ATTACK_FAIL_EXTRA.getType(), PushGiftConditionFromEnum.PVP_ATK_FAIL);
			extraCondition.handle(player.getData(), Arrays.asList(allCount, player.getCityLv()), player.isActiveOnline());
			checkSpecial(new ArrayList<>());
		}
	}

	/**
	 * 英雄升星触发
	 * @param msg
	 */
	@MessageHandler
	private void onHeroUpStarMsg(HeroStarUpMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance()
				.getCondition(PushGiftConditionEnum.HERO_STAR_UP.getType());
		for (int i = msg.getOldStar() + 1; i <= msg.getNewStar(); i++) {
			condition.handle(player.getData(), Arrays.asList(msg.getHeroId(), i), player.isActiveOnline());
		}
	}

	/**
	 * 英雄升级触发
	 * 
	 * @param msg
	 */
	@MessageHandler
	private void onHeroUpLevelMsg(HeroLevelUpMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance()
				.getCondition(PushGiftConditionEnum.HERO_LEVEL_UP.getType());
		for (int i = msg.getOldLevel() + 1; i <= msg.getNewLevel(); i++) {
			condition.handle(player.getData(), Arrays.asList(msg.getHeroId(), i), player.isActiveOnline());
		}
	}
	
	/**
	 * 建筑升级触发城建礼包
	 * @param msg
	 */
	@MessageHandler
	private void onBuildingLevelUpMsg(BuildingLevelUpMsg msg) {
		if (msg.getBuildingCfg().getProgress() > 0) {
			logger.info("thr building progress more than 0 playerId:{}", player.getId());
			return;
		}
		AbstractPushGiftCondition condition = PushGiftManager.getInstance()
				.getCondition(PushGiftConditionEnum.BUILDING_PAKCAGE.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getBuildingType(), msg.getCurLeve()), player.isActiveOnline());
		checkSpecial(Arrays.asList(msg.getBuildingType(), msg.getCurLeve()));
	}
	
	/**
	 * 指挥官升级触发指挥官礼包
	 * @param msg
	 */
	@MessageHandler
	private void onCommanderLevelUpMsg(CommanderLevlUpMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance()
				.getCondition(PushGiftConditionEnum.COMMANDER_PACKAGE.getType());
		for (int i = msg.getOldLevel() + 1; i <= msg.getNewLevel(); i++) {
			condition.handle(player.getData(), Arrays.asList(i), player.isActiveOnline());
		}		
	}
	
	/**
	 * 解锁英雄触发
	 * @param msg
	 */
	@MessageHandler
	private void onUnlockHeroMsg(UnlockHeroMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance()
				.getCondition(PushGiftConditionEnum.UNLOCK_HERO.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getQualityColor(), msg.getHeroId()), player.isActiveOnline());
	}
	
	/**
	 * 一键升级英雄等级触发
	 * @param msg
	 */
	@MessageHandler
	private void onOneKeyHeroUp(OneKeyHeroUpMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.ONE_KEY_HERO_UP.getType());
		condition.handle(player.getData(), new ArrayList<>(), player.isActiveOnline());
	}
	
	/**
	 * 一键升级英雄技能触发
	 * @param msg
	 */
	@MessageHandler
	private void onOneKeyHeroSkillUp(OneKeyHeroSkillUpMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.ONE_KEY_HERO_SKILL_UP.getType());
		condition.handle(player.getData(), new ArrayList<>(), player.isActiveOnline());
	}
	
	/**
	 * 造兵或治疗队列道具加速触发加速礼包
	 * 
	 * @param msg
	 */
	@MessageHandler
	private void onQueueSpeedMsg(QueueSpeedMsg msg) {
		AbstractPushGiftCondition condition = null;
		switch (msg.getSpeedType()) {
		case QueueType.SOILDER_QUEUE_VALUE:
			condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.TRAIN_SPEED.getType());
			break;
		case QueueType.CURE_QUEUE_VALUE:
			condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.CURE_SPEED.getType());
			break;
		default:
			break;
		}
		
		if (condition != null) {
			condition.handle(player.getData(), Arrays.asList(msg.getSpeedTime()), player.isActiveOnline());
		}
	}
	
	/**
	 * 使用特定道具触发礼包
	 * 
	 * @param msg
	 */
	@MessageHandler
	private void onUseToolMsg(UseItemMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.ITEM_CONSUME.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getItemId(), msg.getCount()), player.isActiveOnline());
	}
	
	/**
	 * 超能实验室升级部件触发礼包
	 * @param msg
	 */
	@MessageHandler
	private void onSuperLabLevelUpMsg(SuperLabLvUpMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.SUPER_LAB_LEVEL_UP.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getLevelUpTimes(), msg.getNextNeedItemNum()), player.isActiveOnline());
	}
	
	/**
	 * 装备研究升级触发礼包
	 * @param msg
	 */
	@MessageHandler
	private void onEquipResearchMsg(EquipQueueSpeedMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.EQUIP_RESEARCH.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getSpeedTime(), msg.getNeedItemId(), msg.getNextNeedItemNum()), player.isActiveOnline());
	}
	
	/**
	 * 装备强化触发礼包
	 * @param msg
	 */
	@MessageHandler
	private void onEquipEnhanceMsg(EquipEnhanceMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.EQUIP_ENHANCE.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getEnhanceTimes(), msg.getNextNeedItemNum()), player.isActiveOnline());
	}
	
	/**
	 * 指挥官经验增加触发礼包
	 * @param msg
	 */
	@MessageHandler
	private void onCommanderExpAddMsg(CommanderExpAddMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.COMMANDER_EXP.getType());
		condition.handle(player.getData(), Collections.emptyList(), player.isActiveOnline());
	}
	
	
	/**
	 * 客户端倒计时触发
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PUSH_GIFT_NOTIFY_DUE_VALUE)
	private void onPushGiftDue(HawkProtocol protocol) {
		PushGiftNotifyDue cparam = protocol.parseProtocol(PushGiftNotifyDue.getDefaultInstance());
		List<Integer> idList = cparam.getGiftIdsList();
		List<Integer> deleteList = new ArrayList<>();
		int curTime = HawkTime.getSeconds();
		PushGiftEntity pushGiftEntity = player.getData().getPushGiftEntity();
		Map<Integer, Integer> giftIdTimeMap = pushGiftEntity.getGiftIdTimeMap();
		//在客户端列表, 但是不在服务器列表
		List<Integer> clientDeleteList = new ArrayList<>();
		for (Integer id : idList) {
			Integer endTime = giftIdTimeMap.get(id);
			if (endTime == null) {
				//player.sendError(protocol.getType(), Status.Error.PUSH_GIFT_ID_NOT_EXIST_VALUE, 0);				
				clientDeleteList.add(id);
				HawkLog.errPrintln("pushGiftDue not exist id, playerId: {}, id: {}", player.getId(), id);
				continue;
			}
			
			//做一个时间容错
			if (endTime <= curTime || Math.abs(curTime - endTime) < 1) {
				deleteList.add(id);
			}
		}
		
		if (!deleteList.isEmpty()) {
			logger.info("pushGiftDue playerId:{}, deleteIds:{}", player.getId(), deleteList);
			deleteList.stream().forEach(id->pushGiftEntity.removeGiftIdTime(id));
			PushGiftManager.getInstance().updatePushGiftList(player, deleteList, PushGiftOper.DELETE);
		}
		
		if (!clientDeleteList.isEmpty()) {
			logger.info("pushGiftClientDeleteList playerId:{}, deleteIds:{}", player.getId(), clientDeleteList);
			PushGiftManager.getInstance().updatePushGiftList(player, clientDeleteList, PushGiftOper.DELETE);
		}
	}
	
	@MessageHandler
	private void onPushGiftBuy(PushGiftDeliverMsg msg) {		
		PushGiftEntity pushGiftEntity = player.getData().getPushGiftEntity();
		List<Integer> idList = AssembleDataManager.getInstance().getPushIdListByPayId(msg.getPayId());
		if (CollectionUtils.isEmpty(idList)) {
			logger.error("playerId:{}, payId:{} can not found push gift ", player.getId(), msg.getPayId());
			return;
		}		
		int id = idList.get(0);
		for (Entry<Integer, Integer> entry : pushGiftEntity.getGiftIdTimeMap().entrySet()) {
			if (idList.contains(entry.getKey())) {
				id = entry.getKey();
				break;
			}
		}							
		PushGiftLevelCfg pushGiftLevelCfg = HawkConfigManager.getInstance().getConfigByKey(PushGiftLevelCfg.class, id);
		PushGiftGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(PushGiftGroupCfg.class, pushGiftLevelCfg.getGroupId());
		if (groupCfg.getIsSale() != GsConst.PushGiftConst.SALE) {			
			pushGiftEntity.removeGiftIdTime(id);			
			logger.warn("pushGift config invliad playerId: {}, groupId: {}, giftId: {} ", player.getId(), groupCfg.getGroupId(), pushGiftLevelCfg.getLevel());			
			PushGiftManager.getInstance().updatePushGiftList(player, Arrays.asList(id), PushGiftOper.DELETE);			
			return;
		}				
		pushGiftEntity.removeGiftIdTime(id);
		List<Integer> deleteList = new ArrayList<>();
		deleteList.add(id);
		//删除关联的.
		List<Integer> specialIdList = this.getSpecialGroupCfg(false);
		if (specialIdList.contains(groupCfg.getGroupId())) {
			logger.info(" playerId:{}, delete relation id :{}", player.getId(), specialIdList);
			
			for (Entry<Integer, Integer> entry : pushGiftEntity.getGiftIdTimeMap().entrySet()) {
				PushGiftLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(PushGiftLevelCfg.class, entry.getKey());
				if (levelCfg == null) {
					logger.error("playerId:{} can not found giftid cfg id:{}", player.getId(), entry.getValue());
					continue;
				}
				PushGiftGroupCfg gCfg = HawkConfigManager.getInstance().getConfigByKey(PushGiftGroupCfg.class, levelCfg.getGroupId());
				if (gCfg == null) {
					continue;
				}
				
				if (specialIdList.contains(gCfg.getGroupId())) {
					deleteList.add(entry.getKey());
				}
			}
			
			for (Integer giftId : deleteList) {
				pushGiftEntity.removeGiftIdTime(giftId);
			}
		}		
		LogUtil.logGiftBagFlow(player, GiftType.PUSH_GIFT, String.valueOf(id), 0, 0, 0);				
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
		        .setPlayerId(player.getId())
		        .setMailId(MailId.PUSH_GIFT)
		        .addRewards(pushGiftLevelCfg.getCrystalRewardList())
		        .addRewards(pushGiftLevelCfg.getSpecialRewardList())
		        .addRewards(pushGiftLevelCfg.getOrdinaryRewardList())
		        .build());
		
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(pushGiftLevelCfg.getCrystalRewardList());		
		awardItems.addItemInfos(pushGiftLevelCfg.getSpecialRewardList());
		awardItems.addItemInfos(pushGiftLevelCfg.getOrdinaryRewardList());
		awardItems.rewardTakeAffectAndPush(player, Action.PUSH_GIFT_BUY, true, RewardOrginType.SHOPPING_GIFT);
				
		// 联盟成员发放礼物
		int allianceGift = pushGiftLevelCfg.getAllianceGift();
		if (!player.isCsPlayer() && player.hasGuild() && allianceGift > 0) {
			GuildService.getInstance().bigGift(player.getGuildId()).addSmailGift(allianceGift, false);
		}
		
		logger.info("pushGiftBuy playerId: {}, pushGiftId: {}", player.getId(), id);		
		PushGiftManager.getInstance().updatePushGiftList(player, deleteList, PushGiftOper.DELETE);			
	}
	
	@ProtocolHandler(code = HP.code.PUSH_GIFT_BUY_REQ_VALUE)
	private void onPushGiftBuy(HawkProtocol protocol) {
		PushGiftBuyReq cparam = protocol.parseProtocol(PushGiftBuyReq.getDefaultInstance());
		PushGiftEntity pushGiftEntity = player.getData().getPushGiftEntity();
		
		if (SystemControler.getInstance().isModuleClosed(ControlerModule.PUSH_GIFT)) {
			sendError(protocol.getType(), Status.SysError.MODULE_CLOSED);			
			return;
		}
		Integer endTime = pushGiftEntity.getGiftIdTimeMap().get(cparam.getLevelId());
		if (endTime == null) {
			player.sendError(protocol.getType(), Status.Error.PUSH_GIFT_ALEARDY_BOUGHT_VALUE, 0);
			logger.warn("pushGift already bought playerId{}, giftId: {}", player.getId(), cparam.getLevelId());
			return ;
		}
		
		if (endTime < HawkTime.getSeconds()) {
			logger.warn("pushGift time over playerId{}, giftId: {}", player.getId(), cparam.getLevelId());
			player.sendError(protocol.getType(),  Status.Error.PUSH_GIFT_SOLD_OUT, 0);
			return;
		}
		
		PushGiftLevelCfg pushGiftLevelCfg = HawkConfigManager.getInstance().getConfigByKey(PushGiftLevelCfg.class, cparam.getLevelId());
		PushGiftGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(PushGiftGroupCfg.class, pushGiftLevelCfg.getGroupId());
		if (groupCfg.getIsSale() != GsConst.PushGiftConst.SALE) {
			player.sendError(protocol.getType(), Status.SysError.CONFIG_ERROR_VALUE, 0);
			pushGiftEntity.removeGiftIdTime(cparam.getLevelId());			
			logger.warn("pushGift config invliad playerId: {}, groupId: {}, giftId: {} ", player.getId(), groupCfg.getGroupId(), pushGiftLevelCfg.getLevel());			
			PushGiftManager.getInstance().updatePushGiftList(player, Arrays.asList(cparam.getLevelId()), PushGiftOper.DELETE);
			return;
		}
		
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		List<ItemInfo> priceItems = pushGiftLevelCfg.getPriceList();
		ItemInfo priceItem = priceItems.get(0);
		consumeItems.addConsumeInfo(priceItems, false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		
		if (consumeItems.getBuilder().hasAttrInfo() && consumeItems.getBuilder().getAttrInfo().getDiamond() > 0) {
			try {
				if (!pushGiftLevelCfg.getCrystalRewardList().isEmpty()) {
					ItemInfo itemInfo = pushGiftLevelCfg.getCrystalRewardList().get(0);
					consumeItems.addPayItemInfo(new PayItemInfo(String.valueOf(itemInfo.getItemId()), 1, (int)itemInfo.getCount()));
				}
				
				if (!pushGiftLevelCfg.getSpecialRewardList().isEmpty()) {
					for (ItemInfo itemInfo : pushGiftLevelCfg.getSpecialRewardList()) {
						ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
						consumeItems.addPayItemInfo(new PayItemInfo(String.valueOf(itemInfo.getItemId()), itemCfg.getSellPrice(), (int)itemInfo.getCount()));
					}
				}
				
				if (!pushGiftLevelCfg.getOrdinaryRewardList().isEmpty()) {
					for (ItemInfo itemInfo : pushGiftLevelCfg.getOrdinaryRewardList()) {
						ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
						consumeItems.addPayItemInfo(new PayItemInfo(String.valueOf(itemInfo.getItemId()), itemCfg.getSellPrice(), (int)itemInfo.getCount()));
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		consumeItems.consumeAndPush(player, Action.PUSH_GIFT_BUY);
		pushGiftEntity.removeGiftIdTime(cparam.getLevelId());
		
		LogUtil.logGiftBagFlow(player, GiftType.PUSH_GIFT, String.valueOf(cparam.getLevelId()), (int)priceItem.getCount(), priceItem.getItemId(), 0);
				
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
		        .setPlayerId(player.getId())
		        .setMailId(MailId.PUSH_GIFT)
		        .addRewards(pushGiftLevelCfg.getCrystalRewardList())
		        .addRewards(pushGiftLevelCfg.getSpecialRewardList())
		        .addRewards(pushGiftLevelCfg.getOrdinaryRewardList())
		        .build());
		
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(pushGiftLevelCfg.getCrystalRewardList());		
		awardItems.addItemInfos(pushGiftLevelCfg.getSpecialRewardList());
		awardItems.addItemInfos(pushGiftLevelCfg.getOrdinaryRewardList());
		awardItems.rewardTakeAffectAndPush(player, Action.PUSH_GIFT_BUY, true, RewardOrginType.SHOPPING_GIFT);
		
		int costDiamond = GameUtil.getItemNumByItemId(pushGiftLevelCfg.getPriceList(), ItemType.PLAYER_ATTR, PlayerAttr.DIAMOND_VALUE);
		if (costDiamond > 0) {
			ActivityManager.getInstance().postEvent(new CostDiamondBuyGiftEvent(player.getId(), costDiamond));
		}
				
		// 联盟成员发放礼物
		int allianceGift = pushGiftLevelCfg.getAllianceGift();
		if (!player.isCsPlayer() && player.hasGuild() && allianceGift > 0) {
			GuildService.getInstance().bigGift(player.getGuildId()).addSmailGift(allianceGift, false);
		}
		
		logger.info("pushGiftBuy playerId: {}, pushGiftId: {}", player.getId(), cparam.getLevelId());		
		PushGiftManager.getInstance().updatePushGiftList(player, Arrays.asList(cparam.getLevelId()), PushGiftOper.DELETE);
		
		player.responseSuccess(protocol.getType());
	}
	
	@MessageHandler
	public void onCrossDay(PlayerAcrossDayLoginMsg msg) {
		// 记下下一周期开始的时间（此时会重置上一周期积攒的数据），后面只要玩家在线就进行tick比较，到了时间重置数据，并且更新这个时间，循环下去直到跨天
		PushGiftEntity pushGiftEntity = player.getData().getPushGiftEntity();
		long curTime = HawkTime.getMillisecond();
		Map<Integer, Integer> map = PushGiftGroupCfg.getGroupDateTimeIntervalMap();
		//这里不能简单地clear，因为有些需要统计计数，但不是要按周期重置的，比如指挥官经验礼包（其实指挥官经验礼包也可以不计数，但为了防止重复触发，方便一点的处理办法也就把数据记录到统计数据里面了）
		//pushGiftEntity.getGroupRefreshTimeMap().clear();
		//pushGiftEntity.getGroupStatisticsMap().clear();
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			long nextTime = curTime + entry.getValue() * HawkTime.HOUR_MILLI_SECONDS;
			pushGiftEntity.getGroupRefreshTimeMap().put(entry.getKey(), (int) (nextTime / 1000));
			pushGiftEntity.getGroupStatisticsMap().put(entry.getKey(), 0);
		}
				
		this.reset();
	}
	
	public void reset() {
		PushGiftEntity pushGiftEntity = player.getData().getPushGiftEntity();
		long curTime = HawkTime.getMillisecond();
		if (HawkTime.isSameDay(pushGiftEntity.getResetTime(), curTime)) {
			logger.warn("pushGiftReset same day ");
			return;
		}
		
		pushGiftEntity.getGroupRefreshCountMap().clear();
		pushGiftEntity.setResetTime(curTime);
	}
	
	public List<Integer> getSpecialGroupCfg(boolean needSale) {
		ConfigIterator<PushGiftGroupCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(PushGiftGroupCfg.class);
		List<Integer> cfgList = new ArrayList<>();
		while (configIterator.hasNext()) {
			PushGiftGroupCfg groupCfg = configIterator.next();
			if (needSale && groupCfg.getIsSale() != GsConst.PushGiftConst.SALE) {
				continue;
			}
			if (!groupCfg.isSpecial()) {
				continue;
			}
			cfgList.add(groupCfg.getGroupId());
		}
		
		return cfgList;
	}
	
	public void checkSpecial(List<Integer> paramList) {
		long newGiftStartTime = ConstProperty.getInstance().getNewPushGiftStartTimeValue();
		if (GsApp.getInstance().getServerOpenTime() < newGiftStartTime) {
			return;
		}
		//已经触发了不触发.
		if (player.getData().checkFlagSet(PlayerFlagPosition.PUSH_GIFT_SPECIAL)) {
			return;
		}
		List<Integer> cfgList = getSpecialGroupCfg(true);
		if (cfgList.isEmpty()) {
			return;
		}		
		AbstractPushGiftCondition condition = PushGiftManager.getInstance()
				.getCondition(PushGiftConditionEnum.SPECIAL.getType());
		boolean handleResult = condition.handle(player.getData(), paramList, player.isActiveOnline());
		if (handleResult) {
			player.getData().setFlag(PlayerFlagPosition.PUSH_GIFT_SPECIAL, 1);
		}				
	}
	
	/**
	 * 装备研究解锁触发
	 * 
	 * 礼包类型 = 2200
	 * 礼包条件：解锁装备技术研究时触发，且VIP等级在vipMin_vipMax之间 时触发
	 * 配置格式：vipMin_vipMax
	 * @param msg
	 */
	@MessageHandler
	private void onEquipResearchUnlock(EquipResearchUnlockMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.EQUIP_RESEARCH_UNLOCK.getType());
		condition.handle(player.getData(), Arrays.asList(0), player.isActiveOnline());
	}
	
	/**
	 * 装备研究强化到指定等级触发
	 * 
	 * 礼包类型 = 2300
	 * 礼包条件：装备研究强化至XX级，且VIP等级在vipMin_vipMax之间 时触发
	 * 配置格式：xx部件_XX等级_vipMin_vipMax
	 * @param msg
	 */
	@MessageHandler
	private void onEquipResearchLeveUpl(EquipResearchLevelUpMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.EQUIP_RESEARCH_LEVEL.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getResearchId(), msg.getResearchLevel()), player.isActiveOnline());
	}
	
	/**
	 * 装备强化到指定等级触发
	 * 
	 * 礼包类型 = 2400
	 * 礼包条件：单套装备强化至XX级，且VIP等级在vipMin_vipMax之间 时触发
	 * 配置格式：XX_vipMin_vipMax
	 * @param msg
	 */
	@MessageHandler
	private void onEquipLevelUp(EquipLevelUpMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.EQUIP_LEVEL.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getAfterLevel()), player.isActiveOnline());
	}
	
	/**
	 * 能量源等级礼包
 	 * 礼包类型 = 2600
 	 * 礼包条件：单套能量核心强化至XX级，且VIP等级在vipMin_vipMax之间 时触发
	 * 配置格式：XX_vipMin_vipMax
	 * @param msg
	 */
	@MessageHandler
	private void onLaboratoryLevel(SuperLabLevelUpMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.ENGRY.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getTotalLevel()), player.isActiveOnline());
	}
	
	/**
	 * 泰能强化
	 */
	@MessageHandler
	private void onPlantTechnology(PlantTechnologyChipMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.PUSH_GIFT_2700.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getPlantTechnologyTimes()), player.isActiveOnline());
		
		condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.PUSH_GIFT_3100.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getPlantType().getNumber()), player.isActiveOnline());
	}
	
	/**
	 * 破译仪器升级
	 */
	@MessageHandler
	private void onPlantIstrumentChip(PlantInstrumentUpChipMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.PUSH_GIFT_3300.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getLevel()), player.isActiveOnline());
	}
	
	/**
	 * 泰能战士破译
	 * @param msg
	 * 礼包类型 = 2800
	 * 礼包条件：泰能战士第X次破译完成，返回主界面，且VIP等级在vipMin_vipMax之间 时触发
	 * 配置格式：X_vipMin_vipMax
	 */
	@MessageHandler
	private void onPlantSoldierCrack(PlantSoldierCrackChipMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.PUSH_GIFT_2800.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getPlantSoldierCrackChipTimes()), player.isActiveOnline());
		
		condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.PUSH_GIFT_3400.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getLevel()), player.isActiveOnline());
	}
	
	/**
	 * 第X次将装备等级强化至Y级
	 * @param msg
	 * 礼包类型 = 2900
	 * 礼包条件：第X次将装备等级强化至Y级，返回主界面，且VIP等级在vipMin_vipMax之间 时触发
	 * 配置格式：X_vipMin_vipMax
	 */
	@MessageHandler
	private void onArmourIntensifyTimes(ArmourIntensifyTimesMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.PUSH_GIFT_2900.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getTimes(), msg.getLevel()), player.isActiveOnline());
	}
	
	/**
	 * 第X次将装备泰晶等级强化至Y级
	 * @param msg
	 */
	@MessageHandler
	private void onArmourStarUpTimes(ArmourStarUpTimesMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.PUSH_GIFT_3000.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getTimes(), msg.getLevel()), player.isActiveOnline());
	}
	
	/**
	 * 晶体分析
	 * @param msg
	 */
	@MessageHandler
	private void onPlantCrystalAnalysisChip(PlantCrystalAnalysisChipMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.PUSH_GIFT_3500.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getLevel()), player.isActiveOnline());
	}
	
	/**
	 * 泰能科技升级
	 * @param msg
	 */
	@MessageHandler
	private void onSoldierStrengthenTechLevelUpMsg(SoldierStrengthenTechLevelUpMsg msg) {
		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.PUSH_GIFT_3600.getType());
		condition.handle(player.getData(), Arrays.asList(msg.getSoldierId(), msg.getGroup(), msg.getLevel()), player.isActiveOnline());
	}
}
