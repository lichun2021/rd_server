package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.hawk.game.cfgElement.*;
import com.hawk.log.LogConst;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.ArmourStrengthenEvent;
import com.hawk.activity.event.impl.ArmourTechLevelUpEvent;
import com.hawk.activity.event.impl.EquipQualityAchiveEvent;
import com.hawk.activity.event.impl.PlanetExploreLevelUpEvent;
import com.hawk.activity.type.impl.equipCraftsman.EquipCarftsmanActivity;
import com.hawk.activity.type.impl.equipCraftsman.cfg.EquipCarftsmanGachaCfg;
import com.hawk.activity.type.impl.equipCraftsman.entity.EquipCarftsmanEntity;
import com.hawk.activity.type.impl.equipCraftsman.item.EquipCarftsmanItem;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.config.ArmourAdditionalCfg;
import com.hawk.game.config.ArmourBreakthroughCfg;
import com.hawk.game.config.ArmourCfg;
import com.hawk.game.config.ArmourChargeLabCfg;
import com.hawk.game.config.ArmourConstCfg;
import com.hawk.game.config.ArmourLevelCfg;
import com.hawk.game.config.ArmourQuantumConsumeCfg;
import com.hawk.game.config.ArmourStarConsumeCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.EquipResearchCfg;
import com.hawk.game.config.EquipResearchLevelCfg;
import com.hawk.game.config.EquipResearchRewardCfg;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.entity.DailyDataEntity;
import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.entity.PushGiftEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ArmourAttrTemplate;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.msg.ArmourIntensifyTimesMsg;
import com.hawk.game.msg.ArmourStarUpTimesMsg;
import com.hawk.game.msg.EquipEnhanceMsg;
import com.hawk.game.msg.EquipLevelUpMsg;
import com.hawk.game.msg.EquipResearchLevelUpMsg;
import com.hawk.game.msg.EquipResearchQueueFinishMsg;
import com.hawk.game.msg.EquipResearchUnlockMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Activity.EquipCraftsmanInheritReq;
import com.hawk.game.protocol.Armour;
import com.hawk.game.protocol.Armour.ArmourAttr;
import com.hawk.game.protocol.Armour.ArmourAttrBreakthroughReq;
import com.hawk.game.protocol.Armour.ArmourBreakthroughReq;
import com.hawk.game.protocol.Armour.ArmourInheritReq;
import com.hawk.game.protocol.Armour.ArmourIntensifyReq;
import com.hawk.game.protocol.Armour.ArmourLockReq;
import com.hawk.game.protocol.Armour.ArmourPutOnReq;
import com.hawk.game.protocol.Armour.ArmourResolveByQualityReq;
import com.hawk.game.protocol.Armour.ArmourResolveReq;
import com.hawk.game.protocol.Armour.ArmourStarAttrOpenReq;
import com.hawk.game.protocol.Armour.ArmourStarAttrOpenResp;
import com.hawk.game.protocol.Armour.ArmourStarAttrRandomReq;
import com.hawk.game.protocol.Armour.ArmourStarAttrReplaceReq;
import com.hawk.game.protocol.Armour.ArmourStarAttrReplaceResp;
import com.hawk.game.protocol.Armour.ArmourStarAttrUpReq;
import com.hawk.game.protocol.Armour.ArmourStarAttrUpResp;
import com.hawk.game.protocol.Armour.ArmourStarExploreUpReq;
import com.hawk.game.protocol.Armour.ArmourStarUpReq;
import com.hawk.game.protocol.Armour.ArmourSuitChangeName;
import com.hawk.game.protocol.Armour.ArmourSuitChangeReq;
import com.hawk.game.protocol.Armour.ArmourTakeOffReq;
import com.hawk.game.protocol.Armour.ArmourTechAwardReq;
import com.hawk.game.protocol.Armour.ArmourTechLevelUpReq;
import com.hawk.game.protocol.Armour.ArmourTechShowReq;
import com.hawk.game.protocol.Armour.ArmourUnLockReq;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.SpeedUpTimeWeightType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.QueueService;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;
import com.hawk.game.service.pushgift.PushGiftManager;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.RandomUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.Action;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 铠甲
 * @author golden
 *
 */
public class PlayerArmourModule extends PlayerModule {

	static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 构造
	 */
	public PlayerArmourModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		WorldPointService.getInstance().removeShowEquipTech(player.getId());
		if(player.isCsPlayer()){
			WorldPointService.getInstance().refreshStarExploreShow(player.getId());
		}
		CommanderEntity entity = player.getData().getCommanderEntity();
		ArmourStarExplores starExplores = entity.getStarExplores();
		starExplores.loadIsActive(player.getId());
		if(starExplores.getIsActive() == 0){
			List<ArmourEntity> armours = player.getData().getArmourEntityList();
			for (ArmourEntity armour : armours) {
				if(armour.getQuantum() >= ArmourConstCfg.getInstance().getQuantumRedLevel()){
					starExplores.active(player.getId());
					triggerPushGift();
					break;
				}
			}
		}

		player.getPush().syncAllArmourInfo();
		player.getPush().syncArmourSuitInfo();
		player.getPush().syncEquipResearchInfo();
		player.getPush().syncArmourStarExploreInfo();
		
		checkEquipResearchUnlock();
		
		int showEquipTech = WorldPointService.getInstance().getShowEquipTech(player.getId());
		int currentTech = GameUtil.isEquipResearchShowUnlock(player);
		if (showEquipTech > 0 && showEquipTech != currentTech) {
			WorldPointService.getInstance().updateShowEquipTech(player.getId(), currentTech);
		}
		
