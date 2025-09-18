package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;

import com.google.common.base.Splitter;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.GuildStoreEvent;
import com.hawk.game.config.AllianceStorehouseCfg;
import com.hawk.game.config.AllianceStorehouseCharacterCfg;
import com.hawk.game.config.AllianceStorehouseMinimumCfg;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.config.PurchaseCfg;
import com.hawk.game.entity.StorehouseBaseEntity;
import com.hawk.game.entity.StorehouseEntity;
import com.hawk.game.entity.StorehouseHelpEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.guild.guildrank.GuildRankMgr;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.msg.GuildJoinMsg;
import com.hawk.game.msg.GuildQuitMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.SpeedUpTimeWeightType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Storehouse.HPExcavateReq;
import com.hawk.game.protocol.Storehouse.HPGetHelpRewardReq;
import com.hawk.game.protocol.Storehouse.HPGetRewardReq;
import com.hawk.game.protocol.Storehouse.HPHelpHPExcavateReq;
import com.hawk.game.protocol.Storehouse.HPHelpHPExcavateSpeedupReq;
import com.hawk.game.protocol.Storehouse.HPQueryHelpReq;
import com.hawk.game.protocol.Storehouse.HPStoreHelp;
import com.hawk.game.protocol.Storehouse.HPStorehouseList;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.guildtask.event.GuildStorehouseRewardTaskEvent;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventGuildExcavte;
import com.hawk.game.service.mssion.event.EventGuildExcavteHelp;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.Predicates;
import com.hawk.log.Action;
import com.hawk.log.LogConst.StoreHouseOperType;

/**
 * 联盟宝藏
 * 
 * @author lwt
 *
 */
public class PlayerStorehouseModule extends PlayerModule {
	private enum HelpType {
		HELPED(1), UNHELP(2), LOCKED(3);
		HelpType(int value) {
			this.value = value;
		}

		int value;

		int intValue() {
			return value;
		}
	}

	private int groupLevelUpRefrashCount;

	// 宝藏自动刷新时间
	private int refreshTime() {
		return GuildConstProperty.getInstance().getRefreshTime() * 1000;
	}

	// 宝藏挖掘恢复时间
	private int excavateTime() {
		return Math.max(1, GuildConstProperty.getInstance().getExcavateTime() * 1000);
	}

	/** 宝藏帮助恢复时间 */
	private int helpTime() {
		return Math.max(1, GuildConstProperty.getInstance().getHelpTime() * 1000);
	}

	public PlayerStorehouseModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		if (player.isCsPlayer()) {
			return true;
		}
		
