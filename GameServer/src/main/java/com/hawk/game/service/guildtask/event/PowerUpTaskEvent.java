package com.hawk.game.service.guildtask.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskType;

/**
 * 战力增长事件
 * 
 * @author jesse
 *
 */
public class PowerUpTaskEvent extends GuildTaskEvent {

	/** 战力增长值 */
	int powerUp;

	public PowerUpTaskEvent(String guildId, int powerUp) {
		super(guildId);
		this.powerUp = powerUp;
	}

	public int getPowerUp() {
		return powerUp;
	}



	@Override
	public List<GuildTaskType> touchTasks() {
		List<GuildTaskType> touchTaskList = new ArrayList<GuildTaskType>();
		touchTaskList.add(GuildTaskType.battle_point_increase);
		return touchTaskList;
	}
}