		player.getPush().syncEquipStarShow();
		logArmourStarExploreInfo();
		return true;
	}

	private void logArmourStarExploreInfo(){
		try {
			CommanderEntity entity = player.getData().getCommanderEntity();
			ArmourStarExplores starExpores = entity.getStarExplores();
			starExpores.logInfo(player);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void checkEquipResearchUnlock() {
		
		// 检测装备科技解锁
		int unlockEquipResearch = player.getEntity().getUnlockEquipResearch();
		if (unlockEquipResearch <= 0) {
			// 没有套装不检测了
			int armourSuit = player.getEntity().getArmourSuit();
			List<ArmourEntity> armours = player.getData().getSuitArmours(armourSuit);
			if (armours.size() < 7) {
				return;
			}
			boolean unlockResearch = true;
			// 一套装备上7件全是紫色以上才解锁
			for (ArmourEntity armour : armours) {
				if (armour.getQuality() < ConstProperty.getInstance().getEquipResearchUnlockQuality()) {
					unlockResearch = false;
					break;
				}
			}
			if (unlockResearch) {
				player.getEntity().setUnlockEquipResearch(1);
				player.getPush().syncEquipResearchInfo();
				
				HawkApp.getInstance().postMsg(player.getXid(), EquipResearchUnlockMsg.valueOf());
			}
		}
		
		if (GameUtil.checkPhaseOneArmourTechMaxLevel(player.getData())) {
			List<EquipResearchEntity> equipResearchEntityList = player.getData().getEquipResearchEntityList();
			for (EquipResearchEntity researchEntity : equipResearchEntityList) {
				EquipResearchCfg researchCfg = HawkConfigManager.getInstance().getConfigByKey(EquipResearchCfg.class, researchEntity.getResearchId());
				if (researchCfg != null && researchCfg.getPhaseTwo() > 0) {
					updateWorldPoint(researchEntity.getResearchId(), researchEntity.getResearchLevel());
				}
			}
		}
	}
	
	/**
	 * 穿戴铠甲
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_PUT_ON_REQ_VALUE)
	private void putOn(HawkProtocol protocol) {

		ArmourPutOnReq req = protocol.parseProtocol(ArmourPutOnReq.getDefaultInstance());
		int suit = req.getSuit().getNumber();
		String id = req.getArmourId();

		ArmourEntity armour = player.getData().getArmourEntity(id);
		if (armour == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_NOT_FOUND_VALUE);
			return;
		}

		ArmourCfg armourCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, armour.getArmourId());
		if (armourCfg == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_CONFIG_NOT_FOUND_VALUE);
			return;
		}

		if (player.getCityLevel() < ArmourConstCfg.getInstance().getCityLevelUnlock()) {
			sendError(protocol.getType(), Status.ArmourError.AROUR_CITY_LEVEL_LIMIT_VALUE);
			return;
		}
		
		// 未解锁
		if (suit > player.getEntity().getArmourSuitCount()) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_SUIT_HAS_NOT_UNLOKED_VALUE);
			return;
		}

		// 脱下之前的铠甲
		String beforeArmour = player.getArmourSuit(suit, armourCfg.getPos());
		if (!HawkOSOperator.isEmptyString(beforeArmour)) {
			takeOff(protocol, suit, beforeArmour);
		}

		// 穿戴铠甲
		player.wearArmour(suit, armourCfg.getPos(), armour.getId());
		armour.addSuit(suit);

		player.getPush().syncArmourInfo(armour);
		player.getPush().syncArmourSuitInfo();
		player.responseSuccess(protocol.getType());

		// 刷新作用号
		player.getEffect().resetEffectArmour(player);

		player.refreshPowerElectric(PowerChangeReason.ARMOUR_CHANGE);
		
		logArmour(armour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_4);
		
		logger.info("armourChange, putOn, playerId:{}, armourId:{}, suit:{}", player.getId(), id, req.getSuit().getNumber());
		
		checkEquipResearchUnlock();
		
		// 更新泰能装备外显
		WorldPointService.getInstance().updateEquipStarShow(player);
	}

	/**
	 * 卸下铠甲
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_TAKE_OFF_REQ_VALUE)
	private void takeOff(HawkProtocol protocol) {
		ArmourTakeOffReq req = protocol.parseProtocol(ArmourTakeOffReq.getDefaultInstance());
		int suit = req.getSuit().getNumber();
		String id = req.getArmourId();

		takeOff(protocol, suit, id);

		player.getPush().syncArmourSuitInfo();
		player.responseSuccess(protocol.getType());
		
		player.refreshPowerElectric(PowerChangeReason.ARMOUR_CHANGE);
		
		// 更新泰能装备外显
		WorldPointService.getInstance().updateEquipStarShow(player);
	}

	public void takeOff(HawkProtocol protocol, int suit, String id) {
		ArmourEntity armour = player.getData().getArmourEntity(id);
		if (armour == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_NOT_FOUND_VALUE);
			return;
		}

		ArmourCfg armourCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, armour.getArmourId());
		if (armourCfg == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_CONFIG_NOT_FOUND_VALUE);
			return;
		}

		// 脱下铠甲
		player.takeOffArmour(suit, armourCfg.getPos());
		armour.removeSuit(suit);

		// 容错处理
		List<ArmourEntity> suitArmours = player.getSuitArmours(suit);
		for (ArmourEntity suitArmour : suitArmours) {
			ArmourCfg thisArmourCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, suitArmour.getArmourId());
			if (thisArmourCfg.getPos() != armourCfg.getPos()) {
				continue;
			}
			suitArmour.removeSuit(suit);
		}
		
		player.getPush().syncArmourInfo(armour);

		// 刷新作用号
		player.getEffect().resetEffectArmour(player);

		logArmour(armour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_5);
		
		logger.info("armourChange, takeOff, playerId:{}, armourId:{}, suit:{}", player.getId(), id, suit);
	}

	/**
	 * 强化
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_INTENSIFY_REQ_VALUE)
	private void intensify(HawkProtocol protocol) {
		ArmourIntensifyReq req = protocol.parseProtocol(ArmourIntensifyReq.getDefaultInstance());
		String id = req.getArmourId();

		ArmourEntity armour = player.getData().getArmourEntity(id);
		if (armour == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_NOT_FOUND_VALUE);
			return;
		}

		if (armour.isSuper()) {
			return;
		}
		
		ArmourCfg armourCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, armour.getArmourId());
		if (armourCfg == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_CONFIG_NOT_FOUND_VALUE);
			return;
		}

		int level = armour.getLevel();

		ArmourLevelCfg armourLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourLevelCfg.class, level);
		if (armourLevelCfg == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_LEVEL_CFG_NOT_FOUND_VALUE);
			return;
		}

		ArmourLevelCfg armourNextLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourLevelCfg.class, level + 1);
		if (armourNextLevelCfg == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_LEVEL_CFG_NOT_FOUND_VALUE);
			return;
		}

		ArmourBreakthroughCfg armourQualityCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourBreakthroughCfg.class, armour.getQuality());
		if (armourQualityCfg == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_QUALITY_CFG_NOT_FOUND_VALUE);
			return;
		}

		// 提升装备等级上限，仅对ss装备生效
		int levelLimit = armourQualityCfg.getLevelLimit();
		if(armour.getQuantum() >= ArmourConstCfg.getInstance().getQuantumRedLevel()){
			levelLimit += ArmourConstCfg.getInstance().getQuantumLevelLimitAdd();
		}
		if (armourQualityCfg.getQuality() == 4) {
			int effVal = player.getEffect().getEffVal(EffType.ARMOUR_1601);
			levelLimit += effVal;
		}
		
		if (level >= levelLimit) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_LEVEL_LIMIT_VALUE);
			return;
		}

		// 消耗
		ConsumeItems consum = ConsumeItems.valueOf();
		consum.addConsumeInfo(armourLevelCfg.getConsumItem());
		if (!consum.checkConsume(player, protocol.getType())) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_TOOL_NOT_ENOUGTH_VALUE);
			return;
		}
		consum.consumeAndPush(player, Action.ARMOUR_INTENSIFY);

		// 强化
		armour.setLevel(level + 1);
		//强化事件
		ActivityManager.getInstance().postEvent(new ArmourStrengthenEvent(player.getId(), armourCfg.getArmourId(), armour.getLevel()));
		armourLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourLevelCfg.class, level + 1);
		if (armourLevelCfg != null && !armourLevelCfg.getConsumItem().isEmpty()) {
			HawkApp.getInstance().postMsg(player, EquipEnhanceMsg.valueOf(1, (int) armourLevelCfg.getConsumItem().get(0).getCount()));
			HawkApp.getInstance().postMsg(player, EquipLevelUpMsg.valueOf(armour.getLevel()));
		}
		
		player.getPush().syncArmourInfo(armour);
		player.responseSuccess(protocol.getType());

		// 刷新作用号
		player.getEffect().resetEffectArmour(player);

		logger.info("armourChange, intensify, playerId:{}, armourId:{}, afterLevel:{}", player.getId(), id, armour.getLevel());
		
		LogUtil.logArmourIntensify(player, armour.getId(), armour.getArmourId(), armour.getLevel());
		
		player.refreshPowerElectric(PowerChangeReason.ARMOUR_CHANGE);
		
		// 装备属性改变日志
		logArmour(armour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_2);
		
		// 推送礼包
		PushGiftEntity pushGiftEntity = player.getData().getPushGiftEntity();
		int afterTimes = pushGiftEntity.addArmourIntensifyTimes(level);
		HawkApp.getInstance().postMsg(player, ArmourIntensifyTimesMsg.valueOf(afterTimes, armour.getLevel()));
	}

	/**
	 * 突破
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_BREAKTHROUGH_REQ_VALUE)
	private void breakthrough(HawkProtocol protocol) {
		ArmourBreakthroughReq req = protocol.parseProtocol(ArmourBreakthroughReq.getDefaultInstance());
		String id = req.getArmourId();

		ArmourEntity armour = player.getData().getArmourEntity(id);
		if (armour == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_NOT_FOUND_VALUE);
			return;
		}

		if (armour.isSuper()) {
			return;
		}
		
		int quality = armour.getQuality();

		ArmourBreakthroughCfg armourQualityCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourBreakthroughCfg.class, quality);
		if (armourQualityCfg == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_QUALITY_CFG_NOT_FOUND_VALUE);
			return;
		}

		ArmourBreakthroughCfg armourNextQualityCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourBreakthroughCfg.class, quality + 1);
		if (armourNextQualityCfg == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_QUALITY_CFG_NOT_FOUND_VALUE);
			return;
		}

		// 消耗
		ConsumeItems consum = ConsumeItems.valueOf();
		consum.addConsumeInfo(armourQualityCfg.getConsumItem());
		if (!consum.checkConsume(player, protocol.getType())) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_TOOL_NOT_ENOUGTH_VALUE);
			return;
		}
		consum.consumeAndPush(player, Action.ARMOUR_BREAKTHROUGH);

		// 强化
		armour.setQuality(quality + 1);

		List<ArmourAdditionalCfg> attrCfgs = AssembleDataManager.getInstance().getArmourAdditionCfgs(1, armour.getQuality());

		int addExtraCount = 1;
		if (armour.getQuality() == 3) {
			addExtraCount += player.getEffect().getEffVal(EffType.ARMOUR_1600);;
		}
		
		for (int i = 0; i < addExtraCount; i++) {
			int randTimes = 0;
			while(true) {
				randTimes++;
				
				// 随机属性
				ArmourAdditionalCfg randAttrCfg = RandomUtil.random(attrCfgs);
				ArmourAttrTemplate randAttr = RandomUtil.random(randAttrCfg.getAttrList());
				
				boolean hasAttrBefore = false;
				for(ArmourEffObject eff : armour.getExtraAttrEff()) {
					if (eff.getEffectType() == randAttr.getEffect()) {
						hasAttrBefore = true;
					}
				}
				
				if (!hasAttrBefore || randTimes >= 10) {
					armour.addExtraAttrEff(new ArmourEffObject(randAttrCfg.getId(), randAttr.getEffect(), HawkRand.randInt(randAttr.getRandMin(), randAttr.getRandMax())));
					break;
				}
			}
		}
		
		player.getPush().syncArmourInfo(armour);
		player.responseSuccess(protocol.getType());

		// 刷新作用号
		player.getEffect().resetEffectArmour(player);

		logger.info("armourChange, breakthrough, playerId:{}, armourId:{}, afterQuality:{}", player.getId(), id, armour.getQuality());
		
		LogUtil.logArmourBreakthrough(player, armour.getId(), armour.getArmourId(), armour.getQuality());
		
		player.refreshPowerElectric(PowerChangeReason.ARMOUR_CHANGE);
		
		//突破事件
		EquipQualityAchiveEvent event = new EquipQualityAchiveEvent(player.getId());
		event.addEquip(armour.getQuality(),1);
		ActivityManager.getInstance().postEvent(event);
		
		// 装备属性改变日志
		logArmour(armour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_1);
		
		checkEquipResearchUnlock();
	}

	/**
	 *               
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_INHERIT_REQ_VALUE)
	private void inherit(HawkProtocol protocol) {
		ArmourInheritReq req = protocol.parseProtocol(ArmourInheritReq.getDefaultInstance());
		String id = req.getArmourId();
		String beId = req.getBeArmourId();
		ArmourAttr attr = req.getArmourAttr();
		ArmourAttr beAttr = req.getBeArmourAttr();

		// 传承的铠甲
		ArmourEntity armour = player.getData().getArmourEntity(id);
		if (armour == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_NOT_FOUND_VALUE);
			return;
		}

		// 被传承的铠甲
		ArmourEntity beArmour = player.getData().getArmourEntity(beId);
		if (beArmour == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_NOT_FOUND_VALUE);
			return;
		}

		if (armour.isSuper() || beArmour.isSuper()) {
			return;
		}
		
		// 被传承的配置
		ArmourAdditionalCfg beAttrCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourAdditionalCfg.class, beAttr.getAttrId());
		if (beAttrCfg == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_ATTR_CFG_NOT_FOUND_VALUE);
			return;
		}

		// 判断传承属性是否存在
		if (req.hasArmourAttr()) {
			boolean armourHasAttr = false;
			for (ArmourEffObject attrEff : armour.getExtraAttrEff()) {
				if (attrEff.getAttrId() == attr.getAttrId() && attrEff.getEffectType() == attr.getAttrType() && attrEff.getEffectValue() == attr.getAttrValue()) {
					armourHasAttr = true;
					break;
				}
			}
			
			if (!armourHasAttr) {
				sendError(protocol.getType(), Status.ArmourError.AROUR_INHERIT_ATTR_NOT_EXIT_VALUE);
				return;
			}
			
			if (attr.getAttrType() == beAttr.getAttrType() && attr.getAttrValue() > beAttr.getAttrValue()) {
				sendError(protocol.getType(), Status.ArmourError.AROUR_INHERIT_MIN_TO_MAX_VALUE);
				return;
			}
			
		} else {
			int extrAttrCount = armour.getExtraAttrEff().size();
			int maxExtrAttrCount = ArmourConstCfg.getInstance().getExtrAttrCount(armour.getQuality());
			int effAddCount = player.getEffect().getEffVal(EffType.ARMOUR_1600);
			if (extrAttrCount >= maxExtrAttrCount + effAddCount) {
				return;
			}
			
		}

		// 只能传承到同类型属性上
		for (ArmourEffObject thisAttr : armour.getExtraAttrEff()) {
			if (beAttr.getAttrType() == thisAttr.getEffectType() && attr.getAttrId() != thisAttr.getAttrId()) {
				return;
			}
		}
		
		// 判断被传承属性是否存在
		boolean baArmourHasAttr = false;
		for (ArmourEffObject attrEff : beArmour.getExtraAttrEff()) {
			if (attrEff.getAttrId() == beAttr.getAttrId() && attrEff.getEffectType() == beAttr.getAttrType() && attrEff.getEffectValue() == beAttr.getAttrValue()) {
				baArmourHasAttr = true;
				break;
			}
		}

		if (!baArmourHasAttr) {
			sendError(protocol.getType(), Status.ArmourError.AROUR_INHERIT_BE_ATTR_NOT_EXIT_VALUE);
			return;
		}

		// 传承属性品质大于铠甲品质
		if (beAttrCfg.getQuality() > armour.getQuality()) {
			sendError(protocol.getType(), Status.ArmourError.AROUR_INHERIT_ATTR_QUAL_UPER_VALUE);
			return;
		}
		
		// 消耗
		ConsumeItems consum = ConsumeItems.valueOf();
		consum.addConsumeInfo(beAttrCfg.getInheritConsumeItem());
		if (!consum.checkConsume(player, protocol.getType())) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_TOOL_NOT_ENOUGTH_VALUE);
			return;
		}
		consum.consumeAndPush(player, Action.ARMOUR_INHERIT);

		// 移除传承的属性
		if (req.hasArmourAttr()) {
			for (ArmourEffObject attrEff : armour.getExtraAttrEff()) {
				if (attrEff.getAttrId() == attr.getAttrId() && attrEff.getEffectType() == attr.getAttrType() && attrEff.getEffectValue() == attr.getAttrValue()) {
					armour.getExtraAttrEff().remove(attrEff);
					break;
				}
			}
		}

		// 移除被传承的属性
		for (ArmourEffObject attrEff : beArmour.getExtraAttrEff()) {
			if (attrEff.getAttrId() == beAttr.getAttrId() && attrEff.getEffectType() == beAttr.getAttrType() && attrEff.getEffectValue() == beAttr.getAttrValue()) {
				beArmour.getExtraAttrEff().remove(attrEff);
				break;
			}
		}
		beArmour.notifyUpdate();

		armour.addExtraAttrEff(new ArmourEffObject(beAttr.getAttrId(), beAttr.getAttrType(), beAttr.getAttrValue()));

		player.getPush().syncArmourInfo(armour);
		player.getPush().syncArmourInfo(beArmour);
		player.responseSuccess(protocol.getType());

		// 刷新作用号
		player.getEffect().resetEffectArmour(player);

		logger.info("armourChange, inherit, playerId:{}, armourId:{}, beArmourId:{}, attr:{}, beAttr:{}", player.getId(), id, beId, attr, beAttr.toString());
		
		LogUtil.logArmourInherit(player, id, beId);
		
		player.refreshPowerElectric(PowerChangeReason.ARMOUR_CHANGE);
		
		// 装备属性改变日志
		logArmour(armour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_3);
		logArmour(beArmour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_3);
	}

	/**
	 * 分解
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_RESOLVE_REQ_VALUE)
	private void resolve(HawkProtocol protocol) {
		ArmourResolveReq req = protocol.parseProtocol(ArmourResolveReq.getDefaultInstance());
		Set<String> ids = new HashSet<>();
		for (String id : req.getArmourIdList()) {
			ids.add(id);
		}
		resolveArmour(ids);
		player.responseSuccess(protocol.getType());
	}

	/**
	 * 分解
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_RESOLVE_BY_QUALITY_REQ_VALUE)
	private void resolveByQuality(HawkProtocol protocol) {
		ArmourResolveByQualityReq req = protocol.parseProtocol(ArmourResolveByQualityReq.getDefaultInstance());
		List<Integer> qualityList = req.getQualityList();
		if(qualityList.size() > 5) {
			return;
		}
		
		List<ArmourEntity> armourEntityList = player.getData().getArmourEntityList();
		
		Set<String> ids = new HashSet<>();
		for (int quality : qualityList) {
			if(quality == 5){
				for (ArmourEntity armour : armourEntityList) {
					if (armour.getQuality() != 4) {
						continue;
					}
					if(armour.getQuantum() < ArmourConstCfg.getInstance().getQuantumRedLevel()){
						continue;
					}
					ids.add(armour.getId());
				}
			}else {
				for (ArmourEntity armour : armourEntityList) {
					if (armour.getQuality() != quality) {
						continue;
					}
					if(armour.getQuantum() >= ArmourConstCfg.getInstance().getQuantumRedLevel()){
						continue;
					}
					ids.add(armour.getId());
				}
			}

		}
		resolveArmour(ids);
		
		player.responseSuccess(protocol.getType());
	}
	
	public void resolveArmour(Set<String> ids) {
		long startTime = HawkTime.getMillisecond();
		
		// 分解列表
		List<ArmourEntity> resolveList = new ArrayList<>();

		// 分解奖励
		AwardItems resolveAward = AwardItems.valueOf();

		for (String id : ids) {
			ArmourEntity armour = player.getData().getArmourEntity(id);
			if (armour == null) {
				continue;
			}
			if (armour.isSuper()) {
				continue;
			}
			ArmourLevelCfg armourLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourLevelCfg.class, armour.getLevel());
			if (armourLevelCfg == null) {
				continue;
			}
			ArmourBreakthroughCfg armourQualityCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourBreakthroughCfg.class, armour.getQuality());
			if (armourQualityCfg == null) {
				continue;
			}
			ArmourStarConsumeCfg armourStarCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourStarConsumeCfg.class, armour.getStar());
			ArmourQuantumConsumeCfg armourQuantumCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourQuantumConsumeCfg.class, armour.getQuantum());
			
			if (armour.isLock()) {
				continue;
			}
			if (!armour.getSuitSet().isEmpty() && !armour.getSuitSet().contains(0)) {
				continue;
			}
			
			resolveList.add(armour);
			resolveAward.addAwards(armourLevelCfg.getResolveAwards());
			resolveAward.addAwards(armourQualityCfg.getResolveAwards());
			
			if (!armour.getConsumeItems().isEmpty()) {
				int startAttrResolveRate = ArmourConstCfg.getInstance().getStartAttrResolveRate();
				resolveAward.addItemInfos(armour.getConsumeItems().getItemInfosClon(startAttrResolveRate));
			}
			
			if (armourStarCfg != null) {
				resolveAward.addItemInfos(armourStarCfg.getReolveItems().getItemInfosClon());
			}
			if (armourQuantumCfg != null) {
				resolveAward.addItemInfos(armourQuantumCfg.getReolveItems().getItemInfosClon());
			}
		}

		// 删除铠甲
		for (ArmourEntity armour : resolveList) {
			logArmour(armour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_6);
			player.getData().removeArmourEntity(armour);
			armour.delete(true);
		}
		Map<Integer, Long> itemsAwardMap = resolveAward.getAwardItemsCount();
		// 发分解奖励
		resolveAward.rewardTakeAffectAndPush(player, Action.ARMOUR_RESOLVE, true, RewardOrginType.ARMOUR_RESOLVE);
		
		// 刷新作用号
		player.getEffect().resetEffectArmour(player);

		logger.info("armourChange, resolve, playerId:{}, armourId:{}, costTime:{}", player.getId(), Arrays.toString(ids.toArray()), HawkTime.getMillisecond() - startTime);
		
		LogUtil.logArmourResolve(player, ids.size());
		//成长激励活动需要记录
		ActivityManager.getInstance().getDataGeter().growUpBoostEquipDecomposeItemRecord(player.getId(), itemsAwardMap);
	}

	/**
	 * 锁定
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_LOCK_REQ_VALUE)
	private void lock(HawkProtocol protocol) {
		ArmourLockReq req = protocol.parseProtocol(ArmourLockReq.getDefaultInstance());

		// 锁定列表
		List<ArmourEntity> lockList = new ArrayList<>();
		for (String id : req.getArmourIdList()) {
			ArmourEntity armour = player.getData().getArmourEntity(id);
			if (armour == null) {
				continue;
			}
			armour.setLock(true);
			lockList.add(armour);
		}

		player.getPush().syncArmourInfo(lockList);
		player.responseSuccess(protocol.getType());

		logger.info("armourChange, lock, playerId:{}, armourId:{}", player.getId(), Arrays.toString(req.getArmourIdList().toArray()));
	}

	/**
	 * 套装改名
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_SUIT_CHANGE_NAME_VALUE)
	private void changeSuitName(HawkProtocol protocol) {
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_EQUIP_MARSHALLING);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return;
		}
		
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return;
		}
		
		ArmourSuitChangeName req = protocol.parseProtocol(ArmourSuitChangeName.getDefaultInstance());
		JSONObject json = new JSONObject();
		json.put("msg_type", 0);
		json.put("post_id", req.getSuitType().getNumber());
		json.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		json.put("param_id", String.valueOf(req.getSuitType().getNumber()));
		GameTssService.getInstance().wordUicChatFilter(player, req.getName(), 
				MsgCategory.ARMOUR_GROUP_NAME.getNumber(), GameMsgCategory.AMOUR_SUIT_CHANGE_NAME, 
				String.valueOf(req.getSuitType().getNumber()), json, protocol.getType());
	}

	/**
	 * 套装解锁
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_SUIT_UNLOCK_VALUE)
	private void suitUnlock(HawkProtocol protocol) {
		// 当前解锁到第几套了
		int current = player.getEntity().getArmourSuitCount();

		// 已经解锁所有套装
		int suitMaxCount = ArmourConstCfg.getInstance().getSuitMaxCount();
		if (current >= suitMaxCount) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_SUIT_HAS_UNLOKED_VALUE);
			return;
		}

		// 没有找到消耗相关配置
		List<ItemInfo> cost = ArmourConstCfg.getInstance().getSuitUnlockCostMap(current + 1);
		if (cost == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_CONFIG_NOT_FOUND_VALUE);
			return;
		}

		// 消耗
		ConsumeItems consum = ConsumeItems.valueOf();
		consum.addConsumeInfo(cost);
		if (!consum.checkConsume(player, protocol.getType())) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_TOOL_NOT_ENOUGTH_VALUE);
			return;
		}
		consum.consumeAndPush(player, Action.ARMOUR_SUIT_UNLOCK);

		player.getEntity().setArmourSuitCount(current + 1);

		player.getPush().syncPlayerInfo();
		player.responseSuccess(protocol.getType());

		logger.info("armourChange, suitUnlock, playerId:{}, suit:{}", player.getId(), player.getEntity().getArmourSuitCount());
		
		LogUtil.logArmourUnlockSuit(player, current + 1);
	}

	/**
	 * 套装切换
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_SUIT_CHANGE_REQ_VALUE)
	private void changeSuit(HawkProtocol protocol) {
		ArmourSuitChangeReq req = protocol.parseProtocol(ArmourSuitChangeReq.getDefaultInstance());
		int suit = req.getSuit().getNumber();
		if (suit > player.getEntity().getArmourSuitCount() || suit <= 0) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_SUIT_HAS_NOT_UNLOKED_VALUE);
			return;
		}

		player.getEntity().setArmourSuit(suit);
		player.getPush().syncPlayerInfo();
		player.responseSuccess(protocol.getType());

		// 刷新作用号
		player.getEffect().resetEffectArmour(player);

		player.refreshPowerElectric(PowerChangeReason.ARMOUR_CHANGE);
		
		logger.info("armourChange, changeSuit, playerId:{}, suit:{}", player.getId(), player.getEntity().getArmourSuit());
		
		// 更新泰能装备外显
		WorldPointService.getInstance().updateEquipStarShow(player);
	}

	/**
	 * 解锁
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_UNLOCK_REQ_VALUE)
	private void unlock(HawkProtocol protocol) {
		ArmourUnLockReq req = protocol.parseProtocol(ArmourUnLockReq.getDefaultInstance());

		// 锁定列表
		List<ArmourEntity> lockList = new ArrayList<>();
		for (String id : req.getArmourIdList()) {
			ArmourEntity armour = player.getData().getArmourEntity(id);
			if (armour == null) {
				continue;
			}
			armour.setLock(false);
			lockList.add(armour);
		}

		player.getPush().syncArmourInfo(lockList);
		player.responseSuccess(protocol.getType());

		logger.info("armourChange, unlock, playerId:{}, armourId:{}", player.getId(), Arrays.toString(req.getArmourIdList().toArray()));
	}
	
	/**
	 * 一键锁定最大等级
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_ONEKEY_LOCK_VALUE)
	private void lockMax(HawkProtocol protocol) {
		// 先找到最高的属性
		Map<Integer, Integer> lockAttr = new HashMap<>();
		List<ArmourEntity> armours = player.getData().getArmourEntityList();
		for (ArmourEntity armour : armours) {
			ArmourCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, armour.getArmourId());
			if (cfg == null) {
				continue;
			}
			int pos = cfg.getPos();
			
			for (ArmourEffObject eff : armour.getExtraAttrEff()) {
				int effType = eff.getType().getNumber();
				int effectValue = eff.getEffectValue();
				int xid = GameUtil.combineXAndY(pos, effType);
				
				if (lockAttr.get(xid) == null) {
					lockAttr.put(xid, effectValue);
				} else {
					int oldInfo = lockAttr.get(xid);
					if (oldInfo <= effectValue) {
						lockAttr.put(xid, effectValue);
					}
				}
			}
		}
		
		// 找到锁定的装备
		Set<String> lockSet = new HashSet<>();
		for (ArmourEntity armour : armours) {
			ArmourCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, armour.getArmourId());
			if (cfg == null) {
				continue;
			}
			int pos = cfg.getPos();
			
			for (ArmourEffObject eff : armour.getExtraAttrEff()) {
				int effType = eff.getType().getNumber();
				int effectValue = eff.getEffectValue();
				int xid = GameUtil.combineXAndY(pos, effType);
				
				if (lockAttr.get(xid) != null && lockAttr.get(xid) <= effectValue) {
					lockSet.add(armour.getId());
				}
			}
		}
		
		for (String id : lockSet) {
			ArmourEntity armour = player.getData().getArmourEntity(id);
			if (armour == null) {
				continue;
			}
			armour.setLock(true);
			lockSet.add(armour.getId());
		}

		player.responseSuccess(protocol.getType());
		logger.info("armourChange, onceLock, playerId:{}, armourId:{}", player.getId(), Arrays.toString(lockSet.toArray()));
	}
	
	/**
	 * 一键解锁
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_ONEKEY_UNLOCK_VALUE)
	private void unlockOnceKey(HawkProtocol protocol) {
		// 解锁列表
		List<ArmourEntity> lockList = new ArrayList<>();
				
		List<ArmourEntity> armours = player.getData().getArmourEntityList();
		for (ArmourEntity armour : armours) {
			if (armour.isLock()) {
				armour.setLock(false);
				lockList.add(armour);
			}
		}

		player.responseSuccess(protocol.getType());
		logger.info("armourChange, unlockOnceKey, playerId:{}, armourId:{}", player.getId());		
	}
	
	/**
	 * SS装备属性改变日志
	 * @param armour
	 * @param reason
	 */
	public void logArmour(ArmourEntity armour, int reason) {
		// 记录SS级装备
		if (armour.getQuality() >= 4) {
			logArmourChange(armour, reason);
		}
		// 记录S级并且等级大于1级的装备
		if (armour.getQuality() == 3 && armour.getLevel() > 1) {
			logArmourChange(armour, reason);
		}
	}
	
	private void logArmourChange(ArmourEntity armour, int reason) {
		try {
			LogUtil.logArmourChange(player,
					armour.getArmourId(),
					armour.getLevel(),
					armour.getQuality(),
					armour.getStar(),
					armour.getQuantum(),
					GameUtil.getArmourPower(armour),
					SerializeHelper.collectionToString(armour.getSuitSet(), SerializeHelper.BETWEEN_ITEMS),
					SerializeHelper.collectionToString(armour.getExtraAttrEff(), SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT),
					SerializeHelper.collectionToString(armour.getSkillEff(), SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT),
					SerializeHelper.collectionToString(armour.getStarEff(), SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT),
					reason, armour.getId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 研究升级
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_TECH_LEVEL_UP_VALUE)
	private void researchLevelUp(HawkProtocol protocol) {
		ArmourTechLevelUpReq req = protocol.parseProtocol(ArmourTechLevelUpReq.getDefaultInstance());

		//  装备科技未解锁
		if (player.getEntity().getUnlockEquipResearch() <= 0) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_RESEARCH_NOT_UNLOCK_VALUE);
			return;
		}

		// 当前有正在研究的装备科技
		Map<String, QueueEntity> queue = player.getData().getQueueEntitiesByType(QueueType.EQUIP_RESEARCH_QUEUE_VALUE);
		if (queue.size() > 0) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_RESEARCH_QUEUE_VALUE);
			return;
		}
		
		// 客户端传上来的装备科技线路错误
		EquipResearchEntity entity = player.getData().getEquipResearchEntity(req.getArmourTechId());
		if (entity == null) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}

		// 下一等级的配置不存在
		int nextLevel = entity.getResearchLevel() + 1;
		EquipResearchLevelCfg cfg = AssembleDataManager.getInstance().getEquipResearchLevelCfg(req.getArmourTechId(), nextLevel);
		if (cfg == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_RESEARCH_MAX_LEVEL_VALUE);
			return;
		}

		// 前置路线等级未完成
		EquipResearchCfg researchCfg = HawkConfigManager.getInstance().getConfigByKey(EquipResearchCfg.class, req.getArmourTechId());
		if (researchCfg == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_RESEARCH_CONFIG_ERROR_VALUE);
			return;
		}
		
		for (Entry<Integer, Integer> entry : researchCfg.getUnlockEquipMap().entrySet()) {
			int researchId = entry.getKey(), unlockLevel = entry.getValue();
			EquipResearchEntity tarEntity = player.getData().getEquipResearchEntity(researchId);
			if (tarEntity == null || tarEntity.getResearchLevel() < unlockLevel) {
				sendError(protocol.getType(), Status.ArmourError.ARMOUR_RESEARCH_PRE_LEVEL_ERROR_VALUE);
				return;
			}
		}
		double time = cfg.getTime() * (1 - player.getEffect().getEffVal(EffType.EFF_1479) * GsConst.EFF_PER);
		// 道具或者金币不足
		List<ItemInfo> costItems = researchConsume(cfg, req.getImmediate(),time);
		if (costItems == null) {
			sendError(protocol.getType(), Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		
		logger.info("armourResearchLevelUp req, playerId:{}, researchId:{}, researchLevel:{}, immediate:{}",
				player.getId(), cfg.getResearchId(), cfg.getLevel(), req.getImmediate());
		
		// PhaseTwo > 0 表示是装备二期研究, 不用花时间
		if (researchCfg.getPhaseTwo() > 0) {
			researchLevelUp(cfg.getResearchId(), cfg.getLevel());
			if (GameUtil.checkPhaseOneArmourTechMaxLevel(player.getData())) {
				updateWorldPoint(cfg.getResearchId(), cfg.getLevel());
			}
		} else if (req.getImmediate()) {
			researchLevelUp(cfg.getResearchId(), cfg.getLevel());
		} else {
			QueueService.getInstance().addReusableQueue(player, 
					QueueType.EQUIP_RESEARCH_QUEUE_VALUE, 
					QueueStatus.QUEUE_STATUS_COMMON_VALUE,
					cfg.getResearchId() + "_" +cfg.getLevel(), 
					0, 
					time * 1000,
					costItems, 
					GsConst.QueueReusage.EQUIP_RESEARCH);
		}
		
		// 推送成功
		player.responseSuccess(protocol.getType());
	}
	
	/**
	 * 更新世界点装备科技等级信息
	 * 
	 * @param techId
	 * @param level
	 */
	private void updateWorldPoint(int techId, int level) {
		WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
		if (worldPoint != null) {
			worldPoint.updateEquipTechLevel(techId, level);
		}
	}
	
	/**
	 * 装备科技队列已完成
	 */
	@MessageHandler
	private void onEquipResearchQueueFinishMsg(EquipResearchQueueFinishMsg msg) {
		logger.info("onEquipResearchQueueFinishMsg, playerId:{}, researchId:{}, researchLevel:{}", player.getId(), msg.getResearchId(), msg.getResearchLevel());
		researchLevelUp(msg.getResearchId(), msg.getResearchLevel());
	}
	
	/**
	 * 装备科技升级
	 */
	public void researchLevelUp(int researchId, int researchLevel) {
		
		EquipResearchEntity entity = player.getData().getEquipResearchEntity(researchId);
		if (entity == null) {
			return;
		}
		int unlockShowBefore = GameUtil.isEquipResearchShowUnlock(player);
		entity.setResearchLevel(researchLevel);
		int unlockShowAfter = GameUtil.isEquipResearchShowUnlock(player);
		if (unlockShowBefore != unlockShowAfter) {
			WorldPointService.getInstance().updateShowEquipTech(player.getId(), unlockShowAfter);
		}
		
		// 刷新战力
		player.refreshPowerElectric(PowerChangeReason.EQUIP_RESRERCH_LEVEL_UP);
		
		// 同步装备科技信息
		player.getPush().syncEquipResearchInfo();
		
		// 刷新作用号
		player.getEffect().resetEffectEquipResearch(player);
		
		// 刷新作用号
		player.getEffect().resetEffectArmour(player);
				
		logger.info("armourResearchLevelUp success, playerId:{}, researchId:{}, researchLevel:{}", player.getId(), researchId, researchLevel);
		//活动事件
		ActivityManager.getInstance().postEvent(new ArmourTechLevelUpEvent(player.getId()));
		
		LogUtil.logEquipResearchLevelUp(player, researchId, researchLevel);
		
		HawkApp.getInstance().postMsg(player, EquipResearchLevelUpMsg.valueOf(researchId, researchLevel));
	}
	
	/**
	 * 装备科技升级消耗
	 */
	private List<ItemInfo> researchConsume(EquipResearchLevelCfg cfg, boolean immidiate,double time) {
		ConsumeItems consume = ConsumeItems.valueOf();
		if (immidiate) {
			consume.addConsumeInfo(ItemInfo.valueListOf(cfg.getCost()));
			consume.addConsumeInfo(PlayerAttr.GOLD, GameUtil.caculateTimeGold((long) time, SpeedUpTimeWeightType.EQUIP_RESEARCH));
		} else {
			consume.addConsumeInfo(ItemInfo.valueListOf(cfg.getCost()));
		}
		
		if (!consume.checkConsume(player, HP.code.ARMOUR_TECH_LEVEL_UP_VALUE)) {
			return null;
		}
		AwardItems awardItems = consume.consumeAndPush(player, Action.EQUIP_RESEARCH_LEVEL_UP);
		
		return awardItems.getAwardItems();
	}

	
	/**
	 * 领取装备科技奖励
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_TECH_AWARD_VALUE)
	private void researchReward(HawkProtocol protocol) {
		
		if (player.getEntity().getUnlockEquipResearch() <= 0) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_RESEARCH_NOT_UNLOCK);
			return;
		}
		
		ArmourTechAwardReq req = protocol.parseProtocol(ArmourTechAwardReq.getDefaultInstance());
		
		// 客户端传上来的装备科技线路错误
		EquipResearchEntity entity = player.getData().getEquipResearchEntity(req.getArmourTechId());
		if (entity == null) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		
		// 没有该奖励的配置
		EquipResearchRewardCfg rewardCfg = AssembleDataManager.getInstance().getEquipResearchRewardCfg(req.getArmourTechId(), req.getLevel());
		if (rewardCfg == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_RESEARCH_CONFIG_ERROR_VALUE);
			return;
		}
		
		// 等级不够
		if (entity.getResearchLevel() < req.getLevel()) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_RESRERCH_REWARD_LEVEL_NOT_ENOUGTH_VALUE);
			return;
		}
		
		// 已经领取过
		if (entity.getReceiveBoxSet().contains(req.getLevel())) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_RESRERCH_REWARD_ALREADY_RECEIVE_VALUE);
			return;
		}
		
		entity.addReceiveBox(req.getLevel());
		
		// 刷新作用号
		player.getEffect().resetEffectEquipResearch(player);
		
		// 发奖
		List<EffectObject> effTouchList = rewardCfg.getEffTouchList();
		for (EffectObject eff : effTouchList) {
			
			if (eff.getType().equals(EffType.ARMOUR_1600)) {
				extrAttrIncre();
			}
		}
		
		player.responseSuccess(protocol.getType());
		
		player.getPush().syncEquipResearchInfo();
		
		// 刷新作用号
		player.getEffect().resetEffectEquipResearch(player);
		player.getEffect().resetEffectArmour(player);
		player.refreshPowerElectric(PowerChangeReason.ARMOUR_CHANGE);
		
		logger.info("armourResearchReward, playerId:{}, researchId:{}, researchLevel:{}", player.getId(), req.getArmourTechId(), req.getLevel());
	}
	
	/**
	 * 装备科技外显设置
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_TECH_SHOW_SET_VALUE)
	private void researchShowSet(HawkProtocol protocol) {
		ArmourTechShowReq req = protocol.parseProtocol(ArmourTechShowReq.getDefaultInstance());
		
		//  装备科技未解锁
		if (player.getEntity().getUnlockEquipResearch() <= 0) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_RESEARCH_NOT_UNLOCK_VALUE);
			return;
		}
		
		WorldPointService.getInstance().updateShowEquipTech(player.getId(), req.getShow() ? GameUtil.isEquipResearchShowUnlock(player) : 0);
		player.getPush().syncEquipResearchInfo();
		player.responseSuccess(protocol.getType());
	}
	
	/**
	 * 额外属性条目增加
	 */
	public void extrAttrIncre() {
		
		List<ArmourEntity> armours = player.getData().getArmourEntityList();
		
		for (ArmourEntity armour : armours) {
			int quality = armour.getQuality();
			
			// 只增加A品质以上的装备条目数
			if (quality < 3) {
				continue;
			}
			
			ArmourBreakthroughCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ArmourBreakthroughCfg.class, quality);
			if (cfg == null) {
				continue;
			}
			
			// 基础条目数量
			int extraCount = cfg.getExtraAttrCount();
			// 作用号添加条目数量
			int effAddCount = player.getEffect().getEffVal(EffType.ARMOUR_1600);
			// 最大条目数量
			int maxExtraCount = extraCount + player.getEffect().getEffVal(EffType.ARMOUR_1600);
			
			// 超出上限条目数，就不处理了
			if (armour.getExtraAttrEff().size() >= maxExtraCount) {
				continue;
			}
			
			// 添加额外属性条目
			for (int i = 0; i < effAddCount; i++) {
				int additionType = armour.isSuper() ? 2 : 1;
				List<ArmourAdditionalCfg> attrCfgs = AssembleDataManager.getInstance().getArmourAdditionCfgs(additionType, armour.getQuality());

				int randTimes = 0;
				while(true) {
					randTimes++;
					
					// 随机属性
					ArmourAdditionalCfg randAttrCfg = RandomUtil.random(attrCfgs);
					ArmourAttrTemplate randAttr = RandomUtil.random(randAttrCfg.getAttrList());
					
					boolean hasAttrBefore = false;
					for(ArmourEffObject eff : armour.getExtraAttrEff()) {
						if (eff.getEffectType() == randAttr.getEffect()) {
							hasAttrBefore = true;
						}
					}
					
					if (!hasAttrBefore || randTimes >= 10) {
						armour.addExtraAttrEff(new ArmourEffObject(randAttrCfg.getId(), randAttr.getEffect(), HawkRand.randInt(randAttr.getRandMin(), randAttr.getRandMax())));
						break;
					}
				}
				player.getPush().syncArmourInfo(armour);
			}
			
		}
	}
	
	/**
	 * 装备工匠(活动)传承
	 */
	@ProtocolHandler(code = HP.code.EQUIP_CRAFTSMAN_INHERIT_REQ_VALUE)
	private void carftsmanInherit(HawkProtocol protocol) {
		
		EquipCraftsmanInheritReq req = protocol.parseProtocol(EquipCraftsmanInheritReq.getDefaultInstance());
		
		// 活动数据
		Optional<EquipCarftsmanActivity> activityOp = ActivityManager.getInstance()
				.getGameActivityByType(ActivityType.EQUIP_CRAFTSMAN_VALUE);
		if (!activityOp.isPresent()) {
			return;
		}
		EquipCarftsmanActivity activity = activityOp.get();

		// 判断活动是否开启
		if (!activity.isOpening(player.getId())) {
			return;
		}

		// 玩家活动数据实体
		Optional<EquipCarftsmanEntity> opEntity = activity.getPlayerDataEntity(player.getId());
		if (!opEntity.isPresent()) {
			return;
		}
		EquipCarftsmanEntity entity = opEntity.get();

		// 传承的铠甲
		ArmourEntity armour = player.getData().getArmourEntity(req.getArmourId());
		if (armour == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_NOT_FOUND_VALUE);
			return;
		}

		// 被传承的属性
		EquipCarftsmanItem beAttr = entity.getAttrBoxMap().get(req.getBeInheritId());
		if (beAttr == null) {
			sendError(protocol.getType(), Status.ArmourError.AROUR_INHERIT_BE_ATTR_NOT_EXIT_VALUE);
			return;
		}

		if (armour.isSuper()) {
			return;
		}

		// 被传承的配置
		EquipCarftsmanGachaCfg baActivityCfg = HawkConfigManager.getInstance().getConfigByKey(EquipCarftsmanGachaCfg.class, beAttr.getGachaId());
		ArmourAdditionalCfg beAttrCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourAdditionalCfg.class,baActivityCfg.getAdditionId());
		if (beAttrCfg == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_ATTR_CFG_NOT_FOUND_VALUE);
			return;
		}

		// 判断传承属性是否存在
		if (req.getAttrId() > 0) {
			boolean armourHasAttr = false;
			for (ArmourEffObject attrEff : armour.getExtraAttrEff()) {
				if (attrEff.getAttrId() == req.getAttrId() && attrEff.getEffectType() == req.getAttrType()
						&& attrEff.getEffectValue() == req.getAttrValue()) {
					armourHasAttr = true;
					break;
				}
			}

			if (!armourHasAttr) {
				sendError(protocol.getType(), Status.ArmourError.AROUR_INHERIT_ATTR_NOT_EXIT_VALUE);
				return;
			}

			if (req.getAttrType() == req.getAttrType() && req.getAttrValue() > req.getAttrValue()) {
				sendError(protocol.getType(), Status.ArmourError.AROUR_INHERIT_MIN_TO_MAX_VALUE);
				return;
			}

		} else {
			int extrAttrCount = armour.getExtraAttrEff().size();
			int maxExtrAttrCount = ArmourConstCfg.getInstance().getExtrAttrCount(armour.getQuality());
			int effAddCount = player.getEffect().getEffVal(EffType.ARMOUR_1600);
			if (extrAttrCount >= maxExtrAttrCount + effAddCount) {
				return;
			}

		}

		// 只能传承到同类型属性上
		for (ArmourEffObject thisAttr : armour.getExtraAttrEff()) {
			if (beAttrCfg.getAttrList().get(0).getEffect() == thisAttr.getEffectType()
					&& req.getAttrId() != thisAttr.getAttrId()) {
				return;
			}
		}

		// 传承属性品质大于铠甲品质
		if (beAttrCfg.getQuality() > armour.getQuality()) {
			sendError(protocol.getType(), Status.ArmourError.AROUR_INHERIT_ATTR_QUAL_UPER_VALUE);
			return;
		}

		// 消耗
		ConsumeItems consum = ConsumeItems.valueOf();
		consum.addConsumeInfo(ItemInfo.valueListOf(baActivityCfg.getInheritPrice()));
		if (!consum.checkConsume(player, protocol.getType())) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_TOOL_NOT_ENOUGTH_VALUE);
			return;
		}
		consum.consumeAndPush(player, Action.ARMOUR_INHERIT);

		// 移除传承的属性
		if (req.getAttrId() > 0) {
			for (ArmourEffObject attrEff : armour.getExtraAttrEff()) {
				if (attrEff.getAttrId() == req.getAttrId() && attrEff.getEffectType() == req.getAttrType()
						&& attrEff.getEffectValue() == req.getAttrValue()) {
					armour.getExtraAttrEff().remove(attrEff);
					break;
				}
			}
		}

		// 移除被传承的属性
		entity.removeAttr(req.getBeInheritId());
		activity.syncPageInfo(player.getId());
		
		armour.addExtraAttrEff(new ArmourEffObject(beAttrCfg.getId(), beAttrCfg.getAttrList().get(0).getEffect(), baActivityCfg.getAttributeValue()));

		player.getPush().syncArmourInfo(armour);
		player.responseSuccess(protocol.getType());

		// 刷新作用号
		player.getEffect().resetEffectArmour(player);
		
		logArmour(armour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_7);
		
		LogUtil.logEquipCarftsmanAttr(player, baActivityCfg.getId(), baActivityCfg.getAdditionId(), baActivityCfg.getAttributeType(), baActivityCfg.getAttributeValue(), 3, armour.getArmourId());
	}

	/**
	 * 装备升星(装备泰能灌注)
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_STAR_UP_REQ_VALUE)
	private void onStarUp(HawkProtocol protocol) {
		ArmourStarUpReq req = protocol.parseProtocol(ArmourStarUpReq.getDefaultInstance());

		// 装备不存在
		ArmourEntity armour = player.getData().getArmourEntity(req.getArmourId());
		if (armour == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_NOT_FOUND_VALUE);
			return;
		}
		
		// 此品质不能升星
		int quality = armour.getQuality();
		if (!ArmourConstCfg.getInstance().canQualityStar(quality)) {
			return;
		}
		
		// 此等级不能升星
		int level = armour.getLevel();
		if (!ArmourConstCfg.getInstance().canLevelStar(level)) {
			return;
		}
		
		// 升星后星级
		int afterStar = armour.getStar() + 1;
		
		// 超过此品质可升级的最大星级
		ArmourBreakthroughCfg qualityCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourBreakthroughCfg.class, quality);
		if(armour.getQuantum() >= ArmourConstCfg.getInstance().getQuantumRedLevel()){
			if (afterStar > qualityCfg.getStarLimit() + ArmourConstCfg.getInstance().getStarLimitAdd()) {
				return;
			}
		}else {
			if (afterStar > qualityCfg.getStarLimit()) {
				return;
			}
		}
		
		// 下一星级配置不存在
		ArmourStarConsumeCfg afterStarCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourStarConsumeCfg.class, afterStar);
		if (afterStarCfg == null) {
			return;
		}
		
		// 升星消耗
		List<ItemInfo> resItems = afterStarCfg.getConsume();
		final int TAIJING = 800002; // 泰晶
		GameUtil.reduceByEffect(resItems, TAIJING, player.getEffect().getEffValArr(EffType.EFF_367816));
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(resItems);
		if (!consume.checkConsume(player)) {
			return;
		}
		consume.consumeAndPush(player, Action.ARMOUR_STAR_UP);
		
		// 升星
		armour.addStar();

		// 推单条装备信息
		player.getPush().syncArmourInfo(armour);
		// 通用成功返回
		player.responseSuccess(protocol.getType());
		// 刷新作用号
		player.getEffect().resetEffectArmour(player);
		// 刷新战力
		player.refreshPowerElectric(PowerChangeReason.ARMOUR_CHANGE);
		
		logArmour(armour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_8);
		
		logger.info("armourChange, starUp, playerId:{}, armourId:{}, afterStar:{}", player.getId(), armour.getId(), afterStar);
		
		// 更新泰能装备外显
		WorldPointService.getInstance().updateEquipStarShow(player);
		
		// 推送礼包
		PushGiftEntity pushGiftEntity = player.getData().getPushGiftEntity();
		int afterTimes = pushGiftEntity.addArmourStarUpTimesMap(armour.getStar());
		HawkApp.getInstance().postMsg(player, ArmourStarUpTimesMsg.valueOf(afterTimes, afterStar));
	}
	
	/**
	 * 装备星级属性激活
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_STAR_ATTR_OPEN_REQ_VALUE)
	private void onArmourStarAttrOpen(HawkProtocol protocol) {
		ArmourStarAttrOpenReq req = protocol.parseProtocol(ArmourStarAttrOpenReq.getDefaultInstance());
		
		// 装备不存在
		ArmourEntity armour = player.getData().getArmourEntity(req.getArmourId());
		if (armour == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_NOT_FOUND_VALUE);
			return;
		}
		
		// 装备星级
		int star = armour.getStar();
		ArmourStarConsumeCfg starCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourStarConsumeCfg.class, star);
		if (starCfg == null) {
			return;
		}
		
		// 超出最大解锁条目数量
		int beforeChargeCount = armour.getStarEff().size();
		if (beforeChargeCount >= starCfg.getUnlockCharging()) {
			return;
		}
		
		// 已经有了的充能条目
		Set<Integer> alreadyChargeType = new HashSet<>();
		for (ArmourEffObject starEff : armour.getStarEff()) {
			ArmourChargeLabCfg chargeLabCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourChargeLabCfg.class, starEff.getAttrId());
			if (chargeLabCfg != null) {
				alreadyChargeType.add(chargeLabCfg.getChargingLabel());
			}
			// 替换属性也不能重复
			if (starEff.getReplaceAttrId() != 0) {
				chargeLabCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourChargeLabCfg.class, starEff.getReplaceAttrId());
				if (chargeLabCfg != null) {
					alreadyChargeType.add(chargeLabCfg.getChargingLabel());
				}
			}
		}
		
		// 取出所有可以参与随机的条目
		List<ArmourChargeLabCfg> randCfgs = new ArrayList<>();
		ConfigIterator<ArmourChargeLabCfg> chargeLabCfgIter = HawkConfigManager.getInstance().getConfigIterator(ArmourChargeLabCfg.class);
		while (chargeLabCfgIter.hasNext()) {
			ArmourChargeLabCfg chargeLabCfg = chargeLabCfgIter.next();
			if (alreadyChargeType.contains(chargeLabCfg.getChargingLabel())) {
				continue;
			}
			randCfgs.add(chargeLabCfg);
		}
		
		// 随机属性
		ArmourChargeLabCfg randCfg = RandomUtil.random(randCfgs);
		EffectObject attributeValue = randCfg.getAttributeValue();
		ArmourEffObject armourEff = new ArmourEffObject(randCfg.getId(), attributeValue.getEffectType(), attributeValue.getEffectValue());
		armourEff.setRate(randCfg.getDefaultProgress());
		armour.addStarEff(armourEff);
		
		// 推单条装备信息
		player.getPush().syncArmourInfo(armour);
		// 通用成功返回
		player.responseSuccess(protocol.getType());
		// 刷新作用号
		player.getEffect().resetEffectArmour(player);
		// 刷新战力
		player.refreshPowerElectric(PowerChangeReason.ARMOUR_CHANGE);

		ArmourStarAttrOpenResp.Builder builder = ArmourStarAttrOpenResp.newBuilder();
		builder.setArmourId(armour.getId());
		builder.setAttrId(randCfg.getId());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ARMOUR_STAR_ATTR_OPEN_RESP, builder));
		
		logArmour(armour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_9);
		
		logger.info("armourChange, starAttrOpen, playerId:{}, armourId:{}, armourEff:{}", player.getId(), armour.getId(), armourEff.toString());
		
		// 更新泰能装备外显
		WorldPointService.getInstance().updateEquipStarShow(player);
	}
	
	/**
	 * 装备星级属性充能
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_STAR_ATTR_UP_REQ_VALUE)
	private void onArmourStarAttrUp(HawkProtocol protocol) {
		ArmourStarAttrUpReq req = protocol.parseProtocol(ArmourStarAttrUpReq.getDefaultInstance());
		ArmourEntity armour = player.getData().getArmourEntity(req.getArmourId());
		if (armour == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_NOT_FOUND_VALUE);
			return;
		}
		ArmourEffObject starAttr = getStarAttr(armour, req.getAttrId());
		
		List<Integer> addRateList = new ArrayList<>();
		
		if (starAttr.getRate() < ArmourConstCfg.getInstance().getChargeCommonLimit()) {
			// 普通充能
			starAttrUpCommon(armour, req.getAttrId(), addRateList,1);
		} else if(starAttr.getRate() < ArmourConstCfg.getInstance().getChargeSpecialLimit()){
			// 高级充能
			starAttrUpSpecial(armour, req.getAttrId(), addRateList,1);
			if(addRateList.size() > 0){
				starAttr.setBreakthrough(1);
			}
		}else {
			// 红装充能
			starAttrUpRed(armour, req.getAttrId(), addRateList,1);
			if(addRateList.size() > 0){
				starAttr.setBreakthrough(2);
			}
		}
		
		// 推单条装备信息
		player.getPush().syncArmourInfo(armour);
		// 通用成功返回
		player.responseSuccess(protocol.getType());
		// 刷新作用号
		player.getEffect().resetEffectArmour(player);
		// 刷新战力
		player.refreshPowerElectric(PowerChangeReason.ARMOUR_CHANGE);
		
		ArmourStarAttrUpResp.Builder builder = ArmourStarAttrUpResp.newBuilder();
		builder.setArmourId(req.getArmourId());
		builder.setAttrId(req.getAttrId());
		for (Integer rate : addRateList) {
			builder.addAddRate(rate);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ARMOUR_STAR_ATTR_UP_RESP, builder));
		
		logArmour(armour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_10);
	}
	
	/**
	 * 装备星级属性充能十次
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_STAR_ATTR_UP_TEN_REQ_VALUE)
	private void onArmourStarAttrUpTen(HawkProtocol protocol) {
		ArmourStarAttrUpReq req = protocol.parseProtocol(ArmourStarAttrUpReq.getDefaultInstance());
		ArmourEntity armour = player.getData().getArmourEntity(req.getArmourId());
		if (armour == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_NOT_FOUND_VALUE);
			return;
		}
		ArmourEffObject starAttr = getStarAttr(armour, req.getAttrId());
		
		List<Integer> addRateList = new ArrayList<>();
		
		if (starAttr.getRate() < ArmourConstCfg.getInstance().getChargeCommonLimit()) {
			// 普通充能
			starAttrUpCommon(armour, req.getAttrId(), addRateList,10);
		} else if(starAttr.getRate() < ArmourConstCfg.getInstance().getChargeSpecialLimit()){
			// 高级充能
			starAttrUpSpecial(armour, req.getAttrId(), addRateList,10);
			if(addRateList.size() > 0){
				starAttr.setBreakthrough(1);
			}
		}else {
			// 红装充能
			starAttrUpRed(armour, req.getAttrId(), addRateList,10);
			if(addRateList.size() > 0){
				starAttr.setBreakthrough(2);
			}
		}
		
		// 推单条装备信息
		player.getPush().syncArmourInfo(armour);
		// 通用成功返回
		player.responseSuccess(protocol.getType());
		// 刷新作用号
		player.getEffect().resetEffectArmour(player);
		// 刷新战力
		player.refreshPowerElectric(PowerChangeReason.ARMOUR_CHANGE);
		
		ArmourStarAttrUpResp.Builder builder = ArmourStarAttrUpResp.newBuilder();
		builder.setArmourId(req.getArmourId());
		builder.setAttrId(req.getAttrId());
		for (Integer rate : addRateList) {
			builder.addAddRate(rate);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ARMOUR_STAR_ATTR_UP_RESP, builder));
		
		logArmour(armour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_11);
	}
	
	/**
	 * 普通充能
	 * @param armour
	 * @param starAttrId
	 */
	private void starAttrUpCommon(ArmourEntity armour, int starAttrId, List<Integer> addRateList,int count) {
		ArmourEffObject starAttr = getStarAttr(armour, starAttrId);
		// 充能前进度
		int beforeRate = starAttr.getRate();
		int curRate = beforeRate;
		int upCount = 0;
		for(int i = 1;i<=count;i++){
			// 已经到最大进度了
			if (curRate >= ArmourConstCfg.getInstance().getChargeCommonLimit()) {
				break;
			}
			ConsumeItems consum = ConsumeItems.valueOf();
			consum.addConsumeInfo(ArmourConstCfg.getInstance().getChargeConsumeCommon(i));
			if (!consum.checkConsume(player)) {
				break;
			}
			// 随机增加的进度值
			Integer randRate = HawkRand.randomWeightObject(ArmourConstCfg.getInstance().getChargeCommonRateMap());
			// 充能后进度
			curRate = Math.min(ArmourConstCfg.getInstance().getChargeCommonLimit(), curRate + randRate.intValue());
			
			addRateList.add(randRate);
			upCount ++;
		}
		if(upCount > 0){
			ConsumeItems itemUse = ConsumeItems.valueOf();
			List<ItemInfo> ilist = ArmourConstCfg.getInstance().getChargeConsumeCommon(upCount);
			itemUse.addConsumeInfo(ilist);
			if (!itemUse.checkConsume(player)) {
				return;
			}
			// 消耗 小于满紫进度值用普通消耗,否则用高级消耗
			itemUse.consumeAndPush(player, Action.ARMOUR_STAR_ATTR_CHARGE);
			// 设置充能进度
			starAttr.setRate(curRate);
			// 已消耗道具记录
			armour.addConsume(ArmourConstCfg.getInstance().getChargeConsumeCommon(upCount));
			
			logger.info("armourChange, attrUpCommon, playerId:{}, armourId:{}, beforeRate:{}, afterRate:{}", player.getId(), armour.getId(), beforeRate, curRate);
		}
		
	}
	
	/**
	 * 高级充能
	 * @param armour
	 * @param starAttrId
	 */
	private void starAttrUpSpecial(ArmourEntity armour, int starAttrId, List<Integer> addRateList,int count) {
		ArmourEffObject starAttr = getStarAttr(armour, starAttrId);
		// 充能前进度
		int beforeRate = starAttr.getRate();
		int curRate = beforeRate;
		int upCount = 0;
		for(int i=1;i<= count;i++){
			// 紫色装备以下,不让高级充能
			if (armour.getQuality() < ArmourConstCfg.getInstance().getArmourStarAttrBreakQuality() ) {
				break;
			}
			// 已经到最大进度了
			if (curRate >= ArmourConstCfg.getInstance().getChargeSpecialLimit()) {
				break;
			}
			// 消耗 小于满紫进度值用普通消耗,否则用高级消耗
			ConsumeItems consum = ConsumeItems.valueOf();
			consum.addConsumeInfo(ArmourConstCfg.getInstance().getChargeConsumeSpecial(i));
			if (!consum.checkConsume(player)) {
				break;
			}
			// 随机增加的进度值
			Integer randRate = HawkRand.randomWeightObject(ArmourConstCfg.getInstance().getChargeSpecialRateMap());
			// 充能后进度
			curRate = Math.min(ArmourConstCfg.getInstance().getChargeSpecialLimit(), curRate + randRate.intValue());
			
			addRateList.add(randRate);
			upCount ++;
		}
		
		if(upCount > 0){
			// 消耗 小于满紫进度值用普通消耗,否则用高级消耗
			ConsumeItems itemUse = ConsumeItems.valueOf();
			itemUse.addConsumeInfo(ArmourConstCfg.getInstance().getChargeConsumeSpecial(upCount));
			if (!itemUse.checkConsume(player)) {
				return;
			}
			itemUse.consumeAndPush(player, Action.ARMOUR_STAR_ATTR_CHARGE);
			// 设置充能进度
			starAttr.setRate(curRate);
			// 已消耗道具记录
			armour.addConsume(ArmourConstCfg.getInstance().getChargeConsumeSpecial(upCount));
			logger.info("armourChange, attrUpSpecial, playerId:{}, armourId:{}, beforeRate:{}, afterRate:{}", player.getId(), armour.getId(), beforeRate, curRate);
		}
		
	}

	/**
	 * 红色充能
	 * @param armour
	 * @param starAttrId
	 */
	private void starAttrUpRed(ArmourEntity armour, int starAttrId, List<Integer> addRateList,int count) {
		ArmourEffObject starAttr = getStarAttr(armour, starAttrId);
		// 充能前进度
		int beforeRate = starAttr.getRate();
		int curRate = beforeRate;
		int upCount = 0;
		for(int i = 1;i<=count;i++){
			if(armour.getQuantum() < ArmourConstCfg.getInstance().getQuantumRedLevel()){
				break;
			}
			// 已经到最大进度了
			if (curRate >= ArmourConstCfg.getInstance().getChargeRedLimit()) {
				break;
			}
			// 消耗 小于满紫进度值用普通消耗,否则用高级消耗
			ConsumeItems consum = ConsumeItems.valueOf();
			consum.addConsumeInfo(ArmourConstCfg.getInstance().getChargeConsumeRed(i));
			if (!consum.checkConsume(player)) {
				break;
			}
			// 随机增加的进度值
			Integer randRate = HawkRand.randomWeightObject(ArmourConstCfg.getInstance().getChargeRedRateMap());
			// 充能后进度
			curRate = Math.min(ArmourConstCfg.getInstance().getChargeRedLimit(), curRate + randRate.intValue());
			addRateList.add(randRate);
			upCount ++;
		}
		if(upCount > 0){
			ConsumeItems itemUse = ConsumeItems.valueOf();
			itemUse.addConsumeInfo(ArmourConstCfg.getInstance().getChargeConsumeRed(upCount));
			if (!itemUse.checkConsume(player)) {
				return;
			}
			itemUse.consumeAndPush(player, Action.ARMOUR_STAR_ATTR_CHARGE);
			// 设置充能进度
			starAttr.setRate(curRate);
			// 已消耗道具记录
			armour.addConsume(ArmourConstCfg.getInstance().getChargeConsumeRed(upCount));
			logger.info("armourChange, attrUpRed, playerId:{}, armourId:{}, beforeRate:{}, afterRate:{}", player.getId(), armour.getId(), beforeRate, curRate);
		}
	}

	/**
	 * 获取星级属性
	 * @param armour
	 * @param attrId
	 * @return
	 */
	private ArmourEffObject getStarAttr(ArmourEntity armour, int attrId) {
		ArmourEffObject armourEff = null;
		for (ArmourEffObject starEff : armour.getStarEff()) {
			if (starEff.getAttrId() == attrId) {
				armourEff = starEff;
				break;
			}
		}
		return armourEff;
	}
	
	/**
	 * 装备星级属性随机
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_STAR_ATTR_RANDOM_REQ_VALUE)
	private void onArmourStarAttrRand(HawkProtocol protocol) {
		ArmourStarAttrRandomReq req = protocol.parseProtocol(ArmourStarAttrRandomReq.getDefaultInstance());
		ArmourEntity armour = player.getData().getArmourEntity(req.getArmourId());
		if (armour == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_NOT_FOUND_VALUE);
			return;
		}
		ArmourEffObject starAttr = getStarAttr(armour, req.getAttrId());
		if (starAttr == null) {
			return;
		}
		
		// 消耗判定
		DailyDataEntity dailyDataEntity = player.getData().getDailyDataEntity();
		int armourStarAttrTimes = dailyDataEntity.getArmourStarAttrTimes();
		if (armourStarAttrTimes >= ArmourConstCfg.getInstance().getFreeCharge()) {
			ConsumeItems consum = ConsumeItems.valueOf();
			consum.addConsumeInfo(ArmourConstCfg.getInstance().getChargeRefreshConsume(armourStarAttrTimes - ArmourConstCfg.getInstance().getFreeCharge()));
			if (!consum.checkConsume(player)) {
				return;
			}
			consum.consumeAndPush(player, Action.ARMOUR_STAR_ATTR_REFRESH);
		}
		dailyDataEntity.addArmourStarAttrTimes();
		
		Set<Integer> alreadyChargeType = new HashSet<>();
		for (ArmourEffObject starEff : armour.getStarEff()) {
			ArmourChargeLabCfg chargeLabCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourChargeLabCfg.class, starEff.getAttrId());
			if (chargeLabCfg != null) {
				alreadyChargeType.add(chargeLabCfg.getChargingLabel());
			}
			// 替换属性也不能重复
			if (starEff.getReplaceAttrId() != 0) {
				chargeLabCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourChargeLabCfg.class, starEff.getReplaceAttrId());
				if (chargeLabCfg != null) {
					alreadyChargeType.add(chargeLabCfg.getChargingLabel());
				}
			}
		}
		
		// 取出所有可以参与随机的条目
		List<ArmourChargeLabCfg> randCfgs = new ArrayList<>();
		ConfigIterator<ArmourChargeLabCfg> chargeLabCfgIter = HawkConfigManager.getInstance().getConfigIterator(ArmourChargeLabCfg.class);
		while (chargeLabCfgIter.hasNext()) {
			ArmourChargeLabCfg chargeLabCfg = chargeLabCfgIter.next();
			if (alreadyChargeType.contains(chargeLabCfg.getChargingLabel())) {
				continue;
			}
			randCfgs.add(chargeLabCfg);
		}
		
		// 随机属性
		ArmourChargeLabCfg randCfg = RandomUtil.random(randCfgs);
		starAttr.setReplaceAttrId(randCfg.getId());
		armour.notifyUpdate();
		
		// 推单条装备信息
		player.getPush().syncArmourInfo(armour);
		// 通用成功返回
		player.responseSuccess(protocol.getType());
		// 每日数据刷新
		player.getPush().synPlayerDailyData();
		
		logger.info("armourChange, starAttrRand, playerId:{}, armourId:{}, randTimes:{}, randAttrId:{}", player.getId(), armour.getId(), armourStarAttrTimes + 1, randCfg.getId());
		
		logArmour(armour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_12);
	}
	
	/**
	 * 装备星级属性替换
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_STAR_ATTR_REPLACE_REQ_VALUE)
	private void onArmourStarAttrReplace(HawkProtocol protocol) {
		ArmourStarAttrReplaceReq req = protocol.parseProtocol(ArmourStarAttrReplaceReq.getDefaultInstance());
		ArmourEntity armour = player.getData().getArmourEntity(req.getArmourId());
		if (armour == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_NOT_FOUND_VALUE);
			return;
		}
		ArmourEffObject starAttr = getStarAttr(armour, req.getAttrId());
		if (starAttr == null) {
			return;
		}
		ArmourChargeLabCfg replaceAttrCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourChargeLabCfg.class, starAttr.getReplaceAttrId());
		if (replaceAttrCfg == null) {
			return;
		}
		starAttr.setAttrId(replaceAttrCfg.getId());
		EffectObject attributeValue = replaceAttrCfg.getAttributeValue();
		starAttr.setEffectType(attributeValue.getEffectType());
		starAttr.setEffectValue(attributeValue.getEffectValue());
		starAttr.setReplaceAttrId(0);
		armour.notifyUpdate();
		
		// 推单条装备信息
		player.getPush().syncArmourInfo(armour);
		// 通用成功返回
		player.responseSuccess(protocol.getType());
		
		ArmourStarAttrReplaceResp.Builder builder = ArmourStarAttrReplaceResp.newBuilder();
		builder.setArmourId(req.getArmourId());
		builder.setAttrId(req.getAttrId());
		builder.setReplaceAttrId(replaceAttrCfg.getId());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ARMOUR_STAR_ATTR_REPLACE_RESP, builder));
		
		// 刷新作用号
		player.getEffect().resetEffectArmour(player);
		// 刷新战力
		player.refreshPowerElectric(PowerChangeReason.ARMOUR_CHANGE);
		
		logger.info("armourChange, starAttrReplace, playerId:{}, armourId:{}, repAttrId:{}", player.getId(), armour.getId(), replaceAttrCfg.getId());
		
		logArmour(armour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_13);
	}
	
	/**
	 * 装备星级词条突破
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_ATTR_BREAKTHROUGH_REQ_VALUE)
	private void onArmourStarAttrBreakthrough(HawkProtocol protocol) {
		ArmourAttrBreakthroughReq req = protocol.parseProtocol(ArmourAttrBreakthroughReq.getDefaultInstance());
		ArmourEntity armour = player.getData().getArmourEntity(req.getArmourId());
		if (armour == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_NOT_FOUND_VALUE);
			return;
		}
		ArmourEffObject starAttr = getStarAttr(armour, req.getAttrId());
		if (starAttr == null) {
			return;
		}
		if(starAttr.getBreakthrough() == 0){
			if (starAttr.getRate() < ArmourConstCfg.getInstance().getChargeCommonLimit()) {
				return;
			}
			// 紫色装备以下,不让突破
			if (armour.getQuality() < ArmourConstCfg.getInstance().getArmourStarAttrBreakQuality() ) {
				return;
			}
			starAttr.setBreakthrough(1);
			armour.notifyUpdate();
		}else {
			if (starAttr.getRate() < ArmourConstCfg.getInstance().getChargeSpecialLimit()) {
				return;
			}
			// 红色装备才能图片
			if(armour.getQuantum() < ArmourConstCfg.getInstance().getQuantumRedLevel()){
				return;
			}
			starAttr.setBreakthrough(2);
			armour.notifyUpdate();
		}
		
		// 推单条装备信息
		player.getPush().syncArmourInfo(armour);
		// 通用成功返回
		player.responseSuccess(protocol.getType());
		// 刷新作用号
		player.getEffect().resetEffectArmour(player);
		// 刷新战力
		player.refreshPowerElectric(PowerChangeReason.ARMOUR_CHANGE);
		
		logArmour(armour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_14);
	}
	
	/**
	 * 改变泰能装备外显
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.EUQUIP_STAR_SHOW_CHANGE_REQ_VALUE)
	private void onArmourStarShowReq(HawkProtocol protocol) {
		WorldPointService.getInstance().updateEquipStarShow(player, true);
		player.getPush().syncEquipStarShow();
	}

	/**
	 * 装备量子升级请求(槽位升级)
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.ARMOUR_QUANTUM_UP_REQ_VALUE)
	private void onQuantumUpReq(HawkProtocol protocol) {
		Armour.ArmourQuantumUpReq req = protocol.parseProtocol(Armour.ArmourQuantumUpReq.getDefaultInstance());
		// 装备不存在
		ArmourEntity armour = player.getData().getArmourEntity(req.getArmourId());
		if (armour == null) {
			sendError(protocol.getType(), Status.ArmourError.ARMOUR_NOT_FOUND_VALUE);
			return;
		}

		// 此品质不能升级量子槽位
		int quality = armour.getQuality();
		if (!ArmourConstCfg.getInstance().canQualityQuantum(quality)) {
			return;
		}

		// 此等级不能升级量子槽位
		int level = armour.getLevel();
		if (!ArmourConstCfg.getInstance().canLevelQuantum(level)) {
			return;
		}

		// 升级后的量子槽位等级
		int afterQuantum = armour.getQuantum() + 1;

		// 下一量子槽位等级配置不存在
		ArmourQuantumConsumeCfg afterQuantumCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourQuantumConsumeCfg.class, afterQuantum);
		if (afterQuantumCfg == null) {
			return;
		}

		// 升级量子槽位消耗
		final int LZKCS = 802004; //量子扩充石
		List<ItemInfo> resItems = afterQuantumCfg.getConsume();
		GameUtil.reduceByEffect(resItems, LZKCS, player.getEffect().getEffValArr(EffType.EFF_367817));
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(resItems);
		if (!consume.checkConsume(player)) {
			return;
		}
		consume.consumeAndPush(player, Action.ARMOUR_QUANTUM_UP);

		// 升级量子槽位
		armour.addQuantum();
		CommanderEntity entity = player.getData().getCommanderEntity();
		ArmourStarExplores starExplores = entity.getStarExplores();
		if(starExplores.getIsActive() != 1){
			if(armour.getQuantum() >= ArmourConstCfg.getInstance().getQuantumRedLevel()) {
				starExplores.active(player.getId());
				triggerPushGift();
				player.getPush().syncArmourStarExploreInfo();
			}
		}
		// 推单条装备信息
		player.getPush().syncArmourInfo(armour);
		// 通用成功返回
		player.responseSuccess(protocol.getType());
		// 刷新作用号
		player.getEffect().resetEffectArmour(player);
		// 刷新战力
		player.refreshPowerElectric(PowerChangeReason.ARMOUR_CHANGE);

		logArmour(armour, GsConst.ArmourChangeReason.ARMOUR_CHANGE_15);
		//触发推送礼包（量子扩充礼包）
		try {
			AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.PUSH_GIFT_3900.getType());
			condition.handle(player.getData(), Arrays.asList(armour.getQuantum()), player.isActiveOnline());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 触发推送礼包（星能探索功能解锁礼包）
	 */
	private void triggerPushGift() {
		try {
			AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.PUSH_GIFT_3800.getType());
			condition.handle(player.getData(), Collections.emptyList(), player.isActiveOnline());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 星能探索升级
	 */
	@ProtocolHandler(code = HP.code2.ARMOUR_STAR_EXPLORE_UP_REQ_VALUE)
	private void starExploreUp(HawkProtocol protocol) {
		ArmourStarExploreUpReq req = protocol.parseProtocol(ArmourStarExploreUpReq.getDefaultInstance());
		
		// 获取星能球star
		CommanderEntity entity = player.getData().getCommanderEntity();
		ArmourStarExplores starExplores = entity.getStarExplores();
		ArmourStarExploreObj star = starExplores.getStar(req.getStarId());
		
		int curLevel = star.getCurrentLevel();
		// 升级
		star.upLevel(player, req.getTimes());
		starExplores.checkCollect(player.getId());
		Map<String, Object> param = new HashMap<>();
		param.put("starId", star.getStarId());
		param.put("before",curLevel);//升级前
		param.put("after", star.getCurrentLevel()); //升级后
		LogUtil.logActivityCommon(player, LogConst.LogInfoType.star_explore_up, param);
		// db更新
		entity.notifyUpdate();
		// 通用成功返回
		player.responseSuccess(protocol.getType());
		player.getPush().syncArmourStarExploreInfo();
		starExplores.loadEffMap();
		player.getEffect().syncStarExplore(player, starExplores);
		// 刷新作用号
		player.getEffect().resetEffectArmour(player);
		// 刷新战力
		player.refreshPowerElectric(PowerChangeReason.STAR_EXPLORE);
		int upLevelTimes = star.getCurrentLevel() - curLevel;
		if (upLevelTimes > 0) {
			ActivityManager.getInstance().postEvent(new PlanetExploreLevelUpEvent(player.getId(), upLevelTimes));
		}
	}


	/**
	 * 星能探索跃迁
	 */
	@ProtocolHandler(code = HP.code2.ARMOUR_STAR_EXPLORE_JUMP_REQ_VALUE)
	private void starExploreJump(HawkProtocol protocol) {
		Armour.ArmourStarExploreJumpReq req = protocol.parseProtocol(Armour.ArmourStarExploreJumpReq.getDefaultInstance());
		// 获取星能球star
		CommanderEntity entity = player.getData().getCommanderEntity();
		ArmourStarExplores starExplores = entity.getStarExplores();
		ArmourStarExploreCollect collect = starExplores.getCollect(req.getId());
		String beforeFix = SerializeHelper.mapToString(collect.getFixAttrMap());
		String beforeRandom = SerializeHelper.mapToString(collect.getRandomAttrMap());
		// 跃迁
		collect.jump(player);
		// db更新
		entity.notifyUpdate();
		String afterFix = SerializeHelper.mapToString(collect.getFixAttrMap());
		String afterRandom = SerializeHelper.mapToString(collect.getRandomAttrMap());
		Map<String, Object> param = new HashMap<>();
		param.put("collectId", collect.getCollectId());
		param.put("beforeFix",beforeFix);//升级前
		param.put("afterFix", afterFix); //升级后
		param.put("beforeRandom",beforeRandom);//升级前
		param.put("afterRandom", afterRandom); //升级后
		LogUtil.logActivityCommon(player, LogConst.LogInfoType.star_explore_jump, param);
		// 通用成功返回
		player.responseSuccess(protocol.getType());
		player.getPush().syncArmourStarExploreInfo();
		starExplores.loadEffMap();
		player.getEffect().syncStarExplore(player, starExplores);
		// 刷新作用号
		player.getEffect().resetEffectArmour(player);
	}

	/**
	 * 星能探索外显
	 */
	@ProtocolHandler(code = HP.code2.ARMOUR_STAR_EXPLORE_SHOW_REQ_VALUE)
	private void starExploreShow(HawkProtocol protocol) {
		// 获取星能球star
		CommanderEntity entity = player.getData().getCommanderEntity();
		ArmourStarExplores starExplores = entity.getStarExplores();
		int show = WorldPointService.getInstance().getStarExploreShow(player.getId());
		if(show >=0 ){
			WorldPointService.getInstance().updateStarExploreShow(player.getId(), -1);
		}else {
			WorldPointService.getInstance().updateStarExploreShow(player.getId(), starExplores.getMaxCount());
		}
		player.getPush().syncArmourStarExploreInfo();
	}
}
