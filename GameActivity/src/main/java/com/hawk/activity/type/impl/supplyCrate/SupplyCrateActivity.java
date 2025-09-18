package com.hawk.activity.type.impl.supplyCrate;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.*;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveManager;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.supplyCrate.cfg.SupplyCrateAchieveCfg;
import com.hawk.activity.type.impl.supplyCrate.cfg.SupplyCrateDrawCfg;
import com.hawk.activity.type.impl.supplyCrate.cfg.SupplyCrateKVCfg;
import com.hawk.activity.type.impl.supplyCrate.cfg.SupplyCrateTimeCfg;
import com.hawk.activity.type.impl.supplyCrate.entity.SupplyCrateEntity;
import com.hawk.activity.type.impl.supplyCrate.entity.SupplyCrateItemObj;
import com.hawk.game.protocol.*;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.rank.RankScoreHelper;
import com.hawk.log.Action;
import com.hawk.log.LogConst;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Tuple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SupplyCrateActivity extends ActivityBase implements AchieveProvider {
    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger("Server");
    private static final String redisRankKey = "SUPPY_CRATE:";

    private static final String SUPPY_CRATE_VIT_COST = "SUPPY_CRATE_VIT_COST";
    private static final String SUPPY_CRATE_ITEM_GET = "SUPPY_CRATE_ITEM_GET";
    private static final String SUPPY_CRATE_RECHARGE = "SUPPY_CRATE_RECHARGE";

    private static final String SUPPY_CRATE_PLAYER_BOX = "SUPPY_CRATE_PLAYER_BOX:";

    private Map<String, Activity.SupplyCrateRankResp.Builder> rankMap = new ConcurrentHashMap<>();
    private Map<String, Long> lastRankMap = new ConcurrentHashMap<>();
    private boolean isInit = false;

    private final ConcurrentLinkedQueue<HawkTuple2<String, String>> queue1 = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<HawkTuple3<String, String, Integer>> queue2 = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<HawkTuple2<String, Integer>> queue3 = new ConcurrentLinkedQueue<>();

    private Map<String, Set<String>> rechargePlayerMap = new ConcurrentHashMap<>();
    private Map<String, Map<String, Integer>> vitCostMap = new ConcurrentHashMap<>();
    private Map<String, Integer> itemGetMap = new ConcurrentHashMap<>();

    public SupplyCrateActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.SUPPLY_CRATE;
    }

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        SupplyCrateActivity activity = new SupplyCrateActivity(config.getActivityId(), activityEntity);
        AchieveContext.registeProvider(activity);
        return activity;
    }

    @Override
    public int providerActivityId() {
        return this.getActivityType().intValue();
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        //根据玩家id和活动期数去数据库里取数据
        List<SupplyCrateEntity> queryList = HawkDBManager.getInstance()
                .query("from SupplyCrateEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
            SupplyCrateEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        SupplyCrateEntity entity = new SupplyCrateEntity(playerId, termId);
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

    @Override
    public Optional<AchieveItems> getAchieveItems(String playerId) {
        Optional<SupplyCrateEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        SupplyCrateEntity entity = opEntity.get();
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
        Optional<SupplyCrateEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        SupplyCrateEntity entity = opEntity.get();
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        ConfigIterator<SupplyCrateAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SupplyCrateAchieveCfg.class);
        List<AchieveItem> list = new ArrayList<>();
        while (iterator.hasNext()) {
            SupplyCrateAchieveCfg cfg = iterator.next();
            AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
            list.add(item);
        }
        entity.setItemList(list);
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()));
    }

    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        SupplyCrateAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SupplyCrateAchieveCfg.class, achieveId);
        return cfg;
    }

    @Override
    public Action takeRewardAction() {
        return Action.SUPPLY_CRATE_ACHIEVE_REWARD;
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        Optional<SupplyCrateEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        SupplyCrateKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SupplyCrateKVCfg.class);
        if(kvCfg == null){
            return;
        }
        SupplyCrateEntity entity = opEntity.get();
        SupplyCrateDrawCfg cfg = SupplyCrateDrawCfg.getCfgByRound(entity.getRound());
        if(cfg == null){
            return;
        }
        Activity.SupplyCrateSync.Builder builder = Activity.SupplyCrateSync.newBuilder();
        builder.setIsCanOpen(entity.isCanOPen());
        builder.setIsCanNext(entity.isCanNext());
        if(entity.getCustomIndex() > 0){
            builder.setCustomItem(cfg.getCoustmItem(entity.getCustomIndex()).toPB());
        }
        if(!entity.getOpenItemList().isEmpty()){
            for(SupplyCrateItemObj itemObj : entity.getOpenItemList()){
                builder.addOpenItems(itemObj.toPB());
            }
        }
        builder.setBoxProg(entity.getBoxProg());
        builder.setRound(entity.getRound());
        builder.setMulti(entity.getMult());
        builder.setIsBoxMax(entity.getBoxCount() >= kvCfg.getBoxMax());
        builder.setBoxCount((entity.getBoxCount() - entity.getBoxProg()) / kvCfg.getTargetItemCount());
        pushToPlayer(playerId, HP.code2.SUPPLY_CRATE_SYNC_VALUE, builder);
    }

    @Override
    public void onPlayerLogin(String playerId) {
        try {
            rewardSupplement(playerId);
            int vitTarget = 200;
            SupplyCrateKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SupplyCrateKVCfg.class);
            if(kvCfg != null){
                vitTarget = kvCfg.getVitTarget();
            }
            String guildId = this.getDataGeter().getGuildId(playerId);
            if(!HawkOSOperator.isEmptyString(guildId)){
                if(vitCostMap.containsKey(guildId)){
                    Map<String, Integer> playerVitMap = vitCostMap.get(guildId);
                    int vitCount = 0;
                    for(String memberId : playerVitMap.keySet()){
                        if(playerVitMap.get(memberId) >= vitTarget){
                            vitCount++;
                        }
                    }
                    ActivityManager.getInstance().postEvent(new SupplyCrateGuildVitCostEvent(playerId, vitCount));
                }
                if(rechargePlayerMap.containsKey(guildId)){
                    int rechageCount = rechargePlayerMap.get(guildId).size();
                    ActivityManager.getInstance().postEvent(new SupplyCrateGuildRechargeEvent(playerId, rechageCount));
                }
                int itemCount = itemGetMap.getOrDefault(guildId, 0);
                ActivityManager.getInstance().postEvent(new SupplyCrateGuildItemGetEvent(playerId, itemCount));
            }
            String boxCountStr = ActivityGlobalRedis.getInstance().hget(SUPPY_CRATE_PLAYER_BOX, playerId);
            if(!HawkOSOperator.isEmptyString(boxCountStr)){
                int boxCount = Integer.parseInt(boxCountStr);
                ActivityGlobalRedis.getInstance().hDel(SUPPY_CRATE_PLAYER_BOX, playerId);
                ActivityManager.getInstance().postEvent(new SupplyCrateGuildBoxEvent(playerId, boxCount));
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }


    private void rewardSupplement(String playerId) {
        SupplyCrateKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SupplyCrateKVCfg.class);
        if(kvCfg == null){
            return;
        }
        int termId = this.getActivityTermId();
        if (termId > 0) {
            return;
        }
        int lastTermId = this.getLastTermId();
        if (lastTermId <= 0) {
            return;
        }
        String spKey = playerId + ":SupplyCrateSupplement:" + lastTermId;
        String val = ActivityGlobalRedis.getInstance().getRedisSession().getString(spKey);
        if (!HawkOSOperator.isEmptyString(val)){
            return;
        }
        ActivityGlobalRedis.getInstance().getRedisSession().setString(spKey, spKey);
        int count = this.getDataGeter().getItemNum(playerId, kvCfg.getUseItemId());
        if(count <= 0){
            return;
        }
        List<Reward.RewardItem.Builder> costList = new ArrayList<>();
        Reward.RewardItem.Builder costBuilder = Reward.RewardItem.newBuilder();
        //类型为道具
        costBuilder.setItemType(Const.ItemType.TOOL_VALUE);
        //待扣除物品ID
        costBuilder.setItemId(kvCfg.getUseItemId());
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
        List<Reward.RewardItem.Builder> rewardList = new ArrayList<>();
        Reward.RewardItem.Builder rewardBuilder = Reward.RewardItem.newBuilder();
        //类型为道具
        rewardBuilder.setItemType(Const.ItemType.TOOL_VALUE);
        //待扣除物品ID
        rewardBuilder.setItemId(kvCfg.getExchangeRewardId());
        //待扣除的物品数量
        rewardBuilder.setItemCount(count);
        //把待扣除的物品数据加入参数容器
        rewardList.add(rewardBuilder);
        sendMailToPlayer(playerId, MailConst.MailId.MAIL_2024071203, null, new Object[] { count }, new Object[] { count }, rewardList, false);
    }


    public String gm(Map<String, String> map){
        String cmd = map.getOrDefault("cmd", "");
        switch (cmd) {
            case "rewardSupplement":{
                String playerId = map.getOrDefault("playerId", "");
                SupplyCrateKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SupplyCrateKVCfg.class);
                if(kvCfg == null){
                    return "rewardSupplement kvCfg is null";
                }
                int count = this.getDataGeter().getItemNum(playerId, kvCfg.getUseItemId());
                if(count <= 0){
                    return "rewardSupplement count is zero";
                }
                List<Reward.RewardItem.Builder> costList = new ArrayList<>();
                Reward.RewardItem.Builder costBuilder = Reward.RewardItem.newBuilder();
                //类型为道具
                costBuilder.setItemType(Const.ItemType.TOOL_VALUE);
                //待扣除物品ID
                costBuilder.setItemId(kvCfg.getUseItemId());
                //待扣除的物品数量
                costBuilder.setItemCount(count);
                //把待扣除的物品数据加入参数容器
                costList.add(costBuilder);
                //注意这里先扣除源道具，如果失败，不给兑换后的道具
                boolean cost = this.getDataGeter().cost(playerId,costList, 1, Action.ROSE_GIFT_DRAW_RECOVER, true);
                //扣除失败不继续处理
                if (!cost) {
                    return "rewardSupplement cost fail";
                }
                List<Reward.RewardItem.Builder> rewardList = new ArrayList<>();
                Reward.RewardItem.Builder rewardBuilder = Reward.RewardItem.newBuilder();
                //类型为道具
                rewardBuilder.setItemType(Const.ItemType.TOOL_VALUE);
                //待扣除物品ID
                rewardBuilder.setItemId(kvCfg.getExchangeRewardId());
                //待扣除的物品数量
                rewardBuilder.setItemCount(count);
                //把待扣除的物品数据加入参数容器
                rewardList.add(rewardBuilder);
                sendMailToPlayer(playerId, MailConst.MailId.MAIL_2024071203, null, new Object[] { count }, new Object[] { count }, rewardList, false);
            }
            break;
        }
        return "no match cmd";
    }


    private int getLastTermId() {
        long curTime = HawkTime.getMillisecond();
        SupplyCrateTimeCfg lastCfg = null;
        List<SupplyCrateTimeCfg> list = HawkConfigManager.getInstance()
                .getConfigIterator(SupplyCrateTimeCfg.class).toList();
        for(SupplyCrateTimeCfg cfg : list){
            if(cfg.getHiddenTimeValue() < curTime){
                if(lastCfg == null){
                    lastCfg = cfg;
                }
                if(cfg.getTermId() > lastCfg.getTermId()){
                    lastCfg = cfg;
                }
            }
        }
        if(lastCfg == null){
            return  0;
        }
        return lastCfg.getTermId();
    }

    @Override
    public void onOpen() {
        rechargePlayerMap.clear();
        vitCostMap.clear();
        itemGetMap.clear();
        ActivityGlobalRedis.getInstance().del(SUPPY_CRATE_RECHARGE);
        ActivityGlobalRedis.getInstance().del(SUPPY_CRATE_VIT_COST);
        ActivityGlobalRedis.getInstance().del(SUPPY_CRATE_ITEM_GET);
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for(String playerId : onlinePlayerIds){
            callBack(playerId, GameConst.MsgId.ON_SUPPLY_CRATE_INIT, () -> {
                syncActivityDataInfo(playerId);
                initAchieve(playerId);
                Optional<SupplyCrateEntity> opEntity = getPlayerDataEntity(playerId);
                if (opEntity.isPresent()) {
                    SupplyCrateEntity entity = opEntity.get();
                    entity.setResetTime(HawkTime.getMillisecond());
                }
                postCommonLoginEvent(playerId);
            });
        }
    }

    @Override
    public void onTick() {
        init();
        checkCrossDay();
        goldTick();
        vitTick();
        itemTick();
    }

    public void init(){
        if(isInit){
            return;
        }
        isInit = true;
        lastTickTime = HawkTime.getMillisecond();
        List<String> guildIdList = this.getDataGeter().getGuildIds();
        if (guildIdList.isEmpty()) {
        	return;
        }
        
        String [] guildIds = guildIdList.toArray(new String[0]);
        List<String> map1 = ActivityGlobalRedis.getInstance().getRedisSession().hmGet(SUPPY_CRATE_VIT_COST, guildIds);
        List<String> map2 = ActivityGlobalRedis.getInstance().getRedisSession().hmGet(SUPPY_CRATE_ITEM_GET, guildIds);
        List<String> map3 = ActivityGlobalRedis.getInstance().getRedisSession().hmGet(SUPPY_CRATE_RECHARGE, guildIds);
        for(int i = 0; i < map1.size() ; i++){
            try {
                String tmp = map1.get(i);
                if(HawkOSOperator.isEmptyString(tmp)){
                    continue;
                }
                vitCostMap.put(guildIds[i], SerializeHelper.stringToMap(tmp, String.class, Integer.class));
                logger.info("SupplyCrateActivity vitCostMap guildid:{}, value:{}", guildIds[i], tmp);
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
        for(int i = 0; i < map2.size() ; i++){
            try {
                String tmp = map2.get(i);
                if(HawkOSOperator.isEmptyString(tmp)){
                    continue;
                }
                itemGetMap.put(guildIds[i], Integer.parseInt(tmp));
                logger.info("SupplyCrateActivity itemGetMap guildid:{}, value:{}", guildIds[i], tmp);
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
        for(int i = 0; i < map3.size() ; i++){
            try {
                String tmp = map3.get(i);
                if(HawkOSOperator.isEmptyString(tmp)){
                    continue;
                }
                rechargePlayerMap.put(guildIds[i], SerializeHelper.stringToSet(String.class, tmp, SerializeHelper.ATTRIBUTE_SPLIT,null,null));
                logger.info("SupplyCrateActivity rechargePlayerMap guildid:{}, value:{}", guildIds[i], tmp);
            }catch (Exception e){
                HawkException.catchException(e);
            }

        }
    }

    private long lastTickTime = 0;

    public void checkCrossDay(){
        long now = HawkTime.getMillisecond();
        if(HawkTime.isSameDay(now, lastTickTime)){
            return;
        }
        lastTickTime = now;
        vitCostMap.clear();
        rechargePlayerMap.clear();
        ActivityGlobalRedis.getInstance().del(SUPPY_CRATE_VIT_COST);
        ActivityGlobalRedis.getInstance().del(SUPPY_CRATE_RECHARGE);
    }

    public void goldTick(){
        try {
            Set<String> updateGuildIds = new HashSet<>();
            while (!queue1.isEmpty()){
                HawkTuple2<String, String> rechargePlayer = queue1.poll();
                if(!rechargePlayerMap.containsKey(rechargePlayer.first)){
                    rechargePlayerMap.put(rechargePlayer.first, new HashSet<>());
                }
                rechargePlayerMap.get(rechargePlayer.first).add(rechargePlayer.second);
                updateGuildIds.add(rechargePlayer.first);
                logger.info("SupplyCrateActivity rechargePlayerMap tick guildid:{}, playerId:{}", rechargePlayer.first, rechargePlayer.second);
            }
            for(String guildId : updateGuildIds){
                int count = rechargePlayerMap.get(guildId).size();
                pushEventToGuildActive(guildId, 2, count);
                String tmp = SerializeHelper.collectionToString(rechargePlayerMap.get(guildId), SerializeHelper.ATTRIBUTE_SPLIT);
                ActivityGlobalRedis.getInstance().hset(SUPPY_CRATE_RECHARGE, guildId, tmp);
                logger.info("SupplyCrateActivity rechargePlayerMap tick guildid:{}, value:{}", guildId, tmp);
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }

    }

    public void vitTick(){
        try {
            Set<String> updateGuildIds = new HashSet<>();
            while (!queue2.isEmpty()){
                HawkTuple3<String, String, Integer> vitCost = queue2.poll();
                if(!vitCostMap.containsKey(vitCost.first)){
                    vitCostMap.put(vitCost.first, new HashMap<>());
                }
                int count = vitCostMap.get(vitCost.first).getOrDefault(vitCost.second, 0);
                vitCostMap.get(vitCost.first).put(vitCost.second, count + vitCost.third);
                updateGuildIds.add(vitCost.first);
                logger.info("SupplyCrateActivity vitCostMap tick guildid:{}, playerId:{}, count:{}", vitCost.first, vitCost.second, vitCost.third);
            }
            int vitTarget = 200;
            SupplyCrateKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SupplyCrateKVCfg.class);
            if(kvCfg != null){
                vitTarget = kvCfg.getVitTarget();
            }
            for(String guildId : updateGuildIds){
                Map<String, Integer> playerVitMap = vitCostMap.get(guildId);
                int count = 0;
                for(String playerId : playerVitMap.keySet()){
                    if(playerVitMap.get(playerId) >= vitTarget){
                        count++;
                    }
                }
                pushEventToGuildActive(guildId, 1, count);
                String tmp = SerializeHelper.mapToString(vitCostMap.get(guildId));
                ActivityGlobalRedis.getInstance().hset(SUPPY_CRATE_VIT_COST, guildId, tmp);
                logger.info("SupplyCrateActivity vitCostMap tick guildid:{}, value:{}", guildId, tmp);
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    public void itemTick(){
        try {
            Set<String> updateGuildIds = new HashSet<>();
            Map<String, Integer> boxCountMap = new HashMap<>();
            while (!queue3.isEmpty()){
                HawkTuple2<String, Integer> itemGet = queue3.poll();
                int count = itemGetMap.getOrDefault(itemGet.first, 0);
                itemGetMap.put(itemGet.first, count + itemGet.second);
                updateGuildIds.add(itemGet.first);
                boxCountMap.put(itemGet.first, boxCountMap.getOrDefault(itemGet.first, 0) + itemGet.second);
                logger.info("SupplyCrateActivity itemGetMap tick guildid:{}, count:{}", itemGet.first, itemGet.second);
            }
            for(String guildId : updateGuildIds){
                int count = itemGetMap.getOrDefault(guildId, 0);
                pushEventToGuildActive(guildId, 3, count);
                String tmp = String.valueOf(itemGetMap.get(guildId));
                ActivityGlobalRedis.getInstance().hset(SUPPY_CRATE_ITEM_GET, guildId, tmp);
                pushEventToGuildActive(guildId, 4, boxCountMap.getOrDefault(guildId, count));
                logger.info("SupplyCrateActivity itemGetMap tick guildid:{}, value:{}", guildId, tmp);
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }


    @Subscribe
    public void onContinueLoginEvent(ContinueLoginEvent event){
        if(!isOpening(event.getPlayerId())){
            return;
        }
        Optional<SupplyCrateEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
        if (!opEntity.isPresent()) {
            return;
        }
        SupplyCrateEntity entity = opEntity.get();
        long now = HawkTime.getMillisecond();
        if(HawkTime.isSameDay(now, entity.getResetTime())){
            return;
        }
        entity.setResetTime(now);
        if (entity.getItemList().isEmpty()) {
            //初始化成就数据
            this.initAchieve(event.getPlayerId());
        }
        //数据有变化的成就，需要推送给前端
        List<AchieveItem> needPushList = new ArrayList<>();
        //遍历成就数据
        for(AchieveItem item : entity.getItemList()){
            SupplyCrateAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SupplyCrateAchieveCfg.class, item.getAchieveId());
            if(cfg == null){
                continue;
            }
            if(cfg.getIsDaily() == 0){
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
            AchievePushHelper.pushAchieveUpdate(event.getPlayerId(), needPushList);
        }
        postCommonLoginEvent(event.getPlayerId());
    }

    @Subscribe
    public void onVitCostEvent(VitCostEvent event){
        if(!isOpening(event.getPlayerId())){
            return;
        }
        String guildId = this.getDataGeter().getGuildId(event.getPlayerId());
        if(!HawkOSOperator.isEmptyString(guildId)){
            queue2.add(new HawkTuple3<>(guildId, event.getPlayerId(), event.getCost()));
        }
    }

    @Subscribe
    public void onRechargeMoneyEvent(RechargeMoneyEvent event){
        if(!isOpening(event.getPlayerId())){
            return;
        }
        String guildId = this.getDataGeter().getGuildId(event.getPlayerId());
        if(!HawkOSOperator.isEmptyString(guildId)){
            queue1.add(new HawkTuple2<>(guildId, event.getPlayerId()));
        }
    }
    
    @Subscribe
    public void onRechargeMoneyEvent(ShareProsperityEvent event){
        if(!isOpening(event.getPlayerId())){
            return;
        }
        String guildId = this.getDataGeter().getGuildId(event.getPlayerId());
        if(!HawkOSOperator.isEmptyString(guildId)){
            queue1.add(new HawkTuple2<>(guildId, event.getPlayerId()));
        }
        
        Optional<SupplyCrateEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
        if (!opEntity.isPresent()) {
            return;
        }
        SupplyCrateEntity entity = opEntity.get();
        AchieveManager.getInstance().onSpecialAchieve(this, event.getPlayerId(), entity.getItemList(), AchieveType.ACCUMULATE_DIAMOND_RECHARGE, event.getDiamondNum());
        entity.notifyUpdate();
    }

    @Subscribe
    public void onSupplyCrateGuildBoxEvent(SupplyCrateGuildBoxEvent event) {
        if (!isOpening(event.getPlayerId())) {
            return;
        }
        Optional<SupplyCrateEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
        if (opEntity.isPresent()) {
            SupplyCrateEntity entity = opEntity.get();
            int before = entity.getBoxCount();
            entity.addBoxProg(event.getNum());
            int after = entity.getBoxCount();
            String guildId = getDataGeter().getGuildId(event.getPlayerId());
            Map<String, Object> param = new HashMap<>();
            param.put("guildId", guildId == null ? "" : guildId);//联盟id
            param.put("before", before);//得分前
            param.put("after", after);//得分后
            getDataGeter().logActivityCommon(entity.getPlayerId(), LogConst.LogInfoType.supply_crate_score_guild, param);
            syncActivityDataInfo(event.getPlayerId());
        }else {
            ActivityGlobalRedis.getInstance().hIncrBy(SUPPY_CRATE_PLAYER_BOX, event.getPlayerId(), event.getNum());
        }
    }

    @Subscribe
    public void onJoinGuildEvent(JoinGuildEvent event) {
        String playerId = event.getPlayerId();
        if (!isOpening(event.getPlayerId())) {
            return;
        }
        String guildId = this.getDataGeter().getGuildId(event.getPlayerId());
        if(HawkOSOperator.isEmptyString(guildId)){
            return;
        }
        Optional<SupplyCrateEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
        if (!opEntity.isPresent()) {
            return;
        }
        SupplyCrateEntity entity = opEntity.get();
        entity.setGuildBoxProg(itemGetMap.getOrDefault(guildId, 0));
        int vitTarget = 200;
        SupplyCrateKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SupplyCrateKVCfg.class);
        if(kvCfg != null){
            vitTarget = kvCfg.getVitTarget();
        }
        if(vitCostMap.containsKey(guildId)){
            Map<String, Integer> playerVitMap = vitCostMap.get(guildId);
            int vitCount = 0;
            for(String memberId : playerVitMap.keySet()){
                if(playerVitMap.get(memberId) >= vitTarget){
                    vitCount++;
                }
            }
            ActivityManager.getInstance().postEvent(new SupplyCrateGuildVitCostEvent(playerId, vitCount));
        }
        if(rechargePlayerMap.containsKey(guildId)){
            int rechageCount = rechargePlayerMap.get(guildId).size();
            ActivityManager.getInstance().postEvent(new SupplyCrateGuildRechargeEvent(playerId, rechageCount));
        }
        int itemCount = itemGetMap.getOrDefault(guildId, 0);
        ActivityManager.getInstance().postEvent(new SupplyCrateGuildItemGetEvent(playerId, itemCount));
    }

    public void pushEventToGuildActive(String guildId, int type, int count){
        Collection<String> guildMemberIds = this.getDataGeter().getGuildMemberIds(guildId);
        for (String playerId : guildMemberIds) {
            if(this.getDataGeter().isCrossPlayer(playerId)){
                continue;
            }
//            if(!this.getDataGeter().isOnlinePlayer(playerId)){
//                continue;
//            }
            switch (type){
                case 1:{
                    ActivityManager.getInstance().postEvent(new SupplyCrateGuildVitCostEvent(playerId, count));
                }
                break;
                case 2:{
                    ActivityManager.getInstance().postEvent(new SupplyCrateGuildRechargeEvent(playerId, count));
                }
                break;
                case 3:{
                    ActivityManager.getInstance().postEvent(new SupplyCrateGuildItemGetEvent(playerId, count));
                    //ActivityManager.getInstance().postEvent(new SupplyCrateGuildBoxEvent(playerId, count));
                }
                break;
                case 4:{
                    ActivityManager.getInstance().postEvent(new SupplyCrateGuildBoxEvent(playerId, count));
                }
                break;
            }
        }
    }

    public void updateRank(String guildId, String playerId, int score){
        int termId = this.getActivityTermId();
        String guildRankKey = redisRankKey + termId + ":" + guildId;
        RedisIndex index = ActivityGlobalRedis.getInstance().zrevrankAndScore(guildRankKey, playerId);
        int selfrank = -1;
        long selfscore = 0;
        if (index != null) {
            selfrank = index.getIndex().intValue() + 1;
            selfscore = RankScoreHelper.getRealScore(index.getScore().longValue());
        }
        ActivityGlobalRedis.getInstance().zadd(guildRankKey, RankScoreHelper.calcSpecialRankScore(selfscore + score), playerId);
    }

    public Result<Integer> info(String playerId){
        syncActivityDataInfo(playerId);
        return Result.success();
    }

    public Result<Integer> choose(String playerId, int index){
        Optional<SupplyCrateEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        SupplyCrateEntity entity = opEntity.get();
        if(entity.isCanOPen()){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        SupplyCrateDrawCfg cfg = SupplyCrateDrawCfg.getCfgByRound(entity.getRound());
        if(cfg == null){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        SupplyCrateItemObj itemObj = cfg.getCoustmItem(index);
        if(itemObj == null){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        entity.setCustomIndex(index);
        syncActivityDataInfo(playerId);
        return Result.success();
    }

    public Result<Integer> ready(String playerId){
        Optional<SupplyCrateEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        SupplyCrateEntity entity = opEntity.get();
        if(entity.isCanOPen()){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        SupplyCrateDrawCfg cfg = SupplyCrateDrawCfg.getCfgByRound(entity.getRound());
        if(cfg == null){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        entity.setCrateItemList(cfg.getDrawList(entity.getCustomIndex()));
        entity.setCanOPen(true);
        syncActivityDataInfo(playerId);
        return Result.success();
    }

    public Result<Integer> open(String playerId, int pos){
        Optional<SupplyCrateEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        SupplyCrateEntity entity = opEntity.get();
        if(!entity.isCanOPen()){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        for(SupplyCrateItemObj obj : entity.getOpenItemList()){
            if(obj.getPos() == pos){
                return Result.fail(Status.Error.SUPPLY_CRATE_HAS_OPENED_VALUE);
            }
        }
        SupplyCrateKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SupplyCrateKVCfg.class);
        if(kvCfg == null){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        SupplyCrateDrawCfg cfg = SupplyCrateDrawCfg.getCfgByRound(entity.getRound());
        if(cfg == null){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        int cost = cfg.getCostCount(entity.getOpenItemList().size());
        if(cost <= 0){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //兑换消耗
        boolean flag = this.getDataGeter().cost(playerId, kvCfg.getUseItemList(), cost, Action.SUPPLY_CRATE_OPEN_COST, true);
        //如果不够消耗，返回错误码
        if (!flag) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        SupplyCrateItemObj itemObj = draw(entity.getCrateItemList());
        if(itemObj == null){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        int count = 1;
        if(entity.isCanDouble()){
            count = 2;
            entity.setCanDouble(false);
        }
        float multi = 1f;
        itemObj.setPos(pos);
        entity.getOpenItemList().add(itemObj);
        if(itemObj.isGood()){
            entity.setCanNext(true);
            if(entity.getRound() > 1){
                multi = entity.getMult() / 10000f;
            }
        }
        if(itemObj.isDouble()){
            entity.setCanDouble(true);
        }
        entity.notifyUpdate();
        if(!itemObj.isDouble()){
            if(itemObj.getItemId() == kvCfg.getTargetItemId()){
                String guildId = this.getDataGeter().getGuildId(playerId);
                int realCount = (int)(itemObj.getCount() * count * multi);
                if(HawkOSOperator.isEmptyString(guildId)){
                    int before = entity.getBoxCount();
                    entity.addBoxProg(realCount);
                    int after = entity.getBoxCount();
                    Map<String, Object> param = new HashMap<>();
                    param.put("guildId", "");//联盟id
                    param.put("before", before);//得分前
                    param.put("after", after);//得分后
                    getDataGeter().logActivityCommon(playerId, LogConst.LogInfoType.supply_crate_score_guild, param);
                }else {
                    queue3.add(new HawkTuple2<>(guildId, realCount));
                    updateRank(guildId, playerId, realCount);
                }
                Map<String, Object> param = new HashMap<>();
                param.put("guildId", guildId == null ? "" : guildId);//联盟id
                param.put("score", realCount);//分数
                getDataGeter().logActivityCommon(playerId, LogConst.LogInfoType.supply_crate_score_self, param);
            }else {
                Reward.RewardItem.Builder reward = itemObj.getReward();
                reward.setItemCount((int)(reward.getItemCount() * count * multi));
                List<Reward.RewardItem.Builder> rewardList = new ArrayList<>();
                rewardList.add(reward);
                if(itemObj.isGood()){
                    this.getDataGeter().takeReward(playerId, rewardList, 1 ,Action.SUPPLY_CRATE_OPEN_GET,true, Reward.RewardOrginType.ACTIVITY_BIG_REWEARD);
                }else {
                    this.getDataGeter().takeReward(playerId, rewardList, 1 ,Action.SUPPLY_CRATE_OPEN_GET,true);
                }

            }
        }
        syncActivityDataInfo(playerId);
        return Result.success();
    }

    public SupplyCrateItemObj draw(List<SupplyCrateItemObj> drawList){
        if(drawList.isEmpty()){
            return null;
        }
        if(drawList.size() == 2){
            for (SupplyCrateItemObj itemObj : drawList) {
                if(itemObj.isDouble()){
                    drawList.remove(itemObj);
                    return itemObj;
                }
            }
        }
        int totalWeight = drawList.stream().mapToInt(SupplyCrateItemObj::getWeight).sum();
        int randomValue = HawkRand.randInt(totalWeight);
        int currentWeight = 0;
        for (SupplyCrateItemObj itemObj : drawList) {
            currentWeight += itemObj.getWeight();
            if (randomValue < currentWeight) {
                drawList.remove(itemObj);
                return itemObj;
            }
        }
        return null;
    }

    public Result<Integer> next(String playerId){
        Optional<SupplyCrateEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        SupplyCrateEntity entity = opEntity.get();
        if(!entity.isCanNext()){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        SupplyCrateKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SupplyCrateKVCfg.class);
        if(kvCfg == null){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        SupplyCrateDrawCfg nextCfg = SupplyCrateDrawCfg.getCfgByRound(entity.getRound() + 1);
        if(nextCfg == null){
            return Result.fail(Status.Error.SUPPLY_CRATE_NOT_NEXT_VALUE);
        }
        int multi = HawkRand.randomWeightObject(kvCfg.getFinalMultipleMap());
        entity.setRound(entity.getRound() + 1);
        entity.setCanOPen(false);
        entity.setCanNext(false);
        entity.setCanDouble(false);
        entity.setCustomIndex(0);
        entity.setCrateItemList(new ArrayList<>());
        entity.setOpenItemList(new ArrayList<>());
        entity.setMult(multi);
        syncActivityDataInfo(playerId);
        return Result.success();
    }

    public Result<Integer> buy(String playerId, int count){
        SupplyCrateKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SupplyCrateKVCfg.class);
        if(kvCfg == null){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        long now = HawkTime.getMillisecond();
        int termId = getActivityTermId();
        long endTime = getTimeControl().getEndTimeByTermId(termId);
        if(endTime - now > kvCfg.getCanBuyTime()){
            return Result.fail(Status.Error.SUPPLY_CRATE_LAST_DAY_VALUE);
        }
        Optional<SupplyCrateEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        SupplyCrateEntity entity = opEntity.get();
        for(AchieveItem item : entity.getItemList()){
            if(item.getState() != Activity.AchieveState.TOOK_VALUE){
                return Result.fail(Status.Error.SUPPLY_CRATE_ACHIEVE_NOT_FINISH_VALUE);
            }
        }
        SupplyCrateDrawCfg cfg = SupplyCrateDrawCfg.getCfgByRound(entity.getRound());
        if(cfg == null){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        int canBuy = 0;
        for(int i = entity.getOpenItemList().size(); i < 9; i++){
            int cost = cfg.getCostCount(i);
            canBuy = canBuy + cost;
        }
        int curCount = this.getDataGeter().getItemNum(playerId, kvCfg.getTargetItemId());
        canBuy = canBuy - curCount;
        if(count > canBuy){
            return Result.fail(Status.Error.SUPPLY_CRATE_BUY_LIMIT_VALUE);
        }
        //兑换消耗
        boolean flag = this.getDataGeter().cost(playerId, kvCfg.getBuyGoldList(), count, Action.SUPPLY_CRATE_BUY_COST, true);
        //如果不够消耗，返回错误码
        if (!flag) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        this.getDataGeter().takeReward(playerId, kvCfg.getUseItemList(), count,Action.SUPPLY_CRATE_BUY_GET,true);
        return Result.success();
    }

    public Result<Integer> guildBox(String playerId){
        Optional<SupplyCrateEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        SupplyCrateEntity entity = opEntity.get();
        SupplyCrateKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SupplyCrateKVCfg.class);
        if(kvCfg == null){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        if(entity.getBoxProg() < kvCfg.getTargetItemCount()){
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
//        if(entity.getBoxCount() >= kvCfg.getBoxMax()){
//            return Result.fail(Status.Error.SUPPLY_CRATE_GUILD_BOX_LIMIT_VALUE);
//        }
//        entity.setBoxCount(entity.getBoxCount() + 1);
        entity.setBoxProg(entity.getBoxProg() - kvCfg.getTargetItemCount());
        this.getDataGeter().takeReward(playerId, kvCfg.getAward(), 1, Action.SUPPLY_CRATE_BOX_GET,true);
        syncActivityDataInfo(playerId);
        return Result.success();
    }

    public Result<Integer> rank(String playerId){
        String guildId = this.getDataGeter().getGuildId(playerId);
        if(HawkOSOperator.isEmptyString(guildId)){
            return Result.success();
        }
        SupplyCrateKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SupplyCrateKVCfg.class);
        if(kvCfg == null){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        Activity.SupplyCrateRankResp.Builder builder = Activity.SupplyCrateRankResp.newBuilder();
        int termId = this.getActivityTermId();
        String guildRankKey = redisRankKey + termId + ":" + guildId;
        RedisIndex index = ActivityGlobalRedis.getInstance().zrevrankAndScore(guildRankKey, playerId);
        int selfrank = -1;
        long selfscore = 0;
        if (index != null) {
            selfrank = index.getIndex().intValue() + 1;
            selfscore = RankScoreHelper.getRealScore(index.getScore().longValue());
        }
        String guildName = this.getDataGeter().getGuildName(guildId);
        String guildTag = this.getDataGeter().getGuildTag(guildId);
        Set<Tuple> rankSet = ActivityGlobalRedis.getInstance().getRedisSession().zRevrangeWithScores(guildRankKey, 0, kvCfg.getRankMax() - 1, 0);
        Activity.PlanetExploreScoreRank.Builder self = null;
        int rankIndex = 1;
        for (Tuple rankTuple : rankSet) {
            try {
                Activity.PlanetExploreScoreRank.Builder rankBuilder = Activity.PlanetExploreScoreRank.newBuilder();
                rankBuilder.setRank(rankIndex);
                rankBuilder.setScore(RankScoreHelper.getRealScore((long) rankTuple.getScore()));
                rankBuilder.setPlayerId(rankTuple.getElement());
                rankBuilder.setPlayerName(this.getDataGeter().getPlayerName(rankTuple.getElement()));
                rankBuilder.setGuildName(guildName);
                rankBuilder.setGuildTag(guildTag);
                if(playerId.equals(rankTuple.getElement())){
                    self = rankBuilder;
                }
                builder.addRank(rankBuilder);
            }catch (Exception e){
                HawkException.catchException(e);
            }
            rankIndex++;
        }
        if(self == null){
            self = Activity.PlanetExploreScoreRank.newBuilder();
            self.setRank(selfrank);
            self.setScore(selfscore);
            self.setPlayerId(playerId);
            self.setPlayerName(this.getDataGeter().getPlayerName(playerId));
            self.setGuildName(guildName);
            self.setGuildTag(guildTag);
        }
        builder.setSelfRank(self);
        pushToPlayer(playerId, HP.code2.SUPPLY_CRATE_RANK_RESP_VALUE, builder);
        return Result.success();
    }
}
