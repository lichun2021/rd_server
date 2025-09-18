package com.hawk.game.service.guildTeam;

import com.hawk.game.GsConfig;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.Result;
import com.hawk.game.protocol.GuildTeam.*;
import com.hawk.game.protocol.HP;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.guildTeam.model.GuildTeamData;
import com.hawk.game.service.guildTeam.model.GuildTeamPlayerData;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import com.hawk.game.util.LogUtil;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class GuildTeamManagerBase {
    /**
     * 小队数据
     * key:小队id
     * value:小队数据
     */
    public Map<String, GuildTeamData> teamDataMap = new ConcurrentHashMap<>();
    /**
     * 玩家数据
     * key:玩家id
     * value:玩家数据
     */
    public Map<String, GuildTeamPlayerData> playerDataMap = new ConcurrentHashMap<>();
    /**
     * 通过小队id索引玩家id
     * key:小队id
     * value:玩家id集合
     */
    public Map<String, Set<String>> teamIdToPlayerIds = new ConcurrentHashMap<>();

    public abstract GuildTeamType getType();

    public String getTeamKey(){
        return getType().name()+":TEAM";
    }

    public String getPlayerKey(){
        return getType().name()+":PLAYER";
    }

    public String getTeamPlayerKey(){
        return getType().name()+":TEAM_PLAYER:%s";
    }

    public void init(){
        loadTeam();
        loadTeamPlayer();
    }

    /**
     * 加载小队数据
     */
    public void loadTeam(){
        try {
            Set<String> teamIds = new HashSet<>();
            List<String> guildList = GuildService.getInstance().getGuildIds();
            for(String guildId : guildList){
                for(int i = 1; i <= this.getTeamNumLimit(); i++) {
                    teamIds.add(getTeamId(guildId, i));
                }
            }
            if(teamIds.isEmpty()){
                HawkLog.logPrintln("GuildTeamManager {} loadTeam teamIds is empty", getType());
                return;
            }
            HawkLog.logPrintln("GuildTeamManager {} loadTeam start size:{}", getType(), teamIds.size());
            List<String> list = RedisProxy.getInstance().getRedisSession().hmGet(getTeamKey(), teamIds.toArray(new String[teamIds.size()]));
            Map<String, String> mergerTeamMap = new HashMap<>();
            for(String str : list) {
                if (HawkOSOperator.isEmptyString(str)) {
                    continue;
                }
                try {
                    HawkLog.logPrintln("GuildTeamManager {} loadTeam teamData:{}",getType() , str);
                    GuildTeamData teamData = GuildTeamData.unSerialize(str);
                    if(teamData == null){
                        HawkLog.logPrintln("GuildTeamManager {} loadTeam data is null teamData:{}",getType(), str);
                        continue;
                    }
                    try {
                        GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(teamData.guildId);
                        if(guildInfoObject != null){
                            teamData.guildName = guildInfoObject.getName();
                            teamData.guildTag = guildInfoObject.getTag();
                            teamData.guildFlag = guildInfoObject.getFlagId();
                        }
                        if(!GsConfig.getInstance().getServerId().equals(teamData.serverId)){
                            HawkLog.logPrintln("GuildTeamManager {} loadTeam severId is diff teamData:{}",getType(), str);
                            teamData.serverId = GsConfig.getInstance().getServerId();
                            mergerTeamMap.put(teamData.id, teamData.serialize());
                        }
                    } catch (Exception e) {
                        HawkException.catchException(e);
                    }
                    teamDataMap.put(teamData.id, teamData);
                    teamIdToPlayerIds.put(teamData.id, new CopyOnWriteArraySet<>());
                } catch (Exception e) {
                    HawkException.catchException(e);
                    HawkLog.logPrintln("GuildTeamManager {} loadTeam error teamData:{}",getType(), str);
                }
            }
            if(!mergerTeamMap.isEmpty()){
                RedisProxy.getInstance().getRedisSession().hmSet(getTeamKey(), mergerTeamMap, 0);
            }
            HawkLog.logPrintln("GuildTeamManager {} loadTeam end",getType());
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * 加载玩家数据
     */
    public void loadTeamPlayer(){
        try {
            for(String teamId : teamIdToPlayerIds.keySet()){
                String teamPlayerKey = String.format(getTeamPlayerKey(), teamId);
                Set<String> playerIds = RedisProxy.getInstance().getRedisSession().sMembers(teamPlayerKey);
                if(playerIds == null || playerIds.isEmpty()){
                    HawkLog.logPrintln("GuildTeamManager {} loadTeamPlayer playerIds is empty, teamId:{}",getType(), teamId);
                    continue;
                }
                HawkLog.logPrintln("GuildTeamManager {} loadTeamPlayer start, teamId:{}, size:{}",getType(), teamId, playerIds.size());
                List<String> list = RedisProxy.getInstance().getRedisSession().hmGet(getPlayerKey(), playerIds.toArray(new String[playerIds.size()]));
                for(String str : list) {
                    if (HawkOSOperator.isEmptyString(str)) {
                        continue;
                    }
                    try {
                        HawkLog.logPrintln("GuildTeamManager {} loadTeamPlayer playerData:{}",getType(), str);
                        GuildTeamPlayerData playerData = GuildTeamPlayerData.unSerialize(str);
                        if(playerData == null){
                            HawkLog.logPrintln("GuildTeamManager {} loadTeamPlayer data is null playerData:{}",getType(), str);
                            continue;
                        }
                        if(HawkOSOperator.isEmptyString(playerData.teamId)){
                            playerDataMap.put(playerData.id, playerData);
                        }else {
                            if(teamId.equals(playerData.teamId)){
                                if(teamDataMap.containsKey(playerData.teamId)){
                                    playerDataMap.put(playerData.id, playerData);
                                    teamIdToPlayerIds.get(playerData.teamId).add(playerData.id);
                                }
                            }
                        }
                    } catch (Exception e) {
                        HawkException.catchException(e);
                        HawkLog.logPrintln("GuildTeamManager {} loadTeamPlayer error playerData:{}",getType(), str);
                    }

                }
                HawkLog.logPrintln("GuildTeamManager {} loadTeamPlayer end, teamId:{}",getType(), teamId);
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    public void onGuildDismiss(String guildId) {
        for(int i = 1; i <= getTeamNumLimit(); i++) {
            String teamId = getTeamId(guildId, i);
            GuildTeamData teamData = teamDataMap.remove(teamId);
            if(teamData == null){
                continue;
            }
            Set<String> playerIds = teamIdToPlayerIds.remove(teamData.id);
            updateTeamDissmiss(teamData);
            updatePlayerDissmiss(playerIds);
            dissmissFromSeason(teamId);
        }
    }

    public void onQuitGuild(Player player){
        GuildTeamPlayerData playerData = playerDataMap.get(player.getId());
        if(playerData != null){
            String oldTeamId = playerData.teamId;
            playerData.teamId = "";
            playerData.auth = GuildTeamAuth.GT_NO_TEAM_VALUE;
            if(!HawkOSOperator.isEmptyString(oldTeamId)){
                teamIdToPlayerIds.get(oldTeamId).remove(playerData.id);
                String teamPlayerKey = String.format(getTeamPlayerKey(), oldTeamId);
                RedisProxy.getInstance().getRedisSession().sRem(teamPlayerKey, playerData.id);
            }
        }
    }

    public void updateTeamDissmiss(GuildTeamData teamData){
        removeTeam(teamData);
        String teamPlayerKey = String.format(getTeamPlayerKey(), teamData.id);
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
                HawkLog.logPrintln("GuildTeamManager updatePlayerDissmiss playerIds is empty");
                return;
            }
            HawkLog.logPrintln("GuildTeamManager updatePlayerDissmiss start，size:{}", playerIds.size());
            for(String playerId : playerIds){
                try {
                    GuildTeamPlayerData playerData = playerDataMap.get(playerId);
                    if(playerData == null){
                        HawkLog.logPrintln("GuildTeamManager updatePlayerDissmiss playerData is null, playerId:{}",playerId);
                        continue;
                    }
                    playerData.teamId = "";
                    playerData.auth = GuildTeamAuth.GT_NO_TEAM_VALUE;
                    updatePlayer(playerData);
                }catch (Exception e){
                    HawkException.catchException(e);
                    HawkLog.logPrintln("GuildTeamManager updatePlayerDissmiss player error, playerId:{}",playerId);
                }
            }
            HawkLog.logPrintln("GuildTeamManager updatePlayerDissmiss end");
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    public void teamManager(Player player, GuildBattleTeamManagerReq req){
        if(!player.hasGuild()){
            return;
        }
        GuildTeamData teamData = null;
        switch (req.getOpt()){
            //创建小队
            case GT_TEAM_CREATE:{
                int teamIndex = 0;
                for(int i = 1; i <= getTeamNumLimit(); i++) {
                    GuildTeamData tmp = teamDataMap.get(getTeamId(player.getGuildId(), i));
                    if(teamIndex == 0 && tmp == null){
                        teamIndex = i;
                    }
                }
                if(teamIndex > 0){
                    GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(player.getGuildId());
                    teamData = new GuildTeamData(guildInfoObject, teamIndex);
                    teamData.name = req.getName();
                    teamDataMap.put(teamData.id, teamData);
                    teamIdToPlayerIds.put(teamData.id, new CopyOnWriteArraySet<>());
                    updateTeam(teamData);
                }
            }
            break;
            //小队改名
            case GT_CHANGE_NAME:{
                teamData = teamDataMap.get(req.getTeamId());
                if(teamData != null){
                    teamData.name = req.getName();
                    updateTeam(teamData);
                }
            }
            break;
            //小队选时间报名
            case GT_CHOOSE_TIME:{
                teamData = teamDataMap.get(req.getTeamId());
                if(teamData != null){
                    teamData.timeIndex = req.getTimeIndex();
                    updateTeam(teamData);
                    signUp(player, teamData.id, teamData.timeIndex);
                }
            }
            break;
            //小队解散
            case GT_TEAM_DISMISS:{
                teamData = teamDataMap.remove(req.getTeamId());
                Set<String> playerIds = teamIdToPlayerIds.remove(teamData.id);
                removeTeam(teamData);
                updateTeamDissmiss(teamData);
                updatePlayerDissmiss(playerIds);
            }
            break;

        }
        //如果小队数据不为空同步给前端
        if(teamData != null){
            refreshInfo(teamData);
            GuildBattleTeamManagerResp.Builder resp = GuildBattleTeamManagerResp.newBuilder();
            //小队信息
            resp.setTeamInfo(fillTeamTime(teamData.toPB()));
            //操作
            resp.setOpt(req.getOpt());
            resp.setType(req.getType());
            player.sendProtocol(HawkProtocol.valueOf(HP.code2.GUILD_BATTLE_TEAM_MANAGER_RESP, resp));
        }
    }

    public void memberManager(Player player, GuildBattleMemberManagerReq req){
        GuildTeamPlayerData playerData = null;
        String oldTeamId = "";
        String newTeamId = "";
        switch (req.getAuth()){
            //设置出战，身份为指挥官，首发，预备队
            case GT_COMMAND:
            case GT_STARTER:
            case GT_CANDIDATE:{
                playerData = playerDataMap.get(req.getPlayerId());
                if(playerData != null){
                    oldTeamId = playerData.teamId;
                    newTeamId = req.getTeamId();
                    playerData.teamId = req.getTeamId();
                    playerData.auth = req.getAuth().getNumber();
                    if(!HawkOSOperator.isEmptyString(oldTeamId) && !oldTeamId.equals(playerData.teamId)){
                        teamIdToPlayerIds.get(oldTeamId).remove(playerData.id);
                        String teamPlayerKey = String.format(getTeamPlayerKey(), oldTeamId);
                        RedisProxy.getInstance().getRedisSession().sRem(teamPlayerKey, playerData.id);
                    }
                    if(!oldTeamId.equals(playerData.teamId)){
                        teamIdToPlayerIds.get(playerData.teamId).add(playerData.id);
                        String teamPlayerKey = String.format(getTeamPlayerKey(), playerData.teamId);
                        RedisProxy.getInstance().getRedisSession().sAdd(teamPlayerKey, 0, playerData.id);
                    }
                    updatePlayer(playerData);
                    LogUtil.logGuildTeamMamageMember(player, player.getGuildId(),this.getType().getNumber() * 1000 + 1, playerData.id, oldTeamId, newTeamId);
                    
                }
            }
            break;
            //设置为不出战
            case GT_NO_TEAM:{
                playerData = playerDataMap.get(req.getPlayerId());
                if(playerData != null){
                    oldTeamId = playerData.teamId;
                    playerData.teamId = "";
                    playerData.auth = req.getAuth().getNumber();
                    if(!HawkOSOperator.isEmptyString(oldTeamId)){
                        teamIdToPlayerIds.get(oldTeamId).remove(playerData.id);
                        String teamPlayerKey = String.format(getTeamPlayerKey(), oldTeamId);
                        RedisProxy.getInstance().getRedisSession().sRem(teamPlayerKey, playerData.id);
                    }
                    updatePlayer(playerData);
                    LogUtil.logGuildTeamMamageMember(player, player.getGuildId(), this.getType().getNumber() * 1000 + 2, playerData.id, oldTeamId, "");
                    
                }
            }
            break;
        }
        if(playerData != null){
            GuildBattleMemberManagerResp.Builder resp = GuildBattleMemberManagerResp.newBuilder();
            //玩家信息
            resp.setMemberInfo(playerData.toPB());
            //操作
            resp.setAuth(req.getAuth());
            resp.setType(req.getType());
            player.sendProtocol(HawkProtocol.valueOf(HP.code2.GUILD_BATTLE_MEMBER_MANAGER_RESP, resp));
        }
        GuildTeamData oldTeamData = teamDataMap.get(oldTeamId);
//如果小队数据不为空同步给前端
        if(oldTeamData != null){
            refreshInfo(oldTeamData);
            GuildBattleTeamManagerResp.Builder resp = GuildBattleTeamManagerResp.newBuilder();
            //小队信息
            resp.setTeamInfo(fillTeamTime(oldTeamData.toPB()));
            //操作
            resp.setOpt(GuildTeamOpt.GT_TEAM_UPDATE);
            resp.setType(req.getType());
            player.sendProtocol(HawkProtocol.valueOf(HP.code2.GUILD_BATTLE_TEAM_MANAGER_RESP, resp));
        }
        GuildTeamData newTeamData = teamDataMap.get(newTeamId);
//如果小队数据不为空同步给前端
        if(newTeamData != null){
            refreshInfo(newTeamData);
            GuildBattleTeamManagerResp.Builder resp = GuildBattleTeamManagerResp.newBuilder();
            //小队信息
            resp.setTeamInfo(fillTeamTime(newTeamData.toPB()));
            //操作
            resp.setOpt(GuildTeamOpt.GT_TEAM_UPDATE);
            resp.setType(req.getType());
            player.sendProtocol(HawkProtocol.valueOf(HP.code2.GUILD_BATTLE_TEAM_MANAGER_RESP, resp));
        }
    }

    public void teamList(Player player, GuildBattleTeamListReq req){
        GuildBattleTeamListResp.Builder resp = GuildBattleTeamListResp.newBuilder();
        if(player.isCsPlayer()){
            GuildTeamPlayerData playerData = load(player.getId());
            if(playerData!=null){
                GuildTeamData teamData = getBattleTeam(playerData.teamId);
                if(teamData != null){
                    GuildTeamInfo.Builder info = fillTeamTime(teamData.toPB());
                    info.setCanDissmiss(canDissmiss());
                    if(!HawkOSOperator.isEmptyString(teamData.oppTeamId)) {
                        GuildTeamData oppTeamData = getBattleTeam(teamData.oppTeamId);
                        if (oppTeamData != null) {
                            info.setOppTeam(oppTeamData.toPB());
                        }
                    }
                    resp.addTeamInfos(info);
                }
                resp.setSelfTeamId(playerData.teamId);
            }
        }else {
            resp.addAllTeamInfos(getTeamInfos(player));
            GuildTeamPlayerData playerData = playerDataMap.get(player.getId());
            if(playerData!=null){
                resp.setSelfTeamId(playerData.teamId);
            }
        }
        resp.setType(req.getType());
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.GUILD_BATTLE_TEAM_LIST_RESP, resp));
    }

    public void memberList(Player player, GuildBattleMemberListReq req){
        GuildBattleMemberListResp.Builder resp = GuildBattleMemberListResp.newBuilder();
        for(GuildTeamPlayerData playerData : getMemberList(player.getGuildId())){
            resp.addMemberInfos(playerData.toPB());
        }
        resp.setType(req.getType());
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.GUILD_BATTLE_MEMBER_LIST_RESP, resp));
    }

    /**
     * 获得页面战队信息
     * @param player 玩家数据
     * @return 小队页面信息
     */
    public List<GuildTeamInfo> getTeamInfos(Player player){
        List<GuildTeamInfo> list = new ArrayList<>();
        String guildId = player.getGuildId();
        if(HawkOSOperator.isEmptyString(guildId)){
            return list;
        }
        GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(player.getGuildId());
        if(guildInfoObject == null){
            return list;
        }
        //本服玩家加载本联盟所有小队0
        //遍历所有小队
        for(int i = 1; i <= getTeamNumLimit(); i++){
            GuildTeamData teamData = makeSureTeamData(guildInfoObject, i);
            if(teamData != null){
                refreshInfo(teamData);
                GuildTeamInfo.Builder info = fillTeamTime(teamData.toPB());
                info.setCanDissmiss(canDissmiss());
                if(!HawkOSOperator.isEmptyString(teamData.oppTeamId)) {
                    GuildTeamData oppTeamData = getBattleTeam(teamData.oppTeamId);
                    if (oppTeamData != null) {
                        info.setOppTeam(oppTeamData.toPB());
                    }
                }
                list.add(info.build());
            }
        }

        return list;
    }

    public GuildTeamData makeSureTeamData(GuildInfoObject guildInfoObject, int index){
        String teamId = getTeamId(guildInfoObject.getId(), index);
        if(canDissmiss()){
            return teamDataMap.get(teamId);
        }else {
            boolean isUpdate = false;
            if(!teamDataMap.containsKey(teamId)){
                GuildTeamData teamData = new GuildTeamData(guildInfoObject, index);
                teamData.name = getTeamName()+index+"队";
                GuildTeamData tmp = teamDataMap.putIfAbsent(teamData.id, teamData);
                teamIdToPlayerIds.putIfAbsent(teamData.id, new CopyOnWriteArraySet<>());
                if(tmp == null){
                    isUpdate = true;
                }
            }
            GuildTeamData teamData = teamDataMap.get(teamId);
            if(isUpdate){
                updateTeam(teamData);
            }
            return teamData;
        }
    }

    public GuildTeamData getBattleTeam(String teamId){
        return null;
    }

    public void signUp(Player player, String teamId, int index) {

    }
    /**
     * 本联盟所有成员数据
     * @param guildId
     * @return
     */
    public List<GuildTeamPlayerData> getMemberList(String guildId){
        List<GuildTeamPlayerData> list = new ArrayList<>();
        if(HawkOSOperator.isEmptyString(guildId)){
            return list;
        }
        Collection<String> memberIds =GuildService.getInstance().getGuildMembers(guildId);
        for (String memberId : memberIds) {
            Player player = GlobalData.getInstance().makesurePlayer(memberId);
            GuildTeamPlayerData data = makesurePlayerData(player);
            if(data == null){
                continue;
            }
            GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(memberId);
            data.refreshInfo(player, member);
            try {
                if(!HawkOSOperator.isEmptyString(data.teamId) && !data.teamId.startsWith(player.getGuildId())){
                    onQuitGuild(player);
                }
            }catch (Exception e){
                HawkException.catchException(e);
            }
            list.add(data);
        }
        return list;
    }

    /**
     * 获得玩家数据
     * @param player 玩家数据
     * @return 玩家数据
     */
    public GuildTeamPlayerData makesurePlayerData(Player player){
        if(player == null || player.isCsPlayer()){
            return null;
        }
        if(!playerDataMap.containsKey(player.getId())){
            GuildTeamPlayerData data = new GuildTeamPlayerData(player);
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

    public void updateTeam(GuildTeamData teamData){
        RedisProxy.getInstance().getRedisSession().hSet(getTeamKey(), teamData.id, teamData.serialize());
    }

    public abstract void refreshInfo(GuildTeamData teamData);

    public abstract GuildTeamInfo.Builder fillTeamTime(GuildTeamInfo.Builder info);

    public abstract int getTeamNumLimit();
    public String getTeamName(){
        return "泰伯";
    }

    public abstract boolean isSignUp(String teamId);
    public abstract boolean isInSeason(String teamId);
    public abstract void dissmissFromSeason(String teamId);
    public abstract boolean noCheckPlayerGuildOp();

    public int checkGuildOperation(String guildId, String playerId){
        if (!HawkOSOperator.isEmptyString(guildId)){
            for(int i = 1; i <= getTeamNumLimit(); i++) {
                String teamId = getTeamId(guildId, i);
                if(isInSeason(teamId)){
                    return Result.FAIL_VALUE;
                }
                if(isSignUp(teamId)){
                    return Result.FAIL_VALUE;
                }
            }
        }
        if(!HawkOSOperator.isEmptyString(playerId)){
            if(noCheckPlayerGuildOp()){
                return Result.SUCCESS_VALUE;
            }
            String teamId = getSelfTeamId(playerId);
            if(isInSeason(teamId)){
                return Result.FAIL_VALUE;
            }
            if(isSignUp(teamId)){
                return Result.FAIL_VALUE;
            }
        }
        return Result.SUCCESS_VALUE;
    }

    public abstract boolean checkTeamManager(Player player, GuildBattleTeamManagerReq req);
    public abstract boolean checkMemberManager(Player player, GuildBattleMemberManagerReq req);
    public abstract boolean canDissmiss();

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
            GuildTeamPlayerData playerData = playerDataMap.get(memberId);
            if(playerData == null || !teamId.equals(playerData.teamId)){
                continue;
            }
            Player player = GlobalData.getInstance().makesurePlayer(memberId);
            if(player == null){
                continue;
            }
            power += player.getNoArmyPower();
            count++;
        }
        return new HawkTuple2<>(power, count);
    }

    public void removeTeam(GuildTeamData teamData){
        RedisProxy.getInstance().getRedisSession().hDel(getTeamKey(), teamData.id);
    }

    public void updatePlayer(GuildTeamPlayerData playerData){
        RedisProxy.getInstance().getRedisSession().hSet(getPlayerKey(), playerData.id, playerData.serialize());
    }

    public GuildTeamData getTeamData(String teamId){
        return teamDataMap.get(teamId);
    }

    public List<GuildTeamData> getAllTeamList(){
        return new ArrayList<>(teamDataMap.values());
    }

    public GuildTeamPlayerData getPlayerData(String playerId){
        return playerDataMap.get(playerId);
    }

    public Set<String> getTeamPlayerIds(String teamId){
        return teamIdToPlayerIds.get(teamId);
    }

    public List<GuildTeamData> loadTeams(Set<String> teamIds){
        List<GuildTeamData> teamList = new ArrayList<>();
        List<String> list = RedisProxy.getInstance().getRedisSession().hmGet(getTeamKey(), teamIds.toArray(new String[teamIds.size()]));
        for(String json : list){
            GuildTeamData teamData = GuildTeamData.unSerialize(json);
            if(teamData == null){
                continue;
            }
            teamList.add(teamData);
        }
        return teamList;
    }

    public Map<String, GuildTeamData> loadTeamMap(Set<String> teamIds){
        Map<String, GuildTeamData> teamMap = new HashMap<>();
        if(teamIds == null || teamIds.isEmpty()){
            return teamMap;
        }
        List<String> list = RedisProxy.getInstance().getRedisSession().hmGet(getTeamKey(), teamIds.toArray(new String[teamIds.size()]));
        for(String json : list){
            GuildTeamData teamData = GuildTeamData.unSerialize(json);
            if(teamData == null){
                continue;
            }
            teamMap.put(teamData.id, teamData);
        }
        return teamMap;
    }

    public void updataFromBattle(GuildTeamData teamData){
        teamDataMap.put(teamData.id, teamData);
    }

    public void cleanTeam(){
        for(String teamId : teamDataMap.keySet()){
            try {
                GuildTeamData teamData = teamDataMap.get(teamId);
                if(teamData!=null && (!"".equals(teamData.oppTeamId) || teamData.timeIndex != 0)){
                    teamData.oppTeamId = "";
                    teamData.timeIndex = 0;
                    updateTeam(teamData);
                }
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
    }

    public void cleanPlayer(){
        if(GsConfig.getInstance().isDebug() && TBLYWarService.getInstance().getTermId() == 0){
            for(String playerId : playerDataMap.keySet()) {
                try {
                    GuildTeamPlayerData playerData = playerDataMap.get(playerId);
                    if(playerData != null && playerData.quitTIme != 0){
                        playerData.quitTIme = 0L;
                        playerData.isMidwayQuit = false;
                        updatePlayer(playerData);
                    }
                }catch (Exception e){
                    HawkException.catchException(e);
                }

            }
        }
    }


    public GuildTeamData loadTeam(String teamId){
        String json = RedisProxy.getInstance().getRedisSession().hGet(getTeamKey(), teamId);
        return GuildTeamData.unSerialize(json);
    }

    public GuildTeamPlayerData load(String playerId){
        String json = RedisProxy.getInstance().getRedisSession().hGet(getPlayerKey(), playerId);
        return GuildTeamPlayerData.unSerialize(json);
    }
}
