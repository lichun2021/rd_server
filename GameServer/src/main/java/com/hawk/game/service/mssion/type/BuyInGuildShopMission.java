package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventBuyInGuildShop;

/**
 * 联盟商店购买{1}道具{2}次
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_BUY_ITEM_IN_GUILD_SHOP_TIMES)
public class BuyInGuildShopMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventBuyInGuildShop event = (EventBuyInGuildShop)missionEvent;
		int conditionItemid = cfg.getIds().get(0);
		if (conditionItemid != 0 && event.getItemId() != conditionItemid) {
			return;
		}
		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}
}
