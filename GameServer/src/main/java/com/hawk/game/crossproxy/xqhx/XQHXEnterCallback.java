package com.hawk.game.crossproxy.xqhx;

import com.hawk.game.crossproxy.CsRpcCallback;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Cross;
import com.hawk.game.protocol.Status;
import org.hawk.net.protocol.HawkProtocol;

public class XQHXEnterCallback extends CsRpcCallback {
    private Player player;

    public XQHXEnterCallback(Player player) {
        this.player = player;
    }


    @Override
    public int invoke(Object args) {
        HawkProtocol hawkProtocol = (HawkProtocol)args;
        Cross.RpcCommonResp rpcCommonResp = hawkProtocol.parseProtocol(Cross.RpcCommonResp.getDefaultInstance());
        Player.logger.info("xqhx  login_c resp  playerId:{}, errorCode:{}", player.getId(), rpcCommonResp.getErrorCode());
        if (rpcCommonResp.getErrorCode() != Status.SysError.SUCCESS_OK_VALUE) {
            XQHXCallbackOperationService.getInstance().onPrepareCrossFail(player);
        }
        return 0;
    }

    @Override
    public void onTimeout(Object args) {
        Player.logger.info("xqhx enter timeout playerId:{}", player.getId());
        XQHXCallbackOperationService.getInstance().onPrepareCrossFail(player);
    }
}
