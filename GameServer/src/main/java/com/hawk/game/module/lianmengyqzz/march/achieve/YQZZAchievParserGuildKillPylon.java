package com.hawk.game.module.lianmengyqzz.march.achieve;

import java.util.List;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData;
import com.hawk.game.module.lianmengyqzz.march.entitiy.YQZZAchievecomponent;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveType;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;

public class YQZZAchievParserGuildKillPylon extends IYQZZAchievParser{

	@Override
	public YQZZAchieveType getYQZZAchieveType() {
		return YQZZAchieveType.GUILD_COLLECT_PYLON;
	}

	@Override
	public boolean updateAchieveOnUpdate(YQZZAchievecomponent achieve,List<Long> conditionValueList,long targetValue) {
		String guildId = achieve.getParent().getParent().getPlayerGuild();
		if(HawkOSOperator.isEmptyString(guildId)){
			return false;
		}
		YQZZBattleData data = YQZZMatchService.getInstance().getDataManger()
				.getBattleData();
		if(data == null){
			return false;
		}
		long curVal = data.getGuildKillPylon(guildId);
		long achieveVal = achieve.getValue();
		if(curVal >  achieveVal){
			achieve.setValue(curVal);
			return true;
		}
		return false;
	}


}
