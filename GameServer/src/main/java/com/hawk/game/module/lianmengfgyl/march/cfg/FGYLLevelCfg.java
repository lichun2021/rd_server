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
@HawkConfigManager.XmlResource(file = "xml/fgyl_level.xml")
public class FGYLLevelCfg extends HawkConfigBase {
	/** id*/
	private final int id;

	/**等级 */
	@Id
	private final int level;

	/** 奖励*/
	private final String reward;

	/** 是否受限*/
	private final int free;
	
	
	

	public FGYLLevelCfg() {
		id = 0;
		level = 0;
		reward = "";
		free = 0;
	}
	
	public int getId() {
		return id;
	}
	
	public int getLevel() {
		return level;
	}
	
	public String getReward() {
		return reward;
	}
	
	public int getFree() {
		return free;
	}
	

	protected boolean assemble() {
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


}
