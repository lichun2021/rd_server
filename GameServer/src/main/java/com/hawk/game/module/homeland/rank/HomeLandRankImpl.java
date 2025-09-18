package com.hawk.game.module.homeland.rank;

import com.hawk.game.protocol.HomeLand;

import java.util.List;

/**
 * 家园榜
 *
 * @author zhy
 */
public interface HomeLandRankImpl {
    void doRankSort();

    long getLastRankRefreshTime();

    void setLastRankRefreshTime(long time);

    int getRefreshInterval();

    void insertIntoRank(HomeLandRank param);

    List<HomeLand.HomeLandRankMsg> getRankList();

    HomeLandRank getRank(String playerId);
}
