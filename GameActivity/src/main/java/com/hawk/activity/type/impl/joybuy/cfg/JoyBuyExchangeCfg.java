package com.hawk.activity.type.impl.joybuy.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
@HawkConfigManager.XmlResource(file = "activity/joy_exchange/joy_exchange.xml")
public class JoyBuyExchangeCfg extends HawkConfigBase implements HawkRandObj{
	@Id
	private final int id;
	// 组
	private final int pool;
	private final int weight;
	// 需要道具
	private final String needItem;
	// 获取道具
	private final String gainItem;
	//次数
	private final int times;

	private List<RewardItem.Builder> needItemList;
	private List<RewardItem.Builder> gainItemItemList;

	private static Map<Integer,List<JoyBuyExchangeCfg>> groupExchange = new HashMap<>() ;
	
	public JoyBuyExchangeCfg() {
		this.id = 0;
		this.pool = 0;
		this.weight =0;
		this.needItem = "";
		this.gainItem = "";
		this.times=0;
	}
	
	public int getId() {
		return id;
	}

	public int getPool() {
		return pool;
	}

	@Override
	public int getWeight() {
		return weight;
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

	public static List<JoyBuyExchangeCfg> getGroupExchange(int pool) {
		return groupExchange.get(pool);
	}
	
	public int getTimes() {
		return times;
	}
	
	@Override
	public boolean equals(Object obj) {
		JoyBuyExchangeCfg cfg = (JoyBuyExchangeCfg) obj;
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
		
		List<JoyBuyExchangeCfg> cfgs = groupExchange.get(pool);
		if(cfgs == null){
			cfgs = new ArrayList<>();
			groupExchange.put(pool, cfgs);
		}
		cfgs.add(this);
		return true;
	}
	
	@Override
	public boolean checkValid() {
		return 
			ConfigChecker.getDefaultChecker().checkAwardsValid(needItem) &&
			ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem) ;
	}
	
	
}
