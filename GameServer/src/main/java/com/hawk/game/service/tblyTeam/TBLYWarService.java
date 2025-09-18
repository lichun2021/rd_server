package com.hawk.game.service.tblyTeam;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.TWScoreEvent;
import com.hawk.game.GsConfig;
import com.hawk.game.config.TiberiumAppointMatchCfg;
import com.hawk.game.config.TiberiumConstCfg;
import com.hawk.game.config.TiberiumGuildAwardCfg;
import com.hawk.game.config.TiberiumPersonAwardCfg;
import com.hawk.game.config.TiberiumTimeCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.guildBackActivity.GuildBackDropInvoker;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.lianmengtaiboliya.TBLYExtraParam;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager;
import com.hawk.game.module.lianmengtaiboliya.msg.TBLYBilingInformationMsg;
import com.hawk.game.module.lianmengtaiboliya.msg.TBLYBilingInformationMsg.*;
import com.hawk.game.module.lianmengtaiboliya.roomstate.TBLYGameOver;
import com.hawk.game.module.schedule.ScheduleInfo;
import com.hawk.game.module.schedule.ScheduleService;
import com.hawk.game.player.Player;
import com.hawk.game.player.PowerElectric;
import com.hawk.game.protocol.*;
import com.hawk.game.protocol.Schedule.ScheduleType;
import com.hawk.game.protocol.TiberiumWar.*;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.ActivityService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.guildTeam.ipml.TBLYGuildTeamManager;
import com.hawk.game.service.guildTeam.model.GuildTeamData;
import com.hawk.game.service.guildTeam.model.GuildTeamGuildData;
import com.hawk.game.service.guildTeam.model.GuildTeamPlayerData;
import com.hawk.game.service.guildTeam.model.GuildTeamRoomData;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventTiberiumWar;
import com.hawk.game.service.tblyTeam.state.TBLYWarBattleStateData;
import com.hawk.game.service.tblyTeam.state.TBLYWarStateData;
import com.hawk.game.service.tblyTeam.state.TBLYWarStateEnum;
import com.hawk.game.service.tiberium.TWGuildData;
import com.hawk.game.service.tiberium.TiberiumConst;
import com.hawk.game.service.tiberium.logunit.TWGuildScoreLogUnit;
import com.hawk.game.service.tiberium.logunit.TWLogUtil;
import com.hawk.game.service.tiberium.logunit.TWPlayerScoreLogUnit;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst;
import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

/**
 * 泰伯普通小队模式
 */
public class TBLYWarService extends HawkAppObj {
    /*******************************************************************************************************************
     * 活动状态机
     ******************************************************************************************************************/
    /**
     * 主状态机
     */
    private TBLYWarStateData stateData;
    /**
     * 战斗状态机
     * key:场次时间索引
     * value:对应场次状态机
     */
    private Map<Integer, TBLYWarBattleStateData> battleStateDataMap = new ConcurrentHashMap<>();
    /**==============================================================================================================**/

    private static TBLYWarService instance = null;
    public static TBLYWarService getInstance() {
        return instance;
    }

    public TBLYWarService(HawkXID xid) {
        super(xid);
        instance = this;
    }

    /*******************************************************************************************************************
     * 战斗数据
     * 每期活动结束后删除
     ******************************************************************************************************************/
    /**
     * 小队报名信息
     * key:小队id
     * value:报名时间索引
     */
    private Map<String, Integer> signUpMap = new ConcurrentHashMap<>();
    /**
     * 房间数据
     * key:房间id
     * value:房间数据
     */
    private Map<String, GuildTeamRoomData> roomDataMap = new ConcurrentHashMap<>();
    /**
     * 通过时间段索引房间id
     * key:时间索引
     * value:房间id集合
     */
    private Map<Integer, Set<String>> timeIndexToRoomIds = new ConcurrentHashMap<>();
    /**
     * 战时小队数据
     * key:小队id
     * value:小队数据
     */
    private Map<String, GuildTeamData> battleTeamDataMap = new ConcurrentHashMap<>();
    /**
     * 通过小队id索引房间id
     * key:小队id
     * value:房间id
     */
    private Map<String, String> teamIdToRoomId = new ConcurrentHashMap<>();
    /**
     * 通过玩家id索引房间id
     * key:玩家id
     * value:房间id
     */
    private Map<String, String> playerIdToRoomId = new ConcurrentHashMap<>();
    /**
     * 通过玩家id索引小队id
     * key:玩家id
     * value:房间id
     */
    private Map<String, String> playerIdToTeamId = new ConcurrentHashMap<>();
    /**
     * 玩家角色权限
     * key:玩家id
     * value:玩家角色权限
     */
    private Map<String, Integer> playerIdToAuth = new ConcurrentHashMap<>();
    private Map<String, Set<String>> teamIdToBattlePlayerIds = new ConcurrentHashMap<>();
    /**==============================================================================================================**/

