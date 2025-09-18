package com.hawk.game.service.xhjzWar;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.XWScoreEvent;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.XHJZShopCfg;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.guildBackActivity.GuildBackDropInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZBattleRoom;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZExtraParam;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager;
import com.hawk.game.module.lianmenxhjz.battleroom.msg.XHJZBilingInformationMsg;
import com.hawk.game.module.lianmenxhjz.battleroom.roomstate.XHJZGameOver;
import com.hawk.game.module.schedule.ScheduleInfo;
import com.hawk.game.module.schedule.ScheduleService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.*;
import com.hawk.game.protocol.Schedule.ScheduleType;
import com.hawk.game.service.ActivityService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.commonMatch.data.CMWData;
import com.hawk.game.service.commonMatch.manager.ipml.XHJZSeasonManager;
import com.hawk.game.service.commonMatch.state.CMWStateEnum;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.config.XHJZConstCfg;
import com.hawk.game.config.XHJZRewardCfg;
import com.hawk.game.config.XHJZWarTimeCfg;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import com.hawk.log.LogConst;
import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.obj.HawkObjBase;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.xid.HawkXID;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

/**
 * 星海激战赛制
 */
public class XHJZWarService extends HawkAppObj {
    /*******************************************************************************************************************
     * 活动状态机
     ******************************************************************************************************************/
    /**
     * 主状态机
     */
    private XHJZWarStateData stateData;
    /**
     * 战斗状态机
     * key:场次时间索引
     * value:对应场次状态机
     */
    private Map<Integer, XHJZWarBattleStateData> battleStateDataMap = new ConcurrentHashMap<>();
    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 常驻数据
     * 不删除
     ******************************************************************************************************************/
    /**
     * 小队数据
     * key:小队id
     * value:小队数据
     */
    private Map<String, XHJZWarTeamData> teamDataMap = new ConcurrentHashMap<>();
    /**
     * 玩家数据
     * key:玩家id
     * value:玩家数据
     */
    private Map<String, XHJZWarPlayerData> playerDataMap = new ConcurrentHashMap<>();
    /**
     * 通过小队id索引玩家id
     * key:小队id
     * value:玩家id集合
     */
    private Map<String, Set<String>> teamIdToPlayerIds = new ConcurrentHashMap<>();
    /**
     * 兑换商店期数key, 偷懒行为，别学，少加一个字段
     */
    private static final int XHJZ_SHOP_TERM = 100001;
    /**==============================================================================================================**/

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
    private Map<String, XHJZWarRoomData> roomDataMap = new ConcurrentHashMap<>();
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
    private Map<String, XHJZWarTeamData> battleTeamDataMap = new ConcurrentHashMap<>();
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
    private Map<String, Set<XHJZWarPlayerData>> teamIdToCommonderPlayer = new ConcurrentHashMap<>();
    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 战力排行数据
     ******************************************************************************************************************/
    /**
     * 排行数据缓存缓存前100
     */
    private XHJZWar.XWTeamRankResp.Builder rankResp = null;
    /**
     * 排名缓存，方便快速知道当前排名
     */
    private Map<String, Integer> teamRank = new ConcurrentHashMap<>();
    /**==============================================================================================================**/

    /** 单例模式实例 */
    private static XHJZWarService instance = null;

    /**
     * 获得单例
     * @return 单例
     */
    public static XHJZWarService getInstance(){
        return instance;
    }

    /**
     * 构造函数
     * @param xid
     */
    public XHJZWarService(HawkXID xid) {
        super(xid);
        instance = this;
    }

    /**
     * 初始化
     * @return 初始化结果
     */
    public boolean init() {
        try {
            stateData = new XHJZWarStateData();
            stateData.load();
            loadTeam();
            loadTeamPlayer();
            loadPlayer();
            doTeamRank(true);
            if(stateData.getState() != XHJZWarStateEnum.PEACE){
                loadSignUp();
            }
            if(stateData.getState() == XHJZWarStateEnum.MATCH_END
                    ||stateData.getState() == XHJZWarStateEnum.BATTLE
                    ||stateData.getState() == XHJZWarStateEnum.FINISH){
                loadBattleRoom();
            }
            addTickable(new HawkPeriodTickable(1000) {
                @Override
                public void onPeriodTick() {
                    try {
                        stateData.tick();
                    }catch (Exception e){
                        HawkException.catchException(e);
                    }
                    if(stateData.getState() == XHJZWarStateEnum.BATTLE){
                        for(XHJZWarBattleStateData battleStateData : battleStateDataMap.values()){
                            try {
                                battleStateData.tick();
                            }catch (Exception e){
                                HawkException.catchException(e);
                            }
                        }
                    }
                }
            });
            addTickable(new HawkPeriodTickable(TimeUnit.MINUTES.toMillis(10)) {
                @Override
                public void onPeriodTick() {
                    doTeamRank(false);
                }
            });
        }catch (Exception e){
            HawkException.catchException(e);
            return false;
        }
        return true;
    }

    /*******************************************************************************************************************
     * 状态机相关方法
     ******************************************************************************************************************/

    public XHJZWarStateEnum getState() {
        return stateData.getState();
    }

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
     * @return 执行结果
     */
    public boolean onOpen(int termId){
        //设置当前期数
        stateData.setTermId(termId);
        //清理数据
        clean();
        //进入下一状态
        stateData.toNext();
        return true;
    }

