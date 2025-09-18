
package com.hawk.activity.type.impl.dailysign.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 补签 
 * @author RickMei 
 *
 */
@HawkConfigManager.XmlResource(file = "activity/daily_sign/daily_sign_retroactive.xml")
public class DailySignRetroactiveCfg extends HawkConfigBase {

	@Id
	private final int id;

	private  final int times;
	
	private final String retroactiveCost;
	
	private List<RewardItem.Builder> costList;
	
	public DailySignRetroactiveCfg(){
		id = 0;
		times = 0;
		retroactiveCost = null;
	}
	
	@Override
	protected boolean assemble() {
		costList = RewardHelper.toRewardItemImmutableList(retroactiveCost);
		return super.assemble();
	}
	
	public int getId() {
		return id;
	}

	public int getTimes() {
		return times;
	}

	public String getRetroactiveCost() {
		return retroactiveCost;
	}

	public List<RewardItem.Builder> getCostList() {
		return costList;
	}
}
