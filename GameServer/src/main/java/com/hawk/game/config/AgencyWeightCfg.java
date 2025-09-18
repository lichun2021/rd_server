package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

/**
 * 情报中心等级配置
 * 
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/agency_weight_group.xml")
public class AgencyWeightCfg extends HawkConfigBase implements HawkRandObj{

	/**
	 * 等级
	 */
	@Id
	private final int id;
	
	/**
	 * 经验
	 */
	private final int group;
	
	/**
	 * 品质权重
	 */
	private final int quality;
	
	/**
	 * 类型权重 
	 */
	private final int type;

	/**
	 * 权重
	 */
	private final int weight;

	
	public AgencyWeightCfg() {
		id = 0;
		group = 0;
		quality = 0;
		type = 0;
		weight = 0;
		
	}


	public int getId() {
		return id;
	}


	public int getGroup() {
		return group;
	}


	public int getQuality() {
		return quality;
	}


	public int getType() {
		return type;
	}


	@Override
	public int getWeight() {
		return weight;
	}
	
	

}
