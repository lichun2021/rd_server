package com.hawk.game.service.tblyTeam.state.impl.season.big;

import com.hawk.game.config.TiberiumSeasonTimeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.state.ITBLYSeasonBigState;
import org.hawk.os.HawkTime;

import java.util.concurrent.TimeUnit;

public class TBLYSeasonBigGroupWaitState implements ITBLYSeasonBigState {
    @Override
    public void enter() {
        TBLYSeasonService.getInstance().onBigGroupWait();
    }

    @Override
    public void tick() {
        TiberiumSeasonTimeCfg cfg = TBLYSeasonService.getInstance().getFirstTimeCfg();
        if(cfg != null){
            long now = HawkTime.getMillisecond();
            if (now >= cfg.getMatchStartTimeValue() + TimeUnit.MINUTES.toMillis(10)) {
                TBLYSeasonService.getInstance().toBigNext();
            }
        }
    }

    @Override
    public void leave() {

    }

    @Override
    public TiberiumWar.TLWBigStateInfo.Builder getStateInfo(Player player) {
        TiberiumWar.TLWBigStateInfo.Builder builder = TiberiumWar.TLWBigStateInfo.newBuilder();
        builder.setState(TiberiumWar.TLWBigState.TLW_BIG_GROUP);
        return builder;
    }
}
