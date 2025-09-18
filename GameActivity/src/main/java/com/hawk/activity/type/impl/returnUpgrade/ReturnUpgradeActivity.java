package com.hawk.activity.type.impl.returnUpgrade;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.*;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import com.hawk.activity.type.impl.returnUpgrade.cfg.*;
import com.hawk.activity.type.impl.returnUpgrade.entity.ReturnUpgradeEntity;
import com.hawk.game.protocol.*;
import com.hawk.log.Action;
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
import java.util.concurrent.TimeUnit;

public class ReturnUpgradeActivity extends ActivityBase implements AchieveProvider {
    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger("Server");

    public ReturnUpgradeActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.RETURN_UPGRADE;
    }
    
    @Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        ReturnUpgradeActivity activity = new ReturnUpgradeActivity(config.getActivityId(), activityEntity);
        //注册活动
        AchieveContext.registeProvider(activity);
        //返回活动实例
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        //根据条件从数据库中检索
        List<ReturnUpgradeEntity> queryList = HawkDBManager.getInstance()
                .query("from ReturnUpgradeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        //如果有数据的话，返回第一个数据
        if (queryList != null && queryList.size() > 0) {
            ReturnUpgradeEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        ReturnUpgradeEntity entity = new ReturnUpgradeEntity(playerId, termId);
        return entity;
    }

    @Override
    public void syncActivityDataInfo(String playerId) {
        //获得玩家活动数据
        Optional<ReturnUpgradeEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        ReturnUpgradeEntity entity = opEntity.get();
        Activity.ReturnUpgradeSync.Builder builder = Activity.ReturnUpgradeSync.newBuilder();
        builder.addItems(getBaseInfo(entity));
        builder.addItems(getRoleInfo(entity));
        builder.addItems(getTechInfo(entity));
        getShopItem(builder, entity);
        builder.setBuyTimes(entity.getGoldBuyCount());
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.RETURN_UPGRADE_SYNC, builder));
    }

    public Activity.ReturnUpgradeItem.Builder getBaseInfo(ReturnUpgradeEntity entity){
        Activity.ReturnUpgradeItem.Builder builder = Activity.ReturnUpgradeItem.newBuilder();
        builder.setType(Activity.ReturnUpgradeType.REUP_BASE);
        boolean isDone = entity.getUpgradeMap().getOrDefault(Activity.ReturnUpgradeType.REUP_BASE_VALUE, 0) > 0;
        if(isDone){
            builder.setIsDone(true);
        }else {
            builder.setIsDone(entity.getBaseAfterLevel() <= entity.getBaseBeforLevel());
        }
        builder.setOldLevel(entity.getBaseBeforLevel());
        builder.setNewLevel(entity.getBaseAfterLevel());
        builder.setCost((int) calBaseCostNum(getDataGeter().getConstructionFactoryCfgId(entity.getPlayerId()), entity.getBaseAfterLevel()));
        return builder;
    }

    public Activity.ReturnUpgradeItem.Builder getRoleInfo(ReturnUpgradeEntity entity){
        Activity.ReturnUpgradeItem.Builder builder = Activity.ReturnUpgradeItem.newBuilder();
        builder.setType(Activity.ReturnUpgradeType.REUP_ROLE);
        boolean isDone = entity.getUpgradeMap().getOrDefault(Activity.ReturnUpgradeType.REUP_ROLE_VALUE, 0) > 0;
        if(isDone){
            builder.setIsDone(true);
        }else {
            builder.setIsDone(entity.getRoleAfterLevel() <= entity.getRoleBeforLevel());
        }
        builder.setOldLevel(entity.getRoleBeforLevel());
        builder.setNewLevel(entity.getRoleAfterLevel());
        builder.setCost((int)calRoleCostNum(getDataGeter().getPlayerLevel(entity.getPlayerId()), entity.getRoleAfterLevel()));
        return builder;
    }

    public Activity.ReturnUpgradeItem.Builder getTechInfo(ReturnUpgradeEntity entity){
        Activity.ReturnUpgradeItem.Builder builder = Activity.ReturnUpgradeItem.newBuilder();
        builder.setType(Activity.ReturnUpgradeType.REUP_TECH);
        boolean isDone = entity.getUpgradeMap().getOrDefault(Activity.ReturnUpgradeType.REUP_TECH_VALUE, 0) > 0;
        //todo
        long power = getDataGeter().getTechLevelPower(entity.getBaseAfterLevel());
        if(isDone){
            builder.setIsDone(true);
        }else {
            builder.setIsDone(power <= entity.getTechPower());
        }
        builder.setOldPower(getDataGeter().getPowerData(entity.getPlayerId()).getTechBattlePoint());
        builder.setNewPower(power);
        builder.setCost((int)calTechCostNum(entity.getBaseBeforLevel(), entity.getBaseAfterLevel()));
        return builder;
    }

    public void getShopItem(Activity.ReturnUpgradeSync.Builder builder, ReturnUpgradeEntity entity){
        for(Map.Entry<Integer, Integer> entry : entity.getBuyNumMap().entrySet()){
            Activity.ReturnUpgradeShopItem.Builder shopItem = Activity.ReturnUpgradeShopItem.newBuilder();
            //配置id
            shopItem.setGoodsId(entry.getKey());
            //已兑换数量
            shopItem.setExhangeTimes(entry.getValue());
            builder.addGoods(shopItem);
        }
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
        //获得玩家活动数据
        Optional<ReturnUpgradeEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        //获得玩家活动数据实体
        ReturnUpgradeEntity entity = opEntity.get();
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
        //获得玩家活动数据
        Optional<ReturnUpgradeEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        ReturnUpgradeEntity entity = opEntity.get();
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        ConfigIterator<ReturnUpgradeAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ReturnUpgradeAchieveCfg.class);
        List<AchieveItem> list = new ArrayList<>();
        for(ReturnUpgradeAchieveCfg cfg : iterator){
            AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
            list.add(item);
        }
        entity.setItemList(list);
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()));
    }

    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        ReturnUpgradeAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ReturnUpgradeAchieveCfg.class, achieveId);
        return cfg;
    }

    @Override
    public Action takeRewardAction() {
        return Action.RETURN_UPGRADE_ACHIEVE_REWARD;
    }

    @Subscribe
    public void onContinueLoginEvent(ContinueLoginEvent event) {
        String playerId = event.getPlayerId();
        //如果活动没开返回
        if (!isOpening(playerId)) {
            return;
        }
        resetAchieve(playerId);
        postCommonLoginEvent(playerId);

    }

    private void resetAchieve(String playerId){
        //获得玩家活动数据
        Optional<ReturnUpgradeEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        ReturnUpgradeEntity entity = opEntity.get();
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
            ReturnUpgradeAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ReturnUpgradeAchieveCfg.class, item.getAchieveId());
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
        if(needPushList.size() > 0){
            //推送给前端
            AchievePushHelper.pushAchieveUpdate(playerId, needPushList);
        }
    }


    /**
     * 监听建筑升级事件
     * @param event 建筑升级
     */
    @Subscribe
    public void onBuildingLevelUpEvent(BuildingLevelUpEvent event) {
        //不是主城不管
        if(event.getBuildType() != Const.BuildingType.CONSTRUCTION_FACTORY_VALUE){
            return;
        }
        String playerId = event.getPlayerId();
        if(!isShow(playerId)){
            return;
        }
        syncActivityDataInfo(playerId);
//        //获得玩家活动数据
//        Optional<ReturnUpgradeEntity> opEntity = getPlayerDataEntity(playerId);
//        //如果数据为空直接返回
//        if (!opEntity.isPresent()) {
//            return;
//        }
//        //获得玩家活动数据实体
//        ReturnUpgradeEntity entity = opEntity.get();
//        if(getDataGeter().getConstructionFactoryCfgId(entity.getPlayerId()) >= entity.getBaseAfterLevel()){
//            entity.getUpgradeMap().put(Activity.ReturnUpgradeType.REUP_BASE_VALUE, 2);
//            syncActivityDataInfo(playerId);
//        }
//        if(event.getLevel() >= entity.getBaseAfterLevel() && getDataGeter().getPlayerLevel(playerId) >= entity.getRoleAfterLevel()){
//            entity.getUpgradeMap().put(Activity.ReturnUpgradeType.REUP_TECH_VALUE, 1);
//            syncActivityDataInfo(playerId);
//        }
    }

    /**
     * 监听建筑升级事件
     * @param event 建筑升级
     */
    @Subscribe
    public void onRoleLevelUpEvent(PlayerLevelUpEvent event) {
        String playerId = event.getPlayerId();
        if(!isShow(playerId)){
            return;
        }
        syncActivityDataInfo(playerId);
//        //获得玩家活动数据
//        Optional<ReturnUpgradeEntity> opEntity = getPlayerDataEntity(playerId);
//        //如果数据为空直接返回
//        if (!opEntity.isPresent()) {
//            return;
//        }
//        //获得玩家活动数据实体
//        ReturnUpgradeEntity entity = opEntity.get();
//        if(event.getLevel() >= entity.getRoleAfterLevel()){
//            entity.getUpgradeMap().put(Activity.ReturnUpgradeType.REUP_ROLE_VALUE, 2);
//            syncActivityDataInfo(playerId);
//        }
//        if(event.getLevel() >= entity.getRoleAfterLevel() && getDataGeter().getConstructionFactoryLevel(playerId) >= entity.getBaseAfterLevel()){
//            entity.getUpgradeMap().put(Activity.ReturnUpgradeType.REUP_TECH_VALUE, 1);
//            syncActivityDataInfo(playerId);
//        }
    }

    /**
     * 监听监听战力提升事件
     * @param event 战力提升事件
     */
    @Subscribe
    public void onBattlePointChangeEvent(BattlePointChangeEvent event) {
        String playerId = event.getPlayerId();
        if(!isShow(playerId)){
            return;
        }
        syncActivityDataInfo(playerId);
//        //获得玩家活动数据
//        Optional<ReturnUpgradeEntity> opEntity = getPlayerDataEntity(playerId);
//        //如果数据为空直接返回
//        if (!opEntity.isPresent()) {
//            return;
//        }
//        //获得玩家活动数据实体
//        ReturnUpgradeEntity entity = opEntity.get();
//        if(event.getPowerData().getTechBattlePoint() >= getDataGeter().getTechLevelPower(entity.getBaseAfterLevel())){
//            entity.getUpgradeMap().put(Activity.ReturnUpgradeType.REUP_TECH_VALUE, 2);
//            syncActivityDataInfo(playerId);
//        }
    }

    @Override
    public boolean isActivityClose(String playerId) {
        //获得玩家活动数据
        Optional<ReturnUpgradeEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return true;
        }
        //获得玩家活动数据实体
        ReturnUpgradeEntity entity = opEntity.get();
        long now = HawkTime.getMillisecond();
        if(now < entity.getStartTime() || now > entity.getOverTime()){
            return true;
        }
        return false;
    }

    public int calFirstBaseDelay(int firstValue, int delay){
        if(firstValue<=30){
            return Math.max(1, firstValue - delay);
        }else {
            int level = firstValue / 100;
            int process = firstValue % 100;
            level = level - delay;
            if(level < 30){
                return Math.max(1, level);
            }else if(level == 30 && process == 0){
                return level;
            }else {
                return level * 100 + process;
            }
        }
    }


    public int getBaseCfgIdFromRank(int rankInfoValue){
        if(rankInfoValue < 10){
            return Integer.parseInt("20100"+rankInfoValue);
        }else {
            return Integer.parseInt("2010"+rankInfoValue);
        }
    }

    @Override
    public void onPlayerLogin(String playerId) {
        Optional<ReturnUpgradeEntity> optional = getPlayerDataEntity(playerId);
        if (!optional.isPresent()){
            return;
        }
        ReturnUpgradeEntity entity = optional.get();
        BackFlowPlayer backFlowPlayer = this.getDataGeter().getBackFlowPlayer(playerId);
        if(backFlowPlayer == null){
            return;
        }
        if(this.checkFitLostParams(backFlowPlayer,entity)){
            ReturnUpgradeKvCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(ReturnUpgradeKvCfg.class);
            int backTimes = backFlowPlayer.getBackCount();
            long startTime = HawkTime.getAM0Date(new Date(backFlowPlayer.getBackTimeStamp())).getTime();
            ReturnUpgradeTypeCfg cfg = getBackTypeCfg(backFlowPlayer);
            long continueTime = 0;
            int backType = 0;
            if(cfg != null){
                continueTime = TimeUnit.DAYS.toMillis(cfg.getDuration()) - 1000;
                backType = cfg.getId();
            }
            long overTime = startTime + continueTime;
            entity.setBackCount(backTimes);
            entity.setStartTime(startTime);
            entity.setOverTime(overTime);
            entity.notifyUpdate();
            logger.info("onPlayerLogin  checkFitLostParams init sucess,  playerId: "+
                            "{},backCount:{},backTime:{},startTime:{}.overTime:{},backType:{}",
                    playerId,backTimes,backFlowPlayer.getBackTimeStamp(),startTime,overTime,backType);
            List<Rank.RankInfo> baseRankInfoList = getDataGeter().getRankCache(Rank.RankType.PLAYER_CASTLE_KEY, kvCfg.getPlayerBuildRanking());
            int baseSize = Math.min(baseRankInfoList.size(), 100);
            Rank.RankInfo baseRankInfo = baseRankInfoList.get(baseSize - 1);
            Rank.RankInfo firstBaseRankInfo = baseRankInfoList.get(0);

            List<Rank.RankInfo> roleRankInfoList = getDataGeter().getRankCache(Rank.RankType.PLAYER_GRADE_KEY, kvCfg.getPlayerRanking());
            int roleSize = Math.min(roleRankInfoList.size(), 100);
            Rank.RankInfo roleRankInfo = roleRankInfoList.get(roleSize - 1);
            entity.setBaseBeforLevel(getDataGeter().getConstructionFactoryCfgId(playerId));
            entity.setBaseAfterLevel(Math.min(getBaseCfgIdFromRank((int)baseRankInfo.getRankInfoValue()), getBaseCfgIdFromRank(calFirstBaseDelay((int)firstBaseRankInfo.getRankInfoValue(), kvCfg.getRankingDelay()))));
            entity.setRoleBeforLevel(getDataGeter().getPlayerLevel(playerId));
            entity.setRoleAfterLevel((int)roleRankInfo.getRankInfoValue());
            entity.setTechPower(getDataGeter().getPowerData(playerId).getTechBattlePoint());
            entity.setBuyNumMap(new HashMap<>());
            entity.setGoldBuyCount(0);
            entity.notifyUpdate();
        }
    }

    public boolean checkFitLostParams(BackFlowPlayer backFlowPlayer, ReturnUpgradeEntity entity) {
        if(entity.getBackCount() > 0){
            logger.info("checkFitLostParams failed, BackCount data fail , playerId: "
                            + "{},backCount:{},entityBackCount:{}", backFlowPlayer.getPlayerId(),
                    backFlowPlayer.getBackCount(),entity.getBackCount());
            return false;
        }
        if(backFlowPlayer.getBackCount() <= entity.getBackCount()){
            logger.info("checkFitLostParams failed, BackCount data fail , playerId: "
                            + "{},backCount:{},entityBackCount:{}", backFlowPlayer.getPlayerId(),
                    backFlowPlayer.getBackCount(),entity.getBackCount());
            return false;
        }
        long backTime = backFlowPlayer.getBackTimeStamp();
        //如果在活动中，只更新期数，不更新其他数据
        if(backTime < entity.getOverTime() && backTime > entity.getStartTime()){
            entity.setBackCount(backFlowPlayer.getBackCount());
            entity.notifyUpdate();
            logger.info("checkFitLostParams failed,in activity, playerId: "
                    + "{},backCount:{},backTime:{}", entity.getPlayerId(),entity.getBackCount(),backTime);
            return false;
        }
        //停止触发，只更新期数，不更新其他数据
        if(!this.canTrigger(backTime)){
            entity.setBackCount(backFlowPlayer.getBackCount());
            entity.notifyUpdate();
            logger.info("checkFitLostParams failed,can not Trigger, playerId: "
                    + "{},backCount:{},backTime:{}", entity.getPlayerId(),entity.getBackCount(),backTime);
            return false;
        }
        int lossDays = backFlowPlayer.getLossDays();
        logger.info("checkFitLostParams sucess, playerId: "
                + "{},loss:{}", backFlowPlayer.getPlayerId(),lossDays);
        return true;
    }

    public boolean canTrigger(long backTime){
        int termId = this.getActivityTermId();
        ReturnUpgradeTimeCfg cfg = HawkConfigManager.getInstance().
                getConfigByKey(ReturnUpgradeTimeCfg.class, termId);
        if(cfg == null){
            return false;
        }
        if(backTime < cfg.getStartTimeValue()){
            return false;
        }
        if(backTime > cfg.getStopTriggerValue()){
            return false;
        }
        return true;
    }

    public ReturnUpgradeTypeCfg getBackTypeCfg(BackFlowPlayer backFlowPlayer){
        ConfigIterator<ReturnUpgradeTypeCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(ReturnUpgradeTypeCfg.class);
        for(ReturnUpgradeTypeCfg cfg : cfgs){
            if(cfg.isAdapt(backFlowPlayer)){
                return cfg;
            }
        }
        return null;
    }

    private List<Reward.RewardItem.Builder> calBaseCost(int before, int after){
    	int size = HawkConfigManager.getInstance().getConfigSize(ReturnUpgradeLevelCfg.class);
    	ReturnUpgradeLevelCfg maxLevelCfg = HawkConfigManager.getInstance().getConfigByIndex(ReturnUpgradeLevelCfg.class, size - 1);
        ReturnUpgradeLevelCfg beforeLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ReturnUpgradeLevelCfg.class, before);
        if (beforeLevelCfg == null) {
        	beforeLevelCfg = maxLevelCfg;
        }
        ReturnUpgradeLevelCfg afterLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ReturnUpgradeLevelCfg.class, after);
        if (afterLevelCfg == null) {
        	afterLevelCfg = maxLevelCfg;
        }
        List<Reward.RewardItem.Builder> costList = RewardHelper.toRewardItemImmutableList(afterLevelCfg.getBuildCost());
        for(Reward.RewardItem.Builder afterBuilder : costList){
            for(Reward.RewardItem.Builder beforeBuilder : RewardHelper.toRewardItemImmutableList(beforeLevelCfg.getBuildCost())){
                if(afterBuilder.getItemId() != beforeBuilder.getItemId()){
                    continue;
                }
                afterBuilder.setItemCount(afterBuilder.getItemCount() - beforeBuilder.getItemCount());
            }
        }
        return costList;
    }

    private long calBaseCostNum(int before, int after){
    	int size = HawkConfigManager.getInstance().getConfigSize(ReturnUpgradeLevelCfg.class);
    	ReturnUpgradeLevelCfg maxLevelCfg = HawkConfigManager.getInstance().getConfigByIndex(ReturnUpgradeLevelCfg.class, size - 1);
        ReturnUpgradeLevelCfg beforeLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ReturnUpgradeLevelCfg.class, before);
        if (beforeLevelCfg == null) {
        	beforeLevelCfg = maxLevelCfg;
        }
        ReturnUpgradeLevelCfg afterLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ReturnUpgradeLevelCfg.class, after);
        if (afterLevelCfg == null) {
        	afterLevelCfg = maxLevelCfg;
        }
        return Math.max(0, afterLevelCfg.getBuildCostNum() - beforeLevelCfg.getBuildCostNum());
    }

    private List<Reward.RewardItem.Builder> calRoleCost(int before, int after){
        ReturnUpgradeLevelTwoCfg beforeLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ReturnUpgradeLevelTwoCfg.class, before);
        ReturnUpgradeLevelTwoCfg afterLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ReturnUpgradeLevelTwoCfg.class, after);
        List<Reward.RewardItem.Builder> costList = RewardHelper.toRewardItemImmutableList(afterLevelCfg.getPlayerCost());
        for(Reward.RewardItem.Builder afterBuilder : costList){
            for(Reward.RewardItem.Builder beforeBuilder : RewardHelper.toRewardItemImmutableList(beforeLevelCfg.getPlayerCost())){
                if(afterBuilder.getItemId() != beforeBuilder.getItemId()){
                    continue;
                }
                afterBuilder.setItemCount(afterBuilder.getItemCount() - beforeBuilder.getItemCount());
            }
        }
        return costList;
    }

    private long calRoleCostNum(int before, int after){
        ReturnUpgradeLevelTwoCfg beforeLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ReturnUpgradeLevelTwoCfg.class, before);
        ReturnUpgradeLevelTwoCfg afterLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ReturnUpgradeLevelTwoCfg.class, after);
        return Math.max(0, afterLevelCfg.getPlayerCostNum() - beforeLevelCfg.getPlayerCostNum());
    }

    private List<Reward.RewardItem.Builder> calTechCost(int before, int after){
    	int size = HawkConfigManager.getInstance().getConfigSize(ReturnUpgradeLevelCfg.class);
    	ReturnUpgradeLevelCfg maxLevelCfg = HawkConfigManager.getInstance().getConfigByIndex(ReturnUpgradeLevelCfg.class, size - 1);
        ReturnUpgradeLevelCfg beforeLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ReturnUpgradeLevelCfg.class, before);
        if (beforeLevelCfg == null) {
        	beforeLevelCfg = maxLevelCfg;
        }
        ReturnUpgradeLevelCfg afterLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ReturnUpgradeLevelCfg.class, after);
        if (afterLevelCfg == null) {
        	afterLevelCfg = maxLevelCfg;
        }
        List<Reward.RewardItem.Builder> costList = RewardHelper.toRewardItemImmutableList(afterLevelCfg.getTechCost());
        for(Reward.RewardItem.Builder afterBuilder : costList){
            for(Reward.RewardItem.Builder beforeBuilder : RewardHelper.toRewardItemImmutableList(beforeLevelCfg.getTechCost())){
                if(afterBuilder.getItemId() != beforeBuilder.getItemId()){
                    continue;
                }
                afterBuilder.setItemCount(afterBuilder.getItemCount() - beforeBuilder.getItemCount());
            }
        }
        return costList;
    }

    private long calTechCostNum(int before, int after){
    	int size = HawkConfigManager.getInstance().getConfigSize(ReturnUpgradeLevelCfg.class);
    	ReturnUpgradeLevelCfg maxLevelCfg = HawkConfigManager.getInstance().getConfigByIndex(ReturnUpgradeLevelCfg.class, size - 1);
        ReturnUpgradeLevelCfg beforeLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ReturnUpgradeLevelCfg.class, before);
        if (beforeLevelCfg == null) {
        	beforeLevelCfg = maxLevelCfg;
        }
        ReturnUpgradeLevelCfg afterLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ReturnUpgradeLevelCfg.class, after);
        if (afterLevelCfg == null) {
        	afterLevelCfg = maxLevelCfg;
        }
        return Math.max(0, afterLevelCfg.getTechCostNum() - beforeLevelCfg.getTechCostNum());
    }


    public Result<Integer> upgrade(String playerId, Activity.ReturnUpgradeType type, boolean useGold) {
        if(!getDataGeter().returnUpgradeCheck(playerId, type)){
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        //如果数据为空，返回错误码
        Optional<ReturnUpgradeEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        ReturnUpgradeEntity entity = opEntity.get();
        boolean isDone = entity.getUpgradeMap().getOrDefault(type.getNumber(), 0) > 0;
        if(isDone){
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        Activity.ReturnUpgradeResp.Builder builder = Activity.ReturnUpgradeResp.newBuilder();
        builder.setType(type);
        switch (type){
            case REUP_BASE:{
                int before = getDataGeter().getConstructionFactoryCfgId(playerId);
                int after = entity.getBaseAfterLevel();
                if(before >= after){
                    entity.getUpgradeMap().put(type.getNumber(), 1);
                    entity.notifyUpdate();
                    syncActivityDataInfo(playerId);
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                List<Reward.RewardItem.Builder> costList = calBaseCost(before, after);
                boolean flag = this.getDataGeter().cost(playerId, costList, 1, Action.RETURN_UPGRADE_SHOP_COST, true);
                //如果不够消耗，返回错误码
                if (!flag) {
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                entity.getUpgradeMap().put(type.getNumber(), 1);
                entity.notifyUpdate();
                PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.RETURN_UPGRADE_RESP, builder));
                getDataGeter().returnBuildingLvUp(playerId, entity.getBaseAfterLevel());
            }
            break;
            case REUP_ROLE:{
                int before = getDataGeter().getPlayerLevel(playerId);
                int after = entity.getRoleAfterLevel();
                if(before >= after){
                    entity.getUpgradeMap().put(type.getNumber(), 1);
                    entity.notifyUpdate();
                    syncActivityDataInfo(playerId);
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                List<Reward.RewardItem.Builder> costList = calRoleCost(before, after);
                boolean flag = this.getDataGeter().cost(playerId, costList, 1, Action.RETURN_UPGRADE_SHOP_COST, true);
                //如果不够消耗，返回错误码
                if (!flag) {
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                entity.getUpgradeMap().put(type.getNumber(), 1);
                entity.notifyUpdate();
                PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.RETURN_UPGRADE_RESP, builder));
                getDataGeter().returnRoleUp(playerId, entity.getRoleAfterLevel());
            }
            break;
            case REUP_TECH:{
                boolean isBaseDone = entity.getUpgradeMap().getOrDefault(Activity.ReturnUpgradeType.REUP_BASE_VALUE, 0) > 0;
                if(!isBaseDone){
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                long before = getDataGeter().getPowerData(playerId).getBattlePoint();
                long after =  getDataGeter().getTechLevelPower(entity.getBaseAfterLevel());
                if(before >= after){
                    entity.getUpgradeMap().put(type.getNumber(), 1);
                    entity.notifyUpdate();
                    syncActivityDataInfo(playerId);
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                List<Reward.RewardItem.Builder> costList = calTechCost(entity.getBaseBeforLevel(), entity.getBaseAfterLevel());
                boolean flag = this.getDataGeter().cost(playerId, costList, 1, Action.RETURN_UPGRADE_SHOP_COST, true);
                //如果不够消耗，返回错误码
                if (!flag) {
                    return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
                }
                entity.getUpgradeMap().put(type.getNumber(), 1);
                entity.notifyUpdate();
                PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.RETURN_UPGRADE_RESP, builder));
                getDataGeter().returnTechUp(playerId, entity.getBaseAfterLevel());
            }
            break;
        }
        //syncActivityDataInfo(playerId);
        return Result.success();
    }

    public Result<Integer> exchange(String playerId, int exchangeId, int num) {
        //判断活动是否开启，如果没开返回错误码
        if (!isShow(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //获取兑换配置，如果配置为空返回错误码
        ReturnUpgradeShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ReturnUpgradeShopCfg.class, exchangeId);
        if (cfg == null) {
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //如果数据为空，返回错误码
        Optional<ReturnUpgradeEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        ReturnUpgradeEntity entity = opEntity.get();
        //当前已经兑换数量
        int buyNum = entity.getBuyNumMap().getOrDefault(exchangeId, 0);
        //兑换后的数量
        int newNum = buyNum + num;
        //判断是否超过可兑换数量最大值，如果超过返回错误码
        if (newNum > cfg.getBuyTimes()) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        //兑换消耗
        boolean flag = this.getDataGeter().cost(playerId, cfg.getPayItemList(), num, Action.RETURN_UPGRADE_SHOP_COST, true);
        //如果不够消耗，返回错误码
        if (!flag) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        //设置新的兑奖数量
        entity.getBuyNumMap().put(exchangeId, newNum);
        entity.notifyUpdate();
        //发奖
        this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.RETURN_UPGRADE_SHOP_GET, true);
        //记录日志
        logger.info("BackToNewFlyActivity exchange playerId:{}, exchangeId:{}, num:{}", playerId, exchangeId, num);
        //syncActivityDataInfo(playerId);
        Activity.ReturnUpgradeExchangeResp.Builder builder = Activity.ReturnUpgradeExchangeResp.newBuilder();
        builder.setGoodsId(exchangeId);
        builder.setExhangeTimes(newNum);
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.RETURN_UPGRADE_EXCHANGE_RESP, builder));
        //返回兑换状态
        return Result.success(newNum);
    }

    public Result<Integer> buy(String playerId, int num) {
        //判断活动是否开启，如果没开返回错误码
        if (!isShow(playerId)) {
            return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
        }
        //如果数据为空，返回错误码
        Optional<ReturnUpgradeEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        ReturnUpgradeEntity entity = opEntity.get();
        ReturnUpgradeKvCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(ReturnUpgradeKvCfg.class);
        //当前已经兑换数量
        int buyNum = entity.getGoldBuyCount();
        //兑换后的数量
        int newNum = buyNum + num;
        //判断是否超过可兑换数量最大值，如果超过返回错误码
        if (newNum > kvCfg.getItemLimit()) {
            return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
        }
        //兑换消耗
        boolean flag = this.getDataGeter().cost(playerId, kvCfg.getItemCostList(), num, Action.RETURN_UPGRADE_SHOP_COST, true);
        //如果不够消耗，返回错误码
        if (!flag) {
            return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
        }
        //设置新的兑奖数量
        entity.setGoldBuyCount(newNum);
        entity.notifyUpdate();
        //发奖
        this.getDataGeter().takeReward(playerId, kvCfg.getItemUseList(), num, Action.RETURN_UPGRADE_SHOP_GET, true);
        //记录日志
        logger.info("ReturnUpgradeActivity buy playerId:{}, num:{}", playerId, num);
        Activity.ReturnUpgradeBuyResp.Builder builder = Activity.ReturnUpgradeBuyResp.newBuilder();
        builder.setBuyTimes(newNum);
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.RETURN_UPGRADE_BUY_RESP, builder));
        return Result.success();
    }

    public Result<Integer> techInfo(String playerId) {
        int [] typeList = {1,2,3,5,9,10,11,12,13,14,15,16};
        Activity.ReturnUpgradeTechResp.Builder builder =  Activity.ReturnUpgradeTechResp.newBuilder();
        //如果数据为空，返回错误码
        Optional<ReturnUpgradeEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        ReturnUpgradeEntity entity = opEntity.get();
        Map<Integer, Integer> beforeMap = getDataGeter().getTechTypeMap(playerId);
        Map<Integer, Integer> afterMap = getDataGeter().getTechLevelTypeMap(entity.getBaseAfterLevel());
        Map<Integer, Integer> maxMap = getDataGeter().getTechTypeMaxMap();
        for(int type : typeList){
            Activity.ReturnUpgradeTechItem.Builder item = Activity.ReturnUpgradeTechItem.newBuilder();
            item.setTechId(type);
            item.setBefore(beforeMap.getOrDefault(type, 0));
            item.setAfter(afterMap.getOrDefault(type, 0));
            item.setMax(maxMap.getOrDefault(type, 0));
            builder.addItems(item);
        }
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.RETURN_UPGRADE_TECH_RESP, builder));
        return Result.success();
    }
}
