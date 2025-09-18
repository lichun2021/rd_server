package com.hawk.activity.type.impl.developFastOld;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.*;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.developFastOld.cfg.*;
import com.hawk.activity.type.impl.developFastOld.entity.DevelopFastOldEntity;
import com.hawk.activity.type.impl.developFastOld.entity.DevelopFastOldTask;
import com.hawk.game.protocol.*;
import com.hawk.log.Action;
import org.apache.commons.lang.StringUtils;
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

/**
 * 实力飞升活动 活动id335
 * 继承：ActivityBase 活动基础类
 * 实现：AchieveProvider 任务成就接口
 */
public class DevelopFastOldActivity extends ActivityBase implements AchieveProvider {
    private static final Logger logger = LoggerFactory.getLogger("Server");
    /**
     * 构造函数
     * @param activityId 活动id
     * @param activityEntity 活动数据库实体
     */
    public DevelopFastOldActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    /**
     * 获得活动枚举
     * @return 活动枚举
     */
    @Override
    public ActivityType getActivityType() {
        return ActivityType.DEVELOP_FAST_OLD;
    }
    
    @Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

    /**
     * 新建活动实例
     * @param config 活动配置activity.xml
     * @param activityEntity 活动数据库实体
     * @return
     */
    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        //创建活动实例
        DevelopFastOldActivity activity = new DevelopFastOldActivity(config.getActivityId(), activityEntity);
        //注册活动 用于接收成就任务数据
        AchieveContext.registeProvider(activity);
        return activity;
    }

    /**
     * 从数据库加载玩家数据
     * @param playerId 玩家id
     * @param termId 活动期数
     * @return 玩家一期活动数据
     */
    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        //根据条件从数据库中检索
        List<DevelopFastOldEntity> queryList = HawkDBManager.getInstance()
                .query("from DevelopFastOldEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        //如果有数据的话，返回第一个数据
        if (queryList != null && queryList.size() > 0) {
            DevelopFastOldEntity entity = queryList.get(0);
            return entity;
        }
        //否则返回空
        return null;
    }

    /**
     * 构建玩家一期活动数据
     * @param playerId 玩家id
     * @param termId 活动期数
     * @return 玩家一期活动数据
     */
    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        DevelopFastOldEntity entity = new DevelopFastOldEntity(playerId, termId);
        return entity;
    }

    /**
     * 活动开启自定义条件判断
     * @param playerId
     * @return
     */
    @Override
    public boolean isActivityClose(String playerId) {
        //获得本服开服时间
        long openTime = getDataGeter().getServerOpenDate();
        //活动活动总配置
        DevelopFastOldKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DevelopFastOldKVCfg.class);
        //如果开服时间早于配置时间
        if(openTime >= cfg.getServerOpenTimeValue()){
            return true;
        }
        return false;
    }

    @Override
    public void onPlayerLogin(String playerId) {
        rewardSupplement(playerId);
        //判断活动是否开启
        if(!isOpening(playerId)){
            return;
        }
        //获得玩家当期数据
        Optional<DevelopFastOldEntity> opEntity = getPlayerDataEntity(playerId);
        //不存在则返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家当期数据
        DevelopFastOldEntity entity = opEntity.get();
        //如果自定义任务数据不存在就生成一下
        if(entity.getTaskList().isEmpty()){
            //获得自定义任务配置
            ConfigIterator<DevelopFastOldLevelCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(DevelopFastOldLevelCfg.class);
            List<DevelopFastOldTask> list = new ArrayList<>();
            while (iterator.hasNext()){
                //构建自定义任务实例
                DevelopFastOldLevelCfg cfg = iterator.next();
                //添加进自定义任务列表中
                list.add(DevelopFastOldTask.valueOf(cfg.getId()));
            }
            //存数据
            entity.setTaskList(list);
        }
        //大本等级
        int baseLevel = getDataGeter().getConstructionFactoryLevel(playerId);
        //玩家科技战力
        int power = getDataGeter().getPowerData(playerId).getTechBattlePoint();
        //触发大本任务
        reachCondition(playerId, Activity.DevelopFastTaskType.DF_BASE_LEVEL_VALUE, baseLevel);
        //触发科技战力任务
        reachCondition(playerId, Activity.DevelopFastTaskType.DF_SCIENCE_VALUE, power);
        reachCondition(playerId, Activity.DevelopFastTaskType.DF_LAB_VALUE, entity.getScoreMap().getOrDefault(Activity.DevelopFastTaskType.DF_LAB_VALUE, 0));
        checkBuyAll(entity);

    }

    //给前端同步科技战力数据
    @Override
    public void syncActivityDataInfo(String playerId) {
        //判断活动是否开启
        if(!isOpening(playerId)){
            return;
        }
        //获得当期活动数据
        Optional<DevelopFastOldEntity> opEntity = getPlayerDataEntity(playerId);
        //获得不到就返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得当期活动数据
        DevelopFastOldEntity entity = opEntity.get();
        //构建前端数据
        Activity.DevelopFastSync.Builder builder = Activity.DevelopFastSync.newBuilder();
        //添加大本任务数据
        builder.addTasks(makeBuilder(Activity.DevelopFastTaskType.DF_BASE_LEVEL_VALUE, entity));
        //添加科技任务数据
        builder.addTasks(makeBuilder(Activity.DevelopFastTaskType.DF_SCIENCE_VALUE, entity));
        //添加超能实验室任务数据
        builder.addTasks(makeBuilder(Activity.DevelopFastTaskType.DF_LAB_VALUE, entity));
        //添加总任务数据
        builder.addTasks(makeBuilder(Activity.DevelopFastTaskType.DF_LEVEL_VALUE, entity));
        //同步给前端
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.DEVELOP_FAST_OLD_SYNC, builder));
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
        String spKey = playerId + ":DevelopFastOldSupplement:" + lastTermId;
        String val = ActivityGlobalRedis.getInstance().getRedisSession().getString(spKey);
        if (StringUtils.isNotEmpty(val)){
            return;
        }
        ActivityGlobalRedis.getInstance().getRedisSession().setString(spKey, spKey);
        HawkDBEntity dbEntity = PlayerDataHelper.getInstance().getActivityDataEntity(playerId, getActivityType());
        if (dbEntity == null) {
            dbEntity = this.loadFromDB(playerId, lastTermId);
            if(null == dbEntity){
                return;
            }
            PlayerDataHelper.getInstance().putActivityDataEntity(playerId, getActivityType(), dbEntity);
        }
        DevelopFastOldEntity entity = (DevelopFastOldEntity)dbEntity;
        Map<Integer,Integer> buyMap = entity.getBuyMap();
        List<Reward.RewardItem.Builder> list = new ArrayList<>();
        //遍历任务
        for(DevelopFastOldTask developFastOldTask : entity.getTaskList()) {
            if(developFastOldTask.getState() == Activity.DevelopFastTaskState.DF_NOT_RECEIVED_VALUE){
                DevelopFastOldLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DevelopFastOldLevelCfg.class, developFastOldTask.getTaskId());
                int buy = buyMap.getOrDefault(cfg.getType(), 0);
                if(buy == 0){
                    list.addAll(cfg.getRewardList());
                    developFastOldTask.setState(Activity.DevelopFastTaskState.DF_RECEIVED_VALUE);
                }else {
                    list.addAll(cfg.getRewardList());
                    list.addAll(cfg.getRewardList());
                    developFastOldTask.setState(Activity.DevelopFastTaskState.DF_RECEIVED_TWO_VALUE);
                }
            }else if(developFastOldTask.getState() == Activity.DevelopFastTaskState.DF_RECEIVED_VALUE){
                DevelopFastOldLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DevelopFastOldLevelCfg.class, developFastOldTask.getTaskId());
                int buy = buyMap.getOrDefault(cfg.getType(), 0);
                if(buy != 0){
                    list.addAll(cfg.getRewardList());
                    developFastOldTask.setState(Activity.DevelopFastTaskState.DF_RECEIVED_TWO_VALUE);
                }
            }
        }
        for(AchieveItem achieveItem : entity.getItemList()) {
            if(achieveItem.getState() != Activity.AchieveState.NOT_REWARD_VALUE){
                continue;
            }
            DevelopFastOldAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DevelopFastOldAchieveCfg.class, achieveItem.getAchieveId());
            int buy = buyMap.getOrDefault(cfg.getCategory(), 0);
            if(buy == 0){
                list.addAll(cfg.getRewardList());
            }else {
                list.addAll(cfg.getRewardList());
                list.addAll(cfg.getRewardList());
            }
        }
        entity.notifyUpdate();
        if(list.size() > 0){
            sendMailToPlayer(playerId, MailConst.MailId.DEVELOP_FAST_SUPPLEMENT, null, null, null, RewardHelper.mergeRewardItem(list), false);
        }
    }


    private int getLastTermId() {
        long curTime = HawkTime.getMillisecond();
        DevelopFastOldTimeCfg lastCfg = null;
        List<DevelopFastOldTimeCfg> list = HawkConfigManager.getInstance().getConfigIterator(DevelopFastOldTimeCfg.class).toList();
        for(DevelopFastOldTimeCfg cfg : list){
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

    /**
     * 构建任务数据
     * @param type 任务类型
     * @param entity 玩家数据
     * @return 前端任务数据
     */

    public Activity.DevelopFastTask.Builder makeBuilder(int type, DevelopFastOldEntity entity){
        //构建前端数据
        Activity.DevelopFastTask.Builder task = Activity.DevelopFastTask.newBuilder();
        //是否购买当前养成线直购
        task.setIsBuy(entity.getBuyMap().getOrDefault(type, 0) == 1);
        //当前积分
        int score = entity.getScoreMap().getOrDefault(type, 0);
        int level = calLevel(type, score);
        //设置type
        task.setType(Activity.DevelopFastTaskType.valueOf(type));
        //设置积分
        task.setScore(score);
        //计算并设置等级
        task.setLevel(level);
        //遍历任务
        for(DevelopFastOldTask developFastOldTask : entity.getTaskList()){
            //获得任务配置
            DevelopFastOldLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DevelopFastOldLevelCfg.class, developFastOldTask.getTaskId());
            //过滤非本养成线任务
            if(cfg.getType() != type){
                continue;
            }
            //添加数据
            task.addTasks(developFastOldTask.toPB());
        }
        //添加显示成就
        fillAchieveIds(task, entity, type, level);
        //返回数据
        return task;
    }

    /**
     * 填充成就id
     * @param task 养成线任务
     * @param entity 玩家数据
     * @param type 养成线type
     * @param level 养成线当前等级
     */
    public void fillAchieveIds(Activity.DevelopFastTask.Builder task, DevelopFastOldEntity entity, int type, int level){
        //获得全部成就
        List<AchieveItem> itemList = entity.getItemList();
        //成就分类
        Map<Integer, AchieveItem> itemMap = new HashMap<>();
        //各等级完成状态
        Map<Integer, Boolean> booleanMap = new HashMap<>();
        //等级类型分类
        Map<Integer, Map<Integer, List<DevelopFastOldAchieveCfg>>> sortMap = new HashMap<>();
        //遍历成就
        for(AchieveItem item : itemList){
            //获得成就配置
            DevelopFastOldAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DevelopFastOldAchieveCfg.class, item.getAchieveId());
            //过滤非当前养成线任务
            if(cfg.getCategory() != type){
                continue;
            }
            //分类
            itemMap.put(item.getAchieveId(), item);
            //如果有一个没领取完当前阶段就没结束
            if(item.getState() != Activity.AchieveState.TOOK_VALUE){
                booleanMap.put(cfg.getChapter(), true);
            }
            //根据等级分类
            if(!sortMap.containsKey(cfg.getChapter())){
                sortMap.put(cfg.getChapter(), new HashMap<>());
            }
            //根据type分类
            if(!sortMap.get(cfg.getChapter()).containsKey(cfg.getConditionType())){
                sortMap.get(cfg.getChapter()).put(cfg.getConditionType(), new ArrayList<>());
            }
            sortMap.get(cfg.getChapter()).get(cfg.getConditionType()).add(cfg);
        }
        //从1级遍历到当前等级
        for(int i = 1; i <= level; i++){
            //如果当前等级都完成了就跳过
            if(!booleanMap.getOrDefault(i, false)){
                continue;
            }
            //获得当前等级type分类
            Map<Integer, List<DevelopFastOldAchieveCfg>> map = sortMap.get(i);
            //如果为空直接跳过
            if(map == null){
                continue;
            }
            //当前显示
            Map<Integer, Integer> typeToAchieveId = new HashMap<>();
            //遍历
            for(int conditionType : map.keySet()){
                //排序
                List<DevelopFastOldAchieveCfg> list = map.get(conditionType);
                Collections.sort(list, new Comparator<DevelopFastOldAchieveCfg>() {
                    @Override
                    public int compare(DevelopFastOldAchieveCfg o1, DevelopFastOldAchieveCfg o2) {
                        if(o1.getShowOrder() != o2.getShowOrder()){
                            return o1.getShowOrder() > o2.getShowOrder() ? -1 : 1;
                        }
                        return 0;
                    }
                });
                //按顺序显示
                for(DevelopFastOldAchieveCfg cfg : list){
                    if(!typeToAchieveId.containsKey(conditionType)){
                        typeToAchieveId.put(conditionType, cfg.getAchieveId());
                    }else {
                        AchieveItem item = itemMap.get(cfg.getAchieveId());
                        if(item != null && item.getState() != Activity.AchieveState.TOOK_VALUE){
                            typeToAchieveId.put(conditionType, cfg.getAchieveId());
                        }
                    }
                }
            }
            //如果当前有数据
            if(typeToAchieveId.size()>0){
                task.addAllAchieveIds(typeToAchieveId.values());
                break;
            }
        }
    }

    /**
     * 计算对应养成线等级
     * @param type 养成线类型
     * @param score 养成线分数
     * @return 养成线等级
     */
    public int calLevel(int type, int score){
        //养成线配置
        ConfigIterator<DevelopFastOldLevelCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(DevelopFastOldLevelCfg.class);
        //初始等级为1
        int level = 1;
        //遍历配置
        while (iterator.hasNext()){
            DevelopFastOldLevelCfg cfg = iterator.next();
            //过滤非本养成线代码
            if(cfg.getType() != type){
                continue;
            }
            //过滤最高等级
            if(cfg.getConditionValue() == -1){
                continue;
            }
            //获取当前达到的等级
            if(score >= cfg.getConditionValue() && level < cfg.getChapter() + 1){
                level = cfg.getChapter() + 1;
            }
        }
        //返回等级
        return level;
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
        //获得配置
        DevelopFastOldAuthorityCfg cfg = getPayCfg(id);
        if(cfg == null){
            return false;
        }
        //获得当期活动数据
        Optional<DevelopFastOldEntity> opEntity = getPlayerDataEntity(playerId);
        //如果没有返回不可购买
        if (!opEntity.isPresent()) {
            return false;
        }
        //获得当期活动数据
        DevelopFastOldEntity entity = opEntity.get();
        //判断当前养成线是否购买了
        int buy = entity.getBuyMap().getOrDefault(cfg.getCategory(), 0);
        //如果已经购买了就不能买了
        if(buy != 0){
            return false;
        }
        //可以购买
        return true;
    }


    /**
     * 获得养成线礼包配置
     * @param payGiftId 礼包id
     * @return 返回养成线礼包配置
     */
    public DevelopFastOldAuthorityCfg getPayCfg(int payGiftId){
        //养成线礼包配置
        ConfigIterator<DevelopFastOldAuthorityCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(DevelopFastOldAuthorityCfg.class);
        //筛选出对应配置
        while (iterator.hasNext()){
            DevelopFastOldAuthorityCfg cfg = iterator.next();
            if(cfg.getAndroid() == payGiftId || cfg.getIos() == payGiftId){
                return cfg;
            }
        }
        //没有满足的就返回空
        return null;
    }

    /**
     * 检测是否都购买了
     * @param entity 玩家活动数据
     */
    public void checkBuyAll(DevelopFastOldEntity entity){
        //如果数据为空直接返回
        if(entity == null){
            return;
        }
        //总进度购买已经激活返回
        if(entity.getBuyMap().getOrDefault(Activity.DevelopFastTaskType.DF_LEVEL_VALUE, 0) != 0){
            return;
        }
        //如果养成线都购买了就激活
        if((entity.getBuyMap().getOrDefault(Activity.DevelopFastTaskType.DF_BASE_LEVEL_VALUE, 0) != 0)
                && (entity.getBuyMap().getOrDefault(Activity.DevelopFastTaskType.DF_SCIENCE_VALUE, 0) != 0)
                && (entity.getBuyMap().getOrDefault(Activity.DevelopFastTaskType.DF_LAB_VALUE, 0) != 0)){
            //激活总进度
            entity.getBuyMap().put(Activity.DevelopFastTaskType.DF_LEVEL_VALUE, 1);
            //入库
            entity.notifyUpdate();
        }
    }

    /**
     * 监听购买事件
     * @param event 购买事件
     */
    @Subscribe
    public void onAuthBuyEvent(DevelopFastBuyEvent event){
        //礼包id
        String payGiftId = event.getPayGiftId();
        //转换参数类型
        int id = Integer.valueOf(payGiftId);
        //玩家id
        String playerId = event.getPlayerId();
        if(!isOpening(playerId)){
            return;
        }
        //是否满足购买条件
        if(!checkAuthBuy(playerId, payGiftId)){
            return;
        }
        //礼包配置
        DevelopFastOldAuthorityCfg cfg = getPayCfg(id);
        //配置为空返回
        if(cfg == null){
            return;
        }
        //获得当期活动数据
        Optional<DevelopFastOldEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
        //如果没有就返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得当期活动数据
        DevelopFastOldEntity entity = opEntity.get();
        //获取购买状态
        int buy = entity.getBuyMap().getOrDefault(cfg.getCategory(), 0);
        //如果已经购买就返回
        if(buy != 0){
            return;
        }
        //设置购买状态
        entity.getBuyMap().put(cfg.getCategory(), 1);
        //入库
        entity.notifyUpdate();
        //加buff
        int termId = getActivityTermId();
        //this.getDataGeter().addBuff(playerId, cfg.getBuffId(), getTimeControl().getEndTimeByTermId(termId));
        if(cfg.getCategory() == Activity.DevelopFastTaskType.DF_BASE_LEVEL_VALUE){
            sendMailToPlayer(playerId, MailConst.MailId.DEVELOP_FAST_BASE, null, null, null, getDoneAchieveReward(entity,cfg.getCategory()), false);
        }else if(cfg.getCategory() == Activity.DevelopFastTaskType.DF_SCIENCE_VALUE){
            sendMailToPlayer(playerId, MailConst.MailId.DEVELOP_FAST_SCIENCE, null, null, null, getDoneAchieveReward(entity,cfg.getCategory()), false);
        }else if(cfg.getCategory() == Activity.DevelopFastTaskType.DF_LAB_VALUE){
            sendMailToPlayer(playerId, MailConst.MailId.DEVELOP_FAST_LAB, null, null, null, getDoneAchieveReward(entity,cfg.getCategory()), false);
        }
        //检查是否已经全部购买
        checkBuyAll(entity);
        //同步前端
        syncActivityDataInfo(event.getPlayerId());

    }

    private List<Reward.RewardItem.Builder> getDoneAchieveReward(DevelopFastOldEntity entity, int type){
        List<Reward.RewardItem.Builder> list = new ArrayList<>();
        for(AchieveItem item : entity.getItemList()){
            if(item.getState() != Activity.AchieveState.TOOK_VALUE){
                continue;
            }
            DevelopFastOldAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DevelopFastOldAchieveCfg.class, item.getAchieveId());
            if(cfg == null || cfg.getCategory() != type){
                continue;
            }
            list.addAll(cfg.getRewardList());
        }
        return RewardHelper.mergeRewardItem(list);
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
        //触发大本任务
        reachCondition(event.getPlayerId(), Activity.DevelopFastTaskType.DF_BASE_LEVEL_VALUE, event.getLevel());
    }

    /**
     * 监听监听战力提升事件
     * @param event 战力提升事件
     */
    @Subscribe
    public void onBattlePointChangeEvent(BattlePointChangeEvent event) {
        reachCondition(event.getPlayerId(), Activity.DevelopFastTaskType.DF_SCIENCE_VALUE, event.getPowerData().getTechBattlePoint());
    }


    /**
     * 监听超能实验室升级事件
     * @param event 超能实验室升级
     */
    @Subscribe
    public void onSuperLabLevelUpEvent(SuperLabLevelUpEvent event) {
        reachCondition(event.getPlayerId(), Activity.DevelopFastTaskType.DF_LAB_VALUE, event.getTotalLevel());
    }

    @Subscribe
    public void onContinueLoginEvent(ContinueLoginEvent event) {
        String playerId = event.getPlayerId();
        //如果活动没开返回
        if(!isOpening(playerId)){
            return;
        }
        //获得活动数据
        Optional<DevelopFastOldEntity> opEntity = getPlayerDataEntity(playerId);
        //如果没有数据返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得当期活动数据
        DevelopFastOldEntity entity = opEntity.get();
        long now = HawkTime.getMillisecond();
        if(!HawkTime.isSameDay(now, entity.getLoginTime())){
            entity.setLoginDays(entity.getLoginDays() + 1);
            entity.setLoginTime(now);
        }
        ActivityManager.getInstance().postEvent(new DevelopFastLoginEvent(playerId, entity.getLoginDays()));
    }


    /**
     * 触发型任务触发逻辑
     * @param playerId 玩家id
     * @param type 养成线type
     * @param value 养成线数值
     */
    public void reachCondition(String playerId, int type, int value){
        //如果活动没开返回
        if(!isOpening(playerId)){
            return;
        }
        //获得活动数据
        Optional<DevelopFastOldEntity> opEntity = getPlayerDataEntity(playerId);
        //如果没有数据返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得当期活动数据
        DevelopFastOldEntity entity = opEntity.get();
        //获得当前分
        int score = entity.getScoreMap().getOrDefault(type, 0);
        //分数没有增长返回
        if(value < score){
            return;
        }
        //分数赋值
        score = value;
        //入库
        entity.getScoreMap().put(type, score);
        //遍历任务
        for(DevelopFastOldTask developFastOldTask : entity.getTaskList()) {
            DevelopFastOldLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DevelopFastOldLevelCfg.class, developFastOldTask.getTaskId());
            //过滤非当前养成线的任务
            if (cfg.getType() != type) {
                continue;
            }
            //过滤已经完成的任务
            if(developFastOldTask.getState() != Activity.DevelopFastTaskState.DF_NOT_REACH_VALUE){
                continue;
            }
            //过滤最高等级任务
            if(cfg.getConditionValue() == -1){
                continue;
            }
            //满足条件的话改变任务状态
            if(score >= cfg.getConditionValue()){
                //设置任务状态为已完成
                developFastOldTask.setState(Activity.DevelopFastTaskState.DF_NOT_RECEIVED_VALUE);
            }
        }
        //入库
        entity.notifyUpdate();
        //同步数据
        syncActivityDataInfo(playerId);
        checkAchieveItem(entity, type, calLevel(type, score));
    }

    /**
     * 累加型任务触发逻辑
     * @param playerId
     * @param type
     * @param value
     */
    public void addCondition(String playerId, int type, int value){
        //增加值为小于等于零的话返回
        if(value <= 0){
            return;
        }
        //如果活动没开则返回
        if(!isOpening(playerId)){
            return;
        }
        //获得当前活动数据
        Optional<DevelopFastOldEntity> opEntity = getPlayerDataEntity(playerId);
        //没有的话返回
        if (!opEntity.isPresent()) {
            return;
        }
        //获得当期活动数据
        DevelopFastOldEntity entity = opEntity.get();
        //获得当前活动分数
        int score = entity.getScoreMap().getOrDefault(type, 0);
        //分数增加
        score = score + value;
        //入库
        entity.getScoreMap().put(type, score);
        //遍历任务
        for(DevelopFastOldTask developFastOldTask : entity.getTaskList()) {
            DevelopFastOldLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DevelopFastOldLevelCfg.class, developFastOldTask.getTaskId());
            //过滤非当前任务线任务
            if (cfg.getType() != type) {
                continue;
            }
            //过滤已完成任务
            if(developFastOldTask.getState() != Activity.DevelopFastTaskState.DF_NOT_REACH_VALUE){
                continue;
            }
            //过滤最高等级任务
            if(cfg.getConditionValue() == -1){
                continue;
            }
            //满足条件改变任务状态
            if(score >= cfg.getConditionValue()){
                //设置任务状态为已完成
                developFastOldTask.setState(Activity.DevelopFastTaskState.DF_NOT_RECEIVED_VALUE);
            }
        }
        //入库
        entity.notifyUpdate();
        //同步数据
        syncActivityDataInfo(playerId);
    }

    public void checkAchieveItem(DevelopFastOldEntity entity, int type, int level){
        List<AchieveItem> needUpdate = new ArrayList<>();
        for (AchieveItem item : entity.getItemList()){
            DevelopFastOldAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DevelopFastOldAchieveCfg.class, item.getAchieveId());
            if(cfg == null){
                continue;
            }
            if(cfg.getCategory() != type){
                continue;
            }
            if(cfg.getChapter() >= level){
                continue;
            }
            if(item.getState() != Activity.AchieveState.NOT_ACHIEVE_VALUE){
                continue;
            }
            item.setValue(0, cfg.getConditionValue(0));
            item.setState(Activity.AchieveState.NOT_REWARD_VALUE);
            needUpdate.add(item);
        }
        if(needUpdate.size() > 0){
            entity.notifyUpdate();
            AchievePushHelper.pushAchieveUpdate(entity.getPlayerId(), needUpdate);
        }
    }

    /**
     * 判断成就任务是否激活
     * @param playerId 玩家id
     * @return 是否激活
     */
    @Override
    public boolean isProviderActive(String playerId) {
        //活动开就激活
        return isOpening(playerId);
    }

    /**
     * 判断成就任务是否显示
     * @param playerId 玩家id
     * @return 是否显示
     */
    @Override
    public boolean isProviderNeedSync(String playerId) {
        //活动展示就同步
        return isShow(playerId);
    }

    @Override
    public boolean isProviderNeedUpdate(String playerId, int achieveId) {
        DevelopFastOldAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DevelopFastOldAchieveCfg.class, achieveId);
        if(cfg == null){
            return false;
        }
        int type = cfg.getCategory();
        //获得活动数据
        Optional<DevelopFastOldEntity> opEntity = getPlayerDataEntity(playerId);
        //如果没有返回false
        if (!opEntity.isPresent()) {
            return false;
        }
        //获得玩家活动数据实体
        DevelopFastOldEntity entity = opEntity.get();
        //获得当前活动分数
        int score = entity.getScoreMap().getOrDefault(type, 0);
        int level = calLevel(type, score);
        if(cfg.getChapter() > level){
            return false;
        }
        return true;
    }

    /**
     * 获得成就数据
     * @param playerId 玩家id
     * @return 成就任务数据
     */
    @Override
    public Optional<AchieveItems> getAchieveItems(String playerId) {
        //获得活动数据
        Optional<DevelopFastOldEntity> opEntity = getPlayerDataEntity(playerId);
        //如果没有返回空
        if (!opEntity.isPresent()) {
            return Optional.empty();
        }
        //获得玩家活动数据实体
        DevelopFastOldEntity entity = opEntity.get();
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
        //获得玩家当期数据
        Optional<DevelopFastOldEntity> opEntity = getPlayerDataEntity(playerId);
        //如果没有直接返回
        if (!opEntity.isPresent()) {
            return;
        }
        //活动数据
        DevelopFastOldEntity entity = opEntity.get();
        //如果成就任务数据不是空就返回
        if (!entity.getItemList().isEmpty()) {
            return;
        }
        //成就任务配置
        ConfigIterator<DevelopFastOldAchieveCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(DevelopFastOldAchieveCfg.class);
        List<AchieveItem> list = new ArrayList<>();
        //构造成就任务数据
        while (iterator.hasNext()) {
            DevelopFastOldAchieveCfg cfg = iterator.next();
            AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
            list.add(item);
        }
        //入库
        entity.setItemList(list);
        //成就生成事件
        ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()));
    }

    /**
     * 获得成就任务配置
     * @param achieveId
     * @return 成就任务配置
     */
    @Override
    public AchieveConfig getAchieveCfg(int achieveId) {
        DevelopFastOldAchieveCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DevelopFastOldAchieveCfg.class, achieveId);
        return cfg;
    }

    /**
     * 获得成就任务奖励
     * @param playerId 玩家id
     * @param achieveConfig 成就任务
     * @return
     */
    @Override
    public List<Reward.RewardItem.Builder> getRewardList(String playerId, AchieveConfig achieveConfig) {
        //获得初始奖励
        List<Reward.RewardItem.Builder> list = achieveConfig.getRewardList();
        //获得成就对应养成线
        int type = ((DevelopFastOldAchieveCfg)achieveConfig).getCategory();
        //获得玩家当期数据
        Optional<DevelopFastOldEntity> opEntity = getPlayerDataEntity(playerId);
        //如果没有直接返回
        if (!opEntity.isPresent()) {
            return list;
        }
        //活动数据
        DevelopFastOldEntity entity = opEntity.get();
        //是否已经购买
        int buy = entity.getBuyMap().getOrDefault(type, 0);
        //如果已经购买
        if(buy != 0){
            //奖励翻倍
            for(Reward.RewardItem.Builder item: list){
                item.setItemCount(item.getItemCount() * 2);
            }
        }
        return list;
    }

    /**
     * 获得成就任务奖励action
     * @return 成就任务奖励action
     */
    @Override
    public Action takeRewardAction() {
        return Action.DEVELOP_FAST_REWARD;
    }

    @Override
    public void onTakeRewardSuccessAfter(String playerId, List<Reward.RewardItem.Builder> reweardList, int achieveId) {
        syncActivityDataInfo(playerId);
    }

    /**
     * 前端请求活动信息
     * @param playerId 玩家id
     * @return 逻辑运行结果
     */
    public Result<Integer> info(String playerId){
        //同步数据
        syncActivityDataInfo(playerId);
        //返回成功
        return Result.success();
    }

    /**
     * 领取奖励
     * @param playerId 玩家id
     * @param taskId 任务id
     * @return 逻辑运行结果
     */
    public Result<Integer> award(String playerId, int taskId){
        //获得玩家数据
        Optional<DevelopFastOldEntity> opEntity = getPlayerDataEntity(playerId);
        //不存在返回错误码
        if (!opEntity.isPresent()) {
            return Result.fail(1);
        }
        //获得玩家数据
        DevelopFastOldEntity entity = opEntity.get();
        DevelopFastOldTask task = null;
        //遍历玩家任务
        for(DevelopFastOldTask developFastOldTask : entity.getTaskList()) {
            //筛选活动任务
            if(developFastOldTask.getTaskId() != taskId){
                continue;
            }else {
                task = developFastOldTask;
                break;
            }
        }
        //如果任务不存在返回错误码
        if(task == null){
            return Result.fail(1);
        }
        //获得配置
        DevelopFastOldLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DevelopFastOldLevelCfg.class, task.getTaskId());
        //如果配置不存在返回错误码
        if(cfg == null){
            return Result.fail(1);
        }
        //是否已经购买
        int buy = entity.getBuyMap().getOrDefault(cfg.getType(), 0);

        if(buy == 0){
            //如果没已经购买
            if(task.getState() != Activity.DevelopFastTaskState.DF_NOT_RECEIVED_VALUE){
                return Result.fail(1);
            }
            //设置活动状态
            task.setState(Activity.DevelopFastTaskState.DF_RECEIVED_VALUE);
            //发奖
            this.getDataGeter().takeReward(entity.getPlayerId(), cfg.getRewardList(), 1 , Action.DEVELOP_FAST_REWARD, true);
            //发积分
            if(cfg.getType() != Activity.DevelopFastTaskType.DF_LEVEL_VALUE){
                //触发主任务
                addCondition(playerId, Activity.DevelopFastTaskType.DF_LEVEL_VALUE, cfg.getScore());
            }
        }else {
            //如果已经购买了
            if(task.getState() == Activity.DevelopFastTaskState.DF_NOT_RECEIVED_VALUE){
                //设置活动状态
                task.setState(Activity.DevelopFastTaskState.DF_RECEIVED_TWO_VALUE);
                //发奖
                this.getDataGeter().takeReward(entity.getPlayerId(), cfg.getRewardList(), 2 , Action.DEVELOP_FAST_REWARD, true);
                //发积分
                if(cfg.getType() != Activity.DevelopFastTaskType.DF_LEVEL_VALUE){
                    //触发主任务
                    addCondition(playerId, Activity.DevelopFastTaskType.DF_LEVEL_VALUE, cfg.getScore());
                }
            }else if (task.getState() == Activity.DevelopFastTaskState.DF_RECEIVED_VALUE){
                //设置活动状态
                task.setState(Activity.DevelopFastTaskState.DF_RECEIVED_TWO_VALUE);
                //发奖
                this.getDataGeter().takeReward(entity.getPlayerId(), cfg.getRewardList(), 1 , Action.DEVELOP_FAST_REWARD, true);
            }else {
                return Result.fail(1);
            }
        }
        //入库
        entity.notifyUpdate();
        //同步数据
        syncActivityDataInfo(playerId);
        //返回成功
        return Result.success();
    }

    /**
     * 获得购买信息
     * @param playerId 玩家id
     * @param type 养成线类型
     * @return 购买信息
     */
    public Result<Integer> buyInfo(String playerId, Activity.DevelopFastTaskType type){
        //获得玩家数据
        Optional<DevelopFastOldEntity> opEntity = getPlayerDataEntity(playerId);
        //如果为空返回错误码
        if (!opEntity.isPresent()) {
            return Result.fail(1);
        }
        //获得玩家数据
        DevelopFastOldEntity entity = opEntity.get();
        //奖励内容
        List<Reward.RewardItem.Builder> list = new ArrayList<>();
        //遍历任务
        ConfigIterator<DevelopFastOldLevelCfg> iterator1 = HawkConfigManager.getInstance().getConfigIterator(DevelopFastOldLevelCfg.class);
        while (iterator1.hasNext()){
            DevelopFastOldLevelCfg cfg = iterator1.next();
            if(cfg.getType() != type.getNumber()){
                continue;
            }
            list.addAll(cfg.getRewardList());
        }
        ConfigIterator<DevelopFastOldAchieveCfg> iterator2 = HawkConfigManager.getInstance().getConfigIterator(DevelopFastOldAchieveCfg.class);
        while (iterator2.hasNext()){
            DevelopFastOldAchieveCfg cfg = iterator2.next();
            if(cfg.getCategory() != type.getNumber()){
                continue;
            }
            list.addAll(cfg.getRewardList());
        }
        //前端数据
        Activity.DevelopFastBuyResp.Builder builder = Activity.DevelopFastBuyResp.newBuilder();
        //设置养成线类型
        builder.setType(type);
        //设置奖励内容
        list = RewardHelper.mergeRewardItem(list);
        for (Reward.RewardItem.Builder item : list){
            builder.addReward(item);
        }
        //同步前端
        PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.DEVELOP_FAST_OLD_BUY_RESP_VALUE, builder));
        //返回成功
        return Result.success();
    }
}
