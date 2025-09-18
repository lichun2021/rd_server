package com.hawk.game.module.lianmengyqzz.march.achieve;

import java.util.List;
import java.util.Set;

import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData;
import com.hawk.game.module.lianmengyqzz.march.entitiy.YQZZAchievecomponent;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveType;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;


public class YQZZAchievParserGuildOccupyBuild extends IYQZZAchievParser{

	@Override
	public YQZZAchieveType getYQZZAchieveType() {
		return YQZZAchieveType.GUILD_OCCUPY_BUILDING;
	}

	@Override
	public boolean updateAchieveOnUpdate(YQZZAchievecomponent achieve,List<Long> conditionValueList,long targetValue) {
		String guildId = achieve.getParent().getParent().getPlayerGuild();
		YQZZBattleData data = YQZZMatchService.getInstance().getDataManger()
				.getBattleData();
		if(data == null){
			return false;
		}
		Set<Integer> set = data.getGuildOccupyBuilds(guildId);
		int curVal = set.contains((int)targetValue)?1:0;
		long achieveVal = achieve.getValue();
		if(curVal > achieveVal){
			achieve.setValue(curVal);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean achieveFinish(YQZZAchievecomponent achieve,List<Long> conditionValueList,long targetValue) {
		long achieveVal = achieve.getValue();
		if(achieveVal > 0){
			achieve.setValue(1);
			achieve.setState(YQZZAchieveState.FINISH.getValue());
			return true;
		}
		return false;
	}


}
