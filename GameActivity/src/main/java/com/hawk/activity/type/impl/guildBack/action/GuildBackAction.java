package com.hawk.activity.type.impl.guildBack.action;

public class GuildBackAction {
    private String playerId = "";
    private GuildBackActionEnum opt = GuildBackActionEnum.UN_KNOW;
    private String guildId = "";
    private boolean isBack;
    private long count;
    private int baseLv;

    public GuildBackAction(String playerId, GuildBackActionEnum opt, String guildId) {
        this.playerId = playerId;
        this.opt = opt;
        this.guildId = guildId;
    }

    public GuildBackAction(String playerId, GuildBackActionEnum opt, String guildId, boolean isBack) {
        this.playerId = playerId;
        this.opt = opt;
        this.guildId = guildId;
        this.isBack = isBack;
    }

    public GuildBackAction(String playerId, GuildBackActionEnum opt, String guildId, boolean isBack, long count) {
        this.playerId = playerId;
        this.opt = opt;
        this.guildId = guildId;
        this.isBack = isBack;
        this.count = count;
    }

    public GuildBackAction(String playerId, GuildBackActionEnum opt, String guildId, boolean isBack, long count, int baseLv) {
        this.playerId = playerId;
        this.opt = opt;
        this.guildId = guildId;
        this.isBack = isBack;
        this.count = count;
        this.baseLv = baseLv;
    }

    public String getPlayerId() {
        return playerId;
    }

    public GuildBackActionEnum getOpt() {
        return opt;
    }

    public String getGuildId() {
        return guildId;
    }

    public boolean isBack() {
        return isBack;
    }

    public long getCount() {
        return count;
    }

    public int getBaseLv() {
        return baseLv;
    }

    @Override
    public String toString() {
        return "GuildBackAction{" +
                "playerId='" + playerId + '\'' +
                ", opt=" + opt +
                ", guildId='" + guildId + '\'' +
                ", isBack=" + isBack +
                ", count=" + count +
                ", baseLv=" + baseLv +
                '}';
    }
}
