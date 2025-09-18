package com.hawk.activity.type.impl.armamentexchange.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * @author luke
 */
@HawkConfigManager.XmlResource(file = "activity/arms_upgrade/arms_upgrade_exchange.xml")
public class ArmamentExchangeCostCfg extends HawkConfigBase {
	/** 成就id */
	@Id
	private final int id;
	private final int boxId;
	private final String needItem;
	private RewardItem.Builder awardItem;
	
	public static Map<Integer,List<Integer>> map = new ConcurrentHashMap<>();
	public static Map<Integer,List<RewardItem.Builder>> itemMap = new ConcurrentHashMap<>();
	
	public ArmamentExchangeCostCfg() {
		id = 0;
		boxId = 0;
		needItem="";
	}

	public int getId() {
		return id;
	}

	public RewardItem.Builder getAwardItem() {
		return awardItem;
	}

	public void setAwardItem(RewardItem.Builder awardItem) {
		this.awardItem = awardItem;
	}
	
	public int getBoxId() {
		return boxId;
	}

	public String getNeedItem() {
		return needItem;
	}

	@Override
	protected boolean assemble() {
		awardItem = RewardHelper.toRewardItem(needItem);
		
		List<RewardItem.Builder> itemList = itemMap.get(boxId);
		if(itemList == null){
			itemList = new ArrayList<RewardItem.Builder>();
			itemMap.put(boxId, itemList);
		}
		itemList.add(awardItem);
		
		List<Integer> list = map.get(boxId);
		if(list == null){
			list = new CopyOnWriteArrayList<Integer>();
			map.put(boxId, list);
		}
		if(!list.contains(awardItem.getItemId()))
			list.add(awardItem.getItemId());
		return true;
	}

}

