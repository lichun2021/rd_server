package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.item.ItemInfo;

/**
 * 赛博之战出战奖励配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cyborg_personal_award.xml")
public class CyborgPersonAwardCfg extends HawkConfigBase {
	@Id
	private final int id;

	private final int rank;

	/** 积分区间 */
	private final String range;

	/** 奖励 */
	private final String awardPack;

	
	private HawkTuple2<Long, Long> scoreRange;
	/**
	 * 任务奖励列表
	 */
	private List<ItemInfo> rewardItems;

	public CyborgPersonAwardCfg() {
		id = 0;
		rank = 0;
		range = "";
		awardPack = "";
	}

	public int getId() {
		return id;
	}

	public int getRank() {
		return rank;
	}

	public HawkTuple2<Long, Long> getScoreRange() {
		return scoreRange;
	}

	public List<ItemInfo> getRewardItem() {
		List<ItemInfo> copy = new ArrayList<>();
		for (ItemInfo item : rewardItems) {
			copy.add(item.clone());
		}
		return copy;
	}

	protected boolean assemble() {
		this.rewardItems = ItemInfo.valueListOf(this.awardPack);
		if (HawkOSOperator.isEmptyString(this.range)) {
			return false;
		}
		String[] arr = this.range.split("_");
		long min = 0;
		long max = 0;
		if (arr.length == 1) {
			min = Long.valueOf(arr[0]);
			max = Long.MAX_VALUE;
		} else if (arr.length == 2) {
			min = Long.valueOf(arr[0]);
			max = Long.valueOf(arr[1]);
		} else {
			return false;
		}
		scoreRange = new HawkTuple2<Long, Long>(min, max);
		return true;
	}

	@Override
	protected boolean checkValid() {
		if (scoreRange.first > scoreRange.second) {
			return false;
		}
		return true;
	}
}
