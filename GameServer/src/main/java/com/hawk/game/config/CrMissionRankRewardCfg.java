package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "xml/cr_rank_reward.xml")
public class CrMissionRankRewardCfg extends HawkConfigBase {
	
	@Id
	private final int id;
	
	private final String rank;
	
	private final String reward;
	
	public CrMissionRankRewardCfg(){
		this.id = 0;
		this.rank = "";
		this.reward = "";
	}

	public int getId() {
		return id;
	}

	public String getRank() {
		return rank;
	}

	public String getReward() {
		return reward;
	}

	@Override
	protected boolean checkValid() {
		try {
			String []src = rank.split("_");
			if(src.length == 1){
				Integer.valueOf(src[0]);
			}else if(src.length == 2){
				int min = Integer.valueOf(src[0]);
				int max = Integer.valueOf(src[1]);
				if(min >= max){
					throw new RuntimeException(String.format("cr_rank_reward.xml配置出错,id:%d, rank:%s", id, rank));
				}
			}
			boolean result = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
			if(!result){
				throw new RuntimeException(String.format("cr_rank_reward.xml排行奖励出错,id:%d, reward:%s", id, reward));
			}
		} catch (Exception e) {
			throw e;
		}
		return super.checkValid();
	}
	
	
}
