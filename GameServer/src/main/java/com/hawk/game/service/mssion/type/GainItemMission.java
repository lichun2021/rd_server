package com.hawk.game.service.mssion.type;

import java.util.List;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventGainItem;

@Mission(missionType = MissionType.GAIN_ITEM)
public class GainItemMission implements IMission {
	
	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventGainItem event = (EventGainItem) missionEvent;
		
		List<Integer> conditions = cfg.getIds();
		if (conditions.get(0) != 0 && !conditions.contains(event.getItemId())) {
			return;
		}
		
		entityItem.addValue(event.getCount());
		checkMissionFinish(entityItem, cfg);
	}

	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int itemId = cfg.getIds().get(0);
		String countStr = RedisProxy.getInstance().getRedisSession().hGet("GainItemTotal:" + playerData.getPlayerId(), String.valueOf(itemId));
		if (!HawkOSOperator.isEmptyString(countStr)) {
			int count = Integer.parseInt(countStr);
			entityItem.addValue(count);
			checkMissionFinish(entityItem, cfg);
		}
	}
	
	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}
	
}
