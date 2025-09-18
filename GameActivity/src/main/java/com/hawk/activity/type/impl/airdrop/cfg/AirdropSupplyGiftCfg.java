package com.hawk.activity.type.impl.airdrop.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;


@HawkConfigManager.XmlResource(file = "activity/airdrop_supply/airdrop_supply_gift.xml")
public class AirdropSupplyGiftCfg extends HawkConfigBase {
	// 礼包ID
	@Id 
	private final int id;
	
	private final String androidPayId;
	
	private final String iosPayId;
	
	private final String buyAward;
	
	private final String dailyScore;
	
	private List<RewardItem.Builder> buyAwardList;
	private List<Integer> dailyScoreList;
	private static Map<String, Integer> payGiftIdMap = new HashMap<String, Integer>();
	
	public AirdropSupplyGiftCfg() {
		id = 0;
		androidPayId = "";
		iosPayId = "";
		buyAward = "";
		dailyScore = "";
	}
	
	@Override
	protected boolean assemble() {
		payGiftIdMap.put(androidPayId, id);
		payGiftIdMap.put(iosPayId, id);
		dailyScoreList = SerializeHelper.stringToList(Integer.class, dailyScore, SerializeHelper.ATTRIBUTE_SPLIT);
		buyAwardList = RewardHelper.toRewardItemImmutableList(buyAward);
		return true;
	}


	public List<RewardItem.Builder> getBuyAwardList() {
		return buyAwardList;
	}

	public static Map<String, Integer> getPayGiftIdMap() {
		return payGiftIdMap;
	}


	public int getId() {
		return id;
	}

	public String getBuyAward() {
		return buyAward;
	}

	public String getDailyScore() {
		return dailyScore;
	}

	public String getAndroidPayId() {
		return androidPayId;
	}

	public String getIosPayId() {
		return iosPayId;
	}
	

	public static int getGiftId(String payGiftId) {
		if (!payGiftIdMap.containsKey(payGiftId)) {
			throw new RuntimeException("payGiftId not match customGiftId");
		}
		return payGiftIdMap.get(payGiftId);
	}

	public List<Integer> getDailyScoreList() {
		return dailyScoreList;
	}
}
