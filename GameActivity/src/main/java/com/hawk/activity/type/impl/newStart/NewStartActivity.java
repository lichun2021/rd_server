package com.hawk.activity.type.impl.newStart;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.NewStartActiveEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.newStart.cfg.NewStartBaseCfg;
import com.hawk.activity.type.impl.newStart.cfg.NewStartRewardCfg;
import com.hawk.activity.type.impl.newStart.entity.NewStartEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;
import com.hawk.log.LogConst;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class NewStartActivity extends ActivityBase {
    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger("Server");

    public NewStartActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.NEW_START;
    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        NewStartActivity activity = new NewStartActivity(config.getActivityId(), activityEntity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        //根据条件从数据库中检索
        List<NewStartEntity> queryList = HawkDBManager.getInstance()
                .query("from NewStartEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        //如果有数据的话，返回第一个数据
        if (queryList != null && !queryList.isEmpty()) {
            return queryList.get(0);
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        return new NewStartEntity(playerId, termId);
    }

    @Override
    public boolean isActivityClose(String playerId) {
        NewStartBaseCfg baseCfg = HawkConfigManager.getInstance().getKVInstance(NewStartBaseCfg.class);
        if(getDataGeter().getConstructionFactoryLevel(playerId) < baseCfg.getOpenLimit()){
            return true;
        }
        //获得玩家活动数据
        Optional<NewStartEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return true;
        }
        //获得玩家活动数据实体
        NewStartEntity entity = opEntity.get();
        if(!entity.isActive()){
            return true;
        }
        long now = HawkTime.getMillisecond();
        if(now < entity.getStartTime() || now > entity.getOverTime()){
            return true;
        }
        return false;
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        //获得玩家活动数据
        Optional<NewStartEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        NewStartEntity entity = opEntity.get();
        Activity.NewStartInfoResp.Builder resp = Activity.NewStartInfoResp.newBuilder();
        resp.setIsbind(entity.isBind());
        fillOldPlayerInfo(entity, resp);
        fillNewPlayerInfo(entity, resp);
        fillAwardItemInfo(entity, resp);
        fillTimeInfo(entity, resp);
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.NEW_START_INFO_RESP, resp));
    }

    public void fillOldPlayerInfo(NewStartEntity entity, Activity.NewStartInfoResp.Builder resp){
        Activity.NewStartPlayerInfo.Builder playerInfo = Activity.NewStartPlayerInfo.newBuilder();
        playerInfo.setId(entity.getOldPlayerId());
        playerInfo.setPlayeName(entity.getName());
        playerInfo.setVipLevel(entity.getVipLevel());
        playerInfo.setServerId(entity.getOldServerId());
        playerInfo.setPlayerLevel(entity.getPlayerLevel());
        playerInfo.setBaseLevel(entity.getBaseLevel());
        playerInfo.setIcon(entity.getIcon());
        playerInfo.setPfIcon(entity.getPfIcon());
        resp.setOldInfo(playerInfo);
    }

    public void fillNewPlayerInfo(NewStartEntity entity, Activity.NewStartInfoResp.Builder resp){
        Activity.NewStartPlayerInfo.Builder playerInfo = Activity.NewStartPlayerInfo.newBuilder();
        playerInfo.setId(entity.getPlayerId());
        playerInfo.setPlayeName(getDataGeter().getPlayerName(entity.getPlayerId()));
        playerInfo.setVipLevel(getDataGeter().getVipLevel(entity.getPlayerId()));
        playerInfo.setServerId(getDataGeter().getServerId());
        resp.setNewInfo(playerInfo);
    }

    public void fillAwardItemInfo(NewStartEntity entity, Activity.NewStartInfoResp.Builder resp){
        for(int day : entity.getCfgMap().keySet()){
            int cfgId = entity.getCfgMap().getOrDefault(day, 0);
            int getCount = entity.getAwardMap().getOrDefault(day, 0);
            Activity.NewStartAwardItem.Builder item = Activity.NewStartAwardItem.newBuilder();
            item.setDay(day);
            item.setCfgId(cfgId);
            item.setGetCount(getCount);
            resp.addAwardItems(item);
        }
    }

    public void fillTimeInfo(NewStartEntity entity, Activity.NewStartInfoResp.Builder resp){
        resp.setCurDay(calCurDay(entity));
        resp.setStartTime(entity.getStartTime());
        resp.setEndTime(entity.getOverTime());
    }

    public int calCurDay(NewStartEntity entity){
        long now = HawkTime.getMillisecond();
        return (int)((now - entity.getStartTime()) / TimeUnit.DAYS.toMillis(1)) + 1;
    }

    @Subscribe
    public void onContinueLoginEvent(ContinueLoginEvent event){
        String playerId = event.getPlayerId();
        syncActivityDataInfo(playerId);
        syncActivityStateInfo(playerId);
    }

    @Subscribe
    public void onBuildingLevelUpEvent(BuildingLevelUpEvent event) {
        //不是主城不管
        if (event.getBuildType() != Const.BuildingType.CONSTRUCTION_FACTORY_VALUE) {
            return;
        }
        String playerId = event.getPlayerId();
        NewStartBaseCfg baseCfg = HawkConfigManager.getInstance().getKVInstance(NewStartBaseCfg.class);
        if(event.getLevel() >= baseCfg.getOpenLimit()){
            syncActivityDataInfo(playerId);
            syncActivityStateInfo(playerId);
        }
    }

    @Subscribe
    public void onActive(NewStartActiveEvent event){
        String playerId = event.getPlayerId();
        //获得玩家活动数据
        Optional<NewStartEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        NewStartEntity entity = opEntity.get();
        entity.setActive(true);
        entity.setOldPlayerId(event.getOldPlayerId());
        entity.setOldServerId(event.getOldServerId());
        entity.setName(event.getName());
        entity.setIcon(event.getIcon());
        entity.setPfIcon(event.getPfIcon());
        entity.setPlayerLevel(event.getPlayerLevel());
        entity.setVipLevel(event.getVipLevel());
        entity.setBaseLevel(event.getBaseLevel());
        entity.setHeroCount(event.getHeroCount());
        entity.setEquipTechLevel(event.getEquipTechLevel());
        entity.setJijiaLevel(event.getJijiaLevel());
        long starTimeZero = HawkTime.getAM0Date(new Date(HawkTime.getMillisecond())).getTime();
        NewStartBaseCfg baseCfg = HawkConfigManager.getInstance().getKVInstance(NewStartBaseCfg.class);
        entity.setStartTime(starTimeZero);
        entity.setOverTime(starTimeZero + baseCfg.getLastTime());
        fillCfg(entity);
        entity.notifyUpdate();
        syncActivityDataInfo(playerId);
        syncActivityStateInfo(playerId);
        Map<String, Object> param = new HashMap<>();
        param.put("oldPlayerId", event.getOldPlayerId());
        param.put("oldServerId", event.getOldServerId());
        param.put("oldPlayerLevel", event.getPlayerLevel());
        param.put("oldVipLevel", event.getVipLevel());
        param.put("oldCityLevel", event.getBaseLevel());
        param.put("oldHeroCount", event.getHeroCount());
        param.put("oldTechLevel", event.getEquipTechLevel());
        param.put("oldJijiaLevel", event.getJijiaLevel());
        getDataGeter().logActivityCommon(playerId, LogConst.LogInfoType.new_start_active, param);
    }

    public void fillCfg(NewStartEntity entity){
        for(NewStartEnum type : NewStartEnum.values()){
            NewStartRewardCfg cfg = getRewardCfg(type.getType(), getLevel(type, entity));
            if(cfg != null){
                entity.getCfgMap().put(cfg.getDay(), cfg.getId());
            }
        }
    }

    public int getLevel(NewStartEnum type, NewStartEntity entity){
        switch (type){
            case PLAYER_LEVEL:{
                return entity.getPlayerLevel();
            }
            case VIP_LEVEL:{
                return entity.getVipLevel();
            }
            case BASE_LEVEL:{
                return entity.getBaseLevel();
            }
            case HERO_COUNT:{
                return entity.getHeroCount();
            }
            case EQUIP_TECH_LEVEL:{
                return entity.getEquipTechLevel();
            }
            case JIJIA_LEVEL:{
                return entity.getJijiaLevel();
            }
        }
        return 0;
    }

    public NewStartRewardCfg getRewardCfg(int type, int level){
        ConfigIterator<NewStartRewardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(NewStartRewardCfg.class);
        for (NewStartRewardCfg cfg : iterator){
            if(cfg.getType() == type){
                String[] arr = cfg.getRange().split(",");
                if(level >= Integer.parseInt(arr[0]) && level <= Integer.parseInt(arr[1])){
                    return cfg;
                }
            }
        }
        return null;
    }

    public Result<Integer> info(String playerId) {
        syncActivityDataInfo(playerId);
        return Result.success();
    }

    public Result<Integer> bind(String playerId) {
        //获得玩家活动数据
        Optional<NewStartEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        NewStartEntity entity = opEntity.get();
        entity.setBind(true);
        syncActivityDataInfo(playerId);
        return Result.success();
    }

    public Result<Integer> award(String playerId, int day) {
        //获得玩家活动数据
        Optional<NewStartEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        NewStartEntity entity = opEntity.get();
        int curDay = calCurDay(entity);
        if(day > curDay){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        if (entity.getAwardMap().getOrDefault(day, 0) >= 7){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        int oldCount = entity.getAwardMap().getOrDefault(day, 0);
        int newCount = oldCount + 1;
        if(oldCount > curDay - day){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        entity.getAwardMap().put(day, newCount);
        entity.notifyUpdate();
        int cfgId = entity.getCfgMap().get(day);
        NewStartRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NewStartRewardCfg.class, cfgId);
        if(cfg != null && !HawkOSOperator.isEmptyString(cfg.getReward(newCount))){
            //发奖
            this.getDataGeter().takeReward(entity.getPlayerId(), RewardHelper.toRewardItemImmutableList(cfg.getReward(newCount)), 1 , Action.NEW_START_AWARD, true);
        }
        syncActivityDataInfo(playerId);
        return Result.success();
    }

    /**
     * gm逻辑
     * @param map 参数
     * @return 结果
     */
    public String gm(Map<String, String> map){
        //要执行的gm指令
        String cmd = map.getOrDefault("cmd", "");
        switch (cmd) {
            case "active":{
                String playerId = map.get("playerId");
                String name = getDataGeter().getPlayerName(playerId);
                int icon = getDataGeter().getIcon(playerId);
                String pfIcon = getDataGeter().getPfIcon(playerId);
                String oldPlayerId = playerId;
                String oldServerId = getDataGeter().getServerId();
                int playerLevel = getDataGeter().getPlayerLevel(playerId);
                int vipLevel = getDataGeter().getVipLevel(playerId);
                int baseLevel = getDataGeter().getConstructionFactoryLevel(playerId);
                int heroCount = 10;
                int equipTechLevel = 10;
                int jijiaLevel = 10;
                NewStartActiveEvent event = new NewStartActiveEvent(playerId,name, icon, pfIcon, oldPlayerId, oldServerId,
                        playerLevel, vipLevel, baseLevel, heroCount, equipTechLevel, jijiaLevel);
                ActivityManager.getInstance().postEvent(event);
                return "active success";
            }
        }
        return "no match cmd";
    }
}
