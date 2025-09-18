package com.hawk.activity.type.impl.backToNewFly;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import com.hawk.activity.type.impl.backToNewFly.cfg.BackToNewFlyKvCfg;
import com.hawk.activity.type.impl.backToNewFly.cfg.BackToNewFlyTimeCfg;
import com.hawk.activity.type.impl.backToNewFly.data.BackToNewFlyData;
import com.hawk.activity.type.impl.backToNewFly.entity.BackToNewFlyOldEntity;
import com.hawk.log.LogConst.LogInfoType;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkTime;

import java.util.*;

public class BackToNewFlyOldActivity extends ActivityBase {
    public BackToNewFlyOldActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.BACK_TO_NEW_FLY_OLD;
    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        BackToNewFlyOldActivity activity = new BackToNewFlyOldActivity(config.getActivityId(), activityEntity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        //根据条件从数据库中检索
        List<BackToNewFlyOldEntity> queryList = HawkDBManager.getInstance()
                .query("from BackToNewFlyOldEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        //如果有数据的话，返回第一个数据
        if (queryList != null && queryList.size() > 0) {
            BackToNewFlyOldEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        BackToNewFlyOldEntity entity = new BackToNewFlyOldEntity(playerId, termId);
        return entity;
    }

    @Override
    public void onPlayerLogin(String playerId) {
        Optional<BackToNewFlyOldEntity> optional = getPlayerDataEntity(playerId);
        if (!optional.isPresent()){
            return;
        }
        BackToNewFlyOldEntity entity = optional.get();
        BackFlowPlayer backFlowPlayer = this.getDataGeter().getBackFlowPlayer(playerId);
        if(backFlowPlayer == null){
            return;
        }
        if(this.checkFitLostParams(backFlowPlayer,entity)){
            int backTimes = backFlowPlayer.getBackCount();
            long startTime = HawkTime.getAM0Date(new Date(backFlowPlayer.getBackTimeStamp())).getTime();
            BackToNewFlyKvCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BackToNewFlyKvCfg.class);
            long continueTime = kvCfg.getOldDuration() - 1000;
            long overTime = startTime + continueTime;
            entity.setBackCount(backTimes);
            entity.setStartTime(startTime);
            entity.setOverTime(overTime);
            entity.notifyUpdate();
            logger.info("onPlayerLogin  checkFitLostParams init sucess,  playerId: "+
                            "{},backCount:{},backTime:{},startTime:{}.overTime:{}",
                    playerId,backTimes,backFlowPlayer.getBackTimeStamp(),startTime,overTime);
            int baseLevel = getDataGeter().getConstructionFactoryLevel(playerId);
            String openid = getDataGeter().getOpenId(playerId);
            BackToNewFlyData oldData = BackToNewFlyData.load(openid);
            if(oldData == null){
                BackToNewFlyData data = new BackToNewFlyData(openid, playerId, backTimes, baseLevel, startTime, overTime, HawkTime.getMillisecond());
                data.save();
                Map<String, Object> param = new HashMap<>();
                param.put("openId", openid);
                param.put("baseLevel", baseLevel);
                getDataGeter().logActivityCommon(playerId, LogInfoType.back_to_new_fly_old, param);
            }else {
                if(backTimes > oldData.getBackCount() || (backTimes == oldData.getBackCount() && baseLevel > oldData.getBaseLevel())){
                    BackToNewFlyData data = new BackToNewFlyData(openid, playerId, backTimes, baseLevel, startTime, overTime, HawkTime.getMillisecond());
                    data.save();
                    Map<String, Object> param = new HashMap<>();
                    param.put("openId", openid);
                    param.put("baseLevel", baseLevel);
                    getDataGeter().logActivityCommon(playerId, LogInfoType.back_to_new_fly_old, param);
                }
            }
        }
    }

    public boolean checkFitLostParams(BackFlowPlayer backFlowPlayer, BackToNewFlyOldEntity entity) {
        if(backFlowPlayer.getBackCount() <= entity.getBackCount()){
            logger.info("checkFitLostParams failed, BackCount data fail , playerId: "
                            + "{},backCount:{},entityBackCount:{}", backFlowPlayer.getPlayerId(),
                    backFlowPlayer.getBackCount(),entity.getBackCount());
            return false;
        }
        long backTime = backFlowPlayer.getBackTimeStamp();
        //如果在活动中，只更新期数，不更新其他数据
        if(backTime < entity.getOverTime() && backTime > entity.getStartTime()){
            entity.setBackCount(backFlowPlayer.getBackCount());
            entity.notifyUpdate();
            logger.info("checkFitLostParams failed,in activity, playerId: "
                    + "{},backCount:{},backTime:{}", entity.getPlayerId(),entity.getBackCount(),backTime);
            return false;
        }
        //停止触发，只更新期数，不更新其他数据
        if(!this.canTrigger(backTime)){
            entity.setBackCount(backFlowPlayer.getBackCount());
            entity.notifyUpdate();
            logger.info("checkFitLostParams failed,can not Trigger, playerId: "
                    + "{},backCount:{},backTime:{}", entity.getPlayerId(),entity.getBackCount(),backTime);
            return false;
        }
        int lossDays = backFlowPlayer.getLossDays();
        logger.info("checkFitLostParams sucess, playerId: "
                + "{},loss:{}", backFlowPlayer.getPlayerId(),lossDays);
        return true;
    }

    public boolean canTrigger(long backTime){
        int termId = this.getActivityTermId();
        BackToNewFlyTimeCfg cfg = HawkConfigManager.getInstance().
                getConfigByKey(BackToNewFlyTimeCfg.class, termId);
        if(cfg == null){
            return false;
        }
        if(backTime < cfg.getStartTimeValue()){
            return false;
        }
        if(backTime > cfg.getStopTriggerValue()){
            return false;
        }
        return true;
    }

    @Override
    public boolean isActivityClose(String playerId) {
        Optional<BackToNewFlyOldEntity> optional = getPlayerDataEntity(playerId);
        if (!optional.isPresent()){
            return true;
        }
        BackToNewFlyOldEntity entity = optional.get();
        long now = HawkTime.getMillisecond();
        if(now < entity.getStartTime() || now > entity.getOverTime()){
            return true;
        }
        return false;
    }
}
