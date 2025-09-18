package com.hawk.game.player.itemadd.impl;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.player.itemadd.ItemAddLogic;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventGainItem;
import com.hawk.log.Action;

public class RecordMissionLogic implements ItemAddLogic {
	
	@Override
	public void addLogic(Player player, int itemId, int addCount, Action action) {
		if (!ConstProperty.getInstance().getRecordItemList().contains(itemId)) {
			return;
		}
		RedisProxy.getInstance().getRedisSession().hIncrBy("GainItemTotal:" + player.getId(), String.valueOf(itemId), addCount);
		MissionManager.getInstance().postMsg(player, new EventGainItem(itemId, addCount));
	}
	
}
