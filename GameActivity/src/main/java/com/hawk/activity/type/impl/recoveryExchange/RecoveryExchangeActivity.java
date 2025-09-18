package com.hawk.activity.type.impl.recoveryExchange;

import java.util.*;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.recoveryExchange.cfg.RecoveryExchangeExchangeCfg;
import com.hawk.activity.type.impl.recoveryExchange.cfg.RecoveryExchangeRedundantItemCfg;
import com.hawk.activity.type.impl.recoveryExchange.cfg.RecoveryExchangeUselessItemCfg;
import com.hawk.activity.type.impl.recoveryExchange.entity.RecoveryExchangeEntity;
import com.hawk.game.protocol.Activity.*;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

/**
 * 道具回收活动
 * 将玩家背包中的垃圾道具进行回收，兑换成积分
 * 积分可赎回已回收的道具
 * 使用废旧道具进行精炼合成，生成新道具
 *
 * @author richard
 */
public class RecoveryExchangeActivity extends ActivityBase {

    private static final Logger logger = LoggerFactory.getLogger("Server");

    /**
     * 单次允许的最大兑换获得精华的数量
     */
    private static final int maxAddItemNum = 250000;

    public RecoveryExchangeActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.RECOVERY_EXCHANGE_ACTIVITY;
    }


    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        RecoveryExchangeActivity activity = new RecoveryExchangeActivity(config.getActivityId(), activityEntity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<RecoveryExchangeEntity> queryList = HawkDBManager.getInstance().query(
                "from RecoveryExchangeEntity where playerId = ? and termId = ? and invalid = 0",
                playerId, termId);

        if (queryList != null && queryList.size() > 0) {
            RecoveryExchangeEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        RecoveryExchangeEntity entity = new RecoveryExchangeEntity(playerId, termId);
        return entity;
    }

    /**
     * 活动开启
     */
    @Override
    public void onOpen() {
    }

    /**
     * 积分兑换物品
     *
     * @param playerId
     * @param exchangeId
     * @param exchangeCount
     */
    public void itemExchange(String playerId, int exchangeId, int exchangeCount) {
        //活动没开不处理
        if (!isOpening(playerId)) {
            return;
        }
        //取配置
        RecoveryExchangeExchangeCfg config = HawkConfigManager.getInstance().
                getConfigByKey(RecoveryExchangeExchangeCfg.class, exchangeId);

        if (config == null) {
            return;
        }
        //取玩家数据
        Optional<RecoveryExchangeEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        RecoveryExchangeEntity entity = opDataEntity.get();
        int eCount = entity.getExchangeTimes(exchangeId);
        //判断兑换次数，次数不足给错误码，不继续处理
        if (eCount + exchangeCount > config.getTimes()) {
            //错误码
            logger.info("RecoveryExchangeActivity,itemExchange,fail,countless,playerId: " + "{},exchangeType:{},ecount:{}",
                    playerId, exchangeId, eCount);
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                    HP.code2.ITEM_RECYCLE_ENTEGRAL_EXCHANGE_ITEMS_REQ_VALUE,
                    Status.Error.ITEM_RECYCLE_ENTEGRAL_EXCHANGE_TIMES_NOT_ENOUGH_VALUE);
            return;
        }
        //扣消耗
        List<RewardItem.Builder> makeCost = config.getNeedItemList();
        boolean cost = this.getDataGeter().cost(playerId, makeCost, exchangeCount,
                Action.ITEM_RECYCLE_EXCHANGE_COST, false);
        if (!cost) {
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                    HP.code2.ITEM_RECYCLE_ENTEGRAL_EXCHANGE_ITEMS_REQ_VALUE,
                    Status.Error.ITEM_NOT_ENOUGH_VALUE);
            return;
        }

        //记录兑换次数
        entity.addExchangeTimes(exchangeId, exchangeCount);
        //发奖励
        this.getDataGeter().takeReward(playerId, config.getGainItemList(),
                exchangeCount, Action.ITEM_RECYCLE_EXCHANGE_GAIN, true);
        //同步
        this.syncActivityInfo(playerId, entity);
        logger.info("RecoveryExchangeActivity,itemExchange,sucess,playerId: " + "{},exchangeType:{},ecount:{}",
                playerId, exchangeId, eCount);
    }

    /**
     * 物品回收获得积分请求处理
     *
     * @param playerId
     * @param req
     */
    public void onItemRecycleReq(String playerId, PBItemRecycleReq req) {
        //活动没开不处理
        if (!isOpening(playerId)) {
            return;
        }

        //取批量次数
        int loop = req.getRecycleItemsCount();
        if (loop <= 0) {
            return;
        }

        //取玩家数据
        Optional<RecoveryExchangeEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        RecoveryExchangeEntity entity = opDataEntity.get();
        //循环处理批量
        for (int i = 0; i < loop; ++i) {
            PBExchangeStruct recycleReq = req.getRecycleItems(i);
            itemRecycle(playerId, recycleReq.getExchangeId(), recycleReq.getCount(), entity);
        }
        //同步
        this.syncActivityInfo(playerId, entity);
    }

    /**
     * 物品回收获得积分的具体逻辑
     *
     * @param playerId
     * @param cfgId
     * @param count
     * @param entity
     */
    private void itemRecycle(String playerId, int cfgId, int count, RecoveryExchangeEntity entity) {


        //取配置
        RecoveryExchangeUselessItemCfg config = HawkConfigManager.getInstance().
                getConfigByKey(RecoveryExchangeUselessItemCfg.class, cfgId);

        if (config == null) {
            return;
        }
        if(config.getRecoveryIntegral().isEmpty()){
            return;
        }
        //单次获得物品超过上限会导致发放失败，这里先判断下
        long RewardCount = config.getRecoveryIntegral().get(0).getItemCount() * count;
        if(RewardCount > this.maxAddItemNum){
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                    HP.code2.ITEM_RECYCLE_RECYCLE_REQ_VALUE,
                    Status.Error.ITEM_RECYCLE_RECYCLE_EXCEED_THE_LIMIT_VALUE);
        }

        //扣除道具消耗
        List<RewardItem.Builder> makeCost = config.getItemId();
        boolean cost = this.getDataGeter().cost(playerId, makeCost, count,
                Action.ITEM_RECYCLE_RECYCLE_COST, false);
        //扣除失败给错误码
        if (!cost) {
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                    HP.code2.ITEM_RECYCLE_RECYCLE_REQ_VALUE,
                    Status.Error.ITEM_NOT_ENOUGH_VALUE);
            return;
        }
        //记录回收的道具
        entity.changeRecycleItem(cfgId, count);
        //发奖励
        this.getDataGeter().takeReward(playerId, config.getRecoveryIntegral(),
                count, Action.ITEM_RECYCLE_RECYCLE_GAIN, true);

        logger.info("RecoveryExchangeActivity,itemRecycle,sucess,playerId: " + "{},itemRecycle:{},ecount:{}",
                playerId, cfgId, count);
    }

    /**
     * 用积分赎回物品请求处理
     *
     * @param playerId
     * @param req
     */
    public void onRedemptionItemReq(String playerId, PBRedemptionReq req) {
        //活动没开不处理
        if (!isOpening(playerId)) {
            return;
        }

        //取批量次数
        int loop = req.getRedemptionItemsCount();
        if (loop <= 0) {
            return;
        }
        //取玩家数据
        Optional<RecoveryExchangeEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        RecoveryExchangeEntity entity = opDataEntity.get();

        for (int i = 0; i < loop; ++i) {
            PBExchangeStruct redemptionReq = req.getRedemptionItems(i);
            redemptionItem(playerId, redemptionReq.getExchangeId(), redemptionReq.getCount(), entity);
        }
        //同步
        this.syncActivityInfo(playerId, entity);
    }

    /**
     * 用积分赎回物品的具体逻辑
     *
     * @param playerId
     * @param cfgId
     * @param count
     * @param entity
     */
    private void redemptionItem(String playerId, int cfgId, int count, RecoveryExchangeEntity entity) {
        //取配置
        RecoveryExchangeUselessItemCfg config = HawkConfigManager.getInstance().
                getConfigByKey(RecoveryExchangeUselessItemCfg.class, cfgId);

        if (config == null) {
            return;
        }

        int recycleItemCount = entity.getRecycleItemCount(cfgId);
        if (recycleItemCount < count) {
            //待赎回的物品数量不足，不继续处理
            return;
        }

        if(config.getItemId().isEmpty()){
            return;
        }
        //单次获得物品超过上限会导致发放失败，这里先判断下
        long RewardCount = config.getItemId().get(0).getItemCount() * count;
        if(RewardCount > this.maxAddItemNum){
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                    HP.code2.ITEM_RECYCLE_RECYCLE_REQ_VALUE,
                    Status.Error.ITEM_RECYCLE_REDEMPTION_EXCEED_THE_LIMIT_VALUE);
        }

        //扣除道具
        List<RewardItem.Builder> makeCost = config.getRedeemIntegral();
        boolean cost = this.getDataGeter().cost(playerId, makeCost, count,
                Action.ITEM_RECYCLE_REDEMPTION_COST, false);
        if (!cost) {
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                    HP.code2.ITEM_RECYCLE_REDEMPTION_REQ_VALUE,
                    Status.Error.ITEM_NOT_ENOUGH_VALUE);
            return;
        }

        entity.changeRecycleItem(cfgId, -count);
        //发奖励
        this.getDataGeter().takeReward(playerId, config.getItemId(),
                count, Action.ITEM_RECYCLE_REDEMPTION_GAIN, true);

        logger.info("RecoveryExchangeActivity,redemptionItem,sucess,playerId: " + "{},itemRecycle:{},ecount:{}",
                playerId, cfgId, count);
    }

    /**
     * 物品精炼，分为普通和高级
     *
     * @param playerId
     */
    public void onItemRecovery(String playerId, PBRecoveryReq req) {
        if (!isOpening(playerId)) {
            return;
        }

        RecoveryExchangeRedundantItemCfg cfg = HawkConfigManager.getInstance().
                getConfigByKey(RecoveryExchangeRedundantItemCfg.class, req.getRecoveryId());

        if (null == cfg) {
            logger.error("RecoveryExchangeActivity onItemRecovery null == Cfg player:{} cfgId:{}",
                    playerId, req.getRecoveryId());
            return;
        }

        Optional<RecoveryExchangeEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return;
        }
        //取玩家数据对象
        RecoveryExchangeEntity entity = optional.get();

        if (req.getIsOrdinary()) {
            itemRecoveryOrdinary(playerId, req, cfg, entity);
        } else {
            itemRecoveryExtraordinary(playerId, req, cfg, entity);
        }
    }

    /**
     * 普通精炼
     *
     * @param playerId
     * @param req
     * @param cfg
     * @param entity
     */
    private void itemRecoveryOrdinary(String playerId, PBRecoveryReq req,
                                      RecoveryExchangeRedundantItemCfg cfg, RecoveryExchangeEntity entity) {

        if (cfg.getOrdinaryTimes() < entity.getRedTimes(cfg.getId()) + req.getCount()) {
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                    HP.code2.ITEM_RECYCLE_RECOVERY_REQ_VALUE,
                    Status.Error.ITEM_RECYCLE_RECOVERY_ORDINARY_TIMES_NOT_ENOUGH_VALUE);

        }

        RewardItem.Builder reqiredItemBuilder = RewardHelper.toRewardItem(req.getRequiredItem());

        List<RewardItem.Builder> costList = cfg.getOrdinaryItem(reqiredItemBuilder.getItemId());
        //检查消耗
        boolean cost = this.getDataGeter().cost(playerId, costList, req.getCount(),
                Action.ITEM_RECYCLE_RECOVERY_ORDINARY_COST, false);
        if (!cost) {
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                    HP.code2.ITEM_RECYCLE_RECOVERY_REQ_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
            return;
        }

        entity.addRedTimesMap(cfg.getId(), req.getCount());
        //发奖励
        this.getDataGeter().takeReward(playerId, cfg.getObtainedItem(),
                req.getCount(), Action.ITEM_RECYCLE_RECOVERY_ORDINARY_GAIN, true, Reward.RewardOrginType.ITEM_RECYCLE_RECOVERY);
        this.syncActivityInfo(playerId, entity);
    }

    /**
     * 高级精炼
     *
     * @param playerId
     * @param req
     * @param cfg
     * @param entity
     */
    private void itemRecoveryExtraordinary(String playerId, PBRecoveryReq req,
                                           RecoveryExchangeRedundantItemCfg cfg, RecoveryExchangeEntity entity) {

        if (cfg.getHighTimes() < entity.getRedHighTimes(cfg.getId()) + req.getCount()) {
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                    HP.code2.ITEM_RECYCLE_RECOVERY_REQ_VALUE,
                    Status.Error.ITEM_RECYCLE_RECOVERY_EXTRAORDINARY_TIMES_NOT_ENOUGH_VALUE);
        }

        RewardItem.Builder reqiredItemBuilder = RewardHelper.toRewardItem(req.getRequiredItem());

        List<RewardItem.Builder> costList = cfg.getHighExchangeItems(reqiredItemBuilder.getItemId());
        //检查消耗
        boolean cost = this.getDataGeter().cost(playerId, costList, req.getCount(),
                Action.ITEM_RECYCLE_RECOVERY_EXTRAORDINARY_COST, false);
        if (!cost) {
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                    HP.code2.ITEM_RECYCLE_RECOVERY_REQ_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
            return;
        }
        entity.addRedHighTimes(cfg.getId(), req.getCount());
        //发奖励
        this.getDataGeter().takeReward(playerId, cfg.getObtainedItem(),
                req.getCount(), Action.ITEM_RECYCLE_RECOVERY_EXTRAORDINARY_GAIN, true, Reward.RewardOrginType.ITEM_RECYCLE_RECOVERY);
        this.syncActivityInfo(playerId, entity);
    }

    /**
     * 玩家勾选信息处理
     *
     * @param playerId
     * @param tipsList
     */
    public void updateActivityTips(String playerId, List<PBItemRecycleTip> tipsList) {
        if (!isOpening(playerId)) {
            return;
        }
        if (tipsList.isEmpty()) {
            return;
        }
        Optional<RecoveryExchangeEntity> opt = getPlayerDataEntity(playerId);
        if (!opt.isPresent()) {
            return;
        }

        RecoveryExchangeEntity entity = opt.get();

        for (PBItemRecycleTip tip : tipsList) {
            updateOneTip(entity, tip.getId(), tip.getTip());
        }

        this.syncActivityInfo(playerId, entity);
    }

    private void updateOneTip(RecoveryExchangeEntity entity, int id, boolean isSelected) {
        RecoveryExchangeExchangeCfg config = HawkConfigManager.getInstance().getConfigByKey(
                RecoveryExchangeExchangeCfg.class, id);
        if (config == null) {
            return;
        }

        if (isSelected) {
            entity.removeTips(id);
        } else {
            entity.addTips(id);
        }
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        Optional<RecoveryExchangeEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        syncActivityInfo(playerId, opDataEntity.get());
    }

    /**
     * 信息同步
     *
     * @param playerId
     */
    public void syncActivityInfo(String playerId, RecoveryExchangeEntity entity) {
        PBItemRecycleResp.Builder builder = PBItemRecycleResp.newBuilder();
        entity.getExchangeTimes(builder);
        entity.getRedTimes(builder);
        entity.getRedHighTimes(builder);
        entity.getRecycleItems(builder);

        List<RecoveryExchangeExchangeCfg> eList = HawkConfigManager.getInstance()
                .getConfigIterator(RecoveryExchangeExchangeCfg.class).toList();
        List<Integer> carePoints = entity.getPlayerPoints();
        for (RecoveryExchangeExchangeCfg ecfg : eList) {
            //默认是全选，没勾选的会记录，这里有值的是没选中的，给客户端同步没勾选的
            if (carePoints.contains(ecfg.getId())) {
                builder.addTips(ecfg.getId());
            }
        }

        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(
                HP.code2.ITEM_RECYCLE_RESP, builder));
    }
}
