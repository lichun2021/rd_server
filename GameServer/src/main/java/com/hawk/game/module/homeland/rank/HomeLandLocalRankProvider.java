package com.hawk.game.module.homeland.rank;

import com.hawk.game.GsConfig;
import com.hawk.game.module.homeland.cfg.HomeLandRankCfg;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 家园本服榜
 *
 * @author zhy
 */
public class HomeLandLocalRankProvider {
    private final Map<HomeLandRankType, HomeLandRankImpl> rankList = new ConcurrentHashMap<>();

    public void init() {
        try {
            ConfigIterator<HomeLandRankCfg> ranks = HawkConfigManager.getInstance().getConfigIterator(HomeLandRankCfg.class);
            for (HomeLandRankCfg rankCfg : ranks) {
                if (rankCfg.getRange() != HomeLandRankServerType.LOCAL.getType()) {
                    continue;
                }
                rankList.computeIfAbsent(
                        HomeLandRankType.getValue(rankCfg.getType()),
                        k1 -> {
                            String serverId = GsConfig.getInstance().getServerId();
                            HomeLandLocalRankImpl localRank = new HomeLandLocalRankImpl(rankCfg, serverId);
                            localRank.loadRank();
                            return localRank;
                        }
                );
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    public void onTick() {
        for (HomeLandRankImpl rank : rankList.values()) {
            long curTime = HawkTime.getMillisecond();
            int delay = rank.getRefreshInterval();
            if (curTime - rank.getLastRankRefreshTime() > delay) {
                rank.doRankSort();
                rank.setLastRankRefreshTime(curTime);
            }
        }
    }

    public HomeLandRankImpl getRankByType(HomeLandRankType type) {
        if (rankList.containsKey(type)) {
            return rankList.get(type);
        }
        return null;
    }

    public void updateRank(HomeLandRankType rankType, HomeLandRank param) {
        if (param.getScore() <= 0) {
            return;
        }
        try {
            HomeLandRankImpl rankImpl = getRankByType(rankType);
            if (rankImpl == null) {
                return;
            }
            rankImpl.insertIntoRank(param);
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

}
