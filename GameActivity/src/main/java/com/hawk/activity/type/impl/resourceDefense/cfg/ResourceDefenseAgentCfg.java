package com.hawk.activity.type.impl.resourceDefense.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资源保卫特工配置
 * hf
 */
@HawkConfigManager.XmlResource(file = "activity/resource_defense/resource_defense_agent.xml")
public class ResourceDefenseAgentCfg extends HawkConfigBase {
	@Id
	private final int id;
	//组随机
	private final String abilityRefresh;
	//每天刷新次数
	private final int refreshNumLimit;
	//刷新消耗
	private final String refreshCost;


	private Map<Integer, Integer> abilityRefreshMap = new HashMap<>();
	/**
	 * 奖励列表
	 */
	private List<Reward.RewardItem.Builder> refreshCostItem;

	public ResourceDefenseAgentCfg(){
		id=0;
		abilityRefresh = "";
		refreshNumLimit = 0;
		refreshCost = "";
	}


	@Override
	protected boolean assemble() {
		List<Reward.RewardItem.Builder> refreshCostItem = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(refreshCost)) {
			refreshCostItem = RewardHelper.toRewardItemImmutableList(refreshCost);
		}
		this.refreshCostItem = refreshCostItem;
		this.abilityRefreshMap = SerializeHelper.stringToMap(abilityRefresh, Integer.class, Integer.class, "_", ",");
		return true;
	}


	public int getId() {
		return id;
	}



	public int getRefreshNumLimit() {
		return refreshNumLimit;
	}

	public String getRefreshCost() {
		return refreshCost;
	}

	public List<Reward.RewardItem.Builder> getRefreshCostItem() {
		return refreshCostItem;
	}

	public void setRefreshCostItem(List<Reward.RewardItem.Builder> refreshCostItem) {
		this.refreshCostItem = refreshCostItem;
	}

	public String getAbilityRefresh() {
		return abilityRefresh;
	}

	public Map<Integer, Integer> getAbilityRefreshMap() {
		return abilityRefreshMap;
	}

	public void setAbilityRefreshMap(Map<Integer, Integer> abilityRefreshMap) {
		this.abilityRefreshMap = abilityRefreshMap;
	}
}
