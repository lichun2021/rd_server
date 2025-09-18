package com.hawk.activity.type.impl.recallFriend.parser;

import com.hawk.activity.event.impl.VitreceiveConsumeEvent;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**体力消耗
 * @author hf
 */
public class GuildVitreceiveConsumeParser extends IGuildAchieveParser<VitreceiveConsumeEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RECALL_GUILD_VITRECEIVE_CONSUME;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, VitreceiveConsumeEvent event) {
		int value = event.getConsumeValue() + achieveItem.getValue(0);
		achieveItem.setValue(0, value);
		return true;
	}
}
