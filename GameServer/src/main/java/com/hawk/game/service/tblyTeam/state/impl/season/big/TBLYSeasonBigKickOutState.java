package com.hawk.game.service.tblyTeam.state.impl.season.big;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.state.ITBLYSeasonBigState;

public class TBLYSeasonBigKickOutState implements ITBLYSeasonBigState {
    @Override
    public void enter() {
        TBLYSeasonService.getInstance().onBigKickOut();
    }

    @Override
    public void tick() {

    }

    @Override
    public void leave() {

    }

    @Override
    public TiberiumWar.TLWBigStateInfo.Builder getStateInfo(Player player) {
        TiberiumWar.TLWBigStateInfo.Builder builder = TiberiumWar.TLWBigStateInfo.newBuilder();
        builder.setState(TiberiumWar.TLWBigState.TLW_BIG_KICKOUT);
        return builder;
    }
}
