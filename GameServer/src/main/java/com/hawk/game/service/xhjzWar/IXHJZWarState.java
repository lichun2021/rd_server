package com.hawk.game.service.xhjzWar;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.XHJZWar;

public interface IXHJZWarState {
    void enter();

    void tick();

    void leave();

    XHJZWar.XWStateInfo.Builder getStateInfo(Player player);
}
