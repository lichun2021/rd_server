package com.hawk.game.invoker.xhjz;

import com.hawk.game.service.xhjzWar.XHJZWarService;
import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkException;

public class XHJZWarDismissGuildInvoker extends HawkMsgInvoker {
    private String guildId;

    public XHJZWarDismissGuildInvoker(String guildId) {
        this.guildId = guildId;
    }

    @Override
    public boolean onMessage(HawkAppObj hawkAppObj, HawkMsg hawkMsg) {
        try {
            XHJZWarService.getInstance().onGuildDismiss(guildId);
        }catch (Exception e){
            HawkException.catchException(e);
        }
        return true;
    }
}
