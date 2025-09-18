package com.hawk.activity.type.impl.redPackage.cfg;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/***
 * 抢红包信息
 * @author yang.rao
 *
 */

@HawkConfigManager.XmlResource(file = "activity/red_packet/red_packet_achieve.xml")
public class RedPackageAchieveCfg extends HawkConfigBase {

	@Id
	private final int achieveId; 
	
	private final int rewardId;
	
	private final int integral;
	
	private final String rewards;

	
	private Map<Integer,Integer> rewardMap = new HashMap<Integer,Integer>();
	
	public RedPackageAchieveCfg(){
		this.achieveId = 0;
		this.rewardId = 0;
		this.integral = 0;
		this.rewards = "";
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public Map<Integer, Integer> getRewardMap() {
		return rewardMap;
	}

	public void setRewardMap(Map<Integer, Integer> rewardMap) {
		this.rewardMap = rewardMap;
	}

	public int getAchieveId() {
		return achieveId;
	}

	public int getRewardId() {
		return rewardId;
	}

	public int getIntegral() {
		return integral;
	}

	public String getRewards() {
		return rewards;
	}
	
	
}
