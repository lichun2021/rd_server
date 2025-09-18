package com.hawk.game.module.toucai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.MedalFactoryCaijiEvent;
import com.hawk.activity.event.impl.MedalFactoryShouEvent;
import com.hawk.activity.event.impl.MedalFactoryTouEvent;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.PlayerMarchModule;
import com.hawk.game.module.toucai.cfg.MedalFactoryConstCfg;
import com.hawk.game.module.toucai.cfg.MedalFactoryLevelCfg;
import com.hawk.game.module.toucai.cfg.MedalFactoryProductCfg;
import com.hawk.game.module.toucai.cfg.MedalFactoryRewardCfg;
import com.hawk.game.module.toucai.entity.MedalCollect;
import com.hawk.game.module.toucai.entity.MedalEntity;
import com.hawk.game.module.toucai.entity.MedalFactoryObj;
import com.hawk.game.module.toucai.entity.MedalSteal;
import com.hawk.game.module.toucai.entity.MedalStealed;
import com.hawk.game.msg.BuildingLevelUpMsg;
import com.hawk.game.msg.BuyMonthCardMsg;
import com.hawk.game.msg.PlayerEffectChangeMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.MedalFactory.HPLeyuzhurenReq;
import com.hawk.game.protocol.MedalFactory.HPMedalFactoryCaiJiMarchReq;
import com.hawk.game.protocol.MedalFactory.HPMedalFactoryCaiJiMarchResp;
import com.hawk.game.protocol.MedalFactory.HPMedalFactoryCollectReq;
import com.hawk.game.protocol.MedalFactory.HPMedalFactoryHistory;
import com.hawk.game.protocol.MedalFactory.HPMedalFactoryInfo;
import com.hawk.game.protocol.MedalFactory.HPMedalFactoryPreMarchResp;
import com.hawk.game.protocol.MedalFactory.HPMedalFactoryQuganReq;
import com.hawk.game.protocol.MedalFactory.HPMedalFactoryQuganResp;
import com.hawk.game.protocol.MedalFactory.HPMedalFactoryReq;
import com.hawk.game.protocol.MedalFactory.HPMedalFactoryShouReq;
import com.hawk.game.protocol.MedalFactory.HPMedalFactoryTouReq;
import com.hawk.game.protocol.MedalFactory.HPMedalHistroyResp;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.PresetMarchInfo;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.PushService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ModuleType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.log.Action;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

public class PlayerMedalFactoryModule extends PlayerModule {
	final String MedalPreMarch = "medalfctoryffdf:";
	final int CAIJI = 16;
	final int QUGAN = 17;

	final String HistoryKey = "medalfff:";
	final int HistoryExpire = 72 * 3600;
	private long lastCheckTime = 0;

