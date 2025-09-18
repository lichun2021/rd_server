package com.hawk.activity.type.impl.seasonActivity;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.DYZZGradeEvent;
import com.hawk.activity.event.impl.GuildDismissEvent;
import com.hawk.activity.event.impl.GuildQuiteEvent;
import com.hawk.activity.event.impl.SeasonOrderAuthBuyEvent;
import com.hawk.activity.event.speciality.OrderEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.activity.type.impl.order.IOrderActivity;
import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;
import com.hawk.activity.type.impl.order.task.OrderTaskContext;
import com.hawk.activity.type.impl.order.task.OrderTaskParser;
import com.hawk.activity.type.impl.rank.ActivityRankContext;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.activity.type.impl.seasonActivity.cfg.*;
import com.hawk.activity.type.impl.seasonActivity.data.SeasonActivityGuildGradeData;
import com.hawk.activity.type.impl.seasonActivity.entity.SeasonActivityEntity;
import com.hawk.activity.type.impl.seasonActivity.entity.SeasonActivityGuildGradeEntity;
import com.hawk.activity.type.impl.seasonActivity.rank.GuildSeasonKingGradeInfo;
import com.hawk.activity.type.impl.seasonActivity.rank.GuildSeasonKingGradeRank;
import com.hawk.activity.type.impl.seasonActivity.rank.GuildSeasonKingGradeRankProvider;
import com.hawk.game.protocol.*;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 赛季活动活动类
 * ActivityBase 活动基类
 * IOrderActivity 战令活动接口
 * IExchangeTip 兑换提醒接口<兑换配置>
 */

public class SeasonActivity extends ActivityBase implements IOrderActivity, IExchangeTip<SeasonShopCfg> {
    //赛季活动段位王者排行耪Redis键前缀
    private static final String SEASON_GUILD_GRADE_KING_RANK = "SEASON_GUILD_GRADE_KING_RANK";
    //段位数据内存数据 主键：联盟id 值：段位数据
    Map<String, SeasonActivityGuildGradeData> guildGradeEntityMap = new ConcurrentHashMap<>();

    /**
     * 构造函数
     * @param activityId 活动数据
     * @param activityEntity 活动数据实体
     */
    public SeasonActivity(int activityId, ActivityEntity activityEntity) {
        //调用父类构造函数
        super(activityId, activityEntity);
    }

    /**
     * 获得活动类型枚举
     * @return 活动类型枚举
     */
    @Override
    public ActivityType getActivityType() {
        //返回赛季活动
        return ActivityType.SEASON_ACTIVITY;
    }

