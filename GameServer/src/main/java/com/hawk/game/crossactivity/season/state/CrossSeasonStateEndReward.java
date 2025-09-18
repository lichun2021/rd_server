package com.hawk.game.crossactivity.season.state;

import java.util.Objects;

import org.hawk.os.HawkTime;

import com.hawk.game.config.CrossSeasonTimeCfg;
import com.hawk.game.crossactivity.season.CrossActivitySeasonService;
import com.hawk.game.crossactivity.season.CrossSeasonScoreRank;
import com.hawk.game.crossactivity.season.CrossSeasonStateEnum;
import com.hawk.game.protocol.CrossActivity.CrossActivitySeasonState;

public class CrossSeasonStateEndReward extends ICrossSeasonState{
	
	
	@Override
	public void start() {
		this.sendFinalReward();
	}

	@Override
	public void update() {
		long curTime = HawkTime.getMillisecond();
		CrossSeasonTimeCfg timeCfg = this.getTimeCfg();
		if(curTime > timeCfg.getHiddenTimeValue()){
			//改变状态
			this.getStateData().changeState(CrossSeasonStateEnum.HIDDEN);
			return;
		}
	}

	@Override
	public CrossSeasonStateEnum getStateEnum() {
		return CrossSeasonStateEnum.REWARD;
	}
	
	
	@Override
	public CrossActivitySeasonState getCrossActivitySeasonState() {
		return CrossActivitySeasonState.C_SEASON_END;
	}
	
	
	/**
	 * 发放最终邮件
	 */
	public void sendFinalReward(){
		CrossSeasonScoreRank rank = CrossActivitySeasonService.getInstance().getRank();
		if(Objects.isNull(rank)){
			return;
		}
		rank.sendFinalRankReward();
	}

	

}
