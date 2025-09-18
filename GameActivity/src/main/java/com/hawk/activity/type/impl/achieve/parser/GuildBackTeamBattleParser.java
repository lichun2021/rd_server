package com.hawk.activity.type.impl.achieve.parser;

import com.hawk.activity.event.impl.GuildBackTeamBattleEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

public class GuildBackTeamBattleParser extends AchieveParser<GuildBackTeamBattleEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.GUILD_BACK_TEAM_BATTLE;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return true;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, GuildBackTeamBattleEvent event) {
        int configValue = achieveConfig.getConditionValue(0);
        int value = achieveData.getValue(0) + 1;
        if (value > configValue) {
            value = configValue;
        }
        achieveData.setValue(0, value);
        return true;
    }
}
