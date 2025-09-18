package com.hawk.game.service.tblyTeam.state;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar.*;

public interface ITBLYWarBattleState {
    void enter(int timeIndex);

    void tick(int timeIndex);

    void leave(int timeIndex);

    TWStateInfo.Builder  getStateInfo(Player player, int timeIndex);
}
