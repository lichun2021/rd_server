package com.hawk.activity.type.impl.machineSell.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.KVResource(file = "activity/machine_sell/machine_sell_activity_cfg.xml")
public class MachineSellKVCfg extends HawkConfigBase {

	/** 是否跨天重置，1重置0不重置 **/
	//private final int isReset;
	
	/** 延迟开启时间*/
	private final long serverDelay;
	
	/**使用道具*/
	private final String itemOnce;
	/**道具价格*/
	private final String itemOnecePrice;
	/**单次抽奖固定所得*/
	private final String extReward;
	
	/**五次消耗道具*/
	private final String item5Times;
	/**五次价格*/
	private final String item5TimesPrice;
	/**五次固定所得*/
	private final String ext5TimesReward;
	
	/**免费抽次数*/
	private final int free;
	
	/**是否每日重置免费抽卡次数*/
	private final int reset;
	
	/**五连抽抽几次*/
	private final int fiveTimes;
	
	//抽一次花费道具
	private List<RewardItem.Builder> itemOnceItems;
	
	//抽一次价格
	private List<RewardItem.Builder> itemOncePriceItems;
	
	//抽一次固定奖励
	private List<RewardItem.Builder> extRewardItems;
	
	//抽一次花费道具
	private List<RewardItem.Builder> item5TimesItems;
	
	//抽一次价格
	private List<RewardItem.Builder> item5TimesPriceItems;
	
	//抽一次固定奖励
	private List<RewardItem.Builder> ext5TimesRewardItems;
	

	public MachineSellKVCfg(){
		//isReset = 0;
		serverDelay = 0;
		itemOnce = "30000_9990004_1";
		item5Times = "30000_9990004_5";
		itemOnecePrice = "10000_1000_56";
		item5TimesPrice = "10000_1000_280";
		extReward = "30000_840172_1";
		ext5TimesReward = "30000_840172_5";
		free = 1;
		reset = 1;
		fiveTimes = 5;
	}

	public boolean isReset(){
		return reset == 1;
	}
	
	public long getServerDelay(){
		return serverDelay * 1000L;
	}

	@Override
	protected boolean assemble() {
		this.itemOnceItems = RewardHelper.toRewardItemImmutableList(this.itemOnce);
		this.itemOncePriceItems = RewardHelper.toRewardItemImmutableList(this.itemOnecePrice);
		this.extRewardItems = RewardHelper.toRewardItemImmutableList(this.extReward);
		this.item5TimesPriceItems = RewardHelper.toRewardItemImmutableList(this.item5TimesPrice);
		this.item5TimesItems = RewardHelper.toRewardItemImmutableList(this.item5Times);
		this.ext5TimesRewardItems = RewardHelper.toRewardItemImmutableList(this.ext5TimesReward);
		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		if(reset != 0 && reset != 1){
			throw new RuntimeException(String.format("machine_sell_activity_cfg.xml 配置isReset出错:%d", reset));
		}
		return super.checkValid();
	}


	public String getItemOnecePrice() {
		return itemOnecePrice;
	}

	public List<RewardItem.Builder> getItemOnceItems() {
		return itemOnceItems;
	}


	public int getFree() {
		return free;
	}

	public String getExtReward() {
		return extReward;
	}

	public int getReset() {
		return reset;
	}

	public List<RewardItem.Builder> getItemOncePriceItems() {
		return itemOncePriceItems;
	}

	public String getItem5Times() {
		return item5Times;
	}

	public String getItem5TimesPrice() {
		return item5TimesPrice;
	}

	public String getExt5TimesReward() {
		return ext5TimesReward;
	}

	public List<RewardItem.Builder> getExtRewardItems() {
		return extRewardItems;
	}

	public void setExtRewardItems(List<RewardItem.Builder> extRewardItems) {
		this.extRewardItems = extRewardItems;
	}

	public List<RewardItem.Builder> getItem5TimesItems() {
		return item5TimesItems;
	}

	public void setItem5TimesItems(List<RewardItem.Builder> item5TimesItems) {
		this.item5TimesItems = item5TimesItems;
	}

	public List<RewardItem.Builder> getItem5TimesPriceItems() {
		return item5TimesPriceItems;
	}

	public void setItem5TimesriceItems(List<RewardItem.Builder> item5TimesPriceItems) {
		this.item5TimesPriceItems = item5TimesPriceItems;
	}

	public List<RewardItem.Builder> getExt5TimesRewardItems() {
		return ext5TimesRewardItems;
	}

	public void setExt5TimesRewardItems(List<RewardItem.Builder> ext5TimesRewardItems) {
		this.ext5TimesRewardItems = ext5TimesRewardItems;
	}

	public int getFiveTimes() {
		return fiveTimes;
	}


}
