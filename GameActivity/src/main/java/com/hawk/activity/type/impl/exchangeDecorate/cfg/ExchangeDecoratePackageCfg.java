package com.hawk.activity.type.impl.exchangeDecorate.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
@HawkConfigManager.XmlResource(file = "activity/freeavatar/freeavatar_package.xml")
public class ExchangeDecoratePackageCfg extends HawkConfigBase{
	@Id
	private final int id;
	// 道具id
	private final String item;
	private final int needLevel;
	// 价格
	private final String price;
	// 获取道具
	private final float discount;
	//次数
	private final int num;
	private final int priority;

	private List<RewardItem.Builder> giveItemItemList;
	private List<RewardItem.Builder> priceItemItemList;

	public ExchangeDecoratePackageCfg() {
		this.id = 0;
		this.item="";
		this.needLevel=0;
		this.price="";
		this.discount=0f;
		this.num=0;
		this.priority=0;
	}
	
	public int getId() {
		return id;
	}

	public List<RewardItem.Builder> getPriceItemItemList() {
		return priceItemItemList;
	}

	public void setPriceItemItemList(List<RewardItem.Builder> priceItemItemList) {
		this.priceItemItemList = priceItemItemList;
	}

	public List<RewardItem.Builder> getGiveItemItemList() {
		return giveItemItemList;
	}

	public void setGiveItemItemList(List<RewardItem.Builder> giveItemItemList) {
		this.giveItemItemList = giveItemItemList;
	}

	public String getItem() {
		return item;
	}

	public int getNeedLevel() {
		return needLevel;
	}

	public String getPrice() {
		return price;
	}

	public float getDiscount() {
		return discount;
	}

	public int getNum() {
		return num;
	}

	public int getPriority() {
		return priority;
	}

	@Override
	public boolean equals(Object obj) {
		ExchangeDecoratePackageCfg cfg = (ExchangeDecoratePackageCfg) obj;
		if(cfg.getId() == getId())
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public boolean assemble() {
		priceItemItemList = RewardHelper.toRewardItemImmutableList(price);
		giveItemItemList  = RewardHelper.toRewardItemImmutableList(item);
		return true;
	}
	
	@Override
	public boolean checkValid() {
		return 
			ConfigChecker.getDefaultChecker().checkAwardsValid(price) &&
			ConfigChecker.getDefaultChecker().checkAwardsValid(item);
	}
	
	
}
