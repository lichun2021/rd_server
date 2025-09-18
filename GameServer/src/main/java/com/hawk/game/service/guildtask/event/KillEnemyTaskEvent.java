package com.hawk.game.service.guildtask.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.activity.event.impl.PvpBattleEvent;
import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskType;

/**
 * pvp战斗击杀敌军事件
 * 
 * @author jesse
 *
 */
public class KillEnemyTaskEvent extends GuildTaskEvent {

	/** pvp战斗事件*/
	PvpBattleEvent battleEvent;

	public KillEnemyTaskEvent(String guildId, PvpBattleEvent battleEvent) {
		super(guildId);
		this.battleEvent = battleEvent;
	}


	public PvpBattleEvent getBattleEvent() {
		return battleEvent;
	}


	@Override
	public List<GuildTaskType> touchTasks() {
		List<GuildTaskType> touchTaskList = new ArrayList<GuildTaskType>();
		touchTaskList.add(GuildTaskType.kill_enemy);
		return touchTaskList;
	}
}

