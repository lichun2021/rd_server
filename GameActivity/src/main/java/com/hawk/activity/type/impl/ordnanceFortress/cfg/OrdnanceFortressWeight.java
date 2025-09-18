package com.hawk.activity.type.impl.ordnanceFortress.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 时空好礼时空之门直购礼包道具
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/ordnance_fortress/ordnance_fortress_weight.xml")
public class OrdnanceFortressWeight extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	private final int times;
	private final int weight1;
	private final int weight2;
	private final int weight3;
	
	
	public OrdnanceFortressWeight() {
		id = 0;
		times = 0;
		weight1 = 1;
		weight2 = 1;
		weight3 = 1;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public int getId() {
		return id;
	}

	public int getTimes() {
		return times;
	}

	public int getWeight1() {
		return weight1;
	}

	public int getWeight2() {
		return weight2;
	}

	public int getWeight3() {
		return weight3;
	}

	
	
	

	
	
	


}
