package com.hawk.game.gmscript;

import com.hawk.activity.type.impl.seasonActivity.cfg.SeasonOpenTimeCfg;
import com.hawk.activity.type.impl.seasonActivity.data.SeasonActivityGuildGradeData;
import com.hawk.activity.type.impl.seasonActivity.rank.GuildSeasonKingGradeInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZSeasonServer;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.YQZZWar;
import com.hawk.game.service.GuildService;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import java.util.*;

public class SeasonHonorGmHandler extends HawkScript {
	/**
	   wx预发布（yqzz，swNew，seasonNew1）
		curl 'http://127.0.0.1:9001/script/SeasonHonorGm?opt=yqzz&season=3&honorServers=10166,10144,10041,10545,10002,10009'
		curl 'http://127.0.0.1:9001/script/SeasonHonorGm?opt=swNew&season=3&honorGuilds=1,%E5%8B%87%E6%95%A2%E7%89%9B%E7%89%9B,21,%E7%A0%B4%E6%99%93%E4%B8%B6%E6%9C%A8%E5%AD%90,%E6%94%BE%E7%89%9B%E7%8F%AD,10002;2,Red-Devils,24,%E7%BA%A2%E9%AD%94%E4%B8%B6%E5%B0%8F%E9%AD%94%E9%AC%BC,%E7%BA%A2%E9%AD%94%E4%B8%B6,10029;3,%E6%A8%AA%E6%89%AB%E5%A4%A9%E4%B8%8B,1,%E9%95%87%E5%A4%A9%E4%B8%8B%E4%B8%A8%E9%98%BF%E4%B9%9D,%E5%AF%92%E6%AD%A6%E7%BA%AA,10144;4,%E6%98%9F%E7%A9%BA%E5%AF%B0%E5%AE%87,25,%E6%98%9F%E4%B8%B6simIyn,%E6%98%9F%E4%B8%B6%E7%A9%BA,10166;5,%E5%8B%87%E6%95%A2%E7%89%9B%E7%89%9B,21,%E7%A0%B4%E6%99%93%E4%B8%B6%E6%9C%A8%E5%AD%90,%E6%94%BE%E7%89%9B%E7%8F%AD,10002'
		curl 'http://127.0.0.1:9001/script/SeasonHonorGm?opt=seasonNew1&season=3'
	
	   qq预发布（yqzz，swNew，seasonNew1）
		curl 'http://127.0.0.1:9001/script/SeasonHonorGm?opt=yqzz&season=3&honorServers=20119,20015,20186,20004,20198,20144'
		curl 'http://127.0.0.1:9001/script/SeasonHonorGm?opt=swNew&season=3&honorGuilds=1,%E6%98%9F%E5%AE%BF%E6%B5%B7,25,%E6%98%9F%E6%B5%B7%E4%B8%B6%E5%A4%9A%E5%A4%9A,%E6%98%9F%E5%AE%BF%E6%B5%B7,20119;2,%E7%BA%B8%E9%86%89%E9%87%91%E8%BF%B7,25,Au%E7%81%AC%E8%89%AF%E6%B0%91,%E9%BB%84%E9%87%91%E5%9F%8E,20072;3,%E6%96%B0%E7%BE%A4%E9%9B%84%E6%97%B6%E4%BB%A3,26,%E7%BE%A4%E9%9B%84%E4%B8%B6%E9%92%A2%E9%93%81,%E6%96%B0%E7%BE%A4%E9%9B%84,20004;4,%E9%BA%92%E6%9C%88%E8%BD%A9,5,%E9%BA%92%E8%BE%B0%E4%B8%B6%E5%B9%B8%E8%BF%90,%E9%BA%92%E8%BE%B0%E4%B9%84,20186;5,%E6%98%9F%E5%AE%BF%E6%B5%B7,25,%E6%98%9F%E6%B5%B7%E4%B8%B6%E5%A4%9A%E5%A4%9A,%E6%98%9F%E5%AE%BF%E6%B5%B7,20119'
		curl 'http://127.0.0.1:9001/script/SeasonHonorGm?opt=seasonNew1&season=3'
	*/
    @Override
    public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
        String opt = params.get("opt");
        int season = Integer.parseInt(params.get("season"));
        int areaId = Integer.parseInt(params.get("channel")); //1代表wx，2代表手Q
        SeasonOpenTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(SeasonOpenTimeCfg.class, season);
        switch (opt){
            case "yqzz":{  /** 月球之战 http://127.0.0.1:9001/script/SeasonHonorGm?opt=yqzz&season=3&honorServers=20119,20015,20186,20004,20198,20144  */
                return gmYQZZ(areaId, params.get("honorServers"), timeCfg);
            }
            case "sw":{ /** 统帅之战  */
               return gmSw(areaId, params.get("honorGuilds"), timeCfg);
            }
            case "season":{ /** 泰伯利亚赛季  */
                return gmSeason(areaId, season);
            }
            case "seasonNew":{
               return gmSeasonNew(areaId, season, params.get("seasonGuilds"));
            }
            case "seasonNew1":{ /** 泰伯利亚赛季  http://127.0.0.1:9001/script/SeasonHonorGm?opt=seasonNew1&season=3 */
               return gmSeasonNew1(areaId, season);
            }
            case "swNew":{ /** http://127.0.0.1:9001/script/SeasonHonorGm?opt=swNew&season=3&honorGuilds=1,%E6%98%9F%E5%AE%BF%E6%B5%B7,25,%E6%98%9F%E6%B5%B7%E4%B8%B6%E5%A4%9A%E5%A4%9A,%E6%98%9F%E5%AE%BF%E6%B5%B7,20119;2,%E7%BA%B8%E9%86%89%E9%87%91%E8%BF%B7,25,Au%E7%81%AC%E8%89%AF%E6%B0%91,%E9%BB%84%E9%87%91%E5%9F%8E,20072;3,%E6%96%B0%E7%BE%A4%E9%9B%84%E6%97%B6%E4%BB%A3,26,%E7%BE%A4%E9%9B%84%E4%B8%B6%E9%92%A2%E9%93%81,%E6%96%B0%E7%BE%A4%E9%9B%84,20004;4,%E9%BA%92%E6%9C%88%E8%BD%A9,5,%E9%BA%92%E8%BE%B0%E4%B8%B6%E5%B9%B8%E8%BF%90,%E9%BA%92%E8%BE%B0%E4%B9%84,20186;5,%E6%98%9F%E5%AE%BF%E6%B5%B7,25,%E6%98%9F%E6%B5%B7%E4%B8%B6%E5%A4%9A%E5%A4%9A,%E6%98%9F%E5%AE%BF%E6%B5%B7,20119 */
                return gmSwNew(areaId, params.get("honorGuilds"), timeCfg);
            }
            case "xhjz":{
            	return gmXHJZ(areaId, timeCfg);
            }
        }
        return HawkScript.successResponse("unknow opt");
    }
    
    
    private String gmYQZZ(int areaId, String honorServers, SeasonOpenTimeCfg timeCfg) {
    	int yqzzTerm = timeCfg.getMatchTermId(Activity.SeasonMatchType.S_YQZZ_VALUE);
        String yqzzHonorKey = "SEASON_HONOR:YQZZ:" + areaId + ":" + yqzzTerm;
        RedisProxy.getInstance().getRedisSession().del(yqzzHonorKey);
        for(String serverId : honorServers.split(",")){
            YQZZSeasonServer server = YQZZSeasonServer.loadByServerId(yqzzTerm, serverId);
            if(server == null){
                continue;
            }
            YQZZWar.PBYQZZLeagueWarServerInfo.Builder serverInfo = YQZZWar.PBYQZZLeagueWarServerInfo.newBuilder();
            serverInfo.setServerId(server.getServerId());
            serverInfo.setServerName(server.getServerName());
            serverInfo.setLeaderName(server.getLeaderName());
            serverInfo.setRank(server.getKickoutRank());
            serverInfo.setWinPoint(server.getTotalPoint());
            serverInfo.setLastRank(0);
            serverInfo.setSeason(yqzzTerm);
            RedisProxy.getInstance().getRedisSession().hSetBytes(yqzzHonorKey, serverId, serverInfo.build().toByteArray());
        }
        return HawkScript.successResponse("yqzz ok");
    }
    
    private String gmSw(int areaId, String honorGuilds, SeasonOpenTimeCfg timeCfg) {
    	 String swHonorKey = "SEASON_HONOR:SW:" + areaId + ":" + timeCfg.getMatchTermId(Activity.SeasonMatchType.S_SW_VALUE);
         RedisProxy.getInstance().getRedisSession().del(swHonorKey);
         int rank = 0;
         for(String guildId : honorGuilds.split(",")){
             rank++;
             if(!GuildService.getInstance().isGuildExist(guildId)){
                 continue;
             }
             Activity.SeasonSWRankInfo.Builder swGuildinfo = Activity.SeasonSWRankInfo.newBuilder();
             swGuildinfo.setRank(rank);
             //联盟名字
             swGuildinfo.setGuildName(GuildService.getInstance().getGuildName(guildId));
             //联盟旗帜
             swGuildinfo.setGuildFlag(GuildService.getInstance().getGuildFlag(guildId));
             //盟主名字
             swGuildinfo.setGuildLeader(GuildService.getInstance().getGuildLeaderName(guildId));
             //联盟简称
             swGuildinfo.setGuildTag(GuildService.getInstance().getGuildTag(guildId));
             //服务器id
             swGuildinfo.setServerId(GsConfig.getInstance().getServerId());
             if(rank==1){
                 swGuildinfo.setLevel(3);
             }else {
                 swGuildinfo.setLevel(2);
             }
             RedisProxy.getInstance().getRedisSession().hSetBytes(swHonorKey, guildId, swGuildinfo.build().toByteArray());
         }
         return HawkScript.successResponse("sw ok");
    }
    
    private String gmSeason(int areaId, int season) {
    	String seasonHonorKey = "SEASON_HONOR:SEASON:" + areaId + ":" + season;
        Map<String, SeasonActivityGuildGradeData> dataMap = SeasonActivityGuildGradeData.loadAll(season);
        List<SeasonActivityGuildGradeData> guildGradeData = new ArrayList<>(dataMap.values());
        Collections.sort(guildGradeData, new Comparator<SeasonActivityGuildGradeData>() {
            @Override
            public int compare(SeasonActivityGuildGradeData o1, SeasonActivityGuildGradeData o2) {
                if(o1.getExp() != o2.getExp()){
                    return o1.getExp() > o2.getExp() ? -1 : 1;
                }
                return 0;
            }
        });
        int count = Math.min(100, guildGradeData.size());
        RedisProxy.getInstance().getRedisSession().del(seasonHonorKey.getBytes());
        for(int i = 1 ; i <= count ; i++){
            SeasonActivityGuildGradeData gradeData = guildGradeData.get(i);
            String guildInfoString = RedisProxy.getInstance().getRedisSession().hGet("SEASON_ACTIVITY_GINFO", gradeData.getGuildId());
            GuildSeasonKingGradeInfo guildInfo = SerializeHelper.getValue(GuildSeasonKingGradeInfo.class, guildInfoString, SerializeHelper.COLON_ITEMS);
            if(guildInfo == null){
                continue;
            }
            //构建排行信息
            Activity.SeasonGuildKingRankMsg.Builder rankBuilder = Activity.SeasonGuildKingRankMsg.newBuilder();
            //排名
            rankBuilder.setRank(i);
            //分数
            rankBuilder.setScore(gradeData.getExp());
            //联盟名字
            rankBuilder.setGuildName(guildInfo==null? "":guildInfo.getGuildName());
            //联盟旗帜
            rankBuilder.setGuildFlag(guildInfo==null? 1:guildInfo.getGuildFlag());
            //盟主名字
            rankBuilder.setGuildLeader(guildInfo==null? "":guildInfo.getGuildLeader());
            //联盟简称
            rankBuilder.setGuildTag(guildInfo==null? "":guildInfo.getGuildTag());
            //服务器id
            rankBuilder.setServerId(guildInfo==null? "":guildInfo.getServerId());
            RedisProxy.getInstance().getRedisSession().lPush(seasonHonorKey.getBytes(), 0, rankBuilder.build().toByteArray());
        }
        return HawkScript.successResponse("season ok");
    }
    
    private String gmSeasonNew(int areaId, int season, String seasonGuilds) {
    	 String seasonHonorKey = "SEASON_HONOR:SEASON:" + areaId + ":" + season;
         RedisProxy.getInstance().getRedisSession().del(seasonHonorKey);
         String [] seasonGuildInfos = seasonGuilds.split(";");
         for(String seasonGuildInfo : seasonGuildInfos){
             String [] infos = seasonGuildInfo.split(",");
             //构建排行信息
             Activity.SeasonGuildKingRankMsg.Builder rankBuilder = Activity.SeasonGuildKingRankMsg.newBuilder();
             //排名
             rankBuilder.setRank(Integer.parseInt(infos[0]));
             //分数
             rankBuilder.setScore(Long.parseLong(infos[1]));
             //联盟名字
             rankBuilder.setGuildName(infos[2]);
             //联盟旗帜
             rankBuilder.setGuildFlag(Integer.parseInt(infos[3]));
             //盟主名字
             rankBuilder.setGuildLeader(infos[4]);
             //联盟简称
             rankBuilder.setGuildTag(infos[5]);
             //服务器id
             rankBuilder.setServerId(infos[6]);
             RedisProxy.getInstance().getRedisSession().lPush(seasonHonorKey.getBytes(), 0, rankBuilder.build().toByteArray());
         }
         return HawkScript.successResponse("seasonNew ok");
    }
    
    /** 泰星赛季 TODO */
    private String gmSeasonNew1(int areaId, int season) {
    	 try {
             String seasonHonorKey = "SEASON_HONOR:SEASON:" + areaId + ":" + season;
             RedisProxy.getInstance().getRedisSession().del(seasonHonorKey);
             // 读文件
             List<String> seasonResult = new ArrayList<>();
             if(areaId == 1){
                 HawkOSOperator.readTextFileLines("tmp/seasonResultWX.txt", seasonResult);
             }else {
                 HawkOSOperator.readTextFileLines("tmp/seasonResultQQ.txt", seasonResult);
             }
             for(String seasonGuildInfo : seasonResult){
                 String [] infos = seasonGuildInfo.split(",");
                 //构建排行信息
                 Activity.SeasonGuildKingRankMsg.Builder rankBuilder = Activity.SeasonGuildKingRankMsg.newBuilder();
                 //排名
                 rankBuilder.setRank(Integer.parseInt(infos[0]));
                 //分数
                 rankBuilder.setScore(Long.parseLong(infos[1]));
                 //联盟名字
                 rankBuilder.setGuildName(infos[2]);
                 //联盟旗帜
                 rankBuilder.setGuildFlag(Integer.parseInt(infos[3]));
                 //盟主名字
                 rankBuilder.setGuildLeader(infos[4]);
                 //联盟简称
                 rankBuilder.setGuildTag(infos[5]);
                 //服务器id
                 rankBuilder.setServerId(infos[6]);
                 RedisProxy.getInstance().getRedisSession().lPush(seasonHonorKey.getBytes(), 0, rankBuilder.build().toByteArray());
             }
             HawkLog.logPrintln("SeasonHonorGm seasonNew1, areaId: {}, season: {}, data size: {}", areaId, season, seasonResult.size());
         } catch (Exception e) {
             HawkException.catchException(e);
         }
         return HawkScript.successResponse("seasonNew1 ok");
    }
    
    /** 统帅之战 TODO */
    private String gmSwNew(int areaId, String honorGuilds, SeasonOpenTimeCfg timeCfg) {
    	String swHonorKey = "SEASON_HONOR:SW:" + areaId + ":" + timeCfg.getMatchTermId(Activity.SeasonMatchType.S_SW_VALUE);
        RedisProxy.getInstance().getRedisSession().del(swHonorKey);
        List<String> swResult = new ArrayList<>();
        try {
        	if(areaId == 1){
        		HawkOSOperator.readTextFileLines("tmp/swHonorWX.txt", swResult);
        	}else {
        		HawkOSOperator.readTextFileLines("tmp/swHonorQQ.txt", swResult);
        	}
        } catch (Exception e) {
        	HawkException.catchException(e);
        }
        
        int rank = 0;
        for(String honorGuild : swResult){
            rank++;
            String [] infos = honorGuild.split(",");
            Activity.SeasonSWRankInfo.Builder swGuildinfo = Activity.SeasonSWRankInfo.newBuilder();
            swGuildinfo.setRank(rank);
            //联盟名字
            swGuildinfo.setGuildName(infos[1]);
            //联盟旗帜
            swGuildinfo.setGuildFlag(Integer.parseInt(infos[2]));
            //盟主名字
            swGuildinfo.setGuildLeader(infos[3]);
            //联盟简称
            swGuildinfo.setGuildTag(infos[4]);
            //服务器id
            swGuildinfo.setServerId(infos[5]);
            if(rank==1){
                swGuildinfo.setLevel(3);
            }else {
                swGuildinfo.setLevel(2);
            }
            RedisProxy.getInstance().getRedisSession().hSetBytes(swHonorKey, infos[0], swGuildinfo.build().toByteArray());
        }
        HawkLog.logPrintln("SeasonHonorGm swNew, areaId: {}, season: {}, data size: {}", areaId, timeCfg.getTermId(), swResult.size());
        return HawkScript.successResponse("swNew ok");
    }
    
    /** 星海激战 TODO */
    private String gmXHJZ(int areaId, SeasonOpenTimeCfg timeCfg) {
    	String xhjzHonorKey = "SEASON_HONOR:XHJZ:" + areaId + ":" + timeCfg.getMatchTermId(Activity.SeasonMatchType.S_XHJZ_VALUE);
        RedisProxy.getInstance().getRedisSession().del(xhjzHonorKey);
        List<String> honorResult = new ArrayList<>();
        try {
        	if(areaId == 1){
        		HawkOSOperator.readTextFileLines("tmp/xhjzHonorWX.txt", honorResult);
        	}else {
        		HawkOSOperator.readTextFileLines("tmp/xhjzHonorQQ.txt", honorResult);
        	}
        } catch (Exception e) {
        	HawkException.catchException(e);
        }
      
        int rank = 0;
        for(String honorGuild : honorResult){
            rank++;
            String [] infos = honorGuild.split(",");
            Activity.SeasonXHJZRankInfo.Builder xhjzGuildinfo = Activity.SeasonXHJZRankInfo.newBuilder();
            xhjzGuildinfo.setRank(rank);
            //联盟名字
            xhjzGuildinfo.setGuildName(infos[1]);
            //联盟旗帜
            xhjzGuildinfo.setGuildFlag(Integer.parseInt(infos[2]));
            //盟主名字
            xhjzGuildinfo.setGuildLeader(infos[3]);
            //联盟简称
            xhjzGuildinfo.setGuildTag(infos[4]);
            //服务器id
            xhjzGuildinfo.setServerId(infos[5]);
            RedisProxy.getInstance().getRedisSession().hSetBytes(xhjzHonorKey, infos[0], xhjzGuildinfo.build().toByteArray());
        }
        HawkLog.logPrintln("SeasonHonorGm xhjz, areaId: {}, season: {}, data size: {}", areaId, timeCfg.getTermId(), honorResult.size());
        return HawkScript.successResponse("XHJZ ok");
    }
    
}
