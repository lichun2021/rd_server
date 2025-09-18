package com.hawk.game.service.xqhxWar;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
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

import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.XQHXConstCfg;
import com.hawk.game.config.XQHXPersonAwardCfg;
import com.hawk.game.config.XQHXWarTimeCfg;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.lianmengXianquhx.XQHXExtraParam;
import com.hawk.game.module.lianmengXianquhx.XQHXRoomManager;
import com.hawk.game.module.lianmengXianquhx.msg.XQHXBilingInformationMsg;
import com.hawk.game.module.lianmengXianquhx.roomstate.XQHXGameOver;
import com.hawk.game.module.schedule.ScheduleInfo;
import com.hawk.game.module.schedule.ScheduleService;
import com.hawk.game.player.Player;
import com.hawk.game.player.PowerElectric;
import com.hawk.game.protocol.CHP;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildTeam;
import com.hawk.game.protocol.GuildTeam.GuildTeamInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Schedule.ScheduleType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.XQHX;
import com.hawk.game.protocol.XQHXWar;
import com.hawk.game.protocol.XQHXWar.PBXQHXDataUpdateReq;
import com.hawk.game.protocol.XQHXWar.XQHXPageInfoResp;
import com.hawk.game.protocol.XQHXWar.XQHXState;
import com.hawk.game.protocol.XQHXWar.XQHXStateInfo;
import com.hawk.game.protocol.XQHXWar.XQHXTeamRankResp;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.guildTeam.ipml.XQHXGuildTeamManager;
import com.hawk.game.service.guildTeam.model.GuildTeamData;
import com.hawk.game.service.guildTeam.model.GuildTeamPlayerData;
import com.hawk.game.service.guildTeam.model.GuildTeamRoomData;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.tiberium.TiberiumConst;
import com.hawk.game.service.xqhxWar.model.XQHXWarGuildData;
import com.hawk.game.service.xqhxWar.model.XQHXWarHistoryData;
import com.hawk.game.service.xqhxWar.state.XQHXWarBattleStateData;
import com.hawk.game.service.xqhxWar.state.XQHXWarStateData;
import com.hawk.game.service.xqhxWar.state.XQHXWarStateEnum;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;

/**
 * 先驱回响服务类
 */
public class XQHXWarService extends HawkAppObj {
    /**
     * 构造函数
     * @param xid 唯一id
     */
    public XQHXWarService(HawkXID xid) {
        super(xid);
        instance = this;
    }

    /*******************************************************************************************************************
     * 单例
     ******************************************************************************************************************/
    private static XQHXWarService instance = null;
    public static XQHXWarService getInstance() {
        return instance;
    }
    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 状态机
     ******************************************************************************************************************/
    /**
     * 活动状态机，唯一一个，会持久化到redis里面
     */
    private XQHXWarStateData stateData;
    /**
     * 战斗状态机，配置了几个战斗时间就有几个，进入BATTLE状态时候加载，离开FINISH状态的时候清理，不持久化，BATTLE期间停服，不会重新加载
     */
    private Map<Integer, XQHXWarBattleStateData> battleStateDataMap = new ConcurrentHashMap<>();
    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 战斗数据
     * 每期活动结束后删除
     * 不仅包含本服数据还包含对手数据
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
    /**
     * 战斗期间关联小队id和玩家id
     * key:小队id
     * value:玩家is集合
     */
    private Map<String, Set<String>> teamIdToBattlePlayerIds = new ConcurrentHashMap<>();
    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 战力排行数据
     ******************************************************************************************************************/
    /**
     * 排行数据缓存缓存前100
     */
    private XQHXTeamRankResp.Builder rankResp = null;
    /**
     * 排名缓存，方便快速知道当前排名，只存前100
     */
    private Map<String, Integer> teamRank = new ConcurrentHashMap<>();
    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 初始化相关逻辑
     ******************************************************************************************************************/
    /**
     * 初始化，起服时加载数据
     * @return 初始化结果
     */
    public boolean init() {
        try {
            //初始化状态机
            stateData = new XQHXWarStateData();
            //从redis里面加载状态数据
            stateData.load();
            //获取当前状态
            XQHXWarStateEnum state = stateData.getState();
            //起服时是报名期间加载报名数据
            if(state != XQHXWarStateEnum.PEACE){
                //加载报名数据
                loadSignUp();
            }
            //如果匹配期已经结束了，需要加载房间战场数据
            if(state == XQHXWarStateEnum.MATCH_END || state == XQHXWarStateEnum.BATTLE || state == XQHXWarStateEnum.FINISH){
                //加载房间战场数据
                loadBattleRoom();
            }
            //每1秒tick逻辑
            addTickable(new HawkPeriodTickable(1000) {
                @Override
                public void onPeriodTick() {
                    onTickPerOneSecond();
                }
            });
            //每10分钟tick逻辑
            addTickable(new HawkPeriodTickable(TimeUnit.MINUTES.toMillis(10)) {
                @Override
                public void onPeriodTick() {
                    onTickPerTenMinute();
                }
            });
            return true;
        } catch (Exception e) {
            HawkException.catchException(e);
            return false;
        }
    }

    /**
     * 每1秒tick逻辑
     */
    public void onTickPerOneSecond(){
        try {
            //状态机tick
            stateData.tick();
        }catch (Exception e){
            HawkException.catchException(e);
        }
        //只有在主状态机为BATTLE时，战斗状态机才会tick
        if(stateData.getState() == XQHXWarStateEnum.BATTLE){
            for(XQHXWarBattleStateData battleStateData : battleStateDataMap.values()){
                try {
                    //战斗状态机tick
                    battleStateData.tick();
                }catch (Exception e){
                    HawkException.catchException(e);
                }
            }
        }
    }

    /**
     * 每10分钟tick逻辑
     */
    public void onTickPerTenMinute(){
        //刷新小队排行榜
    	if(GsApp.getInstance().isInitOK()){
    		if(Objects.isNull(this.rankResp)){
    			doTeamRank(true);
    		}else{
    			doTeamRank(false);
    		}
    	}
        
    }
    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 配置相关逻辑
     ******************************************************************************************************************/
    /**
     * 计算当前期数配置
     * @return 期数配置
     */
    public XQHXWarTimeCfg calCfg(){
        XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
        if(!constCfg.getOpenServerIdSet().isEmpty() && !constCfg.getOpenServerIdSet().contains(GsConfig.getInstance().getServerId())){
            return null;
        }
        //当前时间戳
        long now = HawkTime.getMillisecond();

		//开服限定时间
        long openTime = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime();
        long serverDelay = constCfg.getServerDelay();
        long openLimit = openTime + serverDelay;
        
        //拆合服时间
        String serverId = GsConfig.getInstance().getServerId();
		Long mergerTime = AssembleDataManager.getInstance().getServerMergeTime(serverId);
        //遍历配置
        ConfigIterator<XQHXWarTimeCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(XQHXWarTimeCfg.class);
        for (XQHXWarTimeCfg cfg : iterator){
        	//开服限定
        	if(openLimit >= cfg.getSignupTime()){
        		continue;
        	}
        	//拆合服限定
        	if(Objects.nonNull(mergerTime) && mergerTime >= cfg.getSignupTime() &&
        			mergerTime <= cfg.getEndTime()){
        		continue;
        	}
            //如果当前时间在报名开始和活动结束之间返回对应活动
            if(cfg.getSignupTime() <= now && cfg.getEndTime() > now){
                //返回期数配置
                return cfg;
            }
        }
        //如果没有返回空
        return null;
    }

    /**
     * 获得当期配置
     * @return 期数配置
     */
    public XQHXWarTimeCfg getTimeCfg(){
        //通过期数获得对应配置
        return HawkConfigManager.getInstance().getConfigByKey(XQHXWarTimeCfg.class, stateData.getTermId());
    }

    /**
     * 获得战斗开始时间
     * @param termId 期数
     * @param timeIndex 战斗索引
     * @return 战斗开始时间戳
     */
    public long getBattleStartTime(int termId, int timeIndex){
        //根据期数获得期数配置
        XQHXWarTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(XQHXWarTimeCfg.class, termId);
        //如果配置不存在返回一个未来时间
        if (cfg == null){
            return HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(1);
        }
        //获得常量配置
        XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
        //通过战斗索引获得当天的战斗开始时间
        long cfgTime = constCfg.getWarTime(timeIndex);
        //如果没有配置直接返回一个未来时间
        if(cfgTime < 0){
            return HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(1);
        }
        //计算战斗开始时间，战斗状态当天的0点加上常量配置的时间
        return cfg.getBattleTimeZero() + cfgTime;
    }

