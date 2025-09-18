package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 联盟领地配置
 *
 * @author hawk
 *
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_manor.xml")
public class GuildManorCfg extends HawkConfigBase {
	
	/** 哨塔Id */
	@Id
	protected final int id; 
	
	/** 默认名称 */
	protected final String defaultName;
	
	/** 联盟人数限制 */
	protected final int peopleLimit;
	
	/** 联盟战力限制 */
	protected final int powerLimit;
	
	/** 联盟科技限制 */
	protected final int scienceLimit;
	
	/** 建筑值上限 */
	protected final int buildingUpLimit;
	
	/** 重置建筑值 */
	protected final int resetBuilding;
	
	/** 箭塔开启数量 */
	protected final int towerCount;
	
	/** 初始建筑值 */
	protected final int buildingInitial;
	
	public GuildManorCfg() {
		this.id = 0;
		this.defaultName = "";
		this.peopleLimit = 0;
		this.powerLimit = 0;
		this.scienceLimit = 0;
		this.buildingUpLimit = 0;
		this.resetBuilding = 0;
		this.towerCount = 0;
		this.buildingInitial = 0;
	}

	public int getId() {
		return id;
	}

	public String getDefaultName() {
		return defaultName;
	}

	public int getPeopleLimit() {
		return peopleLimit;
	}

	public int getPowerLimit() {
		return powerLimit;
	}

	public int getScienceLimit() {
		return scienceLimit;
	}

	public int getBuildingUpLimit() {
		return buildingUpLimit;
	}

	public int getResetBuilding() {
		return resetBuilding;
	}

	public int getTowerCount() {
		return towerCount;
	}

	public int getBuildingInitial() {
		return buildingInitial;
	}
}
