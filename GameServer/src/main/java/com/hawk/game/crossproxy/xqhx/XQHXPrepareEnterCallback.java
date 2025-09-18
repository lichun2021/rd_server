package com.hawk.game.crossproxy.xqhx;

import com.hawk.game.crossproxy.CsRpcCallback;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Cross;
import org.hawk.net.protocol.HawkProtocol;

public class XQHXPrepareEnterCallback extends CsRpcCallback {
    private Player player;
    private Cross.XQHXCrossMsg crossMsg;

    public XQHXPrepareEnterCallback(Player player, Cross.XQHXCrossMsg crossMsg) {
        this.player = player;
        this.crossMsg = crossMsg;
    }

    @Override
    public int invoke(Object args) {
        XQHXCallbackOperationService.getInstance().onPrepareCrossBack(player,(HawkProtocol)args, crossMsg);
        return 0;
    }


    @Override
    public void onTimeout(Object args) {
        Player.logger.info("xqhx prepare enter timeout playerId:{}", player.getId());
        XQHXCallbackOperationService.getInstance().onPrepareCrossFail(player);
    }
}
