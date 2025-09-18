package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.IntStream;

import com.hawk.game.protocol.*;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.ItemUseResCollectBufEvent;
import com.hawk.activity.event.impl.ResToolUseEvent;
import com.hawk.game.GsConfig;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.BuffCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.CrossItemBlackListCfg;
import com.hawk.game.config.DressCfg;
import com.hawk.game.config.DressToolCfg;
import com.hawk.game.config.EffectCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.ItemExchangeCfg;
import com.hawk.game.config.PurchaseCfg;
import com.hawk.game.config.RevengeStoreGoodsCfg;
import com.hawk.game.config.ShopCfg;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.data.RevengeInfo;
import com.hawk.game.data.RevengeInfo.RevengeState;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.QuestionnaireCheckInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.DiamondConsumeInfo;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZSeasonCfg;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZService;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.item.ItemService;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.ToolType;
import com.hawk.game.protocol.Item.BuyVitResp;
import com.hawk.game.protocol.Item.ExchangeItemPB;
import com.hawk.game.protocol.Item.HPItemBuyAndUseReq;
import com.hawk.game.protocol.Item.HPItemBuyReq;
import com.hawk.game.protocol.Item.HPItemUseByItemIdReq;
import com.hawk.game.protocol.Item.HPItemUseReq;
import com.hawk.game.protocol.Item.HPResItemBatchUseReq;
import com.hawk.game.protocol.Item.HPResOutputItemBatchUseReq;
import com.hawk.game.protocol.Item.HPResOutputItemBatchUseResp;
import com.hawk.game.protocol.Item.HPShopRecord;
import com.hawk.game.protocol.Item.HPSyncItemBuyResp;
import com.hawk.game.protocol.Item.ItemExchangeReq;
import com.hawk.game.protocol.Item.ItemExchangeTimesPB;
import com.hawk.game.protocol.Item.NewlyShopItemClickedNotify;
import com.hawk.game.protocol.Item.RevengeShopBuyReq;
import com.hawk.game.protocol.Item.RevengeShopBuySuccess;
import com.hawk.game.protocol.Item.VitPurchaseInfoRespPB;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.GsConst.QuestionnaireConst;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.GiftType;
import com.hawk.log.LogConst.IMoneyType;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.sdk.msdk.entity.PayItemInfo;

/**
 * 物品模块
 *
 * @author David
 */
public class PlayerItemModule extends PlayerModule {

	static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public PlayerItemModule(Player player) {
		super(player);
	}

	/**
	 * 更新
	 *
	 * @return
	 */
	@Override
	public boolean onTick() {
		return super.onTick();
	}

	/**
	 * 玩家登陆处理(数据同步)
	 */
	@Override
	protected boolean onPlayerLogin() {
		player.getPush().syncItemInfo();
		syncClickedNewlyShopItem();
		String serverId = GsConfig.getInstance().getServerId();
		int diamonds = RedisProxy.getInstance().getImmgrationDiamonds(player.getId() + ":" + serverId);
		if (diamonds > 0) {
			RedisProxy.getInstance().removeImmgrationDiamonds(player.getId() + ":" + serverId);
			player.increaseDiamond(diamonds, Action.IMMGRATION_DIAMONDS);
		}
		
		//删除过期道具
		removeExpiredItems();
		return true;
	}
	
