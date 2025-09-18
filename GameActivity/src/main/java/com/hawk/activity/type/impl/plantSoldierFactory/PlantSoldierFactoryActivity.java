package com.hawk.activity.type.impl.plantSoldierFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.event.impl.PlantSoldierFactoryDrawEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.plantSoldierFactory.cfg.*;
import com.hawk.activity.type.impl.plantSoldierFactory.entity.PlantSoldierFactoryActivityEntity;
import com.hawk.activity.type.impl.plantSoldierFactory.entity.PlantSoldierFactoryItemObj;
import com.hawk.game.protocol.*;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PlantSoldierFactoryActivity extends ActivityBase implements AchieveProvider {
    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger("Server");
    public static final int AWARD_COUNT = 25;
    public static final int BIG_AWARD_COUNT = 12;
    public PlantSoldierFactoryActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.PLANT_SOLDIER_FACTORY;
    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        PlantSoldierFactoryActivity activity = new PlantSoldierFactoryActivity(config.getActivityId(), activityEntity);
        AchieveContext.registeProvider(activity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        //根据玩家id和活动期数去数据库里取数据
        List<PlantSoldierFactoryActivityEntity> queryList = HawkDBManager.getInstance()
                .query("from PlantSoldierFactoryActivityEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && !queryList.isEmpty()) {
            return queryList.get(0);
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        PlantSoldierFactoryActivityEntity entity = new PlantSoldierFactoryActivityEntity(playerId, termId);
        return entity;
    }

    @Override
    public boolean isProviderActive(String playerId) {
        //活动开就激活
        return isOpening(playerId);
    }

    @Override
    public boolean isProviderNeedSync(String playerId) {
        //活动展示就同步
        return isShow(playerId);
    }

    @Override
    public Optional<AchieveItems> getAchieveItems(String playerId) {
        Optional<PlantSoldierFactoryActivityEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        PlantSoldierFactoryActivityEntity entity = opEntity.get();
        //如果成就数据为空就初始化成就数据
        if (entity.getItemList().isEmpty()) {
            //初始化成就数据
            this.initAchieve(playerId);
        }
        //返回当前成就数据
        AchieveItems items = new AchieveItems(entity.getItemList(), entity);
        return Optional.of(items);
    }

    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        PlantSoldierFactoryAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierFactoryAchieveCfg.class, achieveId);
        return cfg;
    }

    @Override
    public Action takeRewardAction() {
        return Action.PLANT_SOLDIER_FACTORY_ACHIEVE_REWARD;
    }

    @Override
    public int providerActivityId() {
        return this.getActivityType().intValue();
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        Optional<PlantSoldierFactoryActivityEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        PlantSoldierFactoryActivityEntity entity = opEntity.get();
        if(entity.getAwardMap().size() < AWARD_COUNT || entity.getBigAwardMap().size() < BIG_AWARD_COUNT){
            initDrawItem(playerId);
        }
        PlantSoldierFactoryKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PlantSoldierFactoryKVCfg.class);
        Activity.PlantSoldierFactoryInfoResp.Builder resp = Activity.PlantSoldierFactoryInfoResp.newBuilder();
        for(int i = 1; i <= AWARD_COUNT; i++){
            PlantSoldierFactoryItemObj itemObj = entity.getAwardMap().get(i);
            resp.addItems(itemObj.toPB());
        }
        for(int i = 1; i <= BIG_AWARD_COUNT; i++){
            PlantSoldierFactoryItemObj itemObj = entity.getBigAwardMap().get(i);
            resp.addBigItems(itemObj.toPB());
        }
        for (Map.Entry<Integer, Integer> entry : entity.getShopItemMap().entrySet()) {
            Activity.PlantSoldierFactoryShopInfo.Builder builder = Activity.PlantSoldierFactoryShopInfo.newBuilder();
            builder.setShopId(entry.getKey());
            builder.setCount(entry.getValue());
            resp.addShopInfos(builder);
        }
        resp.setIsCanDraw(getBigAwardUnlockCount(entity) < kvCfg.getGrandTimesRefresh());
        resp.setDrawCount(entity.getDrawCount());
        resp.setDrawTotalCount(entity.getDrawTotalCount());
        pushToPlayer(playerId, HP.code2.PLANT_SOLDIER_FACTORY_INFO_RESP_VALUE, resp);
    }

    @Override
    public void onOpen() {
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for(String playerId : onlinePlayerIds){
            callBack(playerId, GameConst.MsgId.ON_SUPPLY_CRATE_INIT, () -> {
                resetPlayerData(playerId);
            });
        }
    }

    /**
     * 初始化成就数据
     * @param playerId 玩家id
     */
    private void initAchieve(String playerId) {
        Optional<PlantSoldierFactoryActivityEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        PlantSoldierFactoryActivityEntity entity = opEntity.get();
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        ConfigIterator<PlantSoldierFactoryAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(PlantSoldierFactoryAchieveCfg.class);
        List<AchieveItem> list = new ArrayList<>();
        while (iterator.hasNext()) {
            PlantSoldierFactoryAchieveCfg cfg = iterator.next();
            AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
            list.add(item);
        }
        entity.setItemList(list);
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()));
    }

    private void initDrawItem(String playerId) {
        Optional<PlantSoldierFactoryActivityEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        PlantSoldierFactoryActivityEntity entity = opEntity.get();
        if (!entity.getAwardMap().isEmpty() || !entity.getBigAwardMap().isEmpty()) {
            return;
        }
        Map<PlantSoldierFactoryPoolCfg, Integer> awardWeightMap = new HashMap<>();
        Map<Integer,Map<PlantSoldierFactoryPoolCfg, Integer>> bigAwardWeightMap = new HashMap<>();
        ConfigIterator<PlantSoldierFactoryPoolCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(PlantSoldierFactoryPoolCfg.class);
        for(PlantSoldierFactoryPoolCfg cfg : iterator){
            if(cfg.getPoolType() == 1){
                awardWeightMap.put(cfg, cfg.getWeight());
            }
            if(cfg.getPoolType() == 2){
                if(!bigAwardWeightMap.containsKey(cfg.getGroupSign())){
                    bigAwardWeightMap.put(cfg.getGroupSign(), new HashMap<>());
                }
                bigAwardWeightMap.get(cfg.getGroupSign()).put(cfg, cfg.getWeight());
            }
        }

        Map<Integer, PlantSoldierFactoryItemObj> tmp = new HashMap<>();
        Map<Integer, PlantSoldierFactoryItemObj> bigTmp = new HashMap<>();
        for(int i = 1; i <= AWARD_COUNT; i++){
            PlantSoldierFactoryPoolCfg poolCfg = randomAward(awardWeightMap);
            tmp.put(i, new PlantSoldierFactoryItemObj(i, poolCfg.getId(), Activity.PlantSoldierFactoryItemState.PSFI_CLOSE));
        }
        for(int i = 1; i <= BIG_AWARD_COUNT; i++){
            PlantSoldierFactoryPoolCfg poolCfg = randomBigAward(i, bigAwardWeightMap);
            bigTmp.put(i, new PlantSoldierFactoryItemObj(i, poolCfg.getId(), Activity.PlantSoldierFactoryItemState.PSFI_LOCK));
        }
        entity.setAwardMap(tmp);
        entity.setBigAwardMap(bigTmp);
    }

    public PlantSoldierFactoryPoolCfg randomAward(Map<PlantSoldierFactoryPoolCfg, Integer> awardWeightMap){
        PlantSoldierFactoryPoolCfg cfg = HawkRand.randomWeightObject(awardWeightMap);
        if(cfg != null){
            awardWeightMap.remove(cfg);
        }
        return cfg;
    }

    public PlantSoldierFactoryPoolCfg randomBigAward(int bigAwardPos, Map<Integer,Map<PlantSoldierFactoryPoolCfg, Integer>> bigAwardWeightMap){
        PlantSoldierFactoryGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierFactoryGroupCfg.class, bigAwardPos);
        Map<PlantSoldierFactoryPoolCfg, Integer> tmpMap = bigAwardWeightMap.get(groupCfg.getGroup());
        PlantSoldierFactoryPoolCfg cfg = HawkRand.randomWeightObject(tmpMap);
        if(cfg != null){
            tmpMap.remove(cfg);
        }
        return cfg;
    }

    public int getBigAwardUnlockCount(PlantSoldierFactoryActivityEntity entity){
        int count = 0;
        for(PlantSoldierFactoryItemObj itemObj : entity.getBigAwardMap().values()){
            if(itemObj.getPBState() != Activity.PlantSoldierFactoryItemState.PSFI_LOCK){
                count++;
            }
        }
        return count;
    }

    public void checkBigAward(PlantSoldierFactoryActivityEntity entity){
        for(PlantSoldierFactoryItemObj bigItemObj : entity.getBigAwardMap().values()){
            if(bigItemObj.getPBState() != Activity.PlantSoldierFactoryItemState.PSFI_LOCK){
                continue;
            }
            PlantSoldierFactoryGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierFactoryGroupCfg.class, bigItemObj.getPos());
            boolean isConnection = true;
            for(int pos : groupCfg.getConnectionList()){
                PlantSoldierFactoryItemObj itemObj = entity.getAwardMap().get(pos);
                if(itemObj.getPBState() != Activity.PlantSoldierFactoryItemState.PSFI_OPEN){
                    isConnection = false;
                }
            }
            if(isConnection){
                bigItemObj.setState(Activity.PlantSoldierFactoryItemState.PSFI_CAN_VALUE);
            }
        }
        entity.notifyUpdate();
    }

    public void resetPlayerData(String playerId){
        Optional<PlantSoldierFactoryActivityEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        PlantSoldierFactoryActivityEntity entity = opEntity.get();
        if (entity.getItemList().isEmpty()) {
            //初始化成就数据
            this.initAchieve(playerId);
        }

        if (entity.getAwardMap().isEmpty() && entity.getBigAwardMap().isEmpty()) {
            //初始化成就数据
            this.initDrawItem(playerId);
        }
        long now = HawkTime.getMillisecond();
        if(HawkTime.isSameDay(now, entity.getResetTime())){
            return;
        }
        //数据有变化的成就，需要推送给前端
        List<AchieveItem> needPushList = new ArrayList<>();
        //遍历成就数据
        for(AchieveItem item : entity.getItemList()){
            PlantSoldierFactoryAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierFactoryAchieveCfg.class, item.getAchieveId());
            if(cfg == null){
                continue;
            }
            if(cfg.getRefreshType() == 0){
                continue;
            }
            if(item.getValue(0) == 0 && item.getState() == Activity.AchieveState.NOT_ACHIEVE_VALUE){
                continue;
            }
            //设置数据为0
            item.setValue(0, 0);
            //设置成就状态
            item.setState(Activity.AchieveState.NOT_ACHIEVE_VALUE);
            //添加到需要推送列表里
            needPushList.add(item);
        }

        entity.setResetTime(now);
        ConfigIterator<PlantSoldierFactoryShopCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(PlantSoldierFactoryShopCfg.class);
        for (PlantSoldierFactoryShopCfg shopCfg : iterator){
            if(shopCfg.getIsRefresh() == 0){
                continue;
            }
            entity.getShopItemMap().put(shopCfg.getId(), 0);
        }
        entity.setDrawCount(0);
        entity.notifyUpdate();
        if(!needPushList.isEmpty()){
            //推送给前端
            AchievePushHelper.pushAchieveUpdate(playerId, needPushList);
        }
        postCommonLoginEvent(playerId);
        syncActivityDataInfo(playerId);
    }

    /**
     * 商店够买
     * @param playerId
     * @param entity
     * @param cfg
     * @param count
     */
    private void shopBuyItem(String playerId, PlantSoldierFactoryActivityEntity entity, PlantSoldierFactoryShopCfg cfg, int count) {
        int shopId = cfg.getId();
        int boughtCount = entity.getShopItemMap().getOrDefault(shopId, 0);
        List<Reward.RewardItem.Builder> rewardItems = RewardHelper.toRewardItemImmutableList(cfg.getGetItem());
        this.getDataGeter().takeReward(playerId, rewardItems, count, Action.PLANT_SOLDIER_FACTORY_SHOP_BUY, true, Reward.RewardOrginType.ACTIVITY_REWARD);
        entity.getShopItemMap().put(shopId, boughtCount + count);
        entity.notifyUpdate();

        HawkLog.logPrintln("PlantSoldierFactoryActivity shop buy success, playerId: {}, shopId: {}, old boughtCount: {}, count: {}", playerId, shopId, boughtCount, count);
    }

    /**
     * 直购判断
     *
     * @param playerId
     * @param goodsId
     * @return
     */
    public int buyItemCheck(String playerId, String goodsId) {
        PlantSoldierFactoryShopCfg cfg = PlantSoldierFactoryShopCfg.getConfig(goodsId);
        if (cfg == null) {
            HawkLog.errPrintln("PlantSoldierFactoryActivity shop buyItem check error, shop config match null, playerId: {}, goodsId: {}", playerId, goodsId);
            return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
        }

        if (!isOpening(playerId)) {
            return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
        }

        Optional<PlantSoldierFactoryActivityEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
        }
        PlantSoldierFactoryActivityEntity entity = opEntity.get();

        int boughtCount = entity.getShopItemMap().getOrDefault(cfg.getId(), 0);
        int newCount = boughtCount + 1;
        //限购
        if (newCount > cfg.getTimes()) {
            HawkLog.errPrintln("PlantSoldierFactoryActivity shop buyItem check error, playerId: {}, goodsId: {}, shopId: {}, oldCount: {}", playerId, goodsId, cfg.getId(), boughtCount);
            return Status.Error.PLANT_WEAPON_SHOP_LIMIT_VALUE;
        }

        return 0;
    }

    @Subscribe
    public void onContinueLoginEvent(ContinueLoginEvent event) {
        if (!isOpening(event.getPlayerId())) {
            return;
        }
        resetPlayerData(event.getPlayerId());
    }

    /***
     * 购买事件
     *
     * @param event
     */
    @Subscribe
    public void onEvent(PayGiftBuyEvent event) {
        String playerId = event.getPlayerId();
        String goodsId = event.getGiftId();
        PlantSoldierFactoryShopCfg cfg = PlantSoldierFactoryShopCfg.getConfig(goodsId);
        if (cfg == null) {
            HawkLog.errPrintln("PlantSoldierFactoryActivity payGift callback error,  shop config match null, playerId: {}, goodsId: {}", playerId, goodsId);
            return;
        }

        if (!isOpening(playerId)) {
            return;
        }

        Optional<PlantSoldierFactoryActivityEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        PlantSoldierFactoryActivityEntity entity = opEntity.get();
        shopBuyItem(playerId, entity, cfg, 1);

        HawkLog.logPrintln("PlantSoldierFactoryActivity payGift finish, playerId: {}, goodsId: {}, shopId: {}", playerId, goodsId, cfg.getId());
        syncActivityDataInfo(playerId);
        sendMailToPlayer(playerId, MailConst.MailId.PLANT_SOLDIER_FACTORY_PURCHASE, null, null, null, RewardHelper.toRewardItemImmutableList(cfg.getGetItem()), true);
    }

    public Result<Integer> info(String playerId){
        syncActivityDataInfo(playerId);
        return Result.success();
    }

    public Result<Integer> draw(String playerId){
        Optional<PlantSoldierFactoryActivityEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        PlantSoldierFactoryActivityEntity entity = opEntity.get();
        PlantSoldierFactoryKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PlantSoldierFactoryKVCfg.class);
        if(entity.getDrawCount() >= kvCfg.getDrawValueLimit()){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        if(getBigAwardUnlockCount(entity) >= kvCfg.getGrandTimesRefresh()){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //兑换消耗
        boolean flag = this.getDataGeter().cost(playerId, kvCfg.getUseItemList(), 1, Action.PLANT_SOLDIER_FACTORY_DRAW, true);
        //如果不够消耗，返回错误码
        if (!flag) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        entity.setDrawCount(entity.getDrawCount() + 1);
        entity.setDrawTotalCount(entity.getDrawTotalCount() + 1);
        int randomPos = HawkRand.randInt(AWARD_COUNT);
        for(int i = 0; i<AWARD_COUNT;i++){
            int pos = (randomPos + i) % AWARD_COUNT + 1;
            PlantSoldierFactoryItemObj itemObj = entity.getAwardMap().get(pos);
            if(itemObj.getPBState() == Activity.PlantSoldierFactoryItemState.PSFI_CLOSE){
                itemObj.setState(Activity.PlantSoldierFactoryItemState.PSFI_OPEN_VALUE);
                PlantSoldierFactoryPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierFactoryPoolCfg.class, itemObj.getCfgId());
                this.getDataGeter().takeReward(playerId, poolCfg.getRewardList(), 1 ,Action.PLANT_SOLDIER_FACTORY_DRAW,true, Reward.RewardOrginType.PLANT_SOLDIER_FACTORY_DRAW);
                break;
            }
        }
        checkBigAward(entity);
        entity.notifyUpdate();
        syncActivityDataInfo(playerId);
        ActivityManager.getInstance().postEvent(new PlantSoldierFactoryDrawEvent(playerId, entity.getDrawTotalCount()));
        return Result.success();
    }

    public Result<Integer> award(String playerId, int bigPos){
        Optional<PlantSoldierFactoryActivityEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        PlantSoldierFactoryActivityEntity entity = opEntity.get();
        PlantSoldierFactoryItemObj itemObj = entity.getBigAwardMap().get(bigPos);
        if(itemObj.getPBState() != Activity.PlantSoldierFactoryItemState.PSFI_CAN){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        itemObj.setState(Activity.PlantSoldierFactoryItemState.PSFI_GET_VALUE);
        entity.notifyUpdate();
        PlantSoldierFactoryPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierFactoryPoolCfg.class, itemObj.getCfgId());
        this.getDataGeter().takeReward(playerId, poolCfg.getRewardList(), 1 ,Action.PLANT_SOLDIER_FACTORY_AWARD,true);
        syncActivityDataInfo(playerId);
        return Result.success();
    }

    public Result<Integer> refresh(String playerId){
        Optional<PlantSoldierFactoryActivityEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        PlantSoldierFactoryActivityEntity entity = opEntity.get();
        PlantSoldierFactoryKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PlantSoldierFactoryKVCfg.class);
        if(getBigAwardUnlockCount(entity) < kvCfg.getGrandTimesRefresh()){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        List<Reward.RewardItem.Builder> rewardList = new ArrayList<>();
        for(PlantSoldierFactoryItemObj itemObj : entity.getBigAwardMap().values()){
            if(itemObj.getPBState() == Activity.PlantSoldierFactoryItemState.PSFI_CAN){
                itemObj.setState(Activity.PlantSoldierFactoryItemState.PSFI_GET_VALUE);
                PlantSoldierFactoryPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierFactoryPoolCfg.class, itemObj.getCfgId());
                rewardList.addAll(poolCfg.getRewardList());
            }
        }
        this.getDataGeter().takeReward(playerId, rewardList, 1 ,Action.PLANT_SOLDIER_FACTORY_AWARD,true);
        Map<PlantSoldierFactoryPoolCfg, Integer> awardWeightMap = new HashMap<>();
        Map<Integer,Map<PlantSoldierFactoryPoolCfg, Integer>> bigAwardWeightMap = new HashMap<>();
        ConfigIterator<PlantSoldierFactoryPoolCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(PlantSoldierFactoryPoolCfg.class);
        for(PlantSoldierFactoryPoolCfg cfg : iterator){
            if(cfg.getPoolType() == 1){
                awardWeightMap.put(cfg, cfg.getWeight());
            }
            if(cfg.getPoolType() == 2){
                if(!bigAwardWeightMap.containsKey(cfg.getGroupSign())){
                    bigAwardWeightMap.put(cfg.getGroupSign(), new HashMap<>());
                }
                bigAwardWeightMap.get(cfg.getGroupSign()).put(cfg, cfg.getWeight());
            }
        }
        Map<Integer, PlantSoldierFactoryItemObj> tmp = new HashMap<>();
        Map<Integer, PlantSoldierFactoryItemObj> bigTmp = new HashMap<>();
        for(int i = 1; i <= AWARD_COUNT; i++){
            PlantSoldierFactoryPoolCfg poolCfg = randomAward(awardWeightMap);
            tmp.put(i, new PlantSoldierFactoryItemObj(i, poolCfg.getId(), Activity.PlantSoldierFactoryItemState.PSFI_CLOSE));
        }
        for(int i = 1; i <= BIG_AWARD_COUNT; i++){
            PlantSoldierFactoryPoolCfg poolCfg = randomBigAward(i, bigAwardWeightMap);
            bigTmp.put(i, new PlantSoldierFactoryItemObj(i, poolCfg.getId(), Activity.PlantSoldierFactoryItemState.PSFI_LOCK));
        }
        entity.setAwardMap(tmp);
        entity.setBigAwardMap(bigTmp);
        syncActivityDataInfo(playerId);
        return Result.success();
    }

    public Result<Integer> shopBuy(String playerId, int shopId, int count){
        if (count <= 0) {
            HawkLog.errPrintln("PlantSoldierFactoryActivity shop buy error, playerId: {}, count: {}", playerId, count);
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }

        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }

        PlantSoldierFactoryShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantSoldierFactoryShopCfg.class, shopId);
        if (cfg == null) {
            HawkLog.errPrintln("PlantSoldierFactoryActivity shop buy error of config, playerId: {}, shopId: {}", playerId, shopId);
            return Result.fail(Status.SysError.CONFIG_ERROR_VALUE);
        }
        //需要通过直购付费的形式购买
        if (cfg.getShopItemType() == 1) {
            //todo
            HawkLog.errPrintln("PlantSoldierFactoryActivity shop buy error, playerId: {}, shopId: {}, shopItemType: {}", playerId, shopId, cfg.getShopItemType());
            return Result.fail(Status.Error.PLANT_WEAPON_SHOP_BUY_ERROR_VALUE);
        }

        Optional<PlantSoldierFactoryActivityEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        PlantSoldierFactoryActivityEntity entity = opEntity.get();

        int boughtCount = entity.getShopItemMap().getOrDefault(shopId, 0);
        int newCount = boughtCount + count;
        //限购
        if (newCount > cfg.getTimes()) {
            HawkLog.errPrintln("PlantSoldierFactoryActivity shop buy error, playerId: {}, shopId: {}, oldCount: {}, newCount: {}", playerId, shopId, boughtCount, newCount);
            return Result.fail(Status.Error.PLANT_WEAPON_SHOP_LIMIT_VALUE);
        }

        List<Reward.RewardItem.Builder> consumeItems = RewardHelper.toRewardItemImmutableList(cfg.getPayItem());
        // 判断道具足够否
        boolean flag = this.getDataGeter().cost(playerId, consumeItems, count, Action.PLANT_SOLDIER_FACTORY_SHOP_BUY, false);
        if (cfg.getShopItemType() !=0 && !flag) {
            HawkLog.errPrintln("PlantSoldierFactoryActivity shop buy error of consume, playerId: {}, shopId: {}", playerId, shopId);
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }

        shopBuyItem(playerId, entity, cfg, count);
        syncActivityDataInfo(playerId);
        if(cfg.getShopItemType() != 0){
            sendMailToPlayer(playerId, MailConst.MailId.PLANT_SOLDIER_FACTORY_PURCHASE, null, null, null, RewardHelper.toRewardItemImmutableList(cfg.getGetItem()), true);
        }
        return Result.success();
    }
}
