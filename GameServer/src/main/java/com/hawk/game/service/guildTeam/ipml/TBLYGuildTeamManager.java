package com.hawk.game.service.guildTeam.ipml;

import com.hawk.game.config.TiberiumConstCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildTeam.*;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Rank;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.TiberiumWar;
import com.hawk.game.protocol.Const.Result;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.guildTeam.GuildTeamManagerBase;
import com.hawk.game.service.guildTeam.model.GuildTeamData;
import com.hawk.game.service.guildTeam.model.GuildTeamPlayerData;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import com.hawk.game.service.tblyTeam.state.TBLYWarStateEnum;
import com.hawk.game.service.tiberium.TWPlayerData;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TBLYGuildTeamManager extends GuildTeamManagerBase {

    // 在类加载时就创建实例
    private static final TBLYGuildTeamManager instance = new TBLYGuildTeamManager();

    // 私有构造函数，防止外部实例化
    private TBLYGuildTeamManager() {}

    // 提供公共方法获取实例
    public static TBLYGuildTeamManager getInstance() {
        return instance;
    }

    @Override
    public GuildTeamType getType() {
        return GuildTeamType.TBLY_WAR;
    }

    @Override
    public GuildTeamData getBattleTeam(String teamId) {
        if(TBLYSeasonService.getInstance().isInSeason(teamId)){
            return TBLYSeasonService.getInstance().getBattleTeam(teamId);
        }else {
            return TBLYWarService.getInstance().getBattleTeam(teamId);
        }
    }

    @Override
    public void signUp(Player player, String teamId, int index) {
        TBLYWarService.getInstance().signUp(player, teamId, index);
    }

    public List<TWPlayerData> getAllTWPlayerData(Collection<String> playerIds){
        List<TWPlayerData> result = new ArrayList<>(playerIds.size());
        List<String> list = RedisProxy.getInstance().getRedisSession().hmGet(getPlayerKey(), playerIds.toArray(new String[playerIds.size()]));
        for(String str : list) {
            if (HawkOSOperator.isEmptyString(str)) {
                HawkLog.logPrintln("GuildTeamManager {} getAllTWPlayerData data is null playerData:{}",getType(), str);
                continue;
            }
            try {
                GuildTeamPlayerData playerData = GuildTeamPlayerData.unSerialize(str);
                if(playerData == null){
                    HawkLog.logPrintln("GuildTeamManager {} getAllTWPlayerData data is null playerData:{}",getType(), str);
                    continue;
                }
                result.add(playerData.toTWPlayerData());
            } catch (Exception e) {
                HawkException.catchException(e);
                HawkLog.logPrintln("GuildTeamManager {} getAllTWPlayerData error playerData:{}",getType(), str);
            }
        }
        return result;
    }

    @Override
    public void refreshInfo(GuildTeamData teamData) {
        HawkTuple2<Long, Integer> powerAndCount = getTeamPowerAndCnt(teamData.id);
        teamData.battlePoint = powerAndCount.first;
        teamData.memberCnt = powerAndCount.second;
    }
    
    
    

    @Override
    public GuildTeamInfo.Builder fillTeamTime(GuildTeamInfo.Builder info) {
        boolean isInSesson = TBLYSeasonService.getInstance().isInSeason(info.getId());
        if(isInSesson){
            info.setIsInSeason(true);
            return info;
        }else {
            int timeIndex = info.getTimeIndex();
            if(timeIndex > 0){
                List<TiberiumWar.WarTimeChoose> timeList = TBLYWarService.getInstance().getChooses();
                if(timeList.size() >= timeIndex){
                    TiberiumWar.WarTimeChoose battleTime = timeList.get(timeIndex-1);
                    long warStartTime = battleTime.getTime();
                    long warEndTime = warStartTime + TiberiumConstCfg.getInstance().getWarOpenTime();
                    info.setBattleStartTime(warStartTime);
                    info.setBattleEndtime(warEndTime);
                    return info;
                }
            }
            long startTime = HawkTime.getMillisecond() + TimeUnit.MINUTES.toMillis(30);
            long endTime = HawkTime.getMillisecond() + TimeUnit.MINUTES.toMillis(60);
            info.setBattleStartTime(startTime);
            info.setBattleEndtime(endTime);
            return info;
        }
    }

    @Override
    public int getTeamNumLimit() {
        return TiberiumConstCfg.getInstance().getTeamNumLimit();
    }

    @Override
    public boolean isSignUp(String teamId) {
        return TBLYWarService.getInstance().isSignUp(teamId);
    }

    @Override
    public boolean isInSeason(String teamId) {
        return TBLYSeasonService.getInstance().isInSeason(teamId);
    }

    @Override
    public boolean noCheckPlayerGuildOp() {
        TBLYWarStateEnum seasonState = TBLYSeasonService.getInstance().getState();
        TBLYWarStateEnum state = TBLYWarService.getInstance().getState();
        return seasonState != TBLYWarStateEnum.SEASON_WAR_WAIT
                && seasonState != TBLYWarStateEnum.SEASON_WAR_OPEN
                && state != TBLYWarStateEnum.MATCH_WAIT
                && state != TBLYWarStateEnum.MATCH
                && state != TBLYWarStateEnum.MATCH_END
                && state != TBLYWarStateEnum.BATTLE;
    }

    @Override
    public void dissmissFromSeason(String teamId) {
        TBLYSeasonService.getInstance().dissmissFromSeason(teamId);
    }

    @Override
    public boolean checkTeamManager(Player player, GuildBattleTeamManagerReq req) {
        TBLYWarStateEnum stateEnum = TBLYWarService.getInstance().getState();
        if(stateEnum == TBLYWarStateEnum.MATCH_WAIT
                || stateEnum == TBLYWarStateEnum.MATCH
                || stateEnum == TBLYWarStateEnum.MATCH_END
                || stateEnum == TBLYWarStateEnum.BATTLE){
            return false;
        }
        if(!canDissmiss() && (req.getOpt() == GuildTeamOpt.GT_TEAM_CREATE || req.getOpt() == GuildTeamOpt.GT_TEAM_DISMISS)){
            return false;
        }
        if(TBLYSeasonService.getInstance().isInSeason(req.getTeamId()) && req.getOpt() == GuildTeamOpt.GT_CHOOSE_TIME){
            return false;
        }
        if(req.getOpt() == GuildTeamOpt.GT_TEAM_CREATE){
            int teamIndex = 0;
            int nameLength = req.getName().length();
            String[] nameSplit = TiberiumConstCfg.getInstance().getTeamNameNumLimit().split("_");
            if(nameLength < Integer.parseInt(nameSplit[0]) || nameLength > Integer.parseInt(nameSplit[1]) ){
                return false;
            }
            for(int i = 1; i <= getTeamNumLimit(); i++) {
                GuildTeamData tmp = teamDataMap.get(getTeamId(player.getGuildId(), i));
                if(teamIndex == 0 && tmp == null){
                    teamIndex = i;
                }
            }
            if(teamIndex <= 0){
                return false;
            }
        }
        if(req.getOpt() == GuildTeamOpt.GT_CHOOSE_TIME){
            if(isSignUp(req.getTeamId())){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_HAS_CHOOSED_TIME, 0);
                return false;
            }
            Set<String> playerIds = teamIdToPlayerIds.get(req.getTeamId());
            if(playerIds == null || playerIds.size() < TiberiumConstCfg.getInstance().getWarMemberMinCnt()){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_MEMBER_NUM_LIMIT, 0);
                return false;
            }
            GuildTeamData teamData = teamDataMap.get(req.getTeamId());
            if(teamData == null){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_MEMBER_NUM_LIMIT, 0);
                return false;
            }
            if(teamData.timeIndex > 0){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_HAS_CHOOSED_TIME, 0);
                return false;
            }
            Rank.RankInfo rankInfo = RankService.getInstance().getRankInfo(Rank.RankType.ALLIANCE_FIGHT_KEY, teamData.guildId);
            if(rankInfo == null || rankInfo.getRank() > TiberiumConstCfg.getInstance().getSignRankLimit()){
                player.sendError(HP.code2.XHJZ_WAR_ENTER_INSTANCE_REQ_VALUE, Status.XHJZError.XHJZ_RANK_LIMIT, 0);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean checkMemberManager(Player player, GuildBattleMemberManagerReq req) {
        TBLYWarStateEnum seasonStateEnum = TBLYSeasonService.getInstance().getState();
        if(seasonStateEnum == TBLYWarStateEnum.SEASON_WAR_WAIT || seasonStateEnum == TBLYWarStateEnum.SEASON_WAR_OPEN){
            player.sendError(HP.code.TIBERIUM_WAR_TEAM_MEMBER_MANAGE_C_VALUE, Status.Error.TIBERIUM_LEAGUA_CANNOT_MANAGE_TEAM, 0);
            return false;
        }
        TBLYWarStateEnum stateEnum = TBLYWarService.getInstance().getState();
        if(stateEnum == TBLYWarStateEnum.MATCH_WAIT
                || stateEnum == TBLYWarStateEnum.MATCH
                || stateEnum == TBLYWarStateEnum.MATCH_END
                || stateEnum == TBLYWarStateEnum.BATTLE){
            return false;
        }
        String guildId = player.getGuildId();
        if(HawkOSOperator.isEmptyString(guildId)){
            return false;
        }
        GuildTeamData teamData = teamDataMap.get(req.getTeamId());
        if(teamData == null){
            return false;
        }
        if(!guildId.equals(teamData.guildId)){
            return false;
        }
        TiberiumConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(TiberiumConstCfg.class);
        HawkTuple3<Integer, Integer, Integer> cntData = getTeamAuthCnt(req.getTeamId());
        switch (req.getAuth()){
            case GT_STARTER:{
                if(cntData.second >= constCfg.getTeamMemberLimit()){
                    return false;
                }
                boolean limit = TBLYSeasonService.getInstance().teamManageLimit(player.getId(), req.getTeamId());
                if(limit){
                	player.sendError(HP.code.TIBERIUM_WAR_TEAM_MEMBER_MANAGE_C_VALUE, Status.Error.TIBERIUM_LEAGUA_JOIN_WIN_VALUE, 0);
                	return false;
                }
            }
            break;
            case GT_CANDIDATE:{
                if(cntData.third >= constCfg.getTeamPreparationLimit()){
                    return false;
                }
                boolean limit = TBLYSeasonService.getInstance().teamManageLimit(player.getId(), req.getTeamId());
                if(limit){
                	player.sendError(HP.code.TIBERIUM_WAR_TEAM_MEMBER_MANAGE_C_VALUE, Status.Error.TIBERIUM_LEAGUA_JOIN_WIN_VALUE, 0);
                	return false;
                }
            }
            break;
        }
        return true;
    }

    @Override
    public boolean canDissmiss() {
        return false;
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
            GuildTeamPlayerData playerData = playerDataMap.get(memberId);
            if(playerData == null || !teamId.equals(playerData.teamId)){
                continue;
            }
            if(playerData.auth == GuildTeamAuth.GT_COMMAND_VALUE){
                commandCnt++;
            }
            if(playerData.auth == GuildTeamAuth.GT_STARTER_VALUE){
                starterCnt++;
            }
            if(playerData.auth == GuildTeamAuth.GT_CANDIDATE_VALUE){
                candidateCnt++;
            }
        }
        return new HawkTuple3<>(commandCnt, starterCnt, candidateCnt);
    }
    
    @Override
    public int checkGuildOperation(String guildId, String playerId) {
    	int rlt =  super.checkGuildOperation(guildId, playerId);
    	if(rlt != Result.SUCCESS_VALUE){
    		return Status.Error.TIBERIUM_GUILD_OPERATION_FORBID_VALUE;
    	}
    	return rlt;
    }
}
