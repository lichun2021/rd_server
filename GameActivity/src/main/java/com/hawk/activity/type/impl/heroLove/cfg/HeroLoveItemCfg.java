package com.hawk.activity.type.impl.heroLove.cfg;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;


/**
 * 委任英雄
 * @author jm
 *
 */
@HawkConfigManager.XmlResource(file = "activity/hero_love/hero_love_item.xml")
public class HeroLoveItemCfg extends HawkConfigBase{
	@Id
	private final int id;

	/**
	 * 是否是免费物品.
	 */
	private final boolean free;
	/**
	 * 最小亲密度
	 */
	private final int minLove;
	/**
	 * 最大亲密度
	 */
	private final int maxLove;
	/**
	 * 扣取的道具
	 */	
	private final String costItem;
	/**
	 * 亲密度的
	 */
	private final String loves;
	/**
	 * 值
	 */
	private List<Integer> loveCritList;
	/**
	 * 权重.
	 */
	private List<Integer> loveRateList;
	/**
	 * 扣取的道具.
	 */
	private List<RewardItem.Builder> costItemList;
	
	public HeroLoveItemCfg() {
		this.id = 0;		
		this.minLove = 0;
		this.maxLove = 0;
		this.free = false;
		this.costItem = "";
		this.loves = "";
	}
	
	@Override
	public boolean assemble() {
		List<RewardItem.Builder> itemList = RewardHelper.toRewardItemImmutableList(costItem);
		this.costItemList = Collections.synchronizedList(itemList);
		
		String[] loveArray = loves.split(",");
		List<Integer> critList = new ArrayList<>();
		List<Integer> rateList = new ArrayList<>();
		for (String loveItem : loveArray) {
			String[] valueRate = loveItem.split("_");
			critList.add(Integer.valueOf(valueRate[0]));
			rateList.add(Integer.valueOf(valueRate[1]));
		}
		
		this.loveCritList = Collections.synchronizedList(critList);
		this.loveRateList = Collections.synchronizedList(rateList);
		
		return true;
	}
	
	public int getMinLove() {
		return minLove;
	}
	public int getMaxLove() {
		return maxLove;
	}

	public int getId() {
		return id;
	}

	public boolean isFree() {
		return free;
	}

	public List<RewardItem.Builder> getCostItemList() {
		return costItemList;
	}
	
	/**
	 * 随机一个亲密度出来.
	 * @return
	 */
	public int getRandomLoveCrit() {
		return HawkRand.randomWeightObject(loveCritList, loveRateList, 1).get(0);
	}
}
