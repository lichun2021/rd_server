package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/stronger_score.xml")
public class StrongerScoreCfg extends HawkConfigBase {

	@Id
	private final int id;
	
	private final int leftScore;
	
	private final int rightScore;
	
	public StrongerScoreCfg(){
		id = 0;
		leftScore = 0;
		rightScore = 0;
	}

	public int getId() {
		return id;
	}

	public int getLeftScore() {
		return leftScore;
	}

	public int getRightScore() {
		return rightScore;
	}
	
}

