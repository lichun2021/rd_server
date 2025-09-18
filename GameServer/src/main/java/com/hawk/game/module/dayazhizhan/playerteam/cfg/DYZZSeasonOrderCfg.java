package com.hawk.game.module.dayazhizhan.playerteam.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;


/**
 * 达雅赛季
 * @author chechangda
 *
 */
@HawkConfigManager.XmlResource(file = "xml/dyzz_season_order_cfg.xml")
public class DYZZSeasonOrderCfg extends HawkConfigBase{
	/** 活动期数*/
	@Id
	private final int id;

	private final int season;
	/** 需要积分*/
	private final int score; 
	
	/**
	 * 普通奖励
	 */
	private final String reward;
	/**
	 * 进阶奖励
	 */
	private final String advanceReward;

	private final String rewardChoosePool;

	private final String advanceRewardChoosePool;
	


	
	public DYZZSeasonOrderCfg() {
		id = 0;
		season = 0;
		score = 0;
		reward = "";
		advanceReward ="";
		rewardChoosePool ="";
		advanceRewardChoosePool ="";
	}
	
	public int getId() {
		return id;
	}

	public int getSeason() {
		return season;
	}

	public int getScore() {
		return score;
	}
	

	public List<ItemInfo> getRewardItems(int itemId) {
		List<ItemInfo> rewardItems = ItemInfo.valueListOf(this.reward);
		ItemInfo itemTmp = null;
		for (ItemInfo itemInfo : ItemInfo.valueListOf(this.rewardChoosePool)){
			if(itemTmp == null || itemInfo.getItemId() == itemId){
				itemTmp = itemInfo;
			}
		}
		if(itemTmp != null){
			rewardItems.add(itemTmp);
		}
		return rewardItems;
	}
	
	public List<ItemInfo> getAdvanceRewardItems(int itemId) {
		List<ItemInfo> rewardItems = ItemInfo.valueListOf(this.advanceReward);
		ItemInfo itemTmp = null;
		for (ItemInfo itemInfo : ItemInfo.valueListOf(this.advanceRewardChoosePool)){
			if(itemTmp == null || itemInfo.getItemId() == itemId){
				itemTmp = itemInfo;
			}
		}
		if(itemTmp != null){
			rewardItems.add(itemTmp);
		}
		return rewardItems;
	}
	
	
	@Override
	protected boolean assemble() {
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		return true;
	}
}
