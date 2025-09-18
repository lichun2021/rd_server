package com.hawk.game.service.xhjzWar;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.XHJZWar;

public interface IXHJZWarBattleState {
    void enter(int timeIndex);

    void tick(int timeIndex);

    void leave(int timeIndex);

    XHJZWar.XWStateInfo.Builder getStateInfo(Player player, int timeIndex);
}
