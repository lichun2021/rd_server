package com.hawk.activity.type.impl.newFirstRecharge;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.FirstRechargeEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.event.impl.RechargeMoneyEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.newFirstRecharge.cfg.NewFirstRechargeKVCfg;
import com.hawk.activity.type.impl.newFirstRecharge.cfg.NewFirstRechargeRewardCfg;
import com.hawk.activity.type.impl.newFirstRecharge.entity.NewFirstRechargeEntity;
import com.hawk.game.protocol.Activity.NewFirstRechargeInfo;
import com.hawk.game.protocol.Activity.NewFirstRechargeState;
import com.hawk.game.protocol.Activity.NewFirstRechargeSync;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Recharge;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 新首充活动
 * 说明：用于替换老首充活动
 * 活动详情：
 * 1，活动分为三天
 * 2，首充活动激活后，玩家可以直接获得第一天的奖励，后两天的奖励有两种方式可以获得，
 *      方法1：等待一定时间（第二天等待24小时，第三天等待48小时）
 *      方法2：连续充值（第二天再充值一笔，第三天再充值两笔，包含第二天那笔）
 * 3，扩展规则：当新首充开启之后，老首充激活了并且没有领奖的时候自动激活本活动
 */
public class NewFirstRechargeActivity extends ActivityBase {
    /**
     * 构造函数
     * @param activityId 活动id
     * @param activityEntity 活动数据库类
     */
    public NewFirstRechargeActivity(int activityId, ActivityEntity activityEntity) {
        //调用基类构造函数
        super(activityId, activityEntity);
    }

    /**
     * 获得活动类型
     * @return 活动类型
     */
    @Override
    public ActivityType getActivityType() {
        //返回活动类型，新首充
        return ActivityType.NEW_FIRST_RECHARGE;
    }

