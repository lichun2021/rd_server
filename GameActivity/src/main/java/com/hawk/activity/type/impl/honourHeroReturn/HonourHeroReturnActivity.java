package com.hawk.activity.type.impl.honourHeroReturn;

import java.util.*;
import java.util.Map.Entry;

import com.hawk.activity.event.impl.*;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.honourHeroReturn.cfg.HonourHeroReturnAchieveCfg;
import com.hawk.activity.type.impl.honourHeroReturn.cfg.HonourHeroReturnExchangeCfg;
import com.hawk.activity.type.impl.honourHeroReturn.cfg.HonourHeroReturnKVCfg;
import com.hawk.activity.type.impl.honourHeroReturn.cfg.HonourHeroReturnRewardCfg;
import com.hawk.activity.type.impl.honourHeroReturn.entity.HonourHeroReturnEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.game.protocol.Activity.PBHonourHeroReturnExchange;
import com.hawk.game.protocol.Activity.PBHonourHeroReturnResp;
import com.hawk.game.protocol.Activity.PBHonourHeroReturnTip;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 荣耀英雄回归
 *
 * @author richard
 */
public class HonourHeroReturnActivity extends ActivityBase implements AchieveProvider {

    private static final Logger logger = LoggerFactory.getLogger("Server");

    public HonourHeroReturnActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.HONOUR_HERO_RETURN_ACTIVITY;
    }

    @Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        HonourHeroReturnActivity activity = new HonourHeroReturnActivity(config.getActivityId(), activityEntity);
        AchieveContext.registeProvider(activity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<HonourHeroReturnEntity> queryList = HawkDBManager.getInstance().query("from HonourHeroReturnEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
            HonourHeroReturnEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        HonourHeroReturnEntity entity = new HonourHeroReturnEntity(playerId, termId);
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
        Optional<HonourHeroReturnEntity> optional = getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return Optional.empty();
        }
        HonourHeroReturnEntity entity = optional.get();
        if (entity.getItemList().isEmpty()) {
            this.initAchieve(playerId, entity);
        }
        AchieveItems items = new AchieveItems(entity.getItemList(), entity);
        return Optional.of(items);
    }

    /**
     * 初始化成就
     */
    private void initAchieve(String playerId, HonourHeroReturnEntity entity) {
        //成就数据不为空，不需要继续处理
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        //取成就配置
        List<AchieveItem> itemList = initAchieveItems(entity);
        //初始化成就
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemList), true);
    }

    private List<AchieveItem> initAchieveItems(HonourHeroReturnEntity entity) {
        ConfigIterator<HonourHeroReturnAchieveCfg> configIterator =
                HawkConfigManager.getInstance().getConfigIterator(HonourHeroReturnAchieveCfg.class);
        List<AchieveItem> itemList = new ArrayList<>();
        //遍历成就配置，向数据库对象添加成就项
        while (configIterator.hasNext()) {
            HonourHeroReturnAchieveCfg cfg = configIterator.next();
            AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
            itemList.add(item);
        }
        entity.setItemList(itemList);
        return itemList;
    }

    /**
     * 活动开启
     */
    @Override
    public void onOpen() {
        //活动开启时初始化所有在线玩家荣耀英雄回归数据
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for (String playerId : onlinePlayerIds) {
            initOnlinePlayer(playerId);
        }
    }

    /**
     * 活动开启时初始化在线玩家荣耀英雄回归数据
     *
     * @param playerId
     */
    private void initOnlinePlayer(String playerId) {
        //这里异步处理
        callBack(playerId, MsgId.HONOUR_HERO_RETURN_INIT, () -> {
            Optional<HonourHeroReturnEntity> optional = this.getPlayerDataEntity(playerId);
            if (!optional.isPresent()) {
                return;
            }
            HonourHeroReturnEntity entity = optional.get();
            entity.recordLoginDay();
            //初始化成就
            this.initAndUpdateAchieve(playerId, entity);
            //给客户端同步数据
            this.syncActivityInfo(playerId, entity);
        });
    }

    /**
     * 玩家登陆
     *
     * @param playerId
     */
    @Override
    public void onPlayerLogin(String playerId) {
    }

    /**
     * 玩家登陆事件的监听函数
     *
     * @param event
     */
    @Subscribe
    public void onEvent(ContinueLoginEvent event) {
        String playerId = event.getPlayerId();
        //活动是否开启，没开不继续处理
        if (!isOpening(playerId)) {
            return;
        }
        //去当前期数
        int termId = getActivityTermId(playerId);
        //取当前期数结束时间
        long endTime = getTimeControl().getEndTimeByTermId(termId, playerId);
        //取当前时间
        long now = HawkTime.getMillisecond();
        //如果当前时间大于当前期数结束时间，不继续处理
        if (now >= endTime) {
            return;
        }
        Optional<HonourHeroReturnEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }

        HonourHeroReturnEntity entity = opEntity.get();
        //玩家数据里记录当前天的的日期，说明已经处理过每日登陆成就，不继续处理
        if (entity.getLoginDaysList().contains(HawkTime.getYyyyMMddIntVal())) {
            return;
        }
        //记录当天日期
        entity.recordLoginDay();
        initAndUpdateAchieve(playerId, entity);
        //给客户端同步数据
        syncActivityInfo(playerId, entity);
    }

    private void initAndUpdateAchieve(String playerId, HonourHeroReturnEntity entity) {
        //这里初始化了成就，同时初始化了数据库List<AchieveItem>，核心是处理AchieveItemCreateEvent
        initAchieve(playerId, entity);
        //这里如果initAchieve不是create事件，需要把成就数据初始化一下
        initAchieveItems(entity);
        //抛每日登陆成就事件
        ActivityManager.getInstance().postEvent(new HonourHeroReturnDailyLoginEvent(playerId, 1), true);
        AchievePushHelper.pushAchieveUpdate(playerId, entity.getItemList());
    }

    /**
     * 玩家选择抽奖页的协议处理函数
     * @param playerId
     * @param pageCfgId
     */
    public void onSelectLotteryPage(String playerId, int pageCfgId){
        int size = HawkConfigManager.getInstance().getConfigSize(HonourHeroReturnRewardCfg.class);
        HonourHeroReturnRewardCfg cfg =
                HawkConfigManager.getInstance().getConfigByKey(HonourHeroReturnRewardCfg.class, pageCfgId);
        if(null == cfg){
            return;
        }
        Optional<HonourHeroReturnEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }

        HonourHeroReturnEntity entity = opEntity.get();
        //玩家数据里记录当前天的的日期，说明已经处理过每日登陆成就，不继续处理
        entity.setLotteryPage(cfg.getId());
        this.syncActivityInfo(playerId, entity);
    }

    /**
     * 抽奖
     *
     * @param playerId
     */
    public void lottery(String playerId, int type) {
        if (type == lotteryType.lotteryType_1.value) {
            this.lotteryRewardsOne(playerId);
        } else if (type == lotteryType.lotteryType_10.value) {
            this.lotteryRewardsTen(playerId);
        }
    }

    /**
     * 10抽
     *
     * @param playerId
     */
    private void lotteryRewardsTen(String playerId) {
        Optional<HonourHeroReturnEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return;
        }
        //为空则初始化
        HonourHeroReturnEntity entity = optional.get();
        HonourHeroReturnKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonourHeroReturnKVCfg.class);
        //次数不足
        int lCount = entity.getTotalLotteryCount();
        if (lCount + 10 > cfg.getLimitTimes()) {
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                    HP.code2.HONOUR_HERO_RETURN_LOTTERY_REQ_VALUE,
                    Status.Error.HONOUR_HERO_RETURN_REWARD_TIMES_NOT_ENOUGH_VALUE);
            logger.info("HonourHeroReturnActivity,lotteryRewardsTen,fail,countless,playerId: " + "{},curCount:{},lotteryCount:{}", playerId, lCount, 10);
            return;
        }
        List<RewardItem.Builder> costList = cfg.getTenCost();
        HonourHeroReturnRewardCfg rewardCfg = HawkConfigManager.getInstance().
                getConfigByKey(HonourHeroReturnRewardCfg.class, entity.getLotteryPage());

        if(null == rewardCfg){
            return;
        }
        //检查消耗
        boolean cost = this.getDataGeter().cost(playerId, costList, 1, Action.HONOUR_HERO_RETURN_LOTTERY_COST, false);
        if (!cost) {
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                    HP.code2.HONOUR_HERO_RETURN_LOTTERY_REQ_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
            return;
        }

        //发随机奖励
        this.getDataGeter().takeRewardWithFixItem(playerId, rewardCfg.getAwardFixItem(),
                Integer.valueOf(rewardCfg.getAwardId()), 10,
                Action.HONOUR_HERO_RETURN_LOTTERY_REWARD_TEN,
                Reward.RewardOrginType.HONOUR_HERO_RETURN_LOTTERY_REWARD,
                true);

        //发固定奖励
        this.getDataGeter().takeReward(playerId, cfg.getFixedLotteryRewards(),
                10, Action.HONOUR_HERO_RETURN_LOTTERY_REWARD_TEN_FIX, true,
                Reward.RewardOrginType.HONOUR_HERO_RETURN_LOTTERY_FIX_REWARD);

        entity.addTenLotteryCount();
        //刷新数据
        this.syncActivityInfo(playerId, entity);

        logger.info("HonourHeroReturnActivity ten lottery, playerId: {}", playerId);
    }

    /**
     * 单抽
     *
     * @param playerId
     */
    private void lotteryRewardsOne(String playerId) {
        Optional<HonourHeroReturnEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return;
        }
        //取玩家数据对象
        HonourHeroReturnEntity entity = optional.get();
        //取活动配置
        HonourHeroReturnKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonourHeroReturnKVCfg.class);
        //玩家抽奖剩余次数不足，返回错误码
        int lCount = entity.getTotalLotteryCount();
        if (lCount + 1 > cfg.getLimitTimes()) {
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                    HP.code2.HONOUR_HERO_RETURN_LOTTERY_REQ_VALUE,
                    Status.Error.HONOUR_HERO_RETURN_REWARD_TIMES_NOT_ENOUGH_VALUE);

            logger.info("HonourHeroReturnActivity,lotteryRewardsOne,fail,countless,playerId: " + "{},curCount:{},lotteryCount:{}", playerId, lCount, 1);
            return;
        }

        HonourHeroReturnRewardCfg rewardCfg = HawkConfigManager.getInstance().
                getConfigByKey(HonourHeroReturnRewardCfg.class, entity.getLotteryPage());

        if(null == rewardCfg){
            return;
        }

        List<RewardItem.Builder> costList = cfg.getOneCost();
        //检查消耗
        boolean cost = this.getDataGeter().cost(playerId, costList, 1,
                Action.HONOUR_HERO_RETURN_LOTTERY_COST, false);
        if (!cost) {
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId,
                    HP.code2.HONOUR_HERO_RETURN_LOTTERY_REQ_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
            return;
        }

        //发随机奖励
        this.getDataGeter().takeRewardWithFixItem(playerId, rewardCfg.getAwardFixItem(),
                Integer.valueOf(rewardCfg.getAwardId()), 1,
                Action.HONOUR_HERO_RETURN_LOTTERY_REWARD,
                Reward.RewardOrginType.HONOUR_HERO_RETURN_LOTTERY_REWARD,
                true);


        //发固定奖励
        this.getDataGeter().takeReward(playerId, cfg.getFixedLotteryRewards(),
                1, Action.HONOUR_HERO_RETURN_LOTTERY_REWARD_FIX, true,
                Reward.RewardOrginType.HONOUR_HERO_RETURN_LOTTERY_FIX_REWARD);

        entity.addOneLotteryCount();
        this.syncActivityInfo(playerId, entity);

        logger.info("HonourHeroReturnActivity one lottery, playerId: {}", playerId);
    }

    /**
     * 物品兑换
     *
     * @param playerId
     * @param exchangeId
     * @param exchangeCount
     */
    public void itemExchange(String playerId, int exchangeId, int exchangeCount) {
        HonourHeroReturnExchangeCfg config = HawkConfigManager.getInstance().
                getConfigByKey(HonourHeroReturnExchangeCfg.class, exchangeId);

        if (config == null) {
            return;
        }
        HonourHeroReturnKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonourHeroReturnKVCfg.class);
        Optional<HonourHeroReturnEntity> opDataEntity = getPlayerDataEntity(playerId);
        if (!opDataEntity.isPresent()) {
            return;
        }
        HonourHeroReturnEntity entity = opDataEntity.get();
        int eCount = entity.getExchangeCount(exchangeId);
        if (eCount + exchangeCount > config.getTimes()) {
            //错误码
            logger.info("HonourHeroReturnActivity,itemExchange,fail,countless,playerId: " + "{},exchangeType:{},ecount:{}",
                    playerId, exchangeId, eCount);
            return;
        }

        List<RewardItem.Builder> makeCost = config.getNeedItemList();
        boolean cost = this.getDataGeter().cost(playerId, makeCost, exchangeCount,
                Action.HONOUR_HERO_RETURN_EXCAHNGE_COST, true);
        if (!cost) {
            PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.HONOUR_HERO_RETURN_EXCHANGE_REQ_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
            return;
        }

        //增加兑换次数
        entity.addExchangeCount(exchangeId, exchangeCount);
        //发奖励
        this.getDataGeter().takeReward(playerId, config.getGainItemList(), exchangeCount, Action.HONOUR_HERO_RETURN_EXCAHNGE_GAIN, true);
        //同步
        this.syncActivityInfo(playerId, entity);
        logger.info("HonourHeroRichardActivity,itemExchange,sucess,playerId: " + "{},exchangeType:{},ecount:{}", playerId, exchangeId, eCount);
    }

    /**
     * 玩家勾选信息处理
     * @param playerId
     * @param tipsList
     */
    public void updateActivityTips(String playerId, List<PBHonourHeroReturnTip> tipsList){
        if (!isOpening(playerId)) {
            return;
        }
        if(tipsList.isEmpty()){
            return;
        }
        Optional<HonourHeroReturnEntity> opt = getPlayerDataEntity(playerId);
        if (!opt.isPresent()) {
            return;
        }

        HonourHeroReturnEntity entity = opt.get();

        for(PBHonourHeroReturnTip tip : tipsList){
            updateOneTip(entity, tip.getId(), tip.getTip());
        }

        this.syncActivityInfo(playerId, entity);
    }

    private void updateOneTip(HonourHeroReturnEntity entity, int id, int tip) {
        HonourHeroReturnExchangeCfg config = HawkConfigManager.getInstance().getConfigByKey(
                HonourHeroReturnExchangeCfg.class, id);
        if (config == null) {
            return;
        }

        if (tip > 0) {
            entity.addTips(id);
        } else {
            entity.removeTips(id);
        }
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        Optional<HonourHeroReturnEntity> opDataEntity = getPlayerDataEntity(playerId);
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
    public void syncActivityInfo(String playerId, HonourHeroReturnEntity entity) {
        HonourHeroReturnKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonourHeroReturnKVCfg.class);
        int lotteryCount = entity.getTotalLotteryCount();
        //组织PB数据
        Activity.PBHonourHeroReturnResp.Builder builder = PBHonourHeroReturnResp.newBuilder();
        builder.setLotteryCount(lotteryCount);
        builder.setLotteryPage(entity.getLotteryPage());
        Map<Integer, Integer> emap = entity.getExchangeNumMap();
        for (Entry<Integer, Integer> entry : emap.entrySet()) {
            PBHonourHeroReturnExchange.Builder ebuilder = PBHonourHeroReturnExchange.newBuilder();
            ebuilder.setExchangeId(entry.getKey());
            ebuilder.setNum(entry.getValue());
            builder.addExchanges(ebuilder);
        }

        List<HonourHeroReturnExchangeCfg> eList = HawkConfigManager.getInstance()
                .getConfigIterator(HonourHeroReturnExchangeCfg.class).toList();
        List<Integer>carePoints = entity.getPlayerPoints();
        for(HonourHeroReturnExchangeCfg ecfg : eList){
            if(!carePoints.contains(ecfg.getId())){
                builder.addTips(ecfg.getId());
            }
        }

        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.HONOUR_HERO_RETURN_INFO_RESP_VALUE, builder));
    }


    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        AchieveConfig cfg = HawkConfigManager.getInstance().getConfigByKey(HonourHeroReturnAchieveCfg.class, achieveId);
        return cfg;
    }

    @Override
    public Action takeRewardAction() {
        return Action.HONOUR_HERO_RETURN_ACHIVE_REWARD;
    }

    private enum lotteryType{
        lotteryType_1(1),
        lotteryType_10(10);
        public int value;
        lotteryType(int value){
            this.value = value;
        }
    }
}
