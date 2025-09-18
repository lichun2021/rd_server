package com.hawk.game.service.tblyTeam.state.impl.season.big;

import com.hawk.game.config.TiberiumSeasonTimeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.state.ITBLYSeasonBigState;
import org.hawk.os.HawkTime;

public class TBLYSeasonBigFinalState implements ITBLYSeasonBigState {
	
	private long tickTime;
	
    @Override
    public void enter() {
        TBLYSeasonService.getInstance().onBigFinal();
    }

    @Override
    public void tick() {
        TiberiumSeasonTimeCfg cfg = TBLYSeasonService.getInstance().getFinalTimeCfg();
        if(cfg != null){
            long now = HawkTime.getMillisecond();
            if (now >= cfg.getSeasonEndShowTimeValue()) {
                TBLYSeasonService.getInstance().toBigNext();
            }
            //所有战斗结束以后，结算排行
            if(now - tickTime > HawkTime.MINUTE_MILLI_SECONDS){
            	this.tickTime = now;
            	if(now > cfg.getWarEndTimeValue() + HawkTime.MINUTE_MILLI_SECONDS * 5){
            		TBLYSeasonService.getInstance().calFinal();
            	}
            }
        }
        
    }

    @Override
    public void leave() {
    	//切状态的时候也检查一下
    	TBLYSeasonService.getInstance().calFinal();
    }

    @Override
    public TiberiumWar.TLWBigStateInfo.Builder getStateInfo(Player player) {
        TiberiumWar.TLWBigStateInfo.Builder builder = TiberiumWar.TLWBigStateInfo.newBuilder();
        builder.setState(TiberiumWar.TLWBigState.TLW_BIG_FINAL);
        return builder;
    }
}
