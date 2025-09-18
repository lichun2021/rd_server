package com.hawk.game.service.guildTeam.model;

import com.alibaba.fastjson.JSON;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildTeam.*;
import com.hawk.game.service.tblyTeam.TBLYWarResidKey;
import com.hawk.game.service.tiberium.TWPlayerData;
import org.hawk.os.HawkOSOperator;


public class GuildTeamPlayerData {
    public String id = "";
    public String name= "";
    public String serverId = "";
    public int icon;
    public String pfIcon = "";
    public int cityLevel;
    public String guildId = "";
    public String guildTag = "";
    public String teamId = "";
    public int guildAuth;
    public int auth;
    public int guildOfficer;
    public long battlePoint;
    public long enterTime;
    public long quitTIme;
    public boolean isMidwayQuit;


    public GuildTeamPlayerData(){

    }

    public GuildTeamPlayerData(Player player){
        this.id = player.getId();
        this.serverId = player.getMainServerId();
        this.auth = GuildTeamAuth.GT_NO_TEAM_VALUE;
        this.name = player.getName();
        this.icon = player.getIcon();
        this.pfIcon = player.getPfIcon();
        this.cityLevel = player.getCityLevel();
        this.guildId = player.getGuildId();
        this.guildTag = player.getGuildTag();
        this.battlePoint = player.getNoArmyPower();
    }

    public void refreshInfo(Player player, GuildMemberObject member){
        this.name = player.getName();
        this.icon = player.getIcon();
        this.pfIcon = player.getPfIcon();
        this.cityLevel = player.getCityLevel();
        this.guildId = player.getGuildId();
        this.guildTag = player.getGuildTag();
        this.battlePoint = player.getNoArmyPower();
        if (member != null && guildId.equals(member.getGuildId())) {
            this.guildAuth = member.getAuthority();
            this.guildOfficer = member.getOfficeId();
        }
    }

    public GuildTeamMemberInfo.Builder toPB(){
        GuildTeamMemberInfo.Builder info = GuildTeamMemberInfo.newBuilder();
        info.setId(id);
        info.setName(name);
        info.setServerId(serverId);
        info.setIcon(icon);
        info.setPfIcon(pfIcon);
        info.setGuildId(guildId);
        info.setGuildTag(guildTag);
        info.setTeamId(teamId);
        info.setAuth(guildAuth);
        info.setTeamAuth(GuildTeamAuth.valueOf(auth));
        info.setGuildOfficer(guildOfficer);
        info.setBattlePoint(battlePoint);
        return info;
    }

    public String serialize() {
        return JSON.toJSONString(this);
    }

    public static GuildTeamPlayerData unSerialize(String json) {
        if(HawkOSOperator.isEmptyString(json)){
            return null;
        }
        GuildTeamPlayerData playerData = JSON.parseObject(json, GuildTeamPlayerData.class);
        return playerData;
    }

    public TWPlayerData toTWPlayerData(){
        TWPlayerData twPlayerData = new TWPlayerData();
        twPlayerData.setName(name);
        twPlayerData.setId(id);
        twPlayerData.setCityLevel(cityLevel);
        twPlayerData.setIcon(icon);
        twPlayerData.setPfIcon(pfIcon);
        return twPlayerData;
    }
}
