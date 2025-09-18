package com.hawk.game.player.itemadd.impl;

import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.ActivityRewardsEvent;
import com.hawk.activity.type.impl.shareGlory.cfg.ShareGloryKVCfg;
import com.hawk.activity.type.impl.shareGlory.cfg.ShareGloryKVCfg.DonateItemType;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.itemadd.ItemAddLogic;
import com.hawk.log.Action;

public class ShareGloryDonateLogic implements ItemAddLogic {
	
	@Override
	public void addLogic(Player player, int itemId, int addCount, Action action) {
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		int itemType = itemCfg == null ? 0 : itemCfg.getItemType();
		ShareGloryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ShareGloryKVCfg.class);
		DonateItemType donateItemType = cfg.getEnergyType(action.intItemVal());
		if(donateItemType != DonateItemType.typeErr && action != Action.CELEBRATION_SHOP_EXCHANGE_REWARD){
			ActivityManager.getInstance().postEvent(new ActivityRewardsEvent(player.getId(), action.intItemVal(), itemId, itemType, addCount, donateItemType));
		}
	}
}
