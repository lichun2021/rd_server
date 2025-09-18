package com.hawk.activity.type.impl.shootingPractice.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 *  中部养成计划
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/shooting_practice/shooting_practice_cfg.xml")
public class ShootingPracticeKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	//游戏持有上线
	private final int gameCountLimit;
	//购买次数消耗
	private final String buyGameCountCost;
	//购买次数上限
	private final int buyGameTimes; 
	//排行榜显示个数
	private final int rankSize;
	
	private final String scoreItem;
	
	private final int barrageSize;
	
	private final int atkValue;
	
	// 双杀额外分数
	private final int doubleKillValue;

	// 三杀额外分数
	private final int tripleKillValue;
	
	public ShootingPracticeKVCfg() {
		serverDelay = 0;
		gameCountLimit = 0;
		buyGameCountCost = "";
		rankSize = 0;
		buyGameTimes = 0;
		scoreItem= "";
		barrageSize = 0;
		atkValue =0;
		doubleKillValue = 0;
		tripleKillValue = 0;
	}
	
	
	@Override
	protected boolean assemble() {
		return true;
	}


	public long getServerDelay() {
		return serverDelay  * 1000l;
	}


	public int getRankSize() {
		return rankSize;
	}
	
	
	public int getGameCountLimit() {
		return gameCountLimit;
	}
	
	
	public int getBarrageSize() {
		return barrageSize;
	}
	
	public int getAtkValue() {
		return atkValue;
	}
	
	
	public int getDoubleKillValue() {
		return doubleKillValue;
	}
	
	public int getTripleKillValue() {
		return tripleKillValue;
	}
	
	public RewardItem.Builder getBuyGameCountCost(int buyTimes) {
		String[] arr = this.buyGameCountCost.split(";");
		if(buyTimes <= arr.length){
			return RewardHelper.toRewardItem(arr[buyTimes -1]);
		}
		return RewardHelper.toRewardItem(arr[arr.length -1]);
	}
	
	public int getBuyGameTimes() {
		return buyGameTimes;
	}

	
	public RewardItem.Builder getScoreItem() {
		return RewardHelper.toRewardItem(this.scoreItem);
	}
	
}