	/**
	 * 删除过期的道具entity
	 */
	private void removeExpiredItems() {
		try {
			int hour = HawkTime.getHour();
			int[] freeTime = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 15, 16, 17, 18, 22 };
			boolean free = IntStream.of(freeTime).filter(t -> t == hour).findAny().isPresent();
			if (!free) {
				return;
			}

			long now = HawkTime.getMillisecond();
			List<ItemEntity> removeEntries = new ArrayList<>();
			for (ItemEntity itemEntity : player.getData().getItemEntities()) {
				if (itemEntity.getItemCount() <= 0 && now - itemEntity.getUpdateTime() > HawkTime.DAY_MILLI_SECONDS) {
					removeEntries.add(itemEntity);
				}
			}

			if (removeEntries.isEmpty()) {
				return;
			}
			HawkLog.logPrintln("player login removeExpiredItems, playerId: {}, remove count: {}", player.getId(), removeEntries.size());
			player.getData().getItemEntities().removeAll(removeEntries);
			removeEntries.forEach(e -> e.delete(true));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 同步商城内新上架商品中已点击过的商品类型（已点击过的相关页签不再显示红点）
	 */
	private void syncClickedNewlyShopItem() {
		Set<Integer> newlyShopItemGroups = ShopCfg.getNewlyShopItemGroups();
		if (newlyShopItemGroups.isEmpty()) {
			return;
		}
		
		int newTerm = ConstProperty.getInstance().getShopTerm();
		int historyTerm = LocalRedis.getInstance().getShopTerm(player.getId());
		if (newTerm > historyTerm) {
			LocalRedis.getInstance().updateShopTerm(player.getId(), newTerm);
			LocalRedis.getInstance().removeClickedNewlyShopItem(player.getId());
			return;
		}
		
		Set<String> shopItemGroups = LocalRedis.getInstance().getClickedNewlyShopItem(player.getId());
		if (shopItemGroups.isEmpty()) {
			return;
		}
		
		NewlyShopItemClickedNotify.Builder builder = NewlyShopItemClickedNotify.newBuilder();
		for (String itemGroup : shopItemGroups) {
			builder.addShopItemType(Integer.valueOf(itemGroup));
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.NEWLY_SHOP_ITEM_CLICKED_PUSH, builder));
	}
	
	
	/**
	 * 背包内通过道具uuid使用道具
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ITEM_USE_C_VALUE)
	private boolean onItemUse(HawkProtocol protocol) {
		HPItemUseReq req = protocol.parseProtocol(HPItemUseReq.getDefaultInstance());
		final int itemCount = req.getItemCount();
		if (itemCount <= 0) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			logger.error("use item, param invalid, playerId: {}, itemId: {}, itemCount: {}", player.getId(), req.getUuid(), itemCount);
			return false;
		}
		
		final String uuid = req.getUuid();
		final String targetId = req.getTargetId();
		ConsumeItems consume = ConsumeItems.valueOf();
		if (!itemUseCheck(protocol, consume, uuid, itemCount, targetId)) {
			return false;
		}

		if (!consume.checkConsume(player, protocol.getType())) {
			logger.error("use item, item not enough, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.ITEM_NOT_ENOUGH);
			return false;
		}
		
		ItemEntity itemEntity = player.getData().getItemById(uuid);
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemEntity.getItemId());
		if (checkToolBackGoldLimit(itemCfg, itemCount)) {
			sendError(protocol.getType(), Status.Error.TOOL_BACK_GOLD_LIMIT_ERROR);
			return false;
		}
		if(specialCheck(player, itemCfg, itemCount, protocol.getType(), targetId)){
			return false;
		}
		consume.consumeAndPush(player, GameUtil.itemTypeToAction(itemCfg)); // 背包内使用道具
		// 添加道具效果
		addItemEffect(itemCfg, itemCount, targetId);

		if (itemCfg.isNotify()) {
			player.responseSuccess(protocol.getType());
		}

		return true;
	}
	
	/**
	 * 批量使用资源田增产道具
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.RES_OUTPUT_ITEM_BATCH_USE_C_VALUE)
	private boolean onResOutputItemBatchUse(HawkProtocol protocol) {
		HPResOutputItemBatchUseReq req = protocol.parseProtocol(HPResOutputItemBatchUseReq.getDefaultInstance());
		int buildingType = req.getBuildingType();
		
		// 不是资源田建筑类型
		if (!BuildingCfg.isResBuildingType(buildingType)) {
			sendError(protocol.getType(), Status.Error.RES_BUILDING_TYPE_NEED);
			logger.error("batch use resOutput item, resBuilding type error, playerId: {}, buildingType: {}", player.getId(), buildingType);
			return false;
		}
		
		// 还没建造过该类资源田
		List<BuildingBaseEntity> buildingEntities = player.getData().getBuildingListByType(BuildingType.valueOf(buildingType));
		if (buildingEntities.isEmpty()) {
			sendError(protocol.getType(), Status.Error.RES_BUILDING_COUNT_ZERO);
			logger.error("batch use resOutput item, no building exist, playerId: {}, buildingType: {}", player.getId(), buildingType);
			return false;
		}

		// 所有需要增产的
		List<String> targetIds = new ArrayList<String>();
		for (BuildingBaseEntity buildingEntity : buildingEntities) {
			targetIds.add(buildingEntity.getId());
		}

		// 道具数量
		int itemCount = buildingEntities.size();
		// 道具id
		int itemId = BuildingCfg.getIncreaseProItem(buildingType);
		
		// 消耗
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addItemConsume(itemId, itemCount, req.getUseGold());
		if (!consume.checkConsume(player, protocol.getType())) {
			sendError(protocol.getType(), Status.Error.ITEM_NOT_ENOUGH);
			logger.error("batch use resOutput item not enough, playerId: {}, buildingType: {}", player.getId(), buildingType);
			return false;
		}
		consume.consumeAndPush(player, Action.RES_BUILDING_OUTPUT_INC);
		
		// 添加道具效果
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		for (String targetId : targetIds) {
			addItemEffect(itemCfg, 1, targetId);
		}
		
		HPResOutputItemBatchUseResp.Builder resp = HPResOutputItemBatchUseResp.newBuilder();
		resp.setBuildingType(buildingType);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.RES_OUTPUT_ITEM_BATCH_USE_S, resp));
		return true;
	}

	/**
	 * 批量使用资源道具，通过uuid
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.RES_ITEM_BATCH_USE_C_VALUE)
	private boolean onResItemBatchUse(HawkProtocol protocol) {
		HPResItemBatchUseReq reqbatch = protocol.parseProtocol(HPResItemBatchUseReq.getDefaultInstance());
		ConsumeItems consume = ConsumeItems.valueOf();
		AwardItems award = AwardItems.valueOf();
		int totalItemCount = 0;
		boolean isNotify = true;
		Map<ItemCfg, Integer> overflowAttrResMap = new HashMap<ItemCfg, Integer>(4);
		for (HPItemUseReq req : reqbatch.getResItemsList()) {
			final String uuid = req.getUuid();
			final int itemCount = req.getItemCount();
			final String targetId = req.getTargetId();
			if (!itemUseCheck(protocol, consume, uuid, itemCount, targetId)) {
				return false;
			}

			ItemEntity itemEntity = player.getData().getItemById(uuid);
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemEntity.getItemId());
			if(specialCheck(player, itemCfg, itemCount, protocol.getType(), targetId)){
				return false;
			}
			if (itemCfg.getItemType() == Const.ToolType.ADD_ATTR_VALUE) {
				long total = 1L * itemCfg.getAttrVal() * itemCount;
				if (total >= Integer.MAX_VALUE) {
					overflowAttrResMap.put(itemCfg, itemCount);
				} else {
					ItemInfo item = new ItemInfo(ItemType.PLAYER_ATTR_VALUE, itemCfg.getAttrType(), itemCfg.getAttrVal() * itemCount);
					award.addItem(item);
				}
			} else if (itemCfg.getItemType() == Const.ToolType.REWARD_VALUE) {
				int count = itemCount;
				AwardCfg awardCfg =  HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, itemCfg.getRewardId());
				if(awardCfg != null){
					AwardItems awardItems = awardCfg.getRandomAward();
					if (!awardItems.isResourceItem()) {
						return false;
					}
					
					award.appendAward(awardItems);
					count--;
					while(count > 0) {
						AwardItems items = awardCfg.getRandomAward();
						if(items != null) {
							award.appendAward(items);
						}
						count--;
					}
				}
				
			} else {
				return false;
			}
			
			totalItemCount += itemCount;
			
			if (isNotify) {
				isNotify = itemCfg.isNotify();
			}
		}

		if (totalItemCount <= 0) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			logger.error("batch use item, param invalid, playerId: {}, total count: {}", player.getId(), totalItemCount);
			return false;
		}
		
		if (!consume.checkConsume(player, protocol.getType())) {
			logger.error("batch use item, item not enough, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.ITEM_NOT_ENOUGH);
			return false;
		}
		
		consume.consumeAndPush(player, Action.USE_RESOURCE_ITEM);
		if (award.hasAwardItem()) {
			award.rewardTakeAffectAndPush(player, Action.USE_RESOURCE_ITEM, true, RewardOrginType.ITEM_BATCH_RESOURCE);
		}
		
		for (Entry<ItemCfg, Integer> entry : overflowAttrResMap.entrySet()) {
			GameUtil.addPlayeAttr(player, entry.getKey(), entry.getValue(), "", Action.USE_RESOURCE_ITEM);
		}
		
		ActivityManager.getInstance().postEvent(new ResToolUseEvent(player.getId(), totalItemCount));
		if (isNotify) {
			player.responseSuccess(protocol.getType());
		}
		
		return true;
	}

	/**
	 * 通过uuid使用道具条件判断
	 * 
	 * @param protocol
	 * @param consume
	 * @param uuid
	 * @param itemCount
	 * @param targetId
	 * @return
	 */
	private boolean itemUseCheck(HawkProtocol protocol, ConsumeItems consume, final String uuid, final int itemCount, final String targetId) {
		HawkAssert.checkPositive(itemCount);
		ItemEntity itemEntity = player.getData().getItemById(uuid);
		// 不存在此物品
		if (itemEntity == null) {
			logger.error("use item, item not found, playerId: {}, itemUUid: {}", player.getId(), uuid);
			sendError(protocol.getType(), Status.Error.ITEM_NOT_FOUND);
			player.getPush().syncItemInfo();
			return false;
		}

		// 道具不足
		if (itemEntity.getItemCount() < itemCount) {
			logger.error("use item, item not enough, playerId: {}, itemId: {}, itemCount: {}, useCount: {}",
					player.getId(), itemEntity.getItemId(), itemEntity.getItemCount(), itemCount);
			sendError(protocol.getType(), Status.Error.ITEM_NOT_ENOUGH);
			return false;
		}

		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemEntity.getItemId());
		// 不满足使用条件
		if (!itemUseCheck(itemCfg, itemEntity.getItemId(), itemCount, protocol.getType(), targetId)) {
			return false;
		}

