package com.hawk.activity.type.impl.plan.cfg;


import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkRand;


/**
 * 阶段积分奖励配置
 * @author RickMei
 *
 */
@HawkConfigManager.XmlResource(file = "activity/plan/plan_integral_crit.xml")
public class PlanIntegralCritCfg extends HawkConfigBase {



	/** */
	@Id
	private final int id;
	/** 倍率*/
	private final float multiple;
	/** 权重*/
	private final float weight;

	
	public PlanIntegralCritCfg() {
		id = 0;
		multiple = 0;
		weight = 0;
	}
	
	static public PlanIntegralCritCfg randomCfg(){
		int randWeight = HawkRand.randInt(1, PlanIntegralCritCfg.getTotalWeight());
		ConfigIterator<PlanIntegralCritCfg> iter = HawkConfigManager.getInstance().getConfigIterator(PlanIntegralCritCfg.class);
		while(iter.hasNext()){
			PlanIntegralCritCfg cfg = iter.next();
			if(cfg.weight < randWeight){
				randWeight -= cfg.weight;
				continue;
			}
			return cfg;
		}
		return null;
	}
	
	@Override
	protected boolean assemble() {
		return super.assemble();
	}
	
	public int getId() {
		return id;
	}


	public float getMultiple() {
		return multiple;
	}


	public float getWeight() {
		return weight;
	}

	public static int getTotalWeight() {
		int ret = 0;
		ConfigIterator<PlanIntegralCritCfg> iter = HawkConfigManager.getInstance().getConfigIterator(PlanIntegralCritCfg.class);
		while(iter.hasNext()){
			ret += iter.next().getWeight();
		}
		return ret;
	}
}
