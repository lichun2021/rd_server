package com.hawk.game.service.tblyTeam.state;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar.*;

public interface ITBLYWarState {
    void enter();

    void tick();

    void leave();

    TWStateInfo.Builder getStateInfo(Player player);
}
