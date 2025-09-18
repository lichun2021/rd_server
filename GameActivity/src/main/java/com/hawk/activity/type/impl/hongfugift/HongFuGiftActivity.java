package com.hawk.activity.type.impl.hongfugift;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.HongFuGiftBuyEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.hongfugift.cfg.HongFuGiftActivityKVCfg;
import com.hawk.activity.type.impl.hongfugift.cfg.HongFuGiftCfg;
import com.hawk.activity.type.impl.hongfugift.cfg.HongFuRewardChooseCfg;
import com.hawk.activity.type.impl.hongfugift.entity.HongFuGiftEntity;
import com.hawk.activity.type.impl.hongfugift.entity.HongFuInfo;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 洪福礼包
 * @author hf
 */
public class HongFuGiftActivity extends ActivityBase {
    public final Logger logger = LoggerFactory.getLogger("Server");

    public HongFuGiftActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }
    @Override
    public ActivityType getActivityType() {
        return ActivityType.HONG_FU_GIFT_ACTIVITY;
    }


    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        HongFuGiftActivity activity = new HongFuGiftActivity(config.getActivityId(), activityEntity);
        return activity;
    }


    @Override
    public void onOpen() {
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for (String playerId : onlinePlayerIds) {
            callBack(playerId, GameConst.MsgId.ACHIEVE_INIT_HONG_FU_GIFT, ()-> {
                Optional<HongFuGiftEntity> opEntity = getPlayerDataEntity(playerId);
                if (!opEntity.isPresent()) {
                    logger.error("on HongFuGiftEntity open init HongFuGiftEntity error, no entity created:{}" , playerId);
                    return;
                }
                opEntity.get().recordLoginDay();
                syncActivityDataInfo(playerId);
            });
        }
    }
    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<HongFuGiftEntity> queryList = HawkDBManager.getInstance()
                .query("from HongFuGiftEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
            HongFuGiftEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        HongFuGiftEntity entity = new HongFuGiftEntity(playerId, termId);
        return entity;
    }


    @Subscribe
    public void onEvent(ContinueLoginEvent event) {
        String playerId = event.getPlayerId();
        if (!isOpening(playerId)) {
            return;
        }
        //活动数据不存在
        Optional<HongFuGiftEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        opEntity.get().recordLoginDay();
        //跨天
        if(event.isCrossDay()){
        	syncActivityDataInfo(playerId);
        }
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        //活动数据不存在
        Optional<HongFuGiftEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        HongFuGiftEntity entity = opEntity.get();
        HongFuGiftActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(HongFuGiftActivityKVCfg.class);
        int activityDays = kvCfg.getActivityDay();
        //当前是活动第几天(最高7天表配置)
        int currentDays = Math.min(getCurrentDays(), activityDays);
        Activity.HongFuGiftInfoSync.Builder allBuilder = Activity.HongFuGiftInfoSync.newBuilder();
        List<HongFuGiftCfg> giftCfgList = HawkConfigManager.getInstance().getConfigIterator(HongFuGiftCfg.class).toList();
        for (HongFuGiftCfg giftCfg : giftCfgList) {
            HongFuInfo hongFuInfo = entity.getHongFuInfoById(giftCfg.getGiftId());
            Activity.HongFuGiftPB.Builder builder = Activity.HongFuGiftPB.newBuilder();
            builder.setId(hongFuInfo.getId());
            builder.setRewardId(hongFuInfo.getRewardId());
            boolean isUnlock = hongFuInfo.isUnlock();
            builder.setIsUnlock(isUnlock);
            //可领取的天数
            int canRecDays = 0;
            //已经领取的天数
            int hasRecDays = 0;
            //未解锁可领取的天数
            int noLockCanRecDays = 0;
            if (isUnlock){
                hasRecDays = hongFuInfo.getRecDayList().size();
                //最多可领7天奖励
                canRecDays = Math.max((currentDays - hasRecDays), 0);
                if (canRecDays == 0){
                    builder.setNextRecTime(HawkTime.getNextAM0Date());
                }
            }else{
                noLockCanRecDays = currentDays;
            }
            builder.setCanRecDays(canRecDays);
            builder.setHasRecDays(hasRecDays);
            builder.setNoLockCanRecDays(noLockCanRecDays);
            allBuilder.addHongFuGiftPb(builder.build());
        }
        pushToPlayer(playerId, HP.code.HONGFU_GIFT_INFO_RESP_VALUE, allBuilder);
    }

    /**
     * 洪福礼包自选奖励
     * @param playerId
     * @param giftId
     * @param rewardId
     * @return
     */
    public Result<?> onChooseGiftReward(String playerId, int giftId, int rewardId) {
        //活动未开
        if (!isOpening(playerId)){
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //配置不存在
        HongFuGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(HongFuGiftCfg.class, giftId);
        if (giftCfg == null) {
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //配置不存在
        HongFuRewardChooseCfg chooseCfg = HawkConfigManager.getInstance().getConfigByKey(HongFuRewardChooseCfg.class, rewardId);
        if (chooseCfg == null) {
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //自选奖励参数错误
        if (chooseCfg.getGiftId() != giftId){
            return Result.fail(Status.Error.HONG_FU_CHOOSE_REWARD_INVALID_VALUE);
        }
        //活动数据不存在
        Optional<HongFuGiftEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        HongFuGiftEntity entity = opEntity.get();
        //礼包信息
        HongFuInfo hongFuInfo = entity.getHongFuInfoById(giftId);
        boolean isUnlock = hongFuInfo.isUnlock();
        //已经解锁的就不能更换自选奖励
        if (isUnlock){
            return Result.fail(Status.Error.HONG_FU_GIFT_HAS_UNLOCK_VALUE);
        }
        hongFuInfo.setRewardId(rewardId);
        entity.notifyUpdate();
        //push
        syncActivityDataInfo(playerId);
        logger.info("HongFuGiftActivity onChooseGiftReward is success playerId:{} giftId:{},rewardId:{}",playerId, giftId, rewardId);
        return Result.success();
    }


    /**
     * 免费解锁洪福礼包
     * @param playerId
     * @param giftId
     * @return
     */
    public Result<?> onUnlockFreeGift(String playerId, int giftId) {
        //活动未开
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //配置不存在
        HongFuGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(HongFuGiftCfg.class, giftId);
        if (giftCfg == null) {
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //不是免费礼包
        if (!giftCfg.isFree()){
            return Result.fail(Status.Error.HONG_FU_GIFT_NO_FREE_VALUE);
        }
        //活动数据不存在
        Optional<HongFuGiftEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        HongFuGiftEntity entity = opEntity.get();
        //礼包信息
        HongFuInfo hongFuInfo = entity.getHongFuInfoById(giftId);
        boolean isUnlock = hongFuInfo.isUnlock();
        //经解锁
        if (isUnlock){
            return Result.fail(Status.Error.HONG_FU_GIFT_HAS_UNLOCK_VALUE);
        }
        int chooseRewardId = hongFuInfo.getRewardId();
        //自选奖励未选定,无法解锁
        if (chooseRewardId <= 0){
            return Result.fail(Status.Error.HONG_FU_GIFT_NO_CHOOSE_REWARD_VALUE);
        }
        HongFuRewardChooseCfg chooseCfg = HawkConfigManager.getInstance().getConfigByKey(HongFuRewardChooseCfg.class, chooseRewardId);
        if (chooseCfg == null) {
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //自选奖励参数错误
        if (chooseCfg.getGiftId() != giftId){
            return Result.fail(Status.Error.HONG_FU_CHOOSE_REWARD_INVALID_VALUE);
        }
        //更新解锁状态
        hongFuInfo.setUnlock(1);
        entity.notifyUpdate();
        //固定的奖励
        List<Reward.RewardItem.Builder> unlockRewardList = RewardHelper.toRewardItemImmutableList(giftCfg.getUnlockReward());
        //发奖励
        this.getDataGeter().takeReward(playerId, unlockRewardList, 1, Action.HONG_FU_UNLOCK_GIFT_REWARD, true);
        //push
        syncActivityDataInfo(playerId);
        logger.info("HongFuGiftActivity onUnlockFreeGift is success playerId:{} giftId:{}",playerId,giftId);

        //打点
        this.getDataGeter().logHongFuGiftUnlock(playerId, this.getActivityTermId(), giftId);
        return Result.success();
    }

    /**
     * 领取已经解锁的礼包累计的奖励
     * @param playerId
     * @param giftId
     * @return
     */
    public Result<?> onReceiveGiftReward(String playerId, int giftId) {
        //活动未开
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //配置不存在
        HongFuGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(HongFuGiftCfg.class, giftId);
        if (giftCfg == null) {
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //活动数据不存在
        Optional<HongFuGiftEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        HongFuGiftEntity entity = opEntity.get();
        //礼包信息
        HongFuInfo hongFuInfo = entity.getHongFuInfoById(giftId);
        boolean isUnlock = hongFuInfo.isUnlock();
        //未解锁礼包无法领取奖励
        if (!isUnlock){
            return Result.fail(Status.Error.HONG_FU_GIFT_NO_UNLOCK_REC_VALUE);
        }
        int chooseRewardId = hongFuInfo.getRewardId();
        //自选奖励未选定,无法领奖
        if (chooseRewardId <= 0){
            return Result.fail(Status.Error.HONG_FU_GIFT_NO_CHOOSE_REWARD_VALUE);
        }
        HongFuRewardChooseCfg chooseCfg = HawkConfigManager.getInstance().getConfigByKey(HongFuRewardChooseCfg.class, chooseRewardId);
        //自选奖励参数错误
        if (chooseCfg.getGiftId() != giftId){
            return Result.fail(Status.Error.HONG_FU_CHOOSE_REWARD_INVALID_VALUE);
        }
        //可领取的天数奖励
        int canRecDays = checkRecRewardDay(hongFuInfo);
        //无奖励可领取
        if (canRecDays <= 0){
            return Result.fail(Status.Error.HONG_FU_GIFT_NO_REWARD_REC_VALUE);
        }
        //记录已领取的天数数据
        for (int i = 0; i < canRecDays; i++) {
            int dayTh = HawkTime.getYyyyMMddIntVal(-i);
            hongFuInfo.addRecDayList(dayTh);
        }
        entity.notifyUpdate();

        //应发奖励
        List<Reward.RewardItem.Builder> rewardList = new ArrayList<>();
        //固定的奖励
        List<Reward.RewardItem.Builder> fixRewardList = RewardHelper.toRewardItemImmutableList(giftCfg.getFixedReward());
        rewardList.addAll(fixRewardList);
        //自选奖励
        List<Reward.RewardItem.Builder> chooseRewardList = RewardHelper.toRewardItemImmutableList(chooseCfg.getReward());
        rewardList.addAll(chooseRewardList);
        //发奖励
        this.getDataGeter().takeReward(playerId, rewardList, canRecDays, Action.HONG_FU_REC_GIFT_REWARD, true);
        logger.info("HongFuGiftActivity onReceiveGiftReward is success playerId:{} giftId:{},canRecDays:{}",playerId,giftId,canRecDays);

        //push
        syncActivityDataInfo(playerId);
        //打点
        this.getDataGeter().logHongFuGiftRecReward(playerId, this.getActivityTermId(), giftId, canRecDays, chooseRewardId);
        return Result.success();
    }

    /**
     * 购买登录基金
     * @return
     */
    @Subscribe
    public void onHongFuGiftBuyEvent(HongFuGiftBuyEvent event) {
        String playerId = event.getPlayerId();
        String payId = event.getGiftId();
        //校验
        int result = checkBuySuccess(playerId, payId);
        if (result != Status.SysError.SUCCESS_OK_VALUE){
            logger.info("HongFuGiftActivity onHongFuGiftBuyEvent is fail playerId:{} payId:{}, result:{}",playerId, payId, result);
            return;
        }
        //活动数据不存在
        Optional<HongFuGiftEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return ;
        }
        HongFuGiftEntity entity = opEntity.get();
        HongFuGiftCfg hongFuGiftCfg =  getHongFuGiftCfg(payId);
        if (hongFuGiftCfg == null){
            return;
        }
        //校验成功
        int giftId = hongFuGiftCfg.getGiftId();
        //礼包信息
        HongFuInfo hongFuInfo = entity.getHongFuInfoById(giftId);
        //更新解锁状态
        hongFuInfo.setUnlock(1);
        entity.notifyUpdate();
        //固定的奖励
        List<Reward.RewardItem.Builder> unlockRewardList = RewardHelper.toRewardItemImmutableList(hongFuGiftCfg.getUnlockReward());
        //发奖励
        this.getDataGeter().takeReward(playerId, unlockRewardList, 1, Action.HONG_FU_UNLOCK_GIFT_REWARD, true);
        logger.info("HongFuGiftActivity onHongFuGiftBuyEvent is success playerId:{} giftId:{}",playerId,giftId);
        //打点
        this.getDataGeter().logHongFuGiftUnlock(playerId, this.getActivityTermId(), giftId);
        //push
        syncActivityDataInfo(playerId);
    }
    /**
     * 当前是活动第几天
     * @return
     */
    private int getCurrentDays() {
        int termId = this.getActivityTermId();
        long openTime = getTimeControl().getStartTimeByTermId(termId);
        int crossHour = getDataGeter().getCrossDayHour();
        int betweenDays = HawkTime.getCrossDay(HawkTime.getMillisecond(), openTime, crossHour);
        return betweenDays + 1;
    }

    /**
     * 购买礼包的校验
     * @param playerId
     * @param payId
     * @return
     */
    public int checkBuySuccess(String playerId, String payId) {
        if (!isOpening(playerId)) {
            HawkLog.errPrintln("HongFuGiftActivity checkBuySuccess failed, activity not open, playerId: {}, payId: {}", playerId, payId);
            return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
        }
        //配置不存在
        HongFuGiftCfg giftCfg = getHongFuGiftCfg(payId);
        if (giftCfg == null) {
            HawkLog.errPrintln("HongFuGiftActivity checkBuySuccess failed, HongFuGiftCfg not exist, playerId: {}, payId: {}", playerId, payId);
            return Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE;
        }
        int giftId = giftCfg.getGiftId();
        //活动数据不存在
        Optional<HongFuGiftEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            HawkLog.errPrintln("HongFuGiftActivity checkBuySuccess failed, entity data not exist, playerId: {}, giftId: {}", playerId, giftId);
            return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
        }

        //免费礼包不需要购买
        if (giftCfg.isFree()){
            HawkLog.errPrintln("HongFuGiftActivity checkBuySuccess failed, gift is free no need to buy, playerId: {}, giftId: {}", playerId, giftId);
            return Status.Error.HONG_FU_GIFT_NO_NEED_BUY_FREE_VALUE;
        }
        HongFuGiftEntity entity = opEntity.get();
        //礼包信息
        HongFuInfo hongFuInfo = entity.getHongFuInfoById(giftId);
        boolean isUnlock = hongFuInfo.isUnlock();
        //已经解锁
        if (isUnlock){
            HawkLog.errPrintln("HongFuGiftActivity checkBuySuccess failed, gift is has unlock, playerId: {}, giftId: {}", playerId, giftId);
            return Status.Error.HONG_FU_GIFT_HAS_UNLOCK_VALUE;
        }
        int chooseRewardId = hongFuInfo.getRewardId();
        //自选奖励未选定,无法解锁
        if (chooseRewardId <= 0){
            HawkLog.errPrintln("HongFuGiftActivity checkBuySuccess failed, chooseRewardId is null, playerId: {}, giftId: {}", playerId, giftId);
            return Status.Error.HONG_FU_GIFT_NO_CHOOSE_REWARD_VALUE;
        }
        HongFuRewardChooseCfg chooseCfg = HawkConfigManager.getInstance().getConfigByKey(HongFuRewardChooseCfg.class, chooseRewardId);
        if (chooseCfg == null) {
            return Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE;
        }
        //自选奖励参数错误
        if (chooseCfg.getGiftId() != giftId){
            HawkLog.errPrintln("HongFuGiftActivity checkBuySuccess failed, giftId is valid, playerId: {}, giftId: {}, chooseGiftId:{}", playerId, giftId, chooseCfg.getGiftId());
            return Status.Error.HONG_FU_CHOOSE_REWARD_INVALID_VALUE;
        }

        return Status.SysError.SUCCESS_OK_VALUE;
    }

    /**
     * 检测还可以领取几天奖励
     * @param hongFuInfo
     * @return
     */
    public int checkRecRewardDay(HongFuInfo hongFuInfo){
        //已经领取的天数
        List<Integer> hasRecDayList = hongFuInfo.getRecDayList();
        int hasRecTimes = hasRecDayList.size();
        HongFuGiftActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(HongFuGiftActivityKVCfg.class);
        //最多7天奖励
        int limitTimes = kvCfg.getActivityDay();
        // 7天已经领取过了
        if (hasRecTimes >= limitTimes){
            logger.info("HongFuGiftActivity checkRecRewardDay reward all rec giftId:{}, hasRecTimes:{}, limit:{}", hongFuInfo.getId(), hasRecTimes, limitTimes);
            return 0;
        }
        int todayTh = HawkTime.getYyyyMMddIntVal();
        //今天已经领取过了
        if (hasRecDayList.contains(todayTh)){
            logger.info("HongFuGiftActivity checkRecRewardDay reward today is rec giftId:{}, hasRecTimes:{}, todayTh:{}", hongFuInfo.getId(), hasRecTimes, todayTh);
            return 0;
        }
        //当前是活动第几天(最高7天表配置)
        int actCurrentDays = Math.min(getCurrentDays(), limitTimes);
        //通过活动第几天计算,还可以领取的天数
        int canRecDays = Math.max((actCurrentDays - hasRecTimes), 0);
        if (canRecDays <= 0){
            logger.info("HongFuGiftActivity checkRecRewardDay reward no times to rec giftId:{}, hasRecTimes:{}, actCurrentDays:{}", hongFuInfo.getId(), hasRecTimes, actCurrentDays);
            return 0;
        }
        return canRecDays;
    }

    /**
     * 获取礼包配置
     * @param payId
     * @return
     */
    public HongFuGiftCfg getHongFuGiftCfg(String payId){
        ConfigIterator<HongFuGiftCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(HongFuGiftCfg.class);
        while(configIterator.hasNext()){
            HongFuGiftCfg giftCfg = configIterator.next();
            if (giftCfg.getAndroidPayId().equals(payId) || giftCfg.getIosPayId().equals(payId)){
                return giftCfg;
            }
        }
        return null;
    }

}
