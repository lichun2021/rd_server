package com.hawk.activity.type.impl.homeland;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.HomeLandPuzzleBuyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.activity.type.impl.homeland.cfg.HomeLandPuzzleActivityKVCfg;
import com.hawk.activity.type.impl.homeland.cfg.HomeLandPuzzlePointShopCfg;
import com.hawk.activity.type.impl.homeland.cfg.HomeLandPuzzleShopCfg;
import com.hawk.activity.type.impl.homeland.entity.HomeLandPuzzleEntity;
import com.hawk.activity.type.impl.homeland.entity.HomeLandPuzzlePoolItem;
import com.hawk.activity.type.impl.homeland.entity.HomeLandPuzzlePoolType;
import com.hawk.activity.type.impl.homeland.entity.HomeLandPuzzleRecord;
import com.hawk.game.protocol.*;
import com.hawk.game.protocol.ActivityHomeLandPuzzle.HomeLandPuzzlePageInfo;
import com.hawk.game.protocol.ActivityHomeLandPuzzle.HomeLandPuzzleScratchRsp;
import com.hawk.game.protocol.ActivityHomeLandPuzzle.HomeLandPuzzleShopItemPB;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class HomeLandPuzzleActivity extends ActivityBase implements IExchangeTip<HomeLandPuzzlePointShopCfg> {
    public final Logger logger = LoggerFactory.getLogger("Server");

    @Override
    public ActivityType getActivityType() {
        return ActivityType.HOME_LAND_PUZZLE;
    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        HomeLandPuzzleActivity activity = new HomeLandPuzzleActivity(config.getActivityId(), activityEntity);
        return activity;
    }

    @Override
    public void onPlayerLogin(String playerId) {
        super.onPlayerLogin(playerId);
        if (this.isOpening(playerId)) {
            Optional<HomeLandPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
            opEntity.ifPresent(HomeLandPuzzleEntity::recordLoginDay);
        }
    }
    @Subscribe
    public void onEvent(BuildingLevelUpEvent event) {
        // 大本升级,检测活动是否关闭
        if(event.getBuildType() == Const.BuildingType.CONSTRUCTION_FACTORY_VALUE){
            this.syncActivityStateInfo(event.getPlayerId());
            this.syncActivityDataInfo(event.getPlayerId());
        }
    }
    @Subscribe
    public void onContinueLogin(ContinueLoginEvent event) {
        String playerId = event.getPlayerId();
        if (!isOpening(playerId)) {
            clearDrawConsumeItems(playerId);
            return;
        }
        if (!event.isCrossDay()) {
            return;
        }
        Optional<HomeLandPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        HomeLandPuzzleEntity entity = opEntity.get();
        for (Map.Entry<Integer, Integer> shopEntry : entity.getShopItemMap().entrySet()) {
            HomeLandPuzzleShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandPuzzleShopCfg.class, shopEntry.getKey());
            if (shopCfg == null) {
                continue;
            }
            if (shopCfg.getRefreshType() == 1) {
                shopEntry.setValue(0);
            }
        }
        for (Map.Entry<Integer, Integer> exchangeEntry : entity.getExchangeItemMap().entrySet()) {
            HomeLandPuzzlePointShopCfg exchangeCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandPuzzlePointShopCfg.class, exchangeEntry.getKey());
            if (exchangeCfg == null) {
                continue;
            }
            if (exchangeCfg.getRefreshType() == 1) {
                exchangeEntry.setValue(0);
            }
        }
        entity.recordLoginDay();
        entity.setFreeTimes(0);
        entity.notifyUpdate();
        this.syncActivityDataInfo(playerId);
    }

    @Override
    public void onEnd() {
        HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
            @Override
            public Object run() {
                Set<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
                for (String playerId : onlinePlayerIds) {
                    clearDrawConsumeItems(playerId);
                }
                return null;
            }
        });
    }

    /**
     * 活动结束清空抽奖券
     *
     * @param playerId
     */
    private void clearDrawConsumeItems(String playerId) {
        if (this.getDataGeter().isPlayerCrossIngorePlayerObj(playerId)) {
            return;
        }
        HomeLandPuzzleActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandPuzzleActivityKVCfg.class);
        Reward.RewardItem.Builder exchangeItem = RewardHelper.toRewardItem(cfg.getExchangeItem());
        int itemCount = getDataGeter().getItemNum(playerId, exchangeItem.getItemId());
        if (itemCount <= 0) {
            return;
        }
        Reward.RewardItem.Builder consumeItems = RewardHelper.toRewardItem(30000, exchangeItem.getItemId(), itemCount);
        this.getDataGeter().consumeItems(playerId, Collections.singletonList(consumeItems), 0, Action.HOME_PUZZLE_END_CONSUME);
    }

    public int canPayRMBGift(String playerId, String payGiftId) {
        if (!isOpening(playerId)) {
            return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
        }
        int giftId = HomeLandPuzzleShopCfg.getGiftId(payGiftId);
        HomeLandPuzzleShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandPuzzleShopCfg.class,
                giftId);
        if (shopCfg == null) {
            return Status.SysError.CONFIG_ERROR_VALUE;
        }
        Optional<HomeLandPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
        }
        HomeLandPuzzleEntity entity = opEntity.get();
        int shopCount = entity.getShopCount(giftId);
        if (shopCount >= shopCfg.getTimes()) {
            return Status.HomePuzzleError.HOME_PUZZLE_SHOP_BUY_LIMIT_VALUE;
        }
        return 0;
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        if (!this.isOpening(playerId)) {
            return;
        }
        Optional<HomeLandPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        HomeLandPuzzleEntity entity = opEntity.get();
        HomeLandPuzzlePageInfo.Builder builder = HomeLandPuzzlePageInfo.newBuilder();
        builder.setFreeTimes(entity.getFreeTimes());
        entity.getCollectedCombinationItemSet().forEach(builder::addCombinations);
        builder.setGrandPrize(entity.getGrandPrizeWon());
        builder.setScratchTimes(entity.getDrawCount());
        entity.getRecordItemMap().values().forEach(v -> builder.addRecord(v.buildRecord()));
        entity.getShopItemMap().forEach((key, value) -> {
            HomeLandPuzzleShopItemPB.Builder itemBuilder = HomeLandPuzzleShopItemPB.newBuilder();
            itemBuilder.setCfgId(key);
            itemBuilder.setItemCount(value);
            builder.addShopItem(itemBuilder);
        });
        entity.getExchangeItemMap().forEach((key, value) -> {
            HomeLandPuzzleShopItemPB.Builder itemBuilder = HomeLandPuzzleShopItemPB.newBuilder();
            itemBuilder.setCfgId(key);
            itemBuilder.setItemCount(value);
            builder.addExchangeItem(itemBuilder);
        });
        builder.addAllTips(getTips(HomeLandPuzzlePointShopCfg.class, entity.getTipSet()));
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.HOME_PUZZLE_ACT_INFO_S, builder));
    }

    public HomeLandPuzzleActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<HomeLandPuzzleEntity> queryList = HawkDBManager.getInstance()
                .query("from HomeLandPuzzleEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && !queryList.isEmpty()) {
            return queryList.get(0);
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        return new HomeLandPuzzleEntity(playerId, termId);
    }

    //直购买
    @Subscribe
    public void onBuyEvent(HomeLandPuzzleBuyEvent event) {
        String payForId = event.getGiftId();
        String playerId = event.getPlayerId();
        int giftId = HomeLandPuzzleShopCfg.getGiftId(payForId);
        HomeLandPuzzleShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandPuzzleShopCfg.class,
                giftId);
        if (shopCfg == null) {
            logger.error("onHomeLandPuzzleGiftBuy giftCfg is null playerId:{},payforId:{},giftId:{}", playerId, payForId, giftId);
            return;
        }
        Optional<HomeLandPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        HomeLandPuzzleEntity entity = opEntity.get();
        int shopCount = entity.getShopCount(shopCfg.getId());
        if (shopCount >= shopCfg.getTimes()) {
            return;
        }
        entity.addShopCount(shopCfg.getId(), 1);
        //发奖励
        this.getDataGeter().takeReward(playerId, RewardHelper.toRewardItemImmutableList(shopCfg.getGetItem()),
                1, Action.HOME_PUZZLE_BUY_AWARD, true);
        sendMailToPlayer(playerId, MailConst.MailId.HOME_PUZZLE_PURCHASE, null, null, null, RewardHelper.toRewardItemImmutableList(shopCfg.getGetItem()), true);
        //sync
        syncActivityDataInfo(playerId);
        HawkLog.logPrintln("HomePuzzleActivity,itemBuy,sucess,playerId: "
                + "{},cfgId:{},payForId:{}", playerId, shopCfg.getId(), payForId);
    }

    //商店购买
    public Result<?> onShopBuy(String playerId, int cfgId, int count) {
        //活动未开
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        HomeLandPuzzleShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandPuzzleShopCfg.class, cfgId);
        if (shopCfg == null) {
            return Result.fail(Status.SysError.CONFIG_ERROR_VALUE);
        }
        if (count <= 0 || count > shopCfg.getTimes()) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        if (shopCfg.getShopItemType() == 1) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        if (shopCfg.getShopItemType() != 0) {
            boolean flag = this.getDataGeter().cost(playerId, RewardHelper.toRewardItemImmutableList(shopCfg.getPayItem()), count,
                    Action.HOME_PUZZLE_BUY_COST, false);
            if (!flag) {
                return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
            }
        }
        Optional<HomeLandPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        HomeLandPuzzleEntity entity = opEntity.get();
        int shopCount = entity.getShopCount(shopCfg.getId());
        if (shopCount >= shopCfg.getTimes()) {
            return Result.fail(Status.HomePuzzleError.HOME_PUZZLE_SHOP_BUY_LIMIT_VALUE);
        }
        //增加购买次数，需判断每日刷新或是不刷新
        entity.addShopCount(shopCfg.getId(), count);
        //发奖励
        ImmutableList<Reward.RewardItem.Builder> reward = RewardHelper.toRewardItemImmutableList(shopCfg.getGetItem());
        this.getDataGeter().takeReward(playerId, reward,
                count, Action.HOME_PUZZLE_BUY_AWARD, true);
        //同步
        syncActivityDataInfo(playerId);
        if (shopCfg.getShopItemType() != 0) {
            List<Reward.RewardItem.Builder> mailRewardList = new ArrayList<>();
            for (Reward.RewardItem.Builder rewardBuilder : reward) {
                Reward.RewardItem.Builder mailReward = Reward.RewardItem.newBuilder();
                mailReward.setItemId(rewardBuilder.getItemId());
                mailReward.setItemCount(rewardBuilder.getItemCount() * count);
                mailReward.setItemType(rewardBuilder.getItemType());
                mailRewardList.add(mailReward);
            }
            sendMailToPlayer(playerId, MailConst.MailId.HOME_PUZZLE_PURCHASE, null, null, null, mailRewardList, true);
        }
        HawkLog.logPrintln("HomePuzzleActivity,itemBuy,sucess,playerId: "
                + "{},cfgId:{},ecount:{}", playerId, cfgId, count);
        return Result.success();
    }

    //兑换
    public Result<?> onShopExchange(String playerId, int cfgId, int count) {
        //活动未开
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        Optional<HomeLandPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        HomeLandPuzzleEntity entity = opEntity.get();
        HomeLandPuzzlePointShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandPuzzlePointShopCfg.class, cfgId);
        if (cfg == null) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        if (count <= 0 || count > cfg.getTimes()) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        // 判断道具足够否
        int eCount = entity.getExchangeCount(cfgId);
        if (eCount + count > cfg.getTimes()) {
            return Result.fail(Status.HomePuzzleError.HOME_PUZZLE_SHOP_EXCHANGE_LIMIT_VALUE);
        }
        boolean flag = this.getDataGeter().cost(playerId, RewardHelper.toRewardItemImmutableList(cfg.getNeedItem()), count,
                Action.HOME_PUZZLE_EXCHANGE_COST, false);
        if (!flag) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        //增加兑换次数
        entity.addExchangeCount(cfgId, count);
        //发奖励
        this.getDataGeter().takeReward(playerId, RewardHelper.toRewardItemImmutableList(cfg.getGainItem()),
                count, Action.HOME_PUZZLE_EXCHANGE_AWARD, true);
        //同步
        this.syncActivityDataInfo(playerId);
        HawkLog.logPrintln("HomePuzzleActivity,itemExchange,sucess,playerId: "
                + "{},exchangeType:{},ecount:{}", playerId, cfgId, eCount);
        return Result.success();
    }

    public Result<?> onScratch(String playerId, int count) {
        //活动未开
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        Optional<HomeLandPuzzleEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        HomeLandPuzzleEntity entity = opEntity.get();
        HomeLandPuzzleActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandPuzzleActivityKVCfg.class);
        if (cfg == null) {
            return Result.fail(Status.SysError.CONFIG_ERROR_VALUE);
        }
        if (count <= 0 || count > cfg.getScratchCardTimesLimit()) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        //判断免费次数
        boolean isFree = oneLotteryFree(entity);
        if (!isFree) {
            // 判断道具足够否
            boolean flag = this.getDataGeter().cost(playerId, RewardHelper.toRewardItemImmutableList(cfg.getScratchCardItemSave()), count,
                    Action.HOME_PUZZLE_SCRATCH_COST, false);
            if (!flag) {
                return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
            }
        }
        if (entity.getDrawCount() + count > cfg.getScratchCardTimesLimit()) {
            return Result.fail(Status.HomePuzzleError.HOME_PUZZLE_SCRATCH_LIMIT_VALUE);
        }
        long now = HawkTime.getMillisecond();
        HomeLandPuzzleRecord record = HomeLandPuzzleRecord.valueOf(now);
        List<Reward.RewardItem.Builder> combineRewardList = new ArrayList<>();
        for (int i = count; i > 0; i--) {
            // 增加计数器
            entity.setDrawCount(entity.getDrawCount() + 1);
            entity.setPCombine(entity.getPCombine() + 1);
            entity.setPGrandPrize(entity.getPGrandPrize() + 1);
            HomeLandPuzzlePoolItem rewardItem = draw(entity, cfg);
            if (rewardItem == null) {
                continue;
            }
            combineRewardList.addAll(RewardHelper.toRewardItemImmutableList(rewardItem.getItemInfo()));
            combineRewardList.addAll(RewardHelper.toRewardItemImmutableList(rewardItem.getPatternItem()));
            // 检查是否集齐了所有组合奖
            int combine = entity.getCollectedCombinationItemSet().size();
            if (entity.getCollectedCombinationItemSet().size() == cfg.getPuzzlePool(HomeLandPuzzlePoolType.COMBINATION).size()) {
                entity.getCollectedCombinationItemSet().clear();
                combineRewardList.addAll(RewardHelper.toRewardItemImmutableList(cfg.getGroupItems()));
            }
            String rewards = this.getDataGeter().getItemAward(cfg.getDrawGetAward());
            combineRewardList.addAll(RewardHelper.toRewardItemImmutableList(rewards));
            record.addNewRecord(rewardItem, combine, entity.getGrandPrizeWon(), rewards);
            HawkLog.debugPrintln("HomeLandPuzzleActivity draw playerId:{}," +
                            "pool:{},drawCount:{},pCombine:{},pPrize:{},prize:{},combines:{}", playerId, rewardItem.getType(),
                    entity.getDrawCount(), entity.getPCombine(), entity.getPGrandPrize(),
                    entity.getGrandPrizeWon(), entity.getCollectedCombinationItemSet().size());
        }
        if (isFree) {
            entity.setFreeTimes(entity.getFreeTimes() + 1);
        }
