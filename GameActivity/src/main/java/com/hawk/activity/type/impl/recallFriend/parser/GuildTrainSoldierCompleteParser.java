package com.hawk.activity.type.impl.recallFriend.parser;

import com.hawk.activity.event.impl.TrainSoldierCompleteEvent;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**造兵
 * @author hf
 */
public class GuildTrainSoldierCompleteParser extends IGuildAchieveParser<TrainSoldierCompleteEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RECALL_GUILD_TRAIN_SOLDIER_COMPLETE_NUM;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, TrainSoldierCompleteEvent event) {
		int soldierId = achieveConfig.getConditionValue(0);
		if (soldierId > 0 && event.getTrainId() != soldierId) {
			return false;
		}
		int num = event.getNum() + achieveItem.getValue(0);
		achieveItem.setValue(0, num);
		return true;
	}
}
