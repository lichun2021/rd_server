package com.hawk.game.module.homeland.rank;

import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.game.global.RedisKey;
import com.hawk.game.module.homeland.cfg.HomeLandRankCfg;
import com.hawk.game.protocol.HomeLand;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import redis.clients.jedis.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.hawk.game.global.RedisKey.HOME_LAND_RANK;

/**
 * 家园榜
 *
 * @author zhy
 */
public class HomeLandCrossRankImpl implements HomeLandRankImpl {
    protected final List<HomeLand.HomeLandRankMsg> showRankList = new ArrayList<>();
    protected HomeLandRankCfg cfg;
    private String keySuffix;
    private long lastRankRefreshTime;

    public HomeLandCrossRankImpl(HomeLandRankCfg cfg, String keySuffix) {
        this.cfg = cfg;
        this.keySuffix = keySuffix;
    }

    protected String getRedisKey() {
        return HOME_LAND_RANK + HomeLandRankType.getValue(this.cfg.getType()).name() + ":" +
                HomeLandRankServerType.getValue(this.cfg.getRange()).name();
    }

    public void loadRank() {
        doRankSort();
    }

    @Override
    public int getRefreshInterval() {
        return this.cfg.getDelay();
    }

    protected int getRankSize() {
        return cfg.getMaxNum();
    }

    public void setCfg(HomeLandRankCfg cfg) {
        this.cfg = cfg;
    }

    @Override
    public void insertIntoRank(HomeLandRank rank) {
        int curSeconds = HawkTime.getSeconds();
        String playerId = rank.getId();
        long param = 9999999999L - curSeconds;
        String valStr = String.format("%d.%d", rank.getScore(), param);
        double val = Double.parseDouble(valStr);
        ActivityLocalRedis.getInstance().zadd(getRedisKey(), val, playerId);
    }

    @Override
    public void doRankSort() {
        int rankSize = getRankSize();
        Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(getRedisKey(), 0, Math.max((rankSize - 1), 0));
        showRankList.clear();
        List<HomeLandRank> showRankTemp = new ArrayList<>();
        HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
        List<String> playerIds = new ArrayList<>();
        for (Tuple rank : rankSet) {
            HomeLandRank homeLandRank = new HomeLandRank();
            homeLandRank.setId(rank.getElement());
            homeLandRank.setScore((long) rank.getScore());
            playerIds.add(rank.getElement());
            showRankTemp.add(homeLandRank);
        }
        // 玩家信息
        Map<String, HomeLandPlayerRankInfo> playerInfoMap = new ConcurrentHashMap<>();
        String[] playerIdArray = playerIds.toArray(new String[0]);
        if (playerIdArray.length > 0) {
            List<String> playerInfoList = redisSession.hmGet(RedisKey.HOME_LAND_RANK_PLAYER, playerIdArray);
            for (String playerInfoStr : playerInfoList) {
                HomeLandPlayerRankInfo playerInfo = SerializeHelper.parseJsonStr(playerInfoStr, HomeLandPlayerRankInfo.class);
                if (playerInfo != null) {
                    playerInfoMap.put(playerInfo.getPlayerId(), playerInfo);
                }
            }
        }
        int index = 1;
        for (HomeLandRank homeLandRank : showRankTemp) {
            if (!playerInfoMap.containsKey(homeLandRank.getId())) {
                continue;
            }
            homeLandRank.setRank(index);
            HomeLandPlayerRankInfo rankInfo = playerInfoMap.get(homeLandRank.getId());
            if (!cfg.getServerList().isEmpty() && !cfg.getServerList().contains(rankInfo.getServerId())) {
                continue;
            }
            showRankList.add(rankInfo.buildRankInfo(homeLandRank));
            index++;
        }
    }

    @Override
    public List<HomeLand.HomeLandRankMsg> getRankList() {
        return showRankList;
    }

    @Override
    public HomeLandRank getRank(String playerId) {
        HomeLandRank scoreRank = new HomeLandRank();
        for (HomeLand.HomeLandRankMsg rankMsg : showRankList) {
            if (rankMsg.getPlayerId().equals(playerId)) {
                scoreRank.setId(playerId);
                scoreRank.setRank(rankMsg.getRank());
                scoreRank.setScore(rankMsg.getScore());
                return scoreRank;
            }
        }
        return scoreRank;
    }

    @Override
    public long getLastRankRefreshTime() {
        return lastRankRefreshTime;
    }

    @Override
    public void setLastRankRefreshTime(long lastRankRefreshTime) {
        this.lastRankRefreshTime = lastRankRefreshTime;
    }
}
