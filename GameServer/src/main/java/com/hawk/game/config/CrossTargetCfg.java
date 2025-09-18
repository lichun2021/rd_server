package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.CrossActivity.CrossRankType;

/**
 * 跨服任务配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cross_target.xml")
public class CrossTargetCfg extends HawkConfigBase {
	@Id
	/**
	 * 任务id
	 */
	protected final int id;
	/**
	 * 类型 2-个人,3-联盟
	 */
	private final int scoreType;
	/**
	 * 任务条件
	 */
	private final long scoreValue;
	/**
	 * 解锁需要的作用号 
	 */
	private final int unlockBuff;
	
	/**
	 * 任务奖励
	 */
	private final String rewards;
	
	/**
	 * 积分榜单类型
	 */
	private CrossRankType rankType;
	
	/**
	 * 任务奖励列表
	 */
	private List<ItemInfo> rewardItems;
	
	public CrossTargetCfg() {
		this.id = 0;
		this.scoreType = 0;
		this.scoreValue = 0;
		this.rewards = "";
		this.unlockBuff = 0;
	}

	public int getId() {
		return id;
	}

	public long getScoreValue() {
		return scoreValue;
	}

	public int getUnlockBuff() {
		return unlockBuff;
	}

	public CrossRankType getRankType() {
		return rankType;
	}

	public List<ItemInfo> getRewardItems() {
		List<ItemInfo> copy = new ArrayList<>();
		for(ItemInfo item : rewardItems){
			copy.add(item.clone());
		}
		return copy;
	}

	@Override
	protected boolean assemble() {
		rankType = CrossRankType.valueOf(scoreType);
		if (rankType == null || (rankType != CrossRankType.C_SELF_RANK && rankType != CrossRankType.C_GUILD_RANK)) {
			return false;
		}
		this.rewardItems = ItemInfo.valueListOf(this.rewards);
		return super.assemble();
	}

}
