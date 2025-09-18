package com.hawk.activity.type.impl.armamentexchange.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * @author luke
 */
@HawkConfigManager.XmlResource(file = "activity/arms_upgrade/arms_upgrade_box.xml")
public class ArmamentExchangeBoxCfg extends HawkConfigBase {
	/** 成就id */
	@Id
	private final int id;
	private final int type;
	private final String itemId;
	/**
	 * 消耗
	 */
	private final String price;
	/**
	 * 兑换次数
	 */
	private final int timesLimit;

	private RewardItem.Builder awardItem;
	private RewardItem.Builder priceItem;
	
	public ArmamentExchangeBoxCfg() {
		id = 0;
		type = 0;
		itemId="";
		price="";
		timesLimit=0;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public String getItemId() {
		return itemId;
	}

	public String getPrice() {
		return price;
	}

	public int getTimesLimit() {
		return timesLimit;
	}

	public RewardItem.Builder getAwardItem() {
		return awardItem;
	}

	public void setAwardItem(RewardItem.Builder awardItem) {
		this.awardItem = awardItem;
	}

	public RewardItem.Builder getPriceItem() {
		return priceItem;
	}

	public void setPriceItem(RewardItem.Builder priceItem) {
		this.priceItem = priceItem;
	}

	@Override
	protected boolean assemble() {
		awardItem = RewardHelper.toRewardItem(itemId);
		priceItem = RewardHelper.toRewardItem(price);
		return true;
	}

}

