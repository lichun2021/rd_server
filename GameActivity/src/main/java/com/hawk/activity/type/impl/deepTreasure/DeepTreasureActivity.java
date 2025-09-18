package com.hawk.activity.type.impl.deepTreasure;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.DeepTreasureOpenBoxEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.deepTreasure.cfg.*;
import com.hawk.activity.type.impl.deepTreasure.entity.DeepTreasureBox;
import com.hawk.activity.type.impl.deepTreasure.entity.DeepTreasureBuff;
import com.hawk.activity.type.impl.deepTreasure.entity.DeepTreasureEntity;
import com.hawk.activity.type.impl.deepTreasure.entity.DeepTreasureRandItem;
import com.hawk.game.protocol.Activity.*;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.Dress;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

public class DeepTreasureActivity extends ActivityBase implements AchieveProvider {
    public final Logger logger = LoggerFactory.getLogger("Server");

    public DeepTreasureActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.DEEP_TREASURE_ACTIVITY;
    }

    @Override
    public int providerActivityId() {
        return this.getActivityType().intValue();
    }

    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        DeepTreasureActivity activity = new DeepTreasureActivity(config.getActivityId(), activityEntity);
        AchieveContext.registeProvider(activity);
        return activity;
    }

    @Override
    public void onPlayerLogin(String playerId) {
        super.onPlayerLogin(playerId);
        if (this.isOpening(playerId)) {
            Optional<DeepTreasureEntity> opEntity = getPlayerDataEntity(playerId);
            opEntity.ifPresent(DeepTreasureEntity::recordLoginDay);
        }
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        if (this.isOpening(playerId)) {
            Optional<DeepTreasureEntity> opEntity = getPlayerDataEntity(playerId);
            if (!opEntity.isPresent()) {
                return;
            }
            DeepTreasureEntity entity = opEntity.get();
            PBDeepTreasureInfo.Builder builder = PBDeepTreasureInfo.newBuilder();
            builder.setNextFree(entity.getNextFree());
            builder.setRefreshtimes(entity.getRefreshtimes());
            builder.setPurchaseItemTimes(entity.getPurchaseItemTimes());
            builder.setLottoryCount(entity.getLotteryCount());
            for (DeepTreasureBox box : entity.getNineBox()) {
                builder.addNineBox(PBDeepTreasureBox.newBuilder().setPoolCfgId(box.getPoolCfgId()).setOpen(box.isOpen()));
            }
            for (DeepTreasureBuff buff : entity.getLotteryBuffMap().values()) {
                builder.addBuffList(PBDeepTreasureBuff.newBuilder().setBuffId(buff.getId()).setTimes(buff.getTimes()));
            }
            Map<Integer, Integer> emap = entity.getExchangeNumMap();

            for (Entry<Integer, Integer> entry : emap.entrySet()) {
                PBDeepTreasureExchange.Builder ebuilder = PBDeepTreasureExchange.newBuilder();
                ebuilder.setExchangeId(entry.getKey());
                ebuilder.setNum(entry.getValue());
                builder.addExchanges(ebuilder);
            }

            // push
            PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.DEEP_TREASURE_INFO_SYNC, builder));
        }
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<DeepTreasureEntity> queryList = HawkDBManager.getInstance()
                .query("from DeepTreasureEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && !queryList.isEmpty()) {
            return queryList.get(0);
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        DeepTreasureEntity entity = new DeepTreasureEntity(playerId, termId);
        return entity;
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
        Optional<DeepTreasureEntity> optional = getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return Optional.empty();
        }
        DeepTreasureEntity entity = optional.get();
        if (entity.getItemList().isEmpty()) {
            this.initAchieve(playerId);
        }
        AchieveItems items = new AchieveItems(entity.getItemList(), entity);
        return Optional.of(items);
    }

    @Override
    public void onOpen() {
        Set<String> playerIds = this.getDataGeter().getOnlinePlayers();
        for (String playerId : playerIds) {
            this.callBack(playerId, GameConst.MsgId.DEEP_TREASURE, () -> {
                initAchieve(playerId);
                this.syncActivityDataInfo(playerId);
            });
        }
    }

    // 初始化成就
    public void initAchieve(String playerId) {
        Optional<DeepTreasureEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return;
        }
        // 为空则初始化
        DeepTreasureEntity entity = optional.get();
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        ConfigIterator<DeepTreasureBoxAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(DeepTreasureBoxAchieveCfg.class);
        List<AchieveItem> itemList = new ArrayList<>();
        while (configIterator.hasNext()) {
            DeepTreasureBoxAchieveCfg cfg = configIterator.next();
            AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
            itemList.add(item);
        }
        entity.recordLoginDay();
        entity.setItemList(itemList);

        DeepTreasureActivityKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(DeepTreasureActivityKVCfg.class);
        for (Integer bid : kvcfg.getFirstExtractionList()) {
            DeepTreasureBox box = new DeepTreasureBox();
            box.setPoolCfgId(bid);
            entity.getNineBox().add(box);
        }

        ConfigIterator<DeepTreasureAwardPoolCfg> it = HawkConfigManager.getInstance().getConfigIterator(DeepTreasureAwardPoolCfg.class);
        for (int i = entity.getNineBox().size(); i < 9; i++) {
            DeepTreasureAwardPoolCfg cfg = HawkRand.randomWeightObject(it.toList());
            DeepTreasureBox box = new DeepTreasureBox();
            box.setPoolCfgId(cfg.getRewardId());
            entity.getNineBox().add(box);
        }
        entity.notifyUpdate();
        // 初始化成就
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemList), true);
    }

    @Override
    public Result<?> onTakeReward(String playerId, int achieveId) {
        DeepTreasureBoxAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(DeepTreasureBoxAchieveCfg.class, achieveId);
        if (achieveCfg == null) {
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        Optional<DeepTreasureEntity> optional = getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        optional.get().addLotteryBuff(achieveCfg);
        ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
        syncActivityDataInfo(playerId);
        return Result.success();
    }

    /***
     * 抽奖
     */
    public Result<?> lottery(String playerId, PBDeepTreasureOpenboxReq req) {
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        Optional<DeepTreasureEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        DeepTreasureEntity entity = opEntity.get();
        List<DeepTreasureBox> all = entity.getNineBox();
        List<DeepTreasureBox> toOpen = new ArrayList<>();
        if (req.getOpenAll()) {// 全开
            for (DeepTreasureBox box : all) {
                if (!box.isOpen()) {
                    toOpen.add(box);
                }
            }
        } else {
            DeepTreasureBox box = all.get(req.getIndex() - 1);
            if (!box.isOpen()) {
                toOpen.add(box);
            }
        }

        if (toOpen.isEmpty()) {
            return Result.success(null);
        }

        DeepTreasureActivityKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(DeepTreasureActivityKVCfg.class);
        int modelType = getDataGeter().getShowDress(playerId, Dress.DressType.TITLE_VALUE);
        boolean chenHao = modelType == kvcfg.getModelType();
        List<RewardItem.Builder> allReward = new ArrayList<>();
        for (DeepTreasureBox box : toOpen) {
            box.setOpen(true);
            DeepTreasureAwardPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(DeepTreasureAwardPoolCfg.class, box.getPoolCfgId());
            String rewards = getDataGeter().getItemAward(poolCfg.getReward());
            allReward.addAll(RewardHelper.toRewardItemImmutableList(rewards));
            if (chenHao) {
                allReward.addAll(RewardHelper.toRewardItemImmutableList(poolCfg.getExtraReward()));
            }
        }
        // 消耗道具
        RewardItem.Builder cost = RewardHelper.toRewardItem(kvcfg.getOpenTreasureCost());
        // 不消耗buff
        long costCount = cost.getItemCount() * toOpen.size();
        int toOpenLeft = toOpen.size();
        Optional<DeepTreasureBuff> noCostOpt = getLotteryBuff(LotteryBuff.NO_COST, entity);
        if (noCostOpt.isPresent()) {
            DeepTreasureBuff noCost = noCostOpt.get();
            int costTimes = Math.min(noCost.getTimes(), toOpen.size());
            costCount -= cost.getItemCount() * costTimes;
            toOpenLeft -= costTimes;
            noCost.setTimes(noCost.getTimes() - costTimes);
        }
        Optional<DeepTreasureBuff> costOpt = getLotteryBuff(LotteryBuff.REDUCE, entity);
        if (toOpenLeft > 0) {
            if (costOpt.isPresent()) {
                DeepTreasureBuff reduceOpt = costOpt.get();
                DeepTreasureBuffCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DeepTreasureBuffCfg.class, reduceOpt.getId());
                if (cfg != null) {
                    int costTimes = Math.min(reduceOpt.getTimes(), toOpenLeft);
                    costCount -= (long) cfg.getParam() * costTimes;
                    reduceOpt.setTimes(reduceOpt.getTimes() - costTimes);
                }
            }
        }
        cost.setItemCount(Math.max(costCount, 0));
        // 已有道具数量
        int itemCnt = getDataGeter().getItemNum(playerId, cost.getItemId());
        if (itemCnt < cost.getItemCount()) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        List<RewardItem.Builder> consumeList = new ArrayList<>();
        if (itemCnt >= cost.getItemCount()) {
            consumeList.add(cost);
        } else {
            // 还需要道具数量
            int needBuyCnt = (int) (cost.getItemCount() - itemCnt);
            // 单个物品价格
            RewardItem.Builder price = RewardHelper.toRewardItem(kvcfg.getPurchaseItemCost());
            // 总价格
            price.setItemCount(price.getItemCount() * needBuyCnt);
            consumeList.add(price);
            if (itemCnt > 0) {
                cost.setItemCount(itemCnt);
                consumeList.add(cost);
            }
        }
        boolean consumeResult = getDataGeter().consumeItems(playerId, consumeList, HP.code2.DEEP_TREASURE_OPEN_BOX_REQ_VALUE, Action.DEEP_TREASURE_LOTTERY_COST);
        if (!consumeResult) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }

        this.getDataGeter().takeReward(playerId, allReward, 1, Action.DEEP_TREASURE_LOTTERY, true);
        entity.setLotteryCount(entity.getLotteryCount() + toOpen.size());
        // // 抽奖事件
        ActivityManager.getInstance().postEvent(new DeepTreasureOpenBoxEvent(playerId, entity.getLotteryCount()));

        // 通报
        for (RewardItem.Builder send : allReward) {
            if (send.getItemId() == kvcfg.getNoticeItemPB().getItemId() && send.getItemCount() >= kvcfg.getNoticeItemPB().getItemCount()) {
                String playerName = getDataGeter().getPlayerName(playerId);
                this.addWorldBroadcastMsg(ChatType.SYS_BROADCAST, NoticeCfgId.DEEP_TREASURE_BIG_RAWARD, null, playerName, send.getItemCount());
            }
        }
        logger.info("DeepTreasure lottery player:{} lotCnt:{}, itemCnt:{}, modelType:{},noCostOpt:{},costReduce:{},toOpenLeft:{}", playerId,
                toOpen.size(), itemCnt, modelType, noCostOpt.isPresent() ? noCostOpt.get().getTimes() : "", costOpt.isPresent() ? costOpt.get().getTimes() : "", toOpenLeft);

        boolean openAll = true;
        for (DeepTreasureBox box : all) {
            if (!box.isOpen()) {
                openAll = false;
            }
        }
        if (openAll) {
            refreshNineBox(playerId, true);
        }

        syncActivityDataInfo(playerId);
        return Result.success(null);
    }

    /***
     *  单个宝箱开启道具消耗减少x%，持续n次（最终减免数量向上取整）
     *  单个宝箱开启不消耗道具，持续n次
     *  获得免费刷新n次
     *  奖池刷新时橙色宝箱刷出概率提升x%
     */
    public Optional<DeepTreasureBuff> getLotteryBuff(LotteryBuff lotterybuff, DeepTreasureEntity lotteryEntity) {
        if (!lotteryEntity.getLotteryBuffMap().containsKey(lotterybuff.value)) {
            return Optional.empty();
        }
        DeepTreasureBuff buff = lotteryEntity.getLotteryBuffMap().get(lotterybuff.value);
        if (buff.getTimes() <= 0) {
            return Optional.empty();
        }
        return Optional.of(buff);
    }

    /***
     * 刷新
     */
    public Result<?> refreshNineBox(String playerId, boolean sysRefresh) {
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        Optional<DeepTreasureEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }

        DeepTreasureEntity entity = opEntity.get();
        DeepTreasureActivityKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(DeepTreasureActivityKVCfg.class);
        if (entity.getRefreshtimes() >= kvcfg.getRefreshTimes()) {
            return Result.fail(Status.Error.SUPER_DISCOUNT_REFRESH_TIMES_VALUE);
        }

        boolean freeTime = entity.getNextFree() < HawkTime.getMillisecond();
        Optional<DeepTreasureBuff> refreshOpt = getLotteryBuff(LotteryBuff.FREE_REFRESH, entity);
        if (!sysRefresh && !freeTime && !refreshOpt.isPresent()) {
            List<RewardItem.Builder> cost = RewardHelper.toRewardItemImmutableList(kvcfg.getRefreshCost());
            boolean consumeResult = getDataGeter().consumeItems(playerId, cost, HP.code2.DEEP_TREASURE_REFRESH_BOX_REQ_VALUE, Action.DEEP_TREASURE_REFRESH);
            if (!consumeResult) {
                return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
            }
        }

        entity.getNineBox().clear();
        ConfigIterator<DeepTreasureAwardPoolCfg> it = HawkConfigManager.getInstance().getConfigIterator(DeepTreasureAwardPoolCfg.class);
        Optional<DeepTreasureBuff> refreshBoxOpt = getLotteryBuff(LotteryBuff.QUALITY_REFRESH, entity);
        List<DeepTreasureRandItem> all = new ArrayList<>();
        for (DeepTreasureAwardPoolCfg deepTreasureAwardPoolCfg : it.toList()) {
            int rewardId = deepTreasureAwardPoolCfg.getRewardId();
            int weight = refreshBoxOpt.isPresent() ? deepTreasureAwardPoolCfg.getBuffWeight() : deepTreasureAwardPoolCfg.getWeight();
            all.add(DeepTreasureRandItem.valueOf(rewardId, weight));
        }
        refreshBoxOpt.ifPresent(deepTreasureBuff -> deepTreasureBuff.setTimes(deepTreasureBuff.getTimes() - 1));
        for (int i = 0; i < 9; i++) {
            DeepTreasureRandItem cfg = HawkRand.randomWeightObject(all);
            DeepTreasureBox box = new DeepTreasureBox();
            box.setPoolCfgId(cfg.getRewardId());
            entity.getNineBox().add(box);
        }
        entity.setRefreshtimes(entity.getRefreshtimes() + 1);
        if (!sysRefresh && freeTime) {
            entity.setNextFree(HawkTime.getMillisecond() + kvcfg.getFreeRefreshTimes() * 1000L);
        }
        if (!sysRefresh && !freeTime) {
            refreshOpt.ifPresent(deepTreasureBuff -> deepTreasureBuff.setTimes(deepTreasureBuff.getTimes() - 1));
        }
        logger.info("DeepTreasure refresh player:{},sysRefresh:{},freeTime:{},refreshBoxOpt:{},refreshOpt:{}", playerId, sysRefresh, freeTime,
                refreshBoxOpt.isPresent() ? refreshBoxOpt.get() : "", refreshOpt.isPresent() ? refreshOpt.get() : "");
        entity.notifyUpdate();
        return Result.success(null);
    }

    @Subscribe
    public void onEvent(ContinueLoginEvent event) {
        String playerId = event.getPlayerId();
        if (!isOpening(playerId)) {
            return;
        }
        if (!event.isCrossDay()) {
            return;
        }
        Optional<DeepTreasureEntity> optional = getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return;
        }
        DeepTreasureEntity entity = optional.get();
        entity.recordLoginDay();
        this.syncActivityDataInfo(playerId);
    }

    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        AchieveConfig cfg = HawkConfigManager.getInstance().getConfigByKey(DeepTreasureBoxAchieveCfg.class, achieveId);
        return cfg;
    }

    @Override
    public Action takeRewardAction() {
        return Action.DEEP_TREASURE_REWARD;
    }

    @Override
    public void onPlayerMigrate(String playerId) {

    }

    @Override
    public void onImmigrateInPlayer(String playerId) {

    }

    public Result<?> purchaseItemCost(String playerId, int number) {
        Optional<DeepTreasureEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        DeepTreasureActivityKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(DeepTreasureActivityKVCfg.class);
        DeepTreasureEntity entity = opEntity.get();
        if (entity.getPurchaseItemTimes() + number > kvcfg.getPurchaseItemTimes()) {
            return Result.fail(Status.Error.ITEM_BUY_COUNT_EXCEED_VALUE);
        }

        // 消耗道具
        RewardItem.Builder cost = RewardHelper.toRewardItem(kvcfg.getPurchaseItemCost());
        cost.setItemCount(cost.getItemCount() * number);

        boolean consumeResult = getDataGeter().consumeItems(playerId, Collections.singletonList(cost), HP.code2.DEEP_TREASURE_BOX_ITEM_BUY_REQ_VALUE, Action.DEEP_TREASURE_BUY);
        if (!consumeResult) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }

        RewardItem.Builder get = RewardHelper.toRewardItem(kvcfg.getAccelerateItemId());
        get.setItemCount(get.getItemCount() * number);
        this.getDataGeter().takeReward(playerId, Collections.singletonList(get), 1, Action.DEEP_TREASURE_BUY, true);

        entity.setPurchaseItemTimes(entity.getPurchaseItemTimes() + number);
        this.syncActivityDataInfo(playerId);
        return Result.success(null);
    }

    /**
     * 道具兑换
     *
     * @param playerId
     */
    public void itemExchange(String playerId, int exchangeId, int exchangeCount, int protocolType) {
        DeepTreasureExchangeAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(DeepTreasureExchangeAwardCfg.class, exchangeId);
        if (config == null) {
            return;
        }
        Optional<DeepTreasureEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        DeepTreasureEntity entity = opDataEntity.get();
        int eCount = entity.getExchangeCount(exchangeId);
        if (eCount + exchangeCount > config.getLimittimes()) {
            logger.info("DeepTreasureActivity,itemExchange,fail,countless,playerId: "
                    + "{},exchangeType:{},ecount:{}", playerId, exchangeId, eCount);
            return;
        }

        List<RewardItem.Builder> makeCost = RewardHelper.toRewardItemImmutableList(config.getExchangerequirements());
        boolean cost = this.getDataGeter().cost(playerId, makeCost, exchangeCount, Action.DEEP_TREASURE_EXCAHNGE, true);
        if (!cost) {
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                    protocolType, Status.Error.ITEM_NOT_ENOUGH_VALUE);
            return;
        }

        // 增加兑换次数
        entity.addExchangeCount(exchangeId, exchangeCount);
        // 发奖励
        this.getDataGeter().takeReward(playerId, RewardHelper.toRewardItemImmutableList(config.getExchangeobtain()),
                exchangeCount, Action.DEEP_TREASURE_EXCAHNGE, true);
        // 同步
        this.syncActivityDataInfo(playerId);
        if (config.getNoticeId() > 0) {
            String playerName = getDataGeter().getPlayerName(playerId);
            this.addWorldBroadcastMsg(ChatType.SYS_BROADCAST, NoticeCfgId.valueOf(config.getNoticeId()), null, playerName);
        }
        logger.info("DeepTreasureActivity,itemExchange,sucess,playerId: "
                + "{},exchangeType:{},ecount:{}", playerId, exchangeId, eCount);

    }

    public enum LotteryBuff {
        REDUCE(1),
        NO_COST(2),
        FREE_REFRESH(3),
        QUALITY_REFRESH(4);
        public final int value;

        LotteryBuff(int value) {
            this.value = value;
        }
    }
}
