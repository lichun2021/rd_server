package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 国家基础配置
 * @author zhenyu.shang
 * @since 2022年4月11日
 */
@HawkConfigManager.XmlResource(file = "xml/nation_construction_base.xml")
public class NationConstructionBaseCfg extends HawkConfigBase {

	@Id
	protected final int nationBuild;
	
	protected final int buildType;
	
	protected final int isOpen;
	
	protected final int maxLv;
	
	protected final int x;
	
	protected final int y;
	
	protected final int daylimit;
	
	public NationConstructionBaseCfg() {
		this.nationBuild = 0;
		this.buildType = 0;
		this.x = 0;
		this.y = 0;
		this.isOpen = 0;
		this.maxLv = 0;
		this.daylimit = 0;
	}

	public int getNationBuild() {
		return nationBuild;
	}

	public int getBuildType() {
		return buildType;
	}

	public int getIsOpen() {
		return isOpen;
	}

	public int getMaxLv() {
		return maxLv;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getDaylimit() {
		return daylimit;
	}
}

