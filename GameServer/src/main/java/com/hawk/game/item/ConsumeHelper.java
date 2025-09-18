package com.hawk.game.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hawk.collection.ConcurrentHashTable;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple3;

import com.google.common.collect.Table;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.ShopCfg;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.PlayerBaseEntity;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Consume.ConsumeItem;
import com.hawk.game.protocol.Consume.HPConsumeInfo;
import com.hawk.game.protocol.Consume.SyncAttrInfo;
import com.hawk.game.util.GameUtil;

public class ConsumeHelper {
	//// 本次重构尽量保留原代码. 未完全重构
	private class ComsumeItem {
		private ComsumeItem() {
		}

		/** 种类 */
		ItemType type;
		/** id */
		int itemId;
		/** 数量 */
		int count;
		/** 水晶可兑换数 */
		int convertible;
		/** 针对tool 至少要从指定uuid扣除的数量 */
		Map<String, Integer> entityCon = new HashMap<>();

		void addItemEntityCost(String uuid, int count) {
			entityCon.merge(uuid, count, (v1, v2) -> v1 + v2);
		}

		int getItemId() {
			return itemId;
		}

		int getCount() {
			return count;
		}

	}

	private Table<ItemType, Integer, ComsumeItem> table = ConcurrentHashTable.create();

	public void addConsume(ItemType type, int itemId, int count, boolean isGold) {
		this.addConsume(type, itemId, count, null, isGold);
	}

	public void addConsume(ItemType type, int itemId, int count, String uuid, boolean isGold) {
		if (count < 0) {
			throw new RuntimeException("consumeItem count error!");
		}

		if (count == 0) {
			return;
		}
		
		ComsumeItem consume = table.get(type, itemId);
		if (Objects.isNull(consume)) {
			consume = new ComsumeItem();
			consume.type = type;
			consume.itemId = itemId;
			table.put(type, itemId, consume);
		}

		consume.count = consume.count + count;
		
		// 该道具能否使用金币替代
		boolean canUseGold = true;
		if (type == ItemType.TOOL) {
			ItemCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
			canUseGold = cfg != null && cfg.consumeCanConvert();
		}
		if (isGold && canUseGold) {
			consume.convertible = consume.convertible + count;
		}

		if (Objects.nonNull(uuid) && !isGold) { // 指定从某一堆扣除
			consume.addItemEntityCost(uuid, count);
		}

	}

	private class ItemConsumPiece {
		List<ConsumeItem> itemList = new ArrayList<>();
		int gold;
		int diamond;
	}

	public ItemConsumPiece addItemConsume(PlayerData playerData, ComsumeItem itemInfo) {
		ItemConsumPiece result = new ItemConsumPiece();
		int needCount = itemInfo.count;
		List<ItemEntity> items = playerData.getItemsByItemId(itemInfo.itemId);

		// 每一堆所拥有的数量
		Map<String, Integer> entityCountMap = items.stream().collect(Collectors.toMap(ItemEntity::getId, ItemEntity::getItemCount));

		// 计算固定扣除是否满足.
		for (Entry<String, Integer> must : itemInfo.entityCon.entrySet()) {
			String uuid = must.getKey();
			int count = must.getValue();
			// 计算剩余可用量
			int left = entityCountMap.merge(uuid, -count, (v1, v2) -> v1 + v2);
			if (left < 0) {// 按堆不满足
				result.itemList.add(markFail(itemInfo.itemId, left));
				return result;
			}
		}
		// 剩余应扣
		needCount = needCount - itemInfo.entityCon.values().stream().mapToInt(Integer::intValue).sum();
		for (Entry<String, Integer> ent : entityCountMap.entrySet()) {
			String uuid = ent.getKey();
			int useAble = ent.getValue();
			if (needCount == 0 || useAble == 0) {
				continue;
			}
			int disCount = Math.min(needCount, useAble);// 可扣除
			itemInfo.addItemEntityCost(uuid, disCount);
			needCount -= disCount;
		}

		for (Entry<String, Integer> ent : itemInfo.entityCon.entrySet()) {
			String uuid = ent.getKey();
			int disCount = ent.getValue();
			ConsumeItem.Builder builder = ConsumeItem.newBuilder();
			builder.setType(ItemType.TOOL_VALUE);
			builder.setId(uuid);
			builder.setItemId(itemInfo.itemId);
			builder.setCount(disCount);
			result.itemList.add(builder.build());
		}

		if (needCount <= 0) {
			return result;
		}

		// 不够扣
		ShopCfg shopCfg;
		if (itemInfo.convertible < needCount || Objects.isNull(shopCfg = ShopCfg.getShopCfgByItemId(itemInfo.itemId))) {
			// 只要少一个业务就失败
			ConsumeItem builder = markFail(itemInfo.itemId, needCount);
			result.itemList.add(builder);
			return result;
		}

		ItemInfo cost = shopCfg.getPriceItemInfo();
		cost.setCount(cost.getCount() * needCount);

		if (cost.getItemId() == PlayerAttr.DIAMOND_VALUE) {
			result.diamond = (int)cost.getCount();
		} else {
			result.gold = (int)cost.getCount();
		}
		return result;

	}