		try {
			List<StorehouseEntity> rmStorehouseEntities = new ArrayList<>();
			List<StorehouseEntity> storehouseEntities = player.getData().getStorehouseEntities();
			for (StorehouseEntity entity : storehouseEntities) {
				Player target = GlobalData.getInstance().makesurePlayer(entity.getHelpId());
				if (entity.getHelpId() != null && target == null) {
					rmStorehouseEntities.add(entity);
				}
			}
			
			List<StorehouseHelpEntity> rmStorehouseHelpEntities = new ArrayList<>();
			List<StorehouseHelpEntity> storehouseHelpEntities = player.getData().getStorehouseHelpEntities();
			for (StorehouseHelpEntity entity : storehouseHelpEntities) {
				Player target = GlobalData.getInstance().makesurePlayer(entity.getTargetId());
				if (target == null) {
					rmStorehouseHelpEntities.add(entity);
				}
			}
			
			for (StorehouseEntity entity : rmStorehouseEntities) {
				entity.delete(true);
				player.getData().getStorehouseEntities().remove(entity);
			}
			
			for (StorehouseHelpEntity entity : rmStorehouseHelpEntities) {
				entity.delete(true);
				player.getData().getStorehouseHelpEntities().remove(entity);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return true;
	}

	@Override
	public boolean onTick() {
		return super.onTick();
	}

	@MessageHandler
	private boolean onGuildJoinMsg(GuildJoinMsg msg) {
		syncStorehouseInfo();
		return true;
	}

	/**
	 * 退出联盟
	 * 
	 * @return
	 */
	@MessageHandler
	private boolean onGuildQuitMsg(GuildQuitMsg msg) {
		HawkDBEntity.batchDelete(player.getData().getStorehouseEntities());
		player.getData().getStorehouseEntities().clear();
		HawkDBEntity.batchDelete(player.getData().getStorehouseHelpEntities());
		player.getData().getStorehouseHelpEntities().clear();
		player.getData().getStorehouseBase().setNextFreeExc(0);
		return true;
	}

	/**
	 * 取得宝藏列表
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.STORE_HOUSE_INFO_C_VALUE)
	private void onStorehouseInfo(HawkProtocol protocol) {
		HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
		if (threadPool != null) {
			HawkTask task = new HawkTask() {
				@Override
				public Object run() {
					if (player.hasGuild()) {
						syncStorehouseInfo();
					}

					player.responseSuccess(protocol.getType());
					return null;
				}
			};
			task.setPriority(1);
			task.setTypeName("onStorehouseInfo");
			int threadIndex = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
			threadPool.addTask(task, threadIndex, false);
			return;
		} else {

			if (player.hasGuild()) {
				syncStorehouseInfo();
			}

			player.responseSuccess(protocol.getType());
		}

	}

	/**
	 * 挖掘
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.STORE_HOUSE_EXC_C_VALUE)
	private void onExcavate(HawkProtocol protocol) {
		HPExcavateReq req = protocol.parseProtocol(HPExcavateReq.getDefaultInstance());
		final int storeId = req.getStoreId();
		StorehouseBaseEntity storeHouseBase = refrashAndGet();
		ConsumeItems consume = ConsumeItems.valueOf();
		Optional<ItemInfo> excCost = excCost();
		excCost.ifPresent(cost -> consume.addConsumeInfo(cost, false));

		if (storeHouseBase.getExc() < 1) {
			sendError(protocol.getType(), Status.Error.STORE_HOUSE_EXCPLESS);
			syncStorehouseInfo();
			return;
		}

		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		if (!storeHouseBase.getStoreList().contains(storeId)) {
			sendError(protocol.getType(), Status.Error.STORE_HOUSE_DISAPER);
			syncStorehouseInfo();
			return;
		}

		int cost = 0;
		if (!excCost.isPresent()) {// 如果本次免费
			AllianceStorehouseCfg storeCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceStorehouseCfg.class, storeId);
			AllianceStorehouseCharacterCfg charaCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceStorehouseCharacterCfg.class, storeCfg.getGroupId());
			storeHouseBase.setNextFreeExc(HawkTime.getMillisecond() + charaCfg.getOpenTime() * 1000);
		} else {
			cost = (int)excCost.get().getCount();
		}

		consume.consumeAndPush(player, Action.STOREHOUSE_EXCAVATE);
		StorehouseEntity store = newStorehouse(storeId);
		// 如果挖掘最高品质,保底重置
		int firstStoreGroup = maxStoreGroup(storeHouseBase);
		AllianceStorehouseCfg storecfg = HawkConfigManager.getInstance().getConfigByKey(AllianceStorehouseCfg.class, storeId);
		if (storecfg.getGroupId() == firstStoreGroup) {
			groupLevelUpRefrashCount = 0;
		}

		storeHouseBase.setExcCount(storeHouseBase.getExcCount() + 1);
		storeHouseBase.getStoreList().remove(Integer.valueOf(storeId));
		storeHouseBase.setExc(storeHouseBase.getExc() - 1);

		syncStorehouseInfo();
		changeNotice();
		MissionManager.getInstance().postMsg(player, new EventGuildExcavte(1)); // 任务刷新
		ActivityManager.getInstance().postEvent(new GuildStoreEvent(player.getId()));
		player.responseSuccess(protocol.getType());

		LogUtil.logStoreHouseFlow(player, store.getId(), store.getStoreId(), StoreHouseOperType.STOREHOUSE_EXCAVATE, cost);
	}

	private Optional<ItemInfo> excCost() {
		StorehouseBaseEntity storeHouseBase = refrashAndGet();
		long now = HawkTime.getMillisecond();
		if (now > storeHouseBase.getNextFreeExc()) {
			return Optional.empty();
		}
		return Optional.of(ItemInfo.valueOf(GuildConstProperty.getInstance().getExcavateCost()));
	}

	/** 帮助 */
	@ProtocolHandler(code = HP.code.STORE_HOUSE_HELP_C_VALUE)
	private void onHelpExcavate(HawkProtocol protocol) {
		HPHelpHPExcavateReq req = protocol.parseProtocol(HPHelpHPExcavateReq.getDefaultInstance());
		final String targetId = req.getPlayerId(); // 被帮助人ID
		final String storehouseId = req.getStoreUUID(); // 对方宝藏UUID

		StorehouseBaseEntity storeHouseBase = refrashAndGet();
		if (storeHouseBase.getHelp() < 1) {
			sendError(protocol.getType(), Status.Error.STORE_HOUSE_HELPLESS);
			syncStorehouseInfo();
			return;
		}
		long helping = player.getData().getStorehouseHelpEntities().stream()
				.filter(e -> e.getOpenTime() > HawkTime.getMillisecond())
				.count();
		final Player tarPlayer = GlobalData.getInstance().makesurePlayer(targetId);
		if (null == tarPlayer || helping > 0) {
			return;
		}
		
		Optional<StorehouseEntity> tarStoreOP = tarPlayer.getData().getStorehouseEntities().stream()
				.filter(s -> Objects.equals(s.getId(), storehouseId))
				.findAny();
		if (!tarStoreOP.isPresent()) {
			sendError(protocol.getType(), Status.Error.STORE_HOUSE_DISAPER);
			syncStorehouseInfo();
			return;
		}
		StorehouseEntity tarStoreHouse = tarStoreOP.get();
		if (!HawkOSOperator.isEmptyString(tarStoreHouse.getHelpId())) {// 已经有帮助人操作失败
			sendError(protocol.getType(), Status.Error.STORE_HOUSE_HELPED);
			syncStorehouseInfo();
			return;
		}
		tarStoreHouse.setHelpId(player.getId());// 对方玩家数据. transient 关健字

		newHelpStorehouse(tarStoreHouse);
		syncStorehouseInfo();
		storeHouseBase.setHelp(storeHouseBase.getHelp() - 1);

		player.responseSuccess(protocol.getType());
		changeNotice();

		LogUtil.logStoreHouseFlow(player, tarStoreHouse.getId(), tarStoreHouse.getStoreId(), StoreHouseOperType.STOREHOUSE_HELP, 0);
		
		MissionManager.getInstance().postMsg(player, new EventGuildExcavteHelp(1)); // 任务刷新
	}

	/** 加速 */
	@ProtocolHandler(code = HP.code.STORE_HOUSE_SPEED_C_VALUE)
	private void onSpeedUp(HawkProtocol protocol) {
		HPHelpHPExcavateSpeedupReq req = protocol.parseProtocol(HPHelpHPExcavateSpeedupReq.getDefaultInstance());
		final String helpUUID = req.getHelpUUID();
		Optional<StorehouseHelpEntity> entityOpr = player.getData().getStorehouseHelpEntities().stream()
				.filter(e -> Objects.equals(helpUUID, e.getId()))
				.findAny();
		if (!entityOpr.isPresent()) {
			return;
		}
		StorehouseHelpEntity helpEntity = entityOpr.get();
		long now = HawkTime.getMillisecond();
		int gold = GameUtil.caculateTimeGold(Math.max(0, (helpEntity.getOpenTime() - now) / 1000), SpeedUpTimeWeightType.SPEEDUP_COEFFICIENT);
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(PlayerAttr.GOLD, gold);
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		consume.consumeAndPush(player, Action.STOREHOUSE_HELP_SEPPDUP);
		helpEntity.setOpenTime(now);

		syncStorehouseInfo();
		player.responseSuccess(protocol.getType());

		LogUtil.logStoreHouseFlow(player, helpEntity.getId(), helpEntity.getStoreId(), StoreHouseOperType.STOREHOUSE_SPEED, gold);
	}

	/** 领取帮助奖励 */
	@ProtocolHandler(code = HP.code.STORE_HOUSE_HELP_REWARD_C_VALUE)
	private void onGetHelpReward(HawkProtocol protocol) {
		HPGetHelpRewardReq req = protocol.parseProtocol(HPGetHelpRewardReq.getDefaultInstance());
		final String helpUUID = req.getHelpUUID();
		Optional<StorehouseHelpEntity> entityOpr = player.getData().getStorehouseHelpEntities().stream()
				.filter(e -> Objects.equals(helpUUID, e.getId()))
				.findAny();
		if (!entityOpr.isPresent()) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			HawkLog.errPrintln("store_house_help_reward req failed, playerId: {}, param: {}", player.getId(), helpUUID);
			return;
		}
		StorehouseHelpEntity helpEntity = entityOpr.get();
		long now = HawkTime.getMillisecond();
		if (helpEntity.isCollect() || helpEntity.getOpenTime() > now) {// 已领
			HawkLog.errPrintln("store_house_help_reward req failed, playerId: {}, param: {}, collect: {}, openTime: {}", player.getId(), helpUUID, helpEntity.isCollect(), helpEntity.getOpenTime());
			return;
		}

		AllianceStorehouseCfg storeCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceStorehouseCfg.class, helpEntity.getStoreId());

		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItemInfos(ItemInfo.valueListOf(storeCfg.getHelpaward()));
		awardItem.rewardTakeAffectAndPush(player, Action.STOREHOUSE_HELP_REWARD, true, RewardOrginType.STORE_HOUSE_REWARD);
		helpEntity.setCollect(true);
		syncStorehouseInfo();
		player.responseSuccess(protocol.getType());