//        entity.getRecordItemMap().put(now, record);
//        while (getRecordMax(entity.getRecordItemMap()) > cfg.getDrawLogShowLimit()) {
//            Long eldestKey = entity.getRecordItemMap().keySet().iterator().next();
//            entity.getRecordItemMap().remove(eldestKey);
//        }
        //每次刮奖
        this.getDataGeter().takeReward(playerId, combineRewardList, 1, Action.HOME_PUZZLE_SCRATCH_AWARD, false);
        entity.notifyUpdate();
        //刮奖回调
        HomeLandPuzzleScratchRsp.Builder resp = HomeLandPuzzleScratchRsp.newBuilder();
        resp.setCount(count);
        resp.setReward(record.buildRecord());
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.HOME_PUZZLE_SCRATCH_S, resp));
        syncActivityDataInfo(playerId);
        HawkLog.logPrintln("HomeLandPuzzleActivity onScratch is success playerId:{},count:{},freeTime:{}," +
                        "drawCount:{},pCombine:{},pPrize:{},prize:{},combines:{}", playerId, count,
                entity.getFreeTimes(), entity.getDrawCount(), entity.getPCombine(), entity.getPGrandPrize(),
                entity.getGrandPrizeWon(), entity.getCollectedCombinationItemSet().size());
        return Result.success();
    }

    private int getRecordMax(Map<Long, HomeLandPuzzleRecord> recordMap) {
        int recordMax = 0;
        for (HomeLandPuzzleRecord recordLog : recordMap.values()) {
            recordMax += recordLog.getCombine().size();
            recordMax += recordLog.getNormal().size();
            recordMax += recordLog.getPrize().size();
        }
        return recordMax;
    }

    /**
     * 单抽是否免费
     *
     * @param entity
     * @return
     */
    private boolean oneLotteryFree(HomeLandPuzzleEntity entity) {
        HomeLandPuzzleActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandPuzzleActivityKVCfg.class);
        int freeCount = cfg.getDailyFreeDrawLimit();
        return entity.getFreeTimes() < freeCount;
    }

    /**
     * 核心抽奖方法。
     *
     * @param entity The current state of the player.
     * @return The reward item the player won.
     */
    public HomeLandPuzzlePoolItem draw(HomeLandPuzzleEntity entity, HomeLandPuzzleActivityKVCfg cfg) {

        // 保底系统检查 ---
        boolean grandPrizePityHit = entity.getPGrandPrize() >= cfg.getGrandPrizePityThreshold();
        boolean combinationPityHit = entity.getPCombine() >= cfg.getCombinationPityThreshold();

        // 特殊规则：大奖优先
        if (grandPrizePityHit && entity.getGrandPrizeWon() < cfg.getGrandPrizeGlobalLimit()) {
            entity.setPGrandPrize(0);
            // 如果组合奖保底也同时触发，需要将其“挂起”到下一次。
            if (combinationPityHit && entity.getPItem() == 0) {
                // 找一个还没获得的组合奖，并挂起它。
                cfg.getPuzzlePool(HomeLandPuzzlePoolType.COMBINATION).stream()
                        .filter(p -> !entity.getCollectedCombinationItemSet().contains(p.getId()))
                        .findFirst()
                        .ifPresent(p -> entity.setPItem(p.getId()));
            }
            return processResult(cfg.getPuzzlePool(HomeLandPuzzlePoolType.GRAND_PRIZE).get(0), entity, cfg);
        }
        // 普通组合奖保底触发
        if (combinationPityHit) {
            entity.setPCombine(0);
            // 找一个未获得的组合奖作为奖励。
            HomeLandPuzzlePoolItem pityReward = cfg.getPuzzlePool(HomeLandPuzzlePoolType.COMBINATION).stream()
                    .filter(p -> !entity.getCollectedCombinationItemSet().contains(p.getId()))
                    .findFirst()
                    .orElse(cfg.getPuzzlePool(HomeLandPuzzlePoolType.NORMAL).get(0));
            return processResult(pityReward, entity, cfg);
        }

        // 概率抽奖 ---
        List<HomeLandPuzzlePoolItem> currentDrawablePool = buildDrawablePool(entity, cfg);
        HomeLandPuzzlePoolItem drawnItem = performWeightedDraw(currentDrawablePool, cfg);
        return processResult(drawnItem, entity, cfg);
    }

    /**
     * 处理抽奖结果，包含对组合奖和大奖的特殊逻辑。
     */
    private HomeLandPuzzlePoolItem processResult(HomeLandPuzzlePoolItem drawnItem, HomeLandPuzzleEntity entity, HomeLandPuzzleActivityKVCfg cfg) {
        HomeLandPuzzlePoolItem finalItem = drawnItem;
        if (drawnItem.getType() == HomeLandPuzzlePoolType.COMBINATION) {
            // 如果有被挂起的奖励，则优先发放那个。
            if (entity.getPItem() > 0) {
                int pendingId = entity.getPItem();
                Optional<HomeLandPuzzlePoolItem> opsItem = cfg.getPuzzlePool(HomeLandPuzzlePoolType.COMBINATION).stream()
                        .filter(p -> p.getId() == pendingId)
                        .findFirst();
                if (opsItem.isPresent()) {
                    finalItem = opsItem.get();
                }
                entity.setPItem(0);
            }
            entity.getCollectedCombinationItemSet().add(finalItem.getId());
            entity.setPCombine(0);
        }
        if (drawnItem.getType() == HomeLandPuzzlePoolType.GRAND_PRIZE) {
            entity.setGrandPrizeWon(entity.getGrandPrizeWon() + 1);
            entity.setPGrandPrize(0);
        }
        return finalItem;
    }

    /**
     * 动态构建下一次抽奖可用的奖池。
     */
    private List<HomeLandPuzzlePoolItem> buildDrawablePool(HomeLandPuzzleEntity entity, HomeLandPuzzleActivityKVCfg cfg) {
        List<HomeLandPuzzlePoolItem> pool = new ArrayList<>(cfg.getPuzzlePool(HomeLandPuzzlePoolType.NORMAL));

        List<HomeLandPuzzlePoolItem> availableCombinationItems = cfg.getPuzzlePool(HomeLandPuzzlePoolType.COMBINATION).stream()
                .filter(p -> !entity.getCollectedCombinationItemSet().contains(p.getId()))
                .collect(Collectors.toList());
        pool.addAll(availableCombinationItems);

        if (entity.getGrandPrizeWon() < cfg.getGrandPrizeGlobalLimit()) {
            pool.addAll(cfg.getPuzzlePool(HomeLandPuzzlePoolType.GRAND_PRIZE));
        }
        return pool;
    }

    /**
     * 从给定奖池中执行一次权重随机抽取。
     */
    private HomeLandPuzzlePoolItem performWeightedDraw(List<HomeLandPuzzlePoolItem> pool, HomeLandPuzzleActivityKVCfg cfg) {
        int totalWeight = pool.stream().mapToInt(HomeLandPuzzlePoolItem::getWeight).sum();
        if (totalWeight <= 0) {
            return cfg.getPuzzlePool(HomeLandPuzzlePoolType.NORMAL).get(0);
        }
        int randomValue = new Random().nextInt(totalWeight);

        int cumulativeWeight = 0;
        for (HomeLandPuzzlePoolItem poolItem : pool) {
            cumulativeWeight += poolItem.getWeight();
            if (randomValue < cumulativeWeight) {
                return poolItem;
            }
        }
        return pool.get(pool.size() - 1);
    }

    @Override
    public boolean isActivityClose(String playerId) {
        if (HawkOSOperator.isEmptyString(playerId)) {
            return true;
        }
        return !isPlayerOpen(playerId);
    }

    /**
     * 判断建筑工厂等级
     *
     * @return
     */
    public boolean isPlayerOpen(String playerId) {
        int cityLevel = this.getDataGeter().getConstructionFactoryLevel(playerId);
        HomeLandPuzzleActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandPuzzleActivityKVCfg.class);
        return cityLevel >= cfg.getBuildLevelLimit();
    }
}
