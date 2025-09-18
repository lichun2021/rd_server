package com.hawk.game.module.dayazhizhan.playerteam.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;


/**
 * 达雅赛季段位
 * @author chechangda
 *
 */
@HawkConfigManager.XmlResource(file = "xml/dyzz_season_grade_cfg.xml")
public class DYZZSeasonGradeCfg extends HawkConfigBase{
	/** 活动期数*/
	@Id
	private final int id;
	
	/** 需要积分*/
	private final int score; 
	/**
	 * 赛季积分增长计算参数
	 */
	private final double winScoreParam;
	private final double lossScoreParam;
	/**
	 * 首胜奖励
	 */
	private final String firstWinReward;
	private final String winReward;
	private final String lossReward;
	private final String mvpReward;
	private final String smvpReward;
	private final String negativeReward;
	/**
	 * 段位结算奖励
	 */
	private final String settlementReward;


	public DYZZSeasonGradeCfg() {
		id = 0;
		score = 0;
		winScoreParam = 0;
		lossScoreParam = 0;
		
		firstWinReward = "";
		winReward = "";
		lossReward = "";
		mvpReward = "";
		smvpReward = "";
		negativeReward = "";
		settlementReward = "";
	}
	
	public int getId() {
		return id;
	}
	
	public int getScore() {
		return score;
	}
	
	public double getWinScoreParam() {
		return winScoreParam;
	}
	
	public double getLossScoreParam() {
		return lossScoreParam;
	}
	
	
	
	
	
	public List<ItemInfo> getFirstWinRewardItems() {
		List<ItemInfo> rewardItems = ItemInfo.valueListOf(this.firstWinReward);
		return rewardItems;
	}
	
	public List<ItemInfo> getSettlementRewardItems() {
		List<ItemInfo> rewardItems = ItemInfo.valueListOf(this.settlementReward);
		return rewardItems;
	}
	
	public List<ItemInfo> getwinRewardItems(){
		List<ItemInfo> rewardItems = ItemInfo.valueListOf(this.winReward);
		return rewardItems;
	}
	

	public List<ItemInfo> getLossRewardItems(){
		List<ItemInfo> rewardItems = ItemInfo.valueListOf(this.lossReward);
		return rewardItems;
	}
	
	public List<ItemInfo> getMvpRewardItems() {
		List<ItemInfo> rewardItems = ItemInfo.valueListOf(this.mvpReward);
		return rewardItems;
	}
	
	public List<ItemInfo> getSmvpRewardItems() {
		List<ItemInfo> rewardItems = ItemInfo.valueListOf(this.smvpReward);
		return rewardItems;
	}

	public List<ItemInfo> getNegativeRewardItems() {
		List<ItemInfo> rewardItems = ItemInfo.valueListOf(this.negativeReward);
		return rewardItems;
	}
	
	public double getScoreParam(boolean win){
		if(win){
			return this.getWinScoreParam();
		}
		return this.getLossScoreParam();
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
