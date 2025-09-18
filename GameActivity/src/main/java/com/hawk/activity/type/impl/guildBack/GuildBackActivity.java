package com.hawk.activity.type.impl.guildBack;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.*;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.msg.PlayerRewardFromActivityMsg;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.backFlow.backGift.BackGiftActivity;
import com.hawk.activity.type.impl.backFlow.chemistry.ChemistryActivity;
import com.hawk.activity.type.impl.backFlow.chemistry.entity.ChemistryEntity;
import com.hawk.activity.type.impl.backFlow.developSput.DevelopSpurtActivity;
import com.hawk.activity.type.impl.exchangeTip.IExchangeTip;
import com.hawk.activity.type.impl.guildBack.action.GuildBackAction;
import com.hawk.activity.type.impl.guildBack.action.GuildBackActionEnum;
import com.hawk.activity.type.impl.guildBack.cfg.*;
import com.hawk.activity.type.impl.guildBack.entity.GuildBackEntity;
import com.hawk.game.protocol.*;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import com.hawk.log.LogConst;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * activityId:359
 * 联盟回流活动-联合作战
 */
public class GuildBackActivity extends ActivityBase implements AchieveProvider, IExchangeTip<GuildBackShopCfg> {
    //日志
    private static final Logger logger = LoggerFactory.getLogger("Server");
    //是否初始化
    private boolean isInit = false;
    //联盟体力池
    private Map<String, Double> guildVitPoolMap = new ConcurrentHashMap<>();
    //联盟金币池
    private Map<String, Double> guildGoldPoolMap = new ConcurrentHashMap<>();
    //联盟回流玩家消耗金条总量，用于联盟金条成就计算
    private Map<String, Double> guildDiamondCostMap = new ConcurrentHashMap<>();
    //联盟回流玩家消耗体力总量，用于联盟体力成就计算
    private Map<String, Double> guildVitCostMap = new ConcurrentHashMap<>();
    //联盟体力池注入总量，用于限制联盟体力奖池注入
    private Map<String, Double> guildVitLimitMap = new ConcurrentHashMap<>();
    //联盟金币池注入总量，用于限制联盟金币奖池注入
    private Map<String, Double> guildGoldLimitMap = new ConcurrentHashMap<>();
    //回归玩家数据
    private Map<String, Activity.GuildBackInvitePlayer.Builder> backPlayerMap = new ConcurrentHashMap<>();
    //玩家个人邀请列表
    private final Map<String, Map<String, Activity.GuildBackInvitePlayer.Builder>> inviteBackPlayerMap = new ConcurrentHashMap<>();
    //回流人数缓存
    private final Map<String, Integer> guildBackCountMap = new ConcurrentHashMap<>();
    //记录回流人数时间缓存
    private final Map<String, Long> guildBackCountTimeMap = new ConcurrentHashMap<>();
    //联盟相关操作队列，保证线程安全
    private final ConcurrentLinkedQueue<GuildBackAction> queue = new ConcurrentLinkedQueue<>();

    /**
     * 构造函数
     * @param activityId 活动id
     * @param activityEntity 活动数据库实体
     */
    public GuildBackActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    /**
     * 获取活动类型
     * @return 活动类型
     */
    @Override
    public ActivityType getActivityType() {
        return ActivityType.GUILD_BACK;
    }

