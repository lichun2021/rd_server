package com.hawk.game.player.tick;

import com.hawk.game.player.tick.impl.BuffTicker;
import com.hawk.game.player.tick.impl.CoreExlopreActivityTicker;
import com.hawk.game.player.tick.impl.DataRefreshTicker;
import com.hawk.game.player.tick.impl.HealthCareTicker;
import com.hawk.game.player.tick.impl.HealthGameTicker;
import com.hawk.game.player.tick.impl.MonthCardTicker;
import com.hawk.game.player.tick.impl.NationalRedDotTicker;
import com.hawk.game.player.tick.impl.OilConsumeTicker;
import com.hawk.game.player.tick.impl.OnlineStateTicker;
import com.hawk.game.player.tick.impl.PlayerStrengthTicker;
import com.hawk.game.player.tick.impl.PrestressLossTicker;
import com.hawk.game.player.tick.impl.QQScoreBatchTicker;
import com.hawk.game.player.tick.impl.SilentTicker;
import com.hawk.game.player.tick.impl.VitTicker;
import com.hawk.game.player.tick.impl.ZeroEarningTicker;

/**
 * player tick逻辑枚举
 * 
 * @author lating
 *
 */
public enum PlayerTickEnum {
	/**
	 * 作用号检测
	 */
	BUFF_TICK(new BuffTicker()),
	/**
	 * 体力检测
	 */
	VIT_TICK(new VitTicker()),
	/**
	 * 玩家数据访问标识刷新
	 */
	DATA_REFRESH_TICK(new DataRefreshTicker()),
	/**
	 * 计算玩家战力
	 */
	STRENGTH_TICK(new PlayerStrengthTicker()),
	/**
	 * 国家小红点
	 */
	NATION_REDDOT_TICK(new NationalRedDotTicker()),
	/**
	 * 未成年关注检测
	 */
	HEALTH_CARE_TICK(new HealthCareTicker()),
	/**
	 * 健康游戏指引检测
	 */
	HEALTH_GAME_TICK(new HealthGameTicker()),
	/**
	 * 周卡月卡检测
	 */
	MONTH_CARD_TICK(new MonthCardTicker()),
	/**
	 * 兵力消耗石油检测
	 */
	OIL_CONSUME_TICK(new OilConsumeTicker()),
	/**
	 * 玩家在线状态检测
	 */
	ONLINE_TICK(new OnlineStateTicker()),
	/**
	 * 预流失干预活动
	 */
	PRESTRESS_LOSS_TICK(new PrestressLossTicker()),
	/**
	 * 手Q积分上报检测
	 */
	QQ_SCOREBATCH_TICK(new QQScoreBatchTicker()),
	/**
	 * 禁言检测
	 */
	SILENT_TICK(new SilentTicker()),
	/**
	 * 零收益检测
	 */
	ZERO_EARNING_TICK(new ZeroEarningTicker()),
	/**
	 * 核心勘探活动tick
	 */
	CORE_EXPLORE_TICK(new CoreExlopreActivityTicker()),
	;
	
	private PlayerTickLogic tickLogic;
	
	PlayerTickEnum(PlayerTickLogic tickLogic) {
		this.tickLogic = tickLogic;
	}
	
	public PlayerTickLogic getTickLogic() {
		return tickLogic;
	}
}
