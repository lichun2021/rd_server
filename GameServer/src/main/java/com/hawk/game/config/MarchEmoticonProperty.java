package com.hawk.game.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.recharge.RechargeType;

/**
 * 行军表情常量配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "xml/march_emoji_const.xml")
public class MarchEmoticonProperty extends HawkConfigBase {
	/**
	 * 使用表情消耗道具
	 */
	protected final String itemCost;
	/**
	 * 使用表情消耗货币
	 */
	protected final String itemPrice;
	/**
	 * 表情包1关联购买项
	 */
	protected final String emoticon1Price;
	/**
	 * 表情包1购买赠送
	 */
	protected final String emoticon1Gift;
	/**
	 * 行军表情持续时长
	 */
	protected final int continueTime;
	/**
	 * 集结时不同玩家使用表情间隔(s)
	 */
	protected final int timeInterval;

	/**
	 * 实例
	 */
	private static MarchEmoticonProperty instance = null;
	
	private ItemInfo itemInfo;
	
	private ItemInfo priceInfo;
	
	/**
	 * payGiftId与表情包的关联
	 */
	private Map<String, Integer> payGiftEmoticonMap = new HashMap<String, Integer>();
	/**
	 * 表情包购买赠送
	 */
	private Map<String, String> payRewardMap = new HashMap<String, String>();

	/**
	 * 构造
	 */
	public MarchEmoticonProperty() {
		instance = this;
		itemCost = "";
		itemPrice = "";
		emoticon1Price = "";
		emoticon1Gift = "";
		continueTime = 30;
		timeInterval = 0;
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static MarchEmoticonProperty getInstance() {
		return instance;
	}
	
	public String getItemCost() {
		return itemCost;
	}

	public String getItemPrice() {
		return itemPrice;
	}

	public String getEmoticon1Price() {
		return emoticon1Price;
	}

	public String getEmoticon1Gift() {
		return emoticon1Gift;
	}
	
	public long getEmoticonPeriod() {
		return continueTime * 1000L;
	}
	
	public long getTimeInterval() {
		return timeInterval * 1000L;
	}

	@Override
	protected boolean assemble() {
		itemInfo = ItemInfo.valueOf(itemCost);
		priceInfo = ItemInfo.valueOf(itemPrice);
		
		String[] emoticonPriceStr = emoticon1Price.split(",");
		String[] rewardStr = emoticon1Gift.split(",");
		if (rewardStr.length < emoticonPriceStr.length) {
			return false;
		}
		
		for (int i = 0; i < emoticonPriceStr.length; i++) {
			String str = emoticonPriceStr[i];
			String[] bagStrs = str.split("_");
			if (bagStrs.length < 3) {
				return false;
			}
			
			int bag = Integer.valueOf(bagStrs[2]);
			payGiftEmoticonMap.put(bagStrs[0], bag);
			payGiftEmoticonMap.put(bagStrs[1], bag);
			if (ItemInfo.valueOf(rewardStr[i]) != null) {
				payRewardMap.put(bagStrs[0], rewardStr[i]);
				payRewardMap.put(bagStrs[1], rewardStr[i]);
			}
		}
		
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		for (String key : payGiftEmoticonMap.keySet()) {
			PayGiftCfg payGiftCfg = HawkConfigManager.getInstance().getConfigByKey(PayGiftCfg.class, key);
			if (payGiftCfg == null || payGiftCfg.getGiftType() != RechargeType.MARCH_EMOTICON) {
				return false;
			}
		}
		
		return true;
	}
	
	public int getEmoticonBagByPayGift(String payGiftId) {
		return payGiftEmoticonMap.getOrDefault(payGiftId, 0);
	}
	
	public List<ItemInfo> getPayReward(String payGiftId) {
		return ItemInfo.valueListOf(payRewardMap.getOrDefault(payGiftId, ""));
	}

	public ItemInfo getItemInfo() {
		return itemInfo.clone();
	}

	public ItemInfo getPriceInfo() {
		return priceInfo.clone();
	}

}