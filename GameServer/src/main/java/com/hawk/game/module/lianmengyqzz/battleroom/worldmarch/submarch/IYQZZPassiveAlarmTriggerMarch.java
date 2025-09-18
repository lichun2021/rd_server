package com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.submarch;

import com.hawk.game.world.march.IWorldMarch;

/** 玩家派遣出去的行军, 会导致自身闪红的. (支援,驻扎,采集)
 * 
 * @author lwt
 * @date 2017年10月6日 */
public interface IYQZZPassiveAlarmTriggerMarch extends IWorldMarch {
	/** 拉取警报 */
	void pullAttackReport();

	void pullAttackReport(String playerId);
}