    /**
     * 通过战斗索引获得当期的战斗开始时间
     * @param timeIndex 战斗索引
     * @return 战斗开始时间戳
     */
    public long getCurBattleStartTime(int timeIndex){
        //通过期数获得配置
        XQHXWarTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(XQHXWarTimeCfg.class, stateData.getTermId());
        //如果配置不存在返回一个未来时间
        if (cfg == null){
            return HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(1);
        }
        //获得常量配置
        XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
        //通过战斗索引获得当天的战斗开始时间
        long cfgTime = constCfg.getWarTime(timeIndex);
        //如果没有配置直接返回一个未来时间
        if(cfgTime < 0){
            return HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(1);
        }
        //计算战斗开始时间，战斗状态当天的0点加上常量配置的时间
        return cfg.getBattleTimeZero() + cfgTime;
    }

    /**
     * 获得当期的战斗战斗状态配置时间，用于发给前端
     * @return 战斗状态配置时间
     */
    public long getCfgBattleTime(){
        //当期期数配置
        XQHXWarTimeCfg cfg = getTimeCfg();
        //如果配置为空直接返回当前时间
        if (cfg == null){
            return HawkTime.getMillisecond();
        }
        //返回时间
        return cfg.getBattleTime();
    }

    /**
     * 计算当前期数
     * @return 期数
     */
    public int calTermId(){
        //计算当前配置
        XQHXWarTimeCfg cfg = calCfg();
        //如果不存在就返回期数0
        return cfg == null ? 0 : cfg.getTermId();
    }
    /**==============================================================================================================**/

    /*******************************************************************************************************************
     * 赛制相关逻辑
     ******************************************************************************************************************/
    /**
     * 获得当前期数
     * @return 期数
     */
    public int getTermId(){
        return stateData.getTermId();
    }

    /**
     * 获得当前状态
     * @return 状态
     */
    public XQHXWarStateEnum getState(){
        return stateData.getState();
    }


