package com.hawk.game.crossactivity.season.state;

import java.util.Objects;

import org.hawk.os.HawkTime;

import com.hawk.game.config.CrossSeasonTimeCfg;
import com.hawk.game.crossactivity.season.CrossActivitySeasonConst;
import com.hawk.game.crossactivity.season.CrossActivitySeasonService;
import com.hawk.game.crossactivity.season.CrossSeasonScoreRank;
import com.hawk.game.crossactivity.season.CrossSeasonStateEnum;
import com.hawk.game.protocol.CrossActivity.CrossActivitySeasonState;

public class CrossSeasonStateEnd extends ICrossSeasonState{
	
	
	@Override
	public void start() {
		this.doFinalRankData();
	}

	@Override
	public void update() {
		long curTime = HawkTime.getMillisecond();
		CrossSeasonTimeCfg timeCfg = this.getTimeCfg();
		long endTime = timeCfg.getEndTimeValue() + HawkTime.MINUTE_MILLI_SECONDS * 5;
		endTime = Math.min(endTime, timeCfg.getHiddenTimeValue());
		if(curTime > endTime){
			//改变状态
			this.getStateData().changeState(CrossSeasonStateEnum.REWARD);
			return;
		}
		
	}

	@Override
	public CrossSeasonStateEnum getStateEnum() {
		return CrossSeasonStateEnum.END;
	}
	
	@Override
	public CrossActivitySeasonState getCrossActivitySeasonState() {
		return CrossActivitySeasonState.C_SEASON_END;
	}
	
	/**
	 * 确定最终榜
	 */
	public void doFinalRankData(){
		boolean take = CrossActivitySeasonService.getInstance().takeLock(CrossActivitySeasonConst.LockType.CROSS_SEASON_FINAL_RANK_SORT);
		if(!take){
			return;
		}
		CrossSeasonScoreRank rank = CrossActivitySeasonService.getInstance().getRank();
		if(Objects.isNull(rank)){
			return;
		}
		rank.makeSureFinalRank();
	}
	
	

	

}
