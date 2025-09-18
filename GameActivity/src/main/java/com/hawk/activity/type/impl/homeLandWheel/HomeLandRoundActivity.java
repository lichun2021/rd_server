package com.hawk.activity.type.impl.homeLandWheel;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.*;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.activity.type.impl.homeLandWheel.cfg.HomeLandRoundAchieveCfg;
import com.hawk.activity.type.impl.homeLandWheel.cfg.HomeLandRoundActivityFloorCfg;
import com.hawk.activity.type.impl.homeLandWheel.cfg.HomeLandRoundActivityKVCfg;
import com.hawk.activity.type.impl.homeLandWheel.cfg.HomeLandRoundShopCfg;
import com.hawk.activity.type.impl.homeLandWheel.entity.HomeLandRoundEntity;
import com.hawk.game.protocol.*;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class HomeLandRoundActivity extends ActivityBase implements AchieveProvider, IExchangeTip<HomeLandRoundShopCfg> {
    public final Logger logger = LoggerFactory.getLogger("Server");

    @Override
    public ActivityType getActivityType() {
        return ActivityType.HOME_LAND_ROUND;
    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        HomeLandRoundActivity activity = new HomeLandRoundActivity(config.getActivityId(), activityEntity);
        AchieveContext.registeProvider(activity);
        return activity;
    }

    @Override
    public void onPlayerLogin(String playerId) {
        super.onPlayerLogin(playerId);
        if (this.isOpening(playerId)) {
            this.initAcivityData(playerId);
        }
    }

    @Subscribe
    public void onContinueLogin(ContinueLoginEvent event) {
        String playerId = event.getPlayerId();
        if (!isOpening(playerId)) {
            return;
        }
        if (!event.isCrossDay()) {
            return;
        }
        Optional<HomeLandRoundEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        HomeLandRoundEntity entity = opEntity.get();
        entity.recordLoginDay();
        //初始化任务
        updateAchieveData(entity);
        //抛每天登录事件
        entity.notifyUpdate();
        this.syncActivityDataInfo(playerId);
    }

    public int draw(HomeLandRoundEntity entity, HomeLandRoundDrawType drawType) {
        int currentFloor = entity.getCurrentFloor();
        HomeLandRoundActivityFloorCfg cfg = HomeLandRoundActivityFloorCfg.getConfigList(currentFloor);
        // --- 决定使用哪个权重表 ---
        Map<Integer, Integer> weightsToUse;
        int pityCount = entity.getPityCount(currentFloor);
        if (drawType == HomeLandRoundDrawType.PROTECTED && pityCount >= cfg.getGuarantee() - 1) {
            weightsToUse = cfg.getGuaranteeWeightMap();
            entity.resetPityCount(currentFloor);
        } else {
            weightsToUse = cfg.getResultWeightMap();
        }
        // --- 抽取结果 ---
        int floorChange = performWeightedDraw(weightsToUse);
        // 0层不会降层
        if (currentFloor == 0 && floorChange < 0) {
            floorChange = 0;
        }
        return floorChange;
    }

    private int performWeightedDraw(Map<Integer, Integer> weights) {
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
        if (totalWeight <= 0) return 0; // 如果权重配置错误，则层数不变
        int randomValue = new Random().nextInt(totalWeight);
        int cumulativeWeight = 0;
        // 按key排序以保证抽取顺序稳定
        List<Map.Entry<Integer, Integer>> sortedWeights = weights.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).collect(Collectors.toList());

        for (Map.Entry<Integer, Integer> entry : sortedWeights) {
            cumulativeWeight += entry.getValue();
            if (randomValue < cumulativeWeight) {
                return entry.getKey();
            }
        }
        return 0; // Fallback
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        if (!this.isOpening(playerId)) {
            return;
        }
        Optional<HomeLandRoundEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        HomeLandRoundEntity entity = opEntity.get();
        ActivityHomeLandRound.HomeLandRoundPageInfo.Builder builder = ActivityHomeLandRound.HomeLandRoundPageInfo.newBuilder();
        builder.setDailyTimes(entity.getDrawTimes());
        entity.getExchangeItemMap().forEach((key, value) -> builder.addExchangeItem(ActivityHomeLandRound.HomeLandRoundItemPB.newBuilder().setItemCount(value).setCfgId(key)));
        builder.setFloor(entity.getCurrentFloor());
        builder.addAllTips(getTips(HomeLandRoundShopCfg.class, entity.getTipSet()));
        builder.setLastFloorChange(entity.getLastFloorChange());
        HomeLandRoundActivityFloorCfg cfg = HomeLandRoundActivityFloorCfg.getConfigList(entity.getCurrentFloor());
        int pity = Math.max(cfg.getGuarantee() - entity.getPityCount(entity.getCurrentFloor()), 0);
        builder.setFloorPity(pity);
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.HOME_ROUND_ACT_INFO_S, builder));
    }

    public HomeLandRoundActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public void onOpen() {
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for (String playerId : onlinePlayerIds) {
            callBack(playerId, GameConst.MsgId.HOME_LAND_ROUND_INIT, () -> {
                Optional<HomeLandRoundEntity> optional = this.getPlayerDataEntity(playerId);
                if (!optional.isPresent()) {
                    return;
                }
                this.initAcivityData(playerId);
                syncActivityDataInfo(playerId);
            });
        }
    }

    private void initAcivityData(String playerId) {
        int termId = this.getActivityTermId();
        if (termId <= 0) {
            return;
        }
        if (!this.isOpening(playerId)) {
            return;
        }
        Optional<HomeLandRoundEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        HomeLandRoundEntity entity = opEntity.get();
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        entity.recordLoginDay();
        initAchieveData(entity);
        //抛每天登录事件
        ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, entity.getLoginDaysCount(), this.providerActivityId()), true);
    }

    /**
     * 成就数据
     *
     * @param entity
     */
    public void initAchieveData(HomeLandRoundEntity entity) {
        List<AchieveItem> itemList = new CopyOnWriteArrayList<>();
        ConfigIterator<HomeLandRoundAchieveCfg> itr = HawkConfigManager.getInstance().getConfigIterator(HomeLandRoundAchieveCfg.class);
        for (HomeLandRoundAchieveCfg cfg : itr) {
            AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
            itemList.add(item);
        }
        entity.setItemList(itemList);
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), itemList), true);
    }

    /**
     * 成就数据
     *
     * @param entity
     */
    public void updateAchieveData(HomeLandRoundEntity entity) {
        Map<Integer, AchieveItem> amap = new HashMap<>();
        List<AchieveItem> list = entity.getItemList();
        for (AchieveItem item : list) {
            HomeLandRoundAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandRoundAchieveCfg.class, item.getAchieveId());
            //配置不在
            if (Objects.isNull(cfg)) {
                continue;
            }
            if (cfg.getRefreshType() != 1) {
                amap.put(item.getAchieveId(), item);
            }
        }
        ConfigIterator<HomeLandRoundAchieveCfg> itr = HawkConfigManager.getInstance().getConfigIterator(HomeLandRoundAchieveCfg.class);
        for (HomeLandRoundAchieveCfg cfg : itr) {
            if (cfg.getRefreshType() == 1) {
                AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
                amap.put(item.getAchieveId(), item);
            }
        }
        List<AchieveItem> itemList = new CopyOnWriteArrayList<>(amap.values());
        entity.setItemList(itemList);
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), itemList), true);
        //抛每天登录事件
        ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(entity.getPlayerId(), entity.getLoginDaysCount(), this.providerActivityId()), true);

    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<HomeLandRoundEntity> queryList = HawkDBManager.getInstance()
                .query("from HomeLandRoundEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && !queryList.isEmpty()) {
            return queryList.get(0);
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        return new HomeLandRoundEntity(playerId, termId);
    }

    public Result<?> onTakeFloorReward(String playerId) {
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        Optional<HomeLandRoundEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        HomeLandRoundEntity entity = opEntity.get();
        int currentFloor = entity.getCurrentFloor();
        HomeLandRoundActivityFloorCfg floorCfg = HomeLandRoundActivityFloorCfg.getConfigList(currentFloor);
        if (floorCfg == null) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        if (currentFloor == 0) {
            return Result.fail(Status.HomeRoundError.HOME_ROUND_TAKE_ZERO_LIMIT_VALUE);
        }
        entity.setCurrentFloor(0);
        //发奖励
        this.getDataGeter().takeReward(playerId, floorCfg.getFloorRewardInfo(),
                1, Action.HOME_ROUND_FLOOR_AWARD, true);
        syncActivityDataInfo(playerId);
        return Result.success();
    }

    public Result<?> onBuy(String playerId, int count) {
        //活动未开
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        Optional<HomeLandRoundEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        HomeLandRoundActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandRoundActivityKVCfg.class);
        if (cfg == null) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        if (count <= 0 || count > cfg.getCoinBuyLimit()) {
            return Result.fail(Status.HomeRoundError.HOME_ROUND_BUY_LIMIT_VALUE);
        }
        boolean flag = this.getDataGeter().cost(playerId, cfg.getCoinPriceInfo(), count,
                Action.HOME_ROUND_BUY_COST, false);
        if (!flag) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        //发奖励
        this.getDataGeter().takeReward(playerId, cfg.getCoinItemInfo(),
                count, Action.HOME_ROUND_BUY_AWARD, true);
        return Result.success();
    }

    //兑换
    public Result<?> onShopExchange(String playerId, int cfgId, int count) {
        //活动未开
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        Optional<HomeLandRoundEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        HomeLandRoundEntity entity = opEntity.get();
        HomeLandRoundShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandRoundShopCfg.class, cfgId);
        if (cfg == null) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        if (count <= 0 || count > cfg.getTimes()) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        // 判断道具足够否
        int eCount = entity.getExchangeCount(cfgId);
        if (eCount + count > cfg.getTimes()) {
            return Result.fail(Status.HomeRoundError.HOME_ROUND_SHOP_EXCHANGE_LIMIT_VALUE);
        }
        boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemInfo(), count,
                Action.HOME_ROUND_EXCHANGE_COST, false);
        if (!flag) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        //增加兑换次数
        entity.addExchangeCount(cfgId, count);
        //发奖励
        this.getDataGeter().takeReward(playerId, cfg.getGainItemInfo(),
                count, Action.HOME_ROUND_EXCHANGE_AWARD, true);
        //同步
        this.syncActivityDataInfo(playerId);
        HawkLog.logPrintln("HomeRoundActivity,itemExchange,sucess,playerId: "
                + "{},exchangeType:{},ecount:{}", playerId, cfgId, eCount);
        return Result.success();
    }

    public Result<?> onDraw(String playerId, int drawType) {
        //活动未开
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        Optional<HomeLandRoundEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        HomeLandRoundEntity entity = opEntity.get();
        HomeLandRoundDrawType roundDrawType = HomeLandRoundDrawType.getValueOf(drawType);
        HomeLandRoundActivityKVCfg activityKVCfg = HawkConfigManager.getInstance().getKVInstance(HomeLandRoundActivityKVCfg.class);
        int currentFloor = entity.getCurrentFloor();
        if (entity.getDrawTimes() > activityKVCfg.getTermLimit()) {
            return Result.fail(Status.HomeRoundError.HOME_ROUND_DAILY_DRAW_LIMIT_VALUE);
        }
        // --- 前置条件检查 ---
        if (roundDrawType == HomeLandRoundDrawType.NORMAL && currentFloor == 0) {
            return Result.fail(Status.HomeRoundError.HOME_ROUND_DRAW_ZERO_LIMIT_VALUE);
        }
        if (roundDrawType == HomeLandRoundDrawType.PROTECTED) {
            HomeLandRoundActivityFloorCfg floorCfg = HomeLandRoundActivityFloorCfg.getConfigList(currentFloor);
            ImmutableList<Reward.RewardItem.Builder> costItems = ImmutableList.copyOf(floorCfg.getCostInfo());
            boolean flag = this.getDataGeter().cost(entity.getPlayerId(), costItems, 1,
                    Action.HOME_ROUND_DRAW_COST, false);
            if (!flag) {
                return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
            }
            //抽奖事件
            for (Reward.RewardItem.Builder costItem : costItems) {
                ActivityManager.getInstance().postEvent(new HomeRoundItemCostEvent(playerId, costItem.getItemId(), (int) costItem.getItemCount()));
            }
        }
        int maxFloor = HomeLandRoundActivityFloorCfg.getMaxFloor();
        int floorChange = draw(entity, roundDrawType);
        ImmutableList<Reward.RewardItem.Builder> rewards = null;
        int drawFloor;
        // --- 处理结果 ---
        if (floorChange > 0) { // 升层
            if (roundDrawType == HomeLandRoundDrawType.PROTECTED) entity.resetPityCount(currentFloor);
            int newFloor = currentFloor + floorChange;
            if (newFloor >= maxFloor) {
                //升层！恭喜到达最高层,达到最高层后自动领取奖励并自动重置层数至0层
                entity.setCurrentFloor(0);
                entity.resetAllPityCounts();
                HomeLandRoundActivityFloorCfg floorCfg = HomeLandRoundActivityFloorCfg.getConfigList(maxFloor);
                if (floorCfg != null) {
                    rewards = ImmutableList.copyOf(floorCfg.getFloorRewardInfo());
                    this.getDataGeter().takeReward(playerId, rewards, 1, Action.HOME_ROUND_DRAW_AWARD, false);
                }
                drawFloor = maxFloor;
            } else {
                //升层(+%d)成功
                entity.setCurrentFloor(newFloor);
                drawFloor = newFloor;
            }
        } else if (floorChange < 0) { // 降层
            if (roundDrawType == HomeLandRoundDrawType.PROTECTED) {
                //降层(%d)，但保护成功！
                entity.incrementPityCount(currentFloor);
                drawFloor = currentFloor;
            } else {
                //降层(%d)！
                int newFloor = Math.max(0, currentFloor + floorChange);
                entity.setCurrentFloor(0); // 强制领取后重置到0
                entity.resetAllPityCounts();
                HomeLandRoundActivityFloorCfg floorCfg = HomeLandRoundActivityFloorCfg.getConfigList(newFloor);
                if (floorCfg != null) {
                    rewards = ImmutableList.copyOf(floorCfg.getFloorRewardInfo());
                    this.getDataGeter().takeReward(playerId, rewards, 1, Action.HOME_ROUND_DRAW_AWARD, false);
                }
                drawFloor = newFloor;
            }
        } else {
            if (roundDrawType == HomeLandRoundDrawType.PROTECTED) {
                entity.incrementPityCount(currentFloor);
            }
            drawFloor = currentFloor;
        }
        entity.setLastFloorChange(floorChange);
        entity.setDrawTimes(entity.getDrawTimes() + 1);
        entity.notifyUpdate();
        ActivityHomeLandRound.HomeLandRoundDrawRsp.Builder builder = ActivityHomeLandRound.HomeLandRoundDrawRsp.newBuilder();
        builder.setDrawFloor(drawFloor);
        builder.setFloorChange(floorChange);
        builder.setDrawType(drawType);
        if (rewards != null && !rewards.isEmpty()) {
            rewards.forEach(v -> builder.addReward(v.build()));
        }
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.HOME_ROUND_DRAW_INFO_S, builder));
        syncActivityDataInfo(playerId);
        HawkLog.logPrintln("HomeRoundActivity,draw,sucess,playerId: "
                        + "{},drawType:{},old:{},new:{},floorChange:{},pityCount:{},dailyTimes:{},drawFloor:{}", playerId, drawType, currentFloor, entity.getCurrentFloor(),
                floorChange, entity.getPityCount(currentFloor), entity.getDrawTimes(), drawFloor);
        return Result.success();
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
        Optional<HomeLandRoundEntity> optional = getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return Optional.empty();
        }
        HomeLandRoundEntity entity = optional.get();
        if (entity.getItemList().isEmpty()) {
            return Optional.empty();
        }
        AchieveItems items = new AchieveItems(entity.getItemList(), entity, true, getActivityId(), entity.getTermId());
        return Optional.of(items);
    }

    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        return HawkConfigManager.getInstance().getConfigByKey(HomeLandRoundAchieveCfg.class, achieveId);
    }

    @Override
    public Action takeRewardAction() {
        return Action.HOME_ROUND_ACHIEVE_REWARD;
    }

    @Override
    public int providerActivityId() {
        return this.getActivityType().intValue();
    }

    public enum HomeLandRoundDrawType {
        NORMAL(1),  // 组合图案
        PROTECTED(2),   // 大奖图案
        ;
        final int type;

        HomeLandRoundDrawType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public static HomeLandRoundDrawType getValueOf(int type) {
            for (HomeLandRoundDrawType drawType : values()) {
                if (drawType.getType() == type) {
                    return drawType;
                }
            }
            return HomeLandRoundDrawType.NORMAL;
        }
    }

    @Override
    public boolean isActivityClose(String playerId) {
        if (HawkOSOperator.isEmptyString(playerId)) {
            return true;
        }
        return !isPlayerOpen(playerId);
    }

    @Subscribe
    public void onEvent(BuildingLevelUpEvent event) {
        // 大本升级,检测活动是否关闭
        if (event.getBuildType() == Const.BuildingType.CONSTRUCTION_FACTORY_VALUE) {
            this.syncActivityStateInfo(event.getPlayerId());
            this.syncActivityDataInfo(event.getPlayerId());
        }
    }

    /**
     * 判断建筑工厂等级
     *
     * @return
     */
    public boolean isPlayerOpen(String playerId) {
        int cityLevel = this.getDataGeter().getConstructionFactoryLevel(playerId);
        HomeLandRoundActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HomeLandRoundActivityKVCfg.class);
        return cityLevel >= cfg.getBaseLimit();
    }
}
