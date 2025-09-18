package com.hawk.game.service;

import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.math.HawkMathExpress;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.WishingEvent;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.WishingWellCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.WishingWellEntity;
import com.hawk.game.entity.item.WishingCountItem;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.log.Action;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Wishing.PlayerWishingResp;
import com.hawk.game.protocol.Wishing.WishingCountInfo;
import com.hawk.game.protocol.Wishing.WishingInfoSync;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventWishing;
import com.hawk.game.util.LogUtil;
import com.hawk.util.TimeUtil;

/**
 * 许愿池逻辑
 * @author PhilChen
 *
 */
public class WishingService {

	static final Logger logger = LoggerFactory.getLogger("Server");

	private static WishingService service;

	private WishingService() {
	}

	public static WishingService getInstance() {
		if (service == null) {
			service = new WishingService();
		}
		return service;
	}

	/**
	 * 刷新许愿池信息（重置数据）
	 * @param player
	 */
	public void refreshWishingInfo(Player player) {
		WishingWellEntity wishingEntity = player.getData().getWishingEntity();
		long now = HawkTime.getMillisecond();
		// 跨天零点重置数据
		if (wishingEntity.getLastWishTime() == 0 || !TimeUtil.isNeedReset(0, now, wishingEntity.getLastWishTime())) {
			syncWishingInfo(player);
			return;
		}
		// 重置数据
		wishingEntity.cleanCount();
		wishingEntity.setLastWishTime(0);
		logger.info("[wishing] reset player wishing. playerId{}", player.getId());
		syncWishingInfo(player);
	}

