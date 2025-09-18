package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;

/**
 * 全军突击(累积在线)
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/accumulate_online.xml")
public class AccumulateOnlineCfg extends HawkConfigBase {

	/**
	 * 配置id
	 */
	@Id
	private final int id;
	
	private final int dayCount;
	
	private final int posId;
	
	private final int onlineTime;
	
	private final String rewards;
	
	/**
	 * 奖励列表
	 */
	private List<ItemInfo> rewardList;
	
	public AccumulateOnlineCfg() {
		this.id = 0;
		this.dayCount = 0;
		this.posId = 0;
		this.onlineTime = 0;
		this.rewards = "";
	}

	public int getId() {
		return id;
	}

	public int getDayCount() {
		return dayCount;
	}

	public int getPosId() {
		return posId;
	}

	public long getOnlineTime() {
		return onlineTime * 1000L;
	}

	public String getRewards() {
		return rewards;
	}
	
	public List<ItemInfo> getRewardList() {
		return rewardList;
	}

	@Override
	protected boolean assemble() {
		if (HawkOSOperator.isEmptyString(rewards)) {
			return false;
		}
		rewardList = ItemInfo.valueListOf(rewards);
		return true;
	}
}
