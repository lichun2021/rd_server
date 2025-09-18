package com.hawk.activity.type.impl.monthcard.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 特权商店配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/monthCard_shop.xml")
public class MonthCardShopCfg extends HawkConfigBase {
	@Id 
	private final int ShopItemId;
	/**
	 * 限购次数
	 */
	private final int ShopQuota;
	/**
	 * 限购类型 1.是周刷新按照自然周刷新  2.是终身限购
	 */
	private final int ShopQuotaType;
	
	private final String PayItem;
	
	private final String GetItem;
	
	private static int payItemId;

	public MonthCardShopCfg() {
		ShopItemId = 0;
		ShopQuota = 0;
		ShopQuotaType = 0;
		PayItem = "";
		GetItem = "";
	}

	public int getShopItemId() {
		return ShopItemId;
	}

	public int getShopQuota() {
		return ShopQuota;
	}

	public int getShopQuotaType() {
		return ShopQuotaType;
	}
	
	public boolean isWeekLimit() {
		return ShopQuotaType == 1;
	}
	
	public boolean isLimitType() {
		return ShopQuotaType > 0;
	}

	public String getPayItem() {
		return PayItem;
	}

	public String getGetItem() {
		return GetItem;
	}
	
	public boolean assemble() {
		if (payItemId == 0) {
			RewardItem.Builder item = RewardHelper.toRewardItem(PayItem);
			if (item != null) {
				payItemId = item.getItemId();
			}
		}
		return true;
	}
	
	public static int getPayItemId() {
		return payItemId;
	}
	
}