	private ConsumeItem markFail(int itemId, int needCount) {
		ConsumeItem.Builder builder = ConsumeItem.newBuilder();
		builder.setType(ItemType.TOOL_VALUE);
		builder.setId("");
		builder.setItemId(itemId);
		builder.setCount(needCount);
		return builder.build();
	}

	/**
	 * 添加消耗列表数据
	 *
	 * @param playerData
	 * @param needItems
	 * @param isGold
	 *            资源不足的部分是否使用金钱弥补
	 * @return
	 */
	public HPConsumeInfo beforeCheck(PlayerData playerData) {
		int gold = 0, diamond = 0;
		HPConsumeInfo.Builder consumeInfo = HPConsumeInfo.newBuilder();
		SyncAttrInfo.Builder builder = SyncAttrInfo.newBuilder();
		PlayerBaseEntity playerBaseEntity = playerData.getPlayerBaseEntity();
		// 是否有基础属性资源消耗
		boolean hasAttrInfo = false;
		
		for (ComsumeItem itemInfo : table.values()) {
			if (itemInfo.type != ItemType.PLAYER_ATTR) {
				ItemConsumPiece result = addItemConsume(playerData, itemInfo);
				gold += result.gold;
				diamond += result.diamond;
				consumeInfo.addAllConsumeItem(result.itemList);
				continue;
			}

			hasAttrInfo = true;
			PlayerAttr playerAttr = PlayerAttr.valueOf(itemInfo.itemId);
			switch (playerAttr) {
			case GOLD: {
				gold += itemInfo.getCount();
				break;
			}

			case DIAMOND: {
				diamond += itemInfo.getCount();
				break;
			}

			case LEVEL: {
				builder.setLevel(builder.getLevel() + itemInfo.getCount());
				break;
			}

			case VIP_POINT: {
				builder.setVipExp(builder.getVipExp() + itemInfo.getCount());
				break;
			}

			case VIT: {
				builder.setVit(builder.getVit() + itemInfo.getCount());
				break;
			}

			case GUILD_CONTRIBUTION: {
				builder.setGuildContribution(builder.getGuildContribution() + itemInfo.getCount());
				break;
			}
			
			case MILITARY_SCORE: {
				builder.setMilitaryScore(builder.getMilitaryScore() + itemInfo.getCount());
				break;
			}
			case CYBORG_SCORE: {
				builder.setCyborgScore(builder.getCyborgScore() + itemInfo.getCount());
				break;
			}
			case DYZZ_SCORE:{
				builder.setDyzzScore(builder.getDyzzScore() + itemInfo.getCount());
				break;
			}
			case COIN: {
				builder.setCoin(builder.getCoin() + itemInfo.getCount());
				break;
			}

			case EXP: {
				builder.setExp(builder.getExp() + itemInfo.getCount());
				break;
			}

			case GOLDORE_UNSAFE: {// 消耗非受保护部分,不足部分使用保护
				HawkTuple3<Long, Long, Integer> tuple = addUnsafeRes(playerBaseEntity.getGoldoreUnsafe(), playerBaseEntity.getGoldore() - builder.getGoldore(), itemInfo);
				gold += tuple.third;
				builder.setGoldoreUnsafe(builder.getGoldoreUnsafe() + tuple.first);
				builder.setGoldore(builder.getGoldore() + tuple.second);
				builder.setGoldoreNotEnough(builder.getGoldoreNotEnough() + (itemInfo.getCount() - tuple.first - tuple.second));
				break;
			}

			case GOLDORE: {
				HawkTuple3<Long, Long, Integer> tuple = addSafeRes(playerBaseEntity.getGoldore() - builder.getGoldore(), itemInfo);
				gold += tuple.third;
				builder.setGoldore(builder.getGoldore() + tuple.first);
				builder.setGoldoreNotEnough(builder.getGoldoreNotEnough() + tuple.second);
				break;
			}

			case OIL_UNSAFE: {// 消耗非受保护部分,不足部分使用保护
				HawkTuple3<Long, Long, Integer> tuple = addUnsafeRes(playerBaseEntity.getOilUnsafe(), playerBaseEntity.getOil() - builder.getOil(), itemInfo);
				gold += tuple.third;
				builder.setOilUnsafe(builder.getOilUnsafe() + tuple.first);
				builder.setOil(builder.getOil() + tuple.second);
				builder.setOilNotEnough(builder.getOilNotEnough() + (itemInfo.getCount() - tuple.first - tuple.second));
				break;
			}

			case OIL: {
				HawkTuple3<Long, Long, Integer> tuple = addSafeRes(playerBaseEntity.getOil() - builder.getOil(), itemInfo);
				gold += tuple.third;
				builder.setOil(builder.getOil() + tuple.first);
				builder.setOilNotEnough(builder.getOilNotEnough() + tuple.second);
				break;
			}

			case STEEL_UNSAFE: {// 消耗非受保护部分,不足部分使用保护
				HawkTuple3<Long, Long, Integer> tuple = addUnsafeRes(playerBaseEntity.getSteelUnsafe(), playerBaseEntity.getSteel() - builder.getSteel(), itemInfo);
				gold += tuple.third;
				builder.setSteelUnsafe(builder.getSteelUnsafe() + tuple.first);
				builder.setSteel(builder.getSteel() + tuple.second);
				builder.setSteelNotEnough(builder.getSteelNotEnough() + (itemInfo.getCount() - tuple.first - tuple.second));
				break;
			}

			case STEEL: {
				HawkTuple3<Long, Long, Integer> tuple = addSafeRes(playerBaseEntity.getSteel() - builder.getSteel(), itemInfo);
				gold += tuple.third;
				builder.setSteel(builder.getSteel() + tuple.first);
				builder.setSteelNotEnough(builder.getSteelNotEnough() + tuple.second);
				break;
			}

			case TOMBARTHITE_UNSAFE: {// 消耗非受保护部分,不足部分使用保护
				HawkTuple3<Long, Long, Integer> tuple = addUnsafeRes(playerBaseEntity.getTombarthiteUnsafe(), playerBaseEntity.getTombarthite() - builder.getTombarthite(), itemInfo);
				gold += tuple.third;
				builder.setTombarthiteUnsafe(builder.getTombarthiteUnsafe() + tuple.first);
				builder.setTombarthite(builder.getTombarthite() + tuple.second);
				builder.setTombarthiteNotEnough(builder.getTombarthiteNotEnough() + (itemInfo.getCount() - tuple.first - tuple.second));
				break;
			}

			case TOMBARTHITE: { //<安全资源消耗，不足的资源数量， 消耗金币数量>
				HawkTuple3<Long, Long, Integer> tuple = addSafeRes(playerBaseEntity.getTombarthite() - builder.getTombarthite(), itemInfo);
				gold += tuple.third;
				builder.setTombarthite(builder.getTombarthite() + tuple.first);
				builder.setTombarthiteNotEnough(builder.getTombarthiteNotEnough() + tuple.second);
				break;
			}

			default:
				throw new RuntimeException("Unprocessed attr " + playerAttr);
			}
		}

		if (gold > 0) {
			builder.setGold(builder.getGold() + gold);
			hasAttrInfo = true;
		}

		if (diamond > 0) {
			builder.setDiamond(builder.getDiamond() + diamond);
			hasAttrInfo = true;
		}
		
		if (hasAttrInfo) {
			consumeInfo.setAttrInfo(builder);
		}
		
		return consumeInfo.build();
	}
	