    /**
     * 初始化活动实例
     * @param config 活动配置
     * @param activityEntity 活动数据库类
     * @return 活动实例
     */
    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
        //创建活动实例并返回
        return new NewFirstRechargeActivity(config.getActivityId(), activityEntity);
    }

    /**
     * 从数据库加载玩家活动数据
     * @param playerId 玩家id
     * @param termId 活动期数配置id
     * @return 玩家活动数据数据库实例
     */
    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        //根据玩家id和活动期数id从数据库里面获得玩家数据
        List<NewFirstRechargeEntity> queryList = HawkDBManager.getInstance()
                .query("from NewFirstRechargeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        //数据不为空的情况下返回第一个数据
        if (queryList != null && queryList.size() > 0) {
            NewFirstRechargeEntity entity = queryList.get(0);
            return entity;
        }
        //如果为空，则返回空
        return null;
    }

    /**
     * 创建玩家活动数据
     * @param playerId 玩家id
     * @param termId 活动期数id
     * @return 玩家活动数据
     */
    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
        //创建活动数据实例
        NewFirstRechargeEntity entity = new NewFirstRechargeEntity(playerId, termId);
        //返回活动数据
        return entity;
    }

    @Override
    public boolean isActivityClose(String playerId) {
        //活动总配置
        NewFirstRechargeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(NewFirstRechargeKVCfg.class);
        if(cfg == null){
            return false;
        }
        //long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
        long serverOpenDate = getDataGeter().getServerOpenTime(playerId);
        if(serverOpenDate < cfg.getServerOpenValue()){
            return true;
        }
        //获得玩家活动数据
        Optional<NewFirstRechargeEntity> opEntity = getPlayerDataEntity(playerId);
        //如果没有直接返回错误码
        if (opEntity.isPresent()) {
            //获得活动数据
            NewFirstRechargeEntity entity = opEntity.get();
            //所有奖励配置迭代器
            ConfigIterator<NewFirstRechargeRewardCfg> iter = HawkConfigManager.getInstance().getConfigIterator(NewFirstRechargeRewardCfg.class);
            //奖励总数
            int num = 0;
            //已经领取数量
            int get = 0;
            //遍历奖励配置
            while (iter.hasNext()) {
                num++;
                //获得奖励配置
                NewFirstRechargeRewardCfg rewardCfg = iter.next();
                if(entity.getRewardStateMap().containsKey(rewardCfg.getId())){
                    get++;
                }
            }
            //如果有没领取的
            if(num == get){
                return true;
            }
        }
        return false;
    }

    /**
     * 同步活动数据
     * @param playerId 玩家id
     */
    @Override
    public void syncActivityDataInfo(String playerId) {
        //获得玩家活动数据
        Optional<NewFirstRechargeEntity> opEntity = getPlayerDataEntity(playerId);
        //如果没有直接返回错误码
        if (!opEntity.isPresent()) {
            return;
        }
        //获得活动数据
        NewFirstRechargeEntity entity = opEntity.get();
        //给前端发数据
        syncActivityInfo(playerId, entity);
    }

    /**
     * 给前端发数据
     * @param playerId 玩家id
     * @param entity 玩家活动数据
     */
    public void syncActivityInfo(String playerId, NewFirstRechargeEntity entity) {
        //活动信息
        NewFirstRechargeSync.Builder sync = NewFirstRechargeSync.newBuilder();
        //当前时间
        long now = HawkTime.getMillisecond();
        //所有奖励配置迭代器
        ConfigIterator<NewFirstRechargeRewardCfg> iter = HawkConfigManager.getInstance().getConfigIterator(NewFirstRechargeRewardCfg.class);
        //遍历奖励配置
        while (iter.hasNext()){
            //获得奖励配置
            NewFirstRechargeRewardCfg cfg = iter.next();
            //单天活动信息
            NewFirstRechargeInfo.Builder info = NewFirstRechargeInfo.newBuilder();
            //设置配置id
            info.setId(cfg.getId());
            //如果奖励已经被领取
            if(entity.getRewardStateMap().containsKey(cfg.getId())){
                //设置状态为已领取
                info.setState(NewFirstRechargeState.NFR_GET);
                //设置时间为long最大值
                info.setWaitTime(Long.MAX_VALUE);
            }else {
                //如果活动已经被激活
                if(entity.isActive()){
                    //如果实际充值数已经大于配置的充值数
                    if(entity.getPayCount() >= cfg.getPayCount()) {
                        //设置状态为可领取
                        info.setState(NewFirstRechargeState.NFR_CAN);
                    }else {
                        //如果当前时间已经大于等待时间
                        if(now > entity.getActiveTime() + cfg.getWaitTime()){
                            //设置状态为可领取
                            info.setState(NewFirstRechargeState.NFR_CAN);
                        }else {
                            //设置状态为已激活
                            info.setState(NewFirstRechargeState.NFR_ACTIVE);
                        }
                    }
                    //设置等待时间（激活时间+策划配置的等待时间）
                    info.setWaitTime(entity.getActiveTime() + cfg.getWaitTime());
                }else {
                    //设置状态为未激活
                    info.setState(NewFirstRechargeState.NFR_NOT);
                    //设置时间为long最大值
                    info.setWaitTime(Long.MAX_VALUE);
                }
            }
            //添加单天奖励信息
            sync.addInfos(info);
        }
        //设置已经弹窗的等级
        sync.setLevel(entity.getPopLevel());
        pushToPlayer(playerId, HP.code.NEW_FIRST_RECHARGE_SYNC_VALUE, sync);
    }

    /**
     * 响应首充事件的逻辑
     * @param event 首充事件
     */
    @Subscribe
    public void onFirstRechargeEvent(FirstRechargeEvent event) {
        String playerId = event.getPlayerId();
        if(!isOpening(playerId)){
            return;
        }
        //获得玩家活动数据
        Optional<NewFirstRechargeEntity> opEntity = getPlayerDataEntity(playerId);
        //如果没有直接返回错误码
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据
        NewFirstRechargeEntity entity = opEntity.get();
        //设置激活状态
        entity.setActive(true);
        //设置激活时间
        entity.setActiveTime(HawkTime.getMillisecond());
        //同步给前端
        syncActivityInfo(playerId, entity);
    }

    /**
     * 响应充值事件的逻辑
     * @param event 充值事件
     */
    @Subscribe
    public void onRechargeMoneyEvent(RechargeMoneyEvent event) {
        String playerId = event.getPlayerId();
        if(!isOpening(playerId)){
            return;
        }
        //获得玩家活动数据
        Optional<NewFirstRechargeEntity> opEntity = getPlayerDataEntity(playerId);
        //如果没有直接返回错误码
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据
        NewFirstRechargeEntity entity = opEntity.get();
        //玩家充值次数加1
        entity.setPayCount(entity.getPayCount() + 1);
        //同步给前端
        syncActivityInfo(playerId, entity);
    }

    /**
     * 响应直购时间的逻辑
     * @param event
     */
    @Subscribe
    public void onBuyGiftEvent(PayGiftBuyEvent event) {
        String playerId = event.getPlayerId();
        if(!isOpening(playerId)){
            return;
        }
        //获得玩家活动数据
        Optional<NewFirstRechargeEntity> opEntity = getPlayerDataEntity(playerId);
        //如果没有直接返回错误码
        if (!opEntity.isPresent()) {
            return;
        }
        //获得玩家活动数据
        NewFirstRechargeEntity entity = opEntity.get();
        //如果没激活
        if(!entity.isActive()){
            //设置激活状态
            entity.setActive(true);
            //设置激活时间
            entity.setActiveTime(HawkTime.getMillisecond());
            Recharge.HasFirstRechargeSync.Builder sync = Recharge.HasFirstRechargeSync.newBuilder();
            sync.setHasFirstRecharge(true);
            pushToPlayer(playerId, HP.code.HAS_FIRST_RECHARGE_SYNC_VALUE, sync);
        }
        //玩家充值次数加1
        entity.setPayCount(entity.getPayCount() + 1);
        //同步给前端
        syncActivityInfo(playerId, entity);
    }

    /**
     * 领奖
     * @param playerId 玩家id
     * @param cfgId 奖励配置id
     * @return 是否成功
     */
    public Result<Integer> reward(String playerId, int cfgId){
        //获得奖励配置
        NewFirstRechargeRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NewFirstRechargeRewardCfg.class, cfgId);
        //如果配置为空返回错误码
        if(cfg == null){
            //返回错误码，活动相关配置未找到
            return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据
        Optional<NewFirstRechargeEntity> opEntity = getPlayerDataEntity(playerId);
        //如果没有直接返回错误码
        if (!opEntity.isPresent()) {
            //返回错误码，活动数据不存在
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据
        NewFirstRechargeEntity entity = opEntity.get();
        //如果活动没激活直接返回错误码
        if(!entity.isActive()){
            //错误日志
            logger.info("NewFirstRechargeActivity reward not active playerid:{},cfgid:{}",playerId,cfgId);
            //返回错误码，活动数据不存在，此处为非法操作，前端不应该在这种情况下请求，所以随便回了一个错误码
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        if(entity.getRewardStateMap().containsKey(cfgId)){
            //错误日志
            logger.info("NewFirstRechargeActivity reward have get playerid:{},cfgid:{}",playerId,cfgId);
            //返回错误码，活动数据不存在，此处为非法操作，前端不应该在这种情况下请求，所以随便回了一个错误码
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        long now = HawkTime.getMillisecond();
        if(entity.getPayCount() < cfg.getPayCount() && now < entity.getActiveTime() + cfg.getWaitTime()){
            //错误日志
            logger.info("NewFirstRechargeActivity reward not reach playerid:{},cfgid:{}",playerId,cfgId);
            //返回错误码，活动数据不存在，此处为非法操作，前端不应该在这种情况下请求，所以随便回了一个错误码
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //设置状态为已领奖
        entity.getRewardStateMap().put(cfgId, NewFirstRechargeState.NFR_GET_VALUE);
        entity.notifyUpdate();
        //发奖
        this.getDataGeter().takeReward(playerId, cfg.getCommonAwardList(), 1 , Action.GRATEFUL_BENEFITS_REWARD, true);
        //同步数据给前端
        syncActivityInfo(playerId, entity);
        checkActivityClose(playerId);
        //返回成功
        return Result.success();
    }

    /**
     * 记录已经弹窗的数据给前端
     * @param playerId 玩家id
     * @param level 当前弹窗的等级（前端发过来的）
     * @return 是否成功
     */
    public Result<Integer> pop(String playerId, int level){
        //获得玩家活动数据
        Optional<NewFirstRechargeEntity> opEntity = getPlayerDataEntity(playerId);
        //如果没有直接返回错误码
        if (!opEntity.isPresent()) {
            return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
        }
        //获得玩家活动数据
        NewFirstRechargeEntity entity = opEntity.get();
        //设置当前弹窗等级
        entity.setPopLevel(level);
        //同步数据给前端
        syncActivityInfo(playerId, entity);
        //返回成功
        return Result.success();
    }
}
