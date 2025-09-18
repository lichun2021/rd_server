package com.hawk.activity.type.impl.roseGift;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.*;
import com.hawk.activity.extend.KeyValue;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveManager;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.roseGift.cfg.RoseGiftAchieveCfg;
import com.hawk.activity.type.impl.roseGift.cfg.RoseGiftDrawCfg;
import com.hawk.activity.type.impl.roseGift.cfg.RoseGiftExchangeCfg;
import com.hawk.activity.type.impl.roseGift.cfg.RoseGiftKVCfg;
import com.hawk.activity.type.impl.roseGift.entity.RoseGiftEntity;
import com.hawk.game.protocol.*;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 玫瑰赠礼，活动id319
 *
 */
public class RoseGiftActivity extends ActivityBase implements AchieveProvider {
    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger("Server");
    
    /**
     * 本服花瓣数量
     */
    private AtomicInteger serverNumAdd = new AtomicInteger();

    private int serverNum = 0;
    private int lastServerNum = 0;
    private Set<Integer> serverEnventSet = new HashSet<>();

    /**
     * 上次注水时间
     */
    private long lastTickTime = 0;

    private boolean isInit = false;



    /**
     * 构造函数
     * @param activityId 活动id
     * @param activityEntity 活动数据库实体
     */
    public RoseGiftActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    /**
     * 获得活动类型
     * @return 活动类型
     */
    @Override
    public ActivityType getActivityType() {
        return ActivityType.ROSE_GIFT;
    }
    
    @Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

