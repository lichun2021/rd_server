package com.hawk.game.gacha;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.GachaCfg;
import com.hawk.game.config.GachaPoolCfg;
import com.hawk.game.entity.PlayerGachaEntity;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.GachaType;
import com.hawk.game.protocol.HP;
import com.hawk.game.util.RandomUtil;
import com.hawk.log.Action;
import com.hawk.sdk.msdk.entity.PayItemInfo;

/**
 * 
 * @author luwentao
 *
 */
public interface GachaOprator {
	public static final int DEFAULT_BATCH = 10;
	static Logger logger = LoggerFactory.getLogger("Server");
	final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::new);

	default CheckAndConsumResult checkAndConsum(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, Player player) {
		ConsumeItems consume = ConsumeItems.valueOf();
		ItemInfo ticket = ItemInfo.valueOf(gachaCfg.getTicketExpend());
		ticket.setCount(getGachaCount());
		int ticketCount = player.getData().getItemNumByItemId(ticket.getItemId());
		int notHave = getGachaCount() - ticketCount;

		List<ItemInfo> cost = new ArrayList<>();
		if (notHave > 0) {
			if (ticketCount > 0) {
				ticket.setCount(ticketCount);
				consume.addConsumeInfo(ticket, false);
				cost.add(ticket);
			}
			ItemInfo priceItem = ItemInfo.valueOf(gachaCfg.getTicketPrice());
			priceItem.setCount(priceItem.getCount() * (getGachaCount() - ticketCount));
			consume.addConsumeInfo(priceItem, true);
			cost.add(priceItem);

		} else {
			consume.addConsumeInfo(ticket, false);
			cost.add(ticket);
		}
		if (!consume.checkConsume(player, HP.code.GACHA_C_VALUE)) {
			return CheckAndConsumResult.create(false);
		}
		
		if (consume.getBuilder().hasAttrInfo() && consume.getBuilder().getAttrInfo().getDiamond() > 0) {
			ItemInfo priceItem = ItemInfo.valueOf(gachaCfg.getTicketPrice());
			consume.addPayItemInfo(new PayItemInfo(String.valueOf(ticket.getItemId()), (int)priceItem.getCount(), getGachaCount() - ticketCount));
		}
		consume.consumeAndPush(player, Action.GACHA);
		return CheckAndConsumResult.create(true, cost);
	}

	List<String> gacha(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, Player player);

	public static GachaOprator of(GachaType gachaType) {
		switch (gachaType) {
		case ADVANCE_ONE:
		case NORMAL_ONE:
		case SKILL_ONE:{
			return new GachaOnceImpl();
		}
		
		case ADVANCE_TEN:
		case NORMAL_TEN:
		case SKILL_TEN:{
			return new GachaTenImpl();
		}

		//铠甲一次
		case ARMOUR_ONE:{
			return new GachaArmourOnceImpl();
		}
		
		//铠甲十次
		case ARMOUR_TEN:{
			return new GachaArmourTenImpl();
		}
		
		//铠甲宝箱
		case ARMOUR_BOX: {
			return new GachaArmourBoxImpl();
		}
		
		//机甲核心模块一次
		case MODULE_ONE:{
			return new GachaModuleOnceImpl();
		}
		
		//机甲核心模块十次
		case MODULE_TEN:{
			return new GachaModuleTenImpl();
		}

		default: {
			break;
		}
		}

		return null;
	}

	/**
	 * 取得对应类型抽将次数
	 * 
	 * @param gachaType
	 * @return
	 */
	int getGachaCount();
	void setGachaCount(int num);

	default String gacha(final int pool, final int homeLeavl) {
		ConfigIterator<GachaPoolCfg> poolList = HawkConfigManager.getInstance().getConfigIterator(GachaPoolCfg.class);
		List<GachaPoolCfg> gachaPoolList = poolList.stream()
				.filter(o -> o.getId() == pool)
				.filter(o -> homeLeavl >= o.getLvMin() && homeLeavl <= o.getLvMax())
				.collect(Collectors.toList());

		if (gachaPoolList.isEmpty()) { // 策划配置错了.但是又想流程能走下去
			gachaPoolList = poolList.stream().filter(o -> o.getId() == pool).collect(Collectors.toList());
			logger.error("gacha config error, pool: {}, homeLeavl: {}", pool, homeLeavl);
		}
		GachaPoolCfg rewardCfg = RandomUtil.random(gachaPoolList);
		final int count = rewardCfg.getNumMin() + RANDOM.get().nextInt(rewardCfg.getNumMax() - rewardCfg.getNumMin() + 1);
		return rewardCfg.getDropType() + "_" + count;
	}

	default boolean isGachArmour() {
		return false;
	}
	
	default boolean isGachaModule() {
		return false;
	}
	
	// default boolean checkAndConsumTicketExpend(GachaCfg gachaCfg,
	// PlayerGachaEntity gachaEntity, Player player) {
	// Optional<ItemInfo> itemOP = gachaCfg.getTicketExpendItemInfo();
	// if (!itemOP.isPresent()) {
	// return false;
	// }
	//
	// ItemInfo itemInfo = itemOP.get();
	// ConsumeItems consume = ConsumeItems.valueOf();
	// consume.addItemConsume( itemInfo.getItemId(), itemInfo.getCount());
	// if (!consume.checkConsume(player)) {
	// return false;
	// }
	// consume.consumeAndPush(player, Action.GACHA);
	// return true;
	// }
	//
	// default boolean checkAndConsumDiamondExpend(GachaCfg gachaCfg,
	// PlayerGachaEntity gachaEntity, Player player) {
	// Optional<ItemInfo> itemOP = gachaCfg.getDiamondExpendItemInfo();
	// if (!itemOP.isPresent()) {
	// return false;
	// }
	//
	// ConsumeItems consume = ConsumeItems.valueOf();
	// consume.addConsumeInfo(itemOP.get(), true);
	// if (!consume.checkConsume(player,HP.code.GACHA_C_VALUE)) {
	// return false;
	// }
	// consume.consumeAndPush(player, Action.GACHA);
	// return true;
	// }
}
