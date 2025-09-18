package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 联盟商店购买物品
 * @author golden
 *
 */
public class EventBuyInGuildShop extends MissionEvent {

	int itemId;
	
	int count;
	
	public EventBuyInGuildShop(int itemId, int count) {
		super();
		this.itemId = itemId;
		this.count = count;
	}

	public int getItemId() {
		return itemId;
	}

	public int getCount() {
		return count;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_BUY_ITEM_IN_GUILD_SHOP_TIMES);
		return touchMissionList;
	}
}