	public PlayerMedalFactoryModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		if (!player.isCsPlayer()) {
			getFactory().sync();
		}
		return super.onPlayerLogin();
	}

	@Override
	public boolean onTick() {
		if (player.isCsPlayer()) {
			return true;
		}
		long currentTime = HawkApp.getInstance().getCurrentTime();
		if (currentTime - lastCheckTime > 1_000) {
			lastCheckTime = currentTime;
			MedalEntity dbEntity = getFactory().getDbEntity();
			if (dbEntity.getLastRefreshDay() != HawkTime.getYearDay()) {
				dbEntity.setLastRefreshDay(HawkTime.getYearDay());
				dbEntity.setDailyRefresh(0);
				dbEntity.setStealTodayStr("");
				getFactory().notifyChange();
			}
			// 偷取中处理
			checkSteal();
			logStealed();
			if (getFactory().isNeedSync()) {
				getFactory().notifyChange();
				getFactory().sync();
			}
		}
		return super.onTick();
	}

	private void checkSteal() {

		List<MedalSteal> steals = getFactory().getSteals();
		Iterator<MedalSteal> it = steals.iterator();
		while (it.hasNext()) {
			MedalSteal ms = it.next();
			if (ms.getEnd() < HawkTime.getMillisecond()) { // 偷取成功
				it.remove();
				List<ItemInfo> rewards = null;
				if (StringUtils.isNotEmpty(ms.getStealed())) {
					rewards = ItemInfo.valueListOf(ms.getStealed());
				} else {
					MedalFactoryRewardCfg reward = HawkConfigManager.getInstance().getConfigByKey(MedalFactoryRewardCfg.class, ms.getRewardCfgId());
					rewards = ItemInfo.valueListOf(reward.getStealReward());
				}
				for (ItemInfo award : rewards) {
					award.setCount((long) (award.getCount() * (10000 + player.getEffect().getEffVal(EffType.MEDAL_653)) * GsConst.EFF_PER));
				}

				{// 3. 当玩家偷取别人的产物时，记录：时间、偷取玩家、偷取内容
					saveRecord(2, ms.getTargetId(), ItemInfo.toString(rewards), player.getId(), ms.getEnd());
				}

				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.addContents(ms.getTarName())
						.setPlayerId(player.getId())
						.setMailId(MailId.ATTACK_MEDALF_STEAL)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.setRewards(rewards)
						.build());
				if (HawkTime.isToday(ms.getEnd())) {
					AwardItems awardI = AwardItems.valueOf();
					awardI.addItemInfos(rewards);
					MedalEntity dbEntity = getFactory().getDbEntity();
					if (StringUtils.isNotEmpty(dbEntity.getStealTodayStr())) {
						awardI.addItemInfos(ItemInfo.valueListOf(dbEntity.getStealTodayStr()));
					}
					dbEntity.setStealTodayStr(ItemInfo.toString(awardI.getAwardItems()));
				}
				getFactory().notifyChange();
			}
		}
	}

	private void logStealed() {
		boolean change = false;
		long now = HawkTime.getMillisecond();
		for (MedalCollect collect : getFactory().getCollects()) {
			for (MedalStealed steal : collect.getStealed()) {
				if (steal.getLog() == 0 && steal.getEnd() <= now) {
					steal.setLog(1);
					
					String stealReward = "";
					if (StringUtils.isNotEmpty(steal.getStealed())) {
						stealReward = steal.getStealed();
					} else {
						MedalFactoryRewardCfg reward = HawkConfigManager.getInstance().getConfigByKey(MedalFactoryRewardCfg.class, steal.getRewardCfgId());
						stealReward = reward.getStealReward();
					}
					saveRecord(3, steal.getPlayerId(), stealReward, player.getId(), now);
					// 仇人
					getFactory().addEnemy(steal.getPlayerId());
					change = true;
				}
			}
		}
		if (change) {
			getFactory().notifyChange();
		}
	}

	private MedalFactoryObj getFactory() {
		return player.getData().getMedalEntity().getFactoryObj();
	}

	/** 每日奖励领取*/
	@ProtocolHandler(code = HP.code2.TOUCAI_LEYUZHUREN_REQ_VALUE)
	private void onLeyuzhuren(HawkProtocol protocol) {
		HPLeyuzhurenReq req = protocol.parseProtocol(HPLeyuzhurenReq.getDefaultInstance());
		MedalFactoryObj factoryObj = getFactory();
		factoryObj.getDbEntity().setLeyuzhuren(req.getOpen() ? 1 : 0);
		factoryObj.notifyChange();
		factoryObj.sync();
		DungeonRedisLog.log(player.getId(), "onLeyuzhuren {}", req.getOpen());
		player.responseSuccess(protocol.getType());
	}

	/**
	 * 刷新
	 */
	@ProtocolHandler(code = HP.code2.TOUCAI_TANXUN_REFRESH_C_VALUE)
	private void onShuaXin(HawkProtocol protocol) {
		if (player.isCsPlayer()) {
			return;
		}
		MedalFactoryConstCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(MedalFactoryConstCfg.class);
		MedalEntity dbEntity = getFactory().getDbEntity();
		if (dbEntity.getRefreshCool() > HawkTime.getMillisecond() || kvCfg.getRefreshNum() - dbEntity.getDailyRefresh() <= 0) {
			return;
		}
		refashTanXun();

		dbEntity.setRefreshCool(HawkTime.getMillisecond() + kvCfg.getRefreshTime() * 1000);
		dbEntity.setDailyRefresh(dbEntity.getDailyRefresh() + 1);

		getFactory().getRefresh().sync(player);
		player.responseSuccess(protocol.getType());
	}

	// 刷新探寻
	private void refashTanXun() {
		// 当前正在偷的
		List<String> stealIds = getFactory().getSteals().stream().map(st -> st.getTargetId()).collect(Collectors.toList());
		List<String> canStealList = MedalFactoryService.getInstance().canStealList();
		canStealList.remove(player.getId());
		Collections.shuffle(canStealList);
		canStealList.removeAll(stealIds);

		// 好友
		List<String> friends = new ArrayList<>(CollectionUtils.intersection(getFactory().getRefresh().getFriend(), stealIds));// 正在偷的不刷掉
		List<String> relationFriendList = RelationService.getInstance().getPlayerRelationList(player.getId(), GsConst.RelationType.FRIEND).stream().map(e -> e.getTargetPlayerId())
				.collect(Collectors.toList());
		for (String pid : relationFriendList) {
			if (friends.size() >= 15) {
				break;
			}
			if (canStealList.contains(pid)) {
				friends.add(pid);
				canStealList.remove(pid);
			}
		}
		getFactory().getRefresh().setFriend(friends);

		// 敌人
		List<String> enemys = new ArrayList<>(CollectionUtils.intersection(getFactory().getRefresh().getEnemy(), stealIds));// 正在偷的不刷掉
		for (String pid : getFactory().getEnemys()) {
			if (enemys.size() >= 5) {
				break;
			}
			if (canStealList.contains(pid)) {
				enemys.add(pid);
				canStealList.remove(pid);
			}
		}
		getFactory().getRefresh().setEnemy(enemys);

		// 随即的
		List<String> rands = new ArrayList<>(CollectionUtils.intersection(getFactory().getRefresh().getRand(), stealIds));// 正在偷的不刷掉
		for (String pid : canStealList) {
			if (rands.size() >= 3) {
				break;
			}
			rands.add(pid);
		}
		getFactory().getRefresh().setRand(rands);

		getFactory().notifyChange();
	}

	/**
	 * 探寻更表
	 */
	@ProtocolHandler(code = HP.code2.TOUCAI_TANXUN_C_VALUE)
	private void onTanxun(HawkProtocol protocol) {
		if (player.isCsPlayer()) {
			return;
		}

		if (getFactory().getRefresh().isEmpty()) {
			refashTanXun();
		}

		getFactory().getRefresh().sync(player);
	}

	/**
	 * 开始采集
	 */
	@ProtocolHandler(code = HP.code2.TOUCAI_COLLECT_REQ_VALUE)
	private void onCaiji(HawkProtocol protocol) {
		HPMedalFactoryCollectReq req = protocol.parseProtocol(HPMedalFactoryCollectReq.getDefaultInstance());
		final int index = req.getIndex();
		final int productCfgId = req.getProductCfgId();
		MedalCollect collect = getFactory().getCollect(index);
		if (collect == null) {
			return;
		}
		if (!collect.isUnlock()) {
			if (collect.getIndex() == 3) {
				sendError(protocol.getType(), Status.Error.MEFAL_NOT_UNLOCK_VALUE);
			}
			return;
		}

		// 有采集中, 或未收取
		if (collect.getRewardCfgId() > 0) {
			return;
		}

		MedalFactoryProductCfg productCfg = HawkConfigManager.getInstance().getConfigByKey(MedalFactoryProductCfg.class, productCfgId);

		ItemInfo costItem = new ItemInfo(productCfg.getUesItem());
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(costItem, false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.MEDAL_COLLECT);

		ConfigIterator<MedalFactoryRewardCfg> rit = HawkConfigManager.getInstance().getConfigIterator(MedalFactoryRewardCfg.class);

		List<MedalFactoryRewardCfg> rcfgList = rit.stream().filter(rcfg -> rcfg.getLibrary() == productCfg.getLibrary()).collect(Collectors.toList());
		MedalFactoryRewardCfg reward = HawkRand.randomWeightObject(rcfgList);

		collect.setRewardCfgId(reward.getId());
		collect.setStart(HawkTime.getMillisecond());
		double speedup = player.getEffect().getEffVal(EffType.MEDAL_648) * GsConst.EFF_PER;
		int productTime = (int) (productCfg.getProductTime() * 1000 / (1 + speedup));
		collect.setEnd(collect.getStart() + productTime);
		collect.setProductCfgId(productCfgId);
		collect.getMedalFactory().notifyChange();

		MedalEntity dbEntity = collect.getMedalFactory().getDbEntity();
		dbEntity.setExp(dbEntity.getExp() + productCfg.getGetExp());
		collect.getMedalFactory().sync();
		player.responseSuccess(protocol.getType());

		ActivityManager.getInstance().postEvent(new MedalFactoryCaijiEvent(player.getId()));
	}

	/**
	 * 成熟收取
	 */
	@ProtocolHandler(code = HP.code2.TOUCAI_SHOU_REQ_VALUE)
	private void onShouqu(HawkProtocol protocol) {
		HPMedalFactoryShouReq req = protocol.parseProtocol(HPMedalFactoryShouReq.getDefaultInstance());
		final int index = req.getIndex();

		shouqu(protocol.getType(), index);
	}

	private void shouqu(int type, final int index) {
		MedalCollect collect = getFactory().getCollect(index);
		long now = HawkTime.getMillisecond();
		if (collect.getRewardCfgId() == 0 || collect.getEnd() > now) {
			return;
		}
		MedalFactoryRewardCfg reward = HawkConfigManager.getInstance().getConfigByKey(MedalFactoryRewardCfg.class, collect.getRewardCfgId());

		List<ItemInfo> gets = ItemInfo.valueListOf(reward.getReward()); // 原使
		boolean collectAll = collect.getIndex() == 3 || player.getEffect().getEffVal(EffType.MEDAL_655) > 0; // 收全量
		boolean stealAll = false;
		for (MedalStealed stealed : collect.getStealed()) {// 被偷菜
			if (!collectAll) {
				List<ItemInfo> stealList = ItemInfo.valueListOf(reward.getStealReward());
				for (ItemInfo steal : stealList) {
					for (ItemInfo award : gets) {
						if (award.getItemId() == steal.getItemId()) {
							award.setCount(award.getCount() - steal.getCount());
						}
					}
				}
			}
			if (stealed.getEnd() > now) {// 未驱赶的直接完成
				stealed.setEnd(now);
				Player tar = GlobalData.getInstance().makesurePlayer(stealed.getPlayerId());
				for (MedalSteal steal : tar.getData().getMedalEntity().getFactoryObj().getSteals()) {
					if (Objects.equals(steal.getTargetId(), player.getId())) {
						steal.setEnd(now);
					}
				}
			}
			if(stealed.isStealAll()){
				stealAll = true;
			}
		}
		logStealed();
		
		if (!stealAll || collectAll) {

			for (ItemInfo award : gets) {
				award.setCount((long) (award.getCount() * GsConst.EFF_PER * (10000 + player.getEffect().getEffVal(EffType.MEDAL_650))));
			}

			AwardItems awardItem = AwardItems.valueOf();
			awardItem.addItemInfos(gets);
			awardItem.rewardTakeAffectAndPush(player, Action.MEDAL_SHOU, true);
			saveRecord(1, player.getId(), ItemInfo.toString(gets), player.getId(), now);
		}

		collect.setRewardCfgId(0);
		collect.setStart(0);
		collect.setEnd(0);
		collect.getStealed().clear();
		collect.getMedalFactory().notifyChange();
		collect.getMedalFactory().sync();
		player.responseSuccess(type);


		ActivityManager.getInstance().postEvent(new MedalFactoryShouEvent(player.getId()));
	}

	/**
	 * 偷
	 */
	@ProtocolHandler(code = HP.code2.TOUCAI_TOU_REQ_VALUE)
	private void onTouqu(HawkProtocol protocol) {
		
		HPMedalFactoryTouReq req = protocol.parseProtocol(HPMedalFactoryTouReq.getDefaultInstance());
		final String tarId = req.getTarId();
		Player tarPlayer = GlobalData.getInstance().makesurePlayer(tarId);
		if(tarPlayer.isInDungeonMap() || CrossService.getInstance().isCrossPlayer(tarPlayer.getId())){
			sendError(protocol.getType(), Status.Error.PLAYER_IN_INSTANCE_VALUE);
			return;
		}
		// 目标工厂
		MedalFactoryObj tarFactoryObj = tarPlayer.getData().getMedalEntity().getFactoryObj();
		if (tarFactoryObj.getDbEntity().getLeyuzhuren() > 0 && getFactory().getRefresh().getFriend().contains(tarId)) {
			leyuzhurenTouqu(protocol);
		} else {
			touqu(protocol);
		}

	}
	
	private void leyuzhurenTouqu(HawkProtocol protocol) {
		Optional<PresetMarchInfo> preSetInfo = getPresetInfo(player.getId(), CAIJI);
		if (!preSetInfo.isPresent()) {
			sendError(protocol.getType(), Status.Error.MEFAL_NO_16_VALUE);
			return;
		}
		HPMedalFactoryTouReq req = protocol.parseProtocol(HPMedalFactoryTouReq.getDefaultInstance());
		final int index = req.getIndex();
		final String tarId = req.getTarId();
		Player tarPlayer = GlobalData.getInstance().makesurePlayer(tarId);

		MedalFactoryConstCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(MedalFactoryConstCfg.class);
		Map<Integer, Long> todaySteal = getFactory().getStealCnt();
		// 目标工厂
		MedalCollect tarCollect = tarPlayer.getData().getMedalEntity().getFactoryObj().getCollect(index);
		long now = HawkTime.getMillisecond();
		if (tarCollect.getRewardCfgId() == 0 || tarCollect.getEnd() > now) {
			sendError(protocol.getType(), Status.Error.MEFAL_TAR_CHANGHE_VALUE);
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.TOUCAI_TAR_FACTORY_S, tarCollect.getMedalFactory().toHP()));
			return;
		}
		for (MedalStealed stealed : tarCollect.getStealed()) {
			if (stealed.getPlayerId().equals(player.getId())) { // 有正在被偷取
				sendError(protocol.getType(), Status.Error.MEFAL_STEALED_HAS_VALUE);
				return;
			}
			if (stealed.getEnd() > now) { // 有正在被偷取
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.TOUCAI_TAR_FACTORY_S, tarCollect.getMedalFactory().toHP()));
				sendError(protocol.getType(), Status.Error.MEFAL_STEAL_ING_VALUE);
				return;
			}
		}

		boolean selfStealing = tarCollect.getMedalFactory().getCollects().stream().flatMap(fa -> fa.getStealed().stream()).filter(s -> s.getEnd() > now)
				.filter(s -> s.getPlayerId().equals(player.getId())).findAny().isPresent();
		if (selfStealing) {
			sendError(protocol.getType(), Status.Error.MEFAL_STEALED_ING_VALUE);
			return;
		}

		MedalFactoryRewardCfg reward = HawkConfigManager.getInstance().getConfigByKey(MedalFactoryRewardCfg.class, tarCollect.getRewardCfgId());
		int leftCnt = reward.getStealNumMax() - tarCollect.getStealed().size();
		if (leftCnt <= 0) {
			sendError(protocol.getType(), Status.Error.MEFAL_STEALED_TOMUCH_VALUE);
			return;
		}
		// 可偷物品
		List<ItemInfo> canStealList = ItemInfo.valueListOf(reward.getStealReward());
		// 没到上限
		boolean canSteal = false;
		for (ItemInfo item : canStealList) {
			int max = (int) (kvCfg.getItemCntMap().getOrDefault(item.getItemId(), 0L) * (10000 + player.getEffect().getEffVal(EffType.MEDAL_654)) * GsConst.EFF_PER);
			if (todaySteal.getOrDefault(item.getItemId(), 0L) < max) {
				canSteal = true;
			}
		}

		if (!canSteal) {
			sendError(protocol.getType(), Status.Error.MEFAL_STEAL_TOMUCH_VALUE);
			return;
		}
		
		List<ItemInfo> gets = ItemInfo.valueListOf(reward.getReward()); // 原使
		for (MedalStealed stealed : tarCollect.getStealed()) {// 被偷菜
			List<ItemInfo> stealList = ItemInfo.valueListOf(reward.getStealReward());
			for (ItemInfo steal : stealList) {
				for (ItemInfo award : gets) {
					if (award.getItemId() == steal.getItemId()) {
						award.setCount(award.getCount() - steal.getCount());
					}
				}
			}
		}
		
		
		{ // 被偷
			MedalStealed stealed = new MedalStealed();
			stealed.setRewardCfgId(reward.getId());
			stealed.setStart(now);
			stealed.setEnd(now); // 立即成功
			stealed.setPlayerId(player.getId());
			stealed.setName(player.getName());
			stealed.setPficon(player.getPfIcon());
			stealed.setIcon(player.getIcon());
			stealed.setStealAll(true);
			stealed.setStealed(ItemInfo.toString(gets));
			tarCollect.getStealed().add(stealed);
		}

		// 记录正在偷
		{
			MedalSteal steal = new MedalSteal();
			steal.setRewardCfgId(reward.getId());
			steal.setStart(now);
			steal.setEnd(now);
			steal.setTargetId(tarPlayer.getId());
			steal.setTarName(tarPlayer.getName());
			steal.setStealed(ItemInfo.toString(gets));
			getFactory().getSteals().add(steal);
			getFactory().notifyChange();
		}

		ActivityManager.getInstance().postEvent(new MedalFactoryTouEvent(player.getId()));
		PushService.getInstance().pushMsg(tarId, PushMsgType.TOUCAI_BEI_TOU_VALUE, player.getName());
		// 通知目标
		PlayerMedalFactoryModule tarmoudle = tarPlayer.getModule(GsConst.ModuleType.MEDAL_FACTORY);
		tarmoudle.shouqu(protocol.getType(), index);
		
