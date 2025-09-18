package com.hawk.game.module.college.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 军事学院普通商店配置
 * @author lating
 */
@HawkConfigManager.XmlResource(file = "xml/college_shop.xml")
public class CollegeShopCfg extends HawkConfigBase {
	@Id
	private final int id;
	private final String needItem;
	private final String gainItem;
	/**
	 * 军事学院等级
	 */
	private final int collegeLevel;
	/**
	 * 限购类型：日刷新(1)，周刷新(2)，月刷新(3)，终身一次(4)
	 */
	private final int refreshType;
	/**
	 * 限购次数
	 */
	private final int times;
	
	public CollegeShopCfg() {
		id = 0;
		needItem = "";
		gainItem = "";
		collegeLevel = 0;
		refreshType = 0;
		times = 0;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		return super.checkValid();
	}
	
	public int getId() {
		return id;
	}
	
	public int getCollegeLevel() {
		return collegeLevel;
	}
	
	
	public String getNeedItem() {
		return needItem;
	}
	
	public String getGainItem() {
		return gainItem;
	}
	
	public int getTimes() {
		return times;
	}
	
	public int getRefreshType() {
		return refreshType;
	}
}
