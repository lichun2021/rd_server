package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.tuple.HawkTuple5;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.EquipQualityAchiveEvent;
import com.hawk.activity.event.impl.EquipTechScoreEvent;
import com.hawk.activity.event.impl.HeroChangeEvent;
import com.hawk.activity.event.impl.HeroLevelUpEvent;
import com.hawk.activity.event.impl.HeroUnlockEvent;
import com.hawk.activity.event.impl.HeroUpStarEvent;
import com.hawk.activity.event.impl.RandomHeroEvent;
import com.hawk.activity.event.impl.UseHeroExpItemEvent;
import com.hawk.activity.type.impl.equipTech.cfg.EquipTechActivityKVConfig;
import com.hawk.game.GsApp;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.ArmourBreakthroughCfg;
import com.hawk.game.config.ArmourLevelCfg;
import com.hawk.game.config.ArmourPoolCfg;
import com.hawk.game.config.BuffCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.GachaCfg;
import com.hawk.game.config.HeroArchivesChapterCfg;
import com.hawk.game.config.HeroArchivesConstCfg;
import com.hawk.game.config.HeroArchivesContentCfg;
import com.hawk.game.config.HeroCfg;
import com.hawk.game.config.HeroColorQualityCfg;
import com.hawk.game.config.HeroLevelCfg;
import com.hawk.game.config.HeroOfficeCfg;
import com.hawk.game.config.HeroSkillCfg;
import com.hawk.game.config.HeroSkinCfg;
import com.hawk.game.config.HeroSoulLevelCfg;
import com.hawk.game.config.HeroSoulResetCfg;
import com.hawk.game.config.HeroSoulStageCfg;
import com.hawk.game.config.HeroStarLevelCfg;
import com.hawk.game.config.HeroTalentCfg;
import com.hawk.game.config.HeroTalentPoolCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.SuperSoldierSkillexpExchangeCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.HeroArchivesEntity;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.PlayerGachaEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.gacha.CheckAndConsumResult;
import com.hawk.game.gacha.GachaOprator;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.plantsoldier.science.PlantScience;
import com.hawk.game.msg.BuffHeroSkinEndMsg;
import com.hawk.game.msg.BuildingRemoveMsg;
import com.hawk.game.msg.HeroItemChangedMsg;
import com.hawk.game.msg.HeroLevelUpMsg;
import com.hawk.game.msg.HeroStarUpMsg;
import com.hawk.game.msg.OneKeyHeroSkillUpMsg;
import com.hawk.game.msg.OneKeyHeroUpMsg;
import com.hawk.game.msg.PlayerEffectChangeMsg;
import com.hawk.game.msg.UnlockHeroMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.hero.HeroSkin;
import com.hawk.game.player.hero.HeroTalent;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.hero.SkillSlot;
import com.hawk.game.player.hero.TalentSlot;
import com.hawk.game.player.hero.skill.HeroSkillFactory;
import com.hawk.game.player.hero.skill.IHeroSkill;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.GachaType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.ToolType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.HPHeroChangeSkinReq;
import com.hawk.game.protocol.Hero.HPHeroUseSkinItemReq;
import com.hawk.game.protocol.Hero.PBCastHeroSkillRequest;
import com.hawk.game.protocol.Hero.PBChoseRandTalentReq;
import com.hawk.game.protocol.Hero.PBExchangeItem;
import com.hawk.game.protocol.Hero.PBHeroArchiveExchange;
import com.hawk.game.protocol.Hero.PBHeroArchiveInfo;
import com.hawk.game.protocol.Hero.PBHeroArchivePush;
import com.hawk.game.protocol.Hero.PBHeroArchiveReq;
import com.hawk.game.protocol.Hero.PBHeroEffect;
import com.hawk.game.protocol.Hero.PBHeroItem;
import com.hawk.game.protocol.Hero.PBHeroOffice;
import com.hawk.game.protocol.Hero.PBHeroOfficeAppointRequest;
import com.hawk.game.protocol.Hero.PBHeroSkinStepUpReq;
import com.hawk.game.protocol.Hero.PBHeroSkinStepUpResp;
import com.hawk.game.protocol.Hero.PBHeroSkinUlockReq;
import com.hawk.game.protocol.Hero.PBHeroSoulLevelUpReq;
import com.hawk.game.protocol.Hero.PBHeroSoulRestReq;
import com.hawk.game.protocol.Hero.PBHeroSoulStageUpReq;
import com.hawk.game.protocol.Hero.PBHeroStarUpRequest;
import com.hawk.game.protocol.Hero.PBHeroState;
import com.hawk.game.protocol.Hero.PBHeroTalentRollPriceResp;
import com.hawk.game.protocol.Hero.PBInstallSkillRequest;
import com.hawk.game.protocol.Hero.PBMergeSkill;
import com.hawk.game.protocol.Hero.PBMergeSkillRequest;
import com.hawk.game.protocol.Hero.PBRandomTalentReq;
import com.hawk.game.protocol.Hero.PBRandomTalentResp;
import com.hawk.game.protocol.Hero.PBRemoveSkillRequest;
import com.hawk.game.protocol.Hero.PBResolveSkillRequest;
import com.hawk.game.protocol.Hero.PBStrengTalentReq;
import com.hawk.game.protocol.Hero.PBStrengTalentResp;
import com.hawk.game.protocol.Hero.PBUnlockHeroRequest;
import com.hawk.game.protocol.Hero.PBUnlockHeroTalentReq;
import com.hawk.game.protocol.Hero.PBUseHeroExpItemRequest;
import com.hawk.game.protocol.Hero.PBUseSkillExpItemRequest;
import com.hawk.game.protocol.Item.HPGachaReq;
import com.hawk.game.protocol.Item.HPGachaResp;
import com.hawk.game.protocol.Item.HPSyncGachaInfoResp;
import com.hawk.game.protocol.Player.PlayerFlagPosition;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.StrengthenGuide.HeroQualityColorType;
import com.hawk.game.protocol.SuperSoldier.HPSupersoldierExchangeItem;
import com.hawk.game.protocol.SuperSoldier.HPSupersoldierExchangeItemResp;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventGacha;
import com.hawk.game.service.mssion.event.EventHeroAppoint;
import com.hawk.game.service.mssion.event.EventHeroChange;
import com.hawk.game.service.mssion.event.EventHeroGarrison;
import com.hawk.game.service.mssion.event.EventHeroStarUp;
import com.hawk.game.service.mssion.event.EventHeroUpgrade;
import com.hawk.game.service.mssion.event.EventInstallSkill;
import com.hawk.game.strengthenguide.StrengthenGuideManager;
import com.hawk.game.strengthenguide.msg.SGHeroStarUpMsg;
import com.hawk.game.strengthenguide.msg.SGPlayerHeroUnlockMsg;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.QueueReusage;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.log.Action;
import com.hawk.log.LogConst.HeroSkillOperType;
import com.hawk.log.Source;

/**
 * @author lwt
 * @date 2017年7月25日
 */
public class PlayerHeroModule extends PlayerModule {
	private int lastTickDay = 0;
	private long lastCheckTime = 0;

	public PlayerHeroModule(Player player) {
		super(player);
	}

	@Override
	public boolean onTick() {
		long currentTime = HawkApp.getInstance().getCurrentTime();
		if (currentTime - lastCheckTime > 1_000) {
			lastCheckTime = currentTime;
			gachaRefresh();
			int staffVal = 0;
			for (PlayerHero hero : player.getAllHero()) {
				hero.tick();
				staffVal += hero.staffVal();
			}
			if(staffVal != player.getStaffOffic().getStaffVal()){
				player.getStaffOffic().refresh(player.getData());
			}
		}
		return super.onTick();
	}
	
	@ProtocolHandler(code = HP.code2.HERO_SOUL_RESET_C_VALUE)
	private void onSSSSoulReset(HawkProtocol protocol) {
		PBHeroSoulRestReq req = protocol.parseProtocol(PBHeroSoulRestReq.getDefaultInstance());
		final int heroId = req.getHeroId();
		PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
		if (ConstProperty.getInstance().getHeroSoulResetOpen() <= 0) {
			return;
		}
		if (hero == null || hero.getConfig().getSoulOpen() == 0) {
			return;
		}
		if (HawkTime.getMillisecond() < player.getData().getCommanderEntity().getSoulResetCd()) {
			return;
		}

		int cfgId = hero.getSoul().soulLevelMaxCfgId();
		HeroSoulLevelCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulLevelCfg.class, cfgId);

		int stage = hero.getSoul().getSoulStage().size();
		int level = curCfg.getStage() > stage ? curCfg.getLevel() : 0;

