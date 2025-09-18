package com.hawk.activity.type.impl.lotteryDraw.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 十连抽活动倍率权重配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/lottery_draw/lottery_draw_multi_weight.xml")
public class LotteryDrawMultiWeightCfg extends HawkConfigBase {
	@Id
	private final int id;

	/** 倍率 */
	private final int multi;

	/** 权重 */
	private final int weight;

	public LotteryDrawMultiWeightCfg() {
		id = 0;
		multi = 0;
		weight = 0;
	}

	public int getId() {
		return id;
	}

	public int getMulti() {
		return multi;
	}

	public int getWeight() {
		return weight;
	}

}
