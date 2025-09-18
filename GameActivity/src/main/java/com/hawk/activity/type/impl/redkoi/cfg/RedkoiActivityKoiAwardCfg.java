package com.hawk.activity.type.impl.redkoi.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 锦鲤大奖配置
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/redkoi/koi_fish_big_reward.xml")
public class RedkoiActivityKoiAwardCfg extends HawkConfigBase{

	@Id
	private final int rewardId;// ="1"
	private final String bigReward;// ="10000_1001_10"
	

	public RedkoiActivityKoiAwardCfg(){
		this.rewardId=0;
		this.bigReward="";
	}

	

	public int getRewardId() {
		return rewardId;
	}



	public String getBigReward() {
		return bigReward;
	}



	





	


	
	
	
	
	
}