		// 消耗道具
		consume.addConsumeInfo(ItemType.TOOL, itemEntity.getId(), itemEntity.getItemId(), itemCount);
		return true;
	}

	/**
	 * 背包外通过itemId使用道具
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ITEM_USE_BY_ITEMID_C_VALUE)
	private boolean onItemUseByItemId(HawkProtocol protocol) {
		HPItemUseByItemIdReq req = protocol.parseProtocol(HPItemUseByItemIdReq.getDefaultInstance());
		final int itemId = req.getItemId();
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.ITEM_GET, itemId)) {
			logger.error("item get closed, itemId: {}", itemId);
			sendError(protocol.getType(), Status.SysError.ITEM_GET_OFF);
			return false;
		}

		final int itemCount = req.getItemCount();
		if (itemCount <= 0) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			logger.error("use item by itemId, param invalid, playerId: {}, itemId: {}, itemCount: {}", player.getId(), itemId, itemCount);
			return false;
		}

		// 不存在此物品
		List<ItemEntity> items = player.getData().getItemsByItemId(itemId);
		if (items == null || items.size() == 0) {
			logger.error("use item by itemId, item not found, playerId: {}, itemId: {}", player.getId(), itemId);
			sendError(protocol.getType(), Status.Error.ITEM_NOT_FOUND);
			return false;
		}

		// 计算消耗
		int needCount = itemCount;
		Map<String, Integer> needItems = new HashMap<String, Integer>();
		for (ItemEntity entity : items) {
			int disCount = Math.min(needCount, entity.getItemCount());
			needItems.put(entity.getId(), disCount);
			needCount -= disCount;
			if (needCount == 0) {
				break;
			}
		}

		// 道具不足
		if (needCount > 0) {
			logger.error("use item by itemId, item not enough, playerId: {}, itemId: {}, needCount: {}", player.getId(),
					itemId, needCount);
			sendError(protocol.getType(), Status.Error.ITEM_NOT_ENOUGH);
			return false;
		}

		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		String targetId = req.hasTargetId() ? req.getTargetId() : "";
		// 不满足使用条件
		if (!itemUseCheck(itemCfg, itemId, itemCount, protocol.getType(), targetId)) {
			return false;
		}
		if(specialCheck(player, itemCfg, itemCount, protocol.getType(), targetId)){
			return false;
		}
		// 消耗道具
		if (!consumeItemByItemId(itemCfg, itemCount, needItems, protocol.getType())) {
			return false;
		}

		// 添加道具效果
		addItemEffect(itemCfg, itemCount, targetId);
		
		if (itemCfg.isNotify()) {
			player.responseSuccess(protocol.getType());
		}
		
		return true;
	}

	/**
	 * 购买使用道具（购买基地增益、购买体力、资源田增产）
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.BUY_AND_USE_C_VALUE)
	private boolean onItemBuyAndUse(HawkProtocol protocol) {
		HPItemBuyAndUseReq req = protocol.parseProtocol(HPItemBuyAndUseReq.getDefaultInstance());
		final int itemCount = req.getItemCount();
		if (itemCount <= 0) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			logger.error("buy use item, param invalid, playerId: {}, itemId: {}, itemCount: {}", player.getId(), req.getItemId(), itemCount);
			return false;
		}
		
		final int itemId = req.getItemId();
		
		// 购买体力
		if (itemId == PlayerAttr.VIT_VALUE) {
			buyVit(protocol.getType(), itemCount);
			return true;
		}

		// 零收益状态，不能购买
		if (player.isZeroEarningState()) {
			sendError(protocol.getType(), Status.SysError.ZERO_EARNING_STATE);
			//player.sendIDIPZeroEarningMsg();
			return false;
		}

		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		String targetId = req.hasTargetId() ? req.getTargetId() : "";
		// 不满足使用条件
		if (!itemUseCheck(itemCfg, itemId, itemCount, protocol.getType(), targetId)) {
			return false;
		}
		if(specialCheck(player, itemCfg, itemCount, protocol.getType(), targetId)){
			return false;
		}
		// 消耗货币
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(PlayerAttr.GOLD, itemCount * itemCfg.getSellPrice());
		if (!consume.checkConsume(player, protocol.getType())) {
			logger.error("buy use item, gold not enough, playerId: {}, itemId: {}", player.getId(), itemId);
			return false;
		}
		
		consume.consumeAndPush(player, getGoldUseAction(itemCfg, targetId));

		// 添加道具效果
		addItemEffect(itemCfg, itemCount, targetId);
		if (itemCfg.isNotify()) {
			player.responseSuccess(protocol.getType());
		}
		return true;
	}
	
	/**
	 * 获取水晶消耗Action
	 * 
	 * @param itemCfg
	 * @param targetId
	 */
	private Action getGoldUseAction(ItemCfg itemCfg, String targetId) {
		switch (itemCfg.getItemType()) {
			case Const.ToolType.STATUS_VALUE: {
				if (player.getData().getBuildingBaseEntity(targetId) != null) {
					return Action.RES_BUILDING_OUTPUT_INC;
				}
				
				return Action.USE_GOLD_BUY_STATUS;
			}
			
			default:
				return Action.TOOL_BUY;
		}
	}

	/**
	 * 道具使用条件校验
	 * 
	 * @param itemCfg
	 * @param itemId
	 * @param itemCount
	 * @param protoType
	 * @return
	 */
	private boolean itemUseCheck(ItemCfg itemCfg, int itemId, int itemCount, int protoType, String targetId) {
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.ITEM_USE, itemId)) {
			logger.error("item use closed, playerId: {}, itemId: {}", player.getId(), itemId);
			sendError(protoType, Status.SysError.ITEM_USE_OFF);
			return false;
		}

		if (itemCfg == null) {
			logger.error("use item, item config error, playerId: {}, itemId: {}, protocol: {}", player.getId(), itemId, protoType);
			sendError(protoType, Status.SysError.CONFIG_ERROR);
			return false;
		}
		
		// 不能批量使用时，要使用的个数大于一
		if (itemCfg.getUseAll() == 0 && itemCount > 1) {
			HawkLog.errPrintln("use item, use single one time, playerId: {}, itemId: {}, itemCount: {}, protocol: {}", player.getId(), itemCfg.getId(), itemCount, protoType);
			player.sendError(protoType, Status.Error.ITEM_USE_COUNT_ERROR_VALUE, 0);
			return false;
		}

		// 大本等级不满足
		if (player.getCityLevel() < itemCfg.getCityLevel()) {
			HawkLog.errPrintln("use item, city level not enough, playerId: {}, itemId: {}, player cityLevel: {}, config cityLevel: {}, protocol: {}",
					player.getId(), itemCfg.getId(), player.getCityLevel(), itemCfg.getCityLevel(), protoType);
			player.sendError(protoType, Status.Error.CITY_LEVEL_NOT_ENOUGH_VALUE, 0);
			return false;
		}
		
		//玩家在跨服,判断跨服表是否禁用该物品
		if(player.isCsPlayer()){
			CrossItemBlackListCfg crossBlackCfg = HawkConfigManager.getInstance().getConfigByKey(CrossItemBlackListCfg.class, itemCfg.getId());
			if (crossBlackCfg != null) {
				player.sendError(protoType, Status.Error.ITEM_CAN_NOT_USE_IN_CROSS_VALUE, 0);
				return false;
			}
		}

		return ItemService.getInstance().itemUseCheck(player, itemCfg, itemCount, protoType, targetId); //TODO
	}

	/**
	 * 商品购买记数
	 */
	@ProtocolHandler(code = HP.code.SYNC_ITEM_BUY_C_VALUE)
	private boolean onSyncItemBought(HawkProtocol protocol) {
		final int[] allShops = HawkConfigManager.getInstance().getConfigIterator(ShopCfg.class).stream()
				.mapToInt(ShopCfg::getId).toArray();

		HPSyncItemBuyResp.Builder resp = HPSyncItemBuyResp.newBuilder();
		LocalRedis.getInstance().shopBuyCountDay(player.getId(), allShops).entrySet().stream()
				.filter(e -> e.getValue() > 0)
				.map(e -> HPShopRecord.newBuilder().setShopId(e.getKey()).setCount(e.getValue()))
				.forEach(resp::addShopRecord);
		LocalRedis.getInstance().shopBuyCountTotal(player.getId(), allShops).entrySet().stream()
				.filter(e -> e.getValue() > 0)
				.map(e -> HPShopRecord.newBuilder().setShopId(e.getKey()).setCount(e.getValue()))
				.forEach(resp::addTotalRecord);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SYNC_ITEM_BUY_S, resp));
		return true;
	}
	
	/**
	 * 客户端通知服务器，热销商品中新上架的商品红点消失（已点击）
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.NEWLY_SHOP_ITEM_CLICKED_C_VALUE)
	private boolean onNewlyShopItemClicked(HawkProtocol protocol) {
		NewlyShopItemClickedNotify data = protocol.parseProtocol(NewlyShopItemClickedNotify.getDefaultInstance());
		if (data.getShopItemTypeList().isEmpty()) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}
		
		// 客户端在线的情况下不可能更新客户端配置，所以这里不用判断热销商品配置期数，在登录时统一判断期数即可
		Set<Integer> newlyShopItemGroups = ShopCfg.getNewlyShopItemGroups();
		if (!newlyShopItemGroups.isEmpty()) {
			List<String> validTypes = new ArrayList<String>(); 
			for (Integer type : data.getShopItemTypeList()) {
				if (newlyShopItemGroups.contains(type)) {
					validTypes.add(String.valueOf(type));
				}
			}
			
			if (!validTypes.isEmpty()) {
				LocalRedis.getInstance().addClickedNewlyShopItem(player.getId(), validTypes.toArray(new String[validTypes.size()]));
			}
		}
		
		player.responseSuccess(protocol.getType());
		
		return true;
	} 

	/**
	 * 购买道具（热销商品、资源道具等）
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ITEM_BUY_C_VALUE)
	private boolean onItemBuy(HawkProtocol protocol) {
		HPItemBuyReq req = protocol.parseProtocol(HPItemBuyReq.getDefaultInstance());
		final int shopId = req.getShopId();
		// 热销商品关闭
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.SALES_GOODS, shopId)) {
			logger.error("sales goods closed, goodsId: {}", shopId);
			sendError(protocol.getType(), Status.SysError.SALES_GOODS_OFF);
			return false;
		}

		final int shopCount = req.getItemCount();
		if (shopCount <= 0) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			logger.error("shop buy item param invalid, playerId: {}, shopId: {}, count: {}", player.getId(), shopId, shopCount);
			return false;
		}

		ShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(ShopCfg.class, shopId);
		// 商品配置不存在
		if (shopCfg == null) {
			logger.error("shopCfg config error, playerId: {}, shopId: {}", player.getId(), shopId);
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}
		
		long now = HawkTime.getMillisecond(), showTime = shopCfg.getShowTimeStamp(), hiddenTime = shopCfg.getHiddenTimeStamp();
		if (showTime > 0 && (now < showTime || now > hiddenTime)) {
			logger.error("shopItem not in sellTime, playerId: {}, shopId: {}", player.getId(), shopId);
			sendError(protocol.getType(), Status.Error.SELL_ITEM_INVALID);
			return false;
		}

		// 限购商品条件判断
		int dailyLimitCount = shopCfg.getBuyCount();
		int totalLimit = shopCfg.getMaxBuyTimes();
		int buyCountToday = LocalRedis.getInstance().shopBuyCountDay(player.getId(), shopId).get(shopId);
		int buyCountTotal = LocalRedis.getInstance().shopBuyCountTotal(player.getId(), shopId).get(shopId);
		if (buyCountToday + shopCount > dailyLimitCount || buyCountTotal + shopCount > totalLimit) {
			logger.error("shop buy item count exceed, playerId: {}, itemCount: {}, daily limit: {}, total limit: {}, already buy today: {}, already buy total: {}",
					player.getId(), shopCount, dailyLimitCount, totalLimit, buyCountToday, buyCountTotal);
			sendError(protocol.getType(), Status.Error.ITEM_BUY_COUNT_EXCEED);
			return false;
		}

		// 商品当前不可出售
		if (shopCfg.getIsUse() == 0) {
			logger.error("shop buy item error, playerId: {}, shopId: {}, useable: {}", player.getId(), shopId,
					shopCfg.getIsUse());
			sendError(protocol.getType(), Status.Error.GOODS_CAN_NOT_SELL);
			return false;
		}

		// 玩家大本等级不够无法购买此商品
		if (player.getCityLv() < shopCfg.getBuyLV()) {
			logger.error("shop buy item error, playerId: {}, shopId: {}, playerCityLevel: {}, needLevel: {}",
					player.getId(), shopId, player.getCityLv(), shopCfg.getBuyLV());
			sendError(protocol.getType(), Status.Error.PLAYER_LEVEL_NOT_ENOUGH);
			return false;
		}

		int itemId = shopCfg.getShopItemID();
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.ITEM_GET, itemId)) {
			logger.error("item get closed, itemId: {}", itemId);
			sendError(protocol.getType(), Status.SysError.ITEM_GET_OFF);
			return false;
		}

		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		// 物品配置不存在
		if (itemCfg == null) {
			logger.error("shop buy item config error, playerId: {}, shopId: {}, itemId: {}", player.getId(), shopId,
					itemId);
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}

		// 消耗货币发放物品
		ItemInfo addItem = new ItemInfo(Const.ItemType.TOOL_VALUE, itemId, shopCount * shopCfg.getNumber());
		ItemInfo consumeItem = shopCfg.getPriceItemInfo();
		int costMoney = (int) (consumeItem.getCount() * shopCount);
		
		RewardOrginType orginType = req.hasType() ? req.getType() : null;
		if (!consumeAndReward(addItem, shopCount, consumeItem, Action.TOOL_BUY, protocol.getType(), orginType, shopCfg.getExtraItemList())) {
			logger.error("buy item consume error, playerId: {}, shopId: {}", player.getId(), shopId);
			return false;
		}

		player.dealMsg(MsgId.ITEM_BUY_QUESTIONAIRE_CHECK, new QuestionnaireCheckInvoker(player, QuestionnaireConst.CONDITION_BUILDING_BUY_ITEM, itemId));

		player.responseSuccess(protocol.getType());
		LocalRedis.getInstance().incrementShopBuyCount(player.getId(), shopId, shopCount);
		// 游戏商城道具购买打点日志
		LogUtil.logGiftBagFlow(player, GiftType.SHOPPING_MALL_ITEM, String.valueOf(shopId), costMoney, consumeItem.getItemId(), itemCfg.getItemType(), shopCount);
		
		logger.debug("player buy item, playerId: {}, shopId: {}, cityLvl: {}, exp: {}, vipLvl: {}", player.getId(), shopId, shopCount, player.getCityLevel(), player.getExp(), player.getVipLevel());
		//客户端买完之后再请求还不如服务器主动推.
		onSyncItemBought(null);
		
		return true;
	}
	
	/**
	 * 购买大R复仇折扣商品
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.REVENGE_SHOP_BUY_C_VALUE)
	private boolean onRevengeShopBuy(HawkProtocol protocol) {
		if (ConstProperty.getInstance().getRevengeShopOpen() <= 0) {
    		return false;
    	}
		
		RevengeInfo revengeInfo = player.getRevengeInfo(true);
		if (revengeInfo == null || revengeInfo.getState() != RevengeState.ON) {
			sendError(protocol.getType(), Status.Error.REVENGE_NOT_OPEN);
			HawkLog.errPrintln("onRevengeShopBuy error, playerId: {}, revenge state: {}", player.getId(), revengeInfo == null ? "null" : revengeInfo.getState());
			return false;
		}
		
		RevengeShopBuyReq req = protocol.parseProtocol(RevengeShopBuyReq.getDefaultInstance());
		final int shopCount = req.getCount();
		if (shopCount <= 0) {
			sendError(protocol.getType(), Status.Error.REVENGE_SHOP_COUNT_ERROR);
			HawkLog.errPrintln("onRevengeShopBuy error, playerId: {}, shopCount: {}", player.getId(), shopCount);
			return false;
		}

		final int shopId = req.getShopId();
		RevengeStoreGoodsCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(RevengeStoreGoodsCfg.class, shopId);
		if (shopCfg == null) {
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			HawkLog.errPrintln("onRevengeShopBuy config error, playerId: {}, shopId: {}", player.getId(), shopId);
			return false;
		}
		
		// 损失兵的数量未达到该档次
		if (revengeInfo.getDeadSoldierTotal() < shopCfg.getLossTroopsNum()) {
			sendError(protocol.getType(), Status.Error.REVENGE_SHOP_CONDITION_ERROR);
			HawkLog.errPrintln("onRevengeShopBuy error, playerId: {}, shopId: {}, loss troop: {}, config troop: {}", 
					player.getId(), shopId, revengeInfo.getDeadSoldierTotal(), shopCfg.getLossTroopsNum());
			return false;
		}
		
		Map<Integer, Integer> shopBuyInfo = player.getRevengeShopBuyInfo();
		int alreadyBuyCount = shopBuyInfo.containsKey(shopId) ? shopBuyInfo.get(shopId) : 0;
		alreadyBuyCount += shopCount;
		// 商品购买数量超上限
		if (alreadyBuyCount > shopCfg.getLimitNum()) {
			sendError(protocol.getType(), Status.Error.REVENGE_BUY_COUNT_EXCEED);
			HawkLog.errPrintln("onRevengeShopBuy error, playerId: {}, shopId: {}, config limitCount: {}, alreadyBuyCount: {}, this time count: {}", 
					player.getId(), shopId, shopCfg.getLimitNum(), alreadyBuyCount - shopCount, shopCount);
			return false;
		}
		
		int itemId = shopCfg.getItemId();
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.ITEM_GET, itemId)) {
			sendError(protocol.getType(), Status.SysError.ITEM_GET_OFF);
			HawkLog.errPrintln("onRevengeShopBuy error, item get closed, playerId: {}, shopId: {}, itemId: {}", player.getId(), shopId, itemId);
			return false;
		}

		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		if (itemCfg == null) {
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			HawkLog.errPrintln("onRevengeShopBuy config error, playerId: {}, shopId: {}, itemId: {}", player.getId(), shopId, itemId);
			return false;
		}

		// 消耗货币发放物品
		ItemInfo addItem = new ItemInfo(Const.ItemType.TOOL_VALUE, itemId, shopCount);
		ItemInfo consumeItem = ItemInfo.valueOf(shopCfg.getPrice());
		long costMoney = consumeItem.getCount() * shopCount;
		consumeItem.setCount(costMoney);
		if (!consumeAndReward(addItem, 1, consumeItem, Action.REVENGE_SHOP_BUY, protocol.getType())) {
			HawkLog.errPrintln("onRevengeShopBuy consume error, playerId: {}, shopId: {}, consume: {}", player.getId(), shopId, consumeItem);
			return false;
		}
		
		RevengeShopBuySuccess.Builder builder = RevengeShopBuySuccess.newBuilder();
		builder.setShopId(shopId);
		builder.setCount(shopCount);
		sendProtocol(HawkProtocol.valueOf(HP.code.REVENGE_SHOP_BUY_S, builder));

		shopBuyInfo.put(shopId, alreadyBuyCount);
		long startTime = revengeInfo.getStartTime() > 0 ? revengeInfo.getStartTime() : HawkApp.getInstance().getCurrentTime();
		long expireTime = startTime + ConstProperty.getInstance().getRevengeShopRefresh() - HawkApp.getInstance().getCurrentTime();
		RedisProxy.getInstance().updateRevengeShopBuyInfo(player.getId(), shopId, alreadyBuyCount, (int) (expireTime/1000) - 1);
		
		LogUtil.logGiftBagFlow(player, GiftType.REVENGE_STORE_ITEM, String.valueOf(shopId), (int)costMoney, consumeItem.getItemId(), itemCfg.getItemType(), shopCount);
		HawkLog.logPrintln("onRevengeShopBuy success, playerId: {}, shopId: {}, count: {}", player.getId(), shopId, shopCount);
		return true;
	}

	/**
	 * 打开背包
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ITEM_OPEN_BAG_C_VALUE)
	private boolean openBag(HawkProtocol protocol) {
		// 清除新道具标签
		List<ItemEntity> items = player.getData().getItemEntities();
		for (ItemEntity item : items) {
			if (!item.isNew()) {
				continue;
			}
			ItemCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, item.getItemId());
			// 装备材料单独处理
			if (cfg != null && cfg.getItemType() != ToolType.EQUIP_MATERIAL_VALUE) {
				item.setNew(false);
			} else if(cfg == null) {
				HawkLog.errPrintln("player openBag error, playerId: {}, itemdId: {}", player.getId(), item.getItemId());
			}
		}
		return true;
	}
	
	/**
	 * 玩家进入体力购买界面时先请求已购买信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.VIT_PURCHASE_INFO_C_VALUE)
	private boolean vitPurchaseInfoReq(HawkProtocol protocol) {
		VitPurchaseInfoRespPB.Builder builder = VitPurchaseInfoRespPB.newBuilder();
		int buyVitTimes = Math.max(0, player.getData().getVitBuyTimesToday());
		builder.setBuyVitTimes(buyVitTimes);
		if (buyVitTimes > 0) {
			builder.setCrossDayTime(HawkTime.getNextAM0Date());
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.VIT_PURCHASE_INFO_S, builder));
		
		return true;
	}

	/**
	 * 购买体力
	 * 
	 * @param protoType
	 */
	private void buyVit(int protoType, int buyCount) {
		if(player.isCsPlayer()){
			try {
				DYZZWar.PBDYZZWarState state = DYZZService.getInstance().getDYZZWarState();
				if(state == DYZZWar.PBDYZZWarState.DYZZ_OPEN || state == DYZZWar.PBDYZZWarState.DYZZ_CLOSE ){
					logger.error("buy vit req times error dyzz, playerId: {}", player.getId());
					sendError(protoType, Status.Error.PLAYER_IN_INSTANCE);
					return;
				}
			}catch (Exception e){
				HawkException.catchException(e);
			}
		}
		//可选择购买次数上限 = min（水晶限制购买次数，实际体力上限限制购买次数）
		//实际体力上限限制购买次数 = int（（实际体力上限 - 当前体力值）/单次购买增加体力值）
		int vitCountOnce = ConstProperty.getInstance().getBuyEnergyAdd();
		int actualCount = (int) Math.ceil((ConstProperty.getInstance().getActualVitLimit() - player.getVit()) * 1D / vitCountOnce);
		if (actualCount == 0 && ConstProperty.getInstance().getActualVitLimit() - player.getVit() > 0) {
			actualCount = 1;
		}
		
		if (buyCount > actualCount || buyCount < 1) {
			logger.error("buy vit req times error, playerId: {}, req times: {}, actual times: {}, player vit: {}", player.getId(), buyCount, actualCount, player.getVit());
			sendError(protoType, Status.Error.VIT_BUY_COUNT_ERROR);
			return;
		}
		
		// 获取当日已购买的次数
		int buyTimes = player.getData().getVitBuyTimesToday();
		// 跨天了
		if (buyTimes < 0) {
			player.getPush().syncPlayerInfo();
			sendError(protoType, Status.Error.VIT_BUYTIMES_ANOTHERDAY_RESET);
			return;
		}
		
		// 判断当日花费金币购买体力次数是否超上限
		if (buyTimes + buyCount > ConstProperty.getInstance().getDailyBuyEnergyTimesLimit()) {
			sendError(protoType, Status.Error.DAILY_BUYVIT_TIMES_LIMIT_ERROR);
			return;
		}
		
		ConsumeItems consume = ConsumeItems.valueOf();
		for (int i = buyTimes + 1; i <= buyTimes + buyCount; i++) {
			int cfgId = Math.min(i, PurchaseCfg.getMaxTime());
			PurchaseCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PurchaseCfg.class, cfgId);
			if (cfg == null) {
				logger.error("buy vit purchase config error, playerId: {}, cfgId: {}", player.getId(), cfgId);
				sendError(protoType, Status.SysError.CONFIG_ERROR);
				return;
			}
			
			consume.addConsumeInfo(cfg.getConsumeItem(), false);
 		}
		
		if (!consume.checkConsume(player, protoType)) {
			logger.error("buy vit consume error, playerId: {}, buyTimes: {}, buyCount: {}", player.getId(), buyTimes, buyCount);
			return;
		}
		
		consume.consumeAndPush(player, Action.BUY_VIT);

		// 发放体力
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItem(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIT_VALUE, vitCountOnce * buyCount);
		awardItem.rewardTakeAffectAndPush(player, Action.BUY_VIT);
		
		LocalRedis.getInstance().updateBuyVitTimes(player.getId(), buyTimes + buyCount);
		player.getData().updateVitBuyTimesInfo(buyTimes + buyCount);
		BuyVitResp.Builder builder = BuyVitResp.newBuilder();
		builder.setBuyCount(buyCount);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.BUY_VIT_S, builder));
		player.responseSuccess(protoType);
		logger.debug("buy vit finish, playerId: {}, player vit: {}", player.getId(), player.getVit());
	}
	
	/**
	 * 物品兑换
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ITEM_EXCHANGE_C_VALUE)
	private boolean onItemExchange(HawkProtocol protocol) {
		ItemExchangeReq req = protocol.parseProtocol(ItemExchangeReq.getDefaultInstance());
		final int cfgId = req.getExchangeCfgId();
		final int count = req.getExchangeCount();
		HawkAssert.checkPositive(count);
		
		ItemExchangeCfg exchangeCfg = HawkConfigManager.getInstance().getConfigByKey(ItemExchangeCfg.class, cfgId);
		if (exchangeCfg == null) {
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			logger.error("ItemExchangeCfg config not found, playerId: {}, exchangeId: {}", player.getId(), cfgId);
			return false;
		}
		
		List<ItemInfo> exchangeFromItems = exchangeCfg.getExchangeFromItems();
		if (exchangeFromItems.isEmpty()) {
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			logger.error("ItemExchangeCfg config exchangeFromItems empty, playerId: {}, exchangeId: {}", player.getId(), cfgId);
			return false;
		}
		
		int exchangeTimesLimit = exchangeCfg.getCanExchangetimes();
		int exchangeTimes = 0;
		Map<Integer, Integer> itemExchangeInfoMap = fetchItemExchangeInfo();
		if (exchangeTimesLimit > 0) {
			exchangeTimes = itemExchangeInfoMap.containsKey(cfgId) ? itemExchangeInfoMap.get(cfgId) : 0;
			// 超出上限了，拦截掉
			if (exchangeTimes + count > exchangeTimesLimit) {
				sendError(protocol.getType(), Status.Error.ITEM_EXCHANGE_TIME_LIMIT);
				logger.error("ItemExchange error, playerId: {}, exchangeId: {}, exchangeTimes: {}, timeLimit: {}", player.getId(), cfgId, exchangeTimes, exchangeTimesLimit);
				return false;
			}
		}
		
		for (ItemInfo itemInfo : exchangeFromItems) {
			itemInfo.setCount(itemInfo.getCount() * count);
		}
		
		ConsumeItems consumeItem = ConsumeItems.valueOf();
		consumeItem.addConsumeInfo(exchangeFromItems);
		
		if (!consumeItem.checkConsume(player, protocol.getType())) {
			logger.error("ItemExchange consume error, playerId: {}, exchangeId: {}", player.getId(), cfgId);
			return false;
		}
		
		consumeItem.consumeAndPush(player, Action.ITEM_EXCHANGE);
		
		List<ItemInfo> exchangeToItems = exchangeCfg.getExchangeToItems();
		for (ItemInfo itemInfo : exchangeToItems) {
			itemInfo.setCount(itemInfo.getCount() * count);
		}
		
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItemInfos(exchangeToItems);
		awardItem.rewardTakeAffectAndPush(player, Action.ITEM_EXCHANGE, true, RewardOrginType.SHOPPING_GIFT);
		
		if (exchangeTimesLimit > 0) {
			itemExchangeInfoMap.put(cfgId, exchangeTimes + count);
			LocalRedis.getInstance().addItemExchangeTimes(player.getId(), String.valueOf(cfgId), count);
		}
		
		ItemExchangeTimesPB.Builder resp = ItemExchangeTimesPB.newBuilder();
		ExchangeItemPB.Builder inner = ExchangeItemPB.newBuilder();
		inner.setExchangeCfgId(cfgId);
		inner.setExchangeTimes(exchangeTimes + count);
		resp.addExchangeItem(inner);
		sendProtocol(HawkProtocol.valueOf(HP.code.ITEM_EXCHANGE_TIMES_PUSH, resp));
		
		return true;
	}
	
	/**
	 * 请求物品兑换信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.ITEM_EXCHANGE_TIMES_C_VALUE)
	private boolean fetchItemExchangeInfo(HawkProtocol protocol) {
		Map<Integer, Integer> itemExchangeInfoMap = fetchItemExchangeInfo();
		ItemExchangeTimesPB.Builder resp = ItemExchangeTimesPB.newBuilder();
		for (Entry<Integer, Integer> entry : itemExchangeInfoMap.entrySet()) {
			if (entry.getKey() <= 0) {
				continue;
			}
			
			ExchangeItemPB.Builder inner = ExchangeItemPB.newBuilder();
			inner.setExchangeCfgId(entry.getKey());
			inner.setExchangeTimes(entry.getValue());
			resp.addExchangeItem(inner);
		}
		
		sendProtocol(HawkProtocol.valueOf(HP.code.ITEM_EXCHANGE_TIMES_PUSH, resp));
		return true;
	}
	
	/**
	 * 获取物品兑换信息缓存数据
	 * 
	 * @return
	 */
	private Map<Integer, Integer> fetchItemExchangeInfo() {
		Map<Integer, Integer> itemExchangeInfoMap = player.getData().getItemExchangeInfo();
		// 内存缓存为空时，从redis中获取
		if (itemExchangeInfoMap.isEmpty()) {
			Map<String, String> result = LocalRedis.getInstance().getItemExchangeTimesAll(player.getId());
			// redis缓存也为空时，put一个默认的，防止重复访问redis
			if (result.isEmpty()) {
				itemExchangeInfoMap.put(0, 0);
			} else {
				for (Entry<String, String> entry : result.entrySet()) {
					Integer cfgId = Integer.valueOf(entry.getKey());
					Integer count = Integer.valueOf(entry.getValue());
					itemExchangeInfoMap.put(cfgId, count);
				}
			}
		}
		
		return itemExchangeInfoMap;
	}
	
	/**
	 * 判断当日使用道具获得金币数量是否达上限
	 * @param itemCfg
	 * @param itemCount
	 * @return
	 */
	private boolean checkToolBackGoldLimit(ItemCfg itemCfg, int itemCount) {
		int backGold = 0;
		int itemType = itemCfg.getItemType();
		
		if (itemType == Const.ToolType.REWARD_VALUE) {
			AwardCfg awardCfg =  HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, itemCfg.getRewardId());
			if(awardCfg != null){
				AwardItems awardItems = awardCfg.getRandomAward();
				itemCount--;
				while(itemCount > 0) {
					AwardItems items = awardCfg.getRandomAward();
					if(items != null) {
						awardItems.appendAward(items);
					}
					itemCount--;
				}
				
				ItemInfo itemInfo = awardItems.getItem(ItemType.PLAYER_ATTR_VALUE * GsConst.ITEM_TYPE_BASE, PlayerAttr.GOLD_VALUE);
				backGold = (int) itemInfo.getCount();
			}
			
		} else if (itemType == Const.ToolType.TALENT_SWITCH_VALUE) {
			backGold = itemCfg.getSellPrice() * itemCount;
		} else if (itemType == Const.ToolType.ADD_ATTR_VALUE && itemCfg.getAttrType() == PlayerAttr.GOLD_VALUE) {
			backGold = itemCfg.getAttrVal() * itemCount;
		}
		
		if (backGold == 0) {
			return false;
		}
		
		int toolsBackGoldsToday = player.getToolBackGoldToday(backGold);
		if (toolsBackGoldsToday < 0) {
			return true;
		}
		
		LocalRedis.getInstance().updateToolBackGold(player.getId(), toolsBackGoldsToday);
		
		return false;
	}

	/**
	 * 实施道具作用效果
	 * 
	 * @param itemCfg
	 * @param itemCount
	 * @param targetId
	 */
	private void addItemEffect(ItemCfg itemCfg, int itemCount, String targetId) {
		ItemService.getInstance().onItemUse(player, itemCfg, itemCount, targetId); //TODO
		if (itemCfg.getEffect() == EffType.RES_COLLECT_BUF_VALUE) {
			ActivityManager.getInstance().postEvent(new ItemUseResCollectBufEvent(player.getId(), itemCfg.getId(), itemCount));
		}
		
		if (itemCfg.isResItem()) {
			ActivityManager.getInstance().postEvent(new ResToolUseEvent(player.getId(), itemCount));
		}
	}
	
	/**
	 * 消耗货币发放物品
	 * 
	 * @param addItem
	 * @param count
	 * @param consumeItem
	 * @param action
	 * @param hpCode
	 * @return
	 */
	private boolean consumeAndReward(ItemInfo addItem, int count, ItemInfo consumeItem, Action action, int hpCode) {
		return consumeAndReward(addItem, count, consumeItem, action, hpCode, null, Collections.emptyList());
	}
	
	/**
	 * 消耗货币发放物品
	 * 
	 * @param itemId
	 * @param count
	 * @param consumeItem
	 * @param action
	 * @param hpCode
	 * @return
	 */
	private boolean consumeAndReward(ItemInfo addItem, int count, ItemInfo consumeItem, Action action, int hpCode, 
			RewardOrginType orginType, List<ItemInfo> extraItemList) {
		ConsumeItems consume = ConsumeItems.valueOf();
		int price = (int) consumeItem.getCount();
		consumeItem.setCount(price * count);
		consume.addConsumeInfo(consumeItem, false);
		if (!consume.checkConsume(player, hpCode)) {
			return false;
		}

		final int itemId = addItem.getItemId();
		int goldBefore = player.getGold();
		int diamondBefore = player.getDiamonds();
		
		if (consume.getBuilder().hasAttrInfo() && consume.getBuilder().getAttrInfo().getDiamond() > 0) {
			consume.addPayItemInfo(new PayItemInfo(String.valueOf(addItem.getItemId()), price, (int)addItem.getCount()));
		}
		consume.consumeAndPush(player, action);

		// 发放物品
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItem(addItem);
		awardItem.addItemInfos(extraItemList);
		
		orginType = orginType != null ? orginType : RewardOrginType.SHOPPING_ITEM;
		if (!awardItem.rewardTakeAffectAndPush(player, action, orginType)) {
			awardItem = null;
		}

		// 发放物品失败，返还扣除的钻石
		if (awardItem == null) {
			HawkLog.logPrintln("diliver goods failed, playerId: {}, itemId: {}, count: {}", player.getId(), itemId,
					count);
			sendError(hpCode, Status.Error.ITEMS_ADD_FAILED);
			onPayCancel(consume.getDiamondConsumeInfo());
			return false;
		} else {
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
			if (itemCfg != null) {
				int consumeGold = goldBefore - player.getGold();
				if (consumeGold > 0) {
					LogUtil.logItemFlow(player, action, LogInfoType.goods_add, itemCfg.getItemType(), itemId, addItem.getCount(),
							consumeGold, IMoneyType.MT_GOLD);
				}

				int consumeDiamond = diamondBefore - player.getDiamonds();
				if (consumeDiamond > 0) {
					LogUtil.logItemFlow(player, action, LogInfoType.goods_add, itemCfg.getItemType(), itemId, addItem.getCount(),
							consumeDiamond, IMoneyType.MT_DIAMOND);
				}
			}
		}

		return true;
	}

	/**
	 * 取消支付
	 * 
	 * @param conusmeInfo
	 */
	private void onPayCancel(DiamondConsumeInfo conusmeInfo) {
		if (conusmeInfo == null || conusmeInfo.getCount() <= 0
				|| HawkOSOperator.isEmptyString(conusmeInfo.getBillno())) {
			return;
		}

		HawkLog.logPrintln("diliver goods failed, invoke pay cancel interface, playerId: {}, cousume: {}, billno: {}",
				player.getId(), conusmeInfo.getCount(), conusmeInfo.getBillno());
		player.cancelPay(conusmeInfo.getCount(), conusmeInfo.getBillno());
	}

	/**
	 * 道具消耗
	 * 
	 * @param itemId
	 * @param items
	 * @param hpCode
	 * @return
	 */
	private boolean consumeItemByItemId(ItemCfg itemCfg, int itemCount, Map<String, Integer> items, int hpCode) {
		ConsumeItems consume = ConsumeItems.valueOf();
		for (Entry<String, Integer> e : items.entrySet()) {
			consume.addConsumeInfo(ItemType.TOOL, e.getKey(), itemCfg.getId(), e.getValue());
		}
		
		if (!consume.checkConsume(player, hpCode)) {
			logger.error("use item by itemId, item not enough, playerId: {}, itemId: {}", player.getId(), itemCfg.getId());
			sendError(hpCode, Status.Error.ITEM_NOT_ENOUGH);
			return false;
		}
		
		if (checkToolBackGoldLimit(itemCfg, itemCount)) {
			sendError(hpCode, Status.Error.TOOL_BACK_GOLD_LIMIT_ERROR);
			return false;
		}
		
		consume.consumeAndPush(player, GameUtil.itemTypeToAction(itemCfg));
		
		return true;
	}

	private boolean specialCheck(Player player, ItemCfg itemCfg, int itemCount, int protoType, String targetId){
		int itemId = itemCfg.getId();
		switch (itemCfg.getItemType()){
			case Const.ToolType.BOX_CHOOSE_REWARD_VALUE:{
				DYZZSeasonCfg dyzzSeasonCfg = HawkConfigManager.getInstance().getKVInstance(DYZZSeasonCfg.class);
				if(dyzzSeasonCfg != null && dyzzSeasonCfg.getItemSpecialCheck().contains(itemId)){
					int index = Integer.parseInt(targetId);
					index = Math.max(0, index - 1);
					ItemInfo itemInfo = itemCfg.getChooseAward(index);
					if(itemInfo != null && hasDressAndFrame(player, itemInfo.getItemId())){
						sendError(protoType, Status.Error.DYZZ_CHOOSE_BOX_SAME);
						return true;
					}
				}
			}
			break;
		}
		return false;
	}

	private boolean hasDressAndFrame(Player player, int itemId){
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		switch (itemCfg.getItemType()){
			case ToolType.STATUS_VALUE:{
				BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, itemCfg.getBuffId());
				if (buffCfg != null) {
					EffectCfg effCfg = HawkConfigManager.getInstance().getConfigByKey(EffectCfg.class, buffCfg.getEffect());
					if (effCfg != null) {
						if (effCfg.getType() == GsConst.EffectType.IMAGE_ITEM.getValue()) {
							if(player.getData().getItemNumByItemId(itemId) > 0){
								return true;
							}
							StatusDataEntity entity = player.getData().getStatusById(effCfg.getId());
							if(entity != null && entity.getEndTime() > HawkTime.getMillisecond()){
								return true;
							}
						}
					}
				}
			}
			break;
			case Const.ToolType.DRESS_VALUE:{
				if(player.getData().getItemNumByItemId(itemId) > 0){
					return true;
				}
				DressToolCfg dressToolCfg = HawkConfigManager.getInstance().getConfigByKey(DressToolCfg.class, itemCfg.getDressId());
				if (dressToolCfg == null) {
					return false;
				}
				DressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(DressCfg.class, dressToolCfg.getDressId());
				if (dressCfg == null) {
					return false;
				}
				DressEntity dressEntity = player.getData().getDressEntity();
				DressItem dressInfo = dressEntity.getDressInfo(dressCfg.getDressType(), dressCfg.getModelType());
				if(dressInfo!=null){
					return true;
				}
			}
			break;
		}
		return false;
	}
	
}
