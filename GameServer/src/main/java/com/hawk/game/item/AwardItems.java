package com.hawk.game.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.AddScoreEvent;
import com.hawk.game.config.ArmourPoolCfg;
import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.EquipmentCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.SysControlProperty;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.EquipEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.lianmengcyb.CYBORGConst.CYBORGState;
import com.hawk.game.lianmengjunyan.LMJYConst.PState;
import com.hawk.game.module.PlayerTravelShopModule;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.module.mechacore.cfg.MechaCoreModuleCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Equip.EquipState;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.HPPlayerReward;
import com.hawk.game.protocol.Reward.RewardInfo;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventSoldierCalc;
import com.hawk.game.task.MailRewardItemLogTask;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;
import com.hawk.log.LogConst.IMoneyType;
import com.hawk.log.LogConst.LogInfoType;

/**
 * 奖励信息内存数据 禁忌: 此对象不可重复复用, 避免奖励累加, 切记
 *
 * @author hawk
 *
 */
public class AwardItems {
	static final Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * 日志记录
	 */
	private boolean keepItemFlow = true;
	/**
	 * 数量检测
	 */
	private boolean countCheck = true;

	// 所有奖励
	private List<ItemInfo> awardItems;

	// 奖励展示列表
	private List<RewardItem> showItems;

	private List<MailRewardItemLogTask> mailLogTaskList;

	/**
	 * 构造函数
	 */
	private AwardItems() {
		awardItems = new LinkedList<ItemInfo>();
		mailLogTaskList = new ArrayList<>();
	}

	/**
	 * 默认构建
	 * 
	 * @return
	 */
	public static AwardItems valueOf() {
		return new AwardItems();
	}

	/**
	 * 从字符串构造
	 *
	 * @param info
	 * @return
	 */
	public static AwardItems valueOf(String info) {
		AwardItems awardItems = new AwardItems();
		if (HawkOSOperator.isEmptyString(info) || awardItems.init(info)) {
			return awardItems;
		}
		return null;
	}

	/**
	 * 从AwardId构造
	 *
	 * @return
	 */
	public static AwardItems valueOf(int awardId) {
		AwardItems awardItems = new AwardItems();
		awardItems.addAward(awardId);
		return awardItems;
	}

	public boolean isCountCheck() {
		return countCheck;
	}

	public void setCountCheck(boolean countCheck) {
		this.countCheck = countCheck;
	}

	/**
	 * 判断是否有奖励
	 */
	public boolean hasAwardItem() {
		return awardItems.size() > 0;
	}

	/**
	 * 获取所有奖励信息
	 *
	 * @return
	 */
	public List<ItemInfo> getAwardItems() {
		return new ArrayList<>(awardItems);
	}

	/**
	 * 克隆奖励对象
	 */
	@Override
	public AwardItems clone() {
		AwardItems newAward = new AwardItems();
		for (ItemInfo item : awardItems) {
			newAward.awardItems.add(item.clone());
		}
		return newAward;
	}

	/**
	 * 生成存储字符串
	 *
	 * @return
	 */
	public String toDbString() {
		String result = "";
		for (int i = 0; i < awardItems.size(); i++) {
			if (i > 0) {
				result += ",";
			}
			result += awardItems.get(i).toString();
		}
		return result;
	}

	/**
	 * 转换为字符串
	 */
	@Override
	public String toString() {
		String result = "";
		for (int i = 0; i < awardItems.size(); i++) {
			if (i > 0) {
				result += ",";
			}
			result += awardItems.get(i).toString();
		}
		return result;
	}

	/**
	 * 追加奖励
	 *
	 * @param awards
	 * @return
	 */
	public AwardItems appendAward(AwardItems awards) {
		if (awards != null) {
			for (ItemInfo item : awards.awardItems) {
				addItem(item.type, item.itemId, item.count);
			}
		}
		return this;
	}

	/**
	 * 追加奖励,不堆叠
	 *
	 * @param awards
	 * @return
	 */
	public AwardItems appendAwardDoNotStacked(AwardItems awards) {
		if (awards != null) {
			for (ItemInfo item : awards.awardItems) {
				addNewItem(item.type, item.itemId, item.count);
			}
		}
		return this;
	}

	/**
	 * 字符串初始化奖励对象
	 *
	 * @param info
	 * @return
	 */
	public boolean init(String info) {
		if (info != null && info.length() > 0 && !info.equals("0") && !info.equals("none")) {
			String[] awardItemArray = info.split(",");
			for (int i = 0; i < awardItemArray.length; i++) {
				String[] items = awardItemArray[i].split("_");
				if (items.length >= 3) {
					ItemInfo awardItem = new ItemInfo();
					awardItem.type = Integer.parseInt(items[0]);
					awardItem.itemId = Integer.parseInt(items[1]);
					awardItem.count = Long.parseLong(items[2]);
					addItem(awardItem);
				}
			}
			return awardItems.size() > 0;
		}
		return false;
	}