	/**
	 * 同步许愿池信息
	 */
	public void syncWishingInfo(Player player) {
		WishingWellEntity wishingEntity = player.getData().getWishingEntity();
		WishingInfoSync.Builder builder = WishingInfoSync.newBuilder();
		Map<Integer, WishingCountItem> wishCountMap = wishingEntity.getTodayWishCountMap();
		int freeWishCount = 0;
		
		for (WishingCountItem item : wishCountMap.values()) {
			WishingCountInfo.Builder countInfo = WishingCountInfo.newBuilder();
			countInfo.setResourceType(PlayerAttr.valueOf(item.getResourceType()));
			countInfo.setTodayFreeWishCount(item.getFreeCount());
			countInfo.setTodayCostWishCount(item.getCostCount());
			countInfo.setTodayExtraWishCount(item.getExtraCount());
			builder.addWishCount(countInfo);
			freeWishCount += item.getFreeCount();
		}
		int level = player.getData().getBuildingMaxLevel(BuildingType.WISHING_WELL_VALUE);
		WishingWellCfg buildConfig = HawkConfigManager.getInstance().getConfigByKey(WishingWellCfg.class, level);
		int freeCount = 0;
		if (buildConfig != null) {
			freeCount = buildConfig.getFreeCount();
		}
		int remainCount;
		if (freeWishCount < freeCount) {
			remainCount = freeCount - freeWishCount + wishingEntity.getExtraWishCount();
		} else {
			remainCount = wishingEntity.getExtraWishCount();
		}
		builder.setRemainFreeCount(remainCount < 0 ? 0 : remainCount);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WISHING_INFO_SYNC_S_VALUE, builder));
		
		// 许愿池建筑状态
		BuildingStatus status = remainCount > 0 ? BuildingStatus.WISHING_FREE : BuildingStatus.COMMON;
		BuildingBaseEntity wishEntity = player.getData().getBuildingEntityByType(BuildingType.WISHING_WELL);
		if (wishEntity != null &&wishEntity.getStatus() != status.getNumber()) {
			wishEntity.setStatus(status.getNumber());
			player.getPush().pushBuildingStatus(wishEntity, status);
		}
	}

	/**
	 * 玩家许愿操作
	 * @param player
	 * @param resourceType
	 * @return
	 */
	public Result<PlayerWishingResp.Builder> playerWishing(Player player, PlayerAttr resourceType) {
		WishingWellEntity wishingEntity = player.getData().getWishingEntity();
		int level = player.getData().getBuildingMaxLevel(BuildingType.WISHING_WELL_VALUE);
		WishingWellCfg buildConfig = HawkConfigManager.getInstance().getConfigByKey(WishingWellCfg.class, level);
		if (buildConfig == null) {
			return Result.fail(Status.Error.WISHING_LEVEL_CONFIG_NOT_FOUND_VALUE);
		}
		int todayWishCount = wishingEntity.getTodayTotalWishCount();
		int todayFreeCount = wishingEntity.getTodayFreeWishCount();
		
		if (todayWishCount >= buildConfig.getMaxCount()) {
			return Result.fail(Status.Error.WISHING_TODAY_IS_MAX_COUNT_VALUE);
		}
		// 暴击
		Map<Integer, Integer> wishingCritRate = ConstProperty.getInstance().getWishingCritRate();
		Integer crit = HawkRand.randomWeightObject(wishingCritRate);
		if (crit == null) {
			return Result.fail(Status.Error.WISHING_CRIT_CONFIG_ERROR_VALUE);
		}
		
		int costMoney = 0;
		// 优先扣除免费次数
		if (todayFreeCount >= buildConfig.getFreeCount()) {
			// 没有免费次数时，扣除额外次数
			if (wishingEntity.getExtraWishCount() > 0) {
				wishingEntity.setExtraWishCount(wishingEntity.getExtraWishCount() - 1);
				wishingEntity.addExtraCount(resourceType, 1);
			} else {
				// 没有额外次数时，计算付费次数
				int result = consumeWishCost(player, resourceType, buildConfig, wishingEntity.getCostCount(resourceType) + 1);
				if (result > 0) {
					return Result.fail(result);
				}
				
				costMoney = Math.abs(result);
				// 增加消费次数
				wishingEntity.addCostCount(resourceType, 1);
			}
		} else {
			// 增加一次免费许愿次数
			wishingEntity.addFreeCount(resourceType, 1);
		}
		
		int addId = 0;
		int baseValue = 0;
		switch (resourceType) {
		case OIL:
		case OIL_UNSAFE:
			addId = PlayerAttr.OIL_UNSAFE_VALUE;
			baseValue = buildConfig.getOil();
			break;
		case GOLDORE:
		case GOLDORE_UNSAFE:
			addId = PlayerAttr.GOLDORE_UNSAFE_VALUE;
			baseValue = buildConfig.getGoldore();
			break;
		case STEEL:
		case STEEL_UNSAFE:
			addId = PlayerAttr.STEEL_UNSAFE_VALUE;
			baseValue = buildConfig.getSteel();
			break;
		case TOMBARTHITE:
		case TOMBARTHITE_UNSAFE:
			addId = PlayerAttr.TOMBARTHITE_UNSAFE_VALUE;
			baseValue = buildConfig.getTombarthite();
			break;
		default:
			return Result.fail(Status.Error.WISHING_RESOURCE_TYPE_NOT_FOUND_VALUE);
		}
		// 增加许愿次数
		int wishCount = wishingEntity.getWishingCount(resourceType);
		wishingEntity.setLastWishTime(HawkTime.getMillisecond());

		// 添加物品
		// 资源数量根次数有关
		String expr = ConstProperty.getInstance().getWishingAddValueExpr();
		baseValue = (int) Math.ceil(HawkMathExpress.calculate(expr, baseValue, wishCount));
		int addValue = baseValue;
		if (crit > 1) {
			addValue = baseValue * crit;
		}
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItem(ItemType.PLAYER_ATTR_VALUE, addId, addValue);
		awardItem.rewardTakeAffectAndPush(player, Action.WISHING);

		// 军需处花费黄金购买资源打点记录
		if (costMoney > 0) {
			LogUtil.logWishingCostFlow(player, addId, addValue, costMoney);
		}
		
		ActivityManager.getInstance().postEvent(new WishingEvent(player.getId()));
		MissionManager.getInstance().postMsg(player, new EventWishing());
		
		logger.info("[wishing] player wishing. playerId={} resourceType={} wishCount={} addId={} addValue={} crit={}", player.getId(), resourceType,
				wishCount, addId, addValue, crit);

		// 同步许愿池信息
		syncWishingInfo(player);

		PlayerWishingResp.Builder builder = PlayerWishingResp.newBuilder();
		builder.setMultiple(crit);
		builder.setBaseValue(baseValue);
		return Result.success(builder);
	}

	/**
	 * 扣除许愿花费
	 * @param resourceType
	 * @param buildConfig
	 * @return
	 */
	private int consumeWishCost(Player player, PlayerAttr resourceType, WishingWellCfg buildConfig, int wishCount) {
		// 付费次数
		ConsumeItems consume = ConsumeItems.valueOf();
		int costNum = 0;
		switch (resourceType) {
		case OIL:
		case OIL_UNSAFE:
			costNum = buildConfig.getOilCost();
			break;
		case GOLDORE:
		case GOLDORE_UNSAFE:
			costNum = buildConfig.getGoldoreCost();
			break;
		case STEEL:
		case STEEL_UNSAFE:
			costNum = buildConfig.getSteelCost();
			break;
		case TOMBARTHITE:
		case TOMBARTHITE_UNSAFE:
			costNum = buildConfig.getTombarthiteCost();
			break;
		default:
			break;
		}

		String expr = ConstProperty.getInstance().getWishingCostValueExpr();
		int costResType = ConstProperty.getInstance().getWishingCostResType();
		PlayerAttr playerAttr = PlayerAttr.valueOf(costResType);
		if (playerAttr == null) {
			return Status.Error.PLAYER_ATTRIBUTE_NOT_EXIST_VALUE;
		}
		
		int num = HawkMathExpress.calculate(expr, costNum, wishCount).intValue();
		consume.addConsumeInfo(playerAttr, num);

		if (!consume.checkConsume(player)) {
			return Status.Error.WISHING_RESOURCE_NOT_ENOUGH_VALUE;
		}

		// 扣除资源
		consume.consumeAndPush(player, Action.WISHING);
		// 大于0时返回的是错误码，小于0时才代表钻石数
		return 0 - num;
	}

	/**
	 * 增加额外许愿次数
	 * @param player
	 * @param itemCount
	 */
	public void addWishingCount(Player player, int itemCount) {
		WishingWellEntity wishingEntity = player.getData().getWishingEntity();
		wishingEntity.setExtraWishCount(wishingEntity.getExtraWishCount() + itemCount);
		if (logger.isDebugEnabled()) {
			logger.debug("add wishing count. playerId={} addCount={}", player.getId(), itemCount);
		}
		syncWishingInfo(player);
	}

}
