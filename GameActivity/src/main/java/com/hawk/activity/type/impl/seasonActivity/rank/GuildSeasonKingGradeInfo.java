package com.hawk.activity.type.impl.seasonActivity.rank;

import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

import java.util.List;

/**
 * 联盟段位王者段位联盟信息
 */
public class GuildSeasonKingGradeInfo implements SplitEntity {
    //联盟id
    private String guildId;
    //联盟名字
    private String guildName;
    //联盟简称
    private String guildTag;
    //联盟旗帜
    private int guildFlag;
    //盟主名字
    private String guildLeader;
    //联盟服务器id
    private String serverId;

    //实例化
    @Override
    public SplitEntity newInstance() {
        return new GuildSeasonKingGradeRank();
    }

    //序列化
    @Override
    public void serializeData(List<Object> dataList) {
        dataList.add(guildId);
        dataList.add(guildName == null ? "" : guildName);
        dataList.add(guildTag == null ? "" : guildTag);
        dataList.add(guildFlag);
        dataList.add(guildLeader == null ? "" : guildLeader);
        dataList.add(serverId == null ? "" : serverId);
    }

    //解析
    @Override
    public void fullData(DataArray dataArray) {
        dataArray.setSize(6);
        guildId = dataArray.getString();
        guildName = dataArray.getString();
        guildTag = dataArray.getString();
        guildFlag = dataArray.getInt();
        guildLeader = dataArray.getString();
        serverId = dataArray.getString();
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getGuildName() {
        return guildName;
    }

    public void setGuildName(String guildName) {
        this.guildName = guildName;
    }

    public String getGuildTag() {
        return guildTag;
    }

    public void setGuildTag(String guildTag) {
        this.guildTag = guildTag;
    }

    public int getGuildFlag() {
        return guildFlag;
    }

    public void setGuildFlag(int guildFlag) {
        this.guildFlag = guildFlag;
    }

    public String getGuildLeader() {
        return guildLeader;
    }

    public void setGuildLeader(String guildLeader) {
        this.guildLeader = guildLeader;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
}
