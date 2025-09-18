package com.hawk.activity.type.impl.spread.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.KVResource(file = "activity/spread/spread_cfg.xml")
public class SpreadKVCfg extends HawkConfigBase {

	private final long serverDelay;
	/** 是否跨天重置，1重置0不重置 **/
	private final int isReset;

	/** 回答正确奖励  **/
	private final String gift;

	private final int packageIsReset;

	private final int baseRequest;

	private final long logRequest;
	private final String startDate;
	private final int endDate;
	// #新人绑定礼包
	private final String newBandAward;// =10000_1007_1000000

	private List<RewardItem.Builder> giftItems;
	private List<RewardItem.Builder> newBandAwardItems;

	private long logRequesetMs = 0;

	public SpreadKVCfg() {
		baseRequest = 12;
		logRequest = 259200;
		packageIsReset = 0;
		isReset = 0;
		gift = "";
		serverDelay = 0;
		startDate = "";
		newBandAward = "";
		endDate = 30;
	}

	public long getStartDateTime() {
		return HawkTime.parseTime(startDate);
	}

	public int getIsReset() {
		return isReset;
	}

	public boolean isReset() {
		return isReset == 1;
	}

	public String getRightReward() {
		return gift;
	}

	public List<RewardItem.Builder> getGiftItems() {
		return giftItems;
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	@Override
	protected boolean assemble() {
		logRequesetMs = logRequest * 1000;
		giftItems = RewardHelper.toRewardItemImmutableList(gift);
		newBandAwardItems = giftItems = RewardHelper.toRewardItemImmutableList(newBandAward);
		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		if (isReset != 0 && isReset != 1) {
			throw new RuntimeException(String.format("spread_cfg.xml 配置isReset出错:%d", isReset));
		}
		return super.checkValid();
	}

	public int getPackageIsReset() {
		return packageIsReset;
	}

	public int getBaseRequest() {
		return baseRequest;
	}

	public long getLogRequestMs() {
		return logRequesetMs;
	}

	public List<RewardItem.Builder> getNewBandAwardItems() {
		return newBandAwardItems;
	}

	public long getLogRequesetMs() {
		return logRequesetMs;
	}

	public void setLogRequesetMs(long logRequesetMs) {
		this.logRequesetMs = logRequesetMs;
	}

	public String getGift() {
		return gift;
	}

	public long getLogRequest() {
		return logRequest;
	}

	public String getStartDate() {
		return startDate;
	}

	public int getEndDate() {
		return endDate;
	}

	public String getNewBandAward() {
		return newBandAward;
	}

	public void setGiftItems(List<RewardItem.Builder> giftItems) {
		this.giftItems = giftItems;
	}

	public void setNewBandAwardItems(List<RewardItem.Builder> newBandAwardItems) {
		this.newBandAwardItems = newBandAwardItems;
	}

}
