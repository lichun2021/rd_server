package com.hawk.game.service.tblyTeam;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.TWScoreEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.seasonActivity.SeasonActivity;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.config.TiberiumConstCfg;
import com.hawk.game.config.TiberiumGuildAwardCfg;
import com.hawk.game.config.TiberiumPersonAwardCfg;
import com.hawk.game.config.TiberiumSeasonGuildAwardCfg;
import com.hawk.game.config.TiberiumSeasonPersonAwardCfg;
import com.hawk.game.config.TiberiumSeasonRankAwardCfg;
import com.hawk.game.config.TiberiumSeasonTimeCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.GuildCreateObj;
import com.hawk.game.invoker.GuildCreateRpcInvoker;
import com.hawk.game.invoker.guildBackActivity.GuildBackDropInvoker;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.lianmengtaiboliya.TBLYExtraParam;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager;
import com.hawk.game.module.lianmengtaiboliya.msg.TBLYBilingInformationMsg;
import com.hawk.game.module.lianmengtaiboliya.roomstate.TBLYGameOver;
import com.hawk.game.msg.PlayerAssembleMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.GuildTeam;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Rank;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.TBLY;
import com.hawk.game.protocol.TiberiumWar;
import com.hawk.game.protocol.TiberiumWar.GetTLWGuildRankResp;
import com.hawk.game.protocol.TiberiumWar.PBTLWEliminationGroup;
import com.hawk.game.protocol.TiberiumWar.TLWBattle;
import com.hawk.game.protocol.TiberiumWar.TLWFinalMatchMatchInfo;
import com.hawk.game.protocol.TiberiumWar.TLWFreeRoom;
import com.hawk.game.protocol.TiberiumWar.TLWGetMatchInfo;
import com.hawk.game.protocol.TiberiumWar.TLWGetMatchInfoReq;
import com.hawk.game.protocol.TiberiumWar.TLWGetMatchInfoResp;
import com.hawk.game.protocol.TiberiumWar.TLWGetOBRoomInfo;
import com.hawk.game.protocol.TiberiumWar.TLWGetScoreInfoResp;
import com.hawk.game.protocol.TiberiumWar.TLWGetTeamGuildInfoResp;
import com.hawk.game.protocol.TiberiumWar.TLWGroup;
import com.hawk.game.protocol.TiberiumWar.TLWGuildBaseInfo;
import com.hawk.game.protocol.TiberiumWar.TLWGuildInfo;
import com.hawk.game.protocol.TiberiumWar.TLWGuildRank;
import com.hawk.game.protocol.TiberiumWar.TLWPageInfo;
import com.hawk.game.protocol.TiberiumWar.TLWRoomInfo;
import com.hawk.game.protocol.TiberiumWar.TLWScoreInfo;
import com.hawk.game.protocol.TiberiumWar.TLWSelfMatchList;
import com.hawk.game.protocol.TiberiumWar.TLWServer;
import com.hawk.game.protocol.TiberiumWar.TLWStateInfo;
import com.hawk.game.protocol.TiberiumWar.TLWTeamGuildInfo;
import com.hawk.game.protocol.TiberiumWar.TWGuildInfo;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankObject;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.ActivityService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.guildTeam.ipml.TBLYGuildTeamManager;
import com.hawk.game.service.guildTeam.model.GuildTeamData;
import com.hawk.game.service.guildTeam.model.GuildTeamPlayerData;
import com.hawk.game.service.guildTeam.model.GuildTeamRoomData;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventTiberiumWar;
import com.hawk.game.service.tblyTeam.comparetor.TLWTeamGroupComparator;
import com.hawk.game.service.tblyTeam.comparetor.TLWTeamTotalRankComparator;
import com.hawk.game.service.tblyTeam.model.TBLYSeasonData;
import com.hawk.game.service.tblyTeam.model.TBLYSeasonPlayerData;
import com.hawk.game.service.tblyTeam.model.TBLYSrasonBattleTeamData;
import com.hawk.game.service.tblyTeam.model.TBLYSrasonGuildData;
import com.hawk.game.service.tblyTeam.state.TBLYSeasonBigStateData;
import com.hawk.game.service.tblyTeam.state.TBLYSeasonStateData;
import com.hawk.game.service.tblyTeam.state.TBLYWarStateEnum;
import com.hawk.game.service.tiberium.TWGuildData;
import com.hawk.game.service.tiberium.TiberiumConst;
import com.hawk.game.service.tiberium.logunit.TLWLeaguaGuildInfoUnit;
import com.hawk.game.service.tiberium.logunit.TLWWarResultLogUnit;
import com.hawk.game.service.tiberium.logunit.TWLogUtil;
import com.hawk.game.service.tiberium.logunit.TWPlayerSeasonScoreLogUnit;
import com.hawk.game.service.tiberium.logunit.TWSelfRewardLogUnit;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst;
import com.hawk.log.LogConst;

import redis.clients.jedis.Tuple;

public class TBLYSeasonService extends HawkAppObj {
    /** 赛季数据-大状态机*/
    private TBLYSeasonBigStateData bigStateData;
    /** 赛季数据-主状态机*/
    private TBLYSeasonStateData stateData;
    /** 赛季数据-小队赛季数据*/
    private Map<String, TBLYSeasonData> seasonDataMap = new HashMap<>();
    /** 赛季数据-小队数据*/
    private Map<String, GuildTeamData> seasonTeamDataMap = new HashMap<>();
    
    /** 当前期战时数据-房间数据(本服相关) */
    private Map<String, GuildTeamRoomData> roomDataMap = new ConcurrentHashMap<>();
    /** 当前期战时数据-小队数据 小队id:小队数据 (当期本服相关)*/
    private Map<String, TBLYSrasonBattleTeamData> battleTeamDataMap = new ConcurrentHashMap<>();
    /** 当前期战时数据-玩家id : 小队id*/
    private Map<String, String> playerIdToTeamId = new ConcurrentHashMap<>();
    
    /** 排行榜-入围赛排行*/
    private GetTLWGuildRankResp.Builder joinRank;
    /** 排行榜-战绩排行*/
    private Map<Integer,GetTLWGuildRankResp.Builder> battleRank;
    /** 队伍战力*/
    private Map<String, Long> teamPowerMap = new HashMap<>();

    /** 类型列表-淘汰赛分组*/
    private List<TiberiumConst.TLWGroupType> groupTypeList = Arrays.asList(
    		TiberiumConst.TLWGroupType.S_GROUP,
    		TiberiumConst.TLWGroupType.A_GROUP);
    /** 赛区类型*/
    private List<TLWServer> TLWServerList = Arrays.asList(
    		TLWServer.TLW_TOP_SERVER,
    		TLWServer.TLW_DAWN_SERVER,
    		TLWServer.TLW_HOT_SERVER); 
    
    
    private static TBLYSeasonService instance = null;
    public static TBLYSeasonService getInstance() {
        return instance;
    }
    public TBLYSeasonService(HawkXID xid) {
        super(xid);
        instance = this;
    }