	/**
	 * 计算非安全资源
	 * @param unsafeRes
	 * @param safeRes
	 * @param itemInfo
	 * @return <非安全资源，安全资源，水晶消耗>
	 */
	private HawkTuple3<Long, Long, Integer> addUnsafeRes(long unsafeRes, long safeRes, ComsumeItem itemInfo) {
		long totalCost = itemInfo.getCount();
		long costUnsafeRes = Math.min(totalCost, unsafeRes);
		long costSafeRes = Math.min(totalCost - costUnsafeRes, safeRes);
		long notEnough = totalCost - costUnsafeRes - costSafeRes;
		// 如果可用水晶替代部分不足,就不必兑换,业务注定失败 差一个和差一亿并没有区别
		if (itemInfo.convertible >= notEnough) {
			int costGold = GameUtil.caculateResGold(PlayerAttr.valueOf(itemInfo.getItemId()), notEnough);
			return new HawkTuple3<Long, Long, Integer> (costUnsafeRes, costSafeRes, costGold);
		}

		long safe = totalCost - costUnsafeRes;
		return new HawkTuple3<Long, Long, Integer> (costUnsafeRes, safe, 0);
	}
	
	/**
	 * 计算安全资源
	 * @param safeRes
	 * @param itemInfo
	 * @return <安全资源消耗，不足的资源数量， 消耗金币数量>
	 */
	private HawkTuple3<Long, Long, Integer> addSafeRes(long safeRes, ComsumeItem itemInfo) {
		long notEnough = itemInfo.getCount() - safeRes;
		notEnough = Math.max(0, notEnough);
		if (itemInfo.convertible < notEnough) {// 如果可用水晶替代部分不足,就不必兑换,业务注定失败
			return new HawkTuple3<Long, Long, Integer>((long)itemInfo.getCount(), 0L, 0);
		}

		int costGold = GameUtil.caculateResGold(PlayerAttr.valueOf(itemInfo.getItemId()), notEnough);
		long costGoldore = costGold > 0 ? safeRes : itemInfo.getCount();
		return new HawkTuple3<Long, Long, Integer>(costGoldore, notEnough, costGold);
	}

}
