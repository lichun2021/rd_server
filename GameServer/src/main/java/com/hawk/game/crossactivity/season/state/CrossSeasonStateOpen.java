package com.hawk.game.crossactivity.season.state;

import org.hawk.os.HawkTime;

import com.hawk.game.config.CrossSeasonTimeCfg;
import com.hawk.game.crossactivity.season.CrossSeasonStateEnum;
import com.hawk.game.protocol.CrossActivity.CrossActivitySeasonState;

public class CrossSeasonStateOpen extends ICrossSeasonState{

	@Override
	public void start() {
		
	}

	@Override
	public void update() {
		long curTime = HawkTime.getMillisecond();
		CrossSeasonTimeCfg timeCfg = this.getTimeCfg();
		if(curTime > timeCfg.getEndTimeValue()){
			//改变状态
			this.getStateData().changeState(CrossSeasonStateEnum.END);
			return;
		}
	}

	@Override
	public CrossSeasonStateEnum getStateEnum() {
		return CrossSeasonStateEnum.OPEN;
	}

	
	@Override
	public CrossActivitySeasonState getCrossActivitySeasonState() {
		return CrossActivitySeasonState.C_SEASON_OPEN;
	}
	
	public void initSeasonScore(){
		
	}
}
