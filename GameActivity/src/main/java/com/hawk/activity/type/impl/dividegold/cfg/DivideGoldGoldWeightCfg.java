package com.hawk.activity.type.impl.dividegold.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
/**瓜分金币 红包开金币配置数据
 * @author Winder
 */
@HawkConfigManager.XmlResource(file = "activity/divide_gold/dividegold_gold_weight.xml")
public class DivideGoldGoldWeightCfg extends HawkConfigBase{
	@Id
	private final int id;
	//奖励金币数量
	private final int goldNum;
	//权重
	private final int weight;

	public DivideGoldGoldWeightCfg() {
		id = 0;
		goldNum = 0;
		weight = 0;
	}

	public int getId() {
		return id;
	}

	public int getGoldNum() {
		return goldNum;
	}


	public int getWeight() {
		return weight;
	}

}
