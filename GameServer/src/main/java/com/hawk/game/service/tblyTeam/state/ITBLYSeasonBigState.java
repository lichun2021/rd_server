package com.hawk.game.service.tblyTeam.state;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar.*;

public interface ITBLYSeasonBigState {
    void enter();

    void tick();

    void leave();

    TLWBigStateInfo.Builder getStateInfo(Player player);
}
