package com.hawk.activity.type.impl.lotteryDraw.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 十连抽活动翻倍阶段配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/lottery_draw/lottery_draw_multi_stage.xml")
public class LotteryDrawMultiStageCfg extends HawkConfigBase {
	@Id
	private final int id;

	/** 最小次数 */
	private final int min;

	/** 最大次数 */
	private final int max;

	/** 翻倍概率 */
	private final int rate;

	public LotteryDrawMultiStageCfg() {
		id = 0;
		min = 0;
		max = 0;
		rate = 0;
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

	public int getRate() {
		return rate;
	}

}
