package com.hawk.activity.type.impl.bestprize.cfg;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.gamelib.activity.ConfigChecker;
	
/**
 * 直购商店配置
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/the_best_prize/the_best_prize_shop.xml")
public class BestPrizeShopCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 0.免费商店1.人民币直购 2.金条直购
	 */
	private final int shopItemType;
	
	private final int times;
	/**
	 * 不配置（默认为0）.活动期间不刷新  1.每日刷新
	 */
	private final int refreshType;
	
	private final String payItem;
	private final String getItem;
	
	private final int payQuota;
	private final String androidPayID;
	private final String iosPayId;
	
	private static String freeAward;
	private static Map<String, BestPrizeShopCfg> buyItemGoodsMap = new HashMap<>();
	
	/**
	 * 构造
	 */
	public BestPrizeShopCfg() {
		id = 0;
		shopItemType = 0;
		times = 0;
		refreshType = 0;
		payItem = "";
		getItem = "";
		payQuota = 0;
		androidPayID = "";
		iosPayId = "";
	}

	public int getId() {
		return id;
	}

	public int getShopItemType() {
		return shopItemType;
	}

	public int getTimes() {
		return times;
	}

	public int getRefreshType() {
		return refreshType;
	}

	public String getPayItem() {
		return payItem;
	}

	public String getGetItem() {
		return getItem;
	}

	public int getPayQuota() {
		return payQuota;
	}

	public String getAndroidPayID() {
		return androidPayID;
	}

	public String getIosPayId() {
		return iosPayId;
	}

	public boolean assemble() {
		if (shopItemType == 0) {
			freeAward = getItem;
		}
		buyItemGoodsMap.put(androidPayID, this);
		buyItemGoodsMap.put(iosPayId, this);
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(getItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("BestPrizeShopCfg reward error, id: %s , reward: %s", id, getItem));
		}
		return super.checkValid();
	}
	
	public static BestPrizeShopCfg getConfig(String goodsId) {
		return buyItemGoodsMap.get(goodsId);
	}
	
	public static String getFreeAward() {
		return freeAward;
	}
}