		HeroSoulResetCfg rcfg = HawkConfigManager.getInstance().getCombineConfig(HeroSoulResetCfg.class, stage, level);
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(rcfg.getConsumption()), false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.HERO_SOUL_RESET);

		hero.getSoul().reset();
		player.getData().getCommanderEntity().setSoulResetCd(ConstProperty.getInstance().getHeroSoulResetTimeLimit()*1000 + HawkTime.getMillisecond());

		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItemInfos(ItemInfo.valueListOf(rcfg.getReturnItem()));
		awardItem.rewardTakeAffectAndPush(player, Action.HERO_SOUL_RESET, RewardOrginType.HERO_SOUL_RESET);

		hero.notifyChange();
		player.responseSuccess(protocol.getType());
		LogUtil.logHeroAttrChange(player, Action.HERO_SOUL_RESET, 0, hero);
	}
	
	
	@ProtocolHandler(code = HP.code2.HERO_SOUL_LEVEL_UP_C_VALUE)
	private void onSSSSoulLevelUp(HawkProtocol protocol) {
		PBHeroSoulLevelUpReq req = protocol.parseProtocol(PBHeroSoulLevelUpReq.getDefaultInstance());
		final int heroId = req.getHeroId();
		PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
		if (hero == null || hero.getConfig().getSoulOpen() == 0) {
			return;
		}

		HeroSoulLevelCfg nextCfg = null;
		int cfgId = hero.getSoul().soulLevelMaxCfgId();
		if (cfgId == 0) {
			nextCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulLevelCfg.class, hero.getSoul().soulLevelCfgId(1, 1));
		} else {
			HeroSoulLevelCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulLevelCfg.class, cfgId);
			nextCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulLevelCfg.class, hero.getSoul().soulLevelCfgId(curCfg.getStage(), curCfg.getLevel() + 1));
			if (nextCfg == null && hero.getSoul().getSoulStage().contains(hero.getSoul().soulStageCfgId(curCfg.getStage()))) { // 要先升级
				nextCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulLevelCfg.class, hero.getSoul().soulLevelCfgId(curCfg.getStage() + 1, 1));
			}
		}

		if (nextCfg == null) {
			return;
		}
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(nextCfg.getConsumption()), false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.HERO_SOUL_LEVEL_UP);
		hero.getSoul().getSoulLevel().put(nextCfg.getStage(), nextCfg.getLevel());

		hero.notifyChange();
		player.responseSuccess(protocol.getType());
		LogUtil.logHeroAttrChange(player, Action.HERO_SOUL_LEVEL_UP, 0, hero);
	}
	
	
	@ProtocolHandler(code = HP.code2.HERO_SOUL_STAGE_UP_C_VALUE)
	private void onSSSSoulStageUp(HawkProtocol protocol) {
		PBHeroSoulStageUpReq req = protocol.parseProtocol(PBHeroSoulStageUpReq.getDefaultInstance());
		final int heroId = req.getHeroId();
		PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
		if (hero == null || hero.getConfig().getSoulOpen() == 0) {
			return;
		}

		HeroSoulLevelCfg curLevelCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulLevelCfg.class, hero.getSoul().soulLevelMaxCfgId());
		HeroSoulLevelCfg nextLevelCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulLevelCfg.class,
				hero.getSoul().soulLevelCfgId(curLevelCfg.getStage(), curLevelCfg.getLevel() + 1));
		if (nextLevelCfg != null) {// 没有升满
			return;
		}
		HeroSoulStageCfg nextCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSoulStageCfg.class, hero.getSoul().soulStageCfgId(curLevelCfg.getStage()));
		if (nextCfg == null || hero.getSoul().getSoulStage().contains(nextCfg.getId())) {
			return;
		}
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(nextCfg.getConsumption()), false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.HERO_SOUL_STAGE_UP);
		hero.getSoul().getSoulStage().add(nextCfg.getId());

		hero.notifyChange();
		player.responseSuccess(protocol.getType());
		LogUtil.logHeroAttrChange(player, Action.HERO_SOUL_STAGE_UP, 0, hero);
	}

	// 招募
	// 检查夸天
	private void gachaRefresh() {
		if (player.isInDungeonMap()) {
			return;
		}
		final int dayOfYear = HawkTime.getYearDay();
		if (dayOfYear != lastTickDay && player.isActiveOnline()) {
			boolean sync = false;
			for (PlayerGachaEntity entity : player.getData().getPlayerGachaEntities()) {
				if (entity.getDayOfYear() != dayOfYear) {
					entity.setDayOfYear(dayOfYear);
					entity.setDayCount(0);
					entity.setFreeTimesUsed(0);
					entity.setNextFree(0);
					sync = true;
				}
			}
			player.getPlayerMechaCore().updateGachaAddProductCrossDay();
			if (sync) {
				player.getPush().syncGachaInfo();
			}
			lastTickDay = dayOfYear;
		}
	}

	@Override
	protected boolean onPlayerLogin() {
		herosCheckAndFix();
		// 经查日志上次道具回档中有玩家英雄整卡被保留在背包之中. 为了持续可修正错误数据,在登录时检查是否有英雄整卡残留
		for (ItemEntity itemEntity : player.getData().getItemEntities()) {
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemEntity.getItemId());
			if (Objects.nonNull(itemCfg) && itemCfg.getItemType() == Const.ToolType.HERO_VALUE && itemEntity.getItemCount() > 0) {
				ItemInfo itemAdd = new ItemInfo(ItemType.TOOL_VALUE, itemEntity.getItemId(), itemEntity.getItemCount());
				HawkApp.getInstance().postMsg(player.getXid(), HeroItemChangedMsg.valueOf(itemAdd.clone(), itemCfg));
			}
		}
		player.getPush().pushHeroList();
		player.getPush().syncGachaInfo();

		// 已拥有的其他英雄检查一遍羁绊
		int allNum = 0;
		for (PlayerHero playerHero : player.getAllHero()) {
			if (playerHero.getHeroCollect().isActive()) {
				allNum += playerHero.getHeroCollect().getCollectNum();
			}
		}
		// 打点羁绊记录日志
		LogUtil.logHeroCollect(player, allNum);

		// 推送英雄档案信息
		syncHeroArchivesInfo();
		
		checkHeroArchiveOpenAward();
		
		if (HawkTime.getYyyyMMddIntVal() < 20230917) {
			try {
				HeroArchivesEntity entity = player.getData().getHeroArchivesEntity();
				for (PlayerHero playerHero : player.getAllHero()) {
					int afterLevel = entity.getArchiveLevel(playerHero.getCfgId());
					if (afterLevel > 0) {
						LogUtil.logHeroAttrChange(player, Action.HERO_ARCHIVE_UPLEVEL, 0, playerHero);
					}
				}
				PlantScience plantScience = player.getPlantScience();
				LogUtil.logPlantScienceResearchOperation(player, 0, 0, 1, plantScience.getTechPower());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		return super.onPlayerLogin();
	}

	/** 解锁皮肤 */
	@ProtocolHandler(code = HP.code.HERO_UNLOCK_SKIN_C_VALUE)
	private void onUnlockSkin(HawkProtocol protocol) {
		PBHeroSkinUlockReq req = protocol.parseProtocol(PBHeroSkinUlockReq.getDefaultInstance());
		final int skinId = req.getSkinId();
		final int heroId = req.getHeroId();
		PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
		if (hero == null) {
			return;
		}

		Optional<HeroSkin> skinOp = hero.getSkin(skinId);
		if (!skinOp.isPresent()) {
			return;
		}

		HeroSkin skin = skinOp.get();
		if (skin.isUnlock()) {
			return;
		}

		ItemInfo costItem = new ItemInfo(skin.getCfg().getUnlockItem());
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(costItem, false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.HERO_SKIN_UNLOCK);

		skin.setUnlock(true);
		hero.changeSkin(skinId);
		player.responseSuccess(protocol.getType());
		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.HERO_SKIN_UNLOCK,
				Params.valueOf("cost", consumeItems.getItemsInfo()),
				Params.valueOf("skinId", skinId),
				Params.valueOf("step", skin.getStep()),
				Params.valueOf("heroId", heroId));
		LogUtil.logHeroSkinChange(player, Action.HERO_SKIN_UNLOCK, consumeItems.getItemsInfo(), skin);

	}

	/** 皮肤强化 */
	@ProtocolHandler(code = HP.code.HERO_SKIN_UP_C_VALUE)
	private void onSkinStrengh(HawkProtocol protocol) {
		PBHeroSkinStepUpReq req = protocol.parseProtocol(PBHeroSkinStepUpReq.getDefaultInstance());
		final int skinId = req.getSkinId();
		final int heroId = req.getHeroId();
		final int itemCount = req.getItemCount();
		if (itemCount <= 0) {
			return;
		}
		PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
		if (hero == null) {
			return;
		}

		Optional<HeroSkin> skinOp = hero.getSkin(skinId);
		if (!skinOp.isPresent()) {
			return;
		}

		HeroSkin skin = skinOp.get();
		if (!skin.isUnlock() || skin.getNextStepCfg() == null) {
			return;
		}
		int step = skin.getStep();
		HeroSkinCfg cfg = skin.getCfg();
		ItemInfo costItem = new ItemInfo(cfg.getStepUpItem());
		long maxCount = costItem.getCount();
		long costCount = Math.min(itemCount, maxCount);

		costItem.setCount(costCount);
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(costItem, false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.HERO_SKIN_STARUP);

		double luck = skin.getLuck();
		double itemRate = costCount * 1D / maxCount;
		boolean isSuccess = Math.random() < itemRate || luck > 0.99;
		if (isSuccess) {
			skin.setStep(step + 1);
			skin.setLuck(0);
		} else {
			luck = Math.min(1, luck + costCount * cfg.getLuckRate() * 0.01);
			skin.setLuck(luck);
		}

		hero.notifyChange();

		player.sendProtocol(HawkProtocol.valueOf(HP.code.HERO_SKIN_UP_S, PBHeroSkinStepUpResp.newBuilder().setSuccess(isSuccess)));

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.HERO_SKIN_STARUP,
				Params.valueOf("cost", consumeItems.getItemsInfo()),
				Params.valueOf("skinId", skinId),
				Params.valueOf("step", step),
				Params.valueOf("heroId", heroId));

		LogUtil.logHeroSkinChange(player, Action.HERO_SKIN_UNLOCK, consumeItems.getItemsInfo(), skin);
	}

	/** 天赋解锁 */
	@ProtocolHandler(code = HP.code.HERO_TALENT_UNLOCK_VALUE)
	private void onTalentUnlock(HawkProtocol protocol) {
		PBUnlockHeroTalentReq req = protocol.parseProtocol(PBUnlockHeroTalentReq.getDefaultInstance());
		int index = req.getIndex();
		int heroId = req.getHeroId();
		PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
		if (hero == null) {
			return;
		}
		if (hero.getStar() < 5) {
			return;
		}
		if (hero.getConfig().getQualityColor() > 5) { // SSS英雄不走这套天赋
			return;
		}

		Optional<TalentSlot> talentOp = hero.getTalentSlotByIndex(index);
		if (!talentOp.isPresent()) {
			return;
		}
		TalentSlot talentSlot = talentOp.get();
		if (talentSlot.isUnLock()) {
			return;
		}

		ItemInfo itemList = hero.getConfig().getTalentUnlockPrice(index);
		ConsumeItems consum = ConsumeItems.valueOf();
		consum.addConsumeInfo(itemList, true);
		if (!consum.checkConsume(player, protocol.getType())) {
			return;
		}
		consum.consumeAndPush(player, Action.HERO_TALENT_UNLOCK);
		talentSlot.setUnlock(true);
		int talentId = 0;
		if (index == 0) {
			talentId = hero.getConfig().getPassiveTalent();
		} else {
			HeroTalentPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(HeroTalentPoolCfg.class, hero.getConfig().getTalentList());
			talentId = poolCfg.randomTalent(hero, index);
		}
		talentSlot.setTalent(new HeroTalent(talentId));

		hero.notifyChange();
		player.responseSuccess(protocol.getType());

		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.HERO_TALENT_UNLOCK,
				Params.valueOf("heroId", heroId),
				Params.valueOf("index", index),
				Params.valueOf("exp", talentSlot.getTalent().getExp()),
				Params.valueOf("talentId", talentId));
	}

	/** 天赋强化 */
	@ProtocolHandler(code = HP.code.HERO_TALENT_STRENG_VALUE)
	private void onTalentStrengh(HawkProtocol protocol) {
		PBStrengTalentReq req = protocol.parseProtocol(PBStrengTalentReq.getDefaultInstance());
		int index = req.getIndex();
		int heroId = req.getHeroId();
		int times = req.getType() > 0 ? 10 : 1;
		PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
		if (hero == null) {
			return;
		}

		Optional<TalentSlot> talentOp = hero.getTalentSlotByIndex(index);
		if (!talentOp.isPresent()) {
			return;
		}
		TalentSlot slot = talentOp.get();
		if (!slot.isUnLock() || slot.getTalent() == null) {
			return;
		}

		PBStrengTalentResp.Builder resp = PBStrengTalentResp.newBuilder();
		resp.setHeroId(heroId).setIndex(index).setTalentId(slot.getTalent().getSkillID()).setType(req.getType());
		int maxExp = slot.getTalent().getCfg().getMaxPoint();
		int toAdd = 0;
		int costCount = 0;
		final int oldExp = slot.getTalent().getExp();

		HawkTuple5<Integer, Double, Double, Integer, Integer> eic = slot.getTalent().getCfg().getBuff().get(0);
		for (int i = 0; i < times; i++) {
			if (toAdd + oldExp >= maxExp) {
				break;
			}

			toAdd += ConstProperty.getInstance().randomHeroTalentLevelupRate(oldExp);
			costCount++;

			/** 模拟属性 */
			int effVal = (int) Math.ceil(eic.second + eic.third * (oldExp + toAdd));
			PBHeroEffect ef = PBHeroEffect.newBuilder()
					.setEffectId(eic.first)
					.setValue(effVal).build();
			resp.addEffect(ef);
		}

		ConsumeItems consum = ConsumeItems.valueOf();
		ItemInfo cost = ItemInfo.valueOf(hero.getConfig().getTalentLevelupCost());
		cost.setCount(cost.getCount() * costCount);
		consum.addConsumeInfo(cost, false);
		if (!consum.checkConsume(player, protocol.getType())) {
			return;
		}
		consum.consumeAndPush(player, Action.HERO_TALENT_STRENG);

		slot.getTalent().addExp(toAdd);

		hero.notifyChange();
		player.sendProtocol(HawkProtocol.valueOf(HP.code.HERO_TALENT_STRENG_S, resp));
		LogUtil.logHeroTalentStrengh(player, heroId, index, slot.getTalent().getSkillID(), cost.toString(), toAdd, slot.getTalent().getExp(), (int) slot.power());
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.HERO_TALENT_STRENG,
				Params.valueOf("heroId", heroId),
				Params.valueOf("index", index),
				Params.valueOf("cost", cost.toString()),
				Params.valueOf("exp", slot.getTalent().getExp()),
				Params.valueOf("talentId", slot.getTalent().getSkillID()));
	}

	/** 天赋随机 */
	@ProtocolHandler(code = HP.code.HERO_TALENT_RAND_TALE_VALUE)
	private void onTalentRoll(HawkProtocol protocol) {
		PBRandomTalentReq req = protocol.parseProtocol(PBRandomTalentReq.getDefaultInstance());
		int index = req.getIndex();
		int heroId = req.getHeroId();
		int selectPool = req.getSelectPool();
		if (selectPool > 0 && !selectPoolCheck(heroId, selectPool)) { // 如果玩家主动选择了池子
			selectPool = 0;
		}
		if (index == 0) {
			return;
		}
		PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
		if (hero == null) {
			return;
		}

		Optional<TalentSlot> talentOp = hero.getTalentSlotByIndex(index);
		if (!talentOp.isPresent()) {
			return;
		}
		TalentSlot slot = talentOp.get();
		if (!slot.isUnLock()) {
			return;
		}
		if (selectPool == 0) {
			selectPool = hero.getConfig().getTalentList();
		}
		HeroTalentPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(HeroTalentPoolCfg.class, selectPool);
		int talent = poolCfg.randomTalent(hero, index);
		if (talent == 0) {
			return;
		}

		int todayRollCount = RedisProxy.getInstance().getHeroTalentDayRollCount(player.getId());
		ConsumeItems consum = ConsumeItems.valueOf();
		ItemInfo costInfo = ItemInfo.valueOf(getTalentRoolPrice(todayRollCount));
		consum.addConsumeInfo(Arrays.asList(costInfo), true);
		if (!consum.checkConsume(player, protocol.getType())) {
			return;
		}
		consum.consumeAndPush(player, Action.HERO_TALENT_ROLL_TALE);

		hero.getTalentSlots().forEach(t -> t.setPreSetTalent(0));

		slot.setPreSetTalent(talent);
		// 增加今日随机次数
		RedisProxy.getInstance().incHeroTalentDayRollCount(player.getId());
		todayRollCount++;
		hero.notifyChange();
		HeroTalentCfg talentCfg = HawkConfigManager.getInstance().getConfigByKey(HeroTalentCfg.class, talent);
		PBRandomTalentResp.Builder resp = PBRandomTalentResp.newBuilder();
		resp.setHeroId(heroId);
		resp.setIndex(index);
		resp.setTalentId(talent);
		resp.setRandCount(todayRollCount);
		resp.setTalentRollCost(getTalentRoolPrice(todayRollCount));
		final int attrVal = Math.min(slot.getTalent().getExp(), talentCfg.getMaxPoint());
		for (HawkTuple5<Integer, Double, Double, Integer, Integer> eic : talentCfg.getBuff()) {
			int effVal = (int) Math.ceil(eic.second + eic.third * attrVal);
			PBHeroEffect ef = PBHeroEffect.newBuilder()
					.setEffectId(eic.first)
					.setValue(effVal).build();
			resp.addEffect(ef);
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.HERO_TALENT_RAND_TALE_S, resp));

		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.HERO_TALENT_ROLL_TALE,
				Params.valueOf("heroId", heroId),
				Params.valueOf("index", index),
				Params.valueOf("exp", slot.getTalent().getExp()),
				Params.valueOf("talentId", slot.getTalent().getSkillID()),
				Params.valueOf("rollId", talent));
		// 天赋随机打点
		LogUtil.logHeroTalentRandom(player, heroId, index, selectPool, talent, costInfo.getItemId(), costInfo.getCount());
	}

	/**玩家手动选择天赋池验证*/
	private boolean selectPoolCheck(int heroId, int selectPool) {
		HeroTalentPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(HeroTalentPoolCfg.class, selectPool);
		if (poolCfg == null) {
			return false;
		}
		if (poolCfg.getHero() != heroId) {
			return false;
		}
		if (poolCfg.getType() == 2) {
			boolean activityOpen = this.activityBreakShacklesOpen();
			if (!activityOpen) {
				return false;
			}
		}

		return true;
	}

	/** 冲破枷锁活动是否开启*/
	public boolean activityBreakShacklesOpen() {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.BREAK_SHACKLES_ACTIVITY_VALUE);
		if (opActivity.isPresent()) {
			ActivityBase activity = opActivity.get();
			if (activity.isOpening(player.getId())) {
				return true;
			}
		}
		return false;

	}

	public String getTalentRoolPrice(int todayRollCount) {
		String cost = ConstProperty.getInstance().getJeroTalentChangeCost(todayRollCount);
		return cost;
	}

	/** 天赋随机选定 */
	@ProtocolHandler(code = HP.code.HERO_TALENT_CHOSE_TALE_VALUE)
	private void onTalentRollChose(HawkProtocol protocol) {
		PBChoseRandTalentReq req = protocol.parseProtocol(PBChoseRandTalentReq.getDefaultInstance());
		int index = req.getIndex();
		int heroId = req.getHeroId();
		if (index == 0) {
			return;
		}
		PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
		if (hero == null) {
			return;
		}

		Optional<TalentSlot> talentOp = hero.getTalentSlotByIndex(index);
		if (!talentOp.isPresent()) {
			return;
		}
		TalentSlot slot = talentOp.get();
		if (!slot.isUnLock()) {
			return;
		}
		int talent = slot.getPreSetTalent();
		if (talent == 0) {
			return;
		}
		int oldTalent = 0;
		if (slot.getTalent() == null) {
			slot.setTalent(new HeroTalent(talent));
		} else {
			oldTalent = slot.getTalent().getSkillID();
			slot.getTalent().setSkillID(talent);
		}

		hero.notifyChange();
		player.responseSuccess(protocol.getType());

		LogUtil.logHeroTalentSelect(player, heroId, index, oldTalent, talent, slot.getTalent().getExp(), (int) slot.power());

		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.HERO_TALENT_CHOSE_TALE,
				Params.valueOf("heroId", heroId),
				Params.valueOf("index", index),
				Params.valueOf("exp", slot.getTalent().getExp()),
				Params.valueOf("talentId", slot.getTalent().getSkillID()));
	}

	/** 天赋标记打开 */
	@ProtocolHandler(code = HP.code.HERO_TALENT_OPEN_VALUE)
	private void onTalentMarkOpen(HawkProtocol protocol) {
		PBUnlockHeroTalentReq req = protocol.parseProtocol(PBUnlockHeroTalentReq.getDefaultInstance());
		int heroId = req.getHeroId();
		PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
		if (hero == null) {
			return;
		}
		if (hero.getStar() < 5) {
			return;
		}
		hero.setTalentOpen();
		hero.notifyChange();
		player.responseSuccess(protocol.getType());
	}

	/** 天赋随即价格 */
	@ProtocolHandler(code = HP.code.HERO_TALENT_ROLL_PRICE_C_VALUE)
	private void onTalentRollPrice(HawkProtocol protocol) {
		int todayRollCount = RedisProxy.getInstance().getHeroTalentDayRollCount(player.getId());
		String price = getTalentRoolPrice(todayRollCount);
		PBHeroTalentRollPriceResp.Builder resp = PBHeroTalentRollPriceResp.newBuilder();
		resp.setPrice(price);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.HERO_TALENT_ROLL_PRICE_S, resp));
	}

	/** 使用英雄皮肤道具 */
	@ProtocolHandler(code = HP.code.HERO_USE_SKIN_ITEM_VALUE)
	private void onUseSkinItem(HawkProtocol protocol) {
		HPHeroUseSkinItemReq req = protocol.parseProtocol(HPHeroUseSkinItemReq.getDefaultInstance());
		ItemInfo item = ItemInfo.valueOf(req.getSkinItem());
		final int itemId = item.getItemId();
		ItemCfg skinItemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		if (Objects.isNull(skinItemCfg) || skinItemCfg.getItemType() != ToolType.HERO_SKIN_VALUE) {
			return;
		}
		BuffCfg bufCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, skinItemCfg.getBuffId());
		int skinId = bufCfg.getEffect();
		int heroId = HeroSkin.getHeroIdBySkinId(skinId);
		PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
		if (hero == null) {
			return;
		}
		StatusDataEntity entity = player.getData().getStatusById(bufCfg.getEffect());
		if (entity != null && entity.getEndTime() > HawkTime.getMillisecond()) {
			hero.changeSkin(skinId);
			return;
		}
		ConsumeItems consum = ConsumeItems.valueOf();
		consum.addItemConsume(itemId, 1);
		if (!consum.checkConsume(player, protocol.getType())) {
			return;
		}
		consum.consumeAndPush(player, Action.HERO_SKIN_USE);

		GameUtil.addBuff(player, skinItemCfg, "");
		hero.changeSkin(skinId);
		player.responseSuccess(protocol.getType());
	}

	/**
	 * 更换皮肤
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.HERO_CHANGE_SKIN_VALUE)
	public void onChangeSkin(HawkProtocol protocol) {
		HPHeroChangeSkinReq req = protocol.parseProtocol(HPHeroChangeSkinReq.getDefaultInstance());
		int skinId = req.getSkinId();
		int heroId = req.getHeroId();

		PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
		if (hero == null) {
			return;
		}

		if (skinId == 0) { // 还原原始皮肤, 不需要验证
			hero.changeSkin(skinId);
			player.responseSuccess(protocol.getType());
			return;
		}
		Optional<HeroSkin> skinOp = hero.getSkin(skinId);
		if (!skinOp.isPresent()) {
			return;
		}

		HeroSkin skin = skinOp.get();
		if (skin == null || !skin.isUnlock()) {
			return;
		}

		hero.changeSkin(skinId);
		player.responseSuccess(protocol.getType());
	}

	/**
	 * 主动请求同步扭蛋信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GACHA_SYNC_C_VALUE)
	private boolean onSyncGachaInfo(HawkProtocol protocol) {
		HPSyncGachaInfoResp.Builder resp = BuilderUtil.gachaInfoPB(player.getData());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GACHA_SYNC_S, resp));
		return true;
	}

	/**
	 * 扭蛋（英雄招募）
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GACHA_C_VALUE)
	private boolean onGacha(HawkProtocol protocol) {
		HPGachaReq req = protocol.parseProtocol(HPGachaReq.getDefaultInstance());
		final GachaType gachaType = GachaType.valueOf(req.getGachaType());
		GachaOprator gachaOprator = GachaOprator.of(gachaType);
		if (gachaOprator.isGachaModule()) {
			gachaRefresh(); //检查下跨天
			return player.getPlayerMechaCore().gachaModule(player, gachaType, req);
		}
		
		GachaCfg gachaCfg = HawkConfigManager.getInstance().getConfigByKey(GachaCfg.class, gachaType.getNumber());
		PlayerGachaEntity gachaEntity = player.getData().getGachaEntityByType(gachaType);
		boolean batchable = gachaOprator.getGachaCount() == GachaOprator.DEFAULT_BATCH || gachaType == GachaType.ARMOUR_BOX;
		int batchGachaCnt = req.getBatchGachaCnt();
		if (batchGachaCnt > 0 && batchable) {
			if (gachaType == GachaType.ADVANCE_TEN || gachaType == GachaType.NORMAL_TEN) {
				batchGachaCnt = Math.min(batchGachaCnt, ConstProperty.getInstance().getBatchHeroMax());
			}
			if (gachaType == GachaType.SKILL_TEN) {
				batchGachaCnt = Math.min(batchGachaCnt, ConstProperty.getInstance().getBatchChipMax());
			}
			if (gachaType == GachaType.ARMOUR_TEN) {
				batchGachaCnt = Math.min(batchGachaCnt, ConstProperty.getInstance().getBatchEquipmentMax());
			}
			gachaOprator.setGachaCount(batchGachaCnt);
		}
		// 检查下跨天
		gachaRefresh();
		
		// 验证消耗
		CheckAndConsumResult checkAndConsumResult = gachaOprator.checkAndConsum(gachaCfg, gachaEntity, player);
		if (!checkAndConsumResult.isSuccess()) {
			// sendError(protocol.getType(),
			// Status.Error.DIAMONDS_NOT_ENOUGH_VALUE);
			return false;
		}

		// awardItem.addItem(ItemInfo.valueOf("10000_1001_999999"));
		// 先给默认奖励
		List<ItemInfo> gachaAwardItems = new LinkedList<>();
		if (gachaCfg.getBuyItem().length() > 0) {
			ItemInfo buyItem = ItemInfo.valueOf(gachaCfg.getBuyItem());
			buyItem.setCount(gachaOprator.getGachaCount());
			gachaAwardItems.add(buyItem);
		}

		// 显示奖励
		List<String> rewardsShow = gachaOprator.gacha(gachaCfg, gachaEntity, player);
		rewardsShow.forEach(re -> gachaAwardItems.add(ItemInfo.valueOf(re)));

		player.getPush().syncGachaInfo();
		
		// 刷新任务
		int gachaCount = gachaOprator.getGachaCount();
		ActivityManager.getInstance().postEvent(new RandomHeroEvent(player.getId(), gachaCount, gachaType.getNumber()));
		MissionManager.getInstance().postMsg(player, new EventGacha(gachaType.getNumber(), gachaCount));
		
		// 活动事件
		if (gachaType == GachaType.ARMOUR_ONE || gachaType == GachaType.ARMOUR_TEN) {
			EquipTechActivityKVConfig cfg = HawkConfigManager.getInstance().getKVInstance(EquipTechActivityKVConfig.class);
			int addScore = cfg.getGetScore() * gachaCount;
			ActivityManager.getInstance().postEvent(new EquipTechScoreEvent(player.getId(), addScore));
		}

		if (gachaType == GachaType.ARMOUR_ONE || gachaType == GachaType.ARMOUR_TEN ||
				gachaType == GachaType.ARMOUR_BOX) {
			// 活动是否开启
			Optional<ActivityBase> optionalActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.COMMAND_COLLEGE_VALUE);
			boolean isActOpen = optionalActivity.isPresent() ? optionalActivity.get().isOpening(player.getId()) : false;
			
			Optional<ActivityBase> newActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.COMMAND_COLLEGE_SIMPLIFY_VALUE);
			boolean isNewActOpen = newActivity.isPresent() ? newActivity.get().isOpening(player.getId()) : false;
			if (isActOpen || isNewActOpen) {
				EquipQualityAchiveEvent event = new EquipQualityAchiveEvent(player.getId());
				for (ItemInfo info : gachaAwardItems) {
					ArmourPoolCfg aCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourPoolCfg.class, info.getItemId());
					if (aCfg == null || info.getCount() <= 0) {
						continue;
					}
					event.addEquip(aCfg.getQuality(), (int) info.getCount());
				}
				ActivityManager.getInstance().postEvent(event);
			}
		}
		// 记录英雄抽卡打点日志
		LogUtil.logGacha(player, gachaType.getNumber(), checkAndConsumResult.getCost());
		LogUtil.logGachaItemsFlow(player, gachaType.getNumber(), gachaAwardItems);

		HPGachaResp.Builder resp = HPGachaResp.newBuilder().setGachaType(gachaType.getNumber()).addAllRewards(rewardsShow).setBatchGachaCnt(batchGachaCnt).setArmourResolveQuality(req.getArmourResolveQuality());
		int armourResolveNum = 0;
		if (batchGachaCnt > 0 &&  gachaType == GachaType.ARMOUR_TEN) { // 处理多抽装备分解
			int armourResolveQuality = Math.max(req.getArmourResolveQuality(), 2);
			// 分解奖励
			AwardItems resolveAward = AwardItems.valueOf();
			List<ItemInfo> removeList =new ArrayList<>(gachaAwardItems.size());
			for (ItemInfo item : gachaAwardItems) {
				int itemType = item.getType() / GsConst.ITEM_TYPE_BASE;
				if (itemType == Const.ItemType.ARMOUR_VALUE) {
					int armourPoolId = item.getItemId();
					ArmourPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourPoolCfg.class, armourPoolId);
					if(poolCfg.getQuality() > armourResolveQuality){
						continue;
					}
					ArmourLevelCfg armourLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourLevelCfg.class, 0);
					ArmourBreakthroughCfg armourQualityCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourBreakthroughCfg.class, poolCfg.getQuality());
					resolveAward.addAwards(armourLevelCfg.getResolveAwards());
					resolveAward.addAwards(armourQualityCfg.getResolveAwards());
					removeList.add(item);
					armourResolveNum++;
				}
			}
			
			gachaAwardItems.removeAll(removeList);
			gachaAwardItems.addAll(resolveAward.getAwardItems());
			
			resp.setArmourResolveRewards(ItemInfo.toString(resolveAward.getAwardItems())).setArmourResolveNum(armourResolveNum);
			//成长激励活动需要记录
			Map<Integer, Long> itemsAwardMap = resolveAward.getAwardItemsCount();
			ActivityManager.getInstance().getDataGeter().growUpBoostEquipDecomposeItemRecord(player.getId(), itemsAwardMap);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GACHA_S, resp));
		
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItemInfos(gachaAwardItems);
		awardItem.rewardTakeAffectAndPush(player, Action.GACHA);
		
		return true;
	}

	/**
	 * 查询英雄信息
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.HERO_INFO_REQUEST_C_VALUE)
	private void onHeroInfoRequest(HawkProtocol protocol) {
		final int heroId = 0;
		final String playerId = "";
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(playerId);
		if (Objects.isNull(targetPlayer)) {
			return;
		}
		PlayerHero targetHero = targetPlayer.getHeroByCfgId(heroId).orElse(null);
		if (Objects.isNull(targetHero)) {
			return;
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.HERO_INFO_REQUEST_S,
				targetHero.toPBobj().toBuilder()));
	}

	/**
	 * 升星
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.HERO_STAR_UP_C_VALUE)
	private void onHeroStarUp(HawkProtocol protocol) {
		PBHeroStarUpRequest req = protocol.parseProtocol(PBHeroStarUpRequest.getDefaultInstance());
		final int heroId = req.getHeroId();
		final int toStar = req.getToStar();
		final int toStep = req.getToStep();
		int itemId = req.getItemId();
		int itemCount = req.getItemCount();
		int toItemId = req.getToItemId();

		HawkAssert.checkNonNegative(itemCount);

		Optional<PlayerHero> heroOp = player.getHeroByCfgId(heroId);
		if (!heroOp.isPresent()) {
			return;
		}

		HeroStarLevelCfg toStarLevelCfg = HawkConfigManager.getInstance().getCombineConfig(HeroStarLevelCfg.class, heroId, toStar, toStep);
		if (Objects.isNull(toStarLevelCfg)) {
			return;
		}
		PlayerHero hero = heroOp.get();
		final int beforeStar = hero.getStar();
		final int beforeStep = hero.getStep();
		HeroStarLevelCfg starLevelCfg = HawkConfigManager.getInstance().getCombineConfig(HeroStarLevelCfg.class, heroId, hero.getStar(), hero.getStep());
		if (toStarLevelCfg.getId() <= starLevelCfg.getId()) {
			return;
		}

		if (itemCount > 0) {// 需要兑换
			exchangeItem(protocol.getType(), itemId, itemCount, toItemId, heroId);
		}

		HeroCfg heroCfg = hero.getConfig();
		HeroColorQualityCfg colorQualityCfg = HawkConfigManager.getInstance().getConfigByKey(HeroColorQualityCfg.class, heroCfg.getQualityColor());
		if (hero.getStar() > colorQualityCfg.getMaxStarLevel()) {// 最高星级
			return;
		}
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		ConfigIterator<HeroStarLevelCfg> slit = HawkConfigManager.getInstance().getConfigIterator(HeroStarLevelCfg.class);
		slit.stream()
				.filter(st -> st.getHeroId() == heroId)
				.filter(st -> st.getId() >= starLevelCfg.getId())
				.filter(st -> st.getId() < toStarLevelCfg.getId())
				.forEach(st -> {
					consumeItems.addConsumeInfo(new ItemInfo(st.getPiecesForNextLevel()), false);
				});

		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}

		hero.starUp(toStar, toStep);
		consumeItems.consumeAndPush(player, Action.HERO_STAR_UP);

		player.responseSuccess(protocol.getType());
		// 抛出活动事件
		ActivityManager.getInstance().postEvent(new HeroUpStarEvent(player.getId(), hero.getCfgId(), beforeStar, hero.getStar()));
		ActivityManager.getInstance().postEvent(new HeroChangeEvent(player.getId()));
		MissionManager.getInstance().postMsg(player, new EventHeroStarUp(heroId, beforeStar, hero.getStar()));
		MissionManager.getInstance().postMsg(player, new EventHeroChange());
		// 推送礼包
		HawkTaskManager.getInstance().postMsg(player.getXid(), new HeroStarUpMsg(hero.getCfgId(), beforeStar, hero.getStar()));

		StrengthenGuideManager.getInstance().postMsg(new SGHeroStarUpMsg(player));

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.HERO_STAR_UP,
				Params.valueOf("beforeStar", beforeStar),
				Params.valueOf("beforeStep", beforeStep),
				Params.valueOf("cost", consumeItems.getItemsInfo()),
				Params.valueOf("star", hero.getStar()),
				Params.valueOf("step", hero.getStep()));

		LogUtil.logHeroAttrChange(player, Action.HERO_STAR_UP, 0, hero);
	}

	/** 使用经验道具 */
	@ProtocolHandler(code = HP.code.HERO_ADD_EXP_C_VALUE)
	private void onUseHeroExpItem(HawkProtocol protocol) {
		PBUseHeroExpItemRequest req = protocol.parseProtocol(PBUseHeroExpItemRequest.getDefaultInstance());
		final int heroId = req.getHeroId();
		Optional<PlayerHero> heroOp = player.getHeroByCfgId(heroId);
		if (!heroOp.isPresent()) {
			return;
		}
		PlayerHero hero = heroOp.get();

		int totalExpAdd = 0;
		ConsumeItems consume = ConsumeItems.valueOf();
		int count = 0;
		for (PBHeroItem item : req.getItemUseList()) {
			ItemCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, item.getItemId());
			if (Objects.isNull(cfg) || cfg.getItemType() != Const.ToolType.HERO_EXP_VALUE) { // 只能使用英雄加经验道具.业务失败
				return;
			}
			consume.addItemConsume(item.getItemId(), item.getCount());
			totalExpAdd = totalExpAdd + cfg.getHeroExp() * item.getCount();
			count += item.getCount();
		}
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		consume.consumeAndPush(player, Action.USE_HERO_EXP);
		hero.addExp(totalExpAdd);

		player.responseSuccess(protocol.getType());

		// 每日任务 道具总数
		int itemCount = req.getItemUseList().stream().mapToInt(PBHeroItem::getCount).sum();
		ActivityManager.getInstance().postEvent(new UseHeroExpItemEvent(player.getId(), itemCount));
		if (count >= 2) {
			GsApp.getInstance().postMsg(player.getXid(), new OneKeyHeroUpMsg());
		}
		LogUtil.logHeroAttrChange(player, Action.USE_HERO_EXP, 0, hero);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.USE_HERO_EXP,
				Params.valueOf("items", req.getItemUseList()));
	}

	/**
	 * 安装技能
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.HERO_INSTALL_SKILL_C_VALUE)
	private void onInstallSkill(HawkProtocol protocol) {
		PBInstallSkillRequest req = protocol.parseProtocol(PBInstallSkillRequest.getDefaultInstance());
		final int heroId = req.getHeroId();
		final int slotIndex = req.getIndex();
		final int skillItemId = req.getItemId();
		Optional<PlayerHero> heroOp = player.getHeroByCfgId(heroId);
		if (!heroOp.isPresent()) {
			return;
		}
		PlayerHero hero = heroOp.get();

		Optional<SkillSlot> slotOp = hero.getSkillSlots().stream().filter(slot -> slot.getIndex() == slotIndex).findAny();
		if (!slotOp.isPresent() || !slotOp.get().isUnLock() || slotOp.get().getSkill() != null) {
			return;
		}
		ItemCfg skillItemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, skillItemId);
		if (Objects.isNull(skillItemCfg) || hero.getSkillById(skillItemCfg.getSkillGet()) != null) {
			return;
		}

		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addItemConsume(skillItemId, 1);
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}

		consume.consumeAndPush(player, Action.HERO_INSTALL_SKILL);

		IHeroSkill skill = HeroSkillFactory.getInstance().createEmptySkill(skillItemCfg.getSkillGet());
		slotOp.get().setSkill(skill);
		hero.notifyChange();
		player.responseSuccess(protocol.getType());

		LogUtil.logHeroAttrChange(player, Action.HERO_INSTALL_SKILL, 0, hero);
		HeroSkillCfg heroSkillCfg = skill.getCfg();
		if (heroSkillCfg != null) {
			LogUtil.logHeroSkillEquip(player, heroSkillCfg.getSkillType(), heroSkillCfg.getSkillId(), skill.getLevel(), true, hero);
		}

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.HERO_INSTALL_SKILL,
				Params.valueOf("heroId", heroId),
				Params.valueOf("index", slotIndex),
				Params.valueOf("skillItemId", skillItemId));

		MissionManager.getInstance().postMsg(player, new EventInstallSkill());
	}

	/**
	 * 拆除一个英雄技能
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.HERO_REMOVE_SKILL_C_VALUE)
	private void onRemoveSkill(HawkProtocol protocol) {
		PBRemoveSkillRequest req = protocol.parseProtocol(PBRemoveSkillRequest.getDefaultInstance());
		final int heroId = req.getHeroId();
		final int slotIndex = req.getIndex();
		// final int itemId = req.getIntemId(); // 保留80%经验, 用道具的话返还技能道具
		Optional<PlayerHero> heroOp = player.getHeroByCfgId(heroId);
		if (!heroOp.isPresent()) {
			return;
		}
		PlayerHero hero = heroOp.get();
		if (hero.getConfig().getStaffOfficer() == 1) {
			return;
		}
		Optional<SkillSlot> slotOp = hero.getSkillSlots().stream().filter(slot -> slot.getIndex() == slotIndex).findAny();
		if (!slotOp.isPresent()) {
			return;
		}
		IHeroSkill toRemoveSkill = slotOp.get().getSkill();
		if (toRemoveSkill == null) { // 并没有技能
			return;
		}
		// ItemCfg removeItemCfg =
		// HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class,
		// itemId);
		// if (Objects.nonNull(removeItemCfg) && removeItemCfg.getItemType() ==
		// Const.ToolType.HERO_REMOVE_SKILL_VALUE) {
		// 消耗道具 返还技能道具
		// ConsumeItems consume = ConsumeItems.valueOf();
		// consume.addItemConsume(itemId, 1);
		// if (consume.checkConsume(player, protocol.getType())) {
		// consume.consumeAndPush(player, Action.HERO_SKILL_UNINSTALL);
		Optional<ItemCfg> skillCfgOP = HawkConfigManager.getInstance().getConfigIterator(ItemCfg.class).stream()
				.filter(cfg -> cfg.getItemType() == Const.ToolType.HERO_SKILL_VALUE)
				.filter(cfg -> cfg.getSkillGet() == toRemoveSkill.skillID())
				.findAny();
		if (skillCfgOP.isPresent()) {
			AwardItems awardItem = AwardItems.valueOf();
			awardItem.addItem(ItemType.TOOL_VALUE, skillCfgOP.get().getId(), 1);
			awardItem.rewardTakeAffectAndPush(player, Action.HERO_SKILL_UNINSTALL);
		}
		// }
		// }

		// 返还80%经验
		if (toRemoveSkill.getExp() > 0) {
			int backExp = (int) (toRemoveSkill.getExp() * 0.8);
			List<ItemCfg> skillExpItemList = HawkConfigManager.getInstance().getConfigIterator(ItemCfg.class).stream()
					.filter(cfg -> cfg.getItemType() == Const.ToolType.HERO_SKILL_EXP_VALUE)
					.sorted(Comparator.comparingInt(ItemCfg::getSkillExp).reversed())
					.collect(Collectors.toList());
			AwardItems awardItem = AwardItems.valueOf();
			for (ItemCfg cfg : skillExpItemList) {
				int count = backExp / cfg.getSkillExp();
				if (count > 0) {
					awardItem.addItem(ItemType.TOOL_VALUE, cfg.getId(), count);
					backExp = backExp - cfg.getSkillExp() * count;
				}
			}
			if (!awardItem.getAwardItems().isEmpty()) {
				awardItem.rewardTakeAffectAndPush(player, Action.HERO_SKILL_UNINSTALL);
			}
		}

		HeroSkillCfg heroSkillCfg = toRemoveSkill.getCfg();
		if (heroSkillCfg != null) {
			LogUtil.logHeroSkillEquip(player, heroSkillCfg.getSkillType(), heroSkillCfg.getSkillId(), toRemoveSkill.getLevel(), false, hero);
		}

		slotOp.get().setSkill(null);
		hero.notifyChange();
		player.responseSuccess(protocol.getType());

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.HERO_SKILL_UNINSTALL,
				Params.valueOf("heroId", heroId),
				Params.valueOf("slotIndex", slotIndex));
	}

	/**
	 * 使用技能经验道具
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.HERO_SKILL_ADD_EXP_C_VALUE)
	private void onUseSkillExpItem(HawkProtocol protocol) {
		PBUseSkillExpItemRequest req = protocol.parseProtocol(PBUseSkillExpItemRequest.getDefaultInstance());
		int heroId = req.getHeroId();
		int index = req.getIndex();
		int type = req.getType(); // 1 自带技能, 2 学习技能
		Optional<PlayerHero> heroOp = player.getHeroByCfgId(heroId);
		if (!heroOp.isPresent()) {
			return;
		}

		PlayerHero hero = heroOp.get();
		if (hero.getConfig().getStaffOfficer() == 1) {
			return;
		}
		
		ImmutableList<SkillSlot> skillSlots = type == 1 ? hero.getPassiveSkillSlots() : hero.getSkillSlots();
		Optional<SkillSlot> skillSlot = skillSlots.stream().filter(slot -> slot.getIndex() == index).findAny();
		if (!skillSlot.isPresent() || skillSlot.get().getSkill() == null) {
			return;
		}
		IHeroSkill skill = skillSlot.get().getSkill();
		if (skill.getLevel() >= skill.maxLevel()) {
			return;
		}

		int totalExpAdd = 0;
		ConsumeItems consume = ConsumeItems.valueOf();
		int count = 0;
		for (PBHeroItem item : req.getItemUseList()) {
			ItemCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, item.getItemId());
			if (Objects.isNull(cfg) || cfg.getItemType() != Const.ToolType.HERO_SKILL_EXP_VALUE) { // 只能使用英雄技能加经验道具.业务失败
				return;
			}
			consume.addItemConsume(item.getItemId(), item.getCount());
			totalExpAdd = totalExpAdd + cfg.getSkillExp() * item.getCount();
			count += item.getCount();
		}

		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}

		consume.consumeAndPush(player, Action.USE_HERO_SKILL_EXP);
		int skillLevelBefore = skill.getLevel();
		int skillexpBefore = skill.getExp();
		skill.addExp(totalExpAdd);
		hero.notifyChange();
		player.responseSuccess(protocol.getType());

		int skillLevelAfter = skill.getLevel();
		int skillExpAfter = skill.getExp();
		HeroSkillCfg heroSkillCfg = skill.getCfg();
		if (skillLevelAfter != skillLevelBefore && heroSkillCfg != null) {
			LogUtil.logHeroSkillChange(player, heroId, index, heroSkillCfg.getSkillType(), heroSkillCfg.getSkillId(), skillLevelBefore, skillLevelAfter, HeroSkillOperType.LEVELUP);
		}
		if (count >= 2) {
			GsApp.getInstance().postMsg(player.getXid(), new OneKeyHeroSkillUpMsg());
		}

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.USE_HERO_SKILL_EXP,
				Params.valueOf("heroId", heroId),
				Params.valueOf("slotIndex", index),
				Params.valueOf("skillb", skillLevelBefore),
				Params.valueOf("skilla", skillLevelAfter),
				Params.valueOf("items", req.getItemUseList().toString()),
				Params.valueOf("skillexpBefore", skillexpBefore),
				Params.valueOf("skillExpAfter", skillExpAfter));

		MissionManager.getInstance().postMsg(player, new EventInstallSkill());
	}

	/**
	 * 委任官员
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.HERO_OFFICE_APPOINT_C_VALUE)
	private void onHeroOfficeAppoint(HawkProtocol protocol) {
		PBHeroOfficeAppointRequest req = protocol.parseProtocol(PBHeroOfficeAppointRequest.getDefaultInstance());

		for (PBHeroOffice officePB : req.getOfficeList()) {
			int officeId = officePB.getOffice();
			int heroId = officePB.getHeroId();
			HeroOfficeCfg officeCfg = HawkConfigManager.getInstance().getConfigByKey(HeroOfficeCfg.class, officeId);
			if (Objects.nonNull(officeCfg) && !officeCfg.checkOfficeBuilding(player.getData().getBuildingEntities())) { // 官没解锁
				continue;
			}
			if (Objects.nonNull(officeCfg) && officeCfg.getTaiLevel() > 0){
				if (officeCfg.getTaiLevel() > player.getMaxSoldierPlantMilitaryLevel()){
					continue;
				}
			}

			Optional<PlayerHero> heroOp = player.getHeroByCfgId(heroId);
			if (!heroOp.isPresent()) {
				continue;
			}
			PlayerHero hero = heroOp.get();
			if (hero.getOffice() > 0) { // 当前英雄是否空闲
				HeroOfficeCfg heroofficeCfg = HawkConfigManager.getInstance().getConfigByKey(HeroOfficeCfg.class, hero.getOffice());
				if (Objects.nonNull(heroofficeCfg)) {
					if (hasBusyQueue(heroofficeCfg)) {
						continue;
					}
					if (heroofficeCfg.getIsglory() == 1) {
						if (WorldMarchService.getInstance().getPlayerMarchCount(player.getId()) > 0) {
							continue;
						}
					}
				}
			}
			PlayerHero oldOfficeHero = officeId > 0 ? getHeroByOfficeId(officeId) : null;
			if (Objects.equals(hero, oldOfficeHero)) {
				continue;
			}

			if (Objects.nonNull(oldOfficeHero) && Objects.nonNull(officeCfg)) {
				if (hasBusyQueue(officeCfg)) {
					continue;
				}
				oldOfficeHero.officeAppoint(0);
			}

			hero.officeAppoint(officeId); // 只能委任非守城
			if (Objects.nonNull(officeCfg)) {
				MissionManager.getInstance().postMsg(player, new EventHeroAppoint(officeCfg.getUnlockBuildingType()));
			}
			LogUtil.logHeroAttrChange(player, Action.HERO_OFFICE_APPOINT, 0, hero);
			// 行为日志
			BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.HERO_OFFICE_APPOINT,
					Params.valueOf("heroId", hero.getCfgId()),
					Params.valueOf("officeId", officeId));

		}

		player.responseSuccess(protocol.getType());
	}

	private boolean hasBusyQueue(HeroOfficeCfg officeCfg) {
		QueueEntity queue = player.getData().getQueueByBuildingType(officeCfg.getUnlockBuildingType());
		if (Objects.nonNull(queue)
				&& queue.getEnableEndTime() == 0
				&& officeCfg.getQueueUsedList().contains(queue.getQueueType())
				&& queue.getReusage() != QueueReusage.FREE.intValue()) {
			return true;
		}
		return false;
	}

	public PlayerHero getHeroByOfficeId(int officeId) {
		for (PlayerHero hero : player.getAllHero()) {
			if (hero.getOffice() == officeId) {
				return hero;
			}
		}
		return null;
	}

	/**
	 * 英雄驻防
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.HERO_CITY_DEF_C_VALUE)
	private void onHeroCityDef(HawkProtocol protocol) {
		PBHeroOfficeAppointRequest req = protocol.parseProtocol(PBHeroOfficeAppointRequest.getDefaultInstance());
		
		PBHeroOffice toofficePB = req.getOfficeList().get(0);
		if (toofficePB.getOffice() == BattleConst.CITY_DEF107 || toofficePB.getOffice() == BattleConst.CITY_DEF108) {
			int officeId = toofficePB.getOffice() == BattleConst.CITY_DEF107 ? BattleConst.CITY_DEF108 : BattleConst.CITY_DEF107;
			PlayerHero defhero = BattleService.getInstance().getHeroBycityDefenseId(player,
					officeId);
			if (defhero != null && defhero.getConfig().getProhibitedHero() == toofficePB.getHeroId()) {
				return;
			}
		}
		
		List<PlayerHero> heroes = player.getAllHero();
		HashBiMap<Integer, PlayerHero> heroOffice = HashBiMap.create(); // 原官职-英雄
		for (PlayerHero hero : heroes) {
			if (hero.getCityDefense() > 0) {
				heroOffice.put(hero.getCityDefense(), hero);
			}
		}

		for (PBHeroOffice officePB : req.getOfficeList()) {
			PlayerHero oldOfficeHero = heroOffice.get(officePB.getOffice());
			if (oldOfficeHero != null && oldOfficeHero.getCfgId() != officePB.getHeroId() && oldOfficeHero.getCityDefense() == officePB.getOffice()) { // 老官卸任
				oldOfficeHero.cityDef(0);
			}
			this.cityDef(officePB);
		}

		player.responseSuccess(protocol.getType());

		GameUtil.setFlagAndPush(player, PlayerFlagPosition.HERO_DEF, 1);
	}

	/**
	 * 技能碎片道具合成技能道具
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.HERO_MERGE_SKILL_C_VALUE)
	private void onMergeSkill(HawkProtocol protocol) {
		PBMergeSkillRequest req = protocol.parseProtocol(PBMergeSkillRequest.getDefaultInstance());
		ConsumeItems consume = ConsumeItems.valueOf();
		AwardItems awardItem = AwardItems.valueOf();
		List<Integer> skillIdList = new ArrayList<>();
		for (PBMergeSkill merge : req.getMergeSkillsList()) {
			final int itemId = merge.getItemId();
			final int skillItemId = merge.getSkillItemId();

			ItemCfg chipCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);// 碎片
			if (Objects.isNull(chipCfg) || chipCfg.getItemType() != Const.ToolType.HERO_SKILL_CHIP_VALUE) {
				continue;
			}

			ItemCfg skillCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, skillItemId);
			if (Objects.isNull(skillCfg)) {
				continue;
			}
			ItemInfo consumeItem = ItemInfo.valueOf(skillCfg.getSkillComposeItem());
			if (consumeItem.getItemId() != itemId) {
				continue;
			}

			int itemCnt = player.getData().getItemNumByItemId(itemId); // 碎片总数
			int mergeCnt = itemCnt / (int) consumeItem.getCount();

			consumeItem.setCount(consumeItem.getCount() * mergeCnt);

			consume.addConsumeInfo(consumeItem, false);

			awardItem.addItem(ItemType.TOOL_VALUE, skillCfg.getId(), mergeCnt);
			skillIdList.add(skillCfg.getSkillGet());
		}

		if (!consume.checkConsume(player)) {
			return;
		}

		consume.consumeAndPush(player, Action.MERGE_HERO_SKILL);
		awardItem.rewardTakeAffectAndPush(player, Action.MERGE_HERO_SKILL);
		player.responseSuccess(protocol.getType());

		for (int skillId : skillIdList) {
			HeroSkillCfg skillCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSkillCfg.class, skillId);
			if (skillCfg != null) {
				LogUtil.logHeroSkillChange(player, 0, 0, skillCfg.getSkillType(), skillCfg.getSkillId(), 0, 1, HeroSkillOperType.MERGE);
			}
		}

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.MERGE_HERO_SKILL,
				Params.valueOf("req", req.toString()));
	}

	/** 分解技能道具为碎片 */
	@ProtocolHandler(code = HP.code.HERO_RESOLVE_SKILL_C_VALUE)
	private void onResolveSkill(HawkProtocol protocol) {
		PBResolveSkillRequest req = protocol.parseProtocol(PBResolveSkillRequest.getDefaultInstance());
		List<PBHeroItem> resList = req.getItemsList();
		ConsumeItems consume = ConsumeItems.valueOf();
		AwardItems awardItem = AwardItems.valueOf();
		for (PBHeroItem ritem : resList) {
			final int itemId = ritem.getItemId();
			final int count = ritem.getCount();

			ItemCfg skillCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId); // 技能道具
			if (skillCfg.getItemType() != Const.ToolType.HERO_SKILL_VALUE) { // 只能分解技能
				continue;
			}

			consume.addItemConsume(itemId, count);

			ItemInfo piceItem = ItemInfo.valueOf(skillCfg.getSkillResolveItem());
			piceItem.setCount(piceItem.getCount() * count);
			awardItem.addItem(piceItem);

			HeroSkillCfg heroSkillCfg = HawkConfigManager.getInstance().getConfigByKey(HeroSkillCfg.class, skillCfg.getSkillGet());
			if (heroSkillCfg != null) {
				LogUtil.logHeroSkillChange(player, 0, 0, heroSkillCfg.getSkillType(), heroSkillCfg.getSkillId(), 1, 0, HeroSkillOperType.RESOLVE);
			}

			// 行为日志
			BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.HERO_SKILL_RESOLVE,
					Params.valueOf("itemId", itemId));
		}
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		consume.consumeAndPush(player, Action.HERO_SKILL_RESOLVE);// 扣除技能道具
		awardItem.rewardTakeAffectAndPush(player, Action.HERO_SKILL_RESOLVE, true);

		player.responseSuccess(protocol.getType());
	}

	/** 万能碎片合成技能/英雄/机甲碎片 /皮肤碎片 */
	@ProtocolHandler(code = HP.code.HERO_EXCHANGE_ITEM_C_VALUE)
	private void onExchangeItem(HawkProtocol protocol) {
		PBExchangeItem req = protocol.parseProtocol(PBExchangeItem.getDefaultInstance());
		int itemId = req.getItemId();
		int toItemCount = req.getItemCount();
		int toItemId = req.getToItemId();
		int heroId = req.getTargetHeroId();

		HawkAssert.checkPositive(toItemCount);

		exchangeItem(protocol.getType(), itemId, toItemCount, toItemId, heroId);
		player.responseSuccess(protocol.getType());

	}

	/** 超级兵兑换道具同步 */
	@ProtocolHandler(code = HP.code.SUPER_SOLDIER_EXCHANGE_SYNC_REQ_VALUE)
	private void onExchangeSuperSoldierItemSync(HawkProtocol protocol) {
		exchangeSuperSoldierItemSync();
	}

	/** 超级兵兑换道具同步 */
	private void exchangeSuperSoldierItemSync() {
		Map<Integer, Integer> countMap = RedisProxy.getInstance().getSupersoldierSkillExpExchangeCount(player.getId());
		HPSupersoldierExchangeItemResp.Builder resp = HPSupersoldierExchangeItemResp.newBuilder();
		for (Entry<Integer, Integer> ent : countMap.entrySet()) {
			HPSupersoldierExchangeItem.Builder bul = HPSupersoldierExchangeItem.newBuilder().setItemId(ent.getKey()).setExchangeCount(ent.getValue());
			resp.addItems(bul);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_SOLDIER_EXCHANGE_SYNC_RESP, resp));
	}

	public void exchangeItem(int protocolType, int itemId, int toItemCount, int toItemId, int heroId) {
		HawkAssert.checkPositive(toItemCount);
		ItemCfg toitemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, toItemId); // 技能碎片道具
		ItemInfo cost = ItemInfo.valueOf(toitemCfg.getExchangeItem());
		if (Objects.isNull(toitemCfg) || cost.getItemId() != itemId) {
			return;
		}
		cost.setCount(cost.getCount() * toItemCount);

		// 如果兑换英雄碎片,需要英雄已解锁
		if (toitemCfg.getItemType() == ToolType.HERO_CHIP_VALUE) {
			PlayerHero targetHero = player.getHeroByCfgId(heroId).orElse(null);
			if (Objects.isNull(targetHero)) {
				return;
			}
			if (targetHero.getStar() < targetHero.getConfig().getExchangePiecesStar()) {
				return;
			}
			ItemInfo unlockItem = ItemInfo.valueOf(targetHero.getConfig().getUnlockPieces());
			if (unlockItem.getItemId() != toItemId) {
				return;
			}
		}
		// 机甲部件升级材料, 一次一个
		if (toitemCfg.getItemType() == ToolType.SUPER_SOLDIER_SKILL_EXP_VALUE) {
			toItemCount = 1;
			int exchangeIndex = 1 + RedisProxy.getInstance().getSupersoldierSkillExpExchangeCount(player.getId(), toItemId);
			SuperSoldierSkillexpExchangeCfg costCfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierSkillexpExchangeCfg.class, exchangeIndex);
			if (costCfg == null) {
				return;
			}
			cost.setCount(costCfg.getCount());
		}

		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(cost, false);
		if (!consume.checkConsume(player, protocolType)) {
			return;
		}
		consume.consumeAndPush(player, Action.HERO_EXCHANGE_ITEM);// 扣除技能道具

		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItem(Const.ItemType.TOOL_VALUE, toItemId, toItemCount);
		awardItem.rewardTakeAffectAndPush(player, Action.HERO_EXCHANGE_ITEM);

		if (toitemCfg.getItemType() == ToolType.SUPER_SOLDIER_SKILL_EXP_VALUE) {
			RedisProxy.getInstance().incrSupersoldierSkillExpExchangeCount(player.getId(), toItemId, 1);
			exchangeSuperSoldierItemSync();
		}

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.HERO_EXCHANGE_ITEM,
				Params.valueOf("itemId", itemId),
				Params.valueOf("toItemCount", toItemCount),
				Params.valueOf("toItemId", toItemId));
	}

	/**
	 * 英雄驻防
	 * 
	 * @param office
	 * @return
	 */
	private boolean cityDef(PBHeroOffice office) {
		Optional<PlayerHero> heroOp = player.getHeroByCfgId(office.getHeroId());
		if (!heroOp.isPresent()) {
			return false;
		}
		PlayerHero hero = heroOp.get();
		hero.cityDef(office.getOffice()); // 只能委任守城

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.HERO_OFFICE_APPOINT,
				Params.valueOf("heroId", hero.getCfgId()),
				Params.valueOf("officeId", office.getOffice()));

		MissionManager.getInstance().postMsg(player, new EventHeroGarrison(hero.getCfgId(), office.getOffice()));
		return true;
	}

	@MessageHandler
	private void onEffectChangeEvent(PlayerEffectChangeMsg event) {
		boolean repush = event.hasEffectChange(EffType.T3_1409)
				|| event.hasEffectChange(EffType.T3_1410)
				|| event.hasEffectChange(EffType.T3_1411)
				|| event.hasEffectChange(EffType.T3_1401)
				|| event.hasEffectChange(EffType.T3_1402)
				|| event.hasEffectChange(EffType.T3_1403);
		if (!repush) {
			return;
		}
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (threadPool != null) {
			HawkTask task = new HawkTask() {
				@Override
				public Object run() {
					player.getAllHero().forEach(PlayerHero::notifyChange);
					return null;
				}
			};
			task.setPriority(1);
			task.setTypeName("onEffectChangeEvent");
			int threadIndex = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
			threadPool.addTask(task, threadIndex, false);
			return;
		} else {
			player.getAllHero().forEach(PlayerHero::notifyChange);
		}
	}

	@MessageHandler
	private void onHeroSkinEndEvent(BuffHeroSkinEndMsg event) {
		int skinId = event.getStatusId();
		int heroId = HeroSkin.getHeroIdBySkinId(skinId);
		PlayerHero hero = player.getHeroByCfgId(heroId).orElse(null);
		if (hero == null) {
			return;
		}
		if (hero.getShowSkin() == skinId) {
			hero.changeSkin(0);
		}
	}

	/** 英雄数据改变事件 */
	@MessageHandler
	private void onHeroItemChangedEvent(HeroItemChangedMsg event) {
		ItemCfg itemCfg = event.getItemCfg();
		ItemInfo itemAdd = event.getItemAdd();
		final int heroCardId = itemCfg.getId();
		HeroCfg heroCfg = HawkConfigManager.getInstance().getConfigIterator(HeroCfg.class).stream()
				.filter(cfg -> heroCardId == cfg.getItemUnlockCard().getItemId())
				.findAny()
				.get();
		final int heroId = heroCfg.getHeroId();
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(itemAdd, false);
		if (!consumeItems.checkConsume(player)) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.HERO_ITEM_RESLOVE);// 获得整卡直接消耗掉.
																		// 剩余转化为碎片

		if (!player.getHeroByCfgId(heroId).isPresent()) {
			this.unLockHero(heroId);
			itemAdd.setCount(itemAdd.getCount() - 1);

		}
		if (itemAdd.getCount() > 0) {
			// 有此英雄转换成碎片
			ItemInfo convertItem = new ItemInfo(itemCfg.getResolveList());
			convertItem.setCount(convertItem.getCount() * itemAdd.getCount());
			AwardItems awardItem = AwardItems.valueOf();
			awardItem.addItem(convertItem);
			awardItem.rewardTakeAffectAndPush(player, Action.HERO_ITEM_RESLOVE);
		}
	}

	/** 拆除建筑 */
	@MessageHandler
	private void onBuildingRemoveMsg(BuildingRemoveMsg event) {
		BuildingBaseEntity build = event.getBuildingEntity();
		List<HeroOfficeCfg> offList = HawkConfigManager.getInstance().getConfigIterator(HeroOfficeCfg.class).stream()
				.filter(cfg -> cfg.getUnlockBuildingType() == build.getType())
				.collect(Collectors.toList());

		for (HeroOfficeCfg cfg : offList) {
			if (!cfg.checkOfficeBuilding(player.getData().getBuildingEntities())) {
				PlayerHero officeHero = getHeroByOfficeId(cfg.getId());
				if (Objects.nonNull(officeHero)) {
					officeHero.officeAppoint(0);
				}
			}
		}

	}

	/**
	 * 释放英雄技能
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.CAST_HERO_SKILL_C_VALUE)
	private void onCastSkill(HawkProtocol protocol) {
		PBCastHeroSkillRequest req = protocol.parseProtocol(PBCastHeroSkillRequest.getDefaultInstance());
		final int heroId = req.getHeroId();
		final int skillId = req.getSkillId();
		Optional<PlayerHero> heroOP = player.getHeroByCfgId(heroId);
		if (!heroOP.isPresent()) {
			return;
		}
		PlayerHero hero = heroOP.get();
		hero.castSkill(skillId);
		player.responseSuccess(protocol.getType());

		LogUtil.logHeroAttrChange(player, Action.HERO_SKILL_CAST, 0, hero);
		// 行为日志
		DungeonRedisLog.log(player.getId(), "heroId {} skillId {}",heroId, skillId);
	}

	/** 解锁英雄 */
	@ProtocolHandler(code = HP.code.HERO_UNLOCK_C_VALUE)
	private void onUnlockHero(HawkProtocol protocol) {
		PBUnlockHeroRequest req = protocol.parseProtocol(PBUnlockHeroRequest.getDefaultInstance());
		final int heroId = req.getHeroId();
		if (player.getHeroByCfgId(heroId).isPresent()) {// 已解锁
			return;
		}
		HeroCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HeroCfg.class, heroId);
		if (cfg == null) {
			return;
		}

		ItemInfo costItem = new ItemInfo(cfg.getUnlockPieces());
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(costItem, false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		PlayerHero hero = this.unLockHero(heroId);
		if (Objects.nonNull(hero)) {
			consumeItems.consumeAndPush(player, Action.HERO_UNLOCK);
		}

		player.responseSuccess(protocol.getType());

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.HERO_UNLOCK,
				Params.valueOf("heroId", heroId));

	}

	/** 出征英雄异常检测及修复 */
	private void herosCheckAndFix() {
		if (player.isInDungeonMap()) {
			return;
		}
		// 出征中的英雄列表
		List<Integer> marchHeros = new ArrayList<>();

		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		for (IWorldMarch march : marchs) {
			marchHeros.addAll(march.getMarchEntity().getHeroIdList());
		}

		for (PlayerHero hero : player.getAllHero()) {
			// 英雄状态为出征中 && 出征英雄列表没有此英雄
			int heroId = hero.getCfgId();
			if (hero.getState() == PBHeroState.HERO_STATE_MARCH && !marchHeros.contains(heroId)) {
				hero.backFromMarch(null);
			}
			if (hero.getState() == PBHeroState.HERO_STATE_FREE && marchHeros.contains(heroId)) {
				hero.goMarch(null);
			}
		}
	}

	public PlayerHero unLockHero(int heroId) {
		HeroCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HeroCfg.class, heroId);
		if (cfg == null) {
			return null;
		}
		HeroLevelCfg iniCfg = HawkConfigManager.getInstance().getCombineConfig(HeroLevelCfg.class, cfg.getIniLevel() - 1, cfg.getQualityColor());
		int initXp = Objects.isNull(iniCfg) ? 0 : iniCfg.getLevelUpExp();

		HeroEntity newHero = new HeroEntity();
		newHero.setHeroId(heroId);
		newHero.setPlayerId(player.getId());
		newHero.setStar(cfg.getIniStar());
		newHero.setExp(initXp);
		newHero.setState(PBHeroState.HERO_STATE_FREE_VALUE);

		PlayerHero hero = PlayerHero.create(newHero);
		HawkDBManager.getInstance().create(newHero);

		// 抛出活动事件
		ActivityManager.getInstance().postEvent(new HeroChangeEvent(player.getId()));
		ActivityManager.getInstance().postEvent(new HeroUnlockEvent(player.getId(), cfg.getQualityColor()));
		ActivityManager.getInstance().postEvent(new HeroLevelUpEvent(player.getId(), heroId, hero.getLevel()));
		ActivityManager.getInstance().postEvent(new HeroUpStarEvent(player.getId(), hero.getCfgId(), 0, hero.getStar()));

		MissionManager.getInstance().postMsg(player, new EventHeroUpgrade(heroId, 0, 1));
		MissionManager.getInstance().postMsg(player, new EventHeroStarUp(heroId, 0, 1));
		MissionManager.getInstance().postMsg(player, new EventHeroChange());
		GsApp.getInstance().postMsg(player, new UnlockHeroMsg(heroId, cfg.getQualityColor()));

		// 我要变强
		StrengthenGuideManager.getInstance().postMsg(new SGPlayerHeroUnlockMsg(player));

		player.getData().getHeroEntityList().add(newHero);

		hero.notifyChange();
		LogUtil.logHeroAttrChange(player, Action.HERO_UNLOCK, 0, hero);
		return hero;

	}

	@MessageHandler
	private void onUnlockHeroMsg(UnlockHeroMsg msg) {
		// 已拥有的其他英雄检查一遍羁绊
		int allNum = checkHeroCollect(msg.getHeroId());
		// 打点羁绊记录日志
		LogUtil.logHeroCollect(player, allNum);
		//SSS跑马灯
		if(msg.getQualityColor() > HeroQualityColorType.Orange.getNumber()){
			Const.NoticeCfgId noticeId = Const.NoticeCfgId.PLAYER_UNLOCK_HERO_NOTICE;
			ChatParames chatParams = ChatParames.newBuilder()
					.setChatType(Const.ChatType.SPECIAL_BROADCAST)
					.setKey(noticeId)
					.addParms(player.getName())
					.addParms(msg.getHeroId())
					.build();
			ChatService.getInstance().addWorldBroadcastMsg(chatParams);
		}
	}

	private int checkHeroCollect(int heroId) {
		int allNum = 0;
		for (PlayerHero playerHero : player.getAllHero()) {
			if (playerHero.getHeroCollect().isActive() && playerHero.getHeroCollect().checkHasNotify(heroId)) {
				playerHero.notifyChange();
				allNum += playerHero.getHeroCollect().getCollectNum();
			}
		}
		return allNum;
	}

	@MessageHandler
	private void onStarupHeroMsg(HeroStarUpMsg msg) {
		this.checkHeroCollect(msg.getHeroId());
	}

	@MessageHandler
	private void onlevelupHeroMsg(HeroLevelUpMsg msg) {
		this.checkHeroCollect(msg.getHeroId());
	}

	/** 解锁英雄机器人专用 */
	@ProtocolHandler(code = HP.code.UNLOCK_ROBOT_HERO_VALUE)
	private void onUnlockRobotHero(HawkProtocol protocol) {
		if (!player.isRobot()) {
			return;
		}

		PBUnlockHeroRequest req = protocol.parseProtocol(PBUnlockHeroRequest.getDefaultInstance());
		final int heroId = req.getHeroId();
		if (player.getHeroByCfgId(heroId).isPresent()) {// 已解锁
			return;
		}

		PlayerHero hero = this.unLockHero(heroId);
		if (Objects.nonNull(hero)) {
		} else {
			HawkLog.errPrintln("unlock roobt hero fail heroId = {}", heroId);
			return;
		}

		AwardItems awardItem = AwardItems.valueOf();
		// 给经验道具
		ItemInfo exp = ItemInfo.valueOf("30000_1510005_1000");
		awardItem.addItem(exp);
		// 给升星道具
		HeroStarLevelCfg starLevelCfg = HawkConfigManager.getInstance().getCombineConfig(HeroStarLevelCfg.class, heroId, 1, 1);
		ItemInfo piceItem = ItemInfo.valueOf(starLevelCfg.getPiecesForNextLevel());
		piceItem.setCount(999);
		awardItem.addItem(piceItem);

		awardItem.rewardTakeAffectAndPush(player, Action.GM_AWARD);

		player.responseSuccess(protocol.getType());

		// 行为日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.HERO_UNLOCK,
				Params.valueOf("heroId", heroId));

	}

	/**
	 * 解锁英雄档案
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.HERO_ARCHIVES_UNLOCK_REQ_VALUE)
	private void unlockPlayerArchive(HawkProtocol protocol) {
		PBHeroArchiveReq req = protocol.parseProtocol(PBHeroArchiveReq.getDefaultInstance());
		int chapterId = req.getChapterId();
		int heroId = req.getHeroId();
		
		// 大本等级不足
		if (player.getCityLevel() < ConstProperty.getInstance().getHeroArchivesOpenLv()) {
			return;
		}
		
		// 英雄不存在
		Optional<PlayerHero> heroOp = player.getHeroByCfgId(heroId);
		if (!heroOp.isPresent()) {
			return;
		}
		
		// 已经解锁
		HeroArchivesEntity entity = player.getData().getHeroArchivesEntity();
		if (entity.getArchiveLevel(heroId) > 0) {
			return;
		}
		
		// 档案不存在
		HeroArchivesChapterCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HeroArchivesChapterCfg.class, chapterId);
		if (cfg == null || !cfg.hasHero(heroId)) {
			return;
		}
		
		// 解锁消耗
		ConsumeItems cost = ConsumeItems.valueOf();
		int beforeUnlockCount = entity.getUnlockAchives().size();
		List<ItemInfo> unlockCost = getUnlockCost(beforeUnlockCount);
		cost.addConsumeInfo(unlockCost);;
		
		// 检测消耗
		if (!cost.checkConsume(player, protocol.getType())) {
			return;
		}
		cost.consumeAndPush(player, Action.HERO_ARCHIVE_UNLOCK);
		
		// 更新英雄档案
		entity.updateArchive(heroId);
		
		// 成功协议返回
		syncHeroArchivesInfo();
		player.responseSuccess(protocol.getType());
		
		// 推送英雄档案作用号
		pushHeroArchivesEff(heroId);
		
		LogUtil.logHeroAttrChange(player, Action.HERO_ARCHIVE_UNLOCK, 0, heroOp.get());
	}
	
	/**
	 * 获取解锁消耗
	 * @param 解锁第几个英雄
	 * @return
	 */
	public List<ItemInfo> getUnlockCost(int beforeUnlockCount) {
		HeroArchivesConstCfg cfg = HeroArchivesConstCfg.getInstance();
		
		// 解锁一个的递增
		int addOnce = cfg.getArchivesAccumulationCost();
		// 最大消耗数量
		int max = cfg.getMaxCost();
		
		List<ItemInfo> items = cfg.getArchivesFirstHeroCost();
		for (ItemInfo i : items) {
			long beforeCnt = i.getCount();
			long afterCnt = beforeCnt + addOnce * beforeUnlockCount;
			afterCnt = Math.min(afterCnt, max);
			i.setCount(afterCnt);
		}
		return items;
	}
	
	/**
	 * 升级英雄档案
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.HERO_ARCHIVES_UPLEVEL_REQ_VALUE)
	private void onHeroArchiveLevelUp(HawkProtocol protocol) {
		// 大本等级不足
		if (player.getCityLevel() < ConstProperty.getInstance().getHeroArchivesOpenLv()) {
			return;
		}
		
		PBHeroArchiveReq req = protocol.parseProtocol(PBHeroArchiveReq.getDefaultInstance());
		int heroId = req.getHeroId();
		heroArchiveLevelUp(protocol, heroId);
	}

	private void heroArchiveLevelUp(HawkProtocol protocol, int heroId) {
		// 英雄不存在
		Optional<PlayerHero> heroOp = player.getHeroByCfgId(heroId);
		if (!heroOp.isPresent()) {
			return;
		}

		// 目标等级错误(策划说写死)
		HeroArchivesEntity entity = player.getData().getHeroArchivesEntity();
		int afterLevel = entity.getArchiveLevel(heroId) + 1;
		if (afterLevel <= 1 || afterLevel > 5) {
			return;
		}
		
		// 档案不存在
		HeroArchivesContentCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HeroArchivesContentCfg.class, heroId);
		if (cfg == null) {
			return;
		}
		
		// 词条3：需要对应英雄主要天赋满才能解锁(策划说写死)
		if (afterLevel == 3) {
			Optional<TalentSlot> talentSlotOp = heroOp.get().getTalentSlotByIndex(0);
			if (!talentSlotOp.isPresent()) {
				return;
			}
			TalentSlot talentSlot = talentSlotOp.get();
			if (!talentSlot.isUnLock() || !talentSlot.getTalent().isExpMax()) {
				return;
			}
		}
		
		// 词条5：需要对应英雄全部天赋满才能解锁(策划说写死)
		if (afterLevel == 5) {
			ImmutableList<TalentSlot> talentSlots = heroOp.get().getTalentSlots();
			for (TalentSlot talentSlot : talentSlots) {
				if (!talentSlot.isUnLock() || !talentSlot.getTalent().isExpMax()) {
					return;
				}
			}
		}
		
		// 检测消耗
		ConsumeItems cost = ConsumeItems.valueOf();
		cost.addConsumeInfo(cfg.getCost(afterLevel));;
		if (!cost.checkConsume(player, protocol.getType())) {
			return;
		}
		cost.consumeAndPush(player, Action.HERO_ARCHIVE_UPLEVEL);
		
		// 更新英雄档案
		entity.updateArchive(heroId);
		
		// 成功协议返回
		syncHeroArchivesInfo();
		player.responseSuccess(protocol.getType());
		
		// 推送英雄档案作用号
		pushHeroArchivesEff(heroId);
		LogUtil.logHeroAttrChange(player, Action.HERO_ARCHIVE_UPLEVEL, 0, heroOp.get());
	}
	
	/**
	 * 推送英雄档案作用号
	 * @param heroId
	 */
	private void pushHeroArchivesEff(int heroId) {
		HeroArchivesContentCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HeroArchivesContentCfg.class, heroId);
		Set<EffType> effTypeSet = cfg.getEffTypeSet();
		EffType[] effTypeArray = effTypeSet.toArray(new EffType[effTypeSet.size()]);
		player.getPush().syncPlayerEffect(effTypeArray);
	}
	
	/**
	 * 推送英雄档案信息
	 */
	public void syncHeroArchivesInfo() {
		PBHeroArchivePush.Builder push = PBHeroArchivePush.newBuilder();
		HeroArchivesEntity entity = player.getData().getHeroArchivesEntity();
		for (Entry<Integer, Integer> info : entity.getArchiveInfo().entrySet()) {
			PBHeroArchiveInfo.Builder builder = PBHeroArchiveInfo.newBuilder();
			builder.setHeroId(info.getKey());
			builder.setLevel(info.getValue());
			push.addInfo(builder);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.HERO_ARCHIVES_INFO_PUSH, push));		
	}
	
	/**
	 * 英雄档案碎片兑换
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.HERO_ARCHIVES_EXCHANGE_REQ_VALUE)
	private void onHeroArchiveExchange(HawkProtocol protocol) {
		PBHeroArchiveExchange req = protocol.parseProtocol(PBHeroArchiveExchange.getDefaultInstance());
		
		// 检测下发上来的数量
		int itemCount = req.getItemCount();
		HawkAssert.checkNonNegative(itemCount);
		
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, req.getItemId());
		ItemInfo toItem = itemCfg.getHeroArchivesExchangeItem(req.getToItemId());
		if (toItem == null) {
			return;
		}
		
		// 消耗
		ConsumeItems consume = ConsumeItems.valueOf();
		ItemInfo cost = new ItemInfo(ItemType.TOOL_VALUE, req.getItemId(), itemCount);
		consume.addConsumeInfo(cost, false);
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		consume.consumeAndPush(player, Action.HERO_ARCHIVES_EXCHANGE);
		
		// 发碎片
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItem(Const.ItemType.TOOL_VALUE, toItem.getItemId(), toItem.getCount() * itemCount);
		awardItem.rewardTakeAffectAndPush(player, Action.HERO_ARCHIVES_EXCHANGE);

		// 直接升级
		if (req.hasLevelUpHeroId()) {
			heroArchiveLevelUp(protocol, req.getLevelUpHeroId());
		}
		player.responseSuccess(protocol.getType());
	}
	
	/**
	 * 英雄档案碎片反向兑换
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.HERO_ARCHIVES_FULL_EXCHANGE_REQ_VALUE)
	private void onHeroArchiveFullExchange(HawkProtocol protocol) {
		PBHeroArchiveExchange req = protocol.parseProtocol(PBHeroArchiveExchange.getDefaultInstance());
		
		// 检测下发上来的数量
		int itemCount = req.getItemCount();
		HawkAssert.checkNonNegative(itemCount);
		
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, req.getItemId());
		ItemInfo toItem = itemCfg.getHeroArchivesFullExchange();
		if (toItem == null) {
			return;
		}
		
		// 消耗
		ConsumeItems consume = ConsumeItems.valueOf();
		ItemInfo cost = new ItemInfo(ItemType.TOOL_VALUE, req.getItemId(), itemCount);
		consume.addConsumeInfo(cost, false);
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		consume.consumeAndPush(player, Action.HERO_ARCHIVES_EXCHANGE);
		
		// 发碎片
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItem(Const.ItemType.TOOL_VALUE, toItem.getItemId(), toItem.getCount() * itemCount);
		awardItem.rewardTakeAffectAndPush(player, Action.HERO_ARCHIVES_EXCHANGE);
		player.responseSuccess(protocol.getType());
	}
	
	/**
	 * 检测英雄档案开放奖励
	 */
	public void checkHeroArchiveOpenAward() {
		if (player.getCityLevel() < ConstProperty.getInstance().getHeroArchivesOpenLv()) {
			return;
		}
		// 已经发过奖励了
		String heroArchiveOpenAward = RedisProxy.getInstance().getHeroArchiveOpenAward(player.getId());
		if (!HawkOSOperator.isEmptyString(heroArchiveOpenAward)) {
			return;
		}
		
		RedisProxy.getInstance().updateHeroArchiveOpenAward(player.getId());
		
		// 发奖
		List<ItemInfo> item = ItemInfo.valueListOf(ConstProperty.getInstance().getHeroArchivesOpenAward());
		AwardItems award = AwardItems.valueOf();
		award.addItemInfos(item);
		award.rewardTakeAffect(player, Action.HERO_ARCHIVES_OPEN_AWARD);
	}
}
