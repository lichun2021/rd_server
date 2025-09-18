package com.hawk.game.service.commonMatch.manager.ipml;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.seasonActivity.SeasonActivity;
import com.hawk.game.GsConfig;
import com.hawk.game.config.*;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.*;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.commonMatch.CMWRedisKey;
import com.hawk.game.service.commonMatch.data.CMWBattleInfo;
import com.hawk.game.service.commonMatch.data.CMWData;
import com.hawk.game.service.commonMatch.data.CMWPlayerData;
import com.hawk.game.service.commonMatch.manager.CMWManagerBase;
import com.hawk.game.service.commonMatch.state.CMWStateData;
import com.hawk.game.service.commonMatch.state.CMWStateEnum;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.xhjzWar.*;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst;
import com.hawk.serialize.string.SerializeHelper;

import org.apache.commons.collections4.CollectionUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkDelayTask;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.tuple.HawkTuple2;
import redis.clients.jedis.Tuple;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class XHJZSeasonManager extends CMWManagerBase {
    private static final XHJZSeasonManager instance = new XHJZSeasonManager();

    private XHJZSeasonManager() {}

    public static XHJZSeasonManager getInstance() {
        return instance;
    }

    @Override
    public void init() {
        stateData = new CMWStateData();
        stateData.load(getInstance());
        loadQualifierRank(true);
        loadQualifierRank(false);
        if(getState() == CMWStateEnum.RANKING || getState() == CMWStateEnum.END_SHOW){
            loadAllJoinDatas();
            loadRankingRank(true);
            loadRankingRank(false);
        }
    }

    @Override
    public PBCommonMatch.PBCMWMatchType getMatchType() {
        return PBCommonMatch.PBCMWMatchType.XHJZ_SEASON;
    }

    @Override
    public PBCommonMatch.PBCMWServerType getServerType() {
        XHJZSeasonConst seasonConst = HawkConfigManager.getInstance().getKVInstance(XHJZSeasonConst.class);
        if(seasonConst.getOnlyOldServerSet().contains(GsConfig.getInstance().getServerId())){
            return PBCommonMatch.PBCMWServerType.OLD_SERVER;
        }
        PBCommonMatch.PBCMWServerType type = PBCommonMatch.PBCMWServerType.NEW_SERVER;
        String serverId = GsConfig.getInstance().getServerId();
        List<String> serverList = GlobalData.getInstance().getMergeServerList(serverId);
        if (CollectionUtils.isEmpty(serverList)) {
            return type;
        }
        if(serverList.size() > 8){
            type = PBCommonMatch.PBCMWServerType.OLD_SERVER;
        }
        return type;
    }

    public XHJZSeasonTimeCfg getCfg(){
        return HawkConfigManager.getInstance().getConfigByKey(XHJZSeasonTimeCfg.class, getSeason());
    }

    public XHJZSeasonTimeCfg calCfg(){
        long now = HawkTime.getMillisecond();
        ConfigIterator<XHJZSeasonTimeCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(XHJZSeasonTimeCfg.class);
        for(XHJZSeasonTimeCfg cfg : iterator){
            if(cfg.getSeasonStartTimeValue() <= now && cfg.getSeasonEndTimeValue() > now){
                return cfg;
            }
        }
        return null;
    }

    @Override
    public int calSeason(){
        XHJZSeasonTimeCfg cfg = calCfg();
        return cfg == null ? -1 : cfg.getTermId();
    }

    @Override
    public long getEndTime() {
        switch (getState()){
            case CLOSE:{
                XHJZSeasonTimeCfg cfg = calCfg();
                if(cfg == null){
                    return HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(30);
                }
                return cfg.getSeasonStartTimeValue();
            }
            case QUALIFIER: {
                XHJZSeasonTimeCfg cfg = getCfg();
                if(cfg == null){
                    return HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(30);
                }
                return cfg.getQualifierEndTimeValue();
            }
            case RANKING: {
                XHJZSeasonTimeCfg cfg = getCfg();
                if(cfg == null){
                    return HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(30);
                }
                return cfg.getRankingEndTimeValue();
            }
            case END_SHOW: {
                XHJZSeasonTimeCfg cfg = getCfg();
                if(cfg == null){
                    return HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(30);
                }
                return cfg.getSeasonEndTimeValue();
            }
        }
        return Long.MAX_VALUE;
    }


    public void onTeamDismiss(String teamId){
        try {
            removeRank(teamId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addScore(String teamId, long score, boolean isWin) {
        if(getState() == CMWStateEnum.CLOSE){
            HawkLog.logPrintln("XHJZSeasonManager addScore season is close, teamId:{}, score:{}, isWin:{}", teamId, score, isWin);
            return;
        }
        HawkLog.logPrintln("XHJZSeasonManager addScore start, teamId:{}, score:{}, isWin:{}", teamId, score, isWin);
        XHJZSeasonConst seasonConst = HawkConfigManager.getInstance().getKVInstance(XHJZSeasonConst.class);
        CMWData data = loadData(teamId);
        if(data == null){
            data = new CMWData();
            data.score = seasonConst.getInitPoint();
            data.teamId = teamId;
            data.isNew = (getServerType() == PBCommonMatch.PBCMWServerType.NEW_SERVER);
        }
        if(getState() == CMWStateEnum.QUALIFIER){
            data.score += calScore(seasonConst, score, isWin);
        }else {
            data.rankingScore += calScore(seasonConst, score, isWin);
        }
        data.totalScore += score;
        if(isWin){
            if(getState() == CMWStateEnum.QUALIFIER){
                data.winCnt++;
            }else {
                data.rankingWinCnt++;
            }

        }else {
            if(getState() == CMWStateEnum.QUALIFIER){
                data.loseCnt++;
            }else {
                data.rankingLoseCnt++;
            }

        }
        update(data);
        addPlayerScore(teamId, data.totalScore);
        HawkLog.logPrintln("XHJZSeasonManager addScore data, data:{}", data.serialize());
        HawkLog.logPrintln("XHJZSeasonManager addScore end, teamId:{}, score:{}, isWin:{}", teamId, score, isWin);
    }

    public void addPlayerScore(String teamId, long score) {
        try {
            HawkLog.logPrintln("XHJZSeasonManager addPlayerScore start, teamId:{}, score:{}", teamId, score);
            Set<String> playerIds = XHJZWarService.getInstance().getTeamPlayerIds(teamId);
            if(playerIds == null || playerIds.isEmpty()){
                HawkLog.logPrintln("XHJZSeasonManager addPlayerScore playerIds is null, teamId:{}", teamId);
                return;
            }
            Map<String, CMWPlayerData> playerDataMap = loadPlayerDatas(playerIds);
            HawkLog.logPrintln("XHJZSeasonManager addPlayerScore playerIds, teamId:{}, size:{}", teamId, playerDataMap.size());
            Map<String, String> playerDataStrMap = new HashMap<>();
            for(String playerId : playerIds){
                CMWPlayerData playerData = playerDataMap.getOrDefault(playerId, new CMWPlayerData(playerId));
                playerData.teamScore = score;
                ConfigIterator<XHJZSeasonTargetRewardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(XHJZSeasonTargetRewardCfg.class);
                for(XHJZSeasonTargetRewardCfg cfg : iterator){
                    if(cfg.getTarget() > score){
                        HawkLog.logPrintln("XHJZSeasonManager addPlayerScore send player target is not, teamId:{}, playerId:{}", teamId, playerId);
                        continue;
                    }
                    if(playerData.teamRewarded.contains(cfg.getId())) {
                        HawkLog.logPrintln("XHJZSeasonManager addPlayerScore send player have send, teamId:{}, playerId:{}", teamId, playerId);
                        continue;
                    }
                    playerData.teamRewarded.add(cfg.getId());
                    HawkLog.logPrintln("XHJZSeasonManager addPlayerScore send player, teamId:{}, playerId:{}, cfgId:{}", teamId, playerId, cfg.getId());
                    SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                            .setPlayerId(playerData.playerId)
                            .setMailId(MailConst.MailId.XHJZ_SEASON_2024072505)
                            .addContents(cfg.getTarget())
                            .setAwardStatus(Const.MailRewardStatus.NOT_GET)
                            .addRewards(cfg.getRewardItem())
                            .build());
                }
                playerDataStrMap.put(playerData.playerId, playerData.serialize());
            }
            updatePlayerDatas(playerDataStrMap);
            HawkLog.logPrintln("XHJZSeasonManager addPlayerScore end, teamId:{}, score:{}", teamId, score);
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    public int calScore(XHJZSeasonConst seasonConst, long score, boolean isWin){
        if(isWin){
            return (int)(score/seasonConst.getWinPointPara()) + seasonConst.getWinPoint();
        }else {
            return (int)(score/seasonConst.getLosrPointPara()) + seasonConst.getLosrPoint();
        }
    }

    public void updateBattleInfo(XHJZWarTeamData teamData1, XHJZWarTeamData teamData2, XHJZWarRoomData roomData){
        try {
            if(teamData1 == null || teamData2 == null || roomData == null || roomData.group == 0){
                return;
            }
            CMWBattleInfo battleInfo = new CMWBattleInfo(teamData1, teamData2, roomData);
            CMWStateEnum state = getState();
            String name = getMatchType().name();
            int season = getSeason();
            int termId = roomData.termId;
            long delayTime = randDelayTime();
            addDelayTask(new HawkDelayTask(delayTime, delayTime, 1) {
                @Override
                public Object run() {
                    if(state == CMWStateEnum.QUALIFIER){
                        String kayBase = battleInfo.isNew ? CMWRedisKey.CMW_BATTLE_QUALIFIER_SELF_NEW : CMWRedisKey.CMW_BATTLE_QUALIFIER_SELF;
                        String key1 = String.format(kayBase, name, season, battleInfo.teamIdA);
                        String key2 = String.format(kayBase, name, season, battleInfo.teamIdB);
                        RedisProxy.getInstance().getRedisSession().hSet(key1, String.valueOf(battleInfo.termId), battleInfo.serialize());
                        RedisProxy.getInstance().getRedisSession().hSet(key2, String.valueOf(battleInfo.termId), battleInfo.serialize());
                    }
                    if(state == CMWStateEnum.RANKING){
                        String kayBase = battleInfo.isNew ? CMWRedisKey.CMW_BATTLE_RANKING_SELF_NEW : CMWRedisKey.CMW_BATTLE_RANKING_SELF;
                        String key1 = String.format(kayBase, name, season, battleInfo.teamIdA);
                        String key2 = String.format(kayBase, name, season, battleInfo.teamIdB);
                        RedisProxy.getInstance().getRedisSession().hSet(key1, String.valueOf(battleInfo.termId), battleInfo.serialize());
                        RedisProxy.getInstance().getRedisSession().hSet(key2, String.valueOf(battleInfo.termId), battleInfo.serialize());
                        String kayGroupBase = battleInfo.isNew ? CMWRedisKey.CMW_BATTLE_GROUP_NEW : CMWRedisKey.CMW_BATTLE_GROUP;
                        String key = String.format(kayGroupBase, name, season, battleInfo.group,termId);
                        RedisProxy.getInstance().getRedisSession().hSet(key, battleInfo.roomId, battleInfo.serialize());
                    }
                    return null;
                }
            });
            if(HawkOSOperator.isEmptyString(roomData.winnerId)){
                Map<String, Object> param = new HashMap<>();
                param.put("teamA", roomData.campA);
                param.put("teamB", roomData.campB);
                param.put("serverType", roomData.isNew ? 1 : 2);
                param.put("groupType", roomData.group);
                LogUtil.logActivityCommon(LogConst.LogInfoType.xhjz_season_match, param);
            }else {
                Map<String, Object> param = new HashMap<>();
                param.put("teamA", roomData.campA);
                param.put("scoreA", roomData.scoreA);
                param.put("teamB", roomData.campB);
                param.put("scoreB", roomData.scoreB);
                param.put("winnerId", roomData.winnerId);
                param.put("serverType", roomData.isNew ? 1 : 2);
                param.put("groupType", roomData.group);
                LogUtil.logActivityCommon(LogConst.LogInfoType.xhjz_season_result, param);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static long randDelayTime() {
        long baseTime = 5000l;
        long randTime = HawkRand.randInt(30000);
        return baseTime + randTime;
    }

    private static void addDelayTask(HawkDelayTask task) {
        HawkThreadPool taskPool = HawkTaskManager.getInstance().getThreadPool("task");
        if (null != taskPool) {
            task.setTypeName("XHJZDelayTask");
            taskPool.addTask(task, 0, false);
        }
    }

    @Override
    public void pageInfo(Player player, PBCommonMatch.PBCMWPageInfoReq req) {
        PBCommonMatch.PBCMWPageInfoResp.Builder resp = PBCommonMatch.PBCMWPageInfoResp.newBuilder();
        resp.setMatchType(req == null ? getMatchType() : req.getMatchType());
        resp.setServerType(req == null ? getServerType() : req.getServerType());
        resp.setPageInfo(genPage(player, req));
        resp.setSelfType(getServerType());
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.CMW_PAGE_INFO_RESP, resp));

    }


    @Override
    public void rankInfo(Player player, PBCommonMatch.PBCMWRankInfoReq req) {
        PBCommonMatch.PBCMWRankInfoResp.Builder resp = null;
        Map<String, PBCommonMatch.PBCMWTeamInfo.Builder> infoMap = null;
        if(req.getBattleType() == PBCommonMatch.PBCMWBattleType.QUALIFIER){
            if(req.getServerType() == PBCommonMatch.PBCMWServerType.NEW_SERVER){
                resp = newQualifierRank;
                infoMap = newQualifierInfoMap;
            }else {
                resp = oldQualifierRank;
                infoMap = oldQualifierInfoMap;
            }
        }

        if(req.getBattleType() == PBCommonMatch.PBCMWBattleType.RANKING){
            int termId = XHJZWarService.getInstance().getTermId();
            XHJZSeasonTimeCfg cfg = getCfg();
            if(cfg != null){
                if(termId < cfg.getRankingTermId() || termId > cfg.getEndTermId()){
                    return;
                }
                if(termId == cfg.getRankingTermId() && XHJZWarService.getInstance().getState() != XHJZWarStateEnum.PEACE){
                    return;
                }
            }
            if(req.getServerType() == PBCommonMatch.PBCMWServerType.NEW_SERVER){
                resp = newRankingRank;
                infoMap = newRankingInfoMap;
            }else {
                resp = oldRankingRank;
                infoMap = oldRankingInfoMap;
            }
        }
        if(resp == null){
            return;
        }
        resp.setMatchType(req.getMatchType());
        resp.setServerType(req.getServerType());
        resp.setBattleType(req.getBattleType());
        resp.setIsEnd(req.getIsEnd());
        String teamId = XHJZWarService.getInstance().getSelfTeamId(player);
        if(infoMap != null){
            PBCommonMatch.PBCMWTeamInfo.Builder selfInfo = infoMap.get(teamId);
            if(selfInfo != null){
                resp.setSelfInfo(selfInfo);
            }
        }
        long curTime = HawkTime.getMillisecond();
        String timeStr = "2025-07-01 23:02:00";
        long timeLimit = HawkTime.parseTime(timeStr);
        if(curTime > timeLimit && GsConfig.getInstance().getAreaId().equals("1") 
        		&& req.getBattleType() == PBCommonMatch.PBCMWBattleType.RANKING
        		&& req.getServerType() == PBCommonMatch.PBCMWServerType.OLD_SERVER){
        	PBCommonMatch.PBCMWRankInfoResp.Builder respClone = resp.clone();
        	respClone.clearRankInfos();
        	
        	List<PBCommonMatch.PBCMWTeamInfo.Builder> ranks = resp.getRankInfosBuilderList();
        	for(PBCommonMatch.PBCMWTeamInfo.Builder rank : ranks){
        		PBCommonMatch.PBCMWTeamInfo.Builder rankClone = rank.clone();
        		if(rankClone.getRank() >=2 && rankClone.getRank() <= 9){
        			rankClone.setRank(2);
        		}
        		respClone.addRankInfos(rankClone);
        	}
        	
        	PBCommonMatch.PBCMWTeamInfo.Builder selfInfo = resp.getSelfInfoBuilder();
        	if(Objects.nonNull(selfInfo)){
        		PBCommonMatch.PBCMWTeamInfo.Builder selfInfoClone = selfInfo.clone();
        		if(selfInfoClone.getRank() >=2 && selfInfoClone.getRank() <= 9){
        			selfInfoClone.setRank(2);
        		}
        		respClone.setSelfInfo(selfInfoClone);
        	}
        	player.sendProtocol(HawkProtocol.valueOf(HP.code2.CMW_RANK_INFO_RESP, respClone));
        	return;
        }
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.CMW_RANK_INFO_RESP, resp));
    }

    @Override
    public void battleInfo(Player player, PBCommonMatch.PBCMWBattleInfoReq req) {
        PBCommonMatch.PBCMWBattleInfoResp.Builder resp = PBCommonMatch.PBCMWBattleInfoResp.newBuilder();
        resp.setMatchType(req.getMatchType());
        resp.setServerType(req.getServerType());
        resp.setBattleType(req.getBattleType());
        resp.setGroupType(req.getGroupType());
        resp.setIsSelf(req.getIsSelf());
        resp.setTermId(req.getTermId());
        XHJZSeasonTimeCfg cfg = getCfg();
        String teamId = XHJZWarService.getInstance().getSelfTeamId(player);
        int curTermId = XHJZWarService.getInstance().getTermId();
        if(req.getBattleType() == PBCommonMatch.PBCMWBattleType.QUALIFIER){
            String keyBase = req.getServerType() == PBCommonMatch.PBCMWServerType.NEW_SERVER ? CMWRedisKey.CMW_BATTLE_QUALIFIER_SELF_NEW : CMWRedisKey.CMW_BATTLE_QUALIFIER_SELF;
            Map<Integer, CMWBattleInfo> battleInfoMap = loadQualifierBattleInfo(String.format(keyBase, getMatchType().name(), getSeason(),teamId));
            int endTerm = Math.min(curTermId+1, cfg.getRankingTermId());
            for(int i = cfg.getQualifierTermId(); i < endTerm ; i++){
                XHJZWarTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(XHJZWarTimeCfg.class, i);
                long battleStartTIme = timeCfg == null ? HawkTime.getMillisecond() : timeCfg.getBattleTime();
                CMWBattleInfo battleInfo = battleInfoMap.get(i);
                if(battleInfo == null){
                    PBCommonMatch.PBCMWBattleInfo.Builder builder = PBCommonMatch.PBCMWBattleInfo.newBuilder();
                    builder.setTermId(i - cfg.getQualifierTermId() + 1);
                    builder.setBattleStartTime(battleStartTIme);
                    builder.setWinCount(0);
                    resp.addBattleInfos(builder);
                }else {
                    if(!HawkOSOperator.isEmptyString(battleInfo.winnerId)){
                        PBCommonMatch.PBCMWBattleInfo.Builder builder = toBattleInfoPB(battleInfo, teamId);
                        builder.setTermId(i - cfg.getQualifierTermId() + 1);
                        resp.addBattleInfos(builder);
                    }
                }
            }
        }else {
            if(req.getIsSelf()){
                String keyBase = req.getServerType() == PBCommonMatch.PBCMWServerType.NEW_SERVER ? CMWRedisKey.CMW_BATTLE_RANKING_SELF_NEW: CMWRedisKey.CMW_BATTLE_RANKING_SELF;
                Map<Integer, CMWBattleInfo> battleInfoMap = loadQualifierBattleInfo(String.format(keyBase, getMatchType().name(), getSeason(),teamId));
                int endTerm = Math.min(curTermId, cfg.getEndTermId());
                for(int i = cfg.getRankingTermId(); i <= endTerm ; i++){
                    XHJZWarTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(XHJZWarTimeCfg.class, i);
                    long battleStartTIme = timeCfg == null ? HawkTime.getMillisecond() : timeCfg.getBattleTime();
                    CMWBattleInfo battleInfo = battleInfoMap.get(i);
                    if(battleInfo == null){
                        PBCommonMatch.PBCMWBattleInfo.Builder builder = PBCommonMatch.PBCMWBattleInfo.newBuilder();
                        builder.setTermId(i - cfg.getRankingTermId() + 1);
                        builder.setBattleStartTime(battleStartTIme);
                        builder.setWinCount(0);
                        resp.addBattleInfos(builder);
                    }else {
                        if(!HawkOSOperator.isEmptyString(battleInfo.winnerId)){
                            PBCommonMatch.PBCMWBattleInfo.Builder builder = toBattleInfoPB(battleInfo, teamId);
                            builder.setTermId(i - cfg.getRankingTermId() + 1);
                            resp.addBattleInfos(builder);
                        }
                    }
                }
            }else {
                int realTerm = req.getTermId() + cfg.getRankingTermId() - 1;
                String keyBase = req.getServerType() == PBCommonMatch.PBCMWServerType.NEW_SERVER ? CMWRedisKey.CMW_BATTLE_GROUP_NEW : CMWRedisKey.CMW_BATTLE_GROUP;
                Map<String, CMWBattleInfo> battleInfoMap = loadRankingBattleInfo(String.format(keyBase, getMatchType().name(), getSeason(),req.getGroupType().getNumber(), realTerm));
                for(CMWBattleInfo battleInfo : battleInfoMap.values()){
                    if(!HawkOSOperator.isEmptyString(battleInfo.winnerId)){
                        resp.addBattleInfos(toBattleInfoPB(battleInfo, teamId));
                    }
                }
                XHJZWarTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(XHJZWarTimeCfg.class, realTerm);
                long battleStartTIme = timeCfg == null ? HawkTime.getMillisecond() : timeCfg.getBattleTime();
                resp.setBattleStartTime(battleStartTIme);
            }

        }
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.CMW_BATTLE_INFO_RESP, resp));
    }

    public PBCommonMatch.PBCMWBattleInfo.Builder toBattleInfoPB(CMWBattleInfo battleInfo, String teamId){
        PBCommonMatch.PBCMWBattleInfo.Builder builder = PBCommonMatch.PBCMWBattleInfo.newBuilder();
        PBCommonMatch.PBCMWTeamInfo.Builder team1 = PBCommonMatch.PBCMWTeamInfo.newBuilder();
        PBCommonMatch.PBCMWTeamInfo.Builder team2 = PBCommonMatch.PBCMWTeamInfo.newBuilder();
        if(teamId.equals(battleInfo.teamIdA)){
            team1.setId(battleInfo.teamIdA);
            team1.setName(battleInfo.teamNameA);
            team1.setGuildName(battleInfo.guildNameA);
            team1.setGuildTag(battleInfo.guildTagA);
            team1.setServerId(battleInfo.serverIdA);
            team1.setIsWin(battleInfo.winnerId.equals(battleInfo.teamIdA));
            team2.setId(battleInfo.teamIdB);
            team2.setName(battleInfo.teamNameB);
            team2.setGuildName(battleInfo.guildNameB);
            team2.setGuildTag(battleInfo.guildTagB);
            team2.setServerId(battleInfo.serverIdB);
            team2.setIsWin(battleInfo.winnerId.equals(battleInfo.teamIdB));
        }else {
            team1.setId(battleInfo.teamIdB);
            team1.setName(battleInfo.teamNameB);
            team1.setGuildName(battleInfo.guildNameB);
            team1.setGuildTag(battleInfo.guildTagB);
            team1.setServerId(battleInfo.serverIdB);
            team1.setIsWin(battleInfo.winnerId.equals(battleInfo.teamIdB));
            team2.setId(battleInfo.teamIdA);
            team2.setName(battleInfo.teamNameA);
            team2.setGuildName(battleInfo.guildNameA);
            team2.setGuildTag(battleInfo.guildTagA);
            team2.setServerId(battleInfo.serverIdA);
            team2.setIsWin(battleInfo.winnerId.equals(battleInfo.teamIdA));
        }
        builder.setTeam1(team1);
        builder.setTeam2(team2);
        builder.setTermId(battleInfo.termId);
        builder.setBattleStartTime(XHJZWarService.getInstance().getBattleStartTime(battleInfo.termId, battleInfo.timeIndex));
        builder.setWinCount(battleInfo.winCount);
        return builder;
    }

    @Override
    public void timeInfo(Player player, PBCommonMatch.PBCMWBattleTimeReq req) {
        PBCommonMatch.PBCMWBattleTimeResp.Builder resp = PBCommonMatch.PBCMWBattleTimeResp.newBuilder();
        resp.setMatchType(req.getMatchType());
        resp.setServerType(req.getServerType());

        PBCommonMatch.PBCMWBattleTime.Builder time1 = PBCommonMatch.PBCMWBattleTime.newBuilder();
        time1.setType(PBCommonMatch.PBCMWBattleType.QUALIFIER);
        PBCommonMatch.PBCMWBattleTime.Builder time2 = PBCommonMatch.PBCMWBattleTime.newBuilder();
        time2.setType(PBCommonMatch.PBCMWBattleType.RANKING);
        XHJZSeasonTimeCfg cfg = getCfg();
        long now = HawkTime.getMillisecond();
        if(cfg != null){
            time1.setStartTime(cfg.getSeasonStartTimeValue());
            time1.setEndTime(cfg.getQualifierEndTimeValue());
            time2.setStartTime(cfg.getQualifierEndTimeValue());
            time2.setEndTime(cfg.getRankingEndTimeValue());

            for(int i = cfg.getQualifierTermId(); i < cfg.getRankingTermId(); i++){
                PBCommonMatch.PBCMWBattleTimeInfo.Builder info = PBCommonMatch.PBCMWBattleTimeInfo.newBuilder();
                info.setTermId(i - cfg.getQualifierTermId() + 1);
                XHJZWarTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(XHJZWarTimeCfg.class, i);
                if(timeCfg != null){
                    info.setMatchEndTime(timeCfg.getMatchEndTime());
                    info.setBattleStartTime(timeCfg.getBattleTime());
                    info.setBattleEndTime(timeCfg.getSettleTime());
                }else {
                    info.setMatchEndTime(now);
                    info.setBattleStartTime(now);
                    info.setBattleEndTime(now);
                }
                time1.addInfos(info);
            }
            for(int i = cfg.getRankingTermId(); i <= cfg.getEndTermId(); i++){
                PBCommonMatch.PBCMWBattleTimeInfo.Builder info = PBCommonMatch.PBCMWBattleTimeInfo.newBuilder();
                info.setTermId(i - cfg.getRankingTermId() + 1);
                XHJZWarTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(XHJZWarTimeCfg.class, i);
                if(timeCfg != null){
                    info.setMatchEndTime(timeCfg.getMatchEndTime());
                    info.setBattleStartTime(timeCfg.getBattleTime());
                    info.setBattleEndTime(timeCfg.getSettleTime());
                }else {
                    info.setMatchEndTime(now);
                    info.setBattleStartTime(now);
                    info.setBattleEndTime(now);
                }
                time2.addInfos(info);
            }
        }
        resp.addTimeInfos(time1);
        resp.addTimeInfos(time2);
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.CMW_BATTLE_TIME_RESP, resp));
    }

    @Override
    public void targetInfo(Player player, PBCommonMatch.PBCMWBattleTargetReq req) {
        PBCommonMatch.PBCMWBattleTargetResp.Builder resp = PBCommonMatch.PBCMWBattleTargetResp.newBuilder();
        resp.setMatchType(getMatchType());
        resp.setServerType(getServerType());
        CMWPlayerData playerData = loadPlayerData(player.getId());
        if(playerData == null){
            player.sendProtocol(HawkProtocol.valueOf(HP.code2.CMW_BATTLE_TARGET_RESP, resp));
            return;
        }
        resp.setScore(playerData.teamScore);
        resp.addAllSendAwards(playerData.teamRewarded);
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.CMW_BATTLE_TARGET_RESP, resp));
    }

    public PBCommonMatch.PBCMWPageInfo.Builder genPage(Player player, PBCommonMatch.PBCMWPageInfoReq req){
        PBCommonMatch.PBCMWPageInfo.Builder pageInfo = PBCommonMatch.PBCMWPageInfo.newBuilder();
        pageInfo.setBigState(getClientBigState());
        fillClientState(pageInfo, player);
        fillClientSeasonTime(pageInfo);
        if(req == null || req.getServerType() == getServerType()){
            fillClientTeam(pageInfo, player);
        }
        return pageInfo;
    }




    public PBCommonMatch.PBCMWBigState getClientBigState(){
        switch (getState()){
            case CLOSE:return PBCommonMatch.PBCMWBigState.BIG_COLSE;
            case SIGNUP:return PBCommonMatch.PBCMWBigState.BIG_SIGNUP;
            case QUALIFIER:return PBCommonMatch.PBCMWBigState.BIG_QUALIFIER;
            case RANKING:return PBCommonMatch.PBCMWBigState.BIG_RANKING;
            case END_SHOW:return PBCommonMatch.PBCMWBigState.BIG_END_SHOW;
        }
        return PBCommonMatch.PBCMWBigState.BIG_COLSE;
    }

    public void fillClientState(PBCommonMatch.PBCMWPageInfo.Builder pageInfo, Player player){
        XHJZWar.XWStateInfo.Builder builder =  XHJZWarService.getInstance().getStateInfo(player);
        switch (builder.getState()){
            case XW_PEACE:{
                pageInfo.setState(PBCommonMatch.PBCMWState.PEACE);
            }
            break;
            case XW_SIGNUP:{
                pageInfo.setState(PBCommonMatch.PBCMWState.SIGNUP);
            }
            break;
            case XW_MATCH_WAIT:{
                pageInfo.setState(PBCommonMatch.PBCMWState.MATCH_WAIT);
            }
            break;
            case XW_MATCH:{
                pageInfo.setState(PBCommonMatch.PBCMWState.MATCH);
            }
            break;
            case XW_PREPARE:{
                pageInfo.setState(PBCommonMatch.PBCMWState.PREPARE);
            }
            break;
            case XW_BATTLE:{
                pageInfo.setState(PBCommonMatch.PBCMWState.BATTLE);
            }
            break;
            case XW_FINISH:{
                pageInfo.setState(PBCommonMatch.PBCMWState.FINISH);
            }
            break;
        }
        pageInfo.setTermId(calSeasonTerm());
        pageInfo.setEndTime(builder.getEndTime());
    }

    public int calSeasonTerm(){
        XHJZSeasonTimeCfg cfg = getCfg();
        if(cfg == null){
            return 1;
        }else {
            if(getState() == CMWStateEnum.QUALIFIER){
                return Math.max(1 ,XHJZWarService.getInstance().getTermId() - cfg.getQualifierTermId() + 1);
            }else if(getState() == CMWStateEnum.RANKING){
                return Math.max(1 ,XHJZWarService.getInstance().getTermId() - cfg.getRankingTermId() + 1);
            }else {
                return 1;
            }

        }
    }

    public void fillClientTeam(PBCommonMatch.PBCMWPageInfo.Builder pageInfo, Player player){
        PBCommonMatch.PBCMWTeamInfo.Builder teamInfo = PBCommonMatch.PBCMWTeamInfo.newBuilder();
        String teamId = XHJZWarService.getInstance().getSelfTeamId(player);
        teamInfo.setId(teamId);
        if(getState() == CMWStateEnum.QUALIFIER){
            teamInfo.setRank(rankMap.getOrDefault(teamId, -1));
        }else {
            teamInfo.setRank(rankingRankMap.getOrDefault(teamId, -1));
        }
        CMWData data = loadData(teamId);
        if(data != null){
            if(getState() == CMWStateEnum.QUALIFIER){
                teamInfo.setWinCount(data.winCnt);
                teamInfo.setLoseCount(data.loseCnt);
                teamInfo.setScore(data.score);
                teamInfo.setGroupType(PBCommonMatch.PBCMWGroupType.QUALIFIER_GROUP);
            }else {
                teamInfo.setWinCount(data.rankingWinCnt);
                teamInfo.setLoseCount(data.rankingLoseCnt);
                teamInfo.setScore(data.rankingScore);
                teamInfo.setGroupType(PBCommonMatch.PBCMWGroupType.valueOf(data.group));
            }
        }else {
            if(getState() == CMWStateEnum.QUALIFIER){
                teamInfo.setGroupType(PBCommonMatch.PBCMWGroupType.QUALIFIER_GROUP);
            }else {
                teamInfo.setGroupType(PBCommonMatch.PBCMWGroupType.NOMAL_GROUP);
            }
        }
        pageInfo.setSelfTeam(teamInfo);
    }

    public void fillClientSeasonTime(PBCommonMatch.PBCMWPageInfo.Builder pageInfo){
        XHJZSeasonTimeCfg cfg = getCfg();
        if(cfg == null){
            long now = HawkTime.getMillisecond();
            pageInfo.setSeasonStartTime(now + TimeUnit.DAYS.toMillis(10));
            pageInfo.setSignUpEndTime(now + TimeUnit.DAYS.toMillis(10));
            pageInfo.setQualifierEndTime(now + TimeUnit.DAYS.toMillis(20));
            pageInfo.setRankingEndTime(now + TimeUnit.DAYS.toMillis(30));
            pageInfo.setSeasonEndTime(now + TimeUnit.DAYS.toMillis(40));
            pageInfo.setQualifierMax(100);
            pageInfo.setRankingEndMax(100);
        }else {
            pageInfo.setSeasonStartTime(cfg.getSeasonStartTimeValue());
            pageInfo.setSignUpEndTime(cfg.getSeasonStartTimeValue());
            pageInfo.setQualifierEndTime(cfg.getQualifierEndTimeValue()+1000);
            pageInfo.setRankingEndTime(cfg.getRankingEndTimeValue());
            pageInfo.setSeasonEndTime(cfg.getSeasonEndTimeValue());
            pageInfo.setQualifierMax(cfg.getRankingTermId() - cfg.getQualifierTermId());
            pageInfo.setRankingEndMax(cfg.getEndTermId() - cfg.getRankingTermId() + 1);
        }
    }

    PBCommonMatch.PBCMWRankInfoResp.Builder newQualifierRank;
    PBCommonMatch.PBCMWRankInfoResp.Builder oldQualifierRank;
    Map<String, PBCommonMatch.PBCMWTeamInfo.Builder> newQualifierInfoMap;
    Map<String, PBCommonMatch.PBCMWTeamInfo.Builder> oldQualifierInfoMap;

    PBCommonMatch.PBCMWRankInfoResp.Builder newRankingRank;
    PBCommonMatch.PBCMWRankInfoResp.Builder oldRankingRank;
    Map<String, PBCommonMatch.PBCMWTeamInfo.Builder> newRankingInfoMap;
    Map<String, PBCommonMatch.PBCMWTeamInfo.Builder> oldRankingInfoMap;

    public void onSignup(){
        if(getState() == CMWStateEnum.RANKING){
            loadAllJoinDatas();
            XHJZSeasonConst seasonConst = HawkConfigManager.getInstance().getKVInstance(XHJZSeasonConst.class);
            for(String teamId : oldMap.keySet()){
                XHJZWarService.getInstance().seasonSignup(teamId, seasonConst.getSeasonTimeIndex());
            }
            for(String teamId : newMap.keySet()){
                XHJZWarService.getInstance().seasonSignup(teamId, seasonConst.getSeasonTimeIndex());
            }
        }
    }

    public void onQualifierMatch(){
        int termId = XHJZWarService.getInstance().getTermId();
        HawkLog.logPrintln("XHJZWarService onMatch start, termId:{}",termId);
        //抢锁
        String serverId = GsConfig.getInstance().getServerId();
        String matchKey = String.format(XHJZRedisKey.XHJZ_WAR_MATCH, termId);
        boolean getLock = RedisProxy.getInstance().getRedisSession().setNx(matchKey, serverId);
        if(!getLock){
            HawkLog.logPrintln("XHJZWarService onMatch get lock fail, termId:{}",termId);
            return;
        }
        HawkLog.logPrintln("XHJZWarService onMatch real start, termId:{}",termId);
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        Map<String, Set<String>> serverIdToRoomIdMap = new HashMap<>();
        Map<String, String> roomStrMap = new HashMap<>();
        List<XHJZWarTeamData> noMatchList = new ArrayList<>();
        for(int i = 1; i <= constCfg.getWarCount(); i++){
            String signUpTimeKey = String.format(XHJZRedisKey.XHJZ_WAR_SIGNUP_TIME, termId, i);
            Set<String> teamIds = RedisProxy.getInstance().getRedisSession().sMembers(signUpTimeKey);
            if(teamIds == null || teamIds.isEmpty()){
                continue;
            }
            List<String> list = RedisProxy.getInstance().getRedisSession().hmGet(XHJZRedisKey.XHJZ_WAR_TEAM, teamIds.toArray(new String[teamIds.size()]));
            List<XHJZWarTeamData> newTeamList = new ArrayList<>();
            List<XHJZWarTeamData> oldTeamList = new ArrayList<>();
            for(String json : list){
                XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(json);
                if(teamData == null){
                    continue;
                }
                if(teamData.isNew){
                    newTeamList.add(teamData);
                }else {
                    oldTeamList.add(teamData);
                }
            }
            createRooms(termId, i, newTeamList, roomStrMap, serverIdToRoomIdMap, true, PBCommonMatch.PBCMWGroupType.QUALIFIER_GROUP_VALUE, 0);
            createRooms(termId, i, oldTeamList, roomStrMap, serverIdToRoomIdMap, false, PBCommonMatch.PBCMWGroupType.QUALIFIER_GROUP_VALUE, 0);
            if(!newTeamList.isEmpty()){
                noMatchList.addAll(newTeamList);
            }
            if(!oldTeamList.isEmpty()){
                noMatchList.addAll(oldTeamList);
            }
        }
        if(!roomStrMap.isEmpty()){
            RedisProxy.getInstance().getRedisSession().hmSet(String.format(XHJZRedisKey.XHJZ_WAR_ROOM, termId), roomStrMap, 0);
        }
        for(String toServerId : serverIdToRoomIdMap.keySet()){
            Set<String> roomIds = serverIdToRoomIdMap.get(toServerId);
            RedisProxy.getInstance().getRedisSession().sAdd(String.format(XHJZRedisKey.XHJZ_WAR_ROOM_SERVER, termId, toServerId), 0, roomIds.toArray(new String[roomIds.size()]));
        }
        List<String> noMatchIds = new ArrayList<>();
        for(XHJZWarTeamData teamData : noMatchList){
           noMatchIds.add(teamData.id);
       }
        if(!noMatchIds.isEmpty()){
            RedisProxy.getInstance().getRedisSession().sAdd(String.format(XHJZRedisKey.XHJZ_WAR_NO_MATCH, termId), 0, noMatchIds.toArray(new String[0]));
        }
    }

    public void onRankingMatch(){
        int termId = XHJZWarService.getInstance().getTermId();
        HawkLog.logPrintln("XHJZWarService onMatch start, termId:{}",termId);
        //抢锁
        String serverId = GsConfig.getInstance().getServerId();
        String matchKey = String.format(XHJZRedisKey.XHJZ_WAR_MATCH, termId);
        boolean getLock = RedisProxy.getInstance().getRedisSession().setNx(matchKey, serverId);
        if(!getLock){
            HawkLog.logPrintln("XHJZWarService onMatch get lock fail, termId:{}",termId);
            return;
        }
        HawkLog.logPrintln("XHJZWarService onMatch real start, termId:{}",termId);
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        Map<String, Set<String>> serverIdToRoomIdMap = new HashMap<>();
        Map<String, String> roomStrMap = new HashMap<>();
        List<XHJZWarTeamData> noMatchList = new ArrayList<>();
        for(int i = 1; i <= constCfg.getWarCount(); i++){
            String signUpTimeKey = String.format(XHJZRedisKey.XHJZ_WAR_SIGNUP_TIME, termId, i);
            Set<String> teamIds = RedisProxy.getInstance().getRedisSession().sMembers(signUpTimeKey);
            if(teamIds == null || teamIds.isEmpty()){
                continue;
            }
            List<String> list = RedisProxy.getInstance().getRedisSession().hmGet(XHJZRedisKey.XHJZ_WAR_TEAM, teamIds.toArray(new String[teamIds.size()]));
            List<XHJZWarTeamData> normalTeamList = new ArrayList<>();
            Map<Integer, List<XHJZWarTeamData>> newSTeamListMap = new HashMap<>();
            Map<Integer, List<XHJZWarTeamData>> oldSTeamListMap = new HashMap<>();
            Map<Integer, List<XHJZWarTeamData>> newATeamListMap = new HashMap<>();
            Map<Integer, List<XHJZWarTeamData>> oldATeamListMap = new HashMap<>();
            Map<Integer, List<XHJZWarTeamData>> newBTeamListMap = new HashMap<>();
            Map<Integer, List<XHJZWarTeamData>> oldBTeamListMap = new HashMap<>();
            for(String json : list){
                XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(json);
                if(teamData == null){
                    continue;
                }
                if(oldMap.containsKey(teamData.id)){
                    CMWData seasonData = oldMap.get(teamData.id);
                    if(seasonData.group == PBCommonMatch.PBCMWGroupType.S_GROUP_VALUE){
                        putToCountMap(oldSTeamListMap, teamData, seasonData);
                    }else if(seasonData.group == PBCommonMatch.PBCMWGroupType.A_GROUP_VALUE){
                        putToCountMap(oldATeamListMap, teamData, seasonData);
                    }else {
                        putToCountMap(oldBTeamListMap, teamData, seasonData);
                    }
                }else if(newMap.containsKey(teamData.id)){
                    CMWData seasonData = newMap.get(teamData.id);
                    if(seasonData.group == PBCommonMatch.PBCMWGroupType.S_GROUP_VALUE){
                        putToCountMap(newSTeamListMap, teamData, seasonData);
                    }else if(seasonData.group == PBCommonMatch.PBCMWGroupType.A_GROUP_VALUE){
                        putToCountMap(newATeamListMap, teamData, seasonData);
                    }else {
                        putToCountMap(newBTeamListMap, teamData, seasonData);
                    }
                }else {
                    normalTeamList.add(teamData);
                }

            }
            for(int count : oldSTeamListMap.keySet()){
                List<XHJZWarTeamData> teamList = oldSTeamListMap.get(count);
                createRooms(termId, i, teamList, roomStrMap, serverIdToRoomIdMap, false, PBCommonMatch.PBCMWGroupType.S_GROUP_VALUE, count);
                noMatchList.addAll(teamList);
            }
            for(int count : newSTeamListMap.keySet()){
                List<XHJZWarTeamData> teamList = newSTeamListMap.get(count);
                createRooms(termId, i, teamList, roomStrMap, serverIdToRoomIdMap, true, PBCommonMatch.PBCMWGroupType.S_GROUP_VALUE, count);
                noMatchList.addAll(teamList);
            }
            for(int count : oldATeamListMap.keySet()){
                List<XHJZWarTeamData> teamList = oldATeamListMap.get(count);
                createRooms(termId, i, teamList, roomStrMap, serverIdToRoomIdMap, false, PBCommonMatch.PBCMWGroupType.A_GROUP_VALUE, count);
                noMatchList.addAll(teamList);
            }
            for(int count : newATeamListMap.keySet()){
                List<XHJZWarTeamData> teamList = newATeamListMap.get(count);
                createRooms(termId, i, teamList, roomStrMap, serverIdToRoomIdMap, true, PBCommonMatch.PBCMWGroupType.A_GROUP_VALUE, count);
                noMatchList.addAll(teamList);
            }
            for(int count : oldBTeamListMap.keySet()){
                List<XHJZWarTeamData> teamList = oldBTeamListMap.get(count);
                createRooms(termId, i, teamList, roomStrMap, serverIdToRoomIdMap, false, PBCommonMatch.PBCMWGroupType.B_GROUP_VALUE, count);
                noMatchList.addAll(teamList);
            }
            for(int count : newBTeamListMap.keySet()){
                List<XHJZWarTeamData> teamList = newBTeamListMap.get(count);
                createRooms(termId, i, teamList, roomStrMap, serverIdToRoomIdMap, true, PBCommonMatch.PBCMWGroupType.B_GROUP_VALUE, count);
                noMatchList.addAll(teamList);
            }
            createRooms(termId, i, normalTeamList, roomStrMap, serverIdToRoomIdMap, false, 0, 0);
        }
        if(!roomStrMap.isEmpty()){
            RedisProxy.getInstance().getRedisSession().hmSet(String.format(XHJZRedisKey.XHJZ_WAR_ROOM, termId), roomStrMap, 0);
        }
        for(String toServerId : serverIdToRoomIdMap.keySet()){
            Set<String> roomIds = serverIdToRoomIdMap.get(toServerId);
            RedisProxy.getInstance().getRedisSession().sAdd(String.format(XHJZRedisKey.XHJZ_WAR_ROOM_SERVER, termId, toServerId), 0, roomIds.toArray(new String[roomIds.size()]));
        }
//        for(XHJZWarTeamData teamData : noMatchList){
//            addScore(teamData.id, 0, true);
//        }
    }

    public void putToCountMap(Map<Integer, List<XHJZWarTeamData>> countMap, XHJZWarTeamData teamData,CMWData seasonData){
        int winCount = seasonData.rankingWinCnt;
        if(!countMap.containsKey(winCount)){
            countMap.put(winCount, new ArrayList<>());
        }
        countMap.get(winCount).add(teamData);
    }

    public void createRooms(int termId, int i, List<XHJZWarTeamData> teamList, Map<String, String> roomStrMap, Map<String, Set<String>> serverIdToRoomIdMap, boolean isNew, int group, int count){
        teamList.sort((o1, o2) -> {
            if (o1.seasonScore != o2.seasonScore) {
                return o1.seasonScore > o2.seasonScore ? -1 : 1;
            }
            if (o1.matchPower != o2.matchPower) {
                return o1.matchPower > o2.matchPower ? -1 : 1;
            }
            return 0;
        });
        
        String str = RedisProxy.getInstance().getRedisSession().getString("xhjz_gm_match_20250626");
        Map<String,String> gmMatchMap = SerializeHelper.stringToMap(str, String.class, String.class, SerializeHelper.ATTRIBUTE_SPLIT, SerializeHelper.BETWEEN_ITEMS);
        
        while (teamList.size() > 1){
        	/************************GM匹配***************************************************/
        	HawkTuple2<XHJZWarTeamData, XHJZWarTeamData> result = null;
        	if(gmMatchMap.size() > 0){
        		for(Map.Entry<String, String> gmMatch : gmMatchMap.entrySet()){
            		String t1 = gmMatch.getKey();
            		String t2 = gmMatch.getValue();
            		XHJZWarTeamData team1 = null;
            		XHJZWarTeamData team2 = null;
            		for(XHJZWarTeamData teamData : teamList){
            			if(teamData.id.equals(t1)){
            				team1 = teamData;
            			}
            			if(teamData.id.equals(t2)){
            				team2 = teamData;
            			}
            		}
            		if(team1 != null && team2 != null){
            			result = new HawkTuple2<XHJZWarTeamData, XHJZWarTeamData>(team1, team2);
            			HawkLog.logPrintln("XHJZSeasonManager-xhjz_gm_match_20250626,{}-{}",team1.id,team2.id);
            		}
            	}
        	}
        	/************************GM匹配***************************************************/
        	if(Objects.isNull(result)){
        		 result = matchTeam(teamList);
        	}
            if(result == null){
                break;
            }
            if(result.first != null){
                teamList.remove(result.first);
            }
            if(result.second != null){
                teamList.remove(result.second);
            }
            XHJZWarTeamData teamData1 = result.first;
            XHJZWarTeamData teamData2 = result.second;
            if(teamData1 != null && teamData2 != null){
                teamData1.oppTeamId = teamData2.id;
                teamData2.oppTeamId = teamData1.id;
                XHJZWarRoomData roomData = new XHJZWarRoomData(termId, i, teamData1, teamData2);
                roomData.roomServerId = teamData1.serverId.compareTo(teamData2.serverId) < 0 ? teamData1.serverId : teamData2.serverId;
                roomData.isNew = isNew;
                roomData.group = group;
                roomData.winCount = count;
                teamData1.update();
                teamData2.update();
                roomStrMap.put(roomData.id, roomData.serialize());
                XHJZWarService.getInstance().updateRoomIdToServer(serverIdToRoomIdMap, teamData1.serverId, roomData.id);
                XHJZWarService.getInstance().updateRoomIdToServer(serverIdToRoomIdMap, teamData2.serverId, roomData.id);
                XHJZWarService.getInstance().updateRoomIdToServer(serverIdToRoomIdMap, roomData.roomServerId, roomData.id);
                XHJZSeasonManager.getInstance().updateBattleInfo(teamData1, teamData2, roomData);
            }
        }
    }

    public HawkTuple2<XHJZWarTeamData, XHJZWarTeamData> matchTeam(List<XHJZWarTeamData> teamList){
        if(teamList == null || teamList.isEmpty()){
            return null;
        }
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        int teamCount = constCfg.getMatchParam();
        teamCount = Math.min(teamCount, teamList.size());
        List<XHJZWarTeamData> subList = teamList.subList(0, teamCount);
        XHJZWarTeamData teamData1 = subList.get(0);
        List<XHJZWarTeamData> tmpList = new ArrayList<>();
        for(int i = 1; i < subList.size(); i++){
            XHJZWarTeamData tmpData = subList.get(i);
            if(tmpData.guildId.equals(teamData1.guildId)){
                continue;
            }
            tmpList.add(tmpData);
        }
        if(tmpList.isEmpty()){
            return new HawkTuple2<>(teamData1, null);
        }
        Collections.shuffle(tmpList);
        XHJZWarTeamData teamData2 = tmpList.get(0);
        return new HawkTuple2<>(teamData1, teamData2);
    }

    public void onMatch(){
       if(getState() == CMWStateEnum.RANKING){
           onRankingMatch();
       }else {
           onQualifierMatch();
       }
    }

    public void onEnd(){
        rankMap.clear();
        rankingRankMap.clear();
        if(getState() == CMWStateEnum.QUALIFIER){
            loadQualifierRank(false);
            loadQualifierRank(true);
        }
        if(getState() == CMWStateEnum.RANKING){
            loadAllJoinDatas();
            loadRankingRank(false);
            loadRankingRank(true);
        }
        XHJZSeasonTimeCfg cfg = getCfg();
        int termId = XHJZWarService.getInstance().getTermId();
        if(cfg != null && termId == (cfg.getRankingTermId() - 1)){
            calQualifier();
        }
        if(cfg != null && termId == cfg.getEndTermId()){
            calFinal();
        }
    }



    public void loadQualifierRank(boolean isNew){
        Set<Tuple> tuples  = RedisProxy.getInstance().getRedisSession().zRevrangeWithScores(getRankKey(isNew), 0, 999,0);
        Set<String> teamIds = new HashSet<>();
        for (Tuple tuple : tuples) {
            teamIds.add(tuple.getElement());
        }
        Map<String, CMWData> dataMap = loadDatas(teamIds);
        Map<String, XHJZWarTeamData> teamDataMap = loadTeams(teamIds);
        Map<String, PBCommonMatch.PBCMWTeamInfo.Builder> infoMap = new HashMap<>();
        PBCommonMatch.PBCMWRankInfoResp.Builder resp = PBCommonMatch.PBCMWRankInfoResp.newBuilder();
        resp.setMatchType(PBCommonMatch.PBCMWMatchType.XHJZ_SEASON);
        resp.setServerType(isNew ? PBCommonMatch.PBCMWServerType.NEW_SERVER : PBCommonMatch.PBCMWServerType.OLD_SERVER);
        resp.setBattleType(PBCommonMatch.PBCMWBattleType.QUALIFIER);
        int i = 1;
        for (Tuple tuple : tuples) {
            PBCommonMatch.PBCMWTeamInfo.Builder info = PBCommonMatch.PBCMWTeamInfo.newBuilder();
            String teamId = tuple.getElement();
            long score = Math.round(tuple.getScore());
            XHJZWarTeamData teamData = teamDataMap.get(teamId);
            if(teamData == null){
                continue;
            }
            CMWData cmwData = dataMap.get(teamId);
            if(cmwData == null){
                continue;
            }
            info.setRank(i);
            info.setId(teamId);
            info.setName(teamData.name);
            info.setGuildName(teamData.guildName);
            info.setGuildTag(teamData.guildTag);
            info.setServerId(teamData.serverId);
            info.setWinCount(cmwData.winCnt);
            info.setLoseCount(cmwData.loseCnt);
            info.setScore(score);
            info.setGroupType(PBCommonMatch.PBCMWGroupType.QUALIFIER_GROUP);
            resp.addRankInfos(info);
            infoMap.put(teamId, info);
            rankMap.put(teamId, i);
            i++;
        }
        if(isNew){
            newQualifierRank = resp;
            newQualifierInfoMap = infoMap;
        }else {
            oldQualifierRank = resp;
            oldQualifierInfoMap = infoMap;
        }
    }

    public void loadRankingRank(boolean isNew){
        Map<String, PBCommonMatch.PBCMWTeamInfo.Builder> infoMap = new HashMap<>();
        PBCommonMatch.PBCMWRankInfoResp.Builder resp = PBCommonMatch.PBCMWRankInfoResp.newBuilder();
        resp.setMatchType(PBCommonMatch.PBCMWMatchType.XHJZ_SEASON);
        resp.setServerType(isNew ? PBCommonMatch.PBCMWServerType.NEW_SERVER : PBCommonMatch.PBCMWServerType.OLD_SERVER);
        resp.setBattleType(PBCommonMatch.PBCMWBattleType.RANKING);
        List<CMWData> dataList = isNew ? new ArrayList<>(newMap.values()) : new ArrayList<>(oldMap.values());
        dataList.sort(( o1, o2) -> {
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
        Set<String> teamIds = isNew ? newMap.keySet() : oldMap.keySet();
        Map<String, XHJZWarTeamData> teamDataMap = loadTeams(teamIds);
        int i = 1;
        for(CMWData data : dataList){
            PBCommonMatch.PBCMWTeamInfo.Builder info = PBCommonMatch.PBCMWTeamInfo.newBuilder();
            String teamId = data.teamId;
            XHJZWarTeamData teamData = teamDataMap.get(teamId);
            if(teamData == null){
                continue;
            }
            info.setRank(i);
            info.setId(teamId);
            info.setName(teamData.name);
            info.setGuildName(teamData.guildName);
            info.setGuildTag(teamData.guildTag);
            info.setServerId(teamData.serverId);
            info.setWinCount(data.rankingWinCnt);
            info.setLoseCount(data.rankingLoseCnt);
            info.setScore(data.rankingScore);
            info.setGroupType(PBCommonMatch.PBCMWGroupType.valueOf(data.group));
            resp.addRankInfos(info);
            infoMap.put(teamId, info);
            if(data.rankingWinCnt >0 || data.rankingLoseCnt>0) {
                rankingRankMap.put(teamId, i);
            }
            i++;
        }
        if(isNew){
            newRankingRank= resp;
            newRankingInfoMap = infoMap;
        }else {
            oldRankingRank = resp;
            oldRankingInfoMap = infoMap;
        }
    }


    public Map<String, XHJZWarTeamData> loadTeams(Collection<String> teamIds){
        Map<String, XHJZWarTeamData> teamDataMap = new HashMap<>();
        if(teamIds == null || teamIds.isEmpty()){
            return teamDataMap;
        }
        List<String> list = RedisProxy.getInstance().getRedisSession().hmGet(XHJZRedisKey.XHJZ_WAR_TEAM, teamIds.toArray(new String[0]));
        for(String json : list){
            XHJZWarTeamData data = XHJZWarTeamData.unSerialize(json);
            if(data == null){
                continue;
            }
            teamDataMap.put(data.id, data);
        }
        return teamDataMap;
    }

    public Map<Integer, CMWBattleInfo> loadQualifierBattleInfo(String key){
        Map<Integer, CMWBattleInfo> infoMap = new HashMap<>();
        Map<String, String> map = RedisProxy.getInstance().getRedisSession().hGetAll(key);
        for(String json : map.values()){
            CMWBattleInfo battleInfo = CMWBattleInfo.unSerialize(json);
            if(battleInfo == null){
                continue;
            }
            infoMap.put(battleInfo.termId, battleInfo);
        }
        return infoMap;
    }

    public Map<String, CMWBattleInfo> loadRankingBattleInfo(String key){
        Map<String, CMWBattleInfo> infoMap = new HashMap<>();
        Map<String, String> map = RedisProxy.getInstance().getRedisSession().hGetAll(key);
        for(String json : map.values()){
            CMWBattleInfo battleInfo = CMWBattleInfo.unSerialize(json);
            if(battleInfo == null){
                continue;
            }
            infoMap.put(battleInfo.roomId, battleInfo);
        }
        return infoMap;
    }

    @Override
    public void sendOpenMail() {
        long now = HawkTime.getMillisecond();
        SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
                        .setMailId(MailConst.MailId.XHJZ_SEASON_2024072502)
                        .addContents(getServerType().getNumber())
                        .build()
                , now, now + TimeUnit.DAYS.toMillis(7));
    }

    @Override
    public void sendQualifierMail() {
        sendQualifierMail(oldMap);
        sendQualifierMail(newMap);
    }


    public void sendQualifierMail(Map<String, CMWData> dataMap){
        for(String teamId : dataMap.keySet()) {
            try {
                if (!XHJZWarService.getInstance().isLocalTeam(teamId)) {
                    continue;
                }
                CMWData data = dataMap.get(teamId);
                int serverType = getServerType().getNumber();
                long score = data.score;
                int rank = data.qualifierRank;
                int groupType = data.group;

                Map<String, Object> param = new HashMap<>();
                param.put("teamId", teamId);
                param.put("score", score);
                param.put("rank", rank);
                param.put("serverType", serverType);
                param.put("groupType", data.group);
                LogUtil.logActivityCommon(LogConst.LogInfoType.xhjz_season_qualifier, param);

                Set<String> playerIds = XHJZWarService.getInstance().getTeamPlayerIds(teamId);
                if (playerIds == null || playerIds.isEmpty()) {
                    continue;
                }
                HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
                    @Override
                    public Object run() {
                        // 匹配成功邮件
                        for (String playerId : playerIds) {
                            SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                                    .setPlayerId(playerId)
                                    .setMailId(MailConst.MailId.XHJZ_SEASON_2024072503)
                                    .addContents(serverType, score, rank, groupType).build());
                        }
                        return null;
                    }
                });
            } catch (Exception e) {
                HawkException.catchException(e);
            }
        }
    }

    @Override
    public void sendFinalMail() {
        HawkLog.logPrintln("XHJZSeasonManager sendFinalMail start");
        sendFinalMail(oldMap);
        sendFinalMail(newMap);
        HawkLog.logPrintln("XHJZSeasonManager sendFinalMail end");
    }

    public void sendFinalMail(Map<String, CMWData> dataMap){
        try {
            HawkLog.logPrintln("XHJZSeasonManager sendFinalMail start, size:{}", dataMap.size());
            SeasonActivity activity = null;
            Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY.intValue());
            if (opActivity.isPresent()) {
                activity = opActivity.get();
            }
            Map<String, Integer> guildIdToRank = new HashMap<>();
            for(String teamId : dataMap.keySet()){
                try {
                    if(!XHJZWarService.getInstance().isLocalTeam(teamId)){
                        HawkLog.logPrintln("XHJZSeasonManager sendFinalMail team is not local, teamId:{}", teamId);
                        continue;
                    }
                    HawkLog.logPrintln("XHJZSeasonManager sendFinalMail team start, teamId:{}", teamId);
                    CMWData data = dataMap.get(teamId);
                    if(data == null){
                        HawkLog.logPrintln("XHJZSeasonManager sendFinalMail team data is null, teamId:{}", teamId);
                        continue;
                    }
                    int serverType = getServerType().getNumber();
                    int groupType = data.group;
                    int winCount = data.rankingWinCnt;
                    int loseCount = data.rankingLoseCnt;
                    int rank = data.rank;
                    HawkLog.logPrintln("XHJZSeasonManager sendFinalMail team mail team, teamId:{}, serverType:{}, groupType:{},winCount:{},loseCount:{},rank:{}",
                            teamId, serverType, groupType, winCount, loseCount, rank);
                    Map<String, Object> param = new HashMap<>();
                    param.put("teamId", teamId);
                    param.put("winCount", winCount);
                    param.put("loseCount", loseCount);
                    param.put("rank", rank);
                    param.put("serverType", serverType);
                    param.put("groupType", groupType);
                    LogUtil.logActivityCommon(LogConst.LogInfoType.xhjz_season_final, param);
                    XHJZWarTeamData teamData = XHJZWarService.getInstance().getTeamData(teamId);
                    if(teamData != null){
                        int guildRank = guildIdToRank.getOrDefault(teamData.guildId, Integer.MAX_VALUE);
                        if(data.rank < guildRank){
                            guildRank = data.rank;
                            guildIdToRank.put(teamData.guildId, guildRank);
                        }
                    }
                    XHJZSeasonRankRewrdCfg rankRewrdCfg = XHJZSeasonRankRewrdCfg.getRankCfg(serverType, rank);
                    if(rankRewrdCfg == null){
                        HawkLog.logPrintln("XHJZSeasonManager sendFinalMail team rankRewrdCfg is null, teamId:{}, rank:{}", teamId, rank);
                        continue;
                    }
                    Set<String> playerIds = XHJZWarService.getInstance().getTeamPlayerIds(teamId);
                    if(playerIds == null || playerIds.isEmpty()){
                        HawkLog.logPrintln("XHJZSeasonManager sendFinalMail team playerIds is empty, teamId:{}", teamId);
                        continue;
                    }
                    HawkLog.logPrintln("XHJZSeasonManager sendFinalMail team playerIds, teamId:{}, size:{}", teamId, playerIds.size());
                    HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
                        @Override
                        public Object run() {
                            // 匹配成功邮件
                            for (String playerId : playerIds) {
                                try {
                                    HawkLog.logPrintln("XHJZSeasonManager sendFinalMail team mail player, teamId:{}, playerId:{}, serverType:{}, groupType:{},winCount:{},loseCount:{},rank:{}",
                                            teamId, playerId, serverType, groupType, winCount, loseCount, rank);
                                    SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                                            .setPlayerId(playerId)
                                            .setMailId(MailConst.MailId.XHJZ_SEASON_2024072506)
                                            .addContents(serverType, groupType, winCount, loseCount, rank)
                                            .setAwardStatus(Const.MailRewardStatus.NOT_GET)
                                            .addRewards(rankRewrdCfg.getRewardItem())
                                            .build());
                                } catch (Exception e) {
                                    HawkLog.logPrintln("XHJZSeasonManager sendFinalMail team mail player error, teamId:{}, playerId:{}", teamId, playerId);
                                    HawkException.catchException(e);
                                }
                            }
                            return null;
                        }
                    });
                    HawkLog.logPrintln("XHJZSeasonManager sendFinalMail team end, teamId:{}", teamId);
                } catch (Exception e) {
                    HawkLog.logPrintln("XHJZSeasonManager sendFinalMail team error, teamId:{}", teamId);
                    HawkException.catchException(e);
                }
            }
            if(activity != null){
                for(String guildId : guildIdToRank.keySet()){
                    try {
                        int guildRank = guildIdToRank.getOrDefault(guildId, Integer.MAX_VALUE);
                        activity.addGuildGradeExpFromMatchRank(Activity.SeasonMatchType.S_XHJZ, guildId, guildRank);
                        HawkLog.logPrintln("XHJZSeasonManager sendFinalMail guild, guildId:{}, guildRank:{}", guildId, guildRank);
                    } catch (Exception e) {
                        HawkException.catchException(e);
                    }
                }
            }
            HawkLog.logPrintln("XHJZSeasonManager sendFinalMail end");
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * gm入口
     * @param map gm参数
     * @return 活动信息
     */
    @Override
    public String gm(Map<String, String> map){
        //要执行的gm指令
        String cmd = map.getOrDefault("cmd", "");
        switch (cmd){
            case "info":{
                return printInfo();
            }
            case "next":{
                stateData.toNext();
                return printInfo();
            }
            case "setSeason":{
                int season = Integer.parseInt(map.get("season"));
                stateData.setSeason(season);
                return printInfo();
            }
            case "addSeason":{
                int add = Integer.parseInt(map.get("add"));
                if(add == 0){
                    stateData.setSeason(0);
                }else {
                    stateData.setSeason(stateData.getSeason() + add);
                }
                return printInfo();
            }
            case "load":{
                if(getState() == CMWStateEnum.QUALIFIER){
                    loadQualifierRank(false);
                    loadQualifierRank(true);
                }
                if(getState() == CMWStateEnum.RANKING){
                    loadRankingRank(false);
                    loadRankingRank(true);
                }
                return printInfo();
            }
            case "allOne":{
                List<String> guildIds = GuildService.getInstance().getGuildIds();
                guildIds.forEach(id -> {
                    try {
                        GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(id);
                        if(guildInfoObject!=null){
                            Player leader = GlobalData.getInstance().makesurePlayer(guildInfoObject.getLeaderId());
                            XHJZWarService.getInstance().memberList(leader);
                            XHJZWar.XWMemberManagerReq.Builder req = XHJZWar.XWMemberManagerReq.newBuilder();
                            req.setAuth(XHJZWar.XWPlayerAuth.XW_COMMAND);
                            req.setTeamId(id+":1");
                            req.setPlayerId(leader.getId());
                            XHJZWarService.getInstance().memberManager(leader, req.build());
                            for(String memberId : GuildService.getInstance().getGuildMembers(id)){
                                if(memberId.equals(leader.getId())){
                                    continue;
                                }
                                XHJZWar.XWMemberManagerReq.Builder builder = XHJZWar.XWMemberManagerReq.newBuilder();
                                builder.setAuth(XHJZWar.XWPlayerAuth.XW_STARTER);
                                builder.setTeamId(id+":1");
                                builder.setPlayerId(memberId);
                                XHJZWarService.getInstance().memberManager(leader, builder.build());
                            }
                        }
                    }catch (Exception e){
                        HawkException.catchException(e);
                    }
                });
                return printInfo();
            }
            case "sendFinalMail":{
                sendFinalMail();
                return printInfo();
            }
        }
        return "";
    }

    public String printInfo(){
        //页面信息
        String info = "";
        try {
            //获取本地InetAddress对象
            InetAddress localAddress = InetAddress.getLocalHost();
            //获取本地IP地址
            String ipAddress = localAddress.getHostAddress();
            //获得gm端口
            int port = GsConfig.getInstance().getGmPort();
            //组装页面信息
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=CMWGM&type=XHJZ_SEASON&cmd=info\">刷新</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=CMWGM&type=XHJZ_SEASON&cmd=next\">切阶段</a>          ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=CMWGM&type=XHJZ_SEASON&cmd=addSeason&add=1\">加赛季</a>          ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=CMWGM&type=XHJZ_SEASON&cmd=addSeason&add=0\">赛季归0</a>          ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=CMWGM&type=XHJZ_SEASON&cmd=allOne\">全员进1队</a>          ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=CMWGM&type=XHJZ_SEASON&cmd=sendFinalMail\">发最终奖励</a>          ";
            info +="<br><br>";
            info += stateData.toString() + "<br>";
        } catch (Exception e) {
            HawkException.catchException(e);
        }
        return info;
    }
}
