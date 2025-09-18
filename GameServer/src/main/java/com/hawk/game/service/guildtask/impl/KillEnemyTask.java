package com.hawk.game.service.guildtask.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigManager;

import com.hawk.activity.event.impl.PvpBattleEvent;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.service.guildtask.GuildTask;
import com.hawk.game.service.guildtask.GuildTaskCfgItem;
import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskItem;
import com.hawk.game.service.guildtask.GuildTaskType;
import com.hawk.game.service.guildtask.event.KillEnemyTaskEvent;

/**
 * pvp战斗消灭敌军任务
 * 
 * @author jesse
 *
 */
@GuildTask(taskType = GuildTaskType.kill_enemy)
public class KillEnemyTask implements IGuildTask {

	@Override
	public <T extends GuildTaskEvent> boolean refreshTask(String guildId, T taskEvent, GuildTaskItem taskItem, GuildTaskCfgItem cfg) {
		KillEnemyTaskEvent event = (KillEnemyTaskEvent) taskEvent;
		PvpBattleEvent battleEvent = event.getBattleEvent();
		long disPower = 0;
		disPower += calcArmyPower(battleEvent.getArmyHurtMap());
		disPower += calcArmyPower(battleEvent.getArmyKillMap());
		long value = taskItem.getValue() + disPower;
		taskItem.setValue((int) Math.min(cfg.getValue(), value));
		return true;
	}

	private long calcArmyPower(Map<Integer, Integer> armyMap) {
		double totalPower = 0;
		for (Entry<Integer, Integer> entry : armyMap.entrySet()) {
			Integer armyId = entry.getKey();
			Integer cnt = entry.getValue();
			BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			if (soldierCfg == null) {
				continue;
			}
			totalPower += soldierCfg.getPower() * cnt;
		}
		return (long) Math.floor(totalPower);
	}

}
