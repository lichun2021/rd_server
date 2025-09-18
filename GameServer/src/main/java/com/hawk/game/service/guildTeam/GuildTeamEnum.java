package com.hawk.game.service.guildTeam;

import com.hawk.game.protocol.GuildTeam.*;
import com.hawk.game.service.guildTeam.ipml.TBLYGuildTeamManager;
import com.hawk.game.service.guildTeam.ipml.XQHXGuildTeamManager;

public enum GuildTeamEnum {
    TBLY_WAR(GuildTeamType.TBLY_WAR, TBLYGuildTeamManager.getInstance()),
    XQHX_WAR(GuildTeamType.XQHX_WAR, XQHXGuildTeamManager.getInstance()),
    ;


    private GuildTeamType type;
    private GuildTeamManagerBase manager;

    GuildTeamEnum(GuildTeamType type, GuildTeamManagerBase manager){
        this.type = type;
        this.manager = manager;
    }

    public GuildTeamType getType() {
        return type;
    }

    public GuildTeamManagerBase getManager() {
        return manager;
    }
}
