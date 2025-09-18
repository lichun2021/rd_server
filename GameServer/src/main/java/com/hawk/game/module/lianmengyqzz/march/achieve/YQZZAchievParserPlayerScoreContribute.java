package com.hawk.game.module.lianmengyqzz.march.achieve;

import java.util.List;

import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData;
import com.hawk.game.module.lianmengyqzz.march.entitiy.YQZZAchievecomponent;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveType;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;

public class YQZZAchievParserPlayerScoreContribute extends IYQZZAchievParser{

	@Override
	public YQZZAchieveType getYQZZAchieveType() {
		return YQZZAchieveType.PLAYER_CONTRIBUTE;
	}

	@Override
	public boolean updateAchieveOnUpdate(YQZZAchievecomponent achieve,List<Long> conditionValueList,long targetValue) {
		YQZZBattleData data = YQZZMatchService.getInstance().getDataManger()
				.getBattleData();
		if(data == null){
			return false;
		}
		String playerId = achieve.getParent().getParent().getPlayerId();
		long curVal = data.getPlayerScore(playerId);
		long achieveVal = achieve.getValue();
		if(curVal >  achieveVal){
			achieve.setValue(curVal);
			return true;
		}
		return false;
	}

}
