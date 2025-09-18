package com.hawk.game.service.xhjzWar;

import com.alibaba.fastjson.JSON;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.XHJZWar;
import org.hawk.os.HawkOSOperator;

public class XHJZWarPlayerData {
    public String id = "";
    public String name= "";
    public String serverId = "";
    public int icon;
    public String pfIcon = "";
    public String guildId = "";
    public String guildTag = "";
    public String teamId = "";
    public int guildAuth;
    public int auth;
    public int guildOfficer;
    public long battlePoint;
    public long enterTime;
    public long quitTIme;

    public XHJZWarPlayerData(){

    }

    public XHJZWarPlayerData(Player player){
        this.id = player.getId();
        this.serverId = player.getMainServerId();
        this.auth = XHJZWar.XWPlayerAuth.XW_NO_TEAM_VALUE;
        this.name = player.getName();
        this.icon = player.getIcon();
        this.pfIcon = player.getPfIcon();
        this.guildId = player.getGuildId();
        this.guildTag = player.getGuildTag();
        this.battlePoint = player.getPower();
    }

    public void refreshInfo(Player player, GuildMemberObject member){
        this.name = player.getName();
        this.icon = player.getIcon();
        this.pfIcon = player.getPfIcon();
        this.guildId = player.getGuildId();
        this.guildTag = player.getGuildTag();
        this.battlePoint = player.getPower();
        if (member != null && guildId.equals(member.getGuildId())) {
           this.guildAuth = member.getAuthority();
           this.guildOfficer = member.getOfficeId();
        }
    }

    public XHJZWar.XWPlayerInfo.Builder toPB(){
        XHJZWar.XWPlayerInfo.Builder info = XHJZWar.XWPlayerInfo.newBuilder();
        info.setId(id);
        info.setName(name);
        info.setServerId(serverId);
        info.setIcon(icon);
        info.setPfIcon(pfIcon);
        info.setGuildId(guildId);
        info.setGuildTag(guildTag);
        info.setTeamId(teamId);
        info.setAuth(guildAuth);
        info.setTeamAuth(XHJZWar.XWPlayerAuth.valueOf(auth));
        info.setGuildOfficer(guildOfficer);
        info.setBattlePoint(battlePoint);
        return info;
    }

    public String serialize() {
        return JSON.toJSONString(this);
    }

    public static XHJZWarPlayerData unSerialize(String json) {
        if(HawkOSOperator.isEmptyString(json)){
            return null;
        }
        XHJZWarPlayerData playerData = JSON.parseObject(json, XHJZWarPlayerData.class);
        return playerData;
    }

    public static XHJZWarPlayerData load(String playerId){
        String json = RedisProxy.getInstance().getRedisSession().hGet(XHJZRedisKey.XHJZ_WAR_PLAYER, playerId);
        return unSerialize(json);
    }

    public void update(){
        RedisProxy.getInstance().getRedisSession().hSet(XHJZRedisKey.XHJZ_WAR_PLAYER, id, serialize());
    }
}
