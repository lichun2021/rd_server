package com.hawk.game.module.lianmengfgyl.march.cfg;

import java.security.InvalidParameterException;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 月球之战时间配置
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "xml/fgyl_rank_reward.xml")
public class FGYLRankRewardCfg extends HawkConfigBase {
	/** id*/
	@Id
	private final int id;

	/**等级 */
	private final int rankMin;
	
	private final int rankMax;

	/** 奖励*/
	private final String reward;

	
	private static int rankCount;
	
	

	public FGYLRankRewardCfg() {
		id = 0;
		rankMin = 0;
		rankMax = 0;
		reward = "";
	}
	
	public int getId() {
		return id;
	}
	
	public int getRankMin() {
		return rankMin;
	}
	
	public int getRankMax() {
		return rankMax;
	}
	public String getReward() {
		return reward;
	}
	

	protected boolean assemble() {
		if(this.rankMax > rankCount){
			rankCount = this.rankMax;
		}
		return true;
	}
	
	
	
	@Override
	protected boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.reward);
        if (!valid) {
            throw new InvalidParameterException(String.format("FGYLLevelCfg reward error, id: %s , cost: %s", id, reward));
        }
        return super.checkValid();
	}

	public static int getRankCount() {
		return rankCount;
	}

}
