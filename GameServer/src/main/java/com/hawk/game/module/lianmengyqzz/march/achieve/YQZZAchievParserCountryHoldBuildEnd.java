package com.hawk.game.module.lianmengyqzz.march.achieve;

import java.util.List;

import org.hawk.os.HawkTime;

import com.hawk.game.GsApp;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZTimeCfg;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData;
import com.hawk.game.module.lianmengyqzz.march.entitiy.YQZZAchievecomponent;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveType;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;

public class YQZZAchievParserCountryHoldBuildEnd extends IYQZZAchievParser{

	@Override
	public YQZZAchieveType getYQZZAchieveType() {
		return YQZZAchieveType.COUNTRY_HOLD_BUILDING;
	}

	@Override
	public boolean updateAchieveOnUpdate(YQZZAchievecomponent achieve,List<Long> conditionValueList,long targetValue) {
		String serverId = achieve.getParent().getParent().getParent().getMainServerId();
		YQZZBattleData data = YQZZMatchService.getInstance().getDataManger()
				.getBattleData();
		if(data == null){
			return false;
		}
		int total = 0;
		for(long btype : conditionValueList){
			int bcount = data.getServerHoldBuildsById(serverId,(int)btype);
			total += bcount;
		}
		int achieveVal = (int) achieve.getValue();
		if(total != achieveVal){
			achieve.setValue(total);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean achieveFinish(YQZZAchievecomponent achieve,List<Long> conditionValueList,long targetValue) {
		YQZZTimeCfg timeCfg = YQZZMatchService.getInstance().getState().getTimeCfg();
		if(timeCfg == null){
			return false;
		}
		long time = GsApp.getInstance().getCurrentTime();
		if(time < timeCfg.getRewardTimeValue() + HawkTime.MINUTE_MILLI_SECONDS * 3){
			return false;
		}
		long achieveVal = achieve.getValue();
		if(achieveVal >= targetValue){
			achieve.setValue(targetValue);
			achieve.setState(YQZZAchieveState.FINISH.getValue());
			return true;
		}
		return false;
	}

	

}
