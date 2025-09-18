package com.hawk.game.crossactivity.season.state;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkTime;

import com.hawk.game.GsApp;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.CrossSeasonTimeCfg;
import com.hawk.game.crossactivity.season.CrossSeaonStateData;
import com.hawk.game.crossactivity.season.CrossSeasonStateEnum;
import com.hawk.game.protocol.CrossActivity.CrossActivitySeasonState;

public class CrossSeasonStateHidden extends ICrossSeasonState{

	@Override
	public void start() {
		
	}

	@Override
	public void update() {
		int season = this.calNewSeason();
		if(season <= 0){
			return;
		}
		CrossSeaonStateData data = this.getStateData();
		if(data.getSeason() >= season){
			return;
		}
		data.setSeason(season);
		data.changeState(CrossSeasonStateEnum.SHOW);
	}
	
	@Override
	public CrossSeasonStateEnum getStateEnum() {
		return CrossSeasonStateEnum.HIDDEN;
	}

	
	@Override
	public CrossActivitySeasonState getCrossActivitySeasonState() {
		return CrossActivitySeasonState.C_SEASON_HIDDEN;
	}

	/**
     * 获取当前所在赛季
     * @return
     */
    public int calNewSeason() {
    	long now = HawkTime.getMillisecond();
    	long openTime = GsApp.getInstance().getServerOpenTime();
    	CrossConstCfg constCfg = CrossConstCfg.getInstance();
    	long timeLimit = openTime + constCfg.getServerDelayTime();
    	if(now < timeLimit){
    		return 0;
    	}
        ConfigIterator<CrossSeasonTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(CrossSeasonTimeCfg.class);
        for (CrossSeasonTimeCfg cfg : its) {
            long startTime = cfg.getShowTimeValue();
            long hiddenTime = cfg.getHiddenTimeValue();
            if (openTime >= cfg.getEndTimeValue()) {
                continue;
            }
            if (startTime < now && now < hiddenTime) {
                return cfg.getSeason();
            }
        }
        return 0;
    }

	

	
}
