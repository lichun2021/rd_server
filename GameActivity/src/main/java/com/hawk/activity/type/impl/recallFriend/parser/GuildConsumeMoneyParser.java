package com.hawk.activity.type.impl.recallFriend.parser;

import com.hawk.activity.event.impl.ConsumeMoneyEvent;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/** 联盟成员消耗资源
 * @author hf
 */
public class GuildConsumeMoneyParser extends IGuildAchieveParser<ConsumeMoneyEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RECALL_GUILD_CONSUME_MONEY;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, ConsumeMoneyEvent event) {
		int resType = achieveConfig.getConditionValue(0);
		if (resType != event.getResType()) {
			return false;
		}
		int value = achieveItem.getValue(0) + (int) event.getNum();
		achieveItem.setValue(0, value);
		return true;
	}
}
