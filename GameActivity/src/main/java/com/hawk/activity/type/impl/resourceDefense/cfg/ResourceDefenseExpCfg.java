package com.hawk.activity.type.impl.resourceDefense.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 资源保卫战购买经验配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/resource_defense/resource_defense_exp.xml")
public class ResourceDefenseExpCfg extends HawkConfigBase {

	@Id
	private final int id;
	
	/**
	 * 经验
	 */
	private final int exp;
	
	/**
	 * 价格
	 */
	private final String price;
	
	public ResourceDefenseExpCfg(){
		id=0;
		exp = 0;
		price = "";
	}

	public int getId() {
		return id;
	}

	public int getExp() {
		return exp;
	}

	public String getPrice() {
		return price;
	}
}
