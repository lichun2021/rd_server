package com.hawk.game.module.lianmengyqzz.march.achieve;

import java.util.List;

import org.hawk.os.HawkTime;

import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData;
import com.hawk.game.module.lianmengyqzz.march.entitiy.YQZZAchievecomponent;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveType;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;

public class YQZZAchievParserPlayerInBuildTime extends IYQZZAchievParser{

	@Override
	public YQZZAchieveType getYQZZAchieveType() {
		return YQZZAchieveType.PLAYER_IN_BUILDING_TIME;
	}

	@Override
	public boolean updateAchieveOnUpdate(YQZZAchievecomponent achieve,List<Long> conditionValueList,long targetValue) {
		YQZZBattleData data = YQZZMatchService.getInstance().getDataManger()
				.getBattleData();
		if(data == null){
			return false;
		}
		String playerId = achieve.getParent().getParent().getPlayerId();
		long curVal = 0;
		for(long configVal : conditionValueList){
			long inTime = data.getPlayerInBuildTimeByType(playerId,(int)configVal);
			curVal += inTime;
		}
		curVal = curVal / HawkTime.MINUTE_MILLI_SECONDS;
		long achieveVal = achieve.getValue();
		if(curVal >  achieveVal){
			achieve.setValue(curVal);
			return true;
		}
		return false;
	}
	
	
}
