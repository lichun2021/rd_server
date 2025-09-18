package com.hawk.game.invoker.xhjz;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.service.xhjzWar.XHJZWarService;
import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkException;

public class XHJZWarRefreshPowerInvoker extends HawkMsgInvoker {
    /** 玩家id*/
    private String playerId;
    /** 玩家战力*/
    private long power;

    public XHJZWarRefreshPowerInvoker(String playerId, long power) {
        this.playerId = playerId;
        this.power = power;
    }

    @Override
    public boolean onMessage(HawkAppObj hawkAppObj, HawkMsg hawkMsg) {
        Player player = GlobalData.getInstance().makesurePlayer(playerId);
        if(player == null || player.isCsPlayer()){
            return true;
        }
        try {
            XHJZWarService.getInstance().refreshPower(playerId, power);
        }catch (Exception e){
            HawkException.catchException(e);
        }
        return true;
    }
}
