package com.hawk.activity.type.impl.exchangeDecorate.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
@HawkConfigManager.XmlResource(file = "activity/freeavatar/freeavatar_exchange.xml")
public class ExchangeDecorateExchangeCfg extends HawkConfigBase{
	@Id
	private final int id;
	// 需要道具
	private final String needItem;
	// 获取道具
	private final String gainItem;
	//次数
	private final int times;

	private List<RewardItem.Builder> needItemList;
	private List<RewardItem.Builder> gainItemItemList;

	public ExchangeDecorateExchangeCfg() {
		this.id = 0;
		this.needItem = "";
		this.gainItem = "";
		this.times=0;
	}
	
	public int getId() {
		return id;
	}

	public String getNeedItem() {
		return needItem;
	}

	public String getGainItem() {
		return gainItem;
	}

	public List<RewardItem.Builder> getNeedItemList() {
		return needItemList;
	}

	public void setNeedItemList(List<RewardItem.Builder> needItemList) {
		this.needItemList = needItemList;
	}

	public List<RewardItem.Builder> getGainItemItemList() {
		return gainItemItemList;
	}

	public void setGainItemItemList(List<RewardItem.Builder> gainItemItemList) {
		this.gainItemItemList = gainItemItemList;
	}

	public int getTimes() {
		return times;
	}
	
	@Override
	public boolean equals(Object obj) {
		ExchangeDecorateExchangeCfg cfg = (ExchangeDecorateExchangeCfg) obj;
		if(cfg.getId() == getId())
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public boolean assemble() {
		needItemList = RewardHelper.toRewardItemImmutableList(needItem);
		gainItemItemList = RewardHelper.toRewardItemImmutableList(gainItem);
		return true;
	}
	
	@Override
	public boolean checkValid() {
		return 
			ConfigChecker.getDefaultChecker().checkAwardsValid(needItem) &&
			ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem) ;
	}
	
	
}
