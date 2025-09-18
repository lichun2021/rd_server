package com.hawk.game.invoker.guildTeam;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildTeam.*;
import com.hawk.game.service.guildTeam.GuildTeamService;
import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkException;

public class GuildTeamMemberManagerInvoker extends HawkMsgInvoker {
    /** 玩家id*/
    private String playerId;
    /** 操作参数*/
    GuildBattleMemberManagerReq req;

    public GuildTeamMemberManagerInvoker(String playerId, GuildBattleMemberManagerReq req) {
        this.playerId = playerId;
        this.req = req;
    }

    @Override
    public boolean onMessage(HawkAppObj hawkAppObj, HawkMsg hawkMsg) {
        Player player = GlobalData.getInstance().makesurePlayer(playerId);
        if(player == null || player.isCsPlayer()){
            return true;
        }
        try {
            GuildTeamService.getInstance().memberManager(player, req);
        } catch (Exception e) {
            HawkException.catchException(e);
        }
        return true;
    }
}