    /**
     * 离开和平阶段
     */
    public void outPeace(){
        try {

        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * 报名阶段，发报名邮件
     */
    public void onSignup(){
        try {
            stateData.setTermId(calTermId());
            // 联盟管理邮件
    		List<String> guilds = GuildService.getInstance().getGuildIds();
    		for (String guildId : guilds) {
    			GuildMailService.getInstance().sendGuildMail(guildId, AuthId.ALLIANCE_MANOR_SET,
    					MailParames.newBuilder().setMailId(MailId.XQHX_20250323));
    		}
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * 匹配准备阶段，等待匹配数据上传
     */
    public void onMatchWait(){
        try {
            updateSignUpInfo();
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * 匹配阶段
     */
    public void onMatch(){
        try {
            //当前期数
            int termId = getTermId();
            HawkLog.logPrintln("XQHXWarService onMatch start, termId:{}", termId);
            //抢锁 只有个一个服能跑以下逻辑
            String serverId = GsConfig.getInstance().getServerId();
            String matchKey = String.format(XQHXWarResidKey.XQHX_WAR_MATCH, termId);
            boolean getLock = RedisProxy.getInstance().getRedisSession().setNx(matchKey, serverId);
            if (!getLock) {
                HawkLog.logPrintln("XQHXWarService onMatch get lock fail, termId:{}", termId);
                return;
            }
            //服务器id关联房间
            Map<String, Set<String>> serverIdToRoomIdMap = new HashMap<>();
            //房间数据
            Map<String, String> roomStrMap = new HashMap<>();
            //常量配置
            XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
            //分时间段匹配
            for (int i = 1; i <= constCfg.getWarCount(); i++){
                //从redis里面加载报名玩家
                String signUpTimeKey = String.format(XQHXWarResidKey.XQHX_WAR_SIGNUP_TIME, termId, i);
                Set<String> teamIds = RedisProxy.getInstance().getRedisSession().sMembers(signUpTimeKey);
                if(teamIds == null || teamIds.isEmpty()){
                    continue;
                }
                //加载报名数据
                List<GuildTeamData> teamList = XQHXGuildTeamManager.getInstance().loadTeams(teamIds);
                //排序
                teamList.sort((o1, o2) -> {
                    //开服天数
                    if (o1.openDayW != o2.openDayW) {
                        return o1.openDayW > o2.openDayW ? -1 : 1;
                    }
                    //匹配强度
                    if (o1.matchPower != o2.matchPower) {
                        return o1.matchPower > o2.matchPower ? -1 : 1;
                    }
                    return 0;
                });
                while (teamList.size() > 1){
                    //匹配对手
                    HawkTuple2<GuildTeamData, GuildTeamData> result = matchTeam(teamList);
                    //为空直接结束
                    if(result == null){
                        break;
                    }
                    //从列表中删除匹配成功的队伍
                    if(result.first != null){
                        teamList.remove(result.first);
                    }
                    if(result.second != null){
                        teamList.remove(result.second);
                    }
                    //获得小队数据
                    GuildTeamData teamData1 = result.first;
                    GuildTeamData teamData2 = result.second;
                    if(teamData1 != null && teamData2 != null){
                        //关联对手
                        teamData1.oppTeamId = teamData2.id;
                        teamData2.oppTeamId = teamData1.id;
                        //创建房间
                        GuildTeamRoomData roomData = new GuildTeamRoomData(termId, i, teamData1, teamData2);
                        //设置战场服
                        roomData.roomServerId = teamData1.serverId.compareTo(teamData2.serverId) < 0 ? teamData1.serverId : teamData2.serverId;
                        //更新数据
                        XQHXGuildTeamManager.getInstance().updateTeam(teamData1);
                        XQHXGuildTeamManager.getInstance().updateTeam(teamData2);
                        roomStrMap.put(roomData.id, roomData.serialize());
                        //关联服务器和战场
                        updateRoomIdToServer(serverIdToRoomIdMap, teamData1.serverId, roomData.id);
                        updateRoomIdToServer(serverIdToRoomIdMap, teamData2.serverId, roomData.id);
                        updateRoomIdToServer(serverIdToRoomIdMap, roomData.roomServerId, roomData.id);
                        //打印日志
                        HawkLog.logPrintln("XQHXWarService do match, roomId: {}, roomServer: {}, termId: {}, timeIndex: {}, guildA: {}, guildStrengthA:{}, serverA: {}, guildB: {},  guildStrengthB:{}, serverB: {} ",
                                roomData.id, roomData.roomServerId, termId, roomData.timeIndex, teamData1.id, teamData1.matchPower, teamData1.serverId, teamData2.id,
                                teamData2.matchPower,teamData2.serverId);
                    }
                }
            }
            //更新房间数据
            if(!roomStrMap.isEmpty()){
                RedisProxy.getInstance().getRedisSession().hmSet(String.format(XQHXWarResidKey.XQHX_WAR_ROOM, termId), roomStrMap, 0);
            }
            //更新关联数据
            for(String toServerId : serverIdToRoomIdMap.keySet()){
                Set<String> roomIds = serverIdToRoomIdMap.get(toServerId);
                RedisProxy.getInstance().getRedisSession().sAdd(String.format(XQHXWarResidKey.XQHX_WAR_ROOM_SERVER, termId, toServerId), 0, roomIds.toArray(new String[roomIds.size()]));
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * 匹配结束阶段，加载匹配结果
     */
    public void onMatchEnd(){
        try {
            //加载房间
            loadBattleRoom();
            //发送匹配结果
            sendMatchResult();
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * 进入战斗阶段，加载战斗状态机
     */
    public void onBattle(){
        //常量配置
        XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
        //初始化战斗状态机
        for (int i = 1; i <= constCfg.getWarCount(); i++){
            battleStateDataMap.put(i, new XQHXWarBattleStateData(stateData.getTermId(), i));
        }
    }

    /**
     * 战斗开始阶段，创建战场
     */
    public void onBattleOpen(int timeIndex){
        //本时间段房间id
        Set<String> roomIds = timeIndexToRoomIds.get(timeIndex);
        //为空直接返回
        if(roomIds == null || roomIds.isEmpty()){
            return;
        }
        //常量配置
        XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
        //本期时间配置
        XQHXWarTimeCfg timeCfg = getTimeCfg();
        //战斗开始时间
        long warStartTime = HawkTime.getMillisecond();
        if(timeCfg != null){
            warStartTime = getCurBattleStartTime(timeIndex);
        }
        //战斗结束时间
        long warEndTime =warStartTime + constCfg.getBattleTime();
        Map<String, String> roomStrMap = new HashMap<>();
        //创建战场
        for(String roomId : roomIds){
            //房间数据
            GuildTeamRoomData roomData = roomDataMap.get(roomId);
            //为空，时间索引不对，战场服不是本服直接跳过
            if(roomData == null || roomData.timeIndex != timeIndex || !roomData.roomServerId.equals(GsConfig.getInstance().getServerId())){
                continue;
            }
            //小队时间
            GuildTeamData teamDataA = battleTeamDataMap.get(roomData.campA);
            GuildTeamData teamDataB = battleTeamDataMap.get(roomData.campB);
            //战场参数
            XQHXExtraParam extParm = new XQHXExtraParam();
            extParm.setCampAGuild(teamDataA.guildId);
            extParm.setCampAGuildName(teamDataA.guildName);
            extParm.setCampAGuildTag(teamDataA.guildTag);
            extParm.setCampAServerId(teamDataA.serverId);
            extParm.setCampAguildFlag(teamDataA.guildFlag);
            /**-----------------------------------------------------------------------------------------------------------------------------------------------*/
            /**-----------------------------------------------------------------------------------------------------------------------------------------------*/
            extParm.setCampBGuild(teamDataB.guildId);
            extParm.setCampBGuildName(teamDataB.guildName);
            extParm.setCampBGuildTag(teamDataB.guildTag);
            extParm.setCampBServerId(teamDataB.serverId);
            extParm.setCampBguildFlag(teamDataB.guildFlag);
            //创建战场
            XQHXRoomManager.getInstance().creatNewBattle(warStartTime, warEndTime, roomData.id, extParm);
            //更新房间数据
            roomData.roomState = 1;
            roomStrMap.put(roomData.id, roomData.serialize());
        }
        //更新到redis
        RedisProxy.getInstance().getRedisSession().hmSet(String.format(XQHXWarResidKey.XQHX_WAR_ROOM, getTermId()), roomStrMap, 0);

    }

    /**
     * 战场结束
     * @param msg
     */
    @MessageHandler
    private void onBattleFinish(XQHXBilingInformationMsg msg){
        //房间id
        String roomId = msg.getRoomId();
        //房间数据
        GuildTeamRoomData roomData = roomDataMap.get(roomId);
        //为空返回
        if(roomData == null){
            return;
        }
        //如果不为空返回，说明已经结算过
        if(!HawkOSOperator.isEmptyString(roomData.winnerId)){
            return;
        }
        //当前期数
        int termId = getTermId();
        //更新房间状态，2是已经结束
        roomData.roomState = 2;
        //获胜联盟
        String winGuild = msg.getWinGuild();
        //小队数据
        GuildTeamData teamDataA = battleTeamDataMap.get(roomData.campA);
        GuildTeamData teamDataB = battleTeamDataMap.get(roomData.campB);
        //判断哪个小队获胜
        String guildA = teamDataA.guildId;
        long scoreA = msg.getGuildInfo(guildA).getHonor();
        String guildB = teamDataB.guildId;
        long scoreB = msg.getGuildInfo(guildB).getHonor();
        //战场那边如果没发获胜联盟自己判断一下
        if (HawkOSOperator.isEmptyString(winGuild)) {
            if (scoreA != scoreB) {
                winGuild = scoreA > scoreB ? guildA : guildB;
            } else {
                winGuild = teamDataA.battlePoint > teamDataB.battlePoint ? guildA : guildB;
            }
        }
        //更新房间状态，2是已经结束，写重复了，先不改
        roomData.roomState = 2;
        //更新获胜玩家
        roomData.winnerId = winGuild.equals(guildA) ? teamDataA.id : teamDataB.id;
        //更新战场分数
        roomData.scoreA = scoreA;
        roomData.scoreB = scoreB;
        //更新小队分数
        teamDataA.score = scoreA;
        teamDataB.score = scoreB;
        //更新到redis
        RedisProxy.getInstance().getRedisSession().hSet(String.format(XQHXWarResidKey.XQHX_WAR_ROOM, termId), roomData.id, roomData.serialize());
        //更新玩家分数
        Map<String, String> playerScoreMap = new HashMap<>();
        List<XQHX.PBPlayerInfo> playerInfoList = msg.getLastSyncpb().getPlayerInfoList();
        for(XQHX.PBPlayerInfo pbPlayerInfo : playerInfoList){
            playerScoreMap.put(pbPlayerInfo.getPlayerId(), String.valueOf(pbPlayerInfo.getHonor()));
        }
        //更新到redis
        RedisProxy.getInstance().getRedisSession().hmSet(String.format(XQHXWarResidKey.XQHX_WAR_PLAYER_SCORE, getTermId()), playerScoreMap, 0);
        //通知另外一个服更新房间数据
        String notifyServer = roomData.roomServerId.equals(teamDataA.serverId)?teamDataB.serverId:teamDataA.serverId;
        if(HawkOSOperator.isEmptyString(notifyServer)){
        	HawkProtocol hawkProtocol = HawkProtocol.valueOf(CHP.code.XQHX_DATA_UPDATE_VALUE, 
        			PBXQHXDataUpdateReq.newBuilder().setRoomId(roomData.id));
    		CrossProxy.getInstance().sendNotify(hawkProtocol, notifyServer, null);
        }
        try {
        	//参战人数
        	int joinBattleCntA = msg.getGuildInfo(guildA).getPlayerCount();
        	int joinBattleCntB = msg.getGuildInfo(guildB).getPlayerCount();
        	//号令点
        	int orderA = msg.getGuildInfo(guildA).getGuildOrder();
        	int orderB = msg.getGuildInfo(guildB).getGuildOrder();
        	LogUtil.logXqhxBattleOver(termId, roomData.timeIndex, guildA, teamDataA.id, teamDataA.matchPower, teamDataA.memberCnt, joinBattleCntA, scoreA, orderA, 
        			guildB, teamDataB.id, teamDataB.matchPower, teamDataB.memberCnt, joinBattleCntB, scoreB, orderB);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
    }

    /**
     * 战斗结束结束阶段，加载战场结果，并且进行发奖
     */
    public void onBattleEnd(int timeIndex){
        //加载战斗结果
        loadBattleRoomResult(timeIndex);
        //发奖
        sendAward(timeIndex);
    }


    public void onEnd(){

    }

    /**
     * 离开结束阶段，活动结束，清理状态和数据
     */
    public void outEnd(){
        clean();
    }


    /**
     * 小队排行帮
     * @param isInit 是否是起服
     */
    public void doTeamRank(boolean isInit){
        //如果不是起服阶段，只有报名阶段更新排名
        if(!isInit && stateData.getState() != XQHXWarStateEnum.SIGNUP){
            return;
        }
        //本服全部小队
        List<GuildTeamData> teamDataList = XQHXGuildTeamManager.getInstance().getAllTeamList();
        //如果为空直接返回
        if(teamDataList.isEmpty()){
            this.rankResp = XQHXWar.XQHXTeamRankResp.newBuilder();
            return;
        }
        //刷新战力
        teamDataList.forEach(data -> {
            XQHXGuildTeamManager.getInstance().refreshInfo(data);
        });
        //排序
        teamDataList.sort(((o1, o2) -> {
            if(o1.battlePoint != o2.battlePoint){
                return o1.battlePoint > o2.battlePoint ? -1 : 1;
            }
            return 0;
        }));
        //组装数据
        XQHXTeamRankResp.Builder resp = XQHXTeamRankResp.newBuilder();
        Map<String, Integer> tmpTeamRank = new ConcurrentHashMap<>();
        int i = 1;
        for(GuildTeamData data : teamDataList){
            if(i == 101){
                break;
            }
            if(data != null){
                GuildTeamInfo.Builder teamInfo = data.toPB();
                teamInfo.setRank(i);
                resp.addTeamInfos(teamInfo);
                tmpTeamRank.put(data.id, i);
                i++;
            }
        }
        //缓存下来
        this.rankResp = resp;
        this.teamRank = tmpTeamRank;
    }


    /**
     * 报名
     * @param player 玩家实体
     * @param teamId 小队id
     * @param index 索引
     */
    public void signUp(Player player, String teamId, int index) {
        //更新缓存
        signUpMap.put(teamId, index);
        //更新到redis
        RedisProxy.getInstance().getRedisSession().hSet(getSignUpServerKey(), teamId, String.valueOf(index));
    }

    /**
     * 是否已经报名
     */
    public boolean isSignUp(String teamId){
        return signUpMap.containsKey(teamId);
    }

    /**
     * 加载报名数据
     */
    public void loadSignUp(){
        try {
            //从redis加载数据
            Map<String, String> tmp = RedisProxy.getInstance().getRedisSession().hGetAll(getSignUpServerKey());
            if(tmp == null || tmp.isEmpty()){
                HawkLog.logPrintln("XQHXWarService loadSignUp data is empty");
                return;
            }
            HawkLog.logPrintln("XQHXWarService loadSignUp start size:{}", tmp.size());
            Map<String, Integer> signUpMap = new ConcurrentHashMap<>();
            for(Map.Entry<String, String> entry : tmp.entrySet()){
                try {
                    HawkLog.logPrintln("XQHXWarService loadSignUp key:{}, value:{}", entry.getKey(), entry.getValue());
                    signUpMap.put(entry.getKey(), Integer.parseInt(entry.getValue()));
                }catch (Exception e){
                    HawkException.catchException(e);
                    HawkLog.logPrintln("XQHXWarService loadSignUp error key:{}, value:{}", entry.getKey(), entry.getValue());
                }
            }
            this.signUpMap = signUpMap;
            HawkLog.logPrintln("XQHXWarService loadSignUp end");
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * 更新报名信息
     */
    public void updateSignUpInfo(){
        try {
            XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
            //获得期数
            int termId = getTermId();
            //报名联盟
            Map<String, GuildInfoObject> guildIdMap = new HashMap<>();
            //遍历报名小队
            for(String teamId : signUpMap.keySet()) {
                try{
                    //小队数据
                    GuildTeamData teamData = XQHXGuildTeamManager.getInstance().getTeamData(teamId);
                    //为空跳过
                    if(teamData == null){
                        continue;
                    }
                    //时间对不上跳过
                    if(teamData.timeIndex != signUpMap.get(teamId)){
                        continue;
                    }
                    //玩家数量不达标跳过
                    Set<String> playerIds = XQHXGuildTeamManager.getInstance().getTeamPlayerIds(teamId);
                    if(playerIds == null || playerIds.size() < constCfg.getMemberSignupLimit()){
                        continue;
                    }
                    //记录联盟信息
                    GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(teamData.guildId);
                    if(guildInfoObject != null){
                        guildIdMap.put(teamData.guildId, guildInfoObject);
                    }
                    //玩家权限信息
                    Map<String, String> playerAuthMap = new HashMap<>();
                    //刷新小队数据
                    freshTeamMatchData(teamData, playerAuthMap, playerIds, guildInfoObject);
                    //更新到redis
                    XQHXGuildTeamManager.getInstance().updateTeam(teamData);
                    //更新报名信息
                    RedisProxy.getInstance().getRedisSession().sAdd(getSignUpTimeKey(teamData.timeIndex), 0, teamData.id);
                    //如果玩家信息不为空，存redis
                    if(!playerAuthMap.isEmpty()){
                        //把数据存入redis
                        RedisProxy.getInstance().getRedisSession().hmSet(getSignUpPlayerKey(teamData.id), playerAuthMap, 0);
                    }
                    HawkLog.logPrintln("XQHXWarService flushSignerInfo, teamId: {}, teamName: {} , " +
                                    "serverId: {}, memberCnt: {}, totalPowar: {}, termId: {}, timeIndex: {}",
                            teamId, teamData.name, teamData.serverId, teamData.memberCnt, teamData.battlePoint, termId, teamData.timeIndex);
                } catch (Exception e) {
                    HawkException.catchException(e);
                }
            }
            //更新联盟信息
            updateSignUpGuild(guildIdMap.values());
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * 更新报名的联盟信息
     * @param guilds 联盟集合
     */
    public void updateSignUpGuild(Collection<GuildInfoObject> guilds){
        try {
            //如果为空直接返回
            if(guilds == null || guilds.isEmpty()){
                HawkLog.logPrintln("XQHXWarService updateSignUpGuild guildIds is empty");
                return;
            }
            HawkLog.logPrintln("XQHXWarService updateSignUpGuild start, size:{}", guilds.size());
            Map<String, String> guildDataMap = new HashMap<>();
            for(GuildInfoObject guildInfoObject : guilds){
                try {
                    //转化联盟信息
                    guildDataMap.put(guildInfoObject.getId(), new XQHXWarGuildData(guildInfoObject).serialize());
                } catch (Exception e) {
                    HawkException.catchException(e);
                    HawkLog.logPrintln("XQHXWarService updateSignUpGuild error, guildId:{}",guildInfoObject.getId());
                }
            }
            //通过期数存联盟数据
            int termId = getTermId();
            //联盟信息更新到redis
            String guildKey = String.format(XQHXWarResidKey.XQHX_WAR_GUILD, termId);
            RedisProxy.getInstance().getRedisSession().hmSet(guildKey, guildDataMap, 0);
            HawkLog.logPrintln("XQHXWarService updateSignUpGuild end");
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 加载参战联盟相关信息
     * @param guildId 联盟id
     * @return 联盟信息
     */
    public XQHXWarGuildData loadGuildData(String guildId){
        int termId = getTermId();
        String guildKey = String.format(XQHXWarResidKey.XQHX_WAR_GUILD, termId);
        String guildStr = RedisProxy.getInstance().getRedisSession().hGet(guildKey, guildId);
        return XQHXWarGuildData.unSerialize(guildStr);
    }

    public void freshTeamMatchData(GuildTeamData teamData, Map<String, String> playerAuthMap, Set<String> playerIds, GuildInfoObject guildInfoObject){
        long battlePoint = 0L;
        long matchPower = 0L;
        //玩家权限map
        for (String playerId : playerIds) {
            //玩家数据
            GuildTeamPlayerData playerData = XQHXGuildTeamManager.getInstance().getPlayerData(playerId);
            //玩家数据为空跳过
            if(playerData == null){
                continue;
            }
            //玩家小队id不对跳过
            if(!teamData.id.equals(playerData.teamId)){
                continue;
            }
            //玩家实体
            Player player = GlobalData.getInstance().makesurePlayer(playerId);
            if (player == null) {
                continue;
            }
            //强度和战力
            long playerStrength = player.getStrength();
            matchPower += playerStrength;
            long noArmyPower = player.getNoArmyPower();
            battlePoint += noArmyPower;
            //权限
            playerAuthMap.put(playerData.id, String.valueOf(playerData.auth));
        }
        //更新小队数据
        teamData.matchPower = matchPower;
        teamData.battlePoint = battlePoint;
        teamData.memberCnt = playerAuthMap.size();
        teamData.serverId = GsConfig.getInstance().getServerId();
        //更新联盟相关数据
        if(guildInfoObject != null){
            teamData.guildName = guildInfoObject.getName();
            teamData.guildTag = guildInfoObject.getTag();
            teamData.guildFlag = guildInfoObject.getFlagId();
        }
    }

    /**
     * 挑选对手
     * @param teamList 待匹配数据
     * @return 对手数据
     */
    public HawkTuple2<GuildTeamData, GuildTeamData> matchTeam(List<GuildTeamData> teamList){
        //为空返回
        if(teamList == null || teamList.isEmpty()){
            return null;
        }
        //匹配区间为6个，这里可以改成配置
        int teamCount = 6;
        teamCount = Math.min(teamCount, teamList.size());
        //把第一个拿出来，然后后面五个里面随机一个出来
        List<GuildTeamData> subList = teamList.subList(0, teamCount);
        GuildTeamData teamData1 = subList.get(0);
        List<GuildTeamData> tmpList1 = new ArrayList<>();
        List<GuildTeamData> tmpList2 = new ArrayList<>();
        for(int i = 1; i < subList.size(); i++){
            GuildTeamData tmpData = subList.get(i);
            if(tmpData.guildId.equals(teamData1.guildId)){
                continue;
            }
            if(tmpData.openDayW == teamData1.openDayW){
                tmpList1.add(tmpData);
            }else {
                tmpList2.add(tmpData);
            }
        }
        if(tmpList1.isEmpty() && tmpList2.isEmpty()){
            return new HawkTuple2<>(teamData1, null);
        }
        if(!tmpList1.isEmpty()){
            Collections.shuffle(tmpList1);
            GuildTeamData teamData2 = tmpList1.get(0);
            return new HawkTuple2<>(teamData1, teamData2);
        }else {
            Collections.shuffle(tmpList2);
            GuildTeamData teamData2 = tmpList2.get(0);
            return new HawkTuple2<>(teamData1, teamData2);
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
     * 匹配结束后加载战场数据
     */
    public void loadBattleRoom(){
        try {
            //获得期数
            int termId = getTermId();
            //房间id的key，只存了和本服相关的房间的id
            String roomServerKey = String.format(XQHXWarResidKey.XQHX_WAR_ROOM_SERVER, termId, GsConfig.getInstance().getServerId());
            //本服参与或开在本服战场的id
            Set<String> roomIds = RedisProxy.getInstance().getRedisSession().sMembers(roomServerKey);
            //如果为空直接返回
            if (roomIds == null || roomIds.isEmpty()) {
                HawkLog.logPrintln("XQHXWarService loadBattleRoom roomIds is empty");
                return;
            }
            //房间加载开始
            HawkLog.logPrintln("XQHXWarService loadBattleRoom start termId:{}, size:{}", termId, roomIds.size());
            //加载房间redis数据
            List<String> roomList = RedisProxy.getInstance().getRedisSession().hmGet(String.format(XQHXWarResidKey.XQHX_WAR_ROOM, termId), roomIds.toArray(new String[roomIds.size()]));
            //如果为空就返回，一般不会为空上面id已经校验一遍了，以防万一再检查一次
            if (roomList == null || roomList.isEmpty()) {
                HawkLog.logPrintln("XQHXWarService loadBattleRoom roomList is empty");
                return;
            }
            //参与相关战场的小队id
            Set<String> teamIds = new HashSet<>();
            //遍历房间数据
            for (String roomStr : roomList) {
                try {
                    HawkLog.logPrintln("XQHXWarService loadBattleRoom roomData:{}", roomStr);
                    //解析房间数据
                    GuildTeamRoomData roomData = GuildTeamRoomData.unSerialize(roomStr);
                    //数据为空的话跳过
                    if (roomData == null) {
                        HawkLog.logPrintln("XQHXWarService loadBattleRoom data is null roomData:{}", roomStr);
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
                    HawkLog.logPrintln("XQHXWarService loadBattleRoom error roomData:{}", roomStr);
                }
            }
            //加载参与的小队数据
            loadBattleTeam(teamIds);
            //加载参与的玩家数据
            loadBattlePlayer(teamIds);
            //房间加载结束
            HawkLog.logPrintln("XQHXWarService loadBattleRoom end");
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
                HawkLog.logPrintln("XQHXWarService loadBattleTeam teamIds is empty");
                return;
            }
            //本服id，用于判断小队是否是本服的
            String serverId = GsConfig.getInstance().getServerId();
            //加载相关小队数据
            List<GuildTeamData> list = XQHXGuildTeamManager.getInstance().loadTeams(teamIds);
            //遍历小队数据
            for(GuildTeamData teamData : list) {
                try {
                    //加入参战小队数据缓存中
                    battleTeamDataMap.put(teamData.id, teamData);
                    //如果小队是本服小队，更新本服小队数据
                    if(serverId.equals(teamData.serverId)){
                        XQHXGuildTeamManager.getInstance().updataFromBattle(teamData);
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

    /**
     * 参战成员数据
     * @param teamIds 参战队伍id
     */
    public void loadBattlePlayer(Set<String> teamIds){
        try {
            //如果为空直接返回
            if(teamIds == null || teamIds.isEmpty()){
                HawkLog.logPrintln("XQHXWarService loadBattlePlayer teamIds is empty");
                return;
            }
            //获得期数
            int termId = getTermId();
            for(String teamId : teamIds){
                try {
                    HawkLog.logPrintln("XQHXWarService loadBattlePlayer team start teamId:{}", teamId);
                    //获得当期期参与人的信息
                    String signUpPlayerKey = String.format(XQHXWarResidKey.XQHX_WAR_SIGNUP_PLAYER, termId, teamId);
                    Map<String, String> playerAuth = RedisProxy.getInstance().getRedisSession().hGetAll(signUpPlayerKey);
                    //如果为空直接跳过
                    if(playerAuth == null || playerAuth.isEmpty()){
                        HawkLog.logPrintln("XQHXWarService loadBattlePlayer playerAuth is empty, teamId:{}",teamId);
                        continue;
                    }
                    //遍历玩家
                    for(String playerId : playerAuth.keySet()){
                        try {
                            HawkLog.logPrintln("XQHXWarService loadBattlePlayer playerId:{},auth:{}", playerId, playerAuth.get(playerId));
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
                            HawkLog.logPrintln("XQHXWarService loadBattlePlayer player error playerId:{}", playerId);
                        }
                    }
                    HawkLog.logPrintln("XQHXWarService loadBattlePlayer team end teamId:{}", teamId);
                } catch (Exception e) {
                    HawkException.catchException(e);
                    HawkLog.logPrintln("XQHXWarService loadBattlePlayer team error teamId:{}", teamId);
                }
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     *是否在战斗中
     * @param playerId 玩家id
     * @return 是否战斗中
     */
    public boolean isInBattle(String playerId){
        //已经不是战斗状态直接返回否
        if(getState() != XQHXWarStateEnum.BATTLE){
            return false;
        }
        //没有房间信息返回否
        String roomId = playerIdToRoomId.get(playerId);
        if(HawkOSOperator.isEmptyString(roomId)){
            return false;
        }
        GuildTeamRoomData roomData = roomDataMap.get(roomId);
        if(roomData == null || battleStateDataMap.get(roomData.timeIndex)== null){
            return false;
        }
        //战斗状态机不是开战状态返回否
        return battleStateDataMap.get(roomData.timeIndex).getState() == XQHXWarStateEnum.BATTLE_OPEN;
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
     * 发匹配结果邮件
     */
    public void sendMatchResult(){
        try {
            for(GuildTeamRoomData roomData : roomDataMap.values()){
                try {
                    if(roomData == null){
                        continue;
                    }
                    GuildTeamData teamDataA = XQHXGuildTeamManager.getInstance().getTeamData(roomData.campA);
                    if(teamDataA != null){
                        sendMatchResult(roomData.campA, roomData.timeIndex);
                    }
                    GuildTeamData teamDataB = XQHXGuildTeamManager.getInstance().getTeamData(roomData.campB);
                    if(teamDataB != null){
                        sendMatchResult(roomData.campB, roomData.timeIndex);
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
     * 发匹配结果邮件
     */
    public void sendMatchResult(String teamId, int timeIndex){
        try {
            Set<String> playerIds = XQHXGuildTeamManager.getInstance().getTeamPlayerIds(teamId);
            if(playerIds == null || playerIds.isEmpty()){
                return;
            }
            long startTime = getBattleStartTime(getTermId(), timeIndex);
            String startData = HawkTime.formatTime(startTime);
            HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
                @Override
                public Object run() {
                    // 匹配成功邮件
                    for (String playerId : playerIds) {
                        SystemMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(playerId).setMailId(MailConst.MailId.XQHX_20250324).addContents(startData).build());
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * 获得相关战队数据，本服和对手
     * @param teamId 小队id
     * @return 战队数据
     */
    public GuildTeamData getBattleTeam(String teamId){
        return battleTeamDataMap.get(teamId);
    }

    /**
     * 获得排名
     * @param teamId
     * @return
     */
    public int getRank(String teamId){
        return teamRank.getOrDefault(teamId, 1000);
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
                HawkLog.logPrintln("XQHXWarService loadBattleRoomResult roomIds is empty, timeIndex:{}",timeIndex);
                return;
            }
            HawkLog.logPrintln("XQHXWarService loadBattleRoomResult start, timeIndex:{}",timeIndex);
            //加载Redis房间数据
            List<String> roomList = RedisProxy.getInstance().getRedisSession().hmGet(String.format(XQHXWarResidKey.XQHX_WAR_ROOM, termId), roomIds.toArray(new String[roomIds.size()]));
            //如果为空就返回，一般不会为空上面id已经校验一遍了，以防万一再检查一次
            if(roomList == null || roomList.isEmpty()){
                HawkLog.logPrintln("XQHXWarService loadBattleRoomResult roomList is empty");
                return;
            }
            HawkLog.logPrintln("XQHXWarService loadBattleRoomResult size:{}", roomList.size());
            //遍历房间数据
            for(String roomStr : roomList){
                try {
                    HawkLog.logPrintln("XQHXWarService loadBattleRoomResult roomData:{}", roomStr);
                    //解析房间数据
                    GuildTeamRoomData roomData = GuildTeamRoomData.unSerialize(roomStr);
                    //如果为空直接跳过
                    if(roomData == null){
                        HawkLog.logPrintln("XQHXWarService loadBattleRoomResult data is null roomData:{}", roomStr);
                        continue;
                    }
                    //加入房间数据缓存
                    roomDataMap.put(roomData.id, roomData);
                    //更新分数
                    updateRoomScore(roomData);
                } catch (Exception e) {
                    HawkException.catchException(e);
                    HawkLog.logPrintln("XQHXWarService loadBattleRoomResult error roomData:{}", roomStr);
                }
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * 更新战斗结果
     * @param roomData 战场数据
     */
    public void updateRoomScore(GuildTeamRoomData roomData){
        //如果战场数据为空直接返回
        if(roomData == null){
            return;
        }
        //更新阵营1战斗结果
        updateTeamScore(roomData.campA, roomData.scoreA);
        //更新阵营2战斗结果
        updateTeamScore(roomData.campB, roomData.scoreB);
    }

    /**
     * 更新小队战斗结果
     * @param teamId
     * @param score
     */
    public void updateTeamScore(String teamId, long score){
        //小队id为空直接返回
        if(HawkOSOperator.isEmptyString(teamId)){
            return;
        }
        //获得小队数据
        GuildTeamData teamData = XQHXGuildTeamManager.getInstance().getTeamData(teamId);
        if(teamData != null){
            //更新分数
            teamData.score = score;
            //更新到redis
            XQHXGuildTeamManager.getInstance().updateTeam(teamData);
        }
        //获得战斗小队数据，这里有在两个服都执行了，不过不影响结果，后续可以修改
        GuildTeamData battleTeamData = battleTeamDataMap.get(teamId);
        if(battleTeamData != null){
            //更新分数
            battleTeamData.score = score;
            //更新到redis
            XQHXGuildTeamManager.getInstance().updateTeam(battleTeamData);
        }
    }

    /**
     * 发奖
     * @param timeIndex 时间索引
     */
    public void sendAward(int timeIndex){
        int termId = getTermId();
        //遍历报名数据
        for(String teamId : signUpMap.keySet()) {
            //报名时间段
            int chooseTime = signUpMap.get(teamId);
            //如果不是同一时间直接跳过
            if(chooseTime != timeIndex){
                continue;
            }
            //确保只发奖一次
            String teamAwardKey = String.format(XQHXWarResidKey.XQHX_WAR_TEAM_AWARD, termId);
            boolean teamGetLock = RedisProxy.getInstance().getRedisSession().hSetNx(teamAwardKey, teamId, String.valueOf(HawkTime.getMillisecond())) > 0;
            if(!teamGetLock){
                continue;
            }
            //获得战场id
            String roomId = teamIdToRoomId.get(teamId);
            //战场id为空说明轮空跳过
            if(HawkOSOperator.isEmptyString(roomId)){
                HawkLog.logPrintln("XQHXWarService sendAward roomId is null teamId:{}", teamId);
                continue;
            }
            //获得战场数据
            GuildTeamRoomData roomData = roomDataMap.get(roomId);
            //战场数据为空跳过
            if(roomData == null){
                continue;
            }
            //邮件id
            MailConst.MailId mailId = null;
            boolean isWin = teamId.equals(roomData.winnerId);
            if (isWin){
                mailId = MailConst.MailId.XQHX_20250325;
            }else {
                mailId = MailConst.MailId.XQHX_20250326;
            }
            //记录历史记录
            try {
                //获得战斗双方数据
                GuildTeamData teamDataA = battleTeamDataMap.get(roomData.campA);
                GuildTeamData teamDataB = battleTeamDataMap.get(roomData.campB);
                //双方数据都不为空
                if(teamDataA != null && teamDataB != null){
                    //只存自己本服的
                    if(teamId.equals(roomData.campA)){
                        XQHXWarHistoryData historyDataA = new XQHXWarHistoryData(roomData.campA, roomData.winnerId, teamDataA, teamDataB);
                        historyDataA.update();
                    }else {
                        XQHXWarHistoryData historyDataB = new XQHXWarHistoryData(roomData.campB, roomData.winnerId, teamDataA, teamDataB);
                        historyDataB.update();
                    }
                }
            } catch (Exception e) {
                HawkException.catchException(e);
            }
            Set<String> playerIds = XQHXGuildTeamManager.getInstance().getTeamPlayerIds(teamId);
            //发玩家奖励
            for(String playerId : playerIds){
                try {
                    String playerAwardKey = String.format(XQHXWarResidKey.XQHX_WAR_PLAYER_AWARD, termId);
                    boolean playerGetLock = RedisProxy.getInstance().getRedisSession().hSetNx(playerAwardKey, playerId, String.valueOf(HawkTime.getMillisecond())) > 0;
                    if(!playerGetLock){
                        continue;
                    }
                    String scoreStr = RedisProxy.getInstance().getRedisSession().hGet(String.format(XQHXWarResidKey.XQHX_WAR_PLAYER_SCORE, getTermId()), playerId);
                    if(!HawkOSOperator.isEmptyString(scoreStr)){
                        try {
                            long playerSelfScore = Long.parseLong(scoreStr);
                            XQHXPersonAwardCfg selfCfg = getPersonAwardCfg(playerSelfScore, isWin);
                            if(selfCfg != null){
                                SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                                        .setPlayerId(playerId)
                                        .setMailId(mailId)
                                        .addContents(playerSelfScore)
                                        .addRewards(selfCfg.getRewardItem())
                                        .setAwardStatus(Const.MailRewardStatus.NOT_GET).build());
                                LogUtil.logXqhxSendAward(termId, timeIndex, teamId, teamId, playerId, roomId, isWin?1:0, playerSelfScore, selfCfg.getId());
                            }
                            
                        } catch (Exception e) {
                            HawkException.catchException(e);
                        }
                    }
                    //ActivityManager.getInstance().postEvent(new XWScoreEvent(playerId, selfScore, seasonState != CMWStateEnum.CLOSE, 0));
                } catch (Exception e) {
                    HawkException.catchException(e);
                }
            }
//            try {
//                ActivityService.getInstance().dealMsg(GameConst.MsgId.ON_GUILD_BACK_DROP, new GuildBackDropInvoker(new ArrayList<>(teamIdToPlayerIds.get(teamId))));
//            }catch (Exception e){
//                HawkException.catchException(e);
//            }
        }
    }

    public XQHXPersonAwardCfg getPersonAwardCfg(long selfScore, boolean isWin) {
        ConfigIterator<XQHXPersonAwardCfg> its = HawkConfigManager.getInstance().getConfigIterator(XQHXPersonAwardCfg.class);
        for(XQHXPersonAwardCfg cfg : its){
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
     * 清理数据
     */
    public void clean(){
        try {
            //清理小队报名数据
            XQHXGuildTeamManager.getInstance().cleanTeam();
            //清理玩家退出时间
            XQHXGuildTeamManager.getInstance().cleanPlayer();
            //清理报名数据
            cleanSignUp();
            //清理战斗数据
            cleanBattle();
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * 清理报名数据
     */
    public void cleanSignUp(){
        try {
            signUpMap.clear();
        }catch (Exception e){
            HawkException.catchException(e);
        }

    }

    /**
     * 清理战斗数据
     */
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


    /**
     * 本服报名数据rediskey
     * @return rediskey
     */
    public String getSignUpServerKey(){
        return String.format(XQHXWarResidKey.XQHX_WAR_SIGNUP_SERVER, getTermId(), GsConfig.getInstance().getServerId());
    }

    /**
     * 对应时间报名信息的rediskey
     * @param timeIndex 时间索引
     * @return rediskey
     */
    public String getSignUpTimeKey(int timeIndex){
        return String.format(XQHXWarResidKey.XQHX_WAR_SIGNUP_TIME, getTermId(), timeIndex);
    }

    /**
     * 出战玩家rediskey
     * @param teamId 小队id
     * @return rediskey
     */
    public String getSignUpPlayerKey(String teamId){
        return String.format(XQHXWarResidKey.XQHX_WAR_SIGNUP_PLAYER, getTermId(), teamId);
    }
    /**==============================================================================================================**/


    /*******************************************************************************************************************
     * 前端请求和返回
     ******************************************************************************************************************/
    /**
     * 给前端的状态信息
     * @param player
     * @return
     */
    public XQHXStateInfo.Builder getStateInfo(Player player){
        //获得当前期数配置
        XQHXWarTimeCfg cfg = getTimeCfg();
        //返回给前端的数据
        XQHXStateInfo.Builder stateInfo = XQHXStateInfo.newBuilder();
        //如果是战斗阶段
        if(stateData.getState() == XQHXWarStateEnum.BATTLE){
            //小队id
            String teamId = playerIdToTeamId.get(player.getId());
            //如果小队id不为空
            if(!HawkOSOperator.isEmptyString(teamId)){
                //小队数据
                GuildTeamData teamData = battleTeamDataMap.get(teamId);
                if(teamData != null){
                    //对应战斗状态机
                    XQHXWarBattleStateData battleStateData = battleStateDataMap.get(teamData.timeIndex);
                    if (battleStateData != null){
                        //战斗开始时间
                        long battleStartTIme = XQHXWarService.getInstance().getBattleStartTime(battleStateData.getTermId(), battleStateData.getTimeIndex());
                        switch (battleStateData.getState()){
                            //战斗等待
                            case BATTLE_WAIT:{
                                stateInfo.setState(XQHXState.XQHX_PREPARE);
                                stateInfo.setEndTime(battleStartTIme);
                            }
                            break;
                            //战斗开始
                            case BATTLE_OPEN:{
                                XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
                                stateInfo.setState(XQHXState.XQHX_BATTLE);
                                stateInfo.setEndTime(battleStartTIme + constCfg.getBattleTime());
                            }
                            break;
                            //战斗结束等待和战斗结束
                            case BATTLE_END_WAIT:
                            case BATTLE_END:{
                                stateInfo.setState(XQHXState.XQHX_FINISH);
                                if (cfg != null) {
                                    stateInfo.setEndTime(cfg.getEndTime());
                                }else {
                                    stateInfo.setEndTime(HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(1));
                                }
                            }
                            break;
                        }
                        return stateInfo;
                    }
                }
            }
        }
        switch (stateData.getState()){
            //和平阶段
            case PEACE:{
                stateInfo.setState(XQHXState.XQHX_PEACE);
            }
            break;
            //报名阶段
            case SIGNUP:{
                stateInfo.setState(XQHXState.XQHX_SIGNUP);
                if (cfg != null) {
                    stateInfo.setEndTime(cfg.getMatchWaitTime());
                }else {
                    stateInfo.setEndTime(HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(1));
                }
            }
            break;
            //匹配等待和匹配阶段
            case MATCH_WAIT:
            case MATCH:{
                stateInfo.setState(XQHXState.XQHX_MATCH_WAIT);
                if (cfg != null) {
                    stateInfo.setEndTime(cfg.getBattleTime());
                }else {
                    stateInfo.setEndTime(HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(1));
                }
            }
            break;
            //匹配结束
            case MATCH_END: {
                stateInfo.setState(XQHXState.XQHX_MATCH);
                if (cfg != null) {
                    stateInfo.setEndTime(cfg.getBattleTime());
                }else {
                    stateInfo.setEndTime(HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(1));
                }
            }
            break;
            //战斗轮空未报名和结束
            case BATTLE:
            case FINISH:{
                stateInfo.setState(XQHXState.XQHX_FINISH);
                if (cfg != null) {
                    stateInfo.setEndTime(cfg.getEndTime());
                }else {
                    stateInfo.setEndTime(HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(1));
                }
            }
            break;
        }
        return stateInfo;
    }


    /**
     * 自己的小队id
     * @param player 玩家实体
     * @return 小队id
     */
    public String getSelfTeamId(Player player){
        //本服和非本服两种情况，期数最好是没有跨服这个情况，让前端屏蔽入口为好
        if(player.isCsPlayer()){
            return playerIdToTeamId.getOrDefault(player.getId(), "");
        }else {
            return XQHXGuildTeamManager.getInstance().getSelfTeamId(player.getId());
        }
    }

    /**
     * 本联盟各小队信息
     * @param player 玩家实体
     * @return 小队列表
     */
    public List<GuildTeamInfo> getTeamInfos(Player player){
        List<GuildTeamInfo> list = new ArrayList<>();
        String guildId = player.getGuildId();
        if(HawkOSOperator.isEmptyString(guildId)){
            return list;
        }
        if(player.isCsPlayer()){
            return list;
        }else {
            return XQHXGuildTeamManager.getInstance().getTeamInfos(player);
        }
    }



    /**
     * 同步页面信息
     * @param player 玩家
     */
    public void syncPageInfo(Player player){
        //前端数据
        XQHXPageInfoResp.Builder resp = XQHXPageInfoResp.newBuilder();
        //状态信息
        resp.setStateInfo(getStateInfo(player));
        //小队信息
        resp.addAllTeamInfos(getTeamInfos(player));
        //配置信息
        resp.setCfgBattleTime(getCfgBattleTime());
        //自己小队id
        resp.setSelfTeamId(getSelfTeamId(player));
        //发给前端
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_PAGE_INFO_RESP, resp));
    }

    /**
     * 小队战力排行
     * @param player 玩家数据
     */
    public void teamRank(Player player){
        XQHXTeamRankResp.Builder resp = rankResp.clone();
        resp.setSelfTeamId(getSelfTeamId(player));
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_TEAM_RANK_RESP, resp));
    }

    /**
     * 对战历史
     * @param player 玩家数据
     */
    public void history(Player player){
        //联盟id
        String guildId = player.getGuildId();
        //为空直接返回
        if(HawkOSOperator.isEmptyString(guildId)){
            return;
        }
        //常量配置
        XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
        //历史信息
        XQHXWar.XQHXHistoryResp.Builder resp = XQHXWar.XQHXHistoryResp.newBuilder();
        //遍历各小队数据
        for(int i = 1; i <= constCfg.getTeamNumLimit(); i++) {
            //从redis加载数据
            List<XQHXWarHistoryData> historyDataList = XQHXWarHistoryData.load(XQHXGuildTeamManager.getInstance().getTeamId(player.getGuildId(), i), constCfg.getRecordPointCount());
            //组装数据
            for(XQHXWarHistoryData historyData : historyDataList){
                resp.addTeamInfos(historyData.toPB());
            }
        }
        //发给前端
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_HISTORY_RESP, resp));
    }

    /**
     * 全服在线玩家推送同步页面信息
     */
    public void syncAllPLayer(){
        for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
            try {
                syncPageInfo(player);
            }catch (Exception e){
                HawkException.catchException(e);
            }
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
                player.sendError(HP.code2.XQHX_WAR_ENTER_INSTANCE_REQ_VALUE, Status.CrossServerError.CROSS_FIGHTERING_VALUE, 0);
                return false;
            }
            String guildId = player.getGuildId();
            if (HawkOSOperator.isEmptyString(guildId)) {
                player.sendError(HP.code2.XQHX_WAR_ENTER_INSTANCE_REQ_VALUE, Status.Error.GUILD_NO_JOIN,0);
                return false;
            }
            GuildTeamPlayerData playerData = XQHXGuildTeamManager.getInstance().getPlayerData(player.getId());
            if(playerData == null){
                player.sendError(HP.code2.XQHX_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XQHXError.XQHX_NOT_IN_THIS_WAR_VALUE,0);
                return false;
            }
            if(!playerData.teamId.startsWith(guildId)){
                player.sendError(HP.code2.XQHX_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XQHXError.XQHX_NOT_IN_THIS_WAR_VALUE,0);
                return false;
            }
            int teamEnterNum = getTeamEnterNum(playerData.teamId);
            XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
            if(teamEnterNum >= constCfg.getTeamMemberLimit()){
                player.sendError(HP.code.TIBERIUM_WAR_ENTER_INSTANCE_REQ_VALUE, Status.Error.TIBERIUM_PLAYER_OVER_LIMIT_VALUE,0);
                return false;
            }
            GuildTeamRoomData roomData = roomDataMap.get(teamIdToRoomId.get(playerData.teamId));
            if(roomData == null){
                player.sendError(HP.code2.XQHX_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XQHXError.XQHX_NOT_IN_THIS_WAR_VALUE,0);
                return false;
            }
            if(roomData.timeIndex <= 0 ||stateData.getState() != XQHXWarStateEnum.BATTLE){
                player.sendError(HP.code2.XQHX_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XQHXError.XQHX_NOT_IN_THIS_WAR_VALUE, 0);
                return false;
            }
            if(battleStateDataMap.get(roomData.timeIndex).getState() != XQHXWarStateEnum.BATTLE_OPEN){
                player.sendError(HP.code2.XQHX_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XQHXError.XQHX_NOT_IN_THIS_WAR_VALUE, 0);
                return false;
            }
            long now = HawkTime.getMillisecond();
            XQHXWarTimeCfg timeCfg = getTimeCfg();
            long warStartTime = HawkTime.getMillisecond();
            if(timeCfg != null){
                warStartTime = getCurBattleStartTime(roomData.timeIndex);
            }
            long warEndTime =warStartTime + constCfg.getBattleTime();
            if(now < warStartTime || now > warEndTime - TimeUnit.MINUTES.toMillis(5)){
                player.sendError(HP.code2.XQHX_WAR_ENTER_INSTANCE_REQ_VALUE, Status.Error.TIBERIUM_ROOM_NEAR_CLOSE_VALUE, 0);
                return false;
            }
            if(playerData.auth == GuildTeam.GuildTeamAuth.GT_CANDIDATE_VALUE && now < warStartTime + TimeUnit.MINUTES.toMillis(5)){
                player.sendError(HP.code2.XQHX_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XQHXError.XQHX_CANDIDATE_NOT_IN_TIME, 0);
                return false;
            }
            GuildTeamPlayerData redisData = XQHXGuildTeamManager.getInstance().load(player.getId());
            if(redisData.quitTIme > warStartTime && redisData.quitTIme < warEndTime){
                player.sendError(HP.code2.XQHX_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XQHXError.XQHX_HAS_JOINED_WAR_VALUE, 0);
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
            String teamEnterKey = String.format(XQHXWarResidKey.XQHX_WAR_TEAM_ENTER, getTermId(), teamId);
            RedisProxy.getInstance().getRedisSession().increaseBy(teamEnterKey, add, TiberiumConst.TLW_EXPIRE_SECONDS);
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    public int getTeamEnterNum(String teamId){
        try {
            String teamEnterKey = String.format(XQHXWarResidKey.XQHX_WAR_TEAM_ENTER, getTermId(), teamId);
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
     * 进入战场
     * @param player 玩家
     * @return 执行结果
     */
    public boolean joinRoom(Player player) {
        String roomId = playerIdToRoomId.get(player.getId());
        GuildTeamRoomData roomData = roomDataMap.get(roomId);
        if (!XQHXRoomManager.getInstance().hasGame(roomId)) {
            return false;
        }
        if (!XQHXRoomManager.getInstance().joinGame(roomData.id, player)) {
            return false;
        }
        updateTeamEnterNum(playerIdToTeamId.get(player.getId()), 1L);
        GuildTeamPlayerData playerData = XQHXGuildTeamManager.getInstance().load(player.getId());
        LogUtil.logXqhxJoinBattle(player, roomData.termId, playerData.guildId, playerData.teamId, roomId);
        return true;
    }

    /**
     * 退出战场
     * @param player 玩家
     * @param isMidwayQuit 是否中途退出
     * @return 执行结果
     */
    public boolean quitRoom(Player player, boolean isMidwayQuit){
        GuildTeamPlayerData playerData = XQHXGuildTeamManager.getInstance().load(player.getId());
        playerData.quitTIme = HawkTime.getMillisecond();
        XQHXGuildTeamManager.getInstance().updatePlayer(playerData);
        int termId = this.getTermId();
        String roomId = playerIdToRoomId.get(player.getId());
        LogUtil.logXqhxExitBattle(player, termId, playerData.guildId, playerData.teamId, roomId);
        return true;
    }
    
    
    
    
    
    /**
     * 数据更新
     * @param hawkProtocol
     */
    @ProtocolHandler(code = CHP.code.XQHX_DATA_UPDATE_VALUE)
	public void onXQHXDataUpdate(HawkProtocol hawkProtocol) {
    	PBXQHXDataUpdateReq req = hawkProtocol.parseProtocol(PBXQHXDataUpdateReq.getDefaultInstance());
    	String roomId = req.getRoomId();
    	if(HawkOSOperator.isEmptyString(roomId)){
    		return;
    	}
    	GuildTeamRoomData roomData = roomDataMap.get(roomId);
        if(roomData == null){
            return;
        }
        if(roomData.roomState == 2){
        	return;
        }
        int tindex = roomData.timeIndex;
        XQHXWarBattleStateData stateData =  this.battleStateDataMap.get(tindex);
        if(stateData.getState() != XQHXWarStateEnum.BATTLE){
        	return;
        }
        int termId = this.getTermId();
        String roomStr = RedisProxy.getInstance().getRedisSession().hGet(String.format(XQHXWarResidKey.XQHX_WAR_ROOM, termId),roomId);
        if(HawkOSOperator.isEmptyString(roomStr)){
        	return;
        }
       
        GuildTeamRoomData redisData = GuildTeamRoomData.unSerialize(roomStr);
        if(roomData.roomState != 2){
        	return;
        }
        this.updateRoomScore(redisData);
    }
    
    
    
    
    
    
    /**==============================================================================================================**/


    /*******************************************************************************************************************
     * 战场相关方法
     ******************************************************************************************************************/
    /**
     * gm入口
     * @param map gm参数
     * @return 活动信息
     */
    public String gm(Map<String, String> map){
        String cmd = map.getOrDefault("cmd", "");
        switch (cmd) {
            case "info":{
                return printInfo();
            }
            case "next":{
                if(stateData.getState() == XQHXWarStateEnum.BATTLE){
                    XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
                    boolean isDone = false;
                    for(int i = 1; i <= constCfg.getWarCount(); i++){
                        if(battleStateDataMap.get(i).getState() != XQHXWarStateEnum.BATTLE_END){
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
                if(stateData.getState() == XQHXWarStateEnum.BATTLE){
                    if(map.containsKey("timeIndex")){
                        int timeIndex = Integer.parseInt(map.get("timeIndex"));
                        battleStateDataMap.get(timeIndex).toNext();
                    }else {
                        for(int i = 1; i <= 4; i++){
                            if(battleStateDataMap.get(i).getState() != XQHXWarStateEnum.BATTLE_END){
                                battleStateDataMap.get(i).toNext();
                                break;
                            }
                        }
                    }
                }
                return printInfo();
            }
            //匹配
            case "onMatch":{
                onMatch();
                return printInfo();
            }
            //加载房间数据
            case "loadBattleRoom":{
                loadBattleRoom();
                return printInfo();
            }
            //战斗结束
            case "battleOver":{
                XQHXRoomManager.getInstance().findAllRoom().forEach(room -> room.setState(new XQHXGameOver(room)));
                return printInfo();
            }
            case "sendAward":{
                if(map.containsKey("timeIndex")){
                    int timeIndex = Integer.parseInt(map.get("timeIndex"));
                    sendAward(timeIndex);
                }else {
                    sendAward(1);
                }
                return printInfo();
            }
            case "doTeamRank":{
                doTeamRank(true);
                return printInfo();
            }
            case "addTermId":{
                if(getState() != XQHXWarStateEnum.SIGNUP){
                    return printInfo();
                }
                int add = Integer.parseInt(map.get("add"));
                if(add == 0){
                    stateData.setTermId(0);
                }else {
                    stateData.setTermId(stateData.getTermId() + add);
                }
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
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=info\">刷新</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=next\">切阶段</a>          ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=bigNext\">切大阶段</a>         ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=battleNext&timeIndex=1\">切战场1阶段</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=battleNext&timeIndex=2\">切战场2阶段</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=battleNext&timeIndex=3\">切战场3阶段</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=battleNext&timeIndex=4\">切战场4阶段</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=battleOver\">结束战斗</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=doTeamRank\">刷新排行榜</a>           ";
//            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=addAllTeam\">战队加满</a>           ";
//            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=allSignFirst\">所有战队报名时间1</a>           ";
//            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=allOneSignFirst\">所有1队报名时间1</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=addTermId&add=1\">期数加1</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=addTermId&add=5\">期数加5</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=addTermId&add=10\">期数加10</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=addTermId&add=100\">期数加100</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=addTermId&add=1000\">期数加1000</a>           ";
            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=addTermId&add=0\">期数清0</a>           ";
//            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=matchTestInfo\">匹配测试</a>           ";
//            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=signupInfo\" target=\"_blank\">报名信息</a>           ";
//            info +="<a href=\"http://"+ipAddress+":"+port+"/script/whcgm?opt=XQHXGM&cmd=matchInfo\" target=\"_blank\">战场信息</a>           ";
            info +="<br><br>";
            info += stateData.toString() + "<br>";
            for(XQHXWarBattleStateData battleStateData : battleStateDataMap.values()){
                info += battleStateData.toString() + "<br>";
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
        return info;
    }
    /**==============================================================================================================**/
}
