package com.hawk.activity.type.impl.alliesWishing.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableMap;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 装扮投放系列活动四:硝烟再起
 * @author hf
 */
@HawkConfigManager.KVResource(file = "activity/alliance_wish/alliance_wish_cfg.xml")
public class  AllianceWishKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间；单位:秒
	 */
	private final int serverDelay;
	
	private final int numCount;
	
	private final int wishGuildMemberCount;
	private final int wishGuildSendCD;
	
	
	private final int iosPay;
	
	private final int androidPay;

	private final String resetCost;
	
	private final int wishSignPosPoolId;
	
	private final int wishCommPosPoolId;
	
	private final int wishLuxuryPosPoolId;
	
	private final int  giftWishCount;
	
	private final int  giftWishLuxuryCount;
	
	private final int giftMulitParam;
	
	private final String achiveItem;
	
	private final String  supplySignCost;
	
	private final String  signMust;
	
	private Map<Integer,Integer> signMustMap = new HashMap<>();
	
	
	public AllianceWishKVCfg(){
		serverDelay =0;
		numCount= 0;
		iosPay=0;
		androidPay =0;
		resetCost = "";
		wishGuildMemberCount = 0;
		wishGuildSendCD = 0;
		wishSignPosPoolId = 0;
		wishCommPosPoolId = 0;
		wishLuxuryPosPoolId = 0;
		giftWishCount = 0;
		giftWishLuxuryCount =0;
		giftMulitParam = 1;
		achiveItem = "";
		supplySignCost = "";
		signMust = "";
		
	}
	
	
	@Override
	protected boolean assemble() {
		Map<Integer,Integer> signMustMapTemp = new HashMap<>();
		if(!HawkOSOperator.isEmptyString(this.signMust)){
			String[] arr = this.signMust.split(",");
			for(String posStr : arr){
				String[] posArr = posStr.split("_");
				signMustMapTemp.put(Integer.parseInt(posArr[0]), Integer.parseInt(posArr[1]));
			}
		}
		this.signMustMap = ImmutableMap.copyOf(signMustMapTemp);
		return super.assemble();
	}
	
	
	
	@Override
	protected boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(resetCost);
		if (!valid) {
			return false;
		}
		return super.checkValid();
	}


	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	
	
	public int getAndroidPay() {
		return androidPay;
	}
	
	
	public int getIosPay() {
		return iosPay;
	}

	
	public int getWishGuildMemberCount() {
		return wishGuildMemberCount;
	}
	
	
	public int getwishGuildSendCD() {
		return wishGuildSendCD;
	}

	public List<RewardItem.Builder> getResetCostItemList() {
		return RewardHelper.toRewardItemImmutableList(this.resetCost);
	}

	
	public int getWishSignPosPoolId() {
		return wishSignPosPoolId;
	}
	
	
	
	public int getWishCommPosPoolId() {
		return wishCommPosPoolId;
	}
	
	
	
	
	public int getWishLuxuryPosPoolId() {
		return wishLuxuryPosPoolId;
	}
	
	public int getGiftWishCount() {
		return giftWishCount;
	}
	
	public int getGiftWishLuxuryCount() {
		return giftWishLuxuryCount;
	}
	
	public int getGiftMulitParam() {
		return giftMulitParam;
	}
	
	
	
	public List<RewardItem.Builder> getAchiveItemList() {
		return RewardHelper.toRewardItemImmutableList(this.achiveItem);
	}
	
	
	
	public List<RewardItem.Builder> getsupplySignCostItemList() {
		return RewardHelper.toRewardItemImmutableList(this.supplySignCost);
	}

	
	public int getNumCount() {
		return numCount;
	}
	
	public int getSignMust(int count){
		return this.signMustMap.getOrDefault(count, 0);
	}
}