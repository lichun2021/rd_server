package com.hawk.activity.type.impl.pddActivity;

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
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.pddActivity.cfg.PDDAchieveCfg;
import com.hawk.activity.type.impl.pddActivity.cfg.PDDKVCfg;
import com.hawk.activity.type.impl.pddActivity.cfg.PDDShopCfg;
import com.hawk.activity.type.impl.pddActivity.cfg.PDDTimeCfg;
import com.hawk.activity.type.impl.pddActivity.data.PDDOrderData;
import com.hawk.activity.type.impl.pddActivity.data.PDDUserData;
import com.hawk.activity.type.impl.pddActivity.entity.PDDActivityEntity;
import com.hawk.game.protocol.*;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple2;
import org.hawk.uuid.HawkUUIDGenerator;
import redis.clients.jedis.Tuple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PDDActivity extends ActivityBase implements AchieveProvider {

    private static final int CREATER = 1;//发起
    private static final int PARTNER = 2;//参与
    private static final int SYSTEM = 3;//系统
    private Map<Integer, Integer> totalMap = new ConcurrentHashMap<>();
    private Map<Integer, Long> totalTimeMap = new ConcurrentHashMap<>();

    private long lastWaterFlood = 0;

    private Map<String, PDDUserData> userDataMap = new ConcurrentHashMap<>();

    private long userDataTime = 0;

    private List<Activity.PDDTipsSync.Builder> tipList1 = new ArrayList<>();

    private List<Activity.PDDTipsSync.Builder> tipList2 = new ArrayList<>();

    private long lastTipTime = 0;

    /**
     * 构造函数
     * @param activityId 活动id
     * @param activityEntity 活动数据库实体
     */
    public PDDActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    /**
     * 获得活动类型
     * @return 活动类型
     */
    @Override
    public ActivityType getActivityType() {
        return ActivityType.PDD_ACTIVITY;
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
        PDDActivity activity = new PDDActivity(config.getActivityId(), activityEntity);
        //注册活动
        AchieveContext.registeProvider(activity);
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
        List<PDDActivityEntity> queryList = HawkDBManager.getInstance()
                .query("from PDDActivityEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        //如果有数据的话，返回第一个数据
        if (queryList != null && queryList.size() > 0) {
            PDDActivityEntity entity = queryList.get(0);
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
        PDDActivityEntity entity = new PDDActivityEntity(playerId,termId);
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
        Optional<PDDActivityEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        //获得玩家活动数据实体
        PDDActivityEntity entity = opEntity.get();
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
        //获得玩家活动数据
        Optional<PDDActivityEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        PDDActivityEntity entity = opEntity.get();
        //如果成就数据为空就初始化成就数据
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        ConfigIterator<PDDAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(PDDAchieveCfg.class);
        List<AchieveItem> list = new ArrayList<>();
        for(PDDAchieveCfg cfg : iterator){
            AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
            list.add(item);
        }
        entity.setItemList(list);
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()));
    }


    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(PDDAchieveCfg.class, achieveId);
        return config;
    }

    @Override
    public Action takeRewardAction() {
        return Action.PDD_ACHIEVE_REWARD;
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        info(playerId);
    }

    public int getCurDay(){
        long now  = HawkTime.getMillisecond();
        int termId = getActivityTermId();
        long startTime = getTimeControl().getStartTimeByTermId(termId);
        startTime = HawkTime.getAM0Date(new Date(startTime)).getTime();
        return (int)Math.ceil((now - startTime) * 1.0f / TimeUnit.DAYS.toMillis(1));
    }

    public long getCurEndTime(){
        int curDay = getCurDay();
        int termId = getActivityTermId();
        long startTime = getTimeControl().getStartTimeByTermId(termId);
        startTime = HawkTime.getAM0Date(new Date(startTime)).getTime();
        return startTime + TimeUnit.DAYS.toMillis(curDay);
    }

    private String getTotalNumKey(int cfgId){
        int termId = getActivityTermId();
        return "PDD:" + termId + ":TOTAL_NUM:" + cfgId;
    }

    private String getUserOrderKey(String playerId){
        int termId = getActivityTermId();
        return "PDD:" + termId + ":USER_ORDER:" + playerId;
    }

    private String getUserInfoKey(){
        int termId = getActivityTermId();
        return "PDD:" + termId + ":USER_INFO";
    }

    private String getOrderStateKey(){
        int termId = getActivityTermId();
        return "PDD:" + termId + ":ORDER_STATE";
    }

    private String getOrderFeed(int cfgId){
        int termId = getActivityTermId();
        return "PDD:" + termId + ":ORDER_FEED:" + cfgId;
    }

    private String genOrderId(String playerId){
        String uuid = HawkUUIDGenerator.genUUID();
        return playerId +":"+ uuid;
    }

    private void addFeed(PDDOrderData data){
        ActivityGlobalRedis.getInstance().zadd(getOrderFeed(data.getCfgId()), data.getEndTime(),data.getOrderId());
    }

    private void removeFeed(PDDOrderData data){
        ActivityGlobalRedis.getInstance().zrem(getOrderFeed(data.getCfgId()), data.getOrderId());
    }

    private void saveOrder(PDDOrderData data){
        ActivityGlobalRedis.getInstance().hset(getUserOrderKey(data.getPlayerId()), data.getOrderId(), data.serializ());
    }

    private void saveOrderToPlayer(String playerId, PDDOrderData data){
        ActivityGlobalRedis.getInstance().hset(getUserOrderKey(playerId), data.getOrderId(), data.serializ());
    }

    private boolean lockOrder(String orderId, String playerId){
        return ActivityGlobalRedis.getInstance().getRedisSession().hSetNx(getOrderStateKey(), orderId, playerId) > 0;
    }

    private void sendTips(PDDOrderData data){
        Activity.PDDTipsSync.Builder builder = Activity.PDDTipsSync.newBuilder();
        PDDUserData userData = getUserData(data.getPlayerId());
        builder.setName(userData.getName());
        builder.setTime(data.getBuyTime());
        builder.setCfgId(data.getCfgId());
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for(String playerId : onlinePlayerIds){
            PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.PDD_TIPS_SYNC, builder));
        }
        tipList1.add(builder);
    }

    public void sendFirstTip(String playerId){
        if(tipList1.size() > 0) {
            Activity.PDDTipsSync.Builder builder = tipList1.get(0);
            PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.PDD_TIPS_SYNC, builder));
        }
    }
    private void saveUser(PDDUserData data){
        userDataMap.put(data.getPlayerId(), data);
        ActivityGlobalRedis.getInstance().hset(getUserInfoKey(), data.getPlayerId(), data.serializ());
    }

    public Map<String, PDDOrderData> loadAllUserOrder(String playerId){
        Map<String, PDDOrderData> rlt = new HashMap<>();
        Map<String,String> map = ActivityGlobalRedis.getInstance().hgetAll(getUserOrderKey(playerId));
        for(Map.Entry<String, String> entry : map.entrySet()){
            String value = entry.getValue();
            if(HawkOSOperator.isEmptyString(value)){
                continue;
            }
            PDDOrderData data = new PDDOrderData();
            data.mergeFrom(value);
            rlt.put(data.getOrderId(), data);
        }
        return rlt;
    }

    public Map<String, PDDOrderData> loadUserOrderByTermId(String playerId,int termId){
        Map<String, PDDOrderData> rlt = new HashMap<>();
        Map<String,String> map = ActivityGlobalRedis.getInstance().hgetAll("PDD:" + termId + ":USER_ORDER:" + playerId);
        for(Map.Entry<String, String> entry : map.entrySet()){
            String value = entry.getValue();
            if(HawkOSOperator.isEmptyString(value)){
                continue;
            }
            PDDOrderData data = new PDDOrderData();
            data.mergeFrom(value);
            rlt.put(data.getOrderId(), data);
        }
        return rlt;
    }

    public PDDOrderData loadUserOrderByOrderId(String orderId){
        String targetPlayerId = orderId.split(":")[0];
        return loadUserOrder(targetPlayerId, orderId);
    }

    public PDDOrderData loadUserOrder(String playerId, String orderId){
        String value = ActivityGlobalRedis.getInstance().hget(getUserOrderKey(playerId), orderId);
        if(HawkOSOperator.isEmptyString(value)){
            return null;
        }
        PDDOrderData data = new PDDOrderData();
        data.mergeFrom(value);
        return data;
    }

    public PDDUserData loadUser(String playerId){
        String value = ActivityGlobalRedis.getInstance().hget(getUserInfoKey(), playerId);
        if(HawkOSOperator.isEmptyString(value)){
            return null;
        }
        PDDUserData data = new PDDUserData();
        data.mergeFrom(value);
        return data;
    }

    public PDDUserData getUserData(String playerId){
        long now = HawkTime.getMillisecond();
        if(now - userDataTime> TimeUnit.MINUTES.toMillis(10)){
            userDataTime = now;
            userDataMap = new ConcurrentHashMap<>();
        }
        PDDUserData data = userDataMap.get(playerId);
        if(data != null){
            return data;
        }
        data = loadUser(playerId);
        if(data == null){
            return null;
        }
        userDataMap.put(playerId, data);
        return data;
    }

    public PDDUserData getRandomUserData(String playerId){
        PDDKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PDDKVCfg.class);
        if(userDataMap.containsKey(playerId) && userDataMap.size() == 1){
            Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
            List<String> randomList = new ArrayList<>(onlinePlayerIds);
            Collections.shuffle(randomList);
            String targetId = "";
            for(String id : randomList){
                if(!playerId.equals(id)){
                    targetId = id;
                    break;
                }
            }
            if(!HawkOSOperator.isEmptyString(targetId)){
                PDDUserData userData = new PDDUserData();
                userData.setPlayerId(targetId);
                String name = getDataGeter().getPlayerName(targetId);
                if(!kvCfg.isShowName()){
                    name = name.substring(0,1)+"***";
                }
                userData.setName(name);
                userData.setIcon(getDataGeter().getIcon(targetId));
                userData.setPfIcon(getDataGeter().getPfIcon(targetId));
                saveUser(userData);
                return userData;
            }
        }else {
            String targetId = "";
            for(String key : userDataMap.keySet()){
                if(!playerId.equals(key)){
                    targetId = key;
                    break;
                }
            }
            if(!HawkOSOperator.isEmptyString(targetId)){
                return userDataMap.get(targetId);
            }
        }
        return null;
    }

    public Activity.PDDOrderInfo.Builder fillOrderPb(PDDOrderData data){
        Activity.PDDOrderInfo.Builder info = data.toPB();
        PDDUserData userData = getUserData(data.getPlayerId());
        info.setOriginator(userData.toPB());
        if(!HawkOSOperator.isEmptyString(data.getPartnerId())){
            PDDUserData partner = getUserData(data.getPartnerId());
            info.setPartner(partner.toPB());
        }
        return info;
    }

    public void addTotal(int cfgId, int add){
        long result = ActivityGlobalRedis.getInstance().getRedisSession().increaseBy(getTotalNumKey(cfgId), add, (int)TimeUnit.DAYS.toSeconds(30));
        totalMap.put(cfgId, (int)result);
    }

    public void waterFlood(){
        boolean isCenter = false;
        long now = HawkTime.getMillisecond();
        PDDKVCfg pddkvCfg = HawkConfigManager.getInstance().getKVInstance(PDDKVCfg.class);
        String serverId = getDataGeter().getServerId();
        int areaId = Integer.valueOf(this.getDataGeter().getAreaId());
        //1微信 2手Q
        if(areaId == 1){
            if(serverId.equals(pddkvCfg.getWaterFloodWx())){
                isCenter = true;
            }
        }else {
            if(serverId.equals(pddkvCfg.getWaterFloodQq())){
                isCenter = true;
            }
        }
        if(isCenter){
            int hour = HawkTime.getHour();
            int curDay = getCurDay();
            ConfigIterator<PDDShopCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(PDDShopCfg.class);
            for(PDDShopCfg cfg : iterator) {
                if (cfg.getDay() != curDay) {
                    continue;
                }
                int cfgId = cfg.getId();
                HawkTuple2<Long, Integer> tuple2 = cfg.getWaterFlood(areaId, hour);
                long lastTIme = totalTimeMap.getOrDefault(cfg.getId(),0l);
                if(lastTIme == 0l){
                    totalTimeMap.put(cfgId, now);
                    continue;
                }
                if(now - lastTIme < tuple2.first){
                    continue;
                }
                totalTimeMap.put(cfgId, now);
                addTotal(cfgId, tuple2.second);
            }
        }else {
            if(now - lastWaterFlood < TimeUnit.MINUTES.toMillis(10)){
                return;
            }
            lastWaterFlood = now;
            ConfigIterator<PDDShopCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(PDDShopCfg.class);
            for(PDDShopCfg cfg : iterator) {
                int cfgId = cfg.getId();
                String val = ActivityGlobalRedis.getInstance().getRedisSession().getString(getTotalNumKey(cfgId));
                if(!HawkOSOperator.isEmptyString(val)){
                    int tmp = Integer.parseInt(val);
                    totalMap.put(cfgId, tmp);
                }
            }
        }
    }

    public void noticeTips(){
        long now = HawkTime.getMillisecond();
        if(now - lastTipTime < TimeUnit.SECONDS.toMillis(10)){
            return;
        }
        lastTipTime = now;
        if(tipList1.size() > 0){
            Activity.PDDTipsSync.Builder builder = tipList1.remove(0);
            Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
            for(String playerId : onlinePlayerIds){
                PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.PDD_TIPS_SYNC, builder));
            }
            tipList2.add(builder);
        }
        if(tipList1.size()<=0){
            tipList1 = tipList2;
            tipList2 = new ArrayList<>();
        }
    }

    @Override
    public void onTick() {
        waterFlood();
        noticeTips();
    }

    @Override
    public void onOpen() {
        tipList1 = new ArrayList<>();
        tipList2 = new ArrayList<>();
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for(String playerId : onlinePlayerIds){
            callBack(playerId, GameConst.MsgId.ON_PDD_ACTIVITY_OPEN, () -> {
                info(playerId);
                resetAchieve(playerId);
            });
        }
    }

    @Override
    public void onPlayerLogin(String playerId) {
        int termId = this.getActivityTermId();
        if (termId > 0) {
            onPlayerTick(playerId);
        }else {
            rewardSupplement(playerId);
        }
    }


    private void rewardSupplement(String playerId) {
        int lastTermId = this.getLastTermId();
        if (lastTermId <= 0) {
            return;
        }
        String spKey = playerId + ":pddRewardSupplement:" + lastTermId;
        String val = ActivityGlobalRedis.getInstance().getRedisSession().getString(spKey);
        if (StringUtils.isNotEmpty(val)){
            return;
        }
        ActivityGlobalRedis.getInstance().getRedisSession().setString(spKey, spKey, 30 * 24 * 3600);
        Map<String, PDDOrderData> orderDataMap = loadUserOrderByTermId(playerId, lastTermId);
        for(PDDOrderData data : orderDataMap.values()){
            if(data.getType() == Activity.PDDBuyType.PDD_CREATE_VALUE && data.getState() == Activity.PDDOrderState.PDD_DONE_VALUE && !data.isGet()){
                PDDShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PDDShopCfg.class, data.getCfgId());
                data.setIsGet(true);
                ActivityGlobalRedis.getInstance().hset("PDD:" + lastTermId + ":USER_ORDER:" + playerId, data.getOrderId(), data.serializ());
                if(cfg!=null){
                    sendMailToPlayer(playerId, MailConst.MailId.PDD_ORDER_GIVE, null, new Object[] { cfg.getId() }, new Object[] { cfg.getId() }, cfg.getGainItemList(), false);
                }
            }
            if(data.getType() == Activity.PDDBuyType.PDD_CREATE_VALUE && data.getState() == Activity.PDDOrderState.PDD_OPEN_VALUE && !data.isGet()){
                PDDShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PDDShopCfg.class, data.getCfgId());
                data.setIsGet(true);
                ActivityGlobalRedis.getInstance().hset("PDD:" + lastTermId + ":USER_ORDER:" + playerId, data.getOrderId(), data.serializ());
                if(cfg!=null){
                    sendMailToPlayer(playerId, MailConst.MailId.PDD_ORDER_FAIL, null, new Object[] { cfg.getId() }, new Object[] { cfg.getId() }, cfg.getNeedItemList(), false);
                }
                Map<String, Object> param = new HashMap<>();
                param.put("cfgId", cfg.getId());
                getDataGeter().logActivityCommon(playerId, LogInfoType.pdd_order_fail, param);
            }
        }
    }

    @Override
    public void onPlayerTick(String playerId) {
        if(!isOpening(playerId)){
            return;
        }
        Optional<PDDActivityEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        PDDActivityEntity entity = opEntity.get();
        long now = HawkTime.getMillisecond();
        Map<String, PDDOrderData> orderDataMap = loadAllUserOrder(playerId);
        Activity.PDDOrderListResp.Builder builder = Activity.PDDOrderListResp.newBuilder();
        for(PDDOrderData data : orderDataMap.values()){
            checkOrder(data, playerId, now, entity);
            Activity.PDDOrderInfo.Builder info = fillOrderPb(data);
            builder.addInfos(info);
        }
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.PDD_ORDER_LIST_RESP, builder));
    }

    private int getLastTermId() {
        long curTime = HawkTime.getMillisecond();
        PDDTimeCfg lastCfg = null;
        List<PDDTimeCfg> list = HawkConfigManager.getInstance()
                .getConfigIterator(PDDTimeCfg.class).toList();
        for(PDDTimeCfg cfg : list){
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

    private void checkOrder(PDDOrderData data, String playerId, long now, PDDActivityEntity entity){
        if(data.getState() == Activity.PDDOrderState.PDD_OPEN_VALUE) {
            if (data.getSuccessTime() > 0 && data.getSuccessTime() < now) {
                PDDUserData userData = getRandomUserData(playerId);
                if (userData != null) {
                    if (lockOrder(data.getOrderId(), playerId)) {
                        removeFeed(data);
                        data.setPartnerId(userData.getPlayerId());
                        data.setState(Activity.PDDOrderState.PDD_DONE_VALUE);
                        data.setBuyTime(now);
                        data.setSend(true);
                        saveOrder(data);
                        sendMailToPlayer(playerId, MailConst.MailId.PDD_ORDER_SUCCESS, null, new Object[] { data.getCfgId() ,userData.getName()}, new Object[] { data.getCfgId() ,userData.getName()}, Collections.emptyList(), true);
                        PDDShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PDDShopCfg.class, data.getCfgId());
                        int gold = 0;
                        if(cfg != null) {
							for(Reward.RewardItem.Builder item : cfg.getNeedItemList()){
                            	gold += item.getItemCount();
                        	}
						}
                        Map<String, Object> param = new HashMap<>();
                        param.put("cfgId", data.getCfgId());
                        param.put("cost", gold); //消耗金条
                        param.put("reason", SYSTEM);
                        getDataGeter().logActivityCommon(playerId, LogInfoType.pdd_order_done, param);
                        addTotal(data.getCfgId(), 1);
                    }
                }
            } else if (data.getEndTime() < now) {
                if (lockOrder(data.getOrderId(), playerId)) {
                    data.setState(Activity.PDDOrderState.PDD_CLOSE_VALUE);
                    data.setCancelTime(now);
                    saveOrder(data);
                    int num = entity.getBuyNumMap().getOrDefault(data.getCfgId(), 0);
                    if(num >0){
                        entity.getBuyNumMap().put(data.getCfgId(), num - 1);
                    }
                    entity.notifyUpdate();
                    PDDShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PDDShopCfg.class, data.getCfgId());
                    if(cfg!=null){
                        sendMailToPlayer(playerId, MailConst.MailId.PDD_ORDER_FAIL, null, new Object[] { cfg.getId() }, new Object[] { cfg.getId() }, cfg.getNeedItemList(), false);
                    }
                    Map<String, Object> param = new HashMap<>();
                    param.put("cfgId", cfg.getId());
                    getDataGeter().logActivityCommon(playerId, LogInfoType.pdd_order_fail, param);
                    try {
                        info(playerId);
                    }catch (Exception e){
                        HawkException.catchException(e);
                    }
                }
            }
        }else if(data.getType() == Activity.PDDBuyType.PDD_CREATE_VALUE && data.getState() == Activity.PDDOrderState.PDD_DONE_VALUE && !data.isSend()){
            data.setSend(true);
            saveOrder(data);
            PDDUserData pddUserData = getUserData(data.getPartnerId());
            String pddName = pddUserData == null ? "" : pddUserData.getName();
            sendMailToPlayer(playerId, MailConst.MailId.PDD_ORDER_SUCCESS, null, new Object[] { data.getCfgId() ,pddName}, new Object[] { data.getCfgId() ,pddName}, Collections.emptyList(), true);
            
            PDDShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PDDShopCfg.class, data.getCfgId());
            int gold = 0;
            if(cfg != null) {
				for(Reward.RewardItem.Builder item : cfg.getNeedItemList()){
                	gold += item.getItemCount();
            	}
			}
            Map<String, Object> param = new HashMap<>();
            param.put("cfgId", data.getCfgId());
            param.put("cost", gold); //消耗金条
            param.put("reason", CREATER);
            getDataGeter().logActivityCommon(playerId, LogInfoType.pdd_order_done, param);
        }
    }

    @Subscribe
    public void onContinueLoginEvent(ContinueLoginEvent event){
        //获取玩家id
        String playerId = event.getPlayerId();
        info(playerId);
        resetAchieve(playerId);
    }

    private void resetAchieve(String playerId){
        //判断活动是否处于开启状态
        if(!isOpening(playerId)){
            return;
        }
        //获得玩家活动数据
        Optional<PDDActivityEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        PDDActivityEntity entity = opEntity.get();
        long now = HawkTime.getMillisecond();
        if(HawkTime.isSameDay(now, entity.getResetTime())){
            return;
        }
        entity.setResetTime(now);
        if (entity.getItemList().isEmpty()) {
            //初始化成就数据
            this.initAchieve(playerId);
        }
        //数据有变化的成就，需要推送给前端
        List<AchieveItem> needPushList = new ArrayList<>();
        //遍历成就数据
        for(AchieveItem item : entity.getItemList()){
            PDDAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PDDAchieveCfg.class, item.getAchieveId());
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
        entity.setShareCount(0);
        entity.notifyUpdate();
        if(needPushList.size() > 0){
            //推送给前端
            AchievePushHelper.pushAchieveUpdate(playerId, needPushList);
        }
        ActivityManager.getInstance().postEvent(new PDDLoginEvent(playerId));
    }

    public Result<Integer> info(String playerId){
        //获得玩家活动数据
        Optional<PDDActivityEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        PDDActivityEntity entity = opEntity.get();
        int curDay = getCurDay();
        Activity.PDDInfoResp.Builder builder = Activity.PDDInfoResp.newBuilder();
        builder.setCurDay(curDay);
        ConfigIterator<PDDShopCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(PDDShopCfg.class);
        for(PDDShopCfg cfg : iterator){
//            if (cfg.getDay() != curDay){
//                continue;
//            }
            Activity.PDDBuyInfo.Builder info = Activity.PDDBuyInfo.newBuilder();
            info.setCfgId(cfg.getId());
            info.setCnt(entity.getBuyNumMap().getOrDefault(cfg.getId(), 0));
            info.setTotal(totalMap.getOrDefault(cfg.getId(), 0));
            builder.addInfos(info);
        }
        builder.setShareCount(entity.getShareCount());
        builder.setShareTime(entity.getShareTime());
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.PDD_INFO_RESP, builder));
        return Result.success();
    }

    public Result<Integer> orderList(String playerId){
        long now = HawkTime.getMillisecond();
        Optional<PDDActivityEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        PDDActivityEntity entity = opEntity.get();
        Activity.PDDOrderListResp.Builder builder = Activity.PDDOrderListResp.newBuilder();
        Map<String, PDDOrderData> orderDataMap = loadAllUserOrder(playerId);
        for(PDDOrderData data : orderDataMap.values()){
            checkOrder(data, playerId, now, entity);
            Activity.PDDOrderInfo.Builder info = fillOrderPb(data);
            builder.addInfos(info);
        }
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.PDD_ORDER_LIST_RESP, builder));
        return Result.success();
    }

    public Result<Integer> orderInfo(String playerId, String targetPlayer, String orderId){
        PDDOrderData data = loadUserOrder(targetPlayer, orderId);
        if(data == null){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        Activity.PDDOrderInfo.Builder info = fillOrderPb(data);
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.PDD_ORDER_INFO_RESP, info));
        return Result.success();
    }

    public Result<Integer> groupList(String playerId, int cfgId){
        Activity.PDDGroupListResp.Builder builder = Activity.PDDGroupListResp.newBuilder();
        String feedKey = getOrderFeed(cfgId);
        long now = HawkTime.getMillisecond();
        long begin = ActivityGlobalRedis.getInstance().getRedisSession().zCount(feedKey, 0, now);
        long end = ActivityGlobalRedis.getInstance().getRedisSession().zCount(feedKey, 0, Long.MAX_VALUE);
        int count = 0;
        OUT:
        for(long i = begin; i < end; i+=500){
            Set<Tuple> rankSet = ActivityGlobalRedis.getInstance().getRedisSession().zRangeWithScores(getOrderFeed(cfgId), i,  i + 499, 0);
            for (Tuple rank : rankSet) {
                String orderId = rank.getElement();
                if(orderId.startsWith(playerId)){
                    continue;
                }
                PDDOrderData data = loadUserOrderByOrderId(orderId);
                if(data == null){
                    continue;
                }
                if(data.getEndTime() <= now){
                    continue;
                }
                Activity.PDDOrderInfo.Builder info = fillOrderPb(data);
                builder.addInfos(info);
                count++;
                if(count >= 2){
                    break OUT;
                }
            }
        }
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.PDD_GROUP_LIST_RESP, builder));
        return Result.success();
    }

    public Result<Integer> buy(String playerId, Activity.PDDBuyType type, int cfgId, String targetPlayerID, String orderId, boolean isShare, Activity.PDDShareType shareType){
        if(!isOpening(playerId)){
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        PDDKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(PDDKVCfg.class);
        //获得玩家活动数据
        Optional<PDDActivityEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        PDDActivityEntity entity = opEntity.get();
        PDDShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PDDShopCfg.class, cfgId);
        if(cfg == null){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        int curDay = getCurDay();
        if(cfg.getDay() != curDay){
            return Result.fail(Status.Error.PDD_ORDER_END_VALUE);
        }
        int num = entity.getBuyNumMap().getOrDefault(cfgId, 0);
        if(num >= cfg.getTimes()){
            return Result.fail(Status.Error.PDD_BUY_COUNT_NOT_ENOUGH_VALUE);
        }
        PDDUserData userData = new PDDUserData();
        userData.setPlayerId(playerId);
        String name = getDataGeter().getPlayerName(playerId);
        if(!kvCfg.isShowName()){
            name = name.substring(0,1)+"***";
        }
        userData.setName(name);
        userData.setIcon(getDataGeter().getIcon(playerId));
        userData.setPfIcon(getDataGeter().getPfIcon(playerId));
        saveUser(userData);
        switch (type){
            case PDD_ALONE:{
                //兑换消耗
                boolean flag = this.getDataGeter().cost(playerId, cfg.getAloneNeedItemList(), 1, Action.PDD_COST, true);
                //如果不够消耗，返回错误码
                if (!flag) {
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                entity.getBuyNumMap().put(cfgId, num + 1);
                //发奖
                this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), 1, Action.PDD_GAIN, true);
                PDDOrderData data = new PDDOrderData();
                data.setPlayerId(playerId);
                data.setOrderId(genOrderId(playerId));
                data.setTermId(getActivityTermId());
                data.setCfgId(cfgId);
                data.setCreatTime(HawkTime.getMillisecond());
                data.setType(Activity.PDDBuyType.PDD_ALONE_VALUE);
                data.setState(Activity.PDDOrderState.PDD_DONE_VALUE);
                data.setIsGet(true);
                data.setEndTime(data.getCreatTime());
                data.setBuyTime(data.getCreatTime());
                saveOrder(data);
                //sendTips(data);
                //addTotal(cfgId, 1);
                //ActivityManager.getInstance().postEvent(new PDDOrderDoneEvent(playerId));
                int gold = 0;
                for(Reward.RewardItem.Builder item : cfg.getAloneNeedItemList()){
                    gold += item.getItemCount();
                }
                ActivityManager.getInstance().postEvent(new PDDGoldCostEvent(playerId, gold));
                Map<String, Object> param = new HashMap<>();
                param.put("cfgId", data.getCfgId());
                param.put("cost", gold); //消耗金条
                getDataGeter().logActivityCommon(playerId, LogInfoType.pdd_buy_alone, param);
            }
            break;
            case PDD_GROUP:{
                long now = HawkTime.getMillisecond();
                if(orderId.startsWith(playerId)){
                    return Result.fail(Status.Error.PDD_CAN_NOT_BUY_SELF_VALUE);
                }
                PDDOrderData data = loadUserOrder(targetPlayerID, orderId);
                if(data == null){
                    return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
                }
                if(data.getEndTime() <= now){
                    return Result.fail(Status.Error.PDD_ORDER_END_VALUE);
                }

                //兑换消耗
                boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), 1, Action.PDD_COST, true);
                //如果不够消耗，返回错误码
                if (!flag) {
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                entity.getBuyNumMap().put(cfgId, num + 1);
                if(lockOrder(data.getOrderId(), playerId)){
                    data.setPartnerId(playerId);
                    data.setState(Activity.PDDOrderState.PDD_DONE_VALUE);
                    data.setBuyTime(HawkTime.getMillisecond());
                    saveOrder(data);
                    removeFeed(data);
                }
                this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), 1, Action.PDD_GAIN, true);
                data.setPartnerId(playerId);
                data.setState(Activity.PDDOrderState.PDD_DONE_VALUE);
                data.setBuyTime(HawkTime.getMillisecond());
                data.setType(Activity.PDDBuyType.PDD_GROUP_VALUE);
                data.setIsGet(true);
                saveOrderToPlayer(playerId, data);
                sendTips(data);
                addTotal(cfgId, 1);
                ActivityManager.getInstance().postEvent(new PDDOrderDoneEvent(playerId));
                int gold = 0;
                for(Reward.RewardItem.Builder item : cfg.getNeedItemList()){
                    gold += item.getItemCount();
                }
                ActivityManager.getInstance().postEvent(new PDDGoldCostEvent(playerId, gold));
                PDDUserData pddUserData = getUserData(targetPlayerID);
                String pddName = pddUserData == null ? "" : pddUserData.getName();
                //sendMailToPlayer(playerId, MailConst.MailId.PDD_ORDER_SUCCESS, null, new Object[] { cfg.getId() ,pddName}, new Object[] { cfg.getId() ,pddName}, Collections.emptyList(), true);
                Map<String, Object> param = new HashMap<>();
                param.put("cfgId", data.getCfgId());
                param.put("cost", gold); //消耗金条
                param.put("reason", PARTNER);
                getDataGeter().logActivityCommon(playerId, LogInfoType.pdd_order_done, param);
            }
            break;
            case PDD_CREATE:{
                if(HawkTime.getMillisecond() + kvCfg.getCannotPddTime() > getCurEndTime()){
                    return Result.fail(Status.Error.PDD_ORDER_CREAT_TIME_LIMIT_VALUE);
                }
                int termId = this.getActivityTermId();
                PDDTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(PDDTimeCfg.class, termId);
                if(timeCfg == null){
                    return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
                }
                //兑换消耗
                boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), 1, Action.PDD_COST, true);
                //如果不够消耗，返回错误码
                if (!flag) {
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                entity.getBuyNumMap().put(cfgId, num + 1);
                long now = HawkTime.getMillisecond();
                long delayTime = 0;
                if(now >= timeCfg.getServerDownStartValue() && now <= timeCfg.getServerDownEndValue()){
                    delayTime = timeCfg.getDelayTime();
                }
                PDDOrderData data = new PDDOrderData();
                data.setPlayerId(playerId);
                data.setOrderId(genOrderId(playerId));
                data.setTermId(getActivityTermId());
                data.setCfgId(cfgId);
                data.setCreatTime(HawkTime.getMillisecond());
                data.setState(Activity.PDDOrderState.PDD_OPEN_VALUE);
                data.setType(Activity.PDDBuyType.PDD_CREATE_VALUE);
                data.setEndTime(data.getCreatTime() + cfg.getEndTime() + delayTime);
                if(kvCfg.isSuccess()){
                    data.setSuccessTime(data.getCreatTime() + cfg.getSuccessTime() + delayTime);
                }
                saveOrder(data);
                addFeed(data);
                if(isShare){
                    switch (shareType){
                        case PDD_GUILD:{
                            //获得联盟id
                            String guildId = this.getDataGeter().getGuildId(playerId);
                            //联盟id为空不可操作
                            if(!HawkOSOperator.isEmptyString(guildId)){
                                if(entity.addShare()){
                                    //发消息到联盟聊天
                                    this.getDataGeter().addWorldBroadcastMsg(Const.ChatType.GUILD_HREF, guildId,
                                            Const.NoticeCfgId.PDD_INViTE, playerId, data.getOrderId(), data.getCfgId(), data.getEndTime());
                                    ActivityManager.getInstance().postEvent(new PDDOrderShareEvent(playerId));
                                }
                            }
                        }
                        break;
                        case PDD_WORLD:{
                            if(entity.addShare()){
                                //发消息到世界聊天
                                this.getDataGeter().addWorldBroadcastMsg(Const.ChatType.WORLD_HREF, Const.NoticeCfgId.PDD_INViTE, playerId, data.getOrderId(), data.getCfgId(), data.getEndTime());
                                ActivityManager.getInstance().postEvent(new PDDOrderShareEvent(playerId));
                            }
                        }
                        break;
                        default:{
                        }
                        break;
                    }
                }
                int gold = 0;
                for(Reward.RewardItem.Builder item : cfg.getNeedItemList()){
                    gold += item.getItemCount();
                }
                ActivityManager.getInstance().postEvent(new PDDGoldCostEvent(playerId, gold));
                Map<String, Object> param = new HashMap<>();
                param.put("cfgId", data.getCfgId());
                getDataGeter().logActivityCommon(playerId, LogInfoType.pdd_order_create, param);
            }
            break;
        }
        entity.notifyUpdate();
        info(playerId);
        return Result.success();
    }

    public Result<Integer> share(String playerId, String orderId, Activity.PDDShareType type){
        //获得玩家活动数据
        Optional<PDDActivityEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        PDDActivityEntity entity = opEntity.get();
        //获得联盟id
        String guildId = this.getDataGeter().getGuildId(playerId);
        //联盟id为空不可操作
        if(type == Activity.PDDShareType.PDD_GUILD && HawkOSOperator.isEmptyString(guildId)){
            return Result.fail(Status.Error.DO_NOT_HAVE_A_GUILD_VALUE);
        }
        PDDOrderData data = loadUserOrder(playerId, orderId);
        if(data == null){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        long now = HawkTime.getMillisecond();
        PDDKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PDDKVCfg.class);
        if(cfg == null){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        if(now - entity.getShareTime() < cfg.getShareCdTime()){
            return Result.fail(Status.Error.PDD_ORDER_SHARE_CD_VALUE);
        }
        if(entity.getShareCount() >= cfg.getShareDailyNum()){
            return Result.fail(Status.Error.PDD_ORDER_SHARE_CONUT_LIMIT_VALUE);
        }
        entity.setShareCount(entity.getShareCount() + 1);
        entity.setShareTime(now);
        switch (type){
            case PDD_GUILD:{
                //发消息到联盟聊天
                this.getDataGeter().addWorldBroadcastMsg(Const.ChatType.GUILD_HREF, guildId,
                        Const.NoticeCfgId.PDD_INViTE, playerId, orderId, data.getCfgId(), data.getEndTime());
            }
            break;
            case PDD_WORLD:{
                //发消息到世界聊天
                this.getDataGeter().addWorldBroadcastMsg(Const.ChatType.WORLD_HREF, Const.NoticeCfgId.PDD_INViTE, playerId, data.getOrderId(), data.getCfgId(), data.getEndTime());
            }
            break;
            default:{
            }
            break;
        }

        ActivityManager.getInstance().postEvent(new PDDOrderShareEvent(playerId));
        info(playerId);
        return Result.success();
    }

    public Result<Integer> cancel(String playerId, String orderId){
        return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
//        //获得玩家活动数据
//        Optional<PDDActivityEntity> opEntity = getPlayerDataEntity(playerId);
//        //如果数据为空直接返回
//        if (!opEntity.isPresent()) {
//            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
//        }
//        //获得玩家活动数据实体
//        PDDActivityEntity entity = opEntity.get();
//        PDDOrderData data = loadUserOrder(playerId, orderId);
//        if(data == null){
//            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
//        }
//        if(data.getState() != Activity.PDDOrderState.PDD_OPEN_VALUE){
//            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
//        }
//        data.setState(Activity.PDDOrderState.PDD_CLOSE_VALUE);
//        data.setCancelTime(HawkTime.getMillisecond());
//        saveOrder(data);
//        PDDShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PDDShopCfg.class, data.getCfgId());
//        HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, entity.getPlayerId());
//        Reward.RewardOrginType orginType = Reward.RewardOrginType.ACTIVITY_REWARD;
//        PlayerRewardFromActivityMsg msg = PlayerRewardFromActivityMsg.valueOf(cfg.getNeedItemList(), Action.PDD_COST_CANCEL, true, orginType, 0);
//        HawkTaskManager.getInstance().postMsg(xid, msg);
//        return Result.success();
    }

    public Result<Integer> award(String playerId, String orderId){
        PDDOrderData data = loadUserOrder(playerId, orderId);
        if(data == null){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        if(data.isGet()){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        PDDShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PDDShopCfg.class, data.getCfgId());
        if(cfg == null){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        data.setIsGet(true);
        saveOrder(data);
        this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), 1, Action.PDD_GAIN, true);
        ActivityManager.getInstance().postEvent(new PDDOrderDoneEvent(playerId));
        PDDUserData pddUserData = getUserData(data.getPartnerId());
        String pddName = pddUserData == null ? "" : pddUserData.getName();
        //sendMailToPlayer(playerId, MailConst.MailId.PDD_ORDER_SUCCESS, null, new Object[] { cfg.getId() }, new Object[] { cfg.getId() }, Collections.emptyList(), true);
        return Result.success();
    }

    public void gmAddExpireOrder(int count, int cfgId){
        if(!getDataGeter().isServerDebug()){
            return;
        }
        for (int i = 0; i < count; i++){
            String playerId = "test";
            PDDOrderData data = new PDDOrderData();
            data.setPlayerId(playerId);
            data.setOrderId(genOrderId(playerId));
            data.setTermId(getActivityTermId());
            data.setCfgId(cfgId);
            data.setCreatTime(HawkTime.getMillisecond());
            data.setState(Activity.PDDOrderState.PDD_OPEN_VALUE);
            data.setType(Activity.PDDBuyType.PDD_CREATE_VALUE);
            data.setEndTime(HawkTime.getMillisecond());
            saveOrder(data);
            addFeed(data);
        }
    }
}
