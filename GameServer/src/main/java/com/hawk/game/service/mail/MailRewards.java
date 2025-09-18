package com.hawk.game.service.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hawk.game.item.ItemInfo;

public class MailRewards {
	/**
	 * 公共奖励列表(所有人都会发)
	 */
	private List<ItemInfo> publicReward;
	
	/**
	 * 个人奖励集合
	 */
	private Map<String, List<ItemInfo>> selfReward;

	public MailRewards() {
		publicReward = new ArrayList<>();
		selfReward = new HashMap<>();
	}
	
	/**
	 * 添加公共奖励
	 * @param itemInfo
	 * @return
	 */
	public MailRewards addPublicReward(ItemInfo itemInfo){
		publicReward.add(itemInfo);
		return this;
	}
	
	/**
	 * 添加公共奖励列表
	 * @param itemInfos
	 * @return
	 */
	public MailRewards addPublicRewards(List<ItemInfo> itemInfos){
		publicReward.addAll(itemInfos);
		return this;
	}
	
	/**
	 * 添加个人奖励
	 * @param playerId
	 * @param itemInfos
	 * @return
	 */
	public MailRewards addSelfRewards(String playerId, List<ItemInfo> itemInfos){
		if(!selfReward.containsKey(playerId)){
			selfReward.put(playerId, new ArrayList<>());
		}
		selfReward.get(playerId).addAll(itemInfos);
		return this;
	}
	
	/**
	 * 获取邮件奖励
	 * @param playerId
	 * @return
	 */
	public List<ItemInfo> getReward(String playerId) {
		List<ItemInfo> items = new ArrayList<>();
		for (ItemInfo item : publicReward) {
			items.add(item.clone());
		}
		if (selfReward.containsKey(playerId)) {
			for (ItemInfo item : selfReward.get(playerId)) {
				items.add(item.clone());
			}
		}
		return items;
	}
	
	
}
