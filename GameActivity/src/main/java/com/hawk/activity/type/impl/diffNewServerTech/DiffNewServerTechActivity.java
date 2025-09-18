package com.hawk.activity.type.impl.diffNewServerTech;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.diffNewServerTech.cfg.DiffNewServerTechKVCfg;
import com.hawk.activity.type.impl.diffNewServerTech.cfg.DiffNewServerTechRewardCfg;
import com.hawk.activity.type.impl.diffNewServerTech.entity.DiffNewServerTechEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DiffNewServerTechActivity extends ActivityBase {
	
    public DiffNewServerTechActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.DIFF_NEW_SERVER_TECH;
    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        DiffNewServerTechActivity activity = new DiffNewServerTechActivity(config.getActivityId(), activityEntity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<DiffNewServerTechEntity> queryList = HawkDBManager.getInstance()
                .query("from DiffNewServerTechEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        //如果有数据的话，返回第一个数据
        if (queryList != null && queryList.size() > 0) {
            DiffNewServerTechEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        DiffNewServerTechEntity entity = new DiffNewServerTechEntity(playerId, termId);
        return entity;
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        syncActivityInfo(playerId);
    }

    @Override
    public boolean isActivityClose(String playerId) {
        long openTime = getDataGeter().getServerOpenDate();
        DiffNewServerTechKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DiffNewServerTechKVCfg.class);
        if(openTime < cfg.getTimeValue()){
            return true;
        }
        long now = HawkTime.getMillisecond();
        int curDay = getCurDay(now);
        if(curDay > getMaxDay()){
            return true;
        }
        return false;
    }

    @Subscribe
    public void onContinueLoginEvent(ContinueLoginEvent event){
        long openTime = getDataGeter().getServerOpenDate();
        DiffNewServerTechKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DiffNewServerTechKVCfg.class);
        if(openTime < cfg.getTimeValue()){
            return;
        }
        long now = HawkTime.getMillisecond();
        int curDay = getCurDay(now);
        if(curDay > getMaxDay()){
            syncActivityStateInfo(event.getPlayerId());
        }else {
            syncActivityInfo(event.getPlayerId());
        }
    }

    /**
     * 同步数据
     * @param playerId
     */
    public void syncActivityInfo(String playerId) {
        //判断活动状态，如果活动未开启直接返回
        if(!isOpening(playerId)){
            return;
        }
        //获得玩家活动数据
        Optional<DiffNewServerTechEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        DiffNewServerTechEntity entity = opEntity.get();
        //构造发给前端的数据
        syncActivityInfo(entity);
    }

    /**
     * 同步数据
     * @param entity
     */
    public void syncActivityInfo(DiffNewServerTechEntity entity) {
        long now = HawkTime.getMillisecond();
        int curDay = getCurDay(now);
        DiffNewServerTechRewardCfg cfg = getCurCfg(curDay);
        if(cfg == null){
            return;
        }
        checkBuff(entity, cfg);
        Activity.DiffNewServerTechSync.Builder builder = Activity.DiffNewServerTechSync.newBuilder();
        builder.setIsProprietary(isProprietary());
        builder.setCurDay(curDay);
        builder.setCfgId(cfg.getId());
        builder.setEndTime(getEndTime(cfg));
        builder.addAllGet(entity.getRewardGetList());
        PlayerPushHelper.getInstance().pushToPlayer(entity.getPlayerId(), HawkProtocol.valueOf(HP.code2.DIFF_NEW_SERVER_TECH_SYNC, builder));
    }

    private boolean isProprietary(){
        try {
            String proprietaryServer = ActivityGlobalRedis.getInstance().hget("PROPRIETARY_SERVER", getDataGeter().getServerId());
            if(HawkOSOperator.isEmptyString(proprietaryServer) || !"1".equals(proprietaryServer)){
                return false;
            }
            return true;
        }catch (Exception e){
            HawkException.catchException(e);
            return false;
        }
    }

    public int getCurDay(long now){
        long startTime = getDataGeter().getServerOpenAM0Date();
        return (int)Math.ceil((now - startTime) * 1.0f / TimeUnit.DAYS.toMillis(1));
    }

    public int getMaxDay(){
        int day = 0;
        ConfigIterator<DiffNewServerTechRewardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(DiffNewServerTechRewardCfg.class);
        for(DiffNewServerTechRewardCfg cfg : iterator){
            if(isProprietary()){
                if(!cfg.isService()){
                    continue;
                }
            }else {
                if(cfg.isService()){
                    continue;
                }
            }
            if(cfg.getMax() > day){
                day = cfg.getMax();
            }
        }
        return day;
    }

    public DiffNewServerTechRewardCfg getCurCfg(int curDay){
        ConfigIterator<DiffNewServerTechRewardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(DiffNewServerTechRewardCfg.class);
        for (DiffNewServerTechRewardCfg cfg : iterator){
            if(isProprietary()){
                if(!cfg.isService()){
                    continue;
                }
            }else {
                if(cfg.isService()){
                    continue;
                }
            }
            if(curDay >= cfg.getMin() && curDay <= cfg.getMax()){
                return cfg;
            }
        }
        return null;
    }

    public long getEndTime(DiffNewServerTechRewardCfg cfg){
        long startTime = getDataGeter().getServerOpenAM0Date();
        return startTime + TimeUnit.DAYS.toMillis(cfg.getMax());
    }

    public void checkBuff(DiffNewServerTechEntity entity, DiffNewServerTechRewardCfg cfg){
        if(entity.getBuffGetList() != null && entity.getBuffGetList().contains(cfg.getId())){
            return;
        }
        entity.getBuffGetList().add(cfg.getId());
        entity.notifyUpdate();
        for (int buffId : cfg.getBuffList()) {
        	this.getDataGeter().addBuff(entity.getPlayerId(), buffId, getEndTime(cfg));
        }
    }

    public Result<Integer> award(String playerId, int cfgId){
        //判断活动是否开启，如果没开返回错误码
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //获得玩家活动数据
        Optional<DiffNewServerTechEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        long now = HawkTime.getMillisecond();
        int curDay = getCurDay(now);
        DiffNewServerTechEntity entity = opEntity.get();
        DiffNewServerTechRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DiffNewServerTechRewardCfg.class, cfgId);
        if(cfg == null){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        if(isProprietary()){
            if(!cfg.isService()){
                return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
            }
        }else {
            if(cfg.isService()){
                return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
            }
        }
        if(curDay < cfg.getMin() || curDay > cfg.getMax()){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        if(entity.getRewardGetList() !=null && entity.getRewardGetList().contains(cfgId)){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        entity.getRewardGetList().add(cfgId);
        entity.notifyUpdate();
        this.getDataGeter().takeReward(playerId, cfg.getRewardList(), 1, Action.DIFF_NEW_SERVER_TECH_REWARD, true);
        syncActivityInfo(entity);
        return Result.success();
    }
}
