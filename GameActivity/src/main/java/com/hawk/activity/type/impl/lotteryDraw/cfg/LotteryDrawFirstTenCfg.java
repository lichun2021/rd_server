package com.hawk.activity.type.impl.lotteryDraw.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 十连抽活动首次连抽奖励配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/lottery_draw/lottery_draw_first_ten.xml")
public class LotteryDrawFirstTenCfg extends HawkConfigBase {
	/** 抽奖次数 */
	@Id
	private final int times;

	/** 格子id */
	private final int cellId;

	/** 奖励倍数 */
	private final int multi;

	public LotteryDrawFirstTenCfg() {
		times = 0;
		cellId = 0;
		multi = 0;
	}

	public int getTimes() {
		return times;
	}

	public int getCellId() {
		return cellId;
	}

	public int getMulti() {
		return multi;
	}

	@Override
	protected boolean checkValid() {
		LotteryDrawCellCfg cellCfg = HawkConfigManager.getInstance().getConfigByKey(LotteryDrawCellCfg.class, cellId);
		if (cellCfg == null) {
			return false;
		}
		return super.checkValid();
	}
}
