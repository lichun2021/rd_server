package com.hawk.activity.type.impl.changeServer;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ConsumeMoneyEvent;
import com.hawk.activity.event.impl.UseItemSpeedUpEvent;
import com.hawk.activity.event.impl.VitCostEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.changeServer.action.ChangeSvrActivityAction;
import com.hawk.activity.type.impl.changeServer.action.ChangeSvrActivityActionType;
import com.hawk.activity.type.impl.changeServer.cfg.*;
import com.hawk.activity.type.impl.changeServer.entity.ChangeServerEntity;
import com.hawk.activity.type.impl.changeServer.state.ChangeSvrActivityState;
import com.hawk.game.protocol.*;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.player.PowerData;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple2;
import redis.clients.jedis.Tuple;

import java.util.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class ChangeServerActivity extends ActivityBase {
    private ChangeSvrActivityState state = ChangeSvrActivityState.INIT;
    private boolean isGmNext;
    private int totalCount;
    private boolean inited;

    private List<Activity.ChangeServerActivityPlayerInfo.Builder> rankList = new ArrayList<>();


    private Map<String, Activity.ChangeServerActivityPlayerInfo.Builder> managerInfoMap = new ConcurrentHashMap<>();

    private List<Activity.ChangeServerActivityPlayerInfo.Builder> managerList = new ArrayList<>();

    private Map<String, Map<String, Activity.ChangeServerActivityPlayerInfo.Builder>> memberMap = new ConcurrentHashMap<>();

    private Map<String, Map<String, Activity.ChangeServerActivityPlayerInfo.Builder>> inviteMap = new ConcurrentHashMap<>();

    Map<String, Activity.ChangeServerActivityPlayerInfo.Builder> applyMap = new ConcurrentHashMap<>();
    private Map<String, Activity.ChangeServerActivityPlayerList.Builder> powerMap = new ConcurrentHashMap<>();

    private List<Activity.ChangeServerActivityPlayerInfo.Builder> powerRankList = new ArrayList<>();

    private Queue<ChangeSvrActivityAction> actionQueue = new LinkedList<>();


    private List<Activity.ChangeServerActivityPlayerInfo.Builder> showList = new ArrayList<>();
    private List<Activity.ChangeServerActivityPlayerInfo.Builder> showSList = new ArrayList<>();

    public ChangeServerActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.CHANGE_SVR_ACTIVITY;
    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        ChangeServerActivity activity = new ChangeServerActivity(config.getActivityId(), activityEntity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        //根据条件从数据库中检索
        List<ChangeServerEntity> queryList = HawkDBManager.getInstance()
                .query("from ChangeServerEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        //如果有数据的话，返回第一个数据
        if (queryList != null && queryList.size() > 0) {
            ChangeServerEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        ChangeServerEntity entity = new ChangeServerEntity(playerId, termId);
        return entity;
    }

    @Override
    public boolean isActivityClose(String playerId) {
        ChangeServerTimeCfg cfg = getTimeCfg();
        if(cfg == null){
            return true;
        }
        String serverId = getDataGeter().getServerId();
        if(!cfg.getToServerIds(serverId).contains(serverId)){
            return true;
        }
        return false;
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        if(!isOpening(playerId)){
            return;
        }
        info(playerId);
    }

    @Override
    public void onOpen() {
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for(String playerId : onlinePlayerIds){
            callBack(playerId, GameConst.MsgId.CHANGE_SVR_ACTIVITY_INFO_SYN, () -> {
                info(playerId);
            });
        }
    }

    @Override
    public void onPlayerLogin(String playerId) {

    }

    private Activity.ChangeServerActivityPlayerInfo.Builder creatNewInfo(String playerId){
        try {
            Activity.ChangeServerActivityPlayerInfo.Builder playerInfo = Activity.ChangeServerActivityPlayerInfo.newBuilder();
            playerInfo.setPlayerId(playerId);
            playerInfo.setPlayerName(getDataGeter().getPlayerName(playerId));
            playerInfo.setPfIcon(getDataGeter().getPfIcon(playerId));
            playerInfo.setIcon(getDataGeter().getIcon(playerId));
            playerInfo.setGuildName(getDataGeter().getGuildNameByByPlayerId(playerId));
            playerInfo.setScore(0);
            playerInfo.setRank(-1);
            String scourServerId = getScoureServerId(playerId);
            playerInfo.setServerId(scourServerId);
            playerInfo.setChangeServerId(scourServerId);
            playerInfo.setTotalCount(0);
            return playerInfo;
        }catch (Exception e){
            HawkException.catchException(e);
            return null;
        }
    }

    @Override
    public void onTick() {
        if(!isOpening("")){
            return;
        }
        init();
        check();
        doScoreSort(false);
        doPowerSort(false);
        loadShowS();
    }

    @Override
    public void onQuickTick() {
        init();
        onActionTick();
    }

    /**
     * 消耗金条
     *
     * @param event
     */
    @Subscribe
    public void onCostDiamon(ConsumeMoneyEvent event) {
        if (event.getResType() != Const.PlayerAttr.DIAMOND_VALUE) {
            return;
        }
        addScore(event.getPlayerId(), 1, (int) event.getNum());
    }

    /**
     * 消耗金币
     *
     * @param event
     */
    @Subscribe
    public void onCostGold(ConsumeMoneyEvent event) {
        if (event.getResType() != Const.PlayerAttr.GOLD_VALUE) {
            return;
        }
        addScore(event.getPlayerId(), 2, (int) event.getNum());
    }

    /**
     * 消耗体力
     *
     * @param event
     */
    @Subscribe
    public void onConsumeVit(VitCostEvent event) {
        addScore(event.getPlayerId(), 3, event.getCost());
    }

    /**
     * 消耗加速道具
     *
     * @param event
     */
    @Subscribe
    public void onConsumeSpeedTool(UseItemSpeedUpEvent event) {
        addScore(event.getPlayerId(), 4, event.getMinute());
    }

    private void addScore(String playerId, int getType, int num) {
        if (HawkOSOperator.isEmptyString(playerId)) {
            return;
        }
        if (!isOpening(playerId)) {
            return;
        }
        if(state != ChangeSvrActivityState.RANK){
            return;
        }
        if (num <= 0) {
            return;
        }
        ChangeServerPointGetCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ChangeServerPointGetCfg.class, getType);
        if (cfg == null) {
            return;
        }
        //获得玩家活动数据
        Optional<ChangeServerEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        ChangeServerEntity entity = opEntity.get();
        // 添加的积分
        int addScore = num * cfg.getProportion();
        long beforeScore = getOwnScore(entity, cfg.getGetType());
        long afterScore = beforeScore + addScore;
        if (cfg.getIsGetLimit() > 0) {
            if (afterScore > cfg.getLimitPoints()) {
                afterScore = cfg.getLimitPoints();
            }
        }
        setOwnScore(entity, getType, afterScore);
        updateRank(playerId, entity.getTotalScore());
        info(playerId);
    }

    private long getOwnScore(ChangeServerEntity entity, int getType) {
        switch (getType){
            case 1:{
                return entity.getCostDiamon();
            }
            case 2:{
                return entity.getCostGold();
            }
            case 3:{
                return entity.getConsumeVit();
            }
            case 4:{
                return entity.getConsumeSpeedTool();
            }
            default:{
                return 0l;
            }
        }
    }

    private void setOwnScore(ChangeServerEntity entity, int getType, long score) {
        switch (getType){
            case 1:{
                entity.setCostDiamon(score);
            }
            break;
            case 2:{
                entity.setCostGold(score);
            }
            break;
            case 3:{
                entity.setConsumeVit(score);
            }
            break;
            case 4:{
                entity.setConsumeSpeedTool(score);
            }
            break;
        }
    }

    private String getStateKey(){
        int termId = getActivityTermId();
        ChangeServerTimeCfg cfg = getTimeCfg();
        if(cfg == null){
            return "CHANGE_SVR:ERROR";
        }
        String mainServer = getDataGeter().getServerId();
        String serverId = cfg.getToServerIds(mainServer).get(0);
        return "CHANGE_SVR:" + termId + ":" + serverId + ":STATE";
    }

    private String getRankKey(){
        int termId = getActivityTermId();
        String serverId = getDataGeter().getServerId();
        return "CHANGE_SVR:" + termId + ":" + serverId + ":SCORE_RANK";
    }

    private String getPowerKey(){
        int termId = getActivityTermId();
        ChangeServerTimeCfg cfg = getTimeCfg();
        if(cfg == null){
            return "CHANGE_SVR:ERROR";
        }
        String mainServer = getDataGeter().getServerId();
        String serverId = cfg.getToServerIds(mainServer).get(0);
        return "CHANGE_SVR:" + termId + ":" + serverId + ":POWER";
    }

    private String getShowKey(){
        int termId = getActivityTermId();
        ChangeServerTimeCfg cfg = getTimeCfg();
        if(cfg == null){
            return "CHANGE_SVR:ERROR";
        }
        String mainServer = getDataGeter().getServerId();
        String serverId = cfg.getToServerIds(mainServer).get(0);
        return "CHANGE_SVR:" + termId + ":" + serverId + ":SHOW";
    }

    private String getShowSKey(){
        int termId = getActivityTermId();
        ChangeServerTimeCfg cfg = getTimeCfg();
        if(cfg == null){
            return "CHANGE_SVR:ERROR";
        }
        String mainServer = getDataGeter().getServerId();
        String serverId = cfg.getToServerIds(mainServer).get(0);
        return "CHANGE_SVR:" + termId + ":" + serverId + ":SHOW_S";
    }

    private String getManagerKey(){
        int termId = getActivityTermId();
        ChangeServerTimeCfg cfg = getTimeCfg();
        if(cfg == null){
            return "CHANGE_SVR:ERROR";
        }
        String mainServer = getDataGeter().getServerId();
        String serverId = cfg.getToServerIds(mainServer).get(0);
        return "CHANGE_SVR:" + termId + ":" + serverId + ":MANAGER";
    }

    private String getMemberKey(String playerId){
        int termId = getActivityTermId();
        ChangeServerTimeCfg cfg = getTimeCfg();
        if(cfg == null){
            return "CHANGE_SVR:ERROR";
        }
        String mainServer = getDataGeter().getServerId();
        String serverId = cfg.getToServerIds(mainServer).get(0);
        return "CHANGE_SVR:" + termId + ":" + serverId + ":MEMBER:"+playerId;
    }

    private String getChangeKey(String playerId){
        int termId = getActivityTermId();
        ChangeServerTimeCfg cfg = getTimeCfg();
        if(cfg == null){
            return "CHANGE_SVR:ERROR";
        }
        String mainServer = getDataGeter().getServerId();
        String serverId = cfg.getToServerIds(mainServer).get(0);
        return "CHANGE_SVR:" + termId + ":" + serverId + ":CHANGE:"+playerId;
    }

    private void loadPower(){
        List<byte[]> powers = ActivityGlobalRedis.getInstance().getRedisSession().lRange(getPowerKey().getBytes(), 0, 1000, 0);
        for (byte[] bytes : powers) {
            try {
                Activity.ChangeServerActivityPlayerInfo.Builder playerInfo = Activity.ChangeServerActivityPlayerInfo.newBuilder();
                playerInfo.mergeFrom(bytes);
                powerRankList.add(0, playerInfo);
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
    }


    private long lastShowSTime = 0;
    private void loadShowS(){
        if(state != ChangeSvrActivityState.CHANGE){
            return;
        }
        long now = HawkTime.getMillisecond();
        if(now - lastShowSTime < 600000l){
            return;
        }
        lastShowSTime = now;
        List<Activity.ChangeServerActivityPlayerInfo.Builder> showSList = new ArrayList<>();
        List<byte[]> showSs = ActivityGlobalRedis.getInstance().getRedisSession().lRange(getShowSKey().getBytes(), 0, 1000, 0);
        for (byte[] bytes : showSs) {
            try {
                Activity.ChangeServerActivityPlayerInfo.Builder playerInfo = Activity.ChangeServerActivityPlayerInfo.newBuilder();
                playerInfo.mergeFrom(bytes);
                showSList.add(0, playerInfo);
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
        this.showSList = showSList;
    }

    private void loadShow(){
        Map<byte[], byte[]> showInfos = ActivityGlobalRedis.getInstance().getRedisSession().hGetAllBytes(getShowKey().getBytes());
        for (byte[] bytes : showInfos.values()) {
            try {
                Activity.ChangeServerActivityPlayerInfo.Builder playerInfo = Activity.ChangeServerActivityPlayerInfo.newBuilder();
                playerInfo.mergeFrom(bytes);
                applyMap.put(playerInfo.getPlayerId(), playerInfo);
                showList.add(playerInfo);
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
    }

    private boolean isChange(String playerId){
        String changeStr = ActivityGlobalRedis.getInstance().getRedisSession().getString(getChangeKey(playerId));
        if(HawkOSOperator.isEmptyString(changeStr)){
            return false;
        }
        if("1".equals(changeStr)){
            return true;
        }
        return false;
    }

    private void setChange(String playerId){
        ActivityGlobalRedis.getInstance().getRedisSession().setString(getChangeKey(playerId), "1");
    }

    private void updatePower(Activity.ChangeServerActivityPlayerInfo.Builder playerInfo){
        ActivityGlobalRedis.getInstance().getRedisSession().lPush(getPowerKey().getBytes(), 0, playerInfo.build().toByteArray());
    }

    private void updateShow(Activity.ChangeServerActivityPlayerInfo.Builder info){
        ActivityGlobalRedis.getInstance().getRedisSession().hSetBytes(getShowKey(), info.getPlayerId(), info.build().toByteArray());
    }

    private void updateShowS(Activity.ChangeServerActivityPlayerInfo.Builder playerInfo){
        ActivityGlobalRedis.getInstance().getRedisSession().lPush(getShowSKey().getBytes(), 0, playerInfo.build().toByteArray());
    }


    private void delShow(String playerId){
        ActivityGlobalRedis.getInstance().getRedisSession().hDelBytes(getShowKey(), playerId.getBytes());
    }

    private void loadManager(){
        Map<byte[], byte[]> managerInfos = ActivityGlobalRedis.getInstance().getRedisSession().hGetAllBytes(getManagerKey().getBytes());
        for (byte[] bytes : managerInfos.values()) {
            try {
                Activity.ChangeServerActivityPlayerInfo.Builder playerInfo = Activity.ChangeServerActivityPlayerInfo.newBuilder();
                playerInfo.mergeFrom(bytes);
                managerInfoMap.put(playerInfo.getPlayerId(), playerInfo);
                managerList.add(playerInfo);
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
    }

    private void updateManager(Activity.ChangeServerActivityPlayerInfo.Builder info){
        ActivityGlobalRedis.getInstance().getRedisSession().hSetBytes(getManagerKey(), info.getPlayerId(), info.build().toByteArray());
    }


    private void loadMember(){
        for(Activity.ChangeServerActivityPlayerInfo.Builder info : managerList){
            Map<String, Activity.ChangeServerActivityPlayerInfo.Builder> map = new ConcurrentHashMap<>();
            Map<byte[], byte[]> memberInfos = ActivityGlobalRedis.getInstance().getRedisSession().hGetAllBytes(getMemberKey(info.getPlayerId()).getBytes());
            for (byte[] bytes : memberInfos.values()) {
                try {
                    Activity.ChangeServerActivityPlayerInfo.Builder playerInfo = Activity.ChangeServerActivityPlayerInfo.newBuilder();
                    playerInfo.mergeFrom(bytes);
                    map.put(playerInfo.getPlayerId(), playerInfo);
                }catch (Exception e){
                    HawkException.catchException(e);
                }
            }
            memberMap.put(info.getPlayerId(), map);
        }
    }

    private void updateMember(String playerId, Activity.ChangeServerActivityPlayerInfo.Builder info){
        ActivityGlobalRedis.getInstance().getRedisSession().hSetBytes(getMemberKey(playerId), info.getPlayerId(), info.build().toByteArray());
    }

    private void delMember(String managerId, String playerId){
        ActivityGlobalRedis.getInstance().getRedisSession().hDelBytes(getMemberKey(managerId), playerId.getBytes());
    }


    public int getRankSize() {
        return 100;
    }

    private void updateRank(String playerId, long score){
        long rankScore = RankScoreHelper.calcSpecialRankScore(score);
        ActivityGlobalRedis.getInstance().getRedisSession().zAdd(getRankKey(), rankScore, playerId);
    }

    public Set<Tuple> getRankSet(){
        return ActivityGlobalRedis.getInstance().getRedisSession().zRevrangeWithScores(getRankKey(), 0, Math.max((getRankSize() - 1), 0), 0);
    }

    public HawkTuple2<Integer, Long> getSelfRank(String playerId){
        RedisIndex index = ActivityGlobalRedis.getInstance().zrevrankAndScore(getRankKey(), playerId);
        int rank = -1;
        long score = 0;
        if (index != null) {
            rank = index.getIndex().intValue() + 1;
            score = RankScoreHelper.getRealScore(index.getScore().longValue());
        }
        return new HawkTuple2<>(rank, score);
    }

    private void addAction(ChangeSvrActivityActionType type, String from, String to){
        ChangeSvrActivityAction action = new ChangeSvrActivityAction(type, from, to);
        actionQueue.add(action);
    }

    private void onActionTick(){
        if (state != ChangeSvrActivityState.APPLY) {
            return;
        }
        if (actionQueue.isEmpty()) {
            return;
        }
        while (!actionQueue.isEmpty()) {
            try {
                ChangeSvrActivityAction action = actionQueue.poll();
                if (action != null) {
                    onAction(action);
                }
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
    }

    private void onAction(ChangeSvrActivityAction action){
        String from = action.getFrom();
        String to = action.getTo();
        switch (action.getType()){
            case INVITE:{
                onInviteAction(from, to);
            }
            break;
            case APPROVE_YES:{
                onApproveYesAction(from, to);
            }
            break;
            case APPROVE_NO:{
                onApproveNoAction(from, to);
            }
            break;
            case CANCEL:{
                onCancelAction(from, to);
            }
            break;
            default:{

            }
            break;
        }
        callBack(from, GameConst.MsgId.CHANGE_SVR_ACTIVITY_INFO_SYN, () -> {
            info(from);
        });
        callBack(to, GameConst.MsgId.CHANGE_SVR_ACTIVITY_INFO_SYN, () -> {
            info(to);
        });
    }

    private void onInviteAction(String from, String to){
        int hpCode = HP.code2.CHANGE_SVR_ACTIVITY_INVITE_REQ_VALUE;
        if(applyMap.containsKey(to)){
            return;
        }
        Activity.ChangeServerActivityPlayerInfo.Builder info = managerInfoMap.get(from);
        if(info == null){
            return;
        }
        if(memberMap.getOrDefault(from, new HashMap<>()).size() >= info.getTotalCount()){
            sendError(from, hpCode, Status.Error.CHANGE_SVR_INVITE_CONUT_LIMIT_SELF_VALUE);
            return;
        }
        if(from.equals(to)){
            if(!memberMap.containsKey(to)){
                memberMap.put(to, new HashMap<>());
            }
            memberMap.get(from).put(from, info);
            updateMember(from, info);
            if(!inviteMap.containsKey(to)){
                inviteMap.put(to, new HashMap<>());
            }
            inviteMap.get(from).clear();
            info.setPower(getNoArmyPower(info.getPlayerId()));
            applyMap.put(from, info);
            updateShow(info);
            power(from);
            sendMailToPlayer(from, MailConst.MailId.CHANGE_SVR_APPLY_MAIL, null, new Object[] {}, new Object[] {}, null, false);
            Map<String, Object> param = new HashMap<>();
            param.put("target", to);
            getDataGeter().logActivityCommon(from, LogInfoType.change_svr_apply, param);
            return;
        }
        if(!inviteMap.containsKey(to)){
            inviteMap.put(to, new HashMap<>());
        }
        inviteMap.get(to).put(from, info);
        applyList(to);
        responseSuccess(from, hpCode);
    }

    private void onApproveYesAction(String from, String to){
        int hpCode = HP.code2.CHANGE_SVR_ACTIVITY_APPROVE_REQ_VALUE;
        if(applyMap.containsKey(from)){
            return;
        }
        Activity.ChangeServerActivityPlayerInfo.Builder toInfo = managerInfoMap.get(to);
        if(toInfo == null){
            return;
        }
        if(memberMap.getOrDefault(to, new HashMap<>()).size() >= toInfo.getTotalCount()){
            sendError(from, hpCode, Status.Error.CHANGE_SVR_INVITE_CONUT_LIMIT_VALUE);
            return;
        }
        updateManager(toInfo);
        Activity.ChangeServerActivityPlayerInfo.Builder fromInfo = creatNewInfo(from);
        if(!memberMap.containsKey(to)){
            memberMap.put(to, new HashMap<>());
        }
        memberMap.get(to).put(from, fromInfo);
        updateMember(to, fromInfo);
        inviteMap.get(from).clear();
        fromInfo.setPower(getNoArmyPower(fromInfo.getPlayerId()));
        applyMap.put(from, fromInfo);
        updateShow(fromInfo);
        applyList(from);
        responseSuccess(from, hpCode);
        sendMailToPlayer(from, MailConst.MailId.CHANGE_SVR_APPLY_MAIL, null, new Object[] {}, new Object[] {}, null, false);
        sendMailToPlayer(to, MailConst.MailId.CHANGE_SVR_INVITE_MAIL, null, new Object[] {}, new Object[] {fromInfo.getPlayerName()}, null, false);
        Map<String, Object> param = new HashMap<>();
        param.put("target", to);
        getDataGeter().logActivityCommon(from, LogInfoType.change_svr_apply, param);
    }

    private void onApproveNoAction(String from, String to){
        inviteMap.get(from).remove(to);
        applyList(from);
    }

    private void onCancelAction(String from, String to){
        Activity.ChangeServerActivityPlayerInfo.Builder fromInfo = managerInfoMap.get(from);
        if(fromInfo == null){
            return;
        }
        if(memberMap.get(from) == null || !memberMap.get(from).containsKey(to)){
            return;
        }
        memberMap.get(from).remove(to);
        delMember(from, to);
        applyMap.remove(to);
        delShow(to);
        intiveeList(from);
        sendMailToPlayer(to, MailConst.MailId.CHANGE_SVR_CANCEL_MAIL, null, new Object[] {}, new Object[] {}, null, false);
        Map<String, Object> param = new HashMap<>();
        param.put("target", to);
        getDataGeter().logActivityCommon(from, LogInfoType.change_svr_cancel, param);
    }

    private void init(){
        if(inited){
            return;
        }
        inited = true;
        String stateStr = ActivityGlobalRedis.getInstance().getRedisSession().getString(getStateKey());
        if(!HawkOSOperator.isEmptyString(stateStr)){
            state = ChangeSvrActivityState.getStateByValue(Integer.parseInt(stateStr));
        }
        loadManager();
        loadMember();
        loadShow();
        loadPower();
    }

    private void check(){
        boolean isNext = false;
        long now = HawkTime.getMillisecond();
        long endtime = getEndTime();
        switch (state){
            case INIT:{
                state = ChangeSvrActivityState.RANK;
                isNext = true;
            }
            break;
            case RANK:{
                if(isGmNext || now > endtime){
                    state = ChangeSvrActivityState.APPLY;
                    isNext = true;
                }
            }
            break;
            case APPLY:{
                if(isGmNext || now > endtime){
                    state = ChangeSvrActivityState.SHOW;
                    isNext = true;
                }
            }
            break;
            case SHOW:{
                if(isGmNext || now > endtime){
                    state = ChangeSvrActivityState.CHANGE;
                    isNext = true;
                }
            }
            break;
            case CHANGE:{
            }
            break;
            default:{
            }
            break;
        }
        if(isNext){
            isGmNext = false;
            enter();
            updateState();
        }
    }

    private void enter(){
        switch (state){
            case APPLY:{
                onEnterApply();
            }
            break;
            case SHOW:{
                onEnterShow();
            }
            break;
            case CHANGE:{
                onEnterChange();
            }
            break;
        }
    }

    public void next(){
        if(getDataGeter().isServerDebug()){
            isGmNext = true;
        }
    }

    private void onEnterApply(){
        doScoreSort(true);
        doPowerSort(true);
        for(Activity.ChangeServerActivityPlayerInfo.Builder info : rankList){
            if(info.getTotalCount() > 0){
                info = info.clone();
                managerInfoMap.put(info.getPlayerId(), info);
                managerList.add(info);
                updateManager(info);
            }
        }
        for(Activity.ChangeServerActivityPlayerInfo.Builder info : powerRankList){
            updatePower(info);
        }
    }

    private void onEnterShow(){
        List<Activity.ChangeServerActivityPlayerInfo.Builder> showList = new ArrayList<>();
        for(Activity.ChangeServerActivityPlayerInfo.Builder info : applyMap.values()){
            info.setChangeServerId(getChangeServerId(info.getPlayerId()));
            showList.add(info);
        }
        this.showList = showList;
    }

    private void onEnterChange(){

    }

    private void updateState(){
        ActivityGlobalRedis.getInstance().getRedisSession().setString(getStateKey(), String.valueOf(state.getIndex()));
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for(String playerId : onlinePlayerIds){
            callBack(playerId, GameConst.MsgId.ON_PDD_ACTIVITY_OPEN, () -> {
                info(playerId);
            });
        }
    }


    private long lastScoreSortTime = 0;
    private void doScoreSort(boolean isForce){
        if(!isForce && state != ChangeSvrActivityState.RANK){
            return;
        }
        long now = HawkTime.getMillisecond();
        if(!isForce && (now - lastScoreSortTime < 600000l)){
            return;
        }
        lastScoreSortTime = now;
        RangeMap<Integer, ChangeServerRankListCfg> rangeMap = getRankCountMap();
        List<Activity.ChangeServerActivityPlayerInfo.Builder> rankList = new ArrayList<>();
        Set<Tuple> rankSet = getRankSet();
        int rank = 1;
        for (Tuple tuple : rankSet) {
            String playerId = tuple.getElement();
            Activity.ChangeServerActivityPlayerInfo.Builder playerInfo = creatNewInfo(playerId);
            if(playerInfo == null){
                continue;
            }
            long score = RankScoreHelper.getRealScore((long) tuple.getScore());
            playerInfo.setScore(score);
            playerInfo.setRank(rank);
            ChangeServerRankListCfg cfg = rangeMap.get(rank);
            if(cfg != null){
                playerInfo.setTotalCount(cfg.getChangeServerCount());
            }
            rankList.add(playerInfo);
            rank++;
        }
        this.rankList = rankList;
    }


    private long lastPowerSortTime = 0;
    private void doPowerSort(boolean isForce){
        if(!isForce && state != ChangeSvrActivityState.RANK){
            return;
        }
        long now = HawkTime.getMillisecond();
        if(!isForce && (now - lastPowerSortTime < 600000l)){
            return;
        }
        lastPowerSortTime = now;
        List<Activity.ChangeServerActivityPlayerInfo.Builder> powerRankList = new ArrayList<>();
        Set<Tuple> rankSet = getDataGeter().getRankList(Rank.RankType.PLAYER_NOARMY_POWER_RANK, 1000);
        for (Tuple tuple : rankSet) {
            String playerId = tuple.getElement();
            long score = (long) tuple.getScore();
            Activity.ChangeServerActivityPlayerInfo.Builder playerInfo = creatNewInfo(playerId);
            if(playerInfo == null){
                continue;
            }
            playerInfo.setPower(score);
            powerRankList.add(playerInfo);
        }
        this.powerRankList = powerRankList;

    }

    private RangeMap<Integer, ChangeServerRankListCfg> getRankCountMap(){
        RangeMap<Integer, ChangeServerRankListCfg> rangeMap = TreeRangeMap.create();
        ConfigIterator<ChangeServerRankListCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ChangeServerRankListCfg.class);
        while (iterator.hasNext()) {
            ChangeServerRankListCfg cfg = iterator.next();
            if(cfg != null){
                rangeMap.put(Range.closed(cfg.getRankUpper(), cfg.getRankLower()), cfg);
            }
        }
        return rangeMap;
    }

    public String getScoureServerId(String playerId){
        String serverId = getDataGeter().getPlayerServerId(playerId);
        ChangeServerTimeCfg cfg = getTimeCfg();
        return cfg.getMainServer(serverId);
    }

    public String getChangeServerId(String playerId){
        String scourServerId = getScoureServerId(playerId);
        ChangeServerTimeCfg cfg = getTimeCfg();
        String mainServer = getDataGeter().getServerId();
        for(String serverId : cfg.getToServerIds(mainServer)){
            if(!scourServerId.equals(serverId)){
                return serverId;
            }
        }
        return "";
    }
    private long getEndTime(){
        ChangeServerTimeCfg cfg = getTimeCfg();
        if(cfg == null){
            return Long.MAX_VALUE;
        }
        switch (state){
            case RANK:{
                return cfg.getEndTimeConutValue();
            }
            case APPLY:{
                return cfg.getEndTimeChangeValue();
            }

            case SHOW:{
                return cfg.getEndTimeShowListValue();
            }
            case CHANGE:{
                return cfg.getEndTimeOfficialChangeValue();
            }
        }
        return Long.MAX_VALUE;
    }


    private ChangeServerTimeCfg getTimeCfg(){
        int termId = this.getActivityTermId();
        ChangeServerTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ChangeServerTimeCfg.class, termId);
        return cfg;
    }

    private int getTotalCount(){
        if(totalCount > 0){
            return totalCount;
        }
        int tmp = 0;
        ConfigIterator<ChangeServerRankListCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ChangeServerRankListCfg.class);
        while (iterator.hasNext()) {
            ChangeServerRankListCfg cfg = iterator.next();
            for(int i = cfg.getRankUpper(); i <= cfg.getRankLower(); i++){
                tmp += cfg.getChangeServerCount();
            }
        }
        totalCount = tmp;
        return totalCount;
    }

    private void fillActivityData(Activity.ChangeServerActivityInfoResp.Builder builder){
        builder.setState(state.getClientState());
        builder.setEndTime(getEndTime());
        ChangeServerTimeCfg cfg = getTimeCfg();
        String mainServer = getDataGeter().getServerId();
        for(String toServerId : cfg.getToServerIds(mainServer)){
            builder.addToServerIds(toServerId);
        }
        builder.setSeparateServerTime(cfg.getSeparateServerTime());
    }

    private void fillStateData(Activity.ChangeServerActivityInfoResp.Builder builder, String playerId){

        ChangeServerTimeCfg cfg = getTimeCfg();
        switch (state){
            case RANK:{
                HawkTuple2<Integer, Long> tuple2 = getSelfRank(playerId);
                builder.setRank(tuple2.first);
                //获得玩家活动数据
                Optional<ChangeServerEntity> opEntity = getPlayerDataEntity(playerId);
                //如果数据为空直接返回
                if (opEntity.isPresent()) {
                    ChangeServerEntity entity = opEntity.get();
                    builder.addScore(entity.getCostDiamon());
                    builder.addScore(entity.getCostGold());
                    builder.addScore(entity.getConsumeVit());
                    builder.addScore(entity.getConsumeSpeedTool());
                }
            }
            break;
            case APPLY:{
                builder.setNormalEndTime(cfg.getEndTimeConutChangeValue());
                builder.setIsManager(managerInfoMap.containsKey(playerId));
                builder.setIsApply(applyMap.containsKey(playerId));
                builder.setUseCount(applyMap.size());
                builder.setTotalCount(getTotalCount());
            }
            break;
            case CHANGE:{
                if(applyMap.containsKey(playerId)){
                    builder.setChangeGold(0);
                }else {
                    builder.setChangeGold(getChangeGold(playerId));
                }
                builder.setIsChangeSuccess(isChange(playerId));
            }
            break;

        }
    }

    private void fillPlayerData(Activity.ChangeServerActivityInfoResp.Builder builder, String playerId){
        String scourServerId = getScoureServerId(playerId);
        String changeServerId = getChangeServerId(playerId);
        builder.setServerId(scourServerId);
        if(applyMap.containsKey(playerId)){
            builder.setChangeServerId(changeServerId);
        }else {
            builder.setChangeServerId(scourServerId);
        }
        Activity.ChangeServerActivityPlayerInfo.Builder managerInfo = managerInfoMap.get(playerId);
        if(managerInfo != null){
            builder.setSelfUseCount(memberMap.getOrDefault(managerInfo.getPlayerId(), new HashMap<>()).size());
            builder.setSelfTotalCount(managerInfo.getTotalCount());
        }
        ChangeServerTimeCfg cfg = getTimeCfg();
        Activity.ChangeServerActivityCondition.Builder condition =
                getDataGeter().getChangeServerActivityCondition(playerId, cfg.getToServerIdMap(), changeServerId,
                        Activity.ChangeServerActivityConditionType.CHANGE_SVR_ACCOUNT );
        if(condition != null){
            builder.setHasAccount(!condition.getIsDone());
        }
    }

    public Result<Integer> info(String playerId){
        Activity.ChangeServerActivityInfoResp.Builder builder = Activity.ChangeServerActivityInfoResp.newBuilder();
        fillActivityData(builder);
        fillPlayerData(builder, playerId);
        fillStateData(builder, playerId);
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.CHANGE_SVR_ACTIVITY_INFO_RESP, builder));
        return Result.success();
    }

    public Result<Integer> scoreRank(String playerId){
        Activity.ChangeServerActivityPlayerList.Builder pbRankList = Activity.ChangeServerActivityPlayerList.newBuilder();
        Activity.ChangeServerActivityPlayerInfo.Builder selfInfo = null;
        for(Activity.ChangeServerActivityPlayerInfo.Builder info : this.rankList){
            pbRankList.addInfos(info);
            if(playerId.equals(info.getPlayerId())){
                selfInfo = info;
            }
        }
        if(selfInfo == null){
            selfInfo = creatNewInfo(playerId);
            //获得玩家活动数据
            Optional<ChangeServerEntity> opEntity = getPlayerDataEntity(playerId);
            //如果数据为空直接返回
            if (opEntity.isPresent()) {
                ChangeServerEntity entity = opEntity.get();
                selfInfo.setScore(entity.getTotalScore());
            }
        }
        pbRankList.setSelfRank(selfInfo);
        Activity.ChangeServerActivityScoreRankResp.Builder builder = Activity.ChangeServerActivityScoreRankResp.newBuilder();
        builder.setRankList(pbRankList);
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.CHANGE_SVR_ACTIVITY_SCORE_RANK_RESP, builder));
        return Result.success();
    }

    public Result<Integer> managerList(String playerId){
        Activity.ChangeServerActivityPlayerList.Builder managerList = Activity.ChangeServerActivityPlayerList.newBuilder();
        for(Activity.ChangeServerActivityPlayerInfo.Builder info : this.managerList){
            info.setUseCount(memberMap.getOrDefault(info.getPlayerId(), new HashMap<>()).size());
            managerList.addInfos(info);
        }
        Activity.ChangeServerActivityManagerListResp.Builder builder = Activity.ChangeServerActivityManagerListResp.newBuilder();
        builder.setManagerList(managerList);
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.CHANGE_SVR_ACTIVITY_MANAGER_LIST_RESP, builder));
        return Result.success();
    }

    public Result<Integer> intiveeList(String playerId){
        Activity.ChangeServerActivityPlayerList.Builder inviteeList = Activity.ChangeServerActivityPlayerList.newBuilder();
        if(this.memberMap.containsKey(playerId)){
            for(Activity.ChangeServerActivityPlayerInfo.Builder info : this.memberMap.get(playerId).values()){
                info.setChangeServerId(getChangeServerId(info.getPlayerId()));
                inviteeList.addInfos(info);
            }
        }
        Activity.ChangeServerActivityInviteeListResp.Builder builder = Activity.ChangeServerActivityInviteeListResp.newBuilder();
        builder.setInviteeList(inviteeList);
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.CHANGE_SVR_ACTIVITY_INTIVEE_LIST_RESP, builder));
        return Result.success();
    }

    public Result<Integer> applyList(String playerId){
        Activity.ChangeServerActivityPlayerList.Builder applyList = Activity.ChangeServerActivityPlayerList.newBuilder();
        if(this.inviteMap.containsKey(playerId)){
            for(Activity.ChangeServerActivityPlayerInfo.Builder info : this.inviteMap.get(playerId).values()){
                applyList.addInfos(info);
            }
        }
        Activity.ChangeServerActivityApplyListResp.Builder builder = Activity.ChangeServerActivityApplyListResp.newBuilder();
        builder.setApplyList(applyList);
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.CHANGE_SVR_ACTIVITY_APPLY_LIST_RESP, builder));
        return Result.success();
    }

    public Result<Integer> power(String playerId){
        ChangeServerTimeCfg cfg = getTimeCfg();
        Activity.ChangeServerActivityPowerResp.Builder builder = Activity.ChangeServerActivityPowerResp.newBuilder();
        String mainServer = getDataGeter().getServerId();
        builder.setPowerList1(getPowerRank(cfg.getToServerIds(mainServer).get(0), playerId));
        builder.setPowerList2(getPowerRank(cfg.getToServerIds(mainServer).get(1), playerId));
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.CHANGE_SVR_ACTIVITY_POWER_RESP, builder));
        return Result.success();
    }

    private Activity.ChangeServerActivityPlayerList.Builder getPowerRank(String serverId, String playerId){
        Activity.ChangeServerActivityPlayerList.Builder power = Activity.ChangeServerActivityPlayerList.newBuilder();
        power.setServerId(serverId);
        Activity.ChangeServerActivityPlayerInfo.Builder selfInfo = null;
        int noArmyPoint = getNoArmyPower(playerId);
        int selfRank = Integer.MAX_VALUE;
        int rank = 1;
        for(Activity.ChangeServerActivityPlayerInfo.Builder info : powerRankList){
            if(playerId.equals(info.getPlayerId())){
                selfInfo = info.clone();
                selfInfo.setRank(rank);
            }
            if(serverId.equals(info.getServerId())){
                Activity.ChangeServerActivityPlayerInfo.Builder rankInfo = info.clone();
                rankInfo.setRank(rank);
                rankInfo.setIsApply(isApply(playerId, rankInfo.getPlayerId()));
                power.addInfos(rankInfo);
                if(noArmyPoint >= rankInfo.getPower() && rank < selfRank){
                    selfRank = rank;
                }
                rank++;
            }
            if(rank == 101){
                break;
            }
        }
        if(selfRank == Integer.MAX_VALUE){
            selfRank = -1;
        }
        if(selfInfo == null){
            selfInfo = creatNewInfo(playerId);
            selfInfo.setPower(noArmyPoint);
            selfInfo.setRank(selfRank);
        }
        power.setSelfRank(selfInfo);
        return power;
    }

    private int getNoArmyPower(String playerId){
        PowerData powerData = getDataGeter().getPowerData(playerId);
        if (powerData == null) {
        	return 0;
        }
        long totalPoint = powerData.getTotalPoint();
        long armyPoint = powerData.getArmyBattlePoint();
        int trapPoint = powerData.getTrapBattlePoint();
        int noArmyPoint = (int) Math.max(totalPoint - armyPoint - trapPoint, 0);
        return noArmyPoint;
    }

    private int getChangeGold(String playerId){
        if(applyMap.containsKey(playerId)){
            return 0;
        }
        ChangeServerKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(ChangeServerKVCfg.class);
        Activity.ChangeServerActivityPlayerList.Builder rankList = getPowerRank(getChangeServerId(playerId), playerId);
        int rank = rankList.getSelfRank().getRank();
        int noArmyPoint = getNoArmyPower(playerId);
        ChangeServerCostRankCfg rankCfg = getRankCostMap().get(rank);
        ChangeServerCostPowerCfg powerCfg = getPowerCostMap().get(noArmyPoint);
        int gold = 0;
        if(rankCfg == null){
            gold += kvCfg.getCostRankNothing();
        }else {
            gold += rankCfg.getCost();
        }
        if(powerCfg == null){
            gold += kvCfg.getCostPowerNothing();
        }else {
            gold += powerCfg.getCost();
        }
        return gold;
    }

    private RangeMap<Integer, ChangeServerCostRankCfg> getRankCostMap(){
        RangeMap<Integer, ChangeServerCostRankCfg> rangeMap = TreeRangeMap.create();
        ConfigIterator<ChangeServerCostRankCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ChangeServerCostRankCfg.class);
        while (iterator.hasNext()) {
            ChangeServerCostRankCfg cfg = iterator.next();
            if(cfg != null){
                rangeMap.put(Range.closed(cfg.getRankUpper(), cfg.getRankLower()), cfg);
            }
        }
        return rangeMap;
    }

    private RangeMap<Integer, ChangeServerCostPowerCfg> getPowerCostMap(){
        RangeMap<Integer, ChangeServerCostPowerCfg> rangeMap = TreeRangeMap.create();
        ConfigIterator<ChangeServerCostPowerCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ChangeServerCostPowerCfg.class);
        while (iterator.hasNext()) {
            ChangeServerCostPowerCfg cfg = iterator.next();
            if(cfg != null){
                rangeMap.put(Range.closed(cfg.getPowerUpper(), cfg.getPowerLower()), cfg);
            }
        }
        return rangeMap;
    }


    public boolean isApply(String from, String to){
        try {
            if(applyMap.containsKey(to)){
                return true;
            }
            if(!inviteMap.containsKey(to)){
                return false;
            }
            return inviteMap.get(to).containsKey(from);
        }catch (Exception e){
            HawkException.catchException(e);
            return false;
        }

    }

    public Result<Integer> invite(String playerId, String to){
        String changeServerId = getChangeServerId(to);
        ChangeServerTimeCfg cfg = getTimeCfg();
        Activity.ChangeServerActivityCondition.Builder condition =
                getDataGeter().getChangeServerActivityCondition(to, cfg.getToServerIdMap(), changeServerId,
                        Activity.ChangeServerActivityConditionType.CHANGE_SVR_ACCOUNT );
        if(condition != null && !condition.getIsDone()){
            if(playerId.equals(to)) {
                return Result.fail(Status.Error.CHANGE_SVR_INVITE_HAVE_ACCOUNT_SELF_VALUE);
            }else {
                return Result.fail(Status.Error.CHANGE_SVR_INVITE_HAVE_ACCOUNT_VALUE);
            }
        }
        addAction(ChangeSvrActivityActionType.INVITE, playerId, to);
        return Result.success();
    }

    public Result<Integer> approve(String playerId, String to, boolean isApprove){
        if(isApprove){
            addAction(ChangeSvrActivityActionType.APPROVE_YES, playerId, to);
        }else {
            addAction(ChangeSvrActivityActionType.APPROVE_NO, playerId, to);
        }

        return Result.success();
    }

    public Result<Integer> cancel(String playerId, String to){
        addAction(ChangeSvrActivityActionType.CANCEL, playerId, to);
        return Result.success();
    }


    public Result<Integer> show(String playerId){
        Activity.ChangeServerActivityPlayerList.Builder showList = Activity.ChangeServerActivityPlayerList.newBuilder();
        for(Activity.ChangeServerActivityPlayerInfo.Builder info : this.showList){
            showList.addInfos(info);
        }
        Activity.ChangeServerActivityPlayerList.Builder showList1 = Activity.ChangeServerActivityPlayerList.newBuilder();
        for(Activity.ChangeServerActivityPlayerInfo.Builder info : this.showSList){
            showList1.addInfos(info);
        }
        Activity.ChangeServerActivityShowResp.Builder builder = Activity.ChangeServerActivityShowResp.newBuilder();
        builder.setNormalList(showList);
        builder.setSpecialList(showList1);
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.CHANGE_SVR_ACTIVITY_SHOW_RESP, builder));
        return Result.success();
    }

    public Result<Integer> changeCheck(String playerId){
        ChangeServerTimeCfg cfg = getTimeCfg();
        Activity.ChangeServerActivityChangeCheckResp.Builder builder = Activity.ChangeServerActivityChangeCheckResp.newBuilder();
        String changeServerId = getChangeServerId(playerId);
        for(Activity.ChangeServerActivityConditionType type : Activity.ChangeServerActivityConditionType.values()){
            builder.addConditions(getDataGeter().getChangeServerActivityCondition(playerId, cfg.getToServerIdMap(), changeServerId, type));
        }
        builder.setRealChangeSeverId(getDataGeter().getRealChangeServerId(playerId, changeServerId, cfg.getToServerIdMap()));
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.CHANGE_SVR_ACTIVITY_CHANGE_CHECK_RESP, builder));
        return Result.success();
    }

    public Result<Integer> change(String playerId){
        if(state !=  ChangeSvrActivityState.CHANGE){
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        if(isChange(playerId)){
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        String changeServerId = getChangeServerId(playerId);
        ChangeServerTimeCfg cfg = getTimeCfg();
        for(Activity.ChangeServerActivityConditionType type : Activity.ChangeServerActivityConditionType.values()){
            Activity.ChangeServerActivityCondition.Builder builder = getDataGeter().getChangeServerActivityCondition(playerId, cfg.getToServerIdMap(), changeServerId, type);
            if(!builder.getIsDone()){
                return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
            }
        }
        //单次消耗
        List<Reward.RewardItem.Builder> counsumeList = new ArrayList<>();
        int cost = getChangeGold(playerId);
        if(cost > 0){
            Reward.RewardItem.Builder counsumeItem = RewardHelper.toRewardItem("10000_1000_1");
            counsumeItem.setItemCount(counsumeItem.getItemCount() * cost);
            counsumeList.add(counsumeItem);
            boolean costSuccess = getDataGeter().consumeItems(playerId, counsumeList, HP.code2.CHANGE_SVR_ACTIVITY_CHANGE_REQ_VALUE, Action.CHANGE_SVR);
            if (!costSuccess) {
                return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
            }
        }
        Map<String, Object> param = new HashMap<>();
        param.put("cost", cost);
        getDataGeter().logActivityCommon(playerId, LogInfoType.change_svr_real, param);
        int noArmyPoint = getNoArmyPower(playerId);
        updateShowS(creatNewInfo(playerId).setPower(noArmyPoint));
        setChange(playerId);
        boolean result = getDataGeter().onChangeServer(playerId, changeServerId, cfg.getToServerIdMap());

        return Result.success();
    }

    public Result<Integer> search(String playerId, int protoType, String name, int type){
        getDataGeter().onChangeServerSearch(playerId, protoType, name, type);
        return Result.success();
    }
}
