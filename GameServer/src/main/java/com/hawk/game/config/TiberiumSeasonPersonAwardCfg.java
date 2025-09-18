package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;

/**
 * 泰伯利亚联赛联盟奖励配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/tiberium_season_person_award.xml")
public class TiberiumSeasonPersonAwardCfg extends HawkConfigBase {
	@Id
	private final int id;

	private final int score;

	/** 奖励 */
	private final String award;

	/**
	 * 任务奖励列表
	 */
	private List<ItemInfo> rewardItems;

	public TiberiumSeasonPersonAwardCfg() {
		id = 0;
		score = 0;
		award = "";
	}

	public int getId() {
		return id;
	}

	public int getScore() {
		return score;
	}

	public List<ItemInfo> getRewardItem() {
		List<ItemInfo> copy = new ArrayList<>();
		for (ItemInfo item : rewardItems) {
			copy.add(item.clone());
		}
		return copy;
	}

	protected boolean assemble() {
		this.rewardItems = ItemInfo.valueListOf(this.award);
		return true;
	}
}
