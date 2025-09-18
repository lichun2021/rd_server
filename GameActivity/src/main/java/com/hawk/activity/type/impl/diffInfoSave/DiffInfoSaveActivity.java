package com.hawk.activity.type.impl.diffInfoSave;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AgencyRewardEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.DiffInfoSaveBuyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.msg.PlayerRewardFromActivityMsg;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.diffInfoSave.cfg.DiffInfoSaveBuyCfg;
import com.hawk.activity.type.impl.diffInfoSave.cfg.DiffInfoSaveKVCfg;
import com.hawk.activity.type.impl.diffInfoSave.entity.DiffInfoSaveEntity;
import com.hawk.game.protocol.*;
import com.hawk.gamelib.GameConst;
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
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DiffInfoSaveActivity  extends ActivityBase {

    public DiffInfoSaveActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.DIFF_INFO_SAVE;
    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        DiffInfoSaveActivity activity = new DiffInfoSaveActivity(config.getActivityId(), activityEntity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<DiffInfoSaveEntity> queryList = HawkDBManager.getInstance()
                .query("from DiffInfoSaveEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        //如果有数据的话，返回第一个数据
        if (queryList != null && queryList.size() > 0) {
            DiffInfoSaveEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        DiffInfoSaveEntity entity = new DiffInfoSaveEntity(playerId, termId);
        return entity;
    }

    @Override
    public boolean isActivityClose(String playerId) {
        Optional<DiffInfoSaveEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return true;
        }
        DiffInfoSaveKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DiffInfoSaveKVCfg.class);
        DiffInfoSaveEntity entity = opEntity.get();
        if(entity.getPopCnt() !=-1 && entity.getPopCnt() < cfg.getTaskCount()){
            return true;
        }
        return entity.isEnd();
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        syncActivityInfo(playerId);
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
        Optional<DiffInfoSaveEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        DiffInfoSaveEntity entity = opEntity.get();
        //构造发给前端的数据
        syncActivityInfo(entity);
    }

    /**
     * 同步数据
     * @param entity
     */
    public void syncActivityInfo(DiffInfoSaveEntity entity) {
        if(entity.isEnd()){
            return;
        }
        DiffInfoSaveBuyCfg cfg = getCurCfg(entity);
        if(cfg == null){
            return;
        }
        DiffInfoSaveKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(DiffInfoSaveKVCfg.class);
        Activity.DiffInformationSaveSync.Builder builder = Activity.DiffInformationSaveSync.newBuilder();
        builder.setIsProprietary(isProprietary());
        builder.setType(entity.getType());
        builder.setScore(entity.getScore());
        builder.setInitial(cfg.getInitialGoldValue());
        builder.setMax(cfg.getMaxGoldValue());
        if(entity.getPopCnt() >= kvCfg.getTaskCount()){
            builder.setNeedPop(true);
        }else {
            builder.setNeedPop(false);
        }
        long now = HawkTime.getMillisecond();
        if(!HawkTime.isSameDay(now, entity.getClickTime())
                && entity.getDotCnt() < 3
                && entity.getScore() >= (cfg.getMaxGoldValue() - cfg.getInitialGoldValue())){
            builder.setRedDot(true);
        }else {
            builder.setRedDot(false);
        }
        PlayerPushHelper.getInstance().pushToPlayer(entity.getPlayerId(), HawkProtocol.valueOf(HP.code2.DIFF_INFO_SAVE_SYNC, builder));
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

    private void addCount(String playerId){
        //获得玩家活动数据
        Optional<DiffInfoSaveEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        DiffInfoSaveEntity entity = opEntity.get();
        if(entity.getPopCnt() == -1){
            return;
        }
        entity.setPopCnt(entity.getPopCnt() + 1);
        DiffInfoSaveKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DiffInfoSaveKVCfg.class);
        if(entity.getPopCnt() >= cfg.getTaskCount()){
            syncActivityStateInfo(playerId);
            syncActivityInfo(entity);
            entity.setPopCnt(-1);
        }
    }

    private void addScore(String playerId){
        if(!isOpening(playerId)){
            return;
        }
        //获得玩家活动数据
        Optional<DiffInfoSaveEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        DiffInfoSaveEntity entity = opEntity.get();
        DiffInfoSaveBuyCfg cfg = getCurCfg(entity);
        if(cfg == null){
            return;
        }
        int add = cfg.getIncremental();
        if(entity.getScore() + add > (cfg.getMaxGoldValue() - cfg.getInitialGoldValue())){
            entity.setScore(cfg.getMaxGoldValue() - cfg.getInitialGoldValue());
        }else {
            entity.setScore(entity.getScore() + add);
        }
        syncActivityInfo(entity);
    }

    private DiffInfoSaveBuyCfg getCurCfg(DiffInfoSaveEntity entity){
        ConfigIterator<DiffInfoSaveBuyCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(DiffInfoSaveBuyCfg.class);
        for(DiffInfoSaveBuyCfg cfg : iterator){
            if(isProprietary()){
                if(!cfg.isService()){
                    continue;
                }
                if(entity.getType() == cfg.getType()){
                    return cfg;
                }
            }else {
                if(cfg.isService()){
                    continue;
                }
                if(entity.getType() == cfg.getType()){
                    return cfg;
                }
            }
        }
        return null;
    }

    private DiffInfoSaveBuyCfg getNextCfg(DiffInfoSaveEntity entity){
        ConfigIterator<DiffInfoSaveBuyCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(DiffInfoSaveBuyCfg.class);
        for(DiffInfoSaveBuyCfg cfg : iterator){
            if(isProprietary()){
                if(!cfg.isService()){
                    continue;
                }
                if(entity.getType() + 1 == cfg.getType()){
                    return cfg;
                }
            }else {
                if(cfg.isService()){
                    continue;
                }
                if(entity.getType() + 1 == cfg.getType()){
                    return cfg;
                }
            }
        }
        return null;
    }

    private DiffInfoSaveBuyCfg getPayCfg(int payGiftId){
        ConfigIterator<DiffInfoSaveBuyCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(DiffInfoSaveBuyCfg.class);
        for(DiffInfoSaveBuyCfg cfg : iterator){
            if(isProprietary()){
                if(!cfg.isService()){
                    continue;
                }
            }else {
                if(cfg.isService()){
                    continue;
                }
            }
            if(payGiftId == cfg.getAndroidPayId() || payGiftId == cfg.getIosPayId()){
                return cfg;
            }
        }
        return null;
    }

    @Subscribe
    public void onAgencyFinish(AgencyRewardEvent event){
        addScore(event.getPlayerId());
        addCount(event.getPlayerId());
    }

    @Subscribe
    public void onAuthBuyEvent(DiffInfoSaveBuyEvent event){
        if(!checkAuthBuy(event.getPlayerId(),event.getPayGiftId())){
            return;
        }
        //获得玩家活动数据
        Optional<DiffInfoSaveEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        DiffInfoSaveEntity entity = opEntity.get();
        DiffInfoSaveBuyCfg cfg = getCurCfg(entity);
        DiffInfoSaveBuyCfg nextCfg = getNextCfg(entity);
        if(nextCfg == null){
            entity.setEnd(true);
            syncActivityStateInfo(entity.getPlayerId());
        }else {
            entity.setScore(0);
            entity.setType(nextCfg.getType());
            entity.setClickTime(0l);
            entity.setDotCnt(0);
            syncActivityInfo(entity);
        }
        List<Reward.RewardItem.Builder> rewardList = new ArrayList<>();
        Reward.RewardItem.Builder goldItem = Reward.RewardItem.newBuilder();
        goldItem.setItemType(Const.ItemType.PLAYER_ATTR_VALUE * GameConst.ITEM_TYPE_BASE);
        goldItem.setItemId(Const.PlayerAttr.DIAMOND_VALUE);
        goldItem.setItemCount(cfg.getMaxGoldValue());
        rewardList.add(goldItem);
        if (!rewardList.isEmpty()) {
            HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, entity.getPlayerId());
            Reward.RewardOrginType orginType = Reward.RewardOrginType.ACTIVITY_REWARD;
            PlayerRewardFromActivityMsg msg = PlayerRewardFromActivityMsg.valueOf(rewardList, Action.DIFF_INFO_SAVE_REWARD, true, orginType, 0);
            HawkTaskManager.getInstance().postMsg(xid, msg);
        }
        //this.getDataGeter().takeReward(entity.getPlayerId(), rewardList, 1 , Action.DIFF_INFO_SAVE_REWARD, true);
    }

    @Subscribe
    public void onContinueLoginEvent(ContinueLoginEvent event){
        String playerId = event.getPlayerId();
        //活动没开不可购买
        if (!isOpening(playerId)) {
            return;
        }
        //获得玩家活动数据
        Optional<DiffInfoSaveEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        DiffInfoSaveEntity entity = opEntity.get();
        if(entity.isEnd()){
            return;
        }
        syncActivityInfo(entity);
    }

    public boolean checkAuthBuy(String playerId, String payGiftId){
        //活动没开不可购买
        if (!isOpening(playerId)) {
            return false;
        }
        //获得玩家活动数据
        Optional<DiffInfoSaveEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return false;
        }
        DiffInfoSaveEntity entity = opEntity.get();
        if(entity.isEnd()){
            return false;
        }
        DiffInfoSaveBuyCfg cfg = getPayCfg(Integer.parseInt(payGiftId));
        if(cfg == null){
            return false;
        }
        if(isProprietary()){
            if(!cfg.isService()){
                return false;
            }
        }else {
            if (cfg.isService()){
                return false;
            }
        }
        if(cfg.getType() != entity.getType()){
            return false;
        }
        if(entity.getScore() < (cfg.getMaxGoldValue() - cfg.getInitialGoldValue())){
            return false;
        }
        return true;
    }

    public Result<Integer> click(String playerId) {
        Optional<DiffInfoSaveEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        DiffInfoSaveEntity entity = opEntity.get();
        if(entity.isEnd()){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        DiffInfoSaveBuyCfg cfg = getCurCfg(entity);
        if(entity.getScore() < (cfg.getMaxGoldValue() - cfg.getInitialGoldValue())){
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        long now = HawkTime.getMillisecond();
        if(HawkTime.isSameDay(now, entity.getClickTime())){
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        entity.setClickTime(now);
        entity.setDotCnt(entity.getDotCnt()+1);
        syncActivityInfo(entity);
        return Result.success();
    }
}
