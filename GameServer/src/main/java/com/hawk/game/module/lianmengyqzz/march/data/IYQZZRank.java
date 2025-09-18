package com.hawk.game.module.lianmengyqzz.march.data;

import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.gamelib.rank.RankScoreHelper;
import org.hawk.redis.HawkRedisSession;
import redis.clients.jedis.Tuple;

import java.util.Set;

public interface IYQZZRank {
    String getRankKey();

    long getRankScore();

    String getRankId();

    int getRankSize();

    default void updateRank(){
        HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
        long rankScore = RankScoreHelper.calcSpecialRankScore(getRankScore());
        redisSession.zAdd(getRankKey(), rankScore, getRankId());
    }

    default Set<Tuple> getRankSet(){
        HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
        Set<Tuple> rankSet = redisSession.zRevrangeWithScores(getRankKey(), 0, Math.max((getRankSize() - 1), 0), 0);
        return rankSet;
    }
}
