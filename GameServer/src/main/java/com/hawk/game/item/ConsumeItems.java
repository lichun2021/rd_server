package com.hawk.game.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.roleexchange.RoleExchangeService;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Consume.ConsumeItem;
import com.hawk.game.protocol.Consume.HPConsumeInfo;
import com.hawk.game.protocol.Consume.SyncAttrInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.IMoneyType;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.sdk.msdk.entity.PayItemInfo;

/**
 * @author hawk
 *
 */
public class ConsumeItems {

	static Logger logger = LoggerFactory.getLogger("Server");
	private ConsumeHelper helper;
	/**
	 * 消耗信息
	 */
	private HPConsumeInfo consumeInfo;
	/**
	 * 钻石消耗信息
	 */
	private DiamondConsumeInfo diamondConsumeInfo;
	/**
	 * 消耗金条的物品信息，不消耗金条的情况下此项为空
	 */
	private List<PayItemInfo> payItemInfos;

	/**
	 * 默认构造
	 */
	private ConsumeItems() {
		helper = new ConsumeHelper();
	}

	/**
	 * 默认构造
	 * 
	 * @return
	 */
	public static ConsumeItems valueOf() {
		return new ConsumeItems();
	}

	/**
	 * 带参构造
	 * 
	 * @param type
	 * @param count
	 * @return
	 */
	public static ConsumeItems valueOf(PlayerAttr type, int count) {
		ConsumeItems consumeItems = new ConsumeItems();
		consumeItems.addConsumeInfo(type, count);
		return consumeItems;
	}

	/**
	 * 获取数据builder
	 * 
	 * @return
	 */
	public HPConsumeInfo getBuilder() {
		return consumeInfo;
	}

	/**
	 * 获取钻石消耗详情
	 * 
	 * @return
	 */
	public DiamondConsumeInfo getDiamondConsumeInfo() {
		return diamondConsumeInfo;
	}

	/**
	 * 字符串显示
	 */
	@Override
	public String toString() {
		return consumeInfo.toString();
	}
	
	/**
	 * 添加消耗金条的物品信息
	 * 
	 * @param payItem
	 */
	public void addPayItemInfo(PayItemInfo payItem) {
		if (payItemInfos == null) {
			payItemInfos = new ArrayList<PayItemInfo>();
		}
		
		payItemInfos.add(payItem);
	}

	/**
	 * 添加消耗
	 *
	 * @param type
	 *            所消耗资源（包括金币和钻石）的类型 参考 Const.changeInfo
	 * @param count
	 *            所消耗资源的数量
	 */
	public void addConsumeInfo(PlayerAttr type, long count) {
		if (count >= Integer.MAX_VALUE) {
			throw new RuntimeException("consume count too large, " + count);
		}
		
		helper.addConsume(ItemType.PLAYER_ATTR, type.getNumber(), (int) count, false);

	}

	/**
	 * 添加消耗
	 */
	public void addConsumeInfo(ItemType type, String uuid, int itemId, int count) {
		this.helper.addConsume(type, itemId, count, uuid, false);
	}

	/**
	 * 添加消耗列表数据
	 *
	 * @param playerData
	 * @param needItem
	 * @param isGold
	 *            资源不足的部分是否使用金钱弥补
	 * @return
	 */
	public void addConsumeInfo(ItemInfo item, boolean isGold) {
		helper.addConsume(item.getItemType(), item.getItemId(), (int)item.getCount(), isGold);

	}
	
