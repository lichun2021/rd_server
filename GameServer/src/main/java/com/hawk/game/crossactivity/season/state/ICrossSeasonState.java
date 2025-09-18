package com.hawk.game.crossactivity.season.state;

import java.util.Objects;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.CrossSeasonTimeCfg;
import com.hawk.game.crossactivity.season.CrossActivitySeasonService;
import com.hawk.game.crossactivity.season.CrossSeaonStateData;
import com.hawk.game.crossactivity.season.CrossSeasonStateEnum;
import com.hawk.game.protocol.CrossActivity.CrossActivitySeasonState;
import com.hawk.game.protocol.CrossActivity.CrossActivitySeasonStateData;

public abstract class ICrossSeasonState {
	
	
	
	public abstract void start();
	public abstract void update();
	public abstract CrossSeasonStateEnum getStateEnum();
	public abstract CrossActivitySeasonState getCrossActivitySeasonState();
	
	public void buildState(CrossActivitySeasonStateData.Builder builder){
		builder.setState(this.getCrossActivitySeasonState());
		CrossSeasonTimeCfg cfg = this.getTimeCfg();
		if(Objects.nonNull(cfg)){
			builder.setSeason(cfg.getSeason());
			builder.setShowTime(cfg.getShowTimeValue());
			builder.setOpenTime(cfg.getStartTimeValue());
			builder.setEndTime(cfg.getEndTimeValue());
			builder.setHiddenTime(cfg.getHiddenTimeValue());
		}
		
	}
	
	
	public CrossSeaonStateData getStateData(){
		return CrossActivitySeasonService.getInstance().getCrossSeaonStateData();
	}
	
	public CrossSeasonTimeCfg getTimeCfg(){
		CrossSeaonStateData data = this.getStateData();
		return HawkConfigManager.getInstance().getConfigByKey(CrossSeasonTimeCfg.class, data.getSeason());
	}
	
	
}
