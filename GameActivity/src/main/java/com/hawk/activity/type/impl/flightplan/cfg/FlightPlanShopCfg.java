package com.hawk.activity.type.impl.flightplan.cfg;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.exchangeTip.AExchangeTipConfig;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 威龙庆典-飞行计划活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/j20_celebration/j20_celebration_shop.xml")
public class FlightPlanShopCfg extends AExchangeTipConfig {
	@Id
	private final int id;
	/** 兑换商品 */
	private final String goods;
	/** 兑换商品消耗 */
	private final String price;
	// 可兑换次数
	private final int total;
	
	private static int consumeItemId;
	
	private int awardItemId;
	private int consumeCount;
	
	public FlightPlanShopCfg() {
		id = 0;
		goods = "";
		price = "";
		total = 0;
	}
	
	public int getId() {
		return id;
	}

	public String getGoods() {
		return goods;
	}

	public String getPrice() {
		return price;
	}

	public int getTotal() {
		return total;
	}
	
	public boolean assemble() {
		RewardItem.Builder builder = RewardHelper.toRewardItem(price);
		consumeCount = (int) builder.getItemCount();
		if (consumeItemId == 0) {
			consumeItemId = builder.getItemId();
		} else if (builder.getItemId() != consumeItemId) {
			HawkLog.errPrintln("consumeItemId should keep consistency");
			return false;
		} 
		
		RewardItem.Builder awardBuilder = RewardHelper.toRewardItem(goods);
		awardItemId = awardBuilder.getItemId();
		
		return true;
	}

	public static int getConsumeItemId() {
		return consumeItemId;
	}

	public int getConsumeCount() {
		return consumeCount;
	}

	public int getAwardItemId() {
		return awardItemId;
	}

}
