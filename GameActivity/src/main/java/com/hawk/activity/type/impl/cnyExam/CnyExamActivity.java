package com.hawk.activity.type.impl.cnyExam;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.CnyExamBuyEvent;
import com.hawk.activity.event.impl.CnyExamLoginEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.cnyExam.cfg.CnyExamAchieveCfg;
import com.hawk.activity.type.impl.cnyExam.cfg.CnyExamKvCfg;
import com.hawk.activity.type.impl.cnyExam.cfg.CnyExamLevelCfg;
import com.hawk.activity.type.impl.cnyExam.entity.CnyExamEntity;
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
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 新春试炼（步步高升）
 * 活动id - 338
 */
public class CnyExamActivity extends ActivityBase implements AchieveProvider {

    /**
     * 构造函数
     * @param activityId 活动id
     * @param activityEntity 活动数据库实例
     */
    public CnyExamActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    /**
     * 获得活动类型
     * @return
     */
    @Override
    public ActivityType getActivityType() {
        return ActivityType.CNY_EXAM_ACTIVITY;
    }

    @Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
    
    /**
     * 活动实例化
     * @param config 活动配置
     * @param activityEntity 活动数据库实例
     * @return
     */
    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        //创建活动实例
        CnyExamActivity activity = new CnyExamActivity(config.getActivityId(), activityEntity);
        //注册活动成就
        AchieveContext.registeProvider(activity);
        //返回活动实例
        return activity;
    }

    /**
     * 从数据库加载玩家数据
     * @param playerId 玩家id
     * @param termId 期数id
     * @return 玩家活动数据实例
     */
    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<CnyExamEntity> queryList = HawkDBManager.getInstance()
                .query("from CnyExamEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        //如果有数据的话，返回第一个数据
        if (queryList != null && queryList.size() > 0) {
            //获得第一个数据
            CnyExamEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    /**
     * 创建玩家数据
     * @param playerId 玩家id
     * @param termId 活动数据
     * @return 玩家数据实例
     */
    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        //创建活动数据
        CnyExamEntity entity = new CnyExamEntity(playerId, termId);
        return entity;
    }

    @Override
    public void onOpen() {
        Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
        for(String playerId : onlinePlayerIds){
            callBack(playerId, GameConst.MsgId.CNY_EXAM_ACTIVITY_OPEN, () -> {
            	long time = HawkTime.getMillisecond();
                info(playerId);
                initAchieve(playerId);
                Optional<CnyExamEntity> opEntity = getPlayerDataEntity(playerId);
                //如果数据不为空
                if (opEntity.isPresent()) {
                    //获得玩家活动数据实体
                    CnyExamEntity entity = opEntity.get();
                    long now = HawkTime.getMillisecond();
                    if(!HawkTime.isSameDay(now, entity.getLoginTime())) {
                        entity.setLoginDays(entity.getLoginDays() + 1);
                        entity.setLoginTime(now);
                        //更新累计登录成就
                        ActivityManager.getInstance().postEvent(new CnyExamLoginEvent(playerId, entity.getLoginDays()));
                    }
                }
                long gap = HawkTime.getMillisecond() - time;
                if (gap > 30L) {
                	HawkLog.logPrintln("bubugaosheng activity open callback, playerId: {}, costtime: {}", playerId, gap);
                }
            });
        }
    }

    /**
     * 是否活跃
     * @param playerId 玩家id
     * @return 是否活跃
     */
    @Override
    public boolean isProviderActive(String playerId) {
        //活动开就激活
        return isOpening(playerId);
    }

    /**
     * 是否显示
     * @param playerId 玩家id
     * @return 是否显示
     */
    @Override
    public boolean isProviderNeedSync(String playerId) {
        //活动展示就同步
        return isShow(playerId);
    }

    /**
     * 是否更新
     * @param playerId 玩家id
     * @param achieveId 成就id
     * @return 是否更新
     */
    @Override
    public boolean isProviderNeedUpdate(String playerId, int achieveId) {
        //获得成就配置
        CnyExamAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CnyExamAchieveCfg.class, achieveId);
        //配置为空，返回不可更新
        if(cfg == null){
            return false;
        }
        //第几天
        int curDay = getCurDay();
        //未解锁不更新
        if(curDay < cfg.getDay()){
            return false;
        }
        return true;
    }

    /**
     * 获得成就数据
     * @param playerId 玩家id
     * @return 成就数据
     */
    @Override
    public Optional<AchieveItems> getAchieveItems(String playerId) {
        //获得玩家活动数据
        Optional<CnyExamEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        //获得玩家活动数据实体
        CnyExamEntity entity = opEntity.get();
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
        Optional<CnyExamEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        CnyExamEntity entity = opEntity.get();
        //如果成就数据为空就初始化成就数据
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        //遍历成就配置
        ConfigIterator<CnyExamAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(CnyExamAchieveCfg.class);
        List<AchieveItem> list = new ArrayList<>();
        for(CnyExamAchieveCfg cfg : iterator){
            //实例化成就数据
            AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
            list.add(item);
        }
        //成就数据入库
        entity.setItemList(list);
        //成就创建事件
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()));
    }

    /**
     * 活动成就配置
     * @param achieveId
     * @return
     */
    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        //获得成就配置
        CnyExamAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CnyExamAchieveCfg.class, achieveId);
        //返回成就配置
        return cfg;
    }

    /**
     * 获得成就action
     * @return 成就action
     */
    @Override
    public Action takeRewardAction() {
        return Action.CNY_EXAM_ACHIEVE_REWARD;
    }

    /**
     * 成功领取后加分
     * @param playerId
     * @param reweardList
     * @param achieveId
     */
    @Override
    public void onTakeRewardSuccessAfter(String playerId, List<Reward.RewardItem.Builder> reweardList, int achieveId) {
    	long time = HawkTime.getMillisecond();
        //获得玩家活动数据
        Optional<CnyExamEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        CnyExamEntity entity = opEntity.get();
        CnyExamKvCfg cfg = HawkConfigManager.getInstance().getKVInstance(CnyExamKvCfg.class);
        //计算个数
        int count = 0;
        //遍历奖励
        for(Reward.RewardItem.Builder item : reweardList){
            if(item.getItemId() == cfg.getScoreItem()){
                count += item.getItemCount();
            }
        }
        //添加分数
        entity.setScore(entity.getScore() + count);
        //计算新等级
        int newLevel = calLevel(entity.getScore());
        //老等级
        int oldLevel = entity.getLevel();
        //构建前端数据
        Activity.CNYExamUpdate.Builder builder = Activity.CNYExamUpdate.newBuilder();
        //分数
        builder.setScore(entity.getScore());
        //如果等级变化
        if(newLevel > oldLevel){
            //等级
            entity.setLevel(newLevel);
            //构建等级变化了的数据
            for(int i = oldLevel + 1; i <= newLevel; i++){
                builder.addLevelInfos(buildLevelInfo(entity, i));
            }
        }
        //等级
        builder.setLevel(entity.getLevel());
        //天数
        builder.setCurDay(getCurDay());
        //发送给前端
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.CNY_EXAM_UPDATE, builder));
        long gap = HawkTime.getMillisecond() - time;
        if (gap > 30L) {
        	HawkLog.logPrintln("bubugaosheng activity achieve reward after process, playerId: {}, achieveId: {}, costtime: {}", playerId, achieveId, gap);
        }
    }

    /**
     * 同步前端数据
     * @param playerId 玩家id
     */
    @Override
    public void syncActivityDataInfo(String playerId) {
        info(playerId);
    }

    /**
     * 当前是活动第几天
     * @return
     */
    public int getCurDay(){
        //现在时间戳
        long now  = HawkTime.getMillisecond();
        //活动期数
        int termId = getActivityTermId();
        //开始时间
        long startTime = getTimeControl().getStartTimeByTermId(termId);
        //定位到0点
        startTime = HawkTime.getAM0Date(new Date(startTime)).getTime();
        //计算天数
        return (int)Math.ceil((now - startTime) * 1.0f / TimeUnit.DAYS.toMillis(1));
    }


    /**
     * 计算等级
     * @param score 分数
     * @return 等级
     */
    public int calLevel(int score){
        int level = 0;
        //遍历配置找到最高等级
        CnyExamLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CnyExamLevelCfg.class, level + 1);
        while (cfg != null && score >= cfg.getScore()){
            level++;
            cfg = HawkConfigManager.getInstance().getConfigByKey(CnyExamLevelCfg.class, level + 1);
        }
        //返回等级
        return level;
    }

    /**
     * 构建等级信息
     * @param entity 玩家活动数据
     * @param level 构建等级
     * @return
     */
    public Activity.CNYExamLevel.Builder buildLevelInfo(CnyExamEntity entity, int level){
        //构建等级数据
        Activity.CNYExamLevel.Builder builder = Activity.CNYExamLevel.newBuilder();
        //等级
        builder.setLevel(level);
        //选择1
        builder.setChoose1(entity.getChooseMap1().getOrDefault(level, -1));
        //选择2
        builder.setChoose2(entity.getChooseMap2().getOrDefault(level, -1));
        //是否已经直购
        builder.setIsBuy(entity.getBuyMap().getOrDefault(level, 0) == 1);
        //是否已经领奖
        builder.setIsReceived(entity.getTakeMap().getOrDefault(level, 0) == 1);
        //返回
        return builder;
    }

    /**
     * 监听购买事件
     * @param event 购买事件
     */
    @Subscribe
    public void onAuthBuyEvent(CnyExamBuyEvent event){
        //礼包id
        String payGiftId = event.getPayGiftId();
        //转换参数类型
        int id = Integer.valueOf(payGiftId);
        //玩家id
        String playerId = event.getPlayerId();
        CnyExamLevelCfg cfg = CnyExamLevelCfg.getCfgByPayId(id);
        if(cfg == null){
            return;
        }
        //是否满足购买条件
        if(!checkAuthBuy(playerId, payGiftId)){
            return;
        }
        //获得玩家活动数据
        Optional<CnyExamEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        CnyExamEntity entity = opEntity.get();
        //设置已购买
        entity.getBuyMap().put(cfg.getLevel(), 1);
        //设置已领取
        entity.getTakeMap().put(cfg.getLevel(), 1);
        //入库
        entity.notifyUpdate();
        //构建奖励
        List<Reward.RewardItem.Builder> list = new ArrayList<>();
        //固定奖励
        list.addAll(cfg.getGainList());
        //奖励1列表
        List<Reward.RewardItem.Builder> chooseList1 = cfg.getChooseList1();
        //奖励2列表
        List<Reward.RewardItem.Builder> chooseList2 = cfg.getChooseList2();
        //奖励1索引
        int chooseIndex1 = entity.getChooseMap1().getOrDefault(cfg.getLevel(), -1);
        //奖励2索引
        int chooseIndex2 = entity.getChooseMap2().getOrDefault(cfg.getLevel(), -1);
        //奖励1
        if(chooseIndex1 != -1){
            list.add(chooseList1.get(chooseIndex1));
        }
        //奖励2
        if(chooseIndex2 != -1){
            list.add(chooseList2.get(chooseIndex2));
        }
        //发奖
        this.getDataGeter().takeReward(entity.getPlayerId(), list, 1 , Action.CNY_EXAM_ACHIEVE_REWARD, true);
        //构建前端数据
        Activity.CNYExamUpdate.Builder builder = Activity.CNYExamUpdate.newBuilder();
        //分数
        builder.setScore(entity.getScore());
        //等级
        builder.setLevel(entity.getLevel());
        //天数
        builder.setCurDay(getCurDay());
        //等级信息
        builder.addLevelInfos(buildLevelInfo(entity, cfg.getLevel()));
        //发送给前端
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.CNY_EXAM_UPDATE, builder));
        //收据邮件
        sendMailToPlayer(playerId, MailConst.MailId.CNY_EXAM_BUY, null, null, null, list, true);
    }

     /**
     * 判断礼包是否可以购买
     * @param playerId 玩家id
     * @param payGiftId 礼包id
     * @return 是否可以购买
     */
    public boolean checkAuthBuy(String playerId, String payGiftId){
        if(!isOpening(playerId)){
            return false;
        }
        //转换参数类型
        int id = Integer.valueOf(payGiftId);
        CnyExamLevelCfg cfg = CnyExamLevelCfg.getCfgByPayId(id);
        if(cfg == null){
            return false;
        }
        //获得玩家活动数据
        Optional<CnyExamEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return false;
        }
        //现在第几天
        int curDay = getCurDay();
        //天数不够不可购买
        if(curDay < cfg.getDay()){
            return false;
        }
        //获得玩家活动数据实体
        CnyExamEntity entity = opEntity.get();
        if(entity.getBuyMap().getOrDefault(cfg.getLevel(), 0) == 1){
            return false;
        }
        //前一等级配置
        boolean check = this.checkFrontLevelTake(entity, cfg);
        if(!check){
        	return false;
        }
        //是否设置自选
        List<Reward.RewardItem.Builder> chooseList1 = cfg.getChooseList1();
        List<Reward.RewardItem.Builder> chooseList2 = cfg.getChooseList2();
        int chooseIndex1 = entity.getChooseMap1().getOrDefault(cfg.getLevel(), -1);
        int chooseIndex2 = entity.getChooseMap2().getOrDefault(cfg.getLevel(), -1);
        if((chooseList1.size() > 0 &&  chooseIndex1 == -1) ||(chooseList2.size() > 0 &&  chooseIndex2 == -1)){
            return false;
        }
        //可以购买
        return true;
    }


    /**
     * 跨天处理
     * @param event 跨天时间
     */
    @Subscribe
    public void onContinueLoginEvent(ContinueLoginEvent event) {
        //玩家id
        String playerId = event.getPlayerId();
        //如果活动没开返回
        if(!isOpening(playerId)){
            return;
        }
        //获得玩家活动数据
        Optional<CnyExamEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据实体
        CnyExamEntity entity = opEntity.get();
        long now = HawkTime.getMillisecond();
        if(!HawkTime.isSameDay(now, entity.getLoginTime())){
            entity.setLoginDays(entity.getLoginDays() + 1);
            entity.setLoginTime(now);
            //构建前端数据
            Activity.CNYExamUpdate.Builder builder = Activity.CNYExamUpdate.newBuilder();
            //分数
            builder.setScore(entity.getScore());
            //等级
            builder.setLevel(entity.getLevel());
            //当前天数
            builder.setCurDay(getCurDay());
            //发送给前端
            PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.CNY_EXAM_UPDATE, builder));
        }
        //更新累计登录成就
        ActivityManager.getInstance().postEvent(new CnyExamLoginEvent(playerId, entity.getLoginDays()));
    }

    /**
     * 活动数据
     * @param playerId 玩家数据
     * @return 执行结果
     */
    public Result<Integer> info(String playerId){
        //获得玩家活动数据
        Optional<CnyExamEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        CnyExamEntity entity = opEntity.get();
        //构建前端数据
        Activity.CNYExamInfoSync.Builder builder = Activity.CNYExamInfoSync.newBuilder();
        //天数
        builder.setCurDay(getCurDay());
        //分数
        builder.setScore(entity.getScore());
        //等级
        builder.setLevel(entity.getLevel());
        //遍历等级配置
        ConfigIterator<CnyExamLevelCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(CnyExamLevelCfg.class);
        for(CnyExamLevelCfg cfg : iterator){
            //构建等级数据
            builder.addLevelInfos(buildLevelInfo(entity, cfg.getLevel()));
        }
        //发送给前端
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.CNY_EXAM_INFO_SYNC, builder));
        //返回执行成功
        return Result.success();
    }

    /**
     * 选择
     * @param playerId 玩家id
     * @param level 选择等级
     * @param choose1 选择一索引
     * @param choose2 选择二索引
     * @return 返回执行结果
     */
    public Result<Integer> choose(String playerId, int level, int choose1, int choose2){
        //获得奖励配置
        CnyExamLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CnyExamLevelCfg.class, level);
        //配置为空，直接返回错误码
        if(cfg == null){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //选择1奖励列表
        List<Reward.RewardItem.Builder> chooseList1 = cfg.getChooseList1();
        //选择2奖励列表
        List<Reward.RewardItem.Builder> chooseList2 = cfg.getChooseList2();
        //如果不可自选直接返回错误码
        if(chooseList1.size() <= 0 || chooseList2.size() <= 0){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //校验选择参数是否合理，不合理直接返回错误码
        if(choose1 < 0 || choose2 < 0 || choose1 >= chooseList1.size() || choose2 >= chooseList2.size()){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据
        Optional<CnyExamEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        CnyExamEntity entity = opEntity.get();
        //设置选择1
        entity.getChooseMap1().put(level, choose1);
        //设置选择2
        entity.getChooseMap2().put(level, choose2);
        //入库
        entity.notifyUpdate();
        //构建前端数据
        Activity.CNYExamUpdate.Builder builder = Activity.CNYExamUpdate.newBuilder();
        //分数
        builder.setScore(entity.getScore());
        //等级
        builder.setLevel(entity.getLevel());
        //天数
        builder.setCurDay(getCurDay());
        //等级信息
        builder.addLevelInfos(buildLevelInfo(entity, level));
        //发送给前端
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.CNY_EXAM_UPDATE, builder));
        //返回执行成功
        return Result.success();
    }

    /**
     * 领奖
     * @param playerId
     * @param level
     * @return
     */
    public Result<Integer> award(String playerId, int level){
        //获得奖励配置
        CnyExamLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CnyExamLevelCfg.class, level);
        //配置为空，直接返回错误码
        if(cfg == null){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //如果奖励需要直购，直接返回错误码，直购奖励不能直接领奖
        if(cfg.getAndroid() != 0 || cfg.getIos() != 0){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        if(getCurDay() < cfg.getDay()){
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据
        Optional<CnyExamEntity> opEntity = getPlayerDataEntity(playerId);
        //如果数据为空直接返回
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据实体
        CnyExamEntity entity = opEntity.get();
        if(cfg.getDayLevel() > 1 && level > entity.getLevel()){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //是否已经领过，如果领过直接返回错误码
        if(entity.getTakeMap().getOrDefault(level, 0) == 1){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //检查前置是否领取
        boolean check = this.checkFrontLevelTake(entity, cfg);
        if(!check && cfg.getDayLevel() > 1){
        	return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //自选奖励1列表
        List<Reward.RewardItem.Builder> chooseList1 = cfg.getChooseList1();
        //自选奖励2列表
        List<Reward.RewardItem.Builder> chooseList2 = cfg.getChooseList2();
        //自选奖励1索引
        int chooseIndex1 = entity.getChooseMap1().getOrDefault(level, -1);
        //自选奖励2索引
        int chooseIndex2 = entity.getChooseMap2().getOrDefault(level, -1);
        //如果没选，返回错误码
        if((chooseList1.size() > 0 &&  chooseIndex1 == -1) ||(chooseList2.size() > 0 &&  chooseIndex2 == -1)){
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //设置领取标志
        entity.getTakeMap().put(level, 1);
        //入库
        entity.notifyUpdate();
        //拼装奖励
        List<Reward.RewardItem.Builder> list = new ArrayList<>();
        //固定奖励
        list.addAll(cfg.getGainList());
        //自选奖励1
        if(chooseIndex1 != -1){
            list.add(chooseList1.get(chooseIndex1));
        }
        //自选奖励2
        if(chooseIndex2 != -1){
            list.add(chooseList2.get(chooseIndex2));
        }
        //发奖
        this.getDataGeter().takeReward(entity.getPlayerId(), list, 1 , Action.CNY_EXAM_ACHIEVE_REWARD, true);
        //构建前端数据
        Activity.CNYExamUpdate.Builder builder = Activity.CNYExamUpdate.newBuilder();
        //分数
        builder.setScore(entity.getScore());
        //等级
        builder.setLevel(entity.getLevel());
        //天数
        builder.setCurDay(getCurDay());
        //等级信息
        builder.addLevelInfos(buildLevelInfo(entity, level));
        //发送给前端
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.CNY_EXAM_UPDATE, builder));
        //返回执行成功
        return Result.success();
    }
    
    
    
    public Set<Integer> getUnlockLevelDays(CnyExamEntity entity){
    	Set<Integer> rlt = new HashSet<>();
    	int curDay = this.getCurDay();
    	List<AchieveItem>list = entity.getItemList();
    	for(AchieveItem item : list){
    		CnyExamAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CnyExamAchieveCfg.class, item.getAchieveId());
    		if(Objects.isNull(cfg)){
    			continue;
    		}
    		if(item.getState() != AchieveState.TOOK_VALUE){
    			continue;
    		}
    		
    		if(cfg.getDay() > curDay){
    			continue;
    		}
    		rlt.add(cfg.getDay());
    	}
    	return rlt;
    }
    
    
    public boolean checkFrontLevelTake(CnyExamEntity entity,CnyExamLevelCfg cfg){
    	Set<Integer> set = this.getUnlockLevelDays(entity);
    	if(!set.contains(cfg.getDay())){
    		return false;
    	}
    	if(cfg.getDayLevel()>1){
    		CnyExamLevelCfg bef = HawkConfigManager.getInstance().getCombineConfig(CnyExamLevelCfg.class,cfg.getDay(), cfg.getDayLevel() -1);
    		if(Objects.isNull(bef)){
    			return false;
    		}
    		if(!entity.getTakeMap().containsKey(bef.getLevel())){
    			return false;
    		}
    	}
    	return true;
    }
}
