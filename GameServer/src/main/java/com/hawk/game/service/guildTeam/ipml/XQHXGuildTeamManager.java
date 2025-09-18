package com.hawk.game.service.guildTeam.ipml;

import com.hawk.game.config.XQHXConstCfg;
import com.hawk.game.config.XQHXWarTimeCfg;
import com.hawk.game.module.schedule.ScheduleInfo;
import com.hawk.game.module.schedule.ScheduleService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildTeam.*;
import com.hawk.game.protocol.Schedule.ScheduleType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.Result;
import com.hawk.game.service.guildTeam.GuildTeamManagerBase;
import com.hawk.game.service.guildTeam.model.GuildTeamData;
import com.hawk.game.service.guildTeam.model.GuildTeamPlayerData;
import com.hawk.game.service.xqhxWar.XQHXWarService;
import com.hawk.game.service.xqhxWar.state.XQHXWarStateEnum;
import com.hawk.game.util.LogUtil;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;

import java.util.Objects;
import java.util.Set;

/**
 * 先驱回响小队管理器
 */
public class XQHXGuildTeamManager extends GuildTeamManagerBase {
    // 在类加载时就创建实例
    private static final XQHXGuildTeamManager instance = new XQHXGuildTeamManager();

    private XQHXGuildTeamManager(){}

    public static XQHXGuildTeamManager getInstance() {
        return instance;
    }

    @Override
    public GuildTeamType getType() {
        return GuildTeamType.XQHX_WAR;
    }

    /**
     * 参战队伍数据
     * @param teamId 小队id
     * @return 队伍数据
     */
    @Override
    public GuildTeamData getBattleTeam(String teamId) {
        return XQHXWarService.getInstance().getBattleTeam(teamId);
    }

    /**
     * 书信数据
     * @param teamData 小队数据
     */
    @Override
    public void refreshInfo(GuildTeamData teamData) {
        HawkTuple2<Long, Integer> powerAndCount = getTeamPowerAndCnt(teamData.id);
        teamData.battlePoint = powerAndCount.first;
        teamData.memberCnt = powerAndCount.second;
    }

