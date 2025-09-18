package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;

/**
 * 大本升级可选礼包奖励配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/baseUpgrade.xml")
public class BaseUpgradeCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 建筑ID
	protected final int baseBuildId;
	// 奖励
	protected final String reward;
	
	private List<ItemInfo> awardList = new ArrayList<ItemInfo>();

	public BaseUpgradeCfg() {
		id = 0;
		baseBuildId = 0;
		reward = "";
	}

	public int getId() {
		return id;
	}

	public int getBaseBuildId() {
		return baseBuildId;
	}

	public String getReward() {
		return reward;
	}
	
	public boolean assemble() {
		if (!HawkOSOperator.isEmptyString(reward)) {
			String[] rewards = reward.split(";");
			for (String rewardStr : rewards) {
				ItemInfo itemInfo = ItemInfo.valueOf(rewardStr);
				awardList.add(itemInfo);
			}
		}
		
		return super.assemble();
	}
	
	public ItemInfo getReward(int rewardPos) {
		if (awardList.isEmpty() || rewardPos > awardList.size() || rewardPos <= 0) {
			return null;
		}
		
		return awardList.get(rewardPos - 1).clone();
	}
}