	public void addConsumeInfo(List<ItemInfo> needItems) {
		this.addConsumeInfo(needItems, false);
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
	public void addConsumeInfo(List<ItemInfo> needItems, boolean isGold) {
		for (ItemInfo item : needItems) {
			helper.addConsume(item.getItemType(), item.getItemId(), (int)item.getCount(), isGold);
		}
	}

	/**
	 * 添加物品消耗
	 * 
	 * @param playerData
	 * @param itemId
	 * @param needCount
	 * @return
	 */
	public void addItemConsume(int itemId, int needCount) {
		addItemConsume(itemId, needCount, false);
	}

	/**
	 * 添加消耗
	 *
	 * @param type
	 * @param id
	 * @param itemId
	 * @param count
	 * @return 道具不足时用水晶替换不足的道具需要的货币数量
	 */
	public void addItemConsume(int itemId, int needCount, boolean isGold) {
		helper.addConsume(ItemType.TOOL, itemId, needCount, isGold);
	}

	/**
	 * 检测是否可消耗
	 *
	 * @return
	 */
	public boolean checkConsume(Player player) {
		return checkConsumeResult(player) <= 0;
	}
	
	public int checkConsumeAndGetResult(Player player) {
		return checkConsumeResult(player);		
	}
	
	public static HawkTuple2<Integer, ConsumeItems> checkConsumeAndGetResult(Player player, List<ItemInfo> itemInfoList) {
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(itemInfoList);
		int rlt = consumeItems.checkConsumeAndGetResult(player);
		
		return new HawkTuple2<Integer, ConsumeItems>(rlt, consumeItems);
	}	
	/**
	 * 检测是否可消耗
	 *
	 * @param player
	 * @param hpCode
	 * @return
	 */
	public boolean checkConsume(Player player, int hpCode) {
		int result = checkConsumeResult(player);
		if (result > 0 && hpCode > 0) {
			player.sendError(hpCode, result, 0);
			return false;
		}
		return true;
	}

	/**
	 * 检测消耗（测试用）
	 * 
	 * @param player
	 * @return
	 */
	public int checkResult(Player player) {
		return checkConsumeResult(player);
	}

	/**
	 * 检测消耗物品或者属性数量是否充足
	 * 
	 * @param player
	 * @return
	 */
	private int checkConsumeResult(Player player) {
		int resultCode = RoleExchangeService.getInstance().consumeCheck(player);
		if (resultCode != 0) {
			return resultCode;
		}

		consumeInfo = helper.beforeCheck(player.getData());
		
		// 道具消耗
		for (ConsumeItem consumeItem : consumeInfo.getConsumeItemList()) {
			if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.ITEM_USE, consumeItem.getItemId())) {
				return Status.SysError.ITEM_USE_OFF_VALUE;
			}

			String itemId = consumeItem.getId();
			if (HawkOSOperator.isEmptyString(itemId)) {
				logger.debug("item not enough, itemId is null");
				return Status.Error.ITEM_NOT_ENOUGH_VALUE;
			}

			ItemEntity itemEntity = player.getData().getItemById(itemId);
			if (itemEntity == null || itemEntity.getItemCount() < consumeItem.getCount()) {
				logger.debug("item not enough, playerId: {}, itemId: {}, player count: {}, consume count: {}", player.getId(), itemId,
						itemEntity == null ? 0 : itemEntity.getItemCount(), consumeItem.getCount());
				return Status.Error.ITEM_NOT_ENOUGH_VALUE;
			}
		}
		
		// 不存在基础属性资源消耗，直接返回
		if (!consumeInfo.hasAttrInfo()) {
			return 0;
		}

		SyncAttrInfo syncAttrInfo = consumeInfo.getAttrInfo();