	/**
	 * 添加奖励
	 * 
	 * @param awardIds
	 */
	public void addAwards(List<Integer> awardIds) {
		awardIds.forEach(awardId -> addAward(awardId));
	}

	/**
	 * 添加奖励
	 * 
	 * @param awardId
	 * @return
	 */
	public boolean addAward(int awardId) {
		AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, awardId);
		if (awardCfg == null) {
			HawkLog.errPrintln("add award failed, unkown award config, awardId: {}, stack trace: {}", awardId, Thread.currentThread().getStackTrace());
			return false;
		}
		appendAward(awardCfg.getRandomAward());
		return true;
	}

	/**
	 * 添加物品
	 *
	 * @param item
	 * @return
	 */
	public AwardItems addItem(ItemInfo item) {
		if (item.getCount() == 0) {
			return this;
		}
		// 检测堆叠
		ItemInfo existItem = null;
		if (GameUtil.itemCanOverlap(item.type)) {
			for (ItemInfo tmpItem : awardItems) {
				if (item.type == tmpItem.type && item.itemId == tmpItem.itemId) {
					existItem = tmpItem;
					break;
				}
			}
		}

		// 添加
		if (existItem == null) {
			this.awardItems.add(item.clone());
		} else {
			// long count = existItem.count + item.count;
			// if (count >= Integer.MAX_VALUE) {
			// throw new RuntimeException("award item count too large, " + count);
			// }
			existItem.count += item.count;
		}

		return this;
	}

	/**
	 * 添加物品
	 *
	 * @param itemType
	 * @param itemId
	 * @param count
	 * @return
	 */
	public AwardItems addItem(int itemType, int itemId, long count) {
		ItemInfo awardItem = new ItemInfo();
		if (itemType < GsConst.ITEM_TYPE_BASE) {
			itemType *= GsConst.ITEM_TYPE_BASE;
		}

		awardItem.setType(itemType);
		awardItem.setItemId(itemId);
		awardItem.setCount(count);
		addItem(awardItem);
		return this;
	}

	/**
	 * 添加邮件附件奖励物品
	 * 
	 * @param itemType
	 * @param itemId
	 * @param count
	 * @param player
	 * @param action
	 * @return
	 */
	public AwardItems addMailItem(int itemType, int itemId, long count, Player player, Action action, int mailId, String uuid) {
		MailRewardItemLogTask logTask = new MailRewardItemLogTask(itemType, itemId, count, player, action, mailId, uuid);
		mailLogTaskList.add(logTask);
		return this.addItem(itemType, itemId, count);
	}

	/**
	 * 添加一个新的奖励类型, 不检查叠加
	 * 
	 * @param itemType
	 * @param itemId
	 * @param count
	 */
	public void addNewItem(int itemType, int itemId, long count) {
		ItemInfo awardItem = new ItemInfo();
		if (itemType < GsConst.ITEM_TYPE_BASE) {
			itemType *= GsConst.ITEM_TYPE_BASE;
		}

		awardItem.setType(itemType);
		awardItem.setItemId(itemId);
		awardItem.setCount(count);

		this.awardItems.add(awardItem.clone());
	}

	/**
	 * 添加物品(不累加合并，用于记录每次的记录)
	 *
	 * @param itemType
	 * @param itemId
	 * @param count
	 * @return
	 */
	public AwardItems addItemNotAccumulation(int itemType, int itemId, long count) {
		ItemInfo awardItem = new ItemInfo();
		if (itemType < GsConst.ITEM_TYPE_BASE) {
			itemType *= GsConst.ITEM_TYPE_BASE;
		}
		awardItem.setType(itemType);
		awardItem.setItemId(itemId);
		awardItem.setCount(count);
		this.awardItems.add(awardItem);
		return this;
	}

	/**
	 * 添加物品列表
	 * 
	 * @param itemInfos
	 * @return
	 */
	public AwardItems addItemInfos(List<ItemInfo> itemInfos) {
		for (ItemInfo itemInfo : itemInfos) {
			addItem(itemInfo);
		}
		return this;
	}

	/**
	 * 获取奖励中物品信息
	 * 
	 * @param type
	 * @param itemId
	 * @return
	 */
	public ItemInfo getItem(int type, int itemId) {
		for (ItemInfo item : awardItems) {
			if (item.getType() == type && item.getItemId() == itemId) {
				return item;
			}
		}
		ItemInfo item = new ItemInfo();
		item.setType(type);
		item.setItemId(itemId);
		awardItems.add(item);
		return item;
	}

	/**
	 * 判断是否（全）是资源奖励
	 * 
	 * @return
	 */
	public boolean isResourceItem() {
		for (ItemInfo item : awardItems) {
			if (!GameUtil.isResItem(item)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 玩家添加资源
	 * 
	 * @param gold
	 * @return
	 */
	public AwardItems addResource(int resourceType, long count) {
		getItem(ItemType.PLAYER_ATTR_VALUE * GsConst.ITEM_TYPE_BASE, resourceType).addCount(count);
		return this;
	}

	/**
	 * 玩家属性修改
	 * 
	 * @param gold
	 * @return
	 */
	public AwardItems addGold(long gold) {
		getItem(ItemType.PLAYER_ATTR_VALUE * GsConst.ITEM_TYPE_BASE, PlayerAttr.GOLD_VALUE).addCount(gold);
		return this;
	}

	/**
	 * 玩家属性修改
	 * 
	 * @param vitPoint
	 * @return
	 */
	public AwardItems addVit(int vitPoint) {
		getItem(ItemType.PLAYER_ATTR_VALUE * GsConst.ITEM_TYPE_BASE, PlayerAttr.VIT_VALUE).addCount(vitPoint);
		return this;
	}

	/**
	 * 玩家属性修改
	 * 
	 * @param coin
	 * @return
	 */
	public AwardItems addCoin(int coin) {
		getItem(ItemType.PLAYER_ATTR_VALUE * GsConst.ITEM_TYPE_BASE, PlayerAttr.COIN_VALUE).addCount(coin);
		return this;
	}

	/**
	 * 玩家属性修改
	 * 
	 * @param level
	 * @return
	 */
	public AwardItems setLevel(int level) {
		getItem(ItemType.PLAYER_ATTR_VALUE * GsConst.ITEM_TYPE_BASE, PlayerAttr.LEVEL_VALUE).addCount(level);
		return this;
	}

	/**
	 * 玩家属性修改
	 * 
	 * @param exp
	 * @return
	 */
	public AwardItems addExp(int exp) {
		getItem(ItemType.PLAYER_ATTR_VALUE * GsConst.ITEM_TYPE_BASE, PlayerAttr.EXP_VALUE).addCount(exp);
		return this;
	}

	/**
	 * 玩家属性修改
	 * 
	 * @param value
	 * @return
	 */
	public AwardItems addVipExp(int value) {
		getItem(ItemType.PLAYER_ATTR_VALUE * GsConst.ITEM_TYPE_BASE, PlayerAttr.VIP_POINT_VALUE).addCount(value);
		return this;
	}

	@SafeVarargs
	public static boolean rewardTaskAffectAndPush(Player player, Action action, RewardOrginType orginType, List<ItemInfo>... listArray) {
		AwardItems awardItems = AwardItems.valueOf();
		for (List<ItemInfo> itemList : listArray) {
			awardItems.addItemInfos(itemList);
		}

		return awardItems.rewardTakeAffectAndPush(player, action, true, true, orginType).first;
	}

	/**
	 * 发放奖励
	 *
	 * @param player
	 * @param action
	 */
	public boolean rewardTakeAffectAndPush(Player player, Action action) {
		if (hasAwardItem()) {
			return rewardTakeAffectAndPush(player, action, true, false, null).first;
		}

		return true;
	}

	/**
	 * 发放奖励, 附带奖励来源
	 * 
	 * @param player
	 * @param action
	 * @param orginType
	 *            奖励来源类型
	 * @return
	 */
	public boolean rewardTakeAffectAndPush(Player player, Action action, RewardOrginType orginType) {
		if (hasAwardItem()) {
			boolean push = orginType != RewardOrginType.BUILDING_LEVELUP_REWARD;
			return rewardTakeAffectAndPush(player, action, push, true, orginType).first;
		}

		return true;
	}

	/**
	 * 发放奖励, 客户端弹框显示
	 *
	 * @param player
	 * @param action
	 * @param popup
	 *            控制前端是否要弹框显示奖励数据
	 */
	public RewardInfo.Builder rewardTakeAffectAndPush(Player player, Action action, boolean popup) {
		if (hasAwardItem()) {
			return rewardTakeAffectAndPush(player, action, popup, null);
		}

		return null;
	}

	/**
	 * 发放奖励, 附带奖励来源, 客户端弹框显示
	 * 
	 * @param player
	 * @param action
	 * @param popup
	 *            控制前端是否要弹框显示奖励数据
	 * @param orginType
	 *            奖励来源类型
	 * @param orginArgs
	 *            奖励来源相关参数
	 * @return
	 */
	public RewardInfo.Builder rewardTakeAffectAndPush(Player player, Action action, boolean popup, RewardOrginType orginType, int... orginArgs) {
		if (hasAwardItem()) {
			boolean push = orginType != RewardOrginType.BUILDING_LEVELUP_REWARD;
			return rewardTakeAffectAndPush(player, action, push, popup, orginType, orginArgs).second;
		}

		return null;
	}

	/**
	 * 发放邮件附件奖励
	 * 
	 * @param player
	 * @param action
	 * @param popup
	 * @param orginType
	 * @param orginArgs
	 * @return
	 */
	public RewardInfo.Builder rewardTakeMailAffectAndPush(Player player, Action action, boolean popup, RewardOrginType orginType, int... orginArgs) {
		keepItemFlow = false;
		RewardInfo.Builder result = this.rewardTakeAffectAndPush(player, action, popup, orginType, orginArgs);
		mailLogTaskList.forEach(task -> task.run());
		return result;
	}

	/**
	 * 发放奖励, 附带奖励来源
	 * 
	 * @param player
	 * @param action
	 * @param push
	 *            控制是否要向前端推送奖励数据
	 * @param popup
	 *            控制前端是否要弹框显示奖励数据
	 * @param orginType
	 *            奖励来源类型
	 * @param orginArgs
	 *            奖励来源相关参数
	 * @return
	 */
	private HawkTuple2<Boolean, RewardInfo.Builder> rewardTakeAffectAndPush(Player player, Action action, boolean push, boolean popup, RewardOrginType orginType,
			int... orginArgs) {
		HawkTuple2<Boolean, RewardInfo.Builder> tuple = rewardTakeAffect(player, action, push);

		if (!awardItems.isEmpty()) {
			HPPlayerReward.Builder rewardBuilder = HPPlayerReward.newBuilder();

			RewardInfo.Builder rewardInfo = tuple.second;

			if (showItems != null && !showItems.isEmpty()) {
				rewardInfo.clearShowItems();
				rewardInfo.addAllShowItems(showItems);
			}

			rewardBuilder.setRewards(rewardInfo);

			if (popup) {
				rewardBuilder.setFlag(1);
			}

			if (orginType != null) {
				rewardBuilder.setOrgin(orginType.getNumber());
			}

			for (int arg : orginArgs) {
				rewardBuilder.addOrginArgs(arg);
			}

			player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_AWARD_S_VALUE, rewardBuilder));
		} else {
			HawkLog.errPrintln("rewardItems is empty, playerId: {}, action: {}, push: {}, popup: {}, orginType: {}, orginArgs: {}", player.getId(), action.name(), push, popup,
					orginType, orginArgs.length > 0 ? orginArgs : null);
		}

		return tuple;
	}

	/**
	 * 功能发放奖励
	 * 
	 * @param player
	 * @param action
	 * @return
	 */
	public HawkTuple2<Boolean, RewardInfo.Builder> rewardTakeAffect(Player player, Action action) {
		if (hasAwardItem()) {
			return rewardTakeAffect(player, action, false);
		}

		return null;
	}

	/**
	 * 功能发放奖励
	 *
	 * @param player
	 * @param action
	 * @param push
	 *            控制是否要向前端推送奖励数据
	 * @return
	 */
	private HawkTuple2<Boolean, RewardInfo.Builder> rewardTakeAffect(Player player, Action action, boolean push) {
		RewardInfo.Builder builder = RewardInfo.newBuilder();
		// 转换在items中的属性奖励
		for (ItemInfo item : awardItems) {
			// 传输细节类型
			item.setType(GameUtil.convertToStandardItemType(item.type));

			// 配置出错误
			if (item.getCount() <= 0 || item.getItemId() <= 0) {
				continue;
			}
		}

		// 玩家属性变更
		int playerLevel = player.getLevel();
		int playerVipLevel = player.getVipLevel();
		int beforeVipExp = player.getVipExp();

		AwardHelper awardHelper = new AwardHelper();
		// 实体对象类奖励
		boolean result = false;
		if (checkAwardItems(player, action)) {
			result = deliverItemAwards(player, builder, action, awardHelper, push);
		}

		// 添加奖励显示
		for (ItemInfo item : awardItems) {
			builder.addShowItems(item.toRewardItem());
		}

		if (builder.getDiamond() > 0) {
			player.getPush().syncPlayerDiamonds();
		}

		// 集中同步非玩家基础属性类奖励信息
		awardHelper.syncAward(player);

		// 玩家升级奖励
		awardHelper.playerLevelUpReward(player, playerLevel);

		// vip升级奖励
		awardHelper.vipLevelUpAward(player, playerVipLevel, beforeVipExp, action);

		return new HawkTuple2<Boolean, RewardInfo.Builder>(result, builder);
	}

	private boolean checkAwardItems(Player player, Action action) {
		try {
			for (ItemInfo item : awardItems) {
				if (!awardItemCheck(player.getId(), item, action)) {
					continue;
				}

				int itemType = item.type / GsConst.ITEM_TYPE_BASE;
				if (itemType == Const.ItemType.PLAYER_ATTR_VALUE) {
					// 玩家属性
					switch (item.getItemId()) {
					case PlayerAttr.GOLD_VALUE:
					case PlayerAttr.COIN_VALUE:
					case PlayerAttr.LEVEL_VALUE:
					case PlayerAttr.EXP_VALUE:
					case PlayerAttr.VIP_POINT_VALUE:
					case PlayerAttr.VIT_VALUE:
					case PlayerAttr.GOLDORE_VALUE:
					case PlayerAttr.GOLDORE_UNSAFE_VALUE:
					case PlayerAttr.OIL_VALUE:
					case PlayerAttr.OIL_UNSAFE_VALUE:
					case PlayerAttr.STEEL_VALUE:
					case PlayerAttr.STEEL_UNSAFE_VALUE:
					case PlayerAttr.TOMBARTHITE_VALUE:
					case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
					case PlayerAttr.GUILD_CONTRIBUTION_VALUE:
					case PlayerAttr.MILITARY_SCORE_VALUE:
					case PlayerAttr.ACTIVITY_SCORE_VALUE:
					case PlayerAttr.GUILD_SCORE_VALUE:
					case PlayerAttr.MILITARY_EXP_VALUE:
					case PlayerAttr.CYBORG_SCORE_VALUE:
					case PlayerAttr.TRAVEL_SHOP_FRIENDLY_VALUE:
					case PlayerAttr.DYZZ_SCORE_VALUE:
					case PlayerAttr.CROSS_TALENT_POINT_VALUE:
					case PlayerAttr.NATION_MILITARY_BATTLE_VALUE:
					case PlayerAttr.NATION_MILITARY_VALUE:
						break;
					// 钻石只有通过充值渠道获得！！！
					case PlayerAttr.DIAMOND_VALUE:
						throw new RuntimeException("diamond award error");

					default:
						HawkLog.errPrintln("unsupport award itemId: {}, playerId: {}", item.getItemId(), player.getId());
						throw new RuntimeException(String.format("unsupport award itemId: %d", item.getItemId()));
					}
				} else {
					switch (itemType) {
					case Const.ItemType.TOOL_VALUE: {
						ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, item.getItemId());
						if (itemCfg == null) {
							HawkLog.errPrintln("award item config error, itemId: {}, playerId: {}", item.getItemId(), player.getId());
							throw new RuntimeException(String.format("award item config error, itemId: %d", item.getItemId()));
						}
						break;
					}

					case Const.ItemType.SOLDIER_VALUE: {
						int armyId = item.getItemId();
						BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
						if (armyCfg == null || item.getCount() <= 0) {
							HawkLog.errPrintln("award soldier config error, armyId: {}, count: {}, playerId: {}", armyId, item.getCount(), player.getId());
							throw new RuntimeException(String.format("award soldier config error, armyId: %d, count: %d", armyId, item.getCount()));
						}
						break;
					}

					case Const.ItemType.EQUIP_VALUE: {
						int equipCfgId = item.getItemId();
						EquipmentCfg equipCfg = HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, equipCfgId);
						if (equipCfg == null || item.getCount() <= 0) {
							HawkLog.errPrintln("award equip config error, equipId: {}, count: {}, playerId: {}", equipCfgId, item.getCount(), player.getId());
							throw new RuntimeException(String.format("award equip config error, equipId: %d, count: %d", equipCfgId, item.getCount()));
						}
						break;
					}
					
					case Const.ItemType.ARMOUR_VALUE: {
						ArmourPoolCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ArmourPoolCfg.class, item.getItemId());
						if (cfg == null || item.getCount() <= 0) {
							HawkLog.errPrintln("award amour config error, equipId: {}, count: {}, playerId: {}", item.getItemId(), item.getCount(), player.getId());
							throw new RuntimeException(String.format("award amour config error, equipId: %d, count: %d", item.getItemId(), item.getCount()));
						}
						break;
					}
					
					case Const.ItemType.MECHA_CORE_MODULE_VALUE: {
						MechaCoreModuleCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleCfg.class, item.getItemId());
						if (cfg == null || item.getCount() <= 0) {
							HawkLog.errPrintln("award mechacore module config error, moduleId: {}, count: {}, playerId: {}", item.getItemId(), item.getCount(), player.getId());
							throw new RuntimeException(String.format("award mechacore module config error, moduleId: %d, count: %d", item.getItemId(), item.getCount()));
						}
						break;
					}

					default:
						HawkLog.errPrintln("unsupport award itemType: {}, playerId: {}", itemType, player.getId());
						throw new RuntimeException(String.format("unsupport award itemType: %d", itemType));
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}

		return true;
	}

	/**
	 * 具体发放奖励
	 *
	 * @param player
	 * @param action
	 * @param async
	 * @return award列表中的物品，只要有一个发放失败，就返回false，只有全发放成功，才返回true
	 */
	private boolean deliverItemAwards(Player player, RewardInfo.Builder builder, Action action, AwardHelper awardHelper, boolean push) {

		boolean result = true;

		// 实体对象奖励(/10000)
		for (ItemInfo item : awardItems) {
			if (!awardItemCheck(player.getId(), item, action)) {
				continue;
			}

			int itemType = item.type / GsConst.ITEM_TYPE_BASE;
			try {
				if (itemType == Const.ItemType.PLAYER_ATTR_VALUE) {
					// 玩家属性
					switch (item.getItemId()) {
					// 钻石只有通过充值渠道获得！！！
					case PlayerAttr.DIAMOND_VALUE:
						throw new RuntimeException("diamond award error");

					case PlayerAttr.GOLD_VALUE:
						player.increaseGold(item.getCount(), action, keepItemFlow);
						builder.setGold(player.getGold());
						break;

					case PlayerAttr.COIN_VALUE:
						player.increaseCoin((int) item.getCount(), action);
						builder.setCoin(player.getCoin());
						break;

					case PlayerAttr.LEVEL_VALUE:
						player.increaseLevel((int) item.getCount(), action);
						builder.setLevel(player.getLevel());
						break;

					case PlayerAttr.EXP_VALUE:
						player.increaseExp((int) item.getCount(), action, push);
						builder.setExp(player.getPlayerBaseEntity().getExp());
						builder.setLevel(player.getLevel());
						break;

					case PlayerAttr.VIP_POINT_VALUE:
						player.increaseVipExp((int) item.getCount(), action);
						awardHelper.addVipExp((int) item.getCount());
						builder.setVipPoint(player.getEntity().getVipExp());
						builder.setVipLevel(player.getVipLevel());
						break;

					case PlayerAttr.VIT_VALUE:
						player.increaseVit((int) item.getCount(), action, false);
						builder.setVit(player.getVit());
						break;

					case PlayerAttr.GOLDORE_VALUE:
						player.increaseResource(item.count, item.getItemId(), action, keepItemFlow);
						builder.setGoldore(player.getGoldore());
						break;

					case PlayerAttr.GOLDORE_UNSAFE_VALUE:
						player.increaseResource(item.count, item.getItemId(), action, keepItemFlow);
						builder.setGoldoreUnsafe(player.getGoldoreUnsafe());
						break;

					case PlayerAttr.OIL_VALUE:
						player.increaseResource(item.count, item.getItemId(), action, keepItemFlow);
						builder.setOil(player.getOil());
						break;

					case PlayerAttr.OIL_UNSAFE_VALUE:
						player.increaseResource(item.count, item.getItemId(), action, keepItemFlow);
						builder.setOilUnsafe(player.getOilUnsafe());
						break;

					case PlayerAttr.STEEL_VALUE:
						player.increaseResource(item.count, item.getItemId(), action, keepItemFlow);
						builder.setSteel(player.getSteel());
						break;
					case PlayerAttr.STEEL_UNSAFE_VALUE:
						player.increaseResource(item.count, item.getItemId(), action, keepItemFlow);
						builder.setSteelUnsafe(player.getSteelUnsafe());
						break;
					case PlayerAttr.TOMBARTHITE_VALUE:
						player.increaseResource(item.count, item.getItemId(), action, keepItemFlow);
						builder.setTombarthite(player.getTombarthite());
						break;
					case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
						player.increaseResource(item.count, item.getItemId(), action, keepItemFlow);
						builder.setTombarthiteUnsafe(player.getTombarthiteUnsafe());
						break;
					case PlayerAttr.GUILD_CONTRIBUTION_VALUE: // 联盟捐献荣誉值
						player.increaseGuildContribution((int) item.count, action, keepItemFlow);
						builder.setGuildContribution(player.getGuildContribution());
						break;
					case PlayerAttr.MILITARY_SCORE_VALUE: // 军演积分
						player.increaseMilitaryScore((int) item.count, action, keepItemFlow);
						builder.setMilitaryScore(player.getMilitaryScore());
						break;

					case PlayerAttr.ACTIVITY_SCORE_VALUE:
						ActivityManager.getInstance().postEvent(new AddScoreEvent(player.getId(), (int) item.count));
						break;

					case PlayerAttr.GUILD_SCORE_VALUE:
						GuildService.getInstance().incGuildScore(player.getGuildId(), (int) item.count);
						break;

					case PlayerAttr.MILITARY_EXP_VALUE:
						player.increaseMilitaryRankExp((int) item.count, action);
						break;
					case PlayerAttr.CYBORG_SCORE_VALUE: // 赛博积分
						player.increaseCyborgScore((int) item.count, action, keepItemFlow);
						builder.setCyborgScore(player.getCyborgScore());
						break;
					case PlayerAttr.TRAVEL_SHOP_FRIENDLY_VALUE:
						PlayerTravelShopModule travelShopModule = player.getModule(GsConst.ModuleType.TRAVEL_SHOP);
						travelShopModule.travelShopFriendlyAddPush(item.count);
						break;
					case PlayerAttr.DYZZ_SCORE_VALUE:
						player.increaseDYZZScore((int) item.count, action, keepItemFlow);
						builder.setDyzzScore(player.getDYZZScore());
						break;
					case PlayerAttr.CROSS_TALENT_POINT_VALUE:
						player.increaseCrossTalentPoint((int) item.count, action, keepItemFlow);
						break;
					case PlayerAttr.NATION_MILITARY_VALUE:
						int addCnt = player.increaseNationMilitary((int) item.count, item.getItemId(), action, keepItemFlow);
						builder.setNationMilitary(addCnt);// 军功有上限, 以实际加的为准 
						break;
					default:
						HawkLog.errPrintln("unsupport award itemId: {}, playerId: {}", item.getItemId(), player.getId());
						throw new RuntimeException(String.format("unsupport award itemId: %d", item.getItemId()));
					}
				} else {
					switch (itemType) {
					case Const.ItemType.TOOL_VALUE:
						if (!awardItem(player, item, action, awardHelper)) {
							return false;
						}
						break;

					case Const.ItemType.SOLDIER_VALUE:
						if (!awardSoldier(player, item, action, awardHelper)) {
							return false;
						}
						break;
					case Const.ItemType.EQUIP_VALUE:
						if (!awardEquip(player, item, awardHelper)) {
							return false;
						}
						break;
					case Const.ItemType.ARMOUR_VALUE:
						for (int i = 0; i < item.getCount(); i++) {
							player.addArmour(item.getItemId());
						}
						break;
					case Const.ItemType.MECHA_CORE_MODULE_VALUE:
						for (int i = 0; i < item.getCount(); i++) {
							player.getPlayerMechaCore().addModule(item.getItemId());
						}
						break;

					default:
						HawkLog.errPrintln("unsupport award itemType: {}, playerId: {}", itemType, player.getId());
						throw new RuntimeException(String.format("unsupport award itemType: %d", itemType));
					}
				}
			} catch (Exception e) {
				result = false;
				HawkException.catchException(e);
				// 此处不能直接return，否则正常的奖励也被中断了
			}
		}

		return result;
	}

	/**
	 * 奖励校验
	 * 
	 * @param item
	 * @return
	 */
	private boolean awardItemCheck(String playerId, ItemInfo item, Action action) {
		// 配置出错误
		if (item.getCount() <= 0 || item.getItemId() <= 0) {
			return false;
		}

		// 判断奖励失效
		if (SysControlProperty.getInstance().isDisableAward(item.getType(), item.getItemId())) {
			logger.error("deliverItemAwards disabled, playerId:{}, itemType:{}, itemId:{}, itemCount:{}, action:{}",
					playerId, item.getType(), item.getItemId(), item.getCount(), action);
			return false;
		}

		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.ITEM_GET, item.getItemId())) {
			logger.error("item get closed, playerId:{}, itemType:{}, itemId:{}, itemCount:{}, action:{}",
					playerId, item.getType(), item.getItemId(), item.getCount(), action);
			return false;
		}

		return true;
	}

	/**
	 * 奖励道具
	 * 
	 * @param player
	 * @param item
	 * @param action
	 * @return
	 */
	private boolean awardItem(Player player, ItemInfo item, Action action, AwardHelper awardHelper) {
		// 物品配置
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, item.getItemId());
		if (itemCfg == null) {
			return false;
		}

		List<ItemEntity> items = null;
		if (this.countCheck) {
			items = player.increaseTools(item, action, itemCfg);
		} else {
			items = player.increaseTools(item, action, itemCfg, false);
		}

		if (items != null && items.size() > 0) {
			if (keepItemFlow && action != Action.TOOL_BUY) {
				// 此处不是购买道具不花钱，所以moneyType填什么无所谓
				LogUtil.logItemFlow(player, action, LogInfoType.goods_add, itemCfg.getItemType(), item.getItemId(), item.getCount(), 0, IMoneyType.MT_GOLD);
			}

			for (ItemEntity entity : items) {
				awardHelper.addItem(entity.getId());
			}
		}

		return true;
	}

	/**
	 * 士兵或城防武器奖励
	 * 
	 * @param player
	 * @param item
	 * @return
	 */
	private boolean awardSoldier(Player player, ItemInfo item, Action action, AwardHelper awardHelper) {
		int armyId = item.getItemId();
		BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		if (armyCfg == null || item.getCount() <= 0) {
			return false;
		}

		ArmyEntity armyEntity = player.getData().getArmyEntity(armyId);
		if (armyEntity == null) {
			armyEntity = new ArmyEntity();
			armyEntity.setPlayerId(player.getId());
			armyEntity.setArmyId(armyId);
			armyEntity.addFree((int) item.getCount());
			if (HawkDBManager.getInstance().create(armyEntity)) {
				player.getData().addArmyEntity(armyEntity);
			}
		} else {
			armyEntity.addFree((int) item.getCount());
		}

		// 城防武器（陷阱）需要判断上限
		if (armyCfg.isDefWeapon()) {
			armyEntity.addFreeWithLimit(player.getData().getTrapCapacity());
		}

		ArmyChangeReason reason = ArmyChangeReason.AWARD;
		if (action == Action.TRAIN_SOLDIER_CANCEL) {
			reason = ArmyChangeReason.ADVANCE_CANCEL;
		}

		LogUtil.logArmyChange(player, armyEntity, (int) item.getCount(), ArmySection.FREE, reason);
		awardHelper.addSoldier(armyId, (int) item.getCount());
		// 如果是在副本中, 需要把兵带出去
		if (player.getLmjyState() == PState.GAMEING) {
			LocalRedis.getInstance().lmjyIncreaseCreateSoldier(player.getId(), armyId, (int) item.getCount());
		}
		if (player.getTBLYState() == TBLYState.GAMEING || player.getCYBORGState() == CYBORGState.GAMEING) {
			RedisProxy.getInstance().jbsIncreaseCreateSoldier(player.getId(), armyId, (int) item.getCount());
		}
		
		MissionManager.getInstance().postMsg(player, new EventSoldierCalc());
		return true;
	}

	/**
	 * 装备奖励
	 * 
	 * @param player
	 * @param item
	 * @return
	 */
	private boolean awardEquip(Player player, ItemInfo item, AwardHelper awardHelper) {
		int equipCfgId = item.getItemId();
		EquipmentCfg equipCfg = HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, equipCfgId);
		if (equipCfg == null || item.getCount() <= 0) {
			return false;
		}

		List<EquipEntity> equipList = new ArrayList<>();
		for (int i = 0; i < item.getCount(); i++) {
			EquipEntity equipEntity = new EquipEntity();
			equipEntity.setCfgId(equipCfg.getId());
			equipEntity.setPlayerId(player.getId());
			equipEntity.setState(EquipState.FREE_VALUE);

			if (!HawkDBManager.getInstance().create(equipEntity)) {
				HawkLog.errPrintln("awardItem create equip failed, playerId: {}, equipCfg: {}", player.getId(), equipCfg.getId());
				continue;
			}
			equipList.add(equipEntity);
		}
		player.getData().addEquipEntities(equipList);

		for (EquipEntity equip : equipList) {
			LogUtil.logEquipmentAttrChange(player, equip.getId(), equipCfg.getId(), false, equipCfg.getPos(),
					equipCfg.getPower(), equipCfg.getQuality(), equipCfg.getLevel());
		}

		awardHelper.addEquip(equipList);
		return true;
	}
	
	/**
	 * 按百分比缩放奖励(向上取整)
	 * 
	 * @param d
	 */
	public AwardItems scale(double factor) {
		for (ItemInfo item : awardItems) {
			if (item.getCount() <= 0 || item.getItemId() <= 0) {
				continue;
			}

			double scaleCount = factor * item.getCount();
			item.setCount((long) Math.ceil(scaleCount));
		}
		return this;
	}

	/**
	 * 获取奖励中的各类物品的数量
	 * 
	 * @return
	 */
	public Map<Integer, Long> getAwardItemsCount() {
		Map<Integer, Long> itemsCountMap = new HashMap<Integer, Long>();
		for (ItemInfo item : awardItems) {
			if (item.getCount() <= 0 || item.getItemId() <= 0) {
				continue;
			}

			itemsCountMap.put(item.getItemId(), item.getCount());
		}
		return itemsCountMap;
	}

	/**
	 * 获取道具id和数量
	 * 
	 * @return
	 */
	public HawkTuple2<String, String> getItemsInfo() {
		StringBuilder itemIds = new StringBuilder();
		StringBuilder itemCounts = new StringBuilder();
		for (ItemInfo item : awardItems) {
			if (item.getCount() <= 0 || item.getItemId() <= 0 || item.getType() / GsConst.ITEM_TYPE_BASE == Const.ItemType.PLAYER_ATTR_VALUE) {
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

	public List<RewardItem> getShowItems() {
		return showItems;
	}

	public void setShowItems(List<RewardItem> showItems) {
		this.showItems = showItems;
	}
}