    /**
     * 活动实例
     * @param config 活动配置
     * @param activityEntity 活动数据库实体
     * @return 活动实例
     */
    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        GuildBackActivity activity = new GuildBackActivity(config.getActivityId(), activityEntity);
        //注册活动
        AchieveContext.registeProvider(activity);
        //返回活动实例
        return activity;
    }

    /**
     * 加载玩家数据库数据
     * @param playerId 玩家id
     * @param termId 活动期数
     * @return 玩家数据库实体
     */
    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        //根据条件从数据库中检索
        List<GuildBackEntity> queryList = HawkDBManager.getInstance()
                .query("from GuildBackEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        //如果有数据的话，返回第一个数据
        if (queryList != null && !queryList.isEmpty()) {
            return queryList.get(0);
        }
        return null;
    }

    /**
     * 创建玩家数据库实体
     * @param playerId 玩家id
     * @param termId 活动期数
     * @return 玩家数据实体
     */
    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        return new GuildBackEntity(playerId, termId);
    }

    /**
     * 活动开始
     */
    @Override
    public void onOpen() {
        //获得所有在线玩家
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        //遍历在线玩家，进行时间投递
        for(String playerId : onlinePlayerIds) {
            callBack(playerId, GameConst.MsgId.ON_GUILD_BACK_SYNC, () -> {
                //是否回流玩家
                checkBackPlayer(playerId);
                //是否增加联盟宝箱次数
                checkPlayerAddGuildBox(playerId);
                //推送活动信息
                syncActivityDataInfo(playerId);
                //初始化成就
                resetAchieve(playerId);
            });
        }
    }

    /**
     * 活动结束
     */
    @Override
    public void onEnd() {
        //清空联盟体力池
        guildVitPoolMap.clear();
        //清空联盟金币池
        guildGoldPoolMap.clear();
        guildVitCostMap.clear();
        guildDiamondCostMap.clear();
        guildVitLimitMap.clear();
        guildGoldLimitMap.clear();
        //清空回流玩家数据
        backPlayerMap.clear();
        //清空玩家邀请数据
        inviteBackPlayerMap.clear();
        //清空联盟回流个数缓存
        guildBackCountMap.clear();
        //清空联盟回流个数时间缓存
        guildBackCountTimeMap.clear();
        getDataGeter().cleanGuildEffect(getActivityId());
    }

    /**
     * 玩家登陆
     * @param playerId 玩家id
     */
    @Override
    public void onPlayerLogin(String playerId) {
        if(!isOpening(playerId)){
            return;
        }
        //检查是否是回流玩家
        checkBackPlayer(playerId);
        //检查是否增加联盟宝箱
        checkPlayerAddGuildBox(playerId);
    }

    /**
     * 同步活动数据
     * @param playerId 玩家id
     */
    @Override
    public void syncActivityDataInfo(String playerId) {
        //玩家活动数据
        Optional<GuildBackEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        GuildBackEntity entity = opEntity.get();
        //构造前端数据
        Activity.GuildBackInfoResp.Builder resp = Activity.GuildBackInfoResp.newBuilder();
        //商店兑换数据
        getShopItem(resp, entity);
        //兑换红点数据
        getTips(resp, entity);
        //可打开联盟宝箱次数
        resp.setBoxCount(entity.getGetBox() - entity.getUseBox());
        //联盟id
        String guildId = getDataGeter().getGuildId(playerId);
        //如果没有联盟没有数据
        if (HawkOSOperator.isEmptyString(guildId)) {
            resp.setBoxCount(0);
            resp.setGoldPool(0L);
            resp.setVitPool(0L);
            resp.setJoinTime(0L);

        }else {
            GuildBackKvCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(GuildBackKvCfg.class);
            //回流人数
            resp.setBackCount(getBackPlayerCount(guildId));
            //金币池数量
            double goldPool = guildGoldPoolMap.getOrDefault(guildId, kvCfg.getStartCoinsValue());
            resp.setGoldPool((long) goldPool);
            //体力池数量
            double vitPool = guildVitPoolMap.getOrDefault(guildId, kvCfg.getStartVitValue());
            resp.setVitPool((long) vitPool);
            resp.setJoinTime(getDataGeter().getJoinGuildTime(playerId));
        }
        //发送给前端
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.GUILD_BACK_INFO_RESP, resp));
    }

    /**
     * 构造商店前端数据
     * @param resp 前端返回数据
     * @param entity 玩家活动数据
     */
    public void getShopItem(Activity.GuildBackInfoResp.Builder resp, GuildBackEntity entity){
        for(Map.Entry<Integer, Integer> entry : entity.getBuyNumMap().entrySet()){
            Activity.GuildBackShopItem.Builder shopItem = Activity.GuildBackShopItem.newBuilder();
            //配置id
            shopItem.setGoodsId(entry.getKey());
            //已兑换数量
            shopItem.setExhangeTimes(entry.getValue());
            resp.addShopItems(shopItem);
        }
    }

    /**
     * 兑换提醒红点
     * @param resp 前端返回数据
     * @param entity 玩家活动数据
     */
    public void getTips(Activity.GuildBackInfoResp.Builder resp, GuildBackEntity entity){
        resp.addAllTips(getTips(GuildBackShopCfg.class, entity.getTipSet()));
    }

    /**
     * 玩家线程tick
     * 目前十分钟tick一次
     * @param playerId
     */
    @Override
    public void onPlayerTick(String playerId) {
        if(!isOpening(playerId)){
            return;
        }
        //检查是否增加联盟宝箱数量
        checkPlayerAddGuildBox(playerId);
    }

    /**
     * 活动线程tick
     * 目前十秒tick一次
     */
    @Override
    public void onTick() {
        //活动初始化
        init();
    }

    /**
     * 活动初始化
     */
    public void init() {
        //如果已经初始化过直接返回
        if (isInit) {
            return;
        }
        //标记已经初始化
        isInit = true;
        //加载已经触发回流的玩家
        loadBackPlayer();
        loadGuildDatas();
        loadGuildEffects();
    }

    /**
     * 玩家快速线程tick
     * 目前200ms tick一次
     */
    @Override
    public void onQuickTick() {
        //如果队列不为空
        while (!queue.isEmpty()){
            //获取操作
            GuildBackAction action = queue.poll();
            try {
                //执行逻辑
                onAction(action);
            }catch (Exception e){
                HawkException.catchException(e);
            }

        }
    }

    /**
     * 操作逻辑
     * @param action 操作
     */
    public void onAction(GuildBackAction action){
        logger.info("GuildBackActivity onAction {}", action.toString());
        switch (action.getOpt()){
            case VIT_ADD:{//体力池增加
                double newCost = 0;
                if(action.isBack()){
                    double cost = guildVitCostMap.getOrDefault(action.getGuildId(), 0d);
                    guildVitCostMap.put(action.getGuildId(), cost + action.getCount());
                    newCost = guildVitCostMap.getOrDefault(action.getGuildId(), 0d);
                    ActivityGlobalRedis.getInstance().getRedisSession().hSet(getVitCostKey(), action.getGuildId(), String.valueOf(newCost));
                }
                GuildBackKvCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(GuildBackKvCfg.class);
                double limit = guildVitLimitMap.getOrDefault(action.getGuildId(), 0d);
                boolean isUpdate = false;
                if(action.getBaseLv() >= kvCfg.getBaseLevel() && limit < kvCfg.getVitMax()){
                    GuildBackIdentityCfg identityCfg = getIdentityCfg(action.isBack());
                    double add = roundToTwoDecimalPlaces(action.getCount() * identityCfg.getVitRatio() / 10000d);
                    double newLimit = limit + add;
                    if(newLimit > kvCfg.getVitMax()){
                        newLimit = kvCfg.getVitMax();
                        add = newLimit - limit;
                    }
                    guildVitLimitMap.put(action.getGuildId(), newLimit);
                    ActivityGlobalRedis.getInstance().getRedisSession().hSet(getVitLimitKey(), action.getGuildId(), String.valueOf(newLimit));
                    double count = guildVitPoolMap.getOrDefault(action.getGuildId(), kvCfg.getStartVitValue());
                    double newCount = count + add;
                    guildVitPoolMap.put(action.getGuildId(), newCount);
                    ActivityGlobalRedis.getInstance().getRedisSession().hSet(getVitPoolKey(), action.getGuildId(), String.valueOf(newCount));
                    isUpdate = true;
                }
                //活动联盟成员
                Collection<String> memberIds = getDataGeter().getGuildMemberIds(action.getGuildId());
                //遍历联盟成员
                for(String memberId : memberIds){
                    if(newCost > 0){
                        ActivityManager.getInstance().postEvent(new GuildBackVitCostEvent(memberId, (int)newCost));
                    }
                    if(isUpdate){
                        callBack(memberId, GameConst.MsgId.ON_GUILD_BACK_SYNC, () -> {
                            syncActivityDataInfo(memberId);
                        });
                    }
                }
            }
            break;
            case GOLD_ADD:{//金币池增加
                GuildBackKvCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(GuildBackKvCfg.class);
                double limit = guildGoldLimitMap.getOrDefault(action.getGuildId(), 0d);
                if(action.getBaseLv() >= kvCfg.getBaseLevel() && limit < kvCfg.getCoinMax()){
                    GuildBackIdentityCfg identityCfg = getIdentityCfg(action.isBack());
                    double add = roundToTwoDecimalPlaces(action.getCount() * identityCfg.getGoldRatio() / 10000d);
                    double newLimit = limit + add;
                    if(newLimit > kvCfg.getCoinMax()){
                        newLimit = kvCfg.getCoinMax();
                        add = newLimit - limit;
                    }
                    guildGoldLimitMap.put(action.getGuildId(), newLimit);
                    ActivityGlobalRedis.getInstance().getRedisSession().hSet(getGoldLimitKey(), action.getGuildId(), String.valueOf(newLimit));
                    double count = guildGoldPoolMap.getOrDefault(action.getGuildId(), kvCfg.getStartCoinsValue());
                    double newCount = count + add;
                    guildGoldPoolMap.put(action.getGuildId(), newCount);
                    ActivityGlobalRedis.getInstance().getRedisSession().hSet(getGoldPoolKey(), action.getGuildId(), String.valueOf(newCount));
                    Collection<String> memberIds = getDataGeter().getGuildMemberIds(action.getGuildId());
                    //遍历联盟成员
                    for(String memberId : memberIds){
                        callBack(memberId, GameConst.MsgId.ON_GUILD_BACK_SYNC, () -> {
                            syncActivityDataInfo(memberId);
                        });
                    }
                }

            }
            break;
            case DIAMOND_ADD:{
                if(action.isBack()){
                    double cost = guildDiamondCostMap.getOrDefault(action.getGuildId(), 0d);
                    guildDiamondCostMap.put(action.getGuildId(), cost + action.getCount());
                    double newCost = guildDiamondCostMap.getOrDefault(action.getGuildId(), 0d);
                    ActivityGlobalRedis.getInstance().getRedisSession().hSet(getDiamondCostKey(), action.getGuildId(), String.valueOf(newCost));
                    //活动联盟成员
                    Collection<String> memberIds = getDataGeter().getGuildMemberIds(action.getGuildId());
                    //遍历联盟成员
                    for(String memberId : memberIds){
                        ActivityManager.getInstance().postEvent(new GuildBackGoldCostEvent(memberId, (int)newCost));
                    }
                }
            }
            break;
            case VIT_PARTITION:{//瓜分体力池
                GuildBackKvCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(GuildBackKvCfg.class);
                GuildBackPartitionCfg cfg = getPartitionCfg(2);
                double count = guildVitPoolMap.getOrDefault(action.getGuildId(), kvCfg.getStartVitValue());
                long getCount = (long) Math.ceil(((int)count) * cfg.getRatio() / 10000);
                double newCount = count - getCount;
                guildVitPoolMap.put(action.getGuildId(), newCount);
                ActivityGlobalRedis.getInstance().getRedisSession().hSet(getVitPoolKey(), action.getGuildId(), String.valueOf(newCount));
                String playerId = action.getPlayerId();
                HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, playerId);
                List<Reward.RewardItem.Builder> rewardList = RewardHelper.toRewardItemImmutableList(cfg.getRewards());
                RewardHelper.multiCeilItemList(rewardList, getCount);
                PlayerRewardFromActivityMsg msg = PlayerRewardFromActivityMsg.valueOf(rewardList, Action.GUILD_BACK_VIT_POOL_GET, true);
                HawkTaskManager.getInstance().postMsg(xid, msg);
                callBack(playerId, GameConst.MsgId.ON_GUILD_BACK_SYNC, () -> {
                    syncActivityDataInfo(playerId);
                    Optional<GuildBackEntity> opEntity = getPlayerDataEntity(playerId);
                    //如果数据为空直接返回
                    if (!opEntity.isPresent()) {
                        return;
                    }
                    GuildBackEntity entity = opEntity.get();
                    entity.setVitNum(entity.getVitNum() + (int)getCount);
                });
                //活动联盟成员
                Collection<String> memberIds = getDataGeter().getGuildMemberIds(action.getGuildId());
                //遍历联盟成员
                for(String memberId : memberIds){
                    if(memberId.equals(playerId)){
                        continue;
                    }
                    callBack(memberId, GameConst.MsgId.ON_GUILD_BACK_SYNC, () -> {
                        syncActivityDataInfo(memberId);
                    });
                }
            }
            break;
            case GOLD_PARTITION:{//瓜分金币池
                GuildBackKvCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(GuildBackKvCfg.class);
                GuildBackPartitionCfg cfg = getPartitionCfg(1);
                double count = guildGoldPoolMap.getOrDefault(action.getGuildId(), kvCfg.getStartCoinsValue());
                long getCount = (long) Math.ceil(((int)count) * cfg.getRatio() / 10000);
                double newCount = count - getCount;
                guildGoldPoolMap.put(action.getGuildId(), newCount);
                ActivityGlobalRedis.getInstance().getRedisSession().hSet(getGoldPoolKey(), action.getGuildId(), String.valueOf(newCount));
                String playerId = action.getPlayerId();
                HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, playerId);
                List<Reward.RewardItem.Builder> rewardList = RewardHelper.toRewardItemImmutableList(cfg.getRewards());
                RewardHelper.multiCeilItemList(rewardList, getCount);
                PlayerRewardFromActivityMsg msg = PlayerRewardFromActivityMsg.valueOf(rewardList, Action.GUILD_BACK_GOLD_POOL_GET, true);
                HawkTaskManager.getInstance().postMsg(xid, msg);
                callBack(playerId, GameConst.MsgId.ON_GUILD_BACK_SYNC, () -> {
                    syncActivityDataInfo(playerId);
                    Optional<GuildBackEntity> opEntity = getPlayerDataEntity(playerId);
                    //如果数据为空直接返回
                    if (!opEntity.isPresent()) {
                        return;
                    }
                    GuildBackEntity entity = opEntity.get();
                    entity.setGoldNum(entity.getGoldNum() + (int)getCount);
                });
                //活动联盟成员
                Collection<String> memberIds = getDataGeter().getGuildMemberIds(action.getGuildId());
                //遍历联盟成员
                for(String memberId : memberIds){
                    if(memberId.equals(playerId)){
                        continue;
                    }
                    callBack(memberId, GameConst.MsgId.ON_GUILD_BACK_SYNC, () -> {
                        syncActivityDataInfo(memberId);
                    });
                }
            }
            break;
            case GUILD_DISMISS:{//联盟解散
                //删除联盟体力池
                guildVitPoolMap.remove(action.getGuildId());
                //删除联盟金币池
                guildGoldPoolMap.remove(action.getGuildId());
                //删除联盟回流人数缓存
                guildBackCountMap.remove(action.getGuildId());
                //删除联盟回流人数时间缓存
                guildBackCountTimeMap.remove(action.getGuildId());
            }
            break;
            case ADD_GUILD_BOX:{//增加联盟宝箱数量
                //活动联盟成员
                Collection<String> memberIds = getDataGeter().getGuildMemberIds(action.getGuildId());
                //遍历联盟成员
                for(String memberId : memberIds){
                    //给每个人投递联盟宝箱增加事件
                    ActivityManager.getInstance().postEvent(new GuildBackBoxAddEvent(memberId));
                }
            }
            break;
        }
    }

    /**
     * 将输入的 double 值保留两位小数并返回
     *
     * @param number 需要保留两位小数的数字
     * @return 保留两位小数的 double 值
     */
    public double roundToTwoDecimalPlaces(double number) {
        BigDecimal bd = new BigDecimal(number);
        bd = bd.setScale(3, RoundingMode.HALF_UP);  // 四舍五入保留两位小数
        return bd.doubleValue();  // 返回 double 类型的结果
    }

    /**
     * 获得联盟回流人数
     * @param guildId 联盟id
     * @return 回流人数
     */
    public int getBackPlayerCount(String guildId){
        //当前数据
        long now = HawkTime.getMillisecond();
        //没有数据或者或者缓存数据超过十秒开始重新计算人数
        if(!guildBackCountMap.containsKey(guildId)
                || now - guildBackCountTimeMap.getOrDefault(guildId, 0L) > TimeUnit.SECONDS.toMillis(10)){
            //计算回流人数
            int count = calBackPlayerCount(guildId);
            //缓存回流人数避免瞬间多次计算
            Integer oldCountObj = guildBackCountMap.put(guildId, count);
            int oldCount = oldCountObj == null ? 0 : oldCountObj;
            //缓存时间用于刷新，其实可以不用时间刷新，这是一个保护机制，免得有意外
            guildBackCountTimeMap.put(guildId, now);
            if(count != oldCount){
                Map<String, Object> param = new HashMap<>();
                param.put("guildId", guildId);
                param.put("count", count);
                getDataGeter().logActivityCommon(LogConst.LogInfoType.guild_back_buff, param);
                Collection<String> memberIds = getDataGeter().getGuildMemberIds(guildId);
                GuildBackBuffCfg oldBuffCfg = getBuffCfg(oldCount);
                GuildBackBuffCfg newBuffCfg = getBuffCfg(count);
                Set<Integer> effIds = new HashSet<>();
                if(oldBuffCfg != null){
                    effIds.addAll(oldBuffCfg.getEffMap().keySet());
                }
                if(newBuffCfg != null){
                    effIds.addAll(newBuffCfg.getEffMap().keySet());
                    Map<Integer, Integer> effMap = new HashMap<>(newBuffCfg.getEffMap());
                    getDataGeter().updateGuildEffect(getActivityId(), guildId, effMap);
                }
                for(String memberId : memberIds){
                    callBack(memberId, GameConst.MsgId.ON_GUILD_BACK_SYNC, () -> {
                        getDataGeter().syncEffect(memberId, effIds);
                    });
                }

            }

        }
        //返回人数
        return guildBackCountMap.get(guildId);
    }

    /**
     * 刷新联盟回流人数
     * @param guildId 联盟id
     */
    public void refreshBackPlayerCount(String guildId){
        refreshBackPlayerCount(guildId, false);
    }

    /**
     * 刷新联盟回流人数
     * @param guildId 联盟id
     */
    public void refreshBackPlayerCount(String guildId, boolean isInit){
        //计算人数
        int count = calBackPlayerCount(guildId);
        //当前时间
        long now = HawkTime.getMillisecond();
        //缓存回流人数避免瞬间多次计算
        Integer oldCountObj = guildBackCountMap.put(guildId, count);
        int oldCount = oldCountObj == null ? 0 : oldCountObj;
        //缓存时间用于刷新，其实可以不用时间刷新，这是一个保护机制，免得有意外
        guildBackCountTimeMap.put(guildId, now);
        if(count != oldCount){
            Map<String, Object> param = new HashMap<>();
            param.put("guildId", guildId);
            param.put("count", count);
            getDataGeter().logActivityCommon(LogConst.LogInfoType.guild_back_buff, param);
            Collection<String> memberIds = getDataGeter().getGuildMemberIds(guildId);
            GuildBackBuffCfg oldBuffCfg = getBuffCfg(oldCount);
            GuildBackBuffCfg newBuffCfg = getBuffCfg(count);
            Set<Integer> effIds = new HashSet<>();
            if(oldBuffCfg != null){
                effIds.addAll(oldBuffCfg.getEffMap().keySet());
            }
            if(newBuffCfg != null){
                effIds.addAll(newBuffCfg.getEffMap().keySet());
                Map<Integer, Integer> effMap = new HashMap<>(newBuffCfg.getEffMap());
                getDataGeter().updateGuildEffect(getActivityId(), guildId, effMap);
            }
            if(!isInit){
                for(String memberId : memberIds){
                    callBack(memberId, GameConst.MsgId.ON_GUILD_BACK_SYNC, () -> {
                        getDataGeter().syncEffect(memberId, effIds);
                    });
                }
            }
        }
    }

    /**
     * 计算联盟回流人数
     * @param guildId 联盟id
     * @return 联盟回流人数
     */
    public int calBackPlayerCount(String guildId){
        int count = 0;
        //获得联盟成员id
        Collection<String> memberIds = getDataGeter().getGuildMemberIds(guildId);
        //遍历联盟成员
        for(String memberId : memberIds){
            //是否是回流玩家
            if(isActivityBackPlayer(memberId)){
                count++;
            }
        }
        return count;
    }

    /**
     * 是否处于活动回流中
     * @param playerId 玩家id
     * @return 是否处于回流活动中
     */
    public boolean isBackPlayer(String playerId){
        //回归有礼
        Optional<BackGiftActivity> opActivity171 = ActivityManager.getInstance().getGameActivityByType(ActivityType.BACK_GIFT.intValue());
        if (opActivity171.isPresent()) {
            BackGiftActivity activity = opActivity171.get();
            //返回是否处于回流活动中
            if(activity.isOpening(playerId) && !activity.isHidden(playerId)){
                return true;
            }
        }
        //发展冲刺
        Optional<DevelopSpurtActivity> opActivity173 = ActivityManager.getInstance().getGameActivityByType(ActivityType.DEVELOP_SPURT.intValue());
        if (opActivity173.isPresent()) {
            DevelopSpurtActivity activity = opActivity173.get();
            //返回是否处于回流活动中
            if(activity.isOpening(playerId) && !activity.isHidden(playerId)){
                return true;
            }
        }
        return false;
    }

    /**
     * 是否在本活动期间触发过回流活动
     * @param playerId 玩家id
     * @return 是否在本活动期间触发过回流活动
     */
    public boolean isActivityBackPlayer(String playerId){
        return backPlayerMap.containsKey(playerId);
    }

    /**
     * 获得回流时间
     * @param playerId 玩家id
     * @return 回流时间
     */
    public long getBackTime(String playerId){
        //获得回流活动
        Optional<ChemistryActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.CHEMISTRY.intValue());
        if (opActivity.isPresent()) {
            ChemistryActivity activity = opActivity.get();
            //获得玩家活动数据
            Optional<ChemistryEntity> opEntity = activity.getPlayerDataEntity(playerId, false);
            if(opEntity.isPresent()){
                ChemistryEntity entity = opEntity.get();
                //获得回流时间
                return entity.getStartTime();
            }
        }
        return 0L;
    }

    /**
     * 检查玩家是否为回流玩家
     * @param playerId 玩家id
     */
    public void checkBackPlayer(String playerId){
        if(!backPlayerMap.containsKey(playerId) && isBackPlayer(playerId)){
            Activity.GuildBackInvitePlayer.Builder backPlayer = Activity.GuildBackInvitePlayer.newBuilder();
            //玩家id
            backPlayer.setPlayerId(playerId);
            //玩家名字
            backPlayer.setPlayeName(getDataGeter().getPlayerName(playerId));
            //玩家头像
            backPlayer.setIcon(getDataGeter().getIcon(playerId));
            //平台头像
            backPlayer.setPfIcon(getDataGeter().getPfIcon(playerId));
            //是否被邀请过，默认没有
            backPlayer.setIsInvited(false);
            //回流时间
            backPlayer.setBackTime(getBackTime(playerId));
            //缓存并存进redis
            backPlayerMap.put(playerId, backPlayer);
            String key = getBackPlayerKey();
            if(key != null){
                ActivityGlobalRedis.getInstance().getRedisSession().hSetBytes(key, playerId, backPlayer.build().toByteArray());
            }
        }
    }

    /**
     * 检查是否怎加联盟保险数量
     * @param playerId 玩家id
     */
    public void checkPlayerAddGuildBox(String playerId){
        //不是回流玩家返回
        if(!isActivityBackPlayer(playerId)){
            return;
        }
        //没有联盟返回
        String guildId = getDataGeter().getGuildId(playerId);
        if (HawkOSOperator.isEmptyString(guildId)) {
            return;
        }
        //获得玩家活动数据
        Optional<GuildBackEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        GuildBackEntity entity = opEntity.get();
        if(entity.getDayBoxTime() > 0){
            return;
        }
        //记录增加时间
        entity.setDayBoxTime(HawkTime.getMillisecond());
        //投递增加行为
        queue.add(new GuildBackAction(playerId, GuildBackActionEnum.ADD_GUILD_BOX, guildId));
    }

    /**
     * 从redis中加载回流玩家信息，起服时调用
     */
    public void loadBackPlayer(){
        List<String> mergeServerList = getDataGeter().getMergeServerList();
        if(mergeServerList != null && !mergeServerList.isEmpty()){
            Map<String, Activity.GuildBackInvitePlayer.Builder> tmp = new ConcurrentHashMap<>();
            for(String serverId : mergeServerList){
                String key = getBackPlayerKey(serverId);
                if(key == null){
                    continue;
                }
                Map<byte[], byte[]> backPlayers = ActivityGlobalRedis.getInstance().getRedisSession().hGetAllBytes(key.getBytes());
                if(backPlayers == null || backPlayers.isEmpty()){
                    continue;
                }
                for(byte[] bytes : backPlayers.values()){
                    try {
                        Activity.GuildBackInvitePlayer.Builder backPlayer = Activity.GuildBackInvitePlayer.newBuilder();
                        backPlayer.mergeFrom(bytes);
                        tmp.put(backPlayer.getPlayerId(), backPlayer);
                    }catch (Exception e){
                        HawkException.catchException(e);
                    }
                }
            }
            backPlayerMap = tmp;
        }else {
            String key = getBackPlayerKey();
            if(key == null){
                backPlayerMap = new ConcurrentHashMap<>();
                return;
            }
            Map<byte[], byte[]> backPlayers = ActivityGlobalRedis.getInstance().getRedisSession().hGetAllBytes(key.getBytes());
            if(backPlayers == null || backPlayers.isEmpty()){
                backPlayerMap = new ConcurrentHashMap<>();
                return;
            }
            Map<String, Activity.GuildBackInvitePlayer.Builder> tmp = new ConcurrentHashMap<>();
            for(byte[] bytes : backPlayers.values()){
                try {
                    Activity.GuildBackInvitePlayer.Builder backPlayer = Activity.GuildBackInvitePlayer.newBuilder();
                    backPlayer.mergeFrom(bytes);
                    tmp.put(backPlayer.getPlayerId(), backPlayer);
                }catch (Exception e){
                    HawkException.catchException(e);
                }
            }
            backPlayerMap = tmp;
        }
    }

    public void loadGuildDatas(){
        List<String> guildIdList = this.getDataGeter().getGuildIds();
        String [] guildIds = guildIdList.toArray(new String[0]);
        guildVitPoolMap = loadGuildData(getVitPoolKey(), guildIds);
        guildGoldPoolMap = loadGuildData(getGoldPoolKey(), guildIds);
        guildDiamondCostMap = loadGuildData(getDiamondCostKey(), guildIds);
        guildVitCostMap = loadGuildData(getVitCostKey(), guildIds);
        guildVitLimitMap = loadGuildData(getVitLimitKey(), guildIds);
        guildGoldLimitMap = loadGuildData(getGoldLimitKey(), guildIds);
    }

    public void loadGuildEffects(){
        List<String> guildIdList = this.getDataGeter().getGuildIds();
        for(String guildId : guildIdList){
            refreshBackPlayerCount(guildId, true);
        }
    }

    public Map<String, Double> loadGuildData(String key, String[] guildIds){
        Map<String, Double> tmp = new ConcurrentHashMap<>();
        if(key == null || guildIds == null || guildIds.length == 0){
            return tmp;
        }
        List<String> datas = ActivityGlobalRedis.getInstance().getRedisSession().hmGet(key, guildIds);
        for(int i = 0; i < guildIds.length; i++){
            try {
                if (!HawkOSOperator.isEmptyString(datas.get(i))) {
                    tmp.put(guildIds[i], Double.parseDouble(datas.get(i)));
                }
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
        return tmp;
    }

    /**
     * 增益
     * @param playerId 玩家id
     * @param effId 作用号id
     * @return 作用好值
     */
    public int getBuff(String playerId, int effId){
        if(HawkOSOperator.isEmptyString(playerId)){
            return 0;
        }
        if(!isOpening(playerId)){
            return 0;
        }
        String guildId = getDataGeter().getGuildId(playerId);
        if(HawkOSOperator.isEmptyString(guildId)){
            return 0;
        }
        int count = getBackPlayerCount(guildId);
        GuildBackBuffCfg buffCfg = getBuffCfg(count);
        if(buffCfg == null || buffCfg.getEffMap() == null){
            return 0;
        }
        return buffCfg.getEffMap().getOrDefault(effId, 0);
    }

    /**
     * 增益配置
     * @param count 数量
     * @return 增益配置
     */
    public GuildBackBuffCfg getBuffCfg(int count){
        ConfigIterator<GuildBackBuffCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(GuildBackBuffCfg.class);
        GuildBackBuffCfg buffCfg = null;
        for (GuildBackBuffCfg cfg : iterator){
            if(cfg.getCount() <= count && (buffCfg == null || cfg.getCount() > buffCfg.getCount())){
                buffCfg = cfg;
            }
        }
        return buffCfg;
    }

    public GuildBackPartitionCfg getPartitionCfg(int type){
        Map<GuildBackPartitionCfg, Integer> map = new HashMap<>();
        ConfigIterator<GuildBackPartitionCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(GuildBackPartitionCfg.class);
        for(GuildBackPartitionCfg cfg : iterator){
            if(cfg.getType() != type){
                continue;
            }
            map.put(cfg, cfg.getWeight());
        }
        return HawkRand.randomWeightObject(map);
    }

    public GuildBackIdentityCfg getIdentityCfg(boolean isBack){
        if(isBack){
            return HawkConfigManager.getInstance().getConfigByKey(GuildBackIdentityCfg.class, 1);
        }else {
            return HawkConfigManager.getInstance().getConfigByKey(GuildBackIdentityCfg.class, 2);
        }
    }

    private String getBackPlayerKey(){
        int termId = getActivityTermId();
        if(termId == 0){
            return null;
        }
        String serverId = getDataGeter().getServerId();
        return "GUILD_BACK:" + termId + ":" + serverId + ":BACK_PLAYER";
    }

    private String getBackPlayerKey(String serverId){
        int termId = getActivityTermId();
        if(termId == 0){
            return null;
        }
        return "GUILD_BACK:" + termId + ":" + serverId + ":BACK_PLAYER";
    }


    private String getVitPoolKey(){
        int termId = getActivityTermId();
        if(termId == 0){
            return null;
        }
        return "GUILD_BACK:" + termId + ":VIT_POOL";
    }

    private String getGoldPoolKey(){
        int termId = getActivityTermId();
        if(termId == 0){
            return null;
        }
        return "GUILD_BACK:" + termId + ":GOOD_POOL";
    }

    private String getDiamondCostKey(){
        int termId = getActivityTermId();
        if(termId == 0){
            return null;
        }
        return "GUILD_BACK:" + termId + ":DIAMOND_COST";
    }

    private String getVitCostKey(){
        int termId = getActivityTermId();
        if(termId == 0){
            return null;
        }
        return "GUILD_BACK:" + termId + ":VIT_COST";
    }

    private String getVitLimitKey(){
        int termId = getActivityTermId();
        if(termId == 0){
            return null;
        }
        return "GUILD_BACK:" + termId + ":VIT_LIMIT";
    }

    private String getGoldLimitKey(){
        int termId = getActivityTermId();
        if(termId == 0){
            return null;
        }
        return "GUILD_BACK:" + termId + ":GOLD_LIMIT";
    }

    /**
     * 掉落配置
     * @param count 数量
     * @return 掉落配置
     */
    public GuildBackTeamDropCfg getDropCfg(int count){
        ConfigIterator<GuildBackTeamDropCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(GuildBackTeamDropCfg.class);
        GuildBackTeamDropCfg dropCfg = null;
        for (GuildBackTeamDropCfg cfg : iterator){
            if(cfg.getCount() <= count && (dropCfg == null || cfg.getCount() > dropCfg.getCount())){
                dropCfg = cfg;
            }
        }
        return dropCfg;
    }

    /**
     * 组队掉落
     * @param playerIds 参与玩家列表
     */
    public void teamBattledrop(List<String> playerIds){
        if(!isOpening("")){
            return;
        }
        if(playerIds == null || playerIds.size() <= 1){
            return;
        }
        int count = 0;
        for(String playerId : playerIds){
            if(isActivityBackPlayer(playerId)){
                count++;
            }
        }
        if(count == 0){
            return;
        }
        for(String playerId : playerIds){
            ActivityManager.getInstance().postEvent(new GuildBackTeamBattleEvent(playerId, count));
        }
    }

    /**
     * 成就是否激活
     * @param playerId 玩家id
     * @return 是否激活
     */
    @Override
    public boolean isProviderActive(String playerId) {
        //活动开就激活
        return isOpening(playerId);
    }

    /**
     * 是否同步成就信息
     * @param playerId 玩家id
     * @return 是都同步
     */
    @Override
    public boolean isProviderNeedSync(String playerId) {
        //活动展示就同步
        return isShow(playerId);
    }

    /**
     * 成就数据
     * @param playerId 玩家id
     * @return 成就数据
     */
    @Override
    public Optional<AchieveItems> getAchieveItems(String playerId) {
        Optional<GuildBackEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        GuildBackEntity entity = opEntity.get();
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
     * 成就配置
     * @param achieveId 成就id
     * @return 成就配置
     */
    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        return HawkConfigManager.getInstance().getConfigByKey(GuildBackAchieveCfg.class, achieveId);
    }

    /**
     * 成就奖励action
     * @return action
     */
    @Override
    public Action takeRewardAction() {
        return Action.GUILD_BACK_ACHIEVE_REWARD;
    }

    /**
     * 成就绑定的活动id
     * @return 活动id
     */
    @Override
    public int providerActivityId() {
        return this.getActivityType().intValue();
    }

    /**
     * 联盟宝箱增加事件
     * @param event 事件
     */
    @Subscribe
    public void onGuildBackBoxAddEvent(GuildBackBoxAddEvent event) {
        String playerId = event.getPlayerId();
        if (isHidden(playerId)) {
            return;
        }
        Optional<GuildBackEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        GuildBackEntity entity = opEntity.get();
        GuildBackKvCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(GuildBackKvCfg.class);
        if(entity.getGetBox() >= kvCfg.getBoxMax()){
            return;
        }
        entity.setGetBox(entity.getGetBox() + 1);
    }

    @Subscribe
    public void onGuildBackTeamBattleEvent(GuildBackTeamBattleEvent event) {
        String playerId = event.getPlayerId();
        if (isHidden(playerId)) {
            return;
        }
        Optional<GuildBackEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        GuildBackEntity entity = opEntity.get();
        GuildBackKvCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(GuildBackKvCfg.class);
        if(entity.getDropCount() >= kvCfg.getDropMax()){
            return;
        }
        GuildBackTeamDropCfg dropCfg = getDropCfg(event.getNum());
        if(dropCfg == null){
            return;
        }
        entity.setDropCount(entity.getDropCount() + 1);
        if(entity.getDropCount() == 1 || HawkRand.randInt(10000) < dropCfg.getProbability()){
            sendMailToPlayer(playerId, MailConst.MailId.GUILD_BACK_MAIL_2024100202, null, null, null,
                    RewardHelper.toRewardItemImmutableList(dropCfg.getRewards()), false);
        }
    }

    /**
     * 跨天事件
     * @param event 事件
     */
    @Subscribe
    public void onContinueLoginEvent(ContinueLoginEvent event) {
        String playerId = event.getPlayerId();
        if(!isOpening(playerId)){
            return;
        }
        resetAchieve(playerId);
        checkPlayerAddGuildBox(playerId);
    }

    /**
     * 重置成绩
     * @param playerId
     */
    private void resetAchieve(String playerId){
        Optional<GuildBackEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        GuildBackEntity entity = opEntity.get();
        long now = HawkTime.getMillisecond();
        if(HawkTime.isSameDay(now, entity.getResetTime())){
            return;
        }
        entity.setResetTime(now);
        entity.setDayPoolCount(0);
        entity.setDropCount(0);
        if (entity.getItemList().isEmpty()) {
            //初始化成就数据
            this.initAchieve(playerId);
        }
        //数据有变化的成就，需要推送给前端
        List<AchieveItem> needPushList = new ArrayList<>();
        //遍历成就数据
        for(AchieveItem item : entity.getItemList()){
            GuildBackAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GuildBackAchieveCfg.class, item.getAchieveId());
            if(cfg == null){
                continue;
            }
            if(!cfg.isReset()){
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
        entity.notifyUpdate();
        if(!needPushList.isEmpty()){
            //推送给前端
            AchievePushHelper.pushAchieveUpdate(playerId, needPushList);
        }
        //通用累计登陆
        postCommonLoginEvent(playerId);
    }

    /**
     * 初始化成就数据
     * @param playerId 玩家id
     */
    private void initAchieve(String playerId) {
        Optional<GuildBackEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        GuildBackEntity entity = opEntity.get();
        //如果成就数据为空就初始化成就数据
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        ConfigIterator<GuildBackAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(GuildBackAchieveCfg.class);
        List<AchieveItem> list = new ArrayList<>();
        for (GuildBackAchieveCfg cfg : iterator){
            AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
            list.add(item);
        }
        entity.setItemList(list);
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()));
    }

    /**
     * 创建联盟事件
     * @param event 事件
     */
    @Subscribe
    public void onCreateGuildEvent(CreateGuildEvent event) {
        String playerId = event.getPlayerId();
        if (isHidden(playerId)) {
            return;
        }
        String guildId = event.getGuildId();
        refreshBackPlayerCount(guildId);
    }

    /**
     * 联盟解散
     * @param event 事件
     */
    @Subscribe
    public void onGuildDismissEvent(GuildDismissEvent event) {
        queue.add(new GuildBackAction(event.getPlayerId(), GuildBackActionEnum.GUILD_DISMISS, event.getGuildId()));
        checkBackPlayer(event.getPlayerId());
    }

    /**
     * 加入联盟事件
     * @param event 事件
     */
    @Subscribe
    public void onJoinGuildEvent(JoinGuildEvent event) {
        String playerId = event.getPlayerId();
        if (HawkOSOperator.isEmptyString(playerId)) {
            return;
        }
        if (isHidden(playerId)) {
            return;
        }
        String guildId = getDataGeter().getGuildId(playerId);
        if (HawkOSOperator.isEmptyString(guildId)) {
            return;
        }
        checkBackPlayer(playerId);
        refreshBackPlayerCount(guildId);
        checkPlayerAddGuildBox(playerId);
        double diamondCost = guildDiamondCostMap.getOrDefault(guildId, 0d);
        ActivityManager.getInstance().postEvent(new GuildBackGoldCostEvent(playerId, (int)diamondCost));
        double vitCost = guildVitCostMap.getOrDefault(guildId, 0d);
        ActivityManager.getInstance().postEvent(new GuildBackVitCostEvent(playerId, (int)vitCost));
    }

    /**
     * 联盟退出事件
     * @param event 退出联盟
     */
    @Subscribe
    public void onGuildQuite(GuildQuiteEvent event){
        String playerId = event.getPlayerId();
        if (HawkOSOperator.isEmptyString(playerId)) {
            return;
        }
        if (isHidden(playerId)) {
            return;
        }
        String guildId = event.getGuildId();
        if (HawkOSOperator.isEmptyString(guildId)) {
            return;
        }
        refreshBackPlayerCount(guildId);
    }

    /**
     * 回流事件
     * @param event 事件
     */
    @Subscribe
    public void onLoginDayChemistryEvent(LoginDayChemistryEvent event){
        String playerId = event.getPlayerId();
        if (HawkOSOperator.isEmptyString(playerId)) {
            return;
        }
        if (isHidden(playerId)) {
            return;
        }
        String guildId = getDataGeter().getGuildId(playerId);
        if (HawkOSOperator.isEmptyString(guildId)) {
            return;
        }
        refreshBackPlayerCount(guildId);
    }

    /**
     * 体力消耗事件
     * @param event 事件
     */
    @Subscribe
    public void onVitreceiveConsumeEvent(VitCostEvent event) {
        String playerId = event.getPlayerId();
        if (HawkOSOperator.isEmptyString(playerId)) {
            return;
        }
        if (isHidden(playerId)) {
            return;
        }
        String guildId = getDataGeter().getGuildId(playerId);
        if (HawkOSOperator.isEmptyString(guildId)) {
            return;
        }
        boolean isBack = isActivityBackPlayer(playerId);
        int baseLv = getDataGeter().getConstructionFactoryLevel(playerId);
        queue.add(new GuildBackAction(playerId, GuildBackActionEnum.VIT_ADD, guildId, isBack, event.getCost(), baseLv));
    }

    /**
     * 金币消耗事件
     * @param event 事件
     */
    @Subscribe
    public void onConsumeGlodBar(ConsumeMoneyEvent event) {
        if (event.getResType() == Const.PlayerAttr.GOLD_VALUE) {
            String playerId = event.getPlayerId();
            if (HawkOSOperator.isEmptyString(playerId)) {
                return;
            }
            if (isHidden(playerId)) {
                return;
            }
            String guildId = getDataGeter().getGuildId(playerId);
            if (HawkOSOperator.isEmptyString(guildId)) {
                return;
            }
            boolean isBack = isActivityBackPlayer(playerId);
            int baseLv = getDataGeter().getConstructionFactoryLevel(playerId);
            queue.add(new GuildBackAction(playerId, GuildBackActionEnum.GOLD_ADD, guildId, isBack, event.getNum(), baseLv));
        }else if(event.getResType() == Const.PlayerAttr.DIAMOND_VALUE){
            String playerId = event.getPlayerId();
            if (HawkOSOperator.isEmptyString(playerId)) {
                return;
            }
            if (isHidden(playerId)) {
                return;
            }
            String guildId = getDataGeter().getGuildId(playerId);
            if (HawkOSOperator.isEmptyString(guildId)) {
                return;
            }
            boolean isBack = isActivityBackPlayer(playerId);
            queue.add(new GuildBackAction(playerId, GuildBackActionEnum.DIAMOND_ADD, guildId, isBack, event.getNum()));
        }

    }


    /**
     * 前端请求活动数据
     * @param playerId 玩家id
     * @return 执行结果
     */
    public Result<Integer> info(String playerId) {
        syncActivityDataInfo(playerId);
        //返回兑换状态
        return Result.success();
    }

    /**
     * 兑换
     * @param playerId 玩家id
     * @param exchangeId 兑换配置id
     * @param num 兑换数量
     * @return 执行结果
     */
    public Result<Integer> exchange(String playerId, int exchangeId, int num) {
        //判断活动是否开启，如果没开返回错误码
        if (!isShow(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        GuildBackShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GuildBackShopCfg.class, exchangeId);
        if (cfg == null) {
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        Optional<GuildBackEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        GuildBackEntity entity = opEntity.get();
        //当前已经兑换数量
        int buyNum = entity.getBuyNumMap().getOrDefault(exchangeId, 0);
        //兑换后的数量
        int newNum = buyNum + num;
        //判断是否超过可兑换数量最大值，如果超过返回错误码
        if (newNum > cfg.getTimes()) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        //兑换消耗
        boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), num, Action.GUILD_BACK_SHOP_COST, true);
        //如果不够消耗，返回错误码
        if (!flag) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        //设置新的兑奖数量
        entity.getBuyNumMap().put(exchangeId, newNum);
        entity.notifyUpdate();
        //发奖
        this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.GUILD_BACK_SHOP_GET, true);
        //记录日志
        logger.info("GuildBackActivity exchange playerId:{}, exchangeId:{}, num:{}", playerId, exchangeId, num);
        syncActivityDataInfo(playerId);
        //返回兑换状态
        return Result.success(newNum);
    }

    /**
     * 邀请
     * @param playerId 玩家id
     * @param targetId 被邀请的玩家id
     * @return 执行结果
     */
    public Result<Integer> invite(String playerId, String targetId) {
        if(HawkOSOperator.isEmptyString(targetId)){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        Map<String, Activity.GuildBackInvitePlayer.Builder> map = inviteBackPlayerMap.get(playerId);
        if(map == null){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        Activity.GuildBackInvitePlayer.Builder backPlayer = map.get(targetId);
        if(backPlayer == null){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        if(backPlayer.getIsInvited()){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        if(!getDataGeter().checkGuildAuthority(playerId, GuildManager.AuthId.INVITE_TO_JOIN_ALLIANCE)){
            return Result.fail(Status.Error.GUILD_LOW_AUTHORITY_VALUE);
        }
        if(!HawkOSOperator.isEmptyString(getDataGeter().getGuildId(targetId))){
            return Result.fail(Status.Error.GUILD_ALREADYJOIN_VALUE);
        }
        int result = getDataGeter().invitePlayerJoinGuild(playerId, targetId);
        if (result != Status.SysError.SUCCESS_OK_VALUE) {
            return Result.fail(result);
        }
        backPlayer.setIsInvited(true);
        Activity.GuildBackInviteResp.Builder resp = Activity.GuildBackInviteResp.newBuilder();
        resp.setPlayer(backPlayer);
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.GUILD_BACK_INVITE_RESP, resp));
        //返回兑换状态
        return Result.success();
    }

    /**
     * 邀请列表
     * @param playerId 玩家id
     * @param isSwitch 是否刷新列表
     * @return 执行结果
     */
    public Result<Integer> inviteList(String playerId, boolean isSwitch) {
        GuildBackKvCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(GuildBackKvCfg.class);
        Map<String, Activity.GuildBackInvitePlayer.Builder> map = inviteBackPlayerMap.get(playerId);
        if(map == null || isSwitch){
            List<Activity.GuildBackInvitePlayer.Builder> list = new ArrayList<>(backPlayerMap.values());
            Collections.shuffle(list);
            Map<String, Activity.GuildBackInvitePlayer.Builder> tmp = new ConcurrentHashMap<>();
            int count = 0;
            for(Activity.GuildBackInvitePlayer.Builder backPlayer : list){
                if(!getDataGeter().isPlayerExist(backPlayer.getPlayerId())){
                    continue;
                }
                tmp.put(backPlayer.getPlayerId(), backPlayer.clone());
                count++;
                if(count >= kvCfg.getInviteCount()){
                    break;
                }
            }
            inviteBackPlayerMap.put(playerId, tmp);
        }
        map = inviteBackPlayerMap.get(playerId);
        Activity.GuildBackInviteListResp.Builder resp = Activity.GuildBackInviteListResp.newBuilder();
        for(Activity.GuildBackInvitePlayer.Builder backPlayer : map.values()){
            try {
                //玩家名字
                backPlayer.setPlayeName(getDataGeter().getPlayerName(backPlayer.getPlayerId()));
                //玩家头像
                backPlayer.setIcon(getDataGeter().getIcon(backPlayer.getPlayerId()));
                //平台头像
                backPlayer.setPfIcon(getDataGeter().getPfIcon(backPlayer.getPlayerId()));
            }catch (Exception e){
                HawkException.catchException(e);
            }
            resp.addPlayers(backPlayer);
        }
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.GUILD_BACK_INVITE_LIST_RESP, resp));
        //返回兑换状态
        return Result.success();
    }

    /**
     * 瓜分奖池
     * @param playerId 玩家id
     * @param type 奖池type
     * @return 执行结果
     */
    public Result<Integer> poolReward(String playerId, int type) {
        //没有联盟返回错误码
        String guildId = getDataGeter().getGuildId(playerId);
        if (HawkOSOperator.isEmptyString(guildId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        GuildBackKvCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(GuildBackKvCfg.class);
        if(getDataGeter().getJoinGuildTime(playerId) + kvCfg.getDivideJackpotCD() > HawkTime.getMillisecond()){
            return Result.fail(Status.Error.GUILD_BACK_ACTI_NEW_JOIN_VALUE);
        }
        Optional<GuildBackEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        GuildBackEntity entity = opEntity.get();
        //是否回流
        boolean isBack = isActivityBackPlayer(playerId);
        GuildBackIdentityCfg identityCfg = getIdentityCfg(isBack);
        if(entity.getDayPoolCount() >= identityCfg.getDayDivideStaminaTimesMax()){
            return Result.fail(Status.Error.GUILD_BACK_ACTI_POOL_LIMIT_VALUE);
        }
        switch (type){
            case 1:{//瓜分金币
                if(entity.getGoldNum() >= identityCfg.getDayDivideStaminaCoinsValueMax()){
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                double count = guildGoldPoolMap.getOrDefault(guildId, kvCfg.getStartCoinsValue());
                if(((int)count) <= 0){
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                //消耗
                boolean flag = this.getDataGeter().cost(playerId, RewardHelper.toRewardItemImmutableList(kvCfg.getPartitionCost()), 1, Action.GUILD_BACK_GOLD_POOL_COST, true);
                //如果不够消耗，返回错误码
                if (!flag) {
                    return Result.fail(Status.Error.GUILD_BACK_ACTI_POOL_NOT_ENOUGH_VALUE);
                }
                entity.setDayPoolCount(entity.getDayPoolCount() + 1);
                queue.add(new GuildBackAction(playerId, GuildBackActionEnum.GOLD_PARTITION, guildId, isBack));
            }
            break;
            case 2:{//瓜分体力
                if(entity.getVitNum() >= identityCfg.getDayDivideStaminaVitValueMax()){
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                double count = guildVitPoolMap.getOrDefault(guildId, kvCfg.getStartVitValue());
                if(((int)count) <= 0){
                    return Result.fail(Status.Error.GUILD_BACK_ACTI_POOL_NOT_ENOUGH_VALUE);
                }
                //消耗
                boolean flag = this.getDataGeter().cost(playerId, RewardHelper.toRewardItemImmutableList(kvCfg.getPartitionCost()), 1, Action.GUILD_BACK_VIT_POOL_COST, true);
                //如果不够消耗，返回错误码
                if (!flag) {
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                entity.setDayPoolCount(entity.getDayPoolCount() + 1);
                queue.add(new GuildBackAction(playerId, GuildBackActionEnum.VIT_PARTITION, guildId, isBack));
            }
            break;
        }
        //返回兑换状态
        return Result.success();
    }

    /**
     * 宝箱领奖
     * @param playerId 玩家id
     * @param isAll 是否全部领奖
     * @return 执行结果
     */
    public Result<Integer> boxReward(String playerId, boolean isAll) {
        //没有联盟返回错误码
        String guildId = getDataGeter().getGuildId(playerId);
        if (HawkOSOperator.isEmptyString(guildId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        GuildBackKvCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(GuildBackKvCfg.class);
        if(getDataGeter().getJoinGuildTime(playerId) + kvCfg.getLuckDrawCD() > HawkTime.getMillisecond()){
            return Result.fail(Status.Error.GUILD_BACK_ACTI_NEW_JOIN_VALUE);
        }
        Optional<GuildBackEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        GuildBackEntity entity = opEntity.get();
        if(entity.getGetBox() <= entity.getUseBox()){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        int count = 1;
        if(isAll){
            count = entity.getGetBox() - entity.getUseBox();
        }
        if(count <= 0){
            return Result.fail(Status.Error.GUILD_BACK_ACTI_POOL_NOT_ENOUGH_VALUE);
        }
        entity.setUseBox(entity.getUseBox() + count);
        this.getDataGeter().sendAwardFromAwardCfg(kvCfg.getBoxRewards(), count, playerId, true, Action.GUILD_BACK_BOX_REWARD);
        syncActivityDataInfo(playerId);
        //返回兑换状态
        return Result.success();
    }

    /**
     * gm逻辑
     * @param map 参数
     * @return 结果
     */
    public String gm(Map<String, String> map){
        //要执行的gm指令
        String cmd = map.getOrDefault("cmd", "");
        switch (cmd) {
            case "addBackPlayer": {
                String playerId = map.get("playerId");
                gmAddBackPLayer(playerId);
                return "addBackPlayer success";
            }
            case "addBackPlayerFromOnline": {
                Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
                for(String playerId : onlinePlayerIds) {
                    gmAddBackPLayer(playerId);
                }
                return "addBackPlayerFromOnline success";
            }
            case "addOnlineVitPool":{
                Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
                for(String playerId : onlinePlayerIds) {
                    String guildId = getDataGeter().getGuildId(playerId);
                    if (HawkOSOperator.isEmptyString(guildId)) {
                        continue;
                    }
                    boolean isBack = isActivityBackPlayer(playerId);
                    int baseLv = getDataGeter().getConstructionFactoryLevel(playerId);
                    queue.add(new GuildBackAction(playerId, GuildBackActionEnum.VIT_ADD, guildId, isBack, 1000, baseLv));
                }
                return "addOnlineVitPool success";
            }
            case "addOnlineGuildBox":{
                Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
                for(String playerId : onlinePlayerIds) {
                    Optional<GuildBackEntity> opEntity = getPlayerDataEntity(playerId);
                    //如果数据为空直接返回
                    if (!opEntity.isPresent()) {
                        continue;
                    }
                    GuildBackEntity entity = opEntity.get();
                    entity.setGetBox(entity.getGetBox() + 100);
                }
                return "addOnlineGuildBox success";
            }
            case "backPlayerList":{
                StringBuilder info = new StringBuilder();
                for(Activity.GuildBackInvitePlayer.Builder player : backPlayerMap.values()){
                    info.append(player.getPlayerId()).append(":").append(player.getPlayeName()).append("<br>");
                }
                return info.toString();
            }
            case "testTeamDrop":{
                Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
                int count = Integer.parseInt(map.get("count"));
                for(int i = 0; i < count; i++){
                    teamBattledrop(new ArrayList<>(onlinePlayerIds));
                }
            }
        }
        return "no match cmd";
    }

    /**
     * gm添加回流玩家
     * @param playerId
     */
    public void gmAddBackPLayer(String playerId){
        Activity.GuildBackInvitePlayer.Builder backPlayer = Activity.GuildBackInvitePlayer.newBuilder();
        backPlayer.setPlayerId(playerId);
        backPlayer.setPlayeName(getDataGeter().getPlayerName(playerId));
        backPlayer.setIcon(getDataGeter().getIcon(playerId));
        backPlayer.setPfIcon(getDataGeter().getPfIcon(playerId));
        backPlayer.setIsInvited(false);
        backPlayer.setBackTime(HawkTime.getMillisecond());
        backPlayerMap.put(playerId, backPlayer);
        String key = getBackPlayerKey();
        if(key != null){
            ActivityGlobalRedis.getInstance().getRedisSession().hSetBytes(key, playerId, backPlayer.build().toByteArray());
        }

    }

}
