package com.hawk.game.player.itemadd.impl;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.itemadd.ItemAddLogic;
import com.hawk.log.Action;

public class SendTimeLimitLogic implements ItemAddLogic {
	
	@Override
	public void addLogic(Player player, int itemId, int addCount, Action action) {
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		if (itemCfg == null || itemCfg.getProtectionPeriod() <= 0) {
			return;
		}
		// 赠送保护道具
		player.updateSendTimeLimitTool(itemId);
		player.getPush().syncDressSendProtectInfo();
	}
	
}
