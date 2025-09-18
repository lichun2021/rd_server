package com.hawk.game.module.college.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 军事学院在线时长奖励
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/college_level.xml")
public class CollegeLevelCfg extends HawkConfigBase {
	
	@Id
	private final int collegeLevel;
	
	private final int id;
	
	private final int collegeExp;
	
	private final int collegeShopGrid;
	
	private static int levelMax;
	private static int expMax;
	
	public CollegeLevelCfg() {
		collegeLevel = 0;
		collegeExp =0;
		id = 0;
		collegeShopGrid = 0;
	}
	
	@Override
	protected boolean assemble() {
		if(this.collegeLevel > levelMax){
			levelMax = this.collegeLevel;
		}
		expMax += this.collegeExp;
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		return super.checkValid();
	}
	
	public int getCollegeLevel() {
		return collegeLevel;
	}
	
	public int getCollegeExp() {
		return collegeExp;
	}
	
	public int getId() {
		return id;
	}

	public int getCollegeShopGrid() {
		return collegeShopGrid;
	}
	
	
	public static int getExpMax() {
		return expMax;
	}
	
	public static int getLevelMax() {
		return levelMax;
	}
	
}
