package com.hawk.activity.type.impl.invest.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 投资理财产品信息配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/invest/invest_product.xml")
public class InvestProductCfg extends HawkConfigBase {
	// 产品ID
	@Id
	private final int id;
	// 投资收益基础比率（万分比）
	private final int productProfit;
	// 投资收益返还期限（秒）
	private final int productDuration;
	// 对应的投资顾问ID
	private final int customerId;
	// 最低投资额度
	private final String productMinAmount;
	// 最高投资额度
	private final String productMaxAmount;
   
	private RewardItem.Builder investConsumeItem;
	
	private int investMaxAmount;

	public InvestProductCfg() {
		id = 0;
		productProfit = 0;
		productDuration = 0;
		customerId = 0;
		productMinAmount = "";
		productMaxAmount = "";
	}
	
	public int getId() {
		return id;
	}
	
	
    public int getProductProfit() {
		return productProfit;
	}

	public long getProductDuration() {
		return productDuration * 1000L;
	}

	public int getCustomerId() {
		return customerId;
	}

	public String getProductMinAmount() {
		return productMinAmount;
	}

	public String getProductMaxAmount() {
		return productMaxAmount;
	}

	public boolean assemble() {
		investConsumeItem = RewardHelper.toRewardItem(productMinAmount);
 		if (investConsumeItem == null || investConsumeItem.getItemCount() < 0) {
 			return false;
 		}
 		
 		RewardItem.Builder itemPrice = RewardHelper.toRewardItem(productMaxAmount);
 		if (itemPrice == null || itemPrice.getItemCount() <= 0) {
 			return false;
 		}
 		
 		investMaxAmount = (int) itemPrice.getItemCount();
 		
		return true;
	}
	
	@Override
	public boolean checkValid() {
		
		return true;		
	}

	public int getInvestMaxAmount() {
		return investMaxAmount;
	}
	
	public RewardItem.Builder getInvestConsumeItem() {
		RewardItem.Builder builder = RewardItem.newBuilder();
		builder.setItemType(investConsumeItem.getItemType());
		builder.setItemId(investConsumeItem.getItemId());
		builder.setItemCount(investConsumeItem.getItemCount());
		return builder;
	}

	public int getCustomerProfit() {
		InvestCustomerCfg customerCfg = HawkConfigManager.getInstance().getConfigByKey(InvestCustomerCfg.class, getCustomerId());
		if (customerCfg != null) {
			return customerCfg.getCustomerProfit();
		}
		
		return 0;
	}
	
	public RewardItem.Builder getCustomerPriceItem() {
		InvestCustomerCfg customerCfg = HawkConfigManager.getInstance().getConfigByKey(InvestCustomerCfg.class, getCustomerId());
		if (customerCfg != null) {
			return customerCfg.getCustomerPriceItem();
		}
		
		return RewardItem.newBuilder();
	}
	
	public int getComsumerAmount() {
		InvestCustomerCfg customerCfg = HawkConfigManager.getInstance().getConfigByKey(InvestCustomerCfg.class, getCustomerId());
		if (customerCfg != null) {
			return customerCfg.getComsumerAmount();
		}
		
		return 0;
	}
	
}
