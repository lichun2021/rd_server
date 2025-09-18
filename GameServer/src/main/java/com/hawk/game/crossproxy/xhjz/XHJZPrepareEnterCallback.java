package com.hawk.game.crossproxy.xhjz;

import com.hawk.game.crossproxy.CsRpcCallback;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Cross.XHJZCrossMsg;
import org.hawk.net.protocol.HawkProtocol;

public class XHJZPrepareEnterCallback extends CsRpcCallback {
    private Player player;
    private XHJZCrossMsg crossMsg;

    public XHJZPrepareEnterCallback(Player player, XHJZCrossMsg crossMsg) {
        this.player = player;
        this.crossMsg = crossMsg;
    }

    @Override
    public int invoke(Object args) {
        XHJZCallbackOperationService.getInstance().onPrepareCrossBack(player,(HawkProtocol)args, crossMsg);
        return 0;
    }


    @Override
    public void onTimeout(Object args) {
        Player.logger.info("xhjz prepare enter timeout playerId:{}", player.getId());
        XHJZCallbackOperationService.getInstance().onPrepareCrossFail(player);
    }
}
