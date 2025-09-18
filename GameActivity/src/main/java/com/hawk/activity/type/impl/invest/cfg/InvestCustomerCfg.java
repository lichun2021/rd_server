package com.hawk.activity.type.impl.invest.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 投资理财产品对应的理财顾问信息
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/invest/invest_customer.xml")
public class InvestCustomerCfg extends HawkConfigBase {
	@Id
	private final int customerId;
	// 购买加成道具后额外增加的收益比率（万分比）
	private final int customerProfit;
	// 购买加成道具后额外增加的投资额度
	private final String customerAmout;
	// 加成道具的价格
	private final String customerPrice;
	
	private RewardItem.Builder customerAmountItem;
	private RewardItem.Builder customerPriceItem;

	public InvestCustomerCfg() {
		customerId = 0;
		customerProfit = 0;
		customerAmout = "";
		customerPrice = "";
	}
	
	public int getCustomerId() {
		return customerId;
	}

	public int getCustomerProfit() {
		return customerProfit;
	}
	
    public String getCustomerAmout() {
		return customerAmout;
	}

	public String getCustomerPrice() {
		return customerPrice;
	}
	
	public RewardItem.Builder getCustomerAmountItem() {
		RewardItem.Builder builder = RewardItem.newBuilder();
		builder.setItemType(customerAmountItem.getItemType());
		builder.setItemId(customerAmountItem.getItemId());
		builder.setItemCount(customerAmountItem.getItemCount());
		return builder;
	}

	public RewardItem.Builder getCustomerPriceItem() {
		RewardItem.Builder builder = RewardItem.newBuilder();
		builder.setItemType(customerPriceItem.getItemType());
		builder.setItemId(customerPriceItem.getItemId());
		builder.setItemCount(customerPriceItem.getItemCount());
		return customerPriceItem;
	}

	public boolean assemble() {
		customerAmountItem = RewardHelper.toRewardItem(customerAmout);
 		if (customerAmountItem == null || customerAmountItem.getItemCount() <= 0) {
 			return false;
 		}
 		
 		customerPriceItem = RewardHelper.toRewardItem(customerPrice);
 		if (customerPriceItem == null || customerPriceItem.getItemCount() <= 0) {
 			return false;
 		}
 		
		return true;
	}
	
	@Override
	public boolean checkValid() {
		
		return true;		
	}
	
	public int getComsumerAmount() {
		return (int) customerAmountItem.getItemCount();
	}
	
}
