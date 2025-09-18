package com.hawk.game.service.tblyTeam.state.impl.season.big;

import com.hawk.game.GsApp;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar;
import com.hawk.game.protocol.TiberiumWar.*;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.state.ITBLYSeasonBigState;
import org.hawk.tuple.HawkTuple2;

public class TBLYSeasonBigNotOpenState implements ITBLYSeasonBigState {
    @Override
    public void enter() {

    }

    @Override
    public void tick() {
        int season = TBLYSeasonService.getInstance().getCurrSeason();
        if(season != -1){
            HawkTuple2<Long, Long> timeInfo = AssembleDataManager.getInstance().getTiberiumSeasonTime(season);
            if(timeInfo != null){
                long openTime = GsApp.getInstance().getServerOpenTime();
                if(openTime > timeInfo.first){
                    return;
                }
            }
            TBLYSeasonService.getInstance().onSeasonOpen(season);
        }
    }

    @Override
    public void leave() {

    }

    @Override
    public TLWBigStateInfo.Builder getStateInfo(Player player) {
        TiberiumWar.TLWBigStateInfo.Builder builder = TiberiumWar.TLWBigStateInfo.newBuilder();
        builder.setState(TLWBigState.TLW_BIG_NOT_OPEN);
        return builder;
    }
}
