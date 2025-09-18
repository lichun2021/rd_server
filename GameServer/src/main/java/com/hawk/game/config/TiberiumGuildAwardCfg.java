package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;

/**
 * 泰伯利亚之战联盟奖励配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/tiberium_guild_award.xml")
public class TiberiumGuildAwardCfg extends HawkConfigBase {
	@Id
	private final int id;

	private final int isWin;

	/** 奖励 */
	private final String awardPack;

	/**
	 * 任务奖励列表
	 */
	private List<ItemInfo> rewardItems;

	public TiberiumGuildAwardCfg() {
		id = 0;
		isWin = 0;
		awardPack = "";
	}

	public int getId() {
		return id;
	}

	public boolean isWin() {
		return isWin == 1;
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
		return true;
	}
}
