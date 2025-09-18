package com.hawk.game.module.homeland.rank;


public class HomeLandRank implements Comparable<HomeLandRank> {

    private String id;

    private int rank;

    private long score;

    private String guildId;

    private String serverId;

    private long rankTime;

    public static HomeLandRank valueOf(String playerId, String guildId, String serverId, long score) {
        HomeLandRank scoreRank = new HomeLandRank();
        scoreRank.setId(playerId);
        scoreRank.setGuildId(guildId);
        scoreRank.setScore(score);
        scoreRank.setServerId(serverId);
        return scoreRank;
    }
    public static HomeLandRank valueOf(String playerId, String guildId, String serverId) {
        HomeLandRank scoreRank = new HomeLandRank();
        scoreRank.setId(playerId);
        scoreRank.setGuildId(guildId);
        scoreRank.setServerId(serverId);
        return scoreRank;
    }
    public static HomeLandRank valueOf(String playerId,long score) {
        HomeLandRank scoreRank = new HomeLandRank();
        scoreRank.setScore(score);
        scoreRank.setId(playerId);
        return scoreRank;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public long getRankTime() {
        return rankTime;
    }

    public void setRankTime(long rankTime) {
        this.rankTime = rankTime;
    }

    @Override
    public int compareTo(HomeLandRank arg0) {
        return 0;
    }


}