		// 直接消耗钻石的情况
		if (syncAttrInfo.getDiamond() > 0) {
			if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.DIAMOND_USE)) {
				return Status.SysError.DIAMOND_USE_OFF_VALUE;
			}

			if (player.getDiamonds() < syncAttrInfo.getDiamond()) {
				logger.debug("diamond not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getDiamonds(), syncAttrInfo.getDiamond());
				return Status.Error.DIAMONDS_NOT_ENOUGH_VALUE;
			}
		}
				
		if (syncAttrInfo.getCoin() > 0) {
			if (player.getCoin() < syncAttrInfo.getCoin()) {
				logger.debug("coins not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getCoin(), syncAttrInfo.getCoin());
				return Status.Error.COINS_NOT_ENOUGH_VALUE;
			}
		}

		if (syncAttrInfo.getGold() > 0) {
			if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.GOLD_USE)) {
				return Status.SysError.GOLD_USE_OFF_VALUE;
			}

			// 水晶不足，钻石来补
			if (player.getGold() < syncAttrInfo.getGold()) {
				int needDiamonds = syncAttrInfo.getGold() - player.getGold();
				// 钻石也不足，提示玩家货币不足
				if (needDiamonds > player.getDiamonds()) {
					logger.debug("gold not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getGold(), syncAttrInfo.getGold());
					return Status.Error.GOLD_NOT_ENOUGH_VALUE;
				}
			}
		}

		if (syncAttrInfo.getGoldore() > 0) {
			if (player.getGoldore() < syncAttrInfo.getGoldore()) {
				logger.warn("goldore not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getGoldore(), syncAttrInfo.getGoldore());
				return Status.Error.GOLDORE_NOT_ENOUGH_VALUE;
			}
		}

		if (syncAttrInfo.getGoldoreUnsafe() > 0) {
			if (player.getGoldoreUnsafe() < syncAttrInfo.getGoldoreUnsafe()) {
				logger.warn("goldoreunsafe not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getGoldoreUnsafe(),
						syncAttrInfo.getGoldoreUnsafe());
				return Status.Error.GOLDORE_NOT_ENOUGH_VALUE;
			}
		}

		if (syncAttrInfo.getOil() > 0) {
			if (player.getOil() < syncAttrInfo.getOil()) {
				logger.warn("oil not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getOil(), syncAttrInfo.getOil());
				return Status.Error.OIL_NOT_ENOUGH_VALUE;
			}
		}

		if (syncAttrInfo.getOilUnsafe() > 0) {
			if (player.getOilUnsafe() < syncAttrInfo.getOilUnsafe()) {
				logger.warn("oilUnsafe not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getOilUnsafe(), syncAttrInfo.getOilUnsafe());
				return Status.Error.OIL_NOT_ENOUGH_VALUE;
			}
		}

		if (syncAttrInfo.getSteel() > 0) {
			if (player.getSteel() < syncAttrInfo.getSteel()) {
				logger.warn("steel not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getSteel(), syncAttrInfo.getSteel());
				return Status.Error.STEEL_NOT_ENOUGH_VALUE;
			}
		}
		
		if (syncAttrInfo.getSteelUnsafe() > 0) {
			if (player.getSteelUnsafe() < syncAttrInfo.getSteelUnsafe()) {
				logger.warn("steelUnsafe not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getSteelUnsafe(), syncAttrInfo.getSteelUnsafe());
				return Status.Error.STEEL_NOT_ENOUGH_VALUE;
			}
		}

		if (syncAttrInfo.getTombarthite() > 0) {
			if (player.getTombarthite() < syncAttrInfo.getTombarthite()) {
				logger.warn("tombarthite not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getTombarthite(), syncAttrInfo.getTombarthite());
				return Status.Error.TOMBARTHITE_NOT_ENOUGH_VALUE;
			}
		}

		if (syncAttrInfo.getTombarthiteUnsafe() > 0) {
			if (player.getTombarthiteUnsafe() < syncAttrInfo.getTombarthiteUnsafe()) {
				logger.warn("tombarthiteUnsafe not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getTombarthiteUnsafe(),
						syncAttrInfo.getTombarthiteUnsafe());
				return Status.Error.TOMBARTHITE_NOT_ENOUGH_VALUE;
			}
		}

		if (syncAttrInfo.getVit() > 0) {
			if (player.getVit() < syncAttrInfo.getVit()) {
				logger.debug("vit not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getVit(), syncAttrInfo.getVit());
				return Status.Error.VIT_NOT_ENOUGH_VALUE;
			}
		}

		if (syncAttrInfo.getGuildContribution() > 0) {
			if (player.getGuildContribution() < syncAttrInfo.getGuildContribution()) {
				logger.debug("guildContribution not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getGuildContribution(),
						syncAttrInfo.getGuildContribution());
				return Status.Error.GUILD_CONTRIBUTION_NOT_ENOUGH_VALUE;
			}
		}
		
		if (syncAttrInfo.getMilitaryScore() > 0) {
			if (player.getMilitaryScore() < syncAttrInfo.getMilitaryScore()) {
				logger.debug("militaryScore not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getMilitaryScore(),
						syncAttrInfo.getMilitaryScore());
				return Status.Error.MILITARY_SCORE_NOT_ENOUGH_VALUE;
			}
		}
		
		if (syncAttrInfo.getCyborgScore() > 0) {
			if (player.getCyborgScore() < syncAttrInfo.getCyborgScore()) {
				logger.debug("cyborgScore not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getCyborgScore(),
						syncAttrInfo.getCyborgScore());
				return Status.Error.MILITARY_SCORE_NOT_ENOUGH_VALUE;
			}
		}

		// IDIP扣除玩家经验值
		if (syncAttrInfo.getExp() > 0) {
			if (player.getExp() < syncAttrInfo.getExp()) {
				logger.debug("exp not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getExp(), syncAttrInfo.getExp());
				return Status.Error.EXP_NOT_ENOUGH_VALUE;
			}
		}

		// IDIP接口扣除vip经验值
		if (syncAttrInfo.getVipExp() > 0) {
			if (player.getVipExp() < syncAttrInfo.getVipExp()) {
				logger.debug("vipExp not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getVipExp(), syncAttrInfo.getVipExp());
				return Status.Error.VIP_EXP_NOT_ENOUGH_VALUE;
			}
		}
		//达雅记分
		if (syncAttrInfo.getDyzzScore() > 0) {
			if (player.getDYZZScore() < syncAttrInfo.getDyzzScore()) {
				logger.debug("dyzzScore not enough, playerId: {}, player count: {}, consume count: {}", player.getId(), player.getDYZZScore(),
						syncAttrInfo.getDyzzScore());
				return Status.Error.MILITARY_SCORE_NOT_ENOUGH_VALUE;
			}
		}

		return 0;
	}

	/**
	 * 数据消耗(不推送)
	 */
	private boolean consumeAndRecord(Player player, Action action, AwardItems awardItems) {

		Consumer<ItemInfo> recordConsumer = Objects.isNull(awardItems) ? x -> {
		} : awardItems::addItem;
		
		// 先扣金条，再扣其它消耗
		if (consumeInfo.hasAttrInfo()) {
			SyncAttrInfo syncAttrInfo = consumeInfo.getAttrInfo();
			if (syncAttrInfo.getDiamond() > 0) {
				recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.DIAMOND_VALUE, syncAttrInfo.getDiamond()));
				if (payItemInfos == null) {
					payItemInfos = new ArrayList<PayItemInfo>(1);
					payItemInfos.add(new PayItemInfo("", syncAttrInfo.getDiamond(), 1));
				}
				
				String billno = player.consumeDiamonds(syncAttrInfo.getDiamond(), action, payItemInfos);
				if (!HawkOSOperator.isEmptyString(billno)) {
					diamondConsumeInfo = new DiamondConsumeInfo(syncAttrInfo.getDiamond(), billno);
				}
			}
			
			if (syncAttrInfo.getGold() > 0) {
				int needDiamonds = syncAttrInfo.getGold() - player.getGold();
				// 黄金不足，先用钻石兑换
				if (needDiamonds > 0) {
					List<PayItemInfo> payItems = new ArrayList<PayItemInfo>(1);
					payItems.add(new PayItemInfo(String.valueOf(PlayerAttr.GOLD_VALUE), 1, needDiamonds));
					String billno = player.consumeDiamonds(needDiamonds, Action.EXCHANGE_DIAMONDS_TO_GOLD, payItems);
					if (!HawkOSOperator.isEmptyString(billno)) {
						diamondConsumeInfo = new DiamondConsumeInfo(needDiamonds, billno);
						player.increaseGold(needDiamonds, Action.EXCHANGE_DIAMONDS_TO_GOLD);
					}
				}
				
				recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.GOLD_VALUE, syncAttrInfo.getGold()));
				player.consumeGold(syncAttrInfo.getGold(), action);
			}
		}
				
		// 道具物品消耗
		for (ConsumeItem consumeItem : consumeInfo.getConsumeItemList()) {
			recordConsumer.accept(new ItemInfo(consumeItem.getType(), consumeItem.getItemId(), consumeItem.getCount()));
			player.consumeTool(consumeItem.getId(), consumeItem.getType(), consumeItem.getCount(), action);
			// 此处不是购买道具不花钱，所以moneyType填什么无所谓
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, consumeItem.getItemId());
			int goodsType = itemCfg != null ? itemCfg.getItemType() : (consumeItem.getType() > 10000 ? consumeItem.getType() : consumeItem.getType() * 10000);
			LogUtil.logItemFlow(player, action, LogInfoType.goods_sub, goodsType, consumeItem.getItemId(),
					consumeItem.getCount(), 0, IMoneyType.MT_GOLD);
		}
		
		// 不存在基础属性资源消耗，直接返回
		if (!consumeInfo.hasAttrInfo()) {
			return true;
		}
		
		SyncAttrInfo syncAttrInfo = consumeInfo.getAttrInfo();
		
		if (syncAttrInfo.getCoin() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.COIN_VALUE, syncAttrInfo.getCoin()));
			player.consumeCoin(syncAttrInfo.getCoin(), action);
		}

		if (syncAttrInfo.getGoldore() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.GOLDORE_VALUE, (int) syncAttrInfo.getGoldore()));
			player.consumeResource(syncAttrInfo.getGoldore(), Const.PlayerAttr.GOLDORE_VALUE, action);
		}

		if (syncAttrInfo.getGoldoreUnsafe() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.GOLDORE_UNSAFE_VALUE, (int) syncAttrInfo.getGoldoreUnsafe()));
			player.consumeResource(syncAttrInfo.getGoldoreUnsafe(), Const.PlayerAttr.GOLDORE_UNSAFE_VALUE, action);
		}

		if (syncAttrInfo.getGoldoreNotEnough() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.GOLDORE_VALUE, (int) syncAttrInfo.getGoldoreNotEnough()));
		}

		if (syncAttrInfo.getOil() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.OIL_VALUE, (int) syncAttrInfo.getOil()));
			player.consumeResource(syncAttrInfo.getOil(), Const.PlayerAttr.OIL_VALUE, action);
		}

		if (syncAttrInfo.getOilUnsafe() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.OIL_UNSAFE_VALUE, (int) syncAttrInfo.getOilUnsafe()));
			player.consumeResource(syncAttrInfo.getOilUnsafe(), Const.PlayerAttr.OIL_UNSAFE_VALUE, action);
		}

		if (syncAttrInfo.getOilNotEnough() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.OIL_VALUE, (int) syncAttrInfo.getOilNotEnough()));
		}

		if (syncAttrInfo.getSteel() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.STEEL_VALUE, (int) syncAttrInfo.getSteel()));
			player.consumeResource(syncAttrInfo.getSteel(), Const.PlayerAttr.STEEL_VALUE, action);
		}
		
		if (syncAttrInfo.getSteelUnsafe() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.STEEL_UNSAFE_VALUE, (int) syncAttrInfo.getSteelUnsafe()));
			player.consumeResource(syncAttrInfo.getSteelUnsafe(), Const.PlayerAttr.STEEL_UNSAFE_VALUE, action);
		}

		if (syncAttrInfo.getSteelNotEnough() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.STEEL_VALUE, (int) syncAttrInfo.getSteelNotEnough()));
		}

		if (syncAttrInfo.getTombarthite() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.TOMBARTHITE_VALUE, (int) syncAttrInfo.getTombarthite()));
			player.consumeResource(syncAttrInfo.getTombarthite(), Const.PlayerAttr.TOMBARTHITE_VALUE, action);
		}

		if (syncAttrInfo.getTombarthiteUnsafe() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.TOMBARTHITE_UNSAFE_VALUE, (int) syncAttrInfo.getTombarthiteUnsafe()));
			player.consumeResource(syncAttrInfo.getTombarthiteUnsafe(), Const.PlayerAttr.TOMBARTHITE_UNSAFE_VALUE, action);
		}

		if (syncAttrInfo.getTombarthiteNotEnough() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.TOMBARTHITE_VALUE, (int) syncAttrInfo.getTombarthiteNotEnough()));
		}

		if (syncAttrInfo.getVit() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIT_VALUE, syncAttrInfo.getVit()));
			player.consumeVit(syncAttrInfo.getVit(), action);
		}

		if (syncAttrInfo.getGuildContribution() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.GUILD_CONTRIBUTION_VALUE, syncAttrInfo.getGuildContribution()));
			player.consumeGuildContribution(syncAttrInfo.getGuildContribution(), action);
		}
		
		if (syncAttrInfo.getMilitaryScore() > 0) {
			int count = (int) (syncAttrInfo.getMilitaryScore() >= Integer.MAX_VALUE ? Integer.MAX_VALUE : syncAttrInfo.getMilitaryScore());
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.MILITARY_SCORE_VALUE, count));
			player.consumeMilitaryScore(syncAttrInfo.getMilitaryScore(), action);
		}
		
		if (syncAttrInfo.getCyborgScore() > 0) {
			int count = (int) (syncAttrInfo.getCyborgScore() >= Integer.MAX_VALUE ? Integer.MAX_VALUE : syncAttrInfo.getCyborgScore());
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.CYBORG_SCORE_VALUE, count));
			player.consumeCyborgScore(syncAttrInfo.getCyborgScore(), action);
		}

		// IDIP扣除玩家经验值
		if (syncAttrInfo.getExp() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.EXP_VALUE, syncAttrInfo.getExp()));
			player.decreaseExp(syncAttrInfo.getExp(), action);
		}

		// IDIP扣除vip经验值
		if (syncAttrInfo.getVipExp() > 0) {
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.VIP_POINT_VALUE, syncAttrInfo.getVipExp()));
			player.decreaceVipExp(syncAttrInfo.getVipExp(), action);
		}
		//扣除达雅积分
		if (syncAttrInfo.getDyzzScore() > 0) {
			int count = (int) (syncAttrInfo.getDyzzScore() >= Integer.MAX_VALUE ? Integer.MAX_VALUE : syncAttrInfo.getDyzzScore());
			recordConsumer.accept(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, PlayerAttr.DYZZ_SCORE_VALUE, count));
			player.consumeDYZZScore(syncAttrInfo.getDyzzScore(), action);
		}
		return true;
	}

	/**
	 * 数据消耗，并记录实际消耗的内容
	 * 
	 * @param player
	 * @param action
	 * @return 返回实际消耗的东西 
	 */
	public AwardItems consumeAndPush(Player player, Action action) {
		try {
			AwardItems awardItems = AwardItems.valueOf();
			if (!consumeAndRecord(player, action, awardItems)) {
				return awardItems;
			}
			pushChange(player);
			return awardItems;			
		} catch (Exception e) {
			HawkLog.errPrintln("consumeAndPush Exception, playerId: {}, action: {}, consume: {}", player.getId(), action, toString());
			throw e;
		}
	}

	/**
	 * 同步改变信息
	 *
	 * @param player
	 * @return
	 */
	private boolean pushChange(Player player) {
		// 不存在基础属性资源消耗
		if (!consumeInfo.hasAttrInfo()) {
			return player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_CONSUME_S, this.consumeInfo.toBuilder()));
		}
		
		SyncAttrInfo syncAttrInfo = consumeInfo.getAttrInfo();
		HPConsumeInfo.Builder resultInfo = this.consumeInfo.toBuilder();
		if (syncAttrInfo.hasCoin()) {
			resultInfo.getAttrInfoBuilder().setCoin(player.getCoin());
		}

		if (syncAttrInfo.hasGold()) {
			resultInfo.getAttrInfoBuilder().setGold(player.getGold());
		}

		if (syncAttrInfo.hasGoldore()) {
			resultInfo.getAttrInfoBuilder().setGoldore(player.getGoldore());
		}

		if (syncAttrInfo.hasGoldoreUnsafe()) {
			resultInfo.getAttrInfoBuilder().setGoldoreUnsafe(player.getGoldoreUnsafe());
		}

		if (syncAttrInfo.hasOil()) {
			resultInfo.getAttrInfoBuilder().setOil(player.getOil());
		}
		if (syncAttrInfo.hasOilUnsafe()) {
			resultInfo.getAttrInfoBuilder().setOilUnsafe(player.getOilUnsafe());
		}

		if (syncAttrInfo.hasSteel()) {
			resultInfo.getAttrInfoBuilder().setSteel(player.getSteel());
		}

		if (syncAttrInfo.hasSteelUnsafe()) {
			resultInfo.getAttrInfoBuilder().setSteelUnsafe(player.getSteelUnsafe());
		}

		if (syncAttrInfo.hasTombarthite()) {
			resultInfo.getAttrInfoBuilder().setTombarthite(player.getTombarthite());
		}

		if (syncAttrInfo.hasTombarthiteUnsafe()) {
			resultInfo.getAttrInfoBuilder().setTombarthiteUnsafe(player.getTombarthiteUnsafe());
		}

		if (syncAttrInfo.hasVit()) {
			resultInfo.getAttrInfoBuilder().setVit(player.getVit());
		}

		if (syncAttrInfo.hasMilitaryScore()) {
			resultInfo.getAttrInfoBuilder().setMilitaryScore(player.getMilitaryScore());
		}

		if (syncAttrInfo.hasCyborgScore()) {
			resultInfo.getAttrInfoBuilder().setCyborgScore(player.getCyborgScore());
		}

		if (syncAttrInfo.hasDyzzScore()) {
			resultInfo.getAttrInfoBuilder().setDyzzScore(player.getDYZZScore());
		}

		return player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_CONSUME_S, resultInfo));
	}

	/**
	 * 获取道具id和数量
	 * 
	 * @return
	 */
	public HawkTuple2<String, String> getItemsInfo() {
		StringBuilder itemIds = new StringBuilder();
		StringBuilder itemCounts = new StringBuilder();
		for (ConsumeItem item : consumeInfo.getConsumeItemList()) {
			if (item.getCount() <= 0 || item.getItemId() <= 0) {
				continue;
			}
			itemIds.append(item.getItemId()).append(",");
			itemCounts.append(item.getCount()).append(",");
		}
		if (itemIds.indexOf(",") >= 0) {
			itemIds.deleteCharAt(itemIds.length() - 1);
			itemCounts.deleteCharAt(itemCounts.length() - 1);
		}

		return new HawkTuple2<String, String>(itemIds.toString(), itemCounts.toString());
	}
	
	/**
	 * 返回消耗道具的副本, 只包含道具
	 * 
	 * 此方法慎用！！！！
	 * 
	 * 看了下ItemInfo的代码:
	 * 		itmeInfo1: type=3 id=1001 count=1
	 *      itmeInfo2: type=30000 id=1001 count=1
	 *      他们两个itemInfo.toString()输出的都是30000_1001_1
	 * 
	 * 这段代码会导致consumeItems.add(itemInfo2)后，，再调用comsumeItems.getItemsCopy()后就变成了itemInfo1
	 * 从而使得前后的itemInfo调用getType()方法并不相等
	 * @return
	 */
	@Deprecated
	public List<ItemInfo> getItemsCopy() {
		List<ItemInfo> list = new ArrayList<>();
		for (ConsumeItem item : consumeInfo.getConsumeItemList()) {
			ItemInfo rs = new ItemInfo(item.getType(), item.getItemId(), item.getCount());
			list.add(rs);
		}
		return list;
	}

}