    /**
     * 活动实例方法
     * @param config 活动配置activity.xml
     * @param activityEntity 活动数据实体
     * @return
     */
    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        //赛季活动数据
        SeasonActivity activity = new SeasonActivity(config.getActivityId(), activityEntity);
        return activity;
    }

    /**
     * 从数据库里面加载数据
     * @param playerId 玩家id
     * @param termId 活动期数
     * @return 玩家活动数据数据实体
     */
    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        //根据玩家id和活动期数找到玩家活动数据
        List<SeasonActivityEntity> queryList = HawkDBManager.getInstance()
                .query("from SeasonActivityEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        //取数据库返回结果的第一个
        if (queryList != null && queryList.size() > 0) {
            SeasonActivityEntity entity = queryList.get(0);
            return entity;
        }
        //不存在的话返回空
        return null;
    }

    /**
     * 玩家活动数据实例化
     * @param playerId 玩家id
     * @param termId 活动期数
     * @return 玩家活动数据实体
     */
    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        //赛季活动数据
        SeasonActivityEntity entity = new SeasonActivityEntity(playerId, termId);
        return entity;
    }

    /**
     * 给前端同步全量数据
     * @param playerId 玩家id
     */
    @Override
    public void syncActivityDataInfo(String playerId) {
        //构建前端数据
        Activity.SeasonInfoSync.Builder builder = Activity.SeasonInfoSync.newBuilder();
        //遍历赛事类型
        for(Activity.SeasonMatchType matchType : Activity.SeasonMatchType.values()){
            //添加赛事时间信息
            builder.addInfos(getSeasonMatchInfo(matchType));
        }
        //获得玩家活动数实体
        Optional<SeasonActivityEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return;
        }
        SeasonActivityEntity entity = optional.get();
        //构造段位信息
        builder.setGradeInfo(getGradeInfo(playerId));
        //构造战令基础信息
        builder.setOrderInfo(getOrderBaseInfo(entity));
        //构造战令任务信息
        getOrderItem(builder, entity);
        //构造兑换商店兑换信息
        getShopItem(builder, entity);
        //构造兑换商店提醒信息
        getTips(builder, entity);
        //拆合服时间
        getMergerAndSeperate(builder);
        //发送给前端
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.SEASON_INFO_SYNC, builder));
    }

    /**
     * 给前端同步战令基础信息
     * @param entity 玩家活动数据实体
     */
    public void syncActivityOrderBaseInfo(SeasonActivityEntity entity){
        //构造数据
        Activity.SeasonInfoSync.Builder builder = Activity.SeasonInfoSync.newBuilder();
        //添加战令基础信息
        builder.setOrderInfo(getOrderBaseInfo(entity));
        //发送给前端
        PlayerPushHelper.getInstance().pushToPlayer(entity.getPlayerId(), HawkProtocol.valueOf(HP.code2.SEASON_ORDER_BASE_SYNC, builder));
    }

    /**
     * 给前端同步兑换商店信息
     * @param entity 玩家活动数据实体
     */
    public void syncActivityShopInfo(SeasonActivityEntity entity) {
        //构造数据
        Activity.SeasonInfoSync.Builder builder = Activity.SeasonInfoSync.newBuilder();
        //兑换信息
        getShopItem(builder, entity);
        //提醒信息
        getTips(builder, entity);
        //发送给前端
        PlayerPushHelper.getInstance().pushToPlayer(entity.getPlayerId(), HawkProtocol.valueOf(HP.code2.SEASON_SHOP_SYNC, builder));
    }

    /**
     * 给前端同步战令任务信息
     * @param playerId 玩家id
     * @param changeList 改变的战令任务数据
     */
    public void syncActivityOrderItem(String playerId, List<OrderItem> changeList) {
        //构造数据
        Activity.SeasonInfoSync.Builder builder = Activity.SeasonInfoSync.newBuilder();
        //遍历任务
        for(OrderItem item : changeList){
            //添加任务数据
            builder.addItem(item.build());
        }
        //发送给前端
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.SEASON_ORDER_ITEM_SYNC, builder));
    }

    /**
     * 初始化战令任务
     * @param entity 玩家活动数据实体
     */
    private void initOrder(SeasonActivityEntity entity){
        List<OrderItem> list = new ArrayList<>();
        //遍历配置
        ConfigIterator<SeasonAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SeasonAchieveCfg.class);
        for(SeasonAchieveCfg cfg : iterator){
            //创建战令任务
            OrderItem item = new OrderItem();
            item.setOrderId(cfg.getId());
            list.add(item);
        }
        //入库
        entity.setOrderList(list);
    }


    /**
     * 修复战令任务
     * @param playerId 玩家Id
     */
    private void fixOrder(String playerId){
        //如果玩家数据不存在不可购买
        Optional<SeasonActivityEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        SeasonActivityEntity entity = optional.get();
        if(entity.getOrderList().isEmpty()){
            return;
        }
        List<OrderItem> list = new ArrayList<>();
        //遍历配置
        ConfigIterator<SeasonAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SeasonAchieveCfg.class);
        for(SeasonAchieveCfg cfg : iterator){
            boolean isNew = true;
            for (OrderItem item : entity.getOrderList()){
                if(item.getOrderId() == cfg.getId()){
                    isNew = false;
                }
            }
            if(isNew){
                //创建战令任务
                OrderItem item = new OrderItem();
                item.setOrderId(cfg.getId());
                list.add(item);
            }
        }
        entity.getOrderList().addAll(list);
        entity.notifyUpdate();
    }

    /**
     * 判断礼包是否可以购买
     * @param playerId 玩家id
     * @param payGiftId 礼包id
     * @return 购买状态
     */
    public boolean checkAuthBuy(String playerId, String payGiftId){
        //活动没开不可购买
        if (!isShow(playerId)) {
            return false;
        }
        //礼包id转换成整型
        int id = Integer.valueOf(payGiftId);
        //根据id找活动礼包配置
        SeasonAuthorityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SeasonAuthorityCfg.class, id);
        //如果配置为空不可购买
        if(cfg == null){
            return false;
        }
        //如果玩家数据不存在不可购买
        Optional<SeasonActivityEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return false;
        }
        //获得玩家活动数据实体
        SeasonActivityEntity entity = optional.get();
        //如果购买标志不为0
        if(entity.getAuthorityId() != 0){
            //获得已经购买的配置
            SeasonAuthorityCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(SeasonAuthorityCfg.class, entity.getAuthorityId());
            //如果已经是2了说明已经购买到最高，不可再购买
            if(curCfg.getOrder() == 2){
                return false;
            }
            //如果当前购买的依旧是1，不可购买
            // 如果当前购买的不是补差价的，不可购买
            if(cfg.getOrder() == 1 || cfg.getSupply() != 1){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onPlayerLogin(String playerId) {
        //回收玩家道具
        this.recoverItem(playerId);
        this.fixOrder(playerId);
    }

    public void recoverItem(String playerId) {
        /*//取当前活动期数
        int termId = this.getActivityTermId();
        if (termId > 0) {
            return;
        }
        //取玩家身上此道具的数量
        int count = this.getDataGeter().getItemNum(playerId, 3180002);
        if(count <= 0){
            return;
        }
        List<Reward.RewardItem.Builder> costList = new ArrayList<>();
        Reward.RewardItem.Builder costBuilder = Reward.RewardItem.newBuilder();
        //类型为道具
        costBuilder.setItemType(Const.ItemType.TOOL_VALUE);
        //待扣除物品ID
        costBuilder.setItemId(3180002);
        //待扣除的物品数量
        costBuilder.setItemCount(count);
        //把待扣除的物品数据加入参数容器
        costList.add(costBuilder);
        //注意这里先扣除源道具，如果失败，不给兑换后的道具
        boolean cost = this.getDataGeter().cost(playerId,costList, 1, Action.SEASON_SHOP_COST, true);
        //扣除失败不继续处理
        if (!cost) {
            return;
        }*/
    }

    @Override
    public void onOpen() {
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for(String playerId : onlinePlayerIds){
            callBack(playerId, GameConst.MsgId.SEASON_ACTIVITY_SYNC, () -> {
                syncActivityDataInfo(playerId);
            });
        }
    }

    /**
     * 战令礼包购买事件
     * @param event 战令进阶直购事件
     */
    @Subscribe
    public void onAuthBuyEvent(SeasonOrderAuthBuyEvent event) {
        //获得玩家id
        String playerId = event.getPlayerId();
        //活动没开不可购买
        if (!isShow(playerId)) {
            return;
        }
        //礼包id转换成整型
        int id = Integer.valueOf(event.getPayGiftId());
        //如果配置为空不可购买
        SeasonAuthorityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SeasonAuthorityCfg.class, id);
        if(cfg == null){
            return;
        }
        //如果玩家数据不存在不可购买
        Optional<SeasonActivityEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        SeasonActivityEntity entity = optional.get();
        //如果购买标志不为0
        if(entity.getAuthorityId() != 0){
            //获得已经购买的配置
            SeasonAuthorityCfg curCfg = HawkConfigManager.getInstance().getConfigByKey(SeasonAuthorityCfg.class, entity.getAuthorityId());
            //如果已经是2了说明已经购买到最高，不可再购买
            if(curCfg.getOrder() == 2){
                return;
            }
            //如果当前购买的依旧是1，不可购买
            // 如果当前购买的不是补差价的，不可购买
            if(cfg.getOrder() == 1 || cfg.getSupply() != 1){
                return;
            }
        }
        //设置购买标志
        entity.setAuthorityId(id);
        //加经验
        addExp(entity, cfg.getExp(),0,0);
        //发奖励
        //this.getDataGeter().takeReward(playerId, cfg.getRewardList(), 1, Action.SEASON_SHOP_GET, true);
        //同步战令基础数据
        syncActivityOrderBaseInfo(entity);
        getDataGeter().logSeasonBuyOrderAuth(playerId, entity.getTermId(),cfg.getId());
    }

    /**
     * 前端的购买状态
     * @param entity
     * @return
     */
    private int getAuthorityId(SeasonActivityEntity entity){
        //根据购买标志找配置，标志为对应礼包id
        SeasonAuthorityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SeasonAuthorityCfg.class, entity.getAuthorityId());
        //如果配置为空说明未购买
        if(cfg == null){
            return 0;
        }
        //返回已解锁状态
        return cfg.getOrder();
    }

    /**
     * 战令任务触发事件
     * @param event
     */
    @Subscribe
    public void onEvent(OrderEvent event) {
        //获得玩家id
        String playerId = event.getPlayerId();
        //如果活动没开直接返回
        if (!(event instanceof DYZZGradeEvent) && !isOpening(playerId)) {
            return;
        }
        //获得玩家活动数据实体
        Optional<SeasonActivityEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return;
        }
        SeasonActivityEntity entity = optional.get();
        //获得任务解析器
        List<OrderTaskParser<?>> parsers = OrderTaskContext.getParser(event.getClass());
        //如果为空直接返回
        if (parsers == null) {
            logger.info("OrderTaskParser not found, eventClass: {}", event.getClass().getName());
            return;
        }
        logger.info("SeasonActivity on OrderEvent, eventClass: {}", event.getClass().getName());
        //获得战令任务数据
        List<OrderItem> orderList = entity.getOrderList();
        //数据变化标志
        boolean update = false;
        //变化任务数据列表
        List<OrderItem> changeList = new ArrayList<>();
        //遍历解析器
        for (OrderTaskParser<?> parser : parsers) {
            //遍历战令任务
            for (OrderItem orderItem : orderList) {
                //获得战令任务配置
                SeasonAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SeasonAchieveCfg.class, orderItem.getOrderId());
                //配置为空直接跳过
                if(cfg == null){
                    continue;
                }
                // 判定任务类型是否一致
                if (!cfg.getTaskType().equals(parser.getTaskType())) {
                    continue;
                }
                // 完全完成的任务不做处理
                if (parser.finished(orderItem, cfg)) {
                    continue;
                }
                // 处理任务数据
                if (parser.onEventUpdate(entity, cfg, orderItem, event.convert())) {
                    //添加变化列表
                    changeList.add(orderItem);
                    //设置更新状态
                    update = true;
                }
            }
        }
        //更新数据
        if (update) {
            //更新数据库
            entity.notifyUpdate();
            //更新战令基础数据
            syncActivityOrderBaseInfo(entity);
            //更新战令任务数据
            syncActivityOrderItem(playerId, entity.getOrderList());
        }
    }

    /**
     * 增加战令经验
     * @param dataEntity 玩家数据实体
     * @param addExp 增加经验值
     * @param reason 增加原因
     * @param reasonId 原因id
     */
    @Override
    public void addExp(IOrderDateEntity dataEntity, int addExp, int reason, int reasonId) {
        //不是赛季活动数据直接返回
        if(!(dataEntity instanceof SeasonActivityEntity)){
            return;
        }
        //转换数据
        SeasonActivityEntity entity = (SeasonActivityEntity)dataEntity;
        //如果增加经验小于等于0直接返回
        if (addExp <= 0) {
            return;
        }
        int oldLvl = entity.getOrderLevel();
        //老经验值
        int oldExp = entity.getOrderExp();
        //增加后的经验值
        int newExp = oldExp + addExp;
        //计算当前等级
        int newLvl = calcLevel(newExp);
        //设置经验
        entity.setOrderExp(newExp);
        //设置等级
        entity.setOrderLevel(newLvl);
        //给前端同步战令基础数据
        syncActivityOrderBaseInfo(entity);
        getDataGeter().logSeasonOrderExpChange(entity.getPlayerId(), entity.getTermId(), addExp, newExp,oldLvl, newLvl, reason, reasonId);
    }

    @Override
    public void logOrderFinishId(IOrderDateEntity dataEntity, IOrderTaskCfg cfg, OrderItem orderItem, int addTimes) {
        //不是赛季活动数据直接返回
        if(!(dataEntity instanceof SeasonActivityEntity)){
            return;
        }
        SeasonActivityEntity entity = (SeasonActivityEntity)dataEntity;
        getDataGeter().logSeasonOrderFinishId(entity.getPlayerId(), entity.getTermId(), cfg.getId(),addTimes,orderItem.getFinishTimes());
    }

    /**
     * 计算战令等级
     * @param exp 总经验
     * @return 当前等级
     */
    private int calcLevel(int exp) {
        //初始等级为1
        int level = 1;
        //遍历战令配置, 找到当前等级
        ConfigIterator<SeasonBattlepassLevelCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SeasonBattlepassLevelCfg.class);
        for(SeasonBattlepassLevelCfg cfg : iterator){
            if(exp >= cfg.getLevelUpExp() && cfg.getLevel() > level){
                level = cfg.getLevel();
            }
        }
        //返回等级
        return level;
    }

    /**
     * 添加赛事时间信息
     * @param matchType 赛事类型
     * @return 赛事时间信息
     */
    public Activity.SeasonInfo.Builder getSeasonMatchInfo(Activity.SeasonMatchType matchType){
        //构造赛事时间信息
        Activity.SeasonInfo.Builder info = Activity.SeasonInfo.newBuilder();
        //设置赛事类型
        info.setType(matchType);
        //获得本期赛事活动配置
        SeasonOpenTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(SeasonOpenTimeCfg.class, getActivityTermId());
        //根据赛事类型获得赛事期数
        int matchTermId = timeCfg.getMatchTermId(matchType.getNumber());
        //如果没有配置对应期数，则期数为零，开始时间和结束时间皆为0
        if(matchTermId == 0){
            info.setStartTime(0);
            info.setEndTime(0);
            return info;
        }
        //根据类型和期数获得对应活动的开始结束时间 first 为开始时间，second为结束时间
        HawkTuple2<Long, Long> matchTime = getDataGeter().getSeasonActivityMatchInfo(matchType, matchTermId);
        info.setStartTime(matchTime.first);
        info.setEndTime(matchTime.second);
        return info;
    }

    /**
     * 构造段位信息
     * @param playerId 玩家id
     * @return 段位信息
     */
    public Activity.SeasonGradeInfo.Builder getGradeInfo(String playerId){
        //根据玩家id获得联盟id
        String guildId = getDataGeter().getGuildId(playerId);
        //如果没有联盟返回没有段位
        if(HawkOSOperator.isEmptyString(guildId)){
            Activity.SeasonGradeInfo.Builder info = Activity.SeasonGradeInfo.newBuilder();
            info.setGradeLevel(-1);
            info.setGradeExp(0);
            return info;
        }
        //获得联盟段位数据实体
        SeasonActivityGuildGradeData entity = getGuildDataEntity(guildId);
        //构建联盟段位信息
        Activity.SeasonGradeInfo.Builder info = Activity.SeasonGradeInfo.newBuilder();
        info.setGradeLevel(entity.getLevel());
        info.setGradeExp(entity.getExp());
        return info;
    }

    /**
     * 构造战令基础信息
     * @param entity 玩家活动数据实体
     * @return 战令基础信息
     */
    public Activity.SeasonOrderBaseInfo.Builder getOrderBaseInfo(SeasonActivityEntity entity){
        //构造战令基础信息
        Activity.SeasonOrderBaseInfo.Builder info = Activity.SeasonOrderBaseInfo.newBuilder();
        //战令等级
        info.setLevel(entity.getOrderLevel());
        //战令经验
        info.setExp(entity.getOrderExp());
        //购买档位
        info.setAuthorityId(getAuthorityId(entity));
        //已领取普通奖励
        info.addAllRewardLevel(entity.getOrderRewardLevelList());
        //已领取高级奖励
        info.addAllRewardAdLevel(entity.getOrderRewardAdLevelList());
        //前端记录等级
        info.setClientLevel(entity.getClientLevel());
        return info;
    }

    /**
     * 构造战令任务信息
     * @param builder 前端活动数据信息
     * @param entity 玩家数据实体
     */
    public void getOrderItem(Activity.SeasonInfoSync.Builder builder, SeasonActivityEntity entity){
        //遍历所有战令任务数据
        for(OrderItem item : getOrderList(entity)){
            //添加战令任务信息
            builder.addItem(item.build());
        }
    }

    /**
     * 获得玩家战令任务
     * @param entity 玩家活动数据实体
     * @return 玩家战令任务
     */
    public List<OrderItem> getOrderList(SeasonActivityEntity entity){
        //如果任务数据为空，则创建任务数据
        if(entity.getOrderList().isEmpty()){
            initOrder(entity);
        }
        //返回任务数据
        return entity.getOrderList();
    }

    /**
     * 构造兑换商店兑换信息
     * @param builder 前端活动数据信息
     * @param entity 玩家活动数据实体
     */
    public void getShopItem(Activity.SeasonInfoSync.Builder builder, SeasonActivityEntity entity){
        //遍历已兑换数据
        for(Map.Entry<Integer, Integer> entry : entity.getExchangeMap().entrySet()){
            Activity.OrderShopItemPB.Builder shopItem = Activity.OrderShopItemPB.newBuilder();
            //配置id
            shopItem.setId(entry.getKey());
            //已兑换数量
            shopItem.setCount(entry.getValue());
            builder.addShopItem(shopItem);
        }
    }

    /**
     * 钩爪兑换商店提醒信息
     * @param builder 前端活动数据信息
     * @param entity 玩家活动数据实体
     */
    public void getTips(Activity.SeasonInfoSync.Builder builder, SeasonActivityEntity entity) {
        //添加兑换信息
        builder.addAllTips(getTips(SeasonShopCfg.class, entity.getTipSet()));
    }

    /**
     * 拆合服时间
     * @param builder 前端活动数据信息
     */

    public void getMergerAndSeperate(Activity.SeasonInfoSync.Builder builder) {
        int termId = getActivityTermId();
        if(termId <= 0){
            return;
        }
        SeasonOpenTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SeasonOpenTimeCfg.class, termId);
        if(cfg == null){
            return;
        }
        int serverLevel = 1;
        List<String> mergeServerList = getDataGeter().getMergeServerList();
        if(mergeServerList != null && mergeServerList.size() > 0){
            serverLevel = mergeServerList.size();
        }
        if(serverLevel >= cfg.getShowServerLevel()){
            if(!HawkOSOperator.isEmptyString(cfg.getSeperateTime())){
                builder.setSeperateTime(cfg.getSeperateTime());
            }
            if(!HawkOSOperator.isEmptyString(cfg.getMergerTime())){
                builder.setMergerTime(cfg.getMergerTime());
            }
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
        SeasonShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SeasonShopCfg.class, exchangeId);
        if (cfg == null) {
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //如果数据为空，返回错误码
        Optional<SeasonActivityEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得活动数据
        SeasonActivityEntity entity = optional.get();
        //当前已经兑换数量
        int buyNum = entity.getExchangeMap().getOrDefault(exchangeId, 0);
        //兑换后的数量
        int newNum = buyNum + num;
        //判断是否超过可兑换数量最大值，如果超过返回错误码
        if(newNum > cfg.getTimes()){
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        //兑换消耗
        boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), num, Action.SEASON_SHOP_COST, true);
        //如果不够消耗，返回错误码
        if (!flag) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        //设置新的兑奖数量
        entity.getExchangeMap().put(exchangeId, newNum);
        entity.notifyUpdate();
        //发奖
        this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.SEASON_SHOP_GET, true);
        //记录日志
        logger.info("season_shop_exchange playerId:{}, exchangeId:{}, num:{}", playerId, exchangeId, num);
        syncActivityShopInfo(entity);
        //返回兑换状态
        return Result.success(newNum);
    }

    /**
     * 领奖
     * @param playerId 玩家id
     * @param type 奖励档位 1，是普通，2，是高级
     * @param level 奖励等级
     * @return
     */
    public Result<Integer> reward(String playerId, int type, int level){
        //判断活动是否开启，如果没开返回错误码
        if (!isShow(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //如果数据为空，返回错误码
        Optional<SeasonActivityEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得活动数据
        SeasonActivityEntity entity = optional.get();
        //如果标志为0但是领取的是高档位，返回错误码
        if(type == 2 && entity.getAuthorityId()==0){
            return Result.fail(Status.Error.SEASON_ORDER_NEED_BUY_VALUE);
        }
        //如果领取等级高于当前等级，返回错误码
        if(entity.getOrderLevel() < level){
            return Result.fail(Status.Error.SEASON_ORDER_NEED_BUY_VALUE);
        }
        //获得当前等级配置
        SeasonBattlepassLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SeasonBattlepassLevelCfg.class, level);
        switch (type){
            //低档位
            case 1:{
                //如果已经领取过了返回错误码
                if(entity.getOrderRewardLevelList().contains(level)){
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                //添加领奖标记
                entity.getOrderRewardLevelList().add(level);
                //入库
                entity.notifyUpdate();
                //发奖
                if(cfg.isNormalbox()){
                    //宝箱自动开箱
                    this.getDataGeter().takeRewardAuto(playerId, cfg.getNormalRewardList().get(0), 1, Action.SEASON_ORDER_BOX_GET, true, Reward.RewardOrginType.SEASON_ACTIVITY_BOX);
                }else {
                    //普通发奖
                    this.getDataGeter().takeReward(playerId, cfg.getNormalRewardList(), 1, Action.SEASON_ORDER_GET, true);
                }
            }
            break;
            //高档位
            case 2:{
                //如果已经领取过了返回错误码
                if(entity.getOrderRewardAdLevelList().contains(level)){
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                //添加领奖标记
                entity.getOrderRewardAdLevelList().add(level);
                //入库
                entity.notifyUpdate();
                //发奖
                if(cfg.isAdvbox()){
                    //宝箱自动开箱
                    this.getDataGeter().takeRewardAuto(playerId, cfg.getAdvRewardList().get(0), 1, Action.SEASON_ORDER_BOX_AD_GET, true, Reward.RewardOrginType.SEASON_ACTIVITY_BOX);
                }else {
                    //普通发奖
                    this.getDataGeter().takeReward(playerId, cfg.getAdvRewardList(), 1, Action.SEASON_ORDER_AD_GET, true);
                }

            }
            break;
        }
        //给前端同步战令基础信息
        syncActivityOrderBaseInfo(entity);
        return Result.success();
    }

    public void rewardAll(SeasonActivityEntity entity){
        ConfigIterator<SeasonBattlepassLevelCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SeasonBattlepassLevelCfg.class);
        List<Reward.RewardItem.Builder> rewardLidt = new ArrayList<>();
        for(SeasonBattlepassLevelCfg cfg : iterator){
            int level = cfg.getLevel();
            if(entity.getOrderLevel() < level){
                continue;
            }
            if(!entity.getOrderRewardLevelList().contains(level)){
                entity.getOrderRewardLevelList().add(level);
                rewardLidt.addAll(cfg.getNormalRewardList());
            }
            if(!entity.getOrderRewardAdLevelList().contains(level) && entity.getAuthorityId()!=0){
                entity.getOrderRewardAdLevelList().add(level);
                rewardLidt.addAll(cfg.getAdvRewardList());
            }
        }
        entity.notifyUpdate();
        getDataGeter().sendMail(entity.getPlayerId(), MailConst.MailId.SEASON_ACTIVITY_ORDER_REWARD, null, null, null,rewardLidt,false);
    }

    /**
     * 通过排名增加联盟段位奖励
     * @param matchType 赛事类型
     * @param guildId 联盟id
     * @param rank 联盟排名
     */
    public void addGuildGradeExpFromMatchRank(Activity.SeasonMatchType matchType, String guildId, int rank){
        try {
            //如果活动没开直接返回
            if (!isOpening("")) {
                return;
            }
            //通过赛事类型和排名找到对应配置
            ConfigIterator<SeasonGradePointCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SeasonGradePointCfg.class);
            for(SeasonGradePointCfg cfg : iterator){
                if(cfg.getMatch() != matchType.getNumber()){
                    continue;
                }
                if(rank < cfg.getMin() || rank > cfg.getMax()){
                    continue;
                }
                //增加联盟段位经验
                addGuildGradeExp(guildId, cfg.getPoint());
                logger.info("SeasonActivity addGuildGradeExpFromMatchRank, matchType: {}, guildId:{},rank:{}", matchType, guildId, rank);
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    /**
     * 通过排名增加联盟段位奖励
     * @param matchType 赛事类型
     * @param guildId 联盟id
     * @param rank 联盟排名
     */
    public void addGuildGradeExpFromMatchRank(Activity.SeasonMatchType matchType, int zone,String guildId, int rank){
        try {
            //如果活动没开直接返回
            if (!isOpening("")) {
                return;
            }
            //通过赛事类型和排名找到对应配置
            ConfigIterator<SeasonGradePointCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SeasonGradePointCfg.class);
            for(SeasonGradePointCfg cfg : iterator){
                if(cfg.getMatch() != matchType.getNumber()){
                    continue;
                }
                if(cfg.getZone() != zone){
                    continue;
                }
                if(rank < cfg.getMin() || rank > cfg.getMax()){
                    continue;
                }
                //增加联盟段位经验
                addGuildGradeExp(guildId, cfg.getPoint());
                logger.info("SeasonActivity addGuildGradeExpFromMatchRank, matchType: {}, guildId:{},rank:{}", matchType, guildId, rank);
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }
    
    
    /**
     * 验证赛事的期数  是否可以加活动积分
     * --addGuildGradeExpFromMatchRank  这个方法调用的地方比较多
     * --所以先 在大帝战 处 单点使用
     * --后期如果有需要  就放进 addGuildGradeExpFromMatchRank  这个方法中使用
     * @param matchType
     * @param seasonTerm
     * @return
     */
    public boolean matchSeasonVerify(Activity.SeasonMatchType matchType, int seasonTerm){
    	 //如果活动没开直接返回
        if (!isOpening("")) {
            return false;
        }
        int termId = this.getActivityTermId();
        SeasonOpenTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(SeasonOpenTimeCfg.class, termId);
        if(Objects.isNull(timeCfg)){
        	 return false;
        }
        int matchTerm = timeCfg.getMatchTermId(matchType.getNumber());
        if(matchTerm != seasonTerm){
        	 return false;
        }
        return true;
    }
    
    
    /**
     * 增加联盟段位经验
     * @param guildId
     * @param addExp
     */
    public void addGuildGradeExp(String guildId, int addExp){
        //获得联盟段位数据
        SeasonActivityGuildGradeData entity = getGuildDataEntity(guildId);
        //避免多线程问题，可以不加，从需求上来看没有多线程问题，但是我不加难受
        synchronized (entity){
            //增加经验
            entity.addExp(addExp);
        }
        //如果当前段位为王者段位，则进入排行榜
        if(entity.getLevel()>=7){
            //获得排行榜管理器
            GuildSeasonKingGradeRankProvider provider = getRankProvider();
            //构建排行数据
            GuildSeasonKingGradeRank rank = new GuildSeasonKingGradeRank();
            //设置联盟id
            rank.setId(guildId);
            //设置积分
            rank.setScore(entity.getExp());
            //进入排行
            provider.insertIntoRank(rank);
            //更新排行榜联盟信息
            GuildSeasonKingGradeInfo info = new GuildSeasonKingGradeInfo();
            //设置联盟id
            info.setGuildId(guildId);
            //设置联盟名字
            info.setGuildName(getDataGeter().getGuildName(guildId));
            //设置联盟简称
            info.setGuildTag(getDataGeter().getGuildTag(guildId));
            //设置联盟旗帜
            info.setGuildFlag(getDataGeter().getGuildFlag(guildId));
            //设置盟主名字
            info.setGuildLeader(getDataGeter().getGuildLeaderName(guildId));
            //设置联盟服务器id
            info.setServerId(getDataGeter().getServerId());
            //更新联盟信息
            getRankProvider().updataGuildInfo(guildId, info);
        }
        Collection<String> guildMemberIds = getDataGeter().getGuildMemberIds(guildId);
        for(String playerId : guildMemberIds){
            callBack(playerId, GameConst.MsgId.SEASON_ACTIVITY_SYNC, () -> {
                syncActivityDataInfo(playerId);
            });
        }
    }

    /**
     * 联盟退出
     *
     * @param event
     */
    @Subscribe
    public void onGuildQuite(GuildQuiteEvent event) {
        if (!isOpening(event.getPlayerId())) {
            return;
        }
        syncActivityDataInfo(event.getPlayerId());
    }

    /**
     * 联盟解散
     * @param event
     */
    @Subscribe
    public void onGuildDismiss(GuildDismissEvent event){
        if (!isOpening("")) {
            return;
        }
        Collection<String> guildMemberIds = getDataGeter().getGuildMemberIds(event.getGuildId());
        for(String playerId : guildMemberIds){
            callBack(playerId, GameConst.MsgId.SEASON_ACTIVITY_SYNC, () -> {
                syncActivityDataInfo(playerId);
            });
        }
    }


    /**
     * 获得排行数据redis主键
     * @return 排行数据redis主键
     */
    public String getRankKey() {
        return SEASON_GUILD_GRADE_KING_RANK + ":" + getActivityTermId();
    }

    public int getTblyRank(String guildId){
        try {
            SeasonOpenTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(SeasonOpenTimeCfg.class, getActivityTermId());
            //根据赛事类型获得赛事期数
            int matchTermId = timeCfg.getMatchTermId(Activity.SeasonMatchType.S_TBLY.getNumber());
            String rankStr = ActivityGlobalRedis.getInstance().hget("tlw_guild_total_rank:"+matchTermId, guildId);
            if(HawkOSOperator.isEmptyString(rankStr)){
                return 999;
            }
            return Integer.parseInt(rankStr);
        }catch (Exception e){
            HawkException.catchException(e);
            return 999;
        }
    }

    /**
     * 获得联盟段位数据库实体
     * @param guildId 联盟id
     * @return 联盟段位数据库实体
     */
    public SeasonActivityGuildGradeData getGuildDataEntity(String guildId) {
        //如果内存里面有直接获得数据
        if(guildGradeEntityMap.containsKey(guildId)){
            return guildGradeEntityMap.get(guildId);
        }
        //获得期数
        int termId = getActivityTermId();
        SeasonActivityGuildGradeData data = SeasonActivityGuildGradeData.loadByGuildId(termId, guildId);
        if(data == null){
            //联盟段位数据库实体
            SeasonActivityGuildGradeEntity entity;
            //根据联盟id和活动期数去数据库里面查找数据
            List<SeasonActivityGuildGradeEntity> queryList = HawkDBManager.getInstance()
                    .query("from SeasonActivityGuildGradeEntity where guildId = ? and termId = ? and invalid = 0", guildId, termId);
            //如果数据库里面没有就新建数据
            if (queryList == null || queryList.size() <= 0) {
                data = new SeasonActivityGuildGradeData(guildId, termId);
            }else {
                //获得查询结果的第一个
                entity = queryList.get(0);
                data = new SeasonActivityGuildGradeData(entity);
            }
            //放到map里，避免多线程问题用putIfAbsent
            SeasonActivityGuildGradeData tmp = guildGradeEntityMap.putIfAbsent(guildId, data);
            if(tmp == null){
                data.saveRedis();
            }
        }else {
            guildGradeEntityMap.putIfAbsent(guildId, data);
        }
        //返回联盟对应段位信息
        return guildGradeEntityMap.get(guildId);
    }

    /**
     * 获得王者排行榜管理器
     * @return 王者排行榜管理器
     */
    public GuildSeasonKingGradeRankProvider getRankProvider(){
        return (GuildSeasonKingGradeRankProvider) ActivityRankContext.getRankProvider(ActivityRankType.GUILD_SEASON_KING_GRADE_RANK, GuildSeasonKingGradeRank.class);
    }

    /**
     * 给前端推送排名数据
     * @param playerId
     */
    public void pushRankInfo(String playerId){
        //获得王者排行榜管理器
        GuildSeasonKingGradeRankProvider provider = getRankProvider();
        //构造排行数据
        Activity.SeasonGuildKingRankResp.Builder builder = Activity.SeasonGuildKingRankResp.newBuilder();
        //遍历排行榜
        for(GuildSeasonKingGradeRank rank : provider.getRankList()){
            //构建排行信息
            Activity.SeasonGuildKingRankMsg.Builder rankBuilder = Activity.SeasonGuildKingRankMsg.newBuilder();
            //排名
            rankBuilder.setRank(rank.getRank());
            //分数
            rankBuilder.setScore(rank.getScore());
            //获得联盟信息
            GuildSeasonKingGradeInfo info = provider.getGuildInfo(rank.getId());
            //联盟名字
            rankBuilder.setGuildName(info.getGuildName());
            //联盟旗帜
            rankBuilder.setGuildFlag(info.getGuildFlag());
            //盟主名字
            rankBuilder.setGuildLeader(info.getGuildLeader());
            //联盟简称
            rankBuilder.setGuildTag(info.getGuildTag());
            //服务器id
            rankBuilder.setServerId(info.getServerId());
            //添加信息
            builder.addRankInfo(rankBuilder);
        }
        //构建本盟信息
        String guildId = getDataGeter().getGuildId(playerId);
        //如果为空返回未上榜
        if(HawkOSOperator.isEmptyString(guildId)){
            //rank为-1是未上榜
            builder.setMyGuildRank(-1);
            builder.setMyGuildScore(0);
            builder.setMyGuildName("");
            builder.setMyGuildFlag(0);
            builder.setMyGuildLeader("");
            builder.setGuildTag("");
            builder.setServerId(getDataGeter().getServerId());
        }else {
            GuildSeasonKingGradeRank rank = provider.getRank(guildId);
            builder.setMyGuildRank(rank.getRank());
            builder.setMyGuildScore(rank.getScore());
            builder.setMyGuildName(getDataGeter().getGuildName(guildId));
            builder.setMyGuildFlag(getDataGeter().getGuildFlag(guildId));
            builder.setMyGuildLeader(getDataGeter().getGuildLeaderName(guildId));
            builder.setGuildTag(getDataGeter().getGuildTag(guildId));
            builder.setServerId(getDataGeter().getServerId());
        }
        //给前端推送数据
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.SEASON_GUILD_KING_RANK_RESP, builder));
    }

    //处理结束逻辑
    @Override
    public void onEnd() {
        //获得联盟王者段位排行榜管理器
        GuildSeasonKingGradeRankProvider provider = getRankProvider();
        //获得排行榜
        List<GuildSeasonKingGradeRank> rankList = provider.getRankList();
        //构造排行map 主键是联盟id 值是联盟排行数据
        Map<String, GuildSeasonKingGradeRank> rankMap = new HashMap<>();
        if(rankList!=null && rankList.size()>0){
            for (GuildSeasonKingGradeRank rank : rankList){
                rankMap.put(rank.getId(), rank);
            }
        }
        //获得当前期数
        int termId = getActivityTermId();
        logger.info("SeasonActivity sendSeasonReward start, termId: {}", termId);
        for(String guildId : getDataGeter().getGuildIds()){
            try {
                if (!getDataGeter().isGuildLocalExist(guildId)){
                    continue;
                }
                logger.info("SeasonActivity sendGuildReward start, termId:{}, guildId:{}", termId, guildId);
                SeasonActivityGuildGradeData data =  SeasonActivityGuildGradeData.loadByGuildId(termId, guildId);
                if(data == null){
                    logger.info("SeasonActivity sendGuildReward data is null, termId:{}, guildId:{}", termId, guildId);
                    continue;
                }
                if(data.isReward()){
                    logger.info("SeasonActivity sendGuildReward has rewarded, termId:{}, guildId:{}", termId, guildId);
                    continue;
                }
                data.setReward(true);
                data.saveRedis();
                logger.info("SeasonActivity sendGuildReward real start, termId:{}, guildId:{}", termId, guildId);
                //获得成员id
                Collection<String> guildMemberIds = getDataGeter().getGuildMemberIds(guildId);
                //获得段位
                int grade = data.getLevel();
                logger.info("SeasonActivity sendGuildReward grade start, termId:{}, guildId:{}, grade:{}", termId, guildId, grade);
                //段位配置
                SeasonGradeLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SeasonGradeLevelCfg.class, grade);
                //如果配置不为空发段位奖励
                if(cfg != null){
                    logger.info("SeasonActivity sendGuildReward grade toMember, termId:{}, guildId:{}, grade:{}", termId, guildId, grade);
                    for(String playerId : guildMemberIds){
                        logger.info("SeasonActivity sendGuildReward grade toMember start, termId:{}, guildId:{}, playerId:{}, grade:{}", termId, guildId, playerId, grade);
                        getDataGeter().sendMail(playerId, MailConst.MailId.SEASON_ACTIVITY_GRADE, null, null,new Object[] { grade },cfg.getNormalRewardList(),false);
                        logger.info("SeasonActivity sendGuildReward grade toMember end, termId:{}, guildId:{}, playerId:{}, grade:{}", termId, guildId, playerId, grade);
                    }
                }
                logger.info("SeasonActivity sendGuildReward grade end, termId:{}, guildId:{}, grade:{}", termId, guildId, grade);
                //获得联盟王者排名
                int guildRank = -1;
                if(rankMap.containsKey(guildId)){
                    GuildSeasonKingGradeRank rank = rankMap.get(guildId);
                    guildRank = rank.getRank();
                }
                logger.info("SeasonActivity sendGuildReward rank start, termId:{}, guildId:{}, rank:{}", termId, guildId, guildRank);
                //如果有排名则发排名奖励
                if(guildRank!=-1){
                    SeasonKingAwardCfg kingAwardCfg = getSeasonKingAwardCfg(guildRank);
                    logger.info("SeasonActivity sendGuildReward rank toMember, termId:{}, guildId:{}, rank:{}, cfgId:{}", termId, guildId, guildRank, kingAwardCfg.getId());
                    for(String playerId : guildMemberIds){
                        logger.info("SeasonActivity sendGuildReward rank toMember start, termId:{}, guildId:{}, playerId:{}, rank:{}, cfgId:{}", termId, guildId, playerId, guildRank, kingAwardCfg.getId());
                        getDataGeter().sendMail(playerId, MailConst.MailId.SEASON_ACTIVITY_KING_RANK, null, null,new Object[] { guildRank },kingAwardCfg.getRewardList(),false);
                        logger.info("SeasonActivity sendGuildReward rank toMember end, termId:{}, guildId:{}, playerId:{}, rank:{}, cfgId:{}", termId, guildId, playerId, guildRank, kingAwardCfg.getId());
                    }
                }
                logger.info("SeasonActivity sendGuildReward rank end, termId:{}, guildId:{}, rank:{}", termId, guildId, guildRank);
                logger.info("SeasonActivity sendGuildReward real end, termId:{}, guildId:{}", termId, guildId);
            }catch (Exception e){
                logger.info("SeasonActivity sendGuildReward error, termId:{}, guildId:{}", termId, guildId);
                HawkException.catchException(e);
            }
        }
    }

    private SeasonKingAwardCfg getSeasonKingAwardCfg(int rank){
        ConfigIterator<SeasonKingAwardCfg>  iterator = HawkConfigManager.getInstance().getConfigIterator(SeasonKingAwardCfg.class);
        for(SeasonKingAwardCfg kingAwardCfg : iterator){
            if(rank < kingAwardCfg.getRankUpper() || rank > kingAwardCfg.getRankLower()){
                continue;
            }
            return kingAwardCfg;
        }
        return null;
    }

    @Override
    public void onHidden() {
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for(String playerId : onlinePlayerIds){
            callBack(playerId, GameConst.MsgId.ON_SEASON_ACTIVITY_END, () -> {
                recoverItem(playerId);
                getDataGeter().xqhxTalentCheck(playerId);
            });
        }
    }

    /**
     * 前端记录等级
     * @param playerId 玩家id
     * @param level 等级
     * @return 执行结果
     */
    public Result<Integer> clientLevelupdate(String playerId, int level){
        if (!isShow(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //如果数据为空，返回错误码
        Optional<SeasonActivityEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得活动数据
        SeasonActivityEntity entity = optional.get();
        entity.setClientLevel(level);
        syncActivityOrderBaseInfo(entity);
        return Result.success();
    }
}
