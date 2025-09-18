package com.hawk.activity.type.impl.skinPlan.cfg;

import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;
@HawkConfigManager.KVResource(file = "activity/hero_skin/hero_skin_cfg.xml")
public class SkinPlanActivityKVCfg extends HawkConfigBase {
	private final int serverDelay;
	
	private final String extReward;
	
	private final String getItem;
	
	private final String diceWeight;
	
	private Map<Integer, Integer> diceWeightMap;
	
	private List<RewardItem.Builder> extRewardList;
	
	private RewardItem.Builder getItemList;
	
	public SkinPlanActivityKVCfg() {
		this.serverDelay = 0;
		this.extReward = "";
		this.getItem = "";
		this.diceWeight = "";
	}
	
	

	@Override
	protected boolean assemble() {
		try {
			extRewardList = RewardHelper.toRewardItemImmutableList(extReward);
			getItemList = RewardHelper.toRewardItem(getItem);
			diceWeightMap = SerializeHelper.stringToMap(diceWeight, Integer.class, Integer.class, SerializeHelper.ATTRIBUTE_SPLIT, SerializeHelper.BETWEEN_ITEMS);
			if (diceWeightMap == null) {
				HawkLog.errPrintln("SkinPlanActivityKVCfg assemble faild  diceWeight:{}", diceWeight);
				return false;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}


	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getExtReward() {
		return extReward;
	}

	public String getGetItem() {
		return getItem;
	}

	public String getDiceWeight() {
		return diceWeight;
	}

	public Map<Integer, Integer> getDiceWeightMap() {
		return diceWeightMap;
	}

	public List<RewardItem.Builder> getExtRewardList() {
		return extRewardList;
	}

	public RewardItem.Builder getGetItemBuilder() {
		return getItemList;
	}
}
