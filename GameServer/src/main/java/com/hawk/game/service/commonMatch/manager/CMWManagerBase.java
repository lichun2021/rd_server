package com.hawk.game.service.commonMatch.manager;

import com.hawk.game.GsConfig;
import com.hawk.game.config.XHJZSeasonConst;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.PBCommonMatch;
import com.hawk.game.service.commonMatch.CMWRedisKey;
import com.hawk.game.service.commonMatch.data.CMWData;
import com.hawk.game.service.commonMatch.data.CMWPlayerData;
import com.hawk.game.service.commonMatch.state.CMWStateData;
import com.hawk.game.service.commonMatch.state.CMWStateEnum;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import redis.clients.jedis.Tuple;

import java.util.*;

public abstract class CMWManagerBase {
    protected CMWStateData stateData;
    protected Map<String, CMWData> oldMap = new HashMap<>();
    protected Map<String, CMWData> newMap = new HashMap<>();
    protected Map<String, Integer> rankMap = new HashMap<>();
    protected Map<String, Integer> rankingRankMap = new HashMap<>();
    public abstract void init();
    public abstract PBCommonMatch.PBCMWMatchType getMatchType();
    public abstract PBCommonMatch.PBCMWServerType getServerType();

    public abstract void pageInfo(Player player, PBCommonMatch.PBCMWPageInfoReq req);

    public abstract void rankInfo(Player player, PBCommonMatch.PBCMWRankInfoReq req);

    public abstract void battleInfo(Player player, PBCommonMatch.PBCMWBattleInfoReq req);

    public abstract void timeInfo(Player player, PBCommonMatch.PBCMWBattleTimeReq req);

    public abstract void targetInfo(Player player, PBCommonMatch.PBCMWBattleTargetReq req);

    public abstract long getEndTime();
    public abstract int calSeason();
    public abstract String gm(Map<String, String> map);
    public abstract void sendOpenMail();
    public abstract void sendQualifierMail();
    public abstract void sendFinalMail();

    public void onTickPerOneSecond(){
        stateData.tick();
    }

    public void onTickPerTenMinute(){

    }

    public int getSeason(){
        return stateData == null ? 0 : stateData.getSeason();
    }

    public CMWStateEnum getState(){
        return stateData == null ? CMWStateEnum.CLOSE: stateData.getState();
    }

