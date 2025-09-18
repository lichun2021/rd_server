package com.hawk.game.module.lianmengyqzz.march.achieve;

import java.util.List;

import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData;
import com.hawk.game.module.lianmengyqzz.march.entitiy.YQZZAchievecomponent;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveType;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;

public class YQZZAchievParserCountryWinScore extends IYQZZAchievParser{

	@Override
	public YQZZAchieveType getYQZZAchieveType() {
		return YQZZAchieveType.COUNTRY_WIN_SOCRE;
	}

	@Override
	public boolean updateAchieveOnUpdate(YQZZAchievecomponent achieve,List<Long> conditionValueList,long targetValue) {
		String serverId = achieve.getParent().getParent().getParent().getMainServerId();
		YQZZBattleData data = YQZZMatchService.getInstance().getDataManger()
				.getBattleData();
		if(data == null){
			return false;
		}
		long curVal = data.getCountryScore(serverId);
		long achieveVal = achieve.getValue();
		if(curVal >  achieveVal){
			achieve.setValue(curVal);
			return true;
		}
		return false;
	}


}
