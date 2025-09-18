package com.hawk.game.invoker.xhjz;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.service.xhjzWar.XHJZWarService;
import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkException;

public class XHJZWarChangeNameMsgInvoker extends HawkMsgInvoker {
    /** 玩家id*/
    private String playerId;
    /** 玩家名字*/
    private String playerName;

    public XHJZWarChangeNameMsgInvoker(String playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }

    @Override
    public boolean onMessage(HawkAppObj hawkAppObj, HawkMsg hawkMsg) {
        Player player = GlobalData.getInstance().makesurePlayer(playerId);
        if(player == null || player.isCsPlayer()){
            return true;
        }
        try {
            XHJZWarService.getInstance().refreshName(playerId, playerName);
        }catch (Exception e){
            HawkException.catchException(e);
        }
        return true;
    }
}
