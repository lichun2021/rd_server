package com.hawk.activity.type.impl.plan.cfg;


import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;



/**
 * 阶段积分奖励配置
 * @author RickMei
 *
 */
@HawkConfigManager.XmlResource(file = "activity/plan/plan_integral.xml")
public class PlanIntegralCfg extends HawkConfigBase {

	/** */
	@Id
	private final int id;
	/** 最小*/
	private final int min;
	/** 最大*/
	private final int max;

	
	public PlanIntegralCfg() {
		id = 0;
		min = 0;
		max = 0;
	}
	

	public int getId() {
		return id;
	}

	public int getMin() {
		return min;
	}


	public int getMax() {
		return max;
	}

}
