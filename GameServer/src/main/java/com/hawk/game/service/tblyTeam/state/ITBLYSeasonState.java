package com.hawk.game.service.tblyTeam.state;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar.*;

public interface ITBLYSeasonState {
    void enter();

    void tick();

    void leave();

    TLWStateInfo.Builder getStateInfo(Player player);
}
