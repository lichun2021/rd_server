package com.hawk.activity.type.impl.honorRepay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.hawk.activity.constant.ObjType;
import com.hawk.activity.msg.PlayerRewardFromActivityMsg;
import com.hawk.game.protocol.*;
import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.HonorRepayBuyEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.honorRepay.cfg.HonorRepayAchieveCfg;
import com.hawk.activity.type.impl.honorRepay.cfg.HonorRepayGiftCfg;
import com.hawk.activity.type.impl.honorRepay.cfg.HonorRepayKVCfg;
import com.hawk.activity.type.impl.honorRepay.entity.HonorRepayEntity;
import com.hawk.game.protocol.Activity.HonorRepayInfo;
import com.hawk.game.protocol.Activity.HonorRepayPageInfo;
import com.hawk.game.protocol.Activity.RepayState;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 荣耀返利活动
 * hf
 */
public class HonorRepayActivity extends ActivityBase implements AchieveProvider{
    public final Logger logger = LoggerFactory.getLogger("Server");

    public HonorRepayActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.HONOR_REPAY_ACTIVITY;
    }

    @Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
    
    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        HonorRepayActivity activity  = new HonorRepayActivity(config.getActivityId(), activityEntity);
        AchieveContext.registeProvider(activity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<HonorRepayEntity> queryList = HawkDBManager.getInstance()
                .query("from HonorRepayEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
            HonorRepayEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        HonorRepayEntity entity = new HonorRepayEntity(playerId, termId);
        return entity;
    }

    @Subscribe
    public void onContinueLogin(ContinueLoginEvent event) {
        String playerId = event.getPlayerId();
        //检查并补发奖励
        checkAndSendRepayReward(playerId);
    }

    @Override
    public void onOpen() {
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for (String playerId : onlinePlayerIds) {
            callBack(playerId, MsgId.HONOR_REPAY_INIT, () -> {
                initAchieve(playerId);
                syncActivityDataInfo(playerId);
            });
        }
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        Optional<HonorRepayEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        HonorRepayEntity entity = opEntity.get();
        pushToPlayer(playerId, HP.code.HONOR_REPAY_INFO_SYNC_VALUE, genHonorRepayPageInfo(entity));
    }



    /**购买
     * @param playerId
     * @param num
     * @param protoType
     * @return
     */
    public Result<?> buyHonorRepay(String playerId, int num, int protoType){
        try {
            if (!isOpening(playerId)) {
                return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
            }
            Optional<HonorRepayEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
            if (!opPlayerDataEntity.isPresent()) {
                return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
            }

            HonorRepayEntity entity = opPlayerDataEntity.get();
            int hasBuyTimes = entity.getBuyimes();
            HonorRepayKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(HonorRepayKVCfg.class);
            if (hasBuyTimes + num > kvCfg.getLimitBuyTimes()) {
                return Result.fail(Status.Error.HONOR_REPAY_BUY_TIMES_LIMIT_VALUE);
            }
            HawkTuple2<Long, Long> buyRangeTimeTuple = getRepayBuyTimeTuple();
            long nowTime = HawkTime.getMillisecond();
            //不在时间范围内
            if (nowTime < buyRangeTimeTuple.first || nowTime > buyRangeTimeTuple.second) {
                return Result.fail(Status.Error.HONOR_REPAY_BUY_TIME_LIMIT_VALUE);
            }
            //需要消耗的物品
            List<RewardItem.Builder> consumeItemList = new ArrayList<RewardItem.Builder>();
            Reward.RewardItem.Builder costItem = RewardHelper.toRewardItem(kvCfg.getRepayCost());
            costItem.setItemCount(costItem.getItemCount() * num);
            consumeItemList.add(costItem);
            boolean success = getDataGeter().consumeItems(playerId, consumeItemList, protoType, Action.HONOR_REPAY_BUY_COST);
            if (!success) {
                logger.error("HonorRepayActivity buyHonorRepay consume not enought, playerId: {}", playerId);
                return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
            }

            List<RewardItem.Builder> currentReward = RewardHelper.toRewardItemImmutableList(kvCfg.getCurrentAward());
            //发奖励
            this.getDataGeter().takeReward(playerId, currentReward, num, Action.HONOR_REPAY_BUY_REWARD, true);

            entity.setBuyimes(entity.getBuyimes() + num);
            //update
            entity.notifyUpdate();
            //redis 存储
            String recordKey = getHonorRepayBuyPlayerKeys(playerId);
            // 记录返利
            //redis 中的记录
            String recordBuyTimesKey = getHonorRepayBuyTimesKeys(playerId);
            String buyTimesStr = String.valueOf(entity.getBuyimes());
            //更新购买的次数
            ActivityGlobalRedis.getInstance().set(recordBuyTimesKey, buyTimesStr, (int)TimeUnit.DAYS.toSeconds(60));

            Map<String, String> recordMap = ActivityGlobalRedis.getInstance().hgetAll(recordKey);
            if (recordMap == null || recordMap.isEmpty()) {
                ActivityGlobalRedis.getInstance().hmset(recordKey, initRecordRewardMap(), (int)TimeUnit.DAYS.toSeconds(60));
            }
            //entity 中的记录
            Map<Integer, Integer> entityRecordMap = entity.getReceiveRewardMap();
            if (entityRecordMap == null || entityRecordMap.isEmpty()) {
                entity.setReceiveRewardMap(initRecordRewardMapInt());
            }
            //购买次数事件
            ActivityManager.getInstance().postEvent(new HonorRepayBuyEvent(playerId, num));

            //push
            syncActivityDataInfo(playerId);

            //Tlog
            int termId = this.getActivityTermId();
            getDataGeter().logHonorRepayBuy(playerId, termId, num);

            logger.info("HonorRepayActivity buyHonorRepay success playerId:{}, num:{}", playerId, num);
            return Result.success();
        } catch (Exception e) {
            HawkException.catchException(e);
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
    }

    /**初始化4个礼包档位
     * @return
     */
    public Map<String, String> initRecordRewardMap(){
        Map<String, String> map = new HashMap<>();
        HonorRepayKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(HonorRepayKVCfg.class);
        int repayTimes = kvCfg.getRepayTimes();
        for (int i = 0; i < repayTimes; i++) {
            map.put(String.valueOf(i), String.valueOf(RepayState.REPAY_CAN_RECIEVE_VALUE));
        }
        return map;
    }

    /**初始化4个礼包档位
     * @return
     */
    public Map<Integer, Integer> initRecordRewardMapInt(){
        Map<Integer, Integer> map = new HashMap<>();
        HonorRepayKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(HonorRepayKVCfg.class);
        int repayTimes = kvCfg.getRepayTimes();
        for (int i = 0; i < repayTimes; i++) {
            map.put(i, RepayState.REPAY_CAN_RECIEVE_VALUE);
        }
        return map;
    }

    /**领取补给奖励
     * @param playerId
     * @param level 从0 开始
     * @param protoType
     * @return
     */
    public Result<?> receiveHonorRepayReward(String playerId, int level, int protoType){
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        Optional<HonorRepayEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
        if (!opPlayerDataEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        HawkTuple2<Long, Long> timeTuple = getRepayTimeTuple(level);
        long nowTime = HawkTime.getMillisecond();
        //不在时间范围内
        if (nowTime < timeTuple.first || nowTime > timeTuple.second) {
            return Result.fail(Status.Error.HONOR_REPAY_RECEIVE_TIME_LIMIT_VALUE);
        }
        HonorRepayEntity entity = opPlayerDataEntity.get();
        //未购买
        if (entity.getBuyimes() <= 0) {
            return Result.fail(Status.Error.HONOR_REPAY_RECEIVE_NO_BUY_VALUE);
        }
        Map<Integer, Integer> receiveRewardMap = entity.getReceiveRewardMap();
        if (receiveRewardMap == null || receiveRewardMap.isEmpty()) {
            return Result.fail(Status.Error.HONOR_REPAY_RECEIVE_NO_BUY_VALUE);
        }
        //已经领取
        if (receiveRewardMap.get(level) == RepayState.REPAY_RECIEVED_VALUE) {
            return Result.fail(Status.Error.HONOR_REPAY_RECEIVED_VALUE);
        }
        HonorRepayKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(HonorRepayKVCfg.class);
        List<RewardItem.Builder> allRewardList = new ArrayList<>();
        List<RewardItem.Builder> repayRewardList = kvCfg.getRepayRewardList();
        int buyTimes = entity.getBuyimes();

        for (RewardItem.Builder item : repayRewardList) {
            RewardItem.Builder copyRewardItem = RewardHelper.toRewardItem(item.getItemType(), item.getItemId(), item.getItemCount() * buyTimes);
            allRewardList.add(copyRewardItem);
        }
        //发奖
        //this.getDataGeter().takeReward(playerId, allRewardList, Action.HONOR_REPAY_REBATE_REWARD, true);

        //这个消息不支持多个,只能发多次消息.
        HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, playerId);
        PlayerRewardFromActivityMsg msg = PlayerRewardFromActivityMsg.valueOf(allRewardList, this.takeRewardAction(), true, RewardOrginType.ACTIVITY_REWARD, this.getActivityId());
        HawkTaskManager.getInstance().postMsg(xid, msg);

        //更新db领奖状态
        receiveRewardMap.put(level, RepayState.REPAY_RECIEVED_VALUE);
        entity.notifyUpdate();
        //更新redis的领奖数据
        String recordKey = getHonorRepayBuyPlayerKeys(playerId);
        ActivityGlobalRedis.getInstance().hset(recordKey, String.valueOf(level), String.valueOf(RepayState.REPAY_RECIEVED_VALUE));
        //push
        syncActivityDataInfo(playerId);

        //Tlog
        int termId = this.getActivityTermId();
        getDataGeter().logHonorRepayReceiveReward(playerId, termId, entity.getBuyimes(), 1);

        logger.info("HonorRepayActivity receiveHonorRepayReward success playerId:{}, level:{}, buyTimes:{}", playerId, level, buyTimes);

        return Result.success();
    }

    /**检查并补发奖励
     * @param playerId
     */
    public void checkAndSendRepayReward(String playerId){
        try {
            HonorRepayKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(HonorRepayKVCfg.class);
            long nowTime = HawkTime.getMillisecond();
            HawkTuple2<Long, Long> buyRangeTimeTuple = getRepayBuyTimeTuple();
            if(nowTime > buyRangeTimeTuple.second){
                Reward.RewardItem.Builder costItem = RewardHelper.toRewardItem(kvCfg.getRepayCost());
                int num = getDataGeter().getItemNum(playerId, costItem.getItemId());
                if(num > 0){
                    logger.error("HonorRepayActivity checkAndSendRepayReward consume enought, playerId: {}, itemid:{}, count:{}", playerId, costItem.getItemId(), num);
                    List<RewardItem.Builder> consumeItemList = new ArrayList<RewardItem.Builder>();
                    costItem.setItemCount(num);
                    consumeItemList.add(costItem);
                    boolean success = getDataGeter().consumeItems(playerId, consumeItemList, 1, Action.HONOR_REPAY_BUY_COST);
                    if (success) {
                        Reward.RewardItem.Builder rewardItem = RewardHelper.toRewardItem(kvCfg.getConvertedGold());
                        rewardItem.setItemCount(rewardItem.getItemCount() * num);
                        //补发
                        List<RewardItem.Builder> rewardList = new ArrayList<>();
                        rewardList.add(rewardItem);
                        //邮件发送奖励
                        Object[] title = new Object[0];
                        Object[] subTitle = new Object[0];
                        Object[] content = new Object[0];
                        //发邮件
                        sendMailToPlayer(playerId, MailConst.MailId.HONOR_REPAY_GOLD, title, subTitle, content, rewardList);
                    }else {
                        logger.error("HonorRepayActivity checkAndSendRepayReward consume not enought, playerId: {}", playerId);
                    }
                }
            }
            String recordBuyTimesKey = getHonorRepayBuyTimesKeys(playerId);
            String buyTimesStr = ActivityGlobalRedis.getInstance().get(recordBuyTimesKey);
            if (StringUtils.isEmpty(buyTimesStr)) {
                return;
            }
            int buyTimes = Integer.valueOf(buyTimesStr);
            String recordKey = getHonorRepayBuyPlayerKeys(playerId);
            Map<String, String> recordMap = ActivityGlobalRedis.getInstance().hgetAll(recordKey);
            if (recordMap == null || recordMap.isEmpty()) {
                logger.info("HonorRepayActivity checkAndSendRepayReward this redis receiveMap is empty  playerId:{}, level:{}, buyTimes:{}", playerId);
                return;
            }
            Map<Integer, HawkTuple2<Long, Long>> repayRangeTimeMap = getRepayTimeRangeMap();
            //取下db数据,如果活动过期取不到,
            Optional<HonorRepayEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
            for (Entry<Integer, HawkTuple2<Long, Long>> entry : repayRangeTimeMap.entrySet()) {
                int level = entry.getKey();
                //db数据 同时校验下
                if (opPlayerDataEntity.isPresent()) {
                    HonorRepayEntity entity = opPlayerDataEntity.get();
                    Map<Integer, Integer> receiveMap = entity.getReceiveRewardMap();
                    if (receiveMap == null || receiveMap.isEmpty()) {
                        logger.info("HonorRepayActivity checkAndSendRepayReward this db receiveMap is empty  playerId:{}, level:{}, buyTimes:{}", playerId, level, buyTimes);
                        return;
                    }
                    int entityState = receiveMap.get(level);
                    if (entityState == RepayState.REPAY_RECIEVED_VALUE) {
                        logger.info("HonorRepayActivity checkAndSendRepayReward this level is have received from db playerId:{}, level:{}, buyTimes:{}", playerId, level, buyTimes);
                        continue;
                    }
                }
                //redis 数据校验补发
                HawkTuple2<Long, Long> timeRangeTuple = entry.getValue();
                long endTime = timeRangeTuple.second;
                if (nowTime <= endTime) {
                    logger.info("HonorRepayActivity checkAndSendRepayReward this level is not arrival time  playerId:{}, level:{}, buyTimes:{}, recordMap:{}", playerId, level, buyTimes, recordMap.toString());
                    continue;
                }
                //redis超过时间才补发
                String stateStr = recordMap.get(String.valueOf(level));

                if (Integer.valueOf(stateStr) == RepayState.REPAY_RECIEVED_VALUE) {
                    logger.info("HonorRepayActivity checkAndSendRepayReward this level is have received from redis playerId:{}, level:{}, buyTimes:{}", playerId, level, buyTimes);
                    continue;
                }
                //补发
                List<RewardItem.Builder> lastRewardList = new ArrayList<>();
                List<RewardItem.Builder> rewardList = kvCfg.getRepayRewardList();
                for (RewardItem.Builder rewardItem : rewardList) {
                    RewardItem.Builder builder = RewardItem.newBuilder().setItemId(rewardItem.getItemId()).setItemType(rewardItem.getItemType()).setItemCount(rewardItem.getItemCount() * buyTimes);
                    lastRewardList.add(builder);
                }
                //邮件发送奖励
                Object[] title = new Object[0];
                Object[] subTitle = new Object[0];
                Object[] content = new Object[0];
                //发邮件
                sendMailToPlayer(playerId, MailConst.MailId.HONOR_REPAY_REWARD, title, subTitle, content, lastRewardList);
                //更新redis
                ActivityGlobalRedis.getInstance().hset(recordKey, String.valueOf(level), String.valueOf(RepayState.REPAY_RECIEVED_VALUE));

                //更新db数据
                if (opPlayerDataEntity.isPresent()) {
                    HonorRepayEntity entity = opPlayerDataEntity.get();
                    Map<Integer, Integer> receiveMap = entity.getReceiveRewardMap();
                    receiveMap.put(level, RepayState.REPAY_RECIEVED_VALUE);
                    entity.notifyUpdate();
                }
                //Tlog
                getDataGeter().logHonorRepayReceiveReward(playerId, this.getActivityTermId(), buyTimes, 2);

                logger.info("HonorRepayActivity checkAndSendRepayReward is success playerId:{}, level:{}, buyTimes:{}", playerId, level, buyTimes);
            }
            if (!opPlayerDataEntity.isPresent()) {
                //如果活动不存在,说明活动结束补发的,则删除redis数据
                ActivityGlobalRedis.getInstance().del(recordKey);
                ActivityGlobalRedis.getInstance().del(recordBuyTimesKey);
                logger.info("HonorRepayActivity checkAndSendRepayReward  activity end  delete redis key playerId:{}", playerId);
            }

        } catch (Exception e) {
            HawkException.catchException(e);
            logger.error("HonorRepayActivity checkAndSendRepayReward is Exception playerId:{}", playerId);
        }
    }

    /**根据档位获取时间范围
     * @param level
     * @return
     */
    public HawkTuple2<Long, Long> getRepayTimeTuple(int level){
        Map<Integer, HawkTuple2<Long, Long>> map = getRepayTimeRangeMap();

        HawkTuple2<Long, Long> timeRangeTupe = map.get(level);
        return timeRangeTupe;
    }

    /** 获取购买的时间范围
     * @return
     */
    public HawkTuple2<Long, Long> getRepayBuyTimeTuple(){
        HonorRepayKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(HonorRepayKVCfg.class);

        String purchaseTime = kvCfg.getPurchaseTime();
        String[] purchaseTimesArr = purchaseTime.split("_");
        int termId = this.getActivityTermId();
        long activityStartTime = this.getTimeControl().getStartTimeByTermId(termId);
        long startTime = Long.valueOf(purchaseTimesArr[0]) * 1000L + activityStartTime;
        long endTime = Long.valueOf(purchaseTimesArr[1]) * 1000L + activityStartTime;

        return new HawkTuple2<>(startTime, endTime);
    }

    /** 获取四个档位对应的时间范围 0 1 2 3
     * @return
     */
    public Map<Integer, HawkTuple2<Long, Long>> getRepayTimeRangeMap(){
        int termId = this.getActivityTermId();
        long activityStartTime = this.getTimeControl().getStartTimeByTermId(termId);
        HonorRepayKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(HonorRepayKVCfg.class);
        List<String> timeRangeList = kvCfg.getRepayTimeRangeList();
        //每个档位对应的时间范围,封装成map
        Map<Integer, HawkTuple2<Long, Long>> timeRangeMap = new HashMap<>();
        for (int i = 0; i < timeRangeList.size(); i++) {
            String timeRangeStr = timeRangeList.get(i);
            String range[] = timeRangeStr.split("_");
            long startTime = Long.valueOf(range[0]) * 1000L + activityStartTime;
            long endTime = Long.valueOf(range[1]) * 1000L + activityStartTime;

            timeRangeMap.put(i, new HawkTuple2<>(startTime, endTime));
        }
        return timeRangeMap;
    }
    /**玩家返利key
     * @param serverId
     * @return
     */
    private String getHonorRepayBuyPlayerKeys(String playerId) {
        return "activiy_honor_repay:" + playerId;
    }

    /**玩家购买次数key
     * @param playerId
     * @return
     */
    private String getHonorRepayBuyTimesKeys(String playerId) {
        return "activiy_honor_buy_times:" + playerId;
    }

    /***
     * 购买活动直购礼包事件
     * @param event
     */
    @Subscribe
    public void onEvent(PayGiftBuyEvent event) {
        String playerId = event.getPlayerId();
        String payforId = event.getGiftId();
        int giftId = HonorRepayGiftCfg.getGiftId(payforId);
        //不是该活动礼包
        if (giftId == 0) {
            return;
        }
        // 发货
        HonorRepayGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HonorRepayGiftCfg.class,giftId);
        if (cfg == null) {
            logger.error("HonorRepayActivity  PayGiftBuyEvent failed, cfg not exist, playerId: {}, giftId: {}",playerId, event.getGiftId());
            return;
        }
        if (!isOpening(playerId)) {
            return;
        }

        Optional<HonorRepayEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        pushToPlayer(playerId, HP.code.HONOR_REPAY_INFO_SYNC_VALUE, genHonorRepayPageInfo(opEntity.get()));
        logger.info("HonorRepayActivity PayGiftBuyEvent success, playerId:{},giftId:{}", playerId, giftId);
    }


    /**检查是否可以购买礼包
     * @param playerId
     * @param payforId
     * @return
     */
    public boolean canPayforGift(String playerId, String payforId) {
        int giftId = HonorRepayGiftCfg.getGiftId(payforId);
        // 发货
        HonorRepayGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HonorRepayGiftCfg.class,giftId);
        if (cfg == null) {
            return false;
        }
        if (!isOpening(playerId)) {
            return false;
        }
        Optional<HonorRepayEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return false;
        }
        HonorRepayEntity entity = opEntity.get();
        int buyTimes = entity.getBuyimes();
        //活动已经超次数,不让买礼包了,买完物品也无处消耗
        HonorRepayKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(HonorRepayKVCfg.class);
        if (buyTimes >= kvCfg.getLimitBuyTimes()) {
            return false;
        }
        int termId = this.getActivityTermId();
        long startTime = this.getTimeControl().getStartTimeByTermId(termId);
        int count = getDataGeter().getRechargeTimesAfter(entity.getPlayerId(), 2, cfg.getIosPayId(), cfg.getAndroidPayId(), startTime);
        if(count >= cfg.getTimes()){
            return false;
        }
        return true;
    }


    //初始化成就
    private void initAchieve(String playerId){
        Optional<HonorRepayEntity>  optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return;
        }
        //为空则初始化
        HonorRepayEntity entity = optional.get();
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        ConfigIterator<HonorRepayAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(HonorRepayAchieveCfg.class);
        List<AchieveItem> itemList = new ArrayList<>();
        while(configIterator.hasNext()){
            HonorRepayAchieveCfg cfg = configIterator.next();
            AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
            itemList.add(item);
        }
        entity.setItemList(itemList);
        //初始化成就
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemList), true);
    }

    /**协议生成
     * @param entity
     * @return
     */
    public HonorRepayPageInfo.Builder genHonorRepayPageInfo(HonorRepayEntity entity){
        HonorRepayPageInfo.Builder builder = HonorRepayPageInfo.newBuilder();
        builder.setBuyNum(entity.getBuyimes());

        Map<Integer, Integer> receiveRewardMap = entity.getReceiveRewardMap();
        if (receiveRewardMap != null && !receiveRewardMap.isEmpty()) {
            for (Entry<Integer, Integer> entry : receiveRewardMap.entrySet()) {
                HonorRepayInfo.Builder repayBuilder = HonorRepayInfo.newBuilder();
                repayBuilder.setLevel(entry.getKey());
                repayBuilder.setState(RepayState.valueOf(entry.getValue()));
                builder.addHonorRepayInfo(repayBuilder);
            }
        }
        int termId = this.getActivityTermId();
        long startTime = this.getTimeControl().getStartTimeByTermId(termId);
        ConfigIterator<HonorRepayGiftCfg> iter = HawkConfigManager.getInstance().getConfigIterator(HonorRepayGiftCfg.class);
        while(iter.hasNext()){
            HonorRepayGiftCfg cfg = iter.next();
            int count = getDataGeter().getRechargeTimesAfter(entity.getPlayerId(), 2, cfg.getIosPayId(), cfg.getAndroidPayId(), startTime);
            Activity.HonorRepayBuyCount.Builder countBuilder = Activity.HonorRepayBuyCount.newBuilder();
            countBuilder.setGiftId(cfg.getGiftId());
            countBuilder.setCount(count);
            builder.addHonorRepayBuyCount(countBuilder);
        }
        return builder;
    }


    @Override
    public boolean isProviderActive(String playerId) {
        return isOpening(playerId);
    }

    @Override
    public boolean isProviderNeedSync(String playerId) {
        return isShow(playerId);
    }

    @Override
    public Optional<AchieveItems> getAchieveItems(String playerId) {
        Optional<HonorRepayEntity> optional = getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return Optional.empty();
        }
        HonorRepayEntity entity = optional.get();
        if (entity.getItemList().isEmpty()) {
            this.initAchieve(playerId);
        }
        AchieveItems items = new AchieveItems(entity.getItemList(), entity);
        return Optional.of(items);
    }

    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        AchieveConfig cfg = HawkConfigManager.getInstance().getConfigByKey(HonorRepayAchieveCfg.class, achieveId);
        return cfg;
    }

    @Override
    public Action takeRewardAction() {
        return Action.HONOR_REPAY_TASK_REWARD;
    }

}