		helpEntity.delete(true);
		player.getData().getStorehouseHelpEntities().remove(helpEntity);
		// 联盟任务-领取帮助奖励
		GuildService.getInstance().postGuildTaskMsg(new GuildStorehouseRewardTaskEvent(player.getGuildId()));
		LogUtil.logStoreHouseFlow(player, helpEntity.getId(), helpEntity.getStoreId(), StoreHouseOperType.STOREHOUSE_HELP_REWARD, 0);
		GuildRankMgr.getInstance().onPlayerTreasure( player.getId(), player.getGuildId(), 1 );
	}

	/** 领取宝藏奖励 */
	@ProtocolHandler(code = HP.code.STORE_HOUSE_REWARD_C_VALUE)
	private void onGetReward(HawkProtocol protocol) {
		HPGetRewardReq req = protocol.parseProtocol(HPGetRewardReq.getDefaultInstance());
		final String storeUUID = req.getStoreUUID();
		Optional<StorehouseEntity> entityOpr = player.getData().getStorehouseEntities().stream()
				.filter(e -> Objects.equals(storeUUID, e.getId()))
				.findAny();
		if (!entityOpr.isPresent()) {
			syncStorehouseInfo();
			player.responseSuccess(protocol.getType());
			return;
		}

		StorehouseEntity storeEntity = entityOpr.get();
		long now = HawkTime.getMillisecond();
		if (storeEntity.isCollect() || now < storeEntity.getOpenTime()) {// 已领
			syncStorehouseInfo();
			player.responseSuccess(protocol.getType());
			return;
		}

		AllianceStorehouseCfg storeCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceStorehouseCfg.class, storeEntity.getStoreId());

		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItemInfos(ItemInfo.valueListOf(storeCfg.getAward()));
		awardItem.rewardTakeAffectAndPush(player, Action.STOREHOUSE_REWARD, true, RewardOrginType.STORE_HOUSE_REWARD);
		storeEntity.setCollect(true);
		syncStorehouseInfo();
		player.responseSuccess(protocol.getType());

		storeEntity.delete(true);
		player.getData().getStorehouseEntities().remove(storeEntity);
		// 联盟任务-领取挖掘奖励
		GuildService.getInstance().postGuildTaskMsg(new GuildStorehouseRewardTaskEvent(player.getGuildId()));
		LogUtil.logStoreHouseFlow(player, storeEntity.getId(), storeEntity.getStoreId(), StoreHouseOperType.STOREHOUSE_REWARD, 0);
		
		//领取联盟宝箱
		GuildRankMgr.getInstance().onPlayerTreasure(player.getId(), player.getGuildId(), 1);
	}

	/** 请求帮助挖掘 */
	@ProtocolHandler(code = HP.code.STORE_HOUSE_QUERY_HELP_VALUE)
	private void onQueryHelp(HawkProtocol protocol) {
		HPQueryHelpReq req = protocol.parseProtocol(HPQueryHelpReq.getDefaultInstance());
		final String storeUUID = req.getStoreUUID();
		Optional<StorehouseEntity> entityOpr = player.getData().getStorehouseEntities().stream()
				.filter(e -> Objects.equals(storeUUID, e.getId()))
				.findAny();
		if (!entityOpr.isPresent()) {
			syncStorehouseInfo();
			player.responseSuccess(protocol.getType());
			return;
		}

		StorehouseEntity storeEntity = entityOpr.get();

		if (storeEntity.getQueryHelp() > 0) {// 已领
			syncStorehouseInfo();
			player.responseSuccess(protocol.getType());
			return;
		}

		// 联盟聊天推送
		//TODO jason fix 联盟宝藏求助权限检测,先屏蔽
//		if (GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.GUILD_TREASURE_PERMISSION)) {
//			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
//			return;
//		}

		AllianceStorehouseCfg storeCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceStorehouseCfg.class, storeEntity.getStoreId());
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.GUILD_HREF, Const.NoticeCfgId.STORE_HOUSE_QUERY_HELP,
				player, storeCfg.getColor(), storeCfg.getName());
		storeEntity.setQueryHelp(storeEntity.getQueryHelp() + 1);
		syncStorehouseInfo();
		changeNotice();
		player.responseSuccess(protocol.getType());

		LogUtil.logStoreHouseFlow(player, storeEntity.getId(), storeEntity.getStoreId(), StoreHouseOperType.STOREHOUSE_QUERY_HELP, 0);

	}

	/** 手动刷新宝藏奖励 */
	@ProtocolHandler(code = HP.code.STORE_HOUSE_REFRASH_C_VALUE)
	private void onRefrashStore(HawkProtocol protocol) {
		StorehouseBaseEntity storeHouseBase = refrashAndGet();
		ItemInfo price = refrastCost();

		boolean free = price.getCount() == 0;
		if (!free) {
			ConsumeItems consume = ConsumeItems.valueOf();
			consume.addConsumeInfo(price, false);
			// 钻石不足
			if (!consume.checkConsume(player, protocol.getType())) {
				return;
			}
			consume.consumeAndPush(player, Action.STORE_HOUSE_REFRASH);
		}

		storeHouseBase.setRefrashCount(storeHouseBase.getRefrashCount() + 1);

		int lastStoreGroup = maxStoreGroup(storeHouseBase);
		int firstStoreGroup = lastStoreGroup;
		AllianceStorehouseMinimumCfg upcfg = HawkConfigManager.getInstance().getConfigByKey(AllianceStorehouseMinimumCfg.class, firstStoreGroup);
		groupLevelUpRefrashCount++;
		if (groupLevelUpRefrashCount > upcfg.getUpgradeNum()) {
			firstStoreGroup = firstStoreGroup + 1;
			AllianceStorehouseMinimumCfg ocfg = HawkConfigManager.getInstance().getConfigByKey(AllianceStorehouseMinimumCfg.class, firstStoreGroup);
			if (Objects.nonNull(ocfg)) {
				upcfg = ocfg;
			}
		}
		firstStoreGroup = upcfg.nextGoup();
		final int cityLevel = player.getCityLevel();
		List<Integer> list = new ArrayList<Integer>(3);
		list.add(GameUtil.randomStore(cityLevel, free, firstStoreGroup));
		list.add(GameUtil.randomStore(cityLevel, free, 0));
		list.add(GameUtil.randomStore(cityLevel, free, 0));
		storeHouseBase.setStoreList(list);
		syncStorehouseInfo();
		player.responseSuccess(protocol.getType());

		if (maxStoreGroup(storeHouseBase) > lastStoreGroup) {// 只要宝藏品质提高就重置
			groupLevelUpRefrashCount = 0;
		}

		LogUtil.logStoreHouseFlow(player, "NULL", 0, StoreHouseOperType.STOREHOUSE_REFRASH, (int)price.getCount());
	}

	private int maxStoreGroup(StorehouseBaseEntity storeHouseBase) {
		int firstStoreGroup = 1;
		for (int storeId : storeHouseBase.getStoreList()) {
			AllianceStorehouseCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllianceStorehouseCfg.class, storeId);
			if (Objects.nonNull(cfg)) {
				firstStoreGroup = Math.max(firstStoreGroup, cfg.getGroupId());
			}
		}
		return firstStoreGroup;
	}

	private ItemInfo refrastCost() {
		StorehouseBaseEntity storeHouseBase = refrashAndGet();
		int refrashCount = storeHouseBase.getRefrashCount();
		PurchaseCfg purchaseCfg = HawkConfigManager.getInstance().getConfigByKey(PurchaseCfg.class, ++refrashCount);
		if (Objects.isNull(purchaseCfg)) {
			purchaseCfg = HawkConfigManager.getInstance().getConfigIterator(PurchaseCfg.class).stream()
					.sorted(Comparator.comparingInt(PurchaseCfg::getId).reversed())
					.findFirst()
					.get();
		}
		ItemInfo price = ItemInfo.valueOf(purchaseCfg.getRefreshcost());
		return price;
	}

	/** 帮助其它玩家挖宝 */
	private void newHelpStorehouse(StorehouseEntity tarStoreHouse) {
		AllianceStorehouseCfg storeCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceStorehouseCfg.class, tarStoreHouse.getStoreId());
		AllianceStorehouseCharacterCfg charaCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceStorehouseCharacterCfg.class, storeCfg.getGroupId());

		StorehouseHelpEntity entity = new StorehouseHelpEntity();
		entity.setPlayerId(player.getId());
		entity.setTargetId(tarStoreHouse.getPlayerId());
		entity.setStorehouseId(tarStoreHouse.getId());
		entity.setStoreId(tarStoreHouse.getStoreId());
		entity.setOpenTime(HawkTime.getMillisecond() + charaCfg.getOpenTime() * 1000);

		HawkDBManager.getInstance().create(entity);
		player.getData().getStorehouseHelpEntities().add(entity);

	}

	/** 新增宝藏 */
	private StorehouseEntity newStorehouse(final int storeId) {
		AllianceStorehouseCfg storeCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceStorehouseCfg.class, storeId);
		AllianceStorehouseCharacterCfg charaCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceStorehouseCharacterCfg.class, storeCfg.getGroupId());
		StorehouseEntity entity = new StorehouseEntity();
		entity.setPlayerId(player.getId());
		entity.setStoreId(storeId);
		entity.setOpenTime(HawkTime.getMillisecond() + charaCfg.getOpenTime() * 1000);

		// 给自己添加宝藏
		HawkDBManager.getInstance().create(entity);
		player.getData().getStorehouseEntities().add(entity);
		return entity;
	}

	private StorehouseBaseEntity refrashAndGet() {
		long now = HawkTime.getMillisecond();
		StorehouseBaseEntity storeHouseBase = player.getData().getStorehouseBase();
		GuildService.getInstance().checkCrossDay(player);
		long lastRefrash = storeHouseBase.getLastRefrash();
		if (now - lastRefrash > refreshTime()) {
			groupLevelUpRefrashCount = 0;
			List<Integer> list;
			if (storeHouseBase.getExcCount() == 0) {
				list = Splitter.on("_").omitEmptyStrings().splitToList(GuildConstProperty.getInstance().getFirstStorehouse()).stream()
						.map(Integer::valueOf)
						.collect(Collectors.toList());
			} else {
				final int cityLevel = player.getCityLevel();
				list = new ArrayList<Integer>(3);
				list.add(GameUtil.randomStore(cityLevel, true, 0));
				list.add(GameUtil.randomStore(cityLevel, true, 0));
				list.add(GameUtil.randomStore(cityLevel, true, 0));
			}
			storeHouseBase.setStoreList(list);
			if (lastRefrash == 0) {
				storeHouseBase.setLastRefrash(now);
			} else {
				storeHouseBase.setLastRefrash(now - (now - lastRefrash) % refreshTime());
			}
		}
		return storeHouseBase;
	}

	private void syncStorehouseInfo() {
		HPStorehouseList.Builder resp = HPStorehouseList.newBuilder();
		StorehouseBaseEntity storehouseBase = refrashAndGet();
		resp.setRefrashTime(storehouseBase.getLastRefrash() + refreshTime())
				.setExc(storehouseBase.getExc())
				.setHelp(storehouseBase.getHelp())
				.addAllStoreId(storehouseBase.getStoreList());
		if (Long.MAX_VALUE != storehouseBase.getLastExcRecover()) {
			resp.setExcRecover(storehouseBase.getLastExcRecover() + excavateTime());
		}
		if (Long.MAX_VALUE != storehouseBase.getLastHelpRecover()) {
			resp.setHelpRecover(storehouseBase.getLastHelpRecover() + helpTime());
		}
		// 挖掘费用
		excCost().ifPresent(item -> {
			resp.setExcavateCost(item.toString());
			resp.setFreeExcTime(storehouseBase.getNextFreeExc());
		});
		// 刷新费
		resp.setRefrashCost(refrastCost().toString());
		// 我的
		Predicate<StorehouseEntity> isShow = Predicates.of((StorehouseEntity e) -> !HawkOSOperator.isEmptyString(e.getHelpId()) && !e.isCollect())
				.or(e -> e.getOpenTime() > HawkTime.getMillisecond());
		resp.addAllStoreList(player.getData().getStorehouseEntities().stream()
				.filter(isShow)
				.map(o -> o.toHPMessage()).collect(Collectors.toList()));
		// 我已帮助的
		player.getData().getStorehouseHelpEntities().stream()
				.filter(e -> !e.isCollect())
				.sorted(Comparator.comparingLong(StorehouseHelpEntity::getOpenTime))
				.forEach(e -> {
					Player tarPlayer = GlobalData.getInstance().makesurePlayer(e.getTargetId());
					HPStoreHelp help = genHelpItem(tarPlayer, HelpType.HELPED, e.getId(), e.getStoreId(), e.getOpenTime());
					resp.addHelpList(help);
				});

		// 是否有帮助中的 有帮助中的其它成员宝藏锁定,不能被帮助
		long helping = player.getData().getStorehouseHelpEntities().stream()
				.filter(e -> e.getOpenTime() > HawkTime.getMillisecond())
				.count();
		GuildService.getInstance().getGuildMembers(player.getGuildId()).stream()
				.filter(mId -> !Objects.equals(mId, player.getId()))
				.map(GlobalData.getInstance()::makesurePlayer)
				.filter(Objects::nonNull)
				.forEach(tarPlayer -> {
					tarPlayer.getData().getStorehouseEntities().stream()
							.filter(Objects:: nonNull)
							.filter(e -> HawkOSOperator.isEmptyString(e.getHelpId()))
							.filter(e -> HawkTime.getMillisecond() < e.getOpenTime())
							.forEach(e -> {
								HPStoreHelp help = genHelpItem(tarPlayer, helping == 0 ? HelpType.UNHELP : HelpType.LOCKED, e.getId(), e.getStoreId(), e.getOpenTime());
								resp.addHelpList(help);
							});
				});

		player.sendProtocol(HawkProtocol.valueOf(HP.code.STORE_HOUSE_SYNC, resp));
	}

	private HPStoreHelp genHelpItem(Player tarPlayer, HelpType type, String uuid, int storeId, long openTime) {
		HPStoreHelp help = HPStoreHelp.newBuilder()
				.setPlayerId(tarPlayer.getId())
				.setLevel(tarPlayer.getLevel())
				.setName(tarPlayer.getName())
				.setOffice(GameUtil.getOfficerId(tarPlayer.getId()))
				.setVip(tarPlayer.getVipLevel())
				.setCommon(BuilderUtil.genPlayerCommonBuilder(tarPlayer))
				.setVipActive(tarPlayer.getData().getVipActivated())
				.setIcon(tarPlayer.getIcon())
				.setPfIcon(tarPlayer.getPfIcon() == null ? "" : tarPlayer.getPfIcon())
				.setStoreId(storeId)
				.setOpenTime(openTime)
				.setState(type.intValue())
				.setStoreUUID(uuid)
				.build();

		return help;
	}

	/**
	 * 通知其它联盟会员宝藏变化
	 */
	private void changeNotice() {

		GuildService.getInstance().broadcastProtocol(player.getGuildId(), HawkProtocol.valueOf(HP.code.STORE_HOUSE_UPDATE_S));
	}

}
