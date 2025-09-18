package com.hawk.activity.type.impl.shootingPractice.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/shooting_practice/shooting_practice_enemy.xml")
public class ShootingPracticeEnemyCfg extends HawkConfigBase {
	
	@Id
	private final int enemyId;
	
	private final int score;
	
	private final int doubleScore;
	
	private final int shootAddValue;
	
	private final int timeAddValue;
	
	public ShootingPracticeEnemyCfg(){
		this.enemyId = 0;
		this.score = 0;
		this.doubleScore = 0;
		this.shootAddValue = 0;
		this.timeAddValue = 0;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		return super.checkValid();
	}

	public int getEnemyId() {
		return enemyId;
	}
	
	public int getDoubleScore() {
		return doubleScore;
	}
	
	public int getScore() {
		return score;
	}
	
	public int getShootAddValue() {
		return shootAddValue;
	}
	
	public int getTimeAddValue() {
		return timeAddValue;
	}
}
