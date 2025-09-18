package com.hawk.activity.type.impl.backToNewFly;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.BackToNewFlyEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.backToNewFly.cfg.BackToNewFlyAchieveCfg;
import com.hawk.activity.type.impl.backToNewFly.cfg.BackToNewFlyKvCfg;
import com.hawk.activity.type.impl.backToNewFly.cfg.BackToNewFlyShopCfg;
import com.hawk.activity.type.impl.backToNewFly.data.BackToNewFlyData;
import com.hawk.activity.type.impl.backToNewFly.entity.BackToNewFlyEntity;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BackToNewFlyActivity extends ActivityBase implements AchieveProvider, IExchangeTip<BackToNewFlyShopCfg> {
    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger("Server");

    public BackToNewFlyActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.BACK_TO_NEW_FLY;
    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        BackToNewFlyActivity activity = new BackToNewFlyActivity(config.getActivityId(), activityEntity);
        //注册活动
        AchieveContext.registeProvider(activity);
        //返回活动实例
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        //根据条件从数据库中检索
        List<BackToNewFlyEntity> queryList = HawkDBManager.getInstance()
                .query("from BackToNewFlyEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        //如果有数据的话，返回第一个数据
        if (queryList != null && queryList.size() > 0) {
            BackToNewFlyEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        BackToNewFlyEntity entity = new BackToNewFlyEntity(playerId, termId);
        return entity;
    }

    @Override
    public void onPlayerLogin(String playerId) {
        Optional<BackToNewFlyEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        BackToNewFlyEntity entity = opEntity.get();
        String openid = getDataGeter().getOpenId(playerId);
        BackToNewFlyData data = BackToNewFlyData.load(openid);
        if(data == null){
            return;
        }
        if(data.getBackCount() <= entity.getBackCount()){
            return;
        }
        BackToNewFlyKvCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BackToNewFlyKvCfg.class);
        long createTime = getDataGeter().getPlayerCreateTime(playerId);
        long startTime = HawkTime.getAM0Date(new Date(createTime)).getTime();
        long continueTime = kvCfg.getDuration() - 1000;
        long overTime = data.getStartTime() + continueTime;
        if(createTime >= data.getUpdateTime() && createTime <= data.getOverTime()){
            ActivityGlobalRedis.getInstance().getRedisSession().setNx("BackToNewFly:"+data.getBackCount()+":"+openid, playerId);
            String id = ActivityGlobalRedis.getInstance().get("BackToNewFly:"+data.getBackCount()+":"+openid);
            if(playerId.equals(id)){
                entity.setStartTime(startTime);
                entity.setOverTime(overTime);
                entity.setBackCount(data.getBackCount());
                entity.setBaseLevel(data.getBaseLevel());
                entity.notifyUpdate();
                ActivityManager.getInstance().postEvent(new BackToNewFlyEvent(playerId));
                Map<String, Object> param = new HashMap<>();
                param.put("openId", openid);
                param.put("baseLevel", data.getBaseLevel());
                getDataGeter().logActivityCommon(playerId, LogInfoType.back_to_new_fly, param);
            }
        }
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        Optional<BackToNewFlyEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        BackToNewFlyEntity entity = opEntity.get();
        Activity.BackToNewFlySync.Builder builder = Activity.BackToNewFlySync.newBuilder();
        getShopItem(builder, entity);
        getTips(builder, entity);
        builder.setBaseLevel(entity.getBaseLevel());
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.BACK_TO_NEW_FLY_SYNC, builder));
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
    public boolean isProviderNeedUpdate(String playerId, int achieveId) {
        BackToNewFlyAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BackToNewFlyAchieveCfg.class, achieveId);
        if(cfg == null){
            return false;
        }
        Optional<BackToNewFlyEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return false;
        }
        //获得玩家活动数据实体
        BackToNewFlyEntity entity = opEntity.get();
        if(entity.getBaseLevel() < cfg.getBaseCondition() || entity.getBaseLevel() > cfg.getBaseConditionUp()){
            return false;
        }
        return true;
    }

    @Override
    public Optional<AchieveItems> getAchieveItems(String playerId) {
        //获得玩家活动数据
        Optional<BackToNewFlyEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        //获得玩家活动数据实体
        BackToNewFlyEntity entity = opEntity.get();
        //如果成就数据为空就初始化成就数据
        if (entity.getItemList().isEmpty()) {
            //初始化成就数据
            this.initAchieve(playerId);
        }
        //返回当前成就数据
        AchieveItems items = new AchieveItems(entity.getItemList(), entity);
        return Optional.of(items);
    }

    private void initAchieve(String playerId) {
        Optional<BackToNewFlyEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        BackToNewFlyEntity entity = opEntity.get();
        //如果成就数据为空就初始化成就数据
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        ConfigIterator<BackToNewFlyAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(BackToNewFlyAchieveCfg.class);
        List<AchieveItem> list = new ArrayList<>();
        for(BackToNewFlyAchieveCfg cfg : iterator){
            AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
            list.add(item);
        }
        entity.setItemList(list);
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()));
    }

    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        BackToNewFlyAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BackToNewFlyAchieveCfg.class, achieveId);
        return cfg;
    }

    @Override
    public Action takeRewardAction() {
        return Action.BACK_TO_NEW_FLY_ACHIEVE_REWARD;
    }

    public void getTips(Activity.BackToNewFlySync.Builder builder, BackToNewFlyEntity entity){
        builder.addAllTips(getTips(BackToNewFlyShopCfg.class, entity.getTipSet()));
    }

    public void getShopItem(Activity.BackToNewFlySync.Builder builder, BackToNewFlyEntity entity){
        for(Map.Entry<Integer, Integer> entry : entity.getBuyNumMap().entrySet()){
            Activity.BackToNewFlyShopItem.Builder shopItem = Activity.BackToNewFlyShopItem.newBuilder();
            //配置id
            shopItem.setGoodsId(entry.getKey());
            //已兑换数量
            shopItem.setExhangeTimes(entry.getValue());
            builder.addGoods(shopItem);
        }
    }


    /**
     * 兑换物品
     * @param playerId 玩家id
     * @param exchangeId 兑换id
     * @param num 兑换数量
     * @return 兑换结果
     */
    public Result<Integer> exchange(String playerId, int exchangeId, int num) {
        //判断活动是否开启，如果没开返回错误码
        if (!isShow(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //获取兑换配置，如果配置为空返回错误码
        BackToNewFlyShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BackToNewFlyShopCfg.class, exchangeId);
        if (cfg == null) {
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //如果数据为空，返回错误码
        Optional<BackToNewFlyEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        BackToNewFlyEntity entity = opEntity.get();
        //当前已经兑换数量
        int buyNum = entity.getBuyNumMap().getOrDefault(exchangeId, 0);
        //兑换后的数量
        int newNum = buyNum + num;
        //判断是否超过可兑换数量最大值，如果超过返回错误码
        if (newNum > cfg.getTimes()) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        //兑换消耗
        boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), num, Action.BACK_TO_NEW_FLY_SHOP_COST, true);
        //如果不够消耗，返回错误码
        if (!flag) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        //设置新的兑奖数量
        entity.getBuyNumMap().put(exchangeId, newNum);
        entity.notifyUpdate();
        //发奖
        this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.BACK_TO_NEW_FLY_SHOP_GET, true);
        //记录日志
        logger.info("BackToNewFlyActivity exchange playerId:{}, exchangeId:{}, num:{}", playerId, exchangeId, num);
        syncActivityDataInfo(playerId);
        //返回兑换状态
        return Result.success(newNum);
    }

    public Result<Integer> fly(String playerId){
//        if(!getDataGeter().dealWithBackToNewFlyBuild(playerId)){
//            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
//        }
        return Result.success();
    }

    @Subscribe
    public void onFlyEvent(BackToNewFlyEvent event) {
        String playerId = event.getPlayerId();
        //如果活动没开返回
        if (!isOpening(playerId)) {
            return;
        }
        String spKey = "BackToNewFlyReal:" + playerId;
        String val = ActivityGlobalRedis.getInstance().getRedisSession().getString(spKey);
        if (StringUtils.isNotEmpty(val)){
            return;
        }
        ActivityGlobalRedis.getInstance().getRedisSession().setString(spKey, String.valueOf(HawkTime.getMillisecond()));
        BackToNewFlyKvCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BackToNewFlyKvCfg.class);
        sendMailToPlayer(playerId, MailConst.MailId.valueOf(kvCfg.getRewardMail()), null, null, null, RewardHelper.toRewardItemImmutableList(kvCfg.getRewardItemGetNew()), false);
//        fly(event.getPlayerId());
    }

    @Subscribe
    public void onContinueLoginEvent(ContinueLoginEvent event) {
        String playerId = event.getPlayerId();
        //如果活动没开返回
        if (!isOpening(playerId)) {
            return;
        }
        postCommonLoginEvent(playerId);
    }

    @Override
    public boolean isActivityClose(String playerId) {
        Optional<BackToNewFlyEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return true;
        }
        //获得玩家活动数据实体
        BackToNewFlyEntity entity = opEntity.get();
        long now = HawkTime.getMillisecond();
        if(now < entity.getStartTime() || now > entity.getOverTime()){
            return true;
        }
        return false;
    }
    
    @Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
}