    /**
     * 报名阶段，发报名邮件
     */
    public void onSignup(){
        try {
            XHJZWarTimeCfg timeCfg = getTimeCfg();
            // 全服通知邮件
            SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder().setMailId(MailConst.MailId.XW_SIGNUP_OPEN).build(), timeCfg.getSignupTime(),
                    timeCfg.getMatchWaitTime());
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 匹配准备阶段，等待匹配数据上传
     * @return 执行结果
     */
    public boolean onMatchWait(){
        //上传本服报名数据
        updateSignUpInfo();
        return true;
    }

    /**
     * 活动状态机进入下一状态
     * @return 执行结果
     */
    public boolean onMatch(){
        int termId = getTermId();
        HawkLog.logPrintln("XHJZWarService onMatch start, termId:{}",termId);
        //抢锁
        String serverId = GsConfig.getInstance().getServerId();
        String matchKey = String.format(XHJZRedisKey.XHJZ_WAR_MATCH, termId);
        boolean getLock = RedisProxy.getInstance().getRedisSession().setNx(matchKey, serverId);
        if(!getLock){
            HawkLog.logPrintln("XHJZWarService onMatch get lock fail, termId:{}",termId);
            return true;
        }
        HawkLog.logPrintln("XHJZWarService onMatch real start, termId:{}",termId);
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        Map<String, Set<String>> serverIdToRoomIdMap = new HashMap<>();
        Map<String, String> roomStrMap = new HashMap<>();
        for(int i = 1; i <= constCfg.getWarCount(); i++){
            String signUpTimeKey = String.format(XHJZRedisKey.XHJZ_WAR_SIGNUP_TIME, termId, i);
            Set<String> teamIds = RedisProxy.getInstance().getRedisSession().sMembers(signUpTimeKey);
            if(teamIds == null || teamIds.isEmpty()){
                continue;
            }
            List<String> list = RedisProxy.getInstance().getRedisSession().hmGet(XHJZRedisKey.XHJZ_WAR_TEAM, teamIds.toArray(new String[teamIds.size()]));
            Map<Integer, List<XHJZWarTeamData>> teamDayMap = new HashMap<>();
            for(String json : list){
                XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(json);
                if(teamData == null){
                    continue;
                }
                if(!teamDayMap.containsKey(teamData.openDayW)){
                    teamDayMap.put(teamData.openDayW, new ArrayList<>());
                }
                teamDayMap.get(teamData.openDayW).add(teamData);
            }
            List<Integer> dayList = new ArrayList<>(teamDayMap.keySet());
            List<XHJZWarTeamData> noMatchList = new ArrayList<>();
            dayList.sort((o1, o2) -> o2 - o1);
            for(int day : dayList){
                List<XHJZWarTeamData> teamList = new ArrayList<>();
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
                    HawkTuple2<XHJZWarTeamData, XHJZWarTeamData> result = matchTeam(teamList);
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
                    XHJZWarTeamData teamData1 = result.first;
                    XHJZWarTeamData teamData2 = result.second;
                    if(teamData1 != null && teamData2 != null){
                        teamData1.oppTeamId = teamData2.id;
                        teamData2.oppTeamId = teamData1.id;
                        XHJZWarRoomData roomData = new XHJZWarRoomData(termId, i, teamData1, teamData2);
                        roomData.roomServerId = teamData1.serverId.compareTo(teamData2.serverId) < 0 ? teamData1.serverId : teamData2.serverId;
                        teamData1.update();
                        teamData2.update();
                        roomStrMap.put(roomData.id, roomData.serialize());
                        updateRoomIdToServer(serverIdToRoomIdMap, teamData1.serverId, roomData.id);
                        updateRoomIdToServer(serverIdToRoomIdMap, teamData2.serverId, roomData.id);
                        updateRoomIdToServer(serverIdToRoomIdMap, roomData.roomServerId, roomData.id);
                        //XHJZSeasonManager.getInstance().updateBattleInfo(teamData1, teamData2, roomData);
                    }
                }
                if(!teamList.isEmpty()){
                    noMatchList.addAll(teamList);
                }
            }
        }
        if(!roomStrMap.isEmpty()){
            RedisProxy.getInstance().getRedisSession().hmSet(String.format(XHJZRedisKey.XHJZ_WAR_ROOM, termId), roomStrMap, 0);
        }
        for(String toServerId : serverIdToRoomIdMap.keySet()){
            Set<String> roomIds = serverIdToRoomIdMap.get(toServerId);
            RedisProxy.getInstance().getRedisSession().sAdd(String.format(XHJZRedisKey.XHJZ_WAR_ROOM_SERVER, termId, toServerId), 0, roomIds.toArray(new String[roomIds.size()]));
        }
        return true;
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
    /**
     * 匹配结束阶段，加载匹配结果
     */
    public void onMatchEnd(){
        loadBattleRoom();
        sendMatchResult();
        sendMatchFailResult();
    }

    public void sendMatchFailResult(){
        if(XHJZSeasonManager.getInstance().getState() != CMWStateEnum.QUALIFIER){
            return;
        }
        for(String teamId : signUpMap.keySet()) {
            if(teamIdToRoomId.containsKey(teamId)){
                continue;
            }
            XHJZSeasonManager.getInstance().addScore(teamId, 0, true);
        }
    }

    public void sendMatchResult(){
        try {
            for(XHJZWarRoomData roomData : roomDataMap.values()){
                if(roomData == null){
                    continue;
                }
                XHJZWarTeamData teamDataA = teamDataMap.get(roomData.campA);
                if(teamDataA != null){
                    sendMatchResult(roomData.campA, roomData.timeIndex);
                }
                XHJZWarTeamData teamDataB = teamDataMap.get(roomData.campB);
                if(teamDataA != null){
                    sendMatchResult(roomData.campB, roomData.timeIndex);
                }
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    public void sendMatchResult(String teamId, int timeIndex){
        try {
            Set<String> playerIds = teamIdToPlayerIds.get(teamId);
            if(playerIds == null || playerIds.isEmpty()){
                return;
            }
            long startTime = HawkTime.getMillisecond();
            HawkTuple2<Long, Long> battleTime = getBattleTime(timeIndex);
            if(battleTime != null){
                startTime = battleTime.first;
            }
            String startData = HawkTime.formatTime(startTime);
            HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
                @Override
                public Object run() {
                    // 匹配成功邮件
                    for (String playerId : playerIds) {
                        SystemMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(playerId).setMailId(MailConst.MailId.XW_MATCH_RESULT).addContents(startData).build());
                    }
                    return null;
                }
            });
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 进入战斗阶段，加载战斗状态机
     */
    public void onBattle(){
        //初始化战斗状态机
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        for(int i = 1; i <= constCfg.getWarCount(); i++){
            battleStateDataMap.put(i, new XHJZWarBattleStateData(i));
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
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        HawkTuple2<Long, Long> battleTime = getBattleTime(timeIndex);
        for(String roomId : roomIds){
            XHJZWarRoomData roomData = roomDataMap.get(roomId);
            if(roomData == null || roomData.timeIndex != timeIndex || !roomData.roomServerId.equals(GsConfig.getInstance().getServerId())){
                continue;
            }
            XHJZWarTeamData teamDataA = battleTeamDataMap.get(roomData.campA);
            XHJZWarTeamData teamDataB = battleTeamDataMap.get(roomData.campB);
            XHJZExtraParam extParm = new XHJZExtraParam();
            extParm.setCampAGuild(teamDataA.guildId);
            extParm.setCampAGuildName(teamDataA.guildName);
            extParm.setCampAGuildTag(teamDataA.guildTag);
            extParm.setCampAServerId(teamDataA.serverId);
            extParm.setCampAguildFlag(teamDataA.guildFlag);
            extParm.setTeamAName(teamDataA.name);
            Set<XHJZWarPlayerData> playerDataSetA = teamIdToCommonderPlayer.get(roomData.campA);
            if(playerDataSetA != null){
                for(XHJZWarPlayerData playerData : playerDataSetA){
                    extParm.getCampACommonder().add(playerData);
                }
            }
            /**-----------------------------------------------------------------------------------------------------------------------------------------------*/
            /**-----------------------------------------------------------------------------------------------------------------------------------------------*/
            extParm.setCampBGuild(teamDataB.guildId);
            extParm.setCampBGuildName(teamDataB.guildName);
            extParm.setCampBGuildTag(teamDataB.guildTag);
            extParm.setCampBServerId(teamDataB.serverId);
            extParm.setCampBguildFlag(teamDataB.guildFlag);
            extParm.setTeamBName(teamDataB.name);
            Set<XHJZWarPlayerData> playerDataSetB = teamIdToCommonderPlayer.get(roomData.campB);
            if(playerDataSetB != null){
                for(XHJZWarPlayerData playerData : playerDataSetB){
                    extParm.getCampBCommonder().add(playerData);
                }
            }
            if(battleTime == null){
                XHJZRoomManager.getInstance().creatNewBattle(HawkTime.getMillisecond(), HawkTime.getMillisecond() + constCfg.getBattleTime(), roomData.id, extParm);
            }else {
                XHJZRoomManager.getInstance().creatNewBattle(battleTime.first, battleTime.second, roomData.id, extParm);
            }
            roomData.roomState = 1;
            roomData.update();
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

    public void clean(){
        try {
            cleanTeam();
            cleanPlayer();
            cleanSignUp();
            cleanBattle();
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    public void cleanTeam(){
        for(String teamId : teamDataMap.keySet()){
            try {
                XHJZWarTeamData teamData = teamDataMap.get(teamId);
                if(teamData!=null && (!"".equals(teamData.oppTeamId) || teamData.timeIndex != 0)){
                    teamData.oppTeamId = "";
                    teamData.timeIndex = 0;
                    teamData.score = 0;
                    teamData.update();
                }
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
    }

    public void cleanPlayer(){
        if(GsConfig.getInstance().isDebug() && getTermId() == 0){
            for(String playerId : playerDataMap.keySet()) {
                try {
                    XHJZWarPlayerData playerData = playerDataMap.get(playerId);
                    if(playerData != null && playerData.quitTIme != 0){
                        playerData.quitTIme = 0L;
                        playerData.update();
                    }
                }catch (Exception e){
                    HawkException.catchException(e);
                }

            }
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
            teamIdToCommonderPlayer.clear();
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }
    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 小队战力排行相关方法
     ******************************************************************************************************************/
    /**
     * 小队排行逻辑，每十分钟执行一次
     */
    public void doTeamRank(boolean isInit){
        if(!isInit && stateData.getState() != XHJZWarStateEnum.SIGNUP){
            return;
        }
        Map<String, String> teamInfo = new HashMap<>();
        List<XHJZWarTeamData> teamDataList = new ArrayList<>();
        for (XHJZWarTeamData teamData : teamDataMap.values()){
            teamData.refreshInfo();
            teamDataList.add(teamData);
            teamInfo.put(teamData.id, teamData.serialize());
        }
        if(teamDataList.isEmpty()){
            this.rankResp = XHJZWar.XWTeamRankResp.newBuilder();
            return;
        }
        RedisProxy.getInstance().getRedisSession().hmSet(XHJZRedisKey.XHJZ_WAR_TEAM, teamInfo, 0);
        Collections.sort(teamDataList, (o1, o2) -> {
            if(o1.battlePoint != o2.battlePoint){
                return o1.battlePoint > o2.battlePoint ? -1 : 1;
            }
            return 0;
        });
        XHJZWar.XWTeamRankResp.Builder resp = XHJZWar.XWTeamRankResp.newBuilder();
        Map<String, Integer> tmpTeamRank = new ConcurrentHashMap<>();
        int i = 1;
        for(XHJZWarTeamData teamData  : teamDataList){
            if(i == 101){
                break;
            }
            if(teamData!=null){
                XHJZWar.XWTeamInfo.Builder info = teamData.toPB();
                info.setRank(i);
                resp.addTeamInfos(info);
                tmpTeamRank.put(teamData.id, i);
                i++;
            }
        }
        this.rankResp = resp;
        this.teamRank = tmpTeamRank;
    }

    public void refreshPower(String playerId, long power){
        XHJZWarPlayerData playerData = playerDataMap.get(playerId);
        if(playerData == null){
            return;
        }
        playerData.battlePoint = power;
        playerData.update();
        if(HawkOSOperator.isEmptyString(playerData.teamId)){
            return;
        }
        XHJZWarTeamData teamData = teamDataMap.get(playerData.teamId);
        if(teamData == null){
            return;
        }
        teamData.refreshInfo();
        //teamData.update();
    }

    public void refreshName(String playerId, String name){
        XHJZWarPlayerData playerData = playerDataMap.get(playerId);
        if(playerData == null){
            return;
        }
        playerData.name = name;
        playerData.update();
    }
    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 发奖相关方法
     ******************************************************************************************************************/
    /**
     * 发奖
     * @param timeIndex 时间索引
     * @return 执行结果
     */
    public boolean sendAward(int timeIndex){
        int termId = getTermId();
        CMWStateEnum seasonState = XHJZSeasonManager.getInstance().getState();
        for(String teamId : signUpMap.keySet()) {
            int chooseTime = signUpMap.get(teamId);
            if(chooseTime != timeIndex){
                continue;
            }
            String teamAwardKey = String.format(XHJZRedisKey.XHJZ_WAR_TEAM_AWARD, termId);
            boolean teamGetLock = RedisProxy.getInstance().getRedisSession().hSetNx(teamAwardKey, teamId, String.valueOf(HawkTime.getMillisecond())) > 0;
            if(!teamGetLock){
                continue;
            }
            String roomId = teamIdToRoomId.get(teamId);
            if(HawkOSOperator.isEmptyString(roomId)){
                HawkLog.logPrintln("XHJZWarService sendAward roomId is null teamId:{}", teamId);
                continue;
            }
            XHJZWarRoomData roomData = roomDataMap.get(roomId);
            if(roomData == null){
                continue;
            }
            MailConst.MailId mailId = null;
            XHJZRewardCfg rewardCfg = null;
            long selfScore = 0;
            long oppScore = 0;
            if(teamId.equals(roomData.campA)){
                selfScore = roomData.scoreA;
                oppScore = roomData.scoreB;
            }else {
                selfScore = roomData.scoreB;
                oppScore = roomData.scoreA;
            }
            if(teamId.equals(roomData.winnerId)){
                mailId = MailConst.MailId.XW_BATTLE_WIN;
                rewardCfg = HawkConfigManager.getInstance().getConfigByKey(XHJZRewardCfg.class, 1);
            }else {
                if(oppScore == 0){
                    mailId = MailConst.MailId.XW_BATTLE_LOST_1;
                    rewardCfg = HawkConfigManager.getInstance().getConfigByKey(XHJZRewardCfg.class, 2);
                }else {
                    long lose = selfScore * 100 / oppScore;
                    if(lose >= 70){
                        mailId = MailConst.MailId.XW_BATTLE_LOST_1;
                        rewardCfg = HawkConfigManager.getInstance().getConfigByKey(XHJZRewardCfg.class, 2);
                    }else {
                        mailId = MailConst.MailId.XW_BATTLE_LOST_2;
                        rewardCfg = HawkConfigManager.getInstance().getConfigByKey(XHJZRewardCfg.class, 3);
                    }
                }
            }
            try {
                XHJZSeasonManager.getInstance().addScore(teamId, selfScore, teamId.equals(roomData.winnerId));
            } catch (Exception e) {
                HawkException.catchException(e);
            }
            try {
                XHJZWarTeamData teamDataA = battleTeamDataMap.get(roomData.campA);
                XHJZWarTeamData teamDataB = battleTeamDataMap.get(roomData.campB);
                if(teamDataA != null && teamDataB != null){
                    if(teamId.equals(roomData.campA)){
                        XHJZWarHistoryData historyDataA = new XHJZWarHistoryData(roomData.campA, roomData.winnerId, teamDataA, teamDataB);
                        historyDataA.update();
                    }else {
                        XHJZWarHistoryData historyDataB = new XHJZWarHistoryData(roomData.campB, roomData.winnerId, teamDataA, teamDataB);
                        historyDataB.update();
                    }
                }
            } catch (Exception e) {
                HawkException.catchException(e);
            }
            for(String playerId : teamIdToPlayerIds.get(teamId)){
                try {
                    String playerAwardKey = String.format(XHJZRedisKey.XHJZ_WAR_PLAYER_AWARD, termId);
                    boolean playerGetLock = RedisProxy.getInstance().getRedisSession().hSetNx(playerAwardKey, playerId, String.valueOf(HawkTime.getMillisecond())) > 0;
                    if(!playerGetLock){
                        continue;
                    }
                    SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                            .setPlayerId(playerId)
                            .setMailId(mailId)
                            .addContents(selfScore, oppScore)
                            .addRewards(rewardCfg.getRewardItems())
                            .setAwardStatus(Const.MailRewardStatus.NOT_GET).build());
                    ActivityManager.getInstance().postEvent(new XWScoreEvent(playerId, selfScore, seasonState != CMWStateEnum.CLOSE, 0));
                } catch (Exception e) {
                    HawkException.catchException(e);
                }
            }
            try {
                ActivityService.getInstance().dealMsg(GameConst.MsgId.ON_GUILD_BACK_DROP, new GuildBackDropInvoker(new ArrayList<>(teamIdToPlayerIds.get(teamId))));
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
        return true;
    }
    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 小队管理相关方法
     ******************************************************************************************************************/
    /**
     * 获得玩家数据
     * @param player 玩家数据
     * @return 玩家数据
     */
    public XHJZWarPlayerData makesurePlayerData(Player player){
        if(player == null || player.isCsPlayer()){
            return null;
        }
        if(!playerDataMap.containsKey(player.getId())){
            XHJZWarPlayerData data = new XHJZWarPlayerData(player);
            playerDataMap.putIfAbsent(player.getId(), data);
        }
        return playerDataMap.get(player.getId());

    }

    /**
     * 根据索引生成小队id
     * @param guildId 联盟id
     * @param index 小队索引
     * @return 小队id
     */
    public String getTeamId(String guildId, int index){
        return guildId + ":" + index;
    }

    /**
     * 本联盟所有成员数据
     * @param guildId
     * @return
     */
    public List<XHJZWarPlayerData> getMemberList(String guildId){
        List<XHJZWarPlayerData> list = new ArrayList<>();
        if(HawkOSOperator.isEmptyString(guildId)){
            return list;
        }
        Collection<String> memberIds =GuildService.getInstance().getGuildMembers(guildId);
        for (String memberId : memberIds) {
            Player player = GlobalData.getInstance().makesurePlayer(memberId);
            XHJZWarPlayerData data = makesurePlayerData(player);
            if(data == null){
                continue;
            }
            GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(memberId);
            data.refreshInfo(player, member);
            list.add(data);
        }
        return list;
    }
    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 联盟相关方法
     ******************************************************************************************************************/
    public boolean checkGuildOperation(String guildId, String playerId) {
        if(stateData.getState() == XHJZWarStateEnum.PEACE
                || stateData.getState() == XHJZWarStateEnum.SIGNUP
                || stateData.getState() == XHJZWarStateEnum.FINISH){
            return true;
        }
        if (!HawkOSOperator.isEmptyString(guildId)){
            XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
            for(int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
                String teamId = getTeamId(guildId, i);
                if(signUpMap.containsKey(teamId)){
                    return false;
                }
                if(isInSeason(teamId)){
                    return false;
                }
            }
        }
        if(!HawkOSOperator.isEmptyString(playerId)){
            String teamId = getSelfTeamId(playerId);
            if(signUpMap.containsKey(teamId)){
                return false;
            }
        }
        return true;
    }

    public void onGuildDismiss(String guildId) {
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        for(int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
            String teamId = getTeamId(guildId, i);
            XHJZWarTeamData teamData = teamDataMap.remove(teamId);
            if(teamData == null){
                continue;
            }
            Set<String> playerIds = teamIdToPlayerIds.remove(teamData.id);
            updateTeamDissmiss(teamData);
            updatePlayerDissmiss(playerIds);
            XHJZSeasonManager.getInstance().onTeamDismiss(teamId);
        }
    }

    public void onQuitGuild(Player player){
        XHJZWarPlayerData playerData = playerDataMap.get(player.getId());
        if(playerData != null){
            String oldTeamId = playerData.teamId;
            playerData.teamId = "";
            playerData.auth = XHJZWar.XWPlayerAuth.XW_NO_TEAM_VALUE;
            if(!HawkOSOperator.isEmptyString(oldTeamId)){
                teamIdToPlayerIds.get(oldTeamId).remove(playerData.id);
                String teamPlayerKey = String.format(XHJZRedisKey.XHJZ_WAR_TEAM_PLAYER, oldTeamId);
                RedisProxy.getInstance().getRedisSession().sRem(teamPlayerKey, playerData.id);
            }
            //updateNoTeam(playerData);
        }
    }

    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 兑换商店相关方法
     ******************************************************************************************************************/
    public List<XHJZWar.XWShopItem> geShopItems(Player player, boolean isAll){
        List<XHJZWar.XWShopItem> list = new ArrayList<>();
        Map<Integer, Integer> map = getShopData(player, isAll);
        for(int id : map.keySet()){
            if(id == XHJZ_SHOP_TERM){
                continue;
            }
            XHJZWar.XWShopItem.Builder item = XHJZWar.XWShopItem.newBuilder();
            item.setCfgId(id);
            item.setTimes(map.get(id));
            list.add(item.build());
        }
        return list;
    }

    public Map<Integer, Integer> getShopData(Player player, boolean isAll){
        CommanderEntity entity = player.getData().getCommanderEntity();
        if(!isAll){
            int shopTerm = entity.getShopDataMap().getOrDefault(XHJZ_SHOP_TERM, 0);
            int curTerm = getCurShopTerm();
            if(curTerm > shopTerm){
                entity.setShopDataMap(new HashMap<>());
                entity.getShopDataMap().put(XHJZ_SHOP_TERM, curTerm);
                entity.notifyUpdate();
            }
        }
        return entity.getShopDataMap();
    }

    public int getCurShopTerm(){
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        long now = HawkTime.getMillisecond();
        long dur = now - constCfg.getShopStartTime();
        int term = (int)(dur / constCfg.getShopRefreshTime()) + 1;
        return Math.max(1, term);
    }

    public long getShopRefreshTime(Player player){
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        CommanderEntity entity = player.getData().getCommanderEntity();
        int shopTerm = entity.getShopDataMap().getOrDefault(XHJZ_SHOP_TERM, 0);
        return constCfg.getShopStartTime() + constCfg.getShopRefreshTime() * shopTerm;
    }

    public void updataShopData(Player player){
        CommanderEntity entity = player.getData().getCommanderEntity();
        entity.notifyUpdate();
    }

    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 期数时间配置相关方法
     ******************************************************************************************************************/
    /**
     * 根据期数获得时间配置
     * @return 时间配置
     */
    public XHJZWarTimeCfg getTimeCfg(){
        int termId = stateData.getTermId();
        if(termId <=0){
            return null;
        }
        return HawkConfigManager.getInstance().getConfigByKey(XHJZWarTimeCfg.class, termId);
    }

    /**
     * 根据时间获得时间配置
     * @return 时间配置
     */
    public XHJZWarTimeCfg getCurTimeCfg(){
        long now = HawkTime.getMillisecond();
        ConfigIterator<XHJZWarTimeCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(XHJZWarTimeCfg.class);
        for (XHJZWarTimeCfg cfg : iterator){
            if(now >= cfg.getSignupTime() && now <= cfg.getEndTime()){
                return cfg;
            }
        }
        return null;
    }

    /**
     * 获得对应时间段的战斗时间
     * @param timeIndex 时间索引
     * @return 战斗开始结束时间戳
     */
    public HawkTuple2<Long, Long> getBattleTime(int timeIndex){
        //获得档期配置
        XHJZWarTimeCfg cfg = getTimeCfg();
        //如果配置为空直接返回
        if (cfg == null){
            return null;
        }
        //获得常量配置
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        long cfgTime = constCfg.getWarTime(timeIndex);
        //如果没有配置直接返回
        if(cfgTime < 0){
            return null;
        }
        //计算战场开始时间
        long starTime =  cfg.getBattleTimeZero() + cfgTime;
        //计算战场结束时间，加十分钟保证数据上传
        long endTime = starTime + constCfg.getBattleTime();
        return new HawkTuple2<>(starTime, endTime);
    }

    public long getCfgBattleTime(){
        //获得档期配置
        XHJZWarTimeCfg cfg = getTimeCfg();
        //如果配置为空直接返回当前时间
        if (cfg == null){
            return HawkTime.getMillisecond();
        }
        return cfg.getBattleTime();
    }

    public long getBattleStartTime(int termId, int timeIndex){
        XHJZWarTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(XHJZWarTimeCfg.class, termId);
        if (cfg == null){
            return HawkTime.getMillisecond();
        }
        //获得常量配置
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        long cfgTime = constCfg.getWarTime(timeIndex);
        //如果没有配置直接返回
        if(cfgTime < 0){
            return HawkTime.getMillisecond();
        }
        return cfg.getBattleTimeZero() + cfgTime;
    }

    public int getOpenDayW(){
        try {
            XHJZConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
            return cfg.serverMatchOpenDayWeight();
        } catch (Exception e) {
            HawkException.catchException(e);
        }
        return 0;
    }

    /**
     * 获得当前期数
     * @return 当前期数
     */
    public int getTermId(){
        return stateData.getTermId();
    }
    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 加载上传数据相关方法
     ******************************************************************************************************************/
    /**
     * 加载报名数据
     */
    public void loadSignUp(){
        try {
            String signUpServerKey = String.format(XHJZRedisKey.XHJZ_WAR_SIGNUP_SERVER, getTermId(), GsConfig.getInstance().getServerId());
            Map<String, String> tmp = RedisProxy.getInstance().getRedisSession().hGetAll(signUpServerKey);
            if(tmp == null || tmp.isEmpty()){
                HawkLog.logPrintln("XHJZWarService loadSignUp data is empty");
                return;
            }
            HawkLog.logPrintln("XHJZWarService loadSignUp start size:{}", tmp.size());
            Map<String, Integer> signUpMap = new ConcurrentHashMap<>();
            for(Map.Entry<String, String> entry : tmp.entrySet()){
                try {
                    HawkLog.logPrintln("XHJZWarService loadSignUp key:{}, value:{}", entry.getKey(), entry.getValue());
                    signUpMap.put(entry.getKey(), Integer.parseInt(entry.getValue()));
                }catch (Exception e){
                    HawkException.catchException(e);
                    HawkLog.logPrintln("XHJZWarService loadSignUp error key:{}, value:{}", entry.getKey(), entry.getValue());
                }
            }
            this.signUpMap = signUpMap;
            HawkLog.logPrintln("XHJZWarService loadSignUp end");
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 加载小队数据
     */
    public void loadTeam(){
        try {
            //String teamServerKey = String.format(XHJZRedisKey.XHJZ_WAR_TEAM_SERVER, GsConfig.getInstance().getServerId());
            //Set<String> teamIds = RedisProxy.getInstance().getRedisSession().sMembers(teamServerKey);
            Set<String> teamIds = new HashSet<>();
            List<String> guildList = GuildService.getInstance().getGuildIds();
            for(String guildId : guildList){
                XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
                for(int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
                    teamIds.add(getTeamId(guildId, i));
                }
            }
            if(teamIds.isEmpty()){
                HawkLog.logPrintln("XHJZWarService loadTeam teamIds is empty");
                return;
            }
            HawkLog.logPrintln("XHJZWarService loadTeam start size:{}", teamIds.size());
            List<String> list = RedisProxy.getInstance().getRedisSession().hmGet(XHJZRedisKey.XHJZ_WAR_TEAM, teamIds.toArray(new String[teamIds.size()]));
            for(String str : list) {
                if (HawkOSOperator.isEmptyString(str)) {
                    continue;
                }
                try {
                    HawkLog.logPrintln("XHJZWarService loadTeam teamData:{}", str);
                    XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(str);
                    if(teamData == null){
                        HawkLog.logPrintln("XHJZWarService loadTeam data is null teamData:{}", str);
                        continue;
                    }
                    teamDataMap.put(teamData.id, teamData);
                    teamIdToPlayerIds.put(teamData.id, new CopyOnWriteArraySet<>());
                }catch (Exception e){
                    HawkException.catchException(e);
                    HawkLog.logPrintln("XHJZWarService loadTeam error teamData:{}", str);
                }
            }
            HawkLog.logPrintln("XHJZWarService loadTeam end");
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 加载玩家数据
     */
    public void loadTeamPlayer(){
        try {
            for(String teamId : teamIdToPlayerIds.keySet()){
                String teamPlayerKey = String.format(XHJZRedisKey.XHJZ_WAR_TEAM_PLAYER, teamId);
                Set<String> playerIds = RedisProxy.getInstance().getRedisSession().sMembers(teamPlayerKey);
                if(playerIds == null || playerIds.isEmpty()){
                    HawkLog.logPrintln("XHJZWarService loadTeamPlayer playerIds is empty, teamId:{}", teamId);
                    continue;
                }
                HawkLog.logPrintln("XHJZWarService loadTeamPlayer start, teamId:{}, size:{}", teamId, playerIds.size());
                List<String> list = RedisProxy.getInstance().getRedisSession().hmGet(XHJZRedisKey.XHJZ_WAR_PLAYER, playerIds.toArray(new String[playerIds.size()]));
                for(String str : list) {
                    if (HawkOSOperator.isEmptyString(str)) {
                        continue;
                    }
                    try {
                        HawkLog.logPrintln("XHJZWarService loadTeamPlayer playerData:{}", str);
                        XHJZWarPlayerData playerData = XHJZWarPlayerData.unSerialize(str);
                        if(playerData == null){
                            HawkLog.logPrintln("XHJZWarService loadTeamPlayer data is null playerData:{}", str);
                            continue;
                        }
                        if(HawkOSOperator.isEmptyString(playerData.teamId)){
                            playerDataMap.put(playerData.id, playerData);
                        }else {
                            if(teamDataMap.containsKey(playerData.teamId)){
                                playerDataMap.put(playerData.id, playerData);
                                teamIdToPlayerIds.get(playerData.teamId).add(playerData.id);
                            }
                        }
                    } catch (Exception e) {
                        HawkException.catchException(e);
                        HawkLog.logPrintln("XHJZWarService loadTeamPlayer error playerData:{}", str);
                    }

                }
                HawkLog.logPrintln("XHJZWarService loadTeamPlayer end, teamId:{}", teamId);
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 加载玩家数据
     */
    public void loadPlayer(){
        try {
            String playerServerKey = String.format(XHJZRedisKey.XHJZ_WAR_PLAYER_SERVER, GsConfig.getInstance().getServerId());
            Set<String> oldPlayerIds = RedisProxy.getInstance().getRedisSession().sMembers(playerServerKey);
            if(oldPlayerIds == null || oldPlayerIds.isEmpty()){
                HawkLog.logPrintln("XHJZWarService loadPlayer oldPlayerIds is empty");
                return;
            }
            Set<String> playerIds = new HashSet<>();
            Set<String> removeIds = new HashSet<>();
            for(String playerId : oldPlayerIds){
                if(playerDataMap.containsKey(playerId)){
                    removeIds.add(playerId);
                }else {
                    playerIds.add(playerId);
                }
            }
            if(!removeIds.isEmpty()){
                HawkLog.logPrintln("XHJZWarService loadPlayer remove Ids size:{}", removeIds.size());
                RedisProxy.getInstance().getRedisSession().sRem(playerServerKey, removeIds.toArray(new String[removeIds.size()]));
            }
            if(playerIds.isEmpty()){
                HawkLog.logPrintln("XHJZWarService loadPlayer playerIds is empty");
                return;
            }
            HawkLog.logPrintln("XHJZWarService loadPlayer start size:{}", playerIds.size());
            Map<String, Set<String>> updateTeamPlayers = new HashMap<>();
            List<String> list = RedisProxy.getInstance().getRedisSession().hmGet(XHJZRedisKey.XHJZ_WAR_PLAYER, playerIds.toArray(new String[playerIds.size()]));
            for(String str : list) {
                if (HawkOSOperator.isEmptyString(str)) {
                    continue;
                }
                try {
                    HawkLog.logPrintln("XHJZWarService loadPlayer playerData:{}", str);
                    XHJZWarPlayerData playerData = XHJZWarPlayerData.unSerialize(str);
                    if(playerData == null){
                        HawkLog.logPrintln("XHJZWarService loadPlayer data is null playerData:{}", str);
                        continue;
                    }
                    if(HawkOSOperator.isEmptyString(playerData.teamId)){
                        playerDataMap.put(playerData.id, playerData);
                    }else {
                        if(teamDataMap.containsKey(playerData.teamId)){
                            playerDataMap.put(playerData.id, playerData);
                            teamIdToPlayerIds.get(playerData.teamId).add(playerData.id);
                            if(!updateTeamPlayers.containsKey(playerData.teamId)){
                                updateTeamPlayers.put(playerData.teamId, new HashSet<>());
                            }
                            updateTeamPlayers.get(playerData.teamId).add(playerData.id);
                        }
                    }
                } catch (Exception e) {
                    HawkException.catchException(e);
                    HawkLog.logPrintln("XHJZWarService loadPlayer error playerData:{}", str);
                }

            }
            for(String teamId : updateTeamPlayers.keySet()){
                String teamPlayerKey = String.format(XHJZRedisKey.XHJZ_WAR_TEAM_PLAYER, teamId);
                Set<String> teamPlayerIds = updateTeamPlayers.get(teamId);
                RedisProxy.getInstance().getRedisSession().sAdd(teamPlayerKey, 0, teamPlayerIds.toArray(new String[teamPlayerIds.size()]));
            }
            HawkLog.logPrintln("XHJZWarService loadPlayer end");
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 小队创建更新数据
     * @param teamData 小队数据
     */
    public void updateTeamCrate(XHJZWarTeamData teamData){
        teamData.update();
        //把小队数据和本服数关联
        String teamServerKey = String.format(XHJZRedisKey.XHJZ_WAR_TEAM_SERVER, GsConfig.getInstance().getServerId());
        RedisProxy.getInstance().getRedisSession().sAdd(teamServerKey, 0, teamData.id);
    }

    /**
     * 小队报名更新数据
     * @param teamData 小队数据
     */
    public void updateTeamSignUp(XHJZWarTeamData teamData){
        teamData.update();
        //更新本服和全服当期报名数据
        String signUpTimeKey = String.format(XHJZRedisKey.XHJZ_WAR_SIGNUP_TIME, getTermId(), teamData.timeIndex);
        String signUpServerKey = String.format(XHJZRedisKey.XHJZ_WAR_SIGNUP_SERVER, getTermId(), GsConfig.getInstance().getServerId());
        RedisProxy.getInstance().getRedisSession().sAdd(signUpTimeKey, 0, teamData.id);
        RedisProxy.getInstance().getRedisSession().hSet(signUpServerKey, teamData.id, String.valueOf(teamData.timeIndex));
    }

    /**
     * 小队解散更新数据
     * @param teamData 小队数据
     */
    public void updateTeamDissmiss(XHJZWarTeamData teamData){
        signUpMap.remove(teamData.id);
        teamData.remove();
        //小队解散后解除小队和本服关联，下次起服不加载
        String teamServerKey = String.format(XHJZRedisKey.XHJZ_WAR_TEAM_SERVER, GsConfig.getInstance().getServerId());
        RedisProxy.getInstance().getRedisSession().sRem(teamServerKey, teamData.id);
        String signUpTimeKey = String.format(XHJZRedisKey.XHJZ_WAR_SIGNUP_TIME, getTermId(), teamData.timeIndex);
        String signUpServerKey = String.format(XHJZRedisKey.XHJZ_WAR_SIGNUP_SERVER, getTermId(), GsConfig.getInstance().getServerId());
        RedisProxy.getInstance().getRedisSession().sRem(signUpTimeKey, teamData.id);
        RedisProxy.getInstance().getRedisSession().hDel(signUpServerKey, teamData.id);
        String teamPlayerKey = String.format(XHJZRedisKey.XHJZ_WAR_TEAM_PLAYER, teamData.id);
        RedisProxy.getInstance().getRedisSession().del(teamPlayerKey);
    }

    /**
     * 处理被解散的小队的成员数据
     * @param playerIds
     */
    public void updatePlayerDissmiss(Set<String> playerIds){
        try {
            //如果为空直接返回
            if(playerIds == null || playerIds.isEmpty()){
                HawkLog.logPrintln("XHJZWarService updatePlayerDissmiss playerIds is empty");
                return;
            }
            HawkLog.logPrintln("XHJZWarService updatePlayerDissmiss start，size:{}", playerIds.size());
            for(String playerId : playerIds){
                try {
                    XHJZWarPlayerData playerData = playerDataMap.get(playerId);
                    if(playerData == null){
                        HawkLog.logPrintln("XHJZWarService updatePlayerDissmiss playerData is null, playerId:{}",playerId);
                        continue;
                    }
                    playerData.teamId = "";
                    playerData.auth = XHJZWar.XWPlayerAuth.XW_NO_TEAM_VALUE;
                    playerData.update();
                }catch (Exception e){
                    HawkException.catchException(e);
                    HawkLog.logPrintln("XHJZWarService updatePlayerDissmiss player error, playerId:{}",playerId);
                }
            }
            //玩家和本服关联的key，删除，下次加载的时候不加载不在小队内的玩家
            String playerServerKey = String.format(XHJZRedisKey.XHJZ_WAR_PLAYER_SERVER, GsConfig.getInstance().getServerId());
            RedisProxy.getInstance().getRedisSession().sRem(playerServerKey, playerIds.toArray(new String[playerIds.size()]));
            HawkLog.logPrintln("XHJZWarService updatePlayerDissmiss end");
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 更新玩家数据
     * @param playerData 玩家数据
     */
    public void updatePlayerDate(XHJZWarPlayerData playerData){
        playerData.update();
        String playerServerKey = String.format(XHJZRedisKey.XHJZ_WAR_PLAYER_SERVER, GsConfig.getInstance().getServerId());
        RedisProxy.getInstance().getRedisSession().sAdd(playerServerKey, 0, playerData.id);
    }

    public void updateNoTeam(XHJZWarPlayerData playerData){
        playerData.update();
        String playerServerKey = String.format(XHJZRedisKey.XHJZ_WAR_PLAYER_SERVER, GsConfig.getInstance().getServerId());
        RedisProxy.getInstance().getRedisSession().sRem(playerServerKey, playerData.id);
    }

    /**
     * 更新报名信息
     */
    public void updateSignUpInfo(){
        //记录参赛的联盟id
        Set<String> guildIds = new HashSet<>();
        for(String teamId : signUpMap.keySet()) {
            try {
                updateSignUpTeam(teamId);
                //上传本战队玩家
                updateSignUpPlayer(teamId);
                //记录联盟id
                guildIds.add(teamDataMap.get(teamId).guildId);

            } catch (Exception e) {
                HawkException.catchException(e);
                HawkLog.logPrintln("XHJZWarService updateSignUpInfo team error, teamId:{}",teamId);
            }
        }
        //上传联盟数据
        updateSignUpGuild(guildIds);
    }

    public void updateSignUpTeam(String teamId){
        try {
            int openDayW = getOpenDayW();
            PBCommonMatch.PBCMWServerType serverType = XHJZSeasonManager.getInstance().getServerType();
            boolean isNew = (serverType == PBCommonMatch.PBCMWServerType.NEW_SERVER);
            XHJZWarTeamData teamData = teamDataMap.get(teamId);
            if(teamData == null){
                return;
            }
            Set<String> playerIds = teamIdToPlayerIds.get(teamId);
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
            XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
            List<XHJZWarHistoryData> historyDataList = XHJZWarHistoryData.load(teamId, constCfg.getRecordPointCount());
            if(historyDataList == null || historyDataList.isEmpty()){
                teamData.matchPower = matchPower;
            }else {
                float param = 0f;
                for(XHJZWarHistoryData historyData : historyDataList){
                    if(teamId.equals(historyData.winnerId)){
                        param += constCfg.getRecordPointParam();
                    }else {
                        param -= constCfg.getRecordPointParam();
                    }
                }
                if(param < constCfg.getRecordPointLimitParam().first){
                    param = constCfg.getRecordPointLimitParam().first;
                }
                if(param > constCfg.getRecordPointLimitParam().second){
                    param = constCfg.getRecordPointLimitParam().second;
                }
                teamData.matchPower = (long)(matchPower * (1 + param));
            }
            CMWData data = XHJZSeasonManager.getInstance().loadData(teamId);
            if(data != null){
                teamData.seasonScore = data.score;
            }
            teamData.serverId = GsConfig.getInstance().getServerId();
            teamData.isNew = isNew;
            teamData.openDayW = openDayW;
            teamData.update();
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 更新报名的玩家信息
     * @param teamId 小队id
     */
    public void updateSignUpPlayer(String teamId){
        try {
            HawkLog.logPrintln("XHJZWarService updateSignUpPlayer start teamId:{}",teamId);
            //获得期数
            int termId = getTermId();
            //报名玩家的key
            String signUpPlayerKey = String.format(XHJZRedisKey.XHJZ_WAR_SIGNUP_PLAYER, termId, teamId);
            //方便测试，如果是debug环境且期数为0，清理一下redis数据，因为第0期会被重复开
            if(GsConfig.getInstance().isDebug() && termId == 0){
                HawkLog.logPrintln("XHJZWarService updateSignUpPlayer debug clean teamId:{}",teamId);
                RedisProxy.getInstance().getRedisSession().del(signUpPlayerKey);
            }
            //玩家权限map
            Map<String, String> playerAuth = new HashMap<>();
            //本小队本期参赛人员
            Set<String> playerIds = teamIdToPlayerIds.get(teamId);
            //如果为空直接返回
            if(playerIds == null || playerIds.isEmpty()){
                HawkLog.logPrintln("XHJZWarService updateSignUpPlayer playerIds is empty, teamId:{}",teamId);
                return;
            }
            for(String playerId : teamIdToPlayerIds.get(teamId)){
                try {
                    XHJZWarPlayerData playerData = playerDataMap.get(playerId);
                    if(playerData == null){
                        HawkLog.logPrintln("XHJZWarService updateSignUpPlayer playerData is null, playerId:{}",playerId);
                    }
                    playerAuth.put(playerData.id, String.valueOf(playerData.auth));
                } catch (Exception e) {
                    HawkException.catchException(e);
                    HawkLog.logPrintln("XHJZWarService updateSignUpPlayer player error, playerId:{}",playerId);
                }
            }
            //如果数据为空直接返回
            if(playerAuth.isEmpty()){
                return;
            }
            //把数据存入redis
            RedisProxy.getInstance().getRedisSession().hmSet(signUpPlayerKey, playerAuth, 0);
            HawkLog.logPrintln("XHJZWarService updateSignUpPlayer end teamId:{}",teamId);
        } catch (Exception e) {
            HawkException.catchException(e);
            HawkLog.logPrintln("XHJZWarService updateSignUpPlayer team error, teamId:{}",teamId);
        }
    }

    /**
     * 更新报名的联盟信息
     * @param guildIds 联盟id集合
     */
    public void updateSignUpGuild(Set<String> guildIds){
        try {
            //如果为空直接返回
            if(guildIds == null || guildIds.isEmpty()){
                HawkLog.logPrintln("XHJZWarService updateSignUpGuild guildIds is empty");
                return;
            }
            HawkLog.logPrintln("XHJZWarService updateSignUpGuild start, size:{}", guildIds.size());
            Map<String, String> guildDataMap = new HashMap<>();
            for(String guildId : guildIds){
                try {
                    //生成联盟数据
                    GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(guildId);
                    guildDataMap.put(guildId, new XHJZWarGuildData(guildInfoObject).serialize());
                } catch (Exception e) {
                    HawkException.catchException(e);
                    HawkLog.logPrintln("XHJZWarService updateSignUpGuild error, guildId:{}",guildId);
                }
            }
            //通过期数存联盟数据
            int termId = getTermId();
            String guildKey = String.format(XHJZRedisKey.XHJZ_WAR_GUILD, termId);
            RedisProxy.getInstance().getRedisSession().hmSet(guildKey, guildDataMap, 0);
            HawkLog.logPrintln("XHJZWarService updateSignUpGuild end");
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 加载参战联盟相关信息
     * @param guildId 联盟id
     * @return 联盟信息
     */
    public XHJZWarGuildData loadGuildData(String guildId){
        int termId = getTermId();
        String guildKey = String.format(XHJZRedisKey.XHJZ_WAR_GUILD, termId);
        String guildStr = RedisProxy.getInstance().getRedisSession().hGet(guildKey, guildId);
        return XHJZWarGuildData.unSerialize(guildStr);
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
     * 匹配结束后加载战场数据
     */
    public void loadBattleRoom(){
        try {
            //获得期数
            int termId = getTermId();
            //房间id的key，只存了和本服相关的房间的id
            String roomServerKey = String.format(XHJZRedisKey.XHJZ_WAR_ROOM_SERVER, termId, GsConfig.getInstance().getServerId());
            //本服参与或开在本服战场的id
            Set<String> roomIds = RedisProxy.getInstance().getRedisSession().sMembers(roomServerKey);
            //如果为空直接返回
            if(roomIds == null || roomIds.isEmpty()){
                HawkLog.logPrintln("XHJZWarService loadBattleRoom roomIds is empty");
                return;
            }
            //房间加载开始
            HawkLog.logPrintln("XHJZWarService loadBattleRoom start termId:{}, size:{}", termId, roomIds.size());
            //加载房间redis数据
            List<String> roomList = RedisProxy.getInstance().getRedisSession().hmGet(String.format(XHJZRedisKey.XHJZ_WAR_ROOM, termId), roomIds.toArray(new String[roomIds.size()]));
            //如果为空就返回，一般不会为空上面id已经校验一遍了，以防万一再检查一次
            if(roomList == null || roomList.isEmpty()){
                HawkLog.logPrintln("XHJZWarService loadBattleRoom roomList is empty");
                return;
            }
            //参与相关战场的小队id
            Set<String> teamIds = new HashSet<>();
            //遍历房间数据
            for(String roomStr : roomList){
                try {
                    HawkLog.logPrintln("XHJZWarService loadBattleRoom roomData:{}",roomStr);
                    //解析房间数据
                    XHJZWarRoomData roomData = XHJZWarRoomData.unSerialize(roomStr);
                    //数据为空的话跳过
                    if(roomData == null){
                        HawkLog.logPrintln("XHJZWarService loadBattleRoom data is null roomData:{}",roomStr);
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
                    if(!timeIndexToRoomIds.containsKey(roomData.timeIndex)){
                        timeIndexToRoomIds.put(roomData.timeIndex, new CopyOnWriteArraySet<>());
                    }
                    timeIndexToRoomIds.get(roomData.timeIndex).add(roomData.id);
                }catch (Exception e){
                    HawkException.catchException(e);
                    HawkLog.logPrintln("XHJZWarService loadBattleRoom error roomData:{}",roomStr);
                }
            }
            //加载参与的小队数据
            loadBattleTeam(teamIds);
            //加载参与的玩家数据
            loadBattlePlayer(teamIds);
            //房间加载结束
            HawkLog.logPrintln("XHJZWarService loadBattleRoom end");
        }catch (Exception e){
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
                HawkLog.logPrintln("XHJZWarService loadBattleTeam teamIds is empty");
                return;
            }
            //本服id，用于判断小队是否是本服的
            String serverId = GsConfig.getInstance().getServerId();
            //加载相关小队数据
            List<String> list = RedisProxy.getInstance().getRedisSession().hmGet(XHJZRedisKey.XHJZ_WAR_TEAM, teamIds.toArray(new String[teamIds.size()]));
            //如果为空就返回，一般不会为空上面id已经校验一遍了，以防万一再检查一次
            if(list == null || list.isEmpty()){
                HawkLog.logPrintln("XHJZWarService loadBattleTeam teamList is empty");
                return;
            }
            //遍历小队数据
            for(String str : list) {
                try {
                    HawkLog.logPrintln("XHJZWarService loadBattleTeam teamData:{}", str);
                    //解析小队数据
                    XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(str);
                    //数据为空的话跳过
                    if(teamData == null){
                        HawkLog.logPrintln("XHJZWarService loadBattleTeam data is null teamData:{}", str);
                        continue;
                    }
                    //加入参战小队数据缓存中
                    battleTeamDataMap.put(teamData.id, teamData);
                    //如果小队是本服小队，更新本服小队数据
                    if(serverId.equals(teamData.serverId)){
                        teamDataMap.put(teamData.id, teamData);
                    }
                }catch (Exception e){
                    HawkException.catchException(e);
                    HawkLog.logPrintln("XHJZWarService loadBattleTeam error roomData:{}",str);
                }
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 参战成员数据
     * @param teamIds 参战队伍id
     */
    public void loadBattlePlayer(Set<String> teamIds){
        try {
            //如果为空直接返回
            if(teamIds == null || teamIds.isEmpty()){
                HawkLog.logPrintln("XHJZWarService loadBattlePlayer teamIds is empty");
                return;
            }
            //获得期数
            int termId = getTermId();
            for(String teamId : teamIds){
                try {
                    HawkLog.logPrintln("XHJZWarService loadBattlePlayer team start teamId:{}", teamId);
                    //获得当期期参与人的信息
                    String signUpPlayerKey = String.format(XHJZRedisKey.XHJZ_WAR_SIGNUP_PLAYER, termId, teamId);
                    Map<String, String> playerAuth = RedisProxy.getInstance().getRedisSession().hGetAll(signUpPlayerKey);
                    //如果为空直接跳过
                    if(playerAuth == null || playerAuth.isEmpty()){
                        HawkLog.logPrintln("XHJZWarService loadBattlePlayer playerAuth is empty, teamId:{}",teamId);
                        continue;
                    }
                    //遍历玩家
                    for(String playerId : playerAuth.keySet()){
                        try {
                            HawkLog.logPrintln("XHJZWarService loadBattlePlayer playerId:{},auth:{}", playerId, playerAuth.get(playerId));
                            int auth = Integer.parseInt(playerAuth.get(playerId));
                            //关联玩家和小队
                            playerIdToTeamId.put(playerId, teamId);
                            //关联玩家和房间
                            playerIdToRoomId.put(playerId, teamIdToRoomId.get(teamId));
                            //关联玩家和权限
                            playerIdToAuth.put(playerId, auth);
                            if(auth == XHJZWar.XWPlayerAuth.XW_COMMAND_VALUE){
                                addCommonder(teamId, playerId);
                            }
                        } catch (Exception e) {
                            HawkException.catchException(e);
                            HawkLog.logPrintln("XHJZWarService loadBattlePlayer player error playerId:{}", playerId);
                        }
                    }
                    HawkLog.logPrintln("XHJZWarService loadBattlePlayer team end teamId:{}", teamId);
                } catch (Exception e) {
                    HawkException.catchException(e);
                    HawkLog.logPrintln("XHJZWarService loadBattlePlayer team error teamId:{}", teamId);
                }
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    public void addCommonder(String teamId, String playerId){
        try {
            if(!teamIdToCommonderPlayer.containsKey(teamId)){
                teamIdToCommonderPlayer.put(teamId, new CopyOnWriteArraySet<>());
            }
            XHJZWarPlayerData playerData = XHJZWarPlayerData.load(playerId);
            if(playerData != null){
                teamIdToCommonderPlayer.get(teamId).add(playerData);
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
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
                HawkLog.logPrintln("XHJZWarService loadBattleRoomResult roomIds is empty, timeIndex:{}",timeIndex);
                return;
            }
            HawkLog.logPrintln("XHJZWarService loadBattleRoomResult start, timeIndex:{}",timeIndex);
            //加载Redis房间数据
            List<String> roomList = RedisProxy.getInstance().getRedisSession().hmGet(String.format(XHJZRedisKey.XHJZ_WAR_ROOM, termId), roomIds.toArray(new String[roomIds.size()]));
            //如果为空就返回，一般不会为空上面id已经校验一遍了，以防万一再检查一次
            if(roomList == null || roomList.isEmpty()){
                HawkLog.logPrintln("XHJZWarService loadBattleRoomResult roomList is empty");
                return;
            }
            HawkLog.logPrintln("XHJZWarService loadBattleRoomResult size:{}", roomList.size());
            //遍历房间数据
            for(String roomStr : roomList){
                try {
                    HawkLog.logPrintln("XHJZWarService loadBattleRoomResult roomData:{}", roomStr);
                    //解析房间数据
                    XHJZWarRoomData roomData = XHJZWarRoomData.unSerialize(roomStr);
                    //如果为空直接跳过
                    if(roomData == null){
                        HawkLog.logPrintln("XHJZWarService loadBattleRoomResult data is null roomData:{}", roomStr);
                        continue;
                    }
                    //加入房间数据缓存
                    roomDataMap.put(roomData.id, roomData);
                    updateRoomScore(roomData);
                } catch (Exception e) {
                    HawkException.catchException(e);
                    HawkLog.logPrintln("XHJZWarService loadBattleRoomResult error roomData:{}", roomStr);
                }
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    public void updateRoomScore(XHJZWarRoomData roomData){
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
        XHJZWarTeamData teamData = teamDataMap.get(teamId);
        if(teamData != null){
            teamData.score = score;
            teamData.update();
        }
        XHJZWarTeamData battleTeamData = battleTeamDataMap.get(teamId);
        if(battleTeamData != null){
            battleTeamData.score = score;
            battleTeamData.update();
        }
    }

    public void updateTeamEnterNum(String teamId){
        try {
            if(HawkOSOperator.isEmptyString(teamId)){
                return;
            }
            String teamEnterKey = String.format(XHJZRedisKey.XHJZ_WAR_TEAM_ENTER, getTermId(), teamId);
            RedisProxy.getInstance().getRedisSession().increase(teamEnterKey);
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    public int getTeamEnterNum(String teamId){
        try {
            String teamEnterKey = String.format(XHJZRedisKey.XHJZ_WAR_TEAM_ENTER, getTermId(), teamId);
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
    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 战场相关方法
     ******************************************************************************************************************/
    /**
     * 进入战场前置检查
     * @param player
     * @return
     */
    public boolean checkEnter(Player player) {
        try {
            if(player.isCsPlayer()){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_WAR_IN_CROSS, 0);
                return false;
            }
            String guildId = player.getGuildId();
            if (HawkOSOperator.isEmptyString(guildId)) {
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.Error.GUILD_NO_JOIN,0);
                return false;
            }
            XHJZWarPlayerData playerData = playerDataMap.get(player.getId());
            if(playerData == null){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_NOT_IN_THIS_WAR,0);
                return false;
            }
            XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
            int teamEnterNum = getTeamEnterNum(playerData.teamId);
            if(teamEnterNum >= constCfg.getPlayerMax()){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_TEAM_ENTER_LIMIT,0);
                return false;
            }
            XHJZWarRoomData roomData = roomDataMap.get(teamIdToRoomId.get(playerData.teamId));
            if(roomData == null){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_HAS_NO_MATCH_INFO,0);
                return false;
            }
            if(roomData.timeIndex <= 0 ||stateData.getState() != XHJZWarStateEnum.BATTLE){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_WAR_NOT_IN_BATTLE_TIME, 0);
                return false;
            }
            if(battleStateDataMap.get(roomData.timeIndex).getState() != XHJZWarStateEnum.BATTLE_OPEN){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_WAR_NOT_IN_BATTLE_TIME, 0);
                return false;
            }
            long now = HawkTime.getMillisecond();
            HawkTuple2<Long, Long> battleTime = getBattleTime(roomData.timeIndex);
            if(battleTime != null && (now < battleTime.first || now > battleTime.second - TimeUnit.MINUTES.toMillis(5))){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_BATTLE_NEAR_OVER, 0);
                return false;
            }
            if(battleTime != null && playerData.auth == XHJZWar.XWPlayerAuth.XW_CANDIDATE_VALUE && now < battleTime.first + constCfg.getPreparationTime()){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_CANDIDATE_NOT_IN_TIME, 0);
                return false;
            }
            XHJZWarPlayerData redisData = XHJZWarPlayerData.load(player.getId());
            if(battleTime != null && redisData.quitTIme > battleTime.first && redisData.quitTIme < battleTime.second){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_HAS_JOINED_WAR, 0);
                return false;
            }
        }catch (Exception e){
            HawkException.catchException(e);
            return false;
        }
        return true;
    }

    /**
     * 获得战场数据
     * @param player 玩家
     * @return 战场数据
     */
    public XHJZWarRoomData getRoomData(Player player){
        return roomDataMap.get(playerIdToRoomId.get(player.getId()));
    }

    /**
     * 是否是指挥官
     * @param playerId 玩家id
     * @return 是否是指挥官
     */
    public boolean isCommonder(String playerId){
        try {
            return playerIdToAuth.get(playerId) == XHJZWar.XWPlayerAuth.XW_COMMAND_VALUE;
        }catch (Exception e){
            HawkException.catchException(e);
            return false;
        }
    }

    public boolean isLocalTeam(String teamId){
        return teamDataMap.containsKey(teamId);
    }

    public XHJZWarTeamData getTeamData(String teamId){
        return teamDataMap.get(teamId);
    }

    public boolean isInSeason(String teamId){
        return XHJZSeasonManager.getInstance().isInSeason(teamId);
    }

    public Set<String> getTeamPlayerIds(String teamId){
        return teamIdToPlayerIds.get(teamId);
    }

    /**
     * 进入战场
     * @param player 玩家
     * @return 执行结果
     */
    public boolean joinRoom(Player player) {
        String roomId = playerIdToRoomId.get(player.getId());
        XHJZWarRoomData roomData = roomDataMap.get(roomId);
        if(roomData == null){
            return false;
        }
        if(!XHJZRoomManager.getInstance().hasGame(roomData.id)){
            return false;
        }
        if(GsConfig.getInstance().isDebug() && getTermId() == 0){
            XHJZWarPlayerData playerData = XHJZWarPlayerData.load(player.getId());
            if(playerData == null){
                return false;
            }
            HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.XHJZAOGUAN_ROOM, roomData.id);
            HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.XHJZAOGUAN_ROOM).queryObject(roomXid);
            XHJZBattleRoom room = null;
            if (roomObj != null) {
                room = (XHJZBattleRoom) roomObj.getImpl();
            }
            if(room == null){
                return false;
            }
            if(playerData.quitTIme > room.getStartTime() && playerData.quitTIme < room.getOverTime()){
                return false;
            }
            long now = HawkTime.getMillisecond();
            if(playerData.auth == XHJZWar.XWPlayerAuth.XW_CANDIDATE_VALUE && now < (room.getStartTime() + TimeUnit.MINUTES.toMillis(5))){
                return false;
            }
        }
        updateTeamEnterNum(playerIdToTeamId.get(player.getId()));
        XHJZRoomManager.getInstance().joinGame(roomId, player);
        try {
            String teamId = playerIdToTeamId.get(player.getId());
            Map<String, Object> param = new HashMap<>();
            param.put("teamId", teamId);
            LogUtil.logActivityCommon(player, LogConst.LogInfoType.xhjz_join, param);
        }catch (Exception e){
            HawkException.catchException(e);
        }
        return true;
    }

    /**
     * 退出战场
     * @param player 玩家
     * @param isMidwayQuit 是否中途退出
     * @return 执行结果
     */
    public boolean quitRoom(Player player, boolean isMidwayQuit){
        XHJZWarPlayerData playerData = XHJZWarPlayerData.load(player.getId());
        playerData.quitTIme = HawkTime.getMillisecond();
        playerData.update();
        try {
            String teamId = playerIdToTeamId.get(player.getId());
            Map<String, Object> param = new HashMap<>();
            param.put("teamId", teamId);
            LogUtil.logActivityCommon(player, LogConst.LogInfoType.xhjz_quit, param);
        }catch (Exception e){
            HawkException.catchException(e);
        }
        return true;
    }

    /**
     * 战场结算
     * @param msg 战场结算消息
     */
    @MessageHandler
    private void onBattleFinish(XHJZBilingInformationMsg msg){
        String roomId = msg.getRoomId();
        XHJZWarRoomData roomData = roomDataMap.get(roomId);
        if(roomData == null){
            return;
        }
        if(!HawkOSOperator.isEmptyString(roomData.winnerId)){
            return;
        }
        long scoreA = msg.getLastSyncpb().getGuildInfo(0).getHonor();
        long scoreB = msg.getLastSyncpb().getGuildInfo(1).getHonor();
        roomData.scoreA = scoreA;
        roomData.scoreB = scoreB;
        XHJZRoomManager.XHJZ_CAMP winCamp = XHJZRoomManager.XHJZ_CAMP.valueOf(msg.getLastSyncpb().getWinCamp());
        switch (winCamp){
            case A:{
                roomData.winnerId = roomData.campA;
            }
            break;
            case B:{
                roomData.winnerId = roomData.campB;
            }
            break;
        }
        roomData.roomState = 2;
        roomData.update();
        try {
            XHJZWarTeamData teamDataA = battleTeamDataMap.get(roomData.campA);
            XHJZWarTeamData teamDataB = battleTeamDataMap.get(roomData.campB);
            int teamEnterNumA = getTeamEnterNum(roomData.campA);
            int teamEnterNumB = getTeamEnterNum(roomData.campB);
            Map<String, Object> param = new HashMap<>();
            param.put("time", roomData.timeIndex);
            param.put("teamA", teamDataA.id);
            param.put("powerA", teamDataA.matchPower);
            param.put("countA", teamDataA.memberCnt);
            param.put("numA", teamEnterNumA);
            param.put("scoreA", scoreA);
            param.put("teamB", teamDataB.id);
            param.put("powerB", teamDataB.matchPower);
            param.put("countB", teamDataB.memberCnt);
            param.put("numB", teamEnterNumB);
            param.put("scoreB", scoreB);
            LogUtil.logActivityCommon(LogConst.LogInfoType.xhjz_finish, param);
            XHJZSeasonManager.getInstance().updateBattleInfo(teamDataA, teamDataB, roomData);
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }
    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 同步客户端相关方法
     ******************************************************************************************************************/
    public void syncAllPLayer(){
        for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
            try {
                syncPageInfo(player, true);
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
    }

    /**
     * 同步页面信息
     * @param player 玩家
     * @param isAll 是否为全局推送
     */
    public void syncPageInfo(Player player, boolean isAll){
        XHJZWar.XWPageInfo.Builder pageInfo = XHJZWar.XWPageInfo.newBuilder();
        pageInfo.setStateInfo(getStateInfo(player));
        pageInfo.addAllTeamInfos(getTeamInfos(player));
        pageInfo.setSelfTeamId(getSelfTeamId(player));
        pageInfo.addAllShopItems(geShopItems(player, isAll));
        pageInfo.setShopRefreshTime(getShopRefreshTime(player));
        pageInfo.setCfgBattleTime(getCfgBattleTime());
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_PAGE_INFO_RESP, pageInfo));
    }

    /**
     * 同步页面信息
     * @param player 玩家
     */
    public void syncPageInfo(Player player){
        syncPageInfo(player, false);
    }

    public String getSelfTeamId(Player player){
        if(player.isCsPlayer()){
            return playerIdToTeamId.getOrDefault(player.getId(), "");
        }else {
            if(playerDataMap.containsKey(player.getId())){
                return playerDataMap.get(player.getId()).teamId;
            }else {
                return "";
            }
        }
    }

    public String getSelfTeamId(String playerId){
        if(playerDataMap.containsKey(playerId)){
            return playerDataMap.get(playerId).teamId;
        }else {
            return "";
        }
    }

    public HawkTuple2<Long, Integer> getTeamPowerAndCnt(String teamId){
        Set<String> memberIds = teamIdToPlayerIds.get(teamId);
        if(memberIds == null || memberIds.isEmpty()){
            return new HawkTuple2<>(0L, 0);
        }
        long power = 0L;
        int count = 0;
        for(String memberId : memberIds){
            XHJZWarPlayerData playerData = playerDataMap.get(memberId);
            if(playerData == null || !teamId.equals(playerData.teamId)){
                continue;
            }
            power += playerData.battlePoint;
            count++;
        }
        return new HawkTuple2<>(power, count);
    }

    public HawkTuple3<Integer, Integer, Integer> getTeamAuthCnt(String teamId){
        Set<String> memberIds = teamIdToPlayerIds.get(teamId);
        if(memberIds == null || memberIds.isEmpty()){
            return new HawkTuple3<>(0, 0, 0);
        }
        int commandCnt = 0;
        int starterCnt = 0;
        int candidateCnt = 0;
        for(String memberId : memberIds){
            XHJZWarPlayerData playerData = playerDataMap.get(memberId);
            if(playerData == null || !teamId.equals(playerData.teamId)){
                continue;
            }
            if(playerData.auth == XHJZWar.XWPlayerAuth.XW_COMMAND_VALUE){
                commandCnt++;
            }
            if(playerData.auth == XHJZWar.XWPlayerAuth.XW_STARTER_VALUE){
                starterCnt++;
            }
            if(playerData.auth == XHJZWar.XWPlayerAuth.XW_CANDIDATE_VALUE){
                candidateCnt++;
            }
        }
        return new HawkTuple3<>(commandCnt, starterCnt, candidateCnt);
    }

    public int getTeamPowerRank(String teamId){
        return teamRank.getOrDefault(teamId, -1);
    }

    /**
     * 获得页面战队信息
     * @param player 玩家数据
     * @return 小队页面信息
     */
    public List<XHJZWar.XWTeamInfo> getTeamInfos(Player player){
        List<XHJZWar.XWTeamInfo> list = new ArrayList<>();
        String guildId = player.getGuildId();
        if(HawkOSOperator.isEmptyString(guildId)){
            return list;
        }
        if(player.isCsPlayer()){
            //如果是跨服玩家只从参战小队中加载自己的小队
            //查找自己的小队id
            String teamId = playerIdToTeamId.get(player.getId());
            //如果为空直接返回数据
            if(HawkOSOperator.isEmptyString(teamId)){
                return list;
            }
            XHJZWarTeamData teamData = battleTeamDataMap.get(teamId);
            if(teamData != null){
                XHJZWar.XWTeamInfo.Builder info = teamData.toPB();
                if(!HawkOSOperator.isEmptyString(teamData.oppTeamId)) {
                    XHJZWarTeamData oppTeamData = battleTeamDataMap.get(teamData.oppTeamId);
                    if (oppTeamData != null) {
                        info.setOppTeam(oppTeamData.toPB());
                    }
                }
                list.add(info.build());
            }
        }else {
            //本服玩家加载本联盟所有小队
            XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
            //遍历所有小队
            for(int i = 1; i <= constCfg.getTeamNumLimit(); i++){
                String teamId = getTeamId(guildId, i);
                XHJZWarTeamData teamData = teamDataMap.get(teamId);
                if(teamData != null){
                    teamData.refreshInfo();
                    XHJZWar.XWTeamInfo.Builder info = teamData.toPB();
                    if(!HawkOSOperator.isEmptyString(teamData.oppTeamId)) {
                        XHJZWarTeamData oppTeamData = battleTeamDataMap.get(teamData.oppTeamId);
                        if (oppTeamData != null) {
                            info.setOppTeam(oppTeamData.toPB());
                        }
                    }
                    list.add(info.build());
                }
            }
        }
        return list;
    }

    /**
     * 页面状态信息
     * @param player 玩家
     * @return 状态信息
     */
    public XHJZWar.XWStateInfo.Builder getStateInfo(Player player){
        switch (stateData.getState()){
            case MATCH_WAIT:
            case MATCH:
            case MATCH_END:{
                String teamId = getSelfTeamId(player);
                if(HawkOSOperator.isEmptyString(teamId) || !signUpMap.containsKey(teamId)){
                    XHJZWar.XWStateInfo.Builder stateInfo = XHJZWar.XWStateInfo.newBuilder();
                    stateInfo.setState(XHJZWar.XWState.XW_FINISH);
                    XHJZWarTimeCfg cfg = XHJZWarService.getInstance().getTimeCfg();
                    if(cfg != null){
                        stateInfo.setEndTime(cfg.getEndTime());
                    }else {
                        stateInfo.setEndTime(HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(1));
                    }
                    return stateInfo;
                }
            }
            break;
            case BATTLE:{
                String teamId = playerIdToTeamId.get(player.getId());
                if(!HawkOSOperator.isEmptyString(teamId)){
                    XHJZWarTeamData teamData = battleTeamDataMap.get(teamId);
                    if(teamData != null){
                        XHJZWarBattleStateData battleStateData = battleStateDataMap.get(teamData.timeIndex);
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
    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 客户端请求相关方法
     * 需要进行线程安全处理
     ******************************************************************************************************************/
    /**
     * 当前状态是否可以管理小队
     * @return 是否可以管理
     */
    public boolean canManagerState(){
        //匹配和战斗期间不能进行小队管理
        if(stateData.getState() == XHJZWarStateEnum.MATCH_WAIT
                || stateData.getState() == XHJZWarStateEnum.MATCH
                || stateData.getState() == XHJZWarStateEnum.MATCH_END
                || stateData.getState() == XHJZWarStateEnum.BATTLE){
            return false;
        }
        return true;
    }

    public boolean checkTeamManager(Player player, XHJZWar.XWTeamManagerReq req){
        if(req.getOpt() == XHJZWar.XWTeamOpt.XW_CREATE){
            int teamIndex = 0;
            XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
            int nameLength = req.getName().length();
            if(nameLength < constCfg.getTeamNameNumLimit().first || nameLength > constCfg.getTeamNameNumLimit().second){
                return false;
            }
            for(int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
                XHJZWarTeamData tmp = teamDataMap.get(getTeamId(player.getGuildId(), i));
                if(teamIndex == 0 && tmp == null){
                    teamIndex = i;
                }
            }
            if(teamIndex <= 0){
                return false;
            }
        }
        if(req.getOpt() == XHJZWar.XWTeamOpt.XW_CHOOSE_TIME){
            if(signUpMap.containsKey(req.getTeamId())){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_HAS_CHOOSED_TIME, 0);
                return false;
            }
            int rank = teamRank.getOrDefault(req.getTeamId(), 1000);
            XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
            if(rank > constCfg.getSignRankLimit()){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_RANK_LIMIT, 0);
                return false;
            }
            Set<String> playerIds = teamIdToPlayerIds.get(req.getTeamId());
            if(playerIds == null || playerIds.size() < constCfg.getMemberSignupLimit()){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_MEMBER_NUM_LIMIT, 0);
                return false;
            }
            XHJZWarTeamData teamData = teamDataMap.get(req.getTeamId());
            if(teamData == null){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_MEMBER_NUM_LIMIT, 0);
                return false;
            }
            if(teamData.timeIndex > 0){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_HAS_CHOOSED_TIME, 0);
                return false;
            }
        }
        if(req.getOpt() == XHJZWar.XWTeamOpt.XW_DISMISS){
            if(XHJZSeasonManager.getInstance().getState() != CMWStateEnum.CLOSE){
                player.sendError(HP.code2.XHJZ_TEAM_MANAGER_REQ_VALUE, Status.XHJZError.XHJZ_IN_SEASON, 0);
                return false;
            }
        }
        return true;
    }

    public boolean checkMemberManager(Player player, XHJZWar.XWMemberManagerReq req){
        String guildId = player.getGuildId();
        if(HawkOSOperator.isEmptyString(guildId)){
            return false;
        }
        XHJZWarTeamData teamData = teamDataMap.get(req.getTeamId());
        if(teamData == null){
            return false;
        }
        if(!guildId.equals(teamData.guildId)){
            return false;
        }
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        HawkTuple3<Integer, Integer, Integer> cntData = getTeamAuthCnt(req.getTeamId());
        switch (req.getAuth()){
            case XW_COMMAND:{
                if(cntData.first >= constCfg.getTeamCommanderLimit()){
                    return false;
                }
            }
            break;
            case XW_STARTER:{
                if(cntData.second >= constCfg.getTeamMemberLimit()){
                    return false;
                }
            }
            break;
            case XW_CANDIDATE:{
                if(cntData.third >= constCfg.getTeamPreparationLimit()){
                    return false;
                }
            }
            break;
        }
        return true;
    }

    /**
     * 小队管理
     * @param player 玩家数据
     * @param req 玩家请求
     */
    public void teamManager(Player player, XHJZWar.XWTeamManagerReq req){
        //检查当前状态是否可以执行此操作
        if(!canManagerState()){
            return;
        }
        if(!checkTeamManager(player, req)){
            return;
        }
        //玩家如果没有联盟不允许进行小队操作
        if(!player.hasGuild()){
            return;
        }
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        XHJZWarTeamData teamData = null;
        switch (req.getOpt()){
            //创建小队
            case XW_CREATE:{
                int teamIndex = 0;
                for(int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
                    XHJZWarTeamData tmp = teamDataMap.get(getTeamId(player.getGuildId(), i));
                    if(teamIndex == 0 && tmp == null){
                        teamIndex = i;
                    }
                }
                if(teamIndex > 0){
                    GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(player.getGuildId());
                    teamData = new XHJZWarTeamData(guildInfoObject, teamIndex);
                    teamData.name = req.getName();
                    teamDataMap.put(teamData.id, teamData);
                    teamIdToPlayerIds.put(teamData.id, new CopyOnWriteArraySet<>());
                    updateTeamCrate(teamData);
                }
            }
            break;
            //小队改名
            case XW_CHANGE_NAME:{
                teamData = teamDataMap.get(req.getTeamId());
                if(teamData != null){
                    teamData.name = req.getName();
                    teamData.update();
                }
            }
            break;
            //小队选时间报名
            case XW_CHOOSE_TIME:{
                teamData = teamDataMap.get(req.getTeamId());
                if(teamData != null){
                    teamData.timeIndex = req.getTimeIndex();
                    signUpMap.put(teamData.id, teamData.timeIndex);
                    updateTeamSignUp(teamData);
                    
                    //添加代办
                    HawkTuple2<Long, Long> timeTupe = this.getBattleTime(teamData.timeIndex);
                    if(Objects.nonNull(timeTupe)){
                    	ScheduleInfo schedule = ScheduleInfo.createNewSchedule(ScheduleType.SCHEDULE_TYPE_3_VALUE, teamData.guildId,timeTupe.first, 0, 0,teamData.id);
                		ScheduleService.getInstance().addSystemSchedule(schedule);
                    }
                }
            }
            break;
            //小队解散
            case XW_DISMISS:{
                teamData = teamDataMap.remove(req.getTeamId());
                Set<String> playerIds = teamIdToPlayerIds.remove(teamData.id);
                updateTeamDissmiss(teamData);
                updatePlayerDissmiss(playerIds);
            }
            break;

        }
        //如果小队数据不为空同步给前端
        if(teamData != null){
            teamData.refreshInfo();
            XHJZWar.XWTeamManagerResp.Builder resp = XHJZWar.XWTeamManagerResp.newBuilder();
            //小队信息
            resp.setTeamInfo(teamData.toPB());
            //操作
            resp.setOpt(req.getOpt());
            player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_TEAM_MANAGER_RESP, resp));
            try {
                Map<String, Object> param = new HashMap<>();
                param.put("teamId", teamData.id);
                param.put("power", teamData.battlePoint);
                param.put("count", teamData.memberCnt);
                param.put("rank", teamData.rank);
                param.put("opt", req.getOpt().getNumber());
                LogUtil.logActivityCommon(player, LogConst.LogInfoType.xhjz_team, param);
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
    }

    /**
     * 成员管理
     * @param player 玩家数据
     * @param req 玩家请求
     */
    public void memberManager(Player player, XHJZWar.XWMemberManagerReq req){
        //检查当前状态是否可以执行此操作
        if(!canManagerState()){
            return;
        }
        if(!checkMemberManager(player, req)){
            return;
        }
        XHJZWarPlayerData playerData = null;
        String oldTeamId = "";
        String newTeamId = "";
        switch (req.getAuth()){
            //设置出战，身份为指挥官，首发，预备队
            case XW_COMMAND:
            case XW_STARTER:
            case XW_CANDIDATE:{
                playerData = playerDataMap.get(req.getPlayerId());
                if(playerData != null){
                    oldTeamId = playerData.teamId;
                    newTeamId = req.getTeamId();
                    playerData.teamId = req.getTeamId();
                    playerData.auth = req.getAuth().getNumber();
                    if(!HawkOSOperator.isEmptyString(oldTeamId) && !oldTeamId.equals(playerData.teamId)){
                        teamIdToPlayerIds.get(oldTeamId).remove(playerData.id);
                        String teamPlayerKey = String.format(XHJZRedisKey.XHJZ_WAR_TEAM_PLAYER, oldTeamId);
                        RedisProxy.getInstance().getRedisSession().sRem(teamPlayerKey, playerData.id);
                    }
                    if(!oldTeamId.equals(playerData.teamId)){
                        teamIdToPlayerIds.get(playerData.teamId).add(playerData.id);
                        String teamPlayerKey = String.format(XHJZRedisKey.XHJZ_WAR_TEAM_PLAYER, playerData.teamId);
                        RedisProxy.getInstance().getRedisSession().sAdd(teamPlayerKey, 0, playerData.id);
                    }
                    playerData.update();
                    //updatePlayerDate(playerData);
                }
            }
            break;
            //设置为不出战
            case XW_NO_TEAM:{
                playerData = playerDataMap.get(req.getPlayerId());
                if(playerData != null){
                    oldTeamId = playerData.teamId;
                    playerData.teamId = "";
                    playerData.auth = req.getAuth().getNumber();
                    if(!HawkOSOperator.isEmptyString(oldTeamId)){
                        teamIdToPlayerIds.get(oldTeamId).remove(playerData.id);
                        String teamPlayerKey = String.format(XHJZRedisKey.XHJZ_WAR_TEAM_PLAYER, oldTeamId);
                        RedisProxy.getInstance().getRedisSession().sRem(teamPlayerKey, playerData.id);
                    }
                    playerData.update();
                    //updateNoTeam(playerData);
                }
            }
            break;
        }
        if(playerData != null){
            XHJZWar.XWMemberManagerResp.Builder resp = XHJZWar.XWMemberManagerResp.newBuilder();
            //玩家信息
            resp.setPlayerInfo(playerData.toPB());
            //操作
            resp.setAuth(req.getAuth());
            player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_MEMBER_MANAGER_RESP, resp));
            try {
                Map<String, Object> param = new HashMap<>();
                param.put("targetId", playerData.id);
                param.put("oldTeamId", oldTeamId);
                param.put("newTeamId", newTeamId);
                param.put("auth", req.getAuth().getNumber());
                LogUtil.logActivityCommon(player, LogConst.LogInfoType.xhjz_member, param);
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
        XHJZWarTeamData oldTeamData = teamDataMap.get(oldTeamId);
        //如果小队数据不为空同步给前端
        if(oldTeamData != null){
            oldTeamData.refreshInfo();
            XHJZWar.XWTeamManagerResp.Builder resp = XHJZWar.XWTeamManagerResp.newBuilder();
            //小队信息
            resp.setTeamInfo(oldTeamData.toPB());
            //操作
            resp.setOpt(XHJZWar.XWTeamOpt.XW_TEAM_UPDATE);
            player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_TEAM_MANAGER_RESP, resp));
        }
        XHJZWarTeamData newTeamData = teamDataMap.get(newTeamId);
        //如果小队数据不为空同步给前端
        if(newTeamData != null){
            newTeamData.refreshInfo();
            XHJZWar.XWTeamManagerResp.Builder resp = XHJZWar.XWTeamManagerResp.newBuilder();
            //小队信息
            resp.setTeamInfo(newTeamData.toPB());
            //操作
            resp.setOpt(XHJZWar.XWTeamOpt.XW_TEAM_UPDATE);
            player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_TEAM_MANAGER_RESP, resp));
        }
    }

    public void seasonSignup(String teamId, int timeIndex){
        XHJZWarTeamData teamData = teamDataMap.get(teamId);
        if(teamData != null){
            teamData.timeIndex = timeIndex;
            signUpMap.put(teamData.id, teamData.timeIndex);
            updateTeamSignUp(teamData);
        }
    }


    /**
     * 成员列表
     * @param player 玩家数据
     */
    public void memberList(Player player){
        XHJZWar.XWMemberListResp.Builder resp = XHJZWar.XWMemberListResp.newBuilder();
        for(XHJZWarPlayerData playerData : getMemberList(player.getGuildId())){
            resp.addPlayerInfos(playerData.toPB());
        }
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_MEMBER_LIST_RESP, resp));
    }

    /**
     * 小队战力排行
     * @param player 玩家数据
     */
    public void teamRank(Player player){
        XHJZWar.XWTeamRankResp.Builder resp = rankResp.clone();
        resp.setSelfTeamId(getSelfTeamId(player));
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_TEAM_RANK_RESP, resp));
    }

    /**
     * 对战历史
     * @param player 玩家数据
     */
    public void history(Player player){
        String guildId = player.getGuildId();
        if(HawkOSOperator.isEmptyString(guildId)){
            return;
        }
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        XHJZWar.XWHistoryResp.Builder resp = XHJZWar.XWHistoryResp.newBuilder();
        for(int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
            List<XHJZWarHistoryData> historyDataList = XHJZWarHistoryData.load(getTeamId(player.getGuildId(), i), constCfg.getRecordPointCount());
            for(XHJZWarHistoryData historyData : historyDataList){
                resp.addTeamInfos(historyData.toPB());
            }
        }
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_HISTORY_RESP, resp));
    }

    /**
     * 兑换道具
     * @param player 玩家数据
     * @param req 玩家参数
     */
    public void exchange(Player player, XHJZWar.XWEXchangeReq req){
        XHJZShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(XHJZShopCfg.class, req.getCfgId());
        Map<Integer, Integer> map = getShopData(player, false);
        int count = map.getOrDefault(req.getCfgId(), 0);
        if(count >= cfg.getNumLimit()){
            return;
        }
        ConsumeItems consume = ConsumeItems.valueOf();
        consume.addConsumeInfo(ItemInfo.valueListOf(cfg.getCost(), req.getTimes()));
        if(!consume.checkConsume(player)){
            return;
        }
        count += req.getTimes();
        map.put(req.getCfgId(), count);
        updataShopData(player);
        consume.consumeAndPush(player, Action.XHJZ_SHOP_COST);
        AwardItems awardItems = AwardItems.valueOf();
        awardItems.addItemInfos(ItemInfo.valueListOf(cfg.getItem(), req.getTimes()));
        awardItems.rewardTakeAffectAndPush(player, Action.XHJZ_SHOP_GET, true);
        XHJZWar.XWEXchangeResp.Builder resp = XHJZWar.XWEXchangeResp.newBuilder();
        resp.setCfgId(req.getCfgId());
        resp.setTimes(count);
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_EXCHANGE_RESP, resp));
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
    public String gm(Map<String, String> map){
        //只有测试环境可以使用
        if(!GsConfig.getInstance().isDebug()){
            return "不是测试环境";
        }
        //要执行的gm指令
        String cmd = map.getOrDefault("cmd", "");
        switch (cmd){

            case "info":{
                return printInfo();
            }
            case "next":{
                if(stateData.getState() == XHJZWarStateEnum.BATTLE){
                    XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
                    boolean isDone = false;
                    for(int i = 1; i <= constCfg.getWarCount(); i++){
                        if(battleStateDataMap.get(i).getState() != XHJZWarStateEnum.BATTLE_END){
                            battleStateDataMap.get(i).toNext();
                            isDone = true;
                            break;
                        }
                    }
                    if(!isDone){
                        stateData.toNext();
                    }
                }else {
                    if(XHJZSeasonManager.getInstance().getState() != CMWStateEnum.CLOSE && getState() == XHJZWarStateEnum.PEACE){
                        stateData.setTermId(stateData.getTermId()+1);
                    }
                    stateData.toNext();
                }
                return printInfo();
            }
            case "bigNext":{
                stateData.toNext();
                return printInfo();
            }
            case "battleNext":{
                if(stateData.getState() == XHJZWarStateEnum.BATTLE){
                    if(map.containsKey("timeIndex")){
                        int timeIndex = Integer.parseInt(map.get("timeIndex"));
                        battleStateDataMap.get(timeIndex).toNext();
                    }else {
                        for(int i = 1; i <= 4; i++){
                            if(battleStateDataMap.get(i).getState() != XHJZWarStateEnum.BATTLE_END){
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
                XHJZWarService.getInstance().onMatch();
                return printInfo();
            }
            //加载房间数据
            case "loadRoom":{
                XHJZWarService.getInstance().loadBattleRoom();
                return printInfo();
            }
            //战斗结束
            case "battleOver":{
                XHJZRoomManager.getInstance().findAllRoom().forEach(room -> room.setState(new XHJZGameOver(room)));
                return printInfo();
            }
            case "sendAward":{
                if(map.containsKey("timeIndex")){
                    int timeIndex = Integer.parseInt(map.get("timeIndex"));
                    XHJZWarService.getInstance().sendAward(timeIndex);
                }else {
                    XHJZWarService.getInstance().sendAward(1);
                }
                return printInfo();
            }
            case "matchInfo":{
                return printMatchInfo(Integer.parseInt(map.getOrDefault("termId", "0")), Integer.parseInt(map.getOrDefault("timeIndex", "0")));
            }
            case "signupInfo":{
                return printSignupInfo(Integer.parseInt(map.getOrDefault("termId", "0")), Integer.parseInt(map.getOrDefault("timeIndex", "0")));
            }
            case "matchTestInfo":{
                return printMatchTestInfo(map);
            }
            case "matchTestAddGuild":{
                matchTestAddGuild(map);
                return printMatchTestInfo(map);
            }
            case "matchTestAddTeam":{
                matchTestAddTeam(map);
                return printMatchTestInfo(map);
            }
            case "matchTestTeamAddPower":{
                matchTestTeamAddPower(map);
                return printMatchTestInfo(map);
            }
            case "matchTestTeamCleanPower":{
                matchTestTeamCleanPower(map);
                return printMatchTestInfo(map);
            }
            case "matchTestTeamAddDay":{
                matchTestTeamAddDay(map);
                return printMatchTestInfo(map);
            }
            case "matchTestTeamCleanDay":{
                matchTestTeamCleanDay(map);
                return printMatchTestInfo(map);
            }
            case "matchTestClean":{
                matchTestClean(map);
                return printMatchTestInfo(map);
            }
            case "matchTestTeamRankInfo":{
                return printMatchTestTeamRankInfo(map);
            }
            case "matchTestResultInfo":{
                return printMatchTestResultInfo(map);
            }
            case "doTeamRank":{
                doTeamRank(false);
                return printInfo();
            }
            case "setTermId":{
                int termId = Integer.parseInt(map.get("termId"));
                stateData.setTermId(termId);
                return printInfo();
            }
            case "addTermId":{
                int add = Integer.parseInt(map.get("add"));
                if(add == 0){
                    stateData.setTermId(0);
                }else {
                    stateData.setTermId(stateData.getTermId() + add);
                }
                return printInfo();
            }
            case "addAllTeam":{
                XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
                List<String> guildIds = GuildService.getInstance().getGuildIds();
                guildIds.forEach(id -> {
                    try {
                        GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(id);
                        if (guildInfoObject != null) {
                            for(int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
                                XHJZWarTeamData tmp = teamDataMap.get(getTeamId(id, i));
                                if(tmp != null){
                                    continue;
                                }
                                tmp = new XHJZWarTeamData(guildInfoObject, i);
                                tmp.name = tmp.id;
                                teamDataMap.put(tmp.id, tmp);
                                teamIdToPlayerIds.put(tmp.id, new CopyOnWriteArraySet<>());
                                updateTeamCrate(tmp);
                            }
                        }
                    } catch (Exception e) {
                        HawkException.catchException(e);
                    }
                });
                return printInfo();
            }
            case "allSignFirst":{
                for(XHJZWarTeamData teamData : teamDataMap.values()){
                    if(teamData != null){
                        teamData.timeIndex = 1;
                        signUpMap.put(teamData.id, teamData.timeIndex);
                        updateTeamSignUp(teamData);
                    }
                }
                return printInfo();
            }
            case "allOneSignFirst":{
                List<String> guildIds = GuildService.getInstance().getGuildIds();
                guildIds.forEach(id -> {
                    try {
                        GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(id);
                        if (guildInfoObject != null) {
                            XHJZWarTeamData teamData = teamDataMap.get(getTeamId(id, 1));
                            if(teamData != null){
                                teamData.timeIndex = 1;
                                signUpMap.put(teamData.id, teamData.timeIndex);
                                updateTeamSignUp(teamData);
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                return printInfo();
            }
            default:{
                return "未知操作";
            }
        }
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
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=info\">刷新</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=next\">切阶段</a>          ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=bigNext\">切大阶段</a>         ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=battleNext&timeIndex=1\">切战场1阶段</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=battleNext&timeIndex=2\">切战场2阶段</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=battleNext&timeIndex=3\">切战场3阶段</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=battleNext&timeIndex=4\">切战场4阶段</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=battleOver\">结束战斗</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=doTeamRank\">刷新排行榜</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=addAllTeam\">战队加满</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=allSignFirst\">所有战队报名时间1</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=allOneSignFirst\">所有1队报名时间1</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=addTermId&add=1\">期数加1</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=addTermId&add=5\">期数加5</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=addTermId&add=10\">期数加10</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=addTermId&add=100\">期数加100</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=addTermId&add=1000\">期数加1000</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=addTermId&add=0\">期数清0</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestInfo\">匹配测试</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=signupInfo\" target=\"_blank\">报名信息</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchInfo\" target=\"_blank\">战场信息</a><br><br>";
            info += stateData.toString() + "<br>";
            for(XHJZWarBattleStateData battleStateData : battleStateDataMap.values()){
                info += battleStateData.toString() + "<br>";
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
        return info;
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
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=info\">主页</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchInfo\">全部</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchInfo&timeIndex=1\">时间段1</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchInfo&timeIndex=2\">时间段2</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchInfo&timeIndex=3\">时间段3</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchInfo&timeIndex=4\">时间段4</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=signupInfo\" target=\"_blank\">报名信息</a><br>";
            //获得期数
            termId = termId == 0 ? getTermId(): termId;
            Map<String, String> roomMap = RedisProxy.getInstance().getRedisSession().hGetAll(String.format(XHJZRedisKey.XHJZ_WAR_ROOM, termId));
            Map<String, String> teamMap = RedisProxy.getInstance().getRedisSession().hGetAll(XHJZRedisKey.XHJZ_WAR_TEAM);
            for(String roomStr : roomMap.values()){
                //解析房间数据
                XHJZWarRoomData roomData = XHJZWarRoomData.unSerialize(roomStr);
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
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=info\">主页</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=signupInfo\">全部</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=signupInfo&timeIndex=1\">时间段1</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=signupInfo&timeIndex=2\">时间段2</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=signupInfo&timeIndex=3\">时间段3</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=signupInfo&timeIndex=4\">时间段4</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchInfo\" target=\"_blank\">战场信息</a><br>";
            //获得期数
            termId = termId == 0 ? getTermId(): termId;
            Map<String, String> teamMap = RedisProxy.getInstance().getRedisSession().hGetAll(XHJZRedisKey.XHJZ_WAR_TEAM);
            if(timeIndex == 0){
                XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
                for(int i = 1; i <= constCfg.getWarCount(); i++) {
                    info += "==========================================================================================<br>";
                    info += "报名时间段:" + i + "<br>";
                    String signUpTimeKey = String.format(XHJZRedisKey.XHJZ_WAR_SIGNUP_TIME, termId, i);
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
                String signUpTimeKey = String.format(XHJZRedisKey.XHJZ_WAR_SIGNUP_TIME, termId, timeIndex);
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

    public String printMatchTestInfo(Map<String, String> map){
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
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=info\">主页</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestInfo\">刷新</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestAddGuild\">加100联盟</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestAddGuild&count=10\">加10联盟</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestAddGuild&count=1\">加1联盟</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestAddTeam\">加满战队</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamAddPower\">战力随机</a>        ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamCleanPower\">战力清空</a>        ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestClean\">清空数据</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamRankInfo\" target=\"_blank\">战队排行</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestResultInfo\" target=\"_blank\">匹配结果</a><br><br>";

            Map<String, String> guildMap = RedisProxy.getInstance().getRedisSession().hGetAll(XHJZRedisKey.XHJZ_WAR_TEST_GUILD);
            Map<String, String> teamMap = RedisProxy.getInstance().getRedisSession().hGetAll(XHJZRedisKey.XHJZ_WAR_TEST_TEAM);
            info +="<table border=\"1px\" cellspacing=\"0\">\n";
            for(int i = 1; i <= guildMap.size(); i++){
                info += gmFillMatchTestGuildData(String.valueOf(i), guildMap, teamMap);
            }
            info +="</table>";
        } catch (Exception e) {
            HawkException.catchException(e);
        }
        return info;
    }

    public String printMatchTestTeamRankInfo(Map<String, String> map){
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
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=info\">主页</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamRankInfo\">刷新</a><br><br>";
            Map<String, String> teamMap = RedisProxy.getInstance().getRedisSession().hGetAll(XHJZRedisKey.XHJZ_WAR_TEST_TEAM);
            List<XHJZWarTeamData> teamList = new ArrayList<>();
            for(String teamId : teamMap.keySet()) {
                String teamStr = teamMap.get(teamId);
                XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
                if (teamData == null) {
                    continue;
                }
                teamList.add(teamData);
            }
            teamList.sort((o1, o2) -> {
                if (o1.matchPower != o2.matchPower) {
                    return o1.matchPower > o2.matchPower ? -1 : 1;
                }
                return 0;
            });
            info +="<table border=\"1px\" cellspacing=\"0\">\n";
            int i = 1;
            for(XHJZWarTeamData teamData : teamList){
                info += "\t<tr>\n" +
                        "\t\t<td colspan=\"2\">"+i+"</td>\n" +
                        "\t\t<td colspan=\"2\">"+teamData.id+"</td>\n" +
                        "\t\t<td colspan=\"2\">"+teamData.name+"</td>\n" +
                        "\t\t<td colspan=\"2\">"+teamData.matchPower+"</td>\n" +
                        "\t</tr>\n";
                i++;
            }
            info +="</table>";
        } catch (Exception e) {
            HawkException.catchException(e);
        }
        return info;
    }

    public String printMatchTestResultInfo(Map<String, String> map){
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
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=info\">主页</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestResultInfo\">刷新</a><br><br>";
            Map<String, String> teamMap = RedisProxy.getInstance().getRedisSession().hGetAll(XHJZRedisKey.XHJZ_WAR_TEST_TEAM);
            Map<Integer, List<XHJZWarTeamData>> teamDayMap = new HashMap<>();
            for(String teamId : teamMap.keySet()) {
                String teamStr = teamMap.get(teamId);
                XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
                if (teamData == null) {
                    continue;
                }
                if(!teamDayMap.containsKey(teamData.openDayW)){
                    teamDayMap.put(teamData.openDayW, new ArrayList<>());
                }
                teamDayMap.get(teamData.openDayW).add(teamData);
            }
            List<Integer> dayList = new ArrayList<>(teamDayMap.keySet());
            List<XHJZWarTeamData> noMatchList = new ArrayList<>();
            dayList.sort((o1, o2) -> o2 - o1);
            for(int day : dayList){
                List<XHJZWarTeamData> teamList = new ArrayList<>();
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
                while (teamList.size() > 1) {
                    HawkTuple2<XHJZWarTeamData, XHJZWarTeamData> result = matchTeam(teamList);
                    if (result == null) {
                        break;
                    }
                    if(result.first != null && result.second == null){
                        noMatchList.add(result.first);
                    }
                    if (result.first != null) {
                        teamList.remove(result.first);
                    }
                    if (result.second != null) {
                        teamList.remove(result.second);
                    }
                    XHJZWarTeamData teamData1 = result.first;
                    XHJZWarTeamData teamData2 = result.second;
                    if (teamData1 != null && teamData2 != null) {
                        info +=  "==========================================================================================<br>";
                        info += teamData1.name +"|"+teamData2.name+"<br>";
                    }
                }
                if(!teamList.isEmpty()){
                    noMatchList.addAll(teamList);
                }
            }
            info +=  "==========================================================================================<br>";
        } catch (Exception e) {
            HawkException.catchException(e);
        }
        return info;
    }

    public String gmFillMatchTestGuildData(String guildId, Map<String, String> guildMap, Map<String, String> teamMap){
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        String info = "";
        //组装页面信息
        try {
            //获取本地InetAddress对象
            InetAddress localAddress = InetAddress.getLocalHost();
            //获取本地IP地址
            String ipAddress = localAddress.getHostAddress();
            //获得gm端口
            int port = GsConfig.getInstance().getGmPort();
            info = "\t<tr>\n" +
                   "\t\t<td colspan=\"2\">"+guildMap.get(guildId)+"</td>\n" +
                   "\t\t<td colspan=\"8\">" +
                   "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestAddTeam&guildId="+guildId+"\">加满战队</a>        " +
                   "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestClean&guildId="+guildId+"\">清空战队</a>        " +
                   "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestAddTeam&guildId="+guildId+"&count=1\">战队加1</a>        " +
                   "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamAddPower&guildId="+guildId+"\">战力随机</a>        " +
                   "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamCleanPower&guildId="+guildId+"\">战力清空</a>        " +
                   "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamAddDay&guildId="+guildId+"&add=1\">天数加1</a>        " +
                   "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamCleanDay&guildId="+guildId+"\">天数清空</a>        " +
                   "</td>\n" +
                   "\t</tr>\n";
            for(int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
                info += gmFillMatchTestTeamData(i, getTeamId(guildId, i), teamMap);
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }


        return info;
    }

    public String gmFillMatchTestTeamData(int index, String teamId, Map<String, String> teamMap){
        if(!teamMap.containsKey(teamId)){
            return "";
        }
        String teamStr = teamMap.get(teamId);
        XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
        if(teamData == null){
            return "";
        }
        String info = "";
        //组装页面信息
        try {
            //获取本地InetAddress对象
            InetAddress localAddress = InetAddress.getLocalHost();
            //获取本地IP地址
            String ipAddress = localAddress.getHostAddress();
            //获得gm端口
            int port = GsConfig.getInstance().getGmPort();
            info =  "\t<tr>\n" +
                    "\t\t<td colspan=\"2\">"+index+"小队</td>\n" +
                    "\t\t<td colspan=\"2\">"+teamData.id+"</td>\n" +
                    "\t\t<td colspan=\"2\">"+teamData.name+"</td>\n" +
                    "\t\t<td colspan=\"2\">"+teamData.matchPower+"</td>\n" +
                    "\t\t<td colspan=\"2\">"+teamData.openDayW+"</td>\n" +
                    "\t\t<td colspan=\"2\">" +
                    "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamAddPower&teamId="+teamId+"&add=1000\">加1000战力</a>        " +
                    "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamAddPower&teamId="+teamId+"&add=-1000\">减1000战力</a>        " +
                    "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamAddPower&teamId="+teamId+"&add=100\">加100战力</a>        " +
                    "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamAddPower&teamId="+teamId+"&add=-100\">减100战力</a>        " +
                    "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamAddPower&teamId="+teamId+"&add=10\">加10战力</a>        " +
                    "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamAddPower&teamId="+teamId+"&add=-10\">减10战力</a>        " +
                    "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamAddPower&teamId="+teamId+"&add=1\">加1战力</a>        " +
                    "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamAddPower&teamId="+teamId+"&add=-1\">减1战力</a>        " +
                    "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamCleanPower&teamId="+teamId+"\">战力清空</a>        " +
                    "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamAddDay&teamId="+teamId+"&add=1\">天数加1</a>        " +
                    "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestTeamCleanDay&teamId="+teamId+"\">天数清空</a>        " +
                    "<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XHJZGM&cmd=matchTestClean&teamId="+teamId+"\">删除</a>        " +
                    "</td>\n" +
                    "\t</tr>\n";
        } catch (Exception e) {
            HawkException.catchException(e);
        }

        return info;
    }

    public void matchTestTeamAddPower(Map<String, String> map){
        Map<String, String> teamTmpMap = new HashMap<>();
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        if(map.containsKey("guildId")) {
            String guildId = map.get("guildId");
            for (int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
                String teamId = getTeamId(guildId, i);
                String teamStr = RedisProxy.getInstance().getRedisSession().hGet(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamId);
                XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
                if (teamData == null) {
                    continue;
                }
                teamData.matchPower = HawkRand.randInt(1000);
                teamTmpMap.put(teamId, teamData.serialize());
            }
        }else if(map.containsKey("teamId")){
            String teamId = map.get("teamId");
            String teamStr = RedisProxy.getInstance().getRedisSession().hGet(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamId);
            XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
            if(teamData == null){
                return;
            }
            int add = Integer.parseInt(map.getOrDefault("add", "100"));
            teamData.matchPower = teamData.matchPower + add;
            RedisProxy.getInstance().getRedisSession().hSet(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamId, teamData.serialize());
        }else {
            Map<String, String> teamMap = RedisProxy.getInstance().getRedisSession().hGetAll(XHJZRedisKey.XHJZ_WAR_TEST_TEAM);
            for(String teamId : teamMap.keySet()){
                String teamStr = teamMap.get(teamId);
                XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
                if(teamData == null){
                    continue;
                }
                teamData.matchPower = HawkRand.randInt(1000);
                teamTmpMap.put(teamId, teamData.serialize());
            }
        }
        if(!teamTmpMap.isEmpty()){
            RedisProxy.getInstance().getRedisSession().hmSet(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamTmpMap, 0);
        }
    }

    public void matchTestTeamCleanPower(Map<String, String> map){
        Map<String, String> teamTmpMap = new HashMap<>();
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        if(map.containsKey("guildId")) {
            String guildId = map.get("guildId");
            for (int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
                String teamId = getTeamId(guildId, i);
                String teamStr = RedisProxy.getInstance().getRedisSession().hGet(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamId);
                XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
                if(teamData == null){
                    continue;
                }
                teamData.matchPower = 0;
                teamTmpMap.put(teamId, teamData.serialize());
            }
        }else if(map.containsKey("teamId")){
            String teamId = map.get("teamId");
            String teamStr = RedisProxy.getInstance().getRedisSession().hGet(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamId);
            XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
            if(teamData != null){
                teamData.matchPower = 0;
                teamTmpMap.put(teamId, teamData.serialize());
            }
        }else {
            Map<String, String> teamMap = RedisProxy.getInstance().getRedisSession().hGetAll(XHJZRedisKey.XHJZ_WAR_TEST_TEAM);
            for(String teamId : teamMap.keySet()){
                String teamStr = teamMap.get(teamId);
                XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
                if(teamData == null){
                    continue;
                }
                teamData.matchPower = 0;
                teamTmpMap.put(teamId, teamData.serialize());
            }
        }
        if(!teamTmpMap.isEmpty()){
            RedisProxy.getInstance().getRedisSession().hmSet(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamTmpMap, 0);
        }
    }

    public void matchTestTeamAddDay(Map<String, String> map){
        Map<String, String> teamTmpMap = new HashMap<>();
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        if(map.containsKey("guildId")) {
            String guildId = map.get("guildId");
            for (int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
                String teamId = getTeamId(guildId, i);
                String teamStr = RedisProxy.getInstance().getRedisSession().hGet(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamId);
                XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
                if (teamData == null) {
                    continue;
                }
                int add = Integer.parseInt(map.getOrDefault("add", "100"));
                teamData.openDayW = teamData.openDayW + add;
                teamTmpMap.put(teamId, teamData.serialize());
            }
        }else if(map.containsKey("teamId")){
            String teamId = map.get("teamId");
            String teamStr = RedisProxy.getInstance().getRedisSession().hGet(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamId);
            XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
            if(teamData == null){
                return;
            }
            int add = Integer.parseInt(map.getOrDefault("add", "100"));
            teamData.openDayW = teamData.openDayW + add;
            RedisProxy.getInstance().getRedisSession().hSet(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamId, teamData.serialize());
        }else {
            Map<String, String> teamMap = RedisProxy.getInstance().getRedisSession().hGetAll(XHJZRedisKey.XHJZ_WAR_TEST_TEAM);
            for(String teamId : teamMap.keySet()){
                String teamStr = teamMap.get(teamId);
                XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
                if(teamData == null){
                    continue;
                }
                teamData.openDayW = HawkRand.randInt(1000);
                teamTmpMap.put(teamId, teamData.serialize());
            }
        }
        if(!teamTmpMap.isEmpty()){
            RedisProxy.getInstance().getRedisSession().hmSet(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamTmpMap, 0);
        }
    }

    public void matchTestTeamCleanDay(Map<String, String> map){
        Map<String, String> teamTmpMap = new HashMap<>();
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        if(map.containsKey("guildId")) {
            String guildId = map.get("guildId");
            for (int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
                String teamId = getTeamId(guildId, i);
                String teamStr = RedisProxy.getInstance().getRedisSession().hGet(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamId);
                XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
                if(teamData == null){
                    continue;
                }
                teamData.openDayW = 0;
                teamTmpMap.put(teamId, teamData.serialize());
            }
        }else if(map.containsKey("teamId")){
            String teamId = map.get("teamId");
            String teamStr = RedisProxy.getInstance().getRedisSession().hGet(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamId);
            XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
            if(teamData != null){
                teamData.openDayW = 0;
                teamTmpMap.put(teamId, teamData.serialize());
            }
        }else {
            Map<String, String> teamMap = RedisProxy.getInstance().getRedisSession().hGetAll(XHJZRedisKey.XHJZ_WAR_TEST_TEAM);
            for(String teamId : teamMap.keySet()){
                String teamStr = teamMap.get(teamId);
                XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
                if(teamData == null){
                    continue;
                }
                teamData.openDayW = 0;
                teamTmpMap.put(teamId, teamData.serialize());
            }
        }
        if(!teamTmpMap.isEmpty()){
            RedisProxy.getInstance().getRedisSession().hmSet(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamTmpMap, 0);
        }
    }

    public void matchTestClean(Map<String, String> map){
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        if(map.containsKey("guildId")) {
            String guildId = map.get("guildId");
            for (int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
                String teamId = getTeamId(guildId, i);
                RedisProxy.getInstance().getRedisSession().hDel(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamId);
            }
        }else if(map.containsKey("teamId")){
            String teamId = map.get("teamId");
            RedisProxy.getInstance().getRedisSession().hDel(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamId);
        }else {
            RedisProxy.getInstance().getRedisSession().del(XHJZRedisKey.XHJZ_WAR_TEST_GUILD);
            RedisProxy.getInstance().getRedisSession().del(XHJZRedisKey.XHJZ_WAR_TEST_TEAM);
        }

    }

    public void matchTestAddGuild(Map<String, String> map){
        Map<String, String> guildMap = RedisProxy.getInstance().getRedisSession().hGetAll(XHJZRedisKey.XHJZ_WAR_TEST_GUILD);
        int count = Integer.parseInt(map.getOrDefault("count", "100"));
        int size = guildMap.size();
        Map<String, String> guildTmpMap = new HashMap<>();
        for(int i = size + 1; i <= size + count; i++){
            guildTmpMap.put(String.valueOf(i), "联盟"+i);
        }
        RedisProxy.getInstance().getRedisSession().hmSet(XHJZRedisKey.XHJZ_WAR_TEST_GUILD, guildTmpMap, 0);
    }

    public void matchTestAddTeam(Map<String, String> map){
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        Map<String, String> teamMap = RedisProxy.getInstance().getRedisSession().hGetAll(XHJZRedisKey.XHJZ_WAR_TEST_TEAM);
        Map<String, String> guildMap = RedisProxy.getInstance().getRedisSession().hGetAll(XHJZRedisKey.XHJZ_WAR_TEST_GUILD);
        Map<String, String> teamTmpMap = new HashMap<>();
        if(map.containsKey("guildId")) {
            String guildId = map.get("guildId");
            if(map.containsKey("count")){
                for(int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
                    String teamId = getTeamId(guildId, i);
                    if(!teamMap.containsKey(teamId)){
                        XHJZWarTeamData teamData = new XHJZWarTeamData();
                        teamData.id = teamId;
                        teamData.name = guildMap.get(guildId) + "的"+i+"小队";
                        teamData.guildId = guildId;
                        teamTmpMap.put(teamId, teamData.serialize());
                        break;
                    }
                }
            }else {
                for(int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
                    String teamId = getTeamId(guildId, i);
                    if(teamMap.containsKey(teamId)){
                        continue;
                    }
                    XHJZWarTeamData teamData = new XHJZWarTeamData();
                    teamData.id = teamId;
                    teamData.name = guildMap.get(guildId) + "的"+i+"小队";
                    teamData.guildId = guildId;
                    teamTmpMap.put(teamId, teamData.serialize());
                }
            }
        }else {
            for(String guildId : guildMap.keySet()){
                for(int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
                    String teamId = getTeamId(guildId, i);
                    if(teamMap.containsKey(teamId)){
                        continue;
                    }
                    XHJZWarTeamData teamData = new XHJZWarTeamData();
                    teamData.id = teamId;
                    teamData.name = guildMap.get(guildId) + "的"+i+"小队";
                    teamData.guildId = guildId;
                    teamTmpMap.put(teamId, teamData.serialize());
                }
            }

        }
        if(!teamTmpMap.isEmpty()){
            RedisProxy.getInstance().getRedisSession().hmSet(XHJZRedisKey.XHJZ_WAR_TEST_TEAM, teamTmpMap, 0);
        }
    }

    public String gmFillRoomData(XHJZWarRoomData roomData, Map<String, String> teamMap){
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
        XHJZWarTeamData teamData = XHJZWarTeamData.unSerialize(teamStr);
        if(teamData == null){
            return info;
        }
        Set<String> playerIds = teamIdToPlayerIds.get(teamData.id);
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
        XHJZConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XHJZConstCfg.class);
        List<XHJZWarHistoryData> historyDataList = XHJZWarHistoryData.load(teamData.id, constCfg.getRecordPointCount());
        if(!historyDataList.isEmpty()){
            for(XHJZWarHistoryData historyData : historyDataList){
                if(teamData.id.equals(historyData.winnerId)){
                    param += constCfg.getRecordPointParam();
                }else {
                    param -= constCfg.getRecordPointParam();
                }
            }
            if(param < 1f + constCfg.getRecordPointLimitParam().first){
                param = 1f + constCfg.getRecordPointLimitParam().first;
            }
            if(param > 1f + constCfg.getRecordPointLimitParam().second){
                param = 1f + constCfg.getRecordPointLimitParam().second;
            }
        }
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

    public String gmFillGuildData(String teamStr) {
        String info = "<br>";
        return info;
    }

    /**==============================================================================================================**/
}