    /**
     * 填充给前端的数据
     * @param info
     * @return
     */
    @Override
    public GuildTeamInfo.Builder fillTeamTime(GuildTeamInfo.Builder info) {
        //时间索引大于0说明已经报名
        int timeIndex = info.getTimeIndex();
        if(timeIndex > 0){
            //填充战斗时间
            long warStartTime = XQHXWarService.getInstance().getCurBattleStartTime(timeIndex);
            long warEndTime = warStartTime + HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class).getBattleTime();
            info.setBattleStartTime(warStartTime);
            info.setBattleEndtime(warEndTime);
            return info;
        }
        return info;
    }

    /**
     * 小队最大数量
     * @return 小队最大数量
     */
    @Override
    public int getTeamNumLimit() {
        return HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class).getTeamNumLimit();
    }

    /**
     * 小队名字前缀
     * @return
     */
    @Override
    public String getTeamName() {
        return "先驱";
    }

    /**
     * 报名
     * @param player 报名玩家
     * @param teamId 小队id
     * @param index 报名时间段
     */
    @Override
    public void signUp(Player player, String teamId, int index) {
        XQHXWarService.getInstance().signUp(player, teamId, index);
        //日志
        GuildTeamData data = this.getTeamData(teamId);
        int termId = XQHXWarService.getInstance().getTermId();
        Set<String> members = this.getTeamPlayerIds(teamId);
        LogUtil.logXqhxSignUp(player, termId, data.guildId, teamId, data.matchPower, Objects.isNull(members)?0:members.size(), index);
        //添加代办
        long startTime = XQHXWarService.getInstance().getBattleStartTime(termId, index);
        if(startTime > 0){
        	ScheduleInfo schedule = ScheduleInfo.createNewSchedule(ScheduleType.SCHEDULE_TYPE_2_VALUE, data.guildId, startTime, 0, 0,data.id);
        	ScheduleService.getInstance().addSystemSchedule(schedule);
        }
    }

    /**
     * 是都已经报名
     * @param teamId
     * @return
     */
    @Override
    public boolean isSignUp(String teamId) {
        return XQHXWarService.getInstance().isSignUp(teamId);
    }

    /**
     * 是否处于赛季中
     * @param teamId
     * @return
     */
    @Override
    public boolean isInSeason(String teamId) {
        //还没有赛季逻辑
        return false;
    }

    /**
     * 赛季内解散小队
     * @param teamId
     */
    @Override
    public void dissmissFromSeason(String teamId) {
        //还没有赛季逻辑
    }

    /**
     * 不检查联盟操作
     * @return
     */
    @Override
    public boolean noCheckPlayerGuildOp() {
        //不在匹配和战斗中不检查联盟操作
        XQHXWarStateEnum state = XQHXWarService.getInstance().getState();
        return state != XQHXWarStateEnum.MATCH_WAIT
                && state != XQHXWarStateEnum.MATCH
                && state != XQHXWarStateEnum.MATCH_END
                && state != XQHXWarStateEnum.BATTLE;
    }

    /**
     * 小队操作检查
     * @param player 玩家数据
     * @param req 前端请求
     * @return 是否可以操作
     */
    @Override
    public boolean checkTeamManager(Player player, GuildBattleTeamManagerReq req) {
        //匹配开始到战斗结束不能操作小队
        XQHXWarStateEnum stateEnum = XQHXWarService.getInstance().getState();
        if(stateEnum == XQHXWarStateEnum.MATCH_WAIT
                || stateEnum == XQHXWarStateEnum.MATCH
                || stateEnum == XQHXWarStateEnum.MATCH_END
                || stateEnum == XQHXWarStateEnum.BATTLE){
            return false;
        }
        //本玩法不能手动创建和删除小队
        if(req.getOpt() == GuildTeamOpt.GT_TEAM_CREATE || req.getOpt() == GuildTeamOpt.GT_TEAM_DISMISS){
            return false;
        }
        //常量配置
        XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
        //报名逻辑
        if(req.getOpt() == GuildTeamOpt.GT_CHOOSE_TIME){
            //如果已经报名返回
            if(isSignUp(req.getTeamId())){
                player.sendError(HP.code2.GUILD_BATTLE_TEAM_MANAGER_REQ_VALUE, Status.XQHXError.XQHX_HAS_CHOOSED_TIME, 0);
                return false;
            }
            //如果玩家数量不够不让报名
            Set<String> playerIds = teamIdToPlayerIds.get(req.getTeamId());
            if(playerIds == null || playerIds.size() < constCfg.getMemberSignupLimit()){
                player.sendError(HP.code2.GUILD_BATTLE_TEAM_MANAGER_REQ_VALUE, Status.XQHXError.XQHX_MEMBER_NUM_LIMIT, 0);
                return false;
            }
            //没有小队数据不让报名
            GuildTeamData teamData = teamDataMap.get(req.getTeamId());
            if(teamData == null){
                player.sendError(HP.code2.GUILD_BATTLE_TEAM_MANAGER_REQ_VALUE, Status.XQHXError.XQHX_MEMBER_NUM_LIMIT, 0);
                return false;
            }
            //已经报名，走到这说明数据可能有问题，需要看一下
            if(teamData.timeIndex > 0){
                player.sendError(HP.code2.GUILD_BATTLE_TEAM_MANAGER_REQ_VALUE, Status.XQHXError.XQHX_HAS_CHOOSED_TIME, 0);
                return false;
            }
            //排名不够不让报名
            int rank = XQHXWarService.getInstance().getRank(req.getTeamId());
            if(rank > constCfg.getSignRankLimit()){
                player.sendError(HP.code2.GUILD_BATTLE_TEAM_MANAGER_REQ_VALUE, Status.XQHXError.XQHX_RANK_LIMIT, 0);
                return false;
            }
        }
        return true;
    }

    /**
     * 成员操作检查
     * @param player 玩家数据
     * @param req 前端请求
     * @return 是否可以操作
     */
    @Override
    public boolean checkMemberManager(Player player, GuildBattleMemberManagerReq req) {
        //匹配开始到战斗结束前不能改变出战
        XQHXWarStateEnum stateEnum = XQHXWarService.getInstance().getState();
        if(stateEnum == XQHXWarStateEnum.MATCH_WAIT
                || stateEnum == XQHXWarStateEnum.MATCH
                || stateEnum == XQHXWarStateEnum.MATCH_END
                || stateEnum == XQHXWarStateEnum.BATTLE){
        	
        	player.sendError(HP.code2.GUILD_BATTLE_MEMBER_MANAGER_REQ_VALUE, Status.XQHXError.XQHX_MAMAGE_STATE_LIMIT_VALUE, 0);
            return false;
        }
        //如果玩家联盟id不对不能操作
        String guildId = player.getGuildId();
        if(HawkOSOperator.isEmptyString(guildId)){
            return false;
        }
        //小队数据为空不能操作
        GuildTeamData teamData = teamDataMap.get(req.getTeamId());
        if(teamData == null){
            return false;
        }
        //小队联盟和玩家联盟不一致不能操作
        if(!guildId.equals(teamData.guildId)){
            return false;
        }
        //产量配置
        XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
        //各角色当前数量
        HawkTuple3<Integer, Integer, Integer> cntData = getTeamAuthCnt(req.getTeamId());
        //判断配置，数量已满不能操作
        switch (req.getAuth()){
            case GT_STARTER:{
                if(cntData.second >= constCfg.getTeamMemberLimit()){
                	player.sendError(HP.code2.GUILD_BATTLE_MEMBER_MANAGER_REQ_VALUE, Status.XQHXError.XQHX_MAMAGE_TEAM_MEMBER_LIMIT_VALUE, 0);
                    return false;
                }
            }
            break;
            case GT_CANDIDATE:{
                if(cntData.third >= 0){
                    return false;
                }
            }
            break;
        }
        return true;
    }

    /**
     * 小队是否可以手动解散
     * @return
     */
    @Override
    public boolean canDissmiss() {
        return false;
    }

    /**
     * 计算各个角色人数，本玩法只有正式队员
     * @param teamId 小队id
     * @return 指挥官，正式，候补
     */
    public HawkTuple3<Integer, Integer, Integer> getTeamAuthCnt(String teamId){
        //小队玩家id
        Set<String> memberIds = teamIdToPlayerIds.get(teamId);
        //为空直接返回0
        if(memberIds == null || memberIds.isEmpty()){
            return new HawkTuple3<>(0, 0, 0);
        }
        //指挥官数量
        int commandCnt = 0;
        //正式数量
        int starterCnt = 0;
        //候补数量
        int candidateCnt = 0;
        //遍历玩家
        for(String memberId : memberIds){
            //根据角色不同计数
            GuildTeamPlayerData playerData = playerDataMap.get(memberId);
            if(playerData == null || !teamId.equals(playerData.teamId)){
                continue;
            }
            //指挥官
            if(playerData.auth == GuildTeamAuth.GT_COMMAND_VALUE){
                commandCnt++;
            }
            //正式队员
            if(playerData.auth == GuildTeamAuth.GT_STARTER_VALUE){
                starterCnt++;
            }
            //候补
            if(playerData.auth == GuildTeamAuth.GT_CANDIDATE_VALUE){
                candidateCnt++;
            }
        }
        //返回数量
        return new HawkTuple3<>(commandCnt, starterCnt, candidateCnt);
    }
    
    
    @Override
    public int checkGuildOperation(String guildId, String playerId) {
    	int rlt =  super.checkGuildOperation(guildId, playerId);
    	if(rlt != Result.SUCCESS_VALUE){
    		return Status.XQHXError.XQHX_GUILD_OPERATION_FORBID_VALUE;
    	}
    	return rlt;
    }
}
