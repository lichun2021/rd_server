package com.hawk.activity.type.impl.equipBlackMarket.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 指挥官学院活动礼包配置
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/arms_market/arms_market_reward.xml")
public class EquipBlackMarketRefineCfg extends HawkConfigBase {
	/** id*/
	@Id
	private final int id;

	private final String material1;
	
	private final String material2;
	
	private final String target;
	
	private final int limit;

	

	public EquipBlackMarketRefineCfg() {
		id = 0;
		material1 = "";
		material2 = "";
		target = "";
		limit = 0;
	}



	


	public int getId() {
		return id;
	}






	public String getMaterial1() {
		return material1;
	}






	public String getMaterial2() {
		return material2;
	}






	public String getTarget() {
		return target;
	}






	public int getLimit() {
		return limit;
	}



	

	
	
	


	

	
	
	
	
	
	
}
