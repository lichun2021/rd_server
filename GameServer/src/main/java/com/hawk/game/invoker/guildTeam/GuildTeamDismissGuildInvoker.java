package com.hawk.game.invoker.guildTeam;

import com.hawk.game.service.guildTeam.GuildTeamService;
import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkException;

public class GuildTeamDismissGuildInvoker extends HawkMsgInvoker {
    private String guildId;


    public GuildTeamDismissGuildInvoker(String guildId) {
        this.guildId = guildId;
    }

    @Override
    public boolean onMessage(HawkAppObj hawkAppObj, HawkMsg hawkMsg) {
        try {
            GuildTeamService.getInstance().onGuildDismiss(guildId);
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
        return true;
    }
}
