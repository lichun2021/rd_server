package com.hawk.activity.type.impl.starLightSign;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.*;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.starLightSign.cfg.*;
import com.hawk.activity.type.impl.starLightSign.entity.StarLightSignActivityEntity;
import com.hawk.activity.type.impl.starLightSign.entity.StarLightSignItem;
import com.hawk.game.protocol.*;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class StarLightSignActivity extends ActivityBase implements AchieveProvider {
    private static final Logger logger = LoggerFactory.getLogger("Server");

    private static final int SIGN_REWARD = 1;
    private static final int MULTI_REWARD = 2;

    public StarLightSignActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.WORLD_HONOR_ACTIVITY;
    }

    @Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
    
    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        StarLightSignActivity activity = new StarLightSignActivity(config.getActivityId(), activityEntity);
        //注册当前活动到成就系统
        AchieveContext.registeProvider(activity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<StarLightSignActivityEntity> queryList = HawkDBManager.getInstance()
                .query("from StarLightSignActivityEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
            StarLightSignActivityEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        StarLightSignActivityEntity entity = new StarLightSignActivityEntity(playerId, termId);
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
        //获得玩家活动数据
        Optional<StarLightSignActivityEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        StarLightSignActivityEntity entity = opEntity.get();
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
        Optional<StarLightSignActivityEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        StarLightSignActivityEntity entity = opEntity.get();
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        ConfigIterator<StarLightSignMissionCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(StarLightSignMissionCfg.class);
        List<AchieveItem> list = new ArrayList<>();
        while (iterator.hasNext()){
            StarLightSignMissionCfg cfg = iterator.next();
            AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
            list.add(item);
        }
        entity.setItemList(list);
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()));
    }



    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        StarLightSignMissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(StarLightSignMissionCfg.class, achieveId);
        return cfg;
    }

    @Override
    public Action takeRewardAction() {
        return Action.STAR_LIGHT_SIGN_ACHIEVE_REWARD;
    }

    @Override
    public Result<?> onTakeReward(String playerId, int achieveId) {
        int curDay = curSignDay();
        if(curDay >= 8){
            return Result.fail(Status.Error.STAR_LIGHT_SIGN_EIGHT_VALUE);
        }
        return Result.success();
    }

    @Override
    public void onTakeRewardSuccessAfter(String playerId, List<Reward.RewardItem.Builder> reweardList, int achieveId) {
        StarLightSignMissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(StarLightSignMissionCfg.class, achieveId);
        if(cfg == null){
            return;
        }
        //获得玩家活动数据
        Optional<StarLightSignActivityEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        StarLightSignActivityEntity entity = opEntity.get();
        entity.setScore(entity.getScore() + cfg.getPoints());
        syncActivityInfo(entity);
    }

    public List<StarLightSignItem> getSignList(StarLightSignActivityEntity entity) {
        //如果成就数据为空就初始化成就数据
        if (entity.getSignList().isEmpty()) {
            //初始化成就数据
            this.initSignItem(entity);
        }
        return entity.getSignList();
    }

    private void initSignItem(StarLightSignActivityEntity entity) {
        if (!entity.getSignList().isEmpty()) {
            return;
        }
        List<StarLightSignItem> signItemList = new ArrayList<>();
        for(int i = 1; i <= Activity.StarlightSignType.STAR_LIGHT_SIGN_ADVANCE_VALUE; i++){
            for (int j = 1; j <= Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_ADVANCED_VALUE; j++){
                StarLightSignItem item = new StarLightSignItem();
                item.setType(i);
                item.setRechargeType(j);
                if(j == Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_ORDINARY_VALUE){
                    item.setIsBuy(1);
                }else {
                    item.setIsBuy(0);
                }
                item.setChoose(getDefaultChoose(i, j));
                signItemList.add(item);
            }
        }
        entity.setSignList(signItemList);
    }

    @Override
    public void onOpen() {
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for(String playerId : onlinePlayerIds){
            callBack(playerId, GameConst.MsgId.STAR_LIGHT_SIGN_INIT, () -> {
                this.syncActivityInfo(playerId);
                initAchieve(playerId);
                sign(playerId, curSignDay());
            });
        }
    }

    @Override
    public void onEnd() {
//        //获得当前期数
//        int termId = getActivityTermId();
//        List<StarLightSignActivityEntity> queryList = HawkDBManager.getInstance()
//                .query("from StarLightSignActivityEntity where termId = ? and invalid = 0", termId);
//        if(queryList!=null && queryList.size()>0){
//            for(StarLightSignActivityEntity dbEntity : queryList){
//                String playerId = dbEntity.getPlayerId();
//                //获得玩家活动数据
//                Optional<StarLightSignActivityEntity> opEntity = getPlayerDataEntity(playerId);
//                //如果数据为空直接返回
//                if (!opEntity.isPresent()) {
//                    continue;
//                }
//                StarLightSignActivityEntity entity = opEntity.get();
//                callBack(playerId, GameConst.MsgId.STAR_LIGHT_SIGN_END, () -> {
//                    checkMultiple(entity, true);
//                    List<Reward.RewardItem.Builder> awardList = new ArrayList<>();
//                    awardList.addAll(awardSign(entity, Activity.StarlightSignType.STAR_LIGHT_SIGN_NORMAL));
//                    awardList.addAll(awardSign(entity, Activity.StarlightSignType.STAR_LIGHT_SIGN_ADVANCE));
//                    awardList.addAll(awardMultiple(entity, Activity.StarlightSignType.STAR_LIGHT_SIGN_NORMAL));
//                    awardList.addAll(awardMultiple(entity, Activity.StarlightSignType.STAR_LIGHT_SIGN_ADVANCE));
//                    if(awardList.size() > 0){
//                        entity.notifyUpdate();
//                        sendMailToPlayer(entity.getPlayerId(), MailConst.MailId.STAR_LIGHT_SIGN_REWARD_2023321001, null, null, mergeReward(awardList));
//                    }
//                });
//            }
//        }
    }

    private void rewardSupplement(String playerId) {
        int termId = this.getActivityTermId();
        if (termId > 0) {
            return;
        }
        int lastTermId = this.getLastTermId();
        if (lastTermId <= 0) {
            return;
        }
        HawkDBEntity dbEntity = PlayerDataHelper.getInstance().getActivityDataEntity(playerId, getActivityType());
        if (dbEntity == null) {
            dbEntity = this.loadFromDB(playerId, lastTermId);
            if(null == dbEntity){
                return;
            }
            PlayerDataHelper.getInstance().putActivityDataEntity(playerId, getActivityType(), dbEntity);
        }
        StarLightSignActivityEntity entity = (StarLightSignActivityEntity)dbEntity;
        checkMultiple(entity, true);
        List<Reward.RewardItem.Builder> awardList = new ArrayList<>();
        awardList.addAll(awardSign(entity, Activity.StarlightSignType.STAR_LIGHT_SIGN_NORMAL));
        awardList.addAll(awardSign(entity, Activity.StarlightSignType.STAR_LIGHT_SIGN_ADVANCE));
        awardList.addAll(awardMultiple(entity, Activity.StarlightSignType.STAR_LIGHT_SIGN_NORMAL));
        awardList.addAll(awardMultiple(entity, Activity.StarlightSignType.STAR_LIGHT_SIGN_ADVANCE));
        if(awardList.size() > 0){
            entity.notifyUpdate();
            sendMailToPlayer(entity.getPlayerId(), MailConst.MailId.STAR_LIGHT_SIGN_REWARD_2023321001, null, null, mergeReward(awardList));
        }
    }

    private int getLastTermId() {
        long curTime = HawkTime.getMillisecond();
        StarLightSignTimeCfg lastCfg = null;
        List<StarLightSignTimeCfg> list = HawkConfigManager.getInstance()
                .getConfigIterator(StarLightSignTimeCfg.class).toList();
        for(StarLightSignTimeCfg cfg : list){
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
    public void onPlayerLogin(String playerId) {
        this.rewardSupplement(playerId);
        if (!isOpening(playerId)) {
            return;
        }
        sign(playerId, curSignDay());
    }

    @Subscribe
    public void onContinueLoginEvent(ContinueLoginEvent event) {
        if (!event.isCrossDay()) {
            return;
        }
        //获取玩家id
        String playerId = event.getPlayerId();
        //判断活动是否处于开启状态
        if(!isOpening(playerId)){
            return;
        }
        //获得玩家活动数据
        Optional<StarLightSignActivityEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        StarLightSignActivityEntity entity = opEntity.get();
        //数据有变化的成就，需要推送给前端
        List<AchieveItem> needPushList = new ArrayList<>();
        //遍历成就数据
        for(AchieveItem item : entity.getItemList()){
            StarLightSignMissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(StarLightSignMissionCfg.class, item.getAchieveId());
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
        //推送给前端
        AchievePushHelper.pushAchieveUpdate(playerId, needPushList);
        sign(playerId, curSignDay());
    }


    @Subscribe
    public void onAuthBuyEvent(StarLightSignBuyEvent event) {
        if(!checkAuthBuy(event.getPlayerId(),event.getPayGiftId())){
            return;
        }
        int id = Integer.valueOf(event.getPayGiftId());
        StarLightSignPayGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(StarLightSignPayGiftCfg.class, id);
        //如果配置为空不可购买
        if(cfg == null){
            return;
        }
        Optional<StarLightSignActivityEntity> optional = this.getPlayerDataEntity(event.getPlayerId());
        if (!optional.isPresent()) {
            return;
        }
        StarLightSignActivityEntity entity = optional.get();
        for(int i : cfg.getBuyList()){
            int type = i + 1;
            for(StarLightSignItem item : getSignList(entity)){
                if(item.getType() == cfg.getType() && item.getRechargeType() == type){
                    item.setIsBuy(1);
                }
            }
        }
        syncActivityInfo(entity);
    }

    public boolean checkAuthBuy(String playerId, String payGiftId){
        //活动没开不可购买
        if (!isOpening(playerId)) {
            return false;
        }
        int curDay = curSignDay();
        if(curDay >= 8){
            return false;
        }
        //礼包id转换成整型
        int id = Integer.valueOf(payGiftId);
        StarLightSignPayGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(StarLightSignPayGiftCfg.class, id);
        //如果配置为空不可购买
        if(cfg == null){
            return false;
        }
        Optional<StarLightSignActivityEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return false;
        }
        StarLightSignActivityEntity entity = optional.get();
        int min = 3;
        for(int i : cfg.getBuyList()){
            if(i < min){
                min = i;
            }
        }
        for(int i = 1; i < min; i++){
            int type = i + 1;
            for(StarLightSignItem item : getSignList(entity)){
                if(item.getType() == cfg.getType() && item.getRechargeType() == type){
                    if(!item.isBuy()){
                        return false;
                    }
                }
            }
        }
        for(int i : cfg.getBuyList()){
            int type = i + 1;
            for(StarLightSignItem item : getSignList(entity)){
                if(item.getType() == cfg.getType() && item.getRechargeType() == type){
                    if(item.isBuy()){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        syncActivityInfo(playerId);
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
        Optional<StarLightSignActivityEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        StarLightSignActivityEntity entity = opEntity.get();
        //构造发给前端的数据
        syncActivityInfo(entity);
    }

    /**
     * 同步数据
     * @param entity
     */
    public void syncActivityInfo(StarLightSignActivityEntity entity) {
        if(entity == null){
            return;
        }
        int curDay = curSignDay();
        if(curDay >= 8){
            //checkMultiple(entity);
            List<Reward.RewardItem.Builder> awardList = new ArrayList<>();
            awardList.addAll(awardSign(entity, Activity.StarlightSignType.STAR_LIGHT_SIGN_NORMAL));
            awardList.addAll(awardSign(entity, Activity.StarlightSignType.STAR_LIGHT_SIGN_ADVANCE));
            if(awardList.size() > 0){
                entity.notifyUpdate();
                sendMailToPlayer(entity.getPlayerId(), MailConst.MailId.STAR_LIGHT_SIGN_REWARD_2023321002, null, null, mergeReward(awardList));
            }
        }
        Activity.StarlightSignSync.Builder builder = Activity.StarlightSignSync.newBuilder();
        builder.addAllSignDays(entity.getSignDayList());
        for(StarLightSignItem item : getSignList(entity)){
            builder.addInfos(item.toPB());
        }
        builder.setScore(entity.getScore());
        builder.addAllHaveGet(entity.getScoreBoxList());
        builder.setRate(getRate(entity));
        builder.setIsMultiple(entity.isMultiple());
        builder.setCurDay(curDay);
        builder.setSignRedeemCount(entity.getSignRedeemCnt());
        PlayerPushHelper.getInstance().pushToPlayer(entity.getPlayerId(), HawkProtocol.valueOf(HP.code2.STAR_LIGHT_SIGN_SYNC, builder));
    }

    public void checkMultiple(StarLightSignActivityEntity entity, boolean isEnd){
        StarLightSignKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(StarLightSignKVCfg.class);
        for(StarLightSignItem item : getSignList(entity)){
            if(isEnd && item.getMultiple() == 0){
                item.setMultiple(cfg.getSmailOdds());
                continue;
            }
            if(item.getMultiple() == 0){
                Integer draw = HawkRand.randomWeightObject(cfg.getMaximummagnificationMap());
                item.setMultiple(draw);
            }
        }
    }

    public void checkMultiple(StarLightSignActivityEntity entity){
        checkMultiple(entity, false);
    }

    public int getDefaultChoose(int type, int rechargeType){
        ConfigIterator<StarLightSignRewardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(StarLightSignRewardCfg.class);
        int choose = 0;
        for(StarLightSignRewardCfg cfg : iterator){
            if(cfg.getType() == type && cfg.getDay() == 1){
                switch (rechargeType){
                    case Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_ORDINARY_VALUE:{
                        if(cfg.getOrdinaryRewardList().size() > 0){
                            choose = cfg.getOrdinaryRewardList().get(0).getItemId();
                        }
                    }
                    break;
                    case Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_JUNIOR_VALUE:{
                        if(cfg.getJuniorRewardList().size() > 0){
                            choose = cfg.getJuniorRewardList().get(0).getItemId();
                        }
                    }
                    break;
                    case Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_INTERMEDIATE_VALUE:{
                        if(cfg.getIntermediateRewardList().size() > 0){
                            choose = cfg.getIntermediateRewardList().get(0).getItemId();
                        }
                    }
                    break;
                    case Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_ADVANCED_VALUE:{
                        if(cfg.getAdvancedRewardList().size() > 0){
                            choose = cfg.getAdvancedRewardList().get(0).getItemId();
                        }
                    }
                    break;
                }
            }
        }
        return choose;
    }

    public boolean checkChoose(int type, int rechargeType, int choose){
        ConfigIterator<StarLightSignRewardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(StarLightSignRewardCfg.class);
        boolean flag  = false;
        for(StarLightSignRewardCfg cfg : iterator){
            if(cfg.getType() == type && cfg.getDay() == 1){
                switch (rechargeType){
                    case Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_ORDINARY_VALUE:{
                        for(Reward.RewardItem.Builder item : cfg.getOrdinaryRewardList()){
                            if(item.getItemId() == choose){
                                flag = true;
                            }
                        }
                    }
                    break;
                    case Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_JUNIOR_VALUE:{
                        for(Reward.RewardItem.Builder item : cfg.getJuniorRewardList()){
                            if(item.getItemId() == choose){
                                flag = true;
                            }
                        }
                    }
                    break;
                    case Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_INTERMEDIATE_VALUE:{
                        for(Reward.RewardItem.Builder item : cfg.getIntermediateRewardList()){
                            if(item.getItemId() == choose){
                                flag = true;
                            }
                        }
                    }
                    break;
                    case Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_ADVANCED_VALUE:{
                        for(Reward.RewardItem.Builder item : cfg.getAdvancedRewardList()){
                            if(item.getItemId() == choose){
                                flag = true;
                            }
                        }
                    }
                    break;
                }
            }
        }
        return flag;
    }

    public float getRate(StarLightSignActivityEntity entity){
        int rate = 0;
        ConfigIterator<StarLightSignMissionRateCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(StarLightSignMissionRateCfg.class);
        for(StarLightSignMissionRateCfg cfg : iterator){
            if(entity.getScore() >= cfg.getValue() && cfg.getMarkup() > rate){
                rate  = cfg.getMarkup();
            }
        }
        return rate / 100f;
    }

    public int curSignDay(){
        long now  = HawkTime.getMillisecond();
        int termId = getActivityTermId();
        long startTime = getTimeControl().getStartTimeByTermId(termId);
        startTime = HawkTime.getAM0Date(new Date(startTime)).getTime();
        return (int)Math.ceil((now - startTime) * 1.0f / TimeUnit.DAYS.toMillis(1));
    }

    public Result<Integer> sign(String playerId, int day){
        //判断活动是否开启，如果没开返回错误码
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //如果数据为空，返回错误码
        Optional<StarLightSignActivityEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得活动数据
        StarLightSignActivityEntity entity = optional.get();
        int curDay = curSignDay();
        if(curDay != day){
            ActivityManager.getInstance().postEvent(new StarLightSignLoginDayEvent(playerId, entity.getSignDayList().size() - entity.getSignRedeemCnt()));
            //todo
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        if(entity.getSignDayList().contains(day)){
            ActivityManager.getInstance().postEvent(new StarLightSignLoginDayEvent(playerId, entity.getSignDayList().size() - entity.getSignRedeemCnt()));
            //todo
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        entity.getSignDayList().add(day);
        entity.notifyUpdate();
        syncActivityInfo(entity);
        ActivityManager.getInstance().postEvent(new StarLightSignLoginDayEvent(playerId, entity.getSignDayList().size() - entity.getSignRedeemCnt()));
        return Result.success();
    }

    public Result<Integer> signRedeem(String playerId, int day){
        //判断活动是否开启，如果没开返回错误码
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //如果数据为空，返回错误码
        Optional<StarLightSignActivityEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得活动数据
        StarLightSignActivityEntity entity = optional.get();
        int curDay = curSignDay();
        if(day >= curDay){
            //todo
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        if(curDay >= 8){
            //todo
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        if(entity.getSignDayList().contains(day)){
            //todo
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //todo 扣钱
        StarLightSignKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(StarLightSignKVCfg.class);
        List<Reward.RewardItem.Builder> awardList = new ArrayList<>();
        awardList.addAll(cfg.getBuysignNeed());
        for(int i = 0; i < entity.getSignRedeemCnt(); i++){
            awardList.addAll(cfg.getBuyIncrementalNeed());
        }
        boolean cost = this.getDataGeter().cost(playerId, awardList, 1,
                Action.STAR_LIGHT_SIGN_REDEEM_COST, false);
        if (!cost) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        entity.setSignRedeemCnt(entity.getSignRedeemCnt() + 1);
        entity.getSignDayList().add(day);
        entity.notifyUpdate();
        syncActivityInfo(entity);
        return Result.success();
    }

    public Result<Integer> choose(String playerId, Activity.StarlightSignType type, Activity.StarlightSignRechargeType rechargeType, int choose){
        //判断活动是否开启，如果没开返回错误码
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //如果数据为空，返回错误码
        Optional<StarLightSignActivityEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        if(!checkChoose(type.getNumber(), rechargeType.getNumber(), choose)){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //获得活动数据
        StarLightSignActivityEntity entity = optional.get();
        for(StarLightSignItem item : getSignList(entity)){
            if(item.getType() == type.getNumber() && item.getRechargeType() == rechargeType.getNumber()){
                item.setChoose(choose);
            }
        }
        entity.notifyUpdate();
        syncActivityInfo(entity);
        getDataGeter().logStarLightSignChoose(playerId, type.getNumber(), rechargeType.getNumber(), choose);
        return Result.success();
    }


    public Result<Integer> award(String playerId, Activity.StarlightSignType type){
        //判断活动是否开启，如果没开返回错误码
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //如果数据为空，返回错误码
        Optional<StarLightSignActivityEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得活动数据
        StarLightSignActivityEntity entity = optional.get();
        List<Reward.RewardItem.Builder> awardList = new ArrayList<>();
        awardList.addAll(awardSign(entity, type));
        //todo
        int curDay = curSignDay();
        if(curDay>=8){
            checkMultiple(entity);
            awardList.addAll(awardMultiple(entity, type));
        }
        //翻倍奖励
        this.getDataGeter().takeReward(playerId, awardList, 1, Action.STAR_LIGHT_SIGN_REWARD, true);
        entity.notifyUpdate();
        syncActivityInfo(entity);
        if(curDay>=8 && awardList.size()<=0){
            return Result.fail(Status.Error.STAR_LIGHT_SIGN_ERROR_VALUE);
        }
        return Result.success();
    }

    private List<Reward.RewardItem.Builder> awardSign(StarLightSignActivityEntity entity, Activity.StarlightSignType type){
        List<Reward.RewardItem.Builder> awardList = new ArrayList<>();
        ConfigIterator<StarLightSignRewardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(StarLightSignRewardCfg.class);
        for(StarLightSignRewardCfg cfg : iterator){
            if(cfg.getType() != type.getNumber()){
                continue;
            }
            if(!entity.getSignDayList().contains(cfg.getDay())){
                continue;
            }
            for(StarLightSignItem signItem : getSignList(entity)){
                if(signItem.getType() != type.getNumber()){
                    continue;
                }
                if(!signItem.isBuy()){
                    continue;
                }
                if(signItem.getReward().contains(cfg.getDay())){
                    continue;
                }
                signItem.getReward().add(cfg.getDay());
                switch (signItem.getRechargeType()){
                    case Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_ORDINARY_VALUE:{
                        for(Reward.RewardItem.Builder item : cfg.getOrdinaryRewardList()){
                            if(item.getItemId() == signItem.getChoose()){
                                awardList.add(item);
                                signItem.setGetCount(signItem.getGetCount() + (int)item.getItemCount());
                            }
                        }
                    }
                    break;
                    case Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_JUNIOR_VALUE:{
                        for(Reward.RewardItem.Builder item : cfg.getJuniorRewardList()){
                            if(item.getItemId() == signItem.getChoose()){
                                awardList.add(item);
                                signItem.setGetCount(signItem.getGetCount() + (int)item.getItemCount());
                            }
                        }
                    }
                    break;
                    case Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_INTERMEDIATE_VALUE:{
                        for(Reward.RewardItem.Builder item : cfg.getIntermediateRewardList()){
                            if(item.getItemId() == signItem.getChoose()){
                                awardList.add(item);
                                signItem.setGetCount(signItem.getGetCount() + (int)item.getItemCount());
                            }
                        }
                    }
                    break;
                    case Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_ADVANCED_VALUE:{
                        for(Reward.RewardItem.Builder item : cfg.getAdvancedRewardList()){
                            if(item.getItemId() == signItem.getChoose()){
                                awardList.add(item);
                                signItem.setGetCount(signItem.getGetCount() + (int)item.getItemCount());
                            }
                        }
                    }
                    break;
                }
            }
        }
        entity.notifyUpdate();
        if(awardList.size()>0){
            logReward(entity.getPlayerId(), awardList, SIGN_REWARD);
        }
        return awardList;
    }

    private List<Reward.RewardItem.Builder> awardMultiple(StarLightSignActivityEntity entity, Activity.StarlightSignType type){
        List<Reward.RewardItem.Builder> awardList = new ArrayList<>();
        for(StarLightSignItem signItem : getSignList(entity)) {
            if (signItem.getType() != type.getNumber()) {
                continue;
            }
            if(!signItem.isBuy()){
                continue;
            }
            if(signItem.isGet()){
                continue;
            }
            signItem.setIsGet(1);
            Reward.RewardItem.Builder builder = getMultipleItem(entity, signItem);
            if(builder != null){
                awardList.add(builder);
            }
        }
        entity.notifyUpdate();
        if(awardList.size()>0){
            logReward(entity.getPlayerId(), awardList, MULTI_REWARD);
        }
        return awardList;
    }

    private List<Reward.RewardItem.Builder> mergeReward(List<Reward.RewardItem.Builder> awardList){
        Map<Integer, Reward.RewardItem.Builder> map = new HashMap<>();
        for(Reward.RewardItem.Builder item : awardList){
            Reward.RewardItem.Builder tmp = map.get(item.getItemId());
            if(tmp == null){
                map.put(item.getItemId(), item.clone());
            }else {
                tmp.setItemCount(tmp.getItemCount() + item.getItemCount());
            }
        }
        return new ArrayList<>(map.values());
    }

    private void logReward(String playerId, List<Reward.RewardItem.Builder> awardList, int reason){
        getDataGeter().logStarLightSignAward(playerId, awardList, reason);
    }

    private Reward.RewardItem.Builder getMultipleItem(StarLightSignActivityEntity entity, StarLightSignItem signItem){
        Reward.RewardItem.Builder builder = null;
        ConfigIterator<StarLightSignRewardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(StarLightSignRewardCfg.class);
        for(StarLightSignRewardCfg cfg : iterator){
            if(cfg.getType() == signItem.getType() && cfg.getDay() == 1){
                switch (signItem.getRechargeType()){
                    case Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_ORDINARY_VALUE:{
                        for(Reward.RewardItem.Builder item : cfg.getOrdinaryRewardList()){
                            if(item.getItemId() == signItem.getChoose()){
                                builder = item.clone();
                            }
                        }
                    }
                    break;
                    case Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_JUNIOR_VALUE:{
                        for(Reward.RewardItem.Builder item : cfg.getJuniorRewardList()){
                            if(item.getItemId() == signItem.getChoose()){
                                builder = item.clone();
                            }
                        }
                    }
                    break;
                    case Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_INTERMEDIATE_VALUE:{
                        for(Reward.RewardItem.Builder item : cfg.getIntermediateRewardList()){
                            if(item.getItemId() == signItem.getChoose()){
                                builder = item.clone();
                            }
                        }
                    }
                    break;
                    case Activity.StarlightSignRechargeType.STAR_LIGHT_SIGN_RECHARGE_ADVANCED_VALUE:{
                        for(Reward.RewardItem.Builder item : cfg.getAdvancedRewardList()){
                            if(item.getItemId() == signItem.getChoose()){
                                builder = item.clone();
                            }
                        }
                    }
                    break;
                }
            }
        }
        if(builder!=null){
            int getCount = signItem.getGetCount();
            float rate = getRate(entity);
            float multiple = signItem.getMultiple();
            logger.info("getMultipleItem,getCount:{},rate:{},multiple:{}",getCount,rate,multiple);
            int itemCount = (int)Math.ceil(getCount * rate * multiple);
            if (itemCount == 0){
                logger.info("getMultipleItem builder is zero,type:{},recharge:{}",signItem.getType(),signItem.getRechargeType());
                return null;
            }
            builder.setItemCount(itemCount);
        }else {
            logger.info("getMultipleItem builder is null,type:{},recharge:{}",signItem.getType(),signItem.getRechargeType());
        }
        return builder;
    }

    public Result<Integer> buyScore(String playerId, int score){
        //判断活动是否开启，如果没开返回错误码
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //如果数据为空，返回错误码
        Optional<StarLightSignActivityEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得活动数据
        StarLightSignActivityEntity entity = optional.get();
        StarLightSignKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(StarLightSignKVCfg.class);
        boolean cost = this.getDataGeter().cost(playerId, cfg.getBuymissionNeed(), score,
                Action.STAR_LIGHT_SIGN_SCORE_COST, false);
        if (!cost) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        int before = entity.getScore();
        entity.setScore(entity.getScore() + score);
        syncActivityInfo(entity);
        getDataGeter().logStarLightSignScore(playerId, before, entity.getScore(), score);
        return Result.success();
    }

    public Result<Integer> multiple(String playerId, Activity.StarlightSignType type, Activity.StarlightSignRechargeType rechargeType){
        //判断活动是否开启，如果没开返回错误码
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //如果数据为空，返回错误码
        Optional<StarLightSignActivityEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        StarLightSignKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(StarLightSignKVCfg.class);
        if(cfg == null){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //获得活动数据
        StarLightSignActivityEntity entity = optional.get();
        for(StarLightSignItem item : getSignList(entity)){
            if(item.getType() == type.getNumber() && item.getRechargeType() == rechargeType.getNumber()){
                if(item.getMultiple() == 0){
                    Integer draw = HawkRand.randomWeightObject(cfg.getMaximummagnificationMap());
                    item.setMultiple(draw);
                }
            }
        }
        entity.notifyUpdate();
        syncActivityInfo(entity);
        return Result.success();
    }

    public Result<Integer> scoreBox(String playerId, int id){
        //判断活动是否开启，如果没开返回错误码
        if (!isOpening(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //如果数据为空，返回错误码
        Optional<StarLightSignActivityEntity> optional = this.getPlayerDataEntity(playerId);
        if (!optional.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得活动数据
        StarLightSignActivityEntity entity = optional.get();
        if(entity.getScoreBoxList().contains(id)){
            //todo
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        StarLightSignMissionRateCfg cfg = HawkConfigManager.getInstance().getConfigByKey(StarLightSignMissionRateCfg.class, id);
        if(cfg == null){
            //todo
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        entity.getScoreBoxList().add(id);
        entity.notifyUpdate();
        this.getDataGeter().takeReward(playerId, cfg.getRewardList(), 1, Action.STAR_LIGHT_SIGN_REWARD, true);
        syncActivityInfo(entity);
        return Result.success();
    }
}