    /**
     * 初始化
     * @return
     */
    public boolean init() {
        try {
            stateData  = new TBLYWarStateData();
            stateData.load();
            if(stateData.getState() != TBLYWarStateEnum.PEACE){
                loadSignUp();
            }
            if(stateData.getState() == TBLYWarStateEnum.MATCH_END
                    ||stateData.getState() == TBLYWarStateEnum.BATTLE
                    ||stateData.getState() == TBLYWarStateEnum.FINISH){
                loadBattleRoom();
            }
            addTickable(new HawkPeriodTickable(1000) {
                @Override
                public void onPeriodTick() {
                    try {
                        stateData.tick();
                    } catch (Exception e) {
                        HawkException.catchException(e);
                    }
                    if(stateData.getState() == TBLYWarStateEnum.BATTLE){
                        for(TBLYWarBattleStateData battleStateData : battleStateDataMap.values()){
                            try {
                                battleStateData.tick();
                            }catch (Exception e){
                                HawkException.catchException(e);
                            }
                        }
                    }
                }
            });
        }catch (Exception e){
            HawkException.catchException(e);
            return false;
        }
        return true;
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
        TWPageInfo.Builder builder = TWPageInfo.newBuilder();
        builder.setStateInfo(genStateInfo(player));
        builder.setIsSignUp(isSignUp(player));
        builder.setIsGuildJoin(isTeamJoin(player));
        builder.setIsPlayerJoin(isPlayerJoin(player));
        builder.setIsEnterMax(isEnterMax(player));
        player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_INFO_SYNC, builder));
        syncTeamList(player);
    }

    public void syncTeamList(Player player){
        GuildTeam.GuildBattleTeamListReq.Builder req = GuildTeam.GuildBattleTeamListReq.newBuilder();
        req.setType(GuildTeam.GuildTeamType.TBLY_WAR);
        TBLYGuildTeamManager.getInstance().teamList(player, req.build());
    }

    public TWStateInfo.Builder genStateInfo(Player player) {
        TWStateInfo.Builder builder = getStateInfo(player);
        fillStateinfo(player, builder);
        return builder;
    }

    public TWStateInfo.Builder getStateInfo(Player player) {
        switch (stateData.getState()){
            case BATTLE:{
                String teamId = playerIdToTeamId.get(player.getId());
                if(!HawkOSOperator.isEmptyString(teamId)){
                    GuildTeamData teamData = battleTeamDataMap.get(teamId);
                    if(teamData != null){
                        TBLYWarBattleStateData battleStateData = battleStateDataMap.get(teamData.timeIndex);
                        if(battleStateData != null){
                            return battleStateData.getStateInfo(player);
                        }
                    }
                }
            }
            break;
        }
        return stateData.getStateInfo(player);
    }

    public void fillStateinfo(Player player, TWStateInfo.Builder builder){
        builder.setStage(stateData.getTermId());
    }

    public TiberiumTimeCfg getCurTimeCfg(){
        long now = HawkTime.getMillisecond();
        ConfigIterator<TiberiumTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(TiberiumTimeCfg.class);
        for (TiberiumTimeCfg cfg : its) {
            if(now >= cfg.getSignStartTimeValue() && now <= cfg.getWarEndTimeValue()){
                return cfg;
            }
        }
        return null;
    }

    /**
     * 获得当前期数
     * @return 当前期数
     */
    public int getTermId(){
        return stateData.getTermId();
    }

    public TBLYWarStateEnum getState(){
        return stateData.getState();
    }

    public TiberiumTimeCfg getTimeCfg(){
        int termId = stateData.getTermId();
        if(termId <= 0){
            return null;
        }
        return HawkConfigManager.getInstance().getConfigByKey(TiberiumTimeCfg.class, termId);
    }

    /**
     * 获取选项
     * @return
     */
    public List<WarTimeChoose> getChooses() {
        List<WarTimeChoose> chooseList = new ArrayList<>();
        TiberiumTimeCfg cfg = getTimeCfg();
        if (cfg == null) {
            return chooseList;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(cfg.getWarStartTimeValue());
        List<HawkTuple2<Integer, Integer>> timeList = TiberiumConstCfg.getInstance().getTimeList();
        for (int i = 0; i < timeList.size(); i++) {
            HawkTuple2<Integer, Integer> timeTuple = timeList.get(i);
            calendar.set(Calendar.HOUR_OF_DAY, timeTuple.first);
            calendar.set(Calendar.MINUTE, timeTuple.second);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            WarTimeChoose.Builder builder = WarTimeChoose.newBuilder();
            builder.setIndex(i);
            builder.setTime(calendar.getTimeInMillis());
            chooseList.add(builder.build());
        }
        return chooseList;
    }

    public int getOpenDayW(){
        try {
            TiberiumConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(TiberiumConstCfg.class);
            return cfg.serverMatchOpenDayWeight();
        } catch (Exception e) {
            HawkException.catchException(e);
        }
        return 0;
    }

    /*******************************************************************************************************************
     * 状态机相关方法
     ******************************************************************************************************************/
    /**
     * 活动状态机进入下一状态
     * @return 执行结果
     */
    public boolean toNext(){
        stateData.toNext();
        return true;
    }

    /**
     * 战斗状态机进入下一状态
     * @param timeIndex 时间索引
     * @return 执行结果
     */
    public boolean toBattleNext(int timeIndex){
        battleStateDataMap.get(timeIndex).toNext();
        return true;
    }

    /**
     * 开始新一期的活动
     * @param termId 当前期数
     */
    public void onOpen(int termId){
        //设置当前期数
        stateData.setTermId(termId);
        //清理数据
        clean();
        //进入下一状态
        stateData.toNext();
    }

    public void onSignup(){

    }

    /**
     * 匹配准备阶段，等待匹配数据上传
     * @return 执行结果
     */
    public void onMatchWait(){
        //上传本服报名数据
        updateSignUpInfo();
    }

    /**
     * 活动状态机进入下一状态
     * @return 执行结果
     */
    public void onMatch() {
        int termId = getTermId();
        HawkLog.logPrintln("TBLYWarService onMatch start, termId:{}", termId);
        //抢锁
        String serverId = GsConfig.getInstance().getServerId();
        String matchKey = String.format(TBLYWarResidKey.TBLY_WAR_MATCH, termId);
        boolean getLock = RedisProxy.getInstance().getRedisSession().setNx(matchKey, serverId);
        if (!getLock) {
            HawkLog.logPrintln("TBLYWarService onMatch get lock fail, termId:{}", termId);
            return;
        }
        Map<String, Set<String>> serverIdToRoomIdMap = new HashMap<>();
        Map<String, String> roomStrMap = new HashMap<>();
        List<HawkTuple2<Integer, Integer>> timeList = TiberiumConstCfg.getInstance().getTimeList();
        for(int i = 1; i <= timeList.size(); i++){
            String signUpTimeKey = String.format(TBLYWarResidKey.TBLY_WAR_SIGNUP_TIME, termId, i);
            Set<String> teamIds = RedisProxy.getInstance().getRedisSession().sMembers(signUpTimeKey);
            if(teamIds == null || teamIds.isEmpty()){
                continue;
            }
            List<GuildTeamData> teamDayList = TBLYGuildTeamManager.getInstance().loadTeams(teamIds);
            
            /************* 暗箱操作匹配 ***************/
            Map<String, GuildTeamData> dataMap = new HashMap<>();
            for (GuildTeamData teamData : teamDayList){
            	dataMap.put(teamData.id, teamData);
            }
    		TiberiumAppointMatchCfg appointCfg = HawkConfigManager.getInstance().getConfigByKey(TiberiumAppointMatchCfg.class, termId);
    		if (appointCfg != null) {
    			List<HawkTuple2<String, String>> appointList = appointCfg.getAppointList();
    			for (HawkTuple2<String, String> tuple : appointList) {
    				GuildTeamData teamData1 = dataMap.get(tuple.first);
    				GuildTeamData teamData2 = dataMap.get(tuple.second);
    				// 内定联盟未在同一个时间区间
    				if (teamData1 == null || teamData2 == null) {
    					continue;
    				}
    				teamDayList.remove(teamData1);
    				teamDayList.remove(teamData2);
                    teamData1.oppTeamId = teamData2.id;
                    teamData2.oppTeamId = teamData1.id;
                    GuildTeamRoomData roomData = new GuildTeamRoomData(termId, i, teamData1, teamData2);
                    roomData.roomServerId = teamData1.serverId.compareTo(teamData2.serverId) < 0 ? teamData1.serverId : teamData2.serverId;
                    TBLYGuildTeamManager.getInstance().updateTeam(teamData1);
                    TBLYGuildTeamManager.getInstance().updateTeam(teamData2);
                    roomStrMap.put(roomData.id, roomData.serialize());
                    updateRoomIdToServer(serverIdToRoomIdMap, teamData1.serverId, roomData.id);
                    updateRoomIdToServer(serverIdToRoomIdMap, teamData2.serverId, roomData.id);
                    updateRoomIdToServer(serverIdToRoomIdMap, roomData.roomServerId, roomData.id);
                    LogUtil.logTimberiumMatchInfo(roomData.id, roomData.roomServerId, termId, roomData.timeIndex, teamData1.id, teamData1.matchPower, teamData1.serverId, teamData2.id,
                            teamData2.matchPower,teamData2.serverId);

                    HawkLog.logPrintln("TiberiumWarService do match, roomId: {}, roomServer: {}, termId: {}, timeIndex: {}, guildA: {}, guildStrengthA:{}, serverA: {}, guildB: {},  guildStrengthB:{}, serverB: {} ",
                            roomData.id, roomData.roomServerId, termId, roomData.timeIndex, teamData1.id, teamData1.matchPower, teamData1.serverId, teamData2.id,
                            teamData2.matchPower,teamData2.serverId);
    			}
    		}
    		/************* 暗箱操作匹配 ***************/
            
            
    		Map<Integer, List<GuildTeamData>> teamDayMap = new HashMap<>();
            for (GuildTeamData teamData : teamDayList){
                if(!teamDayMap.containsKey(teamData.openDayW)){
                    teamDayMap.put(teamData.openDayW, new ArrayList<>());
                }
                teamDayMap.get(teamData.openDayW).add(teamData);
            }
            
            List<Integer> dayList = new ArrayList<>(teamDayMap.keySet());
            List<GuildTeamData> noMatchList = new ArrayList<>();
            dayList.sort((o1, o2) -> o2 - o1);
            for(int day : dayList){
                List<GuildTeamData> teamList = new ArrayList<>();
                teamList.addAll(noMatchList);
                teamList.addAll(teamDayMap.get(day));
                noMatchList = new ArrayList<>();
                teamList.sort((o1, o2) -> {
                    if (o1.openDayW != o2.openDayW) {
                        return o1.openDayW > o2.openDayW ? -1 : 1;
                    }
                    if (o1.matchPower != o2.matchPower) {
                        return o1.matchPower > o2.matchPower ? -1 : 1;
                    }
                    return 0;
                });
                while (teamList.size() > 1){
                    HawkTuple2<GuildTeamData, GuildTeamData> result = matchTeam(teamList);
                    if(result == null){
                        break;
                    }
                    if(result.first != null && result.second == null){
                        noMatchList.add(result.first);
                    }
                    if(result.first != null){
                        teamList.remove(result.first);
                    }
                    if(result.second != null){
                        teamList.remove(result.second);
                    }
                    GuildTeamData teamData1 = result.first;
                    GuildTeamData teamData2 = result.second;
                    if(teamData1 != null && teamData2 != null){
                        teamData1.oppTeamId = teamData2.id;
                        teamData2.oppTeamId = teamData1.id;
                        GuildTeamRoomData roomData = new GuildTeamRoomData(termId, i, teamData1, teamData2);
                        roomData.roomServerId = teamData1.serverId.compareTo(teamData2.serverId) < 0 ? teamData1.serverId : teamData2.serverId;
                        TBLYGuildTeamManager.getInstance().updateTeam(teamData1);
                        TBLYGuildTeamManager.getInstance().updateTeam(teamData2);
                        roomStrMap.put(roomData.id, roomData.serialize());
                        updateRoomIdToServer(serverIdToRoomIdMap, teamData1.serverId, roomData.id);
                        updateRoomIdToServer(serverIdToRoomIdMap, teamData2.serverId, roomData.id);
                        updateRoomIdToServer(serverIdToRoomIdMap, roomData.roomServerId, roomData.id);
                        LogUtil.logTimberiumMatchInfo(roomData.id, roomData.roomServerId, termId, roomData.timeIndex, teamData1.id, teamData1.matchPower, teamData1.serverId, teamData2.id,
                                teamData2.matchPower,teamData2.serverId);


                        HawkLog.logPrintln("TiberiumWarService do match, roomId: {}, roomServer: {}, termId: {}, timeIndex: {}, guildA: {}, guildStrengthA:{}, serverA: {}, guildB: {},  guildStrengthB:{}, serverB: {} ",
                                roomData.id, roomData.roomServerId, termId, roomData.timeIndex, teamData1.id, teamData1.matchPower, teamData1.serverId, teamData2.id,
                                teamData2.matchPower,teamData2.serverId);
                    }
                }
                if(!teamList.isEmpty()){
                    noMatchList.addAll(teamList);
                }
            }
        }
        if(!roomStrMap.isEmpty()){
            RedisProxy.getInstance().getRedisSession().hmSet(String.format(TBLYWarResidKey.TBLY_WAR_ROOM, termId), roomStrMap, 0);
        }
        for(String toServerId : serverIdToRoomIdMap.keySet()){
            Set<String> roomIds = serverIdToRoomIdMap.get(toServerId);
            RedisProxy.getInstance().getRedisSession().sAdd(String.format(TBLYWarResidKey.TBLY_WAR_ROOM_SERVER, termId, toServerId), 0, roomIds.toArray(new String[roomIds.size()]));
        }
    }

    /**
     * 匹配结束阶段，加载匹配结果
     */
    public void onMatchEnd(){
        loadBattleRoom();
        sendMatchResult();
    }

    /**
     * 进入战斗阶段，加载战斗状态机
     */
    public void onBattle(){
        //初始化战斗状态机
        List<HawkTuple2<Integer, Integer>> timeList = TiberiumConstCfg.getInstance().getTimeList();
        for(int i = 1; i <= timeList.size(); i++){
            battleStateDataMap.put(i, new TBLYWarBattleStateData(i));
        }
    }

    /**
     * 战斗开始阶段，创建战场
     */
    public void onBattleOpen(int timeIndex){
        Set<String> roomIds = timeIndexToRoomIds.get(timeIndex);
        if(roomIds == null || roomIds.isEmpty()){
            return;
        }
        long warStartTime = HawkTime.getMillisecond();
        long warEndTime = warStartTime + TiberiumConstCfg.getInstance().getWarOpenTime();
        List<WarTimeChoose> timeList = TBLYWarService.getInstance().getChooses();
        if(timeList.size() >= timeIndex){
            WarTimeChoose battleTime = getChooses().get(timeIndex-1);
            warStartTime = battleTime.getTime();
            warEndTime = warStartTime + TiberiumConstCfg.getInstance().getWarOpenTime();
        }
        Map<String, String> roomStrMap = new HashMap<>();
        for(String roomId : roomIds){
            GuildTeamRoomData roomData = roomDataMap.get(roomId);
            if(roomData == null || roomData.timeIndex != timeIndex || !roomData.roomServerId.equals(GsConfig.getInstance().getServerId())){
                continue;
            }
            GuildTeamData teamDataA = battleTeamDataMap.get(roomData.campA);
            GuildTeamData teamDataB = battleTeamDataMap.get(roomData.campB);
            TBLYExtraParam extParm = new TBLYExtraParam();
            extParm.setCampAGuild(teamDataA.guildId);
            extParm.setCampAGuildName(teamDataA.guildName);
            extParm.setCampAGuildTag(teamDataA.guildTag);
            extParm.setCampAServerId(teamDataA.serverId);
            extParm.setCampAguildFlag(teamDataA.guildFlag);
            extParm.setCampAPlayers(TBLYGuildTeamManager.getInstance().getAllTWPlayerData(teamIdToBattlePlayerIds.get(teamDataA.id)));
            /**-----------------------------------------------------------------------------------------------------------------------------------------------*/
            /**-----------------------------------------------------------------------------------------------------------------------------------------------*/
            extParm.setCampBGuild(teamDataB.guildId);
            extParm.setCampBGuildName(teamDataB.guildName);
            extParm.setCampBGuildTag(teamDataB.guildTag);
            extParm.setCampBServerId(teamDataB.serverId);
            extParm.setCampBguildFlag(teamDataB.guildFlag);
            extParm.setCampAPlayers(TBLYGuildTeamManager.getInstance().getAllTWPlayerData(teamIdToBattlePlayerIds.get(teamDataB.id)));
            extParm.setLeaguaWar(false);
            TBLYRoomManager.getInstance().creatNewBattle(warStartTime, warEndTime, roomData.id, extParm);
            roomData.roomState = 1;
            roomStrMap.put(roomData.id, roomData.serialize());
        }
        RedisProxy.getInstance().getRedisSession().hmSet(String.format(TBLYWarResidKey.TBLY_WAR_ROOM, getTermId()), roomStrMap, 0);
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
        int termId = getTermId();
        roomData.roomState = 2;
        String winGuild = msg.getWinGuild();
        GuildTeamData teamDataA = battleTeamDataMap.get(roomData.campA);
        GuildTeamData teamDataB = battleTeamDataMap.get(roomData.campB);
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
        roomData.roomState = 2;
        roomData.winnerId = winGuild.equals(guildA) ? teamDataA.id : teamDataB.id;
        roomData.scoreA = scoreA;
        roomData.scoreB = scoreB;
        teamDataA.score = scoreA;
        teamDataB.score = scoreB;
        RedisProxy.getInstance().getRedisSession().hSet(String.format(TBLYWarResidKey.TBLY_WAR_ROOM, getTermId()), roomData.id, roomData.serialize());

        Map<String, String> playerScoreMap = new HashMap<>();
        List<TBLYBilingInformationMsg.PlayerGameRecord> recods = msg.getPlayerRecords();
        List<TWPlayerScoreLogUnit> playerScoreLogList = new ArrayList<>();
        // 记录玩家积分数据
        for (PlayerGameRecord recod : recods) {
            try {
                playerScoreMap.put(recod.getPlayerId(), String.valueOf(recod.getHonor()));
                playerScoreLogList.add(new TWPlayerScoreLogUnit(recod.getPlayerId(), termId, playerIdToTeamId.getOrDefault(recod.getPlayerId(), ""), recod.getHonor()));
            } catch (Exception e) {
                HawkException.catchException(e);
            }
        }
        RedisProxy.getInstance().getRedisSession().hmSet(String.format(TBLYWarResidKey.TBLY_WAR_PLAYER_SCORE, getTermId()), playerScoreMap, 0);

        // Tlog 延时记录玩家个人积分,避免拥堵
        TWLogUtil.logTimberiumPlayerScoreInfo(playerScoreLogList);

        TWGuildScoreLogUnit gScoreLogUnitA = new TWGuildScoreLogUnit(teamDataA.id, teamDataA.name, termId, teamDataA.serverId, roomId, roomData.roomServerId, teamDataA.score,
                teamDataA.memberCnt, teamDataA.battlePoint, winGuild.equals(guildA));
        TWGuildScoreLogUnit gScoreLogUnitB = new TWGuildScoreLogUnit(teamDataB.id, teamDataB.name, termId, teamDataB.serverId, roomId, roomData.roomServerId, teamDataB.score,
                teamDataB.memberCnt, teamDataB.battlePoint, winGuild.equals(guildB));
        TWLogUtil.logTimberiumGuildScoreInfo(gScoreLogUnitA);
        TWLogUtil.logTimberiumGuildScoreInfo(gScoreLogUnitB);
        try {
            // 记录战斗记录
            TWBattleLog.Builder builder = TWBattleLog.newBuilder();
            builder.setTermId(termId);
            builder.setRoomId(roomId);
            builder.setWinGuild(winGuild);
            long warStartTime = HawkTime.getMillisecond();
            List<WarTimeChoose> timeList = TBLYWarService.getInstance().getChooses();
            if(timeList.size() >= roomData.timeIndex){
                WarTimeChoose battleTime = getChooses().get(roomData.timeIndex-1);
                warStartTime = battleTime.getTime();
            }
            builder.setTime(warStartTime);
            TWGuildInfo.Builder guildInfoA = teamDataA.toTWGuildInfo();
            guildInfoA.setIsWin(winGuild.equals(guildA));
            builder.addGuildInfo(guildInfoA);
            TWGuildInfo.Builder guildInfoB = teamDataB.toTWGuildInfo();
            guildInfoB.setIsWin(winGuild.equals(guildB));
            builder.addGuildInfo(guildInfoB);
            TWBattleLog battleLog = builder.build();
            RedisProxy.getInstance().addTWBattleLog(battleLog, teamDataA.id);
            RedisProxy.getInstance().addTWBattleLog(battleLog, teamDataB.id);
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * 战斗结束结束阶段，加载战场结果，并且进行发奖
     */
    public void onBattleEnd(int timeIndex){
        loadBattleRoomResult(timeIndex);
        sendAward(timeIndex);
    }

    /**
     * 活动结束，清理状态和数据
     */
    public void onEnd(){
        clean();
    }

    /**
     * 加载战场结果
     * @param timeIndex 时间索引
     */
    public void loadBattleRoomResult(int timeIndex){
        try {
            //获得期数
            int termId = getTermId();
            //根据时间索引获得房间id
            Set<String> roomIds = timeIndexToRoomIds.get(timeIndex);
            //如果为空直接返回
            if(roomIds == null || roomIds.isEmpty()){
                HawkLog.logPrintln("TBLYWarService loadBattleRoomResult roomIds is empty, timeIndex:{}",timeIndex);
                return;
            }
            HawkLog.logPrintln("TBLYWarService loadBattleRoomResult start, timeIndex:{}",timeIndex);
            //加载Redis房间数据
            List<String> roomList = RedisProxy.getInstance().getRedisSession().hmGet(String.format(TBLYWarResidKey.TBLY_WAR_ROOM, termId), roomIds.toArray(new String[roomIds.size()]));
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
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    public void updateRoomScore(GuildTeamRoomData roomData){
        if(roomData == null){
            return;
        }
        updateTeamScore(roomData.campA, roomData.scoreA);
        updateTeamScore(roomData.campB, roomData.scoreB);
    }

    public void updateTeamScore(String teamId, long score){
        if(HawkOSOperator.isEmptyString(teamId)){
            return;
        }
        GuildTeamData teamData = TBLYGuildTeamManager.getInstance().getTeamData(teamId);
        if(teamData != null){
            teamData.score = score;
            TBLYGuildTeamManager.getInstance().updateTeam(teamData);
        }
        GuildTeamData battleTeamData = battleTeamDataMap.get(teamId);
        if(battleTeamData != null){
            battleTeamData.score = score;
            TBLYGuildTeamManager.getInstance().updateTeam(battleTeamData);
        }
    }


    public void sendMatchResult(){
        try {
            for(GuildTeamRoomData roomData : roomDataMap.values()){
                if(roomData == null){
                    continue;
                }
                GuildTeamData teamDataA = TBLYGuildTeamManager.getInstance().getTeamData(roomData.campA);
                if(teamDataA != null){
                    sendMatchResult(roomData.campA, roomData.timeIndex);
                }
                GuildTeamData teamDataB = TBLYGuildTeamManager.getInstance().getTeamData(roomData.campB);
                if(teamDataB != null){
                    sendMatchResult(roomData.campB, roomData.timeIndex);
                }
            }
            for(String teamId : signUpMap.keySet()){
                String roomId = teamIdToRoomId.get(teamId);
                if (!HawkOSOperator.isEmptyString(roomId)) {
                    HawkLog.logPrintln("TBLYWarService sendAward roomId matched teamId:{}", teamId);
                    continue;
                }
                Set<String> playerIds = TBLYGuildTeamManager.getInstance().getTeamPlayerIds(teamId);
                if(playerIds == null || playerIds.isEmpty()){
                	HawkLog.logPrintln("TBLYWarService-sendMatchResult playerIds null teamId:{}", teamId);
                    continue;
                }
                //发奖锁
                String teamAwardKey = String.format(TBLYWarResidKey.TBLY_WAR_TEAM_AWARD, getTermId());
                boolean teamGetLock = RedisProxy.getInstance().getRedisSession().hSetNx(teamAwardKey, teamId, String.valueOf(HawkTime.getMillisecond())) > 0;
                if (!teamGetLock) {
                	HawkLog.logPrintln("TBLYWarService-sendMatchResult teamGetLock has teamId:{}", teamId);
                    continue;
                }
                //报名时段
                int signTime = signUpMap.get(teamId);
                //是否写入匹配池
                String signUpTimeKey = String.format(TBLYWarResidKey.TBLY_WAR_SIGNUP_TIME, getTermId(), signTime);
                boolean inMatchPool = RedisProxy.getInstance().getRedisSession().sIsmember(signUpTimeKey, teamId);
                //如果参与了匹配则是  没有匹配上
                if(inMatchPool){
                	HawkLog.logPrintln("TBLYWarService-sendMatchResult nomatch teamId:{}", teamId);
                	HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
                        @Override
                        public Object run() {
                            TiberiumPersonAwardCfg selfCfg = getPersonAwardCfg(1200, true);
                            TiberiumGuildAwardCfg rewardCfg = getGuildAwardCfg(true);
                            List<ItemInfo> rewardList = new ArrayList<>();
                            if(selfCfg != null) {
                                rewardList.addAll(selfCfg.getRewardItem());
                            }
                            if(rewardCfg != null){
                                rewardList.addAll(rewardCfg.getRewardItem());
                            }
                            // 匹配失败邮件
                            for (String playerId : playerIds) {
                                SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                                        .setPlayerId(playerId)
                                        .setMailId(MailConst.MailId.TBLY_GUILD_MATCH_FAILED)
                                        .setAwardStatus(Const.MailRewardStatus.NOT_GET)
                                        .addRewards(rewardList)
                                        .build());
                            }
                            return null;
                        }
                    });
                }else{
                	//则是人数不够不能进行匹配
                	HawkLog.logPrintln("TBLYWarService-sendMatchResult not in pool teamId:{}", teamId);
                	HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
                        @Override
                        public Object run() {
                            // 匹配失败邮件
                            for (String playerId : playerIds) {
                                SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                                        .setPlayerId(playerId)
                                        .setMailId(MailConst.MailId.TBLY_GUILD_MEMBER_NOT_ENOUGH)
                                        .build());

                            }
                            return null;
                        }
                    });
                }
                
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    public void sendMatchResult(String teamId, int timeIndex){
        try {
            Set<String> playerIds = TBLYGuildTeamManager.getInstance().getTeamPlayerIds(teamId);
            if(playerIds == null || playerIds.isEmpty()){
                return;
            }
            long startTime = HawkTime.getMillisecond();
            WarTimeChoose battleTime = getChooses().get(timeIndex-1);
            if(battleTime != null){
                startTime = battleTime.getTime();
            }
            String startData = HawkTime.formatTime(startTime);
            HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
                @Override
                public Object run() {
                    // 匹配成功邮件
                    for (String playerId : playerIds) {
                        //SystemMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(playerId).setMailId(MailConst.MailId.XW_MATCH_RESULT).addContents(startData).build());
                    }
                    return null;
                }
            });
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 加载报名数据
     */
    public void loadSignUp(){
        try {
            String signUpServerKey = String.format(TBLYWarResidKey.TBLY_WAR_SIGNUP_SERVER, getTermId(), GsConfig.getInstance().getServerId());
            Map<String, String> tmp = RedisProxy.getInstance().getRedisSession().hGetAll(signUpServerKey);
            if(tmp == null || tmp.isEmpty()){
                HawkLog.logPrintln("TBLYWarService loadSignUp data is empty");
                return;
            }
            HawkLog.logPrintln("TBLYWarService loadSignUp start size:{}", tmp.size());
            Map<String, Integer> signUpMap = new ConcurrentHashMap<>();
            for(Map.Entry<String, String> entry : tmp.entrySet()){
                try {
                    HawkLog.logPrintln("TBLYWarService loadSignUp key:{}, value:{}", entry.getKey(), entry.getValue());
                    signUpMap.put(entry.getKey(), Integer.parseInt(entry.getValue()));
                }catch (Exception e){
                    HawkException.catchException(e);
                    HawkLog.logPrintln("TBLYWarService loadSignUp error key:{}, value:{}", entry.getKey(), entry.getValue());
                }
            }
            this.signUpMap = signUpMap;
            HawkLog.logPrintln("TBLYWarService loadSignUp end");
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 匹配结束后加载战场数据
     */
    public void loadBattleRoom() {
        try {
            //获得期数
            int termId = getTermId();
            //房间id的key，只存了和本服相关的房间的id
            String roomServerKey = String.format(TBLYWarResidKey.TBLY_WAR_ROOM_SERVER, termId, GsConfig.getInstance().getServerId());
            //本服参与或开在本服战场的id
            Set<String> roomIds = RedisProxy.getInstance().getRedisSession().sMembers(roomServerKey);
            //如果为空直接返回
            if (roomIds == null || roomIds.isEmpty()) {
                HawkLog.logPrintln("TBLYWarService loadBattleRoom roomIds is empty");
                return;
            }
            //房间加载开始
            HawkLog.logPrintln("TBLYWarService loadBattleRoom start termId:{}, size:{}", termId, roomIds.size());
            //加载房间redis数据
            List<String> roomList = RedisProxy.getInstance().getRedisSession().hmGet(String.format(TBLYWarResidKey.TBLY_WAR_ROOM, termId), roomIds.toArray(new String[roomIds.size()]));
            //如果为空就返回，一般不会为空上面id已经校验一遍了，以防万一再检查一次
            if (roomList == null || roomList.isEmpty()) {
                HawkLog.logPrintln("TBLYWarService loadBattleRoom roomList is empty");
                return;
            }
            //参与相关战场的小队id
            Set<String> teamIds = new HashSet<>();
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
                    teamIdToRoomId.put(roomData.campA, roomData.id);
                    teamIdToRoomId.put(roomData.campB, roomData.id);
                    //把参与的小队id存下来
                    teamIds.add(roomData.campA);
                    teamIds.add(roomData.campB);
                    //关联时间索引和房间id
                    if (!timeIndexToRoomIds.containsKey(roomData.timeIndex)) {
                        timeIndexToRoomIds.put(roomData.timeIndex, new CopyOnWriteArraySet<>());
                    }
                    timeIndexToRoomIds.get(roomData.timeIndex).add(roomData.id);
                } catch (Exception e) {
                    HawkException.catchException(e);
                    HawkLog.logPrintln("TBLYWarService loadBattleRoom error roomData:{}", roomStr);
                }
            }
            //加载参与的小队数据
            loadBattleTeam(teamIds);
            //加载参与的玩家数据
            loadBattlePlayer(teamIds);
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
    public void loadBattleTeam(Set<String> teamIds){
        try {
            //如果为空直接返回
            if(teamIds == null || teamIds.isEmpty()){
                HawkLog.logPrintln("TBLYWarService loadBattleTeam teamIds is empty");
                return;
            }
            //本服id，用于判断小队是否是本服的
            String serverId = GsConfig.getInstance().getServerId();
            //加载相关小队数据
            List<GuildTeamData> list = TBLYGuildTeamManager.getInstance().loadTeams(teamIds);
            //遍历小队数据
            for(GuildTeamData teamData : list) {
                try {
                    //加入参战小队数据缓存中
                    battleTeamDataMap.put(teamData.id, teamData);
                    //如果小队是本服小队，更新本服小队数据
                    if(serverId.equals(teamData.serverId)){
                        TBLYGuildTeamManager.getInstance().updataFromBattle(teamData);
                    }
                    teamIdToBattlePlayerIds.put(teamData.id, new CopyOnWriteArraySet<>());
                }catch (Exception e){
                    HawkException.catchException(e);
                }
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    public GuildTeamData getBattleTeam(String teamId){
        return battleTeamDataMap.get(teamId);
    }

    /**
     * 参战成员数据
     * @param teamIds 参战队伍id
     */
    public void loadBattlePlayer(Set<String> teamIds){
        try {
            //如果为空直接返回
            if(teamIds == null || teamIds.isEmpty()){
                HawkLog.logPrintln("TBLYWarService loadBattlePlayer teamIds is empty");
                return;
            }
            //获得期数
            int termId = getTermId();
            for(String teamId : teamIds){
                try {
                    HawkLog.logPrintln("TBLYWarService loadBattlePlayer team start teamId:{}", teamId);
                    //获得当期期参与人的信息
                    String signUpPlayerKey = String.format(TBLYWarResidKey.TBLY_WAR_SIGNUP_PLAYER, termId, teamId);
                    Map<String, String> playerAuth = RedisProxy.getInstance().getRedisSession().hGetAll(signUpPlayerKey);
                    //如果为空直接跳过
                    if(playerAuth == null || playerAuth.isEmpty()){
                        HawkLog.logPrintln("TBLYWarService loadBattlePlayer playerAuth is empty, teamId:{}",teamId);
                        continue;
                    }
                    //遍历玩家
                    for(String playerId : playerAuth.keySet()){
                        try {
                            HawkLog.logPrintln("TBLYWarService loadBattlePlayer playerId:{},auth:{}", playerId, playerAuth.get(playerId));
                            int auth = Integer.parseInt(playerAuth.get(playerId));
                            //关联玩家和小队
                            playerIdToTeamId.put(playerId, teamId);
                            //关联玩家和房间
                            playerIdToRoomId.put(playerId, teamIdToRoomId.get(teamId));
                            //关联玩家和权限
                            playerIdToAuth.put(playerId, auth);
                            teamIdToBattlePlayerIds.get(teamId).addAll(playerAuth.keySet());

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

    public HawkTuple2<GuildTeamData, GuildTeamData> matchTeam(List<GuildTeamData> teamList){
        if(teamList == null || teamList.isEmpty()){
            return null;
        }
        //todo WHC TBLY
        int teamCount = 6;
        teamCount = Math.min(teamCount, teamList.size());
        List<GuildTeamData> subList = teamList.subList(0, teamCount);
        GuildTeamData teamData1 = subList.get(0);
        List<GuildTeamData> tmpList = new ArrayList<>();
        for(int i = 1; i < subList.size(); i++){
            GuildTeamData tmpData = subList.get(i);
            if(tmpData.guildId.equals(teamData1.guildId)){
                continue;
            }
            tmpList.add(tmpData);
        }
        if(tmpList.isEmpty()){
            return new HawkTuple2<>(teamData1, null);
        }
        Collections.shuffle(tmpList);
        GuildTeamData teamData2 = tmpList.get(0);
        return new HawkTuple2<>(teamData1, teamData2);
    }

    public void signUp(Player player, String teamId, int index) {
        signUpMap.put(teamId, index);
        String signUpServerKey = String.format(TBLYWarResidKey.TBLY_WAR_SIGNUP_SERVER, getTermId(), GsConfig.getInstance().getServerId());
        RedisProxy.getInstance().getRedisSession().hSet(signUpServerKey, teamId, String.valueOf(index));
        
        List<WarTimeChoose> chooseTimeList = getChooses();
        if(chooseTimeList.size() >= index){
            long startTime = chooseTimeList.get(index - 1).getTime();
            ScheduleInfo schedule = ScheduleInfo.createNewSchedule(ScheduleType.SCHEDULE_TYPE_1_VALUE, player.getGuildId(), startTime, 0, 0,teamId);
            ScheduleService.getInstance().addSystemSchedule(schedule);
        }
		
        GuildTeamData teamData = TBLYGuildTeamManager.getInstance().getTeamData(teamId);
        if(teamData!= null){
            LogUtil.logTimberiumSignup(teamData.id, teamData.name, teamData.memberCnt, teamData.battlePoint, GsConfig.getInstance().getServerId(), getTermId(), index, player.getId());
        }
    }

    /**
     * 更新报名信息
     */
    public void updateSignUpInfo(){
        try{
            int openDayW = getOpenDayW();
            //获得期数
            int termId = getTermId();
            Set<String> guildIds = new HashSet<>();
            for(String teamId : signUpMap.keySet()) {
                try {
                    GuildTeamData teamData = TBLYGuildTeamManager.getInstance().getTeamData(teamId);
                    if(teamData == null){
                        continue;
                    }
                    if(teamData.timeIndex != signUpMap.get(teamId)){
                        continue;
                    }
                    Set<String> playerIds = TBLYGuildTeamManager.getInstance().getTeamPlayerIds(teamId);
                    if(playerIds == null || playerIds.size() < TiberiumConstCfg.getInstance().getWarMemberMinCnt()){
                    	HawkLog.logPrintln("TBLYWarService-updateSignUpInfo member less teamId:{}", teamId);
                        continue;
                    }
                    guildIds.add(teamData.guildId);
                    long battlePoint = 0L;
                    long matchPower = 0L;
                    Map<String, String> playerAuth = new HashMap<>();
                    //玩家权限map
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
                            long playerStrength = player.getStrength();
                            matchPower += playerStrength;
                            long noArmyPoint = player.getNoArmyPower();
                            battlePoint += noArmyPoint;
                            playerAuth.put(playerData.id, String.valueOf(playerData.auth));
                            LogUtil.logTimberiumPlayerWarInfo(playerId, teamId, playerStrength, termId);
                        } catch (Exception e) {
                            HawkException.catchException(e);
                        }
                    }
                    teamData.matchPower = matchPower;
                    teamData.battlePoint = battlePoint;
                    teamData.memberCnt = playerAuth.size();
                    teamData.serverId = GsConfig.getInstance().getServerId();
                    teamData.openDayW = openDayW;
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
                    String signUpTimeKey = String.format(TBLYWarResidKey.TBLY_WAR_SIGNUP_TIME, getTermId(), teamData.timeIndex);
                    RedisProxy.getInstance().getRedisSession().sAdd(signUpTimeKey, 0, teamData.id);
                    String signUpPlayerKey = String.format(TBLYWarResidKey.TBLY_WAR_SIGNUP_PLAYER, termId, teamId);
                    if(!playerAuth.isEmpty()){
                        //把数据存入redis
                        RedisProxy.getInstance().getRedisSession().hmSet(signUpPlayerKey, playerAuth, 0);
                    }
                    try {
                        HawkLog.logPrintln("TiberiumWarService flushSignerInfo, teamId: {}, teamName: {} , serverId: {}, memberCnt: {}, totalPowar: {}, termId: {}, timeIndex: {}", teamId, teamData.name, teamData.serverId, teamData.memberCnt, teamData.battlePoint, termId, teamData.timeIndex);
                        LogUtil.logTimberiumMatcherInfo(teamId, teamData.name, teamData.serverId, teamData.memberCnt, teamData.battlePoint, teamData.matchPower, termId, teamData.timeIndex);
                    } catch (Exception e) {
                        HawkException.catchException(e);
                    }
                }catch (Exception e){
                    HawkException.catchException(e);
                }
            }
            Map<String, String> guildDataMap = new HashMap<>();
            List<TWGuildData> twGuildDataList = new ArrayList<>();
            for(String guildId : guildIds) {
                try {
                    //生成联盟数据
                    GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(guildId);
                    guildDataMap.put(guildId, new GuildTeamGuildData(guildInfoObject).serialize());
                    twGuildDataList.add(buildTWGuildData(guildInfoObject));
                } catch (Exception e) {
                    HawkException.catchException(e);
                }
            }
            String guildKey = String.format(TBLYWarResidKey.TBLY_WAR_GUILD, termId);
            RedisProxy.getInstance().getRedisSession().hmSet(guildKey, guildDataMap, 0);
            RedisProxy.getInstance().updateTWGuildData(twGuildDataList, termId);
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

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

    public GuildTeamGuildData loadGuildData(String guildId) {
        int termId = getTermId();
        String guildKey = String.format(TBLYWarResidKey.TBLY_WAR_GUILD, termId);
        String guildStr = RedisProxy.getInstance().getRedisSession().hGet(guildKey, guildId);
        return GuildTeamGuildData.unSerialize(guildStr);
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

    public void clean(){
        try {

            TBLYGuildTeamManager.getInstance().cleanTeam();
            TBLYGuildTeamManager.getInstance().cleanPlayer();
            cleanSignUp();
            cleanBattle();
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    public void cleanSignUp(){
        try {
            signUpMap.clear();
        }catch (Exception e){
            HawkException.catchException(e);
        }

    }

    public void cleanBattle(){
        try {
            battleStateDataMap.clear();
            roomDataMap.clear();
            timeIndexToRoomIds.clear();
            battleTeamDataMap.clear();
            teamIdToRoomId.clear();
            playerIdToRoomId.clear();
            playerIdToTeamId.clear();
            playerIdToAuth.clear();
            teamIdToBattlePlayerIds.clear();
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    public boolean isTeamJoin(Player player){
        try {
            if(player.isCsPlayer()){
                return true;
            }
            String guildId = player.getGuildId();
            if (HawkOSOperator.isEmptyString(guildId)) {
                return false;
            }
            GuildTeamData teamData = getTeam(player);
            if(teamData != null && isSignUp(teamData.id)){
                return true;
            }
            GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
            if (guild == null) {
                return false;
            }
            if(HawkTime.getMillisecond() - guild.getCreateTime() < TiberiumConstCfg.getInstance().getGuildCreateTimeLimit()){
                return false;
            }
            int rankLimit = TiberiumConstCfg.getInstance().getSignRankLimit();
            Rank.RankInfo rankInfo = RankService.getInstance().getRankInfo(Rank.RankType.ALLIANCE_FIGHT_KEY, guildId);
            // 联盟战力排名限制
            if (rankInfo == null || rankInfo.getRank() > rankLimit) {
                return false;
            }
            return true;
        }catch (Exception e){
            HawkException.catchException(e);
            return false;
        }


    }

    public GuildTeamData getTeam(Player player){
        GuildTeamPlayerData playerData = TBLYGuildTeamManager.getInstance().getPlayerData(player.getId());
        if(playerData == null || HawkOSOperator.isEmptyString(playerData.teamId)){
            return null;
        }
        return TBLYGuildTeamManager.getInstance().getTeamData(playerData.teamId);
    }


    public boolean isPlayerJoin(Player player){
        if(player.isCsPlayer()){
            return true;
        }
        GuildTeamData teamData = getTeam(player);
        return teamData != null;
    }

    public boolean isSignUp(Player player){
        GuildTeamData teamData = getTeam(player);
        if(teamData == null){
            return false;
        }
        return isSignUp(teamData.id);
    }

    public boolean isSignUp(String teamId){
        return signUpMap.containsKey(teamId);
    }

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
            GuildTeamRoomData roomData = roomDataMap.get(teamIdToRoomId.get(playerData.teamId));
            if(roomData == null){
                player.sendError(HP.code.TIBERIUM_WAR_ENTER_INSTANCE_REQ_VALUE, Status.Error.TIBERIUM_NOT_IN_THIS_WAR_VALUE,0);
                return false;
            }
            if(roomData.timeIndex <= 0 ||stateData.getState() != TBLYWarStateEnum.BATTLE){
                player.sendError(HP.code.TIBERIUM_WAR_ENTER_INSTANCE_REQ_VALUE, Status.Error.TIBERIUM_NOT_IN_THIS_WAR_VALUE, 0);
                return false;
            }
            if(battleStateDataMap.get(roomData.timeIndex).getState() != TBLYWarStateEnum.BATTLE_OPEN){
                player.sendError(HP.code.TIBERIUM_WAR_ENTER_INSTANCE_REQ_VALUE, Status.Error.TIBERIUM_NOT_IN_THIS_WAR_VALUE, 0);
                return false;
            }
            long now = HawkTime.getMillisecond();
            long warStartTime = HawkTime.getMillisecond();
            long warEndTime = warStartTime + TiberiumConstCfg.getInstance().getWarOpenTime();
            List<WarTimeChoose> timeList = TBLYWarService.getInstance().getChooses();
            if(timeList.size() >= roomData.timeIndex) {
                WarTimeChoose battleTime = getChooses().get(roomData.timeIndex - 1);
                warStartTime = battleTime.getTime();
                warEndTime = warStartTime + TiberiumConstCfg.getInstance().getWarOpenTime();
            }
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
        }catch (Exception e){
            HawkException.catchException(e);
            return false;
        }
        return true;
    }

    public void updateTeamEnterNum(String teamId, long add){
        try {
            if(HawkOSOperator.isEmptyString(teamId)){
                return;
            }
            String teamEnterKey = String.format(TBLYWarResidKey.TBLY_WAR_TEAM_ENTER, getTermId(), teamId);
            RedisProxy.getInstance().getRedisSession().increaseBy(teamEnterKey, add, TiberiumConst.TLW_EXPIRE_SECONDS);
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    public int getTeamEnterNum(String teamId){
        try {
            String teamEnterKey = String.format(TBLYWarResidKey.TBLY_WAR_TEAM_ENTER, getTermId(), teamId);
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

    boolean isEnterMax(Player player){
        try {
            String teamId = playerIdToTeamId.get(player.getId());
            if(HawkOSOperator.isEmptyString(teamId)){
                return false;
            }
            int teamEnterNum = getTeamEnterNum(teamId);
            return teamEnterNum >= TiberiumConstCfg.getInstance().getWarMemberLimit();
        } catch (Exception e) {
            HawkException.catchException(e);
            return true;
        }
    }

    /**
     * 进入战场
     * @param player 玩家
     * @return 执行结果
     */
    public boolean joinRoom(Player player) {
        String roomId = playerIdToRoomId.get(player.getId());
        GuildTeamRoomData roomData = roomDataMap.get(roomId);
        if (!TBLYRoomManager.getInstance().hasGame(roomId)) {
            return false;
        }
        if (!TBLYRoomManager.getInstance().joinGame(roomData.id, player)) {
            return false;
        }
        updateTeamEnterNum(playerIdToTeamId.get(player.getId()), 1L);
        GuildTeamPlayerData playerData = TBLYGuildTeamManager.getInstance().load(player.getId());
        LogUtil.logTimberiumEnterInfo(player.getId(), getTermId(), roomId, roomData.roomServerId, playerData.teamId, playerData.serverId,
                player.getPower(), battleTeamDataMap.get(playerData.teamId).matchPower);
        return true;
    }

    /**
     * 退出战场
     * @param player 玩家
     * @param isMidwayQuit 是否中途退出
     * @return 执行结果
     */
    public boolean quitRoom(Player player, boolean isMidwayQuit){
        GuildTeamPlayerData playerData = TBLYGuildTeamManager.getInstance().load(player.getId());
        playerData.quitTIme = HawkTime.getMillisecond();
        playerData.isMidwayQuit = isMidwayQuit;
        TBLYGuildTeamManager.getInstance().updatePlayer(playerData);
        LogUtil.logTimberiumQuitInfo(player.getId(), getTermId(), playerData.teamId, isMidwayQuit);
        String teamId = playerIdToTeamId.get(player.getId());
        updateTeamEnterNum(teamId, -1L);
        return true;
    }

    /**
     * 获得战场数据
     * @param player 玩家
     * @return 战场数据
     */
    public GuildTeamRoomData getRoomData(Player player){
        return roomDataMap.get(playerIdToRoomId.get(player.getId()));
    }


    /**
     * 发奖
     * @param timeIndex 时间索引
     * @return 执行结果
     */
    public boolean sendAward(int timeIndex){
        int termId = getTermId();
        Map<String, Long> playerScoreMap = new HashMap<>();
        Map<String, Long> guildScoreMap = new HashMap<>();
        for(String teamId : signUpMap.keySet()) {
            int chooseTime = signUpMap.get(teamId);
            if (chooseTime != timeIndex) {
                continue;
            }
            String teamAwardKey = String.format(TBLYWarResidKey.TBLY_WAR_TEAM_AWARD, termId);
            boolean teamGetLock = RedisProxy.getInstance().getRedisSession().hSetNx(teamAwardKey, teamId, String.valueOf(HawkTime.getMillisecond())) > 0;
            if (!teamGetLock) {
                continue;
            }
            String roomId = teamIdToRoomId.get(teamId);
            if (HawkOSOperator.isEmptyString(roomId)) {
                HawkLog.logPrintln("TBLYWarService sendAward roomId is null teamId:{}", teamId);
                continue;
            }
            GuildTeamRoomData roomData = roomDataMap.get(roomId);
            if (roomData == null) {
                continue;
            }
            long selfScore = 0;
            long oppScore = 0;
            if(teamId.equals(roomData.campA)){
                selfScore = roomData.scoreA;
                oppScore = roomData.scoreB;
            }else {
                selfScore = roomData.scoreB;
                oppScore = roomData.scoreA;
            }
            boolean isWin = roomData.winnerId.equals(teamId);
            MailConst.MailId mailId = isWin? MailConst.MailId.TBLY_GUILD_WIN_MAIL : MailConst.MailId.TBLY_GUILD_LOSE_MAIL;
            MailConst.MailId selfMailId = isWin ? MailConst.MailId.TBLY_SELF_WIN_MAIL : MailConst.MailId.TBLY_SELF_LOSE_MAIL;
            TiberiumGuildAwardCfg rewardCfg = getGuildAwardCfg(isWin);
            for (String playerId : TBLYGuildTeamManager.getInstance().getTeamPlayerIds(teamId)) {
                try {
                    String playerAwardKey = String.format(TBLYWarResidKey.TBLY_WAR_PLAYER_AWARD, termId);
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
                    String scoreStr = RedisProxy.getInstance().getRedisSession().hGet(String.format(TBLYWarResidKey.TBLY_WAR_PLAYER_SCORE, getTermId()), playerId);
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
                            ActivityManager.getInstance().postEvent(new TWScoreEvent(playerId, playerSelfScore, false,1));
                        } catch (Exception e) {
                            HawkException.catchException(e);
                        }
                    }
                    MissionManager.getInstance().postMsg(playerId, new EventTiberiumWar());
                } catch (Exception e) {
                    HawkException.catchException(e);
                }
            }
            GuildTeamData teamData = TBLYGuildTeamManager.getInstance().getTeamData(teamId);
            if(teamData != null){
                long guildScore = guildScoreMap.getOrDefault(teamData.guildId, 0L);
                guildScore = guildScore + selfScore;
                guildScoreMap.put(teamData.guildId, guildScore);
            }
            try {
                ActivityService.getInstance().dealMsg(GameConst.MsgId.ON_GUILD_BACK_DROP, new GuildBackDropInvoker(new ArrayList<>(TBLYGuildTeamManager.getInstance().getTeamPlayerIds(teamId))));
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
        for(String guildId : guildScoreMap.keySet()){
            TBLYSeasonService.getInstance().addGuildScore(guildId, guildScoreMap.get(guildId), playerScoreMap);
        }
        return true;
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

    public TiberiumGuildAwardCfg getGuildAwardCfg(boolean isWin) {
        ConfigIterator<TiberiumGuildAwardCfg> its = HawkConfigManager.getInstance().getConfigIterator(TiberiumGuildAwardCfg.class);
        for(TiberiumGuildAwardCfg cfg : its){
            if(isWin == cfg.isWin()){
                return cfg;
            }
        }
        return null;
    }

    /*******************************************************************************************************************
     * 观战相关
     ******************************************************************************************************************/
    /**
     * 表演赛模式获取当前进行中的比赛列表
     * @return
     */
    public TLWGetOBRoomInfo.Builder getTWRoomInfo() {
        TLWGetOBRoomInfo.Builder builder = TLWGetOBRoomInfo.newBuilder();
        int termId = getTermId();
        builder.setSeason(0);
        builder.setTermId(termId);
        if(getState() != TBLYWarStateEnum.BATTLE){
            return builder;
        }

        int currTimeIndex = -1;
        long currTime = HawkTime.getMillisecond();
        List<WarTimeChoose> timeList = TBLYWarService.getInstance().getChooses();
        for(int i=0;i<timeList.size();i++){
            WarTimeChoose timeChoose = timeList.get(i);
            long startTime = timeChoose.getTime();
            long endTime = startTime + TiberiumConstCfg.getInstance().getWarOpenTime();
            if(currTime>=startTime && currTime< endTime){
                currTimeIndex = i;
                builder.setWarStartTime(startTime);
                builder.setWarFinishTime(endTime);
                break;
            }
        }
        // 当前接段没有开启的房间
        if(currTimeIndex == -1){
            return builder;
        }
        Map<String, String> roomMap = RedisProxy.getInstance().getRedisSession().hGetAll(String.format(TBLYWarResidKey.TBLY_WAR_ROOM, termId));
        if(roomMap == null || roomMap.isEmpty()){
            return builder;
        }
        Set<String> teamIds = new HashSet<>();
        for(String roomStr : roomMap.values()){
            GuildTeamRoomData roomData = GuildTeamRoomData.unSerialize(roomStr);
            if (roomData == null){
                continue;
            }
            if(roomData.timeIndex != currTimeIndex + 1){
                continue;
            }
            teamIds.add(roomData.campA);
            teamIds.add(roomData.campB);
        }
        //加载相关小队数据
        Map<String, GuildTeamData> teamMap = TBLYGuildTeamManager.getInstance().loadTeamMap(teamIds);
        for(String roomStr : roomMap.values()){
            //解析房间数据
            GuildTeamRoomData roomData = GuildTeamRoomData.unSerialize(roomStr);
            if (roomData == null){
                continue;
            }
            if(roomData.timeIndex != currTimeIndex + 1){
                continue;
            }
            TLWRoomInfo.Builder obRoomInfo = TLWRoomInfo.newBuilder();
            obRoomInfo.setRoomId(roomData.id);
            obRoomInfo.setRoomServer(roomData.roomServerId);
            TLWGetMatchInfo.Builder matchBuilder = TLWGetMatchInfo.newBuilder();
            GuildTeamData guildA = teamMap.get(roomData.campA);
            GuildTeamData guildB = teamMap.get(roomData.campB);
            if(guildA == null || guildB==null){
                continue;
            }
            TLWGuildBaseInfo.Builder guildBuilderA = TLWGuildBaseInfo.newBuilder();
            guildBuilderA.setId(guildA.id);
            guildBuilderA.setName(guildA.guildName);
            guildBuilderA.setTag(guildA.guildTag);
            guildBuilderA.setGuildFlag(guildA.guildFlag);
            guildBuilderA.setLeaderName(guildA.name);
            guildBuilderA.setServerId(guildA.serverId);
            matchBuilder.setGuildA(guildBuilderA);

            TLWGuildBaseInfo.Builder guildBuilderB = TLWGuildBaseInfo.newBuilder();
            guildBuilderB.setId(guildB.id);
            guildBuilderB.setName(guildB.name);
            guildBuilderB.setTag(guildB.guildTag);
            guildBuilderB.setGuildFlag(guildB.guildFlag);
            guildBuilderB.setLeaderName(guildB.name);
            guildBuilderB.setServerId(guildB.serverId);
            matchBuilder.setGuildB(guildBuilderB);
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
                if(stateData.getState() == TBLYWarStateEnum.BATTLE){
                    boolean isDone = false;
                    for(int i = 1; i <= 4; i++){
                        if(battleStateDataMap.get(i).getState() != TBLYWarStateEnum.BATTLE_END){
                            battleStateDataMap.get(i).toNext();
                            isDone = true;
                            break;
                        }
                    }
                    if(!isDone){
                        stateData.toNext();
                    }
                }else {
                    stateData.toNext();
                }
                return printInfo();
            }
            case "bigNext":{
                stateData.toNext();
                return printInfo();
            }
            case "battleNext":{
                if(stateData.getState() == TBLYWarStateEnum.BATTLE){
                    if(map.containsKey("timeIndex")){
                        int timeIndex = Integer.parseInt(map.get("timeIndex"));
                        battleStateDataMap.get(timeIndex).toNext();
                    }else {
                        for(int i = 1; i <= 4; i++){
                            if(battleStateDataMap.get(i).getState() != TBLYWarStateEnum.BATTLE_END){
                                battleStateDataMap.get(i).toNext();
                                break;
                            }
                        }
                    }
                }
                return printInfo();
            }
            //匹配
            case "match":{
                TBLYWarService.getInstance().onMatch();
                return printInfo();
            }
            //加载房间数据
            case "loadRoom":{
                TBLYWarService.getInstance().loadBattleRoom();
                return printInfo();
            }
            //战斗结束
            case "battleOver":{
                TBLYRoomManager.getInstance().findAllRoom().forEach(room -> room.setState(new TBLYGameOver(room)));
                return printInfo();
            }
            case "sendAward":{
                if(map.containsKey("timeIndex")){
                    int timeIndex = Integer.parseInt(map.get("timeIndex"));
                    TBLYWarService.getInstance().sendAward(timeIndex);
                }else {
                    TBLYWarService.getInstance().sendAward(1);
                }
                return printInfo();
            }
            case "matchInfo":{
                return printMatchInfo(Integer.parseInt(map.getOrDefault("termId", "0")), Integer.parseInt(map.getOrDefault("timeIndex", "0")));
            }
            case "signupInfo":{
                return printSignupInfo(Integer.parseInt(map.getOrDefault("termId", "0")), Integer.parseInt(map.getOrDefault("timeIndex", "0")));
            }
        }
        return "";
    }

    public String printMatchInfo(int termId, int timeIndex) {
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
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=info\">主页</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=matchInfo\">全部</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=matchInfo&timeIndex=1\">时间段1</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=matchInfo&timeIndex=2\">时间段2</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=matchInfo&timeIndex=3\">时间段3</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=matchInfo&timeIndex=4\">时间段4</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=signupInfo\" target=\"_blank\">报名信息</a><br>";
            //获得期数
            termId = termId == 0 ? getTermId(): termId;
            Map<String, String> roomMap = RedisProxy.getInstance().getRedisSession().hGetAll(String.format(TBLYWarResidKey.TBLY_WAR_ROOM, termId));
            Map<String, String> teamMap = RedisProxy.getInstance().getRedisSession().hGetAll(TBLYGuildTeamManager.getInstance().getTeamKey());
            for(String roomStr : roomMap.values()){
                //解析房间数据
                GuildTeamRoomData roomData = GuildTeamRoomData.unSerialize(roomStr);
                //数据为空的话跳过
                if(roomData == null){
                    continue;
                }
                if(timeIndex != 0 && roomData.timeIndex != timeIndex){
                    continue;
                }
                info += gmFillRoomData(roomData, teamMap);
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
        return info;
    }

    public String printSignupInfo(int termId, int timeIndex) {
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
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=info\">主页</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=signupInfo\">全部</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=signupInfo&timeIndex=1\">时间段1</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=signupInfo&timeIndex=2\">时间段2</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=signupInfo&timeIndex=3\">时间段3</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=signupInfo&timeIndex=4\">时间段4</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=matchInfo\" target=\"_blank\">战场信息</a><br>";
            //获得期数
            termId = termId == 0 ? getTermId(): termId;
            Map<String, String> teamMap = RedisProxy.getInstance().getRedisSession().hGetAll(TBLYGuildTeamManager.getInstance().getTeamKey());
            if(timeIndex == 0){
                for(int i = 1; i <= 4; i++) {
                    info += "==========================================================================================<br>";
                    info += "报名时间段:" + i + "<br>";
                    String signUpTimeKey = String.format(TBLYWarResidKey.TBLY_WAR_SIGNUP_TIME, termId, i);
                    Set<String> teamIds = RedisProxy.getInstance().getRedisSession().sMembers(signUpTimeKey);
                    if (teamIds != null && !teamIds.isEmpty()) {
                        for (String teamId : teamIds) {
                            info += "------------------------------------------------------------------------------------------";
                            info += gmFillTeamData(teamMap.get(teamId));
                        }
                    }
                    info += "==========================================================================================<br>";
                }
            }else {
                info += "==========================================================================================<br>";
                info += "报名时间段:" + timeIndex + "<br>";
                String signUpTimeKey = String.format(TBLYWarResidKey.TBLY_WAR_SIGNUP_TIME, termId, timeIndex);
                Set<String> teamIds = RedisProxy.getInstance().getRedisSession().sMembers(signUpTimeKey);
                if (teamIds != null && !teamIds.isEmpty()) {
                    for(String teamId : teamIds){
                        info +=  "------------------------------------------------------------------------------------------";
                        info += gmFillTeamData(teamMap.get(teamId));

                    }
                }
                info += "==========================================================================================<br>";
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
        return info;
    }

    public String gmFillRoomData(GuildTeamRoomData roomData, Map<String, String> teamMap){
        String info = "==========================================================================================<br>";
        info += "房间id:" + roomData.id + "<br>";
        info += "报名时间段:" + roomData.timeIndex + "<br>";
        info += "战场服:" + roomData.roomServerId + "<br>";
        info += "小队1分数:" + roomData.scoreA + "<br>";
        info += "小队2分数:" + roomData.scoreB + "<br>";
        info += "获胜小队id:" + roomData.winnerId + "<br>";
        info +=  "------------------------------------------------------------------------------------------";
        info += "<br>小队1:" + gmFillTeamData(teamMap.get(roomData.campA));
        info +=  "------------------------------------------------------------------------------------------";
        info += "<br>小队2:" + gmFillTeamData(teamMap.get(roomData.campB));
        info +=  "==========================================================================================<br>";
        return info;
    }

    public String gmFillTeamData(String teamStr){
        String info = "<br>";
        GuildTeamData teamData = GuildTeamData.unSerialize(teamStr);
        if(teamData == null){
            return info;
        }
        Set<String> playerIds = TBLYGuildTeamManager.getInstance().getTeamPlayerIds(teamData.id);
        long matchPower = 0L;
        //如果为空直接返回
        if(playerIds != null && !playerIds.isEmpty()){
            for (String playerId : playerIds){
                try {
                    Player player = GlobalData.getInstance().makesurePlayer(playerId);
                    if(player == null){
                        continue;
                    }
                    matchPower += player.getStrength();
                }catch (Exception e){
                    HawkException.catchException(e);
                }
            }
        }
        float param = 1f;
        info += "小队id:" + teamData.id + "<br>";
        info += "小队服:" + teamData.serverId + "<br>";
        info += "小队名字:" + teamData.name + "<br>";
        info += "小队人数:" + teamData.memberCnt + "<br>";
        info += "小队战力:" + teamData.battlePoint + "<br>";
        info += "小队匹配战力:" + teamData.matchPower + "<br>";
        info += "小队队员匹配战力:" + matchPower + "<br>";
        info += "小队匹配修正:" + param + "<br>";
        info += "小队联盟:" + teamData.guildName + "<br>";
        info += "小队分数:" + teamData.score + "<br>";
        return info;
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
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=info\">刷新</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=next\">切阶段</a>          ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=bigNext\">切大阶段</a>         ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=battleNext&timeIndex=1\">切战场1阶段</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=battleNext&timeIndex=2\">切战场2阶段</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=battleNext&timeIndex=3\">切战场3阶段</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=battleNext&timeIndex=4\">切战场4阶段</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=battleOver\">结束战斗</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=doTeamRank\">刷新排行榜</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=matchTestInfo\">匹配测试</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=signupInfo\" target=\"_blank\">报名信息</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=TBLYGM&cmd=matchInfo\" target=\"_blank\">战场信息</a><br><br>";
            info += stateData.toString() + "<br>";
            for(TBLYWarBattleStateData battleStateData : battleStateDataMap.values()){
                info += battleStateData.toString() + "<br>";
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
        return info;
    }
}
