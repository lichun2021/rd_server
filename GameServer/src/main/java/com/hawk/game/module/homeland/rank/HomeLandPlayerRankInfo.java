package com.hawk.game.module.homeland.rank;

import java.util.List;

import com.hawk.game.protocol.HomeLand;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 家园玩家信息
 *
 * @author Golden
 */
public class HomeLandPlayerRankInfo implements SplitEntity {

    private String playerId;

    private String playerName;

    private String guildTag;

    private int icon;

    private String pfIcon;

    private String serverId;

    private String guildName;

    @Override
    public SplitEntity newInstance() {
        return new HomeLandPlayerRankInfo();
    }

    @Override
    public void serializeData(List<Object> dataList) {
        dataList.add(playerId);
        dataList.add(playerName);
        dataList.add(guildTag);
        dataList.add(icon);
        dataList.add(pfIcon);
        dataList.add(serverId);
        dataList.add(guildName);
    }

    @Override
    public void fullData(DataArray dataArray) {
        dataArray.setSize(7);
        playerId = dataArray.getString();
        playerName = dataArray.getString();
        guildTag = dataArray.getString();
        icon = dataArray.getInt();
        pfIcon = dataArray.getString();
        serverId = dataArray.getString();
        guildName = dataArray.getString();
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getGuildTag() {
        return guildTag;
    }

    public void setGuildTag(String guildTag) {
        this.guildTag = guildTag;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getPfIcon() {
        return pfIcon;
    }

    public void setPfIcon(String pfIcon) {
        this.pfIcon = pfIcon;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getGuildName() {
        return guildName;
    }

    public void setGuildName(String guildName) {
        this.guildName = guildName;
    }
    public HomeLand.HomeLandRankMsg buildRankInfo(HomeLandRank homeLandRank) {
        HomeLand.HomeLandRankMsg.Builder rankInfo = HomeLand.HomeLandRankMsg.newBuilder();
        rankInfo.setPlayerId(homeLandRank.getId());
        rankInfo.setPlayerName(getPlayerName());
        rankInfo.setIcon(getIcon());
        rankInfo.setPfIcon(getPfIcon());
        rankInfo.setGuildTag(getGuildTag());
        rankInfo.setGuildName(getGuildName());
        rankInfo.setRank(homeLandRank.getRank());
        rankInfo.setScore(homeLandRank.getScore());
        rankInfo.setServerId(getServerId());
        return rankInfo.build();
    }
}
