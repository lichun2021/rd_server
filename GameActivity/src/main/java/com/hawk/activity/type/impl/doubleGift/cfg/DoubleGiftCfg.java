package com.hawk.activity.type.impl.doubleGift.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 双享豪礼活动配置
 */
@HawkConfigManager.XmlResource(file = "activity/double_gift/double_gift.xml")
public class DoubleGiftCfg extends HawkConfigBase {
	// 礼包ID
	@Id 
	private final int giftId;
	
	private final String androidPayId;
	
	private final String iosPayId;
	
	private final String fixedReward;
	
	private final int isFree;
	
	private List<RewardItem.Builder> fixedRewardList;
	
	private static Map<String, Integer> payGiftIdMap = new HashMap<String, Integer>();
	
	public DoubleGiftCfg() {
		giftId = 0;
		androidPayId = "";
		iosPayId = "";
		fixedReward = "";
		isFree = 0;
	}
	
	@Override
	protected boolean assemble() {
		payGiftIdMap.put(androidPayId, giftId);
		payGiftIdMap.put(iosPayId, giftId);
		fixedRewardList = RewardHelper.toRewardItemImmutableList(fixedReward);
		return true;
	}

	public List<RewardItem.Builder> getBuyAwardList() {
		return fixedRewardList;
	}

	public static Map<String, Integer> getPayGiftIdMap() {
		return payGiftIdMap;
	}

	
	public int getGiftId() {
		return giftId;
	}

	public String getFixedReward() {
		return fixedReward;
	}

	public String getAndroidPayId() {
		return androidPayId;
	}

	public String getIosPayId() {
		return iosPayId;
	}
	

	public static int getGiftId(String payGiftId) {
		if (!payGiftIdMap.containsKey(payGiftId)) {
			return 0;
		}
		return payGiftIdMap.get(payGiftId);
	}


	public boolean isFree() {
		return isFree == 1;
	}
}
