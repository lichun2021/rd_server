package com.hawk.activity.type.impl.allyBeatBack.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/ally_beat_back/activity_allybeatback_item.xml")
public class AllyBeatBackItemCfg extends HawkConfigBase {
	/**
	 * 唯一ID
	 */
	@Id
	private final int id;

	/**
	 * 兑换需要的物品
	 */
	private final String needItem;

	/**
	 * 兑换获得物品
	 */
	private final String gainItem;

	/**
	 * 每天的兑换次数
	 */
	private final int dailyTimes;
	
	/**
	 * 兑换次数.
	 */
	private final int times;

	private List<RewardItem.Builder> gainItemList;

	private List<RewardItem.Builder> needItemList;

	public AllyBeatBackItemCfg() {
		id = 0;
		needItem = "";
		gainItem = "";
		dailyTimes = 0;
		this.times = 0;
	}

	public int getId() {
		return id;
	}

	public String getNeedItem() {
		return needItem;
	}

	public String getGainItem() {
		return gainItem;
	}

	public int getDailyTimes() {
		return dailyTimes;
	}

	public boolean assemble() {
		try {
			this.gainItemList = RewardHelper.toRewardItemImmutableList(this.gainItem);
			this.needItemList = RewardHelper.toRewardItemImmutableList(this.needItem);
			return true;
		} catch (Exception arg1) {
			HawkException.catchException(arg1, new Object[0]);
			return false;
		}
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(needItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("AllyBeatBackItemCfg reward error, id: %s , needItem: %s", id, needItem));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("AllyBeatBackItemCfg reward error, id: %s , gainItem: %s", id, gainItem));
		}
		return super.checkValid();
	}

	public List<RewardItem.Builder> getNeedItemList() {
		return needItemList;
	}

	public List<RewardItem.Builder> getGainItemList() {
		return gainItemList;
	}

	public int getTimes() {
		return times;
	}
}