    public void syncAllPLayer(){
        for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
            try {
                pageInfo(player, null);
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
    }

    public boolean isInSeason(String teamId){
        if(oldMap.containsKey(teamId)){
            return true;
        }
        if(newMap.containsKey(teamId)){
            return true;
        }
        return false;
    }

    public boolean getLock(String key, String state){
        return RedisProxy.getInstance().getRedisSession().hSetNx(key, state, GsConfig.getInstance().getServerId()) > 0;
    }

    public boolean getBigLock(){
        return getLock(getLockKey(), getState().name());
    }

    public void enterQualifier(){
        stateData.setSeason(calSeason());
        sendOpenMail();
    }

    public void enterRanking(){
        loadAllJoinDatas();
        sendQualifierMail();
    }

    public void enterEndShow(){
        loadAllJoinDatas();
        sendFinalMail();
    }

    public void enterClose(){
        rankMap.clear();
        rankingRankMap.clear();
        oldMap.clear();
        newMap.clear();
    }

    public void calQualifier(){
        boolean getLock = getLock(getLockKey(), CMWStateEnum.RANKING.name());
        if(!getLock){
            return;
        }
        pickJoin(false);
        pickJoin(true);
    }


    public void calFinal(){
        boolean getLock = getLock(getLockKey(), CMWStateEnum.END_SHOW.name());
        if(!getLock){
            return;
        }
        loadAllJoinDatas();
        calRank(false);
        calRank(true);
    }

    public void calRank(boolean isNew){
        try {
            List<CMWData> teamList = isNew ? new ArrayList<>(newMap.values()) : new ArrayList<>(oldMap.values());
            teamList.sort(( o1, o2) -> {
                if(o1.group != o2.group){
                    return o1.group < o2.group ? -1 : 1;
                }
                int totalWin1 = o1.rankingWinCnt - o1.rankingLoseCnt;
                int totalWin2 = o2.rankingWinCnt - o2.rankingLoseCnt;
                if(totalWin1 != totalWin2){
                    return  totalWin1 > totalWin2 ? -1 : 1;
                }
                if(o1.rankingScore != o2.rankingScore){
                    return o1.rankingScore > o2.rankingScore ? -1 : 1;
                }
                if(o1.totalScore != o2.totalScore){
                    return o1.totalScore > o2.totalScore ? -1 : 1;
                }
                return 0;
            });
            Map<String, String> dataStrMap = new HashMap<>();
            int i = 1;
            for(CMWData data : teamList){
                if(data == null){
                    continue;
                }
                data.rank = i;
                dataStrMap.put(data.teamId, data.serialize());
                HawkLog.logPrintln("CMWManagerBase calRank, matchType:{}, isNew:{}, teamId:{}， rank:{}", getMatchType(), isNew, data.teamId, i);
                i++;
            }
            updateDatas(dataStrMap);
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    public void pickJoin(boolean isNew){
        try {
            HawkLog.logPrintln("CMWManagerBase pickJoin start, matchType:{}, isNew:{}",getMatchType(), isNew);
            XHJZSeasonConst seasonConst = HawkConfigManager.getInstance().getKVInstance(XHJZSeasonConst.class);
            Set<Tuple> tuples  = RedisProxy.getInstance().getRedisSession().zRevrangeWithScores(getRankKey(isNew), 0, seasonConst.getGroupBmax() - 1,0);
            if(tuples == null || tuples.isEmpty()){
                HawkLog.logPrintln("CMWManagerBase pickJoin tuples is null, matchType:{}, isNew:{}",getMatchType(), isNew);
                return;
            }
            HawkLog.logPrintln("CMWManagerBase pickJoin tuples, matchType:{}, isNew:{}, size:{}", getMatchType(), isNew, tuples.size());
            List<String> teamIds = new ArrayList<>();
            for (Tuple tuple : tuples) {
                teamIds.add(tuple.getElement());
            }
            HawkLog.logPrintln("CMWManagerBase pickJoin add join, matchType:{}, isNew:{}", getMatchType(), isNew);
            RedisProxy.getInstance().getRedisSession().sAdd(getJoinKey(isNew), 0, teamIds.toArray(new String[0]));
            Map<String, String> dataStrMap = new HashMap<>();
            Map<String, CMWData> dataMap = loadDatas(teamIds);
            HawkLog.logPrintln("CMWManagerBase pickJoin pick start, matchType:{}, isNew:{}", getMatchType(), isNew);
            int i = 1;
            for (Tuple tuple : tuples) {
                try {
                    String teamId = tuple.getElement();
                    HawkLog.logPrintln("CMWManagerBase pickJoin pick team start, matchType:{}, isNew:{}, teamId:{}", getMatchType(), isNew, teamId);
                    CMWData data = dataMap.get(teamId);
                    if(data != null){
                        if(i >= seasonConst.getGroupSmin() && i <= seasonConst.getGroupSmax()){
                            data.group = PBCommonMatch.PBCMWGroupType.S_GROUP_VALUE;
                        }
                        if(i >= seasonConst.getGroupAmin() && i <= seasonConst.getGroupAmax()){
                            data.group = PBCommonMatch.PBCMWGroupType.A_GROUP_VALUE;
                        }
                        if(i >= seasonConst.getGroupBmin() && i <= seasonConst.getGroupBmax()){
                            data.group = PBCommonMatch.PBCMWGroupType.B_GROUP_VALUE;
                        }
                        data.qualifierRank = i;
                        HawkLog.logPrintln("CMWManagerBase pickJoin pick team, matchType:{}, isNew:{}, teamId:{}, rank:{}, group:{}", getMatchType(), isNew, teamId, i, data.group);
                        dataStrMap.put(data.teamId, data.serialize());
                    }
                    HawkLog.logPrintln("CMWManagerBase pickJoin pick team end, matchType:{}, isNew:{}, teamId:{}", getMatchType(), isNew, teamId);
                } catch (Exception e) {
                    HawkException.catchException(e);
                }
                i++;
            }
            updateDatas(dataStrMap);
            HawkLog.logPrintln("CMWManagerBase pickJoin end, matchType:{}, isNew:{}",getMatchType(), isNew);
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    public void loadAllJoinDatas(){
        oldMap = loadJoinDatas(false);
        newMap = loadJoinDatas(true);

    }

    public Map<String, CMWData> loadJoinDatas(boolean isNew){
        Set<String> teamIds = RedisProxy.getInstance().getRedisSession().sMembers(getJoinKey(isNew));
        return loadDatas(teamIds);
    }

    public Map<String, CMWData> loadDatas(Collection<String> teamIds){
        Map<String, CMWData> dataMap = new HashMap<>();
        if(teamIds == null || teamIds.isEmpty()){
            return dataMap;
        }
        List<String> list = RedisProxy.getInstance().getRedisSession().hmGet(getDataKey(), teamIds.toArray(new String[0]));
        for(String json : list){
            CMWData data = CMWData.unSerialize(json);
            if(data == null){
                continue;
            }
            dataMap.put(data.teamId, data);
        }
        return dataMap;
    }

    public void updateDatas(Map<String, String> dataStrMap){
        RedisProxy.getInstance().getRedisSession().hmSet(getDataKey(), dataStrMap, 0);
    }

    public CMWData loadData(String teamId){
        String json = RedisProxy.getInstance().getRedisSession().hGet(getDataKey(), teamId);
        return CMWData.unSerialize(json);
    }

    public void update(CMWData data){
        RedisProxy.getInstance().getRedisSession().hSet(getDataKey(), data.teamId, data.serialize());
        if(getState() == CMWStateEnum.QUALIFIER){
            RedisProxy.getInstance().getRedisSession().zAdd(getRankKey(data.isNew), data.score, data.teamId);
        }
    }

    public void removeRank(String teamId){
        if(getState() == CMWStateEnum.QUALIFIER){
            RedisProxy.getInstance().getRedisSession().zRem(getRankKey(true), 0, teamId);
            RedisProxy.getInstance().getRedisSession().zRem(getRankKey(false), 0, teamId);
        }
    }

    public Map<String, CMWPlayerData> loadPlayerDatas(Collection<String> playerIds){
        Map<String, CMWPlayerData> dataMap = new HashMap<>();
        List<String> list = RedisProxy.getInstance().getRedisSession().hmGet(getPlayerDataKey(), playerIds.toArray(new String[0]));
        for(String json : list){
            CMWPlayerData data = CMWPlayerData.unSerialize(json);
            if(data == null){
                continue;
            }
            dataMap.put(data.playerId, data);
        }
        return dataMap;
    }

    public void updatePlayerDatas(Map<String, String> dataStrMap){
        RedisProxy.getInstance().getRedisSession().hmSet(getPlayerDataKey(), dataStrMap, 0);
    }

    public CMWPlayerData loadPlayerData(String playerId){
        String json = RedisProxy.getInstance().getRedisSession().hGet(getPlayerDataKey(), playerId);
        return CMWPlayerData.unSerialize(json);
    }

    public String getStateKey() {
        return String.format(CMWRedisKey.CMW_STATE, getMatchType().name());
    }

    public String getLockKey() {
        return String.format(CMWRedisKey.CMW_LOCK, getMatchType().name(), getSeason());
    }

    public String getDataKey() {
        return String.format(CMWRedisKey.CMW_DATA, getMatchType().name(), getSeason());
    }

    public String getPlayerDataKey() {
        return String.format(CMWRedisKey.CMW_PLAYER_DATA, getMatchType().name(), getSeason());
    }

    public String getRankKey(boolean isNew) {
        if(isNew){
            return String.format(CMWRedisKey.CMW_RANK_NEW, getMatchType().name(), getSeason());
        }else {
            return String.format(CMWRedisKey.CMW_RANK, getMatchType().name(), getSeason());
        }
    }

    public String getJoinKey(boolean isNew){
        if(isNew){
            return String.format(CMWRedisKey.CMW_JOIN_NEW, getMatchType().name(), getSeason());
        }else {
            return String.format(CMWRedisKey.CMW_JOIN, getMatchType().name(), getSeason());
        }
    }
}
