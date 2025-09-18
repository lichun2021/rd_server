package com.hawk.activity.type.impl.recallFriend.parser;

import com.hawk.activity.event.impl.GuildDonateEvent;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**联盟捐献
 * @author hf
 */
public class GuildDonateParser extends IGuildAchieveParser<GuildDonateEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.RECALL_GUILD_GUILD_DONATE;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, GuildDonateEvent event) {
		achieveItem.setValue(0, achieveItem.getValue(0) + 1);
		return true;
	}
}