//		tarCollect.getMedalFactory().notifyChange();
//		tarCollect.getMedalFactory().sync();

		player.sendProtocol(HawkProtocol.valueOf(HP.code2.TOUCAI_TAR_FACTORY_S, tarCollect.getMedalFactory().toHP()));
		player.responseSuccess(protocol.getType());

	}

	private void touqu(HawkProtocol protocol) {
		Optional<PresetMarchInfo> preSetInfo = getPresetInfo(player.getId(), CAIJI);
		if (!preSetInfo.isPresent()) {
			sendError(protocol.getType(), Status.Error.MEFAL_NO_16_VALUE);
			return;
		}
		HPMedalFactoryTouReq req = protocol.parseProtocol(HPMedalFactoryTouReq.getDefaultInstance());
		final int index = req.getIndex();
		final String tarId = req.getTarId();
		Player tarPlayer = GlobalData.getInstance().makesurePlayer(tarId);

		MedalFactoryConstCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(MedalFactoryConstCfg.class);
		Map<Integer, Long> todaySteal = getFactory().getStealCnt();
		// 目标工厂
		MedalCollect tarCollect = tarPlayer.getData().getMedalEntity().getFactoryObj().getCollect(index);
		long now = HawkTime.getMillisecond();
		if (tarCollect.getRewardCfgId() == 0 || tarCollect.getEnd() > now) {
			sendError(protocol.getType(), Status.Error.MEFAL_TAR_CHANGHE_VALUE);
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.TOUCAI_TAR_FACTORY_S, tarCollect.getMedalFactory().toHP()));
			return;
		}
		for (MedalStealed stealed : tarCollect.getStealed()) {
			if (stealed.getPlayerId().equals(player.getId())) { // 有正在被偷取
				sendError(protocol.getType(), Status.Error.MEFAL_STEALED_HAS_VALUE);
				return;
			}
			if (stealed.getEnd() > now) { // 有正在被偷取
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.TOUCAI_TAR_FACTORY_S, tarCollect.getMedalFactory().toHP()));
				sendError(protocol.getType(), Status.Error.MEFAL_STEAL_ING_VALUE);
				return;
			}
		}

		// boolean selfStealing = tarCollect.getMedalFactory().getCollects().stream().flatMap(fa -> fa.getStealed().stream()).filter(s -> s.getEnd() > now)
		// .filter(s -> s.getPlayerId().equals(player.getId())).findAny().isPresent();
		// if (selfStealing) {
		// sendError(protocol.getType(), Status.Error.MEFAL_STEALED_ING_VALUE);
		// return;
		// }

		MedalFactoryRewardCfg reward = HawkConfigManager.getInstance().getConfigByKey(MedalFactoryRewardCfg.class, tarCollect.getRewardCfgId());
		if (tarCollect.getStealed().size() >= reward.getStealNumMax()) {
			sendError(protocol.getType(), Status.Error.MEFAL_STEALED_TOMUCH_VALUE);
			return;
		}
		// 可偷物品
		List<ItemInfo> canStealList = ItemInfo.valueListOf(reward.getStealReward());
		// 没到上限
		boolean canSteal = false;
		for (ItemInfo item : canStealList) {
			int max = (int) (kvCfg.getItemCntMap().getOrDefault(item.getItemId(), 0L) * (10000 + player.getEffect().getEffVal(EffType.MEDAL_654)) * GsConst.EFF_PER);
			if (todaySteal.getOrDefault(item.getItemId(), 0L) < max) {
				canSteal = true;
			}
		}

		if (!canSteal) {
			sendError(protocol.getType(), Status.Error.MEFAL_STEAL_TOMUCH_VALUE);
			return;
		}

		MedalStealed stealed = new MedalStealed();
		stealed.setRewardCfgId(reward.getId());
		stealed.setStart(now);
		stealed.setEnd(now + reward.getStealTime() * 1000);
		stealed.setPlayerId(player.getId());
		stealed.setName(player.getName());
		stealed.setPficon(player.getPfIcon());
		stealed.setIcon(player.getIcon());
		tarCollect.getStealed().add(stealed);

		// 记录正在偷
		MedalSteal steal = new MedalSteal();
		steal.setRewardCfgId(reward.getId());
		steal.setStart(stealed.getStart());
		steal.setEnd(stealed.getEnd());
		steal.setTargetId(tarPlayer.getId());
		steal.setTarName(tarPlayer.getName());
		getFactory().getSteals().add(steal);
		getFactory().notifyChange();
		// 通知目标

		tarCollect.getMedalFactory().notifyChange();
		tarCollect.getMedalFactory().sync();

		player.sendProtocol(HawkProtocol.valueOf(HP.code2.TOUCAI_TAR_FACTORY_S, tarCollect.getMedalFactory().toHP()));
		player.responseSuccess(protocol.getType());

		ActivityManager.getInstance().postEvent(new MedalFactoryTouEvent(player.getId()));
		PushService.getInstance().pushMsg(tarId, PushMsgType.TOUCAI_BEI_TOU_VALUE, player.getName());
	}

	/**
	 * 查看采集行军队列
	 */
	@ProtocolHandler(code = HP.code2.TOUCAI_TAR_PREMARCH_C_VALUE)
	private void onTarCaiJiMarch(HawkProtocol protocol) {
		HPMedalFactoryCaiJiMarchReq req = protocol.parseProtocol(HPMedalFactoryCaiJiMarchReq.getDefaultInstance());
		String tarId = req.getTarId();

		Optional<PresetMarchInfo> preSetInfo = getPresetInfo(tarId, CAIJI);
		WorldMarchPB.Builder caiji = null;
		if (preSetInfo.isPresent()) {
			PresetMarchInfo info = preSetInfo.get();
			WorldMarch march = buildMarch(GlobalData.getInstance().makesurePlayer(tarId), info).getMarchEntity();
			caiji = march.toBuilder(WorldMarchPB.newBuilder(), WorldMarchRelation.ENEMY);
		}

		Optional<PresetMarchInfo> prequganSetInfo = getPresetInfo(tarId, QUGAN);
		WorldMarchPB.Builder qugan = null;
		if (prequganSetInfo.isPresent()) {
			PresetMarchInfo info = prequganSetInfo.get();
			WorldMarch march = buildMarch(GlobalData.getInstance().makesurePlayer(tarId), info).getMarchEntity();
			qugan = march.toBuilder(WorldMarchPB.newBuilder(), WorldMarchRelation.ENEMY);
		}

		HPMedalFactoryCaiJiMarchResp.Builder resp = HPMedalFactoryCaiJiMarchResp.newBuilder();
		resp.setTarId(tarId);
		if (caiji != null) {
			resp.setCaijiMarch(caiji);
		}
		if (qugan != null) {
			resp.setQuganMarch(qugan);
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code2.TOUCAI_TAR_PREMARCH_S, resp));
	}

	private TemporaryMarch buildMarch(Player player, PresetMarchInfo info) {
		TemporaryMarch atkMarch = new TemporaryMarch();
		List<ArmyInfo> armys = new ArrayList<>();
		int max = player.getMaxMarchSoldierNum(EffectParams.getDefaultVal());
		for (ArmySoldierPB arpb : info.getArmyList()) {
			int count = arpb.getCount();
			if (info.getPercentArmy()) {
				count = (int) (1D * max / 1000000 * count) + 1;
			}
			armys.add(new ArmyInfo(arpb.getArmyId(), count));
		}

		WorldMarch march = atkMarch.getMarchEntity();
		march.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_VALUE);
		march.setTargetId("");
		march.setMarchId(player.getId());
		march.setPlayerId(player.getId());
		march.setPlayerName(player.getName());
		march.setMarchType(WorldMarchType.COLLECT_RES_TREASURE_VALUE);
		march.setArmourSuit(info.getArmourSuit().getNumber());
		march.setMechacoreSuit(info.getMechacoreSuit().getNumber());
		march.setHeroIdList(info.getHeroIdsList());
		march.setSuperSoldierId(info.getSuperSoldierId());
		march.setTalentType(info.getTalentType().getNumber());
		march.setSuperLab(info.getSuperLab());
		march.setManhattanAtkSwId(info.getManhattan().getManhattanAtkSwId());
		march.setManhattanDefSwId(info.getManhattan().getManhattanDefSwId());
		atkMarch.setArmys(armys);
		atkMarch.setPlayer(player);
		atkMarch.setHeros(player.getHeroByCfgId(info.getHeroIdsList()));
		march.setDressList(info.getMarchDressList());
		return atkMarch;
	}

	/**
	 * 驱赶
	 */
	@ProtocolHandler(code = HP.code2.TOUCAI_QUGAN_REQ_VALUE)
	private void onQuGan(HawkProtocol protocol) {
		HPMedalFactoryQuganReq req = protocol.parseProtocol(HPMedalFactoryQuganReq.getDefaultInstance());
		final int index = req.getIndex();
		final String tarId = req.getTarId();
		Player tarPlayer = GlobalData.getInstance().makesurePlayer(tarId);

		MedalCollect collect = getFactory().getCollect(index);

		MedalStealed stealed = collect.getStealed().stream().filter(st -> st.getPlayerId().equals(tarId)).findFirst().orElse(null);

		long now = HawkTime.getMillisecond();
		if (stealed == null || stealed.getEnd() < now || stealed.getQcnt() > 0) { // 偷完了
			sendError(protocol.getType(), Status.Error.MEFAL_STEAL_OVER_VALUE);
			return;
		}

		Optional<PresetMarchInfo> preSetInfo = getPresetInfo(player.getId(), QUGAN);
		if (!preSetInfo.isPresent()) {
			sendError(protocol.getType(), Status.Error.MEFAL_NO_17_VALUE);
			return;
		}
		boolean isAtkWin = true;
		Optional<PresetMarchInfo> tarpreSetInfo = getPresetInfo(tarId, CAIJI);
		if (tarpreSetInfo.isPresent()) {
			List<Player> atkPlayers = new ArrayList<>();
			atkPlayers.add(player);
			List<Player> defPlayers = new ArrayList<>();
			defPlayers.add(tarPlayer);
			List<IWorldMarch> atkMarchs = new ArrayList<>();
			atkMarchs.add(buildMarch(player, preSetInfo.get()));

			List<IWorldMarch> defMarchs = new ArrayList<>();
			defMarchs.add(buildMarch(tarPlayer, tarpreSetInfo.get()));

			PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.SIMULATE_WAR, 0, atkPlayers, defPlayers, atkMarchs, defMarchs);
			battleIncome.setDuntype(DungeonMailType.TOUCAI);
			BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
			isAtkWin = battleOutcome.isAtkWin();

			FightMailService.getInstance().sendFightMail(WorldPointType.MEDAL_FACTORY_VALUE, battleIncome, battleOutcome, null);
		}

		if (isAtkWin) {
			collect.getStealed().remove(stealed);
			collect.getMedalFactory().notifyChange();

			// 被驱赶
			MedalFactoryObj tarFactory = tarPlayer.getData().getMedalEntity().getFactoryObj();
			List<MedalSteal> toRemove = tarFactory.getSteals().stream()
					.filter(steal -> steal.getStart() == stealed.getStart())
					.filter(steal -> Objects.equals(steal.getTargetId(), player.getId()))
					.collect(Collectors.toList());
			tarFactory.getSteals().removeAll(toRemove);
			tarFactory.notifyChange();
			tarFactory.sync();
		}
		stealed.setQcnt(stealed.getQcnt() + 1);
		collect.getMedalFactory().notifyChange();
		collect.getMedalFactory().sync();

		tarPlayer.sendProtocol(HawkProtocol.valueOf(HP.code2.TOUCAI_TAR_FACTORY_S, collect.getMedalFactory().toHP()));

		HPMedalFactoryQuganResp.Builder resp = HPMedalFactoryQuganResp.newBuilder();
		resp.setIndex(index);
		resp.setTarId(tarId);
		resp.setSuccess(isAtkWin);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.TOUCAI_QUGAN_RESP, resp));

	}

	/**
	 * 查看目标工厂 
	 */
	@ProtocolHandler(code = HP.code2.TOUCAI_TAR_FACTORY_C_VALUE)
	private void onTarFactory(HawkProtocol protocol) {
		HPMedalFactoryReq req = protocol.parseProtocol(HPMedalFactoryReq.getDefaultInstance());
		Player tarPlayer = GlobalData.getInstance().makesurePlayer(req.getTarId());
		HPMedalFactoryInfo.Builder resp = HPMedalFactoryInfo.newBuilder();
		if (tarPlayer != null) { // 溜了
			resp = tarPlayer.getData().getMedalEntity().getFactoryObj().toHP();
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.TOUCAI_TAR_FACTORY_S, resp));
	}

	@ProtocolHandler(code = HP.code2.TOUCAI_HISTORY_REQ_VALUE)
	private void onHistory(HawkProtocol protocol) {
		List<HPMedalFactoryHistory> list = listRecord(player.getId());
		HPMedalHistroyResp.Builder builder = HPMedalHistroyResp.newBuilder();
		builder.addAllHistory(list);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.TOUCAI_HISTORY_RESP, builder));
	}

	private void saveRecord(int type, String targetId, String reward, String keyPlayerId, long ctime) {
		try {
			HPMedalFactoryHistory.Builder record = HPMedalFactoryHistory.newBuilder();
			record.setType(type);
			Player tarPlayer = GlobalData.getInstance().makesurePlayer(targetId);
			if (tarPlayer != null) {
				record.setPlayerId(tarPlayer.getId());
				record.setIcon(tarPlayer.getIcon());
				record.setPficon(tarPlayer.getPfIcon());
				record.setName(tarPlayer.getName());
			}else{
				record.setPlayerId(targetId);
				record.setName("溜了溜了");
			}
			record.setItemGet(reward);
			record.setCreatTime(ctime);
			record.setId(HawkUUIDGenerator.genUUID());
			String key = HistoryKey + keyPlayerId;
			RedisProxy.getInstance().getRedisSession().zAdd(key.getBytes(), record.getCreatTime(), record.build().toByteArray(), HistoryExpire);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private List<HPMedalFactoryHistory> listRecord(String playerId) {
		String key = HistoryKey + playerId;
		List<HPMedalFactoryHistory> list = new ArrayList<>();
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis()) {
			Set<Tuple> rankSet = jedis.zrevrangeWithScores(key.getBytes(), 0, 49);
			for (Tuple tup : rankSet) {
				try {
					HPMedalFactoryHistory record = HPMedalFactoryHistory.newBuilder().mergeFrom(tup.getBinaryElement()).build();
					list.add(record);
				} catch (InvalidProtocolBufferException e) {
					HawkException.catchException(e);
				}
			}
		}

		return list;
	}

	/**
	 * 查看采集行军队列
	 */
	@ProtocolHandler(code = HP.code2.TOUCAI_SELF_PREMARCH_C_VALUE)
	private void onSelfMarch(HawkProtocol protocol) {

		syncSelfPreMarch();
	}

	private void syncSelfPreMarch() {
		HPMedalFactoryPreMarchResp.Builder resp = HPMedalFactoryPreMarchResp.newBuilder();
		Optional<PresetMarchInfo> preSetInfo = getPresetInfo(player.getId(), CAIJI);
		if (preSetInfo.isPresent()) {
			PresetMarchInfo info = preSetInfo.get();
			resp.setCaijiMarch(info);
		}

		Optional<PresetMarchInfo> prequganSetInfo = getPresetInfo(player.getId(), QUGAN);
		if (prequganSetInfo.isPresent()) {
			PresetMarchInfo info = prequganSetInfo.get();
			resp.setQuganMarch(info);
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code2.TOUCAI_SELF_PREMARCH_S, resp));
	}

	/** 每日奖励领取*/
	@ProtocolHandler(code = HP.code2.TOUCAI_DAILY_RAWARD_REQ_VALUE)
	private void onGetDailyReward(HawkProtocol protocol) {
		MedalFactoryObj factoryObj = getFactory();
		if (factoryObj.getDbEntity().getDailyReward() == HawkTime.getYearDay()) {
			return;
		}
		MedalFactoryLevelCfg levelCfg = factoryObj.getLevelCfg();
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItemInfos(ItemInfo.valueListOf(levelCfg.getDailyReward()));
		if (player.getEffect().getEffVal(EffType.MEDAL_653) > 0) {
			awardItem.addItemInfos(ItemInfo.valueListOf(levelCfg.getMonCardReward()));
		}

		awardItem.rewardTakeAffectAndPush(player, Action.MEDAL_DAILY, true);

		factoryObj.getDbEntity().setDailyReward(HawkTime.getYearDay());
		factoryObj.notifyChange();
		factoryObj.sync();
		player.responseSuccess(protocol.getType());
	}

	/** 编辑预设队列*/
	@ProtocolHandler(code = HP.code2.TOUCAI_PREMARCH_SET_REQ_VALUE)
	private boolean onAddWorldMarchPresetInfo(HawkProtocol protocol) {
		PresetMarchInfo req = protocol.parseProtocol(PresetMarchInfo.getDefaultInstance());
		if (req.getIdx() != CAIJI && req.getIdx() != QUGAN) {
			return false;
		}
		if (req.getSuperSoldierId() > 0) {
			if (player.getSuperSoldierByCfgId(req.getSuperSoldierId()) == null) {
				return false;
			}
		}
		if (req.getHeroIdsCount() > 2) { // 验证英雄不能超过2个
			return false;
		}
		if (req.getArmourSuit() == ArmourSuitType.ARMOUR_NONE) {
			req = req.toBuilder().setArmourSuit(ArmourSuitType.valueOf(player.getEntity().getArmourSuit())).build();
		}
		// 检测皮肤信息
		PlayerMarchModule marckModule = player.getModule(ModuleType.WORLD_MARCH_MODULE);
		if (!marckModule.checkMarchDressReq(req.getMarchDressList())) {
			return false;
		}
		for (PlayerHero hero : player.getHeroByCfgId(req.getHeroIdsList())) { // 两英雄互斥
			if (req.getHeroIdsList().contains(hero.getConfig().getProhibitedHero())) {
				return false;
			}
		}

		if (req.getSuperLab() != 0 && !player.isSuperLabActive(req.getSuperLab())) {
			return false;
		}
		final String key = MedalPreMarch + player.getId();
		RedisProxy.getInstance().getRedisSession().hSetBytes(key, req.getIdx() + "", req.toByteArray());

		syncSelfPreMarch();
		player.responseSuccess(protocol.getType());
		return true;
	}

	private Optional<PresetMarchInfo> getPresetInfo(String playerId, int idx) {
		final String key = MedalPreMarch + playerId;
		byte[] bytes = RedisProxy.getInstance().getRedisSession().hGetBytes(key, idx + "");
		if (bytes == null) {
			return Optional.empty();
		}
		PresetMarchInfo info = null;
		try {
			info = PresetMarchInfo.newBuilder().mergeFrom(bytes).build();
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Optional.ofNullable(info);
	}

	@MessageHandler
	private void onEffectChangeEvent(PlayerEffectChangeMsg event) {
		boolean repush = event.hasEffectChange(EffType.MEDAL_649);
		if (repush) {
			getFactory().notifyChange();
			getFactory().sync();
		}
	}

	@MessageHandler
	private void onBuildingLevelUpMsg(BuildingLevelUpMsg msg) {
		if (msg.getBuildingType() == BuildingType.TOUCAI_FACTORY_VALUE || msg.getBuildingType() == BuildingType.CONSTRUCTION_FACTORY_VALUE) {
			if (getFactory().isUnlock()) {
				getFactory().notifyChange();
				getFactory().sync();
			}
		}
	}

	@MessageHandler
	private void onBuyMonthCardMsg(BuyMonthCardMsg msg) {
		if (msg.getMonthCardType() != 18) {
			return;
		}

		MedalFactoryObj factoryObj = getFactory();
		if (factoryObj.getDbEntity().getDailyReward() == HawkTime.getYearDay()) { // 今日已领取 补
			MedalFactoryLevelCfg levelCfg = factoryObj.getLevelCfg();
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.MONTHARD_MEDALF_REWARD)
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.setRewards(levelCfg.getMonCardReward())
					.build());
		}
	}
}
