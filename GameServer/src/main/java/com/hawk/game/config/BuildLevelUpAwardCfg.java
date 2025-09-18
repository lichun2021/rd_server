package com.hawk.game.config;

import java.util.List;
import java.util.stream.Collectors;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import com.hawk.game.item.ItemInfo;

/**
 * 建筑荣耀奖励配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/build_levelup_award.xml")
public class BuildLevelUpAwardCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 奖励
	protected final String reward;
	
	protected List<ItemInfo> rewardItems;

	
	public BuildLevelUpAwardCfg() {
		id = 0;
		reward = "";
	}
	
	public int getId() {
		return id;
	}

	@Override
	protected boolean assemble() {
		
		if (!HawkOSOperator.isEmptyString(reward)) {
			rewardItems = ItemInfo.valueListOf(reward);
			if (rewardItems == null || rewardItems.isEmpty()) {
				return false;
			}
		}
		
		return true;
	}
	
	public List<ItemInfo> getRewardItems() {
		return rewardItems.stream().map(e -> e.clone()).collect(Collectors.toList());
	}

}