    /**
     * 实例化活动
     * @param config
     * @param activityEntity
     * @return
     */
    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        //创建活动实例
        RoseGiftActivity activity = new RoseGiftActivity(config.getActivityId(), activityEntity);
        //注册活动
        AchieveContext.registeProvider(activity);
        //加载本服花瓣总数
        //loadServerNumFromRedis(activity, activityEntity.getTermId());
        //返回活动实例
        return activity;
    }

    /**
     * 从数据库里加载玩家数据
     * @param playerId 玩家id
     * @param termId 活动期数
     * @return
     */
    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        //根据条件从数据库中检索
        List<RoseGiftEntity> queryList = HawkDBManager.getInstance()
                .query("from RoseGiftEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        //如果有数据的话，返回第一个数据
        if (queryList != null && queryList.size() > 0) {
            RoseGiftEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    /**
     * 创建玩家活动数据
     * @param playerId 玩家id
     * @param termId 活动期数
     * @return
     */
    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        RoseGiftEntity entity = new RoseGiftEntity(playerId, termId);
        return entity;
    }

    /**
     * 成就是否激活
     * @param playerId 玩家id
     * @return 激活状态
     */
    @Override
    public boolean isProviderActive(String playerId) {
        //活动开就激活
        return isOpening(playerId);
    }

    /**
     * 成就状态是否同步
     * @param playerId 玩家id
     * @return 同步状态
     */
    @Override
    public boolean isProviderNeedSync(String playerId) {
        //活动展示就同步
        return isShow(playerId);
    }

    /**
     * 获得玩家活动成就数据
     * @param playerId 玩家id
     * @return 玩家活动成就数据
     */
    @Override
    public Optional<AchieveItems> getAchieveItems(String playerId) {
        //获得玩家活动数据
        Optional<RoseGiftEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        //获得玩家活动数据实体
        RoseGiftEntity entity = opEntity.get();
        //如果成就数据为空就初始化成就数据
        if (entity.getItemList().isEmpty()) {
            //初始化成就数据
            this.initAchieve(playerId);
        }
        //返回当前成就数据
        AchieveItems items = new AchieveItems(entity.getItemList(), entity);
        return Optional.of(items);
    }

    /**
     * 初始化成就数据
     * @param playerId 玩家id
     */
    private void initAchieve(String playerId) {
        Optional<RoseGiftEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        RoseGiftEntity entity = opEntity.get();
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        ConfigIterator<RoseGiftAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(RoseGiftAchieveCfg.class);
        List<AchieveItem> list = new ArrayList<>();
        while (iterator.hasNext()) {
            RoseGiftAchieveCfg cfg = iterator.next();
            AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
            list.add(item);
        }
        entity.setItemList(list);
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()));
        ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, 1, this.providerActivityId()));
    }

    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(RoseGiftAchieveCfg.class, achieveId);
        return config;
    }

    @Override
    public Action takeRewardAction() {
        return Action.ROSE_GIFT_ACHIEVE_REWARD;
    }

    /**
     * 成就领奖前置操作，用于判断成就是否可以领取
     * @param playerId 玩家id
     * @param achieveId 成就id
     * @return 前置判断结果
     */
    @Override
    public Result<?> onTakeReward(String playerId, int achieveId) {
        //获得获救配置
        AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(RoseGiftAchieveCfg.class, achieveId);
        //如果不是本服花瓣数成就的话就直接返回
        if(config.getAchieveType() != AchieveType.ROSE_GIFT_SERVER_NUM){
            return Result.success();
        }
        //获得玩家活动数据
        Optional<RoseGiftEntity> opEntity = getPlayerDataEntity(playerId);
        //如果玩家活动数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        RoseGiftEntity entity = opEntity.get();
        //如果今天没充值返回错误码
        if(!entity.isPayToday()){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        return Result.success();
    }

    /**
     * 成就领奖后置操作，用于判断奖励中是否有花瓣
     * @param playerId 玩家id
     * @param reweardList 奖励数据
     * @param achieveId 成就id
     */
    @Override
    public void onTakeRewardSuccessAfter(String playerId, List<Reward.RewardItem.Builder> reweardList, int achieveId) {
        //判断活动是否处于开启状态
        if(!isOpening(playerId)){
            return;
        }
        //获得玩家活动数据
        Optional<RoseGiftEntity> opEntity = getPlayerDataEntity(playerId);
        //如果玩家活动数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        RoseGiftEntity entity = opEntity.get();
        //活动活动总配置
        RoseGiftKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(RoseGiftKVCfg.class);
        //本服增加数量
        int sumNum = 0;
        //遍历奖励内容
        for(Reward.RewardItem.Builder item : reweardList){
            //如果奖励里面有花瓣就抛成就事件
            if(item.getItemId() == kvcfg.getPetal()){
                int num = (int)item.getItemCount();
                entity.setSelfNum(entity.getSelfNum() + num);
                ActivityManager.getInstance().postEvent(new RoseGiftSelfEvent(playerId, entity.getSelfNum()));
                syncActivityInfo(entity);
                sumNum += num;
            }
        }
        serverNumAdd.addAndGet(sumNum);
    }

    /**
     * 增加本服花瓣数量
     * @param num 当前数量
     */
    private void sycnServerNum(int num){
        if(num != 0 && num <= lastServerNum){
            return;
        }
        lastServerNum = num;
        //当前在线玩家
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        //把数量存redis
        ActivityGlobalRedis.getInstance().set(getRedisKey(getActivityTermId()), String.valueOf(num));
        ActivityGlobalRedis.getInstance().set(getTimeRedisKey(getActivityTermId()), String.valueOf(lastTickTime));
        boolean isUpdate = false;
        ConfigIterator<RoseGiftAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(RoseGiftAchieveCfg.class);
        for(RoseGiftAchieveCfg cfg : iterator){
            if(cfg.getAchieveType() != AchieveType.ROSE_GIFT_SERVER_NUM){
                continue;
            }
            int configValue = cfg.getConditionValue(0);
            if(num >= configValue && !serverEnventSet.contains(cfg.getAchieveId())){
                serverEnventSet.add(cfg.getAchieveId());
                isUpdate = true;
            }
        }
        if(isUpdate){
            //遍历玩家
            for(String playerId : onlinePlayerIds){
                //投递事件
                callBack(playerId, GameConst.MsgId.ON_ROSE_GIFT_SERVER_NUM, () -> {
                    //更新本服获得数量成就
                    ActivityManager.getInstance().postEvent(new RoseGiftServerEvent(playerId, num));
                });
            }
        }
    }

    /**
     * 获得redis key
     * @param termId 活动期数
     * @return 拼装好的key
     */
    private String getRedisKey(int termId) {
        String serverId = this.getDataGeter().getServerId();
        String key = String.format("%s:%s:%d", serverId, "ROSE_GIFT_SERVER_NUM", termId);
        return key;
    }

    private String getTimeRedisKey(int termId) {
        String serverId = this.getDataGeter().getServerId();
        String key = String.format("%s:%s:%d", serverId, "ROSE_GIFT_SERVER_LAST", termId);
        return key;
    }

    /**
     * 从数据库中加载
     * @param termId 活动期数
     */
    private void loadServerNumFromRedis(RoseGiftActivity activity, int termId){
        String numStr = ActivityLocalRedis.getInstance().get(getRedisKey(termId));
        int num = HawkOSOperator.isEmptyString(numStr)? 0 : Integer.parseInt(numStr);
        activity.setServerNum(num);
        String timeStr = ActivityLocalRedis.getInstance().get(getTimeRedisKey(termId));
        long time = HawkOSOperator.isEmptyString(timeStr)? 0 : Long.parseLong(timeStr);
        activity.setLastTickTime(time);
    }

    public void setServerNum(int serverNum) {
        this.serverNum = serverNum;
    }

    public void setLastTickTime(long lastTickTime) {
        this.lastTickTime = lastTickTime;
    }

    @Override
    public void onQuickTick() {
        ActivityEntity activityEntity = getActivityEntity();
        if (activityEntity.getActivityState() != ActivityState.OPEN) {
            return;
        }
        if(!isInit){
            isInit = true;
            loadServerNumFromRedis(this, getActivityTermId());
        }
        //当前时间
        long now = HawkTime.getMillisecond();
        if(!HawkTime.isSameDay(lastTickTime, now)){
            lastTickTime = now;
            serverNumAdd.set(0);
            serverNum = 0;
            serverEnventSet.clear();
            sycnServerNum(serverNum);
            return;
        }
        int num = serverNumAdd.getAndSet(0);
        if(num <= 0){
            return;
        }
        serverNum += num;
        sycnServerNum(serverNum);
    }

    /**
     * 通过tick进行全服花瓣数量注水
     */
    @Override
    public void onTick() {
        ActivityEntity activityEntity = getActivityEntity();
        if (activityEntity.getActivityState() != ActivityState.OPEN) {
            return;
        }
        if(!isInit){
            isInit = true;
            loadServerNumFromRedis(this, getActivityTermId());
        }
        //当前时间
        long now = HawkTime.getMillisecond();
        if(!HawkTime.isSameDay(lastTickTime, now)){
            lastTickTime = now;
            serverNumAdd.set(0);
            serverNum = 0;
            serverEnventSet.clear();
            sycnServerNum(serverNum);
            return;
        }
        if(now - lastTickTime < TimeUnit.MINUTES.toMillis(1)) {
            return;
        }
        //记录本次tick时间
        lastTickTime = now;
        //计算注水量，本次仅和时间相关
        int hour = HawkTime.getHour();
        RoseGiftKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RoseGiftKVCfg.class);
        //本次注水数
        int num = cfg.getWaterFlood(hour);
        //增加本服花瓣数
        serverNum += num;
        sycnServerNum(serverNum);
    }

    @Override
    public void onPlayerLogin(String playerId) {
        //回收玩家道具
        this.recoverItem(playerId);
        if(!isOpening(playerId)){
            return;
        }
        ActivityManager.getInstance().postEvent(new RoseGiftServerEvent(playerId, serverNum));
        //获得玩家活动数据
        Optional<RoseGiftEntity> opEntity = getPlayerDataEntity(playerId);
        //如果玩家活动数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        RoseGiftEntity entity = opEntity.get();
        ActivityManager.getInstance().postEvent(new RoseGiftSelfEvent(playerId, entity.getSelfNum()));
    }

    public void recoverItem(String playerId) {
        //取当前活动期数
        int termId = this.getActivityTermId();
        if (termId > 0) {
            return;
        }
        RoseGiftKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RoseGiftKVCfg.class);
        //取玩家身上此道具的数量
        int count = this.getDataGeter().getItemNum(playerId, cfg.getRose());
        if(count <= 0){
            return;
        }
        List<Reward.RewardItem.Builder> costList = new ArrayList<>();
        Reward.RewardItem.Builder costBuilder = Reward.RewardItem.newBuilder();
        //类型为道具
        costBuilder.setItemType(Const.ItemType.TOOL_VALUE);
        //待扣除物品ID
        costBuilder.setItemId(cfg.getRose());
        //待扣除的物品数量
        costBuilder.setItemCount(count);
        //把待扣除的物品数据加入参数容器
        costList.add(costBuilder);
        //注意这里先扣除源道具，如果失败，不给兑换后的道具
        boolean cost = this.getDataGeter().cost(playerId,costList, 1, Action.ROSE_GIFT_DRAW_RECOVER, true);
        //扣除失败不继续处理
        if (!cost) {
            return;
        }
    }

    @Override
    public void onOpen() {
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for(String playerId : onlinePlayerIds){
            callBack(playerId, GameConst.MsgId.ON_ROSE_GIFT_ACTIVITY_OPEN, () -> {
                this.syncActivityInfo(playerId);
                initAchieve(playerId);
            });
        }
    }

    @Override
    public void onHidden() {
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for(String playerId : onlinePlayerIds){
            callBack(playerId, GameConst.MsgId.ON_ROSE_GIFT_ACTIVITY_END, () -> {
                recoverItem(playerId);
            });
        }
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        this.syncActivityInfo(playerId);
    }

    /**
     * 处理跨天事件
     * @param event 跨天事件
     */
    @Subscribe
    public void onContinueLoginEvent(ContinueLoginEvent event){
        if(!event.isCrossDay()){
            return;
        }
        //获取玩家id
        String playerId = event.getPlayerId();
        //判断活动是否处于开启状态
        if(!isOpening(playerId)){
            return;
        }
        //获得玩家活动数据
        Optional<RoseGiftEntity> opEntity = getPlayerDataEntity(playerId);
        //如果玩家活动数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        RoseGiftEntity entity = opEntity.get();
        //重置当天充值状态
        entity.setPayToday(false);
        //需要每天重置的成就的成就id
        Set<Integer> needResetSet = new HashSet<>();
        //遍历活动成就配置
        ConfigIterator<RoseGiftAchieveCfg> ite = HawkConfigManager.getInstance().getConfigIterator(RoseGiftAchieveCfg.class);
        while (ite.hasNext()) {
            RoseGiftAchieveCfg cfg = ite.next();
            //如果需要重置就记录下来
            if(cfg.isReset()){
                needResetSet.add(cfg.getAchieveId());
            }
        }
        
        String redisKey = this.getDataGeter().getGmRechargeRedisKey();
        Map<String, String> rechargeDataMap = ActivityGlobalRedis.getInstance().hgetAll(redisKey + ":" + playerId);
        String todayTime = String.valueOf(HawkTime.getYyyyMMddIntVal());
        int redisDiamonds = Integer.parseInt(rechargeDataMap.getOrDefault(todayTime, "0"));
        int rechargeDiamonds = redisDiamonds / 10;
        if (redisDiamonds % 10 > 0) {
        	rechargeDiamonds += 1;
        }
        if (rechargeDiamonds > 0) {
        	HawkLog.logPrintln("RoseGiftActivity IDIPGmRecharge calc on login, playerId: {}, rechargeDiamonds: {}", playerId, rechargeDiamonds);
        }
        
        //数据有变化的成就，需要推送给前端
        List<AchieveItem> needPushList = new ArrayList<>();
        //遍历成就数据
        for(AchieveItem item : entity.getItemList()){
            //如果成就是需要重置的成就，就重置成就数据和状态
            if(needResetSet.contains(item.getAchieveId())){
                if(item.getValue(0) == 0 && item.getState() == Activity.AchieveState.NOT_ACHIEVE_VALUE){
                    continue;
                }
                
                //设置数据为0
                item.setValue(0, rechargeDiamonds);
                //设置成就状态
                item.setState(Activity.AchieveState.NOT_ACHIEVE_VALUE);
                if (rechargeDiamonds > 0) {
                	entity.setPayToday(true);
                	AchieveConfig achieveConfig = AchieveManager.getInstance().getAchieveConfig(item.getAchieveId());
                	AchieveParser<?> parser = AchieveContext.getParser(achieveConfig.getAchieveType());
                	if (parser.isFinish(item, achieveConfig)) {
                		item.setState(AchieveState.NOT_REWARD_VALUE);
                		KeyValue<AchieveConfig, AchieveProvider> configAndProvider = AchieveManager.getInstance().getAchieveConfigAndProvider(achieveConfig.getAchieveId());
        				if (configAndProvider != null) {
        					configAndProvider.getValue().onAchieveFinished(event.getPlayerId(), item);
        				}
                	}
                }
                //添加到需要推送列表里
                needPushList.add(item);
            }
        }
        entity.notifyUpdate();
        //推送给前端
        AchievePushHelper.pushAchieveUpdate(playerId, needPushList);
        ActivityManager.getInstance().postEvent(new LoginDaysActivityEvent(playerId, 1, this.providerActivityId()));
    }

    /**
     * 处理充值事件，包含直购和买金条
     * @param event 充值事件
     */
    @Subscribe
    public void onRechargeAllRmbEvent(RechargeAllRmbEvent event) {
        String playerId = event.getPlayerId();
        onRechargeAllRmbEvent(playerId, 0);
    }
    
    /**
     * 处理充值事件，包含直购和买金条
     * @param event 充值事件
     */
    @Subscribe
    public void onRechargeAllRmbEvent(ShareProsperityEvent event) {
        String playerId = event.getPlayerId();
        int value = event.getDiamondNum() / 10;
        onRechargeAllRmbEvent(playerId, value);
    }
    
    private void onRechargeAllRmbEvent(String playerId, int value) {
    	//判断活动是否开启
        if(!isOpening(playerId)){
            return;
        }
        //获得玩家活动数据
        Optional<RoseGiftEntity> opEntity = getPlayerDataEntity(playerId);
        //如果活动数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        RoseGiftEntity entity = opEntity.get();
        //如果当天充值状态为false的话，设置当天充值状态为true
        if(!entity.isPayToday()){
            entity.setPayToday(true);
            syncActivityInfo(playerId);
        }
        
        if (value > 0) {
        	AchieveManager.getInstance().onSpecialAchieve(this, playerId, entity.getItemList(), AchieveType.RECHARGE_ALL_RMB, value);
        }
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
        Optional<RoseGiftEntity> opEntity = getPlayerDataEntity(playerId);
        //如果玩家活动数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        RoseGiftEntity entity = opEntity.get();
        //构造发给前端的数据
        syncActivityInfo(entity);
    }

    /**
     * 同步数据
     * @param entity
     */
    public void syncActivityInfo(RoseGiftEntity entity) {
        //构造发给前端的数据
        Activity.RoseGiftInfo.Builder builder = Activity.RoseGiftInfo.newBuilder();
        //已经兑换数量
        for(int exchangeId : entity.getExchangeNumMap().keySet()){
            Activity.RoseGiftExchangeInfo.Builder exchangeInfo = Activity.RoseGiftExchangeInfo.newBuilder();
            exchangeInfo.setExchangeId(exchangeId);
            exchangeInfo.setNum(entity.getExchangeNumMap().get(exchangeId));
            builder.addExchangeInfos(exchangeInfo);
        }
        //已经抽奖数量
        for(int drawId : entity.getDrawNumMap().keySet()){
            Activity.RoseGiftDrawInfo.Builder drawInfo = Activity.RoseGiftDrawInfo.newBuilder();
            drawInfo.setDrawId(drawId);
            drawInfo.setNum(entity.getDrawNumMap().get(drawId));
            builder.addDrawInfos(drawInfo);
        }
        //兑换提醒
        ConfigIterator<RoseGiftExchangeCfg> ite = HawkConfigManager.getInstance().getConfigIterator(RoseGiftExchangeCfg.class);
        while (ite.hasNext()){
            RoseGiftExchangeCfg cfg = ite.next();
            if(!entity.getPlayerPoints().contains(cfg.getId())){
                builder.addTips(cfg.getId());
            }
        }
        //当天充值状态
        builder.setIsPay(entity.isPayToday());
        builder.setSelfNum(entity.getSelfNum());
        builder.setServerNum(serverNum);
        //同步信息
        PlayerPushHelper.getInstance().pushToPlayer(entity.getPlayerId(), HawkProtocol.valueOf(HP.code.ROSE_GIFT_SYNC, builder));
    }

    /**
     * 兑换
     * @param playerId 玩家id
     * @param exchangeId 兑换id
     * @param num 兑换数量
     * @return
     */
    public Result<Integer> exchange(String playerId, int exchangeId, int num) {
        //判断活动是否开启，如果没开返回错误码
        if(!isOpening(playerId)){
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //获取兑换配置，如果配置为空返回错误码
        RoseGiftExchangeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RoseGiftExchangeCfg.class, exchangeId);
        if (cfg == null) {
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据，如果活动数据为空直接返回
        Optional<RoseGiftEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        RoseGiftEntity entity = opEntity.get();
        //当前已经兑换数量
        int buyNum = entity.getExchangeNumMap().getOrDefault(exchangeId, 0);
        //兑换后的数量
        int newNum = buyNum + num;
        //判断是否超过可兑换数量最大值，如果超过返回错误码
        if(newNum > cfg.getTimes()){
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        //兑换消耗
        boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), num, Action.ROSE_GIFT_EXCHANGE, true);
        //如果不够消耗，返回错误码
        if (!flag) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        //设置新的兑奖数量
        entity.getExchangeNumMap().put(exchangeId, newNum);
        entity.notifyUpdate();
        //发奖
        this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.ROSE_GIFT_EXCHANGE_REWARD, true);
        //记录日志
        logger.info("rose_gift_exchange playerId:{}, exchangeId:{}, num:{}", playerId, exchangeId, num);
        syncActivityInfo(entity);
        //返回兑换状态
        return Result.success(newNum);
    }

    /**
     * 兑换勾选提醒
     * @param playerId 玩家id
     * @param ids 兑换id
     * @param tips 勾选类型 0为去掉 1为增加 2为全选 3为全取消
     * @return
     */
    public Result<?> exchangeTips(String playerId, List<Integer> ids, int tips){
        //判断活动是否开启，如果没开返回错误码
        if(!isOpening(playerId)){
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //获得玩家活动数据，如果为空但会错误码
        Optional<RoseGiftEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //活动玩家活动数据实体
        RoseGiftEntity entity = opEntity.get();
        switch(tips) {
            case 0: {
                for(int id : ids){
                    if(entity.getPlayerPoints().contains(id)){
                        continue;
                    }
                    entity.getPlayerPoints().add(id);
                }
                entity.notifyUpdate();
            }
            break;
            case 1: {
                for(int id : ids){
                    entity.getPlayerPoints().remove(new Integer(id));
                }
                entity.notifyUpdate();
            }
            break;
        }
        syncActivityInfo(entity);
        return Result.success();
    }

    /**
     * 抽奖
     * @param playerId 玩家id
     * @return 抽奖结果
     */
    public Result<?> draw(String playerId){
        //获得玩家活动数据，如果为空但会错误码
        Optional<RoseGiftEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        RoseGiftEntity entity = opEntity.get();
        //遍历活动数据，获得还可以抽奖的奖项
        ConfigIterator<RoseGiftDrawCfg> ite = HawkConfigManager.getInstance().getConfigIterator(RoseGiftDrawCfg.class);
        Map<Integer, Integer> randomMap = new HashMap<>();
        while (ite.hasNext()){
            RoseGiftDrawCfg cfg = ite.next();
            int num = entity.getDrawNumMap().getOrDefault(cfg.getId(), 0);
            if(num >= cfg.getTimes()){
                continue;
            }
            randomMap.put(cfg.getId(), cfg.getWeight());
        }
        if(randomMap.isEmpty()){
            return Result.fail(Status.Error.ROSE_GIFT_DRAW_NOT_ENOUGH_VALUE);
        }
        //抽奖
        Integer draw = HawkRand.randomWeightObject(randomMap);
        //如果没抽出来返回错误码
        if(draw == null ){
            return Result.fail(Status.Error.ROSE_GIFT_DRAW_NOT_ENOUGH_VALUE);
        }
        //消耗一朵玫瑰，如果不够消耗返回错误码
        RoseGiftKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(RoseGiftKVCfg.class);
        Reward.RewardItem.Builder costBuilder = RewardHelper.toRewardItem(Const.ItemType.TOOL_VALUE * GameConst.ITEM_TYPE_BASE, kvcfg.getRose(), 1);
        boolean flag = this.getDataGeter().cost(playerId, Arrays.asList(costBuilder), 1, Action.ROSE_GIFT_DRAW, true);
        if (!flag) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        //获得抽奖配置
        RoseGiftDrawCfg drawCfg = HawkConfigManager.getInstance().getConfigByKey(RoseGiftDrawCfg.class, draw);
        //被抽数量加一
        int num = entity.getDrawNumMap().getOrDefault(draw, 0) + 1;
        //抽奖次数存库
        entity.getDrawNumMap().put(draw, num);
        entity.notifyUpdate();
        //发奖
        this.getDataGeter().takeReward(playerId, drawCfg.getGainItemList(), 1, Action.ROSE_GIFT_DRAW_REWARD, true, Reward.RewardOrginType.ROSE_GIFT_REWARD);
        //通知前端抽到的抽奖项
        Activity.RoseGiftDrawInfo.Builder builder = Activity.RoseGiftDrawInfo.newBuilder();
        builder.setDrawId(draw);
        builder.setNum(num);
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.ROSE_GIFT_DRAW_RESP, builder));
        syncActivityInfo(entity);
        if(drawCfg.isNotice()){
            String playerName = getDataGeter().getPlayerName(playerId);
            Reward.RewardItem.Builder item = drawCfg.getGainItemList().get(0);
            int itemId = item.getItemId();
            long itemNum = item.getItemCount();
            sendBroadcast(Const.NoticeCfgId.ROSE_GIFT_NOTICE, null, playerName, itemId, itemNum);
            String guildId = this.getDataGeter().getGuildId(playerId);
            if(!HawkOSOperator.isEmptyString(guildId)){
                this.getDataGeter().addWorldBroadcastMsg(Const.ChatType.GUILD_HREF, guildId,
                        Const.NoticeCfgId.ROSE_GIFT_NOTICE, playerId, playerName, itemId, itemNum);
            }
        }
        return Result.success();
    }
}
