package com.hawk.game.module.college.cfg;

import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;


/**
 * 军事学院在线时长奖励
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/college_online_reward.xml")
public class CollegeOnlineRewardCfg extends HawkConfigBase {
	
	/** */
	@Id
	private final int id;
	
	/** 在线时长: 单位 s*/
	private final int onlineSeconds;
	
	/** 达成奖励列表(通用奖励格式)*/
	private final String rewards;
	
	private List<ItemInfo> rewardItemInfo;
	
	public CollegeOnlineRewardCfg() {
		id = 0;
		onlineSeconds = 0;
		rewards = "";
	}
	
	@Override
	protected boolean assemble() {
		rewardItemInfo = ItemInfo.valueListOf(rewards);
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		if (rewardItemInfo != null) {
			for (ItemInfo itemInfo : rewardItemInfo) {
				if (!itemInfo.checkItemInfo()) {
					return false;
				}
			}
		}
		return super.checkValid();
	}
	
	public int getId() {
		return id;
	}

	public long getOnlineTime() {
		return onlineSeconds * 1000L;
	}

	public String getRewards() {
		return rewards;
	}
	
	public List<ItemInfo> getRewardItemInfo() {
		return rewardItemInfo.stream().map(e -> e.clone()).collect(Collectors.toList());
	}
	
	
}
