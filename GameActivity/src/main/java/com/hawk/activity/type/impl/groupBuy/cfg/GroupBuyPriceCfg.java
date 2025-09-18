package com.hawk.activity.type.impl.groupBuy.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.XmlResource(file = "activity/group_buy/group_buying_price.xml")
public class GroupBuyPriceCfg extends HawkConfigBase {
	@Id
	private final int id;
	//购买次数范围
	private final String buyTimes;
	//礼包Id
	private final int goodsId;
	//改档位花费
	private final String price;

	private List<RewardItem.Builder> priceList;
	
	private static Map<Integer, GroupBuyPriceCfg> topDiscountCfgMap = new HashMap<>(); 
	
	public GroupBuyPriceCfg(){
		this.id = 0;
		this.buyTimes = "";
		this.goodsId = 0;
		this.price = "";
	}
	
	
	@Override
	protected boolean assemble() {
		try {
			priceList = RewardHelper.toRewardItemImmutableList(price);
			GroupBuyPriceCfg cfg = topDiscountCfgMap.get(goodsId);
			if (cfg == null || priceList.get(0).getItemCount() < cfg.getPriceList().get(0).getItemCount()) {
				topDiscountCfgMap.put(goodsId, this);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	public int getId() {
		return id;
	}

	public String getBuyTimes() {
		return buyTimes;
	}

	public int getGoodsId() {
		return goodsId;
	}

	public String getPrice() {
		return price;
	}

	public List<RewardItem.Builder> getPriceList() {
		return priceList;
	}
	
	public static GroupBuyPriceCfg getTopDiscountCfg(int giftId) {
		return topDiscountCfgMap.get(giftId);
	}
}
