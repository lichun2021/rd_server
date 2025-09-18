package com.hawk.activity.type.impl.luckGetGold;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.*;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.backFlow.chemistry.ChemistryActivity;
import com.hawk.activity.type.impl.luckGetGold.cfg.*;
import com.hawk.activity.type.impl.luckGetGold.data.LuckGetGoldAction;
import com.hawk.activity.type.impl.luckGetGold.entity.LuckGetGoldEntity;
import com.hawk.game.protocol.*;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class LuckGetGoldActivity extends ActivityBase implements AchieveProvider {

    private int globalGoldPool = 0;
    private boolean isInit = false;
    /**
     * 构造函数
     * @param activityId 活动id
     * @param activityEntity 活动数据库实体
     */
    public LuckGetGoldActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    /**
     * 获得活动类型
     * @return 活动类型
     */
    @Override
    public ActivityType getActivityType() {
        return ActivityType.LUCK_GET_GOLD;
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
        LuckGetGoldActivity activity = new LuckGetGoldActivity(config.getActivityId(), activityEntity);
        //注册活动
        AchieveContext.registeProvider(activity);
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
        List<LuckGetGoldEntity> queryList = HawkDBManager.getInstance()
                .query("from LuckGetGoldEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        //如果有数据的话，返回第一个数据
        if (queryList != null && queryList.size() > 0) {
            LuckGetGoldEntity entity = queryList.get(0);
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
        LuckGetGoldEntity entity = new LuckGetGoldEntity(playerId, termId);
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
        Optional<LuckGetGoldEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        //获得玩家活动数据实体
        LuckGetGoldEntity entity = opEntity.get();
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
        Optional<LuckGetGoldEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        LuckGetGoldEntity entity = opEntity.get();
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        ConfigIterator<LuckGetGoldAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(LuckGetGoldAchieveCfg.class);
        List<AchieveItem> list = new ArrayList<>();
        while (iterator.hasNext()) {
            LuckGetGoldAchieveCfg cfg = iterator.next();
            AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
            list.add(item);
        }
        entity.setItemList(list);
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()));
    }

    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        LuckGetGoldAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(LuckGetGoldAchieveCfg.class, achieveId);
        return cfg;
    }

    @Override
    public Action takeRewardAction() {
        return Action.LUCK_GET_GOLD_ACHIEVE_REWARD;
    }

    @Override
    public Result<?> onTakeReward(String playerId, int achieveId) {
        Optional<LuckGetGoldEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        LuckGetGoldEntity entity = opEntity.get();
        LuckGetGoldAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(LuckGetGoldAchieveCfg.class, achieveId);
        if(entity.getAchieveChoose() != 0 && entity.getAchieveChoose() != cfg.getType()){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        entity.setAchieveChoose(cfg.getType());
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.LUCK_GET_GOLD_SYNC, genActivityInfo(entity)));
        return Result.success();
    }



    @Override
    public boolean isActivityClose(String playerId) {
        if("".equals(playerId)){
            return false;
        }
        LuckGetGoldKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
        if(kvCfg.getBaseLevel() > getDataGeter().getConstructionFactoryLevel(playerId)){
            return true;
        }
        if(kvCfg.getVipLevel() > getDataGeter().getVipLevel(playerId)){
            return true;
        }
        Optional<ChemistryActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.CHEMISTRY.intValue());
        if (opActivity.isPresent()) {
            ChemistryActivity activity = opActivity.get();
            if(activity.isOpening(playerId) && !activity.isHidden(playerId)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onOpen() {
        addServerToPool();
        LuckGetGoldKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
        String globalGold = ActivityGlobalRedis.getInstance().getRedisSession().getString(getGoldPoolKey());
        if(HawkOSOperator.isEmptyString(globalGold)){
            globalGoldPool = cfg.getStartGold();
        }else {
            globalGoldPool = cfg.getStartGold() + Integer.parseInt(globalGold);
        }
        calTickIndex = 0;
        normalTickIndex = 0;
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for(String playerId : onlinePlayerIds){
            callBack(playerId, GameConst.MsgId.ON_LUCK_GET_GOLD_SYNC, () -> {
                resetFreeCount(playerId);
                syncActivityDataInfo(playerId);
                initAchieve(playerId);
            });
        }
    }

    @Override
    public void onPlayerLogin(String playerId) {
        if(!isOpening(playerId)){
            return;
        }
        Optional<LuckGetGoldEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        LuckGetGoldEntity entity = opEntity.get();
        ActivityManager.getInstance().postEvent(new LuckGetGoldDrawEvent(playerId, entity.getTotalDrawCount()));
    }

    @Override
    public void onTick() {
//        int gold = getGoldPool();
//        if(gold != lastGoldPool){
//            lastGoldPool = gold;
//            Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
//            for(String playerId : onlinePlayerIds){
//                callBack(playerId, GameConst.MsgId.ON_LUCK_GET_GOLD_SYNC, () -> {
//                    syncActivityDataInfo(playerId);
//                });
//            }
//        }
    }

    private long lastTickTime = 0l;
    private String calServer = "";

    @Override
    public void onQuickTick() {
        long now = HawkTime.getMillisecond();
        LuckGetGoldKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
        if(now - lastTickTime < kvCfg.getTickPeriod()){
            return;
        }
        lastTickTime = now;
        if(!isInit){
            isInit = true;
            init();
        }
        if(!isShow()){
            return;
        }
        if(isCalServer()){
            calTick();
        }
        normalTick();
    }

    private boolean canTick(long now){
        int termId = this.getActivityTermId();
        if (termId > 0) {
            return true;
        }
        LuckGetGoldTimeCfg timeCfg = getLastTimeCfg(now);
        if(timeCfg == null){
            return false;
        }
        return now <= timeCfg.getHiddenTimeValue() + TimeUnit.MINUTES.toMinutes(10);
    }

    private LuckGetGoldTimeCfg getLastTimeCfg(long curTime) {
        LuckGetGoldTimeCfg lastCfg = null;
        List<LuckGetGoldTimeCfg> list = HawkConfigManager.getInstance()
                .getConfigIterator(LuckGetGoldTimeCfg.class).toList();
        for(LuckGetGoldTimeCfg cfg : list){
            if(cfg.getHiddenTimeValue() < curTime){
                if(lastCfg == null){
                    lastCfg = cfg;
                }
                if(cfg.getTermId() > lastCfg.getTermId()){
                    lastCfg = cfg;
                }
            }
        }
        return lastCfg;
    }

    private void init(){
        LuckGetGoldKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
        String areaId = getDataGeter().getAreaId();
        calServer = cfg.getCalServer(areaId);
        if(!isOpening("")){
            return;
        }
        String globalGold = ActivityGlobalRedis.getInstance().getRedisSession().getString(getGoldPoolKey());
        if(HawkOSOperator.isEmptyString(globalGold)){
            globalGoldPool = cfg.getStartGold();
        }else {
            globalGoldPool = cfg.getStartGold() + Integer.parseInt(globalGold);
        }
        int termId = getActivityTermId();
        if(isCalServer()){
            String calTickIndexKey =  "LUCK_GET_GOLD:" + termId  + ":CAL_SERVER_INDEX";
            String calTickIndexStr = ActivityGlobalRedis.getInstance().getRedisSession().getString(calTickIndexKey);
            if(!HawkOSOperator.isEmptyString(calTickIndexStr)){
                calTickIndex = Integer.parseInt(calTickIndexStr);
            }
        }
        String normalTickIndexKey =  "LUCK_GET_GOLD:" + termId  + ":NORMAL_SERVER_INDEX:" + getDataGeter().getServerId();
        String normalTickIndexStr = ActivityGlobalRedis.getInstance().getRedisSession().getString(normalTickIndexKey);
        if(!HawkOSOperator.isEmptyString(normalTickIndexStr)){
            normalTickIndex = Integer.parseInt(normalTickIndexStr);
        }
    }

    private int calTickIndex = 0;

    private void calTick(){
        int lastGoldPool = globalGoldPool;
        LuckGetGoldKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
        int termId = getActivityTermId();
        int calTickEndIndex = calTickIndex + kvCfg.getMaxAction();
        List<String> actionStrList = ActivityGlobalRedis.getInstance().getRedisSession().lRange(getCalServerActionKey(), calTickIndex, calTickEndIndex - 1, 0);
        if(actionStrList.size() < kvCfg.getMaxAction()){
            calTickEndIndex = calTickIndex + actionStrList.size();
        }
        if(calTickIndex != calTickEndIndex){
            HawkLog.logPrintln("calTick index calTickIndex:{},calTickEndIndex:{}",calTickIndex, calTickEndIndex);
        }
        calTickIndex = calTickEndIndex;
        String calTickIndexKey =  "LUCK_GET_GOLD:" + termId  + ":CAL_SERVER_INDEX";
        ActivityGlobalRedis.getInstance().getRedisSession().setString(calTickIndexKey, String.valueOf(calTickIndex));
        List<String> respList = new ArrayList<>();
        for(String actionStr : actionStrList){
            try {
                if(HawkOSOperator.isEmptyString(actionStr)){
                    continue;
                }
                LuckGetGoldAction action = new LuckGetGoldAction();
                action.mergeFrom(actionStr);
                onCalAction(action);
                respList.add(action.serializ());
                HawkLog.logPrintln("calTick:{}", actionStr);
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
        String normalServerKey = "LUCK_GET_GOLD:" + termId  + ":NORMAL_SERVER";
        if(respList.size() > 0){
            ActivityGlobalRedis.getInstance().getRedisSession().rPush(normalServerKey, 0, respList.toArray(new String[respList.size()]));
        }
        if(lastGoldPool != globalGoldPool){
            Activity.LuckGetGoldSync.Builder builder = Activity.LuckGetGoldSync.newBuilder();
            builder.setPoolNum(globalGoldPool);
            builder.setPoolId(0);
            builder.setAchieveChoose(0);
            builder.setFreeCount(0);
            builder.setCanDrawNum(0);
            Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
            for(String playerId : onlinePlayerIds){
                callBack(playerId, GameConst.MsgId.ON_LUCK_GET_GOLD_SYNC, () -> {
                    PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.LUCK_GET_GOLD_POOL_SYNC, builder));
                });
            }
        }
    }


    private void onCalAction(LuckGetGoldAction action){
        try {
            int lastGoldPool = globalGoldPool;
            int calGold = globalGoldPool + action.getCostGold();
            int divideSum = 0;
            if(action.getDrawNum().size() > 0){
                for(int i = 0; i < action.getDrawNum().size(); i++){
                    int divide = calGold * action.getDivide().get(i) / 100;
                    action.getDrawGold().add(divide);
                    divideSum += divide;
                    calGold -= divide;
                }
            }
            LuckGetGoldKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
            globalGoldPool = addGoldPool(action.getCostGold() - divideSum);
            action.setLastPoolGold(lastGoldPool);
            action.setPoolGold(globalGoldPool);
//            Set<String> serverIds = getServerPool();
//            for(String serverId : serverIds){
//                addNormalAction(serverId, action);
//            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    private int normalTickIndex = 0;

    private void normalTick(){
        int lastGoldPool = globalGoldPool;
        LuckGetGoldKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
        int termId = getActivityTermId();
        int normalTickEndIndex = normalTickIndex + kvCfg.getMaxAction();
        String normalServerKey = "LUCK_GET_GOLD:" + termId  + ":NORMAL_SERVER";
        List<String> actionStrList = ActivityGlobalRedis.getInstance().getRedisSession().lRange(normalServerKey, normalTickIndex, normalTickEndIndex - 1, 0);
        if(actionStrList.size() < kvCfg.getMaxAction()){
            normalTickEndIndex = normalTickIndex + actionStrList.size();
        }
        if(normalTickIndex != normalTickEndIndex){
            HawkLog.logPrintln("normalTick index normalTickIndex:{},normalTickEndIndex:{}",normalTickIndex, normalTickEndIndex);
        }
        normalTickIndex = normalTickEndIndex;
        String tickIndexKey =  "LUCK_GET_GOLD:" + termId  + ":NORMAL_SERVER_INDEX:" + getDataGeter().getServerId();
        ActivityGlobalRedis.getInstance().getRedisSession().setString(tickIndexKey, String.valueOf(normalTickIndex));
        for(String actionStr : actionStrList){
            try {
                if (HawkOSOperator.isEmptyString(actionStr)) {
                    continue;
                }
                LuckGetGoldAction action = new LuckGetGoldAction();
                action.mergeFrom(actionStr);
                onNorMalAction(action);
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
        if(lastGoldPool != globalGoldPool){
            Activity.LuckGetGoldSync.Builder builder = Activity.LuckGetGoldSync.newBuilder();
            builder.setPoolNum(globalGoldPool);
            builder.setPoolId(0);
            builder.setAchieveChoose(0);
            builder.setFreeCount(0);
            builder.setCanDrawNum(0);
            Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
            for(String playerId : onlinePlayerIds){
                callBack(playerId, GameConst.MsgId.ON_LUCK_GET_GOLD_SYNC, () -> {
                    PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.LUCK_GET_GOLD_POOL_SYNC, builder));
                });
            }
        }
    }

    private void onNorMalAction(LuckGetGoldAction action){
        try {
            int lastGoldPool = action.getLastPoolGold();
            if(getDataGeter().getServerId().equals(action.getServerId())){
                if(action.getDrawNum().size() > 0) {
                    int calGoldPool = action.getLastPoolGold() + action.getCostGold();
                    for (int i = 0; i < action.getDrawNum().size(); i++) {
                        int drawNum = action.getDrawNum().get(i);
                        int divide = action.getDivide().get(i);
                        int drawGold = action.getDrawGold().get(i);
                        Activity.LuckGetGoldDrawRecord.Builder record = Activity.LuckGetGoldDrawRecord.newBuilder();
                        record.setLuckyNum(drawNum);
                        record.setGetGold(drawGold);
                        fillData(action.getPlayerId(), record);
                        addRecord(getServerKey(), record);
                        addRecord(getPlayerKey(action.getPlayerId()), record);
                        List<Reward.RewardItem.Builder> goldList = new ArrayList<>();
                        Reward.RewardItem.Builder goldItem = Reward.RewardItem.newBuilder();
                        goldItem.setItemType(Const.ItemType.PLAYER_ATTR_VALUE * GameConst.ITEM_TYPE_BASE);
                        goldItem.setItemId(Const.PlayerAttr.DIAMOND_VALUE);
                        goldItem.setItemCount(drawGold);
                        goldList.add(goldItem);
                        sendMailToPlayer(action.getPlayerId(), MailConst.MailId.LUCK_GET_GOLD_WIN, null, new Object[] { drawGold},
                                new Object[] { drawNum, divide, calGoldPool, drawGold, calGoldPool - drawGold},
                                goldList, false);
                        try {
                            Map<String, Object> param = new HashMap<>();
                            param.put("num", drawNum);
                            param.put("gold", drawGold);
                            param.put("before", calGoldPool);
                            param.put("after", calGoldPool - drawGold);
                            getDataGeter().logActivityCommon(action.getPlayerId(), LogInfoType.luck_get_gold_win, param);
                        }catch (Exception e){
                            HawkException.catchException(e);
                        }
                        calGoldPool -= drawGold;
                    }
                }
            }
            if(action.getDrawNum().size() > 0){
                for(int i = 0; i < action.getDrawNum().size(); i++){
                    int drawNum = action.getDrawNum().get(i);
                    int drawGold = action.getDrawGold().get(i);
                    //this.getDataGeter().addWorldBroadcastMsg(Const.ChatType.WORLD_HREF, Const.NoticeCfgId.LUCK_GET_GOLD_WIN, null, action.getPlayerName(), action.getDrawNum(), action.getDrawGold());
                    sendBroadcast(Const.NoticeCfgId.LUCK_GET_GOLD_WIN, null, action.getServerId(), action.getPlayerName(), drawNum, drawGold);
                    HawkLog.logPrintln("LuckGetGoldActivity whc playerId:{},getGold :{}",action.getPlayerName(), drawNum);
                }

            }
            globalGoldPool = action.getPoolGold();
            LuckGetGoldKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
            if(lastGoldPool < kvCfg.getThreshold() && globalGoldPool >= kvCfg.getThreshold()){
                //发消息到世界聊天
                this.getDataGeter().addWorldBroadcastMsg(Const.ChatType.WORLD_HREF, Const.NoticeCfgId.LUCK_GET_GOLD_UP, null, action.getPoolGold());
                HawkLog.logPrintln("LuckGetGoldActivity whc poolGold :{}",action.getPoolGold());
            }
        }catch (Exception e){
            HawkException.catchException(e);
        }
    }

    private boolean isCalServer(){
        return getDataGeter().getServerId().equals(calServer);
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        Optional<LuckGetGoldEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        LuckGetGoldEntity entity = opEntity.get();
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.LUCK_GET_GOLD_SYNC, genActivityInfo(entity)));
    }

    @Subscribe
    public void onContinueLoginEvent(ContinueLoginEvent event) {
        //获取玩家id
        String playerId = event.getPlayerId();
        resetFreeCount(playerId);
        syncActivityDataInfo(playerId);
    }

    @Subscribe
    public void onBuidingLevelUpEvent(BuildingLevelUpEvent event) {
        if(!isOpening(event.getPlayerId())){
            return;
        }
        //不是主城不管
        if(event.getBuildType() != Const.BuildingType.CONSTRUCTION_FACTORY_VALUE){
            return;
        }
        LuckGetGoldKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
        if(event.getLevel() >= kvCfg.getBaseLevel()){
            syncActivityStateInfo(event.getPlayerId());
        }
    }



    @Subscribe
    public void onVipLevelUpEvent(VipLevelupEvent event) {
        if(!isOpening(event.getPlayerId())){
            return;
        }
        LuckGetGoldKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
        if(event.getLevel() >= kvCfg.getVipLevel()){
            syncActivityStateInfo(event.getPlayerId());
        }
    }


    @Subscribe
    public void onWHCGMEvent(WHCGMEvent event) {
        if(!getDataGeter().isServerDebug()){
            return;
        }
        if(event.getType() != WHCGMEvent.LUCK_GET_GOLD_DRAW_TEN){
            return;
        }
        if(!isOpening(event.getPlayerId())){
            return;
        }
        drawGM(event.getPlayerId(), Activity.LuckGetGoldDrawType.LGGD_TEN);
    }

    public Result<Integer> drawGM(String playerId, Activity.LuckGetGoldDrawType type){
        int gold = getGoldPool();
        LuckGetGoldKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
        Optional<LuckGetGoldEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        LuckGetGoldEntity entity = opEntity.get();
        boolean isFree = false;
        if(type == Activity.LuckGetGoldDrawType.LGGD_ONE && entity.getFreeCount() < kvCfg.getFreeCount()){
            isFree = true;
        }
        Map<LuckGetGoldDrawCfg, Integer> itemMap = getItemRandomMap(entity.getPoolChoose());
        Map<LuckGetGoldNumCfg, Integer> numMap = getNumRandomMap(isFree);
        List<LuckGetGoldDrawCfg> itemList = new ArrayList<>();
        List<LuckGetGoldNumCfg> numList = new ArrayList<>();
        switch (type){
            case LGGD_ONE:{
                LuckGetGoldDrawCfg itemCfg = HawkRand.randomWeightObject(itemMap);
                LuckGetGoldNumCfg numCfg = HawkRand.randomWeightObject(numMap);
                if(itemCfg == null || numCfg == null){
                    return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
                }
                itemList.add(itemCfg);
                numList.add(numCfg);
                entity.setDailyDrawCount(entity.getDailyDrawCount() + 1);
                entity.setTotalDrawCount(entity.getTotalDrawCount() + 1);
                this.getDataGeter().takeReward(entity.getPlayerId(), kvCfg.getOneCostGetList(), 1 , Action.LUCK_GET_GOLD_DRAW_GOLD_REWARD, true, Reward.RewardOrginType.LUCK_GET_GOLD_DRAW);
                ActivityManager.getInstance().postEvent(new LuckGetGoldDrawEvent(playerId, entity.getTotalDrawCount()));
            }
            break;
            case LGGD_TEN:{
                for(int i = 0; i < 10; i++){
                    LuckGetGoldDrawCfg itemCfg = HawkRand.randomWeightObject(itemMap);
                    LuckGetGoldNumCfg numCfg = HawkRand.randomWeightObject(numMap);
                    if(itemCfg == null || numCfg == null){
                        return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
                    }
                    itemList.add(itemCfg);
                    numList.add(numCfg);
                }
                entity.setDailyDrawCount(entity.getDailyDrawCount() + 10);
                entity.setTotalDrawCount(entity.getTotalDrawCount() + 10);
                this.getDataGeter().takeReward(entity.getPlayerId(), kvCfg.getTenCostGetList(), 1 , Action.LUCK_GET_GOLD_DRAW_GOLD_REWARD, true, Reward.RewardOrginType.LUCK_GET_GOLD_DRAW);
                ActivityManager.getInstance().postEvent(new LuckGetGoldDrawEvent(playerId, entity.getTotalDrawCount()));
            }
            break;
        }
        List<Reward.RewardItem.Builder> rewardList = new ArrayList<>();
        Activity.LuckGetGoldDrawResp.Builder builder = Activity.LuckGetGoldDrawResp.newBuilder();
        for(LuckGetGoldDrawCfg itemCfg : itemList){
            for(Reward.RewardItem.Builder item : itemCfg.getGainItemList()){
                builder.addRewards(item);
                rewardList.add(item);
            }
        }
        this.getDataGeter().takeReward(entity.getPlayerId(), rewardList, 1 , Action.LUCK_GET_GOLD_DRAW_REWARD, true, Reward.RewardOrginType.LUCK_GET_GOLD_DRAW);
        String playerName = getDataGeter().getPlayerName(playerId);
        LuckGetGoldAction action = new LuckGetGoldAction();
        action.setServerId(getDataGeter().getServerId());
        action.setPlayerId(playerId);
        action.setPlayerName(playerName);
        for(LuckGetGoldNumCfg numCfg : numList){
            int num = 1;
            Activity.LuckGetGoldDrawRecord.Builder record = Activity.LuckGetGoldDrawRecord.newBuilder();
            String [] range = numCfg.getRange().split("_");
            if(range.length == 1){
                num = Integer.parseInt(range[0]);
            }else {
                num = HawkRand.randInt(Integer.parseInt(range[0]), Integer.parseInt(range[1]));
            }
            if(numCfg.getDivide() > 0){
                action.getDrawNum().add(num);
                action.getDivide().add(numCfg.getDivide());
            }
            record.setLuckyNum(num);
            record.setGetGold(numCfg.getDivide());
            builder.addRecords(record);
            if(numCfg.getDivide() <= 0){
                fillData(action.getPlayerId(), record);
                addRecord(getPlayerKey(action.getPlayerId()), record);
            }
        }
        switch (type){
            case LGGD_ONE:{
                if(!isFree){
                    action.setCostGold(kvCfg.getOneCostGold());
                    addCalAction(action);
                }
            }
            break;
            case LGGD_TEN:{
                action.setCostGold(kvCfg.getTenCostGold());
                addCalAction(action);
            }
            break;
        }
        for(LuckGetGoldDrawCfg itemCfg : itemList){
            if(!itemCfg.isCore()){
                continue;
            }
            //this.getDataGeter().addWorldBroadcastMsg(Const.ChatType.WORLD_HREF, Const.NoticeCfgId.LUCK_GET_GOLD_CORE_ITEM, null, playerName, itemCfg.getId());
            sendBroadcast(Const.NoticeCfgId.LUCK_GET_GOLD_CORE_ITEM, null, playerName, itemCfg.getId());
        }
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.LUCK_GET_GOLD_DRAW_RESP, builder));
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.LUCK_GET_GOLD_SYNC, genActivityInfo(entity)));
        return Result.success();
    }

    private void resetFreeCount(String playerId){
        long now = HawkTime.getMillisecond();
        Optional<LuckGetGoldEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        LuckGetGoldEntity entity = opEntity.get();
        if(!HawkTime.isSameDay(now, entity.getResetTime())){
            entity.setResetTime(now);
            entity.setFreeCount(0);
            entity.setDailyDrawCount(0);
        }
    }

    private Activity.LuckGetGoldSync.Builder genActivityInfo(LuckGetGoldEntity entity){
        LuckGetGoldKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
        Activity.LuckGetGoldSync.Builder builder = Activity.LuckGetGoldSync.newBuilder();
        builder.setPoolNum(globalGoldPool);
        builder.setPoolId(entity.getPoolChoose());
        builder.setAchieveChoose(entity.getAchieveChoose());
        int hour = HawkTime.getHour();
        if(hour < cfg.getDailyStartTime() || hour > cfg.getDailyEndTime()){
            builder.setFreeCount(0);
        }else {
            builder.setFreeCount(cfg.getFreeCount() - entity.getFreeCount());
        }
        builder.setCanDrawNum(Math.max(0, cfg.getDailyMax() - entity.getDailyDrawCount()));
        return builder;
    }

    private String getPlayerKey(String playerId) {
        int termId = getActivityTermId();
        return "LUCK_GET_GOLD:" + termId  + ":PLAYER_RECORD:"+playerId;
    }

    private String getServerKey() {
        int termId = getActivityTermId();
        return "LUCK_GET_GOLD:" + termId  + ":SERVER_RECORD";
    }

    private String getGoldPoolKey() {
        int termId = getActivityTermId();
        return "LUCK_GET_GOLD:" + termId  + ":GOLD_POOL";
    }

    private String getServerPoolKey() {
        int termId = getActivityTermId();
        return "LUCK_GET_GOLD:" + termId  + ":SERVER_POOL";
    }

    private String getNormalServerActionKey(String serverId) {
        int termId = getActivityTermId();
        return "LUCK_GET_GOLD:" + termId  + ":NORMAL_SERVER:"+serverId;
    }

    private String getCalServerActionKey() {
        int termId = getActivityTermId();
        return "LUCK_GET_GOLD:" + termId  + ":CAL_SERVER";
    }

    private void addServerToPool(){
        String serverId = getDataGeter().getServerId();
        ActivityGlobalRedis.getInstance().getRedisSession().sAdd(getServerPoolKey(), 0 , serverId);
    }

    private Set<String> getServerPool(){
        return ActivityGlobalRedis.getInstance().getRedisSession().sMembers(getServerPoolKey());
    }

    private void addCalAction(LuckGetGoldAction action){
        ActivityGlobalRedis.getInstance().getRedisSession().rPush(getCalServerActionKey(), 0, action.serializ());
    }

    private LuckGetGoldAction getCalAction(){
        String dataStr = lPop(getCalServerActionKey());
        if(HawkOSOperator.isEmptyString(dataStr)){
            return null;
        }
        LuckGetGoldAction action = new LuckGetGoldAction();
        action.mergeFrom(dataStr);
        return action;
    }

    private void addNormalAction(String serverId, LuckGetGoldAction action){
        ActivityGlobalRedis.getInstance().getRedisSession().lPush(getNormalServerActionKey(serverId), 0, action.serializ());
    }

    private LuckGetGoldAction getNormalAction(){
        String serverId = getDataGeter().getServerId();
        String dataStr = lPop(getNormalServerActionKey(serverId));
        if(HawkOSOperator.isEmptyString(dataStr)){
            return null;
        }
        LuckGetGoldAction action = new LuckGetGoldAction();
        action.mergeFrom(dataStr);
        return action;
    }

    private String lPop(String key){
        Jedis jedis = ActivityGlobalRedis.getInstance().getRedisSession().getJedis();
        if (jedis != null) {
            try {
                return jedis.lpop(key);
            } catch (Exception e) {
                HawkException.catchException(e);
            } finally {
                jedis.close();
            }
        }
        return null;
    }

    private int getGoldPool(){
        LuckGetGoldKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
        String val = ActivityGlobalRedis.getInstance().getRedisSession().getString(getGoldPoolKey());
        if(!HawkOSOperator.isEmptyString(val)){
            return Math.max(0, cfg.getStartGold() + Integer.parseInt(val));
        }
        return cfg.getStartGold();
    }

    private int addGoldPool(int add){
        LuckGetGoldKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
        long result = ActivityGlobalRedis.getInstance().getRedisSession()
                .increaseBy(getGoldPoolKey(), add, 0);
        return Math.max(0, cfg.getStartGold() + (int)result);
    }

    private void addRecord(String key, Activity.LuckGetGoldDrawRecord.Builder record){
        ActivityGlobalRedis.getInstance().getRedisSession().lPush(key.getBytes(), 0, record.build().toByteArray());
    }

    private List<Activity.LuckGetGoldDrawRecord.Builder> loadRecord(String key, int count){
        List<Activity.LuckGetGoldDrawRecord.Builder> list = new ArrayList<>();
        List<byte[]> records = ActivityGlobalRedis.getInstance().getRedisSession().lRange(key.getBytes(), 0, count, 0);
        for (byte[] bytes : records) {
            try {
                Activity.LuckGetGoldDrawRecord.Builder record = Activity.LuckGetGoldDrawRecord.newBuilder();
                record.mergeFrom(bytes);
                list.add(record);
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
        return list;
    }

    private void fillData(String playerId, Activity.LuckGetGoldDrawRecord.Builder record){
        record.setPlayerId(playerId);
        record.setName(getDataGeter().getPlayerName(playerId));
        record.setPfIcon(getDataGeter().getPfIcon(playerId));
        record.setIcon(getDataGeter().getIcon(playerId));
        record.setServerId(getDataGeter().getServerId());
        record.setTime(HawkTime.getMillisecond());
    }

    public Result<Integer> info(String playerId){
        Optional<LuckGetGoldEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        LuckGetGoldEntity entity = opEntity.get();
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.LUCK_GET_GOLD_INFO_RESP, genActivityInfo(entity)));
        Activity.LuckGetGoldTipsSync.Builder builder = Activity.LuckGetGoldTipsSync.newBuilder();
        List<Activity.LuckGetGoldDrawRecord.Builder> recordList = loadRecord(getServerKey(), 10);
        for(Activity.LuckGetGoldDrawRecord.Builder record : recordList){
            builder.addRecords(record);
        }PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.LUCK_GET_GOLD_TIPS_SYNC, builder));
        return Result.success();
    }

    public Result<Integer> choose(String playerId, int poolId){
        boolean isRight = false;
        ConfigIterator<LuckGetGoldDrawCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(LuckGetGoldDrawCfg.class);
        for(LuckGetGoldDrawCfg cfg : iterator){
            if(cfg.getPoolId() == poolId){
                isRight = true;
            }
        }
        if(!isRight){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        Optional<LuckGetGoldEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        LuckGetGoldEntity entity = opEntity.get();
        entity.setPoolChoose(poolId);
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.LUCK_GET_GOLD_CHOOSE_RESP, genActivityInfo(entity)));
        return Result.success();
    }

    public Result<Integer> draw(String playerId, Activity.LuckGetGoldDrawType type){
        if(!isOpening(playerId)){
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        LuckGetGoldKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
        int hour = HawkTime.getHour();
        if(hour < kvCfg.getDailyStartTime() || hour > kvCfg.getDailyEndTime()){
            return Result.fail(Status.Error.LUCK_GET_GOLD_DRAW_END_VALUE);
        }
        long now = HawkTime.getMillisecond();
        int termId = this.getActivityTermId();
        LuckGetGoldTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(LuckGetGoldTimeCfg.class, termId);
        if(now >= timeCfg.getEndTimeValue() - TimeUnit.MINUTES.toMillis(5)){
            return Result.fail(Status.Error.LUCK_GET_GOLD_DRAW_END_VALUE);
        }
        Optional<LuckGetGoldEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        LuckGetGoldEntity entity = opEntity.get();
        boolean isFree = false;
        if(type == Activity.LuckGetGoldDrawType.LGGD_ONE && entity.getFreeCount() < kvCfg.getFreeCount()){
            isFree = true;
        }
        Map<LuckGetGoldDrawCfg, Integer> itemMap = getItemRandomMap(entity.getPoolChoose());
        Map<LuckGetGoldNumCfg, Integer> numMap = getNumRandomMap(isFree);
        List<LuckGetGoldDrawCfg> itemList = new ArrayList<>();
        List<LuckGetGoldNumCfg> numList = new ArrayList<>();
        switch (type){
            case LGGD_ONE:{
                if (!isFree && entity.getDailyDrawCount() + 1 > kvCfg.getDailyMax()){
                    return Result.fail(Status.Error.LUCK_GET_GOLD_DAILY_MAX_LIMIT_VALUE);
                }
                LuckGetGoldDrawCfg itemCfg = HawkRand.randomWeightObject(itemMap);
                LuckGetGoldNumCfg numCfg = HawkRand.randomWeightObject(numMap);
                if(itemCfg == null || numCfg == null){
                    return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
                }
                itemList.add(itemCfg);
                numList.add(numCfg);
                if(entity.getFreeCount() < kvCfg.getFreeCount()){
                    entity.setFreeCount(entity.getFreeCount() + 1);
                }else {
                    //抽奖消耗
                    boolean flag = this.getDataGeter().cost(playerId, kvCfg.getOneCostList(), 1, Action.LUCK_GET_GOLD_DRAW_COST, true);
                    //如果不够消耗，返回错误码
                    if (!flag) {
                        return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                    }
                }
                entity.setDailyDrawCount(entity.getDailyDrawCount() + 1);
                entity.setTotalDrawCount(entity.getTotalDrawCount() + 1);
                this.getDataGeter().takeReward(entity.getPlayerId(), kvCfg.getOneCostGetList(), 1 , Action.LUCK_GET_GOLD_DRAW_GOLD_REWARD, true, Reward.RewardOrginType.LUCK_GET_GOLD_DRAW);
                ActivityManager.getInstance().postEvent(new LuckGetGoldDrawEvent(playerId, entity.getTotalDrawCount()));
            }
            break;
            case LGGD_TEN:{
                if (entity.getDailyDrawCount() + 10 > kvCfg.getDailyMax()){
                    return Result.fail(Status.Error.LUCK_GET_GOLD_DAILY_MAX_LIMIT_VALUE);
                }
                for(int i = 0; i < 10; i++){
                    LuckGetGoldDrawCfg itemCfg = HawkRand.randomWeightObject(itemMap);
                    LuckGetGoldNumCfg numCfg = HawkRand.randomWeightObject(numMap);
                    if(itemCfg == null || numCfg == null){
                        return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
                    }
                    itemList.add(itemCfg);
                    numList.add(numCfg);
                }
                //抽奖消耗
                boolean flag = this.getDataGeter().cost(playerId, kvCfg.getTenCostList(), 1, Action.LUCK_GET_GOLD_DRAW_COST, true);
                //如果不够消耗，返回错误码
                if (!flag) {
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                entity.setDailyDrawCount(entity.getDailyDrawCount() + 10);
                entity.setTotalDrawCount(entity.getTotalDrawCount() + 10);
                this.getDataGeter().takeReward(entity.getPlayerId(), kvCfg.getTenCostGetList(), 1 , Action.LUCK_GET_GOLD_DRAW_GOLD_REWARD, true, Reward.RewardOrginType.LUCK_GET_GOLD_DRAW);
                ActivityManager.getInstance().postEvent(new LuckGetGoldDrawEvent(playerId, entity.getTotalDrawCount()));
            }
            break;
        }
        List<Reward.RewardItem.Builder> rewardList = new ArrayList<>();
        Activity.LuckGetGoldDrawResp.Builder builder = Activity.LuckGetGoldDrawResp.newBuilder();
        for(LuckGetGoldDrawCfg itemCfg : itemList){
            for(Reward.RewardItem.Builder item : itemCfg.getGainItemList()){
                builder.addRewards(item);
                rewardList.add(item);
            }
        }
        this.getDataGeter().takeReward(entity.getPlayerId(), rewardList, 1 , Action.LUCK_GET_GOLD_DRAW_REWARD, true, Reward.RewardOrginType.LUCK_GET_GOLD_DRAW);
        String playerName = getDataGeter().getPlayerName(playerId);
        LuckGetGoldAction action = new LuckGetGoldAction();
        action.setServerId(getDataGeter().getServerId());
        action.setPlayerId(playerId);
        action.setPlayerName(playerName);
        for(LuckGetGoldNumCfg numCfg : numList){
            int num = 1;
            Activity.LuckGetGoldDrawRecord.Builder record = Activity.LuckGetGoldDrawRecord.newBuilder();
            String [] range = numCfg.getRange().split("_");
            if(range.length == 1){
                num = Integer.parseInt(range[0]);
            }else {
                num = HawkRand.randInt(Integer.parseInt(range[0]), Integer.parseInt(range[1]));
            }
            if(numCfg.getDivide() > 0){
                action.getDrawNum().add(num);
                action.getDivide().add(numCfg.getDivide());
            }
            record.setLuckyNum(num);
            record.setGetGold(numCfg.getDivide());
            builder.addRecords(record);
            if(numCfg.getDivide() <= 0){
                fillData(action.getPlayerId(), record);
                addRecord(getPlayerKey(action.getPlayerId()), record);
            }
            try {
                Map<String, Object> param = new HashMap<>();
                param.put("num", num);
                getDataGeter().logActivityCommon(playerId, LogInfoType.luck_get_gold_num, param);
            }catch (Exception e){
                HawkException.catchException(e);
            }
        }
        switch (type){
            case LGGD_ONE:{
                if(!isFree){
                    action.setCostGold(kvCfg.getOneCostGold());
                    addCalAction(action);
                }
            }
            break;
            case LGGD_TEN:{
                action.setCostGold(kvCfg.getTenCostGold());
                addCalAction(action);
            }
            break;
        }
        for(LuckGetGoldDrawCfg itemCfg : itemList){
            if(!itemCfg.isCore()){
                continue;
            }
            //this.getDataGeter().addWorldBroadcastMsg(Const.ChatType.WORLD_HREF, Const.NoticeCfgId.LUCK_GET_GOLD_CORE_ITEM, null, playerName, itemCfg.getId());
            sendBroadcast(Const.NoticeCfgId.LUCK_GET_GOLD_CORE_ITEM, null, playerName, itemCfg.getId());
        }
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.LUCK_GET_GOLD_DRAW_RESP, builder));
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.LUCK_GET_GOLD_SYNC, genActivityInfo(entity)));
        return Result.success();
    }

    private Map<LuckGetGoldDrawCfg, Integer> getItemRandomMap(int poolId){
        Map<LuckGetGoldDrawCfg, Integer> itemMap = new HashMap<>();
        ConfigIterator<LuckGetGoldDrawCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(LuckGetGoldDrawCfg.class);
        for(LuckGetGoldDrawCfg cfg : iterator) {
            if(cfg.getPoolId() != poolId){
                continue;
            }
            itemMap.put(cfg, cfg.getWeight());
        }
        return itemMap;
    }


    private Map<LuckGetGoldNumCfg, Integer> getNumRandomMap(boolean isFree){
        LuckGetGoldKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LuckGetGoldKVCfg.class);
        Map<LuckGetGoldNumCfg, Integer> numMap = new HashMap<>();
        ConfigIterator<LuckGetGoldNumCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(LuckGetGoldNumCfg.class);
        for(LuckGetGoldNumCfg cfg : iterator) {
            if(isFree && cfg.getDivide() > 0){
                continue;
            }
            if(globalGoldPool >= kvCfg.getThreshold()){
                numMap.put(cfg, cfg.getUpWeight());
            }else {
                numMap.put(cfg, cfg.getWeight());
            }
        }
        return numMap;
    }

    public Result<Integer> record(String playerId){
        Activity.LuckGetGoldRecordResp.Builder builder = Activity.LuckGetGoldRecordResp.newBuilder();
        List<Activity.LuckGetGoldDrawRecord.Builder> recordList = loadRecord(getServerKey(), 100);
        for(Activity.LuckGetGoldDrawRecord.Builder record : recordList){
            builder.addRecords(record);
        }
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.LUCK_GET_GOLD_RECORD_RESP, builder));
        return Result.success();
    }

    public Result<Integer> selfRecord(String playerId){
        Activity.LuckGetGoldSelfRecordResp.Builder builder = Activity.LuckGetGoldSelfRecordResp.newBuilder();
        List<Activity.LuckGetGoldDrawRecord.Builder> recordList = loadRecord(getPlayerKey(playerId), 1000);
        for(Activity.LuckGetGoldDrawRecord.Builder record : recordList){
            builder.addRecords(record);
        }
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.LUCK_GET_GOLD_SELF_RECORD_RESP, builder));
        return Result.success();
    }
}