    /**
     * 初始化
     * @return
     */
    public boolean init() {
        try {
            bigStateData = new TBLYSeasonBigStateData();
            stateData = new TBLYSeasonStateData();
            bigStateData.load();
            stateData.load();
            loadSeasonTeamAndGuildTeam();
            if(getState() == TBLYWarStateEnum.SEASON_WAR_WAIT
                    || getState() == TBLYWarStateEnum.SEASON_WAR_MANGE
                    || getState() == TBLYWarStateEnum.SEASON_WAR_OPEN){
                loadBattleRoom();
            }
            addTickable(new HawkPeriodTickable(1000) {
                @Override
                public void onPeriodTick() {
                    try {
                        bigStateData.tick();
                    } catch (Exception e) {
                        HawkException.catchException(e);
                    }
                    try {
                        stateData.tick();
                    } catch (Exception e) {
                        HawkException.catchException(e);
                    }

                }
            });
            //重载小队战力
            addTickable(new HawkPeriodTickable(1000 * 10) {
                @Override
                public void onPeriodTick() {
                	loadTeamPower();
                }
            });
            addTickable(new HawkPeriodTickable(TimeUnit.MINUTES.toMillis(10)) {
                @Override
                public void onPeriodTick() {
                    try {
                        if(bigStateData.getState() == TBLYWarStateEnum.SEASON_BIG_SIGNUP){
                            updatePowerRank();
                        }
                        updateTeamPower();
                        doRank();
                    } catch (Exception e) {
                        HawkException.catchException(e);
                    }
                    try {
                        if(stateData.getState() == TBLYWarStateEnum.SEASON_WAR_WAIT){
                            //加载参与的小队数据
                            loadBattleTeam();
                            //加载参与的玩家数据
                            loadBattlePlayer();
                        }
                    } catch (Exception e) {
                        HawkException.catchException(e);
                    }
                }
            });
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
        return true;
    }
    


    /**
     * 获取当前所在赛季
     * @return
     */
    public int getCurrSeason() {
        long now = HawkTime.getMillisecond();
        int season = -1;
        ConfigIterator<TiberiumSeasonTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(TiberiumSeasonTimeCfg.class);
        for (TiberiumSeasonTimeCfg cfg : its) {
            long startTime = cfg.getSeasonStartTimeValue();
            long endTime = cfg.getSeasonEndTimeValue();
            if (startTime > 0 && now >= startTime) {
                season = cfg.getSeason();
            }
            if (endTime > 0 && now >= endTime) {
            	season = -1;
            }
        }
        return season;
    }
    
    /**
     * 获得状态锁
     * @param key
     * @param state
     * @return
     */
    public boolean getLock(String key, String state){
        return RedisProxy.getInstance().getRedisSession().hSetNx(key, state, GsConfig.getInstance().getServerId()) > 0;
    }
    public boolean getBigLock(){
        return getLock(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_BIG_LOCK, getSeason()), getBigState().name());
    }
    public boolean getMainLock(){
        return getLock(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_MAIN_LOCK, getSeason(), getTermId()), getState().name());
    }

    
    /**
     * 获取当前阶段的联赛时间配置
     * @return
     */
    public TiberiumSeasonTimeCfg getCurrTimeCfg(){
        return getTimeCfgBySeasonAndTermId(stateData.getSeason(), stateData.getTermId());
    }
    public TiberiumSeasonTimeCfg getFirstTimeCfg(){
        return getTimeCfgBySeasonAndTermId(stateData.getSeason(), 1);
    }
    public TiberiumSeasonTimeCfg getFinalTimeCfg(){
        return getTimeCfgBySeasonAndTermId(stateData.getSeason(), TiberiumConstCfg.getInstance().getEliminationFinalTermId());
    }
    
    
    /**
     * 获取指定赛季和期数的时间配置
     * @return
     */
    public TiberiumSeasonTimeCfg getTimeCfgBySeasonAndTermId(int season, int termId) {
        ConfigIterator<TiberiumSeasonTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(TiberiumSeasonTimeCfg.class);
        for (TiberiumSeasonTimeCfg cfg : its) {
            if (cfg.getSeason() == season && cfg.getTermId() == termId) {
                return cfg;
            }
        }
        return null;
    }
    
    
    
    /**
     * 获得当前状态数据
     * @return
     */
    public int getSeason(){
        return stateData.getSeason();
    }
    public int getTermId(){
        return stateData.getTermId();
    }
    public TBLYWarStateEnum getState(){
        return stateData.getState();
    }
    public TBLYWarStateEnum getBigState(){
        return bigStateData.getState();
    }

    
    /**
     * 活动状态机进入下一状态
     * @return 执行结果
     */
    public boolean toNext(){
        stateData.toNext();
        return true;
    }
    public boolean toBigNext(){
        bigStateData.toNext();
        return true;
    }

    
    /**
     * 赛季开始初始化
     * @param season
     */
    public void onSeasonOpen(int season){
        bigStateData.setSeason(season);
        stateData.setSeason(season);
        stateData.setTermId(0);
        stateData.toNext();
        this.sendOpenMail();
    }

    /**
     * 赛季进入报名阶段
     */
    public void onBigSignUp(){
    	
    }
    
    /**
     * 赛季进入小组赛等待阶段
     */
    public void onBigGroupWait(){
        this.updatePowerRank();
    }

    /**
     * 赛季进入小组赛阶段
     */
    public void onBigGroup(){
        boolean getLock = this.getBigLock();
        if(!getLock){
            return;
        }
        try {
        	this.pickZoneAndSchedule();
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 赛季进入淘汰赛阶段
     */
    public void onBigKickOut(){
        boolean getLock = this.getBigLock();
        if(!getLock){
            return;
        }
    }
    
    /**
     * 赛季进入决赛阶段
     */
    public void onBigFinal(){
        boolean getLock = this.getBigLock();
        if(!getLock){
            return;
        }
    }
    
    /**
     * 赛季进入结束展示阶段
     */
    public void onBigEndShow(){
    	this.loadSeasonTeamAndGuildTeam();
        this.sendFinalReward();
    }
    
    
    /**
     * 当前期战斗进入和平阶段
     */
    public void onPeace(){
    	this.stateData.setTermId(stateData.getTermId() + 1);
        if(getTermId() == 1){
        	this.bigStateData.next();
        }
        if(getTermId() == TiberiumConstCfg.getInstance().getEliminationStartTermId()){
        	this.bigStateData.next();
        }
        if(getTermId() == TiberiumConstCfg.getInstance().getEliminationFinalTermId()){
        	this.bigStateData.next();
        }
    }

    /**
     * 当前期战斗离开和平阶段
     */
    public void leavePeace(){
        if(getTermId() == 1){
        	this.bigStateData.next();
        }
    }
    
    /**
     * 当前期战斗进入匹配阶段
     */
    public void onMatch(){
    	this.doMatch();
    }

    /**
     * 当前期战斗离开匹配阶段
     */
    public void leaveMatch(){
        this.loadSeasonTeamAndGuildTeam();
        this.doRank();
        this.sendPickedMail();
        this.sendGroupMail();
    }

    /**
     * 当前期战斗进入调整阶段
     */
    public void onWarManager(){
    	this.doRank();
    	this.loadBattleRoom();
    }

    /**
     * 当前期战斗进入开战等待阶段
     */
    public void onWarWait(){
    	this.updateTeamInfo();
    }
    
    /**
     * 当前期战斗进入开战阶段
     */
    public void onWarOpen(){
    	this.loadBattlePlayer();
        this.createBattleRoom();
    }
    
    /**
     * 当前期战斗离开战斗阶段
     */
    public void leaveWarOpen(){
    	this.loadBattleResult();
    	this.sendAward();
    	this.cleanBattleData();
    }
    
    
    
    /**
	 * 赛季开启邮件通知
	 */
	private void sendOpenMail() {
		int season = this.getSeason();
		TiberiumSeasonTimeCfg cfg = getTimeCfgBySeasonAndTermId(season, 1);
		if(cfg == null){
			return;
		}
		SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
				.setMailId(MailId.TIBERIUM_SEASON_OPEN).build()
				, HawkTime.getMillisecond(), cfg.getMatchStartTimeValue());
	}
    
    
    /**
     * 符合要求的联盟更新小队战力，用于入围海选
     */
    protected void updatePowerRank() {
        String serverId = GsConfig.getInstance().getServerId();
        if(serverId.startsWith("1999") || serverId.startsWith("2999") ){
            return;
        }
        Map<String, Double> powerMap = new HashMap<>();
        Map<String, String> dataStrMap = new HashMap<>();
        List<String> guildIds = getNeedUpdateGuildIds();
        for(String guildId : guildIds){
            GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(guildId);
            if(guildInfoObject == null) {
                continue;
            }
            GuildTeamData teamData = TBLYGuildTeamManager.getInstance().makeSureTeamData(guildInfoObject, 1);
            if(teamData == null){
                continue;
            }
            TBLYSeasonData seasonData = new TBLYSeasonData();
            seasonData.teamId = teamData.id;
            seasonData.createTime = guildInfoObject.getCreateTime();
            dataStrMap.put(seasonData.teamId, seasonData.serialize());
            this.refreshTeamInfo(teamData);
            powerMap.put(teamData.id, (double)teamData.battlePoint);
        }
        if(!powerMap.isEmpty()){
        	String rankKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_RANK, stateData.getSeason());
        	RedisProxy.getInstance().getRedisSession().zAdd(rankKey, powerMap, TiberiumConst.TLW_EXPIRE_SECONDS);
        }
        if(!dataStrMap.isEmpty()){
            String signDataKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_SIGNUP_DATA, getSeason());
            RedisProxy.getInstance().getRedisSession().hmSet(signDataKey, dataStrMap, TiberiumConst.TLW_EXPIRE_SECONDS);
        }
    }
    
    /**
     * 挑选合适联盟队伍进入海选
     * @return
     */
    public List<String> getNeedUpdateGuildIds(){
        // debug 模式,刷新联盟上限扩大为榜单容量
        if (GsConfig.getInstance().isDebug()) {
            return GuildService.getInstance().getGuildIds();
        }
        int rankLimit = TiberiumConstCfg.getInstance().getUpdateRankLimit();
        List<String> guildIds = new ArrayList<>();
        // 刷新本服前30的联盟的战力
        RankObject rankObject = RankService.getInstance().getRankObject(Rank.RankType.ALLIANCE_FIGHT_KEY);
        List<Rank.RankInfo> rankList = new ArrayList<>(rankObject.getSortedRank());
        int size = Math.min(rankLimit, rankList.size());
        for (int i = 0; i < size; i++) {
            Rank.RankInfo rankInfo = rankList.get(i);
            guildIds.add(rankInfo.getId());
        }
        return guildIds;
    }
  
    /**
     * 刷新队伍战数据
     * @param teamData
     */
    public void refreshTeamInfo(GuildTeamData teamData){
        String teamId = teamData.id;
        long battlePoint = 0L;
        int memberCount = 0;
        Set<String> playerIds = TBLYGuildTeamManager.getInstance().getTeamPlayerIds(teamId);
        if(playerIds != null){
            for (String playerId : playerIds) {
                try {
                    GuildTeamPlayerData playerData = TBLYGuildTeamManager.getInstance().getPlayerData(playerId);
                    if (playerData == null) {
                        continue;
                    }
                    if (!teamId.equals(playerData.teamId)) {
                        continue;
                    }
                    Player player = GlobalData.getInstance().makesurePlayer(playerId);
                    if (player == null) {
                        continue;
                    }
                    long noArmyPoint = player.getNoArmyPower();
                    battlePoint += noArmyPoint;
                    memberCount++;
                } catch (Exception e) {
                    HawkException.catchException(e);
                }
            }
        }
        teamData.battlePoint = battlePoint;
        teamData.memberCnt = memberCount;
        teamData.serverId = GsConfig.getInstance().getServerId();
    }
    
    /**
     * 分区 分小组 生成小组对战
     * @return
     */
    public Map<String, TBLYSeasonData> pickZoneAndSchedule(){
        try {
            HawkLog.logPrintln("TBLYSeasonService-pickJoinTeamAndSchedule-start-season:{}",getSeason());
            //入围战力排行榜
            String rankKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_RANK, getSeason());
            //报名联盟数据
            String signDataKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_SIGNUP_DATA, getSeason());
            //入围名次数据
            String joinKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_JOIN, getSeason());
            //入围联盟数据
            String dataKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_DATA, getSeason());
            //小队战力
            String powerKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_POWER, getSeason());
            
            Map<String, String> joinMap = new HashMap<>();
            Map<String, TBLYSeasonData> seasonDataMap = new HashMap<>();
            
            //多取了10个，以免下面队伍信息和这里对不上
            Set<Tuple> tuples  = RedisProxy.getInstance().getRedisSession().zRevrangeWithScores(rankKey, 0, TiberiumConstCfg.getInstance().getGuildPickCnt() +10,0);
            if(tuples.isEmpty()){
                return seasonDataMap;
            }
            List<String> signupIds = new ArrayList<>();
            for (Tuple tuple : tuples) {
                signupIds.add(tuple.getElement());
            }
           
            List<String> dataList = RedisProxy.getInstance().getRedisSession().hmGet(String.format(signDataKey, getSeason(), getTermId()), signupIds.toArray(new String[0]));
            Map<String, TBLYSeasonData> signDataMap = new HashMap<>();
            for(String json : dataList){
                TBLYSeasonData seasonData = TBLYSeasonData.unSerialize(json);
                if(seasonData == null){
                    continue;
                }
                signDataMap.put(seasonData.teamId, seasonData);
            }
            Map<TLWServer,List<TBLYSeasonData>> teamTypeMap = new HashMap<>();
            int rank = 1;
            for (Tuple tuple : tuples) {
                String teamId = tuple.getElement();
                TBLYSeasonData seasonData = signDataMap.get(teamId);
                if(seasonData == null){
                    continue;
                }
                TLWServer teamType = this.getTeamTypeByRank(rank);
                if(Objects.isNull(teamType)){
                	continue;
                }
                seasonData.teamId = teamId;
                seasonData.initPower = Math.round(tuple.getScore());
                seasonData.initPowerRank = rank;
                seasonData.teamType = teamType.getNumber();
                seasonData.groupType = TLWGroup.TEAM_GROUP.getNumber();
                joinMap.put(teamId, String.valueOf(rank));
                seasonDataMap.put(seasonData.teamId, seasonData);
                
                //添加到分组
                List<TBLYSeasonData> typeList = teamTypeMap.get(teamType);
                if(Objects.isNull(typeList)){
                	typeList = new ArrayList<>();
                	teamTypeMap.put(teamType, typeList);
                }
                typeList.add(seasonData);
                rank++;
                if(rank > TiberiumConstCfg.getInstance().getGuildPickCnt()){
                    break;
                }
            }
            //保存入围时名次数据
            RedisProxy.getInstance().getRedisSession().hmSet(joinKey, joinMap, 0);
            //每个区域队伍 分32小组
            for(Entry<TLWServer,List<TBLYSeasonData>> entry: teamTypeMap.entrySet()){
            	TLWServer teamType = entry.getKey();
            	List<TBLYSeasonData> teams = entry.getValue();
            	List<List<TBLYSeasonData>> groups = assignTeamsToGroups(teams, TiberiumConstCfg.getInstance().getTeamCnt());
            	
            	for(int i = 0; i< groups.size(); i++){
            		int groupId = i + 1;
                    List<TBLYSeasonData> groupTeams = groups.get(i);
                    TBLYSeasonData seasonData = groupTeams.get(0);
                    seasonData.groupId = groupId;
                    seasonData.isSeed = true;
                    //当前小组队伍ID列表
                    List<String> groupTeamIds = new ArrayList<>();
                    for(TBLYSeasonData data : groupTeams){
                    	data.groupId = groupId;
                    	groupTeamIds.add(data.teamId);
                    }
                    //保存小组ID列表
                    String groupKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_GROUP, getSeason(),teamType.getNumber(),groupId);
                    RedisProxy.getInstance().getRedisSession().sAdd(groupKey, 0, groupTeamIds.toArray(new String[0]));
                    //小组内对战列表
                    Map<Integer, List<HawkTuple2<TBLYSeasonData, TBLYSeasonData>>> schedule = generateSchedule(groupTeams, TiberiumConstCfg.getInstance().getEliminationStartTermId() - 1);
                    for(int termId : schedule.keySet()){
                        List<HawkTuple2<TBLYSeasonData, TBLYSeasonData>> results = schedule.get(termId);
                        createRooms(termId, results, teamType, TLWGroup.TEAM_GROUP, TiberiumConst.TLWBattleType.TEAM_GROUP_BATTLE, i+1);
                    }
                }
            }
            //入围的队伍数据
            Map<String, String> seasonDataStrMap = new HashMap<>();
            Map<String, String> powerMap = new HashMap<>();
            for(TBLYSeasonData seasonData : seasonDataMap.values()){
                seasonDataStrMap.put(seasonData.teamId, seasonData.serialize());
                powerMap.put(seasonData.teamId, String.valueOf(seasonData.initPower));
            }
            RedisProxy.getInstance().getRedisSession().hmSet(dataKey, seasonDataStrMap, 0);
            
            //保存一下战力
            RedisProxy.getInstance().getRedisSession().hmSet(powerKey, powerMap, TiberiumConst.TLW_EXPIRE_SECONDS);
            HawkLog.logPrintln("TBLYSeasonService-pickJoinTeamAndSchedule-end-season:{}",getSeason());
            return seasonDataMap;
        } catch (Exception e) {
            HawkException.catchException(e);
            return null;
        }
    }
    
    /**
     * 将队伍分配到不同的小组中
     *
     * @param teamIds 所有的队伍
     * @param groups   小组数
     * @return 返回分配后的32个小组，每个小组的队伍
     */
    public List<List<TBLYSeasonData>> assignTeamsToGroups(List<TBLYSeasonData> teams, int groups) {
        // 初始化32个小组
        List<List<TBLYSeasonData>> groupsList = new ArrayList<>();
        for (int i = 0; i < groups; i++) {
            groupsList.add(new ArrayList<>());
        }
        //按顺序每组依次取一个队伍
        for(int i = 0;i < teams.size();i++){
        	TBLYSeasonData seasonData = teams.get(i);
        	int group = i % groups;
        	groupsList.get(group).add(seasonData);
        }
        return groupsList;
    }
    
    
    /**
     * 小组赛生成对战
     * @param teams
     * @param days
     * @return
     */
    public Map<Integer, List<HawkTuple2<TBLYSeasonData, TBLYSeasonData>>> generateSchedule(List<TBLYSeasonData> teams, int days) {
        Map<Integer, List<HawkTuple2<TBLYSeasonData, TBLYSeasonData>>> schedule = new HashMap<>();
        int teamCnt = teams.size();
        for (int day = 0; day < days; day++) {
        	List<HawkTuple2<TBLYSeasonData, TBLYSeasonData>> dayMatches = new ArrayList<>();
            for (int i = 0; i < teamCnt / 2; i++) {
            	TBLYSeasonData team1 = teams.get(i);
            	TBLYSeasonData team2 = teams.get(teamCnt - 1 - i);
            	dayMatches.add(new HawkTuple2<>(team1, team2));
            }
            //轮转队伍（固定第一队）
            teams.add(1, teams.remove(teamCnt - 1));
            schedule.put(day + 1, dayMatches);
        }
        return schedule;
    }
    
    
    /**
     * 匹配对战-淘汰赛和决赛  on
     */
    public void doMatch(){
        boolean getLock = getMainLock();
        if(!getLock){
            return;
        }
        //重载最新的队伍数据
        this.loadSeasonTeamAndGuildTeam();
        //淘汰赛分组
        if(getTermId() == TiberiumConstCfg.getInstance().getEliminationStartTermId()){
        	pickKickoutGroup();
        }
        //淘汰赛匹配
        if(getTermId() >= TiberiumConstCfg.getInstance().getEliminationStartTermId()
                && getTermId() < TiberiumConstCfg.getInstance().getEliminationFinalTermId()){
        	kickOutMatch();
        }
        //决赛匹配
        if(getTermId() == TiberiumConstCfg.getInstance().getEliminationFinalTermId()){
        	onFinalMatch();
        }
    }
  
    /**
     * 进入淘汰赛时,根据战绩分为S组和A组
     * 每个小队前X名进入S组,后Y名进入A组
     * 将S组  A组的成员全部存入胜者组
     */
    public void pickKickoutGroup(){
    	for(TLWServer tlwType : this.TLWServerList){
    		Map<TiberiumConst.TLWGroupType, List<String>> eliminationGroupMap = new HashMap<>();
            for(TiberiumConst.TLWGroupType groupType : groupTypeList){
                eliminationGroupMap.put(groupType, new ArrayList<>());
            }
            for(int i = 1; i <= TiberiumConstCfg.getInstance().getTeamCnt(); i++){
                String groupResidKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_GROUP, getSeason(), tlwType.getNumber(),i);
                Set<String> groupTeamIds = RedisProxy.getInstance().getRedisSession().sMembers(groupResidKey);
                List<TBLYSeasonData> seasonDataList = new ArrayList<>();
                for(String teamId : groupTeamIds){
                    seasonDataList.add(seasonDataMap.get(teamId));
                }
                seasonDataList.sort(new TLWTeamGroupComparator());
                for(int rank = 1; rank <= seasonDataList.size(); rank++){
                    TiberiumConst.TLWGroupType groupType = calcEliminationGroupType(rank);
                    TBLYSeasonData seasonData = seasonDataList.get(rank - 1);
                    seasonData.groupType = groupType.getNumber();
                    eliminationGroupMap.get(groupType).add(seasonData.teamId);
                    updateSeasonData(seasonData);
                }
            }
            for(TiberiumConst.TLWGroupType groupType : groupTypeList){
                List<String> teamIds = eliminationGroupMap.get(groupType);
                String winKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_KICK_OUT_WIN, getSeason(), tlwType.getNumber(),groupType.getNumber());
                RedisProxy.getInstance().getRedisSession().sAdd(winKey, 0, teamIds.toArray(new String[0]));
            }
    	}
    }
    
    
    /**
     * 根据小组排名前4进入S组后4进入A组
     * @param rank
     * @return
     */
    public TiberiumConst.TLWGroupType calcEliminationGroupType(int rank) {
    	int pickCount = TiberiumConstCfg.getInstance().getGuildPickCnt();
    	int zoneCount = 3;
    	int teamCount = TiberiumConstCfg.getInstance().getTeamCnt();
    	int limit = pickCount / (zoneCount * teamCount);
    	limit = limit / 2;
        if (rank >= 1 && rank <= limit) {
            return TiberiumConst.TLWGroupType.S_GROUP;
        }
        return TiberiumConst.TLWGroupType.A_GROUP;
    }
    
    /**
     * 淘汰赛匹配
     * 胜者组两两对战
     * 败者组两两对战
     * 如果败者组队伍数量多余胜者组则胜者组停战一期
     */
    public void kickOutMatch(){
    	for(TLWServer tlwType : this.TLWServerList){
    		for(TiberiumConst.TLWGroupType groupType : groupTypeList){
                String winKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_KICK_OUT_WIN, getSeason(),tlwType.getNumber(), groupType.getNumber());
                String loseKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_KICK_OUT_LOSE, getSeason(),tlwType.getNumber(), groupType.getNumber());
                HawkTuple2<List<String>, List<String>> winAndLoseList = onKickoutTeam(winKey, loseKey);
                List<String> newWinList =  winAndLoseList.first;
                List<String> newloseList =  winAndLoseList.second;
                
                List<TBLYSeasonData> newWinTeams = new ArrayList<>();
                List<TBLYSeasonData> newLoseTeams = new ArrayList<>();
                
                for(String teamId : newWinList){
                	 TBLYSeasonData seasonData = seasonDataMap.get(teamId);
                	 newWinTeams.add(seasonData);
                }
                for(String teamId : newloseList){
                	TBLYSeasonData seasonData = seasonDataMap.get(teamId);
                	newLoseTeams.add(seasonData);
               }
                
               if(newloseList.size() > newWinList.size()){
                   createRooms(stateData.getTermId(), generatePairings(newLoseTeams), tlwType, TLWGroup.valueOf(groupType.getNumber()), TiberiumConst.TLWBattleType.ELIMINATION_LOSS_GROUP_BATTLE);
               }else {
		           if(!newWinList.isEmpty()){
		               createRooms(stateData.getTermId(), generatePairings(newWinTeams), tlwType, TLWGroup.valueOf(groupType.getNumber()), TiberiumConst.TLWBattleType.ELIMINATION_WIN_GROUP_BATTLE);
		           }
		           if(!newloseList.isEmpty()){
		               createRooms(stateData.getTermId(), generatePairings(newLoseTeams), tlwType, TLWGroup.valueOf(groupType.getNumber()), TiberiumConst.TLWBattleType.ELIMINATION_LOSS_GROUP_BATTLE);
		           }
                }
            }
    	}
    }
    

    /**
     * 将队伍两两配对
     *
     * @param teamIds 打乱后的队伍列表
     * @return 返回配对后的队伍列表
     */
    public static List<HawkTuple2<TBLYSeasonData, TBLYSeasonData>> generatePairings(List<TBLYSeasonData> teams) {
        // 打乱队伍顺序
        Collections.shuffle(teams);
        List<HawkTuple2<TBLYSeasonData, TBLYSeasonData>> pairings = new ArrayList<>();
        // 遍历队伍列表，按照两两配对的方式组合
        for (int i = 0; i < teams.size(); i += 2) {
            pairings.add(new HawkTuple2<>(teams.get(i), teams.get(i + 1)));
        }
        return pairings;
    }
    
    /**
     * 淘汰队伍
     * 胜者组 战败队伍进入败者组
     * 败者组 战败队伍 彻底被淘汰
     * @param winKey
     * @param loseKey
     * @return
     */
    public HawkTuple2<List<String>, List<String>> onKickoutTeam(String winKey, String loseKey){
        Set<String> winGroup = RedisProxy.getInstance().getRedisSession().sMembers(winKey);
        Set<String> lostGroup = RedisProxy.getInstance().getRedisSession().sMembers(loseKey);
        
        List<String> oldWinList =  new ArrayList<>(winGroup);
        List<String> oldloseList =  new ArrayList<>(lostGroup);
        
        List<String> newWinList =  new ArrayList<>();
        List<String> newloseList =  new ArrayList<>();
        
        List<String> kickoutList =  new ArrayList<>();
        for(String teamId : oldWinList){
            TBLYSeasonData seasonData = seasonDataMap.get(teamId);
            if(seasonData.kickOutlose <= 0){
                seasonData.eGroupType = TiberiumConst.TLWEliminationGroupType.ELIMINATION_WIN.getValue();
                newWinList.add(teamId);
            }else {
                seasonData.eGroupType = TiberiumConst.TLWEliminationGroupType.ELIMINATION_LOSS.getValue();
                newloseList.add(teamId);
            }
            updateSeasonData(seasonData);
        }
        for(String teamId : oldloseList) {
            TBLYSeasonData seasonData = seasonDataMap.get(teamId);
            if(seasonData.kickOutlose <= 1){
                seasonData.eGroupType = TiberiumConst.TLWEliminationGroupType.ELIMINATION_LOSS.getValue();
                newloseList.add(teamId);
            }else {
                kickoutList.add(teamId);
            }
            updateSeasonData(seasonData);
        }
        for(String teamId : kickoutList){
            TBLYSeasonData seasonData = seasonDataMap.get(teamId);
            if(seasonData == null){
                continue;
            }
            seasonData.kickOutgroupType = seasonData.groupType;
            seasonData.kickOutTerm = getTermId();
            seasonData.groupType = TLWGroup.KICK_OUT_VALUE;
            updateSeasonData(seasonData);
        }
        
        RedisProxy.getInstance().getRedisSession().del(winKey);
        RedisProxy.getInstance().getRedisSession().del(loseKey);
        
        if(!newWinList.isEmpty()){
        	RedisProxy.getInstance().getRedisSession().sAdd(winKey, 0, newWinList.toArray(new String[0]));
        }
        if(!newloseList.isEmpty()){
        	RedisProxy.getInstance().getRedisSession().sAdd(loseKey, 0, newloseList.toArray(new String[0]));
        }
        return new HawkTuple2<>(newWinList, newloseList);
    }

    
    
    /**
     * 决赛匹配
     * 胜者组 和 败者组 剩余两支队伍决战
     */
    public void onFinalMatch(){
    	for(TLWServer tlwType : this.TLWServerList){
    		for(TiberiumConst.TLWGroupType groupType : groupTypeList){
    			 String winKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_KICK_OUT_WIN, getSeason(),tlwType.getNumber(), groupType.getNumber());
                 String loseKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_KICK_OUT_LOSE, getSeason(),tlwType.getNumber(), groupType.getNumber());
                HawkTuple2<List<String>, List<String>> winAndLoseList = onKickoutTeam(winKey, loseKey);
                List<String> newWinList =  winAndLoseList.first;
                List<String> newloseList =  winAndLoseList.second;
                if(newloseList.isEmpty() || newWinList.isEmpty()){
                    return;
                }
                TBLYSeasonData seasonData1 = seasonDataMap.get(newWinList.get(0));
                TBLYSeasonData seasonData2 = seasonDataMap.get(newloseList.get(0));
                
                List<HawkTuple2<TBLYSeasonData, TBLYSeasonData>> results = new ArrayList<>();
                results.add(new HawkTuple2<>(seasonData1, seasonData2));
                createRooms(stateData.getTermId(),results, tlwType, TLWGroup.valueOf(groupType.getNumber()), TiberiumConst.TLWBattleType.ELIMINATION_FINAL_GROUP_BATTLE);
            }
    	}
    }

    
    /**
     * 创建战斗
     * @param termId
     * @param results
     * @param matchType
     * @param groupType
     * @param battleType
     */
    public void createRooms(int termId, List<HawkTuple2<TBLYSeasonData, TBLYSeasonData>> results, TLWServer matchType, TLWGroup groupType, TiberiumConst.TLWBattleType battleType){
        createRooms(termId, results, matchType, groupType,battleType, 0);
    }
    public void createRooms(int termId, List<HawkTuple2<TBLYSeasonData, TBLYSeasonData>> results, TLWServer matchType, TLWGroup groupType, TiberiumConst.TLWBattleType battleType, 
    		int groupId){
        Map<String, Set<String>> serverIdToRoomIdMap = new HashMap<>();
        String roomKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM, getSeason(), termId);
        Map<String, String> roomMap = new HashMap<>();
        TiberiumSeasonTimeCfg timeCfg = getTimeCfgBySeasonAndTermId(getSeason(), termId);
        long warStartTime = timeCfg == null ? 0L : timeCfg.getWarStartTimeValue();
        for(HawkTuple2<TBLYSeasonData, TBLYSeasonData> result : results){
            try {
                GuildTeamData teamData1 = TBLYGuildTeamManager.getInstance().loadTeam(result.first.teamId);
                GuildTeamData teamData2 = TBLYGuildTeamManager.getInstance().loadTeam(result.second.teamId);
                if(teamData1 == null){
                    HawkLog.logPrintln("TBLYSeasonService createRoom teamData1 is null, teamId:{}", result.first);
                    continue;
                }
                if(teamData2 == null){
                    HawkLog.logPrintln("TBLYSeasonService createRoom teamData2 is null, teamId:{}", result.second);
                    continue;
                }
                GuildTeamRoomData roomData = createRoom(teamData1, teamData2,serverIdToRoomIdMap);
                if(roomData == null){
                    HawkLog.logPrintln("TBLYSeasonService createRoom roomData is null, teamId1:{},teamId2:{}", result.first, result.second);
                    continue;
                }
                roomData.matchType = matchType.getNumber();
                roomData.groupType = groupType.getNumber();
                roomData.battleType = battleType.getValue();
                roomData.groupId = groupId;
                roomMap.put(roomData.id, roomData.serialize());
                LogUtil.logTimberiumLeaguaMatchInfo(roomData.id, roomData.roomServerId, getSeason(), termId, teamData1.id, teamData1.serverId, teamData2.id,
                        teamData1.serverId, warStartTime, groupType.getNumber(), matchType.getNumber(), battleType.getValue());
            } catch (Exception e) {
                HawkException.catchException(e);
            }
        }
        //保存战斗数据
        RedisProxy.getInstance().getRedisSession().hmSet(roomKey, roomMap, 0);
        //每个服务器相关的战场数据
        for(String toServerId : serverIdToRoomIdMap.keySet()){
            Set<String> roomIds = serverIdToRoomIdMap.get(toServerId);
            RedisProxy.getInstance().getRedisSession().sAdd(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM_SERVER, getSeason(), termId, toServerId),
            		0, roomIds.toArray(new String[roomIds.size()]));
        }
    }
    
    /**
     * 创建战斗房间
     * @param teamData1
     * @param teamData2
     * @param serverIdToRoomIdMap
     * @return
     */
    public GuildTeamRoomData createRoom(GuildTeamData teamData1,GuildTeamData teamData2 , Map<String, Set<String>> serverIdToRoomIdMap){
        try {
            GuildTeamRoomData roomData = new GuildTeamRoomData(stateData.getTermId(), 0, teamData1, teamData2);
            roomData.roomServerId = teamData1.serverId.compareTo(teamData2.serverId) < 0 ? teamData1.serverId : teamData2.serverId;
            updateRoomIdToServer(serverIdToRoomIdMap, teamData1.serverId, roomData.id);
            updateRoomIdToServer(serverIdToRoomIdMap, teamData2.serverId, roomData.id);
            updateRoomIdToServer(serverIdToRoomIdMap, roomData.roomServerId, roomData.id);
            return roomData;
        }catch (Exception e) {
            HawkException.catchException(e);
            return null;
        }
    }
    
    
    /**
     * 把服务器和房间关联，关联的服务器包括，参与服和战场服
     * @param serverIdToRoomIdMap 服务器id关联战场idmap
     * @param serverId 服务器id
     * @param roomId 战场id
     */
    public void updateRoomIdToServer(Map<String, Set<String>> serverIdToRoomIdMap, String serverId, String roomId){
        if(!serverIdToRoomIdMap.containsKey(serverId)){
            serverIdToRoomIdMap.put(serverId, new HashSet<>());
        }
        serverIdToRoomIdMap.get(serverId).add(roomId);
    }
    
    
    
    /**
     * 全量加载参数联盟数据
     */
    public void loadSeasonTeamAndGuildTeam(){
        try {
            Map<String, TBLYSeasonData> dataMap = new ConcurrentHashMap<>();
            dataMap.putAll(loadSeasonTeamData());
            seasonDataMap = dataMap;
            seasonTeamDataMap = TBLYGuildTeamManager.getInstance().loadTeamMap(seasonDataMap.keySet());
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }
    
    
    /**
     * 最终发奖
     */
    public void sendFinalReward(){
        SeasonActivity activity = null;
        Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY.intValue());
        if (opActivity.isPresent()) {
            activity = opActivity.get();
        }
        for(String teamId : seasonDataMap.keySet()){
            GuildTeamData teamData = TBLYGuildTeamManager.getInstance().getTeamData(teamId);
            if(teamData == null){
                continue;
            }
            TBLYSeasonData seasonData = seasonDataMap.get(teamId);
            if(seasonData == null){
                continue;
            }
            if(activity != null){
                activity.addGuildGradeExpFromMatchRank(Activity.SeasonMatchType.S_TBLY, seasonData.teamType,teamData.guildId, seasonData.finalRank);
            }
            TiberiumSeasonRankAwardCfg cfg = getTLWRankAwardCfg(seasonData.finalRank, seasonData.teamType);
            if(cfg == null){
                continue;
            }
            Collection<String> memberIds = GuildService.getInstance().getGuildMembers(teamData.guildId);
            for(String playerId : memberIds){
                SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                        .setPlayerId(playerId)
                        .setMailId(MailConst.MailId.TIBERIUM_LEAGUA_RANK_REWARD)
                        .setAwardStatus(Const.MailRewardStatus.NOT_GET)
                        .addContents(seasonData.teamType,seasonData.finalRank)
                        .setRewards(cfg.getRewardList())
                        .build());
            }
        }
    }
    
    
    /**
     * 根据排行获取泰伯利亚联赛奖励配置
     * @param rank
     * @return
     */
    private TiberiumSeasonRankAwardCfg getTLWRankAwardCfg(int rank, int type) {
        ConfigIterator<TiberiumSeasonRankAwardCfg> its = HawkConfigManager.getInstance().getConfigIterator(TiberiumSeasonRankAwardCfg.class);
        for (TiberiumSeasonRankAwardCfg cfg : its) {
            if(cfg.getType() != type){
                continue;
            }
            int rankLow = cfg.getRankRange().first;
            int rankHight = cfg.getRankRange().second;
            if (rank >= rankLow && rank <= rankHight) {
                return cfg;
            }
        }
        return null;
    }
    
    
	/**
	 * 推送入围邮件
	 */
	private void sendPickedMail() {
		if(getTermId() != 1){
            return;
        }
		try {
            for(TBLYSeasonData seasonData : seasonDataMap.values()){
                try {
                    GuildTeamData teamData = TBLYGuildTeamManager.getInstance().getTeamData(seasonData.teamId);
                    if(teamData == null){
                        continue;
                    }
                    GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(teamData.guildId);
                    if(guildInfoObject == null){
                        continue;
                    }
                    
                    Set<String> members = TBLYGuildTeamManager.getInstance().getTeamPlayerIds(seasonData.teamId);
                    if(Objects.isNull(members)){
                    	continue;
                    }
                    for(String member : members){
                    	//入围邮件
                    	SystemMailService.getInstance().sendMail(MailParames.newBuilder()
            					.setPlayerId(member)
            					.setMailId(MailId.TIBERIUM_SEASON_PICKED)
            					.addContents(seasonData.teamType)
            					.build());
                    	//分组邮件
                    	SystemMailService.getInstance().sendMail(MailParames.newBuilder()
            					.setPlayerId(member)
            					.setMailId(MailId.TIBERIUM_SEASON_TEAM_GROUP)
            					.addContents(seasonData.teamType,seasonData.groupId)
            					.build());
                    }
                } catch (Exception e) {
                    HawkException.catchException(e);
                }
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
		
	}
	
	
    /**
     * 发放淘汰赛分组邮件
     */
    private void sendGroupMail() {
        if(getTermId() != TiberiumConstCfg.getInstance().getEliminationStartTermId()){
            return;
        }
        try {
            for(TBLYSeasonData seasonData : seasonDataMap.values()){
                try {
                    GuildTeamData teamData = TBLYGuildTeamManager.getInstance().getTeamData(seasonData.teamId);
                    if(teamData == null){
                        continue;
                    }
                    GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(teamData.guildId);
                    if(guildInfoObject == null){
                        continue;
                    }
                    Player leader = GlobalData.getInstance().makesurePlayer(guildInfoObject.getLeaderId());
                    TLWLeaguaGuildInfoUnit logUnit = new TLWLeaguaGuildInfoUnit(getSeason(), getTermId(), seasonData, guildInfoObject, teamData, leader);
                    TWLogUtil.logTimberiumLeaguaGuildInfo(logUnit);
                    
                    Set<String> members = TBLYGuildTeamManager.getInstance().getTeamPlayerIds(seasonData.teamId);
                    if(Objects.isNull(members)){
                    	continue;
                    }
                    for(String member : members){
                    	SystemMailService.getInstance().sendMail(MailParames.newBuilder()
            					.setPlayerId(member)
            					.setMailId(MailId.TIBERIUM_SEASON_FINAL_GROUP)
            					.addContents(seasonData.teamType,seasonData.groupType)
            					.build());
                    }
                } catch (Exception e) {
                    HawkException.catchException(e);
                }
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }
    
    
    
    /**
     * 排行榜
     */
    public void doRank(){
        if(getBigState() == TBLYWarStateEnum.SEASON_BIG_NOT_OPEN){
            return;
        }
        if(getBigState() == TBLYWarStateEnum.SEASON_BIG_SIGNUP || 
        		getBigState() == TBLYWarStateEnum.SEASON_BIG_GROUP_WAIT){
            doBeforeRank();
        }else {
            doAfterRank();
        }
    }
    
    

    /**
     * 入围排行榜
     */
    public void doBeforeRank(){
        GetTLWGuildRankResp.Builder builder = GetTLWGuildRankResp.newBuilder();
        String rankKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_RANK, getSeason());
        Set<Tuple> tuples  = RedisProxy.getInstance().getRedisSession().zRevrangeWithScores(rankKey, 0, TiberiumConstCfg.getInstance().getGuildPickCnt() - 1,0);
        if(tuples.isEmpty()){
            return;
        }
        Set<String> teamIds = new HashSet<>();
        for (Tuple tuple : tuples) {
            String teamId = tuple.getElement();
            teamIds.add(teamId);
        }
        Map<String, GuildTeamData> teamMap = TBLYGuildTeamManager.getInstance().loadTeamMap(teamIds);
        int rank = 1;
        for (Tuple tuple : tuples) {
            String teamId = tuple.getElement();
            GuildTeamData teamData = teamMap.get(teamId);
            if(teamData == null){
                continue;
            }
            long power = (long) tuple.getScore();
            TLWGuildRank.Builder rankInfo = TLWGuildRank.newBuilder();
            TLWGuildInfo.Builder guildBuilder = TLWGuildInfo.newBuilder();
            guildBuilder.setBaseInfo(teamData.genBaseInfo());
            guildBuilder.setBattlePoint(power);
            rankInfo.setGuildInfo(guildBuilder);
            rankInfo.setRank(rank);
            builder.addRankInfo(rankInfo);
            rank++;
        }
        this.joinRank = builder;
    }


    /**
     * 战绩排行榜
     */
	public void doAfterRank() {
		Map<String, TBLYSeasonData> dataMap = loadSeasonTeamData();
		if (dataMap.isEmpty()) {
			return;
		}
		// 战力
		this.loadTeamPower();
		// 小队
		Map<String, GuildTeamData> teamMap = TBLYGuildTeamManager.getInstance().loadTeamMap(dataMap.keySet());
		// 分组
		Map<Integer, List<TBLYSeasonData>> teamTypeMap = new HashMap<>();
		for (TBLYSeasonData seasonData : dataMap.values()) {
			List<TBLYSeasonData> typeList = teamTypeMap.get(seasonData.teamType);
			if (Objects.isNull(typeList)) {
				typeList = new ArrayList<>();
				teamTypeMap.put(seasonData.teamType, typeList);
			}
			typeList.add(seasonData);
		}
		// 排序
		Map<Integer, GetTLWGuildRankResp.Builder> battleRankBuilderMap = new HashMap<>();
		for (Entry<Integer, List<TBLYSeasonData>> entry : teamTypeMap.entrySet()) {
			GetTLWGuildRankResp.Builder builder = GetTLWGuildRankResp.newBuilder();
			int type = entry.getKey();
			List<TBLYSeasonData> dataList = entry.getValue();
			int rank = 1;
			dataList.sort(new TLWTeamTotalRankComparator());
			for (TBLYSeasonData seasonData : dataList) {
				GuildTeamData teamData = teamMap.get(seasonData.teamId);
				if (teamData == null) {
					continue;
				}
				TLWGuildRank.Builder rankInfo = TLWGuildRank.newBuilder();
				TLWGuildInfo.Builder guildBuilder = TLWGuildInfo.newBuilder();
				guildBuilder.setBaseInfo(teamData.genBaseInfo());
				guildBuilder.setBattlePoint(this.teamPowerMap.getOrDefault(teamData.id, 0l));
				guildBuilder.setGroup(TLWGroup.valueOf(seasonData.groupType));
				if (seasonData.eGroupType > 0) {
					guildBuilder.setEliminationGroup(PBTLWEliminationGroup.valueOf(seasonData.eGroupType));
				}
				if (seasonData.groupType == TiberiumConst.TLWGroupType.KICK_OUT.getNumber()
						|| getBigState() == TBLYWarStateEnum.SEASON_BIG_END_SHOW) {
					guildBuilder.setCurrRank(rank);
				}
				rankInfo.setGuildInfo(guildBuilder);
				rankInfo.setRank(rank);
				builder.addRankInfo(rankInfo);
				rank++;
			}
			battleRankBuilderMap.put(type, builder);
		}
		this.battleRank = battleRankBuilderMap;
		// 入围名次数据
		String joinKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_JOIN, getSeason());
		Map<String, String> joinRank = RedisProxy.getInstance().getRedisSession().hGetAll(joinKey);
		List<TLWGuildRank> rblist = new ArrayList<>();
		GetTLWGuildRankResp.Builder joinRankBuilder = GetTLWGuildRankResp.newBuilder();
		for (Entry<String, String> entry : joinRank.entrySet()) {
			String teamId = entry.getKey();
			int rank = Integer.parseInt(entry.getValue());
			GuildTeamData teamData = teamMap.get(teamId);
			if (teamData == null) {
				continue;
			}
			TBLYSeasonData sdata = dataMap.get(teamId);
			if(sdata == null){
				continue;
			}
			TLWGuildRank.Builder rankInfo = TLWGuildRank.newBuilder();
			TLWGuildInfo.Builder guildBuilder = TLWGuildInfo.newBuilder();
			guildBuilder.setBaseInfo(teamData.genBaseInfo());
			guildBuilder.setBattlePoint(sdata.initPower);
			rankInfo.setGuildInfo(guildBuilder);
			rankInfo.setRank(rank);
			rblist.add(rankInfo.build());
			
		}
		Collections.sort(rblist, new Comparator<TLWGuildRank>() {
			@Override
			public int compare(TLWGuildRank o1, TLWGuildRank o2) {
				return o1.getRank() - o2.getRank();
			}
		});
		
		joinRankBuilder.addAllRankInfo(rblist);
		this.joinRank = joinRankBuilder;

	}
    
	/**
	 * 更新小队战力
	 */
	protected void updateTeamPower(){
        try {
            if(seasonDataMap == null || seasonDataMap.isEmpty()){
                return;
            }
            Map<String, String> powerMap = new HashMap<>();
            for(TBLYSeasonData seasonData : seasonDataMap.values()){
                GuildTeamData teamData = TBLYGuildTeamManager.getInstance().getTeamData(seasonData.teamId);
                if(teamData == null){
                    continue;
                }
                refreshTeamInfo(teamData);
                powerMap.put(teamData.id, String.valueOf(teamData.battlePoint));
            }
            if(!powerMap.isEmpty()){
            	String powerKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_POWER, getSeason());
            	RedisProxy.getInstance().getRedisSession().hmSet(powerKey, powerMap, TiberiumConst.TLW_EXPIRE_SECONDS);
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }
	
	
	/**
	 * 更新小队战力
	 */
	protected void loadTeamPower(){
        try {
        	Map<String, Long> teamPowerMapTemp = new HashMap<>();
        	String powerKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_POWER, getSeason());
     		Map<String, String> powerMap = RedisProxy.getInstance().getRedisSession().hGetAll(powerKey);
     		if(Objects.isNull(powerMap) || powerMap.isEmpty()){
     			return;
     		}
        	for(Map.Entry<String, String> entry : powerMap.entrySet()){
        		teamPowerMapTemp.put(entry.getKey(), Long.parseLong(entry.getValue()));
        	}
            this.teamPowerMap = teamPowerMapTemp;
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    
    /**
     * 更新队伍信息
     */
	public void updateTeamInfo(){
        try {
            Set<String> guildIds = new HashSet<>();
            for(TBLYSeasonData seasonData : seasonDataMap.values()){
                if(seasonData.groupType == TLWGroup.NOMAL_GROUP_VALUE || seasonData.groupType == TLWGroup.KICK_OUT_VALUE){
                    continue;
                }
                String teamId = seasonData.teamId;
                GuildTeamData teamData = TBLYGuildTeamManager.getInstance().getTeamData(teamId);
                if(teamData == null){
                    continue;
                }
                guildIds.add(teamData.guildId);
                long battlePoint = 0L;
                long matchPower = 0L;
                Map<String, String> playerAuth = new HashMap<>();
                Map<String, String> playerTeam = new HashMap<>();
                Set<String> playerIds = TBLYGuildTeamManager.getInstance().getTeamPlayerIds(teamId);
                if(playerIds != null){
                    for (String playerId : playerIds) {
                        try {
                            GuildTeamPlayerData playerData = TBLYGuildTeamManager.getInstance().getPlayerData(playerId);
                            if(playerData == null){
                                continue;
                            }
                            if(!teamId.equals(playerData.teamId)){
                                continue;
                            }
                            Player player = GlobalData.getInstance().makesurePlayer(playerId);
                            if (player == null) {
                                continue;
                            }
                            if(!player.getGuildId().equals(teamData.guildId)){
                                continue;
                            }
                            long playerStrength = player.getStrength();
                            matchPower += playerStrength;
                            long noArmyPoint = player.getNoArmyPower();
                            battlePoint += noArmyPoint;
                            playerAuth.put(playerData.id, String.valueOf(playerData.auth));
                            playerTeam.put(playerId, teamId);
                            HawkLog.logPrintln("TiberiumLeaguaWarService flushSignerPlayerInfo, playerId:{}, guildId: {}, serverId: {}, selfPowar: {}, season: {}, termId: {}, group: {}",
                                    playerId, teamId, teamData.serverId, noArmyPoint, getSeason(), getTermId(), seasonData.groupType);
                            LogUtil.logTimberiumLeaguaPlayerWarInfo(playerId, teamId, teamData.serverId, noArmyPoint, getSeason(), getTermId(), seasonData.groupType);
                        } catch (Exception e) {
                            HawkException.catchException(e);
                        }
                    }
                }
                teamData.matchPower = matchPower;
                teamData.battlePoint = battlePoint;
                teamData.memberCnt = playerAuth.size();
                teamData.serverId = GsConfig.getInstance().getServerId();
                try {
                    GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(teamData.guildId);
                    if(guildInfoObject != null){
                        teamData.guildName = guildInfoObject.getName();
                        teamData.guildTag = guildInfoObject.getTag();
                        teamData.guildFlag = guildInfoObject.getFlagId();
                    }
                } catch (Exception e) {
                    HawkException.catchException(e);
                }
                TBLYGuildTeamManager.getInstance().updateTeam(teamData);
                if(!playerAuth.isEmpty()){
                    //把数据存入redis
                	String signUpPlayerKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_SIGNUP_PLAYER, getSeason(), getTermId(), teamId);
                    RedisProxy.getInstance().getRedisSession().hmSet(signUpPlayerKey, playerAuth, 0);
                }
                if(!playerTeam.isEmpty()){
                	String serverId = GsConfig.getInstance().getServerId();
                	String teamPlayerKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_MANAGE_TEAM_PLAYER_RECORD, getSeason(), getTermId(), serverId);
                    RedisProxy.getInstance().getRedisSession().hmSet(teamPlayerKey, playerTeam, (int)TimeUnit.DAYS.toSeconds(30));
                }
                try {
                    HawkLog.logPrintln("TiberiumLeaguaWarService flushSignerGuildInfo, guildId: {}, guildName: {} , serverId: {}, memberCnt: {}, totalPowar: {}, season: {}, termId: {}", teamData.id, teamData.guildName, teamData.serverId, teamData.memberCnt, teamData.battlePoint, getSeason(), getTermId());
                    LogUtil.logTimberiumLeaguaGuildWarInfo(teamData.id, teamData.guildName, teamData.serverId, teamData.memberCnt, teamData.battlePoint, getSeason(), getTermId(), seasonData.groupType, seasonData.teamType);
                } catch (Exception e) {
                    HawkException.catchException(e);
                }
            }
            List<TWGuildData> twGuildDataList = new ArrayList<>();
            for(String guildId : guildIds) {
                try {
                    //生成联盟数据
                    GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(guildId);
                    twGuildDataList.add(buildTWGuildData(guildInfoObject));
                } catch (Exception e) {
                    HawkException.catchException(e);
                }
            }
            RedisProxy.getInstance().updateTWGuildData(twGuildDataList, 10000*getSeason()+getTermId());
        } catch (Exception e) {
            HawkException.catchException(e);
        }
	}
	
	/**
	 * 组建联盟信息
	 * @param guildInfoObject
	 * @return
	 */
	public TWGuildData buildTWGuildData(GuildInfoObject guildInfoObject){
        TWGuildData guildData = new TWGuildData();
        guildData.setId(guildInfoObject.getId());
        guildData.setServerId(guildInfoObject.getServerId());
        guildData.setName(guildInfoObject.getName());
        guildData.setTag(guildInfoObject.getTag());
        guildData.setFlag(guildInfoObject.getFlagId());
        guildData.setMemberCnt(50);
        guildData.setTotalPower(guildInfoObject.getLastRankPower());
        guildData.setTimeIndex(0);
        guildData.setEloScore(0);
        guildData.setGuildPower(guildInfoObject.getLastRankPower());
        return guildData;
    }
    
    
	/**
	 * 初始化战场
	 */
    public void createBattleRoom(){
        //创建战场
        Map<String, String> roomStrMap = new HashMap<>();
        for(GuildTeamRoomData roomData : roomDataMap.values()){
            try {
                if(roomData == null || !roomData.roomServerId.equals(GsConfig.getInstance().getServerId())){
                    continue;
                }
                TBLYSrasonBattleTeamData battleTeamA = battleTeamDataMap.get(roomData.campA);
                TBLYSrasonBattleTeamData battleTeamB = battleTeamDataMap.get(roomData.campB);
                GuildTeamData teamDataA = battleTeamA.getTeamData();
                GuildTeamData teamDataB = battleTeamB.getTeamData();
                TBLYExtraParam extParm = new TBLYExtraParam();
                extParm.setCampAGuild(teamDataA.guildId);
                extParm.setCampAGuildName(teamDataA.guildName);
                extParm.setCampAGuildTag(teamDataA.guildTag);
                extParm.setCampAServerId(teamDataA.serverId);
                extParm.setCampAguildFlag(teamDataA.guildFlag);
                extParm.setCampAPlayers(TBLYGuildTeamManager.getInstance().getAllTWPlayerData(battleTeamA.getTeamPlayers()));
                /**-----------------------------------------------------------------------------------------------------------------------------------------------*/
                /**-----------------------------------------------------------------------------------------------------------------------------------------------*/
                extParm.setCampBGuild(teamDataB.guildId);
                extParm.setCampBGuildName(teamDataB.guildName);
                extParm.setCampBGuildTag(teamDataB.guildTag);
                extParm.setCampBServerId(teamDataB.serverId);
                extParm.setCampBguildFlag(teamDataB.guildFlag);
                extParm.setCampAPlayers(TBLYGuildTeamManager.getInstance().getAllTWPlayerData(battleTeamB.getTeamPlayers()));
                extParm.setLeaguaWar(true);
                TBLYRoomManager.getInstance().creatNewBattle(HawkTime.getMillisecond(), HawkTime.getMillisecond() + TimeUnit.HOURS.toMillis(1), roomData.id, extParm);
                roomData.roomState = 1;
                roomStrMap.put(roomData.id, roomData.serialize());
                LogUtil.logTimberiumLeaguaManageEndInfo(roomData.id, roomData.roomServerId, getSeason(), getTermId(), teamDataA.id, teamDataA.serverId, teamDataA.guildName,
                        teamDataA.battlePoint, teamDataB.id, teamDataB.serverId, teamDataB.guildName, teamDataB.battlePoint);
            } catch (Exception e) {
                HawkException.catchException(e);
            }
        }
        if(!roomStrMap.isEmpty()){
        	RedisProxy.getInstance().getRedisSession().hmSet(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM, getSeason(), getTermId()), roomStrMap, 0);
        }
    }
    
    
    /**
     * 匹配结束后加载战场数据
     */
    public void loadBattleRoom() {
        try {
            //房间id的key，只存了和本服相关的房间的id
            String roomServerKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM_SERVER, stateData.getSeason(), stateData.getTermId(), GsConfig.getInstance().getServerId());
            //本服参与或开在本服战场的id
            Set<String> roomIds = RedisProxy.getInstance().getRedisSession().sMembers(roomServerKey);
            //如果为空直接返回
            if (roomIds == null || roomIds.isEmpty()) {
                HawkLog.logPrintln("TBLYWarService loadBattleRoom roomIds is empty");
                return;
            }
            //房间加载开始
            HawkLog.logPrintln("TBLYWarService loadBattleRoom start termId:{}, size:{}", getTermId(), roomIds.size());
            //加载房间redis数据
            List<String> roomList = RedisProxy.getInstance().getRedisSession().hmGet(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM, getSeason(), getTermId()), roomIds.toArray(new String[0]));
            //如果为空就返回，一般不会为空上面id已经校验一遍了，以防万一再检查一次
            if (roomList == null || roomList.isEmpty()) {
                HawkLog.logPrintln("TBLYWarService loadBattleRoom roomList is empty");
                return;
            }
            Map<String, TBLYSrasonBattleTeamData> battleTeamDataMapTemp = new ConcurrentHashMap<>();
            //遍历房间数据
            for (String roomStr : roomList) {
                try {
                    HawkLog.logPrintln("TBLYWarService loadBattleRoom roomData:{}", roomStr);
                    //解析房间数据
                    GuildTeamRoomData roomData = GuildTeamRoomData.unSerialize(roomStr);
                    //数据为空的话跳过
                    if (roomData == null) {
                        HawkLog.logPrintln("TBLYWarService loadBattleRoom data is null roomData:{}", roomStr);
                        continue;
                    }
                    //放入房间数据缓存中
                    roomDataMap.put(roomData.id, roomData);
                    //关联小队id和房间id
                    battleTeamDataMapTemp.put(roomData.campA, new TBLYSrasonBattleTeamData(roomData.campA, roomData.id));
                    battleTeamDataMapTemp.put(roomData.campB, new TBLYSrasonBattleTeamData(roomData.campB, roomData.id));
                } catch (Exception e) {
                    HawkException.catchException(e);
                    HawkLog.logPrintln("TBLYWarService loadBattleRoom error roomData:{}", roomStr);
                }
            }
            //重新赋值
            this.battleTeamDataMap = battleTeamDataMapTemp;
            //加载参与的小队数据
            loadBattleTeam();
            //加载参与的玩家数据
            loadBattlePlayer();
            //房间加载结束
            HawkLog.logPrintln("TBLYWarService loadBattleRoom end");
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }
    
    
    /**
     * 加载参战队伍数据
     * @param teamIds 参战队伍id
     */
    public void loadBattleTeam(){
        try {
            //如果为空直接返回
            if(this.battleTeamDataMap == null || battleTeamDataMap.isEmpty()){
                HawkLog.logPrintln("TBLYWarService loadBattleTeam teamIds is empty");
                return;
            }
            Set<String> teamIds= new HashSet<>();
            teamIds.addAll(this.battleTeamDataMap.keySet());
            //本服id，用于判断小队是否是本服的
            String serverId = GsConfig.getInstance().getServerId();
            //加载相关小队数据
            List<GuildTeamData> list = TBLYGuildTeamManager.getInstance().loadTeams(teamIds);
            //遍历小队数据
            for(GuildTeamData teamData : list) {
                try {
                    //加入参战小队数据缓存中
                	this.battleTeamDataMap.get(teamData.id).updateTeamData(teamData);
                    //如果小队是本服小队，更新本服小队数据
                    if(serverId.equals(teamData.serverId)){
                        TBLYGuildTeamManager.getInstance().updataFromBattle(teamData);
                    }
                }catch (Exception e){
                    HawkException.catchException(e);
                }
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 加载小队参战玩家列表
     */
    public void loadBattlePlayer(){
        try {
            //如果为空直接返回
            if(this.battleTeamDataMap == null || this.battleTeamDataMap.isEmpty()){
                HawkLog.logPrintln("TBLYWarService loadBattlePlayer teamIds is empty");
                return;
            }
            for(Entry<String, TBLYSrasonBattleTeamData> entry : this.battleTeamDataMap.entrySet()){
            	String teamId = entry.getKey();
            	TBLYSrasonBattleTeamData battleTeam = entry.getValue();
                try {
                    HawkLog.logPrintln("TBLYWarService loadBattlePlayer team start teamId:{}", teamId);
                    //获得当期期参与人的信息
                    String signUpPlayerKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_SIGNUP_PLAYER, getSeason(), getTermId(), teamId);
                    Map<String, String> playerAuth = RedisProxy.getInstance().getRedisSession().hGetAll(signUpPlayerKey);
                    //如果为空直接跳过
                    if(playerAuth == null || playerAuth.isEmpty()){
                        HawkLog.logPrintln("TBLYWarService loadBattlePlayer playerAuth is empty, teamId:{}",teamId);
                        continue;
                    }
                    //更新玩家ID列表
                    battleTeam.updateTeamPlayers(playerAuth.keySet());
                    //遍历玩家
                    for(String playerId : playerAuth.keySet()){
                        try {
                            HawkLog.logPrintln("TBLYWarService loadBattlePlayer playerId:{},auth:{}", playerId, playerAuth.get(playerId));
                            //关联玩家和小队
                            playerIdToTeamId.put(playerId, teamId);
                        } catch (Exception e) {
                            HawkException.catchException(e);
                            HawkLog.logPrintln("TBLYWarService loadBattlePlayer player error playerId:{}", playerId);
                        }
                    }
                    HawkLog.logPrintln("TBLYWarService loadBattlePlayer team end teamId:{}", teamId);
                } catch (Exception e) {
                    HawkException.catchException(e);
                    HawkLog.logPrintln("TBLYWarService loadBattlePlayer team error teamId:{}", teamId);
                }
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    
    /**
     * 加载战斗结果数据
     */
    public void loadBattleResult(){
        Set<String> roomIds = roomDataMap.keySet();
        //如果为空直接返回
        if(roomIds.isEmpty()){
            HawkLog.logPrintln("TBLYWarService loadBattleRoomResult roomIds is empty");
            return;
        }
        HawkLog.logPrintln("TBLYWarService loadBattleRoomResult start");
        //加载Redis房间数据
        List<String> roomList = RedisProxy.getInstance().getRedisSession().hmGet(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM,getSeason(), getTermId()), roomIds.toArray(new String[0]));
        //如果为空就返回，一般不会为空上面id已经校验一遍了，以防万一再检查一次
        if(roomList == null || roomList.isEmpty()){
            HawkLog.logPrintln("TBLYWarService loadBattleRoomResult roomList is empty");
            return;
        }
        HawkLog.logPrintln("TBLYWarService loadBattleRoomResult size:{}", roomList.size());
        //遍历房间数据
        for(String roomStr : roomList){
            try {
                HawkLog.logPrintln("TBLYWarService loadBattleRoomResult roomData:{}", roomStr);
                //解析房间数据
                GuildTeamRoomData roomData = GuildTeamRoomData.unSerialize(roomStr);
                //如果为空直接跳过
                if(roomData == null){
                    HawkLog.logPrintln("TBLYWarService loadBattleRoomResult data is null roomData:{}", roomStr);
                    continue;
                }
                //加入房间数据缓存
                roomDataMap.put(roomData.id, roomData);
                updateRoomScore(roomData);
            } catch (Exception e) {
                HawkException.catchException(e);
                HawkLog.logPrintln("TBLYWarService loadBattleRoomResult error roomData:{}", roomStr);
            }
        }

    }
    
    /**
     * 更新积分数据
     * @param roomData
     */
    public void updateRoomScore(GuildTeamRoomData roomData){
        if(roomData == null){
            return;
        }
        updateTeamScore(roomData.campA, roomData.scoreA, roomData.winnerId.equals(roomData.campA));
        updateTeamScore(roomData.campB, roomData.scoreB, roomData.winnerId.equals(roomData.campB));
    }
    
    /**
     * 更新积分数据
     * @param teamId
     * @param score
     * @param isWin
     */
    public void updateTeamScore(String teamId, long score, boolean isWin){
        if(HawkOSOperator.isEmptyString(teamId)){
            return;
        }
        //只更新自己服的小队数据
        GuildTeamData teamData = TBLYGuildTeamManager.getInstance().getTeamData(teamId);
        if(teamData != null){
            teamData.score = score;
            TBLYGuildTeamManager.getInstance().updateTeam(teamData);
            TBLYSeasonData seasonData = seasonDataMap.get(teamId);
            if(seasonData != null){
                if(getBigState() == TBLYWarStateEnum.SEASON_BIG_GROUP){
                    if(isWin){
                        seasonData.winCnt++;
                    }else {
                        seasonData.loseCnt++;
                    }
                }
                if(getBigState() == TBLYWarStateEnum.SEASON_BIG_KICK_OUT){
                    if(!isWin){
                        seasonData.kickOutlose++;
                    }
                }
                if(getBigState() == TBLYWarStateEnum.SEASON_BIG_FINAL){
                    if(!isWin){
                       seasonData.kickOutTerm = getTermId();
                    }
                }
                seasonData.score += score;
                String dataKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_DATA, getSeason());
                RedisProxy.getInstance().getRedisSession().hSet(dataKey, seasonData.teamId, seasonData.serialize(), 0);
                try {
                    GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(teamData.guildId);
                    Player leader = GlobalData.getInstance().makesurePlayer(guildInfoObject.getLeaderId());
                    TLWLeaguaGuildInfoUnit logUnit = new TLWLeaguaGuildInfoUnit(getSeason(), getTermId(), seasonData, guildInfoObject, teamData, leader);
                    TWLogUtil.logTimberiumLeaguaGuildInfo(logUnit);
                } catch (Exception e) {
                    HawkException.catchException(e);
                }


            }
        }
        //修改一下缓存数据
        TBLYSrasonBattleTeamData battleTeamData = battleTeamDataMap.get(teamId);
        if(battleTeamData != null){
        	battleTeamData.getTeamData().score = score;
            //TBLYGuildTeamManager.getInstance().updateTeam(battleTeamData);
        }
    }
    
    
    /**
     * 发奖
     * @return 执行结果
     */
    public void sendAward(){
        Map<String, Long> playerScoreMap = new HashMap<>();
        Map<String, Long> guildScoreMap = new HashMap<>();
        for(TBLYSeasonData seasonData : seasonDataMap.values()) {
            if (seasonData.groupType == TLWGroup.NOMAL_GROUP_VALUE || seasonData.groupType == TLWGroup.KICK_OUT_VALUE) {
                continue;
            }
            String teamId = seasonData.teamId;
            GuildTeamData teamData = TBLYGuildTeamManager.getInstance().getTeamData(teamId);
            if (teamData == null) {
                continue;
            }
            String teamAwardKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_TEAM_AWARD, getSeason(), getTermId());
            boolean teamGetLock = RedisProxy.getInstance().getRedisSession().hSetNx(teamAwardKey, teamId, String.valueOf(HawkTime.getMillisecond())) > 0;
            if (!teamGetLock) {
                continue;
            }
            TBLYSrasonBattleTeamData battleTeam = this.battleTeamDataMap.get(teamId);
            if(Objects.isNull(battleTeam)){
            	 continue;
            }
            String roomId = battleTeam.getBattleRoomId();
            if (HawkOSOperator.isEmptyString(roomId)) {
                HawkLog.logPrintln("TBLYWarService sendAward roomId is null teamId:{}", teamId);
                continue;
            }
            GuildTeamRoomData roomData = roomDataMap.get(roomId);
            if (roomData == null) {
                continue;
            }
            long selfScore = 0;
            //long oppScore = 0;
            if(teamId.equals(roomData.campA)){
                selfScore = roomData.scoreA;
                //oppScore = roomData.scoreB;
            }else {
                selfScore = roomData.scoreB;
               //oppScore = roomData.scoreA;
            }
            boolean isWin = roomData.winnerId.equals(teamId);
            MailConst.MailId mailId = isWin? MailConst.MailId.TBLY_GUILD_WIN_MAIL : MailConst.MailId.TBLY_GUILD_LOSE_MAIL;
            MailConst.MailId selfMailId = isWin ? MailConst.MailId.TBLY_SELF_WIN_MAIL : MailConst.MailId.TBLY_SELF_LOSE_MAIL;
            TiberiumGuildAwardCfg rewardCfg = getGuildAwardCfg(isWin);
            List<TWPlayerSeasonScoreLogUnit> pSeasonScorgLogUnitList = new ArrayList<>();
            for (String playerId : TBLYGuildTeamManager.getInstance().getTeamPlayerIds(teamId)) {
                try {
                    String playerAwardKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_PLAYER_AWARD, getSeason(), getTermId());
                    boolean playerGetLock = RedisProxy.getInstance().getRedisSession().hSetNx(playerAwardKey, playerId, String.valueOf(HawkTime.getMillisecond())) > 0;
                    if (!playerGetLock) {
                        continue;
                    }
                    SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                            .setPlayerId(playerId)
                            .setMailId(mailId)
                            .setAwardStatus(Const.MailRewardStatus.NOT_GET)
                            .addRewards(rewardCfg.getRewardItem())
                            .build());
                    String scoreStr = RedisProxy.getInstance().getRedisSession().hGet(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_PLAYER_SCORE, getSeason(), getTermId()), playerId);
                    if(!HawkOSOperator.isEmptyString(scoreStr)){
                        try {
                            long playerSelfScore = Long.parseLong(scoreStr);
                            TiberiumPersonAwardCfg selfCfg = getPersonAwardCfg(playerSelfScore, isWin);
                            GuildTeamPlayerData playerData = TBLYGuildTeamManager.getInstance().load(playerId);
                            if(selfCfg != null && playerData != null && !playerData.isMidwayQuit){
                                SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                                        .setPlayerId(playerId)
                                        .setMailId(selfMailId)
                                        .addContents(playerSelfScore)
                                        .setAwardStatus(Const.MailRewardStatus.NOT_GET)
                                        .addRewards(selfCfg.getRewardItem())
                                        .build());
                            }
                            playerScoreMap.put(playerId, playerSelfScore);
                            pSeasonScorgLogUnitList.add(new TWPlayerSeasonScoreLogUnit(playerId,  roomData.id, roomData.roomServerId, teamData.id, teamData.guildName, playerSelfScore, isWin));
                            ActivityManager.getInstance().postEvent(new TWScoreEvent(playerId, playerSelfScore, true,1));
                        } catch (Exception e) {
                            HawkException.catchException(e);
                        }
                    }
                    MissionManager.getInstance().postMsg(playerId, new EventTiberiumWar());
                } catch (Exception e) {
                    HawkException.catchException(e);
                }
            }
            TWLogUtil.logTimberiumLeaguaPlayerScore(pSeasonScorgLogUnitList);
            long guildScore = guildScoreMap.getOrDefault(teamData.guildId, 0L);
            guildScore = guildScore + selfScore;
            guildScoreMap.put(teamData.guildId, guildScore);
            try {
                ActivityService.getInstance().dealMsg(GameConst.MsgId.ON_GUILD_BACK_DROP, new GuildBackDropInvoker(new ArrayList<>(TBLYGuildTeamManager.getInstance().getTeamPlayerIds(teamId))));
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
        for(String guildId : guildScoreMap.keySet()){
            TBLYSeasonService.getInstance().addGuildScore(guildId, guildScoreMap.get(guildId), playerScoreMap);
        }
    }
    
    /**
     * 增加联盟积分
     * @param guildId
     * @param score
     * @param playerScoreMap
     */
    public void addGuildScore(String guildId, long score, Map<String, Long> playerScoreMap){
        try {
            if(getBigState() == TBLYWarStateEnum.SEASON_BIG_NOT_OPEN){
                return;
            }
            String json = RedisProxy.getInstance().getRedisSession().hGet(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_GUILD_DATA, getSeason()), guildId);
            TBLYSrasonGuildData guildData = HawkOSOperator.isEmptyString(json) ? new TBLYSrasonGuildData(guildId) : TBLYSrasonGuildData.unSerialize(json);
            if(guildData == null){
                return;
            }
            guildData.score  = guildData.score + score;
            RedisProxy.getInstance().getRedisSession().hSet(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_GUILD_DATA, getSeason()), guildId, guildData.serialize());
            Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
            List<String> memberList = RedisProxy.getInstance().getRedisSession().hmGet(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_PLAYER_DATA,getSeason()), memberIds.toArray(new String[0]));
            //如果为空就返回，一般不会为空上面id已经校验一遍了，以防万一再检查一次
            if(memberList == null || memberList.isEmpty()){
                return;
            }
            Map<String, TBLYSeasonPlayerData> playerDataMap = new HashMap<>();
            Map<String, String> playerDataStrMap = new HashMap<>();
            //遍历房间数据
            for(String memberStr : memberList) {
                try {
                    //解析房间数据
                    TBLYSeasonPlayerData playerData = TBLYSeasonPlayerData.unSerialize(memberStr);
                    //如果为空直接跳过
                    if (playerData == null) {
                        continue;
                    }
                    playerDataMap.put(playerData.id, playerData);
                } catch (Exception e) {
                    HawkException.catchException(e);
                }
            }
            List<TWSelfRewardLogUnit> selfRewardLogUnitList = new ArrayList<>();
            for(String memberId : memberIds){
                try {
                    TBLYSeasonPlayerData playerData = playerDataMap.getOrDefault(memberId, new TBLYSeasonPlayerData(memberId));
                    addPlayerScore(guildId, playerData, guildData.score, playerScoreMap.getOrDefault(memberId, 0L), selfRewardLogUnitList);
                    playerDataStrMap.put(playerData.id, playerData.serialize());
                }catch (Exception e){
                    HawkException.catchException(e);
                }
            }
            RedisProxy.getInstance().getRedisSession().hmSet(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_PLAYER_DATA,getSeason()), playerDataStrMap, 0);
            TWLogUtil.logTimberiumLeaguaSelfReward(selfRewardLogUnitList);
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    
    /**
     * 增加玩家积分
     * @param guildId
     * @param playerData
     * @param guildScore
     * @param addplayerScore
     * @param selfRewardLogUnitList
     */
    public void addPlayerScore(String guildId, TBLYSeasonPlayerData playerData, long guildScore, long addplayerScore,List<TWSelfRewardLogUnit> selfRewardLogUnitList){
        if(playerData == null){
            return;
        }
        playerData.score = playerData.score + addplayerScore;
        playerData.guildscore = guildScore;
        ConfigIterator<TiberiumSeasonGuildAwardCfg> guildAwardCfgs = HawkConfigManager.getInstance().getConfigIterator(TiberiumSeasonGuildAwardCfg.class);
        for(TiberiumSeasonGuildAwardCfg guildAwardCfg : guildAwardCfgs){
            if(!playerData.guildRewarded.contains(guildAwardCfg.getId()) && guildAwardCfg.getScore() <= guildScore){
                playerData.guildRewarded.add(guildAwardCfg.getId());
                // 赛季联盟积分奖励
                SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                        .setPlayerId(playerData.id)
                        .setMailId(MailConst.MailId.TIBERIUM_SEASON_GUILD_SCORE_REWARD)
                        .addContents(guildAwardCfg.getScore())
                        .setAwardStatus(Const.MailRewardStatus.NOT_GET)
                        .addRewards(guildAwardCfg.getRewardItem())
                        .build());
                LogUtil.logTimberiumLeaguaGuildReward(playerData.id, guildId, GsConfig.getInstance().getServerId(), guildScore, playerData.score, getSeason(), getTermId(), guildAwardCfg.getId(), true);
            }
        }
        ConfigIterator<TiberiumSeasonPersonAwardCfg> personAwardCfgs = HawkConfigManager.getInstance().getConfigIterator(TiberiumSeasonPersonAwardCfg.class);
        for(TiberiumSeasonPersonAwardCfg personAwardCfg : personAwardCfgs){
            if(!playerData.playerRewarded.contains(personAwardCfg.getId()) && personAwardCfg.getScore() <= playerData.score){
                playerData.playerRewarded.add(personAwardCfg.getId());
                // 赛季联盟积分奖励
                SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                        .setPlayerId(playerData.id)
                        .setMailId(MailConst.MailId.TIBERIUM_SEASON_PERSON_SCORE_REWARD)
                        .addContents(personAwardCfg.getScore())
                        .setAwardStatus(Const.MailRewardStatus.NOT_GET)
                        .addRewards(personAwardCfg.getRewardItem())
                        .build());
                selfRewardLogUnitList.add(new TWSelfRewardLogUnit(playerData.id, guildId, GsConfig.getInstance().getServerId(), playerData.score, getSeason(), getTermId(), personAwardCfg.getId(), true));
            }
        }
    }
    
    /**
     * 获取奖励配置
     * @param isWin
     * @return
     */
    public TiberiumGuildAwardCfg getGuildAwardCfg(boolean isWin) {
        ConfigIterator<TiberiumGuildAwardCfg> its = HawkConfigManager.getInstance().getConfigIterator(TiberiumGuildAwardCfg.class);
        for(TiberiumGuildAwardCfg cfg : its){
            if(isWin == cfg.isWin()){
                return cfg;
            }
        }
        return null;
    }

    public TiberiumPersonAwardCfg getPersonAwardCfg(long selfScore, boolean isWin) {
        ConfigIterator<TiberiumPersonAwardCfg> its = HawkConfigManager.getInstance().getConfigIterator(TiberiumPersonAwardCfg.class);
        for(TiberiumPersonAwardCfg cfg : its){
            if(isWin != cfg.isWin()){
                continue;
            }
            HawkTuple2<Long, Long> rang = cfg.getScoreRange();
            if (selfScore >= rang.first && selfScore <= rang.second) {
                return cfg;
            }
        }
        return null;
    }

    
    /**
     * 最终排名计算
     */
    public void calFinal(){
    	String lock = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_CAL_FINAL, getSeason());
    	String serverId = GsConfig.getInstance().getServerId();
        boolean setNx = RedisProxy.getInstance().getRedisSession().setNx(lock, serverId);
        if(!setNx){
        	return;
        }
        Map<String, TBLYSeasonData> dataMap = loadSeasonTeamData();
        if(dataMap.isEmpty()){
            return;
        }
        Map<Integer, List<TBLYSeasonData>> teamTypeMap = new HashMap<>();
		for (TBLYSeasonData seasonData : dataMap.values()) {
			if(seasonData.finalRank > 0){
				return;
			}
			List<TBLYSeasonData> typeList = teamTypeMap.get(seasonData.teamType);
			if (Objects.isNull(typeList)) {
				typeList = new ArrayList<>();
				teamTypeMap.put(seasonData.teamType, typeList);
			}
			typeList.add(seasonData);
		}
		for(List<TBLYSeasonData> dataList : teamTypeMap.values()){
	        dataList.sort(new TLWTeamTotalRankComparator());
	        Map<String, String> dataStrMap = new HashMap<>();
	        int rank = 1;
	        for(TBLYSeasonData seasonData : dataList){
	            seasonData.finalRank = rank;
	            dataStrMap.put(seasonData.teamId, seasonData.serialize());
	            rank++;
	        }
	        String dataKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_DATA, getSeason());
	        RedisProxy.getInstance().getRedisSession().hmSet(dataKey, dataStrMap, 0);
		}
    }
    
    /**
     * 加载联赛所有小队
     * @return
     */
    public Map<String, TBLYSeasonData> loadSeasonTeamData(){
        String dataKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_DATA, getSeason());
        Map<String, String> stringMap = RedisProxy.getInstance().getRedisSession().hGetAll(dataKey);
        Map<String, TBLYSeasonData> dataMap = new ConcurrentHashMap<>();
        for(String str : stringMap.values()){
            if (HawkOSOperator.isEmptyString(str)) {
                continue;
            }
            try {
                TBLYSeasonData seasonData = TBLYSeasonData.unSerialize(str);
                if(seasonData == null){
                    continue;
                }
                dataMap.put(seasonData.teamId, seasonData);
            } catch (Exception e) {
                HawkException.catchException(e);
            }
        }
        return dataMap;
    }


    /**
     * 战场结束
     * @param msg
     */
    @MessageHandler
    private void onBattleFinish(TBLYBilingInformationMsg msg) {
        String roomId = msg.getRoomId();
        GuildTeamRoomData roomData = roomDataMap.get(roomId);
        if(roomData == null){
            return;
        }
        if(!HawkOSOperator.isEmptyString(roomData.winnerId)){
            return;
        }
        long guildScoreLimit = TiberiumConstCfg.getInstance().getSeasonGuildScoreLimit();
        long personScoreLimit = TiberiumConstCfg.getInstance().getSeasonPersonScoreLimit();
        roomData.roomState = 2;
        String winGuild = msg.getWinGuild();
        GuildTeamData teamDataA = battleTeamDataMap.get(roomData.campA).getTeamData();
        GuildTeamData teamDataB = battleTeamDataMap.get(roomData.campB).getTeamData();
        String guildA = teamDataA.guildId;
        long scoreA = msg.getGuildHonor(guildA);
        String guildB = teamDataB.guildId;
        long scoreB = msg.getGuildHonor(guildB);
        if (HawkOSOperator.isEmptyString(winGuild)) {
            if (scoreA != scoreB) {
                winGuild = scoreA > scoreB ? guildA : guildB;
            } else {
                winGuild = teamDataA.battlePoint > teamDataB.battlePoint ? guildA : guildB;
            }
        }
        //不能超上限
        scoreA = Math.min(scoreA, guildScoreLimit);
        scoreB = Math.min(scoreB, guildScoreLimit);
        roomData.roomState = 2;
        roomData.winnerId = winGuild.equals(guildA) ? teamDataA.id : teamDataB.id;
        roomData.scoreA = scoreA;
        roomData.scoreB = scoreB;
        RedisProxy.getInstance().getRedisSession().hSet(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM, getSeason(), getTermId()), roomData.id, roomData.serialize());

        Map<String, String> playerScoreMap = new HashMap<>();
        List<TBLYBilingInformationMsg.PlayerGameRecord> recods = msg.getPlayerRecords();
        // 记录玩家积分数据
        for (TBLYBilingInformationMsg.PlayerGameRecord recod : recods) {
            try {
            	//不能超上限
            	long playerScore = Math.min(recod.getHonor(), personScoreLimit);
                playerScoreMap.put(recod.getPlayerId(), String.valueOf(playerScore));
            } catch (Exception e) {
                HawkException.catchException(e);
            }
        }
        if(!playerScoreMap.isEmpty()){
        	RedisProxy.getInstance().getRedisSession().hmSet(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_PLAYER_SCORE, getSeason(), getTermId()), playerScoreMap, 0);
        }
        TLWWarResultLogUnit resultLogUnit = new TLWWarResultLogUnit(roomId, getSeason(), getTermId(), guildA, scoreA, guildB, scoreB, winGuild, msg.getFirstKillNian(), msg.getFirst5000Honor(), msg.getFirstControlHeXin(), roomData.battleType, roomData.matchType);
        TWLogUtil.logTimberiumLeaguaWarResult(resultLogUnit);
    }


    /**
     * 清理战时数据
     */
    public void cleanBattleData(){
        try {
            roomDataMap.clear();
            battleTeamDataMap.clear();
            playerIdToTeamId.clear();
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }
    
    /**
     * 清理赛季数据
     */
    public void cleanSeasonData(){
    	try {
            this.seasonDataMap.clear();
            this.seasonTeamDataMap.clear();
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }
    
    
    /**
     * 获得战斗房间数据
     * @param player 玩家
     * @return 战场数据
     */
    public GuildTeamRoomData getRoomData(Player player){
        GuildTeamData teamData = getTeam(player);
        if(Objects.isNull(teamData)){
            return null;
        }
        TBLYSrasonBattleTeamData batteTeam = battleTeamDataMap.get(teamData.id);
        if(Objects.isNull(batteTeam)){
        	return null;
        }
        return roomDataMap.get(batteTeam.getBattleRoomId());
    }
    
    
    /**
     * 进入战场检查
     * @param player
     * @return
     */
    public boolean checkEnter(Player player) {
        try {
            if(player.isCsPlayer()){
                player.sendError(HP.code.TIBERIUM_WAR_ENTER_INSTANCE_REQ_VALUE, Status.CrossServerError.CROSS_FIGHTERING_VALUE, 0);
                return false;
            }
            String guildId = player.getGuildId();
            if (HawkOSOperator.isEmptyString(guildId)) {
                player.sendError(HP.code.TIBERIUM_WAR_ENTER_INSTANCE_REQ_VALUE, Status.Error.GUILD_NO_JOIN,0);
                return false;
            }
            GuildTeamPlayerData playerData = TBLYGuildTeamManager.getInstance().getPlayerData(player.getId());
            if(playerData == null){
                player.sendError(HP.code.TIBERIUM_WAR_ENTER_INSTANCE_REQ_VALUE, Status.Error.TIBERIUM_NOT_IN_THIS_WAR_VALUE,0);
                return false;
            }
            if(!playerData.teamId.startsWith(guildId)){
                player.sendError(HP.code.TIBERIUM_WAR_ENTER_INSTANCE_REQ_VALUE, Status.Error.TIBERIUM_NOT_IN_THIS_WAR_VALUE,0);
                return false;
            }
            int teamEnterNum = getTeamEnterNum(playerData.teamId);
            if(teamEnterNum >= TiberiumConstCfg.getInstance().getWarMemberLimit()){
                player.sendError(HP.code.TIBERIUM_WAR_ENTER_INSTANCE_REQ_VALUE, Status.Error.TIBERIUM_PLAYER_OVER_LIMIT_VALUE,0);
                return false;
            }
            TBLYSrasonBattleTeamData batteTeam = battleTeamDataMap.get(playerData.teamId);
            if(Objects.isNull(batteTeam)){
            	player.sendError(HP.code.TIBERIUM_WAR_ENTER_INSTANCE_REQ_VALUE, Status.Error.TIBERIUM_NOT_IN_THIS_WAR_VALUE,0);
            	return false;
            }
            GuildTeamRoomData roomData = roomDataMap.get(batteTeam.getBattleRoomId());
            if(roomData == null){
                player.sendError(HP.code.TIBERIUM_WAR_ENTER_INSTANCE_REQ_VALUE, Status.Error.TIBERIUM_NOT_IN_THIS_WAR_VALUE,0);
                return false;
            }
            if(getState() != TBLYWarStateEnum.SEASON_WAR_OPEN){
                player.sendError(HP.code.TIBERIUM_WAR_ENTER_INSTANCE_REQ_VALUE, Status.Error.TIBERIUM_NOT_IN_THIS_WAR_VALUE, 0);
                return false;
            }
            long now = HawkTime.getMillisecond();
            long warStartTime = HawkTime.getMillisecond();
            TiberiumSeasonTimeCfg cfg = getCurrTimeCfg();
            if(cfg != null){
                warStartTime = cfg.getWarStartTimeValue();
            }
            long warEndTime = warStartTime + TiberiumConstCfg.getInstance().getWarOpenTime();
            if(now < warStartTime || now > warEndTime - TimeUnit.MINUTES.toMillis(5)){
                player.sendError(HP.code.TIBERIUM_WAR_ENTER_INSTANCE_REQ_VALUE, Status.Error.TIBERIUM_ROOM_NEAR_CLOSE_VALUE, 0);
                return false;
            }
            if(playerData.auth == GuildTeam.GuildTeamAuth.GT_CANDIDATE_VALUE && now < warStartTime + TimeUnit.MINUTES.toMillis(5)){
                player.sendError(HP.code.TIBERIUM_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_CANDIDATE_NOT_IN_TIME, 0);
                return false;
            }
            GuildTeamPlayerData redisData = TBLYGuildTeamManager.getInstance().load(player.getId());
            if(redisData.quitTIme > warStartTime && redisData.quitTIme < warEndTime){
                player.sendError(HP.code.TIBERIUM_WAR_ENTER_INSTANCE_REQ_VALUE, Status.Error.TIBERIUM_HAS_JOINED_WAR_VALUE, 0);
                return false;
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
        return true;
    }

    /**
     * 退出战场
     * @param player
     * @param isMidwayQuit
     * @return
     */
    public boolean quitRoom(Player player, boolean isMidwayQuit){
        GuildTeamPlayerData playerData = TBLYGuildTeamManager.getInstance().load(player.getId());
        playerData.quitTIme = HawkTime.getMillisecond();
        playerData.isMidwayQuit = isMidwayQuit;
        TBLYGuildTeamManager.getInstance().updatePlayer(playerData);
        LogUtil.logTimberiumQuitInfo(player.getId(), getTermId(), playerData.teamId, isMidwayQuit);
        String teamId = playerIdToTeamId.get(player.getId());
        updateTeamEnterNum(teamId, -1L);
        try {
            LogUtil.logTimberiumLeaguaQuitInfo(player.getId(), getSeason(), getTermId(), playerData.teamId, isMidwayQuit);
        }catch (Exception e){
            HawkException.catchException(e);
        }
        return true;
    }

    /**
     * 进入战场
     * @param player
     * @return
     */
    public boolean joinRoom(Player player) {
        GuildTeamData teamData = getTeam(player);
        if(Objects.isNull(teamData)){
        	return false;
        }
        TBLYSrasonBattleTeamData battleTeam = this.battleTeamDataMap.get(teamData.id);
		if(Objects.isNull(battleTeam)){
        	return false;
        }
        String roomId = battleTeam.getBattleRoomId();
        GuildTeamRoomData roomData = roomDataMap.get(roomId);
        if (!TBLYRoomManager.getInstance().hasGame(roomId)) {
            return false;
        }
        if (!TBLYRoomManager.getInstance().joinGame(roomData.id, player)) {
            return false;
        }
        updateTeamEnterNum(playerIdToTeamId.get(player.getId()), 1L);
        GuildTeamPlayerData playerData = TBLYGuildTeamManager.getInstance().load(player.getId());
        try {
            LogUtil.logTimberiumLeaguaEnterInfo(player.getId(), getSeason(), getTermId(), roomId, roomData.roomServerId, teamData.id, playerData.serverId,
                    player.getPower());
        } catch (Exception e) {
            HawkException.catchException(e);
        }
        return true;
    }


    /**
     * 添加小队进入战场个数
     * @param teamId
     * @param add
     */
    public void updateTeamEnterNum(String teamId, long add){
        try {
            if(HawkOSOperator.isEmptyString(teamId)){
                return;
            }
            String teamEnterKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_TEAM_ENTER, getSeason(), getTermId(), teamId);
            RedisProxy.getInstance().getRedisSession().increaseBy(teamEnterKey, add, TiberiumConst.TLW_EXPIRE_SECONDS);
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 获取小队进入战场人数
     * @param teamId
     * @return
     */
    public int getTeamEnterNum(String teamId){
        try {
            String teamEnterKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_TEAM_ENTER, getSeason(), getTermId(), teamId);
            String value = RedisProxy.getInstance().getRedisSession().getString(teamEnterKey);
            if (HawkOSOperator.isEmptyString(value)){
                return 0;
            }
            return Integer.parseInt(value);
        } catch (Exception e) {
            HawkException.catchException(e);
            return 100;
        }
    }

    
    /**
     * 小队进入战场是否超员
     * @param player
     * @return
     */
    boolean isEnterMax(Player player){
        try {
            GuildTeamData teamData = getGuildTeam(player);
            if(teamData == null){
                return false;
            }
            int teamEnterNum = getTeamEnterNum(teamData.id);
            return teamEnterNum >= TiberiumConstCfg.getInstance().getWarMemberLimit();
        } catch (Exception e) {
            HawkException.catchException(e);
            return true;
        }

    }

  

    /**
     * 是否在联赛中
     * @param teamId
     * @return
     */
    public boolean isInSeason(String teamId){
    	if(this.bigStateData.getState() == TBLYWarStateEnum.SEASON_BIG_NOT_OPEN){
    		return false;
    	}
    	if(this.stateData.getState() == TBLYWarStateEnum.SEASON_NOT_OPEN){
    		return false;
    	}
        TBLYSeasonData seasonData = seasonDataMap.get(teamId);
        if(seasonData == null){
            return false;
        }
        if(seasonData.groupType == TLWGroup.NOMAL_GROUP_VALUE || 
        		seasonData.groupType == TLWGroup.KICK_OUT_VALUE){
            return false;
        }
        return true;
    }
    public boolean isInSeason(Player player){
        GuildTeamData teamData = getTeam(player);
        if(teamData == null){
            return false;
        }
        return this.isInSeason(teamData.id);
    }
    public boolean isPlayerInSeason(String playerId){
        String teamId = playerIdToTeamId.get(playerId);
        if(HawkOSOperator.isEmptyString(teamId)){
            return false;
        }
        return this.isInSeason(teamId);
    }
    


    /**
     * 获取玩家小队所在赛区
     * @param player
     * @return
     */
    public TLWServer getTeamType(Player player){
        GuildTeamData teamData = getGuildTeam(player);
        if(teamData == null){
            return null;
        }
        TBLYSeasonData seasonData = seasonDataMap.get(teamData.id);
        if(seasonData == null){
            return null;
        }
        return TLWServer.valueOf(seasonData.teamType);
    }

    
    
    /**
     * 获取玩家小队所在小组ID
     * @param player
     * @return
     */
    public int getGroupId(Player player){
        GuildTeamData teamData = getGuildTeam(player);
        if(teamData == null){
            return 0;
        }
        TBLYSeasonData seasonData = seasonDataMap.get(teamData.id);
        if(seasonData == null){
            return 0;
        }
        return seasonData.groupId;
    }

    
    /**
     * 获取玩家小队淘汰赛标识
     * @param player
     * @return
     */
    public TLWGroup getGroup(Player player){
    	GuildTeamData teamData = getGuildTeam(player);
        if(teamData == null){
            return TLWGroup.NOMAL_GROUP;
        }
        TBLYSeasonData seasonData = seasonDataMap.get(teamData.id);
        if(seasonData != null){
            return TLWGroup.valueOf(seasonData.groupType);
        }
        return TLWGroup.NOMAL_GROUP;

    }
    
    
    /**
     * 获取小队信息
     * @param player
     * @return
     */
    public GuildTeamData getGuildTeam(Player player){
        if(!player.hasGuild()){
            return null;
        }
        String teamId = player.getGuildId()+":1";
        if(player.isCsPlayer()){
            return seasonTeamDataMap.get(teamId);
        }else {
            return TBLYGuildTeamManager.getInstance().getTeamData(teamId);
        }
    }

    
    /**
     * 获取小队信息
     * @param player
     * @return
     */
    public GuildTeamData getTeam(Player player){
        if(player.isCsPlayer()){
            String teamId = playerIdToTeamId.get(player.getId());
            if(HawkOSOperator.isEmptyString(teamId)){
                return null;
            }
            return seasonTeamDataMap.get(teamId);
        }else {
            GuildTeamPlayerData playerData = TBLYGuildTeamManager.getInstance().getPlayerData(player.getId());
            if(playerData == null || HawkOSOperator.isEmptyString(playerData.teamId)){
                return null;
            }
            return TBLYGuildTeamManager.getInstance().getTeamData(playerData.teamId);
        }

    }
    
    
    
    public TLWServer getTeamTypeByRank(int rank){
//    	if(rank >=1 && rank <= 256){
//    		return TLWServer.TLW_TOP_SERVER;
//    	}
//    	if(rank >=257 && rank <= 512){
//    		return TLWServer.TLW_DAWN_SERVER;
//    	}
//    	if(rank >=513 && rank <= 768){
//    		return TLWServer.TLW_HOT_SERVER;
//    	}
    	
    	int count = TiberiumConstCfg.getInstance().getGuildPickCnt();
    	int zoneCnt = count / 3;
    	
    	int start1 = 1;
    	int end1 = zoneCnt;
    	
    	int start2 = zoneCnt +1;
    	int end2 = zoneCnt * 2;
    	
    	int start3 = zoneCnt*2 +1;
    	int end3 = zoneCnt * 3;
    	
    	if(rank >=start1 && rank <= end1){
    		return TLWServer.TLW_TOP_SERVER;
    	}
    	if(rank >=start2 && rank <= end2){
    		return TLWServer.TLW_DAWN_SERVER;
    	}
    	if(rank >=start3 && rank <= end3){
    		return TLWServer.TLW_HOT_SERVER;
    	}
    	
    	return null;
    }

    
    /**
     * 更新联赛小队数据
     * @param seasonData
     */
    public void updateSeasonData(TBLYSeasonData seasonData){
        String dataKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_DATA, getSeason());
        RedisProxy.getInstance().getRedisSession().hSet(dataKey, seasonData.teamId, seasonData.serialize(), 0);
    }

   
    /**
     * 获取战时小队
     * @param teamId
     * @return
     */
    public GuildTeamData getBattleTeam(String teamId){
    	TBLYSrasonBattleTeamData battleTeam = battleTeamDataMap.get(teamId);
    	if(Objects.isNull(battleTeam)){
    		return null;
    	}
    	return battleTeam.getTeamData();
    }
    
    
    
    /**
     * 获取对战小队信息
     * @param player
     * @return
     */
    public TWGuildInfo.Builder getOppTeam(Player player){
        GuildTeamData teamData = getGuildTeam(player);
        if(teamData == null){
            return null;
        }
        TBLYSrasonBattleTeamData battleTeam = this.battleTeamDataMap.get(teamData.id);
		if(Objects.isNull(battleTeam)){
        	return null;
        }
        String roomId = battleTeam.getBattleRoomId();
        GuildTeamRoomData roomData = roomDataMap.get(roomId);
        if(roomData == null){
            return null;
        }
        String oppTeamId = teamData.id.equals(roomData.campA) ? roomData.campB : roomData.campA;
        TBLYSrasonBattleTeamData battleTeamData = battleTeamDataMap.get(oppTeamId);
        if(battleTeamData == null){
            return null;
        }
        GuildTeamData oppTeamData =  battleTeamData.getTeamData();
        if(oppTeamData == null){
            return null;
        }
        TWGuildInfo.Builder info = TWGuildInfo.newBuilder();
        info.setId(oppTeamData.id);
        info.setName(oppTeamData.name);
        info.setTag(oppTeamData.guildTag);
        info.setGuildFlag(oppTeamData.guildFlag);
        info.setServerId(oppTeamData.serverId);
        info.setBattlePoint(oppTeamData.battlePoint);
        info.setMemberCnt(oppTeamData.memberCnt);
        TiberiumSeasonTimeCfg cfg = getCurrTimeCfg();
        if(cfg != null){
            info.setWarStartTime(cfg.getWarStartTimeValue());
        }else {
            info.setWarStartTime(HawkTime.getMillisecond());
        }
        return info;
    }

    /**
     * 队伍管理限制
     * @param playerId
     * @param guildId
     */
    public boolean teamManageLimit(String playerId, String teamId){
    	TBLYWarStateEnum seasonStateEnum = this.getState();
    	if(seasonStateEnum == TBLYWarStateEnum.SEASON_NOT_OPEN){
        	return false;
     	}
    	TiberiumSeasonTimeCfg timeCfg = this.getCurrTimeCfg();
    	if(Objects.isNull(timeCfg)){
    		return false;
    	}
    	if(!timeCfg.winGroupFree()){
    		return false;
    	}
    	//目标队伍 已经被淘汰
    	boolean inseason = this.isInSeason(teamId);
    	if(!inseason){
    		return false;
    	}
    	String serverId = GsConfig.getInstance().getServerId();
    	String teamPlayerKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_MANAGE_TEAM_PLAYER_RECORD, getSeason(), timeCfg.getTermId() -1, serverId);
        String lastTeamId = RedisProxy.getInstance().getRedisSession().hGet(teamPlayerKey, playerId);
        if(!teamId.equals(lastTeamId)){
        	return false;
        }
        return true;
    }
    
    /**
     * 获取小组信息
     * @param player
     * @param groupId
     * @return
     */
    public TLWGetTeamGuildInfoResp.Builder getTeamGuildInfo(Player player, int groupId){
        if(getBigState() == TBLYWarStateEnum.SEASON_BIG_NOT_OPEN){
            player.sendError(HP.code.TIBERIUM_LEAGUA_WAR_GET_ZONE_GUILD_INFO_C_VALUE, Status.Error.TIBERIUM_LEAGUA_SEASON_NOT_OPEN, 0);
            return null;
        }
        if(seasonDataMap == null || seasonDataMap.isEmpty()){
            player.sendError(HP.code.TIBERIUM_LEAGUA_WAR_GET_ZONE_GUILD_INFO_C_VALUE, Status.Error.TIBERIUM_LEAGUA_SEASON_NOT_OPEN, 0);
            return null;
        }
        TLWGetTeamGuildInfoResp.Builder builder = TLWGetTeamGuildInfoResp.newBuilder();
        builder.setTeamId(groupId);
        TLWServer serverType = getTeamType(player);
        if(Objects.isNull(serverType)){
        	return builder;
        }
        String groupKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_GROUP, getSeason(), serverType.getNumber(), groupId);
        Set<String> groupTeamIds = RedisProxy.getInstance().getRedisSession().sMembers(groupKey);
        if(groupTeamIds.isEmpty()){
            player.sendError(HP.code.TIBERIUM_LEAGUA_WAR_GET_ZONE_GUILD_INFO_C_VALUE, Status.Error.TIBERIUM_LEAGUA_SEASON_NOT_OPEN, 0);
            return null;
        }
        
        
        List<TBLYSeasonData> seasonDataList = new ArrayList<>();
        for(String teamId : groupTeamIds){
            seasonDataList.add(seasonDataMap.get(teamId));
        }
        Map<String, GuildTeamData> groupTeamMap = TBLYGuildTeamManager.getInstance().loadTeamMap(groupTeamIds);
        seasonDataList.sort(new TLWTeamGroupComparator());
        for(int rank = 1; rank <= seasonDataList.size(); rank++) {
            TBLYSeasonData seasonData = seasonDataList.get(rank - 1);
            GuildTeamData teamData = groupTeamMap.get(seasonData.teamId);
            TLWGuildBaseInfo.Builder baseInfo = genBaseInfo(teamData);
            TLWTeamGuildInfo.Builder infoBuilder = TLWTeamGuildInfo.newBuilder();
            infoBuilder.setBaseInfo(baseInfo);
            infoBuilder.setBaseInfo(baseInfo);
            infoBuilder.setTeamId(groupId);
            infoBuilder.setBattlePoint(this.teamPowerMap.getOrDefault(teamData.id, 0l));
            infoBuilder.setWinCnt(seasonData.winCnt);
            infoBuilder.setLoseCnt(seasonData.loseCnt);
            infoBuilder.setScore(seasonData.score);
            infoBuilder.setGroup(TLWGroup.valueOf(seasonData.groupType));
            infoBuilder.setRank(rank);
            infoBuilder.setIsSeed(seasonData.isSeed);
            builder.addGuildInfo(infoBuilder);
        }
        return builder;
    }

    /**
     * 小队联盟基本信息
     * @param teamData
     * @return
     */
    public TLWGuildBaseInfo.Builder genBaseInfo(GuildTeamData teamData){
        TLWGuildBaseInfo.Builder builder = TLWGuildBaseInfo.newBuilder();
        builder.setId(teamData.guildId);
        builder.setName(teamData.guildName);
        builder.setTag(teamData.guildTag);
        builder.setGuildFlag(teamData.guildFlag);
        builder.setLeaderName(teamData.name);
        builder.setServerId(teamData.serverId);
        return builder;
    }
    
    /**
     * 获取排行榜
     * @param player
     * @param serverType
     * @return
     */
    public GetTLWGuildRankResp.Builder getGuildBattleRank(Player player, TiberiumWar.TLWServer serverType){
    	if(Objects.isNull(this.battleRank)){
            return GetTLWGuildRankResp.newBuilder();
        }
    	GetTLWGuildRankResp.Builder builder = this.battleRank.get(serverType.getNumber());
    	if(Objects.isNull(builder)){
    		return GetTLWGuildRankResp.newBuilder();
    	}
    	GetTLWGuildRankResp.Builder rankbuilder = builder.clone();
        GuildTeamData teamData = getGuildTeam(player);
        if(teamData != null){
        	TLWGuildRank.Builder selfRank = null;
            for(TLWGuildRank.Builder rank : builder.getRankInfoBuilderList()){
            	if(rank.getGuildInfo().getBaseInfo().getId().equals(teamData.guildId)){
            		selfRank = rank.clone();
            	}
            }
            if(selfRank != null){
            	builder.setSelfRank(selfRank);
            }
        }
        return rankbuilder;
    }

    /**
     * 获取排行榜
     * @param player
     * @param serverType
     * @return
     */
    public GetTLWGuildRankResp.Builder getGuildChooseRank(Player player) {
        if(this.joinRank == null){
            return GetTLWGuildRankResp.newBuilder();
        }
        GetTLWGuildRankResp.Builder builder = this.joinRank.clone();
        GuildTeamData teamData = getGuildTeam(player);
        if(teamData != null){
        	TLWGuildRank.Builder selfRank = null;
            for(TLWGuildRank.Builder rank : builder.getRankInfoBuilderList()){
            	if(rank.getGuildInfo().getBaseInfo().getId().equals(teamData.guildId)){
            		selfRank = rank.clone();
            	}
            }
            if(getBigState() == TBLYWarStateEnum.SEASON_BIG_SIGNUP || 
            		getBigState() == TBLYWarStateEnum.SEASON_BIG_GROUP_WAIT){
            	String rankKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_RANK, getSeason());
            	Long rankVal = RedisProxy.getInstance().getRedisSession().zrevrank(rankKey, teamData.id, 0);
                Double power = RedisProxy.getInstance().getRedisSession().zScore(rankKey, teamData.id, 0);
                if(power != null && rankVal != null ){
                    TLWGuildRank.Builder rankInfo = TLWGuildRank.newBuilder();
                    TLWGuildInfo.Builder guildBuilder = TLWGuildInfo.newBuilder();
                    guildBuilder.setBaseInfo(teamData.genBaseInfo());
                    guildBuilder.setBattlePoint(power.longValue());
                    rankInfo.setGuildInfo(guildBuilder);
                    rankInfo.setRank(rankVal.intValue() + 1);
                    selfRank = rankInfo;
                }
            }
            if(selfRank != null){
            	builder.setSelfRank(selfRank);
            }
        }
        return builder;
    }

    
    /**
     * 获取匹配数据
     * @param player
     * @param req
     * @return
     */
    public TLWGetMatchInfoResp.Builder getTLWMatchInfo(Player player,TLWGetMatchInfoReq req) {
        TLWGetMatchInfoResp.Builder builder = TLWGetMatchInfoResp.newBuilder();
        if (!player.hasGuild()) {
            return null;
        }
        if(seasonDataMap == null || seasonDataMap.isEmpty()){
            return null;
        }
        GuildTeamData teamData = getGuildTeam(player);
        if(teamData == null ){
            return null;
        }
        if(!seasonDataMap.containsKey(teamData.id)){
            return null;
        }
        int termId = req.getTermId();
        Map<String, String> roomStrMap = RedisProxy.getInstance().getRedisSession().hGetAll(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM, getSeason(), termId));
        //如果为空就返回，一般不会为空上面id已经校验一遍了，以防万一再检查一次
        if (roomStrMap == null || roomStrMap.isEmpty()) {
            return null;
        }
        List<GuildTeamRoomData> roomList = new ArrayList<>();
        List<GuildTeamRoomData> allRoomList = new ArrayList<>();
        for(String roomStr : roomStrMap.values()){
            GuildTeamRoomData tmp = GuildTeamRoomData.unSerialize(roomStr);
            if(tmp == null){
                continue;
            }

            if (termId >= TiberiumConstCfg.getInstance().getEliminationStartTermId()) {
                if(tmp.matchType == req.getServer().getNumber()
                        && tmp.groupType == req.getGroup().getNumber()){
                    roomList.add(tmp);
                }
                if(termId >= TiberiumConstCfg.getInstance().getEliminationFinalTermId()){
                    if(tmp.matchType == req.getServer().getNumber()){
                        allRoomList.add(tmp);
                    }
                }
            }else {
                if(tmp.matchType == req.getServer().getNumber()
                        && tmp.groupId == req.getTeamId()){
                    roomList.add(tmp);
                }
            }
        }
        TiberiumSeasonTimeCfg timeCfg = getTimeCfgBySeasonAndTermId(getSeason(), termId);

        builder.setTermId(termId);
        if(timeCfg == null){
            builder.setWarTime(HawkTime.getMillisecond() - TimeUnit.HOURS.toMillis(1));
        }else {
            builder.setWarTime(timeCfg.getWarStartTimeValue());
        }
        if (termId >= TiberiumConstCfg.getInstance().getEliminationStartTermId()) {
            builder.setGroup(req.getGroup());
        }else {
            builder.setTeamId(req.getTeamId());
            builder.setGroup(TLWGroup.TEAM_GROUP);
        }
        for (GuildTeamRoomData roomData : roomList) {
            GuildTeamData guildA = TBLYGuildTeamManager.getInstance().loadTeam(roomData.campA);
            GuildTeamData guildB = TBLYGuildTeamManager.getInstance().loadTeam(roomData.campB);
            TLWGetMatchInfo.Builder matchInfo = TLWGetMatchInfo.newBuilder();
            matchInfo.setRoomId(roomData.id);
            matchInfo.setGuildA(genBaseInfo(guildA));
            matchInfo.setGuildB(genBaseInfo(guildB));
            matchInfo.setBattleType(TLWBattle.valueOf(roomData.battleType));
            if (!HawkOSOperator.isEmptyString(roomData.winnerId)) {
                matchInfo.setWinnerId(roomData.winnerId.split(":")[0]);
            }
            builder.addMatchInfo(matchInfo);
        }
        for (GuildTeamRoomData roomData : allRoomList) {
            GuildTeamData guildA = TBLYGuildTeamManager.getInstance().loadTeam(roomData.campA);
            GuildTeamData guildB = TBLYGuildTeamManager.getInstance().loadTeam(roomData.campB);
            TLWFinalMatchMatchInfo.Builder fbuilder = TLWFinalMatchMatchInfo.newBuilder();
            fbuilder.setRoomId(roomData.id);
            fbuilder.setGuildA(genBaseInfo(guildA));
            fbuilder.setGuildB(genBaseInfo(guildB));
            fbuilder.setGuildC(genBaseInfo(guildA));
            fbuilder.setBattleType(TLWBattle.valueOf(roomData.battleType));
            if (!HawkOSOperator.isEmptyString(roomData.winnerId)) {
                fbuilder.setWinnerId(roomData.winnerId.split(":")[0]);
            }
            fbuilder.setGroup(TLWGroup.valueOf(roomData.groupType));
            builder.addFinalMatchInfo(fbuilder);
        }
        return builder;
    }


    public TLWGetScoreInfoResp.Builder getTLWScoreRewardInfo(Player player) {
        TLWGetScoreInfoResp.Builder builder = TLWGetScoreInfoResp.newBuilder();
        if(getBigState() == TBLYWarStateEnum.SEASON_BIG_NOT_OPEN){
            return builder;
        }
        String json = RedisProxy.getInstance().getRedisSession().hGet(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_PLAYER_DATA, getSeason()), player.getId());
        TBLYSeasonPlayerData playerData = TBLYSeasonPlayerData.unSerialize(json);
        if(playerData == null){
            return builder;
        }
        Set<Integer> selfRewarded = playerData.getPlayerRewarded();
        Set<Integer> guildRewarded = playerData.getGuildRewarded();
        TLWScoreInfo.Builder selfBuilder = TLWScoreInfo.newBuilder();
        selfBuilder.setScore(playerData.getScore());
        if(!selfRewarded.isEmpty()){
            selfBuilder.addAllRewardedId(selfRewarded);
        }
        builder.setSelfScore(selfBuilder);
        TLWScoreInfo.Builder guildBuilder = TLWScoreInfo.newBuilder();
        guildBuilder.setScore(playerData.getGuildscore());
        if (!guildRewarded.isEmpty()) {
            guildBuilder.addAllRewardedId(guildRewarded);
        }
        builder.setGuildScore(guildBuilder);
        return builder;
    }

    /**
     * 获取自我小队匹配信息
     * @param player
     * @return
     */
    public TLWSelfMatchList.Builder getSelfMatchInfo(Player player) {
        TLWSelfMatchList.Builder builder = TLWSelfMatchList.newBuilder();
        if (!player.hasGuild()) {
            return builder;
        }
        if(seasonDataMap == null || seasonDataMap.isEmpty()){
            return builder;
        }
        GuildTeamData teamData = getGuildTeam(player);
        if(teamData == null ){
            return builder;
        }
        if(!seasonDataMap.containsKey(teamData.id)){
            return builder;
        }
        int endTermId = Math.max(getTermId(), TiberiumConstCfg.getInstance().getEliminationStartTermId() -1);
        for(int termId=1;termId<= endTermId;termId++){
            TiberiumSeasonTimeCfg timeCfg = getTimeCfgBySeasonAndTermId(getSeason(), termId);
            Map<String, String> roomStrMap = RedisProxy.getInstance().getRedisSession().hGetAll(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM, getSeason(), termId));
            //如果为空就返回，一般不会为空上面id已经校验一遍了，以防万一再检查一次
            if (roomStrMap == null || roomStrMap.isEmpty()) {
                continue;
            }
            GuildTeamRoomData roomData = null;
            for(String roomStr : roomStrMap.values()){
                GuildTeamRoomData tmp = GuildTeamRoomData.unSerialize(roomStr);
                if(tmp.campA.equals(teamData.id) || tmp.campB.equals(teamData.id)){
                    roomData = tmp;
                    break;
                }
            }
            long warStartTime = HawkTime.getMillisecond();
            if(timeCfg != null){
                warStartTime = timeCfg.getWarStartTimeValue();
            }
            if(roomData != null){
                TLWRoomInfo.Builder roomInfo = TLWRoomInfo.newBuilder();
                roomInfo.setRoomId(roomData.id);
                roomInfo.setRoomServer(roomData.roomServerId);
                roomInfo.setTermId(termId);
                roomInfo.setWarStartTime(warStartTime);
                roomInfo.setWarFinishTime(warStartTime + TiberiumConstCfg.getInstance().getWarOpenTime());
                TLWGetMatchInfo.Builder matchInfo = TLWGetMatchInfo.newBuilder();
                matchInfo.setRoomId(roomData.id);
                //todo WHC TBLY
                matchInfo.setBattleType(TLWBattle.valueOf(TiberiumConst.TLWBattleType.TEAM_GROUP_BATTLE.getValue()));
                GuildTeamData guildA = TBLYGuildTeamManager.getInstance().loadTeam(roomData.campA);
                GuildTeamData guildB = TBLYGuildTeamManager.getInstance().loadTeam(roomData.campB);
                matchInfo.setGuildA(genBaseInfo(guildA));
                matchInfo.setGuildB(genBaseInfo(guildB));
                if(!HawkOSOperator.isEmptyString(roomData.winnerId)){
                    matchInfo.setWinnerId(roomData.winnerId.split(":")[0]);
                }
                roomInfo.setMatchInfo(matchInfo);
                builder.addRoomInfo(roomInfo);
            }else {
                if(seasonDataMap.get(teamData.id).kickOutTerm > termId){
                    TLWFreeRoom.Builder freeBuilder = TLWFreeRoom.newBuilder();
                    freeBuilder.setTermId(termId);
                    freeBuilder.setWarStartTime(warStartTime);
                    freeBuilder.setWarFinishTime(warStartTime + TiberiumConstCfg.getInstance().getWarOpenTime());
                    builder.addFreeInfos(freeBuilder);
                }
            }
        }
        return builder;
    }
    
    
    public void syncAllPLayer(){
        for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
            try {
                syncPageInfo(player);
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
    }

    public void syncPageInfo(Player player){    	
        TLWPageInfo.Builder builder = TLWPageInfo.newBuilder();
        builder.setStateInfo(fillStateInfo(stateData.getStateInfo(player)));
        builder.setBigStateInfo(bigStateData.getStateInfo(player));
        builder.setInSeasonWar(isInSeason(player));
        builder.setTeamId(getGroupId(player));
        builder.setGroup(getGroup(player));
        TLWServer ts = getTeamType(player);
        if(Objects.nonNull(ts)){
        	builder.setGuildType(ts);
        }
        //builder.setServerType(ts);
        builder.setIsEnterMax(isEnterMax(player));
        TWGuildInfo.Builder info = getOppTeam(player);
        if(info != null){
            builder.setOppGuild(info);
        }
        player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_LEAGUA_WAR_INFO_SYNC, builder));
    }

    public TLWStateInfo.Builder fillStateInfo(TLWStateInfo.Builder builder){
        int season = stateData.getSeason();
        int termId = stateData.getTermId();

        builder.setSeason(season);
        builder.setTermId(termId);
        HawkTuple2<Long, Long> timeInfo = AssembleDataManager.getInstance().getTiberiumSeasonTime(season);
        TiberiumSeasonTimeCfg fistCfg = getTimeCfgBySeasonAndTermId(season, 1);
        if(timeInfo != null){
            builder.setSeasonStartTime(timeInfo.first);
            builder.setSeasonEndTime(timeInfo.second);
        }else {
            builder.setSeasonStartTime(HawkTime.getMillisecond());
            builder.setSeasonEndTime(HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(60));
        }
        if(fistCfg != null){
            builder.setSignEndTime(fistCfg.getMatchStartTimeValue());
        }else {
            builder.setSignEndTime(HawkTime.getMillisecond() + + TimeUnit.DAYS.toMillis(10));
        }
        builder.setTeamNum(TiberiumConstCfg.getInstance().getTeamCnt());
        builder.setMaxNormalTermNum(TiberiumConstCfg.getInstance().getEliminationStartTermId() - 1);
        builder.setMaxTermNum(TiberiumConstCfg.getInstance().getEliminationFinalTermId());
        return builder;
    }

    public void dissmissFromSeason(String teamId){
        if(getBigState() != TBLYWarStateEnum.SEASON_BIG_SIGNUP){
            return;
        }
        String rankKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_RANK, getSeason());
        //String newRankKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_NEW_RANK, getSeason());
        RedisProxy.getInstance().getRedisSession().zRem(rankKey, 0,  teamId);
    }
    
    
    
    /*******************************************************************************************************************
     * 观战相关
     ******************************************************************************************************************/
    public TLWGetOBRoomInfo.Builder getTLWRoomInfo() {
        TLWGetOBRoomInfo.Builder builder = TLWGetOBRoomInfo.newBuilder();
        int season = getSeason();
        int termId = getTermId();
        builder.setSeason(season);
        builder.setTermId(termId);
        TiberiumSeasonTimeCfg cfg = getCurrTimeCfg();
        if(cfg!=null){
            builder.setWarStartTime(cfg.getWarStartTimeValue());
            builder.setWarFinishTime(cfg.getWarStartTimeValue() + TiberiumConstCfg.getInstance().getWarOpenTime());
        }
        if(getState() != TBLYWarStateEnum.SEASON_WAR_OPEN){
            return builder;
        }
        Map<String, String> roomMap = RedisProxy.getInstance().getRedisSession().hGetAll(String.format(TBLYWarResidKey.TBLY_WAR_SEASON_ROOM, getSeason(), termId));
        if (roomMap == null || roomMap.isEmpty()) {
            return builder;
        }
        Set<String> teamIds = new HashSet<>();
        for(String roomStr : roomMap.values()){
            GuildTeamRoomData roomData = GuildTeamRoomData.unSerialize(roomStr);
            if (roomData == null){
                continue;
            }
            teamIds.add(roomData.campA);
            teamIds.add(roomData.campB);
        }
        //加载相关小队数据
        Map<String, GuildTeamData> teamMap = TBLYGuildTeamManager.getInstance().loadTeamMap(teamIds);

        for(String roomStr : roomMap.values()) {
            GuildTeamRoomData roomData = GuildTeamRoomData.unSerialize(roomStr);
            if (roomData == null) {
                continue;
            }
            TLWGetMatchInfo.Builder matchBuilder = TLWGetMatchInfo.newBuilder();
            GuildTeamData guildA = teamMap.get(roomData.campA);
            GuildTeamData guildB = teamMap.get(roomData.campB);
            matchBuilder.setGuildA(guildA.genBaseInfo());
            matchBuilder.setGuildB(guildB.genBaseInfo());
            TLWRoomInfo.Builder obRoomInfo = TLWRoomInfo.newBuilder();
            obRoomInfo.setRoomId(roomData.id);
            obRoomInfo.setRoomServer(roomData.roomServerId);
            obRoomInfo.setMatchInfo(matchBuilder);
            builder.addRoomInfo(obRoomInfo);
        }
        return builder;
    }
    /**==============================================================================================================**/



    /*******************************************************************************************************************
     * GM工具相关方法
     ******************************************************************************************************************/
    /**
     * gm入口
     * @param map gm参数
     * @return 活动信息
     */
    public String gm(Map<String, String> map) {
        //只有测试环境可以使用
        if (!GsConfig.getInstance().isDebug()) {
            return "不是测试环境";
        }
        //要执行的gm指令
        String cmd = map.getOrDefault("cmd", "");
        switch (cmd) {
            case "info": {
                return printInfo();
            }
            case "next":{
                if(bigStateData.getState() == TBLYWarStateEnum.SEASON_BIG_GROUP_WAIT){
                    bigStateData.toNext();
                    return printInfo();
                }
                if(bigStateData.getState() == TBLYWarStateEnum.SEASON_BIG_END_SHOW){
                    bigStateData.toNext();
                    return printInfo();
                }
                if(bigStateData.getState() == TBLYWarStateEnum.SEASON_BIG_FINAL && stateData.getState() == TBLYWarStateEnum.SEASON_NOT_OPEN){
                    bigStateData.toNext();
                    return printInfo();
                }
                stateData.toNext();
                return printInfo();
            }
            case "bigNext":{
                if(bigStateData.getState() == TBLYWarStateEnum.SEASON_BIG_GROUP
                        ||bigStateData.getState() == TBLYWarStateEnum.SEASON_BIG_KICK_OUT
                        ||bigStateData.getState() == TBLYWarStateEnum.SEASON_BIG_FINAL ){
                    if(stateData.getState() != TBLYWarStateEnum.SEASON_WAR_OPEN){
                        while (stateData.getState() != TBLYWarStateEnum.SEASON_WAR_OPEN){
                            stateData.next();
                        }
                    }else {
                        stateData.next();
                    }
                }
                return printInfo();
            }
            case "signup":{
                Map<String, Double> powerMap = new HashMap<>();
                List<String> guildIds = GuildService.getInstance().getGuildIds();
                guildIds.forEach(id -> {
                    GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(id);
                    if(guildInfoObject!=null){
                        GuildTeamData teamData = TBLYGuildTeamManager.getInstance().makeSureTeamData(guildInfoObject, 1);
                        TBLYGuildTeamManager.getInstance().updateTeam(teamData);
                        powerMap.put(teamData.id, (double) 0);
                    }
                });
                String rankKey = String.format(TBLYWarResidKey.TBLY_WAR_SEASON_RANK, stateData.getSeason());
                RedisProxy.getInstance().getRedisSession().zAdd(rankKey, powerMap, 0);
                return printInfo();
            }
            case "updatePowerRank":{
                updatePowerRank();
                return printInfo();
            }
            case "onBigGroup":{
                onBigGroup();
                return printInfo();
            }
            case "onBigKickOut": {
                onBigKickOut();
                return printInfo();
            }
            case "onWarManager":{
                onWarManager();
                return printInfo();
            }
            case "onMatch":{
                onMatch();
                return printInfo();
            }
            case "loadSeasonData":{
            	loadSeasonTeamData();
                return printInfo();
            }
            case "loadBattleResult":{
                loadBattleResult();
                return printInfo();
            }
            case "testResult":{
                String roomStr = "{\"campA\":\"1pqj-3q4pzr-1:1\",\"campB\":\"1pqj-3qzxra-8k:1\",\"id\":\"1pqj-3q4pzr-1:1_1pqj-3qzxra-8k:1\",\"roomServerId\":\"80011\",\"roomState\":1,\"scoreA\":0,\"scoreB\":0,\"termId\":1,\"timeIndex\":0,\"winnerId\":\"\"}";
                GuildTeamRoomData roomData = GuildTeamRoomData.unSerialize(roomStr);
                updateRoomScore(roomData);
                return printInfo();
            }
            //战斗结束
            case "battleOver":{
                TBLYRoomManager.getInstance().findAllRoom().forEach(room -> room.setState(new TBLYGameOver(room)));
                return printInfo();
            }
            case "doTeamRank":{
                if(bigStateData.getState() == TBLYWarStateEnum.SEASON_BIG_SIGNUP){
                    updatePowerRank();
                }
                updateTeamPower();
                doRank();
                return printInfo();
            }
            case "randomSeason":{
                int season = HawkRand.randInt(10000);
                bigStateData.setSeason(season);
                stateData.setSeason(season);
                stateData.setTermId(0);
                return printInfo();
            }
            case "registRobot":{
                registRobot();
                return printInfo();
            }
            case "createGuild":{
                createGuild(500);
                return printInfo();
            }
            case "techUp":{
                techUp();
                return printInfo();
            }
            case "getSync":{
                String roomId = map.get("roomId");
                try {
                    String key = "TBLY_SYNC:" + roomId;
                    byte[] bytes = RedisProxy.getInstance().getRedisSession().getBytes(key.getBytes());
                    if (bytes != null) {
                        TBLY.PBTBLYGameInfoSync pb = TBLY.PBTBLYGameInfoSync.newBuilder().mergeFrom(bytes).build();
                    }
                } catch (Exception e) {
                    HawkException.catchException(e);
                }
                return printInfo();
            }
            case "allOne":{
                List<String> guildIds = GuildService.getInstance().getGuildIds();
                guildIds.forEach(id -> {
                    try {
                        GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(id);
                        if(guildInfoObject!=null){
                            GuildTeamData teamData = TBLYGuildTeamManager.getInstance().makeSureTeamData(guildInfoObject, 1);
                            TBLYGuildTeamManager.getInstance().updateTeam(teamData);
                            Player leader = GlobalData.getInstance().makesurePlayer(guildInfoObject.getLeaderId());
                            GuildTeam.GuildBattleMemberListReq.Builder listReq = GuildTeam.GuildBattleMemberListReq.newBuilder();
                            listReq.setType(GuildTeam.GuildTeamType.TBLY_WAR);
                            TBLYGuildTeamManager.getInstance().memberList(leader, listReq.build());
                            for(String memberId : GuildService.getInstance().getGuildMembers(id)){
                                Player player = GlobalData.getInstance().makesurePlayer(memberId);
                                if(player == null){
                                    continue;
                                }
                                GuildTeam.GuildBattleMemberManagerReq.Builder req = GuildTeam.GuildBattleMemberManagerReq.newBuilder();
                                req.setAuth(GuildTeam.GuildTeamAuth.GT_STARTER);
                                req.setType(GuildTeam.GuildTeamType.TBLY_WAR);
                                req.setPlayerId(memberId);
                                req.setTeamId(id+":1");
                                TBLYGuildTeamManager.getInstance().memberManager(player, req.build());
                            }
                        }
                    }catch (Exception e){
                        HawkException.catchException(e);
                    }
                });
                return printInfo();
            }
        }
        return "";
    }

    /**
     * 打印活动信息
     * @return 活动信息
     */
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
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYSEASONGM&cmd=info\">刷新</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYSEASONGM&cmd=next\">切阶段</a>          ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYSEASONGM&cmd=bigNext\">切大阶段</a>         ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYSEASONGM&cmd=allOne\">全员出战1战队</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYSEASONGM&cmd=registRobot\">创建机器人</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYSEASONGM&cmd=createGuild\">创建联盟</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYSEASONGM&cmd=techUp\">随机科技</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYSEASONGM&cmd=battleOver\">结束战斗</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYSEASONGM&cmd=doTeamRank\">刷新排行榜</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYSEASONGM&cmd=matchTestInfo\">匹配测试</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYSEASONGM&cmd=signupInfo\" target=\"_blank\">报名信息</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYSEASONGM&cmd=randomSeason\">随机赛季</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYSEASONGM&cmd=matchInfo\" target=\"_blank\">战场信息</a><br><br>";
            info += bigStateData.toString() + "<br>";
            info += stateData.toString() + "<br>";
        }catch (Exception e){
            HawkException.catchException(e);
        }
        return info;
    }

    public void registRobot() {
        int startId = 1;
        int count = 1000;
        for (int i = startId; i < startId + count; i++) {
            try {
                String puid = "robot_puid_" + (i + 1);
                // 构造登录协议对象
                Login.HPLogin.Builder builder = Login.HPLogin.newBuilder();
                builder.setCountry("cn");
                builder.setChannel("guest");
                builder.setLang("zh-CN");
                builder.setPlatform("android");
                builder.setVersion("1.0.0.0");
                builder.setPfToken("da870ef7cf996eb6");
                builder.setPhoneInfo("{\"deviceMode\":\"win32\",\"mobileNetISP\":\"0\",\"mobileNetType\":\"0\"}\n");
                builder.setPuid(puid);
                builder.setServerId(GsConfig.getInstance().getServerId());
                builder.setDeviceId(puid);

                HawkSession session = new HawkSession(null);
                session.setAppObject(new Player(null));
                if (GsApp.getInstance().doLoginProcess(session, HawkProtocol.valueOf(HP.code.LOGIN_C_VALUE, builder), HawkTime.getMillisecond())) {
                    AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(puid, GsConfig.getInstance().getServerId());
                    if (accountInfo != null) {
                        // 加载数据
                        accountInfo.setInBorn(false);
                        HawkXID xid = HawkXID.valueOf(GsConst.ObjType.PLAYER, accountInfo.getPlayerId());
                        Player player = (Player) GsApp.getInstance().queryObject(xid).getImpl();
                        PlayerData playerData = GlobalData.getInstance().getPlayerData(accountInfo.getPlayerId(), true);
                        player.updateData(playerData);

                        // 投递消息
                        HawkApp.getInstance().postMsg(player, PlayerAssembleMsg.valueOf(builder.build(), session));
                    }
                }
            } catch (Exception e) {
                HawkException.catchException(e);
            }
        }
    }

    public void createGuild(int count) {
        int createCount = 0;
        Set<String> playerIds = GlobalData.getInstance().getAllPlayerIds();
        for (String playerId : playerIds) {
            Player player = GlobalData.getInstance().makesurePlayer(playerId);
            if (player == null) {
                HawkLog.logPrintln("TLWGuildCreatHandler error, playerId:{}", playerId);
                continue;
            }
            if (player.hasGuild()) {
                continue;
            }
            if(createCount > count){
                break;
            }
            String guildName = GlobalData.getInstance().randomPlayerName().replaceFirst("指挥官", "");
            String tag = guildName.substring(0, 3);

            GuildCreateObj obj = new GuildCreateObj(guildName, tag, 10000, ConsumeItems.valueOf());
            obj.randomTag();
            player.rpcCall(GameConst.MsgId.GUILD_CREATE, GuildService.getInstance(), new GuildCreateRpcInvoker(player, obj));
            createCount ++;
        }
    }

    public void techUp() {
        Set<String> playerIds = GlobalData.getInstance().getAllPlayerIds();
        for (String playerId : playerIds) {
            AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
            if(accountInfo!=null){
                accountInfo.setInBorn(false);
            }
            Player player = GlobalData.getInstance().makesurePlayer(playerId);
            if (player == null) {
                continue;
            }
            ConfigIterator<TechnologyCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(TechnologyCfg.class);
            int lvlCnt = HawkRand.randInt(30);
            for (int i = 0; i <= lvlCnt; i++) {
                techLevelUp(player, cfgs.next());
            }
        }
    }

    private boolean techLevelUp(Player player, TechnologyCfg cfg) {
        int techId = cfg.getTechId();
        TechnologyEntity entity = player.getData().getTechEntityByTechId(techId);
        if (entity == null) {
            entity = player.getData().createTechnologyEntity(cfg);
        }

        player.getData().getPlayerEffect().addEffectTech(player, entity);
        entity.setLevel(cfg.getLevel());
        entity.setResearching(false);
        player.getPush().syncTechnologyLevelUpFinish(entity.getCfgId());
        player.refreshPowerElectric(LogConst.PowerChangeReason.TECH_LVUP);

        // 如果科技解锁技能,则推送科技技能信息
        if (cfg.getTechSkill() > 0) {
            player.getPush().syncTechSkillInfo();
        }

        return true;
    }
}
