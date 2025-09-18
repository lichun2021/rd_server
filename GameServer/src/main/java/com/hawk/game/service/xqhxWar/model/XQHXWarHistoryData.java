package com.hawk.game.service.xqhxWar.model;

import com.alibaba.fastjson.JSON;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.protocol.GuildTeam;
import com.hawk.game.service.guildTeam.model.GuildTeamData;
import com.hawk.game.service.xqhxWar.XQHXWarResidKey;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import java.util.ArrayList;
import java.util.List;

/**
 * 先驱回响历史战绩
 */
public class XQHXWarHistoryData {
    public String ownerId = "";//拥有者
    public String winnerId = "";//获胜者
    public long time;//时间

    public String idA = "";//战队id
    public String guildIdA = "" ;//联盟id
    public String guildNameA = "";//联盟名字
    public String guildTagA = "";//联盟简称
    public int guildFlagA;//联盟旗帜
    public String nameA = "";//战队名字
    public String serverIdA = "";//服务器
    public long battlePointA;//参战人员总战力
    public int memberCntA;//参战人员数量
    public long scoreA;//比赛积分


    public String idB = "";//战队id
    public String guildIdB = "" ;//联盟id
    public String guildNameB = "";//联盟名字
    public String guildTagB = "";//联盟简称
    public int guildFlagB ;//联盟旗帜
    public String nameB = "";//战队名字
    public String serverIdB = "";//服务器
    public long battlePointB;//参战人员总战力
    public int memberCntB;//参战人员数量
    public long scoreB;//比赛积分

    public XQHXWarHistoryData(){

    }

    public XQHXWarHistoryData(String ownerId, String winnerId, GuildTeamData teamDataA, GuildTeamData teamDataB){
        this.ownerId = ownerId;
        this.winnerId = winnerId;
        this.time = HawkTime.getMillisecond();

        this.idA = teamDataA.id;
        this.guildIdA = teamDataA.guildId;
        this.guildNameA = teamDataA.guildName;
        this.guildTagA = teamDataA.guildTag;
        this.guildFlagA = teamDataA.guildFlag;
        this.nameA = teamDataA.name;
        this.serverIdA = teamDataA.serverId;
        this.battlePointA = teamDataA.battlePoint;
        this.memberCntA = teamDataA.memberCnt;
        this.scoreA = teamDataA.score;

        this.idB = teamDataB.id;
        this.guildIdB = teamDataB.guildId;
        this.guildNameB = teamDataB.guildName;
        this.guildTagB = teamDataB.guildTag;
        this.guildFlagB = teamDataB.guildFlag;
        this.nameB = teamDataB.name;
        this.serverIdB = teamDataB.serverId;
        this.battlePointB = teamDataB.battlePoint;
        this.memberCntB = teamDataB.memberCnt;
        this.scoreB = teamDataB.score;
    }

    public GuildTeam.GuildTeamInfo.Builder toPB(){
        GuildTeam.GuildTeamInfo.Builder infoA = GuildTeam.GuildTeamInfo.newBuilder();
        infoA.setId(idA);
        infoA.setGuildId(guildIdA);
        infoA.setGuildName(guildNameA);
        infoA.setGuildTag(guildTagA);
        infoA.setGuildFlag(guildFlagA);
        infoA.setName(nameA);
        infoA.setServerId(serverIdA);
        infoA.setBattlePoint(battlePointA);
        infoA.setMemberCnt(memberCntA);
        infoA.setScore(scoreA);
        infoA.setIsWin(winnerId.equals(idA));
        infoA.setCreatTime(time);
        GuildTeam.GuildTeamInfo.Builder infoB = GuildTeam.GuildTeamInfo.newBuilder();
        infoB.setId(idB);
        infoB.setGuildId(guildIdB);
        infoB.setGuildName(guildNameB);
        infoB.setGuildTag(guildTagB);
        infoB.setGuildFlag(guildFlagB);
        infoB.setName(nameB);
        infoB.setServerId(serverIdB);
        infoB.setBattlePoint(battlePointB);
        infoB.setMemberCnt(memberCntB);
        infoB.setScore(scoreB);
        infoB.setIsWin(winnerId.equals(idB));
        infoB.setCreatTime(time);
        if(ownerId.equals(idA)){
            infoA.setOppTeam(infoB);
            return infoA;
        }else {
            infoB.setOppTeam(infoA);
            return infoB;
        }
    }


    public String serialize() {
        return JSON.toJSONString(this);
    }

    public static XQHXWarHistoryData unSerialize(String json) {
        if(HawkOSOperator.isEmptyString(json)){
            return null;
        }
        XQHXWarHistoryData historyData = JSON.parseObject(json, XQHXWarHistoryData.class);
        return historyData;
    }

    public void update(){
        String historyKey = String.format(XQHXWarResidKey.XQHX_WAR_HISTORY, ownerId);
        RedisProxy.getInstance().getRedisSession().lPush(historyKey, 0, serialize());
    }

    public static List<XQHXWarHistoryData> load(String teamId, int count){
        String historyKey = String.format(XQHXWarResidKey.XQHX_WAR_HISTORY, teamId);
        List<String> list = RedisProxy.getInstance().getRedisSession().lRange(historyKey, 0, count-1, 0);
        List<XQHXWarHistoryData> historyDataList = new ArrayList<>();
        if(list == null || list.isEmpty()){
            return historyDataList;
        }
        for(String json : list){
            XQHXWarHistoryData historyData = XQHXWarHistoryData.unSerialize(json);
            if(historyData == null){
                continue;
            }
            historyDataList.add(historyData);
        }
        return historyDataList;
    }
}
