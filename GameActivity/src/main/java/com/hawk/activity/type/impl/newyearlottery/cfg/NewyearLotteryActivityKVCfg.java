package com.hawk.activity.type.impl.newyearlottery.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.KVResource(file = "activity/new_year_lottery/new_year_lottery_cfg.xml")
public class NewyearLotteryActivityKVCfg extends HawkConfigBase {

	private final int serverDelay;
	
	// 头彩保底抽奖数
	private final String slot_guarantee_num;
	
	// 关联的直购礼包ID
	private final String androidPayId;
	private final String iosPayId;
	
	private final String lotteryCost;

	private int[] slotGuaranteeNum = new int[2];
	private Map<Integer, Integer> lotteryConstItemIdMap = new HashMap<>();
	private Map<Integer, String> lotteryConstMap = new HashMap<>();
	
	private Map<String, Integer> androidPayGiftIdMap = new HashMap<>();
	private Map<String, Integer> iosPayGiftIdMap = new HashMap<>();
	
	public NewyearLotteryActivityKVCfg(){
		serverDelay = 0;
		slot_guarantee_num = "";
		androidPayId = "";
		iosPayId = "";
		lotteryCost = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}
	
	public String getSlot_guarantee_num() {
		return slot_guarantee_num;
	}

	@Override
	protected boolean assemble() {
		if (HawkOSOperator.isEmptyString(androidPayId) || HawkOSOperator.isEmptyString(iosPayId)) {
			return false;
		}
		
		String[] split = androidPayId.split(",");
		for (String str : split) {
			String[] arr = str.split("_");
			int type = Integer.parseInt(arr[0]);
			if (HawkOSOperator.isEmptyString(arr[1])) {
				return false;
			}
			androidPayGiftIdMap.put(arr[1], type);
		}
		
		String[] split2 = iosPayId.split(",");
		for (String str : split2) {
			String[] arr = str.split("_");
			int type = Integer.parseInt(arr[0]);
			if (HawkOSOperator.isEmptyString(arr[1])) {
				return false;
			}
			iosPayGiftIdMap.put(arr[1], type);
		}
		
		String[] gnum = slot_guarantee_num.split("_");
		slotGuaranteeNum[0] = Integer.parseInt(gnum[0]);
		slotGuaranteeNum[1] = Integer.parseInt(gnum[1]);
		if (slotGuaranteeNum[0] > slotGuaranteeNum[1]) {
			return false;
		}
		
		String[] lotteryCostSplit = lotteryCost.split(";");
		for (String str : lotteryCostSplit) {
			String[] substrSplit = str.split(",");
			int lotteryType = Integer.parseInt(substrSplit[0]);
			String cost = substrSplit[1];
			int itemId = RewardHelper.toRewardItemImmutableList(cost).get(0).getItemId();
			lotteryConstItemIdMap.put(lotteryType, itemId);
			lotteryConstMap.put(lotteryType, cost);
		}
		
		return super.assemble();
	}
	
	public int getLotteryTypeByGiftId(String payGiftId) {
		if (androidPayGiftIdMap.containsKey(payGiftId)) {
			return androidPayGiftIdMap.get(payGiftId);
		}
		
		if (iosPayGiftIdMap.containsKey(payGiftId)) {
			return iosPayGiftIdMap.get(payGiftId);
		}
		
		return 0;
	}
	
	public int randomSlotGuaranteeNum() {
		return HawkRand.randInt(slotGuaranteeNum[0], slotGuaranteeNum[1]);
	}
	
	public List<RewardItem.Builder> getLotteryCost(int lotteryType) {
		return RewardHelper.toRewardItemImmutableList(lotteryConstMap.get(lotteryType));
	}
	
	public int getCostItemId(int lotteryType) {
		return lotteryConstItemIdMap.get(lotteryType);
	}
	
}
 