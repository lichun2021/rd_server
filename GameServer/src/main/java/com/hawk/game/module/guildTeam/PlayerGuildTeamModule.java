package com.hawk.game.module.guildTeam;

import com.hawk.game.invoker.guildTeam.GuildTeamMemberManagerInvoker;
import com.hawk.game.invoker.guildTeam.GuildTeamTeamManagerInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.GuildManager;
import com.hawk.game.protocol.GuildTeam.*;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.guildTeam.GuildTeamService;
import com.hawk.gamelib.GameConst;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerGuildTeamModule extends PlayerModule {
    static Logger logger = LoggerFactory.getLogger("Server");
    /**
     * 构造函数
     *
     * @param player
     */
    public PlayerGuildTeamModule(Player player) {
        super(player);
    }

    @ProtocolHandler(code = HP.code2.GUILD_BATTLE_TEAM_MANAGER_REQ_VALUE)
    public void teamManager(HawkProtocol hawkProtocol) {
        GuildBattleTeamManagerReq req = hawkProtocol.parseProtocol(GuildBattleTeamManagerReq.getDefaultInstance());
        //没有联盟禁止操作，返回错误码
        String guildId = player.getGuildId();
        if (HawkOSOperator.isEmptyString(guildId)) {
            sendError(hawkProtocol.getType(), Status.Error.GUILD_NO_JOIN_VALUE);
            return;
        }
        //权限不足，返回错误码，这里代码有迷惑性，因为直接用了泰伯的权限，赛博也是直接用的，那我也直接用了
        if (!GuildService.getInstance().checkGuildAuthority(player.getId(), GuildManager.AuthId.TIBERIUM_WAR_MANGER)) {
            sendError(hawkProtocol.getType(), Status.Error.GUILD_LOW_AUTHORITY_VALUE);
            return;
        }
        if(!GuildTeamService.getInstance().checkTeamManager(player, req)){
            return;
        }
        if(!GuildTeamService.getInstance().checkTeamManagerCost(player, req)){
            return;
        }
        GuildTeamService.getInstance().dealMsg(GameConst.MsgId.GUILD_TEAM_TEAM_MANAGER,
                        new GuildTeamTeamManagerInvoker(player.getId(), req));
    }

    @ProtocolHandler(code = HP.code2.GUILD_BATTLE_MEMBER_MANAGER_REQ_VALUE)
    public void memberManager(HawkProtocol hawkProtocol) {
        GuildBattleMemberManagerReq req = hawkProtocol.parseProtocol(GuildBattleMemberManagerReq.getDefaultInstance());
        //没有联盟禁止操作，返回错误码
        String guildId = player.getGuildId();
        if (HawkOSOperator.isEmptyString(guildId)) {
            sendError(hawkProtocol.getType(), Status.Error.GUILD_NO_JOIN_VALUE);
            return;
        }
        //权限不足，返回错误码，这里代码有迷惑性，因为直接用了泰伯的权限，赛博也是直接用的，那我也直接用了
        if (!GuildService.getInstance().checkGuildAuthority(player.getId(), GuildManager.AuthId.TIBERIUM_WAR_MANGER)) {
            sendError(hawkProtocol.getType(), Status.Error.GUILD_LOW_AUTHORITY_VALUE);
            return;
        }
        if(!GuildTeamService.getInstance().checkMemberManager(player, req)){
            return;
        }
        GuildTeamService.getInstance().dealMsg(GameConst.MsgId.GUILD_TEAM_MEMBER_MANAGER,
                new GuildTeamMemberManagerInvoker(player.getId(), req));
    }

    @ProtocolHandler(code = HP.code2.GUILD_BATTLE_TEAM_LIST_REQ_VALUE)
    public void teamList(HawkProtocol hawkProtocol) {
        GuildBattleTeamListReq req = hawkProtocol.parseProtocol(GuildBattleTeamListReq.getDefaultInstance());
        GuildTeamService.getInstance().teamList(player, req);
    }
    @ProtocolHandler(code = HP.code2.GUILD_BATTLE_MEMBER_LIST_REQ_VALUE)
    public void memberList(HawkProtocol hawkProtocol) {
        GuildBattleMemberListReq req = hawkProtocol.parseProtocol(GuildBattleMemberListReq.getDefaultInstance());
        GuildTeamService.getInstance().memberList(player, req);
    }
}

