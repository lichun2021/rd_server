package com.hawk.activity.type.impl.submarineWar.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 *  中部养成计划
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/submarine_war/submarine_war_monster.xml")
public class SubmarineWarMonsterCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	private final int type;
	private final int score;
	
	
	public SubmarineWarMonsterCfg() {
		id = 0;
		type = 0;
		score = 0;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	

	public int getScore() {
		return score;
	}
	
	
	public int getType() {
		return type;
	}


	@Override
	protected final boolean checkValid() {
		return super.checkValid();
	}
	

}
