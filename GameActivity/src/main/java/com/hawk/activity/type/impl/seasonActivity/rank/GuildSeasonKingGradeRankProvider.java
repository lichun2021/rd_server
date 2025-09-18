package com.hawk.activity.type.impl.seasonActivity.rank;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.rank.AbstractActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.activity.type.impl.seasonActivity.SeasonActivity;
import com.hawk.game.protocol.Activity;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.redis.HawkRedisSession;
import redis.clients.jedis.Tuple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 联盟段位王者段位排行管理器
 */
public class GuildSeasonKingGradeRankProvider extends AbstractActivityRankProvider<GuildSeasonKingGradeRank> {
    //联盟信息redis主键
    private static final String GUILD_INFO = "SEASON_ACTIVITY_GINFO";
    //本服缓存的联盟数据
    private List<GuildSeasonKingGradeRank> showList = new ArrayList<>();
    //本服缓存的联盟信息
    private Map<String, GuildSeasonKingGradeInfo> guildInfoMap = new ConcurrentHashMap<>();
    //是否可以更新排行榜
    @Override
    protected boolean canInsertIntoRank(GuildSeasonKingGradeRank rankInfo) {
        return true;
    }

    //更新数据
    @Override
    protected boolean insertRank(GuildSeasonKingGradeRank rankInfo) {
        HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
        long rankScore = RankScoreHelper.calcSpecialRankScore(rankInfo.getScore());
        rankScore = rankScore * 1000 + 999 - getTblyRank(rankInfo.getId());
        redisSession.zAdd(getRedisKey(), rankScore, rankInfo.getId());
        return true;
    }

    private int getTblyRank(String guildId){
        Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY.intValue());
        if (opActivity.isPresent()) {
            return opActivity.get().getTblyRank(guildId);
        }
        return 999;
    }

    /**
     * 排行榜展示范围
     * @return
     */
    @Override
    protected int getRankSize() {
        return 100;
    }

    /**
     * 排行榜类型
     * @return
     */
    @Override
    public ActivityRankType getRankType() {
        return ActivityRankType.GUILD_SEASON_KING_GRADE_RANK;
    }

    /**
     * 是否定时更新
     * @return
     */
    @Override
    public boolean isFixTimeRank() {
        Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.SEASON_ACTIVITY_VALUE);
        if (opActivity.isPresent()) {
            return opActivity.get().isShow();
        } else {
            return false;
        }
    }

    //加载排行榜
    @Override
    public void loadRank() {
        this.doRankSort();
    }

    //更新排行榜
    @Override
    public void doRankSort() {
        Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY.intValue());
        if (!opActivity.isPresent()) {
            return;
        }
        int rankSize = getRankSize();

        HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
        Set<Tuple> rankSet = redisSession.zRevrangeWithScores(getRedisKey(), 0, Math.max((rankSize - 1), 0), 0);
        List<String> guildIds = new ArrayList<>();
        List<GuildSeasonKingGradeRank> newRankList = new ArrayList<>(rankSize);
        int index = 1;
        for (Tuple rank : rankSet) {
            GuildSeasonKingGradeRank kingGradeRank = new GuildSeasonKingGradeRank();
            kingGradeRank.setRank(index);
            kingGradeRank.setId(rank.getElement());
            long score = RankScoreHelper.getRealScore((long) rank.getScore() / 1000);
            kingGradeRank.setScore(score);
            newRankList.add(kingGradeRank);
            guildIds.add(rank.getElement());
            index++;
        }
        showList = newRankList;
    }

    /**
     * 排行榜存储主键
     * @return
     */
    private String getRedisKey(){
        Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY.intValue());
        if (opActivity.isPresent()) {
            return opActivity.get().getRankKey();
        }
        return null;
    }

    /**
     * 联盟信息存储主键
     * @return
     */
    private String getGuildInfoKey(){
        return GUILD_INFO;
    }

    /**
     * 获得排行榜数据
     * @return
     */
    @Override
    public List<GuildSeasonKingGradeRank> getRankList() {
        return showList;
    }

    /**
     * 获得排名
     * @param id
     * @return
     */
    @Override
    public GuildSeasonKingGradeRank getRank(String id) {
        RedisIndex index = ActivityGlobalRedis.getInstance().zrevrankAndScore(getRedisKey(), id);
        int rank = -1;
        long score = 0;
        if (index != null) {
            rank = index.getIndex().intValue() + 1;
            score = RankScoreHelper.getRealScore(index.getScore().longValue() / 1000);
        }
        GuildSeasonKingGradeRank kingGradeRank = new GuildSeasonKingGradeRank();
        kingGradeRank.setId(id);
        kingGradeRank.setRank(rank);
        kingGradeRank.setScore(score);
        return kingGradeRank;
    }

    /**
     * 批量获得缓存排名
     * @param start
     * @param end
     * @return
     */
    @Override
    public List<GuildSeasonKingGradeRank> getRanks(int start, int end) {
        start = start < 0 ? 0 : start;
        end = end > showList.size() ? showList.size() : end;

        return showList.subList(start, end);
    }

    /**
     * 清理排行数据
     */
    @Override
    public void clean() {
        showList  = new ArrayList<>(this.getRankSize());
        HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
        redisSession.del(getRedisKey());
    }

    @Override
    public void addScore(String id, int score) {
        throw  new UnsupportedOperationException("GuildSeasonKingGradeRank can not addScore");
    }

    @Override
    public void remMember(String id) {
        HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
        redisSession.zRem(getRedisKey(), 0, id);
    }

    /**
     * 更新联盟信息
     * @param guildId
     * @param info
     */
    public void updataGuildInfo(String guildId, GuildSeasonKingGradeInfo info){
        guildInfoMap.put(guildId, info);
        HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
        redisSession.hSet(getGuildInfoKey(), guildId, SerializeHelper.toSerializeString(info, SerializeHelper.COLON_ITEMS));
    }

    /**
     * 获得联盟信息
     * @param guildId
     * @return
     */
    public GuildSeasonKingGradeInfo getGuildInfo(String guildId){
        GuildSeasonKingGradeInfo guildInfo = guildInfoMap.get(guildId);
        if(guildInfo != null){
            return guildInfo;
        }
        HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
        String guildInfoString = redisSession.hGet(getGuildInfoKey(), guildId);
        guildInfo = SerializeHelper.getValue(GuildSeasonKingGradeInfo.class, guildInfoString, SerializeHelper.COLON_ITEMS);
        guildInfoMap.put(guildId, guildInfo);
        return guildInfo;
    }
}
