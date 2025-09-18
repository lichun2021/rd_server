package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.SendFriendsGiftsEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class SendGfitToFriendsParser extends AchieveParser<SendFriendsGiftsEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.SEND_GFIT_TO_FRIENDS;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, SendFriendsGiftsEvent event) {
		
		int configValue = achieveConfig.getConditionValue(0);
		int count = 0;
		if(event.getFriends()!= null){
			count = event.getFriends().size();
		}
		int value = achieveItem.getValue(0) + count;
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}
